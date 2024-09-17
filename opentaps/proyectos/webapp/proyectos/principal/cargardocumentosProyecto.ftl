<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@frameSection title=uiLabelMap.DataImportUploadFile>
<form name="CargaDocumentosProyecto" method="post" enctype="multipart/form-data" action="CargaDocumentosProyecto">
	<table>
	<tr>                	
				<@displayTitleCell title="Proyecto" titleClass="requiredField"/>
				<td>	   	    
					<select name="proyectoId" id="proyectoId" size="1" class="selectBox" onChange="contactTypeChanged(this.form);">
						<option value=""></option>
							<#list listProyectos as proyecto>
									<option  value="${proyecto.proyectoId}">${proyecto.get("codigoProyecto",locale)}-${proyecto.get("nombreProyecto",locale)}</option>
							</#list>
				    </select>	 
				</td>
			</tr>
		<#assign tiposDocumentos = ["Contrato","Documentos"]/>
		<tr>
			<@displayTitleCell title="Documentos de proyecto"/>
		    <td>
		    	<select name="tipoDocumento" class="inputBox">
		        	<#list tiposDocumentos as tipo>
		            		<option value="${tipo}">${tipo}</option>
		        	</#list>
		        </select>
		    </td>
	    </tr>
		<@inputFileRow title=uiLabelMap.DataImportFileToImport name="uploadedFile" />
        
        <@inputSubmitRow title="${uiLabelMap.DataImportUpload}"/>
	</table>
</form>
</@frameSection>