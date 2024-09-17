
<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<#assign hostname = request.getServerName() />
<@paginate name="listBalanceFinal" list=balanceFinalListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>                
        <table class="listTable">
            <tr class="listTableHeader">
				<@headerCell title=uiLabelMap.AccountingAccount orderBy="glAccountId"/> 	
            	<@headerCell title=uiLabelMap.NombreCuenta orderBy="nombreGlAccount"/>
				<#--<@headerCell title=uiLabelMap.AccountingSaldoInicial orderBy="endingBalanceAnterior"/>-->			            
                <#--<<@headerCell title=uiLabelMap.FinancialsDebitTotal orderBy="postedDebits"/>-->
				<#--<@headerCell title=uiLabelMap.FinancialsCreditTotal orderBy="postedCredits"/>-->					
                <@headerCell title=uiLabelMap.AccountingSaldoCierre orderBy ="endingBalance"  align="right" />
				<#--<@headerCell title=uiLabelMap.AccountingSaldoFinal orderBy ="saldoFinal"/>-->	
				            
            </tr>
            <#list pageRows as row>       
            <tr class="${tableRowClass(row_index)}"/>
				<@displayCell text=row.glAccountId />
            	<@displayCell text=row.nombreGlAccount />
				<#--<@displayCurrencyCell amount=row.endingBalanceAnterior currencyUomId=row.currencyUomId class="textright"/>-->
                <#--<@displayCurrencyCell amount=row.postedDebits currencyUomId=row.currencyUomId class="textright"/>-->
                <#--<@displayCurrencyCell amount=row.postedCredits currencyUomId=row.currencyUomId class="textright"/>-->					
                <@displayCurrencyCell amount=row.endingBalance currencyUomId=row.currencyUomId class="textright"/>
				<#--<@displayCurrencyCell amount=row.saldoFinal currencyUomId=row.currencyUomId class="textright"/>-->
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>