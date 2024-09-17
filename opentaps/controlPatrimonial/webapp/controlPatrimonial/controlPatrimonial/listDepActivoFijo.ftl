<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<@paginate name="listDepreciacionListBuilder" list=depreciacionListBuilder rememberPage=false>    
    <#noparse>    
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.ControlId orderBy="fixedAssetId"/>
                <@headerCell title=uiLabelMap.ControlNombre orderBy="fixedAssetName"/>
                <@headerCell title=uiLabelMap.ControlFechaAdquisicion orderBy="dateAcquired"/>
                <@headerCell title=uiLabelMap.ControlTipoActivo orderBy="descTipoActivo"/>
                <@headerCell title=uiLabelMap.ControlMes orderBy="mes"/>
                <@headerCell title=uiLabelMap.ControlAnio orderBy="ciclo"/>
                <@headerCell title=uiLabelMap.ControlMontoDepreciacion orderBy="monto"/>
                <@headerCell title=uiLabelMap.ControlPorcentajeMensual orderBy="porcentajeDepreciacion"/>
                <@headerCell title=uiLabelMap.ControlDepreciacionAnual orderBy="montoDepreciacion"/>
                <@headerCell title=uiLabelMap.ControlCostoCompra orderBy="purchaseCost"/>
                <@headerCell title=uiLabelMap.ControlPolizaContable orderBy="poliza"/>                
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.fixedAssetId href="verActivoFijo?fixedAssetId=${row.fixedAssetId?if_exists}"/>
                <@displayCell text=row.fixedAssetName?if_exists/>
                <@displayDateCell date=row.dateAcquired?if_exists format="DATE_ONLY" />
                <@displayCell text=row.descTipoActivo?if_exists/>
                <@displayCell text=row.mes?if_exists/>
                <@displayCell text=row.ciclo?if_exists/>
                <@displayCurrencyCell amount=row.monto?if_exists currencyUomId=row.moneda?if_exists class="textright"/>
                <@displayCell text=row.porcentajeDepreciacion?if_exists/>
                <@displayCell text=row.montoDepreciacion?if_exists/>
                <@displayCurrencyCell amount=row.purchaseCost?if_exists currencyUomId=row.moneda?if_exists class="textright"/>                
                <@displayLinkCell href="viewAcctgTrans?acctgTransId=${row.acctgTransId?if_exists}" text=row.poliza />
           	</tr>                               
            </#list>
        </table>
    </#noparse>
</@paginate>
