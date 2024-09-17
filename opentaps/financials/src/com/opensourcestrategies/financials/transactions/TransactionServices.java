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

package com.opensourcestrategies.financials.transactions;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.security.Security;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.base.entities.AcctgTransEntry;
import org.opentaps.base.entities.PartyAcctgPreference;
import org.opentaps.base.services.CreateQuickAcctgTransService;
import org.opentaps.base.services.PostAcctgTransService;
import org.opentaps.common.util.UtilAccountingTags;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.common.util.UtilMessage;
import org.opentaps.domain.DomainsLoader;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.domain.organization.AccountingTagConfigurationForOrganizationAndUsage;
import org.opentaps.domain.organization.Organization;
import org.opentaps.domain.organization.OrganizationRepositoryInterface;
import org.opentaps.foundation.infrastructure.Infrastructure;
import org.opentaps.foundation.infrastructure.User;

/**
 * TransactionServices - Services for dealing with transactions.
 * 
 * @author <a href="mailto:libertine@ars-industria.com">Chris Liberty</a>
 * @version $Rev$
 */
public final class TransactionServices {

	private TransactionServices() {
	}

	private static final String MODULE = TransactionServices.class.getName();

	/**
	 * Create a Quick <code>AcctgTrans</code> record. IsPosted is forced to "N".
	 * Creates an Quick AcctgTrans and two offsetting AcctgTransEntry records.
	 * 
	 * @param dctx
	 *            a <code>DispatchContext</code> value
	 * @param context
	 *            a <code>Map</code> value
	 * @return a <code>Map</code> value
	 */
	@SuppressWarnings("unchecked")
	public static Map createQuickAcctgTrans(DispatchContext dctx, Map context) {
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");

		try {
			DomainsLoader dl = new DomainsLoader(
					new Infrastructure(dispatcher), new User(userLogin));
			OrganizationRepositoryInterface organizationRepository = dl
					.getDomainsDirectory().getOrganizationDomain()
					.getOrganizationRepository();
			String organizationPartyId = (String) context
					.get("organizationPartyId");
			Organization organization = organizationRepository
					.getOrganizationById(organizationPartyId);

			// create the accounting transaction
			Map createAcctgTransCtx = dctx.getModelService("createAcctgTrans")
					.makeValid(context, ModelService.IN_PARAM);
			if (UtilValidate
					.isEmpty(createAcctgTransCtx.get("transactionDate"))) {
				createAcctgTransCtx.put("transactionDate",
						UtilDateTime.nowTimestamp());
			}
			Map results = dispatcher.runSync("createAcctgTrans",
					createAcctgTransCtx);
			if (!UtilCommon.isSuccess(results)) {
				return UtilMessage.createAndLogServiceError(results, MODULE);
			}
			String acctgTransId = (String) results.get("acctgTransId");

			// create both debit and credit entries
			String currencyUomId = (String) context.get("currencyUomId");
			if (UtilValidate.isEmpty(currencyUomId)) {
				PartyAcctgPreference partyAcctgPref = organization
						.getPartyAcctgPreference();
				if (partyAcctgPref != null) {
					currencyUomId = partyAcctgPref.getBaseCurrencyUomId();
				} else {
					Debug.logWarning(
							"No accounting preference found for organization: "
									+ organizationPartyId, MODULE);
				}
			}

			// debit entry, using createAcctgTransEntryManual which validate the
			// accounting tags, the tags for are prefixed by "debitTagEnumId"
			Map createAcctgTransEntryCtx = dctx.getModelService(
					"createAcctgTransEntryManual").makeValid(context,
					ModelService.IN_PARAM);
			Map debitCtx = new HashMap(createAcctgTransEntryCtx);
			UtilAccountingTags.addTagParameters(context, debitCtx,
					"debitTagEnumId", UtilAccountingTags.ENTITY_TAG_PREFIX);
			debitCtx.put("acctgTransId", acctgTransId);
			debitCtx.put("glAccountId", context.get("debitGlAccountId"));
			debitCtx.put("debitCreditFlag", "D");
			debitCtx.put("acctgTransEntryTypeId", "_NA_");
			debitCtx.put("currencyUomId", currencyUomId);
			results = dispatcher.runSync("createAcctgTransEntryManual",
					debitCtx);

			// credit entry, the tags for are prefixed by "creditTagEnumId"
			Map creditCtx = new HashMap(createAcctgTransEntryCtx);
			UtilAccountingTags.addTagParameters(context, creditCtx,
					"creditTagEnumId", UtilAccountingTags.ENTITY_TAG_PREFIX);
			creditCtx.put("acctgTransId", acctgTransId);
			creditCtx.put("glAccountId", context.get("creditGlAccountId"));
			creditCtx.put("debitCreditFlag", "C");
			creditCtx.put("acctgTransEntryTypeId", "_NA_");
			creditCtx.put("currencyUomId", currencyUomId);
			results = dispatcher.runSync("createAcctgTransEntryManual",
					creditCtx);

			results = ServiceUtil.returnSuccess();
			results.put("acctgTransId", acctgTransId);
			return results;

		} catch (GeneralException e) {
			return UtilMessage.createAndLogServiceError(e, MODULE);
		}
	}

