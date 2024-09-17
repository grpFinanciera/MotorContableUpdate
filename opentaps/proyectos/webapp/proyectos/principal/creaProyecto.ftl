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
<@frameSection title=uiLabelMap.ProyectosCreacion >
<form name="guardaProyecto" method="post" action="<@ofbizUrl>guardaProyecto</@ofbizUrl>">
	<table class="twoColumnForm">
    	<tbody>
    	<@inputHidden  name="oganizationPartyId" value=oganizationPartyId />
    		<tr>
   			 	<@displayTitleCell title=uiLabelMap.FechaProyecto titleClass="requiredField" />
    			<@inputDateCell name="fechaInicio"  />
  			</tr>
			<@inputTextRow name="nombreProyecto" title=uiLabelMap.NombreProyecto size=50 titleClass="requiredField"/>
			<@inputTextRow name="codigoProyecto" title=uiLabelMap.CodigoProyecto size=50 titleClass="requiredField"/>
			<@inputTextareaRow title="Objetivo" name="objetivo" default="" titleClass="requiredField"/>
			<@inputCurrencySelectRow name="tipoMoneda" title=uiLabelMap.CommonCurrency defaultCurrencyUomId="MXN" titleClass="requiredField"/>
			<@inputTextRow name="numConvenio" title=uiLabelMap.NumConvenio size=20 />
			<@inputTextRow name="nombreOrganismo" title=uiLabelMap.NombreOrganismo size=40 titleClass="requiredField" />
			<tr>
				<@displayTitleCell title="Administrador/Responsable Nombre" titleClass="requiredField"/>
				<@inputAutoCompletePartyCell name="responsable" size="25" default="${thisPartyId?if_exists}" onChange="supplierChanged()" filtro="PERSON" />
			</tr>
			<tr>
				<@displayTitleCell title="Responsable Técnico Nombre" titleClass="requiredField"/>
				<@inputAutoCompletePartyCell name="responsableT" size="25" default="${thisPartyId?if_exists}" onChange="supplierChanged()" filtro="PERSON" />
			</tr>
			<@inputSelectRow title=uiLabelMap.UnidadResponsable required=false list=listAreas displayField="groupName" name="partyId" default=partyId?if_exists />
			<@inputDateRangeRow title=uiLabelMap.PresupuestoFechaVigencia fromName="fromDate" thruName="thruDate" titleClass="requiredField"/>
			<@inputTextareaRow title="Productos esperados" name="productos" default="" titleClass="requiredField"/>
			<@inputTextRow name="observaciones" title="Observaciones" size=40/>
			<tr>
               	<td align="right" class="titleCell">
				<@display text=uiLabelMap.AccountingTipoDocumento class="requiredField" />
				</td>
               	<@inputSelectCell name="acctgTransTypeId" list=eventosContables?if_exists?sort_by("descripcion") id="acctgTransTypeId" key="acctgTransTypeId" displayField="descripcion" required=false defaultOptionText="" />
            </tr>
            <@inputSelectRow title=uiLabelMap.CommonFinBankName id="bancoIdOrigen" name="bancoIdOrigen" list=bancos key="bancoId" displayField="nombreBanco" onChange="cargaBanco();"  titleClass="requiredField"/>
			<@inputSelectRow title=uiLabelMap.FinancialsCuentaBancaria id="cuentaOrigen" name="cuentaOrigen" list=[]  titleClass="requiredField"/>
			<tr>
                <@displayTitleCell title="Auxiliar" titleClass="requiredField"/>
				<@inputAutoCompletePartyCell name="auxiliar" size="25" onChange="supplierChanged()" filtro="PROYECTOS_ESP" />
            </tr>
            <tr>
                <@displayTitleCell title="Auxiliar general del proyecto" titleClass="requiredField"/>
				<@inputAutoCompletePartyCell name="auxiliarProyecto" size="25" onChange="supplierChanged()" filtro="OT_DER_BIEN_SERV_CP" />
            </tr>
            <tr>
                <@displayTitleCell title="Auxiliar cuenta por cobrar" titleClass="requiredField"/>
				<@inputAutoCompletePartyCell name="auxiliarCuentaCobrar" size="25" onChange="supplierChanged()" filtro="CUENTAS_POR_COBRAR" />
            </tr>
            <#assign validaPresup = ["Si","No"]/>
			<tr>
				<@displayTitleCell title="Valida presupuesto del proyecto"/>
			    <td>
			    	<select name="validacion" class="inputBox">
			        	<#list validaPresup as validacion>
			            		<option value="${validacion}">${validacion}</option>
			        	</#list>
			        </select>
			    </td>
		    </tr>
            
    		<@inputSubmitRow title=uiLabelMap.CommonCreate />  

		</tbody>
  	</table>
</form>
</@frameSection>