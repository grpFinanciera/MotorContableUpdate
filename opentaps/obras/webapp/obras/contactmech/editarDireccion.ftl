 <@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
 <@form name="editarDireccion" url="editarDireccion" >
 <table>
 	<@inputHidden name="obraId" value=request.getParameter("obraId")! />
 	<@inputHidden id="contactMechId" name="contactMechId" value=request.getParameter("contactMechId")! />
	<@inputTextRow name="toName" title=uiLabelMap.PartyToName default="${(postalAddress.toName)?default(parameters.get('toName')?if_exists)}"/>
	<@inputTextRow name="attnName" title=uiLabelMap.PartyAttentionName default="${(postalAddress.attnName)?default(parameters.get('attnName')?if_exists)}"/>
	<@inputTextRow name="address1" title=uiLabelMap.PartyAddressLine1 default="${(postalAddress.address1)?default(parameters.get('address1')?if_exists)}"/>
	<@inputTextRow name="address2" title=uiLabelMap.PartyAddressLine2 default="${(postalAddress.address2)?default(parameters.get('address2')?if_exists)}"/>
	<@jerarquiaGeo idlistaGeoAct="" />
	<tr>
		<@displayTitleCell title=uiLabelMap.PartyZipCode  />
	<td>
		<@inputText name="postalCodeExt" size=10 maxlength="60" default="${(postalAddress.postalCodeExt)?default(parameters.get('postalCodeExt')?if_exists)}" />
	</td>
	</tr>
	<tr>
		<@displayTitleCell title=uiLabelMap.PartyPostalDirections />
		<@inputTextareaCell name="directions" cols=60 rows=2 default="${(postalAddress.directions)?default(parameters.get('directions')?if_exists)}" />
	</tr>
	<@inputSubmitRow title=uiLabelMap.CommonSave />
</table>
</@form>