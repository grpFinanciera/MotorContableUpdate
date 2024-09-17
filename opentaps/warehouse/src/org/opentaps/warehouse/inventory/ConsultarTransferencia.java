package org.opentaps.warehouse.inventory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.common.AB.UtilWorkflow;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.opentaps.common.party.PartyHelper;
import org.opentaps.foundation.action.ActionContext;

public class ConsultarTransferencia {

	/**
	 * Metodo para consultar las solicitudes de transferencia 
	 * @param context
	 * @throws GenericEntityException
	 */
	public static void buscaSolicitudTransferencia(Map<String, Object> context) throws GenericEntityException{
		
		final ActionContext ac = new ActionContext(context);
		final Delegator delegator = ac.getDelegator();
		
		String solicitudTransferenciaId = ac.getParameter("solicitudTransferenciaId");
		String filtroId = ac.getParameter("filtroId");
		String estatusId = ac.getParameter("statusId");
		String productId = ac.getParameter("productId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String usuarioPartyId = null;

		if (UtilValidate.isNotEmpty(filtroId)){
			usuarioPartyId = userLogin.getString("partyId");	
		}
		
		List<GenericValue> listStatus = delegator.findByAnd("Estatus",UtilMisc.toMap("tipo","T"));
		ac.put("estatusList", listStatus);
		
		List<Map<String,Object>> listTransferencia = FastList.newInstance();
		
		if ("Y".equals(ac.getParameter("performFind"))) {
			// build search conditions
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();

			if (UtilValidate.isNotEmpty(solicitudTransferenciaId)) {
				searchConditions.add(EntityCondition.makeCondition("solicitudTransferenciaId", EntityOperator.EQUALS,solicitudTransferenciaId));
			}
			if (UtilValidate.isNotEmpty(estatusId)) {
				searchConditions.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS,estatusId));
			}
			if (UtilValidate.isNotEmpty(productId)) {
				searchConditions.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS,productId));
			}
			if (UtilValidate.isNotEmpty(usuarioPartyId)) {
				searchConditions.add(EntityCondition.makeCondition("personaSolicitanteId", EntityOperator.EQUALS,usuarioPartyId));
			}
			
			List<String> fieldsToSelect = UtilMisc.toList("solicitudTransferenciaId","descripcion","fechaAutorizacion","firstName","lastName","partyId");
			List<String> orderBy = UtilMisc.toList("solicitudTransferenciaId");
			
			List<GenericValue> listSolicitudTrans = delegator.findByCondition("ConsultaSolicitudTransferencia", EntityCondition.makeCondition(searchConditions),fieldsToSelect,orderBy);
			
			Map<String,Object> mapaSolicitud = FastMap.newInstance();
			for (GenericValue SolicitudTrans : listSolicitudTrans) {
				mapaSolicitud.put("solicitudTransferenciaId", Long.valueOf(SolicitudTrans.getString("solicitudTransferenciaId")));
				mapaSolicitud.put("descripcion", SolicitudTrans.getString("descripcion"));
				mapaSolicitud.put("fechaAutorizacion", SolicitudTrans.getTimestamp("fechaAutorizacion"));
				mapaSolicitud.put("personaNameDesc",PartyHelper.getCrmsfaPartyName(SolicitudTrans));
				listTransferencia.add(mapaSolicitud);
				mapaSolicitud = FastMap.newInstance();
			}
			
			Collections.sort(listTransferencia, mapComparator);
			
		}
		
		ac.put("listTransferencia", listTransferencia);
		
	}
	
	/**
	 * Busqueda de transferencias pendientes por aprobar
	 * @param context
	 * @throws GenericEntityException 
	 */
	public static void pendientesTransferir(Map<String, Object> context) throws GenericEntityException{
		
		final ActionContext ac = new ActionContext(context);
		final Delegator delegator = ac.getDelegator();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String autorizadorPartyId = userLogin.getString("partyId");
		
		List<EntityCondition> searchConditions = new FastList<EntityCondition>();
		searchConditions.add(EntityCondition.makeCondition("personParentId",EntityOperator.EQUALS, autorizadorPartyId));
		searchConditions.add(EntityCondition.makeCondition("statusId",EntityOperator.EQUALS, UtilWorkflow.estatusPendiente));
		searchConditions.add(EntityCondition.makeCondition("tipoWorkFlowId",EntityOperator.EQUALS, UtilWorkflow.tipoTransferencia));
		
		List<String> fieldsToSelect = UtilMisc.toList("solicitudTransferenciaId","descripcion","firstName","lastName","partyId","descripcionEstatus");
		
		List<GenericValue> listSolicitudes = delegator.findByCondition("SolicitudesTransferenciaPendientes", EntityCondition.makeCondition(searchConditions), fieldsToSelect, null);
		
		List<Map<String,Object>> listTransferenciasPendientes = FastList.newInstance();
		
		Map<String,Object> mapaSolicitud = FastMap.newInstance();
		for (GenericValue SolicitudeTransferenciaPendiente : listSolicitudes) {
			mapaSolicitud.put("solicitudTransferenciaId", Long.valueOf(SolicitudeTransferenciaPendiente.getString("solicitudTransferenciaId")));
			mapaSolicitud.put("descripcion", SolicitudeTransferenciaPendiente.getString("descripcion"));
			mapaSolicitud.put("personaNameDesc",PartyHelper.getCrmsfaPartyName(SolicitudeTransferenciaPendiente));
			mapaSolicitud.put("descripcionEstatus",SolicitudeTransferenciaPendiente.getString("descripcionEstatus"));
			listTransferenciasPendientes.add(mapaSolicitud);
			mapaSolicitud = FastMap.newInstance();
		}
		
		Collections.sort(listTransferenciasPendientes, mapComparator);
		
		ac.put("listTransferenciasPendientes", listTransferenciasPendientes);
	}
	
	/*
	 * Metodo para ordenar la lista por medio de el ids
	 */
	private static Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
	    public int compare(Map<String, Object> m1, Map<String, Object> m2) {
	        return ((Long) m1.get("solicitudTransferenciaId")).compareTo((Long) m2.get("solicitudTransferenciaId"));
	    }
	};
	
}
