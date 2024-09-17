<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<script type="text/javascript">
	function actualizaEtiqueta(numero){
		var etiquetaId = document.getElementById("etiquetaId_o_"+numero).value;
		var descripcion = document.getElementById("descripcion_o_"+numero).value;
		var fromDate = document.getElementById("fromDate_o_"+numero).value;
		var thruDate = document.getElementById("thruDate_o_"+numero).value;
		
		var requestData = { 'etiquetaId' : etiquetaId, 'descripcion' : descripcion,
							'fromDate' : fromDate, 'thruDate' : thruDate };	
		opentaps.sendRequest('actualizaEtiqueta',requestData, function(data) {responseActualizar(data,"actualizado")});
		
	}
	
	function borrarEtiqueta(numero){
		var etiquetaId = document.getElementById("etiquetaId_o_"+numero).value;
		var requestData = {'etiquetaId' : etiquetaId};	
		opentaps.sendRequest('eliminarEtiqueta',requestData, function(data) {responseActualizar(data,"eliminado")});
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
					document.getElementById("administracionEtiqueta").submit();
				}
		}		
	}
</script>

<@form name="administracionEtiqueta" url="administracionEtiqueta" id="administracionEtiqueta"/>

<@frameSection title=uiLabelMap.PresupuestoEtiquetasConfiguradas >
	<@paginate name="listEtiqueta" list=listEtiqueta>
	<#noparse>
		<@navigationHeader/>
	</#noparse>
	</@paginate>
		<table class="listTable" style="max-width:100%">
			<thead>
			<tr class="listTableHeader">
				<th>${uiLabelMap.PresupuestoEtiquetaId}</th>
				<th>${uiLabelMap.CommonDescription}</th>
				<th>${uiLabelMap.CommonFromDate}</th>
				<th>${uiLabelMap.CommonThruDate}</th>
				<th/>
				<th/>
			</tr>
			</thead>
			<tbody>
				<#list listEtiqueta as etiqueta>
				<tr class="${tableRowClass(etiqueta_index)}" >
					<@inputHidden id="etiquetaId_o_${etiqueta_index}" name="etiquetaId" value=etiqueta.etiquetaId index=etiqueta_index />
					<@displayCell text=etiqueta.etiquetaId />
					<@inputTextCell id="descripcion" name="descripcion" size=50 maxlength="255" default=etiqueta.descripcion index=etiqueta_index/>
					<@inputDateCell id="fromDate_o_${etiqueta_index}" name="fromDate" default=etiqueta.fromDate index=etiqueta_index/>
					<@inputDateCell id="thruDate_o_${etiqueta_index}" name="thruDate" default=etiqueta.thruDate index=etiqueta_index/>
					<@inputButtonNoSubmitCell title="${uiLabelMap.CommonUpdate}" onClick="actualizaEtiqueta('${etiqueta_index}');"/>
					<@inputButtonNoSubmitConfirmCell title="${uiLabelMap.CommonDelete}" onClick="borrarEtiqueta('${etiqueta_index}');"/>
				</tr>
				</#list>
			</tbody>
		</table>
</@frameSection>