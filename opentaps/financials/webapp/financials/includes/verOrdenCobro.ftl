
<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<script  languaje="JavaScript">    
	//Functions to send request to server for load relate objects
		function contactTypeChangedOrigen() 
		{   var bancoId = document.getElementById('bancoId').value;
			if (bancoId != "") 
		    { 	var requestData = {'bancoId' : bancoId, 'partyId' : '${partyId}'};		    			    
		    	opentaps.sendRequest('obtieneCuentasBancariasAsignadas', requestData, function(data) {cargarCuentasResponseOrigen(data)});
		    }
			
		}

		//Functions load the response from the server
		function cargarCuentasResponseOrigen(data) 
		{	i = 0;				
			var select = document.getElementById("cuentaBancariaId");
			document.getElementById("cuentaBancariaId").innerHTML = "";
			for (var key in data) 
			{	if(data[key]=="ERROR")
				{	document.getElementById("cuentaBancariaId").innerHTML = "";
				}
				else
				{	select.options[i] = new Option(data[key]);
					select.options[i].value = key;					
				  	i++;
				}	    		
			}		
		}
		
		/*function recalcularCobro(contador) 
		{	var montCont = parseFloat("0");
			var montoPresupuestal = parseFloat("0");
		  	montCont = document.getElementById('montoBancoContable'+contador).value;
		  	montoPresupuestal = document.getElementById('montoPresupuestal').value;		  	
		  	var matriz = "${tipoMatrizId!''}";
		  	if(matriz == "B.2")
		  	{	var total=parseFloat(montCont) + parseFloat(montoPresupuestal);		  				  		
		  		document.getElementById('montoPorCobrar').value = total;
		  	}
		  	else	
		  	{	var total=parseFloat(montCont);		  				  		
		  		document.getElementById('montoPorCobrar').value = total;
		  	}		  	 											
		}*/    

</script>


<#assign LinkEnviarMotor = "">
<#assign montoACobrar=0 id="montoACobrar" name="montoACobrar"/>



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







<#if ! statusId.equals("ORDEN_COBRO_RECAUDAR")>
		<#assign LinkEnviarMotor>${LinkEnviarMotor}<@submitFormLink form="enviarOrdenRecaudadoParcial" text=uiLabelMap.Cobrar class="subMenuButton"/></#assign>
</#if>

<#assign LinkEnviarMotor>${LinkEnviarMotor}
	<#if datosPoliza?has_content>		
			<@selectActionForm name="shipmentActions" prompt=uiLabelMap.FinancialVerPoliza>
		   		${verPolizaAction?if_exists}
			</@selectActionForm>
	</#if>
</#assign>

<form id="actualizarFechaCobro" method="post" action="actualizarFechaCobro">
	<table>
		<tr>
			<input type="hidden" name="invoiceId" id="invoiceId" value="${invoiceId}"/>
			<input type="hidden" name="ordenCobroId" id="ordenCobroId" value="${ordenCobroId}"/>				
			<td><@displayTooltip text=uiLabelMap.ActualizarFechaCobro /></td>
			<td>													
			    <@displayTitleCell title=uiLabelMap.FechaDeCobro titleClass="requiredField"/>
			    <@inputDateTimeCell name="fechaCobroValor" default=fechaCobro?if_exists/>
		    </td>
		    <td><input id="submitButton" type="button" onclick="javascript:$('actualizarFechaCobro').submit();" class="smallSubmit" value="${uiLabelMap.ActualizarFecha}" /></td>
		<tr>
	</table>	
			
</form>

<form method="post" action="<@ofbizUrl>enviarOrdenRecaudadoParcial</@ofbizUrl>" name="enviarOrdenRecaudadoParcial" style="margin: 0;" id="enviarOrdenRecaudadoParcial">


