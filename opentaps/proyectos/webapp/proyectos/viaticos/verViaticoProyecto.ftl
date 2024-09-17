<#--
 *@author     Jesus Ruiz
 *@since      1.0
-->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl" />
<#assign hostname = request.getServerName() />
<#if ViaticoProyecto?has_content && ViaticoProyecto.viaticoProyectoId?exists>
<@frameSection title=uiLabelMap.ViaticosHome extra=LinkActualizar>
<form method="post" action="<@ofbizUrl>verViatico</@ofbizUrl>" name="verViatico" style="margin: 0;" id="verViatico">
	<table width="100%">
		<@inputHidden name="viaticoId" value=ViaticoProyecto.viaticoProyectoId/>
		<@inputHidden name="motivo" value=ViaticoProyecto.motivo/>		        
		<@displayRow title=uiLabelMap.ViaticoId text=ViaticoProyecto.viaticoProyectoId/>
		<@displayRow title=uiLabelMap.ViaticoJustification text=ViaticoProyecto.motivo/>
		<@displayRow title=uiLabelMap.CommonCurrency text=ViaticoProyecto.tipoMoneda/>
		<@displayCurrencyRow title=uiLabelMap.DailyAmount currencyUomId=ViaticoProyecto.tipoMoneda amount=ViaticoProyecto.montoDiario class="tabletext"/>
		<@displayCurrencyRow title=uiLabelMap.FieldAmount currencyUomId=ViaticoProyecto.tipoMoneda amount=ViaticoProyecto.montoTrabCampo class="tabletext"/>
		<@displayCurrencyRow title=uiLabelMap.PurchTransportAmount currencyUomId=ViaticoProyecto.tipoMoneda amount=ViaticoProyecto.montoTransporte class="tabletext"/>
		<#assign geoO = ViaticoProyecto.getRelatedOne("GeoOrigenGeo")/>
		<@displayRow title=uiLabelMap.PurchOrigin text=geoO.geoName/>
		<#assign geoD = ViaticoProyecto.getRelatedOne("GeoDestinoGeo")/>
		<@displayRow title=uiLabelMap.PurchDestination text=geoD.geoName/>
		
		<@displayDateRow title=uiLabelMap.CommonStartDate date=ViaticoProyecto.fechaInicial! format="DATE" />
		<@displayDateRow title=uiLabelMap.CommonEndDate date=ViaticoProyecto.fechaFinal! format="DATE" />
</form>
	</table>
</@frameSection>
</#if>