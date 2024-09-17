<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#assign objetoModulo = eventoContable.getRelatedOne("Modulo")! />

<#if eventoContable.moduloId?has_content && eventoContable.moduloId == "DESHABILITADO">
	<#assign textoActivo="${uiLabelMap.FinancialsActivarEvento}"/>
	<#assign textoStatus="${uiLabelMap.FinancialsInactivo}"/>
<#else>
	<#assign textoActivo="${uiLabelMap.FinancialsDesactivarEvento}"/>
	<#assign textoStatus="${uiLabelMap.FinancialsActivo}"/>
</#if>

<#if !existeEvento>
	<#assign extraOptions>
 		<a class="buttontext" href="EditarEvento?acctgTransTypeId=${acctgTransTypeId}">${uiLabelMap.FinancialsEditarEvento}</a>
 		<a class="buttontext" href="EliminarRegistroEvento?acctgTransTypeId=${acctgTransTypeId}">${uiLabelMap.FinancialsEliminarEvento}</a>
 		<a class="buttontext" href="DesactivarEvento?acctgTransTypeId=${acctgTransTypeId}">${textoActivo}</a>
	</#assign>
<#else>
	<#assign extraOptions>
		<a class="buttontext" href="EditarEventoLineasCont?acctgTransTypeId=${acctgTransTypeId}">${uiLabelMap.FinancialsEditarEvento}</a>
 		<a class="buttontext" href="DesactivarEvento?acctgTransTypeId=${acctgTransTypeId}">${textoActivo}</a>
	</#assign>
</#if>


<@frameSection title=uiLabelMap.VerEvento extra=extraOptions >
	<table width="100%">
		<@seccionRow colspan="4" title=uiLabelMap.InfGeneral />
		<@displayRow title=uiLabelMap.FinancialsDocumentoId text=eventoContable.acctgTransTypeId />
		<@displayRow title=uiLabelMap.FinancialsDescripcionDoc text=eventoContable.descripcion />
		<@displayRow title=uiLabelMap.FinancialModulo text=objetoModulo.nombre! />
		<@displayRow title=uiLabelMap.TipoPoliza text=eventoContable.tipoPolizaId />
		<@displayRow title=uiLabelMap.FinancialPredecesor text=eventoContable.predecesor! />
		<@displayRow title=uiLabelMap.FinancialsStatusId text=textoStatus />
		<#if lineaPresupuestal?has_content>
		<@seccionRow colspan="4" title=uiLabelMap.CuentasPresupuestales/>
		<#list lineaPresupuestal as presupuestal>
		<#assign objetoCargoCta = presupuestal.getRelatedOne("cargoGlAccount")! />
		<#assign objetoAbonoCta = presupuestal.getRelatedOne("abonoGlAccount")! />
			<tr>
				<@displayTitleCell title=uiLabelMap.CommonDescription />
				<@displayTitleCell title=presupuestal.descripcion! titleClass=""/>
				<@displayTitleCell title=uiLabelMap.FinancialTipoMatriz/>
				<#assign objetoTipoMatriz = presupuestal.getRelatedOne("TipoMatriz")!/>
				<@displayTitleCell title=objetoTipoMatriz.descripcion! titleClass=""/>
			</tr>
			<tr>
				<@displayTitleCell title=uiLabelMap.CuentaCargo! />
				<@displayTitleCell title=objetoCargoCta.accountName+"("+presupuestal.cuentaCargo+")" titleClass=""/>
				<@displayTitleCell title=uiLabelMap.CuentaAbono! />
				<@displayTitleCell title=objetoAbonoCta.accountName+"("+presupuestal.cuentaAbono+")" titleClass=""/>				
			</tr>
			<tr>
				<@displayTitleCell title=uiLabelMap.CatalogoCargo />
				<#assign objetoCargoAux = presupuestal.getRelatedOne("cargoTipoAuxiliar")!/>
				<@displayTitleCell title=objetoCargoAux.descripcion! titleClass=""/>
				<@displayTitleCell title=uiLabelMap.CatalogoAbono />
				<#assign objetoAbonoAux = presupuestal.getRelatedOne("abonoTipoAuxiliar")!/>
				<@displayTitleCell title=objetoAbonoAux.descripcion! titleClass=""/>
			</tr>
		</#list>		
		</#if>
		<#if lineaContable?has_content>
		<@seccionRow colspan="4" title=uiLabelMap.CuentasPatrimoniales	/>
		<#list lineaContable as contable>
		<#assign objetoCargoCta = contable.getRelatedOne("cargoGlAccount")! />
		<#assign objetoAbonoCta = contable.getRelatedOne("abonoGlAccount")! />
			<tr>
				<@displayTitleCell title=uiLabelMap.CommonDescription />
				<@displayTitleCell title=contable.descripcion! titleClass=""/>
			</tr>
			<tr>
				<@displayTitleCell title=uiLabelMap.CuentaCargo! />
				<@displayTitleCell title=objetoCargoCta.accountName+"("+contable.cuentaCargo+")" titleClass=""/>
				<@displayTitleCell title=uiLabelMap.CuentaAbono! />
				<@displayTitleCell title=objetoAbonoCta.accountName+"("+contable.cuentaAbono+")" titleClass=""/>			
			</tr>
			<tr>
				<@displayTitleCell title=uiLabelMap.CatalogoCargo />
				<#assign objetoCargoAux = contable.getRelatedOne("cargoTipoAuxiliar")! />
				<@displayTitleCell title=objetoCargoAux.descripcion! titleClass=""/>
				<@displayTitleCell title=uiLabelMap.CatalogoAbono />
				<#assign objetoAbonoAux = contable.getRelatedOne("abonoTipoAuxiliar")! />
				<@displayTitleCell title=objetoAbonoAux.descripcion! titleClass=""/>
			</tr>					
			<tr>
				<@displayTitleCell title=uiLabelMap.Excepcion />
				<#assign textoExcepcion><#if contable.excepcion?exists && contable.excepcion=="Y">${uiLabelMap.CommonYes}<#else>${uiLabelMap.CommonNo}</#if></#assign>
				<@displayTitleCell title=textoExcepcion! titleClass=""/>
			</tr>
		</#list>
		</#if>
	</table>

</@frameSection>
