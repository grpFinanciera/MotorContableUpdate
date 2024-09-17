<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>



<@paginate name="listObraInicio" list=obraInicioListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.ObraObraId orderBy="obraId"/>
                <@headerCell title=uiLabelMap.ObraNombre orderBy="nombre"/>
                <@headerCell title=uiLabelMap.ObraDescripcion orderBy="descripcion"/>
                <@headerCell title=uiLabelMap.ObraEstatus orderBy="statusId"/>
                <@headerCell title=uiLabelMap.ObrasMontoAutorizado orderBy="montoAutorizado"/>
                <@headerCell title=uiLabelMap.ObraValorObra orderBy="valorObra"/>
                <@headerCell title=uiLabelMap.ObraProductId orderBy="productId"/>
            </tr>
            
            <#list pageRows as row>
            
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.obraId href="vistaObra?obraId=${row.obraId}"/>
                <@displayCell text=row.nombre/>
                <@displayCell text=row.descripcion/>
                <@displayCell text=row.statusId/>
                <@displayCell text=row.montoAutorizado/>
                <@displayCell text=row.valorObra/>
                <@displayCell text=row.productId/>
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>
