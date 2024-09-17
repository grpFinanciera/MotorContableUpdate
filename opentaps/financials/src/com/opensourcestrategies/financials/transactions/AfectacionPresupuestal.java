package com.opensourcestrategies.financials.transactions;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilNumber;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.LineaMotorContable;
import org.ofbiz.order.finaccount.UtilClavePresupuestal;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.base.entities.OperacionPatrimonial;
import org.opentaps.common.util.UtilAccountingTags;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.action.ActionContext;
import org.opentaps.foundation.repository.RepositoryException;

import com.ibm.icu.util.Calendar;


public class AfectacionPresupuestal extends MotorContableFinanzas{

	@SuppressWarnings({ "rawtypes" })
	public static Map creaAfectacion(DispatchContext dctx, Map context) throws Exception {
		
		Debug.log("Ingresa a afectaci\u00f3n");
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
		List<String> error = new ArrayList<String>();
		
		// obtenemos informacion de pantalla
		String organizationPartyId = (String) context
				.get("organizationPartyId"), performFind = (String) context
				.get("performFind"), agrupadorP = null;
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		Map result = ServiceUtil.returnSuccess();
		

		if ("Y".equals(performFind)) {
			
			Debug.log("Entra a crear informacion generica");
			
			cambiaStatus(delegator, userLogin.getString("userLoginId"));
			
			// obtenemos informacion de pantalla
			String descripcion = (String) context
					.get("description");
			Timestamp fechaContable = (Timestamp) context
					.get("fechaContable");
			String tipoMovimiento = (String) context
					.get("tipoMovimiento");
			
			if(descripcion == null){
				error.add("Debe ingresar la descripci\u00f3n de la operaci\u00f3n");
			}
			if(fechaContable == null){
				error.add("Debe ingresar la fecha contable de la operaci\u00f3n");
			}
			if(tipoMovimiento == null){
				error.add("Debe ingresar el tipo de adecuaci\u00f3n");
			}
			if(!error.isEmpty()) {
				return ServiceUtil.returnError(error);
            }

			GenericValue afectacionP = GenericValue.create(delegator
					.getModelEntity("AfectacionCompensada"));
			agrupadorP = delegator.getNextSeqId("AfectacionCompensada");
			afectacionP.set("agrupadorP", agrupadorP);
			afectacionP.set("fechaTransaccion", UtilDateTime.nowTimestamp());
			afectacionP.set("fechaContable", fechaContable);
			afectacionP.set("estatus", "N");
			afectacionP.set("organizationPartyId", organizationPartyId);
			afectacionP.set("userLoginId", userLogin.getString("userLoginId"));
			afectacionP.set("description", descripcion);
			afectacionP.set("tipoMovimiento", tipoMovimiento);
			delegator.create(afectacionP);
			
			GenericValue datos = delegator.findByPrimaryKey("AfectacionCompensada", 
					UtilMisc.toMap("agrupadorP", agrupadorP));
			
			result.put("datos", datos);
			
			Debug.log("Datos: " + datos);
			
			return result;
			
		} else if ("B".equals(performFind)) {
			
			Debug.log("Ingresa a guardar claves presupuestales");
			
			Calendar cal = Calendar.getInstance();
			
			String periodoA = (String) context.get("perdiodoIA"),
					periodoR = (String) context.get("perdiodoIR"),
					moneda = (String) context.get("currency"),
					tipoClave = (String) context.get("tipoClave"),
					ciclo = Integer.toString(cal.get(Calendar.YEAR)),
					clavePresupuestalA = "", clavePresupuestalR = "";
			
			agrupadorP = (String) context.get("agrupadorP");
		    BigDecimal amount = (BigDecimal) context.get("monto");
		    
		    cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.YEAR, Integer.parseInt(ciclo));		    
		    cal.set(Calendar.MONTH, (Integer.parseInt(periodoA)-1));
		    Timestamp fechaA = new Timestamp(cal.getTimeInMillis());
		    cal.set(Calendar.MONTH, (Integer.parseInt(periodoR)-1));
		    Timestamp fechaR = new Timestamp(cal.getTimeInMillis());

		    List<String> claveAmpliacion = new ArrayList<String>(), 
					claveReduccion = new ArrayList<String>();
			

	        for(int i=1; i<16; i++){
	        	String clasif = (String) context.get("clasificacion" + i);
	        	if(clasif != null && !clasif.isEmpty()) 
	        		claveAmpliacion.add(clasif);
	        }	
				
	        for(int i=1; i<16; i++){
	        	String clasif = (String) context.get("clasif" + i);
	        	if(clasif != null && !clasif.isEmpty()) 
	        		claveReduccion.add(clasif);
	        }
	        
	        // validamos datos requeridos
	        List<String> obligatorios = validaObligatoriosAfectacion(delegator,
	     					periodoA, periodoR, moneda, amount);
	        if (!obligatorios.isEmpty()){
	            Iterator<String> avisos = obligatorios.iterator();
	            while(avisos.hasNext()){
	                 error.add(avisos.next());
	            }
	        }
	                 
	        if(!error.isEmpty()) {
	            return ServiceUtil.returnError(error); 
	        }
	        
			for (String cve : claveAmpliacion) {
	        	clavePresupuestalA += cve.trim();
			}
	        
	        for (String cve : claveReduccion) {
	        	clavePresupuestalR += cve.trim();
			}

	        GenericValue cvePrespuA = obtenerCvePresupuestal(delegator, clavePresupuestalA);
	        if(cvePrespuA == null || cvePrespuA.isEmpty())
	        	error.add("No existe la clave presupuestal de ampliaci\u00f3n");
	        
	        GenericValue cvePrespuR = obtenerCvePresupuestal(delegator, clavePresupuestalR);
	        if(cvePrespuR == null || cvePrespuR.isEmpty())
	        	error.add("No existe la clave presupuestal de reducci\u00f3n");
	        
	        if(!error.isEmpty()) {
            	return ServiceUtil.returnError(error); 
            }
			
	        guardaDetalle(delegator, agrupadorP, fechaR, amount, clavePresupuestalR, "R", moneda);	
	        guardaDetalle(delegator, agrupadorP, fechaA, amount, clavePresupuestalA, "A", moneda);
	        
	        GenericValue datos = delegator.findByPrimaryKey("AfectacionCompensada", 
					UtilMisc.toMap("agrupadorP", agrupadorP));
	        
	        List<String> orderBy = org.ofbiz.base.util.UtilMisc.toList("agrupadorD");
	        
	        EntityCondition condition = EntityCondition.makeCondition(EntityOperator.AND, 
					  EntityCondition.makeCondition("agrupadorP", EntityOperator.EQUALS, agrupadorP));
	        
	        List<GenericValue> listDetalle = delegator.findByCondition("AfectacionCompensadaDetalle", condition, null,
	        		orderBy); 
			
			result.put("datos", datos);
			result.put("listDetalle", listDetalle);
			
			Debug.log("Datos: " + datos);
			Debug.log("listDetalle: " + listDetalle);
			
			return result;
		}else if ("C".equals(performFind)) {
			agrupadorP = (String) context.get("agrupadorP");
			String tipoClave = (String) context.get("tipoClave");
			
			Debug.log("Agrupador: " + agrupadorP);
			
			GenericValue datos = delegator.findByPrimaryKey("AfectacionCompensada", 
					UtilMisc.toMap("agrupadorP", agrupadorP));
			
			Debug.log("Mike datos: " + datos);
	        
	        Map<String, Object> input = FastMap.newInstance();
	        if(tipoClave.equals("EGRESO")){
	        	input.put("eventoContableId", "PEAD-01");
	        }else{
	        	input.put("eventoContableId", "LIAD-01");
	        }
	        input.put("tipoClaveId", tipoClave);
	        input.put("fechaRegistro", datos.getTimestamp("fechaTransaccion"));
	        input.put("fechaContable", datos.getTimestamp("fechaContable"));
	        input.put("currency", "MXN");
	        input.put("usuario", userLogin.getString("userLoginId"));
	        input.put("organizationId", datos.getString("organizationPartyId"));
	        input.put("descripcion", datos.getString("description"));
	        input.put("tipoMovimiento", datos.getString("tipoMovimiento"));
	        
	        Debug.log("Mike input: " + input);

	        Map<String, Object> output = FastMap.newInstance();
	        List<LineaMotorContable> listLineaContable = FastList.newInstance();
	        listLineaContable = obtenerLineaMotor(delegator, agrupadorP, "R");
	        
	        try {
		        
		        input.put("lineasMotor", listLineaContable);
	        	output = dispatcher.runSync("creaTransaccionMotor", input);
	        	GenericValue transaccion = (GenericValue) output.get("transaccion");

	        	Debug.log("transaccion: " + transaccion);

	        	if(ServiceUtil.isError(output)){
	        		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
	        	}
	        	
	        	input.clear();
	        	listLineaContable.clear();
	        	output.clear();
	        	listLineaContable = obtenerLineaMotor(delegator, agrupadorP, "A");
	        	if(tipoClave.equals("EGRESO")){
		        	input.put("eventoContableId", "PEAD-02");
		        }else{
		        	input.put("eventoContableId", "LIAD-02");
		        }
		        input.put("tipoClaveId", tipoClave);
		        input.put("lineasMotor", listLineaContable);
		        input.put("transaccion", transaccion);
		        output = dispatcher.runSync("agregaEventoTransaccion", input);
		        
		        result.put("acctgTransId", transaccion.getString("acctgTransId"));
		        result.put("datos", datos);
		        
		        String transId = transaccion.getString("acctgTransId");
		        Debug.log("Idddddddd: " + transId);
		        
		        //Actualizamos la tabla de afectacion.
		        List<GenericValue> detalles = delegator.findByAnd("AfectacionCompensadaDetalle", "agrupadorP", agrupadorP);
		        for(GenericValue detalle : detalles){
		        	detalle.set("acctgTransId", transId);
		        	delegator.store(detalle);
		        }
		        
		        
		        return result;
		        
			} catch (GenericServiceException e) {
				return ServiceUtil.returnError(e.getMessage());
			}	

		} 
		return result;
	}

	public static void guardaDetalle(Delegator delegator, String agrupadorP,
			Timestamp fecha, BigDecimal monto, String clavePresupuestal,
			String flag, String moneda) throws GenericEntityException{
		
		GenericValue afectacionD = GenericValue.create(delegator
				.getModelEntity("AfectacionCompensadaDetalle"));
		afectacionD.set("agrupadorD", delegator.getNextSeqId("AfectacionCompensadaDetalle"));
		afectacionD.set("agrupadorP", agrupadorP);
		afectacionD.set("fecha", fecha);
		afectacionD.set("monto", monto);
		afectacionD.set("clavePresupuestal", clavePresupuestal);
		afectacionD.set("flag", flag);
		afectacionD.set("tipoMoneda", moneda);
		delegator.create(afectacionD);
	}
	
	public static void cambiaStatus(Delegator delegator, String userLoginId)
			throws GenericEntityException, RepositoryException {

		Debug.log("Ingresa a cambiar el estatus");
		
		EntityCondition condition = EntityCondition.makeCondition(EntityOperator.AND, 
				  EntityCondition.makeCondition("estatus", EntityOperator.EQUALS, "N"),
				  EntityCondition.makeCondition("userLoginId", EntityOperator.EQUALS, userLoginId));
		
		List<String> orderBy = org.ofbiz.base.util.UtilMisc.toList("agrupadorP");

		List<GenericValue> listDetalle = delegator.findByCondition("AfectacionCompensada", condition, null,
        		orderBy);

		if (listDetalle.size() != 0) {
			GenericValue operacionP = actualizaAfectacion(delegator,
					listDetalle.get(0).getString("agrupadorP"));
			delegator.store(operacionP);
		}
	}
	
	public static GenericValue actualizaAfectacion(Delegator delegator,
			String agrupadorP) {
		
		Debug.log("Ingresa a actualizar la afectacion");
		
		GenericValue operacionP = GenericValue.create(delegator
				.getModelEntity("AfectacionCompensada"));
		operacionP.set("agrupadorP",
				agrupadorP);
		operacionP.set("estatus", "Y");

		return operacionP;
	}
	
	public static List<LineaMotorContable> obtenerLineaMotor(Delegator delegator, String agrupadorP, String flag) throws GenericEntityException{
		
		List<String> orderBy = org.ofbiz.base.util.UtilMisc.toList("agrupadorD");
        
        EntityCondition condition = EntityCondition.makeCondition(EntityOperator.AND, 
				  EntityCondition.makeCondition("agrupadorP", EntityOperator.EQUALS, agrupadorP),
				  EntityCondition.makeCondition("flag", EntityOperator.EQUALS, flag));
        
        List<GenericValue> listDetalle = delegator.findByCondition("AfectacionCompensadaDetalle", condition, null,
        		orderBy);
        
        Iterator itemIter = listDetalle.iterator();
        
        List<LineaMotorContable> listLineaContable = FastList.newInstance();
        
        while (itemIter.hasNext()) {
			GenericValue clave = (GenericValue) itemIter.next();
			LineaMotorContable lineaContable = new LineaMotorContable();
			
			lineaContable.setClavePresupuestal(clave.getString("clavePresupuestal"));
			lineaContable.setMontoPresupuestal(clave.getBigDecimal("monto"));
			lineaContable.setFecha(clave.getTimestamp("fecha"));
				
			listLineaContable.add(lineaContable);
				
		}
		
        return listLineaContable;
	}
	
}