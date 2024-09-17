package com.absoluciones.gastosreserva;


import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.common.AB.UtilWorkflow;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.util.LineaMotorContable;
import org.ofbiz.order.finaccount.UtilClavePresupuestal;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

public class ServiciosGastosReserva {

	//Status Gastos a Reserva
	public final static String gastoReservaAprobada = "APROBADA_GR";
	public final static String gastoReservaCancelado = "CANCELADA_GR";
	public final static String gastoReservaComentado = "COMENTADA_GR";
	public final static String gastoReservaComprobado = "COMPROBADA_GR";
	public final static String gastoReservaDevolucion100 = "DEVOLUCION100_GR";
	public final static String gastoReservaCreado = "CREADA_GR";
	public final static String gastoReservaEnviado = "ENVIADA_GR";
	public final static String gastoReservaFinalizado = "FINALIZADA_GR";
	public final static String gastoReservaOtorgado = "OTORGADA_GR";
	public final static String gastoReservaRechazado = "RECHAZADA_GR";
	public final static String gastoReservaValidado = "VALIDADA_GR";
	public final static String module = ServiciosGastosReserva.class.getName();

	/**
	 * Crea Gasto a Reserva
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> guardarSolicitudGastosReserva(DispatchContext dctx,Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String partyId = userLogin.getString("partyId");
		String organizationPartyId = (String) context.get("organizationPartyId");

		try	{
			GenericValue Person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", partyId));
			String area = Person.getString("areaId");
			if(UtilValidate.isEmpty(area)){
				return ServiceUtil.returnError("&Aacute;rea sin asignar. Contacte al administrador");
			}
			GenericValue gastoReservaSol = GenericValue.create(delegator.getModelEntity("GastoReservaComp"));
			gastoReservaSol.setAllFields(context, false, null, null);
			gastoReservaSol.setNextSeqId();
			gastoReservaSol.set("areaId",area);
			gastoReservaSol.set("solicitanteId",partyId);
			gastoReservaSol.set("statusId", gastoReservaCreado);
			gastoReservaSol.set("organizationPartyId", organizationPartyId);
			delegator.create(gastoReservaSol);
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("gastosReservaId", gastoReservaSol.getString("gastosReservaId"));
			return result;
		}catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}


	/**
	 * Enviar Gasto a Reserva de comprobar
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> enviarSolicitud(DispatchContext dctx, Map<String, Object> context) {

		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Locale locale = (Locale) context.get("locale");
		String urlHost = (String) context.get("urlHost");
		String gastosReservaId = (String) context.get("gastosReservaId");

		try	{
			
			GenericValue gastoReserva = delegator.findByPrimaryKey("GastoReservaComp", UtilMisc.toMap("gastosReservaId", gastosReservaId));
			gastoReserva.setAllFields(context, false, null, null);
			gastoReserva.set("statusId", gastoReservaEnviado);

			String areaId = gastoReserva.getString("areaId");
			String organizationPartyId = gastoReserva.getString("organizationPartyId");
			String estatusWorkflowPendiente = "PENDIENTE";
			String estatusComentadaW = "COMENTADA_W";
			String CorreoAutorizacion = "CorreoMensajeAutorizar";
			String tipoWorkFlowId = "GASTORESERVA";

			Map<String, Object> result = ServiceUtil.returnSuccess();			

			//Valida si el gasto ya tiene un Workflow asociado
			EntityCondition condicionesWorkflowId = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("origenId", EntityOperator.EQUALS, gastosReservaId),
					EntityCondition.makeCondition("tipoWorkFlowId", EntityOperator.EQUALS, tipoWorkFlowId));		
			List<GenericValue> listWorkflowId = delegator.findByCondition("ControlWorkFlow",condicionesWorkflowId, null, null);
			String workFlowId = "";
			boolean esNuevoWorkflow = true;
			if(listWorkflowId.isEmpty()){	
				workFlowId = delegator.getNextSeqId("ControlWorkFlow");			
			
			}else{	
				Iterator<GenericValue> workflowIter = listWorkflowId.iterator();
				while (workflowIter.hasNext()){	
					GenericValue generic = workflowIter.next();
					workFlowId = generic.getString("workFlowId");
					esNuevoWorkflow = false;
				}
			}

			//Agrega registro en Control_Workflow
			UtilWorkflow.registroControlWorkflow(dctx, context, gastosReservaId, tipoWorkFlowId, workFlowId);				

			GenericValue autorizador = null;
			if(esNuevoWorkflow){
				
				//Se guarda el workFlowId en la tabla de Gastos a Reserva
				gastoReserva.set("workFlowId", workFlowId);
				//Actualiza el elemento de gastos reserva y cambia el estatus a Enviado
				delegator.store(gastoReserva);
				UtilWorkflow.registroEstatusWorkflow(dctx, context, areaId, organizationPartyId, workFlowId, estatusWorkflowPendiente, tipoWorkFlowId);
				autorizador = UtilWorkflow.obtenAutorizador(areaId, organizationPartyId, 1, delegator, tipoWorkFlowId);
			}else{
				
				List<GenericValue> statusWorkFlowList = UtilWorkflow.getStatusWorkFlow(workFlowId, estatusComentadaW, delegator);
				if(statusWorkFlowList.isEmpty()){	
					return ServiceUtil.returnError("No se han encontrado autorizadores asociados al Workflow");			
				}else{	
					
					Iterator<GenericValue> statusWorkFlowIter = statusWorkFlowList.iterator();
					while (statusWorkFlowIter.hasNext()) {	
						GenericValue statusWorkFlow = statusWorkFlowIter.next();								
						statusWorkFlow.set("statusId", estatusWorkflowPendiente);
						delegator.store(statusWorkFlow);
						delegator.store(gastoReserva);
						Long nivel = statusWorkFlow.getLong("nivelAutor");
						autorizador = UtilWorkflow.obtenAutorizador(areaId, organizationPartyId, nivel.intValue(), delegator, tipoWorkFlowId);
					}
				}
			}
			
			//Enviar correo de notificacion al autorizador
			String correoOrigen = gastoReserva.getString("solicitanteId");
			String correoDestino = autorizador.getString("autorizadorId");
			UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,tipoWorkFlowId,CorreoAutorizacion,
					urlHost,gastosReservaId,null,delegator,locale,dispatcher);
			result.put("gastosReservaId", gastoReserva.getString("gastosReservaId"));


			return result;
		}catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}

	}

	public static Map<String, Object> cancelarSolicitud(DispatchContext dctx, Map<String, Object> context) {

		Delegator delegator = dctx.getDelegator();
		String gastosReservaId = (String) context.get("gastosReservaId");

		try{
			GenericValue gastoReserva = delegator.findByPrimaryKey("GastoReservaComp", UtilMisc.toMap("gastosReservaId", gastosReservaId));
			gastoReserva.set("statusId", gastoReservaCancelado);

			delegator.store(gastoReserva);

			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("gastosReservaId", gastoReserva.getString("gastosReservaId"));

			return result;
		}catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		 }
	}
	
	/**
	 * Crea Detalle Gasto a Reserva (Facturas)
	 * @return
	 * @throws GenericEntityException 
	 */
	 
