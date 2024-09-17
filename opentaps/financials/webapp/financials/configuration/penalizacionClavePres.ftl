<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form method="post" action="<@ofbizUrl>AgregarClavePenalizacion</@ofbizUrl>" name="AgregarClavePenalizacion">
	<table>
		<@displayCvesPresupRow tagTypes=tagTypes />
		<@inputSubmitRow title=uiLabelMap.CommonAdd/>
	</table>
</form> 

<@frameSection title=uiLabelMap.ClavesPresupuestales >
	<table width="100%">
		<tr>
			<th>${uiLabelMap.ClavePresupuestal}</th>
			<th>${uiLabelMap.CommonSince}</th>
			<th>${uiLabelMap.CommonThru}</th>
			<th>${uiLabelMap.FinancialsActivo}</th>
		</tr>
	<#list clavesPresList as row>	
		<tr>
			<@displayCell text=row.clavePresupuestal!'' />
			<@displayCell text=row.fechaDesde!''/>
			<@displayCell text=row.fechaHasta!''/>
			<#assign activo><#if row.flag?has_content && row.flag='Y'>Si<#else>No</#if></#assign>
			<@displayCell text=activo />
		</tr>
	</#list>
	</table>
</@frameSection>
	
