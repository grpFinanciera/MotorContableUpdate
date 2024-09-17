package org.opentaps.purchasing.pedidosInternos;

import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
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
import org.opentaps.base.entities.BuscarPedidoInternoConProduct;
import org.opentaps.base.entities.BuscarPedidoInternoSinProduct;
import org.opentaps.base.entities.Estatus;
import org.opentaps.base.entities.PartyRole;
import org.opentaps.base.entities.PedidosInternosPendientes;
import org.opentaps.common.builder.EntityListBuilder;
import org.opentaps.common.builder.PageBuilder;
import org.opentaps.domain.DomainsDirectory;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.action.ActionContext;
import org.opentaps.foundation.repository.RepositoryException;

import com.opensourcestrategies.crmsfa.party.PartyHelper;

public class ConsultarPedidoInterno {
	
	private static final String MODULE = ConsultarPedidoInterno.class.getName();

	@SuppressWarnings("unused")
	public static void buscaPedidoInterno(Map<String, Object> context)
			throws GeneralException, ParseException {

		final ActionContext ac = new ActionContext(context);
		final Locale locale = ac.getLocale();
		Delegator delegator = ac.getDelegator();
//		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac
//				.getRequest());

		// possible fields we're searching by
		String pedidoInternoId = ac.getParameter("pedidoInternoId");
		String filtroId = ac.getParameter("filtroId");
		String estatusId = ac.getParameter("statusId");
		String productId = ac.getParameter("productId");
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
				Estatus.Fields.tipo.name(), EntityOperator.EQUALS, "S"));
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

			if (UtilValidate.isNotEmpty(pedidoInternoId)) {
				searchConditions.add(EntityCondition.makeCondition(
						"pedidoInternoId", EntityOperator.EQUALS,
						pedidoInternoId));
			}
			if (UtilValidate.isNotEmpty(estatusId)) {
				searchConditions.add(EntityCondition.makeCondition("statusId",
						EntityOperator.EQUALS, estatusId));
			}
			if (UtilValidate.isNotEmpty(productId)) {
				searchConditions.add(EntityCondition.makeCondition("productId",
						EntityOperator.EQUALS, productId));
			}
			if (UtilValidate.isNotEmpty(usuario)) {
				searchConditions
						.add(EntityCondition.makeCondition(
								"personaSolicitanteId", EntityOperator.EQUALS,
								usuario));
			}
//			if (UtilValidate.isNotEmpty(organizationPartyId)) {
//				searchConditions.add(EntityCondition.makeCondition(
//						"organizationPartyId", EntityOperator.EQUALS,
//						organizationPartyId));
//			}

			EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
			
			// fields to select
			List<String> fieldsToSelect = UtilMisc.toList("pedidoInternoId",
					"statusId", "personaSolicitanteId", "fechaAtendida", "organizationPartyId", "partyId", "firstName", "lastName", "descripcion");
			List<String> orderBy = UtilMisc.toList("pedidoInternoId");
			Debug.logInfo(
					"search conditions : "
							+ EntityCondition.makeCondition(searchConditions,
									EntityOperator.AND).toString(), MODULE);
			
			List<GenericValue> listPedidoInterno = delegator.findByCondition("BuscarPedidoInternoConProduct", condiciones, fieldsToSelect, orderBy);

			List<Map<String,Object>> listPedidoInternoBuilder = FastList.newInstance();
			
			Map<String,Object> mapaPedidoInterno = FastMap.newInstance();
			
			GenericValue Person = delegator.makeValue("Person");
			for (GenericValue pedidoInterno : listPedidoInterno) {
				mapaPedidoInterno.putAll(pedidoInterno.getAllFields());		
				mapaPedidoInterno.put("pedidoInternoId", Long.valueOf(pedidoInterno.getString("pedidoInternoId")));
				Person.setAllFields(pedidoInterno, false, null, null);
				mapaPedidoInterno.put("personaNameDesc", PartyHelper.getCrmsfaPartyName(Person));
				Person = delegator.makeValue("Person");
				listPedidoInternoBuilder.add(mapaPedidoInterno);
				mapaPedidoInterno = FastMap.newInstance();
			}
			
			Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
				public int compare(Map<String, Object> m1, Map<String, Object> m2) {
					return ((Long) m1.get("pedidoInternoId")).compareTo((Long) m2.get("pedidoInternoId"));
				}
			};

			Collections.sort(listPedidoInternoBuilder, mapComparator);	
				ac.put("pedidoInternoListBuilder", listPedidoInternoBuilder);
		}
	}

	public static void pedidosInternosPendientes(Map<String, Object> context)
			throws GeneralException, ParseException {
		final ActionContext ac = new ActionContext(context);
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String autorizador = userLogin.getString("partyId");
		String status = "PENDIENTE";
		Debug.log("usuario: " + userLogin);

		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
				.getLedgerRepository();
		
		List<EntityCondition> searchConditions = new FastList<EntityCondition>();
		searchConditions.add(EntityCondition.makeCondition("personParentId",
				EntityOperator.EQUALS, autorizador));
		searchConditions.add(EntityCondition.makeCondition("statusId",
				EntityOperator.EQUALS, status));
		searchConditions.add(EntityCondition.makeCondition("tipoWorkFlowId",
				EntityOperator.EQUALS, "PEDIDO_INTERNO"));
		
		// fields to select
		List<String> orderBy = UtilMisc.toList("PedidoInternoId");
		Debug.log("Search conditions : " + searchConditions);
		EntityListBuilder pedidosInternosPendientesListBuilder = null;
		PageBuilder<PedidosInternosPendientes> pageBuilderPedidosInternosPendientes = null;

		pedidosInternosPendientesListBuilder = new EntityListBuilder(ledgerRepository,
				PedidosInternosPendientes.class, EntityCondition.makeCondition(
						searchConditions, EntityOperator.AND), null, orderBy);
		pageBuilderPedidosInternosPendientes = new PageBuilder<PedidosInternosPendientes>() {
			public List<Map<String, Object>> build(
					List<PedidosInternosPendientes> page) throws Exception {
				List<Map<String, Object>> newPage = FastList.newInstance();
				for (PedidosInternosPendientes pedId : page) {
					Map<String, Object> newRow = FastMap.newInstance();
					newRow.putAll(pedId.toMap());
					newPage.add(newRow);
				}
				return newPage;
			}
		};
		pedidosInternosPendientesListBuilder
				.setPageBuilder(pageBuilderPedidosInternosPendientes);
		ac.put("pedidosInternosPendientesListBuilder", pedidosInternosPendientesListBuilder);
	}
	

	/*
	 * Obtenemos el areaId
	 */
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
