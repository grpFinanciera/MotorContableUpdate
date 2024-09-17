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

<#macro importForm importService label sectionLabel submitLabel processed notProcessed reportHref reportLabel>
    <form name="${importService}Form" method="post" action="setServiceParameters">
      <@inputHidden name="SERVICE_NAME" value="${importService}"/>
      <@inputHidden name="POOL_NAME" value="pool"/>
      <@inputHidden name="sectionHeaderUiLabel" value="${sectionLabel}"/>
      <@displayCell text="${label}:"/>
      <@displayCell text="${processed}"/>
      <@displayCell text="${notProcessed}"/>
      <#if hasDIAdminPermissions?default(false)>
        <@inputSubmitCell title="${submitLabel}"/>
      </#if>
      <@displayLinkCell href="${reportHref}" text="${reportLabel}" class="buttontext"/>
    </form>
    
</#macro>

<table  class="headedTable">
  <tr class="header">
    <@displayCell text=uiLabelMap.DataImportImporting/>
    <@displayCell text=uiLabelMap.DataImportNumberProcessed/>
    <@displayCell text=uiLabelMap.DataImportNumberNotProcessed/>
    <#if hasFullPermissions?default(false)><td>&nbsp;</td></#if>
  </tr>
 <!-- <tr>
    <@importForm importService="importCustomers"
                 sectionLabel="DataImportImportCustomers"
                 label=uiLabelMap.FinancialsCustomers
                 submitLabel=uiLabelMap.DataImportImport
                 processed=customersProcessed notProcessed=customersNotProcessed
                 reportHref="setupReport?reportId=CUST_IMP&amp;sectionName=myHome"
                 reportLabel=uiLabelMap.OpentapsReport/>
  </tr>-->
  <tr>
    <@importForm importService="importSuppliers"
                 sectionLabel="DataImportImportSuppliers"
                 label=uiLabelMap.DataImportAuxiliar
                 submitLabel=uiLabelMap.DataImportImport
                 processed=suppliersProcessed notProcessed=suppliersNotProcessed
                 reportHref="setupReport?reportId=SUPPL_IMP&amp;sectionName=myHome"
                 reportLabel=uiLabelMap.OpentapsReport/>
  </tr>
  <tr>
    <@importForm importService="importProducts"
                 sectionLabel="DataImportImportProducts"
                 label=uiLabelMap.ProductProducts
                 submitLabel=uiLabelMap.DataImportImport
                 processed=productsProcessed notProcessed=productsNotProcessed
                 reportHref="setupReport?reportId=PROD_IMP&amp;sectionName=myHome"
                 reportLabel=uiLabelMap.OpentapsReport/>
  </tr>
  <tr>
    <@importForm importService="importProductInventory"
                 sectionLabel="DataImportImportInventory"
                 label=uiLabelMap.ProductInventoryItems
                 submitLabel=uiLabelMap.DataImportImport
                 processed=inventoryProcessed notProcessed=inventoryNotProcessed
                 reportHref="setupReport?reportId=INVENT_IMP&amp;sectionName=myHome"
                 reportLabel=uiLabelMap.OpentapsReport/>
  </tr>
  <tr>
    <@importForm importService="importGlAccounts"
                 sectionLabel="DataImportImportGlAccounts"
                 label=uiLabelMap.DataImportGlAccounts
                 submitLabel=uiLabelMap.DataImportImport
                 processed=glAccountsProcessed notProcessed=glAccountsNotProcessed
                 reportHref="setupReport?reportId=GL_ACCTS_IMP&amp;sectionName=myHome"
                 reportLabel=uiLabelMap.OpentapsReport/>
  </tr>
  <tr>
    <@importForm importService="importTag"
                 sectionLabel="DataImportImportTag"
                 label=uiLabelMap.DataImportTag
                 submitLabel=uiLabelMap.DataImportImport
                 processed=tagProcessed notProcessed=tagNotProcessed
                 reportHref="setupReport?reportId=TAG_IMP&amp;sectionName=myHome"
                 reportLabel=uiLabelMap.OpentapsReport/>
  </tr>
  <tr>
    <@importForm importService="importParty"
                 sectionLabel="DataImportImportParty"
                 label="Clasificaci&oacute;n administrativa"
                 submitLabel=uiLabelMap.DataImportImport
                 processed=partyProcessed notProcessed=partyNotProcessed
                 reportHref="setupReport?reportId=CLAS_ADMON_IMP&amp;sectionName=myHome"
                 reportLabel=uiLabelMap.OpentapsReport/>
  </tr>
  <!--<tr>
    <@importForm importService="importOrders"
                 sectionLabel="DataImportImportOrders"
                 label=uiLabelMap.DataImportOrderLines
                 submitLabel=uiLabelMap.DataImportImport
                 processed=orderHeadersProcessed notProcessed=orderHeadersNotProcessed
                 reportHref="setupReport?reportId=ORDER_H_IMP&amp;sectionName=myHome"
                 reportLabel=uiLabelMap.OpentapsReport/>
  </tr>
  <tr>
    <@importForm importService="importPagos"
                 sectionLabel="DataImportImportPagos"
                 label=uiLabelMap.PagosPayment
                 submitLabel=uiLabelMap.DataImportImport
                 processed=pagosProcessed notProcessed=pagosNotProcessed
                 reportHref="setupReport?reportId=PAGO_IMP&amp;sectionName=myHome"
                 reportLabel=uiLabelMap.OpentapsReport/>
  </tr>  -->
 <!-- <tr>
    <@displayCell text="${uiLabelMap.DataImportOrderItemLines}:"/>
    <@displayCell text="${orderItemsProcessed}"/>
    <@displayCell text="${orderItemsNotProcessed}"/>
    <td>&nbsp;</td>
    <@displayLinkCell href="setupReport?reportId=ORDER_I_IMP&amp;sectionName=myHome" text=uiLabelMap.OpentapsReport class="buttontext" />
  </tr> -->
  <tr>
    <@displayCell text="${uiLabelMap.MatrizEgreso}:"/>
    <@displayCell text="${matrizEgresoProcessed}"/>
    <@displayCell text="${matrizEgresoNotProcessed}"/>
    <td>&nbsp;</td>
    <@displayLinkCell href="setupReport?reportId=MATRIZ_EGRESO&amp;sectionName=myHome" text=uiLabelMap.OpentapsReport class="buttontext" />
  </tr>
  <tr>
    <@displayCell text="${uiLabelMap.MatrizIngreso}:"/>
    <@displayCell text="${matrizIngresoProcessed}"/>
    <@displayCell text="${matrizIngresoNotProcessed}"/>
    <td>&nbsp;</td>
    <@displayLinkCell href="setupReport?reportId=MATRIZ_INGRESO&amp;sectionName=myHome" text=uiLabelMap.OpentapsReport class="buttontext" />
  </tr>
  <!--
  <tr>
    <@displayCell text="${uiLabelMap.MatrizConcepto}:"/>
    <@displayCell text="${matrizConceptoProcessed}"/>
    <@displayCell text="${matrizConceptoNotProcessed}"/>
    <td>&nbsp;</td>
    <@displayLinkCell href="setupReport?reportId=MATRIZ_CONCEPTO&amp;sectionName=myHome" text=uiLabelMap.OpentapsReport class="buttontext" />
  </tr>-->
  <tr>
    <@importForm importService="importPresupuestoInicial"
                 sectionLabel="DataImportImportPresupuestoInicial"
                 label=uiLabelMap.PresupuestoInicial
                 submitLabel=uiLabelMap.DataImportImport
                 processed=presupuestoInicialProcessed notProcessed=presupuestoInicialNotProcessed
                 reportHref="setupReport?reportId=PRESUPUESTO_INICIAL&amp;sectionName=myHome"
                 reportLabel=uiLabelMap.OpentapsReport/>
  </tr>
