<#--
 *@author     Jesus Ruiz
 *@since      1.0
-->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl" />
<#assign hostname = request.getServerName() />

<script language="JavaScript" type="text/javascript">
////Funcion para eliminar el registro de un comprobante de una solicitud
</script>

<#if ViaticoProyecto?has_content && ViaticoProyecto.viaticoProyectoId?exists>
<#assign LinkActualizar><@submitFormLink form="generaPolizaDevo" text="Comprobar" /></#assign>
<form method="post" action="<@ofbizUrl>generaPolizaDevo</@ofbizUrl>" name="generaPolizaDevo" style="margin: 0;" id="generaPolizaDevo">
			<@inputHidden name="viaticoProyectoId" value=ViaticoProyecto.viaticoProyectoId/>
			<@inputDateRow name="fechaContable" title=uiLabelMap.Date form="generaPolizaDevo" titleClass="requiredField" />
			<tr>
		       	<td align="right" class="titleCell">
				<@display text="Evento devolución" class="requiredField" />
				</td>
		       	<@inputSelectCell name="eventoDevuelve" list=eventosDevo?if_exists?sort_by("descripcion") id="eventoDevuelve" key="acctgTransTypeId" displayField="descripcion" required=true defaultOptionText="" />
    		</tr>
		</form>
<@frameSection title=uiLabelMap.ViaticosHome extra=LinkActualizar>
<form method="post" action="<@ofbizUrl>devolverViatico</@ofbizUrl>" name="devolverViatico" style="margin: 0;" id="devolverViatico">

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