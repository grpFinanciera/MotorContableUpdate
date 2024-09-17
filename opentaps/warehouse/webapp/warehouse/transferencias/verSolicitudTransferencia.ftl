<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl" />


<script language="JavaScript" type="text/javascript">
<!-- //
	   
  	   //Funcion para actualizar los campos del item de una solicitud
		function actualizaArticulo(contador) 
		{	var solicitudTransferenciaId = document.getElementById('solicitudTransferenciaId').value;
			var detalleSolicitudTransferId = document.getElementById('detalleSolicitudTransferId'+contador).value;
			var cantidadElemento = document.getElementById('cantidad'+contador);
		  	var cantidad = cantidadElemento.value;
		  	if (solicitudTransferenciaId != "" && detalleSolicitudTransferId != "" && cantidad != "") 
		    { 	
		    	var requestData = {'solicitudTransferenciaId' : solicitudTransferenciaId, 'detalleSolicitudTransferId' : detalleSolicitudTransferId, 'cantidad' : cantidad};		    			    
		    	opentaps.sendRequest('actualizaItemSolicitud', requestData, function(data) {responseActualizaItems(data,cantidadElemento)});
		    }
			
		};

		//Functions load the response from the server
		function responseActualizaItems(data,cantidadElemento) 
		{	for (var key in data) 
			{	if(data[key].indexOf("ERROR") != -1)
				{	
					var Error = data[key].split(':');
					if(Error[1]){
						alert(Error[1]);
						cantidadElemento.value = Error[2]; 
					} else {
						alert("Ocurri\u00f3 un error al actualizar los campos");
					}
				}
				else
				{	
					alert("Se actualiz\u00f3 correctamente");	
					document.getElementById('actualizaSolicitudTransferencia').submit();
				}	    		
			}		
		};
		
		
		////Funcion para eliminar el registro de un item de una solicitud
		function eliminaArticulo(contador) 
		{	confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar) 
			{	var solicitudTransferenciaId = document.getElementById('solicitudTransferenciaId').value;
				var detalleSolicitudTransferId = document.getElementById('detalleSolicitudTransferId'+contador).value;
		  		if (solicitudTransferenciaId != "" && detalleSolicitudTransferId != "") 
		    	{ 	var requestData = {'solicitudTransferenciaId' : solicitudTransferenciaId, 'detalleSolicitudTransferId' : detalleSolicitudTransferId};		    			    
		    		opentaps.sendRequest('eliminaItemSolicitud', requestData, function(data) {responseEliminaItems(data)});
		    	}				
			}													
		};

		//Functions load the response from the server
		function responseEliminaItems(data) 
		{	for (var key in data) 
			{	if(data[key]=="ERROR")
				{	alert("Ocurri\u00f3 un error al eliminar el elemento");
				}
				else
				{	alert("El elemento se ha eliminado");
					document.getElementById('actualizaSolicitudTransferencia').submit();
				}	    		
			}		
		};			
		
		
		function autorizar() 
		{	confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar)
			{	var comentario = "";
				if(document.getElementsByName('comentario')[0].value != "")
				{	
					comentario = document.getElementsByName('comentario')[0].value;					
		        }		        				
				var form = document.autorizarsolicitud;
				document.getElementById('comentarioAutorizado').value = comentario;
				document.getElementById('autorizarSolicitudTransferencia').submit();
			}					
		};
		
		function rechazar() 
		{	confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar)
			{	if(document.getElementsByName('comentario')[0].value != "")
				{	var comentario = document.getElementsByName('comentario')[0].value;
					if(comentario.trim() != "")
					{	
						var form = document.rechazarsolicitud;
						document.getElementById('comentarioRechazo').value = comentario;
						document.getElementById('rechazarSolicitudTransferencia').submit();
		        	}
		        }		        
				else
				{	alert("Es necesario escribir un comentario")
				} 				
			}					
		};
		
		function comentar() 
		{	confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar)
			{	if(document.getElementsByName('comentario')[0].value != "")
				{	var comentario = document.getElementsByName('comentario')[0].value;
					if(comentario.trim() != "")
					{	var form = document.comentarsolicitud;
						document.getElementById('comentarioComentar').value = comentario;
						document.getElementById('comentarSolicitudTransferencia').submit();
		        	}
				}
				else
				{	alert("Es necesario escribir un comentario")
				}				 				
			}						
		};
		  
// -->
</script>			

<#assign rol = "" />	    
<#if rolesAutorizador?has_content>
	<#list rolesAutorizador as rolAutorizador>
		<#if solicitud?has_content && solicitud.solicitudTransferenciaId?exists>	    		
    		<#if solicitud.statusId != 'CREADA_ST' && solicitud.statusId != 'COMENTADA_ST'>
    			<#assign rol = 'AUTORIZADOR' />		    					    		
    		<#else>
    			<#assign rol = "SOLICITANTE" />
    		</#if>		    		
    	</#if>			    				    
    </#list>		    		   
