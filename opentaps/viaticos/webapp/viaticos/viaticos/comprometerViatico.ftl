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
		function contactTypeClickActualiza(contador) 
		{	var viaticoId = document.getElementById('viaticoId').value;
			var detalleViaticoId = document.getElementById('detalleViaticoId'+contador).value;
		  	var montoDetalle = document.getElementsByName('montoDetalle'+contador)[0].value;
		  	if (viaticoId != "" && detalleViaticoId != "" && montoDetalle != "") 
		    { 	var requestData = {'viaticoId' : viaticoId, 'detalleViaticoId' : detalleViaticoId, 'montoDetalle' : montoDetalle};		    			    
		    	opentaps.sendRequest('actualizaMontosItemsViatico', requestData, function(data) {responseActualizaItems(data)});
		    }
			
		}

		//Functions load the response from the server
		function responseActualizaItems(data) 
		{	for (var key in data) 
			{	if(data[key]=="ERROR")
				{	alert("Ocurri\u00f3 un error al actualizar los campos");
				}
				else
				{	var detalleViaticoId = document.getElementById('comprometerViatico').submit();
				}	    		
			}		
		}
		
		
		////Funcion para eliminar el registro de un item de una solicitud
		function contactTypeClickElimina(contador) 
		{confirmar=confirm("ÀEst\u00e1 seguro?"); 
			if (confirmar) 
			{	var viaticoId = document.getElementById('viaticoId').value;
				var detalleViaticoId = document.getElementById('detalleViaticoId'+contador).value;
		  		if (viaticoId != "" && detalleViaticoId != "") 
		    	{ 	var requestData = {'viaticoId' : viaticoId, 'detalleViaticoId' : detalleViaticoId};		    			    
		    		opentaps.sendRequest('eliminaItemsViatico', requestData, function(data) {responseEliminaItems(data)});
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
					var detalleViaticoId = document.getElementById('comprometerViatico').submit();
				}	    		
			}		
		}			
		  
// -->
</script>

<#if Viatico?has_content && Viatico.viaticoId?exists && detalleViatico?has_content>
	<form method="post" action="<@ofbizUrl>impactaViatico</@ofbizUrl>" name="impactaViatico" style="margin: 0;" id="impactaViatico">
		<@inputHidden name="urlHost" value=urlHost/>		
		<@inputHidden name="viaticoId" value=Viatico.viaticoId/>			
	</form>
</#if>


<@frameSection title=uiLabelMap.PurchViatico extra=LinkActualizar>
<form method="post" action="<@ofbizUrl>comprometerViatico</@ofbizUrl>" name="comprometerViatico" style="margin: 0;" id="comprometerViatico">
	<table width="100%">
		<@inputHidden name="viaticoId" value=Viatico.viaticoId/>
		<@inputHidden name="descripcion" value=Viatico.descripcion/>
		<@inputHidden name="tipoMoneda" value=Viatico.tipoMoneda/>
		<@inputHidden name="justificacion" value=Viatico.justificacion/>		        
        <@inputHidden name="organizationPartyId" value=Viatico.organizationPartyId/>
        <@inputHidden name="areaPartyId" value=Viatico.areaPartyId/>
		<@displayRow title=uiLabelMap.PurchDescription text=Viatico.descripcion/>
		<@displayRow title=uiLabelMap.PurchJustification text=Viatico.justificacion/>
		<#assign areaNombreObj = Static["org.opentaps.purchasing.viaticos.ConsultarViatico"].obtenPartyGroup(delegator,Viatico.areaPartyId)! />
		<@displayRow title=uiLabelMap.PurchArea text=areaNombreObj.groupName! />		
	    <@displayRow title=uiLabelMap.PurchDailyAmount text=Viatico.montoDiario/>
		<@displayRow title=uiLabelMap.PurchTransportType text=Viatico.tipoTransporteId/>
		<@displayRow title=uiLabelMap.PurchTransportAmount text=Viatico.montoTransporte/>				
		<@displayRow title=uiLabelMap.PurchOrigin text=Viatico.geoOrigenId/>
		<@displayRow title=uiLabelMap.PurchDestination text=Viatico.geoDestinoId/>								
		<@displayRow title=uiLabelMap.CommonStartDate text=Viatico.fechaInicial/>
		<@displayRow title=uiLabelMap.CommonEndDate text=Viatico.fechaFinal/>
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
	</table>
