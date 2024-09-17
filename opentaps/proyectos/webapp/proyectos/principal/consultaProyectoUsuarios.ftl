<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form name="busquedaProyectoForm" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <table class="twoColumnForm">
    <tbody>
      <tr>
        <@displayTitleCell title="Fecha" />
        <@inputDateCell name="fechaContable" form="" default="" />
     </tr> 
      <tr>
        <@displayTitleCell title="Codigo de proyecto"/>
        <@inputTextCell name="codigoProyecto" maxlength=60  />
      </tr>
      <tr>
        <@displayTitleCell title="Id de proyecto"/>
        <@inputTextCell name="proyectoId" maxlength=60  />
      </tr>
       <tr>
       <@inputSelectRow title=uiLabelMap.UnidadResponsable required=false list=listAreas displayField="groupName" name="partyId" default=partyId?if_exists />
       </tr>
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>