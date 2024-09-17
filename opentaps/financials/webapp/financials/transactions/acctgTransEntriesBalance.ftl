<#--
 * Copyright (c) Open Source Strategies, Inc.
 *
 * Opentaps is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Opentaps is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Opentaps.  If not, see <http://www.gnu.org/licenses/>.
 *
-->
<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#if creditTotal?exists && debitTotal?exists && trialBalance?exists><#assign existe = "true"/><#else><#assign existe = "false"/></#if>

<div class="screenlet" style="position: fixed; bottom:200px; left:8px; width:158px; z-index:1;_position:absolute;_bottom:expression(eval(document.body.scrollBottom)-100);">
  <div class="screenlet-header">
    <span class="boxhead">${uiLabelMap.FinancialsTransactionBalance}</span>
  </div>
  <!-- orgCurrencyUomId :  ${orgCurrencyUomId!} -->
  <div class="screenlet-body">
    <#if accountingTransaction?exists || existe=="true" >
	<#if orgCurrencyUomId?has_content ><#assign moneda = orgCurrencyUomId! /><#else><#assign moneda = currencyUomId! /></#if>
      <table width="100%" cellpadding="0" cellspacing="2">
        <tr>
          <td><div class="tabletext"><b>${uiLabelMap.FinancialsDebitTotal}</b></div></td>
			<#if existe=="true"><#assign debit = debitTotal/><#else><#assign debit = accountingTransaction.debitTotal /></#if> 
          <td align="right"><@displayCurrency amount=debit currencyUomId=moneda /></td>
        </tr>
        <tr>
          <td><div class="tabletext"><b>${uiLabelMap.FinancialsCreditTotal}</b></div></td>
			<#if existe=="true"><#assign credit = creditTotal/><#else><#assign credit = accountingTransaction.creditTotal /></#if>
          <td align="right"><@displayCurrency amount=credit currencyUomId=moneda /></td>
        </tr>
        <tr>
          <td><div class="tabletext"><b>${uiLabelMap.FinancialsStatementsBalance}</b></div></td>
			<#if existe=="true"><#assign trial = trialBalance /><#else><#assign trial = accountingTransaction.trialBalance /></#if>
          <td align="right"><@displayCurrency amount=trial currencyUomId=moneda /></td>
        </tr>
      </table>
    <#else/>
		<div class="tabletext">${uiLabelMap.FinancialsServiceErrorReverseTransactionNotFound} ${acctgTransId!}</div>
    </#if>
  </div>
</div>
