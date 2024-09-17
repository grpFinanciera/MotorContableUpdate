<#--
 *@author     Jesus Ruiz
 *@since      1.0
-->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl" />
<#assign hostname = request.getServerName() />
<script language="JavaScript" type="text/javascript">
<!-- //
	   
  	   	function enviar() 
		{	confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar)
			{	var form = document.enviarViatico;
				form.submit(); 				
			}					
		}
  	   	
  	   	function editar() 
		{	var form = document.editarViatico;
				form.submit();
		}
  	   	
  	   	function cancelar() 
		{	confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar)
			{	var form = document.cancelarViatico;
				form.submit(); 				
			}					
		}
  	   	
  	   	function autorizar() 
		{	confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar)
			{	var comentario = "";
				if(document.getElementsByName('comentario')[0].value != "")
				{	comentario = document.getElementsByName('comentario')[0].value;					
		        }		        				
				var form = document.autorizarViatico;
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
					{	var form = document.rechazarViatico;
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
					{	var form = document.comentarViatico;
						document.getElementById('comentarioComentar').value = comentario;
						form.submit();
		        	}
				}
				else
				{	alert("Es necesario escribir un comentario")
				}				 				
			}						
		}
		
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
				{	alert("Ocurri\u00f3 un error al actualizar los campos, puede que no cuente con Suficiencia Presupuestal");
				}
				else
				{	var detalleViaticoId = document.getElementById('verViatico').submit();
				}	    		
			}		
		}
		
		
		////Funcion para eliminar el registro de un item de una solicitud
		function contactTypeClickElimina(contador) 
		{confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
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
					var detalleViaticoId = document.getElementById('verViatico').submit();
				}	    		
			}		
		}
		
		////Funcion para eliminar el registro de un comprobante de una solicitud
		function contactTypeClickEliminaComprobante(contador) 
		{confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar) 
			{	var conceptoViaticoMontoId = document.getElementById('conceptoViaticoMontoId'+contador).value;
				if (conceptoViaticoMontoId != "") 
		    	{ 	var requestData = {'viaticoId' : viaticoId, 'conceptoViaticoMontoId' : conceptoViaticoMontoId};		    			    
		    		opentaps.sendRequest('eliminaComprobanteViatico', requestData, function(data) {responseEliminaItems(data)});
		    	}				
			}													
		}
					
		  
// -->
</script>
		<#assign rol = "" />	    
	    <#if rolesSolicitante?has_content && (Viatico.statusId == 'CREADA_V'||Viatico.statusId == 'PAGADA_V'||Viatico.statusId == 'COMENTADA_V')>
	    	<#list rolesSolicitante as rolSolicitante>
		    	<#if rolSolicitante.roleTypeId?exists && rolSolicitante.roleTypeId == "SOLICITANTE">
		    		<#assign rol = "SOLICITANTE" />
		    	</#if>			    
		    </#list>
		    	
	    <#elseif rolesAutorizador?has_content && (Viatico.statusId == 'ENVIADA_V'||Viatico.statusId == 'COMENTADA_V')>
			<#list rolesAutorizador as rolAutorizador>	    		
		    	<#if rolAutorizador.roleTypeId?exists && rolAutorizador.roleTypeId == "AUTORIZADOR">
		    		<#assign rol = "AUTORIZADOR" />
		    	</#if>
		    </#list>
		    
		<#elseif rolesPresupuesto?has_content && (Viatico.statusId == 'APROBADA_V'||Viatico.statusId == 'COMPROBADA_V')>
			<#list rolesPresupuesto as rolPresupuesto>	    		
		    	<#if rolPresupuesto.roleTypeId?exists && rolPresupuesto.roleTypeId == "PRESUPUESTO">
		    		<#assign rol = "PRESUPUESTO" />
		    	</#if>
		    </#list>    		    
	    </#if>

<#assign urlHost = request.getServerName()+":"+request.getServerPort()/>

