<form action="<@ofbizUrl>ActualizaPoliza</@ofbizUrl>" method="post">
	<table>
		<tr>
			<td class="titleCell"><span class="">${uiLabelMap.FormFieldTitle_numeroPoliza} Anterior</span></td>
			<td><input name="numeroPolizaAnt" type="text" class="inputBox" /></td>
		</tr>
		<tr>
			<td class="titleCell"><span class="">${uiLabelMap.FormFieldTitle_numeroPoliza} Nueva</span></td>
			<td><input name="numeroPolizaNueva" type="text" class="inputBox" /></td>
		</tr>
		<tr>
			<td colspan="2" align="center">
				<input type="submit" value="${uiLabelMap.CommonSubmit}" />
			</td>
		</tr>
	</table>
</form>