	/**
	 * Service to reverse an <code>AcctgTrans</code> entity.
	 * 
	 * @param dctx
	 *            a <code>DispatchContext</code> value
	 * @param context
	 *            a <code>Map</code> value
	 * @return a <code>Map</code> value
	 */
	public static Map<String, Object> reverseAcctgTrans(DispatchContext dctx,
			Map<String, ?> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		Security security = dctx.getSecurity();
		Locale locale = UtilCommon.getLocale(context);
		String acctgTransId = (String) context.get("acctgTransId");
		String postImmediately = (String) context.get("postImmediately");

		if (!security.hasEntityPermission("FINANCIALS", "_REVERSE", userLogin)) {
			return ServiceUtil.returnError(UtilProperties.getMessage(
					"FinancialsUiLabels", "FinancialsServiceErrorNoPermission",
					locale));
		}

		try {

			// Get the AcctgTrans record
			GenericValue acctgTrans = delegator.findByPrimaryKey("AcctgTrans",
					UtilMisc.toMap("acctgTransId", acctgTransId));
			if (acctgTrans == null) {
				return ServiceUtil.returnError(UtilProperties.getMessage(
						"FinancialsUiLabels",
						"FinancialsServiceErrorReverseTransactionNotFound",
						locale)
						+ ":" + acctgTransId);
			}

			// Get the related AcctgTransEntry records
			List<GenericValue> acctgTransEntries = delegator.findByAnd(
					"AcctgTransEntry",
					UtilMisc.toMap("acctgTransId", acctgTransId),
					UtilMisc.toList("acctgTransEntrySeqId"));
			if (acctgTrans == null) {
				return ServiceUtil.returnError(UtilProperties.getMessage(
						"FinancialsUiLabels",
						"FinancialsServiceErrorReverseTransactionNoEntries",
						locale)
						+ ":" + acctgTransId);
			}

			// Toggle the debit/credit flag, set the reconcileStatusId and move
			// the acctgTransId to the parent fields from each AcctgTransEntry
			String organizationPartyId = null;
			String cargo="";//Debit
			String abono="";//Credit
			String auxiliar="";
			String producto="";
			String auxiliarAbono="";
			String productoAbono="";
			boolean auxiliares = false;
			boolean bancos = false;
			for (GenericValue acctgTransEntry : acctgTransEntries) {
				// Verificamos si la Entry es Debit (Cargo)
				if (acctgTransEntry.getString("debitCreditFlag")
						.equalsIgnoreCase("D")) {
					// Se cambia flag de la Entry.
					acctgTransEntry.set("debitCreditFlag", "C");
					// Se checa si tiene un auxiliar.
					if (acctgTransEntry.getString("theirPartyId")!=null) {
						// Al cambiar la flag, se asigna el auxiliar al abono.
						auxiliarAbono = acctgTransEntry
								.getString("theirPartyId");
						// Se checa si tiene un producto.
					} else if (acctgTransEntry.getString("productId")
							!=null) {
						// Al cambiar la flag, se asigna el producto al abono.
						productoAbono = acctgTransEntry
								.getString("productId");
					}
					// Verificamos si la Entry es Credit (Abono)
				}else if (acctgTransEntry.getString("debitCreditFlag")
						.equalsIgnoreCase("C")) {
					// Se cambia flag de la Entry.
					acctgTransEntry.set("debitCreditFlag", "D");
					// Se checa si tiene un auxiliar.
					if (acctgTransEntry.getString("theirPartyId")!=null) {
						// Al cambiar la flag, se asigna el auxiliar al cargo.
						auxiliar = acctgTransEntry
								.getString("theirPartyId");
						// Se checa si tiene un producto.
					} else if (acctgTransEntry.getString("productId")
							!=null) {
						// Al cambiar la flag, se asigna el producto al abono.
						producto = acctgTransEntry
								.getString("productId");
					}
				}
				
				//Cuentas 8 no afectan Presupuesto.
				if (!acctgTransEntry.getString("glAccountId").startsWith("8")
						&& (!auxiliar.isEmpty() || !producto.isEmpty()
								|| !auxiliarAbono.isEmpty() || !productoAbono
									.isEmpty())) {
					Debug.log("La trans cuenta con aux para hacer reversa");
					Debug.log("auxiliar.- " + auxiliar);
					Debug.log("producto.- " + producto);
					Debug.log("auxiliarAbono.- " + auxiliarAbono);
					Debug.log("productoAbono.- " + productoAbono);
					auxiliares = true;
					
					if (acctgTransEntry.getString("debitCreditFlag")
							.equalsIgnoreCase("D")) {
						cargo = acctgTransEntry.getString("glAccountId");
					} else {
						abono = acctgTransEntry.getString("glAccountId");
					}
				}
				
				if(acctgTransEntry.getString("cuentaBancariaId")!=null){
					bancos = true;
				}
				
				acctgTransEntry.set("parentAcctgTransId", acctgTransId);
				acctgTransEntry.set("parentAcctgTransEntrySeqId",
						acctgTransEntry.get("acctgTransEntrySeqId"));
				acctgTransEntry.set("reconcileStatusId", "AES_NOT_RECONCILED");
				acctgTransEntry.remove("acctgTransId");
				if (organizationPartyId == null) {
					organizationPartyId = acctgTransEntry
							.getString("organizationPartyId");
				}
				
				// Se actualiza la tabla Gl_Account_Organization y
				// Gl_Account_History
				Map<String, Object> input = FastMap.newInstance();
				input.put("monto", acctgTransEntry.get("amount"));
				input.put("cuenta", acctgTransEntry.get("glAccountId"));
				input.put("organizacionPartyId",
						acctgTransEntry.get("organizationPartyId"));
				input.put("partyId", acctgTransEntry.get("partyId"));
				input.put("naturaleza", (acctgTransEntry.getString("debitCreditFlag")
						.equalsIgnoreCase("C") ? "CREDIT" : "DEBIT"));
				input.put("postedDate", (Date) acctgTrans.get("postedDate"));
				Map<String, Object> glOrganization = dispatcher.runSync(
						"actualizaGlAccountOrganization", input);
				if (ServiceUtil.isError(glOrganization)) {
					return ServiceUtil.returnError(ServiceUtil
							.getErrorMessage(glOrganization));
				}
			}
			
			//Se actualiza auxiliares.
			if(auxiliares){
				Debug.log("Se actualizara aux");
				Debug.log("La trans cuenta con aux para hacer reversa");
				Debug.log("auxiliar.- " + auxiliar);
				Debug.log("producto.- " + producto);
				Debug.log("auxiliarAbono.- " + auxiliarAbono);
				Debug.log("productoAbono.- " + productoAbono);
				//Preparamos el mapa.
				Map<String, Object> input = FastMap.newInstance();
				input.put("idTipoDoc", acctgTrans.get("idTipoDoc"));
				//input.put("partyId", acctgTrans.getString("partyId"));
				input.put("organizationPartyId", acctgTrans.getString("organizationPartyId"));
				input.put("auxiliar", auxiliar);
				input.put("producto", producto);
				input.put("monto", acctgTrans.getBigDecimal("postedAmount"));
				input.put("cargo",cargo);
				input.put("abono", abono);
				input.put("auxiliarAbono", auxiliarAbono);
				input.put("productoAbono", productoAbono);
				input.put("reversa", "1");
				input.put("fechaContable",(Timestamp) acctgTrans.get("postedDate"));
				Debug.log("Mapa input.- "+input);
				Map<String, Object> result = null;
				result = dispatcher.runSync("actualizaCatalogos", input);
				if (ServiceUtil.isError(result)) {
					return ServiceUtil.returnError(ServiceUtil
							.getErrorMessage(result));
				}
			}
			
			//Se actualiza bancos.
			if(bancos){
				for (GenericValue acctgTransEntry : acctgTransEntries) {
					//actualizaSaldoCuentasBancarias
					if(acctgTransEntry.getString("cuentaBancariaId")!=null){
						Debug.log("Se actualiza bancos");						
						GenericValue documento = delegator.findByPrimaryKey("TipoDocumento", UtilMisc.toMap("idTipoDoc", acctgTrans.getString("idTipoDoc")));
						Map<String, Object> input = FastMap.newInstance();
						input.put("cuentaBancariaId", acctgTransEntry.get("cuentaBancariaId"));
						input.put("modulo", documento.getString("moduloId"));
						input.put("monto", acctgTrans.getBigDecimal("postedAmount"));
						input.put("periodo",(Timestamp) acctgTrans.get("postedDate") );
						input.put("debitCreditFlag", acctgTransEntry.get("debitCreditFlag"));												
						Map<String, Object> result = null;
						result = dispatcher.runSync("actualizaSaldoCuentasBancarias", input);
						if (ServiceUtil.isError(result)) {
							return ServiceUtil.returnError(ServiceUtil
									.getErrorMessage(result));
						}
					}
				}
			}
			
			

			// Assemble the context for the service that creates and posts
			// AcctgTrans and AcctgTransEntry records
			Debug.log("Remove las Entries");
			Map<String, Object> serviceMap = acctgTrans.getAllFields();
			serviceMap.remove("acctgTransId");
			serviceMap.put("parentAcctgTransId", acctgTransId);
			serviceMap.remove("createdStamp");
			serviceMap.remove("createdTxStamp");
			serviceMap.remove("lastUpdatedStamp");
			serviceMap.remove("lastUpdatedTxStamp");

			// check the PartyAcctgPreference autoPostReverseAcctgTrans flag: if
			// set to N
			// set the createAcctgTransAndEntries service not to auto post the
			// transaction and make sure the isPosted flag is set to N
			// Also, we have to take into account postImmediately attribute that
			// overrides autoPostReverseAcctgTrans and enforces posting
			// in any case.
			if (!"Y".equals(postImmediately)) {
				if (UtilValidate.isEmpty(organizationPartyId)) {
					Debug.logWarning(
							"Got empty organizationPartyId for transaction ["
									+ acctgTransId
									+ "], cannot determine autoPostReverseAcctgTrans",
							MODULE);
				} else {
					GenericValue pref = delegator.findByPrimaryKeyCache(
							"PartyAcctgPreference",
							UtilMisc.toMap("partyId", organizationPartyId));
					if ("N".equals(pref.get("autoPostReverseAcctgTrans"))) {
						serviceMap.put("isPosted", "N");
						serviceMap.put("autoPostReverseAcctgTrans", "N");
					}
				}
			}

			// reverse the transaction at the same date, so it posts to the same
			// time periods as the original transaction
			Debug.log("Reverse the transaction");
			serviceMap
					.put("transactionDate", acctgTrans.get("transactionDate"));
			serviceMap.put("postedDate", acctgTrans.get("postedDate"));
			serviceMap.put("description", "Reversa de la transacci\u00f3n #"
					+ acctgTransId);
			serviceMap.put("acctgTransTypeId", "REVERSE");
			serviceMap.put("acctgTransEntries", acctgTransEntries);
			serviceMap.put("userLogin", userLogin);
			serviceMap.put("claveContabilizada", "Y");

			// Call the service
			Debug.log("Creamos la transaccion");
			Map<String, Object> createAcctgTransAndEntriesResult = dispatcher
					.runSync("createAcctgTransAndEntries", serviceMap);
			if (ServiceUtil.isError(createAcctgTransAndEntriesResult)) {
				return createAcctgTransAndEntriesResult;
			}

			Debug.log("Transaccion exitosa");
			Map<String, Object> serviceResult = ServiceUtil.returnSuccess();
			serviceResult.put("acctgTransId",
					createAcctgTransAndEntriesResult.get("acctgTransId"));
			return serviceResult;

		} catch (GenericEntityException e) {
			Debug.logError(e.getMessage(), MODULE);
			return ServiceUtil.returnError(e.getMessage());
		} catch (GenericServiceException e) {
			Debug.logError(e.getMessage(), MODULE);
			return ServiceUtil.returnError(e.getMessage());
		}
	}

