<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form name="consultaActivoFijoForm" method="post" action="">
  <@inputHidden name="performFind" value="Y"/>
  <table class="twoColumnForm">
    <tbody>
      <tr>
        <@displayTitleCell title=uiLabelMap.ControlFixedAssetId />
        <@inputTextCell name="fixedAssetId" />
        <@displayTitleCell title=uiLabelMap.ControlFixedAssetType />
        <@inputSelectCell name="fixedAssetTypeId" list=listFixedAssetType key="fixedAssetTypeId" required=false displayField="description"/>
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.ControlProduct />
        <@inputAutoCompleteProductCell form="consultaActivoFijoForm" name="productId" />
        <@displayTitleCell title=uiLabelMap.ControlResguardante />
        <@inputAutoCompletePartyCell id="resguardante" name="resguardante" />        
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.ControlNombre />
        <@inputTextCell name="nombreActivo" />
        <@displayTitleCell title=uiLabelMap.ControlNumeroSerie />
        <@inputTextCell name="numeroSerie" />        
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.ControlEstadoFisico />
        <@inputSelectCell name="edoFisicoId" list=listEstadoFisico key="edoFisicoId" required=false displayField="descripcion"/>
        <@displayTitleCell title=uiLabelMap.ControlUbicacionFisica />
        <@inputSelectCell name="ubicacionRapidaId" list=listUbicacionRapida key="ubicacionRapidaId" required=false displayField="descripcion"/>
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.ControlMarca />
        <@inputTextCell name="marca" />
        <@displayTitleCell title=uiLabelMap.ControlModelo />
        <@inputTextCell name="modelo" />        
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.ControlProveedor />
        <@inputAutoCompletePartyCell id="proveedorId" name="proveedorId" />
        <@displayTitleCell title=uiLabelMap.ControlNumeroFactura />
        <@inputTextCell name="numeroFactura" />        
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.ControlOrganizationPartyId />
        <@inputAutoCompletePartyCell name="organizationId"/>
        <@displayTitleCell title=uiLabelMap.ControlPolizaSeguro />
        <@inputTextCell name="numeroPoliza" />        
      </tr>
      <tr>        
        <@displayTitleCell title=uiLabelMap.ControlArea />
        <@inputSelectCell name="partyId" list=listAreas key="partyId" required=false displayField="groupName" />
        <@displayTitleCell title=uiLabelMap.ControlOrdenCompra />
        <@inputTextCell name="orderId" />        
      </tr>
      <tr>        
        <@displayTitleCell title=uiLabelMap.ControlPlaca />
        <@inputTextCell name="placa" />        
        <@displayTitleCell title=uiLabelMap.CodigoActivoAnterior />
        <@inputTextCell name="idActivoAnterior" />        
      </tr>             
  	  <tr><td colspan="4" class="formSectionHeaderTitle">${uiLabelMap.ControlIndicacionesImpresion}</td></tr>
  	  
  	  <tr>        
        <@displayTitleCell title=uiLabelMap.TamanoImpresion titleClass="requiredField" />
        <@inputSelectCell name="idTamano" list=paginaList key="idTamano" required=true displayField="medida" />
      </tr>
      	<@inputHidden name="posicionImpresion" value="1"/> 
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
