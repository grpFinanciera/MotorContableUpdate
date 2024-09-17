package org.opentaps.warehouse.inventory;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.base.entities.ListaLevantamiento;
import org.opentaps.base.entities.ListaLevantamientoNoExiste;
import org.opentaps.common.builder.EntityListBuilder;
import org.opentaps.common.builder.ListBuilderException;
import org.opentaps.common.builder.PageBuilder;
import org.opentaps.domain.DomainsDirectory;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.action.ActionContext;
import org.opentaps.foundation.repository.RepositoryException;

public class ActivoFijoServices {
	/**
	 * Metodo para realizar el levantamiento de inventario de activos fijos
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String,?> agregaActivo(DispatchContext dctx, Map<String,?> context){
		
		Delegator delegator = dctx.getDelegator();
		
		Map<String, Object> result = ServiceUtil.returnSuccess("Se agrego correctamente");
		
		String fixedAssetId = (String) context.get("fixedAssetId");
		
		try {
			GenericValue FixedAsset = delegator.findByPrimaryKey("FixedAsset", UtilMisc.toMap("fixedAssetId",fixedAssetId));
			
			if(UtilValidate.isEmpty(FixedAsset)){
				throw new GenericEntityException("No se encontr\u00f3 el activo fijo");
			}
			
		GenericValue LevantamientoActivo = delegator.makeValue("LevantamientoActivo");
		LevantamientoActivo.setPKFields(context);
		
		//Se valida la existencia para mandar error
		GenericValue ListaLevantamientoBusca =  delegator.findByPrimaryKey(LevantamientoActivo.getPrimaryKey());
		if(!UtilValidate.isEmpty(ListaLevantamientoBusca)){
			throw new GenericEntityException("Ya se encuentra registrado el activo en esta ubicaci\u00f3n");
		}
		
		LevantamientoActivo.create();
		result.putAll(LevantamientoActivo.getFields(UtilMisc.toList("fixedAssetId","ubicacionRapidaId")));
		
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		
		return result;
	}
	
	/**
	 * Borra los registros del levantamiento de inventario de activo fijo
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static String borrarLevantamiento(HttpServletRequest request, HttpServletResponse response){
		final Delegator delegator = (Delegator)request.getAttribute("delegator");
		
		try {
			delegator.removeAll("LevantamientoActivo");
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return "error";
		}
		ServiceUtil.setMessages(request,null , "Se limpio el levantamiento para inicar", null);
		return "success";
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
			
			levantamientoListBuilder = new  EntityListBuilder(ledgerRepository, ListaLevantamiento.class, null, orderBy);
			
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
			
			EntityCondition condicion = EntityCondition.makeCondition("activoFijoIdLeva",EntityOperator.EQUALS,null);
			
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
}
