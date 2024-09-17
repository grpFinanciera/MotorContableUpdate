<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@frameSection title=uiLabelMap.PurchCrearRequisicion >
<form id="editarRequisicionForm" name="editarRequisicionForm" method="post" action="<@ofbizUrl>editarDatosRequisicion</@ofbizUrl>">
  <@inputHidden name="requisicionId" value=Requisicion.requisicionId />
  <table class="twoColumnForm">
    <tbody>
    <tr>
	  <@displayRow title=uiLabelMap.PurchRequisicionId text=Requisicion.requisicionId/>
	</tr>
    <tr>  
      <@displayTitleCell title=uiLabelMap.PurchDescripcion />
      <@inputTextCell name="descripcion" maxlength="255" size=50 default=Requisicion.descripcion/>
    </tr>
	<tr>  
      <@inputTextareaRow title=uiLabelMap.PurchJustification name="justificacion" default=Requisicion.justificacion />
    </tr>
    <tr>  
      <@displayTitleCell title=uiLabelMap.PurchMoneda />
      <@inputSelectCell key="uomId" list=listMonedas name="tipoMoneda" displayField="uomId" default=Requisicion.tipoMoneda/>
    </tr>    
    <tr>        
        <@inputHidden name="organizationPartyId" value="${parameters.organizationPartyId}" index=tag_index />
    </tr>
    <@inputSubmitRow title=uiLabelMap.CommonUpdate />  
    </tbody>
  </table>
</form>
</@frameSection>