	/**
	 * Servicio que da reversa una <code>Poliza</code> .
	 * 
	 * @param dctx
	 *            a <code>DispatchContext</code> value
	 * @param context
	 *            a <code>Map</code> value
	 * @return a <code>Map</code> value
	 */
	public static Map<String, Object> reversaPoliza(DispatchContext dctx,
			Map<String, ?> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String organizationPartyId = (String) context
				.get("organizationPartyId");
		String agrupador = (String) context.get("agrupador");
		String tipoPolizaId = (String) context.get("tipoPolizaId");

		try {

			EntityCondition condiciones = EntityCondition.makeCondition(
					EntityOperator.AND, EntityCondition.makeCondition(
							"agrupador", EntityOperator.LIKE, agrupador),
					EntityCondition.makeCondition("tipoPolizaId",
							EntityOperator.LIKE, tipoPolizaId), EntityCondition
							.makeCondition("organizationPartyId",
									EntityOperator.LIKE, organizationPartyId));

			List<GenericValue> transacciones = delegator.findByCondition(
					"AcctgTrans", condiciones, UtilMisc.toList("acctgTransId",
							"idTipoDoc", "clavePresupuestal", "postedDate"), UtilMisc
							.toList("acctgTransId"));

			/*
			 * Aqui se guardan las transacciones con claves presupuestales
			 * diferentes
			 */
			Map<String, GenericValue> transPresup = FastMap.newInstance();

			Map input = FastMap.newInstance();
			Map inputValidaPeriodo = FastMap.newInstance();
			input.put("userLogin", userLogin);
			Map resultReverse = FastMap.newInstance();
			GenericValue tipoDoc = null;
			String acctgTransId = null;
			if (transacciones != null && !transacciones.isEmpty()) {
				
				//Valida si el periodo esta cerrado
				List<GenericValue> periodos = obtenPeriodos(delegator, organizationPartyId, transacciones.get(0).getTimestamp("postedDate"));					
				
				if(periodos.size() == 0)
				{	return ServiceUtil.returnError("No pudo ser completada la reversa. El periodo ya ha sido cerrado");
				}				
				else if(periodos.size() == 1)
				{	String tipoPeriodo = periodos.get(0).getString("periodTypeId");
					if(tipoPeriodo.equalsIgnoreCase("FISCAL_YEAR"))
					{	return ServiceUtil.returnError("No pudo ser completada la reversa. El periodo ya ha sido cerrado");						
					}																				
				}
				
				
				
				
				
				
				
				tipoDoc = transacciones.get(0).getRelatedOne("TipoDocumento");
				for (GenericValue acctgTrans : transacciones) {

					acctgTransId = acctgTrans.getString("acctgTransId");
					input.put("acctgTransId", acctgTransId);
					resultReverse = dispatcher.runSync("reverseAcctgTrans",
							input);

					if (ServiceUtil.isError(resultReverse)) {
						return ServiceUtil
								.returnError("No pudo ser completada la reversa en la cuenta "
										+ acctgTransId);
					}

					if(acctgTrans.getString("clavePresupuestal") != null)
						transPresup.put(acctgTrans.getString("clavePresupuestal"),
							acctgTrans);
				}
				if (tipoDoc != null) {
					for (GenericValue acctgTrans : transPresup.values()) {
						acctgTransId = acctgTrans.getString("acctgTransId");
						input.put("acctgTransId", acctgTransId);
						resultReverse = dispatcher.runSync(
								"reversaPresupuestal", input);
						if (ServiceUtil.isError(resultReverse)) {
							return ServiceUtil
									.returnError("No se pudo completar la reversa en presupuesto ");
						}
					}
				}
			}

		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		} catch (GenericServiceException e) {
			return ServiceUtil.returnError(e.getMessage());
		}

		Map<String, Object> serviceResult = ServiceUtil.returnSuccess();
		serviceResult.put("agrupador", agrupador);
		serviceResult.put("tipoPolizaId", tipoPolizaId);
		serviceResult.put("organizationPartyId", organizationPartyId);
		return serviceResult;
	}

