package org.opentaps.financials.motor;

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
import org.opentaps.base.entities.EventoContable;
import org.opentaps.base.entities.Modulo;
import org.opentaps.common.builder.EntityListBuilder;
import org.opentaps.common.builder.PageBuilder;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.domain.DomainsDirectory;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.action.ActionContext;

public class ConsultarEventos {
	
	 private static final String MODULE = ConsultarEventos.class.getName();
	
	 @SuppressWarnings("unused")
	public static void buscaEventos(Map<String, Object> context) throws GeneralException, ParseException {
		 	
	        final ActionContext ac = new ActionContext(context);
	        final Locale locale = ac.getLocale();
	        String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());

	        // possible fields we're searching by
	        String moduloId = ac.getParameter("moduloId");
	        String acctgTransTypeId = ac.getParameter("acctgTransTypeId");
	        String codigo = ac.getParameter("codigo");

	        DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
	        final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();

	        // get the list of modulos for the parametrized form ftl
	        List<EntityCondition> searchConditionsMod = new FastList<EntityCondition>();
	        searchConditionsMod.add(EntityCondition.makeCondition(Modulo.Fields.uso.name(), EntityOperator.EQUALS, "Y"));
	        List<Modulo> modulos = ledgerRepository.findList(Modulo.class, searchConditionsMod, UtilMisc.toList("nombre"));
	        List<Map<String, Object>> moduloList = new FastList<Map<String, Object>>();
	        for (Modulo s : modulos) {
	            Map<String, Object> map = s.toMap();
	            moduloList.add(map);
	        }
	        ac.put("moduloList", moduloList);

	        // get the list of EventoContables for the parametrized form ftl
	        List<EventoContable> listEventos = ledgerRepository.findAll(EventoContable.class, UtilMisc.toList("descripcion"));
	        List<Map<String, Object>> EventoContablesList = new FastList<Map<String, Object>>();
	        for (EventoContable s : listEventos) {
	            Map<String, Object> map = s.toMap();
	            EventoContablesList.add(map);
	        }
	        ac.put("EventoContablesList", EventoContablesList);
	        
	        if ("Y".equals(ac.getParameter("performFind"))) {
	            // build search conditions
	            List<EntityCondition> searchConditions = new FastList<EntityCondition>();
	           
	            if (UtilValidate.isNotEmpty(moduloId)) {
	                searchConditions.add(EntityCondition.makeCondition(EventoContable.Fields.moduloId.name(), EntityOperator.EQUALS, moduloId));
	            }
	            if (UtilValidate.isNotEmpty(acctgTransTypeId)) {
	                searchConditions.add(EntityCondition.makeCondition(EventoContable.Fields.acctgTransTypeId.name(), EntityOperator.EQUALS, acctgTransTypeId));
	            }
	            if (UtilValidate.isEmpty(acctgTransTypeId) && UtilValidate.isNotEmpty(codigo)) {
	                searchConditions.add(EntityCondition.makeCondition(EventoContable.Fields.acctgTransTypeId.name(), EntityOperator.LIKE, "%"+codigo+"%"));
	            }
	            
	            // fields to select
	            List<String> fieldsToSelect = UtilMisc.toList("acctgTransTypeId", "descripcion");
	            Debug.logInfo("search conditions : " + EntityCondition.makeCondition(searchConditions, EntityOperator.AND).toString(), MODULE);
	            EntityListBuilder EventoContableListBuilder = new EntityListBuilder(ledgerRepository, EventoContable.class, EntityCondition.makeCondition(searchConditions, EntityOperator.AND), fieldsToSelect, UtilMisc.toList(EventoContable.Fields.descripcion.desc()));
	            PageBuilder<EventoContable> pageBuilder = new PageBuilder<EventoContable>() {
	                public List<Map<String, Object>> build(List<EventoContable> page) throws Exception {
	                    Delegator delegator = ac.getDelegator();
	                    List<Map<String, Object>> newPage = FastList.newInstance();
	                    for (EventoContable eventoCont : page) {
	                        Map<String, Object> newRow = FastMap.newInstance();
	                        newRow.putAll(eventoCont.toMap());

	                        EventoContable doc = ledgerRepository.findOneCache(EventoContable.class, ledgerRepository.map(EventoContable.Fields.acctgTransTypeId, eventoCont.getAcctgTransTypeId()));
	                        newRow.put("idTipoDocDescripcion", doc.get(EventoContable.Fields.descripcion.name(), locale));
	                        newPage.add(newRow);
	                    }
	                    return newPage;
	                }
	            };
	            EventoContableListBuilder.setPageBuilder(pageBuilder);
	            ac.put("EventoContableListBuilder", EventoContableListBuilder);
	        }
	 }
}
