<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<script  languaje="JavaScript">    
	//Functions to send request to server for load relate objects
		function cargaBanco(cuentaBancariaId) 
		{   var bancoId = document.getElementById('bancoIdOrigen').value;
			if (bancoId != "") 
		    { 	var requestData = {'bancoId' : bancoId, 'organizationPartyId' : '${organizationPartyId}'};		    			    
		    	opentaps.sendRequest('obtieneCuentasBancarias', requestData, function(data) {cargarCuentasResponseOrigen(data,cuentaBancariaId)});
		    }
			
		}

		//Functions load the response from the server
		function cargarCuentasResponseOrigen(data,cuentaBancariaId) 
		{	i = 0;				
			var select = document.getElementById("cuentaOrigen");
			document.getElementById("cuentaOrigen").innerHTML = "";
			for (var key in data) 
			{	if(data[key]=="ERROR")
				{	
					document.getElementById("cuentaOrigen").innerHTML = "";
				}
				else
				{	
					select.options[i] = new Option(data[key]);
					select.options[i].value = key;
					if(cuentaBancariaId == key){
						select.options[i].selected = true;
					}			
				  	i++;
				}	    		
			}		
		}
		    
</script>

<#assign LinkEnviarMotor = "">

<#if datosPoliza?has_content>
	<#list datosPoliza as datos>
		<#assign transId = datos.acctgTransId!/>
		<#assign poliza = datos.poliza!/>
		<#if transId?has_content && poliza?has_content>		
			<@form name="verPolizaAcctgTrans${transId}" url="viewAcctgTrans?acctgTransId=${transId}"/>
			<#assign verPolizaAction>${verPolizaAction!}<@actionForm form="verPolizaAcctgTrans${transId}" text=poliza /></#assign>
		</#if>	
	</#list>
	<@form name="ImprimirChequePoliza" url="ImprimirChequePoliza" paymentId=datosPayment.get(0).paymentId! />
	<#assign LinkEnviarMotor>${LinkEnviarMotor!}<@submitFormLink form="ImprimirChequePoliza" text=uiLabelMap.FinancialsImprimeCheque class="subMenuButton" /></#assign>
<#else>
		<#assign LinkEnviarMotor>${LinkEnviarMotor}<@submitFormLink form="finalizarPendienteTesoreria" text=uiLabelMap.Pagar class="subMenuButton"/></#assign>		
</#if>	
<#assign LinkEnviarMotor>${LinkEnviarMotor}
	<#if datosPoliza?has_content>
			<@selectActionForm name="shipmentActions" prompt=uiLabelMap.FinancialVerPoliza>
		   		${verPolizaAction?if_exists}
			</@selectActionForm>
	</#if>
</#assign>

<form method="post" action="<@ofbizUrl>finalizarPendienteTesoreria</@ofbizUrl>" name="finalizarPendienteTesoreria" style="margin: 0;" id="finalizarPendienteTesoreria">
<#assign urlHost = request.getServerName()+":"+request.getServerPort()/>
<input type="hidden" name="urlHost" value="${urlHost}"/>