<#if Viatico?has_content && Viatico.viaticoId?exists>
	<#if Viatico.statusId == 'APROBADA_V' && rol == "PRESUPUESTO">
		<#assign LinkActualizar><@submitFormLink form="comprometerViatico" text=uiLabelMap.CommonComprometer />
		<@submitFormLinkConfirm form="cancelarViaticoPresupuesto" text="Cancelar" /></#assign>
		
		<form method="post" action="<@ofbizUrl>comprometerViatico</@ofbizUrl>" name="comprometerViatico" style="margin: 0;" id="comprometerViatico">
			<@inputHidden name="urlHost" value=urlHost/>		
			<@inputHidden name="viaticoId" value=Viatico.viaticoId/>
			<table width="100%">
				<@inputSelectRow name="tipoDocumento" title=uiLabelMap.AccountingTipoDocumento
						list=tiposDocumentos! key="acctgTransTypeId" displayField="descripcion" defaultOptionText="" required=false titleClass="requiredField"/>
				<@inputDateRow name="fechaContable" title=uiLabelMap.CommonDate form="comprometerViatico" titleClass="requiredField"/>	
				<@inputCurrencySelectRow name="tipoMoneda" title=uiLabelMap.CommonCurrency defaultCurrencyUomId=Viatico.tipoMoneda titleClass="requiredField"/>					
			</table>			
		</form>
		<form method="post" action="<@ofbizUrl>cancelarViaticoPresupuesto</@ofbizUrl>" name="cancelarViaticoPresupuesto" style="margin: 0;" id="cancelarViaticoPresupuesto">
			<@inputHidden name="urlHost" value=urlHost/>		
			<@inputHidden name="viaticoId" value=Viatico.viaticoId/>			
		</form>
	</#if>
	<#if Viatico.statusId == 'PAGADA_V' && rol == "SOLICITANTE">
		<#assign LinkActualizar><@submitFormLink form="comprobarViaticoSolicitante" text=uiLabelMap.CommonEnviar /></#assign>
		<form method="post" action="<@ofbizUrl>comprobarViaticoSolicitante</@ofbizUrl>" name="comprobarViaticoSolicitante" style="margin: 0;" id="comprobarViaticoSolicitante">
			<@inputHidden name="urlHost" value=urlHost/>		
			<@inputHidden name="viaticoId" value=Viatico.viaticoId/>			
		</form>
	</#if>	
</#if>

<#if Viatico.statusId == 'CREADA_V' || Viatico.statusId == 'COMENTADA_V' && rol == "SOLICITANTE" && userLogin.partyId == Viatico.personaSolicitanteId>
	<form method="post" action="<@ofbizUrl>enviarViatico</@ofbizUrl>" name="enviarViatico">
		<@inputHidden name="urlHost" value=urlHost/>		
		<@inputHidden name="viaticoId" value=Viatico.viaticoId/>			
	</form>
	<form method="post" action="<@ofbizUrl>cancelarViatico</@ofbizUrl>" name="cancelarViatico">
		<@inputHidden name="viaticoId" value=Viatico.viaticoId/>												
	</form>
