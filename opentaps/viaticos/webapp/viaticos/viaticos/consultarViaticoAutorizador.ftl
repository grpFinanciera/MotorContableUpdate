<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form name="busquedaViaticoForm" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <table class="twoColumnForm">
    <tbody>
      <tr>
        <@displayTitleCell title=uiLabelMap.ViaticoId/>
        <@inputTextCell name="viaticoId" maxlength=60  />
      </tr>
      <tr>
      	<@displayTitleCell title=uiLabelMap.PurchSolicitante />
        <@inputLookupCell lookup="LookupPartyName" form="busquedaViaticoForm" name="solicitante" />
      </tr>
      <tr>
	    <@inputSelectRow title=uiLabelMap.PurchStatus required=false list=estatusList  displayField="descripcion" name="statusId" default=statusId?if_exists />
       </tr>
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
