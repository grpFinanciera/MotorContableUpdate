<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>



<@paginate name="listDevoluciones" list=devolucionesListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.DevolucionId orderBy="createdStamp"/>
                <@headerCell title=uiLabelMap.OrderId orderBy="orderId"/>
                <@headerCell title=uiLabelMap.OrderItemSeqId orderBy="orderItemSeqId"/>
                <@headerCell title=uiLabelMap.Evento orderBy="acctgTransTypeId"/>
                <@headerCell title=uiLabelMap.FechaContable orderBy="fechaContable"/>
                <@headerCell title=uiLabelMap.OrdenCobroId orderBy="invoiceId"/>
                <@headerCell title=uiLabelMap.ProductId orderBy="productId"/>
                <@headerCell title=uiLabelMap.Cantidad orderBy="cantidad"/>                               
                <@headerCell title=uiLabelMap.PrecioUnitario orderBy="precioUnitario"/>
                <@headerCell title=uiLabelMap.Total orderBy="precioUnitario"/>
                <@headerCell title=uiLabelMap.Poliza orderBy="poliza"/>                
            </tr>
            
            <#list pageRows as row>     
	            <tr class="${tableRowClass(row_index)}">	                
	                <@displayCell text=row.devolucionId/>
	                <@displayCell text=row.orderId/>
	                <@displayCell text=row.orderItemSeqId/>
	                <@displayCell text=row.acctgTransTypeId/>
	                <@displayDateCell date=row.fechaContable?if_exists format="DATE_ONLY" />
	                <@displayCell text=row.invoiceId?if_exists/>
	                <@displayCell text=row.productId/>
	                <@displayCell text=row.cantidad/>
	                <@displayCurrencyCell currencyUomId=row.currencyUomId />
	                <#if row.precioUnitario?exists>
	                	<#if row.cantidad?exists>
	                		<@displayCurrencyCell currencyUomId=row.currencyUomId amount=(row.precioUnitario * row.cantidad)/>
	                	<#else>	
	                		<@displayCell text=""/>
	                	</#if>	
	                </#if>
	                <#if row.acctgTransId?exists>
	                	<@displayLinkCell text=row.poliza href="viewAcctgTrans?acctgTransId=${row.acctgTransId?if_exists}"/>
	               	</#if>                
	            </tr>       
            </#list>
        </table>
    </#noparse>
</@paginate>