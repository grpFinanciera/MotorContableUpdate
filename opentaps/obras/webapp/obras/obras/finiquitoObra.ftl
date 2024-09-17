
<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<#assign accionesFiniquitoObra = "">

<@form name="vistaObra" url="vistaObra" obraId=obraId/>
<#assign accionesFiniquitoObra>${accionesFiniquitoObra}<@submitFormLink form="vistaObra" text=uiLabelMap.ObrasInformacionObra class="subMenuButton"/></#assign>

<@form name="datosContrato" url="crearContratoObra" obraId=obraId/>
<#assign accionesFiniquitoObra>${accionesFiniquitoObra}<@submitFormLink form="datosContrato" text=uiLabelMap.ObrasInicioContrato class="subMenuButton"/></#assign>

<@form name="buscarContrato" url="buscarContrato" obraId=obraId performFind="B"/>
<#assign accionesFiniquitoObra>${accionesFiniquitoObra}<@submitFormLink form="buscarContrato" text=uiLabelMap.ObrasConsultarContrato class="subMenuButton"/></#assign>

<@form name="seguimientoObra" url="seguimientoObra" obraId=obraId/>
<#assign accionesFiniquitoObra>${accionesFiniquitoObra}<@submitFormLink form="seguimientoObra" text=uiLabelMap.ObrasSeguimiento class="subMenuButton"/></#assign>

<@form name="modificacionesObra" url="modificacionesObra" obraId=obraId/>
<#assign accionesFiniquitoObra>${accionesFiniquitoObra}<@submitFormLink form="modificacionesObra" text=uiLabelMap.ObrasModificacionesObra class="subMenuButton"/></#assign>

<@form name="verSolicitud" url="verSolicitud" obraId=obraId performFind="B"/>
<#assign accionesFiniquitoObra>${accionesFiniquitoObra}<@submitFormLink form="verSolicitud" text=uiLabelMap.ObrasInformacionSolicitudObra class="subMenuButton"/></#assign>

<#--<@form name="finiquitoObra" url="verFiniquitoObra" obraId=obraId/>
<#assign accionesFiniquitoObra>${accionesFiniquitoObra}<@submitFormLink form="finiquitoObra" text=uiLabelMap.ObrasFiniquitoObra class="subMenuButton"/></#assign>-->
		
<@form name="finalizarObra" url="finalizarObra" obraId=obraId/>
<#assign accionesFiniquitoObra>${accionesFiniquitoObra}<@submitFormLink form="finalizarObra" text=uiLabelMap.FinalizarObra class="subMenuButtonDangerous"/></#assign>

<#if datosPoliza?has_content>
	<#list datosPoliza as poliza>
		<#assign acctgTransId = poliza.acctgTransId/>
		<#assign poliza = poliza.poliza/>
		<@form name="verPoliza${poliza}" url="viewAcctgTrans"  
			acctgTransId=acctgTransId />
		<#assign verPolizaAction>${verPolizaAction!}<@actionForm form="verPoliza${poliza}" text=acctgTransId /></#assign>
	</#list>
</#if>

<#assign accionesFiniquitoObra>${accionesFiniquitoObra}
			<#if datosPoliza?has_content>
				<div class="subMenuBar">
					<@selectActionForm name="shipmentActions" prompt=uiLabelMap.FinancialVerPoliza>
		   				${verPolizaAction?if_exists}
					</@selectActionForm>
				</div>
			</#if>
</#assign>	
  		  		
<@frameSection title=uiLabelMap.ObrasFiniquitoObra extra=accionesFiniquitoObra?if_exists>
	
<@form name="guardarFiniquito" url="guardarFiniquitoObra" >
	<#if datosFiniquito?has_content>	
		<#list datosFiniquito as finiquito>
		<table>		
			<@displayRow title=uiLabelMap.ObraObraId text=obraId/>
			<input type="hidden" name="obraId" value="${obraId}"/>
			<input type="hidden" name="contratoId" value="${contratoId}"/>
			<@displayRow title=uiLabelMap.ObraContratoId text=contratoId/>									
			<@inputTextRow name="numOficioEntregaFisica" title=uiLabelMap.ObraNumOficioNotificacionContratistaEntregaFisica default=finiquito.numOficioEntregaFisica?if_exists />
			<@inputTextRow name="numOficioEntregaRecepcion" title=uiLabelMap.ObraNumOficioActaEntregaRecepcion default=finiquito.numOficioEntregaRecepcion?if_exists />
			<@inputTextRow name="numOficioViciosOcultos" title=uiLabelMap.ObraNumOficioGarantiaViciosOcultos default=finiquito.numOficioViciosOcultos?if_exists />
			<@inputTextRow name="numOficioFiniquitoContrato" title=uiLabelMap.ObraNumOficioNotificacionContratanteFiniquitoContrato default=finiquito.numOficioFiniquitoContrato?if_exists />
			<@inputTextRow name="numOficioExtincionDerechos" title=uiLabelMap.ObraNumOficioActaAdministrativaExtincionDerechos default=finiquito.numOficioExtincionDerechos?if_exists />
			<@inputTextRow name="numOficioPlanosDefinitivos" title=uiLabelMap.ObraNumOficioPlanosDefinitivosActualizados default=finiquito.numOficioPlanosDefinitivos?if_exists />
			<@inputTextRow name="numOficioCatalogoConcepto" title=uiLabelMap.ObraNumOficioCatalogoConceptoDefinitivo default=finiquito.numOficioCatalogoConcepto?if_exists />		
			<@inputSubmitRow title=uiLabelMap.ObraFiniquitar />
		</table>
		</#list>
	<#else>	
		<table>		
			<@displayRow title=uiLabelMap.ObraObraId text=obraId/>
			<input type="hidden" name="obraId" value="${obraId}"/>
			<input type="hidden" name="contratoId" value="${contratoId}"/>
			<@displayRow title=uiLabelMap.ObraContratoId text=contratoId/>									
			<@inputTextRow name="numOficioEntregaFisica" title=uiLabelMap.ObraNumOficioNotificacionContratistaEntregaFisica/>
			<@inputTextRow name="numOficioEntregaRecepcion" title=uiLabelMap.ObraNumOficioActaEntregaRecepcion/>
			<@inputTextRow name="numOficioViciosOcultos" title=uiLabelMap.ObraNumOficioGarantiaViciosOcultos/>
			<@inputTextRow name="numOficioFiniquitoContrato" title=uiLabelMap.ObraNumOficioNotificacionContratanteFiniquitoContrato/>
			<@inputTextRow name="numOficioExtincionDerechos" title=uiLabelMap.ObraNumOficioActaAdministrativaExtincionDerechos/>
			<@inputTextRow name="numOficioPlanosDefinitivos" title=uiLabelMap.ObraNumOficioPlanosDefinitivosActualizados/>
			<@inputTextRow name="numOficioCatalogoConcepto" title=uiLabelMap.ObraNumOficioCatalogoConceptoDefinitivo/>		
			<@inputSubmitRow title=uiLabelMap.ObraFiniquitar />
		</table>
	</#if>	
		
</@form>
</@frameSection>