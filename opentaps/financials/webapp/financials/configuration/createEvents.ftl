<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<form method="POST" name="createEvents" action="${creaOEventos}"> <#-- action set by the screen -->
  <input type="hidden" name="organizationPartyId" value="${organizationPartyId}"/>
  <div class="form" style="border:0">
    <table class="fourColumnForm" style="border:0">
      <th>
	  	<@displayTooltip text="Información general" />
	  </th>
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialsDocumentoId titleClass="requiredField"/>
        <@inputTextCell name="idTipoDoc" maxlength=20  />
        <@displayTitleCell title=uiLabelMap.FinancialsDescripcionDoc titleClass="requiredField"/>
        <@inputTextCell name="descripcionDoc" maxlength=60  />
      </tr>
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialModulo titleClass="requiredField" />
        <td>	   	    
     		<select name="moduloId">
		    	<option value=""></option>
		        <#list listModulos as modulo>
		        	<option  value="${modulo.moduloId}">${modulo.get("nombre",locale)}</option>
		        </#list>
		    </select>	 
	    </td>
      </tr>
      <th>
        <hr>
	  	<@displayTooltip text="Cuentas presupuestales" />
	  </th>
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialsCuantaCargoP />
        <@inputAutoCompleteGlAccountCell name="cuentaCargo" default=cuentaCargo?if_exists />
        <@displayTitleCell title=uiLabelMap.FinancialsCuantaCargoC  />
        <@inputAutoCompleteGlAccountCell name="cuentaAbono" default=cuentaAbono?if_exists />
      </tr>
      <th>
        <hr>
	  	<@displayTooltip text="Cuentas patrimoniales" />
	  </th>
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialsCuantaCargoP />
        <@inputAutoCompleteGlAccountCell name="cuentaCargoPatri" default=cuentaCargo?if_exists />
        <@displayTitleCell title=uiLabelMap.FinancialsCuantaCargoC />
        <@inputAutoCompleteGlAccountCell name="cuentaAbonoPatri" default=cuentaAbono?if_exists />
      </tr>
      <th>
        <hr>
	  	<@displayTooltip text="Tipo de póliza" />
	  </th>
      <tr>
        <@displayTitleCell title=uiLabelMap.FinancialTipoPolizaP />
        <td>	   	    
     		<select name="tipoPolizaIdP" >
		    	<option value=""></option>
		        <#list listTipoPolizaP as poliza>
		        	<option  value="${poliza.tipoPolizaId}">${poliza.get("description",locale)}</option>
		        </#list>
		    </select>	 
	    </td>
	    <@displayTitleCell title=uiLabelMap.FinancialTipoPolizaC />
 		<td>	   	    
     		<select name="tipoPolizaIdC" >
		    	<option value=""></option>
		        <#list listTipoPolizaC as poliza>
		        	<option  value="${poliza.tipoPolizaId}">${poliza.get("description",locale)}</option>
		        </#list>
		    </select>	 
	    </td>      
      </tr>	 
      <th>
        <hr>
	  	<@displayTooltip text="Referencia de documento" />
	  </th>       
      <tr>
         <@displayTitleCell title=uiLabelMap.FinancialReferencia titleClass="requiredField"  />
         <#assign referencias = ["Matriz", "Patrimonio", "Sin Referencia", "Traspasos"]/>
         <td>
         	<select name="referenciaId" class="inputBox">
         	<option value=""></option>
         		<#list referencias as referencia>
            		<option value="${referencia}">${referencia}</option>
        		</#list>
        	</select>
         </td>
         <@displayTitleCell title=uiLabelMap.FinancialTipoMatriz />
         <#assign tipoMatrices = ["A.1 Devengada de egresos", "A.2 Pagada de Egresos", "B.1 Devengada de ingresos", "B.2 Recaudada de ingresos"]/>
         <td>
         	<select name="tipoMatrizId" class="inputBox">
         	<option value=""></option>
         		<#list tipoMatrices as tipoMatriz>
            		<option value="${tipoMatriz}">${tipoMatriz}</option>
        		</#list>
        	</select>
         </td>
      </tr>     
      <th>
        <hr>
	  	<@displayTooltip text="Catálogos contables" />
	  </th> 
      <tr>
         <@displayTitleCell title=uiLabelMap.FinancialTipoAuxiliar titleClass="requiredField"  />
         <#assign auxiliares = ["Auxiliar", "Producto", "No es necesario"]/>
         <td>
         	<select name="auxiliarCargoId" class="inputBox">
         	<option value=""></option>
         		<#list auxiliares as auxiliar>
            		<option value="${auxiliar}">${auxiliar}</option>
        		</#list>
        	</select>
         </td>
         <@displayTitleCell title=uiLabelMap.FinancialProductiId titleClass="requiredField" />
         <#assign productos = ["Auxiliar", "Producto", "No es necesario"]/>
         <td>
         	<select name="auxiliarAbonoId" class="inputBox">
         	<option value=""></option>
         		<#list productos as producto>
            		<option value="${producto}">${producto}</option>
        		</#list>
        	</select>
         </td>
      </tr>
      <th>
        <hr>
	  	<@displayTooltip text="Validación de suficiencia" />
	  </th>  
      <tr>
         <@displayTitleCell title=uiLabelMap.FinancialComparacion />
         <#assign comparaciones = ["menor que", "menor o igual que", "mayor o igual que", "mayor que", "liberar presupuesto"]/>
         <td>
         	<select name="comparacionId" class="inputBox">
         	<option value=""></option>
         		<#list comparaciones as comparacion>
            		<option value="${comparacion}">${comparacion}</option>
        		</#list>
        	</select>
         </td>
      </tr> 
      <th>
        <hr>
	  	<@displayTooltip text="Evento predecesor" />
	  </th>  
      <tr>
         <@displayTitleCell title=uiLabelMap.FinancialPredecesor />
         <td>
         	<select name="predecesorId" class="inputBox">
         	<option value=""></option>
         		<#list listTipoDocumentos as predecesor>
            		<option  value="${predecesor.idTipoDoc}">${predecesor.get("descripcion",locale)}</option>
        		</#list>
        	</select>
         </td>
      </tr>                                                                                 
      <@inputSubmitRow title=uiLabelMap.CommonCreate />
    </table>
  </div>
</form>
