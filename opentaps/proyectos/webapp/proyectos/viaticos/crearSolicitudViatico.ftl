<#--
 * Copyright (c) Open Source Strategies, Inc.
 * 
 * Opentaps is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Opentaps is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Opentaps.  If not, see <http://www.gnu.org/licenses/>.
-->

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


<@frameSection title="RegistrarViatico">
	<@form name="RegistraViatico" url="RegistraViatico">
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
			
			
   
	<@inputTextareaRow title=uiLabelMap.ViaticoJustification name="motivo" default="" titleClass="requiredField"/>
	<@inputCurrencySelectRow name="tipoMoneda" title=uiLabelMap.CommonCurrency defaultCurrencyUomId="MXN" titleClass="requiredField"/>
	<@displayTitleCell title="Tipo de viático" titleClass="requiredField"/>
	<#assign tiposViaticos = ["Nacional","Internacional"]/>
	    <td>
	    	<select name="tipoViatico" class="inputBox">
	        	<#list tiposViaticos as tipo>
	            		<option value="${tipo}">${tipo}</option>
	        	</#list>
	        </select>
	    </td>
	<@inputSelectRow name="tipoTransporteId" title=uiLabelMap.PurchTransportType
		list=TiposTransportes! key="tipoTransporteId" displayField="descripcion" default="PUBLICO" titleClass="requiredField"/>
	<@inputCurrencyRow name="montoDiario" title=uiLabelMap.DailyAmount disableCurrencySelect=true titleClass="requiredField"/>
	<@inputCurrencyRow name="montoTrabCampo" title=uiLabelMap.FieldAmount disableCurrencySelect=true />
	<@inputCurrencyRow name="montoTransporte" title=uiLabelMap.PurchTransportAmount disableCurrencySelect=true titleClass="requiredField"/>
			
	<@inputAutoCompleteZonaGeoRow title=uiLabelMap.PurchOrigin name="geoOrigen" titleClass="requiredField"/>
	<@inputAutoCompleteZonaGeoRow title=uiLabelMap.PurchDestination name="geoDestino" titleClass="requiredField"/>
								
	<@inputDateRow name="fechaInicio" title=uiLabelMap.CommonStartDate form="RegistraViatico" titleClass="requiredField"/>
	<@inputDateRow name="fechaFin" title=uiLabelMap.CommonEndDate form="RegistraViatico" titleClass="requiredField"/>
	<tr>
       	<td align="right" class="titleCell">
		<@display text=uiLabelMap.AccountingTipoDocumento class="requiredField" />
		</td>
       	<@inputSelectCell name="eventoSolicitud" list=eventosContables?if_exists?sort_by("descripcion") id="acctgTransTypeId" key="acctgTransTypeId" displayField="descripcion" required=false defaultOptionText="" />
    </tr>
    <tr>
        <@displayTitleCell title="Auxiliar" titleClass="requiredField"/>
		<@inputAutoCompletePartyCell name="auxiliar" size="25" onChange="supplierChanged()" filtro="PERSON" />
    </tr>
    <@inputSelectRow title=uiLabelMap.CommonFinBankName id="bancoIdOrigen" name="bancoIdOrigen" list=bancos key="bancoId" displayField="nombreBanco" onChange="cargaBanco();"  />
	<@inputSelectRow title=uiLabelMap.FinancialsCuentaBancaria id="cuentaOrigen" name="cuentaOrigen" list=[] />
	<@inputSelectRow title=uiLabelMap.AccountingPaymentType name="paymentTypeId" ignoreParameters=true list=tipoPago key="paymentTypeId" displayField="description" default=paymType titleClass="requiredField"/>
	<@inputSubmitRow title=uiLabelMap.CommonCreate />  
		</table>
	</@form>
</@frameSection>