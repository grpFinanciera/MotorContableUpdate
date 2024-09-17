<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<script language="JavaScript" type="text/javascript">
<!-- //

		////Funcion para eliminar un firmante
		function imprimirPdf(contador, campoPDF) 
		{	alert(":))");
			alert("campoPDF: " + campoPDF);
			var wor = "workFlowId"+contador;
			
			var workFlowId = document.getElementById(wor).value;
			alert("workFlowId: " + workFlowId);
			    
	  		if (workFlowId != "") 
	    	{ 	var requestData = {'workFlowId' : workFlowId, 'campoPDF' : campoPDF};		    			    
	    		opentaps.sendRequest('imprimePdf', requestData, function(data) {response(data)});
	    	}																			
		}

		//Functions load the response from the server
		function response(data) 
		{	alert(":)");
			for (var key in data) 
			{	if(data[key]=="error")
				{	alert("Ocurri\u00f3 un error al imprimir el documento original");
				}									    	
			}		
		}		
		  
// -->
</script>

<@frameSection title="">		
	<table border="0" width="100%">

	<thead>		
		<tr>
			<th>${uiLabelMap.workFlowId}</th>
			<th>${uiLabelMap.reporteId}</th>			
			<th>${uiLabelMap.firmanteId}</th>			
			<th>${uiLabelMap.reporteNombre}</th>
			<th>${uiLabelMap.fechaEmision}</th>
			<th>${uiLabelMap.descripcion}</th>
			<th>${uiLabelMap.pdfOriginal}</th>			
			<th></th>
		</tr>
	</thead>
	<tbody>
		<#assign count = 1 />
		<#list listaReportes as row>
				<@inputHidden name="workFlowId" id="workFlowId${count}" value=row.reporteId!/>           		
						<tr class="${tableRowClass(row_index)}">
						    <@displayLinkCell text=row.workFlowId href="nuevoFirmante?workFlowId=${row.workFlowId}&amp;reporteId=${row.reporteId}"/>
                			<@displayCell text=row.reporteId/>
                			<@displayCell text=row.firmanteId/>
                			<@displayCell text=row.reporteNombre/>
                			<@displayCell text=row.fechaEmision/>                 			
                			<@displayCell text=row.descripcion/>
                			<td>
                			<a href="<@ofbizUrl>imprimePdf?workFlowId=${row.workFlowId?if_exists}&amp;campoPDF=pdfOriginal</@ofbizUrl>">Archivo original</a>
                			</td>                				
						</tr>
			<#assign count=count + 1/>
		</#list>
	</tbody>
	</table>
</@frameSection>

