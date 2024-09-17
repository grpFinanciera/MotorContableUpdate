<#--
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
-->

<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<#-- This file has been modified from the version included with the Apache-licensed OFBiz product application -->
<#-- This file has been modified by Open Source Strategies, Inc. -->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<script type="text/javascript">

    function rowClassChange(/*Element*/ input, /*Number*/ rowIndex) {
        // trim the input and replace empty by "0"
        if (input.value != null && input.value != "0") {
            input.value = input.value.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
            input.value = input.value.replace(/^00*/, '');
            if (input.value == "") input.value = "0";
        }
        // color the row according to the value: 0=>normal positive_integer=>green invalid=>red
        if (input.value != null && input.value != "0") {            
            if(parseInt(input.value) != input.value - 0 || parseInt(input.value) < 0) {
              input.parentNode.parentNode.className = 'rowLightRed';
            } else {
              input.parentNode.parentNode.className = 'rowLightGreen';
            }
        } else {
            input.parentNode.parentNode.className = rowIndex % 2 == 0 ? 'rowWhite' : 'rowLightGray';
        }
                
        var cantidad = parseFloat("0");
        var cantidadTotal = parseFloat("0");
        var montoPagar = parseFloat("0");
        var montoPagarTotal = parseFloat("0");
        var contador = 0;
        while(contador < document.getElementById('orderItemDatasSize').value)        
        {	cantidad = document.getElementById('quantityAccepted_o_'+contador).value;  		
			cantidadTotal = parseFloat(cantidadTotal) + parseFloat(cantidad);				
			document.getElementById('cantidadPorRecibir').innerHTML = parseFloat(cantidadTotal);
			
			montoPagar = document.getElementById('unitCost_o_'+contador).value * parseFloat(cantidad);  		
			montoPagarTotal = parseFloat(montoPagarTotal) + parseFloat(montoPagar);				
			document.getElementById('montoPorRecibir').innerHTML = parseFloat(montoPagarTotal).toFixed(2);
			
			contador++;			
        }        		
		                	
			
		  	
		  			  
        
    }

</script>

<div class="screenlet" style="position: fixed; bottom:200px; left:8px; width:158px; z-index:1;_position:absolute;_bottom:expression(eval(document.body.scrollBottom)-100);">
  <div class="screenlet-header">
    <span class="boxhead">${uiLabelMap.WarehouseTotalRecibir}</span>
  </div>
  <!-- orgCurrencyUomId :  ${orgCurrencyUomId!} -->
  <div class="screenlet-body">
    	
      <table width="100%" cellpadding="0" cellspacing="2">
        <tr>
          <td><div class="tabletext"><b>${uiLabelMap.Cantidad}</b></div></td>			
          <td align="right"><label> </label></td>
          <td align="right"><label id="cantidadPorRecibir">0</label></td>
        </tr>
        <tr>
          <td><div class="tabletext"><b>${uiLabelMap.Monto}</b></div></td>
          <td align="right"><label>$</label></td>
          <td align="right"><label id="montoPorRecibir">0</label></td>
        </tr>        
      </table>
    
  </div>
</div>

<#assign productId = parameters.productId?if_exists/>
<#if shipmentPoliza?has_content>
	<#list shipmentPoliza as shipment>
		<#assign transId = shipment.acctgTransId!/>
		<#if transId?has_content>
			<@form name="verPoliza${transId}" url="viewAcctgTrans?acctgTransId=${transId}"/>
				<#assign verPolizaAction>${verPolizaAction!}<@actionForm form="verPoliza${transId}" text=transId /></#assign>
		</#if>
	</#list>
</#if>

<#if polizaLiberacion?has_content>
	<#list polizaLiberacion as shipment>
		<#assign transId2 = shipment.acctgTransId!/>
		<#if transId2?has_content>
			<@form name="verPoliza2${transId2}" url="viewAcctgTrans?acctgTransId=${transId2}"/>
				<#assign verPolizaAction2>${verPolizaAction2!}<@actionForm form="verPoliza2${transId2}" text=transId2 /></#assign>
		</#if>
	</#list>
</#if>

