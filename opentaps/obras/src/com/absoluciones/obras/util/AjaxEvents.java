package com.absoluciones.obras.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastList;
import net.sf.json.JSONObject;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.common.AB.UtilGeo;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.opentaps.base.entities.BuscarContrato;
import org.opentaps.base.entities.CuentasBanco;

/**
 * Ajax events to be invoked by the controller.
 *
 * @author Salvador Cortes
 */
public class AjaxEvents {

    public static final String module = AjaxEvents.class.getName();
    
    /**
     * Obtiene los campos en jerarquia de las zonas geograficas para llenar los combos
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     */
	public final static String obtenCamposSeleccionar(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException{
		
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		
		String contactMechId = (String) request.getParameter("contactMechId");
		String tipoGeo = (String) request.getParameter("tipoGeo");
		LinkedHashMap <String,String> mapaCampos = new LinkedHashMap<String,String>(); 

		GenericValue postalAddress = delegator.findByPrimaryKey("PostalAddress", UtilMisc.toMap("contactMechId",contactMechId));
		
		LinkedList<GenericValue> jerarquia = UtilGeo.obtenJerarquiaPaisGeo(delegator, tipoGeo);
		for (GenericValue geo : jerarquia) {
			String typeGeo = geo.getString("geoTypeId");
			String valorId = postalAddress.getString(UtilGeo.equivalenciasPostalAddress.get(typeGeo));
			mapaCampos.put(typeGeo,valorId);
		}
		
		return doJSONResponse(response, mapaCampos);
	}   
	
	    public static String obtieneContratistas(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
			try
			{	Delegator delegator = (Delegator) request.getAttribute("delegator");
				String obraId = (String) request.getParameter("obraId");
				
				Debug.log("Mike obraId: " + obraId);
				
				List<String> orderBy = org.ofbiz.base.util.UtilMisc.toList("groupName");			
				
				EntityCondition condicionBan;	        
				condicionBan = EntityCondition.makeCondition(EntityOperator.AND,
		                EntityCondition.makeCondition(BuscarContrato.Fields.obraId.name(), EntityOperator.EQUALS, obraId),
		                EntityCondition.makeCondition(EntityOperator.OR,
		                        EntityCondition.makeCondition(BuscarContrato.Fields.statusId.name(), EntityOperator.EQUALS, "EN_PROCESO_C"),
		                        EntityCondition.makeCondition(BuscarContrato.Fields.statusId.name(), EntityOperator.EQUALS, "INICIADO_C"))
		                );
				
				List<GenericValue> proveedores = delegator.findByCondition("BuscarContrato", condicionBan , 
						UtilMisc.toList("obraId", "contratoId", "contratistaId", "groupName"), orderBy);
				Map<String, Object> resultado = new HashMap<String, Object>();

				Debug.log("Mike proveedores: " + proveedores);
				
				if (!proveedores.isEmpty()) 
				{	Iterator<GenericValue> proveedoresIter = proveedores.iterator();
					while (proveedoresIter.hasNext()) 
					{	GenericValue proveedoresLista = proveedoresIter.next();
						resultado.put(proveedoresLista.getString("contratoId"), proveedoresLista.getString("contratoId") +" - "+ 
								proveedoresLista.getString("contratistaId") +" - "+ proveedoresLista.getString("groupName"));										
					}
					
					Debug.log("Mike resultado proveedores: " + resultado);
					
					return doJSONResponse(response, resultado);
				}
				else			
				{	resultado.put("contratistaId", "ERROR");
					return doJSONResponse(response, resultado);
				}
			}
			catch(GeneralException e)
			{	return org.opentaps.common.event.AjaxEvents.doJSONResponse(response, FastList.newInstance());
			}
		}
	    
	    public static String obtieneContratistasFin(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
			try
			{	Delegator delegator = (Delegator) request.getAttribute("delegator");
				String obraId = (String) request.getParameter("obraId");
				
				Debug.log("Mike obraId: " + obraId);
				
				List<String> orderBy = org.ofbiz.base.util.UtilMisc.toList("groupName");			
				
				EntityCondition condicionBan;	        
				condicionBan = EntityCondition.makeCondition(EntityOperator.AND,
		                EntityCondition.makeCondition(BuscarContrato.Fields.obraId.name(), EntityOperator.EQUALS, obraId),
		                EntityCondition.makeCondition(EntityOperator.OR,
		                        EntityCondition.makeCondition(BuscarContrato.Fields.statusId.name(), EntityOperator.EQUALS, "EN_PROCESO_C"),
		                        EntityCondition.makeCondition(BuscarContrato.Fields.statusId.name(), EntityOperator.EQUALS, "TERMINADO_C"))
		                );
				
				List<GenericValue> proveedores = delegator.findByCondition("BuscarContrato", condicionBan , 
						UtilMisc.toList("obraId", "contratoId", "contratistaId", "groupName"), orderBy);
				Map<String, Object> resultado = new HashMap<String, Object>();

				Debug.log("Mike proveedores: " + proveedores);
				
				if (!proveedores.isEmpty()) 
				{	Iterator<GenericValue> proveedoresIter = proveedores.iterator();
					while (proveedoresIter.hasNext()) 
					{	GenericValue proveedoresLista = proveedoresIter.next();
						resultado.put(proveedoresLista.getString("contratoId"), proveedoresLista.getString("contratoId") +" - "+ 
								proveedoresLista.getString("contratistaId") +" - "+ proveedoresLista.getString("groupName"));										
					}
					
					Debug.log("Mike resultado proveedores: " + resultado);
					
					return doJSONResponse(response, resultado);
				}
				else			
				{	resultado.put("contratistaId", "ERROR");
					return doJSONResponse(response, resultado);
				}
			}
			catch(GeneralException e)
			{	return org.opentaps.common.event.AjaxEvents.doJSONResponse(response, FastList.newInstance());
			}
		}
    	
    	/**
         * Using common method to return json response.
         */
        private static String doJSONResponse(HttpServletResponse response, @SuppressWarnings("rawtypes") Map map) {
            return org.opentaps.common.event.AjaxEvents.doJSONResponse(response, JSONObject.fromObject(map));
        }
 }