package org.opentaps.purchasing.order;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpSession;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilNumber;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.order.shoppingcart.ShoppingCart;
import org.ofbiz.order.shoppingcart.ShoppingCartItem;
import org.opentaps.base.entities.BuscarClaveDetalleRequisicon;
import org.opentaps.base.entities.BuscarProductosPendientes;
import org.opentaps.base.entities.DetalleRequisicion;
import org.opentaps.base.entities.Estatus;
import org.opentaps.base.entities.SolicitudesPagoAntiPendientes;
import org.opentaps.base.entities.SolicitudesPendientes;
import org.opentaps.common.builder.EntityListBuilder;
import org.opentaps.common.builder.ListBuilderException;
import org.opentaps.common.builder.PageBuilder;
import org.opentaps.common.order.shoppingcart.OpentapsShoppingCart;
import org.opentaps.common.util.UtilAccountingTags;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.domain.DomainsDirectory;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.action.ActionContext;
import org.opentaps.foundation.repository.RepositoryException;

import com.opensourcestrategies.crmsfa.party.PartyHelper;

public class ConsultarRequisicion {

	private static final String MODULE = ConsultarRequisicion.class.getName();

	/**
	 * Metodo para consultar la requisicion
	 * @param context
	 */
	public static void buscaRequisicion(Map<String, Object> context) {
		
		try {

		final ActionContext ac = new ActionContext(context);
		final Locale locale = ac.getLocale();
		TimeZone timeZone = ac.getTimeZone();
		String dateTimeFormat = UtilDateTime.getDateFormat(locale);
		
		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
		Delegator delegator = ac.getDelegator();

		// possible fields we're searching by
		String requisicionId = ac.getParameter("requisicionId");
		String estatusId = ac.getParameter("statusId");
		String productId = ac.getParameter("productId");
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
		String usuario = null, area = null, allReq = null;
		
		usuario = userLogin.getString("partyId");
		Debug.logInfo("usuario: " + userLogin,MODULE);
		
		EntityCondition conditions = EntityCondition.makeCondition(
				EntityOperator.AND, EntityCondition.makeCondition("partyId",
						EntityOperator.EQUALS, usuario));
		
		List<GenericValue> partyRole = delegator.findByCondition(
				"PartyRole", conditions, null, null);
		
		if(!partyRole.isEmpty()){
			for(int i=0; i<partyRole.size();i++){
				String rol = partyRole.get(i).getString("roleTypeId");
				if(rol.equals("REQAREA")){
					GenericValue person = delegator.findByPrimaryKey("Person",
			                UtilMisc.toMap("partyId", usuario));
					area = person.getString("areaId");
				}else if(rol.equals("REQINSTITUCION")){
					allReq = "verTodas";
				}
			}
		}

		if(allReq != null){
			area = null;
			usuario = null;
		}else if (area != null){
			usuario = null;
		}

		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
				.getLedgerRepository();

		// get the list of estatus for the parametrized form ftl
		List<EntityCondition> searchConditionsMod = new FastList<EntityCondition>();
		searchConditionsMod.add(EntityCondition.makeCondition(
				Estatus.Fields.tipo.name(), EntityOperator.EQUALS, "P"));
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
			
			if (UtilValidate.isNotEmpty(fechaContable_inicial)) {
				searchConditions.add(EntityCondition.makeCondition(
						"fechaContable", EntityOperator.GREATER_THAN_EQUAL_TO, fechaContable_inicial));
				searchConditions.add(EntityCondition.makeCondition(
						"fechaContable", EntityOperator.LESS_THAN_EQUAL_TO, fechaContable_final));
			}

			if (UtilValidate.isNotEmpty(requisicionId)) {
				searchConditions.add(EntityCondition.makeCondition("requisicionId", requisicionId));
			}
			if (UtilValidate.isNotEmpty(estatusId)) {
				searchConditions.add(EntityCondition.makeCondition("statusId", estatusId));
			}
			if (UtilValidate.isNotEmpty(productId)) {
				searchConditions.add(EntityCondition.makeCondition("productId", productId));
			}
			if (UtilValidate.isNotEmpty(usuario)) {
				searchConditions.add(EntityCondition.makeCondition("personaSolicitanteId", usuario));
			}
			if (UtilValidate.isNotEmpty(area)) {
				searchConditions.add(EntityCondition.makeCondition("areaPartyId", area));
			}
			if (UtilValidate.isNotEmpty(organizationPartyId)) {
				searchConditions.add(EntityCondition.makeCondition("organizationPartyId", organizationPartyId));
			}

			
			// fields to select
			
			EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
			EntityCondition condicionAutorizador = EntityCondition.makeCondition("tipoWorkFlowId", EntityOperator.EQUALS, "REQUISICION");
			List<String> orderBy = UtilMisc.toList("requisicionId");
			List<String> fieldsToSelect = UtilMisc.toList("requisicionId", "fechaContable", "descripcion", "descripcionStatus", 
					"fechaAutorizada", "montoTotal", "tipoMoneda", "firstName", "lastName", "partyId");
			
			List<GenericValue> listUltimoAutorizador = delegator.findByCondition("BuscarUltimoAutorizador", condicionAutorizador, null, null);
			
			Map<String,Object> mapaUltimoAutorizador = FastMap.newInstance();
			
			GenericValue Person = delegator.makeValue("Person");
			for (GenericValue autorizador : listUltimoAutorizador) {
				Person.setAllFields(autorizador, false, null, null);
				mapaUltimoAutorizador.put(autorizador.getString("origenId"), PartyHelper.getCrmsfaPartyName(Person));
				Person = delegator.makeValue("Person");
			}
			
			Debug.logInfo(
					"search conditions : "
							+ EntityCondition.makeCondition(searchConditions,
									EntityOperator.AND).toString(), MODULE);
			
			List<GenericValue> listRequisicion = delegator.findByCondition("BuscarRequisicionConProduct", condiciones, fieldsToSelect, orderBy);

			List<Map<String,Object>> listRequisicionBuilder = FastList.newInstance();
			
			Map<String,Object> mapaRequisicion = FastMap.newInstance();
			
			Person = delegator.makeValue("Person");
			for (GenericValue requisicion : listRequisicion) {
				mapaRequisicion.putAll(requisicion.getAllFields());				
				mapaRequisicion.put("requisicionId", Long.valueOf(requisicion.getString("requisicionId")));
				Person.setAllFields(requisicion, false, null, null);
				mapaRequisicion.put("personaNameDesc", PartyHelper.getCrmsfaPartyName(Person));
				mapaRequisicion.put("autorizador", mapaUltimoAutorizador.get(requisicion.getString("requisicionId")));
				listRequisicionBuilder.add(mapaRequisicion);
				mapaRequisicion = FastMap.newInstance();
				Person = delegator.makeValue("Person");
			}
			
			Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
				public int compare(Map<String, Object> m1, Map<String, Object> m2) {
					return ((Long) m1.get("requisicionId")).compareTo((Long) m2.get("requisicionId"));
				}
			};

			Collections.sort(listRequisicionBuilder, mapComparator);			
				ac.put("requisicionListBuilder", listRequisicionBuilder);

		}
		
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Metodo que consulta las requisiciones pendientes
	 * @param context
	 */
	public static void requisicionesPendientes(Map<String, Object> context) {
	
		try {
			
		final ActionContext ac = new ActionContext(context);
		final Delegator delegator = ac.getDelegator();
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
				EntityOperator.EQUALS, "REQUISICION"));

