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

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl" />

<script language="JavaScript" type="text/javascript">
<!-- //
	   
  	   //Funcion para actualizar los campos del item de una solicitud
		function contactTypeClickActualiza(contador) 
		{	var pedidoInternoId = document.getElementById('pedidoInternoId').value;
			var detallePedidoInternoId = document.getElementById('detallePedidoInternoId'+contador).value;
		  	var cantidadDetalle = document.getElementById('cantidadDetalle'+contador).value;
		  	if (pedidoInternoId != "" && detallePedidoInternoId != "" && cantidadDetalle != "") 
		    { 	
		    	var requestData = {'pedidoInternoId' : pedidoInternoId, 'detallePedidoInternoId' : detallePedidoInternoId, 'cantidadDetalle' : cantidadDetalle};		    			    
		    	opentaps.sendRequest('actualizaItemsPedidoInterno', requestData, function(data) {responseActualizaItems(data)});
		    }
			
		}

		//Functions load the response from the server
		function responseActualizaItems(data) 
		{	for (var key in data) 
			{	if(data[key]=="ERROR")
				{	alert("Ocurri\u00f3 un error al actualizar los campos");
				}
				else
				{	var detallePedidoInternoId = document.getElementById('actualizaPedidoInterno').submit();
				}	    		
			}		
		}
		
		
		////Funcion para eliminar el registro de un item de una solicitud
		function contactTypeClickElimina(contador) 
		{	confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar) 
			{	var pedidoInternoId = document.getElementById('pedidoInternoId').value;
				var detallePedidoInternoId = document.getElementById('detallePedidoInternoId'+contador).value;
		  		if (pedidoInternoId != "" && detallePedidoInternoId != "") 
		    	{ 	var requestData = {'pedidoInternoId' : pedidoInternoId, 'detallePedidoInternoId' : detallePedidoInternoId};		    			    
		    		opentaps.sendRequest('eliminaItemsPedidoInterno', requestData, function(data) {responseEliminaItems(data)});
		    	}				
			}													
		}

		//Functions load the response from the server
		function responseEliminaItems(data) 
		{	for (var key in data) 
			{	if(data[key]=="ERROR")
				{	alert("Ocurri\u00f3 un error al eliminar el elemento");
				}
				else
				{	alert("El elemento se ha eliminado");
					var detallePedidoInternoId = document.getElementById('actualizaPedidoInterno').submit();
				}	    		
			}		
		}				
		
		
		function autorizar() 
		{	confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar)
			{	var comentario = "";
				if(document.getElementsByName('comentario')[0].value != "")
				{	comentario = document.getElementsByName('comentario')[0].value;					
		        }		        				
				var form = document.autorizarPedidoInterno;
				document.getElementById('comentarioAutorizado').value = comentario;
				form.submit();		        					
			}					
		}
		
		function rechazar() 
		{	confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar)
			{	if(document.getElementsByName('comentario')[0].value != "")
				{	var comentario = document.getElementsByName('comentario')[0].value;
					if(comentario.trim() != "")
					{	var form = document.rechazarPedidoInterno;
						document.getElementById('comentarioRechazo').value = comentario;
						form.submit();
		        	}
		        }		        
				else
				{	alert("Es necesario escribir un comentario")
				} 				
			}					
		}
		
		function comentar() 
		{	confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar)
			{	if(document.getElementsByName('comentario')[0].value != "")
				{	var comentario = document.getElementsByName('comentario')[0].value;
					if(comentario.trim() != "")
					{	var form = document.comentarPedidoInterno;
						document.getElementById('comentarioComentar').value = comentario;
						form.submit();
		        	}
				}
				else
				{	alert("Es necesario escribir un comentario")
				}				 				
			}						
		}
					
		  
// -->
</script>			    	    
	    <#assign rol = "" />	    
	    <#if rolesAutorizador?has_content>
	    	<#list rolesAutorizador as rolAutorizador>
	    		<#if pedidoInterno?has_content && pedidoInterno.pedidoInternoId?exists>	    		
		    		<#if pedidoInterno.statusId != 'CREADA_PI' && pedidoInterno.statusId != 'COMENTADA_PI'>
		    			<#assign rol = 'AUTORIZADOR' />		    					    		
		    		<#else>
		    			<#assign rol = "SOLICITANTE" />
		    		</#if>		    		
		    	</#if>			    				    
		    </#list>		    		   
	    </#if>
	    

