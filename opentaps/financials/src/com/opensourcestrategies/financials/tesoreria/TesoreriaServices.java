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

package com.opensourcestrategies.financials.tesoreria;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.commons.lang.math.NumberUtils;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilNumber;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.base.entities.ConsultaCobrosOrdenCobro;
import org.opentaps.base.entities.ConsultaOrdenCobro;
import org.opentaps.base.entities.ConsultaPagosOrdenPago;
import org.opentaps.base.entities.ConsultaTraspasoBanco;
import org.opentaps.base.entities.CuentaSucursalBanco;
import org.opentaps.base.entities.Estatus;
import org.opentaps.base.entities.PendienteTesoreriaEventoStatus;
import org.opentaps.base.entities.ConsultaOrdenPago;
import org.opentaps.base.entities.SaldoCatalogo;
import org.opentaps.base.entities.SaldosCuentasBancarias;
import org.opentaps.base.entities.TraspasoBanco;
import org.opentaps.common.builder.EntityListBuilder;
import org.opentaps.common.builder.PageBuilder;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.common.util.UtilMessage;
import org.opentaps.domain.DomainsDirectory;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.domain.organization.Organization;
import org.opentaps.domain.organization.OrganizationRepositoryInterface;
import org.opentaps.foundation.action.ActionContext;
import org.opentaps.foundation.repository.RepositoryException;

import antlr.StringUtils;



/**
 * ConfigurationServices - Services for configuring GL Accounts.
 * 
 * @author <a href="mailto:ali@opensourcestrategies.com">Ali Afzal Malik</a>
 * @version $Rev: 150 $
 * @since 2.2
 */
public final class TesoreriaServices {

	private TesoreriaServices() {
	}

	private static String MODULE = TesoreriaServices.class.getName();	

	/**
	 * Crear una cuenta de banco
	 * 
	 * @param dctx
	 *            a <code>DispatchContext</code> value
	 * @param context
	 *            a <code>Map</code> value
	 * @return a service response <code>Map</code> value
	 */
	@SuppressWarnings({ "unchecked" })
	public static Map crearCuentaBanco(DispatchContext dctx, Map context)
			throws ParseException {
		Delegator delegator = dctx.getDelegator();
		Debug.log("contextBanco: " + context);
		String bancoId = (String) context.get("bancoId");
		String sucursalId = (String) context.get("sucursalId");
		String nombreCuenta = (String) context.get("nombreCuenta");
		String numeroCuenta = (String) context.get("numeroCuenta");
		String descripcionCuenta = (String) context.get("descripcionCuenta");
		BigDecimal montoCuenta = (BigDecimal) context.get("montoCuenta");
		String moneda = (String) context.get("currency");
		String partyId = (String) context.get("partyId");
		String tipoCuentaId = (String) context.get("tipoCuentaId");
		String usoCuentaId = (String) context.get("usoCuentaId");
		Timestamp periodo = (Timestamp) context.get("periodo"); 
		BigDecimal saldo = BigDecimal.ZERO;
		String estatus = "1";
		context.put(saldo, estatus);		

		try {
			String error = "";
			if (bancoId.trim().equals("") ) {
				error+="El p\u00E1rametro Banco no puede estar vacio";				
			}
			if (sucursalId.trim().equals("")) {
				error+="\nEl p\u00E1rametro Sucursal no puede estar vacio";
			}
			if (nombreCuenta == null || nombreCuenta.trim().equals("")) {
				error+="\nEl p\u00E1rametro Nombre de la cuenta no puede estar vacio";
			}
			if (numeroCuenta == null || numeroCuenta.trim().equals("")) {
				error+="\nEl p\u00E1rametro N\u00FAmero de Cuenta no puede estar vacio";
			}
			if (partyId == null || partyId.trim().equals("")) {
				error+="\nEl p\u00E1rametro Organizaci\u00F3n no puede estar vacio";
			}
			if (montoCuenta == null || montoCuenta.equals("")) {
				error+="\nEl p\u00E1rametro Monto no puede estar vacio";
			}
			if (moneda == null || moneda.trim().equals("")) {
				error+="\nEl p\u00E1rametro Moneda no puede estar vacio";				
			}
			if (tipoCuentaId == null || tipoCuentaId.trim().equals("")) {
				error+="\nEl p\u00E1rametro Tipo de cuenta bancaria no puede estar vacio";				
			}
			if (usoCuentaId == null || usoCuentaId.trim().equals("")) {
				error+="\nEl p\u00E1rametro Uso de cuenta bancaria no puede estar vacio";				
			}
			if(!error.isEmpty()){
				return UtilMessage.createAndLogServiceError(
						error, MODULE);
			}


			GenericValue cuentaBancaria = delegator.makeValue("CuentaBancaria");
			GenericValue partyCuentaBancaria = delegator.makeValue("PartyCuentaBancaria");
			// if an id was given, check for duplicate
			if (UtilValidate.isNotEmpty(numeroCuenta)) {
				cuentaBancaria.setPKFields(context);				

				Map fields = UtilMisc.toMap("numeroCuenta", numeroCuenta, "bancoId", bancoId);
				List value = null;
				value = delegator.findByAnd("CuentaBancaria", fields);
				if (!value.isEmpty()) 
				{	return UtilMessage.createAndLogServiceError(
						"Ya existe una cuenta bancaria con este identificador ["
								+ numeroCuenta + "]", MODULE);
				}
			}			

			// else generate the id from the sequence
			cuentaBancaria.setNonPKFields(context);
			String idCuenta = delegator.getNextSeqId("CuentaBancaria");
			cuentaBancaria.put("cuentaBancariaId", idCuenta);
			cuentaBancaria.put("sucursalId", sucursalId);
			cuentaBancaria.put("bancoId", bancoId);			
			cuentaBancaria.put("nombreCuenta", nombreCuenta);
			cuentaBancaria.put("numeroCuenta", numeroCuenta);
			cuentaBancaria.put("descripcion", descripcionCuenta);
			cuentaBancaria.put("saldoInicial", montoCuenta);
			cuentaBancaria.put("estatus", estatus);	
			cuentaBancaria.put("moneda", moneda);
			cuentaBancaria.put("tipoCuentaBancariaId", tipoCuentaId);
			cuentaBancaria.put("usoCuentaBancariaId", usoCuentaId);
			cuentaBancaria.put("habilitada", "S");
			delegator.create(cuentaBancaria);

			//Se crea SaldoCatalogo para llevar el monto de la cuenta
			GenericValue saldoCatalogo = delegator.makeValue("SaldoCatalogo");
			String idSaldoCatalogo = delegator.getNextSeqId("SaldoCatalogo");
			saldoCatalogo.put("secuenciaId", idSaldoCatalogo);
			saldoCatalogo.put("catalogoId",idCuenta);
			saldoCatalogo.put("tipoId", "BANCO");
			saldoCatalogo.put("partyId", partyId);
			saldoCatalogo.put("tipo", "B");
			saldoCatalogo.put("monto", montoCuenta);
			Calendar date = Calendar.getInstance();
			date.setTime(periodo);
			date.set(Calendar.DAY_OF_MONTH,1);
			date.set(Calendar.HOUR, 0);
			date.set(Calendar.MINUTE, 0);
			date.set(Calendar.SECOND, 0);
			date.set(Calendar.MILLISECOND, 0);
			date.set(Calendar.AM_PM, Calendar.AM);
			periodo = new Timestamp(date.getTimeInMillis());
			saldoCatalogo.put("periodo", periodo);
			delegator.create(saldoCatalogo);

			partyCuentaBancaria.setNonPKFields(context);
			partyCuentaBancaria.put("cuentaBancariaId", idCuenta);
			partyCuentaBancaria.put("partyId", partyId);
			delegator.create(partyCuentaBancaria);

			return ServiceUtil.returnSuccess("Se ha creado la cuenta con \u00e9xito");

		} catch (GeneralException e) {
			return UtilMessage.createAndLogServiceError(e, MODULE);
		}
	}

