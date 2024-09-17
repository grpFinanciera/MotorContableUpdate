<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form name="consultaMatrizForm" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <table class="twoColumnForm">
  
    <tbody>    	
      <@inputSelectRow title=uiLabelMap.TipoMatriz required=true list=tipoList displayField="acctgTagUsageTypeId" name="acctgTagUsageTypeId" key="acctgTagUsageTypeId" default=tipoMatriz?if_exists/>      
      <@inputSelectRow title=uiLabelMap.MatrizId required=false list=matrizList  displayField="descripcion" name="matrizId" key="tipoMatrizId" />
            
      <tr>
      <@displayTitleCell title=uiLabelMap.Cog />
      <@inputTextCell name="cog" size="20" maxlength="20"/>
      </tr>
      <tr>
      <@displayTitleCell title=uiLabelMap.Cri />
      <@inputTextCell name="cri" size="20" maxlength="20"/>
      </tr>
      <tr>
      <@displayTitleCell title=uiLabelMap.NombreCog />
      <@inputTextCell name="nombreCog" size="20"/>
      </tr>
      <tr>
      <@displayTitleCell title=uiLabelMap.NombreCri />
      <@inputTextCell name="nombreCri" size="20"/>
      </tr>
      <tr>
      <@displayTitleCell title=uiLabelMap.TipoGasto />
      <@inputTextCell name="tipoGasto" size="20" maxlength="100"/>
      </tr>      
	  
	  <tr>						
	  <@displayTitleCell title=uiLabelMap.CuentaCargo />
	  <@inputAutoCompleteGlAccountCell name="cargo" url="getAutoCompleteGlAccountsRegistro" />
	  </tr>
	  <tr>
	  <@displayTitleCell title=uiLabelMap.CuentaAbono />
      <@inputAutoCompleteGlAccountCell name="abono" url="getAutoCompleteGlAccountsRegistro" />
      </tr>
      					
            
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