<@frameSection title=uiLabelMap.ViaticosHome >
<form id="creaViaticoForm" name="editarViatico" method="post" action="<@ofbizUrl>editarViatico</@ofbizUrl>">
	<@inputHidden name="viaticoId" value=Viatico.viaticoId/>
  <table class="twoColumnForm">
    <tbody>
     <@displayRow title=uiLabelMap.ViaticoId text=Viatico.viaticoId/>	
    <tr>  
      <@displayTitleCell title=uiLabelMap.PurchDestination />
      <@inputTextCell name="descripcion" maxlength="255" size=50 default=Viatico.descripcion/>
    </tr>
    <tr>	
    	<#assign userLogin = userLogin.userLoginId />
    	<#assign areaId = Static["com.absoluciones.viaticos.ConsultarViatico"].obtenerArea(delegator,userLogin)! />
    	<@displayTitleCell title=uiLabelMap.ViaticoArea />
    	<@inputHidden name="areaPartyId" value=areaId! />
    	<#assign areaNombreObj = Static["com.absoluciones.viaticos.ConsultarViatico"].obtenPartyGroup(delegator,areaId)! />
    	<@displayCell text=areaNombreObj.groupName! />    	
    </tr>
	<@inputTextareaRow title=uiLabelMap.ViaticoJustification name="justificacion" default=Viatico.justificacion />
	<@inputSelectRow name="programa2" title=uiLabelMap.ViaticoPrograma
		list=Programas2! key="programaId" displayField="nombrePrograma" default=Viatico.programa2/>
	<@inputCurrencySelectRow name="tipoMoneda" title=uiLabelMap.CommonCurrency defaultCurrencyUomId=Viatico.tipoMoneda/>
    <@inputSelectRow name="recurso" title=uiLabelMap.ViaticoRecurso list=recursos key="idRec" displayField="desc" default=Viatico.recurso />
	<@inputSelectRow name="fuenteFinanciamientoId" title=uiLabelMap.ViaticoFuente
		list=FuenteFinanciamiento! key="fuenteFinanciamientoId" displayField="descripcion" default=Viatico.fuenteFinanciamientoId />
    <@inputSelectRow name="tipoTransporte" title=uiLabelMap.PurchTransportType
		list=TiposTransportes! key="tipoTransporteId" displayField="descripcion" default=Viatico.tipoTransporteId/>
	<@inputCurrencyRow name="montoDiario" title=uiLabelMap.DailyAmount disableCurrencySelect=true default=Viatico.montoDiario/>
	<@inputCurrencyRow name="montoTrabCampo" title=uiLabelMap.FieldAmount disableCurrencySelect=true default=Viatico.montoTrabCampo/>
	<@inputCurrencyRow name="montoTransporte" title=uiLabelMap.PurchTransportAmount disableCurrencySelect=true default=Viatico.montoTransporte/>				
	<@inputSelectRow name="geoOrigen" title=uiLabelMap.PurchOrigin
		list=Geos! key="geoId" displayField="geoName" default=Viatico.geoOrigenId/>
	<@inputSelectRow name="geoDestino" title=uiLabelMap.PurchDestination
		list=Geos! key="geoId" displayField="geoName" default=Viatico.geoDestinoId/>								
	<@inputDateRow name="fechaInicio" title=uiLabelMap.CommonStartDate form="solicitudViatico" default=Viatico.fechaInicial/>
	<@inputDateRow name="fechaFin" title=uiLabelMap.CommonEndDate form="solicitudViatico" default=Viatico.fechaFinal/>
	<td><td>
	<@displayLink href="javascript:enviar();" text="${uiLabelMap.CommonSend}" class="subMenuButton"/>
	<@displayLink href="javascript:editar();" text="${uiLabelMap.CommonEdit}" class="subMenuButton"/>
	<@displayLink href="javascript:cancelar();" text="${uiLabelMap.CommonCancel}" class="subMenuButtonDangerous"/>
	</tbody>
  </table>
</form>
</@frameSection>
</#if>

