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
		{	var mapaClas = {};
			var requisicionId = document.getElementById('requisicionId').value;
			var detalleRequisicionId = document.getElementById('detalleRequisicionId'+contador).value;
		  	var cantidadDetalle = document.getElementById('cantidadDetalle'+contador).value;
			var montoDetalle = document.getElementsByName('montoDetalle'+contador)[0].value;
			var organizationPartyId = document.getElementsByName('organizationPartyId')[0].value;
			var descripcionDetalle = document.getElementById('descripcionDetalle'+contador).value;
			var customTimePeriodId = document.getElementById('customTimePeriodId'+contador).value;
			
			for (i = 1; i <= 15; i++) 
			{	var clasificacion = document.getElementById('clasificacion'+i+'_'+contador);
				if(clasificacion != null)
				{	mapaClas[i] = clasificacion.value;
				} 
				else 
				{	mapaClas[i] = '';
				}
			}
			
			if (requisicionId != "" && detalleRequisicionId != "" && cantidadDetalle != "" && montoDetalle != "") 
		    { 	var requestData = {'requisicionId' : requisicionId, 'detalleRequisicionId' : detalleRequisicionId, 'cantidadDetalle' : cantidadDetalle, 'montoDetalle' : montoDetalle,
		    						'descripcionDetalle' : descripcionDetalle , 'customTimePeriodId' : customTimePeriodId,
		    						'clasificacion1' : mapaClas[1], 'clasificacion2' : mapaClas[2], 'clasificacion3' : mapaClas[3], 'clasificacion4' : mapaClas[4], 
		    						'clasificacion5' : mapaClas[5], 'clasificacion6' : mapaClas[6], 'clasificacion7' : mapaClas[7], 'clasificacion8' : mapaClas[8], 
		    						'clasificacion9' : mapaClas[9], 'clasificacion10' : mapaClas[10], 'clasificacion11' : mapaClas[11], 'clasificacion12' : mapaClas[12], 
		    						'clasificacion13' : mapaClas[13], 'clasificacion14' : mapaClas[14], 'clasificacion15' : mapaClas[15], 'organizationPartyId' : organizationPartyId};		    
		    								    		    			    
		    	opentaps.sendRequest('actualizaMontosItems', requestData, function(data) {responseActualizaItems(data)});
		    }
			
		}

		//Functions load the response from the server
		function responseActualizaItems(data) 
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
					document.getElementById('cargarPagina').submit();
				}	   
			
			}		
		}
		
		
		////Funcion para eliminar el registro de un item de una solicitud
		function contactTypeClickElimina(contador) 
		{	confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar) 
			{	var requisicionId = document.getElementById('requisicionId').value;
				var detalleRequisicionId = document.getElementById('detalleRequisicionId'+contador).value;
		  		if (requisicionId != "" && detalleRequisicionId != "") 
		    	{ 	var requestData = {'requisicionId' : requisicionId, 'detalleRequisicionId' : detalleRequisicionId};		    			    
		    		opentaps.sendRequest('eliminaItemsRequisicion', requestData, function(data) {responseEliminaItems(data)});
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
					var detalleRequisicionId = document.getElementById('cargarPagina').submit();
					
					
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
				var form = document.autorizarSolicitud;
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
					{	var form = document.rechazarSolicitud;
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
					{	var form = document.comentarSolicitud;
						document.getElementById('comentarioComentar').value = comentario;
						form.submit();
		        	}
				}
				else
				{	alert("Es necesario escribir un comentario")
				}				 				
			}						
		}
		
		////Funcion para cancelar la requisicion
		function contactTypeClickCancela(requisicion) 
		{	confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
		    var requisicionId = requisicion;	
			if (confirmar) 
			{		
		  		if (requisicionId != "") 
		    	{ 	var requestData = {'requisicionId' : requisicionId};
		    		opentaps.sendRequest('cancelaRequisicion', requestData, function(data) {responseCancelaRequisicion(data)});
		    	}				
			}													
		}
		
		//Functions load the response from the server
		function responseCancelaRequisicion(data) 
		{	for (var key in data) 
			{
			if(data[key]=="ERROR")
				{	alert("La requisici\u00f3n no se puede cancelar, es posible que ya se halla relizado un pedido o contrato con esta requisici\u00f3n");
				}
				else
				{	document.getElementById('cargarPagina').submit();	
				}    		
			}		
		}		
					
		  
