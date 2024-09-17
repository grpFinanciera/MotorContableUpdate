package org.opentaps.warehouse.inventory;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilNumber;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.common.AB.UtilWorkflow;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

public class TransferenciasServices {

	private static final String MODULE = TransferenciasServices.class.getName();
	public static final DecimalFormat formatoDetalleId = new DecimalFormat("0000");
	
//	private static final String estatusCreada = "CREADA_ST";
	private static final String estatusEnviada = "ENVIADA_ST";
	private static final String estatusRechazada = "RECHAZADA_ST";
	private static final String estatusTransferida = "TRANSFERIDA_ST";
	private static final String estatusComentada = "COMENTADA_ST";
	private static final String estatusAtendida = "ATENDIDA_T";
	
	/**
	 * Crea una solicitud de transferencia entre almacenes
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> crearSolicitudTransferencia(DispatchContext dctx,
			Map<?, ?> context) {
		
		Debug.logInfo("Entrando a crear solicitud de transferencia", MODULE);
		
		Map<String, Object> regreso = FastMap.newInstance();
		
		Delegator delegator = dctx.getDelegator();
		GenericValue SolicitudTransferencia = delegator.makeValue("SolicitudTransferencia");
		SolicitudTransferencia.setNextSeqId();
		SolicitudTransferencia.setNonPKFields(context);
		try {
			SolicitudTransferencia.create();
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("No se puedo registrar la solicitud : "+e.getMessage());
		}
		
		regreso.put("solicitudTransferenciaId", SolicitudTransferencia.getString("solicitudTransferenciaId"));
		regreso.putAll(ServiceUtil.returnSuccess("Se registro correctamente la solicitud"));
		
		return regreso;
	}
	
	/**
	 * Agrega un producto a la transferencia 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> agregaItemProductoSolicitud(DispatchContext dctx,
			Map<?, ?> context){
		
		Debug.logInfo("Entrando a agregar producto a la solicitud", MODULE);
		
		Map<String, Object> regreso = FastMap.newInstance();
		
		Delegator delegator = dctx.getDelegator();
		String solicitudTransferenciaId = (String) context.get("solicitudTransferenciaId");
		String productId = (String) context.get("productId");
		Long cantidad = (Long) context.get("cantidad");
		
		DecimalFormat df = new DecimalFormat("0000");
		
		try {
			
			GenericValue SolicitudTransferencia = delegator.findByPrimaryKey("SolicitudTransferencia",UtilMisc.toMap("solicitudTransferenciaId",solicitudTransferenciaId));
			
			//Suma los productos del mismo tipo ya registrados en la solicitud para obtener la cantidad correcta
			cantidad = cantidad + getCantidadProducto(delegator, productId, solicitudTransferenciaId);
			
			//Valida que el almacen tenga la existencia suficiente para ser transferida
			if(!validaExistenciaAlmacen(delegator, productId, SolicitudTransferencia.getString("almacenOrigenId"), cantidad)){
				throw new GenericEntityException("El n\u00famero de productos son insuficientes "
						+ "para transferir del almacen ["+SolicitudTransferencia.getRelatedOne("OrigenFacility").getString("facilityName")+"]");
			}
			
			long numeroProducto = delegator.findCountByAnd("DetalleSolicitudTransfer",UtilMisc.toMap("solicitudTransferenciaId",solicitudTransferenciaId,"productId",productId));
			if(numeroProducto > 0){
				throw new GenericEntityException("No puede agregar el mismo producto");
			}
			
			long numero = delegator.findCountByAnd("DetalleSolicitudTransfer",UtilMisc.toMap("solicitudTransferenciaId",solicitudTransferenciaId));
			
			GenericValue DetalleSolicitudTransfer = delegator.makeValue("DetalleSolicitudTransfer");
			DetalleSolicitudTransfer.setPKFields(context);
			DetalleSolicitudTransfer.setNonPKFields(context);
			DetalleSolicitudTransfer.put("detalleSolicitudTransferId", df.format(numero+1));
			DetalleSolicitudTransfer.put("cantidadTransferida", new Long(0));
			DetalleSolicitudTransfer.create();
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("No se pudo agregar el producto ["+productId+"] : "+e.getMessage());
		}
		
		return regreso;
	}
	
	/**
	 * Agrega uno o varios productos a traves de una requisicion generada por el 
	 * usuario anteriormente y que no haya sido utilizada por el mismo con anterioridad
	 * @param dctx
	 * @return
	 */
	public static Map<String,Object> agregaRequisicionSolicitud (DispatchContext dctx, Map<String, Object> context){
		
		Debug.logInfo("Entrando a agregar requisicion a la solicitud", MODULE);
		
		Map<String, Object> regreso = FastMap.newInstance();
		
		Delegator delegator = dctx.getDelegator();
		String solicitudTransferenciaId = (String) context.get("solicitudTransferenciaId");
		String requisicionId = (String) context.get("requisicionId");
		
		List<GenericValue> listDetalleTransferencia = FastList.newInstance(); 
		
		try {
			
			GenericValue SolicitudTransferencia = delegator.findByPrimaryKey("SolicitudTransferencia",UtilMisc.toMap("solicitudTransferenciaId",solicitudTransferenciaId));
			
			//numero de detalle en el que va 
			List<GenericValue> listDetalleSolicitud = delegator.findByAnd("DetalleSolicitudTransfer",UtilMisc.toMap("solicitudTransferenciaId",solicitudTransferenciaId));
			long numero = listDetalleSolicitud.size()+1;
			
			Map<String,GenericValue> mapaProductoDetalleSolicitud = FastMap.newInstance();
			for (GenericValue DetalleSolicitudTransfer : listDetalleSolicitud) {
				mapaProductoDetalleSolicitud.put(DetalleSolicitudTransfer.getString("productId"), DetalleSolicitudTransfer);
			}
			
			//Validamos la suficiencia de los productos en almacen antes de realizar el proceso
			String mensaje = validaExistenciaAlmacenEntidad(delegator,"SumaCantidadProductoRequisicionTransferencia",
								requisicionId, solicitudTransferenciaId, SolicitudTransferencia.getString("almacenOrigenId"),mapaProductoDetalleSolicitud);
			
			if(UtilValidate.isNotEmpty(mensaje)){
				throw new GenericEntityException(mensaje);
			}
			
			//Se agregan los productos de la requisicion a la solicitud
			List<GenericValue> listProductosInventarioRequisicion = delegator.findByAnd("SumaCantidadProductoRequisicionTransferencia", UtilMisc.toMap("requisicionId",requisicionId));
			String productId = new String();
			Long cantidad = new Long(0);
			GenericValue DetalleSolicitudTransfer = null;
			for (GenericValue ProductosRequisicion : listProductosInventarioRequisicion) {
				productId = ProductosRequisicion.getString("productId");
				cantidad = ProductosRequisicion.getLong("cantidad");
				
				if(mapaProductoDetalleSolicitud.containsKey(productId)){
					DetalleSolicitudTransfer = mapaProductoDetalleSolicitud.get(productId);
					DetalleSolicitudTransfer.put("cantidad", cantidad + DetalleSolicitudTransfer.getLong("cantidad"));
				} else {
					DetalleSolicitudTransfer = delegator.makeValue("DetalleSolicitudTransfer");
					DetalleSolicitudTransfer.set("solicitudTransferenciaId", solicitudTransferenciaId);
					DetalleSolicitudTransfer.put("detalleSolicitudTransferId", formatoDetalleId.format(numero++));
					DetalleSolicitudTransfer.put("productId", productId);
					DetalleSolicitudTransfer.put("cantidad", cantidad);
					DetalleSolicitudTransfer.put("cantidadTransferida", new Long(0));
				}
				
				listDetalleTransferencia.add(DetalleSolicitudTransfer);

			}
			
			//Por utlimo se actualiza la requisicion para no ser utilizada nuevamente
			GenericValue Requisicion = delegator.findByPrimaryKey("Requisicion",UtilMisc.toMap("requisicionId",requisicionId));
			Requisicion.set("solicitudTransferenciaId", solicitudTransferenciaId);
			Requisicion.store();
			
			delegator.storeAll(listDetalleTransferencia);
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("No se pudo agregar la requisicion ["+requisicionId+"] : "+e.getMessage());
		}
		
		regreso = ServiceUtil.returnSuccess("Se agregaron correctamente los productos de la requisici\u00f3n ["+requisicionId+"]");
		
		return regreso;
	}
	
