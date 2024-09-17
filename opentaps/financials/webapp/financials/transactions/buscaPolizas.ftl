<#--
 * Formulario de busqueda de p�lizas contables
 * Author: Vidal Garc�a
 * Versi�n 1.0
 * Fecha de Creaci�n: Julio 2013
-->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<form name="BuscaPoliza" >
  <table class="twoColumnForm">
    <tbody>
      <@inputTextRow title=uiLabelMap.NumeroDePoliza name="poliza" size="20" maxlength="20"/>
      <@inputSelectRow title=uiLabelMap.FinancialsTipoPoliza required=false list=listaTipoPoliza  displayField="description" name="tipoPolizaId" key="tipoPolizaId" />
      <@inputAutoCompletePartyRow title="${uiLabelMap.PurchArea}" name="areaId" />
      <@inputAutoCompleteUserLoginPartyRow name="usuario" title=uiLabelMap.Usuario />
	  <#assign diaActual = Static["org.ofbiz.base.util.UtilDateTime"].nowSqlDate()>
      <@inputDateRangeRow title=uiLabelMap.FinancialsFechaContable fromName="fechaContableDesde" thruName="fechaContableHasta" 
			defaultFrom=Static["org.ofbiz.base.util.UtilDateTime"].getMonthStart(diaActual) defaultThru=diaActual />
      <@inputRangeRow title=uiLabelMap.Monto fromName="montoDesde" thruName="montoHasta" size=10/>           
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>
