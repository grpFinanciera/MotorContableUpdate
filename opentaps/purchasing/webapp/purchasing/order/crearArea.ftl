<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#if error?has_content>
    <@display text=error class="requiredField" />
</#if>
<form name="crearAreaForm" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <table class="twoColumnForm">
    <tbody>
       <tr>
        <@displayTitleCell title=uiLabelMap.PurchArea titleClass="requiredField"/>
        <@inputLookupCell lookup="LookupPartyName" form="crearAreaForm" name="areaId" />
       </tr>
        <tr>
        <@displayTitleCell title=uiLabelMap.PurchasingAuto titleClass="requiredField"/>
        <@inputLookupCell lookup="LookupPartyName" form="crearAreaForm" name="personId" />
       </tr>
       <tr> 
	      <@displayTitleCell title=uiLabelMap.TipoWorkFlow titleClass="requiredField"/>
	      <@inputSelectCell key="tipoWorkFlowId" list=workflowList name="tipoWorkFlowId" displayField="descripcion"/>
	    </tr>
      <@inputSubmitRow title=uiLabelMap.CommonCrear />
    </tbody>
  </table>
</form>