// -->
</script>			    	    
<#assign rol = "" />	
<#if rolesAutorizador?has_content>
	<#list rolesAutorizador as rolAutorizador>
		<#if Requisicion?has_content && Requisicion.requisicionId?exists>	    		
    		<#if rolAutorizador.roleTypeId?exists && rolAutorizador.roleTypeId == "AUTORIZADOR" && Requisicion.statusId != 'CREADA' && Requisicion.statusId != 'COMENTADA'>
    			<#assign rol = "AUTORIZADOR" />		    					    	
    		<#else>
    			<#assign rol = "SOLICITANTE" />
    		</#if>		    		
    	</#if>			    				    
    </#list>		    		   
</#if>
	   

<#if !Requisicion.statusId.equals("CANCELADA") && rolesSolicitante?has_content> 
	<#assign LinkActualizar>
		<input name="submitButton" id="cancelar" type="button" class="subMenuButtonDangerous" value="${uiLabelMap.PurchasingCancela}" onClick="contactTypeClickCancela('${Requisicion.requisicionId}')"/>	
	</#assign>							
</#if>	
	    

<#assign urlHost = request.getServerName()+":"+request.getServerPort()/>
<#if Requisicion?has_content && Requisicion.requisicionId?exists>
	<#if Requisicion.statusId == 'CREADA' || Requisicion.statusId == 'COMENTADA' && rolesSolicitante?has_content>
		<#assign LinkActualizar>${LinkActualizar!''}<@submitFormLink form="enviarSolicitud" text=uiLabelMap.CommonEnviar /></#assign>
		<form method="post" action="<@ofbizUrl>enviarSolicitud</@ofbizUrl>" name="enviarSolicitud" style="margin: 0;" id="enviarSolicitud">
			<@inputHidden name="urlHost" value=urlHost/>
			<@inputHidden name="requisicionId" value=Requisicion.requisicionId />
		</form>
	</#if>	
</#if>

