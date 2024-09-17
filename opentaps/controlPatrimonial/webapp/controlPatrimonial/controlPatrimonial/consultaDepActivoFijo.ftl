<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form name="consultaActivoFijoForm" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <table class="twoColumnForm">
    <tbody>
      <tr>               
        <@displayTitleCell title=uiLabelMap.FormFieldTitleTipoActivoFijo />
        <@inputSelectCell name="fixedAssetTypeId" list=tiposActivo! key="fixedAssetTypeId" required=false displayField="description"/>
      </tr>
      <tr>               
        <@displayTitleCell title=uiLabelMap.FormFieldTitleMes />
        <@inputSelectCell name="mesId" list=meses! key="mesId" required=false displayField="description"/>
      </tr>
      <tr>               
        <@displayTitleCell title=uiLabelMap.FormFieldTitleCiclo />
        <@inputSelectCell name="enumCode" list=ciclos! key="enumCode" required=false displayField="enumCode"/>
      </tr>
      <tr>               
        <@displayTitleCell title=uiLabelMap.FormFieldTitleTipoEvento />
        <@inputSelectCell name="acctgTransTypeId" list=eventos! key="acctgTransTypeId" required=false displayField="descripcion"/>
      </tr>        
      <tr>            
        <@displayTitleCell title=uiLabelMap.FormFieldTitleComentario />
        <@inputTextCell name="comentario" />      
      </tr>  
                            
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
