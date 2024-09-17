package org.opentaps.warehouse.domain.inventory;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.commons.lang.math.NumberUtils;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilNumber;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

public class ServiciosInventario {

	
	/**
	 * Obtiene los productos disponobles en inventaro, de una orden
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> getProductosInventarioXPedido(DispatchContext dctx, Map<String, ? extends Object> context){
		
        Delegator delegator = dctx.getDelegator();
        List<GenericValue> orderItems = UtilGenerics.checkList(context.get("orderItems"));
        String facilityId = (String) context.get("facilityId");
        Map<String, BigDecimal> atpMap = FastMap.newInstance();
        Map<String, BigDecimal> qohMap = FastMap.newInstance();
        Map<String, BigDecimal> mktgPkgAtpMap = FastMap.newInstance();
        Map<String, BigDecimal> mktgPkgQohMap = FastMap.newInstance();
        Map<String, Object> results = ServiceUtil.returnSuccess();
        
        LinkedHashSet<String> listProductos = new LinkedHashSet<String>();
        
        for (GenericValue OrderItem : orderItems) {
        	if(UtilValidate.isNotEmpty(OrderItem.getString("productId"))){
        		listProductos.add(OrderItem.getString("productId"));
        	}
		}
        
        
        List<String> select = UtilMisc.toList("productId","availableToPromiseTotal","quantityOnHandTotal");
        List<String> orderBy = UtilMisc.toList("productId");
        EntityCondition condiciones = EntityCondition.makeCondition("productId",EntityOperator.IN,listProductos);
        
        if(UtilValidate.isNotEmpty(facilityId)){
        	condiciones = EntityCondition.makeCondition(EntityOperator.AND,
        			EntityCondition.makeCondition("facilityId",facilityId),
        			condiciones);
        }
        
        try {
			List<GenericValue> listProductoInventario = delegator.findByCondition("ProductoInventarioOrder", condiciones, select, orderBy);
			
			String productId = new String();
			
			for (GenericValue ProductoInventario : listProductoInventario) {
				productId = ProductoInventario.getString("productId");
				atpMap.put(productId, UtilNumber.getBigDecimal(ProductoInventario.getBigDecimal("availableToPromiseTotal")));
				qohMap.put(productId, UtilNumber.getBigDecimal(ProductoInventario.getBigDecimal("quantityOnHandTotal")));
			}
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
        
        results.put("availableToPromiseMap", atpMap);
        results.put("quantityOnHandMap", qohMap);
        //FALTA implementar la busqueda de este mapa con los productos hijos del tipo MARKETING_PKG
        results.put("mktgPkgATPMap", mktgPkgAtpMap);
        results.put("mktgPkgQOHMap", mktgPkgQohMap);
		
		return results;
	}
	
	/**
	 * Busca el inventario
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> buscarInventoryItem(DispatchContext dctx, Map<String, ? extends Object> context){
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> resultado = ServiceUtil.returnSuccess();
		
		String performFind = (String) context.get("performFind");
		String locationSeqId = (String) context.get("locationSeqId");
		String productId = (String) context.get("productId");
		String internalName = (String) context.get("internalName");
		String serialNumber = (String) context.get("serialNumber");
		String lotId = (String) context.get("lotId");
		String facilityId = (String) context.get("facilityId");
		
		
		try {
			
			GenericValue Facility = delegator.findByPrimaryKey("Facility",UtilMisc.toMap("facilityId",facilityId));
			if(UtilValidate.isNotEmpty(Facility)){
				GenericValue PartyGroup = delegator.findByPrimaryKey("PartyGroup",UtilMisc.toMap("partyId",Facility.getString("ownerPartyId")));
				String organizationName = PartyGroup.getString("groupName");
				
				resultado.put("organizationName", organizationName);
			}

			if (UtilValidate.isEmpty(performFind)) {
			    return resultado;
			}
			
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();
			
			searchConditions.add(EntityCondition.makeCondition("facilityId", EntityOperator.EQUALS, facilityId));
			
			if (UtilValidate.isNotEmpty(productId)) {
			    searchConditions.add(EntityCondition.makeCondition("productId", EntityOperator.LIKE, "%"+ productId + "%"));
			}
			if (UtilValidate.isNotEmpty(internalName)) {
			    searchConditions.add(EntityCondition.makeCondition("internalName", EntityOperator.LIKE, "%"+ internalName + "%"));
			}
			if (UtilValidate.isNotEmpty(serialNumber)) {
			    searchConditions.add(EntityCondition.makeCondition("serialNumber", EntityOperator.LIKE, "%"+ serialNumber + "%"));
			}
			if (UtilValidate.isNotEmpty(locationSeqId)) {
			    searchConditions.add(EntityCondition.makeCondition("locationSeqId", EntityOperator.LIKE, "%"+ locationSeqId + "%"));
			}
			if (UtilValidate.isNotEmpty(lotId)) {
			    searchConditions.add(EntityCondition.makeCondition("lotId", EntityOperator.LIKE, "%"+ lotId + "%"));
			}
			
			searchConditions.add(EntityCondition.makeCondition(EntityOperator.OR,
                    EntityCondition.makeCondition("availableToPromiseTotal", EntityOperator.NOT_EQUAL, BigDecimal.ZERO),
                    EntityCondition.makeCondition("quantityOnHandTotal", EntityOperator.NOT_EQUAL, BigDecimal.ZERO)));
			
			List<String> fieldsToSelect = UtilMisc.toList("inventoryItemId", "productId","internalName","locationSeqId",
					"lotId","datetimeReceived","brandName","modelo","unitCost","currencyUomId","quantityOnHandTotal","availableToPromiseTotal","QOHTSUM","ATPSUM");
			List<String> orderBy = UtilMisc.toList("productId", "datetimeReceived","locationSeqId");
			
			List<GenericValue> listInventoryItem = delegator.findByCondition("ProductInventoryItem", EntityCondition.makeCondition(searchConditions), fieldsToSelect, orderBy);
			
			List<Map<String,Object>> listMapInventoryItem = FastList.newInstance();
			
			Map<String,Object> mapaInventory = FastMap.newInstance();
			String inventoryItemId = new String(); 
			for (GenericValue InventoryItem : listInventoryItem) {
				mapaInventory.putAll(InventoryItem.getAllFields());
				inventoryItemId = InventoryItem.getString("inventoryItemId");
				if(NumberUtils.isNumber(inventoryItemId)){//Sobreescribe el id para poder ordenarlo posteriormente
					mapaInventory.put("inventoryItemId", Long.valueOf(inventoryItemId));
				}
				listMapInventoryItem.add(mapaInventory);
				mapaInventory = FastMap.newInstance();
			}
			
			Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
				public int compare(Map<String, Object> m1, Map<String, Object> m2) {
					if(m1.get("inventoryItemId") instanceof Long && m2.get("inventoryItemId") instanceof Long){
						return ((Long) m1.get("inventoryItemId")).compareTo((Long) m2.get("inventoryItemId"));
					} else {
						return (m1.get("inventoryItemId").toString()).compareTo(m2.get("inventoryItemId").toString());
					}
				}
			};

			Collections.sort(listMapInventoryItem, mapComparator);
			
			resultado.put("listMapInventoryItem", listMapInventoryItem);
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
			ServiceUtil.returnError("Hub\u00f3 un error al consultar "+e.getMessage());
		}
		
		return resultado;
	}
}
