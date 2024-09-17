<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form name="busquedaRequisicionForm" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <table class="twoColumnForm">
    <tbody>
      <tr>
        <@displayTitleCell title=uiLabelMap.ObraObraId/>
        <@inputTextCell name="obraId" maxlength=60  />
      </tr>       
       <tr>
	    <@inputSelectRow title=uiLabelMap.ObraStatus required=false list=estatusList  displayField="descripcion" name="statusId" default=statusId?if_exists />
       </tr>
       <tr>
        <@displayTitleCell title=uiLabelMap.ObraProductId />
        <@inputAutoCompleteProductCell form="busquedaRequisicionForm" name="productId" />
       </tr>
       <tr>
        <@displayTitleCell title=uiLabelMap.ObraNombre/>
        <@inputTextCell name="nombreObra" maxlength=60  />
      </tr>
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
