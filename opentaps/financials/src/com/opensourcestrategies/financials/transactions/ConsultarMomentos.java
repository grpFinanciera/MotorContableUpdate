
package com.opensourcestrategies.financials.transactions;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
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
import org.opentaps.common.util.UtilAccountingTags;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.foundation.action.ActionContext;


public class ConsultarMomentos {

	private static final String MODULE = ConsultarMomentos.class.getName();
	public static final List<String> listaMeses = UtilMisc.toList("01","02","03","04","05","06","07","08","09","10","11","12");

	/**
	 * Consulta la suficiencia presupuestal del año , a traves del momento seleccionado 
	 * @param context
	 * @throws GeneralException
	 * @throws ParseException
	 */
	public static void buscaMomentos(Map<String, Object> context)
			throws GeneralException, ParseException {

		final ActionContext ac = new ActionContext(context);
		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
		String ciclo = UtilCommon.getCicloId(ac.getRequest());

		// possible fields we're searching by
		String momentoId = ac.getParameter("momentoId");

		Delegator delegator = ac.getDelegator();
		List<Map<String, Object>> tiposClave = new FastList<Map<String, Object>>();
		Map<String, Object> tipos = new HashMap<String, Object>();
		tipos.put("id", "Ingreso");
		tiposClave.add(tipos);
		tipos = new HashMap<String, Object>();
		tipos.put("id", "Egreso");
		tiposClave.add(tipos);
		ac.put("tiposClave", tiposClave);
		
		List<String> orderByMomentos = UtilMisc.toList("cuentaAsociada");
		String cuentaAsociada = new String();
		
		if (ac.getParameter("ingresoEgreso") == null
				|| ac.getParameter("ingresoEgreso").equalsIgnoreCase("Ingreso")) {
			ac.put("tagTypes", UtilAccountingTags
					.getClassificationTagsForOrganization(organizationPartyId,ciclo,UtilAccountingTags.INGRESO_TAG, delegator));
			
			cuentaAsociada = "8.1";
			
			ac.put("ingresoEgreso", "Ingreso");
		} else {
			ac.put("tagTypes", UtilAccountingTags
					.getClassificationTagsForOrganization(organizationPartyId,ciclo,UtilAccountingTags.EGRESO_TAG, delegator));
			
			cuentaAsociada = "8.2";
			
			ac.put("ingresoEgreso", "Egreso");
		}
		
		EntityCondition condicionCuenta = EntityCondition.makeCondition("cuentaAsociada" ,EntityOperator.LIKE, cuentaAsociada+"%");
		
		List<GenericValue> listMomentos = delegator.findByCondition("Momento", condicionCuenta, null, orderByMomentos);
		ac.put("momentos", listMomentos);
		
		if ("Y".equals(ac.getParameter("performFind"))) {
			
			String baseCurrencyUomId = UtilCommon.getOrgBaseCurrency(organizationPartyId, ac.getDelegator());
			// build search conditions
			EntityCondition condicionMomento = EntityCondition.makeCondition("momentoId",momentoId);
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();
			searchConditions.add(EntityCondition.makeCondition("tipo",EntityOperator.EQUALS, ac.getParameter("ingresoEgreso")));
			searchConditions.add(condicionMomento);

			for (int i = 1; i < 16; i++) {
				String clasif = (String) ac.getParameter("clasificacion" + i);
				Debug.log("clasificacion" + i + ".- " + clasif);
				if (clasif != null && !clasif.isEmpty())
					searchConditions.add(EntityCondition.makeCondition("clasificacion"+i, EntityOperator.EQUALS, clasif));
			}
			
			Debug.logInfo("Lista condiciones .- "+searchConditions, MODULE);
			
			List<String> fieldsToSelect = UtilMisc.toList("clavePresupuestal","momentoId","mesId","monto");
			List<String> orderBy = UtilMisc.toList("clavePresupuestal","momentoId","mesId");

			// buscamos las claves segun las condiciones de la clave presupuestal
			List<GenericValue> listControlPresupuestal = delegator.findByCondition("ControlPresupuestalClave", EntityCondition.makeCondition(searchConditions), fieldsToSelect, orderBy);
			
			
			fieldsToSelect = UtilMisc.toList("clavePresupuestal");
			orderBy = UtilMisc.toList("clavePresupuestal");
			searchConditions.remove(searchConditions.indexOf(condicionMomento));
			List<GenericValue> listClavePresupuestal = delegator.findByCondition("ClavePresupuestal", EntityCondition.makeCondition(searchConditions), fieldsToSelect, orderBy);

			List<Map<String, Object>> clavesMomentoList = FastList.newInstance();
			
			Map<String,Object> mapaSumaMesTotal = FastMap.newInstance();
			Map<String,String> mapaClavesExistentes = FastMap.newInstance();
			boolean guardaClave = true;
			//Se guardan los datos que se encuentran en control presupuestal
			if(UtilValidate.isNotEmpty(listControlPresupuestal)){	
				Map<String,Object> mapaClavePresupuestal = FastMap.newInstance();
				String clavePresupuestalAnt = listControlPresupuestal.get(0).getString("clavePresupuestal");
				//Se guardan los registros de control presupuestal
				for (GenericValue ControlPresupuestal : listControlPresupuestal) {
					if(guardaClave){
						mapaClavesExistentes.put(clavePresupuestalAnt, clavePresupuestalAnt);
						mapaClavePresupuestal.put("clavePresupuestal", clavePresupuestalAnt);
						guardaClave = false;
					}
					if(clavePresupuestalAnt.equals(ControlPresupuestal.getString("clavePresupuestal"))){
						mapaClavePresupuestal.put("M"+ControlPresupuestal.getString("mesId"),ControlPresupuestal.getBigDecimal("monto"));
					} else {
						clavesMomentoList.add(mapaClavePresupuestal);
						mapaClavePresupuestal = FastMap.newInstance();
						mapaClavePresupuestal.put("M"+ControlPresupuestal.getString("mesId"),ControlPresupuestal.getBigDecimal("monto"));
						guardaClave = true;
					}
					clavePresupuestalAnt = ControlPresupuestal.getString("clavePresupuestal");
					UtilMisc.addToBigDecimalInMap(mapaSumaMesTotal, "M"+ControlPresupuestal.getString("mesId"), ControlPresupuestal.getBigDecimal("monto"));
				}
				
				mapaClavePresupuestal.put("clavePresupuestal", clavePresupuestalAnt);
				mapaClavesExistentes.put(clavePresupuestalAnt, clavePresupuestalAnt);
				clavesMomentoList.add(mapaClavePresupuestal);
			}
			
			//Se llenan los datos de los meses faltantes con ceros
			for (Map<String, Object> mapaClaves : clavesMomentoList) {
				guardaCerosMapaClavesSumaTotal(mapaClaves,baseCurrencyUomId);
			}
			
			String clavePresupuestal = new String();
			for(GenericValue ClavePresupuestal : listClavePresupuestal){
				clavePresupuestal = ClavePresupuestal.getString("clavePresupuestal");
				if(!mapaClavesExistentes.containsKey(clavePresupuestal)){
					Map<String, Object> mapaClavesInExistentes = FastMap.newInstance();
					mapaClavesInExistentes.put("clavePresupuestal", clavePresupuestal);
					guardaCerosMapaClavesSumaTotal(mapaClavesInExistentes,baseCurrencyUomId);
					clavesMomentoList.add(mapaClavesInExistentes);
				}
			}
			
			sumaTotal(mapaSumaMesTotal, baseCurrencyUomId);
			clavesMomentoList.add(mapaSumaMesTotal);

			ac.put("clavesMomentoList", clavesMomentoList);
		}
	}

	
	private static void sumaTotal(Map<String, Object> mapaMontos, String baseCurrencyUomId){
		BigDecimal total = BigDecimal.ZERO;
		for (Object montoMes : mapaMontos.values()) {
			if(montoMes instanceof BigDecimal){
				total = total.add((BigDecimal) montoMes);
			}
		}
		mapaMontos.put("total", total);
		mapaMontos.put("currencyUomId", baseCurrencyUomId);
	}
	