	public static Map<String, Object> agregaDetalleGastos(DispatchContext dctx,Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		String gastosReservaId = (String) context.get("gastosReservaId");
		String iva = (String) context.get("iva");
		String montoSubtotalFactura = (String) context.get("montoSubtotalFactura");
		String montoTotalFactura = (String) context.get("montoTotalFactura");
		String facturaNota = (String) context.get("facturaNota");
		BigDecimal montoTotalFact = BigDecimal.ZERO;
		montoTotalFact = montoTotalFact.add(BigDecimal.valueOf(Double.valueOf(montoTotalFactura)));
		
		try	{
		
			GenericValue GastoReserva = delegator.findByPrimaryKey("GastoReservaComp", UtilMisc.toMap("gastosReservaId", gastosReservaId));
			BigDecimal monto = GastoReserva.getBigDecimal("monto");
					
			//Validación de equilibrio del subtotal y el iva con el total.
			int montoValidarFactura =  BigDecimal.valueOf(Double.valueOf(montoTotalFactura)).compareTo(BigDecimal.valueOf(Double.valueOf(montoSubtotalFactura)).add(BigDecimal.valueOf(Double.valueOf(iva))));
			if(montoValidarFactura<0){
				return ServiceUtil.returnError("El Subtotal de la factura + IVA es mayor al Total");
			}
			if(montoValidarFactura>0){
				return ServiceUtil.returnError("El Total de la factura es mayor al Subtotal + IVA");
			}
			
		
		
			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND, EntityCondition.makeCondition("gastosReservaId", EntityOperator.EQUALS, gastosReservaId));
			List<GenericValue> listDetalleGasto = delegator.findByCondition("DetalleGasto",condiciones, null, UtilMisc.toList("detalleGId DESC"));
			
			//Validación de factura o nota existente en el sistema
			for (GenericValue detalleGastoComparar : listDetalleGasto) {
				montoTotalFact = montoTotalFact.add(BigDecimal.valueOf(Double.valueOf(detalleGastoComparar.getString("montoTotalFactura"))));
				if(facturaNota.equals(detalleGastoComparar.getString("facturaNota"))){
					return ServiceUtil.returnError("El número de factura o nota ya existe");
				}
			}
			
			//Validación monto total a comprobar excede el monto de lo solicitado
			int montoValidarMontFactura =  monto.compareTo(montoTotalFact);
			if(montoValidarMontFactura<0){
				return ServiceUtil.returnError("El monto comprobado es mayor al monto solicitado. No se puede agregar la factura");
			}
			
			long detalleId = 0L;
			
			if(!listDetalleGasto.isEmpty()){
				detalleId = Long.valueOf(EntityUtil.getFirst(listDetalleGasto).getString("detalleGId"));
			}
			
			String detalleGastoId = UtilFormatOut.formatPaddedNumber((detalleId + 1), 4);			
			GenericValue guardarDetalleGasto = GenericValue.create(delegator.getModelEntity("DetalleGasto"));
			guardarDetalleGasto.setAllFields(context, true, null, null);
			guardarDetalleGasto.set("detalleGId", detalleGastoId);
			guardarDetalleGasto.set("gastosReservaId", gastosReservaId);
			guardarDetalleGasto.set("iva", UtilValidate.isNotEmpty(iva) ? iva : "N");
			guardarDetalleGasto.set("statusId", gastoReservaComprobado);
			delegator.create(guardarDetalleGasto);
			
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("gastosReservaId", gastosReservaId);
			result.put("detalleGId", guardarDetalleGasto.getString("detalleGId"));
			
			return result;
		}catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		 }
	}
	
	/**
	 * Comprobar Solicitud (Facturas)
	 * @return
	 * @throws GenericEntityException 
	 */
	 
	public static Map<String, Object> comprobarSolicitudGastosReserva(DispatchContext dctx,Map<String, Object> context) throws GenericEntityException {
		Delegator delegator = dctx.getDelegator();
		String gastosReservaId = (String) context.get("gastosReservaId");
		EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND, EntityCondition.makeCondition("gastosReservaId", EntityOperator.EQUALS, gastosReservaId));
		List<GenericValue> listDetalleGasto = delegator.findByCondition("DetalleGasto", condiciones, null, UtilMisc.toList("detalleGId DESC"));
		GenericValue GastoReserva = delegator.findByPrimaryKey("GastoReservaComp", UtilMisc.toMap("gastosReservaId", gastosReservaId));
		
		if(listDetalleGasto.isEmpty()){
			return ServiceUtil.returnError("Para comprobar la solicitud es necesario agregar al menos una factura");
		}
		
		try	{
			GastoReserva.set("statusId", gastoReservaComprobado);
			GastoReserva.set("fechaComprobacion", UtilDateTime.nowSqlDate());
			delegator.store(GastoReserva);
			Map<String, Object> result = ServiceUtil.returnSuccess("Solicitud comprobada");
			result.put("gastosReservaId", gastosReservaId);
			return result;
		}catch (GenericEntityException | NullPointerException e) {
			return ServiceUtil.returnError(e.getMessage());
		 }
	}
	
	/**
	 * Metodo que agrega monto y clave presupuestal al gasto a reserva
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map<String, Object> agregaDetalleGastoPresupuesto(DispatchContext dctx,Map<String, Object> context) {
	
		Map<String, Object> result = ServiceUtil.returnSuccess();
		
		Delegator delegator = dctx.getDelegator();
		TimeZone timeZone = (TimeZone) context.get("timeZone");
		Locale locale = (Locale) context.get("locale");
		String gastosReservaId = (String) context.get("gastosReservaId");
		BigDecimal monto = (BigDecimal) context.get("monto");
		Date fecha = (Date) context.get("fecha");
		String ciclo = String.valueOf(UtilDateTime.getYear(UtilDateTime.toTimestamp(fecha), timeZone, locale));
		
		try {
		
			GenericValue GastoReserva = delegator.findByPrimaryKey("GastoReservaComp", UtilMisc.toMap("gastosReservaId", gastosReservaId));
			String organizationPartyId = GastoReserva.getString("organizationPartyId");
			
			GenericValue DetalleGastoSuma = EntityUtil.getFirst(delegator.findByAnd("DetalleGastoSuma", UtilMisc.toMap("gastosReservaId", gastosReservaId)));
			BigDecimal montoSumaFacturas = DetalleGastoSuma.getBigDecimal("montoTotalFactura"); 
			
			GenericValue DetalleGastoPresupuesto = delegator.makeValue("DetalleGastoPresupuesto");
			
			//Se obtienen los componentes de la clave presupuestal, se valida que existe la clave y que tiene presupuesto 
			String clavePresupuestal = UtilClavePresupuestal.almacenaClavePresupuestal(context, DetalleGastoPresupuesto, delegator, UtilClavePresupuestal.EGRESO_TAG, organizationPartyId, false, ciclo);
			UtilClavePresupuestal.validaSuficienciaExistenciaClave(clavePresupuestal, monto, "DISPONIBLE", delegator, UtilDateTime.toTimestamp(fecha), UtilClavePresupuestal.EGRESO_TAG);
			
			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND, EntityCondition.makeCondition("gastosReservaId", EntityOperator.EQUALS, gastosReservaId));
			List<GenericValue> listDetalleGasto = delegator.findByCondition("DetalleGastoPresupuesto",condiciones, null, UtilMisc.toList("detallePresId DESC"));
			BigDecimal sumaMontoPresupuesto = monto;
			for (GenericValue DetGastoPresupuesto : listDetalleGasto) {
				sumaMontoPresupuesto = sumaMontoPresupuesto.add(DetGastoPresupuesto.getBigDecimal("monto"));
				if(clavePresupuestal.equals(DetGastoPresupuesto.getString("clavePresupuestal"))){
					throw new GenericEntityException("La clave presupuestal ya fue ingresada");
				}
			}
			
			Integer detallePresIdSiguiente = 1;
			GenericValue DetGastoPresupuestoContador = EntityUtil.getFirst(listDetalleGasto);
			if(UtilValidate.isNotEmpty(DetGastoPresupuestoContador)){
				String detallePresId = DetGastoPresupuestoContador.getString("detallePresId");
				detallePresIdSiguiente = Integer.valueOf(detallePresId)+1;
			}
			
			if(sumaMontoPresupuesto.compareTo(montoSumaFacturas) > 0 ){
				throw new GenericEntityException("El monto a comprobar es superior al solicitado. No se puede agregar la clave");
			}
			
			DetalleGastoPresupuesto.setAllFields(context, false, null, null);
			DetalleGastoPresupuesto.put("clavePresupuestal", clavePresupuestal);
			DetalleGastoPresupuesto.put("detallePresId", UtilFormatOut.formatPaddedNumber(detallePresIdSiguiente, 4));
			DetalleGastoPresupuesto.create();
			
		} catch(GenericEntityException | NullPointerException e){
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		
		result.put("gastosReservaId",gastosReservaId);
		
		return result;
	}
	
	/**
	 * Genera el impacto contable del presupuesto para terminar con la comprobacion del gasto , si es necesario envia 
	 * un pendiente a tesoreria para liberar gasto sobrante
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> presupuestarSolicitudGastosReserva(DispatchContext dctx,Map<String, Object> context) {
	
		Map<String, Object> result = ServiceUtil.returnSuccess();
		
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Delegator delegator = dctx.getDelegator();
		String gastosReservaId = (String) context.get("gastosReservaId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		
		try {
			
			GenericValue GastoReserva = delegator.findByPrimaryKey("GastoReservaComp", UtilMisc.toMap("gastosReservaId", gastosReservaId));
			BigDecimal montoGasto = GastoReserva.getBigDecimal("monto");
			boolean devolucion = false;
			
			//Validar que el monto de facturas y el de claves sea igual 
			GenericValue DetalleGastoSuma = EntityUtil.getFirst(delegator.findByAnd("DetalleGastoSuma", UtilMisc.toMap("gastosReservaId", gastosReservaId)));
			BigDecimal montoSumaFacturas = DetalleGastoSuma.getBigDecimal("montoTotalFactura"); 
			
			GenericValue DetalleGastoPresupSuma = EntityUtil.getFirst(delegator.findByAnd("DetalleGastoPresupSuma", UtilMisc.toMap("gastosReservaId", gastosReservaId)));
			BigDecimal montoSumaPresup = DetalleGastoPresupSuma.getBigDecimal("monto"); 
			
			if(montoSumaFacturas.compareTo(montoSumaPresup) != 0){
				throw new GenericEntityException("La suma de las facturas y de claves presupuestarias debe ser igual para realizar la comprobaci\u00f3n");
			} 
			
			if(montoSumaFacturas.compareTo(montoGasto) < 0){
				devolucion = true;
			}
			
			//Buscamos el evento predefinido para la comprobacion 
			EntityCondition condicionEvento = EntityCondition.makeCondition("descripcion",EntityOperator.LIKE,"Comprobaci%n de gastos a reserva de comprobar");
			GenericValue EventoComprobacion = EntityUtil.getFirst(delegator.findByCondition("EventoContable", condicionEvento , null, null));
			
			if(UtilValidate.isEmpty(EventoComprobacion)){
				throw new GenericEntityException("No se encuentra el evento contable necesario para realizar esta acci\u00f3n");
			}
			
			String acctgTransTypeId = EventoComprobacion.getString("acctgTransTypeId");
			
			List<GenericValue> listLineaPresupuestal = delegator.findByAnd("LineaPresupuestal", UtilMisc.toMap("acctgTransTypeId",acctgTransTypeId));
			
			//Se crea el mapa de Lineas Presupuestales, el motor contable lo requiere , sin embargo en este proceso no se utiliza
			Map<String,GenericValue> mapaSecuenciaPresupuestal = FastMap.newInstance();
			String catalogoAbono = new String();
			for (GenericValue LineaPresupuestal : listLineaPresupuestal) {
				mapaSecuenciaPresupuestal.put(LineaPresupuestal.getString("secuencia"), LineaPresupuestal);
				//Ingresamos el auxiliar de abono para disminuir o liquidar la deuda del solicitante
				catalogoAbono = LineaPresupuestal.getString("catalogoAbono");
				if(UtilValidate.isNotEmpty(catalogoAbono)){
					LineaPresupuestal.set("catalogoAbono", GastoReserva.getString("auxiliarId"));
				}
			}
			
			
			List<LineaMotorContable> listLineaMotorContable = FastList.newInstance();
			
			List<GenericValue> listDetalleGastoPresupuesto = delegator.findByAnd("DetalleGastoPresupuesto", UtilMisc.toMap("gastosReservaId", gastosReservaId));
			for (GenericValue DetalleGastoPresupuesto : listDetalleGastoPresupuesto) {
				LineaMotorContable LineaMotorContable = new LineaMotorContable();
				LineaMotorContable.setClavePresupuestal(DetalleGastoPresupuesto.getString("clavePresupuestal"));
				LineaMotorContable.setMontoPresupuestal(DetalleGastoPresupuesto.getBigDecimal("monto"));
				LineaMotorContable.setFecha(UtilDateTime.toTimestamp(DetalleGastoPresupuesto.getDate("fecha")));
				LineaMotorContable.setLineasPresupuestales(mapaSecuenciaPresupuestal);
				
				listLineaMotorContable.add(LineaMotorContable);
			}
			
			Map<String, Object> input = FastMap.newInstance();
			input.put("fechaRegistro", UtilDateTime.nowTimestamp());
			input.put("fechaContable", UtilDateTime.toTimestamp(GastoReserva.getDate("fecha")));
			input.put("eventoContableId", acctgTransTypeId);
			input.put("usuario", userLogin.getString("userLoginId"));
			input.put("organizationId", GastoReserva.getString("organizationPartyId"));
			input.put("currency", GastoReserva.getString("tipoMoneda"));
			input.put("tipoClaveId", UtilClavePresupuestal.EGRESO_TAG);
			input.put("lineasMotor", listLineaMotorContable);
			input.put("descripcion", GastoReserva.getString("concepto") + " - Comprobacion");
			input.put("campo", "gastosReservaId");
			input.put("valorCampo", gastosReservaId);
			
			Map<String,Object> resultPoliza = dispatcher.runSync("creaTransaccionMotor",input, 43200, true);
			
			if (ServiceUtil.isError(resultPoliza)) {
				Debug.logError("Hubo Error al crear la poliza "+ServiceUtil.getErrorMessage(resultPoliza),module);
				return ServiceUtil.returnError(ServiceUtil.getErrorMessage(resultPoliza));
			} else {
				GenericValue transaccion = (GenericValue) resultPoliza.get("transaccion");
				String acctgTransId = transaccion.getString("acctgTransId");
				GastoReserva.put("acctgTransIdComprueba", acctgTransId);
				result.putAll(ServiceUtil.returnSuccess("La solicitud ha sido comprobada con el n\u00famero de p\u00f3liza "+
								transaccion.getString("poliza")+", se ha enviado un aviso al tesorero"));
			}
			
			if(devolucion){
				registraPendienteTesoreriaDevolucion(GastoReserva, montoGasto.subtract(montoSumaFacturas), delegator);
				GastoReserva.put("statusId", "VALIDADA_GR");
			} else {
				GastoReserva.put("statusId", "FINALIZADA_GR");
			}
			
			GastoReserva.store();
			
		} catch(GenericEntityException | NullPointerException | GenericServiceException e){
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		
		result.put("gastosReservaId",gastosReservaId);
		
		return result;
	}
	
	/**
	 * Genera unpendiente de tesoreria por concepto de devolucion
	 * @param monto
	 * @param entidad
	 * @param delegator
	 * @param userLoginpartyId
	 * @throws GenericEntityException
	 */
	public static void registraPendienteTesoreriaDevolucion(GenericValue entidad, BigDecimal monto, Delegator delegator) throws GenericEntityException{
		
		GenericValue pendientesTesoreria = GenericValue.create(delegator.getModelEntity("PendientesTesoreria"));
		pendientesTesoreria.set("idPendienteTesoreria", entidad.getString(entidad.getModelEntity().getFirstPkFieldName()));
		pendientesTesoreria.set("tipo", "DEVO_GASTOS");
		pendientesTesoreria.set("fechaTransaccion", UtilDateTime.nowTimestamp());
		pendientesTesoreria.set("fechaContable", UtilDateTime.toTimestamp(entidad.getDate("fecha")));
		pendientesTesoreria.set("descripcion", entidad.getString("concepto"));
		pendientesTesoreria.set("moneda", entidad.getString("tipoMoneda"));
		pendientesTesoreria.set("estatus", "VALIDADA_GR");
		pendientesTesoreria.set("monto", monto);
		pendientesTesoreria.set("conceptoPago", entidad.getString("concepto"));
		pendientesTesoreria.set("auxiliar", entidad.getString("auxiliarId"));
		delegator.create(pendientesTesoreria);
		
	}
	
	/**
	 * Metodo para la devolucion total del gasto
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String,Object> devolucion100GastoReserva(DispatchContext dctx,Map<String, Object> context){
		
		Map<String, Object> result = ServiceUtil.returnSuccess();
		
		Delegator delegator = dctx.getDelegator();
		String gastosReservaId = (String) context.get("gastosReservaId");
		try {
			
			GenericValue GastoReserva = delegator.findByPrimaryKey("GastoReservaComp", UtilMisc.toMap("gastosReservaId", gastosReservaId));
			BigDecimal monto = GastoReserva.getBigDecimal("monto");
			registraPendienteTesoreriaDevolucion(GastoReserva, monto, delegator);
			GastoReserva.set("statusId", gastoReservaDevolucion100);
			GastoReserva.store();
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		
		result.putAll(ServiceUtil.returnSuccess("La devoluci\u00f3n ha sido realizada , se ha enviado un aviso al tesorero"));
		result.put("gastosReservaId",gastosReservaId);
		
		
		return result;
	}
}
