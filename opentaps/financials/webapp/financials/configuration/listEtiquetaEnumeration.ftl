<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<script type="text/javascript">

	
	function borrarEtiqueta(numero){
			var etiquetaId = document.getElementById("etiquetaId_o_"+numero).value;
			var enumId = document.getElementById("enumId_o_"+numero).value;
			var requestData = {'etiquetaId' : etiquetaId, 'enumId' : enumId};	
			opentaps.sendRequest('eliminarEtiquetaEnum',requestData, function(data) {responseActualizar(data,"eliminado")});
	}
	
	function responseActualizar(data, mensaje) 
	{
		for (var key in data) 
		{
				if(data[key].indexOf("ERROR") != -1)
				{
					var Error = data[key].split(':');
					if(Error[1]){
						alert(Error[1]);
					} else {
						alert("Ocurri\u00f3 un error el realizar la operaci\u00f3n");
					}
				}
				else
				{
					alert("El elemento se ha "+mensaje+" correctamente");
					document.getElementById("asociarEtiqueta").submit();
				}
		}		
	}
</script>

<@form name="asociarEtiqueta" url="asociarEtiqueta" id="asociarEtiqueta"/>

<@frameSection title=uiLabelMap.PresupuestoEtiquetasConfiguradas >
		<table class="listTable" style="max-width:100%">
			<thead>
			<tr class="listTableHeader">
				<th>${uiLabelMap.FinancialsClasificacion}</th>
				<th>${uiLabelMap.PresupuestoEtiqueta}</th>
				<th/>
			</tr>
			</thead>
			<tbody>
				<#list listEtiquetaEnumeration as etiquetaEnum>
				<tr class="${tableRowClass(etiquetaEnum_index)}" >
					<@inputHidden id="etiquetaId_o_${etiquetaEnum_index}" name="etiquetaId" value=etiquetaEnum.etiquetaId index=etiquetaEnum_index />
					<@inputHidden id="enumId_o_${etiquetaEnum_index}" name="enumId" value=etiquetaEnum.enumId index=etiquetaEnum_index />
					<@displayCell text=etiquetaEnum.enumId+' - '+etiquetaEnum.getRelatedOne("Enumeration").getString("description") />
					<@displayCell text=etiquetaEnum.etiquetaId+' - '+etiquetaEnum.getRelatedOne("Etiqueta").getString("descripcion") />
					<@inputButtonNoSubmitConfirmCell title="${uiLabelMap.CommonDelete}" onClick="borrarEtiqueta('${etiquetaEnum_index}');"/>
				</tr>
				</#list>
			</tbody>
		</table>
</@frameSection>