<!-- <tr>
    <@importForm importService="importOperacionIngresoEgreso"
                 sectionLabel="DataImportImportIEOperacionDiaria"
                 label=uiLabelMap.IngresoEgreso
                 submitLabel=uiLabelMap.DataImportImport
                 processed=ingresoEgresoProcessed notProcessed=ingresoEgresoNotProcessed
                 reportHref="setupReport?reportId=INGRESO_EGRESO&amp;sectionName=myHome"
                 reportLabel=uiLabelMap.OpentapsReport/>
  </tr> -->
  <!--<tr>
    <@importForm importService="importOperacionDiaria"
                 sectionLabel="DataImportImportOperacionDiaria"
                 label=uiLabelMap.OperacionDiaria
                 submitLabel=uiLabelMap.DataImportImport
                 processed=operacionDiariaProcessed notProcessed=operacionDiariaNotProcessed
                 reportHref="setupReport?reportId=OPERACION_DIARIA&amp;sectionName=myHome"
                 reportLabel=uiLabelMap.OpentapsReport/>
  </tr>-->
  <!--<tr>
  	<@importForm importService="importOrdenesPago"
  				sectionLabel="DataImportPurchaseOrder"
  				label=uiLabelMap.DataImportPurchaseOrder 
  				submitLabel=uiLabelMap.DataImportImport
  				processed=ordenesPagoProcessed notProcessed=ordenesPagoNotProcessed
  				reportHref="setupReport?reportId=ORDENES_PAGO&amp;sectionName=myHome"
  				reportLabel=uiLabelMap.OpentapsReport />
  </tr>
  <tr>
  	<@importForm importService="importOrdenesCobro"
  				sectionLabel="DataImportSalesOrder"
  				label=uiLabelMap.DataImportSalesOrder 
  				submitLabel=uiLabelMap.DataImportImport
  				processed=ordenesCobroProcessed notProcessed=ordenesCobroNotProcessed
  				reportHref="setupReport?reportId=ORDENES_COBRO&amp;sectionName=myHome"
  				reportLabel=uiLabelMap.OpentapsReport />
  </tr>
  <tr>-->
  <@importForm importService="importSaldoInicialAuxiliar"
                 sectionLabel="DataImportSaldosInicialesAuxiliares"
                 label=uiLabelMap.DataImportSaldosInicialesAuxiliares
                 submitLabel=uiLabelMap.DataImportImport
                 processed=saldosInicialAuxProcessed notProcessed=saldosInicialAuxNotProcessed
                 reportHref="setupReport?reportId=SALDO_INICIAL_AUX&amp;sectionName=myHome"                 
                 reportLabel=uiLabelMap.OpentapsReport/>
  </tr> 
  <tr>
  <@importForm importService="importUser"
                 sectionLabel="DataImportUser"
                 label=uiLabelMap.DataImportUser
                 submitLabel=uiLabelMap.DataImportImport
                 processed=userProcessed notProcessed=userNotProcessed
                 reportHref="setupReport?reportId=USUARIOS&amp;sectionName=myHome"
                 reportLabel=uiLabelMap.OpentapsReport/>
  </tr> 
  <!--<tr>
  <@importForm importService="importAfectacionEgreso"
                 sectionLabel="DataImportAfectacionEgreso"
                 label=uiLabelMap.DataImportAfectacionEgreso
                 submitLabel=uiLabelMap.DataImportImport
                 processed=afectacionEgresoProcessed notProcessed=afectacionEgresoNotProcessed
                 reportHref="setupReport?reportId=AFECTACION_EGRESO&amp;sectionName=myHome"
                 reportLabel=uiLabelMap.OpentapsReport/>
  </tr>-->
  <tr>
  <@importForm   importService="importFixedAsset"
                 sectionLabel="DataImportFixedAsset"
                 label=uiLabelMap.DataImportFixedAsset
                 submitLabel=uiLabelMap.DataImportImport
                 processed=fixedAssetProcessed notProcessed=fixedAssetNotProcessed
                 reportHref="setupReport?reportId=ACTIVOFIJO&amp;sectionName=myHome"
                 reportLabel=uiLabelMap.OpentapsReport/>
  </tr>
  <tr>
  <@importForm   importService="importLevantaActFijo"
                 sectionLabel="DataImportLevantaFixedAsset"
                 label=uiLabelMap.DataImportLevantaFixedAsset
                 submitLabel=uiLabelMap.DataImportImport
                 processed=levantaFixedAssetProcessed notProcessed=levantaFixedAssetNotProcessed
                 reportHref="setupReport?reportId=ACTIVOFIJO&amp;sectionName=myHome"
                 reportLabel=uiLabelMap.OpentapsReport/>
  </tr>
  <tr>
  <@importForm   importService="importValidacionPresupuestal"
                 sectionLabel="DataImportValidacionPresupuestal"
                 label=uiLabelMap.DataImportValidacionPresupuestal
                 submitLabel=uiLabelMap.DataImportImport
                 processed=validacionPresupuestalProcessed notProcessed=validacionPresupuestalNotProcessed
                 reportHref="setupReport?reportId=VALPRES&amp;sectionName=myHome"
                 reportLabel=uiLabelMap.OpentapsReport/>
  </tr>
  <tr>
    <@importForm importService="importCompromisoDevengoNomina"
                 sectionLabel="DataImportImportCompDevNomina"
                 label=uiLabelMap.NominaCDP
                 submitLabel=uiLabelMap.DataImportImport
                 processed=compDevNomPresProcessed notProcessed=compDevNomPresNotProcessed
                 reportHref="setupReport?reportId=COMP_DEV_NOMINA&amp;sectionName=myHome"
                 reportLabel=uiLabelMap.OpentapsReport/>
  </tr>
  <tr>
    <@displayCell text="${uiLabelMap.NominaCDC}:"/>
    <@displayCell text="${compDevNomContProcessed}"/>
    <@displayCell text="${compDevNomContNotProcessed}"/>
    <td>&nbsp;</td>
    <@displayLinkCell href="setupReport?reportId=COMP_DEV_NOMINA_CONT&amp;sectionName=myHome" text=uiLabelMap.OpentapsReport class="buttontext" />
  </tr>
  <tr>
    <@importForm importService="importEjercidoNomina"
                 sectionLabel="DataImportImportEjerNomina"
                 label=uiLabelMap.NominaEP
                 submitLabel=uiLabelMap.DataImportImport
                 processed=ejerNomPresProcessed notProcessed=ejerNomPresNotProcessed
                 reportHref="setupReport?reportId=EJER_NOMINA&amp;sectionName=myHome"
                 reportLabel=uiLabelMap.OpentapsReport/>
  </tr>
  <tr>
    <@displayCell text="${uiLabelMap.NominaEC}:"/>
    <@displayCell text="${ejerNomContProcessed}"/>
    <@displayCell text="${ejerNomContNotProcessed}"/>
    <td>&nbsp;</td>
    <@displayLinkCell href="setupReport?reportId=EJER_NOMINA_CONT&amp;sectionName=myHome" text=uiLabelMap.OpentapsReport class="buttontext" />
  </tr>
  <tr>
    <@importForm importService="importOperacionPatrimonial"
                 sectionLabel="DataImportImportOperacionPatrimonial"
                 label=uiLabelMap.OperacionPatrimonial
                 submitLabel=uiLabelMap.DataImportImport
                 processed=operacionPatrimonialProcessed notProcessed=operacionPatrimonialNotProcessed
                 reportHref="setupReport?reportId=OPER_PATRIMONIAL&amp;sectionName=myHome"
                 reportLabel=uiLabelMap.OpentapsReport/>
  </tr>
  <tr>
    <@importForm importService="importIngreso"
                 sectionLabel="DataImportImportIngreso"
                 label=uiLabelMap.Ingreso
                 submitLabel=uiLabelMap.DataImportImport
                 processed=ingresoProcessed notProcessed=ingresoNotProcessed
                 reportHref="setupReport?reportId=INGRESO&amp;sectionName=myHome"
                 reportLabel=uiLabelMap.OpentapsReport/>
  </tr>
  <tr>
    <@displayCell text="${uiLabelMap.DevIng}:"/>
    <@displayCell text="${devIngProcessed}"/>
    <@displayCell text="${devIngNotProcessed}"/>
    <td>&nbsp;</td>
    <@displayLinkCell href="setupReport?reportId=DEV_ING&amp;sectionName=myHome" text=uiLabelMap.OpentapsReport class="buttontext" />
  </tr>
  <tr>
    <@displayCell text="${uiLabelMap.RecIng}:"/>
    <@displayCell text="${recIngProcessed}"/>
    <@displayCell text="${recIngNotProcessed}"/>
    <td>&nbsp;</td>
    <@displayLinkCell href="setupReport?reportId=REC_ING&amp;sectionName=myHome" text=uiLabelMap.OpentapsReport class="buttontext" />
  </tr> 
  <tr>
  <@importForm importService="importRequisicion"
                 sectionLabel="DataImportDetalleRequisicion"
                 label=uiLabelMap.DataImportDetalleRequisicion
                 submitLabel=uiLabelMap.DataImportImport
                 processed=requisicionProcessed notProcessed=requisicionNotProcessed
                 reportHref="setupReport?reportId=REQUISICION&amp;sectionName=myHome"
                 reportLabel=uiLabelMap.OpentapsReport/>
  </tr> 
  <tr>
  <@importForm importService="importCompensada"
                 sectionLabel="DataImportAfectacionCompensada"
                 label=uiLabelMap.DataImportAfectacionCompensada
                 submitLabel=uiLabelMap.DataImportImport
                 processed=compensadaProcessed notProcessed=compensadaNotProcessed
                 reportHref="setupReport?reportId=COMPENSADA&amp;sectionName=myHome"
                 reportLabel=uiLabelMap.OpentapsReport/>
  </tr>
  <tr>
  <@importForm importService="importHistorialBienes"
                 sectionLabel="DataImportHistorialBienes"
                 label=uiLabelMap.DataImportHistorialBienes
                 submitLabel=uiLabelMap.DataImportImport
                 processed=historialBienesProcessed notProcessed=historialBienesNotProcessed
                 reportHref="setupReport?reportId=HISTORIALBIENES&amp;sectionName=myHome"
                 reportLabel=uiLabelMap.OpentapsReport/>
  </tr> 
  <tr>
  <@importForm importService="importPagoExterno"
                 sectionLabel="DataImportPagoExterno"
                 label=uiLabelMap.PagoExterno
                 submitLabel=uiLabelMap.DataImportImport
                 processed=pagoExternoProcessed notProcessed=pagoExternoNotProcessed
                 reportHref="setupReport?reportId=PAGO_EXTERNO&amp;sectionName=myHome"
                 reportLabel=uiLabelMap.OpentapsReport/>
  </tr> 
