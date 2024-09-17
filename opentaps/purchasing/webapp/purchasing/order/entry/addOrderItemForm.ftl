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

<script language="JavaScript" type="text/javascript">
<!-- //

		////Funcion para agregar item a la orden
		function contactTypeClickAdd(contador) 
		{	confirmar=confirm("¿Est\u00e1 seguro?"); 
			if (confirmar) 
			{	
				var Art = new Articulo();
				Art.contador = contador;
				Art.getDatos();
				if (productId != "" && quantity != "") 
				    { 
				    	var requestData = {'productId' : Art.productId, 'quantity' : Art.quantity, 'clasificacion1' : Art.mapaClas[1], 'clasificacion2' : Art.mapaClas[2]
				    	, 'clasificacion3' : Art.mapaClas[3], 'clasificacion4' : Art.mapaClas[4], 'clasificacion5' : Art.mapaClas[5], 'clasificacion6' : Art.mapaClas[6]
					    , 'clasificacion7' : Art.mapaClas[7], 'clasificacion8' : Art.mapaClas[8], 'clasificacion9' : Art.mapaClas[9], 'clasificacion10' : Art.mapaClas[10]
					    , 'clasificacion11' : Art.mapaClas[11], 'clasificacion12' : Art.mapaClas[12], 'clasificacion13' : Art.mapaClas[13], 'clasificacion14' : Art.mapaClas[14]
					    , 'clasificacion15' : Art.mapaClas[15], 'monto' : Art.monto, 'detalleRequisicionId' : Art.detalleRequisicionId, 'requisicionId' : Art.requisicionId
					    , 'customTimePeriodId' : Art.customTimePeriodId};	   	   
				
				    	opentaps.sendRequest('addOrderItem', requestData, function(data) {responseAdd(data)});
				    }				
			}													
		}
		
		//Functions load the response from the server
		function responseAdd(data) 
		{	
			if(data['mensaje']){
				alert(data['mensaje']);				
			} else {
				alert("El producto se ha agregado");
				document.getElementById('modifycart').submit();						
			}
			
		}	
		
		var Articulo = function () {
			var contador = "";
			var mapaClas = {};
			var productId = "";
			var quantity = "";
			var monto = "";
			var detalleRequisicionId = "";
			var requisicionId = "";
			var customTimePeriodId = "";
			var mesId = "";
			this.getDatos = function (){
				this.productId = document.getElementById('ComboBox_productId'+this.contador).value;
				this.quantity = document.getElementById('cantidad'+this.contador).value;
				this.monto = document.getElementById('monto'+this.contador).value;
				this.detalleRequisicionId = document.getElementById('detalleRequisicionId'+this.contador).value;
				this.requisicionId = document.getElementById('requisicionId'+this.contador).value;
				this.customTimePeriodId = document.getElementById('customTimePeriodId'+this.contador).value;
				this.mesId = document.getElementById('mesId'+this.contador).value;
				this.mapaClas = {};
				for (i = 1; i <= 15; i++) {
					var clasificacion = document.getElementById('clasificacion'+i+'_'+this.contador);
					if(clasificacion != null){
						this.mapaClas[i] = clasificacion.value;
					} else {
						this.mapaClas[i] = '';
					}
				}
			}
		};
		function cambiarTodo(renglones){
			for (var i = 1; i<=parseInt(renglones) ; i++) {
				var nombre = 'check'+i;
				var checkBox = document.getElementById(nombre);
				var checkPrl = document.getElementById('CheckTodos');
				checkBox.checked = checkPrl.checked;
				guardaValorCheck(i);
			}
		}
		
		function guardaValorCheck(contador){
			var check = document.getElementById('check'+contador);
				var Art = new Articulo();
				Art.contador = contador;
				Art.getDatos();
				var datosEnvio = Art.productId+":_"+Art.quantity+":_"+Art.monto+":_"+Art.detalleRequisicionId+":_"+Art.requisicionId+":_"+Art.customTimePeriodId+":_"+Art.mesId;
				for (i = 1; i <= 15; i++) {
					datosEnvio = datosEnvio+":_"+Art.mapaClas[i];
				}
				check.value = datosEnvio;
		}

	function setShippingDestination() {
		document.facilityForm.submit();
	}
	
	function redirectUrlAndDisableLink(url, link, afterClickText) {
    	var subMenuBar = link.parentNode;
    	// remove the link
    	opentaps.removeNode(link);
    	// add a fake link with new label
    	var newLink = opentaps.createAnchor(null, '#', afterClickText, 'subMenuButton disabled', null);
    	newLink.disabled = true;
    	newLink.style.cursor = 'wait';
    	subMenuBar.appendChild(newLink);
    	// redirect self location to url
    	window.location.href = url;
    	return false;
	}