</#if>

<#assign urlHost = request.getServerName()+":"+request.getServerPort()/>
<#if solicitud?has_content && solicitud.solicitudTransferenciaId?exists>
	<#if solicitud.statusId == 'CREADA_ST' || solicitud.statusId == 'COMENTADA_ST' && rol != 'AUTORIZADOR'>
		<#assign LinkActualizar><@submitFormLink form="enviarSolicitudTransferencia" text=uiLabelMap.CommonEnviar /></#assign>
		<form method="post" action="<@ofbizUrl>enviarSolicitudTransferencia</@ofbizUrl>" name="enviarSolicitudTransferencia" style="margin: 0;" id="enviarSolicitudTransferencia">
			<@inputHidden name="urlHost" value=urlHost/>		
			<@inputHidden name="solicitudTransferenciaId" value=solicitud.solicitudTransferenciaId/>						
		</form>
	</#if>	
</#if>

<#if solicitud.statusId == 'TRANSFERIDA_ST' >
	<#assign LinkActualizar>${LinkActualizar!}<@submitFormLink form="imprimirSolicitudTransferencia" text=uiLabelMap.CommonPrint /></#assign>
	<@form name="imprimirSolicitudTransferencia" method="get"
		url="imprimirSolicitudTransferencia" solicitudTransferenciaId=solicitud.solicitudTransferenciaId/>
</#if>

<@form name="actualizaSolicitudTransferencia" 
		url="actualizaSolicitudTransferencia" id="actualizaSolicitudTransferencia"
			solicitudTransferenciaId=solicitud.solicitudTransferenciaId /> 
<@frameSection title=uiLabelMap.AlmacenSolicitudTransferencia extra=LinkActualizar>
	<table width="100%">
		<#assign ObjetoEstatus = solicitud.getRelatedOne('Estatus')! />
		<#assign ObjetoAlmacenOrigen = solicitud.getRelatedOne('OrigenFacility')! />
		<#assign ObjetoAlmacenDestino = solicitud.getRelatedOne('DestinoFacility')! />
		<@inputHidden name="solicitudTransferenciaId" value=solicitud.solicitudTransferenciaId/>
        <@displayRow title=uiLabelMap.AlmacenSolicitudTransferenciaId text=solicitud.solicitudTransferenciaId/>
		<@displayRow title=uiLabelMap.CommonDescription text=solicitud.descripcion/>
		<@displayRow title=uiLabelMap.WarehouseJustification text=solicitud.justificacion/>
		<@displayRow title=uiLabelMap.WarehouseStatus text=ObjetoEstatus.descripcion! />
		<@displayRow title=uiLabelMap.AlmacenOrigen text=ObjetoAlmacenOrigen.facilityName! />
		<@displayRow title=uiLabelMap.AlmacenDestino text=ObjetoAlmacenDestino.facilityName! />
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
				<#assign ObjetoPerson = comentario.getRelatedOne('Person')! />
	    		<@displayRow title="${ObjetoPerson.firstName!} "+"${ObjetoPerson.lastName!}: " text=comentario.comentario />	    				    	
		    </#list>		    
	    </#if>
	    	    
	    <#if solicitud?has_content && solicitud.solicitudTransferenciaId?exists>
	    	<#if (solicitud.statusId == 'ENVIADA_ST' && rol == 'AUTORIZADOR') ||(solicitud.statusId == 'COMENTADA_ST' && rol == "SOLICITANTE")>
				<#if statusAutorizacion?has_content>
	    			<#list statusAutorizacion as autorizacion>
	    				<#if autorizacion.statusId != "AUTORIZADA">	    	   	    
	    					<@inputTextareaRow title=uiLabelMap.CommonComment name="comentario" default="" />
	    				</#if>
	    			</#list>
	    		</#if>
	    	</#if>		    	
	    </#if>			    		    	           
	
	<#if solicitud?has_content && solicitud.solicitudTransferenciaId?exists>

		<#if statusAutorizacion?has_content>
	    	<#list statusAutorizacion as autorizacion>
	    		<#if autorizacion.statusId != "AUTORIZADA">
				<td></td>
				<td>
					<table>
				    	<#if (solicitud.statusId == 'ENVIADA_ST' && rol == 'AUTORIZADOR')>
							<form method="post" action="<@ofbizUrl>autorizarSolicitudTransferencia</@ofbizUrl>" name="autorizarSolicitudTransferencia" id="autorizarSolicitudTransferencia">
								<@inputHidden name="urlHost" value=urlHost/>	
								<@inputHidden name="solicitudTransferenciaId" value=solicitud.solicitudTransferenciaId/>
								<@inputHidden name="comentarioAutorizado" value="" id="comentarioAutorizado"/>												
							</form>
							<form method="post" action="<@ofbizUrl>rechazarSolicitudTransferencia</@ofbizUrl>" name="rechazarSolicitudTransferencia" id="rechazarSolicitudTransferencia">
								<@inputHidden name="urlHost" value=urlHost/>	
								<@inputHidden name="solicitudTransferenciaId" value=solicitud.solicitudTransferenciaId/>
								<@inputHidden name="comentarioRechazo" value="" id="comentarioRechazo"/>						
							</form>
							<@displayLinkCell href="javascript:autorizar();" text="${uiLabelMap.Autorizar}" class="subMenuButton"/>
							<@displayLinkCell href="javascript:rechazar();" text="${uiLabelMap.Rechazar}" class="subMenuButtonDangerous"/>																											    									
						</#if>
						<#if (solicitud.statusId == 'ENVIADA_ST' && rol == 'AUTORIZADOR') || (solicitud.statusId == 'COMENTADA_ST' && rol == "SOLICITANTE")>
							<form method="post" action="<@ofbizUrl>comentarSolicitudTransferencia</@ofbizUrl>" name="comentarSolicitudTransferencia" id="comentarSolicitudTransferencia">
								<@inputHidden name="urlHost" value=urlHost/>	
								<@inputHidden name="solicitudTransferenciaId" value=solicitud.solicitudTransferenciaId/>
								<@inputHidden name="comentarioComentar" value="" id="comentarioComentar"/>
								<@inputHidden name="rol" value=rol id="rol"/>
							</form>					    														
							<@displayLinkCell href="javascript:comentar();" text="${uiLabelMap.Comentar}" class="subMenuButton"/>
						</#if>
					</table>	
				</td>													
				</#if>
			</#list>	
		</#if>
	</#if>	
	</table>
