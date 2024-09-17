<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<script type="text/javascript">
	var textoEliminar = "${uiLabelMap.CommonDelete}";
	var textoAgregar = "A\u00f1adir";
	
	function agregaLinea(tipoLinea, contador)
	{
		eliminaAgregar(tipoLinea, contador);
		var contadorNuevo = parseInt(contador) + parseInt(1);
		var table = document.getElementById(tipoLinea);
		renglonSepara(table, contadorNuevo, tipoLinea);
		var suma = 1;
		var linea;
		var existeLinea = true;
		do{
			var linea = document.getElementById(tipoLinea + suma + "1");
			if(linea){
				agregaRenglon(linea, table, contadorNuevo, suma, tipoLinea);
				suma = suma + 1;
			}
		} while(linea);
		renglonEliminar(table, contadorNuevo, tipoLinea, suma);
		clearFields(tipoLinea, contadorNuevo);
		creaAgregar(table, contadorNuevo, tipoLinea);
	}
	
	function clearFields(tipoLinea, contadorNuevo)
	{
		if(tipoLinea == "lineaPres") {
			var descripcion = document.getElementById("descripcionPresup" + contadorNuevo);
			var tipoMatriz = document.getElementById("tipoMatrizId" + contadorNuevo);
			var compara = document.getElementById("comparacion" + contadorNuevo);
			var cuentaCargo = document.getElementById("cuentaCargoPresup" + contadorNuevo);
			var cuentaAbono = document.getElementById("cuentaAbonoPresup" + contadorNuevo);
			var catalogoCargo = document.getElementById("catalogoCargoPresup" + contadorNuevo);
			var catalogoAbono = document.getElementById("catalogoAbonoPresup" + contadorNuevo);
			tipoMatriz.value = "";
			compara.value = "";
		} else {
			var descripcion = document.getElementById("descripcionPatri" + contadorNuevo);
			var cuentaCargo = document.getElementById("cuentaCargoPatri" + contadorNuevo);
			var cuentaAbono = document.getElementById("cuentaAbonoPatri" + contadorNuevo);
			var catalogoCargo = document.getElementById("catalogoCargoPatri" + contadorNuevo);
			var catalogoAbono = document.getElementById("catalogoAbonoPatri" + contadorNuevo);
			var excepcion = document.getElementById("excepcion" + contadorNuevo);
			excepcion.checked = false;
		}
		descripcion.value = "";
		cuentaCargo.value = "";
		cuentaAbono.value = "";
		catalogoCargo.value = "";
		catalogoAbono.value = "";
	}
	
	function agregaRenglon(linea, table, contadorNuevo, suma, tipoLinea)
	{
		var nuevoRenglon = document.createElement("tr");
		nuevoRenglon.id = tipoLinea + suma.toString() + contadorNuevo.toString();
		for(var i=0; i<linea.childElementCount; i++)
		{	
			if (linea.cells[i] == undefined)
			{
				var nuevaCelda = "undefined";
			}
			else
			{
				var nuevaCelda = linea.cells[i].cloneNode(true);
			}
			var idPrincipal1;
			var idPrincipal2;
			var idTodos;
			var objetoAutocompletar;
			if (nuevaCelda != "undefined")
			{
				if(nuevaCelda.children.length > 1)
				{
					for(var j=0; j<nuevaCelda.children.length; j++)
					{
						var elementoN = nuevaCelda.children[j];
						switch(elementoN.nodeName)
						{
							case "DIV":
								var jsid = elementoN.getAttribute("jsid");
								var identificador = jsid.split("1");
								var idTodos = identificador[0] + contadorNuevo;
								elementoN.setAttribute("jsid", jsid.replace("1", (contadorNuevo)));
								break;
							case "TABLE":
								var widgetid = elementoN.getAttribute("widgetid");
								idPrincipal1 = widgetid;
								objetoAutocompletar = opentaps.initInputAutoCompleteQuick("getAutoCompleteGlAccountsRegistro", idTodos ,idTodos, "", "");
								var combo = objetoAutocompletar.domNode;
								nuevaCelda.replaceChild(combo, elementoN);
								break;
							case "INPUT":
								var id = elementoN.getAttribute("id");
								idPrincipal2 = id; 
								elementoN.id = id.replace("1", (contadorNuevo));
								elementoN.name = id.replace("1", (contadorNuevo));
								break;
							case "A":
								var href = elementoN.getAttribute("href");
								href = href.replace(idPrincipal1, idPrincipal1.replace("1", (contadorNuevo)));
								href = href.replace(idPrincipal2, idPrincipal2.replace("1", (contadorNuevo)));
								elementoN.setAttribute("href", href);
								break;
							case "SPAN":
								var id = elementoN.id;
								elementoN.setAttribute("id", id.replace("1", (contadorNuevo)));
								elementoN.innerHTML = "";
								break;
							case "SELECT":
								var id = elementoN.id;
								elementoN.setAttribute("id", id.replace("1", (contadorNuevo)));
								elementoN.setAttribute("name", id.replace("1", (contadorNuevo)));
								break;
						}
					}
				} else {
					var elemento = nuevaCelda.firstElementChild;
					var id = elemento.id;
					if(elemento.nodeName != 'SPAN')
					{
						elemento.id = id.replace("1",(contadorNuevo));
						elemento.name = elemento.id;
					}
				}
				nuevoRenglon.appendChild(nuevaCelda);
			}
		}
		table.appendChild(nuevoRenglon);
	}
	
	function renglonSepara(table, contadorNuevo, tipoLinea)
	{
		var renglonSeparador = document.createElement("tr");
		if(tipoLinea == "lineaPres") {
			renglonSeparador.id = "lineaPresHr" + contadorNuevo;
		} else {
			renglonSeparador.id = "lineaPatriHr" + contadorNuevo;
		}
		var nuevaCeldaSep = document.createElement("td");
		nuevaCeldaSep.innerHTML = "<hr>";
		nuevaCeldaSep.colSpan = "2";
		renglonSeparador.appendChild(nuevaCeldaSep);
		table.appendChild(renglonSeparador);
	}
	
	function renglonEliminar(table, contadorNuevo, tipoLinea, suma)
	{
		var idRenglonEliminar = parseInt(suma) + parseInt(1);
		var renglonEliminar = document.createElement("tr");
		renglonEliminar.id = tipoLinea + idRenglonEliminar;
		var nuevaCeldaEliminar = document.createElement("td");
		nuevaCeldaEliminar.colSpan = "4";
		nuevaCeldaEliminar.style.textAlign="right";
		var nuevoInput = document.createElement("input");
		if(tipoLinea == "lineaPres") {
			nuevoInput.name = "eliminaPresup" + contadorNuevo;
			nuevoInput.id = "eliminaPresup" + contadorNuevo;
		} else {
			nuevoInput.name = "eliminaPatri" + contadorNuevo;
			nuevoInput.id = "eliminaPatri" + contadorNuevo;
		}
		nuevoInput.type = "button";
		nuevoInput.value = textoEliminar;
		nuevoInput.className = "buttonDangerous";
		nuevoInput.addEventListener("click", function(){eliminar(tipoLinea, contadorNuevo)});
		nuevaCeldaEliminar.appendChild(nuevoInput);
		renglonEliminar.appendChild(nuevaCeldaEliminar);
		table.appendChild(renglonEliminar);
	}
	
	function creaAgregar(table, contadorNuevo, tipoLinea)
	{
		var renglonAgregar = document.createElement("tr");
		var nuevaCeldaAgregar = document.createElement("td");
		nuevaCeldaAgregar.colSpan = "2";
		nuevaCeldaAgregar.style.textAlign="right";
		var nuevoInput = document.createElement("input");
		if(tipoLinea == "lineaPres") {
			nuevoInput.name = "agregaPresup" + contadorNuevo;
			nuevoInput.id = "agregaPresup" + contadorNuevo;
		} else {
			nuevoInput.name = "agregaPatri" + contadorNuevo;
			nuevoInput.id = "agregaPatri" + contadorNuevo;
		}
		nuevoInput.type = "button";
		nuevoInput.value = textoAgregar;
		nuevoInput.className = "smallSubmit";
		nuevoInput.addEventListener("click", function(){agregaLinea(tipoLinea, contadorNuevo)});
		nuevaCeldaAgregar.appendChild(nuevoInput);
		renglonAgregar.appendChild(nuevaCeldaAgregar);
		table.appendChild(renglonAgregar);
	}	
	
	function eliminar(tipoLinea, contador)
	{
		if(tipoLinea == "lineaPres") {
			var tipoLinea = "lineaPres";
			var idElimina = "eliminaPresup" + contador;
		} else {
			var tipoLinea = "lineaPatri";
			var idElimina = "eliminaPatri" + contador;
		}
		eliminaLinea(tipoLinea + "Hr" + contador);
		eliminaLinea(tipoLinea + "1" + contador);
		eliminaLinea(tipoLinea + "2" + contador);
		eliminaLinea(tipoLinea + "3" + contador);
		if(tipoLinea == "lineaPatri")
		{
			eliminaLinea(tipoLinea + "4" + contador);
		}
		botonEliminar = document.getElementById(idElimina);
		botonEliminar.remove();
	}
	
	function eliminaLinea(nombreLinea)
	{
		do{
			linea = document.getElementById(nombreLinea);
			if(linea){
				linea.parentNode.removeChild(linea);
			}
		} while(linea);
	}
	
	function eliminaAgregar(tipoLinea, contador)
	{
		if(tipoLinea == "lineaPres") {
			var idBoton = "agregaPresup" + contador;
		} else {
			var idBoton = "agregaPatri" + contador;
		}
		botonAgregar = document.getElementById(idBoton);
		botonAgregar.remove();
	}
	
	function editaEvento(form){
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
	
	function cambiarValorCheck(contador){
		var check = document.getElementById("excepcion" + contador);
		if(check.value=="Y"){
			check.value="N"
			check.checked=false;
		}else{
			check.value="Y"
		}
	}
</script>

<@frameSection title=uiLabelMap.FinancialsEditarEvento >
	<@form name="EditarRegistroEvento" url="EditarRegistroEvento" >
		<@inputHidden name="parentTypeId" value="INTERNAL_ACCTG_TRANS" />
		<table >
		<tbody>
			<@seccionRow colspan="4" title=uiLabelMap.InfGeneral />
			<tr>
				<@displayTitleCell title=uiLabelMap.FinancialsDocumentoId titleClass="requiredField"/>
				<@inputTextCell name="acctgTransTypeId" maxlength=20 default="${eventoContable.acctgTransTypeId}" />
				<@displayTitleCell title=uiLabelMap.FinancialsDescripcionDoc titleClass="requiredField"/>
				<@inputTextCell name="descripcion" maxlength=60  default="${eventoContable.descripcion}" />
			</tr>
			<tr>
				<@displayTitleCell title=uiLabelMap.FinancialModulo titleClass="requiredField"/>
				<@inputSelectCell name="moduloId" list=listModulos?default([]) key="moduloId" displayField="nombre" default="${eventoContable.moduloId}"/>
				<@displayTitleCell title=uiLabelMap.TipoPoliza titleClass="requiredField"/>
				<@inputSelectCell name="tipoPolizaId" list=listTipoPoliza?default([]) key="tipoPolizaId" displayField="description" default="${eventoContable.tipoPolizaId}" />
			</tr>
			<@inputSelectRow name="predecesor" title=uiLabelMap.FinancialPredecesor  list=listEventoContable?default([]) key="acctgTransTypeId" displayField="descripcion" required=false default="${eventoContable.predecesor?if_exists}"/>
			<@seccionRow colspan="4" title=uiLabelMap.CuentasPresupuestales checkInhabilitar="true" nombreCheck="checkPresup" defaultCheck="${tieneLineaPres}" />
			<tr>
				<td colspan="4">
					<table>
					<tbody id="lineaPres">
					<#assign contP = 0 />
					<#list lineaPresupuestal as lineaP>
					<#assign contP = contP + 1 />
					<#if (contP > 1)>
						<tr id="lineaPresHr${contP}"> <td colspan="2"> <hr>  </hr> </td> </tr>
					</#if>
						<tr id="lineaPres1${contP}">
							<@displayTitleCell title=uiLabelMap.CommonDescription titleClass="requiredField"/>
							<@inputTextCell name="descripcionPresup${contP}" id="descripcionPresup${contP}" maxlength=255 default=lineaP.descripcion />
							<@displayTitleCell title=uiLabelMap.FinancialTipoMatriz />
							<@inputSelectCell name="tipoMatrizId${contP}" id="tipoMatrizId${contP}" list=listTipoMatriz?default([]) key="tipoMatrizId" displayField="descripcion" required=false default=lineaP.tipoMatrizId?if_exists />
						</tr>
						<tr id="lineaPres2${contP}">
							<@displayTitleCell title=uiLabelMap.CuentaCargo />
							<td>
							<@inputAutoCompleteGlAccount name="cuentaCargoPresup${contP}" url="getAutoCompleteGlAccountsRegistro" default=lineaP.cuentaCargo />
							<@inputSelect name="comparacion${contP}" id="comparacion${contP}" list=comparacion?default([]) key="idComparacion" displayField="descComparacion" required=false default=lineaP.comparacion?if_exists />
							</td>
							<@displayTitleCell title=uiLabelMap.CuentaAbono />
							<@inputAutoCompleteGlAccountCell name="cuentaAbonoPresup${contP}" url="getAutoCompleteGlAccountsRegistro" default=lineaP.cuentaAbono/>
						</tr>
						<tr id="lineaPres3${contP}">
							<@displayTitleCell title=uiLabelMap.CatalogoCargo />
							<@inputSelectCell name="catalogoCargoPresup${contP}" list=listTipoAuxiliar?default([]) key="tipoAuxiliarId" displayField="descripcion" required=false default=lineaP.catalogoCargo?if_exists/>
							<@displayTitleCell title=uiLabelMap.CatalogoAbono />
							<@inputSelectCell name="catalogoAbonoPresup${contP}" list=listTipoAuxiliar?default([]) key="tipoAuxiliarId" displayField="descripcion" required=false default=lineaP.catalogoAbono?if_exists />
						</tr>
						<#if (contP > 1)>
							<tr><td colspan="4" align="right"><input name=eliminaPresup${contP} id=eliminaPresup${contP} type="button" value="${uiLabelMap.CommonDelete}" class="buttonDangerous" onclick="javascript:eliminar('lineaPres','${contP}');"/></td></tr>
						</#if>
					</#list>
					<tr><td colspan="2" align="right"><input name=agregaPresup${contP} id=agregaPresup${contP} type="button" value="${uiLabelMap.CommonAdd}" class="smallSubmit" onclick="javascript:agregaLinea('lineaPres','${contP}');"/></td></tr>
					</tbody>
					</table>
				</td>
			</tr>
			<@seccionRow colspan="4" title=uiLabelMap.CuentasPatrimoniales checkInhabilitar="true" nombreCheck="checkPatri" defaultCheck="${tieneLineaCont}" />
			<tr>
				<td colspan="4">
					<table>
					<tbody id="lineaPatri">
					<#assign contC = 0 />
					<#list lineaContable as lineaC>
					<#assign contC = contC + 1 />
					<#if (contC > 1)>
						<tr id="lineaPatriHr${contC}"> <td colspan="2"> <hr>  </hr> </td> </tr>
					</#if>
						<tr id="lineaPatri1${contC}">
							<@displayTitleCell title=uiLabelMap.CommonDescription titleClass="requiredField"/>
							<@inputTextCell name="descripcionPatri${contC}" id="descripcionPatri${contC}" maxlength=255 default=lineaC.descripcion />						
						</tr>			
						<tr id="lineaPatri2${contC}">
							<@displayTitleCell title=uiLabelMap.CuentaCargo />
							<@inputAutoCompleteGlAccountCell name="cuentaCargoPatri${contC}" url="getAutoCompleteGlAccountsRegistro" default=lineaC.cuentaCargo />
							<@displayTitleCell title=uiLabelMap.CuentaAbono />
							<@inputAutoCompleteGlAccountCell name="cuentaAbonoPatri${contC}" url="getAutoCompleteGlAccountsRegistro" default=lineaC.cuentaAbono />
						</tr>
						<tr id="lineaPatri3${contC}">
							<@displayTitleCell title=uiLabelMap.CatalogoCargo />
							<@inputSelectCell name="catalogoCargoPatri${contC}" list=listTipoAuxiliar?default([]) key="tipoAuxiliarId" displayField="descripcion" required=false default=lineaC.catalogoCargo?if_exists />
							<@displayTitleCell title=uiLabelMap.CatalogoAbono />
							<@inputSelectCell name="catalogoAbonoPatri${contC}" list=listTipoAuxiliar?default([]) key="tipoAuxiliarId" displayField="descripcion" required=false default=lineaC.catalogoAbono?if_exists />
						</tr>
						<tr id="lineaPatri4${contC}">
							<@displayTitleCell title=uiLabelMap.Excepcion />
							<@inputCheckboxCell name="excepcion${contC}" id="excepcion${contC}" value="Y" onChange="javascript:cambiarValor(this);" default="Y" />
							<#if lineaC.excepcion?exists>
								<#if lineaC.excepcion == "N">
									<script type="text/javascript">
	                  					cambiarValorCheck(${contC})
                					</script>
								</#if>
							<#else>
								<script type="text/javascript">
                  					cambiarValorCheck(${contC})
                				</script>
							</#if>
						</tr>
						<#if (contC > 1)>
							<tr><td colspan="4" align="right"><input name=eliminaPatri${contC} id=eliminaPatri${contC} type="button" value="${uiLabelMap.CommonDelete}" class="buttonDangerous" onclick="javascript:eliminar('lineaPatri','${contC}');"/></td></tr>
						</#if>
					</#list>
					<tr><td colspan="2" align="right"><input name=agregaPatri${contC} id=agregaPatri${contC} type="button" value="${uiLabelMap.CommonAdd}" class="smallSubmit" onclick="javascript:agregaLinea('lineaPatri','${contC}');"/></td></tr>
					</tbody>
					</table>
				</td>
			</tr>		
			<tr><td colspan="2" align="right"><input type="button" value="${uiLabelMap.CommonSave}" class="smallSubmit" onclick="javascript:editaEvento(this.form);"/></td></tr>	
		</tbody>
		</table>
	</@form>
	
</@frameSection>
