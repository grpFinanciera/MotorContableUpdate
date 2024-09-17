package com.opensourcestrategies.financials.util;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.json.JSONObject;

import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.LineaMotorContable;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.financials.motor.MotorContable;

public class UtilMotorContable {
	
	public static Map<String, String> mapaClassParent = null;
	public static Map<String, String> mapaMomentos = null;
	
	/**
	 * Obtiene un mapa con la naturaleza de las cuentas
	 * @param cuentas
	 * @param delegator
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map<String,String> getMapaCuentaNaturaleza(List<String> cuentas, Delegator delegator) throws GenericEntityException{
		
		//Se obtienen las clases para saber la naturaleza
		getClasses(delegator);
		//Se obtienen los momentos contables
		getMomentos(delegator);
		
		Map<String,String> mapaResultado= FastMap.newInstance();
		
		EntityCondition condicion = EntityCondition.makeCondition("glAccountId", EntityOperator.IN, cuentas);
		
		List<GenericValue> listaCuentas = delegator.findByAnd("GlAccount", condicion);
		for (GenericValue cuenta : listaCuentas) {
			String clase = cuenta.getString("glAccountClassId");
			while(mapaClassParent.containsKey(clase)){
				if(mapaClassParent.get(clase) == null){
					break;
				} else {
					clase = mapaClassParent.get(clase);
				}
			}
			mapaResultado.put(cuenta.getString("glAccountId"), clase);
		}
		
		return mapaResultado;
	}
	
	/**
	 * Obtiene y guarda el mapa de clases de las cuentas , solo si no existe 
	 * @param delegator
	 * @throws GenericEntityException
	 */
	public static void getClasses(Delegator delegator) throws GenericEntityException{
			mapaClassParent = FastMap.newInstance();
			List<GenericValue> classes = delegator.findAllCache("GlAccountClass");
			if(classes.isEmpty()){
				classes = delegator.findAll("GlAccountClass");
			}
			for (GenericValue clase : classes) {
				mapaClassParent.put(clase.getString("glAccountClassId"),clase.getString("parentClassId"));
			}
	}
	
	/**
	 * Obtiene y guarda el mapa de momentos contables , solo si no existe 
	 * @param delegator
	 * @throws GenericEntityException
	 */
	public static void getMomentos(Delegator delegator) throws GenericEntityException{
			mapaMomentos = FastMap.newInstance();
			List<GenericValue> momentos = delegator.findAllCache("Momento");
			if(momentos.isEmpty()){
				momentos = delegator.findAll("Momento");
			}
			for (GenericValue clase : momentos) {
				mapaMomentos.put(clase.getString("cuentaAsociada"),clase.getString("momentoId"));
			}
	}	
	
	
	public static Map creaTransaccion(DispatchContext dctx, Map context)
			throws GenericEntityException {
		Delegator delegator = dctx.getDelegator();
		
		Timestamp fechaRegistro = (Timestamp) context.get("fechaRegistro");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		String eventoContableId = (String) context.get("eventoContableId");
		String usuario = (String) context.get("usuario");
		String organizationId = (String) context.get("organizationId");
		String currency = (String) context.get("currency");
		String tipoClaveId = (String) context.get("tipoClaveId");
		String roleTypeId = (String) context.get("roleTypeId");
		String descripcion = (String) context.get("descripcion");
		List <LineaMotorContable> lineasMotor = (List <LineaMotorContable>) context.get("lineasMotor"); 
		String campo = (String) context.get("campo");
		String valorCampo = (String) context.get("valorCampo");
		String tipoMovimiento = (String) context.get("tipoMovimiento");
		
		try {
			//Mandamos a llamar al metodo del motorContable.
			GenericValue transaccion = MotorContable.creaTransaccion(delegator,
					fechaRegistro, fechaContable, eventoContableId, usuario,
					organizationId, currency, tipoClaveId,
					roleTypeId, descripcion, lineasMotor, campo, valorCampo,
					tipoMovimiento);
			Map result = ServiceUtil.returnSuccess();
			result.put("transaccion", transaccion);
			return result;
			
		} catch (GeneralException e) {
			return ServiceUtil.returnError(e.toString());
		}
	}
	
