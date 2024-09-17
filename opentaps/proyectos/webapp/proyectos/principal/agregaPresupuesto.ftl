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

<script language="JavaScript" type="text/javascript">
<!-- //
	   
  	   //Funcion para actualizar los campos del item de una solicitud
		function contactTypeClickActualiza(contador) {
			var mapaClas = {};
			var proyectoId = document.getElementById('proyectoId').value;
			var detallePresupuestoId = document.getElementById('detallePresupuestoId'+contador).value;
		  	var mes = document.getElementById('mes'+contador).value;
			var fechaInicio = document.getElementById('fechaInicio'+contador).value;
			var fechaFin = document.getElementById('fechaFin'+contador).value;
			var monto = document.getElementById('monto'+contador).value;
			if (proyectoId != "" && detallePresupuestoId != "") 
		    { 	
		    	var requestData = {'proyectoId' : proyectoId, 'detallePresupuestoId' : detallePresupuestoId, 'mes' : mes, 'fechaInicio' : fechaInicio,
		    						'fechaFin' : fechaFin , 'monto' : monto};		  
		    								    		    			    
		    	opentaps.sendRequest('actualizaEtapasPresupuesto', requestData, function(data) {responseActualizaEtapasPresupuesto(data)});
		    }
			
		}

		//Functions load the response from the server
		function responseActualizaEtapasPresupuesto(data) 
		{	for (var key in data) 
			{	
				if(data[key].indexOf("ERROR") != -1)
				{	
					var Error = data[key].split(':');
					if(Error[1]){
						alert(Error[1]);
					} else {
						alert("Ocurri\u00f3 un error al actualizar los campos");
					}
				}
				else
				{	
					alert("Se actualiz\u00f3 correctamente");	
					document.getElementById('cargarPaginaPresupuestoProyecto').submit();
				}	   
			
			}		
		}
		
		
		////Funcion para eliminar el registro de un item de una solicitud
		function contactTypeClickElimina(contador) 
		{	confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar) 
			{	var proyectoId = document.getElementById('proyectoId').value;
				var detallePresupuestoId = document.getElementById('detallePresupuestoId'+contador).value;
		  		if (proyectoId != "" && detallePresupuestoId != "") 
		    	{ 	var requestData = {'proyectoId' : proyectoId, 'detallePresupuestoId' : detallePresupuestoId};		    			    
		    		opentaps.sendRequest('eliminaEtapaPresupuesto', requestData, function(data) {responseEliminaPresupuesto(data)});
		    	}				
			}													
		}

		//Functions load the response from the server
		function responseEliminaPresupuesto(data) 
		{	for (var key in data) 
			{	if(data[key]=="ERROR")
				{	alert("Ocurri\u00f3 un error al eliminar el elemento");
				}
				else
				{	alert("El elemento se ha eliminado");
					var detalleParticipanteId = document.getElementById('cargarPaginaPresupuestoProyecto').submit();
					
					
				}	    		
			}		
		}
		
// -->
</script>

<#assign urlHost = request.getServerName()+":"+request.getServerPort()/>
<#if Proyecto?has_content && Proyecto.proyectoId?exists>
	<#if Proyecto.statusId == 'CREADO'>
		<#assign LinkActualizar>${LinkActualizar!''}<@submitFormLink form="agregaParticipantes" text="Siguiente" /></#assign>
		<form method="post" action="<@ofbizUrl>agregaParticipantes</@ofbizUrl>" name="agregaParticipantes" style="margin: 0;" id="guardarProyecto">
			<@inputHidden name="urlHost" value=urlHost/>
			<@inputHidden name="proyectoId" value=Proyecto.proyectoId />
		</form>
	</#if>	
