<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@frameSection title=uiLabelMap.FinancialsStructureUpdateGlAccount>
  <form method="post" action="<@ofbizUrl>cambiarEstructura</@ofbizUrl>" name="cambiarEstructura">
  	<input type="hidden" name="glAccountId" value="${glAccountId}"/>
  	<input type="hidden" name="organizationPartyId" value="${organizationPartyId}"/>
    <table class="twoColumnForm" style="border:0">
    	<tr> <@inputTextRow title=uiLabelMap.FinancialsGLAccountCode name="newGlAccountId" titleClass="requiredField" maxlength=20 size=20 default="${glAccountId}" /> </tr>         	     
      	<tr> <@inputSubmitRow title=uiLabelMap.CommonUpdate /> </tr>
    </table>
  </form>
</@frameSection>