	public static Map creaTransaccionCierreCont(DispatchContext dctx, Map context)
			throws GenericEntityException {
		Delegator delegator = dctx.getDelegator();
		
		List<Map<String, Object>> lineasContables = FastList.newInstance();
		lineasContables = (List<Map<String, Object>>) context.get("lineasContables");
		
		try {
			//Mandamos a llamar al metodo del motorContable.
			GenericValue transaccion = MotorContable.creaTransaccionCierreContable(
					delegator, lineasContables);
			Map result = ServiceUtil.returnSuccess();
			result.put("transaccion", transaccion);
			return result;
			
		} catch (GeneralException e) {
			return ServiceUtil.returnError(e.toString());
		}
	}
	
    /**
     * Metodo que obtiene lineas contables de un Evento (Ajax)
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     */
    public static String getLineasMotor(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException{
    	Map<String, Object> resultado = new HashMap<String, Object>();
    	
    	Delegator delegator = (Delegator) request.getAttribute("delegator");
    	String acctgTransTypeId = (String) request.getParameter("acctgTransTypeId");
    	String mostrarTodos = (String) request.getParameter("mostrarTodos");
    	mostrarTodos = (mostrarTodos == null ? "N" : mostrarTodos);
    		
    	LinkedList<GenericValue> listaLineas = new LinkedList<GenericValue>();
    		
    	EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
    					EntityCondition.makeCondition("acctgTransTypeId",EntityOperator.EQUALS,acctgTransTypeId),
    					EntityCondition.makeCondition("tipoMatrizId",EntityOperator.NOT_EQUAL,null));
    	List<GenericValue> lineasPresupuestales = delegator.findByAnd("LineaPresupuestal", condiciones); 
    	Collections.sort(lineasPresupuestales,new ComparadorLinea());
    	if(lineasPresupuestales.size() > 1 || mostrarTodos.equals("Y")){
        	for (GenericValue lineaPres : lineasPresupuestales) {
        		GenericValue cargoCat = lineaPres.getRelatedOne("cargoTipoAuxiliar");
        		GenericValue abonoCat = lineaPres.getRelatedOne("abonoTipoAuxiliar");
        		if(cargoCat != null || abonoCat != null){
        			if(cargoCat != null){
        				lineaPres.set("catalogoCargo",cargoCat);
        			}
        			if(abonoCat != null){
        				lineaPres.set("catalogoAbono",abonoCat);
        			}
        			listaLineas.add(lineaPres);
        		}
    		}
    	}
    	
    	List<GenericValue> lineasContables = delegator.findByAnd("LineaContable", UtilMisc.toMap("acctgTransTypeId",acctgTransTypeId)); 
    	Collections.sort(lineasPresupuestales,new ComparadorLinea());
    	if(listaLineas.isEmpty() && lineasContables.isEmpty()){
    		resultado.put("mensaje", "ERROR");
    	} else {
    		for (GenericValue lineaCont : lineasContables) {
        		GenericValue cargoCat = lineaCont.getRelatedOne("cargoTipoAuxiliar");
        		GenericValue abonoCat = lineaCont.getRelatedOne("abonoTipoAuxiliar");
				if(cargoCat != null){
					lineaCont.set("catalogoCargo",cargoCat);
				}
				if(abonoCat != null){
					lineaCont.set("catalogoAbono",abonoCat);
				}
				listaLineas.add(lineaCont);
			}
    		resultado.put("lista", listaLineas);
    	}
    	
    	return doJSONResponse(response, resultado);
    }
    
	private static class ComparadorLinea implements Comparator<GenericValue> {

		public int compare(GenericValue linea0, GenericValue linea1) {
			return linea0.getLong("secuencia").compareTo(linea1.getLong("secuencia"));
		}
		
	}
    
