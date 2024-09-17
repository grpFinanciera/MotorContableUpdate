<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>



<@paginate name="listOrden" list=ordenListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.ordenPagoId orderBy="createdStamp"/>
                <@headerCell title=uiLabelMap.invoiceId orderBy="invoiceId"/>
                <@headerCell title=uiLabelMap.FinancialsAcctgTransTypeId orderBy="acctgTransTypeId"/>
                <@headerCell title=uiLabelMap.AccountingTipoDocumento orderBy="descripcion"/>
                <@headerCell title=uiLabelMap.FinancialsIdProveedor orderBy="partyIdFrom"/>
                <@headerCell title=uiLabelMap.FinancialsNombreProveedor orderBy="groupName"/>
                <@headerCell title=uiLabelMap.descriptionInvoice orderBy="description"/>
                <@headerCell title=uiLabelMap.Estatus orderBy="statusId"/>
                <@headerCell title=uiLabelMap.FechaDePago orderBy="fechaPago"/>
                <@headerCell title=uiLabelMap.Monto orderBy="monto"/>                                
            </tr>
            <#list pageRows as row>     
	            <tr class="${tableRowClass(row_index)}">
	                <@displayLinkCell text=row.ordenPagoId href="verOrdenPago?ordenPagoId=${row.ordenPagoId}&invoiceId=${row.invoiceId}"/>
	                <@displayCell text=row.invoiceId/>
	                <@displayCell text=row.acctgTransTypeId/>
	                <@displayCell text=row.descripcion/>
	                <@displayCell text=row.partyIdFrom/>
	                <@displayCell text=row.groupName/>
	                <@displayCell text=row.description/>	                
	                <@displayCell text=row.statusId />	                
	                <@displayDateCell date=row.fechaPago?if_exists format="DATE_ONLY" />
	                <@displayCurrencyCell currencyUomId=row.currencyUomId amount=row.monto />	                
	            </tr>       
            </#list>
        </table>
    </#noparse>
</@paginate>