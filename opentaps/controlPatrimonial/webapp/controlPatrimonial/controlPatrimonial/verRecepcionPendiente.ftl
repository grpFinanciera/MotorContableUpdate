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
</script>


<@frameSection title=uiLabelMap.ControlListarElementosRecepcionPedidoContrato> 

	<#if listaActivos?has_content>
	<form name="listElementosRecepcionPedidoForm" method="post" action="">
	  <table width=100%>
		<td align="center"><font size=2><b>${uiLabelMap.ControlListarElementosRecepcionPedidoContrato}</b></font></td>	
	  </table width=100%></td>
	  <table class="twoColumnForm">
	    <tbody>
	      <tr>
	      	<@displayCell text=uiLabelMap.ControlId class="tableheadtext"/>
	      	<@displayCell text=uiLabelMap.ControlNombre class="tableheadtext"/>	        
	        <@displayCell text=uiLabelMap.ControlFixedAssetType class="tableheadtext"/>
	        <@displayCell text=uiLabelMap.FechaAdquisicion class="tableheadtext"/>
	        <@displayCell text=uiLabelMap.CostoCompra class="tableheadtext"/>
	        <@displayCell text=uiLabelMap.ControlProveedor class="tableheadtext"/>
	        <@displayCell text=uiLabelMap.ControlNumeroFactura class="tableheadtext"/>	        
	        <td></td>
	      </tr>
	      <#list listaActivos as row>
	        <tr class="${tableRowClass(row_index)}">	        	
	        	<@displayLinkCell text=row.fixedAssetId href="verActivoFijo?fixedAssetId=${row.fixedAssetId}"/>
	        	<@displayCell text=row.fixedAssetName/>
	            <@displayCell text=row.description/>
	            <@displayCell text=row.dateAcquired/>
	            <@displayCell text=row.purchaseCost/>
	            <@displayCell text=row.groupName/>
	            <@displayCell text=row.numeroFactura/>	            	           	            
	        </tr>
	      </#list>   
	    </tbody>
	  </table>
	</form>
	</#if>
</@frameSection>