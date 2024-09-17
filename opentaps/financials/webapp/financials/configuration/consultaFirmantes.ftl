<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<script language="JavaScript" type="text/javascript">
<!-- //

		////Funcion para eliminar un firmante
		function contactTypeClicEliminar(contador) 
		{	confirmar=confirm("ÀEst\u00e1 seguro?"); 
		    var rep = "reporteId"+contador;
		    var fir = "firmanteId"+contador;
			if (confirmar) 
			{	var reporteId = document.getElementById(rep).value;				
			    var firmanteId = document.getElementById(fir).value;
		  		if (reporteId != "" && firmanteId != "") 
		    	{	var requestData = {'reporteId' : reporteId, 'firmanteId' : firmanteId};		    			    
		    		opentaps.sendRequest('eliminarFirmante', requestData, function(data) {responseEliminaFirmante(data)});
		    	}				
			}													
		}

		//Functions load the response from the server
		function responseEliminaFirmante(data) 
		{	for (var key in data) 
			{	if(data[key]=="ERROR")
				{	alert("Ocurri\u00f3 un error al eliminar el firmante de este reporte");
				}				
				else
				{	alert("El firmante se ha eliminado");					
				}	    		
				document.getElementById('formFicticio').submit();
			}		
		}		
		  
// -->
</script>



<#if error?has_content>
    <@display text=error class="requiredField" />
</#if>

<form name="agregarFirmante" id="agregarFirmante" method="post" action="<@ofbizUrl>agregarFirmante</@ofbizUrl>">
  <@inputHidden name="performFind" value="Y"/>
  <input type="hidden" name="organizationPartyId" value="${organizationPartyId}"/>
  <input type="hidden" name="reporteId" value="${reporteId}"/>  
  <table class="twoColumnForm">
    <tbody>
        <tr>
        <@displayTitleCell title=uiLabelMap.Firmante />
        <@inputLookupCell lookup="LookupPartyName" form="agregarFirmante" name="firmanteId" />
       </tr>
      <@inputSubmitRow title=uiLabelMap.CommonCrear />
    </tbody>
  </table>
</form>



<@frameSection title="">		
	<table border="0" width="100%">

	<thead>		
		<tr>
			<th>${uiLabelMap.ReporteId}</th>
			<th>${uiLabelMap.ReporteNombre}</th>			
			<th>${uiLabelMap.FirmanteId}</th>			
			<th>${uiLabelMap.NombreFirmante}</th>
			<th>${uiLabelMap.ApellidosFirmante}</th>
			<th></th>
		</tr>
	</thead>
	<tbody>
		<#assign count = 1 />
		<#list listFirmantes as row>
				<@inputHidden name="reporteId" id="reporteId${count}" value=row.reporteId!/>
           		<@inputHidden name="firmanteId" id="firmanteId${count}" value=row.firmanteId!/> 
						<tr class="${tableRowClass(row_index)}">
						    <@displayCell text=row.reporteId/>
                			<@displayCell text=row.reporteNombre/>
                			<@displayCell text=row.firmanteId/>
                			<@displayCell text=row.firstName/>
                			<@displayCell text=row.lastName/>               				
                			<td align="center" width="7%">
								<input name="submitButton" id="elimina${count}" type="submit" class="subMenuButtonDangerous" value="${uiLabelMap.Eliminar}" onClick="contactTypeClicEliminar('${count}')"/>								
							</td>		
						</tr>
			<#assign count=count + 1/>
		</#list>
	</tbody>
	</table>
</@frameSection>

<form name="formFicticio" id="formFicticio" method="post" action="">
  <input type="hidden" name="reporteId" value="${reporteId}"/>
  </form>