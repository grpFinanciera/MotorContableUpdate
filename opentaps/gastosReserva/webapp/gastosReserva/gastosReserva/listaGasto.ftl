<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@paginate name="listaGasto" list=gastoReservaListBuilder rememberPage=false requestGasto=requestGasto >
    <#noparse>
        <@navigationHeader/>
        <#assign hostname = request.getServerName() /><br/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.GastoReservadoId orderBy="gastosReservaId"/>
                <@headerCell title=uiLabelMap.CommonDate orderBy="fecha"/>
                <@headerCell title=uiLabelMap.PurchSolicitante orderBy="solicitanteId"/>
                <@headerCell title=uiLabelMap.PurchStatus orderBy="statusId"/>
                <@headerCell title=uiLabelMap.PartySupplier orderBy="proveedor"/>
                <@headerCell title=uiLabelMap.AccountingAmount orderBy="monto"/>
           </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.gastosReservaId href="${parameters.requestGasto}?gastosReservaId=${row.gastosReservaId}"/>
                <@displayDateCell date=row.fecha format="DATE"/>
                <@displayCell text=row.firstName +" " +row.lastName/>
                <@displayCell text=row.descripcion/>
                <@displayCell text=row.proveedor/>
                <@displayCurrencyCell amount=row.monto currencyUomId=row.tipoMoneda class="textright"/> 
                <@displayLinkCell text=uiLabelMap.VerSolicitudViatico href="imprimirSolicitudPDF?GastoReservaId=${row.gastosReservaId}"/>
				<#if row.statusId == 'COMPROBADA_GR' || row.statusId == 'VALIDADA_GR' || row.statusId == 'FINALIZADA_GR' >
				<@displayLinkCell text="Ver comprobante" href="imprimirComprobantePDF?GastoReservaId=${row.gastosReservaId}"/>
				<#else>
				<td>
				</#if>
            </#list>
        </table>
    </#noparse>
</@paginate>