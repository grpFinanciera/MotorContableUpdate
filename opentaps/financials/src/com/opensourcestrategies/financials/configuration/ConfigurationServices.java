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

package com.opensourcestrategies.financials.configuration;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericDataSourceException;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.order.finaccount.UtilClavePresupuestal;
import org.ofbiz.security.Security;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.base.entities.AcctgTrans;
import org.opentaps.common.util.UtilAccountingTags;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.common.util.UtilMessage;
import org.opentaps.foundation.action.ActionContext;

/**
 * ConfigurationServices - Services for configuring GL Accounts.
 * 
 * @author <a href="mailto:ali@opensourcestrategies.com">Ali Afzal Malik</a>
 * @version $Rev: 150 $
 * @since 2.2
 */
public final class ConfigurationServices {

	private ConfigurationServices() {
	}

	private static String MODULE = ConfigurationServices.class.getName();

	/**
	 * Removes a GL Account from an organization if it is not associated with
	 * any other entity.
	 * 
	 * @param dctx
	 *            a <code>DispatchContext</code> value
	 * @param context
	 *            a <code>Map</code> value
	 * @return a service response <code>Map</code> value
	 */
	@SuppressWarnings("unchecked")
	public static Map removeGlAccountFromOrganization(DispatchContext dctx,
			Map context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String glAccountId = (String) context.get("glAccountId");
		String organizationPartyId = (String) context
				.get("organizationPartyId");
		List value = null;

		Map fields = UtilMisc.toMap("glAccountId", glAccountId,
				"organizationPartyId", organizationPartyId);

		try {
			// check for relation with GlAccountTypeDefault
			value = delegator.findByAnd("GlAccountTypeDefault", fields);
			if (!value.isEmpty()) {
				return ServiceUtil
						.returnError("Could not remove Gl Account from organization because it is associated with an account type through GlAccountTypeDefault.");
			}

			// check for relation with InvoiceItemTypeGlAccount
			value = delegator.findByAnd("InvoiceItemTypeGlAccount", fields);
			if (!value.isEmpty()) {
				return ServiceUtil
						.returnError("Could not remove Gl Account from organization because it is associated with an invoice item type through InvoiceItemTypeGlAccount.");
			}

			// check for relation with PaymentMethod
			fields = UtilMisc.toMap("glAccountId", glAccountId, "partyId",
					organizationPartyId);
			value = delegator.findByAnd("PaymentMethod", fields);
			if (!value.isEmpty()) {
				return ServiceUtil
						.returnError("Could not remove Gl Account from organization because it is associated with a payment method through PaymentMethod.");
			}

			// reset fields
			fields = UtilMisc.toMap("glAccountId", glAccountId,
					"organizationPartyId", organizationPartyId);

			// check for relation with PaymentMethodTypeGlAccount
			value = delegator.findByAnd("PaymentMethodTypeGlAccount", fields);
			if (!value.isEmpty()) {
				return ServiceUtil
						.returnError("Could not remove Gl Account from organization because it is associated with a payment method type through PaymentMethodTypeGlAccount.");
			}

			// check for relation with ProductGlAccount
			value = delegator.findByAnd("ProductGlAccount", fields);
			if (!value.isEmpty()) {
				return ServiceUtil
						.returnError("Could not remove Gl Account from organization because it is associated with a product through ProductGlAccount.");
			}

			// check for relation with VarianceReasonGlAccount
			value = delegator.findByAnd("VarianceReasonGlAccount", fields);
			if (!value.isEmpty()) {
				return ServiceUtil
						.returnError("Could not remove Gl Account from organization because it is associated with an inventory variance reason through VarianceReasonGlAccount.");
			}

			// make sure the posted balance is null or zero
			GenericValue val = delegator.findByPrimaryKey(
					"GlAccountOrganization", fields);
			if (val.get("postedBalance") != null
					&& val.getDouble("postedBalance") != 0) {
				return ServiceUtil
						.returnError("Could not remove Gl Account from organization because it has a non zero posted balance.");
			}

			//Verificar si tiene hijos
			List<GenericValue> hijos = delegator.findByAnd("GlVigentes",
					UtilMisc.toMap("organizationPartyId", organizationPartyId,
							"parentGlAccountId", glAccountId, "thruDate",
							null));

			if(!hijos.isEmpty()){
				Debug.log("Tiene hijos asociados.");
				return ServiceUtil
						.returnError("La cuenta asociada tiene hijos asociados.");
			}

			// Verificar si es hijo unico
			GenericValue cuenta = delegator.findByPrimaryKey("GlAccount",
					UtilMisc.toMap("glAccountId", glAccountId));
			String parentGlAccountId = cuenta.getString("parentGlAccountId");
			hijos = delegator.findByAnd("GlVigentes",
					UtilMisc.toMap("organizationPartyId", organizationPartyId,
							"parentGlAccountId", parentGlAccountId, "thruDate",
							null));
			if (hijos.size() < 2) {
				cuenta = delegator.findByPrimaryKey("GlAccount",
						UtilMisc.toMap("glAccountId", parentGlAccountId));
				if (cuenta.getString("tipoCuentaId").equalsIgnoreCase("A")) {
					cuenta.put("tipoCuentaId", "R");
					delegator.store(cuenta);
				}
			}

			// remove the GL Account by setting the thru date to now date
			Map updateGlAccountOrganizationContext = UtilMisc.toMap(
					"glAccountId", glAccountId, "organizationPartyId",
					organizationPartyId, "thruDate",
					UtilDateTime.nowTimestamp(), "userLogin", userLogin);
			Map updateGlAccountOrganizationResult = dispatcher.runSync(
					"updateGlAccountOrganization",
					updateGlAccountOrganizationContext);
			if (ServiceUtil.isError(updateGlAccountOrganizationResult)) {
				return updateGlAccountOrganizationResult;
			}

			return ServiceUtil.returnSuccess();
		} catch (GeneralException e) {
			return ServiceUtil
					.returnError("Could not remove Gl Account from organization ("
							+ e.getMessage() + ").");
		}
	}

