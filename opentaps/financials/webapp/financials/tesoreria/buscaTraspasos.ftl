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


<script  languaje="JavaScript">    
		function buscaCuentasBancarias(nombreCampoBanco, nombreCampoCuenta) 
		{   var bancoId = document.getElementById(nombreCampoBanco).value;
			if (bancoId != "")
		    { 	var requestData = {'bancoId' : bancoId, 'organizationPartyId': '${parameters.organizationPartyId}'};		    
		    	opentaps.sendRequest('obtieneCuentasBancarias', requestData, function(data) {cargarCuentasResponse(data,nombreCampoCuenta)});
		    }
			
		}

		//Functions load the response from the server
		function cargarCuentasResponse(data,nombreCampoCuenta) 
		{	i = 1;				
			var select = document.getElementById(nombreCampoCuenta);
			document.getElementById(nombreCampoCuenta).innerHTML = "";
			select.options[0] = new Option('');
			select.options[0].value = '';
			for (var key in data) 
			{	if(data[key]=="ERROR")
				{	document.getElementById(nombreCampoCuenta).innerHTML = "";
				}
				else
				{	select.options[i] = new Option(data[key]);
					select.options[i].value = key;					
				  	i++;
				}	    		
			}		
		}
</script>

<form name="buscaTraspasos" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <table class="twoColumnForm">
    <tbody>
    <#assign fechaHoy = Static["org.ofbiz.base.util.UtilDateTime"].nowSqlDate() />
	<#assign fechaInicioMes = Static["org.ofbiz.base.util.UtilDateTime"].getMonthStart(fechaHoy) />
      <@inputTextRow title=uiLabelMap.FinancialsTransactionId name="findAcctgTransId" size="20" maxlength="20"/>	
	  <@inputDateRangeRow title=uiLabelMap.FinancialsAccountigDate fromName="fechaContableDesde" thruName="fechaContableHasta"
				defaultFrom=fechaInicioMes defaultThru=fechaHoy />
      <@inputRangeRow title=uiLabelMap.FinancialsPostedAmount fromName="postedAmountFrom" thruName="postedAmountThru" size=10/>
      <@inputSelectRow title=uiLabelMap.BancoOrigen required=false list=bancos  displayField="nombreBanco" name="bancoOrigen" id="bancoOrigen" key="bancoId"
								default=bancoId?if_exists onChange="buscaCuentasBancarias('bancoOrigen','cuentaOrigen')"/>
	  <@inputSelectRow title=uiLabelMap.CuentaOrigen name="cuentaOrigen" id="cuentaOrigen" list=([]) />
      <@inputSelectRow title=uiLabelMap.BancoDestino required=false list=bancos  displayField="nombreBanco" name="bancoDestino" id="bancoDestino" key="bancoId"
								default=bancoId?if_exists onChange="buscaCuentasBancarias('bancoDestino','cuentaDestino')" />
	  <@inputSelectRow title=uiLabelMap.CuentaDestino name="cuentaDestino" id="cuentaDestino" list=([]) />
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
