<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#if !penasList?has_content>
<form name="pendaDeductivaForm" method="post" action="">
  <table class="twoColumnForm">
    <tbody>
      <tr>
         	<@displayTitleCell title=uiLabelMap.PrchTipoPena />
         	<#assign tipos = ["Pena Convencional", "Deductiva"]/>
         	<td>
         		<select name="tipoPena" class="inputBox">
         			<option value=""></option>
         			<#list tipos as tipo>
            			<option value="${tipo}">${tipo}</option>
        			</#list>
        		</select>
         	</td>
      </tr>
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
</#if>

<#if penasList?has_content>
<@frameSection title="">
<form name="tipoPenaDeductiva" method="POST" action="">  
	<input type="hidden" name="flagV" value="Y"/>
	<input type="hidden" name="tipoPena" value="${tipoPena}"/>
	<table border="0" width="100%">
	<tbody>
		<tr>
			<@displayTitleCell title=uiLabelMap.PurchBaseCalculo titleClass="requiredField"/>
			<@inputSelectCell name="penaId" list=penasList?default([]) key="penaId" displayField="base" default="${penaDeductiva?if_exists}"/>
		</tr>
		<@inputSubmitRow title=uiLabelMap.CommonUpdate />
	</tbody>
</form>
</@frameSection>
</#if>