	/**
	 * Agrega uno o varios productos a traves de un pedido generado por el 
	 * usuario anteriormente y que no haya sido utilizado por el mismo con anterioridad
	 * @param dctx
	 * @return
	 */
	public static Map<String,Object> agregaOrdenSolicitud (DispatchContext dctx, Map<String, Object> context){
		
		Debug.logInfo("Entrando a agregar pedido a la solicitud", MODULE);
		
		Map<String, Object> regreso = FastMap.newInstance();
		
		Delegator delegator = dctx.getDelegator();
		String solicitudTransferenciaId = (String) context.get("solicitudTransferenciaId");
		String orderId = (String) context.get("orderId");
		
		List<GenericValue> listDetalleTransferencia = FastList.newInstance(); 
		
		try {
			
			GenericValue SolicitudTransferencia = delegator.findByPrimaryKey("SolicitudTransferencia",UtilMisc.toMap("solicitudTransferenciaId",solicitudTransferenciaId));
			
			//numero de detalle en el que va 
			List<GenericValue> listDetalleSolicitud = delegator.findByAnd("DetalleSolicitudTransfer",UtilMisc.toMap("solicitudTransferenciaId",solicitudTransferenciaId));
			long numero = listDetalleSolicitud.size()+1;
			
			Map<String,GenericValue> mapaProductoDetalleSolicitud = FastMap.newInstance();
			for (GenericValue DetalleSolicitudTransfer : listDetalleSolicitud) {
				mapaProductoDetalleSolicitud.put(DetalleSolicitudTransfer.getString("productId"), DetalleSolicitudTransfer);
			}
			
			//Validamos la suficiencia de los productos en almacen antes de realizar el proceso
			String mensaje = validaExistenciaAlmacenEntidad(delegator,"SumaCantidadProductoPedidoTransferencia",
					orderId, solicitudTransferenciaId, SolicitudTransferencia.getString("almacenOrigenId"),mapaProductoDetalleSolicitud);
			
			if(UtilValidate.isNotEmpty(mensaje)){
				throw new GenericEntityException(mensaje);
			}
			
			//Se agregan los productos del pedido a la solicitud
			List<GenericValue> listProductosInventarioRequisicion = delegator.findByAnd("SumaCantidadProductoPedidoTransferencia", UtilMisc.toMap("orderId",orderId));
			String productId = new String();
			Long cantidad = new Long(0);
			GenericValue DetalleSolicitudTransfer = null;
			for (GenericValue ProductosRequisicion : listProductosInventarioRequisicion) {
				productId = ProductosRequisicion.getString("productId");
				cantidad = ProductosRequisicion.getBigDecimal("cantidad").longValue();
				
				if(mapaProductoDetalleSolicitud.containsKey(productId)){
					DetalleSolicitudTransfer = mapaProductoDetalleSolicitud.get(productId);
					DetalleSolicitudTransfer.put("cantidad", cantidad + DetalleSolicitudTransfer.getLong("cantidad"));
				} else {
					DetalleSolicitudTransfer = delegator.makeValue("DetalleSolicitudTransfer");
					DetalleSolicitudTransfer.set("solicitudTransferenciaId", solicitudTransferenciaId);
					DetalleSolicitudTransfer.put("detalleSolicitudTransferId", formatoDetalleId.format(numero++));
					DetalleSolicitudTransfer.put("productId", productId);
					DetalleSolicitudTransfer.put("cantidad", cantidad);
					DetalleSolicitudTransfer.put("cantidadTransferida", new Long(0));
				}
				
				listDetalleTransferencia.add(DetalleSolicitudTransfer);
			}
			
			//Por utlimo se actualiza la requisicion para no ser utilizada nuevamente
			GenericValue OrderHeader = delegator.findByPrimaryKey("OrderHeader",UtilMisc.toMap("orderId",orderId));
			OrderHeader.set("solicitudTransferenciaId", solicitudTransferenciaId);
			OrderHeader.store();
			
			delegator.storeAll(listDetalleTransferencia);
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("No se pudo agregar el pedido o contrato ["+orderId+"] : "+e.getMessage());
		}
		
		regreso = ServiceUtil.returnSuccess("Se agregaron correctamente los productos del pedido o contrato ["+orderId+"]");
		
		return regreso;
	}
	