	/**
	 * Service to void a payment.
	 * 
	 * @param dctx
	 *            a <code>DispatchContext</code> value
	 * @param context
	 *            a <code>Map</code> value
	 * @return a <code>Map</code> value
	 */
	public static Map<String, Object> voidPayment(DispatchContext dctx,
			Map<String, ?> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		Security security = dctx.getSecurity();
		Locale locale = UtilCommon.getLocale(context);
		String paymentId = (String) context.get("paymentId");

		if (!security.hasEntityPermission("FINANCIALS", "_REVERSE", userLogin)) {
			return ServiceUtil.returnError(UtilProperties.getMessage(
					"FinancialsUiLabels", "FinancialsServiceErrorNoPermission",
					locale));
		}

		try {

			// Get the Payment record
			GenericValue payment = delegator.findByPrimaryKey("Payment",
					UtilMisc.toMap("paymentId", paymentId));
			if (payment == null) {
				return ServiceUtil.returnError(UtilProperties.getMessage(
						"FinancialsUiLabels",
						"FinancialsServiceErrorVoidPaymentNotFound", locale)
						+ ":" + payment);
			}

			// Check the Payment status - only payments with status PMNT_SENT or
			// PMNT_RECEIVED can be voided
			if (!(payment.getString("statusId").equals("PMNT_SENT") || payment
					.getString("statusId").equals("PMNT_RECEIVED"))) {
				return ServiceUtil.returnError(UtilProperties.getMessage(
						"FinancialsUiLabels",
						"FinancialsServiceErrorVoidPaymentIncorrectStatus",
						locale)
						+ ":" + payment);
			}

			// Change the Payment status to void
			Map<String, Object> setPaymentStatusResult = dispatcher.runSync(
					"setPaymentStatus", UtilMisc.toMap("paymentId", paymentId,
							"statusId", "PMNT_VOID", "userLogin", userLogin));
			if (ServiceUtil.isError(setPaymentStatusResult)) {
				return setPaymentStatusResult;
			}

			// Iterate through related PaymentApplications
			List<GenericValue> paymentApplications = delegator.getRelated(
					"PaymentApplication", payment);
			for (GenericValue paymentApplication : paymentApplications) {
				// Set the status of related invoice from INVOICE_PAID to
				// INVOICE_READY, if necessary
				if (paymentApplication.getString("invoiceId") != null) {
					GenericValue invoice = delegator.getRelatedOne("Invoice",
							paymentApplication);
					if (invoice.getString("statusId").equals("INVOICE_PAID")) {
						invoice.set("paidDate", null);
						delegator.store(invoice);
						Map<String, Object> setInvoiceStatusResult = dispatcher
								.runSync("setInvoiceStatus", UtilMisc.toMap(
										"invoiceId",
										invoice.getString("invoiceId"),
										"statusId", "INVOICE_READY",
										"userLogin", userLogin));
						if (ServiceUtil.isError(setInvoiceStatusResult)) {
							return setInvoiceStatusResult;
						}
					}
				}

				// Remove the PaymentApplication
				delegator.removeValue(paymentApplication);
			}

			// Iterate through related AcctgTrans, calling the reverseAcctgTrans
			// service on each
			List<GenericValue> acctgTransList = delegator.getRelated(
					"AcctgTrans", payment);
			for (GenericValue acctgTrans : acctgTransList) {
				Map<String, Object> reverseAcctgTransResult = dispatcher
						.runSync("reverseAcctgTrans", UtilMisc.toMap(
								"acctgTransId",
								acctgTrans.getString("acctgTransId"),
								"postImmediately", "Y", "userLogin", userLogin));
				if (ServiceUtil.isError(reverseAcctgTransResult)) {
					return reverseAcctgTransResult;
				}
			}

			return ServiceUtil.returnSuccess();

		} catch (GenericEntityException e) {
			Debug.logError(e.getMessage(), MODULE);
			return ServiceUtil.returnError(e.getMessage());
		} catch (GenericServiceException e) {
			Debug.logError(e.getMessage(), MODULE);
			return ServiceUtil.returnError(e.getMessage());
		}
	}

