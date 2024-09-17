<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#assign LinkHacerDevolucion><@submitFormLink form="crearDevolucion" text=uiLabelMap.CommonEnviar /></#assign>

<#assign productIdList = "" />

<form name="crearDevolucion" method="post" action="<@ofbizUrl>crearDevolucion</@ofbizUrl>">
<#if listDevolucionRecepcionItems?has_content>
<@frameSection title=uiLabelMap.WarehouseHacerDevolucion extra=LinkHacerDevolucion>
<tr>             
        <@displayTitleCell title=uiLabelMap.FinancialsAccountigDate titleClass="requiredField" />
        <@inputDateTimeCell name="fechaContable" default=Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp() />
        </tr>
      <tr>
  <table class="twoColumnForm">
    <tbody>          
        <@displayCell text=uiLabelMap.OrderItemSeqId class="tableheadtext"/>
        <@displayCell text=uiLabelMap.ShipmentId class="tableheadtext"/>
        <@displayCell text=uiLabelMap.RecepcionId class="tableheadtext"/>		
        <@displayCell text=uiLabelMap.ProductId class="tableheadtext"/>
        <@displayCell text=uiLabelMap.ProductName class="tableheadtext"/>                
        <@displayCell text=uiLabelMap.ShipBeforeDate class="tableheadtext"/>
        <@displayCell text=uiLabelMap.FechaRecepcion class="tableheadtext"/>
        <@displayCell text=uiLabelMap.RequisicionId class="tableheadtext"/>
		<@displayCell text=uiLabelMap.RecibidoPorUserLoginId class="tableheadtext"/>		        
        <@displayCell text=uiLabelMap.CantidadRecibida class="tableheadtext"/>   
        <@displayCell text=uiLabelMap.UnitPrice class="tableheadtext"/>             															                   
        <@displayCell text=uiLabelMap.Total class="tableheadtext"/>                        
		<@displayCell text=uiLabelMap.CantidadDevolucion class="tableheadtext"/>
		<td></td>          
      </tr> 
      <#assign count = 1 />     
      <input type="hidden" name="moneda" id="moneda" value="${listDevolucionRecepcionItems.get(0).currencyUom!'MXN'}"/>
      <input type="hidden" name='organizationPartyId' value="${organizationPartyId?if_exists}" />      
      <#list listDevolucionRecepcionItems as row>
        <tr class="${tableRowClass(row_index)}">
        	<input type="hidden" name="orderId${count}" id="orderId${count}" value="${row.orderId?if_exists}"/>
		    <input type="hidden" name="orderItemSeqId${count}" id="orderItemSeqId${count}" value="${row.orderItemSeqId?if_exists}"/>      
		    <input type="hidden" name="productId${count}" id="productId${count}" value="${row.productId?if_exists}"/>
		    <input type="hidden" name="quantityAccepted${count}" id="quantityAccepted${count}" value="${row.quantityAccepted?if_exists}"/>
		    <input type="hidden" name="unitPrice${count}" id="unitPrice${count}" value="${row.unitPrice?if_exists}"/>
		    <input type="hidden" name="shipmentId${count}" id="shipmentId${count}" value="${row.shipmentId?if_exists}"/>
		    <input type="hidden" name="shipmentReceiptId${count}" id="shipmentReceiptId${count}" value="${row.receiptId?if_exists}"/>        			    		            	        	        	       
            <@displayCell text=row.orderItemSeqId/>
            <@displayCell text=row.shipmentId/>
            <@displayCell text=row.receiptId/>                                    
	        <@displayCell text=row.productId/>
	        <@displayCell text=row.productName/>
	        <@displayDateCell date=row.shipBeforeDate format="DATE_ONLY" />
	        <@displayDateCell date=row.datetimeReceived format="DATE_ONLY" />
	        <@displayCell text=row.requisicionId/>
	        <@displayCell text=row.receivedByUserLoginId/>	        
	        <@displayCell text=row.quantityAccepted/>
	        <@displayCurrencyCell amount=row.unitPrice currencyUomId=row.currencyUom class="textright"/>
	        <@displayCurrencyCell amount=(row.unitPrice * row.quantityAccepted) currencyUomId=row.currencyUom class="textright"/>	        	       	        
	        <@inputTextCellCantidad name="cantidadDevolver${count}" id="cantidadDevolver${count}" maxlength=20 />	
	        <@inputCheckboxCell name="excepcion${count}" id="excepcion${count}" value="Y" default="N" />        	        	        
        </tr>
        <#assign count=count + 1/>
      </#list>               
    </tbody>
  </table>
</@frameSection>
</#if>
</form>


