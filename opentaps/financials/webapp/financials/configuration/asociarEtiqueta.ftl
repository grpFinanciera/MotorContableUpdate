<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@frameSection title=uiLabelMap.PresupuestoAsociarEtiqueta >
	<@form name="guardarEtiquetaEnum" url="guardarEtiquetaEnum" >
	<table>
		<@inputSelectRow id="etiquetaId" name="etiquetaId" title=uiLabelMap.PresupuestoEtiqueta 
							list=listEtiqueta key="etiquetaId" displayField="descripcion" />
		<@inputAutoCompleteEnumerationRow id="enumId" name="enumId" title=uiLabelMap.FinancialsClasificacion 
							form="asociarEtiqueta" />
		<@inputSubmitRow title=uiLabelMap.CommonAdd />
	</table>
	</@form>
</@frameSection>