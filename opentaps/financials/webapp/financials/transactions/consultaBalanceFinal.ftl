<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<form name="balanceFinal" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <table class="twoColumnForm">
    <tbody>      
      <@inputSelectRow title=uiLabelMap.FinancialPeriodo required=false list=listCiclosCerrados  displayField="periodName" name="customTimePeriodId" default=customTimePeriodId?if_exists key="customTimePeriodId"/>
	  <@inputAutoCompleteGlAccountRow name="glAccountId" title="${uiLabelMap.AccountingGlAccount}" />
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>