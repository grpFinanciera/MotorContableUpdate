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
 *@since      1.0
-->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl" />

<script language="JavaScript" type="text/javascript">
<!-- //
	   
  	   	////Funcion para eliminar el registro de un item de una solicitud
		function contactTypeClickElimina(contador) 
		{	confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar) 
			{	var adqProyectoId = document.getElementById('adqProyectoId').value;
				var detalleProductoId = document.getElementById('detalleProductoId'+contador).value;
		  		if (adqProyectoId != "" && detalleProductoId != "") 
		    	{ 	var requestData = {'adqProyectoId' : adqProyectoId, 'detalleProductoId' : detalleProductoId};		    			    
		    		opentaps.sendRequest('eliminaProductos', requestData, function(data) {responseEliminaProductos(data)});
		    	}				
			}													
		}

		//Functions load the response from the server
		function responseEliminaProductos(data) 
		{	for (var key in data) 
			{	if(data[key]=="ERROR")
				{	alert("Ocurri\u00f3 un error al eliminar el elemento");
				}
				else
				{	alert("El elemento se ha eliminado");
					var detalleProductoId = document.getElementById('cargarPaginaAdqProyecto').submit();
					
					
				}	    		
			}		
		}
		
// -->
</script>

<#assign urlHost = request.getServerName()+":"+request.getServerPort()/>
<#if Adquisicion?has_content && Adquisicion.adqProyectoId?exists>
	<#if Adquisicion.statusId == 'SOLICITADO'>
		<#assign LinkActualizar>${LinkActualizar!''}<@submitFormLink form="guardarAdquisicion" text="Guardar" /></#assign>
		<form method="post" action="<@ofbizUrl>guardarAdquisicion</@ofbizUrl>" name="guardarAdquisicion" style="margin: 0;" id="guardarAdquisicion">
			<@inputHidden name="urlHost" value=urlHost/>
			<@inputHidden name="adqProyectoId" value=Adquisicion.adqProyectoId />
		</form>
	</#if>
</#if>
<@frameSection title="Ver proyecto" extra=LinkActualizar>
<form method="post" action="<@ofbizUrl>cargarPaginaAdqProyecto</@ofbizUrl>" name="cargarPaginaAdqProyecto" style="margin: 0;" id="cargarPaginaAdqProyecto">
	<table width="100%">		
		<@inputHidden name="adqProyectoId" value=Adquisicion.adqProyectoId />
		<@inputHidden name="descripcion" value=Adquisicion.descripcion />
		<@inputHidden name="tipoMoneda" value=Adquisicion.tipoMoneda />
		<@displayDateRow title=uiLabelMap.FinancialsAccountigDate format="DATE_ONLY" date=Adquisicion.fechaContable highlightStyle=""/>
	    <@displayRow title=uiLabelMap.PurchProyectoId text=Adquisicion.adqProyectoId/>
		<@displayRow title=uiLabelMap.PurchDescription text=Adquisicion.descripcion/>
		<@displayRow title="Monto" text=Adquisicion.montoTotal/>
		<@displayRow title=uiLabelMap.PurchMoneda text=Adquisicion.tipoMoneda />
	</table>
</form>
</@frameSection>
<#if Adquisicion?has_content && Adquisicion.adqProyectoId?exists>	    	
	<#if Adquisicion.statusId == 'SOLICITADO'>
		<@frameSection title=uiLabelMap.PurchRequisicionItems>
			<form method="post" action="<@ofbizUrl>agregaProductoAdq</@ofbizUrl>" name="agregaProductoAdq" style="margin: 0;">
				<table border="0"  width="100%">
					<@inputHidden name="adqProyectoId" value=Adquisicion.adqProyectoId/>
					<@inputAutoCompleteProductRow name="productId" title=uiLabelMap.ProductProductId titleClass="requiredField"/>
					<@inputCurrencyRow name="monto" currencyName="uomId" defaultCurrencyUomId=Adquisicion.tipoMoneda disableCurrencySelect=true
								 title=uiLabelMap.PurchasingMontoEstimado titleClass="requiredField"/>
					<@inputTextRow name="descripcion" title=uiLabelMap.PurchDescriptionArt size=50 maxlength=250 titleClass="requiredField"/>
					<tr>
						<@displayTitleCell title=uiLabelMap.FinancialsEndDate titleClass="requiredField" />
						<@inputDateTimeCell name="fechaEntrega" default=Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp() />
					</tr>
					<tr>
			 			<@displayTitleCell title=uiLabelMap.PurchProcedencia />
			 			<#assign procedencias = ["Nacional", "Extranjera"]/>
			 			<td>
			 				<select name="procedencia" class="inputBox">
			 					<option value=""></option>
			 					<#list procedencias as procedencia>
			    					<option value="${procedencia}">${procedencia}</option>
								</#list>
							</select>
			 			</td>
					</tr>
					<tr>
						<@displayTitleCell title=uiLabelMap.PurchReqIVA titleClass="requiredField" />
						<td><input type="checkbox" name="iva" value="Y" checked="checked" /> </td>
					</tr>
					<@inputCurrencyRow name="cantidad" disableCurrencySelect=true title=uiLabelMap.CommonQuantity titleClass="requiredField" />
					<tr>                	
				<@displayTitleCell title="Resguardante"/>
				<td>	   	    
					<select name="detalleParticipanteId" id="detalleParticipanteId" size="1" class="selectBox">
						<option value=""></option>
							<#list listParticipantes as participante>
									<option  value="${participante.detalleParticipanteId}">${participante.get("nombre",locale)} ${participante.get("apellidos",locale)}</option>
							</#list>
				    </select>	 
				</td>
			</tr>
					<@inputSubmitRow title=uiLabelMap.CommonAdd />
				</table>
			</form>
		</@frameSection>
	</#if>	
</#if>	

<@frameSection title="Productos añadidos" extra=extraOptions >
	<#if Productos?has_content>
	
	<table border="0" width="100%">
				
	<thead>		
		<tr>
			<th>Id Producto Adquisicion</th>
			<th>Id Producto</th>
			<th>Descripcion</th>
			<th>Monto</th>
			<th>Cantidad</th>
			<th></th>
			<th></th>
			
		</tr>
	</thead>
	<tbody>
		<#assign count = 1 />
		<#list Productos as producto>
				<@inputHidden name="adqProyectoId" id="adqProyectoId" value=Adquisicion.adqProyectoId/>
				<@inputHidden name="detalleProductoId${count}" id="detalleProductoId${count}" value=producto.detalleProductoId/>
			<tr class="${tableRowClass(detalle_index)}">
				<@displayCell text=producto.detalleProductoId/>
				<@displayCell text=producto.productId/>
				<@displayCell text=producto.descripcion/>
				<@displayCell text=producto.monto/>
				<@displayCell text=producto.cantidad/>
			<td align="center" width="7%">
			<#if Adquisicion.statusId == 'SOLICITADO'>
				<input name="submitButton" id="elimina${count}"" type="submit" class="subMenuButtonDangerous" value="Eliminar producto" onClick="contactTypeClickElimina('${count}')"/>
			</#if>								
			</td>
			</tr>	
		<#assign count=count + 1/>
		</#list>
	</tbody>
	</table>
	</#if>
</@frameSection>