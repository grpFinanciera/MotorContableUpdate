<#--
 * Copyright (c) Open Source Strategies, Inc.
 *
 * Opentaps is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Opentaps is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Opentaps.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  @author Leon Torres (leon@opensourcestrategies.com)
-->


<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<script type="text/javascript">
    	//Funcion para eliminar el permiso de usuario
		function Elimina(contador) {	
			confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar) {	
				var facilityId = document.getElementById('facilityId'+contador).value;
				var partyId = document.getElementById('partyId'+contador).value;	
		  		if (facilityId  && partyId){ 	
		    		var requestData = {'facilityId' : facilityId, 'partyId' : partyId};
		    		opentaps.sendRequest('eliminaPermisoAlmacenActivo', requestData, function(data) {responseElimina(data)});
		    	}				
			}													
		}
		
		//Functions load the response from the server
		function responseElimina(data){
			for (var key in data) {	
				if(data[key]=="ERROR"){
					alert("Ocurri\u00f3 un error al eliminar el elemento");
				}
				else{
					alert("El elemento se ha eliminado");
					location.reload(true);
				}	    		
			}		
		}    
		
</script>


<@frameSection title=uiLabelMap.ControlPermisosAlmacenActivoFijo> 
<form method="post" action="<@ofbizUrl>asignarPermisoAlmacen</@ofbizUrl>" name="asignarPermisoAlmacen">
  
  <div class="form" style="border:0">
    <table class="fourColumnForm" style="border:0">
	<@inputAutoCompletePartyRow title=uiLabelMap.ControlUsuario name="partyId" id="partyId" size="20" titleClass="requiredField"/>	    
	<@inputSelectRow title=uiLabelMap.ControlAlmacen required=false list=listAlmacen displayField="facilityName" name="facilityId" default=facilityId?if_exists titleClass="requiredField"/>      
    <@inputSubmitRow title=uiLabelMap.CommonAsignar />
    </table>
  </div>
</form>
</@frameSection>


<#if listAsignaciones?has_content>
<form name="listPermisosAlmacenForm" method="post" action="">
  <table width=100%>
	<td align="center"><font size=2><b>Asignaciones</b></font></td>	
  </table width=100%></td>
  <table class="twoColumnForm">
    <tbody>
      <tr>
      	<@displayCell text=uiLabelMap.ControlPersonaId class="tableheadtext"/>
        <@displayCell text=uiLabelMap.ControlNombrePersona class="tableheadtext"/>
        <@displayCell text=uiLabelMap.ControlApellidoPersona class="tableheadtext"/>
        <@displayCell text=uiLabelMap.ControlAlmacenId class="tableheadtext"/>
        <@displayCell text=uiLabelMap.ControlAlmacen class="tableheadtext"/>
        <td></td>
      </tr>
      <#assign count = 1 /> 
      <#list listAsignaciones as row>
        <tr class="${tableRowClass(row_index)}">
        	<@inputHidden name="facilityId${count}" id="facilityId${count}" value=row.facilityId/>
        	<@inputHidden name="partyId${count}" id="partyId${count}" value=row.partyId/>
        	<@displayCell text=row.partyId/>
            <@displayCell text=row.firstName/>
            <@displayCell text=row.lastName/>
            <@displayCell text=row.facilityId/>
            <@displayCell text=row.facilityName/>
            <td align="center" width="2%">
				<a name="submitButton" id="elimina${count}"  class="subMenuButtonDangerous" onClick="Elimina('${count}')">${uiLabelMap.ElimiarPermiso}</a>								
			</td>
        </tr>
        <#assign count=count + 1/>
      </#list>   
    </tbody>
  </table>
</form>
</#if>