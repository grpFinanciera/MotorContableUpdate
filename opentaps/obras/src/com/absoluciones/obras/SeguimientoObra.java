package com.absoluciones.obras;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

public class SeguimientoObra {
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map seguimientoDeObra(DispatchContext dctx, Map context) throws GeneralException, ParseException, Exception, GenericServiceException, GenericEntityException {
		
		Delegator delegator = dctx.getDelegator();
		String organizationPartyId = (String) context.get("organizationPartyId");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		String description = (String) context.get("description");
		String obraId = (String) context.get("obraId");
		String contratoId = (String) context.get("contratoId");
		BigDecimal montoAvanceConIva = (BigDecimal) context.get("montoAvance");
		String performFind = (String) context.get("performFind");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		Timestamp fechaRealInicio = (Timestamp) context.get("fechaRealInicio");
		Timestamp fechaRealFin = (Timestamp) context.get("fechaRealFin");
		
		Debug.log("Mike contratoId: " + contratoId);
		
		List<String> error = new ArrayList<String>();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Map resultado = ServiceUtil.returnSuccess();
		
		
		if (performFind != null && "Y".equals(performFind)) {
			
			Debug.log("Entra a crear poliza para avance de obra");			
			String agrupador = null;
			
			if (organizationPartyId == null){
                error.add("La organizaci\u00f3n no se ha enviado correctamente");
			}
			if (description == null){

                error.add("Se necesita ingresar el siguiente par\u00e1metro  descripci\u00f3n");

			}
            if (fechaContable == null){
            	error.add("Se necesita ingresar el siguiente par\u00e1metro  fecha contable");
            }
            if(obraId == null){
            	error.add("Se necesita ingresar el siguiente par\u00e1metro  obra");
            }
            if(montoAvanceConIva == null){
            	error.add("Se necesita ingresar el siguiente par\u00e1metro el monto de avance");
            }
            if(contratoId == null){
            	error.add("Se necesita ingresar el siguiente par\u00e1metro el contratista");
            }
            if (!error.isEmpty()) {
				return ServiceUtil.returnError(error);
			}
            
            GenericValue obra = delegator.findByPrimaryKey("Obra",
    				UtilMisc.toMap("obraId", obraId));
            Debug.log("obra: " + obra);
            
            GenericValue contrato = delegator.findByPrimaryKey("ContratoObra",
    				UtilMisc.toMap("contratoId", contratoId));
            Debug.log("contrato: " + contrato);
            
            Debug.log("montoAvanceConIva: " + montoAvanceConIva);
            
            if(montoAvanceConIva.compareTo(contrato.getBigDecimal("valorContratoConIva")) > 0 ){
            	error.add("El monto ingresado supera el valor de la obra");
            }
            if(montoAvanceConIva.compareTo(BigDecimal.ZERO) <= 0 ){
            	error.add("El monto ingresado es menor o igual que cero");
            }
            if(contrato.getString("contratistaId") == null){
            	error.add("No se encuentra registrado el proveedor de la obra");
            }            
            if (!error.isEmpty()) {
				return ServiceUtil.returnError(error);
			}
            
            EntityCondition condition = EntityCondition.makeCondition(EntityOperator.AND,
    				EntityCondition.makeCondition("obraId", EntityOperator.EQUALS, obraId),
    				EntityCondition.makeCondition("contratoId", EntityOperator.EQUALS, contratoId));
            
            List<GenericValue> porcentajes = delegator.findByCondition("SeguimientoObra", condition, null,
    				null); 
            Iterator<GenericValue> por = porcentajes.iterator();
    		
            BigDecimal monCompara = montoAvanceConIva;
            while(por.hasNext()){
            	GenericValue segumiento = por.next();
            	monCompara = monCompara.add(segumiento.getBigDecimal("montoAvanceConIva"));
            }

            Debug.log("Obtiene monto de avance: " + monCompara);
            
            if(monCompara.compareTo(contrato.getBigDecimal("valorContratoConIva")) > 0){
            	error.add("Con el monto ingresado, el avance es mayor al 100%, se pasa por: " + 
            			(monCompara.subtract(contrato.getBigDecimal("valorContratoConIva"))));
            }
            
            Debug.log("monto: " + contrato.getBigDecimal("valorContratoConIva") + " : " + montoAvanceConIva);
            
            BigDecimal montoAvance = montoAvanceConIva.divide(new BigDecimal(1.16), 2, BigDecimal.ROUND_HALF_UP);
            Debug.log("montoAvance: " + montoAvance);
            BigDecimal montoIva = montoAvanceConIva.subtract(montoAvance);
            Debug.log("montoIva: " + montoIva);
            BigDecimal montoAmortizacion = montoAvanceConIva.multiply(new BigDecimal(contrato.getString("porcentajeAnticipo")).divide(new BigDecimal(100)));
            Debug.log("montoAmortizacion: " + montoAmortizacion);
            BigDecimal montoAnticipo = montoAmortizacion;
            
            List<String> orderBy = org.ofbiz.base.util.UtilMisc.toList("retencionId");
            
            EntityCondition conditionReten = EntityCondition.makeCondition(EntityOperator.AND,
    				EntityCondition.makeCondition("contratoId", EntityOperator.EQUALS, contratoId));
            
            List<GenericValue> retenciones = delegator.findByCondition("BuscarRetencion", conditionReten, null,
            		orderBy); 
            
            Debug.log("Retenciones: " + retenciones);
    		
            Iterator<GenericValue> reten = retenciones.iterator();

            int cont = 0;
            Map montosRetenciones = FastMap.newInstance();
            while(reten.hasNext()){
            	GenericValue retencion = reten.next();
            	BigDecimal montoRetencion = montoAvance.multiply(new BigDecimal(retencion.getString("porcentaje")).divide(new BigDecimal(100)));
            	Debug.log("montoRetencion"+cont+ ": "+ montoRetencion);
            	montoAnticipo = montoAnticipo.add(montoRetencion);
            	montosRetenciones.put("montoRetencion"+cont, montoRetencion);
            	Debug.log("montoAnticipo: " + montoAnticipo);
            	cont++;
            }  	

            Debug.log("montoAnticipo: " + montoAnticipo);
            BigDecimal pagoNeto = montoAvanceConIva.subtract(montoAnticipo);
            Debug.log("pagoNeto: " + pagoNeto);
            BigDecimal pogoSinAnticipo = montoAvanceConIva.subtract(montoAmortizacion);
            Debug.log("pogoSinAnticipo: " + pogoSinAnticipo);
            BigDecimal porcentajeAvance = (montoAvanceConIva.multiply(new BigDecimal(100))).divide(contrato.getBigDecimal("valorContratoConIva"), 2, BigDecimal.ROUND_HALF_UP);
            Debug.log("porcentajeAvance: " + porcentajeAvance);
            
            Debug.log("Obtiene valores de la obra");
            
//            Map<String, Object> inputC = FastMap.newInstance();
//            inputC.put("idTipoDoc", "PAG_PE_OBRA");
//            inputC.put("partyId", organizationPartyId);
//            inputC.put("transaccionRapida", "Y");
//            inputC.put("fechaTransaccion", UtilDateTime.nowTimestamp());
//            inputC.put("fechaContable", fechaContable);
//            inputC.put("description", description);
//			
//			Map<String, Object> result = null;
//			result = dispatcher.runSync("crearAgrupador", inputC);
//			
//			Debug.log("crearAgrupador.- " + result);
//			
//			if(ServiceUtil.isError(result)){
//				error.add(ServiceUtil.getErrorMessage(result));
//            }
//			
//			if (!error.isEmpty()) {
//				return ServiceUtil.returnError(error);
//			}
//			
//			agrupador = (String) result.get("agrupadorPolizaAux");
//			Debug.log("agrupador.- " + agrupador);
//			
//				// Se va anadiendo cada clavepresupuestal a la factura.
//				result = null;
//				inputC = FastMap.newInstance();
//				Map inputClasificaAdmin = FastMap.newInstance();
//				
//				inputClasificaAdmin.put("tipoClave", "EGRESO");
//                Map mapaClasif = dispatcher.runSync("buscaIndiceAdministrativa", inputClasificaAdmin);
//                Integer numClasAdmin = (Integer) mapaClasif.get("posicionClasificaAdmin");
//                                
//                for(int i=1; i<16; i++){
//					if(Integer.valueOf(numClasAdmin) == i)
//                		inputC.put("clasificacion"+i,buscaExternalId(obra.getString("acctgTagEnumIdAdmin"),delegator));
//                	String clasif = obra.getString("acctgTagEnumId" + i);
//                	if(clasif != null && !clasif.isEmpty())
//                		inputC.put("clasificacion"+i,buscaSequenceId(clasif, delegator));
//                }
//                
//                inputC.put("monto", montoAmortizacion.setScale(2, BigDecimal.ROUND_HALF_UP));
//                inputC.put("currency", contrato.getString("moneda"));
//                inputC.put("caParty", contrato.getString("contratistaId"));
//                inputC.put("caProductId", contrato.getString("productId"));
//                inputC.put("agrupadorPolizaAux", agrupador);
//                inputC.put("userLogin", userLogin);
//                
//                result = dispatcher.runSync("creaPolizaAgrupa", inputC);
//				
//				Debug.log("creaPolizaAgrupa.- " + result);
//				Debug.log("ServiceUtil.- " + ServiceUtil.isError(result));
//				Debug.log("ErrorMessage.- " + ServiceUtil.getErrorMessage(result));
//				
//				if(ServiceUtil.isError(result)){
//					error.add(ServiceUtil.getErrorMessage(result));
//	            }
//				
//				if (!error.isEmpty()) {
//					return ServiceUtil.returnError(error);
//				}
//			
//			// Se crea la poliza.
//			result = null;
//			inputC = FastMap.newInstance();
//			inputC.put("agrupadorPolizaAux", agrupador);
//			inputC.put("fechaTransaccion", UtilDateTime.nowTimestamp());
//			inputC.put("fechaContable", fechaContable);
//			inputC.put("idTipoDoc", "PAG_PE_OBRA");
//			inputC.put("partyId", organizationPartyId);
//			inputC.put("description", description);
//			inputC.put("userLogin", userLogin);
//			result = dispatcher.runSync("afectaCreaTransaccion", inputC);
//			
//			Debug.log("afectaCreaTransaccion.- " + result);
//				
//			if(ServiceUtil.isError(result)){
//				error.add(ServiceUtil.getErrorMessage(result));
//	        }
//			
//			if (!error.isEmpty()) {
//				return ServiceUtil.returnError(error);
//			}        	
//			
//			Debug.log("tipoPolizaId.- " + (String) result.get("tipoPolizaId"));
//			Debug.log("agrupador.- " + (String) result.get("agrupador"));
//			Debug.log("tipoTransaccion.- " + (String) result.get("tipoTransaccion"));
//			String tipoPolizaId = (String) result.get("tipoPolizaId");
//			String tipoTransaccion = (String) result.get("tipoTransaccion");
//			agrupador = (String) result.get("agrupador");
//	        
//	        Map input = FastMap.newInstance();
//            input.put("secuencia", "00001");
//            input.put("idTipoDoc", "DEV_PE");
//            input.put("tipoClave","EGRESO");
//            input.put("fechaTransaccion",UtilDateTime.nowTimestamp());
//            input.put("fechaContable",fechaContable);
//            input.put("currency", contrato.getString("moneda"));
//            input.put("userLogin", userLogin);
//            input.put("organizationPartyId",organizationPartyId);
//            input.put("caProductId", contrato.getString("productId"));
//            input.put("caParty", contrato.getString("contratistaId"));
//            input.put("descripcion", description);
//        	input.put("monto", pogoSinAnticipo.setScale(2, BigDecimal.ROUND_HALF_UP));
//        	input.put("referenciaDocumento", agrupador);
//        	input.put("tipoPolizaId", tipoPolizaId);
//        	input.put("tipoTransaccion", tipoTransaccion);
//        	
//        	for(int i=1; i<16; i++){
//				if(Integer.valueOf(numClasAdmin) == i)
//            		input.put("clasificacion"+i,buscaExternalId(obra.getString("acctgTagEnumIdAdmin"),delegator));
//            	String clasif = (String)obra.getString("acctgTagEnumId" + i);
//            	if(clasif != null && !clasif.isEmpty())
//            		input.put("clasificacion"+i,buscaSequenceId(clasif, delegator));
//            }
//        	
//        	Debug.log("RegistroContable: " + input);
//        	
//        	Map<String,Object> mapaMotor = dispatcher.runSync("registroContable_Presupuestal", input);
        	
        	/************************************/
            /******** Motor Contable ************/
            /************************************/
            Map<String, Object> input = FastMap.newInstance();
            Map<String, Object> output = FastMap.newInstance();
            Map<String, Object> montoMap = FastMap.newInstance();
            Map<String, Object> clavePresupuestalMap = FastMap.newInstance();
            Map<String, Object> productIds = FastMap.newInstance();
            Map<String, Object> catalogoCargoPresMap = FastMap.newInstance();
            Map<String, Object> catalogoAbonoPresMap = FastMap.newInstance();
            
            BigDecimal montoPago = montoAmortizacion.setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal montoDevengo = (pogoSinAnticipo.setScale(2, BigDecimal.ROUND_HALF_UP));
            
            montoMap.put("0", montoPago.toString());
            
            clavePresupuestalMap.put("0", obra.getString("clavePresupuestal"));

            productIds.put("0", contrato.getString("productId"));
            
            catalogoCargoPresMap.put("Devengado0", contrato.getString("productId"));
            catalogoCargoPresMap.put("Ejercido0", contrato.getString("productId"));
            catalogoCargoPresMap.put("Pago0", contrato.getString("contratistaId"));
            catalogoAbonoPresMap.put("Devengado0", contrato.getString("contratistaId"));
            catalogoAbonoPresMap.put("Ejercido0", contrato.getString("contratistaId"));

            context.put("clavePresupuestalMap", clavePresupuestalMap);
            context.put("catalogoCargoPresMap", catalogoCargoPresMap);
            context.put("catalogoAbonoPresMap", catalogoAbonoPresMap);
            
			Timestamp fechaTrans = new Timestamp(Calendar.getInstance().getTimeInMillis());
        	input.put("eventoContableId", "SEGUIMIENTO_OBRA2");
        	input.put("tipoClaveId", "EGRESO");
        	input.put("fechaRegistro", fechaTrans);
        	input.put("fechaContable", fechaContable);
        	input.put("currency", contrato.getString("moneda"));
        	input.put("usuario", userLogin.getString("userLoginId"));
        	input.put("organizationId", organizationPartyId);
        	input.put("descripcion", description);
        	input.put("roleTypeId", "BILL_FROM_VENDOR");
        	input.put("campo", "contratoId");
        	input.put("valorCampo", contratoId);
        	input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables("SEGUIMIENTO_OBRA2", delegator, context, contrato.getString("contratistaId"),null, montoMap, productIds));
        	
        	Debug.log("Entra: " + input);
        	output = dispatcher.runSync("creaTransaccionMotor", input);
        	
        	if(ServiceUtil.isError(output)){
        		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
        	}
        	GenericValue transaccion = (GenericValue) output.get("transaccion");
        	Debug.log("Transaccion: " + output);
        	
        	input.clear();
        	output.clear();
        	
        	montoMap.put("0", montoDevengo.toString());
            
            clavePresupuestalMap.put("0", obra.getString("clavePresupuestal"));
            
            productIds.put("0", contrato.getString("productId"));
            
            catalogoCargoPresMap.put("Devengado0", contrato.getString("productId"));
            catalogoAbonoPresMap.put("Devengado0", contrato.getString("contratistaId"));

            context.put("clavePresupuestalMap", clavePresupuestalMap);
            context.put("catalogoCargoPresMap", catalogoCargoPresMap);
            context.put("catalogoAbonoPresMap", catalogoAbonoPresMap);
            
	        input.put("eventoContableId", "SEGUIMIENTO_OBRA3");
	        input.put("tipoClaveId", "EGRESO");
	        input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables("SEGUIMIENTO_OBRA3", delegator, context, contrato.getString("contratistaId"),null, montoMap, productIds));
	        input.put("transaccion", transaccion);
	        output = dispatcher.runSync("agregaEventoTransaccion", input);
        	
