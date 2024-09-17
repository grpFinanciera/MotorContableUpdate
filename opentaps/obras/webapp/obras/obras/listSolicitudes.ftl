<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<@paginate name="listSolicitud" list=solicitudListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.PurchSolicitudId orderBy="obraId"/>
                <@headerCell title=uiLabelMap.PurchStatus orderBy="descripcion"/>
                <@headerCell title=uiLabelMap.PurchFecha orderBy="fechaAutorizacion"/>
                <@headerCell title=uiLabelMap.PurchSolicitante orderBy="personaNameDesc"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.obraId href="verSolicitud?obraId=${row.obraId}"/>
                <@displayCell text=row.descripcion/>
                <@displayCell text=row.fechaAutorizacion/>
                <@displayCell text=row.personaNameDesc/>
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>
