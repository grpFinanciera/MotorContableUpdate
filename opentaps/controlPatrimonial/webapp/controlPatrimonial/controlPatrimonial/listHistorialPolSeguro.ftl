<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<@paginate name="listHistorialActivoFijo" list=historialListBuilder rememberPage=false>    
    <#noparse>    
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.ControlNumPoliza orderBy="numPolizaNuevo"/>
                <@headerCell title=uiLabelMap.ControlNumPolizaAnt orderBy="numPolizaAnt"/>
                <@headerCell title=uiLabelMap.ControlFechaDesde orderBy="fechaDesdeNueva"/>                															   
                <@headerCell title=uiLabelMap.ControlFechaHasta orderBy="fechaHastaNueva"/>
                <@headerCell title=uiLabelMap.ControlFechaCambio orderBy="fechaCambio"/>                
                <@headerCell title=uiLabelMap.ControlComentario orderBy="comentario"/>                
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">                
                <@displayCell text=row.numPolizaNuevo/>
                <@displayCell text=row.numPolizaAnt/>
                <@displayDateCell date=row.fechaDesdeNueva format="DATE_ONLY" />
                <@displayDateCell date=row.fechaHastaNueva format="DATE_ONLY" />
                <@displayDateCell date=row.fechaCambio format="DATE_ONLY" />                
                <@displayCell text=row.comentario/>            
            </#list>
        </table>
    </#noparse>
</@paginate>
