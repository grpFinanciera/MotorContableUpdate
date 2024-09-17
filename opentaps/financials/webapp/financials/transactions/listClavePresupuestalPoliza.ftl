
<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<#assign hostname = request.getServerName() />
<@paginate name="paginaClavePoliza" list=clavesListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>                
        <table class="listTable">
            <tr class="listTableHeader">
				<@headerCell title=uiLabelMap.FormFieldTitle_numeroPoliza orderBy="poliza"/> 	            
                <@headerCell title=uiLabelMap.clavePresupuestal orderBy="clavePresupuestal"/>
				<@headerCell title=uiLabelMap.FinancialsTipoPoliza orderBy="description"/>	
                <@headerCell title=uiLabelMap.AccountingTipoDocumento orderBy="acctgTransTypeId"/>
                <@headerCell title=uiLabelMap.FinancialsFechaContable orderBy="postedDate"/>                
				<@headerCell title=uiLabelMap.FinancialMomento orderBy="descripcionMomento"/>
                <@headerCell title=uiLabelMap.FinancialsMovimiento orderBy="debitCreditFlag" />                               
                <@headerCell title=uiLabelMap.CommonAmount orderBy="amount"/>
                <@headerCell title=uiLabelMap.FormFieldTitle_createdByUserLogin orderBy="name"/>            
            </tr>
            <#list pageRows as row>       
            <tr class="${tableRowClass(row_index)}">
				<@displayLinkCell text=row.poliza href="viewAcctgTrans?acctgTransId=${row.acctgTransId}" />
                <@displayCell text=row.clavePresupuestal/>
                <@displayCell text=row.description/>
                <@displayCell text=row.acctgTransTypeId/>
				<@displayDateCell date=row.postedDate format="DATE" />
                <@displayCell text=row.descripcionMomento/>
				<@displayCell text=row.debitCreditFlag/>                
                <@displayCurrencyCell amount=row.amount currencyUomId=row.currencyUomId class="textright"/>
                <@displayCell text=row.name/>      
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>