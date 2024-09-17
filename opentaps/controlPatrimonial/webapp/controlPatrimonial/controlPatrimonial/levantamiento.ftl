<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<script type="text/javascript">	
	function borrarIdEnfoca(){
		var fixedAssetId = document.getElementsByName("fixedAssetId");
		for (i = 0; i < fixedAssetId.length; i++) {
        	fixedAssetId[i].value = '';
        	fixedAssetId[i].focus();
        	break;
   		}
	}
</script>

<@form name="IniciaLevantamiento" url="IniciaLevantamiento" />
<#assign linkInicia ><@submitFormLinkConfirm form="IniciaLevantamiento" text=uiLabelMap.FixedAssetIniciarLevantamiento class="subMenuButton" /></#assign>

<@frameSection title=uiLabelMap.ActivoFijoLevantamiento extra=linkInicia >
	<@form name="AgregaActivo" url="AgregaActivo" >
		<table>
			<@inputSelectRow title=uiLabelMap.FormFieldTitle_ubicacionRapidaId name="ubicacionRapidaId" list=listUbicacion key="ubicacionRapidaId" displayField="descripcion" onChange="javascript:borrarIdEnfoca();" titleClass="requiredField"/>
			<@inputLookupRow name="fixedAssetId" title=uiLabelMap.AccountingFixedAsset lookup="LookupFixedAsset" form="AgregaActivo" default="" titleClass="requiredField"/>
			<@inputSubmitRow title=uiLabelMap.CommonAdd />
		</table>
	</@form>
	<@paginate name="listLevantamiento" list=levantamientoListBuilder rememberPage=false>
	    <#noparse>
	        <@navigationHeader/>
	        <table class="listTable">
	            <tr class="listTableHeader">
	                <@headerCell title=uiLabelMap.AccountingFixedAsset orderBy="activoFijo"/>
	                <@headerCell title=uiLabelMap.FixedAssetUbicaActual orderBy="ubicacionActual"/>
	                <@headerCell title=uiLabelMap.FixedAssetUbicaAnterior orderBy="ubicacionAnterior"/>
					<@headerCell title=uiLabelMap.ControlIdAnterior orderBy="idActivoAnterior"/>
	            </tr>
	            <#list pageRows as row>
	            <tr class="${tableRowClass(row_index)}">
	                <@displayLinkCell text=row.activoFijoId href="/assetmaint/control/ListFixedAssets?fixedAssetId=${row.activoFijoId}"/>
	                <@displayCell text=row.ubicacionActual/>
	                <@displayCell text=row.ubicacionAnterior/>
					<@displayCell text=row.idActivoAnterior?if_exists/>
	            </#list>
	        </table>
	    </#noparse>
	</@paginate>
</@frameSection>
<@frameSection title=uiLabelMap.FixedAsseLevantamientoNoEncontrado >
		<@paginate name="listLevantamientoNoExiste" list=levantamientoNoExisteListBuilder rememberPage=false>
	    <#noparse>
	        <@navigationHeader/>
	        <table class="listTable">
	            <tr class="listTableHeader">
	                <@headerCell title=uiLabelMap.AccountingFixedAsset orderBy="activoFijoId"/>
	                <@headerCell title=uiLabelMap.CommonName orderBy="activoFijo"/>
					<@headerCell title=uiLabelMap.FixedAssetUbicaActual orderBy="ubicacionActual"/>
					<@headerCell title=uiLabelMap.ControlIdAnterior orderBy="idActivoAnterior"/>
	            </tr>
	            <#list pageRows as row>
	            <tr class="${tableRowClass(row_index)}">
	                <@displayLinkCell text=row.activoFijoId href="/assetmaint/control/ListFixedAssets?fixedAssetId=${row.activoFijoId}"/>
	                <@displayCell text=row.activoFijo/>
					<@displayCell text=row.ubicacionActual/>
					<@displayCell text=row.idActivoAnterior?if_exists/>
	            </#list>
	        </table>
	    </#noparse>
	</@paginate>
</@frameSection>
<script type="text/javascript">
	opentaps.addOnLoad(borrarIdEnfoca());
</script>