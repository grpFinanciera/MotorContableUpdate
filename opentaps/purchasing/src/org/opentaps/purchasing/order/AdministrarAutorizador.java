package org.opentaps.purchasing.order;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

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
import org.opentaps.base.entities.Autorizador;
import org.opentaps.base.entities.BuscarAutorizador;
import org.opentaps.base.entities.BuscarNombreParty;
import org.opentaps.base.entities.BuscarPenaDeductiva;
import org.opentaps.base.entities.TipoPenaDeductiva;
import org.opentaps.common.builder.EntityListBuilder;
import org.opentaps.common.builder.ListBuilderException;
import org.opentaps.common.builder.PageBuilder;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.domain.DomainsDirectory;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.action.ActionContext;
import org.opentaps.foundation.repository.RepositoryException;

public class AdministrarAutorizador {

	private static final String MODULE = AdministrarAutorizador.class.getName();

	public static void agregarArea(Map<String, Object> context)
			throws GeneralException, ParseException {

		final ActionContext ac = new ActionContext(context);
		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac
				.getRequest());

		// possible fields we're searching by
		String areaId = ac.getParameter("areaId");
		String personId = ac.getParameter("personId");
		String tipoWorkflowId = ac.getParameter("tipoWorkFlowId");
		Delegator delegator = ac.getDelegator();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		List<String> aviso = new ArrayList<String>();
		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
				.getLedgerRepository();

		Debug.log("Area: " + areaId + " Persona: " + personId);
		
		List<GenericValue> workflowList = delegator.findAll("TipoWorkFlow");
		ac.put("workflowList", workflowList);

		// damos de alta una nueva area
		if ("Y".equals(ac.getParameter("performFind"))) {

			Debug.log("Ingresa a crear un area");
			
			// verificamos datos obligatorios
			if (areaId == null) {
				aviso.add("Es necesario agregar el \u00E1rea");
			}
			if (personId == null) {
				aviso.add("Es necesario agregar el autorizador");
			}

			// verificamos que existan los datos y no se haya registrado
			// anteriormente
			if (verificaParty(delegator, areaId) == null) {
				aviso.add("El area ingresada no existe");
			}
			if (verificaParty(delegator, personId) == null) {
				aviso.add("El autorizador ingresado no existe");
			}
			if (!verificaArea(delegator, areaId, organizationPartyId, tipoWorkflowId).isEmpty()) {
				aviso.add("El \u00E1rea ya ha sido ingresada con el tipo de workflow seleccionado");
			}

			// registramos el area con el autorizador
			if (aviso.isEmpty()) {
				addAutorizador(delegator, areaId, personId, 1,
						organizationPartyId, userLogin.getString("userLoginId"), tipoWorkflowId);
			} else {
				ac.put("error", aviso);
				Debug.log("Error 2: " + aviso);
			}

		}

		// build search conditions
		List<EntityCondition> searchConditions = new FastList<EntityCondition>();

		if (UtilValidate.isNotEmpty(organizationPartyId)) {
			searchConditions.add(EntityCondition.makeCondition(
					"organizationPartyId", EntityOperator.EQUALS,
					organizationPartyId));
		}
		
		// fields to select
		List<String> fieldsToSelect = UtilMisc.toList("areaId",
				"groupNameLocal", "partyId", "organizationPartyId","tipoWorkFlowId");
		Debug.logInfo(
				"search conditions : "
						+ EntityCondition.makeCondition(searchConditions,
								EntityOperator.AND).toString(), MODULE);

		// obtenemos las areas ya registradas
		EntityListBuilder areaListBuilder = new EntityListBuilder(
				ledgerRepository, BuscarNombreParty.class,
				EntityCondition.makeCondition(searchConditions,
						EntityOperator.AND), fieldsToSelect,
				UtilMisc.toList(BuscarNombreParty.Fields.areaId.desc()));