<#if Viatico.statusId != 'CREADA_V'>
<@frameSection title=uiLabelMap.ViaticosHome extra=LinkActualizar>
<form method="post" action="<@ofbizUrl>verViatico</@ofbizUrl>" name="verViatico" style="margin: 0;" id="verViatico">
	<table width="100%">
		<@inputHidden name="viaticoId" value=Viatico.viaticoId/>
		<@inputHidden name="descripcion" value=Viatico.descripcion/>
		<@inputHidden name="justificacion" value=Viatico.justificacion/>		        
        <@inputHidden name="organizationPartyId" value=Viatico.organizationPartyId/>
        <@inputHidden name="areaPartyId" value=Viatico.areaPartyId/>
		<@displayRow title=uiLabelMap.ViaticoId text=Viatico.viaticoId/>
		<@displayRow title=uiLabelMap.ViaticoDescription text=Viatico.descripcion/>
		<@displayRow title=uiLabelMap.ViaticoJustification text=Viatico.justificacion/>
		<@displayRow title=uiLabelMap.CommonCurrency text=Viatico.tipoMoneda/>
		<@displayCurrencyRow title=uiLabelMap.DailyAmount currencyUomId=Viatico.tipoMoneda amount=Viatico.montoDiario class="tabletext"/>
		<@displayCurrencyRow title=uiLabelMap.FieldAmount currencyUomId=Viatico.tipoMoneda amount=Viatico.montoTrabCampo class="tabletext"/>
		<@displayCurrencyRow title=uiLabelMap.PurchTransportAmount currencyUomId=Viatico.tipoMoneda amount=Viatico.montoTransporte class="tabletext"/>
		<#assign geoO = Viatico.getRelatedOne("GeoOrigenGeo")/>
		<@displayRow title=uiLabelMap.PurchOrigin text=geoO.geoName/>
		<#assign geoD = Viatico.getRelatedOne("GeoDestinoGeo")/>
		<@displayRow title=uiLabelMap.PurchDestination text=geoD.geoName/>
		<#if Viatico.programa?exists>
			<#assign programa = Viatico.getRelatedOne("viaticoProgramaEnumeration")/>
			<@displayRow title=uiLabelMap.ViaticoPrograma text=programa.description />
		<#else>
			<#assign programa = Viatico.getRelatedOne("viaticoPrograma2ProgramaViatico")/>
			<@displayRow title=uiLabelMap.ViaticoPrograma text=programa.nombrePrograma />
		</#if>
		<@displayRow title=uiLabelMap.ViaticoRecurso text=Viatico.recurso/>
		
		<#if Viatico.fuenteFinanciamientoId?exists>
		<#assign fuenteFinanciamiento = Viatico.getRelatedOne("FuenteFinanciamiento")/>
    	<#assign descrip = fuenteFinanciamiento.descripcion/>
  		<#else>
    	<#assign descrip = ""/>
  		</#if>
		<@displayRow title=uiLabelMap.ViaticoFuente text=descrip />
		<@displayDateRow title=uiLabelMap.CommonStartDate date=Viatico.fechaInicial! format="DATE" />
		<@displayDateRow title=uiLabelMap.CommonEndDate date=Viatico.fechaFinal! format="DATE" />
									
					
		<#assign areaNombreObj = Static["com.absoluciones.viaticos.ConsultarViatico"].obtenPartyGroup(delegator,Viatico.areaPartyId)! />
		<@displayRow title=uiLabelMap.ViaticoArea text=areaNombreObj.groupName! />
		
		<#if listAutorizadores?has_content>
	<@displayRow title="Fujo de autorización"/>
	<#list listAutorizadores as autorizador>
		<@displayRow title="Autorizador "+ autorizador.secuencia text=autorizador.firstName + " " + autorizador.lastName />
	</#list>
</#if>
<#if listAutorizadoresStatus?has_content>
	<@displayRow title="Estatus autorizaciones"/>
	<#list listAutorizadoresStatus as autoriza>
		<@displayRow title="Autorizador" text=autoriza.firstName + " " + autoriza.lastName />
		<@displayRow title="Estatus" text=autoriza.statusId />
	</#list>
