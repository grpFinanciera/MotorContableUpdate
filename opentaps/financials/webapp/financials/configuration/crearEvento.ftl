<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>


<script type="text/javascript">
	var textoEliminar = "${uiLabelMap.CommonDelete}";
	var numeroPresup = 1;
	var numeroPatrim = 1;
	function agregaLinea(tipoLinea){
		var contador = 0;
		if(tipoLinea == "lineaPres"){contador = numeroPresup} else {contador = numeroPatrim}
		var contadorNuevo = contador+1;
		var table = document.getElementById(tipoLinea);
		renglonSepara(table,contadorNuevo,tipoLinea);
		var suma = 1;
		var linea;
		var existeLinea = true;
		do{
			var linea = document.getElementById(tipoLinea+suma);
			if(linea){
				agregaRenglon(linea,table,contadorNuevo);
				suma = suma+1;
			}
		} while(linea);
		renglonEliminarPresup(table,contadorNuevo,tipoLinea,suma);
		
		if(tipoLinea == "lineaPres"){numeroPresup = contadorNuevo} else {numeroPatrim = contadorNuevo}
	}
	
	function agregaRenglon(linea,table,contadorNuevo){
		var nuevoRenglon = document.createElement("tr");
		nuevoRenglon.id = linea.id+contadorNuevo;
		for(var i=0; i<linea.childElementCount;i++){
			var nuevaCelda = linea.cells[i].cloneNode(true);
			var idPrincipal1;
			var idPrincipal2;
			var idTodos;
			var objetoAutocompletar;
			if(nuevaCelda.children.length>1){
				for(var j=0; j<nuevaCelda.children.length;j++){
					var elementoN = nuevaCelda.children[j];
					switch(elementoN.nodeName){
						case "DIV":
							var jsid = elementoN.getAttribute("jsid");
							var identificador = jsid.split("1");
							var idTodos = identificador[0]+contadorNuevo;
							elementoN.setAttribute("jsid",jsid.replace("1",(contadorNuevo)));
							break;
						case "TABLE":
							var widgetid = elementoN.getAttribute("widgetid");
							idPrincipal1 = widgetid;
							objetoAutocompletar = opentaps.initInputAutoCompleteQuick("getAutoCompleteGlAccountsRegistro",idTodos,idTodos,"","");
							var combo = objetoAutocompletar.domNode;
							nuevaCelda.replaceChild(combo,elementoN);
							break;
						case "INPUT":
							var id = elementoN.getAttribute("id");
							idPrincipal2 = id; 
							elementoN.id = id.replace("1",(contadorNuevo));
							elementoN.name = id.replace("1",(contadorNuevo));
							break;
						case "A":
							var href = elementoN.getAttribute("href");
							href = href.replace(idPrincipal1,idPrincipal1.replace("1",(contadorNuevo)));
							href = href.replace(idPrincipal2,idPrincipal2.replace("1",(contadorNuevo)));
							elementoN.setAttribute("href",href);
							break;
						case "SPAN":
							var id = elementoN.id;
							elementoN.setAttribute("id",id.replace("1",(contadorNuevo)));
							elementoN.innerHTML = "";
							break;
						case "SELECT":
							var id = elementoN.id;
							elementoN.setAttribute("id",id.replace("1",(contadorNuevo)));
							elementoN.setAttribute("name",id.replace("1",(contadorNuevo)));
							break;
					}
				}
			} else {
				var elemento = nuevaCelda.firstElementChild;
				var id = elemento.id;
				if(elemento.nodeName != 'SPAN'){
					elemento.id = id.replace("1",(contadorNuevo));
					elemento.name = elemento.id; 
				}
			}
			nuevoRenglon.appendChild(nuevaCelda);
		}
		table.appendChild(nuevoRenglon);
	}
	
	function renglonSepara(table,contadorNuevo,tipoLinea){
		var renglonSeparador = document.createElement("tr");
		renglonSeparador.id = tipoLinea+"0"+contadorNuevo;
		var nuevaCeldaSep = document.createElement("td");
		nuevaCeldaSep.innerHTML = "<hr>";
		nuevaCeldaSep.colSpan = "2";
		renglonSeparador.appendChild(nuevaCeldaSep);
		table.appendChild(renglonSeparador);
	}
	
	function renglonEliminarPresup(table,contadorNuevo,tipoLinea,suma){
		var renglonEliminar = document.createElement("tr");
		renglonEliminar.id = tipoLinea+suma+contadorNuevo;
		var nuevaCeldaEliminar = document.createElement("td");
		nuevaCeldaEliminar.colSpan = "4";
		nuevaCeldaEliminar.style.textAlign="right";
		var nuevoInput = document.createElement("input");
		nuevoInput.name = tipoLinea+contadorNuevo;
		nuevoInput.type = "button";
		nuevoInput.value = textoEliminar;
		nuevoInput.className = "buttonDangerous";
		nuevoInput.addEventListener("click", eliminar);
		nuevaCeldaEliminar.appendChild(nuevoInput);
		renglonEliminar.appendChild(nuevaCeldaEliminar);
		table.appendChild(renglonEliminar);
	}	
	
	function eliminar(){
		var tipoLinea = this.name.split(/(\d+)/);
		var cont = 0;
		do{
			linea = document.getElementById(tipoLinea[0]+cont+tipoLinea[1]);
			if(linea){
				linea.parentNode.removeChild(linea);
				cont = cont+1;
			}
		} while(linea);
		//this.parentNode.parentNode.removeChild(this.parentNode);
	}
	
	function creaEvento(form){
		var objetoValida = validaDescripciones();
		
		if(objetoValida.tieneDescripcion){
			if(objetoValida.descrDuplicada == ""){
				if(objetoValida.inhabilitado == ""){
					form.submit();	
				} else {
					alert(objetoValida.inhabilitado);
				}
			} else {
				alert("La descripci\u00f3n debe ser \u00fanica \["+objetoValida.descrDuplicada+"\]");
			}
		} else {
			alert("Es necesario ingresar la descripci\u00f3n");
		}
	}
	
	function validaDescripciones(){
		var objetoValida = {
			tieneDescripcion : true,
			descrDuplicada : "",
			inhabilitado : ""
		};
		
		var inhabilitarPresup = document.getElementById("checkPresup").checked;
		var inhabilitarPatri = document.getElementById("checkPatri").checked;
		
		if(inhabilitarPresup && inhabilitarPatri){
			objetoValida.inhabilitado = "Debe ingresar cuentas presupuestales o patrimoniales , una al menos";
			return objetoValida;
		}
		
		var listaAIterar = new Array();
		
		var co = 0;
		if(!inhabilitarPresup){
			listaAIterar[co] = {
				linea : "lineaPres",
				nombre : "descripcionPresup"
			};
			co = co+1;
		}
		if(!inhabilitarPatri){
			listaAIterar[co] = {
				linea : "lineaPatri",
				nombre : "descripcionPatri"
			};
		}
		
		
		for(var a = 0 ; a < listaAIterar.length ; a ++){
			var lineaActual = listaAIterar[a];
			var mapaDescripciones = {};
			var tbody1 = document.getElementById(lineaActual.linea);
			for (var i = 0, row; row = tbody1.children[i]; i++) {
				if(objetoValida.tieneDescripcion == false){break;}
				for (var j = 0, cell; cell = row.children[j]; j++) {
					if(objetoValida.tieneDescripcion == false){break;}
					for (var k = 0, elemento; elemento = cell.children[k]; k++) {
						if(elemento.name && elemento.name !="" && startsWith(elemento.name,lineaActual.nombre)){
							if(!elemento.value || 0 == elemento.value.length){
								elemento.focus();
								objetoValida.tieneDescripcion = false;
								break;
							} else {
								var descripcionTrim = elemento.value.trim();
								if(mapaDescripciones.hasOwnProperty(descripcionTrim)){
									objetoValida.descrDuplicada = descripcionTrim;
									break;
								} else {
									mapaDescripciones[descripcionTrim] = elemento;
									elemento.focus();
								}
							}
						}
					}
					
				}
			}
		}
		
		return objetoValida;
	}
	
	function startsWith(str, prefix) {
    	return str.lastIndexOf(prefix, 0) == 0;
	}
	
	function cambiarValor(elemento){
		if(elemento.value=="Y"){
			elemento.value="N"
		}else{
			elemento.value="Y"
		}
	}
