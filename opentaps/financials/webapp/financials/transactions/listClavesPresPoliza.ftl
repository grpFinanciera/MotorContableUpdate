<#--
 * Lista de resultados preliminares de pólizas contables
 * Author: Vidal García
 * Versión 1.0
 * Fecha de Creación: Julio 2013
-->

<#-- Parametrized find form for transactions. -->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@frameSection title=uiLabelMap.ClavesPresupuestales >
  <form method="post" action="<@ofbizUrl>actualizaStatusAcctg</@ofbizUrl>" name="actualizaStatusAcctg">  
	  <table border=1>
	  	<tr>      
		  <td><@displayTitleCell title=uiLabelMap.clavePresupuestal /></td>
		  <td><@displayTitleCell title=uiLabelMap.FinancialsProduct /></td>
		  <td><@displayTitleCell title=uiLabelMap.Descripcion /></td>
		  <td><@displayTitleCell title=uiLabelMap.Organizacion /></td>  
		  <td><@displayTitleCell title=uiLabelMap.Monto /></td>
		  <td><@displayTitleCell title=uiLabelMap.FinancialsStatusId /></td>
		  <td></td>	  
	    </tr>
	    
	    <#assign count = 1 />
  		${acctgTransPolizaList}
  		<form method="post" action="<@ofbizUrl>actualizaStatusAcctg</@ofbizUrl>" name="actualizaStatusAcctg${count}">	  	
	  	<tr>
		  	<#list acctgTransPolizaList as lista>
		  		<#assign agrupador=lista.agrupador />		  		
		  		<@inputHidden name="acctgTransId" value=lista.acctgTransId index=tag_index />
		  		<@inputHidden name="clavePresupuestal" value=lista.clavePresupuestal index=tag_index />
		  		<@inputHidden name="idTipoDoc" value=lista.idTipoDoc index=tag_index />
		  		<@inputHidden name="tipoPolizaId" value=lista.tipoPolizaId index=tag_index />		  				  		
	  			<@inputHidden name="agrupador" value="${(agrupador)?if_exists}" index=tag_index />	  		  	      
			    <td><@displayCell text=lista.clavePresupuestal class="textright"/></td>
			    <td><@displayCell text=lista.productName class="textright"/></td>
			    <td><@displayCell text=lista.description  class="textright"/></td>    
			    <td><@displayCell text=lista.groupName class="textright"/></td>
			    <td><@displayCurrencyCell amount=lista.postedAmount currencyUomId=parameters.orgCurrencyUomId  class="textright"/></td>
			    <#if lista.claveContabilizada == "Y">
			    	<#assign status="AFECTAR" />
			    <#else>
			    	<#assign status="NO AFECTAR" />
			    </#if>	 
			    <td><@displayCell text="${status}" class="textright"/></td>
			    <td><td><@inputSubmitCell title=uiLabelMap.CommonAdd class="textright"/></td></td>
			    <#assign count=count + 1/>
		    </#list>
	    </tr>
	 	</form>	    
	  </table>
   </form> 
  </@frameSection>
