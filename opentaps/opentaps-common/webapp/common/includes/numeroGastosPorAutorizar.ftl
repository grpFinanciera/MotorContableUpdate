<#if application?exists>
	<#if application == "gastosReserva">
		<#if contadorGPA?exists>
			<#if (contadorGPA?int > 0)>
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
					        <li><span class="navTitleRequisicion">${uiLabelMap.SarpGastosPorAutorizar}:</span>			              
							<a href="<@ofbizUrl>pendienteSolicitud</@ofbizUrl>">${contadorGPA!'0'}</a></li>
					        </ul>
					      </td>
					    </tr>
					  </tbody>
					</table>
			</#if>	
		</#if>
	</#if>		
</#if>