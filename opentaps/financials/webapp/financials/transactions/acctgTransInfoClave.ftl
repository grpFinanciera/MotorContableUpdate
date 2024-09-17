<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<@frameSection title=uiLabelMap.FinancialsInfoClave >
	<#assign entry = listEntries.get(0)!/>
	<#assign clasifAdmin = entry.getRelatedOne("adminParty")! />
	<#assign estructura = entry.getRelatedOne("EstructuraClave")! />
	<#assign evento = entry.getRelatedOne("EventoContable")! />	
	<#assign entidad = entry.getRelatedOne("PartyGroup")! />
	<#assign objetoUsuario = entry.getRelatedOne("UserLogin")! />
	<#assign objAux = entry.getRelatedOne("AuxPartyGroup")! />
	<#assign objProduct = entry.getRelatedOne("Product")! />
	<#if objetoUsuario?has_content>
		<#assign usuarioNombre = delegator.findByPrimaryKey("Person",Static["org.ofbiz.base.util.UtilMisc"].toMap("partyId",objetoUsuario.partyId))! />
	<#else>
		<#assign usuarioNombre = "" />
	</#if>
	<#assign tipoPoliza = entry.getRelatedOne("TipoPoliza")! />
	<table width="70%" align="center" >
		<@seccionRow colspan="2" title=uiLabelMap.InfGeneral /> 
		<@displayRow title=uiLabelMap.FormFieldTitle_numeroPoliza text=entry.poliza />
		<@displayRow title=uiLabelMap.FinancialsClavePresup text=entry.clavePresupuestal />
		<@seccionRow colspan="2" title=uiLabelMap.PartyClassifications />
		<#list 1..15 as i>
			<#-- Nombre Clasificaciones -->
			<#assign nombreRelacion = "clasif"+i+"ClasifPresupuestal" />
			<#assign objetoClasif = estructura.getRelatedOne(nombreRelacion)! />
			<#if objetoClasif?has_content>
				<#assign nombreClasif = objetoClasif.get("descripcion")! />
				<#assign idClasif = objetoClasif.get("clasificacionId")! />	
			<#else>
				<#assign nombreClasif = ""/>
				<#assign idClasif = "" />	
			</#if>
			<#-- Datos Clasificaciones -->
			<#assign nombre = "tag"+i+"Enumeration" />
			<#assign clasificacion = entry.getRelatedOne(nombre)! />
			<#if clasificacion?has_content>
					<#assign texto = clasificacion.sequenceId!/>
					<@displayRow title=uiLabelMap.FinancialsClasificacion+" "+i+": " text=nombreClasif+" - "+texto! />
			<#else>
				<#if clasifAdmin?has_content && idClasif = "CL_ADMINISTRATIVA" >
					<#assign texto = clasifAdmin.externalId!/>
					<@displayRow title=uiLabelMap.FinancialsClasificacion+" "+i+": " text=nombreClasif+" - "+texto! />
				</#if>
			</#if>
		</#list>
		<@seccionRow colspan="2" title=uiLabelMap.CommonDetail />
		<@displayRow title=uiLabelMap.AccountingAccountTransactionId text=entry.acctgTransId />
		<@displayRow title=uiLabelMap.AccountingTransactionType text=evento.descripcion! />
		<@displayDateRow title=uiLabelMap.FormFieldTitle_registrationDate date=entry.transactionDate! />
		<@displayDateRow title=uiLabelMap.FormFieldTitle_postedDate date=entry.postedDate! />
		<@displayRow title=uiLabelMap.FinancialsEntidadContable text=entidad.groupName! />
		<@displayRow title=uiLabelMap.FormFieldTitle_username text=usuarioNombre.firstName! />
		<@displayRow title=uiLabelMap.FinancialsTipoPoliza text=tipoPoliza.description! />
		<@seccionRow colspan="2" title=uiLabelMap.FinancialsCatalogosAux />
		<#if objAux?has_content>
			<@displayRow title=uiLabelMap.CAParty text=objAux.groupName! />
		</#if>
		<#if objProduct?has_content>
			<@displayRow title=uiLabelMap.FinancialsProduct text=objProduct.productName! />
		</#if>
	</table>
</@frameSection>
