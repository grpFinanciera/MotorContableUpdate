<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>



<@paginate name="listOrdenPagoPatri" list=ordenPagoListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.OrdenPagoPatrimonialId orderBy="idPendienteTesoreria"/>
                <@headerCell title=uiLabelMap.Tipo orderBy="tipo"/>                
                <@headerCell title=uiLabelMap.Descripcion orderBy="descripcion"/>
                <@headerCell title=uiLabelMap.Moneda orderBy="moneda"/>                                
                <@headerCell title=uiLabelMap.FinancialsTransactionDate orderBy="fechaTransaccion"/>
                <@headerCell title=uiLabelMap.FinancialsAccountigDate orderBy="fechaContable"/>
                <@headerCell title=uiLabelMap.Evento orderBy="acctgTransTypeId"/>
                <@headerCell title=uiLabelMap.FinancialsTipoEvento orderBy="descripcionEvento"/>
                <@headerCell title=uiLabelMap.bancoId orderBy="bancoId"/>
                <@headerCell title=uiLabelMap.FinancialsCuentaBancaria orderBy="cuentaBancariaId"/>                               
                <@headerCell title=uiLabelMap.Estatus orderBy="estatus"/>
                <@headerCell title=uiLabelMap.FinancialsDescripcionEstatus orderBy="descripcionEstatus"/>
                
                
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.idPendienteTesoreria href="verOrdenPagoPatrimonial?idPendienteTesoreria=${row.idPendienteTesoreria}&tipo=${row.tipo}"/>
                <@displayCell text=row.tipo/>               
                <@displayCell text=row.descripcion/>
                <@displayCell text=row.moneda/>
                <@displayDateCell date=row.fechaTransaccion />
                <@displayDateCell date=row.fechaContable />
				<@displayCell text=row.acctgTransTypeId/>
				<@displayCell text=row.descripcionEvento/>
				<@displayCell text=row.bancoId/>
				<@displayCell text=row.cuentaBancariaId/>
                <@displayCell text=row.estatus/>
                <@displayCell text=row.descripcionEstatus/>
            </tr>
            </#list>

        </table>
    </#noparse>
</@paginate>


      
   
      
      