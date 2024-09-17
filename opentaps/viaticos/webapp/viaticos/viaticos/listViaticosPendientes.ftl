<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<@paginate name="listViaticosPendientes" list=viaticoPendienteListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.ViaticoId orderBy="createdStamp"/>
                <@headerCell title=uiLabelMap.PurchDescripcion orderBy="description"/>
                <@headerCell title=uiLabelMap.PurchNumeroEmpleado orderBy="personaSolicitanteId"/>
                <@headerCell title=uiLabelMap.PurchSolicitante orderBy="firstName"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.viaticoId href="verViatico?viaticoId=${row.viaticoId}"/>
                <@displayCell text=row.descripcion/>
                <@displayCell text=row.personaSolicitanteId/>
                <@displayCell text=row.firstName+" "+row.lastName/>                                               
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>