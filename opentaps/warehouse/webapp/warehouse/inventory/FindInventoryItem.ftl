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

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form method="post" action="<@ofbizUrl>findInventoryItem</@ofbizUrl>" name="findInventoryItem">
  <table>
    <@inputLookupRow title=uiLabelMap.ProductLocation name="locationSeqId" lookup="LookupFacilityLocation" form="findInventoryItem" />
    <@inputAutoCompleteProductRow name="productId" title=uiLabelMap.Product />
    <@inputTextRow name="internalName" title=uiLabelMap.ProductInternalName />
    <@inputTextRow name="serialNumber" title=uiLabelMap.ProductSerialNumber />
    <@inputLookupRow title=uiLabelMap.ProductLotId name="lotId" lookup="LookupLot" form="findInventoryItem" />
	<@displayRow title=uiLabelMap.OrganizationWH text=organizationName!'' />
    <@inputHidden name="performFind" value="Y" />
    <@inputSubmitRow title=uiLabelMap.WarehouseFindInventoryItem />
  </table>
</form>
