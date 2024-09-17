<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<#if !documentosList?has_content>
<form name="documentosPendientesForm" method="post" action="">
  <table class="twoColumnForm">
    <tbody>
      <tr>

      ${uiLabelMap.PurchApprovedOrders} :<@inputLookup name="orderId" lookup="LookupPurchaseOrder" form="documentosPendientesForm" default=""/>
      </tr>
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
</#if>

<#if oH?has_content>
<form name="OrderHeader" method="POST" action="">  
	<table border="0" width="100%">

	<thead>		
		<tr>
			<th>${uiLabelMap.PurchNumPurchasingPedido}</th>
			<th>${uiLabelMap.PrchNombrePedido}</th>
			<th>${uiLabelMap.PurchBackOrdersReportBackorderDate}</th>
			<th>${uiLabelMap.PurchSuppliers}</th>
			<th>${uiLabelMap.PurchSupplierName}</th>
			<th>${uiLabelMap.ProcesoAdjudicacion}</th>
			<th>${uiLabelMap.PrchTipoPena}</th>
		</tr>
		<tr>
				<td>
					&nbsp;
				</td>
			</tr>
	</thead>
	<tbody>
		<#list oH as row>
						<tr class="${tableRowClass(row_index)}">
						    <@displayCell text=row.orderId?if_exists/>	
						    <@displayCell text=row.orderName?if_exists/>
						    <@displayCell text=row.orderDate?if_exists/>
						    <@displayCell text=row.billFromPartyId?if_exists/>
						    <@displayCell text=row.groupName?if_exists/>
						    <@displayCell text=row.tipoAdjudicacionId?if_exists/>
						    <@displayCell text=row.nombreAd?if_exists/>		
						</tr>
			<tr>
				<td>
					&nbsp;
				</td>
			</tr>
			<tr>
				<td>
					&nbsp;
				</td>
			</tr>
			<tr>
				<td>
					&nbsp;
				</td>
			</tr>
			<tr>
				<td>
					&nbsp;
				</td>
			</tr>
			<tr>
				<td>
					&nbsp;
				</td>
			</tr>
		</#list>
	</tbody>
</form>
</#if>

<#if documentosList?has_content>
<@frameSection title="">
<form name="docRequeridos" method="POST" action="">  
	<@inputHidden name="flagV" value="A"/>
	<table border="0" width="100%">

	<thead>		
		<tr>
			<th>${uiLabelMap.DocumentosRequeridos}</th>
			<th></th>
			<th>${uiLabelMap.UserC}</th>
			<th>${uiLabelMap.UserM}</th>
			<th>${uiLabelMap.FechaActualizacion}</th>
		</tr>
		<tr>
				<td>
					&nbsp;
				</td>
			</tr>
	</thead>
	<tbody>
		<#list documentosList as row>
				<@inputHidden name="orderId" value="${orderId}"/>
						<tr class="${tableRowClass(row_index)}">
						    <@displayCell text=row.descripcion?if_exists/>	
							<td><input type="checkbox" name="${row.docTipoAdjudicacionId}" value="Y" <#if "${row.flag?if_exists}" == "Y">checked="checked"</#if>/></td>
							<@displayCell text=row.userCr?if_exists/>
							<@displayCell text=row.userMo?if_exists/>
							<@displayCell text=row.fechaActualizacion?if_exists/>				
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
