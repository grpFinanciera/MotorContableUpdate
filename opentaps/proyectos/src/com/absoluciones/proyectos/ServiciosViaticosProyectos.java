package com.absoluciones.proyectos;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import javolution.util.FastMap;

public class ServiciosViaticosProyectos {

	private static String MODULE = ServiciosProyectos.class.getName();

	/**
	 * Crea Proyecto
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */

	public static Map<String, Object> registraViatico(DispatchContext dctx, Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Map<String, Object> input = FastMap.newInstance();
		Map<String, Object> output = FastMap.newInstance();
		Map<String, Object> montoMap = FastMap.newInstance();
		Map<String, Object> clavePresupuestalMap = FastMap.newInstance();
		Map<String, Object> catalogoCargoContMap = FastMap.newInstance();
		Map<String, Object> catalogoAbonoContMap = FastMap.newInstance();
		Map<String, Object> context2 = FastMap.newInstance();
		String poliza = null;
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String partyId = userLogin.getString("partyId");
		String organizationPartyId = (String) context.get("organizationPartyId");
		Timestamp fechaInicio = (Timestamp) context.get("fechaInicio");
		Timestamp fechaFin = (Timestamp) context.get("fechaFin");
		String tipoMoneda = (String) context.get("tipoMoneda");
		String motivo = (String) context.get("motivo");
		String tipoTransporteId = (String) context.get("tipoTransporteId");
		String geoOrigen = (String) context.get("geoOrigen");
		String geoDestino = (String) context.get("geoDestino");
		BigDecimal montoDiario = (BigDecimal) context.get("montoDiario");
		BigDecimal montoTrabCampo = (BigDecimal) context.get("montoTrabCampo");
		BigDecimal montoTransporte = (BigDecimal) context.get("montoTransporte");
		String acctgTransTypeId = (String) context.get("eventoSolicitud");
		String proyectoId = (String) context.get("proyectoId");
		String paymentTypeId = (String) context.get("paymentTypeId");
		String cuentaBancariaId = (String) context.get("cuentaOrigen");
		String auxiliar = (String) context.get("auxiliar");
		String tipoViatico = (String) context.get("tipoViatico");

		try {

			GenericValue viatico = GenericValue.create(delegator.getModelEntity("ViaticosProyecto"));
			GenericValue proyecto = delegator.findByPrimaryKey("Proyecto", UtilMisc.toMap("proyectoId", proyectoId));
			/*if (UtilValidate.isNotEmpty(proyecto)) {
				cuentaBancariaId = proyecto.getString("cuentaOrigen");
			}*/
			viatico.setAllFields(context, false, null, null);
			viatico.setNextSeqId();
			viatico.set("fechaInicial", fechaInicio);
			viatico.set("fechaFinal", fechaFin);
			viatico.set("motivo", motivo);
			viatico.set("tipoMoneda", tipoMoneda);
			viatico.set("tipoTransporteId", tipoTransporteId);
			viatico.set("montoDiario", montoDiario);
			viatico.set("montoTrabCampo", montoTrabCampo);
			viatico.set("montoTransporte", montoTransporte);
			viatico.set("geoOrigenId", geoOrigen);
			viatico.set("geoDestinoId", geoDestino);
			viatico.set("statusId", "SOLICITADO");
			viatico.set("acctgTransTypeId", acctgTransTypeId);
			viatico.set("tipoMoneda", tipoMoneda);
			viatico.set("auxEmpleado", auxiliar);
			viatico.set("tipoViatico", tipoViatico);
			viatico.set("paymentTypeId", paymentTypeId);

			List<String> orderByLinea = UtilMisc.toList("secuencia");
			EntityCondition condicionLinea = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, acctgTransTypeId));
			List<GenericValue> lineaList = delegator.findByCondition("LineaContable", condicionLinea, null,
					orderByLinea);
			Debug.logInfo("condicion: " + condicionLinea, MODULE);
			Debug.logInfo("lineaList: " + lineaList, MODULE);
			BigDecimal montoTotal = BigDecimal.ZERO;
			
			if(UtilValidate.isNotEmpty(montoDiario)){
				montoTotal = montoTotal.add(montoDiario);
			}
			
			if(UtilValidate.isNotEmpty(montoTrabCampo)){
				montoTotal = montoTotal.add(montoTrabCampo);
			}
			