	/**
	 * Adds a new GL Account and associates it to an Organization if the
	 * specified account code is unique i.e. no existing GL Account has the same
	 * account code.
	 * 
	 * @param dctx
	 *            a <code>DispatchContext</code> value
	 * @param context
	 *            a <code>Map</code> value
	 * @return a service response <code>Map</code> value
	 */
	public static Map<String,Object> addNewGlAccount(DispatchContext dctx, Map<String,Object> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String glAccountId = null;
		String accountCode = (String) context.get("accountCode");
		String accountName = (String) context.get("accountName");
		String description = (String) context.get("description");
		String glAccountClassTypeKey = (String) context.get("glAccountClassTypeKey");
		String glResourceTypeId = (String) context.get("glResourceTypeId");
		String parentGlAccountId = (String) context.get("parentGlAccountId");
		Double postedBalance = (Double) context.get("postedBalance");
		String organizationPartyId = (String) context.get("organizationPartyId");
		String tipoCuentaId = (String) context.get("tipoCuentaId");
		String siglas = (String) context.get("siglas");
		String newAccount = (String) context.get("newAccount");

		List<GenericValue> value = null;

		Map<String,String> fields = UtilMisc.toMap("accountCode", accountCode);

		try {
			// check whether the account code is already present
			value = delegator.findByAnd("GlAccount", fields);
			if (!value.isEmpty()) {
				return ServiceUtil.returnError("The account code specified ["
								+ accountCode
								+ "] is already associated with an existing Gl Account.");
			} else {
				glAccountId = accountCode;
			}

			// Obteniendo Nivel.
			int nivel = 1;
			for (int i = 0; i < glAccountId.length(); i++) {
				if (glAccountId.charAt(i) == '.') {
					nivel++;
				}
			}
			String nivelId = nivel + "";
			value = delegator.findByAnd("NivelContable",UtilMisc.toMap("nivelId", nivelId));

			if (value.isEmpty()) {
				return ServiceUtil.returnError("No existe nivel.");
			}

			// Validar nivel del padre
			int nivelPadre = 0;
			if (parentGlAccountId != null)
			{
				nivelPadre = 1;
				for (int i = 0; i < parentGlAccountId.length(); i++)
				{
					if (parentGlAccountId.charAt(i) == '.')
					{
						nivelPadre++;
					}
				}
			}

			if (nivelPadre > 0)
			{
				if (nivel != (nivelPadre + 1))
				{
					return ServiceUtil.returnError("El nivel contable no es el adecuado");
				}
			}

			// extract glAccountClassId and glAccountTypeId from
			// GlAccountClassType
			GenericValue GlAccountClassTypeMap = delegator.findByPrimaryKeyCache(
					"GlAccountClassTypeMap", UtilMisc.toMap(
							"glAccountClassTypeKey", glAccountClassTypeKey));

			String glAccountTypeId = GlAccountClassTypeMap.getString("glAccountTypeId");
			String glAccountClassId = GlAccountClassTypeMap.getString("glAccountClassId");

			context.put("glAccountTypeId", glAccountTypeId);
			context.put("glAccountClassId", glAccountClassId);
			
			// Add a new Gl Account
			Map<String,Object> addNewGlAccountContext = UtilMisc.toMap("glAccountId",glAccountId, 
					"accountCode", accountCode, "accountName", accountName, 
					"description", description, "glAccountTypeId",glAccountTypeId,
					"glAccountClassId", glAccountClassId);
			addNewGlAccountContext.put("glResourceTypeId", glResourceTypeId);
			addNewGlAccountContext.put("parentGlAccountId", parentGlAccountId);
			addNewGlAccountContext.put("postedBalance", postedBalance);
			addNewGlAccountContext.put("tipoCuentaId", tipoCuentaId);
			addNewGlAccountContext.put("siglas", siglas);
			addNewGlAccountContext.put("nivelId", nivelId);
			addNewGlAccountContext.put("userLogin", userLogin);

			if(newAccount == null){
				// Validando padre.
				if (parentGlAccountId != null) {
					if (!tipoCuentaId.equalsIgnoreCase("R")) {
						return ServiceUtil
								.returnError("Una cuenta hija solo puede ser de tipo Registro.");
					}
					GenericValue GlAccountPadre = delegator.findByPrimaryKey("GlAccount",UtilMisc.toMap("glAccountId", parentGlAccountId));
					String tipoCuentaIdPadre = GlAccountPadre.getString("tipoCuentaId");
					if (tipoCuentaIdPadre.equalsIgnoreCase("R")) {
						// Se cambia el tipo del padre de Registro a Acumulativa.
						GlAccountPadre.put("tipoCuentaId", "A");
						delegator.store(GlAccountPadre);
					}
				} else {
					if (!tipoCuentaId.equalsIgnoreCase("T") || nivel > 1) {
						return ServiceUtil
								.returnError("El tipo de cuenta no corresponde al nivel de la cuenta.");
					}
				}
			}
			
			Map<String,Object> addNewGlAccountResult = dispatcher.runSync("createGlAccount",addNewGlAccountContext, -1, false);
			if (ServiceUtil.isError(addNewGlAccountResult)) {
				return addNewGlAccountResult;
			}
			
			if(newAccount == null){
				GenericValue padreGl = delegator.findByPrimaryKey("GlAccountOrganization",
						UtilMisc.toMap("glAccountId", parentGlAccountId, "organizationPartyId", organizationPartyId));
				Debug.log("Mike.- padre: " + padreGl);
				if(padreGl.getBigDecimal("postedBalance") != null){
					postedBalance = padreGl.getBigDecimal("postedBalance").doubleValue();
					Debug.logInfo("Mike.- postedBalance: " + postedBalance,MODULE);
					padreGl.put("postedBalance", 0);
					delegator.store(padreGl);
					
					//Se crea un nuevo registro en GlAccountOrganization
					GenericValue GLAOrganizationNuevo = delegator.makeValue("GlAccountOrganization");
					GLAOrganizationNuevo.put("glAccountId",glAccountId);
					GLAOrganizationNuevo.put("organizationPartyId",organizationPartyId);
					GLAOrganizationNuevo.put("fromDate",UtilDateTime.nowTimestamp());
					GLAOrganizationNuevo.put("postedBalance",BigDecimal.valueOf(postedBalance));
					delegator.create(GLAOrganizationNuevo);
					
					Debug.log("Entra a cambiar los history");
	            	EntityCondition condicionGLAHistory = EntityCondition.makeCondition(EntityOperator.AND, 
	    					EntityCondition.makeCondition("glAccountId", EntityOperator.EQUALS, parentGlAccountId));

	    			Debug.log("Mike condiciones: " + condicionGLAHistory);

	    			List<GenericValue> listGLAHistory = delegator.findByCondition("GlAccountHistory", condicionGLAHistory,	null, null);
	    			
	    			Debug.log("GlAccountHistory: " + listGLAHistory);
	    			
	    			for (GenericValue GLAHistoryOriginal : listGLAHistory) {
	    				GenericValue glAccountHistoryNew = GenericValue.create(delegator.getModelEntity("GlAccountHistory"));
            			glAccountHistoryNew.set("glAccountId", glAccountId);
            			glAccountHistoryNew.set("organizationPartyId",GLAHistoryOriginal.getString("organizationPartyId"));
            			glAccountHistoryNew.set("customTimePeriodId",GLAHistoryOriginal.getString("customTimePeriodId"));
            			glAccountHistoryNew.set("postedCredits", GLAHistoryOriginal.getBigDecimal("postedCredits"));
            			glAccountHistoryNew.set("postedDebits", GLAHistoryOriginal.getBigDecimal("postedDebits"));
        				delegator.create(glAccountHistoryNew);
        				
        				GLAHistoryOriginal.set("postedDebits", BigDecimal.ZERO);
        				GLAHistoryOriginal.set("postedCredits", BigDecimal.ZERO);
        				delegator.store(GLAHistoryOriginal);
        				
					}
	    			
	    			Debug.log("Entra a cambiar las entries");

	    			List<GenericValue> listAcctgTransEntry = delegator.findByCondition("AcctgTransEntry", condicionGLAHistory,	null, null);
	    			
	    			Debug.log("AcctgTransEntry: " + listAcctgTransEntry);
	    			
	    			for (GenericValue AcctgTransEntry : listAcctgTransEntry) {
	    				AcctgTransEntry.set("glAccountId", glAccountId); 
            			delegator.store(AcctgTransEntry);
					}
	    			
				}

			}
			
			return ServiceUtil.returnSuccess();
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError("Could not add the Gl Account ("
					+ e.getMessage() + ").");
		} catch (GenericServiceException e) {
			return ServiceUtil.returnError("Could not add the Gl Account ("
					+ e.getMessage() + ").");
		}
	}
	
