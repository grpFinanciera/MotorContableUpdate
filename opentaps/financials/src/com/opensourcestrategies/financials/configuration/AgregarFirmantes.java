package com.opensourcestrategies.financials.configuration;

import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

public class AgregarFirmantes {
	
	/**
	 * Gregar firmante a un reporte
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> agregarFirmante(DispatchContext dctx,
			Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();		
		Debug.log("context: " + context);
		String reporteId = (String) context.get("reporteId");
		String firmanteId = (String) context.get("firmanteId");
		String organizationPartyId = (String) context.get("organizationPartyId");				

		boolean aceptaFirmantes = cuentaFirmantes(delegator, reporteId);
		
		
		if(aceptaFirmantes)
		{	try 
			{	EntityCondition condicion = EntityCondition.makeCondition(EntityOperator.AND,
							EntityCondition.makeCondition("reporteId", EntityOperator.EQUALS, reporteId),
							EntityCondition.makeCondition("firmanteId", EntityOperator.EQUALS, firmanteId));		
				List<GenericValue> firmantes = delegator.findByCondition("ReporteFirmante",condicion, null, null);				
				if(!firmantes.isEmpty())
				{	return ServiceUtil.returnError("El firmante seleccionado ya se encuentra registrado como firmante de este reporte");
				}
			
				GenericValue reporteFirmante = GenericValue.create(delegator.getModelEntity("ReporteFirmante"));		
				if(reporteId != null && !reporteId.isEmpty())
					reporteFirmante.set("reporteId", reporteId);
				else
					return ServiceUtil.returnError("Es necesario ingresar el identificador del reporte");
				
				reporteFirmante.set("firmanteId", firmanteId);
				reporteFirmante.set("organizationPartyId", organizationPartyId);
				Debug.log("reporteFirmante: " + reporteFirmante);
				delegator.create(reporteFirmante);
				Map<String, Object> result = ServiceUtil.returnSuccess();
				result.put("reporteId", reporteId);
				result.put("firmanteId", firmanteId);
				return result;
			} catch (GenericEntityException e) {
				return ServiceUtil.returnError(e.getMessage());
			}
		}
		else
		{	return ServiceUtil.returnError("Solo es posible registrar 3 firmantes por reporte");
			
		}
	}
	
	/**
	 * Metodo para validar que existan solamente 3 firmantes por reporte
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static boolean cuentaFirmantes(Delegator delegator, String reporteId) {			
		
		try 
		{	EntityCondition condicion = EntityCondition.makeCondition(EntityOperator.AND,
						EntityCondition.makeCondition("reporteId", EntityOperator.EQUALS, reporteId));		
			long firmantes = delegator.findCountByCondition("ReporteFirmante", condicion, null);				
			if(firmantes == 3)
			{	return false;
			}
			
			return true;
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage()) != null;
		}
	}
}
