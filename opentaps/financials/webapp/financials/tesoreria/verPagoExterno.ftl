<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@frameSection title=uiLabelMap.PagoExterno >
	<table>
		<@displayRow title=uiLabelMap.TesoreriaPagoExternoId text=PagoExterno.pagoExternoId!'' />
		<@displayRow title=uiLabelMap.ClavePresupuestal text=PagoExterno.clavePresupuestal!'' />
		<@displayRow title=uiLabelMap.CommonCreatedBy text=PagoExterno.userLoginId!'' />
		<@displayLinkRow href="viewAcctgTrans?acctgTransId=${PagoExterno.acctgTransId!''}" title=uiLabelMap.NumeroDePoliza text=AcctgTrans.poliza!''/>
	</table>
</@frameSection>