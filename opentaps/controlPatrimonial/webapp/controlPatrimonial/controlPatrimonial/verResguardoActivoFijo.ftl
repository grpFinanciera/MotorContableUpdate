

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<script language="JavaScript" type="text/javascript">
<!-- //
	   
  	   //Funcion para actualizar los campos del item de una solicitud
		function ClicActualiza(contador) 
		{	var fixedAssetId = document.getElementById('fixedAssetId').value;
			var partyId = document.getElementById('partyId'+contador).value;
			var fechaHasta = document.getElementById('fechaHasta'+contador).value;
		  	var fechaAsignacion = document.getElementById('fechaAsignacion'+contador).value;
		  	var statusId = document.getElementById('statusId'+contador).value;
		  	var comentario = document.getElementById('comentario'+contador).value;
		  	var comentarioOrig = document.getElementById('comentarioOrig'+contador).value;		  			  					
			
			if (fixedAssetId != "" && partyId != "") 
		    { 	var requestData = {'fixedAssetId' : fixedAssetId, 'partyId' : partyId, 'fechaHasta' : fechaHasta, 'fechaAsignacion' : fechaAsignacion, 
		    						'statusId' : statusId, 'comentario' : comentario, 'comentarioOrig' : comentarioOrig};		    		    								    		    			    
		    	opentaps.sendRequest('actualizaAsignacionActivo', requestData, function(data) {responseActualiza(data)});
		    }
			
		}

		//Functions load the response from the server
		function responseActualiza(data) 
		{	for (var key in data) 
			{	if(data[key]=="ERROR")
				{	alert("Ocurri\u00f3 un error al actualizar los campos");
				}							
				else
				{	var partyId = document.getElementById('cargarPaginaActivo').submit();
				}	    		
			}		
		}
		
		
		////Funcion para eliminar el registro de un item de una solicitud
		function ClicElimina(contador) 
		{	confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar) 
			{	var fixedAssetId = document.getElementById('fixedAssetId').value;
				var partyId = document.getElementById('partyId'+contador).value;		  				
		  		if (fixedAssetId != "" && partyId != "") 
		    	{ 	var requestData = {'fixedAssetId' : fixedAssetId, 'partyId' : partyId};		    			    
		    		opentaps.sendRequest('eliminaAsignacionActivo', requestData, function(data) {responseElimina(data)});
		    	}				
			}													
		}

		function responseElimina(data) 
		{	for (var key in data) 
			{	if(data[key]=="ERROR")
				{	alert("Ocurri\u00f3 un error al eliminar el elemento");
				}
				else
				{	alert("El elemento ha sido eliminado");
					var partyId = document.getElementById('cargarPaginaActivo').submit();
					
					
				}	    		
			}		
		}												
		  
// -->
</script>

<form method="post" action="<@ofbizUrl>cargarPaginaActivo</@ofbizUrl>" name="cargarPaginaActivo" style="margin: 0;" id="cargarPaginaActivo">
	<@inputHidden name="fixedAssetId" id="fixedAssetId" value="${fixedAssetId?if_exists}" />
</form>


<form name="crearAsignacionActivoFijo" method="post" action="<@ofbizUrl>crearAsignacionActivoFijo</@ofbizUrl>">	
<@frameSection title=uiLabelMap.ControlVerConsultaActivoFijo > 		
		<table class="fourColumnForm">		  		  
		  <@displayRow title=uiLabelMap.ControlFixedAssetId text="${fixedAssetId?if_exists}"/>
		  <@inputHidden name="fixedAssetId" id="fixedAssetId" value="${fixedAssetId?if_exists}" />
		  <@inputAutoCompletePartyRow title=uiLabelMap.ControlResguardanteNombre name="partyId" id="partyId" size="20" titleClass="requiredField"/>
		  <tr>
		    <@displayTitleCell title=uiLabelMap.FechaInicioResguardo titleClass="requiredField"/>
		    <@inputDateTimeCell name="fechaDesde" />
		  </tr>
		  <tr>
		    <@displayTitleCell title=uiLabelMap.FechaFinResguardo />
		    <@inputDateTimeCell name="fechaHasta"  />
		  </tr>
		  <tr>
		    <@displayTitleCell title=uiLabelMap.FechaAsignacion />
		    <@inputDateTimeCell name="fechaAsignacion"  />
		  </tr>
		  <@inputSelectRow title=uiLabelMap.Estado required=false list=listStatusItem displayField="description" name="statusId" titleClass="requiredField"/>
		  <@inputTextRow name="comentario" title=uiLabelMap.Comentario size=40 />
		  		  		  		
		  <@inputSubmitRow title=uiLabelMap.CommonCreate />		  		  	  		
		</table>		
</@frameSection>
</form>






<@frameSection title=uiLabelMap.ControlHistorialConsultaActivoFijo > 		
	<#if listPartyFixed?has_content>	
	<table border="0" width="100%">		
		<thead>		
			<tr>
				<th>${uiLabelMap.ControlPersonaResguardanteId}</th>
				<th>${uiLabelMap.ControlResguardanteNombre}</th>				
				<th>${uiLabelMap.FechaInicioResguardo}</th>
				<th>${uiLabelMap.FechaFinResguardo}</th>
				<th>${uiLabelMap.FechaAsignacion}</th>
				<th>${uiLabelMap.Estado}</th>
				<th>${uiLabelMap.Comentario}</th>
				<th>${uiLabelMap.Actualizar}</th>
				<th>${uiLabelMap.Eliminar}</th>			
				<th></th>
				<th></th>				
			</tr>
		</thead>
		<tbody>
			<#assign count = 1 />			
			<#list listPartyFixed as detalle>				
				<tr class="${tableRowClass(detalle_index)}">
				 	<@inputHidden name="partyId${count}" id="partyId${count}"value=detalle.partyId?if_exists />
				 	<@inputHidden name="comentarioOrig${count}" id="comentarioOrig${count}"value=detalle.comments?if_exists />
					<@displayCell text=detalle.partyId/>
					<@displayCell text=detalle.firstName?if_exists?default("")+" "+detalle.lastName?if_exists?default("") />
					<@displayDateCell date=detalle.fromDate format="DATE_ONLY" />					
					<@inputDateCell name="fechaHasta${count}" id="fechaHasta${count}" default=detalle.thruDate?if_exists />
					<@inputDateCell name="fechaAsignacion" id="fechaAsignacion${count}" default=detalle.allocatedDate?if_exists />
					<@inputSelectCell name="statusId" id="statusId${count}" list=listStatusItem key="statusId" required=true default=detalle.statusId?if_exists displayField="description"/>
					<@inputTextCell name="comentario" id="comentario${count}" default=detalle.comments?if_exists />
														
					<td align="center" width="5%">
						<input name="submitButton" id="actualiza${count}"" type="submit" class="subMenuButton" value="${uiLabelMap.CommonUpdate}" onClick="ClicActualiza('${count}')"/>
					</td>
					<td align="center" width="7%">
						<input name="submitButton" id="elimina${count}"" type="submit" class="subMenuButtonDangerous" value="${uiLabelMap.PurchRequisicionElimiarItem}" onClick="ClicElimina('${count}')"/>								
					</td>											
				</tr>																					          			          				
				<#assign count=count + 1/>
			</#list>
		</tbody>
	</table>	
	</#if>
</@frameSection>	
		