</#if>
		
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
	    
	    <#if viaticoPoliza?has_content && viaticoPoliza.polizaComprometida ?has_content>
		<@displayLinkCell text=uiLabelMap.ViaticoPolizaComprometida href="viewAcctgTrans?acctgTransId=${viaticoPoliza.acctTransPolizaComprometida}"/>
		</#if>
		<#if viaticoPoliza?has_content && viaticoPoliza.polizaComprobada ?has_content>
		<@displayLinkCell text=uiLabelMap.ViaticoPolizaComprobada href="viewAcctgTrans?acctgTransId=${viaticoPoliza.acctTransPolizaComprobada}"/>
		</#if>
		<#if viaticoPoliza?has_content && viaticoPoliza.polizaDevolucion ?has_content>
		<@displayLinkCell text=uiLabelMap.ViaticoPolizaDevolucion href="viewAcctgTrans?acctgTransId=${viaticoPoliza.acctTransPolizaDevolucion}"/>		
	    </#if>
	    <#if Viatico.statusId == 'COMPROBADA_V'||Viatico.statusId == 'VALIDADA_V'||Viatico.statusId == 'FINALIZADA_V'||Viatico.statusId == 'DEVOLUCION_V'>
	   
	    </#if>
	    	    
	    <#if Viatico?has_content && Viatico.viaticoId?exists>
	    	<#if Viatico.statusId == 'ENVIADA_V' && rol == "AUTORIZADOR">
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

	<#if Viatico?has_content && Viatico.viaticoId?exists>
	    	<#if Viatico.statusId == 'ENVIADA_V' && rol == "AUTORIZADOR">
	    		<#if statusAutorizacion?has_content>
	    			<#list statusAutorizacion as autorizacion>
	    				<#if autorizacion.statusId != "AUTORIZADA">
							<td><td>			
								<form method="post" action="<@ofbizUrl>autorizarViatico</@ofbizUrl>" name="autorizarViatico">
									<@inputHidden name="urlHost" value=urlHost/>	
									<@inputHidden name="viaticoId" value=Viatico.viaticoId/>
									<@inputHidden name="comentarioAutorizado" value="" id="comentarioAutorizado"/>												
								</form>
								<form method="post" action="<@ofbizUrl>rechazarViatico</@ofbizUrl>" name="rechazarViatico">
									<@inputHidden name="urlHost" value=urlHost/>	
									<@inputHidden name="viaticoId" value=Viatico.viaticoId/>
									<@inputHidden name="comentarioRechazo" value="" id="comentarioRechazo"/>						
									
								</form>																											    									
								<form method="post" action="<@ofbizUrl>comentarViatico</@ofbizUrl>" name="comentarViatico">
									<@inputHidden name="urlHost" value=urlHost/>	
									<@inputHidden name="viaticoId" value=Viatico.viaticoId/>
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
</#if>

<#if  Viatico.statusId == 'APROBADA_V' && rol == "PRESUPUESTO">
<@frameSection title=uiLabelMap.PurchViaticoItems>
	<form method="post" action="<@ofbizUrl>agregaItemViatico</@ofbizUrl>" name="agregaItemViatico" style="margin: 0;">
		<table border="0"  width="100%">
			<@inputHidden name="viaticoId" value=Viatico.viaticoId/>
			<@inputHidden name="organizationPartyId" value=Viatico.organizationPartyId/>
			<@inputCurrencyRow name="monto" currencyName="uomId" defaultCurrencyUomId=Viatico.tipoMoneda disableCurrencySelect=true title=uiLabelMap.PurchasingMontoEstimado titleClass="requiredField"/>
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
			<th>${uiLabelMap.CommonTotal}</th>			
			<th></th>
			<th></th>
			
		</tr>
	</thead>
	<tbody>
		<#assign count = 1 />
		<#list detalleViatico as detalle>		
				<@inputHidden name="viaticoId" id="viaticoId" value=Viatico.viaticoId/>
				<@inputHidden name="detalleViaticoId${count}" id="detalleViaticoId${count}" value=detalle.detalleViaticoId/>
						<tr class="${tableRowClass(detalle_index)}">
							<@displayCell text=detalle.detalleViaticoId/>
							<@inputCurrencyCell name="montoDetalle${count}" currencyName="uomId" defaultCurrencyUomId=Viatico.tipoMoneda disableCurrencySelect=true default=detalle.monto/>
							<td align="center" width="5%">
								<input name="submitButton" id="actualiza${count}"" type="submit" class="subMenuButton" value="${uiLabelMap.CommonUpdate}" onClick="contactTypeClickActualiza('${count}')"/>
							</td>
							<td align="center" width="7%">
								<input name="submitButton" id="elimina${count}"" type="submit" class="subMenuButtonDangerous" value="${uiLabelMap.ViaticoElimiarItem}" onClick="contactTypeClickElimina('${count}')"/>								
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
</#if>	

