<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<@frameSection title=uiLabelMap.Lista extra=extraOptions >
	<form method="post" action="<@ofbizUrl>actualizarMatriz</@ofbizUrl>" name="actualizarMatriz" style="margin: 0;" id="actualizarMatriz">

	<#if detalleMatriz?has_content>
		<table border="0" width="100%">			
			<thead>		
				<tr>
					<th>${uiLabelMap.MatrizId}</th>
					<th>${uiLabelMap.COG}</th>
					<th>${uiLabelMap.CRI}</th>
					<th>${uiLabelMap.Descripcion}</th>
					<th>${uiLabelMap.TipoGasto}</th>
					<th>${uiLabelMap.MedioPago}</th>
					<th>${uiLabelMap.Caracteristicas}</th>
					<th>${uiLabelMap.Cargo}</th>
					<th>${uiLabelMap.CuentaCargo}</th>
					<th>${uiLabelMap.CuentaAbono}</th>			
					<th></th>
					<th></th>			
				</tr>
			</thead>
			<tbody>
				<input type="hidden" name="tipoMatriz" value="${tipoMatriz?if_exists}"/>
				<input type="hidden" name="matrizId" value="${matrizId?if_exists}"/>
				<input type="hidden" name="nombreCog" value="${nombreCog?if_exists}"/>
				<input type="hidden" name="nombreCri" value="${nombreCri?if_exists}"/>
				<input type="hidden" name="cog	" value="${cog?if_exists}"/>
				<input type="hidden" name="cri" value="${cri?if_exists}"/>
				<input type="hidden" name="tipoGasto" value="${tipoGasto?if_exists}"/>
				<input type="hidden" name="cargo" value="${cargo?if_exists}"/>
				<input type="hidden" name="abono" value="${abono?if_exists}"/>
				<#list detalleMatriz as detalle>					
					<tr class="${tableRowClass(detalle_index)}">
						<input type="hidden" name="matrizIdOriginal_o_${detalle_index}" value="${detalle.matrizId?if_exists}"/>
						<input type="hidden" name="cogOriginal_o_${detalle_index}" value="${detalle.cog?if_exists}"/>
						<input type="hidden" name="criOriginal_o_${detalle_index}" value="${detalle.cri?if_exists}"/>
						<input type="hidden" name="tipoGastoOriginal_o_${detalle_index}" value="${detalle.tipoGasto?if_exists}"/>
						<input type="hidden" name="matrizEgresoId_o_${detalle_index}" value="${detalle.matrizEgresoId?if_exists}"/>
						<input type="hidden" name="matrizIngresoId_o_${detalle_index}" value="${detalle.matrizIngresoId?if_exists}"/>
						<td><input type="text" class='inputBox' size="9" name="matrizId_o_${detalle_index}" id="matrizId_o_${detalle_index}" value="${detalle.matrizId?if_exists}"/></td>
						<td><input type="text" class='inputBox' size="9" name="cog_o_${detalle_index}" id="cog_o_${detalle_index}" value="${detalle.cog?if_exists}"/></td>
						<td><input type="text" class='inputBox' size="9" name="cri_o_${detalle_index}" id="cri_o_${detalle_index}" value="${detalle.cri?if_exists}"/></td>						
						<@displayCell text=detalle.description />
						<td><input type="text" class='inputBox' size="9" name="tipoGasto_o_${detalle_index}" id="tipoGasto_o_${detalle_index}" value="${detalle.tipoGasto?if_exists}"/></td>
						<td><input type="text" class='inputBox' size="9" name="caracteristicas_o_${detalle_index}" id="caracteristicas_o_${detalle_index}" value="${detalle.caracteristicas?if_exists}"/></td>
						<td><input type="text" class='inputBox' size="9" name="medioPago_o_${detalle_index}" id="medioPago_o_${detalle_index}" value="${detalle.medioPago?if_exists}"/></td>
						<@inputAutoCompleteGlAccountCell name="cargo_o_${detalle_index}" url="getAutoCompleteGlAccountsRegistro" default="${detalle.cargo?if_exists}"/>
						<@displayCell text=detalle.cuentaCargo/>
						<@inputAutoCompleteGlAccountCell name="abono_o_${detalle_index}" url="getAutoCompleteGlAccountsRegistro" default="${detalle.abono?if_exists}"/>
						<@displayCell text=detalle.cuentaAbono/>	
					</tr>																																						
				</#list>
				<@submitFormLink form="actualizarMatriz" text=uiLabelMap.CommonUpdate />
			</tbody>
		</table>
	</#if>	
	</form>
</@frameSection>
