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

<@frameSection title=uiLabelMap.WarehouseChooseWarehouse>
  <#if facilities.size() != 0>
    <form method="post" action="<@ofbizUrl>setFacility</@ofbizUrl>">
      <select name="facilityId" class="selectBox">
        <#list facilities as facility>
          <option value="${facility.facilityId}">${facility.facilityName}</option>
        </#list>
      </select>
    	<#assign cicloDefault = Static["org.ofbiz.base.util.UtilDateTime"].getYear(Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp(),timeZone,locale)/>
		<@inputSelectRow title=uiLabelMap.FormFieldTitleCiclo name="cicloId" list=listCiclos key="periodName" displayField="periodName" 
			default=cicloDefault?string />
	  <tr><td>
      <input type="submit" class="smallSubmit" value="${uiLabelMap.CommonSelect}"/>
    </form>
  </#if>
  
  <#if hasCreateWarehousePermission>
    <p><a href="<@ofbizUrl>createWarehouseForm</@ofbizUrl>" class="tabletext">${uiLabelMap.WarehouseCreateNewWarehouse}</a></p>
  </#if>
</@frameSection>
