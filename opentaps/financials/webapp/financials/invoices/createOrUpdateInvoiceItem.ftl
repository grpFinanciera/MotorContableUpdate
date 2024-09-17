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

<#if hasUpdatePermission>
           
<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<#if invoiceItem?exists><#assign formAction = "updateInvoiceItem"><#else><#assign formAction = "createInvoiceItem"></#if>

<form method="post" action="<@ofbizUrl>${formAction}</@ofbizUrl>" name="${formAction}">
  <@inputHidden name="invoiceId" value="${invoice.invoiceId}"/>
  <@inputHidden name="validateAccountingTags" value="False"/>

<#if invoiceItem?exists>
  <@inputHidden name="invoiceItemSeqId" value="${invoiceItem.invoiceItemSeqId}"/>
  
  <div class="screenlet">
    <div class="screenlet-header">
      <div class="boxhead">
        Update Invoice #<a href="<@ofbizUrl>viewInvoice?invoiceId=${invoice.invoiceId}</@ofbizUrl>" class="buttontext">${invoice.invoiceId}</a> ${uiLabelMap.FinancialsInvoiceItemSeqId} ${invoiceItem.invoiceItemSeqId}
      </div>
    </div>
    <div class="screenlet-body">
      <table border="0" width="100%">
        <#--<@inputSelectRow name="invoiceItemTypeId" title=uiLabelMap.CommonType list=invoiceItemTypes displayField="description" default=invoiceItem.invoiceItemTypeId />-->
                
        <@inputTextRow name="description" title=uiLabelMap.CommonDescription size="60" default=invoiceItem.description />
        <!-- <tr> -->
          <#-- <@displayTitleCell title=uiLabelMap.FinancialsOverrideGlAccount /> -->
          <!-- <td >--><#--<@inputAutoCompleteGlAccount name="overrideGlAccountId" id="overrideGlAccountId" default=invoiceItem.overrideGlAccountId/></td>-->
          <!--</tr>-->
          <@inputAutoCompleteProductRow name="productId" title=uiLabelMap.ProductProductId default=invoiceItem.productId />
          <@displayCell text=uiLabelMap.CommonQuantity blockClass="titleCell" blockStyle="width: 200px" class="tableheadtext"/>
          <@inputTextCell name="quantity" size=4 default=invoiceItem.quantity />
          <@displayCell text=uiLabelMap.CommonAmount blockClass="titleCell" blockStyle="width: 100px" class="tableheadtext"/>
          <@inputCurrencyCell name="amount" currencyName="uomId" default=invoiceItem.amount defaultCurrencyUomId=parameters.orgCurrencyUomId disableCurrencySelect=true/>
		  <tr>
		  <#--
          <@displayCell text=uiLabelMap.FinancialsIsTaxable blockClass="titleCell" blockStyle="width: 200px" class="tableheadtext"/>
          <@inputIndicatorCell name="taxableFlag" default=invoiceItem.taxableFlag />
          </tr>
          <tr>
          <@displayCell text=uiLabelMap.AccountingTaxAuthority blockClass="titleCell" blockStyle="width: 100px" class="tableheadtext"/>
          <@inputSelectTaxAuthorityCell list=taxAuthorities required=false defaultGeoId=invoiceItem.taxAuthGeoId defaultPartyId=invoiceItem.taxAuthPartyId />
          </tr> -->
		<#--
        <#if tagTypes?has_content>
          <@accountingTagsSelectRows tags=tagTypes prefix="acctgTagEnumId" entity=invoiceItem />
        </#if>-->
	    </tr>
        <@displayCvesPresupRow tagTypes=tagTypes />
        <@inputSubmitRow title=uiLabelMap.CommonUpdate />
      </table>
    </div>
  </div>
<#else>
  <div class="screenlet">
    <div class="screenlet-header"><div class="boxhead">${uiLabelMap.FinancialsNewInvoiceItem}</div></div>
    <div class="screenlet-body">
      <table border="0"  width="100%">
        	<#--<@inputSelectRow name="invoiceItemTypeId" title=uiLabelMap.CommonType list=invoiceItemTypes displayField="description" required=false />-->
        	<@displayCell text=uiLabelMap.CommonDescription blockClass="titleCell" class="tableheadtext" />
	        <@inputTextCell name="description" size="30" />
	        <!--<tr>-->
	          <#-- <@displayTitleCell title=uiLabelMap.FinancialsOverrideGlAccount /> -->
	          <!--<td >--><#-- <@inputAutoCompleteGlAccount name="overrideGlAccountId" id="overrideGlAccountId" default=glAccountId/></td> -->
	        <!--</tr>-->
	        <@displayCell text=uiLabelMap.ProductProductId blockClass="titleCell" class="tableheadtext" />
	        <@inputAutoCompleteProductCell name="productId"/>
	        
	        <@displayCell text=uiLabelMap.CommonQuantity blockClass="titleCell" class="tableheadtext" />
	        <@inputTextCell name="quantity" size=10 />
	          
	        <@displayCell text=uiLabelMap.CommonAmount blockClass="titleCell" class="tableheadtext" />
	        <@inputCurrencyCell name="amount" currencyName="uomId" defaultCurrencyUomId=parameters.orgCurrencyUomId disableCurrencySelect=true/>
	          
	          <#--
	          <tr>
	          <@displayCell text=uiLabelMap.FinancialsIsTaxable blockClass="titleCell" blockStyle="width: 200px" class="tableheadtext"/>
	          <@inputIndicatorCell name="taxableFlag" default="N"/>
	          <tr>
	          </tr>
	          <@displayCell text=uiLabelMap.AccountingTaxAuthority blockClass="titleCell" blockStyle="width: 100px" class="tableheadtext" />
	          <@inputSelectTaxAuthorityCell list=taxAuthorities required=false/>
	          </tr>-->
	          <tr class="${tableRowClass(0)}" name="item" >
	    	</tr>      
		</table>
		<table>
		<#--
        <#if !disableTags?exists && tagTypes?has_content>
          <@accountingTagsSelectRows tags=tagTypes prefix="acctgTagEnumId" entity=lastItem! />
        </#if>-->
        <br>
        <br>
        <@displayCvesPresupRow tagTypes=tagTypes titleClass="requiredField"/>
        <@inputSubmitRow title=uiLabelMap.CommonAdd/>
      </table>
    </div>
  </div>
</#if>

</form>

<#else>
<#-- TODO The error here depends on what happened:  either invoice, invoiceItem or hasUpdatePermission was missing/false -->
</#if>