	/**
	 * Posts any AcctgTrans that are currently scheduled to be posted. Requires
	 * ACCOUNTING_ATX_POST permission.
	 * 
	 * @param dctx
	 *            a <code>DispatchContext</code> value
	 * @param context
	 *            a <code>Map</code> value
	 * @return a <code>Map</code> value
	 */
	@SuppressWarnings("unchecked")
	public static Map postScheduledAcctgTrans(DispatchContext dctx, Map context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		Security security = dctx.getSecurity();
		Locale locale = UtilCommon.getLocale(context);

		if (!security.hasEntityPermission("ACCOUNTING", "_ATX_POST", userLogin)) {
			return ServiceUtil.returnError(UtilProperties.getMessage(
					"FinancialsUiLabels", "FinancialsServiceErrorNoPermission",
					locale));
		}

		List transactions = null;
		try {
			List<EntityCondition> conditions = UtilMisc
					.<EntityCondition> toList(EntityCondition.makeCondition(
							"scheduledPostingDate", EntityOperator.NOT_EQUAL,
							null), EntityCondition.makeCondition(
							"scheduledPostingDate",
							EntityOperator.LESS_THAN_EQUAL_TO,
							UtilDateTime.nowTimestamp()), EntityCondition
							.makeCondition("isPosted", EntityOperator.EQUALS,
									"N"));
			transactions = delegator.findByAnd("AcctgTrans", conditions);
		} catch (GenericEntityException e) {
			Debug.logError(e.getMessage(), MODULE);
			return ServiceUtil.returnError(e.getMessage());
		}

		Map input = UtilMisc.toMap("userLogin", userLogin);
		for (Iterator iter = transactions.iterator(); iter.hasNext();) {
			GenericValue transaction = (GenericValue) iter.next();
			input.put("acctgTransId", transaction.get("acctgTransId"));
			try {
				Map results = dispatcher.runSync("postAcctgTrans", input);
				if (ServiceUtil.isError(results)) {
					Debug.logError(
							"Failed to post scheduled AcctgTransaction ["
									+ transaction.get("acctgTransId")
									+ "] due to logic error: "
									+ ServiceUtil.getErrorMessage(results),
							MODULE);
				}
			} catch (GenericServiceException e) {
				Debug.logError(e, "Failed to post scheduled AcctgTransaction ["
						+ transaction.get("acctgTransId")
						+ "] due to service engine error: " + e.getMessage(),
						MODULE);
			}
		}
		return ServiceUtil.returnSuccess();
	}

