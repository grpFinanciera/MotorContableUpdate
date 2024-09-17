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

  <@frameSection title=uiLabelMap.DataImportUploadFile>
	<#assign urlHost = request.getServerName()+":"+request.getServerPort()/>
	
	<form name="FirmaPDF" method="post" enctype="multipart/form-data" action="FirmaPDF">
	<@inputHidden name="urlHost" value=urlHost/>
	<table>
		<@displayTitleCell title=uiLabelMap.FinancialsReporteFirma/>
  		<@inputSelectCell list=reporteCatalogo?if_exists displayField="reporteNombre" name="reporteId" id="reporteId" required=false/>
		<@inputFileRow title=uiLabelMap.CommonPdf name="pdf" />
		<#--<@inputFileRow title=uiLabelMap.FinancialsCertificado name="certificado" />-->
		<@inputFileRow title=uiLabelMap.FinancialsClavePriv name="clavePrivada" />
		<tr>
			<@displayCell text=uiLabelMap.CommonPassword blockClass="titleCell" class="tableheadtext"/>
			<td><@inputText name="clave" password=true /></td>
		</tr>
		<@inputSubmitRow title=uiLabelMap.FinancialsFirmar />
	</table>
	</form>
  </@frameSection>
