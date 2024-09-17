<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<script language="JavaScript" type="text/javascript">
	function cargaEventos(/*String*/ moduloId){
		var requestData = {'moduloId' : moduloId};	    			    
		    	opentaps.sendRequest('getEventosAfectacion', requestData, function(data) {llenaComboEventos(data)});
	}
	
	function llenaComboEventos(data){
		var idDefecto = '${parameters.acctgTransTypeId!}';
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
				if(key == idDefecto){
					select.options[i].selected = true;
					acctgTransTypeId = idDefecto;
				} 
			  	i++;
			}	    		
		}	
	}
</script>

<@frameSection title=uiLabelMap.FinancialsConsultarMomento>

<@form name="BuscaAfectacion" url="">
	<table>
	  <tr>
        <@displayTitleCell title=uiLabelMap.FinancialTipoAfectacion titleClass="requiredField"/>
        <@inputSelectHashCell hash= {"":"","CONTABILIDAD_E":"EGRESO", "CONTABILIDAD_I":"INGRESO"} id="moduloId" name="moduloId" onChange="cargaEventos(this.value);"/>
      </tr> 
		<tr>
        <@displayTitleCell title=uiLabelMap.FinancialsTipoEvento />
			<td>
			<select id="acctgTransTypeId" name="acctgTransTypeId" class="inputBox">
			</select>
			</td>
      	</tr>
		<@inputTextRow name="numeroPoliza" title=uiLabelMap.FormFieldTitle_numeroPoliza />
		<@inputDateRow name="fechaTransaccion" title=uiLabelMap.FinancialsTransactionDate form="BuscaAfectacion" />
		<@inputDateRow name="fechaContable" title=uiLabelMap.FinancialsAccountigDate form="BuscaAfectacion" />
		<@inputTextRow name="comentario" title=uiLabelMap.FormFieldTitleComentario />
		<@inputSubmitRow title=uiLabelMap.CommonFind/> 
	</table>
</@form>
<@paginate name="listAfectacionListBuilder" list=afectacionListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.NumeroAfectacion orderBy="afectacionId"/>
				<@headerCell title=uiLabelMap.FormFieldTitle_numeroPoliza orderBy="poliza"/>
                <@headerCell title=uiLabelMap.FinancialsTransactionDate orderBy="transactionDate"/>
                <@headerCell title=uiLabelMap.FinancialsAccountigDate orderBy="postedDate"/>
                <@headerCell title=uiLabelMap.Comentario orderBy="description"/>
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell href="VerAfectaMomento?acctgTransTypeId=${row.eventoSeleccionado}&afectacionId=${row.afectacionId}" text=row.afectacionId/>
                <@displayCell text=row.poliza/>
                <@displayCell text=row.transactionDate/>
                <@displayCell text=row.postedDate/>
				<@displayCell text=row.description/> 
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>

</@frameSection>
<script type="text/javascript">
	var moduloId = document.getElementById('moduloId').value;
    opentaps.addOnLoad(cargaEventos(moduloId));
</script>