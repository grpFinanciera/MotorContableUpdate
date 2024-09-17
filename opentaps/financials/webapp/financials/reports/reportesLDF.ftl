<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<div class="tabletext" style="margin-bottom: 30px;">
<#assign hostname = request.getServerName()/>
<table style="width: 100%;">
<tr>
  <td style="vertical-align: top; width: 35%;">
    <@displayReportGroup group="LDF" nameOnly=true>
		<li><a href="" target="_blank">${uiLabelMap.ReportesLDFEstadoSituacionFinancieraDet}</a></li>
		<li><a href="" target="_blank">${uiLabelMap.ReportesLDFInfoAnaliticoDeudaPublica}</a></li>
		<li><a href="" target="_blank">${uiLabelMap.ReportesLDFBalancePresupuestario}</a></li>
		<li><a href="" target="_blank">${uiLabelMap.ReportesLDFEstadoAnaliticoIngresosDet}</a></li>
		<li><a href="" target="_blank">${uiLabelMap.ReportesLDFAnaliticoPresupEgresosDetCOG}</a></li>
		<li><a href="" target="_blank">${uiLabelMap.ReportesLDFAnaliticoPresupEgresosDetAdmin}</a></li>
		<li><a href="" target="_blank">${uiLabelMap.ReportesLDFAnaliticoPresupEgresosDetFuncional}</a></li>
		<li><a href="" target="_blank">${uiLabelMap.ReportesLDFAnaliticoPresupEgresosDetServiciosPersonales}</a></li>
		<li><a href="" target="_blank">${uiLabelMap.ReportesLDFProyeccionesIngresos}</a></li>
		<li><a href="" target="_blank">${uiLabelMap.ReportesLDFProyeccionesEgresos}</a></li>
		<li><a href="" target="_blank">${uiLabelMap.ReportesLDFResultadoIngresos}</a></li>
		<li><a href="" target="_blank">${uiLabelMap.ReportesLDFResultadoEgresos}</a></li>
    </@displayReportGroup> 		
  </td>
</tr>
</table>
</div>