<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<@paginate name="ExcelSuficiencia" list=clavesMomentoList rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.clavePresupuestal orderBy="clavePresupuestal" align="center"/>
                <@headerCell title=uiLabelMap.FinancialEnero orderBy="M01" align="center"/>
                <@headerCell title=uiLabelMap.FinancialFebrero orderBy="M02" align="center"/>
                <@headerCell title=uiLabelMap.FinancialMarzo orderBy="M03" align="center"/>
                <@headerCell title=uiLabelMap.FinancialAbril orderBy="M04" align="center"/>
                <@headerCell title=uiLabelMap.FinancialMayo orderBy="M05" align="center"/>
                <@headerCell title=uiLabelMap.FinancialJunio orderBy="M06" align="center"/>
                <@headerCell title=uiLabelMap.FinancialJulio orderBy="M07" align="center"/>
                <@headerCell title=uiLabelMap.FinancialAgosto orderBy="M08" align="center"/>
				<@headerCell title=uiLabelMap.FinancialSeptiembre orderBy="M09" align="center"/> 
				<@headerCell title=uiLabelMap.FinancialOctubre orderBy="M10" align="center"/> 
				<@headerCell title=uiLabelMap.FinancialNoviembre orderBy="M11" align="center"/>
				<@headerCell title=uiLabelMap.FinancialDiciembre orderBy="M12" align="center"/>
				<@headerCell title=uiLabelMap.FinancialsTotalAmount orderBy="total" align="center"/>                             
            </tr>
            <#list pageRows as row>     
	            <tr>
	            	<#if row.clavePresupuestal?exists >	
						<#assign estilo="" />
					<#else>
						<#assign estilo="font-weight:bold;"  />
					</#if>
	                <@displayCell text=row.clavePresupuestal />
					<@displayCurrencyCell currencyUomId=row.currencyUomId amount=row.M01 style=estilo/>
					<@displayCurrencyCell currencyUomId=row.currencyUomId amount=row.M02 style=estilo/>
					<@displayCurrencyCell currencyUomId=row.currencyUomId amount=row.M03 style=estilo/>
					<@displayCurrencyCell currencyUomId=row.currencyUomId amount=row.M04 style=estilo/>
					<@displayCurrencyCell currencyUomId=row.currencyUomId amount=row.M05 style=estilo/>
					<@displayCurrencyCell currencyUomId=row.currencyUomId amount=row.M06 style=estilo/>
					<@displayCurrencyCell currencyUomId=row.currencyUomId amount=row.M07 style=estilo/>
					<@displayCurrencyCell currencyUomId=row.currencyUomId amount=row.M08 style=estilo/>
					<@displayCurrencyCell currencyUomId=row.currencyUomId amount=row.M09 style=estilo/>
					<@displayCurrencyCell currencyUomId=row.currencyUomId amount=row.M10 style=estilo/>
					<@displayCurrencyCell currencyUomId=row.currencyUomId amount=row.M11 style=estilo/>
					<@displayCurrencyCell currencyUomId=row.currencyUomId amount=row.M12 style=estilo/>
					<@displayCurrencyCell currencyUomId=row.currencyUomId amount=row.total style="font-weight:bold;" />
	            </tr>       
            </#list>
        </table>
    </#noparse>
</@paginate>