	public static Map cambiarEstructura(DispatchContext dctx, Map context) throws GenericEntityException, GenericServiceException {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String glAccountId = (String) context.get("glAccountId");
		String organizationPartyId = (String) context.get("organizationPartyId");
		String newGlAccountId = (String) context.get("newGlAccountId");
		String cuentaPredecerora = (String) context.get("cuentaPredecerora");
		
		Debug.log("mike glAccountId "+glAccountId);
		Debug.log("mike newGlAccountId "+newGlAccountId);
		
		//se obtiene el nivel de la cuenta existente
		GenericValue cuenta = delegator.findByPrimaryKeyCache(
				"GlAccount", UtilMisc.toMap(
						"glAccountId", glAccountId));
		String nivelId = cuenta.getString("nivelId");
		
		//se verifica que la nueva cuenta tenga el mismo nivel
		int nivel = 1;
		for (int i = 0; i < newGlAccountId.length(); i++) {
			if (newGlAccountId.charAt(i) == '.') {
				nivel++;
			}
		}
		String newNivelId = nivel + "";
		if(!nivelId.equals(newNivelId)){
			return ServiceUtil
					.returnError("La cuenta que ingresas no contiene la misma estructura (niveles contables).");
		}
		
		//verifica que no se haya cambiado la estructura de los niveles predecesores
		if(cuentaPredecerora == null){
			nivel = 1;
			if(Integer.parseInt(nivelId)>1){
				for (int i = 0; i < newGlAccountId.length(); i++) {
					if(glAccountId.charAt(i) != newGlAccountId.charAt(i)){
						return ServiceUtil
								.returnError("La cuenta ingresada ha cambiado en los nivles anteriores");
					}
					if (glAccountId.charAt(i) == '.') {
						nivel++;
					}
					if(nivel == Integer.parseInt(nivelId)){
						break;
					}
				}
			}
		}
		
		// remove the GL Account by setting the thru date to now date
		Map updateGlAccountOrganizationContext = UtilMisc
				.toMap("glAccountId", glAccountId, "organizationPartyId",
						organizationPartyId, "thruDate",
						UtilDateTime.nowTimestamp(), "userLogin", userLogin);
		Map updateGlAccountOrganizationResult = dispatcher.runSync(
				"updateGlAccountOrganization",
				updateGlAccountOrganizationContext);
		if (ServiceUtil.isError(updateGlAccountOrganizationResult)) {
			return updateGlAccountOrganizationResult;
		}
		
		//creamos la nueva cuenta
		Map addNewGlAccountContext = UtilMisc
				.toMap("accountCode", newGlAccountId, "organizationPartyId", organizationPartyId,
						"accountName", cuenta.getString("accountName"), "glAccountClassTypeKey", cuenta.getString("glAccountClassId"),
						"parentGlAccountId", cuenta.getString("parentGlAccountId"), "tipoCuentaId", cuenta.getString("tipoCuentaId"),
						"newAccount", "Y", "userLogin", userLogin);
		Map addNewGlAccountResult = dispatcher.runSync(
				"addNewGlAccount", addNewGlAccountContext);
		if (ServiceUtil.isError(addNewGlAccountResult)) {
			return addNewGlAccountResult;
		}

		//buscamos si tiene hijos
		List<GenericValue> hijos = delegator.findByAnd("GlVigentes",
				UtilMisc.toMap("organizationPartyId", organizationPartyId,
						"parentGlAccountId", glAccountId,
						"thruDate", null));

		//verificamos el tamanio de las cuentas 
		int tamGl = glAccountId.length();
		int tamNewGl = newGlAccountId.length();
		int tam = tamNewGl - tamGl;
		
		//creamos la nueva estructura en las cuentas hijas haciendo recursividad
		if(hijos.size()>0){
			//nivel = nivel+1;
			Iterator<GenericValue> hijosIter = hijos.iterator();
			while (hijosIter.hasNext()) 
			{	GenericValue generic = hijosIter.next();
				generic.put("parentGlAccountId", newGlAccountId);
				delegator.store(generic);
				
				String cuentaHija = generic.getString("glAccountId");

				String nuevaCuentaHija = "";
				int nivelHijo = 1;
				for (int i = 0; i < (cuentaHija.length()+tam); i++) {
					if (i<tamNewGl && newGlAccountId.charAt(i) == '.') {
						nivelHijo++;
					}
					if(nivel>=nivelHijo && i<tamNewGl){
						nuevaCuentaHija = nuevaCuentaHija + newGlAccountId.charAt(i);
					}else{
						nuevaCuentaHija = nuevaCuentaHija + cuentaHija.charAt(i-tam);
					}
				}
				
				Map cuentaHijaContext = UtilMisc.toMap("userLogin", userLogin, "glAccountId", cuentaHija,
						"organizationPartyId", organizationPartyId, "newGlAccountId", nuevaCuentaHija,
						"cuentaPredecerora", "Y");
				Debug.log("Mike cuentaHijaContetx: "+cuentaHijaContext);
				Map addCuentaHijaResult = dispatcher.runSync(
						"cambiarEstructura", cuentaHijaContext);
				if (ServiceUtil.isError(addCuentaHijaResult)) {
					return addCuentaHijaResult;
				}
			}
		}
		
		//creamos registro de quien hizo el cambio de la cuenta
		GenericValue cambio = GenericValue.create(delegator.getModelEntity("GlAccountStructure"));
		cambio.set("idStructure", delegator.getNextSeqId("GlAccountStructure"));
		cambio.set("glAccountIdAnterior", glAccountId);
		cambio.set("glAccountIdNueva", newGlAccountId);
		cambio.set("userLoginId", userLogin.getString("userLoginId"));
		delegator.create(cambio);
		
		//cambiamos todos os valores de la cuenta anterior a la cuenta nueva
				if(cuenta.getString("tipoCuentaId").equals("R")){
					
					//se obtiene el registro de la nueva cuenta
					GenericValue cuentaNueva = delegator.findByPrimaryKeyCache(
							"GlAccount", UtilMisc.toMap(
									"glAccountId", newGlAccountId));
					
					//obtenemos el valor que tenia la cuenta anterior en GlAccountOrganization
					GenericValue registroGl = delegator.findByPrimaryKey("GlAccountOrganization",
							UtilMisc.toMap("glAccountId", glAccountId, "organizationPartyId", organizationPartyId));
					Debug.log("Mike.- cuenta registro: " + registroGl);
					
					//obtenemos el valor que tenia la cuenta nueva en GlAccountOrganization
					GenericValue nuevaGl = delegator.findByPrimaryKey("GlAccountOrganization",
							UtilMisc.toMap("glAccountId", newGlAccountId, "organizationPartyId", organizationPartyId));
					Debug.log("Mike.- cuenta nueva: " + nuevaGl);
					
					//en caso de obtner un valor se lo ingresamos 
					if(registroGl.getBigDecimal("postedBalance") != null){
						Double postedBalance = registroGl.getBigDecimal("postedBalance").doubleValue();
						Debug.log("Mike.- postedBalance: " + postedBalance);
						nuevaGl.put("postedBalance", postedBalance);
						delegator.store(nuevaGl);

						//cambia los history
						Debug.log("Entra a cambiar los history");
		            	EntityCondition condicion1 = EntityCondition.makeCondition(EntityOperator.AND, 
		    					EntityCondition.makeCondition("glAccountId", EntityOperator.EQUALS, glAccountId));
		    			Debug.log("Mike condiciones: " + condicion1);
		    			List<GenericValue> busqueda = delegator.findByCondition("GlAccountHistory", condicion1,	null, null);
		    			Debug.log("GlAccountHistory: " + busqueda);
		    
		    			//si encontrqamos resultados cambiamos todos los histories
		    			if(!busqueda.isEmpty()){
		    				
		    				Iterator<GenericValue> cuentaId = busqueda.iterator();
		    				while (cuentaId.hasNext()) {
		    					GenericValue cuentaH = cuentaId.next();

		    					//creamos los nuevos registros
		            			GenericValue glAccountHistoryNew = GenericValue.create(delegator
		        						.getModelEntity("GlAccountHistory"));
		            			glAccountHistoryNew.set("glAccountId", newGlAccountId);
		            			glAccountHistoryNew.set("organizationPartyId",
		            					cuentaH.getString("organizationPartyId"));
		            			glAccountHistoryNew.set("customTimePeriodId",
		            					cuentaH.getString("customTimePeriodId"));
		            			if (cuentaH.getBigDecimal("postedCredits") != null) {
		            				glAccountHistoryNew.set("postedCredits", cuentaH.getBigDecimal("postedCredits"));
		        				}
		            			if (cuentaH.getBigDecimal("postedDebits") != null) {
		            				glAccountHistoryNew.set("postedDebits", cuentaH.getBigDecimal("postedDebits"));
		        				}
		        				delegator.create(glAccountHistoryNew);

		        				//borramos el history anterior
								delegator.removeByAnd("GlAccountHistory", UtilMisc.toMap(
											"glAccountId", glAccountId, "organizationPartyId",
											organizationPartyId, "customTimePeriodId", 
											cuentaH.getString("customTimePeriodId")));
		    					
		    				}
		    			}
		    			
		    			//cambiamos las polizas
		    			Debug.log("Entra a cambiar las entries");
		    			List<GenericValue> busqueda2 = delegator.findByCondition("AcctgTransEntry", condicion1,	null, null);
		    			Debug.log("AcctgTransEntry: " + busqueda2);
		    			
		    			//si hay resultados cambiamos las polizas
		    			if(!busqueda2.isEmpty()){
		    				Iterator<GenericValue> entryId = busqueda2.iterator();
		    				while (entryId.hasNext()) {
		    					GenericValue entry = entryId.next();
		            			
		            			GenericValue entryN = GenericValue.create(delegator
		        						.getModelEntity("AcctgTransEntry"));
		            			entryN.set("acctgTransId", entry.getString("acctgTransId"));
		            			entryN.set("acctgTransEntrySeqId", entry.getString("acctgTransEntrySeqId"));
		            			entryN.set("organizationPartyId", entry.getString("organizationPartyId"));
		            			entryN.set("glAccountId", newGlAccountId); 
		            			delegator.create(entryN);
		    				}
		    			}
		    			
		    			//cambiamos los detalles de las acctg trans
		    			Debug.log("Entra a cambiar los detalles");
		    			List<GenericValue> busqueda3 = delegator.findByCondition("DetalleAcctgTrans", condicion1, null, null);
		    			Debug.log("DetalleAcctgTrans: " + busqueda3);
		    			
		    			//si hay resultados cambiamos los detalles
		    			if(!busqueda3.isEmpty()){
		    				Iterator<GenericValue> entryId = busqueda3.iterator();
		    				while (entryId.hasNext()) {
		    					GenericValue entry = entryId.next();
		            			
		            			GenericValue entryN = GenericValue.create(delegator
		        						.getModelEntity("DetalleAcctgTrans"));
		            			entryN.set("acctgTransId", entry.getString("acctgTransId"));
		            			entryN.set("acctgTransEntryId", entry.getString("acctgTransEntryId"));
		            			entryN.set("glAccountId", newGlAccountId); 
		            			delegator.create(entryN);
		    				}
		    			}

		    			//comenzamos con Linea Contable
		    			EntityCondition condicionCuentasL = EntityCondition.makeCondition(EntityOperator.OR, 
		    					EntityCondition.makeCondition("cuentaCargo", EntityOperator.EQUALS, glAccountId),
		    					EntityCondition.makeCondition("cuentaAbono", EntityOperator.EQUALS, glAccountId),
		    					EntityCondition.makeCondition("cuentaCancelar", EntityOperator.EQUALS, glAccountId));
		    			List<GenericValue> busqueda4 = delegator.findByCondition("LineaContable", condicionCuentasL, null, null);
		    			
		    			//si hay resultados lo cambiamos
		    			if(!busqueda4.isEmpty()){
		    				Iterator<GenericValue> entryId = busqueda4.iterator();
		    				while (entryId.hasNext()) {
		    					GenericValue entry = entryId.next();
		            			
		            			GenericValue entryN = GenericValue.create(delegator
		        						.getModelEntity("LineaContable"));
		            			entryN.set("acctgTransTypeId", entry.getString("acctgTransTypeId"));
		            			entryN.set("secuencia", entry.getString("secuencia"));
		            			if(entry.getString("cuentaCargo") != null && entry.getString("cuentaCargo").equals(glAccountId)){
		            				entryN.set("cuentaCargo", newGlAccountId);
		            			}
		            			if(entry.getString("cuentaAbono") != null && entry.getString("cuentaAbono").equals(glAccountId)){
		            				entryN.set("cuentaAbono", newGlAccountId);
		            			}
		            			if(entry.getString("cuentaCancelar") != null && entry.getString("cuentaCancelar").equals(glAccountId)){
		            				entryN.set("cuentaCancelar", newGlAccountId);
		            			}
		            			
		            			delegator.create(entryN);
		    				}
		    			}
		    			
		    			//Seguimos con Linea Presupuestal
		    			EntityCondition condicionCuentasP = EntityCondition.makeCondition(EntityOperator.OR, 
		    					EntityCondition.makeCondition("cuentaCargo", EntityOperator.EQUALS, glAccountId),
		    					EntityCondition.makeCondition("cuentaAbono", EntityOperator.EQUALS, glAccountId));
		    			List<GenericValue> busqueda5 = delegator.findByCondition("LineaPresupuestal", condicionCuentasP, null, null);
		    			
		    			//si hay resultados lo cambiamos
		    			if(!busqueda5.isEmpty()){
		    				Iterator<GenericValue> entryId = busqueda4.iterator();
		    				while (entryId.hasNext()) {
		    					GenericValue entry = entryId.next();
		            			
		            			GenericValue entryN = GenericValue.create(delegator
		        						.getModelEntity("LineaPresupuestal"));
		            			entryN.set("acctgTransTypeId", entry.getString("acctgTransTypeId"));
		            			entryN.set("secuencia", entry.getString("secuencia"));
		            			if(entry.getString("cuentaCargo") != null && entry.getString("cuentaCargo").equals(glAccountId)){
		            				entryN.set("cuentaCargo", newGlAccountId);
		            			}
		            			if(entry.getString("cuentaAbono") != null && entry.getString("cuentaAbono").equals(glAccountId)){
		            				entryN.set("cuentaAbono", newGlAccountId);
		            			}

		            			delegator.create(entryN);
		    				}
		    			}
					}

				}
				
				//borramos glAccountOrganization
				delegator.removeByAnd("GlAccountOrganization", UtilMisc.toMap(
							"glAccountId", glAccountId));
				
				//borramos la cuenta anterior
				delegator.removeByAnd("GlAccount", UtilMisc.toMap(
							"glAccountId", glAccountId));
		
		return result;
	}

