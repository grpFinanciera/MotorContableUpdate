<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#if PolizaPedido?has_content>
	<#list PolizaPedido as PolizaPedidoInterno>
		<#assign acctgTransId = PolizaPedidoInterno.acctgTransId!/>
		<#assign poliza = PolizaPedidoInterno.poliza!/>
			<@form name="verPoliza${acctgTransId}" url="viewAcctgTrans?acctgTransId=${acctgTransId}"/>
				<#assign verPolizaAction>${verPolizaAction!}<@actionForm form="verPoliza${acctgTransId}" text=poliza! /></#assign> 
			<@form name="imprimeSutirPedido${acctgTransId}" url="imprimeSutirPedido?acctgTransId=${acctgTransId}&pedidoInternoId=${PedidoInterno.pedidoInternoId!}"/>
				<#assign imprimeReporteAction>${imprimeReporteAction!}<@actionForm form="imprimeSutirPedido${acctgTransId}" text=poliza! /></#assign>
	</#list>
</#if>

<#if verPolizaAction?has_content>
	<#assign extraOptions>
    <@selectActionForm name="verPoliza" prompt=uiLabelMap.FinancialVerPoliza>
    	${verPolizaAction?if_exists}
	</@selectActionForm>
	</#assign>
</#if>

<#if imprimeReporteAction?has_content>
	<#assign extraOptions>${extraOptions!}
    <@selectActionForm name="imprimirReporte" prompt=uiLabelMap.CommonPrint>
    	${imprimeReporteAction?if_exists}
	</@selectActionForm>
	</#assign>
</#if>

<@frameSectionHeader title="" extra=extraOptions! />
<form name="SurtirPedidoParcialmente" method="POST" action="<@ofbizUrl>surtirPedidoParcialmente?facilityId=${parameters.facilityId}</@ofbizUrl>">
  <table class="twoColumnForm">
    <tbody>
    	<@inputHidden name="pedidoInternoId" value=PedidoInterno.pedidoInternoId! />
    	<@inputHidden name="organizationPartyId" value=PedidoInterno.organizationPartyId! />
		<@inputHidden name="almacenId" value=PedidoInterno.almacenId! />
		<@displayRow title=uiLabelMap.WarehousePedidoInterno text=PedidoInterno.pedidoInternoId! />
		<@displayRow title=uiLabelMap.CommonDescription text=PedidoInterno.descripcion! />
		<@displayRow title=uiLabelMap.WarehouseJustification text=PedidoInterno.justificacion! /> 
		<@displayRow title=uiLabelMap.WarehouseStatus text=estatus.descripcion/>
        <@displayTitleCell title=uiLabelMap.FinancialsAccountigDate titleClass="requiredField" />
		<#assign formato = Static["org.ofbiz.base.util.UtilDateTime"].getDateFormat(locale)>
        <@inputDateCell name="fechaContable" default=Static["org.ofbiz.base.util.UtilDateTime"].nowDateString(formato) />
      </tr>  
      <tr>
      	<@inputTextRow title=uiLabelMap.PurchDescription name="comentario" size=60 default=comentario?if_exists titleClass="requiredField" />
      </tr>
      <tr>
      	<@inputTextRow title=uiLabelMap.PurchInvoiceNumber name="numeroFactura" size=15 default=comentario?if_exists titleClass="requiredField" />
      </tr>
      <tr>
		<td class="titleCell" >
		<@display text=uiLabelMap.AccountingTipoDocumento+" :" class="requiredField"/>
		</td>
		<@inputSelectCell id="acctgTransTypeId" name="acctgTransTypeId" list=listEventos?sort_by("descripcion") 
                		key="acctgTransTypeId" displayField="descripcion" onChange="cambiaEvento(this.value,'2');" required=false defaultOptionText=""/>
	 </tr>
    </tbody>
  </table>

<#if datosPedido?exists>
<@paginate name="datosPedido" list=datosPedido facilityId=facilityId  >
<#noparse >
<@navigationHeader title=uiLabelMap.ProductInventoryItems />
  <#-- turn this into a multi form -->
  <@inputHiddenUseRowSubmit/>
  <@inputHiddenRowCount list=pageRows/>
  <table class="listTable">
    <tr class="listTableHeader">
        <@headerCell title=uiLabelMap.WarehousePedidoInternoId                orderBy="pedidoInternoId" />
        <@headerCell title=uiLabelMap.WarehouseDetallePedido                  orderBy="detallePedidoInternoId" />
        <@headerCell title=uiLabelMap.ProductProductId                        orderBy="productId" />  
        <@headerCell title=uiLabelMap.PurchasingProduct                       orderBy="productName" />       
        <@headerCell title=uiLabelMap.CommonQuantity      				      orderBy="cantidad" />
        <@headerCell title=uiLabelMap.WarehouseCantidadEntregada              orderBy="cantidadEntregada" />
        <td>${uiLabelMap.WarehouseVariance}</td>
    </tr>
    <#list pageRows as item>
        <@inputHidden name="pedidoInternoId"         value=item.pedidoInternoId         index=item_index/>
        <@inputHidden name="detallePedidoInternoId"  value=item.detallePedidoInternoId  index=item_index/>
        <@inputHiddenRowSubmit                											index=item_index/>
        <tr class="${tableRowClass(item_index)}" name="item_o_${item_index}">
            <@displayCell          text=item.pedidoInternoId/>
            <@displayCell          text=item.detallePedidoInternoId/>
            <@displayCell          text=item.productId/>
            <@displayCell          text=item.productName?if_exists />
            <@displayCell          text=item.cantidad />
            <@displayCell          text=item.cantidadEntregada />
            <@inputTextCell        name="varianceQty" size=6 index=item_index />
        </tr>
    </#list >
    <#if pageRows?size != 0>
     <table>
     	<tr>
        <@displayTitleCell title=uiLabelMap.WarehouseEstatusPedidoInt />
        <@inputSelectHashCell hash= {"ABIERTO":"Abierto", "CERRAR":"Cerrado"} id="estatusId" name="estatusId" />
      </tr> 
      	<td colspan="1">&nbsp;</td>
      	<td colspan="1"><div id="buttonsBar"><@inputSubmit title=uiLabelMap.CommonApply /></div></td>
     </table>
    </#if>
  </table>
</#noparse >
</@paginate>
</#if>
</form>