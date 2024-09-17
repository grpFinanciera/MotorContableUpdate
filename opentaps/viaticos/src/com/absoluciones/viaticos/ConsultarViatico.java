package com.absoluciones.viaticos;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.order.finaccount.UtilClavePresupuestal;
import org.opentaps.base.entities.Enumeration;
import org.opentaps.base.entities.Estatus;
import org.opentaps.common.party.PartyHelper;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.domain.DomainsDirectory;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.action.ActionContext;
import org.opentaps.foundation.repository.RepositoryException;
import org.ofbiz.base.util.UtilDateTime;

import javolution.util.FastList;
import javolution.util.FastMap;

public class ConsultarViatico {

	private static final String MODULE = ConsultarViatico.class.getName();
	
	/**
	 * Se ejecuta la busqueda de solicitudes de viaticos. 
	 * @param context
	 * @return
	 * @throws GenericEntityException
	 * @throws ParseException
	 */
	public static void buscaViatico(Map<String, Object> context) throws GeneralException, ParseException {

		final ActionContext ac = new ActionContext(context);
		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
		// possible fields we're searching by
		String viaticoId = ac.getParameter("viaticoId");
		String filtroId = ac.getParameter("filtroId");
		String estatusId = ac.getParameter("statusId");
		String solicitante = ac.getParameter("solicitante");
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

			if (UtilValidate.isNotEmpty(viaticoId)) {
				searchConditions.add(EntityCondition.makeCondition("viaticoId", EntityOperator.EQUALS, viaticoId));
			}
			if (UtilValidate.isNotEmpty(estatusId)) {
				searchConditions.add(EntityCondition.makeCondition("statusId",EntityOperator.EQUALS, estatusId));
			}
			if (filtroId != null) {
				searchConditions.add(EntityCondition.makeCondition("personaSolicitanteId", EntityOperator.EQUALS,usuario));
			}
			if (UtilValidate.isNotEmpty(solicitante)) {
				searchConditions
						.add(EntityCondition.makeCondition("personaSolicitanteId", EntityOperator.EQUALS,solicitante));
			}
			if (UtilValidate.isNotEmpty(organizationPartyId)) {
				searchConditions.add(EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS,organizationPartyId));
			}
			
			GenericValue partyRole = delegator.findByPrimaryKey("PartyRole",UtilMisc.toMap("partyId", usuario, "roleTypeId","VIAINSTITUCION"));
			
			if (partyRole == null) {
				partyRole = delegator.findByPrimaryKey("PartyRole", UtilMisc.toMap("partyId",usuario, "roleTypeId", "VIAAREA"));
				if(partyRole != null){
					GenericValue person = delegator.findByPrimaryKey("Person",UtilMisc.toMap("partyId", usuario));
									
					searchConditions.add(EntityCondition.makeCondition("areaPartyId", EntityOperator.EQUALS,person.getString("areaId")));
				}else{
					searchConditions.add(EntityCondition.makeCondition("personaSolicitanteId", EntityOperator.EQUALS,usuario));
				}
			}


			//Definicion de la condicion de los where
			List<String> orderBy = UtilMisc.toList("createdStamp");
			EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
			//Se crea una lista con los elementos resultantes
	        List<GenericValue> listViaticos = delegator.findByCondition("BuscarViatico", condiciones, null, orderBy);
	        
	        //Se crea una lista provisional para almacenar los elementos que van a ser insertdos en el mapa
	        List<Map<String,Object>> viaticosListBuilder = FastList.newInstance();
	        //Se crea el mapa provisional que se va a llenar de los objets que va a la pantalla
	        Map<String,Object> mapaViaticos = FastMap.newInstance();
	        //Se itera el mapa para colocar los elementos en el mapa de manera formateada
	        for (GenericValue Viaticos : listViaticos) {
	        	GenericValue Person = delegator.makeValue("Person");
	        	Person.setAllFields(Viaticos, false, null, null);
	        	mapaViaticos.putAll(Viaticos.getAllFields());
	        	mapaViaticos.put("viaticoId", Long.valueOf(Viaticos.getString("viaticoId")));
	        	mapaViaticos.put("personaNameDesc", PartyHelper.getCrmsfaPartyName(Person));
	        	viaticosListBuilder.add(mapaViaticos);
	        	mapaViaticos = FastMap.newInstance();
			}
	        
