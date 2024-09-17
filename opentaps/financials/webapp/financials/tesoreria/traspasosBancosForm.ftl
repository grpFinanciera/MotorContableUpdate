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
 *  
 *  @author Leon Torres (leon@opensourcestrategies.com)
-->


<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<!--
<script type="text/javascript">
    
    
    
		
		//Functions to send request to server for load relate objects
		function contactTypeChangedOrigen() 
		{   var bancoId = document.getElementById('bancoIdOrigen').value;
			if (bancoId != "") 
		    { 	var requestData = {'bancoId' : bancoId, 'organizationPartyId' : '${organizationPartyId}'};		    			    
		    	opentaps.sendRequest('obtieneCuentasBancarias', requestData, function(data) {cargarCuentasResponseOrigen(data)});
		    }
			
		}

		//Functions load the response from the server
		function cargarCuentasResponseOrigen(data) 
		{	i = 0;				
			var select = document.getElementById("cuentaOrigen");
			document.getElementById("cuentaOrigen").innerHTML = "";
			for (var key in data) 
			{	if(data[key]=="ERROR")
				{	document.getElementById("cuentaOrigen").innerHTML = "";
				}
				else
				{	select.options[i] = new Option(data[key]);
					select.options[i].value = key;					
				  	i++;
				}	    		
			}		
		}
		
		//Functions to send request to server for load relate objects
		function contactTypeChangedDestino() 
		{   var bancoId = document.getElementById('bancoIdDestino').value;
			if (bancoId != "") 
		    { 	var requestData = {'bancoId' : bancoId, 'organizationPartyId' : '${organizationPartyId}'};		    			    
		    	opentaps.sendRequest('obtieneCuentasBancarias', requestData, function(data) {cargarCuentasResponseDestino(data)});
		    }			
		}

		//Functions load the response from the server
		function cargarCuentasResponseDestino(data) 
		{	i = 0;				
			var select = document.getElementById("cuentaDestino");
			document.getElementById("cuentaDestino").innerHTML = "";
			for (var key in data) 
			{	if(data[key]=="ERROR")
				{	document.getElementById("cuentaDestino").innerHTML = "";
				}
				else
				{	select.options[i] = new Option(data[key]);
					select.options[i].value = key;					
				  	i++;
				}	    		
			}			
		}
		
</script>
-->
<form method="post" action="<@ofbizUrl>traspasosBancosRequest</@ofbizUrl>" name="traspasosBancosRequest">
  
  <table border="0" cellpadding="3" cellspacing="0">
     <tbody id="tableAllForm">
     <th>
	  	<@displayTooltip text="Referencia de movimiento" />
	  </th>
     <tr>
        <@displayTitleCell title=uiLabelMap.Monto titleClass="requiredField" />
        <@displayTitleCell title=uiLabelMap.CuentaDestino titleClass="requiredField" />
        <@displayTitleCell title=uiLabelMap.CuentaOrigen titleClass="requiredField" />
      </tr>
      <th>
        <hr>
	  </th>
     </tbody>
  </table>
  <table border="0" cellpadding="2" cellspacing="0">
     <tbody id="tableAllForm">
  <tr name="item" >
      <tr>
		<td class="titleCell" >
		<@display text=uiLabelMap.AccountingTipoDocumento+" :" class="requiredField"/>
		</td>
		<@inputSelectCell id="acctgTransTypeId" name="acctgTransTypeId" list=listEventos?sort_by("descripcion") 
                		key="acctgTransTypeId" displayField="descripcion" onChange="cambiaEvento(this.value,'2');" required=false defaultOptionText=""/>
	  </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialsAccountigDate titleClass="requiredField" />
        <@inputDateTimeCell name="fechaContable" default=Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp() />
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.CommonDescription />
        <@inputTextCell name="descripcion" />
      </tr>           
      <tr>
      	<@inputCurrencySelectRow name="moneda" title=uiLabelMap.CommonCurrency defaultCurrencyUomId="MXN" />
      </tr>  
  </tr>       
      <@inputSubmitRow title=uiLabelMap.CommonCreate />
     </tbody>
    </table>
</form>