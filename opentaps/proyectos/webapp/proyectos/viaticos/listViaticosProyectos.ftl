<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<@paginate name="listViaticoProyecto" list=viaticoProyectoListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
		<#assign hostname = request.getServerName() /></br>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.ViaticoId orderBy="viaticoProyectoId"/>
                <@headerCell title=uiLabelMap.PurchStatus orderBy="statusId"/>
                <@headerCell title=uiLabelMap.FechaCreacion orderBy="createdStamp"/>
                <@headerCell title=uiLabelMap.PurchFecha orderBy="fechaAutorizacion"/>
                <@headerCell title=uiLabelMap.PurchSolicitante orderBy="nombre"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.viaticoProyectoId href="comprobarViatico?viaticoProyectoId=${row.viaticoProyectoId}"/>
                <@displayCell text=row.statusId/>
                 <@displayCell text=row.createdStamp/>
                <@displayCell text=row.fechaInicial/>
                <@displayCell text=row.nombre+" "+row.apellidos/>
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>
