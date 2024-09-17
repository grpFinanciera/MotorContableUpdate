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

public class ServiciosGastosProyectos {
	
	private static String MODULE = ServiciosGastosProyectos.class.getName();
	
	/**
	 * Crea Proyecto
	 * @param dctx
	 * @param context
	 * @return
	 */
	
	public static Map<String, Object> registraGasto(DispatchContext dctx,Map<?, ?> context) {
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
		String partyId = userLogin.getString("partyId");
		String organizationPartyId = (String) context.get("organizationPartyId");
		Timestamp fecha = (Timestamp) context.get("fecha");
		String tipoMoneda = (String) context.get("tipoMoneda");
		String concepto = (String) context.get("concepto");
		BigDecimal monto = (BigDecimal) context.get("monto");
		String proveedor = (String) context.get("proveedor");
		String observaciones = (String) context.get("observaciones");
		String acctgTransTypeId = (String) context.get("eventoSolicitud");
    	String proyectoId = (String) context.get("proyectoId");
    	String paymentTypeId = (String) context.get("paymentTypeId");
		String cuentaBancariaId = (String) context.get("cuentaOrigen");
		String auxiliar = (String) context.get("auxiliar");

		try	{
			
			GenericValue gasto = GenericValue.create(delegator.getModelEntity("GastosXCompProyecto"));
			GenericValue proyecto = delegator.findByPrimaryKey("Proyecto", UtilMisc.toMap("proyectoId", proyectoId));
			/*if (UtilValidate.isNotEmpty(proyecto)) {
				cuentaBancariaId = proyecto.getString("cuentaOrigen");
			}*/
			gasto.setAllFields(context, false, null, null);
			gasto.setNextSeqId();
			gasto.set("fecha", fecha);
			gasto.set("concepto", concepto);
			gasto.set("tipoMoneda", tipoMoneda);
			gasto.set("proveedor", proveedor);
			gasto.set("observaciones", observaciones);
			gasto.set("monto", monto);
			gasto.set("statusId", "SOLICITADO");
			gasto.set("acctgTransTypeId", acctgTransTypeId);
			gasto.set("auxEmpleado", auxiliar);
			gasto.set("paymentTypeId", paymentTypeId);
			
			List<String> orderByLinea = UtilMisc.toList("secuencia");
	    	EntityCondition condicionLinea = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, acctgTransTypeId));
			List<GenericValue> lineaList = delegator.findByCondition("LineaContable", condicionLinea, null, orderByLinea);
			Debug.logInfo("condicion: " + condicionLinea,MODULE);
			Debug.logInfo("lineaList: " + lineaList,MODULE);
			
