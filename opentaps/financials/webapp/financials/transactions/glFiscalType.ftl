<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<#-- display the list of transactions -->
	
<#if listTransIds?has_content>
	<#assign acctTrans = listTransIds>
	   <#list acctTrans as mapAcctTrans>
        	<#if mapAcctTrans.acctgTransId?contains("PT")>         	  
	        	<div class="screenlet-header"> 	
	 				<span class="boxhead">Transacci�n Patrimonial</span>  
				</div>
			<#elseif mapAcctTrans.acctgTransId?contains("P")>       	  
	        	<div class="screenlet-header"> 	
	 				<span class="boxhead">Transacci�n Presupuestal</span>  
				</div>
			<#elseif mapAcctTrans.acctgTransId?contains("A")>       	  
	        	<div class="screenlet-header"> 	
	 				<span class="boxhead">Transacci�n Presupuesto Compensado</span>  
				</div>
			<#else>
				<div class="screenlet-header"> 	
	 				<span class="boxhead">Transacci�n Contable</span>  
				</div>
			</#if>
        	<table class="listTable" cellspacing="0" style="border:none;">          	  
		      <tr class="listTableHeader">
		        <td><span>${uiLabelMap.Transaction}</span></td>
		        <td><span>${uiLabelMap.Description}</span></td>       
		        <td></td>
		      </tr>
		      <tr class="${tableRowClass(entry_index)}">
		            <td><a class="linktext" href="<@ofbizUrl>viewAcctPresupuestal?acctgTransId=${mapAcctTrans.acctgTransId}</@ofbizUrl>">${mapAcctTrans.acctgTransId}</a></td>
		            <td>${mapAcctTrans.clavePresupuestal}</td>			                                               
		      </tr> 
     	  </table>
        </#list>  	       
  </#if>  

     
   

