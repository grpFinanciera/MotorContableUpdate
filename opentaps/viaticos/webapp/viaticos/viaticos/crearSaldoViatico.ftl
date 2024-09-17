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
 * @author     Jesus Ruiz
 * @since      1.0
 
-->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form method="post" action="<@ofbizUrl>crearSaldoViatico</@ofbizUrl>" name="crearSaldoViatico" >
  <table class="twoColumnForm">
    <tbody>
       <tr>
        <@displayTitleCell title=uiLabelMap.PurchSolicitante titleClass="requiredField" />
        <@inputLookupCell lookup="LookupPartyName" form="crearSaldoViatico" name="catalogoId" />
       </tr>
       <tr> 
	      <@displayTitleCell title=uiLabelMap.PurchSaldo titleClass="requiredField"/>
	      <@inputCurrencyCell name="saldo" currencyName="uomId" disableCurrencySelect=true default=0/>
	   </tr>
	   <tr> 
	      <@inputSelectRow title=uiLabelMap.PurchPeriodo list=listPeriodo  displayField="enumCode" name="enumCode"  default=enumCode?if_exists titleClass="requiredField"/>
	   </tr>	   	 
      <@inputSubmitRow title=uiLabelMap.CommonCrear />
    </tbody>
  </table>
</form>