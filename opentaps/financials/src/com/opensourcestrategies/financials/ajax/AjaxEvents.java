package com.opensourcestrategies.financials.ajax;

import java.sql.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ofbiz.accounting.util.UtilAccounting;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.order.finaccount.UtilClavePresupuestal;
import org.opentaps.base.entities.CuentasBanco;
import org.opentaps.base.entities.CuentasPorAsignacionCuentaUsuario;
import org.opentaps.base.entities.PartyCuentaBancaria;
import org.opentaps.base.entities.ReporteFirmante;

import com.opensourcestrategies.financials.transactions.ControladorAfectacion;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import net.sf.json.JSONObject;


/**
 * Ajax events to be invoked by the controller.
 *
 * @author Salvador Cortes
 */
public class AjaxEvents {

    public static final String module = AjaxEvents.class.getName();
    
    public static String obtenDatosLookupAgrupador(HttpServletRequest request, HttpServletResponse response) {
    	Delegator delegator = (Delegator) request.getAttribute("delegator");

        String idTipoDoc = (String) request.getParameter("idTipoDoc");
        
        try {
			GenericValue tipoDoc = delegator.findByPrimaryKey("TipoDocumento", UtilMisc.toMap("idTipoDoc",idTipoDoc));
			String tipoPolizaId = UtilAccounting.buscarIdPolizaXDocu(idTipoDoc, delegator);
			Map<String,Object> resultado = FastMap.newInstance();
			resultado.put("momentoId1", tipoDoc.getString("mComparativo"));
			resultado.put("tipoPolizaId", tipoPolizaId);
			return org.opentaps.common.event.AjaxEvents.doJSONResponse(response, resultado);
	
        } catch (GenericEntityException e) {
            return org.opentaps.common.event.AjaxEvents.doJSONResponse(response, FastList.newInstance());
        }
    }
	
