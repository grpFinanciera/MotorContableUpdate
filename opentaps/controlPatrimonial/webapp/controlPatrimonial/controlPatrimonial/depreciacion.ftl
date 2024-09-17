<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<form action="<@ofbizUrl>ProcesoDepreciacion</@ofbizUrl>" name="ProcesoDepreciacion"  method="post">
	<table>
		
		<input type="hidden" name='organizationPartyId' value="${organizationPartyId?if_exists}" />	   	         		
		<@inputSelectRow title=uiLabelMap.FormFieldTitleTipoActivoFijo required=false list=tiposActivo displayField="description" name="fixedAssetTypeId" default=fixedAssetTypeId?if_exists titleClass="requiredField" />
		<@inputSelectRow title=uiLabelMap.FormFieldTitleMes required=false list=meses displayField="description" name="mesId" default=mesId?if_exists titleClass="requiredField" />
		<@inputSelectRow title=uiLabelMap.FormFieldTitleCiclo required=false list=ciclos displayField="enumCode" name="enumCode" default=enumCode?if_exists titleClass="requiredField" />
		<@inputDateTimeRow name="fechaContable" title=uiLabelMap.FinancialsAccountigDate form="ProcesoDepreciacion" default=Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp() titleClass="requiredField"/>
		<#--<@inputSelectRow title=uiLabelMap.FormFieldTitleTipoEvento required=false list=eventos displayField="descripcion" name="acctgTransTypeId" default=acctgTransTypeId?if_exists titleClass="requiredField" />-->
		<@inputTextRow name="comentario" title=uiLabelMap.FormFieldTitleComentario size=70 titleClass="requiredField"/>		
		<@inputSubmitRow title=uiLabelMap.CommonSubmit />  		
	</table>
</form>