// -->
</script>

<#-- finalizeOrder?finalizeMode=init -->
<#if shoppingCart.size() != 0>
  <#assign finalizeLink ><a class="subMenuButton" href="#" onclick="redirectUrlAndDisableLink('/purchasing/control/processorder',this,'Favor de esperar')" >${uiLabelMap.OpentapsFinalizeOrder}</a> </#assign>
</#if>

<@frameSectionTitleBar title=uiLabelMap.OrderOrders titleClass="sectionHeaderTitle" titleId="sectionHeaderTitle_order" extra=finalizeLink! />

<#assign createOrderExtraButtons>
<a class="toggleButton" href="createOrderMainScreen?&amp;useGwt=Y">${uiLabelMap.OpentapsGridView}</a><span class="toggleButtonDisabled">${uiLabelMap.OpentapsFullView}</span>
</#assign>

<@frameSectionHeader title=uiLabelMap.OrderCreateOrder extra=createOrderExtraButtons />
<form name="addOrderItemForm" id="addOrderItemForm" method="POST" action="<@ofbizUrl>addOrderItem</@ofbizUrl>">
<div class="screenlet">
  <table class="fourColumnForm">
    <tr>
      <@displayTitleCell title=uiLabelMap.ProductProductId titleClass="requiredField"/>
      <@inputAutoCompleteProductCell name="productId" errorField="productId" url="getAutoCompleteProductNoVirtual"/>
    </tr>
	<@inputCurrencyRow name="monto" currencyName="uomId" defaultCurrencyUomId="MXN" disableCurrencySelect=true
							 title=uiLabelMap.CommonAmount titleClass="requiredField"/>    
    <@inputDateRow title=uiLabelMap.OrderShipBeforeDate name="shipBeforeDate" errorField="shipBeforeDate" />
    <@inputTextareaRow title=uiLabelMap.CommonComment name="comments" cols=40/>
    	<#if tagTypes?has_content>
			<@displayCvesPresupRow tagTypes=tagTypes titleClass="requiredField"/>
		</#if>
	<@inputCurrencyRow name="quantity" id="quantity" disableCurrencySelect=true title=uiLabelMap.CommonQuantity default=1 titleClass="requiredField" />
	<@inputSelectRow title=uiLabelMap.CommonMonth name="customTimePeriodId" id="customTimePeriodId" list=[] titleClass="requiredField"/>
	<@inputTextRow name="cantidadMeses" title="Cantidad de meses" size=2 maxlength=2 default="1" titleClass="requiredField"/>
  <tr>
    <td>&nbsp;</td>
    <td>
  <input type="button" value="${uiLabelMap.OrderAddToOrder}" class="smallSubmit" onclick="javascript:opentaps.checkSupplierProduct(this, document.getElementById('productId').value, '${shoppingCart.partyId?if_exists}', '${shoppingCart.getCurrency()?if_exists}', document.getElementById('quantity').value, '${uiLabelMap.PurchOrderConfirmNotExistSupplierProduct}', true)" />
  </td>
  </tr>
  </table>
</div>
</form>

