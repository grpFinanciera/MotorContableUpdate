
<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#assign idPoliza=""/>
<#assign clavePresupuestal=""/>

<#assign acctgTransId=""/>
<#assign acctgTransTypeId=""/>
<#assign transactionDate=""/>
<#assign postedDate=""/>
<#assign theirPartyId=""/>
<#assign productId=""/>
<#assign glFiscalTypeId=""/>
<#assign partyId=""/>
<#assign createdByUserLogin=""/>
<#assign idTipoDoc=""/>
<#assign agrupador=""/>
<#assign tipoPolizaIdPres=""/>
<#assign tipoPolizaIdCont=""/>

<#assign clas1=""/>
<#assign clas2=""/>
<#assign clas3=""/>
<#assign clas4=""/>
<#assign clas5=""/>
<#assign clas6=""/>
<#assign clas7=""/>
<#assign clas8=""/>
<#assign clas9=""/>
<#assign clas10=""/>
<#assign clas11=""/>
<#assign clas12=""/>
<#assign clas13=""/>
<#assign clas14=""/>
<#assign clas15=""/>

<#assign clas01=""/>
<#assign clas02=""/>
<#assign clas03=""/>
<#assign clas04=""/>
<#assign clas05=""/>
<#assign clas06=""/>
<#assign clas07=""/>
<#assign clas08=""/>
<#assign clas09=""/>
<#assign clas010=""/>
<#assign clas011=""/>
<#assign clas012=""/>
<#assign clas013=""/>
<#assign clas014=""/>
<#assign clas015=""/>



<#list mapTransPresupPol?keys as idPoliza>
		<#if mapTransPresupPol.get(idPoliza)??>
						<#if idPoliza=="idPoliza">
							<#assign idPoliza=idPoliza?replace("idPoliza","<b>Identificador de Póliza</b>")+": "+ mapTransPresupPol.get(idPoliza) />
						</#if>				
						<#if idPoliza=="clavePresupuestal">
							<#assign clavePresupuestal=idPoliza?replace("clavePresupuestal","<b>Clave Presupuestal</b>")+": "+ mapTransPresupPol.get(idPoliza) />
						</#if>		
						<#if idPoliza=="acctgTransId">
							<#assign acctgTransId=idPoliza?replace("acctgTransId","<b>Identificador de la Transacción</b>")+": "+ mapTransPresupPol.get(idPoliza) />
						</#if>	
						<#if idPoliza=="transactionDate">
							<#assign transactionDate=idPoliza?replace("transactionDate","<b>Fecha de Registro</b>")+": "+ mapTransPresupPol.get(idPoliza) />
						</#if>		
						<#if idPoliza=="postedDate">
							<#assign postedDate=idPoliza?replace("postedDate","<b>Fecha Contable</b>")+": "+ mapTransPresupPol.get(idPoliza) />
						</#if>	
						<#if idPoliza=="createdByUserLogin">
							<#assign createdByUserLogin=idPoliza?replace("createdByUserLogin","<b>Usuario</b>")+": "+ mapTransPresupPol.get(idPoliza) />
						</#if>	
						<#if idPoliza=="agrupador">
							<#assign agrupador=idPoliza?replace("agrupador","<b>Identificador de la Póliza</b>")+": "+ mapTransPresupPol.get(idPoliza) />
						</#if>	
						<#if idPoliza=="theirPartyId">
							<#assign theirPartyId=idPoliza?replace("theirPartyId","<b>Auxiliar</b>")+": "+ mapTransPresupPol.get(idPoliza) />
						</#if>
						<#if idPoliza=="productId">
							<#assign productId=idPoliza?replace("productId","<b>Producto</b>")+": "+ mapTransPresupPol.get(idPoliza) />
						</#if>
		</#if>
</#list>

<#list mapTransPresup?keys as idPoliza>
		<#if mapTransPresup.get(idPoliza)??>
						<#if idPoliza=="description">
							<#assign idTipoDoc=idPoliza?replace("description","<b>Tipo de Documento</b>")+": "+ mapTransPresup.get(idPoliza) />
						</#if>							
		</#if>
