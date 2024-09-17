<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<@form name="consultarPenalizacion" url="" id="consultarPenalizacion" >	
<table>	
	<@inputHidden id="performFind" name="performFind" value="Y" />
	<@inputTextRow name="penaId" title=uiLabelMap.PurchNumPenalizacion size=20 maxlength=20 />
	<@inputTextRow name="ordenCobroId" title=uiLabelMap.PurchOrdenCobro size=20 maxlength=20 />
	<@inputTextRow name="aplicaPenaId" title=uiLabelMap.PurchNumPenalizacionDetalle size=20 maxlength=20 />
	<@inputSubmitRow title=uiLabelMap.CommonFind />
</table>
</@form>
<@paginate name="listMapInventoryItem" list=penasAplicadasList rememberPage=false >
	<#noparse>
	<@navigationHeader/>
	<table class="listTable">
		<tr class="listTableHeader">
			<@headerCell title=uiLabelMap.PurchNumPenalizacion orderBy="penaId"/>
			<@headerCell title=uiLabelMap.PurchOrdenCobro orderBy="invoiceId"/>
			<@headerCell title=uiLabelMap.PrchTipoPena orderBy="penaDeductivaFlag"/>
			<@headerCell title=uiLabelMap.PurchBaseCalculo orderBy="base"/>
			<@headerCell title=uiLabelMap.PurchNumPenalizacionDetalle orderBy="aplicaPenaId"/>
			<@headerCell title=uiLabelMap.PurchNumPurchasingPedido orderBy="orderId"/>
			<@headerCell title=uiLabelMap.PurchNumPurchasingPedidoDetalle orderBy="orderItemSeqId"/>
			<@headerCell title=uiLabelMap.PurchasingCantidadProductos orderBy="cantidad"/>
			<@headerCell title=uiLabelMap.PurchMontoPena orderBy="monto" />
			<@headerCell title=uiLabelMap.CommonDescription orderBy="descripcion" />
			<@headerCell title=uiLabelMap.ProductName orderBy="productName" />
			<@headerCell title=uiLabelMap.CommonStatus orderBy="estatus" />	
		</tr>	
		<#list pageRows as row>
		<tr class="${tableRowClass(row_index)}">
			<@displayCell text=row.penaId! />
			<@displayCell text=row.invoiceId! />
			<@displayCell text=row.penaDeductivaFlag! />
			<@displayCell text=row.base! />
			<@displayCell text=row.aplicaPenaId! />
			<@displayCell text=row.orderId! />
			<@displayCell text=row.orderItemSeqId! />
			<@displayCell text=row.cantidad! />
			<@displayCurrencyCell  amount=row.monto!0 currencyUomId=row.moneda />	
			<@displayCell text=row.descripcion! />
			<@displayCell text=row.productName! />
			<@displayCell text=row.estatus! />
        </tr>
        </#list>	
	</table>
	</#noparse>
</@paginate>

