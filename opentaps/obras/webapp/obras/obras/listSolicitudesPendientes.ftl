<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<@paginate name="listSolicitudesPendientes" list=solicitudPendienteListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.ObraObraId orderBy="obraId"/>
                <@headerCell title=uiLabelMap.ObraDescripcion orderBy="description"/>
                <@headerCell title=uiLabelMap.ObraPersonaSolicitanteId orderBy="personaSolicitanteId"/>
                <@headerCell title=uiLabelMap.ObraSolicitante orderBy="firstName"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.obraId href="verSolicitud?obraId=${row.obraId}"/>
                <@displayCell text=row.descripcion/>
                <@displayCell text=row.personaSolicitanteId/>
                <@displayCell text=row.firstName+" "+row.lastName/>                                               
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>