<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form name="busquedaOrdenPagoForm" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <table class="twoColumnForm">
    <tbody>
      <tr>
        <@displayTitleCell title=uiLabelMap.ordenPagoId/>
        <@inputTextCell name="ordenPagoId" maxlength=60  />
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.invoiceId/>
        <@inputTextCell name="invoiceId" maxlength=60  />
      </tr>       
       <tr>
	    <@inputSelectRow title=uiLabelMap.AccountingTipoDocumento required=false list=eventos displayField="descripcion" name="acctgTransTypeId" default=acctgTransTypeId?if_exists />
       </tr>
       <tr>
	    <@inputSelectRow title=uiLabelMap.FinancialsStatusId required=false list=estatusList  displayField="descripcion" name="statusId" default=statusId?if_exists />
       </tr>       
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