<#if  Viatico.statusId == 'PAGADA_V' && rol == "SOLICITANTE">
<@frameSection title=uiLabelMap.InformeActividades>
	<form method="post" action="<@ofbizUrl>enviaObservacion</@ofbizUrl>" name="enviaObservacion" style="margin: 0;">
		<table border="0"  width="100%">
				<@inputHidden name="viaticoId" value=Viatico.viaticoId/>
				<@inputTextareaRow title=uiLabelMap.InformeActividades name="observacionComprobante" default="" titleClass="requiredField"/>
				<@inputSubmitRow title="Guardar Informe" />	
		</table>
	</form>
</@frameSection>
<#--JRRM-->

<@frameSection title=uiLabelMap.ViaticoComprobantes>
	<form method="post" action="<@ofbizUrl>agregaComprobanteViatico</@ofbizUrl>" name="agregaComprobanteViatico" style="margin: 0;">
		<table border="0"  width="100%">
			<@inputHidden name="viaticoId" value=Viatico.viaticoId/>
			<@inputDateRow name="fecha" title=uiLabelMap.Date form="agregaComprobanteViatico" titleClass="requiredField" />
			<@inputSelectRow name="conceptoViatico" title=uiLabelMap.ConceptoViatico
				list=conceptosViaticos! key="conceptoViaticoId" displayField="descripcion" titleClass="requiredField" />
			<tr>
			<@displayTitleCell title=uiLabelMap.ViaticoReferenciaFactura titleClass="requiredField"/>
			<@inputTextCell name="referencia" maxlength="20" size=20/>
			</tr>
			<tr>
			<@displayTitleCell title=uiLabelMap.ViaticoDescripcion titleClass="requiredField"/>
			<@inputTextCell name="descripcion" maxlength="255" size=50/>
			</tr>
			<@inputCurrencyRow name="monto" title=uiLabelMap.ViaticoAmount disableCurrencySelect=true titleClass="requiredField"/>	
			<@inputSubmitRow title=uiLabelMap.CommonAdd />			
		</table>
	</form>
</@frameSection>
</#if>

<#if  (Viatico.statusId == 'PAGADA_V' && rol == "SOLICITANTE") || (Viatico.statusId == 'COMPROBADA_V' && rol == "PRESUPUESTO") >	
<@frameSection title=uiLabelMap.ViaticoConceptoAdded >
	<#if Comprobantes?has_content>
	
	<table border="0" width="100%">
				
	<thead>		
		<tr>
			<th>${uiLabelMap.PurchViaticoItemId}</th>
			<th>${uiLabelMap.Date}</th>
			<th>${uiLabelMap.ConceptoViatico}</th>
			<th>${uiLabelMap.ViaticoReferenciaFactura}</th>
			<th>${uiLabelMap.ViaticoDescripcion}</th>
			<th>${uiLabelMap.ViaticoAmount}</th>			
			<th></th>
			<th></th>			
		</tr>
	</thead>
	<tbody>
		<#assign count = 1 />
		<#assign montoTotal = 0 />
		<#list Comprobantes as comprobante>		
				<@inputHidden name="viaticoId" id="viaticoId" value=Viatico.viaticoId/>
				<@inputHidden name="conceptoViaticoMontoId${count}" id="conceptoViaticoMontoId${count}" value=comprobante.conceptoViaticoMontoId/>
						<tr class="${tableRowClass(comprobante_index)}">
							<@displayCell text=comprobante.conceptoViaticoMontoId/>
							<@displayDateCell date=comprobante.fecha format="DATE"/>
							<#assign concepto = comprobante.getRelatedOne("ConceptoViatico")/>
							<@displayCell text=concepto.descripcion/>
							<@displayCell text=comprobante.referencia/>
							<@displayCell text=comprobante.descripcion/>
							<@displayCurrencyCell currencyUomId=Viatico.tipoMoneda  amount=comprobante.monto/>
							<td align="center" width="7%">
								<input name="submitButton" id="elimina${count}"" type="submit" class="subMenuButtonDangerous" value="${uiLabelMap.ViaticoElimiarItem}" onClick="contactTypeClickEliminaComprobante('${count}')"/>								
							</td>							
						</tr>
			<#assign count=count + 1/>
			<#assign montoTotal=montoTotal + comprobante.monto/>	
		</#list>
		<tr>
		
			<td align="right" colspan="4">				
			<@displayTitleCell title="Total"/>
			<@displayCurrencyCell currencyUomId=Viatico.tipoMoneda  amount=montoTotal/>
			</td>
		</tr>
	</tbody>
	</table>
	</#if>
