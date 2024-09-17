<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<script language="JavaScript" type="text/javascript">
<!-- //

		////Funcion para eliminar el registro de un autorizador
		function contactTypeClickElimina(contador) 
		{	confirmar=confirm("Est\u00e1 seguro?"); 
			if (confirmar) 
			{	
				var areaId = document.getElementById('areaId').value;
			    var organizationPartyId = document.getElementById('organizationPartyId').value;
			    var tipoWorkFlowId = document.getElementById('tipoWorkFlowId').value;
				var autorizadorId = document.getElementById('autorizadorId'+contador).value;
		  		if (areaId != "" && autorizadorId != "") 
		    	{ 	
		    		var requestData = {'areaId' : areaId, 'autorizadorId' : autorizadorId, 'organizationPartyId' : organizationPartyId, 'tipoWorkFlowId' : tipoWorkFlowId};
		    		opentaps.sendRequest('eliminaAutorizador', requestData, function(data) {responseEliminaAutorizador(data)});
		    	}		    					
			}													
		}

		//Functions load the response from the server
		function responseEliminaAutorizador(data) 
		{	for (var key in data) 
			{	if(data[key]=="ERROR")
				{	alert("Ocurri\u00f3 un error al eliminar el autorizador");
				}
				else if(data[key]=="ERROR2")
				{	alert("No se puede eliminar el autorizador, es necesario eliminar su antecesor o predecesor");
				}
				else 
				{	alert("El autorizador se ha eliminado");
					document.getElementById('formFicticio').submit();	
				}	    		
			}		
		}		
		  
// -->
</script>

<#if error?has_content>
    <@display text=error class="requiredField" />
</#if>
<form name="addAutoForm" id="addAutoForm" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <input type="hidden" name="organizationPartyId" value="${organizationPartyId}"/>
  <input type="hidden" name="areaId" value="${areaId}"/>
  <input type="hidden" name="tipoWorkFlowId" value="${tipoWorkFlowId}"/>
  <table class="twoColumnForm">
    <tbody>
        <tr>
        <@displayTitleCell title=uiLabelMap.PurchasingAuto />
        <@inputLookupCell lookup="LookupPartyName" form="addAutoForm" name="personId" />
       </tr>
      <@inputSubmitRow title=uiLabelMap.CommonCrear />
    </tbody>
  </table>
</form>

<#if areaListBuilder?has_content>
<@paginate name="listAutorizadores" list=areaListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.PurchasingAuto orderBy="autorizadorId"/>
                <@headerCell title=uiLabelMap.PurchasingAutoName orderBy="personaNameDesc"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayCell text=row.autorizadorId/>
                <@displayCell text=row.personaNameDesc/>
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>

<form name="secuenciaAutoForm" method="post" action="">
  <@inputHidden name="performFind" value="A"/>
  <table class="twoColumnForm">
    <tbody>
      <@inputSelectRow title=uiLabelMap.PurchasingAutoAdd required=false list=autorizadorList  displayField="autorizadorId" name="autorizadorId" default=autorizadorId?if_exists />
      <@inputSubmitRow title=uiLabelMap.CommonAdd />
    </tbody>
  </table>
</form>
</#if>

<#if autorizadorListBuilder?has_content>
<@paginate name="listAdd" list=autorizadorListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.PurchasingAuto orderBy="autorizadorId"/>
                <@headerCell title=uiLabelMap.PurchasingAutoName orderBy="personaNameDesc"/>
                <@headerCell title=uiLabelMap.PurchasingSecuencia orderBy="secuencia"/>
                <th></th>
            </tr>
            <#assign count = 1 />
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
           		<@inputHidden name="areaId" id="areaId" value=row.areaId/>
           		<@inputHidden name="organizationPartyId" id="organizationPartyId" value=row.organizationPartyId/>
           		<@inputHidden name="tipoWorkFlowId" id="tipoWorkFlowId" value=row.tipoWorkFlowId/>
                <@displayCell text=row.autorizadorId/>
                <@inputHidden name="autorizadorId${count}" id="autorizadorId${count}" value=row.autorizadorId/>
                <@displayCell text=row.personaNameDesc/>
                <@displayCell text=row.secuencia/>
                <td align="center" width="7%">
					<input name="submitButton" id="elimina${count}"" type="submit" class="subMenuButtonDangerous" value="${uiLabelMap.PurchasingEliminaAuto}" onClick="contactTypeClickElimina('${count}')"/>								
				</td>
            </tr>
            <#assign count=count + 1/>
            </#list>
        </table>
    </#noparse>
</@paginate>
</#if>

<form name="formFicticio" id="formFicticio" method="post" action="">
  <input type="hidden" name="organizationPartyId" value="${organizationPartyId}"/>
  <input type="hidden" name="areaId" value="${areaId}"/>
  <input type="hidden" name="tipoWorkFlowId" value="${tipoWorkFlowId}"/>
  <table class="twoColumnForm">
    <tbody>
    </tbody>
  </table>
</form>