			if(UtilValidate.isNotEmpty(montoTransporte)){
				montoTotal = montoTotal.add(montoTransporte);
			}

			if (!lineaList.isEmpty()) {
				Iterator<GenericValue> listaIter = lineaList.iterator();

				while (listaIter.hasNext()) {
					GenericValue item = listaIter.next();
					String descripcionLinea = item.getString("descripcion");
					descripcionLinea = descripcionLinea.replaceAll(" ", "");
					catalogoCargoContMap.put(descripcionLinea + "0",
							item.getString("catalogoCargo") != null
									&& item.getString("catalogoCargo").equalsIgnoreCase("BANCO") ? cuentaBancariaId
											: auxiliar);
					catalogoAbonoContMap.put(descripcionLinea + "0",
							item.getString("catalogoAbono") != null
									&& item.getString("catalogoAbono").equalsIgnoreCase("BANCO") ? cuentaBancariaId
											: auxiliar);
					montoMap.put(descripcionLinea + "0", montoTotal.toString());
				}
			}
			clavePresupuestalMap.put("0", "");

			context2.put("montoMap", montoMap);
			context2.put("clavePresupuestalMap", clavePresupuestalMap);
			context2.put("catalogoCargoContMap", catalogoCargoContMap);
			context2.put("catalogoAbonoContMap", catalogoAbonoContMap);

			Debug.log("montoMap: " + montoMap);
			Debug.log("clavePresupuestalMap: " + clavePresupuestalMap);
			Debug.log("catalogoCargoContMap: " + catalogoCargoContMap);
			Debug.log("catalogoAbonoContMap: " + catalogoAbonoContMap);

			// Invocacion a motor contable
			/***** MOTOR CONTABLE *****/

