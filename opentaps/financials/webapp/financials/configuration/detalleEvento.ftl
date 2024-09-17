
<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#assign idTipoDoc=""/>
<#assign descripcion=""/>
<#assign mComparativo=""/>
<#assign mEjecutar1=""/>
<#assign mEjecutar2=""/>
<#assign comparacion=""/>
<#assign userLogin=""/>
<#assign predecesor=""/>
<#assign moduloId=""/>

<#assign cuentaCargo=""/>
<#assign cuentaAbono=""/>
<#assign glFiscalTypeIdPres=""/>
<#assign cuentaCargoPatri=""/>
<#assign cuentaAbonoPatri=""/>
<#assign tipoPolizaIdPres=""/>
<#assign glFiscalTypeIdCont=""/>
<#assign tipoPolizaIdCont=""/>
<#assign referencia=""/>
<#assign tipoMatriz=""/>

<#assign catalogoCargo=""/>
<#assign catalogoAbono=""/>


<#list mapTipoDocumento?keys as docId>
		<#if mapTipoDocumento.get(docId)??>
						<#if docId=="idTipoDoc">
							<#assign idTipoDoc=docId?replace("idTipoDoc","<b>Identificador del evento</b>")+": "+ mapTipoDocumento.get(docId) />
						</#if>				
						<#if docId=="descripcion">
							<#assign descripcion=docId?replace("descripcion","<b>Descripción</b>")+": "+ mapTipoDocumento.get(docId) />
						</#if>						
						<#if docId=="comparacion">
							<#if mapTipoDocumento.get(docId).equals(">")>
								<#assign comparacion=docId?replace("comparacion","<b>Validación de la suficiencia presupuestal</b>")+": "+ "menor que" />
							<#elseif mapTipoDocumento.get(docId).equals(">=")>
								<#assign comparacion=docId?replace("comparacion","<b>Validación de la suficiencia presupuestal</b>")+": "+ "menor o igual que" />
							<#elseif mapTipoDocumento.get(docId).equals("<=")>
								<#assign comparacion=docId?replace("comparacion","<b>Validación de la suficiencia presupuestal</b>")+": "+ "mayor o igual que" />
							<#elseif mapTipoDocumento.get(docId).equals("<")>
								<#assign comparacion=docId?replace("comparacion","<b>Validación de la suficiencia presupuestal</b>")+": "+ "mayor que" />
							</#if>	
						</#if>	
						<#if docId=="userLogin">
							<#assign userLogin=docId?replace("userLogin","<b>Creado por el usuario</b>")+": "+ mapTipoDocumento.get(docId) />
						</#if>	
		</#if>
</#list>

<#list mapEvento?keys as docId>
		<#if mapEvento.get(docId)??>
						<#if docId=="cuentaCargo">
							<#assign cuentaCargo=docId?replace("cuentaCargo","<b>Cuenta cargo presupuestal</b>")+": "+ mapEvento.get(docId) />
						</#if>	
						<#if docId=="cuentaAbono">
							<#assign cuentaAbono=docId?replace("cuentaAbono","<b>Cuenta abono presupuestal</b>")+": "+ mapEvento.get(docId) />
						</#if>
						<#if docId=="cuentaCargoPatri">
							<#assign cuentaCargoPatri=docId?replace("cuentaCargoPatri","<b>Cuenta cargo patrimonial</b>")+": "+ mapEvento.get(docId) />
						</#if>	
						<#if docId=="cuentaAbonoPatri">
							<#assign cuentaAbonoPatri=docId?replace("cuentaAbonoPatri","<b>Cuenta abono patrimonial</b>")+": "+ mapEvento.get(docId) />
						</#if>
						<#if docId=="referencia">
							<#assign referencia=docId?replace("referencia","<b>Referencia</b>")+": "+ mapEvento.get(docId) />
						</#if>
						<#if docId=="tipoMatriz">
							<#assign tipoMatriz=docId?replace("tipoMatriz","<b>Tipo de matriz</b>")+": "+ mapEvento.get(docId) />
						</#if>						
		</#if>
</#list>

<#list mapTipoTrans?keys as docId>
		<#if mapTipoTrans.get(docId)??>
						<#if docId=="catalogoCargo">
							<#assign catalogoCargo=docId?replace("catalogoCargo","<b>Solicitará auxiliar de cargo</b>")+": "+ mapTipoTrans.get(docId) />
						</#if>	
						<#if docId=="catalogoAbono">
							<#assign catalogoAbono=docId?replace("catalogoAbono","<b>Solicitará auxiliar de abono</b>")+": "+ mapTipoTrans.get(docId) />
						</#if>								
		</#if>
</#list>

<#if mapTipoPolizaPres?has_content>
<#list mapTipoPolizaPres?keys as docId>
		<#if mapTipoPolizaPres.get(docId)??>
						<#if docId=="description">
							<#assign tipoPolizaIdPres=docId?replace("description","<b>Tipo de póliza presupuestal</b>")+": "+ mapTipoPolizaPres.get(docId) />
						</#if>								
		</#if>
</#list>
</#if>

<#if mapTipoPolizaCont?has_content>
<#list mapTipoPolizaCont?keys as docId>
		<#if mapTipoPolizaCont.get(docId)??>
						<#if docId=="description">
							<#assign tipoPolizaIdCont=docId?replace("description","<b>Tipo de póliza contable</b>")+": "+ mapTipoPolizaCont.get(docId) />
						</#if>								
		</#if>
</#list>
</#if>

