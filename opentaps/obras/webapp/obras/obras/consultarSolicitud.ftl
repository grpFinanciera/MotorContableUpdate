<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form name="busquedaSolicitudForm" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <table class="twoColumnForm">
    <tbody>
      <tr>
        <@displayTitleCell title=uiLabelMap.ObraObraId/>
        <@inputTextCell name="obraId" maxlength=60  />
      </tr>
      <tr>
         <@displayTitleCell title=uiLabelMap.ObraFiltroSolicitud/>
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
	    <@inputSelectRow title=uiLabelMap.ObraStatus required=false list=estatusList  displayField="descripcion" name="statusId" default=statusId?if_exists />
       </tr>
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
