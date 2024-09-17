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
 *@author     Rodrigo Ruiz
 *@since      1.0
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
		document.getElementById('detallePresupuestoId').innerHTML = '';
		if (proyectoId) 
	    { 	var requestData = {'proyectoId' : proyectoId};		    			    
	    	opentaps.sendRequest('obtienePresupuesto', requestData, function(data) {cargarPresupuesto(data)});
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
	
</script>

<@frameSection title="Adquisiciones" >
	<form id="creaAdquisicionForm" name="creaAdquisicionForm" method="post" action="<@ofbizUrl>RegistrarAdquisicion</@ofbizUrl>">
	  <table class="twoColumnForm">
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
			<@displayTitleCell title="Etapa" titleClass="requiredField" />
			<td>  
				<select name="detallePresupuestoId" id="detallePresupuestoId" size="1" class="selectBox">      				  				
				</select>		
			</td>	
		</tr>
		
		 <tr>
        <@displayTitleCell title=uiLabelMap.FinancialsAccountigDate titleClass="requiredField" />
        <@inputDateTimeCell name="fechaContable" default=Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp() />
	     </tr> 
	    <tr>  
	      <@displayTitleCell title=uiLabelMap.PurchDescripcion titleClass="requiredField"/>
	      <@inputTextCell name="descripcion" maxlength="255" size=50/>
	    </tr>
		<tr>  
	      <@inputTextareaRow title=uiLabelMap.PurchJustification name="justificacion" default="" titleClass="requiredField"/>
	    </tr>
	    <tr>
            <@displayTitleCell title="Acreedor" titleClass="requiredField"/>
           	<@inputAutoCompletePartyCell name="supplierPartyId" size="25" default="${thisPartyId?if_exists}" onChange="supplierChanged()" filtro="ACREEDOR" />
        </tr>
        <@inputCurrencySelectRow name="tipoMoneda" title=uiLabelMap.CommonCurrency defaultCurrencyUomId="MXN" titleClass="requiredField"/>
        <tr>
		       	<td align="right" class="titleCell">
				<@display text=uiLabelMap.AccountingTipoDocumento class="requiredField" />
				</td>
		       	<@inputSelectCell name="eventoAdq" list=eventosContables?if_exists?sort_by("descripcion") id="acctgTransTypeId" key="acctgTransTypeId" displayField="descripcion" required=false defaultOptionText="" />
    		</tr>
    		<@inputSelectRow name="gastoProyecto" title="Descripcion del gasto"
				list=ObjetoGastoProyecto! key="gastoProyId" displayField="nombreObjetoGasto" titleClass="requiredField" />
        <@inputSubmitRow title=uiLabelMap.CommonCreate/>
	  </table>
	</form>
</@frameSection>