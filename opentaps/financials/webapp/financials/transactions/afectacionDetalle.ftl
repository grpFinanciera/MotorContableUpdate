<#--
 * Copyright (c) Open Source Strategies, Inc.
 *
 * Opentaps is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Opentaps is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Opentaps.  If not, see <http://www.gnu.org/licenses/>.
-->
<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#assign evento = Afectacion.getRelatedOne("EventoContable")! />

<#assign extraOptions>
  <@submitFormLink form="AfectaCreaTransaccion" text=uiLabelMap.FinancialsAfectar />
</#assign>
<@frameSection title=uiLabelMap.AgrupadorDePoliza extra=extraOptions?if_exists >
<@form name="AfectaCreaTransaccion" url="AfectaCreaTransaccion" afectacionId=Afectacion.afectacionId! >
  <table class="twoColumnForm">
		<@displayRow title=uiLabelMap.idPolizaAgrupador text=Afectacion.afectacionId! />
		<@displayDateRow title=uiLabelMap.FinancialsTransactionDate date=Afectacion.fechaTransaccion! />
		<@displayDateRow title=uiLabelMap.FinancialsAccountigDate date=Afectacion.fechaContable! />
		<@displayRow title=uiLabelMap.Comentario text=Afectacion.comentario! />
		<@displayRow title=uiLabelMap.FinancialsTipoEvento text=evento.descripcion! />
  </table>
 </@form>
</@frameSection>

<@frameSection title=uiLabelMap.AnadirClavePresupuestal>
  <form method="post" action="<@ofbizUrl>AgregarClaveAfecta</@ofbizUrl>" name="AgregarClaveAfecta">
  <table>
  	<@inputHidden name="afectacionId" value=Afectacion.afectacionId!/>
  <tr>
     <@displayCvesPresupRow tagTypes=tagTypes />
  </tr>   
  <tr>
          <@displayCell text=uiLabelMap.CommonAmount blockClass="titleCell" blockStyle="width: 100px" class="tableheadtext" />
          <@inputCurrencyCell name="monto" currencyName="currency" defaultCurrencyUomId="MXN"/>
       </tr>
      <tr name="item" > </tr>
      <@inputSubmitRow title=uiLabelMap.CommonAdd/>
     <div class="cleaner">&nbsp;</div>
   </table>
   </form> 
  </@frameSection>
  
  <#assign montoClaves = 0 /> 
  <#list listAfectacionDetalle as row>
  	<#if row.statusId == "AFECTAR">
  		<#assign montoClaves = montoClaves + row.montoRestante />
  	</#if>
  </#list>
  <#assign extraOptions><span class="boxhead">${uiLabelMap.FinancialsTotalClaves}</span><@displayCurrencyCell id="montoTotal" currencyUomId=parameters.orgCurrencyUomId amount=montoClaves class="boxhead"/></#assign>
 
  <@frameSection title=uiLabelMap.ClavesPresupuestales extra=extraOptions>
  <table width="100%" >     
  <@displayTitleCell title=uiLabelMap.clavePresupuestal />
  <@displayTitleCell title=uiLabelMap.Descripcion />
  <@displayTitleCell title=uiLabelMap.Monto />
  <@displayTitleCell title=uiLabelMap.FinancialsStatusId />
	<td colspan="3" ></td>	    
   <#list listAfectacionDetalle as row>		
  	<@form name="actualizaEstatusDetalle${row.afectacionSeqId}" url="ActualizaEstatus" afectacionId=row.afectacionId! afectacionSeqId=row.afectacionSeqId! />
  </#list>
  <#list listAfectacionDetalle as row>	
	<@form name="actualizaDetalle${row.afectacionSeqId}" url="ActualizaDetalle" afectacionId=row.afectacionId! afectacionSeqId=row.afectacionSeqId! >
	<#assign listLineas = Static["com.opensourcestrategies.financials.transactions.ControladorAfectacion"].getLineasClave(delegator,row.afectacionId,row.afectacionSeqId) />
		<tr>
		    <@displayCell text=row.clavePresupuestal! style="float:right"/>
		    <@displayCell text=row.description! style="float:right"/>    
		    <@inputCurrencyCell name="montoClave${row_index}" default=row.montoRestante defaultCurrencyUomId=parameters.orgCuClaveyUomId disableCurrencySelect=true align="right" />
		    <@displayCell text=row.statusId blockStyle="float:center" />
			<td><@inputConfirm title=uiLabelMap.CommonUpdate form="actualizaDetalle${row.afectacionSeqId}"/></td>
			<#if row.statusId == "AFECTAR" > <#assign estatus = uiLabelMap.Desactivar /> <#else> <#assign estatus = uiLabelMap.FinancialActivate /></#if>
		   	<td colspan="2"><@inputConfirm title=estatus form="actualizaEstatusDetalle${row.afectacionSeqId}"/></td>
		</tr>
		<#list listLineas as linea>
		<#assign objetoCargo = linea.getRelatedOne("cargoTipoAuxiliar")! />
		<#assign objetoAbono = linea.getRelatedOne("abonoTipoAuxiliar")! />
		<tr>
			<td></td>
			<#if (linea.tipoLinea != 'PRESUPUESTO') >
				<@displayCell text=linea.descripcion! class="tableheadtext"/>
				<@inputCurrencyCell name="monto${row.afectacionSeqId}${linea.secuencia}" default=linea.monto! disableCurrencySelect=true />
			<#else>
				<@displayCell text=linea.descripcion! class="tableheadtext"/><td></td>
			</#if>
			<#if objetoCargo?has_content >
				<@displayCell text=objetoCargo.descripcion! class="tableheadtext"/>
				<td><@inputAutoComplete name="catalogoCargo${row.afectacionSeqId}${linea.secuencia}" url=objetoCargo.urlBusqueda form="actualizaDetalle${row.afectacionSeqId}" lookup=objetoCargo.nombreLookup default=linea.valorCargo /></td>
			</#if>
			<#if objetoAbono?has_content >
				<@displayCell text=objetoAbono.descripcion! class="tableheadtext"/>
				<td><@inputAutoComplete name="catalogoAbono${row.afectacionSeqId}${linea.secuencia}" url=objetoAbono.urlBusqueda form="actualizaDetalle${row.afectacionSeqId}" lookup=objetoAbono.nombreLookup default=linea.valorAbono /></td>
			</#if>	
		</tr>
		</#list>
	</@form>
  </#list>
   </table>
  </@frameSection>

<script type="text/javascript">
	var acctgTransTypeId = "${Afectacion.acctgTransTypeId}"; 
    opentaps.addOnLoad(cambiaEventoTodos(acctgTransTypeId,'2'));
</script>
