
<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@form name="CreaXML" url="CreaXML" >
	<#assign listMeses = delegator.findAll("Mes") />	
	<#assign mapaCondicion = Static["org.ofbiz.base.util.UtilMisc"].toMap("enumTypeId","CL_CICLO") />
	<#assign listAnios = delegator.findByAnd("Enumeration",mapaCondicion) />
	<#assign mapaReportes = {"CATALOGO_CUENTAS":uiLabelMap.FinancialsCatalogoCuentas,
			"BALANZA":uiLabelMap.FinancialsBalanzaComprobacion,"POLIZAS":uiLabelMap.PolizasContables,
			"FINANCIERO":uiLabelMap.FinancialsReporteFinanciero,
			"PRESUPUESTAL":uiLabelMap.FinancialsReportePresupuestal} />
			
	<table>
	<@inputAutoCompletePartyGroupRow title=uiLabelMap.Organizacion name="organizationPartyId" />
	<@inputSelectRow title=uiLabelMap.FinancialMes name="mes" list=listMeses key="mesId" displayField="description" />
	<@inputSelectRow title=uiLabelMap.CommonYear name="anio" list=listAnios key="sequenceId" displayField="sequenceId" />
	<@inputSelectHashRow name="reporteId" title=uiLabelMap.CommonReport hash=mapaReportes />
	<tr><td></td><td><input class="smallSubmit" type="submit" value="${uiLabelMap.CommonCreate}" /></td></tr>
	</table>

</@form>