<@frameSection title=uiLabelMap.PurchRequisicion extra=LinkActualizar>
<form method="post" action="<@ofbizUrl>cargarPagina</@ofbizUrl>" name="cargarPagina" style="margin: 0;" id="cargarPagina">
	<table width="100%">		
		<@inputHidden name="requisicionId" value=Requisicion.requisicionId />
		<@inputHidden name="descripcion" value=Requisicion.descripcion />
		<@inputHidden name="tipoMoneda" value=Requisicion.tipoMoneda />
		<@inputHidden name="justificacion" value=Requisicion.justificacion />
		<@inputHidden name="organizationPartyId" value=Requisicion.organizationPartyId id="organizationPartyId"/>
		<@inputHidden name="areaPartyId" value=Requisicion.areaPartyId />
		<@displayDateRow title=uiLabelMap.FinancialsAccountigDate format="DATE_ONLY" date=Requisicion.fechaContable highlightStyle=""/>
		<tr>
	      <@displayTitleCell title=uiLabelMap.PurchRequisicionId />
	      <@displayLinkCell text=Requisicion.requisicionId href="editarRequisicion?requisicionId=${Requisicion.requisicionId}"/>
	    </tr>
		<@displayRow title=uiLabelMap.PurchDescription text=Requisicion.descripcion/>
		<@displayRow title=uiLabelMap.PurchJustification text=Requisicion.justificacion/>
		<@displayRow title=uiLabelMap.PurchMoneda text=Requisicion.tipoMoneda />
		<@displayRow title=uiLabelMap.CodigoSolicitante text=Requisicion.personaSolicitanteId />
		<#assign areaNombreObj = Static["org.opentaps.purchasing.order.ConsultarRequisicion"].obtenPartyGroup(delegator,Requisicion.areaPartyId)! />
		<@displayRow title=uiLabelMap.PurchArea text=areaNombreObj.groupName! />
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
	    
	    <#if autorizadores?has_content>	    	
	    	<tr>
	    	</tr>
	    	<tr>
	    	</tr>
	    	<tr>	    		
	    		<td><td>
      			<@displayTooltip text="Autorizadores" />
      			</td></td>
      		</tr>
      		<tr>
	    	</tr>	
	    	<#list autorizadores as autorizador>
	    		<@displayRow title="Autorizador: " text=autorizador.firstName+" "+autorizador.lastName />	    				    	
		    </#list>		    
	    </#if>
	    
	    
	    	    
	    <#if Requisicion?has_content && Requisicion.requisicionId?exists>
	    	<#if Requisicion.statusId == 'ENVIADA' && rol == "AUTORIZADOR">
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

	<#if Requisicion?has_content && Requisicion.requisicionId?exists>
	    	<#if Requisicion.statusId == 'ENVIADA' && rol == "AUTORIZADOR">
	    		<#if statusAutorizacion?has_content>
	    			<#list statusAutorizacion as autorizacion>
	    				<#if autorizacion.statusId != "AUTORIZADA">
							<td><td>			
								<form method="post" action="<@ofbizUrl>autorizarSolicitud</@ofbizUrl>" name="autorizarSolicitud">
									<@inputHidden name="urlHost" value=urlHost/>	
									<@inputHidden name="requisicionId" value=Requisicion.requisicionId/>
									<@inputHidden name="comentarioAutorizado" value="" id="comentarioAutorizado"/>												
								</form>
								<form method="post" action="<@ofbizUrl>rechazarSolicitud</@ofbizUrl>" name="rechazarSolicitud">
									<@inputHidden name="urlHost" value=urlHost/>	
									<@inputHidden name="requisicionId" value=Requisicion.requisicionId/>
									<@inputHidden name="comentarioRechazo" value="" id="comentarioRechazo"/>						
									
								</form>																											    									
								<form method="post" action="<@ofbizUrl>comentarSolicitud</@ofbizUrl>" name="comentarSolicitud">
									<@inputHidden name="urlHost" value=urlHost/>	
									<@inputHidden name="requisicionId" value=Requisicion.requisicionId/>
									<@inputHidden name="comentarioComentar" value="" id="comentarioComentar"/>
									
								</form>					    														
								<@displayLink href="javascript:autorizar();" text="${uiLabelMap.Autorizar}" class="subMenuButton"/>
								<@displayLink href="javascript:rechazar();" text="${uiLabelMap.Rechazar}" class="subMenuButtonDangerous"/>
								<@displayLink href="javascript:comentar();" text="${uiLabelMap.Comentar}" class="subMenuButton"/>													
							</td></td>
						</#if>
					</#list>	
				</#if>		
			</#if>		    	
	    </#if>	
	</table>
</@frameSection>

<#if Requisicion?has_content && Requisicion.requisicionId?exists>	    	
	<#if Requisicion.statusId == 'CREADA' || Requisicion.statusId == 'COMENTADA' && rolesSolicitante?has_content>
		<@frameSection title=uiLabelMap.PurchRequisicionItems>
		<form method="post" action="<@ofbizUrl>agregaItemRequisicion</@ofbizUrl>" name="agregaItemRequisicion" style="margin: 0;">
			<table border="0"  width="100%">
				<@inputHidden name="requisicionId" value=Requisicion.requisicionId/>
				<@inputHidden name="organizationPartyId" value=Requisicion.organizationPartyId/>
				<@inputAutoCompleteProductRow name="productId" title=uiLabelMap.ProductProductId default=detalleReqAnt.productId! titleClass="requiredField"/>
				<@inputCurrencyRow name="monto" currencyName="uomId" defaultCurrencyUomId=Requisicion.tipoMoneda disableCurrencySelect=true
							 title=uiLabelMap.PurchasingMontoEstimado default=detalleReqAnt.monto! titleClass="requiredField"/>
				<@inputTextRow name="descripcion" title=uiLabelMap.PurchDescriptionArt size=50 maxlength=250 default=detalleReqAnt.descripcion! titleClass="requiredField"/>
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
				<#if tagTypes?has_content>
				  <@displayCvesPresupRow tagTypes=tagTypes titleClass="requiredField"/>
				</#if>
				<@inputCurrencyRow name="cantidad" disableCurrencySelect=true title=uiLabelMap.CommonQuantity default=detalleReqAnt.cantidad! titleClass="requiredField" />
				<@inputSelectRow title=uiLabelMap.CommonMonth name="customTimePeriodId" id="customTimePeriodId" list=[] titleClass="requiredField"/>
				<@inputTextRow name="cantidadMeses" title="Cantidad de meses" size=2 maxlength=2 default="1" titleClass="requiredField"/>
				<@inputSubmitRow title=uiLabelMap.CommonAdd />
			</table>
		</form>
		</@frameSection>
	</#if>	
