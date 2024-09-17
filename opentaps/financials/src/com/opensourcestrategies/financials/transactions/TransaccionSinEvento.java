/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
/* This file has been modified by Open Source Strategies, Inc. */
package com.opensourcestrategies.financials.transactions;

import java.math.BigDecimal;
import java.sql.Timestamp;
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
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.base.entities.DetalleAcctgTrans;


/**
 *	Transaccion sin evento
 */

public class TransaccionSinEvento {
	
	public static String MODULE = TransaccionSinEvento.class.getName();
	
	/**
	 * Crea el viatico
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> guardaTransSinEvento(DispatchContext dctx,
			Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		String descripcion = (String) context.get("descripcion");
		String tipoPolizaId = (String) context.get("tipoPolizaId");
		String organizationPartyId = (String) context.get("organizationPartyId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String currency = (String) context.get("currency");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		
		try {
			
			GenericValue trans = GenericValue.create(delegator.getModelEntity("AcctgTransSinEvento"));
			trans.setNextSeqId();
			trans.set("descripcion", descripcion);
			trans.set("tipoPolizaId", tipoPolizaId);
			trans.set("organizationPartyId", organizationPartyId);
			trans.set("currency", currency);
			trans.set("fechaContable", fechaContable);
			trans.set("userLoginId",
					userLogin.getString("partyId"));
			
			delegator.create(trans);
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("acctgTransId", trans.getString("acctgTransId"));
			return result;			
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}
	
	/**
	 * Agrega un articulo a el viatico , realizando validaciones necesarias
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> agregaEntryTransSinEvento(DispatchContext dctx,
			Map<?, ?> context){
		
		Delegator delegator = dctx.getDelegator();
		String acctgTransId = (String) context.get("acctgTransId");
		BigDecimal monto = (BigDecimal) context.get("monto");
		String cuenta = (String) context.get("cuenta");
		String isDebitCreditFlag = (String) context.get("isDebitCreditFlag");
		String tipoAuxiliar = (String) context.get("tipoAuxiliar");
		String auxiliar = (String) context.get("auxiliar");
		String cuentaBancariaId = (String) context.get("cuentaBancariaId");
		String campo=null,valor=null;
		
		try {
			
			if(UtilValidate.isNotEmpty(tipoAuxiliar)){
				
				GenericValue TipoAuxiliar = delegator.findByPrimaryKey("TipoAuxiliar", UtilMisc.toMap("tipoAuxiliarId",tipoAuxiliar));
				
				if(UtilValidate.isNotEmpty(TipoAuxiliar)){
					String tipo = TipoAuxiliar.getString("tipo");
					
					if(UtilValidate.isNotEmpty(tipo) && tipo.equals("A")){
						if(UtilValidate.isEmpty(auxiliar)){
							return ServiceUtil.returnError("Se requiere ingresar un auxiliar");
						}
						campo="auxiliar";
						valor=auxiliar;
					}
					
					if(UtilValidate.isNotEmpty(tipo) && tipo.equals("B")){
						if(UtilValidate.isEmpty(cuentaBancariaId)){
							return ServiceUtil.returnError("Se requiere ingresar una cuenta bancaria");
						}
						campo="cuentaBancariaId";
						valor=cuentaBancariaId;
					}
				}

			}
						
			//Validamos si la cuenta es de registro.
			if (!isCuentaRegistro(delegator, cuenta)){
				return ServiceUtil.returnError("La cuenta no es de registro");
			}
			
			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
										  EntityCondition.makeCondition("acctgTransId", EntityOperator.EQUALS, acctgTransId));
			List<GenericValue> detallesTrans = delegator.findByCondition("DetalleAcctgTrans",condiciones, UtilMisc.toList("acctgTransId","acctgTransEntryId"), null);
			
			String detalleTransId = "";
			long detalleId = 0;
			Iterator<GenericValue> detallesViaticoIter = detallesTrans.iterator();
			while (detallesViaticoIter.hasNext()) 
			{	GenericValue generic = detallesViaticoIter.next();
				detalleTransId = generic.getString("acctgTransEntryId");
				detalleId = Long.parseLong(detalleTransId);
			}
			
			GenericValue detTrans = GenericValue.create(delegator.getModelEntity("DetalleAcctgTrans"));
			detTrans.set("acctgTransId", acctgTransId);
			detTrans.set("acctgTransEntryId", UtilFormatOut.formatPaddedNumber((detalleId + 1), 4));
			detTrans.set("monto", monto);
			detTrans.set("glAccountId", cuenta);
			detTrans.set("isDebitCreditFlag", isDebitCreditFlag);
			if(campo!=null){
				detTrans.set(campo, valor);
			}
			delegator.create(detTrans);
						
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("acctgTransId", acctgTransId);
			return result;
			
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}		
	}
	
	/**
	 * Realiza la operacion de la poliza sin evento
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> afectaTransSinEvento(
			DispatchContext dctx, Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		String acctgTransId = (String) context.get("acctgTransId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String organizationPartyId = (String) context.get("organizationPartyId");
		Timestamp fechaTrans = UtilDateTime.nowTimestamp();

		try {
			GenericValue AcctgTransSinEvento;
			AcctgTransSinEvento = delegator.findByPrimaryKey("AcctgTransSinEvento", UtilMisc.toMap("acctgTransId", acctgTransId));
			//Iteramos los detalles para crear entries.
			List<GenericValue> detalles = delegator.findByAnd("DetalleAcctgTrans", "acctgTransId", acctgTransId);
			List<GenericValue> entries = FastList.newInstance();
			
			if(detalles.isEmpty()){
				return ServiceUtil.returnError("Debe tener al menos 2 lineas de transacci\u00f3n.");
			}
			
			BigDecimal cargos = BigDecimal.ZERO;
			BigDecimal abonos = BigDecimal.ZERO;
			for(GenericValue detalle : detalles){
				GenericValue entry = GenericValue.create(delegator.getModelEntity("AcctgTransEntry"));
				entry.set("acctgTransEntrySeqId", detalle.getString("acctgTransEntryId"));
			    entry.set("glAccountId", detalle.getString("glAccountId"));
			    entry.set("amount", detalle.getBigDecimal("monto"));
			    entry.set("debitCreditFlag", detalle.getString("isDebitCreditFlag"));
			    entry.set("theirPartyId",detalle.getString("auxiliar"));
			    entry.set("cuentaBancariaId",detalle.getString("cuentaBancariaId"));
			    entries.add(entry);
			    
			    if(entry.getString("debitCreditFlag").equalsIgnoreCase("D")){
			    	cargos = cargos.add(entry.getBigDecimal("amount"));
			    }else{
			    	abonos = abonos.add(entry.getBigDecimal("amount"));
			    }
			}
			
			if(cargos.compareTo(abonos)!=0){
				return ServiceUtil
						.returnError("La transacci\u00f3n no esta balanceada. Cargos="
								+ cargos + " Abonos=" + abonos);
			}
		
			// Hacemos la poliza.
			Map<String, Object> input = FastMap.newInstance();
			input.put("fechaRegistro", fechaTrans);
			input.put("fechaContable", AcctgTransSinEvento.getTimestamp("fechaContable"));
			input.put("usuario", userLogin.getString("userLoginId"));
			input.put("organizationId", organizationPartyId);
			input.put("currency", AcctgTransSinEvento.getString("currency"));
			input.put("descripcion", AcctgTransSinEvento.getString("descripcion"));
			input.put("tipoPolizaId", AcctgTransSinEvento.getString("tipoPolizaId"));
			input.put("entries", entries);
			Map<String, Object> result = dispatcher.runSync("creaTransaccionSinEvento", input);

			if (ServiceUtil.isError(result)) {
				Debug.logError("Hubo Error",MODULE);
				return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
			}

			GenericValue acctgTrans = (GenericValue) result.get("transaccion");

			// Guardamos el agrupador y tipoPoliza del viatico.
			AcctgTransSinEvento.set("poliza", acctgTrans.getString("poliza"));
			AcctgTransSinEvento.set("acctgTransPoliza", acctgTrans.getString("acctgTransId"));
			delegator.store(AcctgTransSinEvento);
			
			result = ServiceUtil.returnSuccess("Se gener\u00f3 correctamente la p\u00f3liza "+acctgTrans.getString("poliza"));
			result.put("acctgTransId", acctgTransId);
			
			return result;
		} catch (GenericEntityException | NullPointerException | GenericServiceException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
			
	}
	
	public static String cambiaEntryTransSinEvento(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
		Map<String, Object> resultado = new HashMap<String, Object>();
		try
		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
		
			String acctgTransId = (String) request.getParameter("acctgTransId");
    		String acctgTransEntryId = (String) request.getParameter("acctgTransEntryId");
    		String isDebitCreditFlag = (String) request.getParameter("isDebitCreditFlag");
    		String montoDetalleString = (String)request.getParameter("montoDetalle");
    		String cuenta = (String) request.getParameter("cuenta");
    		BigDecimal montoDetalle = new BigDecimal(Double.parseDouble(montoDetalleString));
    		
    		//Validamos si la cuenta es de registro.
    			if (!isCuentaRegistro(delegator, cuenta)){
    				throw new GeneralException("La cuenta no es de registro");
    			}
    		
    		//Actualiza el monto y la cantidad en el detalle de la solicitud
    		GenericValue detalleGeneric = delegator.findByPrimaryKey("DetalleAcctgTrans", UtilMisc.toMap("acctgTransId", acctgTransId, "acctgTransEntryId", acctgTransEntryId));											
    		detalleGeneric.set("monto", montoDetalle);
    		detalleGeneric.set("isDebitCreditFlag", isDebitCreditFlag);
    		detalleGeneric.set("glAccountId", cuenta);
			delegator.store(detalleGeneric);
    		
			resultado.put("mensaje", "SUCCESS");
			return doJSONResponse(response, resultado);
			
		}
		catch(GeneralException e)
		{	resultado.put("mensaje", "ERROR");
			return doJSONResponse(response, resultado);
		}
	}
	
	public static String eliminaEntryTransSinEvento(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
		Map<String, Object> resultado = new HashMap<String, Object>();
		try
		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
		
			String acctgTransId = (String) request.getParameter("acctgTransId");
			String acctgTransEntryId = (String) request.getParameter("acctgTransEntryId");
		
    		//Actualiza el monto y la cantidad en el detalle de la solicitud
			GenericValue detalleGeneric = delegator.findByPrimaryKey("DetalleAcctgTrans", UtilMisc.toMap("acctgTransId", acctgTransId, "acctgTransEntryId", acctgTransEntryId));											
			detalleGeneric.remove();
			
			//Valida si se elimino
			EntityCondition condicionElimina;	        
			condicionElimina = EntityCondition.makeCondition(EntityCondition.makeCondition(DetalleAcctgTrans.Fields.acctgTransId.name(), EntityOperator.EQUALS, acctgTransId),
															 EntityCondition.makeCondition(DetalleAcctgTrans.Fields.acctgTransEntryId.name(), EntityOperator.EQUALS, acctgTransEntryId));    			    			
			List<GenericValue> eliminaList = delegator.findByCondition("DetalleAcctgTrans", condicionElimina , null, null);
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
     * Using common method to return json response.
     */
    private static String doJSONResponse(HttpServletResponse response, @SuppressWarnings("rawtypes") Map map) {
        return org.opentaps.common.event.AjaxEvents.doJSONResponse(response, JSONObject.fromObject(map));
    }
    
    public static boolean isCuentaRegistro(Delegator delegator, String cuenta)
			throws GenericEntityException {
		GenericValue generic = delegator.findByPrimaryKey("GlAccount",
				UtilMisc.toMap("glAccountId", cuenta));
		return generic.getString("tipoCuentaId").equals("R");
	}
	
}