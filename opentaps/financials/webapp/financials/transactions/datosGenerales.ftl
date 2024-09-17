<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form method="post" action="<@ofbizUrl>crearPatrimonio</@ofbizUrl>" name="selectAllForm" style="margin: 0;">
  <@inputHidden name="organizationPartyId" value="${organizationPartyId}"/>
  <table class="twoColumnForm">
    <tbody>
    <tr class="${tableRowClass(0)}" name="item" >
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialsAccountigDate titleClass="requiredField" />
        <@inputDateTimeCell name="fechaContable" default=Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp() />
      </tr>  
      <tr>
      	<@inputTextRow title=uiLabelMap.Descripcion name="description" size=60 default=description?if_exists titleClass="requiredField"/>
      </tr>  
      <tr>
      	<@displayTitleCell title=uiLabelMap.Moneda titleClass="requiredField" />
        <@inputCurrencySelectCell name="currencyUomId" list=currencies defaultCurrencyUomId="MXN" useDescription=false />
       </tr>  
       <tr>
			<td class="titleCell" >
				<@display text=uiLabelMap.AccountingTipoDocumento+" :" class="requiredField"/>
			</td>
			<@inputSelectCell id="acctgTransTypeId" name="acctgTransTypeId" list=listEventos?sort_by("descripcion") 
                		key="acctgTransTypeId" displayField="descripcion" onChange="cambiaEvento(this.value,'2');" required=false defaultOptionText=""/>
	   </tr>
     </tr>
      <@inputSubmitRow title=uiLabelMap.CommonCreate />
    </tbody>
  </table>
</form>
