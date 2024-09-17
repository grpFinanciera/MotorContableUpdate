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
-->



	

<#if application?exists>
	<#if application == "purchasing">
		<#if contador?exists>
			<#if (contador?int > 0)>
				<style type="text/css">
				ul.numRequisicion {
				    background: #FFFFFF;
				    border-top: 1px solid #FFFFFF;
				    border-bottom: 1px solid <#--${historyDecorationColor}-->;
				}
				
				ul.numRequisicion a {
				    color: #FF0000;
				    border-left: 1px solid #FFFFFF;
				}
				
				ul.numRequisicion a:hover {
				    color: #0000FF;
				}
				</style>
							
					<table cellpadding="0" cellspacing="0" border="0" width="100%">
					  <tbody>
					    <tr>
					      <td>
					        <ul class="numRequisicion">
					        <li><span class="navTitleRequisicion">${uiLabelMap.OpentapsRequisicionesPendientes}:</span></li>					              
						            
						            <#assign text = contador/>
						            <li><a href="<@ofbizUrl>requisicionesPendientes</@ofbizUrl>" title="">${text}</a></li>
						            							          		      			      
					        </ul>
					      </td>
					    </tr>
					  </tbody>
					</table>
			</#if>	
		</#if>
	</#if>		
</#if>
