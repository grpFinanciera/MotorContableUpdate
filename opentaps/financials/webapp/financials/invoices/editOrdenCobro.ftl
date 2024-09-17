<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#assign urlHost = request.getServerName()+":"+request.getServerPort()/>
<#assign moneda = "" />
<#if datosInvoice?has_content>
	<#list datosInvoice as invo>
		<#if invo.statusId == 'INVOICE_IN_PROCESS' || invo.statusId == 'DEVENGADO_PARCIAL'>								
			<#assign LinkEnviarMotor><@submitFormLink form="enviarOrdenIngresosDevengo" text=uiLabelMap.CommonEnviar /></#assign>													
		</#if>
	</#list>
</#if>
<@form name="enviarOrdenIngresosDevengo" id="enviarOrdenIngresosDevengo" url="enviarOrdenIngresosDevengo" >
<@frameSection title=uiLabelMap.FinancialsCrearOrdenCobro extra=LinkEnviarMotor>	
		<table>								
			<#if datosInvoice?has_content>
				<#list datosInvoice as invoice>	 
					<@displayRow title=uiLabelMap.FinancialsPreOrden text=invoice.invoiceId/>
					<input type="hidden" name="invoiceId" id="invoiceId" value="${invoiceId}"/>	
					<input type="hidden" name="organizationPartyId" id="organizationPartyId" value="${organizationPartyId}"/>
					<input type="hidden" name="moneda" id="moneda" value="${invoice.currencyUomId}"/>
					<input type="hidden" name="descripcion" id="descripcion" value="${invoice.description!}"/>
					<@displayRow title=uiLabelMap.Descripcion text=invoice.description/>					
					<@displayRow title=uiLabelMap.FinancialsAccountigDate text=invoice.invoiceDate/>
					<tr>
					    <@displayTitleCell title=uiLabelMap.FechaDeCobro titleClass="requiredField" />
					    <@inputDateTimeCell name="fechaCobro"  />
  					</tr>
					<@displayRow title=uiLabelMap.FinancialsIdProveedor text=invoice.partyId/>
					<@displayRow title=uiLabelMap.FinancialsNombreProveedor text=invoice.groupName/>
					<@displayRow title=uiLabelMap.FinancialsAcctgTransTypeId text=invoice.acctgTransTypeId/>
					<@displayRow title=uiLabelMap.AccountingTipoDocumento text=invoice.descripcion/>					
					<@displayRow title=uiLabelMap.FinancialsInvoiceTotal text=invoice.invoiceTotal/>
					<@displayRow title=uiLabelMap.FinancialsMoneda text=invoice.currencyUomId/>
					<#assign moneda = invoice.currencyUomId />
					<td>
						<th>
				        	<hr><@displayTooltip text=uiLabelMap.FinancialsEventoParaRecaudado />
					  	</th>
					</td>
					<@inputSelectRow title=uiLabelMap.FinancialsNombreEventoParaRecaudado name="acctgTransTypeId" list=eventos?sort_by("descripcion") required=false id="acctgTransTypeId" key="acctgTransTypeId" displayField="descripcion" onChange="cambiaEvento(this.value,'10');" titleClass="requiredField"/>																	
				</#list>
	    	</#if>	    		
		</table>
</@frameSection>


<@frameSection title=uiLabelMap.AccountingInvoiceItems>
	<#if invoiceItems?has_content>
		<table width="100%">
				
		<thead>		
			<tr>
				<th>${uiLabelMap.FormFieldTitle_invoiceItemSeqId}</th>
				<th>${uiLabelMap.ProductProductId}</th>
				<th>${uiLabelMap.CommonDescription}</th>
				<th>${uiLabelMap.CommonQuantity}</th>
				<th>${uiLabelMap.CommonAmount}</th>
				<th>${uiLabelMap.CommonTotal}</th>
				<th>${uiLabelMap.MontoRestante}</th>
				<th>${uiLabelMap.MontoAPagar}</th>			
				<th></th>								
			</tr>
		</thead>
		<tbody>
			<#assign count = 1 />			
			
			<#list invoiceItems as items>
				
				<#assign organizationPartyId = "${organizationPartyId}" />
				
				<input type="hidden" name="invoiceItemSeqId${count}" id="invoiceItemSeqId${count}" value="${items.invoiceItemSeqId}"/>				
				<input type="hidden" name="productId${count}" id="productId${count}" value="${items.productId}"/>
					<tr class="${tableRowClass(items_index)}" name="item" >
						<@displayCell text=items.invoiceItemSeqId/>
						<@displayCell text=items.productId />
						<@displayCell text=items.description?if_exists?default("")/>
						<@displayCell text=items.quantity?if_exists?default("")/>												
						<@displayCurrencyCell currencyUomId=items.uomId amount=items.amount currencyUomId=moneda />						
						<@displayCurrencyCell currencyUomId=items.uomId amount=items.amount*items.quantity currencyUomId=moneda/>
						<@displayCurrencyCell currencyUomId=items.uomId amount=items.montoRestante currencyUomId=moneda/>
						<@inputCurrencyCell name="amount${count}" defaultCurrencyUomId=moneda disableCurrencySelect=true default=items.montoRestante/>												
						<#if (items.montoRestante > 0)>
														
						</#if>							
					</tr>
																			
					<tr class="${tableRowClass(items_index)}">
						<td colspan="8" valign="top">
						<@flexArea targetId="ClavePresupuestal${items.invoiceItemSeqId}" title=uiLabelMap.ClavePresupuestal controlStyle="font-weight: bold;" >
							<table border="0" cellpadding="0" cellspacing="0" width="100%">
								<tr>
								<#if tagTypes?has_content>
									<@clavesPresupDisplay tags=tagTypes entity=items partyExternal=items.acctgTagEnumIdAdmin/>
								</#if>
								</tr>
							</table>
						</@flexArea>
						</td>
					<tr>				
				<#assign count=count + 1/>
			</#list>
		</tbody>
	</table>
	</#if>
</@frameSection>

</@form>
<script type="text/javascript">
	var acctgTransTypeId = document.getElementById('acctgTransTypeId').value;
    opentaps.addOnLoad(cambiaEvento(acctgTransTypeId,'11'));
</script>

