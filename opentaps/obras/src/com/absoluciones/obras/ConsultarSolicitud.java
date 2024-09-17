package com.absoluciones.obras;

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
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.opentaps.base.entities.BuscarSolicitud;
import org.opentaps.base.entities.Estatus;
import org.opentaps.base.entities.SolicitudesPendientes;
import org.opentaps.base.entities.SolicitudesPendientesObra;
import org.opentaps.common.builder.EntityListBuilder;
import org.opentaps.common.builder.PageBuilder;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.domain.DomainsDirectory;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.action.ActionContext;
import org.opentaps.foundation.repository.RepositoryException;

public class ConsultarSolicitud {

	private static final String MODULE = ConsultarSolicitud.class.getName();

	@SuppressWarnings("unused")
	public static void buscaSolicitud(Map<String, Object> context)
			throws GeneralException, ParseException {

		final ActionContext ac = new ActionContext(context);
		final Locale locale = ac.getLocale();
		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac
				.getRequest());

		// possible fields we're searching by
		String obraId = ac.getParameter("obraId");
		String filtroId = ac.getParameter("filtroId");
		String estatusId = ac.getParameter("statusId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String usuario = null;

		if (filtroId != null)
			usuario = userLogin.getString("partyId");

		Debug.log("usuario: " + userLogin);

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
				searchConditions.add(EntityCondition.makeCondition("statusId",
						EntityOperator.EQUALS, estatusId));
			}
			if (UtilValidate.isNotEmpty(usuario)) {
				searchConditions
						.add(EntityCondition.makeCondition(
								"personaSolicitanteId", EntityOperator.EQUALS,
								usuario));
			}
			if (UtilValidate.isNotEmpty(organizationPartyId)) {
				searchConditions.add(EntityCondition.makeCondition(
						"organizationPartyId", EntityOperator.EQUALS,
						organizationPartyId));
			}

			List<String> orderBy = UtilMisc.toList("obraId");
			Debug.logInfo(
					"search conditions : "
							+ EntityCondition.makeCondition(searchConditions,
									EntityOperator.AND).toString(), MODULE);
			EntityListBuilder solicitudListBuilder = null;
			PageBuilder<BuscarSolicitud> pageBuilderSolicitud = null;

			solicitudListBuilder = new EntityListBuilder(
						ledgerRepository, BuscarSolicitud.class,
						EntityCondition.makeCondition(searchConditions,
								EntityOperator.AND), null, orderBy);
				pageBuilderSolicitud = new PageBuilder<BuscarSolicitud>() {
					public List<Map<String, Object>> build(
							List<BuscarSolicitud> page)
							throws Exception {
						Delegator delegator = ac.getDelegator();
						List<Map<String, Object>> newPage = FastList
								.newInstance();
						for (BuscarSolicitud solicitud : page) {
							Map<String, Object> newRow = FastMap.newInstance();
							newRow.putAll(solicitud.toMap());
							Debug.log("solicitud.- "+solicitud);
							String nombre = solicitud.getFirstName() + " "
									+ solicitud.getLastName();
							newRow.put("personaNameDesc", nombre);
							newPage.add(newRow);
						}
						return newPage;
					}
				};
				solicitudListBuilder.setPageBuilder(pageBuilderSolicitud);
				ac.put("solicitudListBuilder", solicitudListBuilder);
		}
	}

	public static void solicitudesPendientes(Map<String, Object> context)
			throws GeneralException, ParseException {
		final ActionContext ac = new ActionContext(context);
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String solicitante = userLogin.getString("partyId");
		String status = "PENDIENTE";
		Debug.log("usuario: " + userLogin);

		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
				.getLedgerRepository();

		// build search conditions
		List<EntityCondition> searchConditions = new FastList<EntityCondition>();
		searchConditions.add(EntityCondition.makeCondition("personParentId",
				EntityOperator.EQUALS, solicitante));
		searchConditions.add(EntityCondition.makeCondition("statusId",
				EntityOperator.EQUALS, status));
		searchConditions.add(EntityCondition.makeCondition("tipoWorkFlowId",
				EntityOperator.EQUALS, "OBRAS"));

		// fields to select
		List<String> orderBy = UtilMisc.toList("obraId");
		Debug.log("Search conditions : " + searchConditions);
		EntityListBuilder solicitudPendienteListBuilder = null;
		PageBuilder<SolicitudesPendientesObra> pageBuilderSolicitudesPendientes = null;
		
		////
		List<SolicitudesPendientesObra> solicitudes = ledgerRepository.findList(SolicitudesPendientesObra.class, EntityCondition.makeCondition(
				searchConditions, EntityOperator.AND));
		Debug.log("Solicitudes:");
		
		solicitudPendienteListBuilder = new EntityListBuilder(ledgerRepository,
				SolicitudesPendientesObra.class, EntityCondition.makeCondition(
						searchConditions, EntityOperator.AND), null, orderBy);
		pageBuilderSolicitudesPendientes = new PageBuilder<SolicitudesPendientesObra>() {
			public List<Map<String, Object>> build(
					List<SolicitudesPendientesObra> page) throws Exception {
				List<Map<String, Object>> newPage = FastList.newInstance();
				for (SolicitudesPendientesObra solicitud : page) {
					Map<String, Object> newRow = FastMap.newInstance();
					newRow.putAll(solicitud.toMap());
					newPage.add(newRow);
				}
				return newPage;
			}
		};
		solicitudPendienteListBuilder
				.setPageBuilder(pageBuilderSolicitudesPendientes);
		ac.put("solicitudPendienteListBuilder", solicitudPendienteListBuilder);
	}
	

	public static String obtenerArea(Delegator delegator, String user)
			throws GenericEntityException {

		Debug.log("Mike user: " + user);
		GenericValue userLogin = delegator.findByPrimaryKey("UserLogin",
				UtilMisc.toMap("userLoginId", user));

		GenericValue person = delegator.findByPrimaryKey("Person",
				UtilMisc.toMap("partyId", userLogin.getString("partyId")));

		Debug.log("Mike encontro party: " + person.getString("areaId"));
		return person.getString("areaId");
	}

	public static String obtenPadre(Delegator delegator, String organizationId)
			throws RepositoryException, GenericEntityException {
		String parentId = organizationId;
		do {
			GenericValue generic = obtenPartyGroup(delegator, organizationId);

			organizationId = generic.getString("Parent_id");
			if (organizationId != null)
				parentId = generic.getString("Parent_id");
		} while (organizationId != null);
		return parentId;
	}

	public static GenericValue obtenPartyGroup(Delegator delegator, String party)
			throws GenericEntityException {

		GenericValue generic = delegator.findByPrimaryKey("PartyGroup",
				UtilMisc.toMap("partyId", party));

		return generic;
	}
}
