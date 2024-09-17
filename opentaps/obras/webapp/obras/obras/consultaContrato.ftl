<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#assign accionesConsultaContratoObra = "">
<@form name="vistaObra" url="vistaObra" obraId=obraId/>
<#assign accionesConsultaContratoObra>${accionesConsultaContratoObra}<@submitFormLink form="vistaObra" text=uiLabelMap.ObrasInformacionObra class="subMenuButton"/></#assign>

<@form name="datosContrato" url="crearContratoObra" obraId=obraId/>
<#assign accionesConsultaContratoObra>${accionesConsultaContratoObra}<@submitFormLink form="datosContrato" text=uiLabelMap.ObrasInicioContrato class="subMenuButton"/></#assign>

<#--<@form name="buscarContrato" url="buscarContrato" obraId=obraId performFind="B"/>
<#assign accionesConsultaContratoObra>${accionesConsultaContratoObra}<@submitFormLink form="buscarContrato" text=uiLabelMap.ObrasConsultarContrato class="subMenuButton"/></#assign>-->

<@form name="seguimientoObra" url="seguimientoObra" obraId=obraId/>
<#assign accionesConsultaContratoObra>${accionesConsultaContratoObra}<@submitFormLink form="seguimientoObra" text=uiLabelMap.ObrasSeguimiento class="subMenuButton"/></#assign>

<@form name="modificacionesObra" url="modificacionesObra" obraId=obraId/>
<#assign accionesConsultaContratoObra>${accionesConsultaContratoObra}<@submitFormLink form="modificacionesObra" text=uiLabelMap.ObrasModificacionesObra class="subMenuButton"/></#assign>

<@form name="verSolicitud" url="verSolicitud" obraId=obraId performFind="B"/>
<#assign accionesConsultaContratoObra>${accionesConsultaContratoObra}<@submitFormLink form="verSolicitud" text=uiLabelMap.ObrasInformacionSolicitudObra class="subMenuButton"/></#assign>

<#--<@form name="finiquitoContrato" url="verFiniquitoObra" obraId=obraId/>
<#assign accionesConsultaContratoObra>${accionesConsultaContratoObra}<@submitFormLink form="finiquitoObra" text=uiLabelMap.ObrasFiniquitoObra class="subMenuButton"/></#assign>-->
		
<@form name="finalizarObra" url="finalizarObra" obraId=obraId/>
<#assign accionesConsultaContratoObra>${accionesConsultaContratoObra}<@submitFormLink form="finalizarObra" text=uiLabelMap.FinalizarObra class="subMenuButtonDangerous"/></#assign>	
  		
<@frameSection title=uiLabelMap.ObrasBuscaContrato extra=accionesConsultaContratoObra?if_exists>


<form name="busquedaContratoForm" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <@inputHidden name="obraId" value=obraId/>
  <table class="twoColumnForm">
    <tbody>
      <tr>
        <@displayTitleCell title=uiLabelMap.ObraContratoId/>
        <@inputTextCell name="contratoId" maxlength=60  />
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.ObraNumContrato/>
        <@inputTextCell name="numContrato" maxlength=60  />
      </tr>                            
       <@displayTitleCell title=uiLabelMap.ObraContratistaId />           
       <@inputAutoCompletePartyCell name="contratistaId" id="contratistaId" />              
       <tr>
        <@displayTitleCell title=uiLabelMap.ObraProductId />
        <@inputAutoCompleteProductCell form="busquedaContratoForm" name="productId" />
       </tr>
       <tr>
        <@displayTitleCell title=uiLabelMap.ObraDescripcion/>
        <@inputTextCell name="descripcion" maxlength=60  />
      </tr>
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
</@frameSection>
