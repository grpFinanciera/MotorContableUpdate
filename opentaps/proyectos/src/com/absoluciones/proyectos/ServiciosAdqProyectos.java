package com.absoluciones.proyectos;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import javolution.util.FastMap;

public class ServiciosAdqProyectos {
	
private static String MODULE = ServiciosAdqProyectos.class.getName();
	
	/**
	 * Crea Proyecto
	 * @param dctx
	 * @param context
	 * @return
	 */
	
	public static Map<String, Object> registrarAdquisicion(DispatchContext dctx,Map<?, ?> context) {
		
		Delegator delegator = dctx.getDelegator();		
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String partyId = userLogin.getString("partyId");
		String organizationPartyId = (String) context.get("organizationPartyId");
		String proyectoId = (String) context.get("proyectoId");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		String tipoMoneda = (String) context.get("tipoMoneda");
        String descripcion = (String) context.get("descripcion");
        String justificacion = (String) context.get("justificacion");
        String supplierPartyId = (String) context.get("supplierPartyId");
    	String eventoAdq = (String) context.get("eventoAdq");
    	
    	String gastoProyecto = (String) context.get("gastoProyecto");
    	
    	try{
    		GenericValue proyecto = delegator.findByPrimaryKey("Proyecto", UtilMisc.toMap("proyectoId", proyectoId));
			GenericValue adquisicion = GenericValue.create(delegator.getModelEntity("AdquisicionesProyecto"));
			String auxiliar = proyecto.getString("auxiliarProyecto");
			adquisicion.setAllFields(context, false, null, null);
			adquisicion.setNextSeqId();
			adquisicion.set("fechaContable", fechaContable);
			adquisicion.set("descripcion", descripcion);
			adquisicion.set("tipoMoneda", tipoMoneda);
			adquisicion.set("justificacion", justificacion);
			adquisicion.set("supplierPartyId", supplierPartyId);
			adquisicion.set("statusId", "SOLICITADO");
			adquisicion.set("eventoAdq", eventoAdq);
			adquisicion.set("tipoMoneda", tipoMoneda);
			adquisicion.set("montoTotal", BigDecimal.ZERO);
			adquisicion.set("auxiliar", auxiliar);
			adquisicion.set("gastoProyId", gastoProyecto);
			delegator.create(adquisicion);
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("adqProyectoId", adquisicion.getString("adqProyectoId"));
			return result; 
		
    	}catch (GenericEntityException e){
    		return ServiceUtil.returnError(e.getMessage());
    	}
	}
	
	public static Map<String, Object> agregaProductoAdq(DispatchContext dctx,
			Map<String, Object> context){

		Delegator delegator = dctx.getDelegator();
        String adqProyectoId = (String) context.get("adqProyectoId");
        BigDecimal monto = (BigDecimal) context.get("monto");
        BigDecimal cantidad = (BigDecimal) context.get("cantidad");
        String detalleParticipanteId = (String) context.get("detalleParticipanteId");
		
		try {
			
			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
										  EntityCondition.makeCondition("adqProyectoId", EntityOperator.EQUALS, adqProyectoId));
			List<GenericValue> listProducto = delegator.findByCondition("ProductosAdqProyecto",condiciones, null, UtilMisc.toList("detalleProductoId DESC"));
			GenericValue adquisicion = delegator.findByPrimaryKey("AdquisicionesProyecto", UtilMisc.toMap("adqProyectoId", adqProyectoId));
			
			BigDecimal montoAdq = adquisicion.getBigDecimal("montoTotal").add(monto.multiply(cantidad));
			
			adquisicion.set("montoTotal", montoAdq);
			long detalleId = 0;
			
			if(!listProducto.isEmpty()){
				detalleId = Long.valueOf(EntityUtil.getFirst(listProducto).getString("detalleProductoId"));
			}
			
			String detalleProductoId = UtilFormatOut.formatPaddedNumber((detalleId + 1), 4);
			
			GenericValue producto = GenericValue.create(delegator.getModelEntity("ProductosAdqProyecto"));
			producto.setAllFields(context, true, null, null);
			producto.set("detalleProductoId", detalleProductoId);
			producto.set("detalleParticipanteId", detalleParticipanteId);
			
			//Se agrega a la lista la nueva requisicion para validar la suficiencia presupuestal
			listProducto.add(producto);
			delegator.store(adquisicion);
			delegator.create(producto);
			
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("adqProyectoId", adqProyectoId);
			
			return result;
			
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
		
	}
	
