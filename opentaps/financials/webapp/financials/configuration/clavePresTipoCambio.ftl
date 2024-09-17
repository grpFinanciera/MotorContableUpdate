<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form method="post" action="<@ofbizUrl>AgregarClaveTipoCambio</@ofbizUrl>" name="AgregarClaveTipoCambio">
  <@inputHidden id="performFind" name="performFind" value="Y"/>
  <@inputHidden id="organizationPartyId" name="performFind" value="${organizationPartyId}"/>
  <table class="twoColumnForm">
    <tbody>
      <@displayTitleCell title=uiLabelMap.FinancialTipoClave />
      <@inputSelectCell key="id" displayField="id" list=tiposClave name="ingresoEgreso" defaultOptionText=ingresoEgreso?if_exists/>
      <@displayCvesPresupRow tagTypes=tagTypes titleClass="requiredField"/>
      <@inputSubmitRow title=uiLabelMap.CommonAdd />
    </tbody>
  </table>
</form>

<#if clavesPresList?has_content>
<@frameSection title="">
	<table border="0" width="100%">

	<thead>		
		<tr>
			<th>${uiLabelMap.FinancialsClavePresup}</th>
			<th>${uiLabelMap.Tipo}</th>			
			<th>${uiLabelMap.FinancialsFechaDesde}</th>
			<th>${uiLabelMap.FinancialsFechaHasta}</th>
			<th>${uiLabelMap.isActive}</th>
			<th></th>
		</tr>
	</thead>
	<tbody>
		<#list clavesPresList as row>
			<tr class="${tableRowClass(row_index)}">
			    <@displayCell text=row.clavePresupuestal/>
    			<@displayCell text=row.tipo/>
    			<@displayDateCell date=row.fechaDesde format="DATE"/>
    			<@displayDateCell date=row.fechaHasta format="DATE"/>
    			<#if row.flag.equals("Y")>
    				<@displayCell text="SI"/>
    			<#else>
    				<@displayCell text="NO"/>
    			</#if>    			
			</tr>
			<tr class="${tableRowClass(row_index)}">
				<td colspan="8" valign="top">
				<@flexArea targetId="ClavePresupuestal${row.idClaveTipoCambio}" title=uiLabelMap.FinancialsClavePresup controlStyle="font-weight: bold;" >
					<table border="0" cellpadding="0" cellspacing="0" width="100%">
						<tr>
						<#if tagTypes?has_content>
							<@clavesPresupDisplay tags=tagTypes entity=row partyExternal=row.acctgTagEnumIdAdmin/>
						</#if>
						</tr>
					</table>
				</@flexArea>
				</td>
			</tr>
			<tr>
				<td>
					&nbsp;
				</td>
			</tr>			
		</#list>
	</tbody>
	</table>
</@frameSection>
</#if>
