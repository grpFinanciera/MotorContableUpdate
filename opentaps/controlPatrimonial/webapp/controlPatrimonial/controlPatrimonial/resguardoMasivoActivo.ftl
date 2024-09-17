<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form name="consultaActivoFijoForm" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <table class="twoColumnForm">
    <tbody>      
      <tr>
        <@displayTitleCell title=uiLabelMap.ControlResguardante />
        <@inputAutoCompletePartyCell id="resguardante" name="resguardante" />
      </tr>         
      <tr>
	    <@displayTitleCell title=uiLabelMap.FechaInicioResguardo />
	    <@inputDateTimeCell name="fechaInicioResguardo" default=fechaInicioResguardo?if_exists />
	  </tr>
	  <tr>
	    <@displayTitleCell title=uiLabelMap.FechaFinResguardo />
	    <@inputDateTimeCell name="fechaFinResguardo" default=fechaFinResguardo?if_exists />
	  </tr>
	  <tr>
	    <@displayTitleCell title=uiLabelMap.FechaAsignacion />
	    <@inputDateTimeCell name="fechaAsignacion" default=fechaAsignacion?if_exists />
	  </tr>
	  <tr>    
	    <@inputTextRow name="comentariosAsignacion" title=uiLabelMap.comentariosAsignacion size=30 />
	  </tr>
	  <tr>  
        <@displayTitleCell title=uiLabelMap.ControlNuevoResguardante />
        <@inputAutoCompletePartyCell id="nuevoResguardante" name="nuevoResguardante" />
      </tr>
            
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
