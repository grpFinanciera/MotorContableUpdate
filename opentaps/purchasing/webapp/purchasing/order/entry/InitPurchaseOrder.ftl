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

<script type="text/javascript">
/*<![CDATA[*/

var skipReqForAgreements = false;

/*
  AJAX handler.
  Create new <select/> of agreements, fill its options and replace existent input with new one.  
*/
function handleAgreements(/*Array*/ data) {
    if (!data) return;

    // agreement may be selected earlier
    <#if shoppingCart?has_content && shoppingCart.agreementId?has_content>
    var cartAgreementId = '${shoppingCart.agreementId}';
    <#else>
    var cartAgreementId = null;
    </#if>

    var agreementInput = document.getElementById('agreementId');
    if (!agreementInput) return;

    // creates new input
    var newSelect = document.createElement('select');
    newSelect.id = 'agreementId';
    newSelect.name = 'agreementId';
    newSelect.className = 'inputBox';
    newSelect.onchange = function() {agreementChanged();};

    if (data.length > 0) {
        newSelect.options[0] = new Option('', '', true, true);
    } else {
        // not agreements to supplier, put default message
        newSelect.options[0] = new Option('${uiLabelMap.PurchNoAgreements}', '', true, true);
    }

    // fill list of available agreements
    for (var i = 0; i < data.length; i++) {
        var agreement = data[i];
        var optionText = agreement.agreementId;
        optionText += ' - ';
        if (agreement.description != null && agreement.description.length > 0) {
            optionText += agreement.description;
        }

        var option = new Option(optionText, agreement.agreementId, false, agreement.agreementId == cartAgreementId ? true : false);
        newSelect.options[i+1] = option;

    }

    // replace agreement drop-down with new one
    opentaps.replaceNode(agreementInput, newSelect);

    // enable currency box 
    agreementChanged();
}

