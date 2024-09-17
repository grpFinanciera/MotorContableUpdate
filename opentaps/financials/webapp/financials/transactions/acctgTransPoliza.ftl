<#-- Parametrized find form for transactions. -->
<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<#assign hostname = request.getServerName() /></br>

<@form name="verImprimirPoliza" url="verImprimirPoliza" acctgTransId=acctgTrans.acctgTransId/>
<#assign accionesPoliza>
	<@submitFormLink form="verImprimirPoliza" text=uiLabelMap.ImprimirPoliza class="subMenuButton"/>
</#assign>

<#if !acctgTrans.isReversa ?has_content>
	<@form name="reverseAcctgTransForm" url="reversaPoliza" acctgTransId=acctgTrans.acctgTransId/>
	<#assign accionesPoliza>${accionesPoliza}  <@submitFormLinkConfirm form="reverseAcctgTransForm" text=uiLabelMap.FinancialsReverseTransaction /></#assign>
</#if>

<#if TotalCheque?has_content >
	<@form name="detalleChequeSimple" url="detalleChequeSimple" acctgTransId=acctgTrans.acctgTransId monto=TotalCheque />
	<#assign accionesPoliza>${accionesPoliza}<@submitFormLink form="detalleChequeSimple" text=uiLabelMap.FinancialsPrintChecks class="subMenuButton"/></#assign>
</#if>

<@frameSection title=uiLabelMap.FinancialsPoliza extra=accionesPoliza>
	<table class="listTable">
		<thead>
			<tr>
	        	<@displayTitleCell title=uiLabelMap.NumeroDePoliza />
	        	<@displayTitleCell title=uiLabelMap.Concepto />
	        	<@displayTitleCell title=uiLabelMap.FinancialsFechaContable/>
	        	<@displayTitleCell title=uiLabelMap.FinancialsTipoPoliza/>
	        	<@displayTitleCell title=uiLabelMap.FinancialsCreditTotal/>
	        	<@displayTitleCell title=uiLabelMap.FinancialsDebitTotal/>
	      	</tr>
		</thead>
		<tbody>
			<tr>
				<@displayCell text=acctgTrans.poliza! blockClass="titleCell"/>
				<@displayCell text=acctgTrans.description! blockClass="titleCell"/>
				<@displayDateCell date=acctgTrans.postedDate blockClass="titleCell"/>
				<@displayCell text=acctgTrans.getRelatedOne("tpcpTipoPoliza").description! blockClass="titleCell"/>
				<@displayCurrencyCell amount=acctgTrans.postedAmount! currencyUomId="MXN" class="textright"/>
				<@displayCurrencyCell amount=acctgTrans.postedAmount! currencyUomId="MXN" class="textright"/>
			</tr>
		</tbody>
	</table>
</@frameSection>
