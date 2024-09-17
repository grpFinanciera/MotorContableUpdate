<#--
 *@author     Jesus Ruiz
 *@since      1.0
-->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl" />
<#assign hostname = request.getServerName() />
<#if ServicioPersonal?has_content && ServicioPersonal.servicioPersonalId?exists>
<@frameSection title="Servicios Personales" extra=LinkActualizar>
<form method="post" action="<@ofbizUrl>verServiciosPersonales</@ofbizUrl>" name="verServiciosPersonales" style="margin: 0;" id="verServiciosPersonales">
	<table width="100%">
		<@inputHidden name="servicioPersonalId" value=ServicioPersonal.servicioPersonalId/>
		<@inputHidden name="concepto" value=ServicioPersonal.tipoContratacion/>		        
		<@displayRow title="Id Servicio Personal" text=ServicioPersonal.servicioPersonalId/>
		<@displayRow title="Concepto" text=ServicioPersonal.tipoContratacion/>
		<@displayRow title=uiLabelMap.CommonCurrency text=ServicioPersonal.tipoMoneda/>
		<@displayRow title="Nombre" text=ServicioPersonal.nombre +" "+ServicioPersonal.apellidos!/>
		<@displayCurrencyRow title="Monto Solicitado" currencyUomId=ServicioPersonal.tipoMoneda amount=ServicioPersonal.sueldo class="tabletext"/>
		
		<@displayDateRow title="Fecha Contable" date=ServicioPersonal.fechaContable! format="DATE" />
</form>
	</table>
</@frameSection>
</#if>