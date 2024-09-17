<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form name="devolucionesForm" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <table >
    <tbody>
      ${uiLabelMap.ProductOrderId} : 
      <@inputLookup name="orderId" lookup="LookupPurchaseOrder" form="devolucionesForm" default=orderId?if_exists/>
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
