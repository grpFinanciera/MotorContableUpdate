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

package org.opentaps.purchasing.requisiciones;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.json.JSONObject;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.order.finaccount.UtilClavePresupuestal;
import org.ofbiz.order.order.RequisicionServices;
import org.opentaps.base.entities.Autorizador;
import org.opentaps.base.entities.ControlWorkFlow;
import org.opentaps.base.entities.DetalleRequisicion;
import org.opentaps.base.entities.StatusWorkFlow;
import org.opentaps.base.entities.TipoAdjudicacionArticulo;
import org.opentaps.base.entities.TipoAdjudicacionDoc;
import org.opentaps.base.entities.SubTipoAdjudicacion;
import org.opentaps.base.entities.TipoAdjudicacion;
import org.opentaps.common.util.UtilCommon;

/**
 * Ajax events to be invoked by the controller.
 *
 * @author Salvador Cortes
 */
public class AjaxEvents {

    public static final String module = AjaxEvents.class.getName();
    
    	public static String actualizaMontosItems(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
    		Map<String, Object> resultado = new HashMap<String, Object>();
    		String ciclo = UtilCommon.getCicloId(request);
    		String clavePresupuestal = "";
    		try
    		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
    			Map<String,Object> context = FastMap.newInstance();
    		
    			String requisicionId = (String) request.getParameter("requisicionId");
	    		String detalleRequisicionId = (String) request.getParameter("detalleRequisicionId");
	    		String cantidadDetalleString = (String) request.getParameter("cantidadDetalle");
	    		String montoDetalleString = (String)request.getParameter("montoDetalle");
	    		String organizationPartyId = (String)request.getParameter("organizationPartyId");
	    		String descripcionDetalle = (String)request.getParameter("descripcionDetalle");
	    		String customTimePeriodId = (String)request.getParameter("customTimePeriodId");
	    		
	    		GenericValue CustomTimePeriod = delegator.findByPrimaryKeyCache("CustomTimePeriod", UtilMisc.toMap("customTimePeriodId",customTimePeriodId));
	    		
	    		if(UtilValidate.isNotEmpty(CustomTimePeriod)){
	    			if(CustomTimePeriod.getString("isClosed").equalsIgnoreCase("Y")){
	    				throw new GenericEntityException("El mes que desea actualizar se encuentra cerrado");
	    			}
	    		}
	            	           
	    		//Actualiza el monto y la cantidad en el detalle de la solicitud
	    		GenericValue detalleRequisicionGeneric = delegator.findByPrimaryKey("DetalleRequisicion", UtilMisc.toMap("requisicionId", requisicionId, "detalleRequisicionId", detalleRequisicionId));											
				detalleRequisicionGeneric.set("monto", BigDecimal.valueOf(Double.valueOf(montoDetalleString)));
				detalleRequisicionGeneric.set("cantidad", cantidadDetalleString);
				detalleRequisicionGeneric.set("descripcion", descripcionDetalle);
				detalleRequisicionGeneric.set("customTimePeriodId", customTimePeriodId);
				
				context.putAll(detalleRequisicionGeneric.getAllFields());
				
				for (int i = 1; i <= UtilClavePresupuestal.TAG_COUNT; i++) {
					context.put(UtilClavePresupuestal.VIEW_TAG_PREFIX+i, (String) request.getParameter(UtilClavePresupuestal.VIEW_TAG_PREFIX+i));
				}
				
				clavePresupuestal = UtilClavePresupuestal.almacenaClavePresupuestal(
							context,detalleRequisicionGeneric,delegator,UtilClavePresupuestal.EGRESO_TAG, organizationPartyId,true,ciclo);
				detalleRequisicionGeneric.set("clavePresupuestal", clavePresupuestal);
				
				RequisicionServices.validaCombinacionUnica(context, clavePresupuestal, 
						delegator.findByAnd("DetalleRequisicion", UtilMisc.toMap("requisicionId",requisicionId)),detalleRequisicionId,customTimePeriodId);
				
				delegator.store(detalleRequisicionGeneric);
				
				
				//Obtiene el monto total de la requisicion				
	    		EntityCondition condicion;	        
    			condicion = EntityCondition.makeCondition(EntityCondition.makeCondition(DetalleRequisicion.Fields.requisicionId.name(), EntityOperator.EQUALS, requisicionId));    			    			
    			List<GenericValue> sumaMontoList = delegator.findByCondition("DetalleRequisicion", condicion , UtilMisc.toList("monto", "cantidad"), null);
    			BigDecimal total = BigDecimal.ZERO;
    			Iterator<GenericValue> sumaMontoIter = sumaMontoList.iterator();
				while (sumaMontoIter.hasNext()) 
				{	GenericValue sumaMontoLista = sumaMontoIter.next();
					BigDecimal monto = sumaMontoLista.getBigDecimal("monto");
					Long cantidad = sumaMontoLista.getLong("cantidad");
					total = total.add(monto.multiply(BigDecimal.valueOf(cantidad)));
				}
				
				//Actualiza el monto total de la requisicion
				GenericValue requisicion = delegator.findByPrimaryKey("Requisicion", UtilMisc.toMap("requisicionId",requisicionId));
				requisicion.set("montoTotal", total);
				delegator.store(requisicion);
				
				if(!total.equals(BigDecimal.ZERO))
				{	resultado.put("mensaje", "SUCCESS");
					return doJSONResponse(response, resultado);
				}
				else
				{	
					resultado.put("mensaje", "ERROR");					
					return doJSONResponse(response, resultado);
				}
    		}
    		catch(GenericEntityException e)
    		{	
    			resultado.put("mensaje", "ERROR:"+e.getMessage());
				return doJSONResponse(response, resultado);
    		}
    	}
    	
    	
    	public static String eliminaItemsRequisicion(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
    		Map<String, Object> resultado = new HashMap<String, Object>();
    		try
    		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
    		
    			String requisicionId = (String) request.getParameter("requisicionId");
	    		String detalleRequisicionId = (String) request.getParameter("detalleRequisicionId");
	    		
	    		//Actualiza el monto y la cantidad en el detalle de la solicitud
	    		GenericValue detalleRequisicionGeneric = delegator.findByPrimaryKey("DetalleRequisicion", UtilMisc.toMap("requisicionId", requisicionId, "detalleRequisicionId", detalleRequisicionId));											
	    		detalleRequisicionGeneric.remove();
				
				//Obtiene el monto total de la requisicion				
	    		EntityCondition condicion;	        
    			condicion = EntityCondition.makeCondition(EntityCondition.makeCondition(DetalleRequisicion.Fields.requisicionId.name(), EntityOperator.EQUALS, requisicionId));    			    			
    			List<GenericValue> sumaMontoList = delegator.findByCondition("DetalleRequisicion", condicion , UtilMisc.toList("monto", "cantidad"), null);
    			BigDecimal total = BigDecimal.ZERO;
    			Iterator<GenericValue> sumaMontoIter = sumaMontoList.iterator();
				while (sumaMontoIter.hasNext()) 
				{	GenericValue sumaMontoLista = sumaMontoIter.next();
					BigDecimal monto = sumaMontoLista.getBigDecimal("monto");
					Long cantidad = sumaMontoLista.getLong("cantidad");
					total = total.add(monto.multiply(BigDecimal.valueOf(cantidad)));
				}
				
				//Actualiza el monto total de la requisicion
				GenericValue requisicion = delegator.findByPrimaryKey("Requisicion", UtilMisc.toMap("requisicionId",requisicionId));
				requisicion.set("montoTotal", total);
				delegator.store(requisicion);
				
				//Valida si se elimino
				EntityCondition condicionElimina;	        
				condicionElimina = EntityCondition.makeCondition(EntityCondition.makeCondition(DetalleRequisicion.Fields.requisicionId.name(), EntityOperator.EQUALS, requisicionId),
																 EntityCondition.makeCondition(DetalleRequisicion.Fields.detalleRequisicionId.name(), EntityOperator.EQUALS, detalleRequisicionId));    			    			
    			List<GenericValue> eliminaList = delegator.findByCondition("DetalleRequisicion", condicionElimina , null, null);
    			
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
    	
    	public static String eliminaAutorizador(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
    		Map<String, Object> resultado = new HashMap<String, Object>();
    		try
    		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
    		
    			String areaId = (String) request.getParameter("areaId");
	    		String autorizadorId = (String) request.getParameter("autorizadorId");
	    		String organizationPartyId = (String) request.getParameter("organizationPartyId");
	    		String tipoWorkFlowId = (String) request.getParameter("tipoWorkFlowId");
	    		String compara = null;
	    		long secuencia = 0;
	    		
	    		//Valida si el que se quiere elimininar es el ultimo autorizador
				EntityCondition condicion;	        
				condicion = EntityCondition.makeCondition(EntityCondition.makeCondition(Autorizador.Fields.areaId.name(), EntityOperator.EQUALS, areaId),
																 EntityCondition.makeCondition(Autorizador.Fields.organizationPartyId.name(), EntityOperator.EQUALS, organizationPartyId),
																 EntityCondition.makeCondition(Autorizador.Fields.secuencia.name(), EntityOperator.NOT_EQUAL, 0),
																 EntityCondition.makeCondition(Autorizador.Fields.tipoWorkFlowId.name(), EntityOperator.EQUALS, tipoWorkFlowId));    			    			
    			List<GenericValue> autorizadoresList = delegator.findByCondition("Autorizador", condicion , UtilMisc.toList("autorizadorId"), UtilMisc.toList("secuencia DESC"));
    			
    			Debug.log("autorizadoresList.- "+ autorizadoresList);
    			
    			Iterator<GenericValue> autoId = autorizadoresList.iterator();
    			GenericValue aut = autoId.next();
    			compara = aut.getString("autorizadorId");
    			
    			GenericValue app = delegator.findByPrimaryKey("Autorizador", UtilMisc.toMap("areaId", areaId, 
    					"autorizadorId", autorizadorId, "organizationPartyId", organizationPartyId,
    					"tipoWorkFlowId", tipoWorkFlowId));
    			
    			Debug.log("autorizadorApp.- "+ app);
    			
    			secuencia = app.getLong("secuencia");
    			
    			Debug.log("autorizadorId.- "+ autorizadorId);
    			Debug.log("compara.- "+ compara);
    			Debug.log("Secuencia.- "+ secuencia);
    			Debug.log("Tamano.- "+ autorizadoresList.size());
	    		if((secuencia == 1 && autorizadoresList.size() == 1 && compara.equalsIgnoreCase(autorizadorId))
	    				|| (secuencia != 1 && autorizadoresList.size() > 1 && compara.equalsIgnoreCase(autorizadorId))){
	    			long sec = 0;
	    			//Actualiza el autorizador 
	    			GenericValue autorizador = GenericValue.create(delegator
	    				.getModelEntity("Autorizador"));
	    			autorizador.set("areaId", areaId);
	    			autorizador.set("autorizadorId", autorizadorId);
	    			autorizador.set("organizationPartyId", organizationPartyId);
	    			autorizador.set("secuencia", sec);
	    			autorizador.set("tipoWorkFlowId", tipoWorkFlowId);

	    			delegator.createOrStore(autorizador);
				
	    			//Valida si se elimino correctamente
	    			EntityCondition condicionElimina;	        
	    			condicionElimina = EntityCondition.makeCondition(EntityCondition.makeCondition(Autorizador.Fields.areaId.name(), EntityOperator.EQUALS, areaId),
																 EntityCondition.makeCondition(Autorizador.Fields.autorizadorId.name(), EntityOperator.EQUALS, autorizadorId),
																 EntityCondition.makeCondition(Autorizador.Fields.organizationPartyId.name(), EntityOperator.EQUALS, organizationPartyId),
																 EntityCondition.makeCondition(Autorizador.Fields.secuencia.name(), EntityOperator.EQUALS, 0),
																 EntityCondition.makeCondition(Autorizador.Fields.tipoWorkFlowId.name(), EntityOperator.EQUALS, tipoWorkFlowId));    			    			
	    			List<GenericValue> eliminaList = delegator.findByCondition("Autorizador", condicionElimina , null, null);
    			
	    			if(!eliminaList.isEmpty())
	    			{	resultado.put("mensaje", "SUCCESS");
						return doJSONResponse(response, resultado);
	    			}
	    			else
	    			{	resultado.put("mensaje", "ERROR");
						return doJSONResponse(response, resultado);
	    			}
				}else{
					resultado.put("mensaje", "ERROR2");
					return doJSONResponse(response, resultado);
				}
    		}
    		catch(GeneralException e)
    		{	resultado.put("mensaje", "ERROR");
				return doJSONResponse(response, resultado);
    		}
    	}
    	
    	
    	
    	/** Return the objects which related with product in receive inventory form.
         * @throws GenericEntityException
         */
        public static String obtieneDocsRequeridos(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
    		try
    		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
    			String tipoAdjudicacionId = (String) request.getParameter("tipoAdjudicacionId");
    			
    			List<String> orderBy = org.ofbiz.base.util.UtilMisc.toList("nombreDocumento");
    			
    			EntityCondition condicionDocs;	        
    			condicionDocs = EntityCondition.makeCondition(EntityOperator.AND,
                    EntityCondition.makeCondition(TipoAdjudicacionDoc.Fields.tipoAdjudicacionId.name(), EntityOperator.EQUALS, tipoAdjudicacionId),
                    EntityCondition.makeCondition(TipoAdjudicacionDoc.Fields.flagArticulo.name(), EntityOperator.EQUALS, null));    			
    			
    			List<GenericValue> documentos = delegator.findByCondition("TipoAdjudicacionDoc", condicionDocs , UtilMisc.toList("docTipoAdjudicacionId", "nombreDocumento", "notaDocumento"), orderBy);
    			Map<String, Object> resultado = new HashMap<String, Object>();    			
    			
    			if (!documentos.isEmpty()) 
    			{	Iterator<GenericValue> documentosIter = documentos.iterator();
    				while (documentosIter.hasNext()) 
    				{	GenericValue documentosListaTrans = documentosIter.next();
    					if(documentosListaTrans.getString("notaDocumento") != null)
    						resultado.put(documentosListaTrans.getString("docTipoAdjudicacionId"), documentosListaTrans.getString("nombreDocumento")+"  "+documentosListaTrans.getString("notaDocumento"));
    					else
    						resultado.put(documentosListaTrans.getString("docTipoAdjudicacionId"), documentosListaTrans.getString("nombreDocumento"));
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
        
        /** Return the objects which related with product in receive inventory form.
         * @throws GenericEntityException
         */
        @SuppressWarnings("unchecked")
        public static String obtieneSubTiposAdjudicacion(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
    		try
    		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
    			String tipoAdjudicacionId = (String) request.getParameter("tipoAdjudicacionId");
    			
    			List<String> orderBy = org.ofbiz.base.util.UtilMisc.toList("subTipoAdjudicacionId");
    			
    			EntityCondition condicionSubTipos;	        
    			condicionSubTipos = EntityCondition.makeCondition(EntityOperator.AND,
                    EntityCondition.makeCondition(SubTipoAdjudicacion.Fields.tipoAdjudicacionId.name(), EntityOperator.EQUALS, tipoAdjudicacionId));    			
    			
    			List<GenericValue> subTiposAdju = delegator.findByCondition("SubTipoAdjudicacion", condicionSubTipos , UtilMisc.toList("tipoAdjudicacionId", "subTipoAdjudicacionId", "nombreSubTipoAdjudicacion"), orderBy);
    			Map<String, Object> resultado = new HashMap<String, Object>();    			
    			
    			if (!subTiposAdju.isEmpty()) 
    			{	Iterator<GenericValue> subTiposAdjuIter = subTiposAdju.iterator();
    				while (subTiposAdjuIter.hasNext()) 
    				{	GenericValue subTiposAdjuIterListaTrans = subTiposAdjuIter.next();
    					if(subTiposAdjuIterListaTrans.getString("nombreSubTipoAdjudicacion") != null)
    					{	resultado.put(subTiposAdjuIterListaTrans.getString("subTipoAdjudicacionId"), subTiposAdjuIterListaTrans.getString("nombreSubTipoAdjudicacion"));
    					}    					
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
        
        @SuppressWarnings("unchecked")
        public static String obtieneDescTipoAdjudicacion(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
    		try
    		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
    			String tipoAdjudicacionId = (String) request.getParameter("tipoAdjudicacionId");
    			
    			List<String> orderBy = org.ofbiz.base.util.UtilMisc.toList("descTipoAdjudicacion");
    			
    			EntityCondition condicionDescTipo;	        
    			condicionDescTipo = EntityCondition.makeCondition(EntityOperator.AND,
                    EntityCondition.makeCondition(TipoAdjudicacion.Fields.tipoAdjudicacionId.name(), EntityOperator.EQUALS, tipoAdjudicacionId));    			
    			
    			List<GenericValue> descTipoAdju = delegator.findByCondition("TipoAdjudicacion", condicionDescTipo , UtilMisc.toList("descTipoAdjudicacion", "tipoAdjudicacionId"), orderBy);
    			Map<String, Object> resultado = new HashMap<String, Object>();    			
    			
    			if (!descTipoAdju.isEmpty()) 
    			{	Iterator<GenericValue> descTipoAdjuIter = descTipoAdju.iterator();
    				while (descTipoAdjuIter.hasNext()) 
    				{	GenericValue descTipoAdjuIterListaTrans = descTipoAdjuIter.next();
    					if(descTipoAdjuIterListaTrans.getString("descTipoAdjudicacion") != null)
    					{	resultado.put(descTipoAdjuIterListaTrans.getString("tipoAdjudicacionId"), descTipoAdjuIterListaTrans.getString("descTipoAdjudicacion"));
    					}    					
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
        
        /*
         * Obtener articulos
         */
        public static String obtieneArticulos(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
    		try
    		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
    			String tipoAdjudicacionId = (String) request.getParameter("tipoAdjudicacionId");
    			
    			List<String> orderBy = org.ofbiz.base.util.UtilMisc.toList("nombreDocumento");
    			
    			EntityCondition condicionDocs;	        
    			condicionDocs = EntityCondition.makeCondition(EntityOperator.AND,
                    EntityCondition.makeCondition(TipoAdjudicacionDoc.Fields.tipoAdjudicacionId.name(), EntityOperator.EQUALS, tipoAdjudicacionId),
                    EntityCondition.makeCondition(TipoAdjudicacionDoc.Fields.flagArticulo.name(), EntityOperator.EQUALS, "Y"));    			
    			
    			List<GenericValue> documentos = delegator.findByCondition("TipoAdjudicacionDoc", condicionDocs , UtilMisc.toList("docTipoAdjudicacionId", "nombreDocumento", "notaDocumento"), orderBy);
    			Map<String, Object> resultado = new HashMap<String, Object>();    			
    			
    			if (!documentos.isEmpty()) 
    			{	Iterator<GenericValue> documentosIter = documentos.iterator();
    				while (documentosIter.hasNext()) 
    				{	GenericValue documentosListaTrans = documentosIter.next();
    					if(documentosListaTrans.getString("notaDocumento") != null)
    						resultado.put(documentosListaTrans.getString("docTipoAdjudicacionId"), documentosListaTrans.getString("nombreDocumento")+"  "+documentosListaTrans.getString("notaDocumento"));
    					else
    						resultado.put(documentosListaTrans.getString("docTipoAdjudicacionId"), documentosListaTrans.getString("nombreDocumento"));
    				}
    				return doJSONResponse(response, resultado);
    			}
    			else			
    			{	resultado.put("tipoAdjudicacionId", "ERROR");
    				return doJSONResponse(response, resultado);
    			}
    		}
    		catch(GeneralException e)
    		{	return org.opentaps.common.event.AjaxEvents.doJSONResponse(response, FastList.newInstance());
    		}
    	}
        
        /*
         * Obtener fracciones
         */
        public static String obtieneFracciones(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
    		try
    		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
    			String docTipoAdjudicacionId = (String) request.getParameter("docTipoAdjudicacionId");
    			
    			List<String> orderBy = org.ofbiz.base.util.UtilMisc.toList("fraccion");
    			
    			EntityCondition condicionDocs;	        
    			condicionDocs = EntityCondition.makeCondition(EntityOperator.AND,
                    EntityCondition.makeCondition(TipoAdjudicacionArticulo.Fields.docTipoAdjudicacionId.name(), EntityOperator.EQUALS, docTipoAdjudicacionId));    			
    			
    			List<GenericValue> documentos = delegator.findByCondition("TipoAdjudicacionArticulo", condicionDocs , UtilMisc.toList("tipoFraccionId", "fraccion"), orderBy);
    			Map<String, Object> resultado = new HashMap<String, Object>();    			
    			
    			if (!documentos.isEmpty()) 
    			{	Iterator<GenericValue> documentosIter = documentos.iterator();
    				while (documentosIter.hasNext()) 
    				{	GenericValue documentosListaTrans = documentosIter.next();
    					resultado.put(documentosListaTrans.getString("tipoFraccionId"), documentosListaTrans.getString("fraccion"));
    				}
    				return doJSONResponse(response, resultado);
    			}
    			else			
    			{	resultado.put("docTipoAdjudicacionId", "ERROR");
    				return doJSONResponse(response, resultado);
    			}
    		}
    		catch(GeneralException e)
    		{	return org.opentaps.common.event.AjaxEvents.doJSONResponse(response, FastList.newInstance());
    		}
    	}
        
        /*
         * Obtener descripcion de los articulos
         */
        public static String obtieneDesArticulos(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
    		try
    		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
    			String docTipoAdjudicacionId = (String) request.getParameter("docArtId");
    			
    			List<String> orderBy = org.ofbiz.base.util.UtilMisc.toList("descArtId");
    			
    			EntityCondition condicionDocs;	        
    			condicionDocs = EntityCondition.makeCondition(EntityOperator.AND,
                    EntityCondition.makeCondition(TipoAdjudicacionArticulo.Fields.docTipoAdjudicacionId.name(), EntityOperator.EQUALS, docTipoAdjudicacionId));    			
    			
    			List<GenericValue> documentos = delegator.findByCondition("TipoAdjudicacionDescArticul", condicionDocs , UtilMisc.toList("descArtId", "descripcion"), orderBy);
    			Map<String, Object> resultado = new HashMap<String, Object>();    			
    			
    			if (!documentos.isEmpty()) 
    			{	Iterator<GenericValue> documentosIter = documentos.iterator();
    				while (documentosIter.hasNext()) 
    				{	GenericValue documentosListaTrans = documentosIter.next();
    					resultado.put(documentosListaTrans.getString("descArtId"), documentosListaTrans.getString("descripcion"));
    				}
    				return doJSONResponse(response, resultado);
    			}
    			else			
    			{	resultado.put("descArtId", "ERROR");
    				return doJSONResponse(response, resultado);
    			}
    		}
    		catch(GeneralException e)
    		{	return org.opentaps.common.event.AjaxEvents.doJSONResponse(response, FastList.newInstance());
    		}
    	}
        
        /*
         * Cancela productos pendientes
         */
        public static String cancelaProducto(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
    		Map<String, Object> resultado = new HashMap<String, Object>();
    		try
    		{	
    			Debug.log("Entra a cancelar el producto");
    			
    			Delegator delegator = (Delegator) request.getAttribute("delegator");
    		
    			String requisicionId = (String) request.getParameter("requisicionId");
	    		String detalleRequisicionId = (String) request.getParameter("detalleRequisicionId");	
	    		
	    		Debug.log("requisicion: "+ requisicionId + "detalle " + detalleRequisicionId);
	    		
	    		//Valida si exista ese producto
				EntityCondition condicion;	        
				condicion = EntityCondition.makeCondition(EntityCondition.makeCondition(DetalleRequisicion.Fields.detalleRequisicionId.name(), EntityOperator.EQUALS, detalleRequisicionId),
																 EntityCondition.makeCondition(DetalleRequisicion.Fields.requisicionId.name(), EntityOperator.EQUALS, requisicionId));    			    			
    			List<GenericValue> autorizadoresList = delegator.findByCondition("DetalleRequisicion", condicion , UtilMisc.toList("detalleRequisicionId"), UtilMisc.toList("detalleRequisicionId DESC"));
    			
    			Iterator<GenericValue> autoId = autorizadoresList.iterator();
    			GenericValue aut = autoId.next();
    			
    			Debug.log("si existe: "+ aut.getString("detalleRequisicionId"));
    			
	    		if(aut.getString("detalleRequisicionId") != null){
	    			//Actualiza el producto
	    			GenericValue detalle = GenericValue.create(delegator
	    					.getModelEntity("DetalleRequisicion"));
	    			detalle.set("detalleRequisicionId", detalleRequisicionId);
	    			detalle.set("requisicionId", requisicionId);
	    			detalle.set("estatusProducto", "CANCELADO");
	    			delegator.createOrStore(detalle);
				
	    			//Valida si se actualizo
	    			EntityCondition condicionActualiza;	        
	    			condicionActualiza = EntityCondition.makeCondition(EntityCondition.makeCondition(DetalleRequisicion.Fields.detalleRequisicionId.name(), EntityOperator.EQUALS, detalleRequisicionId),
							 									 EntityCondition.makeCondition(DetalleRequisicion.Fields.requisicionId.name(), EntityOperator.EQUALS, requisicionId),
																 EntityCondition.makeCondition(DetalleRequisicion.Fields.estatusProducto.name(), EntityOperator.EQUALS, "CANCELADO"));    			    			
	    			List<GenericValue> cancelaList = delegator.findByCondition("DetalleRequisicion", condicionActualiza , null, null);
    			
	    			if(!cancelaList.isEmpty())
	    			{	resultado.put("mensaje", "SUCCESS");
						return doJSONResponse(response, resultado);
	    			}
	    			else
	    			{	resultado.put("mensaje", "ERROR");
						return doJSONResponse(response, resultado);
	    			}
				}else{
					resultado.put("mensaje", "ERROR2");
					return doJSONResponse(response, resultado);
				}
    		}
    		catch(GeneralException e)
    		{	resultado.put("mensaje", "ERROR");
				return doJSONResponse(response, resultado);
    		}
    	}
        
        /** Regresa el nombre del partyId
         * @throws GenericEntityException
         */
        public static String obtieneNombrePartyId(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
    		try
    		{	
    			Map<String, Object> resultado = new HashMap<String, Object>();
    			Delegator delegator = (Delegator) request.getAttribute("delegator");
    			String partyId = (String) request.getParameter("partyId");
    			GenericValue partyGroup = null;
    			
    			partyGroup = delegator.findOne("PartyGroup", UtilMisc.toMap("partyId", partyId), false);
    			String groupName = partyGroup.getString("groupName");
    			
    			if (groupName != null)
    			{
    				resultado.put("groupName", groupName);
    				
    			}
    			else
    			{
    				resultado.put("groupName", "");
    			}
    			return doJSONResponse(response, resultado);
    		}
    		catch(GeneralException e)
    		{
    			return org.opentaps.common.event.AjaxEvents.doJSONResponse(response, FastList.newInstance());
    		}
    	}
        
        /*
         * Cancela productos pendientes
         */
        public static String cancelaRequisicion(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
    		Map<String, Object> resultado = new HashMap<String, Object>();
    		try
    		{	
    			Debug.log("Entra a cancelar la requisicion");
    			
    			Delegator delegator = (Delegator) request.getAttribute("delegator");
    		
    			String requisicionId = (String) request.getParameter("requisicionId");	

	    		Debug.log("requisicion: "+ requisicionId);
	    		
	    		GenericValue requisicion = delegator.findOne("Requisicion", UtilMisc.toMap("requisicionId", requisicionId), false);
	    		Debug.log("Mike entra a requisicion: "+ requisicion);
	    		
	    		//Verifica si fue utilizada para crear un pedido o contrato
	    		List<GenericValue> orderItems = delegator.findByAnd("OrderItem", UtilMisc.toMap("requisicionId", requisicionId));
	    		
	    		if(UtilValidate.isNotEmpty(orderItems)){
	    			resultado.put("mensaje", "ERROR");
					return doJSONResponse(response, resultado);
	    		}
	    		
	    		requisicion.set("statusId", "CANCELADA");
	    		delegator.store(requisicion); 
	    		
	    		//Valida si existen productos para cancelarlos
				EntityCondition condicion;	        
				condicion = EntityCondition.makeCondition(EntityCondition.makeCondition(DetalleRequisicion.Fields.requisicionId.name(), EntityOperator.EQUALS, requisicionId));    			    			
    			List<GenericValue> listDetalleRequisicion = delegator.findByCondition("DetalleRequisicion", condicion , UtilMisc.toList("detalleRequisicionId"), UtilMisc.toList("detalleRequisicionId DESC"));
    			
	    		if(!listDetalleRequisicion.isEmpty()){

	    			Iterator<GenericValue> autoId = listDetalleRequisicion.iterator();
	    			
	    			while(autoId.hasNext()){
	    				GenericValue aut = autoId.next();
	    				Debug.log("si existe: "+ aut.getString("detalleRequisicionId"));
		    			
		    			//Actualiza el producto
		    			GenericValue detalle = GenericValue.create(delegator
		    					.getModelEntity("DetalleRequisicion"));
		    			detalle.set("detalleRequisicionId", aut.getString("detalleRequisicionId"));
		    			detalle.set("requisicionId", requisicionId);
		    			detalle.set("estatusProducto", "CANCELADO");
		    			delegator.createOrStore(detalle);
	    			}

				}
	    		
	    		//valida si hay autorizadores para cancelarlos
	    		EntityCondition condicion1;	        
				condicion1 = EntityCondition.makeCondition(EntityCondition.makeCondition(ControlWorkFlow.Fields.origenId.name(), EntityOperator.EQUALS, requisicionId),
						EntityCondition.makeCondition(ControlWorkFlow.Fields.tipoWorkFlowId.name(), EntityOperator.EQUALS, "REQUISICION"));    			    			
    			List<GenericValue> controlList = delegator.findByCondition("ControlWorkFlow", condicion1 , null, null);
    			Debug.log("Mike control: " + controlList);
	    		
	    		if(!controlList.isEmpty()){
	    			
	    			EntityCondition condicion2;	        
					condicion2 = EntityCondition.makeCondition(EntityCondition.makeCondition(StatusWorkFlow.Fields.workFlowId.name(), EntityOperator.EQUALS, controlList.get(0).getString("workFlowId")));    			    			
	    			List<GenericValue> workFlowList = delegator.findByCondition("StatusWorkFlow", condicion2 , null, null);
	    			Debug.log("Mike workFlowList: " + workFlowList);
	    			
	    			Iterator<GenericValue> autoId = workFlowList.iterator();
	    			
	    			while(autoId.hasNext()){
	    				GenericValue aut = autoId.next();
		    			
	    				Debug.log("Mike aut: " + aut);
		    			//Actualiza el estatus
		    			GenericValue status = GenericValue.create(delegator
		    					.getModelEntity("StatusWorkFlow"));
		    			status.set("statusWorkFlowId", aut.getString("statusWorkFlowId"));
		    			status.set("workFlowId", controlList.get(0).getString("workFlowId"));
		    			status.set("statusId", "CANCELADA_W");
		    			delegator.createOrStore(status);
	    			}

				}
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
 }