<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<script language="JavaScript" type="text/javascript">

	function cambiaTipo()
	{
		document.getElementById("performFind").value = "N";
		document.getElementById("crearClavePresForm").submit();		
	}
	
	function creaClave()
	{
		document.crearClavePresForm.action = "<@ofbizUrl>crearClavePresRequest</@ofbizUrl>";
    	document.crearClavePresForm.submit();
	}
	
</script>

<@frameSection title=uiLabelMap.FinancialsCrearClavePres >

<form id="crearClavePresForm" name="crearClavePresForm" method="post" action="">
  <@inputHidden id="performFind" name="performFind" value="Y"/>
  <@inputHidden id="organizationPartyId" name="organizationPartyId" value="${organizationPartyId}"/>
  <table class="twoColumnForm">
    <tbody>	
      <@displayTitleCell title=uiLabelMap.FinancialTipoClave />
      <@inputSelectCell key="id" displayField="id" list=tiposClave name="ingresoEgreso" defaultOptionText=ingresoEgreso?if_exists onChange="cambiaTipo()"/>
      <@displayCvesPresupRow tagTypes=tagTypes titleClass="requiredField" />
      <@inputSubmitRow title=uiLabelMap.CommonCreate onClick="creaClave()" />
    </tbody>
  </table>
</form>
  
</@frameSection>