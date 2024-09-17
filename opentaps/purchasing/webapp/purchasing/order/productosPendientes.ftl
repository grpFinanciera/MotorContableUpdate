<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<script language="JavaScript" type="text/javascript">
<!-- //

		////Funcion para cancelar el producto
		function contactTypeClickCancela(contador) 
		{	confirmar=confirm("Est\u00e1 seguro?"); 
		    var req = "requisicionId"+contador;
		    var det = "detalleRequisicionId"+contador;
			if (confirmar) 
			{	var requisicionId = document.getElementById(req).value;
			    var detalleRequisicionId = document.getElementById(det).value;
		  		if (requisicionId != "" && detalleRequisicionId != "") 
		    	{ 	var requestData = {'requisicionId' : requisicionId, 'detalleRequisicionId' : detalleRequisicionId};		    			    
		    		opentaps.sendRequest('cancelaProducto', requestData, function(data) {responseCancelaProducto(data)});
		    	}				
			}													
		}

		//Functions load the response from the server
		function responseCancelaProducto(data) 
		{	for (var key in data) 
			{	if(data[key]=="ERROR")
				{	alert("Ocurri\u00f3 un error al cancelar el producto");
				}
				else if(data[key]=="ERROR2")
				{	alert("No se puede cancelar el producto");
				}
				else
				{	alert("El producto se ha cancelado");
					document.getElementById('formFicticio').submit();	
				}	    		
			}		
		}		
		  
// -->
</script>

<form name="consultaProductosForm" method="post" action="">
  <table class="twoColumnForm">
    <tbody>
      <tr>
        <@inputSelectRow title=uiLabelMap.PurchasingStatus required=false list=estatusList  displayField="descripcion" name="statusId" default=statusId?if_exists />
      </tr>
      <tr>
      <@displayTitleCell title=uiLabelMap.PurchRequisicion />
      <@inputTextCell name="requisicionId" maxlength="50" size=10/>
      </tr>
      <tr>
      		<td class="titleCell">
        		<span class="tableheadtext">${uiLabelMap.PurchasingSolicitante}</span>
      		</td>
      		<td>
        		<input type="text" size="20" name="solicitanteId" class="inputBox">
        		<a href="javascript:call_fieldlookup2(document.consultaProductosForm.partyId,'LookupPerson');"><img src="/images/fieldlookup.gif" alt="Lookup" border="0" height="16" width="16"></a>
      		</td>
    	</tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.PurchasingProduct />
        <@inputAutoCompleteProductCell form="consultaProductosForm" name="productId" />
      </tr>
      <@inputDateRow name="fechaInicio" title=uiLabelMap.CommonStartDate form="consultaProductosForm" />
	  <@inputDateRow name="fechaFin" title=uiLabelMap.CommonEndDate form="consultaProductosForm" />
      <@inputSubmitRow title=uiLabelMap.CommonFind />
    </tbody>
  </table>
</form>

<#if productosList?has_content>
<@frameSection title="">
	<#if productosList?has_content>
	
	<table border="0" width="100%">

	<thead>		
		<tr>
			<th>${uiLabelMap.PurchRequisicion}</th>
			<th>${uiLabelMap.PurchDescripcion}</th>
			<th>${uiLabelMap.PurchasingSolicitante}</th>
			<th>${uiLabelMap.PurchasingNombreSolicitante}</th>
			<th>${uiLabelMap.PurchasingProduct}</th>
			<th>${uiLabelMap.PurchasingProductName}</th>
			<th>${uiLabelMap.PurchDescriptionArt}</th>
			<th>${uiLabelMap.FinancialsEndDate}</th>
			<th>${uiLabelMap.PurchasingCantidad}</th>		
			<th>${uiLabelMap.CommonAmount}</th>
			<th>${uiLabelMap.PurchasingProcedencia}</th>
			<th>${uiLabelMap.PurchReqIVA}</th>	
			<th>${uiLabelMap.PurchOrderParty}</th>
			<th></th>
		</tr>
	</thead>
	<tbody>
		<#assign count = 1 />
		<#list productosList as row>
				<#assign iva = row.iva!'' />
				<@inputHidden name="requisicionId" id="requisicionId${count}" value=row.requisicionId!/>
           		<@inputHidden name="detalleRequisicionId" id="detalleRequisicionId${count}" value=row.detalleRequisicionId!/>
						<tr class="${tableRowClass(row_index)}">
						    <@displayCell text=row.requisicionId/>
                			<@displayCell text=row.descripcion/>
                			<@displayCell text=row.personaSolicitanteId/>
                			<@displayCell text=row.nombreSolicitante/>
                			<@displayCell text=row.productId/>
               				<@displayCell text=row.productName/>
               				<@displayCell text=row.descripcionDetalle?if_exists/>
               				<@displayCell text=row.fechaEntrega?if_exists/>
                			<@displayCell text=row.cantidad/>
							<@displayCurrencyCell currencyUomId=row.tipoMoneda amount=row.monto/>
                			<@displayCell text=row.procedencia?if_exists?default("") />
							<#if iva?has_content && iva.equals("Y")>
								<@displayCell text="Si" />
							<#else>
								<@displayCell text="No" />
							</#if>
                			<@displayCell text=row.groupName/>
                			<td align="center" width="7%">
                			<#if row.estatusProducto!='CANCELADO'&&row.estatusProducto!='ORDENADO'>
								<input name="submitButton" id="elimina${count}" type="submit" class="subMenuButtonDangerous" value="${uiLabelMap.PurchasingCancela}" onClick="contactTypeClickCancela('${count}')"/>
							</#if>									
							</td>		
						</tr>
						<tr class="${tableRowClass(row_index)}">
							<td colspan="8" valign="top">
							<@flexArea targetId="ClavePresupuestal${row.requisicionId}${row.detalleRequisicionId}" title=uiLabelMap.ClavePresupuestal controlStyle="font-weight: bold;" >
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
			<#assign count=count + 1/>
		</#list>
	</tbody>
	</table>
	</#if>
</@frameSection>
</#if>

<form name="formFicticio" id="formFicticio" method="post" action="">
</form>
