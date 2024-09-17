package com.absoluciones.proyectos;

import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.opentaps.base.entities.Estatus;
import org.opentaps.common.party.PartyHelper;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.domain.DomainsDirectory;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.action.ActionContext;

import com.absoluciones.proyectos.ConsultaViaticosProyectos;

import javolution.util.FastList;
import javolution.util.FastMap;

public class ConsultaViaticosProyectos {
	
private static final String MODULE = ConsultaViaticosProyectos.class.getName();
	
	/**
	 * Se ejecuta la busqueda de solicitudes de viaticos. 
	 * @param context
	 * @return
	 * @throws GenericEntityException
	 * @throws ParseException
	 */
	public static void buscarViatico(Map<String, Object> context) throws GeneralException, ParseException {

		final ActionContext ac = new ActionContext(context);
		// possible fields we're searching by
		String viaticoProyectoId = ac.getParameter("viaticoProyectoId");
		String estatusId = "SOLICITADO";
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String usuario = userLogin.getString("partyId");
		
		
		Delegator delegator = ac.getDelegator();
		
		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();

		// get the list of estatus for the parametrized form ftl
		List<EntityCondition> searchConditionsMod = new FastList<EntityCondition>();
		searchConditionsMod.add(EntityCondition.makeCondition(
				Estatus.Fields.tipo.name(), EntityOperator.EQUALS, "V"));
		List<Estatus> estatus = ledgerRepository.findList(Estatus.class,
				searchConditionsMod);
		List<Map<String, Object>> estatusList = new FastList<Map<String, Object>>();
		for (Estatus s : estatus) {
			Map<String, Object> map = s.toMap();
			estatusList.add(map);
		}
		ac.put("estatusList", estatusList);
		if ("Y".equals(ac.getParameter("performFind"))) {
			// build search conditions
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();
			
			GenericValue partyRole = delegator.findByPrimaryKey("PartyRole", UtilMisc.toMap("partyId",usuario, "roleTypeId", "ADMPROY"));
			
			if(partyRole == null){
				
				List<GenericValue> responsable = delegator.findByCondition("Proyecto", EntityCondition.makeCondition("responsable", usuario), null, null);
				if (UtilValidate.isNotEmpty(responsable)){
					searchConditions.add(EntityCondition.makeCondition("responsable", EntityOperator.EQUALS,usuario));
				}else{
					searchConditions.add(EntityCondition.makeCondition("responsableTecnico", EntityOperator.EQUALS,usuario));
				}
				
		}

			if (UtilValidate.isNotEmpty(viaticoProyectoId)) {
				searchConditions.add(EntityCondition.makeCondition("viaticoProyectoId", EntityOperator.EQUALS, viaticoProyectoId));
			}
			if (UtilValidate.isNotEmpty(estatusId)) {
				searchConditions.add(EntityCondition.makeCondition("statusId",EntityOperator.EQUALS, estatusId));
			}


			//Definicion de la condicion de los where
			List<String> orderBy = UtilMisc.toList("createdStamp");
			EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
			//Se crea una lista con los elementos resultantes
	        List<GenericValue> listViaticos = delegator.findByCondition("BuscarViaticoProyectos", condiciones, null, orderBy);
	        
	        //Se crea una lista provisional para almacenar los elementos que van a ser insertdos en el mapa
	        List<Map<String,Object>> viaticosListBuilder = FastList.newInstance();
	        //Se crea el mapa provisional que se va a llenar de los objets que va a la pantalla
	        Map<String,Object> mapaViaticos = FastMap.newInstance();
	        //Se itera el mapa para colocar los elementos en el mapa de manera formateada
	        for (GenericValue Viaticos : listViaticos) {
	        	GenericValue Person = delegator.makeValue("Person");
	        	Person.setAllFields(Viaticos, false, null, null);
	        	mapaViaticos.putAll(Viaticos.getAllFields());
	        	mapaViaticos.put("viaticoProyectoId", Long.valueOf(Viaticos.getString("viaticoProyectoId")));
	        	mapaViaticos.put("personaNameDesc", PartyHelper.getCrmsfaPartyName(Person));
	        	viaticosListBuilder.add(mapaViaticos);
	        	mapaViaticos = FastMap.newInstance();
			}
	        
	        //Se crea un metodo comparator para ordenar los elementos del mapa
	        Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
				public int compare(Map<String, Object> m1, Map<String, Object> m2) {
					return ((Long) m1.get("viaticoProyectoId")).compareTo((Long) m2.get("viaticoProyectoId"));
				}
			};

			Collections.sort(viaticosListBuilder, mapComparator);
			//Se coloca el mapa con el identificador en la instancia del actioncontext
			ac.put("viaticoProyectoListBuilder", viaticosListBuilder);
		}
	}
	
	/**
	 * Se ejecuta la busqueda de solicitudes por devolver viaticos. 
	 * @param context
	 * @return
	 * @throws GenericEntityException
	 * @throws ParseException
	 */
	public static void buscarDevoViatico(Map<String, Object> context) throws GeneralException, ParseException {

		final ActionContext ac = new ActionContext(context);
		// possible fields we're searching by
		String viaticoProyectoId = ac.getParameter("viaticoProyectoId");
		String estatusId = "DEVOLVER";
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String usuario = userLogin.getString("partyId");
		
		
		Delegator delegator = ac.getDelegator();
		
		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();

		// get the list of estatus for the parametrized form ftl
		List<EntityCondition> searchConditionsMod = new FastList<EntityCondition>();
		searchConditionsMod.add(EntityCondition.makeCondition(
				Estatus.Fields.tipo.name(), EntityOperator.EQUALS, "V"));
		List<Estatus> estatus = ledgerRepository.findList(Estatus.class,
				searchConditionsMod);
		List<Map<String, Object>> estatusList = new FastList<Map<String, Object>>();
		for (Estatus s : estatus) {
			Map<String, Object> map = s.toMap();
			estatusList.add(map);
		}
		ac.put("estatusList", estatusList);
		if ("Y".equals(ac.getParameter("performFind"))) {
			// build search conditions
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();
			
			GenericValue partyRole = delegator.findByPrimaryKey("PartyRole", UtilMisc.toMap("partyId",usuario, "roleTypeId", "ADMPROY"));
			
			if(partyRole == null){
				
				List<GenericValue> responsable = delegator.findByCondition("Proyecto", EntityCondition.makeCondition("responsable", usuario), null, null);
				if (UtilValidate.isNotEmpty(responsable)){
					searchConditions.add(EntityCondition.makeCondition("responsable", EntityOperator.EQUALS,usuario));
				}else{
					searchConditions.add(EntityCondition.makeCondition("responsableTecnico", EntityOperator.EQUALS,usuario));
				}
				
			}
			
			if (UtilValidate.isNotEmpty(viaticoProyectoId)) {
				searchConditions.add(EntityCondition.makeCondition("viaticoProyectoId", EntityOperator.EQUALS, viaticoProyectoId));
			}
			if (UtilValidate.isNotEmpty(estatusId)) {
				searchConditions.add(EntityCondition.makeCondition("statusId",EntityOperator.EQUALS, estatusId));
			}


			//Definicion de la condicion de los where
			List<String> orderBy = UtilMisc.toList("createdStamp");
			EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
			//Se crea una lista con los elementos resultantes
	        List<GenericValue> listViaticos = delegator.findByCondition("BuscarViaticoProyectos", condiciones, null, orderBy);
	        
	        //Se crea una lista provisional para almacenar los elementos que van a ser insertdos en el mapa
	        List<Map<String,Object>> viaticosDevoListBuilder = FastList.newInstance();
	        //Se crea el mapa provisional que se va a llenar de los objets que va a la pantalla
	        Map<String,Object> mapaViaticos = FastMap.newInstance();
	        //Se itera el mapa para colocar los elementos en el mapa de manera formateada
	        for (GenericValue Viaticos : listViaticos) {
	        	GenericValue Person = delegator.makeValue("Person");
	        	Person.setAllFields(Viaticos, false, null, null);
	        	mapaViaticos.putAll(Viaticos.getAllFields());
	        	mapaViaticos.put("viaticoProyectoId", Long.valueOf(Viaticos.getString("viaticoProyectoId")));
	        	mapaViaticos.put("personaNameDesc", PartyHelper.getCrmsfaPartyName(Person));
	        	viaticosDevoListBuilder.add(mapaViaticos);
	        	mapaViaticos = FastMap.newInstance();
			}
	        
	        //Se crea un metodo comparator para ordenar los elementos del mapa
	        Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
				public int compare(Map<String, Object> m1, Map<String, Object> m2) {
					return ((Long) m1.get("viaticoProyectoId")).compareTo((Long) m2.get("viaticoProyectoId"));
				}
			};

			Collections.sort(viaticosDevoListBuilder, mapComparator);
			//Se coloca el mapa con el identificador en la instancia del actioncontext
			ac.put("viaticoDevuelveProyectoListBuilder", viaticosDevoListBuilder);
		}
	}
	
	
	/**
	 * Se ejecuta la busqueda de solicitudes de viaticos. 
	 * @param context
	 * @return
	 * @throws GenericEntityException
	 * @throws ParseException
	 */
	public static void buscarViaticoConsulta(Map<String, Object> context) throws GeneralException, ParseException {

		final ActionContext ac = new ActionContext(context);
		// possible fields we're searching by
		String viaticoProyectoId = ac.getParameter("viaticoProyectoId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String usuario = userLogin.getString("partyId");
		
		Delegator delegator = ac.getDelegator();
		
		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();

		// get the list of estatus for the parametrized form ftl
		List<EntityCondition> searchConditionsMod = new FastList<EntityCondition>();
		searchConditionsMod.add(EntityCondition.makeCondition(
				Estatus.Fields.tipo.name(), EntityOperator.EQUALS, "V"));
		List<Estatus> estatus = ledgerRepository.findList(Estatus.class,
				searchConditionsMod);
		List<Map<String, Object>> estatusList = new FastList<Map<String, Object>>();
		for (Estatus s : estatus) {
			Map<String, Object> map = s.toMap();
			estatusList.add(map);
		}
		ac.put("estatusList", estatusList);
		if ("Y".equals(ac.getParameter("performFind"))) {
			// build search conditions
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();
			
			GenericValue partyRole = delegator.findByPrimaryKey("PartyRole", UtilMisc.toMap("partyId",usuario, "roleTypeId", "ADMPROY"));
			
			if(partyRole == null){
				
				List<GenericValue> responsable = delegator.findByCondition("Proyecto", EntityCondition.makeCondition("responsable", usuario), null, null);
				if (UtilValidate.isNotEmpty(responsable)){
					searchConditions.add(EntityCondition.makeCondition("responsable", EntityOperator.EQUALS,usuario));
				}else{
					searchConditions.add(EntityCondition.makeCondition("responsableTecnico", EntityOperator.EQUALS,usuario));
				}
				
		}

			if (UtilValidate.isNotEmpty(viaticoProyectoId)) {
				searchConditions.add(EntityCondition.makeCondition("viaticoProyectoId", EntityOperator.EQUALS, viaticoProyectoId));
			}


			//Definicion de la condicion de los where
			List<String> orderBy = UtilMisc.toList("createdStamp");
			EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
			//Se crea una lista con los elementos resultantes
	        List<GenericValue> listViaticos = delegator.findByCondition("BuscarViaticoProyectos", condiciones, null, orderBy);
	        
	        //Se crea una lista provisional para almacenar los elementos que van a ser insertdos en el mapa
	        List<Map<String,Object>> viaticosListBuilder = FastList.newInstance();
	        //Se crea el mapa provisional que se va a llenar de los objets que va a la pantalla
	        Map<String,Object> mapaViaticos = FastMap.newInstance();
	        //Se itera el mapa para colocar los elementos en el mapa de manera formateada
	        for (GenericValue Viaticos : listViaticos) {
	        	GenericValue Person = delegator.makeValue("Person");
	        	Person.setAllFields(Viaticos, false, null, null);
	        	mapaViaticos.putAll(Viaticos.getAllFields());
	        	mapaViaticos.put("viaticoProyectoId", Long.valueOf(Viaticos.getString("viaticoProyectoId")));
	        	mapaViaticos.put("personaNameDesc", PartyHelper.getCrmsfaPartyName(Person));
	        	viaticosListBuilder.add(mapaViaticos);
	        	mapaViaticos = FastMap.newInstance();
			}
	        
	        //Se crea un metodo comparator para ordenar los elementos del mapa
	        Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
				public int compare(Map<String, Object> m1, Map<String, Object> m2) {
					return ((Long) m1.get("viaticoProyectoId")).compareTo((Long) m2.get("viaticoProyectoId"));
				}
			};

			Collections.sort(viaticosListBuilder, mapComparator);
			//Se coloca el mapa con el identificador en la instancia del actioncontext
			ac.put("viaticoConsProyListBuilder", viaticosListBuilder);
		}
	}

}
