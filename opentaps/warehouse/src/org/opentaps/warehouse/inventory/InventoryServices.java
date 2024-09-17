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

/* This file may contain code which has been modified from that included with the Apache-licensed OFBiz product application */
/* This file has been modified by Open Source Strategies, Inc. */

package org.opentaps.warehouse.inventory;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilNumber;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityJoinOperator;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.security.Security;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.base.entities.ConsultaArticulosEnAlmacenesPorPermisoUsuario;
import org.opentaps.base.entities.ConsultaMaxMinAlmacen;
import org.opentaps.base.entities.Devolucion;
import org.opentaps.base.entities.DevolucionRecepcionItems;
import org.opentaps.base.entities.PhysicalInventorySearch;
import org.opentaps.base.entities.ProductoAlmacenMaxMin;
import org.opentaps.base.entities.SurtirPedidoInternoParcial;
import org.opentaps.common.builder.EntityListBuilder;
import org.opentaps.common.builder.ListBuilderException;
import org.opentaps.common.builder.PageBuilder;
import org.opentaps.common.util.UtilAccountingTags;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.common.util.UtilDate;
import org.opentaps.common.util.UtilMessage;
import org.opentaps.domain.DomainsDirectory;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.action.ActionContext;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.warehouse.security.WarehouseSecurity;

import com.ibm.icu.util.Calendar;
import com.opensourcestrategies.financials.util.UtilCOGS;
import com.opensourcestrategies.financials.util.UtilFinancial;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

