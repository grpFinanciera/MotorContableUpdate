<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<#if !listOrderItem?has_content>
<form name="cambioFechasForm" method="post" action="">
  <table class="twoColumnForm">
    <tbody>
      <tr>
      <@displayTitleCell title=uiLabelMap.PurchApprovedOrders />
      <@inputTextCell name="orderId" maxlength="20" size=10 default=""/>
      </tr>
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
</#if>

<#if listOrderItem?has_content>
<form name="fechasEntregaPro" method="POST" action="">
  
	<table border="0" width="100%">
	<thead>		
		<tr>
			<th>${uiLabelMap.PurchasingProduct}</th>
			<th>${uiLabelMap.PurchasingProductName}</th>
			<th>${uiLabelMap.CommonMonth}</th>
		</tr>
	</thead>
	<tbody>
		<@inputHidden name="orderId" value="${orderId}"/>
		<#list listOrderItem as row>
			<tr class="${tableRowClass(row_index)}">
				<@displayCell text=row.productId/>	
				<@displayCell text=row.itemDescription/>
				<@inputSelectCell name="customTimePeriodId_${row.orderItemSeqId}" id="customTimePeriodId_${row.orderItemSeqId}" 
					list=mapMesesXCiclo.get(cicloId!"")![] key="customTimePeriodId" displayField="periodNum" default=row.customTimePeriodId! />
			</tr>
		</#list>
	</tbody>
	<table>
      	<td colspan="1"><div id="buttonsBar"><@inputSubmit title=uiLabelMap.CommonUpdate /></div></td>
     </table>
	</table>
</form>
</#if>