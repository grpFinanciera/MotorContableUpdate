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
    	    	
		function Revisar(contador) 
		{	confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar) 
			{	var facilityId = document.getElementById('facilityId'+contador).value;
				var orderId = document.getElementById('orderId'+contador).value;
				var shipmentId = document.getElementById('shipmentId'+contador).value;				
		  		if (facilityId != "" && orderId != "" && shipmentId != "") 
		    	{ 	var requestData = {'facilityId' : facilityId, 'orderId' : orderId, 'shipmentId' : shipmentId};		    			    
		    		opentaps.sendRequest('marcarNotificacionResuelta', requestData, function(data) {responseMarcarResuelta(data)});
		    	}				
			}													
		}

		//Functions load the response from the server
		function responseMarcarResuelta(data) 
		{	for (var key in data) 
			{	if(data[key]=="ERROR")
				{	alert("Ocurri\u00f3 un error al eliminar el elemento");
				}
				else
				{	alert("El elemento se ha eliminado");
					var cuentaBancariaId = document.getElementById('cargarPagina').submit();
					
					
				}	    		
			}		
		}    
		
</script>


<@frameSection title=uiLabelMap.ControlNotificacionesRecepcionBienesActivoFijo> 




	<#if listaPendientes?has_content>
	<form name="listNotificacionRecepcionesPendientesForm" method="post" action="">
	  <table width=100%>
		<td align="center"><font size=2><b>${uiLabelMap.ControlNotificacionesRecepcionBienesActivoFijo}</b></font></td>	
	  </table width=100%></td>
	  <table class="twoColumnForm">
	    <tbody>
	      <tr>
	      	<@displayCell text=uiLabelMap.ControlShipmentId class="tableheadtext"/>
	      	<@displayCell text=uiLabelMap.ControlOrdenCompra class="tableheadtext"/>	        
	        <@displayCell text=uiLabelMap.ControlAlmacenId class="tableheadtext"/>
	        <@displayCell text=uiLabelMap.ControlAlmacen class="tableheadtext"/>
	        <@displayCell text=uiLabelMap.ControlOrderName class="tableheadtext"/>
	        <@displayCell text=uiLabelMap.ControlOrderDate class="tableheadtext"/>
	        <@displayCell text=uiLabelMap.ControlEstatusNotificacion class="tableheadtext"/>	        
	        <td></td>
	      </tr>
	      <#assign count = 1 />
	      <#list listaPendientes as row>
	        <tr class="${tableRowClass(row_index)}">
	        	<@inputHidden name="shipmentId${count}" id="shipmentId${count}" value=row.shipmentId/>
	        	<@inputHidden name="orderId${count}" id="orderId${count}" value=row.orderId/>
	        	<@inputHidden name="facilityId${count}" id="facilityId${count}" value=row.facilityId/>
	        	<@displayLinkCell text=row.shipmentId href="verRecepcionPendiente?shipmentId=${row.shipmentId}&orderId=${row.orderId}"/>
	            <@displayCell text=row.orderId/>
	            <@displayCell text=row.facilityId/>
	            <@displayCell text=row.facilityName/>
	            <@displayCell text=row.orderName/>
	            <@displayCell text=row.orderDate/>
	            <@displayCell text=row.description/>	            
	            <td align="center" width="2%">
					<input name="submitButton" id="revisar${count}"" type="submit" class="subMenuButtonDangerous" value="${uiLabelMap.MarcarRevisada}" onClick="Revisar('${count}')"/>								
				</td>
	        </tr>
	        <#assign count=count + 1/>
	      </#list>   
	    </tbody>
	  </table>
	</form>
	</#if>
</@frameSection>