<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<script language="JavaScript" type="text/javascript">

		function clickEliminaFactura(personaSolicitanteId, userLoginId){
			confirmar=confirm("\u00bfEst\u00e1 seguro?"); 
			if (confirmar){	
				if (personaSolicitanteId && userLoginId) {
					var requestData = {'personaSolicitanteId' : personaSolicitanteId, 'userLoginId' : userLoginId};		    			    
		    		opentaps.sendRequest('desactivaUsuario', requestData, function(data){responseDesactivaUsuario(data)});
		    	}	
		    }		
		}
		
		//Funcion para confirmar eliminacion de factura
		function responseDesactivaUsuario(data){
			for (var key in data){
				if(data[key]=="ERROR"){
					alert("Ocurri\u00f3 un error al desactivar al usuario");
				}
				else{
					alert("El usuario ha sido desactivado");
					location.reload();
					
				}	    		
			}		
		}
</script>

<h4>Fecha de consulta:
				${Static["org.ofbiz.base.util.UtilDateTime"].nowSqlDate()}</h4>

<@paginate name="listUsuariosInh" list=usuarioInhListBuilder rememberPage=false>
    <#noparse>
        <@navigationHeader/>
        <table class="listTable">
            <tr class="listTableHeader">
            	<@headerCell title=uiLabelMap.PurchSolicitante orderBy="usuario"/>
                <@headerCell title=uiLabelMap.ViaticoId orderBy="createdStamp"/>
                <@headerCell title=uiLabelMap.ViaticoDescription orderBy="geoOrigenId"/>
                <@headerCell title=uiLabelMap.FechaFinal orderBy="fechaFinal"/>
                <@headerCell title=uiLabelMap.CommonStatus orderBy="statusId"/>
                
            </tr>
            <#list pageRows as row>
            <tr class="${tableRowClass(row_index)}">
            	<@displayCell text=row.usuario/> 
                <@displayLinkCell text=row.viaticoId href="verViatico?viaticoId=${row.viaticoId}"/>
                <@displayCell text=row.geoOrigenId/>
                <@displayCell text=row.fechaFinal/>
                <@displayCell text=row.statusId/> 
                <td align="center" width="7%">
					<a name="submitButton" id="elimina${row.personaSolicitanteId}"  class="subMenuButtonDangerous" onClick="clickEliminaFactura('${row.personaSolicitanteId}','${row.userLoginId}')">${uiLabelMap.DeshabilitarUsuario}</a>
				</td>                                            
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>