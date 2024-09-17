<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<#if listInventoryItem?has_content>
<#assign LinkTransferir><@submitFormLink form="transferirArticulosAlmacen" text=uiLabelMap.CommonTransferir /></#assign>
<form name="transferirArticulosAlmacen" method="post" action="<@ofbizUrl>transferirArticulosAlmacen</@ofbizUrl>">
<@frameSection title=uiLabelMap.TransferirArticulosEntreAlmacenes extra=LinkTransferir>  

  <table class="twoColumnForm">
    <tbody>
    <tr><td colspan="4" class="formSectionHeaderTitle">${uiLabelMap.AlmacenDestino}</td></tr>  	  
  	<tr>        
    	<@displayTitleCell title=uiLabelMap.AlmacenDestino />
        <@inputSelectCell name="facilityDestinoId" list=listaAlmacenes key="facilityId" required=false displayField="facilityName"/>
    </tr>
    
    <tr><td colspan="4" class="formSectionHeaderTitle">${uiLabelMap.ListaArticulos}</td></tr>
    	
      <tr>
      	<@displayCell text=uiLabelMap.CodigoArticuloInventario class="tableheadtext"/>                
        <@displayCell text=uiLabelMap.FacilityName class="tableheadtext"/>
        <@displayCell text=uiLabelMap.FacilityId class="tableheadtext"/>
        <#--<@displayCell text=uiLabelMap.partyId class="tableheadtext"/>
        <@displayCell text=uiLabelMap.nombreUsuraio class="tableheadtext"/>-->
        <@displayCell text=uiLabelMap.ProductId class="tableheadtext"/>
        <@displayCell text=uiLabelMap.InternalName class="tableheadtext"/>
        <@displayCell text=uiLabelMap.DatetimeReceived class="tableheadtext"/>        
        <@displayCell text=uiLabelMap.UnitCost class="tableheadtext"/>
        <@displayCell text=uiLabelMap.QuantityOnHandTotal class="tableheadtext"/>
        <@displayCell text=uiLabelMap.CantidadTransferir class="tableheadtext"/>        
        <td><div class="tableheadtext">${uiLabelMap.CommonTransferir}<input type="checkbox" name="selectAll" value="${uiLabelMap.CommonY}" onclick="javascript:toggleAll(this, 'transferirArticulosAlmacen');"></div>        </td>                               
      </tr>               
      <#list listInventoryItem as row>
        <tr class="${tableRowClass(row_index)}">        	
        	<input type="hidden" name="inventoryItemId${row_index}" id="inventoryItemId${row_index}" value="${row.inventoryItemId?if_exists}"/>
        	<input type="hidden" name="facilityId${row_index}" id="facilityId${row_index}" value="${row.facilityId?if_exists}"/>
        	<input type="hidden" name="unitCost${row_index}" id="unitCost${row_index}" value="${row.unitCost?if_exists}"/>
        	<input type="hidden" name="quantityOnHandTotal${row_index}" id="quantityOnHandTotal${row_index}" value="${row.quantityOnHandTotal?if_exists}"/>        	
        	<@displayCell text=row.inventoryItemId/>
            <@displayCell text=row.facilityName/>
            <@displayCell text=row.facilityId/>            
            <#--<@displayCell text=row.partyId/>
            <@displayCell text=row.nombreUsuraio/>-->
            <@displayCell text=row.productId/>
            <@displayCell text=row.internalName/>
            <@displayDateCell date=row.datetimeReceived format="DATE_ONLY" />
            <@displayCurrencyCell amount=row.unitCost currencyUomId=row.currencyUomId class="textright"/>
            <@displayCell text=row.quantityOnHandTotal/>            
            <@inputTextCell name="cantidadTransferir${row_index}" id="cantidadTransferir${row_index}" default=row.quantityOnHandTotal?if_exists />
            <td><input type="checkbox" name="_rowSubmit_o_${row_index}" value="Y" onclick="javascript:checkToggle(this, 'transferirArticulosAlmacen');"></td>                        
        </tr>
      </#list>   
    </tbody>
  </table>
</form>
<script language="JavaScript" type="text/javascript">selectAll('transferirArticulosAlmacen');</script>
</@frameSection>
</#if>