	        if(ServiceUtil.isError(output)){
        		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
        	}
	        
	        GenericValue transaccion2 = (GenericValue) output.get("transaccion");
        	Debug.log("Transaccion2: " + output);
        	
        	//Actualiza el saldo del auxiliar de la OBRA
			GenericValue productoObra = buscaSaldoProductoObra(delegator, obra.getString("productId"), fechaContable, organizationPartyId);
			productoObra.set("monto", productoObra.getBigDecimal("monto").add(montoAvanceConIva));    			
			delegator.store(productoObra);

        	BigDecimal montoRestante = contrato.getBigDecimal("valorContratoConIva").subtract(monCompara).setScale(2, BigDecimal.ROUND_HALF_UP);
        	GenericValue seguimiento = GenericValue.create(delegator
    				.getModelEntity("SeguimientoObra"));
        	String avanceId = delegator.getNextSeqId("SeguimientoObra");
        	seguimiento.set("avanceId", avanceId);
        	seguimiento.set("obraId", obraId);
        	seguimiento.set("contratoId", contratoId);
        	seguimiento.set("descripcion", description);
        	seguimiento.set("porcentajeAvance", porcentajeAvance.setScale(2, BigDecimal.ROUND_HALF_UP));
        	seguimiento.set("montoAvance", montoAvance.setScale(2, BigDecimal.ROUND_HALF_UP));
        	seguimiento.set("montoIva", montoIva.setScale(2, BigDecimal.ROUND_HALF_UP));
        	seguimiento.set("montoAvanceConIva", montoAvanceConIva.setScale(2, BigDecimal.ROUND_HALF_UP));
        	seguimiento.set("montoAmortizacion", montoAmortizacion.setScale(2, BigDecimal.ROUND_HALF_UP));
        	seguimiento.set("montoAnticipo", montoAnticipo.setScale(2, BigDecimal.ROUND_HALF_UP));
        	seguimiento.set("pagoNeto", pagoNeto.setScale(2, BigDecimal.ROUND_HALF_UP));
        	seguimiento.set("montoRestante", montoRestante);
        	seguimiento.set("fechaAvance", fechaContable);
        	seguimiento.set("agrupador", transaccion2.getString("poliza"));
        	seguimiento.set("tipoPolizaId", transaccion2.getString("tipoPolizaId"));
        	seguimiento.set("acctgTransId", transaccion2.getString("acctgTransId"));
        	seguimiento.set("userLoginId", userLogin.getString("userLoginId"));
        	seguimiento.set("fechaRealInicio", fechaRealInicio);
        	seguimiento.set("fechaRealFin", fechaRealFin);
    		delegator.create(seguimiento);
    		
