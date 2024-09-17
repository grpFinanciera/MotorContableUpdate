package org.opentaps.warehouse.domain.inventory;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilNumber;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.security.Security;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.common.util.UtilMessage;
import org.opentaps.foundation.action.ActionContext;
import org.opentaps.purchasing.order.ConsultarRequisicion;

import com.ibm.icu.text.DecimalFormat;

public class ServiciosAlmacen {

	private static final String MODULE = ServiciosAlmacen.class.getName();
	private static final DecimalFormat df4digitos =  new DecimalFormat("0000");
	
	/**
	 * Recibe los productos al inventario provenientes de una orden, pedido o contrato
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String,Object> recibeProductosInventario(DispatchContext dctx, Map<String, ? extends Object> context){
		
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		
		Map<String,Object> mapRetorno = ServiceUtil.returnSuccess(); 
		
		List<Map<String,Object>> listaCrear = FastList.newInstance();
		List<Map<String,Object>> listaActualizar = FastList.newInstance();
		List<GenericValue> listaInventoryItem = FastList.newInstance();
		List<GenericValue> listaInventoryItemDetail = FastList.newInstance();
		List<GenericValue> listaInventoryItemValueHistory = FastList.newInstance();
		List<GenericValue> listaShipmentReceipt = FastList.newInstance();
		Map<String,GenericValue> mapaInventoryItem = FastMap.newInstance(); 
		Map<String,Map<String,Object>> mapaValores = FastMap.newInstance(); 
		
		Map<String,Object> mapaInput = FastMap.newInstance(); 
		
		try {
		
			List<Map<String,Object>> listInventario = UtilGenerics.checkList(context.get("listInventario"));
			String facilityId = (String) context.get("facilityId");
			String currencyUomId = (String) context.get("currencyUomId");
			
			String shipmentId = new String();
			String orderId = new String();
			
			for (Map<String, Object> mapContexto : listInventario) {
				if(mapContexto.containsKey("inventoryItemId")){
					listaActualizar.add(mapContexto);
				} else {
					listaCrear.add(mapContexto);
				}
				shipmentId = (String) mapContexto.get("shipmentId");
				orderId = (String) mapContexto.get("orderId");
			}
			
			validaContextoRecepcion(listaCrear, listaActualizar, dctx, userLogin, delegator, facilityId, currencyUomId);
			listaInventoryItem.addAll(crearInventoryItems(listaCrear,delegator,listaShipmentReceipt,userLogin.getString("userLoginId"),mapaValores));
			listaInventoryItem.addAll(actualizarInventoryItems(listaActualizar,delegator,listaShipmentReceipt,userLogin.getString("userLoginId"),mapaValores));	
			mapaInventoryItem = creaMapaInventoryItem(listaInventoryItem);
			listaInventoryItemValueHistory = actualizaInventoryItemValueHistory(mapaInventoryItem,delegator,locale, userLogin.getString("userLoginId"));
			listaInventoryItemDetail = crearInventoryItemDetail(listaInventoryItem, delegator,mapaValores);
			actualizaReceiptId(listaShipmentReceipt,listaInventoryItemDetail);
			
	
			delegator.storeAll(listaInventoryItem);
			delegator.storeAll(listaShipmentReceipt);
			delegator.storeAll(listaInventoryItemDetail);
			delegator.storeAll(listaInventoryItemValueHistory);
			

			mapaInput.put("shipmentId",shipmentId);
			mapaInput.put("userLogin",userLogin);
			Map<String,Object> resultado = FastMap.newInstance();
			resultado = dctx.getDispatcher().runSync("updatePurchaseShipmentFromReceipt", mapaInput);
			
            if (ServiceUtil.isError(resultado)) {
                Debug.logError(ServiceUtil.getErrorMessage(resultado), MODULE);
                return ServiceUtil.returnError(ServiceUtil.getErrorMessage(resultado));
            }
			
			mapaInput = FastMap.newInstance(); 
			mapaInput.put("orderId",orderId);
			mapaInput.put("userLogin",userLogin);
			resultado = dctx.getDispatcher().runSync("updateOrderStatusFromReceipt", mapaInput);
			
            if (ServiceUtil.isError(resultado)) {
                Debug.logError(ServiceUtil.getErrorMessage(resultado), MODULE);
                return ServiceUtil.returnError(ServiceUtil.getErrorMessage(resultado));
            }
			
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("No fue posible recibir los productos al almac\u00e9n "+e.getMessage());
		} catch (GenericServiceException e){
			e.printStackTrace();
			return ServiceUtil.returnError("No fue posible recibir los productos al almac\u00e9n "+e.getMessage());
		}

		return mapRetorno;
	}
	
	/**
	 * Actualiza el campo ReceiptId de la tabla InventoryItemDetails
	 * @param listaShipmentReceipt
	 * @param listaInventoryItemDetail
	 */
	private static void actualizaReceiptId(
			List<GenericValue> listaShipmentReceipt,
			List<GenericValue> listaInventoryItemDetail) {
		
		Map<String,String> mapaShimpentReceipt = FastMap.newInstance();
		
		String inventoryItemId = new String();
		for (GenericValue ShipmentReceipt : listaShipmentReceipt) {
			inventoryItemId = ShipmentReceipt.getString("inventoryItemId");
			mapaShimpentReceipt.put(inventoryItemId, ShipmentReceipt.getString("receiptId"));
		}
		
		for (GenericValue InventoryItemDetail : listaInventoryItemDetail) {
			inventoryItemId = InventoryItemDetail.getString("inventoryItemId");
			InventoryItemDetail.put("receiptId", mapaShimpentReceipt.get(inventoryItemId));
		}
		
	}

