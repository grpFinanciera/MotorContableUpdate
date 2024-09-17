
<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@frameSection title=uiLabelMap.NuevaOrdenPagoPatrimonial >
	
<@form name="guardaOrdenPagoPatrimonial" url="guardaOrdenPagoPatrimonial" >
	<table>
		<#assign urlHost = request.getServerName()+":"+request.getServerPort()/>
		<@inputHidden name="organizationPartyId" value=organizationPartyId />
		<@inputHidden name="tipo" value="ORDEN_PAGO_PATRI" />
		<input type="hidden" name="urlHost" value="${urlHost}"/>		
		<@displayRow title=uiLabelMap.Tipo text="Orden de Pago Patrimonial"/>
		<@displayTitleCell title=uiLabelMap.FinancialsTransactionDate titleClass="requiredField"/>
        <@inputDateTimeCell name="fechaTransaccion" default=Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp() />
        <tr>
              
        <@displayTitleCell title=uiLabelMap.FinancialsAccountigDate titleClass="requiredField" />
        <@inputDateTimeCell name="fechaContable" default=Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp() />
        </tr>
		<@inputTextRow name="descripcion" title=uiLabelMap.CommonDescription titleClass="requiredField"/>
		<@inputSelectRow title=uiLabelMap.Evento name="acctgTransTypeId" titleClass="requiredField" list=eventos?sort_by("descripcion") id="acctgTransTypeId" key="acctgTransTypeId" required=false displayField="descripcion" onChange="cambiaEvento(this.value,'0');"/>
		<#--<@inputAutoCompletePartyRow title=uiLabelMap.Auxiliar name="auxiliar" id="auxiliar" />-->
		<tr></tr>
		<#--<@displayCell text=uiLabelMap.Monto blockClass="titleCell" blockStyle="width: 100px" class="tableheadtext" />			
		<@inputCurrencyCell name="monto" currencyName="moneda" defaultCurrencyUomId="MXN"/>-->
		<tr>
      	<@displayTitleCell title=uiLabelMap.AccountingCurrency titleClass="requiredField" />
		<@inputCurrencySelectCell name="moneda" defaultCurrencyUomId="MXN" />
      	</tr>
		<@inputSubmitRow title=uiLabelMap.CommonCreate />
		
		<tr></tr>
		<tr></tr>
		<tr></tr>
		
		
		
		<table width="50%">
				
		
		<tbody>
						
			
	
				
				<#assign organizationPartyId = "${organizationPartyId}" />
				
				
					<tr name="item" >
				
					<tr>				
				
			
		</tbody>
	</table>
				
	</table>
</@form>
</@frameSection>


<script type="text/javascript">
	var acctgTransTypeId = document.getElementById('acctgTransTypeId').value;
    opentaps.addOnLoad(cambiaEvento(acctgTransTypeId,'0'));
</script>