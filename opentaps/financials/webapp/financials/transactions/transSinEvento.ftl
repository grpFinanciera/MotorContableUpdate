<#--
 *@author     Jesus Ruiz
 *@since      1.0
-->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl" />

<script language="JavaScript" type="text/javascript">
<!-- //
  	   	//Funcion para actualizar los campos del item de una solicitud
		function contactTypeClickActualiza(contador) 
		{	var acctgTransId = document.getElementById('acctgTransId').value;
			var acctgTransEntryId = document.getElementById('acctgTransEntryId'+contador).value;
		  	var montoDetalle = document.getElementsByName('montoDetalle'+contador)[0].value;
		  	var cuenta = document.getElementsByName('cuenta'+contador)[0].value;
		  	var isDebitCreditFlag = document.getElementsByName('isDebitCreditFlag'+contador)[0].value;
		  	if (acctgTransId != "" && acctgTransEntryId != "" && montoDetalle != "" && isDebitCreditFlag !="") 
		    { 	var requestData = {'acctgTransId' : acctgTransId, 'acctgTransEntryId' : acctgTransEntryId, 'montoDetalle' : montoDetalle, 'isDebitCreditFlag' : isDebitCreditFlag, 'cuenta' : cuenta};
		    	opentaps.sendRequest('cambiaEntryTransSinEvento', requestData, function(data) {responseActualizaItems(data)});
		    }
			
		}

		//Functions load the response from the server
		function responseActualizaItems(data) 
		{	for (var key in data) 
			{	if(data[key]=="ERROR")
				{	alert("Ocurri\u00f3 un error al actualizar los campos, compruebe que la cuenta es de registro.");
				}
				else
				{	var acctgTransEntryId = document.getElementById('transSinEvento').submit();
				}	    		
			}		
		}
		
		
		////Funcion para eliminar el registro de un item de una solicitud
		function contactTypeClickElimina(contador) 
		{confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar) 
			{	var acctgTransId = document.getElementById('acctgTransId').value;
				var acctgTransEntryId = document.getElementById('acctgTransEntryId'+contador).value;
		  		if (acctgTransId != "" && acctgTransEntryId != "") 
		    	{ 	var requestData = {'acctgTransId' : acctgTransId, 'acctgTransEntryId' : acctgTransEntryId};		    			    
		    		opentaps.sendRequest('eliminaEntryTransSinEvento', requestData, function(data) {responseEliminaItems(data)});
		    	}				
			}													
		}

		//Functions load the response from the server
		function responseEliminaItems(data) 
		{	for (var key in data) 
			{	if(data[key]=="ERROR")
				{	alert("Ocurri\u00f3 un error al eliminar el elemento");
				}
				else
				{	alert("El elemento se ha eliminado");
					var acctgTransEntryId = document.getElementById('transSinEvento').submit();
				}	    		
			}		
		}
		
		function contactTypeChanged() 
		{   var bancoId = document.getElementById('bancoId').value;
			if (bancoId != "") 
		    { 	var requestData = {'bancoId' : bancoId, 'organizationPartyId' : '${organizationPartyId}'};		    			    
		    	opentaps.sendRequest('obtieneCuentasBancarias', requestData, function(data) {cargarCuentasResponseOrigen(data)});
		    }
			
		}

		//Functions load the response from the server
		function cargarCuentasResponseOrigen(data) 
		{	i = 0;				
			var select = document.getElementById("cuentaBancariaId");
			document.getElementById("cuentaBancariaId").innerHTML = "";
			for (var key in data) 
			{	if(data[key]=="ERROR")
				{	document.getElementById("cuentaBancariaId").innerHTML = "";
				}
				else
				{	select.options[i] = new Option(data[key]);
					select.options[i].value = key;					
				  	i++;
				}	    		
			}		
		}
		
		var mapTipoAuxiliar = ${Static["net.sf.json.JSONArray"].fromObject(mapTipoAuxiliar)};
		function MostrarTipoAuxiliar(/*String*/ tipoAuxiliarId){
			
			var mapaTipo = mapTipoAuxiliar[0];
			var ObjetoAuxiliar = mapaTipo[tipoAuxiliarId];
			if(ObjetoAuxiliar.tipo === 'B'){
				document.getElementById('bancoTr').style = '';
				document.getElementById('auxiliarTr').style.display = 'none';
				document.getElementById('auxiliar').value = '';
			} else {
				document.getElementById('bancoTr').style.display = "none";
				document.getElementById('cuentaBancariaId').value = '';
				document.getElementById('auxiliarTr').style= '';
			}
			
			dijit.registry.byId('ComboBox_auxiliar').filtro = ObjetoAuxiliar.tipoEntidad;
			
		}
// -->
</script>

