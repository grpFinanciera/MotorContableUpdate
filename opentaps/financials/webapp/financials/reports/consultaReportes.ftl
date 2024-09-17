<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form name="busquedaReportesForm" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <table class="twoColumnForm">
    <tbody>
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialsWorkFlowId/>
        <@inputTextCell name="workFlowId" maxlength=60  />        
      </tr>
      <tr>
         
       </tr> 
       <tr>
	    <@inputSelectRow title=uiLabelMap.PurchasingStatus required=false list=estatusList  displayField="descripcion" name="statusId" default=statusId?if_exists />
       </tr>
       <tr>
        <@displayTitleCell title=uiLabelMap.FinancialsReporteId/>
        <@inputTextCell name="reporteId" maxlength=60  />
       </tr>
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