/*
  Send AJAX request if supplier id is changed.
*/
function supplierChanged() {
    if (skipReqForAgreements) {
        skipReqForAgreements = false;
        /*return;*/
    }

    var partyId;
    var supplierInput = document.getElementById("ComboBox_supplierPartyId");
    if (supplierInput) {
        partyId = prevSupplierId = supplierInput.value;
    } else {
        <#if parameters.partyId?has_content>
        partyId = prevSupplierId = '${parameters.partyId}';
        if (!partyId) {
            return;
        }
        <#else>
        return;
        </#if>
    };
        
    opentaps.sendRequest('getSupplierAgreementsJSON', {'partyId' : partyId, 'organizationPartyId' : '${organizationPartyId}'}, handleAgreements);
}

	var primerArticulo = "";
	
	//Functions to send request to server for load relate objects
	function contactTypeChanged() 
	{ 	 var tipoAdjudicacionId = document.getElementById('tipoAdjudicacionId').value;
		document.getElementById('fraccionId').innerHTML = '';
		document.getElementById('articuloId').innerHTML = '';
		document.getElementById('docsRequeridos').innerHTML = '';
		document.getElementById('subTipoAdjudicacionId').innerHTML = '';
		document.getElementById('descTipoAdjudicacion').innerHTML = '';
		document.getElementById('descArtId').innerHTML = '';
		if (tipoAdjudicacionId) 
	    { 	var requestData = {'tipoAdjudicacionId' : tipoAdjudicacionId};		    			    
	    	opentaps.sendRequest('obtieneDocsRequeridos', requestData, function(data) {cargarDocsRequeridos(data)});
	    	opentaps.sendRequest('obtieneArticulos', requestData, function(data) {cargarArticulos(data)});
	    	opentaps.sendRequest('obtieneSubTiposAdjudicacion', requestData, function(data) {cargarSubTiposAdjudicacion(data)});
	    	opentaps.sendRequest('obtieneDescTipoAdjudicacion', requestData, function(data) {cargarDescTipoAdjudicacion(data)});
	    }
		
	}

	
	function cargarDescTipoAdjudicacion(data) {			
			var lista = document.getElementById("descTipoAdjudicacion");		
			lista.innerHTML = "";
			for (var key in data)
			{	
				if(data[key]=="ERROR")
				{	
					lista.innerHTML = "";
				}
				else
				{	lista.options[i] = new Option(data[key]);
					lista.options[i].value = key;					
					i++;
				}
			}
								   		    
  	   }
		
	function cargarSubTiposAdjudicacion(data) 
	{	i = 0;				
		var lista = document.getElementById("subTipoAdjudicacionId");		
		document.getElementById("subTipoAdjudicacionId").innerHTML = "";
		for (var key in data) 
		{	if(data[key]=="ERROR")
			{	document.getElementById("subTipoAdjudicacionId").innerHTML = "";
			}
			else
			{	lista.options[i] = new Option(data[key]);
				lista.options[i].value = key;					
				i++;
			}	    		
		}		
	}
	
	function cargarArticulos(data) 
	{	i = 0;
		var lista = document.getElementById("articuloId");		
		document.getElementById("articuloId").innerHTML = "";
		for (var key in data) 
		{	if(data[key]=="ERROR")
			{	document.getElementById("articuloId").innerHTML = "";
				primerArticulo = "";
			}
			else
			{	
				if(i==0){ primerArticulo = key;}
				lista.options[i] = new Option(data[key]);
				lista.options[i].value = key;					
				i++;
			}	    		
		}
		contactTypeChanged2();
	}	
	
	function cargarDocsRequeridos(data) 
	{	i = 0;				
		var lista = document.getElementById("docsRequeridos");		
		document.getElementById("docsRequeridos").innerHTML = "";
		for (var key in data) 
		{	if(data[key]=="ERROR")
			{	document.getElementById("docsRequeridos").innerHTML = "";
			}
			else
			{	lista.options[i] = new Option(data[key]);
				lista.options[i].value = key;					
				i++;
			}	    		
		}		
	}		
	
	//Functions to send request to server for load relate objects
	function contactTypeChanged2() 
	{ 	 var docTipoAdjudicacionId = document.getElementById('articuloId').value;
		if(!docTipoAdjudicacionId){
			docTipoAdjudicacionId = primerArticulo;
		}
		if (docTipoAdjudicacionId) 
	    { 	var requestData = {'docTipoAdjudicacionId' : docTipoAdjudicacionId};		    			    
	    	opentaps.sendRequest('obtieneFracciones', requestData, function(data) {cargarFracciones(data)});
	    }		
	}
	
	function cargarFracciones(data) 
	{	i = 0;				
		var lista = document.getElementById("fraccionId");		
		document.getElementById("fraccionId").innerHTML = "";
		for (var key in data) 
		{	if(data[key]=="ERROR")
			{	document.getElementById("fraccionId").innerHTML = "";
			}
			else 
			{
					lista.options[i] = new Option(data[key]);
					lista.options[i].value = key;					
					i++;
			}
		}		
	}
	
	//Functions to send request to server for load relate objects
	function contactTypeChanged3() 
	{ 	 var docArtId = document.getElementById('articuloId').value;
		if (articuloId != "") 
	    { 	var requestData = {'docArtId' : docArtId};		    			    
	    	opentaps.sendRequest('obtieneDesArticulos', requestData, function(dataF) {cargarDescArt(dataF)});
	    }		
	}
	
	function cargarDescArt(dataF) 
	{	i = 0;				
		var listaDes = document.getElementById("descArtId");		
		document.getElementById("descArtId").innerHTML = "";
		for (var key in dataF) 
		{	if(dataF[key]=="ERROR")
			{	document.getElementById("descArtId").innerHTML = "";
			}
			else
			{	listaDes.options[i] = new Option(dataF[key]);
				listaDes.options[i].value = key;					
				i++;
			}	    		
		}		
	}				

/* disable/enable currency box depending on agreement selection */
function agreementChanged() {
    var agreementField = document.getElementById('agreementId');
    if (agreementField) {
        var currencyField = document.getElementById('currencyUomId');
        if (agreementField.value == null || agreementField.value.length == 0) {
            currencyField.disabled = false;
        } else {
            currencyField.disabled = true;
        }
    }
}

var onLookupReturn = function() { supplierChanged(); skipReqForAgreements = true; };

