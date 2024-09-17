  <#--
 * Copyright (c) Open Source Strategies, Inc.
 *
 * Opentaps is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Opentaps is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Opentaps.  If not, see <http://www.gnu.org/licenses/>.
-->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<script  languaje="JavaScript">

	function crearClasificacion(){
		var enumTypeId = document.getElementById("enumTypeId").value;
		var nivelId = document.getElementById("nivelId").value;
		var enumCode = document.getElementById("enumCode").value;
		var sequenceId = document.getElementById("sequenceId").value;
		var description = document.getElementById("description").value;
		var parentEnumId = document.getElementById("parentEnumId").value;
		var fechaInicio = document.getElementById("fechaInicio").value;
		var fechaFin = document.getElementById("fechaFin").value;
		
		var requestData = { 'enumTypeId' : enumTypeId, 'enumCode' : enumCode,
							'description' : description, 'nivelId' : nivelId,
							'parentEnumId' : parentEnumId, 'fechaInicio' : fechaInicio,
							'fechaFin' : fechaFin ,'sequenceId': sequenceId };		    			    
		opentaps.sendRequest('crearNuevaClasificacion', requestData, function(data) {responseCrear(data)});
	}
	
	function responseCrear(data) 
	{
		for (var key in data) 
			{
				if(data[key]=="ERROR")
				{	
					alert(data['mensajeError']);
					break;
				}
				else
				{	
					alert("El elemento ha sido creado");
					document.getElementById('buscarClasificacion').submit();
					break;
				}	
			}
	}

 </script> 


<@frameSection title=uiLabelMap.FinancialsCreateAccountingTag>
  <form method="post" name="buscarClasificacion" id="buscarClasificacion">
    <table class="twoColumnForm" style="border:0">
	<@inputHidden id="performFind" name="performFind" value="Y"/>
	<@inputSelectRow title=uiLabelMap.FinancialsClasificacion name="enumTypeId" titleClass="requiredField" id="enumTypeId"
			list=tagTypes?default({}) key="enumTypeId" displayField="description" onChange="this.form.submit();" />
	<@inputSelectRow title=uiLabelMap.ProductLevel name="nivelId" list=listNivelPresupuestal! key="nivelId" displayField="descripcion" titleClass="requiredField" id="nivelId"/>
   	<@inputTextRow title=uiLabelMap.CommonCode name="sequenceId" size=10 maxlength=20 titleClass="requiredField" id="sequenceId"/>
	<@inputTextRow title=uiLabelMap.CommonName name="enumCode" size=40 maxlength=255 titleClass="requiredField" id="enumCode"/>
	<@inputTextRow title=uiLabelMap.CommonDescription name="description" size=60 maxlength=255 titleClass="requiredField" id="description"/>	
	<@inputTextRow title=uiLabelMap.ClaveSuperior  size=10 name="parentEnumId" size=10  id="parentEnumId"/>
	<@inputDateRow title=uiLabelMap.FormFieldTitle_datefrom name="fechaInicio" size=12 default="" titleClass="requiredField" id="fechaInicio"/>
	<@inputDateRow title=uiLabelMap.FormFieldTitle_dateThru name="fechaFin" size=12 default="" titleClass="requiredField" id="fechaFin"/>
	<@inputButtonNoSubmitRow title=uiLabelMap.CommonCreate onClick="crearClasificacion();" />
    </table>
	</form>
</@frameSection>
