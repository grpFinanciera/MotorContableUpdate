<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@frameSection title=uiLabelMap.FinancialsPrintChecks >
	<@form name="imprimirChequeSimple" url="imprimirChequeSimple" acctgTransId=parameters.acctgTransId monto=parameters.monto>
		<table>
		<@inputTextRow name="lugar" title=uiLabelMap.FinancialsLugarCheque default=parameters.get('lugar')! />
		<tr>
		<@displayTitleCell title=uiLabelMap.TitularFirma1 />
		<td><@inputAutoComplete name="titular1" url="getAutoCompletePartyIds" form="SeleccionaCheque" filtro="PERSON" default=parameters.get('titular1')! /> </td>
		</tr>
		<tr>
		<@displayTitleCell title=uiLabelMap.TitularFirma2 />
		<td><@inputAutoComplete name="titular2" url="getAutoCompletePartyIds" form="SeleccionaCheque" filtro="PERSON" default=parameters.get('titular2')! /></td>
		</tr>
		<@inputTextRow name="beneficiario" title=uiLabelMap.Beneficiario />
		<tr>
		<@displayTitleCell title=uiLabelMap.FormuladoPor />
		<td><@inputAutoComplete name="formuladoPor" url="getAutoCompletePartyIds" form="SeleccionaCheque" filtro="PERSON" default=parameters.get('formuladoPor')!/></td>
		</tr>
		<tr>
		<@displayTitleCell title=uiLabelMap.RevisadoPor />
		<td><@inputAutoComplete name="revisadoPor" url="getAutoCompletePartyIds" form="SeleccionaCheque" filtro="PERSON" default=parameters.get('revisadoPor')!/></td>
		</tr>
		<tr>
		<@displayTitleCell title=uiLabelMap.AutorizadoPor />
		<td><@inputAutoComplete name="autorizadoPor" url="getAutoCompletePartyIds" form="SeleccionaCheque" filtro="PERSON" default=parameters.get('autorizadoPor')!/></td>
		</tr>
		<tr>
		<@displayTitleCell title=uiLabelMap.RecibidoPor />
		<td><@inputAutoComplete name="recibidoPor" url="getAutoCompletePartyIds" form="SeleccionaCheque" filtro="PERSON"  default=parameters.get('recibidoPor')!/></td>
		</tr>
		<@inputTextRow name="noCheque" title=uiLabelMap.NumeroCheque default=parameters.get('noCheque')! />
		<@inputDateRow name="fechaImpresion" title=uiLabelMap.FechaImpresion form="SeleccionaCheque" default=Static["org.ofbiz.base.util.UtilDateTime"].nowSqlDate() />
        <#assign chequeHash = {"Bancomer1" : "BBVA Bancomer 1", "Bancomer2" : "BBVA Bancomer 2" , "Bancomer3" : "BBVA Bancomer 3" ,
        								"Banorte" : "Banorte", "HSBC" : "HSBC" , "PolizaBancomer" : uiLabelMap.FinancialsPoliza+" Bancomer" }/>
		<@inputSelectHashRow name="nombreCheque" title=uiLabelMap.FinancialsNombreCheque hash=chequeHash />
		<tr><td></td><td>
		<input type="submit" value="${uiLabelMap.CommonPrint}" class="smallSubmit" />
		</td></tr>			
		</table>
	</@form>
</@frameSection>