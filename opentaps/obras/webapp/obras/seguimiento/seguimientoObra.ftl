<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<script type="text/javascript">

		//Functions to send request to server for load relate objects
		function contactTypeChangedContratista() 
		{   var obraId = document.getElementById('obraId').value;
			if (obraId != "") 
		    { 	var requestData = {'obraId' : obraId};		    			    
		    	opentaps.sendRequest('obtieneContratistas', requestData, function(data) {cargarProveedores(data)});
		    }
			
		}

		//Functions load the response from the server
		function cargarProveedores(data) 
		{	i = 0;				
			var select = document.getElementById("contratoId");
			document.getElementById("contratoId").innerHTML = "";
			for (var key in data) 
			{	if(data[key]=="ERROR")
				{	document.getElementById("contratoId").innerHTML = "";
				}
				else
				{	select.options[i] = new Option(data[key]);
					select.options[i].value = key;					
				  	i++;
				}	    		
			}		
		}

</script>

<#if !seguimientoList?has_content>
<form name="buscarObraForm" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <table class="twoColumnForm">
    <tbody>
      <tr>
        <@displayTitleCell title=uiLabelMap.Obra titleClass="requiredField" />
        <td>	   	    
     		<select name="obraId" id ="obraId" size="1"  onchange="contactTypeChangedContratista(this.form)">
		    	<option value=""></option>
		        <#list obrasList as modulo>
		        	<option  value="${modulo.obraId}">${modulo.get("nombre",locale)}</option>
		        </#list>
		    </select>	 
	    </td>
      </tr>
      <tr>      
        <@displayTitleCell title=uiLabelMap.ObraContratista titleClass="requiredField" />
	    <td>  
      		<select name="contratoId" id="contratoId" size="1" class="selectBox">      				  				
	  		</select>		
	  	</td>	
	  </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialsAccountigDate titleClass="requiredField" />
        <@inputDateTimeCell name="fechaContable" default=Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp() />
      </tr>
      <tr>
      	<@displayTitleCell title=uiLabelMap.ObrasFechaRealInicio />
      	<@inputDateCell name="fechaRealInicio" size=10 />
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.ObrasFechaRealFin  />
        <@inputDateCell name="fechaRealFin" size=10 />
      </tr>
      <tr>
      	<@inputTextRow title=uiLabelMap.Descripcion name="description" size=60 default=description?if_exists titleClass="requiredField"/>
      </tr>  
      <tr>
        <@displayTitleCell title=uiLabelMap.MontoAvance titleClass="requiredField" />
      	<@inputCurrencyCell name="montoAvance" defaultCurrencyUomId="MXN"/>
      </tr>  
      <@inputSubmitRow title=uiLabelMap.CommonCreate />
    </tbody>
  </table>
</form>
</#if>


<#if seguimientoList?has_content>
<form name="seguimientoForm" method="post" action="">
  <table width=100%>
	<td align="center"><font size=2><b>Información del seguimiento del contrato</b></font></td>	
  </table width=100%></td>
  <table class="twoColumnForm">
    <tbody>
      <tr>
        <@displayCell text=uiLabelMap.AvanceId/>
        <@displayCell text=uiLabelMap.FechaAvance/>
        <@displayCell text=uiLabelMap.ObrasFechaRealInicio/>
        <@displayCell text=uiLabelMap.ObrasFechaRealFin/>
        <@displayCell text=uiLabelMap.MontoAvance/>
        <@displayCell text=uiLabelMap.MontoRestante/>
        <@displayCell text=uiLabelMap.ObraDescripcion/>
        <@displayCell text=uiLabelMap.PorcentajeAvance/>
        <@displayCell text=""/>
      </tr>
      <#assign porcentajeTotal = 0 />
      <#list seguimientoList as row>
        <tr class="${tableRowClass(row_index)}">
            <@displayCell text=row.avanceId/>
            <@displayCell text=row.fechaAvance/>
            <@displayDateCell date=row.fechaRealInicio />
            <@displayDateCell date=row.fechaRealFin />
            <@displayCurrencyCell amount=row.montoAvanceConIva currencyUomId=tipoMoneda/>
            <@displayCurrencyCell amount=row.montoRestante currencyUomId=tipoMoneda/>
            <@displayCell text=row.descripcion/>
            <#assign porcentaje = row.porcentajeAvance />
            <#assign porcentajeTotal = porcentajeTotal + porcentaje />
            <@displayCell text=porcentaje/>
            <@displayLinkCell text="Ver póliza" href="viewAcctgTrans?acctgTransId=${row.acctgTransId}"/>
        </tr>
      </#list>  
       <tr>
        <@displayCell text=""/>
        <@displayCell text=""/>
        <@displayCell text=""/>
        <@displayCell text=""/>
        <@displayCell text=""/>
        <@displayCell text=""/>
        <@displayCell text="Total:"/>
        <@displayCell text=porcentajeTotal/>
        <@displayCell text=""/>
      </tr> 
    </tbody>
  </table>
</form> 
</#if>