<@frameSection title=uiLabelMap.VerOrdenCobro extra=LinkEnviarMotor>

	<table width="80%">
		<tr>
			<input type="hidden" name="invoiceId" id="invoiceId" value="${invoiceId}"/>
			<input type="hidden" name="ordenCobroId" id="ordenCobroId" value="${ordenCobroId}"/>	
			<input type="hidden" name="organizationPartyId" id="organizationPartyId" value="${organizationPartyId}"/>
			<input type="hidden" name="acctgTransTypeId" id="acctgTransTypeId" value="${acctgTransTypeId}"/>
			<input type="hidden" name="sumaBancosCargo" id="sumaBancosCargo" value="${sumaBancosCargo?if_exists}"/>
			<input type="hidden" name="sumaBancosAbono" id="sumaBancosAbono" value="${sumaBancosAbono?if_exists}"/>						
			<@displayRow title=uiLabelMap.ordenCobroId text=ordenCobroId/>
			<@displayRow title=uiLabelMap.FinancialsPreOrdenCobro text=invoiceId/>
			<#list datosInvoice as datos>
		    	<@displayDateRow date=datos.invoiceDate format="DATE_TIME" title=uiLabelMap.FechaContablePreOrdenCobro/>
		    	<@displayRow title=uiLabelMap.FinancialsDescripcionPreOrdenCobro text=datos.description/>
		    	<@displayRow title=uiLabelMap.Moneda text=datos.currencyUomId/>
		    	<@displayRow title=uiLabelMap.Auxiliar text=datos.partyIdFrom/>
		    	<@displayRow title=uiLabelMap.NombreAuxiliar text=datos.groupName/>
		    	<#--<@displayRow title=uiLabelMap.EstatusId text=statusId/>-->	
		    	<@displayRow title=uiLabelMap.Estatus text=datos.descripcionStatus/>
		    	<@displayRow title=uiLabelMap.FinancialsAcctgTransTypeId text=acctgTransTypeId/>	
		    	<@displayRow title=uiLabelMap.FinancialsTipoEvento text=datos.descripcionEvento/>		    			    					
		    </#list>


			<#if sumaTotal?exists>
				<tr>        			
        				<@displayTitleCell title=uiLabelMap.MontoTotalOrdenCobro />
        				<@displayCurrencyCell amount=sumaTotal?if_exists currencyUomId=parameters.orgCurrencyUomId/>
        				<input type="hidden" name="montoCobro" id="montoCobro" value="${sumaTotal?if_exists}"/>
        			
        				<@displayTitleCell title=uiLabelMap.MontoPresupuestal />        				        				
        				<#if (sumaPresuRestante?if_exists > 0)>																																																																					
							<@inputTextCell name="montoPresupuestal" id="montoPresupuestal" default="${sumaPresuRestante}" onChange="recalcularCobro();"/>							
						<#else>
							<@displayCurrencyCell currencyUomId=parameters.orgCurrencyUomId amount=sumaPresuRestante?if_exists />
							<input type="hidden" name="montoPresupuestal" id="montoPresupuestal" value="${sumaPresuRestante}"/>
						</#if>
        				        				        				
      			</tr>			            	           	           		           		           	
           </#if>           
           <#if !tipoMatrizId?exists>
           		<#list montoCobrarContable as contable>
           			<#assign montoACobrar=montoACobrar + contable.montoRestante?if_exists/>																			 												
				</#list>
				<@displayTitleCell title=uiLabelMap.MontoACobrar />
           		<@inputTextCell name="montoPorCobrar" id="montoPorCobrar" default="${montoACobrar?if_exists}" readonly=true size=10/>
           <#else>
           		<#assign montoACobrar=0/>
           		<#list montoCobrarContable as contable>
           			<#assign montoACobrar=montoACobrar + contable.montoRestante?if_exists/>																			 												
				</#list>
				<@displayTitleCell title=uiLabelMap.MontoACobrar />
           		<@inputTextCell name="montoPorCobrar" id="montoPorCobrar" default="${montoACobrar+sumaPresuRestante}" readonly=true size=10/>		
           </#if>           
		    
		    	<#-- VALIDA SI EXISTEN DATOS DE COBRO-->
		    
		    <#if datosPayment?has_content>		    
			    <#list datosPayment as datos>					
					<@displayRow title=uiLabelMap.FinancialsTipoCobro text=datos.description?if_exists/>
					<@displayRow title=uiLabelMap.CommonComments text=datos.comments?if_exists/>
					<@displayRow title=uiLabelMap.FinancialsCobroNumReferencia text=datos.paymentRefNum?if_exists/>
					<@displayDateRow date=datos.effectiveDate format="DATE_TIME" title=uiLabelMap.FinancialsAccountigDate/>							
					<@displayTitleCell title=uiLabelMap.Banco/>
        			<@inputSelectCell list=bancos?if_exists displayField="nombreBanco" name="bancoId" id="bancoId" default=datos.bancoId?if_exists onChange="contactTypeChanged(this.form);"/>		
					<tr>      
				        <td class="titleCell"><span><b><font  size=1 color=#000000>Cuenta Bancaria<font><b></span>
				    	</td>
	    				<td>  	    					    				
      						<select name="cuentaBancariaId" id="cuentaBancariaId" size="1" class="selectBox">      				  							  	
					  			<#list cuentas as cuenta>								  					
					        			<option  value="${cuenta.cuentaBancariaId}" <#if cuenta.cuentaBancariaId == datos.cuentaBancariaId> selected="selected" </#if> >${cuenta.get("numeroCuenta",locale)} - ${cuenta.get("nombreCuenta",locale)}</option>
					    		</#list>			    				        			
	  						</select>	  					
	  					</td>	          
  					<tr>    					
			    </#list>
			<#else>  
				<#if ! statusId.equals("ORDEN_COBRO_RECAUDAR")>
					<@inputSelectRow title=uiLabelMap.FinancialsTipoCobro name="paymentTypeId" ignoreParameters=true list=tipoCobro key="paymentTypeId" displayField="description" default="${paymentTypeId?if_exists}"/>			 
					<tr>
					    <@displayTitleCell title=uiLabelMap.CommonComments titleClass="requiredField"/>
					    <td><textarea class="textAreaBox" cols="40" rows="2" name="comentario" id="comentario"><#if paymentValue?has_content>${paymentValue.comments?if_exists}</#if></textarea></td>
	  				</tr>
					<tr>
					    <@displayTitleCell title=uiLabelMap.FinancialsCobroNumReferencia />
					    <td nowrap="nowrap"><input type="text" class="inputBox" name="paymentRefNum" id="paymentRefNum" value="<#if paymentValue?has_content>${paymentValue.paymentRefNum?if_exists}</#if>"/></td>
					</tr>
					
					<tr>
				        <@displayTitleCell title=uiLabelMap.FinancialsAccountigDate titleClass="requiredField" />
				        <@inputDateTimeCell name="fechaContable" default=Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp() />
		      		</tr>			        							
					<tr>
						<td class="titleCell"><span><b><font  size=1 color=#B40404>Banco</font><b></span>
						</td>
						<td>	   	    
							<select name="bancoId" id ="bancoId" size="1"  onchange="contactTypeChangedOrigen(this.form)">
			    				<option value=" "></option>
			        			<#list bancos as banco>
			        					<option  value="${banco.bancoId}">${banco.get("nombreBanco",locale)}</option>
			        			</#list>
			    			</select>	 
						</td>
			    	</tr>
			    	<tr>
						<td class="titleCell"><span><b><font  size=1 color=#B40404>Cuenta<font><b></span>
						</td>
						<td>  
							<select name="cuentaBancariaId" id="cuentaBancariaId" size="1" class="selectBox">      				  				
							</select>		
						</td>
		      		</tr>
		      	</#if>      	  			    
		    </#if>		    
		    
		    
		    
		    
		    
							
					            
		</tr>
		
		<tr><td colspan="4">&nbsp;</td></tr>
  		<tr><td colspan="4" class="formSectionHeaderTitle">${uiLabelMap.AfectacionesConBanco}</td></tr>    
  		<table width="80%">
					
			<thead>		
				<tr>
					<th>${uiLabelMap.Secuencia}</th>			
					<th>${uiLabelMap.Descripcion}</th>
					<th>${uiLabelMap.Concepto}</th>								
					<th>${uiLabelMap.Monto}</th>
					<th>${uiLabelMap.MontoRestante}</th>			
					<th></th>								
				</tr>
			</thead>
			<tbody>
				<#assign count = 1 />											
				<#list montoCobrarContable as contable>														
					<tr class="${tableRowClass(contable_index)}" name="contable" >
						<@displayCell text=contable.secuencia/>
						<@displayCell text=contable.descripcion/>												
						<@displayCell text="CONTABLE" />
						<@displayCurrencyCell currencyUomId=parameters.orgCurrencyUomId amount=contable.monto />
						<#if (contable.montoRestante > 0)>																																																																					
							<@inputTextCell name="montoBancoContable${count}" id="montoBancoContable${count}"default="${contable.montoRestante?if_exists}" onChange="recalcularCobro('${count}');"/>
							<input type="hidden" name="secuenciaConBanco${count}" id="secuenciaConBanco${count}" value="${contable.secuencia}"/>
							<#assign count=count + 1/>
						<#else>
							<@displayCurrencyCell currencyUomId=parameters.orgCurrencyUomId amount=contable.montoRestante />
						</#if>	
																							
					</tr>
																	
				</#list>
				<#list montoCobrarPresupuestal as presupuestal>														
					<tr class="${tableRowClass(presupuestal_index)}" name="presupuestal" >
						<@displayCell text=presupuestal.secuencia/>
						<@displayCell text=presupuestal.descripcion/>						
						<@displayCell text=presupuestal.idCatalogoPres />
						<@displayCurrencyCell currencyUomId=parameters.orgCurrencyUomId amount=presupuestal.monto />
						<@displayCurrencyCell currencyUomId=parameters.orgCurrencyUomId amount=presupuestal.montoRestante />																					
																																												
					</tr>													
				</#list>
			</tbody>
		</table>
		
	</table>
	
		

