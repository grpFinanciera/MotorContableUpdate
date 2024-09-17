<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<@frameSection title="reporteInventario">
<@form name="reporteInventario" url="reporteInventario">
	<table> 
		<#--<@inputSelectRow name="organizationPartyId" title=uiLabelMap.UnidadResponsable list=configuredOrganizations key="partyId" ; option>
        	${option.groupName} (${option.partyId})
      	</@inputSelectRow>-->
        <@inputDateRow title="Inventario hasta" name="thruDate" default=Static["org.ofbiz.base.util.UtilDateTime"].nowSqlDate()! />	
		<@inputSelectRow name="productTypeId" title="Tipo producto" list=productTypes key="productTypeId" ; product>
        	${product.description!product.productTypeId}
      	</@inputSelectRow>
		<@inputSelectRow name="facilityId" title="Almacen" required=false list=facilities key="facilityId" ; facility>
        	${facility.facilityName}
      	</@inputSelectRow>
		<@inputAutoCompleteProductRow name="productId" title=uiLabelMap.ProductProductId />
		<@displayTitleCell title=uiLabelMap.TipoReporte/>
		<@inputSelectHashCell hash= {"pdf":"PDF", "xls":"EXCEL"} id="tipoReporte" name="tipoReporte"/>
		<@inputButtonRow title=uiLabelMap.CommonPrint />
	</table>
</@form>
</@frameSection>