	/**
	 * Crea mapa con llave inventoryItemId
	 * @param listaInventoryItems
	 * @return
	 */
	private static Map<String, GenericValue> creaMapaInventoryItem(
			List<GenericValue> listaInventoryItems) {
		
		Map<String, GenericValue> mapaInventoryItems = FastMap.newInstance();
		
		for (GenericValue InventoryItems : listaInventoryItems) {
			mapaInventoryItems.put(InventoryItems.getString("inventoryItemId"), InventoryItems);
		}
		
		return mapaInventoryItems;
	}

	/**
	 * Valida que los datos ingresados esten correctos 
	 * @param listaCrear
	 * @param listaActualizar
	 * @param dctx
	 * @param userLogin
	 * @param delegator
	 * @param facilityId
	 * @param currencyUomId
	 */
	public static void validaContextoRecepcion(List<Map<String,Object>> listaCrear, List<Map<String,Object>> listaActualizar, DispatchContext dctx, 
			GenericValue userLogin, Delegator delegator, String facilityId, String currencyUomId) throws GenericEntityException{
		
		Security security = dctx.getSecurity();
		
		
		if(listaCrear.size() == 0 && listaActualizar.size() == 0){			
			throw new GenericEntityException("No se recibio ningun elemento");			
		}
		
		if(listaCrear.size() != 0){
			if (!(security.hasEntityPermission("CATALOG", "_CREATE", userLogin) || (security.hasEntityPermission("FACILITY", "_CREATE", userLogin)))) {
				throw new GenericEntityException("Para poder crear usted debe tener los permisos CATALOG_CREATE, CATALOG_ADMIN, FACILITY_CREATE, o FACILITY_ADMIN.");	
	        }
		}
		
		if(listaActualizar.size() != 0){
			
			if (!(security.hasEntityPermission("CATALOG", "_UPDATE", userLogin) || (security.hasEntityPermission("FACILITY", "_UPDATE", userLogin)))) {
				throw new GenericEntityException("Para poder actualizar usted debe tener los permisos CATALOG_UPDATE, CATALOG_ADMIN, FACILITY_UPDATE, o FACILITY_ADMIN.");	
	        }
			
		}
		
		GenericValue facility = delegator.findByPrimaryKey("Facility", UtilMisc.toMap("facilityId", facilityId));
		
		if(UtilValidate.isEmpty(facilityId) || UtilValidate.isEmpty(facility)){
			throw new GenericEntityException("No se encuentra el almacen");
		}
		
		if(UtilValidate.isEmpty(currencyUomId)){
			
			GenericValue acctgPreference = delegator.findByPrimaryKey("PartyAcctgPreference", UtilMisc.toMap("partyId", facility.getString("ownerPartyId")));
			
			if(acctgPreference != null){
				currencyUomId = acctgPreference.getString("baseCurrencyUomId");
			}
			
			if(UtilValidate.isEmpty(currencyUomId)){
				currencyUomId = UtilProperties.getPropertyValue("general", "currency.uom.id.default");
			}
			
			if(UtilValidate.isEmpty(currencyUomId)){
				throw new GenericEntityException("No se encuentra la moneda");
			}
			
		}
		
		List<String> listProductId = new ArrayList <String>(); 
			
		for(Map <String,Object> crear: listaCrear ){
			
			crear.put("facilityId", facilityId);
			crear.put("currencyUomId", currencyUomId);
			crear.put("ownerPartyId", facility.getString("ownerPartyId"));
			
			if(UtilValidate.isEmpty(crear.get("unitCost"))){
				listProductId.add((String) crear.get("productId"));
			}
		}			
		
		if(UtilValidate.isNotEmpty(listProductId)){
			obtenerPreciosUnitariosProducto(listaCrear, listProductId, currencyUomId, delegator);
		}
		
		for(Map <String,Object> actualizar: listaActualizar ){
			
				actualizar.put("facilityId", facilityId);
				actualizar.put("currencyUomId", currencyUomId);
				actualizar.put("ownerPartyId", facility.getString("ownerPartyId"));
			
		}		
	}
	
