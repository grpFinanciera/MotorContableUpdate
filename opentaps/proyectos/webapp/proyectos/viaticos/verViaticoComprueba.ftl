<#--
 *@author     Jesus Ruiz
 *@since      1.0
-->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl" />
<#assign hostname = request.getServerName() />

<script language="JavaScript" type="text/javascript">
////Funcion para eliminar el registro de un comprobante de una solicitud
		function contactTypeClickEliminaComprobante(contador) 
		{confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar) 
			{	var conceptoViaticoMontoProyId = document.getElementById('conceptoViaticoMontoProyId'+contador).value;
				if (conceptoViaticoMontoProyId != "") 
		    	{ 	var requestData = {'viaticoProyectoId' : viaticoProyectoId, 'conceptoViaticoMontoProyId' : conceptoViaticoMontoProyId};		    			    
		    		opentaps.sendRequest('eliminaComprobanteViaticoProy', requestData, function(data) {responseEliminaItems(data)});
		    	}				
			}													
		}
		
		//Functions load the response from the server
		function responseEliminaItems(data) 
		{	for (var key in data) 
			{	if(data[key]=="ERROR")
				{	alert("Ocurri\u00f3 un error al eliminar el elemento");
				}
				else
				{	alert("El elemento se ha eliminado");
					var conceptoViaticoMontoProyId = document.getElementById('comprobarViatico').submit();
				}	    		
			}		
		}
</script>

<#assign urlHost = request.getServerName()+":"+request.getServerPort()/>

<#if ViaticoProyecto?has_content && ViaticoProyecto.viaticoProyectoId?exists>
<#assign LinkActualizar><@submitFormLink form="generaPolizaComprobada" text="Comprobar" />
<@submitFormLinkConfirm form="devolverViaticoProyecto" text="Devolver al 100%" /></#assign>
<form method="post" action="<@ofbizUrl>devolverViaticoProyecto</@ofbizUrl>" name="devolverViaticoProyecto" style="margin: 0;" id="devolverViaticoProyecto">
			<@inputHidden name="urlHost" value=urlHost/>		
			<@inputHidden name="viaticoProyectoId" value=ViaticoProyecto.viaticoProyectoId/>			
		</form>
<form method="post" action="<@ofbizUrl>generaPolizaComprobada</@ofbizUrl>" name="generaPolizaComprobada" style="margin: 0;" id="generaPolizaComprobada">
			<@inputHidden name="viaticoProyectoId" value=ViaticoProyecto.viaticoProyectoId/>
			<@inputDateRow name="fechaContable" title=uiLabelMap.Date form="generaPolizaComprobada" titleClass="requiredField" />
			<tr>
		       	<td align="right" class="titleCell">
				<@display text="Evento comprobación" class="requiredField" />
				</td>
		       	<@inputSelectCell name="eventoComprueba" list=eventosComprueba?if_exists?sort_by("descripcion") id="eventoComprueba" key="acctgTransTypeId" displayField="descripcion" required=true defaultOptionText="" />
				<td align="right" class="titleCell">
				<@display text="Monto a comprobar" class="requiredField" />
				</td>
				<@inputCurrencyCell name="montoComprueba" disableCurrencySelect=true/>
    		</tr>
		</form>
<@frameSection title=uiLabelMap.ViaticosHome extra=LinkActualizar>
<form method="post" action="<@ofbizUrl>comprobarViatico</@ofbizUrl>" name="comprobarViatico" style="margin: 0;" id="comprobarViatico">

	<table width="100%">
		<@inputHidden name="viaticoProyectoId" value=ViaticoProyecto.viaticoProyectoId/>
		<@inputHidden name="motivo" value=ViaticoProyecto.motivo/>		        
		<@displayRow title=uiLabelMap.ViaticoId text=ViaticoProyecto.viaticoProyectoId/>
		<@displayRow title=uiLabelMap.ViaticoJustification text=ViaticoProyecto.motivo/>
		<@displayRow title=uiLabelMap.CommonCurrency text=ViaticoProyecto.tipoMoneda/>
		<@displayCurrencyRow title=uiLabelMap.DailyAmount currencyUomId=ViaticoProyecto.tipoMoneda amount=ViaticoProyecto.montoDiario class="tabletext"/>
		<@displayCurrencyRow title=uiLabelMap.FieldAmount currencyUomId=ViaticoProyecto.tipoMoneda amount=ViaticoProyecto.montoTrabCampo class="tabletext"/>
		<@displayCurrencyRow title=uiLabelMap.PurchTransportAmount currencyUomId=ViaticoProyecto.tipoMoneda amount=ViaticoProyecto.montoTransporte class="tabletext"/>
		<#assign geoO = ViaticoProyecto.getRelatedOne("GeoOrigenGeo")/>
		<@displayRow title=uiLabelMap.PurchOrigin text=geoO.geoName/>
		<#assign geoD = ViaticoProyecto.getRelatedOne("GeoDestinoGeo")/>
		<@displayRow title=uiLabelMap.PurchDestination text=geoD.geoName/>
		
		<@displayDateRow title=uiLabelMap.CommonStartDate date=ViaticoProyecto.fechaInicial! format="DATE" />
		<@displayDateRow title=uiLabelMap.CommonEndDate date=ViaticoProyecto.fechaFinal! format="DATE" />
