
<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<script type="text/javascript">
	/*<![CDATA[*/
		
		function calculaAnticipoObra() 
		{ 	
			var valorContrato = parseFloat("0");
			var porcentajeAnticipoContrato = 0;
			var montoAnticipo = parseFloat("0");		
			var iva = 1.16;							
			if(document.getElementById('valorContrato').value != "")
			{	valorContrato = document.getElementById('valorContrato').value;
				valorContratoIva = document.getElementById('valorContratoConIva').value;
				if(document.getElementById('montoAnticipo').value != "")
				{	montoAnticipo = document.getElementById('montoAnticipo').value;					
					if(parseFloat(valorContratoIva) < parseFloat(montoAnticipo))
					{	alert("El porcentaje de anticipo de obra es incorrecto");
					}
					else
					{	if(valorContrato != null && valorContrato > 0)
						{	if(montoAnticipo != null)
							{	porcentajeAnticipoContrato = (montoAnticipo/valorContratoIva)*100;
								porcentajeAnticipoContrato.toPrecision(6);					
							}							
						}						
					}
				}
				
				var valorContratoConIva = valorContrato*iva;
				valorContratoConIva.toPrecision(2);
				document.getElementById('valorContratoConIva').value = valorContratoConIva;
			}																
			document.getElementById('porcentajeAnticipoContrato').value = porcentajeAnticipoContrato;			
					
		}
		
		campos=1;
		
		function clonar()
		{	var ret = ${retenciones.size()};
			if(campos<=ret)
			{	document.getElementById('retencionId'+campos).style.display = 'inline';
				document.getElementById("numRetenciones").value = campos;
				campos++;
			}
							
		}		
		
		function inicio()		
		{	var ret = ${retenciones.size()};
			for(i=1; i<=ret; i++)
			{	document.getElementById('retencionId'+i).style.display = 'none';								
			}
		}
		
		function eliminar()		
		{	if(campos >= 1)
			{	if(campos>1)
				{	campos--;
				}
				document.getElementById('retencionId'+campos).style.display = 'none';
				document.getElementById("numRetenciones").value = campos;							
			}	
		}
		
		
							
	/*]]>*/		
</script>

<#assign accionesCrearContratoObra = "">
<@form name="vistaObra" url="vistaObra" obraId=obraId/>
<#assign accionesCrearContratoObra>${accionesCrearContratoObra}<@submitFormLink form="vistaObra" text=uiLabelMap.ObrasInformacionObra class="subMenuButton"/></#assign>

<#--<@form name="datosContrato" url="crearContratoObra" obraId=obraId/>
<#assign accionesCrearContratoObra>${accionesCrearContratoObra}<@submitFormLink form="datosContrato" text=uiLabelMap.ObrasInicioContrato class="subMenuButton"/></#assign>-->

<@form name="buscarContrato" url="buscarContrato" obraId=obraId performFind="B"/>
<#assign accionesCrearContratoObra>${accionesCrearContratoObra}<@submitFormLink form="buscarContrato" text=uiLabelMap.ObrasConsultarContrato class="subMenuButton"/></#assign>

<@form name="seguimientoObra" url="seguimientoObra" obraId=obraId/>
<#assign accionesCrearContratoObra>${accionesCrearContratoObra}<@submitFormLink form="seguimientoObra" text=uiLabelMap.ObrasSeguimiento class="subMenuButton"/></#assign>

<@form name="modificacionesObra" url="modificacionesObra" obraId=obraId/>
<#assign accionesCrearContratoObra>${accionesCrearContratoObra}<@submitFormLink form="modificacionesObra" text=uiLabelMap.ObrasModificacionesObra class="subMenuButton"/></#assign>

<@form name="verSolicitud" url="verSolicitud" obraId=obraId performFind="B"/>
<#assign accionesCrearContratoObra>${accionesCrearContratoObra}<@submitFormLink form="verSolicitud" text=uiLabelMap.ObrasInformacionSolicitudObra class="subMenuButton"/></#assign>

<#--<@form name="finiquitoObra" url="verFiniquitoObra" obraId=obraId/>
<#assign accionesCrearContratoObra>${accionesCrearContratoObra}<@submitFormLink form="finiquitoObra" text=uiLabelMap.ObrasFiniquitoObra class="subMenuButton"/></#assign>-->
		
<@form name="finalizarObra" url="finalizarObra" obraId=obraId/>
<#assign accionesCrearContratoObra>${accionesCrearContratoObra}<@submitFormLink form="finalizarObra" text=uiLabelMap.FinalizarObra class="subMenuButtonDangerous"/></#assign>

			

<#if datosPoliza?has_content>
	<#list datosPoliza as poliza>
		<#assign agrupador = poliza.agrupador/>
		<#assign organizacion = poliza.organizationPartyId/>
		<@form name="verPoliza${agrupador}" url="viewAcctgTransPolizasLista" 
			tipoPolizaId=Static["org.ofbiz.accounting.util.UtilAccounting"].buscarIdPolizaXDocuActual(poliza.idTipoDoc,delegator) 
			agrupador=agrupador	otraOrganizacion=organizacion organizationPartyId=organizacion/>
		<#assign verPolizaAction>${verPolizaAction!}<@actionForm form="verPoliza${agrupador}" text=agrupador /></#assign>
	</#list>
</#if>	
<#assign accionesCrearContratoObra>${accionesCrearContratoObra}
			<#if datosPoliza?has_content>
				<div class="subMenuBar">
					<@selectActionForm name="shipmentActions" prompt=uiLabelMap.FinancialVerPoliza>
		   				${verPolizaAction?if_exists}
					</@selectActionForm>
				</div>
			</#if>