	/**
	 * Obtiene y guarda el precio unitario de un producto si este no es ingresado por el usuario
	 * @param listaCrear
	 * @param listProductId
	 * @param moneda
	 */
	public static void obtenerPreciosUnitariosProducto(List<Map<String,Object>> listaCrear, List<String> listProductId, 
			String moneda, Delegator delegator) throws GenericEntityException{
		
		Timestamp fechaActual = UtilDateTime.nowTimestamp();
		
        EntityConditionList<EntityExpr> searchConditionsAND = EntityCondition.makeCondition(UtilMisc.toList(
                EntityCondition.makeCondition("productId", EntityOperator.IN, listProductId),
                EntityCondition.makeCondition("currencyUomId", EntityOperator.EQUALS, moneda),
                EntityCondition.makeCondition("availableFromDate", EntityOperator.GREATER_THAN, fechaActual)), EntityOperator.AND);
		
        EntityConditionList<EntityExpr> searchConditionsOR = EntityCondition.makeCondition(UtilMisc.toList(
                EntityCondition.makeCondition("availableThruDate", EntityOperator.LESS_THAN, fechaActual),
                EntityCondition.makeCondition("availableThruDate", EntityOperator.EQUALS, null)), EntityOperator.OR);
		
		EntityCondition condiciones = EntityCondition.makeCondition(UtilMisc.toList(searchConditionsOR, searchConditionsAND),EntityOperator.AND);
		
		List<String> fieldsToSelect = UtilMisc.toList("productId","lastPrice");
		
			List<GenericValue> supplierProduct = delegator.findByCondition("SupplierProduct", condiciones, fieldsToSelect, null);
			
			Map<String, Object> costos = FastMap.newInstance();
			
			for (GenericValue supplier : supplierProduct){				
				costos.put(supplier.getString("productId"), supplier.getString("lastPrice"));				
			}
			
			for(Map <String,Object> crear: listaCrear ){
				
				if(UtilValidate.isEmpty(crear.get("unitCost"))){
					
					crear.put("unitCost", costos.get(crear.get("productId")));
					
				}				
			}	
	}
	
	/**
	 * Genera y regresa una lista de nuevos InventoryItem para ser guardados 
	 * Al mismo tiempo se genera la lista de ShipmentReceipt's
	 * @param listaCrear
	 * @param string 
	 * @param listaShipmentReceipt 
	 * @param mapaValores 
	 * @return
	 */
	public static List<GenericValue> crearInventoryItems(List<Map<String,Object>> listaCrear, 
							Delegator delegator, List<GenericValue> listaShipmentReceipt, 
							String userLoginId, Map<String, Map<String, Object>> mapaValores){
		
		List<GenericValue> listInventoryItem = FastList.newInstance(); 
		
		for (Map<String, Object> mapInventoryItem : listaCrear) {
			GenericValue InventoryItem = delegator.makeValue("InventoryItem");
			InventoryItem.setNextSeqId();
			InventoryItem.setPKFields(mapInventoryItem);
			InventoryItem.setNonPKFields(mapInventoryItem);
			InventoryItem.set("quantityOnHandTotal", mapInventoryItem.get("quantity"));
			InventoryItem.set("availableToPromiseTotal", mapInventoryItem.get("quantity"));
			listInventoryItem.add(InventoryItem);
			listaShipmentReceipt.add(creaShipmentReceipt(InventoryItem, mapInventoryItem, delegator,userLoginId));
			mapaValores.put(InventoryItem.getString("inventoryItemId"), mapInventoryItem);
		}
		
		return listInventoryItem;
	}
	
