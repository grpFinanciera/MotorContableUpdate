<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<form name="BuscaPedidoInterno">
  <table class="twoColumnForm">
    <tbody>
    	<@inputHidden name="performFind" value="Y" />
    	<@inputHidden name="almacenId" value="${parameters.facilityId}" />
    	<@inputTextRow name="pedidoInternoId" title=uiLabelMap.WarehousePedidoInternoId size=30 maxlength=20 default="${parameters.pedidoInternoId!}" />
		<@inputTextRow name="descripcion" title=uiLabelMap.CommonDescription size=100 maxlength=255 default="${parameters.descripcion!}" />
		<@inputAutoCompletePersonRow title=uiLabelMap.WarehouseSolicitante name="personaSolicitanteId" default="${parameters.personaSolicitanteId!}"/>
		<@inputSelectRow title=uiLabelMap.CommonStatus name="statusId" list=listEstatus! key="statusId" displayField="descripcion" default="${parameters.statusId!}" required=false />
		<@inputDateRangeRow title=uiLabelMap.CommonDate fromName="fechaDesde" thruName="fechaHasta" />
		<@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