    		crearOrdenPago(userLogin, avanceId, contrato.getString("contratistaId"), contrato.getString("moneda") , description, fechaContable, dispatcher,
        			organizationPartyId, obraId, contrato.getString("productId"), pagoNeto.setScale(2, BigDecimal.ROUND_HALF_UP), obra, delegator);
    		
    		Debug.log("Creo orden de pago");
    		
    		if(!retenciones.isEmpty()){
    			Iterator<GenericValue> ret = retenciones.iterator();
    			int r = 0;
    			while(ret.hasNext()){
    				GenericValue retencionP = ret.next();
    				Debug.log("Monto de retencion: " + montosRetenciones.get("montoRetencion"+r).toString());
    				crearOrdenPago(userLogin, avanceId, retencionP.getString("auxiliar"), contrato.getString("moneda") , retencionP.getString("nombreRetencion"), 
            			fechaContable, dispatcher, organizationPartyId, obraId, contrato.getString("productId"),
            			new BigDecimal(montosRetenciones.get("montoRetencion"+r).toString()).setScale(2, BigDecimal.ROUND_HALF_UP), obra, delegator);
    				r++;
    			}
    		}
       		porcentajes = delegator.findByCondition("SeguimientoObra", condition, null,
    				null); 
    		
    		por = porcentajes.iterator();
    		