	/**
	 * Transferencia de saldos entre cuentas bancarias y contables
	 * 
	 * @param dctx
	 *            a <code>DispatchContext</code> value
	 * @param context
	 *            a <code>Map</code> value
	 * @return a service response <code>Map</code> value
	 * @throws Exception 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map traspasosBancos(DispatchContext dctx, Map context) throws Exception 
	{	try
	{	Delegator delegator = dctx.getDelegator();				
	LocalDispatcher dispatcher = dctx.getDispatcher();
	Debug.log("Mike.- " + context);
	//			String bancoIdOrigen = (String) context.get("bancoIdOrigen");
	//			String bancoIdDestino = (String) context.get("bancoIdDestino");
	//			String cuentaOrigen = (String) context.get("cuentaOrigen");
	//			String cuentaDestino = (String) context.get("cuentaDestino");
	//			String idTipoDoc = (String) context.get("idTipoDoc");
	//			String partyId = (String) context.get("partyId");
	String organizationPartyId = (String) context.get("organizationPartyId");
	String descripcion = (String) context.get("descripcion");
	//			BigDecimal amount = (BigDecimal) context.get("amount");		
	//String concepto = (String) context.get("id_concepto_sub");
	String currency = (String) context.get("moneda");
	String acctgTransTypeId = (String) context.get("acctgTransTypeId");
	GenericValue userLogin = (GenericValue) context.get("userLogin");
	String idTraspaso = delegator.getNextSeqId("TraspasoBanco");
	Timestamp fechaTrans = new Timestamp(Calendar.getInstance().getTimeInMillis());
	Timestamp fechaContable = (Timestamp) context.get("fechaContable");
	if(fechaTrans.compareTo(fechaContable) < 0)
	{	return UtilMessage.createAndLogServiceError(
			"No es posible realizar un traspaso bancario en una fecha posterior a la actual", MODULE);				
	}



	if (organizationPartyId.equals(" ")) {
		return UtilMessage.createAndLogServiceError(
				"El par\u00e1metro Organizaci\u00f3n no puede estar vacio", MODULE);
	}

	if (fechaContable.equals(" ")) {
		return UtilMessage.createAndLogServiceError(
				"El par\u00e1metro Fecha Contable no puede estar vacio", MODULE);
	}
	if (acctgTransTypeId == null) {
		return UtilMessage.createAndLogServiceError(
				"El evento no puede estar vacio", MODULE);
	}
	//			if (bancoIdOrigen.equals(bancoIdDestino) && cuentaOrigen.equals(cuentaDestino))
	//				return UtilMessage.createAndLogServiceError(
	//						"No es posible realizar traspasos entre la misma cuenta", MODULE);
	//			//validamos si hay periodos abiertos
	//    		List<GenericValue> vPeriodos = obtenPeriodos(delegator,
	//    				organizationPartyId, fechaContable);
	//			if (vPeriodos.isEmpty())
	//				return UtilMessage
	//						.createAndLogServiceError(
	//								"El o los periodos ha(n) sido cerrado(s) o no han sido creados",
	//								MODULE);
	//	
	//			// Validar si la cuenta origen tiene saldo
	//			if (UtilValidate.isNotEmpty(cuentaOrigen)) {
	//				
	//				if (obtenSaldoCuentaActual(delegator, cuentaOrigen,
	//						fechaContable).compareTo(amount) < 0) {
	//					return UtilMessage
	//							.createAndLogServiceError(
	//									"La Cuenta Origen ["
	//											+ cuentaOrigen
	//											+ "] no tiene saldo suficiente para realizar la transferencia",
	//									MODULE);
	//				}
	//			}			
	//												
	//			
	//			Boolean esConcepto = false;
	//			
	//			//Valida si el concepto esta en la guia contable en la columna concepto
	//			List searchConditionsConcepto = new FastList<EntityCondition>();                         
	//            if (UtilValidate.isNotEmpty(concepto)) {
	//            	searchConditionsConcepto.add(EntityCondition.makeCondition(GuiaContable.Fields.id_concepto.name(), EntityOperator.EQUALS, concepto));
	//            }            
	//            Set fieldsToSelect = UtilMisc.toSet("id_concepto");                   
	//            List resultList = delegator.findList("GuiaContable", EntityCondition.makeCondition(searchConditionsConcepto, EntityOperator.AND), fieldsToSelect, UtilMisc.toList("id_concepto"), null, false);
	//            
	//          //Valida si el concepto esta en la guia contable en la columna subconcepto
	//			List searchConditionsSubconcepto = new FastList<EntityCondition>();                         
	//            if (UtilValidate.isNotEmpty(concepto)) {
	//            	searchConditionsSubconcepto.add(EntityCondition.makeCondition(GuiaContable.Fields.id_subconcepto.name(), EntityOperator.EQUALS, concepto));
	//            }            
	//            Set fieldsToSelectSub = UtilMisc.toSet("id_concepto");                   
	//            List resultListSub = delegator.findList("GuiaContable", EntityCondition.makeCondition(searchConditionsSubconcepto, EntityOperator.AND), fieldsToSelect, UtilMisc.toList("id_concepto"), null, false);
	//            
	//            if(resultListSub.isEmpty())
	//            {	esConcepto = true; 	            
	//            }
	//            else if(resultList.isEmpty())
	//            {	esConcepto = false;
	//            }
	//            else
	//            {	return UtilMessage.createAndLogServiceError("El concepto seleccionado no se encuentra en la Guia Contable", MODULE);            	
	//            }

	//Invoca a Motor contable			
	//			Map input = FastMap.newInstance();
	//            input.put("idTipoDoc",idTipoDoc);
	//            input.put("tipoClave","");
	////            input.put("referenciaDocumento",idTraspaso+"_"+abreviatura);
	//            input.put("fechaTransaccion",fechaTrans);
	//            input.put("fechaContable",fechaContable);
	//            input.put("currency", currency);
	//            input.put("userLogin", userLogin);
	//            input.put("organizationPartyId",organizationPartyId);
	//            input.put("partyId", partyId);
	//            input.put("monto", amount);
	//            input.put("secuencia", "0001");
	//            input.put("descripcion", descripcion);
	//            if(esConcepto)
	//            	input.put("concepto", concepto);
	//            else
	//            	input.put("subconcepto", concepto);

	//            Map mapaMotor = dispatcher.runSync("registroContable", input);
	//            Debug.log("mapaMotor: " + mapaMotor.get("listaTrans"));
	//            List<GenericValue> list = (List<GenericValue>) mapaMotor.get("listaTrans");
	//            Iterator<GenericValue> secuenciaId = list.iterator();
	//    		Map secuencia = secuenciaId.next();
	//            String transaccionId = (String) secuencia.get("acctgTransId");
	//            Debug.log("transaccionId: " + transaccionId);

	/************************************/
	/******** Motor Contable ************/
	/************************************/

	Map catalogoCargoMap = (Map) context.get("catalogoCargoContMap");
	Map catalogoAbonoMap = (Map) context.get("catalogoAbonoContMap");
	Map montoMap = (Map) context.get("montoMap");
	Map clavePresupuestalMap = (Map) context.get("clavePresupuestalMap");
	String descrSinEspacios = "";

	List<String> orderBy = UtilMisc.toList("secuencia");
	List<GenericValue> listaLineasContables = delegator.findByAndCache("LineaContable", UtilMisc.toMap("acctgTransTypeId",acctgTransTypeId),orderBy);
	for (GenericValue lineaCont : listaLineasContables) {
		descrSinEspacios = lineaCont.getString("descripcion").replaceAll("\\s","")+0;
		Debug.log("Descripcioooon: " + descrSinEspacios);

	}

	clavePresupuestalMap.put("0", "");
	context.put("clavePresupuestalMap", clavePresupuestalMap);

