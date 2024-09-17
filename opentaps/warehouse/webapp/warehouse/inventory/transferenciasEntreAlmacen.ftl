<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form name="transferenciaEntreAlmacenForm" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <table class="twoColumnForm">
    <tbody>      
      <tr>        
        <@displayTitleCell title=uiLabelMap.Almacen />
        <@inputSelectCell name="facilityId" list=listaAlmacenes key="facilityId" required=false displayField="facilityName"/>
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.Producto />
        <@inputAutoCompleteProductCell form="consultaActivoFijoForm" name="productId" />        
      </tr>
      <tr>
      	<@displayTitleCell title=uiLabelMap.CodigoArticuloInventario />
      	<@inputTextCell name="inventoryItemId" size=20 />
      </tr>                
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
