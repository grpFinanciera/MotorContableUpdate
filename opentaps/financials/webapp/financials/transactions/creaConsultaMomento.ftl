<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#assign extraOptions>
  <@submitFormLink form="CrearMomento" text=uiLabelMap.FinancialsAfectar />
</#assign>

<@form name="CrearMomento" url="CrearMomento" afectacionId=Afectacion.afectacionId! acctgTransTypeId=acctgTransTypeId statusId="CREADA" >
<@frameSection title=uiLabelMap.FinancialsConsultarMomento extra=extraOptions?if_exists>
  <table class="twoColumnForm">
		<@displayRow title=uiLabelMap.idPolizaAgrupador text=Afectacion.afectacionId! />
		<@displayDateRow title=uiLabelMap.FinancialsTransactionDate date=Afectacion.fechaTransaccion! />
		<@inputDateRow name="fechaContable" title=uiLabelMap.FinancialsAccountigDate default=Afectacion.fechaContable! />
		<@inputTextRow name="comentario" title=uiLabelMap.Comentario default=Afectacion.comentario!/>
		<@displayRow title=uiLabelMap.FinancialsTipoEvento text=EventoContable.descripcion! />
  </table>
</@frameSection>

<@frameSection title=uiLabelMap.ClavesPresupuestales>
<table width="100%">     
	<@displayTitleCell title=uiLabelMap.clavePresupuestal />
	<@displayTitleCell title=uiLabelMap.Monto />

	<#list AfectacionDetalle as row>
	<#if row.statusId=='AFECTAR' && (row.montoRestante > 0) >
	<tr name="item">	
	<@displayCell text=row.clavePresupuestal style="float:right" />
		<@inputHidden name="afectacionSeqId${row_index}" value=row.afectacionSeqId />
		<@inputHidden name="clavePresupuestal${row_index}" value=row.clavePresupuestal />
		<@inputCurrencyCell name="montoClave${row_index}" default=row.montoRestante
				defaultCurrencyUomId=parameters.orgCuClaveyUomId disableCurrencySelect=true align="right" />
	</tr>
	<#else>
	<tr>
		<@displayCell text=row.clavePresupuestal style="float:right" />
		<@displayCurrencyCell currencyUomId=Afectacion.currency amount=row.montoRestante />
	<tr>	
	</#if>
	</#list>
  
</table>
</@frameSection>

</@form>
<script type="text/javascript">
	var acctgTransTypeId = "${EventoContable.acctgTransTypeId}"; 
    opentaps.addOnLoad(cambiaEventoTodos(acctgTransTypeId,'3'));
</script>