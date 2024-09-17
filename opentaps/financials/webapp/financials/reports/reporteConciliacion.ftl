<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<@frameSection title="ConciliacionContPres">
<@form name="reporteConciliacion" url="reporteConciliacion">
	<table> 
        <@inputDateRow title="Fecha desde" name="fromDate" default=Static["org.ofbiz.base.util.UtilDateTime"].getYearStart(Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp())! />	
        <@inputDateRow title="Fecha hasta" name="thruDate" default=Static["org.ofbiz.base.util.UtilDateTime"].nowSqlDate()! />
		<@displayTitleCell title=uiLabelMap.CierreReportes/>
		<@inputSelectHashCell hash= {"true":"Con cierre", "false":"Sin cierre"} id="cierre" name="cierre"/>
		<@displayTitleCell title=uiLabelMap.TipoReporte/>
		<@inputSelectHashCell hash= {"pdf":"PDF", "xls":"EXCEL"} id="tipoReporte" name="tipoReporte"/>
		<@inputButtonRow title=uiLabelMap.CommonPrint />
	</table>
</@form>
</@frameSection>