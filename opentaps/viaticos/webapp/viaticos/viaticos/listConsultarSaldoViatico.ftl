<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<@paginate name="listConsultarSaldoViatico" list=listConsultarSaldoViaticoListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.PurchCatalogoId orderBy="catalogoId"/>
                <@headerCell title=uiLabelMap.PurchSolicitante orderBy="firstName"/>
                <@headerCell title=uiLabelMap.PurchPeriodo orderBy="periodo"/>
                <@headerCell title=uiLabelMap.PurchSaldo orderBy="monto"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayCell text=row.catalogoId />
                <@displayCell text=row.firstName+" "+row.lastName/>
                <@displayCell text=row.periodo/>
                <@displayCell text=row.monto/>                                                               
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>