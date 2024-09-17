package org.opentaps.proyectos.principal;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.opentaps.base.entities.Estatus;
import org.opentaps.common.util.UtilClassification;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.domain.DomainsDirectory;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.action.ActionContext;
import org.opentaps.foundation.repository.RepositoryException;

import javolution.util.FastList;
import javolution.util.FastMap;

public class ConsultarProyecto {
	
	private static final String MODULE = ConsultarProyecto.class.getName();

	/**
	 * Metodo para consultar la requisicion
	 * @param context
	 */
	public static void buscaProyecto(Map<String, Object> context) {
		
		try {

		final ActionContext ac = new ActionContext(context);
		final Locale locale = ac.getLocale();
		TimeZone timeZone = ac.getTimeZone();
		String dateTimeFormat = UtilDateTime.getDateFormat(locale);
		
		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
		Delegator delegator = ac.getDelegator();

		// possible fields we're searching by
		String proyectoId = ac.getParameter("proyectoId");
		String codigoProyecto = ac.getParameter("codigoProyecto");
		String estatusId = ac.getParameter("statusId");
		String areaProyecto = ac.getParameter("partyId");
		Timestamp fechaContable_inicial = null;
		Timestamp fechaContable_final=null;
		
		
		if(UtilValidate.isNotEmpty(ac.getParameter("fechaContable"))) {
			
			String fechaContable = ac.getParameter("fechaContable");
			fechaContable_inicial = UtilDateTime.getDayStart(
						UtilDateTime.getTimestamp(
								UtilDateTime.getDateFromString(dateTimeFormat, timeZone, locale, fechaContable).getTime()));
			fechaContable_final = UtilDateTime.getDayEnd(fechaContable_inicial);
			
		}
	
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String usuario = null, area = null;
		
		usuario = userLogin.getString("partyId");
		Debug.logInfo("usuario: " + userLogin,MODULE);
		
		

		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
				.getLedgerRepository();

		// get the list of estatus for the parametrized form ftl
		List<EntityCondition> searchConditionsMod = new FastList<EntityCondition>();
		searchConditionsMod.add(EntityCondition.makeCondition(
				Estatus.Fields.tipo.name(), EntityOperator.EQUALS, "Y"));
		List<Estatus> estatus = ledgerRepository.findList(Estatus.class,
				searchConditionsMod);
		List<Map<String, Object>> estatusList = new FastList<Map<String, Object>>();
		for (Estatus s : estatus) {
			Map<String, Object> map = s.toMap();
			estatusList.add(map);
		}
		
		// get the list of estatus for the parametrized form ftl
				
			List<GenericValue> areas = UtilClassification.getListaCatalogos("NivelesParty", delegator, "",true);
		
		ac.put("estatusList", estatusList);
		ac.put("listAreas", areas);
		
		if ("Y".equals(ac.getParameter("performFind"))) {
			// build search conditions
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();
			
			if (UtilValidate.isNotEmpty(fechaContable_inicial)) {
				searchConditions.add(EntityCondition.makeCondition(
						"fechaContable", EntityOperator.GREATER_THAN_EQUAL_TO, fechaContable_inicial));
				searchConditions.add(EntityCondition.makeCondition(
						"fechaContable", EntityOperator.LESS_THAN_EQUAL_TO, fechaContable_final));
			}
			
			GenericValue partyRole = delegator.findByPrimaryKey("PartyRole", UtilMisc.toMap("partyId",usuario, "roleTypeId", "ADMPROY"));
			
			if(partyRole == null){
				
				List<GenericValue> responsable = delegator.findByCondition("Proyecto", EntityCondition.makeCondition("responsable", usuario), null, null);
				if (UtilValidate.isNotEmpty(responsable)){
					searchConditions.add(EntityCondition.makeCondition("responsable", EntityOperator.EQUALS,usuario));
				}else{
					searchConditions.add(EntityCondition.makeCondition("responsableTecnico", EntityOperator.EQUALS,usuario));
				}
				
		}

			if (UtilValidate.isNotEmpty(proyectoId)) {
				searchConditions.add(EntityCondition.makeCondition("proyectoId", proyectoId));
			}
			if (UtilValidate.isNotEmpty(codigoProyecto)) {
				searchConditions.add(EntityCondition.makeCondition("codigoProyecto", codigoProyecto));
			}
			if (UtilValidate.isNotEmpty(estatusId)) {
				searchConditions.add(EntityCondition.makeCondition("statusId", estatusId));
			}
			if (UtilValidate.isNotEmpty(areaProyecto)) {
				searchConditions.add(EntityCondition.makeCondition("areaId", areaProyecto));
			}
			if (UtilValidate.isNotEmpty(area)) {
				searchConditions.add(EntityCondition.makeCondition("areaPartyId", area));
			}
			if (UtilValidate.isNotEmpty(organizationPartyId)) {
				searchConditions.add(EntityCondition.makeCondition("organizationPartyId", organizationPartyId));
			}
			
			// fields to select
			
			EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
			
			List<String> orderBy = UtilMisc.toList("proyectoId");
						
			Debug.logInfo(
					"search conditions : "
							+ EntityCondition.makeCondition(searchConditions,
									EntityOperator.AND).toString(), MODULE);
			
			List<GenericValue> listProyecto = delegator.findByCondition("Proyecto", condiciones, null, orderBy);

			List<Map<String,Object>> listProyectoBuilder = FastList.newInstance();
			
			Map<String,Object> mapaProyecto = FastMap.newInstance();
			
			for (GenericValue proyecto : listProyecto) {
				mapaProyecto.putAll(proyecto.getAllFields());				
				mapaProyecto.put("proyectoId", Long.valueOf(proyecto.getString("proyectoId")));
				Timestamp fecha = new Timestamp(proyecto.getDate("fecha").getTime());
				mapaProyecto.put("fecha", fecha);
				listProyectoBuilder.add(mapaProyecto);
				mapaProyecto = FastMap.newInstance();
			}
			
			Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
				public int compare(Map<String, Object> m1, Map<String, Object> m2) {
					return ((Long) m1.get("proyectoId")).compareTo((Long) m2.get("proyectoId"));
				}
			};

			Collections.sort(listProyectoBuilder, mapComparator);			
				ac.put("proyectoListBuilder", listProyectoBuilder);

		}
		
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}
	
public static void buscaPrevisionProyecto(Map<String, Object> context) {
		
		try {

		final ActionContext ac = new ActionContext(context);
		final Locale locale = ac.getLocale();
		TimeZone timeZone = ac.getTimeZone();
		String dateTimeFormat = UtilDateTime.getDateFormat(locale);
		
		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
		Delegator delegator = ac.getDelegator();

		// possible fields we're searching by
		String proyectoId = ac.getParameter("proyectoId");
		String codigoProyecto = ac.getParameter("codigoProyecto");
		String estatusId = ac.getParameter("statusId");
		String areaProyecto = ac.getParameter("partyId");
		Timestamp fechaContable_inicial = null;
		Timestamp fechaContable_final=null;
		
		
		if(UtilValidate.isNotEmpty(ac.getParameter("fechaContable"))) {
			
			String fechaContable = ac.getParameter("fechaContable");
			fechaContable_inicial = UtilDateTime.getDayStart(
						UtilDateTime.getTimestamp(
								UtilDateTime.getDateFromString(dateTimeFormat, timeZone, locale, fechaContable).getTime()));
			fechaContable_final = UtilDateTime.getDayEnd(fechaContable_inicial);
			
		}
	
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String usuario = null, area = null;
		
		usuario = userLogin.getString("partyId");
		Debug.logInfo("usuario: " + userLogin,MODULE);
		
		

		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
				.getLedgerRepository();

		// get the list of estatus for the parametrized form ftl
		List<EntityCondition> searchConditionsMod = new FastList<EntityCondition>();
		searchConditionsMod.add(EntityCondition.makeCondition(
				Estatus.Fields.tipo.name(), EntityOperator.EQUALS, "Y"));
		List<Estatus> estatus = ledgerRepository.findList(Estatus.class,
				searchConditionsMod);
		List<Map<String, Object>> estatusList = new FastList<Map<String, Object>>();
		for (Estatus s : estatus) {
			Map<String, Object> map = s.toMap();
			estatusList.add(map);
		}
		
		// get the list of estatus for the parametrized form ftl
				
			List<GenericValue> areas = UtilClassification.getListaCatalogos("NivelesParty", delegator, "",true);
		
		ac.put("estatusList", estatusList);
		ac.put("listAreas", areas);
		
		if ("Y".equals(ac.getParameter("performFind"))) {
			// build search conditions
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();
			
			if (UtilValidate.isNotEmpty(fechaContable_inicial)) {
				searchConditions.add(EntityCondition.makeCondition(
						"fecha", EntityOperator.GREATER_THAN_EQUAL_TO, fechaContable_inicial));
				searchConditions.add(EntityCondition.makeCondition(
						"fecha", EntityOperator.LESS_THAN_EQUAL_TO, fechaContable_final));
			}
			
			GenericValue partyRole = delegator.findByPrimaryKey("PartyRole", UtilMisc.toMap("partyId",usuario, "roleTypeId", "ADMPROY"));
			
			if(partyRole == null){
				
				List<GenericValue> responsable = delegator.findByCondition("Proyecto", EntityCondition.makeCondition("responsable", usuario), null, null);
				if (UtilValidate.isNotEmpty(responsable)){
					searchConditions.add(EntityCondition.makeCondition("responsable", EntityOperator.EQUALS,usuario));
				}else{
					searchConditions.add(EntityCondition.makeCondition("responsableTecnico", EntityOperator.EQUALS,usuario));
				}
				
		}

			if (UtilValidate.isNotEmpty(proyectoId)) {
				searchConditions.add(EntityCondition.makeCondition("proyectoId", proyectoId));
			}
			if (UtilValidate.isNotEmpty(codigoProyecto)) {
				searchConditions.add(EntityCondition.makeCondition("codigoProyecto", codigoProyecto));
			}
			searchConditions.add(EntityCondition.makeCondition("statusId", "VIGENTE"));
			if (UtilValidate.isNotEmpty(areaProyecto)) {
				searchConditions.add(EntityCondition.makeCondition("areaId", areaProyecto));
			}
			if (UtilValidate.isNotEmpty(area)) {
				searchConditions.add(EntityCondition.makeCondition("areaPartyId", area));
			}
			if (UtilValidate.isNotEmpty(organizationPartyId)) {
				searchConditions.add(EntityCondition.makeCondition("organizationPartyId", organizationPartyId));
			}
			
			// fields to select
			
			EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
			
			List<String> orderBy = UtilMisc.toList("proyectoId");
						
			Debug.logInfo(
					"search conditions : "
							+ EntityCondition.makeCondition(searchConditions,
									EntityOperator.AND).toString(), MODULE);
			
			List<GenericValue> listProyecto = delegator.findByCondition("Proyecto", condiciones, null, orderBy);

			List<Map<String,Object>> listPrevisionProyectoBuilder = FastList.newInstance();
			
			Map<String,Object> mapaProyecto = FastMap.newInstance();
			
			for (GenericValue proyecto : listProyecto) {
				mapaProyecto.putAll(proyecto.getAllFields());				
				mapaProyecto.put("proyectoId", Long.valueOf(proyecto.getString("proyectoId")));
				Timestamp fecha = new Timestamp(proyecto.getDate("fecha").getTime());
				mapaProyecto.put("fecha", fecha);
				listPrevisionProyectoBuilder.add(mapaProyecto);
				mapaProyecto = FastMap.newInstance();
			}
			
			Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
				public int compare(Map<String, Object> m1, Map<String, Object> m2) {
					return ((Long) m1.get("proyectoId")).compareTo((Long) m2.get("proyectoId"));
				}
			};

			Collections.sort(listPrevisionProyectoBuilder, mapComparator);			
				ac.put("previsionProyectoListBuilder", listPrevisionProyectoBuilder);

		}
		
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Metodo para consultar la requisicion
	 * @param context
	 */
	public static void buscaEtapaProyecto(Map<String, Object> context) {
		
		try {

		final ActionContext ac = new ActionContext(context);
		final Locale locale = ac.getLocale();
		TimeZone timeZone = ac.getTimeZone();
		String dateTimeFormat = UtilDateTime.getDateFormat(locale);
		
		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
		Delegator delegator = ac.getDelegator();

		// possible fields we're searching by
		String proyectoId = ac.getParameter("proyectoId");
		String codigoProyecto = ac.getParameter("codigoProyecto");
		String areaProyecto = ac.getParameter("partyId");
		Timestamp fechaContable_inicial = null;
		Timestamp fechaContable_final=null;
		
		
		if(UtilValidate.isNotEmpty(ac.getParameter("fechaContable"))) {
			
			String fechaContable = ac.getParameter("fechaContable");
			fechaContable_inicial = UtilDateTime.getDayStart(
						UtilDateTime.getTimestamp(
								UtilDateTime.getDateFromString(dateTimeFormat, timeZone, locale, fechaContable).getTime()));
			fechaContable_final = UtilDateTime.getDayEnd(fechaContable_inicial);
			
		}
	
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String usuario = null, area = null;
		
		usuario = userLogin.getString("partyId");
		Debug.logInfo("usuario: " + userLogin,MODULE);
		
		

		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
				.getLedgerRepository();

		// get the list of estatus for the parametrized form ftl
		List<EntityCondition> searchConditionsMod = new FastList<EntityCondition>();
		searchConditionsMod.add(EntityCondition.makeCondition(
				Estatus.Fields.tipo.name(), EntityOperator.EQUALS, "Y"));
		List<Estatus> estatus = ledgerRepository.findList(Estatus.class,
				searchConditionsMod);
		List<Map<String, Object>> estatusList = new FastList<Map<String, Object>>();
		for (Estatus s : estatus) {
			Map<String, Object> map = s.toMap();
			estatusList.add(map);
		}
		
		// get the list of estatus for the parametrized form ftl
				
			List<GenericValue> areas = UtilClassification.getListaCatalogos("NivelesParty", delegator, "",true);
		
		ac.put("estatusList", estatusList);
		ac.put("listAreas", areas);
		
		if ("Y".equals(ac.getParameter("performFind"))) {
			// build search conditions
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();
			
			if (UtilValidate.isNotEmpty(fechaContable_inicial)) {
				searchConditions.add(EntityCondition.makeCondition(
						"fecha", EntityOperator.GREATER_THAN_EQUAL_TO, fechaContable_inicial));
				searchConditions.add(EntityCondition.makeCondition(
						"fecha", EntityOperator.LESS_THAN_EQUAL_TO, fechaContable_final));
			}
			
			GenericValue partyRole = delegator.findByPrimaryKey("PartyRole", UtilMisc.toMap("partyId",usuario, "roleTypeId", "ADMPROY"));
			
			if(partyRole == null){
				
				List<GenericValue> responsable = delegator.findByCondition("Proyecto", EntityCondition.makeCondition("responsable", usuario), null, null);
				if (UtilValidate.isNotEmpty(responsable)){
					searchConditions.add(EntityCondition.makeCondition("responsable", EntityOperator.EQUALS,usuario));
				}else{
					searchConditions.add(EntityCondition.makeCondition("responsableTecnico", EntityOperator.EQUALS,usuario));
				}
				
		}

			if (UtilValidate.isNotEmpty(proyectoId)) {
				searchConditions.add(EntityCondition.makeCondition("proyectoId", proyectoId));
			}
			if (UtilValidate.isNotEmpty(codigoProyecto)) {
				searchConditions.add(EntityCondition.makeCondition("codigoProyecto", codigoProyecto));
			}
			searchConditions.add(EntityCondition.makeCondition("statusId", "VIGENTE"));
			if (UtilValidate.isNotEmpty(areaProyecto)) {
				searchConditions.add(EntityCondition.makeCondition("areaId", areaProyecto));
			}
			if (UtilValidate.isNotEmpty(area)) {
				searchConditions.add(EntityCondition.makeCondition("areaPartyId", area));
			}
			if (UtilValidate.isNotEmpty(organizationPartyId)) {
				searchConditions.add(EntityCondition.makeCondition("organizationPartyId", organizationPartyId));
			}

			
			// fields to select
			
			EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
			
			List<String> orderBy = UtilMisc.toList("proyectoId");
						
			Debug.logInfo(
					"search conditions : "
							+ EntityCondition.makeCondition(searchConditions,
									EntityOperator.AND).toString(), MODULE);
			
			List<GenericValue> listProyecto = delegator.findByCondition("Proyecto", condiciones, null, orderBy);

			List<Map<String,Object>> proyectoEtapaListBuilder = FastList.newInstance();
			
			Map<String,Object> mapaProyecto = FastMap.newInstance();
			
			for (GenericValue proyecto : listProyecto) {
				mapaProyecto.putAll(proyecto.getAllFields());				
				mapaProyecto.put("proyectoId", Long.valueOf(proyecto.getString("proyectoId")));
				Timestamp fecha = new Timestamp(proyecto.getDate("fecha").getTime());
				mapaProyecto.put("fecha", fecha);
				proyectoEtapaListBuilder.add(mapaProyecto);
				mapaProyecto = FastMap.newInstance();
			}
			
			Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
				public int compare(Map<String, Object> m1, Map<String, Object> m2) {
					return ((Long) m1.get("proyectoId")).compareTo((Long) m2.get("proyectoId"));
				}
			};

			Collections.sort(proyectoEtapaListBuilder, mapComparator);			
				ac.put("proyectoEtapaListBuilder", proyectoEtapaListBuilder);

		}
		
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Metodo para consultar el proyecto y modificar la etapa
	 * @param context
	 */
	public static void buscaEtapaModificaProyecto(Map<String, Object> context) {
		
		try {

		final ActionContext ac = new ActionContext(context);
		final Locale locale = ac.getLocale();
		TimeZone timeZone = ac.getTimeZone();
		String dateTimeFormat = UtilDateTime.getDateFormat(locale);
		
		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
		Delegator delegator = ac.getDelegator();

		// possible fields we're searching by
		String proyectoId = ac.getParameter("proyectoId");
		String codigoProyecto = ac.getParameter("codigoProyecto");
		String areaProyecto = ac.getParameter("partyId");
		Timestamp fechaContable_inicial = null;
		Timestamp fechaContable_final=null;
		
		
		if(UtilValidate.isNotEmpty(ac.getParameter("fechaContable"))) {
			
			String fechaContable = ac.getParameter("fechaContable");
			fechaContable_inicial = UtilDateTime.getDayStart(
						UtilDateTime.getTimestamp(
								UtilDateTime.getDateFromString(dateTimeFormat, timeZone, locale, fechaContable).getTime()));
			fechaContable_final = UtilDateTime.getDayEnd(fechaContable_inicial);
			
		}
	
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String usuario = null, area = null;
		
		usuario = userLogin.getString("partyId");
		Debug.logInfo("usuario: " + userLogin,MODULE);
		
		

		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
				.getLedgerRepository();

		// get the list of estatus for the parametrized form ftl
		List<EntityCondition> searchConditionsMod = new FastList<EntityCondition>();
		searchConditionsMod.add(EntityCondition.makeCondition(
				Estatus.Fields.tipo.name(), EntityOperator.EQUALS, "Y"));
		List<Estatus> estatus = ledgerRepository.findList(Estatus.class,
				searchConditionsMod);
		List<Map<String, Object>> estatusList = new FastList<Map<String, Object>>();
		for (Estatus s : estatus) {
			Map<String, Object> map = s.toMap();
			estatusList.add(map);
		}
		
		// get the list of estatus for the parametrized form ftl
				
			List<GenericValue> areas = UtilClassification.getListaCatalogos("NivelesParty", delegator, "",true);
		
		ac.put("estatusList", estatusList);
		ac.put("listAreas", areas);
		
		if ("Y".equals(ac.getParameter("performFind"))) {
			// build search conditions
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();
			
			if (UtilValidate.isNotEmpty(fechaContable_inicial)) {
				searchConditions.add(EntityCondition.makeCondition(
						"fecha", EntityOperator.GREATER_THAN_EQUAL_TO, fechaContable_inicial));
				searchConditions.add(EntityCondition.makeCondition(
						"fecha", EntityOperator.LESS_THAN_EQUAL_TO, fechaContable_final));
			}
			
			GenericValue partyRole = delegator.findByPrimaryKey("PartyRole", UtilMisc.toMap("partyId",usuario, "roleTypeId", "ADMPROY"));
			
			if(partyRole == null){
				
				List<GenericValue> responsable = delegator.findByCondition("Proyecto", EntityCondition.makeCondition("responsable", usuario), null, null);
				if (UtilValidate.isNotEmpty(responsable)){
					searchConditions.add(EntityCondition.makeCondition("responsable", EntityOperator.EQUALS,usuario));
				}else{
					searchConditions.add(EntityCondition.makeCondition("responsableTecnico", EntityOperator.EQUALS,usuario));
				}
				
		}

			if (UtilValidate.isNotEmpty(proyectoId)) {
				searchConditions.add(EntityCondition.makeCondition("proyectoId", proyectoId));
			}
			if (UtilValidate.isNotEmpty(codigoProyecto)) {
				searchConditions.add(EntityCondition.makeCondition("codigoProyecto", codigoProyecto));
			}
			searchConditions.add(EntityCondition.makeCondition("statusId", "VIGENTE"));
			if (UtilValidate.isNotEmpty(areaProyecto)) {
				searchConditions.add(EntityCondition.makeCondition("areaId", areaProyecto));
			}
			if (UtilValidate.isNotEmpty(area)) {
				searchConditions.add(EntityCondition.makeCondition("areaPartyId", area));
			}
			if (UtilValidate.isNotEmpty(organizationPartyId)) {
				searchConditions.add(EntityCondition.makeCondition("organizationPartyId", organizationPartyId));
			}

			
			// fields to select
			
			EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
			
			List<String> orderBy = UtilMisc.toList("proyectoId");
						
			Debug.logInfo(
					"search conditions : "
							+ EntityCondition.makeCondition(searchConditions,
									EntityOperator.AND).toString(), MODULE);
			
			List<GenericValue> listProyecto = delegator.findByCondition("Proyecto", condiciones, null, orderBy);

			List<Map<String,Object>> proyectoEtapaModificaListBuilder = FastList.newInstance();
			
			Map<String,Object> mapaProyecto = FastMap.newInstance();
			
			for (GenericValue proyecto : listProyecto) {
				mapaProyecto.putAll(proyecto.getAllFields());				
				mapaProyecto.put("proyectoId", Long.valueOf(proyecto.getString("proyectoId")));
				Timestamp fecha = new Timestamp(proyecto.getDate("fecha").getTime());
				mapaProyecto.put("fecha", fecha);
				proyectoEtapaModificaListBuilder.add(mapaProyecto);
				mapaProyecto = FastMap.newInstance();
			}
			
			Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
				public int compare(Map<String, Object> m1, Map<String, Object> m2) {
					return ((Long) m1.get("proyectoId")).compareTo((Long) m2.get("proyectoId"));
				}
			};

			Collections.sort(proyectoEtapaModificaListBuilder, mapComparator);			
				ac.put("proyectoEtapaModificaListBuilder", proyectoEtapaModificaListBuilder);

		}
		
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}
	
public static void buscaUsuarioProyecto(Map<String, Object> context) {
		
		try {

		final ActionContext ac = new ActionContext(context);
		final Locale locale = ac.getLocale();
		TimeZone timeZone = ac.getTimeZone();
		String dateTimeFormat = UtilDateTime.getDateFormat(locale);
		
		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
		Delegator delegator = ac.getDelegator();

		// possible fields we're searching by
		String proyectoId = ac.getParameter("proyectoId");
		String codigoProyecto = ac.getParameter("codigoProyecto");
		String estatusId = ac.getParameter("statusId");
		String areaProyecto = ac.getParameter("partyId");
		Timestamp fechaContable_inicial = null;
		Timestamp fechaContable_final=null;
		
		
		if(UtilValidate.isNotEmpty(ac.getParameter("fechaContable"))) {
			
			String fechaContable = ac.getParameter("fechaContable");
			fechaContable_inicial = UtilDateTime.getDayStart(
						UtilDateTime.getTimestamp(
								UtilDateTime.getDateFromString(dateTimeFormat, timeZone, locale, fechaContable).getTime()));
			fechaContable_final = UtilDateTime.getDayEnd(fechaContable_inicial);
			
		}
	
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String usuario = null, area = null;
		
		usuario = userLogin.getString("partyId");
		Debug.logInfo("usuario: " + userLogin,MODULE);
		
		

		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
				.getLedgerRepository();

		// get the list of estatus for the parametrized form ftl
		List<EntityCondition> searchConditionsMod = new FastList<EntityCondition>();
		searchConditionsMod.add(EntityCondition.makeCondition(
				Estatus.Fields.tipo.name(), EntityOperator.EQUALS, "Y"));
		List<Estatus> estatus = ledgerRepository.findList(Estatus.class,
				searchConditionsMod);
		List<Map<String, Object>> estatusList = new FastList<Map<String, Object>>();
		for (Estatus s : estatus) {
			Map<String, Object> map = s.toMap();
			estatusList.add(map);
		}
		
		// get the list of estatus for the parametrized form ftl
				
			List<GenericValue> areas = UtilClassification.getListaCatalogos("NivelesParty", delegator, "",true);
		
		ac.put("estatusList", estatusList);
		ac.put("listAreas", areas);
		
		if ("Y".equals(ac.getParameter("performFind"))) {
			// build search conditions
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();
			
			if (UtilValidate.isNotEmpty(fechaContable_inicial)) {
				searchConditions.add(EntityCondition.makeCondition(
						"fecha", EntityOperator.GREATER_THAN_EQUAL_TO, fechaContable_inicial));
				searchConditions.add(EntityCondition.makeCondition(
						"fecha", EntityOperator.LESS_THAN_EQUAL_TO, fechaContable_final));
			}
			
			GenericValue partyRole = delegator.findByPrimaryKey("PartyRole", UtilMisc.toMap("partyId",usuario, "roleTypeId", "ADMPROY"));
			
			if(partyRole == null){
				
				List<GenericValue> responsable = delegator.findByCondition("Proyecto", EntityCondition.makeCondition("responsable", usuario), null, null);
				if (UtilValidate.isNotEmpty(responsable)){
					searchConditions.add(EntityCondition.makeCondition("responsable", EntityOperator.EQUALS,usuario));
				}else{
					searchConditions.add(EntityCondition.makeCondition("responsableTecnico", EntityOperator.EQUALS,usuario));
				}
				
		}

			if (UtilValidate.isNotEmpty(proyectoId)) {
				searchConditions.add(EntityCondition.makeCondition("proyectoId", proyectoId));
			}
			if (UtilValidate.isNotEmpty(codigoProyecto)) {
				searchConditions.add(EntityCondition.makeCondition("codigoProyecto", codigoProyecto));
			}
			if (UtilValidate.isNotEmpty(areaProyecto)) {
				searchConditions.add(EntityCondition.makeCondition("areaId", areaProyecto));
			}
			if (UtilValidate.isNotEmpty(area)) {
				searchConditions.add(EntityCondition.makeCondition("areaPartyId", area));
			}
			if (UtilValidate.isNotEmpty(organizationPartyId)) {
				searchConditions.add(EntityCondition.makeCondition("organizationPartyId", organizationPartyId));
			}
			
			searchConditions.add(EntityCondition.makeCondition("statusId", "VIGENTE"));

			
			// fields to select
			
			EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
			
			List<String> orderBy = UtilMisc.toList("proyectoId");
						
			Debug.logInfo(
					"search conditions : "
							+ EntityCondition.makeCondition(searchConditions,
									EntityOperator.AND).toString(), MODULE);
			
			List<GenericValue> listProyecto = delegator.findByCondition("Proyecto", condiciones, null, orderBy);

			List<Map<String,Object>> listProyectoBuilder = FastList.newInstance();
			
			Map<String,Object> mapaProyecto = FastMap.newInstance();
			
			for (GenericValue proyecto : listProyecto) {
				mapaProyecto.putAll(proyecto.getAllFields());				
				mapaProyecto.put("proyectoId", Long.valueOf(proyecto.getString("proyectoId")));
				Timestamp fecha = new Timestamp(proyecto.getDate("fecha").getTime());
				mapaProyecto.put("fecha", fecha);
				listProyectoBuilder.add(mapaProyecto);
				mapaProyecto = FastMap.newInstance();
			}
			
			Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
				public int compare(Map<String, Object> m1, Map<String, Object> m2) {
					return ((Long) m1.get("proyectoId")).compareTo((Long) m2.get("proyectoId"));
				}
			};

			Collections.sort(listProyectoBuilder, mapComparator);			
				ac.put("proyectoUsuarioListBuilder", listProyectoBuilder);

		}
		
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}

	public static void buscaConcluyeProyecto(Map<String, Object> context) {
		
		try {
	
		final ActionContext ac = new ActionContext(context);
		final Locale locale = ac.getLocale();
		TimeZone timeZone = ac.getTimeZone();
		String dateTimeFormat = UtilDateTime.getDateFormat(locale);
		
		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
		Delegator delegator = ac.getDelegator();
	
		// possible fields we're searching by
		String proyectoId = ac.getParameter("proyectoId");
		String codigoProyecto = ac.getParameter("codigoProyecto");
		String estatusId = ac.getParameter("statusId");
		String areaProyecto = ac.getParameter("partyId");
		Timestamp fechaContable_inicial = null;
		Timestamp fechaContable_final=null;
		
		
		if(UtilValidate.isNotEmpty(ac.getParameter("fechaContable"))) {
			
			String fechaContable = ac.getParameter("fechaContable");
			fechaContable_inicial = UtilDateTime.getDayStart(
						UtilDateTime.getTimestamp(
								UtilDateTime.getDateFromString(dateTimeFormat, timeZone, locale, fechaContable).getTime()));
			fechaContable_final = UtilDateTime.getDayEnd(fechaContable_inicial);
			
		}
	
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String usuario = null, area = null;
		
		usuario = userLogin.getString("partyId");
		Debug.logInfo("usuario: " + userLogin,MODULE);
		
		
	
		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
				.getLedgerRepository();
	
		// get the list of estatus for the parametrized form ftl
		List<EntityCondition> searchConditionsMod = new FastList<EntityCondition>();
		searchConditionsMod.add(EntityCondition.makeCondition(
				Estatus.Fields.tipo.name(), EntityOperator.EQUALS, "Y"));
		List<Estatus> estatus = ledgerRepository.findList(Estatus.class,
				searchConditionsMod);
		List<Map<String, Object>> estatusList = new FastList<Map<String, Object>>();
		for (Estatus s : estatus) {
			Map<String, Object> map = s.toMap();
			estatusList.add(map);
		}
		
		// get the list of estatus for the parametrized form ftl
				
			List<GenericValue> areas = UtilClassification.getListaCatalogos("NivelesParty", delegator, "",true);
		
		ac.put("estatusList", estatusList);
		ac.put("listAreas", areas);
		
		if ("Y".equals(ac.getParameter("performFind"))) {
			// build search conditions
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();
			
			if (UtilValidate.isNotEmpty(fechaContable_inicial)) {
				searchConditions.add(EntityCondition.makeCondition(
						"fecha", EntityOperator.GREATER_THAN_EQUAL_TO, fechaContable_inicial));
				searchConditions.add(EntityCondition.makeCondition(
						"fecha", EntityOperator.LESS_THAN_EQUAL_TO, fechaContable_final));
			}
			
			GenericValue partyRole = delegator.findByPrimaryKey("PartyRole", UtilMisc.toMap("partyId",usuario, "roleTypeId", "ADMPROY"));
			
			if(partyRole == null){
				
				List<GenericValue> responsable = delegator.findByCondition("Proyecto", EntityCondition.makeCondition("responsable", usuario), null, null);
				if (UtilValidate.isNotEmpty(responsable)){
					searchConditions.add(EntityCondition.makeCondition("responsable", EntityOperator.EQUALS,usuario));
				}else{
					searchConditions.add(EntityCondition.makeCondition("responsableTecnico", EntityOperator.EQUALS,usuario));
				}
				
		}
	
			if (UtilValidate.isNotEmpty(proyectoId)) {
				searchConditions.add(EntityCondition.makeCondition("proyectoId", proyectoId));
			}
			if (UtilValidate.isNotEmpty(codigoProyecto)) {
				searchConditions.add(EntityCondition.makeCondition("codigoProyecto", codigoProyecto));
			}
			if (UtilValidate.isNotEmpty(areaProyecto)) {
				searchConditions.add(EntityCondition.makeCondition("areaId", areaProyecto));
			}
			if (UtilValidate.isNotEmpty(area)) {
				searchConditions.add(EntityCondition.makeCondition("areaPartyId", area));
			}
			if (UtilValidate.isNotEmpty(organizationPartyId)) {
				searchConditions.add(EntityCondition.makeCondition("organizationPartyId", organizationPartyId));
			}
			searchConditions.add(EntityCondition.makeCondition("statusId", "VIGENTE"));
	
			
			// fields to select
			
			EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
			
			List<String> orderBy = UtilMisc.toList("proyectoId");
						
			Debug.logInfo(
					"search conditions : "
							+ EntityCondition.makeCondition(searchConditions,
									EntityOperator.AND).toString(), MODULE);
			
			List<GenericValue> listProyecto = delegator.findByCondition("Proyecto", condiciones, null, orderBy);
	
			List<Map<String,Object>> listProyectoBuilder = FastList.newInstance();
			
			Map<String,Object> mapaProyecto = FastMap.newInstance();
			
			for (GenericValue proyecto : listProyecto) {
				mapaProyecto.putAll(proyecto.getAllFields());				
				mapaProyecto.put("proyectoId", Long.valueOf(proyecto.getString("proyectoId")));
				Timestamp fecha = new Timestamp(proyecto.getDate("fecha").getTime());
				mapaProyecto.put("fecha", fecha);
				listProyectoBuilder.add(mapaProyecto);
				mapaProyecto = FastMap.newInstance();
			}
			
			Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
				public int compare(Map<String, Object> m1, Map<String, Object> m2) {
					return ((Long) m1.get("proyectoId")).compareTo((Long) m2.get("proyectoId"));
				}
			};
	
			Collections.sort(listProyectoBuilder, mapComparator);			
				ac.put("proyectoConcluyeListBuilder", listProyectoBuilder);
	
		}
		
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}

	public static void buscaDevuelveProyecto(Map<String, Object> context) {
		
		try {

		final ActionContext ac = new ActionContext(context);
		final Locale locale = ac.getLocale();
		TimeZone timeZone = ac.getTimeZone();
		String dateTimeFormat = UtilDateTime.getDateFormat(locale);
		
		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
		Delegator delegator = ac.getDelegator();

		// possible fields we're searching by
		String proyectoId = ac.getParameter("proyectoId");
		String codigoProyecto = ac.getParameter("codigoProyecto");
		String estatusId = ac.getParameter("statusId");
		String areaProyecto = ac.getParameter("partyId");
		Timestamp fechaContable_inicial = null;
		Timestamp fechaContable_final=null;
		
		
		if(UtilValidate.isNotEmpty(ac.getParameter("fechaContable"))) {
			
			String fechaContable = ac.getParameter("fechaContable");
			fechaContable_inicial = UtilDateTime.getDayStart(
						UtilDateTime.getTimestamp(
								UtilDateTime.getDateFromString(dateTimeFormat, timeZone, locale, fechaContable).getTime()));
			fechaContable_final = UtilDateTime.getDayEnd(fechaContable_inicial);
			
		}

		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String usuario = null, area = null;
		
		usuario = userLogin.getString("partyId");
		Debug.logInfo("usuario: " + userLogin,MODULE);
		
		

		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
				.getLedgerRepository();

		// get the list of estatus for the parametrized form ftl
		List<EntityCondition> searchConditionsMod = new FastList<EntityCondition>();
		searchConditionsMod.add(EntityCondition.makeCondition(
				Estatus.Fields.tipo.name(), EntityOperator.EQUALS, "Y"));
		List<Estatus> estatus = ledgerRepository.findList(Estatus.class,
				searchConditionsMod);
		List<Map<String, Object>> estatusList = new FastList<Map<String, Object>>();
		for (Estatus s : estatus) {
			Map<String, Object> map = s.toMap();
			estatusList.add(map);
		}
		
		// get the list of estatus for the parametrized form ftl
				
			List<GenericValue> areas = UtilClassification.getListaCatalogos("NivelesParty", delegator, "",true);
		
		ac.put("estatusList", estatusList);
		ac.put("listAreas", areas);
		
		if ("Y".equals(ac.getParameter("performFind"))) {
			// build search conditions
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();
			
			if (UtilValidate.isNotEmpty(fechaContable_inicial)) {
				searchConditions.add(EntityCondition.makeCondition(
						"fecha", EntityOperator.GREATER_THAN_EQUAL_TO, fechaContable_inicial));
				searchConditions.add(EntityCondition.makeCondition(
						"fecha", EntityOperator.LESS_THAN_EQUAL_TO, fechaContable_final));
			}
			
			GenericValue partyRole = delegator.findByPrimaryKey("PartyRole", UtilMisc.toMap("partyId",usuario, "roleTypeId", "ADMPROY"));
			
			if(partyRole == null){
				
				List<GenericValue> responsable = delegator.findByCondition("Proyecto", EntityCondition.makeCondition("responsable", usuario), null, null);
				if (UtilValidate.isNotEmpty(responsable)){
					searchConditions.add(EntityCondition.makeCondition("responsable", EntityOperator.EQUALS,usuario));
				}else{
					searchConditions.add(EntityCondition.makeCondition("responsableTecnico", EntityOperator.EQUALS,usuario));
				}
				
		}

			if (UtilValidate.isNotEmpty(proyectoId)) {
				searchConditions.add(EntityCondition.makeCondition("proyectoId", proyectoId));
			}
			
			if (UtilValidate.isNotEmpty(codigoProyecto)) {
				searchConditions.add(EntityCondition.makeCondition("codigoProyecto", codigoProyecto));
			}
			
			if (UtilValidate.isNotEmpty(areaProyecto)) {
				searchConditions.add(EntityCondition.makeCondition("areaId", areaProyecto));
			}
			if (UtilValidate.isNotEmpty(area)) {
				searchConditions.add(EntityCondition.makeCondition("areaPartyId", area));
			}
			if (UtilValidate.isNotEmpty(organizationPartyId)) {
				searchConditions.add(EntityCondition.makeCondition("organizationPartyId", organizationPartyId));
			}
			
			searchConditions.add(EntityCondition.makeCondition("statusId", "DEVOLUCION"));
			
			// fields to select
			
			EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
			
			List<String> orderBy = UtilMisc.toList("proyectoId");
						
			Debug.logInfo(
					"search conditions : "
							+ EntityCondition.makeCondition(searchConditions,
									EntityOperator.AND).toString(), MODULE);
			
			List<GenericValue> listProyecto = delegator.findByCondition("Proyecto", condiciones, null, orderBy);

			List<Map<String,Object>> listProyectoBuilder = FastList.newInstance();
			
			Map<String,Object> mapaProyecto = FastMap.newInstance();
			
			for (GenericValue proyecto : listProyecto) {
				mapaProyecto.putAll(proyecto.getAllFields());				
				mapaProyecto.put("proyectoId", Long.valueOf(proyecto.getString("proyectoId")));
				Timestamp fecha = new Timestamp(proyecto.getDate("fecha").getTime());
				mapaProyecto.put("fecha", fecha);
				listProyectoBuilder.add(mapaProyecto);
				mapaProyecto = FastMap.newInstance();
			}
			
			Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
				public int compare(Map<String, Object> m1, Map<String, Object> m2) {
					return ((Long) m1.get("proyectoId")).compareTo((Long) m2.get("proyectoId"));
				}
			};

			Collections.sort(listProyectoBuilder, mapComparator);			
				ac.put("proyectoDevuelveListBuilder", listProyectoBuilder);

		}
		
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}
	
public static void buscaDescargaProyecto(Map<String, Object> context) {
		
		try {

		final ActionContext ac = new ActionContext(context);
		final Locale locale = ac.getLocale();
		TimeZone timeZone = ac.getTimeZone();
		String dateTimeFormat = UtilDateTime.getDateFormat(locale);
		
		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
		Delegator delegator = ac.getDelegator();

		// possible fields we're searching by
		String proyectoId = ac.getParameter("proyectoId");
		String codigoProyecto = ac.getParameter("codigoProyecto");
		String estatusId = ac.getParameter("statusId");
		String areaProyecto = ac.getParameter("partyId");
		Timestamp fechaContable_inicial = null;
		Timestamp fechaContable_final=null;
		
		
		if(UtilValidate.isNotEmpty(ac.getParameter("fechaContable"))) {
			
			String fechaContable = ac.getParameter("fechaContable");
			fechaContable_inicial = UtilDateTime.getDayStart(
						UtilDateTime.getTimestamp(
								UtilDateTime.getDateFromString(dateTimeFormat, timeZone, locale, fechaContable).getTime()));
			fechaContable_final = UtilDateTime.getDayEnd(fechaContable_inicial);
			
		}
	
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String usuario = null, area = null;
		
		usuario = userLogin.getString("partyId");
		Debug.logInfo("usuario: " + userLogin,MODULE);
		
		

		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
				.getLedgerRepository();

		// get the list of estatus for the parametrized form ftl
		List<EntityCondition> searchConditionsMod = new FastList<EntityCondition>();
		searchConditionsMod.add(EntityCondition.makeCondition(
				Estatus.Fields.tipo.name(), EntityOperator.EQUALS, "Y"));
		List<Estatus> estatus = ledgerRepository.findList(Estatus.class,
				searchConditionsMod);
		List<Map<String, Object>> estatusList = new FastList<Map<String, Object>>();
		for (Estatus s : estatus) {
			Map<String, Object> map = s.toMap();
			estatusList.add(map);
		}
		
		// get the list of estatus for the parametrized form ftl
				
			List<GenericValue> areas = UtilClassification.getListaCatalogos("NivelesParty", delegator, "",true);
		
		ac.put("estatusList", estatusList);
		ac.put("listAreas", areas);
		
		if ("Y".equals(ac.getParameter("performFind"))) {
			// build search conditions
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();
			
			if (UtilValidate.isNotEmpty(fechaContable_inicial)) {
				searchConditions.add(EntityCondition.makeCondition(
						"fecha", EntityOperator.GREATER_THAN_EQUAL_TO, fechaContable_inicial));
				searchConditions.add(EntityCondition.makeCondition(
						"fecha", EntityOperator.LESS_THAN_EQUAL_TO, fechaContable_final));
			}
			
			GenericValue partyRole = delegator.findByPrimaryKey("PartyRole", UtilMisc.toMap("partyId",usuario, "roleTypeId", "ADMPROY"));
			
			if(partyRole == null){
				
				List<GenericValue> responsable = delegator.findByCondition("Proyecto", EntityCondition.makeCondition("responsable", usuario), null, null);
				if (UtilValidate.isNotEmpty(responsable)){
					searchConditions.add(EntityCondition.makeCondition("responsable", EntityOperator.EQUALS,usuario));
				}else{
					searchConditions.add(EntityCondition.makeCondition("responsableTecnico", EntityOperator.EQUALS,usuario));
				}
				
		}

			if (UtilValidate.isNotEmpty(proyectoId)) {
				searchConditions.add(EntityCondition.makeCondition("proyectoId", proyectoId));
			}
			if (UtilValidate.isNotEmpty(codigoProyecto)) {
				searchConditions.add(EntityCondition.makeCondition("codigoProyecto", codigoProyecto));
			}
			if (UtilValidate.isNotEmpty(estatusId)) {
				searchConditions.add(EntityCondition.makeCondition("statusId", estatusId));
			}
			if (UtilValidate.isNotEmpty(areaProyecto)) {
				searchConditions.add(EntityCondition.makeCondition("areaId", areaProyecto));
			}
			if (UtilValidate.isNotEmpty(area)) {
				searchConditions.add(EntityCondition.makeCondition("areaPartyId", area));
			}
			if (UtilValidate.isNotEmpty(organizationPartyId)) {
				searchConditions.add(EntityCondition.makeCondition("organizationPartyId", organizationPartyId));
			}

			
			// fields to select
			
			EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
			
			List<String> orderBy = UtilMisc.toList("proyectoId");
						
			Debug.logInfo(
					"search conditions : "
							+ EntityCondition.makeCondition(searchConditions,
									EntityOperator.AND).toString(), MODULE);
			
			List<GenericValue> listProyecto = delegator.findByCondition("Proyecto", condiciones, null, orderBy);

			List<Map<String,Object>> listProyectoDescargaBuilder = FastList.newInstance();
			
			Map<String,Object> mapaProyecto = FastMap.newInstance();
			
			for (GenericValue proyecto : listProyecto) {
				mapaProyecto.putAll(proyecto.getAllFields());				
				mapaProyecto.put("proyectoId", Long.valueOf(proyecto.getString("proyectoId")));
				Timestamp fecha = new Timestamp(proyecto.getDate("fecha").getTime());
				mapaProyecto.put("fecha", fecha);
				listProyectoDescargaBuilder.add(mapaProyecto);
				mapaProyecto = FastMap.newInstance();
			}
			
			Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
				public int compare(Map<String, Object> m1, Map<String, Object> m2) {
					return ((Long) m1.get("proyectoId")).compareTo((Long) m2.get("proyectoId"));
				}
			};

			Collections.sort(listProyectoDescargaBuilder, mapComparator);			
				ac.put("proyectoDescargaListBuilder", listProyectoDescargaBuilder);

		}
		
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Metodo para consultar el proyecto y registrar los intereses y las comisiones
	 * @param context
	 */
	public static void buscaProyectoIntCom(Map<String, Object> context) {
		
		try {
	
		final ActionContext ac = new ActionContext(context);
		final Locale locale = ac.getLocale();
		TimeZone timeZone = ac.getTimeZone();
		String dateTimeFormat = UtilDateTime.getDateFormat(locale);
		
		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
		Delegator delegator = ac.getDelegator();
	
		// possible fields we're searching by
		String proyectoId = ac.getParameter("proyectoId");
		String codigoProyecto = ac.getParameter("codigoProyecto");
		String areaProyecto = ac.getParameter("partyId");
		Timestamp fechaContable_inicial = null;
		Timestamp fechaContable_final=null;
		
		
		if(UtilValidate.isNotEmpty(ac.getParameter("fechaContable"))) {
			
			String fechaContable = ac.getParameter("fechaContable");
			fechaContable_inicial = UtilDateTime.getDayStart(
						UtilDateTime.getTimestamp(
								UtilDateTime.getDateFromString(dateTimeFormat, timeZone, locale, fechaContable).getTime()));
			fechaContable_final = UtilDateTime.getDayEnd(fechaContable_inicial);
			
		}
	
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String usuario = null, area = null;
		
		usuario = userLogin.getString("partyId");
		Debug.logInfo("usuario: " + userLogin,MODULE);
		
		
	
		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
				.getLedgerRepository();
	
		// get the list of estatus for the parametrized form ftl
		List<EntityCondition> searchConditionsMod = new FastList<EntityCondition>();
		searchConditionsMod.add(EntityCondition.makeCondition(
				Estatus.Fields.tipo.name(), EntityOperator.EQUALS, "Y"));
		List<Estatus> estatus = ledgerRepository.findList(Estatus.class,
				searchConditionsMod);
		List<Map<String, Object>> estatusList = new FastList<Map<String, Object>>();
		for (Estatus s : estatus) {
			Map<String, Object> map = s.toMap();
			estatusList.add(map);
		}
		
		// get the list of estatus for the parametrized form ftl
				
			List<GenericValue> areas = UtilClassification.getListaCatalogos("NivelesParty", delegator, "",true);
		
		ac.put("estatusList", estatusList);
		ac.put("listAreas", areas);
		
		if ("Y".equals(ac.getParameter("performFind"))) {
			// build search conditions
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();
			
			if (UtilValidate.isNotEmpty(fechaContable_inicial)) {
				searchConditions.add(EntityCondition.makeCondition(
						"fecha", EntityOperator.GREATER_THAN_EQUAL_TO, fechaContable_inicial));
				searchConditions.add(EntityCondition.makeCondition(
						"fecha", EntityOperator.LESS_THAN_EQUAL_TO, fechaContable_final));
			}
			
			GenericValue partyRole = delegator.findByPrimaryKey("PartyRole", UtilMisc.toMap("partyId",usuario, "roleTypeId", "ADMPROY"));
			
			if(partyRole == null){
				
				List<GenericValue> responsable = delegator.findByCondition("Proyecto", EntityCondition.makeCondition("responsable", usuario), null, null);
				if (UtilValidate.isNotEmpty(responsable)){
					searchConditions.add(EntityCondition.makeCondition("responsable", EntityOperator.EQUALS,usuario));
				}else{
					searchConditions.add(EntityCondition.makeCondition("responsableTecnico", EntityOperator.EQUALS,usuario));
				}
				
		}
	
			if (UtilValidate.isNotEmpty(proyectoId)) {
				searchConditions.add(EntityCondition.makeCondition("proyectoId", proyectoId));
			}
			if (UtilValidate.isNotEmpty(codigoProyecto)) {
				searchConditions.add(EntityCondition.makeCondition("codigoProyecto", codigoProyecto));
			}
			searchConditions.add(EntityCondition.makeCondition("statusId", "VIGENTE"));
			if (UtilValidate.isNotEmpty(areaProyecto)) {
				searchConditions.add(EntityCondition.makeCondition("areaId", areaProyecto));
			}
			if (UtilValidate.isNotEmpty(area)) {
				searchConditions.add(EntityCondition.makeCondition("areaPartyId", area));
			}
			if (UtilValidate.isNotEmpty(organizationPartyId)) {
				searchConditions.add(EntityCondition.makeCondition("organizationPartyId", organizationPartyId));
			}
	
			
			// fields to select
			
			EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
			
			List<String> orderBy = UtilMisc.toList("proyectoId");
						
			Debug.logInfo(
					"search conditions : "
							+ EntityCondition.makeCondition(searchConditions,
									EntityOperator.AND).toString(), MODULE);
			
			List<GenericValue> listProyecto = delegator.findByCondition("Proyecto", condiciones, null, orderBy);
	
			List<Map<String,Object>> proyectoInteresComisionListBuilder = FastList.newInstance();
			
			Map<String,Object> mapaProyecto = FastMap.newInstance();
			
			for (GenericValue proyecto : listProyecto) {
				mapaProyecto.putAll(proyecto.getAllFields());				
				mapaProyecto.put("proyectoId", Long.valueOf(proyecto.getString("proyectoId")));
				Timestamp fecha = new Timestamp(proyecto.getDate("fecha").getTime());
				mapaProyecto.put("fecha", fecha);
				proyectoInteresComisionListBuilder.add(mapaProyecto);
				mapaProyecto = FastMap.newInstance();
			}
			
			Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
				public int compare(Map<String, Object> m1, Map<String, Object> m2) {
					return ((Long) m1.get("proyectoId")).compareTo((Long) m2.get("proyectoId"));
				}
			};
	
			Collections.sort(proyectoInteresComisionListBuilder, mapComparator);			
				ac.put("proyectoInteresComisionListBuilder", proyectoInteresComisionListBuilder);
	
		}
		
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}
	
}
