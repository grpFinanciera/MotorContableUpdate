<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#if GastoReserva.statusId == "OTORGADA_GR">
	<#assign comprobarButton><@submitFormLink form="comprobarSolicitudGastosReserva" text=uiLabelMap.GastosReservaComprobarButton /></#assign>
	<#assign comprobarButton>${comprobarButton}<@submitFormLinkConfirm form="devolucion100GastoReserva" text=uiLabelMap.DevolucionTotal /></#assign>
</#if>

<script language="JavaScript" type="text/javascript">
		//Funcion para eliminar la factura anadida
		function clickEliminaFactura(contador){
			confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar){	
				var gastosReservaId = document.getElementById('gastosReservaId').value;
				var detalleGId = document.getElementById('detalleGId'+contador).value;
				if (gastosReservaId && detalleGId) {
					var requestData = {'gastosReservaId' : gastosReservaId, 'detalleGId' : detalleGId};		    			    
		    		opentaps.sendRequest('eliminaFacturas', requestData, function(data){responseEliminaItems(data)});
		    	}	
		    }		
		}
		
		//Funcion para confirmar eliminacion de factura
		function responseEliminaItems(data){
			for (var key in data){
				if(data[key]=="ERROR"){
					alert("Ocurri\u00f3 un error al eliminar el elemento");
				}
				else{
					alert("El elemento se ha eliminado");
					var eliminaFact = document.getElementById('eliminaFact').submit();
					
				}	    		
			}		
		}
		
		function sumaTotal(){
			var subtotal = parseFloat(document.getElementById("montoSubtotalFactura").value);
			var iva = parseFloat(document.getElementById("iva").value);
			var total = document.getElementById("montoTotalFactura");
			if(isNaN(subtotal)){
				subtotal = 0;
			} 
			if(isNaN(iva)){
				iva = 0;
			} 
			total.value = subtotal + iva;		}
	
</script>
<@form name="devolucion100GastoReserva" url="devolucion100GastoReserva" gastosReservaId=GastoReserva.gastosReservaId />

<@form name="comprobarSolicitudGastosReserva" url="comprobarSolicitudGastosReserva" gastosReservaId=GastoReserva.gastosReservaId />
<@frameSection title=uiLabelMap.GastosReservaApplication extra=comprobarButton!''>
	<@form name="agregaDetalleGastos" url="agregaDetalleGastos">
		<table>
			<@inputHidden name="gastosReservaId" id="gastosReservaId" value=GastoReserva.gastosReservaId/>
			<@include location="component://gastosReserva/webapp/gastosReserva/gastosReserva/encabezadoGasto.ftl"/>
			<#if GastoReserva.statusId == "OTORGADA_GR">
				<tr><td colspan="2"><HR width=70% align="center"></td></tr>
				<tr><td></td></tr>
				<@inputTextRow name="facturaNota" title=uiLabelMap.GastosReservaFactNote size=30 maxlength="255" titleClass="requiredField"/>
				<@inputTextRow title=uiLabelMap.GastosReservaProveedor id="proveedor" name="proveedor" size=30 maxlength="255" titleClass="requiredField"/>
	  			<@inputCurrencyRow id="montoSubtotalFactura" name="montoSubtotalFactura" title=uiLabelMap.GastosReservaSubtotal disableCurrencySelect=true titleClass="requiredField" onChange="sumaTotal();"/>
	  			<@inputCurrencyRow id="iva" name="iva" title=uiLabelMap.GastosReservaIva disableCurrencySelect=true titleClass="requiredField" onChange="sumaTotal();"/>
	  			<@inputCurrencyRow id="montoTotalFactura" name="montoTotalFactura" title=uiLabelMap.GastosReservaTotal disableCurrencySelect=true titleClass="requiredField"/>
				<@inputSubmitRow title=uiLabelMap.GastosReservaAddButton/>
			</#if>
		</table>
	</@form>
</@frameSection>

<@form name="eliminaFact" id="eliminaFact" url="" gastosReservaId=GastoReserva.gastosReservaId/>

<@frameSection title=uiLabelMap.GastosReservaFacturasAdd>
	<#if FacturasProvName?has_content>
	<table border="0" width="100%">
	<thead>		
		<tr>
			<th>${uiLabelMap.GastosReservaFactNote}</th>
			<th>${uiLabelMap.GastosReservaProveedor}</th>
			<th>${uiLabelMap.GastosReservaSubtotal}</th>
			<th>${uiLabelMap.GastosReservaIva}</th>
			<th>${uiLabelMap.GastosReservaTotal}</th>
			<th></th>
			<th></th>			
		</tr>
	</thead>
	<tbody>
		<#assign count = 1 />
		<#assign montoTotal = 0 />
		<@inputHidden name="gastosReservaId" id="gastosReservaId" value=GastoReserva.gastosReservaId/>
		<#list FacturasProvName as facturas>
			<@inputHidden name="detalleGId${count}" id="detalleGId${count}" value=facturas.detalleGId/>
			<tr class="${tableRowClass(facturas_index)}">
				<@displayCell text=facturas.facturaNota/>
				<@displayCell text=facturas.groupName/>
				<@displayCurrencyCell currencyUomId=GastoReserva.tipoMoneda amount=facturas.montoSubtotalFactura/>
				<@displayCurrencyCell currencyUomId=GastoReserva.tipoMoneda amount=facturas.iva/>
				<@displayCurrencyCell currencyUomId=GastoReserva.tipoMoneda amount=facturas.montoTotalFactura/>
				<#if GastoReserva.statusId == "OTORGADA_GR" || GastoReserva.statusId == "COMPROBADA_GR" >
				<td align="center" width="7%">
					<a name="submitButton" id="elimina${count}"  class="subMenuButtonDangerous" onClick="clickEliminaFactura('${count}')">${uiLabelMap.CommonDelete}</a>
				</td>
				</#if>							
			</tr>
			<#assign count=count + 1/>
			<#assign montoTotal=montoTotal + facturas.montoTotalFactura/>
		</#list>
		<tr>
			<td align="right" colspan="3">				
			<@displayTitleCell title="Total"/>
			<@displayCurrencyCell currencyUomId=GastoReserva.tipoMoneda amount=montoTotal/>
			</td>
		</tr>
	</tbody>
	</table>
	</#if>
</@frameSection>
