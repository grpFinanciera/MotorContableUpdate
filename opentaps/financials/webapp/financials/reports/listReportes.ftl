<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<@paginate name="workFlowList" list=workFlowListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.FinancialsWorkFlowId orderBy="workFlowId"/>
                <@headerCell title=uiLabelMap.ReporteId orderBy="reporteId"/>
                <@headerCell title=uiLabelMap.ReporteNombre orderBy="reporteNombre"/>
                <@headerCell title=uiLabelMap.FinancialsFechaEmision orderBy="fechaEmision"/>
                <@headerCell title=uiLabelMap.Estatus orderBy="statusId	"/>
                <@headerCell title=uiLabelMap.DescEstatus orderBy="descripcion"/>
                <@headerCell title=uiLabelMap.PdfOriginal orderBy="workFlowId"/>
                <@headerCell title=uiLabelMap.PdfFirmado orderBy="workFlowId"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayCell text=row.workFlowId/>
                <@displayCell text=row.reporteId/>
                <@displayCell text=row.reporteNombre/>
                <@displayCell text=row.fechaEmision/>
                <@displayCell text=row.statusId/>
                <@displayCell text=row.descripcion/>
                <td>
                	<a href="<@ofbizUrl>imprimePdf?workFlowId=${row.workFlowId?if_exists}&amp;campoPDF=pdfOriginal</@ofbizUrl>">PDF Original</a>
                </td>
                <#if row.statusId=="FIRMADO">
                	<td>
                		<a href="<@ofbizUrl>imprimePdf?workFlowId=${row.workFlowId?if_exists}&amp;campoPDF=pdfFirmado</@ofbizUrl>">PDF Firmado</a>
                	</td>
                </#if>	
            </tr>    
            </#list>
        </table>
    </#noparse>
</@paginate>
