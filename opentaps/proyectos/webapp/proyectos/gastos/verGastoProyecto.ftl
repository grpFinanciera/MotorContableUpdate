<#--
 *@author     Jesus Ruiz
 *@since      1.0
-->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl" />
<#assign hostname = request.getServerName() />
<#if GastoProyecto?has_content && GastoProyecto.gastoProyectoId?exists>
<@frameSection title=uiLabelMap.GastosReservaApplication extra=LinkActualizar>
<form method="post" action="<@ofbizUrl>verGastoProyecto</@ofbizUrl>" name="verGastoProyecto" style="margin: 0;" id="verGastoProyecto">
	<table width="100%">
		<@inputHidden name="gastoProyectoId" value=GastoProyecto.gastoProyectoId/>
		<@inputHidden name="concepto" value=GastoProyecto.concepto/>		        
		<@displayRow title="Id Gasto" text=GastoProyecto.gastoProyectoId/>
		<@displayRow title="Concepto" text=GastoProyecto.concepto/>
		<@displayRow title=uiLabelMap.CommonCurrency text=GastoProyecto.tipoMoneda/>
		<@displayCurrencyRow title="Monto Solicitado" currencyUomId=GastoProyecto.tipoMoneda amount=GastoProyecto.monto class="tabletext"/>
		
		<@displayDateRow title="Fecha" date=GastoProyecto.fecha! format="DATE" />
</form>
	</table>
</@frameSection>
</#if>