	/**
	 * Metodo para recargar la pagina
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> cargarPaginaAdqProyecto(DispatchContext dctx,
			Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		
		String adqProyectoId = (String) context.get("adqProyectoId");		

		GenericValue adqProyecto = GenericValue.create(delegator.getModelEntity("AdquisicionesProyecto"));
		if(adqProyectoId != null && !adqProyectoId.isEmpty())
			adqProyecto.set("adqProyectoId", adqProyectoId);
		
		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("adqProyectoId", adqProyecto.getString("adqProyectoId"));
		return result;
	}
	
	public static Map<String, Object> guardarAdquisicion(DispatchContext dctx,Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String adqProyectoId = (String) context.get("adqProyectoId");
		Map<String, Object> input = FastMap.newInstance();
	    Map<String, Object> output = FastMap.newInstance();
	    Map<String, Object> montoMap = FastMap.newInstance();
	    Map<String, Object> clavePresupuestalMap = FastMap.newInstance();
	    Map<String, Object> catalogoCargoContMap = FastMap.newInstance();
	    Map<String, Object> catalogoAbonoContMap = FastMap.newInstance();
	    Map<String, Object> context2 = FastMap.newInstance();
	    String poliza = null;
		

		try	{
			
			GenericValue adquisicion = delegator.findByPrimaryKey("AdquisicionesProyecto", UtilMisc.toMap("adqProyectoId", adqProyectoId));
			String auxiliar = adquisicion.getString("auxiliar");
			String proveedor = adquisicion.getString("supplierPartyId");
			String cuentaBancariaId = null;
			List<String> orderByLinea = UtilMisc.toList("secuencia");
	    	EntityCondition condicionLinea = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, adquisicion.getString("eventoAdq")));
			
			List<GenericValue> lineaList = delegator.findByCondition("LineaContable", condicionLinea, null, orderByLinea);
			Debug.logInfo("condicion: " + condicionLinea,MODULE);
			Debug.logInfo("lineaList: " + lineaList,MODULE);
			if(!lineaList.isEmpty())
			{	Iterator<GenericValue> listaIter = lineaList.iterator();
			while (listaIter.hasNext())
				{	GenericValue item = listaIter.next();
					String descripcionLinea = item.getString("descripcion");
					descripcionLinea = descripcionLinea.replaceAll(" ", "");
					catalogoCargoContMap
							.put(descripcionLinea + "0",
									item.getString("catalogoCargo") != null
											&& item.getString("catalogoCargo")
													.equalsIgnoreCase("BANCO") ? cuentaBancariaId
											: auxiliar);
					catalogoAbonoContMap
							.put(descripcionLinea + "0",
									item.getString("catalogoAbono") != null
											&& item.getString("catalogoAbono")
													.equalsIgnoreCase("BANCO") ? cuentaBancariaId
											: proveedor);
		        	montoMap.put(descripcionLinea+"0", adquisicion.getBigDecimal("montoTotal").toString());
				}
			}        					
        	clavePresupuestalMap.put("0", "");	        	
        		        	
            context2.put("montoMap", montoMap);        
            context2.put("clavePresupuestalMap", clavePresupuestalMap);
            context2.put("catalogoCargoContMap", catalogoCargoContMap);
            context2.put("catalogoAbonoContMap", catalogoAbonoContMap);
            
            Debug.log("montoMap: " + montoMap);
            Debug.log("clavePresupuestalMap: " + clavePresupuestalMap);
            Debug.log("catalogoCargoContMap: " + catalogoCargoContMap);
            Debug.log("catalogoAbonoContMap: " + catalogoAbonoContMap);
			
			//Invocacion a motor contable
        	/*****MOTOR CONTABLE*****/
            Timestamp fechaContable = adquisicion.getTimestamp("fechaContable");
            
