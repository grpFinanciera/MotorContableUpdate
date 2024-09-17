<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@frameSection title=uiLabelMap.ClassificationTagsPostingEntry>

  <form method="post" action="<@ofbizUrl>classificationTagsUsage</@ofbizUrl>">
    <@inputHidden name="organizationPartyId" value=organizationPartyId />
	<@inputSelectRow name="customTimePeriodYear" id="customTimePeriodYear" title=uiLabelMap.FinancialPeriodo default=defaultYearId list=customTimePeriods displayField="fromDate" key="customTimePeriodId" required=true />
	<@inputSubmitRow title=uiLabelMap.CommonSelect />
  </form>

</@frameSection>
