/*
 * Copyright (c) Open Source Strategies, Inc.
 *
 * Opentaps is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Opentaps is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Opentaps.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.absoluciones.viaticos;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.common.AB.UtilWorkflow;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.order.finaccount.UtilClavePresupuestal;
import org.ofbiz.service.LocalDispatcher;
import org.opentaps.base.entities.DetalleViatico;

import net.sf.json.JSONObject;

/**
 * Ajax events to be invoked by the controller.
 *
 * @author Salvador Cortes
 */
public class AjaxEvents {

    public static final String module = AjaxEvents.class.getName();
    
    	public static String obtenHijosGeo(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
    		Delegator delegator = (Delegator) request.getAttribute("delegator");
    		TreeMap<String, Object> resultado = new TreeMap<String, Object>();
    		
    		String geoId = (String) request.getParameter("geoId");
    		
    		List<GenericValue> geoList = delegator.findByAnd("Geo",UtilMisc.toMap("parentGeoId",geoId),UtilMisc.toList("geoName"));
    		if(geoList != null && !geoList.isEmpty()){
    			for (GenericValue geo : geoList) {
    				resultado.put(geo.getString("geoId"), geo.getString("geoName"));
				}
    			return doJSONResponse(response, resultado);
    		}
			return doJSONResponse(response, resultado);
    	}
    	
    	/**
    	 * Se regresa una lista de tipos geograficos hijos
    	 * @param request
    	 * @param response
    	 * @return
    	 * @throws GenericEntityException
    	 */
    	public static String obtenHijosTipoGeo(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
    		Delegator delegator = (Delegator) request.getAttribute("delegator");
    		Map<String, Object> resultado = new HashMap<String, Object>();
    		
    		String tipoGeo = (String) request.getParameter("tipoGeo");
    		
    		GenericValue geoType = null;
    		List<GenericValue> geoTypeList = null;
    		do{
    			geoTypeList = delegator.findByAnd("GeoType",UtilMisc.toMap("parentTypeId",tipoGeo));
    			if(geoTypeList != null && !geoTypeList.isEmpty()){
    				geoType = geoTypeList.get(0);
    				tipoGeo = geoType.getString("geoTypeId");
    				resultado.put(geoType.getString("geoTypeId"), geoType.getString("description"));
    			} else {
    				geoType	= null;
    			}
    		} while(geoType != null);
    		
    			return doJSONResponse(response, resultado);
    	}
    	
