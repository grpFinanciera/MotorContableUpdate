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
-->

<#-- Parametrized find form for transactions. -->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@paginate name="listPageTraspasoBanco" list=listPageTraspasoBanco rememberPage=false orgCurrencyUomId="${orgCurrencyUomId}">
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.FinancialsTransactionId orderBy="acctgTransId"/>
                <@headerCell title=uiLabelMap.CommonDescription orderBy="descripcion"/>
                <@headerCell title=uiLabelMap.FinancialsTransactionDate orderBy="createdStamp DESC"/>
				<@headerCell title=uiLabelMap.FormFieldTitle_postedDate orderBy="postedDate"/>
                <@headerCell title=uiLabelMap.BancoOrigen orderBy="bancoOrigen"/>
                <@headerCell title=uiLabelMap.BancoDestino orderBy="bancoDestino"/>
                <@headerCell title=uiLabelMap.CuentaOrigen orderBy="cuentaOrigen"/>
                <@headerCell title=uiLabelMap.CuentaDestino orderBy="cuentaDestino"/>
                <@headerCell title="Monto" orderBy="monto"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.acctgTransId href="viewAcctgTrans?acctgTransId=${row.acctgTransId}" />
                <@displayCell text=row.descripcion />
                <@displayDateCell date=row.createdStamp />
				<@displayDateCell date=row.postedDate />
                <@displayCell text=row.bancoOrigen />
                <@displayCell text=row.bancoDestino />
                <@displayCell text=row.cuentaOrigen/>
                <@displayCell text=row.cuentaDestino/>
                <@displayCurrencyCell amount=row.monto currencyUomId=parameters.orgCurrencyUomId class="textright"/>                
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>