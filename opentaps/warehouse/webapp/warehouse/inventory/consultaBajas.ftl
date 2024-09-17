<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#if error?has_content>
 ${error}
</#if>
<form name="ConsultaBajas" >
  <table class="twoColumnForm">
    <tbody>
      <@inputHidden name="facilityId" value=facilityId />
      <@inputHidden name="motivoId" value="B" />
   	  <@inputAutoCompleteProductRow name="productId" title=uiLabelMap.ProductProductId />
      <@inputSelectRow title=uiLabelMap.WarehouseVarianceReasonB required=false list=varianceReasonsB  displayField="description" name="varianceReasonId" key="varianceReasonId" />
      <@inputDateRangeRow title=uiLabelMap.WarehouseOrderDateBaja fromName="fechaContableDesde" thruName="fechaContableHasta" />     
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>

<#if physicalListBuilder?has_content>
<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<@paginate name="paginaAcctgTransBajas" list=physicalListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.FormFieldTitle_numeroPoliza orderBy="agrupador"/>     
                <@headerCell title=uiLabelMap.WarehouseOrderDateBaja orderBy="physicalInventoryDate"/>           
                <@headerCell title=uiLabelMap.CommonDescription orderBy="generalComments"/>
                <@headerCell title=uiLabelMap.WarehouseQuantityQtyIssued orderBy="quantityOnHandDiff"/>
                <@headerCell title=uiLabelMap.WarehouseProduct orderBy="productId"/>
                <@headerCell title=uiLabelMap.WarehouseProductName orderBy="productName"/>
                <@headerCell title=uiLabelMap.FinancialsUnitCost orderBy="unitCost"/>
                <@headerCell title=uiLabelMap.Reporte orderBy="agrupador"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.agrupador href="viewAcctgTrans?acctgTransId=${row.acctgTransId}" />
                <@displayCell text=row.physicalInventoryDate/>				
                <@displayCell text=row.generalComments/>
                <@displayCell text=row.quantityOnHandDiff/>
                <@displayCell text=row.productId/>
                <@displayCell text=row.productName/>
                <@displayCurrencyCell amount=row.unitCost currencyUomId=row.currencyUomId class="textright"/>
                <@displayLinkCell text="Reporte" href="imprimeBajaInventario?acctgTransId=${row.acctgTransId}" />
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>
</#if>
