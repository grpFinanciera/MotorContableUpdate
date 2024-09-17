package com.absoluciones.obras;

import java.sql.Timestamp;
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
import org.opentaps.base.entities.BuscarContrato;
import org.opentaps.base.entities.Estatus;
import org.opentaps.base.entities.Obra;
import org.opentaps.common.builder.EntityListBuilder;
import org.opentaps.common.builder.PageBuilder;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.domain.DomainsDirectory;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.action.ActionContext;

public class ConsultaObra {

	private static final String MODULE = ConsultaObra.class.getName();

	@SuppressWarnings("unused")
	public static void buscarObra(Map<String, Object> context)
			throws GeneralException, ParseException {

		final ActionContext ac = new ActionContext(context);
		final Locale locale = ac.getLocale();
		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac
				.getRequest());

		// possible fields we're searching by
		String obraId = ac.getParameter("obraId");		
		String estatusId = ac.getParameter("statusId");
		String productId = ac.getParameter("productId");
		String nombreObra = ac.getParameter("nombreObra");		
		

		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
				.getLedgerRepository();

		// get the list of estatus for the parametrized form ftl
		List<EntityCondition> searchConditionsMod = new FastList<EntityCondition>();
		searchConditionsMod.add(EntityCondition.makeCondition(
				Estatus.Fields.tipo.name(), EntityOperator.EQUALS, "O"));
		List<Estatus> estatus = ledgerRepository.findList(Estatus.class,
				searchConditionsMod);
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

			if (UtilValidate.isNotEmpty(obraId)) {
				searchConditions.add(EntityCondition.makeCondition(
						"obraId", EntityOperator.EQUALS, obraId));
			}
			if (UtilValidate.isNotEmpty(estatusId)) {
				searchConditions.add(EntityCondition.makeCondition("estatus",
						EntityOperator.EQUALS, estatusId));
			}
			if (UtilValidate.isNotEmpty(productId)) {
				searchConditions.add(EntityCondition.makeCondition("productId",
						EntityOperator.EQUALS, productId));
			}
			if (UtilValidate.isNotEmpty(nombreObra)) {
				searchConditions
						.add(EntityCondition.makeCondition(
								"nombre", EntityOperator.LIKE,
								nombreObra));
			}			
			
