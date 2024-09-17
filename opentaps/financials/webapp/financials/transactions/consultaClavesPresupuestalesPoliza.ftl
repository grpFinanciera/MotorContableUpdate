<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<script language="JavaScript" type="text/javascript">
<!-- //
	function cambiaTipo(){
		document.getElementById("performFind").value = "N";
		document.getElementById("ConsultaClavesPresupuestalesPoliza").submit();		
	}	
	// -->
</script>

<form id="ConsultaClavesPresupuestalesPoliza" name="ConsultaClavesPresupuestalesPoliza" method="post" action="">
  <@inputHidden id="performFind" name="performFind" value="Y"/>
  <table class="twoColumnForm">
    <tbody>
      <@displayTitleCell title=uiLabelMap.FinancialTipoClave />
      <@inputSelectCell key="id" displayField="id" list=tiposClave name="ingresoEgreso" defaultOptionText=ingresoEgreso?if_exists onChange="cambiaTipo()"/>
	  <@inputSelectRow title=uiLabelMap.FinancialMomento required=false list=momentos  displayField="description" name="momentoId" default=momentoId?if_exists />
      <@displayCvesPresupRow tagTypes=tagTypes titleClass="requiredField"/>
	  <@inputDateRangeRow title=uiLabelMap.FinancialsFechaContable fromName="fechaInicialPeriodo" thruName="fechaFinalPeriodo" />
	  <@inputTextRow title=uiLabelMap.NumeroDePoliza name="poliza" size="20" maxlength="20"/>
      <@inputSelectRow title=uiLabelMap.FinancialsTipoPoliza required=false list=listaTipoPoliza  displayField="description" name="tipoPolizaId" key="tipoPolizaId" />	
	  <@displayTitleCell title=uiLabelMap.FinancialsMovimiento/>
	  <@inputSelectHashCell hash= {"D":"Cargo", "C":"Abono", "A":"Ambos"} id="tipoMovimiento" name="tipoMovimiento"/>	
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>