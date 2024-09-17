<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#assign accionesModificacionObra = "">
<@form name="vistaObra" url="vistaObra" obraId=obraId/>
<#assign accionesModificacionObra>${accionesModificacionObra}<@submitFormLink form="vistaObra" text=uiLabelMap.ObrasInformacionObra class="subMenuButton"/></#assign>

<@form name="datosContrato" url="crearContratoObra" obraId=obraId/>
<#assign accionesModificacionObra>${accionesModificacionObra}<@submitFormLink form="datosContrato" text=uiLabelMap.ObrasInicioContrato class="subMenuButton"/></#assign>

<@form name="buscarContrato" url="buscarContrato" obraId=obraId performFind="B"/>
<#assign accionesModificacionObra>${accionesModificacionObra}<@submitFormLink form="buscarContrato" text=uiLabelMap.ObrasConsultarContrato class="subMenuButton"/></#assign>

<@form name="seguimientoObra" url="seguimientoObra" obraId=obraId/>
<#assign accionesModificacionObra>${accionesModificacionObra}<@submitFormLink form="seguimientoObra" text=uiLabelMap.ObrasSeguimiento class="subMenuButton"/></#assign>

<#--<@form name="modificacionesObra" url="modificacionesObra" obraId=obraId/>
<#assign accionesModificacionObra>${accionesModificacionObra}<@submitFormLink form="modificacionesObra" text=uiLabelMap.ObrasModificacionesObra class="subMenuButton"/></#assign>-->

<@form name="verSolicitud" url="verSolicitud" obraId=obraId performFind="B"/>
<#assign accionesModificacionObra>${accionesModificacionObra}<@submitFormLink form="verSolicitud" text=uiLabelMap.ObrasInformacionSolicitudObra class="subMenuButton"/></#assign>

<#--<@form name="finiquitoObra" url="verFiniquitoObra" obraId=obraId/>
<#assign accionesModificacionObra>${accionesModificacionObra}<@submitFormLink form="finiquitoObra" text=uiLabelMap.ObrasFiniquitoObra class="subMenuButton"/></#assign>-->
		
<@form name="finalizarObra" url="finalizarObra" obraId=obraId/>
<#assign accionesModificacionObra>${accionesModificacionObra}<@submitFormLink form="finalizarObra" text=uiLabelMap.FinalizarObra class="subMenuButtonDangerous"/></#assign>	
  				  		
<@frameSection title=uiLabelMap.ObrasModificacionPrecios extra=accionesModificacionObra?if_exists>
<@form method="post" name="modificacionesObra" url="actualizarModificacionesObrasPrecio" >
	<table>
		<input type="hidden" name="obraId" value="${obraId}"/>
		<@inputTextRow name="numOficioJustificacionTec" title=uiLabelMap.ObrasnumOficioSolicitudJustificacionTecnica default=obra.numOficioJustificacionTec! />
		<@inputTextRow name="numOficioAutorizacionSFP" title=uiLabelMap.ObrasnumOficioAutorizacionSFP default=obra.numOficioAutorizacionSFP! />
		<@inputTextRow name="numOficioAutorizaContratant" title=uiLabelMap.ObrasnumOficioAutorizacionContratante default=obra.numOficioAutorizaContratant! />
		<@inputTextRow name="numOficioTarjetas" title=uiLabelMap.ObrasnumOficioTarjetasDefinitivas default=obra.numOficioTarjetas! />
		<@inputTextRow name="numOficioConvenioModif" title=uiLabelMap.ObrasnumOficioConvenioModificatorio default=obra.numOficioConvenioModif! />
		<@inputTextRow name="numOficioPresupuesto" title=uiLabelMap.ObrasnumOficioPresupuestoDefinitivo default=obra.numOficioPresupuesto! />
		<@inputTextRow name="numOficioFianzaCumplimiento" title=uiLabelMap.ObrasnumOficioModificadoFianzaCumplimiento default=obra.numOficioFianzaCumplimiento! />
		<@inputTextRow name="numOficioFianzaAmpliacion" title=uiLabelMap.ObrasnumOficioFianzaAmpliacion default=obra.numOficioFianzaAmpliacion! />
		<@inputSubmitRow title=uiLabelMap.CommonUpdate />
	</table>
</@form>
</@frameSection>