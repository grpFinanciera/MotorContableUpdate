<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl" />

<@displayRow text=GastoReserva.gastosReservaId!'' title=uiLabelMap.GastoReservadoId />
<@displayRow text=nomArea!'' title=uiLabelMap.GastosReservaArea />
<@displayRow text=nombreSolicitanteGasto!'' title=uiLabelMap.GastosReservaSolicitante />
<@displayRow text=GastoReserva.concepto!'' title=uiLabelMap.GastosReservaConceptGasto />
<@displayDateRow date=GastoReserva.fecha format="DATE" title=uiLabelMap.CommonDate />
<@displayRow title=uiLabelMap.PartySupplier text=GastoReserva.proveedor!''/>
<@displayRow text=GastoReserva.observaciones!'' title=uiLabelMap.GastosReservaMotObservaciones />
<@displayRow text=GastoReserva.tipoMoneda title=uiLabelMap.CommonCurrency />
<@displayCurrencyRow title=uiLabelMap.CommonAmount currencyUomId=GastoReserva.tipoMoneda amount=GastoReserva.monto class="tabletext"/>
<#assign objetoAuxiliar = GastoReserva.getRelatedOne("AuxiliarPartyGroup")!'' />
<#if objetoAuxiliar?has_content ><#assign textAux = objetoAuxiliar.groupName!'' /><#else> <#assign textAux = '' /></#if>
<@displayRow text=textAux title=uiLabelMap.GastosReservaAuxiliarContable />
<#if listAutorizadores?has_content>
	<@displayRow title="Fujo de autorización"/>
	<#list listAutorizadores as autorizador>
		<@displayRow title="Autorizador "+ autorizador.secuencia text=autorizador.firstName + " " + autorizador.lastName />
	</#list>
</#if>
<#if listAutorizadoresStatus?has_content>
	<@displayRow title="Estatus autorizaciones"/>
	<#list listAutorizadoresStatus as autoriza>
		<@displayRow title="Autorizador" text=autoriza.firstName + " " + autoriza.lastName />
		<@displayRow title="Estatus" text=autoriza.statusId />
	</#list>
</#if>