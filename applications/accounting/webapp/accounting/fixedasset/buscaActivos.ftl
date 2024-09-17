<form action="<@ofbizUrl>ProcesoDepreciacionActivo</@ofbizUrl>" method="post">
	<table>
		
		<tr>	   	         		
     		<td class="titleCell"><span class="">${uiLabelMap.FormFieldTitleTipoActivoFijo}</span></td>     		
     		<td><select name="tipoActivoFijoid" id ="tipoActivoFijoid" size="1">		    	
		    	<option value=" "></option>		    	
		        <#list tiposActivo as tipoActivo>
		        	<option  value="${tipoActivo.fixedAssetTypeId}">${tipoActivo.get("description",locale)}</option>
		        </#list>
		    </select></td>	 
	    </tr>	    		
		<tr>
			<td colspan="2" align="center">
				<input type="submit" value="${uiLabelMap.CommonSubmit}" />
			</td>
		</tr>		
	</table>
</form>