opentaps.addOnLoad(supplierChanged());
/*]]>*/
</script>
<!-- Purchase Order Entry -->
<#if security.hasEntityPermission("ORDERMGR", "_PURCHASE_CREATE", session)>
	<#if shoppingCart?exists>
	    <#assign sectionTitle = "${uiLabelMap.OrderOrder}&nbsp;${uiLabelMap.OrderInProgress}" />
	<#else>
	    <#assign sectionTitle = "${uiLabelMap.OrderOrder}" />
	</#if>

	<#assign extra ><@submitFormLink form="InitializePurchaseOrder" text=uiLabelMap.OpentapsCreateOrder /></#assign>

	<@frameSection title=sectionTitle extra=extra >

        <#if hasParty>
          <#assign thisPartyId = shoppingCart?if_exists.orderPartyId?if_exists />
        <#--<#else>
          <#assign thisPartyId = requestParameters.partyId?if_exists />-->
        </#if>
		<@form name="InitializePurchaseOrder" url="InitializePurchaseOrder">
            <@inputHidden name='finalizeMode' value='type'/>
            <@inputHidden name='orderMode' value='PURCHASE_ORDER'/>
            <#if shoppingCart?has_content>
                <#--<@inputHidden name="supplierPartyId" value=thisPartyId /> -->
                <#if cartAgreement?has_content>
                    <@inputHidden name="agreementId" value=shoppingCart.agreementId />
                </#if>
            </#if>
            <table class="twoColumnForm" border="0">
                <tr>
                    <@displayTitleCell title=uiLabelMap.PartySupplier titleClass="requiredField"/>
                    <#if shoppingCart?has_content>
                        <@displayCell text="${thisPartyId}"/>
                    <#else>
						<@inputAutoCompletePartyCell name="supplierPartyId" size="25" default="${thisPartyId?if_exists}" onChange="supplierChanged()" filtro="PROVEEDOR" />
                    </#if>
                </tr>
                <tr>
                    <@displayTitleCell title=uiLabelMap.OrderOrderName titleClass="requiredField"/>
                    <td >
                        <@inputText name="orderName" size="50" maxlength="100" default=shoppingCart?if_exists.orderName?if_exists/>
                    </td>
                </tr>
                <@inputHidden name='hasAgreements' value='Y'/>
                <tr>
                    <@displayTitleCell title=uiLabelMap.OrderSelectAgreement titleClass="requiredField"/>
                    <#if shoppingCart?has_content>
                        <#if cartAgreement?has_content>
                            <@displayCell text="${cartAgreement.agreementId} - ${cartAgreement?if_exists.description}" />
                        <#else>
                            <td></td>
                        </#if>
                    <#else>
                        <@inputSelectCell name="agreementId" list=agreements?default([]) key="agreementId" onChange="agreementChanged();" defaultOptionText="${uiLabelMap.PurchNoAgreements}" required=false ; option>
                            ${option.agreementId} - ${option.description?if_exists}
                        </@inputSelectCell>
                    </#if>
                    
                </tr>
                <tr>
                    
                    <@displayTitleCell title=uiLabelMap.OrderSelectCurrencyOr titleClass="requiredField"/>
                    <#if cartAgreement?has_content>
                        <@displayCell text=shoppingCart?if_exists.currency?if_exists />
                    <#else>
                        <@inputCurrencySelectCell defaultCurrencyUomId=defaultCurrencyUomId?if_exists/>
                    </#if>
                </tr>
                <#assign fmt = Static["org.ofbiz.base.util.UtilDateTime"].getDateTimeFormat(locale)/>
                <tr>	
                	<#assign fechaDespues = '' />
                	<#if shoppingCart?if_exists.defaultShipAfterDate?if_exists >
                		<#assign fechaDespues = Static["org.ofbiz.base.util.UtilDateTime"].timeStampToString(shoppingCart?if_exists.defaultShipAfterDate?if_exists,fmt,timeZone,locale) />
					 </#if>
                    <@displayTitleCell title=uiLabelMap.OrderShipAfterDateDefault titleClass="requiredField"/>
                    <@inputDateCell name="shipAfterDate" default=fechaDespues />
               </tr>
               <tr>
                	<#assign fechaAntes = '' />
                	<#if shoppingCart?if_exists.defaultShipBeforeDate?if_exists >
                		<#assign fechaAntes = Static["org.ofbiz.base.util.UtilDateTime"].timeStampToString(shoppingCart?if_exists.defaultShipBeforeDate?if_exists,fmt,timeZone,locale) />
					 </#if>
                    <@displayTitleCell title=uiLabelMap.OrderShipBeforeDateDefault titleClass="requiredField"/>
                    <@inputDateCell name="shipBeforeDate" default=fechaAntes />
                </tr>
				<tr>
                	<td align="right" class="titleCell">
					<@display text=uiLabelMap.AccountingTipoDocumento class="requiredField" />
					</td>
                	<@inputSelectCell name="acctgTransTypeId" list=eventosContables?if_exists?sort_by("descripcion") id="acctgTransTypeId" key="acctgTransTypeId" displayField="descripcion" required=false defaultOptionText="" />
                </tr>
                <tr>                	
                	<@displayTitleCell title=uiLabelMap.ProcesoAdjudicacion titleClass="requiredField"/>
                	<td>	   	    
     					<select name="tipoAdjudicacionId" id="tipoAdjudicacionId" size="1" class="selectBox" onChange="contactTypeChanged(this.form);">
		    				<option value=""></option>
		        				<#list tipoAdjudicacion as tipo>
		        						<option  value="${tipo.tipoAdjudicacionId}">${tipo.get("nombreTipoAdjudicacion",locale)}</option>
		        				</#list>
		   			    </select>	 
	    			</td>
                </tr>
                <tr>
         			<@displayTitleCell title=uiLabelMap.PrchMedioUtilizado titleClass="requiredField"/>
         			<#assign tiposId = ["I. Presencial", "II. Electr&oacute;nica", "III. Mixta"]/>
         			<td>
         				<select name="medioId" class="inputBox">
         					<option value=""></option>
         					<#list tiposId as tipoId>
            					<option value="${tipoId}">${tipoId}</option>
        					</#list>
        				</select>
         			</td>
      			</tr>       
                <tr>      
        			<@displayTitleCell title=uiLabelMap.SubProcesoAdjudicacion titleClass="requiredField" />
	    			<td>  
      					<select name="subTipoAdjudicacionId" id="subTipoAdjudicacionId" size="1" class="selectBox">      				  				
	  					</select>		
	  				</td>	
	 		    </tr>
                
                <tr>
                    <@displayTitleCell title=uiLabelMap.NumInvitacion/>
                    <td >
                        <@inputText name="numInvitacion" id="numInvitacion" size="60" />
                    </td>
                </tr>
                <tr>      
        			<@displayTitleCell title=uiLabelMap.PurchPedidoItemId />
	    			<td>  
      					<select name="articuloId" id="articuloId" size="1" class="selectBox" onChange="contactTypeChanged2(this.form);contactTypeChanged3(this.form);">      				  				
	  					</select>		
	  				</td>	
	 		    </tr>
	 		    <tr>      
        			<@displayTitleCell title=uiLabelMap.PurchFraccion />
	    			<td>  
      					<select name="fraccionId" id="fraccionId" size="1" class="selectBox">      				  				
	  					</select>		
	  				</td>	
	 		    </tr>
	 		<@inputTextRow title=uiLabelMap.NumContrato name="observacion" id="observacion" size="50" maxlength="255" />
            <#assign garantias = {"10":"10" , "20":"20"} />
			<@inputSelectHashRow name="garantia" title=uiLabelMap.PurchGarantia hash=garantias required=true titleClass="requiredField" />
			<@inputMultiSelectRow title=uiLabelMap.InformacionProcesoAdjudicacion name="descTipoAdjudicacion" id="descTipoAdjudicacion" list=[] />
			<@inputMultiSelectRow title=uiLabelMap.DocumentosRequeridos name="docsRequeridos" id="docsRequeridos" list=[] />
			<@inputMultiSelectRow title=uiLabelMap.DescArticulo name="descArtId" id="descArtId" list=[] />
			<@inputTextRow title="Importe del pago Anticipado" name="pagoAnticipado" id="pagoAnticipado" size="10" maxlength="50" />
			<@displayTitleCell title="Proveedor Pago Anticipado" titleClass="requiredField"/>
			<@inputAutoCompletePartyCell name="proveedorPagoAnticipado" size="25" default="${thisPartyId?if_exists}" onChange="supplierChanged()" />
            </table>
        </@form>
	</@frameSection>
</#if>

