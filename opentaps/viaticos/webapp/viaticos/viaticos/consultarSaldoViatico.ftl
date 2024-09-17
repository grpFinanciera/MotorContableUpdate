<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form name="consultarSaldoViaticoForm" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <table class="twoColumnForm">
    <tbody>
      <tr>
        <@displayTitleCell title=uiLabelMap.PurchSolicitante />
        <@inputLookupCell lookup="LookupPartyName" form="consultarSaldoViaticoForm" name="solicitanteId" />
       </tr>
      <tr>
	    <@inputSelectRow title=uiLabelMap.PurchPeriodo required=false list=periodosList  displayField="enumCode" name="enumCode" default=enumCode?if_exists />
       </tr>
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
