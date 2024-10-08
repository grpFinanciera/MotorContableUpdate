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
<#-- Copyright (c) Open Source Strategies, Inc. -->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#if hasUpdatePermission?exists && hasUpdatePermission == true>
  <#assign updateLink = "<a class='subMenuButton' href='updateSupplierForm?partyId=" + partySummary.partyId + "'>" + uiLabelMap.CommonEdit + "</a>">
</#if>

<div class="subSectionBlock">

  <@sectionHeader title=uiLabelMap.ProductSupplier>
    <div class="subMenuBar">${updateLink?if_exists}</div>
  </@sectionHeader>

  <table class="twoColumnForm">
    <@displayRow title=uiLabelMap.CommonName text="${partySummary.groupName} (${partySummary.partyId})" />
    <@displayRow title=uiLabelMap.OpentapsTaxAuthPartyId text=partySummary.federalTaxId />
    <@displayRow title=uiLabelMap.OpentapsRegimen text=partySummary.regimenId />
    <@displayRow title=uiLabelMap.OpentapsTamanioAuxiliar text=partySummary.tamanioAuxiliarId />
    <@displayRow title=uiLabelMap.OpentapsSectorEconomico text=partySummary.sectorEconomicoId />
    <@displayRow title=uiLabelMap.OpentapsOrigenCapital text=partySummary.origenCapitalId />
    <#--
    <@displayIndicatorRow title=uiLabelMap.OpentapsRequires1099 value=partySummary.requires1099?default("") />
    <@displayIndicatorRow title=uiLabelMap.OpentapsIsIncorporated value=partySummary.isIncorporated?default("") />-->
  </table>

</div>

