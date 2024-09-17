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
	</#if>	
</#if>
<@frameSection title="Ver proyecto" extra=LinkActualizar>
<form method="post" action="<@ofbizUrl>cargarPaginaProyecto</@ofbizUrl>" name="cargarPaginaProyecto" style="margin: 0;" id="cargarPaginaProyecto">
	<table width="100%">		
		<@inputHidden name="proyectoId" value=Proyecto.proyectoId />
		<@inputHidden name="nombreProyecto" value=Proyecto.nombreProyecto />
		<@inputHidden name="tipoMoneda" value=Proyecto.tipoMoneda />
		<@inputHidden name="organizationPartyId" value=Proyecto.organizationPartyId id="organizationPartyId"/>
		<@inputHidden name="areaId" value=Proyecto.areaId!/>
		<@displayDateRow title=uiLabelMap.FinancialsAccountigDate format="DATE_ONLY" date=Proyecto.fecha highlightStyle=""/>
	    <tr>
	      <@displayTitleCell title=uiLabelMap.PurchProyectoId />
	      <@displayLinkCell text=Proyecto.proyectoId href="editarProyecto?proyectoId=${Proyecto.proyectoId}"/>
	    </tr>
	    <@displayRow title="Codigo Proyecto" text=Proyecto.codigoProyecto/>
		<@displayRow title=uiLabelMap.PurchDescription text=Proyecto.nombreProyecto/>
		<@displayRow title="Monto" text=Proyecto.monto/>
		<@displayRow title="Monto restante" text=Proyecto.montoRestante/>
		<@displayRow title=uiLabelMap.PurchMoneda text=Proyecto.tipoMoneda />
		<@displayRow title="Responsable" text=Proyecto.responsable />
		<@displayRow title=uiLabelMap.PurchArea text=Proyecto.areaId />	
	</table>
</form>
</@frameSection>

<@frameSection title="Participantes añadidos" extra=extraOptions >
	<#if ParticipantesProyecto?has_content>
	
	<table border="0" width="100%">
				
	<thead>		
		<tr>
			<th>Id Participante</th>
			<th>Nombre</th>
			<th>Apellidos</th>
			<th>Puesto</th>
			<th></th>
			<th></th>
			
		</tr>
	</thead>
	<tbody>
		<#assign count = 1 />
		<#list ParticipantesProyecto as participante>
				<@inputHidden name="proyectoId" id="proyectoId" value=Proyecto.proyectoId/>
				<@inputHidden name="detalleParticipanteId${count}" id="detalleParticipanteId${count}" value=participante.detalleParticipanteId/>
			<tr class="${tableRowClass(detalle_index)}">
				<@displayCell text=participante.detalleParticipanteId/>
				<@displayCell text=participante.nombre/>
				<@displayCell text=participante.apellidos/>
				<@displayCell text=participante.puesto/>
			</tr>	
		<#assign count=count + 1/>
		</#list>
	</tbody>
	</table>
	</#if>
</@frameSection>