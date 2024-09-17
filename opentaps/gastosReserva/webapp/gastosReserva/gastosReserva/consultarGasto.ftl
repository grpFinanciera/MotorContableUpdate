<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form name="busquedaGastoReservaForm" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <table class="twoColumnForm">
    <tbody>
      <tr>
        <@displayTitleCell title=uiLabelMap.GastoReservadoId/>
        <@inputTextCell name="gastosReservaId" maxlength=60  />
      </tr>
      <tr>
         <@displayTitleCell title=uiLabelMap.GastosReservaTipoSolicitud/>
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
	    <@inputSelectRow title=uiLabelMap.PurchStatus required=false list=estatusList  displayField="descripcion" name="statusId" default=statusId?if_exists />
       </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.CommonDate />
        <@inputDateCell name="fecha" form="" default="" />
     </tr>
		<@inputTextRow title=uiLabelMap.GastosReservaProveedor id="proveedor" name="proveedor" size=30 maxlength="255" />
		<@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