            BigDecimal porcen = porcentajeAvance;
            while(por.hasNext()){
            	GenericValue segumiento = por.next();
            	porcen = porcen.add(segumiento.getBigDecimal("porcentajeAvance"));
            }  	
    		
    		GenericValue cambioContrato = GenericValue.create(delegator
    				.getModelEntity("ContratoObra"));
    		cambioContrato.set("contratoId", contratoId);
    		if(porcen.intValue() == 100 || monCompara.compareTo(contrato.getBigDecimal("valorContratoConIva")) == 0
    				|| montoRestante.compareTo(BigDecimal.ZERO) == 0){
    			cambioContrato.set("statusId", "TERMINADO_C");
    			seguimiento.set("porcentajeAvance", (new BigDecimal(100).subtract(porcen).setScale(2, BigDecimal.ROUND_HALF_UP)).negate());
    			delegator.store(seguimiento);
    			porcentajes = delegator.findByCondition("SeguimientoObra", condition, null,
        				null);
    		}else{
    			cambioContrato.set("statusId", "EN_PROCESO_C");
    		}
    		delegator.create(cambioContrato);

    		if(!porcentajes.isEmpty()){
    			resultado.put("tipoMoneda", contrato.getString("moneda"));
        		resultado.put("seguimientoList", porcentajes);
    		}

