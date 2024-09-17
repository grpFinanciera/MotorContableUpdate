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

<form name="updateSupplier" method="post" action="<@ofbizUrl>updateSupplier</@ofbizUrl>">
<table class="twoColumnForm">
  <@inputHidden name="partyId" value=partySummary.partyId />  
  <tr>
  	<@displayTitleCell title=uiLabelMap.CommonName/>
  	<@inputTextCell name="groupName" id="groupName" default=partySummary.groupName?if_exists/>
  </tr>
  <tr>
  	<@displayTitleCell title=uiLabelMap.OpentapsTaxAuthPartyId/>
  	<@inputTextCell name="federalTaxId" id="federalTaxId" default=partySummary.federalTaxId?if_exists/>
  </tr>
  <tr>
  	<@displayTitleCell title=uiLabelMap.OpentapsRegimen/>
  	<@inputSelectCell list=regimen?if_exists displayField="nombreRegimen" name="regimenId" id="regimenId" required=false default=partySummary.regimenId?if_exists/>
  </tr>
  <tr>
  	<@displayTitleCell title=uiLabelMap.OpentapsTamanioAuxiliar/>
  	<@inputSelectCell list=tamanioAuxiliar?if_exists displayField="nombreTamanioAuxiliar" name="tamanioAuxiliarId" id="tamanioAuxiliarId" required=false default=partySummary.tamanioAuxiliarId?if_exists/>
  </tr>
  <tr>
  	<@displayTitleCell title=uiLabelMap.OpentapsSectorEconomico/>
  	<@inputSelectCell list=sectorEconomico?if_exists displayField="nombreSectorEconomico" name="sectorEconomicoId" id="sectorEconomicoId" required=false default=partySummary.sectorEconomicoId?if_exists/>
  </tr>
  <tr>
  	<@displayTitleCell title=uiLabelMap.OpentapsOrigenCapital/>
  	<@inputSelectCell list=origenCapital?if_exists displayField="nombreOrigenCapital" name="origenCapitalId" id="origenCapitalId" required=false default=partySummary.origenCapitalId?if_exists/>
  </tr>
  <tr>
  	<@displayTitleCell title=uiLabelMap.Rupc/>
  	<@inputTextCell name="rupc" id="rupc" default=partySummary.rupc?if_exists/>
  </tr>
  <tr>
  	<@displayTitleCell title=uiLabelMap.GiroEmpresa/>
  	<@inputTextCell name="giroEmpresa" id="giroEmpresa" default=partySummary.giroEmpresa?if_exists/>
  </tr>
  <tr>
  	<@displayTitleCell title=uiLabelMap.NacionalExtranjero/>
  	<@inputSelectCell list=nacionalExtList?if_exists displayField="nacionalExtranjero" name="nacionalExtranjero" id="nacionalExtranjero" required=false default=partySummary.nacionalExtranjero?if_exists/>
  </tr>
  <tr>
  		<@displayTitleCell title=uiLabelMap.NumActaConstitutiva/>
  		<@inputTextCell name="numActaConstitutiva" id="numActaConstitutiva" default=partySummary.numActaConstitutiva?if_exists/>  	
  		<@displayTitleCell title=uiLabelMap.NombreNotarioPublico/>
  		<@inputTextCell name="nombreNotarioPublico" id="nombreNotarioPublico" default=partySummary.nombreNotarioPublico?if_exists/>  		  
  </tr>
  <tr>
  		<@displayTitleCell title=uiLabelMap.NumNotaria/>
  		<@inputTextCell name="numNotaria" id="numNotaria" default=partySummary.numNotaria?if_exists/>  	
  		<@displayTitleCell title=uiLabelMap.NombreApoderadoLegal/>
  		<@inputTextCell name="nombreApoderadoLegal" id="nombreApoderadoLegal" default=partySummary.nombreApoderadoLegal?if_exists/>  		  
  </tr>
  <tr>
  		<@displayTitleCell title=uiLabelMap.TipoIdApoderadoLegal/>
  		<@inputTextCell name="tipoIdApoderadoLegal" id="tipoIdApoderadoLegal" default=partySummary.tipoIdApoderadoLegal?if_exists/>  	
  		<@displayTitleCell title=uiLabelMap.NumIdApoderadoLegal/>
  		<@inputTextCell name="numIdApoderadoLegal" id="numIdApoderadoLegal" default=partySummary.numIdApoderadoLegal?if_exists/>  		  
  </tr>
  <tr>
  		<@displayTitleCell title=uiLabelMap.NumTestimonioApoderado/>
  		<@inputTextCell name="numTestimonioApoderado" id="numTestimonioApoderado" default=partySummary.numTestimonioApoderado?if_exists/>  	
  		<@displayTitleCell title=uiLabelMap.NombreNotarioApoderado/>
  		<@inputTextCell name="nombreNotarioApoderado" id="nombreNotarioApoderado" default=partySummary.nombreNotarioApoderado?if_exists/>  		  
  </tr>
  <tr>
  		<@displayTitleCell title=uiLabelMap.NumNotariaApoderado/>
  		<@inputTextCell name="numNotariaApoderado" id="numNotariaApoderado" default=partySummary.numNotariaApoderado?if_exists/>  	
  		<@displayTitleCell title=uiLabelMap.CurpApoderadoLegal/>
  		<@inputTextCell name="curpApoderadoLegal" id="curpApoderadoLegal" default=partySummary.curpApoderadoLegal?if_exists/>  		  
  </tr>
  <tr>
  		<@displayTitleCell title=uiLabelMap.NombreRepLegal/>
  		<@inputTextCell name="nombreRepLegal" id="nombreRepLegal" default=partySummary.nombreRepLegal?if_exists/>  	
  		<@displayTitleCell title=uiLabelMap.TipoIdRepLegal/>
  		<@inputTextCell name="tipoIdRepLegal" id="tipoIdRepLegal" default=partySummary.tipoIdRepLegal?if_exists/>  		  
  </tr>
  <tr>
  		<@displayTitleCell title=uiLabelMap.NumIdRepLegal/>
  		<@inputTextCell name="numIdRepLegal" id="numIdRepLegal" default=partySummary.numIdRepLegal?if_exists/>  	
  		<@displayTitleCell title=uiLabelMap.NumTestimonioRepLegal/>
  		<@inputTextCell name="numTestimonioRepLegal" id="numTestimonioRepLegal" default=partySummary.numTestimonioRepLegal?if_exists/>  		  
  </tr>
  <tr>
  		<@displayTitleCell title=uiLabelMap.NombreNotarioRepLegal/>
  		<@inputTextCell name="nombreNotarioRepLegal" id="nombreNotarioRepLegal" default=partySummary.nombreNotarioRepLegal?if_exists/>  	
  		<@displayTitleCell title=uiLabelMap.NumNotariaRepLegal/>
  		<@inputTextCell name="numNotariaRepLegal" id="numNotariaRepLegal" default=partySummary.numNotariaRepLegal?if_exists/>  		  
  </tr>
  <tr>
  		<@displayTitleCell title=uiLabelMap.CurpRepLegal/>
  		<@inputTextCell name="curpRepLegal" id="curpRepLegal" default=partySummary.curpRepLegal?if_exists/>  	
  		<@displayTitleCell title=uiLabelMap.NombreAdministrador/>
  		<@inputTextCell name="nombreAdministrador" id="nombreAdministrador" default=partySummary.nombreAdministrador?if_exists/>  		  
  </tr>
  <tr>
  		<@displayTitleCell title=uiLabelMap.TipoIdAdministrador/>
  		<@inputTextCell name="tipoIdAdministrador" id="tipoIdAdministrador" default=partySummary.tipoIdAdministrador?if_exists/>  	
  		<@displayTitleCell title=uiLabelMap.NumIdAdministrador/>
  		<@inputTextCell name="numIdAdministrador" id="numIdAdministrador" default=partySummary.numIdAdministrador?if_exists/>  		  
  </tr>
  <tr>
  		<@displayTitleCell title=uiLabelMap.NumTestimonioAdministrador/>
  		<@inputTextCell name="numTestimonioAdministrador" id="numTestimonioAdministrador" default=partySummary.numTestimonioAdministrador?if_exists/>  	
  		<@displayTitleCell title=uiLabelMap.NombreNotarioAdministrador/>
  		<@inputTextCell name="nombreNotarioAdministrador" id="nombreNotarioAdministrador" default=partySummary.nombreNotarioAdministrador?if_exists/>  		  
  </tr>
  <tr>
  		<@displayTitleCell title=uiLabelMap.NumNotariaAdministrador/>
  		<@inputTextCell name="numNotariaAdministrador" id="numNotariaAdministrador" default=partySummary.numNotariaAdministrador?if_exists/>  	
  		<@displayTitleCell title=uiLabelMap.CurpAdministrador/>
  		<@inputTextCell name="curpAdministrador" id="curpAdministrador" default=partySummary.curpAdministrador?if_exists/>  		  
  </tr>
  
  
  <@inputSubmitRow title=uiLabelMap.CommonUpdate />
</table>
</form>

