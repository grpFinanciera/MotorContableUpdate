
<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<#assign accionesContratoObra = "">

<@form name="vistaObra" url="vistaObra" obraId=obraId/>
<#assign accionesContratoObra>${accionesContratoObra}<@submitFormLink form="vistaObra" text=uiLabelMap.ObrasInformacionObra class="subMenuButton"/></#assign>

<@form name="buscarContrato" url="buscarContrato" obraId=obraId performFind="B"/>
<#assign accionesContratoObra>${accionesContratoObra}<@submitFormLink form="buscarContrato" text=uiLabelMap.ObrasConsultarContrato class="subMenuButton"/></#assign>

<#--<@form name="finiquitoObra" url="verFiniquitoObra" obraId=obraId/>
<#assign accionesContratoObra>${accionesContratoObra}<@submitFormLink form="finiquitoObra" text=uiLabelMap.ObrasFiniquitoObra class="subMenuButton"/></#assign>-->

<@form name="modificacionesObra" url="modificacionesObra" obraId=obraId/>
<#assign accionesContratoObra>${accionesContratoObra}<@submitFormLink form="modificacionesObra" text=uiLabelMap.ObrasModificacionesObra class="subMenuButton"/></#assign>

<@form name="seguimientoObra" url="seguimientoObra" obraId=obraId performFind="B"/>
<#assign accionesContratoObra>${accionesContratoObra}<@submitFormLink form="seguimientoObra" text=uiLabelMap.ObrasSeguimiento class="subMenuButton"/></#assign>

<@form name="verSolicitud" url="verSolicitud" obraId=obraId performFind="B"/>
<#assign accionesContratoObra>${accionesContratoObra}<@submitFormLink form="verSolicitud" text=uiLabelMap.ObrasInformacionSolicitudObra class="subMenuButton"/></#assign>			
  		
<@frameSection title=uiLabelMap.ObrasDatosContrato extra=accionesContratoObra?if_exists>	
<@form name="guardarContratoObra" url="guardarContratoObra" >
	<#list datosContrato as contrato>	
		<table>		
			<@displayRow title=uiLabelMap.ObraObraId text=obraId/>
			<input type="hidden" name="obraId" value="${obraId}"/>									
			<@inputTextRow name="numContrato" title=uiLabelMap.ObraNumContrato default=contrato.numContrato?if_exists />
			<@inputTextRow name="numReciboAnticipo" title=uiLabelMap.ObraNumReciboAnticipo default=contrato.numReciboAnticipo?if_exists />
			<@inputTextRow name="numGarantiaAnticipo" title=uiLabelMap.ObraNumGarantiaAnticipo default=contrato.numGarantiaAnticipo?if_exists />
			<@inputTextRow name="numGarantiaCumplimiento" title=uiLabelMap.ObraNumGarantiaCumplimiento default=contrato.numGarantiaCumplimiento?if_exists />
			<@inputAutoCompletePartyRow title=uiLabelMap.ObraContratista name="proveedor" id="autoCompleteWhichPartyId" size="20" default=contrato.proveedor?if_exists/>				
			<@inputSubmitRow title=uiLabelMap.ObraActualizar />
		</table>
	</#list>	
</@form>
</@frameSection>