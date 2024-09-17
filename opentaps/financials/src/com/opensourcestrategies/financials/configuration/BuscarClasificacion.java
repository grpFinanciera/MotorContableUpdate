package com.opensourcestrategies.financials.configuration;

import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.opentaps.base.entities.EnumerationType;
import org.opentaps.common.util.UtilAccountingTags;
import org.opentaps.common.util.UtilClassification;
import org.opentaps.foundation.action.ActionContext;
import org.opentaps.foundation.repository.RepositoryException;

public class BuscarClasificacion {
	
	private static String MODULE = BuscarClasificacion.class.getName();
	
	/**
	 * Busqueda de clasificaciones por tipo
	 * @param context
	 */
	public static void buscarClasificacion(Map<String, Object> context){
		
    	final ActionContext ac = new ActionContext(context);
    	Delegator delegator = ac.getDelegator();
		
		String performFind = (String) ac.getParameter("performFind");
		String enumTypeId = (String) ac.getParameter("enumTypeId");
		
		String enumTypeDescription = new String();
		
		List<GenericValue> listNivelPresupuestal = FastList.newInstance();
		
		List<Map<String,Object>> listClasificacionBuilder = FastList.newInstance();
		
		List<EnumerationType> tagTypes = FastList.newInstance();
		
		try {
		
			tagTypes = UtilAccountingTags.getAllAccoutingTagEnumerationTypes(delegator);
			
			if (!"Y".equals(performFind)) {
				if(UtilValidate.isNotEmpty(tagTypes)){
					enumTypeId = tagTypes.get(0).getString("enumTypeId");
				}
			}
			
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();
			
			if (UtilValidate.isNotEmpty(enumTypeId)) {
				searchConditions.add(EntityCondition.makeCondition("enumTypeId", enumTypeId));
				
		        EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
		        Debug.logInfo("condiciones: " + condiciones , MODULE);
		        
					List<String> fieldsToSelect = UtilMisc.toList("enumId","enumTypeId","sequenceId","enumCode","description",
											"nivelId","parentEnumId","fechaInicio","fechaFin");
					
					List<String> orderBy = UtilMisc.toList("enumId");
					
					List<GenericValue> listEnumeration = delegator.findByCondition("Enumeration", condiciones, fieldsToSelect, orderBy);
					
					Map<String,Object> mapaEnumeration = FastMap.newInstance();
					for (GenericValue Enumeration : listEnumeration) {
						mapaEnumeration.putAll(Enumeration.getAllFields());
						listClasificacionBuilder.add(mapaEnumeration);
						mapaEnumeration = FastMap.newInstance();
					}
					
					GenericValue EnumerationType = delegator.findByPrimaryKey("EnumerationType",UtilMisc.toMap("enumTypeId",enumTypeId));
					if(UtilValidate.isNotEmpty(EnumerationType)){
						enumTypeDescription = EnumerationType.getString("description");
					}
					
					listNivelPresupuestal = UtilClassification.obtenerNivelPresupuestal(delegator,enumTypeId);
			}
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		
		ac.put("tagTypes",tagTypes);
		ac.put("listNivelPresupuestal", listNivelPresupuestal);
		ac.put("listClasificacionBuilder", listClasificacionBuilder);
		ac.put("enumTypeDescription", enumTypeDescription);
		
	}

}
