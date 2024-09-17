<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@paginate name="listTransferencia" list=listTransferencia rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.AlmacenSolicitudTransferenciaId orderBy="solicitudTransferenciaId"/>
                <@headerCell title=uiLabelMap.WarehouseStatus orderBy="descripcion"/>
                <@headerCell title=uiLabelMap.WarehouseFecha orderBy="fechaAutorizacion"/>
                <@headerCell title=uiLabelMap.WarehouseSolicitante orderBy="personaNameDesc"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.solicitudTransferenciaId href="verSolicitudTransferencia?solicitudTransferenciaId=${row.solicitudTransferenciaId}"/>
                <@displayCell text=row.descripcion! />
                <@displayDateCell date=row.fechaAutorizacion! />
                <@displayCell text=row.personaNameDesc! />
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>