</#if>	

<#assign montoClaves = 0 />
  <#list detalleRequisicion as detalle>
  	<#assign montoClaves = montoClaves + (detalle.monto * detalle.cantidad) />
  </#list>
  <#assign extraOptions><span class="boxhead">${uiLabelMap.FinancialsTotalClaves}</span><@displayCurrencyCell id="montoTotal" currencyUomId=Requisicion.tipoMoneda amount=montoClaves class="boxhead"/></#assign>
	
<@frameSection title=uiLabelMap.PurchRequisicionItemsAdded extra=extraOptions >
	<#if detalleRequisicion?has_content>
	
	<table border="0" width="100%">
				
	<thead>		
		<tr>
			<th>${uiLabelMap.PurchRequisicionItemId}</th>
			<th>${uiLabelMap.CommonMonth}</th>
			<th>${uiLabelMap.ProductProductId}</th>
			<th>${uiLabelMap.PurchasingProduct}</th>
			<th>${uiLabelMap.PurchasingUnidadMedida}</th>
			<th>${uiLabelMap.PurchasingProcedencia}</th>
			<th>${uiLabelMap.PurchReqIVA}</th>
			<th>${uiLabelMap.PurchasingProductAvailable}</th>
			<th>${uiLabelMap.Estatus}</th>
			<th>${uiLabelMap.PurchDescriptionArt}</th>
			<th>${uiLabelMap.FinancialsEndDate}</th>
			<th>${uiLabelMap.CommonQuantity}</th>
			<th>${uiLabelMap.PurchasingMontoEstimado}</th>
			<th>${uiLabelMap.CommonTotal}</th>
			<th>${uiLabelMap.CommonAvailable}</th>			
			<th></th>
			<th></th>
			
		</tr>
	</thead>
	<tbody>
	<#assign autorizada = false />
	<#if statusAutorizacion?has_content>
		<#list statusAutorizacion as autorizacion>
			<#if autorizacion.statusId == "AUTORIZADA">
				<#assign autorizada = true />
			</#if>
		</#list>
	</#if>
		<#assign count = 1 />
		<#list detalleRequisicion as detalle>
		<#assign iva = detalle.iva?if_exists/>
		<#assign nombreProducto = detalle.productName/>
				<@inputHidden name="requisicionId" id="requisicionId" value=Requisicion.requisicionId/>
				<@inputHidden name="detalleRequisicionId${count}" id="detalleRequisicionId${count}" value=detalle.detalleRequisicionId/>				
						<tr class="${tableRowClass(detalle_index)}">
							<@displayCell text=detalle.detalleRequisicionId/>
							<#if Requisicion.statusId == 'CREADA' || Requisicion.statusId == 'COMENTADA' || rol == "AUTORIZADOR">
								<#if autorizada >
									<@displayCell text=detalle.mesId/>
								<#else>
									<@inputSelectCell name="customTimePeriodId${count}" id="customTimePeriodId${count}" 
										list=mapMesesXCiclo.get(detalle.ciclo)![] key="customTimePeriodId" displayField="periodNum" default=detalle.customTimePeriodId! />
								</#if>
							<#else>	
								<@displayCell text=detalle.mesId/>
							</#if>
							<@displayCell text=detalle.productId />
							<@displayCell text=nombreProducto?if_exists?default("") />
							<@displayCell text=detalle.abbreviation?if_exists?default("") />
							<@displayCell text=detalle.procedencia?if_exists?default("") />
							<#if iva == "Y" >
								<@displayCell text="Si" />
							<#else>
								<@displayCell text="No" />
							</#if>
							<@displayCell text=detalle.cantidadDisponibleA?if_exists?default("") />
							<@displayCell text=detalle.descripcionEstatus?if_exists?default("") />	
							<#if Requisicion.statusId == 'CREADA' || Requisicion.statusId == 'COMENTADA' || rol == "AUTORIZADOR">
								<#if autorizada >
									<@displayCell text=detalle.descripcionDetalle?if_exists />
									<@displayCell text=detalle.fechaEntrega?if_exists />	
									<@displayCell text=detalle.cantidad />
									<@displayCurrencyCell currencyUomId=Requisicion.tipoMoneda amount=detalle.monto />								
								<#else>
									<@inputTextCell name="descripcionDetalle${count}" id="descripcionDetalle${count}" size=30 maxlength=255 default=detalle.descripcionDetalle />
									<@displayCell text=detalle.fechaEntrega?if_exists />
									<@inputCurrencyCell name="cantidadDetalle${count}" id="cantidadDetalle${count}" disableCurrencySelect=true default=detalle.cantidad! />	
									<@inputCurrencyCell name="montoDetalle${count}" id="montoDetalle${count}" currencyName="uomId" defaultCurrencyUomId=Requisicion.tipoMoneda disableCurrencySelect=true default=detalle.monto/>						
								</#if>
							<#else>
								<@displayCell text=detalle.descripcionDetalle?if_exists />
								<@displayCell text=detalle.fechaEntrega?if_exists />
								<@displayCell text=detalle.cantidad />
								<@displayCurrencyCell currencyUomId=Requisicion.tipoMoneda amount=detalle.monto />
							</#if>
							<@displayCurrencyCell currencyUomId=Requisicion.tipoMoneda amount=detalle.monto*detalle.cantidad />
							<@displayCurrencyCell currencyUomId=Requisicion.tipoMoneda amount=detalle.disponible />
							<#if Requisicion?has_content && Requisicion.requisicionId?exists>
								<#if Requisicion.statusId == 'CREADA' || Requisicion.statusId == 'COMENTADA' || rol == "AUTORIZADOR">
									<#if statusAutorizacion?has_content>
	    								<#list statusAutorizacion as autorizacion>
	    									<#if autorizacion.statusId != "AUTORIZADA">										
												<td align="center" width="5%">
													<input name="submitButton" id="actualiza${count}"" type="submit" class="subMenuButton" value="${uiLabelMap.CommonUpdate}" onClick="contactTypeClickActualiza('${count}')"/>
												</td>
												<td align="center" width="7%">
													<input name="submitButton" id="elimina${count}"" type="submit" class="subMenuButtonDangerous" value="${uiLabelMap.PurchRequisicionElimiarItem}" onClick="contactTypeClickElimina('${count}')"/>								
												</td>
											</#if>
										</#list>
									<#else>
										<td align="center" width="5%">
											<input name="submitButton" id="actualiza${count}"" type="submit" class="subMenuButton" value="${uiLabelMap.CommonUpdate}" onClick="contactTypeClickActualiza('${count}')"/>
										</td>
										<td align="center" width="7%">
											<input name="submitButton" id="elimina${count}"" type="submit" class="subMenuButtonDangerous" value="${uiLabelMap.PurchRequisicionElimiarItem}" onClick="contactTypeClickElimina('${count}')"/>								
										</td>
									</#if>			
									<tr class="${tableRowClass(detalle_index)}">
						          		<td colspan="10" align="left">
						          		<@flexArea targetId="ClavePresupuestalUpdate${count}" title=uiLabelMap.ClavePresupuestal controlStyle="font-weight: bold;" >
						            		<table border="0" cellpadding="0" cellspacing="0" width="100%">			            
						              			<#if tagTypes?has_content>
						                			<@cvePresupItemsAprov tags=tagTypes item=detalle index=count />
						              			</#if>	
						            		</table>
						           		</@flexArea>
						          		</td>			        
			        				</tr>
								<#else>
									<tr class="${tableRowClass(detalle_index)}">
										<td colspan="8" valign="top">
											<@flexArea targetId="ClavePresupuestal${detalle.detalleRequisicionId}" title=uiLabelMap.ClavePresupuestal controlStyle="font-weight: bold;" >
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
								</#if>
							</#if>	
						</tr>																		
			<#assign count=count + 1/>
		</#list>
	</tbody>
	</table>
	</#if>
</@frameSection>