</@frameSection>

<!-- 	Agregar productos a partir de otros procesos	-->

<#if solicitud?has_content && solicitud.solicitudTransferenciaId?exists>
	<#if solicitud.statusId == 'CREADA_ST' || solicitud.statusId == 'COMENTADA_ST' && rol != 'AUTORIZADOR'>
		<@frameSection title=uiLabelMap.AlmacenTransAgregarProducto>
		<form method="post" action="<@ofbizUrl>agregaItemProductoSolicitud</@ofbizUrl>" name="agregaItemProductoSolicitud" style="margin: 0;">
			<table border="0"  width="100%">
				<@inputHidden name="solicitudTransferenciaId" value=solicitud.solicitudTransferenciaId/>
				<input type="hidden" name='organizationPartyId' value="${solicitud.organizationPartyId?if_exists}" />
				<@inputTextRow name="cantidad" title=uiLabelMap.CommonQuantity size=9 maxlength=9 />
				<@inputAutoCompleteProductRow name="productId" title=uiLabelMap.ProductProductId />
				<@inputSubmitRow title=uiLabelMap.CommonAdd />	    		
			</table>
		</form>
		</@frameSection>
	</#if>	
</#if>

<#if solicitud?has_content && solicitud.solicitudTransferenciaId?exists>
	<#if solicitud.statusId == 'CREADA_ST' || solicitud.statusId == 'COMENTADA_ST' && rol != 'AUTORIZADOR'>
		<@frameSection title=uiLabelMap.AlmacenTransAgregarProductoRequisicion>
		<form method="post" action="<@ofbizUrl>agregaRequisicionSolicitud</@ofbizUrl>" name="agregaRequisicionSolicitud" style="margin: 0;">
			<table border="0"  width="100%">
				<@inputHidden name="solicitudTransferenciaId" value=solicitud.solicitudTransferenciaId/>
				<tr> 
	      			<@displayTitleCell title=uiLabelMap.Requisicion />
	      			<@inputSelectCell key="requisicionId" list=listRequisiciones?default([]) name="requisicionId" displayField="descripcion"/>
	    		</tr>
				<@inputSubmitRow title=uiLabelMap.CommonAdd />	    		
			</table>
		</form>
		</@frameSection>
	</#if>	
</#if>

<#if solicitud?has_content && solicitud.solicitudTransferenciaId?exists>
	<#if solicitud.statusId == 'CREADA_ST' || solicitud.statusId == 'COMENTADA_ST' && rol != 'AUTORIZADOR'>
		<@frameSection title=uiLabelMap.AlmacenTransAgregarProductoPedido>
		<form method="post" action="<@ofbizUrl>agregaOrdenSolicitud</@ofbizUrl>" name="agregaOrdenSolicitudd" style="margin: 0;">
			<table border="0"  width="100%">
				<@inputHidden name="solicitudTransferenciaId" value=solicitud.solicitudTransferenciaId/>
				<tr> 
	      			<@displayTitleCell title=uiLabelMap.OrdenCompra />
	      			<@inputSelectCell key="orderId" list=listOrdenes?default([]) name="orderId" displayField="orderName"/>
	    		</tr>
				<@inputSubmitRow title=uiLabelMap.CommonAdd />	    		
			</table>
		</form>
		</@frameSection>
	</#if>	
