<#--
 *@author     Jesus Ruiz
 *@since      1.0
-->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl" />

<form method="post" action="<@ofbizUrl>actualizaSufPresupuestal</@ofbizUrl>" name="sufPresupuestal" id="sufPresupuestal">
	<table class="twoColumnForm">
    	<tbody>
			<tr>
				<#assign flag = {"Y":"Activa","N":"Inactiva"} />
				<@displayTitleCell title=uiLabelMap.suficienciaPresupuestal />
				<td>
				<@inputSelectHash id="flag" name="flag" hash=flag default=suficiencia.flag/>
				</td>
				<@inputSubmitRow title=uiLabelMap.CommonEdit />
			</tr>								
		</tbody>
  	</table>			
</form>		