	/** Return the objects which related with product in receive inventory form.
     * @throws GenericEntityException
     */
    public static String obtieneCuentasBancarias(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
		try
		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
			String bancoId = (String) request.getParameter("bancoId");
			String organizationPartyId = (String) request.getParameter("organizationPartyId");
			
			List<String> orderBy = org.ofbiz.base.util.UtilMisc.toList("numeroCuenta");			
			
			EntityCondition condicionBan;	        
			condicionBan = EntityCondition.makeCondition(EntityOperator.AND,
                EntityCondition.makeCondition(CuentasBanco.Fields.bancoId.name(), EntityOperator.EQUALS, bancoId),
                EntityCondition.makeCondition(CuentasBanco.Fields.partyId.name(), EntityOperator.EQUALS, organizationPartyId),
                EntityCondition.makeCondition(CuentasBanco.Fields.habilitada.name(), EntityOperator.EQUALS, "S"));
			
			
			List<GenericValue> cuentas = delegator.findByCondition("CuentasBanco", condicionBan , UtilMisc.toList("numeroCuenta", "nombreCuenta", "cuentaBancariaId", "bancoId"), orderBy);
			Map<String, Object> resultado = new HashMap<String, Object>();
			
			
			if (!cuentas.isEmpty()) 
			{	Iterator<GenericValue> cuentasIter = cuentas.iterator();
				while (cuentasIter.hasNext()) 
				{	GenericValue cuentasListaTrans = cuentasIter.next();
					resultado.put(cuentasListaTrans.getString("cuentaBancariaId"), cuentasListaTrans.getString("numeroCuenta")+" - "+cuentasListaTrans.getString("nombreCuenta"));										
				}
				return doJSONResponse(response, resultado);
			}
			else			
			{	resultado.put("cuentaBancariaId", "ERROR");
				return doJSONResponse(response, resultado);
			}
		}
		catch(GeneralException e)
		{	return org.opentaps.common.event.AjaxEvents.doJSONResponse(response, FastList.newInstance());
		}
	}
    
    /** Obtener cuentas bancarias que tiene asignadas un usuario.
     * @throws GenericEntityException
     */
    public static String obtieneCuentasBancariasAsignadas(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
		try
		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
			String bancoId = (String) request.getParameter("bancoId");
			String partyId = (String) request.getParameter("partyId");
			
			List<String> orderBy = org.ofbiz.base.util.UtilMisc.toList("numeroCuenta");			
			
			EntityCondition condicionBan;	        
			condicionBan = EntityCondition.makeCondition(EntityOperator.AND,
                EntityCondition.makeCondition(CuentasPorAsignacionCuentaUsuario.Fields.bancoId.name(), EntityOperator.EQUALS, bancoId),
                EntityCondition.makeCondition(CuentasPorAsignacionCuentaUsuario.Fields.partyId.name(), EntityOperator.EQUALS, partyId));
			
			
			List<GenericValue> cuentas = delegator.findByCondition("CuentasPorAsignacionCuentaUsuario", condicionBan , UtilMisc.toList("numeroCuenta", "nombreCuenta", "cuentaBancariaId", "bancoId"), orderBy);
			Map<String, Object> resultado = new HashMap<String, Object>();
			
			
			if (!cuentas.isEmpty()) 
			{	Iterator<GenericValue> cuentasIter = cuentas.iterator();
				while (cuentasIter.hasNext()) 
				{	GenericValue cuentasListaTrans = cuentasIter.next();
					resultado.put(cuentasListaTrans.getString("cuentaBancariaId"), cuentasListaTrans.getString("numeroCuenta")+" - "+cuentasListaTrans.getString("nombreCuenta"));										
				}
				return doJSONResponse(response, resultado);
			}
			else			
			{	resultado.put("cuentaBancariaId", "ERROR");
				return doJSONResponse(response, resultado);
			}
		}
		catch(GeneralException e)
		{	return org.opentaps.common.event.AjaxEvents.doJSONResponse(response, FastList.newInstance());
		}
	}
    
    /**
     * Valida si el evento tiene catalogo de banco
     * @param request
     * @param response
     * @return
     */
	public static String validaCatalogoBanco(HttpServletRequest request, HttpServletResponse response) {
		try{
			Debug.log("Entra a validaModuloTesoreria");
			Delegator delegator = (Delegator) request.getAttribute("delegator");
			String acctgTransTypeId = (String) request.getParameter("acctgTransTypeId");
			
			Map<String,Object> resultado = FastMap.newInstance();
			
			if(ControladorAfectacion.tieneCatalogoBanco(delegator, acctgTransTypeId)){
				resultado.put("modulo", "1");
			} else {
				resultado.put("modulo", "");
			}

			return org.opentaps.common.event.AjaxEvents.doJSONResponse(response, resultado);
			
		}catch(GeneralException e){
			return org.opentaps.common.event.AjaxEvents.doJSONResponse(response, FastList.newInstance());
		}
	}   
	
	/**
	 * Obtiene los eventos de afectacion dependiendo del tipo de poliza
	 * @param request
	 * @param response
	 * @return
	 */
	public static String getEventosAfectacion(HttpServletRequest request, HttpServletResponse response){
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String moduloId = (String) request.getParameter("moduloId");
		
		Map<String,Object> resultado = FastMap.newInstance();
		
		EntityCondition condicionesPoliza = EntityCondition.makeCondition(UtilMisc.toList(
                EntityCondition.makeCondition("moduloId",EntityOperator.EQUALS,moduloId)), EntityOperator.AND);
		
		List<GenericValue> listEvento;
		try {
			List<String> fieldsToSelect = UtilMisc.toList("acctgTransTypeId", "descripcion");
			List<String> orderBy = UtilMisc.toList("descripcion");
			listEvento = delegator.findByCondition("EventoContable", condicionesPoliza, fieldsToSelect, orderBy);
			
			if(listEvento.isEmpty()){
				resultado.put("resultado", "ERROR");
			} else {
				for (GenericValue evento : listEvento) {
					resultado.put(evento.getString("acctgTransTypeId"), evento.getString("descripcion"));
				}
			}
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
			resultado.put("resultado", "ERROR");
			return org.opentaps.common.event.AjaxEvents.doJSONResponse(response, resultado);
		} 
		
		return org.opentaps.common.event.AjaxEvents.doJSONResponse(response, resultado);
	}
    
    /**
     * Using common method to return json response.
     */
    private static String doJSONResponse(HttpServletResponse response, Map<String,Object> map) {
        return org.opentaps.common.event.AjaxEvents.doJSONResponse(response, JSONObject.fromObject(map));
    }
    
    
    /*
     * Elimina firmantes para un reporte
     */
    public static String eliminarFirmante(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
		Map<String, Object> resultado = new HashMap<String, Object>();
		try
		{	
			Debug.log("Entra a eliminar firmantes");
			
			Delegator delegator = (Delegator) request.getAttribute("delegator");
		
			String reporteId = (String) request.getParameter("reporteId");
    		String firmanteId = (String) request.getParameter("firmanteId");	
    		
    		Debug.log("reporteId: "+ reporteId + "\nfirmanteId: " + firmanteId);
    		
    		//Valida si exista ese producto
			EntityCondition condicion;	        
			condicion = EntityCondition.makeCondition(EntityCondition.makeCondition(ReporteFirmante.Fields.reporteId.name(), EntityOperator.EQUALS, reporteId),
													  EntityCondition.makeCondition(ReporteFirmante.Fields.firmanteId.name(), EntityOperator.EQUALS, firmanteId));    			    			
			List<GenericValue> firmantesList = delegator.findByCondition("ReporteFirmante", condicion , null, null);
			if(!firmantesList.isEmpty())
			{	delegator.removeByCondition("ReporteFirmante", condicion);
				resultado.put("mensaje", "SUCCESS");
				return doJSONResponse(response, resultado);
			}
			else
			{	resultado.put("mensaje", "ERROR");
				return doJSONResponse(response, resultado);
    		}			
		}
		catch(GeneralException e)
		{	resultado.put("mensaje", "ERROR");
			return doJSONResponse(response, resultado);
		}
	}
    
    public static String eliminaPermisoCuentaBancaria(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
		Map<String, Object> resultado = new HashMap<String, Object>();
		try
		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
		
			String partyId = (String) request.getParameter("partyId");
    		String cuentaBancariaId = (String) request.getParameter("cuentaBancariaId");    		
    		
    		//Actualiza el monto y la cantidad en el detalle de la solicitud
    		GenericValue generic = delegator.findByPrimaryKey("PartyCuentaBancaria", UtilMisc.toMap("partyId", partyId, "cuentaBancariaId", cuentaBancariaId));
    		generic.remove();						
			
			//Valida si se elimino
			EntityCondition condicionElimina;	        
			condicionElimina = EntityCondition.makeCondition(EntityCondition.makeCondition(PartyCuentaBancaria.Fields.partyId.name(), EntityOperator.EQUALS, partyId),
															 EntityCondition.makeCondition(PartyCuentaBancaria.Fields.cuentaBancariaId.name(), EntityOperator.EQUALS, cuentaBancariaId));    			    			
			List<GenericValue> eliminaList = delegator.findByCondition("PartyCuentaBancaria", condicionElimina , null, null);
			
			if(eliminaList.isEmpty())
			{	resultado.put("mensaje", "SUCCESS");
				return doJSONResponse(response, resultado);
			}
			else
			{	resultado.put("mensaje", "ERROR");
				return doJSONResponse(response, resultado);
			}
		}
		catch(GeneralException e)
		{	resultado.put("mensaje", "ERROR");
			return doJSONResponse(response, resultado);
		}
	}
    
	/**
	 * Actualiza el catalogo de clasificaciones Enumerarion
	 * @param request
	 * @param response
	 * @return
	 */
	public static String actualizaClasificacion(HttpServletRequest request, HttpServletResponse response){
		
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		TimeZone timeZone = UtilHttp.getTimeZone(request);
		Locale locale = UtilHttp.getLocale(request);
		
		Map<String, Object> resultado = FastMap.newInstance();
		
		Map<String,Object> mapaAtributos = UtilHttp.getParameterMap(request);
		
		String dateFormat = UtilDateTime.getDateFormat(locale);
		
		Date fechaInicio = UtilDateTime.getDateSqlFromString(dateFormat, timeZone, locale, (String) mapaAtributos.get("fechaInicio"));
		Date fechaFin = UtilDateTime.getDateSqlFromString(dateFormat, timeZone, locale, (String) mapaAtributos.get("fechaFin"));
		
		GenericValue Enumeration = delegator.makeValue("Enumeration");
		Enumeration.setPKFields(mapaAtributos);
		
		try {
			
			Enumeration = delegator.findByPrimaryKey("Enumeration",UtilMisc.toMap("enumId",mapaAtributos.get("enumId")));
			
			if(UtilValidate.isNotEmpty(Enumeration)){
				Enumeration.setNonPKFields(mapaAtributos);
				Enumeration.set("fechaInicio", fechaInicio);
				Enumeration.set("fechaFin", fechaFin);
				Enumeration.store();
			} else {
				resultado.put("mensaje", "ERROR");
				return doJSONResponse(response, resultado);
			}

			resultado.put("mensaje", "SUCCESS");
			return doJSONResponse(response, resultado);
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
			resultado.put("mensaje", "ERROR");
			return doJSONResponse(response, resultado);
		}
		
	}
	
	/**
	 * Metodo para crear una nueva clasificacion (Enumeration)
	 * @param request
	 * @param response
	 * @return
	 */
	public static String crearNuevaClasificacion(HttpServletRequest request, HttpServletResponse response){
	
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		TimeZone timeZone = UtilHttp.getTimeZone(request);
		Locale locale = UtilHttp.getLocale(request);
		
		Map<String, Object> resultado = new HashMap<String, Object>();
		
		Map<String,Object> mapaAtributos = UtilHttp.getParameterMap(request);
		
		StringBuffer error = new StringBuffer("Es necesario ingresar los siguientes campos : \n");
		boolean incompleto = false;
		
		Map<String,String> mapaValidaCamposObligatorios = UtilMisc.toMap("Clasificaci\u00f3n",mapaAtributos.get("enumTypeId"),
				"Nivel",mapaAtributos.get("nivelId"),"C\u00f3digo",mapaAtributos.get("sequenceId"),
				"Nombre",mapaAtributos.get("enumCode"),"Descripci\u00f3n",mapaAtributos.get("description"),
				"Fecha Inicial",mapaAtributos.get("fechaInicio"),"Fecha Final",mapaAtributos.get("fechaFin"));
		for (Map.Entry<String, String> entryCampo : mapaValidaCamposObligatorios.entrySet()){
			if(UtilValidate.isEmpty(entryCampo.getValue())){
				error.append(entryCampo.getKey()+" \n");
				incompleto = true;
			}
		}
		
		String dateFormat = UtilDateTime.getDateFormat(locale);
		
		mapaAtributos.put("fechaInicio", 
				UtilDateTime.getDateSqlFromString(dateFormat, timeZone, locale, (String) mapaAtributos.get("fechaInicio")));
		mapaAtributos.put("fechaFin", 
				UtilDateTime.getDateSqlFromString(dateFormat, timeZone, locale, (String) mapaAtributos.get("fechaFin")));
		
		try {
			
			//se consulta para validar que el registro ingresado no exista antes
			List<GenericValue> listEnum = delegator.findByAnd("Enumeration", UtilMisc.toMap("sequenceId",mapaAtributos.get("sequenceId"),
					"enumTypeId",mapaAtributos.get("enumTypeId")));
			if(UtilValidate.isNotEmpty(listEnum)){
				throw new GenericEntityException("El c\u00f3digo ya esta registrado en el sistema ");
			}
			
			if(incompleto){
				throw new GenericEntityException(error.toString());
			}

			GenericValue Enumeration = delegator.makeValue("Enumeration");
			Enumeration.setNextSeqId();
			Enumeration.setAllFields(mapaAtributos, true, null, null);
			delegator.create(Enumeration);
			
			resultado.put("mensaje", "SUCCESS");
			return doJSONResponse(response, resultado);
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
			resultado.put("mensaje", "ERROR");
			resultado.put("mensajeError", e.getMessage());
			return doJSONResponse(response, resultado);
		}
	}

	/**
	 * Regresa un listado con los niveles de la clasificacion recibida
	 * @return
	 */
	public static String obtenerNivelesdeClasificacion(HttpServletRequest request, HttpServletResponse response){
		
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		
		Map<String, Object> resultado = new HashMap<String, Object>();
		
		String enumTypeId = request.getParameter("enumTypeId");
		
		try {
			List<GenericValue> listNivelPresupuestal = delegator.findByAnd("NivelPresupuestal",UtilMisc.toMap("clasificacionId", enumTypeId));
			
			for (GenericValue NivelPresupuestal : listNivelPresupuestal) {
				resultado.put(NivelPresupuestal.getString("nivelId"), NivelPresupuestal.getString("descripcion"));
			}
			
			return doJSONResponse(response, resultado);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			resultado.put("mensaje", "ERROR");
			return doJSONResponse(response, resultado);
		}
		
		
	}
	
	/**
	 * Metodo que actualiza un registro de la tabla Etiqueta Presupuestal
	 * @param request
	 * @param response
	 * @return
	 */
	public static String actualizaEtiqueta(HttpServletRequest request, HttpServletResponse response){
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		TimeZone timeZone = UtilHttp.getTimeZone(request);
		Locale locale = UtilHttp.getLocale(request);
		
		Map<String, Object> resultado = FastMap.newInstance();
		
		Map<String,Object> mapaAtributos = UtilHttp.getParameterMap(request);
		
		String dateFormat = UtilDateTime.getDateFormat(locale);
		
		try {
		
			Date fromDate = UtilDateTime.getDateSqlFromString(dateFormat, timeZone, locale, (String) mapaAtributos.get("fromDate"));
			Date thruDate = UtilDateTime.getDateSqlFromString(dateFormat, timeZone, locale, (String) mapaAtributos.get("thruDate"));
			
			GenericValue Etiqueta = delegator.findByPrimaryKey("Etiqueta",UtilMisc.toMap("etiquetaId",mapaAtributos.get("etiquetaId")));
			
			if(UtilValidate.isNotEmpty(Etiqueta)){
				Etiqueta.setNonPKFields(mapaAtributos);
				Etiqueta.set("fromDate", fromDate);
				Etiqueta.set("thruDate", thruDate);
				Etiqueta.store();
			} else {
				resultado.put("mensaje", "ERROR:No se encontr\u00f3 el registro en la base de datos");
				return doJSONResponse(response, resultado);
			}

			resultado.put("mensaje", "SUCCESS");
			return doJSONResponse(response, resultado);
			
		} catch (NullPointerException | GenericEntityException e) {
			e.printStackTrace();
			resultado.put("mensaje", "ERROR:"+e.getMessage());
			return doJSONResponse(response, resultado);
		}
	}

	/**
	 * Metodo para eliminar una etiqueta presupuestal
	 * @param request
	 * @param response
	 * @return
	 */
	public static String eliminarEtiqueta(HttpServletRequest request, HttpServletResponse response){
		
		Map<String, Object> resultado = FastMap.newInstance();
		
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		Map<String,Object> mapaAtributos = UtilHttp.getParameterMap(request);
		
		try {
			int eliminado = delegator.removeByAnd("Etiqueta",UtilMisc.toMap("etiquetaId",mapaAtributos.get("etiquetaId")));
			
			if(eliminado != 1){
				resultado.put("mensaje", "ERROR:Hubo un error al eliminar el elemento");
				return doJSONResponse(response, resultado);
			}
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
			resultado.put("mensaje", "ERROR:"+e.getMessage());
			return doJSONResponse(response, resultado);
		}
		
		resultado.put("mensaje", "SUCCESS");
		return doJSONResponse(response, resultado);
	}
	
	/**
	 * Metodo para eliminar la asociacion de etiqueta - partida presupuestal
	 * @param request
	 * @param response
	 * @return
	 */
	public static String eliminarEtiquetaEnum(HttpServletRequest request, HttpServletResponse response){
		
		Map<String, Object> resultado = FastMap.newInstance();
		
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		Map<String,Object> mapaAtributos = UtilHttp.getParameterMap(request);
		
		try {
			int eliminado = delegator.removeByAnd("EtiquetaEnumeration",UtilMisc.toMap("etiquetaId",mapaAtributos.get("etiquetaId"),"enumId",mapaAtributos.get("enumId")));
			
			if(eliminado != 1){
				resultado.put("mensaje", "ERROR:Hubo un error al eliminar el elemento");
				return doJSONResponse(response, resultado);
			}
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
			resultado.put("mensaje", "ERROR:"+e.getMessage());
			return doJSONResponse(response, resultado);
		}
		
		resultado.put("mensaje", "SUCCESS");
		return doJSONResponse(response, resultado);
	}
	
	/**
	 * Obtiene la lista de datos del clasificador requerido Ej. clasificador1 --> 2016 - 2016
	 * @param request
	 * @param response
	 * @return
	 */
	public static String getListaClasificador(HttpServletRequest request, HttpServletResponse response){
		Map<String, Object> resultado = FastMap.newInstance();
		
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		Map<String,Object> mapaAtributos = UtilHttp.getParameterMap(request);
		String tipo = (String) mapaAtributos.get("tipo");
		String ciclo = (String) mapaAtributos.get("ciclo");
		String organizationPartyId = (String) mapaAtributos.get("organizationPartyId");
		boolean activas = Boolean.valueOf((String)mapaAtributos.get("activas"));
		String clasificacion = (String) mapaAtributos.get("clasificacion"); //clasificacion1, clasificacion2,..
		String valorClasif = (String) mapaAtributos.get("valorClasif");
		String clasificacionAnterior = new String();
		
		try {
			
			GenericValue Estructura = UtilClavePresupuestal.obtenEstructPresupuestal(tipo, organizationPartyId, delegator, ciclo);
			String clasificacionId = Estructura.getString(clasificacion);
			
			List<EntityExpr> condicionClave = UtilMisc.toList(EntityCondition.makeCondition("idSecuencia",Estructura.getString("idSecuencia")));
			if(UtilValidate.isNotEmpty(valorClasif)){
				String [] clas = clasificacion.split(UtilClavePresupuestal.VIEW_TAG_PREFIX);
				int numAnt = Integer.valueOf(clas[1])-1;
				clasificacionAnterior = UtilClavePresupuestal.VIEW_TAG_PREFIX+numAnt;
				condicionClave.add(EntityCondition.makeCondition(clasificacionAnterior,valorClasif));
			}
			List<GenericValue> listClasificacion = delegator.findByCondition("ClavePresupuestalAgrup", 
					EntityCondition.makeCondition(condicionClave, EntityOperator.AND), UtilMisc.toList(clasificacion.toString()), null); 
			Set<String> partidasValidas = FastSet.newInstance();
			
			for (GenericValue ComponenteClave : listClasificacion) {
				partidasValidas.add(ComponenteClave.getString(clasificacion));
			}

			List<String> orderBy = FastList.newInstance();
			List<String> fieldsToSelect = FastList.newInstance();
			List<EntityExpr> listaCondiciones = FastList.newInstance();
			List<GenericValue> listResultado = FastList.newInstance();
					
			if(clasificacionId.equals("CL_ADMINISTRATIVA")){
				fieldsToSelect = UtilMisc.toList("externalId","groupName");
				orderBy = UtilMisc.toList("externalId","groupName");
				listaCondiciones.add(EntityCondition.makeCondition("externalId",EntityOperator.IN,partidasValidas));
				if(activas){
					listaCondiciones.add(EntityCondition.makeCondition("state", EntityOperator.EQUALS, "A"));
				}
				listResultado = delegator.findByConditionCache("PartyAndGroup", EntityCondition.makeCondition(listaCondiciones,EntityOperator.AND), fieldsToSelect, orderBy);
			} else {
				fieldsToSelect = UtilMisc.toList("sequenceId","enumCode");
				orderBy = UtilMisc.toList("sequenceId","enumCode");
				listaCondiciones.add(EntityCondition.makeCondition("enumTypeId", EntityOperator.EQUALS, clasificacionId));
				listaCondiciones.add(EntityCondition.makeCondition("sequenceId",EntityOperator.IN,partidasValidas));
				if(activas){
					listaCondiciones.add(EntityCondition.makeCondition("fechaFin", EntityOperator.GREATER_THAN, UtilDateTime.nowSqlDate()));
					listaCondiciones.add(EntityCondition.makeCondition("fechaInicio", EntityOperator.LESS_THAN, UtilDateTime.nowSqlDate()));
				}
				listResultado = delegator.findByConditionCache("Enumeration", EntityCondition.makeCondition(listaCondiciones,EntityOperator.AND), fieldsToSelect, orderBy);
			}
			
			for (GenericValue genericValue : listResultado) {
				resultado.put(genericValue.getString(fieldsToSelect.get(0)), genericValue.getString(fieldsToSelect.get(0))+" - "+genericValue.getString(fieldsToSelect.get(1)));
			}
			
			return doJSONResponse(response, resultado);
			
		} catch (Exception e) {
			resultado.put("mensaje", "ERROR:"+e.getMessage());
			return doJSONResponse(response, resultado);
		}
		
	}	
	
}
