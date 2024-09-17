<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@paginate name="proyectoListBuilder" list=proyectoListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <#assign hostname = request.getServerName() /></br>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title="Id proyecto" orderBy="proyectoId"/>
				<@headerCell title="Codigo proyecto" orderBy="codigoProyecto"/>
                <@headerCell title="Fecha" orderBy="fecha"/>
                <@headerCell title="Nombre proyecto" orderBy="nombreProyecto"/>
                <@headerCell title=uiLabelMap.PurchasingStatus orderBy="statusId"/>
                <@headerCell title=uiLabelMap.PurchasingMonto orderBy="monto"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.proyectoId href="verProyecto?proyectoId=${row.proyectoId}"/>
				<@displayCell text=row.codigoProyecto/>
                <@displayDateCell date=row.fecha format="DATE_TIME"/>
                <@displayCell text=row.nombreProyecto/>
                <@displayCell text=row.statusId/>
                <@displayCurrencyCell amount=row.monto currencyUomId=row.tipoMoneda class="textright"/>
            </#list>
        </table>
    </#noparse>
</@paginate>