	/**
	 * Rellena el mapa de los meses que no tienen movimientos en ceros, suma y agrega el total
	 * @param mapaClaves
	 * @param baseCurrencyUomId 
	 */
	private static void guardaCerosMapaClavesSumaTotal(Map<String, Object> mapaClaves, String baseCurrencyUomId){
		for (String mesId : listaMeses) {
			if(UtilValidate.isEmpty(mapaClaves.get("M"+mesId))){
				mapaClaves.put("M"+mesId, BigDecimal.ZERO);
			}
		}
		sumaTotal(mapaClaves, baseCurrencyUomId);
	}

	/**
	 * Metodo para consultar claves presupuestales
	 * @param context
	 * @throws GeneralException
	 * @throws ParseException
	 */
	public static void buscaClaves(Map<String, Object> context)
			throws GeneralException, ParseException {

		final ActionContext ac = new ActionContext(context);
		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
		String cicloId = UtilCommon.getCicloId(ac.getRequest());

		Delegator delegator = ac.getDelegator();
		List<Map<String, Object>> tiposClave = new FastList<Map<String, Object>>();
		Map<String, Object> tipos = new HashMap<String, Object>();
		tipos.put("id", "INGRESO");
		tiposClave.add(tipos);
		tipos = new HashMap<String, Object>();
		tipos.put("id", "EGRESO");
		tiposClave.add(tipos);
		ac.put("tiposClave", tiposClave);

		if (ac.getParameter("ingresoEgreso") == null
				|| ac.getParameter("ingresoEgreso").equalsIgnoreCase("Ingreso")) {
			ac.put("tagTypes", UtilAccountingTags
					.getClassificationTagsForOrganization(organizationPartyId,cicloId,UtilAccountingTags.INGRESO_TAG, delegator));
			ac.put("ingresoEgreso", "Ingreso");
		} else {
			ac.put("tagTypes", UtilAccountingTags
					.getClassificationTagsForOrganization(organizationPartyId,cicloId,UtilAccountingTags.EGRESO_TAG, delegator));
			ac.put("ingresoEgreso", "Egreso");
		}

		if ("Y".equals(ac.getParameter("performFind"))) {
			// build search conditions
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();
			searchConditions.add(EntityCondition.makeCondition("tipo",
					EntityOperator.EQUALS, ac.getParameter("ingresoEgreso")));

			for (int i = 1; i < 16; i++) {
				String clasif = (String) ac.getParameter("clasificacion" + i);
				Debug.log("clasificacion" + i + ".- " + clasif);
				if (clasif != null && !clasif.isEmpty())
					// cvePres.add(clasif);
					searchConditions
					.add(EntityCondition.makeCondition("clasificacion"
							+ i, EntityOperator.EQUALS, clasif));
			}

			// buscamos las claves segun las condiciones de la clave
			// presupuestal.
			List<GenericValue> clavesPresList = delegator.findByAnd(
					"ClavePresupuestal", searchConditions);

			ac.put("clavesPresList", clavesPresList);

		}
	}

