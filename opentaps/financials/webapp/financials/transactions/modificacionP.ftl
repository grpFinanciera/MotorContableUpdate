<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<script language="JavaScript" type="text/javascript">
<!-- //
	var modulo;
	
	function cargaEventos(/*String*/ moduloId){
		var requestData = {'moduloId' : moduloId};		    			    
		    	opentaps.sendRequest('getEventosAfectacion', requestData, function(data) {llenaComboEventos(data)});
	}
	
	function llenaComboEventos(data){
		i = 0;				
		var select = document.getElementById("acctgTransTypeId");
		select.innerHTML = "";
		for (var key in data) 
		{	if(data[key]=="ERROR")
			{	select.innerHTML = "";
			}
			else
			{	select.options[i] = new Option(data[key]);
				select.options[i].value = key;					
			  	i++;
			}	    		
		}	
	}
	
// -->
</script>
          
<@frameSection title=uiLabelMap.ModificacionPresupuesto>
	
<form method="post" action="<@ofbizUrl>AfectacionMomentoActual</@ofbizUrl>" name="AfectacionMomentoActual" >
    
<body>
    <div class="form" style="border:0">
    <table class="fourColumnForm" >
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialTipoAfectacion titleClass="requiredField"/>
        <@inputSelectHashCell hash= {"":"","MODIFICACION_E":"EGRESO", "MODIFICACION_I":"INGRESO"} id="moduloId" name="moduloId" onChange="cargaEventos(this.value);"/>
      </tr> 
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialsTipoEvento titleClass="requiredField"/>
		<td>
		<select id="acctgTransTypeId" name="acctgTransTypeId" class="inputBox">
		</select>
		</td> 
      </tr> 
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialsTransactionDate titleClass="requiredField"/>
        <@inputDateTimeCell name="fechaTransaccion" default=Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp() />
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialsAccountigDate titleClass="requiredField" />
        <@inputDateTimeCell name="fechaContable" default=Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp() />
      </tr> 
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialTipoAfectacion titleClass="requiredField"/>
        <@inputSelectHashCell hash= {"INTERNA":"Interna", "EXTERNA":"Externa"} name="tipoMovimiento"/>
      </tr>            
      <tr>
      	<@inputTextRow title=uiLabelMap.Comentario name="comentario" size=60 default=comentario?if_exists titleClass="requiredField" />
      </tr>
      <tr>
      	<@displayTitleCell title=uiLabelMap.AccountingCurrency titleClass="requiredField" />
		<@inputCurrencySelectCell name="currency" defaultCurrencyUomId="MXN" />
      </tr>
      <tr>        
        <input type="hidden" name="organizationPartyId" value="${organizationPartyId}"/>
        <input type="hidden" name="statusId" value="CREADA" />
      </tr>  	
	  </tr>		          
  	<tr>                                                                                                       
      <@inputSubmitRow title=uiLabelMap.CommonCreate />
    </table>
  </div>
  </body>
  </form>
</@frameSection>

<script type="text/javascript">
	var moduloId = document.getElementById('moduloId').value;
    opentaps.addOnLoad(cargaEventos(moduloId));
</script>
