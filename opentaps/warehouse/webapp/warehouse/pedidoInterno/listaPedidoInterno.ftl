<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<#assign hostname = request.getServerName() />
<@paginate name="paginaPedidoInterno" list=listBuilderPedidoInterno rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.WarehousePedidoInternoId orderBy="pedidoInternoId"/>     
                <@headerCell title=uiLabelMap.CommonDescription orderBy="descripcion"/>           
                <@headerCell title=uiLabelMap.WarehouseSolicitante orderBy="personaSolicitanteId"/>
                <@headerCell title=uiLabelMap.ProductOrganization orderBy="organizationPartyId"/>
                <@headerCell title=uiLabelMap.WarehouseStatus orderBy="statusId"/>
                <@headerCell title=uiLabelMap.PurchArea orderBy="areaPartyId"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.pedidoInternoId href="salidaPedidosInternos?pedidoInternoId=${row.pedidoInternoId}" />
                <@displayCell text=row.descripcion/>				
                <@displayCell text=row.personaSolicitanteId/>
                <@displayCell text=row.organizationPartyId/>
                <@displayCell text=row.statusId/>
                <@displayCell text=row.areaPartyId/>
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>