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
 *@since      1.0
-->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl" />
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

<#if Adquisicion.statusId == 'VIGENTE'>
		<#assign LinkActualizar>${LinkActualizar!''}<@submitFormLink form="cancelarAdqProyecto" text="Cancelar" class="subMenuButtonDangerous"/></#assign>
		<form method="post" action="<@ofbizUrl>cancelarAdqProyecto</@ofbizUrl>" name="cancelarAdqProyecto" style="margin: 0;" id="cancelarAdqProyecto">
			<@inputHidden name="adqProyectoId" value=Adquisicion.adqProyectoId />
		</form>
	</#if>	

<@frameSection title="Ver proyecto" extra=LinkActualizar>
<form method="post" action="<@ofbizUrl>cargarPaginaAdqProyecto</@ofbizUrl>" name="cargarPaginaAdqProyecto" style="margin: 0;" id="cargarPaginaAdqProyecto">
	<table width="100%">		
		<@inputHidden name="adqProyectoId" value=Adquisicion.adqProyectoId />
		<@inputHidden name="descripcion" value=Adquisicion.descripcion />
		<@inputHidden name="tipoMoneda" value=Adquisicion.tipoMoneda />
		<@displayDateRow title=uiLabelMap.FinancialsAccountigDate format="DATE_ONLY" date=Adquisicion.fechaContable highlightStyle=""/>
	    <@displayRow title=uiLabelMap.PurchProyectoId text=Adquisicion.adqProyectoId/>
		<@displayRow title=uiLabelMap.PurchDescription text=Adquisicion.descripcion/>
		<@displayRow title="Monto" text=Adquisicion.montoTotal/>
		<@displayRow title=uiLabelMap.PurchMoneda text=Adquisicion.tipoMoneda />
	</table>
</form>
</@frameSection>

<#if Adquisicion?has_content && Adquisicion.adqProyectoId?exists>	    	
	<#if Adquisicion.statusId == 'VIGENTE'>
		<@frameSection title="Registro de pagos">
			<form method="post" action="<@ofbizUrl>pagoAdquisicion</@ofbizUrl>" name="pagoAdquisicion" style="margin: 0;">
				<table border="0"  width="100%">
					<@inputHidden name="adqProyectoId" value=Adquisicion.adqProyectoId/>
					<tr>
				        <@displayTitleCell title=uiLabelMap.FinancialsAccountigDate titleClass="requiredField" />
				        <@inputDateTimeCell name="fechaContable" default=Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp() />
				     </tr>
				      <@inputSelectRow title=uiLabelMap.CommonFinBankName id="bancoIdOrigen" name="bancoIdOrigen" list=bancos key="bancoId" displayField="nombreBanco" onChange="cargaBanco();"  />
			<@inputSelectRow title=uiLabelMap.FinancialsCuentaBancaria id="cuentaOrigen" name="cuentaOrigen" list=[] />
					<tr>
				       	<td align="right" class="titleCell">
						<@display text=uiLabelMap.AccountingTipoDocumento class="requiredField" />
						</td>
				       	<@inputSelectCell name="eventoPago" list=eventosContablesPago?if_exists?sort_by("descripcion") id="acctgTransTypeId" key="acctgTransTypeId" displayField="descripcion" required=false defaultOptionText="" />
    				</tr>
    				<@inputSelectRow title=uiLabelMap.AccountingPaymentType name="paymentTypeId" ignoreParameters=true list=tipoPago key="paymentTypeId" displayField="description" default=paymType/>
       				<@inputSubmitRow title="Pagar"/>
				</table>
			</form>
		</@frameSection>
	</#if>	
</#if>	