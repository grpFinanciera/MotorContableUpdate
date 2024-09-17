<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#if error?has_content>
    <@display text=error class="requiredField" />
</#if>
<form name="guardarMaximosMinimos" method="post" action="<@ofbizUrl>guardarMaximosMinimos</@ofbizUrl>">
  <@inputHidden name="performFind" value="Y"/>
  <table class="twoColumnForm">
    <tbody>
		<@inputAutoCompleteProductRow title=uiLabelMap.ProductId name="productId" titleClass="requiredField" />
		<@inputSelectRow title=uiLabelMap.FacilityId name="facilityId" list=facilityList key="facilityId" displayField="facilityName" required=false titleClass="requiredField" />
		<@inputTextRow name="maximo" title=uiLabelMap.Maximo titleClass="requiredField"/>
		<@inputTextRow name="minimo" title=uiLabelMap.Minimo titleClass="requiredField"/>
      </tr>
      <@inputSubmitRow title=uiLabelMap.CommonCrear />
    </tbody>
  </table>
</form>