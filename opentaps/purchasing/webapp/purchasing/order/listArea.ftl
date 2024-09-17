<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<@paginate name="listArea" list=areaListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.PurchArea orderBy="areaId"/>
                <@headerCell title=uiLabelMap.PurchTipoWorkflow orderBy="tipoWorkFlowId"/>
                <@headerCell title=uiLabelMap.PurchasingNombreArea orderBy="groupNameLocal"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.areaId href="addApprovers?areaId=${row.areaId}&amp;organizationPartyId=${row.organizationPartyId}&amp;tipoWorkFlowId=${row.tipoWorkFlowId}"/>
                <@displayCell text=row.tipoWorkFlowId/>
                <@displayCell text=row.groupNameLocal/>
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>
