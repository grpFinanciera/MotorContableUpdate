<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form name="busquedaPedidoInternoForm" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <table class="twoColumnForm">
    <tbody>
      <tr>
        <@displayTitleCell title=uiLabelMap.WarehousePedidoInternoId/>
        <@inputTextCell name="pedidoInternoId" maxlength=20 size=10 />
      </tr>
      <tr>
         <@displayTitleCell title=uiLabelMap.WarehousePedidoInternoFiltro/>
         <#assign filtros = ["Propias"]/>
         <td>
         	<select name="filtroId" class="inputBox">
         	<option value=""></option>
         		<#list filtros as filtro>
            		<option value="${filtro}">${filtro}</option>
        		</#list>
        	</select>
         </td>
       </tr> 
       <tr>
	    <@inputSelectRow title=uiLabelMap.WarehouseStatus required=false list=estatusList  displayField="descripcion" name="statusId" default=statusId?if_exists />
       </tr>
       <tr>
        <@displayTitleCell title=uiLabelMap.WarehouseProduct />
        <@inputAutoCompleteProductCell form="busquedaPedidoInternoForm" name="productId" />
       </tr>
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