	/**
	 * Same as the <code>createAcctgTransEntry</code> service but add validation
	 * of accounting tags.
	 * 
	 * @param dctx
	 *            a <code>DispatchContext</code> value
	 * @param context
	 *            a <code>Map</code> value
	 * @return a <code>Map</code> value
	 * @throws GenericServiceException
	 *             if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public static Map createAcctgTransEntryManual(DispatchContext dctx,
			Map context) throws GenericServiceException {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Locale locale = UtilCommon.getLocale(context);
		GenericValue userLogin = (GenericValue) context.get("userLogin");

		try {
			LedgerRepositoryInterface ledgerRepository = new DomainsLoader(
					new Infrastructure(dispatcher), new User(userLogin))
					.getDomainsDirectory().getLedgerDomain()
					.getLedgerRepository();
			AcctgTransEntry entry = new AcctgTransEntry();
			entry.initRepository(ledgerRepository);
			entry.setAllFields(context);

			// validate the accounting tags
			List<AccountingTagConfigurationForOrganizationAndUsage> missings = ledgerRepository
					.validateTagParameters(entry);
			if (!missings.isEmpty()) {
				return UtilMessage.createAndLogServiceError(
						"OpentapsError_ServiceErrorRequiredTagNotFound",
						UtilMisc.toMap("tagName", missings.get(0)
								.getDescription()), locale, MODULE);
			}

			// if not given in the parameters set the currency from the party
			// accounting preferences
			if (UtilValidate.isEmpty(context.get("currencyUomId"))) {
				String baseCurrencyUomId = UtilCommon.getOrgBaseCurrency(
						(String) context.get("organizationPartyId"), delegator);
				if (baseCurrencyUomId == null) {
					return UtilMessage.createAndLogServiceError(
							"FinancialsServiceErrorPartyAcctgPrefNotFound",
							UtilMisc.toMap("partyId",
									context.get("organizationPartyId")),
							locale, MODULE);
				}
				entry.setCurrencyUomId(baseCurrencyUomId);
			}
			// set reconciled status to AES_NOT_RECONCILED
			entry.setReconcileStatusId("AES_NOT_RECONCILED");
			// create
			entry.setNextSubSeqId(AcctgTransEntry.Fields.acctgTransEntrySeqId
					.name());
			ledgerRepository.createOrUpdate(entry);

			Map results = ServiceUtil.returnSuccess();
			results.put("acctgTransEntrySeqId", entry.getAcctgTransEntrySeqId());
			return results;
		} catch (GeneralException e) {
			return UtilMessage.createAndLogServiceError(e, MODULE);
		}
	}

	/**
	 * Same as the <code>updateAcctgTransEntry</code> service but add validation
	 * of accounting tags.
	 * 
	 * @param dctx
	 *            a <code>DispatchContext</code> value
	 * @param context
	 *            a <code>Map</code> value
	 * @return a <code>Map</code> value
	 * @throws GenericServiceException
	 *             if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public static Map updateAcctgTransEntryManual(DispatchContext dctx,
			Map context) throws GenericServiceException {
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Locale locale = UtilCommon.getLocale(context);
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		try {
			LedgerRepositoryInterface ledgerRepository = new DomainsLoader(
					new Infrastructure(dispatcher), new User(userLogin))
					.getDomainsDirectory().getLedgerDomain()
					.getLedgerRepository();
			AcctgTransEntry pk = new AcctgTransEntry();
			pk.setPKFields(context);
			AcctgTransEntry entry = ledgerRepository.findOneNotNull(
					AcctgTransEntry.class, ledgerRepository.map(
							AcctgTransEntry.Fields.acctgTransId,
							pk.getAcctgTransId(),
							AcctgTransEntry.Fields.acctgTransEntrySeqId,
							pk.getAcctgTransEntrySeqId()));
			entry.setNonPKFields(context);

			// validate the accounting tags
			List<AccountingTagConfigurationForOrganizationAndUsage> missings = ledgerRepository
					.validateTagParameters(entry);
			if (!missings.isEmpty()) {
				return UtilMessage.createAndLogServiceError(
						"OpentapsError_ServiceErrorRequiredTagNotFound",
						UtilMisc.toMap("tagName", missings.get(0)
								.getDescription()), locale, MODULE);
			}

			ledgerRepository.update(entry);
			return ServiceUtil.returnSuccess();
		} catch (GeneralException e) {
			return UtilMessage.createAndLogServiceError(e, MODULE);
		}
	}

	/**
	 * Automatically create and Post <code>AcctgTrans</code> record. The
	 * settleFrom parameter have a pattern like UD:121000 or CC:Visa where two
	 * first letters indicates Undeposited receipts or Credit Card and other is
	 * number of the account or type of the Credit Card
	 * 
	 * @param dctx
	 *            a <code>DispatchContext</code> value
	 * @param context
	 *            a <code>Map</code> value
	 * @return a <code>Map</code> value
	 */
	@SuppressWarnings("unchecked")
	public static Map createSettlementAcctgTrans(DispatchContext dctx,
			Map context) {
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		Delegator delegator = dctx.getDelegator();
		String organizationPartyId = (String) context
				.get("organizationPartyId");
		String debitGlAccountId = null;
		String creditGlAccountId = null;
		String cardTransactionsGlAccountId = null;
		String from[] = ((String) context.get("settleFrom")).split(":");

		if (from[0].equalsIgnoreCase("CC")) {
			// CreditCard
			try {
				GenericValue cardTransactionsGlAccount = delegator
						.findByPrimaryKey("CreditCardTypeGlAccount", UtilMisc
								.toMap("cardType", from[1],
										"organizationPartyId",
										organizationPartyId));
				if (cardTransactionsGlAccount != null) {
					cardTransactionsGlAccountId = cardTransactionsGlAccount
							.getString("glAccountId");
				} else {
					Debug.logWarning("No GL account configured for card type "
							+ from[1] + " when trying to settle card", "");
				}
			} catch (GenericEntityException e) {
				return UtilMessage.createAndLogServiceError(e, MODULE);
			}
		} else {
			// Undeposited receipts
			cardTransactionsGlAccountId = from[1];
		}

		if ("REFUND".equalsIgnoreCase((String) context.get("paymentOrRefund"))
				&& from[0].equalsIgnoreCase("UD")) {
			return ServiceUtil
					.returnError("Refund only available for credit cards");
		}

		try {

			if ("REFUND".equalsIgnoreCase((String) context
					.get("paymentOrRefund"))) {
				debitGlAccountId = cardTransactionsGlAccountId;
				creditGlAccountId = (String) context.get("settleTo");
			} else {
				debitGlAccountId = (String) context.get("settleTo");
				creditGlAccountId = cardTransactionsGlAccountId;
			}

			Map createAcctgTransCtx = dctx.getModelService("createAcctgTrans")
					.makeValid(context, ModelService.IN_PARAM);

			String glFiscalTypeId = (String) context.get("glFiscalTypeId");
			if (UtilValidate.isEmpty(glFiscalTypeId)) {
				glFiscalTypeId = "ACTUAL";
			}

			CreateQuickAcctgTransService acctgTransService = new CreateQuickAcctgTransService();
			acctgTransService.setInAcctgTransTypeId("BANK_SETTLEMENT");

			acctgTransService.setInOrganizationPartyId((String) context
					.get("organizationPartyId"));
			acctgTransService.setInDebitGlAccountId(debitGlAccountId);
			acctgTransService.setInCreditGlAccountId(creditGlAccountId);
			acctgTransService.setInAmount((Double) context.get("amount"));
			acctgTransService.setInUserLogin(userLogin);
			acctgTransService.setInGlFiscalTypeId(glFiscalTypeId);

			acctgTransService
					.setInTransactionDate((Timestamp) createAcctgTransCtx
							.get("transactionDate"));

			acctgTransService.runSync(new Infrastructure(dispatcher));

			String acctgTransId = acctgTransService.getOutAcctgTransId();

			PostAcctgTransService postAcctgTransService = new PostAcctgTransService();
			postAcctgTransService.setInUserLogin(userLogin);
			postAcctgTransService.setInAcctgTransId(acctgTransId);
			postAcctgTransService.runSync(new Infrastructure(dispatcher));

			Map results = ServiceUtil
					.returnSuccess("Settle Payments completed successfully");
			results.put("transactionDatePrev", context.get("transactionDate"));
			return results;

		} catch (Exception e) {
			return UtilMessage.createAndLogServiceError(e, MODULE);
		}
	}

