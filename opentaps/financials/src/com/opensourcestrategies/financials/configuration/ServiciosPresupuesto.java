package com.opensourcestrategies.financials.configuration;

import java.util.Map;

import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

public class ServiciosPresupuesto {
	
	public static Map<String, Object> creaEtiqueta(
			DispatchContext dctx, Map<String, Object> context) {
		
		Map<String, Object> regreso = ServiceUtil.returnSuccess();
		
		try {
		
			GenericValue Etiqueta = dctx.getDelegator().makeValue("Etiqueta");
			Etiqueta.setAllFields(context, true, null, null);
			Etiqueta.create();
			regreso.put("etiquetaId", Etiqueta.getString("etiquetaId"));
		
		} catch (NullPointerException | GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		
		return regreso;
	}

	/**
	 * Metodo para guardar la asociacion de etiqueta presupuestal
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> guardarEtiquetaEnum(
			DispatchContext dctx, Map<String, Object> context) {
		
		Map<String, Object> regreso = ServiceUtil.returnSuccess();
		
		try {
			
			GenericValue Etiqueta = dctx.getDelegator().makeValue("EtiquetaEnumeration");
			Etiqueta.setAllFields(context, true, null, null);
			Etiqueta.create();
		
		} catch (NullPointerException | GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		
		return regreso;
	}
}