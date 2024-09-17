<form action="<@ofbizUrl>ProcesoDepreciacionActivo</@ofbizUrl>" method="post">
	<table>
		
		<tr>	   	         		
     		<td class="titleCell"><span class="">${uiLabelMap.FormFieldTitleTipoActivoFijo}</span></td>     		
     		<td><select name="tipoActivoFijoId" id ="tipoActivoFijoId" size="1">		    	
		    	<option value=" "></option>		    	
		        <#list tiposActivo as tipoActivo>
		        	<option  value="${tipoActivo.fixedAssetTypeId}">${tipoActivo.get("description",locale)}</option>
		        </#list>
		    </select></td>	 
	    </tr>
	    <input type="hidden" name='organizationPartyId' value="${organizationPartyId?if_exists}" />
	    <tr>	   	         		
     		<td class="titleCell"><span class="">${uiLabelMap.FormFieldTitleMes}</span></td>     		
     		<td>
     			<select name="mesId" id ="mesId" size="1">		    	
		    		<option value=" "></option>		    	
		        	<#list meses as mes>
		        		<option  value="${mes.mesId}">${mes.get("description",locale)}</option>
		        	</#list>
		    	</select>
		    </td>
		    <td class="titleCell"><span class="">${uiLabelMap.FormFieldTitleCiclo}</span></td>     		
     		<td>
     			<select name="cicloId" id ="cicloId" size="1">		    	
		    		<option value=" "></option>		    	
		        	<#list ciclos as ciclo>
		        		<option  value="${ciclo.enumCode}">${ciclo.get("enumCode",locale)}</option>
		        	</#list>
		    	</select>
		    </td>	 
	    </tr>
	    <tr>
        	<td class="titleCell"><span class="">${uiLabelMap.FormFieldTitleTipoEvento}</span></td>
    		</td>
	    	<td>	   	    
     			<select name="acctgTransTypeId" size="1">
		    		<option value=" "></option>
		        	<#list eventos as evento>
		        		<option  value="${evento.acctgTransTypeId}">${evento.get("descripcion",locale)}</option>
		        	</#list>
		    	</select>	 
	    	</td>
      </tr>
		<tr>
			<td class="titleCell"><span class="">${uiLabelMap.FormFieldTitleComentario}</span></td>
			<td><input name="comentario" type="text" class="inputBox" size="60"  /></td>
		</tr>		
		<tr>
			<td colspan="2" align="center">
				<input type="submit" value="${uiLabelMap.CommonSubmit}" />
			</td>
		</tr>
	</table>
</form>