<#list mapModulo?keys as docId>
		<#if mapModulo.get(docId)??>
						<#if docId=="nombre">
							<#assign moduloId=docId?replace("nombre","<b>Módulo</b>")+": "+ mapModulo.get(docId) />
						</#if>								
		</#if>
</#list>

<#if mapFiscalTypePres?has_content>
<#list mapFiscalTypePres?keys as docId>
		<#if mapFiscalTypePres.get(docId)??>
						<#if docId=="description">
							<#assign glFiscalTypeIdPres=docId?replace("description","<b>Tipo fiscal presupuestal</b>")+": "+ mapFiscalTypePres.get(docId) />
						</#if>								
		</#if>
</#list>
</#if>

<#if mapFiscalTypeCont?has_content>
<#list mapFiscalTypeCont?keys as docId>
		<#if mapFiscalTypeCont.get(docId)??>
						<#if docId=="description">
							<#assign glFiscalTypeIdCont=docId?replace("description","<b>Tipo fiscal contable</b>")+": "+ mapFiscalTypeCont.get(docId) />
						</#if>							
		</#if>
</#list>
</#if>

<#if mapPredecesor?has_content>
<#list mapPredecesor?keys as docId>
		<#if mapPredecesor.get(docId)??>
						<#if docId=="descripcion">
							<#assign predecesor=docId?replace("descripcion","<b>Evento predecesor</b>")+": "+ mapPredecesor.get(docId) />
						</#if>						
		</#if>
</#list>
</#if>

<#if mapComparativo?has_content>
<#list mapComparativo?keys as docId>
		<#if mapComparativo.get(docId)??>
						<#if docId=="description">
							<#assign mComparativo=docId?replace("description","<b>Momento para comparar suficiencia presupuestal</b>")+": "+ mapComparativo.get(docId) />
						</#if>					
		</#if>
</#list>
</#if>

<#if mapEjecutar1?has_content>
<#list mapEjecutar1?keys as docId>
		<#if mapEjecutar1.get(docId)??>
						<#if docId=="description">
							<#assign mEjecutar1=docId?replace("description","<b>Momento que se afectará la suficiencia presupuestal</b>")+": "+ mapEjecutar1.get(docId) />
						</#if>					
		</#if>
</#list>
</#if>

<#if mapEjecutar2?has_content>
<#list mapEjecutar2?keys as docId>
		<#if mapEjecutar2.get(docId)??>
						<#if docId=="description">
							<#assign mEjecutar2=docId?replace("description","<b>Momento 2 que se afectará la suficiencia presupuestal</b>")+": "+ mapEjecutar2.get(docId) />
						</#if>					
		</#if>
</#list>
</#if>

<table width=100%>
<tr><td colspan="2" >
<table width=100%>
			<td align="center"><font size=2><b>Información General</b></font></td>
			<tr><td>${idTipoDoc}</</td></tr>
			<tr><td>${descripcion}</td></tr>
			<tr><td>${userLogin}</td></tr>	
			<tr><td>${predecesor}</td></tr>	
			<tr><td>${moduloId}</td></tr>		
</table width=100%></td>
			</tr>
			
			<#if mEjecutar1?has_content>			    
				<tr>
					<td >
						<table width=100%>
						  <br>
			              <br>
							<th><font size=2>Suficiencia Presupuestaria</font></th>
							<tr><td>${mComparativo}</</td></tr>
							<tr><td>${mEjecutar1}</</td></tr>
							<tr><td>${mEjecutar2}</</td></tr>
							<tr><td>${comparacion}</</td></tr>
						</table>
					</td>
				</tr>	
			</#if>
			<#if catalogoCargo?has_content>    
				<tr>
					<td >
						<table width=100%>
						  <br>
			              <br>
							<th><font size=2>Catálogos Contables</font></th>
							<tr><td>${catalogoCargo}</</td></tr>
							<tr><td>${catalogoAbono}</</td></tr>
						</table>
					</td>
				</tr>
			</#if>	
					    
				<tr>
					<td >
						<table width=100%>
						  <br>
			              <br>
							<th><font size=2>Referencia del evento</font></th>
							<tr><td>${referencia}</</td></tr>
							<tr><td>${tipoMatriz}</</td></tr>
						</table>
					</td>
				</tr>	
			
				<#if tipoPolizaIdPres?has_content>
				<tr>
					<td >
						<table width=100%>
						  <br>
						  <th><font size=2>Afectación Presupuestal</font></th>
						  <tr><td>${cuentaCargo}</</td></tr>
						  <tr><td>${cuentaAbono}</</td></tr>
						  <tr><td>${glFiscalTypeIdPres}</</td></tr>
						  <tr><td>${tipoPolizaIdPres}</</td></tr>
						</table>
					</td>
				</tr>	
				</#if>	
				
				<#if glFiscalTypeIdCont?has_content>
				<tr>
					<td >
						<table width=100%>
						  <br>
						  <th><font size=2>Afectación Contable</font></th>
						  <tr><td>${glFiscalTypeIdCont}</</td></tr>
						  <tr><td>${tipoPolizaIdCont}</</td></tr>
						</table>
					</td>
				</tr>	
				</#if>	
				
				<#if cuentaCargoPatri?has_content>
				<tr>
					<td >
						<table width=100%>
						  <br>
						  <th><font size=2>Cuentas Patrimoniales</font></th>
						   <tr><td>${cuentaCargoPatri}</</td></tr>
						  <tr><td>${cuentaAbonoPatri}</</td></tr>
						</table>
					</td>
				</tr>	
				</#if>
</table>