            input.put("eventoContableId", adquisicion.getString("eventoAdq"));
        	input.put("tipoClaveId", "EGRESO");
        	input.put("fechaRegistro", UtilDateTime.nowTimestamp());
        	input.put("fechaContable", fechaContable);
        	input.put("currency", adquisicion.getString("tipoMoneda"));
        	input.put("usuario", userLogin.getString("userLoginId"));
        	input.put("organizationId", "1");
        	input.put("descripcion", "Adquisicion de "+ adquisicion.getString("descripcion"));
        	input.put("roleTypeId", "BILL_FROM_VENDOR");
        	input.put("valorCampo", adquisicion.getString("proyectoId"));
        	input.put("campo", "proyectoId");      
        	input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(adquisicion.getString("eventoAdq"), delegator, context2, null, null, null, null));
        	
        	
        	//Genera registro en DepreciacionResumen
        	output = dispatcher.runSync("creaTransaccionMotor", input);
        	Debug.log("output: " + output);
        	if(ServiceUtil.isError(output)){
        		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
        	}
        	
        	GenericValue transaccion = (GenericValue) output.get("transaccion");
        	poliza = transaccion.getString("poliza");
        	
        	adquisicion.set("statusId", "VIGENTE");
        	adquisicion.set("acctgTransIdSolicitud", transaccion.getString("acctgTransId"));
        	adquisicion.store();

			Map<String, Object> result = ServiceUtil.returnSuccess("La adquisicion se ha creado con \u00e9xito con la poliza " +poliza);		

			result.put("adqProyectoId", adqProyectoId);
			return result;
		}catch (GenericEntityException | GenericServiceException e) 
			{	return ServiceUtil.returnError("Error al crear la adquisicion: " + e.getMessage());
			}
	}
	
	/**
	 * Cancela adquisicion de proyecto
	 * @param dctx
	 * @param context
	 * @return
	 */
	
	public static Map<String, Object> cancelarAdqProyecto(DispatchContext dctx,Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		
		String adqProyectoId = (String) context.get("adqProyectoId");
		Map<String, Object> result = ServiceUtil.returnSuccess();

		try	{
			
			GenericValue nuevaAdq = delegator.findByPrimaryKey("AdquisicionesProyecto", UtilMisc.toMap("adqProyectoId", adqProyectoId));
			
			String polizaCreacion = nuevaAdq.getString("acctgTransIdSolicitud");
			
			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
					  EntityCondition.makeCondition("acctgTransId", EntityOperator.EQUALS, polizaCreacion),
					  EntityCondition.makeCondition("acctgTransEntrySeqId",EntityOperator.LIKE,"%R"));
			
			List<GenericValue> detallesPoliza = delegator.findByCondition("AcctgTransEntry", condiciones, null, null);
			
			if(UtilValidate.isEmpty(detallesPoliza)){
				result = ServiceUtil.returnError("No se puede cancelar la adquisicion ya que aun no se revierte la poliza de solicitud");
			}else{
				if(nuevaAdq.getString("statusId").equals("PAGADO")){
					result = ServiceUtil.returnError("No se puede cancelar la adquisicion ya que se encuentra pagada");
				}else{
					nuevaAdq.set("statusId","CANCELADO");				
					delegator.store(nuevaAdq);
				}
				
			}			
						
			result.put("adqProyectoId", nuevaAdq.getString("adqProyectoId"));
			return result;
		}catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}
	
	public static Map<String, Object> pagoAdquisicion(DispatchContext dctx,Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String adqProyectoId = (String) context.get("adqProyectoId");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		String eventoPago = (String) context.get("eventoPago");
		String paymentTypeId = (String) context.get("paymentTypeId");
		String cuentaBancariaId = (String) context.get("cuentaOrigen");
		Map<String, Object> input = FastMap.newInstance();
	    Map<String, Object> output = FastMap.newInstance();
	    Map<String, Object> montoMap = FastMap.newInstance();
	    Map<String, Object> clavePresupuestalMap = FastMap.newInstance();
	    Map<String, Object> catalogoCargoContMap = FastMap.newInstance();
	    Map<String, Object> catalogoAbonoContMap = FastMap.newInstance();
	    Map<String, Object> context2 = FastMap.newInstance();
	    String poliza = null;
		

		try	{
			
			GenericValue adquisicion = delegator.findByPrimaryKey("AdquisicionesProyecto", UtilMisc.toMap("adqProyectoId", adqProyectoId));
			String auxiliar = adquisicion.getString("supplierPartyId");
			GenericValue proyecto = delegator.findByPrimaryKey("Proyecto", UtilMisc.toMap("proyectoId", adquisicion.getString("proyectoId")));
			
			List<String> orderByLinea = UtilMisc.toList("secuencia");
	    	EntityCondition condicionLinea = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, eventoPago));
			
			List<GenericValue> lineaList = delegator.findByCondition("LineaContable", condicionLinea, null, orderByLinea);
			Debug.logInfo("condicion: " + condicionLinea,MODULE);
			Debug.logInfo("lineaList: " + lineaList,MODULE);
			if(!lineaList.isEmpty())
			{	Iterator<GenericValue> listaIter = lineaList.iterator();	        
			while (listaIter.hasNext())
			{	GenericValue item = listaIter.next();
				String descripcionLinea = item.getString("descripcion");
				descripcionLinea = descripcionLinea.replaceAll(" ", "");
				catalogoCargoContMap
						.put(descripcionLinea + "0",
								item.getString("catalogoCargo") != null
										&& item.getString("catalogoCargo")
												.equalsIgnoreCase("BANCO") ? cuentaBancariaId
										: auxiliar);
				catalogoAbonoContMap
						.put(descripcionLinea + "0",
								item.getString("catalogoAbono") != null
										&& item.getString("catalogoAbono")
												.equalsIgnoreCase("BANCO") ? cuentaBancariaId
										: auxiliar);
	        	montoMap.put(descripcionLinea+"0", adquisicion.getBigDecimal("montoTotal").toString());
			}
			} 
			
			BigDecimal montoRestanteP = proyecto.getBigDecimal("montoRestante");
        	BigDecimal montoRestantePF = montoRestanteP.subtract(adquisicion.getBigDecimal("montoTotal"));
        	
        	if(proyecto.getString("validacion").equals("Y")){
				if (montoRestantePF.compareTo(BigDecimal.ZERO) <= 0) {
					return ServiceUtil.returnError("No cuenta con suficiencia en el proyecto para esta operacion");
				}
			}
			
        	clavePresupuestalMap.put("0", "");	        	
        		        	
            context2.put("montoMap", montoMap);        
            context2.put("clavePresupuestalMap", clavePresupuestalMap);
            context2.put("catalogoCargoContMap", catalogoCargoContMap);
            context2.put("catalogoAbonoContMap", catalogoAbonoContMap);
            
            Debug.log("montoMap: " + montoMap);
            Debug.log("clavePresupuestalMap: " + clavePresupuestalMap);
            Debug.log("catalogoCargoContMap: " + catalogoCargoContMap);
            Debug.log("catalogoAbonoContMap: " + catalogoAbonoContMap);
			
			//Invocacion a motor contable
        	/*****MOTOR CONTABLE*****/
            
            input.put("eventoContableId", eventoPago);
        	input.put("tipoClaveId", "EGRESO");
        	input.put("fechaRegistro", UtilDateTime.nowTimestamp());
        	input.put("fechaContable", fechaContable);
        	input.put("currency", adquisicion.getString("tipoMoneda"));
        	input.put("usuario", userLogin.getString("userLoginId"));
        	input.put("organizationId", "1");
        	input.put("descripcion", "Pago de "+ adquisicion.getString("descripcion"));
        	input.put("roleTypeId", "BILL_FROM_VENDOR");
        	input.put("valorCampo", adquisicion.getString("proyectoId"));
        	input.put("campo", "proyectoId");      
        	input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(eventoPago, delegator, context2, null, null, null, null));
        	
        	
        	//Genera registro en DepreciacionResumen
        	output = dispatcher.runSync("creaTransaccionMotor", input);
        	Debug.log("output: " + output);
        	if(ServiceUtil.isError(output)){
        		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
        	}
        	
        	GenericValue transaccion = (GenericValue) output.get("transaccion");
        	poliza = transaccion.getString("poliza");
        	
        	adquisicion.set("statusId", "PAGADO");
        	adquisicion.set("fechaContablePago", fechaContable);
        	adquisicion.set("paymentTypeId", paymentTypeId);
        	adquisicion.set("eventoAdqPago", eventoPago);
        	adquisicion.set("acctgTransIdPago", transaccion.getString("acctgTransId"));
        	proyecto.set("montoRestante", montoRestantePF);
        	
        	proyecto.store();
        	adquisicion.store();

			Map<String, Object> result = ServiceUtil.returnSuccess("Se ha realizado el pago con \u00e9xito y se ha creado la poliza " +poliza);		

			result.put("adqProyectoId", adqProyectoId);
			return result;
		}catch (GenericEntityException | GenericServiceException e) 
			{	return ServiceUtil.returnError("Error al crear la adquisicion: " + e.getMessage());
			}
	}

}
