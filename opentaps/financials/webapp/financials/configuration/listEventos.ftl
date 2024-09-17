<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<@paginate name="listEventos" list=EventoContableListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.FinancialsDocumentoId orderBy="acctgTransTypeId"/>
                <@headerCell title=uiLabelMap.FinancialsDescripcionDoc orderBy="descripcion"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.acctgTransTypeId href="VerEvento?acctgTransTypeId=${row.acctgTransTypeId}"/>
                <@displayCell text=row.descripcion/>
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>
