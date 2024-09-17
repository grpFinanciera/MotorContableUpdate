<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<@paginate name="listServiciosPersConsulta" list=servPerListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
		<#assign hostname = request.getServerName() /></br>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title="Id Servicio personal" orderBy="servicioPersonalId"/>
                <@headerCell title=uiLabelMap.PurchStatus orderBy="statusId"/>
                <@headerCell title=uiLabelMap.FechaCreacion orderBy="createdStamp"/>
                <@headerCell title=uiLabelMap.PurchFecha orderBy="fechaContable"/>
                <@headerCell title=uiLabelMap.PurchSolicitante orderBy="nombre"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.servicioPersonalId href="verServiciosPersonales?servicioPersonalId=${row.servicioPersonalId}"/>
                <@displayCell text=row.statusId/>
                 <@displayCell text=row.createdStamp/>
                <@displayCell text=row.fechaContable/>
                <@displayCell text=row.nombre+" "+row.apellidos/>
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>
