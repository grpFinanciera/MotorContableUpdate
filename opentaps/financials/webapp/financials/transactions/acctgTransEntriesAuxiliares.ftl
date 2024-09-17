<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#if acctgTransEntryAuxListBuilder?has_content>
<@frameSection title=uiLabelMap.FinancialsDetalleAuxiliar>
<@paginate name="listAuxiliares" list=acctgTransEntryAuxListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <#assign hostname = request.getServerName() /></br>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.FinancialsTipoAuxiliarId orderBy="identificadorAuxiliar"/>
                <@headerCell title=uiLabelMap.FinancialsTipoAuxiliar orderBy="tipoAuxiliar"/>
                <@headerCell title=uiLabelMap.FinancialsNombreAuxiliar orderBy="nombreAuxiliar"/>
                <@headerCell title=uiLabelMap.FinancialsGLAccountCode orderBy="glAccountId"/>
                <@headerCell title=uiLabelMap.FinancialsDebitCredit orderBy="debitCreditFlag"/>
                <@headerCell title=uiLabelMap.Monto orderBy="amount"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayCell text=row.identificadorAuxiliar/>
                <@displayCell text=row.tipoAuxiliar/>
                <@displayCell text=row.nombreAuxiliar/>
                <@displayCell text=row.glAccountId/>
                <@displayCell text=row.debitCreditFlag/>
                <@displayCurrencyCell amount=row.amount currencyUomId=row.currencyUomId class="textright"/>
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>
</@frameSection>
</#if>