<#if acctgTransId?exists&&acctgTrans?has_content>
	<#if !acctgTrans.acctgTransPoliza ?has_content>
		<#assign LinkActualizar><@submitFormLink form="afectaTransSinEvento" text=uiLabelMap.CommonEnviar /></#assign>
	<#else>
		<#assign LinkActualizar><@displayLinkButton text=uiLabelMap.VerPoliza href="viewAcctgTrans?acctgTransId=${acctgTrans.acctgTransPoliza}"/></#assign>
	</#if>	
		<@frameSection title=uiLabelMap.Trans extra=LinkActualizar!>
		<form method="post" action="<@ofbizUrl>transSinEvento</@ofbizUrl>" name="transSinEvento" style="margin: 0;" id="transSinEvento">
			<table class="twoColumnForm">
    		<tbody>
				<@inputHidden name="acctgTransId" value=acctgTrans.acctgTransId/>
				<@displayRow title=uiLabelMap.ViaticoDescription text=acctgTrans.descripcion/>
				<@displayRow title=uiLabelMap.FinancialsTipoPoliza text=acctgTrans.tipoPolizaId/>
				<@displayRow title=uiLabelMap.CommonCurrency text=acctgTrans.currency/>
				<@displayDateRow title=uiLabelMap.FinancialsAccountigDate  date=acctgTrans.fechaContable! format="DATE" />								
			</tbody>
  			</table>			
		</form>
		
		<form method="post" action="<@ofbizUrl>afectaTransSinEvento</@ofbizUrl>" name="afectaTransSinEvento" style="display:none" id="afectaTransSinEvento">
			<table class="twoColumnForm">
    		<tbody>
				<@inputHidden name="acctgTransId" value=acctgTrans.acctgTransId/>
				<@inputHidden name="organizationPartyId" value=organizationPartyId!'' />
				<@displayRow title=uiLabelMap.ViaticoDescription text=acctgTrans.descripcion/>
				<@displayRow title=uiLabelMap.FinancialsTipoPoliza text=acctgTrans.tipoPolizaId/>
				<@displayRow title=uiLabelMap.CommonCurrency text=acctgTrans.currency/>
				<@displayDateRow title=uiLabelMap.FinancialsAccountigDate  date=acctgTrans.fechaContable! format="DATE" />								
			</tbody>
  			</table>			
		</form>
				
	</@frameSection>
<#else>		
	<form method="post" action="<@ofbizUrl>guardaTransSinEvento</@ofbizUrl>" name="guardaTransSinEvento" style="margin: 0;" id="guardaTransSinEvento">	
  		<table class="twoColumnForm">
    	<tbody>
    	<tr>  
      		<@displayTitleCell title=uiLabelMap.PurchDescripcion titleClass="requiredField" />
      		<@inputTextCell name="descripcion" maxlength="255" size=50/>
    	</tr>
    	<tr>  
      		<@displayTitleCell title=uiLabelMap.FinancialsTipoPoliza titleClass="requiredField" />
			<@inputSelectCell list=listPoliza displayField="description" name="tipoPolizaId" id="tipoPolizaId"/>
    	</tr>    	
	<@inputCurrencySelectRow name="currency" title=uiLabelMap.CommonCurrency defaultCurrencyUomId="MXN" titleClass="requiredField"/>
    <@inputDateRow name="fechaContable" title=uiLabelMap.FinancialsAccountigDate form="fechaCont" titleClass="requiredField"/>
	<@inputSubmitRow title=uiLabelMap.CommonCreate />  
    </tbody>
  </table>
</form>
</#if>

<#if acctgTransId?exists&&acctgTrans?has_content>
<#if !acctgTrans.acctgTransPoliza?has_content >
<@frameSection title=uiLabelMap.lineaTrans>
	<form method="post" action="<@ofbizUrl>agregaEntryTransSinEvento</@ofbizUrl>" name="agregaEntryTransSinEvento" style="margin: 0;">
		<table border="0"  width="100%">
			<@inputHidden name="acctgTransId" value=acctgTransId/>
			<@displayTitleCell title=uiLabelMap.Cuenta titleClass="requiredField"/>
			<@inputAutoCompleteGlAccountCell name="cuenta" url="getAutoCompleteGlAccountsRegistro"/>
			<@inputSelectRow title=uiLabelMap.FinancialsTipoAuxiliar name="tipoAuxiliar" list=listTipoAuxiliar![] key="tipoAuxiliarId" displayField="descripcion" required=false  onChange="MostrarTipoAuxiliar(this.value);"/>
			<tr id="auxiliarTr"style='display:none;' >
				<@displayTitleCell title=uiLabelMap.Auxiliar />
				<@inputAutoCompletePartyCell name="auxiliar" id="auxiliar"  />
			</tr>
			<tr id="bancoTr" style='display:none;'>
				<@displayTitleCell title=uiLabelMap.FinancialsCuentaBancaria  />
				<@inputAutoCompleteCuentaBancoCell name="cuentaBancariaId" id="cuentaBancariaId" />
			</tr>
  			<@inputCurrencyRow name="monto" currencyName="uomId" defaultCurrencyUomId=acctgTrans.currency disableCurrencySelect=true title=uiLabelMap.CommonAmount titleClass="requiredField"/>
			<#assign flag = {"":"","D":"Cargo","C":"Abono"} />
			<tr>
			<@displayTitleCell title=uiLabelMap.flagDC titleClass="requiredField"/>
			<td>
			<@inputSelectHash id="isDebitCreditFlag" name="isDebitCreditFlag" hash=flag />
			</td>
			</tr>
			<@inputSubmitRow title=uiLabelMap.CommonAdd />
		</table>
	</form>
