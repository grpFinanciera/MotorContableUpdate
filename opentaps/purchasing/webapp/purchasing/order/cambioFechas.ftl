<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<#if !orderItem?has_content>
<form name="cambioFechasForm" method="post" action="">
  <table class="twoColumnForm">
    <tbody>
      <tr>
      <@displayTitleCell title=uiLabelMap.PurchApprovedOrders />
      <@inputTextCell name="orderId" maxlength="50" size=10 default=""/>
      </tr>
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
</#if>

<#if orderItem?has_content>
<@frameSection title="">
<form name="fechasEntregaPro" method="POST" action="">  
	<table border="0" width="100%">

	<thead>		
		<tr>
			<th>${uiLabelMap.PurchasingProduct}</th>
			<th>${uiLabelMap.PurchasingProductName}</th>
			<th>${uiLabelMap.FinancialsEndDate}</th>
		</tr>
	</thead>
	<tbody>
		<#list orderItem as row>
				<@inputHidden name="orderId" value="${orderId}"/>
						<tr class="${tableRowClass(row_index)}">
						    <@displayCell text=row.productId/>	
							<@displayCell text=row.itemDescription/>
                        	<@inputDateCell name="${row.orderItemSeqId}" default=row.fechaEntrega?default("") form="fechasEntregaPro"/>
						</tr>
						<tr>
              			<td colspan="6">
              				<@flexArea targetId="ClavePresupuestal${row.orderItemSeqId}" title=uiLabelMap.ClavePresupuestal controlStyle="font-weight: bold;">
              				<table  border="0" cellpadding="1" cellspacing="0">
								<@clavesPresupDisplay tags=tagTypes entity=row partyExternal=row.acctgTagEnumIdAdmin/>
							</table>
							</@flexArea>
						</td>
			  		</tr>
			<tr>
				<td>
					&nbsp;
				</td>
			</tr>
		</#list>
	</tbody>
	<table>
      	<td colspan="1"><div id="buttonsBar"><@inputSubmit title=uiLabelMap.CommonUpdate /></div></td>
     </table>
	</table>
</form>
</@frameSection>
</#if>
