<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>



<@paginate name="listContrato" list=contratoListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">                
                <@headerCell title=uiLabelMap.ObraContratoId orderBy="contratoId"/>
                <@headerCell title=uiLabelMap.ObraSecuencia orderBy="secuencia"/>
                <@headerCell title=uiLabelMap.ObraObraId orderBy="obraId"/>
                <@headerCell title=uiLabelMap.ObraNumContrato orderBy="numContrato"/>
                <@headerCell title=uiLabelMap.ObraContratistaId orderBy="contratistaId"/>
                <@headerCell title=uiLabelMap.ObraContratista orderBy="groupName"/>
                <@headerCell title=uiLabelMap.ObraProductIdentificador orderBy="productId"/>
                <@headerCell title=uiLabelMap.ObraProductId orderBy="internalName"/>
                <@headerCell title=uiLabelMap.ObraDescripcion orderBy="descripcion"/>
                <@headerCell title=uiLabelMap.ObraStatus orderBy="statusId"/>
            </tr>
            
            <#list pageRows as row>
            
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.contratoId href="vistaInicioContratoObra?obraId=${row.obraId}&contratoId=${row.contratoId}"/>
                <@displayCell text=row.secuencia/>
                <@displayCell text=row.obraId/>
                <@displayCell text=row.numContrato/>
                <@displayCell text=row.contratistaId/>
                <@displayCell text=row.groupName/>
                <@displayCell text=row.productId/>
                <@displayCell text=row.internalName/>
                <@displayCell text=row.descripcion/>
                <@displayCell text=row.statusId/>
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>