	/**
	 * Agrega uno o varios productos a traves de un pedido interno generado por el usuario anteriormente 
	 * y que no haya sido utilizado por el mismo con anterioridad
	 * @param dctx
	 * @return
	 */
	public static Map<String,Object> agregaPedidoInterno (DispatchContext dctx, Map<String, Object> context){
		
		Debug.logInfo("Entrando a agregar pedido interno a la solicitud", MODULE);
		
		Map<String, Object> regreso = FastMap.newInstance();
		
		Delegator delegator = dctx.getDelegator();
		String solicitudTransferenciaId = (String) context.get("solicitudTransferenciaId");
		String pedidoInternoId = (String) context.get("pedidoInternoId");
		
		List<GenericValue> listDetalleTransferencia = FastList.newInstance(); 
		
		try {
			
			GenericValue SolicitudTransferencia = delegator.findByPrimaryKey("SolicitudTransferencia",UtilMisc.toMap("solicitudTransferenciaId",solicitudTransferenciaId));
			
			//numero de detalle en el que va 
			List<GenericValue> listDetalleSolicitud = delegator.findByAnd("DetalleSolicitudTransfer",UtilMisc.toMap("solicitudTransferenciaId",solicitudTransferenciaId));
			long numero = listDetalleSolicitud.size()+1;
			
			Map<String,GenericValue> mapaProductoDetalleSolicitud = FastMap.newInstance();
			for (GenericValue DetalleSolicitudTransfer : listDetalleSolicitud) {
				mapaProductoDetalleSolicitud.put(DetalleSolicitudTransfer.getString("productId"), DetalleSolicitudTransfer);
			}
			
			//Validamos la suficiencia de los productos en almacen antes de realizar el proceso
			String mensaje = validaExistenciaAlmacenEntidad(delegator,"SumaCantidadProductoPedidoInternoTransferencia",
					pedidoInternoId, solicitudTransferenciaId, SolicitudTransferencia.getString("almacenOrigenId"),mapaProductoDetalleSolicitud);
			
			if(UtilValidate.isNotEmpty(mensaje)){
				throw new GenericEntityException(mensaje);
			}
			
			//Se agregan los productos de la requisicion a la solicitud
			List<GenericValue> listProductosInventarioPedidoInterno = delegator.findByAnd("SumaCantidadProductoPedidoInternoTransferencia", UtilMisc.toMap("pedidoInternoId",pedidoInternoId));
			String productId = new String();
			Long cantidad = new Long(0);
			GenericValue DetalleSolicitudTransfer = null;
			for (GenericValue ProductosPedidoInterno : listProductosInventarioPedidoInterno) {
				productId = ProductosPedidoInterno.getString("productId");
				cantidad = ProductosPedidoInterno.getLong("cantidad");
				
				if(mapaProductoDetalleSolicitud.containsKey(productId)){
					DetalleSolicitudTransfer = mapaProductoDetalleSolicitud.get(productId);
					DetalleSolicitudTransfer.put("cantidad", cantidad + DetalleSolicitudTransfer.getLong("cantidad"));
				} else {
					DetalleSolicitudTransfer = delegator.makeValue("DetalleSolicitudTransfer");
					DetalleSolicitudTransfer.set("solicitudTransferenciaId", solicitudTransferenciaId);
					DetalleSolicitudTransfer.put("detalleSolicitudTransferId", formatoDetalleId.format(numero++));
					DetalleSolicitudTransfer.put("productId", productId);
					DetalleSolicitudTransfer.put("cantidad", cantidad);
					DetalleSolicitudTransfer.put("cantidadTransferida", new Long(0));
				}
				
				listDetalleTransferencia.add(DetalleSolicitudTransfer);
			}
			
			//Por utlimo se actualiza el pedido interno para no ser utilizado nuevamente
			GenericValue PedidoInterno = delegator.findByPrimaryKey("PedidoInterno",UtilMisc.toMap("pedidoInternoId",pedidoInternoId));
			PedidoInterno.set("solicitudTransferenciaId", solicitudTransferenciaId);
			PedidoInterno.set("statusId", "TRANSFERIDO_PI");
			PedidoInterno.store();
			
			delegator.storeAll(listDetalleTransferencia);
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("No se pudo agregar el pedido interno ["+pedidoInternoId+"] : "+e.getMessage());
		}
		
		regreso = ServiceUtil.returnSuccess("Se agregaron correctamente los productos del pedido interno ["+pedidoInternoId+"]");
		
		return regreso;
	}
	
	/**
	 * Obtiene la cantidad actual de la solicitud de un prodcuto especifico
	 * @param delegator
	 * @param productId
	 * @param solicitudTransferenciaId
	 * @return
	 * @throws GenericEntityException
	 */
	private static Long getCantidadProducto(Delegator delegator, String productId, String solicitudTransferenciaId) throws GenericEntityException{
		
		List<GenericValue> listDetalleSolicitud = delegator.findByAnd("SumaCantidadProductoTransferencia", 
				UtilMisc.toMap("productId",productId,"solicitudTransferenciaId",solicitudTransferenciaId));
		
		Long totalProductos = new Long(0);
		
		for (GenericValue DetalleSolicitudSuma : listDetalleSolicitud) {
			totalProductos = totalProductos + DetalleSolicitudSuma.getLong("cantidad");
		}
		
		return totalProductos;
	}
	
	/**
	 * Valida que el almacen tenga el numero de productos recibidos 
	 * @param delegator
	 * @param productId
	 * @param almacenId
	 * @param cantidad
	 * @return
	 * @throws GenericEntityException
	 */
	public static boolean validaExistenciaAlmacen(Delegator delegator,
					String productId, String almacenId, Long cantidad) throws GenericEntityException{
		
		List<GenericValue> listProductosInventario = delegator.findByAnd("ProductoInventarioOrder", UtilMisc.toMap("productId",productId,"facilityId",almacenId));
		
		Long totalProductos = new Long(0);

		for (GenericValue ProductoInventario : listProductosInventario) {
			totalProductos = totalProductos + ProductoInventario.getBigDecimal("quantityOnHandTotal").longValue();
		}
		
		return totalProductos >= cantidad;
	}
	