		PageBuilder<BuscarNombreParty> pageBuilder = new PageBuilder<BuscarNombreParty>() {
			public List<Map<String, Object>> build(List<BuscarNombreParty> page)
					throws Exception {
				List<Map<String, Object>> newPage = FastList.newInstance();
				for (BuscarNombreParty tipoDoc : page) {
					Map<String, Object> newRow = FastMap.newInstance();
					newRow.putAll(tipoDoc.toMap());
					newPage.add(newRow);
				}
				return newPage;
			}
		};
		areaListBuilder.setPageBuilder(pageBuilder);
		ac.put("areaListBuilder", areaListBuilder);
		Debug.log("areas: " + areaListBuilder);

	}

	public static void agregarAutorizador(Map<String, Object> context)
			throws GeneralException, ParseException {

		final ActionContext ac = new ActionContext(context);
		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac
				.getRequest());

		// possible fields we're searching by
		String areaId = ac.getParameter("areaId");
		String personId = ac.getParameter("personId");
		String organization = ac.getParameter("organizationPartyId");
		String autorizadorId = ac.getParameter("autorizadorId");
		String tipoWorkflowId = ac.getParameter("tipoWorkFlowId");
		int posicion = 0;
		Delegator delegator = ac.getDelegator();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		List<String> aviso = new ArrayList<String>();
		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
				.getLedgerRepository();

		Debug.log("Area: " + areaId + " Persona: " + personId + " Organizacion: " + organization);

		// damos de alta una nueva area
		if ("Y".equals(ac.getParameter("performFind"))) {

			Debug.log("Ingresa a a\u00F1adir un nuevo usuario");
			
			// verificamos datos obligatorios
			if (areaId == null) {
				aviso.add("Es necesario agregar el \u00e1rea");
			}
			if (personId == null) {
				aviso.add("Es necesario agregar el autorizador");
			}
			if (!aviso.isEmpty()) {
				ac.put("error", aviso);
				Debug.log("Error: " + aviso);
			}
			// verificamos que existan los datos y no se haya registrado
			// anteriormente
			if (verificaParty(delegator, areaId) == null) {
				aviso.add("El area ingresada no existe");
			}
			if (verificaParty(delegator, personId) == null) {
				aviso.add("El autorizador ingresado no existe");
			}	
			
			if (!verificaPerson(delegator, areaId, organizationPartyId, personId, tipoWorkflowId).isEmpty()) {
					aviso.add("El autorizador ingresado ya se encuentra en la lista de pre-autorizadores");
	        }

			// registramos el area con el autorizador
			if (aviso.isEmpty()) {
				addAutorizador(delegator, areaId, personId,
						0, organizationPartyId,
						userLogin.getString("userLoginId"), tipoWorkflowId);
			} else {
				ac.put("error", aviso);
				Debug.log("Error 2: " + aviso);
			}

		} else if ("A".equals(ac.getParameter("performFind"))) {

			Debug.log("Ingresa a a\u00f1adir ");

			// registramos el area con el autorizador
			if (autorizadorId != null) {

				List<GenericValue> secuencia = verificaArea(delegator, areaId,
						organizationPartyId, tipoWorkflowId);
				if (!secuencia.isEmpty()) {
					Iterator<GenericValue> secuenciaId = secuencia.iterator();
					GenericValue sec = secuenciaId.next();
					if (sec.getLong("secuencia") > 0) {
						posicion = (int) (sec.getLong("secuencia") + 1);
					} else {
						posicion = 1;
					}

				}
					
			   if (!verificaAutorizador(delegator, areaId, organizationPartyId, autorizadorId,tipoWorkflowId).isEmpty()) {
						aviso.add("El autorizador ingresado ya se encuentra en la lista de autorizadores");
			   }
			   
			   // registramos el area con el autorizador
				if (aviso.isEmpty()) {
					addAutorizador(delegator, areaId,
							autorizadorId, posicion, organizationPartyId,
							userLogin.getString("userLoginId"),tipoWorkflowId);
				} else {
					ac.put("error", aviso);
					Debug.log("Error 2: " + aviso);
				}

						
				
			} else {
				aviso.add("Es necesario elegir un autorizador para a\u00f1adirlo");
				ac.put("error", aviso);
				Debug.log("Error 21: " + aviso);
			}

		}

		ac.put("areaListBuilder", paginacion(organizationPartyId, areaId, ledgerRepository, "N", tipoWorkflowId));
		
		
		 // get the list of autorizadores for the parametrized form ftl
        List<EntityCondition> searchConditionsMod = new FastList<EntityCondition>();
        searchConditionsMod.add(EntityCondition.makeCondition(Autorizador.Fields.areaId.name(), EntityOperator.EQUALS, areaId));
        searchConditionsMod.add(EntityCondition.makeCondition(Autorizador.Fields.organizationPartyId.name(), EntityOperator.EQUALS, organizationPartyId));
        searchConditionsMod.add(EntityCondition.makeCondition(Autorizador.Fields.tipoWorkFlowId.name(), EntityOperator.EQUALS, tipoWorkflowId));
        List<Autorizador> autorizadores = ledgerRepository.findList(Autorizador.class, searchConditionsMod);
        List<Map<String, Object>> autorizadorList = new FastList<Map<String, Object>>();
        for (Autorizador s : autorizadores) {
            Map<String, Object> map = s.toMap();
            autorizadorList.add(map);
        }
        ac.put("autorizadorList", autorizadorList);

		ac.put("autorizadorListBuilder", paginacion(organizationPartyId, areaId, ledgerRepository, "Y", tipoWorkflowId));
	}

	/*
	 * verificamos que se encuentre registrado el dato ingresado
	 */
	public static GenericValue verificaParty(Delegator delegator, String party)
			throws GenericEntityException {

		Debug.log("Mike party: " + party);
		GenericValue generic = delegator.findByPrimaryKey("Party",
				UtilMisc.toMap("partyId", party));

		Debug.log("Mike encontro party: " + generic);
		return generic;
	}

	/*
	 * verificamos que no haya una area ya registrada
	 */
	public static List<GenericValue> verificaArea(Delegator delegator,
			String areaId, String organizationPartyId, String tipoWorkFlowId)
			throws GenericEntityException {

		Debug.log("Mike area: " + areaId + " : " + organizationPartyId);
		List<GenericValue> busqueda = null;
		
		EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("areaId",EntityOperator.EQUALS,areaId),
				EntityCondition.makeCondition("organizationPartyId",EntityOperator.LIKE,organizationPartyId),
				EntityCondition.makeCondition("tipoWorkFlowId",EntityOperator.EQUALS,tipoWorkFlowId));
		
		Debug.log("Mike condiciones: " + condiciones);

        busqueda = delegator.findByCondition("Autorizador", condiciones, UtilMisc.toList("secuencia"),
				UtilMisc.toList("secuencia DESC"));

		Debug.log("Mike encontro area: " + busqueda);

		return busqueda;
	}
	
	/*
	 * verificamos que no haya una persona ya registrada
	 */
	public static List<GenericValue> verificaPerson(Delegator delegator,
			String areaId, String organizationPartyId, String personId, String tipoWorkFlowId)
			throws GenericEntityException {

		Debug.log("Mike autorizador: " + areaId + " : " + organizationPartyId);
		List<GenericValue> busqueda = null;


			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND, 
					EntityCondition.makeCondition("areaId",	EntityOperator.EQUALS, areaId),
					EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS, organizationPartyId),
					EntityCondition.makeCondition("autorizadorId", EntityOperator.EQUALS, personId),
					EntityCondition.makeCondition("tipoWorkFlowId", EntityOperator.EQUALS, tipoWorkFlowId));

			Debug.log("Mike condiciones: " + condiciones);

			busqueda = delegator.findByCondition("Autorizador", condiciones,
					UtilMisc.toList("secuencia"),
					UtilMisc.toList("secuencia DESC"));

			Debug.log("Mike encontro autorizador: " + busqueda);


		return busqueda;
	}
	
	/*
	 * verificamos que no haya un autorizador ya registrado
	 */
	public static List<GenericValue> verificaAutorizador(Delegator delegator,
			String areaId, String organizationPartyId, String autorizador, String tipoWorkFlowId)
			throws GenericEntityException {

		Debug.log("Mike autorizador: " + areaId + " : " + organizationPartyId);
		List<GenericValue> busqueda = null;


			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND, 
					EntityCondition.makeCondition("areaId",	EntityOperator.EQUALS, areaId),
					EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS, organizationPartyId),
					EntityCondition.makeCondition("autorizadorId", EntityOperator.EQUALS, autorizador),
					EntityCondition.makeCondition("secuencia", EntityOperator.NOT_EQUAL, 0),
					EntityCondition.makeCondition("tipoWorkFlowId", EntityOperator.EQUALS, tipoWorkFlowId));

			Debug.log("Mike condiciones: " + condiciones);

			busqueda = delegator.findByCondition("Autorizador", condiciones,
					UtilMisc.toList("secuencia"),
					UtilMisc.toList("secuencia DESC"));

			Debug.log("Mike encontro autorizador: " + busqueda);


		return busqueda;
	}
	
	/*
	 * Anadir autorizador
	 */
	public static void addAutorizador(Delegator delegator, String areaId,
			String personId, int secuencia, String organizationPartyId,
			String userLoginId, String tipoWorkFlowId) throws GenericEntityException {
		GenericValue autorizador = GenericValue.create(delegator
				.getModelEntity("Autorizador"));
		autorizador.set("areaId", areaId);
		autorizador.set("autorizadorId", personId);
		autorizador.set("secuencia", secuencia);
		autorizador.set("organizationPartyId", organizationPartyId);
		autorizador.set("userLoginId", userLoginId);
		autorizador.set("tipoWorkFlowId", tipoWorkFlowId);
		delegator.createOrStore(autorizador);
	}
	
	public static EntityListBuilder paginacion(String organizationPartyId,
			String areaId, LedgerRepositoryInterface ledgerRepository,
			String indicador, String tipoWorkFlowId) throws ListBuilderException {

		// build search conditions
		List<EntityCondition> searchConditions = new FastList<EntityCondition>();

		if (UtilValidate.isNotEmpty(organizationPartyId)) {
			searchConditions.add(EntityCondition.makeCondition(
					"organizationPartyId", EntityOperator.EQUALS,
					organizationPartyId));
		}
		if (UtilValidate.isNotEmpty(areaId)) {
			searchConditions.add(EntityCondition.makeCondition("areaId",
					EntityOperator.EQUALS, areaId));
		}
		if (UtilValidate.isNotEmpty(tipoWorkFlowId)) {
			searchConditions.add(EntityCondition.makeCondition("tipoWorkFlowId",
					EntityOperator.EQUALS, tipoWorkFlowId));
		}
		if (indicador.equals("Y")) {
			searchConditions.add(EntityCondition.makeCondition("secuencia",
					EntityOperator.NOT_EQUAL, 0));
		}
		// fields to select
		List<String> fieldsToSelect = UtilMisc.toList("autorizadorId",
				"firstName", "lastName", "organizationPartyId", "secuencia", "areaId", "tipoWorkFlowId");
		Debug.logInfo(
				"search conditions : "
						+ EntityCondition.makeCondition(searchConditions,
								EntityOperator.AND).toString(), MODULE);

		// obtenemos los autorizadores registrados para esa area
		EntityListBuilder areaListBuilder = new EntityListBuilder(
				ledgerRepository, BuscarAutorizador.class,
				EntityCondition.makeCondition(searchConditions,
						EntityOperator.AND), fieldsToSelect,
				UtilMisc.toList(BuscarAutorizador.Fields.autorizadorId.desc()));

		PageBuilder<BuscarAutorizador> pageBuilder = new PageBuilder<BuscarAutorizador>() {
			public List<Map<String, Object>> build(List<BuscarAutorizador> page)
					throws Exception {
				List<Map<String, Object>> newPage = FastList.newInstance();
				for (BuscarAutorizador auto : page) {
					Map<String, Object> newRow = FastMap.newInstance();
					newRow.putAll(auto.toMap());
					String nombre = auto.getFirstName() + " "
							+ auto.getLastName();
					newRow.put("personaNameDesc", nombre);
					newPage.add(newRow);
				}
				return newPage;
			}
		};
		areaListBuilder.setPageBuilder(pageBuilder);
		return areaListBuilder;
	}
	
	/**
	 * 
	 * @param context
	 * @throws GenericEntityException
	 */
	public static void cambioPenaDeductiva(Map<String, Object> context) throws GenericEntityException {
		
		try {
	
			final ActionContext ac = new ActionContext(context);
			String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
			
			Debug.logInfo("Mike Action Context: " + context,MODULE);
			
			HttpSession session = ac.getRequest().getSession(true);

			// possible fields we're searching by
			GenericValue userLogin = (GenericValue) context.get("userLogin");
			
			Delegator delegator = ac.getDelegator();
			DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
			final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
					.getLedgerRepository();

			String tipo = ac.getParameter("tipoPena");
			String flag = ac.getParameter("flagV");
			String penaId = ac.getParameter("penaId");
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();
			List<EntityCondition> searchConditions2 = new FastList<EntityCondition>();
			List<BuscarPenaDeductiva> busqueda = null;
			
			if(tipo != null || flag != null){
				if(tipo.equals("Pena Convencional")){
					searchConditions.add(EntityCondition.makeCondition(
							TipoPenaDeductiva.Fields.penaDeductivaFlag.name(),
							EntityOperator.EQUALS, "P"));
					searchConditions2.add(EntityCondition.makeCondition(
							BuscarPenaDeductiva.Fields.penaDeductivaFlag.name(),
							EntityOperator.EQUALS, "P"));
				}else if(tipo.equals("Deductiva")){
					searchConditions.add(EntityCondition.makeCondition(
							TipoPenaDeductiva.Fields.penaDeductivaFlag.name(),
							EntityOperator.EQUALS, "D"));
					searchConditions2.add(EntityCondition.makeCondition(
							BuscarPenaDeductiva.Fields.penaDeductivaFlag.name(),
							EntityOperator.EQUALS, "D"));
				}
				
				List<TipoPenaDeductiva> penasList = ledgerRepository
						.findList(TipoPenaDeductiva.class, searchConditions, UtilMisc.toList("penaId"));
				
				List<Map<String, Object>> penas = new FastList<Map<String, Object>>();
				
				for (TipoPenaDeductiva s : penasList) {
					Map<String, Object> map = s.toMap();
					penas.add(map);
				}
				
				ac.put("penasList", penas);
				
				searchConditions2.add(EntityCondition.makeCondition(
						BuscarPenaDeductiva.Fields.organizationPartyId.name(),
						EntityOperator.EQUALS, organizationPartyId));
				
				
				busqueda = ledgerRepository.findList(BuscarPenaDeductiva.class, searchConditions2, UtilMisc.toList("penaId"));
				
				if(flag != null && busqueda.isEmpty()){
					Debug.log("Mike entra a crear le pena");
					GenericValue guardarPena = GenericValue.create(delegator
							.getModelEntity("PenaDeductiva"));
					guardarPena.set("penaDeductivaId", delegator.getNextSeqId("PenaDeductiva"));
					guardarPena.set("penaId", penaId);
					guardarPena.set("organizationPartyId", organizationPartyId);
					delegator.create(guardarPena);					
				}else if(flag != null && !busqueda.isEmpty()){
					Debug.log("Mike entra a actualizar");
					GenericValue guardarPena = GenericValue.create(delegator
							.getModelEntity("PenaDeductiva"));
					guardarPena.set("penaDeductivaId", busqueda.get(0).getString("penaDeductivaId"));
					guardarPena.set("penaId", penaId);
					delegator.create(guardarPena);
				}
				
				busqueda = ledgerRepository.findList(BuscarPenaDeductiva.class, searchConditions2, UtilMisc.toList("penaId"));
				
				if(!busqueda.isEmpty()){
					ac.put("penaDeductiva", busqueda.get(0).getString("penaId"));
				}	
			}
			
			ac.put("tipoPena", tipo);
			
		} catch (RepositoryException e) {
			e.printStackTrace();
		} 

	}
	
}