	/**
	 * Actualiza datos de InventoryItem y regresa la lista actualizada
	 * Al mismo tiempo se genera la lista de ShipmentReceipt's
	 * @param listaActualizar
	 * @param string 
	 * @param listaShipmentReceipt 
	 * @param mapaValores 
	 * @return
	 */
	public static List<GenericValue> actualizarInventoryItems(List<Map<String,Object>> listaActualizar,
									Delegator delegator, List<GenericValue> listaShipmentReceipt, 
									String userLoginId, Map<String, Map<String, Object>> mapaValores){
		
		List<GenericValue> listInventoryItem = FastList.newInstance(); 
		
		for (Map<String, Object> mapInventoryItem : listaActualizar) {
			GenericValue InventoryItem = delegator.makeValue("InventoryItem");
			InventoryItem.setPKFields(mapInventoryItem);
			InventoryItem.setNonPKFields(mapInventoryItem);
			InventoryItem.set("quantityOnHandTotal", mapInventoryItem.get("quantity"));
			InventoryItem.set("availableToPromiseTotal", mapInventoryItem.get("quantity"));
			listInventoryItem.add(InventoryItem);
			listaShipmentReceipt.add(creaShipmentReceipt(InventoryItem, mapInventoryItem, delegator,userLoginId));
			mapaValores.put(InventoryItem.getString("inventoryItemId"), mapInventoryItem);
		}
		
		return listInventoryItem;
	}
	
	/**
	 * Crea un nuevo ShipmentReceipt , guarda sus valores
	 * @param InventoryItem
	 * @param contextoShipmentReceipt
	 */
	private static GenericValue creaShipmentReceipt(GenericValue InventoryItem, Map<String,Object> contextoShipmentReceipt,Delegator delegator, String userLoginId){
		
		GenericValue ShipmentReceipt = delegator.makeValue("ShipmentReceipt");
		ShipmentReceipt.setNextSeqId();
		ShipmentReceipt.setNonPKFields(InventoryItem);
		ShipmentReceipt.setNonPKFields(contextoShipmentReceipt);
		if(UtilValidate.isEmpty(InventoryItem.getTimestamp("datetimeReceived"))){
			ShipmentReceipt.set("datetimeReceived", UtilDateTime.nowTimestamp());
		} 
		ShipmentReceipt.set("receivedByUserLoginId", userLoginId);
		
		return ShipmentReceipt;
	}
	
	/**
	 * Actualiza datos de InventoryItemValueHistory y regresa la lista actualizada
	 * @param listaActualizar
	 * @return
	 * @throws GenericEntityException 
	 */
	public static List<GenericValue> actualizaInventoryItemValueHistory(Map<String,GenericValue> mapaInventoryItem, Delegator delegator, Locale locale, String userLoginId) throws GenericEntityException{
		
		Map<String,Object> contextoMensaje = FastMap.newInstance();
		
		Map<String,GenericValue> mapaInventoryItemNoEnHistory = FastMap.newInstance();
		mapaInventoryItemNoEnHistory.putAll(mapaInventoryItem);
		
		List<GenericValue> listItemValueHistory = delegator.findByAnd("InventoryItemValueHistory", 
				EntityCondition.makeCondition("inventoryItemId",EntityOperator.IN,new ArrayList<String>(mapaInventoryItem.keySet())));
		
		String inventoryItemId = new String();
		BigDecimal preciUnitarioAntiguo = BigDecimal.ZERO;
		BigDecimal preciUnitarioNuevo = BigDecimal.ZERO;
		for (GenericValue ItemValueHistory : listItemValueHistory) {
			inventoryItemId = ItemValueHistory.getString("inventoryItemId");
			mapaInventoryItemNoEnHistory.remove(inventoryItemId);
			preciUnitarioAntiguo = UtilNumber.getBigDecimal(ItemValueHistory.getBigDecimal("unitCost"));
			preciUnitarioNuevo = UtilNumber.getBigDecimal(mapaInventoryItem.get(inventoryItemId).getBigDecimal("unitCost"));
			
			if(preciUnitarioAntiguo.compareTo(preciUnitarioNuevo) != 0){
				ItemValueHistory.set("unitCost", preciUnitarioNuevo);
			} else {
				contextoMensaje.put("inventoryItemId", inventoryItemId);
				UtilMessage.logServiceInfo("OpentapsError_InventoryItemValueHistory_UnitCostNotChanged", contextoMensaje, locale, MODULE);
				contextoMensaje = FastMap.newInstance();
			}
		}

		GenericValue InventoryItem = null;
		//Se crean los valores que no se encontraron en la tabla
		for (Map.Entry<String,GenericValue> entryInventoryItem : mapaInventoryItemNoEnHistory.entrySet()) {
			InventoryItem = entryInventoryItem.getValue();
			GenericValue InventoryItemValueHistory = delegator.makeValue("InventoryItemValueHistory");
			InventoryItemValueHistory.setNextSeqId();
			InventoryItemValueHistory.setNonPKFields(InventoryItem);
			InventoryItemValueHistory.set("dateTime", UtilDateTime.nowTimestamp());
			InventoryItemValueHistory.set("unitCost", InventoryItem.getBigDecimal("unitCost"));
			InventoryItemValueHistory.set("setByUserLogin", userLoginId);
			listItemValueHistory.add(InventoryItemValueHistory);
		}
		
		return listItemValueHistory;
	}
	
