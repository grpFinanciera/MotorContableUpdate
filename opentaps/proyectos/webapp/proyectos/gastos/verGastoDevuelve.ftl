<#--
 *@author     Jesus Ruiz
 *@since      1.0
-->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl" />
<#assign hostname = request.getServerName() />

<script language="JavaScript" type="text/javascript">
//Funcion para eliminar la factura anadida
		
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

<#if GastoProyecto?has_content && GastoProyecto.gastoProyectoId?exists>
<#assign LinkActualizar><@submitFormLink form="generaPolizaGastoDevo" text="Comprobar" /></#assign>
<form method="post" action="<@ofbizUrl>generaPolizaGastoDevo</@ofbizUrl>" name="generaPolizaGastoDevo" style="margin: 0;" id="generaPolizaGastoDevo">
<table width="100%">
			<@inputHidden name="gastoProyectoId" value=GastoProyecto.gastoProyectoId/>
			<@inputDateRow name="fechaContable" title=uiLabelMap.Date form="generaPolizaGastoDevo" titleClass="requiredField" />
			<tr>
		       	<td align="right" class="titleCell">
				<@display text="Evento devolución" class="requiredField" />
				</td>
		       	<@inputSelectCell name="eventoDevuelve" list=eventosDevo?if_exists?sort_by("descripcion") id="eventoDevuelve" key="acctgTransTypeId" displayField="descripcion" required=true defaultOptionText="" />
    		</tr>
</table>
		</form>
<@frameSection title=uiLabelMap.ViaticosHome extra=LinkActualizar>
<form method="post" action="<@ofbizUrl>devolverGasto</@ofbizUrl>" name="devolverGasto" style="margin: 0;" id="devolverGasto">

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