	/**
	 * Valida que el almacen tenga el numero de productos recibidos desde una requisicion
	 * @param delegator
	 * @param productId
	 * @param almacenId
	 * @param mapaProductoDetalleSolicitud 
	 * @param cantidad
	 * @return
	 * @throws GenericEntityException
	 */
	private static String validaExistenciaAlmacenEntidad(Delegator delegator, String nombreEntidad, String campoEntidadId, String solicitudTransferenciaId,
						String almacenId, Map<String, GenericValue> mapaProductoDetalleSolicitud) throws GenericEntityException{
		
		
		GenericValue Almacen = delegator.findByPrimaryKey("Facility",UtilMisc.toMap("facilityId",almacenId));
		String nombreAlmacen = Almacen.getString("facilityName");
		
		StringBuffer mensajeError = new StringBuffer();
		
		//Se obtiene la suma por producto 
		Map<String,Object> mapCantidadProducto = FastMap.newInstance();
		String productId = new String();
		List<GenericValue> listProductosTransferencia = delegator.findByAnd("SumaCantidadProductoTransferencia", UtilMisc.toMap("solicitudTransferenciaId",solicitudTransferenciaId));
		for (GenericValue ProductoTransferencia : listProductosTransferencia) {
			productId = ProductoTransferencia.getString("productId");
			mapCantidadProducto.put(productId, BigDecimal.valueOf(ProductoTransferencia.getLong("cantidad")));
		}
		
		Map<String,String> mapaFiltro = FastMap.newInstance();
		String textoError = new String();
		String textoEntidad = new String();
		if(nombreEntidad.equals("SumaCantidadProductoRequisicionTransferencia")){
			mapaFiltro.put("requisicionId",campoEntidadId);
			textoError = "esta requisici\u00f3n";
			textoEntidad = "La requisici\u00f3n";
		} else if(nombreEntidad.equals("SumaCantidadProductoPedidoTransferencia")){
			mapaFiltro.put("orderId",campoEntidadId);
			textoError = "este pedido o contrato";
			textoEntidad = "El pedido o contrato";
		}else if(nombreEntidad.equals("SumaCantidadProductoPedidoInternoTransferencia")){
			mapaFiltro.put("pedidoInternoId",campoEntidadId);
			textoError = "este pedido interno";
			textoEntidad = "El pedido interno";
		}else{
			throw new GenericEntityException("No se encuentra la entidad por favor contacte al administrador.");
		}
		
		List<GenericValue> listProductosInventarioEntidad = delegator.findByAnd(nombreEntidad, mapaFiltro);
		if(UtilValidate.isEmpty(listProductosInventarioEntidad)){
			throw new GenericEntityException(textoEntidad+" no tiene art\u00edculos");
		}
		
		for (GenericValue ProductosRequisicion : listProductosInventarioEntidad) {
			productId = ProductosRequisicion.getString("productId");
			if(ProductosRequisicion.get("cantidad") instanceof Long){
				mapCantidadProducto.put(productId, BigDecimal.valueOf(ProductosRequisicion.getLong("cantidad")));
			} else {
				mapCantidadProducto.put(productId, ProductosRequisicion.getBigDecimal("cantidad"));
			}
			//Se agregan los productos que ya estan en la solicitud para ser validados
			if(mapaProductoDetalleSolicitud.containsKey(productId)){
				UtilMisc.addToBigDecimalInMap(mapCantidadProducto, productId, 
						BigDecimal.valueOf(mapaProductoDetalleSolicitud.get(productId).getLong("cantidad")));
			}
		}
		
		//Se obtienen los totales de productos disponibles en inventario
		EntityCondition condicionProducto = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("productId", EntityOperator.IN, mapCantidadProducto.keySet()),
				EntityCondition.makeCondition("facilityId", almacenId));
		List<GenericValue> listProductosInventario = delegator.findByAnd("ProductoInventarioOrder", condicionProducto);
		
		if(UtilValidate.isEmpty(listProductosInventario)){
			throw new GenericEntityException("No se cuenta con productos suficientes para transferir a partir de "+textoError);
		}
		
		Map<String,Object> mapaProductosInventario = FastMap.newInstance();
		//Se guarda en un mapa los productos disponibles
		for (GenericValue ProductoInventario : listProductosInventario) {
			productId = ProductoInventario.getString("productId");
			UtilMisc.addToBigDecimalInMap(mapaProductosInventario, productId, ProductoInventario.getBigDecimal("quantityOnHandTotal"));
		}

		//Se compara cada producto por la cantidad disponible
		for (Map.Entry<String, Object> EntryCantidadProduct : mapCantidadProducto.entrySet()) {
			Long cantidadATransferir = UtilNumber.getBigDecimal(((BigDecimal)EntryCantidadProduct.getValue())).longValue();
			Long cantidadDisponible = UtilNumber.getBigDecimal((BigDecimal)mapaProductosInventario.get(EntryCantidadProduct.getKey())).longValue();
			
			if(cantidadATransferir > cantidadDisponible){
				mensajeError.append("El n\u00famero de productos ["+EntryCantidadProduct.getKey()+"] son insuficientes "
						+ "para transferir del almacen ["+nombreAlmacen+"] \n");
			}
		}
		