</@frameSection>


<#if datosLineasCont?has_content>
	
    <#assign montoLineasCont = 0>
	<#list datosLineasCont as datos>
		<#assign montoLineasCont = montoLineasCont + datos.monto>
	</#list>
	
	<#assign extraCont><span class="boxhead">${uiLabelMap.FinancialsTotalLineasCont}</span><@displayCurrencyCell id="montoLineasCont" currencyUomId=parameters.orgCurrencyUomId amount=montoLineasCont class="boxhead"/></#assign>
	       			           		           		           		           	
	<@frameSection title=uiLabelMap.FinancialsLineasCont extra=extraCont>
			<table width="100%">
					
			<thead>		
				<tr>
					<th>${uiLabelMap.SecuenciaLineaContable}</th>
					<th>${uiLabelMap.Descripcion}</th>
					<th>${uiLabelMap.CatalogoCargo}</th>
					<th>${uiLabelMap.ValorCatalogoCargo}</th>
					<th>${uiLabelMap.CatalogoAbono}</th>				
					<th>${uiLabelMap.ValorCatalogoAbono}</th>				
					<th>${uiLabelMap.Monto}</th>
					<th>${uiLabelMap.MontoRestante}</th>						
					<th></th>								
				</tr>
			</thead>
			<tbody>
				<#assign count = 1 />														
				<#list datosLineasCont as datos>														
					<tr class="${tableRowClass(datos_index)}" name="datos" >
						<@displayCell text=datos.secuencia/>
						<@displayCell text=datos.descripcion />
						<@displayCell text=datos.catalogoCargo?if_exists?default("")/>
						<@displayCell text=datos.idCatalogoCargo?if_exists?default("")/>
						<@displayCell text=datos.catalogoAbono?if_exists?default("")/>
						<@displayCell text=datos.idCatalogoAbono?if_exists?default("")/>												
						<@displayCurrencyCell currencyUomId=parameters.orgCurrencyUomId amount=datos.monto />																																						
						<#if datos.idCatalogoAbono?exists && datos.idCatalogoAbono.equals("BANCO")>													
								<@displayCurrencyCell currencyUomId=parameters.orgCurrencyUomId amount=datos.montoRestante />
						<#elseif datos.idCatalogoCargo?exists && datos.idCatalogoCargo.equals("BANCO")>							
								<@displayCurrencyCell currencyUomId=parameters.orgCurrencyUomId amount=datos.montoRestante />														
						<#else>	
							<#if (datos.montoRestante > 0)>
								<@inputTextCell name="montoContableSinBanco${count}" id="montoContableSinBanco${count}" default="${datos.montoRestante?if_exists}" size=10/>
								<input type="hidden" name="secuenciaSinBanco${count}" id="secuenciaSinBanco${count}" value="${datos.secuencia}"/>
							<#else>	
								<@displayCurrencyCell currencyUomId=parameters.orgCurrencyUomId amount=datos.montoRestante />
								<input type="hidden" name="secuenciaSinBanco${count}" id="secuenciaSinBanco${count}" value="${datos.secuencia}"/>
								<input type="hidden" name="montoContableSinBanco${count}" id="montoContableSinBanco${count}" value="${datos.montoRestante?if_exists}"/>								
							</#if>
							<#assign count=count + 1/>	
						</#if>																																							
					</tr>
				</#list>
			</tbody>
		</table>
	</@frameSection>
