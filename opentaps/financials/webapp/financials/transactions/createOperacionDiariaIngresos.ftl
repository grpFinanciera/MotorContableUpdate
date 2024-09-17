<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<form method="POST" name="createOperacionDiariaIngresos" action="${creaOpDiariaIngresos}"> <#-- action set by the screen -->
  <input type="hidden" name="organizationPartyId" value="${organizationPartyId}"/>
  <input type="hidden" name="tipoClave" value="INGRESO"/>
  <div class="form" style="border:0">
    <table class="fourColumnForm" style="border:0">
      <tr>
        <@displayTitleCell title=uiLabelMap.AccountingTipoDocumento />
        <@inputSelectCell list=listDocumentos?if_exists displayField="descripcion" name="idTipoDoc" default=idTipoDoc?if_exists />
      </tr> 
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialsTransactionDate titleClass="requiredField"/>
        <@inputDateTimeCell name="fechaTransaccion" default=Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp() />
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialsAccountigDate titleClass="requiredField"/>
        <@inputDateTimeCell name="fechaContable" default=Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp() />
      </tr>      
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialsReferenceDocument titleClass="requiredField"/>
        <@inputTextCell name="referenciaDocumento" maxlength=60  />
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialsSequence titleClass="requiredField"/>
        <@inputTextCell name="secuencia" maxlength=20  />
      </tr> 
      <tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.Organizacion titleClass="requiredField"/>
        <@inputLookupCell lookup="LookupPartyName" form="createOperacionDiariaIngresos" name="partyId" />
      </tr>
      </tr>           
		<@displayCvesPresupRow tagTypes=tagTypes titleClass="requiredField" /> 
	  <tr>
          <@displayCell text=uiLabelMap.CommonAmount blockClass="titleCell" blockStyle="width: 100px" class="tableheadtext" />
          <@inputCurrencyCell name="monto" currencyName="currency"/>
      </tr> 
      <th>
	  	<@displayTooltip text="Catálogos Auxiliares" />
	  </th>
	  <tr>
        <@displayTitleCell title=uiLabelMap.CAParty />
        <@inputLookupCell lookup="LookupPartyName" form="createOperacionDiariaIngresos" name="caParty" />
        <@inputAutoCompleteProductRow name="caProductId" title=uiLabelMap.ProductProduct />
      </tr>                                                                                                  
      <@inputSubmitRow title=uiLabelMap.CommonCreate />
    </table>
  </div>
</form>