	/**
	 * Update a GL Account taking as input a GlAccountClassTypeKey.
	 * 
	 * @param dctx
	 *            a <code>DispatchContext</code> value
	 * @param context
	 *            a <code>Map</code> value
	 * @return a service response <code>Map</code> value
	 */
	@SuppressWarnings("unchecked")
	public static Map updateExistingGlAccount(DispatchContext dctx, Map context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		String glAccountClassTypeKey = (String) context
				.get("glAccountClassTypeKey");
		context.remove("glAccountClassTypeKey");

		try {
			// extract glAccountClassId and glAccountTypeId from
			// GlAccountClassType
			GenericValue gv = delegator.findByPrimaryKeyCache(
					"GlAccountClassTypeMap", UtilMisc.toMap(
							"glAccountClassTypeKey", glAccountClassTypeKey));

			String glAccountTypeId = gv.getString("glAccountTypeId");
			String glAccountClassId = gv.getString("glAccountClassId");

			context.put("glAccountTypeId", glAccountTypeId);
			context.put("glAccountClassId", glAccountClassId);

			// forward to the original updateGlAccount service
			return dispatcher.runSync("updateGlAccount", context, -1, false);
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError("Could not update the Gl Account ("
					+ e.getMessage() + ").");
		} catch (GenericServiceException e) {
			return ServiceUtil.returnError("Could not update the Gl Account ("
					+ e.getMessage() + ").");
		}
	}

	/**
	 * Update PartyAcctgPreference for an organization.
	 * 
	 * @param dctx
	 *            a <code>DispatchContext</code> value
	 * @param context
	 *            a <code>Map</code> value
	 * @return a service response <code>Map</code> value
	 */
	@SuppressWarnings("unchecked")
	public static Map updatePartyAcctgPreference(DispatchContext dctx,
			Map context) {
		Delegator delegator = dctx.getDelegator();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		Locale locale = UtilCommon.getLocale(context);
		Security security = dctx.getSecurity();
		String partyId = (String) context.get("partyId");

		if (!security.hasEntityPermission("FINANCIALS", "_CONFIG", userLogin)) {
			return ServiceUtil.returnError(UtilProperties.getMessage(
					"FinancialsUiLabels", "FinancialsServiceErrorNoPermission",
					locale));
		}

		try {
			GenericValue partyAcctgPreference = delegator.findByPrimaryKey(
					"PartyAcctgPreference", UtilMisc.toMap("partyId", partyId));
			if (UtilValidate.isEmpty(partyAcctgPreference)) {
				return ServiceUtil.returnError(UtilProperties.getMessage(
						"FinancialsUiLabels",
						"FinancialsServiceErrorPartyAcctgPrefNotFound",
						context, locale));
			}
			partyAcctgPreference.setNonPKFields(context, true);
			partyAcctgPreference.store();

		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(UtilProperties.getMessage(
					"FinancialsUiLabels",
					"FinancialsServiceErrorUpdatingPartyAcctgPref", context,
					locale));
		}
		return ServiceUtil.returnSuccess();
	}

	/**
	 * Create or Update a GlAccount record for an organization.
	 * 
	 * @param dctx
	 *            a <code>DispatchContext</code> value
	 * @param context
	 *            a <code>Map</code> value
	 * @return a service response <code>Map</code> value
	 */
	@SuppressWarnings("unchecked")
	public static Map addGlAccountToOrganization(DispatchContext dctx,
			Map context) {

		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");

		// Mandatory input fields
		String glAccountId = (String) context.get("glAccountId");
		String organizationPartyId = (String) context
				.get("organizationPartyId");

		try {
			Map input = UtilMisc.toMap("glAccountId", glAccountId,
					"organizationPartyId", organizationPartyId);
			GenericValue glAccountOrganization = delegator.findByPrimaryKey(
					"GlAccountOrganization", input);
			if (glAccountOrganization == null) {
				return dispatcher.runSync("createGlAccountOrganization",
						context);
			} else {
				input = UtilMisc.toMap("userLogin", userLogin);
				input.put("glAccountId", glAccountId);
				input.put("organizationPartyId", organizationPartyId);
				input.put("fromDate", UtilDateTime.nowTimestamp());
				input.put("thruDate", null);
				return dispatcher.runSync("updateGlAccountOrganization", input);
			}

		} catch (GeneralException e) {
			return UtilMessage.createAndLogServiceError(e, MODULE);
		}
	}

