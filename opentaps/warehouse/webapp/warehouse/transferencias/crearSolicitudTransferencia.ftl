<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@frameSection title=uiLabelMap.AlmacenCrearSolicitudTransferencia >
<@form name="crearSolicitudTransferencia" url="crearSolicitudTransferencia">
  <table class="twoColumnForm">
    <tbody>
	<@inputDateTimeRow name="fechaContable" title=uiLabelMap.FinancialsAccountigDate 
		default=Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp() titleClass="requiredField"/> 
    <tr>  
      <@displayTitleCell title=uiLabelMap.CommonDescription titleClass="requiredField"/>
      <@inputTextCell name="descripcion" maxlength="255" size=50/>
    </tr>
	<tr>  
      <@inputTextareaRow title=uiLabelMap.WarehouseJustification name="justificacion" titleClass="requiredField"/>
    </tr>
    <tr>  
      <@displayTitleCell title=uiLabelMap.AlmacenOrigen titleClass="requiredField"/>
      <@inputSelectCell key="facilityId" list=listAlmacenes name="almacenOrigenId" displayField="facilityName" required=false/>
    </tr>
    <tr>	
    	<#assign userLoginId = userLogin.userLoginId />
		<#assign userPartyId = userLogin.partyId />
    	<#assign areaId = Static["org.opentaps.purchasing.pedidosInternos.ConsultarPedidoInterno"].obtenerArea(delegator,userLoginId)! />
    	<@inputHidden name="areaPartyId" value=areaId! />
    </tr>
    <tr>        
        <@inputHidden name="organizationPartyId" value="1" index=tag_index />        
    </tr>
	<tr>
		<@inputHidden name="almacenDestinoId" value="${parameters.facilityId}" index=tag_index />
	</tr>
	<tr>
		<@inputHidden name="personaSolicitanteId" value="${userPartyId}" index=tag_index />
	</tr>
	<tr>
		<@inputHidden name="statusId" value="CREADA_ST" index=tag_index />
	</tr>
    <@inputSubmitRow title=uiLabelMap.CommonCreate />  
    </tbody>
  </table>
</@form>
</@frameSection>