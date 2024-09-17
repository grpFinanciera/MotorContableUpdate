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
--->
<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<#-- This file has been modified by Open Source Strategies, Inc. -->

<#escape x as x?xml>
       <fo:table border-spacing="3pt">
           <fo:table-column column-width="3.75in"/>
           <fo:table-column column-width="3.75in"/>
           <fo:table-body>
           <fo:table-row>    <#-- this part could use some improvement -->
             
             <#-- a special purchased from address for Purchase Orders -->
             <#if orderHeader.getString("orderTypeId") == "PURCHASE_ORDER">
             <#if supplierGeneralContactMechValueMap?exists>
               <#assign contactMech = supplierGeneralContactMechValueMap.contactMech>
               <fo:table-cell>
                 <fo:block>
                     <#-- ${uiLabelMap.OrderPurchasedFrom}: -->
                 </fo:block>
                 <#assign postalAddress = supplierGeneralContactMechValueMap.postalAddress>
                 <#if postalAddress?has_content>
                   <#-- <#if postalAddress.toName?has_content><fo:block>${postalAddress.toName}</fo:block></#if>
                   <#if postalAddress.attnName?has_content><fo:block>${postalAddress.attnName?if_exists}</fo:block></#if>
                   <fo:block>${postalAddress.address1?if_exists}</fo:block>
                   <#if postalAddress.address2?has_content><fo:block>${postalAddress.address2?if_exists}</fo:block></#if>
                   <fo:block>${postalAddress.city?if_exists}<#if postalAddress.stateProvinceGeoId?has_content>, ${postalAddress.stateProvinceGeoId} </#if><#if postalAddress.postalCode?has_content>${postalAddress.postalCode}</#if></fo:block>
                   <fo:block>${postalAddress.countryGeoId?if_exists}</fo:block> -->
                 </#if>
               </fo:table-cell>
             <#else>
               <#-- here we just display the name of the vendor, since there is no address -->
               <fo:table-cell>
                 <#assign vendorParty = orderReadHelper.getBillFromParty()>
                 <#-- <fo:block>
                   <fo:inline font-weight="bold">${uiLabelMap.OrderPurchasedFrom}:</fo:inline> ${Static['org.ofbiz.party.party.PartyHelper'].getPartyName(vendorParty)}
                 </fo:block> -->
               </fo:table-cell> 
             </#if>
             </#if>
             
             <#-- list all postal addresses of the order.  there should be just a billing and a shipping here. -->
             <#list orderContactMechValueMaps as orderContactMechValueMap>
               <#assign contactMech = orderContactMechValueMap.contactMech>
               <#assign contactMechPurpose = orderContactMechValueMap.contactMechPurposeType>
               <#if contactMech.contactMechTypeId == "POSTAL_ADDRESS">
               <#assign postalAddress = orderContactMechValueMap.postalAddress>
               <fo:table-cell>
                 <#-- <fo:block font-weight="bold">${contactMechPurpose.get("description",locale)}:</fo:block> -->
                 <#if postalAddress?has_content>
                   <#-- <#if postalAddress.toName?has_content><fo:block>${postalAddress.toName?if_exists}</fo:block></#if>
                   <#if postalAddress.attnName?has_content><fo:block>${postalAddress.attnName?if_exists}</fo:block></#if>
                   <fo:block>${postalAddress.address1?if_exists}</fo:block>
                   <#if postalAddress.address2?has_content><fo:block>${postalAddress.address2?if_exists}</fo:block></#if>
                   <fo:block>${postalAddress.city?if_exists}<#if postalAddress.stateProvinceGeoId?has_content>, ${postalAddress.stateProvinceGeoId} </#if><#if postalAddress.postalCode?has_content>${postalAddress.postalCode}</#if></fo:block> -->
                 </#if>
               </fo:table-cell>
               </#if>
             </#list>
            </fo:table-row>
         </fo:table-body>
       </fo:table>

       <fo:block white-space-collapse="false"> </fo:block> 

<fo:block space-after="10pt"/>

<#if orderHeader?has_content>
  <fo:block text-align="right" font-weight="bold" space-after="25pt">${uiLabelMap.ConstanciaRecepEncabezado}</fo:block>
  <fo:block text-align="right" space-after="25pt">${uiLabelMap.ConstanciaRecepLugar} a ${createdDay} de ${createdMonth} de ${createdYear}.</fo:block>
  <fo:block text-align="justify" space-after="20pt">Hago de su conocimiento que el o los bien(es) y/o servicio(s) consistente(s) en:</fo:block>
  <#if shipmentReceipts?has_content>
  		<#list shipmentReceipts?sort_by("itemDescription") as shipmentReceipt>
			<fo:block text-indent="20pt" space-after="4pt">- (${shipmentReceipt.quantityAccepted})   ${shipmentReceipt.itemDescription!}</fo:block>
  		</#list>
  </#if>
  <#if shipmentReceiptsDevMontos?has_content>
  		<#list shipmentReceiptsDevMontos?sort_by("itemDescription") as shipmentReceipt>
			<fo:block text-indent="20pt" space-after="4pt">- (${shipmentReceipt.cantidad})   ${shipmentReceipt.itemDescription!}</fo:block>
  		</#list>
  </#if>  	
  <fo:block text-align="justify" space-after="20pt"></fo:block>
  <fo:block text-align="justify" space-after="20pt">${uiLabelMap.ConstanciaSuministrado}</fo:block> 
  <fo:block text-align="justify" space-after="20pt">${proveedor} </fo:block>
  <fo:block text-align="justify" space-after="20pt">${uiLabelMap.ConstanciaCorrespondienteA} ${orderId}.</fo:block>
  <fo:block text-align="justify" space-after="25pt">${uiLabelMap.AlmacenReceptor}: ${idAlmacenReceptor} ${almacenReceptor}</fo:block>  
  <fo:block text-align="justify" space-after="20pt">${uiLabelMap.ConstanciaSatisfaccion}</fo:block>
  <fo:block text-align="justify" space-after="20pt">${uiLabelMap.ConstanciaPorloAnterior} ${numeroFactura}, de
        											fecha ${fechaFactura}, por el monto de ${montoFactura} M.N. (${montoLetraFactura} M.N.).</fo:block>
  <fo:block text-align="justify" space-after="20pt">${uiLabelMap.ConstanciaParaEfectos}</fo:block>
  <fo:block text-align="justify" space-after="20pt">Sin otro particular, quedo de usted.</fo:block>
  <fo:block text-align="center" space-after="5pt">${receptor}</fo:block>
  <fo:block text-align="center" space-after="5pt">${firma}</fo:block>
</#if>

</#escape>
