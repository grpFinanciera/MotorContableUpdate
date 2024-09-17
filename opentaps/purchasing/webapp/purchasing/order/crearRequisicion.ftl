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
<#--
 *  Copyright (c) 2001-2005 The Open For Business Project - www.ofbiz.org
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a
 *  copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  and/or sell copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 *  OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 *  OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 *  THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *@author     Salvador Cortes
 *@author     Rodrigo Ruiz
 *@since      1.0
-->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@frameSection title=uiLabelMap.PurchCrearRequisicion >
<form id="crearRequisicionForm" name="crearRequisicionForm" method="post" action="<@ofbizUrl>registraRequisicion</@ofbizUrl>">
  <table class="twoColumnForm">
    <tbody>
     <tr>
        <@displayTitleCell title=uiLabelMap.FinancialsAccountigDate titleClass="requiredField" />
        <@inputDateTimeCell name="fechaContable" default=Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp() />
     </tr> 
    <tr>  
      <@displayTitleCell title=uiLabelMap.PurchDescripcion titleClass="requiredField"/>
      <@inputTextCell name="descripcion" maxlength="255" size=50/>
    </tr>
	<tr>  
      <@inputTextareaRow title=uiLabelMap.PurchJustification name="justificacion" default="" titleClass="requiredField"/>
    </tr>
    <tr>	
    	<#assign userLoginId = userLogin.userLoginId />
    	<#assign areaId = Static["org.opentaps.purchasing.order.ConsultarRequisicion"].obtenerArea(delegator,userLoginId)! />
    	<@displayTitleCell title=uiLabelMap.PurchArea />
    	<@inputHidden name="areaPartyId" value=areaId! />
    	<#assign areaNombreObj = Static["org.opentaps.purchasing.order.ConsultarRequisicion"].obtenPartyGroup(delegator,areaId)! />
    	<@displayCell text=areaNombreObj.groupName! />
    </tr>
    <#-- Se quita porque la obtiene del usuario
    <tr>
      <@displayTitleCell title=uiLabelMap.PurchArea />
      <@inputSelectCell key="partyId" list=listAreas name="areaPartyId" displayField="groupName"/>
    </tr>
    <tr>  
      <@displayTitleCell title=uiLabelMap.PurchAlmacen />
      <@inputSelectCell key="facilityId" list=listAlmacenes name="almacenId" displayField="facilityName"/>
    </tr> -->
    <tr>  
      <@displayTitleCell title=uiLabelMap.PurchMoneda titleClass="requiredField"/>
      <@inputSelectCell key="uomId" list=listMonedas name="tipoMoneda" displayField="uomId" default="MXN"/>
    </tr>    
    <tr>        
        <@inputHidden name="organizationPartyId" value="${parameters.organizationPartyId}" index=tag_index />
    </tr>
    <@inputSubmitRow title=uiLabelMap.CommonCreate />  
    </tbody>
  </table>
</form>
</@frameSection>