<#assign urlHost = request.getServerName()+":"+request.getServerPort()/>
<#if pedidoInterno?has_content && pedidoInterno.pedidoInternoId?exists>
	<#if pedidoInterno.statusId == 'CREADA_PI' || pedidoInterno.statusId == 'COMENTADA_PI' && rol != 'AUTORIZADOR'>
		<#assign LinkActualizar><@submitFormLink form="enviarPedidoInterno" text=uiLabelMap.CommonEnviar /></#assign>
		<form method="post" action="<@ofbizUrl>enviarPedidoInterno</@ofbizUrl>" name="enviarPedidoInterno" style="margin: 0;" id="enviarPedidoInterno">
			<@inputHidden name="urlHost" value=urlHost/>		
			<@inputHidden name="pedidoInternoId" value=pedidoInterno.pedidoInternoId/>						
		</form>
	</#if>	
</#if>


<@frameSection title=uiLabelMap.WarehousePedidoInterno extra=LinkActualizar>
<form method="post" action="<@ofbizUrl>actualizaPedidoInterno</@ofbizUrl>" name="actualizaPedidoInterno" style="margin: 0;" id="actualizaPedidoInterno">
	<table width="100%">
		<@inputHidden name="pedidoInternoId" value=pedidoInterno.pedidoInternoId/>
		<@inputHidden name="descripcion" value=pedidoInterno.descripcion/>
		<@inputHidden name="justificacion" value=pedidoInterno.justificacion/>	
		<@inputHidden name="areaPartyId" value=pedidoInterno.areaPartyId/>	        
        <input type="hidden" name='organizationPartyId' value="${pedidoInterno.organizationPartyId?if_exists}" />
        <@displayRow title=uiLabelMap.WarehousePedidoInternoId text=pedidoInterno.pedidoInternoId/>
		<@displayRow title=uiLabelMap.WarehouseDescripcion text=pedidoInterno.descripcion/>
		<@displayRow title=uiLabelMap.WarehouseJustification text=pedidoInterno.justificacion/>
		<@displayRow title=uiLabelMap.WarehouseStatus text=estatus.descripcion/>
		<tr> 
	      <@displayTitleCell title=uiLabelMap.WarehouseAlmacen />
	      <@inputSelectCell key="facilityId" list=listAlmacenes name="almacenId" displayField="facilityName" default=pedidoInterno.almacenId/>
	    </tr>
	    <#if comentarios?has_content>	    	
	    	<tr>
	    	</tr>
	    	<tr>
	    	</tr>
	    	<tr>	    		
	    		<td><td>
      			<@displayTooltip text="Comentarios" />
      			</td></td>
      		</tr>
      		<tr>
	    	</tr>	
	    	<#list comentarios as comentario>
	    		<@displayRow title="${comentario.firstName} "+"${comentario.lastName}: " text=comentario.comentario />	    				    	
		    </#list>		    
	    </#if>
	    
	    
	    	    
	    <#if pedidoInterno?has_content && pedidoInterno.pedidoInternoId?exists>
	    	<#if (pedidoInterno.statusId == 'ENVIADA_PI' && rol == 'AUTORIZADOR') ||(pedidoInterno.statusId == 'COMENTADA_PI' && rol == "SOLICITANTE")>
				<#if statusAutorizacion?has_content>
	    			<#list statusAutorizacion as autorizacion>
	    				<#if autorizacion.statusId != "AUTORIZADA">	    	   	    
	    					<@inputTextareaRow title=uiLabelMap.CommonComment name="comentario" default="" />
	    				</#if>
	    			</#list>
	    		</#if>
	    	</#if>		    	
	    </#if>			    		    	            
	
