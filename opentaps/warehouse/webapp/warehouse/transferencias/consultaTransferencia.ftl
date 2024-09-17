<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form name="busquedaTransferencia" method="post" action="">
	<@inputHidden name="performFind" value="Y"/>
	<table class="twoColumnForm">
	<tbody>
		<@inputTextRow name="solicitudTransferenciaId" title=uiLabelMap.AlmacenSolicitudTransferenciaId />
		<#assign filtros = {"Propias":"Propias"}/>
		<@inputSelectHashRow name="filtroId" title=uiLabelMap.AlmacenTransferenciaFiltro hash=filtros required=false />
		<@inputSelectRow title=uiLabelMap.WarehouseStatus required=false list=estatusList?default([])
						 	displayField="descripcion" name="statusId" default=statusId?if_exists />
        <@inputAutoCompleteProductRow title=uiLabelMap.WarehouseProduct form="busquedaPedidoInternoForm" name="productId" />
		<@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>