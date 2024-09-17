<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@frameSection title=uiLabelMap.GastosReservaApplication extra=submitButton>
	<@form name="solicitudGastoReserva" url="registrarSolicitud" >
		<table>
			<@inputHidden  name="areaId" value=areaId />
			<@inputHidden  name="oganizationPartyId" value=oganizationPartyId />
			<@displayRow   text=nomArea!'&Aacute;rea sin asignar. Contacte al administrador' title=uiLabelMap.GastosReservaArea />
			<@displayRow   text=nombreSolicitante!'' title=uiLabelMap.GastosReservaSolicitante />
			<@displayRow   text=solicitanteId!'' title=uiLabelMap.GastosReservaNumEmpl />
			<@inputTextareaRow name="concepto" title=uiLabelMap.GastosReservaConceptGasto titleClass="requiredField"/>
			<@inputCurrencySelectRow name="tipoMoneda" title=uiLabelMap.CommonCurrency defaultCurrencyUomId="MXN" titleClass="requiredField"/>
			<@inputDateRow name="fecha" title=uiLabelMap.CommonDate form="solicitudGastoReserva" titleClass="requiredField"/>
			<@inputCurrencyRow name="monto" title=uiLabelMap.GastosReservaMotSalic disableCurrencySelect=true titleClass="requiredField"/>
			<@inputTextRow title=uiLabelMap.GastosReservaProveedor id="proveedor" name="proveedor" size=30 maxlength="255" />
			<@inputTextareaRow name="observaciones" title=uiLabelMap.GastosReservaMotObservaciones readonly=false />
			<#if tipoAuxiliarId?has_content>
				<@inputAutoCompletePartyRow title=uiLabelMap.GastosReservaAuxiliarContable name="auxiliarId" filtro="${tipoAuxiliarId}"/>
			<#else>
				<@display text="Necesita configurar el auxiliar contable en el evento
				 'Comprobaci&oacute;n de gastos a reserva de comprobar', avise al administrador" class="requiredField"/> 
			</#if>
			<@inputSubmitRow title=uiLabelMap.CommonCreate/>
		</table>
	</@form>
</@frameSection>