</form>


	<#if pedidoInterno?has_content && pedidoInterno.pedidoInternoId?exists>
		<#if statusAutorizacion?has_content>
	    	<#list statusAutorizacion as autorizacion>
	    		<#if autorizacion.statusId != "AUTORIZADA">
				<td><td>
			    	<#if (pedidoInterno.statusId == 'ENVIADA_PI' && rol == 'AUTORIZADOR')>
						<form method="post" action="<@ofbizUrl>autorizarPedidoInterno</@ofbizUrl>" name="autorizarPedidoInterno">
							<@inputHidden name="urlHost" value=urlHost/>	
							<@inputHidden name="pedidoInternoId" value=pedidoInterno.pedidoInternoId/>
							<@inputHidden name="comentarioAutorizado" value="" id="comentarioAutorizado"/>												
						</form>
						<form method="post" action="<@ofbizUrl>rechazarPedidoInterno</@ofbizUrl>" name="rechazarPedidoInterno">
							<@inputHidden name="urlHost" value=urlHost/>	
							<@inputHidden name="pedidoInternoId" value=pedidoInterno.pedidoInternoId/>
							<@inputHidden name="comentarioRechazo" value="" id="comentarioRechazo"/>						
						</form>
						<@displayLink href="javascript:autorizar();" text="${uiLabelMap.Autorizar}" class="subMenuButton"/>
						<@displayLink href="javascript:rechazar();" text="${uiLabelMap.Rechazar}" class="subMenuButtonDangerous"/>																											    									
					</#if>
					<#if (pedidoInterno.statusId == 'ENVIADA_PI' && rol == 'AUTORIZADOR') ||(pedidoInterno.statusId == 'COMENTADA_PI' && rol == "SOLICITANTE")>
						<form method="post" action="<@ofbizUrl>comentarPedidoInterno</@ofbizUrl>" name="comentarPedidoInterno">
							<@inputHidden name="urlHost" value=urlHost/>	
							<@inputHidden name="pedidoInternoId" value=pedidoInterno.pedidoInternoId/>
							<@inputHidden name="comentarioComentar" value="" id="comentarioComentar"/>
							<@inputHidden name="rol" value=rol id="rol"/>
						</form>					    														
						<@displayLink href="javascript:comentar();" text="${uiLabelMap.Comentar}" class="subMenuButton"/>
					</#if>														
				</td></td>
				</#if>
			</#list>	
		</#if>
	</#if>	
	</table>
</@frameSection>




		
		
		


<#if pedidoInterno?has_content && pedidoInterno.pedidoInternoId?exists>
	<#if pedidoInterno.statusId == 'CREADA_PI' || pedidoInterno.statusId == 'COMENTADA_PI' && rol != 'AUTORIZADOR'>
		<@frameSection title=uiLabelMap.WarehousePedidoInternoItems>
		<form method="post" action="<@ofbizUrl>agregaItemPedidoInterno</@ofbizUrl>" name="agregaItemPedidoInterno" style="margin: 0;">
			<table border="0"  width="100%">
				<@inputHidden name="pedidoInternoId" value=pedidoInterno.pedidoInternoId/>
				<input type="hidden" name='organizationPartyId' value="${pedidoInterno.organizationPartyId?if_exists}" />
				<@inputTextRow name="cantidad" title=uiLabelMap.CommonQuantity size=9 maxlength=9 />
				<@inputAutoCompleteProductRow name="productId" title=uiLabelMap.ProductProductId />
				<@inputSubmitRow title=uiLabelMap.CommonAdd />	    		
			</table>
		</form>
		</@frameSection>
	</#if>	
</#if>

<#if pedidoInterno?has_content && pedidoInterno.pedidoInternoId?exists>
	<#if pedidoInterno.statusId == 'CREADA_PI' || pedidoInterno.statusId == 'COMENTADA_PI' && rol != 'AUTORIZADOR'>
		<@frameSection title=uiLabelMap.WarehousePedidoInternoRequisicion>
		<form method="post" action="<@ofbizUrl>agregaRequisicionPedidoInterno</@ofbizUrl>" name="agregaRequisicionPedidoInterno" style="margin: 0;">
			<table border="0"  width="100%">
				<@inputHidden name="pedidoInternoId" value=pedidoInterno.pedidoInternoId/>
				<tr> 
	      			<@displayTitleCell title=uiLabelMap.Requisicion />
	      			<@inputSelectCell key="requisicionId" list=listRequisiciones name="requisicionId" displayField="requisicionId"/>
	    		</tr>
				<@inputSubmitRow title=uiLabelMap.CommonAdd />	    		
			</table>
		</form>
		</@frameSection>
	</#if>	
