<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<@paginate name="listpedidosInternosPendientes" list=pedidosInternosPendientesListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.WarehousePedidoInternoId orderBy="requisicionId"/>
                <@headerCell title=uiLabelMap.WarehouseDescripcion orderBy="description"/>
                <@headerCell title=uiLabelMap.WarehousePersonaSolicitanteId orderBy="personaSolicitanteId"/>
                <@headerCell title=uiLabelMap.WarehouseSolicitante orderBy="firstName"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.pedidoInternoId href="verPedidoInterno?pedidoInternoId=${row.pedidoInternoId}"/>
                <@displayCell text=row.descripcion/>
                <@displayCell text=row.personaSolicitanteId/>
                <@displayCell text=row.firstName+" "+row.lastName/>                                               
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>