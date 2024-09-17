<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<#assign accionesVerObra = "">

<#--<@form name="vistaObra" url="vistaObra" obraId=obraObject.obraId/>
<#assign accionesVerContratoObra>${accionesVerContratoObra}<@submitFormLink form="vistaObra" text=uiLabelMap.ObrasInformacionObra class="subMenuButton"/></#assign>-->

<@form name="datosContrato" url="crearContratoObra" obraId=obraObject.obraId/>
<#assign accionesVerObra>${accionesVerObra}<@submitFormLink form="datosContrato" text=uiLabelMap.ObrasInicioContrato class="subMenuButton"/></#assign>

<@form name="buscarContrato" url="buscarContrato" obraId=obraObject.obraId performFind="B"/>
<#assign accionesVerObra>${accionesVerObra}<@submitFormLink form="buscarContrato" text=uiLabelMap.ObrasConsultarContrato class="subMenuButton"/></#assign>

<@form name="seguimientoObra" url="seguimientoObra" obraId=obraObject.obraId/>
<#assign accionesVerObra>${accionesVerObra}<@submitFormLink form="seguimientoObra" text=uiLabelMap.ObrasSeguimiento class="subMenuButton"/></#assign>

<@form name="modificacionesObra" url="modificacionesObra" obraId=obraObject.obraId/>
<#assign accionesVerObra>${accionesVerObra}<@submitFormLink form="modificacionesObra" text=uiLabelMap.ObrasModificacionesObra class="subMenuButton"/></#assign>

<@form name="verSolicitud" url="verSolicitud" obraId=obraObject.obraId performFind="B"/>
<#assign accionesVerObra>${accionesVerObra}<@submitFormLink form="verSolicitud" text=uiLabelMap.ObrasInformacionSolicitudObra class="subMenuButton"/></#assign>

<#--<@form name="finiquitoObra" url="verFiniquitoObra" obraId=obraObject.obraId/>
<#assign accionesVerObra>${accionesVerObra}<@submitFormLink form="finiquitoObra" text=uiLabelMap.ObrasFiniquitoObra class="subMenuButton"/></#assign>-->
		
<@form name="finalizarObra" url="finalizarObra" obraId=obraObject.obraId/>
<#assign accionesVerObra>${accionesVerObra}<@submitFormLink form="finalizarObra" text=uiLabelMap.FinalizarObra class="subMenuButtonDangerous"/></#assign>

<#assign contactMechId = obraObject.ubicacionFisica! />
<@form name="verEditarDireccion" url="verEditarDireccion?obraId=${obraObject.obraId}&contactMechId=${contactMechId!}" obraId=obraObject.obraId />

<@frameSection title=uiLabelMap.InfoObra extra=accionesVerObra?if_exists>  			
	<table width="90%">	
		<@form name="actualizaObra" url="actualizaObra" obraId=obraObject.obraId >
		<@displayRow title=uiLabelMap.ObraObraId text=obraObject.obraId/>							
		<@displayRow title=uiLabelMap.CommonName text=obraObject.nombre/>
		<@displayRow title=uiLabelMap.CommonDescription text=obraObject.descripcion/>
		<@displayRow title=uiLabelMap.PurchJustification text=obraObject.justificacion/>
		<@displayRow title=uiLabelMap.ObrasCarteraInversion text=obraObject.numAutCarteraInversion/>
		<@displayCurrencyRow title=uiLabelMap.ObrasMontoAutorizado amount=obraObject.montoAutorizado currencyUomId=obraObject.moneda class="tabletext"/>
		<#assign fmt = Static["org.ofbiz.base.util.UtilDateTime"].getDateFormat(locale)/>
		<#assign fechaInicio = Static["org.ofbiz.base.util.UtilDateTime"].timeStampToString(obraObject.fechaInicio,fmt,timeZone,locale) />
		<#assign fechaFin = Static["org.ofbiz.base.util.UtilDateTime"].timeStampToString(obraObject.fechaFin,fmt,timeZone,locale) />
		<@inputDateRow name="fechaInicio" title=uiLabelMap.CommonStartDate default=fechaInicio />
		<@inputDateRow name="fechaFin" title=uiLabelMap.CommonEndDate default=fechaFin/>		
		<#-- <@inputSelectDireccionObraRow title=uiLabelMap.ObraDireccion obraId=obraObject.obraId!/> -->
		<tr>
			<@displayTitleCell title=uiLabelMap.ObraDireccion />
			<td>
			<@inputText name="direccion" readonly=true default="${(postalAddress.address1+' '+postalAddress.address2)?if_exists}" />
			<#if (postalAddress?if_exists)?has_content ><#assign etiquetaBoton = uiLabelMap.CommonEdit /><#else><#assign etiquetaBoton = uiLabelMap.CommonAdd /></#if>
			<@submitFormLink form="verEditarDireccion" text=etiquetaBoton class="subMenuButton"/>
			</td>
		</tr>
		<tr><td align="right"><@submitFormLink form="actualizaObra" text=uiLabelMap.CommonUpdate class="subMenuButton"/><td></tr>
		
		<tr class="${tableRowClass(0)}">
			<td colspan="8" valign="top">
				<@flexArea targetId="ClavePresupuestal${obraObject.obraId}" title=uiLabelMap.ClavePresupuestal controlStyle="font-weight: bold;" >
					<table border="0" cellpadding="0" cellspacing="0" width="100%">
					<tr>
						<#if tagTypes?has_content>
							<@clavesPresupDisplay tags=tagTypes entity=obraObject partyExternal=obraObject.acctgTagEnumIdAdmin colspan="4"/>
						</#if>
					</tr>
					</table>
				</@flexArea>
			</td>
		<tr>
		</@form>
	</table>
	</br>		
</@frameSection>