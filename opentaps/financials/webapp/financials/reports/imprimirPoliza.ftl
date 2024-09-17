<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<@frameSection title=uiLabelMap.ImprimirPoliza>
<@form name="imprimirPoliza" url="imprimirPoliza" acctgTransId=parameters.acctgTransId>
	<table>  
		<@displayTitleCell title=uiLabelMap.PolizaAgrupada/> 
		<@inputSelectHashCell hash= {"true":"SI", "false":"NO"} id="isAgrupado" name="isAgrupado"/>
		<@inputSelectRow name="organizationPartyId" title=uiLabelMap.UnidadResponsable list=configuredOrganizations key="partyId" ; option>
        	${option.groupName} (${option.partyId})
      	</@inputSelectRow>
		<@displayTitleCell title=uiLabelMap.TipoReporte/>
		<@inputSelectHashCell hash= {"pdf":"PDF", "xls":"EXCEL"} id="tipoReporte" name="tipoReporte"/>
		<@inputButtonRow title=uiLabelMap.CommonPrint />
	</table>
</@form>
</@frameSection>