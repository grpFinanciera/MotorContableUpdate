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
<#--
 *  Copyright (c) 2001-2005 The Open For Business Project - www.ofbiz.org
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a
 *  copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  and/or sell copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 *  OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 *  OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 *  THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
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
	
	function sumaTotal(){
			var sueldo = parseFloat(document.getElementById("sueldo").value);
			var iva = parseFloat(document.getElementById("iva").value);
			var ivaR = parseFloat(document.getElementById("ivaR").value);
			var isr = parseFloat(document.getElementById("isr").value);
			var total = document.getElementById("montoTotal");
			if(isNaN(sueldo)){
				sueldo = 0;
			} 
			if(isNaN(iva)){
				iva = 0;
			}
			if(isNaN(ivaR)){
				ivaR = 0;
			}
			if(isNaN(isr)){
				isr = 0;
			} 
			total.value = sueldo + iva - ivaR - isr;
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


<@frameSection title="RegistrarServiciosPersonales">
	<@form name="RegistrarServiciosPersonales" url="RegistrarServiciosPersonales">
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
			<@inputDateRow name="fechaContable" title="Fecha contable" form="RegistrarServiciosPersonales" titleClass="requiredField"/>
			<@displayTitleCell title="Tipo de contratación" titleClass="requiredField"/>
			<#assign contrataciones = ["Sueldos complementarios","Honorarios asimilables", "Honorarios profesionales", "Becas"]/>
			    <td>
			    	<select name="tipoContratacion" class="inputBox">
			        	<#list contrataciones as contratacion>
			            		<option value="${contratacion}">${contratacion}</option>
			        	</#list>
			        </select>
			    </td>
			<@inputCurrencySelectRow name="tipoMoneda" title=uiLabelMap.CommonCurrency defaultCurrencyUomId="MXN" titleClass="requiredField"/>
		    <@inputCurrencyRow id="sueldo" name="sueldo" title="Sueldo complementario/Honorarios" disableCurrencySelect=true titleClass="requiredField" onChange="sumaTotal();"/>
		    <@inputCurrencyRow id="iva" name="iva" title="IVA" disableCurrencySelect=true titleClass="requiredField" onChange="sumaTotal();"/>
			<@inputCurrencyRow id="ivaR" name="ivaR" title="IVA Retenido" disableCurrencySelect=true titleClass="requiredField" onChange="sumaTotal();"/>
			<@inputCurrencyRow id="isr" name="isr" title="ISR" disableCurrencySelect=true titleClass="requiredField" onChange="sumaTotal();"/>
			<@inputCurrencyRow id="montoTotal" name="montoTotal" title="Monto total" disableCurrencySelect=true titleClass="requiredField"/>
			<@inputTextareaRow name="observaciones" title=uiLabelMap.GastosReservaMotObservaciones readonly=false />
			<@inputTextRow name="conceptoPoliza" title="Concepto de la Póliza" size=50 titleClass="requiredField"/>
			<@inputSelectRow name="gastoProyecto" title="Descripcion del gasto"
				list=ObjetoGastoProyecto! key="gastoProyId" displayField="nombreObjetoGasto" titleClass="requiredField" />
			<tr>
       		<td align="right" class="titleCell">
				<@display text=uiLabelMap.AccountingTipoDocumento class="requiredField" />
			</td>
       			<@inputSelectCell name="eventoServiciosPersonales" list=eventosContables?if_exists?sort_by("descripcion") id="acctgTransTypeId" key="acctgTransTypeId" displayField="descripcion" required=false defaultOptionText="" />
    		</tr>
			<tr>
                <@displayTitleCell title="Auxiliar" titleClass="requiredField"/>
				<@inputAutoCompletePartyCell name="auxiliar" size="25" onChange="supplierChanged()" filtro="ACREEDOR"/>
            </tr>
			<@inputSubmitRow title=uiLabelMap.CommonCreate /> 
	</@form>
</@frameSection>