	/**
	 * Creates an accounting tag Enumeration record.
	 * 
	 * @param dctx
	 *            a <code>DispatchContext</code> value
	 * @param context
	 *            a <code>Map</code> value
	 * @return a service response <code>Map</code> value
	 */
	@SuppressWarnings("unchecked")
	public static Map createAccountingTag(DispatchContext dctx, Map context)
			throws ParseException {
		Delegator delegator = dctx.getDelegator();
		String nivelId = (String) context.get("nivelId");
		String glAccountTypeId = (String) context.get("enumTypeId");
		String fechaIn = (String) context.get("fechaIni");
		String fechaF = (String) context.get("fechaFi");
		SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yy");
		Date fecInicio;
		Date fecFinal;
		fecFinal = formatoFecha.parse(fechaF);
		fecInicio = formatoFecha.parse(fechaIn);
		context.put("fechaFin", fecFinal);
		context.put("fechaInicio", fecInicio);

		try {
			if (glAccountTypeId.equals(" ")) {
				return UtilMessage.createAndLogServiceError(
						"El parametro Tipo  no puede estar vacio", MODULE);
			}
			if (nivelId.equals(" ")) {
				return UtilMessage.createAndLogServiceError(
						"El parametro Nivel no puede estar vacio", MODULE);
			}

			String enumId = (String) context.get("enumId");
			GenericValue enumeration = delegator.makeValue("Enumeration");
			// if an id was given, check for duplicate
			if (UtilValidate.isNotEmpty(enumId)) {
				enumeration.setPKFields(context);
				GenericValue exists = delegator.findByPrimaryKey("Enumeration",
						enumeration);
				if (exists != null) {
					return UtilMessage.createAndLogServiceError(
							"An Accounting tag Enumeration already exists with ID ["
									+ enumId + "]", MODULE);
				}
			}

			// Validate parent
			String parentEnumId = (String) context.get("parentEnumId");
			if (UtilValidate.isNotEmpty(parentEnumId)) {
				Debug.log("Tiene padre: " + parentEnumId);
				GenericValue parent = delegator.makeValue("Enumeration");
				parent.set("enumId", parentEnumId);
				GenericValue exists = delegator.findByPrimaryKey("Enumeration",
						parent);

				if (exists != null) {
					Debug.log("Existe padre");
					String nivelHijo = (String) context.get("nivelId");
					Debug.log("Nivel hijo: " + nivelHijo);
					String nivelPadre = (String) exists.get("nivelId");
					Debug.log("Nivel padre: " + nivelPadre);

					Map fields = UtilMisc.toMap("nivelId", nivelHijo,
							"nivelPadreId", nivelPadre);
					List exist = delegator.findByAnd("NivelPresupuestal",
							fields);

					// else generate the id from the sequence
					enumeration.put("enumId",
							delegator.getNextSeqId("Enumeration"));
					Debug.log("Se valida Nivel");
					if (exist.isEmpty()) {
						Debug.log("Padre incorrecto");
						return ServiceUtil.returnError("Padre no Valido");
					}
					Debug.log("Padre Correcto");
				} else {
					Debug.log("Padre incorrecto");
					return ServiceUtil.returnError("Padre no Valido");
				}
			}

			// else generate the id from the sequence
			enumeration.put("enumId", delegator.getNextSeqId("Enumeration"));

			enumeration.setNonPKFields(context);
			delegator.create(enumeration);
			return ServiceUtil.returnSuccess();

		} catch (GeneralException e) {
			return UtilMessage.createAndLogServiceError(e, MODULE);
		}
	}

	/**
	 * Updates an accounting tag Enumeration record.
	 * 
	 * @param dctx
	 *            a <code>DispatchContext</code> value
	 * @param context
	 *            a <code>Map</code> value
	 * @return a service response <code>Map</code> value
	 * @throws ParseException
	 */
	@SuppressWarnings("unchecked")
	public static Map updateAccountingTag(DispatchContext dctx, Map context)
			throws ParseException {
		Delegator delegator = dctx.getDelegator();
		String nivelId = (String) context.get("niv");
		String inicio = (String) context.get("fechaIn");
		String fin = (String) context.get("fechaF");
		String nivel = "";
		String sCadenaSinBlancos = "";
		String cadena1 = "";
		String cadena2 = "";

		try {
			SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yy");
			Date fecInicio;
			Date fecFinal;

			GenericValue pk = delegator.makeValue("Enumeration");
			pk.setPKFields(context);
			GenericValue enumeration = delegator.findByPrimaryKey(
					"Enumeration", pk);
			if (enumeration == null) {
				return UtilMessage.createAndLogServiceError(
						"Did not find Accounting tag Enumeration value for PK ["
								+ pk.getPrimaryKey() + "]", MODULE);
			}
			for (int x = 0; x < nivelId.length(); x++) {
				if ((nivelId.charAt(x) != ' ') && (nivelId.charAt(x) != '[')
						&& (nivelId.charAt(x) != ']'))
					sCadenaSinBlancos += nivelId.charAt(x);
			}

			StringTokenizer st = new StringTokenizer(sCadenaSinBlancos, ",");
			while (st.hasMoreTokens()) {
				String Nivel1 = st.nextToken();
				String Nivel2 = st.nextToken();
				if (!(Nivel1.equals(Nivel2)))
					nivel = Nivel1;
			}
			if (!(nivel.equals("")))
				context.put("nivelId", nivel);
			inicio = inicio.replace("[", "").replace("]", "").replace(" ", "");

			StringTokenizer st1 = new StringTokenizer(inicio, ",");
			while (st1.hasMoreTokens()) {
				String FI1 = st1.nextToken();
				String FI2 = st1.nextToken();
				if (!(FI1.equals(FI2)))
					cadena1 = FI1;
			}
			if (!(cadena1.isEmpty())) {
				fecInicio = formatoFecha.parse(cadena1);
				context.put("fechaInicio", fecInicio);
			}
			fin = fin.replace("[", "").replace("]", "").replace(" ", "");
			StringTokenizer st2 = new StringTokenizer(fin, ",");
			while (st2.hasMoreTokens()) {
				String FF1 = st2.nextToken();
				String FF2 = st2.nextToken();
				if (!(FF1.equals(FF2)))
					cadena2 = FF1;
			}
			if (!(cadena2.isEmpty())) {
				fecFinal = formatoFecha.parse(cadena2);
				context.put("fechaFin", fecFinal);
			}
			enumeration.setNonPKFields(context);
			delegator.store(enumeration);
			return ServiceUtil.returnSuccess();
		} catch (GeneralException e) {
			return UtilMessage.createAndLogServiceError(e, MODULE);
		}
	}

	/**
	 * Deletes an accounting tag Enumeration record, this will only works if the
	 * tag is not in use in any other entity.
	 * 
	 * @param dctx
	 *            a <code>DispatchContext</code> value
	 * @param context
	 *            a <code>Map</code> value
	 * @return a service response <code>Map</code> value
	 */
	@SuppressWarnings("unchecked")
	public static Map deleteAccountingTag(DispatchContext dctx, Map context) {
		Delegator delegator = dctx.getDelegator();
		Locale locale = UtilCommon.getLocale(context);

		try {
			GenericValue pk = delegator.makeValue("Enumeration");
			pk.setPKFields(context);
			// get the tag, although not really necessary, in case there is a FK
			// error we can use this to display a better error message (fetching
			// after the error won't work because the transaction rolled back)
			GenericValue tag = delegator.findByPrimaryKey("Enumeration",
					pk.getPrimaryKey());
			try {
				delegator.removeByPrimaryKey(pk.getPrimaryKey());
				return ServiceUtil.returnSuccess();
			} catch (GenericDataSourceException e) {
				// this happens for a FK error
				return UtilMessage.createAndLogServiceError(
						"FinancialsError_CannotDeleteInUseAccoutingTag",
						UtilMisc.toMap("tag", tag), locale, MODULE);
			}

		} catch (GeneralException e) {
			return UtilMessage.createAndLogServiceError(e, MODULE);
		}
	}

	/**
	 * Updates or creates an accounting tag usage record for an organization.
	 * 
	 * @param dctx
	 *            a <code>DispatchContext</code> value
	 * @param context
	 *            a <code>Map</code> value
	 * @return a service response <code>Map</code> value
	 */
	@SuppressWarnings("unchecked")
	public static Map updateAccountingTagUsage(DispatchContext dctx, Map context) {
		Delegator delegator = dctx.getDelegator();

		try {
			GenericValue pk = delegator.makeValue("AcctgTagEnumType");
			pk.setPKFields(context);
			GenericValue usage = delegator.findByPrimaryKey("AcctgTagEnumType",
					pk);
			if (usage == null) {
				usage = pk;
			}
			usage.setNonPKFields(context);
			delegator.createOrStore(usage);
			return ServiceUtil.returnSuccess();

		} catch (GeneralException e) {
			return UtilMessage.createAndLogServiceError(e, MODULE);
		}
	}

