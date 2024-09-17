<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<@paginate name="listAdquisicionConsulta" list=adquisicionesProyectoListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
		<#assign hostname = request.getServerName() /></br>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title="Id Adquisicion" orderBy="adqProyectoId"/>
                <@headerCell title=uiLabelMap.PurchStatus orderBy="statusId"/>
                <@headerCell title=uiLabelMap.FechaCreacion orderBy="createdStamp"/>
                <@headerCell title=uiLabelMap.PurchFecha orderBy="fechaContable"/>
                <@headerCell title="Monto Total" orderBy="montoTotal"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.adqProyectoId href="verAdquisicionProyecto?adqProyectoId=${row.adqProyectoId}"/>
                <@displayCell text=row.statusId/>
                 <@displayCell text=row.createdStamp/>
                <@displayCell text=row.fechaContable/>
                <@displayCell text=row.montoTotal/>
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>