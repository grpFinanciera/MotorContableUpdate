<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form name="busquedaRequisicionForm" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <table class="twoColumnForm">
    <tbody>
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialsAccountigDate />
        <@inputDateCell name="fechaContable" form="" default="" />
     </tr> 
      <tr>
        <@displayTitleCell title=uiLabelMap.PurchRequisicionId/>
        <@inputTextCell name="requisicionId" maxlength=60  />
      </tr>
      <tr>
         <@displayTitleCell title=uiLabelMap.PurchasingFiltro/>
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
	    <@inputSelectRow title=uiLabelMap.PurchasingStatus required=false list=estatusList  displayField="descripcion" name="statusId" default=statusId?if_exists />
       </tr>
       <tr>
        <@displayTitleCell title=uiLabelMap.PurchasingProduct />
        <@inputAutoCompleteProductCell form="busquedaRequisicionForm" name="productId" />
       </tr>
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
