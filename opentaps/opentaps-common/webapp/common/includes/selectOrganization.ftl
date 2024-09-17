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

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl" />

<@frameSection title=uiLabelMap.OpentapsSelectOrganization>
  <form action="setOrganization" method="POST">
    <table>
      <@inputSelectRow name="organizationPartyId" title=uiLabelMap.ProductOrganization list=configuredOrganizations key="partyId" ; option>
        ${option.groupName} (${option.partyId})
      </@inputSelectRow>
	<#assign cicloDefault = Static["org.ofbiz.base.util.UtilDateTime"].getYear(Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp(),timeZone,locale)/>
	<@inputSelectRow title=uiLabelMap.FormFieldTitleCiclo name="cicloId" list=listCiclos key="periodName" displayField="periodName" 
			default=cicloDefault?string />
      <@inputSubmitRow title=uiLabelMap.CommonSelect />
    </table>
  </form>
</@frameSection>
