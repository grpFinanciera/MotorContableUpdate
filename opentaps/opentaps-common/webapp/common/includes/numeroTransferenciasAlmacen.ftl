
<#if application?exists>
	<#if application == "warehouse">
		<#if contador?exists>
			<#if (contador?int > 0)>
				<style type="text/css">
				ul.numTransferencia {
				    background: #FFFFFF;
				    border-top: 1px solid #FFFFFF;
				    border-bottom: 1px solid <#--${historyDecorationColor}-->;
				}
				
				ul.numTransferencia a {
				    color: #FF0000;
				    border-left: 1px solid #FFFFFF;
				}
				
				ul.numTransferencia a:hover {
				    color: #0000FF;
				}

				</style>
							
					<table cellpadding="0" cellspacing="0" border="0" width="100%">
					  <tbody>
					    <tr>
					      <td>
					        <ul class="numTransferencia">					        
					        <li><span class="navTitleRequisicion">${uiLabelMap.SARPTransferenciasPendientes}:</span></li>					              
						            <#assign text = contador/>
						    <li><a href="<@ofbizUrl>pendientesTransferir</@ofbizUrl>" title="">${text}</a></li>
					        </ul>
					      </td>
					    </tr>
					  </tbody>
					</table>
			</#if>	
		</#if>
	</#if>		
</#if>
