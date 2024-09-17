<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl" />


<#assign urlHost = request.getServerName()+":"+request.getServerPort()/>
<#if muestraAutorizador>
			<@include location="component://opentaps-common/webapp/common/workFlow/AutorizadorForm.ftl"/>
</#if>




<@form name="cancelarGastoReserva" url="cancelarGastoReserva" id="cancelarGastoReserva" gastosReservaId = GastoReserva.gastosReservaId urlHost=urlHost/>
	
	<#if edita >
		<#assign Extra>
			<@submitFormLinkConfirm form="actualizarGastoReserva" text="Enviar" id="actualizarGastoReserva"/>
		</#assign>
		
		<#assign Extra>${Extra}
			<@submitFormLinkConfirm form="cancelarGastoReserva" text="Cancelar" id="cancelarGastoReserva"/>
		</#assign>
	</#if>

	<#if GastoReserva.acctgTransIdOtorga?has_content>		
		<@form name="verPolizaAcctgTrans${GastoReserva.acctgTransIdOtorga}" url="viewAcctgTrans?acctgTransId=${GastoReserva.acctgTransIdOtorga}"/>
		<#assign verPolizaAction><@actionForm form="verPolizaAcctgTrans${GastoReserva.acctgTransIdOtorga}" text=uiLabelMap.GastosReservaPolizaOtorga /></#assign>
	</#if>	
	<#if GastoReserva.acctgTransIdComprueba?has_content>		
		<@form name="verPolizaAcctgTrans${GastoReserva.acctgTransIdComprueba}" url="viewAcctgTrans?acctgTransId=${GastoReserva.acctgTransIdComprueba}"/>
		<#assign verPolizaAction>${verPolizaAction!}<@actionForm form="verPolizaAcctgTrans${GastoReserva.acctgTransIdComprueba}" text=uiLabelMap.GastosReservaPolizaComprueba /></#assign>
	</#if>	
	<#if GastoReserva.acctgTransIdDevuelve?has_content>		
		<@form name="verPolizaAcctgTrans${GastoReserva.acctgTransIdDevuelve}" url="viewAcctgTrans?acctgTransId=${GastoReserva.acctgTransIdDevuelve}"/>
		<#assign verPolizaAction>${verPolizaAction!}<@actionForm form="verPolizaAcctgTrans${GastoReserva.acctgTransIdDevuelve}" text=uiLabelMap.GastosReservaPolizaDevolucion /></#assign>
	</#if>	
	
	<#assign Extra>${Extra!}
		<#if verPolizaAction?has_content >
			<@selectActionForm name="verPoliza" prompt=uiLabelMap.FinancialVerPoliza>
		   		${verPolizaAction?if_exists}
			</@selectActionForm>
		</#if>
	</#assign>

<@frameSection title="Gastos a reserva de comprobar" extra=Extra!''>
	<@form name="actualizarGastoReserva" url="actualizarGastoReserva" id="actualizarGastoReserva" gastosReservaId = GastoReserva.gastosReservaId urlHost=urlHost>
	<table width="100%">

		<#if edita >
			<@displayRow title=uiLabelMap.GastoReservadoId text=GastoReserva.gastosReservaId/>
			<@displayRow title=uiLabelMap.GastosReservaArea text=nomArea/>
			<@displayRow title=uiLabelMap.GastosReservaSolicitante text=nombreSolicitanteGasto!''/>
			<@inputTextareaRow title=uiLabelMap.GastosReservaConceptGasto name="concepto" default=GastoReserva.concepto titleClass="requiredField"/>
			<@inputDateRow name="fecha" title=uiLabelMap.CommonDate form="solicitudGastoReserva" default=GastoReserva.fecha titleClass="requiredField"/>
			<@inputTextRow title=uiLabelMap.PartySupplier name="proveedor" size="25" default=GastoReserva.proveedor!'' />
			<@inputCurrencySelectRow name="tipoMoneda" title=uiLabelMap.CommonCurrency defaultCurrencyUomId=GastoReserva.tipoMoneda titleClass="requiredField"/>
			<@inputCurrencyRow name="monto" title=uiLabelMap.CommonAmount disableCurrencySelect=true default=GastoReserva.monto titleClass="requiredField"/>
			<@inputTextareaRow title=uiLabelMap.GastosReservaMotObservaciones name="observaciones" default=GastoReserva.observaciones />
			<#if comentarios?has_content>	    	
		    	<tr>
		    	</tr>
		    	<tr>
		    	</tr>
		    	<tr>	    		
		    		<td><td>
	      			<@displayTooltip text="Comentarios" />
	      			</td></td>
	      		</tr>
	      		<tr>
		    	</tr>	
		    	<#list comentarios as comentario>
		    		<@displayRow title="${comentario.firstName} "+"${comentario.lastName}: " text=comentario.comentario />	    				    	
			    </#list>		    
	    	</#if>	
			
		<#else>
			<@include location="component://gastosReserva/webapp/gastosReserva/gastosReserva/encabezadoGasto.ftl"/>
			<#if comentarios?has_content>	    	
		    	<tr>
		    	</tr>
		    	<tr>
		    	</tr>
		    	<tr>	    		
		    		<td><td>
	      			<@displayTooltip text="Comentarios" />
	      			</td></td>
	      		</tr>
	      		<tr>
		    	</tr>	
		    	<#list comentarios as comentario>
		    		<@displayRow title="${comentario.firstName} "+"${comentario.lastName}: " text=comentario.comentario />	    				    	
			    </#list>		    
	    	</#if>			
		
		</#if>
		<#if muestraAutorizador>
			<@include location="component://opentaps-common/webapp/common/workFlow/Autorizador.ftl"/>
		</#if>

	</@form>
</@frameSection>
