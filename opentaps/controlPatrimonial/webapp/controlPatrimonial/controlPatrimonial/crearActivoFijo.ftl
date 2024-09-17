

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form name="guardaActivoFijo" method="post" action="<@ofbizUrl>guardaActivoFijo</@ofbizUrl>">

<table class="fourColumnForm">

  <tr><td colspan="4" class="formSectionHeaderTitle">${uiLabelMap.Generales}</td></tr>
  
  <tr>
    <@displayTitleCell title=uiLabelMap.FechaContable titleClass="requiredField" />
    <@inputDateTimeCell name="fechaContable"  />
  </tr>
  
  <@inputSelectRow title=uiLabelMap.ControlFixedAssetType required=true list=listTipoActivoFijo displayField="description" name="fixedAssetTypeId" default=fixedAssetTypeId?if_exists titleClass="requiredField" />

  <@inputSelectRow title="Motivo de alta" required=true list=listMotivosAlta displayField="descripcion" name="idMotivo" default=idMotivo?if_exists titleClass="requiredField" />
  
  <@inputAutoCompleteProductRow name="productId" title=uiLabelMap.ControlProduct default=""! titleClass="requiredField"/>    
  
  <@inputTextRow name="nombreActivo" title=uiLabelMap.NombreActivo size=30 titleClass="requiredField"/>
  <#--<@inputTextRow name="orderId" title=uiLabelMap.ControlOrdenCompra size=30/>
  <@inputTextRow name="orderItemId" title=uiLabelMap.ControlOrdenCompraItemId size=30/>-->
  <tr>
    <@displayTitleCell title=uiLabelMap.FechaAdquisicion titleClass="requiredField" />
    <@inputDateTimeCell name="fechaAdquisicion"  />
  </tr>  
  <@inputSelectRow title=uiLabelMap.Estatus required=true list=listStatus displayField="descripcion" name="statusId" default=statusId?if_exists titleClass="requiredField"/>
  <@inputTextRow name="numeroSerie" title=uiLabelMap.NumeroSerie size=30/>
  <@inputCurrencyRow name="monto" title=uiLabelMap.CostoCompra disableCurrencySelect=true titleClass="requiredField"/>
  <tr>  
  	<@displayTitleCell title=uiLabelMap.PurchMoneda titleClass="requiredField"/>
    <@inputSelectCell key="uomId" list=listMonedas name="moneda" displayField="uomId" default="MXN"/>
  </tr>
  
  <@inputSelectRow title=uiLabelMap.ControlUbicacionFisica required=false list=listUbicacionRapida displayField="descripcion" name="ubicacionRapidaId" default=ubicacionRapidaId?if_exists titleClass="requiredField" />
  
  <@inputTextRow name="marca" title=uiLabelMap.Marca size=30/>
  <@inputTextRow name="modelo" title=uiLabelMap.Modelo size=30/>
  
  <@inputAutoCompletePartyRow title=uiLabelMap.ControlProveedor name="proveedorId" id="proveedorId" size="20" />  
  <@inputSelectRow title=uiLabelMap.ControlArea required=false list=listAreas displayField="groupName" name="partyId" default=partyId?if_exists titleClass="requiredField" />	
  <@inputTextRow name="observaciones" title=uiLabelMap.Observaciones size=30 />
  <@inputTextRow name="caracteristicas" title=uiLabelMap.Caracteristicas size=30/>
  <@inputTextRow name="partidaGenerica" title=uiLabelMap.DenominacionPartidaGenerica size=30 />
  <@inputTextRow name="fixedAssetIdAnterior" title=uiLabelMap.CodigoActivoAnterior size=30 />
  <@inputAutoCompletePartyRow name="organizationId" title=uiLabelMap.ControlOrganizationPartyId default=""! titleClass="requiredField"/>
  <@inputSelectRow title=uiLabelMap.Almacen required=false list=listFacility displayField="facilityName" name="facilityId" default=facilityId?if_exists />
  
  
  
  <tr><td colspan="4">&nbsp;</td></tr>
  <tr><td colspan="4" class="formSectionHeaderTitle">${uiLabelMap.Resguardo}</td></tr>
  <@inputAutoCompletePartyRow name="resguardante" title=uiLabelMap.ControlResguardante default=""!/>
  <tr>
    <@displayTitleCell title=uiLabelMap.FechaInicioResguardo />
    <@inputDateTimeCell name="fechaInicioResguardo"  />
  </tr>
  <tr>
    <@displayTitleCell title=uiLabelMap.FechaFinResguardo />
    <@inputDateTimeCell name="fechaFinResguardo"  />
  </tr>
  <tr>
    <@displayTitleCell title=uiLabelMap.FechaAsignacion />
    <@inputDateTimeCell name="fechaAsignacion"  />
  </tr>
  <tr>    
    <@inputTextRow name="comentariosAsignacion" title=uiLabelMap.comentariosAsignacion size=30 />
  </tr>    
  
  
    
  <tr><td colspan="4">&nbsp;</td></tr>
  <tr><td colspan="4" class="formSectionHeaderTitle">${uiLabelMap.Servicio}</td></tr>
  <tr>
    <@displayTitleCell title=uiLabelMap.FechaUltimoServicio />
    <@inputDateTimeCell name="fechaUltimoServicio"  />
  </tr>
  <tr>
    <@displayTitleCell title=uiLabelMap.FechaSiguienteServicio />
    <@inputDateTimeCell name="fechaSiguienteServicio"  />
  </tr>
  <tr>
    <@displayTitleCell title=uiLabelMap.FechaFinalVidaUtil />
    <@inputDateTimeCell name="fechaFinalVidaUtil"  />
  </tr>
  <tr>
    <@displayTitleCell title=uiLabelMap.FechaRealVidaUtil />
    <@inputDateTimeCell name="fechaRealVidaUtil"  />
  </tr>
  <tr>
    <@displayTitleCell title=uiLabelMap.FechaVencimientoGarantia />
    <@inputDateTimeCell name="fechaVencimientoGarantia"  />
  </tr>
  <@inputSelectRow title=uiLabelMap.ControlEstadoFisico required=false list=listEstadoFisico displayField="descripcion" name="edoFisicoId" default=edoFisicoId?if_exists />
  <@inputTextRow name="numeroFactura" title=uiLabelMap.NumeroFactura size=30 />
  <@inputTextRow name="numeroPoliza" title=uiLabelMap.ControlPolizaSeguro size=30 />
  <tr>
    <@displayTitleCell title=uiLabelMap.FechaInicioPolizaSeguro />
    <@inputDateTimeCell name="fechaInicioPoliza"  />
  </tr>
  <tr>
    <@displayTitleCell title=uiLabelMap.FechaFinPolizaSeguro />
    <@inputDateTimeCell name="fechaFinPoliza"  />
  </tr>
  <@inputTextRow name="aniosVidaUtil" title=uiLabelMap.AniosVidaUtil size=30 />
  
  
  
  <tr><td colspan="4">&nbsp;</td></tr>
  <tr><td colspan="4" class="formSectionHeaderTitle">${uiLabelMap.Automoviles}</td></tr>    
  <@inputTextRow name="placa" title=uiLabelMap.Placa size=30 />
  <@inputSelectRow title=uiLabelMap.TipoUnidad required=false list=listTipoUnidadVehiculo  displayField="descripcion" name="tipoUnidadVehiculoId" default=tipoUnidadVehiculoId?if_exists />
  
    
  
  <tr><td colspan="4">&nbsp;</td></tr>
  <tr><td colspan="4" class="formSectionHeaderTitle">${uiLabelMap.Depreciacion}</td></tr>      
  <@inputCurrencyRow name="valorSalvamento" title=uiLabelMap.ValorSalvamento disableCurrencySelect=true/>
  <@inputCurrencyRow name="depreciacion" title=uiLabelMap.Depreciacion disableCurrencySelect=true/>  
  
  
  
  <tr><td colspan="4">&nbsp;</td></tr>
  <tr><td colspan="4" class="formSectionHeaderTitle">${uiLabelMap.Inmuebles}</td></tr>    
  <@inputTextRow name="domicilio" title=uiLabelMap.Domicilio size=30 />
  <@inputTextRow name="municipio" title=uiLabelMap.Municipio size=30 />
  <@inputTextRow name="localidad" title=uiLabelMap.Localidad size=30 />
  <@inputTextRow name="ejido" title=uiLabelMap.Ejido size=30 />
  <@inputTextRow name="tipoTerreno" title=uiLabelMap.TipoTerreno size=30 />
  <@inputTextRow name="tipoDocLegalPropiedad" title=uiLabelMap.TipoDocLegalPropiedad size=30 />
  <@inputTextRow name="descDocLegalPropiedad" title=uiLabelMap.DescDocLegalPropiedad size=30 />
  <@inputTextRow name="origenAdquisicion" title=uiLabelMap.OrigenAdquisicion size=30 />
  <@inputTextRow name="formaAdquisicion" title=uiLabelMap.FormaAdquisicion size=30 />
  <@inputTextRow name="tipoEmisorTituloPropiedad" title=uiLabelMap.TipoEmisorTituloPropiedad size=30 />
  <@inputCurrencyRow name="valorContruccion" title=uiLabelMap.ValorContruccion disableCurrencySelect=true/>
  <@inputCurrencyRow name="valorRazonable" title=uiLabelMap.ValorRazonable disableCurrencySelect=true/>
  <tr>
    <@displayTitleCell title=uiLabelMap.FechaAvaluo />
    <@inputDateTimeCell name="fechaAvaluo"  />
  </tr>
  <tr>
    <@displayTitleCell title=uiLabelMap.FechaTituloPropiedad />
    <@inputDateTimeCell name="fechaTituloPropiedad"  />
  </tr>
  <@inputTextRow name="claveCatastral" title=uiLabelMap.ClaveCatastral size=30 />
  <tr>
    <@displayTitleCell title=uiLabelMap.FechaInicioClaveCatastral />
    <@inputDateTimeCell name="fechaInicioClaveCatastral"  />
  </tr>
  <tr>
    <@displayTitleCell title=uiLabelMap.FechaVenciClaveCatastral />
    <@inputDateTimeCell name="fechaVenciClaveCatastral"  />
  </tr>
  <@inputCurrencyRow name="superficieTerreno" title=uiLabelMap.SuperficieTerreno disableCurrencySelect=true/>
  <@inputCurrencyRow name="superficieConstruccion" title=uiLabelMap.SuperficieConstruccion disableCurrencySelect=true/>
  <@inputCurrencyRow name="superficieDisponible" title=uiLabelMap.SuperficieDisponible disableCurrencySelect=true/>
  <@inputTextRow name="folioRegPubliPropieComercio" title=uiLabelMap.FolioRegPubliPropieComercio size=30 />
  <tr>
    <@displayTitleCell title=uiLabelMap.FechaInscRegPropieComerc />
    <@inputDateTimeCell name="fechaInscRegPropieComer"  />
  </tr>
  <@inputTextRow name="ciudadInscRegPubPropComer" title=uiLabelMap.CiudadInscRegPubPropComer size=30 />
  <tr>
    <@displayTitleCell title=uiLabelMap.FechaIncorInventario />
    <@inputDateTimeCell name="fechaIncorInventario"  />
  </tr>
  <@inputSubmitRow title=uiLabelMap.CommonCreate />  
  	  
</table>

</form>