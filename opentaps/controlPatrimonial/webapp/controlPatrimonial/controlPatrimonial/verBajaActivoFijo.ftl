

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#if shipmentPoliza?has_content>
	<#list shipmentPoliza as shipment>
		<#assign transId = shipment.acctgTransId!/>
		<#if transId?has_content>
			<@form name="verPoliza${transId}" url="viewAcctgTrans?acctgTransId=${transId}"/>
				<#assign verPolizaAction>${verPolizaAction!}<@actionForm form="verPoliza${transId}" text=transId /></#assign>
		</#if>
	</#list>
</#if>


<@form name="bajaActivoFijo" url="bajaActivoFijo" >
<#if verPolizaAction?has_content>
	    <div class="subMenuBar">
	    <@selectActionForm name="shipmentActions" prompt=uiLabelMap.FinancialVerPoliza>
	    	${verPolizaAction?if_exists}
		</@selectActionForm>
		</div>
	</#if>
<#if fixedAssetList?has_content>


<#--<#assign linkCodigoBarras>
	<@displayLinkButton href="ProductBarCode.pdf?productId=${fixedAssetList.fixedAssetId?if_exists}&amp;productName=${fixedAssetList.fixedAssetName?if_exists}&amp;dateAcquired=${fixedAssetList.dateAcquired?if_exists}" text=uiLabelMap.ProductBarcode class="subMenuButton" target="_blank"/>
</#assign>
	