	public static GenericValue getEstructuraClave(Delegator delegator,
			String ciclo, String tipoClave) throws GenericEntityException {
		List<GenericValue> estructura = delegator
				.findByAndCache("EstructuraClave", UtilMisc.toMap("ciclo", ciclo,
						"acctgTagUsageTypeId", tipoClave));
		if (estructura.isEmpty()) {
			Debug.log("Ocurrio algo raro");
			return null;
		} else {
			return estructura.get(0);
		}
	}
	
	public static void buscaClavesTipoCambio(Map<String, Object> context)
			throws GeneralException, ParseException {

		final ActionContext ac = new ActionContext(context);
		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
		String ciclo = UtilCommon.getCicloId(ac.getRequest());

		Delegator delegator = ac.getDelegator();
		List<Map<String, Object>> tiposClave = new FastList<Map<String, Object>>();
		Map<String, Object> tipos = new HashMap<String, Object>();		
		tipos.put("id", "Egreso");
		tiposClave.add(tipos);
		ac.put("tiposClave", tiposClave);

		ac.put("tagTypes", UtilAccountingTags.getClassificationTagsForOrganization(organizationPartyId,ciclo, UtilAccountingTags.EGRESO_TAG, delegator));
		ac.put("ingresoEgreso", "Egreso");
		
		// build search conditions
		List<EntityCondition> searchConditions = new FastList<EntityCondition>();
		searchConditions.add(EntityCondition.makeCondition("tipo",EntityOperator.EQUALS, "EGRESO"));		

		// buscamos las claves segun las condiciones de la clave
		// presupuestal.
		List<GenericValue> clavesPresList = delegator.findByAnd(
				"ClavePresupuestalTipoCambio", searchConditions);

		Debug.log("clavesPresList: " + clavesPresList);
		ac.put("clavesPresList", clavesPresList);
			

	}

}
