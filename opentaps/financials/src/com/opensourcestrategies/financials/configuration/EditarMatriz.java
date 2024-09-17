package com.opensourcestrategies.financials.configuration;

import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

public class EditarMatriz {
	
	/**
	 * Modificar Matriz Conversion
	 * @param dctx
	 * @param context
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> actualizarMatriz(DispatchContext dctx, Map<?, ?> context) 
	{	Delegator delegator = dctx.getDelegator();

        Map result = ServiceUtil.returnSuccess();

        String tipoMatriz = (String) context.get("tipoMatriz");
        String matrizId = (String) context.get("matrizId");
        String nombreCog = (String) context.get("nombreCog");
        String nombreCri = (String) context.get("nombreCri");
        String cog = (String) context.get("cog");
        String cri = (String) context.get("cri");
        String tipoGasto = (String) context.get("tipoGasto");
        String cargo = (String) context.get("cargo");
        String abono = (String) context.get("abono");
        Map matrizIdMap = (Map) context.get("matrizIdMap");
        Map cogMap = (Map) context.get("cogMap");
        Map criMap = (Map) context.get("criMap");
        Map tipoGastoMap = (Map) context.get("tipoGastoMap");
        Map matrizIdOriginal = (Map) context.get("matrizIdOriginal");
        Map cogOriginal = (Map) context.get("cogOriginal");
        Map criOriginal = (Map) context.get("criOriginal");
        Map tipoGastoOriginal = (Map) context.get("tipoGastoOriginal");
        Map caracteristicasMap = (Map) context.get("caracteristicasMap");
        Map medioPagoMap = (Map) context.get("medioPagoMap");
        Map cargoMap = (Map) context.get("cargoMap");
        Map abonoMap = (Map) context.get("abonoMap");
        Map matrizEgresoId = (Map) context.get("matrizEgresoId");
        Map matrizIngresoId = (Map) context.get("matrizIngresoId");                
        
        result.put("tipoMatriz", tipoMatriz);
        result.put("matrizId", matrizId);
        result.put("nombreCog", nombreCog);
        result.put("nombreCri", nombreCri);
        result.put("cog", cog);
        result.put("cri", cri);
        result.put("tipoGasto", tipoGasto);
        result.put("cargo", cargo);
        result.put("abono", abono);
        
        
        Debug.log("context: " + context);       

        try 
        {	if(tipoMatriz.equals("EGRESO"))
        	{	for(int i=0; i<matrizIdMap.size(); i++)
				{	Debug.log("Entra a EGRESO");
        			String matrizIdFor = matrizIdMap.get(Integer.toString(i)).toString();
					String cogFor = cogMap.get(Integer.toString(i)).toString();
					String tipoGastoFor = tipoGastoMap.get(Integer.toString(i)).toString();
					String matrizIdForO = matrizIdOriginal.get(Integer.toString(i)).toString();
					String cogForO = cogOriginal.get(Integer.toString(i)).toString();
					String tipoGastoForO = tipoGastoOriginal.get(Integer.toString(i)).toString();
					Debug.log("matrizIdFor: " + matrizIdFor);
					Debug.log("matrizIdForO: " + matrizIdForO);
					Debug.log("cogFor: " + cogFor);
					Debug.log("cogForO: " + cogForO);
					Debug.log("tipoGastoFor: " + cogFor);
					Debug.log("tipoGastoForO: " + tipoGastoForO);
					
					if(!matrizIdFor.equals(matrizIdForO) || !cogFor.equals(cogForO) || !tipoGastoFor.equals(tipoGastoForO)) 
					{	EntityCondition condLlave = EntityCondition.makeCondition(EntityOperator.AND,
		  										EntityCondition.makeCondition("matrizId", EntityOperator.EQUALS, matrizIdFor),
		  										EntityCondition.makeCondition("cog", EntityOperator.EQUALS, cogFor),
		  										EntityCondition.makeCondition("tipoGasto", EntityOperator.EQUALS, tipoGastoFor));					
				        List<GenericValue> llaveList = delegator.findByCondition("MatrizEgreso", condLlave, UtilMisc.toList("matrizEgresoId"), null);
				        Debug.log("llaveList: " + llaveList);
				        if(!llaveList.isEmpty())
					    {	return ServiceUtil.returnError("Ya existe un registro con los datos Matriz Id: ["+matrizIdFor+"] Cog: ["+cogFor+"] Tipo de gasto: ["+tipoGastoFor+"]");
					    }
					}
								
					GenericValue matrizEgreso = delegator.findByPrimaryKey("MatrizEgreso", UtilMisc.toMap("matrizEgresoId", matrizEgresoId.get(Integer.toString(i)).toString()));
					Debug.log("matrizEgreso: " + matrizEgreso);
				
					if(!matrizEgreso.isEmpty())
	            	{
						if(cogMap.get(Integer.toString(i)).toString().length() == 0){
							return ServiceUtil.returnError("No se ingres\u00F3 el COG");
						}
						if(tipoGastoMap.get(Integer.toString(i)).toString().length() == 0){
							return ServiceUtil.returnError("No se ingres\u00F3 el tipo de gasto");
						}
						if(cargoMap.get(Integer.toString(i)).toString().length() == 0){
							return ServiceUtil.returnError("No se ingres\u00F3 el cargo");
						}
						if(abonoMap.get(Integer.toString(i)).toString().length() == 0){
							return ServiceUtil.returnError("No se ingres\u00F3 el abono");
						}
						
						matrizEgreso.set("matrizId", matrizIdMap.get(Integer.toString(i)).toString());
	            		matrizEgreso.set("cog", cogMap.get(Integer.toString(i)).toString());
	            		matrizEgreso.set("tipoGasto", tipoGastoMap.get(Integer.toString(i)).toString());
	            		matrizEgreso.set("caracteristicas", caracteristicasMap.get(Integer.toString(i)).toString());
	            		matrizEgreso.set("medioPago", medioPagoMap.get(Integer.toString(i)).toString());
	            		matrizEgreso.set("cargo", cargoMap.get(Integer.toString(i)).toString());
	            		matrizEgreso.set("abono", abonoMap.get(Integer.toString(i)).toString());
	            		Debug.log("matrizEgresoInp: " + matrizEgreso);
	            		matrizEgreso.store();									
	            	}							
				}
        	}
			else if(tipoMatriz.equals("INGRESO"))
			{	for(int i=0; i<matrizIdMap.size(); i++)
				{	Debug.log("Entra a INGRESO");
					String matrizIdFor = matrizIdMap.get(Integer.toString(i)).toString();
					String criFor = criMap.get(Integer.toString(i)).toString();
					String matrizIdForO = matrizIdOriginal.get(Integer.toString(i)).toString();
					String criForO = criOriginal.get(Integer.toString(i)).toString();														
					
					if(!matrizIdFor.equals(matrizIdForO) || !criFor.equals(criForO)) 
					{	EntityCondition condLlave = EntityCondition.makeCondition(EntityOperator.AND,
		  										EntityCondition.makeCondition("matrizId", EntityOperator.EQUALS, matrizIdFor),
		  										EntityCondition.makeCondition("cri", EntityOperator.EQUALS, criFor));					
				        List<GenericValue> llaveList = delegator.findByCondition("MatrizIngreso", condLlave, UtilMisc.toList("matrizIngresoId"), null);
				        Debug.log("llaveList: " + llaveList);
				        if(!llaveList.isEmpty())
					    {	return ServiceUtil.returnError("Ya existe un registro con los datos Matriz Id: ["+matrizIdFor+"] Cri: ["+criFor+"]");
					    }
					}
							
					GenericValue matrizIngreso = delegator.findByPrimaryKey("MatrizIngreso", UtilMisc.toMap("matrizIngresoId", matrizIngresoId.get(Integer.toString(i)).toString()));
					Debug.log("matrizIngreso: " + matrizIngreso);
					if(!matrizIngreso.isEmpty())
		        	{	
						if(criMap.get(Integer.toString(i)).toString().length() == 0){
							return ServiceUtil.returnError("No se ingres\u00f3 el CRI");
						}
						if(cargoMap.get(Integer.toString(i)).toString().length() == 0){
							return ServiceUtil.returnError("No se ingres\u00f3 el cargo");
						}
						if(abonoMap.get(Integer.toString(i)).toString().length() == 0){
							return ServiceUtil.returnError("No se ingres\u00f3 el abono");
						}
						matrizIngreso.set("matrizId", matrizIdMap.get(Integer.toString(i)).toString());
			        	matrizIngreso.set("cri", criMap.get(Integer.toString(i)).toString());
			        	matrizIngreso.set("caracteristicas", caracteristicasMap.get(Integer.toString(i)).toString());
			        	matrizIngreso.set("medioPago", medioPagoMap.get(Integer.toString(i)).toString());
			        	matrizIngreso.set("cargo", cargoMap.get(Integer.toString(i)).toString());
			        	matrizIngreso.set("abono", abonoMap.get(Integer.toString(i)).toString());
		        		Debug.log("matrizIngresoInp: " + matrizIngreso);
		        		matrizIngreso.store();									
		        	}							
				}
			}							
        	result = ServiceUtil.returnSuccess("Los registros se han actualizado exitosamente");        				
        	return result;
        }  	
	    catch (GenericEntityException e) 
	    {	return ServiceUtil.returnError("Valores ingresados no validos. " + e.getMessage());
		}
	}
}
