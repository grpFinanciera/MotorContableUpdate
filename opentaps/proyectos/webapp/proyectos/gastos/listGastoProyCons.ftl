<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<@paginate name="listGastoConsultaComp" list=gastoConsProyListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
		<#assign hostname = request.getServerName() /></br>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title="Id de Gasto" orderBy="gastoProyectoId"/>
                <@headerCell title=uiLabelMap.PurchStatus orderBy="statusId"/>
                <@headerCell title=uiLabelMap.FechaCreacion orderBy="createdStamp"/>
                <@headerCell title=uiLabelMap.PurchFecha orderBy="fecha"/>
                <@headerCell title=uiLabelMap.PurchSolicitante orderBy="nombre"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.gastoProyectoId href="verGastoProyecto?gastoProyectoId=${row.gastoProyectoId}"/>
                <@displayCell text=row.statusId/>
                 <@displayCell text=row.createdStamp/>
                <@displayCell text=row.fecha/>
                <@displayCell text=row.nombre+" "+row.apellidos/>
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>