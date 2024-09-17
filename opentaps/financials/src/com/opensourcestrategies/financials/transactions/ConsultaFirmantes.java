package com.opensourcestrategies.financials.transactions;

import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.opentaps.base.entities.ReportesListaDetalle;
import org.opentaps.base.entities.Estatus;
import org.opentaps.common.builder.EntityListBuilder;
import org.opentaps.common.builder.PageBuilder;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.domain.DomainsDirectory;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.action.ActionContext;

public class ConsultaFirmantes {
	
	 private static final String MODULE = ConsultaFirmantes.class.getName();
	
	 @SuppressWarnings("unused")
	 public static void buscarReportes(Map<String, Object> context)
				throws GeneralException, ParseException 
	{
	 	final ActionContext ac = new ActionContext(context);
		final Locale locale = ac.getLocale();
		
		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
				.getLedgerRepository();
		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac
				.getRequest());

		// possible fields we're searching by
		String workFlowId = ac.getParameter("workFlowId");
		String reporteId = ac.getParameter("reporteId");
		String statusId = ac.getParameter("statusId");
		
		Debug.log("workFlowId: " + workFlowId);
		Debug.log("reporteId: " + reporteId);
		Debug.log("statusId: " + statusId);
		
		// get the list of estatus for the parametrized form ftl
		List<EntityCondition> searchConditionsStatus = new FastList<EntityCondition>();
		searchConditionsStatus.add(EntityCondition.makeCondition(Estatus.Fields.tipo.name(), EntityOperator.EQUALS, "F"));
		List<Estatus> estatus = ledgerRepository.findList(Estatus.class, searchConditionsStatus);
		List<Map<String, Object>> estatusList = new FastList<Map<String, Object>>();
		for (Estatus s : estatus) {
			Map<String, Object> map = s.toMap();
			estatusList.add(map);
		}
		ac.put("estatusList", estatusList);		
		
		String vista;
		if ("Y".equals(ac.getParameter("performFind"))) {
			// build search conditions
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();

			if (UtilValidate.isNotEmpty(workFlowId)) {
				searchConditions.add(EntityCondition.makeCondition("workFlowId", EntityOperator.EQUALS, workFlowId));
			}
			if (UtilValidate.isNotEmpty(statusId)) {
				searchConditions.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, statusId));
			}
			if (UtilValidate.isNotEmpty(reporteId)) {
				searchConditions.add(EntityCondition.makeCondition("reporteId", EntityOperator.EQUALS, reporteId));
			}								
			
			// fields to select				
			List<String> orderBy = UtilMisc.toList("workFlowId");
			Debug.logInfo("search conditions : " + EntityCondition.makeCondition(searchConditions, EntityOperator.AND).toString(), MODULE);
			EntityListBuilder workFlowListBuilder = null;
			PageBuilder<ReportesListaDetalle> pageBuilderWorkFlow = null;
			
			workFlowListBuilder = new EntityListBuilder(ledgerRepository, ReportesListaDetalle.class,
														EntityCondition.makeCondition(searchConditions,
														EntityOperator.AND), null, orderBy);
				pageBuilderWorkFlow = new PageBuilder<ReportesListaDetalle>() 
				{	public List<Map<String, Object>> build(List<ReportesListaDetalle> page) throws Exception 
					{	Delegator delegator = ac.getDelegator();
						List<Map<String, Object>> newPage = FastList.newInstance();
						for (ReportesListaDetalle workId : page) 
						{	Map<String, Object> newRow = FastMap.newInstance();
							newRow.putAll(workId.toMap());								
							newPage.add(newRow);
						}
						return newPage;
					}
				};
				workFlowListBuilder.setPageBuilder(pageBuilderWorkFlow);
				ac.put("workFlowListBuilder", workFlowListBuilder);
		} 
	}
}
