/* 
 */

package org.opentaps.common.util;

import java.util.List;

import javolution.util.FastList;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;

/**
 * UtilAccountingTags - Utilities for the accounting tag system.
 */
public final class UtilClassification {

    /** Number of tags defined in <code>AcctgTagEnumType</code>. */
    public static final int TAG_COUNT = 15;
    public static final String MODULE = UtilClassification.class.getName();

    private UtilClassification() { }
    
	public static List<GenericValue> getListaCatalogos(String tabla, Delegator delegator,
					String clasificacionId,boolean activos) throws GenericEntityException
	{	
		String entidad = tabla;
		String valorBusqueda = "";
		String valorId = "";
		String valorIdExtra = "";
		String valorDescripcion = "";
		String fechaInicio = "";
		String fechaFin = "";
		String state = "";
		String padre = "";
		java.sql.Date fechaActual = UtilDateTime.nowSqlDate();
		List<GenericValue> listGenericaNivelesResult = null;
		List<EntityExpr> listaCondiciones = FastList.newInstance();
		List<EntityExpr> condicionesPadre = FastList.newInstance();
		
		Debug.logInfo("Omar - Tabla: " + tabla,MODULE);
		Debug.logInfo("Omar - fechaActual: " + fechaActual,MODULE);		
		
		if(tabla.equals("Enumeration"))
		{	
			valorBusqueda = "enumTypeId";
			valorId = "enumId";
			valorIdExtra="sequenceId";
			valorDescripcion="enumCode";
			fechaInicio = "fechaInicio";
			fechaFin = "fechaFin";
			padre = "parentEnumId";
			
			Debug.logInfo("Omar - EntityCondition Else",MODULE);
			listaCondiciones.add(EntityCondition.makeCondition(valorBusqueda, EntityOperator.EQUALS, clasificacionId));
			if(activos){
				listaCondiciones.add(EntityCondition.makeCondition(fechaFin, EntityOperator.GREATER_THAN, fechaActual));
				listaCondiciones.add(EntityCondition.makeCondition(fechaInicio, EntityOperator.LESS_THAN, fechaActual));
			}
			condicionesPadre.add(EntityCondition.makeCondition(valorBusqueda, EntityOperator.EQUALS, clasificacionId));			
		}	
		else if(tabla.equals("NivelesParty"))
		{
			valorId = "partyId";
			valorIdExtra="externalId";
			valorDescripcion="groupName";
			state="state";
			padre = "Parent_id";
			
			Debug.logInfo("Omar - EntityCondition NivelesParty",MODULE);				
			listaCondiciones.add(EntityCondition.makeCondition(valorIdExtra, EntityOperator.NOT_EQUAL, null));
			listaCondiciones.add(EntityCondition.makeCondition(valorIdExtra, EntityOperator.NOT_EQUAL, ""));
				if(activos)
					listaCondiciones.add(EntityCondition.makeCondition(state, EntityOperator.EQUALS, "A"));
		}
		
		//Se crea consulta para padres.
		List<GenericValue> listPadresResult = null;
		condicionesPadre.add(EntityCondition.makeCondition(padre, EntityOperator.NOT_EQUAL, null));
		condicionesPadre.add(EntityCondition.makeCondition(padre, EntityOperator.NOT_EQUAL, ""));
						
		listPadresResult = delegator.findByCondition(entidad, EntityCondition.makeCondition(condicionesPadre,EntityOperator.AND), null,
				UtilMisc.toList(padre), UtilMisc.toList(padre), UtilCommon.DISTINCT_READ_OPTIONS);
		//Debug.log("Lista padres:" + listPadresResult);
		List<String> cuentasPadre = EntityUtil.getFieldListFromEntityList(listPadresResult, padre, true);
		
		if(!listPadresResult.isEmpty()){
		listaCondiciones.add(EntityCondition.makeCondition(valorId, EntityOperator.NOT_IN, cuentasPadre));
		}
		
		listGenericaNivelesResult = delegator.findByCondition(entidad,
						EntityCondition.makeCondition(listaCondiciones,EntityOperator.AND),
						UtilMisc.toList(valorId,valorIdExtra,
								valorDescripcion), UtilMisc.toList(valorIdExtra,valorDescripcion));				
		
		if(listGenericaNivelesResult.isEmpty())
		{	
			Debug.logWarning("Omar - LA LISTA ESTA VACIA!!!",MODULE);			
		}
		return listGenericaNivelesResult;	
	}
    
	/**
	 * Metodo que obtiene el nivel presupuestal por tipo de clasificacion
	 * @param delegator
	 * @return
	 * @throws GenericEntityException
	 */
	public static List<GenericValue> obtenerNivelPresupuestal(Delegator delegator,String tipoClasificacion) throws GenericEntityException{
		
		return delegator.findByAnd("NivelPresupuestal",UtilMisc.toMap("clasificacionId",tipoClasificacion));
		
	}
    
}
