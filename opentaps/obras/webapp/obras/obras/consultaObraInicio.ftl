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
