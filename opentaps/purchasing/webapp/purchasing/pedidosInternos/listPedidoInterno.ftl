<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<@paginate name="listPedidoInterno" list=pedidoInternoListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.WarehousePedidoInternoId orderBy="pedidoInternoId"/>
                <@headerCell title=uiLabelMap.WarehouseStatus orderBy="descripcion"/>
                <@headerCell title=uiLabelMap.WarehouseFecha orderBy="fechaAutorizada"/>
                <@headerCell title=uiLabelMap.WarehouseSolicitante orderBy="personaNameDesc"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.pedidoInternoId href="verPedidoInterno?pedidoInternoId=${row.pedidoInternoId}"/>
                <@displayCell text=row.descripcion/>
                <@displayCell text=row.fechaAtendida/>
                <@displayCell text=row.personaNameDesc/>
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>