<@frameSection title=uiLabelMap.ControlConsultaEdicionActivoFijo extra=linkCodigoBarras >-->
<@frameSection title=uiLabelMap.ControlConsultaEdicionActivoFijo> 		
		<table class="fourColumnForm">		  
		  <tr><td colspan="4" class="formSectionHeaderTitle">${uiLabelMap.Generales}</td></tr>		
		  <tr>
    		<@displayTitleCell title=uiLabelMap.FechaContable titleClass="requiredField" />
    		<@inputDateTimeCell name="fechaContable"  />
  		  </tr>  
		  <@displayRow title=uiLabelMap.ControlFixedAssetId text=fixedAssetList.fixedAssetId?if_exists/>
		  <@inputHidden name="fixedAssetId" value=fixedAssetList.fixedAssetId?if_exists />
		  <@displayRow title=uiLabelMap.ControlFixedAssetType text=TipoActivoFijo.description?if_exists/>
		  <@inputHidden name="fixedAssetTypeId" value=fixedAssetList.fixedAssetTypeId?if_exists />
		  <@inputSelectRow title="Motivo de baja" required=true list=listMotivosAlta displayField="descripcion" name="idMotivo" default=idMotivo?if_exists titleClass="requiredField" />
		  <#if fixedAssetList.statusId!='ACT_FIJO_BAJA'>
		  	<@inputTextRow name="comentario" title=uiLabelMap.Comentario size=30 />
		  <#else>
		  	<@displayRow title=uiLabelMap.Comentario text=fixedAssetList.comentarioBaja?if_exists />
		  </#if>
		  <@displayRow title=uiLabelMap.ControlProduct text=fixedAssetList.instanceOfProductId?if_exists />    
		  <@displayRow title=uiLabelMap.NombreActivo text=fixedAssetList.fixedAssetName?if_exists/>
		  <@inputHidden name="nombreActivo" value=fixedAssetList.fixedAssetName?if_exists />
		  <@displayRow title=uiLabelMap.ControlOrdenCompra text=fixedAssetList.acquireOrderId?if_exists/>
		  <@displayRow title=uiLabelMap.ControlOrdenCompraItemId text=fixedAssetList.acquireOrderItemSeqId?if_exists/>
		  <@displayDateRow title=uiLabelMap.FechaAdquisicion date=fixedAssetList.dateAcquired?if_exists format="DATE"/>
		  <@displayRow title=uiLabelMap.Estatus text=listStatus.descripcion?if_exists />
		  <@displayRow title=uiLabelMap.NumeroSerie text=fixedAssetList.serialNumber?if_exists/>
		  <@displayCurrencyRow title=uiLabelMap.CostoCompra amount=fixedAssetList.purchaseCost?if_exists currencyUomId="MXN"/>
		  <@inputHidden name="monto" value=fixedAssetList.purchaseCost?if_exists />
		  <@displayRow title=uiLabelMap.UbicacionFisica text=listUbicaciones.descripcion?if_exists />
		  <@displayRow title=uiLabelMap.Marca text=fixedAssetList.marca?if_exists />
		  <@displayRow title=uiLabelMap.Modelo text=fixedAssetList.modelo?if_exists />
		  <@displayRow title=uiLabelMap.Proveedor text=fixedAssetList.proveedor?if_exists />
		  <@displayRow title=uiLabelMap.Area text=listArea.groupName?if_exists />
		  <@displayRow title=uiLabelMap.Observaciones text=fixedAssetList.observaciones?if_exists />
		  <@displayRow title=uiLabelMap.Caracteristicas text=fixedAssetList.caracteristicas?if_exists />
		  <@displayRow title=uiLabelMap.DenominacionPartidaGenerica text=fixedAssetList.denominacionPartidaGen?if_exists />
		  <@displayRow title=uiLabelMap.CodigoActivoAnterior text=fixedAssetList.idActivoAnterior?if_exists />
		  <@displayRow title=uiLabelMap.ControlOrganizationPartyId text=fixedAssetList.organizationPartyId?if_exists />
		  <@displayRow title=uiLabelMap.Almacen text=listAlmacen.facilityName?if_exists />			  
		    
		  <tr><td colspan="4">&nbsp;</td></tr>
		  <tr><td colspan="4" class="formSectionHeaderTitle">${uiLabelMap.Servicio}</td></tr>
		  
		  <@displayDateRow title=uiLabelMap.FechaUltimoServicio date=fixedAssetList.dateLastServiced?if_exists format="DATE"/>
		  <@displayDateRow title=uiLabelMap.FechaSiguienteServicio date=fixedAssetList.dateNextService?if_exists format="DATE"/>
		  <@displayDateRow title=uiLabelMap.FechaFinalVidaUtil date=fixedAssetList.expectedEndOfLife?if_exists format="DATE"/>
		  <@displayDateRow title=uiLabelMap.FechaRealVidaUtil date=fixedAssetList.actualEndOfLife?if_exists format="DATE"/>
		  <@displayDateRow title=uiLabelMap.FechaVencimientoGarantia date=fixedAssetList.fechaVencGarantia?if_exists format="DATE"/>

		  <@displayRow title=uiLabelMap.EstadoFisico text=listEstadoF.descripcion?if_exists />
		  <@displayRow title=uiLabelMap.NumeroFactura text=fixedAssetList.numeroFactura?if_exists />
		  <@displayRow title=uiLabelMap.ControlPolizaSeguro text=fixedAssetList.numeroPoliza?if_exists />
		  <@displayDateRow title=uiLabelMap.FechaInicioPolizaSeguro date=fixedAssetList.fechaIniPoliza?if_exists format="DATE"/>
		  <@displayDateRow title=uiLabelMap.FechaFinPolizaSeguro date=fixedAssetList.fechaFinPoliza?if_exists format="DATE"/>
		  <@displayRow title=uiLabelMap.AniosVidaUtil text=fixedAssetList.aniosVidaUtil?if_exists />
		  		  
		  <tr><td colspan="4">&nbsp;</td></tr>
		  <tr><td colspan="4" class="formSectionHeaderTitle">${uiLabelMap.Automoviles}</td></tr>
		  
		  <@displayRow title=uiLabelMap.Placa text=fixedAssetList.placa?if_exists />
		  <@displayRow title=uiLabelMap.TipoUnidad text=listUnidadV.descripcion?if_exists />    
		  	  		  
		  <tr><td colspan="4">&nbsp;</td></tr>
		  <tr><td colspan="4" class="formSectionHeaderTitle">${uiLabelMap.Depreciacion}</td></tr>      
		  
		  <@displayRow title=uiLabelMap.ValorSalvamento text=fixedAssetList.salvageValue?if_exists />
		  <@displayRow title=uiLabelMap.Depreciacion text=fixedAssetList.depreciation?if_exists />
		  <@inputHidden name="montoDepreciacion" value=fixedAssetList.depreciation?if_exists />
		  		  
		  <tr><td colspan="4">&nbsp;</td></tr>
		  <tr><td colspan="4" class="formSectionHeaderTitle">${uiLabelMap.Inmuebles}</td></tr>    
		  
		  <@displayRow title=uiLabelMap.Domicilio text=fixedAssetList.domicilio?if_exists />
		  <@displayRow title=uiLabelMap.Municipio text=fixedAssetList.municipio?if_exists />
		  <@displayRow title=uiLabelMap.Localidad text=fixedAssetList.localidad?if_exists />
		  <@displayRow title=uiLabelMap.Ejido text=fixedAssetList.ejido?if_exists />
		  <@displayRow title=uiLabelMap.TipoTerreno text=fixedAssetList.tipoTerreno?if_exists />
		  <@displayRow title=uiLabelMap.TipoDocLegalPropiedad text=fixedAssetList.tipoDocumentoLegalPropiedad?if_exists />
		  <@displayRow title=uiLabelMap.DescDocLegalPropiedad text=fixedAssetList.descDocumentoLegalPropiedad?if_exists />
		  <@displayRow title=uiLabelMap.OrigenAdquisicion text=fixedAssetList.origenAdquisicion?if_exists />
		  <@displayRow title=uiLabelMap.FormaAdquisicion text=fixedAssetList.formaAdquisicion?if_exists />
		  <@displayRow title=uiLabelMap.TipoEmisorTituloPropiedad text=fixedAssetList.tipoEmisorTituloPropiedad?if_exists />
		  <@displayRow title=uiLabelMap.ValorContruccion text=fixedAssetList.valorContruccion?if_exists />
		  <@displayRow title=uiLabelMap.ValorRazonable text=fixedAssetList.valorRazonable?if_exists />
		  <@displayDateRow title=uiLabelMap.FechaAvaluo date=fixedAssetList.fechaAvaluo?if_exists format="DATE"/>
		  <@displayDateRow title=uiLabelMap.FechaTituloPropiedad date=fixedAssetList.fechaTituloPropiedad?if_exists format="DATE"/>
		  <@displayRow title=uiLabelMap.ClaveCatastral text=fixedAssetList.claveCatastral?if_exists />
		  <@displayDateRow title=uiLabelMap.FechaInicioClaveCatastral date=fixedAssetList.fechaInicioClaveCatastral?if_exists format="DATE"/>
		  <@displayDateRow title=uiLabelMap.FechaVenciClaveCatastral date=fixedAssetList.vencimientoClaveCatastral?if_exists format="DATE"/>
		  <@displayRow title=uiLabelMap.SuperficieTerreno text=fixedAssetList.superficieTerreno?if_exists />
		  <@displayRow title=uiLabelMap.SuperficieConstruccion text=fixedAssetList.superficieConstruccion?if_exists />
		  <@displayRow title=uiLabelMap.SuperficieDisponible text=fixedAssetList.superficieDisponible?if_exists />
		  <@displayRow title=uiLabelMap.FolioRegPubliPropieComercio text=fixedAssetList.folioRppc?if_exists />
		  <@displayDateRow title=uiLabelMap.FechaInscRegPropieComerc date=fixedAssetList.fechaInscripcionRppc?if_exists format="DATE"/>
		  <@displayRow title=uiLabelMap.CiudadInscRegPubPropComer text=fixedAssetList.ciudadInscripcionRPPC?if_exists />
		  <@displayDateRow title=uiLabelMap.FechaIncorInventario date=fixedAssetList.fechaIncorporacionInventario?if_exists format="DATE"/>
		  
		  <@inputSubmitRow title="Aplicar Baja" />
		  	  		
		</table>
</@frameSection>			
</#if>
</@form>

