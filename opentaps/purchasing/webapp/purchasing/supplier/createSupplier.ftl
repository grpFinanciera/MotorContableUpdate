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

<form name="createSupplierParty" method="post" action="<@ofbizUrl>createSupplierParty</@ofbizUrl>">

<table class="fourColumnForm">
	
  <tr><td colspan="4" class="formSectionHeaderTitle">${uiLabelMap.PurchSupplierDetails}</td></tr>
  <@inputTextRow name="partyId" title=uiLabelMap.PartyPartyId size=20 maxlength=20 titleClass="requiredField"/>
  <@inputTextRow name="groupName" title=uiLabelMap.PurchSupplierName titleClass="requiredField" />
  <@displayTitleCell title=uiLabelMap.PurchTipoAuxiliar titleClass="requiredField"/>
  <@inputSelectCell list=listRoles?if_exists displayField="description" name="roleTypeId" id="roleTypeId" default="PROVEEDOR"?if_exists/>
  <#if requestAttributes.duplicateSuppliersWithName?has_content>
    <tr>
      <td/>
      <td colspan="3"><span class="errorMessage">${uiLabelMap.PurchSuppliersWithDuplicateName}:</span> <#list requestAttributes.duplicateSuppliersWithName as dup><@displayPartyLink partyId=dup.partyId /><#if dup_has_next>, </#if></#list></td>
    </tr>
  </#if>  
  <@inputTextRow name="federalTaxId" title=uiLabelMap.OpentapsTaxAuthPartyId />
  <tr><@displayTitleCell title=uiLabelMap.OpentapsRegimen/>
  <@inputSelectCell list=regimen?if_exists displayField="nombreRegimen" name="regimenId" id="regimenId" required=false/></tr>
  <tr><@displayTitleCell title=uiLabelMap.OpentapsTamanioAuxiliar/>
  <@inputSelectCell list=tamanioAuxiliar?if_exists displayField="nombreTamanioAuxiliar" name="tamanioAuxiliarId" id="tamanioAuxiliarId" required=false/></tr>
  <tr><@displayTitleCell title=uiLabelMap.OpentapsSectorEconomico/>
  <@inputSelectCell list=sectorEconomico?if_exists displayField="nombreSectorEconomico" name="sectorEconomicoId" id="sectorEconomicoId" required=false/></tr>
  <tr><@displayTitleCell title=uiLabelMap.OpentapsOrigenCapital/>
  <@inputSelectCell list=origenCapital?if_exists displayField="nombreOrigenCapital" name="origenCapitalId" id="origenCapitalId" required=false/></tr>
  <@inputCurrencyRow name="saldoInicial" title=uiLabelMap.AccountingSaldoInicial defaultCurrencyUomId="MXN" disableCurrencySelect=true  />
  <@inputHidden name="requires1099" value="N"/>
	<@inputHidden name="organizationPartyId" value = organizationPartyId />
  <tr>
  	<@displayTitleCell title=uiLabelMap.Rupc/>
  	<@inputTextCell name="rupc" id="rupc"/>
  </tr>
  <tr>
  	<@displayTitleCell title=uiLabelMap.GiroEmpresa/>
  	<@inputTextCell name="giroEmpresa" id="giroEmpresa"/>
  </tr>
  <tr>
  	<@displayTitleCell title=uiLabelMap.NacionalExtranjero/>
  	<@inputSelectCell list=nacionalExtList?if_exists displayField="nacionalExtranjero" name="nacionalExtranjero" id="nacionalExtranjero" required=false/>
  </tr>  
  <#--
  <@inputIndicatorRow name="requires1099" title=uiLabelMap.OpentapsRequires1099 />
  <@inputIndicatorRow name="isIncorporated" title=uiLabelMap.OpentapsIsIncorporated /> -->

  <tr><td colspan="4">&nbsp;</td></tr>
  <tr><td colspan="4" class="formSectionHeaderTitle">${uiLabelMap.PartyContactInformation}</td></tr>
  <tr>
    <@displayTitleCell title=uiLabelMap.PartyPhoneNumber />
    <td colspan="3">
      <input name="primaryPhoneCountryCode" size=6 maxlength=6 class="inputBox" value="${Static["org.opentaps.common.util.UtilConfig"].getPropertyValue(opentapsApplicationName, "defaultCountryCode")}" /> -
      (<input name="primaryPhoneAreaCode" size=6 maxlength=10 class="inputBox" />)
      <input name="primaryPhoneNumber" size=10 maxlength=20 class="inputBox" /> ${uiLabelMap.PartyContactExt}
      <input name="primaryPhoneExtension" size=10 maxlength=20 class="inputBox" />
    </td>
  </tr>
  <tr>
    <@displayTitleCell title=uiLabelMap.PartyFaxNumber />
    <td colspan="3">
      <input name="primaryFaxCountryCode" size=6 maxlength=6 class="inputBox" value="${Static["org.opentaps.common.util.UtilConfig"].getPropertyValue(opentapsApplicationName, "defaultCountryCode")}" /> -
      (<input name="primaryFaxAreaCode" size=6 maxlength=10 class="inputBox" />)
      <input name="primaryFaxNumber" size=10 maxlength=20 class="inputBox" /> ${uiLabelMap.PartyContactExt}
      <input name="primaryFaxExtension" size=10 maxlength=20 class="inputBox" />
    </td>
  </tr>  
  <tr>
    <@displayTitleCell title=uiLabelMap.PartyEmailAddress />
    <@inputTextCell name="primaryEmail" />
    <@displayTitleCell title=uiLabelMap.OpentapsWebUrl />
    <@inputTextCell name="primaryWebUrl" />
  </tr>

  <tr><td colspan="4">&nbsp;</td></tr>
  <tr><td colspan="4" class="formSectionHeaderTitle">${uiLabelMap.OpentapsAddress}</td></tr>
  <tr>
    <@displayTitleCell title=uiLabelMap.PartyToName />
    <@inputTextCell name="generalToName" />
    <@displayTitleCell title=uiLabelMap.PartyAttentionName />
    <@inputTextCell name="generalAttnName" />
  </tr>
  <tr>
    <@displayTitleCell title=uiLabelMap.PartyAddressLine1 />
    <@inputTextCell name="generalAddress1" />
    <@displayTitleCell title=uiLabelMap.PartyAddressLine2 />
    <@inputTextCell name="generalAddress2" />
  </tr>
  <tr>
    <@displayTitleCell title=uiLabelMap.PartyCity />
    <@inputTextCell name="generalCity" />
    <@displayTitleCell title=uiLabelMap.PartyState />
    <td><@inputState name="generalStateProvinceGeoId" countryInputName="generalCountryGeoId" /></td>
  </tr>
  <tr>
    <@displayTitleCell title=uiLabelMap.PartyZipCode />
    <@inputTextCell name="generalPostalCode" />
    <@displayTitleCell title=uiLabelMap.CommonCountry />
    <td><@inputCountry stateInputName="generalStateProvinceGeoId" name="generalCountryGeoId" /></td>
  </tr>
  
  <tr><td colspan="4">&nbsp;</td></tr>
  <tr><td colspan="4" class="formSectionHeaderTitle">${uiLabelMap.PartyDatosLegales}</td></tr>
  
  <tr>
  		<@displayTitleCell title=uiLabelMap.NumActaConstitutiva/>
  		<@inputTextCell name="numActaConstitutiva" id="numActaConstitutiva"/>  	
  		<@displayTitleCell title=uiLabelMap.NombreNotarioPublico/>
  		<@inputTextCell name="nombreNotarioPublico" id="nombreNotarioPublico"/>  		  
  </tr>
  <tr>
  		<@displayTitleCell title=uiLabelMap.NumNotaria/>
  		<@inputTextCell name="numNotaria" id="numNotaria"/>  	
  		<@displayTitleCell title=uiLabelMap.NombreApoderadoLegal/>
  		<@inputTextCell name="nombreApoderadoLegal" id="nombreApoderadoLegal"/>  		  
  </tr>
  <tr>
  		<@displayTitleCell title=uiLabelMap.TipoIdApoderadoLegal/>
  		<@inputTextCell name="tipoIdApoderadoLegal" id="tipoIdApoderadoLegal"/>  	
  		<@displayTitleCell title=uiLabelMap.NumIdApoderadoLegal/>
  		<@inputTextCell name="numIdApoderadoLegal" id="numIdApoderadoLegal"/>  		  
  </tr>
  <tr>
  		<@displayTitleCell title=uiLabelMap.NumTestimonioApoderado/>
  		<@inputTextCell name="numTestimonioApoderado" id="numTestimonioApoderado"/>  	
  		<@displayTitleCell title=uiLabelMap.NombreNotarioApoderado/>
  		<@inputTextCell name="nombreNotarioApoderado" id="nombreNotarioApoderado"/>  		  
  </tr>
  <tr>
  		<@displayTitleCell title=uiLabelMap.NumNotariaApoderado/>
  		<@inputTextCell name="numNotariaApoderado" id="numNotariaApoderado"/>  	
  		<@displayTitleCell title=uiLabelMap.CurpApoderadoLegal/>
  		<@inputTextCell name="curpApoderadoLegal" id="curpApoderadoLegal"/>  		  
  </tr>
  <tr>
  		<@displayTitleCell title=uiLabelMap.NombreRepLegal/>
  		<@inputTextCell name="nombreRepLegal" id="nombreRepLegal"/>  	
  		<@displayTitleCell title=uiLabelMap.TipoIdRepLegal/>
  		<@inputTextCell name="tipoIdRepLegal" id="tipoIdRepLegal"/>  		  
  </tr>
  <tr>
  		<@displayTitleCell title=uiLabelMap.NumIdRepLegal/>
  		<@inputTextCell name="numIdRepLegal" id="numIdRepLegal"/>  	
  		<@displayTitleCell title=uiLabelMap.NumTestimonioRepLegal/>
  		<@inputTextCell name="numTestimonioRepLegal" id="numTestimonioRepLegal"/>  		  
  </tr>
  <tr>
  		<@displayTitleCell title=uiLabelMap.NombreNotarioRepLegal/>
  		<@inputTextCell name="nombreNotarioRepLegal" id="nombreNotarioRepLegal"/>  	
  		<@displayTitleCell title=uiLabelMap.NumNotariaRepLegal/>
  		<@inputTextCell name="numNotariaRepLegal" id="numNotariaRepLegal"/>  		  
  </tr>
  <tr>
  		<@displayTitleCell title=uiLabelMap.CurpRepLegal/>
  		<@inputTextCell name="curpRepLegal" id="curpRepLegal"/>  	
  		<@displayTitleCell title=uiLabelMap.NombreAdministrador/>
  		<@inputTextCell name="nombreAdministrador" id="nombreAdministrador"/>  		  
  </tr>
  <tr>
  		<@displayTitleCell title=uiLabelMap.TipoIdAdministrador/>
  		<@inputTextCell name="tipoIdAdministrador" id="tipoIdAdministrador"/>  	
  		<@displayTitleCell title=uiLabelMap.NumIdAdministrador/>
  		<@inputTextCell name="numIdAdministrador" id="numIdAdministrador"/>  		  
  </tr>
  <tr>
  		<@displayTitleCell title=uiLabelMap.NumTestimonioAdministrador/>
  		<@inputTextCell name="numTestimonioAdministrador" id="numTestimonioAdministrador"/>  	
  		<@displayTitleCell title=uiLabelMap.NombreNotarioAdministrador/>
  		<@inputTextCell name="nombreNotarioAdministrador" id="nombreNotarioAdministrador"/>  		  
  </tr>
  <tr>
  		<@displayTitleCell title=uiLabelMap.NumNotariaAdministrador/>
  		<@inputTextCell name="numNotariaAdministrador" id="numNotariaAdministrador"/>  	
  		<@displayTitleCell title=uiLabelMap.CurpAdministrador/>
  		<@inputTextCell name="curpAdministrador" id="curpAdministrador"/>  		  
  </tr>

  <@inputHidden name="forceComplete" value="N"/>
  <#if requestAttributes.duplicateSuppliersWithName?exists>
  <tr> 
  <td >&nbsp;</td>
  
  <td colspan=2>
    <@inputSubmit title=uiLabelMap.PurchCreateSupplier />
    &nbsp;
    <@submitFormLinkConfirm form="createSupplierParty" text=uiLabelMap.PurchCreateSupplierIgnoreDuplicate forceComplete="Y"/>
  </td>
  </tr>
  <#else>
  <@inputSubmitRow title=uiLabelMap.PurchCreateSupplier />
  </#if>


</table>

</form>

