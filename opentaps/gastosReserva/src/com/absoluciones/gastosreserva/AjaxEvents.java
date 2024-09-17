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

package com.absoluciones.gastosreserva;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;

/**|
 * Ajax events to be invoked by the controller.
 *
 * @author Salvador Cortes
 */
public class AjaxEvents {

    public static final String module = AjaxEvents.class.getName();
    
    	/**
    	 * Metodo para eliminar una factura del gasto a reserva
    	 * @param request
    	 * @param response
    	 * @return
    	 * @throws GenericEntityException
    	 */
    	public static String eliminaFacturas(HttpServletRequest request, HttpServletResponse response) {
    		Map<String, Object> resultado = new HashMap<String, Object>();
    		try{	
    			Delegator delegator = (Delegator) request.getAttribute("delegator");
    			String gastosReservaId = (String) request.getParameter("gastosReservaId");
    			String detalleGId = (String) request.getParameter("detalleGId");
	    		GenericValue detalleGasto = delegator.findByPrimaryKey("DetalleGasto", UtilMisc.toMap("gastosReservaId", gastosReservaId, "detalleGId", detalleGId));
	    		detalleGasto.remove();
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
         * Using common method to return json response.
         */
        private static String doJSONResponse(HttpServletResponse response, @SuppressWarnings("rawtypes") Map map) {
            return org.opentaps.common.event.AjaxEvents.doJSONResponse(response, JSONObject.fromObject(map));
        }
        
        /**
         * Metodo para eliminar un detalle de presupuesto
         * @param request
         * @param response
         * @return
         * @throws GenericEntityException
         */
        public static String eliminaDetallePresupuesto(HttpServletRequest request, HttpServletResponse response) {
        	Map<String, Object> resultado = new HashMap<String, Object>();
        	
        	
			Delegator delegator = (Delegator) request.getAttribute("delegator");
			String gastosReservaId = (String) request.getParameter("gastosReservaId");
			String detallePresId = (String) request.getParameter("detallePresId");
			
			try {
				
				delegator.removeByAnd("DetalleGastoPresupuesto", UtilMisc.toMap("gastosReservaId",gastosReservaId,"detallePresId",detallePresId));
	    		resultado.put("mensaje", "SUCCESS");
				return doJSONResponse(response, resultado);
				
			} catch (GenericEntityException e) {
				e.printStackTrace();
    			resultado.put("mensaje", "ERROR:"+e.getMessage());
				return doJSONResponse(response, resultado);
			}
        	
        }
        
        
 }