		// fields to select
		List<String> orderBy = UtilMisc.toList("requisicionId");
		Debug.log("Search conditions : " + searchConditions);
		EntityListBuilder solicitudPendienteListBuilder = null;
		PageBuilder<SolicitudesPendientes> pageBuilderSolicitudesPendientes = null;
		
		////
		List<SolicitudesPendientes> solicitudes = ledgerRepository.findList(SolicitudesPendientes.class, EntityCondition.makeCondition(
				searchConditions, EntityOperator.AND));
		Debug.log("Solicitudes:");
		for(SolicitudesPendientes sol : solicitudes){
			
			Debug.log(""+sol);
		}
		////

		solicitudPendienteListBuilder = new EntityListBuilder(ledgerRepository,
				SolicitudesPendientes.class, EntityCondition.makeCondition(
						searchConditions, EntityOperator.AND), null, orderBy);
		pageBuilderSolicitudesPendientes = new PageBuilder<SolicitudesPendientes>() {
			public List<Map<String, Object>> build(
					List<SolicitudesPendientes> page) throws Exception {
				List<Map<String, Object>> newPage = FastList.newInstance();
				GenericValue requisicionGeneric = null;
				for (SolicitudesPendientes reqId : page) {
					Map<String, Object> newRow = FastMap.newInstance();
					newRow.putAll(reqId.toMap());
					requisicionGeneric = delegator.findByPrimaryKey("Requisicion", UtilMisc.toMap("requisicionId", reqId.get("requisicionId")));
					newRow.put("tipoMoneda", requisicionGeneric.get("tipoMoneda"));
					newPage.add(newRow);
				}
				return newPage;
			}
		};
		solicitudPendienteListBuilder
				.setPageBuilder(pageBuilderSolicitudesPendientes);
		ac.put("solicitudPendienteListBuilder", solicitudPendienteListBuilder);
		
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (ListBuilderException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Busqueda de productos pendientes por comprar
	 * @param context
	 */
	public static void productosPendites(Map<String, Object> context) {
		
		try {
	
			final ActionContext ac = new ActionContext(context);
			String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
			
			HttpSession session = ac.getRequest().getSession(true);
	
	        // if one already exists, return it
	        ShoppingCart cart = (ShoppingCart) session.getAttribute("shoppingCart");
	        OpentapsShoppingCart cart2 = null;
	        if (cart != null && cart instanceof OpentapsShoppingCart) {
	            cart2 = (OpentapsShoppingCart) cart;
	        } else if (cart != null){
	            OpentapsShoppingCart opentapsCart = new OpentapsShoppingCart(cart);
	            session.setAttribute("shoppingCart", opentapsCart);
	            cart2 = opentapsCart;
	        }
	
			// possible fields we're searching by
			GenericValue userLogin = (GenericValue) context.get("userLogin");
			
			Delegator delegator = ac.getDelegator();
			DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
			final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
					.getLedgerRepository();
			Locale locale = ac.getLocale();
			final TimeZone timeZone = ac.getTimeZone();
			
			String areaId = null;
			String statusId = ac.getParameter("statusId");
			String requisicionId = ac.getParameter("requisicionId");
			String productId = ac.getParameter("productId");
			String solicitanteId = ac.getParameter("solicitanteId");
			String cicloId = UtilCommon.getCicloId(ac.getRequest());
			String dateTimeFormat = UtilDateTime.getDateFormat(locale);
			String fechaInicio = ac.getParameter("fechaInicio");
			String fechaFin = ac.getParameter("fechaFin");

			Debug.logInfo("campos de busqueda: " + statusId + " y " + productId,MODULE);
	
			// get the list of estatus for the parametrized form ftl
			List<EntityCondition> searchConditionsMod = new FastList<EntityCondition>();
			searchConditionsMod.add(EntityCondition.makeCondition(
					Estatus.Fields.tipo.name(), EntityOperator.EQUALS, "P"));
			List<Estatus> estatus = ledgerRepository.findList(Estatus.class,
					searchConditionsMod);
			List<Map<String, Object>> estatusList = new FastList<Map<String, Object>>();
			for (Estatus s : estatus) {
				Map<String, Object> map = s.toMap();
				estatusList.add(map);
			}
			ac.put("estatusList", estatusList);
	
			areaId = obtenerArea(delegator, userLogin.getString("userLoginId"));
			// build search conditions
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();
	
			if (UtilValidate.isNotEmpty(statusId)) {
				searchConditions.add(EntityCondition.makeCondition(BuscarProductosPendientes.Fields.estatusProducto.name(),statusId));
			} else {
				searchConditions.add(EntityCondition.makeCondition(
						BuscarProductosPendientes.Fields.estatusProducto.name(),"PENDIENTE_P"));
			}
			if (UtilValidate.isNotEmpty(requisicionId)) {
				searchConditions.add(EntityCondition.makeCondition(
						BuscarProductosPendientes.Fields.requisicionId.name(), requisicionId));
			}
			if (UtilValidate.isNotEmpty(solicitanteId)) {
				searchConditions.add(EntityCondition.makeCondition(
						BuscarProductosPendientes.Fields.personaSolicitanteId.name(), solicitanteId));
			}
			if (UtilValidate.isNotEmpty(productId)) {
				searchConditions.add(EntityCondition.makeCondition(
						BuscarProductosPendientes.Fields.productId.name(), productId));
			}
			if (UtilValidate.isNotEmpty(organizationPartyId)
					&& !organizationPartyId.equals(obtenPadre(delegator,
							organizationPartyId))) {
				searchConditions.add(EntityCondition
								.makeCondition(BuscarProductosPendientes.Fields.organizationPartyId.name(), organizationPartyId));
			}
			if (areaId != null && !areaId.equals(obtenPadre(delegator, areaId))) {
				searchConditions.add(EntityCondition.makeCondition(
						BuscarProductosPendientes.Fields.areaId.name(), areaId));
			}
			if (UtilValidate.isNotEmpty(fechaInicio)) {
				Timestamp fechaInicioTS = UtilDateTime.stringToTimeStamp(fechaInicio, dateTimeFormat, timeZone, locale);
		        searchConditions.add(EntityCondition.makeCondition(BuscarProductosPendientes.Fields.fechaContable.name(), EntityOperator.GREATER_THAN_EQUAL_TO, fechaInicioTS));
		    }
		    if (UtilValidate.isNotEmpty(fechaFin)) {
		    	Timestamp fechaFinTS = UtilDateTime.stringToTimeStamp(fechaFin, dateTimeFormat, timeZone, locale);
		        	searchConditions.add(EntityCondition.makeCondition(BuscarProductosPendientes.Fields.fechaContable.name(), EntityOperator.LESS_THAN_EQUAL_TO, fechaFinTS));
		    }
			
			searchConditions.add(EntityCondition.makeCondition(
					BuscarProductosPendientes.Fields.tipoWorkFlowId.name(), "REQUISICION"));
	
			Debug.logInfo("condiciones: " + searchConditions,MODULE);
	
			List<GenericValue> listProductosPendientes = delegator.findByCondition("BuscarProductosPendientes", 
					EntityCondition.makeCondition(searchConditions), null, UtilMisc.toList("createdStamp"));
			
			List<Map<String, Object>> listProductos = new FastList<Map<String, Object>>();
			
			com.ibm.icu.util.Calendar calendarFecha = null;
			LinkedHashSet<String> listCiclos =  new LinkedHashSet<String>(); 
			
			GenericValue Person = delegator.makeValue("Person");
			for (GenericValue ProductoPendiente : listProductosPendientes) {
				
				//Se obtiene la fecha de customTime, si existe
				if(UtilValidate.isNotEmpty(ProductoPendiente.getDate("fromDate"))){
					calendarFecha = UtilDateTime.toCalendar(UtilDateTime.toTimestamp(ProductoPendiente.getDate("fromDate"))); 
				} else {
					calendarFecha = UtilDateTime.getCalendarInstance(timeZone, locale);
				}
				
				listCiclos.add(String.valueOf(calendarFecha.get(Calendar.YEAR)));
				
				Person.setAllFields(ProductoPendiente, false, null, null);
				Map<String, Object> map = ProductoPendiente.getAllFields();
				map.put("nombreSolicitante",PartyHelper.getCrmsfaPartyName(Person));
				Person = delegator.makeValue("Person");
				map.put("mesId",UtilFormatOut.formatPaddedNumber(calendarFecha.get(Calendar.MONTH)+1, 2));
				map.put("ciclo",String.valueOf(calendarFecha.get(Calendar.YEAR)));
				listProductos.add(map);
				
				if(cart2 != null){
					List<ShoppingCartItem> listShoppingCarts = cart2.findAllCartItems(ProductoPendiente.getString("productId"));
					for (ShoppingCartItem shoppingCartItem : listShoppingCarts) {
						if(UtilValidate.isNotEmpty(shoppingCartItem.getRequisicionId()) &&
								UtilValidate.isNotEmpty(shoppingCartItem.getDetalleRequisicionId()) && 
									UtilValidate.isNotEmpty(ProductoPendiente.getString("requisicionId")) &&
										UtilValidate.isNotEmpty(ProductoPendiente.getString("detalleRequisicionId"))){
				            if(UtilValidate.areEqual(shoppingCartItem.getRequisicionId(), ProductoPendiente.getString("requisicionId")) && 
				            		UtilValidate.areEqual(shoppingCartItem.getDetalleRequisicionId(), ProductoPendiente.getString("detalleRequisicionId"))){
				            	listProductos.remove(map);
				            	Debug.logInfo("Se elimin\u00f3 el producto ["+ProductoPendiente.getString("productId")+"] "
				            			+ "de la requisci\u00f3n con identificador ["+ProductoPendiente.getString("detalleRequisicionId")+"]", MODULE);
				            }
						}
					}
				}
			}
			
			Map<String, List<GenericValue>> mapMesesXCiclo = getMapaMesesXCiclo(listCiclos, delegator, timeZone, locale);
			
			guardaCustomTimePeriodId(listProductos, mapMesesXCiclo);
			
			ac.put("mapMesesXCiclo", mapMesesXCiclo);
			ac.put("productosList", listProductos);
			ac.put("tagTypes", UtilAccountingTags
					.getClassificationTagsForOrganization(organizationPartyId,cicloId,UtilAccountingTags.EGRESO_TAG, delegator));
			
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Guarda customTimePeriodId en el mapa de la busqueda
	 * @param listProductos
	 * @param mapMesesXCiclo
	 */
	public static void guardaCustomTimePeriodId(List<Map<String, Object>> listProductos, Map<String, List<GenericValue>> mapMesesXCiclo){
		for (Map<String, Object> ProductoPendiente : listProductos) {
			if(UtilValidate.isEmpty(ProductoPendiente.get("customTimePeriodId"))){
				java.util.Date fechaCompara = UtilDateTime.toDate(
						Integer.valueOf((String) ProductoPendiente.get("mesId")), 1, 
							Integer.valueOf((String) ProductoPendiente.get("ciclo")), 0, 0, 0);
				
				List<GenericValue> listCustomTimePeriod = mapMesesXCiclo.get(ProductoPendiente.get("ciclo"));
				
				if(UtilValidate.isNotEmpty(listCustomTimePeriod)){
					for (GenericValue CustomTimePeriod : listCustomTimePeriod) {
						if(CustomTimePeriod.getDate("fromDate").compareTo(fechaCompara) <= 0 &&
								CustomTimePeriod.getDate("thruDate").compareTo(fechaCompara) > 0){
							ProductoPendiente.put("customTimePeriodId", CustomTimePeriod.getString("customTimePeriodId"));
							break;
						}
					}
				}
			}
		}
	}
	
	/**
	 * Obtiene los datos de detalle requisicion para mostrar en pantalla
	 * @param context
	 */
	public static void obtenerDetalleRequisicion(Map<String, Object> context) {

		try
		{
		
			final ActionContext ac = new ActionContext(context);
			String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
			String cicloId = UtilCommon.getCicloId(ac.getRequest());
			TimeZone timeZone = UtilHttp.getTimeZone(ac.getRequest());
			Locale locale = UtilHttp.getLocale(ac.getRequest());
	
			// possible fields we're searching by
			Delegator delegator = ac.getDelegator();
			DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
			final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
					.getLedgerRepository();
			
			String requisicionId = ac.getParameter("requisicionId");
				
			// build search conditions
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();
			
			if (UtilValidate.isNotEmpty(requisicionId)) {
				searchConditions.add(EntityCondition.makeCondition(
						DetalleRequisicion.Fields.requisicionId.name(),
						EntityOperator.EQUALS, requisicionId));
			}		
			
			GenericValue Requisicion = delegator.findByPrimaryKey("Requisicion", UtilMisc.toMap("requisicionId",requisicionId));
	
			Debug.logInfo("condiciones: " + searchConditions,MODULE);
	
			List<BuscarClaveDetalleRequisicon> listDetalleRequisicion = ledgerRepository
					.findList(BuscarClaveDetalleRequisicon.class, searchConditions);
			
			LinkedHashSet<String> listClavePresupuestal =  new LinkedHashSet<String>(); 
			for (BuscarClaveDetalleRequisicon DetalleRequisicion : listDetalleRequisicion) {
				listClavePresupuestal.add(DetalleRequisicion.getClavePresupuestal());
			}
			List<EntityCondition> condicionesPresupuesto = new FastList<EntityCondition>();
			condicionesPresupuesto.add(EntityCondition.makeCondition("momentoId","DISPONIBLE"));
			condicionesPresupuesto.add(EntityCondition.makeCondition("clavePresupuestal", EntityOperator.IN , listClavePresupuestal));
			List<GenericValue> listControlPresupuestal = delegator.findByCondition("ControlPresupuestal",EntityCondition.makeCondition(condicionesPresupuesto,EntityOperator.AND),null,null);
			
			Map<String,BigDecimal> mapaPresupuestoMes = FastMap.newInstance();
			
			String mes = new String();
			String clave = new String();
			String ciclo = new String();
			for (GenericValue ControlPresupuestal : listControlPresupuestal) {
				mes = ControlPresupuestal.getString("mesId");
				clave = ControlPresupuestal.getString("clavePresupuestal").trim();
				mapaPresupuestoMes.put(mes+clave, ControlPresupuestal.getBigDecimal("monto"));
			}
			
			LinkedHashSet<String> listCiclos =  new LinkedHashSet<String>(); 
			
			List<Map<String, Object>> detalleRequisicion = FastList.newInstance();
	
			Timestamp fecha = new Timestamp(System.currentTimeMillis());
			for (BuscarClaveDetalleRequisicon DetalleRequisicion : listDetalleRequisicion) {
				if(UtilValidate.isNotEmpty(DetalleRequisicion.getFromDate())){
					fecha = UtilDateTime.toTimestamp(DetalleRequisicion.getFromDate());
					mes = UtilFormatOut.formatPaddedNumber(UtilDateTime.getMonth(fecha, timeZone, locale)+1,2);
				} else {
					fecha = Requisicion.getTimestamp("fechaContable");
					mes = UtilFormatOut.formatPaddedNumber(UtilDateTime.getMonth(fecha, timeZone, locale)+1,2);
				}
				ciclo = String.valueOf(UtilDateTime.getYear(fecha, timeZone, locale));
				
				Map<String, Object> map = DetalleRequisicion.toMap();
				map.put("mesId",mes);
				map.put("ciclo",ciclo);
				listCiclos.add(ciclo);
				map.put("disponible", UtilNumber.getBigDecimal(mapaPresupuestoMes.get(mes+DetalleRequisicion.getClavePresupuestal().trim())));
				map.put("cantidadDisponibleA", DetalleRequisicion.getAvailableToPromiseTotal());
				detalleRequisicion.add(map);
			}
			
			ac.put("mapMesesXCiclo", getMapaMesesXCiclo(listCiclos, delegator, timeZone, locale));
			ac.put("mapMesesXCiclo2", getMapaMesesXCiclo(listCiclos, delegator, timeZone, locale));
			ac.put("detalleRequisicion", detalleRequisicion);
			ac.put("tagTypes", UtilAccountingTags.getClassificationTagsForOrganization(organizationPartyId,cicloId,UtilAccountingTags.EGRESO_TAG, delegator));
			
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Obtiene un mapa de listas con los periodos de cada ciclo <ciclo, listaPeriodos>
	 * @param listCiclos
	 * @param delegator
	 * @param timeZone
	 * @param locale
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map<String, List<GenericValue>> getMapaMesesXCiclo(LinkedHashSet<String> listCiclos,
			Delegator delegator, TimeZone timeZone, Locale locale) throws GenericEntityException{
		
		String mes = null;
		
		Map<String, List<GenericValue>> mapMesesXCiclo = FastMap.newInstance();
		
		for (String cicloString : listCiclos) {
			List<GenericValue> listMapaMeses = FastList.newInstance();
    		EntityConditionList<EntityExpr> condiciones = EntityCondition.makeCondition(
    				UtilMisc.toList(
//    						EntityCondition.makeConditionWhere(" YEAR(FROM_DATE) = "+cicloString),
    						EntityCondition.makeCondition("fromDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.toDate(1, 1, Integer.parseInt(cicloString), 0, 0, 0)),
    						EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.toDate(12, 1, Integer.parseInt(cicloString), 23, 59, 59)),
    						EntityCondition.makeCondition("periodTypeId","FISCAL_MONTH")));
        	
        	List<GenericValue> listCustomTimePeriod = delegator.findByCondition("CustomTimePeriod", condiciones, null, null);
        	
        	Date fechaPeriodo = null;
        	for (GenericValue CustomTimePeriod : listCustomTimePeriod) {
        		fechaPeriodo = CustomTimePeriod.getDate("fromDate");
        		mes = String.valueOf(UtilDateTime.getMonth(UtilDateTime.toTimestamp(fechaPeriodo), timeZone, locale)+1);
        		GenericValue CustomTimeTemporal = delegator.makeValue("CustomTimePeriod");
        		CustomTimeTemporal.put("customTimePeriodId", CustomTimePeriod.getString("customTimePeriodId"));
        		CustomTimeTemporal.put("periodNum", UtilFormatOut.formatPaddedNumber(Integer.valueOf(mes), 2));
        		CustomTimeTemporal.put("fromDate", CustomTimePeriod.getDate("fromDate"));
        		CustomTimeTemporal.put("thruDate", CustomTimePeriod.getDate("thruDate"));
        		listMapaMeses.add(CustomTimeTemporal);
			}
        	
        	mapMesesXCiclo.put(cicloString, listMapaMeses);
		}
		
		return mapMesesXCiclo;
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
	
	 public static void pagosAnticipadosPendientes(Map<String, Object> context) {
     	
 		try {
 			
 		final ActionContext ac = new ActionContext(context);
 		final Delegator delegator = ac.getDelegator();
 		GenericValue userLogin = (GenericValue) context.get("userLogin");
 		String solicitante = userLogin.getString("partyId");
 		String status = "PENDIENTE_PA";
 		String statusComentada = "COMENTADA_W";

 		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
 		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
 				.getLedgerRepository();

 		// build search conditions
 		List<EntityCondition> searchConditions = new FastList<EntityCondition>();
 		
 		List<EntityCondition> searchConditionsOr = new FastList<EntityCondition>();
 		
 		searchConditionsOr.add(EntityCondition.makeCondition("statusId",
 				EntityOperator.EQUALS, status));
 		
 		searchConditionsOr.add(EntityCondition.makeCondition("statusId",
 				EntityOperator.EQUALS, statusComentada));
 		
 		searchConditions.add(EntityCondition.makeCondition(
 		 		searchConditionsOr, EntityOperator.OR));
 		
 		searchConditions.add(EntityCondition.makeCondition("personParentId",
 				EntityOperator.EQUALS, solicitante));
 		searchConditions.add(EntityCondition.makeCondition("tipoWorkFlowId",
 				EntityOperator.EQUALS, "PAGO_ANTICIPADO"));

 		// fields to select
 		List<String> orderBy = UtilMisc.toList("orderId");
 		Debug.log("Search conditions : " + searchConditions);
 		EntityListBuilder solicitudPagoPendienteListBuilder = null;
 		PageBuilder<SolicitudesPagoAntiPendientes> pageBuilderSolicitudesPendientes = null;
 		
 		List<SolicitudesPagoAntiPendientes> solicitudes = ledgerRepository.findList(SolicitudesPagoAntiPendientes.class, EntityCondition.makeCondition(
 				searchConditions, EntityOperator.AND));
 		
 		solicitudPagoPendienteListBuilder = new EntityListBuilder(ledgerRepository,
 				SolicitudesPagoAntiPendientes.class, EntityCondition.makeCondition(
 						searchConditions, EntityOperator.AND), null, orderBy);
 		pageBuilderSolicitudesPendientes = new PageBuilder<SolicitudesPagoAntiPendientes>() {
 			public List<Map<String, Object>> build(
 					List<SolicitudesPagoAntiPendientes> page) throws Exception {
 				List<Map<String, Object>> newPage = FastList.newInstance();
 				GenericValue orderGeneric = null;
 				for (SolicitudesPagoAntiPendientes ordenId : page) {
 					Map<String, Object> newRow = FastMap.newInstance();
 					newRow.putAll(ordenId.toMap());
 					orderGeneric = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", ordenId.get("orderId")));
 					newRow.put("currencyUom", orderGeneric.get("currencyUom"));
 					newPage.add(newRow);
 				}
 				return newPage;
 			}
 		};
 		solicitudPagoPendienteListBuilder
 				.setPageBuilder(pageBuilderSolicitudesPendientes);
 		ac.put("solicitudPagoPendienteListBuilder", solicitudPagoPendienteListBuilder);
 		
 		} catch (RepositoryException e) {
 			e.printStackTrace();
 		} catch (ListBuilderException e) {
 			e.printStackTrace();
 		}
 	}
	
}