</#list>

<#list mapTransTipoTrans?keys as idPoliza>
		<#if mapTransTipoTrans.get(idPoliza)??>
						<#if idPoliza=="description">
							<#assign acctgTransTypeId=idPoliza?replace("description","<b>Tipo de Transacción</b>")+": "+ mapTransTipoTrans.get(idPoliza) />
						</#if>									
		</#if>
</#list>

<#list mapTransTipoFisca?keys as idPoliza>
		<#if mapTransTipoFisca.get(idPoliza)??>
						<#if idPoliza=="description">
							<#assign glFiscalTypeId=idPoliza?replace("description","<b>Tipo Fiscal</b>")+": "+ mapTransTipoFisca.get(idPoliza) />
						</#if>									
		</#if>
</#list>

<#list mapTransEntidadCo?keys as idPoliza>
		<#if mapTransEntidadCo.get(idPoliza)??>
						<#if idPoliza=="groupName">
							<#assign partyId=idPoliza?replace("groupName","<b>Entidad Contable</b>")+": "+ mapTransEntidadCo.get(idPoliza) />
						</#if>									
		</#if>
</#list>

<#list mapTransTipoPoliz?keys as idPoliza>
		<#if mapTransTipoPoliz.get(idPoliza)??>
						<#if idPoliza=="description">
							<#assign tipoPolizaIdPres=idPoliza?replace("description","<b>Tipo de Póliza</b>")+": "+ mapTransTipoPoliz.get(idPoliza) />
						</#if>									
		</#if>
</#list>

<#list mapTransTipoPolizCont?keys as idPoliza>
		<#if mapTransTipoPolizCont.get(idPoliza)??>
						<#if idPoliza=="description">
							<#assign tipoPolizaIdCont=idPoliza?replace("description","<b>Tipo de Póliza</b>")+": "+ mapTransTipoPolizCont.get(idPoliza) />
						</#if>									
		</#if>
</#list>

<#list mapTransClavePres?keys as clavePresupuestal>
		<#if mapTransClavePres.get(clavePresupuestal)??>			
						<#if clavePresupuestal=="clasificacion1">
							<#assign clas1=clavePresupuestal?replace("clasificacion1","<b> - </b>")+ mapTransClavePres.get(clavePresupuestal) />
						</#if>		
						<#if clavePresupuestal=="clasificacion2">
							<#assign clas2=clavePresupuestal?replace("clasificacion2","<b> - </b>")+ mapTransClavePres.get(clavePresupuestal) />
						</#if>	
						<#if clavePresupuestal=="clasificacion3">
							<#assign clas3=clavePresupuestal?replace("clasificacion3","<b> - </b>")+ mapTransClavePres.get(clavePresupuestal) />
						</#if>	
						<#if clavePresupuestal=="clasificacion4">
							<#assign clas4=clavePresupuestal?replace("clasificacion4","<b> - </b>")+ mapTransClavePres.get(clavePresupuestal) />
						</#if>	
						<#if clavePresupuestal=="clasificacion5">
							<#assign clas5=clavePresupuestal?replace("clasificacion5","<b> - </b>")+ mapTransClavePres.get(clavePresupuestal) />
						</#if>	
						<#if clavePresupuestal=="clasificacion6">
							<#assign clas6=clavePresupuestal?replace("clasificacion6","<b> - </b>")+ mapTransClavePres.get(clavePresupuestal) />
						</#if>	
						<#if clavePresupuestal=="clasificacion7">
							<#assign clas7=clavePresupuestal?replace("clasificacion7","<b> - </b>")+ mapTransClavePres.get(clavePresupuestal) />
						</#if>	
						<#if clavePresupuestal=="clasificacion8">
							<#assign clas8=clavePresupuestal?replace("clasificacion8","<b> - </b>")+ mapTransClavePres.get(clavePresupuestal) />
						</#if>	
						<#if clavePresupuestal=="clasificacion9">
							<#assign clas9=clavePresupuestal?replace("clasificacion9","<b> - </b>")+ mapTransClavePres.get(clavePresupuestal) />
						</#if>	
						<#if clavePresupuestal=="clasificacion10">
							<#assign clas10=clavePresupuestal?replace("clasificacion10","<b> - </b>")+ mapTransClavePres.get(clavePresupuestal) />
						</#if>	
						<#if clavePresupuestal=="clasificacion11">
							<#assign clas11=clavePresupuestal?replace("clasificacion11","<b> - </b>")+ mapTransClavePres.get(clavePresupuestal) />
						</#if>	
						<#if clavePresupuestal=="clasificacion12">
							<#assign clas12=clavePresupuestal?replace("clasificacion12","<b> - </b>")+ mapTransClavePres.get(clavePresupuestal) />
						</#if>	
						<#if clavePresupuestal=="clasificacion13">
							<#assign clas13=clavePresupuestal?replace("clasificacion13","<b> - </b>")+ mapTransClavePres.get(clavePresupuestal) />
						</#if>	
						<#if clavePresupuestal=="clasificacion14">
							<#assign clas14=clavePresupuestal?replace("clasificacion14","<b> - </b>")+ mapTransClavePres.get(clavePresupuestal) />
						</#if>	
						<#if clavePresupuestal=="clasificacion15">
							<#assign clas15=clavePresupuestal?replace("clasificacion15","<b> - </b>")+ mapTransClavePres.get(clavePresupuestal) />
						</#if>			
		</#if>