</form>
</@frameSection>




		
		
		


<@frameSection title=uiLabelMap.PurchViaticoItems>
	<form method="post" action="<@ofbizUrl>agregaItemViatico</@ofbizUrl>" name="agregaItemViatico" style="margin: 0;">
		<table border="0"  width="100%">
			<@inputHidden name="viaticoId" value=Viatico.viaticoId/>
			<@inputHidden name="organizationPartyId" value=Viatico.organizationPartyId/>
			<@inputAutoCompleteProductRow name="productId" title=uiLabelMap.ProductProductId />
			<@inputCurrencyRow name="monto" currencyName="uomId" defaultCurrencyUomId=Viatico.tipoMoneda disableCurrencySelect=true title=uiLabelMap.PurchasingMontoEstimado />
			<#if tagTypes?has_content>
				<@displayCvesPresupRow tagTypes=tagTypes titleClass="requiredField"/>
			</#if>
			<@inputSubmitRow title=uiLabelMap.CommonAdd />
		</table>
	</form>
</@frameSection>

	
<@frameSection title=uiLabelMap.PurchViaticoItemsAdded >
	<#if detalleViatico?has_content>
	
	<table border="0" width="100%">
				
	<thead>		
		<tr>
			<th>${uiLabelMap.PurchViaticoItemId}</th>
			<th>${uiLabelMap.ProductProductId}</th>
			<th>${uiLabelMap.PurchasingProduct}</th>
			<th>${uiLabelMap.CommonTotal}</th>			
			<th></th>
			<th></th>
			
		</tr>
	</thead>
	<tbody>
		<#assign count = 1 />
		<#list detalleViatico as detalle>
		<#assign nombreProducto = detalle.getRelatedOne("Product")/>
				<@inputHidden name="viaticoId" id="viaticoId" value=Viatico.viaticoId/>
				<@inputHidden name="detalleViaticoId${count}" id="detalleViaticoId${count}" value=detalle.detalleViaticoId/>
						<tr class="${tableRowClass(detalle_index)}">
							<@displayCell text=detalle.detalleViaticoId/>
							<@displayCell text=detalle.productId />
							<@displayCell text=nombreProducto.productName?if_exists?default("") />
							<@inputCurrencyCell name="montoDetalle${count}" currencyName="uomId" defaultCurrencyUomId=Viatico.tipoMoneda disableCurrencySelect=true default=detalle.monto/>
							<td align="center" width="5%">
								<input name="submitButton" id="actualiza${count}"" type="submit" class="subMenuButton" value="${uiLabelMap.CommonUpdate}" onClick="contactTypeClickActualiza('${count}')"/>
							</td>
							<td align="center" width="7%">
								<input name="submitButton" id="elimina${count}"" type="submit" class="subMenuButtonDangerous" value="${uiLabelMap.PurchViaticoElimiarItem}" onClick="contactTypeClickElimina('${count}')"/>								
							</td>
							
						</tr>
						<tr class="${tableRowClass(detalle_index)}">
							<td colspan="8" valign="top">
							<@flexArea targetId="ClavePresupuestal${detalle.detalleViaticoId}" title=uiLabelMap.ClavePresupuestal controlStyle="font-weight: bold;" >
								<table border="0" cellpadding="0" cellspacing="0" width="100%">
									<tr>
									<#if tagTypes?has_content>
										<@clavesPresupDisplay tags=tagTypes entity=detalle partyExternal=detalle.acctgTagEnumIdAdmin/>
									</#if>
									</tr>
								</table>
							</@flexArea>
							</td>
						<tr>
			
			<#assign count=count + 1/>
		</#list>
	</tbody>
	</table>
	</#if>
</@frameSection>

