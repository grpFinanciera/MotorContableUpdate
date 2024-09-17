<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<script type="text/javascript">

<@paginate name="ExcelSuficiencia" list=clavesPresList >
    <#noparse>
        <@navigationHeader/>
    </#noparse>
</@paginate>

<#if clavesPresList?has_content>
	<form id="listaClaves" name="listaClaves" method="post" action="">
		<table width="100%">	
			<tbody>
            	<#assign numClave = 0 >
            	<#list clavesPresList as row>
            	<#assign numClave = numClave + 1 >
            		<tr class="${tableRowClass(numClave)}" name="claves" >
	            		<@displayCell text=row.clasificacion1?if_exists />
            			<@displayCell text=row.clasificacion2?if_exists />
            			<@displayCell text=row.clasificacion3?if_exists />
            			<@displayCell text=row.clasificacion4?if_exists />
            			<@displayCell text=row.clasificacion5?if_exists />
            			<@displayCell text=row.clasificacion6?if_exists />
            			<@displayCell text=row.clasificacion7?if_exists />
            			<@displayCell text=row.clasificacion8?if_exists />
            			<@displayCell text=row.clasificacion9?if_exists />
            			<@displayCell text=row.clasificacion10?if_exists />
            			<@displayCell text=row.clasificacion11?if_exists />
            			<@displayCell text=row.clasificacion12?if_exists />
            			<@displayCell text=row.clasificacion13?if_exists />
            			<@displayCell text=row.clasificacion14?if_exists />
            			<@displayCell text=row.clasificacion15?if_exists />
            			<#assign status="Activa">
            			<#assign etiqueta=uiLabelMap.FinancialsDeactivate>
            			<#if row.inactivo?exists>
            				<#assign status="Inactiva">
            				<#assign etiqueta=uiLabelMap.FinancialActivate>
            			</#if>
            			<@displayCell text=status />
            			<td><a class="buttontext" href="<@ofbizUrl>desactivarClavePres?clavePresupuestal=${row.clavePresupuestal}&tipoClave=${ingresoEgreso}</@ofbizUrl>">${etiqueta}</a></td>
            		</tr>
				</#list>
			</tbody>
		</table>
	</form>
</#if>