	/**
	 * Updates or creates the accounting tag posting check record for an
	 * organization.
	 * 
	 * @param dctx
	 *            a <code>DispatchContext</code> value
	 * @param context
	 *            a <code>Map</code> value
	 * @return a service response <code>Map</code> value
	 */
	@SuppressWarnings("unchecked")
	public static Map updateAccountingTagPostingCheck(DispatchContext dctx,
			Map context) {
		Delegator delegator = dctx.getDelegator();

		try {
			GenericValue pk = delegator.makeValue("AcctgTagPostingCheck");
			pk.setPKFields(context);
			GenericValue postingCheck = delegator.findByPrimaryKey(
					"AcctgTagPostingCheck", pk);
			if (postingCheck == null) {
				postingCheck = pk;
			}
			postingCheck.setNonPKFields(context);
			delegator.createOrStore(postingCheck);
			return ServiceUtil.returnSuccess();

		} catch (GeneralException e) {
			return UtilMessage.createAndLogServiceError(e, MODULE);
		}
	}

	/**
	 * Crea o actualiza la estructura de las clasificaciones
	 * 
	 * organization.
	 * 
	 * @param dctx
	 *            a <code>DispatchContext</code> value
	 * @param context
	 *            a <code>Map</code> value
	 * @return a service response <code>Map</code> value
	 */
	@SuppressWarnings("unchecked")
	public static Map updateClassificationTag(DispatchContext dctx, Map context) {

		Debug.log("Entro al servico updateClassificationTag");

		Delegator delegator = dctx.getDelegator();
		Date date = new Date();

		String organizationPartyId = (String) context
				.get("organizationPartyId");
		String acctgTagUsageTypeId = (String) context
				.get("acctgTagUsageTypeId");
		String customTimePeriodYear = (String) context
				.get("customTimePeriodYear");
		Debug.log("organizationPartyId " + organizationPartyId);
		Debug.log("acctgTagUsageTypeId " + acctgTagUsageTypeId);


		// Validacion para verificar si esta repetido
		String resultado1 = "";
		String resultado2 = "";
		String cri = "CL_CRI";
		String cog = "CL_COG";
		boolean tieneCri = false;
		boolean tieneCog = false;
		for (int i = 1; i < 16; i++) {
			resultado1 = (String) context.get("clasificacion" + i);
			if (resultado1 != null) {
				if (resultado1.equals(cog)) {
					if (acctgTagUsageTypeId.equals("INGRESO")) {
						return ServiceUtil
								.returnError("La secci&oacute;n de Ingresos contiene una clasificaci&oacute;n econ&oacute;mica incorrecta (CRI/COG)");
					} else {
						tieneCog = true;
					}
				} else if (resultado1.trim().equals(cri)) {
					if (acctgTagUsageTypeId.equals("EGRESO")) {
						return ServiceUtil
								.returnError("La secci&oacute;n de Egreso contiene una clasificaci&oacute;n econ&oacute;mica incorrecta (CRI/COG)");
					} else {
						tieneCri = true;
					}
				}
				for (int j = i + 1; j < 16; j++) {
					resultado2 = (String) context.get("clasificacion" + j);
					if (resultado2 != null) {
						if (resultado1.equals(resultado2)) {
							return ServiceUtil
									.returnError("Existen valores repetidos");
						} else if (resultado2.equals(cog)) {
							if (acctgTagUsageTypeId.equals("INGRESO")) {
								return ServiceUtil
										.returnError("La secci&oacute;n de Ingresos contiene una clasificaci&oacute;n econ&oacute;mica incorrecta (CRI/COG)");
							} else {
								tieneCog = true;
							}
						} else if (resultado2.trim().equals(cri)) {
							if (acctgTagUsageTypeId.equals("EGRESO")) {
								return ServiceUtil
										.returnError("La secci&oacute;n de Egreso contiene una clasificaci&oacute;n econ&oacute;mica incorrecta (CRI/COG)");
							} else {
								tieneCri = true;
							}
						}
					}
				}
			}
		}
		if (!tieneCri && !tieneCog) {
			Debug.log("No tiene CRI ni COG");
			return ServiceUtil
					.returnError("No contiene clasificaci&oacute;n econ&oacute;mica");
		}


		EntityExpr condicionesPeriodo = EntityCondition.makeCondition("customTimePeriodId", EntityOperator.EQUALS, customTimePeriodYear);
		List<GenericValue> customTimePeriod;
		Date fromDate = new Date();
		int ciclo = 0;
		try {
			customTimePeriod = delegator.findByCondition("CustomTimePeriod", condicionesPeriodo, null, null);
			for(GenericValue periodo : customTimePeriod)
			{
				fromDate = periodo.getDate("fromDate");
			}
			Calendar cal = Calendar.getInstance();
			cal.setTime(fromDate);
			ciclo = cal.get(Calendar.YEAR);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		//		int ciclo = date.getYear() + 1900;
		//Valida si es posible modificar la estructura de la clave, dependiendo si ya existen transacciones en ese ciclo

		try 
		{	DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
		Date fechaCompara = df.parse("01-01-"+ciclo);
		Debug.log("Omar-fechaCompara: " + fechaCompara);

		EntityCondition transaccionesCiclo;
		if(acctgTagUsageTypeId.equals("EGRESO"))
		{	transaccionesCiclo = EntityCondition.makeCondition(EntityOperator.AND, 
				EntityCondition.makeCondition(AcctgTrans.Fields.postedDate.name(),EntityOperator.GREATER_THAN_EQUAL_TO, fechaCompara),
				EntityCondition.makeCondition(AcctgTrans.Fields.tipoPolizaId.name(),EntityOperator.EQUALS, "CP_EGRESO"));				
		}
		else
		{	transaccionesCiclo = EntityCondition.makeCondition(EntityOperator.AND, 
				EntityCondition.makeCondition(AcctgTrans.Fields.postedDate.name(),EntityOperator.GREATER_THAN_EQUAL_TO, fechaCompara),
				EntityCondition.makeCondition(AcctgTrans.Fields.tipoPolizaId.name(),EntityOperator.EQUALS, "CP_INGRESO"));			
		}

		Debug.log("Omar-transaccionesCiclo: " + transaccionesCiclo);
		List<GenericValue> tieneTransaccionesCiclo = delegator.findByCondition("AcctgTrans", transaccionesCiclo,
				UtilMisc.toList("postedDate", "tipoPolizaId"),
				UtilMisc.toList("tipoPolizaId"));
		Debug.log("Omar-tieneTransaccionesCiclo: " + tieneTransaccionesCiclo);

		if(!tieneTransaccionesCiclo.isEmpty())
		{	Debug.log("Ya existen transacciones con esa estructura de clave y el ciclo "+ciclo);
		return ServiceUtil
				.returnError("No es posible actualizar la Estructura de la Clave. Ya existen transacciones con esa Estructura en el ciclo "+ciclo);				
		}



		} catch (GenericEntityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		try {

			Debug.log("Se va a generar entidad");

			GenericValue pk = delegator.makeValue("EstructuraClave");

			EntityCondition condicion = EntityCondition.makeCondition(
					EntityOperator.AND, EntityCondition.makeCondition(
							"organizationPartyId", EntityOperator.EQUALS,
							organizationPartyId),
							EntityCondition.makeCondition("acctgTagUsageTypeId",
									EntityOperator.EQUALS, acctgTagUsageTypeId),
									EntityCondition.makeCondition("ciclo",
											EntityOperator.EQUALS, String.valueOf(ciclo)));

			Debug.log("buscar por ciclo" + ciclo);

			List<GenericValue> listUpdateEstructura = delegator
					.findByCondition("EstructuraClave", condicion, UtilMisc
							.toList("idSecuencia", "organizationPartyId",
									"acctgTagUsageTypeId", "ciclo"), null);

			if (listUpdateEstructura.isEmpty()) {
				Debug.log("No existe el ciclo, registro nuevo");
				pk.setNextSeqId();
				pk.put("ciclo", ciclo);
				pk.setNonPKFields(context);
				delegator.create(pk);

			} else {
				for (GenericValue updateEstructura : listUpdateEstructura) {
					Debug.log("Existe ciclo");
					updateEstructura.setNonPKFields(context);
					Debug.log("Genera update: " + context);
					delegator.store(updateEstructura);
					Debug.log("Genera updateEstructura: " + updateEstructura);
				}
			}

			Debug.log("Return Success");
			context.put("customTimePeriodYear", customTimePeriodYear);
			return ServiceUtil.returnSuccess();

		} catch (GeneralException e) {
			return UtilMessage.createAndLogServiceError(e, MODULE);
		}

	}
	
	/**
	 * Metodo para crear una clave presupuestal
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map<String,Object> crearNuevaClavePres(DispatchContext dctx, Map<String,Object> context) {

		try{
			
		Delegator delegator = dctx.getDelegator();
		String cicloId = (String) context.get("ciclo");
		String organizationPartyId = (String) context.get("organizationPartyId");

		String tipoClave = (String) context.get("ingresoEgreso");
		tipoClave = tipoClave.toUpperCase();

		// Se busca la estructura de la Clave Presupuestaria.
		GenericValue Estructura = UtilClavePresupuestal.obtenEstructPresupuestal(tipoClave, organizationPartyId, delegator, cicloId);
		
		List<GenericValue> listClasifPresupuestal = delegator.findAllCache("ClasifPresupuestal");
		
		Map<String,String> mapaDescClasif = FastMap.newInstance();
		for (GenericValue ClasifPresupuestal : listClasifPresupuestal) {
			mapaDescClasif.put(ClasifPresupuestal.getString("clasificacionId"),ClasifPresupuestal.getString("descripcion"));
		}
		
		/*
		 * Valida que los datos ingresados sean del numero exacto que se
		 * encuentra en la configuracion de la estructura presupuestal y arma la clave presupuestal
		 */
		StringBuffer clavePresupuestal = new StringBuffer();
		String campo = new String();
		String clasificacionId = new String();
		String valorCampo = new String();
		for (int i = 1; i <= UtilClavePresupuestal.TAG_COUNT; i++) {
			campo = UtilClavePresupuestal.VIEW_TAG_PREFIX + i;
			valorCampo = (String) context.get(campo);
			clasificacionId = Estructura.getString(campo);
			if (UtilValidate.isNotEmpty(clasificacionId)) {
				if(UtilValidate.isEmpty(valorCampo)){
					throw new GenericEntityException("Le falto ingresar la "+campo+" : "+mapaDescClasif.get(clasificacionId)+"");
				}
				clavePresupuestal.append(valorCampo); //Se obtiene el valor del componente de la clasificacion'N'
			} else {
				if(UtilValidate.isNotEmpty(valorCampo)){
					throw new GenericEntityException("A superado el n\u00famero de clasificaciones requeridas");
				}
				break;
			}
		}
		clavePresupuestal.trimToSize();
		
		long numExiste = delegator.findCountByAnd("ClavePresupuestal", UtilMisc.toMap("clavePresupuestal", clavePresupuestal.toString(),"tipo", tipoClave));
		if(numExiste >= 1){
			throw new GenericEntityException("La clave presupuestal ya existe");
		}
		
		GenericValue ClavePresupuestal = delegator.makeValue("ClavePresupuestal");
		ClavePresupuestal.set("clavePresupuestal", clavePresupuestal.toString());
		ClavePresupuestal.set("tipo", tipoClave);
		ClavePresupuestal.set("idSecuencia", Estructura.getString("idSecuencia"));
		ClavePresupuestal.setAllFields(context, true, null, null);
		ClavePresupuestal.create();
		
		String mes = null;
			for (int i = 1; i < 13; i++) {
				if(i<10){
					mes = "0" + i;
				}else{
					mes = Integer.toString(i);
				}

				GenericValue ControlPresupuestal = GenericValue.create(delegator
						.getModelEntity("ControlPresupuestal"));
				ControlPresupuestal.setNextSeqId();
				ControlPresupuestal.set("clavePresupuestal", ClavePresupuestal.getString("clavePresupuestal"));
				ControlPresupuestal.set("mesId", mes);
				ControlPresupuestal.set("monto", BigDecimal.ZERO);
				ControlPresupuestal.set("ciclo", cicloId);
				if(tipoClave.equalsIgnoreCase("EGRESO")){
					ControlPresupuestal.set("momentoId", "DISPONIBLE");
					ControlPresupuestal.create();
					ControlPresupuestal.setNextSeqId();
					ControlPresupuestal.set("momentoId", "APROBADO");
					ControlPresupuestal.create();
				}else{
					ControlPresupuestal.set("momentoId", "ESTIMADO");
					ControlPresupuestal.create();
					ControlPresupuestal.setNextSeqId();
					ControlPresupuestal.set("momentoId", "EJECUTAR");
					ControlPresupuestal.create();
				}

			}

		} catch (Exception e){
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}

		return ServiceUtil.returnSuccess("Se ha creado la clave presupuestal con \u00e9xito");
	}
	