</@frameSection>
</#if>
<#--JRRM-->



<#if  Viatico.statusId == 'COMPROBADA_V' && rol == "PRESUPUESTO">
<#assign LinkComprobar><@submitFormLink form="comprobarViatico" text=uiLabelMap.CommonComprobar/></#assign>
<#assign LinkComprobar>${LinkComprobar}<@submitFormLinkConfirm form="comprobarViatico" text=uiLabelMap.DevolucionTotal devolucion="true"/></#assign>
<@frameSection title=uiLabelMap.PurchViaticoItemsComprobar extra = LinkComprobar>
<form method="post" action="<@ofbizUrl>comprobarViatico</@ofbizUrl>" name="comprobarViatico" style="margin: 0;" id="comprobarViatico" >
	
	<@inputHidden name="devolucion" value="false"/>
	<@inputHidden name="viaticoId" value=Viatico.viaticoId/>
	<#if Viatico.statusId == "COMPROBADA_V">
		<#assign evento = "PAG_VIATICO">
	</#if>
	<@inputSelectRow name="tipoDocumento" title=uiLabelMap.AccountingTipoDocumento
						list=tiposDocumentos! key="acctgTransTypeId" displayField="descripcion" default=evento?if_exists required=false titleClass="requiredField"/>
	<@inputDateRow name="fechaContable" title=uiLabelMap.CommonDate form="comprobarViatico" titleClass="requiredField"/>
	<#if detalleViatico?has_content>
		<table border="0" width="100%">
			<thead>		
				<tr>
					<th>${uiLabelMap.PurchViaticoItemId}</th>
					<th>${uiLabelMap.PurchClavePresupuestal}</th>
					<th>${uiLabelMap.CommonTotal}</th>
				</tr>
			</thead>
			<tbody>
				<#assign count = 1 />
				<#list detalleViatico as detalle>
					<@inputHidden name="detalleViaticoId${count}" id="detalleViaticoId${count}" value=detalle.detalleViaticoId/>
						<tr class="${tableRowClass(detalle_index)}">
							<@displayCell text=detalle.detalleViaticoId/>
							<@displayCell text=detalle.clavePresupuestal />
							<#assign bandera = Static["com.absoluciones.viaticos.ConsultarViatico"].obtenMontoComprobado(delegator,detalle)! />
							<#if bandera == 'D'>
								<@inputCurrencyCell name="montoDetalle${count}" currencyName="uomId" defaultCurrencyUomId=Viatico.tipoMoneda disableCurrencySelect=true default=Viatico.montoDiarioComprobado/>
							<#elseif bandera == 'T'>
								<@inputCurrencyCell name="montoDetalle${count}" currencyName="uomId" defaultCurrencyUomId=Viatico.tipoMoneda disableCurrencySelect=true default=Viatico.montoTransporteComprobado/>
							<#elseif bandera == 'C'>
								<@inputCurrencyCell name="montoDetalle${count}" currencyName="uomId" defaultCurrencyUomId=Viatico.tipoMoneda disableCurrencySelect=true default=Viatico.montoTrabCampoComprobado/>	
							<#else>
								<@inputCurrencyCell name="montoDetalle${count}" currencyName="uomId" defaultCurrencyUomId=Viatico.tipoMoneda disableCurrencySelect=true default=detalle.monto/>	
							</#if>														
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
</form>	
</@frameSection>
</#if>