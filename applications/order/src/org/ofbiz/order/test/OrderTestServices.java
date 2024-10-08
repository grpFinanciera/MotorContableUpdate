/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
/* This file has been modified by Open Source Strategies, Inc. */
package org.ofbiz.order.test;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import javolution.util.FastList;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.order.order.OrderChangeHelper;
import org.ofbiz.order.shoppingcart.CheckOutHelper;
import org.ofbiz.order.shoppingcart.ShoppingCart;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;

/**
 * Order Processing Services
 */

public class OrderTestServices {

    public static final String module = OrderTestServices.class.getName();

    public static Map createTestSalesOrders(DispatchContext dctx, Map context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Integer numberOfOrders = (Integer) context.get("numberOfOrders");

        int numberOfOrdersInt = numberOfOrders.intValue();
        for (int i = 1; i <= numberOfOrdersInt; i++) {
            try {
                ModelService modelService = dctx.getModelService("createTestSalesOrderSingle");
                Map outputMap = dispatcher.runSync("createTestSalesOrderSingle", modelService.makeValid(context, ModelService.IN_PARAM));
                String orderId = (String)outputMap.get("orderId");
                Debug.logInfo("Test sales order with id [" + orderId + "] has been processed.", module);
            } catch (GenericServiceException e) {
                String errMsg = "Error calling createTestSalesOrderSingle: " + e.toString();
                Debug.logError(e, errMsg, module);
            }
        }
        return ServiceUtil.returnSuccess();
    }

    public static Map createTestSalesOrderSingle(DispatchContext dctx, Map context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        String productCategoryId = (String) context.get("productCategoryId");
        String productStoreId = (String) context.get("productStoreId");
        String currencyUomId = (String) context.get("currencyUomId");
        String partyId = (String) context.get("partyId");
        String productId = (String) context.get("productId");
        Integer numberOfOrders = (Integer) context.get("numberOfOrders");
        Integer numberOfProductsPerOrder = (Integer) context.get("numberOfProductsPerOrder");
        String salesChannel = (String) context.get("salesChannel");
        if (UtilValidate.isEmpty(salesChannel)) {
            salesChannel = "WEB_SALES_CHANNEL";
        }

        List productsList = FastList.newInstance();
        try {
            if (UtilValidate.isNotEmpty(productId)) {
                productsList.add(productId);
                numberOfProductsPerOrder = Integer.valueOf(1);
            } else {
                Map result = dispatcher.runSync("getProductCategoryMembers", UtilMisc.toMap("categoryId", productCategoryId));
                if (result.get("categoryMembers") != null) {
                    List productCategoryMembers = (List)result.get("categoryMembers");
                    if (productCategoryMembers != null) {
                        Iterator i = productCategoryMembers.iterator();
                        while (i.hasNext()) {
                            GenericValue prodCatMemb = (GenericValue) i.next();
                            if (prodCatMemb != null) {
                                productsList.add(prodCatMemb.getString("productId"));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            return ServiceUtil.returnError("The following error occurred: " + e.getMessage());
        }
        if (productsList.size() == 0) {
            return ServiceUtil.returnError("No products found in category [" + productCategoryId + "]; no orders will be created");
        }

        Random r = new Random();

        ShoppingCart cart = new ShoppingCart(delegator, productStoreId, locale, currencyUomId);
        cart.setOrderType("SALES_ORDER");
        cart.setChannelType(salesChannel);
        cart.setProductStoreId(productStoreId);

        cart.setBillToCustomerPartyId(partyId);
        cart.setPlacingCustomerPartyId(partyId);
        cart.setShipToCustomerPartyId(partyId);
        cart.setEndUserCustomerPartyId(partyId);
        try {
            cart.setUserLogin(userLogin, dispatcher);
        } catch (Exception exc) {
            Debug.logWarning("Error setting userLogin in the cart: " + exc.getMessage(), module);
        }
        int numberOfProductsPerOrderInt = numberOfProductsPerOrder.intValue();
        for (int j = 1; j <= numberOfProductsPerOrderInt; j++) {
            // get a product
            int k = r.nextInt(productsList.size());
            try {
                cart.addOrIncreaseItem((String) productsList.get(k), null, BigDecimal.ONE, null, null, null,
                                       null, null, null, null,
                                       null /*catalogId*/, null,
                                       null/*itemType*/, null/*itemGroupNumber*/, 
                                       null, dispatcher,null,null,null,null,null,null);
            } catch (Exception exc) {
                Debug.logWarning("Error adding product with id " + (String) productsList.get(k) + " to the cart: " + exc.getMessage(), module);
            }
        }
        cart.setDefaultCheckoutOptions(dispatcher);
        CheckOutHelper checkout = new CheckOutHelper(dispatcher, delegator, cart);
        Map orderCreateResult = checkout.createOrder(userLogin);
        String orderId = (String) orderCreateResult.get("orderId");

        Map resultMap = ServiceUtil.returnSuccess();
        // approve the order
        if (UtilValidate.isNotEmpty(orderId)) {
            Debug.logInfo("Created test order with id: " + orderId, module);
            boolean approved = OrderChangeHelper.approveOrder(dispatcher, userLogin, orderId);
            Debug.logInfo("Test order with id: " + orderId + " has been approved: " + approved, module);
            resultMap.put("orderId", orderId);
        }
        Boolean shipOrder = (Boolean) context.get("shipOrder");
        if (shipOrder.booleanValue() && UtilValidate.isNotEmpty(orderId)) {
            try {
                Map outputMap = dispatcher.runSync("quickShipEntireOrder", UtilMisc.toMap("orderId", orderId, "userLogin", userLogin));
                Debug.logInfo("Test sales order with id [" + orderId + "] has been shipped", module);
            } catch (Exception exc) {
                Debug.logWarning("Unable to quick ship test sales order with id [" + orderId + "] with error: " + exc.getMessage(), module);
            }
        }

        return resultMap;
    }
}