</#list>

<#list mapEstructuraClave?keys as estructuraClave>
		<#if mapEstructuraClave.get(estructuraClave)??>			
						<#if estructuraClave=="clasificacion1">
							<#assign clas01=estructuraClave?replace("clasificacion1","<b>Clasificación 1</b>")+": "+ mapEstructuraClave.get(estructuraClave) />
						</#if>		
						<#if estructuraClave=="clasificacion2">
							<#assign clas02=estructuraClave?replace("clasificacion2","<b>Clasificación 2</b>")+": "+ mapEstructuraClave.get(estructuraClave) />
						</#if>	
						<#if estructuraClave=="clasificacion3">
							<#assign clas03=estructuraClave?replace("clasificacion3","<b>Clasificación 3</b>")+": "+ mapEstructuraClave.get(estructuraClave) />
						</#if>	
						<#if estructuraClave=="clasificacion4">
							<#assign clas04=estructuraClave?replace("clasificacion4","<b>Clasificación 4</b>")+": "+ mapEstructuraClave.get(estructuraClave) />
						</#if>	
						<#if estructuraClave=="clasificacion5">
							<#assign clas05=estructuraClave?replace("clasificacion5","<b>Clasificación 5</b>")+": "+ mapEstructuraClave.get(estructuraClave) />
						</#if>	
						<#if estructuraClave=="clasificacion6">
							<#assign clas06=estructuraClave?replace("clasificacion6","<b>Clasificación 6</b>")+": "+ mapEstructuraClave.get(estructuraClave) />
						</#if>	
						<#if estructuraClave=="clasificacion7">
							<#assign clas07=estructuraClave?replace("clasificacion7","<b>Clasificación 7</b>")+": "+ mapEstructuraClave.get(estructuraClave) />
						</#if>	
						<#if estructuraClave=="clasificacion8">
							<#assign clas08=estructuraClave?replace("clasificacion8","<b>Clasificación 8</b>")+": "+ mapEstructuraClave.get(estructuraClave) />
						</#if>	
						<#if estructuraClave=="clasificacion9">
							<#assign clas09=estructuraClave?replace("clasificacion9","<b>Clasificación 9</b>")+": "+ mapEstructuraClave.get(estructuraClave) />
						</#if>	
						<#if estructuraClave=="clasificacion10">
							<#assign clas010=estructuraClave?replace("clasificacion10","<b>Clasificación 10</b>")+": "+ mapEstructuraClave.get(estructuraClave) />
						</#if>	
						<#if estructuraClave=="clasificacion11">
							<#assign clas011=estructuraClave?replace("clasificacion11","<b>Clasificación 11</b>")+": "+ mapEstructuraClave.get(estructuraClave) />
						</#if>	
						<#if estructuraClave=="clasificacion12">
							<#assign clas012=estructuraClave?replace("clasificacion12","<b>Clasificación 12</b>")+": "+ mapEstructuraClave.get(estructuraClave) />
						</#if>	
						<#if estructuraClave=="clasificacion13">
							<#assign clas013=estructuraClave?replace("clasificacion13","<b>Clasificación 13</b>")+": "+ mapEstructuraClave.get(estructuraClave) />
						</#if>	
						<#if estructuraClave=="clasificacion14">
							<#assign clas014=estructuraClave?replace("clasificacion14","<b>Clasificación 14</b>")+": "+ mapEstructuraClave.get(estructuraClave) />
						</#if>	
						<#if estructuraClave=="clasificacion15">
							<#assign clas015=estructuraClave?replace("clasificacion15","<b>Clasificación 15</b>")+": "+ mapEstructuraClave.get(estructuraClave) />
						</#if>			
		</#if>