</#assign>
  		
<@frameSection title=uiLabelMap.ObrasDatosContrato extra=accionesCrearContratoObra?if_exists>	
<@form name="iniciarContratoObra" url="iniciarContratoObra" >
	<#assign urlHost = request.getServerName()+":"+request.getServerPort()/>
	<input type="hidden" name="numRetenciones" id="numRetenciones" value="0"/>		
		<table>		
			<@displayRow title=uiLabelMap.ObraObraId text=obraId/>
			<input type="hidden" name="obraId" value="${obraId}"/>
			<input type="hidden" name="urlHost" value="${urlHost}"/>							
			<@inputTextRow name="numContrato" title=uiLabelMap.ObraNumContrato titleClass="requiredField"/>
			<@inputTextRow name="descripcion" title=uiLabelMap.CommonDescription titleClass="requiredField"/>
			<@inputTextRow name="numReciboAnticipo" title=uiLabelMap.ObraNumReciboAnticipo titleClass="requiredField"/>
			<@inputTextRow name="numGarantiaAnticipo" title=uiLabelMap.ObraNumGarantiaAnticipo />
			<@inputTextRow name="numGarantiaCumplimiento" title=uiLabelMap.ObraNumGarantiaCumplimiento  />
			<@inputAutoCompletePartyRow title=uiLabelMap.ObraContratista name="contratistaId" id="autoCompleteWhichPartyId" size="20" titleClass="requiredField"/>
			<@inputAutoCompleteProductRow title=uiLabelMap.Product name="productId" titleClass="requiredField"/>
			<@inputCurrencySelectRow name="moneda" title=uiLabelMap.CommonCurrency defaultCurrencyUomId="MXN" titleClass="requiredField"/>
			<@inputDateRow name="fechaRealInicio" title=uiLabelMap.ObrasFechaRealInicio titleClass="requiredField"/>
			<@inputDateRow name="fechaRealFin" title=uiLabelMap.ObrasFechaRealFin titleClass="requiredField"/>
									
			<td><th>
        	<hr>
	  			<@displayTooltip text="Valor de la obra" />
	  		</th>																															
			<@inputTextRow name="valorContrato" id="valorContrato" title=uiLabelMap.ObravalorContratoSinIva onChange="calculaAnticipoObra(this.form)" titleClass="requiredField"/>
			<@inputTextRowCustom name="valorContratoConIva" id="valorContratoConIva" title=uiLabelMap.ObravalorContratoConIva titleClass="requiredField"/>
			<td><th>
        	<hr>
	  			<@displayTooltip text="Anticipo" />
	  		</th>
			<@inputTextRow name="montoAnticipo" id="montoAnticipo" title=uiLabelMap.ObraMontoAnticipoConIva onChange="calculaAnticipoObra(this.form)" titleClass="requiredField"/>
			<@inputTextRowCustom name="porcentajeAnticipoContrato" id="porcentajeAnticipoContrato" title=uiLabelMap.ObraPorcentajeAnticipo titleClass="requiredField"/>						
	  		<td><th>
        	<hr>
	  			<@displayTooltip text=uiLabelMap.Retenciones/>
	  		</th>
	  				<tr><td><td>	  				
	  				<a href='JavaScript:clonar();'> ${uiLabelMap.Anadir}</a>	  				
	  						        						   							   							
	  				<a href='JavaScript:eliminar();'> ${uiLabelMap.Eliminar}</a>
	  				</td></td>
	  				</tr>        				        					        						   							   							
   					
	  			</tr>	  			  		
	  		</th>
			<tr></tr>
								
			<#assign contadorRetencion = 1/>
			<#if retenciones?has_content>
				<#list contadorRetencion..retenciones.size()?if_exists as ret>					
        			<tr>
        			<td><@inputSelectCell list=retenciones?if_exists displayField="descripcion" key="retencionId" name="retencionId${contadorRetencion}" id="retencionId${contadorRetencion}" default="" /></td>
        			</tr>
					<#assign contadorRetencion = contadorRetencion+1/>
	    		</#list>					   					 		        							    						 							
			</#if>
						
			
			<td><th>
        	<hr>
	  			<@displayTooltip text=uiLabelMap.ObraPresupuestoMensual />
	  		</th>	  			  		

			<#assign contador = 1/>			
			<#assign mesIni = mesInicio?if_exists/>
			
			<input type="hidden" name="mesInicio" value="${mesInicio?if_exists}" />
			<#assign anioIni = anioInicio?if_exists/>
			<input type="hidden" name="anioInicio" value="${anioInicio?if_exists}"/> 
			<#list contador..numMeses?if_exists as meses>
                     <#if mesIni == 13>
                     	<#assign mesIni = 1/>
                     	<#assign anioIni = anioIni+1/>			  					  	
		        	 </#if>                     
                     <@inputCurrencyRow name="montoMes${contador}" currencyName="uomId" defaultCurrencyUomId="MXN" disableCurrencySelect=true title=uiLabelMap.ObraAnio + ": " + anioIni + " - Mes: " + mesIni titleClass="requiredField"/>
                     <#assign contador = contador+1/>
                     <#assign mesIni = mesIni+1/>                     
            </#list>
            <tr></tr>       										
				
										
			<@inputSubmitRow title=uiLabelMap.ObraCrearContrato />
		</table>
		
		
 		
 		<form id="formdinamico" name="formdinamico" action="prueba.php">
    
 
   
 
  </form>
		
<body onload="inicio();">			
			
</@form>
</@frameSection>