			return resultado;
    		
    		
		} else if (performFind != null && "B".equals(performFind) || "C".equals(performFind)) {
			
			Debug.log("opcion B de obraId: " + obraId);
			
			if("C".equals(performFind)){
				if(obraId == null){
					error.add("Es necesario seleccionar la obra");
				}
				if(contratoId == null){
					error.add("Es necesario seleecionar el contrato de la obra que se quiere buscar");
				}
				if (!error.isEmpty()) {
					return ServiceUtil.returnError(error);
				}
			}

			GenericValue contrato = delegator.findByPrimaryKey("ContratoObra",
    				UtilMisc.toMap("contratoId", contratoId));
            Debug.log("contrato: " + contrato);
			
			EntityCondition condition = EntityCondition.makeCondition(EntityOperator.AND,
	    				EntityCondition.makeCondition("obraId", EntityOperator.EQUALS, obraId),
	    				EntityCondition.makeCondition("contratoId", EntityOperator.EQUALS, contratoId));
	            
	        List<GenericValue> porcentajes = delegator.findByCondition("SeguimientoObra", condition, null,
	    				null); 
			
			Debug.log("Se obtiene de obraId: " + porcentajes);

    		if(!porcentajes.isEmpty()){
    			resultado.put("tipoMoneda", contrato.getString("moneda"));
        		resultado.put("seguimientoList", porcentajes);
    		}

			return resultado;
		}

	    return resultado;
	}
	
	public static void crearOrdenPago(GenericValue userLogin, String avanceId, String contratistaId, String moneda,
			String description, Timestamp fechaContable, LocalDispatcher dispatcher,
			String organizationPartyId, String obraId, String productId, BigDecimal monto, 
			GenericValue obra, Delegator delegator) throws GenericServiceException, GenericEntityException {
		
		Debug.log("Monto: " + monto);
		String invoiceId = generaInvoice(userLogin, contratistaId,  
				moneda, description, fechaContable, dispatcher,
				organizationPartyId, obraId);
		
		Debug.log("invoiceId: " + invoiceId);
		
		generaInvoiceItem(userLogin, invoiceId, "0001", productId, monto, obra, dispatcher);
		
		GenericValue ordenPago = GenericValue.create(delegator
				.getModelEntity("OrdenPagoObra"));
		ordenPago.set("ordenPagoId", delegator.getNextSeqId("OrdenPagoObra"));
		ordenPago.set("avanceId", avanceId);
		ordenPago.set("monto", monto);
		ordenPago.set("invoiceId", invoiceId);
		delegator.create(ordenPago);
		
	}

	public static String generaInvoice(GenericValue userLogin, String proveedor,  
			String moneda, String description, Timestamp fechaContable, LocalDispatcher dispatcher,
			String organizationPartyId, String obraId) throws GenericServiceException{
		
		Debug.log("Entra e crear invoice");
		
		Map<String,Object> input = FastMap.newInstance();
		input.put("userLogin", userLogin);
		input.put("invoiceTypeId", "PURCHASE_INVOICE");
		input.put("statusId", "INVOICE_IN_PROCESS");
		input.put("partyId", organizationPartyId);
		input.put("partyIdFrom", proveedor);
		input.put("acctgTransTypeId", "EJE_PE");
		input.put("obraId", obraId);
		input.put("invoiceTypeId", "PURCHASE_INVOICE");
		input.put("invoiceDate", fechaContable);
		input.put("descriptionInvoice", description);
		input.put("currencyUomId", moneda);	
		input.put("statusId", "INVOICE_IN_PROCESS");

		Debug.log("createInvoice: " + input);
		
		Map<String,Object> resultInvoice = dispatcher.runSync("createInvoice", input);
		
		Debug.log("invoiceId: " + (String) resultInvoice.get("invoiceId"));
		return (String) resultInvoice.get("invoiceId");
		
	}
	
	public static void generaInvoiceItem(GenericValue userLogin, String invoiceId, String invoiceItemSeqId,
			String productId, BigDecimal monto, GenericValue obra, LocalDispatcher dispatcher) throws GenericServiceException{
		
		Debug.log("Entra e crear invoice");
		
		Map<String,Object> inputItem = FastMap.newInstance();
		inputItem.put("userLogin", userLogin);
		inputItem.put("invoiceId", invoiceId);
		inputItem.put("invoiceItemSeqId", invoiceItemSeqId);
		inputItem.put("productId", productId);
		inputItem.put("quantity", new BigDecimal(1));
		inputItem.put("amount", monto);
		inputItem.put("montoRestante", monto);		
		
		for (int i = 1; i < 16; i++) {
			String clasif = (String) obra.get("acctgTagEnumId" + i);
			if (clasif != null && !clasif.isEmpty()){
				inputItem.put("acctgTagEnumId" + i, clasif);
			}else if((String) obra.get("acctgTagEnumIdAdmin") != null 
					&& (String) obra.get("acctgTagEnumId" + (i+1)) != null){
				inputItem.put("acctgTagEnumId" + i, "");
				inputItem.put("acctgTagEnumIdAdmin", (String) obra.get("acctgTagEnumIdAdmin"));
			}
		}
		
		Debug.log("createInvoiceItem: " + inputItem);
		
		Map<String,Object> result = dispatcher.runSync("createInvoiceItem", inputItem);
		
		Debug.log("invoiceIdItem: " + result);

	}
	
	/**
	 * Metodo que busca elexternalId a traves de el partyId
	 * @param partyId
	 * @param delegator
	 * @return
	 * @throws GenericEntityException
	 */
    public static String buscaExternalId(String partyId, Delegator delegator) throws GenericEntityException {
			GenericValue party = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId",partyId));
			if(party == null)
				throw new GenericEntityException("No se encontro el party con id "+partyId);
		return party.getString("externalId");
	}
	
    /**
     *Metodo que obtiene el sequenceId a traces del enumId
     * @param enumId
     * @param delegator
     * @return
     * @throws GenericEntityException
     */
    public static String buscaSequenceId(String enumId, Delegator delegator) throws GenericEntityException {
		GenericValue enumeration = delegator.findByPrimaryKey("Enumeration", UtilMisc.toMap("enumId",enumId));
		if(enumeration == null)
			throw new GenericEntityException("No se encontro el enumeration con id "+enumId);
	return enumeration.getString("sequenceId");
	}
    
    public static GenericValue buscaSaldoProductoObra(Delegator delegator, String product,
			Timestamp periodo, String organizationPartyId) throws GenericEntityException {
		// Rango de periodos.
		Calendar date = Calendar.getInstance();
		date.setTime(periodo);
		date.set(Calendar.DAY_OF_MONTH, 1);
		date.set(Calendar.HOUR, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		date.set(Calendar.AM_PM, Calendar.AM);
		periodo = new Timestamp(date.getTimeInMillis());
		
		List<GenericValue> generics = delegator.findByAnd("SaldoCatalogo", "catalogoId", product, "tipo", "P", "periodo", periodo);
		GenericValue catalogo;
		if(generics.isEmpty()){
			// Si esta vacio se crea el registro trayendo la info de un mes almacenado.			
			catalogo = delegator.makeValue("SaldoCatalogo");				
			catalogo.set("secuenciaId", delegator.getNextSeqId("SaldoCatalogo"));
			catalogo.set("catalogoId", product);
			catalogo.set("tipo", "P");
			catalogo.set("tipoId", "CONTRATO");
			catalogo.set("partyId", organizationPartyId);
			catalogo.set("periodo", periodo);
			catalogo.set("monto", BigDecimal.ZERO);						
		}else{
			catalogo=generics.get(0);
		}
		return catalogo;
	}
}