</@frameSection>
</#if>
	
<@frameSection title=uiLabelMap.lineasTrans >
	<#if listDetalle?has_content>
	
	<table border="0" width="100%">
				
	<thead>		
		<tr>
			<th>${uiLabelMap.Cuenta}</th>
			<th>${uiLabelMap.Auxiliar}</th>
			<th>${uiLabelMap.Banco}</th>
			<th>${uiLabelMap.Flag}</th>
			<th>${uiLabelMap.CommonTotal}</th>						
			<th></th>
			
		</tr>
	</thead>
	<tbody>
		<#assign count = 1 />
		<#assign sumaCargos = 0 />
		<#assign sumaAbonos = 0 />
		<#list listDetalle as detalle>		
				<@inputHidden name="acctgTransId" id="acctgTransId" value=acctgTransId/>
				<@inputHidden name="acctgTransEntryId${count}" id="acctgTransEntryId${count}" value=detalle.acctgTransEntryId/>
						<tr class="${tableRowClass(detalle_index)}">
							<@displayCell text=detalle.glAccountId/>
							<@displayCell text=detalle.auxiliar!/>
							<@displayCell text=detalle.cuentaBancariaId!/>
							<#if detalle.isDebitCreditFlag == "D">
            					<td>${"Cargo"}</td>
            					<#assign sumaCargos=sumaCargos + detalle.monto! />
            				<#else>
                				<td>${"Abono"}</td>
                				<#assign sumaAbonos=sumaAbonos + detalle.monto! />
            				</#if>
							<!--<@displayCell text=detalle.isDebitCreditFlag/>-->
							<@displayCurrencyCell currencyUomId=acctgTrans.currency amount=detalle.monto/>
							<!--<@inputAutoCompleteGlAccountCell name="cuenta${count}" url="getAutoCompleteGlAccountsRegistro" default=detalle.glAccountId/>
							<@inputCurrencyCell name="montoDetalle${count}" currencyName="uomId" defaultCurrencyUomId=acctgTrans.currency disableCurrencySelect=true default=detalle.monto/>
							<td>
							<@inputSelectHash id="isDebitCreditFlag" name="isDebitCreditFlag${count}" hash=flag default=detalle.isDebitCreditFlag/>
							</td>							
							<td align="center" width="5%">
								<input name="submitButton" id="actualiza${count}"" type="submit" class="subMenuButton" value="${uiLabelMap.CommonUpdate}" onClick="contactTypeClickActualiza('${count}')"/>
							</td>
							-->
							<#if !acctgTrans.acctgTransPoliza ?has_content>
								<td align="center" width="7%">
									<input name="submitButton" id="elimina${count}"" type="submit" class="subMenuButtonDangerous" value="${uiLabelMap.ViaticoElimiarItem}" onClick="contactTypeClickElimina('${count}')"/>								
								</td>
							</#if>
						</tr>		
			<#assign count=count + 1/>
		</#list>
			<#if !acctgTrans.acctgTransPoliza ?has_content>
				<tr>
					<td colspan="3" />
					<td colspan="2">
						<hr>
					</td>
				</tr>
				<tr>
					<td colspan="3" />
					<@displayCell text=uiLabelMap.FinancialsDebitTotal class="tableheadtext" style="text-align:right"/>
					<@displayCurrencyCell currencyUomId=acctgTrans.currency amount=sumaCargos />
				</tr>
				<tr>
					<td colspan="3" />
					<@displayCell text=uiLabelMap.FinancialsCreditTotal class="tableheadtext"/>
					<@displayCurrencyCell currencyUomId=acctgTrans.currency amount=sumaAbonos />
				</tr>				
			</#if>	
	</tbody>
	</table>
	</#if>
</@frameSection>
</#if>	