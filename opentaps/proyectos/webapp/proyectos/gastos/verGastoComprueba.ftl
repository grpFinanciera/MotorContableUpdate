<#--
 *@author     Jesus Ruiz
 *@since      1.0
-->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl" />
<#assign hostname = request.getServerName() />

<script language="JavaScript" type="text/javascript">
//Funcion para eliminar la factura anadida
		function clickEliminaFactura(contador){
			confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar){	
				var gastoProyectoId = document.getElementById('gastoProyectoId').value;
				var detalleGId = document.getElementById('detalleGId'+contador).value;
				if (gastoProyectoId && detalleGId) {
					var requestData = {'gastoProyectoId' : gastoProyectoId, 'detalleGId' : detalleGId};		    			    
		    		opentaps.sendRequest('eliminaFacturasProy', requestData, function(data){responseEliminaItems(data)});
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
			total.value = subtotal + iva;
		}
</script>

<#assign urlHost = request.getServerName()+":"+request.getServerPort()/>

<#if GastoProyecto?has_content && GastoProyecto.gastoProyectoId?exists>
<#assign LinkActualizar><@submitFormLink form="generaPolizaGastoComprobada" text="Comprobar" />
<@submitFormLinkConfirm form="devolverViaticoGasto" text="Devolver al 100%" /></#assign>
<form method="post" action="<@ofbizUrl>devolverViaticoGasto</@ofbizUrl>" name="devolverViaticoGasto" style="margin: 0;" id="devolverViaticoGasto">
			<@inputHidden name="urlHost" value=urlHost/>		
			<@inputHidden name="gastoProyectoId" value=GastoProyecto.gastoProyectoId/>			
		</form>
<form method="post" action="<@ofbizUrl>generaPolizaGastoComprobada</@ofbizUrl>" name="generaPolizaGastoComprobada" style="margin: 0;" id="generaPolizaGastoComprobada">
<table width="100%">
			<@inputHidden name="gastoProyectoId" value=GastoProyecto.gastoProyectoId/>
			<@inputDateRow name="fechaContable" title=uiLabelMap.Date form="generaPolizaGastoComprobada" titleClass="requiredField" />
			<tr>
		       	<td align="right" class="titleCell">
				<@display text="Evento comprobación" class="requiredField" />
				</td>
		       	<@inputSelectCell name="eventoComprueba" list=eventosComprueba?if_exists?sort_by("descripcion") id="eventoComprueba" key="acctgTransTypeId" displayField="descripcion" required=true defaultOptionText="" />
			</tr>
			<tr>
				<td align="right" class="titleCell">
				<@display text="Monto a comprobar" class="requiredField" />
				</td>
				<@inputCurrencyCell name="montoComprueba" disableCurrencySelect=true/>
    		</tr>
</table>
		</form>
<@frameSection title=uiLabelMap.GastosReservaApplication extra=LinkActualizar>
<form method="post" action="<@ofbizUrl>comprobarGastoProyecto</@ofbizUrl>" name="comprobarGastoProyecto" style="margin: 0;" id="comprobarGastoProyecto">

	<table width="100%">
		<@inputHidden name="gastoProyectoId" value=GastoProyecto.gastoProyectoId/>
		<@inputHidden name="concepto" value=GastoProyecto.concepto/>		        
		<@displayRow title="Id Gasto" text=GastoProyecto.gastoProyectoId/>
		<@displayRow title="Concepto" text=GastoProyecto.concepto/>
		<@displayRow title=uiLabelMap.CommonCurrency text=GastoProyecto.tipoMoneda/>
		<@displayCurrencyRow title="Monto Solicitado" currencyUomId=GastoProyecto.tipoMoneda amount=GastoProyecto.monto class="tabletext"/>
		
		<@displayDateRow title="Fecha" date=GastoProyecto.fecha! format="DATE" />
</form>
	</table>
</@frameSection>
<@form name="comprobarSolicitudGastosReserva" url="comprobarSolicitudGastosReserva" gastoProyectoId=GastoProyecto.gastoProyectoId />
<@frameSection title=uiLabelMap.GastosReservaApplication extra=comprobarButton!''>
	<@form name="agregaDetalleGastosProyectos" url="agregaDetalleGastosProyectos">
		<table>
			<@inputHidden name="gastoProyectoId" id="gastoProyectoId" value=GastoProyecto.gastoProyectoId/>
				<tr><td></td></tr>
				<@inputTextRow name="facturaNota" title=uiLabelMap.GastosReservaFactNote size=30 maxlength="255" titleClass="requiredField"/>
				<@inputTextRow title=uiLabelMap.GastosReservaProveedor id="proveedor" name="proveedor" size=30 maxlength="255" titleClass="requiredField"/>
	  			<@inputCurrencyRow id="montoSubtotalFactura" name="montoSubtotalFactura" title=uiLabelMap.GastosReservaSubtotal disableCurrencySelect=true titleClass="requiredField" onChange="sumaTotal();"/>
	  			<@inputCurrencyRow id="iva" name="iva" title=uiLabelMap.GastosReservaIva disableCurrencySelect=true titleClass="requiredField" onChange="sumaTotal();"/>
	  			<@inputCurrencyRow id="montoTotalFactura" name="montoTotalFactura" title=uiLabelMap.GastosReservaTotal disableCurrencySelect=true titleClass="requiredField"/>
				<@inputSelectRow name="gastoProyecto" title="Descripcion del gasto"
				list=ObjetoGastoProyecto! key="gastoProyId" displayField="nombreObjetoGasto" titleClass="requiredField" />
				<@inputSubmitRow title=uiLabelMap.GastosReservaAddButton/>
		</table>
	</@form>
</@frameSection>

<@form name="eliminaFact" id="eliminaFact" url="" gastosReservaId=GastoProyecto.gastoProyectoId/>

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
		<@inputHidden name="gastoProyectoId" id="gastoProyectoId" value=GastoProyecto.gastoProyectoId/>
		<#list FacturasProvName as facturas>
			<@inputHidden name="detalleGId${count}" id="detalleGId${count}" value=facturas.detalleGId/>
			<tr class="${tableRowClass(facturas_index)}">
				<@displayCell text=facturas.facturaNota/>
				<@displayCell text=facturas.proveedor/>
				<@displayCurrencyCell currencyUomId=GastoProyecto.tipoMoneda amount=facturas.montoSubtotalFactura/>
				<@displayCurrencyCell currencyUomId=GastoProyecto.tipoMoneda amount=facturas.iva/>
				<@displayCurrencyCell currencyUomId=GastoProyecto.tipoMoneda amount=facturas.montoTotalFactura/>
				<td align="center" width="7%">
					<a name="submitButton" id="elimina${count}"  class="subMenuButtonDangerous" onClick="clickEliminaFactura('${count}')">${uiLabelMap.CommonDelete}</a>
				</td>						
			</tr>
			<#assign count=count + 1/>
			<#assign montoTotal=montoTotal + facturas.montoTotalFactura/>
		</#list>
		<tr>
			<td align="right" colspan="3">				
			<@displayTitleCell title="Total"/>
			<@displayCurrencyCell currencyUomId=GastoProyecto.tipoMoneda amount=montoTotal/>
			</td>
		</tr>
	</tbody>
	</table>
	</#if>
</@frameSection>
</#if>