<@frameSection title=uiLabelMap.FinancialsPendienteTesoreria extra=LinkEnviarMotor>
	<#if pendienteTesoreria?exists>		
			<table>
				
				<input type="hidden" name="idPendienteTesoreria" value="${pendienteTesoreria.idPendienteTesoreria}"/>
				<input type="hidden" name="tipo" value="${pendienteTesoreria.tipo}"/>
				<input type="hidden" name="descripcion" value="${pendienteTesoreria.descripcion!''}"/>
				<input type="hidden" name="auxiliar" value="${pendienteTesoreria.auxiliar?if_exists}"/>
				<input type="hidden" name="monto" value="${sumaTotal?if_exists}"/>
				<input type="hidden" name="estatus" value="${pendienteTesoreria.estatus}"/>
				<input type="hidden" name="organizationPartyId" value="${organizationPartyId}"/>
				
				<#if pendienteTesoreria.tipo == "OBRAS">
					<@displayRow title=uiLabelMap.ObraObraId text=pendienteTesoreria.idPendienteTesoreria/>					
				<#elseif pendienteTesoreria.tipo == "ORDEN_PAGO_PATRI">				
					<@displayRow title=uiLabelMap.OrdenPagoPatri text=pendienteTesoreria.idPendienteTesoreria/>	
				<#elseif pendienteTesoreria.tipo == "PAGO_EXTERNO">				
					<@displayLinkRow href="verPagoExterno?pagoExternoId=${pendienteTesoreria.pagoExternoId}" 
						title=uiLabelMap.TesoreriaPagoExternoId text=pendienteTesoreria.pagoExternoId />							
				<#else>	
					<@displayRow title=uiLabelMap.PurchViaticoId text=pendienteTesoreria.idPendienteTesoreria/>
					<#if fechaViatico?exists>
						<@displayRow title=uiLabelMap.FinancialsFechaInicialViatico text=fechaViatico/>
					</#if>					
				</#if>
				<@displayRow title=uiLabelMap.FinancialsTipoPendiente text=pendienteTesoreria.tipo/>
				<@displayRow title=uiLabelMap.Descripcion text=pendienteTesoreria.descripcion/>
				<@displayRow title=uiLabelMap.Auxiliar text=auxNombre/>
				<@displayCurrencyRow title=uiLabelMap.Monto amount=sumaTotal?if_exists currencyUomId=parameters.orgCurrencyUomId />
				<@displayRow title=uiLabelMap.FinancialsStatusId text=pendienteTesoreria.estatus/>
				<@inputDateTimeRow name="fechaContable" title=uiLabelMap.FinancialsFechaContable default=pendienteTesoreria.fechaContable />
				<#-- <@displayRow title=uiLabelMap.FinancialsFechaContable text=pendienteTesoreria.fechaContable/> -->
				<#-- <input type="hidden" name="fechaContable" value="${pendienteTesoreria.fechaContable}"/> -->
				<@displayRow title=uiLabelMap.Moneda text=pendienteTesoreria.moneda/>
				<input type="hidden" name="moneda" value="${pendienteTesoreria.moneda}"/>
				
				<#if !pendienteTesoreria.acctgTransTypeId?exists>				
						<@inputSelectRow title=uiLabelMap.Evento required=false list=documentos  displayField="descripcion" name="acctgTransTypeId" default=acctgTransTypeId?if_exists titleClass="requiredField"/>
				<#else>
					<@displayRow title=uiLabelMap.Evento text=pendienteTesoreria.acctgTransTypeId/>
					<@displayRow title=uiLabelMap.FinancialsTipoEvento text=acctgTransTypeIdName.descripcion/>
					<input type="hidden" name="acctgTransTypeId" value="${pendienteTesoreria.acctgTransTypeId}"/>
				</#if>		
				<@inputSelectRow title=uiLabelMap.CommonFinBankName id="bancoIdOrigen" name="bancoIdOrigen" list=bancos key="bancoId" displayField="nombreBanco" default=pendienteTesoreria.bancoId onChange="cargaBanco();"  />
				<@inputSelectRow title=uiLabelMap.FinancialsCuentaBancaria id="cuentaOrigen" name="cuentaOrigen" list=[] />
      			<@inputTextRow name="conceptoPago" title=uiLabelMap.TesoreriaConceptoPago maxlength="500" default=pendienteTesoreria.conceptoPago!'' />
				<@inputSelectRow title=uiLabelMap.FinancialsPaymentMethod name="paymentTypeId" list=listPaymentType![] key="paymentTypeId" displayField="description" default=pendienteTesoreria.paymentTypeId!'' required=false />
				<@inputTextRow name="numeroCheque" title=uiLabelMap.TesoreriaNumeroCheque maxlength="60" default=pendienteTesoreria.numeroCheque!'' />
				<@inputTextRow name="nombreExterno" title=uiLabelMap.TesoreriaNombreExterno maxlength="255" default=pendienteTesoreria.nombreExterno!'' />
			</table>		
	</#if>
</@frameSection>

<#if resultadoLineaContable?has_content>	           			           		           		           		           	
	<@frameSection title=uiLabelMap.AccountingInvoiceItems>		
			<table width="100%">
					
			<thead>		
				<tr>
					<th>${uiLabelMap.SecuenciaLineaContable}</th>
					<th>${uiLabelMap.Descripcion}</th>
					<th>${uiLabelMap.ValorCatalogoCargo}</th>				
					<th>${uiLabelMap.ValorCatalogoAbono}</th>				
					<th>${uiLabelMap.Monto}</th>			
					<th></th>								
				</tr>
			</thead>
			<tbody>								
				<#list resultadoLineaContable as datos>														
					<tr class="${tableRowClass(datos_index)}" name="datos" >
						<@displayCell text=datos.secuenciaLineaContable/>
						<@displayCell text=datos.descripcion />
						<@displayCell text=datos.idCatalogoCargo?if_exists?default("")/>
						<@displayCell text=datos.idCatalogoAbono?if_exists?default("")/>												
						<@displayCurrencyCell currencyUomId=parameters.orgCurrencyUomId amount=datos.monto />																																						
					</tr>											
				</#list>
			</tbody>
		</table>
	</@frameSection>
</#if>
<script type="text/javascript">
    opentaps.addOnLoad(cargaBanco(${pendienteTesoreria.cuentaBancariaId!''}));
</script>