	Map<String, Object> input = FastMap.newInstance();
	input.put("eventoContableId", acctgTransTypeId);
	input.put("tipoClaveId", "");
	input.put("fechaRegistro", fechaTrans);
	input.put("fechaContable", fechaContable);
	input.put("currency", currency);
	input.put("usuario", userLogin.getString("userLoginId"));
	input.put("organizationId", organizationPartyId);
	input.put("descripcion", descripcion);
	input.put("roleTypeId", "BILL_FROM_VENDOR");
	input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(acctgTransTypeId, delegator, context, null,null, null, null));

	Map<String, Object> output = dispatcher.runSync("creaTransaccionMotor", input);
	if(ServiceUtil.isError(output)){
		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
	}

	GenericValue transaccion = (GenericValue) output.get("transaccion");

	Debug.log("TRansaccion: " + transaccion);

	// Actualiza los saldos de las cuentas
	//            GenericValue cuentaBancariaOrigen = buscaSaldoCuenta(delegator, cuentaOrigen, fechaContable);
	//            GenericValue cuentaBancariaDestino= buscaSaldoCuenta(delegator, cuentaDestino, fechaContable);
	//			cuentaBancariaOrigen.set("monto",
	//					cuentaBancariaOrigen.getBigDecimal("monto").subtract(amount));
	//			cuentaBancariaDestino.set("monto",
	//					cuentaBancariaDestino.getBigDecimal("monto").add(amount));
	//			delegator.createOrStore(cuentaBancariaOrigen);
	//			delegator.createOrStore(cuentaBancariaDestino);			
	//            

	GenericValue cuentaCargo = delegator.findByPrimaryKey("CuentaBancaria",
			UtilMisc.toMap("cuentaBancariaId", catalogoCargoMap.get(descrSinEspacios)));

	Debug.log("Mike cuenta cargo: " + cuentaCargo);

	GenericValue cuentaAbono = delegator.findByPrimaryKey("CuentaBancaria",
			UtilMisc.toMap("cuentaBancariaId", catalogoAbonoMap.get(descrSinEspacios)));

	Debug.log("Mike cuenta cargo: " + cuentaAbono);

	//        	//Genera registro en detalle de traspasos
	//			GenericValue traspasoBanco = delegator.makeValue("TraspasoBanco");
	//			traspasoBanco.setNonPKFields(context);			
	//			traspasoBanco.put("idTraspaso", idTraspaso);
	//			traspasoBanco.put("bancoIdOrigen", cuentaAbono.getString("bancoId"));
	//			traspasoBanco.put("debitCreditFlagOrigen", "C");
	//			traspasoBanco.put("depositoRetiroFlagOrigen", "R");
	//			traspasoBanco.put("bancoIdDestino", cuentaCargo.getString("bancoId"));
	//			traspasoBanco.put("debitCreditFlagDestino", "D");
	//			traspasoBanco.put("depositoRetiroFlagDestino", "D");
	//			traspasoBanco.put("acctgTransId", transaccion.getString("acctgTransId"));
	//			traspasoBanco.put("cuentaOrigen", cuentaAbono.getString("cuentaBancariaId"));
	//			traspasoBanco.put("cuentaDestino", cuentaCargo.getString("cuentaBancariaId"));
	//			traspasoBanco.put("monto", BigDecimal.valueOf(Double.valueOf((String) montoMap.get(descrSinEspacios))));
	//			traspasoBanco.put("moneda", currency);			
	//			traspasoBanco.put("tipotransaccion", "TRASPASO");
	//			traspasoBanco.put("partyId", organizationPartyId);
	//			traspasoBanco.put("descripcion", descripcion);
	//			traspasoBanco.put("createdByUserLogin", userLogin.getString("userLoginId"));
	//			traspasoBanco.put("fechaContable", fechaContable);
	//			delegator.create(traspasoBanco);
	//			
	//			Debug.log("Mike TraspasoBanco.- " + traspasoBanco);

	//			//Afecta el saldo de cuentas bancarias en acctgTransEntry
	//			EntityCondition conditionsEntryAbono;			
	//				conditionsEntryAbono = EntityCondition.makeCondition(EntityOperator.AND,
	//					EntityCondition.makeCondition(AcctgTransEntry.Fields.acctgTransId.name(), EntityOperator.EQUALS, transaccionId),
	//					EntityCondition.makeCondition(AcctgTransEntry.Fields.debitCreditFlag.name(), EntityOperator.EQUALS, "C"));							
	//			
	//			EntityCondition conditionsEntryCargo;
	//				conditionsEntryCargo = EntityCondition.makeCondition(EntityOperator.AND,
	//					EntityCondition.makeCondition(AcctgTransEntry.Fields.acctgTransId.name(), EntityOperator.EQUALS, transaccionId),
	//					EntityCondition.makeCondition(AcctgTransEntry.Fields.debitCreditFlag.name(), EntityOperator.EQUALS, "D"));
	//			
	//			List<GenericValue> acctgEntryAbono = delegator.findByCondition("AcctgTransEntry", conditionsEntryAbono, null, null);
	//			List<GenericValue> acctgEntryCargo = delegator.findByCondition("AcctgTransEntry", conditionsEntryCargo, null, null);
	//
	//			Iterator<GenericValue> acctgEntryIterAbono = acctgEntryAbono.iterator();
	//			if(acctgEntryIterAbono.hasNext())
	//			{	GenericValue acctgTransEntry = acctgEntryIterAbono.next();								
	//				acctgTransEntry.set("cuentaBancariaId", cuentaDestino);
	//				delegator.createOrStore(acctgTransEntry);
	//			}
	//			
	//			Iterator<GenericValue> acctgEntryIterCargo = acctgEntryCargo.iterator();
	//			if(acctgEntryIterCargo.hasNext())
	//			{	GenericValue acctgTransEntry = acctgEntryIterCargo.next();								
	//				acctgTransEntry.set("cuentaBancariaId", cuentaOrigen);
	//				delegator.createOrStore(acctgTransEntry);
	//			}

	Map result = ServiceUtil.returnSuccess();
	result.put("acctgTransId", transaccion.getString("acctgTransId"));
	return result;

	}catch (GeneralException e) 
	{	return UtilMessage.createAndLogServiceError(e, MODULE);
	}
	}

	/**
	 * Action for the find / list transactions screen.
	 * @param context the screen context
	 * @throws GeneralException if an error occurs
	 * @throws ParseException if an error occurs
	 */
	public static void buscarTraspasos(Map<String, Object> context) throws GeneralException, ParseException {

		final ActionContext ac = new ActionContext(context);
		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
		final Delegator delegator = ac.getDelegator();
		final TimeZone timeZone = ac.getTimeZone();
		Locale locale = ac.getLocale();

		// possible fields we're searching by
		//String partyId = ac.getParameter("partyId");
		String acctgTransId = ac.getParameter("findAcctgTransId");                

		String postedAmountFrom = ac.getParameter("postedAmountFrom");
		String postedAmountThru = ac.getParameter("postedAmountThru");

		String fechaContableDesde = ac.getParameter("fechaContableDesde");
		String fechaContableHasta = ac.getParameter("fechaContableHasta");
		String bancoOrigen = ac.getParameter("bancoOrigen");
		String bancoDestino = ac.getParameter("bancoDestino");
		String cuentaOrigen = ac.getParameter("cuentaOrigen");
		String cuentaDestino = ac.getParameter("cuentaDestino");

		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);

		// instead of using the organization's base currency
		OrganizationRepositoryInterface organizationRepository = dd.getOrganizationDomain().getOrganizationRepository();
		Organization organization = organizationRepository.getOrganizationById(organizationPartyId);
		if (organization != null) {
			ac.put("orgCurrencyUomId", organization.getPartyAcctgPreference().getBaseCurrencyUomId());
		}

		// get the list of bancos for the parametrized form ftl
		List<String> orderByBan = org.ofbiz.base.util.UtilMisc.toList("nombreBanco");
		EntityCondition condicionBan = EntityCondition.makeCondition("partyId",EntityOperator.EQUALS, organizationPartyId);
		Object bancos = delegator.findByCondition("BancosCuentasPorOrganizacion", condicionBan , UtilMisc.toList("bancoId", "nombreBanco"), orderByBan);        

		ac.put("bancos", bancos);        

		String padre = obtenPadre(delegator, organizationPartyId);
		if(padre.equals(organizationPartyId))
			organizationPartyId = null;


		Debug.logInfo("Organizacion a buscar: ", organizationPartyId);

		List<Map<String,Object>> listPageTraspasoBanco = FastList.newInstance();

		if ("Y".equals(ac.getParameter("performFind"))) {

			List<EntityCondition> searchConditions = new FastList<EntityCondition>();

			String dateFormat = UtilDateTime.getDateFormat(locale);
			Timestamp fechaInicialPeriodo = null;

			if(UtilValidate.isNotEmpty(fechaContableDesde)){
				fechaInicialPeriodo = UtilDateTime.getDayStart(UtilDateTime.stringToTimeStamp(fechaContableDesde, dateFormat, timeZone, locale), timeZone, locale);
			}else{
				fechaInicialPeriodo = UtilDateTime.getMonthStart(UtilDateTime.nowTimestamp());
			}

			Timestamp fechaFinalPeriodo = null;

			if(UtilValidate.isNotEmpty(fechaContableHasta)){
				fechaFinalPeriodo = UtilDateTime.getDayEnd(UtilDateTime.stringToTimeStamp(fechaContableHasta, dateFormat, timeZone, locale), timeZone, locale);
			}else{
				fechaFinalPeriodo = UtilDateTime.nowTimestamp();
			}

			if (UtilValidate.isNotEmpty(organizationPartyId)) {
				searchConditions.add(EntityCondition.makeCondition(ConsultaTraspasoBanco.Fields.organizationPartyId.name(), EntityOperator.EQUALS, organizationPartyId));
			} 
			if (UtilValidate.isNotEmpty(acctgTransId)) {
				searchConditions.add(EntityCondition.makeCondition(ConsultaTraspasoBanco.Fields.acctgTransId.name(), EntityOperator.EQUALS, acctgTransId));
			}
			if (UtilValidate.isNotEmpty(bancoOrigen)) {
				searchConditions.add(EntityCondition.makeCondition(ConsultaTraspasoBanco.Fields.bancoOrigenId.name(), EntityOperator.EQUALS, bancoOrigen));
			}
			if (UtilValidate.isNotEmpty(bancoDestino)) {
				searchConditions.add(EntityCondition.makeCondition(ConsultaTraspasoBanco.Fields.bancoDestinoId.name(), EntityOperator.EQUALS, bancoDestino));
			}
			if (UtilValidate.isNotEmpty(cuentaOrigen)) {
				searchConditions.add(EntityCondition.makeCondition(ConsultaTraspasoBanco.Fields.cuentaOrigenId.name(), EntityOperator.EQUALS, cuentaOrigen));
			}
			if (UtilValidate.isNotEmpty(cuentaDestino)) {
				searchConditions.add(EntityCondition.makeCondition(ConsultaTraspasoBanco.Fields.cuentaDestinoId.name(), EntityOperator.EQUALS, cuentaDestino));
			}
			if (UtilValidate.isNotEmpty(postedAmountFrom)) {
				searchConditions.add(EntityCondition.makeCondition(ConsultaTraspasoBanco.Fields.monto.name(), EntityOperator.GREATER_THAN_EQUAL_TO, new BigDecimal(postedAmountFrom)));
			}
			if (UtilValidate.isNotEmpty(postedAmountThru)) {
				searchConditions.add(EntityCondition.makeCondition(ConsultaTraspasoBanco.Fields.monto.name(), EntityOperator.LESS_THAN_EQUAL_TO, new BigDecimal(postedAmountThru)));
			}
			if (UtilValidate.isNotEmpty(fechaInicialPeriodo)) {
				searchConditions.add(EntityCondition.makeCondition(ConsultaTraspasoBanco.Fields.postedDate.name(), EntityOperator.GREATER_THAN_EQUAL_TO, fechaInicialPeriodo));
			}
			if (UtilValidate.isNotEmpty(fechaFinalPeriodo)) {
				searchConditions.add(EntityCondition.makeCondition(ConsultaTraspasoBanco.Fields.postedDate.name(), EntityOperator.LESS_THAN_EQUAL_TO, fechaFinalPeriodo));
			}

			// fields to select           
			List<String> fieldsToSelect = UtilMisc.toList("acctgTransId","descripcion","createdStamp","postedDate","bancoOrigen","bancoDestino","cuentaOrigen","cuentaDestino","monto");
			List<String> orderByTrans = UtilMisc.toList("acctgTransId");

			List<GenericValue> listTraspasoBanco = delegator.findByCondition("ConsultaTraspasoBanco", EntityCondition.makeCondition(searchConditions), fieldsToSelect, orderByTrans);

			Map<String,Object> mapaTraspaso = FastMap.newInstance();
			for (GenericValue TraspasoBanco : listTraspasoBanco) {
				acctgTransId = TraspasoBanco.getString("acctgTransId");
				mapaTraspaso.putAll(TraspasoBanco.getAllFields());
				if(NumberUtils.isNumber(acctgTransId)){//Sobreescribe el id para poder ordenarlo posteriormente
					mapaTraspaso.put("acctgTransId", Long.valueOf(acctgTransId));
				}
				listPageTraspasoBanco.add(mapaTraspaso);
				mapaTraspaso = FastMap.newInstance();
			}
		}

		Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
			public int compare(Map<String, Object> m1, Map<String, Object> m2) {
				if(m1.get("acctgTransId") instanceof Long && m2.get("acctgTransId") instanceof Long){
					return ((Long) m1.get("acctgTransId")).compareTo((Long) m2.get("acctgTransId"));
				} else {
					return (m1.get("acctgTransId").toString().compareTo(m2.get("acctgTransId").toString()));
				}
			}
		};

		Collections.sort(listPageTraspasoBanco, mapComparator);

		ac.put("listPageTraspasoBanco", listPageTraspasoBanco);

	}


	/**
	 * Consulta las cuentas bancarias y sus saldos.
	 * @param context the screen context
	 * @throws GeneralException if an error occurs
	 * @throws ParseException if an error occurs
	 */
	public static void buscarCuentasBanco(Map<String, Object> context) throws GeneralException, ParseException {

		final ActionContext ac = new ActionContext(context);
		Delegator delegator = ac.getDelegator();
		String bancoId = ac.getParameter("bancoId");
		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest()); 
		final String fechaInicial = ac.getParameter("fechaInicialPeriodo");
		final String fechaFinal = ac.getParameter("fechaFinalPeriodo");

		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository(); 

		Locale locale = ac.getLocale();
		final TimeZone timeZone = ac.getTimeZone();
		String dateFormat = UtilDateTime.getDateFormat(locale);

		Timestamp fechaInicialPeriodo = null;

		if(UtilValidate.isNotEmpty(fechaInicial)){
			fechaInicialPeriodo = UtilDateTime.getDayStart(UtilDateTime.stringToTimeStamp(fechaInicial, dateFormat, timeZone, locale), timeZone, locale);
		}else{
			fechaInicialPeriodo = UtilDateTime.getMonthStart(UtilDateTime.nowTimestamp());
		}

		Timestamp fechaFinalPeriodo = null;

		if(UtilValidate.isNotEmpty(fechaFinal)){
			fechaFinalPeriodo = UtilDateTime.getDayEnd(UtilDateTime.stringToTimeStamp(fechaFinal, dateFormat, timeZone, locale), timeZone, locale);
		}else{
			fechaFinalPeriodo = UtilDateTime.nowTimestamp();
		}

		final Map<String,BigDecimal> saldoBanco = obtenSaldoCuentaActualFechas(ledgerRepository, bancoId, organizationPartyId, fechaInicialPeriodo, fechaFinalPeriodo, false);
		final Map<String,BigDecimal> saldoInicialBanco = obtenSaldoCuentaActualFechas(ledgerRepository, bancoId, organizationPartyId, fechaInicialPeriodo, fechaFinalPeriodo, true);

		List<EntityCondition> searchConditions = new FastList<EntityCondition>();                         

		searchConditions.add(EntityCondition.makeCondition("asignacion", EntityOperator.EQUALS, null));
		searchConditions.add(EntityCondition.makeCondition(CuentaSucursalBanco.Fields.partyId.name(), EntityOperator.EQUALS, organizationPartyId)); 
		if (UtilValidate.isNotEmpty(bancoId)) {
			searchConditions.add(EntityCondition.makeCondition("bancoId", EntityOperator.EQUALS, bancoId));
		}

		// fields to select           
		List<String> fieldsToSelect = UtilMisc.toList("cuentaBancariaId", "nombreCuenta", "numeroCuenta", "descripcion", "nombreBanco", "nombreSucursal","nombre","nombreUsoCuenta","moneda","habilitada","bancoId");
		List<String> orderByTrans = UtilMisc.toList("cuentaBancariaId");

		List<GenericValue> listCuentaSucursalBanco = delegator.findByCondition("CuentaSucursalBanco", EntityCondition.makeCondition(searchConditions), fieldsToSelect, orderByTrans);
		List<Map<String, Object>> listMapasCuentas = FastList.newInstance();

		Map<String, Object> mapaCuenta = FastMap.newInstance();
		String cuentaBancariaId = new String();
		for (GenericValue CuentaBancaria : listCuentaSucursalBanco) {
			mapaCuenta.putAll(CuentaBancaria.getAllFields());
			mapaCuenta.put("cuentaBancariaId", Long.valueOf(CuentaBancaria.getString("cuentaBancariaId")));
			cuentaBancariaId = CuentaBancaria.getString("cuentaBancariaId");
			mapaCuenta.put("saldoInicial", UtilNumber.getBigDecimal(saldoInicialBanco.get(cuentaBancariaId)));
			mapaCuenta.put("saldo", UtilNumber.getBigDecimal(saldoBanco.get(cuentaBancariaId)));
			mapaCuenta.put("saldoFinal", UtilNumber.getBigDecimal(saldoInicialBanco.get(cuentaBancariaId)).add(UtilNumber.getBigDecimal(saldoBanco.get(cuentaBancariaId))));
			listMapasCuentas.add(mapaCuenta);
			mapaCuenta = FastMap.newInstance();
		}

		Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
			public int compare(Map<String, Object> m1, Map<String, Object> m2) {
				return ((Long) m1.get("cuentaBancariaId")).compareTo((Long) m2.get("cuentaBancariaId"));
			}
		};

		Collections.sort(listMapasCuentas, mapComparator);

		ac.put("cuentasBancariasListBuilder",listMapasCuentas);

	}

	public static String obtenPadre(Delegator delegator, String organizationId)
			throws RepositoryException, GenericEntityException {
		String parentId = organizationId;
		do {
			Debug.log("partyId: " + parentId);
			organizationId = obtenPartyGroup(delegator, organizationId);

			if (organizationId != null)
				parentId = organizationId;
			Debug.log("partyId2: " + parentId);
		} while (organizationId != null);
		return parentId;
	}

	public static String obtenPartyGroup(Delegator delegator, String party)
			throws GenericEntityException {
		String parentId = null;

		Debug.log("Ingresa a partyGroup con: " + party);
		GenericValue generic = delegator.findByPrimaryKey("PartyGroup",
				UtilMisc.toMap("partyId", party));

		Debug.log("Se obtiene padre: " + generic);
		parentId = generic.getString("Parent_id");

		return parentId;
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

	/**
	 * autor JRRM
	 * @param delegator
	 * @param cuenta
	 * @param periodo
	 * @param monto
	 * @return saldo actual de la cuenta
	 * @throws GenericEntityException 
	 */
	public static BigDecimal obtenSaldoCuentaActual(Delegator delegator, String cuenta,
			Timestamp periodo) throws GenericEntityException {
		Debug.log("Obten Saldo Cuenta Actual");
		// Rango de periodos.
		Calendar date = Calendar.getInstance();
		date.setTime(periodo);
		date.set(Calendar.DAY_OF_MONTH, 1);
		date.set(Calendar.HOUR, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		date.set(Calendar.AM_PM, Calendar.AM);
		Timestamp periodoFin = new Timestamp(date.getTimeInMillis());
		//		date.set(Calendar.MONTH, 0);// Enero
		//		Timestamp periodoInicio = new Timestamp(date.getTimeInMillis());

		// Se debe hacer la suma de todos los periodos anteriores al periodo
		// seleccionado para obtener el saldo actual de la cuenta.
		EntityConditionList<EntityExpr> conditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"catalogoId", EntityOperator.EQUALS, cuenta),
						EntityCondition.makeCondition("tipo",
								EntityOperator.EQUALS, "B"),
								//								EntityCondition.makeCondition("periodo",
								//								EntityOperator.GREATER_THAN_EQUAL_TO,
								//								periodoInicio), 
								EntityCondition.makeCondition(
										"periodo", EntityOperator.LESS_THAN_EQUAL_TO,
										periodoFin)), EntityOperator.AND);

		List<GenericValue> saldoCuenta = delegator.findByCondition(
				"SaldoCatalogo", conditions, null, null);

		// Recorremos la lista sumando el monto
		BigDecimal sumaMonto = BigDecimal.ZERO;
		for (GenericValue saldo : saldoCuenta) {
			Debug.log("saldo: "+saldo.getBigDecimal("monto"));
			sumaMonto = sumaMonto.add(saldo.getBigDecimal("monto"));
		}
		return sumaMonto;
	}

	/**
	 * autor JRRM
	 * @param delegator
	 * @param cuenta
	 * @param periodo
	 * @param monto
	 * @return saldo actual de la cuenta
	 * @throws GenericEntityException 
	 * @throws RepositoryException 
	 */
	public static BigDecimal obtenSaldoCuentaActual(LedgerRepositoryInterface ledgerRepository, String cuenta,
			Timestamp periodo) throws RepositoryException {
		Debug.log("Obten Saldo Cuenta Actual");
		// Rango de periodos.
		Calendar date = Calendar.getInstance();
		date.setTime(periodo);
		date.set(Calendar.DAY_OF_MONTH, 1);
		date.set(Calendar.HOUR, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		date.set(Calendar.AM_PM, Calendar.AM);
		Timestamp periodoFin = new Timestamp(date.getTimeInMillis());
		//		date.set(Calendar.MONTH, 0);// Enero
		//		Timestamp periodoInicio = new Timestamp(date.getTimeInMillis());

		// Se debe hacer la suma de todos los periodos anteriores al periodo
		// seleccionado para obtener el saldo actual de la cuenta.
		EntityConditionList<EntityExpr> conditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"catalogoId", EntityOperator.EQUALS, cuenta),
						EntityCondition.makeCondition("tipo",
								EntityOperator.EQUALS, "B"), EntityCondition.makeCondition(
										"periodo", EntityOperator.LESS_THAN_EQUAL_TO,
										periodoFin)), EntityOperator.AND);

		List<SaldoCatalogo> saldoCuenta = ledgerRepository.findList(SaldoCatalogo.class, conditions);

		// Recorremos la lista sumando el monto
		BigDecimal sumaMonto = BigDecimal.ZERO;
		for (SaldoCatalogo saldo : saldoCuenta) {
			Debug.log("saldo: "+saldo.getBigDecimal("monto"));
			sumaMonto = sumaMonto.add(saldo.getBigDecimal("monto"));
		}
		return sumaMonto;
	}

	/**
	 * autor EPA
	 * si la cuenta es deudora d-c bancos siempre es deudora
	 */
	public static Map<String,BigDecimal> obtenSaldoCuentaActualFechas(LedgerRepositoryInterface ledgerRepository, String bancoId, String organizationPartyId,
			Timestamp fechaInicialPeriodo, Timestamp fechaFinalPeriodo, boolean saldoInicial) throws RepositoryException{
		Debug.log("Obten Saldo Cuenta Actual por fecha");

		List<EntityCondition> conditions = new FastList<EntityCondition>();
		if (UtilValidate.isNotEmpty(bancoId)) {
			conditions.add(EntityCondition.makeCondition("bancoId", EntityOperator.EQUALS, bancoId));
		}
		if (UtilValidate.isNotEmpty(organizationPartyId)) {
			conditions.add(EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS, organizationPartyId));
		}

		List<String> fieldsToSelect = UtilMisc.toList("amount","cuentaBancariaId","debitCreditFlag");
		List<String> orderBy = UtilMisc.toList("cuentaBancariaId");

		if(saldoInicial){
			conditions.add(EntityCondition.makeCondition("postedDate", EntityOperator.LESS_THAN, fechaInicialPeriodo));
			fieldsToSelect.add("saldoInicial");
		} else {
			conditions.add(EntityCondition.makeCondition("postedDate", EntityOperator.GREATER_THAN_EQUAL_TO, fechaInicialPeriodo));
			conditions.add(EntityCondition.makeCondition("postedDate", EntityOperator.LESS_THAN_EQUAL_TO, fechaFinalPeriodo));
		}

		List<SaldosCuentasBancarias> saldoCuenta = ledgerRepository.findList(SaldosCuentasBancarias.class, conditions, fieldsToSelect, orderBy);

		Map<String,BigDecimal> mapaSaldo = FastMap.newInstance();

		// Recorremos la lista sumando el monto
		BigDecimal sumaMonto = BigDecimal.ZERO;
		if(UtilValidate.isNotEmpty(saldoCuenta)){
			String cuentaBancariaIdAnterior = saldoCuenta.get(0).getCuentaBancariaId();
			SaldosCuentasBancarias saldoAnterior = saldoCuenta.get(0);
			for (SaldosCuentasBancarias saldo : saldoCuenta) {
				if(cuentaBancariaIdAnterior.equals(saldo.getCuentaBancariaId())){
					sumaMonto = getSumaMonto(saldo, sumaMonto);
				} else {
					mapaSaldo.put(cuentaBancariaIdAnterior, (saldoInicial ? sumaMonto.add(saldoAnterior.getSaldoInicial()): sumaMonto));
					sumaMonto = BigDecimal.ZERO;
					sumaMonto = getSumaMonto(saldo, sumaMonto);
				}
				cuentaBancariaIdAnterior = saldo.getCuentaBancariaId();
				saldoAnterior = saldo;
			}
			mapaSaldo.put(cuentaBancariaIdAnterior, (saldoInicial ? sumaMonto.add(saldoAnterior.getSaldoInicial()): sumaMonto));
		}

		return mapaSaldo;
	}

	private static BigDecimal getSumaMonto(SaldosCuentasBancarias saldo, BigDecimal sumaMonto){
		if(saldo.getDebitCreditFlag().equals("D")){
			return sumaMonto.add(saldo.getAmount());
		} else {
			return sumaMonto.subtract(saldo.getAmount());
		}
	}

	/**
	 * autor JRRM
	 * @param delegator
	 * @param cuenta
	 * @param periodo
	 * @return
	 * @throws GenericEntityException
	 */
	public static GenericValue buscaSaldoCuenta(Delegator delegator, String cuenta,
			Timestamp periodo) throws GenericEntityException {
		// Rango de periodos.
		Calendar date = Calendar.getInstance();
		date.setTime(periodo);
		date.set(Calendar.DAY_OF_MONTH, 1);
		date.set(Calendar.HOUR, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		date.set(Calendar.AM_PM, Calendar.AM);
		periodo = new Timestamp(date.getTimeInMillis());

		List<GenericValue> generics = delegator.findByAnd("SaldoCatalogo", "catalogoId", cuenta, "tipo", "B", "periodo", periodo);
		GenericValue catalogo;
		if(generics.isEmpty()){
			// Si esta vacio se crea el registro trayendo la info de un mes almacenado.
			List<GenericValue> catalogos = delegator.findByAnd("SaldoCatalogo", "catalogoId", cuenta, "tipo", "B");
			catalogo = catalogos.get(0);
			catalogo.set("secuenciaId", delegator.getNextSeqId("ControlAuxiliar"));
			catalogo.set("periodo", periodo);
			catalogo.set("monto", BigDecimal.ZERO);
		}else{
			catalogo=generics.get(0);
		}
		return catalogo;
	}


	public static Map<String,?> editarCuentaBancaria(DispatchContext dctx, Map<String,?> context) {
		Delegator delegator = dctx.getDelegator();
		String cuentaId = (String) context.get("cuentaId");
		String bancoId = (String) context.get("bancoId");
		String nombreCuenta = (String) context.get("nombreCuenta");
		String numeroCuenta = (String) context.get("numeroCuenta");
		String descripcion = (String) context.get("descripcion");
		BigDecimal montoCuenta = (BigDecimal) context.get("montoCuenta");
		String moneda = (String) context.get("currency");
		String habilitadaId = (String) context.get("habilitadaId");
		String tipoCuentaId = (String) context.get("tipoCuentaId");
		String usoCuentaId = (String) context.get("usoCuentaId");
		try 
		{	
			GenericValue cuenta = delegator.findByPrimaryKey(
					"CuentaBancaria", UtilMisc.toMap("cuentaBancariaId", cuentaId));

			Debug.log("mike cuenta.- " + cuenta);

			List<GenericValue> saldos = obtenerSaldos(delegator, cuentaId);
			List<GenericValue> traspasos = obtenerTraspasos(delegator, cuentaId);

			GenericValue cuentaBancaria = GenericValue.create(delegator.getModelEntity("CuentaBancaria"));						
			cuentaBancaria.set("cuentaBancariaId", cuentaId);
			cuentaBancaria.set("bancoId", bancoId);
			cuentaBancaria.set("nombreCuenta", nombreCuenta);
			cuentaBancaria.set("numeroCuenta", numeroCuenta);			
			cuentaBancaria.set("descripcion", descripcion);
			cuentaBancaria.set("tipoCuentaBancariaId", tipoCuentaId);
			cuentaBancaria.set("usoCuentaBancariaId", usoCuentaId);

			if(habilitadaId.equals("N")){
				Debug.log("mike ingresa a cambiar monto N.- ");
				Iterator<GenericValue> saldoId = saldos.iterator();
				GenericValue s = saldoId.next();
				if(BigDecimal.ZERO.compareTo(s.getBigDecimal("monto")) == 0){
					cuentaBancaria.set("habilitada", habilitadaId);
				}else{
					return ServiceUtil.returnError("No se puede deshabilitar la cuenta; debe estar con saldo cero");
				}
			}else if(habilitadaId.equals("S")){
				Debug.log("mike ingresa a cambiar monto S.- ");
				if(!habilitadaId.equals(cuenta.getString("habilitada"))){
					Iterator<GenericValue> saldoId = saldos.iterator();
					GenericValue s = saldoId.next();
					if(BigDecimal.ZERO.compareTo(s.getBigDecimal("monto")) == 0){
						cuentaBancaria.set("habilitada", habilitadaId);
					}else{
						return ServiceUtil.returnError("No se puede habilitar la cuenta");
					}
				}
			}
			if((montoCuenta.compareTo(cuenta.getBigDecimal("saldoInicial")) != 0) && traspasos.isEmpty()){
				Debug.log("mike ingresa a cambiar monto sin transaccion.- ");
				Iterator<GenericValue> saldoId = saldos.iterator();
				GenericValue s = saldoId.next();
				if((cuenta.getBigDecimal("saldoInicial").compareTo(s.getBigDecimal("monto")) == 0) && saldos.size() == 1){
					cuentaBancaria.set("saldoInicial", montoCuenta);
					cuentaBancaria.set("moneda", moneda);
					s.put("catalogoId",cuentaId);
					s.put("tipoId", "BANCO");
					s.put("tipo", "B");
					s.put("monto", montoCuenta);
					delegator.store(s);
				}else{
					return ServiceUtil.returnError("No se puede modificar el saldo, ya se han realizado operaciones con esta cuenta bancaria");
				}
			}

			delegator.createOrStore(cuentaBancaria);					
		} catch (GeneralException e) {
			return ServiceUtil.returnError("Error al actualizar los datos");
		}
		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("cuentaId", cuentaId);
		result.put("bancoId", bancoId);
		return result;
	}    

	public static List<GenericValue> obtenerSaldos(Delegator delegator, String cuentaId) throws GenericEntityException{
		EntityCondition conditions = EntityCondition.makeCondition(
				EntityOperator.AND, EntityCondition.makeCondition(
						"catalogoId", EntityOperator.EQUALS, cuentaId),
						EntityCondition.makeCondition("tipoId",
								EntityOperator.EQUALS, "BANCO"),
								EntityCondition.makeCondition("tipo",
										EntityOperator.EQUALS, "B"));

		Debug.log("mike condiciones.- " + conditions);

		List<String> orderBy = UtilMisc.toList("periodo DESC");
		List<GenericValue> saldos = delegator.findByCondition(
				"SaldoCatalogo", conditions, null, orderBy);

		Debug.log("mike saldo.- " + saldos);

		return saldos;
	}

	public static List<GenericValue> obtenerTraspasos(Delegator delegator, String cuentaId) throws GenericEntityException{
		EntityCondition conditions = EntityCondition.makeCondition(
				EntityOperator.OR, EntityCondition.makeCondition(
						"cuentaOrigen", EntityOperator.EQUALS, cuentaId),
						EntityCondition.makeCondition("cuentaDestino",
								EntityOperator.EQUALS, cuentaId));

		Debug.log("mike condiciones.- " + conditions);

		List<GenericValue> traspasos = delegator.findByCondition(
				"TraspasoBanco", conditions, null, null);

		Debug.log("mike traspasos.- " + traspasos);

		return traspasos;
	}

	public static void consultaOrdenPagoPatrimonial(Map<String, Object> context) throws GeneralException, ParseException 
	{		final ActionContext ac = new ActionContext(context);
	final Locale locale = ac.getLocale();
	String organizationPartyId = UtilCommon.getOrganizationPartyId(ac
			.getRequest());
	Delegator delegator = ac.getDelegator();

	// possible fields we're searching by
	String idPendienteTesoreria = ac.getParameter("idPendienteTesoreria");		
	String estatusId = ac.getParameter("statusId");
	String auxiliar = ac.getParameter("auxiliar");
	String descripcion = ac.getParameter("descripcion");		


	DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
	final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
			.getLedgerRepository();

	List<EntityCondition> searchConditionsEstatus = new FastList<EntityCondition>();
	searchConditionsEstatus.add(EntityCondition.makeCondition(Estatus.Fields.tipo.name(), EntityOperator.EQUALS, "D"));
	searchConditionsEstatus.add(EntityCondition.makeCondition(Estatus.Fields.tipo.name(), EntityOperator.EQUALS, "V"));
	EntityCondition estatusCondicion = EntityCondition.makeCondition(searchConditionsEstatus,EntityOperator.OR);
	List<Estatus> estatus = ledgerRepository.findList(Estatus.class,estatusCondicion);
	List<Map<String, Object>> estatusList = new FastList<Map<String, Object>>();
	for (Estatus s : estatus) {
		Map<String, Object> map = s.toMap();
		estatusList.add(map);
	}
	ac.put("estatusList", estatusList);


	String vista;
	if ("Y".equals(ac.getParameter("performFind"))) {
		List<EntityCondition> searchConditions = new FastList<EntityCondition>();

		if (UtilValidate.isNotEmpty(idPendienteTesoreria)) {
			searchConditions.add(EntityCondition.makeCondition("idPendienteTesoreria", EntityOperator.EQUALS, idPendienteTesoreria));
		}
		if (UtilValidate.isNotEmpty(estatusId)) {
			searchConditions.add(EntityCondition.makeCondition("estatus",EntityOperator.EQUALS, estatusId));
		}				
		if (UtilValidate.isNotEmpty(descripcion)) {
			searchConditions.add(EntityCondition.makeCondition("descripcion", EntityOperator.LIKE,"%"+descripcion+"%"));
		}			
		//searchConditions.add(EntityCondition.makeCondition("tipo", EntityOperator.EQUALS,"ORDEN_PAGO_PATRI"));
		Debug.logInfo("search conditions : "+ EntityCondition.makeCondition(searchConditions,EntityOperator.AND).toString(), MODULE);
		
		EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);

		List<String> orderBy = UtilMisc.toList("idPendienteTesoreria");
		
		List<String> fieldsToSelect = UtilMisc.toList("idPendienteTesoreria","tipo","descripcion","moneda","fechaTransaccion",
        		"fechaContable","acctgTransTypeId","descripcionEvento","bancoId","cuentaBancariaId","estatus", "descripcionEstatus");
		
		Debug.logInfo("search conditions : "+ EntityCondition.makeCondition(searchConditions,EntityOperator.AND).toString(), MODULE);

		List<GenericValue> listOrdenPagoPatrimonial = delegator.findByCondition("PendienteTesoreriaEventoStatus", condiciones, fieldsToSelect, orderBy);

		List<Map<String,Object>> listOrdenPagoPatrimonialBuilder = FastList.newInstance();
		
		Map<String,Object> mapaOrdenPagoPatrimonial = FastMap.newInstance();
		
		for (GenericValue OrdenPagoPatrimonial : listOrdenPagoPatrimonial) {
			mapaOrdenPagoPatrimonial.putAll(OrdenPagoPatrimonial.getAllFields());
			mapaOrdenPagoPatrimonial.put("idPendienteTesoreria", Long.valueOf(OrdenPagoPatrimonial.getString("idPendienteTesoreria")));
			listOrdenPagoPatrimonialBuilder.add(mapaOrdenPagoPatrimonial);
			mapaOrdenPagoPatrimonial = FastMap.newInstance();
		}
		
		Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
			public int compare(Map<String, Object> m1, Map<String, Object> m2) {
				return ((Long) m1.get("idPendienteTesoreria")).compareTo((Long) m2.get("idPendienteTesoreria"));
			}
		};

		Collections.sort(listOrdenPagoPatrimonialBuilder, mapComparator);

				ac.put("ordenPagoListBuilder", listOrdenPagoPatrimonialBuilder);


	}
	}


	public static void consultaOrdenPago(Map<String, Object> context) throws GeneralException, ParseException 
	{	final ActionContext ac = new ActionContext(context);		
	// possible fields we're searching by				
	String ordenPagoId = ac.getParameter("ordenPagoId");
	String invoiceId = ac.getParameter("invoiceId");
	String acctgTransTypeId = ac.getParameter("acctgTransTypeId");			
	String statusId = ac.getParameter("statusId");									

	DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
	final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
			.getLedgerRepository();

	List<EntityCondition> searchConditionsEstatus = new FastList<EntityCondition>();
	searchConditionsEstatus.add(EntityCondition.makeCondition(Estatus.Fields.tipo.name(), EntityOperator.EQUALS, "D"));
	List<Estatus> estatus = ledgerRepository.findList(Estatus.class,searchConditionsEstatus);
	List<Map<String, Object>> estatusList = new FastList<Map<String, Object>>();
	for (Estatus s : estatus) {
		Map<String, Object> map = s.toMap();
		estatusList.add(map);
	}
	ac.put("estatusList", estatusList);

	if ("Y".equals(ac.getParameter("performFind"))) 
	{	List<EntityCondition> searchConditions = new FastList<EntityCondition>();

	if (UtilValidate.isNotEmpty(ordenPagoId)) {
		searchConditions.add(EntityCondition.makeCondition("ordenPagoId", EntityOperator.EQUALS, ordenPagoId));
	}
	if (UtilValidate.isNotEmpty(invoiceId)) {
		searchConditions.add(EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, invoiceId));
	}
	if (UtilValidate.isNotEmpty(acctgTransTypeId)) {
		searchConditions.add(EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, acctgTransTypeId));
	}				
	if (UtilValidate.isNotEmpty(statusId)) {
		searchConditions.add(EntityCondition.makeCondition("statusId",EntityOperator.EQUALS, statusId));
	}				

	EntityConditionList<EntityExpr> dateCondition = EntityCondition.makeCondition(UtilMisc.toList(
			EntityCondition.makeCondition("idCatalogoPres", EntityOperator.EQUALS, "PRESUPUESTO"),
			EntityCondition.makeCondition("idCatalogoCargo", EntityOperator.EQUALS, "BANCO"), 
			EntityCondition.makeCondition("idCatalogoAbono", EntityOperator.EQUALS, "BANCO")),
			EntityOperator.OR);

	searchConditions.add(dateCondition);

	List<String> orderBy = UtilMisc.toList("ordenPagoId");
	Debug.logInfo("search conditions : "+ EntityCondition.makeCondition(searchConditions,EntityOperator.AND).toString(), MODULE);
	EntityListBuilder ordenListBuilder = null;
	PageBuilder<ConsultaOrdenPago> pageBuilderOrden = null;			

	ordenListBuilder = new EntityListBuilder(
			ledgerRepository, ConsultaOrdenPago.class,EntityCondition.makeCondition(searchConditions,EntityOperator.AND), null, orderBy);
	pageBuilderOrden = new PageBuilder<ConsultaOrdenPago>() 
			{	public List<Map<String, Object>> build(List<ConsultaOrdenPago> page) throws Exception 
		{	List<Map<String, Object>> newPage = FastList.newInstance();
		for (ConsultaOrdenPago ordenPagoId : page) 
		{	Map<String, Object> newRow = FastMap.newInstance();
		newRow.putAll(ordenPagoId.toMap());
		newPage.add(newRow);						
		}
		return newPage;
		}
			};
			ordenListBuilder.setPageBuilder(pageBuilderOrden);
			ac.put("ordenListBuilder", ordenListBuilder);				
	}
	}

	public static Map asignarCuentaBancaria(DispatchContext dctx, Map context) throws Exception 
	{	try
	{	Delegator delegator = dctx.getDelegator();				
	LocalDispatcher dispatcher = dctx.getDispatcher();
	String cuenta = (String) context.get("cuenta");
	String partyId = (String) context.get("partyId");

	//Hacemos la asignacion de la cuenta al usuario.
	GenericValue asignacion = GenericValue.create(delegator.getModelEntity("PartyCuentaBancaria"));
	asignacion.set("cuentaBancariaId", cuenta);
	asignacion.set("partyId", partyId);
	asignacion.set("asignacion", "Y");

	delegator.create(asignacion);

	Map result = ServiceUtil.returnSuccess();
	//result.put("listaTrans", mapaMotor.get("listaTrans"));
	return result;

	}catch (GeneralException e) 
	{	return UtilMessage.createAndLogServiceError(e, MODULE);
	}
	}

	public static void consultaOrdenCobro(Map<String, Object> context) throws GeneralException, ParseException {

		final ActionContext ac = new ActionContext(context);		
		// possible fields we're searching by				
		String ordenCobroId = ac.getParameter("ordenCobroId");
		String invoiceId = ac.getParameter("invoiceId");
		String acctgTransTypeId = ac.getParameter("acctgTransTypeId");			
		String statusId = ac.getParameter("statusId");									
		Delegator delegator = ac.getDelegator();

		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
				.getLedgerRepository();

		List<EntityCondition> searchConditionsEstatus = new FastList<EntityCondition>();
		searchConditionsEstatus.add(EntityCondition.makeCondition(Estatus.Fields.tipo.name(), EntityOperator.EQUALS, "B"));
		List<Estatus> estatus = ledgerRepository.findList(Estatus.class,searchConditionsEstatus);
		List<Map<String, Object>> estatusList = new FastList<Map<String, Object>>();
		for (Estatus s : estatus) {
			Map<String, Object> map = s.toMap();
			estatusList.add(map);
		}
		ac.put("estatusList", estatusList);

		if ("Y".equals(ac.getParameter("performFind"))) {	
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();

			if (UtilValidate.isNotEmpty(ordenCobroId)) {
				searchConditions.add(EntityCondition.makeCondition("ordenCobroId", EntityOperator.EQUALS, ordenCobroId));
			}
			if (UtilValidate.isNotEmpty(invoiceId)) {
				searchConditions.add(EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, invoiceId));
			}
			if (UtilValidate.isNotEmpty(acctgTransTypeId)) {
				searchConditions.add(EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, acctgTransTypeId));
			}				
			if (UtilValidate.isNotEmpty(statusId)) {
				searchConditions.add(EntityCondition.makeCondition("statusId",EntityOperator.EQUALS, statusId));
			}				

			EntityConditionList<EntityExpr> dateCondition = EntityCondition.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("idCatalogoPres", EntityOperator.EQUALS, "PRESUPUESTO"),
					EntityCondition.makeCondition("idCatalogoCargo", EntityOperator.EQUALS, "BANCO"), 
					EntityCondition.makeCondition("idCatalogoAbono", EntityOperator.EQUALS, "BANCO")),
					EntityOperator.OR);

			searchConditions.add(dateCondition);
			
			EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);

			List<String> orderBy = UtilMisc.toList("ordenCobroId");
			
			List<String> fieldsToSelect = UtilMisc.toList("ordenCobroId","invoiceId","acctgTransTypeId","descripcion","partyId",
	        		"groupName","description","statusId","fechaCobro","currencyUomId","monto");
			
			Debug.logInfo("search conditions : "+ EntityCondition.makeCondition(searchConditions,EntityOperator.AND).toString(), MODULE);

			List<GenericValue> listOrdenCobro = delegator.findByCondition("ConsultaOrdenCobro", condiciones, fieldsToSelect, orderBy);

			List<Map<String,Object>> listOrdenCobroBuilder = FastList.newInstance();
			
			Map<String,Object> mapaOrdenCobro = FastMap.newInstance();
			
			for (GenericValue OrdenCobro : listOrdenCobro) {
				mapaOrdenCobro.putAll(OrdenCobro.getAllFields());
				mapaOrdenCobro.put("ordenCobroId", Long.valueOf(OrdenCobro.getString("ordenCobroId")));
				listOrdenCobroBuilder.add(mapaOrdenCobro);
				mapaOrdenCobro = FastMap.newInstance();
			}
			
			Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
				public int compare(Map<String, Object> m1, Map<String, Object> m2) {
					return ((Long) m1.get("ordenCobroId")).compareTo((Long) m2.get("ordenCobroId"));
				}
			};

			Collections.sort(listOrdenCobroBuilder, mapComparator);

			ac.put("ordenCobroListBuilder", listOrdenCobroBuilder);
		}   	
	}

	public static void consultaPagosOrdenPago(Map<String, Object> context)
			throws GeneralException, ParseException {

		final ActionContext ac = new ActionContext(context);
		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();
		Delegator delegator = ac.getDelegator();		
		String ordenPagoId = ac.getParameter("ordenPagoId");						


		List<EntityCondition> searchConditions = new FastList<EntityCondition>();			
		if (UtilValidate.isNotEmpty(ordenPagoId)) {
			searchConditions.add(EntityCondition.makeCondition("ordenPagoId", EntityOperator.EQUALS, ordenPagoId));
		}					

		List<ConsultaPagosOrdenPago> itemsPagosOrdenPago = ledgerRepository.findList(ConsultaPagosOrdenPago.class, searchConditions);	        
		List<Map<String, Object>> listPagosOrdenPago = new FastList<Map<String, Object>>();
		for (ConsultaPagosOrdenPago s : itemsPagosOrdenPago) {
			Map<String, Object> map = s.toMap();	            

			String partyIdFromName = "";
			String partyIdToName = "";					
			if(map.get("partyIdFrom") != null && !map.get("partyIdFrom").equals(""))
			{	GenericValue partyGroup = delegator.findByPrimaryKey("PartyGroup", UtilMisc.toMap("partyId", map.get("partyIdFrom")));
			partyIdFromName = (String) partyGroup.get("groupName");													
			}
			if(map.get("partyIdTo") != null && !map.get("partyIdTo").equals(""))
			{	GenericValue partyGroup = delegator.findByPrimaryKey("PartyGroup", UtilMisc.toMap("partyId", map.get("partyIdTo")));
			partyIdToName = (String) partyGroup.get("groupName");							
			}																	
			map.put("partyIdFromName", partyIdFromName);
			map.put("partyIdToName", partyIdToName);	           	            	          	            
			listPagosOrdenPago.add(map);
		}
		ac.put("listPagosOrdenPago", listPagosOrdenPago);	        	     	        	
	}

	public static void consultaCobrosOrdenCobro(Map<String, Object> context)
			throws GeneralException, ParseException {

		final ActionContext ac = new ActionContext(context);
		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();
		Delegator delegator = ac.getDelegator();		
		String ordenCobroId = ac.getParameter("ordenCobroId");						


		List<EntityCondition> searchConditions = new FastList<EntityCondition>();			
		if (UtilValidate.isNotEmpty(ordenCobroId)) {
			searchConditions.add(EntityCondition.makeCondition("ordenCobroId", EntityOperator.EQUALS, ordenCobroId));
		}					

		List<ConsultaCobrosOrdenCobro> itemsCobrosOrdenCobro = ledgerRepository.findList(ConsultaCobrosOrdenCobro.class, searchConditions);	        
		List<Map<String, Object>> listCobrosOrdenCobro = new FastList<Map<String, Object>>();
		for (ConsultaCobrosOrdenCobro s : itemsCobrosOrdenCobro) {
			Map<String, Object> map = s.toMap();	            

			String partyIdFromName = "";
			String partyIdToName = "";					
			if(map.get("partyIdFrom") != null && !map.get("partyIdFrom").equals(""))
			{	GenericValue partyGroup = delegator.findByPrimaryKey("PartyGroup", UtilMisc.toMap("partyId", map.get("partyIdFrom")));
			partyIdFromName = (String) partyGroup.get("groupName");													
			}
			if(map.get("partyIdTo") != null && !map.get("partyIdTo").equals(""))
			{	GenericValue partyGroup = delegator.findByPrimaryKey("PartyGroup", UtilMisc.toMap("partyId", map.get("partyIdTo")));
			partyIdToName = (String) partyGroup.get("groupName");							
			}																	
			map.put("partyIdFromName", partyIdFromName);
			map.put("partyIdToName", partyIdToName);	           	            	          	            
			listCobrosOrdenCobro.add(map);
		}
		ac.put("listCobrosOrdenCobro", listCobrosOrdenCobro);	        	     	        	
	}
}