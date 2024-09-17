<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<@paginate name="listRequisicion" list=requisicionListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <#assign hostname = request.getServerName() /></br>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.PurchRequisicionId orderBy="requisicionId"/>
                <@headerCell title=uiLabelMap.FinancialsAccountigDate orderBy="fechaContable"/>
                <@headerCell title=uiLabelMap.PurchDescripcion orderBy="descripcion"/>
                <@headerCell title=uiLabelMap.PurchasingStatus orderBy="statusId"/>
                <@headerCell title=uiLabelMap.PurchasingAuto orderBy="autorizador"/>
                <@headerCell title=uiLabelMap.PurchasingFecha orderBy="fechaAutorizada"/>
                <@headerCell title=uiLabelMap.PurchasingMonto orderBy="montoTotal"/>
                <@headerCell title=uiLabelMap.PurchasingSolicitante orderBy="personaNameDesc"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.requisicionId href="verRequisicion?requisicionId=${row.requisicionId}"/>
                <@displayDateCell date=row.fechaContable format="DATE_TIME"/>
                <@displayCell text=row.descripcion/>
                <@displayCell text=row.descripcionStatus/>
                <@displayCell text=row.autorizador/>
                <@displayDateCell date=row.fechaAutorizada format="DATE_TIME"/>
                <@displayCurrencyCell amount=row.montoTotal currencyUomId=row.tipoMoneda class="textright"/>
                <@displayCell text=row.personaNameDesc/>
				<@displayLinkCell text=uiLabelMap.VerSolicitudViatico href="imprimirRequisicionPDF?RequisicionId=${row.requisicionId}"/>
            </#list>
        </table>
    </#noparse>
</@paginate>