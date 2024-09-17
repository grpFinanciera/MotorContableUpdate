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

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl" />

<script language="JavaScript" type="text/javascript">
<!-- //
	   	////Funcion para eliminar el registro de un item de una solicitud
		function contactTypeClickEliminar(contador) 
		{	confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar) 
			{	var proyectoId = document.getElementById('proyectoId').value;
				var detalleDocumentoId = document.getElementById('detalleDocumentoId'+contador).value;
		  		if (proyectoId != "" && detalleDocumentoId != "") 
		    	{ 	var requestData = {'proyectoId' : proyectoId, 'detalleDocumentoId' : detalleDocumentoId};		    			    
		    		opentaps.sendRequest('eliminaDocumento', requestData, function(data) {responseEliminar(data)});
		    	}				
			}													
		}

		//Functions load the response from the server
		function responseEliminar(data) 
		{	for (var key in data) 
			{	if(data[key]=="ERROR")
				{	alert("Ocurri\u00f3 un error al eliminar el elemento");
				}
				else
				{	alert("El elemento se ha eliminado con éxito");
					var proyectoId = document.getElementById('cargarPaginaProyectoDocumentos').submit();
					
					
				}	    		
			}		
		}
		
// -->
</script>

<#assign urlHost = request.getServerName()+":"+request.getServerPort()/>
<@frameSection title="Ver proyecto" extra=LinkActualizar>
<form method="post" action="<@ofbizUrl>cargarPaginaProyectoDocumentos</@ofbizUrl>" name="cargarPaginaProyectoDocumentos" style="margin: 0;" id="cargarPaginaProyectoDocumentos">
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
		<@displayRow title=uiLabelMap.PurchDescription text=Proyecto.nombreProyecto/>
		<#--<@displayRow title=uiLabelMap.PurchMoneda text=Proyecto.tipoMoneda />-->
		<@displayRow title="Responsable" text=Proyecto.responsable />
		<@displayRow title=uiLabelMap.PurchArea text=Proyecto.areaId />	
	</table>
</form>
</@frameSection>

<@frameSection title="Documentos añadidos" extra=extraOptions >
	<#if DocumentosProyecto?has_content>
	
	<table border="0" width="100%">
				
	<thead>		
		<tr>
			<th>Id Documento</th>
			<th>Nombre archivo</th>
			<th>Tipo documento</th>
			<th></th>
			<th></th>
			
		</tr>
	</thead>
	<tbody>
		<#assign count = 1 />
		<#list DocumentosProyecto as documento>
				<@inputHidden name="proyectoId" id="proyectoId" value=Proyecto.proyectoId/>
				<@inputHidden name="detalleDocumentoId${count}" id="detalleDocumentoId${count}" value=documento.detalleDocumentoId/>
			<tr class="${tableRowClass(detalle_index)}">
				<@displayCell text=documento.detalleDocumentoId/>
				<@displayCell text=documento.nombre/>
				<@displayCell text=documento.tipoDocumento/>
			<td align="center" width="7%">
				<#--<input name="submitButton" id="descarga${count}"" type="submit" class="subMenuButton" value="Descargar" onClick="contactTypeClickDescarga('${count}')"/>-->
				<a class="subMenuButton" href="descargaDocumento?proyectoId=${Proyecto.proyectoId}&amp;detalleDocumentoId=${documento.detalleDocumentoId}" title="Descargar"><img src="/opentaps_images/buttons/arrow-down-green_benji_p_01.png" alt="Descargar" />&#160;Descargar</a>
												
			</td>
			<td align="center" width="7%">
			<input name="submitButton" id="elimina${count}"" type="submit" class="subMenuButton" value="Eliminar" onClick="contactTypeClickEliminar('${count}')"/>
			<#--<a class="subMenuButton" href="eliminaDocumento?proyectoId=${Proyecto.proyectoId}&amp;detalleDocumentoId=${documento.detalleDocumentoId}" title="Eliminar"><img src="/opentaps_images/buttons/tasto_8_architetto_franc_01.png" alt="Eliminar" />&#160;Eliminar</a>-->
			</td>
				</tr>	
		<#assign count=count + 1/>
		</#list>
	</tbody>
	</table>
	</#if>
</@frameSection>