</script>

<@frameSection title=uiLabelMap.CrearEvento >
	<@form name="RegistroEvento" url="RegistroEvento" >
		<@inputHidden name="parentTypeId" value="INTERNAL_ACCTG_TRANS" />
		<table >
		<tbody>
			<@seccionRow colspan="4" title=uiLabelMap.InfGeneral />
			<tr>
				<@displayTitleCell title=uiLabelMap.FinancialsDocumentoId titleClass="requiredField"/>
				<@inputTextCell name="acctgTransTypeId" maxlength=20  />
				<@displayTitleCell title=uiLabelMap.FinancialsDescripcionDoc titleClass="requiredField"/>
				<@inputTextCell name="descripcion" maxlength=60  />
			</tr>
			<tr>
				<@displayTitleCell title=uiLabelMap.FinancialModulo titleClass="requiredField"/>
				<@inputSelectCell name="moduloId" list=listModulos?default([]) key="moduloId" displayField="nombre"/>
				<@displayTitleCell title=uiLabelMap.TipoPoliza titleClass="requiredField"/>
				<@inputSelectCell name="tipoPolizaId" list=listTipoPoliza?default([]) key="tipoPolizaId" displayField="description"/>
			</tr>
			<@inputSelectRow name="predecesor" title=uiLabelMap.FinancialPredecesor  list=listEventoContable?default([]) key="acctgTransTypeId" displayField="descripcion" required=false/>
			<@seccionRow colspan="4" title=uiLabelMap.CuentasPresupuestales checkInhabilitar="true" nombreCheck="checkPresup"/>
			<#assign mapaComparacion = {"":"",">":"menor que",">=":"menor o igual que","<":"mayor que","<=":"mayor o igual que"} />
			<tr>
				<td colspan="4">
					<table>
					<tbody id="lineaPres">
						<tr id="lineaPres1">
							<@displayTitleCell title=uiLabelMap.CommonDescription titleClass="requiredField"/>
							<@inputTextCell name="descripcionPresup1" maxlength=255 />
							<@displayTitleCell title=uiLabelMap.FinancialTipoMatriz />
							<@inputSelectCell name="tipoMatrizId1" list=listTipoMatriz?default([]) key="tipoMatrizId" displayField="descripcion" required=false/>
						</tr>
						<tr id="lineaPres2">
							<@displayTitleCell title=uiLabelMap.CuentaCargo />
							<td>
							<@inputAutoCompleteGlAccount name="cuentaCargoPresup1" url="getAutoCompleteGlAccountsRegistro"/>
							<@inputSelectHash id="comparacion1" name="comparacion1" hash=mapaComparacion />
							</td>
							<@displayTitleCell title=uiLabelMap.CuentaAbono />
							<@inputAutoCompleteGlAccountCell name="cuentaAbonoPresup1" url="getAutoCompleteGlAccountsRegistro"/>
						</tr>
						<tr id="lineaPres3">
							<@displayTitleCell title=uiLabelMap.CatalogoCargo />
							<@inputSelectCell name="catalogoCargoPresup1" list=listTipoAuxiliar?default([]) key="tipoAuxiliarId" displayField="descripcion" required=false/>
							<@displayTitleCell title=uiLabelMap.CatalogoAbono />
							<@inputSelectCell name="catalogoAbonoPresup1" list=listTipoAuxiliar?default([]) key="tipoAuxiliarId" displayField="descripcion" required=false/> 				
						</tr>
						<tr><td colspan="2" align="right"><input type="button" value="${uiLabelMap.CommonAdd}" class="smallSubmit" onclick="javascript:agregaLinea('lineaPres');"/></td></tr>
					</tbody>
					</table>
				</td>
			</tr>
			<@seccionRow colspan="4" title=uiLabelMap.CuentasPatrimoniales checkInhabilitar="true" nombreCheck="checkPatri"/>
			<tr>
				<td colspan="4">
					<table>
					<tbody id="lineaPatri">			
						<tr id="lineaPatri1">
							<@displayTitleCell title=uiLabelMap.CommonDescription titleClass="requiredField"/>
							<@inputTextCell name="descripcionPatri1" maxlength=255 />						
						</tr>			
						<tr id="lineaPatri2">
							<@displayTitleCell title=uiLabelMap.CuentaCargo />
							<@inputAutoCompleteGlAccountCell name="cuentaCargoPatri1" url="getAutoCompleteGlAccountsRegistro"/>
							<@displayTitleCell title=uiLabelMap.CuentaAbono />
							<@inputAutoCompleteGlAccountCell name="cuentaAbonoPatri1" url="getAutoCompleteGlAccountsRegistro"/>
						</tr>
						<tr id="lineaPatri3">
							<@displayTitleCell title=uiLabelMap.CatalogoCargo />
							<@inputSelectCell name="catalogoCargoPatri1" list=listTipoAuxiliar?default([]) key="tipoAuxiliarId" displayField="descripcion" required=false/>
							<@displayTitleCell title=uiLabelMap.CatalogoAbono />
							<@inputSelectCell name="catalogoAbonoPatri1" list=listTipoAuxiliar?default([]) key="tipoAuxiliarId" displayField="descripcion" required=false/>
						</tr>
						<tr id="lineaPatri4">
							<@displayTitleCell title=uiLabelMap.Excepcion />
							<@inputCheckboxCell name="excepcion1" value="N" onChange="javascript:cambiarValor(this);"/>
						</tr>	
						<tr><td colspan="2" align="right"><input type="button" value="${uiLabelMap.CommonAdd}" class="smallSubmit" onclick="javascript:agregaLinea('lineaPatri');"/></td></tr>
					</tbody>
					</table>
				</td>
			</tr>		
			<tr><td colspan="2" align="right"><input type="button" value="${uiLabelMap.CommonCreate}" class="smallSubmit" onclick="javascript:creaEvento(this.form);"/></td></tr>	
			<#--<@inputButtonRow title=uiLabelMap.CommonCreate onClick="javascript:creaEvento(this.form);" />-->
			<#--<@inputSubmitRow title=uiLabelMap.CommonCreate />-->
		</tbody>
		</table>
	</@form>
</@frameSection>
