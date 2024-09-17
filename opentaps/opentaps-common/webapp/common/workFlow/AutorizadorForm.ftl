<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl" />

<#assign urlHost = request.getServerName()+":"+request.getServerPort()/>
<@form name="accionWorkFlow" url="" id="accionWorkFlow" accion = "" estatus = "" 
entidadId = entidadId tipoWorkFlowId = tipoWorkFlowId urlHost = urlHost entidad = entidad avisoTesorero=avisoTesorero!'N' />