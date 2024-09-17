
<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#assign LinkVerPoliza = "">



<#if datosPoliza?has_content>
	<#list datosPoliza as datos>
		<#assign transId = datos.acctgTransId!/>
		<#assign poliza = datos.poliza!/>
		<#if transId?has_content && poliza?has_content>		
			<@form name="verPolizaAcctgTrans${transId}" url="viewAcctgTrans?acctgTransId=${transId}"/>
			<#assign verPolizaAction>${verPolizaAction!}<@actionForm form="verPolizaAcctgTrans${transId}" text=poliza /></#assign>
		</#if>	
	</#list>			
</#if>	
<#assign LinkVerPoliza>${LinkVerPoliza}
	<#if datosPoliza?has_content>
			<@selectActionForm name="shipmentActions" prompt=uiLabelMap.FinancialVerPoliza>
		   		${verPolizaAction?if_exists}
			</@selectActionForm>
	</#if>
</#assign>


<@frameSection title=uiLabelMap.VerOrdenPagoPatrimonial extra=LinkVerPoliza >
	
<@form name="verOrdenPagoPatrimonial" url="verOrdenPagoPatrimonial">
	<#list datosOrdenPago as orderPago>
		<table>							
			<@displayRow title=uiLabelMap.OrdenPagoPatrimonialId text=orderPago.idPendienteTesoreria?if_exists/>
			<@displayRow title=uiLabelMap.Tipo text="Orden de Pago Patrimonial"/>			
			<@displayRow title=uiLabelMap.Descripcion text=orderPago.descripcion?if_exists/>
			<@displayRow title=uiLabelMap.Evento text=orderPago.acctgTransTypeId?if_exists/>
			<@displayRow title=uiLabelMap.FinancialsTipoEvento text=orderPago.descripcionEvento?if_exists/>
			<@displayRow title=uiLabelMap.Moneda text=orderPago.moneda?if_exists/>
			<@displayDateRow title=uiLabelMap.FinancialsTransactionDate date=orderPago.fechaTransaccion?if_exists/>
			<@displayDateRow title=uiLabelMap.FinancialsAccountigDate date=orderPago.fechaContable?if_exists/>
			<#if orderPago.bancoId?exists>
				<@displayRow title=uiLabelMap.bancoId text=orderPago.bancoId?if_exists/>
			</#if>
			
			<#if datosBanco?has_content>
				<#list datosBanco as banco>
        			<@displayRow title=uiLabelMap.Banco text=banco.nombreBanco?if_exists/>
        		</#list>
        	</#if>	        				
        	<#if orderPago.cuentaBancariaId?exists>
				<@displayRow title=uiLabelMap.FinancialsCuentaBancariaId text=orderPago.cuentaBancariaId?if_exists/>
			</#if>	
			<#if datosCuenta?has_content>
				<#list datosCuenta as cuenta>
        			<@displayRow title=uiLabelMap.FinancialsCuentaBancaria text="${cuenta.nombreCuenta?if_exists} / ${cuenta.numeroCuenta?if_exists} / ${cuenta.descripcion?if_exists}"/>
        		</#list>
        	</#if>									
			<@displayRow title=uiLabelMap.FinancialsDescripcionEstatus text=orderPago.descripcionEstatus?if_exists/>															
		</table>		                                    
		
	</#list>	
</@form>
</@frameSection>

<#if datosOrdenPagoItem?has_content>	           			           		           		           		           	
	<@frameSection title=uiLabelMap.AccountingInvoiceItems>		
			<table width="100%">
					
			<thead>		
				<tr>
					<th>${uiLabelMap.OrdenPagoPatrimonialId}</th>
					<th>${uiLabelMap.SecuenciaLineaContable}</th>
					<th>${uiLabelMap.Descripcion}</th>
					<th>${uiLabelMap.ValorCatalogoCargo}</th>				
					<th>${uiLabelMap.ValorCatalogoAbono}</th>				
					<th>${uiLabelMap.Monto}</th>			
					<th></th>								
				</tr>
			</thead>
			<tbody>								
				<#list datosOrdenPagoItem as datos>														
					<tr class="${tableRowClass(datos_index)}" name="datos" >
						<@displayCell text=datos.idPendienteTesoreria/>
						<@displayCell text=datos.secuenciaLineaContable />
						<@displayCell text=datos.descripcion?if_exists?default("")/>
						<@displayCell text=datos.idCatalogoCargo?if_exists?default("")/>
						<@displayCell text=datos.idCatalogoAbono?if_exists?default("")/>												
						<@displayCurrencyCell currencyUomId=parameters.orgCurrencyUomId amount=datos.monto />																																						
					</tr>				
				</#list>
			</tbody>
		</table>
	</@frameSection>
</#if>	
