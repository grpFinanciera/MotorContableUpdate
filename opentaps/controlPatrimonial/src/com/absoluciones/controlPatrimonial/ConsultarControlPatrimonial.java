package com.absoluciones.controlPatrimonial;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.opentaps.base.entities.ConsultaDepreciacionActivo;
import org.opentaps.base.entities.FixedAssetPolizaSeguro;
import org.opentaps.base.entities.ListaActivoFijo;
import org.opentaps.base.entities.ListaLevantamiento;
import org.opentaps.base.entities.ListaLevantamientoNoExiste;
import org.opentaps.common.builder.EntityListBuilder;
import org.opentaps.common.builder.ListBuilderException;
import org.opentaps.common.builder.PageBuilder;
import org.opentaps.common.party.PartyHelper;
import org.opentaps.domain.DomainsDirectory;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.action.ActionContext;
import org.opentaps.foundation.repository.RepositoryException;

public class ConsultarControlPatrimonial {

	private static final String MODULE = ConsultarControlPatrimonial.class.getName();
	
	public static void consultaActivoFijo(Map<String, Object> context)throws GeneralException, ParseException {
		
		final ActionContext ac = new ActionContext(context);
		Delegator delegator = ac.getDelegator();
		
		String fixedAssetId = ac.getParameter("fixedAssetId");
		String fixedAssetTypeId = ac.getParameter("fixedAssetTypeId");
		String productId = ac.getParameter("productId");
		String resguardante = ac.getParameter("resguardante");
		String nombreActivo = ac.getParameter("nombreActivo");
		String numeroSerie = ac.getParameter("numeroSerie");
		String edoFisicoId = ac.getParameter("edoFisicoId");
		String ubicacionRapidaId = ac.getParameter("ubicacionRapidaId");
		String marca = ac.getParameter("marca");
		String modelo = ac.getParameter("modelo");
		String proveedorId = ac.getParameter("proveedorId");
		String numeroFactura = ac.getParameter("numeroFactura");
		String organizationId = ac.getParameter("organizationId");
		String numeroPoliza = ac.getParameter("numeroPoliza");
		String partyId = ac.getParameter("partyId");
		String orderId = ac.getParameter("orderId");
		String placa = ac.getParameter("placa");
		String idActivoAnterior = ac.getParameter("idActivoAnterior");
		String tipoUnidadVehiculoId = ac.getParameter("tipoUnidadVehiculoId");
		String tipoAdjudicacionId = ac.getParameter("tipoAdjudicacionId");
		String estatus = ac.getParameter("estatus");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
			
		if ("Y".equals(ac.getParameter("performFind"))) {
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();

			if (UtilValidate.isNotEmpty(fixedAssetId)) {
				searchConditions.add(EntityCondition.makeCondition("fixedAssetId", EntityOperator.EQUALS, fixedAssetId));
			}
			if (UtilValidate.isNotEmpty(fixedAssetTypeId)) {
				searchConditions.add(EntityCondition.makeCondition("fixedAssetTypeId", EntityOperator.EQUALS, fixedAssetTypeId));
			}
			if (UtilValidate.isNotEmpty(productId)) {
				searchConditions.add(EntityCondition.makeCondition("instanceOfProductId", EntityOperator.EQUALS, productId));
			}
			if (UtilValidate.isNotEmpty(resguardante)) {
				searchConditions.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, resguardante));
			}
			if (UtilValidate.isNotEmpty(nombreActivo)) {
				searchConditions.add(EntityCondition.makeCondition("fixedAssetName", EntityOperator.LIKE, "%"+nombreActivo+"%"));
			}
			if (UtilValidate.isNotEmpty(numeroSerie)) {
				searchConditions.add(EntityCondition.makeCondition("serialNumber", EntityOperator.EQUALS, numeroSerie));
			}
			if (UtilValidate.isNotEmpty(edoFisicoId)) {
				searchConditions.add(EntityCondition.makeCondition("edoFisicoId", EntityOperator.EQUALS, edoFisicoId));
			}
			if (UtilValidate.isNotEmpty(ubicacionRapidaId)) {
				searchConditions.add(EntityCondition.makeCondition("ubicacionRapidaId", EntityOperator.EQUALS, ubicacionRapidaId));
			}
			if (UtilValidate.isNotEmpty(marca)) {
				searchConditions.add(EntityCondition.makeCondition("marca", EntityOperator.LIKE, "%"+marca+"%"));
			}
			if (UtilValidate.isNotEmpty(modelo)) {
				searchConditions.add(EntityCondition.makeCondition("modelo", EntityOperator.LIKE, "%"+modelo+"%"));
			}
			if (UtilValidate.isNotEmpty(proveedorId)) {
				searchConditions.add(EntityCondition.makeCondition("proveedor", EntityOperator.EQUALS, proveedorId));
			}
			if (UtilValidate.isNotEmpty(numeroFactura)) {
				searchConditions.add(EntityCondition.makeCondition("numeroFactura", EntityOperator.EQUALS, numeroFactura));
			}
			if (UtilValidate.isNotEmpty(organizationId)) {
				searchConditions.add(EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS, organizationId));
			}
			if (UtilValidate.isNotEmpty(numeroPoliza)) {
				searchConditions.add(EntityCondition.makeCondition("numeroPoliza", EntityOperator.EQUALS, numeroPoliza));
			}
			if (UtilValidate.isNotEmpty(partyId)) {
				searchConditions.add(EntityCondition.makeCondition("areaPartyId", EntityOperator.EQUALS, partyId));
			}
			if (UtilValidate.isNotEmpty(orderId)) {
				searchConditions.add(EntityCondition.makeCondition("acquireOrderId", EntityOperator.EQUALS, orderId));
			}
			if (UtilValidate.isNotEmpty(placa)) {
				searchConditions.add(EntityCondition.makeCondition("placa", EntityOperator.LIKE, "%"+placa+"%"));
			}
			if (UtilValidate.isNotEmpty(idActivoAnterior)) {
				searchConditions.add(EntityCondition.makeCondition("idActivoAnterior", EntityOperator.LIKE, "%"+idActivoAnterior+"%"));
			}
			if (UtilValidate.isNotEmpty(tipoUnidadVehiculoId)) {
				searchConditions.add(EntityCondition.makeCondition("tipoUnidadVehiculoId", EntityOperator.LIKE, "%"+tipoUnidadVehiculoId+"%"));
			}
			if (UtilValidate.isNotEmpty(estatus)) {
				searchConditions.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, estatus));
			}
			if (UtilValidate.isNotEmpty(tipoAdjudicacionId)) {
	            List<EntityExpr> cond = UtilMisc.toList(
	                    EntityCondition.makeCondition("tipoAdjudicacionId", EntityOperator.EQUALS, tipoAdjudicacionId),
	                    EntityCondition.makeCondition("AdjudicacionOH", EntityOperator.EQUALS, tipoAdjudicacionId)
	                    );
	            searchConditions.add(EntityCondition.makeCondition(cond, EntityOperator.OR));
			}
			
			searchConditions.add(EntityCondition.makeCondition("partyIdPerson", EntityOperator.EQUALS, userLogin.getString("partyId")));

			// fields to select
			List<String> orderBy = UtilMisc.toList("fixedAssetId");
			EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
			Debug.logInfo("search conditions : " + EntityCondition.makeCondition(searchConditions, EntityOperator.AND).toString(), MODULE);
			List<GenericValue> listActivoFijo = delegator.findByCondition("ListaActivoFijo", condiciones, null, orderBy);

			List<Map<String,Object>> listActivoFijoBuilder = FastList.newInstance();
			
			Map<String,Object> mapaActivoFijo = FastMap.newInstance();
			
			for (GenericValue activoFijo : listActivoFijo) {
				mapaActivoFijo.putAll(activoFijo.getAllFields());				
				mapaActivoFijo.put("fixedAssetId", Long.valueOf(activoFijo.getString("fixedAssetId")));
				
				if(UtilValidate.isNotEmpty(activoFijo.getString("proveedor"))){
					mapaActivoFijo.put("proveName", activoFijo.getString("groupNamePr"));
				}
				
				if(UtilValidate.isNotEmpty(activoFijo.getString("areaPartyId"))){
					mapaActivoFijo.put("areaName", activoFijo.getString("groupNameAr"));
				}
				
				GenericValue partyResguardante = null;
				
				if(UtilValidate.isNotEmpty(activoFijo.getString("partyIdPers"))){
					partyResguardante = delegator.makeValue("Person");
					partyResguardante.set("partyId", activoFijo.getString("partyIdPers"));
					partyResguardante.set("firstName", activoFijo.getString("firstName"));
					partyResguardante.set("lastName", activoFijo.getString("lastName"));
				} else if (UtilValidate.isNotEmpty(activoFijo.getString("partyIdGroupPo"))){
					partyResguardante = delegator.makeValue("PartyGroup");
					partyResguardante.set("partyId", activoFijo.getString("partyIdGroupPo"));
					partyResguardante.set("groupName", activoFijo.getString("groupNamePo"));
				}
				
				mapaActivoFijo.put("resguName", "");
				if(UtilValidate.isNotEmpty(partyResguardante)){
					mapaActivoFijo.put("resguName", PartyHelper.getCrmsfaPartyName(partyResguardante));
				}
				
				listActivoFijoBuilder.add(mapaActivoFijo);
				mapaActivoFijo = FastMap.newInstance();
			}
			
			Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
				public int compare(Map<String, Object> m1, Map<String, Object> m2) {
					return ((Long) m1.get("fixedAssetId")).compareTo((Long) m2.get("fixedAssetId"));
				}
			};

			Collections.sort(listActivoFijoBuilder, mapComparator);			
			ac.put("activoFijoListBuilder", listActivoFijoBuilder);
		}
	}
	
	/**
	 * Consulta de Levantamiento de Inventario
	 * @param context
	 */
	public static void consultarLevantamiento(Map<String, Object> context){
		
		try {
				
			final ActionContext ac = new ActionContext(context);	
			DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
			final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();

			List<String> orderBy = UtilMisc.toList("ubicacionActual","activoFijoId");
			
			EntityListBuilder levantamientoListBuilder = null;
			PageBuilder<ListaLevantamiento> pageBuilderLevantamiento = null;
			EntityCondition condicionLevanta = EntityCondition.makeCondition("statusId",EntityOperator.NOT_EQUAL,"ACT_FIJO_BAJA");
			
			levantamientoListBuilder = new  EntityListBuilder(ledgerRepository, ListaLevantamiento.class, condicionLevanta, orderBy);
			
			pageBuilderLevantamiento = new PageBuilder<ListaLevantamiento>() {
				
				@Override
				public List<Map<String, Object>> build(List<ListaLevantamiento> page) throws Exception {
					List<Map<String, Object>> newPage = FastList.newInstance();
					for (ListaLevantamiento levant : page) 
					{	Map<String, Object> newRow = FastMap.newInstance();
						newRow.putAll(levant.toMap());
						newPage.add(newRow);
					}
					return newPage;
				
				}
			};
			
			levantamientoListBuilder.setPageBuilder(pageBuilderLevantamiento);
	        ac.put("levantamientoListBuilder", levantamientoListBuilder);
	        
	        orderBy = UtilMisc.toList("activoFijoId");
			EntityListBuilder levantamientoNoExisteListBuilder = null;
			PageBuilder<ListaLevantamientoNoExiste> pageBuilderLevantamientoNoExiste = null;
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();
			searchConditions.add(EntityCondition.makeCondition("activoFijoIdLeva",EntityOperator.EQUALS,null));
			searchConditions.add(EntityCondition.makeCondition("statusId",EntityOperator.NOT_EQUAL,"ACT_FIJO_BAJA"));
			EntityCondition condicion = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
			
			levantamientoNoExisteListBuilder = new  EntityListBuilder(ledgerRepository, ListaLevantamientoNoExiste.class, condicion, orderBy);
			
			pageBuilderLevantamientoNoExiste = new PageBuilder<ListaLevantamientoNoExiste>() {
				@Override
				public List<Map<String, Object>> build(List<ListaLevantamientoNoExiste> page) throws Exception {
					List<Map<String, Object>> newPage = FastList.newInstance();
					for (ListaLevantamientoNoExiste levant : page) 
					{	Map<String, Object> newRow = FastMap.newInstance();
						newRow.putAll(levant.toMap());
						newPage.add(newRow);
					}
					return newPage;
				}
			};
			
			levantamientoNoExisteListBuilder.setPageBuilder(pageBuilderLevantamientoNoExiste);
	        ac.put("levantamientoNoExisteListBuilder", levantamientoNoExisteListBuilder);
	        
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (ListBuilderException e) {
			e.printStackTrace();
		}	
	}
	
	/**
	 * Consulta de transacciones realizadas por depreciacion de una categoria de activo fijo
	 * @param context
	 */
	public static void consultaDepActivoFijo(Map<String, Object> context)
			throws GeneralException, ParseException {
		

		final ActionContext ac = new ActionContext(context);
		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
				.getLedgerRepository();
		String fixedAssetTypeId = ac.getParameter("fixedAssetTypeId");
		String mesId = ac.getParameter("mesId");
		String enumCode = ac.getParameter("enumCode");
		String acctgTransTypeId = ac.getParameter("acctgTransTypeId");
		String comentario = ac.getParameter("comentario");				
		
		if ("Y".equals(ac.getParameter("performFind"))) {
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();
			
			if (UtilValidate.isNotEmpty(fixedAssetTypeId)) {
				searchConditions.add(EntityCondition.makeCondition("tipoActivoFijo", EntityOperator.EQUALS, fixedAssetTypeId));
			}
			if (UtilValidate.isNotEmpty(mesId)) {
				searchConditions.add(EntityCondition.makeCondition("mes", EntityOperator.EQUALS, mesId));
			}
			if (UtilValidate.isNotEmpty(enumCode)) {
				searchConditions.add(EntityCondition.makeCondition("ciclo", EntityOperator.EQUALS, enumCode));
			}
			if (UtilValidate.isNotEmpty(acctgTransTypeId)) {
				searchConditions.add(EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, acctgTransTypeId));
			}
			if (UtilValidate.isNotEmpty(comentario)) {
				searchConditions.add(EntityCondition.makeCondition("descPoliza", EntityOperator.LIKE, "%"+comentario+"%"));
			}			
			
			List<String> orderBy = UtilMisc.toList("fixedAssetId");
			Debug.logInfo("search conditions : " + EntityCondition.makeCondition(searchConditions, EntityOperator.AND).toString(), MODULE);						
			
			EntityListBuilder depreciacionListBuilder = null;
			PageBuilder<ConsultaDepreciacionActivo> pageBuilderDepreciacion = null;
			
			depreciacionListBuilder = new EntityListBuilder(ledgerRepository, ConsultaDepreciacionActivo.class, EntityCondition.makeCondition(searchConditions, EntityOperator.AND), null, orderBy);
			
			pageBuilderDepreciacion = new PageBuilder<ConsultaDepreciacionActivo>() {
				
				@Override
				public List<Map<String, Object>> build(List<ConsultaDepreciacionActivo> page) throws Exception {
					List<Map<String, Object>> newPage = FastList.newInstance();
					for (ConsultaDepreciacionActivo levant : page) 
					{	Map<String, Object> newRow = FastMap.newInstance();
						newRow.putAll(levant.toMap());
						newPage.add(newRow);
					}
					return newPage;
				
				}
			};
			
			depreciacionListBuilder.setPageBuilder(pageBuilderDepreciacion);
	        ac.put("depreciacionListBuilder", depreciacionListBuilder);
		}
	}
		
	/**
	 * Consulta el historial de las polizas de seguro de los activos
	 * @param context
	 */
	public static void consultaPolizaSeguroActivo(Map<String, Object> context)
			throws GeneralException, ParseException {
		
		final ActionContext ac = new ActionContext(context);
		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
				.getLedgerRepository();
		
		List<EntityCondition> searchConditions = new FastList<EntityCondition>();						
		
		List<String> orderBy = UtilMisc.toList("fechaDesdeNueva");						

						
		EntityListBuilder historialListBuilder = null;
		PageBuilder<FixedAssetPolizaSeguro> pageBuilderHistorial = null;
		
		historialListBuilder = new EntityListBuilder(ledgerRepository, FixedAssetPolizaSeguro.class, EntityCondition.makeCondition(searchConditions, EntityOperator.AND), null, orderBy);
		
		pageBuilderHistorial = new PageBuilder<FixedAssetPolizaSeguro>() {			
			@Override
			public List<Map<String, Object>> build(List<FixedAssetPolizaSeguro> page) throws Exception {
				List<Map<String, Object>> newPage = FastList.newInstance();
				for (FixedAssetPolizaSeguro levant : page) 
				{	Map<String, Object> newRow = FastMap.newInstance();
					newRow.putAll(levant.toMap());
					newPage.add(newRow);
				}
				return newPage;
			
			}
		};
			
		historialListBuilder.setPageBuilder(pageBuilderHistorial);
        ac.put("historialListBuilder", historialListBuilder);
	}	
	
	/**
	 * Realiza la consulta de los activos por resguardante para realizar la reasignacion masiva
	 * @param context
	 * @throws GeneralException
	 * @throws ParseException
	 */
	public static void consultaResguardoMasivo(Map<String, Object> context)
			throws GeneralException, ParseException {
			
		final ActionContext ac = new ActionContext(context);
		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();
		Delegator delegator = ac.getDelegator();		
		String resguardante = ac.getParameter("resguardante");		
		String nuevoResguardante = ac.getParameter("nuevoResguardante");
		String fechaInicioResguardoS = ac.getCompositeParameter("fechaInicioResguardo");
		String fechaFinResguardoS = ac.getCompositeParameter("fechaFinResguardo");
		String fechaAsignacionS = ac.getCompositeParameter("fechaAsignacion");
		
		Timestamp fechaInicioResguardo = null;
		Timestamp fechaFinResguardo = null;
		Timestamp fechaAsignacion = null;
		String dateTimeFormat = UtilDateTime.getDateTimeFormat(ac.getLocale());
		if(UtilValidate.isNotEmpty(fechaInicioResguardoS)){
			fechaInicioResguardo = UtilDateTime.stringToTimeStamp(
					fechaInicioResguardoS,dateTimeFormat, ac.getTimeZone(), ac.getLocale());
		}
		if(UtilValidate.isNotEmpty(fechaFinResguardoS)){
			fechaFinResguardo = UtilDateTime.stringToTimeStamp(
					fechaFinResguardoS,dateTimeFormat, ac.getTimeZone(), ac.getLocale());
		}
		if(UtilValidate.isNotEmpty(fechaAsignacionS)){
			fechaAsignacion = UtilDateTime.stringToTimeStamp(
					fechaAsignacionS,dateTimeFormat, ac.getTimeZone(), ac.getLocale());
		}
		
		String comentariosAsignacion = ac.getParameter("comentariosAsignacion");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		
		
		if ("Y".equals(ac.getParameter("performFind"))) {
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();			
			if (UtilValidate.isNotEmpty(resguardante)) {
				searchConditions.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, resguardante));
			}			
			searchConditions.add(EntityCondition.makeCondition("partyIdPerson", EntityOperator.EQUALS, userLogin.getString("partyId")));
			searchConditions.add(EntityCondition.makeCondition("statusId",EntityOperator.NOT_EQUAL,"ACT_FIJO_BAJA"));
			//searchConditions.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ASIGN_ASIGNADO"));
			
			
						
			Debug.log("searchConditions: " + searchConditions);
	        List<ListaActivoFijo> itemsActivoFijo = ledgerRepository.findList(ListaActivoFijo.class, searchConditions);	        
	        List<Map<String, Object>> listResguardosActivo = new FastList<Map<String, Object>>();
	        for (ListaActivoFijo s : itemsActivoFijo) {
	            Map<String, Object> map = s.toMap();	    
				GenericValue partyPer = delegator.makeValue("Person");
				GenericValue partyGrp = delegator.makeValue("PartyGroup");
				
				if(UtilValidate.isNotEmpty(map.get("areaPartyId"))){
					map.put("areaName", s.getString("groupNameAr"));
				}
				partyPer.set("partyId", s.getString("partyId"));
				partyPer.set("firstName", s.getString("firstName"));
				partyPer.set("lastName", s.getString("lastName"));
				partyGrp.set("partyId", s.getString("partyId"));
				partyGrp.set("groupName", s.getString("groupNamePo"));
				
				if(UtilValidate.isNotEmpty(partyGrp.getString("groupName"))){
					map.put("resguName", partyGrp.getString("groupName")+" ( "+partyGrp.getString("partyId")+" )");
				}else if(UtilValidate.isNotEmpty(partyPer.getString("firstName"))&&UtilValidate.isNotEmpty(partyPer.getString("lastName"))){
					map.put("resguName", PartyHelper.getCrmsfaPartyName(partyPer));
				}else{
					map.put("resguName", "");
				}
	            listResguardosActivo.add(map);
	        }
	        ac.put("listResguardosActivo", listResguardosActivo);
	        ac.put("nuevoResguardante", nuevoResguardante);
	        ac.put("resguardante", resguardante);	        
	        ac.put("fechaInicioResguardo", fechaInicioResguardo);
	        ac.put("fechaFinResguardo", fechaFinResguardo);
	        ac.put("fechaAsignacion", fechaAsignacion);
	        ac.put("comentariosAsignacion", comentariosAsignacion);

		}
	}
	
	public static void consultaCodigoBarras(Map<String, Object> context)
			throws GeneralException, ParseException {
		

		final ActionContext ac = new ActionContext(context);
		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();
		Delegator delegator = ac.getDelegator();
		String fixedAssetId = ac.getParameter("fixedAssetId");
		String fixedAssetTypeId = ac.getParameter("fixedAssetTypeId");
		String productId = ac.getParameter("productId");
		String resguardante = ac.getParameter("resguardante");
		String nombreActivo = ac.getParameter("nombreActivo");
		String numeroSerie = ac.getParameter("numeroSerie");
		String edoFisicoId = ac.getParameter("edoFisicoId");
		String ubicacionRapidaId = ac.getParameter("ubicacionRapidaId");
		String marca = ac.getParameter("marca");
		String modelo = ac.getParameter("modelo");
		String proveedorId = ac.getParameter("proveedorId");
		String numeroFactura = ac.getParameter("numeroFactura");
		String organizationId = ac.getParameter("organizationId");
		String numeroPoliza = ac.getParameter("numeroPoliza");
		String partyId = ac.getParameter("partyId");
		String orderId = ac.getParameter("orderId");
		String placa = ac.getParameter("placa");
		String idActivoAnterior = ac.getParameter("idActivoAnterior");
		String idTamano = ac.getParameter("idTamano");
		String posicionImpresion = ac.getParameter("posicionImpresion");				
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		
		if(posicionImpresion== null || posicionImpresion.equals(""))
		{	return;
		}
		
		
		if ("Y".equals(ac.getParameter("performFind"))) {
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();

			if (UtilValidate.isNotEmpty(fixedAssetId)) {
				searchConditions.add(EntityCondition.makeCondition("fixedAssetId", EntityOperator.EQUALS, fixedAssetId));
			}
			if (UtilValidate.isNotEmpty(fixedAssetTypeId)) {
				searchConditions.add(EntityCondition.makeCondition("fixedAssetTypeId", EntityOperator.EQUALS, fixedAssetTypeId));
			}
			if (UtilValidate.isNotEmpty(productId)) {
				searchConditions.add(EntityCondition.makeCondition("instanceOfProductId", EntityOperator.EQUALS, productId));
			}			
			if (UtilValidate.isNotEmpty(resguardante)) {
				searchConditions.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, resguardante));
			}
			if (UtilValidate.isNotEmpty(nombreActivo)) {
				searchConditions.add(EntityCondition.makeCondition("fixedAssetName", EntityOperator.LIKE, "%"+nombreActivo+"%"));
			}
			if (UtilValidate.isNotEmpty(numeroSerie)) {
				searchConditions.add(EntityCondition.makeCondition("serialNumber", EntityOperator.EQUALS, numeroSerie));
			}
			if (UtilValidate.isNotEmpty(edoFisicoId)) {
				searchConditions.add(EntityCondition.makeCondition("edoFisicoId", EntityOperator.EQUALS, edoFisicoId));
			}
			if (UtilValidate.isNotEmpty(ubicacionRapidaId)) {
				searchConditions.add(EntityCondition.makeCondition("ubicacionRapidaId", EntityOperator.EQUALS, ubicacionRapidaId));
			}
			if (UtilValidate.isNotEmpty(marca)) {
				searchConditions.add(EntityCondition.makeCondition("marca", EntityOperator.LIKE, "%"+marca+"%"));
			}
			if (UtilValidate.isNotEmpty(modelo)) {
				searchConditions.add(EntityCondition.makeCondition("modelo", EntityOperator.LIKE, "%"+modelo+"%"));
			}
			if (UtilValidate.isNotEmpty(proveedorId)) {
				searchConditions.add(EntityCondition.makeCondition("proveedor", EntityOperator.EQUALS, proveedorId));
			}
			if (UtilValidate.isNotEmpty(numeroFactura)) {
				searchConditions.add(EntityCondition.makeCondition("numeroFactura", EntityOperator.EQUALS, numeroFactura));
			}
			if (UtilValidate.isNotEmpty(organizationId)) {
				searchConditions.add(EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS, organizationId));
			}
			if (UtilValidate.isNotEmpty(numeroPoliza)) {
				searchConditions.add(EntityCondition.makeCondition("numeroPoliza", EntityOperator.EQUALS, numeroPoliza));
			}
			if (UtilValidate.isNotEmpty(partyId)) {
				searchConditions.add(EntityCondition.makeCondition("areaPartyId", EntityOperator.EQUALS, partyId));
			}
			if (UtilValidate.isNotEmpty(orderId)) {
				searchConditions.add(EntityCondition.makeCondition("acquiereOrderId", EntityOperator.EQUALS, orderId));
			}
			if (UtilValidate.isNotEmpty(placa)) {
				searchConditions.add(EntityCondition.makeCondition("placa", EntityOperator.LIKE, "%"+placa+"%"));
			}
			if (UtilValidate.isNotEmpty(idActivoAnterior)) {
				searchConditions.add(EntityCondition.makeCondition("idActivoAnterior", EntityOperator.LIKE, "%"+idActivoAnterior+"%"));
			}
			searchConditions.add(EntityCondition.makeCondition("partyIdPerson", EntityOperator.EQUALS, userLogin.getString("partyId")));
			searchConditions.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "ACT_FIJO_BAJA"));
			Debug.log("searchConditions: " + searchConditions);
			List<String> orderBy = UtilMisc.toList("fixedAssetId");
	        List<ListaActivoFijo> itemsActivoFijo = ledgerRepository.findList(ListaActivoFijo.class, searchConditions, orderBy);	        
	        List<Map<String, Object>> listActivoCodigoBarras = new FastList<Map<String, Object>>();
	        for (ListaActivoFijo s : itemsActivoFijo) {
	            Map<String, Object> map = s.toMap();	            
	            String proveName = "";
	            String areaName = "";
				if(map.get("proveedor") != null && !map.get("proveedor").equals(""))
				{	GenericValue partyGroup = delegator.findByPrimaryKey("PartyGroup", UtilMisc.toMap("partyId", map.get("proveedor")));
					proveName = (String) partyGroup.get("groupName");													
				}							
				if(map.get("areaPartyId") != null && !map.get("areaPartyId").equals(""))
				{	GenericValue partyGroup = delegator.findByPrimaryKey("PartyGroup", UtilMisc.toMap("partyId", map.get("areaPartyId")));
					areaName = (String) partyGroup.get("groupName");							
				}									
				map.put("proveName", proveName);
				map.put("areaName", areaName);	           	            	          	            
				listActivoCodigoBarras.add(map);
	        }
	        ac.put("listActivoCodigoBarras", listActivoCodigoBarras);
	        ac.put("idTamano", idTamano);
	        ac.put("posicionImpresion", posicionImpresion);	        
		}
	}
	
