<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<@paginate name="listViatico" list=viaticoListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
		<#assign hostname = request.getServerName() /></br>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.ViaticoId orderBy="createdStamp"/>
                <@headerCell title=uiLabelMap.PurchStatus orderBy="descripcion"/>
                <@headerCell title=uiLabelMap.FechaCreacion orderBy="createdStamp"/>
                <@headerCell title=uiLabelMap.PurchFecha orderBy="fechaAutorizacion"/>
                <@headerCell title=uiLabelMap.PurchSolicitante orderBy="personaNameDesc"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.viaticoId href="verViatico?viaticoId=${row.viaticoId}"/>
                <@displayCell text=row.descripcion/>
                 <@displayCell text=row.createdStamp/>
                <@displayCell text=row.fechaAutorizacion/>
                <@displayCell text=row.personaNameDesc/>
				<@displayLinkCell text=uiLabelMap.VerSolicitudViatico href="imprimirViaticoPDF?viaticoId=${row.viaticoId}"/>
				
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>