	        //Se crea un metodo comparator para ordenar los elementos del mapa
	        Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
				public int compare(Map<String, Object> m1, Map<String, Object> m2) {
					return ((Long) m1.get("viaticoId")).compareTo((Long) m2.get("viaticoId"));
				}
			};

			Collections.sort(viaticosListBuilder, mapComparator);
			//Se coloca el mapa con el identificador en la instancia del actioncontext
			ac.put("viaticoListBuilder", viaticosListBuilder);
		}
	}
	
	public static void viaticosPendientes(Map<String, Object> context) throws GeneralException, ParseException {
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
			searchConditions.add(persona.equalsIgnoreCase("autorizador") ? EntityCondition.makeCondition("personParentId",EntityOperator.EQUALS, solicitante) : EntityCondition.makeCondition("personaSolicitanteId",EntityOperator.EQUALS, solicitante));
		}
		
		searchConditions.add(status.equalsIgnoreCase("PENDIENTE")?EntityCondition.makeCondition("statusId",EntityOperator.EQUALS, status):EntityCondition.makeCondition("statusVi",EntityOperator.EQUALS, status));
		searchConditions.add(EntityCondition.makeCondition("tipoWorkFlowId",EntityOperator.IN, UtilMisc.toList("VIATICOS","VIATICOSVI","VIATICOSGI")));
		
		
		//Variable para definir el campo por el que se va a ordenar
		List<String> fieldsToSelect = UtilMisc.toList("viaticoId", "descripcion", "personaSolicitanteId", "firstName", "lastName", "createdStamp");
		List<String> orderBy = UtilMisc.toList("createdStamp");
		//Definicion de la condicion de los where
		EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
		//Se crea una lista con los elementos resultantes, apuntando a la vista del entity%.xml
        List<GenericValue> listViaticosPendientes = delegator.findByCondition("ViaticosPendientes", condiciones, fieldsToSelect, orderBy);
        
        //Se crea una lista provisional para almacenar los elementos que van a ser insertdos en el mapa
        List<Map<String,Object>> viaticoPendienteListBuilder = FastList.newInstance();
        //Se crea el mapa provisional que se va a llenar de los objets que va a la pantalla
        Map<String,Object> mapaViaticosPendientes = FastMap.newInstance();
        //Se itera el mapa para colocar los elementos en el mapa de manera formateada
        for (GenericValue Viaticos : listViaticosPendientes) {
        	mapaViaticosPendientes.putAll(Viaticos.getAllFields());
        	mapaViaticosPendientes.put("viaticoId", Long.valueOf(Viaticos.getString("viaticoId")));
        	viaticoPendienteListBuilder.add(mapaViaticosPendientes);
        	mapaViaticosPendientes = FastMap.newInstance();
		}
        
        //Se crea un metodo comparator para ordenar los elementos del mapa
        Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
			public int compare(Map<String, Object> m1, Map<String, Object> m2) {
				return ((Long) m1.get("viaticoId")).compareTo((Long) m2.get("viaticoId"));
			}
		};

		Collections.sort(viaticoPendienteListBuilder, mapComparator);
		//Se coloca el mapa con el identificador en la instancia del actioncontext
		ac.put("viaticoPendienteListBuilder", viaticoPendienteListBuilder);		
	}

	public static void consultarSaldoViatico(Map<String, Object> context) throws GeneralException, ParseException {
		final ActionContext ac = new ActionContext(context);
		//String solicitante = userLogin.getString("partyId");
		
		Delegator delegator = ac.getDelegator();
		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();
		
		// get the list of periodos for the parametrized form ftl
		List<EntityCondition> searchConditionsMod = new FastList<EntityCondition>();
				searchConditionsMod.add(EntityCondition.makeCondition(Enumeration.Fields.enumTypeId.name(), EntityOperator.EQUALS, "CL_CICLO"));
				List<Enumeration> enumerations = ledgerRepository.findList(Enumeration.class,searchConditionsMod);
		List<Map<String, Object>> periodosList = new FastList<Map<String, Object>>();
		for (Enumeration p : enumerations) {
			Map<String, Object> map = p.toMap();
			periodosList.add(map);
		}
		ac.put("periodosList", periodosList);

		// possible fields we're searching by
		String solicitanteId = ac.getParameter("solicitanteId");
		String periodo = ac.getParameter("enumCode");
	
		//get the list of estatus for the parametrized form ftl
		if ("Y".equals(ac.getParameter("performFind"))) {
			// build search conditions
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();
			
			searchConditions.add(EntityCondition.makeCondition("tipo", EntityOperator.EQUALS, "V"));

			if (UtilValidate.isNotEmpty(solicitanteId)) {
					searchConditions.add(EntityCondition.makeCondition("catalogoId", EntityOperator.EQUALS, solicitanteId));
				}
				if (UtilValidate.isNotEmpty(periodo)) {
					Calendar date = Calendar.getInstance();
					date.set(Calendar.DAY_OF_MONTH, 1);
					date.set(Calendar.MONTH, 0);
					date.set(Calendar.HOUR, 0);
					date.set(Calendar.MINUTE, 0);
					date.set(Calendar.SECOND, 0);
					date.set(Calendar.MILLISECOND, 0);
					date.set(Calendar.AM_PM, Calendar.AM);
					date.set(Calendar.YEAR, Integer.parseInt(periodo));					
					searchConditions.add(EntityCondition.makeCondition("periodo",EntityOperator.EQUALS, new Timestamp(date.getTimeInMillis())));
				}
				
				
				//Variable para definir el campo por el que se va a ordenar
				List<String> orderBy = UtilMisc.toList("catalogoId");
				//Definicion de la condicion de los where
				EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
				//Se crea una lista con los elementos resultantes, apuntando a la vista del entity%.xml
		        List<GenericValue> listConsultaSaldoViaticos = delegator.findByCondition("ConsultarSaldoViatico", condiciones, null, orderBy);
		        
		        //Se crea una lista provisional para almacenar los elementos que van a ser insertdos en el mapa
		        List<Map<String,Object>> listConsultarSaldoViaticoListBuilder = FastList.newInstance();
		        //Se crea el mapa provisional que se va a llenar de los objets que va a la pantalla
		        Map<String,Object> mapaConsultaViaticos = FastMap.newInstance();
		        //Se itera el mapa para colocar los elementos en el mapa de manera formateada
		        for (GenericValue Viaticos : listConsultaSaldoViaticos) {
		        	mapaConsultaViaticos.putAll(Viaticos.getAllFields());
		        	mapaConsultaViaticos.put("personaNameDesc", PartyHelper.getCrmsfaPartyName(Viaticos));
		        	listConsultarSaldoViaticoListBuilder.add(mapaConsultaViaticos);
		        	mapaConsultaViaticos = FastMap.newInstance();
				}
		        
		        //Se crea un metodo comparator para ordenar los elementos del mapa
		        Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
					public int compare(Map<String, Object> m1, Map<String, Object> m2) {
						return ( (String) m1.get("personaNameDesc")).compareTo((String) m2.get("personaNameDesc"));
					}
				};

				Collections.sort(listConsultarSaldoViaticoListBuilder, mapComparator);
				//Se coloca el mapa con el identificador en la instancia del actioncontext
				ac.put("listConsultarSaldoViaticoListBuilder", listConsultarSaldoViaticoListBuilder);
				
			}
	}
	public static String obtenerArea(Delegator delegator, String user) throws GenericEntityException {
		GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", user));
		GenericValue person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", userLogin.getString("partyId")));
		return person.getString("areaId");
	}

	public static String obtenPadre(Delegator delegator, String organizationId) throws RepositoryException, GenericEntityException {
		String parentId = organizationId;
		do {
			GenericValue generic = obtenPartyGroup(delegator, organizationId);

			organizationId = generic.getString("Parent_id");
			if (organizationId != null)
				parentId = generic.getString("Parent_id");
		} while (organizationId != null);
		return parentId;
	}

	public static GenericValue obtenPartyGroup(Delegator delegator, String party) throws GenericEntityException {
		GenericValue generic = delegator.findByPrimaryKey("PartyGroup",UtilMisc.toMap("partyId", party));
		return generic;
	}
	
	public static String obtenMontoComprobado(Delegator delegator, GenericValue detalleViatico) throws GenericEntityException{
		GenericValue viatico = delegator.findByPrimaryKey("Viatico", UtilMisc.toMap("viaticoId",detalleViatico.getString("viaticoId")));
		
		Timestamp fechaInicial = viatico.getTimestamp("fechaInicial");
		Calendar calFecInicial = Calendar.getInstance();
		calFecInicial.setTimeInMillis(fechaInicial.getTime());
		
		//Buscamos la posicion del COG.
		int indiceCog = UtilClavePresupuestal.indiceCog(viatico.getString("organizationPartyId"), delegator, String.valueOf(calFecInicial.get(Calendar.YEAR)));
		//Obtenemos el enumId.
		String cog = detalleViatico.getString("acctgTagEnumId"+indiceCog);
		
		//Buscamos en la tabla el monto correspondiente
		List <GenericValue> cogs = delegator.findByAnd("CogViatico", UtilMisc.toMap("acctgTagEnumId", cog));
		
		if(cogs.isEmpty()){
			Debug.logWarning("No se encuentra asociado el Cog con un monto", MODULE);
			return "";
		}
		
		//Obtenemos la bandera
		return cogs.get(0).getString("diarioTransporteFlag");
	}

	
	public static void consultarUsuarios(Map<String, Object> context) throws GeneralException, ParseException {
		final ActionContext ac = new ActionContext(context);		
		Delegator delegator = ac.getDelegator();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		GenericValue configDias = delegator.findByPrimaryKey("ConfiguracionViatico", UtilMisc.toMap("configViaticoId","DIAS_COMPROBACION"));
		GenericValue configSolicitudes = delegator.findByPrimaryKey("ConfiguracionViatico", UtilMisc.toMap("configViaticoId","SOLICITUDES_COMPROBACION"));
		
		int solicitudes = Integer.valueOf(configSolicitudes.getString("valor"));
		int dias = Integer.valueOf(configDias.getString("valor"));
		
		List<GenericValue> viaticos = delegator.findByCondition("Viatico", EntityCondition.makeCondition("statusId",EntityOperator.EQUALS,"PAGADA_V"), null, UtilMisc.toList("personaSolicitanteId"));
		
		FastMap<String, List<String>> usuariosSolicitudes = new FastMap<String, List<String>>();
		for (GenericValue viatico : viaticos){
			List<String> usuarios = new ArrayList<String>();
			if(UtilValidate.isEmpty(usuariosSolicitudes.get(viatico.getString("personaSolicitanteId")))){
				usuarios.add(viatico.getString("viaticoId"));
				usuariosSolicitudes.put(viatico.getString("personaSolicitanteId"), usuarios);
			}else{
				usuarios = usuariosSolicitudes.get(viatico.getString("personaSolicitanteId"));
				usuarios.add(viatico.getString("viaticoId"));
				usuariosSolicitudes.put(viatico.getString("personaSolicitanteId"), usuarios);
			}
			
		}
		
		for(GenericValue viatico : viaticos){
			
			if(UtilValidate.isNotEmpty(usuariosSolicitudes.get(viatico.getString("personaSolicitanteId")))){
				
				if(usuariosSolicitudes.get(viatico.getString("personaSolicitanteId")).size()<solicitudes){
					
					usuariosSolicitudes.remove(viatico.getString("personaSolicitanteId"));
					
				}
				
			}
			
		}
		
		for (GenericValue viatico : viaticos){
			
			Calendar calendar = Calendar.getInstance();
			Calendar calendar2 = Calendar.getInstance();
			calendar.setTime(viatico.getTimestamp("fechaFinal"));
			calendar2.setTime(UtilDateTime.nowTimestamp());
			calendar.add(Calendar.DAY_OF_YEAR, dias);
			
			int comparacion = calendar.compareTo(calendar2);
			
			if(comparacion<=0){

				List<String> usuarios = new ArrayList<String>();
				
				if (UtilValidate.isEmpty(usuariosSolicitudes.get(viatico.getString("personaSolicitanteId")))){
					usuarios.add(viatico.getString("viaticoId"));
					usuariosSolicitudes.put(viatico.getString("personaSolicitanteId"), usuarios);
				}else{
					usuarios = usuariosSolicitudes.get(viatico.getString("personaSolicitanteId"));
					if(!usuarios.contains(viatico.getString("viaticoId"))){
						usuarios.add(viatico.getString("viaticoId"));
						usuariosSolicitudes.put(viatico.getString("personaSolicitanteId"), usuarios);
					}
					
				}
			}
			
		}
		
		List<GenericValue> viaticosPerson = new ArrayList<GenericValue>();
		
		for (java.util.Map.Entry<String, List<String>> entry : usuariosSolicitudes.entrySet()) {
			for(String id:entry.getValue()){
				GenericValue viatico = delegator.findByPrimaryKey("Viatico",UtilMisc.toMap("viaticoId", id));
				GenericValue usuario = delegator.findByPrimaryKey("UsuarioInhViatico", UtilMisc.toMap("personaSolicitanteId", viatico.getString("personaSolicitanteId")));
				if(UtilValidate.isEmpty(usuario)){
					viaticosPerson.add(viatico);
				}else{
					if(usuario.getString("estatus").equals("ACTIVO")){
						viaticosPerson.add(viatico);
					}
				}
			}
		}
		
		
        //Se crea una lista provisional para almacenar los elementos que van a ser insertdos en el mapa
        List<Map<String,Object>> usuarioInhListBuilder = FastList.newInstance();
        //Se crea el mapa provisional que se va a llenar de los objets que va a la pantalla
        Map<String,Object> mapaViaticosPendientes = FastMap.newInstance();
        //Se itera el mapa para colocar los elementos en el mapa de manera formateada
        for (GenericValue Viaticos : viaticosPerson) {
        	GenericValue nombreUsuario = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", Viaticos.getString("personaSolicitanteId")));
        	mapaViaticosPendientes.putAll(Viaticos.getAllFields());
        	mapaViaticosPendientes.put("viaticoId", Long.valueOf(Viaticos.getString("viaticoId")));
        	mapaViaticosPendientes.put("usuario", getNombre(nombreUsuario));
        	mapaViaticosPendientes.put("userLoginId", userLogin.getString("partyId"));
        	usuarioInhListBuilder.add(mapaViaticosPendientes);
        	mapaViaticosPendientes = FastMap.newInstance();
		}
     
		ac.put("usuarioInhListBuilder", usuarioInhListBuilder);		
	}
	
	public static String getNombre(GenericValue nombre){
		
		String nombreUsuario="";
		
		if(UtilValidate.isNotEmpty(nombre.getString("firstName"))){
			
			nombreUsuario=nombre.getString("firstName");	
			
		}
		
		if(UtilValidate.isNotEmpty(nombre.getString("middleName"))){
			
			nombreUsuario=nombreUsuario +" " +nombre.getString("middleName");
			
		}
		
		if(UtilValidate.isNotEmpty(nombre.getString("lastName"))){
			
			nombreUsuario= nombreUsuario +" "+nombre.getString("lastName");
			
		}
		
		return nombreUsuario;
	}
}
