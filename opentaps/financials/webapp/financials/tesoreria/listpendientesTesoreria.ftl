<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<@paginate name="listPendientesTesoreria" list=listaPendientesTesoreriaBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.FinancialsPendientesTesoreriaId orderBy="idPendienteTesoreria"/>
                <@headerCell title=uiLabelMap.FinancialsTipoPendiente orderBy="tipo"/>
                <@headerCell title=uiLabelMap.Descripcion orderBy="descripcion"/>
                <@headerCell title=uiLabelMap.CAParty orderBy="auxiliar"/>
                <@headerCell title=uiLabelMap.Monto orderBy="monto"/>                
                <@headerCell title=uiLabelMap.FinancialsStatusId orderBy="estatus"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.idPendienteTesoreria href="verPendienteTesoreria?idPendienteTesoreria=${row.idPendienteTesoreria}&tipoPendienteTesoreria=${row.tipo}"/>
                <@displayCell text=row.tipo/>
                <@displayCell text=row.descripcion/>
                <@displayCell text=row.auxNombre/>
                <@displayCurrencyCell amount=row.monto currencyUomId=row.moneda />
                <@displayCell text=row.estatus/>
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>