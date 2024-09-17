<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<@paginate name="listRequisicionPendiente" list=solicitudPendienteListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.PurchRequisicionId orderBy="requisicionId"/>
                <@headerCell title=uiLabelMap.PurchDescripcion orderBy="description"/>
                <@headerCell title=uiLabelMap.PurchasingMontoEstimadoImpuestos orderBy="montoTotal"/>
                <@headerCell title=uiLabelMap.PurchIdPersonaSolicitante orderBy="personaSolicitanteId"/>
                <@headerCell title=uiLabelMap.PurchSolicitante orderBy="firstName"/>                
                <@headerCell title=uiLabelMap.PurchIdAreaSolicitante orderBy="areaPartyId"/>
                <@headerCell title=uiLabelMap.PurchAreaSolicitante orderBy="groupName"/>
                <@headerCell title=uiLabelMap.PurchTipoWorkflow orderBy="descripcionWorkflow"/>                                
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.requisicionId href="verRequisicion?requisicionId=${row.requisicionId}"/>
                <@displayCell text=row.descripcion/>
                <@displayCurrencyCell amount=row.montoTotal currencyUomId=row.tipoMoneda/>
                <@displayCell text=row.personaSolicitanteId/>
                <@displayCell text=row.firstName+" "+row.lastName/>
                <@displayCell text=row.areaPartyId/>
                <@displayCell text=row.groupName/>
                <@displayCell text=row.descripcionWorkflow/>
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>