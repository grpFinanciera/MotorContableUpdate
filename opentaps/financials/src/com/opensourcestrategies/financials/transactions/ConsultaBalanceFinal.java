package com.opensourcestrategies.financials.transactions;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.opentaps.base.entities.GlAccountHistory;
import org.opentaps.common.builder.EntityListBuilder;
import org.opentaps.common.builder.PageBuilder;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.domain.DomainsDirectory;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.action.ActionContext;

public class ConsultaBalanceFinal {

	public static void buscaBalanceFinal(Map<String, Object> context)
			throws GeneralException, ParseException{

		final ActionContext ac = new ActionContext(context);
		Delegator delegator = ac.getDelegator();

		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());

		String customTimePeriodId = ac.getParameter("customTimePeriodId");
		String glAccountId = ac.getParameter("glAccountId");

		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
				.getLedgerRepository();
		final String baseCurrencyUomId = UtilCommon.getOrgBaseCurrency(organizationPartyId, ac.getDelegator());
		try{
			
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();

			if(UtilValidate.isNotEmpty(glAccountId) && glAccountId != null){
				searchConditions.add(EntityCondition.makeCondition("glAccountId", EntityOperator.LIKE, glAccountId+"%"));
			}

			if(UtilValidate.isNotEmpty(organizationPartyId) && organizationPartyId != null){
				searchConditions.add(EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS, organizationPartyId));
			}

			searchConditions.add(EntityCondition.makeCondition("customTimePeriodId", EntityOperator.EQUALS, customTimePeriodId));

//			List<GenericValue> listCiclos = delegator.findByAnd("CustomTimePeriod",UtilMisc.toMap("periodTypeId","FISCAL_YEAR","isClosed","Y"));
//			String diaInicio = null;
//			String customTimePeriodIdAnterior=null;
//			for(int i=0; i<listCiclos.size();i++){
//				if(listCiclos.get(i).getString("customTimePeriodId").equals(customTimePeriodId)){
//					diaInicio = listCiclos.get(i).getString("fromDate");
//					break;
//				}
//			}
//			for(int i=0; i<listCiclos.size();i++){
//				if(listCiclos.get(i).getString("thruDate").equals(diaInicio)){
//					customTimePeriodIdAnterior = listCiclos.get(i).getString("customTimePeriodId");
//				}
//			}

//			final Map <String, BigDecimal> balanceAnterior = FastMap.newInstance();
//
//			if(UtilValidate.isNotEmpty(customTimePeriodIdAnterior)){			
//
//				List<GenericValue> listBalanceAnterior = delegator.findByAnd("GlAccountHistory",UtilMisc.toMap("customTimePeriodId",
//						customTimePeriodIdAnterior,"organizationPartyId",organizationPartyId));
//
//				for (GenericValue balanceA:listBalanceAnterior){
//					balanceAnterior.put(balanceA.getString("glAccountId"), balanceA.getBigDecimal("endingBalance"));
//				}
//
//			}else{
//
//			}		

			EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
			List<String> orderBy = UtilMisc.toList("glAccountId");
			List<GenericValue> balanceFinalList= delegator.findByCondition("GlAccountHistory", condiciones, null, orderBy);
			List<Map<String, Object>> balanceFinalListBuilder = FastList.newInstance();
			Map<String,Object> mapaBalance = FastMap.newInstance();
			for (GenericValue balance : balanceFinalList) {

				BigDecimal endingBalance = null;
//				BigDecimal endingBalanceAnterior = null;

				mapaBalance.put("nombreGlAccount", balance.getRelatedOne("GlAccount").getString("accountName"));

				mapaBalance.put("glAccountId", balance.getString("glAccountId"));

//				if(balance.getString("postedDebits")==(null)){
//					mapaBalance.put("postedDebits", BigDecimal.ZERO);
//				}else{
//					mapaBalance.put("postedDebits", balance.getString("postedDebits"));
//				}
//				if(balance.getString("postedCredits")==(null)){
//					mapaBalance.put("postedCredits", BigDecimal.ZERO);
//				}else{
//					mapaBalance.put("postedCredits", balance.getString("postedCredits"));
//				}
				if(balance.getBigDecimal("endingBalance")==(null)){
					endingBalance = BigDecimal.ZERO;
				}else{
					endingBalance = balance.getBigDecimal("endingBalance");
				}

				mapaBalance.put("endingBalance", endingBalance);

//				if(balanceAnterior.get(balance.getString("glAccountId"))==(null)){
//					endingBalanceAnterior = BigDecimal.ZERO;
//				}else{
//					endingBalanceAnterior = balanceAnterior.get(balance.getString("glAccountId")); 
//				}
//
//				mapaBalance.put("endingBalanceAnterior", endingBalanceAnterior);
				
//				mapaBalance.put("saldoFinal", endingBalance.add(endingBalanceAnterior));
				
				mapaBalance.put("currencyUomId", baseCurrencyUomId);

				balanceFinalListBuilder.add(mapaBalance);
				mapaBalance = FastMap.newInstance();
			}
			ac.put("balanceFinalListBuilder", balanceFinalListBuilder);
		} catch (GenericEntityException e) {
			Debug.log(e);
		}
	}
}
