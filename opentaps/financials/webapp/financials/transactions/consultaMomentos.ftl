<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<script language="JavaScript" type="text/javascript">
<!-- //
	function cambiaTipo(){
		document.getElementById("performFind").value = "N";
		document.getElementById("consultaMomentosForm").submit();		
	}	
	// -->
</script>

<form id="consultaMomentosForm" name="consultaMomentosForm" method="post" action="">
	<@inputHidden id="performFind" name="performFind" value="Y"/>
	<table class="twoColumnForm">
	<tbody>
		<@inputSelectRow title=uiLabelMap.FinancialTipoClave key="id" displayField="id" list=tiposClave name="ingresoEgreso" defaultOptionText=ingresoEgreso?if_exists onChange="cambiaTipo()"/>
		<@displayCvesPresupRow tagTypes=tagTypes titleClass="requiredField" activas=false/>
		<@inputSelectRow title=uiLabelMap.FinancialMomento required=true list=momentos  displayField="description" name="momentoId" default=momentoId?if_exists />
		<@inputSubmitRow title=uiLabelMap.CommonFind />
	</tbody>
	</table>
</form>
