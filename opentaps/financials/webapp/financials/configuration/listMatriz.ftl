<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<@paginate name="listMatriz" list=VistaMatrizListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>                
	        <table class="listTable">
	            <tr class="listTableHeader">
	                <@headerCell title=uiLabelMap.MatrizId orderBy="matrizId"/>
	                <@headerCell title=uiLabelMap.COG orderBy="cog"/>
	                <@headerCell title=uiLabelMap.CRI orderBy="cri"/>
	                <@headerCell title=uiLabelMap.Descripcion orderBy="description"/>
	                <@headerCell title=uiLabelMap.TipoGasto orderBy="tipoGasto"/>
	                <@headerCell title=uiLabelMap.Caracteristicas orderBy="caracteristicas"/>
	                <@headerCell title=uiLabelMap.MedioPago orderBy="medioPago"/>
	                <@headerCell title=uiLabelMap.Cargo orderBy="cargo"/>
	                <@headerCell title=uiLabelMap.CuentaCargo orderBy="cuentaCargo"/>
	                <@headerCell title=uiLabelMap.Abono orderBy="cargo"/>
	                <@headerCell title=uiLabelMap.CuentaAbono orderBy="cuentaAbono"/>
	            </tr>
	            <#list pageRows as row>
	            <tr class="${tableRowClass(row_index)}">
	                <@displayCell text=row.matrizId/>
	                <@displayCell text=row.cog?if_exists/>
	                <@displayCell text=row.cri?if_exists/>
	                <@displayCell text=row.description/>
	                <@displayCell text=row.tipoGasto?if_exists/>
	                <@displayCell text=row.caracteristicas/>
	                <@displayCell text=row.medioPago/>
	                <@displayCell text=row.cargo/>
	                <@displayCell text=row.cuentaCargo/>
	                <@displayCell text=row.abono/>
	                <@displayCell text=row.cuentaAbono/>
	            </tr>
	            </#list>
	        </table>	    
    </#noparse>
</@paginate>
