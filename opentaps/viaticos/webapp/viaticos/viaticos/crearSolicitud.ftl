<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@frameSection title=uiLabelMap.PurchCreateDiemRequest>
	<@form name="solicitudViatico" url="solicitaViatico" >
		<table>
			<@inputTextRow name="descripcion" title=uiLabelMap.PurchDescripcion maxlength="255" />
			<@inputTextRow name="justificacion" title=uiLabelMap.PurchJustification maxlength="255" />
			<@inputCurrencySelectRow name="tipoMoneda" title=uiLabelMap.CommonCurrency defaultCurrencyUomId="MXN"/>
			<@inputCurrencyRow name="montoDiario" title=uiLabelMap.PurchDailyAmount disableCurrencySelect=true />
			<@inputSelectRow name="tipoTransporte" title=uiLabelMap.PurchTransportType
				list=TiposTransportes! key="tipoTransporteId" displayField="descripcion"/>
			<@inputCurrencyRow name="montoTransporte" title=uiLabelMap.PurchTransportAmount disableCurrencySelect=true />				
			<@inputSelectRow name="geoOrigen" title=uiLabelMap.PurchOrigin
				list=Geos! key="geoId" displayField="geoName"/>
			<@inputSelectRow name="geoDestino" title=uiLabelMap.PurchDestination
				list=Geos! key="geoId" displayField="geoName"/>
			<@inputDateRow name="fechaInicio" title=uiLabelMap.CommonStartDate form="solicitudViatico"/>
			<@inputDateRow name="fechaFin" title=uiLabelMap.CommonEndDate form="solicitudViatico"/>
			<@inputSubmitRow title=uiLabelMap.CommonSubmit />
		</table>
	</@form>
</@frameSection>