	/**
	 * Desactiva una clave presupuestal
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String,Object> desactivarClavePres(DispatchContext dctx, Map<String,Object> context)
	{
		Delegator delegator = dctx.getDelegator();
		String clavePresupuestal = (String) context.get("clavePresupuestal");

		String tipoClave = (String) context.get("tipoClave");
		List<GenericValue> listControlP = new ArrayList<GenericValue>();

		try {
			
			List<String> fieldsToSelect = UtilMisc.toList("monto");
			List<String> momentosId = FastList.newInstance();
			if(tipoClave.equalsIgnoreCase("INGRESO")){
				momentosId = UtilClavePresupuestal.MOMENTO_TEMP_I;
			}else{
				momentosId = UtilClavePresupuestal.MOMENTO_TEMP_E;
			}
			
			EntityCondition condicionesControlP = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("clavePresupuestal", EntityOperator.EQUALS, clavePresupuestal),
					EntityCondition.makeCondition("momentoId", EntityOperator.NOT_IN, momentosId));
			
			listControlP = delegator.findByCondition("CtrlPresupuestalSuma", condicionesControlP, fieldsToSelect, null);
			
			BigDecimal montoControlP = BigDecimal.ZERO;
			for(GenericValue controlP : listControlP)
			{
				montoControlP = montoControlP.add((BigDecimal) controlP.get("monto"));
			}

			if (montoControlP.compareTo(BigDecimal.ZERO) == 0)
			{

				GenericValue clavePres = delegator.findByPrimaryKey("ClavePresupuestal", UtilMisc.toMap("clavePresupuestal", clavePresupuestal));
				String inactivo = (String) clavePres.get("inactivo");
				String nuevoValor = null;
				if (inactivo == null)
				{
					nuevoValor = "Y";
				}
				clavePres.put("inactivo", nuevoValor);
				delegator.store(clavePres);
				return ServiceUtil.returnSuccess("Se han actualizado los datos de la clave presupuestal con \u00e9xito");

			}
			else
			{
				return ServiceUtil.returnError("No se puede desactivar la clave debido a que su presupuesto no es igual a cero ");
			}

		} catch (GenericEntityException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Error al actualizar los datos de la clave presupuestal");
		}

	}

	/**
	 * Metodo para activar o desactivar la validacion presupuestaria
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String,Object> actualizaSufPresupuestal(DispatchContext dctx, Map<String,Object> context)
	{
		Delegator delegator = dctx.getDelegator();
		String flag = (String) context.get("flag");

		try {

			GenericValue suficiencia = delegator.findByPrimaryKey("SuficienciaPresupuestal",UtilMisc.toMap("suficienciaPresupuestalId", "1"));
			suficiencia.set("flag",flag);
			delegator.createOrStore(suficiencia);

			String mensaje = flag.equalsIgnoreCase("Y")?"La suficiencia presupuestal esta activada":"La suficiencia presupuestal esta desactivada";
			return ServiceUtil.returnSuccess(mensaje);

		} catch (GenericEntityException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Error al actualizar los datos de la suficiencia presupuestal");
		}

	}
	
	
	/**
	 * Agrega clave presupuestal para el tipo de cambio
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<?,? extends Object> agregarClaveTipoCambio(DispatchContext dctx, Map<String,Object> context){
		
		Delegator delegator = dctx.getDelegator();				
		Map<String,String> mapaRegreso = FastMap.newInstance();
		try 
		{	//Armar la clave pres
			//Consultar si existe ya registrada y estatus desactivada
			//Si existe cambiar status, poner fecha y crear otra linea
			String organizationPartyId = (String) context.get("organizationPartyId");
			String ciclo = (String) context.get("ciclo");
			List<String> clavePres = new ArrayList<String>();
			String clave = "";
			Debug.logInfo("se forman las claves presupuestales",MODULE);
			for (int i = 1; i < 16; i++) 
			{	String campo = "clasificacion" + i;
				String valor = (String) context.get(campo);
				if (valor != null) 
				{	clavePres.add(valor.toString());
					clave += valor;
				}else 
				{	break;
				}
			}
			
			List<GenericValue> existeClave = delegator.findByAnd("ClavePresupuestal", UtilMisc.toMap("clavePresupuestal", clave));
			if(existeClave.isEmpty())
			{	return ServiceUtil.returnError("La clave presupuestal capturada no existe");				
			}
			
			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND, 
					EntityCondition.makeCondition("tipo", EntityOperator.EQUALS, "EGRESO"),
					EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS, organizationPartyId));
			List<GenericValue> lineasTipoCambio= delegator.findByCondition("ClavePresupuestalTipoCambio", condiciones, null, null);
			Debug.logInfo("lineasTipoCambio: " + lineasTipoCambio,MODULE);
			for(GenericValue clavePresupuestalTipoCambio : lineasTipoCambio)
			{	if(clavePresupuestalTipoCambio.getString("flag") == null || clavePresupuestalTipoCambio.getString("flag").equals("Y"))
				{
					clavePresupuestalTipoCambio.put("flag", "N");
				}			
				if(clavePresupuestalTipoCambio.getTimestamp("fechaHasta") == null)
				{	
					clavePresupuestalTipoCambio.put("fechaHasta", UtilDateTime.nowTimestamp());				
				}
				delegator.store(clavePresupuestalTipoCambio);				
			}
			
			//Crea el nuevo registro
			GenericValue clavePresupuestalTipoCambio = delegator.makeValue("ClavePresupuestalTipoCambio");
			clavePresupuestalTipoCambio.setNonPKFields(context);
			String idClaveTipoCambio = delegator.getNextSeqId("ClavePresupuestalTipoCambio");								
			clavePresupuestalTipoCambio.put("idClaveTipoCambio", idClaveTipoCambio);
			clavePresupuestalTipoCambio.put("clavePresupuestal", clave);
			clavePresupuestalTipoCambio.put("tipo", "EGRESO");
			clavePresupuestalTipoCambio.put("flag", "Y");
			clavePresupuestalTipoCambio.put("fechaDesde", UtilDateTime.nowTimestamp());	
			clavePresupuestalTipoCambio.put("organizationPartyId", organizationPartyId);
			
			int indice = UtilClavePresupuestal.indiceClasAdmin(UtilClavePresupuestal.EGRESO_TAG, organizationPartyId, delegator,ciclo);
			GenericValue estructuraPresup = UtilClavePresupuestal.obtenEstructPresupuestal(UtilClavePresupuestal.EGRESO_TAG, organizationPartyId, delegator,ciclo);
			
			for(int i=1 ; i <= UtilClavePresupuestal.TAG_COUNT ; i++)
			{	String campo = "clasificacion" + i;
				String valor = (String) context.get(campo);
				if(indice==i){
					clavePresupuestalTipoCambio.put(UtilClavePresupuestal.ENTITY_TAG_PREFIX+"Admin", valor);
					clavePresupuestalTipoCambio.put(UtilClavePresupuestal.ENTITY_TAG_PREFIX+i,null);
	        	} else {
					String enumId = UtilClavePresupuestal.obtenEnumId(valor, estructuraPresup.getString("clasificacion"+i), delegator);
					clavePresupuestalTipoCambio.put(UtilClavePresupuestal.ENTITY_TAG_PREFIX+i,enumId);
	        	}
	        }
			
			delegator.create(clavePresupuestalTipoCambio);
			
									
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		
		return mapaRegreso;
	}
	
	/**
	 * Busqueda de los datos para agregar la clave de penalizacion (INGRESOS)
	 * @param context
	 * @throws GeneralException
	 * @throws ParseException
	 */
	public static void clavePenalizacion(Map<String, Object> context)
			throws GeneralException, ParseException {

		final ActionContext ac = new ActionContext(context);
		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
		String cicloId = UtilCommon.getCicloId(ac.getRequest());
		
		Delegator delegator = ac.getDelegator();
		
		ac.put("tagTypes", UtilAccountingTags.getClassificationTagsForOrganization(organizationPartyId, cicloId , UtilAccountingTags.INGRESO_TAG, delegator));
		
		// build search conditions
		List<EntityCondition> searchConditions = new FastList<EntityCondition>();
		searchConditions.add(EntityCondition.makeCondition("tipo",EntityOperator.EQUALS, "INGRESO"));	
		searchConditions.add(EntityCondition.makeCondition("organizationPartyId",EntityOperator.EQUALS, organizationPartyId));

		// buscamos las claves segun las condiciones de la clave
		// presupuestal.
		List<GenericValue> clavesPresList = delegator.findByCondition("ClavePenaDeductiva", EntityCondition.makeCondition(searchConditions), null, UtilMisc.toList("fechaDesde DESC"));

		ac.put("clavesPresList", clavesPresList);
			

	}
	
