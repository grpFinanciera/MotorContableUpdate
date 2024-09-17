<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form name="busquedaPedidoContratoForm" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <table class="twoColumnForm">
    <tbody>
      <tr>
        <@displayTitleCell title=uiLabelMap.AccountingNumberOfOrders/>
        <@inputTextCell name="orderId" maxlength=60  />
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.OrderOrderName/>
        <@inputTextCell name="orderName" maxlength=60  />
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.PartySupplier/>
		<@inputAutoCompletePartyCell name="supplierPartyId" size="25" />
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.PurchasingProduct />
        <@inputAutoCompleteProductCell form="busquedaPedidoContratoForm" name="productId" />
      </tr>
      <tr>
	    <@inputSelectRow title=uiLabelMap.PurchasingStatus required=false list=estatusList  displayField="description" name="statusId" default=statusId?if_exists />
      </tr>
      <tr>
       <@inputDateRangeRow title=uiLabelMap.PurchBackOrdersReportOrderDate fromName="fechaContableDesde" thruName="fechaContableHasta" />
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.PurchRequisicionId/>
        <@inputTextCell name="requisicionId" maxlength=60  />
      </tr>
      <@inputAutoCompleteUserLoginPartyRow title=uiLabelMap.CommonCreatedBy name="partyId" />
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
