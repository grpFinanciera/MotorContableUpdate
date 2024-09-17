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
    <!--
    	
	     var uno=0;
	     provincias = new Array(' ');
	     provincias[0] = new Array(' ');
	     sucursales=new Array();
	     sucursales[0] = new Array();
	     <#assign contador=1/>
	     <#list bancos as banco>
	     	<#assign valor=" "/>
	     	<#assign valor2=" "/>
	     	<#list sucursales as sucursales>
	     		<#if sucursales.bancoId==banco.bancoId>
	     			<#if sucursales.bancoId==banco.bancoId>
						<#assign valor=valor+"','"+sucursales.nombreSucursal/>
						<#assign valor2=valor2+"','"+sucursales.sucursalId/>
					</#if>
	     		</#if>
	     	</#list>
	     	<#assign valor=valor?replace("&aacute;","�")/>
	     	<#assign valor=valor?replace("&eacute;","�")/>
	     	<#assign valor=valor?replace("&iacute;","�")/>
	     	<#assign valor=valor?replace("&oacute;","�")/>
	     	<#assign valor=valor?replace("&uacute;","�")/>
	     	<#assign valor=valor?replace("&Aacute;","�")/>
	     	<#assign valor=valor?replace("&Eacute;","�")/>
	     	<#assign valor=valor?replace("&Iacute;","�")/>
	     	<#assign valor=valor?replace("&Oacute;","�")/>
	     	<#assign valor=valor?replace("&Uacute;","�")/>
	     	provincias[${contador}]=new Array('${valor?j_string}');
	     	sucursales[${contador}]=new Array('${valor2}');
	     	<#assign contador=contador+1/>
	     </#list>	   
	     
	     
	     function cambiar(formulario){
			  var i = 0;
			  var select1 = formulario['bancoId'];
			  var select2 = formulario['sucursalId'];
			  var vector = provincias[select1.selectedIndex];
			  var vector2= sucursales[select1.selectedIndex];
			  if(vector.length)
			  {	select2.length=vector.length;
			  }
			  	while(vector[i]){
				    select2.options[i].value = vector2[i];
				    select2.options[i].text = vector[i];
				    i++;
			  }
			  select2.options[0].selected = 1;
			  
		}
		
		 

		-->
    </script> 

<@frameSection title=uiLabelMap.GestionCuentasBancos>
  <form method="post" action="<@ofbizUrl>crearCuentaBancoRequest</@ofbizUrl>" name="crearCuentaBancoRequest"  onsubmint="valida(this.form)">
    <table class="twoColumnForm" style="border:0">
    <tr>
    	<td class="titleCell"><span><b><font  size=1 color=#B40404>Banco</font><b></span>
    	</td>
	    <td>	   	    
     <select name="bancoId" size="1"  onchange="cambiar(this.form)">
		    	<option value=" "></option>
		        <#list bancos as banco>
		        	<option  value="${banco.bancoId}">${banco.get("nombreBanco",locale)}</option>
		        </#list>
		    </select>	 
	    </td>
    </tr> 
      <tr>
  <td class="titleCell"><span><b><font  size=1 color=#B40404>Sucursal<font><b></span>
    	</td>
	    <td>  
      <select name="sucursalId">
		  <option value=" "></option>
	  </select>
	  	    </td>
    </tr>
    
      <@inputTextRow title=uiLabelMap.NombreCuenta name="nombreCuenta" titleClass="requiredField"/>
      <@inputTextRow title=uiLabelMap.NumeroCuenta name="numeroCuenta" size=60 maxlength=60 titleClass="requiredField" />      
      <td class="titleCell"><span><b><font  size=1 color=#B40404>Organizaci&oacute;n<font><b></span>
    	</td>
      <@inputLookupCell lookup="LookupPartyName" form="crearCuentaBancoRequest" name="partyId" />
      <@inputTextRow title=uiLabelMap.Descripcion name="descripcionCuenta" size=60  titleClass="requiredField"/>
      <!--<@inputTextRow title=uiLabelMap.Monto name="montoCuenta" size=60  titleClass="requiredField"/>-->
      
      <@inputSelectRow title=uiLabelMap.TipoCuentaBancaria required=false list=listTipoCuenta displayField="nombre" name="tipoCuentaId" id="tipoCuentaId" titleClass="requiredField"/>            
      <@inputSelectRow title=uiLabelMap.UsoCuentaBancaria required=false list=listUsoCuenta displayField="nombre" name="usoCuentaId" id="usoCuentaId" titleClass="requiredField"/>      
      
      <td class="titleCell"><span><b><font  size=1 color=#B40404>Periodo<font><b></span>
    	</td>
      <@inputDateTimeCell name="periodo" default=Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp() />
      <tr>
      <@displayCell text=uiLabelMap.CommonAmount blockClass="titleCell" blockStyle="width: 100px" class="requiredField" />
      <@inputCurrencyCell name="montoCuenta" currencyName="currency" defaultCurrencyUomId="MXN"/>
      </tr>            	     
      <@inputSubmitRow title=uiLabelMap.CommonCreate />
    </table>
  </form>
</@frameSection>
