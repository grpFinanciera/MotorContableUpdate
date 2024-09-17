package com.opensourcestrategies.financials.transactions;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.order.finaccount.UtilClavePresupuestal;
import org.ofbiz.party.party.PartyHelper;
import org.opentaps.base.entities.ConsultaClavePresupuestalPoliza;
import org.opentaps.base.entities.Momento;
import org.opentaps.common.builder.EntityListBuilder;
import org.opentaps.common.builder.PageBuilder;
import org.opentaps.common.util.UtilAccountingTags;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.domain.DomainsDirectory;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.action.ActionContext;

public class ConsultaClavesPresupuestalesPolizas {

	public static void buscaClavePoliza(Map<String, Object> context)
			throws GeneralException, ParseException {

		final ActionContext ac = new ActionContext(context);
		Locale locale = ac.getLocale();
		final TimeZone timeZone = ac.getTimeZone();


		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
		String ciclo = UtilCommon.getCicloId(ac.getRequest());

		// possible fields we're searching by
		String momentoId = ac.getParameter("momentoId");
		String poliza = ac.getParameter("poliza");
		String tipoPolizaId = ac.getParameter("tipoPolizaId");
		String fechaInicialPeriodo = ac.getParameter("fechaInicialPeriodo");
		String fechaFinalPeriodo = ac.getParameter("fechaFinalPeriodo");
		String tipoMovimiento = ac.getParameter("tipoMovimiento");

		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
				.getLedgerRepository();

		Delegator delegator = ac.getDelegator();
		List<Map<String, Object>> tiposClave = new FastList<Map<String, Object>>();
		Map<String, Object> tipos = new HashMap<String, Object>();
		tipos.put("id", "Ingreso");
		tiposClave.add(tipos);
		tipos = new HashMap<String, Object>();
		tipos.put("id", "Egreso");
		tiposClave.add(tipos);
		ac.put("tiposClave", tiposClave);

		if (ac.getParameter("ingresoEgreso") == null
				|| ac.getParameter("ingresoEgreso").equalsIgnoreCase("Ingreso")) {
			ac.put("tagTypes", UtilAccountingTags
					.getClassificationTagsForOrganization(organizationPartyId,ciclo,UtilAccountingTags.INGRESO_TAG, delegator));

			EntityCondition condition = EntityCondition.makeCondition("cuentaAsociada" ,EntityOperator.LIKE, "8.1%");

			List<Momento> momentos = ledgerRepository.findList(Momento.class, condition);
			List<Map<String, Object>> momentoList = new FastList<Map<String, Object>>();
			for (Momento momento : momentos) {
				Map<String, Object> map = momento.toMap();
				momentoList.add(map);
			}
			ac.put("momentos", momentoList);

			ac.put("ingresoEgreso", "Ingreso");
		} else {
			ac.put("tagTypes", UtilAccountingTags
					.getClassificationTagsForOrganization(organizationPartyId,ciclo,
							UtilAccountingTags.EGRESO_TAG, delegator));

			EntityCondition condition = EntityCondition.makeCondition("cuentaAsociada" ,EntityOperator.LIKE, "8.2%");

			List<Momento> momentos = ledgerRepository.findList(Momento.class, condition);
			List<Map<String, Object>> momentoList = new FastList<Map<String, Object>>();
			for (Momento momento : momentos) {
				Map<String, Object> map = momento.toMap();
				momentoList.add(map);
			}
			ac.put("momentos", momentoList);

			ac.put("ingresoEgreso", "Egreso");
		}

		if ("Y".equals(ac.getParameter("performFind"))) {
				String dateFormat = UtilDateTime.getDateFormat(locale);
				// build search conditions
				List<EntityCondition> searchConditions = new FastList<EntityCondition>();
				
				searchConditions.add(EntityCondition.makeCondition("tipo", EntityOperator.EQUALS, ac.getParameter("ingresoEgreso")));
				
				if (UtilValidate.isNotEmpty(momentoId) && momentoId != null) {
				searchConditions.add(EntityCondition.makeCondition("momentoId", EntityOperator.EQUALS, ac.getParameter("momentoId")));
				}

				GenericValue clasifPres = delegator.makeValue("ConsultaClavePresupuestalPoliza");
//				for (int i = 1; i < 16; i++) {
//
//					String clasif = (String) ac.getParameter("clasificacion" + i);
//					clasifPres.set("clasificacion" + i, clasif);
//
//				}

				int indice = UtilClavePresupuestal.indiceClasAdmin(ac.getParameter("ingresoEgreso") , organizationPartyId, delegator,ciclo);
				GenericValue estructuraPresup = UtilClavePresupuestal.obtenEstructPresupuestal(ac.getParameter("ingresoEgreso"), organizationPartyId, delegator,ciclo);
				for (int i = 1; i < 16; i++) {
					
					String clasificacion = (String) ac.getParameter("clasificacion" + i);
					if(indice == i){
						clasifPres.set("acctgTagEnumIdAdmin", clasificacion);
						if (UtilValidate.isNotEmpty(clasifPres.get("acctgTagEnumIdAdmin")) && clasifPres.get("acctgTagEnumIdAdmin") != null) {
							searchConditions.add(EntityCondition.makeCondition("acctgTagEnumIdAdmin", EntityOperator.EQUALS, clasifPres.get("acctgTagEnumIdAdmin")));
						}
					}else{
						clasifPres.set("acctgTagEnumId"+i, 
								UtilClavePresupuestal.obtenEnumId(clasificacion, estructuraPresup.getString("clasificacion"+i), delegator));
						
						if (UtilValidate.isNotEmpty(clasifPres.get("acctgTagEnumId"+i)) && clasifPres.get("acctgTagEnumId"+i) != null) {
							searchConditions.add(EntityCondition.makeCondition("acctgTagEnumId"+i, EntityOperator.EQUALS, clasifPres.get("acctgTagEnumId"+i)));
						}
					}

				}
				

				if (UtilValidate.isNotEmpty(poliza) && poliza != null) {
					searchConditions.add(EntityCondition.makeCondition("poliza", EntityOperator.LIKE, "%"+poliza+"%"));
				}         
				if (UtilValidate.isNotEmpty(tipoPolizaId) && tipoPolizaId != null) {
					searchConditions.add(EntityCondition.makeCondition("tipoPolizaId", EntityOperator.EQUALS, tipoPolizaId));        
				}
				if (UtilValidate.isNotEmpty(fechaInicialPeriodo)&&fechaInicialPeriodo != null) {
					searchConditions.add(EntityCondition.makeCondition("postedDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.getDayStart(UtilDateTime.stringToTimeStamp(fechaInicialPeriodo, dateFormat, timeZone, locale), timeZone, locale)));
				}
				if (UtilValidate.isNotEmpty(fechaFinalPeriodo) && fechaFinalPeriodo != null) {
					searchConditions.add(EntityCondition.makeCondition("postedDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.getDayEnd(UtilDateTime.stringToTimeStamp(fechaFinalPeriodo, dateFormat, timeZone, locale), timeZone, locale)));
				}
				if (UtilValidate.isNotEmpty(tipoMovimiento) && tipoMovimiento != null && !tipoMovimiento.equals("A")) {
					searchConditions.add(EntityCondition.makeCondition("debitCreditFlag", EntityOperator.EQUALS, tipoMovimiento));   
				}
				
				searchConditions.add(EntityCondition.makeCondition("clavePresupuestal", EntityOperator.NOT_EQUAL, null));

//				List<String> fieldsToSelect = UtilMisc.toList("clavePresupuestal","postedDate","description","acctgTransTypeId","descripcionMomento","debitCreditFlag","poliza","amount", "firstName");
				EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
				
				List<String> orderBy = UtilMisc.toList("clavePresupuestal");
				EntityListBuilder clavesListBuilder = new EntityListBuilder(ledgerRepository, ConsultaClavePresupuestalPoliza.class, condiciones, null, orderBy);
//				List<GenericValue> clave = delegator.findByCondition("ConsultaClavePresupuestalPoliza", condiciones, fieldsToSelect, orderBy);
				
//				Debug.log("Lista de ConsultaClavePresupuestalPoliza "+clave);
				
				PageBuilder<ConsultaClavePresupuestalPoliza> pageBuilder = new PageBuilder<ConsultaClavePresupuestalPoliza>() {
					public List<Map<String, Object>> build(List<ConsultaClavePresupuestalPoliza> page) throws Exception {
						List<Map<String, Object>> newPage = FastList.newInstance();
						
						Delegator delegator = ac.getDelegator();
						
						for (ConsultaClavePresupuestalPoliza clavesList : page) {
							Map<String, Object> newRow = FastMap.newInstance();
//							newRow.putAll(clavesList.toMap());
							
							newRow.put("clavePresupuestal", clavesList.getClavePresupuestal());
							
							if(clavesList.getPostedDate()==(null)){
								newRow.put("postedDate", "");
							}else{
								newRow.put("postedDate", clavesList.getPostedDate());
							}
							
							if(clavesList.getDescription()==(null)){
								newRow.put("description", "");
							}else{
								newRow.put("description", clavesList.getDescription());
							}
							
							if(clavesList.getAcctgTransTypeId()==(null)){
								newRow.put("acctgTransTypeId", "");
							}else{
								newRow.put("acctgTransTypeId", clavesList.getAcctgTransTypeId());
							}
							
							if(clavesList.getDescripcionMomento()==(null)){
								newRow.put("descripcionMomento", "");
							}else{
								newRow.put("descripcionMomento", clavesList.getDescripcionMomento());
							}
							
							
							if(clavesList.getDebitCreditFlag().equals("C")){
								newRow.put("debitCreditFlag", "Abono");
							}else if(clavesList.getDebitCreditFlag().equals("D")){
								newRow.put("debitCreditFlag", "Cargo");
							}
							
							if(clavesList.getPoliza()==(null)){
								newRow.put("poliza", "");
							}else{
								newRow.put("poliza", clavesList.getPoliza());
							}
							
							newRow.put("amount", clavesList.getAmount());
							
							if(clavesList.getCurrencyUomId()==(null)){
								newRow.put("currencyUomId", "");
							}else{
								newRow.put("currencyUomId", clavesList.getCurrencyUomId());
							}
							
							if(clavesList.getAcctgTransId()==(null)){
								newRow.put("acctgTransId", "");
							}else{
								newRow.put("acctgTransId", clavesList.getAcctgTransId());
							}
							
							if(PartyHelper.getPartyName(delegator, clavesList.getPartyId(),false)==(null)){
								newRow.put("name", "");
							}else{
								newRow.put("name",  PartyHelper.getPartyName(delegator, clavesList.getPartyId(), false));
							}
							
							newPage.add(newRow);

						}

						return newPage;	                
					}
				};

				clavesListBuilder.setPageBuilder(pageBuilder);
				ac.put("clavesListBuilder", clavesListBuilder);

		}

	}	

}