			// fields to select
			List<String> fieldsToSelect = UtilMisc.toList("obraId, nombre, descripcion, statusId, montoAutorizado, valorObra, productId");
			List<String> orderBy = UtilMisc.toList("obraId");
			Debug.logInfo(
					"search conditions : "
							+ EntityCondition.makeCondition(searchConditions,
									EntityOperator.AND).toString(), MODULE);
			EntityListBuilder obraListBuilder = null;
			PageBuilder<Obra> pageBuilderObra = null;			

			
			obraListBuilder = new EntityListBuilder(
					ledgerRepository, Obra.class,EntityCondition.makeCondition(searchConditions,EntityOperator.AND), fieldsToSelect, orderBy);
			pageBuilderObra = new PageBuilder<Obra>() 
			{	public List<Map<String, Object>> build(List<Obra> page) throws Exception 
				{	Delegator delegator = ac.getDelegator();
					List<Map<String, Object>> newPage = FastList.newInstance();
					for (Obra obrId : page) 
					{	Map<String, Object> newRow = FastMap.newInstance();
						newRow.putAll(obrId.toMap());							
						newPage.add(newRow);
					}
					return newPage;
				}
			};
			obraListBuilder.setPageBuilder(pageBuilderObra);
			ac.put("obraListBuilder", obraListBuilder);
			

		}
	}
		
	public static void buscarContrato(Map<String, Object> context)
			throws GeneralException, ParseException {

		final ActionContext ac = new ActionContext(context);		

		// possible fields we're searching by
		String obraId = ac.getParameter("obraId");	
		String contratoId = ac.getParameter("contratoId");
		String numContrato = ac.getParameter("numContrato");
		String contratistaId = ac.getParameter("contratistaId");
		String productId = ac.getParameter("productId");
		String descripcion = ac.getParameter("descripcion");		
		
		Debug.log("obraId: "+  obraId);

		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
				.getLedgerRepository();

		// get the list of estatus for the parametrized form ftl
		List<EntityCondition> searchConditionsMod = new FastList<EntityCondition>();
		searchConditionsMod.add(EntityCondition.makeCondition(
				Estatus.Fields.tipo.name(), EntityOperator.EQUALS, "O"));
		List<Estatus> estatus = ledgerRepository.findList(Estatus.class,
				searchConditionsMod);
		List<Map<String, Object>> estatusList = new FastList<Map<String, Object>>();
		for (Estatus s : estatus) {
			Map<String, Object> map = s.toMap();
			estatusList.add(map);
		}
		ac.put("estatusList", estatusList);
		
		ac.put("obraId", obraId);		
		
		if ("Y".equals(ac.getParameter("performFind"))) {
			// build search conditions
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();

			if (UtilValidate.isNotEmpty(obraId)) 
			{	searchConditions.add(EntityCondition.makeCondition("obraId", EntityOperator.EQUALS, obraId));
			}
			if (UtilValidate.isNotEmpty(contratoId)) 
			{	searchConditions.add(EntityCondition.makeCondition("contratoId", EntityOperator.EQUALS, contratoId));
			}
			if (UtilValidate.isNotEmpty(numContrato)) 
			{	searchConditions.add(EntityCondition.makeCondition("numContrato", EntityOperator.EQUALS, numContrato));
			}
			if (UtilValidate.isNotEmpty(contratistaId)) 
			{	searchConditions.add(EntityCondition.makeCondition("contratistaId", EntityOperator.EQUALS, contratistaId));
			}
			if (UtilValidate.isNotEmpty(productId)) 
			{	searchConditions.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId));
			}
			if (UtilValidate.isNotEmpty(descripcion)) 
			{	searchConditions.add(EntityCondition.makeCondition("descripcion", EntityOperator.LIKE, descripcion));
			}
			
			// fields to select
			List<String> fieldsToSelect = UtilMisc.toList("obraId, secuencia, contratoId, numContrato, contratistaId, groupName, productId, internalName, descripcion, statusId");
			List<String> orderBy = UtilMisc.toList("contratoId");
			Debug.logInfo("search conditions : " + EntityCondition.makeCondition(searchConditions, EntityOperator.AND).toString(), MODULE);
			EntityListBuilder contratoListBuilder = null;
			PageBuilder<BuscarContrato> pageBuilderContrato = null;			

			
			contratoListBuilder = new EntityListBuilder(
					ledgerRepository, BuscarContrato.class,EntityCondition.makeCondition(searchConditions,EntityOperator.AND), fieldsToSelect, orderBy);
			pageBuilderContrato = new PageBuilder<BuscarContrato>() 
			{	public List<Map<String, Object>> build(List<BuscarContrato> page) throws Exception 
				{	List<Map<String, Object>> newPage = FastList.newInstance();
					for (BuscarContrato contId : page) 
					{	Map<String, Object> newRow = FastMap.newInstance();
						newRow.putAll(contId.toMap());							
						newPage.add(newRow);
					}
					return newPage;
				}
			};
			contratoListBuilder.setPageBuilder(pageBuilderContrato);
			ac.put("contratoListBuilder", contratoListBuilder);
			

		}
	}
	
	public static void inicioObra(Map<String, Object> context)
			throws GeneralException, ParseException {

		final ActionContext ac = new ActionContext(context);		

		// possible fields we're searching by
		String obraId = ac.getParameter("obraId");				
		String productId = ac.getParameter("productId");
		String nombreObra = ac.getParameter("nombreObra");		
		

		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
				.getLedgerRepository();
							
		if ("Y".equals(ac.getParameter("performFind"))) {
			// build search conditions
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();

			if (UtilValidate.isNotEmpty(obraId)) {
				searchConditions.add(EntityCondition.makeCondition("obraId", EntityOperator.EQUALS, obraId));
			}			
			if (UtilValidate.isNotEmpty(productId)) {
				searchConditions.add(EntityCondition.makeCondition("productId",EntityOperator.EQUALS, productId));
			}
			if (UtilValidate.isNotEmpty(nombreObra)) {
				searchConditions.add(EntityCondition.makeCondition("nombre", EntityOperator.LIKE, nombreObra));
			}			
			//searchConditions.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "APROBADA_O"));
			
			// fields to select
			List<String> fieldsToSelect = UtilMisc.toList("obraId, nombre, descripcion, statusId, montoAutorizado, valorObra, productId");
			List<String> orderBy = UtilMisc.toList("obraId");
			Debug.logInfo(
					"search conditions : "
							+ EntityCondition.makeCondition(searchConditions,
									EntityOperator.AND).toString(), MODULE);
			EntityListBuilder obraInicioListBuilder = null;
			PageBuilder<Obra> pageBuilderObraInicio = null;			

			
			obraInicioListBuilder = new EntityListBuilder(
					ledgerRepository, Obra.class,EntityCondition.makeCondition(searchConditions,EntityOperator.AND), fieldsToSelect, orderBy);
			pageBuilderObraInicio = new PageBuilder<Obra>() 
			{	public List<Map<String, Object>> build(List<Obra> page) throws Exception 
				{	List<Map<String, Object>> newPage = FastList.newInstance();
					for (Obra obrId : page) 
					{	Map<String, Object> newRow = FastMap.newInstance();
						newRow.putAll(obrId.toMap());							
						newPage.add(newRow);
					}
					return newPage;
				}
			};
			obraInicioListBuilder.setPageBuilder(pageBuilderObraInicio);
			ac.put("obraInicioListBuilder", obraInicioListBuilder);
			

		}
	}
		
	@SuppressWarnings("deprecation")
	public static void consultaMeses(Map<String, Object> context)
			throws GeneralException, ParseException {
		Debug.log("Entra a consultaMeses");

		final ActionContext ac = new ActionContext(context);		

		// possible fields we're searching by
		String obraId = ac.getParameter("obraId");							

		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
				.getLedgerRepository();


		Timestamp fechaInicio = null;
		Timestamp fechaFin = null;
		List<EntityCondition> searchConditionsMod = new FastList<EntityCondition>();
		searchConditionsMod.add(EntityCondition.makeCondition(Obra.Fields.obraId.name(), EntityOperator.EQUALS, obraId));
		List<Obra> fechasObra = ledgerRepository.findList(Obra.class, searchConditionsMod, UtilMisc.toList("fechaInicio", "fechaFin"), UtilMisc.toList("fechaInicio"));
		if(!fechasObra.isEmpty())
		{	fechaInicio = fechasObra.get(0).getTimestamp("fechaInicio");
			fechaFin = fechasObra.get(0).getTimestamp("fechaFin");											
			//int numMeses = fechaInicio.getMonth() - fechaFin.getMonth();			
			int anioInicio = fechaInicio.getYear()+1900;			
			int anioFin = fechaFin.getYear()+1900;
			int mesInicio = (fechaInicio.getMonth())+1;			
			int mesFin = (fechaFin.getMonth())+1;		
			
			int mesInicioAux = (fechaInicio.getMonth())+1;
			int anioInicioAux = fechaInicio.getYear()+1900;			
			
			int numMeses = 1;
			boolean sumaMes = true;
			while(sumaMes)
			{	if(anioInicioAux <= anioFin)
				{	if(mesInicioAux == mesFin)
					{	sumaMes = false;					
					}					
					else if(mesInicioAux == 12)
					{	mesInicioAux = 1;	
						anioInicioAux++;
						numMeses++;
					}
					else
					{	mesInicioAux++;
						numMeses++;
					}							
				}
				
			}
			Debug.log("Omar-numMesesFinal: " + numMeses);
			
			ac.put("numMeses", numMeses);
			ac.put("mesInicio", mesInicio);
			ac.put("anioInicio", anioInicio);
			ac.put("fechaInicio", fechaInicio);
		}
					
	}		
}


