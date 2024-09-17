<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form name="busquedaDevolucionesForm" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <table class="twoColumnForm">
    <tbody>
      <tr>
        <@displayTitleCell title=uiLabelMap.DevolucionId/>
        <@inputTextCell name="devolucionId" maxlength=60  />
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.OrderId/>
        <@inputTextCell name="orderId" maxlength=60  />
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.Poliza/>
        <@inputTextCell name="poliza" maxlength=60  />
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.OrdenCobroId/>
        <@inputTextCell name="invoiceId" maxlength=60  />
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.ProductId />
        <@inputAutoCompleteProductCell form="busquedaDevolucionesForm" name="productId" />
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.Cantidad/>
        <@inputTextCell name="cantidad" maxlength=60  />
      </tr>       
      <tr>        
        <@inputRangeRow title=uiLabelMap.PrecioUnitario fromName="precioUnitarioFrom" thruName="precioUnitarioThru" size=10/>
      </tr>              
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