</table>

<br/>

<#if hasDIAdminPermissions?default(false)>
  <@frameSection title=uiLabelMap.DataImportUploadFile>
    <form name="uploadFileAndImport" method="post" enctype="multipart/form-data" action="uploadFileForDataImport">
      <@inputHidden name="POOL_NAME" value="pool"/>
      <@inputHidden name="sectionHeaderUiLabel" value="DataImportImportFromFile"/>
      <table class="twoColumnForm">
        <@inputFileRow title=uiLabelMap.DataImportFileToImport name="uploadedFile" />
        <tr>
          <@displayTitleCell title=uiLabelMap.DataImportUploadFileFormat />
          <td>
            <select name="fileFormat" class="inputBox">
              <option value="EXCEL">${uiLabelMap.DataImportUploadFileFormatExcel}</option>
            </select>
          </td>
        </tr>
        <@inputSubmitRow title="${uiLabelMap.DataImportUpload}"/>
      </table>
    </form>
  </@frameSection>
</#if>

<!--<#if hasOrgConfigPermissions?default(false)>
  <@frameSection title=uiLabelMap.DataImportCopyLedgerSetup>
    <form name="copyOrganizationLedgerSetupForm" method="post" action="scheduleService">
      <@inputHidden name="SERVICE_NAME" value="copyOrganizationLedgerSetup"/>
      <@inputHidden name="POOL_NAME" value="pool"/>
      <@inputHidden name="_RUN_SYNC_" value="Y"/>
      <@inputHidden name="sectionHeaderUiLabel" value="${uiLabelMap.DataImportCopyLedgerSetup}"/>

      <table class="twoColumnForm">
       <@inputSelectRow name="templateOrganizationPartyId" title=uiLabelMap.DataImportFromOrganizationTemplate list=fromOrganizationTemplates key="partyId" ; option>
         ${option.groupName} (${option.partyId})
       </@inputSelectRow>        
       <@inputSelectRow name="organizationPartyId" title=uiLabelMap.DataImportToOrganization list=toOrganizations key="partyId" ; option>
         ${option.groupName} (${option.partyId})
       </@inputSelectRow>        
       <@inputSubmitRow title="${uiLabelMap.CommonCopy}"/>
      </table>
    </form>
  </@frameSection>
</#if>-->