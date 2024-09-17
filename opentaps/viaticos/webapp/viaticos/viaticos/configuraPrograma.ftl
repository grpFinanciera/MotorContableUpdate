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
 *@author     
 *@since      1.0
-->
<script language="JavaScript" type="text/javascript">
<!-- //
////Funcion para habilitar el registro de un item de un programa
function contactTypeClickHabilita(contador) 
		{	confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar) {
				var programaId = document.getElementById('programaId'+contador).value;
		  		if (programaId != "") 
		    	{ 	var requestData = {'programaId' : programaId};		    			    
		    		opentaps.sendRequest('habilitaPrograma', requestData, function(data) {responseHabilitaPrograma(data)});
		    	}				
			}													
		}

		//Functions load the response from the server
		function responseHabilitaPrograma(data) 
		{	for (var key in data) 
			{	if(data[key]=="ERROR")
				{	alert("Ocurri\u00f3 un error al eliminar el elemento");
				}else if(data[key]=="ERROR2")
				{	alert("El elemento no puede ser habilitado");
				}
				else
				{	alert("El elemento se ha habilitado");
					var programaId = document.getElementById('cargarPaginaPrograma').submit();
					
					
				}	    		
			}		
		}

////Funcion para inhabilitar el registro de un item de un programa
		function contactTypeClickElimina(contador) 
		{	confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar) {
				var programaId = document.getElementById('programaId'+contador).value;
		  		if (programaId != "") 
		    	{ 	var requestData = {'programaId' : programaId};		    			    
		    		opentaps.sendRequest('eliminaPrograma', requestData, function(data) {responseEliminaPrograma(data)});
		    	}				
			}													
		}

		//Functions load the response from the server
		function responseEliminaPrograma(data) 
		{	for (var key in data) 
			{	if(data[key]=="ERROR")
				{	alert("Ocurri\u00f3 un error al inhabilitar el elemento");
				}else if(data[key]=="ERROR2")
				{	alert("El elemento no puede ser eliminado ya que se ha usado anteriormente");
				}
				else
				{	alert("El elemento se ha inhabilitado");
					var programaId = document.getElementById('cargarPaginaPrograma').submit();
					
					
				}	    		
			}		
		}
		
// -->
</script>

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl" />
<@frameSection title="" extra=LinkActualizar>
<form method="post" action="<@ofbizUrl>cargarPaginaPrograma</@ofbizUrl>" name="cargarPaginaPrograma" style="margin: 0;" id="cargarPaginaPrograma">
	<table width="100%">		
	</table>
</form>
</@frameSection>
<@frameSection title="Asignar proyectos base para viaticos">
	<form method="post" action="<@ofbizUrl>guardaPrograma</@ofbizUrl>" name="guardaPrograma" style="margin: 0;" id="guardaPrograma">
		<table border="0"  width="100%">
			<@inputTextRow name="nombrePrograma" title="Proyecto base" size=50 titleClass="requiredField"/>
			<@inputSubmitRow title=uiLabelMap.CommonCreate />	
		</table>
	</form>
</@frameSection>

<@frameSection title="Proyectos añadidos" extra=extraOptions! >
	<#if Programa?has_content>
	
	<table border="0" width="100%">
				
	<thead>		
		<tr>
			<th>Id Proyecto Base</th>
			<th>Nombre Proyecto Base</th>
			<th>Estatus Proyecto Base</th>
			<th></th>
			<th></th>
			
		</tr>
	</thead>
	<tbody>
		<#assign count = 1 />
		<#list Programa as programa>
			<@inputHidden name="programaId${count}" id="programaId${count}" value=programa.programaId/>
			<tr class="${tableRowClass(detalle_index)}!">
				<@displayCell text=programa.programaId/>
				<@displayCell text=programa.nombrePrograma />
				<@displayCell text=programa.estatus/>
			<td align="center" width="7%">
				<input name="submitButton" id="habilita${count}"" type="submit" class="subMenuButton" value="Habilitar" onClick="contactTypeClickHabilita('${count}')"/>								
			</td>
			<td align="center" width="7%">
				<input name="submitButton" id="elimina${count}"" type="submit" class="subMenuButton" value="Inhabilitar" onClick="contactTypeClickElimina('${count}')"/>								
			</td>
				</tr>	
		<#assign count=count + 1/>
		</#list>
	</tbody>
	</table>
	</#if>
</@frameSection>