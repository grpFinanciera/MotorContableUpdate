<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<@paginate name="listPagosAnticipadosPendiente" list=solicitudPagoPendienteListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title="Número de pedido o contrato" orderBy="orderId"/>
                <@headerCell title=uiLabelMap.PurchDescripcion orderBy="orderName"/>
                <@headerCell title="Monto total" orderBy="grandTotal"/>
                <@headerCell title="Monto pago anticipado" orderBy="pagoAnticipado"/>                 
                <@headerCell title=uiLabelMap.PurchIdPersonaSolicitante orderBy="partyId"/>
                <@headerCell title=uiLabelMap.PurchSolicitante orderBy="firstName"/>                
                <@headerCell title=uiLabelMap.PurchIdAreaSolicitante orderBy="areaId"/>
                <@headerCell title=uiLabelMap.PurchAreaSolicitante orderBy="groupName"/>
                <@headerCell title=uiLabelMap.PurchTipoWorkflow orderBy="descripcionWorkflow"/>                                
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.orderId href="verOrden?orderId=${row.orderId}"/>
                <@displayCell text=row.orderName/>
                <@displayCurrencyCell amount=row.grandTotal currencyUomId=row.currencyUom/>
                <@displayCurrencyCell amount=row.pagoAnticipado currencyUomId=row.currencyUom/>
				<@displayCell text=row.partyId/>
                <@displayCell text=row.firstName+" "+row.lastName/>
                <@displayCell text=row.areaId/>
                <@displayCell text=row.groupName/>
                <@displayCell text=row.descripcionWorkflow/>
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>