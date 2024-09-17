<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl" />
<script language="JavaScript" type="text/javascript">
		function autorizarSolicitudPagoAnti() 
		{	confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar)
			{	var comentario = "";
				if(document.getElementsByName('comentario')[0].value != "")
				{	comentario = document.getElementsByName('comentario')[0].value;					
		        }		        				
				var form = document.autorizarSolicitudPagoAnti;
				document.getElementById('comentarioAutorizado').value = comentario;
				form.submit();		        					
			}					
		}
		
		function rechazarSolicitudPagoAnti() 
		{	confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar)
			{	if(document.getElementsByName('comentario')[0].value != "")
				{	var comentario = document.getElementsByName('comentario')[0].value;
					if(comentario.trim() != "")
					{	var form = document.rechazarSolicitudPagoAnti;
						document.getElementById('comentarioRechazo').value = comentario;
						form.submit();
		        	}
		        }		        
				else
				{	alert("Es necesario escribir un comentario")
				} 				
			}					
		}
		
		function comentarSolicitudPagoAnti() 
		{	confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar)
			{	if(document.getElementsByName('comentario')[0].value != "")
				{	var comentario = document.getElementsByName('comentario')[0].value;
					if(comentario.trim() != "")
					{	var form = document.comentarSolicitudPagoAnti;
						document.getElementById('comentarioComentar').value = comentario;
						form.submit();
		        	}
				}
				else
				{	alert("Es necesario escribir un comentario")
				}				 				
			}						
		}
</script>

<#assign rol = "" />	
<#if rolesAutorizador?has_content>
	<#list rolesAutorizador as rolAutorizador>
		<#if PagoAnticipado?has_content && PagoAnticipado.orderId?exists>	    		
    		<#if rolAutorizador.roleTypeId?exists && rolAutorizador.roleTypeId == "AUTORIZADOR" && PagoAnticipado.statusId != 'CREADA' && PagoAnticipado.statusId != 'COMENTADA'>
    			<#assign rol = "AUTORIZADOR" />		    					    	
    		<#else>
    			<#assign rol = "SOLICITANTE" />
    		</#if>		    		
    	</#if>			    				    
    </#list>		    		   
</#if>

<#if !PagoAnticipado.statusId.equals("CANCELADA") && rolesSolicitante?has_content> 
	<#assign LinkActualizar>
		<input name="submitButton" id="cancelar" type="button" class="subMenuButtonDangerous" value="${uiLabelMap.PurchasingCancela}" onClick="contactTypeClickCancela('${PagoAnticipado.orderId}')"/>	
	</#assign>							
</#if>	
	    

<#assign urlHost = request.getServerName()+":"+request.getServerPort()/>

<@frameSection title=uiLabelMap.PurchRequisicion extra=LinkActualizar>
<form method="post" action="<@ofbizUrl>cargarPagina</@ofbizUrl>" name="cargarPagina" style="margin: 0;" id="cargarPagina">
	<table width="100%">		
		<@inputHidden name="orderId" value=PagoAnticipado.orderId />
		<@inputHidden name="descripcion" value=PagoAnticipado.orderName! />
		<@inputHidden name="tipoMoneda" value=PagoAnticipado.currencyUom />
		<@displayDateRow title=uiLabelMap.FinancialsAccountigDate format="DATE_ONLY" date=PagoAnticipado.orderDate highlightStyle=""/>
		<tr>
	      <@displayTitleCell title="Numero de pedido o contrato" />
	      <@displayLinkCell text=PagoAnticipado.orderId href="editarPagoAnticipado?orderId=${PagoAnticipado.orderId}"/>
	    </tr>
		<@displayRow title=uiLabelMap.PurchDescription text=PagoAnticipado.orderName/>
		<@displayRow title=uiLabelMap.PurchMoneda text=PagoAnticipado.currencyUom />
		<@displayRow title="Monto total" text=PagoAnticipado.grandTotal />
		<@displayRow title="Monto pago anticipado" text=PagoAnticipado.pagoAnticipado />
		<@displayRow title="Proveedor pago anticipado" text=PagoAnticipado.proveedorPagoAnticipado />

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
	    
	    
	    	    
	    <#if PagoAnticipado?has_content && PagoAnticipado.orderId?exists>
	    	<#if PagoAnticipado.statusId == 'ENVIADO_PA' && rol == "AUTORIZADOR">
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

<#if PagoAnticipado?has_content && PagoAnticipado.orderId?exists>
	    	<#if PagoAnticipado.statusId == 'ENVIADO_PA' && rol == "AUTORIZADOR">
	    		<#if statusAutorizacion?has_content>
	    			<#list statusAutorizacion as autorizacion>
	    				<#if autorizacion.statusId != "AUTORIZADA">
							<td><td>			
								<form method="post" action="<@ofbizUrl>autorizarSolicitudPagoAnti</@ofbizUrl>" name="autorizarSolicitudPagoAnti">
									<@inputHidden name="urlHost" value=urlHost/>	
									<@inputHidden name="orderId" value=PagoAnticipado.orderId/>
									<@inputHidden name="comentarioAutorizado" value="" id="comentarioAutorizado"/>												
								</form>
								<form method="post" action="<@ofbizUrl>rechazarSolicitudPagoAnti</@ofbizUrl>" name="rechazarSolicitudPagoAnti">
									<@inputHidden name="urlHost" value=urlHost/>	
									<@inputHidden name="orderId" value=PagoAnticipado.orderId/>
									<@inputHidden name="comentarioRechazo" value="" id="comentarioRechazo"/>						
									
								</form>																											    									
								<form method="post" action="<@ofbizUrl>comentarSolicitudPagoAnti</@ofbizUrl>" name="comentarSolicitudPagoAnti">
									<@inputHidden name="urlHost" value=urlHost/>	
									<@inputHidden name="orderId" value=PagoAnticipado.orderId/>
									<@inputHidden name="comentarioComentar" value="" id="comentarioComentar"/>
									
								</form>					    														
								<@displayLink href="javascript:autorizarSolicitudPagoAnti();" text="${uiLabelMap.Autorizar}" class="subMenuButton"/>
								<@displayLink href="javascript:rechazarSolicitudPagoAnti();" text="${uiLabelMap.Rechazar}" class="subMenuButtonDangerous"/>
								<@displayLink href="javascript:comentarSolicitudPagoAnti();" text="${uiLabelMap.Comentar}" class="subMenuButton"/>													
							</td></td>
						</#if>
					</#list>	
				</#if>		
			</#if>		    	
	    </#if>	
	</table>
</@frameSection>