<#if polizaAmpliacion?has_content>
	<#list polizaAmpliacion as shipment>
		<#assign transId3 = shipment.acctgTransId!/>
		<#if transId3?has_content>
			<@form name="verPoliza3${transId3}" url="viewAcctgTrans?acctgTransId=${transId3}"/>
				<#assign verPolizaAction3>${verPolizaAction3!}<@actionForm form="verPoliza3${transId3}" text=transId3 /></#assign>
		</#if>
	</#list>
</#if>

<#if polizaReduccion?has_content>
	<#list polizaReduccion as shipment>
		<#assign transId4 = shipment.acctgTransId!/>
		<#if transId4?has_content>
			<@form name="verPoliza4${transId4}" url="viewAcctgTrans?acctgTransId=${transId4}"/>
				<#assign verPolizaAction4>${verPolizaAction4!}<@actionForm form="verPoliza4${transId4}" text=transId4 /></#assign>
		</#if>
	</#list>
</#if>

<#assign factura = "sinFactura"/>
<#if shipmentReceipts?has_content>
	<#list shipmentReceipts as shipmentReceipt>
		<#if shipmentReceipt.numeroFactura != factura>
			<@form name="verFormatoPDF${shipmentReceipt.numeroFactura}" url="fulfilledBackOrders.pdf?orderId=${orderId}&numeroFactura=${shipmentReceipt.numeroFactura}"/>
				<#assign verFormatoPDFAction>${verFormatoPDFAction!}<@actionForm form="verFormatoPDF${shipmentReceipt.numeroFactura}" text="Factura " + shipmentReceipt.numeroFactura/></#assign>
				<#assign factura = shipmentReceipt.numeroFactura/>
		</#if>
	</#list>
</#if>

<#assign facturaDev = "sinFactura"/>
<#if devegoParcialMontos?has_content>
	<#list devegoParcialMontos as devegoParcial>
		<#if devegoParcial.numeroFactura != facturaDev>
			<@form name="verFormatoPDFDev${devegoParcial.numeroFactura}" url="recDevengoMontosParciales.pdf?orderId=${orderId}&numeroFactura=${devegoParcial.numeroFactura}"/>
				<#assign verFormatoPDFDevAction>${verFormatoPDFDevAction!}<@actionForm form="verFormatoPDFDev${devegoParcial.numeroFactura}" text="Factura " + devegoParcial.numeroFactura/></#assign>
				<#assign facturaDev = devegoParcial.numeroFactura/>
		</#if>
	</#list>
</#if>

<form name="ReceiveInventoryAgainstPurchaseOrder" action="<@ofbizUrl>ReceiveInventoryAgainstPurchaseOrder</@ofbizUrl>">
    <input type="hidden" name="clearAll" value="Y"/>
    <div class="tabletext" style="float: left;">
        ${uiLabelMap.ProductOrderId} : <@inputLookup name="purchaseOrderId" lookup="LookupPurchaseOrder" form="ReceiveInventoryAgainstPurchaseOrder" default=orderId?if_exists/>
        <!-- ${uiLabelMap.ProductOrderShipGroupId} : <@inputText name="shipGroupSeqId" size="20" default=shipGroupSeqId?default("00001")/> -->
		<td align="right"> 
			<@display text=uiLabelMap.FinancialsAccountigDate+" :" class="requiredField"/>
		</td>
		<@inputDateCell name="fechaContable"/>
        <#if proveedor?has_content>
        	${uiLabelMap.ProductSupplier} : <@inputText name="proveedor" size="50" default=proveedor.partyId?if_exists+" "+proveedor.groupName?if_exists/>
        </#if>	
        <input type="submit" value="${uiLabelMap.CommonFind}" class="smallSubmit"/>
    </div>
    <#if shipmentReceipts?has_content>
    		<div class="subMenuBar">
    			<@selectActionForm name="shipmentReceiptActions" prompt=uiLabelMap.WarehouseFormatoPDF>
    				${verFormatoPDFAction?if_exists}
				</@selectActionForm>
    		</div>
    </#if>
    <#if devegoParcialMontos?has_content>
    		<div class="subMenuBar">
    			<@selectActionForm name="devegoParcialMontosActions" prompt=uiLabelMap.WarehouseFormatoPDFDev>
    				${verFormatoPDFDevAction?if_exists}
				</@selectActionForm>
    		</div>
    </#if>
    <#if verPolizaAction2?has_content>
	    <div class="subMenuBar">
	    <@selectActionForm name="shipmentActions2" prompt=uiLabelMap.FinancialVerPolizaL>
	    	${verPolizaAction2?if_exists}
		</@selectActionForm>
		</div>
	</#if>
	<#if verPolizaAction3?has_content>
	    <div class="subMenuBar">
	    <@selectActionForm name="shipmentActions3" prompt=uiLabelMap.FinancialVerPolizaA>
	    	${verPolizaAction3?if_exists}
		</@selectActionForm>
		</div>
	</#if>
	<#if verPolizaAction4?has_content>
	    <div class="subMenuBar">
	    <@selectActionForm name="shipmentActions4" prompt=uiLabelMap.FinancialVerPolizaR>
	    	${verPolizaAction4?if_exists}
		</@selectActionForm>
		</div>
	</#if>
    <#if verPolizaAction?has_content>
	    <div class="subMenuBar">
	    <@selectActionForm name="shipmentActions" prompt=uiLabelMap.FinancialVerPoliza>
	    	${verPolizaAction?if_exists}
		</@selectActionForm>
		</div>
	</#if>
    <div class="spacer"></div>