</#if>

<#if pedidoInterno?has_content && pedidoInterno.pedidoInternoId?exists>
	<#if pedidoInterno.statusId == 'CREADA_PI' || pedidoInterno.statusId == 'COMENTADA_PI' && rol != 'AUTORIZADOR'>
		<@frameSection title=uiLabelMap.WarehousePedidoInternoPedido>
		<form method="post" action="<@ofbizUrl>agregaOrdenPedidoInterno</@ofbizUrl>" name="agregaOrdenPedidoInterno" style="margin: 0;">
			<table border="0"  width="100%">
				<@inputHidden name="pedidoInternoId" value=pedidoInterno.pedidoInternoId/>
				<tr> 
	      			<@displayTitleCell title=uiLabelMap.OrdenCompra />
	      			<@inputSelectCell key="orderId" list=listOrdenes name="orderId" displayField="orderName"/>
	    		</tr>
				<@inputSubmitRow title=uiLabelMap.CommonAdd />	    		
			</table>
		</form>
		</@frameSection>
	</#if>	
</#if>

	
<@frameSection title=uiLabelMap.WarehousePedidoInternoItemsAdded >
	<#if detallePedidoInterno?has_content>
	
	<table border="0" width="100%">
				
	<thead>		
		<tr>
			<th>${uiLabelMap.WarehousePedidoInternoId}</th>
			<th>${uiLabelMap.ProductProductId}</th>
			<th>${uiLabelMap.PurchasingProduct}</th>
			<th>${uiLabelMap.CommonQuantity}</th>
			<th></th>
			<th></th>
			
		</tr>
	</thead>
	<tbody>
		<#assign count = 1 />
		<#list detallePedidoInterno as detalle>
		<#assign nombreProducto = detalle.getRelatedOne("idproductdProduct")/>
			<#--<form method="post" action="<@ofbizUrl>actualizaItem</@ofbizUrl>" name="actualizaItem${detalle.detallePedidoInternoId}" style="margin: 0;">-->
				<@inputHidden name="pedidoInternoId" id="pedidoInternoId" value=pedidoInterno.pedidoInternoId/>
				<@inputHidden name="detallePedidoInternoId${count}" id="detallePedidoInternoId${count}" value=detalle.detallePedidoInternoId/>
						<tr class="${tableRowClass(detalle_index)}">
							<@displayCell text=detalle.detallePedidoInternoId/>
							<@displayCell text=detalle.productId />
							<@displayCell text=nombreProducto.productName?if_exists?default("") />
							<@inputTextCell name="cantidadDetalle${count}" id="cantidadDetalle${count}" size=9 maxlength=9 default=detalle.cantidad />
							<#if pedidoInterno?has_content && pedidoInterno.pedidoInternoId?exists>
								<#if pedidoInterno.statusId == 'CREADA_PI' || pedidoInterno.statusId == 'COMENTADA_PI' && rol != 'AUTORIZADOR'>	
									<td align="center" width="5%">
										<input name="submitButton" id="actualiza${count}"" type="submit" class="subMenuButton" value="${uiLabelMap.CommonUpdate}" onClick="contactTypeClickActualiza('${count}')"/>
									</td>
									<td align="center" width="7%">
										<input name="submitButton" id="elimina${count}"" type="submit" class="subMenuButtonDangerous" value="${uiLabelMap.WarehousePedidoInternoElimiarItem}" onClick="contactTypeClickElimina('${count}')"/>								
									</td>
								</#if>	
							</#if>							
						</tr>			
			<#assign count=count + 1/>
		</#list>
	</tbody>
	</table>
	</#if>
</@frameSection>