</#list>


<table width=100%>
<tr><td colspan="2" >
<table width=100%>
			<td align="center"><font size=2><b>Información General</b></font></td>
			<tr><td>${agrupador}</</td></tr>
			<tr><td>${clavePresupuestal}</td></tr>		
</table width=100%></td>
			</tr>			    
				<tr>
					<td >
						<table width=100%>
						  <br>
			              <br>
							<th><font size=2>Clasificaciones</font></th>
							<tr><td>${clas01}${clas1}</</td></tr>
							<tr><td>${clas02}${clas2}</</td></tr>
							<tr><td>${clas03}${clas3}</</td></tr>
							<tr><td>${clas04}${clas4}</</td></tr>
							<tr><td>${clas05}${clas5}</</td></tr>
							<tr><td>${clas06}${clas6}</</td></tr>
							<tr><td>${clas07}${clas7}</</td></tr>
							<tr><td>${clas08}${clas8}</</td></tr>
							<tr><td>${clas09}${clas9}</</td></tr>
							<tr><td>${clas010}${clas10}</</td></tr>
							<tr><td>${clas011}${clas11}</</td></tr>
							<tr><td>${clas012}${clas12}</</td></tr>
							<tr><td>${clas013}${clas13}</</td></tr>
							<tr><td>${clas014}${clas14}</</td></tr>
							<tr><td>${clas015}${clas15}</</td></tr>
						</table>
					</td>
				</tr>	
				
				<tr>
					<td >
						<table width=100%>
						  <br>
							<th><font size=2>Detalle</font></th>
							<tr><td>${acctgTransId}</</td></tr>
							<tr><td>${acctgTransTypeId}</</td></tr>
							<tr><td>${transactionDate}</</td></tr>
							<tr><td>${postedDate}</</td></tr>
							<tr><td>${glFiscalTypeId}</</td></tr>
							<tr><td>${partyId}</</td></tr>
							<tr><td>${createdByUserLogin}</</td></tr>
							<tr><td>${idTipoDoc}</</td></tr>
							<#if acctgTransId?contains("P")> 
								<tr><td>${tipoPolizaIdPres}</</td></tr>
							<#else>
								<tr><td>${tipoPolizaIdCont}</</td></tr>
							</#if>	
						</table>
					</td>
				</tr>
				<#if theirPartyId?has_content || productId?has_content>
				<tr>
					<td >
						<table width=100%>
						  <br>
						  <th><font size=2>Catálogos Auxiliares</font></th>
						  <tr><td>${theirPartyId}</</td></tr>
						  <tr><td>${productId}</</td></tr>
						</table>
					</td>
				</tr>	
				</#if>	
</table>
