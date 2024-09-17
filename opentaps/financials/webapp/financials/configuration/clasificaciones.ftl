<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<script type="text/javascript">
	function actualizaClasificacion(numero){
		var enumId = document.getElementById("enumId_o_"+numero).value;
		var enumCode = document.getElementById("enumCode_o_"+numero).value;
		var description = document.getElementById("description_o_"+numero).value;
		var nivelId = document.getElementById("nivelId_o_"+numero).value;
		var parentEnumId = document.getElementById("parentEnumId_o_"+numero).value;
		var fechaInicio = document.getElementById("fechaInicio_o_"+numero).value;
		var fechaFin = document.getElementById("fechaFin_o_"+numero).value;
		
		
		var requestData = { 'enumId' : enumId, 'enumCode' : enumCode,
							'description' : description, 'nivelId' : nivelId,
							'parentEnumId' : parentEnumId, 'fechaInicio' : fechaInicio,
							'fechaFin' : fechaFin };		    			    
		 opentaps.sendRequest('actualizaClasificacion', requestData, function(data) {responseActualizar(data)});
		    		
	}
	
	function responseActualizar(data) 
	{	
		for (var key in data) 
			{
				if(data[key]=="ERROR")
				{	
					alert("Ocurri\u00f3 un error al actualizar el elemento");
				}
				else
				{	
					alert("El elemento ha sido actualizado");
				}
		}		
	}  
</script>

<@frameSection title=enumTypeDescription! >

<@paginate name="listClasificacionBuilder" list=listClasificacionBuilder rememberPage=false listNivelPresupuestal=listNivelPresupuestal! >
	<#noparse>
        <@navigationHeader/>
    </#noparse>
</@paginate>

       <table class="listTable">
            <tr class="listTableHeader">                
                <@displayTitleCell title=uiLabelMap.Codigo />
                <@displayTitleCell title=uiLabelMap.Consecutivo />
                <@displayTitleCell title=uiLabelMap.Nombre />
                <@displayTitleCell title=uiLabelMap.CommonDescription />
                <@displayTitleCell title=uiLabelMap.Nivel />
                <@displayTitleCell title=uiLabelMap.ClaveSuperior />
                <@displayTitleCell title=uiLabelMap.FormFieldTitle_datefrom />
                <@displayTitleCell title=uiLabelMap.FormFieldTitle_dateThru />
				<td></td>
            </tr>
            <#list listClasificacionBuilder as Enumeration>
            <tr class="${tableRowClass(Enumeration_index)}">
				<@inputHidden name="enumId" value=Enumeration.enumId! id="enumId_o_"+Enumeration_index/>
				<@displayCell text=Enumeration.sequenceId!/>
				<@displayCell text=Enumeration.enumId! />
				<@inputTextCell name="enumCode" default=Enumeration.enumCode! size=12 maxlength=255 index=Enumeration_index />
				<@inputTextCell name="description" default=Enumeration.description! size=12 maxlength=255 index=Enumeration_index />
				<@inputSelectCell name="nivelId" list=listNivelPresupuestal! key="nivelId" displayField="descripcion" default=Enumeration.nivelId! index=Enumeration_index />
				<@inputAutoCompleteEnumerationCell name="parentEnumId" default=Enumeration.parentEnumId! index=Enumeration_index/>
				<@inputDateCell name="fechaInicio" default=Enumeration.fechaInicio! index=Enumeration_index/>
				<@inputDateCell name="fechaFin" default=Enumeration.fechaFin! index=Enumeration_index />
				<@inputButtonNoSubmitCell title="${uiLabelMap.CommonUpdate}" onClick="actualizaClasificacion('${Enumeration_index}');"/>
            </tr>
            </#list>
        </table>

</@frameSection>