	/**
	 * Genera y regresa una lista de nuevos InventoryItemDetail para ser guardados
	 * @param listaInventoryItems
	 * @return
	 */
	public static List<GenericValue> crearInventoryItemDetail(List<GenericValue> listaInventoryItems, Delegator delegator,
					Map<String,Map<String,Object>> mapaValores){
		
		List<GenericValue> listInventoyItemDetail = FastList.newInstance();
		
		for (GenericValue InventoryItem : listaInventoryItems) {
			GenericValue InventoryItemDetail = delegator.makeValue("InventoryItemDetail");
			InventoryItemDetail.set("inventoryItemDetailSeqId", df4digitos.format(1));
			InventoryItemDetail.set("effectiveDate", UtilDateTime.nowTimestamp());
			InventoryItemDetail.set("fechaContable", UtilDateTime.nowTimestamp());
			InventoryItemDetail.setPKFields(InventoryItem);
			InventoryItemDetail.setNonPKFields(InventoryItem);
			InventoryItemDetail.setNonPKFields(mapaValores.get(InventoryItem.getString("inventoryItemId")));
			
			listInventoyItemDetail.add(InventoryItemDetail);
		}
		
		return listInventoyItemDetail;
	}
	
	/**
	 * Metodo que sirve para modificar la fecha de recepcion de un pedido o contrato
	 * @param context
	 */
	public static void actualizaFechaRecepcion(Map<String, Object> context) {
		
		try{
		
			ActionContext actionContext = new ActionContext(context);
			Delegator delegator = actionContext.getDelegator();
			String orderId = actionContext.getParameter("orderId");
			
			GenericValue OrderHeader = EntityUtil.getFirst(delegator.findByAnd("OrderHeader", "statusId","ORDER_APPROVED"));
			
			if(UtilValidate.isNotEmpty(OrderHeader)){
				
				List<GenericValue> listOrderItem = delegator.findByCondition("OrderItem", EntityCondition.makeCondition("orderId", orderId), null, UtilMisc.toList("orderItemSeqId"));
				
				String paremetroPeriodo = new String();
				for (GenericValue OrderItem : listOrderItem) {
					paremetroPeriodo = actionContext.getParameter("customTimePeriodId_"+OrderItem.getString("orderItemSeqId"));
					
					if(UtilValidate.isNotEmpty(paremetroPeriodo)){
						OrderItem.set("customTimePeriodId", paremetroPeriodo);
					}
				}
				
				//Se guardan todos los cambios
				delegator.storeAll(listOrderItem);
				
				actionContext.put("listOrderItem", listOrderItem);
			}
			
			String clicloId = UtilCommon.getCicloId(actionContext.getRequest());
			LinkedHashSet<String> listCiclos = new LinkedHashSet<String>();
			listCiclos.add(clicloId);
			actionContext.put("orderId", orderId);
			actionContext.put("cicloId", clicloId);
			actionContext.put("mapMesesXCiclo", ConsultarRequisicion.getMapaMesesXCiclo(listCiclos, delegator, actionContext.getTimeZone(), actionContext.getLocale()));
		
		} catch(NullPointerException | GenericEntityException e){
			e.printStackTrace();
		}
	}
	
}
