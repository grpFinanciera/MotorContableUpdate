<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@paginate name="listaProductosEnMinimo" list=listaProductosEnMinimo rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.ProductId orderBy="productId"/>
                <@headerCell title=uiLabelMap.ProductName orderBy="productName"/>
                <@headerCell title=uiLabelMap.FacilityId orderBy="facilityId"/>
                <@headerCell title=uiLabelMap.FacilityName orderBy="almacenName"/>
                <@headerCell title=uiLabelMap.Maximo orderBy="maximo"/>
                <@headerCell title=uiLabelMap.Minimo orderBy="minimo"/>
                <@headerCell title=uiLabelMap.CantidadActual orderBy="cantidadActual"/>            
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayCell text=row.productId/>
                <@displayCell text=row.productName/>
                <@displayCell text=row.facilityId/>
                <@displayCell text=row.almacenName/>
                <@displayCell text=row.maximo/>
                <@displayCell text=row.minimo/>   
                <#if (row.cantidadActual <= row.minimo)>                             
                	<@displayCell style="color: red;" text=row.cantidadActual/>
                <#else>	
                	<@displayCell text=row.cantidadActual/>
                </#if>	
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>
