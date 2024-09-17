<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<script language="JavaScript" type="text/javascript">
		//Funcion para eliminar la clave presupuestal
		function clickEliminaClave(contador){
			confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar){	
				var gastosReservaId = document.getElementById('gastosReservaId').value;
				var detallePresId = document.getElementById('detallePresId'+contador).value;
				if (gastosReservaId && detallePresId) {
					var requestData = {'gastosReservaId' : gastosReservaId, 'detallePresId' : detallePresId};		    			    
		    		opentaps.sendRequest('eliminaDetallePresupuesto', requestData, function(data){responseEliminaDetallePresupuesto(data)});
		    	}	
		    }		
		}
		
		//Funcion para confirmar eliminacion de factura
		function responseEliminaDetallePresupuesto(data){
			for (var key in data){
				if(data[key].indexOf("ERROR") != -1)
				{
					var Error = data[key].split(':');
					if(Error[1]){
						alert(Error[1]);
					} else {
						alert("Ocurri\u00f3 un error al eliminar el registro");
					}
				}
				else
				{
					alert("El elemento se ha eliminado");
					document.getElementById('presupuestarSolicitudPresupuestoScreen').submit();
				}	    		
			}		
		}
		
</script>

<@include location="component://gastosReserva/webapp/gastosReserva/gastosReserva/comprobarSolicitud.ftl"/>

<@form name="presupuestarSolicitudPresupuestoScreen" id="presupuestarSolicitudPresupuestoScreen" url="presupuestarSolicitudPresupuestoScreen" gastosReservaId=GastoReserva.gastosReservaId/>

<@form name="presupuestarSolicitudGastosReserva" url="presupuestarSolicitudGastosReserva" gastosReservaId=GastoReserva.gastosReservaId/>

<#if GastoReserva.statusId == "COMPROBADA_GR">
	<#assign comprobarButton><@submitFormLink form="presupuestarSolicitudGastosReserva" text=uiLabelMap.GastosReservaComprobarButton /></#assign>
</#if>

<@frameSection title=uiLabelMap.GastosReservaPresupuestar extra=comprobarButton!''>
<@form name="agregaDetalleGastoPresupuesto" url="agregaDetalleGastoPresupuesto" gastosReservaId=GastoReserva.gastosReservaId >
	<table>
		<@inputDateRow name="fecha" title=uiLabelMap.CommonDate form="agregaDetalleGastoPresupuesto" titleClass="requiredField"/>
		<@inputCurrencyRow name="monto" title=uiLabelMap.GastosReservaMotComprobar disableCurrencySelect=true titleClass="requiredField"/>
		<#if tagTypes?has_content>
			<@displayCvesPresupRow tagTypes=tagTypes titleClass="requiredField"/>
		</#if>
		<@inputSubmitRow title=uiLabelMap.CommonAdd />
	</table>
</@form>
</@frameSection>

<@frameSection title=uiLabelMap.GastosReservaClavesAgregadas >
<#if listPresupuesto?has_content>
	<table border="0" width="100%">
	<thead>		
		<tr>
			<th>${uiLabelMap.GastosReservaDetallePresupuestoId}</th>
			<th>${uiLabelMap.GastoReservadoId}</th>
			<th>${uiLabelMap.CommonDate}</th>
			<th>${uiLabelMap.ClavePresupuestal}</th>
			<th>${uiLabelMap.CommonAmount}</th>			
			<th></th>
			<th></th>
	</tr>
	</thead>
	<tbody>
	<#assign total = 0 />	
	<#assign count = 1 />
	<#list listPresupuesto as DetallePresupuesto>
		<tr class="${tableRowClass(DetallePresupuesto_index)}">
			<@inputHidden name="detallePresId${count}" id="detallePresId${count}" value=DetallePresupuesto.detallePresId />
			<@displayCell text=DetallePresupuesto.detallePresId />
			<@displayCell text=DetallePresupuesto.gastosReservaId />
			<@displayDateCell date=DetallePresupuesto.fecha format="DATE"/>
			<@displayCell text=DetallePresupuesto.clavePresupuestal/>
			<@displayCurrencyCell amount=DetallePresupuesto.monto currencyUomId=GastoReserva.tipoMoneda class="textright"/> 
			<#if GastoReserva.statusId == "COMPROBADA_GR" >
			<td align="center" width="7%">
				<a name="submitButton" id="elimina${count}"  class="subMenuButtonDangerous" onClick="clickEliminaClave('${count}')">${uiLabelMap.CommonDelete}</a>								
			</td>
			</#if>	
		</tr>
	<#assign count=count + 1/>
	<#assign total = total + DetallePresupuesto.monto  />	
	</#list>
	<tr>	
		<td align="right" colspan="3">
		<@displayTitleCell title=uiLabelMap.CommonTotal />
		<@displayCurrencyCell amount=total currencyUomId=GastoReserva.tipoMoneda class="textright"/>
	</tr>
	</tbody>
</#if>
</@frameSection>