</#if>

<#if datosLineasPres?has_content>	      

	<#assign montoLineasPres = 0>
	<#list datosLineasPres as datos>
		<#assign montoLineasPres = montoLineasPres + datos.monto>
	</#list>
	
	<#assign extraPres><span class="boxhead">${uiLabelMap.FinancialsTotalLineasPres}</span><@displayCurrencyCell id="montoLineasPres" currencyUomId=parameters.orgCurrencyUomId amount=montoLineasPres class="boxhead"/></#assign>
     			           		           		           		           	
	<@frameSection title=uiLabelMap.FinancialsLineasPres extra=extraPres>		
			<table width="100%">
					
			<thead>		
				<tr>
					<th>${uiLabelMap.FinancialsSecuenciaLineaPres}</th>
					<th>${uiLabelMap.Descripcion}</th>
					<th>${uiLabelMap.clavePresupuestal}</th>								
					<th>${uiLabelMap.Monto}</th>
					<th>${uiLabelMap.MontoRestante}</th>						
					<th></th>								
				</tr>
			</thead>
			<tbody>								
				<#list datosLineasPres as datos>														
					<tr class="${tableRowClass(datos_index)}" name="datos" >
						<@displayCell text=datos.secuencia/>
						<@displayCell text=datos.descripcion />
						<@displayCell text=datos.idCatalogoPres/>																	
						<@displayCurrencyCell currencyUomId=parameters.orgCurrencyUomId amount=datos.monto />																																						
						<@displayCurrencyCell currencyUomId=parameters.orgCurrencyUomId amount=datos.montoRestante />																																						
					</tr>
				</#list>
			</tbody>
		</table>
	</@frameSection>
