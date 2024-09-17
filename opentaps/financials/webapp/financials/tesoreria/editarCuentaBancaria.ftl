
<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<script type="text/javascript">    
		function contactTypeChanged() 
		{   var bancoId = document.getElementById('bancoId').value;
			if (bancoId != "") 
		    { 	var requestData = {'bancoId' : bancoId, 'organizationPartyId' : '${organizationPartyId}'};		    			    
		    	opentaps.sendRequest('obtieneCuentasBancarias', requestData, function(data) {cargarCuentasResponseBanco(data)});
		    }
			
		}

		function cargarCuentasResponseBanco(data) 
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
</script>

  		
<@frameSection title="Editar cuenta bancaria" >
	
<@form name="editarCuenta" url="editarCuentaBancaria" >
	<#list datosCuenta as cuenta>	
		<table>		
			<@displayRow title=uiLabelMap.IdCuenta text=cuentaId/>
			<input type="hidden" name="cuentaId" value="${cuentaId}"/>
			<input type="hidden" name="bancoId" value="${bancoId}"/>
			<@inputTextRow name="nombreCuenta" title=uiLabelMap.NombreCuenta default=cuenta.nombreCuenta?if_exists titleClass="requiredField"/>
			<@inputTextRow name="numeroCuenta" title=uiLabelMap.NumeroCuenta default=cuenta.numeroCuenta?if_exists titleClass="requiredField"/>
			<@inputTextRow name="descripcion" title=uiLabelMap.Descripcion default=cuenta.descripcion?if_exists titleClass="requiredField"/>
			<@inputSelectRow title=uiLabelMap.TipoCuentaBancaria required=false list=listTipoCuenta displayField="nombre" name="tipoCuentaId" id="tipoCuentaId" titleClass="requiredField" default=cuenta.tipoCuentaBancariaId?if_exists/>            
      		<@inputSelectRow title=uiLabelMap.UsoCuentaBancaria required=false list=listUsoCuenta displayField="nombre" name="usoCuentaId" id="usoCuentaId" titleClass="requiredField" default=cuenta.usoCuentaBancariaId?if_exists/>									
      		<@inputCurrencyRow name="montoCuenta" title=uiLabelMap.CommonAmount disableCurrencySelect=true default=saldo?if_exists currencyName="currency" defaultCurrencyUomId=cuenta.moneda titleClass="requiredField"/>      	
      		<tr>
      			<@displayCell text=uiLabelMap.cuentaHabilitada blockClass="titleCell" blockStyle="width: 100px" class="tableheadtext" />
         		<#assign cuentaHabilitada = ["S", "N"]/>
         		<td>
         			<select name="habilitadaId" class="inputBox" color=#B40404>
         				<option value="${cuenta.habilitada}">${cuenta.habilitada}</option>
         					<#list cuentaHabilitada as h>
            					<option value="${h}">${h}</option>
        					</#list>
        			</select>
         		</td>
      		</tr>    
	     	<!--<@displayTitleCell title=uiLabelMap.Banco/>-->
	        <!--<@inputSelectCell list=bancos?if_exists displayField="nombreBanco" name="bancoId" id="bancoId" default=cuenta.bancoId?if_exists onChange="contactTypeChanged(this.form);"/>-->        						 				  	
					    
			<@inputSubmitRow title=uiLabelMap.FinancialsEncumbranceRefresh />
		</table>
	</#list>	
</@form>
</@frameSection>