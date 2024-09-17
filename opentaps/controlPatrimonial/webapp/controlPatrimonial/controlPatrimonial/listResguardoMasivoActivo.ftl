<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#assign LinkActualizar><@submitFormLink form="actualizarResguardos" text=uiLabelMap.CommonEnviar /></#assign>


<#if listResguardosActivo?has_content>
<@frameSection title=uiLabelMap.ControlActualizarResguardos extra=LinkActualizar>
<form name="actualizarResguardos" method="post" action="<@ofbizUrl>actualizarResguardos</@ofbizUrl>">
  <table class="twoColumnForm">
    <tbody>
      <tr>
      	<@displayCell text=uiLabelMap.ControlId class="tableheadtext"/>
        <@displayCell text=uiLabelMap.ControlNombre class="tableheadtext"/>
        <@displayCell text=uiLabelMap.ControlFechaAdquisicion class="tableheadtext"/>
        <@displayCell text=uiLabelMap.ControlCostoCompra class="tableheadtext"/>
        <@displayCell text=uiLabelMap.ControlResguardanteNombre class="tableheadtext"/>
        <@displayCell text=uiLabelMap.ControlAreaNombre class="tableheadtext"/>
		<@displayCell text=uiLabelMap.ControlIdAnterior class="tableheadtext"/>
		<@displayCell text=uiLabelMap.ControlUbicacionFisica class="tableheadtext"/>
        <@displayCell text=uiLabelMap.ControlCambiarResguardante class="tableheadtext"/>        
      </tr>
      <#assign count = 1 />
      <input type="hidden" name="nuevoResguardante" id="nuevoResguardante" value="${nuevoResguardante?if_exists}"/>
      <input type="hidden" name="resguardante" id="resguardante" value="${resguardante?if_exists}"/>      
      <input type="hidden" name="fechaInicioResguardo" id="fechaInicioResguardo" value="${fechaInicioResguardo?if_exists}"/>
      <input type="hidden" name="fechaFinResguardo" id="fechaFinResguardo" value="${fechaFinResguardo?if_exists}"/>
      <input type="hidden" name="fechaAsignacion" id="fechaAsignacion" value="${fechaAsignacion?if_exists}"/>
      <input type="hidden" name="comentariosAsignacion" id="comentariosAsignacion" value="${comentariosAsignacion?if_exists}"/>
      <#list listResguardosActivo as row>
        <tr class="${tableRowClass(row_index)}">        	
        	<input type="hidden" name="fixedAssetId${count}" id="fixedAssetId${count}" value="${row.fixedAssetId?if_exists}"/>        	
        	<@displayCell text=row.fixedAssetId/>
            <@displayCell text=row.fixedAssetName/>
            <@displayDateCell date=row.dateAcquired format="DATE_ONLY" />
            <@displayCurrencyCell amount=row.purchaseCost currencyUomId=row.moneda class="textright"/>
            <@displayCell text=row.resguName/>
            <@displayCell text=row.areaName/>     
            <@displayCell text=row.idActivoAnterior?if_exists/>
            <@displayCell text=row.descripcion?if_exists/>      
            <@inputCheckboxCell name="excepcion${count}" id="excepcion${count}" value="Y" default="Y" />            
        </tr>
        <#assign count=count + 1/>
      </#list>   
    </tbody>
  </table>
</form>
</@frameSection>
</#if>



