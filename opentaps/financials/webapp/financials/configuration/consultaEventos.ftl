<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form name="consultaEventosForm" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <table class="twoColumnForm">
    <tbody>
      <@inputSelectRow title=uiLabelMap.AccountingTipoDocumento required=false list=EventoContablesList  displayField="descripcion" name="acctgTransTypeId" default=acctgTransTypeId?if_exists />
      <@inputSelectRow title=uiLabelMap.FinancialModulo required=false list=moduloList  displayField="nombre" name="moduloId" default=moduloId?if_exists />
      <tr>
      	<@displayTitleCell title=uiLabelMap.FinancialsDocumentoId />
      	<@inputTextCell name="codigo" size="20" maxlength="20"/>
      </tr>
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
