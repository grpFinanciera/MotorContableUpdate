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

package com.absoluciones.controlPatrimonial;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Locale;

import javax.servlet.http.*;

import net.sf.json.JSONObject;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.opentaps.base.entities.FixedAssetPersonaAlmacen;
import org.opentaps.base.entities.NotificacionActivoFijo;
import org.opentaps.base.entities.PartyFixedAssetAssignment;


/**
 * Ajax events to be invoked by the controller.
 *
 * @author Salvador Cortes
 */
public class AjaxEvents {

    public static final String module = AjaxEvents.class.getName();        	
    	
    	/**
         * Using common method to return json response.
         */
        private static String doJSONResponse(HttpServletResponse response, @SuppressWarnings("rawtypes") Map map) {
            return org.opentaps.common.event.AjaxEvents.doJSONResponse(response, JSONObject.fromObject(map));
        }
        
        public static String actualizaAsignacionActivo(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException, ParseException {
    		Map<String, Object> resultado = new HashMap<String, Object>();
    		try
    		{	Delegator delegator = (Delegator) request.getAttribute("delegator");    			    			
    			final Locale locale = request.getLocale();
    			String dateFormat = UtilDateTime.getDateFormat(locale);
    			TimeZone timeZone = UtilHttp.getTimeZone(request);
    		
    			String fixedAssetId = (String) request.getParameter("fixedAssetId");
	    		String partyId = (String) request.getParameter("partyId");
	    		String fechaHastaString = (String) request.getParameter("fechaHasta");
	    		String fechaAsignacionString = (String)request.getParameter("fechaAsignacion");
	    		String statusId = (String)request.getParameter("statusId");
	    		String comentario = (String)request.getParameter("comentario");
	    		String comentarioOrig = (String)request.getParameter("comentarioOrig");	    		
	    		Timestamp fechaHasta = null;
	    		Timestamp fechaAsignacion = null;
	    		if(fechaHastaString != null && !fechaHastaString.equals(""))
	    			fechaHasta = UtilDateTime.stringToTimeStamp(fechaHastaString, dateFormat, timeZone, locale);
	    		if(fechaAsignacionString != null && !fechaAsignacionString.equals(""))
	    			fechaAsignacion = UtilDateTime.stringToTimeStamp(fechaAsignacionString, dateFormat, timeZone, locale);
	    		Debug.log("fixedAssetId: " + fixedAssetId);
	    		Debug.log("partyId: " + partyId);
	    		Debug.log("statusId: " + statusId);
	    		Debug.log("comentario: " + comentario);
	    		Debug.log("comentarioOrig: " + comentarioOrig);
	    		Debug.log("fechaHasta: " + fechaHasta);
	    		Debug.log("fechaAsignacion: " + fechaAsignacion);	    		
	    		EntityCondition condicion = null;	    			          												    			        
	    		if(comentarioOrig == null || comentarioOrig.equals(""))
	    		{	condicion = EntityCondition.makeCondition(EntityOperator.AND,
			   					EntityCondition.makeCondition(PartyFixedAssetAssignment.Fields.partyId.name(), EntityOperator.EQUALS, partyId),
			   					EntityCondition.makeCondition(PartyFixedAssetAssignment.Fields.fixedAssetId.name(), EntityOperator.EQUALS, fixedAssetId),
			   					EntityCondition.makeCondition(PartyFixedAssetAssignment.Fields.comments.name(), EntityOperator.EQUALS, null));
	    		}
	    		else
	    		{	condicion = EntityCondition.makeCondition(EntityOperator.AND,
			   					EntityCondition.makeCondition(PartyFixedAssetAssignment.Fields.partyId.name(), EntityOperator.EQUALS, partyId),
			   					EntityCondition.makeCondition(PartyFixedAssetAssignment.Fields.fixedAssetId.name(), EntityOperator.EQUALS, fixedAssetId),
			   					EntityCondition.makeCondition(PartyFixedAssetAssignment.Fields.comments.name(), EntityOperator.EQUALS, comentarioOrig));	    			
	    		}    			
    			List<GenericValue> partyFixedAssetAssignmentList = delegator.findByCondition("PartyFixedAssetAssignment", condicion , null, null);
    			Debug.log("partyFixedAssetAssignmentList: " + partyFixedAssetAssignmentList);
    			Iterator<GenericValue> partyFixedAssetAssignmentIter = partyFixedAssetAssignmentList.iterator();
				while (partyFixedAssetAssignmentIter.hasNext()) 
				{	GenericValue partyFixedAssetAssignment = partyFixedAssetAssignmentIter.next();
					Debug.log("partyFixedAssetAssignment: " + partyFixedAssetAssignment);
					partyFixedAssetAssignment.set("thruDate", fechaHasta);
					partyFixedAssetAssignment.set("allocatedDate", fechaAsignacion);
					if(statusId != null && !statusId.equals(""))
						partyFixedAssetAssignment.set("statusId", statusId);
					if(comentario == null || comentario.equals(""))
					{	partyFixedAssetAssignment.set("comments", null);						
					}
					else
					{	partyFixedAssetAssignment.set("comments", comentario);						
					}
					delegator.store(partyFixedAssetAssignment);					
				}								
																
				resultado.put("mensaje", "SUCCESS");
				return doJSONResponse(response, resultado);				
    		}
    		catch(GeneralException e)
    		{	resultado.put("mensaje", "ERROR");    			    		
				return doJSONResponse(response, resultado);
    		}
    	}
    	
    	
    	public static String eliminaAsignacionActivo(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
    		Map<String, Object> resultado = new HashMap<String, Object>();
    		try
    		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
    		
	    		String fixedAssetId = (String) request.getParameter("fixedAssetId");
	    		String partyId = (String) request.getParameter("partyId");	    		
	    		Debug.log("fixedAssetId: " + fixedAssetId);
	    		Debug.log("partyId: " + partyId);
	    		
	    		EntityCondition condicion = EntityCondition.makeCondition(EntityOperator.AND,
								   			EntityCondition.makeCondition(PartyFixedAssetAssignment.Fields.partyId.name(), EntityOperator.EQUALS, partyId),
								   			EntityCondition.makeCondition(PartyFixedAssetAssignment.Fields.fixedAssetId.name(), EntityOperator.EQUALS, fixedAssetId));
				List<GenericValue> partyFixedAssetAssignmentList = delegator.findByCondition("PartyFixedAssetAssignment", condicion , null, null);
				Debug.log("partyFixedAssetAssignmentList: " + partyFixedAssetAssignmentList);
				Iterator<GenericValue> partyFixedAssetAssignmentIter = partyFixedAssetAssignmentList.iterator();
				while (partyFixedAssetAssignmentIter.hasNext()) 
				{	GenericValue partyFixedAssetAssignment = partyFixedAssetAssignmentIter.next();
					Debug.log("partyFixedAssetAssignment: " + partyFixedAssetAssignment);				
					partyFixedAssetAssignment.remove();
				}												    	
								
				//Valida si se elimino
				EntityCondition condicionElimina;	        
				condicionElimina = EntityCondition.makeCondition(EntityOperator.AND,
			   					   EntityCondition.makeCondition(PartyFixedAssetAssignment.Fields.partyId.name(), EntityOperator.EQUALS, partyId),
			   					   EntityCondition.makeCondition(PartyFixedAssetAssignment.Fields.fixedAssetId.name(), EntityOperator.EQUALS, fixedAssetId));    			    			
    			List<GenericValue> eliminaList = delegator.findByCondition("PartyFixedAssetAssignment", condicionElimina , null, null);
    			
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
    	
    	
    	
    	
    	
    	public static String eliminaPermisoAlmacenActivo(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {	
    		Map<String, Object> resultado = new HashMap<String, Object>();
    		try{
    			Delegator delegator = (Delegator) request.getAttribute("delegator");
    			String partyId = (String) request.getParameter("partyId");
        		String facilityId = (String) request.getParameter("facilityId");    		
        		
        		GenericValue generic = delegator.findByPrimaryKey("FixedAssetPersonaAlmacen", UtilMisc.toMap("partyId", partyId, "facilityId", facilityId));
        		generic.remove();
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
    			
    	
    	public static String eliminaResponsablePermisoAlmacen(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException 
    	{	Map<String, Object> resultado = new HashMap<String, Object>();
    		try
    		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
    		
    			String partyId = (String) request.getParameter("partyId");
        		String facilityId = (String) request.getParameter("facilityId");
        		Debug.log("partyId: " + partyId);
        		Debug.log("facilityId: " + facilityId);
        		
        		GenericValue generic = delegator.findByPrimaryKey("ResponsableActivoFijo", UtilMisc.toMap("partyId", partyId, "facilityId", facilityId));
        		Debug.log("generic: " + generic);
        		generic.remove();						
    			
    			//Valida si se elimino
    			EntityCondition condicionElimina;	        
    			condicionElimina = EntityCondition.makeCondition(EntityCondition.makeCondition(FixedAssetPersonaAlmacen.Fields.partyId.name(), EntityOperator.EQUALS, partyId),
    															 EntityCondition.makeCondition(FixedAssetPersonaAlmacen.Fields.facilityId.name(), EntityOperator.EQUALS, facilityId));
    			Debug.log("condicionElimina: " + condicionElimina);
    			List<GenericValue> eliminaList = delegator.findByCondition("ResponsableActivoFijo", condicionElimina , null, null);
    			Debug.log("eliminaList: " + eliminaList);
    			
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
    	
    	public static String marcarNotificacionResuelta(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException 
    	{	Map<String, Object> resultado = new HashMap<String, Object>();
    		try
    		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
    		
    			String orderId = (String) request.getParameter("orderId");
        		String facilityId = (String) request.getParameter("facilityId");
        		String shipmentId = (String) request.getParameter("shipmentId");
        		Debug.log("orderId: " + orderId);
        		Debug.log("facilityId: " + facilityId);
        		Debug.log("shipmentId: " + shipmentId);
        		
        		GenericValue generic = delegator.findByPrimaryKey("NotificacionActivoFijo", UtilMisc.toMap("orderId", orderId, "facilityId", facilityId, "shipmentId", shipmentId));
        		Debug.log("generic: " + generic);
        		generic.set("statusId", "RESUELTA_NOTIFI");
        		Debug.log("generic1: " + generic);
        		delegator.store(generic);
        		        								    			
    			//Valida si se actualiza
    			EntityCondition condicion;	        
    			condicion = EntityCondition.makeCondition(EntityCondition.makeCondition(NotificacionActivoFijo.Fields.orderId.name(), EntityOperator.EQUALS, orderId),
    													  EntityCondition.makeCondition(NotificacionActivoFijo.Fields.facilityId.name(), EntityOperator.EQUALS, facilityId),
    													  EntityCondition.makeCondition(NotificacionActivoFijo.Fields.shipmentId.name(), EntityOperator.EQUALS, shipmentId),
    													  EntityCondition.makeCondition(NotificacionActivoFijo.Fields.statusId.name(), EntityOperator.EQUALS, "PENDIENTE_NOTIFI"));
    			Debug.log("condicion: " + condicion);
    			List<GenericValue> lista = delegator.findByCondition("NotificacionActivoFijo", condicion , null, null);
    			Debug.log("lista: " + lista);
    			
    			if(lista.isEmpty())
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
    	
    	
 }