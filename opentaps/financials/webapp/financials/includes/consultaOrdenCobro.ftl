<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form name="busquedaOrdenCobroForm" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <table class="twoColumnForm">
    <tbody>
      <tr>
        <@displayTitleCell title=uiLabelMap.ordenCobroId/>
        <@inputTextCell name="ordenCobroId" maxlength=60  />
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialsPreOrdenCobro/>
        <@inputTextCell name="invoiceId" maxlength=60  />
      </tr>       
       <tr>
	    <@inputSelectRow title=uiLabelMap.AccountingTipoDocumento required=false name="acctgTransTypeId" list=eventos?sort_by("descripcion") id="acctgTransTypeId" key="acctgTransTypeId" displayField="descripcion"/>
       </tr>
       <tr>
	    <@inputSelectRow title=uiLabelMap.FinancialsStatusId required=false list=estatusList  displayField="descripcion" name="statusId" default=statusId?if_exists />
       </tr>       
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
