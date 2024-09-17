<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@frameSection title=uiLabelMap.ObrasModificacionCalendario >
	
<@form method="post" name="modificacionesObra" url="actualizarModificacionesObrasCalendario" >
	<table>
		<input type="hidden" name="obraId" value="${obraId}"/>
		<@inputTextRow name="numOficioJustificaciTecMpa" title=uiLabelMap.ObrasnumOficioSolicitudJustificacionTecnicaMPA default=obra.numOficioJustificaciTecMpa! />
		<@inputTextRow name="numOficioAutorizacioSfpMpa" title=uiLabelMap.ObrasnumOficioAutorizacionSFPMPA default=obra.numOficioAutorizacioSfpMpa! />
		<@inputTextRow name="numOficioDictamenContrate" title=uiLabelMap.ObrasnumOficioDictamenTecnicoContrate default=obra.numOficioDictamenContrate! />
		<@inputTextRow name="numOficioConvenio" title=uiLabelMap.ObrasnumOficioConvenio default=obra.numOficioConvenio! />
		<@inputTextRow name="numOficioCalendarEjecucion" title=uiLabelMap.ObrasnumOficioCalendarioEjecucionDefinitivo default=obra.numOficioCalendarEjecucion! />
		<@inputTextRow name="numOficioModifFianzaCumpl" title=uiLabelMap.ObrasnumOficioModificacionFianzaCumplimiento default=obra.numOficioModifFianzaCumpl! />
		<@inputSubmitRow title=uiLabelMap.CommonUpdate />
	</table>
</@form>
</@frameSection>