			input.put("eventoContableId", acctgTransTypeId);
			input.put("tipoClaveId", "EGRESO");
			input.put("fechaRegistro", UtilDateTime.nowTimestamp());
			input.put("fechaContable", fechaInicio);
			input.put("currency", tipoMoneda);
			input.put("usuario", userLogin.getString("userLoginId"));
			input.put("organizationId", organizationPartyId);
			input.put("descripcion", "Viatico " + motivo);
			input.put("roleTypeId", "BILL_FROM_VENDOR");
			input.put("valorCampo", proyectoId);
			input.put("campo", "proyectoId");
			input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(acctgTransTypeId, delegator,
					context2, null, null, null, null));

			// Genera registro en DepreciacionResumen
			output = dispatcher.runSync("creaTransaccionMotor", input);
			Debug.log("output: " + output);
			if (ServiceUtil.isError(output)) {
				return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
			}

			GenericValue transaccion = (GenericValue) output.get("transaccion");
			poliza = transaccion.getString("poliza");

			viatico.set("acctgTransIdSolicitud", transaccion.getString("acctgTransId"));
			viatico.create();

			Map<String, Object> result = ServiceUtil
					.returnSuccess("El viatico ha sido creado con \u00e9xito con la poliza " + poliza);

			result.put("viaticoProyectoId", viatico.getString("viaticoProyectoId"));
			return result;

		} catch (GenericEntityException | GenericServiceException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}

	/**
	 * Agrega un articulo a el viatico , realizando validaciones necesarias
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> agregaComprobanteViaticoProy(DispatchContext dctx, Map<?, ?> context) {

		Delegator delegator = dctx.getDelegator();
		String viaticoProyectoId = (String) context.get("viaticoProyectoId");
		BigDecimal monto = (BigDecimal) context.get("monto");
		Timestamp fecha = (Timestamp) context.get("fecha");
		String conceptoViaticoId = (String) context.get("conceptoViatico");
		String referencia = (String) context.get("referencia");
		String descripcion = (String) context.get("descripcion");
		String gastoProyecto = (String) context.get("gastoProyecto");

		try {

			// Agregamos los datos del viatico.
			GenericValue viaticoComprobanteMonto = GenericValue
					.create(delegator.getModelEntity("ConceptoViaticoMontoProy"));
			viaticoComprobanteMonto.setNextSeqId();
			viaticoComprobanteMonto.set("viaticoProyectoId", viaticoProyectoId);
			viaticoComprobanteMonto.set("monto", monto);
			viaticoComprobanteMonto.set("fecha", fecha);
			viaticoComprobanteMonto.set("conceptoViaticoId", conceptoViaticoId);
			viaticoComprobanteMonto.set("referencia", referencia);
			viaticoComprobanteMonto.set("descripcion", descripcion);
			viaticoComprobanteMonto.set("gastoProyId", gastoProyecto);

			delegator.create(viaticoComprobanteMonto);

			// Aumentamos el monto Diario o monto Transporte Comprobado.
			GenericValue viatico = delegator.findByPrimaryKey("ViaticosProyecto",
					UtilMisc.toMap("viaticoProyectoId", viaticoProyectoId));
			GenericValue concepto = delegator.findByPrimaryKey("ConceptoViatico",
					UtilMisc.toMap("conceptoViaticoId", conceptoViaticoId));
			String campo = concepto.getString("diarioTransporteFlag").equalsIgnoreCase("D") ? "montoDiarioComprobado"
					: concepto.getString("diarioTransporteFlag").equalsIgnoreCase("T") ? "montoTransporteComprobado"
							: "montoTrabCampoComprobado";
			BigDecimal montoTotal = viatico.getBigDecimal(campo);
			viatico.set(campo, montoTotal == null ? monto : montoTotal.add(monto));
			delegator.store(viatico);

			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("viaticoProyectoId", viaticoProyectoId);
			return result;

		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}

	}

	/**
	 * Crea Proyecto
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */

	public static Map<String, Object> generaPolizaComprobada(DispatchContext dctx, Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Map<String, Object> input = FastMap.newInstance();
		Map<String, Object> output = FastMap.newInstance();
		Map<String, Object> montoMap = FastMap.newInstance();
		Map<String, Object> clavePresupuestalMap = FastMap.newInstance();
		Map<String, Object> catalogoCargoContMap = FastMap.newInstance();
		Map<String, Object> catalogoAbonoContMap = FastMap.newInstance();
		Map<String, Object> context2 = FastMap.newInstance();
		String poliza = null;
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String partyId = userLogin.getString("partyId");
		String viaticoProyectoId = (String) context.get("viaticoProyectoId");
		String eventoComprueba = (String) context.get("eventoComprueba");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		BigDecimal montoComprueba = (BigDecimal) context.get("montoComprueba");
		String auxiliarCargo = null;
		String auxiliarAbono = null;
		String cuentaBancariaId = null;

		try {

			GenericValue viatico = delegator.findByPrimaryKey("ViaticosProyecto",
					UtilMisc.toMap("viaticoProyectoId", viaticoProyectoId));
			GenericValue proyecto = delegator.findByPrimaryKey("Proyecto",
					UtilMisc.toMap("proyectoId", viatico.getString("proyectoId")));

			BigDecimal montoDiario = BigDecimal.ZERO;
			BigDecimal montoTrabCampo = BigDecimal.ZERO;
			BigDecimal montoTransporte = BigDecimal.ZERO;

			if (UtilValidate.isNotEmpty(viatico.getBigDecimal("montoDiario"))) {
				montoDiario = viatico.getBigDecimal("montoDiario");
			}
			if (UtilValidate.isNotEmpty(viatico.getBigDecimal("montoTrabCampo"))) {
				montoTrabCampo = viatico.getBigDecimal("montoTrabCampo");
			}
			if (UtilValidate.isNotEmpty(viatico.getBigDecimal("montoTransporte"))) {
				montoTransporte = viatico.getBigDecimal("montoTransporte");
			}
			if (UtilValidate.isNotEmpty(viatico.getString("auxEmpleado"))) {
				auxiliarAbono = viatico.getString("auxEmpleado");
			}
			if (UtilValidate.isNotEmpty(proyecto.getString("cuentaOrigen"))) {
				cuentaBancariaId = proyecto.getString("cuentaOrigen");
			}
			if (UtilValidate.isNotEmpty(proyecto.getString("auxiliarProyecto"))) {
				auxiliarCargo = proyecto.getString("auxiliarProyecto");
			}

			BigDecimal montoDiarioComprobado = BigDecimal.ZERO;
			BigDecimal montoTrabCampoComprobado = BigDecimal.ZERO;
			BigDecimal montoTransporteComprobado = BigDecimal.ZERO;

			if (UtilValidate.isNotEmpty(viatico.getBigDecimal("montoDiarioComprobado"))) {
				montoDiarioComprobado = viatico.getBigDecimal("montoDiarioComprobado");
			}
			if (UtilValidate.isNotEmpty(viatico.getBigDecimal("montoTrabCampoComprobado"))) {
				montoTrabCampoComprobado = viatico.getBigDecimal("montoTrabCampoComprobado");
			}
			if (UtilValidate.isNotEmpty(viatico.getBigDecimal("montoTransporteComprobado"))) {
				montoTransporteComprobado = viatico.getBigDecimal("montoTransporteComprobado");
			}

			List<String> orderByLinea = UtilMisc.toList("secuencia");
			EntityCondition condicionLinea = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, eventoComprueba));
			List<GenericValue> lineaList = delegator.findByCondition("LineaContable", condicionLinea, null,
					orderByLinea);
			Debug.logInfo("condicion: " + condicionLinea, MODULE);
			Debug.logInfo("lineaList: " + lineaList, MODULE);
			BigDecimal montoTotal = montoDiario.add(montoTrabCampo.add(montoTransporte));
			BigDecimal montoRestante = montoTotal.subtract(montoComprueba);

			if (!lineaList.isEmpty()) {
				Iterator<GenericValue> listaIter = lineaList.iterator();

				while (listaIter.hasNext()) {
					GenericValue item = listaIter.next();
					String descripcionLinea = item.getString("descripcion");
					descripcionLinea = descripcionLinea.replaceAll(" ", "");
					catalogoCargoContMap.put(descripcionLinea + "0",
							item.getString("catalogoCargo") != null
									&& item.getString("catalogoCargo").equalsIgnoreCase("BANCO") ? cuentaBancariaId
											: auxiliarCargo);
					catalogoAbonoContMap.put(descripcionLinea + "0",
							item.getString("catalogoAbono") != null
									&& item.getString("catalogoAbono").equalsIgnoreCase("BANCO") ? cuentaBancariaId
											: auxiliarAbono);
					montoMap.put(descripcionLinea + "0", montoComprueba.toString());
				}
			}

			BigDecimal montoRestanteP = proyecto.getBigDecimal("montoRestante");
			BigDecimal montoRestantePF = montoRestanteP.subtract(montoComprueba);
			
			if(proyecto.getString("validacion").equals("Y")){
				if (montoRestantePF.compareTo(BigDecimal.ZERO) <= 0) {
					return ServiceUtil.returnError("No cuenta con suficiencia en el proyecto para esta operacion");
				}
			}
			

			clavePresupuestalMap.put("0", "");

			context2.put("montoMap", montoMap);
			context2.put("clavePresupuestalMap", clavePresupuestalMap);
			context2.put("catalogoCargoContMap", catalogoCargoContMap);
			context2.put("catalogoAbonoContMap", catalogoAbonoContMap);

			Debug.log("montoMap: " + montoMap);
			Debug.log("clavePresupuestalMap: " + clavePresupuestalMap);
			Debug.log("catalogoCargoContMap: " + catalogoCargoContMap);
			Debug.log("catalogoAbonoContMap: " + catalogoAbonoContMap);

			// Invocacion a motor contable
			/***** MOTOR CONTABLE *****/

			input.put("eventoContableId", eventoComprueba);
			input.put("tipoClaveId", "EGRESO");
			input.put("fechaRegistro", UtilDateTime.nowTimestamp());
			input.put("fechaContable", fechaContable);
			input.put("currency", viatico.getString("tipoMoneda"));
			input.put("usuario", userLogin.getString("userLoginId"));
			input.put("organizationId", "1");
			input.put("descripcion", "Comprobacion viatico " + viatico.getString("motivo"));
			input.put("roleTypeId", "BILL_FROM_VENDOR");
			input.put("valorCampo", viatico.getString("proyectoId"));
			input.put("campo", "proyectoId");
			input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(eventoComprueba, delegator,
					context2, null, null, null, null));

			// Genera registro en DepreciacionResumen
			output = dispatcher.runSync("creaTransaccionMotor", input);
			Debug.log("output: " + output);
			if (ServiceUtil.isError(output)) {
				return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
			}

			GenericValue transaccion = (GenericValue) output.get("transaccion");
			poliza = transaccion.getString("poliza");

			viatico.set("acctgTransIdComprobacion", transaccion.getString("acctgTransId"));
			
			proyecto.set("montoRestante", montoRestantePF);
			Map<String, Object> result = ServiceUtil.returnSuccess();
			
			if (montoRestante.compareTo(BigDecimal.ZERO) == 1) {
				viatico.set("statusId", "DEVOLVER");
				result = ServiceUtil.returnSuccess("El viatico se ha comprobado con exito con la poliza " + poliza);
			} else {
				viatico.set("statusId", "FINALIZADO");
				result = ServiceUtil.returnSuccess("El viatico se ha comprobado con exito con la poliza " + poliza);
			}
			proyecto.store();
			viatico.store();

			result.put("viaticoProyectoId", viatico.getString("viaticoProyectoId"));
			return result;

		} catch (GenericEntityException | GenericServiceException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}
	
	public static Map<String, Object> generaPolizaDevo(DispatchContext dctx, Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		
		Map<String, Object> inputDevo = FastMap.newInstance();
		Map<String, Object> outputDevo = FastMap.newInstance();
		Map<String, Object> montoMapDevo = FastMap.newInstance();
		Map<String, Object> clavePresupuestalMapDevo = FastMap.newInstance();
		Map<String, Object> catalogoCargoContMapDevo = FastMap.newInstance();
		Map<String, Object> catalogoAbonoContMapDevo = FastMap.newInstance();
		Map<String, Object> context2Devo = FastMap.newInstance();
		String polizaDevo = null;
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String viaticoProyectoId = (String) context.get("viaticoProyectoId");
		String eventoDevuelve = (String) context.get("eventoDevuelve");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		String auxiliarCargo = null;
		String auxiliarAbono = null;
		String cuentaBancariaId = null;

		try {

			GenericValue viatico = delegator.findByPrimaryKey("ViaticosProyecto",
					UtilMisc.toMap("viaticoProyectoId", viaticoProyectoId));
			GenericValue proyecto = delegator.findByPrimaryKey("Proyecto",
					UtilMisc.toMap("proyectoId", viatico.getString("proyectoId")));

			BigDecimal montoDiario = BigDecimal.ZERO;
			BigDecimal montoTrabCampo = BigDecimal.ZERO;
			BigDecimal montoTransporte = BigDecimal.ZERO;

			if (UtilValidate.isNotEmpty(viatico.getBigDecimal("montoDiario"))) {
				montoDiario = viatico.getBigDecimal("montoDiario");
			}
			if (UtilValidate.isNotEmpty(viatico.getBigDecimal("montoTrabCampo"))) {
				montoTrabCampo = viatico.getBigDecimal("montoTrabCampo");
			}
			if (UtilValidate.isNotEmpty(viatico.getBigDecimal("montoTransporte"))) {
				montoTransporte = viatico.getBigDecimal("montoTransporte");
			}
			if (UtilValidate.isNotEmpty(viatico.getString("auxEmpleado"))) {
				auxiliarAbono = viatico.getString("auxEmpleado");
			}
			if (UtilValidate.isNotEmpty(proyecto.getString("cuentaOrigen"))) {
				cuentaBancariaId = proyecto.getString("cuentaOrigen");
			}
			if (UtilValidate.isNotEmpty(proyecto.getString("auxiliarProyecto"))) {
				auxiliarCargo = proyecto.getString("auxiliarProyecto");
			}

			BigDecimal montoDiarioComprobado = BigDecimal.ZERO;
			BigDecimal montoTrabCampoComprobado = BigDecimal.ZERO;
			BigDecimal montoTransporteComprobado = BigDecimal.ZERO;

			if (UtilValidate.isNotEmpty(viatico.getBigDecimal("montoDiarioComprobado"))) {
				montoDiarioComprobado = viatico.getBigDecimal("montoDiarioComprobado");
			}
			if (UtilValidate.isNotEmpty(viatico.getBigDecimal("montoTrabCampoComprobado"))) {
				montoTrabCampoComprobado = viatico.getBigDecimal("montoTrabCampoComprobado");
			}
			if (UtilValidate.isNotEmpty(viatico.getBigDecimal("montoTransporteComprobado"))) {
				montoTransporteComprobado = viatico.getBigDecimal("montoTransporteComprobado");
			}
			
			BigDecimal montoTotal = montoDiario.add(montoTrabCampo.add(montoTransporte));
			BigDecimal montoTotalComprobado = montoDiarioComprobado
					.add(montoTrabCampoComprobado.add(montoTransporteComprobado));
			BigDecimal montoRestante = montoTotal.subtract(montoTotalComprobado);

			List<String> orderByLineaDevo = UtilMisc.toList("secuencia");
			EntityCondition condicionLineaDevo = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, eventoDevuelve));
			List<GenericValue> lineaListDevo = delegator.findByCondition("LineaContable", condicionLineaDevo, null,
					orderByLineaDevo);

			if (!lineaListDevo.isEmpty()) {
				Iterator<GenericValue> listaIter = lineaListDevo.iterator();
				while (listaIter.hasNext()) {
					GenericValue item = listaIter.next();
					String descripcionLinea = item.getString("descripcion");
					descripcionLinea = descripcionLinea.replaceAll(" ", "");
					catalogoCargoContMapDevo.put(descripcionLinea + "0",
							item.getString("catalogoCargo") != null
									&& item.getString("catalogoCargo").equalsIgnoreCase("BANCO") ? cuentaBancariaId
											: auxiliarCargo);
					catalogoAbonoContMapDevo.put(descripcionLinea + "0",
							item.getString("catalogoAbono") != null
									&& item.getString("catalogoAbono").equalsIgnoreCase("BANCO") ? cuentaBancariaId
											: auxiliarAbono);
					montoMapDevo.put(descripcionLinea + "0", montoRestante.toString());
				}
			}
			clavePresupuestalMapDevo.put("0", "");

			context2Devo.put("montoMap", montoMapDevo);
			context2Devo.put("clavePresupuestalMap", clavePresupuestalMapDevo);
			context2Devo.put("catalogoCargoContMap", catalogoCargoContMapDevo);
			context2Devo.put("catalogoAbonoContMap", catalogoAbonoContMapDevo);

			Debug.log("montoMap: " + montoMapDevo);
			Debug.log("clavePresupuestalMap: " + clavePresupuestalMapDevo);
			Debug.log("catalogoCargoContMap: " + catalogoCargoContMapDevo);
			Debug.log("catalogoAbonoContMap: " + catalogoAbonoContMapDevo);

			inputDevo.put("eventoContableId", eventoDevuelve);
			inputDevo.put("tipoClaveId", "EGRESO");
			inputDevo.put("fechaRegistro", UtilDateTime.nowTimestamp());
			inputDevo.put("fechaContable", fechaContable);
			inputDevo.put("currency", viatico.getString("tipoMoneda"));
			inputDevo.put("usuario", userLogin.getString("userLoginId"));
			inputDevo.put("organizationId", "1");
			inputDevo.put("descripcion", "Devolucion viatico " + viatico.getString("motivo"));
			inputDevo.put("roleTypeId", "BILL_FROM_VENDOR");
			inputDevo.put("valorCampo", viatico.getString("proyectoId"));
			inputDevo.put("campo", "proyectoId");
			inputDevo.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(eventoDevuelve,
					delegator, context2Devo, null, null, null, null));

			outputDevo = dispatcher.runSync("creaTransaccionMotor", inputDevo);
			Debug.log("output: " + outputDevo);
			if (ServiceUtil.isError(outputDevo)) {
				return ServiceUtil.returnError(ServiceUtil.getErrorMessage(outputDevo));
			}

			GenericValue transaccionDevo = (GenericValue) outputDevo.get("transaccion");
			polizaDevo = transaccionDevo.getString("poliza");

			viatico.set("acctgTransIdDevuelve", transaccionDevo.getString("acctgTransId"));
			
			Map<String, Object> result = ServiceUtil.returnSuccess();
			
			viatico.set("statusId", "FINALIZADO");
			result = ServiceUtil.returnSuccess("El viatico se ha comprobado con exito con la poliza " + polizaDevo);
			
			proyecto.store();
			viatico.store();

			result.put("viaticoProyectoId", viatico.getString("viaticoProyectoId"));
			return result;

		} catch (GenericEntityException | GenericServiceException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}
	
	public static Map<String, Object> devolverViaticoProyecto(DispatchContext dctx, Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		String viaticoProyectoId = (String) context.get("viaticoProyectoId");
		
		try {
			
			delegator.storeByCondition("ViaticosProyecto", UtilMisc.toMap("statusId","DEVOLVER"), EntityCondition.makeCondition("viaticoProyectoId", viaticoProyectoId));
			
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result = ServiceUtil.returnSuccess("Se ha procesado el viatico para que realice la devolucion ");
			result.put("viaticoProyectoId", viaticoProyectoId);
			return result;			
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}

}