</#if>

<#if solicitud?has_content && solicitud.solicitudTransferenciaId?exists>
	<#if solicitud.statusId == 'CREADA_ST' || solicitud.statusId == 'COMENTADA_ST' && rol != 'AUTORIZADOR'>
		<@frameSection title=uiLabelMap.AlmacenTransAgregarProductoPedidoInterno>
		<form method="post" action="<@ofbizUrl>agregaPedidoInterno</@ofbizUrl>" name="agregaPedidoInterno" style="margin: 0;">
			<table border="0"  width="100%">
				<@inputHidden name="solicitudTransferenciaId" value=solicitud.solicitudTransferenciaId/>
				<tr> 
	      			<@displayTitleCell title=uiLabelMap.AlmacenPedidoInterno />
	      			<@inputSelectCell key="pedidoInternoId" list=listPedidoInterno?default([]) name="pedidoInternoId" displayField="descripcion"/>
	    		</tr>
				<@inputSubmitRow title=uiLabelMap.CommonAdd />	    		
			</table>
		</form>
		</@frameSection>
	</#if>	
</#if>


<#assign esTransferencia = false />
<#if solicitud.statusId == 'ATENDIDA_T'>
	<#assign LinkTransferir><@submitFormLink form="transferirSolicitud" text=uiLabelMap.AlmacenTransferir /></#assign>
	<#assign esTransferencia = true />
</#if>

<@frameSection title=uiLabelMap.WarehousePedidoInternoItemsAdded extra=LinkTransferir! >
	<#if detalleSolicitud?has_content>
	<@form name="transferirSolicitud" url="transferirSolicitud" id="transferirSolicitud"> 
	<table border="0" width="100%">
				
	<thead>		
		<tr>
			<th>${uiLabelMap.AlmacenSolicitudTransferenciaId}</th>
			<th>${uiLabelMap.ProductProductId}</th>
			<th>${uiLabelMap.PurchasingProduct}</th>
			<th>${uiLabelMap.CommonQuantity}</th>
			<#if esTransferencia == true >
				<th>
					<@inputMultiSelectAll form="transferirSolicitud" />
				</th>
			</#if> 
			<th></th>
			<th></th>
			
		</tr>
	</thead>
	<tbody>
	<@inputHidden name="solicitudTransferenciaId" id="solicitudTransferenciaId" value=solicitud.solicitudTransferenciaId/>
		<#assign count = 1 />
		<#list detalleSolicitud as detalle>
		<#assign nombreProducto = detalle.getRelatedOne("Product")/>
				<@inputHidden name="detalleSolicitudTransferId${count}" id="detalleSolicitudTransferId${count}" value=detalle.detalleSolicitudTransferId/>
				<@inputHidden name="productId${count}" id="productId${count}" value=detalle.productId />
						<tr class="${tableRowClass(detalle_index)}">
							<@displayCell text=detalle.detalleSolicitudTransferId/>
							<@displayCell text=detalle.productId />
							<@displayCell text=nombreProducto.productName?if_exists?default("") />
							<#if solicitud.statusId == 'TRANSFERIDA_ST' >
								<@displayCell text=detalle.cantidadTransferida />
							<#else>
								<#if detalle.cantidad gt 0 > 
									<@inputTextCell name="cantidad${count}" id="cantidad${count}" size=9 maxlength=9 default=detalle.cantidad />
								<#else>
									<@displayCell text=detalle.cantidad />
								</#if>
							</#if>
							<#if solicitud?has_content && solicitud.solicitudTransferenciaId?exists>
								<#if solicitud.statusId == 'CREADA_ST' || solicitud.statusId == 'COMENTADA_ST' && rol != 'AUTORIZADOR'>	
									<td align="center" width="5%">
										<input name="submitButton${count}" id="actualiza${count}" type="button" class="subMenuButton" value="${uiLabelMap.CommonUpdate}" onClick="actualizaArticulo('${count}')"/>
									</td>
									<td align="center" width="7%">
										<input name="submitButton${count}" id="elimina${count}" type="button" class="subMenuButtonDangerous" value="${uiLabelMap.CommonDelete}" onClick="eliminaArticulo('${count}')"/>								
									</td>
								</#if>	
							</#if>	
							<#if esTransferencia == true >	
								<@inputMultiCheckCell index=count />
							</#if>
						</tr>			
			<#assign count=count + 1/>
		</#list>
	</tbody>
	</table>
	</@form>
	</#if>
</@frameSection>