</#if>

<#if listCobrosOrdenCobro?has_content>	      

	<@frameSection title=uiLabelMap.FinancialsCobrosRealizados>		
			<table width="100%">
					
			<thead>		
				<tr>
					<th>${uiLabelMap.FinancialsCobroId}</th>
					<th>${uiLabelMap.PartyDesdeCobro}</th>
					<th>${uiLabelMap.PartyHaciaCobro}</th>					
					<th>${uiLabelMap.Estatus}</th>
					<th>${uiLabelMap.Referencia}</th>								
					<th>${uiLabelMap.Comentario}</th>					
					<th>${uiLabelMap.BancoId}</th>
					<th>${uiLabelMap.NombreBanco}</th>
					<th>${uiLabelMap.CuentaBancariaId}</th>
					<th>${uiLabelMap.NombreCuenta}</th>
					<th>${uiLabelMap.DescripcionCuenta}</th>
					<th>${uiLabelMap.NumeroCuenta}</th>
					<th>${uiLabelMap.Monto}</th>			
					<th>${uiLabelMap.Poliza}</th>					                   						
				</tr>
			</thead>
			<tbody>								
				<#list listCobrosOrdenCobro as datos>														
					<tr class="${tableRowClass(datos_index)}" name="datos" >
						<@displayCell text=datos.paymentId/>
						<@displayCell text=datos.partyIdToName/>
						<@displayCell text=datos.partyIdFromName />
						<@displayCell text=datos.description/>
						<@displayCell text=datos.paymentRefNum/>
						<@displayCell text=datos.comments/>
						<@displayCell text=datos.bancoId/>
						<@displayCell text=datos.nombreBanco/>
						<@displayCell text=datos.cuentaBancariaId/>
						<@displayCell text=datos.nombreCuenta/>
						<@displayCell text=datos.descripcion/>
						<@displayCell text=datos.numeroCuenta/>																	
						<@displayCurrencyCell currencyUomId=parameters.orgCurrencyUomId amount=datos.amount />														
						<@displayLinkCell href="viewAcctgTrans?acctgTransId=${datos.acctgTransId?if_exists}" text=datos.poliza />														
					</tr>											
				</#list>
			</tbody>
		</table>
	</@frameSection>
</#if>	
