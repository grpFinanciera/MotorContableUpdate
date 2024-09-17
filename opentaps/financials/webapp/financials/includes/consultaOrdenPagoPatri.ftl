<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form name="busquedaOrdenPagoPatriForm" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <table class="twoColumnForm">
    <tbody>
      <tr>
        <@displayTitleCell title=uiLabelMap.OrdenPagoPatrimonialId/>
        <@inputTextCell name="idPendienteTesoreria" maxlength=60  />
      </tr>       
       <tr>
	    <@inputSelectRow title=uiLabelMap.FinancialsStatusId required=false list=estatusList  displayField="descripcion" name="statusId" default=statusId?if_exists />
       </tr>       
       <tr>
        <@displayTitleCell title=uiLabelMap.Descripcion/>
        <@inputTextCell name="descripcion" maxlength=60  />
      </tr>
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
