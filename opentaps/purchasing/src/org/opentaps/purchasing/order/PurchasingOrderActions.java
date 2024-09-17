/*
 * Copyright (c) Open Source Strategies, Inc.
 *
 * Opentaps is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Opentaps is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Opentaps.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opentaps.purchasing.order;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javolution.util.FastList;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.opentaps.base.entities.StatusItem;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.domain.DomainsDirectory;
import org.opentaps.domain.inventory.InventoryRepositoryInterface;
import org.opentaps.domain.order.OrderViewForListing;
import org.opentaps.domain.order.PurchaseOrderLookupRepositoryInterface;
import org.opentaps.foundation.action.ActionContext;


/**
 * PurchasingOrderActions - Java Actions for purchasing orders.
 */
public final class PurchasingOrderActions {

    @SuppressWarnings("unused")
    private static final String MODULE = PurchasingOrderActions.class.getName();

    private PurchasingOrderActions() { }

    /**
     * Action for the lookup purchasing order screen.
     * @param context the screen context
     * @throws GeneralException if an error occurs
     */
    public static void findOrders(Map<String, Object> context) throws GeneralException {
        ActionContext ac = new ActionContext(context);
        Locale locale = ac.getLocale();
        TimeZone timeZone = ac.getTimeZone();
        Delegator delegator = ac.getDelegator();

        DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
        PurchaseOrderLookupRepositoryInterface purchaseOrderLookupRepository = dd.getOrderDomain().getPurchaseOrderLookupRepository();
        InventoryRepositoryInterface inventoryRepository = dd.getInventoryDomain().getInventoryRepository();

        // get the list of statuses for the parametrized form ftl
        List<StatusItem> statuses = purchaseOrderLookupRepository.findListCache(StatusItem.class, purchaseOrderLookupRepository.map(StatusItem.Fields.statusTypeId, "ORDER_STATUS"), UtilMisc.toList(StatusItem.Fields.sequenceId.name()));
        List<Map<String, Object>> statusList = new FastList<Map<String, Object>>();
        for (StatusItem s : statuses) {
            Map<String, Object> status = s.toMap();
            status.put("statusDescription", s.get(StatusItem.Fields.description.name(), locale));
            statusList.add(status);
        }
        ac.put("statusItems", statusList);

        // Initial values for the variables passed to the FTL script
        List<Map<String, Object>> resultList = FastList.<Map<String, Object>>newInstance();
        int resultTotalSize = 0;
        String extraParameters = "";

        // populate the organization party which lookup purchase orders requires
        String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
        String facilityId = (String) ac.getRequest().getSession().getAttribute("facilityId");
        if (UtilValidate.isEmpty(organizationPartyId)) {
            organizationPartyId = inventoryRepository.getFacilityById(facilityId).getOwnerPartyId();
        }


        // order by
        String orderParam = ac.getParameter("ordersOrderBy");
        if (UtilValidate.isEmpty(orderParam)) {
            orderParam = "orderDate DESC";
        }
        List<String> orderBy = UtilMisc.toList(orderParam);

        // possible fields we're searching by
        String partyId = ac.getParameter("supplierPartyId");
        // from URL GET request if form field wasn't set
        if (partyId == null) {
            partyId = ac.getParameter("supplierPartyId");
        }
        String statusId = ac.getParameter("statusId");
        String orderId = ac.getParameter("orderId");
        String orderName = ac.getParameter("orderName");
        String performFind = ac.getParameter("performFind");

        // We only perform a lookup if either this is the "Open Orders" form, or if the perform
        // lookup flag is either passed as a parameter or has already been passed in the context
        // (e.g. by setting it up in a screen)
        if ("Y".equals(ac.getString("performFind")) || "Y".equals(ac.getParameter("performFind")) || "true".equals(ac.getString("onlyOpenOrders"))) {
            extraParameters = "&orderId=" + orderId + "&orderName=" + orderName + "&supplierPartyId=" + partyId + "&statusId=" + statusId + "&performFind=" + performFind;
            purchaseOrderLookupRepository.setLocale(locale);
            purchaseOrderLookupRepository.setTimeZone(timeZone);

            purchaseOrderLookupRepository.setOrganizationPartyId(organizationPartyId);
            if (UtilValidate.isNotEmpty(statusId)) {
                purchaseOrderLookupRepository.setStatusId(statusId);
            }

            if (UtilValidate.isNotEmpty(partyId)) {
                purchaseOrderLookupRepository.setSupplierPartyId(partyId);
            }

            if (UtilValidate.isNotEmpty(orderId)) {
                purchaseOrderLookupRepository.setOrderId(orderId);
            }

            if (UtilValidate.isNotEmpty(orderName)) {
                purchaseOrderLookupRepository.setOrderName(orderName);
            }

            if ("true".equals(ac.getString("onlyOpenOrders"))) {
                purchaseOrderLookupRepository.setFindDesiredOnly(true);
            }

            purchaseOrderLookupRepository.setOrderBy(orderBy);
            purchaseOrderLookupRepository.enablePagination(false);
            List<OrderViewForListing> orders = purchaseOrderLookupRepository.findOrders();
            // return the map collection for the screen render
            for (OrderViewForListing order : orders) {
            	Map<String, Object> result = order.toMap();
            	Debug.log("Mike.- " + order.toMap());
            	Debug.log("Mike 1.1.- : " + order.toMap().get("orderId"));
            	EntityCondition condition = EntityCondition.makeCondition(EntityOperator.AND,
        				EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, order.toMap().get("orderId")));
                
                List<GenericValue> receipt = delegator.findByCondition("ShipmentReceiptOrder", condition, null,
        				null); 
                
                Debug.log("Mike2.- " + receipt);
                Iterator<GenericValue> por = receipt.iterator();
        		
                BigDecimal rec = new BigDecimal(0);
                BigDecimal cantidadTotal = new BigDecimal(0);
                BigDecimal cantidadFaltante = new BigDecimal(0);
                BigDecimal cantidadFaltanteF = new BigDecimal(0);
                BigDecimal precioFaltante = new BigDecimal(0);
                BigDecimal precioTotal = new BigDecimal(0);
                BigDecimal precioTotalF = new BigDecimal(0);
                while(por.hasNext()){
                	GenericValue recib = por.next();
                	Debug.log("recib: " + recib);
                	rec = rec.add(recib.getBigDecimal("quantityAccepted"));
                	Debug.log("Rec: " + rec);
                	cantidadTotal = cantidadTotal.add(recib.getBigDecimal("quantity"));
                	Debug.log("cantidadTotal: " + cantidadTotal);
                	cantidadFaltante = cantidadTotal.subtract(rec);
                	Debug.log("cantidadFaltante: " + cantidadFaltante);
                	precioFaltante = (recib.getBigDecimal("quantity").subtract(recib.getBigDecimal("quantityAccepted"))).multiply(recib.getBigDecimal("unitPrice"));
                	Debug.log("precioFaltante: " + precioFaltante);
                	precioTotal = precioTotal.add(precioFaltante);
                	Debug.log("precioTotal: " + precioTotal);
                } 
                if(receipt.isEmpty()){
                	List<GenericValue> receipt2 = delegator.findByCondition("OutOrder", condition, null,
            				null); 
                	Debug.log("Mike2.1.- " + receipt2);
                	Iterator<GenericValue> por2 = receipt2.iterator();
                	while(por2.hasNext()){
                		GenericValue recib2 = por2.next();
                		cantidadFaltanteF = cantidadFaltanteF.add(recib2.getBigDecimal("quantity"));
                		precioTotalF = recib2.getBigDecimal("grandTotal"); 
                	}
                }else{
                	List<GenericValue> receipt2 = delegator.findByCondition("OutOrder", condition, null,
            				null); 
                	Debug.log("Mike2.1.- " + receipt2);
                	Iterator<GenericValue> por2 = receipt2.iterator();
                	while(por2.hasNext()){
                		GenericValue recib2 = por2.next();
                		cantidadFaltanteF = cantidadFaltanteF.add(recib2.getBigDecimal("quantity"));
                		precioTotalF = recib2.getBigDecimal("grandTotal"); 
                	}
                }
                
                cantidadFaltanteF = cantidadFaltanteF.subtract(cantidadFaltante);
                precioTotalF = precioTotalF.subtract(precioTotal);
                
                result.put("cantidadFaltante", cantidadFaltanteF);
                result.put("precioFaltante", precioTotalF);
                resultList.add(result);
//                Debug.log("Mike3.- " + resultList);
            }
            resultTotalSize = resultList.size();
        }

        ac.put("purchaseOrders", resultList);
        ac.put("purchaseOrdersTotalSize", resultTotalSize);
        ac.put("extraParameters", extraParameters);

    }
}