			if(!lineaList.isEmpty())
			{	Iterator<GenericValue> listaIter = lineaList.iterator();	        
			while (listaIter.hasNext()) {
				GenericValue item = listaIter.next();
				String descripcionLinea = item.getString("descripcion");
				descripcionLinea = descripcionLinea.replaceAll(" ", "");
				catalogoCargoContMap.put(descripcionLinea + "0",
						item.getString("catalogoCargo") != null
								&& item.getString("catalogoCargo").equalsIgnoreCase("BANCO") ? cuentaBancariaId
										: auxiliar);
				catalogoAbonoContMap.put(descripcionLinea + "0",
						item.getString("catalogoAbono") != null
								&& item.getString("catalogoAbono").equalsIgnoreCase("BANCO") ? cuentaBancariaId
										: auxiliar);
				montoMap.put(descripcionLinea + "0", monto.toString());
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
            
            input.put("eventoContableId", acctgTransTypeId);
        	input.put("tipoClaveId", "EGRESO");
        	input.put("fechaRegistro", UtilDateTime.nowTimestamp());
        	input.put("fechaContable", fecha);
        	input.put("currency", tipoMoneda);
        	input.put("usuario", userLogin.getString("userLoginId"));
        	input.put("organizationId", organizationPartyId);
        	input.put("descripcion", "Gasto "+ concepto);
        	input.put("roleTypeId", "BILL_FROM_VENDOR");
        	input.put("valorCampo",  proyectoId);
        	input.put("campo", "proyectoId");      
        	input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(acctgTransTypeId, delegator, context2, null, null, null, null));
        	
        	
        	//Genera registro en DepreciacionResumen
        	output = dispatcher.runSync("creaTransaccionMotor", input);
        	Debug.log("output: " + output);
        	if(ServiceUtil.isError(output)){
        		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
        	}
        	
        	GenericValue transaccion = (GenericValue) output.get("transaccion");
        	poliza = transaccion.getString("poliza");
        	
        	gasto.set("acctgTransIdSolicitud", transaccion.getString("acctgTransId"));
        	gasto.create();

			Map<String, Object> result = ServiceUtil.returnSuccess("El gasto por comprobar ha sido creado con \u00e9xito con la poliza " +poliza);		

			result.put("gastoProyectoId", gasto.getString("gastoProyectoId"));
			return result;
			
		}catch (GenericEntityException | GenericServiceException e) {
			return ServiceUtil.returnError(e.getMessage());
		} 
	}
	
	public static Map<String, Object> agregaDetalleGastosProyectos(DispatchContext dctx,Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		String gastoProyectoId = (String) context.get("gastoProyectoId");
		String iva = (String) context.get("iva");
		String montoSubtotalFactura = (String) context.get("montoSubtotalFactura");
		String montoTotalFactura = (String) context.get("montoTotalFactura");
		String facturaNota = (String) context.get("facturaNota");
		String gastoProyecto = (String) context.get("gastoProyecto");
		BigDecimal montoTotalFact = BigDecimal.ZERO;
		montoTotalFact = montoTotalFact.add(BigDecimal.valueOf(Double.valueOf(montoTotalFactura)));
		
		try	{
		
			GenericValue GastoReserva = delegator.findByPrimaryKey("GastosXCompProyecto", UtilMisc.toMap("gastoProyectoId", gastoProyectoId));
			BigDecimal monto = GastoReserva.getBigDecimal("monto");
			BigDecimal montoComprobado = BigDecimal.ZERO;
			
			if(UtilValidate.isNotEmpty(GastoReserva.getBigDecimal("montoComprobado"))){
				montoComprobado = GastoReserva.getBigDecimal("montoComprobado");
			}
					
			//Validación de equilibrio del subtotal y el iva con el total.
			int montoValidarFactura =  BigDecimal.valueOf(Double.valueOf(montoTotalFactura)).compareTo(BigDecimal.valueOf(Double.valueOf(montoSubtotalFactura)).add(BigDecimal.valueOf(Double.valueOf(iva))));
			if(montoValidarFactura<0){
				return ServiceUtil.returnError("El Subtotal de la factura + IVA es mayor al Total");
			}
			if(montoValidarFactura>0){
				return ServiceUtil.returnError("El Total de la factura es mayor al Subtotal + IVA");
			}
			
		
		
			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND, EntityCondition.makeCondition("gastoProyectoId", EntityOperator.EQUALS, gastoProyectoId));
			List<GenericValue> listDetalleGasto = delegator.findByCondition("DetalleGastoProyecto",condiciones, null, UtilMisc.toList("detalleGId DESC"));
			
			//Validación de factura o nota existente en el sistema
			for (GenericValue detalleGastoComparar : listDetalleGasto) {
				montoTotalFact = montoTotalFact.add(BigDecimal.valueOf(Double.valueOf(detalleGastoComparar.getString("montoTotalFactura"))));
				if(facturaNota.equals(detalleGastoComparar.getString("facturaNota"))){
					return ServiceUtil.returnError("El número de factura o nota ya existe");
				}
			}
			
			//Validación monto total a comprobar excede el monto de lo solicitado
			int montoValidarMontFactura =  monto.compareTo(montoTotalFact);
			/*if(montoValidarMontFactura<0){
				return ServiceUtil.returnError("El monto comprobado es mayor al monto solicitado. No se puede agregar la factura");
			}*/
			
			long detalleId = 0L;
			
			if(!listDetalleGasto.isEmpty()){
				detalleId = Long.valueOf(EntityUtil.getFirst(listDetalleGasto).getString("detalleGId"));
			}
			
			String detalleGastoId = UtilFormatOut.formatPaddedNumber((detalleId + 1), 4);			
			GenericValue guardarDetalleGasto = GenericValue.create(delegator.getModelEntity("DetalleGastoProyecto"));
			guardarDetalleGasto.setAllFields(context, true, null, null);
			guardarDetalleGasto.set("detalleGId", detalleGastoId);
			guardarDetalleGasto.set("gastoProyectoId", gastoProyectoId);
			guardarDetalleGasto.set("iva", UtilValidate.isNotEmpty(iva) ? iva : "N");
			guardarDetalleGasto.set("gastoProyId", gastoProyecto);
			
			GastoReserva.set("montoComprobado", montoTotalFact);
			
			delegator.create(guardarDetalleGasto);
			delegator.store(GastoReserva);
			
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("gastoProyectoId", gastoProyectoId);
			result.put("detalleGId", guardarDetalleGasto.getString("detalleGId"));
			
			return result;
		}catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		 }
	}
	
	/**
	 * Crea Proyecto
	 * @param dctx
	 * @param context
	 * @return
	 */
	
	public static Map<String, Object> generaPolizaGastoComprobada(DispatchContext dctx,Map<?, ?> context) {
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
		String gastoProyectoId = (String) context.get("gastoProyectoId");
		String eventoComprueba = (String) context.get("eventoComprueba");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		BigDecimal montoComprueba = (BigDecimal) context.get("montoComprueba");
		String auxiliarCargo = null;
		//String auxiliarAbono = (String) context.get("auxiliarAbono");
		//String auxiliarDevo = (String) context.get("auxiliarDevo");
		String cuentaBancariaId = null;

		try	{
			
			GenericValue gasto = delegator.findByPrimaryKey("GastosXCompProyecto", UtilMisc.toMap("gastoProyectoId", gastoProyectoId));
			GenericValue proyecto = delegator.findByPrimaryKey("Proyecto", UtilMisc.toMap("proyectoId", gasto.getString("proyectoId")));
			String auxiliar = gasto.getString("auxEmpleado");
			BigDecimal monto = BigDecimal.ZERO;
			
			if(UtilValidate.isNotEmpty(gasto.getBigDecimal("monto"))){
				monto = gasto.getBigDecimal("monto");
			}
			
			
			BigDecimal montoComprobado = BigDecimal.ZERO;
			
			if(UtilValidate.isNotEmpty(gasto.getBigDecimal("montoComprobado"))){
				montoComprobado = gasto.getBigDecimal("montoComprobado");
			}
			if (UtilValidate.isNotEmpty(proyecto.getString("cuentaOrigen"))) {
				cuentaBancariaId = proyecto.getString("cuentaOrigen");
			}
			if (UtilValidate.isNotEmpty(proyecto.getString("auxiliarProyecto"))) {
				auxiliarCargo = proyecto.getString("auxiliarProyecto");
			}
			
			List<String> orderByLinea = UtilMisc.toList("secuencia");
	    	EntityCondition condicionLinea = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, eventoComprueba));
			List<GenericValue> lineaList = delegator.findByCondition("LineaContable", condicionLinea, null, orderByLinea);
			Debug.logInfo("condicion: " + condicionLinea,MODULE);
			Debug.logInfo("lineaList: " + lineaList,MODULE);
			BigDecimal montoRestante = monto.subtract(montoComprueba);
			
			if(!lineaList.isEmpty())
			{	Iterator<GenericValue> listaIter = lineaList.iterator();	        
			while (listaIter.hasNext()) {
				GenericValue item = listaIter.next();
				String descripcionLinea = item.getString("descripcion");
				descripcionLinea = descripcionLinea.replaceAll(" ", "");
				catalogoCargoContMap.put(descripcionLinea + "0",
						item.getString("catalogoCargo") != null
								&& item.getString("catalogoCargo").equalsIgnoreCase("BANCO") ? cuentaBancariaId
										: auxiliarCargo);
				catalogoAbonoContMap.put(descripcionLinea + "0",
						item.getString("catalogoAbono") != null
								&& item.getString("catalogoAbono").equalsIgnoreCase("BANCO") ? cuentaBancariaId
										: auxiliar);
				montoMap.put(descripcionLinea + "0", montoComprueba.toString());
			}
			} 
			
			BigDecimal montoRestanteP = proyecto.getBigDecimal("montoRestante");
        	BigDecimal montoRestantePF = montoRestanteP.subtract(montoComprueba);
        	
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
            
            input.put("eventoContableId", eventoComprueba);
        	input.put("tipoClaveId", "EGRESO");
        	input.put("fechaRegistro", UtilDateTime.nowTimestamp());
        	input.put("fechaContable", fechaContable);
        	input.put("currency", gasto.getString("tipoMoneda"));
        	input.put("usuario", userLogin.getString("userLoginId"));
        	input.put("organizationId", "1");
        	input.put("descripcion", "Comprobacion gasto "+ gasto.getString("concepto"));
        	input.put("roleTypeId", "BILL_FROM_VENDOR");
        	input.put("valorCampo",  gasto.getString("proyectoId"));
        	input.put("campo", "proyectoId");      
        	input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(eventoComprueba, delegator, context2, null, null, null, null));
        	
        	
        	//Genera registro en DepreciacionResumen
        	output = dispatcher.runSync("creaTransaccionMotor", input);
        	Debug.log("output: " + output);
        	if(ServiceUtil.isError(output)){
        		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
        	}
        	
        	GenericValue transaccion = (GenericValue) output.get("transaccion");
        	poliza = transaccion.getString("poliza");
        	
        	gasto.set("acctgTransIdComprobacion", transaccion.getString("acctgTransId"));
        	
        	proyecto.set("montoRestante", montoRestantePF);
        	Map<String, Object> result = ServiceUtil.returnSuccess();
        	
        	if(montoRestante.compareTo(BigDecimal.ZERO)==1){
        		gasto.set("statusId", "DEVOLVER");	
        		result = ServiceUtil.returnSuccess("El gasto se ha comprobado con exito con la poliza " +poliza);		
        	}else{
        		gasto.set("statusId", "FINALIZADO");
        		result = ServiceUtil.returnSuccess("El gasto se ha comprobado con exito con la poliza " +poliza);		
        	}
        	
        	proyecto.store();
        	gasto.store();

			result.put("gastoProyectoId", gasto.getString("gastoProyectoId"));
			return result;
			
		}catch (GenericEntityException | GenericServiceException e) {
			return ServiceUtil.returnError(e.getMessage());
		} 
	}
	
	public static Map<String, Object> generaPolizaGastoDevo(DispatchContext dctx,Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
	    Map<String, Object> inputDevo = FastMap.newInstance();
	    Map<String, Object> outputDevo = FastMap.newInstance();
	    Map<String, Object> montoMapDevo = FastMap.newInstance();
	    Map<String, Object> clavePresupuestalMapDevo = FastMap.newInstance();
	    Map<String, Object> catalogoCargoContMapDevo = FastMap.newInstance();
	    Map<String, Object> catalogoAbonoContMapDevo = FastMap.newInstance();
	    Map<String, Object> context2Devo = FastMap.newInstance();
	    String polizaDevo = null;
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String gastoProyectoId = (String) context.get("gastoProyectoId");
		String eventoDevuelve = (String) context.get("eventoDevuelve");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		String auxiliarCargo = null;
		String cuentaBancariaId = null;

		try	{
			
			GenericValue gasto = delegator.findByPrimaryKey("GastosXCompProyecto", UtilMisc.toMap("gastoProyectoId", gastoProyectoId));
			GenericValue proyecto = delegator.findByPrimaryKey("Proyecto", UtilMisc.toMap("proyectoId", gasto.getString("proyectoId")));
			String auxiliar = gasto.getString("auxEmpleado");
			BigDecimal monto = BigDecimal.ZERO;
			
			if(UtilValidate.isNotEmpty(gasto.getBigDecimal("monto"))){
				monto = gasto.getBigDecimal("monto");
			}
			
			
			BigDecimal montoComprobado = BigDecimal.ZERO;
			
			if(UtilValidate.isNotEmpty(gasto.getBigDecimal("montoComprobado"))){
				montoComprobado = gasto.getBigDecimal("montoComprobado");
			}
			if (UtilValidate.isNotEmpty(proyecto.getString("cuentaOrigen"))) {
				cuentaBancariaId = proyecto.getString("cuentaOrigen");
			}
			if (UtilValidate.isNotEmpty(proyecto.getString("auxiliarProyecto"))) {
				auxiliarCargo = proyecto.getString("auxiliarProyecto");
			}
				    	
			BigDecimal montoRestante = monto.subtract(montoComprobado);
        	
        	
    		List<String> orderByLineaDevo = UtilMisc.toList("secuencia");
	    	EntityCondition condicionLineaDevo = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, eventoDevuelve));
			List<GenericValue> lineaListDevo = delegator.findByCondition("LineaContable", condicionLineaDevo, null, orderByLineaDevo);
			
			if(!lineaListDevo.isEmpty())
			{	Iterator<GenericValue> listaIter = lineaListDevo.iterator();	        
			while (listaIter.hasNext()) {
				GenericValue item = listaIter.next();
				String descripcionLinea = item.getString("descripcion");
				descripcionLinea = descripcionLinea.replaceAll(" ", "");
				catalogoCargoContMapDevo.put(descripcionLinea + "0",
						item.getString("catalogoCargo") != null
								&& item.getString("catalogoCargo").equalsIgnoreCase("BANCO") ? cuentaBancariaId
										: auxiliarCargo);
				catalogoAbonoContMapDevo.put(descripcionLinea + "0",
						item.getString("catalogoAbono") != null
								&& item.getString("catalogoAbono").equalsIgnoreCase("BANCO") ? cuentaBancariaId
										: auxiliar);
				montoMapDevo.put(descripcionLinea + "0", montoRestante.toString());
			}
			}        					
        	clavePresupuestalMapDevo.put("0", "");	        	
        		        	
            context2Devo.put("montoMap", montoMapDevo);        
            context2Devo.put("clavePresupuestalMap", clavePresupuestalMapDevo);
            context2Devo.put("catalogoCargoContMap", catalogoCargoContMapDevo);
            context2Devo.put("catalogoAbonoContMap", catalogoAbonoContMapDevo);
            
            Debug.log("montoMap: " + montoMapDevo);
            Debug.log("clavePresupuestalMap: " + clavePresupuestalMapDevo);
            Debug.log("catalogoCargoContMap: " + catalogoCargoContMapDevo);
            Debug.log("catalogoAbonoContMap: " + catalogoAbonoContMapDevo);
			
            
            inputDevo.put("eventoContableId", eventoDevuelve);
            inputDevo.put("tipoClaveId", "EGRESO");
            inputDevo.put("fechaRegistro", UtilDateTime.nowTimestamp());
            inputDevo.put("fechaContable", fechaContable);
            inputDevo.put("currency", gasto.getString("tipoMoneda"));
            inputDevo.put("usuario", userLogin.getString("userLoginId"));
            inputDevo.put("organizationId", "1");
            inputDevo.put("descripcion", "Devolucion gasto "+ gasto.getString("concepto"));
            inputDevo.put("roleTypeId", "BILL_FROM_VENDOR");
            inputDevo.put("valorCampo",  gasto.getString("proyectoId"));
            inputDevo.put("campo", "proyectoId");      
            inputDevo.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(eventoDevuelve, delegator, context2Devo, null, null, null, null));
        	
        	
        	//Genera registro en DepreciacionResumen
        	outputDevo = dispatcher.runSync("creaTransaccionMotor", inputDevo);
        	Debug.log("output: " + outputDevo);
        	if(ServiceUtil.isError(outputDevo)){
        		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(outputDevo));
        	}
        	
        	GenericValue transaccionDevo = (GenericValue) outputDevo.get("transaccion");
        	polizaDevo = transaccionDevo.getString("poliza");
        	
        	gasto.set("acctgTransIdDevuelve", transaccionDevo.getString("acctgTransId"));
        		
        	Map<String, Object> result = ServiceUtil.returnSuccess();
        	gasto.set("statusId", "FINALIZADO");
        	result = ServiceUtil.returnSuccess("El gasto se ha comprobado con exito con la poliza " +polizaDevo);		
        	
        	proyecto.store();
        	gasto.store();

			result.put("gastoProyectoId", gasto.getString("gastoProyectoId"));
			return result;
			
		}catch (GenericEntityException | GenericServiceException e) {
			return ServiceUtil.returnError(e.getMessage());
		} 
	}
	
	public static Map<String, Object> devolverViaticoGasto(DispatchContext dctx, Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		String gastoProyectoId = (String) context.get("gastoProyectoId");
		
		try {
			
			delegator.storeByCondition("GastosXCompProyecto", UtilMisc.toMap("statusId","DEVOLVER"), EntityCondition.makeCondition("gastoProyectoId", gastoProyectoId));
			
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result = ServiceUtil.returnSuccess("Se ha procesado el gasto para que realice la devolucion ");
			result.put("gastoProyectoId", gastoProyectoId);
			return result;			
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}

}
