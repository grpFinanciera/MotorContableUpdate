

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


<@form name="editarActivoFijo" url="editarActivoFijo" >
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
		  <@displayRow title=uiLabelMap.ControlFixedAssetId text=fixedAssetList.fixedAssetId?if_exists/>
		  <@inputHidden name="fixedAssetId" value=fixedAssetList.fixedAssetId?if_exists />
		  <@inputSelectRow title=uiLabelMap.ControlFixedAssetType required=false list=listTipoActivoFijo displayField="description" name="fixedAssetTypeId" default=fixedAssetList.fixedAssetTypeId?if_exists titleClass="requiredField" />  
		  
		  <@inputAutoCompleteProductRow name="productId" title=uiLabelMap.ControlProduct default=fixedAssetList.instanceOfProductId?if_exists titleClass="requiredField"/>    
		  
		  <@inputTextRow name="nombreActivo" title=uiLabelMap.NombreActivo size=30 titleClass="requiredField" default=fixedAssetList.fixedAssetName?if_exists/>
		  <@inputTextRow name="orderId" title=uiLabelMap.ControlOrdenCompra size=30 default=fixedAssetList.acquireOrderId?if_exists/>
		  <@inputTextRow name="orderItemId" title=uiLabelMap.ControlOrdenCompraItemId size=30 default=fixedAssetList.acquireOrderItemSeqId?if_exists/>
		  <tr>
		    <@displayTitleCell title=uiLabelMap.FechaAdquisicion titleClass="requiredField"/>
		    <@inputDateCell name="fechaAdquisicion" default=fixedAssetList.dateAcquired?if_exists />
		  </tr>  
		  <@inputSelectRow title=uiLabelMap.Estatus required=false list=listStatus displayField="descripcion" name="statusId" default=fixedAssetList.statusId />
		  <@inputTextRow name="numeroSerie" title=uiLabelMap.NumeroSerie size=30 default=fixedAssetList.serialNumber?if_exists/>
		  <@inputCurrencyRow name="monto" title=uiLabelMap.CostoCompra disableCurrencySelect=true titleClass="requiredField" default=fixedAssetList.purchaseCost?if_exists/>
		  <tr>  
		  	<@displayTitleCell title=uiLabelMap.PurchMoneda titleClass="requiredField"/>
		    <@inputSelectCell key="uomId" list=listMonedas name="moneda" displayField="uomId" default="MXN"/>
		  </tr>
		  
		  <@inputSelectRow title=uiLabelMap.UbicacionFisica required=false list=listUbicacionRapida displayField="descripcion" name="ubicacionRapidaId" default=fixedAssetList.ubicacionRapidaId?if_exists titleClass="requiredField" />
		  
		  <@inputTextRow name="marca" title=uiLabelMap.Marca size=30 default=fixedAssetList.marca?if_exists />
		  <@inputTextRow name="modelo" title=uiLabelMap.Modelo size=30 default=fixedAssetList.modelo?if_exists />
		  
		  <@inputAutoCompletePartyRow title=uiLabelMap.Proveedor name="proveedorId" id="proveedorId" size="20" default=fixedAssetList.proveedor?if_exists />  
		  <@inputSelectRow title=uiLabelMap.Area required=false list=listAreas displayField="groupName" name="partyId" default=fixedAssetList.areaPartyId?if_exists titleClass="requiredField" />	
		  <@inputTextRow name="observaciones" title=uiLabelMap.Observaciones size=30 default=fixedAssetList.observaciones?if_exists />
		  <@inputTextRow name="caracteristicas" title=uiLabelMap.Caracteristicas size=30 default=fixedAssetList.caracteristicas?if_exists />
		  <@inputTextRow name="partidaGenerica" title=uiLabelMap.DenominacionPartidaGenerica size=30 default=fixedAssetList.denominacionPartidaGen?if_exists />
		  <@inputTextRow name="fixedAssetIdAnterior" title=uiLabelMap.CodigoActivoAnterior size=30 default=fixedAssetList.idActivoAnterior?if_exists />
		  <@inputAutoCompletePartyRow name="organizationId" title=uiLabelMap.ControlOrganizationPartyId default=fixedAssetList.organizationPartyId?if_exists titleClass="requiredField"/>
		  <@inputSelectRow title=uiLabelMap.Almacen required=false list=listFacility displayField="facilityName" name="facilityId" default=fixedAssetList.facilityId?if_exists titleClass="requiredField" />
		  
		  
		  
		  <tr><td colspan="4">&nbsp;</td></tr>
		  <tr><td colspan="4" class="formSectionHeaderTitle">${uiLabelMap.Resguardo}</td></tr>
		  <@inputAutoCompletePartyRow name="resguardante" title=uiLabelMap.Resguardante default=fixedAssetList.partyId?if_exists/>
		  <tr>
		    <@displayTitleCell title=uiLabelMap.FechaInicioResguardo />
		    <@inputDateTimeCell name="fechaInicioResguardo"  default=listResguardante2.fromDate?if_exists />
		  </tr>
		  <tr>
		    <@displayTitleCell title=uiLabelMap.FechaFinResguardo />
		    <@inputDateTimeCell name="fechaFinResguardo"  default=listResguardante2.thruDate?if_exists />
		  </tr>
		  <tr>
		    <@displayTitleCell title=uiLabelMap.FechaAsignacion />
		    <@inputDateTimeCell name="fechaAsignacion"  default=listResguardante2.allocatedDate?if_exists />
		  <tr>    
    		<@inputTextRow name="comentariosAsignacion" title=uiLabelMap.comentariosAsignacion size=30 default=listResguardante2.comments />
  		</tr> 
		     
		  
		  
		    
		  <tr><td colspan="4">&nbsp;</td></tr>
		  <tr><td colspan="4" class="formSectionHeaderTitle">${uiLabelMap.Servicio}</td></tr>
		  <tr>
		    <@displayTitleCell title=uiLabelMap.FechaUltimoServicio />
		    <@inputDateCell name="fechaUltimoServicio"  default=fixedAssetList.dateLastServiced?if_exists />
		  </tr>
		  <tr>
		    <@displayTitleCell title=uiLabelMap.FechaSiguienteServicio />
		    <@inputDateCell name="fechaSiguienteServicio"  default=fixedAssetList.dateNextService?if_exists />
		  </tr>
		  <tr>
		    <@displayTitleCell title=uiLabelMap.FechaFinalVidaUtil />
		    <@inputDateCell name="fechaFinalVidaUtil"  default=fixedAssetList.expectedEndOfLife?if_exists />
		  </tr>
		  <tr>
		    <@displayTitleCell title=uiLabelMap.FechaRealVidaUtil />
		    <@inputDateCell name="fechaRealVidaUtil"  default=fixedAssetList.actualEndOfLife?if_exists />
		  </tr>
		  <tr>
		    <@displayTitleCell title=uiLabelMap.FechaVencimientoGarantia />
		    <@inputDateCell name="fechaVencimientoGarantia"  default=fixedAssetList.fechaVencGarantia?if_exists />
		  </tr>
		  <@inputSelectRow title=uiLabelMap.EstadoFisico required=false list=listEstadoFisico displayField="descripcion" name="edoFisicoId" default=fixedAssetList.edoFisicoId?if_exists />
		  <@inputTextRow name="numeroFactura" title=uiLabelMap.NumeroFactura size=30 maxlength=20 default=fixedAssetList.numeroFactura?if_exists />
		  <@inputTextRow name="numeroPoliza" title=uiLabelMap.ControlPolizaSeguro size=30 maxlength=20 default=fixedAssetList.numeroPoliza?if_exists />
		  <tr>
		    <@displayTitleCell title=uiLabelMap.FechaInicioPolizaSeguro />
		    <@inputDateCell name="fechaInicioPoliza"   default=fixedAssetList.fechaIniPoliza?if_exists />
		  </tr>
		  <tr>
		    <@displayTitleCell title=uiLabelMap.FechaFinPolizaSeguro />
		    <@inputDateCell name="fechaFinPoliza"   default=fixedAssetList.fechaFinPoliza?if_exists />
		  </tr>
		  <@inputTextRow name="aniosVidaUtil" title=uiLabelMap.AniosVidaUtil size=30 maxlength=20 default=fixedAssetList.aniosVidaUtil?if_exists />
		  
		  
		  
		  <tr><td colspan="4">&nbsp;</td></tr>
		  <tr><td colspan="4" class="formSectionHeaderTitle">${uiLabelMap.Automoviles}</td></tr>    
		  <@inputTextRow name="placa" title=uiLabelMap.Placa size=30 maxlength=20 default=fixedAssetList.placa?if_exists />
		  <@inputSelectRow title=uiLabelMap.TipoUnidad required=false list=listTipoUnidadVehiculo  displayField="descripcion" name="tipoUnidadVehiculoId" default=fixedAssetList.tipoUnidadVehiculoId?if_exists />
		  		
		  		  
		  <tr><td colspan="4">&nbsp;</td></tr>
		  <tr><td colspan="4" class="formSectionHeaderTitle">${uiLabelMap.Depreciacion}</td></tr>      
		  <@inputCurrencyRow name="valorSalvamento" title=uiLabelMap.ValorSalvamento disableCurrencySelect=true default=fixedAssetList.salvageValue?if_exists />
		  <@inputCurrencyRow name="depreciacion" title=uiLabelMap.Depreciacion disableCurrencySelect=true default=fixedAssetList.depreciation?if_exists />  
		  
		  
		  
		  <tr><td colspan="4">&nbsp;</td></tr>
		  <tr><td colspan="4" class="formSectionHeaderTitle">${uiLabelMap.Inmuebles}</td></tr>    
		  <@inputTextRow name="domicilio" title=uiLabelMap.Domicilio size=30 maxlength=20 default=fixedAssetList.domicilio?if_exists />
		  <@inputTextRow name="municipio" title=uiLabelMap.Municipio size=30 maxlength=20 default=fixedAssetList.municipio?if_exists />
		  <@inputTextRow name="localidad" title=uiLabelMap.Localidad size=30 maxlength=20 default=fixedAssetList.localidad?if_exists />
		  <@inputTextRow name="ejido" title=uiLabelMap.Ejido size=30 maxlength=20 default=fixedAssetList.ejido?if_exists />
		  <@inputTextRow name="tipoTerreno" title=uiLabelMap.TipoTerreno size=30 maxlength=20 default=fixedAssetList.tipoTerreno?if_exists />
		  <@inputTextRow name="tipoDocLegalPropiedad" title=uiLabelMap.TipoDocLegalPropiedad size=30 maxlength=20 default=fixedAssetList.tipoDocumentoLegalPropiedad?if_exists />
		  <@inputTextRow name="descDocLegalPropiedad" title=uiLabelMap.DescDocLegalPropiedad size=30 maxlength=20 default=fixedAssetList.descDocumentoLegalPropiedad?if_exists />
		  <@inputTextRow name="origenAdquisicion" title=uiLabelMap.OrigenAdquisicion size=30 maxlength=20 default=fixedAssetList.origenAdquisicion?if_exists />
		  <@inputTextRow name="formaAdquisicion" title=uiLabelMap.FormaAdquisicion size=30 maxlength=20 default=fixedAssetList.formaAdquisicion?if_exists />
		  <@inputTextRow name="tipoEmisorTituloPropiedad" title=uiLabelMap.TipoEmisorTituloPropiedad size=30 maxlength=20 default=fixedAssetList.tipoEmisorTituloPropiedad?if_exists />
		  <@inputCurrencyRow name="valorContruccion" title=uiLabelMap.ValorContruccion disableCurrencySelect=true default=fixedAssetList.valorContruccion?if_exists />
		  <@inputCurrencyRow name="valorRazonable" title=uiLabelMap.ValorRazonable disableCurrencySelect=true default=fixedAssetList.valorRazonable?if_exists />
		  <tr>
		    <@displayTitleCell title=uiLabelMap.FechaAvaluo />
		    <@inputDateCell name="fechaAvaluo"   default=fixedAssetList.fechaAvaluo?if_exists />
		  </tr>
		  <tr>
		    <@displayTitleCell title=uiLabelMap.FechaTituloPropiedad />
		    <@inputDateCell name="fechaTituloPropiedad"   default=fixedAssetList.fechaTituloPropiedad?if_exists />
		  </tr>
		  <@inputTextRow name="claveCatastral" title=uiLabelMap.ClaveCatastral size=30 maxlength=20 default=fixedAssetList.claveCatastral?if_exists />
		  <tr>
		    <@displayTitleCell title=uiLabelMap.FechaInicioClaveCatastral />
		    <@inputDateCell name="fechaInicioClaveCatastral"   default=fixedAssetList.fechaInicioClaveCatastral?if_exists />
		  </tr>
		  <tr>
		    <@displayTitleCell title=uiLabelMap.FechaVenciClaveCatastral />
		    <@inputDateCell name="fechaVenciClaveCatastral"   default=fixedAssetList.vencimientoClaveCatastral?if_exists />
		  </tr>
		  <@inputCurrencyRow name="superficieTerreno" title=uiLabelMap.SuperficieTerreno disableCurrencySelect=true default=fixedAssetList.superficieTerreno?if_exists />
		  <@inputCurrencyRow name="superficieConstruccion" title=uiLabelMap.SuperficieConstruccion disableCurrencySelect=true default=fixedAssetList.superficieConstruccion?if_exists />
		  <@inputCurrencyRow name="superficieDisponible" title=uiLabelMap.SuperficieDisponible disableCurrencySelect=true default=fixedAssetList.superficieDisponible?if_exists />
		  <@inputTextRow name="folioRegPubliPropieComercio" title=uiLabelMap.FolioRegPubliPropieComercio size=30 maxlength=20 default=fixedAssetList.folioRppc?if_exists />
		  <tr>
		    <@displayTitleCell title=uiLabelMap.FechaInscRegPropieComerc />
		    <@inputDateCell name="fechaInscRegPropieComer"   default=fixedAssetList.fechaInscripcionRppc?if_exists />
		  </tr>
		  <@inputTextRow name="ciudadInscRegPubPropComer" title=uiLabelMap.CiudadInscRegPubPropComer size=30 maxlength=20 default=fixedAssetList.ciudadInscripcionRPPC?if_exists />
		  <tr>
		    <@displayTitleCell title=uiLabelMap.FechaIncorInventario />
		    <@inputDateCell name="fechaIncorInventario"   default=fixedAssetList.fechaIncorporacionInventario?if_exists />
		  </tr>
		  <#if fixedAssetList.statusId!='ACT_FIJO_BAJA'>
		  <@inputSubmitRow title=uiLabelMap.ControlGuardarCambios />
		  </#if>	  		
		</table>
</@frameSection>			
</#if>
</@form>