    public static Map agregaEventoTransaccion(DispatchContext dctx, Map context)
			throws GenericEntityException {
		Delegator delegator = dctx.getDelegator();
		
		String eventoContableId = (String) context.get("eventoContableId");
		String tipoClaveId = (String) context.get("tipoClaveId");
		GenericValue trans = (GenericValue) context.get("transaccion");
		List <LineaMotorContable> lineasMotor = (List <LineaMotorContable>) context.get("lineasMotor"); 
		
		try {
			//Mandamos a llamar al metodo del motorContable.
			GenericValue transaccion = MotorContable.agregaEventoTransaccion(
					delegator, trans, eventoContableId, lineasMotor,
					tipoClaveId, trans.getString("createdByUserLogin"));
			Map result = ServiceUtil.returnSuccess();
			result.put("transaccion", transaccion);
			return result;
			
		} catch (GeneralException e) {
			return ServiceUtil.returnError(e.toString());
		}
	}
	
	public static Map reversaTransaccion(DispatchContext dctx, Map context)
			throws GenericEntityException {
		Delegator delegator = dctx.getDelegator();
		
		String transaccionId = (String) context.get("acctgTransId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String usuario = userLogin.getString("userLoginId");

		try {
			// Mandamos a llamar al metodo del motorContable.
			transaccionId = MotorContable.reversaTransaccion(
					delegator, transaccionId, usuario);
			Map result = ServiceUtil.returnSuccess();
			result.put("acctgTransId", transaccionId);
			return result;
		} catch (GeneralException e) {
			return ServiceUtil.returnError(e.toString());
		}
	}
	
	public static Map generaNumeroPoliza(DispatchContext dctx, Map context)
			throws GenericEntityException {
		Delegator delegator = dctx.getDelegator();
		
		String eventoContableId = (String) context.get("eventoContableId");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		GenericValue EventoContable = delegator.findByPrimaryKey("EventoContable", UtilMisc.toMap("acctgTransTypeId",eventoContableId));

		try {
			String poliza = MotorContable.generaNumeroPoliza(delegator, EventoContable, fechaContable, delegator.makeValue("AcctgTrans"),eventoContableId);
			Map result = ServiceUtil.returnSuccess();
			result.put("poliza", poliza);
			return result;
		} catch (GeneralException e) {
			return ServiceUtil.returnError(e.toString());
		}
	}
	
	public static Map validaSuficienciaPresupuestal(DispatchContext dctx, Map context)
			throws GenericEntityException {
		Delegator delegator = dctx.getDelegator();
		
		String eventoContableId = (String) context.get("eventoContableId");
		String clave = (String) context.get("clave");
		BigDecimal monto = (BigDecimal) context.get("monto");
		Timestamp fecha = (Timestamp) context.get("fecha");
		
		try {
			String mensaje = MotorContable.validaSuficienciaPresupuestal(delegator, eventoContableId, clave, fecha, monto);
			return mensaje.isEmpty() ? ServiceUtil.returnSuccess():ServiceUtil.returnError(mensaje);			
		} catch (GeneralException e) {
			return ServiceUtil.returnError(e.toString());
		}
	}
	
	public static Map existenciaClave(DispatchContext dctx, Map context)
			throws GenericEntityException {
		Delegator delegator = dctx.getDelegator();
		
		String clavePresupuestal = (String) context.get("clavePresupuestal"); 
		
		try {
			//Mandamos a llamar al metodo del motorContable.
			
			String existencia = MotorContable.existenciaClave(delegator, clavePresupuestal)?"Si":"No";
			Map result = ServiceUtil.returnSuccess();
			result.put("existencia", existencia);
			return result;
			
		} catch (GeneralException e) {
			return ServiceUtil.returnError(e.toString());
		}
	}
	
	public static Map getNaturalezaCuentas(DispatchContext dctx, Map context)
			throws GenericEntityException {
		Delegator delegator = dctx.getDelegator();
		
		try {
			//Mandamos a llamar al metodo del motorContable.
			
			Map<String, String> naturalezaCuentas = MotorContable.getNaturalezaCuentas(delegator);
			Map result = ServiceUtil.returnSuccess();
			result.put("naturalezaCuentas", naturalezaCuentas);
			return result;
			
		} catch (GeneralException e) {
			return ServiceUtil.returnError(e.toString());
		}
	}
	
    /**
     * Using common method to return json response.
     */
    private static String doJSONResponse(HttpServletResponse response, Map map) {
        return org.opentaps.common.event.AjaxEvents.doJSONResponse(response, JSONObject.fromObject(map));
    }

}
