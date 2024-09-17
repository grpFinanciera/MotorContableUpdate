<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<@paginate name="listHistorialBienesActivoFijo" list=activoFijoListBuilder rememberPage=false>    
    <#noparse>    
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.ControlId orderBy="fixedAssetId"/>
                <@headerCell title=uiLabelMap.ControlNombre orderBy="fixedAssetName"/>
                <@headerCell title=uiLabelMap.ControlFechaAdquisicion orderBy="dateAcquired"/>                															   
                <@headerCell title=uiLabelMap.ControlCostoCompra orderBy="purchaseCost"/>
                <@headerCell title=uiLabelMap.ControlResguardanteNombre orderBy="resguName"/>                
                <@headerCell title=uiLabelMap.ControlAreaNombre orderBy="areaName"/>
                <@headerCell title=uiLabelMap.ControlProveedorNombre orderBy="proveName"/>
                <@headerCell title=uiLabelMap.ControlNumeroFactura orderBy="numeroFactura"/>
                <@headerCell title=uiLabelMap.ControlPlaca orderBy="placa"/>
                <@headerCell title=uiLabelMap.ControlModelo orderBy="modelo"/>
                <@headerCell title=uiLabelMap.ControlMarca orderBy="marca"/>
                <@headerCell title=uiLabelMap.ControlSerie orderBy="serialNumber"/>
                <@headerCell title=uiLabelMap.ControlIdAnterior orderBy="idActivoAnterior"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.fixedAssetId href="verHistorialActivoFijo?fixedAssetId=${row.fixedAssetId}"/>
                <@displayCell text=row.fixedAssetName?if_exists/>
                <@displayDateCell date=row.dateAcquired?if_exists format="DATE_ONLY" />
                <@displayCurrencyCell amount=row.purchaseCost?if_exists currencyUomId=row.moneda class="textright"/>                               
                <@displayCell text=row.resguName?if_exists/>
                <@displayCell text=row.areaName?if_exists/>
                <@displayCell text=row.proveName?if_exists/>
                <@displayCell text=row.numeroFactura?if_exists/>
                <@displayCell text=row.placa?if_exists/>
                <@displayCell text=row.modelo?if_exists/>
                <@displayCell text=row.marca?if_exists/>
                <@displayCell text=row.serialNumber?if_exists/> 
                <@displayCell text=row.idActivoAnterior?if_exists/>           
                <#--<td><a href="<@ofbizUrl>/ProductBarCode.pdf?productId=${row.fixedAssetId?if_exists}&amp;productName=${row.fixedAssetName?if_exists}&amp;dateAcquired=${row.dateAcquired?if_exists}</@ofbizUrl>" target="_blank">${uiLabelMap.ProductBarcode}</a> </td>-->
            </#list>
        </table>
    </#noparse>
</@paginate>
