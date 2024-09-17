
<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<script type="text/javascript">
		
		function calculaAnticipoObra() 
		{ 	
			var valorContrato = parseFloat("0");
			var porcentajeAnticipoContrato = 0;
			var montoAnticipo = parseFloat("0");		
			var iva = 1.16;							
			if(document.getElementById('valorContrato').value != "")
			{	valorContrato = document.getElementById('valorContrato').value;
				if(document.getElementById('montoAnticipo').value != "")
				{	montoAnticipo = document.getElementById('montoAnticipo').value;					
					if(parseFloat(valorContrato) < parseFloat(montoAnticipo))
					{	alert("El porcentaje de anticipo de obra es incorrecto");
					}
					else
					{	if(valorContrato != null && valorContrato > 0)
						{	if(montoAnticipo != null)
							{	porcentajeAnticipoContrato = (montoAnticipo/valorContrato)*100;
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
		
		function validaPorcentajeMillar() 
		{	var porcentaje = null;												
			if(document.getElementById('retencionMillarIVDGLE').value != "")
			{	porcentaje = document.getElementById('retencionMillarIVDGLE').value;
				if(porcentaje>100 || porcentaje<0)
				{	alert("El porcentaje de retencion millar");
					document.getElementById('retencionMillarIVDGLE').value = 0;					
				}					
			}																		
		}
		
		function validaPorcentajeCmic() 
		{	var porcentaje = null;												
			if(document.getElementById('retencionMillarICIC').value != "")
			{	porcentaje = document.getElementById('retencionMillarICIC').value;
				if(porcentaje>100 || porcentaje<0)
				{	alert("El porcentaje de retencion CMIC");
					document.getElementById('retencionMillarICIC').value = 0;					
				}					
			}																		
		}				
		
</script>

<#assign accionesVerContratoObra = "">

<@form name="vistaObra" url="vistaObra" obraId=obraId/>
<#assign accionesVerContratoObra>${accionesVerContratoObra}<@submitFormLink form="vistaObra" text=uiLabelMap.ObrasInformacionObra class="subMenuButton"/></#assign>

<@form name="finiquitoObra" url="verFiniquitoObra" obraId=obraId contratoId=contratoId/>
<#assign accionesVerContratoObra>${accionesVerContratoObra}<@submitFormLink form="finiquitoObra" text=uiLabelMap.ObrasFiniquitoObra class="subMenuButton"/></#assign>

<@form name="buscarContrato" url="buscarContrato" obraId=obraId performFind="B"/>
<#assign accionesVerContratoObra>${accionesVerContratoObra}<@submitFormLink form="buscarContrato" text=uiLabelMap.ObrasConsultarContrato class="subMenuButton"/></#assign>

<@form name="datosContrato" url="crearContratoObra" obraId=obraId/>
<#assign accionesVerContratoObra>${accionesVerContratoObra}<@submitFormLink form="datosContrato" text=uiLabelMap.ObrasInicioContrato class="subMenuButton"/></#assign>

<@form name="modificacionesObra" url="modificacionesObra" obraId=obraId/>
<#assign accionesVerContratoObra>${accionesVerContratoObra}<@submitFormLink form="modificacionesObra" text=uiLabelMap.ObrasModificacionesObra class="subMenuButton"/></#assign>

<@form name="seguimientoObra" url="seguimientoObra" obraId=obraId performFind="B" contratoId=contratoId/>
<#assign accionesVerContratoObra>${accionesVerContratoObra}<@submitFormLink form="seguimientoObra" text=uiLabelMap.ObrasSeguimiento class="subMenuButton"/></#assign>

<@form name="verSolicitud" url="verSolicitud" obraId=obraId performFind="B"/>
<#assign accionesVerContratoObra>${accionesVerContratoObra}<@submitFormLink form="verSolicitud" text=uiLabelMap.ObrasInformacionSolicitudObra class="subMenuButton"/></#assign>			

<#if datosPoliza?has_content>
	<#list datosPoliza as poliza>
		<#assign acctgTransId = poliza.acctgTransId/>
		<#assign poliza = poliza.poliza/>
		<@form name="verPoliza${poliza}" url="viewAcctgTrans"  
			acctgTransId=acctgTransId />
		<#assign verPolizaAction>${verPolizaAction!}<@actionForm form="verPoliza${poliza}" text=acctgTransId /></#assign>
	</#list>
</#if>	
<#assign accionesVerContratoObra>${accionesVerContratoObra}
			<#if datosPoliza?has_content>
				<div class="subMenuBar">
					<@selectActionForm name="shipmentActions" prompt=uiLabelMap.FinancialVerPoliza>
		   				${verPolizaAction?if_exists}
					</@selectActionForm>
				</div>
			</#if>
</#assign>
  		
<@frameSection title=uiLabelMap.ObrasDatosContrato extra=accionesVerContratoObra?if_exists>	

	<#list datosInicio as inicio>
	<#assign urlHost = request.getServerName()+":"+request.getServerPort()/>		
		<table>
			<@displayRow title=uiLabelMap.ObraObraId text=obraId/>
			<@displayRow title=uiLabelMap.ObraContratoId text=contratoId/>
			<input type="hidden" name="obraId" value="${obraId}"/>
			<input type="hidden" name="contratoId" value="${contratoId}"/>
			<input type="hidden" name="urlHost" value="${urlHost}"/>							
			<@displayRow title=uiLabelMap.ObraNumContrato text=inicio.numContrato?if_exists/>			
			<@displayRow title=uiLabelMap.CommonDescription text=inicio.descripcion?if_exists/>
			<@displayRow title=uiLabelMap.ObraNumReciboAnticipo text=inicio.numReciboAnticipo?if_exists/>
			<@displayRow title=uiLabelMap.ObraNumGarantiaAnticipo text=inicio.numGarantiaAnticipo?if_exists/>
			<@displayRow title=uiLabelMap.ObraNumGarantiaCumplimiento text=inicio.numGarantiaCumplimiento?if_exists/>			
			<@displayRow title=uiLabelMap.ObraContratista text=inicio.contratistaId?if_exists/>
			<@displayRow title=uiLabelMap.Product text=inicio.productId?if_exists/>
			<@displayRow title=uiLabelMap.ObrasFechaRealInicio text=inicio.fechaRealInicio?if_exists/>
			<@displayRow title=uiLabelMap.ObrasFechaRealFin text=inicio.fechaRealFin?if_exists/>
									
			<td><th>
        	<hr>
	  			<@displayTooltip text="Valor de la obra" />
	  		</th>																	
			<@displayCurrencyRow title=uiLabelMap.ObravalorContratoSinIva amount=inicio.valorContrato?if_exists currencyUomId=inicio.moneda/>
			<@displayCurrencyRow title=uiLabelMap.ObravalorContratoConIva amount=inicio.valorContratoConIva?if_exists currencyUomId=inicio.moneda/>
			<td><th>
	        <hr>
		  		<@displayTooltip text="Anticipo" />
	  		</th>

			<@displayCurrencyRow title=uiLabelMap.ObraMontoAnticipo amount=inicio.montoAnticipo?if_exists currencyUomId=inicio.moneda/>
			<@displayRow title=uiLabelMap.ObraPorcentajeAnticipo text=inicio.porcentajeAnticipo?if_exists+"%"/>			
		
	
	<#if datosRetencion?has_content>
			<td><th>
        	<hr>
	  			<@displayTooltip text="Retenciones" />
	  		</th>

	  		
	  		<tr>
	  			<td align="right"><span style="font-weight:bold">${uiLabelMap.ObraRetencionId}</span></td>
				<td align="center"><span style="font-weight:bold">${uiLabelMap.ObraNombreRetencion}</span></td>
				<td align="center"><span style="font-weight:bold">${uiLabelMap.ObraAuxiliar}</span></td>
				<td align="center"><span style="font-weight:bold">${uiLabelMap.ObraGroupName}</span></td>				
				<td align="center"><span style="font-weight:bold">${uiLabelMap.ObraPorcentaje}</span></td>
			</tr>	
		<#list datosRetencion as datoReten>
			<tr>
				<td align="right">${datoReten.retencionId}</td>
				<td align="center">${datoReten.nombreRetencion}</td>
				<td align="center">${datoReten.auxiliar}</td>
				<td align="center">${datoReten.groupName}</td>				
				<td align="center">${datoReten.porcentaje}%</td>
				
				
			</tr>
		</#list>		
	</#if>
	
	<#if datosPeriodos?has_content>
		<td><th>
        <hr>
		  	<@displayTooltip text=uiLabelMap.ObraPresupuestoMensual />
	  	</th>	  			  		
	  			  		
		<#list datosPeriodos as periodo>			
			<tr>
			<@displayCellCustom name="texto" text=uiLabelMap.ObraAnio + ": " + periodo.ciclo + " - Mes: " + periodo.mes/>
			<@displayCurrencyCell amount=periodo.monto currencyUomId=inicio.moneda />
			</tr>
		</#list>		
	</#if>		
			
	</#list>	
			
			
			
																							
		</table>
								

</@frameSection>