</form>
    
<div class="errorMessage" style="margin:5px 0px 5px 5px">
    <#if errorMessage?has_content>
        ${errorMessage}
    <#elseif orderId?has_content && shipGroupSeqId?has_content && !orderHeader?exists>
        <@expandLabel label="WarehouseErrorOrderIdAndShipGroupSeqIdNotFound" params={"orderId", orderId, "shipGroupSeqId", shipGroupSeqId} />
        <#assign totalAvailableToReceive = 0/>
    <#elseif orderId?has_content && !orderHeader?exists>
        <@expandLabel label="ProductErrorOrderIdNotFound" params={"orderId", orderId} />
        <#assign totalAvailableToReceive = 0/>
    <#elseif orderHeader?exists && orderHeader.orderTypeId != "PURCHASE_ORDER">
        <@expandlabel label="ProductErrorOrderNotPurchaseOrder" params={"orderId", orderId} />
        <#assign totalAvailableToReceive = 0/>
    <#elseif orderHeader?exists && orderHeader.statusId != "ORDER_APPROVED" && orderHeader.statusId != "ORDER_COMPLETED">
        <@expandLabel label="WarehouseErrorOrderNotApproved" params={"orderId", orderId} />
        <#assign totalAvailableToReceive = 0/>
    <#elseif ProductReceiveInventoryAgainstPurchaseOrderProductNotFound?exists>
        <@expandLabel label="ProductReceiveInventoryAgainstPurchaseOrderProductNotFound" params={"productId", productId, "orderId", orderId} />
        <script type="text/javascript">window.onload=function(){alert('<@expandLabel label="ProductReceiveInventoryAgainstPurchaseOrderProductNotFound" params={"productId", productId?default(""), "orderId", orderId} />')};</script>
    <#elseif ProductReceiveInventoryAgainstPurchaseOrderQuantityExceedsAvailableToReceive?exists>
        <@expandLabel label="ProductReceiveInventoryAgainstPurchaseOrderQuantityExceedsAvailableToReceive" params={"newQuantity", newQuantity?if_exists, "productId", productId?default("")} />
        <script type="text/javascript">window.onload=function(){alert('<@expandLabel label="ProductReceiveInventoryAgainstPurchaseOrderQuantityExceedsAvailableToReceive" params={"newQuantity", newQuantity?if_exists, "productId", productId?default("")} />')};</script>
    </#if>
</div>

