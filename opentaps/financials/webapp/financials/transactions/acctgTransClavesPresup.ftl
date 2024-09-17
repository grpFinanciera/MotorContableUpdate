<#--
 * Lista de resultados preliminares de p�lizas contables
 * Author: Vidal Garc�a
 * Versi�n 1.0
 * Fecha de Creaci�n: Julio 2013
-->

<#-- Parametrized find form for transactions. -->
<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<@frameSection title=uiLabelMap.FinancialBudgetKeys >
	<#if clavesPresupuestales?has_content>
	<table class="listTable">
		<#list clavesPresupuestales as row>
			<#if row.clavePresupuestal?has_content>
    		<tr>   
				<@displayLinkCell text=row.clavePresupuestal href="viewAcctgPresupuestal?acctgTransId=${row.acctgTransId}&clavePresupuestal=${row.clavePresupuestal}"/>
    		</tr>
			</#if>
    	</#list>
	</table>
	</#if>
</@frameSection>

        

