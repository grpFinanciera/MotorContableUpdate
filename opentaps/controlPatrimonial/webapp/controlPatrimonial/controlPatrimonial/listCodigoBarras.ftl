<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#assign LinkImprimeCodigos><@submitFormLink form="imprimirPlanillaPdf" text=uiLabelMap.CommonEnviar /></#assign>

<#assign productIdList = "" />
<#if listActivoCodigoBarras?has_content>
	<#list listActivoCodigoBarras as row>        
		<#assign productIdList=productIdList+"${row.fixedAssetId?if_exists}"+"|"/>		
	</#list>
</#if>

<#--<form name="PlanillaCodigoBarras.pdf" method="post" action="<@ofbizUrl>/PlanillaCodigoBarras.pdf?productId=${productIdList}&amp;productName=${productNameList}&amp;dateAcquired=${dateAcquiredList}&amp;idTamano=${idTamano}&amp;posicionImpresion=${posicionImpresion}</@ofbizUrl>" target="_blank"">-->
<@form name="imprimirPlanillaPdf" target="_blank" method="post" url="planillaCodigosServlet" productIdList=productIdList reportType="application/pdf" idTamano="${idTamano?if_exists}" posicionImpresion="${posicionImpresion?if_exists}" organizationPartyId="${organizationPartyId?if_exists}"/>
<#--<form name="imprimirPlanillaPdf" method="post" action="<@ofbizUrl>/planillaCodigosServlet?productIdList=${productIdList}&amp;reportType=application/pdf&amp;idTamano=${idTamano?if_exists}&amp;posicionImpresion=${posicionImpresion?if_exists}&amp;organizationPartyId=${organizationPartyId?if_exists}</@ofbizUrl>" target="_blank"">-->
<@frameSection title=uiLabelMap.ControlImprimirCodigosBarras extra=LinkImprimeCodigos>
<#if listActivoCodigoBarras?has_content>

  <table class="twoColumnForm">
    <tbody>
      <tr>      	
        <@displayCell text=uiLabelMap.ControlId class="tableheadtext"/>
        <@displayCell text=uiLabelMap.ControlNombre class="tableheadtext"/>
        <@displayCell text=uiLabelMap.ControlFechaAdquisicion class="tableheadtext"/>                															   
        <@displayCell text=uiLabelMap.ControlCostoCompra class="tableheadtext"/>                
        <@displayCell text=uiLabelMap.ControlAreaNombre class="tableheadtext"/>
        <@displayCell text=uiLabelMap.ControlProveedorNombre class="tableheadtext"/>
        <@displayCell text=uiLabelMap.ControlNumeroFactura class="tableheadtext"/>
        <@displayCell text=uiLabelMap.ControlPlaca class="tableheadtext"/>
        <@displayCell text=uiLabelMap.ControlModelo class="tableheadtext"/>
        <@displayCell text=uiLabelMap.ControlMarca class="tableheadtext"/>
        <@displayCell text=uiLabelMap.ControlSerie class="tableheadtext"/>
		<@displayCell text=uiLabelMap.ControlIdAnterior class="tableheadtext"/>   
		<@displayCell text=uiLabelMap.ControlUbicacionFisica class="tableheadtext"/>       
      </tr>
      <#assign count = 1 />      
      <#list listActivoCodigoBarras as row>
        <tr class="${tableRowClass(row_index)}">        	        	        	        	
            <@displayCell text=row.fixedAssetId/>
	        <@displayCell text=row.fixedAssetName/>
	        <@displayDateCell date=row.dateAcquired format="DATE_ONLY" />
	        <@displayCurrencyCell amount=row.purchaseCost currencyUomId=row.moneda class="textright"/>                               
	        <@displayCell text=row.areaName/>
	        <@displayCell text=row.proveName/>
	        <@displayCell text=row.numeroFactura/>
	        <@displayCell text=row.placa/>
	        <@displayCell text=row.modelo/>
	        <@displayCell text=row.marca/>
	        <@displayCell text=row.serialNumber/>
	        <@displayCell text=row.idActivoAnterior?if_exists/>
			<@displayCell text=row.descripcion?if_exists/>
        </tr>
        <#assign count=count + 1/>
      </#list>
         
    </tbody>
  </table>
</form>
</#if>
</@frameSection>