	@SuppressWarnings("unchecked")
	public static Map traspasoMensual(DispatchContext dctx, Map context)
			throws GenericEntityException, GenericServiceException {
		Delegator delegator = dctx.getDelegator();
		String mesId = (String) context.get("mesId");
		String ciclo = (String) context.get("enumCode");

		Debug.log("mes.- " + mesId);
		Debug.log("ciclo.- " + ciclo);

		if (mesId.equalsIgnoreCase("12")) {
			return UtilMessage.createAndLogServiceError(
					"No se puede hacer el traspaso de Diciembre.", MODULE);
		}

		// Se buscan los momentos que se pueden traspasar.
		List<GenericValue> movimientoMensual = delegator
				.findAll("MovimientoMensual");
		if (movimientoMensual.isEmpty()) {
			return UtilMessage
					.createAndLogServiceError(
							"No existen Momentos registrados para el traspaso mensual.",
							MODULE);
		}
		
		//Validamos que el mes no se haya traspasado antes.
		GenericValue mesTraspasado = delegator.findByPrimaryKey("MesTraspaso", UtilMisc.toMap("mesId", mesId, "ciclo", ciclo));
		
		if(mesTraspasado!=null){
			return UtilMessage.createAndLogServiceError(
					"El mes ya ha sido traspasado.", MODULE);
		}
		
		//Validamos que el mes no sea futuro.
		if(!mesId.equalsIgnoreCase("01")){
			Integer mesAnterior = Integer.parseInt(mesId);
			mesAnterior--;
			
			String mesAnt = mesAnterior < 10 ? "0"
					+ new Integer(mesAnterior).toString() : new Integer(
					mesAnterior).toString();		
					
			mesTraspasado = delegator.findByPrimaryKey("MesTraspaso", UtilMisc.toMap("mesId", mesAnt, "ciclo", ciclo));
					
					if(mesTraspasado==null){
						return UtilMessage.createAndLogServiceError(
								"No se puede hacer el traspaso de meses futuros.", MODULE);
					}		
		}
		
		// Se crea la lista de los momentos.
			List<GenericValue> controlPresupuestal = new ArrayList<GenericValue>();
			for (GenericValue movimiento : movimientoMensual) {
				controlPresupuestal.addAll(delegator.findByAnd(
						"ControlPresupuestal", "momentoId",
						movimiento.getString("movimientoMensualId"), "mesId",
						mesId, "ciclo", ciclo));
			}
			if (controlPresupuestal.isEmpty()) {
				return UtilMessage.createAndLogServiceError(
						"No existen Movimientos Registrados para control presupuestal.", MODULE);
			}
			
			Integer mesSiguiente = Integer.parseInt(mesId);
			mesSiguiente++;

			String mes = mesSiguiente < 10 ? "0"
					+ new Integer(mesSiguiente).toString() : new Integer(
					mesSiguiente).toString();
			
			List<GenericValue> controlPresupuestalSiguiente = new ArrayList<GenericValue>();
			Map<String,GenericValue> mapControlPresupuestalSiguiente = FastMap.newInstance();
			for (GenericValue movimiento : movimientoMensual) {
				controlPresupuestalSiguiente.addAll(delegator.findByAnd(
						"ControlPresupuestal", "momentoId",
						movimiento.getString("movimientoMensualId"), "mesId",
						mes, "ciclo", ciclo));
			}
			
			for(GenericValue controlP : controlPresupuestalSiguiente){				
				mapControlPresupuestalSiguiente.put(controlP.getString("clavePresupuestal")+controlP.getString("momentoId"), controlP);
			}
			
			List<GenericValue> traspasosMulti = new ArrayList<GenericValue>();
			
		// Se hace el traspaso de cada elemento de la lista al siguente mes.
		for (GenericValue control : controlPresupuestal) {
			
			// Se hace solo si el monto es mayor que 0.
			if (control.getBigDecimal("monto").signum() == 1) {
				
				GenericValue controlSiguiente = mapControlPresupuestalSiguiente.get(control.getString("clavePresupuestal")+control.getString("momentoId"));
				
				if(UtilValidate.isEmpty(controlSiguiente)){
					
					controlSiguiente = (GenericValue) control.clone();
					controlSiguiente.set("idSecuencia", "");
					controlSiguiente.setNextSeqId();				
					controlSiguiente.set("mesId", mes);					
					mapControlPresupuestalSiguiente.put(controlSiguiente.getString("clavePresupuestal")+controlSiguiente.getString("momentoId"), controlSiguiente);
					
					
				}else{
					
					controlSiguiente.set(
							"monto",
							controlSiguiente.getBigDecimal("monto").add(
									control.getBigDecimal("monto")));
					
				}
				
				List<GenericValue> traspasos = delegator.findByAnd("Traspaso",
						"idSecuenciaViene", control.getString("idSecuencia"),
						"idSecuenciaVa",
						controlSiguiente.getString("idSecuencia"));
				if (!traspasos.isEmpty()) {
					return UtilMessage.createAndLogServiceError(
							"Ya se ha hecho el traspaso de este mes.", MODULE);
				}
				
				GenericValue traspaso = delegator.makeValue("Traspaso");
				traspaso.setNextSeqId();
				traspaso.set("idSecuenciaViene",
						control.getString("idSecuencia"));
				traspaso.set("idSecuenciaVa",
						controlSiguiente.getString("idSecuencia"));
				traspaso.set("monto", control.getBigDecimal("monto"));
				GenericValue user = (GenericValue) context.get("userLogin");
				traspaso.set("usuario", user.getString("userLoginId"));
				traspaso.set("fechaRegistro", UtilDateTime.nowTimestamp());
				
				traspasosMulti.add(traspaso);
				
				// Poniendo en 0 el registro actual.
				control.set("monto", BigDecimal.ZERO);
				
			}
		}
		
		Map <String,Object> result = dctx.getDispatcher().runSync("traspasoRequisicion", UtilMisc.toMap("mesId", mesId, "ciclo", ciclo));
		
		Map <String,Object> result2 = dctx.getDispatcher().runSync("traspasoMontoRestantePedido", UtilMisc.toMap("mesId", mesId, "ciclo", ciclo));
		
		if(ServiceUtil.isError(result)){
			
    		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
    	
		}else if(ServiceUtil.isError(result2)){
			
			return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result2));
    	
    	}else{
		
			controlPresupuestalSiguiente.clear();
			controlPresupuestalSiguiente.addAll(mapControlPresupuestalSiguiente.values());
			
			delegator.storeAll(controlPresupuestal);
			delegator.storeAll(controlPresupuestalSiguiente);
			delegator.storeAll(traspasosMulti);	
			
			//Registramos el traspaso.
			mesTraspasado = delegator.makeValue("MesTraspaso");
			mesTraspasado.set("mesId", mesId);
			mesTraspasado.set("ciclo",ciclo);
			delegator.create(mesTraspasado);
			
			return ServiceUtil.returnSuccess("Se ha realizado el traspaso presupuestal.");
    	}
	}
	
	public static List<GenericValue> obtenPeriodos(Delegator delegator,
			String organizationId, Date fecha) throws GenericEntityException {
		List<GenericValue> periodos = delegator.findByAnd("CustomTimePeriod",
				UtilMisc.toMap("organizationPartyId", organizationId,
						"isClosed", "N"));
		List<GenericValue> periodosAplicables = new ArrayList<GenericValue>();
		for (GenericValue periodo : periodos) {
			java.sql.Date fromDate = periodo.getDate("fromDate");
			java.sql.Date thruDate = periodo.getDate("thruDate");
			if (fecha.after(fromDate) && fecha.before(thruDate)) {
				periodosAplicables.add(periodo);
			}
		}
		Debug.log("Periodos regresados.- " + periodosAplicables.size());
		return periodosAplicables;
	}
}
