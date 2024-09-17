<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#if !datos?exists>
<form method="POST" name="ACompensadaI" action="${ACompensadaI}"> <#-- action set by the screen -->
  <@inputHidden name="organizationPartyId" value="${organizationPartyId}"/>
  <@inputHidden name="performFind" value="Y"/>
  <div class="form" style="border:0">
  <table class="twoColumnForm">
    <tbody>
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialsAccountigDate titleClass="requiredField" />
        <@inputDateTimeCell name="fechaContable" default=Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp() />
      </tr> 
      <tr>
      	<@inputTextRow title=uiLabelMap.Descripcion name="description" size=60 default=description?if_exists titleClass="requiredField"/>
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialTipoAfectacion titleClass="requiredField"/>
        <@inputSelectHashCell hash= {"INTERNA":"Interna", "EXTERNA":"Externa"} name="tipoMovimiento"/>
      </tr> 
       <@inputSubmitRow title=uiLabelMap.CommonCreate />  
    </tbody>
  </table>
</form>
</#if>

<#if datos?exists>
<form method="POST" name="ACompensadaI2" action="${ACompensadaI}"> <#-- action set by the screen -->
  <@inputHidden name="performFind" value="B"/>
  <@inputHidden name="tipoClave" value="INGRESO"/>
  <@inputHidden name="agrupadorP" value="${datos.agrupadorP}"/>
  <table width=100%>
	<td align="center"><font size=2><b>Información</b></font></td>	
  </table width=100%></td>
  <table class="twoColumnForm">
    <tbody>
      <tr>
      	<@displayCell text=uiLabelMap.FinancialsFechaContable/>
        <@displayCell text=uiLabelMap.Descripcion/>
        <@displayCell text=uiLabelMap.FinancialsTransactionDate/>
        <@displayCell text=""/>
      </tr>
        <tr>
        	<@displayCell text=datos.fechaContable/>
            <@displayCell text=datos.description/>
            <@displayCell text=datos.fechaTransaccion/>
            <#if acctgTransId?has_content>
  				<@displayLinkCell text=uiLabelMap.VerPoliza href="viewAcctgTrans?acctgTransId=${acctgTransId}"/>
  			</#if>
  		</tr> 
    </tbody>
  </table>
  <#if !acctgTransId?has_content>  
  <table width=100%>
	<td align="center"><font size=2><b><@displayCell text=uiLabelMap.Monto/></b></font></td>	
  </table width=100%></td>
  <table class="twoColumnForm">
    <tbody>
	   <tr>
          <@displayCell text=uiLabelMap.CommonAmount blockClass="titleCell" blockStyle="width: 100px" class="requiredField" />
          <@inputCurrencyCell name="monto" currencyName="currency" defaultCurrencyUomId="MXN"/>
       </tr>   
    </tbody>
  </table>
  <table width=100%>
	<hr>
	<td align="center"><font size=2><b><@displayCell text=uiLabelMap.Reduccion/></b></font></td>
  </table width=100%>
  <table class="twoColumnForm">
     <tbody>
        <tr>
         <@displayTitleCell title=uiLabelMap.FinancialPeriodo />
         <#assign periodos = ["01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"]/>
         <td>
         	<select name="perdiodoIR" class="inputBox">
         	<option value=""></option>
         		<#list periodos as periodo>
            		<option value="${periodo}">${periodo}</option>
        		</#list>
        	</select>
         </td>
        </tr>
        <@displayAfectPresupuestariaRow tagTypes=tagTypesIngreso titleClass="requiredField"/>
     </tbody>
  </table>
  <table width=100%>
	<hr>
	<td align="center"><font size=2><b><@displayCell text=uiLabelMap.Ampliacion/></b></font></td>
  </table width=100%>
  <table class="twoColumnForm">
     <tbody>
        <tr>
         <@displayTitleCell title=uiLabelMap.FinancialPeriodo />
         <#assign periodos = ["01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"]/>
         <td>
         	<select name="perdiodoIA" class="inputBox">
         	<option value=""></option>
         		<#list periodos as periodo>
            		<option value="${periodo}">${periodo}</option>
        		</#list>
        	</select>
         </td>
        </tr>
        <tr>
     		<@displayCvesPresupRow tagTypes=tagTypesIngreso titleClass="requiredField"/>
       </tr>
       <@inputSubmitRow title=uiLabelMap.CommonAnadir />  
     </tbody>
  </table>
  </#if>
</form>
</#if>

<#if listDetalle?has_content>
<form method="POST" name="ACompensadaI3" action="${ACompensadaI}"> <#-- action set by the screen -->
  <@inputHidden name="agrupadorP" value="${datos.agrupadorP}"/>
  <@inputHidden name="performFind" value="C"/>
  <@inputHidden name="tipoClave" value="INGRESO"/>
  <div class="screenlet-header">
        <div style="float: right;"><#t/>
          <@inputSubmitRow title=uiLabelMap.CommonCreate />
        </div><#t/>
  </div>
  <table class="twoColumnForm">
    <tbody>
      <tr>
      	<@displayCell text=uiLabelMap.FinancialsClavePresup/>
        <@displayCell text=uiLabelMap.Monto/>
        <@displayCell text=uiLabelMap.Date/>
        <@displayCell text=uiLabelMap.AmpliacionReduccion/>
      </tr>
      <#list listDetalle as row>
        <tr class="${tableRowClass(row_index)}">
        	<@displayCell text=row.clavePresupuestal/>
            <@displayCell text=row.monto/>
            <@displayCell text=row.fecha/>
            <@displayCell text=row.flag/>
        </tr>
      </#list>   
    </tbody>
  </table>
</form>
</#if>
