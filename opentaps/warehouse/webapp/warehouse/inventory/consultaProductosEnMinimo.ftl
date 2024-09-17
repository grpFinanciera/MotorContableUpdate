<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form name="consultaProductosEnMinimo" method="post" action="">
  <table class="twoColumnForm">
    <tbody>    
    	<@inputHidden name="facilityId" value=facilityId />
       <tr>
        <@displayTitleCell title=uiLabelMap.ProductId />
        <@inputLookupCell lookup="LookupProductName" form="consultaProductosEnMinimo" name="productId" />
       </tr>              
	    <tr>
	        <@displayTitleCell title=uiLabelMap.MaximoDesde />
	        <@inputTextCell name="maximoDesde" />
	        <@displayTitleCell title=uiLabelMap.Hasta />
	        <@inputTextCell name="maximoHasta" />
        </tr>        
        <tr>
	        <@displayTitleCell title=uiLabelMap.MinimoDesde />
	        <@inputTextCell name="minimoDesde" />
	        <@displayTitleCell title=uiLabelMap.Hasta />
	        <@inputTextCell name="minimoHasta" />
	    </tr>
	    <tr>
	        <@displayTitleCell title=uiLabelMap.CantidadActualDesde />
	        <@inputTextCell name="cantidadActualDesde" />
	        <@displayTitleCell title=uiLabelMap.Hasta />
	        <@inputTextCell name="cantidadActualHasta" />
	    </tr>        
      </tr>
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>