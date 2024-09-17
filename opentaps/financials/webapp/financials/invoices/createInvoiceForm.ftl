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

<script language="JavaScript" type="text/javascript">
<!-- //
	function obtenDatosAgrupador() {
         new Ajax.Request('<@ofbizUrl>obtenDatosLookupAgrupador</@ofbizUrl>', {method:'get',
                asynchronous: false,
                onSuccess: function(transport) {
                    var data = transport.responseText.evalJSON(true);
                    document.getElementById('agrupadorl').href="javascript:call_fieldlookup2(document.createInvoiceForm.agrupador,'LookupAgrupador?momentoId1="+data.momentoId1+"&tipoPolizaId="+data.tipoPolizaId+"&claveContabilizada=N');";
                    //console.log('Success');
                },
	           onFailure: function(transport) {
	             var data = transport.responseText.evalJSON(true);
	             //console.log('Failure');
	           },parameters: $('acctgTransTypeId').serialize(), requestHeaders: {Accept: 'application/json'}
            });
	}
	
	// -->
</script>

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#if invoiceTypeId == "SALES_INVOICE">
  <#assign createInvoiceTitle = uiLabelMap.FinancialsCreateSalesInvoice />
  <#assign whichPartyTitle = uiLabelMap.AccountingToParty />
<#elseif invoiceTypeId == "PURCHASE_INVOICE">
  <#assign createInvoiceTitle = uiLabelMap.FinancialsCreatePurchaseInvoice />
  <#assign whichPartyTitle = uiLabelMap.AccountingFromParty />
<#elseif invoiceTypeId == "CUST_RTN_INVOICE">
  <#assign createInvoiceTitle = uiLabelMap.FinancialsCreateCustomerReturnInvoice />
  <#assign whichPartyTitle = uiLabelMap.AccountingFromParty />
<#elseif invoiceTypeId == "COMMISSION_INVOICE">
  <#assign createInvoiceTitle = uiLabelMap.FinancialsCreateCommissionInvoice />
  <#assign whichPartyTitle = uiLabelMap.AccountingFromParty />
</#if>
<#if isDisbursement>
  <#assign whichPartyId = "partyIdFrom">
  <#assign orgPartyId = "partyId">
<#else>
  <#assign whichPartyId = "partyId">
  <#assign orgPartyId = "partyIdFrom">
</#if>

<#if !momentoId1?exists>
	<#assign momentoId1 = "" />
</#if>
<#if !tipoPolizaId?exists>
	<#assign tipoPolizaId = "" />
</#if>

<#if parameters.paymentTypeId?exists>
	<#assign paymType = parameters.paymentTypeId/>
<#else>
	<#assign paymType = 'ELECT_TRANSF'/>
</#if>


<@frameSection title=createInvoiceTitle>
    <table border="0" cellpadding="2" cellspacing="0" width="100%">
      <form method="post" action="<@ofbizUrl>createInvoice</@ofbizUrl>" name="createInvoiceForm">
        <@inputHidden name="invoiceTypeId" value="${invoiceTypeId}"/>
        <@inputHidden name="statusId" value="INVOICE_IN_PROCESS"/>
        <@inputHidden name="${orgPartyId}" value="${parameters.organizationPartyId}" />
        <#if parameters.oldRefNum?exists><@inputHidden name="oldRefNum" value=parameters.oldRefNum /></#if>
        <@inputAutoCompletePartyRow titleClass="requiredField" title=whichPartyTitle name=whichPartyId id="autoCompleteWhichPartyId" size="20"/>
        <@inputSelectRow titleClass="requiredField" title=uiLabelMap.AccountingTipoDocumento name="acctgTransTypeId" list=tiposDocumento?sort_by("descripcion") id="acctgTransTypeId" required=false key="acctgTransTypeId" displayField="descripcion" onChange="javascript:obtenDatosAgrupador();" />
        <@inputCurrencySelectRow name="currencyUomId" title=uiLabelMap.CommonCurrency defaultCurrencyUomId=parameters.orgCurrencyUomId />
        <@inputDateTimeRow name="invoiceDate" title=uiLabelMap.FinancialsAccountigDate form="createInvoiceForm" default=Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp() />
        <#--<@inputDateTimeRow name="dueDate" title=uiLabelMap.AccountingDueDate form="createInvoiceForm" />-->
        <@inputTextRow name="referenceNumber" title=uiLabelMap.FinancialsReferenciaFactura size=60 />
        <@inputSelectRow title=uiLabelMap.AccountingPaymentType name="paymentTypeId" ignoreParameters=true list=tipoPago! key="paymentTypeId" displayField="description" default=paymType/>
        <@inputTextRow name="description" title=uiLabelMap.CommonDescription size=60 />
        <@inputTextareaRow name="invoiceMessage" title=uiLabelMap.CommonMessage />
        <#--
        <#if isReceipt >
        	<@inputHidden name="agrupador" value="�"/>
       	<#else>
        	<@inputLookupRow title=uiLabelMap.NumeroDePoliza name="agrupador" form="createInvoiceForm" lookup="LookupAgrupador?momentoId1=${momentoId1}&tipoPolizaId=${tipoPolizaId}&claveContabilizada=N"/>
        </#if>-->
        <@inputForceCompleteRow title=uiLabelMap.CommonCreate forceTitle=uiLabelMap.OpentapsForceCreate form="createInvoiceForm" />
      </form>
    </table>
</@frameSection>
