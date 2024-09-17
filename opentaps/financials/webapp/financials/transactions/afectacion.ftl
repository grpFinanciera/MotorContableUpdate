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

<script language="JavaScript" type="text/javascript">
<!-- //
	var modulo;
	
	function cargaEventos(/*String*/ moduloId){
		var requestData = {'moduloId' : moduloId};		    			    
		    	opentaps.sendRequest('getEventosAfectacion', requestData, function(data) {llenaComboEventos(data)});
	}
	
	function llenaComboEventos(data){
		i = 0;				
		var select = document.getElementById("acctgTransTypeId");
		select.innerHTML = "";
		for (var key in data) 
		{	if(data[key]=="ERROR")
			{	select.innerHTML = "";
			}
			else
			{	select.options[i] = new Option(data[key]);
				select.options[i].value = key;					
			  	i++;
			}	    		
		}	
	}
	
		function cargaBanco(form) {
			document.getElementById("titleBanco").style.visibility = "hidden";
			document.getElementById("titleCuenta").style.visibility = "hidden";											
			var select1 = form['bancoId'];
			var select2 = form['cuentaBancariaId'];
			var select3 = form['titleBanco'];
			var select4 = form['titleCuenta'];
			
			validaModulo();
					
			if(Boolean(modulo))			
			{	select1.show();
				select2.show();
				document.getElementById("titleBanco").style.visibility = "visible";
				document.getElementById("titleCuenta").style.visibility = "visible";
			}
			else
			{	select1.hide();
				select2.hide();
				document.getElementById("titleBanco").style.visibility = "hidden";
				document.getElementById("titleCuenta").style.visibility = "hidden";
			}					   		    
  	   }
  	   
  	  function validaModulo() {
         new Ajax.Request('<@ofbizUrl>validaCatalogoBanco</@ofbizUrl>', {method:'get',
                asynchronous: false,
                onSuccess: function(transport) {
                    var data = transport.responseText.evalJSON(true);
                    modulo = data.modulo;                    
                },
	           onFailure: function(transport) {
	             var data = transport.responseText.evalJSON(true);
	             //console.log('Failure');
	           },parameters: $('acctgTransTypeId').serialize(), requestHeaders: {Accept: 'application/json'}
            });            
		}	
  	   
  	   //Functions to send request to server for load relate objects
		function cambiaBanco() 
		{   var bancoId = document.getElementById('bancoId').value;
			if (bancoId != "") 
		    { 	var requestData = {'bancoId' : bancoId, 'organizationPartyId' : '${organizationPartyId}'};		    			    
		    	opentaps.sendRequest('obtieneCuentasBancarias', requestData, function(data) {cargarCuentasResponseBanco(data)});
		    }
			
		}

		//Functions load the response from the server
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
// -->
</script>
          
<@frameSection title=uiLabelMap.AfectacionPresupuestal>
	
<form method="post" action="<@ofbizUrl>AfectacionMomentoActual</@ofbizUrl>" name="AfectacionMomentoActual" >
    
<body>
    <div class="form" style="border:0">
    <table class="fourColumnForm" >
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialTipoAfectacion titleClass="requiredField"/>
        <@inputSelectHashCell hash= {"":"","CONTABILIDAD_E":"EGRESO", "CONTABILIDAD_I":"INGRESO"} id="moduloId" name="moduloId" onChange="cargaEventos(this.value);"/>
      </tr> 
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialsTipoEvento titleClass="requiredField"/>
		<td>
		<select id="acctgTransTypeId" name="acctgTransTypeId" class="inputBox" >
		</select>
		</td> 
      </tr> 
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialsTransactionDate titleClass="requiredField"/>
        <@inputDateTimeCell name="fechaTransaccion" default=Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp() />
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialsAccountigDate titleClass="requiredField" />
        <@inputDateTimeCell name="fechaContable" default=Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp() />
      </tr> 
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialTipoAfectacion titleClass="requiredField"/>
        <@inputSelectHashCell hash= {"INTERNA":"Interna", "EXTERNA":"Externa"} name="tipoMovimiento"/>
      </tr>            
      <tr>
      	<@inputTextRow title=uiLabelMap.Comentario name="comentario" size=60 default=comentario?if_exists titleClass="requiredField" />
      </tr>
      <tr>
      	<@displayTitleCell title=uiLabelMap.AccountingCurrency titleClass="requiredField" />
		<@inputCurrencySelectCell name="currency" defaultCurrencyUomId="MXN" />
      </tr>
      <tr>        
        <input type="hidden" name="organizationPartyId" value="${organizationPartyId}"/>
        <input type="hidden" name="statusId" value="CREADA" />
      </tr>
      <tr>
        
     
   <td><label id="titleBanco" class="titleCell" align="left" style="display:none;" ><span><b><font  size=1 color=#000000>Banco</font><b></span></label></td>
	    <td>	   	    
     		<select name="bancoId" id="bancoId" size="1" style="display:none;" onchange="cambiaBanco(this.form)" default="" class="selectBox"> 
		    	<option value=""></option>
		        <#list bancos as banco>
		        	<option  value="${banco.bancoId}">${banco.get("nombreBanco",locale)}</option>
		        </#list>
		    </select>	 
	    </td>	    	    
      </tr>
      <tr>      
        <td><label id="titleCuenta" class="titleCell" align="left" style="display:none;"><span><b><font  size=1 color=#000000>Cuenta Bancaria</font><b></span></label></td>
	    <td>  
      		<select name="cuentaBancariaId" id="cuentaBancariaId" style="display:none;" size="1" class="selectBox">
		  		<option value=""></option>
	  		</select>	
	  	</td>	  	
	  </tr>		          
  	<tr>                                                                                                       
      <@inputSubmitRow title=uiLabelMap.CommonCreate />
    </table>
  </div>
  </body>
  </form>
</@frameSection>

<script type="text/javascript">
	var moduloId = document.getElementById('moduloId').value;
    opentaps.addOnLoad(cargaEventos(moduloId));
</script>