<@frameSection title=uiLabelMap.AddProduct>
<form name="consultaProductosPendientesForm" method="post" action="">
  <table class="twoColumnForm">
    <tbody>
      <tr>
        <@displayTitleCell title=uiLabelMap.PurchRequisicionId/>
        <@inputTextCell name="requisicionId" maxlength=60  />
      </tr>
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>

	<#if productosList?has_content>
	<form name="agregaArticulosOrden" method="post" action="<@ofbizUrl>agregaArticulosOrden</@ofbizUrl>">
	<table border="0" width="100%" >
	<thead>		
		<tr>
			<th>${uiLabelMap.PurchRequisicion}</th>
			<th>${uiLabelMap.CommonMonth}</th>
			<th>${uiLabelMap.PurchDescripcion}</th>
			<th>${uiLabelMap.PurchasingSolicitante}</th>
			<th>${uiLabelMap.PurchasingProduct}</th>
			<th>${uiLabelMap.PurchasingProductName}</th>
			<th>${uiLabelMap.PurchDescriptionArt}</th>
			<th>${uiLabelMap.FinancialsEndDate}</th>
			<th>${uiLabelMap.PurchasingCantidad}</th>			
			<th>${uiLabelMap.CommonAmount}</th>
			<th>${uiLabelMap.PurchasingProcedencia}</th>
			<th>${uiLabelMap.PurchReqIVA}</th>	
			<th>${uiLabelMap.PurchOrderParty}</th>
			<#-- <th></th> -->
			<th align="center" >
				<input name="submitButton" type="submit" class="subMenuButton" value="${uiLabelMap.AnadirArticulos}" align="center" />
				</br>
				<@inputCheckbox name="CheckTodos" onClick="javascript:cambiarTodo(${productosList?size});" />
			</th>	
		</tr>
	</thead>
	<tbody>
		<#assign count = 1 />
		<#list productosList as row>	
						<#assign iva = row.iva?if_exists/>
						<tr class="${tableRowClass(row_index)}">
							<@inputHidden name="requisicionId" id="requisicionId${count}" value=row.requisicionId!/>
							<@inputHidden name="detalleRequisicionId" id="detalleRequisicionId${count}" value=row.detalleRequisicionId!/>
							<@inputHidden name="cantidad" id="cantidad${count}" value=row.cantidad!/>
							<@inputHidden name="monto" id="monto${count}" value=row.monto!/>
							<@inputHidden name="customTimePeriodId" id="customTimePeriodId${count}" value=row.customTimePeriodId!/>
							<@inputHidden name="mesId" id="mesId${count}" value=row.mesId!/>
						    <@displayCell text=row.requisicionId/>
							<@displayCell text=row.mesId!/>
                			<@displayCell text=row.descripcion/>
                			<@displayCell text=row.personaSolicitanteId/>
                			<@inputAutoCompleteProductCell name="productId" id="productId${count}" default=row.productId onChange="guardaValorCheck('${count}')" />
               				<@displayCell text=row.productName/>
               				<@displayCell text=row.descripcionDetalle?if_exists/>
               				<@displayCell text=row.fechaEntrega?if_exists/>
                			<@displayCell text=row.cantidad/>
                			<@displayCurrencyCell currencyUomId=row.tipoMoneda amount=row.monto/>
							<@displayCell text=row.procedencia?if_exists?default("") />
							<#if iva == "Y">
								<@displayCell text="Si" />
							<#else>
								<@displayCell text="No" />
							</#if>
                			<@displayCell text=row.groupName/>
							<#assign valorCheck = row.productId+":_"+row.cantidad+":_"+row.monto+":_"+row.detalleRequisicionId+":_"+row.requisicionId />
							<#list 1..15 as i>
							  <#assign clasNombre = "clasificacion"+i />
							  <#assign valorCheck = valorCheck+":_"+row.get(clasNombre)! />
							</#list>  
							<@inputCheckboxCell name="check" id="check${count}" value="${valorCheck}" onChange="guardaValorCheck(${count});"/>
						</tr>
			        <tr class="${tableRowClass(row_index)}">
			          <td colspan="8" align="left">
			          <@flexArea targetId="ClavePresupuestalADD${row_index}" title=uiLabelMap.ClavePresupuestal controlStyle="font-weight: bold;" >
			            <table border="0" cellpadding="0" cellspacing="0" width="100%">
			              <#if tagTypes?has_content>
			                <@cvePresupItemsAprov tags=tagTypes item=row index=count onChange="guardaValorCheck(${count});"/>
			              </#if>	
			            </table>
			           </@flexArea>
			          </td>
			        </tr>						
			<tr>
				<td>
					&nbsp;
				</td>
			</tr>
			<#assign count=count + 1/>
		</#list>
	</tbody>
	</table>
	</form>
	</#if>
</@frameSection>

<form method="post" action="<@ofbizUrl>modifycart</@ofbizUrl>" name="modifycart" id="modifycart"style="margin: 0;">
	<@inputHidden name="performFind" value="D"/>
</form>