<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@frameSection title=uiLabelMap.PresupuestoAdminEtiquetas >
	<@form name="creaEtiqueta" url="creaEtiqueta">
	<table>
		<@inputTextRow name="etiquetaId" title=uiLabelMap.PresupuestoEtiquetaId size=20 maxlength=20 titleClass="requiredField"/>
		<@inputTextRow name="descripcion" title=uiLabelMap.CommonDescription size=50 maxlength=20 titleClass="requiredField"/>
		<@inputDateRangeRow title=uiLabelMap.PresupuestoFechaVigencia fromName="fromDate" thruName="thruDate"/>
		<@inputSubmitRow title=uiLabelMap.CommonCreate />
	</table>
	</@form>
</@frameSection>
