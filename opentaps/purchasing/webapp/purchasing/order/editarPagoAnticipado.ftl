<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl" />

<@frameSection title="Editar pago anticipado" >
<form id="editarPagoAnticipadoForm" name="editarPagoAnticipadoForm" method="post" action="<@ofbizUrl>editarDatosPagoAnticipado</@ofbizUrl>">
<#assign urlHost = request.getServerName()+":"+request.getServerPort()/>
  <@inputHidden name="orderId" value=PagoAnticipado.orderId />
  <@inputHidden name="urlHost" value=urlHost/>
  <table class="twoColumnForm">
    <tbody>
    <@displayDateRow title=uiLabelMap.FinancialsAccountigDate format="DATE_ONLY" date=PagoAnticipado.orderDate highlightStyle=""/>
		<tr>
	      <@displayTitleCell title="Numero de pedido o contrato" />
	      <@displayCell text=PagoAnticipado.orderId />
	    </tr>
		<@displayRow title=uiLabelMap.PurchDescription text=PagoAnticipado.orderName/>
		<@displayRow title=uiLabelMap.PurchMoneda text=PagoAnticipado.currencyUom />
		<@displayRow title="Monto total" text=PagoAnticipado.grandTotal />
		<#if PagoAnticipado.statusId=="ENVIADO_PA" || PagoAnticipado.statusId=="COMENTADA_PA">
			<@inputTextRow title="Monto pago anticipado" name="pagoAnticipado" default=PagoAnticipado.pagoAnticipado />
			<@displayTitleCell title="Proveedor Pago Anticipado"/>
			<@inputAutoCompletePartyCell name="proveedorPagoAnticipado" size="25" default="${PagoAnticipado.proveedorPagoAnticipado?if_exists}" onChange="supplierChanged()" />
		<#else>
			<@displayRow title="Monto pago anticipado" text=PagoAnticipado.pagoAnticipado />
			<@displayRow title="Proveedor pago anticipado" text=PagoAnticipado.proveedorPagoAnticipado />
		</#if>
    <@inputSubmitRow title=uiLabelMap.CommonUpdate />  
    </tbody>
  </table>
</form>
</@frameSection>