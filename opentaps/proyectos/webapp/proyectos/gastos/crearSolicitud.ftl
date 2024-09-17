<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<script type="text/javascript">
/*<![CDATA[*/

var skipReqForAgreements = false;

/*
  AJAX handler.
  Create new <select/> of agreements, fill its options and replace existent input with new one.  
*/

//Functions to send request to server for load relate objects
	function contactTypeChanged() 
	{ 	 var proyectoId = document.getElementById('proyectoId').value;
		document.getElementById('detalleParticipanteId').innerHTML = '';
		document.getElementById('detallePresupuestoId').innerHTML = '';
		if (proyectoId) 
	    { 	var requestData = {'proyectoId' : proyectoId};		    			    
	    	opentaps.sendRequest('obtieneParticipantes', requestData, function(data) {cargarParticipantes(data)});
	    	opentaps.sendRequest('obtienePresupuesto', requestData, function(data) {cargarPresupuesto(data)});
	    }
		
	}
	
	function cargarParticipantes(data) 
	{	i = 0;				
		var lista = document.getElementById("detalleParticipanteId");		
		document.getElementById("detalleParticipanteId").innerHTML = "";
		for (var key in data) 
		{	if(data[key]=="ERROR")
			{	document.getElementById("detalleParticipanteId").innerHTML = "";
			}
			else
			{	lista.options[i] = new Option(data[key]);
				lista.options[i].value = key;					
				i++;
			}	    		
		}		
	}
	
	function cargarPresupuesto(data) 
	{	i = 0;				
		var lista = document.getElementById("detallePresupuestoId");		
		document.getElementById("detallePresupuestoId").innerHTML = "";
		for (var key in data) 
		{	if(data[key]=="ERROR")
			{	document.getElementById("detallePresupuestoId").innerHTML = "";
			}
			else
			{	lista.options[i] = new Option(data[key]);
				lista.options[i].value = key;					
				i++;
			}	    		
		}		
	}
	
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

<@frameSection title=uiLabelMap.GastosReservaApplication extra=submitButton>
	<@form name="solicitudGastoProyecto" url="registrarSolicitudGastoProyecto" >
		<table class="twoColumnForm" border="0">
			<tr>                	
				<@displayTitleCell title="Proyecto" titleClass="requiredField"/>
				<td>	   	    
					<select name="proyectoId" id="proyectoId" size="1" class="selectBox" onChange="contactTypeChanged(this.form);">
						<option value=""></option>
							<#list listProyectos as proyecto>
									<option  value="${proyecto.proyectoId}">${proyecto.get("codigoProyecto",locale)}-${proyecto.get("nombreProyecto",locale)}</option>
							</#list>
				    </select>	 
				</td>
			</tr>
			<tr>      
				<@displayTitleCell title="Participante" titleClass="requiredField" />
				<td>  
					<select name="detalleParticipanteId" id="detalleParticipanteId" size="1" class="selectBox">      				  				
					</select>		
				</td>	
			</tr>
			
			<tr>      
				<@displayTitleCell title="Etapa" titleClass="requiredField" />
				<td>  
					<select name="detallePresupuestoId" id="detallePresupuestoId" size="1" class="selectBox">      				  				
					</select>		
				</td>	
			</tr>
			<@inputTextareaRow name="concepto" title=uiLabelMap.GastosReservaConceptGasto titleClass="requiredField"/>
			<@inputCurrencySelectRow name="tipoMoneda" title=uiLabelMap.CommonCurrency defaultCurrencyUomId="MXN" titleClass="requiredField"/>
			<@inputDateRow name="fecha" title=uiLabelMap.CommonDate form="solicitudGastoReserva" titleClass="requiredField"/>
			<@inputCurrencyRow name="monto" title=uiLabelMap.GastosReservaMotSalic disableCurrencySelect=true titleClass="requiredField"/>
			<@inputTextRow title=uiLabelMap.GastosReservaProveedor id="proveedor" name="proveedor" size=30 maxlength="255"/>
			<@inputTextareaRow name="observaciones" title=uiLabelMap.GastosReservaMotObservaciones readonly=false />
			<tr>
		       	<td align="right" class="titleCell">
				<@display text=uiLabelMap.AccountingTipoDocumento class="requiredField" />
				</td>
		       	<@inputSelectCell name="eventoSolicitud" list=eventosContables?if_exists?sort_by("descripcion") id="acctgTransTypeId" key="acctgTransTypeId" displayField="descripcion" required=false defaultOptionText="" />
    		</tr>
    		<tr>
		        <@displayTitleCell title="Auxiliar" titleClass="requiredField"/>
				<@inputAutoCompletePartyCell name="auxiliar" size="25" onChange="supplierChanged()" />
		    </tr>
		    <@inputSelectRow title=uiLabelMap.CommonFinBankName id="bancoIdOrigen" name="bancoIdOrigen" list=bancos key="bancoId" displayField="nombreBanco" onChange="cargaBanco();"  />
			<@inputSelectRow title=uiLabelMap.FinancialsCuentaBancaria id="cuentaOrigen" name="cuentaOrigen" list=[] />
			<@inputSelectRow title=uiLabelMap.AccountingPaymentType name="paymentTypeId" ignoreParameters=true list=tipoPago key="paymentTypeId" displayField="description" default=paymType titleClass="requiredField"/>
			<@inputSubmitRow title=uiLabelMap.CommonCreate/>
		</table>
	</@form>
</@frameSection>