package com.absoluciones.proyectos;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import javolution.util.FastMap;

public class ServiciosServPersonales {
	
private static String MODULE = ServiciosServPersonales.class.getName();
	
	/**
	 * Crea Servicios Personales
	 * @param dctx
	 * @param context
	 * @return
	 */
	
	public static Map<String, Object> registrarServiciosPersonales(DispatchContext dctx,Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Map<String, Object> input = FastMap.newInstance();
	    Map<String, Object> output = FastMap.newInstance();
	    Map<String, Object> montoMap = FastMap.newInstance();
	    Map<String, Object> clavePresupuestalMap = FastMap.newInstance();
	    Map<String, Object> catalogoCargoContMap = FastMap.newInstance();
	    Map<String, Object> catalogoAbonoContMap = FastMap.newInstance();
	    Map<String, Object> context2 = FastMap.newInstance();
	    String poliza = null;
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String organizationPartyId = (String) context.get("organizationPartyId");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		String tipoMoneda = (String) context.get("tipoMoneda");
		String tipoContratacion = (String) context.get("tipoContratacion");
		BigDecimal sueldo = (BigDecimal) context.get("sueldo");
		BigDecimal iva = (BigDecimal) context.get("iva");
		BigDecimal ivaR = (BigDecimal) context.get("ivaR");
		BigDecimal isr = (BigDecimal) context.get("isr");
		BigDecimal montoTotal = (BigDecimal) context.get("montoTotal");
		String observaciones = (String) context.get("observaciones");
		String eventoServiciosPersonales = (String) context.get("eventoServiciosPersonales");
    	String proyectoId = (String) context.get("proyectoId");   
    	String detalleParticipanteId = (String) context.get("detalleParticipanteId");
    	String cuentaBancariaId = (String)context.get("cuentaOrigen");
		String auxiliar = (String)context.get("auxiliar");
		String gastoProyecto = (String) context.get("gastoProyecto");
		String conceptoPoliza = (String) context.get("conceptoPoliza");
		BigDecimal montoTotalIva = sueldo.add(iva);

		try	{
			
			GenericValue serviciosPer = GenericValue.create(delegator.getModelEntity("ServiciosPersonalesProy"));
			GenericValue proyecto = delegator.findByPrimaryKey("Proyecto", UtilMisc.toMap("proyectoId", proyectoId));
			String auxiliarCancelaPasivo = proyecto.getString("auxiliarProyecto");
			serviciosPer.setAllFields(context, false, null, null);
			serviciosPer.setNextSeqId();
			serviciosPer.set("fechaContable", fechaContable);
			serviciosPer.set("tipoContratacion", tipoContratacion);
			serviciosPer.set("tipoMoneda", tipoMoneda);
			serviciosPer.set("observaciones", observaciones);
			serviciosPer.set("sueldo", sueldo);
			serviciosPer.set("iva", iva);
			serviciosPer.set("ivaR", ivaR);
			serviciosPer.set("isr", isr);
			serviciosPer.set("montoTotal", montoTotal);
			if(tipoContratacion.equals("Sueldos complementarios")){
				serviciosPer.set("statusId", "FINALIZADO");
			}else{
				serviciosPer.set("statusId", "SOLICITADO");
			}
			
			serviciosPer.set("eventoServiciosPersonales", eventoServiciosPersonales);
			serviciosPer.set("auxiliar", auxiliar);
			serviciosPer.set("gastoProyId", gastoProyecto);
			serviciosPer.set("conceptoPoliza", conceptoPoliza);
			
			GenericValue participante = delegator.findByPrimaryKey("ParticipantesProyecto", UtilMisc.toMap("proyectoId", proyectoId, "detalleParticipanteId", detalleParticipanteId));
			
			EntityCondition condicionLinea = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, eventoServiciosPersonales));
			List<String> orderByLinea = UtilMisc.toList("secuencia");
			List<GenericValue> lineaList = delegator.findByCondition("LineaContable", condicionLinea, null, orderByLinea);
			Debug.logInfo("condicion: " + condicionLinea,MODULE);
			Debug.logInfo("lineaList: " + lineaList,MODULE);
			int i = lineaList.size();
			if(i>1){
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
						
						if(i==1 && UtilValidate.isNotEmpty(montoTotalIva)){
							montoMap.put(descripcionLinea+"0", montoTotalIva.toString());
						}else if(i==2 && UtilValidate.isNotEmpty(ivaR)){
							montoMap.put(descripcionLinea+"0", ivaR.toString());
						}else if(i==3 && UtilValidate.isNotEmpty(isr)){
							montoMap.put(descripcionLinea+"0", isr.toString());
						}else{
							
						}
						i--;
					}
				}
			}else{	Iterator<GenericValue> listaIter = lineaList.iterator();	        
			   
			while (listaIter.hasNext())
			{	GenericValue item = listaIter.next();
				String descripcionLinea = item.getString("descripcion");
				descripcionLinea = descripcionLinea.replaceAll(" ", "");
				catalogoCargoContMap
						.put(descripcionLinea + "0",
								item.getString("catalogoCargo") != null
										&& item.getString("catalogoCargo")
												.equalsIgnoreCase("BANCO") ? cuentaBancariaId
										: auxiliarCancelaPasivo);
				catalogoAbonoContMap
						.put(descripcionLinea + "0",
								item.getString("catalogoAbono") != null
										&& item.getString("catalogoAbono")
												.equalsIgnoreCase("BANCO") ? cuentaBancariaId
										: auxiliar);
	        	montoMap.put(descripcionLinea+"0", montoTotalIva.toString());
			}
		}
			
			BigDecimal montoRestanteP = proyecto.getBigDecimal("montoRestante");
        	BigDecimal montoRestantePF = montoRestanteP.subtract(montoTotalIva);
        	
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
            
            input.put("eventoContableId", eventoServiciosPersonales);
        	input.put("tipoClaveId", "EGRESO");
        	input.put("fechaRegistro", UtilDateTime.nowTimestamp());
        	input.put("fechaContable", fechaContable);
        	input.put("currency", tipoMoneda);
        	input.put("usuario", userLogin.getString("userLoginId"));
        	input.put("organizationId", organizationPartyId);
        	input.put("descripcion", conceptoPoliza);
        	input.put("roleTypeId", "BILL_FROM_VENDOR");
        	input.put("valorCampo",  proyectoId);
        	input.put("campo", "proyectoId");      
        	input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(eventoServiciosPersonales, delegator, context2, null, null, null, null));
        	
        	
        	//Genera registro en DepreciacionResumen
        	output = dispatcher.runSync("creaTransaccionMotor", input);
        	Debug.log("output: " + output);
        	if(ServiceUtil.isError(output)){
        		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
        	}
        	
        	GenericValue transaccion = (GenericValue) output.get("transaccion");
        	poliza = transaccion.getString("poliza");
        	
        	serviciosPer.set("acctgTransIdServPer", transaccion.getString("acctgTransId"));
        	proyecto.set("montoRestante", montoRestantePF);
        	
        	proyecto.store();
        	serviciosPer.create();

			Map<String, Object> result = ServiceUtil.returnSuccess("La provision de los servicios personales ha sido creada con \u00e9xito con la poliza " +poliza);		

			result.put("servicioPersonalId", serviciosPer.getString("servicioPersonalId"));
			return result;
			
		}catch (GenericEntityException | GenericServiceException e) {
			return ServiceUtil.returnError(e.getMessage());
		} 
	}
	
	public static Map<String, Object> pagoServiciosPersonales(DispatchContext dctx,Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String servicioPersonalId = (String) context.get("servicioPersonalId");
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
			
			GenericValue serviciosPersonales = delegator.findByPrimaryKey("ServiciosPersonalesProy", UtilMisc.toMap("servicioPersonalId", servicioPersonalId));
			
			BigDecimal sueldo = serviciosPersonales.getBigDecimal("sueldo");
			BigDecimal iva = serviciosPersonales.getBigDecimal("iva");
			BigDecimal ivaR = serviciosPersonales.getBigDecimal("ivaR");
			BigDecimal isr = serviciosPersonales.getBigDecimal("isr");
			BigDecimal montoTotal = serviciosPersonales.getBigDecimal("montoTotal");
			BigDecimal montoTotalIva = sueldo.add(iva);
			
			String auxiliar = serviciosPersonales.getString("auxiliar");
			GenericValue proyecto = delegator.findByPrimaryKey("Proyecto", UtilMisc.toMap("proyectoId", serviciosPersonales.getString("proyectoId")));
			GenericValue persona = delegator.findByPrimaryKey("ParticipantesProyecto", UtilMisc.toMap("proyectoId", serviciosPersonales.getString("proyectoId"), "detalleParticipanteId", serviciosPersonales.getString("detalleParticipanteId")));
			List<String> orderByLinea = UtilMisc.toList("secuencia");
	    	EntityCondition condicionLinea = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, eventoPago));
			
			List<GenericValue> lineaList = delegator.findByCondition("LineaContable", condicionLinea, null, orderByLinea);
			Debug.logInfo("condicion: " + condicionLinea,MODULE);
			Debug.logInfo("lineaList: " + lineaList,MODULE);
			int i = lineaList.size();
			if(i>1){
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
						if(i==1 && UtilValidate.isNotEmpty(montoTotal)){
							montoMap.put(descripcionLinea+"0", montoTotal.toString());
						}else if(i==2 && UtilValidate.isNotEmpty(ivaR)){
							montoMap.put(descripcionLinea+"0", ivaR.toString());
						}else if(i==3 && UtilValidate.isNotEmpty(isr)){
							montoMap.put(descripcionLinea+"0", isr.toString());
						}else{
							
						}
						i--;
					}
				}
			}else{	Iterator<GenericValue> listaIter = lineaList.iterator();	        
			   
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
										:serviciosPersonales.getString("auxiliarCancelaPasivo") );
	        	montoMap.put(descripcionLinea+"0", montoTotal.toString());
			}
		}
			
			//BigDecimal montoRestanteP = proyecto.getBigDecimal("montoRestante");
        	//BigDecimal montoRestantePF = montoRestanteP.subtract(serviciosPersonales.getBigDecimal("montoTotal"));
        	
        	/*if(montoRestantePF.compareTo(BigDecimal.ZERO)<=0){
        		return ServiceUtil.returnError("No cuenta con suficiencia en el proyecto para esta operacion");
        	}*/
			
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
        	input.put("currency", serviciosPersonales.getString("tipoMoneda"));
        	input.put("usuario", userLogin.getString("userLoginId"));
        	input.put("organizationId", "1");
        	input.put("descripcion", "PAGO - "+ serviciosPersonales.getString("conceptoPoliza"));
        	input.put("roleTypeId", "BILL_FROM_VENDOR");
        	input.put("valorCampo", serviciosPersonales.getString("proyectoId"));
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
        	
        	if(serviciosPersonales.getString("tipoContratacion").equals("Becas")){
        		serviciosPersonales.set("statusId", "FINALIZADO");
			}else{
				serviciosPersonales.set("statusId", "PAGADO");
			}
        	serviciosPersonales.set("fechaContablePago", fechaContable);
        	serviciosPersonales.set("paymentTypeId", paymentTypeId);
        	serviciosPersonales.set("eventoServPerPago", eventoPago);
        	serviciosPersonales.set("acctgTransIdPago", transaccion.getString("acctgTransId"));
        	//proyecto.set("montoRestante", montoRestantePF);
        	
        	//proyecto.store();
        	serviciosPersonales.store();

			Map<String, Object> result = ServiceUtil.returnSuccess("Se ha realizado el pago con \u00e9xito y se ha creado la poliza " +poliza);		

			result.put("servicioPersonalId", servicioPersonalId);
			return result;
		}catch (GenericEntityException | GenericServiceException e) 
			{	return ServiceUtil.returnError("Error al crear el pago: " + e.getMessage());
			}
	}
	
	/**
	 * Cancela servicios personales de proyecto
	 * @param dctx
	 * @param context
	 * @return
	 */
	
	public static Map<String, Object> cancelarServPerProyecto(DispatchContext dctx,Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		
		String servicioPersonalId = (String) context.get("servicioPersonalId");
		Map<String, Object> result = ServiceUtil.returnSuccess();

		try	{
			
			GenericValue nuevoServPer = delegator.findByPrimaryKey("ServiciosPersonalesProy", UtilMisc.toMap("servicioPersonalId", servicioPersonalId));
			
			String polizaCreacion = nuevoServPer.getString("acctgTransIdServPer");
			
			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
					  EntityCondition.makeCondition("acctgTransId", EntityOperator.EQUALS, polizaCreacion),
					  EntityCondition.makeCondition("acctgTransEntrySeqId",EntityOperator.LIKE,"%R"));
			
			List<GenericValue> detallesPoliza = delegator.findByCondition("AcctgTransEntry", condiciones, null, null);
			
			if(UtilValidate.isEmpty(detallesPoliza)){
				result = ServiceUtil.returnError("No se puede cancelar el proceso de Servicios Personales ya que aun no se revierte la poliza de solicitud");
			}else{
				if(nuevoServPer.getString("statusId").equals("PAGADO")||nuevoServPer.getString("statusId").equals("FINALIZADO")){
					result = ServiceUtil.returnError("No se puede cancelar el proceso de Servicios Personales ya que se encuentra pagado o finalizado");
				}else{
					nuevoServPer.set("statusId","CANCELADO");				
					delegator.store(nuevoServPer);
				}
				
			}			
						
			result.put("servicioPersonalId", nuevoServPer.getString("servicioPersonalId"));
			return result;
		}catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}
	
	public static Map<String, Object> reembolsoServiciosPersonales(DispatchContext dctx,Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String servicioPersonalId = (String) context.get("servicioPersonalId");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		String eventoReembolso = (String) context.get("eventoReembolso");
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
			
			GenericValue serviciosPersonales = delegator.findByPrimaryKey("ServiciosPersonalesProy", UtilMisc.toMap("servicioPersonalId", servicioPersonalId));
			
			BigDecimal sueldo = serviciosPersonales.getBigDecimal("sueldo");
			BigDecimal iva = serviciosPersonales.getBigDecimal("iva");
			BigDecimal ivaR = serviciosPersonales.getBigDecimal("ivaR");
			BigDecimal isr = serviciosPersonales.getBigDecimal("isr");
			BigDecimal montoTotal = serviciosPersonales.getBigDecimal("montoTotal");
			BigDecimal montoTotalIva = ivaR.add(isr);
			
			String auxiliar = serviciosPersonales.getString("auxiliar");
			GenericValue proyecto = delegator.findByPrimaryKey("Proyecto", UtilMisc.toMap("proyectoId", serviciosPersonales.getString("proyectoId")));
			String cuentaBancoProyecto = proyecto.getString("cuentaOrigen");
			GenericValue persona = delegator.findByPrimaryKey("ParticipantesProyecto", UtilMisc.toMap("proyectoId", serviciosPersonales.getString("proyectoId"), "detalleParticipanteId", serviciosPersonales.getString("detalleParticipanteId")));
			List<String> orderByLinea = UtilMisc.toList("secuencia");
	    	EntityCondition condicionLinea = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, eventoReembolso));
			
			List<GenericValue> lineaList = delegator.findByCondition("LineaContable", condicionLinea, null, orderByLinea);
			Debug.logInfo("condicion: " + condicionLinea,MODULE);
			Debug.logInfo("lineaList: " + lineaList,MODULE);
			int i = lineaList.size();
			if(i>1){
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
														.equalsIgnoreCase("BANCO") ? cuentaBancoProyecto
												: auxiliar);
						if(i==1 && UtilValidate.isNotEmpty(ivaR)){
							montoMap.put(descripcionLinea+"0", ivaR.toString());
						}else if(i==2 && UtilValidate.isNotEmpty(isr)){
							montoMap.put(descripcionLinea+"0", isr.toString());
						}else{
							
						}
						i--;
					}
				}
			}else{	Iterator<GenericValue> listaIter = lineaList.iterator();	        
			   
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
												.equalsIgnoreCase("BANCO") ? cuentaBancoProyecto
										:serviciosPersonales.getString("auxiliarCancelaPasivo") );
	        	montoMap.put(descripcionLinea+"0", montoTotalIva.toString());
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
            
            input.put("eventoContableId", eventoReembolso);
        	input.put("tipoClaveId", "EGRESO");
        	input.put("fechaRegistro", UtilDateTime.nowTimestamp());
        	input.put("fechaContable", fechaContable);
        	input.put("currency", serviciosPersonales.getString("tipoMoneda"));
        	input.put("usuario", userLogin.getString("userLoginId"));
        	input.put("organizationId", "1");
        	input.put("descripcion", "REEMBOLSO - "+ serviciosPersonales.getString("conceptoPoliza"));
        	input.put("roleTypeId", "BILL_FROM_VENDOR");
        	input.put("valorCampo", serviciosPersonales.getString("proyectoId"));
        	input.put("campo", "proyectoId");      
        	input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(eventoReembolso, delegator, context2, null, null, null, null));
        	
        	
        	//Genera registro en DepreciacionResumen
        	output = dispatcher.runSync("creaTransaccionMotor", input);
        	Debug.log("output: " + output);
        	if(ServiceUtil.isError(output)){
        		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
        	}
        	
        	GenericValue transaccion = (GenericValue) output.get("transaccion");
        	poliza = transaccion.getString("poliza");
        	
        	serviciosPersonales.set("statusId", "FINALIZADO");
			
        	serviciosPersonales.set("fechaContableReembolso", fechaContable);
        	serviciosPersonales.set("paymentTypeIdReembolso", paymentTypeId);
        	serviciosPersonales.set("eventoServPerReembolso", eventoReembolso);
        	serviciosPersonales.set("acctgTransIdReembolso", transaccion.getString("acctgTransId"));
        	serviciosPersonales.store();

			Map<String, Object> result = ServiceUtil.returnSuccess("Se ha realizado el reembolso con \u00e9xito y se ha creado la poliza " +poliza);		

			result.put("servicioPersonalId", servicioPersonalId);
			return result;
		}catch (GenericEntityException | GenericServiceException e) 
			{	return ServiceUtil.returnError("Error al crear el reembolso: " + e.getMessage());
			}
	}

}