	/**
	 * Agrega clave presupuestal para las penalizaciones
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<?,? extends Object> agregarClavePenalizacion(DispatchContext dctx, Map<String,Object> context){
		
		Delegator delegator = dctx.getDelegator();				
		Map<String,String> mapaRegreso = FastMap.newInstance();
		Debug.log("Mike - context: " + context);
		try 
		{	//Armar la clave pres
			//Consultar si existe ya registrada y estatus desactivada
			//Si existe cambiar status, poner fecha y crear otra linea
			String organizationPartyId = (String) context.get("organizationPartyId");
			String ciclo = (String) context.get("ciclo");
			List<String> clavePres = new ArrayList<String>();
			String clave = "";
			for (int i = 1; i < 16; i++) 
			{	String campo = "clasificacion" + i;
				String valor = (String) context.get(campo);
				if (valor != null) 
				{	clavePres.add(valor.toString());
					clave += valor;
				}else 
				{	break;
				}
			}
			
			List<GenericValue> existeClave = delegator.findByAnd("ClavePresupuestal", UtilMisc.toMap("clavePresupuestal", clave));
			if(existeClave.isEmpty())
			{	return ServiceUtil.returnError("La clave presupuestal capturada no existe");				
			}

			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND, 
					EntityCondition.makeCondition("tipo", EntityOperator.EQUALS, "INGRESO"),
					EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS, organizationPartyId));
			List<GenericValue> lineasTipoCambio= delegator.findByCondition("ClavePenaDeductiva", condiciones, null, null);
			Debug.logInfo("lineasPenalizacion: " + lineasTipoCambio,MODULE);
			for(GenericValue clavePresupuestalTipoCambio : lineasTipoCambio)
			{	if(clavePresupuestalTipoCambio.getString("flag") == null || clavePresupuestalTipoCambio.getString("flag").equals("Y"))
				{	
					clavePresupuestalTipoCambio.put("flag", "N");
				}			
				if(clavePresupuestalTipoCambio.getTimestamp("fechaHasta") == null)
				{	
					clavePresupuestalTipoCambio.put("fechaHasta", UtilDateTime.nowTimestamp());				
				}
				delegator.store(clavePresupuestalTipoCambio);				
			}

			//Crea el nuevo registro
			GenericValue clavePresupuestalTipoCambio = GenericValue.create(delegator
					.getModelEntity("ClavePenaDeductiva"));
			String idClaveTipoCambio = delegator.getNextSeqId("ClavePenaDeductiva");								
			clavePresupuestalTipoCambio.put("idClavePenalizacion", idClaveTipoCambio);
			clavePresupuestalTipoCambio.put("clavePresupuestal", clave);
			clavePresupuestalTipoCambio.put("tipo", "INGRESO");
			clavePresupuestalTipoCambio.put("flag", "Y");
			clavePresupuestalTipoCambio.put("fechaDesde", UtilDateTime.nowTimestamp());	
			clavePresupuestalTipoCambio.put("organizationPartyId", organizationPartyId);
			
			Debug.log("Mike - organizationPartyId: " + organizationPartyId);
			int indice = UtilClavePresupuestal.indiceClasAdmin(UtilClavePresupuestal.INGRESO_TAG, organizationPartyId, delegator, ciclo);
			Debug.log("indice: " + indice);
			GenericValue estructuraPresup = UtilClavePresupuestal.obtenEstructPresupuestal(UtilClavePresupuestal.INGRESO_TAG, organizationPartyId, delegator, ciclo);
			Debug.log("estructuraPresup: " + estructuraPresup);
			
			for(int i=1 ; i <= UtilClavePresupuestal.TAG_COUNT ; i++)
			{	String campo = "clasificacion" + i;
				String valor = (String) context.get(campo);
				if(indice==i){
					clavePresupuestalTipoCambio.put(UtilClavePresupuestal.ENTITY_TAG_PREFIX+"Admin", valor);
					clavePresupuestalTipoCambio.put(UtilClavePresupuestal.ENTITY_TAG_PREFIX+i,null);
	        	} else {
					String enumId = UtilClavePresupuestal.obtenEnumId(valor, estructuraPresup.getString("clasificacion"+i), delegator);
					clavePresupuestalTipoCambio.put(UtilClavePresupuestal.ENTITY_TAG_PREFIX+i,enumId);
	        	}
	        }
			
			delegator.create(clavePresupuestalTipoCambio);
									
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		
		return mapaRegreso;
	}


}

class ClavePresupuestalAux{
	String clave;
	List<String> clavePres;

	public String getClave() {
		return clave;
	}
	public void setClave(String clave) {
		this.clave = clave;
	}
	public List<String> getClavePres() {
		return clavePres;
	}
	public void setClavePres(List<String> clavePres) {
		this.clavePres = clavePres;
	}
}