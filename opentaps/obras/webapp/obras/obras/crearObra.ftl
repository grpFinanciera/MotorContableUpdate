
<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@frameSection title=uiLabelMap.NuevaObra >
	
<@form name="creaObra" url="guardarObra" >
	<table>
		<@inputHidden name="statusId" value="CREADA_O" />
		<@inputHidden name="organizationPartyId" value=organizationPartyId />
		<@inputTextRow name="nombre" title=uiLabelMap.CommonName titleClass="requiredField"/>
		<@inputTextRow name="descripcion" title=uiLabelMap.CommonDescription titleClass="requiredField"/>
		<@inputTextRow name="justificacion" title=uiLabelMap.PurchJustification titleClass="requiredField"/>
		<@inputAutoCompleteProductRow title=uiLabelMap.Product name="productId" titleClass="requiredField"/>
		<@inputTextRow name="numAutCarteraInversion" title=uiLabelMap.ObrasCarteraInversion titleClass="requiredField"/>
		<@inputCurrencySelectRow name="moneda" title=uiLabelMap.CommonCurrency defaultCurrencyUomId="MXN" titleClass="requiredField" />
		<@inputCurrencyRow name="montoAutorizado" title=uiLabelMap.ObrasMontoAutorizado disableCurrencySelect=true titleClass="requiredField"/>
		<@inputDateRow name="fechaInicio" title=uiLabelMap.CommonStartDate titleClass="requiredField"/>
		<@inputDateRow name="fechaFin" title=uiLabelMap.CommonEndDate titleClass="requiredField"/>
		<@displayCvesPresupRow tagTypes=tagTypes titleClass="requiredField"/>		
		<@inputSubmitRow title=uiLabelMap.CommonCreate />
	</table>
</@form>
</@frameSection>