</form>
	</table>
</@frameSection>
<@frameSection title=uiLabelMap.ViaticoComprobantes>
	<form method="post" action="<@ofbizUrl>agregaComprobanteViaticoProy</@ofbizUrl>" name="agregaComprobanteViaticoProy" style="margin: 0;">
		<table border="0"  width="100%">
			<@inputHidden name="viaticoProyectoId" value=ViaticoProyecto.viaticoProyectoId/>
			<@inputDateRow name="fecha" title=uiLabelMap.Date form="agregaComprobanteViaticoProy" titleClass="requiredField" />
			<@inputSelectRow name="conceptoViatico" title=uiLabelMap.ConceptoViatico
				list=conceptosViaticos! key="conceptoViaticoId" displayField="descripcion" titleClass="requiredField" />
			<tr>
			<@displayTitleCell title=uiLabelMap.ViaticoReferenciaFactura titleClass="requiredField"/>
			<@inputTextCell name="referencia" maxlength="20" size=20/>
			</tr>
			<tr>
			<@displayTitleCell title=uiLabelMap.ViaticoDescripcion titleClass="requiredField"/>
			<@inputTextCell name="descripcion" maxlength="255" size=50/>
			</tr>
			<@inputSelectRow name="gastoProyecto" title="Descripcion del gasto"
				list=ObjetoGastoProyecto! key="gastoProyId" displayField="nombreObjetoGasto" titleClass="requiredField" />
			<@inputCurrencyRow name="monto" title=uiLabelMap.ViaticoAmount disableCurrencySelect=true titleClass="requiredField"/>	
			<@inputSubmitRow title=uiLabelMap.CommonAdd />			
		</table>
	</form>
</@frameSection>
<@frameSection title=uiLabelMap.ViaticoConceptoAdded >
	<#if Comprobantes?has_content>
	
	<table border="0" width="100%">
				
	<thead>		
		<tr>
			<th>${uiLabelMap.PurchViaticoItemId}</th>
			<th>${uiLabelMap.Date}</th>
			<th>${uiLabelMap.ConceptoViatico}</th>
			<th>${uiLabelMap.ViaticoReferenciaFactura}</th>
			<th>${uiLabelMap.ViaticoDescripcion}</th>
			<th>${uiLabelMap.ViaticoAmount}</th>			
			<th></th>
			<th></th>			
		</tr>
	</thead>
	<tbody>
		<#assign count = 1 />
		<#assign montoTotal = 0 />
		<#list Comprobantes as comprobante>		
				<@inputHidden name="viaticoProyectoId" id="viaticoProyectoId" value=ViaticoProyecto.viaticoProyectoId/>
				<@inputHidden name="conceptoViaticoMontoProyId${count}" id="conceptoViaticoMontoProyId${count}" value=comprobante.conceptoViaticoMontoProyId/>
						<tr class="${tableRowClass(comprobante_index)}">
							<@displayCell text=comprobante.conceptoViaticoMontoProyId/>
							<@displayDateCell date=comprobante.fecha format="DATE"/>
							<#assign concepto=comprobante.getRelatedOne("ConceptoViatico")/>
							<@displayCell text=concepto.descripcion/>
							<@displayCell text=comprobante.referencia/>
							<@displayCell text=comprobante.descripcion/>
							<@displayCurrencyCell currencyUomId=ViaticoProyecto.tipoMoneda  amount=comprobante.monto/>
							<td align="center" width="7%">
								<input name="submitButton" id="elimina${count}"" type="submit" class="subMenuButtonDangerous" value="${uiLabelMap.ViaticoElimiarItem}" onClick="contactTypeClickEliminaComprobante('${count}')"/>								
							</td>							
						</tr>
			<#assign count=count + 1/>
			<#assign montoTotal=montoTotal + comprobante.monto/>	
		</#list>
		<tr>
		
			<td align="right" colspan="4">				
			<@displayTitleCell title="Total"/>
			<@displayCurrencyCell currencyUomId=ViaticoProyecto.tipoMoneda  amount=montoTotal/>
			</td>
		</tr>
	</tbody>
	</table>
	</#if>
</@frameSection>
</#if>