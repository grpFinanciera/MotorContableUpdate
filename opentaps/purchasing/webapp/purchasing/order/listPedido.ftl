<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<@paginate name="listPedido" list=orderListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <#assign hostname = request.getServerName() /></br>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.AccountingNumberOfOrders orderBy="createdStamp DESC"/>
                <@headerCell title=uiLabelMap.OrderDate orderBy="entryDate"/>
                <@headerCell title=uiLabelMap.OrderOrderName orderBy="orderName"/>
                <@headerCell title=uiLabelMap.PartySupplier orderBy="groupName"/>
                <@headerCell title=uiLabelMap.PurchasingStatus orderBy="description"/>
                <@headerCell title=uiLabelMap.OrderAmount orderBy="grandTotal"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.orderId href="orderview?orderId=${row.orderId}"/>
                <@displayDateCell date=row.entryDate format="DATE_TIME"/>
                <@displayCell text=row.orderName/>
                <@displayCell text=row.groupName/>
                <@displayCell text=row.description/>
                <@displayCurrencyCell amount=row.grandTotal currencyUomId=row.currencyUom class="textright"/>
           </#list>
        </table>
    </#noparse>
</@paginate>
