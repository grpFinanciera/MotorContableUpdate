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
 *@author     Rodrigo Ruiz
 *@since      1.0
-->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@frameSection title=uiLabelMap.ViaticosHome >
<form id="creaViaticoForm" name="creaViaticoForm" method="post" action="<@ofbizUrl>registrarViatico</@ofbizUrl>">
  <table class="twoColumnForm">
    <tbody>
    <tr>  
      <@displayTitleCell title=uiLabelMap.PurchDestination titleClass="requiredField"/>
      <@inputTextCell name="descripcion" maxlength="255" size=50/>
    </tr>
    <tr>	
    	<#assign userLogin = userLogin.userLoginId />
    	<#assign areaId = Static["com.absoluciones.viaticos.ConsultarViatico"].obtenerArea(delegator,userLogin)! />
    	<@displayTitleCell title=uiLabelMap.ViaticoArea titleClass="requiredField"/>
    	<@inputHidden name="areaPartyId" value=areaId! />
    	<#assign areaNombreObj = Static["com.absoluciones.viaticos.ConsultarViatico"].obtenPartyGroup(delegator,areaId)! />
    	<@displayCell text=areaNombreObj.groupName! />    	
    </tr>
	<@inputTextareaRow title=uiLabelMap.ViaticoJustification name="justificacion" default="" titleClass="requiredField"/>
	<@inputSelectRow name="programa2" title=uiLabelMap.ViaticoPrograma
		list=Programas2!?sort_by("nombrePrograma") key="programaId" displayField="nombrePrograma" titleClass="requiredField" required=false />
	<@inputCurrencySelectRow name="tipoMoneda" title=uiLabelMap.CommonCurrency defaultCurrencyUomId="MXN" titleClass="requiredField"/>
    <@displayTitleCell title=uiLabelMap.ViaticoRecurso titleClass="requiredField"/>
    <#assign recursos = ["Fiscales","Propios"]/>
    <td>
    	<select name="recurso" class="inputBox">
        	<#list recursos as recurso>
            		<option value="${recurso}">${recurso}</option>
        	</#list>
        </select>
    </td>
	<@inputSelectRow name="fuenteFinanciamientoId" title=uiLabelMap.ViaticoFuente
		list=FuenteFinanciamiento!?sort_by("descripcion") key="fuenteFinanciamientoId" displayField="descripcion" titleClass="requiredField" required=false />
	<@inputSelectRow name="tipoTransporte" title=uiLabelMap.PurchTransportType
		list=TiposTransportes! key="tipoTransporteId" displayField="descripcion" default="PUBLICO" titleClass="requiredField"/>
	<@inputCurrencyRow name="montoDiario" title=uiLabelMap.DailyAmount disableCurrencySelect=true />
	<@inputCurrencyRow name="montoTrabCampo" title=uiLabelMap.FieldAmount disableCurrencySelect=true />
	<@inputCurrencyRow name="montoTransporte" title=uiLabelMap.PurchTransportAmount disableCurrencySelect=true titleClass="requiredField"/>
			
	<@inputAutoCompleteZonaGeoRow title=uiLabelMap.PurchOrigin name="geoOrigen" titleClass="requiredField"/>
	<@inputAutoCompleteZonaGeoRow title=uiLabelMap.PurchDestination name="geoDestino" titleClass="requiredField"/>
				
	<!--@inputSelectRow name="geoOrigen" title=uiLabelMap.PurchOrigin
		list=Geos! key="geoId" displayField="geoName" titleClass="requiredField" />
	<@inputSelectRow name="geoDestino" title=uiLabelMap.PurchDestination
		list=Geos! key="geoId" displayField="geoName" titleClass="requiredField" />-->								
	<@inputDateRow name="fechaInicio" title=uiLabelMap.CommonStartDate form="solicitudViatico" titleClass="requiredField"/>
	<@inputDateRow name="fechaFin" title=uiLabelMap.CommonEndDate form="solicitudViatico" titleClass="requiredField"/>
	<@inputSubmitRow title=uiLabelMap.CommonCreate />  
    </tbody>
  </table>
</form>
</@frameSection>