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
		
		////Funcion para eliminar el registro de un item de una solicitud
		function contactTypeClickElimina(contador) 
		{	confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar) 
			{	var proyectoId = document.getElementById('proyectoId').value;
				var modDetallePresupuestoId = document.getElementById('modDetallePresupuestoId'+contador).value;
		  		if (proyectoId != "" && modDetallePresupuestoId != "") 
		    	{ 	var requestData = {'proyectoId' : proyectoId, 'modDetallePresupuestoId' : modDetallePresupuestoId};		    			    
		    		opentaps.sendRequest('eliminaModificacionesProyecto', requestData, function(data) {responseEliminaModProyecto(data)});
		    	}				
			}													
		}

		//Functions load the response from the server
		function responseEliminaModProyecto(data) 
		{	for (var key in data) 
			{	if(data[key]=="ERROR")
				{	alert("Ocurri\u00f3 un error al eliminar el elemento");
				}else if (data[key]=="ERROR2"){
					alert("No se puede eliminar un elemento que ya fue aplicado");
				}else
				{	alert("El elemento se ha eliminado");
					var modDetallePresupuestoId = document.getElementById('cargarPaginaModProyecto').submit();
				}	    		
			}		
		}
		
// -->
</script>

<#assign urlHost = request.getServerName()+":"+request.getServerPort()/>
<#if Proyecto?has_content && Proyecto.proyectoId?exists>
	<#if Proyecto.statusId == 'VIGENTE'>
		<#assign LinkActualizar>${LinkActualizar!''}<@submitFormLink form="guardaModProyecto" text="Concluir" /></#assign>
		<form method="post" action="<@ofbizUrl>guardaModProyecto</@ofbizUrl>" name="guardaModProyecto" style="margin: 0;" id="guardaModProyecto">
			<@inputHidden name="urlHost" value=urlHost/>
			<@inputHidden name="proyectoId" value=Proyecto.proyectoId />
			<tr>
   			 	<@displayTitleCell title="Fecha contable" titleClass="requiredField" />
    			<@inputDateCell name="fechaContable"  />
  			</tr>
		</form>
	</#if>	
</#if>
<@frameSection title="Ver proyecto" extra=LinkActualizar>
<form method="post" action="<@ofbizUrl>cargarPaginaModProyecto</@ofbizUrl>" name="cargarPaginaModProyecto" style="margin: 0;" id="cargarPaginaModProyecto">
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

<@frameSection title="Modificar presupuesto del proyecto">
	<form method="post" action="<@ofbizUrl>agregaPresupuestoModProyecto</@ofbizUrl>" name="agregaPresupuestoModProyecto" style="margin: 0;">
		<table border="0"  width="100%">
			<@inputHidden name="proyectoId" value=Proyecto.proyectoId/>
			<@inputHidden name="organizationPartyId" value=Proyecto.organizationPartyId/>
			<tr>                	
				<@displayTitleCell title="Etapa"/>
				<td>	   	    
					<select name="detallePresupuestoId" id="detallePresupuestoId" size="1" class="selectBox"">
						<option value=""></option>
							<#list PresupuestoProyecto as etapaProyecto>
									<option  value="${etapaProyecto.detallePresupuestoId}">${etapaProyecto.get("mes",locale)}</option>
							</#list>
				    </select>	 
				</td>
			</tr>
			<@inputTextRow name="etapa" title="Etapa" size=50 maxlength=250/>
			<@inputDateRow name="fechaInicio" title=uiLabelMap.CommonStartDate/>
			<@inputDateRow name="fechaFin" title=uiLabelMap.CommonEndDate/>
			<tr>
				<@displayTitleCell title="Tipo de movimiento" titleClass="requiredField"/>
				<#assign tipoMovimientos = ["Ampliacion","Reduccion"]/>
			    <td>
			    	<select name="tipoMovimiento" class="inputBox">
			        	<#list tipoMovimientos as tipo>
			            		<option value="${tipo}">${tipo}</option>
			        	</#list>
			        </select>
			    </td>
			</tr>
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
			
		</tr>
	</thead>
	<tbody>
		<#assign count = 1 />
		<#list PresupuestoProyecto as presupuesto>
				<@inputHidden name="proyectoId" id="proyectoId" value=Proyecto.proyectoId/>
				<@inputHidden name="detalleProyectoId${count}" id="detalleProyectoId${count}" value=presupuesto.detallePresupuestoId/>
			<tr class="${tableRowClass(detalle_index)}">
				<@displayCell text=presupuesto.detallePresupuestoId/>
				<@displayCell text=presupuesto.mes/>
				<@displayDateCell date=presupuesto.fechaInicial?if_exists format="DATE_ONLY"/>
				<@displayDateCell date=presupuesto.fechaFinal?if_exists format="DATE_ONLY"/>
				<@displayCurrencyCell amount=presupuesto.monto?if_exists currencyUomId="MXN" class="textright"/>
				</tr>	
		<#assign count=count + 1/>
		</#list>
	</tbody>
	</table>
	</#if>
</@frameSection>
<@frameSection title="Modificado" extra=extraOptions >
	<#if PresupuestoProyecto?has_content>
	
	<table border="0" width="100%">
				
	<thead>		
		<tr>
			<th>Id Proyecto</th>
			<th>Id Modificacion</th>
			<th>Estatus</th>
			<th>Movimiento</th>
			<th>Monto</th>
			<th></th>
			
		</tr>
	</thead>
	<tbody>
		<#assign count = 1 />
		<#list ModificacionPresupuestoProyecto as modificacion>
				<@inputHidden name="proyectoId" id="proyectoId" value=Proyecto.proyectoId/>
				<@inputHidden name="modDetallePresupuestoId${count}" id="modDetallePresupuestoId${count}" value=modificacion.modDetallePresupuestoId/>
			<tr class="${tableRowClass(detalle_index)}">
				<@displayCell text=Proyecto.proyectoId/>
				<@displayCell text=modificacion.modDetallePresupuestoId/>
				<@displayCell text=modificacion.status/>
				<@displayCell text=modificacion.tipoMovimiento/>
				<@displayCurrencyCell amount=modificacion.monto?if_exists currencyUomId="MXN" class="textright"/>
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