package com.opensourcestrategies.financials.configuration;

import java.util.Map;

import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.common.util.UtilMessage;

public class TipoAuxiliar
{	
	
	private static String MODULE = TipoAuxiliar.class.getName();
	
	public static Map<?,?> crearNuevoTipoAuxiliar(DispatchContext dctx, Map<?,?> context)
	{
		Delegator delegator = dctx.getDelegator();			
		String tipoAuxiliarId = (String) context.get("tipoAuxiliarId");
		String entidad = (String) context.get("entidad");
		String descripcion = (String) context.get("descripcion");
		String tipoAuxiliar = (String) context.get("tipoAuxiliar");
		String url = "";
		String lookup = "";
		String tipo = "";
		
		if (entidad.equals("Party"))
		{
			url = "getAutoCompletePartyIds";
			lookup = "LookupPartyName";
			tipo = "A";
		}
		else if (entidad.equals("CuentaBancaria"))
		{
			url = "getCuentaBancoUsuario";
			tipo = "B";
		}
		else if (entidad.equals("Product"))
		{
			url = "getAutoCompleteProduct";
			lookup = "LookupProduct";
			tipo = "P";
		}
		
		GenericValue tipoAuxiliarEntity = delegator.makeValue("TipoAuxiliar");
		tipoAuxiliarEntity.put("tipoAuxiliarId", tipoAuxiliarId);
		tipoAuxiliarEntity.put("entidad", entidad);
		if (entidad.equals("Party"))
		{
			tipoAuxiliarEntity.put("tipoEntidad", tipoAuxiliarId);
		}
		tipoAuxiliarEntity.put("descripcion", descripcion);
		tipoAuxiliarEntity.put("urlBusqueda", url);
		if (!entidad.equals("CuentaBancaria"))
		{
			tipoAuxiliarEntity.put("nombreLooukup", lookup);
		}
		tipoAuxiliarEntity.put("tipo", tipo);
		
		try {
			delegator.create(tipoAuxiliarEntity);
			if (entidad.equals("Party"))
			{
				GenericValue partyType = delegator.makeValue("PartyType");
				partyType.put("partyTypeId", tipoAuxiliarId);
				partyType.put("hasTable", "N");
				partyType.put("description", descripcion);
				if (!tipoAuxiliar.equals("N"))
				{
					partyType.put("auxiliar", "Y");
					partyType.put("tipoAuxiliar", tipoAuxiliar);
				}
				else
				{
					partyType.put("auxiliar", "N");
				}
				
				GenericValue roleType = delegator.makeValue("RoleType");
				roleType.put("roleTypeId", tipoAuxiliarId);
				roleType.put("hasTable", "N");
				roleType.put("description", descripcion);
				roleType.put("flag", "Y");
				
				delegator.create(partyType);
				delegator.create(roleType);
			}
			
		return ServiceUtil.returnSuccess("Se ha creado el tipo de auxiliar con \u00e9xito");
			
		} catch (GenericEntityException e) {
			return UtilMessage.createAndLogServiceError(e, MODULE);
		}
		
	}
}
