<#if application?exists>
	<#if application == "purchasing">
		<#if contadorPA?exists>
			<#if (contadorPA?int > 0)>
				<style type="text/css">
				ul.numGastosPA {
				    background: #FFFFFF;
				    border-top: 1px solid #FFFFFF;
				    border-bottom: 1px solid <#--${historyDecorationColor}-->;
				}
				
				ul.numGastosPA a {
				    color: #FF0000;
				    border-left: 1px solid #FFFFFF;
				}
				
				ul.numGastosPA a:hover {
				    color: #0000FF;
				}
				</style>
							
					<table cellpadding="0" cellspacing="0" border="0" width="100%">
					  <tbody>
					    <tr>
					      <td>
					        <ul class="numGastosPA">					        
					        <li><span class="navTitleRequisicion">${uiLabelMap.SarpPagosAnPorAutorizar}:</span>			              
							<a href="<@ofbizUrl>pagosAnticipadosPendientes</@ofbizUrl>">${contadorPA!'0'}</a></li>
					        </ul>
					      </td>
					    </tr>
					  </tbody>
					</table>
			</#if>	
		</#if>
	</#if>		
</#if>