<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@paginate name="listTransferenciasPendientes" list=listTransferenciasPendientes rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.AlmacenSolicitudTransferenciaId orderBy="solicitudTransferenciaId"/>
                <@headerCell title=uiLabelMap.CommonDescription orderBy="descripcion"/>
                <@headerCell title=uiLabelMap.WarehouseSolicitante orderBy="personaNameDesc"/>
                <@headerCell title=uiLabelMap.CommonStatus orderBy="descripcionEstatus"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.solicitudTransferenciaId href="verSolicitudTransferencia?solicitudTransferenciaId=${row.solicitudTransferenciaId}"/>
                <@displayCell text=row.descripcion! />
                <@displayDateCell date=row.personaNameDesc! />
                <@displayCell text=row.descripcionEstatus! />
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>
