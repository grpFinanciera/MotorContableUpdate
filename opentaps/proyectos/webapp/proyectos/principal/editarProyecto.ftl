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
 *@author     Salvador Cortes
 *@since      1.0
-->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl" />

<#assign urlHost = request.getServerName()+":"+request.getServerPort()/>
<#if Proyecto?has_content && Proyecto.proyectoId?exists>
	<#if Proyecto.statusId == 'CREADO'>
		<#assign LinkActualizar>${LinkActualizar!''}<@submitFormLink form="guardarProyecto" text="Guardar" /></#assign>
		<form method="post" action="<@ofbizUrl>guardarProyecto</@ofbizUrl>" name="guardarProyecto" style="margin: 0;" id="guardarProyecto">
			<@inputHidden name="urlHost" value=urlHost/>
			<@inputHidden name="proyectoId" value=Proyecto.proyectoId />
		</form>
	<#elseif Proyecto.statusId == 'VIGENTE'>
		<#assign LinkActualizar>${LinkActualizar!''}<@submitFormLink form="cancelarProyecto" text="Cancelar" class="subMenuButtonDangerous"/></#assign>
		<@inputHidden name="urlHost" value=urlHost/>
		<@inputHidden name="proyectoId" value=Proyecto.proyectoId />
	</#if>	
</#if>
<@frameSection title="Ver proyecto" extra=LinkActualizar>
<form method="post" action="<@ofbizUrl>cancelarProyecto</@ofbizUrl>" name="cancelarProyecto">
		<@inputHidden name="proyectoId" value=Proyecto.proyectoId/>												
</form>
<form method="post" action="<@ofbizUrl>actualizarProyecto</@ofbizUrl>" name="actualizarProyecto" style="margin: 0;" id="actualizarProyecto">
	<table width="100%">		
		<@inputHidden name="proyectoId" value=Proyecto.proyectoId />
		<@inputHidden name="nombreProyecto" value=Proyecto.nombreProyecto />
		<@inputHidden name="tipoMoneda" value=Proyecto.tipoMoneda />
		<@inputHidden name="organizationPartyId" value=Proyecto.organizationPartyId id="organizationPartyId"/>
		<@inputHidden name="areaId" value=Proyecto.areaId!/>
		<@displayDateRow title=uiLabelMap.FinancialsAccountigDate format="DATE_ONLY" date=Proyecto.fecha highlightStyle=""/>
	    <@displayRow title=uiLabelMap.PurchProyectoId text=Proyecto.proyectoId/>
		<@displayRow title=uiLabelMap.PurchDescription text=Proyecto.nombreProyecto/>
		<@displayRow title="Monto" text=Proyecto.monto/>
		<@displayRow title=uiLabelMap.PurchMoneda text=Proyecto.tipoMoneda />
		<@displayRow title="Responsable" text=Proyecto.responsable />
		<tr>  
	      <@inputTextareaRow title="Productos esperados" name="productos" default=Proyecto.productos! />
	    </tr>
		<tr>  
	      <@displayTitleCell title=uiLabelMap.PurchArea />
	      <@inputSelectCell key="partyId" list=listAreas name="partyId" displayField="groupName" default=Proyecto.areaId/>
	    </tr> 
	    
	    <#if Proyecto.validacion == 'Y'>
	    	<#assign validaPresup = ["Si","No"]/>
	    <#else>
	    	<#assign validaPresup = ["No","Si"]/>
	    </#if>
	    	
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
		    <@inputSubmitRow title=uiLabelMap.CommonUpdate />
	</table>
</form>
</@frameSection>