		return mensajeError.toString();
	}
	
	/**
	 * Envia la solicitud de transferencia al work flow
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map<String, Object> enviarSolicitudTransferencia(DispatchContext dctx, Map<String, Object> context) throws GenericEntityException
	{
		
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		String solicitudTransferenciaId = (String) context.get("solicitudTransferenciaId");
		String urlHost = (String) context.get("urlHost");
		
		GenericValue SolicitudTransferencia = delegator.findByPrimaryKey("SolicitudTransferencia", UtilMisc.toMap("solicitudTransferenciaId",solicitudTransferenciaId));
		String areaPartyId = SolicitudTransferencia.getString("areaPartyId");
		String organizationPartyId = SolicitudTransferencia.getString("organizationPartyId");
		
		Map<String, Object> regreso = ServiceUtil.returnSuccess();	
		
		//Valida si la solicitud tiene items 
		validaItemsSolicitudTransferencia(delegator, solicitudTransferenciaId);	
		
		//Cambia el estatus de la solicitud a enviada
		cambiaStatusSolicitudTransferencia(delegator, solicitudTransferenciaId,estatusEnviada);
		
		Map<String,Object> mapaNuevoWF = UtilWorkflow.validaNuevoWorkFlow(delegator, solicitudTransferenciaId, UtilWorkflow.tipoTransferencia);
		Boolean nuevoWorkflow = (Boolean) mapaNuevoWF.get("nuevoWorkflow");
		String workFlowId = (String) mapaNuevoWF.get("workFlowId");
		
		//Agrega registro en Control_Workflow
		UtilWorkflow.registroControlWorkflow(dctx, context, solicitudTransferenciaId, UtilWorkflow.tipoTransferencia, workFlowId);
		GenericValue autorizador = null;
		if(nuevoWorkflow)
		{	//Obtener autorizador(es) por area. Registro en Status_Workflow (autorizadores, status PENDIENTE y secuencia de autorizadores		
			UtilWorkflow.registroEstatusWorkflow(dctx, context, areaPartyId, organizationPartyId, workFlowId, UtilWorkflow.estatusPendiente, UtilWorkflow.tipoTransferencia);
			autorizador = UtilWorkflow.obtenAutorizador(areaPartyId, organizationPartyId, 1, delegator, UtilWorkflow.tipoTransferencia);
		}
		else
		{	//Actualiza status en Status_Workflow
			List<GenericValue> statusWorkFlowList = UtilWorkflow.getStatusWorkFlow(workFlowId, UtilWorkflow.estatusComentada, delegator);
			if(statusWorkFlowList.isEmpty())
			{	return ServiceUtil.returnError("No se han encontrado autorizadores asociados al Workflow");			
			}
			else
			{	Iterator<GenericValue> statusWorkFlowIter = statusWorkFlowList.iterator();
				while (statusWorkFlowIter.hasNext()) 
				{	GenericValue statusWorkFlow = statusWorkFlowIter.next();								
					statusWorkFlow.set("statusId", UtilWorkflow.estatusPendiente);
					delegator.store(statusWorkFlow);
					Long nivel = statusWorkFlow.getLong("nivelAutor");
					autorizador = UtilWorkflow.obtenAutorizador(areaPartyId, organizationPartyId, nivel.intValue(), delegator, UtilWorkflow.tipoTransferencia);
				}
			}
		}
		
		//Enviar correo de notificacion al autorizador
		String correoOrigen = SolicitudTransferencia.getString("personaSolicitanteId");
		String correoDestino = autorizador.getString("autorizadorId");
		UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,UtilWorkflow.tipoTransferencia,UtilWorkflow.CorreoAutorizacion,
				urlHost,solicitudTransferenciaId,null,delegator,locale,dctx.getDispatcher());
		
		regreso.put("solicitudTransferenciaId", solicitudTransferenciaId);
		regreso.putAll(ServiceUtil.returnSuccess("La solicitud fue enviada correctamente"));
		
		return regreso;
		
	}

	/**
	 * Valida si la solicitud de transferencia contiene articulos
	 * @param delegator
	 * @param solicitudTransferenciaId
	 * @throws GenericEntityException
	 */
	private static void validaItemsSolicitudTransferencia(Delegator delegator,
			String solicitudTransferenciaId) throws GenericEntityException {
		
		long numeroSolicitud = delegator.findCountByAnd("DetalleSolicitudTransfer", UtilMisc.toMap("solicitudTransferenciaId",solicitudTransferenciaId));
		
		if(numeroSolicitud <= 0){
			throw new GenericEntityException("La solicitud de transferencia no contiene art\u00edculos");
		}
		
	}
	
	/**
	 * Cambia estatus de la Solicitud de Transferencia
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static void cambiaStatusSolicitudTransferencia(Delegator delegator, String solicitudTransferenciaId, String estatus) throws GenericEntityException 
	{	
		try
		{	GenericValue pedidoInterno = delegator.findByPrimaryKey("SolicitudTransferencia", UtilMisc.toMap("solicitudTransferenciaId",solicitudTransferenciaId));
			pedidoInterno.set("statusId", estatus);	
			delegator.store(pedidoInterno);
		}
		catch (GenericEntityException e) 
		{	
			throw new GenericEntityException("No se actualiz\u00f3 la solicitud : "+e.getMessage());
		}
	}
	
	/**
	 * Comenta la solicitud de transferencia
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map<String, Object> comentarSolicitudTransferencia(DispatchContext dctx, Map<?, ?> context) throws GenericEntityException 
	{	
		
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		String solicitudTransferenciaId = (String) context.get("solicitudTransferenciaId");
		String comentario = (String) context.get("comentarioComentar");
		String rol = (String) context.get("rol");
		String urlHost = (String) context.get("urlHost");
		String estatus = rol.equalsIgnoreCase("AUTORIZADOR")?estatusComentada:estatusEnviada;
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String userLoginpartyId = userLogin.getString("partyId");
		
		Map<String, Object> result = ServiceUtil.returnSuccess();
		
		Debug.logInfo("rol.- " + rol,MODULE);
		Debug.logInfo("Comentar Transferencia",MODULE);		
		try
		{	
			GenericValue SolicitudTransferencia = delegator.findByPrimaryKey("SolicitudTransferencia", UtilMisc.toMap("solicitudTransferenciaId",solicitudTransferenciaId));
			//Actualizar la transferenacia en comentada
			cambiaStatusSolicitudTransferencia(delegator, solicitudTransferenciaId, estatus);

			//Obtener el workFlowId	
			String workFlowId = UtilWorkflow.obtenerWorkFlowId(dctx, context, solicitudTransferenciaId, UtilWorkflow.tipoTransferencia);
			
			//Actualizar STATUS_WORK_FLOW en rechazado SOLO para el autorizador actual
			UtilWorkflow.cambiarStatusAutorizadorActual(dctx, context, workFlowId, UtilWorkflow.estatusComentada, userLoginpartyId);
			
			//Insertar comentario
			if(UtilValidate.isNotEmpty(comentario))
			{	
				UtilWorkflow.insertarComentario(dctx, context, comentario, workFlowId, userLoginpartyId, solicitudTransferenciaId);					
			}			
			
			//Enviar correo de notificacion al solicitante
			String correoOrigen = userLogin.getString("partyId");
			String correoDestino = SolicitudTransferencia.getString("personaSolicitanteId");
			UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,UtilWorkflow.tipoTransferencia,UtilWorkflow.CorreoComentario,
					urlHost,solicitudTransferenciaId,correoOrigen,
					delegator,locale,dctx.getDispatcher());
			
		}
		catch (GenericEntityException e)
		{	
			e.printStackTrace();
			return ServiceUtil.returnError("No se pudo comentar correctamente : "+e.getMessage());
		}		
		
		result.put("solicitudTransferenciaId", solicitudTransferenciaId);
		result.putAll(ServiceUtil.returnSuccess("Se comento correctamente"));
		return result;
	}
	
	/**
	 * Metodo para rechazar la solicitud de transferencia
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> rechazarSolicitudTransferencia(DispatchContext dctx, Map<?, ?> context) throws GenericEntityException 
	{	
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		String solicitudTransferenciaId = (String) context.get("solicitudTransferenciaId");
		String comentario = (String) context.get("comentarioRechazo");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String userLoginpartyId = userLogin.getString("partyId");	
		String urlHost = (String) context.get("urlHost");
		Locale locale = (Locale) context.get("locale");
		
		Debug.logInfo("Rechazar Transferencia",MODULE);		
		try
		{	
			GenericValue SolicitudTransferencia = delegator.findByPrimaryKey("SolicitudTransferencia", UtilMisc.toMap("solicitudTransferenciaId",solicitudTransferenciaId));
			
			//Actualizar la transferencia a rechazada
			cambiaStatusSolicitudTransferencia(delegator, solicitudTransferenciaId, estatusRechazada);
			
			//Obtener el workFlowId		
			String workFlowId = UtilWorkflow.obtenerWorkFlowId(dctx, context, solicitudTransferenciaId, UtilWorkflow.tipoTransferencia);
			
			//Actualizar STATUS_WORK_FLOW en rechazado para todos los autorizadores
			if(!workFlowId.equals(""))
			{	
				UtilWorkflow.cambiarStatusAutorizadores(dctx, context, workFlowId, UtilWorkflow.estatusRechazada);
			}				
			//Insertar comentario
			if(comentario != null && !comentario.equals(""))
			{	
				UtilWorkflow.insertarComentario(dctx, context, comentario, workFlowId, userLoginpartyId, solicitudTransferenciaId);					
			}			
			
			//Enviar correo de notificacion al solicitante
			String correoOrigen = SolicitudTransferencia.getString("personaSolicitanteId");
			String correoDestino = userLogin.getString("partyId");
			UtilWorkflow.armarEnviarCorreo(correoDestino,correoOrigen,UtilWorkflow.tipoTransferencia,UtilWorkflow.CorreoRechazo,
					urlHost,solicitudTransferenciaId,correoDestino,
					delegator,locale,dctx.getDispatcher());
			
			//Borramos la relacion de Requicision y/o Orden de cobro que pudieran existir.
			List <GenericValue> requisiciones = delegator.findByAnd("Requisicion", "solicitudTransferenciaId", solicitudTransferenciaId);
			List <GenericValue> ordenes = delegator.findByAnd("OrderHeader", "solicitudTransferenciaId", solicitudTransferenciaId);
			
			for(GenericValue req : requisiciones){
				req.set("solicitudTransferenciaId", null);
				delegator.store(req);
			}
			
			for(GenericValue ord : ordenes){
				ord.set("solicitudTransferenciaId", null);
				delegator.store(ord);
			}
		}
		catch (GenericEntityException e)
		{	
			e.printStackTrace();
			return ServiceUtil.returnError("No se pudo rechazar correctamente : "+e.getMessage());
		}		
		
		result.put("solicitudTransferenciaId", solicitudTransferenciaId);
		result.putAll(ServiceUtil.returnSuccess("Se rechaz\u00f3 correctamente"));
		return result;
			
	}
	
	
	public static Map<String, Object> autorizarSolicitudTransferencia(DispatchContext dctx, Map<?, ?> context) throws GenericEntityException
	{
		
		Map<String, Object> result = ServiceUtil.returnSuccess();
		
		Delegator delegator = dctx.getDelegator();
		String comentario = (String) context.get("comentarioAutorizado");
		String solicitudTransferenciaId = (String) context.get("solicitudTransferenciaId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String userLoginpartyId = userLogin.getString("partyId");
		String urlHost = (String) context.get("urlHost");
		Locale locale = (Locale) context.get("locale");
		
		Debug.logInfo("Autorizar Transferencia",MODULE);
		Debug.logInfo("Comentario Solicitud: " + comentario,MODULE);
		
		try 
		{
			//Cambiar status de STATUS_WORKFLOW para el autorizador en curso*/			
			String workFlowId = UtilWorkflow.obtenerWorkFlowId(dctx, context, solicitudTransferenciaId, UtilWorkflow.tipoTransferencia);
			UtilWorkflow.cambiarStatusAutorizadorActual(dctx, context, workFlowId, UtilWorkflow.estatusAutorizada, userLoginpartyId);
			
			//Validar si contiene otro autorizador
			//Obtener area de la solicitud
			GenericValue SolicitudTransferencia = delegator.findByPrimaryKey("SolicitudTransferencia", UtilMisc.toMap("solicitudTransferenciaId",solicitudTransferenciaId));
			String areaPartyId = SolicitudTransferencia.getString("areaPartyId");
			
			
			//Obtener secuencia del autorizador actual
			Long secuencia = UtilWorkflow.getSecuenciaAutorizador(delegator, areaPartyId, userLoginpartyId, UtilWorkflow.tipoTransferencia);
			
			//Validar si contiene otro autorizador obteniendo la secuencia siguiente
			GenericValue AutorizadorSiguiente = UtilWorkflow.getSiguienteAutorizador(delegator, areaPartyId, secuencia);
			
			if(UtilValidate.isNotEmpty(AutorizadorSiguiente))
			{	//Se crea registro de nuevo autorizador en la tabla STATUS_WORKFLOW
				UtilWorkflow.guardaStatusWorkFlow(delegator, AutorizadorSiguiente, workFlowId);
				
				String correoOrigen = SolicitudTransferencia.getString("personaSolicitanteId");
				String correoDestino = AutorizadorSiguiente.getString("autorizadorId");
				//Enviar correo electronico al solicitante
				UtilWorkflow.armarEnviarCorreo(userLoginpartyId,correoOrigen,UtilWorkflow.tipoTransferencia,UtilWorkflow.CorreoAutorizado,
						urlHost,solicitudTransferenciaId,userLoginpartyId,
						delegator,locale,dctx.getDispatcher());
				//Enviar correo electronico al nuevo autorizador
				UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,UtilWorkflow.tipoTransferencia,UtilWorkflow.CorreoAutorizacion,
						urlHost,solicitudTransferenciaId,null,
						delegator,locale,dctx.getDispatcher());
			}
			else
			{	
				//Se guarda la fecha de autorizacion y cambia el estatus
				SolicitudTransferencia.set("statusId", estatusAtendida);	
				SolicitudTransferencia.put("fechaAutorizacion",UtilDateTime.nowTimestamp());
				SolicitudTransferencia.store();
				
				String correoOrigen = userLoginpartyId;
				String correoDestino = SolicitudTransferencia.getString("personaSolicitanteId");
				//Enviar correo electronico al solicitante
				UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,UtilWorkflow.tipoTransferencia,UtilWorkflow.CorreoAutorizado,
						urlHost,solicitudTransferenciaId,correoOrigen,
						delegator,locale,dctx.getDispatcher());
			}
												
		}
		catch (GenericEntityException e)
		{	
			e.printStackTrace();
			return ServiceUtil.returnError("No se pudo autorizar correctamente : "+e.getMessage());
		}		
		
		result.put("solicitudTransferenciaId", solicitudTransferenciaId);
		result.putAll(ServiceUtil.returnSuccess("Se autoriz\u00f3 correctamente"));
		return result;
		
	}
	
	/**
	 * Se transfieren los productos entre almacenes
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String,Object> transferirSolicitud(DispatchContext dctx, Map<?, ?> context){

		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		
		String solicitudTransferenciaId = (String) context.get("solicitudTransferenciaId");
		Map<String, String> mapaDetalleId = UtilGenerics.checkMap(context.get("mapaDetalleId"));
		Map<String, String> mapaProductId = UtilGenerics.checkMap(context.get("mapaProductId"));
		Map<String, String> mapaCantidad = UtilGenerics.checkMap(context.get("mapaCantidad"));
		Map<String, String> mapaBandera = UtilGenerics.checkMap(context.get("mapaBandera"));
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		
		Map<String,Object> regreso = FastMap.newInstance();
		regreso.put("solicitudTransferenciaId", solicitudTransferenciaId);
		
		try {
			
			GenericValue SolicitudTransferencia = delegator.findByPrimaryKey("SolicitudTransferencia", UtilMisc.toMap("solicitudTransferenciaId",solicitudTransferenciaId));
			List<GenericValue> listDetalleSolicitudTransfer = delegator.findByAnd("DetalleSolicitudTransfer", UtilMisc.toMap("solicitudTransferenciaId",solicitudTransferenciaId));
			String almacenDestinoId = SolicitudTransferencia.getString("almacenDestinoId");
		
			List<EntityCondition> condicionesInventory = new FastList<EntityCondition>();
			condicionesInventory.add(EntityCondition.makeCondition("productId",EntityOperator.IN,mapaProductId.values()));
			condicionesInventory.add(EntityCondition.makeCondition("quantityOnHandTotal",EntityOperator.GREATER_THAN,BigDecimal.ZERO));
			
			EntityCondition condicionAlmacenOrigen = EntityCondition.makeCondition("facilityId",SolicitudTransferencia.getString("almacenOrigenId"));
			EntityCondition condicionAlmacenDestino = EntityCondition.makeCondition("facilityId",SolicitudTransferencia.getString("almacenDestinoId"));
			
			List<EntityCondition> condicionesOrigen = new FastList<EntityCondition>();
			condicionesOrigen.addAll(condicionesInventory);
			condicionesOrigen.add(condicionAlmacenOrigen);
			
			List<EntityCondition> condicionesDestino = new FastList<EntityCondition>();
			condicionesDestino.add(condicionesInventory.get(0));
			condicionesDestino.add(condicionAlmacenDestino);
			
			Map<String,GenericValue> mapaDetalleSolicitudId = FastMap.newInstance();
			Map<String,GenericValue> mapaProductoDetalleSolicitud = FastMap.newInstance();
			for (GenericValue DetalleSolicitudTransfer : listDetalleSolicitudTransfer) {
				mapaDetalleSolicitudId.put(DetalleSolicitudTransfer.getString("detalleSolicitudTransferId"), 
												DetalleSolicitudTransfer);
				mapaProductoDetalleSolicitud.put(DetalleSolicitudTransfer.getString("productId"), DetalleSolicitudTransfer);
			}
			
			//Suma de los productos
			Map<String,Object> mapaSumaCantidadProducto = FastMap.newInstance();
			String count = new String();
			String productId = new String();
			Long cantidad = new Long(0);
			String detalleSolicitudTransferId =  new String();
			for (Map.Entry<String, String> ProductEntry : mapaProductId.entrySet()) {
				count = ProductEntry.getKey();
				cantidad = Long.valueOf(mapaCantidad.get(count));
				productId = ProductEntry.getValue();
				detalleSolicitudTransferId = mapaDetalleId.get(count);
				if(UtilValidate.isEmpty(mapaBandera) || UtilValidate.isEmpty(mapaBandera.get(count)) || !mapaBandera.get(count).equalsIgnoreCase("Y")){
						Debug.logWarning("Se omite la transferencia del producto ["+productId+"] en la transferencia ["+solicitudTransferenciaId+"]", MODULE);
						cantidad = new Long(0);
						mapaSumaCantidadProducto.put(productId, BigDecimal.valueOf(cantidad));
				}

				guardaCantidadTransferida(mapaDetalleSolicitudId.get(detalleSolicitudTransferId), cantidad);
				
				UtilMisc.addToBigDecimalInMap(mapaSumaCantidadProducto, productId, BigDecimal.valueOf(cantidad));
			}
		
			List<GenericValue> listAlmacenDestino = FastList.newInstance();
			List<GenericValue> listSolicitudTransferInventario = FastList.newInstance();
			List<Map<String,Object>> listPhysicalInventoryItemVariance = FastList.newInstance();
			
			List<String> orderBy = UtilMisc.toList("productId","datetimeReceived");
			List<GenericValue> listAlmacenOrigen = delegator.findByCondition("InventoryItem", EntityCondition.makeCondition(condicionesOrigen) , null, orderBy);
			Long cantidadFaltante = new Long(0);
			Long cantidadDisponible = new Long(0);
			Long cantidadAlmacenDestino = new Long(0);
			String productIdAnterior = new String();
			if(UtilValidate.isNotEmpty(listAlmacenOrigen)){
				productIdAnterior = listAlmacenOrigen.get(0).getString("productId");
				cantidadFaltante = ((BigDecimal)mapaSumaCantidadProducto.get(productIdAnterior)).longValue();
			}
			for (GenericValue InventoryItemOrigen : listAlmacenOrigen) {
				productId = InventoryItemOrigen.getString("productId");
				cantidadDisponible = UtilNumber.getBigDecimal(InventoryItemOrigen.getBigDecimal("quantityOnHandTotal")).longValue();
				
				if(cantidadDisponible > 0){
					if(productId.equals(productIdAnterior)){
						if(cantidadFaltante == 0){//No hay mas transferencias de este producto que hacer
							continue;
						}
						cantidadFaltante = ActualizaInventoryItem(InventoryItemOrigen, cantidadFaltante, cantidadDisponible,
								cantidadAlmacenDestino,listAlmacenDestino,listSolicitudTransferInventario,almacenDestinoId,
								mapaProductoDetalleSolicitud,userLogin, dispatcher,listPhysicalInventoryItemVariance);
					} else {
						if(cantidadFaltante > 0){
							throw new GenericEntityException("No se cuenta con los productos suficientes para transferir el producto ["+productIdAnterior+"]");
						}
						cantidadFaltante = ((BigDecimal)mapaSumaCantidadProducto.get(productId)).longValue();
						cantidadFaltante = ActualizaInventoryItem(InventoryItemOrigen, cantidadFaltante, cantidadDisponible,
								cantidadAlmacenDestino,listAlmacenDestino,listSolicitudTransferInventario,almacenDestinoId,
								mapaProductoDetalleSolicitud,userLogin, dispatcher,listPhysicalInventoryItemVariance);
					}
				}
				
				productIdAnterior = InventoryItemOrigen.getString("productId");
			}
			
			if(cantidadFaltante > 0){
				throw new GenericEntityException("No se cuenta con los productos suficientes para transferir el producto ["+productIdAnterior+"]");
			}
			
			cambiaStatusSolicitudTransferencia(delegator, solicitudTransferenciaId,estatusTransferida);
			
			delegator.storeAll(listDetalleSolicitudTransfer);
			delegator.storeAll(listAlmacenOrigen);	
			delegator.storeAll(listAlmacenDestino);	
			delegator.storeAll(listSolicitudTransferInventario);	
			
			//Se crean los registros PhysicalInventory e ItemVariance
			/*for (Map<String,Object> mapaPhysical : listPhysicalInventoryItemVariance) {
				Map<String,Object> results = dispatcher.runSync("createPhysicalInventoryAndVariance", mapaPhysical);
				if(ServiceUtil.isError(results)){
					throw new GenericServiceException(ServiceUtil.getErrorMessage(results));
				}
			}*/
			
		} catch (GenericEntityException | GenericServiceException | NullPointerException e) {
			e.printStackTrace();
			regreso.putAll(ServiceUtil.returnError("No se pudo transferir la solicitud ["+solicitudTransferenciaId+"] : "+e.getMessage()));
			return regreso;
		}
		
		regreso.putAll(ServiceUtil.returnSuccess("Se realiz\u00f3 correctamente la transferencia"));
		return regreso;
		
	}
	
	/**
	 * Se guarda la cantidad transferida en la tabla DetalleSolicitudTransfer
	 * @param DetalleSolicitudTransfer
	 * @param cantidadTransferida
	 */
	private static void guardaCantidadTransferida(GenericValue DetalleSolicitudTransfer, Long cantidadTransferida){
		
		if(UtilValidate.isEmpty(cantidadTransferida)){
		DetalleSolicitudTransfer.set("cantidadTransferida", new Long(0));
		} else {
		DetalleSolicitudTransfer.set("cantidadTransferida", new Long(cantidadTransferida));
		}
		
	}
	
	

	/**
	 * Meotod que valida y actualiza las cantidades
	 * @param InventoryItemOrigen
	 * @param cantidadATransferir
	 * @param cantidadDisponible
	 * @param cantidadRestada
	 * @param cantidadTransferida
	 * @param cantidadSuficiente 
	 * @param cantidadSuficiente2 
	 * @param listAlmacenDestino 
	 * @param mapaProductoDetalleSolicitud 
	 * @param listPhysicalInventoryItemVariance 
	 * @throws GenericEntityException 
	 * @throws GenericServiceException 
	 */
	private static Long ActualizaInventoryItem(GenericValue InventoryItemOrigen,
			Long cantidadFaltante,Long cantidadDisponible,
			Long cantidadAlmacenDestino,
			List<GenericValue> listAlmacenDestino,
			List<GenericValue> listSolicitudTransferInventario,
			String almacenDestinoId, 
			Map<String, GenericValue> mapaProductoDetalleSolicitud
			,GenericValue userLogin,LocalDispatcher dispatcher, List<Map<String, Object>> listPhysicalInventoryItemVariance) throws GenericEntityException, GenericServiceException {
		
		Long nuevaCantidadFaltante = new Long(0);
		
		if(cantidadFaltante > cantidadDisponible){
			nuevaCantidadFaltante = cantidadFaltante - cantidadDisponible;
			cantidadAlmacenDestino = cantidadDisponible;
			cantidadDisponible = new Long(0);
		} else {
			cantidadAlmacenDestino = cantidadFaltante;
			cantidadDisponible -= cantidadFaltante;
			nuevaCantidadFaltante = cantidadFaltante - cantidadFaltante;
		}
		
		InventoryItemOrigen.set("quantityOnHandTotal", BigDecimal.valueOf(cantidadDisponible));
		InventoryItemOrigen.set("availableToPromiseTotal", BigDecimal.valueOf(cantidadDisponible));
		
		//Solo se registran transferencias que son mayores a 0
		if(cantidadAlmacenDestino > 0){
			GenericValue InventoryItemDestino = (GenericValue) InventoryItemOrigen.clone();
			InventoryItemDestino.set("facilityId", almacenDestinoId);
			InventoryItemDestino.set("quantityOnHandTotal", cantidadAlmacenDestino);
			InventoryItemDestino.set("availableToPromiseTotal", cantidadAlmacenDestino);
			InventoryItemDestino.set("datetimeReceived", UtilDateTime.nowTimestamp());
			InventoryItemDestino.remove("inventoryItemId");
			InventoryItemDestino.setNextSeqId();
			
			GenericValue inventoryItemDetail = InventoryItemOrigen.getDelegator().makeValue("InventoryItemDetail");
			
			inventoryItemDetail.set("inventoryItemId", InventoryItemOrigen.getString("inventoryItemId"));
			inventoryItemDetail.set("inventoryItemDetailSeqId", InventoryItemOrigen.getDelegator().getNextSeqId("InventoryItemDetail"));
			inventoryItemDetail.set("effectiveDate", UtilDateTime.nowTimestamp());
			inventoryItemDetail.set("quantityOnHandDiff", BigDecimal.valueOf(cantidadAlmacenDestino*(-1)));
			inventoryItemDetail.set("availableToPromiseDiff", BigDecimal.valueOf(cantidadAlmacenDestino*(-1)));
			inventoryItemDetail.set("reasonEnumId", "VAR_TRANSFER");
			inventoryItemDetail.set("fechaContable", UtilDateTime.nowTimestamp());
			listAlmacenDestino.add(inventoryItemDetail);
			
			GenericValue DetalleSolicitud = mapaProductoDetalleSolicitud.get(InventoryItemDestino.getString("productId"));
			if(UtilValidate.isEmpty(DetalleSolicitud)){
				throw new GenericEntityException("El producto ["+InventoryItemDestino.getString("productId")+"] no se encontr\u00f3 en la solicitud de transferencia");
			}
			
			listAlmacenDestino.add(InventoryItemDestino);
			
			GenericValue SolicitudTransferInventario = InventoryItemOrigen.getDelegator().makeValue("SolicitudTransferInventario");
			SolicitudTransferInventario.set("detalleSolicitudTransferId", DetalleSolicitud.getString("detalleSolicitudTransferId"));
			SolicitudTransferInventario.set("solicitudTransferenciaId", DetalleSolicitud.getString("solicitudTransferenciaId"));
			SolicitudTransferInventario.set("inventoryItemId", InventoryItemDestino.getString("inventoryItemId"));
			SolicitudTransferInventario.set("cantidadTransferida", cantidadAlmacenDestino);
			
			listSolicitudTransferInventario.add(SolicitudTransferInventario);
			
			//Se registra en el historico la baja del almacén origen 
			BigDecimal varQuantity = BigDecimal.valueOf(cantidadAlmacenDestino);
			Map<String,Object> mapaPhysicalInventoryAndVarianceResta = 
					UtilMisc.toMap("inventoryItemId",InventoryItemOrigen.getString("inventoryItemId"),"varianceReasonId","VAR_TRANSFER",
					"availableToPromiseVar", varQuantity.negate(),"quantityOnHandVar", varQuantity.negate(),
					"userLogin", userLogin);
			
			listPhysicalInventoryItemVariance.add(mapaPhysicalInventoryAndVarianceResta);
			
			Map<String,Object> mapaPhysicalInventoryAndVarianceSuma = FastMap.newInstance();
			mapaPhysicalInventoryAndVarianceSuma.putAll(mapaPhysicalInventoryAndVarianceResta);
			
			//Se registra en el historico el alta del almacén destino
			mapaPhysicalInventoryAndVarianceSuma.put("inventoryItemId",InventoryItemDestino.getString("inventoryItemId"));
			mapaPhysicalInventoryAndVarianceSuma.put("availableToPromiseVar", varQuantity);
			mapaPhysicalInventoryAndVarianceSuma.put("quantityOnHandVar", varQuantity);
			
			listPhysicalInventoryItemVariance.add(mapaPhysicalInventoryAndVarianceSuma);
		}
		
		return nuevaCantidadFaltante;
		
	}

	

	
	
}
