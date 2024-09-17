<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<form method="post" action="<@ofbizUrl>traspasosPatrimonioRequest</@ofbizUrl>" name="traspasosPatrimonioRequest">
  <table border="0" cellpadding="2" cellspacing="0">
     <tbody id="tableAllForm">
  <tr name="item" >
      <tr>
		<td class="titleCell" >
		<@display text=uiLabelMap.AccountingTipoDocumento+" :" class="requiredField"/>
		</td>
		<@inputSelectCell id="acctgTransTypeId" name="acctgTransTypeId" list=listEventos?sort_by("descripcion") 
                		key="acctgTransTypeId" displayField="descripcion" onChange="cambiaEvento(this.value,'2');" required=false defaultOptionText=""/>
	  </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialsAccountigDate titleClass="requiredField" />
        <@inputDateTimeCell name="fechaContable" default=Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp() />
      </tr>  
      <tr>
        <@displayTitleCell title=uiLabelMap.CommonDescription titleClass="requiredField"/>
        <@inputTextCell name="descripcion" />
      </tr>          
      <tr>
      	<@inputCurrencySelectRow name="moneda" title=uiLabelMap.CommonCurrency defaultCurrencyUomId="MXN" />
      </tr>  
   </tr>         
      <@inputSubmitRow title=uiLabelMap.CommonCreate />
     </tbody>
    </table>
</form>