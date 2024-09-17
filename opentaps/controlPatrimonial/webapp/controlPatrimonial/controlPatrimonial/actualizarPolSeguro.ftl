

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form name="actualizaPolizaSeguro" method="post" action="<@ofbizUrl>actualizaPolizaSeguro</@ofbizUrl>">

<table class="fourColumnForm">

  <tr><td colspan="4" class="formSectionHeaderTitle">${uiLabelMap.ActualizarPolizaSeguro}</td></tr>
    
  <@inputTextRow name="numPolizaAnt" title=uiLabelMap.PolizaAnterior size=30 titleClass="requiredField"/>
  <@inputTextRow name="numPolizaNuevo" title=uiLabelMap.PolizaNueva size=30 titleClass="requiredField"/>
  <tr>
    <@displayTitleCell title=uiLabelMap.FechaDesde titleClass="requiredField" />
    <@inputDateTimeCell name="fechaDesdeNueva"  />
  </tr>
  <tr>
    <@displayTitleCell title=uiLabelMap.FechaHasta/>
    <@inputDateTimeCell name="fechaHastaNueva"  />
  </tr>    
  <@inputTextRow name="comentario" title=uiLabelMap.Comentario size=30 />  
  <@inputSubmitRow title=uiLabelMap.CommonUpdate />      	  
   
</table>

</form>