</#if>
<@frameSection title="Ver proyecto" extra=LinkActualizar>
<form method="post" action="<@ofbizUrl>cargarPaginaPresupuestoProyecto</@ofbizUrl>" name="cargarPaginaPresupuestoProyecto" style="margin: 0;" id="cargarPaginaPresupuestoProyecto">
	<table width="100%">		
		<@inputHidden name="proyectoId" value=Proyecto.proyectoId />
		<@inputHidden name="nombreProyecto" value=Proyecto.nombreProyecto />
		<#--<@inputHidden name="tipoMoneda" value=Proyecto.tipoMoneda />-->
		<@inputHidden name="organizationPartyId" value=Proyecto.organizationPartyId id="organizationPartyId"/>
		<@inputHidden name="areaId" value=Proyecto.areaId!/>
		<@displayDateRow title=uiLabelMap.FinancialsAccountigDate format="DATE_ONLY" date=Proyecto.fecha highlightStyle=""/>
		<tr>
	      <@displayTitleCell title=uiLabelMap.PurchProyectoId />
	      <@displayLinkCell text=Proyecto.proyectoId href="editarProyecto?proyectoId=${Proyecto.proyectoId}"/>
	    </tr>
		<@displayRow title=uiLabelMap.PurchDescription text=Proyecto.nombreProyecto/>
		<#--<@displayRow title=uiLabelMap.PurchMoneda text=Proyecto.tipoMoneda />-->
		<@displayRow title="Responsable" text=Proyecto.responsable />
		<@displayRow title=uiLabelMap.PurchArea text=Proyecto.areaId />	
	</table>
</form>
</@frameSection>

<@frameSection title="Asignar presupuesto al proyecto">
	<form method="post" action="<@ofbizUrl>agregaPresupuestoProyecto</@ofbizUrl>" name="agregaPresupuestoProyecto" style="margin: 0;">
		<table border="0"  width="100%">
			<@inputHidden name="proyectoId" value=Proyecto.proyectoId/>
			<@inputHidden name="organizationPartyId" value=Proyecto.organizationPartyId/>
			<@inputTextRow name="mes" title="Etapa" size=50 maxlength=250 titleClass="requiredField"/>
			<@inputDateRow name="fechaInicio" title=uiLabelMap.CommonStartDate form="solicitudViatico" titleClass="requiredField"/>
			<@inputDateRow name="fechaFin" title=uiLabelMap.CommonEndDate form="solicitudViatico" titleClass="requiredField"/>
			<@inputTextRow name="monto" title="Monto" size=50 maxlength=250 titleClass="requiredField"/>
			<@inputSubmitRow title=uiLabelMap.CommonAdd />
		</table>
	</form>
</@frameSection>

<@frameSection title="Presupuesto" extra=extraOptions >
	<#if PresupuestoProyecto?has_content>
	
	<table border="0" width="100%">
				
	<thead>		
		<tr>
			<th>Id Presupuesto</th>
			<th>Mes</th>
			<th>Fecha inicial</th>
			<th>Fecha final</th>
			<th>Monto</th>
			<th></th>
			<th></th>
			
		</tr>
	</thead>
	<tbody>
		<#assign count = 1 />
		<#list PresupuestoProyecto as presupuesto>
				<@inputHidden name="proyectoId" id="proyectoId" value=Proyecto.proyectoId/>
				<@inputHidden name="detallePresupuestoId${count}" id="detallePresupuestoId${count}" value=presupuesto.detallePresupuestoId/>
			<tr class="${tableRowClass(detalle_index)}">
				<@displayCell text=presupuesto.detallePresupuestoId/>
				<@inputTextCell name="mes${count}" id="mes${count}" size=30 maxlength=255 default=presupuesto.mes />
				<@inputDateCell name="fechaInicio${count}" id="fechaInicio${count}" default=presupuesto.fechaInicial?if_exists />
				<@inputDateCell name="fechaFin${count}" id="fechaFin${count}" default=presupuesto.fechaFinal?if_exists />
				<@inputTextCell name="monto${count}" id="monto${count}" size=30 maxlength=255 default=presupuesto.monto />
			<td align="center" width="5%">
				<input name="submitButton" id="actualiza${count}"" type="submit" class="subMenuButton" value="${uiLabelMap.CommonUpdate}" onClick="contactTypeClickActualiza('${count}')"/>
			</td>
			<td align="center" width="7%">
				<input name="submitButton" id="elimina${count}"" type="submit" class="subMenuButtonDangerous" value="Eliminar" onClick="contactTypeClickElimina('${count}')"/>								
			</td>
				</tr>	
		<#assign count=count + 1/>
		</#list>
	</tbody>
	</table>
	</#if>
</@frameSection>