/**
 * Services for Warehouse application Inventory section.
 *
 * @author     <a href="mailto:cliberty@opensourcestrategies.com">Chris Liberty</a>
 * @version    $Rev$
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class InventoryServices {

    private InventoryServices() { }

    private static final String MODULE = InventoryServices.class.getName();
    private static final String resource = "WarehouseUiLabels";
    private static final String opentapsErrorResource = "OpentapsErrorLabels";
    private static BigDecimal ZERO = BigDecimal.ZERO;
    private static final int INVOICE_ITEM_DIGITOS = 5; // this is the number of digits used for invoiceItemSeqId: 00001, 00002...
    private static int decimals = -1;
    private static int rounding = -1;
//    private static BigDecimal salarioMinimo = new BigDecimal("70.10");
//    private static BigDecimal diasSalarioMinimo = new BigDecimal("35.00");
    private static List<String> CUCOP = new ArrayList<String>();
    static {
        decimals = UtilNumber.getBigDecimalScale("invoice.decimals");
        rounding = UtilNumber.getBigDecimalRoundingMode("invoice.rounding");
        // set zero to the proper scale
        ZERO.setScale(decimals);
    }

    /**
     * Returns an error if InventoryItem.quantityOnHandTotal is less than zero.
     * @param dctx DispatchContext
     * @param context Map
     * @return Map
     */    
    public static Map checkInventoryItemQOHOverZero(DispatchContext dctx, Map context) {
        Delegator delegator = dctx.getDelegator();
        Locale locale = UtilCommon.getLocale(context);

        String inventoryItemId = (String) context.get("inventoryItemId");

        GenericValue inventoryItem = null;
        try {

            inventoryItem = delegator.findByPrimaryKey("InventoryItem", UtilMisc.toMap("inventoryItemId", inventoryItemId));
            if (UtilValidate.isEmpty(inventoryItem)) {
                String errorMessage = UtilProperties.getMessage(resource, "WarehouseErrorInventoryItemNotFound", context, locale);
                Debug.logError(errorMessage, MODULE);
                return ServiceUtil.returnError(errorMessage);
            }

        } catch (GenericEntityException e) {
            Debug.logError(e, MODULE);
            return ServiceUtil.returnError(e.getMessage());
        }

        Double quantityOnHandTotal = inventoryItem.getDouble("quantityOnHandTotal");
        if (UtilValidate.isNotEmpty(quantityOnHandTotal) && quantityOnHandTotal.doubleValue() < 0) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource, "WarehouseErrorInventoryItemQOHUnderZero", context, locale));
        }

        return ServiceUtil.returnSuccess();
    }

    /**
     * Creates a new "Lot" entity based on given service attributes.
     * @param dctx DispatchContext
     * @param context Map
     * @return Map
     */
    public static Map createLot(DispatchContext dctx, Map context) {
        Delegator delegator = dctx.getDelegator();
        Security security = dctx.getSecurity();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = UtilCommon.getLocale(context);

        String facilityId = (String) context.get("facilityId");
        WarehouseSecurity warehouseSecurity = new WarehouseSecurity(security, userLogin, facilityId);
        if (!warehouseSecurity.hasFacilityPermission("WRHS_INV_LOT_CREATE")) {
            String error = UtilProperties.getMessage(opentapsErrorResource, "OpentapsError_PermissionDenied", context, locale);
            return ServiceUtil.returnError(error);
        }

        String lotId = null;

        try {
            GenericValue lot = delegator.makeValidValue("Lot", context);

            if (UtilValidate.isEmpty(lot.get("lotId"))) {
                lot.set("lotId", delegator.getNextSeqId("Lot"));
            }

            if (UtilValidate.isEmpty(lot.get("creationDate"))) {
                lot.set("creationDate", UtilDateTime.nowTimestamp());
            }

            lot.create();
            lotId = (String) lot.get("lotId");
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }

        Map result = ServiceUtil.returnSuccess();
        result.put("lotId", lotId);
        return result;
    }

    /**
     * Updates a given "Lot" entity.
     * @param dctx DispatchContext
     * @param context Map
     * @return Map
     */
    public static Map updateLot(DispatchContext dctx, Map context) {
        Delegator delegator  = dctx.getDelegator();
        Security security = dctx.getSecurity();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = UtilCommon.getLocale(context);

        String facilityId = (String) context.get("facilityId");
        WarehouseSecurity warehouseSecurity = new WarehouseSecurity(security, userLogin, facilityId);
        if (!warehouseSecurity.hasFacilityPermission("WRHS_INV_LOT_UPDATE")) {
            String error = UtilProperties.getMessage(opentapsErrorResource, "OpentapsError_PermissionDenied", context, locale);
            return ServiceUtil.returnError(error);
        }

        String lotId = (String) context.get("lotId");

        try {
            GenericValue lot = delegator.findByPrimaryKey("Lot", UtilMisc.toMap("lotId", lotId));

            if (UtilValidate.isEmpty(lot)) {
                String error = UtilProperties.getMessage(resource, "WarehouseErrorLotIdNotFound", context, locale);
                return ServiceUtil.returnError(error);
            }

            lot.setNonPKFields(context);
            lot.store();
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }

        Map result = ServiceUtil.returnSuccess();
        result.put("lotId", lotId);
        return result;
    }

    /**
     * Issues order item quantity specified to the shipment, then receives inventory for that item and quantity,
     *  creating a new shipment if necessary. Overrides the OFBiz issueOrderItemToShipmentAndReceiveAgainstPO service.
     * If completePurchaseOrder is Y, then this will run the completePurchaseOrder service after receiving any specified
     *  inventory.  Unreserved inventory will be cancelled.
     * @param dctx DispatchContext
     * @param context Map
     * @return Map
     * @throws ParseException 
     */
    public static Map issueOrderItemToShipmentAndReceiveAgainstPO(DispatchContext dctx, Map context) throws ParseException {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        Locale locale = UtilCommon.getLocale(context);
        TimeZone timeZone = UtilCommon.getTimeZone(context);

        Map result = ServiceUtil.returnSuccess();

        Map orderItemSeqIds = (Map) context.get("orderItemSeqIds");
        Map quantitiesAccepted = (Map) context.get("quantitiesAccepted");
        Map quantitiesRejected = (Map) context.get("quantitiesRejected");
        Map lotIds = (Map) context.get("lotIds");
        Map productIds = (Map) context.get("productIds");
        Map unitCosts = (Map) context.get("unitCosts");
        Map inventoryItemTypeIds = (Map) context.get("inventoryItemTypeIds");
        Map claveMap = (Map) context.get("clavePresupuestalMap");
        Map<String, Object> mapaMontoClave = FastMap.newInstance();
        Map<String, Object> mapaMontoClavePagoAnti = FastMap.newInstance();
		Map<String, Object> catalogoAbonoPresMap = FastMap.newInstance();
        String shipmentId = (String) context.get("shipmentId");
        String shipmentId2 = null;
        GenericValue Shipment = null;
        GenericValue Shipment2 = null;
        Map rowSubmits = (Map) context.get("_rowSubmit");
        Map rowDeductivas = (Map) context.get("_rowDeductiva");
        Map montoAmpliarReducir = (Map) context.get("montoAmpliar");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        
        String acctgTransTypeId = (String) context.get("acctgTransTypeId");
        String acctgTransTypeIdPA = (String) context.get("acctgTransTypeIdPA");
        String numeroFactura = (String) context.get("numeroFactura");
        String comentario = (String) context.get("comentario");
        Timestamp fechaFactura = (Timestamp) context.get("fechaFactura");
        Timestamp fechaContable = (Timestamp) context.get("fechaContable");

        String purchaseOrderId = (String) context.get("purchaseOrderId");
        String facilityId = (String) context.get("facilityId");
        
        String currencyUomId = new String();
        String organizationPartyId = null;
        Map preciosReales = FastMap.newInstance();
        BigDecimal montoRestantePagoAnticipado = BigDecimal.ZERO;
        BigDecimal montoRestantePagoAnti = BigDecimal.ZERO;
        BigDecimal montoRestantePagoAnti2 = BigDecimal.ZERO;
        
        if (context.get("completePurchaseOrder").equals("G"))
        {	
        	GenericValue shipmentReceiptTemp = GenericValue.create(delegator.getModelEntity("ShipmentReceiptTemporal"));
        	for(int i=0;i<quantitiesAccepted.size();i++){
        		
        		shipmentReceiptTemp.set("shipmentTemId", Integer.toString(i));
        		shipmentReceiptTemp.set("acctgTransTypeId", acctgTransTypeId);
            	shipmentReceiptTemp.set("descripcion", comentario);
            	shipmentReceiptTemp.set("numeroFactura", numeroFactura);
            	shipmentReceiptTemp.set("fechaFactura", fechaFactura);
            	shipmentReceiptTemp.set("fechaContable", fechaContable);
            	shipmentReceiptTemp.set("orderId", purchaseOrderId);
            	shipmentReceiptTemp.set("unitPrice", Double.parseDouble(unitCosts.get(Integer.toString(i)).toString()));
            	shipmentReceiptTemp.set("quantityAccepted", quantitiesAccepted.get(Integer.toString(i)));
            	shipmentReceiptTemp.set("orderItemSeqIds", orderItemSeqIds.get(Integer.toString(i)));
            	
            	if(UtilValidate.isEmpty(montoAmpliarReducir.get(Integer.toString(i)))){
            		shipmentReceiptTemp.set("montoAmpliar",null);
            	}else{
            		shipmentReceiptTemp.set("montoAmpliar", Double.parseDouble(montoAmpliarReducir.get(Integer.toString(i)).toString()));
            	}
            	
            	shipmentReceiptTemp.set("recibir", rowSubmits.get(Integer.toString(i)));
            	shipmentReceiptTemp.set("deductiva", rowDeductivas.get(Integer.toString(i)));
            	
            	
        		try {
					delegator.createOrStore(shipmentReceiptTemp);
				} catch (GenericEntityException e) {
					e.printStackTrace();
				}
        	}
        	
        	
        	return ServiceUtil.returnSuccess("Se ha guardado la orden con exito");
        }
        
        boolean devengoParcial = false;
        if (context.get("completePurchaseOrder").equals("D"))
        {	devengoParcial = true;        	
        }
        
        try 
        {  
        	GenericValue Order = delegator.findByPrimaryKeyCache("OrderHeader", UtilMisc.toMap("orderId",purchaseOrderId));
        	Debug.log("A que miguelin tan wapo: " + context);
	        if (context.get("completePurchaseOrder").equals("P"))
	        {	
	        	boolean validaMontoampliar = validarMontoAmpliar(dispatcher, delegator, context, rowSubmits, montoAmpliarReducir, purchaseOrderId);
	        	if(validaMontoampliar == false)
	        	{	
	        		return ServiceUtil.returnError("El monto presupuestal a apliar sobrepasa el 20% del monto total del pedido " +  purchaseOrderId);
	        	}
	        	ampliarOrden(dispatcher, delegator, context, claveMap,  rowSubmits, montoAmpliarReducir, fechaFactura, purchaseOrderId, productIds, userLogin, orderItemSeqIds);
	        	
	        	List<GenericValue> shipmentReceiptTemp2 = delegator.findByCondition("ShipmentReceiptTemporal", EntityCondition.makeCondition("orderId",EntityOperator.EQUALS, purchaseOrderId), null, null);
	        	if(UtilValidate.isNotEmpty(shipmentReceiptTemp2)){
	        		delegator.removeAll(shipmentReceiptTemp2);
	        	}
	        	return ServiceUtil.returnSuccess("El presupuesto de la orden de compra o pedido ha sido ampliado");
	        }	        
	        if (context.get("completePurchaseOrder").equals("L"))
	        {	reducirOrden(dispatcher, delegator, context, claveMap,  rowSubmits, montoAmpliarReducir, fechaFactura, purchaseOrderId, productIds, userLogin, orderItemSeqIds);
	        
	        	List<GenericValue> shipmentReceiptTemp2 = delegator.findByCondition("ShipmentReceiptTemporal", EntityCondition.makeCondition("orderId",EntityOperator.EQUALS, purchaseOrderId), null, null);
        	
	        	if(UtilValidate.isNotEmpty(shipmentReceiptTemp2)){
	        		delegator.removeAll(shipmentReceiptTemp2);
	        	}
	        
	        	return ServiceUtil.returnSuccess("El presupuesto de la orden de compra o pedido ha sido reducido");
	        }	        	        
	        
            // List of cancelled order items
            List<String> cancelleOrderItems = EntityUtil.getFieldListFromEntityList(delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", purchaseOrderId, "statusId", "ITEM_CANCELLED")), "orderItemSeqId", true);

            if (UtilValidate.isEmpty(shipmentId)) {

                // Create a new shipment
                Map createShipmentContext = dctx.getModelService("createShipment").makeValid(context, ModelService.IN_PARAM);
                createShipmentContext.put("primaryOrderId", purchaseOrderId);
                createShipmentContext.put("primaryShipGroupSeqId", context.get("shipGroupSeqId"));
                createShipmentContext.put("shipmentTypeId", "PURCHASE_SHIPMENT");
                createShipmentContext.put("statusId", "PURCH_SHIP_CREATED");
                createShipmentContext.put("destinationFacilityId", facilityId);
                createShipmentContext.put("estimatedArrivalDate", UtilDateTime.nowTimestamp());
                createShipmentContext.put("acctgTransTypeId", acctgTransTypeId);
                createShipmentContext.put("numeroFactura", numeroFactura);
                createShipmentContext.put("fechaFactura", fechaFactura);
                createShipmentContext.put("fechaContable", fechaContable);
                createShipmentContext.put("descripcion", comentario);
                Map createShipmentResult = dispatcher.runSync("createShipment", createShipmentContext);
                if (ServiceUtil.isError(createShipmentResult)) {
                    Debug.logError(ServiceUtil.getErrorMessage(createShipmentResult), MODULE);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(createShipmentResult));
                }
                shipmentId = (String) createShipmentResult.get("shipmentId");
                Shipment = delegator.findByPrimaryKey("Shipment", UtilMisc.toMap("shipmentId",shipmentId));
                
                if(UtilValidate.isNotEmpty(Order.getBigDecimal("pagoAnticipadoRestante")) && UtilValidate.isNotEmpty(acctgTransTypeIdPA)){
	                Map createShipmentContext2 = dctx.getModelService("createShipment").makeValid(context, ModelService.IN_PARAM);
	                createShipmentContext2.put("primaryOrderId", purchaseOrderId);
	                createShipmentContext2.put("primaryShipGroupSeqId", context.get("shipGroupSeqId"));
	                createShipmentContext2.put("shipmentTypeId", "PURCHASE_SHIPMENT");
	                createShipmentContext2.put("statusId", "PURCH_SHIP_CREATED");
	                createShipmentContext2.put("destinationFacilityId", facilityId);
	                createShipmentContext2.put("estimatedArrivalDate", UtilDateTime.nowTimestamp());
	                createShipmentContext2.put("acctgTransTypeId", acctgTransTypeIdPA);
	                createShipmentContext2.put("numeroFactura", numeroFactura);
	                createShipmentContext2.put("fechaFactura", fechaFactura);
	                createShipmentContext2.put("fechaContable", fechaContable);
	                createShipmentContext2.put("descripcion", comentario);
	                Map createShipmentResult2 = dispatcher.runSync("createShipment", createShipmentContext2);
	                if (ServiceUtil.isError(createShipmentResult2)) {
	                    Debug.logError(ServiceUtil.getErrorMessage(createShipmentResult2), MODULE);
	                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(createShipmentResult2));
	                }
	                shipmentId2 = (String) createShipmentResult2.get("shipmentId");
	                Shipment2 = delegator.findByPrimaryKey("Shipment", UtilMisc.toMap("shipmentId",shipmentId2));
                }
                context.put("shipmentId", shipmentId);
                
            }
            
            //cambiams los montos para las deductivas
            Iterator modCos = rowSubmits.keySet().iterator();
            GenericValue oItem = null;
	        while (modCos.hasNext()) {
                String linea = (String) modCos.next();
                // Ignore unchecked rows
                if (!UtilValidate.isNotEmpty(rowSubmits.get(linea))) {
                    continue;
                }
                
                String secOrden = (String) orderItemSeqIds.get(linea);
                if (UtilValidate.isNotEmpty(secOrden)) {
                    if (cancelleOrderItems.contains(secOrden)) {
                        continue;
                    }
                }

                try {
                   
                	oItem = delegator.findByPrimaryKey("OrderItem", UtilMisc.toMap("orderId", purchaseOrderId, "orderItemSeqId", secOrden));
                	preciosReales.put(linea, oItem.getBigDecimal("unitPrice").toString());
                	
                    String costoUnitarioString = (String) unitCosts.get(linea);
                	String cantidadAceptadaString = (String) quantitiesAccepted.get(linea);
                	BigDecimal costoUnitarioDecimal = BigDecimal.ZERO;
                	BigDecimal cantidadAceptadaDecimal = BigDecimal.ZERO;    
                	if(costoUnitarioString != null)            		
                		costoUnitarioDecimal = UtilCommon.parseLocalizedNumber(locale, (String) unitCosts.get(linea));
                	if(cantidadAceptadaString != null)
                		cantidadAceptadaDecimal = UtilCommon.parseLocalizedNumber(locale, (String) quantitiesAccepted.get(linea));
                	
                	BigDecimal montoFinal = (costoUnitarioDecimal.multiply(cantidadAceptadaDecimal));
                	   	
                	if(!rowDeductivas.isEmpty())
                	{	String deductiva = (String) rowDeductivas.get(linea);
                		if (deductiva != null) {
	                		if(rowDeductivas.get(linea).toString().equals("Y")||rowDeductivas.get(linea).toString().equals("N")){
	                			Map deductivaResult = dispatcher.runSync("cancelPenalizacion", 
	           							UtilMisc.toMap("orderId", purchaseOrderId, "orderItemSeqId", secOrden,
	           									"cancelQuantity", cantidadAceptadaDecimal,
	           									"userLogin", userLogin,	"fechaContable", fechaContable, "penaDeductivaFlag", "D",
	           									"descripcion", "Aplicaci\u00f3n de deductiva"));
	                        	if (ServiceUtil.isError(deductivaResult))  {
	                           	return ServiceUtil.returnError(ServiceUtil.getErrorMessage(deductivaResult));
	                           }
	                        	
	                        	EntityCondition cond = EntityCondition.makeCondition(EntityOperator.AND, 
	                   				EntityCondition.makeCondition("aplicaPenaId",EntityOperator.EQUALS, deductivaResult.get("aplicaPenaId")));
	
	                   		List<GenericValue> duductiva = delegator.findByCondition("DetalleAplicaPenaDeductiva", cond, null, null);
	                   			montoFinal = montoFinal.subtract(duductiva.get(0).getBigDecimal("monto"));
	                        	unitCosts.put(linea, (montoFinal.divide(cantidadAceptadaDecimal)).toString());
	                        	costoUnitarioDecimal = montoFinal.divide(cantidadAceptadaDecimal);
	                		}
	                   }               
                	}
                	
                } catch (ParseException e) {
                    Debug.logError(e, MODULE);
                    return ServiceUtil.returnError(e.getMessage());
                }
	        }
            //terminamos de cambiar los montos

            List toReceive = new ArrayList();

            Iterator rit = rowSubmits.keySet().iterator();
            BigDecimal suma = new BigDecimal(0);
            GenericValue OrderItem = null;
            BigDecimal pagoAnticipado = Order.getBigDecimal("pagoAnticipadoRestante");
            BigDecimal montoRestOrg = Order.getBigDecimal("pagoAnticipadoRestante");
            while (rit.hasNext()) {
            	Order = delegator.findByPrimaryKeyCache("OrderHeader", UtilMisc.toMap("orderId",purchaseOrderId));
            	montoRestantePagoAnti = BigDecimal.ZERO;
            	pagoAnticipado = Order.getBigDecimal("pagoAnticipadoRestante");
            	BigDecimal montoRestante = BigDecimal.ZERO; 
                String rowNumber = (String) rit.next();

                // Ignore unchecked rows
                if (!UtilValidate.isNotEmpty(rowSubmits.get(rowNumber))) {
                    continue;
                }

                // clear the reusable data because otherwise we end up doing things like receiving everything into one lot from the first line
                context.remove("lotId");

                String orderItemSeqId = (String) orderItemSeqIds.get(rowNumber);
                if (UtilValidate.isNotEmpty(orderItemSeqId)) {
                    if (cancelleOrderItems.contains(orderItemSeqId)) {
                        continue;
                    }
                    context.put("orderItemSeqId", orderItemSeqId);
                }
                if (UtilValidate.isNotEmpty(lotIds.get(rowNumber))) {
                    context.put("lotId", lotIds.get(rowNumber));
                }
                if (UtilValidate.isNotEmpty(productIds.get(rowNumber))) {
                    context.put("productId", productIds.get(rowNumber));
                }
                if (UtilValidate.isNotEmpty(inventoryItemTypeIds.get(rowNumber))) {
                    context.put("inventoryItemTypeId", inventoryItemTypeIds.get(rowNumber));
                }
                try {
                    if (UtilValidate.isNotEmpty((String) quantitiesAccepted.get(rowNumber))) {
                    	if(!quantitiesAccepted.get(rowNumber).equals("0") && devengoParcial)
                        {	context.put("quantityAccepted", UtilCommon.parseLocalizedNumber(locale, (String) "0"));                        	
                        }
                        else
                        {	context.put("quantityAccepted", UtilCommon.parseLocalizedNumber(locale, (String) quantitiesAccepted.get(rowNumber)));
                        }                        
                    }
                    if (UtilValidate.isNotEmpty((String) quantitiesAccepted.get(rowNumber))) {
                        context.put("quantity", UtilCommon.parseLocalizedNumber(locale, (String) quantitiesAccepted.get(rowNumber)));
                    }
                    if (UtilValidate.isNotEmpty((String) quantitiesRejected.get(rowNumber))) {
                        context.put("quantityRejected", UtilCommon.parseLocalizedNumber(locale, (String) quantitiesRejected.get(rowNumber)));
                    }
                    if (UtilValidate.isNotEmpty((String) unitCosts.get(rowNumber))) {
                    	context.put("unitCost", UtilCommon.parseLocalizedNumber(locale, (String) unitCosts.get(rowNumber)));
                    }
                    String unitCostsString = (String) unitCosts.get(rowNumber);
                	String quantitiesAcceptedString = (String) quantitiesAccepted.get(rowNumber);
                	BigDecimal unitCostsDecimal = BigDecimal.ZERO;
                	BigDecimal quantitiesAcceptedDecimal = BigDecimal.ZERO;   
                	montoRestante = BigDecimal.ZERO;  
                	if(unitCostsString != null)            		
                		unitCostsDecimal = BigDecimal.valueOf(Double.valueOf(unitCostsString));
                	if(quantitiesAcceptedString != null)
                		quantitiesAcceptedDecimal = UtilCommon.parseLocalizedNumber(locale, (String) quantitiesAccepted.get(rowNumber));
                	
                	if(devengoParcial)
		            {	if(quantitiesAcceptedDecimal.compareTo(BigDecimal.ZERO) > 1)
		            	{	quantitiesAcceptedDecimal = BigDecimal.ONE;
		            	}
		            }                	
                	
                    Map<String,Object> cancelPenalizacionResult = dispatcher.runSync("cancelPenalizacion", UtilMisc.toMap("orderId", purchaseOrderId, "orderItemSeqId", orderItemSeqId, "cancelQuantity", quantitiesAcceptedDecimal, "userLogin", userLogin, "fechaContable", fechaContable, "penaDeductivaFlag", "P", "descripcion", "Retraso"));
                	if (ServiceUtil.isError(cancelPenalizacionResult)) return cancelPenalizacionResult;
                	
                	OrderItem = delegator.findByPrimaryKey("OrderItem", UtilMisc.toMap("orderId", purchaseOrderId, "orderItemSeqId", orderItemSeqIds.get(rowNumber)));
                	if(OrderItem.getBigDecimal("montoRestante") == null){
                		montoRestante = (OrderItem.getBigDecimal("unitPrice").multiply(OrderItem.getBigDecimal("quantity"))).subtract(unitCostsDecimal.multiply(quantitiesAcceptedDecimal));
                	}else{
                		montoRestante = OrderItem.getBigDecimal("montoRestante").subtract(unitCostsDecimal.multiply(quantitiesAcceptedDecimal));
                	}
                	
                	UtilAccountingTags.putAllAccountingTags(OrderItem, context, UtilAccountingTags.ENTITY_TAG_PREFIX);
                	
                	if(devengoParcial)
		            {	if(montoRestante.compareTo(BigDecimal.ZERO) <= 0)
		            	{	return ServiceUtil.returnError("El producto por el monto ["+unitCostsDecimal+"] va a ser recibido totalmente y no puede completarse por este procedimiento. Utilice el boton \"Recibir y mantener abierto\"");
		            	}
		            }
                	
                	montoRestantePagoAnticipado = pagoAnticipado;
                	
                	BigDecimal montoClave = (unitCostsDecimal.multiply(quantitiesAcceptedDecimal));

                    suma = suma.add(montoClave);
                    
                    
                    if(UtilValidate.isEmpty(pagoAnticipado) || montoRestantePagoAnticipado.compareTo(BigDecimal.ZERO) <=0){
                    	
                    	mapaMontoClave.put(rowNumber, montoClave.toString());
                    	
                    }else{
                    	
                    	if(montoRestantePagoAnticipado.compareTo(montoClave)<=0){
                    		
                    		mapaMontoClavePagoAnti.put(rowNumber, montoRestantePagoAnticipado.toString());
        	        		
        	        		catalogoAbonoPresMap.put("Devengado"+rowNumber, Order.getString("proveedorPagoAnticipado"));
                    		
                    	}else{
                    		
                    		mapaMontoClavePagoAnti.put(rowNumber, montoClave.toString());
                    		catalogoAbonoPresMap.put("Devengado"+rowNumber, Order.getString("proveedorPagoAnticipado"));
                    		
                    	}                    	
                    	
                    	montoRestantePagoAnti = montoClave.subtract(pagoAnticipado);
                    	
                    }
                    
                    if(montoRestantePagoAnti.compareTo(BigDecimal.ZERO)>0){
                    	
                    	mapaMontoClave.put(rowNumber, montoRestantePagoAnti.toString());
                    	
                    }
                	
                	/**SE ACTUALIZA EL UNIT PRICE
                	 * Nativamente, al final de la ejecucion de este servicio, se actualiza el unitPrice de los OrderItems.
                     * Se modifica para que se actualice el unitPrice desde este momento para que al crear el Invoice (Pre orden de pago), ya tenga los montos correctos 
                    */
                	OrderItem.set("unitPrice", unitCostsDecimal);
                	OrderItem.set("montoRestante", montoRestante);
                	Debug.logInfo("OrderItem: " + OrderItem,MODULE);
                	OrderItem.store();
                } catch (ParseException e) {
                    Debug.logError(e, MODULE);
                    return ServiceUtil.returnError(e.getMessage());
                }

                if (UtilValidate.isEmpty(context.get("quantityAccepted")) || ((BigDecimal) context.get("quantityAccepted")).doubleValue() <= 0) {                	
                	
                	if(UtilValidate.isNotEmpty(pagoAnticipado)){
                    	montoRestantePagoAnti2 = montoRestOrg.subtract(suma);
                    	Order.set("pagoAnticipadoRestanteOrg", montoRestantePagoAnti2);
                    	if(montoRestantePagoAnti.compareTo(BigDecimal.ZERO)<0){
                        	Order.set("pagoAnticipadoRestante", montoRestantePagoAnti.negate());
                        }else{
                        	Order.set("pagoAnticipadoRestante", BigDecimal.ZERO);
                        }
                    	Order.store();
                    }
                	
                	continue;
                }                

                // Call the issueOrderItemToShipment service
                Map issueOrderItemContext = dctx.getModelService("issueOrderItemToShipment").makeValid(context, ModelService.IN_PARAM);
                Debug.log("issueOrderItemContext: " + issueOrderItemContext);
                issueOrderItemContext.put("orderId", purchaseOrderId);
                Map issueOrderItemResult = dispatcher.runSync("issueOrderItemToShipment", issueOrderItemContext);
                if (ServiceUtil.isError(issueOrderItemResult)) {
                	Debug.logError(ServiceUtil.getErrorMessage(issueOrderItemResult), MODULE);
                	return ServiceUtil.returnError(ServiceUtil.getErrorMessage(issueOrderItemResult));
                }
                String shipmentItemSeqId = (String) issueOrderItemResult.get("shipmentItemSeqId");
                context.put("shipmentItemSeqId", shipmentItemSeqId);
                String itemIssuanceId = (String) issueOrderItemResult.get("itemIssuanceId");
                context.put("itemIssuanceId", itemIssuanceId);    
                context.put("orderId", purchaseOrderId);
                context.put("datetimeReceived", UtilDateTime.nowTimestamp());

                toReceive.add(new HashMap(context));                
                Debug.log("A que miguelin tan wapo2: " + context);
                if(UtilValidate.isNotEmpty(pagoAnticipado)){
                	montoRestantePagoAnti2 = montoRestOrg.subtract(suma);
                	Order.set("pagoAnticipadoRestanteOrg", montoRestantePagoAnti2);
                }
                
                if(montoRestantePagoAnti.compareTo(BigDecimal.ZERO)<0){
                	Order.set("pagoAnticipadoRestante", montoRestantePagoAnti.negate());
                }else if (UtilValidate.isNotEmpty(pagoAnticipado)){
                	Order.set("pagoAnticipadoRestante", BigDecimal.ZERO);
                }
                
                Order.store();
                
                //physical inventory history
//                GenericValue phistory = GenericValue.create(delegator
//        				.getModelEntity("PhysicalInventoryHistory"));
//                phistory.set("inventoryHistoryId", delegator.getNextSeqId("PhysicalInventoryHistory"));
//                phistory.set("productId", productIds.get(rowNumber));
//                phistory.set("organizationPartyId", (String) context.get("ownerPartyId"));
//                phistory.set("unitCost", UtilCommon.parseLocalizedNumber(locale, (String) unitCosts.get(rowNumber)));
//                phistory.set("cantidad", UtilCommon.parseLocalizedNumber(locale, (String) quantitiesAccepted.get(rowNumber)));
//                phistory.set("effectiveDate", UtilDateTime.nowTimestamp());
//            	delegator.createOrStore(phistory);
            }
            
            
            /************************************/
            /******** Motor Contable ************/
            /************************************/
            
            organizationPartyId = Order.getString("billToPartyId");
            if(suma.compareTo(BigDecimal.ZERO) != 0){

	            Timestamp fechaTrans = new Timestamp(Calendar.getInstance().getTimeInMillis());
	            currencyUomId = Order.getString("currencyUom");
	            Map<String, Object> output = new FastMap();
	            Map<String, Object> output2 = new FastMap();
	            if(UtilValidate.isEmpty(Order.getBigDecimal("pagoAnticipado")) || mapaMontoClave.size()>0){
	            
		            Map<String, Object> input = FastMap.newInstance();
		            input.put("eventoContableId",acctgTransTypeId);
		            input.put("tipoClaveId","EGRESO");
		            input.put("fechaRegistro",fechaTrans);
		            input.put("fechaContable",fechaContable);
		            input.put("currency", currencyUomId);
		            input.put("usuario", userLogin.getString("userLoginId"));
		            input.put("organizationId",Order.getString("billToPartyId"));
		            input.put("descripcion", comentario);
		            input.put("roleTypeId", "BILL_FROM_VENDOR");
		            input.put("campo","shipmentId");
		            input.put("valorCampo", shipmentId);
			        input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(acctgTransTypeId, delegator, context, Order.getString("billFromPartyId"),null, mapaMontoClave, productIds));
			        
			        output = dispatcher.runSync("creaTransaccionMotor", input);
		        	if(ServiceUtil.isError(output)){
		        		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
		        	}
	            }
	        	
	        	if(UtilValidate.isNotEmpty(pagoAnticipado) && mapaMontoClavePagoAnti.size()>0){
	        		
	        		context.put("catalogoAbonoPresMap", catalogoAbonoPresMap);
	        		
	        		Map<String, Object> input2 = FastMap.newInstance();
	        		input2.put("eventoContableId",acctgTransTypeIdPA);
	        		input2.put("tipoClaveId","EGRESO");
	        		input2.put("fechaRegistro",fechaTrans);
	        		input2.put("fechaContable",fechaContable);
	        		input2.put("currency", currencyUomId);
	        		input2.put("usuario", userLogin.getString("userLoginId"));
	        		input2.put("organizationId",Order.getString("billToPartyId"));
	        		input2.put("descripcion", comentario);
	        		input2.put("roleTypeId", "BILL_FROM_VENDOR");
	        		input2.put("campo","shipmentId");
	        		input2.put("valorCampo", purchaseOrderId);
	        		input2.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(acctgTransTypeIdPA, delegator, context, Order.getString("billFromPartyId"),null, mapaMontoClavePagoAnti, productIds));
	    	        
	    	        output2 = dispatcher.runSync("creaTransaccionMotor", input2);
	            	if(ServiceUtil.isError(output2)){
	            		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output2));
	            	}
	        		
	        	}
	        	
	        	if(devengoParcial)
	        	{	
	        		if(UtilValidate.isEmpty(Order.getBigDecimal("pagoAnticipado")) || Order.getBigDecimal("pagoAnticipadoRestante").compareTo(BigDecimal.ZERO)<=0){
		        			//Crea invoice e InvoiceItem e OrderItemBilling
	        			String invoiceId = "";
	        			
	        			invoiceId = crearInvoice(delegator, dispatcher, Order.getString("billToPartyId"), Order.getString("billFromPartyId"), 
								fechaContable, Order.getString("currencyUom"), suma, purchaseOrderId, userLogin, numeroFactura,comentario,"EGRESO");
	        			
	        			if(invoiceId.equals("error")){
		        			return ServiceUtil.returnError("Existio error al crear el Invoice");
		        		}
		        		
	        			String verE= crearInvoiceItem(dispatcher, delegator, invoiceId, productIds, unitCosts, claveMap, quantitiesAccepted, userLogin, orderItemSeqIds, purchaseOrderId, "EGRESO", rowDeductivas, fechaContable);        		
		        		
	        			if(verE!=null){
		        			return ServiceUtil.returnError("Existio error al crear el Invoice");
		        		}else{	
		        			String verEr= crearRecepcionDevengoMontos(dispatcher, purchaseOrderId, shipmentId, orderItemSeqIds, numeroFactura, fechaFactura, 
		        								fechaContable, productIds, Order.getString("billFromPartyId"), unitCosts, userLogin, quantitiesAccepted,invoiceId);
							if(verEr!=null){	
								return ServiceUtil.returnError("Ocurri\u00f3 un error al crear registros en Devengo parcial por montos");
							}
						}
	        			
		        		if(UtilValidate.isNotEmpty(Order.getBigDecimal("pagoAnticipadoRestanteOrg")) && Order.getBigDecimal("pagoAnticipadoRestanteOrg").compareTo(BigDecimal.ZERO)<0){
			            	GenericValue invoice = delegator.findByPrimaryKey("Invoice", UtilMisc.toMap("invoiceId", invoiceId));
			            	List <GenericValue> invoiceItem = delegator.findByCondition("InvoiceItem", EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, invoiceId), null, UtilMisc.toList("invoiceItemSeqId"));
			            	
			            	invoice.setString("invoiceAdjustedTotal", Order.getBigDecimal("pagoAnticipadoRestanteOrg").negate().toString());
			            	invoice.setString("invoiceTotal", Order.getBigDecimal("pagoAnticipadoRestanteOrg").negate().toString());
			            	invoice.setString("openAmount", Order.getBigDecimal("pagoAnticipadoRestanteOrg").negate().toString());
			            	invoice.setString("pendingOpenAmount", Order.getBigDecimal("pagoAnticipadoRestanteOrg").negate().toString());
			            	
			            	BigDecimal montoRestante = Order.getBigDecimal("pagoAnticipadoRestanteOrg").negate();
			            	
			            	for (GenericValue inv : invoiceItem) {
								
			            		if(montoRestante.compareTo(inv.getBigDecimal("montoRestante"))<0){
			            			inv.set("montoRestante", montoRestante);
			            			delegator.store(inv);
			            		}
			            		
			            		montoRestante = montoRestante.subtract(inv.getBigDecimal("montoRestante"));
			            		
							}
			            	
			            	Order.setString("pagoAnticipadoRestanteOrg", "0");
			            	delegator.store(Order);
			            	delegator.store(invoice);
			            }		        		
	        		}else{
	        			String verEr= crearRecepcionDevengoMontos(dispatcher, purchaseOrderId, shipmentId, orderItemSeqIds, numeroFactura, fechaFactura, 
								fechaContable, productIds, Order.getString("billFromPartyId"), unitCosts, userLogin, quantitiesAccepted,null);
						if(verEr!=null)
						{	return ServiceUtil.returnError("Ocurri\u00f3 un error al crear registros en Devengo parcial por montos");
						}
	        		}
	        	}        	
	        	
	        	 if(UtilValidate.isEmpty(Order.getBigDecimal("pagoAnticipado")) || mapaMontoClave.size()>0){
	        		GenericValue transaccion = (GenericValue) output.get("transaccion");
	             	Shipment.set("acctgTransId", transaccion.getString("acctgTransId"));
	             	Shipment.store();
	        	 }
	        	
	        	if(UtilValidate.isNotEmpty(pagoAnticipado) && mapaMontoClavePagoAnti.size()>0){
	        		GenericValue transaccion = (GenericValue) output2.get("transaccion");
	            	Shipment2.set("acctgTransId", transaccion.getString("acctgTransId"));
	            	Shipment2.store();
	        	}
            }
            
        	

        	if(UtilValidate.isNotEmpty(toReceive)){
            	Map<String,Object> mapaRecibirProducto = FastMap.newInstance();
            	mapaRecibirProducto.put("listInventario", toReceive);
            	mapaRecibirProducto.put("facilityId", facilityId);
            	mapaRecibirProducto.put("currencyUomId", currencyUomId);
            	mapaRecibirProducto.put("userLogin", userLogin);
            	mapaRecibirProducto.put("fechaContable", fechaContable);
            	
            	Map<String,Object> receiveInvResult = dispatcher.runSync("recibeProductosInventario", mapaRecibirProducto);
                if (ServiceUtil.isError(receiveInvResult)) {
                    Debug.logError(ServiceUtil.getErrorMessage(receiveInvResult), MODULE);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(receiveInvResult));
                }
        	}

            
            if(UtilValidate.isNotEmpty(context.get("completePurchaseOrder")) &&
            		(context.get("completePurchaseOrder").equals("Y") || 
            		context.get("completePurchaseOrder").equals("R") || 
            		context.get("completePurchaseOrder").equals("T"))){
                Map<String,Object> results = dispatcher.runSync("completePurchaseOrder", 
                		UtilMisc.toMap("orderId", purchaseOrderId, "userLogin", context.get("userLogin"), "facilityId", facilityId, "flag", context.get("completePurchaseOrder") ,
                				"locale", locale,"timeZone",timeZone, "fechaContable", fechaContable));
                if (ServiceUtil.isError(results)) {
                	return ServiceUtil.returnError(ServiceUtil.getErrorMessage(results));
                }
            }
            
            Map<String,Object> pena = dispatcher.runSync("creaPenalizacionDeductiva", UtilMisc.toMap("orderId", purchaseOrderId, "userLogin", context.get("userLogin"), "organizationPartyId", organizationPartyId));
            if (ServiceUtil.isError(pena)) {
            	return ServiceUtil.returnError(ServiceUtil.getErrorMessage(pena));
            }

            //Creacion de artivulos en control patrimonial
            crearRecepcionActivoFijo(dispatcher, delegator, shipmentId, purchaseOrderId, facilityId, (String) context.get("ownerPartyId"), fechaFactura, orderItemSeqIds, quantitiesAccepted, productIds, unitCosts, numeroFactura, userLogin.getString("userLoginId"));
            
            Iterator camb = rowSubmits.keySet().iterator();
            GenericValue oI = null;
            while (camb.hasNext()) {
                String l = (String) camb.next();
                String orderItemSeqId = (String) orderItemSeqIds.get(l);
                oI = delegator.findByPrimaryKey("OrderItem", UtilMisc.toMap("orderId", purchaseOrderId, "orderItemSeqId", orderItemSeqId));
                String unitCostsString = (String) preciosReales.get(l);
                oI.set("unitPrice", new BigDecimal(unitCostsString));
                delegator.store(oI);
            }
            
            List<GenericValue> shipmentReceiptTemp2 = delegator.findByCondition("ShipmentReceiptTemporal", EntityCondition.makeCondition("orderId",EntityOperator.EQUALS, purchaseOrderId), null, null);
        	
            if(UtilValidate.isNotEmpty(shipmentReceiptTemp2)){
        		delegator.removeAll(shipmentReceiptTemp2);
        	}
        
        } catch (GenericServiceException e) {
            Debug.logError(e, MODULE);
            return ServiceUtil.returnError(e.getMessage());
        } catch (GenericEntityException e) {
            Debug.logError(e, MODULE);
            return ServiceUtil.returnError(e.getMessage());
        }
        
        try {
        
	        List<GenericValue> shipmentR = delegator.findByCondition("ShipmentReceipt", EntityCondition.makeCondition("shipmentId", EntityOperator.EQUALS, shipmentId), UtilMisc.toList("inventoryItemId"), null);
	        
	        for (GenericValue shipmentReceipt : shipmentR) {
	        	EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
	        	        EntityCondition.makeCondition("inventoryItemId", EntityOperator.EQUALS, shipmentReceipt.get("inventoryItemId")),
	        	        EntityCondition.makeCondition("inventoryItemDetailSeqId", EntityOperator.NOT_EQUAL, "0001"));
	        	        
	        			List<GenericValue> inventoryItemDetail = delegator.findByCondition("InventoryItemDetail", condiciones, null, null);
	        			
	        			inventoryItemDetail.get(0).set("fechaContable", fechaContable);
	        			delegator.store(inventoryItemDetail.get(0));
			}
	        
	        
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        result.put("shipmentId", shipmentId);
        return result;
    }
    
    /**
     * Creacion de la pena deductiva-orden de cobro
     * @param dctx
     * @param context
     * @return
     * @throws GenericEntityException
     */
    public static Map<String, Object> creaPenalizacionDeductiva(DispatchContext dctx, Map<String, Object> context) throws GenericEntityException {
    	LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();

        Map result = ServiceUtil.returnSuccess();
        
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String organizationPartyId = (String) context.get("organizationPartyId");
        String purchaseOrderId = (String) context.get("orderId");
        
    	EntityCondition condicion1 = EntityCondition.makeCondition(EntityOperator.AND, 
				EntityCondition.makeCondition("flag",EntityOperator.EQUALS, "Y"),
				EntityCondition.makeCondition("userLoginId", EntityOperator.EQUALS, userLogin.getString("userLoginId")));


		List<GenericValue> search = delegator.findByCondition("AplicaPenaDeductiva", condicion1, null, null);
		Map productIdsCancel = FastMap.newInstance();
		Map unitCostsCancel = FastMap.newInstance();
		Map quantitiesAcceptedCancel = FastMap.newInstance();
		Map orderItemSeqIdsCancel = FastMap.newInstance();
		Map claveMapCancel = FastMap.newInstance();
		BigDecimal montoAplica = BigDecimal.ZERO;
		
		if(!search.isEmpty()) {
			EntityCondition condicion = EntityCondition.makeCondition(EntityOperator.AND, 
					EntityCondition.makeCondition("aplicaPenaId",EntityOperator.EQUALS, search.get(0).getString("aplicaPenaId")));

			Debug.logInfo("Mike condiciones detalle: " + condicion, MODULE);

			List<GenericValue> detalles = delegator.findByCondition("DetalleAplicaPenaDeductiva", condicion, null, null);
			
			EntityCondition condicion11 = EntityCondition.makeCondition(EntityOperator.AND, 
					EntityCondition.makeCondition("organizationPartyId",EntityOperator.EQUALS, organizationPartyId),
					EntityCondition.makeCondition("tipo",EntityOperator.EQUALS, "INGRESO"),
					EntityCondition.makeCondition("flag",EntityOperator.EQUALS, "Y"));

			Debug.log("Mike condiciones1 detalle: " + condicion11, MODULE);

			List<GenericValue> clave = delegator.findByCondition("ClavePenaDeductiva", condicion11, null, null);
			
			if(clave.isEmpty()){
				return ServiceUtil.returnError("Es necesario configurar la clave presupuestal para ingresar las penalizaciones");
			}
			int i = 0;

			for (GenericValue d : detalles) {
				GenericValue generic = delegator.findByPrimaryKey("OrderItem", UtilMisc.toMap("orderId",d.getString("orderId"), "orderItemSeqId", d.getString("orderItemSeqId")));
				productIdsCancel.put(String.valueOf(i), generic.getString("productId"));
				unitCostsCancel.put(String.valueOf(i), d.getBigDecimal("monto"));
				quantitiesAcceptedCancel.put(String.valueOf(i), d.getBigDecimal("cantidad"));
				orderItemSeqIdsCancel.put(String.valueOf(i), d.getString("orderItemSeqId"));
				claveMapCancel.put(String.valueOf(i), clave.get(0).getString("clavePresupuestal"));
				montoAplica = montoAplica.add(d.getBigDecimal("monto"));
				i++;
			}
			
			Debug.logInfo("Mike productIdsCancel: " + productIdsCancel, MODULE);
			Debug.logInfo("Mike unitCostsCancel: " + unitCostsCancel, MODULE);
			Debug.logInfo("Mike quantitiesAcceptedCancel: " + quantitiesAcceptedCancel, MODULE);
			Debug.logInfo("Mike orderItemSeqIdsCancel: " + orderItemSeqIdsCancel, MODULE);
			Debug.logInfo("Mike claveMapCancel: " + claveMapCancel, MODULE);
			Debug.logInfo("Mike montoAplica: " + montoAplica, MODULE);
			
			GenericValue oderHeader = delegator.findByPrimaryKeyCache("OrderHeader", UtilMisc.toMap("orderId",purchaseOrderId));
			String invoiceId = crearInvoice(delegator,dispatcher,oderHeader.getString("billFromPartyId"), oderHeader.getString("billToPartyId"), new Timestamp(Calendar.getInstance().getTimeInMillis()), oderHeader.getString("currencyUom"), montoAplica, purchaseOrderId, userLogin, null, "PENA", "INGRESO");
    		if(invoiceId.equals("error")){
    			return ServiceUtil.returnError("Existio error al crear el Invoice");
    		}
    		String verE= crearInvoiceItem(dispatcher, delegator, invoiceId, productIdsCancel, unitCostsCancel, claveMapCancel, quantitiesAcceptedCancel, userLogin, orderItemSeqIdsCancel, purchaseOrderId, "INGRESO", null, null);        		
    		if(verE!=null){
    			return ServiceUtil.returnError("Existio error al crear el Invoice");
    		}
    		
    		GenericValue aplicaPena = search.get(0);
			aplicaPena.set("aplicaPenaId", aplicaPena.getString("aplicaPenaId"));
			aplicaPena.set("flag", "N");
			aplicaPena.set("invoiceId", invoiceId);
			delegator.store(aplicaPena); 
		}
		return result;
    }

    /**
     * Adjust both ATP and QOH by the same given amount.
     * @param dctx DispatchContext
     * @param context Map
     * @return Map
     * @throws GenericEntityException 
     */
    public static Map adjustInventoryQuantity(DispatchContext dctx, Map context) throws GenericEntityException {
    	
    	Debug.logInfo("contextoooo: " + context,MODULE);
    	
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        Map result = ServiceUtil.returnSuccess();
        Locale locale = UtilCommon.getLocale(context);
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        // ignoring 0 and empty variance quantity
        BigDecimal varianceQty = (BigDecimal) context.get("varianceQty");
        BigDecimal porcentajeDescuento = (BigDecimal) context.get("porcentajeDescuento");
        String fechaCont = (String) context.get("fechaContable");
        String pedidoInternoId = (String) context.get("pedidoInternoId");
        BigDecimal productQOH = (BigDecimal) context.get("productQOH");
        Map catalogoCargoMap = FastMap.newInstance();
        Map catalogoAbonoMap = FastMap.newInstance();
        Map montoMap = FastMap.newInstance();
        Map clavePresupuestalMap = FastMap.newInstance();
        String descrSinEspacios = "";
        
        Timestamp fechaContable = UtilDateTime.toTimestamp(UtilDateTime.stringToDate(fechaCont, UtilDateTime.getDateFormat(locale)));

        if (varianceQty == null || varianceQty.compareTo(BigDecimal.ZERO) == 0) {
            return ServiceUtil.returnSuccess();
        }
        try {
        	if((String) context.get("acctgTransTypeId") == null){
        		return ServiceUtil.returnError("Es necesario seleccionar un evento");
        	}
        	GenericValue varianceReason = delegator.findByPrimaryKey(
    				"VarianceReason",
    				UtilMisc.toMap("varianceReasonId", context.get("varianceReasonId")));
        	if(varianceReason.getString("flag").equals("N")){
        		varianceQty = varianceQty.negate();
        	}
        	
        	if(productQOH != null && varianceQty.compareTo(BigDecimal.ZERO) < 0){
        		if(productQOH.compareTo(BigDecimal.ZERO) > 0 && varianceQty.negate().compareTo(productQOH) > 0){
        			return ServiceUtil.returnError("No hay suficientes productos dentro del almacen para poder realizar la entrega");
        		}
        		
        	}
        	
        	Debug.logInfo("antes de ingresar: " + context,MODULE);
        	Debug.logInfo("porcentaje: " + porcentajeDescuento,MODULE);
            Map results = dispatcher.runSync("createPhysicalInventoryAndVariance", UtilMisc.toMap(
                 "inventoryItemId", context.get("inventoryItemId"),
                 "varianceReasonId", context.get("varianceReasonId"),
                 "availableToPromiseVar", varianceQty,
                 "quantityOnHandVar", varianceQty,
                 "userLogin", userLogin,
                 "generalComments", (String) context.get("revisarId"),
                 "personId", (String) context.get("partyId"),
                 "idTipoDoc", (String) context.get("acctgTransTypeId"),
                 "porcentajeDescuento", porcentajeDescuento,
                 "factura", (String) context.get("factura"),
                 "pedidoInternoId", pedidoInternoId,
                 "fechaContable",fechaContable));
            if (ServiceUtil.isError(results)) {
                return results;
            }
            

            String inventoryItemId = (String) context.get("inventoryItemId");
            String physicalInventoryId = (String) results.get("physicalInventoryId");
      		
            try {

                GenericValue inventoryVariance = delegator.findByPrimaryKey("InventoryItemVariance",
                        UtilMisc.toMap("inventoryItemId", inventoryItemId, "physicalInventoryId", physicalInventoryId));
                if (inventoryVariance == null) {
                    return ServiceUtil.returnError("No InventoryVariance entity record for inventoryItemId " + inventoryItemId + " and physicalInventoryId " + physicalInventoryId);
                }

                BigDecimal quantityOnHandVar = inventoryVariance.getBigDecimal("quantityOnHandVar");
                if (quantityOnHandVar == null || quantityOnHandVar.compareTo(ZERO) == 0) {
                    // no actual inventory loss or gain to account for
                    return ServiceUtil.returnSuccess();
                }

                GenericValue inventoryItem = inventoryVariance.getRelatedOne("InventoryItem");
                GenericValue physicalInventory = delegator.findByPrimaryKey("PhysicalInventory", UtilMisc.toMap("physicalInventoryId", physicalInventoryId));

                Debug.logInfo("Datos que se ingresaron inventoryItem: " + inventoryItem,MODULE);
                Debug.logInfo("Datos que se ingresaron physicalInventory: " + physicalInventory,MODULE);
                
//                if (quantityOnHandVar.compareTo(BigDecimal.ZERO) < 0) {
//                	if(quantityOnHandVar.negate().compareTo(inventoryItem.getBigDecimal("quantityOnHandTotal"))>0){
//                		return ServiceUtil.returnError("El id del elemento seleccionado no cuenta con la suficiente cantidad para surtir");
//                	}
//                }
                
                Map<String, String> tags = new HashMap<String, String>();
                if (inventoryItem != null) {
                    UtilAccountingTags.putAllAccountingTags(inventoryItem, tags);
                    Debug.logInfo("Making transaction entries with accounting tags from inventory item [" + inventoryItem + "] : " + tags, MODULE);
                }
                String productId = inventoryItem.getString("productId");
                // owner of inventory item
                String ownerPartyId = inventoryItem.getString("ownerPartyId");

                if (!UtilFinancial.hasActiveLedger(delegator, ownerPartyId)) {
                    return UtilMessage.createAndLogServiceFailure("FinancialsErrorNoActiveLedgerForParty", UtilMisc.toMap("partyId", ownerPartyId), locale, MODULE);
                }

                BigDecimal unitCost = null;
                unitCost = inventoryItem.getBigDecimal("unitCost");
                
                // get the currency conversion factor
                BigDecimal conversionFactor = UtilFinancial.determineUomConversionFactor(delegator, dispatcher, ownerPartyId, inventoryItem.getString("currencyUomId"));
                
                // validate the unitCost and compute transaction amount
                if (unitCost == null) {
                    return ServiceUtil.returnError("Could not determine unitCost of product [" + productId + "] for inventory variance [" + physicalInventoryId + "] and inventory item [" + inventoryItemId + "]");
                }
                // convert the intentory item's unit cost into the owner's currency
                unitCost = unitCost.multiply(conversionFactor).setScale(decimals, rounding);
                // The transaction amount is: amount = quantityOnHandVar * unitCost
                BigDecimal transactionAmount = unitCost.multiply(quantityOnHandVar).setScale(decimals, rounding);
                
                // get owner's party COGS method
                GenericValue acctgPref = delegator.findByPrimaryKeyCache("PartyAcctgPreference", UtilMisc.toMap("partyId", ownerPartyId));
                String cogsMethodId = acctgPref.getString("cogsMethodId");

                // If method is COGS_AVG_COST, also compute the inventory adjustment amount = (prodAvgCost - unitCost) * quantityOnHandVar
//                BigDecimal inventoryAdjAmount = null;
                if ((cogsMethodId != null) && (cogsMethodId.equals("COGS_AVG_COST"))) {
                    BigDecimal prodAvgCost = UtilCOGS.getProductAverageCost(productId, ownerPartyId, userLogin, delegator, dispatcher);
                    if (prodAvgCost == null) {
                        Debug.logWarning("Unable to find a product average cost for product [" + productId + "] in organization [" + ownerPartyId + "], no adjustment will be made in inventory variance", MODULE);
                    } else {
                        // TODO: there could be rounding issues here; maybe it's better to do something like this:
                        //       (prodAvgCost * quantityOnHandVar) - (unitCost * quantityOnHandVar) and then set the scale.
//                        inventoryAdjAmount = prodAvgCost.subtract(unitCost).multiply(quantityOnHandVar).setScale(decimals, rounding);
                    }
                }

                String agrup = buscarAgrupador(delegator,physicalInventory);
                if (transactionAmount.compareTo(BigDecimal.ZERO) < 0) {
                	transactionAmount = transactionAmount.negate();
                }
                Debug.logInfo("Agrupador: " + agrup,MODULE);
    			

                /************************************/
                /******** Motor Contable ************/
                /************************************/
                Map<String, Object> input = FastMap.newInstance();
                Map<String, Object> output = FastMap.newInstance();
                Map<String, Object> montoM = FastMap.newInstance();

                List<String> orderBy = UtilMisc.toList("secuencia");
        		List<GenericValue> listaLineasContables = delegator.findByAndCache("LineaContable", UtilMisc.toMap("acctgTransTypeId",(String) context.get("acctgTransTypeId")),orderBy);
        		for (GenericValue lineaCont : listaLineasContables) {
        			descrSinEspacios = lineaCont.getString("descripcion").replaceAll("\\s","")+0;
        			Debug.log("Descripcioooon: " + descrSinEspacios);
        			Debug.log("Productooooo: " + productId);
        			if(lineaCont.getString("catalogoCargo") != null){
                    	catalogoCargoMap.put(descrSinEspacios, productId);
                    }else if(lineaCont.getString("catalogoAbono") != null){
                    	catalogoAbonoMap.put(descrSinEspacios, productId);
                    }			
        		}

        		montoM.put("0", transactionAmount.toString());
                montoMap.put(descrSinEspacios, transactionAmount.toString());
                clavePresupuestalMap.put("0", "");
                
                context.put("montoMap", montoMap);
                context.put("clavePresupuestalMap", clavePresupuestalMap);
                context.put("catalogoCargoContMap", catalogoCargoMap);
                context.put("catalogoAbonoContMap", catalogoAbonoMap);
                
                Debug.log("Nuevo context: " + context);
                
                if(agrup == null){
                	Timestamp fechaTrans = new Timestamp(Calendar.getInstance().getTimeInMillis());
                	input.put("eventoContableId", (String) context.get("acctgTransTypeId"));
                	input.put("tipoClaveId", "");
                	input.put("fechaRegistro", fechaTrans);
                	input.put("fechaContable", fechaContable);
                	input.put("currency", inventoryItem.getString("currencyUomId"));
                	input.put("usuario", userLogin.getString("userLoginId"));
                	input.put("organizationId", ownerPartyId);
                	input.put("descripcion", physicalInventory.getString("generalComments"));
                	input.put("roleTypeId", "BILL_FROM_VENDOR");
                	input.put("campo", "physicalInventoryId");
                	input.put("valorCampo", physicalInventoryId);
                	input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables((String) context.get("acctgTransTypeId"), delegator, context, null, null, null, null));
                	
                	Debug.logInfo("Entra: " + input,MODULE);
                	output = dispatcher.runSync("creaTransaccionMotor", input);

                }else{
                	Debug.logInfo("Entra a crear la poliza sobre una ya existente: " + agrup,MODULE);
                	GenericValue trans = delegator.findByPrimaryKey("AcctgTrans",
            				UtilMisc.toMap("acctgTransId", agrup));
                	Debug.logInfo("Trans: " + trans,MODULE);
                	input.put("eventoContableId", (String) context.get("acctgTransTypeId"));
                	input.put("tipoClaveId", "");
    		        input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables((String) context.get("acctgTransTypeId"), delegator, context, null, null, null, null));
    		        input.put("transaccion", trans);
    		        
    		        output = dispatcher.runSync("agregaEventoTransaccion", input);
                	
                }

                if(ServiceUtil.isError(output)){
            		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
            	}
            	GenericValue transaccion = (GenericValue) output.get("transaccion");

            	Debug.log("TRansaccion: " + transaccion);  

    			//get variance reason sale
    			BigDecimal montoVenta = BigDecimal.ZERO;
    			BigDecimal montoDescuento = BigDecimal.ZERO;
    			BigDecimal porcentajeDescuento2 = BigDecimal.ZERO;
                if(inventoryVariance.getString("varianceReasonId").equals("VAR_PUBLIC") || inventoryVariance.getString("varianceReasonId").equals("VAR_COUNTER")){
                	Debug.log("Entra a calcular precio de venta");
                	EntityCondition condicion1 = EntityCondition.makeCondition(EntityOperator.AND, 
        					EntityCondition.makeCondition("productId",	EntityOperator.EQUALS, productId),
        					EntityCondition.makeCondition("productPriceTypeId", EntityOperator.EQUALS, "LIST_PRICE"));

        			Debug.log("Mike condiciones: " + condicion1);

        			List<GenericValue> busqueda = delegator.findByCondition("ProductPrice", condicion1,	null, UtilMisc.toList("createdDate DESC"));
        			
        			Debug.log("ProductPrice: " + busqueda);
        			
        			if(!busqueda.isEmpty()){
        				Iterator<GenericValue> itemId = busqueda.iterator();
            			GenericValue item = itemId.next();
            			if(item.getBigDecimal("price") != null){
            				montoVenta = (quantityOnHandVar.negate()).multiply(item.getBigDecimal("price"));
            			}
        			}
        			porcentajeDescuento2 = physicalInventory.getBigDecimal("porcentajeDescuento");
        			if(porcentajeDescuento2 != null && montoVenta.compareTo(BigDecimal.ZERO) != 0){
        				montoDescuento = montoVenta.subtract(montoVenta.multiply(porcentajeDescuento2.divide(new BigDecimal(100))));
        			}else if(porcentajeDescuento2 != null && montoVenta.compareTo(BigDecimal.ZERO) == 0){
        				montoDescuento = transactionAmount.subtract(transactionAmount.multiply(porcentajeDescuento2.divide(new BigDecimal(100))));
        			}

                }
                
    			if(transaccion.getString("acctgTransId") != null){
            		GenericValue operacionP = GenericValue.create(delegator
    						.getModelEntity("PhysicalInventory"));
    				operacionP.set("physicalInventoryId", physicalInventoryId);
    				operacionP.set("generalComments", (String) context.get("revisarId"));
    				operacionP.set("acctgTransId", transaccion.getString("acctgTransId"));
    				operacionP.set("agrupador", transaccion.getString("poliza"));
    				if(montoVenta.compareTo(BigDecimal.ZERO) == 0){
    					operacionP.set("montoVenta", transactionAmount);
    				}else{
    					operacionP.set("montoVenta", montoVenta);
    				}
    				operacionP.set("montoDescuento", montoDescuento);
    				operacionP.set("porcentajeDescuento", porcentajeDescuento);
    				delegator.createOrStore(operacionP);
            	}
    			
    			Debug.logInfo("acctgTransId: " + transaccion.getString("acctgTransId"),MODULE);
                result.put("acctgTransId",transaccion.getString("acctgTransId"));
                return result;
            } catch (GenericEntityException ex) {
                return ServiceUtil.returnError(ex.getMessage());
            } catch (GenericServiceException ex) {
                return ServiceUtil.returnError(ex.getMessage());
            }
			
        } catch (GenericServiceException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
    }
    
    public static String buscarAgrupador(Delegator delegator,GenericValue physicalInventoryActual)
			throws GenericEntityException {
		Debug.logInfo("Entra a buscarAgrupador",MODULE);
		
		String factura = physicalInventoryActual.getString("factura");
		String partyId = physicalInventoryActual.getString("partyId");
		
		String agrupador = null;
		
		List<GenericValue> listPhysicalInventory = delegator.findByAnd("PhysicalInventory", 
				UtilMisc.toMap("factura",factura,"partyId",partyId));
		GenericValue physicalInventoryAnterior = EntityUtil.getFirst(listPhysicalInventory);
		
		if(UtilValidate.isNotEmpty(physicalInventoryAnterior)){
			agrupador = physicalInventoryAnterior.getString("acctgTransId");
		}

		Debug.logInfo("Agrupador: " + agrupador,MODULE);
		return agrupador;
	}

    
    public static Map receiveSingleItem(DispatchContext dctx, Map context) throws GenericServiceException, GenericEntityException {
    	
    	Debug.logInfo("Entramos al nuevo servicio: " + context,MODULE);
    	
    	 LocalDispatcher dispatcher = dctx.getDispatcher();
         Delegator delegator = dctx.getDelegator();

         Map result = ServiceUtil.returnSuccess();
         
         GenericValue userLogin = (GenericValue) context.get("userLogin");

         BigDecimal quantityAccepted = (BigDecimal) context.get("quantityAccepted");
         String acctgTransTypeId = (String) context.get("acctgTransTypeId");
         String productId = (String) context.get("productId");
         Timestamp datetimeReceived = (Timestamp) context.get("datetimeReceived");
         Map catalogoCargoMap = (Map) context.get("catalogoCargoContMap");
         Map catalogoAbonoMap = (Map) context.get("catalogoAbonoContMap");
         Map montoMap = (Map) context.get("montoMap");
         Map clavePresupuestalMap = (Map) context.get("clavePresupuestalMap");
         BigDecimal unitCost = null;
         String descrSinEspacios = null;
         
         List<String> orderBy = UtilMisc.toList("secuencia");
 		 List<GenericValue> listaLineasContables = delegator.findByAndCache("LineaContable", UtilMisc.toMap("acctgTransTypeId",acctgTransTypeId),orderBy);
 		 for (GenericValue lineaCont : listaLineasContables) {
 			descrSinEspacios = lineaCont.getString("descripcion").replaceAll("\\s","")+0;
 			Debug.log("Descripcioooon: " + descrSinEspacios);
 			
 			if(!montoMap.isEmpty()){
 	        	 Debug.log("Monto: " + BigDecimal.valueOf(Double.valueOf((String) montoMap.get(descrSinEspacios))));
 	        	 unitCost = BigDecimal.valueOf(Double.valueOf((String) montoMap.get(descrSinEspacios)));
 	         }
 	         if(!catalogoCargoMap.isEmpty()){
 	        	 productId = (String) catalogoCargoMap.get(descrSinEspacios);
 	         }else if(!catalogoAbonoMap.isEmpty()){
 	        	 productId = (String) catalogoAbonoMap.get(descrSinEspacios);
 	         }
 		 }
 		 
 		 Debug.log("Productoooo: " + productId);
         
         if (UtilValidate.isNotEmpty(productId)) {
             context.put("productId", productId);
         }
         
         if (UtilValidate.isNotEmpty((String) montoMap.get(descrSinEspacios))) {
             context.put("unitCost", unitCost);
         }
         
         if (UtilValidate.isEmpty(context.get("quantityAccepted")) || ((BigDecimal) context.get("quantityAccepted")).doubleValue() <= 0) {
            // continue;
         }
         
         context.put("inventoryItemTypeId", "NON_SERIAL_INV_ITEM");
         
         context.put("fechaContable", datetimeReceived);
         // Call the receiveInventoryProduct service
         Debug.log("Nuevo contexto: " + context);
         Map receiveInvContext = dctx.getModelService("receiveInventoryProduct").makeValid((Map) context, ModelService.IN_PARAM);
         receiveInvContext.put("datetimeReceived", datetimeReceived);
         Map receiveInvResult = dispatcher.runSync("receiveInventoryProduct", receiveInvContext);
         if (ServiceUtil.isError(receiveInvResult)) {
             Debug.logError(ServiceUtil.getErrorMessage(receiveInvResult), MODULE);
             return receiveInvResult;
         }

         Debug.log("Mikeeeee: " + receiveInvResult);
         
         Debug.log("Ingresa a postShipmentReceipt1: " + context);
         
         try {

         	 String receiptId = (String) receiveInvResult.get("receiptId");
             GenericValue receipt = delegator.findByPrimaryKey("ShipmentReceipt", UtilMisc.toMap("receiptId", receiptId));
             
             Debug.log("receiptId" + receipt);
             
             productId = receipt.getString("productId");
             BigDecimal quantityReceived = receipt.getBigDecimal("quantityAccepted");
             GenericValue inventoryItem = receipt.getRelatedOne("InventoryItem");
             GenericValue orderHeader = receipt.getRelatedOne("OrderHeader");
             String inventoryItemId = inventoryItem.getString("inventoryItemId");
             Map productIds = null;
             
             if(catalogoCargoMap != null){
             	productIds = catalogoCargoMap;
             }else if(catalogoAbonoMap != null){
             	productIds = catalogoAbonoMap;
             }

             // For now, skip serialized inventory items
             if ("SERIALIZED_INV_ITEM".equals(inventoryItem.get("inventoryItemTypeId"))) {
                 Debug.logInfo("postShipmentReceiptToGl:  Encountered serialized InventoryItem [" + inventoryItemId + "].  Not posting this shipment receipt to GL.", MODULE);
                 return ServiceUtil.returnSuccess();
             }
             
             Debug.log("Ingresa a postShipmentReceipt2.1" + orderHeader);
             //Obtenemos la organizacion donde se contabilizara 
            // String organizationCont = orderHeader.getString("billToPartyId");

             // get the inventory item's owner and party
             String organizationPartyId = inventoryItem.getString("ownerPartyId");
             Debug.log("Ingresa a postShipmentReceipt2.3" + inventoryItem.getBigDecimal("unitCost"));
             // get the inventory item unit cost
             unitCost = inventoryItem.getBigDecimal("unitCost");

             Debug.log("Ingresa a postShipmentReceipt3");
             // Skip this inventory item if the owner is not an internal organization
             if (UtilValidate.isNotEmpty(organizationPartyId) && !UtilFinancial.hasPartyRole(organizationPartyId, "INTERNAL_ORGANIZATIO", delegator)) {
                 Debug.logInfo("postShipmentReceiptToGl:  Owner Party [" + organizationPartyId + "] of InventoryItem [" + inventoryItemId + "] is not an internal organization.  Not posting this shipment receipt to GL.", MODULE);
                 return ServiceUtil.returnSuccess();
             }

             // get the currency conversion factor
             BigDecimal conversionFactor = UtilFinancial.determineUomConversionFactor(delegator, dispatcher, organizationPartyId, inventoryItem.getString("currencyUomId"));

             Debug.log("Ingresa a postShipmentReceipt4");
             // validate the unitCost and compute transaction amount
             if (unitCost == null) {
                 return ServiceUtil.returnError("Could not determine unitCost of product [" + productId + "] from shipment receipt [" + receiptId + "]");
             }
             BigDecimal transactionAmount = unitCost.multiply(quantityReceived).multiply(conversionFactor).setScale(decimals, rounding);
             
             montoMap.put(descrSinEspacios, transactionAmount.toString());
             clavePresupuestalMap.put("0", "");
             context.put("clavePresupuestalMap", clavePresupuestalMap);

             Debug.log("Ingresa a postShipmentReceipt5");
             
             /************************************/
             /******** Motor Contable ************/
             /************************************/
             
             Timestamp fechaTrans = new Timestamp(Calendar.getInstance().getTimeInMillis());
             Map<String, Object> input = FastMap.newInstance();
             input.put("eventoContableId", (String) context.get("acctgTransTypeId"));
             input.put("tipoClaveId", "");
             input.put("fechaRegistro",fechaTrans);
             input.put("fechaContable", (Timestamp) context.get("datetimeReceived"));
             input.put("currency", inventoryItem.getString("currencyUomId"));
             input.put("usuario", userLogin.getString("userLoginId"));
             input.put("organizationId", organizationPartyId);
             input.put("descripcion", (String) context.get("itemDescription"));
             input.put("roleTypeId", "BILL_FROM_VENDOR");
             input.put("campo", "receiptId");
             input.put("valorCampo", receiptId);
 	         input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables((String) context.get("acctgTransTypeId"), delegator, context, null,null, montoMap, productIds));
 	        
 	        Map<String, Object> output = dispatcher.runSync("creaTransaccionMotor", input);
         	if(ServiceUtil.isError(output)){
         		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
         	}
         	
         	GenericValue transaccion = (GenericValue) output.get("transaccion");

         	Debug.log("TRansaccion: " + transaccion);
         	
         	GenericValue operacionP = GenericValue.create(delegator
    				.getModelEntity("ShipmentReceipt"));
    		operacionP.set("receiptId", receiptId);
    		operacionP.set("poliza", transaccion.getString("poliza"));
    		operacionP.set("acctgTransId", transaccion.getString("acctgTransId"));
    		delegator.createOrStore(operacionP);
         	
         	result.put("inventoryItemId", inventoryItemId);
         	result.put("receiptId", receiptId);
         	
         	registrarMaximosMinimos(delegator, quantityAccepted, productId);
         	
         	return result;
         } catch (GenericEntityException ee) {
             return ServiceUtil.returnError(ee.getMessage());
         }
         
    }
    
    public static void buscaPedidoInterno(Map<String, Object> context) throws GeneralException, ParseException {
    	
//    	 Debug.log("Mike contextooBuscar.- " + context);

        final ActionContext ac = new ActionContext(context);

        DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
        final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();
        
        String pedidoInternoId = ac.getParameter("pedidoInternoId");
        
        if(UtilValidate.isNotEmpty(pedidoInternoId)){
        	List<EntityCondition> searchCondition = new FastList<EntityCondition>();
        	searchCondition.add(EntityCondition.makeCondition(SurtirPedidoInternoParcial.Fields.pedidoInternoId.name(), EntityOperator.EQUALS, pedidoInternoId));
            
            Debug.log("Mike condiciones numPedido.- " + searchCondition);
            
            List<SurtirPedidoInternoParcial> surtir = ledgerRepository.findList(SurtirPedidoInternoParcial.class, searchCondition);
            Debug.log("Mike surtir.- " + surtir);
            List<Map<String, Object>> surtirList = new FastList<Map<String, Object>>();
            for (SurtirPedidoInternoParcial s : surtir) {
                Map<String, Object> map = s.toMap();
                surtirList.add(map);
            }
            Debug.log("Mike surtirList.- " + surtirList);
            ac.put("datosPedido", surtirList);
        }

    }
    
    public static Map guardaInfoSurtirPedido(DispatchContext dctx, Map context) throws GenericEntityException, GenericServiceException, RepositoryException {
    	Debug.logInfo("Mike guarda info: " + context,MODULE);
    	
        Delegator delegator = dctx.getDelegator();
        Map result = ServiceUtil.returnSuccess();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String pedidoInternoId = (String) context.get("pedidoInternoId");
        Date fechaContable = (Date) context.get("fechaContable");
        String numeroFactura = (String) context.get("numeroFactura");
        String comentario = (String) context.get("comentario");
        String acctgTransTypeId = (String) context.get("acctgTransTypeId");
        
        cambiaStatus(delegator, userLogin.getString("userLoginId"));
        
        GenericValue guardarPedido = GenericValue.create(delegator
				.getModelEntity("SurtirPedidoInterno"));
		guardarPedido.set("agrupador", delegator.getNextSeqId("SurtirPedidoInterno"));
		guardarPedido.set("fechaTransaccion", UtilDateTime.nowTimestamp());
		guardarPedido.set("fechaContable", UtilDateTime.toTimestamp(fechaContable));
		guardarPedido.set("status", "N");
		guardarPedido.set("numPedido", pedidoInternoId);
		guardarPedido.set("userLoginId", userLogin.getString("userLoginId"));
		guardarPedido.set("numeroFactura", numeroFactura);
		guardarPedido.set("comentario", comentario);
		guardarPedido.set("acctgTransTypeId", acctgTransTypeId);
		delegator.create(guardarPedido);
        
		result.put("pedidoInternoId", pedidoInternoId);
		
        return result;
    }
    
    public static void cambiaStatus(Delegator delegator, String userLoginId)
			throws GenericEntityException, RepositoryException {

		Debug.log("Ingresa a cambiar el estatus");
		
		EntityCondition condition = EntityCondition.makeCondition(EntityOperator.AND, 
				  EntityCondition.makeCondition("status", EntityOperator.EQUALS, "N"),
				  EntityCondition.makeCondition("userLoginId", EntityOperator.EQUALS, userLoginId));
		
		List<String> orderBy = org.ofbiz.base.util.UtilMisc.toList("agrupador");

		List<GenericValue> listDetalle = delegator.findByCondition("SurtirPedidoInterno", condition, null,
        		orderBy);

		if (listDetalle.size() != 0) {
			GenericValue operacionP = actualizaSurtido(delegator,
					listDetalle.get(0).getString("agrupador"));
			delegator.store(operacionP);
		}
	}
    
    public static GenericValue actualizaSurtido(Delegator delegator,
			String agrupadorP) {
		
		Debug.log("Ingresa a actualizar el surtido");
		
		GenericValue operacionP = GenericValue.create(delegator
				.getModelEntity("SurtirPedidoInterno"));
		operacionP.set("agrupador",
				agrupadorP);
		operacionP.set("status", "Y");

		return operacionP;
	}
    
    public static Map<String,Object> surtirPedido(DispatchContext dctx, Map<String,Object> context) throws GenericEntityException, GenericServiceException {
    	
    	Debug.logInfo("Mike Surtir contextoooo: " + context,MODULE);
    	
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        Map result = ServiceUtil.returnSuccess();
        String pedidoInternoId = (String) context.get("pedidoInternoId");
        Date fechaContable = (Date) context.get("fechaContable");
        String numeroFactura = (String) context.get("numeroFactura");
        String comentario = (String) context.get("comentario");
        String acctgTransTypeId = (String) context.get("acctgTransTypeId");
        String organizationPartyId = (String) context.get("organizationPartyId");
        String almacenId = (String) context.get("almacenId");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Map<String,String> mapDetallePedidoInternoId = (Map) context.get("mapDetallePedidoInternoId");
        Map<String,String> mapVarianceQty = (Map) context.get("mapVarianceQty");
        Locale locale = (Locale) context.get("locale");
        TimeZone timeZone = (TimeZone) context.get("timeZone");
        String estatusId = (String) context.get("estatusId");
        String acctgTransId = null;
        
        Map<String,Object> mapGuardaSutirPedido = FastMap.newInstance();
        mapGuardaSutirPedido.put("pedidoInternoId", pedidoInternoId);
        mapGuardaSutirPedido.put("fechaContable", fechaContable);
        mapGuardaSutirPedido.put("numeroFactura", numeroFactura);
        mapGuardaSutirPedido.put("comentario", comentario);
        mapGuardaSutirPedido.put("acctgTransTypeId", acctgTransTypeId);
        mapGuardaSutirPedido.put("almacenId", almacenId);
        mapGuardaSutirPedido.put("organizationPartyId", organizationPartyId);
        mapGuardaSutirPedido.put("userLogin", userLogin);
        
        Map resultGuardaSurtirPedido =  dispatcher.runSync("guardaInfoSurtirPedido", mapGuardaSutirPedido);
        
        if(ServiceUtil.isError(resultGuardaSurtirPedido)){
        	throw new GenericEntityException(ServiceUtil.getErrorMessage(resultGuardaSurtirPedido));
        }
        
        GenericValue PedidoInterno = delegator.findByPrimaryKey("PedidoInterno",UtilMisc.toMap("pedidoInternoId",pedidoInternoId));
        
        for (Map.Entry<String, String> detallePedidoId : mapDetallePedidoInternoId.entrySet()){
			String detallePedidoInternoId = detallePedidoId.getValue();
			Double numeroDo = 0.0;
			if(UtilValidate.isNotEmpty(mapVarianceQty.get(detallePedidoId.getKey()))){
				numeroDo = Double.valueOf(mapVarianceQty.get(detallePedidoId.getKey()));
			}
			BigDecimal varianceQty = BigDecimal.valueOf(numeroDo);
			
	        if (varianceQty == null || varianceQty.compareTo(BigDecimal.ZERO) == 0) {
	            continue;
	        }
	        GenericValue detallePedido = delegator.findByPrimaryKey("DetallePedidoInterno",
	                UtilMisc.toMap("pedidoInternoId", pedidoInternoId, "detallePedidoInternoId", detallePedidoInternoId));
	        
			int cantidad = varianceQty.intValue();
			String productId = detallePedido.getString("productId");
			
			if(detallePedido.getLong("cantidad").intValue() >= (detallePedido.getLong("cantidadEntregada").intValue()+cantidad)){
	        		
					EntityCondition conditions1 = EntityCondition.makeCondition(
							EntityOperator.AND, EntityCondition.makeCondition("productId",
									EntityOperator.EQUALS, productId), EntityCondition.makeCondition("facilityId",
											EntityOperator.EQUALS, almacenId));
					
					List<String> orderBy = UtilMisc.toList("datetimeReceived ASC");
					List<GenericValue> inventoryItem = delegator.findByCondition(
							"InventoryItem", conditions1, null, orderBy);
					
					if(!inventoryItem.isEmpty()){
						int cantidadSuf = 0;
						
						for (GenericValue InventoryItem : inventoryItem) {
							cantidadSuf = cantidadSuf + InventoryItem.getBigDecimal("availableToPromiseTotal").intValue();
							 if(cantidadSuf == cantidad){
								 break;
							 }
						} 
						
						if(cantidadSuf < cantidad){
							return ServiceUtil.returnError("No hay suficientes productos [" + productId +"] en el inventario ["+almacenId+"]");
						}
						
						 if(cantidadSuf == cantidad || cantidadSuf > cantidad){

							 int cantidadEntrega = 0;
							 int cantidadFaltante = cantidad;
							 int cantidadInv = 0;

							 for(int k=0;cantidadFaltante>0;k++){
								 cantidadSuf = 0;
								 cantidadInv = inventoryItem.get(k).getBigDecimal("availableToPromiseTotal").intValue();
								 if(cantidadInv > 0){
									 
									 if(cantidadInv == cantidadFaltante){
										 cantidadEntrega = cantidadEntrega + cantidadFaltante;
										 cantidadSuf = cantidadFaltante;
									 }else if(cantidadInv > cantidadFaltante){
										 cantidadEntrega = cantidadEntrega + cantidadFaltante;
										 cantidadSuf = cantidadFaltante;
									 }else if(cantidadInv < cantidadFaltante){
										 cantidadEntrega = cantidadEntrega + cantidadInv;
										 cantidadSuf = cantidadInv;
									 }
									 
									 cantidadFaltante = cantidad - cantidadEntrega;
									 
									 Map<String, Object> input = FastMap.newInstance();
									 input.put("inventoryItemId", inventoryItem.get(k).getString("inventoryItemId"));
									 input.put("varianceReasonId", "VAR_ORDER");
									 input.put("varianceQty", new BigDecimal(cantidadSuf));
									 input.put("revisarId", comentario);
									 input.put("partyId", organizationPartyId);
									 input.put("acctgTransTypeId", acctgTransTypeId);
									 input.put("factura", numeroFactura);
									 input.put("fechaContable", UtilDate.dateToString(fechaContable,UtilDateTime.getDateFormat(locale), timeZone, locale));
									 input.put("pedidoInternoId", pedidoInternoId);
									 input.put("userLogin", userLogin);
									 input.put("locale", locale);
					             
									 Map<String, Object> output = dispatcher.runSync("adjustInventoryQuantity", input);
					             
									 if(ServiceUtil.isError(output)){
						         		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
									 }

									 acctgTransId = (String) output.get("acctgTransId"); 
								 }
							 }

						 }else{
							 return ServiceUtil.returnError("No hay suficientes productos de " + productId);
						 }
					} else {
						return ServiceUtil.returnError("No hay productos para surtir " + productId);
					}
					
			}else{
				return ServiceUtil.returnError("La cantidad que se quiere entregar supera la posible a entregar para el producto: " + productId);
			}
			
			Long l = Long.parseLong(String.valueOf(detallePedido.getLong("cantidadEntregada").intValue()+cantidad));
			detallePedido.set("cantidadEntregada", l);
			delegator.store(detallePedido); 
	        
		}
		
		if(estatusId.equals("CERRAR")){
			PedidoInterno.set("statusId", "SURTIDO");
    		delegator.store(PedidoInterno);
		}
		
		GenericValue AcctgTrans = delegator.findByPrimaryKey("AcctgTrans", UtilMisc.toMap("acctgTransId",acctgTransId));
		
		result.putAll(ServiceUtil.returnSuccess("El requerimiento fue entregado correctamente , se gener\u00f3 el n\u00famero de p\u00f3liza "+AcctgTrans.getString("poliza")));
		
		result.put("pedidoInternoId", pedidoInternoId);
		result.put("acctgTransId", acctgTransId);
		return result;

    }
    
    public static void buscarInventario(Map<String, Object> context) throws RepositoryException, ListBuilderException, ParseException {
    	
    	final ActionContext ac = new ActionContext(context);
    	final TimeZone timeZone = ac.getTimeZone();
    	Locale locale = ac.getLocale();
    	
        DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
        final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();
        
        String facilityId = ac.getParameter("facilityId");
        String productId = ac.getParameter("productId");
        final String motivoId = ac.getParameter("motivoId");
    	String varianceReasonId = ac.getParameter("varianceReasonId");
    	String fechaContableDesde = ac.getParameter("fechaContableDesde");
    	String fechaContableHasta = ac.getParameter("fechaContableHasta");  
    	try
    	{
    	
	    	String dateFormat = UtilDateTime.getDateFormat(locale);
	    	
	    	List<EntityCondition> searchConditions = new FastList<EntityCondition>();
	        if (UtilValidate.isNotEmpty(facilityId) && facilityId != null) {
	            searchConditions.add(EntityCondition.makeCondition("facilityId", EntityOperator.EQUALS, facilityId));  
	        }         
	        if (UtilValidate.isNotEmpty(productId) && productId != null) {
	            searchConditions.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId));        
	        }
	        if (UtilValidate.isNotEmpty(varianceReasonId) && varianceReasonId != null) {
	        	searchConditions.add(EntityCondition.makeCondition("reasonEnumId", EntityOperator.EQUALS, varianceReasonId));
	        }
	        
	        if(varianceReasonId == null){
	        	ac.put("error", "Se debe de ingresar el motivo");
	        }else if(!varianceReasonId.equals("VAR_RECEIPT")){
	        	if (UtilValidate.isNotEmpty(fechaContableDesde) && fechaContableDesde != null) {
		        	searchConditions.add(EntityCondition.makeCondition("physicalInventoryDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.getDayStart(UtilDateTime.stringToTimeStamp(fechaContableDesde, dateFormat, timeZone, locale), timeZone, locale)));
		        }
		        if (UtilValidate.isNotEmpty(fechaContableHasta) && fechaContableHasta != null) {
		        	searchConditions.add(EntityCondition.makeCondition("physicalInventoryDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.getDayStart(UtilDateTime.stringToTimeStamp(fechaContableHasta, dateFormat, timeZone, locale), timeZone, locale)));
		        }
		        
		        EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
		        Debug.log("condiciones: " + condiciones );
		        
		        List<String> fieldsToSelect = UtilMisc.toList("physicalInventoryDate", "generalComments", "acctgTransId", "agrupador", "factura",
		        		"quantityOnHandDiff", "unitCost", "currencyUomId", "productId", "productName");
		        List<String> orderBy = UtilMisc.toList("acctgTransId");
		        EntityListBuilder physicalListBuilder = new EntityListBuilder(ledgerRepository, PhysicalInventorySearch.class, condiciones, fieldsToSelect, orderBy);
		        Debug.log("Mike physicalListBuilder: " + physicalListBuilder );
		        PageBuilder<PhysicalInventorySearch> pageBuilder = new PageBuilder<PhysicalInventorySearch>() {
		            public List<Map<String, Object>> build(List<PhysicalInventorySearch> page) throws Exception {
		            	Debug.log("Mike mapa");
		                List<Map<String, Object>> newPage = FastList.newInstance();
		                for (PhysicalInventorySearch pInventory : page) {
		                    Map<String, Object> newRow = FastMap.newInstance();
		                    if(motivoId.equals("A") && pInventory.getQuantityOnHandDiff().compareTo(BigDecimal.ZERO)>0){
		                    	newRow.putAll(pInventory.toMap()); 
		                    	newPage.add(newRow);
		                    }else if(motivoId.equals("B") && pInventory.getQuantityOnHandDiff().compareTo(BigDecimal.ZERO)<0){
		                    	newRow.putAll(pInventory.toMap());                    
			                    newRow.put("quantityOnHandDiff", pInventory.getQuantityOnHandDiff().negate());                                        
			                    newPage.add(newRow);
		                    } 
		                }
		                return newPage;
		            }
		        };
		        
		        physicalListBuilder.setPageBuilder(pageBuilder);
		        ac.put("physicalListBuilder", physicalListBuilder);
		        Debug.log("Mike physicalListBuilder.- " + physicalListBuilder);
	        }
	        
    	}
    	catch (ParseException e) {		
			e.printStackTrace();
		}
    }
    
    public static String crearInvoice(Delegator delegator, LocalDispatcher dispatcher, String organizationPartyId, String proveedor, Timestamp fechaContable, 
    							String moneda, BigDecimal suma, String orderId, GenericValue userLogin, String numeroFactura, String comentario, String tipo)
			throws GenericEntityException 	
	{	Debug.log("Entra a crearInvoice");
		String invoiceId = "";
		try 
		{
			Map<String, Object> input = FastMap.newInstance();
			String evento = null;
			if(tipo.equals("INGRESO")){
				input = UtilMisc.<String, Object>toMap("invoiceTypeId", "SALES_INVOICE", "statusId", "INVOICE_IN_PROCESS");
				evento = "EJE_PE";
			}else{
				input = UtilMisc.<String, Object>toMap("invoiceTypeId", "PURCHASE_INVOICE", "statusId", "INVOICE_IN_PROCESS");
				evento = "PEEJ-02";
			}
			
		    input.put("partyId", organizationPartyId);
		    input.put("partyIdFrom", proveedor);
		    input.put("currencyUomId", moneda);
		    input.put("invoiceDate", fechaContable);
		    input.put("description", comentario+" ( Recepci\u00f3n parcial de orden: " + orderId+" )");
		    input.put("invoiceAdjustedTotal", suma);
		    input.put("invoiceTotal", suma);
		    input.put("openAmount", suma);
		    input.put("pendingOpenAmount", suma);
		    input.put("userLogin", userLogin);
		    input.put("referenceNumber", numeroFactura);
		    input.put("acctgTransTypeId", evento);		    		
		    
		    EntityCondition condicionTipoPago = EntityCondition.makeCondition("parentTypeId",EntityOperator.EQUALS,"DISBURSEMENT");
		    List<GenericValue> paymentType = delegator.findByCondition("PaymentType", condicionTipoPago , null, null);
		    
		    for(GenericValue payment : paymentType){
		    	
		    	if(payment.getString("paymentTypeId").equals("ELECT_TRANSF")){
		    		input.put("paymentTypeId", "ELECT_TRANSF");
		    		break;
		    	}else{
		    		
		    		input.put("paymentTypeId",payment.getString("paymentTypeId"));		    		
		    	}
		    }
		    
		    Debug.log("input: " + input);
		    Map<String, Object> result;			
			result = dispatcher.runSync("createInvoice", input);
			Debug.log("result: " + result);
			if (ServiceUtil.isError(result)) {
				Debug.log("Hubo error al crear el Invoice");
				return "error";
			}
			else
			{	
				invoiceId = (String) result.get("invoiceId");				
			}								
		} 
		catch (GenericServiceException e) 
		{	Debug.logError("Hubo un error al crear el Invoice", MODULE);
			return "error";
		}
		return invoiceId;	      
	}
    
    public static String crearInvoiceItem(LocalDispatcher dispatcher, Delegator delegator, String invoiceId, Map productIds, Map unitCosts, Map clavePresupuestalMap, 
    		Map quantitiesAccepted, GenericValue userLogin, Map orderItemSeqIds, String purchaseOrderId, String tipo, Map rowDeductivas, Timestamp fechaContable) throws GenericEntityException 	
	{		
		try 
		{	Map<String, Object> inputItem = FastMap.newInstance();
			Map<String, GenericValue> claves = generaMapa(delegator,tipo);
			int invoiceItemSeqNum = 1;
            
			// Recorremos cada lineaMotor para crear un ItemInvoice
			for(int i=0; i<unitCosts.size(); i++)
			{	if(quantitiesAccepted.get(Integer.toString(i)) != null && !quantitiesAccepted.get(Integer.toString(i)).equals(""))
				{	if(!quantitiesAccepted.get(Integer.toString(i)).equals("0"))
					{	
						String invoiceItemSeqId = UtilFormatOut.formatPaddedNumber(invoiceItemSeqNum, INVOICE_ITEM_DIGITOS);
						inputItem.put("invoiceId", invoiceId);
						inputItem.put("invoiceItemSeqId", invoiceItemSeqId);
						inputItem.put("invoiceItemTypeId", "INV_FPROD_ITEM");
						inputItem.put("userLogin", userLogin);
						inputItem.put("uomId", "MXN");					
						BigDecimal amount = BigDecimal.valueOf(Double.valueOf(unitCosts.get(Integer.toString(i)).toString()));
						inputItem.put("quantity", BigDecimal.ONE);
						inputItem.put("amount", amount);
						inputItem.put("montoRestante", amount);
						inputItem.put("productId", productIds.get(Integer.toString(i)));
						// Guardamos las clasificaciones.
						inputItem = seteaClasificacionesItem(claves, inputItem, clavePresupuestalMap.get(Integer.toString(i)).toString());									
						
						Map<String, Object> result;
						result = dispatcher.runSync("createInvoiceItem", inputItem);
						if (ServiceUtil.isError(result)) 
						{	Debug.log("Error al crear InvoiceItem");
							return "error";
						}
						invoiceItemSeqNum++;
						String verE= crearOrderItemBilling(dispatcher, invoiceId, invoiceItemSeqId, purchaseOrderId, orderItemSeqIds.get(Integer.toString(i)).toString(), amount, userLogin);
						if(verE!=null){
							return "error";
						}
					}
				}				
			}
			return null;
		} 
		catch (GenericServiceException e) 
		{	Debug.logError("Hubo error al crear el Invoice Item", MODULE);
			return "error";
		}
		    
	}
    
    public static String crearOrderItemBilling(LocalDispatcher dispatcher, String invoiceId, String invoiceItemSeqId, String purchaseOrderId, String orderItemSeqId, BigDecimal amount, GenericValue userLogin) throws GenericEntityException 	
	{	Debug.log("Entra a crear OrderItemBilling");	
		try 
		{	Map<String, Object> createOrderItemBillingContext = FastMap.newInstance();
	        createOrderItemBillingContext.put("invoiceId", invoiceId);
	        createOrderItemBillingContext.put("invoiceItemSeqId", invoiceItemSeqId);
	        createOrderItemBillingContext.put("orderId", purchaseOrderId);
	        createOrderItemBillingContext.put("orderItemSeqId", orderItemSeqId);
	        createOrderItemBillingContext.put("quantity", BigDecimal.ZERO);
	        createOrderItemBillingContext.put("amount", amount);
	        createOrderItemBillingContext.put("userLogin", userLogin);               
	
	        Map<String, Object> createOrderItemBillingResult = dispatcher.runSync("createOrderItemBilling", createOrderItemBillingContext);
	        if (ServiceUtil.isError(createOrderItemBillingResult)) {
	        	Debug.log("Error al crear OrderItemBilling");
	        	return "error";
	        }	
	        return null;
		} 
		catch (GenericServiceException e) 
		{	Debug.logError("Hubo un error al crear el Invoice billing", MODULE);
			return "error";
		}
		    
	}
    
    private static Map<String, Object> seteaClasificacionesItem(
			Map<String, GenericValue> claves, Map<String, Object> item,
			String clave) {
		GenericValue generic = claves.get(clave);
		for (int i = 1; i < 16; i++) {
			item.put("clasificacion" + i,
					generic.getString("clasificacion" + i));
		}
		return item;
	}
    
    private static Map<String, GenericValue> generaMapa(Delegator delegator, String tipo)
			throws GenericEntityException {
		List<GenericValue> claves = delegator.findByAnd("ClavePresupuestal",
				"tipo", tipo);
		Map<String, GenericValue> mapaClave = new HashMap<String, GenericValue>();
		for (GenericValue clave : claves) {
			mapaClave.put(clave.getString("clavePresupuestal"), clave);
		}

		return mapaClave;
	}
   
    
    /**
     * Metodo para ampliar el presupuesto de un pedido. Desde el disponible hasta la recepcion
     * @param dctx DispatchContext
     * @param context Map
     * @return Map
     * @throws GenericEntityException 
     * @throws GenericServiceException 
     */
    public static Map ampliarOrden(LocalDispatcher dispatcher, Delegator delegator, Map context, Map claveMap, Map rowSubmits, Map montoAmpliar, 
    							  Timestamp fechaContable, String purchaseOrderId, Map productIds, GenericValue userLogin, Map orderItemSeqIds) throws GenericEntityException, GenericServiceException 
    {	GenericValue orderHeader = delegator.findByPrimaryKeyCache("OrderHeader", UtilMisc.toMap("orderId",purchaseOrderId));
    	Map<String, Object> montoMap = FastMap.newInstance();
     	Map<String, Object> clavePresupuestalMap = FastMap.newInstance();
     	Map<String, Object> mapaProducto = FastMap.newInstance();
     	Map<String, Object> input = FastMap.newInstance();	
		Map<String, Object> output = FastMap.newInstance();
		Timestamp fechaTrans = new Timestamp(Calendar.getInstance().getTimeInMillis());
		Boolean tieneAmpliaciones = false;				
		Debug.log("rowSubmits: " + rowSubmits);
		for(int i=0; i<claveMap.size(); i++)
		{	if(rowSubmits.get(Integer.toString(i)) != null)
			{	if(rowSubmits.get(Integer.toString(i)).toString().equals("Y")||rowSubmits.get(Integer.toString(i)).toString().equals("N"))
				{	BigDecimal montoAmpliarDecimal = new BigDecimal(montoAmpliar.get(Integer.toString(i)).toString());										
					montoMap.put(String.valueOf(i), montoAmpliar.get(Integer.toString(i)));
			     	clavePresupuestalMap.put(String.valueOf(i), claveMap.get(Integer.toString(i)));
			      	mapaProducto.put(String.valueOf(i), productIds.get(Integer.toString(i)));			      	
			      	
			      //Actualizar el monto restante de la orden de pago
			      	GenericValue OrderItem = delegator.findByPrimaryKey("OrderItem", UtilMisc.toMap("orderId", purchaseOrderId, "orderItemSeqId", orderItemSeqIds.get(Integer.toString(i))));
	            	if(OrderItem.getBigDecimal("montoRestante") != null)
	            	{	OrderItem.set("montoRestante", OrderItem.getBigDecimal("montoRestante").add(montoAmpliarDecimal));
	            		OrderItem.store();
	            	}
	            	else
	            	{	OrderItem.set("montoRestante", (OrderItem.getBigDecimal("quantity").multiply(OrderItem.getBigDecimal("unitPrice"))).add(montoAmpliarDecimal));
            			OrderItem.store();	            	
	            	}
			      	
			      	tieneAmpliaciones = true;
				}
			}
		}
		
		if(tieneAmpliaciones)
		{	context.put("montoMap", montoMap);
	      	context.put("clavePresupuestalMap", clavePresupuestalMap);
	      	context.put("mapaProducto", mapaProducto);
	      	
	      	Debug.log("montoMap: " + montoMap);
	      	Debug.log("clavePresupuestalMap: " + clavePresupuestalMap);
	      	Debug.log("mapaProducto: " + mapaProducto);
	      	     	
	      	input.put("eventoContableId", "PECO-AMP");
	  		input.put("tipoClaveId", "EGRESO");
	  		input.put("fechaRegistro", fechaTrans);
	  		input.put("fechaContable", fechaContable);
	  		input.put("currency", orderHeader.getString("currencyUom"));
	  		input.put("usuario", userLogin.getString("userLoginId"));
	  		input.put("organizationId", orderHeader.getString("billToPartyId"));
	  		input.put("descripcion", "Ampliaci\u00f3n del presupuesto en momento Comprometido para la orden : " + purchaseOrderId);
	  		input.put("roleTypeId", "BILL_FROM_VENDOR");
	  		input.put("campo", "orderId");
	  		input.put("valorCampo", purchaseOrderId);
	  		input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables("PECO-AMP", delegator, context, orderHeader.getString("billFromPartyId"), null, montoMap, mapaProducto)); 
	  	
	  		Debug.log("input: " + input);
	  		output = dispatcher.runSync("creaTransaccionMotor", input);  
	  		Debug.log("output: " + output);
	  		
	  			
	    	
	        return ServiceUtil.returnSuccess();
		}
		else
		{	return ServiceUtil.returnError("No se ha seleccionado algun item para ampliar el pedido o contrato");
		}		
    }
    
    /**
     * Metodo para reducir el presupuesto de un pedido. Desde el comprometido hasta el disponible
     * @param dctx DispatchContext
     * @param context Map
     * @return Map
     * @throws GenericEntityException 
     * @throws GenericServiceException 
     */
    public static Map reducirOrden(LocalDispatcher dispatcher, Delegator delegator, Map context, Map claveMap, Map rowSubmits, Map montoReducir, 
    							  Timestamp fechaFactura, String purchaseOrderId, Map productIds, GenericValue userLogin, Map orderItemSeqIds) throws GenericEntityException, GenericServiceException 
    {	GenericValue orderHeader = delegator.findByPrimaryKeyCache("OrderHeader", UtilMisc.toMap("orderId",purchaseOrderId));
    	Map<String, Object> montoMap = FastMap.newInstance();
     	Map<String, Object> clavePresupuestalMap = FastMap.newInstance();
     	Map<String, Object> mapaProducto = FastMap.newInstance();
     	Map<String, Object> input = FastMap.newInstance();	
		Map<String, Object> output = FastMap.newInstance();
		Timestamp fechaTrans = new Timestamp(Calendar.getInstance().getTimeInMillis());
		Boolean tieneReducciones = false;				
		Debug.log("rowSubmits: " + rowSubmits);
		for(int i=0; i<claveMap.size(); i++)
		{	if(rowSubmits.get(Integer.toString(i)) != null)
			{	if(rowSubmits.get(Integer.toString(i)).toString().equals("Y")||rowSubmits.get(Integer.toString(i)).toString().equals("N"))
				{	BigDecimal montoReducirDecimal = new BigDecimal(montoReducir.get(Integer.toString(i)).toString());										
					montoMap.put(String.valueOf(i), montoReducir.get(Integer.toString(i)));
			     	clavePresupuestalMap.put(String.valueOf(i), claveMap.get(Integer.toString(i)));
			      	mapaProducto.put(String.valueOf(i), productIds.get(Integer.toString(i)));			      	
			      	
			      //Actualizar el monto restante de la orden de pago
			      	GenericValue OrderItem = delegator.findByPrimaryKey("OrderItem", UtilMisc.toMap("orderId", purchaseOrderId, "orderItemSeqId", orderItemSeqIds.get(Integer.toString(i))));
	            	if(OrderItem.getBigDecimal("montoRestante") != null)
	            	{	OrderItem.set("montoRestante", OrderItem.getBigDecimal("montoRestante").subtract(montoReducirDecimal));
	            		OrderItem.store();
	            	}
			      	
	            	tieneReducciones = true;
				}
			}
		}
		
		if(tieneReducciones)
		{	context.put("montoMap", montoMap);
	      	context.put("clavePresupuestalMap", clavePresupuestalMap);
	      	context.put("mapaProducto", mapaProducto);
	      	
	      	Debug.log("montoMap: " + montoMap);
	      	Debug.log("clavePresupuestalMap: " + clavePresupuestalMap);
	      	Debug.log("mapaProducto: " + mapaProducto);
	      	     	
	      	input.put("eventoContableId", "PECO-REDU");
	  		input.put("tipoClaveId", "EGRESO");
	  		input.put("fechaRegistro", fechaTrans);
	  		input.put("fechaContable", fechaFactura);
	  		input.put("currency", orderHeader.getString("currencyUom"));
	  		input.put("usuario", userLogin.getString("userLoginId"));
	  		input.put("organizationId", orderHeader.getString("billToPartyId"));
	  		input.put("descripcion", "Reducci\u00f3n del presupuesto en momento Comprometido para la orden : " + purchaseOrderId);
	  		input.put("roleTypeId", "BILL_FROM_VENDOR");
	  		input.put("campo", "orderId");
	  		input.put("valorCampo", purchaseOrderId);
	  		input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables("PECO-REDU", delegator, context, orderHeader.getString("billFromPartyId"), null, montoMap, mapaProducto)); 
	  	
	  		Debug.log("input: " + input);
	  		output = dispatcher.runSync("creaTransaccionMotor", input);  
	  		Debug.log("output: " + output);
	  		
	  			
	    	
	        return ServiceUtil.returnSuccess();
		}
		else
		{	return ServiceUtil.returnError("No se ha seleccionado algun item para ampliar el pedido o contrato");
		}		
    }
    
    /**
     * Metodo para crear registros en control patrimonial de los bienes muebles que su codigo de producto 
     * inicia con 5, es decir que corresponde al capitulo 5000. Y que su valor unitario sea igual o mayor 
     * a 35 salarios minimos, es decir $2,453.50. Y que en la matriz de conversion del devengo A1 la cuenta 
     * de registro contable inicie con 1, es decir pertenezca al activo.
     * @param dctx DispatchContext
     * @param context Map
     * @return Map
     * @throws GenericEntityException 
     * @throws GenericServiceException 
     */
    public static void crearRecepcionActivoFijo(LocalDispatcher dispatcher, Delegator delegator, String shipmentId, String purchaseOrderId, String facilityId, String organizationPartyId, 
    		Timestamp fechaFactura, Map orderItemSeqIds, Map quantitiesAccepted, Map productIds, Map unitCosts, String numeroFactura, String userLoginId) throws GenericEntityException, GenericServiceException 
    {	
    	Boolean hizoRegistros = false;
    	GenericValue orderHeader = delegator.findByPrimaryKeyCache("OrderHeader", UtilMisc.toMap("orderId",purchaseOrderId));	        
		
		for(int i=0; i<quantitiesAccepted.size(); i++)
		{	
			if(quantitiesAccepted.get(Integer.toString(i)) != null)
			{	
				int cantidadRecibida = 0;
				cantidadRecibida = Integer.parseInt(quantitiesAccepted.get(Integer.toString(i)).toString());
				if(cantidadRecibida > 0)
				{	
					String productoARecibir = "";
					productoARecibir = productIds.get(Integer.toString(i)).toString();
					if(productoARecibir.substring(0, 1).equals("5"))
					{	
						BigDecimal precioUnitarioARecibir = BigDecimal.ZERO;
						precioUnitarioARecibir = new BigDecimal(unitCosts.get(Integer.toString(i)).toString());
//						if(precioUnitarioARecibir.compareTo(salarioMinimo.multiply(diasSalarioMinimo)) >= 0)
//						{	//Buscar matriz y bla bla bla si la cuenta contable empieza con 1	
							if(seRegistraActivo(delegator, productoARecibir.substring(0, 3)))
							{
								for(int j = 1; j<=cantidadRecibida; j++)
								{	//Crear el numero de cantidadReibida de veces el activo fijo
									GenericValue fixedAsset = GenericValue.create(delegator.getModelEntity("FixedAsset"));
									String fixedAssetId = "";			
									fixedAssetId = obtenIdentificadorActivoFijo(delegator, productoARecibir);														
									fixedAsset.set("fixedAssetId", fixedAssetId);
									fixedAsset.set("instanceOfProductId", productoARecibir);
									fixedAsset.set("fixedAssetTypeId", obtenerTipoActivoFijo(delegator, productoARecibir.substring(0, 3)));
									fixedAsset.set("fixedAssetName", obtenerNombreProducto(delegator, productoARecibir));
									fixedAsset.set("dateAcquired", fechaFactura);
									fixedAsset.set("purchaseCost", precioUnitarioARecibir);
									fixedAsset.set("moneda", orderHeader.getString("currencyUom"));
									fixedAsset.set("statusId", "ACT_FIJO_ACTIVO");
									fixedAsset.set("numeroFactura", numeroFactura);
									fixedAsset.set("proveedor", orderHeader.getString("billFromPartyId"));
									fixedAsset.set("acquireOrderId", purchaseOrderId);
									fixedAsset.set("acquireOrderItemSeqId", orderItemSeqIds.get(Integer.toString(i)).toString());
									fixedAsset.set("facilityId", facilityId);
									fixedAsset.set("denominacionPartidaGen", obtenerDenominacionPartidaGen(delegator, productoARecibir.substring(0, 3)));
									fixedAsset.set("shipmentId", shipmentId);								
									fixedAsset.create();
									hizoRegistros = true;
								}
							}
//						}
					}
				}
			}
		}
		if(hizoRegistros)
		{	//Crea registro en tabla de notificaciones al encargado de ese almacen de control aptrimonial
			GenericValue notificacionActivoFijo = GenericValue.create(delegator.getModelEntity("NotificacionActivoFijo"));																
			notificacionActivoFijo.set("orderId", purchaseOrderId);
			notificacionActivoFijo.set("facilityId", facilityId);
			notificacionActivoFijo.set("shipmentId", shipmentId);
			notificacionActivoFijo.set("statusId", "PENDIENTE_NOTIFI");
			notificacionActivoFijo.create();
		}
					
	}				
    
    /**
	 * Metodo para crear el nuevo identificador de un activo 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static String obtenIdentificadorActivoFijo(Delegator delegator, String cucop) throws GenericEntityException  	
	{	String idActivoFijo = "";
		long contadorCucop = 0;		
		
		EntityCondition condicion = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("instanceOfProductId", EntityOperator.EQUALS, cucop));
		contadorCucop = delegator.findCountByCondition("FixedAsset", condicion, null, null);
		
		idActivoFijo = cucop.concat(UtilFormatOut.formatPaddedNumber((contadorCucop + 1), 6));
		CUCOP.add(cucop);
		
		return idActivoFijo;
	}
	
	/**
	 * Metodo para obtener el tipo de activo fijo dado el cucop 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static String obtenerTipoActivoFijo(Delegator delegator, String substringCucop) throws GenericEntityException  	
	{	String fixedAssetTypeId = "";
		EntityCondition condition = EntityCondition.makeCondition(EntityOperator.AND, 
									EntityCondition.makeCondition("prefijoCucop", EntityOperator.EQUALS, substringCucop));			

		List<GenericValue> lista = delegator.findByCondition("FixedAssetType", condition, null, null);		
		if (!lista.isEmpty()) 
		{	fixedAssetTypeId = lista.get(0).getString("fixedAssetTypeId");			
		}
		return fixedAssetTypeId;
	}
	
	
	/**
	 * Metodo para obtener el nombre de un productId 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static String obtenerNombreProducto(Delegator delegator, String productId) throws GenericEntityException  	
	{	String productName = "";
		EntityCondition condition = EntityCondition.makeCondition(EntityOperator.AND, 
									EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId));			

		List<GenericValue> lista = delegator.findByCondition("Product", condition, null, null);		
		if (!lista.isEmpty()) 
		{	productName = lista.get(0).getString("productName");			
		}
		return productName;
	}
	
	/**
	 * Metodo para obtener el nombre de un productId 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static boolean seRegistraActivo(Delegator delegator, String cog) throws GenericEntityException  	
	{	String cuentaCargo = "";
		List<String> orderBy = UtilMisc.toList("tipoGasto");
		EntityCondition condition = EntityCondition.makeCondition(EntityOperator.AND, 
									EntityCondition.makeCondition("cog", EntityOperator.LIKE, cog+"%"),
									EntityCondition.makeCondition("matrizId", EntityOperator.EQUALS, "A.1"));

		List<GenericValue> lista = delegator.findByCondition("MatrizEgreso", condition, null, orderBy);
		if (!lista.isEmpty()) 
		{	
			cuentaCargo = lista.get(0).getString("cargo");			
			return cuentaCargo.substring(0,3).equals("1.2");
		} else {
			return false;
		}
	}
	
	/**
	 * Metodo para crear registro de devengo parcial por montos para imprimir la constancia de recepcion 
	 * @param dctx
	 * @param context	 
	 * @return
	 */
	public static String crearRecepcionDevengoMontos(LocalDispatcher dispatcher, String purchaseOrderId, String shipmentId, Map orderItemSeqIds, String numeroFactura, 
				  Timestamp fechaFactura, Timestamp fechaContable, Map productIds, String proveedorId, Map unitCosts, GenericValue userLogin, Map quantitiesAccepted, String invoiceId) 	
	{	try 
		{	Debug.log("Entra a crear RecepcionDevengoMontos");
			Delegator delegator = dispatcher.getDelegator();
			for(int i=0; i<unitCosts.size(); i++)
			{	if(quantitiesAccepted.get(Integer.toString(i)) != null && !quantitiesAccepted.get(Integer.toString(i)).equals(""))
				{	if(!quantitiesAccepted.get(Integer.toString(i)).equals("0"))
					{	GenericValue recepcionDevengoMontos = GenericValue.create(delegator.getModelEntity("RecepcionDevengoMontos"));																
						recepcionDevengoMontos.set("orderId", purchaseOrderId);
						recepcionDevengoMontos.set("orderItemSeqId", orderItemSeqIds.get(Integer.toString(i)));
						recepcionDevengoMontos.set("shipmentId", shipmentId);
						recepcionDevengoMontos.set("numeroFactura", numeroFactura);
						recepcionDevengoMontos.set("fechaFactura", fechaFactura);
						recepcionDevengoMontos.set("fechaContable", fechaContable);
						recepcionDevengoMontos.set("productId", productIds.get(Integer.toString(i)));
						recepcionDevengoMontos.set("cantidad", "1");
						recepcionDevengoMontos.set("precioUnitario", unitCosts.get(Integer.toString(i)));
						recepcionDevengoMontos.set("proveedor", proveedorId);
						recepcionDevengoMontos.set("userPartyId", userLogin.getString("partyId"));
						recepcionDevengoMontos.set("invoiceId", invoiceId);
						List<GenericValue> orderItem = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", purchaseOrderId, "orderItemSeqId", orderItemSeqIds.get(Integer.toString(i))));
						if(!orderItem.isEmpty()){
	        				Iterator<GenericValue> item = orderItem.iterator();
	            			GenericValue gen = item.next();
	            			if(gen.getString("requisicionId") != null && !gen.getString("requisicionId").equals(""))
	            			{	recepcionDevengoMontos.set("requisicionId", gen.getString("requisicionId"));
	            				recepcionDevengoMontos.set("detalleRequisicionId", gen.getString("detalleRequisicionId"));
	            			}
	        			} 						
						Debug.log("recepcionDevengoMontos: "+ recepcionDevengoMontos);
						delegator.create(recepcionDevengoMontos);																	
					}
				}				
			}
			return null;
		}			
		catch (GenericEntityException e) {
			Debug.logError("Hubo error al crear el Invoice Item", MODULE);
			return "error";
		}
	}
	
	/**
	 * Metodo para obtener la denominacion de partida generica 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static String obtenerDenominacionPartidaGen(Delegator delegator, String cog) throws GenericEntityException  	
	{	String denominacion = "";
		List<String> orderBy = UtilMisc.toList("tipoGasto");
		EntityCondition condition = EntityCondition.makeCondition(EntityOperator.AND, 
									EntityCondition.makeCondition("cog", EntityOperator.LIKE, cog+"%"),
									EntityCondition.makeCondition("matrizId", EntityOperator.EQUALS, "A.1"));
		Debug.log("condition: " + condition);

		List<GenericValue> lista = delegator.findByCondition("MatrizEgreso", condition, null, orderBy);
		Debug.log("lista: " + lista);
		if (!lista.isEmpty()) 
		{	denominacion = lista.get(0).getString("nombreCog");			
		}
		Debug.log("denominacion: " + denominacion);		
		return denominacion;
	}
	
	/**
	 *Prepara y envia la informacion para e reporte de altas en almacen
	 * @param request
	 * @param response
	 * @return
	 */
	public static String preparaReporteAltaInventario(HttpServletRequest request, HttpServletResponse response){
		
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String acctgTransId = UtilCommon.getParameter(request, "acctgTransId");
        Map<String, Object> jrParameters = FastMap.newInstance();
        List<Map<String, Object>> listProductoSurtir = FastList.newInstance();
        
        try {	
        	Debug.log("acctgTransId: " + acctgTransId);
			List<GenericValue> PedidoInternoList = delegator.findByAnd("PhysicalInventory", UtilMisc.toMap("acctgTransId",acctgTransId));
			Debug.log("PedidoInternoList: " + PedidoInternoList);
			GenericValue PhysicalInventory = PedidoInternoList.get(0);
			jrParameters.putAll(PhysicalInventory.getAllFields());
			
			GenericValue AcctgTrans = delegator.findByPrimaryKey("AcctgTrans", UtilMisc.toMap("acctgTransId",acctgTransId));
			GenericValue EventoContable = delegator.findByPrimaryKey("EventoContable", UtilMisc.toMap("acctgTransTypeId",AcctgTrans.getString("acctgTransTypeId")));
			jrParameters.put("noPoliza", AcctgTrans.getString("poliza"));
			jrParameters.put("postedDate", new java.util.Date(AcctgTrans.getTimestamp("postedDate").getTime()));
			jrParameters.put("descripcionEvento", EventoContable.getString("descripcion"));
			
			Debug.log("jrParameters1: " + jrParameters);
			
			String solicitante = "";			
			if(PhysicalInventory.getString("partyId") != null && !PhysicalInventory.getString("partyId").equals(""))
			{	GenericValue person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", PhysicalInventory.getString("partyId")));
				solicitante = person.getString("firstName") + " " + person.getString("lastName");
			}
			jrParameters.put("solicitante", solicitante);
			
			String organizationPartyId = AcctgTrans.getString("organizationPartyId");																	
			
        	GenericValue Organization = delegator.findByPrimaryKey("PartyGroup", UtilMisc.toMap("partyId",organizationPartyId));
        	jrParameters.put("organizacion", Organization.getString("groupNameLocal").toUpperCase());
        	jrParameters.put("logoUrl", Organization.getString("logoImageUrl"));
        	jrParameters.put("logoUrl2", Organization.getString("logoImageUrl2"));
			
        	Debug.log("jrParameters2: " + jrParameters);
        	
			List<GenericValue> listConsultaAltaBajaAlmacen = delegator.findByAnd("ConsultaAltaBajaAlmacen",UtilMisc.toMap("acctgTransId",acctgTransId));
			String almacenId = "";
			String facilityName = "";
			Map<String, Object> mapConsultaAltaBajaAlmacen =  FastMap.newInstance();
			BigDecimal cantidad = BigDecimal.ZERO;
			for (GenericValue consultaAltaBajaAlmacen : listConsultaAltaBajaAlmacen) {
				mapConsultaAltaBajaAlmacen = consultaAltaBajaAlmacen.getAllFields();
				Debug.log("mapConsultaAltaBajaAlmacen: " + mapConsultaAltaBajaAlmacen);
				cantidad = (BigDecimal) mapConsultaAltaBajaAlmacen.get("cantidad");
				//cantidad = cantidad.negate();
				jrParameters.put("descripcionRazon", mapConsultaAltaBajaAlmacen.get("descripcionRazon"));
				if(almacenId.equals(""))				
				{	GenericValue facility = delegator.findByPrimaryKey("Facility", UtilMisc.toMap("facilityId", mapConsultaAltaBajaAlmacen.get("facilityId")));
					almacenId = facility.getString("facilityId");
					facilityName = facility.getString("facilityName");
					jrParameters.put("almacenId", almacenId);
					jrParameters.put("facilityName", facilityName);										
				}								
				mapConsultaAltaBajaAlmacen.put("cantidad", cantidad);
				mapConsultaAltaBajaAlmacen.put("total", consultaAltaBajaAlmacen.getBigDecimal("monto").multiply(cantidad));
				listProductoSurtir.add(mapConsultaAltaBajaAlmacen);
			}
			
			Debug.log("jrParameters: " + jrParameters);			
			
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        
        JRMapCollectionDataSource jrDataSource = new JRMapCollectionDataSource(listProductoSurtir);
        request.setAttribute("jrDataSource", jrDataSource);
        request.setAttribute("jrParameters", jrParameters);
        
		
		return "success";
	}
	
	/**
	 *Prepara y envia la informacion para e reporte de altas en almacen
	 * @param request
	 * @param response
	 * @return
	 */
	public static String preparaReporteBajaInventario(HttpServletRequest request, HttpServletResponse response){
		
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String acctgTransId = UtilCommon.getParameter(request, "acctgTransId");
        Map<String, Object> jrParameters = FastMap.newInstance();
        List<Map<String, Object>> listProductoSurtir = FastList.newInstance();
        
        try {	
        	Debug.log("acctgTransId: " + acctgTransId);
			List<GenericValue> PedidoInternoList = delegator.findByAnd("PhysicalInventory", UtilMisc.toMap("acctgTransId",acctgTransId));
			Debug.log("PedidoInternoList: " + PedidoInternoList);
			GenericValue PhysicalInventory = PedidoInternoList.get(0);
			jrParameters.putAll(PhysicalInventory.getAllFields());
			
			GenericValue AcctgTrans = delegator.findByPrimaryKey("AcctgTrans", UtilMisc.toMap("acctgTransId",acctgTransId));
			GenericValue EventoContable = delegator.findByPrimaryKey("EventoContable", UtilMisc.toMap("acctgTransTypeId",AcctgTrans.getString("acctgTransTypeId")));
			jrParameters.put("noPoliza", AcctgTrans.getString("poliza"));
			jrParameters.put("postedDate", new java.util.Date(AcctgTrans.getTimestamp("postedDate").getTime()));
			jrParameters.put("descripcionEvento", EventoContable.getString("descripcion"));
			
			Debug.log("jrParameters1: " + jrParameters);
			
			String solicitante = "";			
			if(PhysicalInventory.getString("partyId") != null && !PhysicalInventory.getString("partyId").equals(""))
			{	GenericValue person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", PhysicalInventory.getString("partyId")));
				solicitante = person.getString("firstName") + " " + person.getString("lastName");
			}
			jrParameters.put("solicitante", solicitante);
			
			String organizationPartyId = AcctgTrans.getString("organizationPartyId");																	
			
        	GenericValue Organization = delegator.findByPrimaryKey("PartyGroup", UtilMisc.toMap("partyId",organizationPartyId));
        	jrParameters.put("organizacion", Organization.getString("groupNameLocal").toUpperCase());
        	jrParameters.put("logoUrl", Organization.getString("logoImageUrl"));
        	jrParameters.put("logoUrl2", Organization.getString("logoImageUrl2"));
			
        	Debug.log("jrParameters2: " + jrParameters);
        	
			List<GenericValue> listConsultaAltaBajaAlmacen = delegator.findByAnd("ConsultaAltaBajaAlmacen",UtilMisc.toMap("acctgTransId",acctgTransId));
			String almacenId = "";
			String facilityName = "";
			Map<String, Object> mapConsultaAltaBajaAlmacen =  FastMap.newInstance();
			BigDecimal cantidad = BigDecimal.ZERO;
			for (GenericValue consultaAltaBajaAlmacen : listConsultaAltaBajaAlmacen) {
				mapConsultaAltaBajaAlmacen = consultaAltaBajaAlmacen.getAllFields();
				Debug.log("mapConsultaAltaBajaAlmacen: " + mapConsultaAltaBajaAlmacen);
				cantidad = consultaAltaBajaAlmacen.getBigDecimal("cantidad").abs();
				//cantidad = cantidad.negate();
				jrParameters.put("descripcionRazon", mapConsultaAltaBajaAlmacen.get("descripcionRazon"));
				if(almacenId.equals(""))				
				{	GenericValue facility = delegator.findByPrimaryKey("Facility", UtilMisc.toMap("facilityId", mapConsultaAltaBajaAlmacen.get("facilityId")));
					almacenId = facility.getString("facilityId");
					facilityName = facility.getString("facilityName");
					jrParameters.put("almacenId", almacenId);
					jrParameters.put("facilityName", facilityName);										
				}								
				mapConsultaAltaBajaAlmacen.put("cantidad", cantidad);
				mapConsultaAltaBajaAlmacen.put("total", consultaAltaBajaAlmacen.getBigDecimal("monto").multiply(cantidad));
				listProductoSurtir.add(mapConsultaAltaBajaAlmacen);
			}
			
			Debug.log("jrParameters: " + jrParameters);			
			
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        
        JRMapCollectionDataSource jrDataSource = new JRMapCollectionDataSource(listProductoSurtir);
        request.setAttribute("jrDataSource", jrDataSource);
        request.setAttribute("jrParameters", jrParameters);
        
		
		return "success";
	}
	
	public static void consultaArticulosPorAlmacen(Map<String, Object> context) throws GeneralException, ParseException 
	{	final ActionContext ac = new ActionContext(context);
		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();
		String facilityId = ac.getParameter("facilityId");		
		String inventoryItemId = ac.getParameter("inventoryItemId");				
		String productId = ac.getParameter("productId");		
		GenericValue userLogin = (GenericValue) context.get("userLogin");
				
		if ("Y".equals(ac.getParameter("performFind"))) 
		{	List<EntityCondition> searchConditions = new FastList<EntityCondition>();			
			if (UtilValidate.isNotEmpty(facilityId)) {
				searchConditions.add(EntityCondition.makeCondition("facilityId", EntityOperator.EQUALS, facilityId));
			}
			if (UtilValidate.isNotEmpty(productId)) {
				searchConditions.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId));
			}
			if (UtilValidate.isNotEmpty(inventoryItemId)) {
				searchConditions.add(EntityCondition.makeCondition("inventoryItemId", EntityOperator.EQUALS, inventoryItemId));
			}
			searchConditions.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, userLogin.getString("partyId")));
			searchConditions.add(EntityCondition.makeCondition("securityGroupId", EntityOperator.EQUALS, "WRHS_MANAGER"));	
			searchConditions.add(EntityCondition.makeCondition("quantityOnHandTotal", EntityOperator.GREATER_THAN, "0"));
						
			Debug.log("searchConditions: " + searchConditions);
	        List<ConsultaArticulosEnAlmacenesPorPermisoUsuario> itemsInventoryItem = ledgerRepository.findList(ConsultaArticulosEnAlmacenesPorPermisoUsuario.class, searchConditions);	        
	        List<Map<String, Object>> listInventoryItem = new FastList<Map<String, Object>>();
	        for (ConsultaArticulosEnAlmacenesPorPermisoUsuario s : itemsInventoryItem) {
	            Map<String, Object> map = s.toMap();
	            String nombreUsuraio = "";				
								
				if(map.get("firstName") != null && !map.get("firstName").equals(""))
				{	nombreUsuraio = nombreUsuraio + map.get("firstName") + " ";							
				}
				if(map.get("middleName") != null && !map.get("middleName").equals(""))
				{	nombreUsuraio = nombreUsuraio + map.get("middleName") + " ";
				}						
				if(map.get("lastName") != null && !map.get("lastName").equals(""))
				{	nombreUsuraio = nombreUsuraio + map.get("lastName") + " ";
				}
				map.put("nombreUsuraio", nombreUsuraio);				
	            listInventoryItem.add(map);
	        }
	        ac.put("listInventoryItem", listInventoryItem);	        	     	        
		}
	}
		
	/**
	 * Metodo que crea transferencias de articulos entre almacenes 
	 * @param dctx
	 * @param context	 
	 * @return
	 */
	public static Map transferirArticulosAlmacen(DispatchContext dctx, Map context) 
	{	
		Delegator delegator = dctx.getDelegator();
		Debug.log("context: " + context);
    	String facilityDestinoId = (String) context.get("facilityDestinoId");
    	Map inventoryItemIdMap = (Map) context.get("inventoryItemIdMap");
    	Map unitCostMap = (Map) context.get("unitCostMap");
    	Map quantityOnHandTotalMap = (Map) context.get("quantityOnHandTotalMap");
    	Map cantidadTransferirMap = (Map) context.get("cantidadTransferirMap");
    	Map excepcionMap = (Map) context.get("excepcionMap");
    	
    	if(facilityDestinoId == null || facilityDestinoId.equals(""))
    	{	return ServiceUtil.returnError("Es necesario ingresar el almac\u00e9n destino");    		
    	}
    	
		try 
		{	//Recorrer el de excepcion
			for(int i=0; i<inventoryItemIdMap.size(); i++)
			{	//Si excepcion tiene Y, entrar
				if(excepcionMap.get(Integer.toString(i)) != null && excepcionMap.get(Integer.toString(i)).equals("Y"))
				{	if(cantidadTransferirMap.get(Integer.toString(i)) != null && !cantidadTransferirMap.get(Integer.toString(i)).equals(""))
					{	//Si cantidad cantidad en mano es mayor a cantidad a recibir
						GenericValue inventoryItem = delegator.findByPrimaryKey("InventoryItem", UtilMisc.toMap("inventoryItemId", inventoryItemIdMap.get(Integer.toString(i)).toString()));
						if(new BigDecimal(quantityOnHandTotalMap.get(Integer.toString(i)).toString()).compareTo(new BigDecimal(cantidadTransferirMap.get(Integer.toString(i)).toString())) > 0)
						{	//crear Nuevo INVENTORY_ITEM
							GenericValue inventoryItemNuevo = inventoryItem;
							String inventoryItemId = delegator.getNextSeqId("InventoryItem");
							inventoryItemNuevo.set("inventoryItemId", inventoryItemId);
							inventoryItemNuevo.set("facilityId", facilityDestinoId);
							inventoryItemNuevo.set("quantityOnHandTotal", new BigDecimal(cantidadTransferirMap.get(Integer.toString(i)).toString()));
							inventoryItemNuevo.set("availableToPromiseTotal", new BigDecimal(cantidadTransferirMap.get(Integer.toString(i)).toString()));
							inventoryItemNuevo.set("datetimeReceived", UtilDateTime.nowTimestamp());
							Debug.log("inventoryItemNuevo: " + inventoryItemNuevo);
							inventoryItemNuevo.create();							
							//crear un nuevo registro de actualizacion en anterior INVENTORY_ITEM_DETAIL
							GenericValue inventoryItemDetailActualizar = GenericValue.create(delegator.getModelEntity("InventoryItemDetail"));							
							inventoryItemDetailActualizar.set("inventoryItemId", inventoryItemIdMap.get(Integer.toString(i)).toString());
							inventoryItemDetailActualizar.set("inventoryItemDetailSeqId", delegator.getNextSeqId("InventoryItemDetail"));
							inventoryItemDetailActualizar.set("effectiveDate", UtilDateTime.nowTimestamp());
							inventoryItemDetailActualizar.set("quantityOnHandDiff", new BigDecimal(cantidadTransferirMap.get(Integer.toString(i)).toString()).negate());
							inventoryItemDetailActualizar.set("availableToPromiseDiff", new BigDecimal(cantidadTransferirMap.get(Integer.toString(i)).toString()).negate());
							inventoryItemDetailActualizar.set("accountingQuantityDiff", BigDecimal.ZERO);
							inventoryItemDetailActualizar.set("unitCost", new BigDecimal(unitCostMap.get(Integer.toString(i)).toString()));
							inventoryItemDetailActualizar.set("reasonEnumId", "VAR_TRANSFER");												
							Debug.log("inventoryItemDetailActualizar3: "+ inventoryItemDetailActualizar);
							delegator.create(inventoryItemDetailActualizar);
							//Actualizar cantidad en anterior INVENTORY_ITEM
							GenericValue inventoryItemActualiza = delegator.findByPrimaryKey("InventoryItem", UtilMisc.toMap("inventoryItemId", inventoryItemIdMap.get(Integer.toString(i)).toString()));
							Debug.log("inventoryItemPrimero: "+ inventoryItemActualiza);							
							if(inventoryItemActualiza.getBigDecimal("quantityOnHandTotal").compareTo(inventoryItemActualiza.getBigDecimal("availableToPromiseTotal")) == 0)
							{	inventoryItemActualiza.set("quantityOnHandTotal", new BigDecimal(quantityOnHandTotalMap.get(Integer.toString(i)).toString()).subtract(new BigDecimal(cantidadTransferirMap.get(Integer.toString(i)).toString())));
								inventoryItemActualiza.set("availableToPromiseTotal", new BigDecimal(quantityOnHandTotalMap.get(Integer.toString(i)).toString()).subtract(new BigDecimal(cantidadTransferirMap.get(Integer.toString(i)).toString())));	
							}
							else
							{	inventoryItemActualiza.set("quantityOnHandTotal", new BigDecimal(quantityOnHandTotalMap.get(Integer.toString(i)).toString()).subtract(new BigDecimal(cantidadTransferirMap.get(Integer.toString(i)).toString())));								
							}													
							Debug.log("inventoryItemActualiza2: "+ inventoryItemActualiza);
							delegator.store(inventoryItemActualiza);					
							//PHYSICAL_INVENTORY y PHYSICAL_INVENTORY_HISTORY no se actualizan o se crean nuevos porque no se genera poliza
						}
						//Si cantidad cantidad en mano es igual a cantidad a recibir
						else if(new BigDecimal(quantityOnHandTotalMap.get(Integer.toString(i)).toString()).compareTo(new BigDecimal(cantidadTransferirMap.get(Integer.toString(i)).toString())) == 0)
						{	//Cambiar solo el facilityId de todo el registro INVENTORY_ITEM
							inventoryItem.set("facilityId", facilityDestinoId);							
							Debug.log("inventoryItem4: "+ inventoryItem);
							delegator.store(inventoryItem);
						}
						//Si cantidad cantidad en mano es menor a cantidad a recibir, envia error					
						else
						{	return ServiceUtil.returnError("La cantidad a transferir en el C\u00f3digo de art\u00edculo de inventario [ "+inventoryItemIdMap.get(Integer.toString(i))+" ] es menor a la cantidad disponible.");
						}
					}
					else
					{	return ServiceUtil.returnError("La cantidad a transferir en el C\u00f3digo de art\u00edculo de inventario [ "+inventoryItemIdMap.get(Integer.toString(i))+" ] no ha sido ingresada.");						
					}
				}			
			}
			Map result = ServiceUtil.returnSuccess("Se ha creado la transferencia con \u00e9xito");
			return result;
		}
		catch (GenericEntityException e) {
			Debug.logError("Error al crear la transferencia" + e, MODULE);
			return ServiceUtil.returnError("Error al crear la transferencia enre almacenes");
		}		
	}
	
	
	/**
	 * Metodo para consultar los articulos de un pedido o contrato para devolucion 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static void consultarOrdenDevolucion(Map<String, Object> context)
			throws GeneralException, ParseException {
		

		final ActionContext ac = new ActionContext(context);
		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();
		String orderId = ac.getParameter("orderId");					
		
		if(orderId== null || orderId.equals(""))
		{	return;
		}
		
		
		if ("Y".equals(ac.getParameter("performFind"))) {
	        //Consulta items
	        List<EntityCondition> searchConditionsItems = new FastList<EntityCondition>();

			if (UtilValidate.isNotEmpty(orderId)) {
				searchConditionsItems.add(EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId));
			}						
			searchConditionsItems.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ITEM_COMPLETED"));
			
			Debug.logInfo("searchConditionsItemsOrderId: " + searchConditionsItems,MODULE);			
	        List<DevolucionRecepcionItems> devolucionRecepcionItems = ledgerRepository.findList(DevolucionRecepcionItems.class, searchConditionsItems, null, null);	        
	        List<Map<String, Object>> listDevolucionRecepcionItems = new FastList<Map<String, Object>>();
	        for (DevolucionRecepcionItems s : devolucionRecepcionItems) {
	            Map<String, Object> map = s.toMap();	            
				listDevolucionRecepcionItems.add(map);
	        }
	        ac.put("listDevolucionRecepcionItems", listDevolucionRecepcionItems);	   
		}
	}
	
	/**
	 * Metodo para hacer una devolucion de ona orden de compra y liberar presupuesto 
	 * dependiendo si esta devengado, ejercido o pagado.
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 * @throws GenericServiceException 
	 * @throws ParseException 
	 */
	public static Map<String, Object> crearDevolucion(DispatchContext dctx, Map<?, ?> context) throws GenericEntityException, GenericServiceException, ParseException 
	{	
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Delegator delegator = dctx.getDelegator();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		Map<String, Object> montoMapDeve = FastMap.newInstance();
     	Map<String, Object> clavePresupuestalMapDeve = FastMap.newInstance();
     	Map<String, Object> mapaProductoDeve = FastMap.newInstance();
     	Map<String, Object> montoMapEjer = FastMap.newInstance();
     	Map<String, Object> clavePresupuestalMapEjer = FastMap.newInstance();
     	Map<String, Object> mapaProductoEjer = FastMap.newInstance();
     	Map<String, Object> orderItemSeqIdDevolEjer = FastMap.newInstance();
     	Map<String, Object> orderItemSeqIdDevolDeve = FastMap.newInstance();
     	Map<String, Object> cantidadDevolverDeve = FastMap.newInstance();
     	Map<String, Object> unitPriceDeve = FastMap.newInstance();
     	Map<String, Object> cantidadDevolverEjer = FastMap.newInstance();
     	Map<String, Object> unitPriceEjer = FastMap.newInstance();		
		Map orderIdMap = (Map) context.get("orderIdMap");
		Map orderItemSeqIdMap = (Map) context.get("orderItemSeqIdMap");
		Map productIdMap = (Map) context.get("productIdMap");
		Map quantityAcceptedMap = (Map) context.get("quantityAcceptedMap");
		Map unitPriceMap = (Map) context.get("unitPriceMap");
		Map cantidadDevolverMap = (Map) context.get("cantidadDevolverMap");				
		Map excepcionMap = (Map) context.get("excepcionMap");
		Map shipmentReceiptIdMap = (Map) context.get("shipmentReceiptIdMap");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		String organizationPartyId = (String) context.get("organizationPartyId");
		String moneda = (String) context.get("moneda");
		boolean creaOrdenCobro = false;
		boolean creaDevolEjercido = false;
		boolean creaDevolDevengado = false;
		//Datos de orden de cobro
		Map productIdsDevol = FastMap.newInstance();
		Map montoUnitarioDevol = FastMap.newInstance();
		Map claveMapDevol = FastMap.newInstance();
		Map cantidadDevol = FastMap.newInstance();
		Map itemSeqIdDevol = FastMap.newInstance();				
		String purchaseOrderId = "";
		String partyIdFrom = "";
		String partyIdTo = "";
		BigDecimal montoTotalOrdenCobro = BigDecimal.ZERO;
		Debug.logInfo("Context: " + context,MODULE);		
		
		try 
		{	int contador = 0;
			for(int i=0; i<orderItemSeqIdMap.size(); i++)		
			{	
				if(excepcionMap.get(Integer.toString(i+1)) != null && excepcionMap.get(Integer.toString(i+1)).equals("Y"))
				{
					//Consulta la tabla ORDERITEMBILLING para obtener el Invoice (Orden de pago)
				 	purchaseOrderId =  orderIdMap.get(Integer.toString(i+1)).toString();
					List<GenericValue> listaOrderItemBilling = delegator.findByAnd("OrderItemBilling", UtilMisc.toMap("orderId", purchaseOrderId, "shipmentReceiptId", shipmentReceiptIdMap.get(Integer.toString(i+1))));
					String invoiceId = "";
					String invoiceItemSeqId = "";
	        		for (GenericValue listaBilling : listaOrderItemBilling) 
	        		{
	        			invoiceId = listaBilling.getString("invoiceId");
	        			invoiceItemSeqId = listaBilling.getString("invoiceItemSeqId");	        					
	        		}
	        		if(invoiceId.equals("") || invoiceId == null || invoiceItemSeqId.equals("") || invoiceItemSeqId == null)
	        		{
	        			return ServiceUtil.returnError("Error al devolver el elemento " + orderItemSeqIdMap.get(Integer.toString(i+1)) + " del pedido o contrato " + purchaseOrderId);	        			
	        		}
	        		
	        		BigDecimal cantidadAceptada = new BigDecimal(quantityAcceptedMap.get(Integer.toString(i+1)).toString());
	        		BigDecimal cantidadDevolverDecimal = new BigDecimal(cantidadDevolverMap.get(Integer.toString(i+1)).toString());
	        		if(cantidadDevolverDecimal.compareTo(cantidadAceptada) > 0)
	        		{	
	        			return ServiceUtil.returnError("La cantidad a devolver es mayor a la cantidad recibida en el elemento " + orderItemSeqIdMap.get(Integer.toString(i+1)) + " del pedido o contrato " + purchaseOrderId);	        			
	        		}
	        		
	        		//Consulta la tabla OrdenPago y OrdenPagoMulti para ver si ya ha sido ejercido y esta pendiente de pago o ya ha sido pagado	        		
	        		List<GenericValue> listaOrdenPagoMulti = delegator.findByAnd("OrdenPagoMulti", UtilMisc.toMap("invoiceId", invoiceId, "invoiceItemSeqId", invoiceItemSeqId, "idCatalogoPres", "PRESUPUESTO"));
	        		if(listaOrdenPagoMulti.isEmpty())
	        		{	
	        			creaDevolDevengado = true;
	        			//No existe la orden de pago. No ha sido ejercido
		        		//Modificar el InvoiceItem e Invoice	
		        		List<GenericValue> listaInvoiceItem = delegator.findByAnd("InvoiceItem", UtilMisc.toMap("invoiceId", invoiceId, "invoiceItemSeqId", invoiceItemSeqId));
		        		if(!listaInvoiceItem.isEmpty())
		        		{	
		        			Iterator<GenericValue> item = listaInvoiceItem.iterator();
	            			GenericValue invoiceItem = item.next();
	            			BigDecimal montoDevolver = BigDecimal.ZERO;
	            			//Actualizar la cantidad original del registro original del producto en InvoiceItem
	            			
	            			if(cantidadDevolverDecimal.compareTo(invoiceItem.getBigDecimal("quantity")) > 0)	            				
	            			{	
	            				return ServiceUtil.returnError("La cantidad a devolver del producto " + productIdMap.get(Integer.toString(i+1)) + " es mayor a la cantidad que se encuentra en el pedido o contrato");		            				
	            			}	            			
	            			invoiceItem.set("quantity", invoiceItem.getBigDecimal("quantity").subtract(cantidadDevolverDecimal));
	            			invoiceItem.set("montoRestante", invoiceItem.getBigDecimal("amount").multiply(invoiceItem.getBigDecimal("quantity")));
	            			montoDevolver = cantidadDevolverDecimal.multiply(invoiceItem.getBigDecimal("amount"));
	            			delegator.store(invoiceItem);
	            			
	            			//Crear un registro igualito que Actualizar la cantidad original del registro original del producto en InvoiceItem
	            			String invoiceItemSeqIdNuevo = invoiceItem.getString("invoiceItemSeqId") + "-D";
	            			invoiceItem.set("invoiceItemSeqId", invoiceItemSeqIdNuevo);
	            			invoiceItem.set("description", invoiceItem.getString("description") + " - Devoluci\u00f3n");
	            			invoiceItem.set("quantity", cantidadDevolverMap.get(Integer.toString(i+1)));
	            			delegator.create(invoiceItem);
	            			
	            			//Modificar la entidad Invoice en montos
	            			List<GenericValue> listaInvoice = delegator.findByAnd("Invoice", UtilMisc.toMap("invoiceId", invoiceId));
	            			if(!listaInvoice.isEmpty())
			        		{
	            				Iterator<GenericValue> itemInvoice = listaInvoice.iterator();
		            			GenericValue invoice = itemInvoice.next();
		            			invoice.set("invoiceAdjustedTotal", invoice.getBigDecimal("invoiceAdjustedTotal").subtract(cantidadDevolverDecimal));
		            			invoice.set("invoiceTotal", invoice.getBigDecimal("invoiceTotal").subtract(cantidadDevolverDecimal));
		            			invoice.set("openAmount", invoice.getBigDecimal("openAmount").subtract(cantidadDevolverDecimal));
		            			invoice.set("pendingOpenAmount", invoice.getBigDecimal("pendingOpenAmount").subtract(cantidadDevolverDecimal));		            			
		            			delegator.store(invoice);
		            		
			        		}	            				            			
	            			//Se ingresan los datos a los mapas de MotorContable	            			
	            	     	montoMapDeve.put(String.valueOf(contador), montoDevolver.toString());
	            	     	String clavePresupuestal = obtieneClavePresupuestal(dctx, context, delegator, invoiceId, invoiceItemSeqId, "EGRESO");
	    			     	clavePresupuestalMapDeve.put(String.valueOf(contador), clavePresupuestal);
	    			      	mapaProductoDeve.put(String.valueOf(contador), productIdMap.get(Integer.toString(i+1)));
	    			      	orderItemSeqIdDevolDeve.put(String.valueOf(contador), orderItemSeqIdMap.get(Integer.toString(i+1)));
	    			      	cantidadDevolverDeve.put(String.valueOf(contador), cantidadDevolverMap.get(Integer.toString(i+1)));
	    			      	unitPriceDeve.put(String.valueOf(contador), unitPriceMap.get(Integer.toString(i+1)));
	    			      	contador++;
	        			}	        				        				        		
	        		}
	        		else
	        		{	
	        			for (GenericValue listaOrdenPago : listaOrdenPagoMulti) 
	        			{	
	        				if(listaOrdenPago.getBigDecimal("montoRestante").compareTo(BigDecimal.ZERO) > 0)
	        				{	//No se ha pagado esa lineade la orden de pago multi. No ha sido pagado
	        					creaDevolEjercido = true;
	        					Iterator<GenericValue> itemOrdenPagoMulti = listaOrdenPagoMulti.iterator();
	            				GenericValue ordenPagoMulti = itemOrdenPagoMulti.next();
			        			//Modificar el OrdenPagoMulti		        				
		            			//Actualizar la cantidad original del registro original del producto en InvoiceItem
		            			BigDecimal precioUnitarioDevolverDecimal = new BigDecimal(unitPriceMap.get(Integer.toString(i+1)).toString());
		            			BigDecimal montoDevolver = cantidadDevolverDecimal.multiply(precioUnitarioDevolverDecimal);
		            			if(montoDevolver.compareTo(ordenPagoMulti.getBigDecimal("montoRestante")) > 0)	            				
		            			{	
		            				return ServiceUtil.returnError("El monto a devolver es mayor al que se encuentra en la orden de pago " + ordenPagoMulti.getString("ordenPagoId"));		            				
		            			}	            			
		            			ordenPagoMulti.set("montoRestante", ordenPagoMulti.getBigDecimal("montoRestante").subtract(montoDevolver));
		            			ordenPagoMulti.set("monto", ordenPagoMulti.getBigDecimal("monto").subtract(montoDevolver));
		            			delegator.store(ordenPagoMulti);
				        		//Se libera desde Ejercido - Disponible
	            				            				            			
		            			//Se ingresan los datos a los mapas de MotorContable	            			
		            	     	montoMapEjer.put(String.valueOf(contador), montoDevolver.toString());
		            	     	String clavePresupuestal = obtieneClavePresupuestal(dctx, context, delegator, invoiceId, invoiceItemSeqId, "EGRESO");
		    			     	clavePresupuestalMapEjer.put(String.valueOf(contador), clavePresupuestal);
		    			     	orderItemSeqIdDevolEjer.put(String.valueOf(contador), orderItemSeqIdMap.get(Integer.toString(i+1)));
		    			      	cantidadDevolverEjer.put(String.valueOf(contador), cantidadDevolverMap.get(Integer.toString(i+1)));
		    			      	unitPriceEjer.put(String.valueOf(contador), unitPriceMap.get(Integer.toString(i+1)));
		    			      	mapaProductoEjer.put(String.valueOf(contador), productIdMap.get(Integer.toString(i+1)));
		    			      	contador++;
	        				}
	        				else
	        				{	
	        					creaOrdenCobro = true;	   
	        					productIdsDevol.put(String.valueOf(contador), productIdMap.get(Integer.toString(i+1)).toString());
	        					cantidadDevol.put(String.valueOf(contador), cantidadDevolverMap.get(Integer.toString(i+1)).toString());
	        					montoUnitarioDevol.put(String.valueOf(contador), unitPriceMap.get(Integer.toString(i+1)).toString());
	        					itemSeqIdDevol.put(String.valueOf(contador), orderItemSeqIdMap.get(Integer.toString(i+1)).toString());
	        					EntityCondition condicionIngresos = EntityCondition.makeCondition(EntityOperator.AND, 
	        							EntityCondition.makeCondition("organizationPartyId",EntityOperator.EQUALS, organizationPartyId),
	        							EntityCondition.makeCondition("tipo",EntityOperator.EQUALS, "INGRESO"),
	        							EntityCondition.makeCondition("flag",EntityOperator.EQUALS, "Y"));
	        					List<GenericValue> claveIngresos = delegator.findByCondition("ClavePenaDeductiva", condicionIngresos, null, null);	        					
	        					claveMapDevol.put(String.valueOf(contador), claveIngresos);
	        					contador++;
	        					BigDecimal cant = new BigDecimal(cantidadDevolverMap.get(Integer.toString(i+1)).toString());
	        					BigDecimal mont = new BigDecimal(unitPriceMap.get(Integer.toString(i+1)).toString());
	        					montoTotalOrdenCobro = montoTotalOrdenCobro.add(cant.multiply(mont));
	        					List<GenericValue> listaOrderHeader = delegator.findByAnd("OrderHeader", UtilMisc.toMap("orderId", purchaseOrderId));
	        					partyIdFrom = listaOrderHeader.get(0).getString("billFromPartyId");	        					
	        					partyIdTo = listaOrderHeader.get(0).getString("billToPartyId");	        						        						        						        					
	        				}
	        			}
	        			
	        		}	        	
				}				
			}
						
			//Realiza las afectaciones y la orden de Cobro 
			GenericValue devolucion = delegator.makeValidValue("Devolucion", context);
			String devolucionId = "";
                
			if(creaDevolDevengado)
			{	
				Debug.logInfo("Crea devol devengado",MODULE);
				devolucionId = delegator.getNextSeqId("Devolucion");
				Map<String, Object> output = devolucionDevengadoEjercido(dispatcher, delegator, context, clavePresupuestalMapDeve, mapaProductoDeve, montoMapDeve, "orderId", purchaseOrderId, userLogin, moneda, organizationPartyId, purchaseOrderId, "DEVO-LIBE-DEVE", fechaContable);
				if(ServiceUtil.isError(output)){
					return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
				}
				GenericValue transaccion = (GenericValue) output.get("transaccion");
				crearRegistroDevolucion(devolucion, devolucionId, purchaseOrderId, orderItemSeqIdDevolDeve, mapaProductoDeve, cantidadDevolverDeve, unitPriceDeve, transaccion.getString("acctgTransId"), transaccion.getString("poliza"), "DEVO-LIBE-DEVE", null, fechaContable);
			}			
			if(creaDevolEjercido)
			{	
				Debug.logInfo("Crea devol ejercido",MODULE);
				if(devolucionId.equals(""))
					devolucionId = delegator.getNextSeqId("Devolucion");								
				Map<String, Object> output = devolucionDevengadoEjercido(dispatcher, delegator, context, clavePresupuestalMapEjer, mapaProductoEjer, montoMapEjer, "orderId", purchaseOrderId, userLogin, moneda, organizationPartyId, purchaseOrderId, "DEVO-LIBE-EJER", fechaContable);
				if(ServiceUtil.isError(output)){
					return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
				}
				GenericValue transaccion = (GenericValue) output.get("transaccion");
				crearRegistroDevolucion(devolucion, devolucionId, purchaseOrderId, orderItemSeqIdDevolEjer, mapaProductoEjer, cantidadDevolverEjer, unitPriceEjer, transaccion.getString("acctgTransId"), transaccion.getString("poliza"), "DEVO-LIBE-EJER", null, fechaContable);
			}
			if(creaOrdenCobro)
			{	
				Debug.logInfo("Crea orden de cobro",MODULE);
				if(devolucionId.equals(""))
					devolucionId = delegator.getNextSeqId("Devolucion");
				String invoiceId = crearInvoice(delegator,dispatcher, partyIdFrom, partyIdTo, new Timestamp(Calendar.getInstance().getTimeInMillis()), moneda, montoTotalOrdenCobro, purchaseOrderId, userLogin, null, "DEVOLUCI\u00d3N", "INGRESO");
	    		if(invoiceId.equals("error"))
	    		{	
	    			return ServiceUtil.returnError("Existio error al crear el Invoice");
	    		}
	    		String verE= crearInvoiceItem(dispatcher, delegator, invoiceId, productIdsDevol, montoUnitarioDevol, claveMapDevol, cantidadDevol, userLogin, itemSeqIdDevol, purchaseOrderId, "INGRESO", null, fechaContable);
	    		if(verE!=null)
	    		{	
	    			return ServiceUtil.returnError("Existio error al crear el Invoice");
	    		}
	    		crearRegistroDevolucion(devolucion, devolucionId, purchaseOrderId, itemSeqIdDevol, productIdsDevol, cantidadDevol, montoUnitarioDevol, null, null, null, invoiceId, fechaContable);
				
			}													
						
			Map result = ServiceUtil.returnSuccess("La devoluci\u00f3n ha sido realizada con \u00e9xito");
	        result.put("devolucionId", devolucionId);	
			return result;
		} 
		catch (GenericEntityException | NullPointerException e)
		{	
			return ServiceUtil.returnError("Error al realizar la devoluci\u00f3n : " + e.getMessage());
		}												
	}
	
	
	/**
     * Metodo para liberar el presupuesto desde el Devengado al Disponible para una devolcuion donde no se ha ejercido.
     * @param dctx DispatchContext
     * @param context Map
     * @return Map
     * @throws GenericEntityException 
     * @throws GenericServiceException 
     */    
    public static void crearRegistroDevolucion(GenericValue devolucion, String devolucionId, String purchaseOrderId, Map orderItemSeqIdDevolDeve, Map mapaProducto, 
    Map cantidadDevolver, Map unitPrice, String acctgTransId, String poliza, String evento, String invoiceId, Timestamp fechaContable) throws GenericEntityException, GenericServiceException
    {	
    	for (int i = 0; i < orderItemSeqIdDevolDeve.size(); i++)         
    	{	
    		devolucion.set("devolucionId", devolucionId);
	    	devolucion.set("orderId", purchaseOrderId);
	    	devolucion.set("orderItemSeqId", orderItemSeqIdDevolDeve.get(Integer.toString(i)).toString());	    	
	    	devolucion.set("acctgTransId", acctgTransId);
	    	devolucion.set("poliza", poliza);
	    	devolucion.set("acctgTransTypeId", evento);	    		    		    	
	    	devolucion.set("invoiceId", invoiceId);	    		    	
	    	devolucion.set("fechaContable", fechaContable);
	    	devolucion.set("productId", mapaProducto.get(Integer.toString(i)).toString());
	    	devolucion.set("cantidad", cantidadDevolver.get(Integer.toString(i)).toString());
	    	devolucion.set("precioUnitario", unitPrice.get(Integer.toString(i)).toString());
	    	devolucion.create();
    	}				  		  	
	  	
    }
    
    /**
     * Metodo para liberar el presupuesto desde el Devengado al Disponible para una devolcuion donde no se ha ejercido.
     * @param dctx DispatchContext
     * @param context Map
     * @return Map
     * @throws GenericEntityException 
     * @throws GenericServiceException 
     */    
    public static Map devolucionDevengadoEjercido(LocalDispatcher dispatcher, Delegator delegator, Map context, Map clavePresupuestalMap, Map mapaProducto, Map montoMap,
    												String campo, String valorCampo, GenericValue userLogin, String moneda, String organizationPartyId, String orderId, 
    												String evento, Timestamp fechaContable) 
    												throws GenericEntityException, GenericServiceException, ParseException    							      							  
    {	
    	Map<String, Object> input = FastMap.newInstance();	
		Map<String, Object> output = FastMap.newInstance();
		Timestamp fechaTrans = new Timestamp(Calendar.getInstance().getTimeInMillis());
		
		context.put("montoMap", montoMap);
      	context.put("clavePresupuestalMap", clavePresupuestalMap);
      	context.put("mapaProducto", mapaProducto);      	
      	     	
      	input.put("eventoContableId", evento);
  		input.put("tipoClaveId", "EGRESO");
  		input.put("fechaRegistro", fechaTrans);
  		input.put("fechaContable", fechaContable);
  		input.put("currency", moneda);
  		input.put("usuario", userLogin.getString("userLoginId"));
  		input.put("organizationId", organizationPartyId);
  		input.put("descripcion", "Devoluci\u00f3n de articulos del pedido o contrato " + orderId);
  		input.put("roleTypeId", "BILL_FROM_VENDOR");
  		input.put("campo", campo);
  		input.put("valorCampo", valorCampo);
  		input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(evento, delegator, context, organizationPartyId, null, montoMap, mapaProducto)); 
  		output = dispatcher.runSync("creaTransaccionMotor", input, 1200, false);  
	  	return output;
    }
    
    /**
	 * Metodo que regresa el string de la clave presupuestal que hay en el invoiceItemSeqId  
	 * @throws GenericServiceException 
	 * @throws GenericEntityException 
	 */
	public static String obtieneClavePresupuestal(DispatchContext dctx, Map context, Delegator delegator, String invoiceId, String invoiceItemSeqId, String tipoClave) throws GenericServiceException, GenericEntityException
	{	LocalDispatcher dispatcher = dctx.getDispatcher();
		String clavePresupuestal = "";
		List<String> fieldsToSelect = UtilMisc.toList("acctgTagEnumId1", "acctgTagEnumId2", "acctgTagEnumId3", "acctgTagEnumId4", "acctgTagEnumId5", 
													  "acctgTagEnumId6", "acctgTagEnumId7", "acctgTagEnumId8", "acctgTagEnumId9", "acctgTagEnumId10", 
													  "acctgTagEnumId11", "acctgTagEnumId12", "acctgTagEnumId13", "acctgTagEnumId14", "acctgTagEnumId15", "acctgTagEnumIdAdmin");
		//Consultar las claves presupuestarias para llenar el map clavePresupuestalMap
        EntityCondition condicionInvoiceItem = EntityCondition.makeCondition(EntityOperator.AND,
												EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, invoiceId),
												EntityCondition.makeCondition("invoiceItemSeqId", EntityOperator.EQUALS, invoiceItemSeqId));					
        List<GenericValue> invoiceItemList = delegator.findByCondition("InvoiceItem", condicionInvoiceItem, fieldsToSelect, null);        
        if(!invoiceItemList.isEmpty())
        {	Iterator<GenericValue> invoiceItemIter = invoiceItemList.iterator();
        	while (invoiceItemIter.hasNext()) 
        	{	
        		GenericValue invoiceItem = invoiceItemIter.next();
	        	Map inputClasificaAdmin = FastMap.newInstance();
	            inputClasificaAdmin.put("tipoClave", tipoClave);
	            Map mapaClasif = dispatcher.runSync("buscaIndiceAdministrativa", inputClasificaAdmin);
	            Integer numClasAdmin = (Integer) mapaClasif.get("posicionClasificaAdmin");
	            for(int i=1; i<16; i++)
	            {	if(Integer.valueOf(numClasAdmin) == i)
	            	{	clavePresupuestal = clavePresupuestal.concat(UtilFinancial.buscaExternalId(invoiceItem.getString("acctgTagEnumIdAdmin"),delegator));
	            	}		            	
	            	String clasif = (String)invoiceItem.getString("acctgTagEnumId" + i);
	            	if(clasif != null && !clasif.isEmpty())
	            	{	clavePresupuestal = clavePresupuestal.concat(UtilFinancial.buscaSequenceId(clasif, delegator));
	            	}		            	
	            }		          
        	}
        }			
		return clavePresupuestal;		
	}
	
	
	/**
	 * Metodo que consulta las devolcuiones realizadas  
	 * @throws GenericServiceException 
	 * @throws GenericEntityException 
	 */
	public static void consultaDevolucion(Map<String, Object> context) throws GeneralException, ParseException 
	{		
			final ActionContext ac = new ActionContext(context);
			// possible fields we're searching by
			String devolucionId = ac.getParameter("devolucionId");		
			String orderId = ac.getParameter("orderId");
			String poliza = ac.getParameter("poliza");
			String invoiceId = ac.getParameter("invoiceId");
			String productId = ac.getParameter("productId");
			String cantidad = ac.getParameter("cantidad");
			String precioUnitarioFrom = ac.getParameter("precioUnitarioFrom");
			String precioUnitarioThru = ac.getParameter("precioUnitarioThru");
			
			DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
			final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
					.getLedgerRepository();
							
			if ("Y".equals(ac.getParameter("performFind"))) {
				List<EntityCondition> searchConditions = new FastList<EntityCondition>();

				if (UtilValidate.isNotEmpty(devolucionId)) {
					searchConditions.add(EntityCondition.makeCondition("devolucionId", EntityOperator.EQUALS, devolucionId));
				}
				if (UtilValidate.isNotEmpty(orderId)) {
					searchConditions.add(EntityCondition.makeCondition("orderId",EntityOperator.EQUALS, orderId));
				}				
				if (UtilValidate.isNotEmpty(poliza)) {
					searchConditions.add(EntityCondition.makeCondition("poliza", EntityOperator.EQUALS,poliza));
				}
				if (UtilValidate.isNotEmpty(invoiceId)) {
					searchConditions.add(EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS,invoiceId));
				}
				if (UtilValidate.isNotEmpty(productId)) {
					searchConditions.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS,productId));
				}
				if (UtilValidate.isNotEmpty(cantidad)) {
					searchConditions.add(EntityCondition.makeCondition("cantidad", EntityOperator.EQUALS,cantidad));
				}
				if (UtilValidate.isNotEmpty(precioUnitarioFrom)) {
	           	 	searchConditions.add(EntityCondition.makeCondition(Devolucion.Fields.precioUnitario.name(), EntityOperator.GREATER_THAN_EQUAL_TO, new BigDecimal(precioUnitarioFrom)));
	            }
	            if (UtilValidate.isNotEmpty(precioUnitarioThru)) {
	              	searchConditions.add(EntityCondition.makeCondition(Devolucion.Fields.precioUnitario.name(), EntityOperator.LESS_THAN_EQUAL_TO, new BigDecimal(precioUnitarioThru)));
	            }
	            
				Debug.logInfo("search conditions : "+ EntityCondition.makeCondition(searchConditions,EntityOperator.AND).toString(), MODULE);
				EntityListBuilder devolucionesListBuilder = null;
				PageBuilder<Devolucion> pageBuilderOrdenPago = null;			

				
				devolucionesListBuilder = new EntityListBuilder(ledgerRepository, Devolucion.class,EntityCondition.makeCondition(searchConditions,EntityOperator.AND), null, null);
				pageBuilderOrdenPago = new PageBuilder<Devolucion>() 
				{	
					public List<Map<String, Object>> build(List<Devolucion> page) throws Exception 
					{	
						List<Map<String, Object>> newPage = FastList.newInstance();
						for (Devolucion devolucion : page) 
						{	
							Map<String, Object> newRow = FastMap.newInstance();													
							newRow.putAll(devolucion.toMap());
							newRow.put("currencyUomId", devolucion.getRelatedOne("OrderHeader").getString("currencyUom"));
							newPage.add(newRow);
						}
						return newPage;
					}
				};
				devolucionesListBuilder.setPageBuilder(pageBuilderOrdenPago);
				ac.put("devolucionesListBuilder", devolucionesListBuilder);
				
				ac.put("devolucionId", devolucionId);
				

			}
		}
	
	
	/**
	 * Metodo que valida el monto a apliar en un pedido o contrato. No debe de pasar el 20% del monto total del pedido  
	 * @throws GenericServiceException 
	 * @throws GenericEntityException 
	 */
	public static boolean validarMontoAmpliar(LocalDispatcher dispatcher, Delegator delegator, Map context, Map rowSubmits, Map montoAmpliar, String purchaseOrderId) throws GenericServiceException, GenericEntityException
	{	
		BigDecimal montoTotalAmpliar = BigDecimal.ZERO;
		for(int i=0; i<montoAmpliar.size(); i++)
		{	
			if(montoAmpliar.get(Integer.toString(i)) != null && !montoAmpliar.get(Integer.toString(i)).equals(""))
			{	
				montoTotalAmpliar = montoTotalAmpliar.add(new BigDecimal(montoAmpliar.get(Integer.toString(i)).toString()));
			}			
		}
				
		List<String> fieldsToSelect = UtilMisc.toList("grandTotal");
        EntityCondition condicionOrderHeader = EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, purchaseOrderId);					
        List<GenericValue> orderHeaderList = delegator.findByCondition("OrderHeader", condicionOrderHeader, fieldsToSelect, null);        
        if(!orderHeaderList.isEmpty())
        {	
        	Iterator<GenericValue> orderHeaderIter = orderHeaderList.iterator();
        	while (orderHeaderIter.hasNext()) 
        	{	
        		GenericValue orderHeader = orderHeaderIter.next();
        		BigDecimal grandTotal = orderHeader.getBigDecimal("grandTotal");
        		BigDecimal porcentajeGrandTotal = grandTotal.multiply(new BigDecimal(1.2)).setScale(decimals, rounding);
        		BigDecimal sumaTotalyAmpliar = montoTotalAmpliar.add(grandTotal);
        		if(sumaTotalyAmpliar.compareTo(porcentajeGrandTotal) > 0)
        		{	
        			return false;
        		}        		
        	}
        }			
		return true;		
	}
	
	/**
	 * Metodo que actualiza la tabla de maximos y minimos para el manejo de notificaciones al usuario  
	 * @throws GenericServiceException 
	 * @throws GenericEntityException 
	 */
	public static void registrarMultiplesMaximosMinimos(Delegator delegator, Map quantitiesAccepted, Map productIds, Map rowSubmits) throws GenericServiceException, GenericEntityException
	{	
		for(int i=0; i<quantitiesAccepted.size(); i++)
		{	if(quantitiesAccepted.get(Integer.toString(i)) != null && !quantitiesAccepted.get(Integer.toString(i)).equals("0"))
			{	if(rowSubmits.get(Integer.toString(i)) != null && (rowSubmits.get(Integer.toString(i)).equals("Y")||rowSubmits.get(Integer.toString(i)).equals("N")))
				{	EntityCondition condicionProductoAlmacenMaxMin = EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productIds.get(Integer.toString(i)));
					List<GenericValue> productoAlmacenMaxMinList = delegator.findByCondition("ProductoAlmacenMaxMin", condicionProductoAlmacenMaxMin, null, null);
					if(!productoAlmacenMaxMinList.isEmpty())
				    {	
				        Iterator<GenericValue> productoAlmacenMaxMinIter = productoAlmacenMaxMinList.iterator();
				        while (productoAlmacenMaxMinIter.hasNext()) 
				        {	
				        	GenericValue productoAlmacenMaxMin = productoAlmacenMaxMinIter.next();
				        	long cantidadRecibida = Long.parseLong(quantitiesAccepted.get(Integer.toString(i)).toString());
				        	productoAlmacenMaxMin.set("cantidadActual", productoAlmacenMaxMin.getLong("cantidadActual") + cantidadRecibida);
				        	delegator.store(productoAlmacenMaxMin);
				        }
				    }
				}
			}			
		}		
	}
	
	/**
	 * Metodo que actualiza la tabla de maximos y minimos para el manejo de notificaciones al usuario  
	 * @throws GenericServiceException 
	 * @throws GenericEntityException 
	 */
	public static void registrarMaximosMinimos(Delegator delegator, BigDecimal quantityAccepted, String productId) throws GenericServiceException, GenericEntityException
	{	
		EntityCondition condicionProductoAlmacenMaxMin = EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId);
		List<GenericValue> productoAlmacenMaxMinList = delegator.findByCondition("ProductoAlmacenMaxMin", condicionProductoAlmacenMaxMin, null, null);
		if(!productoAlmacenMaxMinList.isEmpty())
	    {	
	        Iterator<GenericValue> productoAlmacenMaxMinIter = productoAlmacenMaxMinList.iterator();
	        while (productoAlmacenMaxMinIter.hasNext()) 
	        {	
	        	GenericValue productoAlmacenMaxMin = productoAlmacenMaxMinIter.next();
	        	long cantidadRecibida = Long.valueOf(quantityAccepted.longValue());
	        	productoAlmacenMaxMin.set("cantidadActual", productoAlmacenMaxMin.getLong("cantidadActual") + cantidadRecibida);
	        	delegator.store(productoAlmacenMaxMin);
	        }
	    }		
	}
	
	
	/**
	 * Metodo para consultar la tabla de maximos y minimos 
	 * @param context
	 * @return
	 * @throws GeneralException  
	 * @throws ParseException 
	 */
	public static void consultaMaximosMinimos(Map<String, Object> context)
			throws GeneralException, ParseException {

		final ActionContext ac = new ActionContext(context);
		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
        final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();
        Delegator delegator = ac.getDelegator();
        
		List<EntityCondition> searchConditions = new FastList<EntityCondition>();
		List<String> orderBy = UtilMisc.toList("productId");
        List<ProductoAlmacenMaxMin> productoAlmacenMaxMin = ledgerRepository.findList(ProductoAlmacenMaxMin.class, searchConditions, null, orderBy);	       
        List<Map<String, Object>> listProductoAlmacenMaxMin = new FastList<Map<String, Object>>();
        for (ProductoAlmacenMaxMin s : productoAlmacenMaxMin) {
            Map<String, Object> map = s.toMap();
            String productName = "";
            String almacenName = "";	            
			if(map.get("productId") != null && !map.get("productId").equals(""))
			{	GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", map.get("productId")));
				productName = (String) product.get("productName");													
			}											
			if(map.get("facilityId") != null && !map.get("facilityId").equals(""))
			{	GenericValue facility = delegator.findByPrimaryKey("Facility", UtilMisc.toMap("facilityId", map.get("facilityId")));
				almacenName = (String) facility.get("facilityName");													
			}
			Debug.log("productName: " + productName);
			Debug.log("almacenName: " + almacenName);			
			map.put("productName", productName);
			map.put("almacenName", almacenName);
            listProductoAlmacenMaxMin.add(map);
        }
        ac.put("listProductoAlmacenMaxMin", listProductoAlmacenMaxMin);	        	        	       
		
	}	
		
	/**
	 * Metodo para crear un registro de un producto con sus maximos y minimos en almacen 
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 * @throws GenericServiceException 
	 * @throws ParseException 
	 */
	public static Map<String, Object> guardarMaximosMinimos(DispatchContext dctx, Map<?, ?> context) throws GenericEntityException, GenericServiceException, ParseException 
	{	
		Debug.log("Entra a guardarMaximosMinimos");
		Delegator delegator = dctx.getDelegator();					
		String productId = (String) context.get("productId");
		String facilityId = (String) context.get("facilityId");
		int maximo = (Integer) context.get("maximo");
		int minimo = (Integer) context.get("minimo");
		BigDecimal totalExistencia = BigDecimal.ZERO;
		
		Debug.logInfo("Context: " + context,MODULE);				
		try 
		{	//Validar si existe
			List<GenericValue> listProductoAlmacenMaxMin = delegator.findByAnd("ProductoAlmacenMaxMin", UtilMisc.toMap("productId", productId, "facilityId", facilityId));
    		if(!listProductoAlmacenMaxMin.isEmpty())
    		{	
    			return ServiceUtil.returnError("El producto ingresado ya ha sido registrado previamente en el almac\u00e9n seleccionado");
    		}
    		List<String> fieldsToSelect = UtilMisc.toList("productId", "facilityId", "quantityOnHandTotal", "availableToPromiseTotal");
	        List<String> orderBy = UtilMisc.toList("productId");
    		EntityCondition condicionesInventoryItem = EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId);		
			List<GenericValue> listInventoryItem = delegator.findByCondition("InventoryItem", condicionesInventoryItem, fieldsToSelect, orderBy);
			if(!listInventoryItem.isEmpty())
			{	Iterator<GenericValue> inventoryItemIter = listInventoryItem.iterator();
				while (inventoryItemIter.hasNext())
				{	GenericValue inventoryItem = inventoryItemIter.next();														
					totalExistencia = totalExistencia.add(inventoryItem.getBigDecimal("quantityOnHandTotal"));
				}								
			}    	
			Debug.logInfo("totalExistencia: " + totalExistencia,MODULE);
			
			GenericValue productoAlmacenMaxMin = GenericValue.create(delegator.getModelEntity("ProductoAlmacenMaxMin"));
			productoAlmacenMaxMin.set("productId", productId);
			productoAlmacenMaxMin.set("facilityId", facilityId);
			productoAlmacenMaxMin.set("maximo", maximo);
			productoAlmacenMaxMin.set("minimo", minimo);
			productoAlmacenMaxMin.set("cantidadActual", Integer.valueOf(totalExistencia.intValue()));			
			delegator.create(productoAlmacenMaxMin);
			
			return ServiceUtil.returnSuccess("Los m\u00e1ximos y m\u00ednimos del producto ["+ productId+"] han sido registrados exitosamente");									
			
		} 
		catch (GenericEntityException e) 
		{	return ServiceUtil.returnError("Error al realizar la devoluci\u00f3n : " + e.getMessage());
		}												
	}
	
	/**
	 * Metodo para consultar los productos que estan en su minimo por almacen 
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 * @throws GenericServiceException 
	 * @throws ParseException 
	 */
	public static void consultaProductosEnMinimo(Map<String, Object> context) {
		try 
		{	
			final ActionContext ac = new ActionContext(context);		
			Debug.logInfo("context: " + context,MODULE);
			Delegator delegator = ac.getDelegator();
			DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
			final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();				

			String productId = ac.getParameter("productId");
			String maximoDesde = ac.getParameter("maximoDesde");
			String minimoDesde = ac.getParameter("minimoDesde");
			String cantidadActualDesde = ac.getParameter("cantidadActualDesde");
			String maximoHasta = ac.getParameter("maximoHasta");		
			String minimoHasta = ac.getParameter("minimoHasta");		
			String cantidadActualHasta = ac.getParameter("cantidadActualHasta");
			String facilityId = ac.getParameter("facilityId");
			GenericValue userLogin = (GenericValue) context.get("userLogin");
			String partyId = userLogin.getString("partyId");


			List<EntityCondition> searchConditions = new FastList<EntityCondition>();

			if (UtilValidate.isNotEmpty(productId) && productId != null) 
			{	searchConditions.add(EntityCondition.makeCondition("productId",EntityOperator.EQUALS, productId));
			} 
			if (UtilValidate.isNotEmpty(maximoDesde) && maximoDesde != null)
			{	searchConditions.add(EntityCondition.makeCondition("maximo", EntityOperator.GREATER_THAN_EQUAL_TO, maximoDesde));
			}
			if (UtilValidate.isNotEmpty(maximoHasta) && maximoHasta != null)
			{	searchConditions.add(EntityCondition.makeCondition("maximo", EntityOperator.LESS_THAN_EQUAL_TO, maximoHasta));
			}
			if (UtilValidate.isNotEmpty(minimoDesde) && minimoDesde != null)
			{	searchConditions.add(EntityCondition.makeCondition("minimo", EntityOperator.GREATER_THAN_EQUAL_TO, minimoDesde));
			}
			if (UtilValidate.isNotEmpty(minimoHasta) && minimoHasta != null)
			{	searchConditions.add(EntityCondition.makeCondition("minimo", EntityOperator.LESS_THAN_EQUAL_TO, minimoHasta));
			}
			if (UtilValidate.isNotEmpty(cantidadActualDesde) && cantidadActualDesde != null)
			{	searchConditions.add(EntityCondition.makeCondition("cantidadActual", EntityOperator.GREATER_THAN_EQUAL_TO, cantidadActualDesde));
			}
			if (UtilValidate.isNotEmpty(cantidadActualHasta) && cantidadActualHasta != null)
			{	searchConditions.add(EntityCondition.makeCondition("cantidadActual", EntityOperator.LESS_THAN_EQUAL_TO, cantidadActualHasta));
			}		
			searchConditions.add(EntityCondition.makeCondition("facilityId",EntityOperator.EQUALS, facilityId));
			searchConditions.add(EntityCondition.makeCondition("partyId",EntityOperator.EQUALS, partyId));
			Debug.logInfo("condiciones: " + searchConditions,MODULE);

			List<String> orderBy = UtilMisc.toList("productId");
			List<ConsultaMaxMinAlmacen> ConsultaMaxMinAlmacen = ledgerRepository.findList(ConsultaMaxMinAlmacen.class, searchConditions, null, orderBy);	       
			List<Map<String, Object>> listaProductosEnMinimo = new FastList<Map<String, Object>>();
			for (ConsultaMaxMinAlmacen s : ConsultaMaxMinAlmacen) {
				Map<String, Object> map = s.toMap();
				String productName = "";
				String almacenName = "";	            
				if(map.get("productId") != null && !map.get("productId").equals(""))
				{	GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", map.get("productId")));
				productName = (String) product.get("productName");													
				}											
				if(map.get("facilityId") != null && !map.get("facilityId").equals(""))
				{	GenericValue facility = delegator.findByPrimaryKey("Facility", UtilMisc.toMap("facilityId", map.get("facilityId")));
				almacenName = (String) facility.get("facilityName");													
				}
				map.put("productName", productName);
				map.put("almacenName", almacenName);
				listaProductosEnMinimo.add(map);
			}
			ac.put("listaProductosEnMinimo", listaProductosEnMinimo);

		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

	}
}