<#if ProductReceiveInventoryAgainstPurchaseOrderQuantityGoesToBackOrder?exists && ! ProductReceiveInventoryAgainstPurchaseOrderQuantityExceedsAvailableToReceive?exists && productId?exists>
    <div class="errorMessage" style="color:green;margin:5px 0px 5px 5px">
        <#assign uiLabelWithVar=uiLabelMap.ProductReceiveInventoryAgainstPurchaseOrderQuantityGoesToBackOrder?interpret><@uiLabelWithVar/>
        <@expandLabel label="ProductReceiveInventoryAgainstPurchaseOrderQuantityGoesToBackOrder" params={"quantityToBackOrder", quantityToBackOrder?if_exists, "quantityToReceive", quantityToReceive?if_exists, "productId", productId?default("")}/>
        <script type="text/javascript">window.onload=function(){alert('<@expandLabel label="ProductReceiveInventoryAgainstPurchaseOrderQuantityGoesToBackOrder" params={"quantityToBackOrder", quantityToBackOrder?if_exists, "quantityToReceive", quantityToReceive?if_exists, "productId", productId?default("")}/>')};</script>
    </div>
</#if>

<#if ! error?exists>
    <#assign itemsAvailableToReceive = itemsAvailableToReceive?default(totalAvailableToReceive?default(0) &gt; 0)/>
    <#if orderItemDatas?exists>
        <#assign totalReadyToReceive = 0/>
        <form action="<@ofbizUrl>issueOrderItemToShipmentAndReceiveAgainstPO?clearAll=Y</@ofbizUrl>" method="post" name="selectAllForm">
            <input type="hidden" name="facilityId" value="${facilityId}"/>
            <input type="hidden" name="purchaseOrderId" value="${orderId}"/>
            <input type="hidden" name="shipGroupSeqId" value="${shipGroupSeqId}"/>
            <input type="hidden" name="currencyUom" value="${orderHeader.currencyUom?default("")}"/>
            <input type="hidden" name="ownerPartyId" value="${(facility.ownerPartyId)?if_exists}"/>
            <@inputHidden name="fechaContable" value=fechaContableString!"" />
            <@inputHidden name="completePurchaseOrder" value="N"/>
            <table class="listTable">
                <tr class="listTableHeader" >
					<td colspan="12" >
					<table border="0" width="100%">
                	<td align="right">
					<@display text=uiLabelMap.AccountingTipoDocumento+" :" class="requiredField"/>
					</td> 
                	<@inputSelectCell id="acctgTransTypeId" name="acctgTransTypeId" list=listEventos?sort_by("descripcion") key="acctgTransTypeId" 
                	displayField="descripcion" default=(acctgTransTypeId.acctgTransTypeId)! defaultOptionText=(acctgTransTypeId.description)! 
                	onChange="cambiaEvento(this.value,'10');" required=false />
					<#if pagoAnticipado?exists>
						<td align="right">
						<@display text=uiLabelMap.AccountingTipoDocumentoPagoA+" :" class="requiredField"/>
						</td> 
						<@inputSelectCell id="acctgTransTypeIdPA" name="acctgTransTypeIdPA" list=listEventosPagoAnti?sort_by("descripcion") key="acctgTransTypeId" 
	                	displayField="descripcion" default=(acctgTransTypeId.acctgTransTypeId)! defaultOptionText=(acctgTransTypeId.description)! 
	                	required=false />
					</#if>
                	<td align="right">
                		<@display text=uiLabelMap.PurchInvoiceNumber+" :" class="requiredField"/>
                	</td> 
                	<@inputTextCell name="numeroFactura" size=18 default=numeroFactura/>
					<td align="right"> 
                		<@display text=uiLabelMap.PurchFechaFactura+" :" class="requiredField" />
                	</td> 
                	<@inputDateCell name="fechaFactura" default=fechaFactura/> 
                	<tr>
					<td align="right"> 
                		<@display text=uiLabelMap.PurchDescription+" :" class="requiredField"/>
                	</td>
					<@inputTextCell name="comentario" size=70 maxlength="255" colspan="4" default=comentario />
					<#if pagoAnticipado?exists>
					<td> 
                		<@display text="Monto de pago anticipado: "+pagoAnticipado/>
                	</td>
                	<td> 
                		<@display text="Monto restante de pago anticipado: "+pagoAnticipadoRestante/>
                	</td>
                	</#if>
					</tr>
					</table>
					</td>
                </tr>
            </table>
            <table class="listTableFijo" >
            	<thead>
        		<tr class="listTableHeader" >
					<th><div class="tableheadtext">${uiLabelMap.WarehouseProductId}</div></th>
		            <th><div class="tableheadtext">${uiLabelMap.ProductProduct}</div></th>
		            <th><div class="tableheadtext">${uiLabelMap.Descripcion}</div></th>                    
                    <th><div class="tableheadtext">${uiLabelMap.FinancialsEndDate}</div></th> 
		            <th><div class="tableheadtext">${uiLabelMap.WarehouseNetOrdered}</div></th>
		            <th><div class="tableheadtext">${uiLabelMap.WarehouseCurrentBackOrderedQty}</div></th>
		            <th><div class="tableheadtext">${uiLabelMap.CommonReceived}</div></th>
		            <th><div class="tableheadtext">${uiLabelMap.ProductOpenQuantity}</div></th>
		            <th><div class="tableheadtext">${uiLabelMap.ProductUnitPrice}</div></th>
		            <th><div class="tableheadtext">${uiLabelMap.ProductQuantityReceive}</div></th>
		            <#--<td style="width:125px"><div class="tableheadtext">${uiLabelMap.WarehouseFulfilledBackOrders}</div></td>-->
		            <#if itemsAvailableToReceive>
		                <th><div class="tableheadtext">${uiLabelMap.CommonReceive}</div></th>
		               <#-- <td><div class="tableheadtext">${uiLabelMap.ProductInventoryItemType}</div></td> -->
		            </#if>
		            <#--<td><div class="tableheadtext">${uiLabelMap.WarehouseLot}</div></td>-->                    
		            <th><div class="tableheadtext">${uiLabelMap.AmpliarReducir}</div></th>
	            	<#if itemsAvailableToReceive>
                        <th>
							<div class="tableheadtext">${uiLabelMap.CommonReceive}
								<input type="checkbox" name="selectAll" value="${uiLabelMap.CommonY}" onclick="javascript:toggleAll(this, 'selectAllForm');">
							</div>
                        </th>
						<th align="right">
                            <div class="tableheadtext">${uiLabelMap.CommonDeductiva}</div>
                        </th>
                    </#if>                    	                              
				</tr>
				</thead>
				<tbody class="listTableFijoBody" > 
                <#list orderItemDatas.values()?default({}) as orderItemData>
                    <#assign orderItem = orderItemData.orderItem>
                    <#assign product = orderItemData.product?if_exists>
                    <#assign itemShipGroupSeqId = orderItemData.shipGroupSeqId?if_exists>
                    <#assign totalQuantityReceived = orderItemData.totalQuantityReceived?default(0)>
                    <#assign availableToReceive = orderItemData.availableToReceive?default(0)>
                    <#assign backOrderedQuantity = orderItemData.backOrderedQuantity?default(0)>
                    <#assign unitPrice = orderItemData.unitPrice?default(0)>
                    <#assign montoRestante = orderItemData.montoRestante?if_exists>
                    <#assign fulfilledOrderIds = orderItemData.fulfilledOrderIds>
                    <#assign lotIds = orderItemData.lotIds>
                    <#if shipmentReceiptTemp2?exists>
                    	<#assign rowSubmit = orderItemData.rowSubmit>
                    	<#assign rowDeductiva = orderItemData.rowDeductiva>
                    </#if>
                    <#assign quantityToReceive = 0>
                    <#assign montoComprometido = 0>
                    <#if itemsAvailableToReceive && availableToReceive &gt; 0 >
                        <#if itemQuantitiesToReceive?exists && itemQuantitiesToReceive.get(orderItem.orderItemSeqId)?exists>
                            <#assign quantityToReceive = itemQuantitiesToReceive.get(orderItem.orderItemSeqId)>
                        </#if>
                        <#assign totalReadyToReceive = totalReadyToReceive + quantityToReceive/>
                    </#if>
                    <#if quantityToReceive &gt; 0>
                        <#assign rowClass = "rowLightGreen"/>
                    <#else>
                        <#assign rowClass = tableRowClass(orderItemData_index)/>
                    </#if>
                    <tr class="${rowClass}" name="item" >
                        <td >
                            <div class="tabletext" >
                                ${orderItemData.orderItem.productId?default("N/A")}
                            </div>
                        </td>                        
                        <td >
                            <div class="tabletext">
                                ${(product.internalName)?if_exists}<br/>
                                <#assign upcaLookup = Static["org.ofbiz.base.util.UtilMisc"].toMap("productId", product.productId, "goodIdentificationTypeId", "UPCA")/>
                                <#assign upca = delegator.findByPrimaryKeyCache("GoodIdentification", upcaLookup)?if_exists/>
                                <#if upca?has_content>
                                    <br/>
                                    ${upca.idValue?if_exists}
                                </#if>
                            </div>
                        </td>
                        <td >
                            <div class="tabletext" >
                                ${orderItemData.descripcionRequisicion?if_exists}
                            </div>
                        </td>
                        <td>
                            <div class="tabletext" >
                                ${orderItemData.fechaEntrega?if_exists}
                            </div>
                        </td>
                        <td>
                            <div class="tabletext">
                                ${orderItem.quantity?default(0) - orderItem.cancelQuantity?default(0)}
                            </div>
                        </td>
                        <td  >
                            <div class="tabletext ${(backOrderedQuantity &gt; 0)?string(" errorMessage","")}">
                                ${backOrderedQuantity}
                            </div>
                        </td>
                        <td>
                            <div class="tabletext">${totalQuantityReceived}</div>
                        </td>
                        <td >
                            <div class="tabletext">
                            	<#assign openQuantity = orderItem.quantity - orderItem.cancelQuantity?default(0) - totalQuantityReceived />
                                ${openQuantity}
                            </div>
                            <#if (openQuantity > 0) ><#assign muestra ="true"> <#else><#assign muestra ="false"></#if>
                            <input type="hidden" id="muestra${orderItemData_index}" value="${muestra}"/>
                            <input type="hidden" id="orderItemDatasSize" value="${orderItemDatasSize}"/>
                            
                        </td>
                        <td >
                            <#--<div class="tabletext">${unitPrice}</div>-->
                            <input type="text" class='inputBox' size="9" name="unitCost_o_${orderItemData_index}" id="unitCost_o_${orderItemData_index}" value="${unitPrice}" onchange="rowClassChange(this, ${orderItemData_index})"/>
                        </td>
                        <td >
                            <div class="tabletext">
                            <#if polizaLiberacion?has_content>
                           		${montoComprometido}
                            <#elseif montoRestante?has_content>
                            	${montoRestante}
                            <#else>
                            	${orderItem.quantity * unitPrice}
                            </#if>  
                            </div>
                        </td>
                        <#--<td>
                            <div class="tabletext">
                                <#if fulfilledOrderIds?has_content>
                                    <#list fulfilledOrderIds?sort as fulfilledOrderId>
                                        ${fulfilledOrderId}&nbsp;[<a href="<@ofbizUrl>PackOrder?orderId=${fulfilledOrderId}</@ofbizUrl>">${uiLabelMap.WarehousePackOrder}</a>]<br/>
                                    </#list>
                                </#if>
                                &nbsp;
                            </div>
                        </td>-->
                        <#if itemsAvailableToReceive>
                          <#if availableToReceive gt 0>
                            <td >
                                <input type="hidden" name="orderItemSeqId_o_${orderItemData_index}" value="${orderItem.orderItemSeqId}"/>
                                <input type="hidden" name="productId_o_${orderItemData_index}" value="${(product.productId)?if_exists}"/>
                                <!--<input type="hidden" name="unitCost_o_${orderItemData_index}" value="${orderItem.unitPrice?default(0)}"/>-->
                                <input type="hidden" name="quantityRejected_o_${orderItemData_index}" value="0"/>
								<input type="hidden" name="clavePresupuestal${orderItemData_index}" value ="${orderItemData.clavePresupuestal?default(orderItemData_index)}" />
								<input type="hidden" name="inventoryItemTypeId_o_${orderItemData_index}" value ="NON_SERIAL_INV_ITEM" />
                                <input type="text" class='inputBox' size="5" name="quantityAccepted_o_${orderItemData_index}" id="quantityAccepted_o_${orderItemData_index}" value="${orderItemData.receiveDefQty?default(0)}" onchange="rowClassChange(this, ${orderItemData_index})"/>
                            </td>
							<#--
                            <td>              
                                <select name="inventoryItemTypeId_o_${orderItemData_index}" class="selectBox">
                                  <#list inventoryItemTypes as inventoryItemType>
                                  <option value="${inventoryItemType.inventoryItemTypeId}"
                                      <#if facility?exists && (facility.defaultInventoryItemTypeId?has_content) && (inventoryItemType.inventoryItemTypeId == facility.defaultInventoryItemTypeId)>
                                          selected="selected"
                                      </#if>    
                                  >${inventoryItemType.get("description",locale)?default(inventoryItemType.inventoryItemTypeId)}</option>
                                  </#list>
                                </select>
                            </td> -->
                          <#else>
                            <td colspan="2" >&nbsp;</td>
                          </#if>
                        </#if>
                        <#--<td>
                            <div class="tabletext">
                                <#if lotIds?has_content>
                                    <#list lotIds?sort as lotId>
                                        <a href="<@ofbizUrl>lotDetails?lotId=${lotId}</@ofbizUrl>">${lotId}</a><br/>
                                    </#list>
                                </#if>
                                <#if itemsAvailableToReceive && availableToReceive &gt; 0 >
                                    <input type="text" class='inputBox' size="10" name="lotId_o_${orderItemData_index}" id="lotId_o_${orderItemData_index}" value=""/>
                                    <a href="javascript:call_fieldlookup2(document.selectAllForm.lotId_o_${orderItemData_index},'createLotPopupForm');" class="buttontext">${uiLabelMap.CommonNew}</a>
                                </#if>
                            </div>
                        </td>-->
                        <td align="center" >
							<input type="text" class='inputBox' size="9" name="montoAmpliar_o_${orderItemData_index}" id="montoAmpliar_o_${orderItemData_index}" value="${orderItemData.montoAmpliar?if_exists}" onchange="rowClassChange(this, ${orderItemData_index})"/>                        
						</td>
                        
                        <#if itemsAvailableToReceive>
                            <td align="right" >
                              <#if availableToReceive gt 0>
                              	<#if shipmentReceiptTemp2?exists && rowSubmit!=''>
                                		<input type="checkbox" name="_rowSubmit_o_${orderItemData_index}" value="Y" onclick="javascript:checkToggle(this, 'selectAllForm');cambiarValorCheckBox(this);" checked/>
                                <#else>
                                		<input type="checkbox" name="_rowSubmit_o_${orderItemData_index}" value="Y" onclick="javascript:cambiarValorCheckBox(this);" />
                                </#if>
                              </#if>
                            </td>
                            <td align="right">
                              <#if availableToReceive gt 0>
                              	<#if shipmentReceiptTemp2?exists && rowDeductiva !=''>
                                	<input type="checkbox" name="_rowDeductiva_o_${orderItemData_index}" value="Y" onclick="javascript:cambiarValorCheckBox(this);" checked/>
                                <#else>
                                	<input type="checkbox" name="_rowDeductiva_o_${orderItemData_index}" value="Y" onclick="javascript:cambiarValorCheckBox(this);" />
                                </#if>
                              </#if>
                            </td>
                        </#if>
                    </tr>
                    <#if tagTypes?has_content>
                    <tr>
              			<td colspan="14">
              				<@flexArea targetId="ClavePresupuestal${orderItem.orderItemSeqId}" title=uiLabelMap.ClavePresupuestal controlStyle="font-weight: bold;">
              				<table  border="0" cellpadding="1" cellspacing="0">
								<@clavesPresupDisplay tags=tagTypes entity=orderItem partyExternal=orderItem.acctgTagEnumIdAdmin/>
							</table>
							</@flexArea>
						</td>
			  		</tr>
					<tr></tr><tr></tr><tr></tr>
			  		</#if>
                </#list>
			</tbody>
            </table>
            <#if itemsAvailableToReceive>
                <div style="width:100%; text-align:right; margin-top: 15px">
                    <@inputConfirm title=uiLabelMap.CommonClearAll href="ReceiveInventoryAgainstPurchaseOrder?purchaseOrderId=${orderId}&clearAll=Y"/>
                </div>
            </#if>
            <#if itemsAvailableToReceive>
            	<div style="width:100%; text-align:right; margin-top: 15px">
                    <@inputConfirm title=uiLabelMap.DevengarParcial form="selectAllForm" onClick="document.selectAllForm.completePurchaseOrder.value='D'"/>
                </div>
                <div style="width:100%; text-align:right; margin-top: 15px">
                    <@inputConfirm title=uiLabelMap.TraerPresupuesto form="selectAllForm" onClick="document.selectAllForm.completePurchaseOrder.value='P'"/>
                </div>
                <div style="width:100%; text-align:right; margin-top: 15px">
                    <@inputConfirm title=uiLabelMap.ReducirPresupuesto form="selectAllForm" onClick="document.selectAllForm.completePurchaseOrder.value='L'"/>
                </div>            
                    
                <div style="width:100%; text-align:right; margin-top: 10px">
                	<@display text=uiLabelMap.PurchReceiveItemsShipment+":" />
                    <select name="shipmentId" class="selectBox">
                        <option value="">${uiLabelMap.CommonNew}</option>
                        <#--
                        <#list shipments as shipment>
                          <option value="${shipment.shipmentId}" <#if primaryShipmentId?default("") == shipment.shipmentId>selected="selected"</#if>>${shipment.shipmentId}</option>
                        </#list> -->
                    </select>
                    <a class="smallSubmit" href="javascript:document.selectAllForm.completePurchaseOrder.value='N';document.selectAllForm.submit();">${uiLabelMap.WarehouseReceiveAndKeepOpen}</a>
                </div>
                <div style="width:100%; text-align:right; margin-top: 15px">
                    <@inputConfirmWarehouse title=uiLabelMap.WarehouseReceiveAndClosePO form="selectAllForm" onClick="document.selectAllForm.completePurchaseOrder.value='Y'"/>
                </div>
                <div style="width:100%; text-align:right; margin-top: 15px">
                    <@inputConfirm title=uiLabelMap.WarehouseRecision form="selectAllForm" onClick="document.selectAllForm.completePurchaseOrder.value='R'"/>
                </div>
                <div style="width:100%; text-align:right; margin-top: 15px">
                    <@inputConfirm title=uiLabelMap.WarehouseTerminacion form="selectAllForm" onClick="document.selectAllForm.completePurchaseOrder.value='T'"/>
                </div>
                <div style="width:100%; text-align:right; margin-top: 15px">
                    <@inputConfirm title="Guardar" form="selectAllForm" onClick="document.selectAllForm.completePurchaseOrder.value='G'"/>
                </div>
            </#if>
        </form>
        <#--<script language="JavaScript" type="text/javascript">selectAll('selectAllForm');</script>-->
    </#if>
    <#if itemsAvailableToReceive && totalReadyToReceive < totalAvailableToReceive && "FALSE" == "TRUE">
        <div class="head3">${uiLabelMap.ProductReceiveInventoryAddProductToReceive}</div>
        <form name="addProductToReceive" method="post" action="<@ofbizUrl>ReceiveInventoryAgainstPurchaseOrder</@ofbizUrl>">
            <input type="hidden" name="purchaseOrderId" value="${orderId}"/>
            <div class="tabletext">
                <span class="tabletext">
                    ${uiLabelMap.ProductProductId}/${uiLabelMap.ProductGoodIdentification}/${uiLabelMap.FormFieldTitle_supplierProductId} <input type="text" class="inputBox" size="20" id="productId" name="productId" value=""/>
                    @
                    <input type="text" class="inputBox" name="quantity" size="6" maxlength="6" value="1" tabindex="0"/>
                    <input type="submit" value="${uiLabelMap.CommonAdd}" class="smallSubmit"/>
                </span>
            </div>
        </form>
        <script language="javascript">
            document.getElementById('productId').focus();
        </script>
    </#if>
</#if>
<script type="text/javascript">
	var acctgTransTypeId = document.getElementById('acctgTransTypeId').value;
    opentaps.addOnLoad(cambiaEvento(acctgTransTypeId,'11'));
</script>
