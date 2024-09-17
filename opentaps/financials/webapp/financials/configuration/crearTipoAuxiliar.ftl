<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@frameSection title=uiLabelMap.FinancialsCrearTipoAuxiliares>
  <form method="post" action="<@ofbizUrl>crearTipoAuxiliarRequest</@ofbizUrl>" name="crearTipoAuxiliarRequest">
    <table class="twoColumnForm" style="border:0">
    	<tr> <@inputTextRow title=uiLabelMap.FinancialsTipoAuxiliarId name="tipoAuxiliarId" titleClass="requiredField" maxlength=20 size=25 /> </tr>
      	<tr>
      	<td class="titleCell"><span><b><font  size=1 color=#B40404>Entidad</font><b></span> </td>
      	<td>
      	<select name="entidad">
        	<option value="Party">${uiLabelMap.Party}</option>
        	<option value="CuentaBancaria">${uiLabelMap.FinancialsCuentaBancaria}</option>
        	<option value="Product">${uiLabelMap.WarehouseProduct}</option>
      	</select>
      	</td>
      	</tr>
      	<tr> <@inputTextRow title=uiLabelMap.CommonDescription name="descripcion" titleClass="requiredField" maxlength=255 size=50 /> </tr>
      	<tr>
      	<td class="titleCell"><span><b><font  size=1 color=#B40404>Tipo de Auxiliar</font><b></span> </td>
      	<td>
      	<select name="tipoAuxiliar">
      		<option value="N">No es auxiliar</option>
        	<option value="A">${uiLabelMap.FinancialsAcreedor}</option>
        	<option value="D">${uiLabelMap.FinancialsDeudor}</option>
      	</select>
      	</td>
      	</tr>          	     
      	<tr> <@inputSubmitRow title=uiLabelMap.CommonCreate /> </tr>
    </table>
  </form>
</@frameSection>
