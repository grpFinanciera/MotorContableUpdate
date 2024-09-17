<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<script type="text/javascript">

	function getElementos(){
		var tipoReporteSelect = document.getElementById('tipoReporteId');
		var tipoReporteId = tipoReporteSelect.options[tipoReporteSelect.selectedIndex].value;
		if(tipoReporteId){
			var requestData = {'tipoReporteId' : tipoReporteId};		    			    
			opentaps.sendRequest('getElementosXML', requestData, function(data) {cargaElementos(data,tipoReporteId)});
		} else {
			ocultaCampos();
			limpiaElementos();
		}

	}
	
	function cargaElementos(data,tipoReporteId){
		var select = document.getElementById('elementoReporteId'); 
		select.innerHTML = "";
		var i = 0;
		for (var key in data) 
		{	
			select.options[i] = new Option(data[key]);
			select.options[i].value = key;					
			i++;
		}	

		if(tipoReporteId == 'FINANCIERO'){
			ocultaCampo('enumId','true');
			ocultaCampo('glAccountId','false');
		} else {
			ocultaCampo('enumId','false');
			ocultaCampo('glAccountId','true');
		}
		
	}
	
	function ocultaCampo(/*String*/idCampo,/*String*/oculta){
		var trStyle = document.getElementById(idCampo).parentNode.parentNode.style;
		if(oculta=='true'){
			trStyle.display = 'none';
		} else if(oculta=='false') {
			trStyle.display = 'table-row';
		}
	}
	
	function ocultaCampos(){
		ocultaCampo('glAccountId','true');
		ocultaCampo('enumId','true');
	}
	
	function limpiaElementos(){
		document.getElementById('elementoReporteId').options.length = 0;
	}
	
	window.onload = function cargaInicial(){
		ocultaCampos();
	}
	

	
</script>
<@frameSection title=uiLabelMap.ConfiguraReporteXML >
<@form name="guardaElementoXML" url="guardaElementoXML" >
	<table>
		<@inputSelectRow title=uiLabelMap.FinancialsReporte id="tipoReporteId" name="tipoReporteId" list=listReportes?default([]) key="tipoReporteId" displayField="nombre" onChange="javascript:getElementos();" required=false ignoreParameters=true/>
		<@inputSelectRow title=uiLabelMap.FinancialsReporteElemento id="elementoReporteId" name="elementoReporteId" list=[] key="elementoReporteId" displayField="elementoReporteId" />
		<@inputAutoCompleteGlAccountRow title=uiLabelMap.FinancialsGlAccount name="glAccountId" id="glAccountId" />
		<@inputAutoCompleteEnumerationRow name="enumId" id="enumId" title=uiLabelMap.classification form="guardaElementoXML" />
		<tr><td colspan="2" align="center"><input type="submit" class="subMenuButton" value="${uiLabelMap.CommonSave}" /></td></tr>
	</table>
</@form>
</@frameSection>