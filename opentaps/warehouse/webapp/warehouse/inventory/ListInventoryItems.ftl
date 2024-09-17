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

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<#assign hostname = request.getServerName() />
<@frameSectionHeader title=uiLabelMap.ProductInventoryItems />
<@paginate name="listMapInventoryItem" list=listMapInventoryItem rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.ProductInventoryItemId orderBy="inventoryItemId"/>
				<@headerCell title=uiLabelMap.ProductProductId orderBy="productId"/>
				<@headerCell title=uiLabelMap.ProductInternalName orderBy="internalName"/>		
				<@headerCell title=uiLabelMap.ProductLocation orderBy="locationSeqId"/>
				<@headerCell title=uiLabelMap.ProductLotId orderBy="lotId"/>
				<@headerCell title=uiLabelMap.ProductDateReceived orderBy="datetimeReceived"/>	
				<@headerCell title=uiLabelMap.ProductBrandName orderBy="brandName"/>	
				<@headerCell title=uiLabelMap.ProductModelo orderBy="modelo"/>
				<@headerCell title=uiLabelMap.ProductUnidadMedida orderBy="abbreviation"/>
     			<@headerCell title=uiLabelMap.WarehouseQuantityATPQOH orderBy="availableToPromiseTotal"/>		
     			<@headerCell title=uiLabelMap.PurchasingPrecioImpuestos orderBy="unitCost"/>		
     			<@headerCell title=uiLabelMap.PurchasingPrecioTotalUnitarioSinIva orderBy="unitCost"/>
				<@headerCell title=uiLabelMap.PurchasingPrecioTotalUnitario orderBy="unitCost"/>				
				<@headerCell title=uiLabelMap.ProductProductQOH orderBy="QOHTSUM"/>				
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">	
            	<@displayLinkCell href="EditInventoryItem?inventoryItemId=${row.inventoryItemId}" text=row.inventoryItemId! />
				<@displayLinkCell href="findInventoryItem?productId=${row.productId}&amp;performFind=Y" text=row.productId! /> 
				<@displayCell text=row.internalName! />
				<@displayLinkCell href="findInventoryItem?locationSeqId=${row.locationSeqId!}&amp;performFind=Y" text=row.locationSeqId! /> 	
				<@displayCell text=row.lotId! />
				<@displayDateCell date=row.datetimeReceived! />
				<@displayCell text=row.brandName! />
				<@displayCell text=row.modelo! />
				<@displayCell text=row.abbreviation! />
				<@displayCell text=row.availableToPromiseTotal!0 />
				<@displayCurrencyCell  amount=row.unitCost currencyUomId=row.currencyUomId />
          		<#assign valorU = row.unitCost * row.QOHTSUM />
          		<#assign valorUSin = valorU - (valorU*0.16) />
          		<@displayCurrencyCell  amount=valorUSin!0 currencyUomId=row.currencyUomId />
          		<@displayCurrencyCell  amount=valorU!0 currencyUomId=row.currencyUomId />
          		<@displayCell text=row.QOHTSUM!0 />
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>
