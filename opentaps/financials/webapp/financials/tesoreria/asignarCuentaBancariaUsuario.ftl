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

<script type="text/javascript">
    	
    	
    	//Funcion para eliminar el permiso de una cuenta bancaria
		function Elimina(contador) 
		{	confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar) 
			{	var cuentaBancariaId = document.getElementById('cuentaBancariaId'+contador).value;
				var partyId = document.getElementById('partyId'+contador).value;				
		  		if (cuentaBancariaId != "" && partyId != "") 
		    	{ 	var requestData = {'cuentaBancariaId' : cuentaBancariaId, 'partyId' : partyId};		    			    
		    		opentaps.sendRequest('eliminaPermisoCuentaBancaria', requestData, function(data) {responseEliminaPermiso(data)});
		    	}				
			}													
		}

		//Functions load the response from the server
		function responseEliminaPermiso(data) 
		{	for (var key in data) 
			{	if(data[key]=="ERROR")
				{	alert("Ocurri\u00f3 un error al eliminar el elemento");
				}
				else
				{	alert("El elemento se ha eliminado");
					var cuentaBancariaId = document.getElementById('cargarPagina').submit();
					
					
				}	    		
			}		
		}
    
    
		
		//Functions to send request to server for load relate objects
		function bancoChanged() 
		{   var bancoId = document.getElementById('bancoId').value;
			if (bancoId != "") 
		    { 	var requestData = {'bancoId' : bancoId, 'organizationPartyId' : '${organizationPartyId}'};		    			    
		    	opentaps.sendRequest('obtieneCuentasBancarias', requestData, function(data) {cargarCuentasResponse(data)});
		    }
			
		}

		//Functions load the response from the server
		function cargarCuentasResponse(data) 
		{	i = 0;				
			var select = document.getElementById("cuenta");
			document.getElementById("cuenta").innerHTML = "";
			for (var key in data) 
			{	if(data[key]=="ERROR")
				{	document.getElementById("cuenta").innerHTML = "";
				}
				else
				{	select.options[i] = new Option(data[key]);
					select.options[i].value = key;					
				  	i++;
				}	    		
			}		
		}
		
</script>


<@frameSection title=uiLabelMap.asignarCuentaBancariaUsuario> 
<form method="post" action="<@ofbizUrl>asignarCuentaBancaria</@ofbizUrl>" name="asignarCuentaBancaria">
  
  <div class="form" style="border:0">
    <table class="fourColumnForm" style="border:0">
	<tr>
      <td class="titleCell">
        <span class="requiredField">${uiLabelMap.UsuarioCuenta}</span>
      </td>
      <td>
        <input type="text" size="20" name="partyId" class="inputBox">
        <a href="javascript:call_fieldlookup2(document.asignarCuentaBancaria.partyId,'LookupPerson');"><img src="/images/fieldlookup.gif" alt="Lookup" border="0" height="16" width="16"></a>
      </td>
    </tr>	    
      <tr>
        <td class="titleCell"><span class="requiredField"><b><font  size=1>Banco</font><b></span>
    	</td>
	    <td>	   	    
     		<select name="bancoId" id ="bancoId" size="1"  onchange="bancoChanged(this.form)">
		    	<option value=" "></option>
		        <#list bancos as banco>
		        	<option  value="${banco.bancoId}">${banco.get("nombreBanco",locale)}</option>
		        </#list>
		    </select>	 
	    </td>
      </tr>
      <tr>      
        <td class="titleCell"><span class="requiredField"><b><font  size=1>Cuenta<font><b></span>
    	</td>
	    <td>  
      		<select name="cuenta" id="cuenta" size="1" class="selectBox">      				  				
	  		</select>		
	  	</td>
	 </tr>
      <@inputSubmitRow title=uiLabelMap.CommonAsignar />
    </table>
  </div>
</form>
</@frameSection>

<#if listAsignaciones?has_content>
<form name="detalleGeneralForm" method="post" action="">
  <table width=100%>
	<td align="center"><font size=2><b>Asignaciones</b></font></td>	
  </table width=100%></td>
  <table class="twoColumnForm">
    <tbody>
      <tr>
      	<@displayCell text="Nombre de la cuenta" class="tableheadtext"/>
        <@displayCell text=uiLabelMap.NumeroCuenta class="tableheadtext"/>
        <@displayCell text=uiLabelMap.Descripcion class="tableheadtext"/>
        <@displayCell text="Nombre" class="tableheadtext"/>
        <@displayCell text="Apellido" class="tableheadtext"/>
        <td></td>
      </tr>
      <#assign count = 1 />
      <#list listAsignaciones as row>
        <tr class="${tableRowClass(row_index)}">
        	<@inputHidden name="cuentaBancariaId${count}" id="cuentaBancariaId${count}" value=row.cuentaBancariaId/>
        	<@inputHidden name="partyId${count}" id="partyId${count}" value=row.partyId/>
        	<@displayCell text=row.nombreCuenta/>
            <@displayCell text=row.numeroCuenta/>
            <@displayCell text=row.descripcion/>
            <@displayCell text=row.firstName/>
            <@displayCell text=row.lastName/>
            <td align="center" width="2%">
				<input name="submitButton" id="elimina${count}"" type="submit" class="subMenuButtonDangerous" value="${uiLabelMap.ElimiarPermiso}" onClick="Elimina('${count}')"/>								
			</td>
        </tr>
        <#assign count=count + 1/>
      </#list>   
    </tbody>
  </table>
</form>
</#if>