public static void consultaBajaActivoFijo(Map<String, Object> context)throws GeneralException, ParseException {
		
		final ActionContext ac = new ActionContext(context);
		Delegator delegator = ac.getDelegator();
		
		String fixedAssetId = ac.getParameter("fixedAssetId");
		String fixedAssetTypeId = ac.getParameter("fixedAssetTypeId");
		String productId = ac.getParameter("productId");
		String resguardante = ac.getParameter("resguardante");
		String nombreActivo = ac.getParameter("nombreActivo");
		String numeroSerie = ac.getParameter("numeroSerie");
		String edoFisicoId = ac.getParameter("edoFisicoId");
		String ubicacionRapidaId = ac.getParameter("ubicacionRapidaId");
		String marca = ac.getParameter("marca");
		String modelo = ac.getParameter("modelo");
		String proveedorId = ac.getParameter("proveedorId");
		String numeroFactura = ac.getParameter("numeroFactura");
		String organizationId = ac.getParameter("organizationId");
		String numeroPoliza = ac.getParameter("numeroPoliza");
		String partyId = ac.getParameter("partyId");
		String orderId = ac.getParameter("orderId");
		String placa = ac.getParameter("placa");
		String idActivoAnterior = ac.getParameter("idActivoAnterior");
		String tipoUnidadVehiculoId = ac.getParameter("tipoUnidadVehiculoId");
		String tipoAdjudicacionId = ac.getParameter("tipoAdjudicacionId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
			
		if ("Y".equals(ac.getParameter("performFind"))) {
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();

			if (UtilValidate.isNotEmpty(fixedAssetId)) {
				searchConditions.add(EntityCondition.makeCondition("fixedAssetId", EntityOperator.EQUALS, fixedAssetId));
			}
			if (UtilValidate.isNotEmpty(fixedAssetTypeId)) {
				searchConditions.add(EntityCondition.makeCondition("fixedAssetTypeId", EntityOperator.EQUALS, fixedAssetTypeId));
			}
			if (UtilValidate.isNotEmpty(productId)) {
				searchConditions.add(EntityCondition.makeCondition("instanceOfProductId", EntityOperator.EQUALS, productId));
			}
			if (UtilValidate.isNotEmpty(resguardante)) {
				searchConditions.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, resguardante));
			}
			if (UtilValidate.isNotEmpty(nombreActivo)) {
				searchConditions.add(EntityCondition.makeCondition("fixedAssetName", EntityOperator.LIKE, "%"+nombreActivo+"%"));
			}
			if (UtilValidate.isNotEmpty(numeroSerie)) {
				searchConditions.add(EntityCondition.makeCondition("serialNumber", EntityOperator.EQUALS, numeroSerie));
			}
			if (UtilValidate.isNotEmpty(edoFisicoId)) {
				searchConditions.add(EntityCondition.makeCondition("edoFisicoId", EntityOperator.EQUALS, edoFisicoId));
			}
			if (UtilValidate.isNotEmpty(ubicacionRapidaId)) {
				searchConditions.add(EntityCondition.makeCondition("ubicacionRapidaId", EntityOperator.EQUALS, ubicacionRapidaId));
			}
			if (UtilValidate.isNotEmpty(marca)) {
				searchConditions.add(EntityCondition.makeCondition("marca", EntityOperator.LIKE, "%"+marca+"%"));
			}
			if (UtilValidate.isNotEmpty(modelo)) {
				searchConditions.add(EntityCondition.makeCondition("modelo", EntityOperator.LIKE, "%"+modelo+"%"));
			}
			if (UtilValidate.isNotEmpty(proveedorId)) {
				searchConditions.add(EntityCondition.makeCondition("proveedor", EntityOperator.EQUALS, proveedorId));
			}
			if (UtilValidate.isNotEmpty(numeroFactura)) {
				searchConditions.add(EntityCondition.makeCondition("numeroFactura", EntityOperator.EQUALS, numeroFactura));
			}
			if (UtilValidate.isNotEmpty(organizationId)) {
				searchConditions.add(EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS, organizationId));
			}
			if (UtilValidate.isNotEmpty(numeroPoliza)) {
				searchConditions.add(EntityCondition.makeCondition("numeroPoliza", EntityOperator.EQUALS, numeroPoliza));
			}
			if (UtilValidate.isNotEmpty(partyId)) {
				searchConditions.add(EntityCondition.makeCondition("areaPartyId", EntityOperator.EQUALS, partyId));
			}
			if (UtilValidate.isNotEmpty(orderId)) {
				searchConditions.add(EntityCondition.makeCondition("acquireOrderId", EntityOperator.EQUALS, orderId));
			}
			if (UtilValidate.isNotEmpty(placa)) {
				searchConditions.add(EntityCondition.makeCondition("placa", EntityOperator.LIKE, "%"+placa+"%"));
			}
			if (UtilValidate.isNotEmpty(idActivoAnterior)) {
				searchConditions.add(EntityCondition.makeCondition("idActivoAnterior", EntityOperator.LIKE, "%"+idActivoAnterior+"%"));
			}
			if (UtilValidate.isNotEmpty(tipoUnidadVehiculoId)) {
				searchConditions.add(EntityCondition.makeCondition("tipoUnidadVehiculoId", EntityOperator.LIKE, "%"+tipoUnidadVehiculoId+"%"));
			}
			if (UtilValidate.isNotEmpty(tipoAdjudicacionId)) {
	            List<EntityExpr> cond = UtilMisc.toList(
	                    EntityCondition.makeCondition("tipoAdjudicacionId", EntityOperator.EQUALS, tipoAdjudicacionId),
	                    EntityCondition.makeCondition("AdjudicacionOH", EntityOperator.EQUALS, tipoAdjudicacionId)
	                    );
	            searchConditions.add(EntityCondition.makeCondition(cond, EntityOperator.OR));
			}
			
			searchConditions.add(EntityCondition.makeCondition("partyIdPerson", EntityOperator.EQUALS, userLogin.getString("partyId")));

			// fields to select
			List<String> orderBy = UtilMisc.toList("fixedAssetId");
			EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
			Debug.logInfo("search conditions : " + EntityCondition.makeCondition(searchConditions, EntityOperator.AND).toString(), MODULE);
			List<GenericValue> listActivoFijo = delegator.findByCondition("ListaActivoFijo", condiciones, null, orderBy);

			List<Map<String,Object>> listBajaActivoFijoBuilder = FastList.newInstance();
			
			Map<String,Object> mapaActivoFijo = FastMap.newInstance();
			
			for (GenericValue activoFijo : listActivoFijo) {
				mapaActivoFijo.putAll(activoFijo.getAllFields());				
				mapaActivoFijo.put("fixedAssetId", Long.valueOf(activoFijo.getString("fixedAssetId")));
				
				if(UtilValidate.isNotEmpty(activoFijo.getString("proveedor"))){
					mapaActivoFijo.put("proveName", activoFijo.getString("groupNamePr"));
				}
				
				if(UtilValidate.isNotEmpty(activoFijo.getString("areaPartyId"))){
					mapaActivoFijo.put("areaName", activoFijo.getString("groupNameAr"));
				}
				
				GenericValue partyResguardante = null;
				
				if(UtilValidate.isNotEmpty(activoFijo.getString("partyIdPers"))){
					partyResguardante = delegator.makeValue("Person");
					partyResguardante.set("partyId", activoFijo.getString("partyIdPers"));
					partyResguardante.set("firstName", activoFijo.getString("firstName"));
					partyResguardante.set("lastName", activoFijo.getString("lastName"));
				} else if (UtilValidate.isNotEmpty(activoFijo.getString("partyIdGroupPo"))){
					partyResguardante = delegator.makeValue("PartyGroup");
					partyResguardante.set("partyId", activoFijo.getString("partyIdGroupPo"));
					partyResguardante.set("groupName", activoFijo.getString("groupNamePo"));
				}
				
				mapaActivoFijo.put("resguName", "");
				if(UtilValidate.isNotEmpty(partyResguardante)){
					mapaActivoFijo.put("resguName", PartyHelper.getCrmsfaPartyName(partyResguardante));
				}
				
				listBajaActivoFijoBuilder.add(mapaActivoFijo);
				mapaActivoFijo = FastMap.newInstance();
			}
			
			Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
				public int compare(Map<String, Object> m1, Map<String, Object> m2) {
					return ((Long) m1.get("fixedAssetId")).compareTo((Long) m2.get("fixedAssetId"));
				}
			};

			Collections.sort(listBajaActivoFijoBuilder, mapComparator);			
			ac.put("activoFijoBajaListBuilder", listBajaActivoFijoBuilder);
		}
	}
}
