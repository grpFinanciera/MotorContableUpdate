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
 *@author     Jesus Ruiz
 *@since      1.0
-->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl" />

<script language="JavaScript" type="text/javascript">
<!-- //
	   
  	   	function autorizar() 
		{	confirmar=confirm("ÀEst\u00e1 seguro?"); 
			if (confirmar)
			{	var comentario = "";
				if(document.getElementsByName('comentario')[0].value != "")
				{	comentario = document.getElementsByName('comentario')[0].value;					
		        }		        				
				var form = document.autorizarSolicitudObra;
				document.getElementById('comentarioAutorizado').value = comentario;
				form.submit();		        					
			}					
		}
		
		function rechazar() 
		{	confirmar=confirm("ÀEst\u00e1 seguro?"); 
			if (confirmar)
			{	if(document.getElementsByName('comentario')[0].value != "")
				{	var comentario = document.getElementsByName('comentario')[0].value;
					if(comentario.trim() != "")
					{	var form = document.rechazarSolicitudObra;
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
		{	confirmar=confirm("ÀEst\u00e1 seguro?"); 
			if (confirmar)
			{	if(document.getElementsByName('comentario')[0].value != "")
				{	var comentario = document.getElementsByName('comentario')[0].value;
					if(comentario.trim() != "")
					{	var form = document.comentarSolicitudObra;
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
	    		<#if Obra?has_content && Obra.obraId?exists>	    		
		    		<#if rolAutorizador.roleTypeId?exists && rolAutorizador.roleTypeId == "AUTORIZADOR" && Obra.statusId != 'CREADA_O' && Obra.statusId != 'COMENTADA_O'>
		    			<#assign rol = "AUTORIZADOR" />		    		
		    		<#else>
		    			<#assign rol = "SOLICITANTE" />
		    		</#if>		    		
		    	</#if>			    				    
		    </#list>		    		   
	    </#if>

<#assign accionesSolicitud = "">
<#assign urlHost = request.getServerName()+":"+request.getServerPort()/>
<#assign areaPartyId = Static["com.absoluciones.obras.ConsultarSolicitud"].obtenerArea(delegator,userLogin.userLoginId)! />

<#if Obra.statusId == "CREADA_O" || Obra.statusId == "COMENTADA_O" && rol != "AUTORIZADOR">
<@form name="enviarSolicitudObra" url="enviarSolicitudObra" obraId=Obra.obraId urlHost=urlHost areaId=areaPartyId!/>
<#assign accionesSolicitud>${accionesSolicitud}<@submitFormLink form="enviarSolicitudObra" text=uiLabelMap.CommonEnviar class="subMenuButtonDangerous"/></#assign>			  		
</#if>
			
<@form name="vistaObra" url="vistaObra" obraId=obraId/>
<#assign accionesSolicitud>${accionesSolicitud}<@submitFormLink form="vistaObra" text=uiLabelMap.ObrasInformacionObra class="subMenuButton"/></#assign>

<@form name="datosContrato" url="crearContratoObra" obraId=obraId/>
<#assign accionesSolicitud>${accionesSolicitud}<@submitFormLink form="datosContrato" text=uiLabelMap.ObrasInicioContrato class="subMenuButton"/></#assign>

<@form name="buscarContrato" url="buscarContrato" obraId=obraId performFind="B"/>
<#assign accionesSolicitud>${accionesSolicitud}<@submitFormLink form="buscarContrato" text=uiLabelMap.ObrasConsultarContrato class="subMenuButton"/></#assign>

<@form name="seguimientoObra" url="seguimientoObra" obraId=obraId/>
<#assign accionesSolicitud>${accionesSolicitud}<@submitFormLink form="seguimientoObra" text=uiLabelMap.ObrasSeguimiento class="subMenuButton"/></#assign>

<@form name="modificacionesObra" url="modificacionesObra" obraId=obraId/>
<#assign accionesSolicitud>${accionesSolicitud}<@submitFormLink form="modificacionesObra" text=uiLabelMap.ObrasModificacionesObra class="subMenuButton"/></#assign>

<#--<@form name="verSolicitud" url="verSolicitud" obraId=obraId performFind="B"/>
<#assign accionesSolicitud>${accionesSolicitud}<@submitFormLink form="verSolicitud" text=uiLabelMap.ObrasInformacionSolicitudObra class="subMenuButton"/></#assign>-->

<#--<@form name="finiquitoObra" url="verFiniquitoObra" obraId=obraId/>
<#assign accionesSolicitud>${accionesSolicitud}<@submitFormLink form="finiquitoObra" text=uiLabelMap.ObrasFiniquitoObra class="subMenuButton"/></#assign>-->
		
<@form name="finalizarObra" url="finalizarObra" obraId=obraId/>
<#assign accionesSolicitud>${accionesSolicitud}<@submitFormLink form="finalizarObra" text=uiLabelMap.FinalizarObra class="subMenuButtonDangerous"/></#assign>	  				  		

<@frameSection title=uiLabelMap.PurchSolicitud extra=LinkActualizar extra=accionesSolicitud?if_exists>
<form method="post" action="<@ofbizUrl>actualizaSolicitud</@ofbizUrl>" name="actualizaSolicitud" style="margin: 0;" id="actualizaSolicitud">
	<table width="100%">
		<@inputHidden name="obraId" value=Obra.obraId/>
		<@displayRow title=uiLabelMap.ObraObraId text=Obra.obraId/>
		<@displayRow title=uiLabelMap.ObraUbicacionFisica text=Obra.ubicacionFisica/>
		<@displayRow title=uiLabelMap.ObraClavePresupuestal text=Obra.clavePresupuestal/>
		<@displayRow title=uiLabelMap.ObraNombre text=Obra.nombre/>
		<@displayRow title=uiLabelMap.ObraDescripcion text=Obra.descripcion/>
		<@displayRow title=uiLabelMap.ObraJustificacion text=Obra.justificacion/>
		<@displayRow title=uiLabelMap.ObrasCarteraInversion text=Obra.numAutCarteraInversion/>
		<@displayRow title=uiLabelMap.ObrasMontoAutorizado text=Obra.montoAutorizado/>
		<@displayRow title=uiLabelMap.ObraFechaInicio text=Obra.fechaInicio/>
		<@displayRow title=uiLabelMap.ObraFechaFin text=Obra.fechaFin/>
		<@displayRow title=uiLabelMap.ObraMoneda text=Obra.moneda/>
		<#if Obra.statusId == "CREADA" || Obra.statusId == "COMENTADA_O" && rol != "AUTORIZADOR">
				<@inputTextareaRow title=uiLabelMap.CommonComment name="comentario" default="" />			
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
	    
	    	    
	    <#if Obra.statusId == "ENVIADA_O" && rol == "AUTORIZADOR">
	    		<#if statusAutorizacion?has_content>
	    			<#list statusAutorizacion as autorizacion>
	    				<#if autorizacion.statusId != "AUTORIZADA">	    	   	    
	    					<@inputTextareaRow title=uiLabelMap.CommonComment name="comentario" default="" />
	    				</#if>
	    			</#list>
	    		</#if>			    						
		</#if>
</form>

	<#if Obra.statusId == "ENVIADA_O" && rol == "AUTORIZADOR">
		<#if statusAutorizacion?has_content>
	    	<#list statusAutorizacion as autorizacion>
	    		<#if autorizacion.statusId != "AUTORIZADA">
					<td><td>			
						<form method="post" action="<@ofbizUrl>autorizarSolicitudObra</@ofbizUrl>" name="autorizarSolicitudObra">
							<@inputHidden name="urlHost" value=urlHost/>	
							<@inputHidden name="obraId" value=Obra.obraId/>
							<@inputHidden name="comentarioAutorizado" value="" id="comentarioAutorizado"/>												
						</form>
						<form method="post" action="<@ofbizUrl>rechazarSolicitudObra</@ofbizUrl>" name="rechazarSolicitudObra">
							<@inputHidden name="urlHost" value=urlHost/>	
							<@inputHidden name="obraId" value=Obra.obraId/>
							<@inputHidden name="comentarioRechazo" value="" id="comentarioRechazo"/>						
						</form>																											    									
						<form method="post" action="<@ofbizUrl>comentarSolicitudObra</@ofbizUrl>" name="comentarSolicitudObra">
							<@inputHidden name="urlHost" value=urlHost/>	
							<@inputHidden name="obraId" value=Obra.obraId/>
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
	</table>
</@frameSection>