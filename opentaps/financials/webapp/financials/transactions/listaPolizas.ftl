<#--
 * Lista de resultados preliminares de p�lizas contables
 * Author: Vidal Garc�a
 * Versi�n 1.0
 * Fecha de Creaci�n: Julio 2013
-->

<#-- Parametrized find form for transactions. -->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<#assign hostname = request.getServerName() />
<@paginate name="paginaAcctgTrans" list=acctgTransListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.FormFieldTitle_numeroPoliza orderBy="poliza"/>     
                <@headerCell title=uiLabelMap.FinancialsTipoPoliza orderBy="descripcionPoliza"/>           
                <@headerCell title=uiLabelMap.AccountingTransactionType orderBy="descripcionEvento"/>
                <@headerCell title=uiLabelMap.CommonDescription orderBy="description"/>
                <@headerCell title=uiLabelMap.ProductOrganization orderBy="descripcionOrg"/>
                <@headerCell title=uiLabelMap.FinancialsFechaContable orderBy="postedDate"/>
                <@headerCell title=uiLabelMap.PurchArea orderBy="nombreArea"/>
                <@headerCell title=uiLabelMap.CommonCreatedBy orderBy="createdByUserLogin"/>
                <@headerCell title=uiLabelMap.CommonAmount orderBy="postedAmount"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.poliza href="viewAcctgTrans?acctgTransId=${row.acctgTransId}" />
                <@displayCell text=row.descripcionPoliza/>				
                <@displayCell text=row.descripcionEvento/>
                <@displayCell text=row.description/>
                <@displayCell text=row.descripcionOrg/>
                <@displayCell text=row.postedDate/>
                <@displayCell text=row.nombreArea/>
                <@displayCell text=row.createdByUserLogin/>                
                <@displayCurrencyCell amount=row.postedAmount currencyUomId=row.currencyUomId class="textright"/>
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>