    	public static String actualizaMontosItemsViatico(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
    		Map<String, Object> resultado = new HashMap<String, Object>();
    		try
    		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
    		
    			String viaticoId = (String) request.getParameter("viaticoId");
	    		String detalleViaticoId = (String) request.getParameter("detalleViaticoId");
	    		String montoDetalleString = (String)request.getParameter("montoDetalle");
	    		BigDecimal montoDetalle = new BigDecimal(Double.parseDouble(montoDetalleString));
	    		
	    		//Actualiza el monto y la cantidad en el detalle de la solicitud
	    		GenericValue detalleViaticoGeneric = delegator.findByPrimaryKey("DetalleViatico", UtilMisc.toMap("viaticoId", viaticoId, "detalleViaticoId", detalleViaticoId));											
				detalleViaticoGeneric.set("monto", montoDetalle);
				
				//Se valida suficiencia
				UtilClavePresupuestal.validaSuficienciaClaves(detalleViaticoGeneric.getString("clavePresupuestal"), detalleViaticoGeneric.getBigDecimal("monto"), "DISPONIBLE", delegator);
				
				delegator.store(detalleViaticoGeneric);
	    		
				resultado.put("mensaje", "SUCCESS");
				return doJSONResponse(response, resultado);
				
    		}
    		catch(GeneralException e)
    		{	resultado.put("mensaje", "ERROR");
				return doJSONResponse(response, resultado);
    		}
    	}
    	
    	
    	public static String eliminaItemsViatico(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
    		Map<String, Object> resultado = new HashMap<String, Object>();
    		try
    		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
    		
    			String viaticoId = (String) request.getParameter("viaticoId");
	    		String detalleViaticoId = (String) request.getParameter("detalleViaticoId");
	    		
	    		//Actualiza el monto y la cantidad en el detalle de la solicitud
	    		GenericValue detalleViaticoGeneric = delegator.findByPrimaryKey("DetalleViatico", UtilMisc.toMap("viaticoId", viaticoId, "detalleViaticoId", detalleViaticoId));											
	    		detalleViaticoGeneric.remove();
				
				//Valida si se elimino
				EntityCondition condicionElimina;	        
				condicionElimina = EntityCondition.makeCondition(EntityCondition.makeCondition(DetalleViatico.Fields.viaticoId.name(), EntityOperator.EQUALS, viaticoId),
																 EntityCondition.makeCondition(DetalleViatico.Fields.detalleViaticoId.name(), EntityOperator.EQUALS, detalleViaticoId));    			    			
    			List<GenericValue> eliminaList = delegator.findByCondition("DetalleViatico", condicionElimina , null, null);
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
    	
    	public static String eliminaComprobanteViatico(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
    		Map<String, Object> resultado = new HashMap<String, Object>();
    		try
    		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
    		
    			String conceptoViaticoMontoId = (String) request.getParameter("conceptoViaticoMontoId");
    			
	    		GenericValue detalleViaticoGeneric = delegator.findByPrimaryKey("ConceptoViaticoMonto", UtilMisc.toMap("conceptoViaticoMontoId", conceptoViaticoMontoId));
	    		
	    		
	    		//Disminuimos el monto Diario o monto Transporte Comprobado.
				GenericValue viatico = delegator.findByPrimaryKey("Viatico", UtilMisc.toMap("viaticoId", detalleViaticoGeneric.getString("viaticoId")));
				GenericValue concepto = delegator.findByPrimaryKey("ConceptoViatico", UtilMisc.toMap("conceptoViaticoId", detalleViaticoGeneric.getString("conceptoViaticoId")));
				String campo = concepto.getString("diarioTransporteFlag")
						.equalsIgnoreCase("D") ? "montoDiarioComprobado"
						: concepto.getString("diarioTransporteFlag")
								.equalsIgnoreCase("T") ? "montoTransporteComprobado"
								: "montoTrabCampoComprobado";
				BigDecimal montoTotal = viatico.getBigDecimal(campo);
				viatico.set(campo, montoTotal.subtract(detalleViaticoGeneric.getBigDecimal("monto")));
				delegator.store(viatico);
				
				detalleViaticoGeneric.remove();
				
	    		resultado.put("mensaje", "SUCCESS");
				return doJSONResponse(response, resultado);
				
    		}
    		catch(GeneralException e)
    		{	resultado.put("mensaje", "ERROR");
				return doJSONResponse(response, resultado);
    		}
    	}
    	
    	/**
         * Using common method to return json response.
         */
        private static String doJSONResponse(HttpServletResponse response, @SuppressWarnings("rawtypes") Map map) {
            return org.opentaps.common.event.AjaxEvents.doJSONResponse(response, JSONObject.fromObject(map));
        }
        
        /**
    	 * Metodo para eliminar una factura del gasto a reserva
    	 * @param request
    	 * @param response
    	 * @return
    	 * @throws GenericEntityException
    	 */
    	public static String desactivaUsuario(HttpServletRequest request, HttpServletResponse response) {
    		Map<String, Object> resultado = new HashMap<String, Object>();
    		try{	
    			Delegator delegator = (Delegator) request.getAttribute("delegator");
    			Locale locale = (Locale)request.getSession().getServletContext().getAttribute("locale");
    			String personaSolicitanteId = (String) request.getParameter("personaSolicitanteId");
    			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
    			String userLoginId = (String) request.getParameter("userLoginId");    			
	    		GenericValue usuario = delegator.findByPrimaryKey("UsuarioInhViatico", UtilMisc.toMap("personaSolicitanteId", personaSolicitanteId));
	    		String urlHost = request.getServerName()+":"+request.getServerPort();
	    		
	    		if(UtilValidate.isEmpty(usuario)){
	    			GenericValue user = GenericValue.create(delegator.getModelEntity("UsuarioInhViatico"));
	    			user.set("personaSolicitanteId", personaSolicitanteId);
	    			user.set("estatus", "INACTIVO");
	    			
	    			
	    			String correoOrigen = userLoginId;
	    			String correoDestino = personaSolicitanteId;
	    			String mensaje = "Usted cuenta con un saldo pendiente de comprobar, por lo que se ha deshabilitado la funcionalidad para la generacion de una nueva solicitud de viaticos, una vez que efectue la comprobacion, el sistema operara normalmente.";
	    			UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,"INHABILITA_USUARIO",mensaje,
	    					urlHost,"",null,delegator,locale,dispatcher);
	    			delegator.create(user);
	    		}else{
	    			if(usuario.getString("estatus").equals("ACTIVO")){
	    				usuario.set("estatus", "INACTIVO");
	    				
	    				
	    				String correoOrigen = userLoginId;
		    			String correoDestino = personaSolicitanteId;
		    			String mensaje = "Usted cuenta con un saldo pendiente de comprobar, por lo que se ha deshabilitado la funcionalidad para la generacion de una nueva solicitud de viaticos, una vez que efectue la comprobacion, el sistema operara normalmente.";
		    			UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,"INHABILITA_USUARIO",mensaje,
		    					urlHost,"",null,delegator,locale,dispatcher);
		    			delegator.store(usuario);
	    			}
	    		}
	    		
	    		resultado.put("mensaje", "SUCCESS");
				return doJSONResponse(response, resultado);
    		}
    		catch(GeneralException | NullPointerException e)
    		{	
    			e.printStackTrace();
    			resultado.put("mensaje", "ERROR");
				return doJSONResponse(response, resultado);
    		}
    	}
    	
    	/**
    	 * Metodo para eliminar un programa cambiando el estatus
    	 * @param request
    	 * @param response
    	 * @return
    	 * @throws GenericEntityException
    	 */
    	public static String eliminaPrograma(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
    		Map<String, Object> resultado = new HashMap<String, Object>();
    		try
    		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
    		
    			String programaId = (String) request.getParameter("programaId");
        		
        		//Actualiza el monto y la cantidad en el detalle de la solicitud
        		GenericValue programasViaticosGeneric = delegator.findByPrimaryKey("ProgramaViatico", UtilMisc.toMap("programaId", programaId));
        		programasViaticosGeneric.set("estatus", "Inactivo");
        		programasViaticosGeneric.store();			
    			resultado.put("mensaje", "SUCCESS");
    			return doJSONResponse(response, resultado);
    		}
    		catch(GeneralException e)
    		{	resultado.put("mensaje", "ERROR");
    			return doJSONResponse(response, resultado);
    		}
    	}
    	
    	/**
    	 * Metodo para activar un programa cambiando el estatus
    	 * @param request
    	 * @param response
    	 * @return
    	 * @throws GenericEntityException
    	 */
    	public static String habilitaPrograma(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
    		Map<String, Object> resultado = new HashMap<String, Object>();
    		try
    		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
    		
    			String programaId = (String) request.getParameter("programaId");
        		
        		//Actualiza el monto y la cantidad en el detalle de la solicitud
        		GenericValue programasViaticosGeneric = delegator.findByPrimaryKey("ProgramaViatico", UtilMisc.toMap("programaId", programaId));
        		programasViaticosGeneric.set("estatus", "Activo");
        		programasViaticosGeneric.store();			
    			resultado.put("mensaje", "SUCCESS");
    			return doJSONResponse(response, resultado);
    		}
    		catch(GeneralException e)
    		{	resultado.put("mensaje", "ERROR");
    			return doJSONResponse(response, resultado);
    		}
    	}
 }