package com.absoluciones.gastosreserva;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
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
import org.opentaps.base.entities.Estatus;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.domain.DomainsDirectory;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.action.ActionContext;

public class BusquedaGastosReserva {

	public static void buscaGasto(Map<String, Object> context) throws GeneralException, ParseException {

		final ActionContext ac = new ActionContext(context);
		final Locale locale = ac.getLocale();
		TimeZone timeZone = ac.getTimeZone();
		String dateTimeFormat = UtilDateTime.getDateFormat(locale);
		Delegator delegator = ac.getDelegator();
		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();
		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
	
		String gastosReservaId = ac.getParameter("gastosReservaId");
		String filtroId = ac.getParameter("filtroId");
		String statusId = ac.getParameter("statusId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String usuario = userLogin.getString("partyId");
		Timestamp fecha_inicial = null;
		Timestamp fecha_final=null;
		String proveedor = ac.getParameter("proveedor");
		String performFind = ac.getParameter("performFind");
		
		if(UtilValidate.isEmpty(statusId)){
			statusId = ac.getString("statusId");
		}
		if(UtilValidate.isEmpty(performFind)){
			performFind = ac.getString("performFind");
		}		
				
		if(UtilValidate.isNotEmpty(ac.getParameter("fecha"))) {
			
			String fecha = ac.getParameter("fecha");
			fecha_inicial = UtilDateTime.getDayStart(
						UtilDateTime.getTimestamp(
								UtilDateTime.getDateFromString(dateTimeFormat, timeZone, locale, fecha).getTime()));
			fecha_final = UtilDateTime.getDayEnd(fecha_inicial);
			
		}
		
		List<EntityCondition> condicionesEstatus = new FastList<EntityCondition>();
		condicionesEstatus.add(EntityCondition.makeCondition(
				Estatus.Fields.tipo.name(), EntityOperator.EQUALS, "G"));
		List<Estatus> estatus = ledgerRepository.findList(Estatus.class,
				condicionesEstatus);
		List<Map<String, Object>> estatusList = new FastList<Map<String, Object>>();
		for (Estatus s : estatus) {
			Map<String, Object> map = s.toMap();
			estatusList.add(map);
		}
		ac.put("estatusList", estatusList);
		
		if ("Y".equals(performFind)) {
			// build search conditions
			List<EntityCondition> condicionesBusqueda = new FastList<EntityCondition>();
			
			if (UtilValidate.isNotEmpty(fecha_inicial)) {
				condicionesBusqueda.add(EntityCondition.makeCondition(
						"fecha", EntityOperator.GREATER_THAN_EQUAL_TO, fecha_inicial));
				condicionesBusqueda.add(EntityCondition.makeCondition(
						"fecha", EntityOperator.LESS_THAN_EQUAL_TO, fecha_final));
			}

			if (UtilValidate.isNotEmpty(gastosReservaId)) {
				condicionesBusqueda.add(EntityCondition.makeCondition("gastosReservaId", EntityOperator.EQUALS, gastosReservaId));
			}
			if (filtroId != null) {
				condicionesBusqueda.add(EntityCondition.makeCondition("solicitanteId", EntityOperator.EQUALS,usuario));
			}
			if (UtilValidate.isNotEmpty(statusId)) {
				condicionesBusqueda.add(EntityCondition.makeCondition("statusId",EntityOperator.EQUALS, statusId));
			}
			if (UtilValidate.isNotEmpty(proveedor)) {
				condicionesBusqueda.add(EntityCondition.makeCondition("proveedor",EntityOperator.LIKE, "%"+proveedor+"%"));
			}
			if (UtilValidate.isNotEmpty(organizationPartyId)) {
				condicionesBusqueda.add(EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS,organizationPartyId));
			}
			
			GenericValue partyRole = delegator.findByPrimaryKey("PartyRole",UtilMisc.toMap("partyId", usuario, "roleTypeId","VIAINSTITUCION"));
			
			if (partyRole == null) {
				partyRole = delegator.findByPrimaryKey("PartyRole", UtilMisc.toMap("partyId",usuario, "roleTypeId", "VIAAREA"));
				if(partyRole != null){
					GenericValue person = delegator.findByPrimaryKey("Person",UtilMisc.toMap("partyId", usuario));
									
					condicionesBusqueda.add(EntityCondition.makeCondition("areaId", EntityOperator.EQUALS,person.getString("areaId")));
				}else{
					condicionesBusqueda.add(EntityCondition.makeCondition("solicitanteId", EntityOperator.EQUALS,usuario));
				}
			}

			EntityCondition condiciones = EntityCondition.makeCondition(condicionesBusqueda, EntityOperator.AND);
			
			//Se crea una lista con los elementos resultantes
	        List<GenericValue> listGastoReserva = delegator.findByCondition("BuscarGastoReserva", condiciones, null, null);
	        
	        //Se crea una lista provisional para almacenar los elementos que van a ser insertdos en el mapa
	        List<Map<String,Object>> gastoReservaListBuilder = FastList.newInstance();
	        //Se crea el mapa provisional que se va a llenar de los objets que va a la pantalla
	        Map<String,Object> mapaGastosReserva = FastMap.newInstance();
	        //Se itera el mapa para colocar los elementos en el mapa de manera formateada
	        for (GenericValue GastosReserva : listGastoReserva) {
	        	mapaGastosReserva.putAll(GastosReserva.getAllFields());
	        	mapaGastosReserva.put("gastosReservaId", Long.valueOf(GastosReserva.getString("gastosReservaId")));
	        	gastoReservaListBuilder.add(mapaGastosReserva);
	        	mapaGastosReserva = FastMap.newInstance();
			}
	        
	        //Se crea un metodo comparator para ordenar los elementos del mapa
	        Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
				public int compare(Map<String, Object> m1, Map<String, Object> m2) {
					return ((Long) m1.get("gastosReservaId")).compareTo((Long) m2.get("gastosReservaId"));
				}
			};

			Collections.sort(gastoReservaListBuilder, mapComparator);
			//Se coloca el mapa con el identificador en la instancia del actioncontext
			ac.put("gastoReservaListBuilder", gastoReservaListBuilder);
		}
	}
	
	/**
	 * Metodo que busca los gastos pendientes por autorizar
	 * @param context
	 * @throws GeneralException
	 * @throws ParseException
	 */
	public static void buscaGastoAutorizar(Map<String, Object> context) throws GeneralException, ParseException {

		final ActionContext ac = new ActionContext(context);
		Delegator delegator = ac.getDelegator();
		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();
		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
	
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String usuario = userLogin.getString("partyId");
		String performFind = ac.getParameter("performFind");
		
		if(UtilValidate.isEmpty(performFind)){
			performFind = ac.getString("performFind");
		}		
		
		List<EntityCondition> condicionesEstatus = new FastList<EntityCondition>();
		condicionesEstatus.add(EntityCondition.makeCondition(
				Estatus.Fields.tipo.name(), EntityOperator.EQUALS, "G"));
		List<Estatus> estatus = ledgerRepository.findList(Estatus.class,
				condicionesEstatus);
		List<Map<String, Object>> estatusList = new FastList<Map<String, Object>>();
		for (Estatus s : estatus) {
			Map<String, Object> map = s.toMap();
			estatusList.add(map);
		}
		ac.put("estatusList", estatusList);
		
		if ("Y".equals(performFind)) {
			// build search conditions
			List<EntityCondition> condicionesBusqueda = new FastList<EntityCondition>();

			if (UtilValidate.isNotEmpty(organizationPartyId)) {
				condicionesBusqueda.add(EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS,organizationPartyId));
			}
			
			if (UtilValidate.isNotEmpty(usuario)) {
				condicionesBusqueda.add(EntityCondition.makeCondition("personParentId", EntityOperator.EQUALS,usuario));
			}
			
			condicionesBusqueda.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "PENDIENTE"));

			EntityCondition condiciones = EntityCondition.makeCondition(condicionesBusqueda, EntityOperator.AND);
			
			//Se crea una lista con los elementos resultantes
	        List<GenericValue> listGastoReserva = delegator.findByCondition("BuscarGastoAutorizador", condiciones, null, null);
	        
	        //Se crea una lista provisional para almacenar los elementos que van a ser insertdos en el mapa
	        List<Map<String,Object>> gastoReservaListBuilder = FastList.newInstance();
	        //Se crea el mapa provisional que se va a llenar de los objets que va a la pantalla
	        Map<String,Object> mapaGastosReserva = FastMap.newInstance();
	        //Se itera el mapa para colocar los elementos en el mapa de manera formateada
	        for (GenericValue GastosReserva : listGastoReserva) {
	        	mapaGastosReserva.putAll(GastosReserva.getAllFields());
	        	mapaGastosReserva.put("gastosReservaId", Long.valueOf(GastosReserva.getString("gastosReservaId")));
	        	gastoReservaListBuilder.add(mapaGastosReserva);
	        	mapaGastosReserva = FastMap.newInstance();
			}
	        
	        //Se crea un metodo comparator para ordenar los elementos del mapa
	        Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
				public int compare(Map<String, Object> m1, Map<String, Object> m2) {
					return ((Long) m1.get("gastosReservaId")).compareTo((Long) m2.get("gastosReservaId"));
				}
			};

			Collections.sort(gastoReservaListBuilder, mapComparator);
			//Se coloca el mapa con el identificador en la instancia del actioncontext
			ac.put("gastoReservaListBuilder", gastoReservaListBuilder);
		}
	}
	
	public static void gastosPendientes(Map<String, Object> context) throws GeneralException, ParseException {
		final ActionContext ac = new ActionContext(context);
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String solicitante = userLogin.getString("partyId");
		String status = (String) context.get("estatus");
		String persona = (String) context.get("persona");
		//String status = "PENDIENTE";
		
		Delegator delegator = ac.getDelegator();

		// build search conditions
		List<EntityCondition> searchConditions = new FastList<EntityCondition>();
		if (persona != null) {
			searchConditions.add(persona.equalsIgnoreCase("autorizador") ? EntityCondition.makeCondition("personParentId",EntityOperator.EQUALS, solicitante) : EntityCondition.makeCondition("solicitanteId",EntityOperator.EQUALS, solicitante));
		}
		
		searchConditions.add(status.equalsIgnoreCase("PENDIENTE")?EntityCondition.makeCondition("statusId",EntityOperator.EQUALS, status):EntityCondition.makeCondition("statusGxC",EntityOperator.EQUALS, status));
		searchConditions.add(EntityCondition.makeCondition("tipoWorkFlowId",EntityOperator.IN, UtilMisc.toList("GASTORESERVA")));
		
		
		//Variable para definir el campo por el que se va a ordenar
		List<String> fieldsToSelect = UtilMisc.toList("gastosReservaId", "descripcion", "solicitanteId", "firstName", "lastName", "fecha", "proveedor", "monto", "tipoMoneda", "statusId");
		List<String> orderBy = UtilMisc.toList("fecha");
		//Definicion de la condicion de los where
		EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
		//Se crea una lista con los elementos resultantes, apuntando a la vista del entity%.xml
        List<GenericValue> listGastosPendientes = delegator.findByCondition("BuscarGastoReservaComp", condiciones, fieldsToSelect, orderBy);
        
        //Se crea una lista provisional para almacenar los elementos que van a ser insertdos en el mapa
        List<Map<String,Object>> gastoPendienteListBuilder = FastList.newInstance();
        //Se crea el mapa provisional que se va a llenar de los objets que va a la pantalla
        Map<String,Object> mapaGastosPendientes = FastMap.newInstance();
        //Se itera el mapa para colocar los elementos en el mapa de manera formateada
        for (GenericValue Gastos : listGastosPendientes) {
        	mapaGastosPendientes.putAll(Gastos.getAllFields());
        	mapaGastosPendientes.put("gastosReservaId", Long.valueOf(Gastos.getString("gastosReservaId")));
        	gastoPendienteListBuilder.add(mapaGastosPendientes);
        	mapaGastosPendientes = FastMap.newInstance();
		}
        
        //Se crea un metodo comparator para ordenar los elementos del mapa
        Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
			public int compare(Map<String, Object> m1, Map<String, Object> m2) {
				return ((Long) m1.get("gastosReservaId")).compareTo((Long) m2.get("gastosReservaId"));
			}
		};

		Collections.sort(gastoPendienteListBuilder, mapComparator);
		//Se coloca el mapa con el identificador en la instancia del actioncontext
		ac.put("gastoReservaPendienteListBuilder", gastoPendienteListBuilder);		
	}
}
