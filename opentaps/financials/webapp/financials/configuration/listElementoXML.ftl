
<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<@paginate name="listElementoXML" list=elementoListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.FinancialsReporteElemento orderBy="elementoReporteId"/>
                <@headerCell title=uiLabelMap.FinancialsReporte orderBy="tipoReporteId"/>
                <@headerCell title=uiLabelMap.Cuenta orderBy="glAccountId"/>
				<@headerCell title=uiLabelMap.accountName orderBy="nombreCuenta"/>
                <@headerCell title=uiLabelMap.classification orderBy="enumId"/>
				<@headerCell title=uiLabelMap.classification orderBy="nombreClasificacion"/>
            </tr>
            
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayCell text=row.elementoReporteId />
                <@displayCell text=row.tipoReporteId />
                <@displayCell text=row.glAccountId />
                <@displayCell text=row.nombreCuenta />
                <@displayCell text=row.enumId />
                <@displayCell text=row.nombreClasificacion />
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>