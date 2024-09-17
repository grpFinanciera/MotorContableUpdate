package org.opentaps.dataimport;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;

import org.ofbiz.accounting.util.UtilAccounting;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.opentaps.base.entities.AcctgTransEntry;
import org.opentaps.base.entities.BuscarViatico;
import org.opentaps.common.builder.EntityListBuilder;
import org.opentaps.common.builder.PageBuilder;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.service.ServiceException;

import com.ibm.icu.util.Calendar;

public class MotorContable {

	// Map<String, String> cuentas;

	public static GenericValue buscaEstructuraClave(Delegator delegator,
			String ciclo, String tipoClave) throws GenericEntityException {
		List<GenericValue> estructura = delegator
				.findByAnd("EstructuraClave", UtilMisc.toMap("ciclo", ciclo,
						"acctgTagUsageTypeId", tipoClave));
		if (estructura.isEmpty()) {
			Debug.log("Ocurrio algo raro");
			return null;
		} else {
			Debug.log("Encontro: " + estructura.size());
			return estructura.get(0);
		}

	}

	public static String validaClaveVigencia(Delegator delegator,
			GenericValue estructura, List<String> clavePres, boolean vigencia,
			Date fecha) throws GenericEntityException {
		String mensaje = "";
		for (int i = 1; i <= clavePres.size(); i++) {
			String tipoClasificacion = estructura
					.getString("clasificacion" + i);
			List<GenericValue> clasificacion;
			if (tipoClasificacion.equalsIgnoreCase("CL_ADMINISTRATIVA")) {
				clasificacion = delegator.findByAnd("Party",
						UtilMisc.toMap("externalId", clavePres.get(i - 1)));
				if (clasificacion.isEmpty()) {
					Debug.log("Clasificacion no valida");
					mensaje += "Clasificacion " + tipoClasificacion
							+ "No existe\n";
				} else {
					if (vigencia
							&& clasificacion.get(0).getString("state")
									.equalsIgnoreCase("I")) {
						mensaje += "Clasificacion " + tipoClasificacion
								+ "No vigente\n";
					}
				}
			} else {
				clasificacion = delegator.findByAnd("Enumeration", UtilMisc
						.toMap("sequenceId", clavePres.get(i - 1),
								"enumTypeId", tipoClasificacion));
				if (clasificacion.isEmpty()) {
					Debug.log("Clasificacion no valida");
					mensaje += "Clasificacion " + tipoClasificacion
							+ " No existe\n";
				} else {
					if (vigencia) {
						mensaje += validaVigenciaEnum(mensaje,
								tipoClasificacion, clasificacion.get(0), fecha);
					}
				}
			}
		}
		return mensaje;
	}

	public static String validaClave(Delegator delegator,
			GenericValue estructura, List<String> clavePres)
			throws GenericEntityException {
		return validaClaveVigencia(delegator, estructura, clavePres, false,
				null);
	}

	public static String validaVigenciaEnum(String mensaje,
			String tipoClasificacion, GenericValue enumeration, Date fechaTrans)
			throws GenericEntityException {
		java.sql.Date fechaInicio = enumeration.getDate("fechaInicio");
		java.sql.Date fechaFin = enumeration.getDate("fechaFin");
		if (!fechaInicio.before(fechaTrans) || !fechaFin.after(fechaTrans)) {
			Debug.log("Clasificacion no vigente");
			mensaje += "Clasificacion " + tipoClasificacion + " No vigente\n";
		}
		return mensaje;
	}

	public static String buscaClasifAdministrativa(Delegator delegator,
			List<String> clavePres, GenericValue estructura) {
		for (int i = 1; i <= clavePres.size(); i++) {
			String tipoClasificacion = estructura
					.getString("clasificacion" + i);
			if (tipoClasificacion.equalsIgnoreCase("CL_ADMINISTRATIVA")) {
				return clavePres.get(i - 1);
			}
		}
		return "";
	}

	public static boolean altaClavePresupuestal(Delegator delegator,
			String clave, String tipo, List<String> clasificaciones)
			throws GenericEntityException {
		if (existenciaClave(delegator, clave)) {
			Debug.log("Clave ya existe");
			return false;
		}
		GenericValue clavePres = GenericValue.create(delegator
				.getModelEntity("ClavePresupuestal"));
		clavePres.set("clavePresupuestal", clave);
		clavePres.set("tipo", tipo);
		int i = 1;
		for (String clasificacion : clasificaciones) {
			String campo = "clasificacion" + i;
			clavePres.set(campo, clasificacion);
			i++;
		}
		delegator.createOrStore(clavePres);
		return true;
	}

	public static boolean existenciaClave(Delegator delegator, String clave)
			throws GenericEntityException {
		GenericValue generic = delegator.findByPrimaryKey("ClavePresupuestal",
				UtilMisc.toMap("clavePresupuestal", clave));
		if (generic == null) {
			return false;
		}
		return true;
	}

	public static boolean controlPresupuestal(Delegator delegator,
			String idTipoDoc, String clave, Date fecha, BigDecimal monto,
			String ciclo) throws GenericEntityException {
		Debug.log("Entra control presupuestal");

		GenericValue generic = obtenDocumento(delegator, idTipoDoc);
		String mComparativo = generic.getString("mComparativo");
		String mEjecutar1 = generic.getString("mEjecutar1");
		String mEjecutar2 = generic.getString("mEjecutar2");
		if (mComparativo != null && !mComparativo.isEmpty()) {
			if (comparaMontos(delegator, clave, mComparativo, fecha, monto,
					generic.getString("comparacion"))) {
				restaMonto(delegator, clave, mComparativo, fecha, monto);

			} else {
				Debug.log("Saldo insuficiente(momentos)");
				return false;
			}
		}
		if (mEjecutar1 != null && !mEjecutar1.isEmpty()) {
			sumaMonto(delegator, clave, mEjecutar1, fecha, monto, ciclo);
		}

		if (mEjecutar2 != null && !mEjecutar2.isEmpty()) {
			sumaMonto(delegator, clave, mEjecutar2, fecha, monto, ciclo);
		}
		return true;
	}

	public static String obtenMesString(int mes) {
		mes++;
		return mes < 10 ? "0" + new Integer(mes).toString() : new Integer(mes)
				.toString();
	};

	@SuppressWarnings("deprecation")
	public static void sumaMonto(Delegator delegator, String clave,
			String momento, Date fecha, BigDecimal monto, String ciclo)
			throws GenericEntityException {
		String mes = obtenMesString(fecha.getMonth());
		List<GenericValue> generics = delegator.findByAnd(
				"ControlPresupuestal", UtilMisc.toMap("clavePresupuestal",
						clave, "mesId", mes, "momentoId", momento));
		if (!generics.isEmpty()) {
			Debug.log("Si existe controlPres");
			GenericValue generic = generics.get(0);
			Debug.log("CP.- " + generic.getString("idSecuencia"));
			Debug.log("Monto.- " + generic.getBigDecimal("monto"));
			BigDecimal original = generic.getBigDecimal("monto");
			generic.set("monto", original.add(monto));
			Debug.log("monto.- " + generic.getString("monto"));
			delegator.createOrStore(generic);
		} else {
			GenericValue generic = GenericValue.create(delegator
					.getModelEntity("ControlPresupuestal"));
			generic.setNextSeqId();
			generic.set("clavePresupuestal", clave);
			generic.set("mesId", mes);
			generic.set("momentoId", momento);
			generic.set("monto", monto);
			generic.set("ciclo", ciclo);
			generic.create();
		}
	}

	@SuppressWarnings("deprecation")
	public static void restaMonto(Delegator delegator, String clave,
			String momento, Date fecha, BigDecimal monto)
			throws GenericEntityException {
		String mes = obtenMesString(fecha.getMonth());
		List<GenericValue> generics = delegator.findByAnd(
				"ControlPresupuestal", UtilMisc.toMap("clavePresupuestal",
						clave, "mesId", mes, "momentoId", momento));
		if (!generics.isEmpty()) {
			GenericValue generic = generics.get(0);
			BigDecimal original = generic.getBigDecimal("monto");
			generic.set("monto", original.subtract(monto));
			delegator.createOrStore(generic);
		} else {
			// No debe pasar esto.
		}
	}

	@SuppressWarnings("deprecation")
	public static boolean comparaMontos(Delegator delegator, String clave,
			String momento, Date fecha, BigDecimal monto, String comparacion)
			throws GenericEntityException {
		String mes = obtenMesString(fecha.getMonth());
		List<GenericValue> generics = delegator.findByAnd(
				"ControlPresupuestal", UtilMisc.toMap("clavePresupuestal",
						clave, "mesId", mes, "momentoId", momento));
		if (!generics.isEmpty()) {
			GenericValue generic = generics.get(0);
			BigDecimal original = generic.getBigDecimal("monto");
			int resultado = original.compareTo(monto); // returns (-1 if a < b),
														// (0 if a == b), (1 if
														// a > b)

			if (comparacion.equalsIgnoreCase("<")) {
				if (resultado == -1) {
					return true;
				}
			} else if (comparacion.equalsIgnoreCase("<=")) {
				if (resultado == -1 || resultado == 0) {
					return true;
				}
			} else if (comparacion.equalsIgnoreCase(">=")) {
				if (resultado == 1 || resultado == 0) {
					return true;
				}
			} else if (comparacion.equalsIgnoreCase(">")) {
				if (resultado == 1) {
					return true;
				}
			}
		} else {
			// No debe pasar esto.
		}
		return false;
	}

	public static Map<String, String> cuentasIngreso(Delegator delegator,
			String idTipoDocumento, String cri) throws GenericEntityException,
			ServiceException {
		Debug.log("cuentas Ingreso");
		Map<String, String> cuentas = new HashMap<String, String>();
		GenericValue tipoDocumento = obtenDocumento(delegator, idTipoDocumento);

		GenericValue evento = obtenEvento(delegator,
				tipoDocumento.getString("acctgTransTypeId"));

		cuentas.put("GlFiscalTypePresupuesto",
				evento.getString("glFiscalTypeIdPres"));
		cuentas.put("GlFiscalTypeContable",
				evento.getString("glFiscalTypeIdCont"));
		cuentas.put("Cuenta Cargo Presupuesto", evento.getString("cuentaCargo"));
		cuentas.put("Cuenta Abono Presupuesto", evento.getString("cuentaAbono"));
		cuentas.put("tipoPolizaP", evento.getString("tipoPolizaIdPres"));
		cuentas.put("tipoPolizaC", evento.getString("tipoPolizaIdCont"));
		cuentas.put("referencia", evento.getString("referencia"));
		cuentas.put("Cuenta Cargo Patrimnonio", evento.getString("cuentaCargoPatri"));
		cuentas.put("Cuenta Abono Patrimnonio", evento.getString("cuentaAbonoPatri"));

		if (evento.getString("referencia").equalsIgnoreCase("M")) {
			Debug.log("Referencia = M");
			List<GenericValue> matriz = delegator.findByAnd(
					"MatrizIngreso",
					UtilMisc.toMap("cri", cri, "matrizId",
							evento.getString("tipoMatriz")));
			if (matriz.isEmpty()) {
				Debug.log("Error, elemento en Matriz no existe");
				throw new ServiceException(
						"Error, elemento en Matriz no existe [" + cri + "]");
			}

			cuentas.put("Cuenta Cargo Contable",
					matriz.get(0).getString("cargo"));
			cuentas.put("Cuenta Abono Contable",
					matriz.get(0).getString("abono"));
		} else {
			// miniGuia.getReferencia().equalsIgnoreCase("N")
			Debug.log("Referencia = N");
		}
		return cuentas;
	}

	public static Map<String, String> cuentasEgreso(Delegator delegator,
			String idTipoDocumento, String cog, String tipoGasto)
			throws GenericEntityException {
		Debug.log("cuentas Egreso");
		Map<String, String> cuentas = new HashMap<String, String>();
		GenericValue tipoDocumento = obtenDocumento(delegator, idTipoDocumento);

		GenericValue evento = obtenEvento(delegator,
				tipoDocumento.getString("acctgTransTypeId"));

		cuentas.put("GlFiscalTypePresupuesto",
				evento.getString("glFiscalTypeIdPres"));
		cuentas.put("GlFiscalTypeContable",
				evento.getString("glFiscalTypeIdCont"));
		cuentas.put("Cuenta Cargo Presupuesto", evento.getString("cuentaCargo"));
		cuentas.put("Cuenta Abono Presupuesto", evento.getString("cuentaAbono"));
		cuentas.put("tipoPolizaP", evento.getString("tipoPolizaIdPres"));
		cuentas.put("tipoPolizaC", evento.getString("tipoPolizaIdCont"));
		cuentas.put("referencia", evento.getString("referencia"));
		cuentas.put("Cuenta Cargo Patrimnonio", evento.getString("cuentaCargoPatri"));
		cuentas.put("Cuenta Abono Patrimnonio", evento.getString("cuentaAbonoPatri"));
		cuentas.put("Cuenta Cargo Obra", evento.getString("cuentaCargoObra"));
		cuentas.put("Cuenta Abono Obra", evento.getString("cuentaAbonoObra"));

		if (evento.getString("referencia").equalsIgnoreCase("M")) {
			Debug.log("Referencia = M");
			Debug.log("cog" + cog);
			Debug.log("tipoGasto" + tipoGasto);
			Debug.log("tipoMatriz" + evento.getString("tipoMatriz"));
			List<GenericValue> matriz = delegator.findByAnd("MatrizEgreso",
					UtilMisc.toMap("cog", cog, "tipoGasto", tipoGasto,
							"matrizId", evento.getString("tipoMatriz")));
			if (matriz.isEmpty()) {
				Debug.log("Error, elemento en Matriz no existe");
				throw new GenericEntityException("Error, elemento [" + cog
						+ "  " + tipoGasto + "]en Matriz no existe");
			}

			cuentas.put("Cuenta Cargo Contable",
					matriz.get(0).getString("cargo"));
			cuentas.put("Cuenta Abono Contable",
					matriz.get(0).getString("abono"));
		} else {
			// miniGuia.getReferencia().equalsIgnoreCase("N")
			Debug.log("Referencia = N");
		}
		Debug.log("LAS CUENTAS: " + cuentas);
		return cuentas;
	}

	@SuppressWarnings({ "rawtypes" })
	public static List<GenericValue> creaTransaccion(Delegator delegator,
			String modulo, Timestamp fechaRegistro, Timestamp fechaContable,
			String tipoDocumento, String usuario, String organizationId,
			String partyId, GenericValue estructura, BigDecimal monto,
			String agrupador, String secuencia, List<String> clavePres,
			Map<String, String> cuentas, String currency, String tipoClave,
			String clavePresupuestal, String invoiceId, String paymentId,
			String shipmentId, String receiptId, String workEffortId,
			String roleTypeId, String auxiliar, String producto,
			String agrupadorAnterior, String operacionNoRegistrada,
			String description, String tipoTransaccion, String orderId,
			String inventoryItemId, String physicalInventoryId, String fixedAssetId, String contratoId)
			throws GenericEntityException, RepositoryException {
		
		Debug.log("comienza la creacion de la transaccion");
		List<String> poliza = null;

		// //////////////////////////////////////////////////
		// Ya no se usan los parametros modulo y secuencia.//
		// //////////////////////////////////////////////////

		// en caso de que no se tenga la Entidad Contable, se busca
		if (partyId == null || partyId.isEmpty())
			partyId = obtenPadre(delegator, organizationId);

		// Se actualiza la clave presupuestal del agrupador anterior
		if (agrupadorAnterior != null && !agrupadorAnterior.isEmpty()) {
			String tipoPolizaId = UtilAccounting.buscarIdPolizaXDocu(
					tipoDocumento, delegator);
			EntityCondition condicionesAgrupa = EntityCondition.makeCondition(
					EntityOperator.AND, EntityCondition.makeCondition(
							"agrupador", EntityOperator.EQUALS,
							agrupadorAnterior), EntityCondition.makeCondition(
							"clavePresupuestal", EntityOperator.EQUALS,
							clavePresupuestal), EntityCondition
							.makeCondition("tipoPolizaId",
									EntityOperator.EQUALS, tipoPolizaId));
			// Buscar la clave con el agrupador anterior
			List<GenericValue> acctgTrans = delegator.findByCondition(
					"AcctgTrans", condicionesAgrupa, null, null);
			if (acctgTrans != null && !acctgTrans.isEmpty()) {
				for (GenericValue trans : acctgTrans) {
					trans.set("claveContabilizada", "Y");
					delegator.createOrStore(trans);
				}
			}
		}

		// AcctgTrans
		GenericValue trans = GenericValue.create(delegator
				.getModelEntity("AcctgTrans"));
		trans.setNextSeqId();

		// se obtienen los momentos contables afectadodos positivamente
		List<String> momentos = obtenMomentos(delegator, tipoDocumento);

		List<GenericValue> transacciones = FastList.newInstance();
		trans.set("transactionDate", fechaRegistro);
		trans.set("postedDate", fechaContable);
		trans.set("description", description);
		trans.set("isPosted", "Y");
		
		trans.set("createdByUserLogin", usuario);
		trans.set("lastModifiedByUserLogin", usuario);
		trans.set("partyId", partyId);
		trans.set("organizationPartyId", organizationId);
		trans.set("postedAmount", monto);
		trans.set("idTipoDoc", tipoDocumento);
		trans.set("idTipoClave", tipoClave);
		trans.set("clavePresupuestal", clavePresupuestal);
		trans.set("invoiceId", invoiceId);
		trans.set("paymentId", paymentId);
		trans.set("shipmentId", shipmentId);
		trans.set("receiptId", receiptId);
		trans.set("workEffortId", workEffortId);
		trans.set("roleTypeId", roleTypeId);
		trans.set("orderId", orderId);
		trans.set("claveContabilizada", "N");
		trans.set("inventoryItemId", inventoryItemId);
		trans.set("physicalInventoryId", physicalInventoryId);
		trans.set("fixedAssetId", fixedAssetId);
		trans.set("contratoId", contratoId);		
		
		// Se ingresa el tipo de transaccion
		if (tipoTransaccion == null)
			trans.set("acctgTransTypeId",
					obtenTipoTransDeDocumento(delegator, tipoDocumento));
		else
			trans.set("acctgTransTypeId", tipoTransaccion);
		
		// En caso de que se tengan catalogos contables se asignan
		if (auxiliar != null)
			trans.set("theirPartyId", auxiliar);
		if (producto != null)
			trans.set("productId", producto);

		// se impactan los momento contables obtenidos anteriormente en caso de
		// que existan
		Iterator mom = momentos.iterator();
		int count = 1;
		while (mom.hasNext()) {
			String mom1 = (String) mom.next();

			if (count == 1 && mom1.length() > 0) {
				trans.set("momentoId1", mom1);
			} else if (count == 2 && mom1.length() > 0) {
				trans.set("momentoId2", mom1);
			}
			count++;
		}

		String idTrans = trans.getString("acctgTransId");
		
		Debug.log("JRRMtipoPolizaTrans.- "+operacionNoRegistrada);

		boolean esReferenciaM = cuentas.get("referencia").equals("M")
				|| cuentas.get("referencia").equals("G") ? true : false;
		
		if (esReferenciaM) {
			trans.set("glFiscalTypeId", cuentas.get("GlFiscalTypeContable"));
			
			if (cuentas.get("Cuenta Cargo Contable") != null) {
				if (auxiliar != null || producto != null) {
					actualizaCatalogos(delegator, auxiliar, producto, monto,
							organizationId, tipoDocumento,
							cuentas.get("Cuenta Cargo Contable"),
							cuentas.get("Cuenta Abono Contable"), fechaContable);
				}
			}else if(cuentas.get("Cuenta Cargo Patrimnonio") != null){
				if (auxiliar != null || producto != null) {
					actualizaCatalogos(delegator, auxiliar, producto, monto,
							organizationId, tipoDocumento,
							cuentas.get("Cuenta Cargo Patrimnonio"),
							cuentas.get("Cuenta Abono Patrimnonio"),
							fechaContable);
				}
			}
			
		}
		else
			trans.set("glFiscalTypeId", cuentas.get("GlFiscalTypePresupuesto"));

		// En caso de que se tenga el agrupador se asigna, sino se crea.
		if (agrupador != null && !agrupador.isEmpty()) {
			trans.set("agrupador", agrupador);
			Debug.log("referencia: " + cuentas.get("referencia"));

			if (operacionNoRegistrada != null) {
				Debug.log("Tiene agrupador y es una operacionNoRegistrada");
				trans.set("tipoPolizaId", operacionNoRegistrada);
				trans.set(
						"consecutivo",
						obtenConsecutivo(delegator, agrupador,
								organizationId,operacionNoRegistrada));
			} else {

				if (esReferenciaM) {
					Debug.log("Entra con referencia M o G");
					trans.set("tipoPolizaId", cuentas.get("tipoPolizaC"));
					trans.set(
							"consecutivo",
							obtenConsecutivo(delegator, agrupador,
									organizationId, cuentas.get("tipoPolizaC")));
					
				} else {
					if(cuentas.get("referencia").equals("N") && cuentas.get("tipoPolizaC") != null)
						trans.set("tipoPolizaId", cuentas.get("tipoPolizaC"));										
					else
						trans.set("tipoPolizaId", cuentas.get("tipoPolizaP"));					
					Debug.log("JRRM agrupador.- " + agrupador);
					Debug.log("JRRM organizationId.- " + organizationId);
					Debug.log("JRRM tipoPolizaP.- "
							+ cuentas.get("tipoPolizaP"));
					trans.set(
							"consecutivo",
							obtenConsecutivo(delegator, agrupador,
									organizationId, cuentas.get("tipoPolizaP")));
				}
			}
		} else {
			poliza = creaIdPoliza(delegator, tipoDocumento, fechaContable,
					organizationId, operacionNoRegistrada);
			trans.set("agrupador", poliza.get(0));
			trans.set("consecutivo", Long.parseLong(poliza.get(1)));
			trans.set("tipoPolizaId", poliza.get(2));
		}

		// History
		Debug.log("Busca periodos");
		List<GenericValue> periodos = obtenPeriodos(delegator, organizationId,
				fechaContable);

		// Si existen cuentas 8 dentro del evento, se procede a realizar una
		// afectacion presupuestal
		if (cuentas.get("Cuenta Cargo Presupuesto") != null) {
			Debug.log("Cuenta Presupuestal");
			trans.set("acctgTransId", "P-" + idTrans);

			// se crea Acctgtrans y se realiza un clone para retornar la
			// informacion
			delegator.createOrStore(trans);
			GenericValue trans2 = (GenericValue) trans.clone();
			transacciones.add(trans2);
			Debug.log("commit CCP");

			// AcctgTransEntry
			GenericValue acctgEntryD = generaAcctgTransEntry(delegator, trans,
					organizationId, partyId, "00001", "D",
					cuentas.get("Cuenta Cargo Presupuesto"), currency,
					auxiliar, producto, tipoDocumento, esReferenciaM, 
					inventoryItemId);

			// Tags seteados.
			acctgEntryD = asignaTags(
					delegator,
					acctgEntryD,
					buscaClasifAdministrativa(delegator, clavePres, estructura),
					clavePres, estructura);
			delegator.createOrStore(acctgEntryD);

			actualizaGlAccountOrganization(delegator, monto,
					cuentas.get("Cuenta Cargo Presupuesto"), organizationId,
					partyId, "DEBIT");

			// GlAccountHistory
			Debug.log("Busca histories");
			List<GenericValue> histories = actualizaGlAccountHistories(
					delegator, periodos,
					cuentas.get("Cuenta Cargo Presupuesto"), monto, "Debit");

			Debug.log("Se impactan las histories regresadas");
			for (GenericValue history : histories) {
				delegator.createOrStore(history);
			}

			Debug.log("commit CAP");
			GenericValue acctgEntryC = generaAcctgTransEntry(delegator, trans,
					organizationId, partyId, "00002", "C",
					cuentas.get("Cuenta Abono Presupuesto"), currency,
					auxiliar, producto, tipoDocumento, esReferenciaM, 
					inventoryItemId);
			// Tags seteados.
			acctgEntryC = asignaTags(
					delegator,
					acctgEntryC,
					buscaClasifAdministrativa(delegator, clavePres, estructura),
					clavePres, estructura);
			delegator.createOrStore(acctgEntryC);

			Debug.log("commit GlAO");
			actualizaGlAccountOrganization(delegator, monto,
					cuentas.get("Cuenta Abono Presupuesto"), organizationId,
					partyId, "CREDIT");

			// GlAccountHistory
			Debug.log("Busca histories");
			histories = actualizaGlAccountHistories(delegator, periodos,
					cuentas.get("Cuenta Abono Presupuesto"), monto, "Credit");

			Debug.log("Se impactan las histories regresadas");
			for (GenericValue history : histories) {
				delegator.createOrStore(history);
			}

		}

		// Si existe referencia a la matriz dentro del evento, se procede a
		// realizar una afectacion contable
		if (cuentas.get("Cuenta Cargo Contable") != null) {

			Debug.log("Cuenta Contable");
			trans.set("acctgTransId", "C-" + idTrans);

			delegator.createOrStore(trans);
			transacciones.add(trans);

			// AcctgTransEntry
			Debug.log("commit CCC");
			GenericValue acctgEntryD = generaAcctgTransEntry(delegator, trans,
					organizationId, partyId, "00001", "D",
					cuentas.get("Cuenta Cargo Contable"), currency, auxiliar,
					producto, tipoDocumento, esReferenciaM,
					inventoryItemId);

			// Tags seteados.
			acctgEntryD = asignaTags(
					delegator,
					acctgEntryD,
					buscaClasifAdministrativa(delegator, clavePres, estructura),
					clavePres, estructura);
			delegator.createOrStore(acctgEntryD);

			actualizaGlAccountOrganization(delegator, monto,
					cuentas.get("Cuenta Cargo Contable"), organizationId,
					partyId, "DEBIT");

			// GlAccountHistory
			Debug.log("Busca histories");
			List<GenericValue> histories = actualizaGlAccountHistories(
					delegator, periodos, cuentas.get("Cuenta Cargo Contable"),
					monto, "Debit");

			Debug.log("Se impactan las histories regresadas");
			for (GenericValue history : histories) {
				delegator.createOrStore(history);
			}

			Debug.log("commit CAC");
			GenericValue acctgEntryC = generaAcctgTransEntry(delegator, trans,
					organizationId, partyId, "00002", "C",
					cuentas.get("Cuenta Abono Contable"), currency, auxiliar,
					producto, tipoDocumento, esReferenciaM,
					inventoryItemId);
			// Tags seteados.
			acctgEntryC = asignaTags(
					delegator,
					acctgEntryC,
					buscaClasifAdministrativa(delegator, clavePres, estructura),
					clavePres, estructura);
			delegator.createOrStore(acctgEntryC);

			Debug.log("commit GlAO");
			actualizaGlAccountOrganization(delegator, monto,
					cuentas.get("Cuenta Abono Contable"), organizationId,
					partyId, "CREDIT");

			// GlAccountHistory
			Debug.log("Busca histories");
			histories = actualizaGlAccountHistories(delegator, periodos,
					cuentas.get("Cuenta Abono Contable"), monto, "Credit");

			Debug.log("Se impactan las histories regresadas");
			for (GenericValue history : histories) {
				delegator.createOrStore(history);
			}
		}
		
		// Si existe referencia al patrimonio
		if (cuentas.get("Cuenta Cargo Patrimnonio") != null) {

			Debug.log("Cuenta Cargo Patrimnonio");
			trans.set("acctgTransId", "PT-" + idTrans);

			delegator.createOrStore(trans);
			transacciones.add(trans);

			// AcctgTransEntry
			Debug.log("commit CCC");
			GenericValue acctgEntryD = generaAcctgTransEntry(delegator, trans,
					organizationId, partyId, "00001", "D",
					cuentas.get("Cuenta Cargo Patrimnonio"), currency, auxiliar,
					producto, tipoDocumento, esReferenciaM,
					inventoryItemId);

			// Tags seteados.
			acctgEntryD = asignaTags(
					delegator,
					acctgEntryD,
					buscaClasifAdministrativa(delegator, clavePres, estructura),
					clavePres, estructura);
			delegator.createOrStore(acctgEntryD);

			actualizaGlAccountOrganization(delegator, monto,
					cuentas.get("Cuenta Cargo Patrimnonio"), organizationId,
					partyId, "DEBIT");

			// GlAccountHistory
			Debug.log("Busca histories");
			List<GenericValue> histories = actualizaGlAccountHistories(
					delegator, periodos, cuentas.get("Cuenta Cargo Patrimnonio"),
					monto, "Debit");

			Debug.log("Se impactan las histories regresadas");
			for (GenericValue history : histories) {
				delegator.createOrStore(history);
			}

			Debug.log("commit CAC");
			GenericValue acctgEntryC = generaAcctgTransEntry(delegator, trans,
					organizationId, partyId, "00002", "C",
					cuentas.get("Cuenta Abono Patrimnonio"), currency, auxiliar,
					producto, tipoDocumento, esReferenciaM,
					inventoryItemId);
			// Tags seteados.
			acctgEntryC = asignaTags(
					delegator,
					acctgEntryC,
					buscaClasifAdministrativa(delegator, clavePres, estructura),
					clavePres, estructura);
			delegator.createOrStore(acctgEntryC);

			Debug.log("commit GlAO");
			actualizaGlAccountOrganization(delegator, monto,
					cuentas.get("Cuenta Abono Patrimnonio"), organizationId,
					partyId, "CREDIT");

			// GlAccountHistory
			Debug.log("Busca histories");
			histories = actualizaGlAccountHistories(delegator, periodos,
					cuentas.get("Cuenta Abono Patrimnonio"), monto, "Credit");

			Debug.log("Se impactan las histories regresadas");
			for (GenericValue history : histories) {
				delegator.createOrStore(history);
			}
		}
		
		// Si es un documento de obra
		if (cuentas.get("Cuenta Cargo Obra") != null) {

			Debug.log("Cuenta Cargo Obra");
			trans.set("acctgTransId", "O-" + idTrans);

			delegator.createOrStore(trans);
			transacciones.add(trans);

			// AcctgTransEntry
			Debug.log("commit CCC");
			GenericValue acctgEntryD = generaAcctgTransEntry(delegator, trans,
					organizationId, partyId, "00001", "D",
					cuentas.get("Cuenta Cargo Obra"), currency,
					auxiliar, producto, tipoDocumento, esReferenciaM,
					inventoryItemId);

			// Tags seteados.
			acctgEntryD = asignaTags(
					delegator,
					acctgEntryD,
					buscaClasifAdministrativa(delegator, clavePres, estructura),
					clavePres, estructura);
			delegator.createOrStore(acctgEntryD);

			actualizaGlAccountOrganization(delegator, monto,
					cuentas.get("Cuenta Cargo Obra"), organizationId,
					partyId, "DEBIT");

			// GlAccountHistory
			Debug.log("Busca histories");
			List<GenericValue> histories = actualizaGlAccountHistories(
					delegator, periodos,
					cuentas.get("Cuenta Cargo Obra"), monto, "Debit");

			Debug.log("Se impactan las histories regresadas");
			for (GenericValue history : histories) {
				delegator.createOrStore(history);
			}

			Debug.log("commit CAC");
			GenericValue acctgEntryC = generaAcctgTransEntry(delegator, trans,
					organizationId, partyId, "00002", "C",
					cuentas.get("Cuenta Abono Obra"), currency,
					auxiliar, producto, tipoDocumento, esReferenciaM,
					inventoryItemId);
			// Tags seteados.
			acctgEntryC = asignaTags(
					delegator,
					acctgEntryC,
					buscaClasifAdministrativa(delegator, clavePres, estructura),
					clavePres, estructura);
			delegator.createOrStore(acctgEntryC);

			Debug.log("commit GlAO");
			actualizaGlAccountOrganization(delegator, monto,
					cuentas.get("Cuenta Abono Obra"), organizationId,
					partyId, "CREDIT");

			// GlAccountHistory
			Debug.log("Busca histories");
			histories = actualizaGlAccountHistories(delegator, periodos,
					cuentas.get("Cuenta Abono Obra"), monto, "Credit");

			Debug.log("Se impactan las histories regresadas");
			for (GenericValue history : histories) {
				delegator.createOrStore(history);
			}
		}

		return transacciones;
	}
	
	public static void actualizaCatalogos(Delegator delegator, String auxiliar,
			String producto, BigDecimal monto, String organizationId,
			String idTipoDoc, String cuentaCargo, String cuentaAbono,
			Timestamp fechaContable) throws GenericEntityException {
		actualizaCatalogos(delegator, auxiliar, producto, monto,
				organizationId, idTipoDoc, cuentaCargo, cuentaAbono,
				fechaContable, null, null);
	}

	public static void actualizaCatalogos(Delegator delegator, String auxiliar,
			String producto, BigDecimal monto, String organizationId,
			String idTipoDoc, String cuentaCargo, String cuentaAbono,
			Timestamp fechaContable, String auxiliarAbono, String productoAbono)
			throws GenericEntityException {
		actualizaCatalogos(delegator, auxiliar, producto, monto,
				organizationId, idTipoDoc, cuentaCargo, cuentaAbono,
				fechaContable, auxiliarAbono, productoAbono,false);
	}
	
	public static void actualizaCatalogos(Delegator delegator, String auxiliar,
			String producto, BigDecimal monto, String organizationId,
			String idTipoDoc, String cuentaCargo, String cuentaAbono,
			Timestamp fechaContable, String auxiliarAbono, String productoAbono, boolean isReversa)
			throws GenericEntityException {

		Debug.log("ingresa a actualizar catalogos");
		Debug.log("Auxiliar1: " + auxiliar + " Auxiliar2: " + auxiliarAbono);
		Debug.log("Producto1: " + producto + " Producto2: " + productoAbono);

		// Se obtiene los auxiliares, dependiendo del evento.
		List<String> auxiliares = obtenAuxiliares(delegator, idTipoDoc);

		// Se pone el periodo segun la fecha contable.
		// Por definicion el periodo debe estar en el formato 01/MM/AAAA 12:00:00 a.m.
		
		Calendar date = Calendar.getInstance();
		date.setTime(fechaContable);
		date.set(Calendar.DAY_OF_MONTH,1);
		date.set(Calendar.HOUR, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		date.set(Calendar.AM_PM, Calendar.AM);
		
		Timestamp periodo = new Timestamp(date.getTimeInMillis());
		
		//Si es una reversa, se cambian los lugares de los tipos de auxiliares.
		String cargo;
		String abono;
		if(isReversa){
			cargo = auxiliares.get(1);
			abono = auxiliares.get(0);
		}else{
			cargo = auxiliares.get(0);
			abono = auxiliares.get(1);
		}

		// Cargo
		Debug.log("Cargo");
		//Se actualiza el catalogo, validando que el evento tenga asociado un catalogo en el Cargo y que este sea A 'Auxiliar' o P 'Producto'
		if (cargo != null
				&& (cargo.equalsIgnoreCase("A") || cargo.equalsIgnoreCase("P"))) {
			String naturaleza = naturaleza(delegator, cuentaCargo);
			String tipoCatalogo;
			String catalogoId;
			if (cargo.equalsIgnoreCase("A")) {
				tipoCatalogo = obtenRolAuxiliar(delegator, auxiliar);
				catalogoId = auxiliar;
			} else {
				tipoCatalogo = obtenRolProducto(delegator, producto);
				catalogoId = producto;
			}
			// Obtenemos Saldo del catalogo.
			GenericValue catalogo = obtenCatalogo(delegator, catalogoId,
					tipoCatalogo, organizationId, cargo, periodo);
			Debug.log("montoInicial del catalogo "+ catalogo
							.getBigDecimal("monto"));
			catalogo.set("monto",
					naturaleza.equalsIgnoreCase("DEBIT") ? catalogo
							.getBigDecimal("monto").add(monto) : catalogo
							.getBigDecimal("monto").subtract(monto));
			Debug.log(naturaleza.equalsIgnoreCase("DEBIT") ? "+" : "-" + monto);
			Debug.log("montoFinal del catalogo "+ catalogo
					.getBigDecimal("monto"));
			delegator.createOrStore(catalogo);
		}

		// Abono
		Debug.log("Abono");
		//Se actualiza el catalogo, validando que el evento tenga asociado un catalogo en el Abono y que este sea A 'Auxiliar' o P 'Producto'
		if (abono!=null
				&& (abono.equalsIgnoreCase("A") || abono.equalsIgnoreCase("P"))) {
			String naturaleza = naturaleza(delegator, cuentaAbono);
			String tipoCatalogo;
			String catalogoId;
			if (abono.equalsIgnoreCase("A")) {
				catalogoId = auxiliarAbono==null||auxiliarAbono.isEmpty() ? auxiliar
						: auxiliarAbono;
				tipoCatalogo = obtenRolAuxiliar(delegator, catalogoId);

			} else {
				catalogoId = productoAbono==null||productoAbono.isEmpty() ? producto
						: productoAbono;
				tipoCatalogo = obtenRolProducto(delegator, catalogoId);
			}
			// Obtenemos Saldo del catalogo.
			GenericValue catalogo = obtenCatalogo(delegator, catalogoId,
					tipoCatalogo, organizationId, abono, periodo);
			Debug.log("montoInicial del catalogo "+ catalogo
					.getBigDecimal("monto"));
			catalogo.set("monto",
					naturaleza.equalsIgnoreCase("CREDIT") ? catalogo
							.getBigDecimal("monto").add(monto) : catalogo
							.getBigDecimal("monto").subtract(monto));
			Debug.log(naturaleza.equalsIgnoreCase("CREDIT") ? "+" : "-" + monto);
			Debug.log("montoFinal del catalogo "+ catalogo
					.getBigDecimal("monto"));
			delegator.createOrStore(catalogo);
		}
	}
	
	public static GenericValue obtenCatalogo(Delegator delegator,
			String catalogoId, String tipoId, String partyId, String tipo,
			Timestamp periodo) throws GenericEntityException {
		List<GenericValue> catalogos = delegator.findByAnd("SaldoCatalogo",
				"catalogoId", catalogoId, "tipoId", tipoId, "partyId", partyId,
				"tipo", tipo, "periodo", periodo);
		GenericValue catalogo;
		if (catalogos.isEmpty()) {
			// Si esta vacio se crea el registro.
			catalogo = GenericValue.create(delegator
					.getModelEntity("SaldoCatalogo"));
			catalogo.set("secuenciaId",
					delegator.getNextSeqId("ControlAuxiliar"));
			catalogo.set("catalogoId", catalogoId);
			catalogo.set("tipoId", tipoId);
			catalogo.set("partyId", partyId);
			catalogo.set("tipo", tipo);
			catalogo.set("periodo", periodo);
			catalogo.set("monto", BigDecimal.ZERO);
		} else {
			catalogo = catalogos.get(0);
		}
		return catalogo;
	}
	
	public static String obtenRolAuxiliar(Delegator delegator,
			String auxiliarId) throws GenericEntityException {
		Debug.log("Entra a obtenRolAuxiliar");

		EntityCondition conditions = EntityCondition.makeCondition(
				EntityOperator.AND, EntityCondition.makeCondition("partyId",
						EntityOperator.EQUALS, auxiliarId));
		
		Debug.log("conditions - obtenerRolAuxiliar: " + conditions);
		
		List<GenericValue> roles = delegator.findByCondition(
				"Party", conditions, null, null);
		Debug.log("roles: " + roles);

		Debug.log("Se obtiene auxiliar: " + roles.get(0).getString("partyTypeId"));
		return roles.get(0).getString("partyTypeId");
	}
	
	public static String obtenRolProducto(Delegator delegator,
			String auxiliarId) throws GenericEntityException {

		GenericValue generic = delegator.findByPrimaryKey("Product",
				UtilMisc.toMap("productId", auxiliarId));

		Debug.log("Se obtiene producto: " + generic.getString("productTypeId"));
		return generic.getString("productTypeId");
	}

//	public static List<GenericValue> creaTransaccion(Delegator delegator,
//			String modulo, Timestamp fechaRegistro, Timestamp fechaContable,
//			String tipoDocumento, String usuario, String organizationId,
//			String partyId, GenericValue estructura, BigDecimal monto,
//			String agrupador, String secuencia, List<String> clavePres,
//			Map<String, String> cuentas, String currency, String tipoClave,
//			String clavePresupuestal, String invoiceId, String paymentId,
//			String shipmentId, String receiptId, String workEffortId,
//			String roleTypeId, String auxiliar, String producto,
//			String agrupadorAnterior, String description) throws GenericEntityException,
//			RepositoryException {
//		return creaTransaccion(delegator, modulo, fechaRegistro, fechaContable,
//				tipoDocumento, usuario, organizationId, partyId, estructura,
//				monto, agrupador, secuencia, clavePres, cuentas, currency,
//				tipoClave, clavePresupuestal, invoiceId, paymentId, shipmentId,
//				receiptId, workEffortId, roleTypeId, auxiliar, producto,
//				agrupadorAnterior, null, description, null);
//				
//
//	}
//	
//	public static List<GenericValue> creaTransaccion(Delegator delegator,
//			String modulo, Timestamp fechaRegistro, Timestamp fechaContable,
//			String tipoDocumento, String usuario, String organizationId,
//			String partyId, GenericValue estructura, BigDecimal monto,
//			String agrupador, String secuencia, List<String> clavePres,
//			Map<String, String> cuentas, String currency, String tipoClave,
//			String clavePresupuestal, String invoiceId, String paymentId,
//			String shipmentId, String receiptId, String workEffortId,
//			String roleTypeId, String auxiliar, String producto,
//			String agrupadorAnterior, String description, String orderId) throws GenericEntityException,
//			RepositoryException {
//		return creaTransaccion(delegator, modulo, fechaRegistro, fechaContable,
//				tipoDocumento, usuario, organizationId, partyId, estructura,
//				monto, agrupador, secuencia, clavePres, cuentas, currency,
//				tipoClave, clavePresupuestal, invoiceId, paymentId, shipmentId,
//				receiptId, workEffortId, roleTypeId, auxiliar, producto,
//				agrupadorAnterior, null, description, orderId);
//				
//
//	}
	
	public static List<String> obtenMomentos(Delegator delegator,
			String tipoDocumento) throws GenericEntityException {

		List<String> momentos = new ArrayList<String>();
		GenericValue generic = obtenDocumento(delegator, tipoDocumento);
		String mEjecutar1 = generic.getString("mEjecutar1");
		String mEjecutar2 = generic.getString("mEjecutar2");

		if (mEjecutar1 != null && !mEjecutar1.isEmpty())
			momentos.add(mEjecutar1);

		if (mEjecutar2 != null && !mEjecutar2.isEmpty())
			momentos.add(mEjecutar2);

		Debug.log("momentos a afectar: " + momentos);
		return momentos;
	}

	public static String obtenTipoTransDeDocumento(Delegator delegator,
			String tipoDocumento) throws GenericEntityException {
		Debug.log("Obten tipoTrans");

		GenericValue generic = obtenDocumento(delegator, tipoDocumento);
		return generic.getString("acctgTransTypeId");
	}

	public static List<GenericValue> obtenPeriodos(Delegator delegator,
			String organizationId, Date fecha) throws GenericEntityException {
		
		EntityCondition conditions = EntityCondition.makeCondition(
				EntityOperator.AND, EntityCondition.makeCondition(
						"organizationPartyId", EntityOperator.EQUALS,
						organizationId), EntityCondition.makeCondition(
						"organizationPartyId", EntityOperator.EQUALS,
						organizationId), EntityCondition.makeCondition(
						"isClosed", EntityOperator.EQUALS, "N"),
				EntityCondition.makeCondition("fromDate",
						EntityOperator.LESS_THAN_EQUAL_TO,
						// new Timestamp(fecha.getTime()));
						fecha), EntityCondition.makeCondition("thruDate",
						EntityOperator.GREATER_THAN_EQUAL_TO,
						// new Timestamp(fecha.getTime()));
						fecha));
				List<GenericValue> periodos = delegator.findByCondition("CustomTimePeriod", conditions, null, null);
		Debug.log("Periodos regresados.- " + periodos.size());
		return periodos;
	}

	public static GenericValue generaAcctgTransEntry(Delegator delegator,
			GenericValue transaccion, String organizacionPartyId,
			String partyId, String seqId, String flag, String cuenta,
			String currency, String auxiliar, String producto,
			String idTipoDoc, boolean esReferenciaM, String inventoryItemId)
			throws GenericEntityException {
		// C/D
		Debug.log("Empieza AcctgTransEntry " + cuenta);

		GenericValue acctgEntry = GenericValue.create(delegator
				.getModelEntity("AcctgTransEntry"));
		acctgEntry.set("acctgTransId", transaccion.get("acctgTransId"));
		acctgEntry.set("acctgTransEntrySeqId", seqId);
		acctgEntry.set("acctgTransEntryTypeId", "_NA_");
		acctgEntry.set("description", transaccion.get("description"));
		acctgEntry.set("glAccountId", cuenta);
		acctgEntry.set("organizationPartyId", organizacionPartyId);
		acctgEntry.set("partyId", partyId);
		acctgEntry.set("amount", transaccion.get("postedAmount"));
		acctgEntry.set("currencyUomId", currency);
		acctgEntry.set("debitCreditFlag", flag);
		acctgEntry.set("reconcileStatusId", "AES_NOT_RECONCILED");
		acctgEntry.set("inventoryItemId", inventoryItemId);
		
		if (esReferenciaM && !cuenta.startsWith("8")) {
			List<String> auxiliares = obtenAuxiliares(delegator, idTipoDoc);
			String auxiliarC = auxiliares.get(0), auxiliarA = auxiliares.get(1);

			if (flag.equals("D")) {
				if (auxiliar != null && auxiliarC.equals("A"))
					acctgEntry.set("theirPartyId", auxiliar);
				else if (producto != null && auxiliarC.equals("P"))
					acctgEntry.set("productId", producto);	
			}
			if (flag.equals("C")) {
				if (auxiliar != null && auxiliarA.equals("A"))
					acctgEntry.set("theirPartyId", auxiliar);
				else if (producto != null && auxiliarA.equals("P"))
					acctgEntry.set("productId", producto);
				}
		}

		return acctgEntry;
	}

	public static GenericValue asignaTags(Delegator delegator,
			GenericValue acctgEntry, String organizationId,
			List<String> clavePres, GenericValue estructura)
			throws GenericEntityException {
		Debug.log("Asigna Tags");
		int id = 1;
		for (String campo : clavePres) {
			if (!campo.equalsIgnoreCase(organizationId)) {
				String tipoClasificacion = estructura.getString("clasificacion"
						+ id);
				List<GenericValue> clasificacion = delegator.findByAnd(
						"Enumeration", UtilMisc.toMap("sequenceId", campo,
								"enumTypeId", tipoClasificacion));
				String field = "acctgTagEnumId" + id;
				acctgEntry.set(field, clasificacion.get(0).getString("enumId"));
			}
			id++;
		}
		return acctgEntry;
	}

	public static void actualizaGlAccountOrganization(Delegator delegator,
			BigDecimal monto, String cuenta, String organizacionPartyId,
			String partyId, String naturaleza) throws GenericEntityException {

		Debug.log("Empieza GlAccountOrganization " + cuenta);

		// Se busca la naturaleza de la cuenta.
		String naturalezaCuenta = naturaleza(delegator, cuenta);

		// GlAccountOrganization
		GenericValue glAccountOrganizationParty = delegator.findByPrimaryKey(
				"GlAccountOrganization", UtilMisc.toMap("glAccountId", cuenta,
						"organizationPartyId", partyId));

		GenericValue glAccountOrganization = delegator.findByPrimaryKey(
				"GlAccountOrganization", UtilMisc.toMap("glAccountId", cuenta,
						"organizationPartyId", organizacionPartyId));

		if (organizacionPartyId != partyId) {
			if (naturalezaCuenta.equalsIgnoreCase(naturaleza)) {
				if (glAccountOrganizationParty.getBigDecimal("postedBalance") == null) {
					glAccountOrganizationParty.set("postedBalance", monto);
				} else {
					BigDecimal montoActual = glAccountOrganizationParty
							.getBigDecimal("postedBalance");
					glAccountOrganizationParty.set("postedBalance",
							montoActual.add(monto));
				}

				if (glAccountOrganization.getBigDecimal("postedBalance") == null) {
					glAccountOrganization.set("postedBalance", monto);
				} else {
					BigDecimal montoActual = glAccountOrganization
							.getBigDecimal("postedBalance");
					glAccountOrganization.set("postedBalance",
							montoActual.add(monto));
				}
			} else {
				if (glAccountOrganizationParty.getBigDecimal("postedBalance") == null) {
					glAccountOrganizationParty.set("postedBalance",
							BigDecimal.ZERO);
				}

				BigDecimal montoActual = glAccountOrganizationParty
						.getBigDecimal("postedBalance");
				glAccountOrganizationParty.set("postedBalance",
						montoActual.subtract(monto));

				if (glAccountOrganization.getBigDecimal("postedBalance") == null) {
					glAccountOrganization.set("postedBalance", BigDecimal.ZERO);
				}

				montoActual = glAccountOrganization
						.getBigDecimal("postedBalance");
				glAccountOrganization.set("postedBalance",
						montoActual.subtract(monto));
			}

			delegator.createOrStore(glAccountOrganizationParty);
			delegator.createOrStore(glAccountOrganization);

		} else {
			if (naturalezaCuenta.equalsIgnoreCase(naturaleza)) {

				if (glAccountOrganization.getBigDecimal("postedBalance") == null) {
					glAccountOrganization.set("postedBalance", monto);
				} else {
					BigDecimal montoActual = glAccountOrganization
							.getBigDecimal("postedBalance");
					glAccountOrganization.set("postedBalance",
							montoActual.add(monto));
				}
			} else {
				if (glAccountOrganization.getBigDecimal("postedBalance") == null) {
					glAccountOrganization.set("postedBalance", BigDecimal.ZERO);
				}

				BigDecimal montoActual = glAccountOrganization
						.getBigDecimal("postedBalance");
				glAccountOrganization.set("postedBalance",
						montoActual.subtract(monto));
			}

			delegator.createOrStore(glAccountOrganization);
		}
	}

	public static String naturaleza(Delegator delegator, String cuenta)
			throws GenericEntityException {
		Debug.log("Naturaleza");

		GenericValue generic = obtenCuenta(delegator, cuenta);

		generic = delegator.findByPrimaryKey(
				"GlAccountClass",
				UtilMisc.toMap("glAccountClassId",
						generic.getString("glAccountClassId")));

		while (generic.get("parentClassId") != null) {
			generic = delegator.findByPrimaryKey(
					"GlAccountClass",
					UtilMisc.toMap("glAccountClassId",
							generic.getString("parentClassId")));
		}
		String naturaleza = generic.getString("glAccountClassId");
		Debug.log("Naturaleza de la cuenta.- " + naturaleza);
		return naturaleza;
	}

	public static List<GenericValue> actualizaGlAccountHistories(
			Delegator delegator, List<GenericValue> periodos, String cuenta,
			BigDecimal monto, String tipo) throws GenericEntityException {
		List<GenericValue> glAccountHistories = new ArrayList<GenericValue>();

		for (GenericValue periodo : periodos) {
			GenericValue glAccountHistory = delegator.findByPrimaryKey(
					"GlAccountHistory", UtilMisc.toMap("glAccountId", cuenta,
							"organizationPartyId",
							periodo.getString("organizationPartyId"),
							"customTimePeriodId",
							periodo.getString("customTimePeriodId")));

			if (glAccountHistory == null) {
				Debug.log("No existe History");
				glAccountHistory = GenericValue.create(delegator
						.getModelEntity("GlAccountHistory"));
				glAccountHistory.set("glAccountId", cuenta);
				glAccountHistory.set("organizationPartyId",
						periodo.getString("organizationPartyId"));
				glAccountHistory.set("customTimePeriodId",
						periodo.getString("customTimePeriodId"));
			}

			if (tipo.equalsIgnoreCase("Credit")) {
				if (glAccountHistory.getBigDecimal("postedCredits") == null) {
					glAccountHistory.set("postedCredits", monto);
				} else {
					BigDecimal montoActual = glAccountHistory
							.getBigDecimal("postedCredits");
					glAccountHistory.set("postedCredits",
							montoActual.add(monto));
				}
			} else {
				if (glAccountHistory.getBigDecimal("postedDebits") == null) {
					glAccountHistory.set("postedDebits", monto);
				} else {
					BigDecimal montoActual = glAccountHistory
							.getBigDecimal("postedDebits");
					glAccountHistory
							.set("postedDebits", montoActual.add(monto));
				}
			}
			glAccountHistories.add(glAccountHistory);
		}
		return glAccountHistories;
	}

	public static String buscaCri(Delegator delegator, GenericValue estructura,
			List<String> clavePres) throws GenericEntityException {

		return buscaClasificacionEconomica(delegator, estructura, clavePres,
				"CL_CRI");
	}

	public static String buscaCog(Delegator delegator, GenericValue estructura,
			List<String> clavePres) throws GenericEntityException {
		return buscaClasificacionEconomica(delegator, estructura, clavePres,
				"CL_COG");
	}

	public static String buscaTipoGasto(Delegator delegator,
			GenericValue estructura, List<String> clavePres)
			throws GenericEntityException {
		return buscaClasificacionEconomica(delegator, estructura, clavePres,
				"CL_TIPOGASTO");
	}

	public static String buscaClasificacionEconomica(Delegator delegator,
			GenericValue estructura, List<String> clavePres, String tipo)
			throws GenericEntityException {
		for (int i = 1; i <= clavePres.size(); i++) {
			String tipoClasificacion = estructura
					.getString("clasificacion" + i);
			if (tipoClasificacion.equalsIgnoreCase(tipo)) {
				return clavePres.get(i - 1);
			}
		}
		return "";
	}

	public static List<String> creaIdPoliza(Delegator delegator,
			String tipoDocumento, Timestamp fechaContable,
			String organizationId, String operacionNoRegistrada)
			throws GenericEntityException {

		Debug.log("Se comienza a crear poliza");
		GenericValue generic = null;
		List<GenericValue> agrupadores = null;

		generic = obtenDocumento(delegator, tipoDocumento);
		generic = obtenEvento(delegator, generic.getString("acctgTransTypeId"));
		String prefijo = obtenPrefijo(delegator, fechaContable);
		String tipoPoliza;

		if (operacionNoRegistrada != null) {
			tipoPoliza = operacionNoRegistrada;
		} else {
			tipoPoliza = generic.getString("tipoPolizaIdCont") != null ? generic
					.getString("tipoPolizaIdCont") : generic
					.getString("tipoPolizaIdPres");
		}

		agrupadores = obtenerListaConsecutivos(delegator, tipoPoliza,
				organizationId, prefijo);
		List<String> resultados = obtenAgrupadoryConsecutivo(delegator,
				agrupadores, prefijo);
		resultados.add(tipoPoliza);
		return resultados;

		/*
		 * EntityCondition conditions = EntityCondition.makeCondition(
		 * EntityOperator.AND, conditions1, EntityCondition.makeCondition(
		 * "organizationPartyId", EntityOperator.EQUALS, organizationId),
		 * EntityCondition.makeCondition( "agrupador", EntityOperator.LIKE,
		 * prefijo + "%"));
		 */
	}

	public static GenericValue obtenDocumento(Delegator delegator,
			String idTipoDoc) throws GenericEntityException {

		GenericValue generic = delegator.findByPrimaryKey("TipoDocumento",
				UtilMisc.toMap("idTipoDoc", idTipoDoc));

		Debug.log("Se obtiene doc: " + generic.getString("idTipoDoc"));
		return generic;
	}

	public static GenericValue obtenEvento(Delegator delegator, String eventoId)
			throws GenericEntityException {
		Debug.log("MMMMeventoId: " + eventoId);
		GenericValue generic = delegator.findByPrimaryKey("Evento",
				UtilMisc.toMap("acctgTransTypeId", eventoId));

		Debug.log("Se obtiene evento: " + generic.getString("acctgTransTypeId"));
		return generic;
	}

	public static String obtenPrefijo(Delegator delegator, Timestamp fecha) {

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(fecha.getTime());
		String year = Integer.toString(cal.get(Calendar.YEAR));
		String month = UtilFormatOut.formatPaddedNumber(
				(cal.get(Calendar.MONTH) + 1), 2);
		String prefijo = year + month;

		Debug.log("Se obtiene prefijo: " + prefijo);
		return prefijo;
	}

	public static List<GenericValue> obtenerListaConsecutivos(
			Delegator delegator, String tipoPoliza, String organizationId,
			String prefijo) throws GenericEntityException {

		EntityCondition conditions1 = EntityCondition.makeCondition(
				EntityOperator.AND, EntityCondition.makeCondition(
						"tipoPolizaId", EntityOperator.EQUALS, tipoPoliza),
				EntityCondition.makeCondition("organizationPartyId",
						EntityOperator.EQUALS, organizationId), EntityCondition
						.makeCondition("agrupador", EntityOperator.LIKE,
								prefijo + "%"));

		List<GenericValue> consecutivos = delegator.findByCondition(
				"AcctgTrans", conditions1, UtilMisc.toList("consecutivo"),
				UtilMisc.toList("consecutivo DESC"));

		Debug.log("Lista consecutivos: " + consecutivos);
		return consecutivos;

	}

	public static List<String> obtenAgrupadoryConsecutivo(Delegator delegator,
			List<GenericValue> consecutivos, String prefijo)
			throws GenericEntityException {
		String agrupadorId = null, consecutivo = null;
		List<String> idPoliza = new ArrayList<String>();

		if (consecutivos.size() != 0) {
			Iterator<GenericValue> polizaId = consecutivos.iterator();
			GenericValue poliza = polizaId.next();
			consecutivo = UtilFormatOut.formatPaddedNumber(
					(poliza.getLong("consecutivo") + 1), 4);
			agrupadorId = prefijo + " - " + consecutivo;
			idPoliza.add(agrupadorId);
			idPoliza.add(consecutivo);
		} else {
			agrupadorId = prefijo + " - 0001";
			idPoliza.add(agrupadorId);
			idPoliza.add("1");
		}

		Debug.log("Agr y Con: " + idPoliza);
		return idPoliza;
	}

	public static String consecutivoContable(Delegator delegator,
			List<GenericValue> consecutivos, String prefijo)
			throws GenericEntityException {
		String consecutivo = null;

		if (consecutivos.size() != 0) {
			Iterator<GenericValue> polizaId = consecutivos.iterator();
			GenericValue poliza = polizaId.next();
			consecutivo = UtilFormatOut.formatPaddedNumber(
					(poliza.getLong("consecutivo") + 1), 4);
		} else {
			consecutivo = "1";
		}

		Debug.log("consecutivo contable: " + consecutivo);
		return consecutivo;
	}

	public static GenericValue obtenerCvePresupuestal(Delegator delegator,
			String cvePresupuestalId) throws GenericEntityException {

		GenericValue cvePresupuestal = delegator.findByPrimaryKey(
				"ClavePresupuestal",
				UtilMisc.toMap("clavePresupuestal", cvePresupuestalId));

		return cvePresupuestal;

	}

	public static String buscarModulo(Delegator delegator, String idTipoDoc)
			throws GenericEntityException {

		GenericValue generic = obtenDocumento(delegator, idTipoDoc);

		generic = obtenModulo(delegator, generic.getString("moduloId"));

		String modulo = generic.getString("moduloId");
		Debug.log("Modulo.- " + modulo);
		return modulo;
	}

	public static GenericValue obtenModulo(Delegator delegator, String moduloId)
			throws GenericEntityException {

		GenericValue generic = delegator.findByPrimaryKey("Modulo",
				UtilMisc.toMap("moduloId", moduloId));

		return generic;
	}

	public static String obtenPadre(Delegator delegator, String organizationId)
			throws RepositoryException, GenericEntityException {
		String parentId = organizationId;
		do {
			GenericValue generic = obtenPartyGroup(delegator, organizationId);

			organizationId = generic.getString("Parent_id");
			if (organizationId != null)
				parentId = generic.getString("Parent_id");
		} while (organizationId != null);
		return parentId;
	}

	public static GenericValue obtenPartyGroup(Delegator delegator, String party)
			throws GenericEntityException {

		GenericValue generic = delegator.findByPrimaryKey("PartyGroup",
				UtilMisc.toMap("partyId", party));

		return generic;
	}

	public static Map<String, String> cuentasDiarias(Delegator delegator,
			String idTipoDocumento) throws GenericEntityException {

		Debug.log("cuentas Diarias");
		Map<String, String> cuentas = new HashMap<String, String>();

		GenericValue tipoDocumento = obtenDocumento(delegator, idTipoDocumento);

		GenericValue evento = obtenEvento(delegator,
				tipoDocumento.getString("acctgTransTypeId"));

		// cuentas.put("GlFiscalTypePresupuesto",
		// evento.getString("glFiscalTypeIdPres"));
		cuentas.put("GlFiscalTypeContable",
				evento.getString("glFiscalTypeIdCont"));
		// cuentas.put("Cuenta Cargo Presupuesto",
		// evento.getString("cuentaCargo"));
		// cuentas.put("Cuenta Abono Presupuesto",
		// evento.getString("cuentaAbono"));
		cuentas.put("Cuenta Cargo Contable",
				evento.getString("cuentaCargoPatri"));
		cuentas.put("Cuenta Abono Contable",
				evento.getString("cuentaAbonoPatri"));
		cuentas.put("referencia", evento.getString("referencia"));
		cuentas.put("tipoPolizaC", evento.getString("tipoPolizaIdCont"));

		// if (evento.getString("referencia").equalsIgnoreCase("G")) {
		// Debug.log("Referencia = G");
		// List<GenericValue> guia = new ArrayList<GenericValue>();
		// if (subconcepto != null) {
		// Debug.log("Subconcepto");
		// guia = delegator.findByAnd("GuiaContable", "idSubconcepto",
		// subconcepto);
		// } else {
		// Debug.log("Concepto");
		// guia = delegator.findByAnd("GuiaContable", "idConcepto",
		// concepto);
		// }
		//
		// if (guia.isEmpty()) {
		// Debug.log("Error, elemento en Guia no existe");
		// throw new GenericEntityException(
		// "Error, elemento en Guia no existe");
		// }
		//
		// if (guia.get(0).getString("rpCargo") != null
		// && !guia.get(0).getString("rpCargo").equalsIgnoreCase("0")) {
		// cuentas.put("Cuenta Cargo Presupuesto",
		// guia.get(0).getString("rpCargo"));
		// }
		// if (guia.get(0).getString("rpAbono") != null
		// && !guia.get(0).getString("rpAbono").equalsIgnoreCase("0")) {
		// cuentas.put("Cuenta Abono Presupuesto",
		// guia.get(0).getString("rpAbono"));
		// }
		// if (guia.get(0).getString("rcCargo") != null
		// && !guia.get(0).getString("rcCargo").equalsIgnoreCase("0")) {
		// cuentas.put("Cuenta Cargo Contable",
		// guia.get(0).getString("rcCargo"));
		// }
		// if (guia.get(0).getString("rcAbono") != null
		// && !guia.get(0).getString("rcAbono").equalsIgnoreCase("0")) {
		// cuentas.put("Cuenta Abono Contable",
		// guia.get(0).getString("rcAbono"));
		// }
		// }
		return cuentas;
	}

	public static boolean validarCuentasRegistro(Delegator delegator,
			String cuenta1, String cuenta2, String cuenta3, String cuenta4)
			throws GenericEntityException {
		GenericValue generic = null;

		if (cuenta1 != null) {

			generic = obtenCuenta(delegator, cuenta1);
			Debug.log("cuentaaaa " + generic.getString("tipoCuentaId"));
			if (!generic.getString("tipoCuentaId").equals("R"))
				return false;
		}

		if (cuenta2 != null) {

			generic = obtenCuenta(delegator, cuenta2);
			Debug.log("cuentaaaa " + generic.getString("tipoCuentaId"));
			if (!generic.getString("tipoCuentaId").equals("R"))
				return false;
		}

		if (cuenta3 != null) {

			generic = obtenCuenta(delegator, cuenta3);
			Debug.log("cuentaaaa " + generic.getString("tipoCuentaId"));

			if (!generic.getString("tipoCuentaId").equals("R"))
				return false;
		}

		if (cuenta4 != null) {

			generic = obtenCuenta(delegator, cuenta4);
			Debug.log("cuentaaaa " + generic.getString("tipoCuentaId"));

			if (!generic.getString("tipoCuentaId").equals("R"))
				return false;
		}

		return true;
	}

	public static GenericValue obtenCuenta(Delegator delegator, String cuenta)
			throws GenericEntityException {
		GenericValue generic = delegator.findByPrimaryKey("GlAccount",
				UtilMisc.toMap("glAccountId", cuenta));

		return generic;
	}

	public static List<String> obtenAuxiliares(Delegator delegator, String docId)
			throws GenericEntityException {
		GenericValue generic = null;
		List<String> auxiliares = new ArrayList<String>();

		generic = obtentTipoTransaccion(delegator,
				obtenTipoTransDeDocumento(delegator, docId));

		auxiliares.add(generic.getString("catalogoCargo"));
		auxiliares.add(generic.getString("catalogoAbono"));
		Debug.log("auxiliares.- " + auxiliares);
		return auxiliares;
	}

	public static GenericValue obtentTipoTransaccion(Delegator delegator,
			String tipoT) throws GenericEntityException {
		GenericValue generic = delegator.findByPrimaryKey("AcctgTransType",
				UtilMisc.toMap("acctgTransTypeId", tipoT));

		return generic;
	}

	public static String obtenConsecutivo(Delegator delegator,
			String agrupador, String organizationId, String tipoPoliza)
			throws GenericEntityException {
		Long consecutivo = null;
		EntityCondition conditions = EntityCondition.makeCondition(
				EntityOperator.AND, EntityCondition.makeCondition(
						"organizationPartyId", EntityOperator.EQUALS,
						organizationId), EntityCondition.makeCondition(
						"agrupador", EntityOperator.EQUALS, agrupador),
				EntityCondition.makeCondition("tipoPolizaId",
						EntityOperator.EQUALS, tipoPoliza));

		List<GenericValue> consecutivos = null;
		consecutivos = delegator.findByCondition("AcctgTrans", conditions,
				UtilMisc.toList("consecutivo"), null);

		Iterator<GenericValue> secuenciaId = consecutivos.iterator();
		GenericValue secuencia = secuenciaId.next();
		consecutivo = secuencia.getLong("consecutivo");

		return Long.toString(consecutivo);
	}
	
	/**
	 * autor JRRM
	 * @param delegator
	 * @param tipoDocumento
	 * @return lista de documentos anteriores
	 * @throws GenericEntityException
	 */
	public static List<String> buscaDocumentosAnteriores(Delegator delegator,
			String tipoDocumento) throws GenericEntityException {
		// Busca todos los tipos Documentos anteriores.
		List<String> documentos = new ArrayList<String>();
		do {
			documentos.add(tipoDocumento);
			GenericValue documento = obtenDocumento(delegator, tipoDocumento);
			tipoDocumento = documento.getString("predecesor");
			Debug.log("DOOOOdocumentos:" + documentos);
			Debug.log("DOOOOtipoDocumento:" + tipoDocumento);

		} while (tipoDocumento != null);
		return documentos;
	}
	
	/**
	 * autor JRRM
	 * @param delegator
	 * @param tipoDocumento
	 * @return id origen de documento
	 * @throws GenericEntityException
	 */
	public static String buscaDocumentoOrigen(Delegator delegator,
			String tipoDocumento) throws GenericEntityException {
		// Busca todos los tipos Documentos anteriores.
		List<String> documentos = buscaDocumentosAnteriores(delegator, tipoDocumento);		
		//El origen es el ultimo elemento de la lista.
		return documentos.get(documentos.size()-1);
	}

	public static List<String> creaOperacionNoRegistrada(Delegator delegator,
			String tipoDocumento, String clave, Timestamp fechaContable,
			String ciclo, String modulo, Timestamp fechaRegistro,
			String usuario, String organizationId, String partyId,
			GenericValue estructura, BigDecimal monto, String agrupador,
			String secuencia, List<String> clavePres,
			Map<String, String> cuentas, String currency, String tipoClave,
			String invoiceId, String paymentId, String shipmentId,
			String receiptId, String workEffortId, String roleTypeId,
			String auxiliar, String producto, String description, String bancoId, String cuentaBancariaId) throws GenericEntityException,
			RepositoryException, ServiceException {
		Debug.log("Entra a creaOperacionNoRegistrada");

		// Se obtiene el tipo de poliza del ultimo documento.
		Debug.log("MMMMtipoDocumento: " + tipoDocumento);
		GenericValue evento = obtenEvento(delegator, tipoDocumento);
		String tipoPoliza = evento.getString("tipoPolizaIdCont") != null ? evento
				.getString("tipoPolizaIdCont") : evento
				.getString("tipoPolizaIdPres");
		String tipoTransaccion = obtenTipoTransDeDocumento(delegator,
				tipoDocumento);
		Debug.log("JRRMtipoPoliza.- " + tipoPoliza);	
		List<String> valores = new ArrayList<String>();

		// Busca todos los tipos Documentos anteriores.
		List<String> documentos = buscaDocumentosAnteriores(delegator, tipoDocumento);

		// Se recorre la lista en reversa para hacer las transacciones
		// necesarias.
		for (int i = documentos.size() - 1; i >= 0; i--) {
			
			List<String> auxiliares = obtenAuxiliares(delegator,
					documentos.get(i));
            
			String moduloActual = buscarModulo(delegator, documentos.get(i));
			
			Debug.log("Auxiliares: " + auxiliares);
			Debug.log("Producto: " + producto);
			Debug.log("Auxiliar: " + auxiliar);
			if (auxiliares.isEmpty())
				agrupador = "No estan espicificados los catalogos auxiliares en el documento";

			if ((auxiliares.get(0).equals("A") || auxiliares.get(1).equals("A"))
					&& auxiliar == null)
				agrupador = "Es necesario ingresar el auxiliar";

			if ((auxiliares.get(0).equals("P") || auxiliares.get(1).equals("P"))
					&& producto == null)
				agrupador = "Es necesario ingresar el producto";
			

			if (agrupador == null || agrupador.length() <= 20) {
				cuentas = cuentasEgreso(delegator, documentos.get(i),
						buscaCog(delegator, estructura, clavePres),
						buscaTipoGasto(delegator, estructura, clavePres));

				Debug.log("Se verifica control presupuestal para el documento: "
						+ documentos.get(i));
				if (!controlPresupuestal(delegator, documentos.get(i), clave,
						fechaContable, monto, ciclo)) {
					throw new ServiceException(montoInsuficiencia(delegator, documentos.get(i), clave, fechaContable, monto));	
					//agrupador = "";
				}
				List<GenericValue> trans = creaTransaccion(delegator, modulo, 
						fechaRegistro, fechaContable, documentos.get(i), 
						usuario, organizationId, partyId, estructura, monto, 
						agrupador, secuencia, clavePres, cuentas, currency, 
						tipoClave, clave, invoiceId, paymentId, shipmentId, 
						receiptId, workEffortId, roleTypeId, auxiliar, 
						producto, agrupador, tipoPoliza, 
						description, tipoTransaccion, null, null, null, null, null);
						
				Debug.log("TTTTtrans: " + trans);				
				Iterator<GenericValue> listaTransIter = trans.iterator();
				while(listaTransIter.hasNext()) 
				{	GenericValue itemListaTrans = listaTransIter.next();
					Debug.log("Omar-itemListaTransMotor:" + itemListaTrans);
					if (agrupador == null || agrupador.length() <= 20) 
					{	agrupador = itemListaTrans.getString("agrupador");
						Debug.log("AAAAAagrupador: " + agrupador);
					}
					if(moduloActual.equalsIgnoreCase("TESORERIA_CXP_PAG") || moduloActual.equalsIgnoreCase("DEVOLUCION_DINERO"))
					{	if(cuentaBancariaId != null && itemListaTrans.getString("acctgTransId").contains("C-"))
						{	Debug.log("Actualiza saldo en cuentas");
							//Actualiza los saldos de las cuentas
							actualizaSaldoCuentasBancarias(delegator, cuentaBancariaId, moduloActual, monto,fechaContable);
														
							Debug.log("Genera registro en detalle de traspasos");
							//Genera registro en detalle de traspasos
							List<String> montoYTransaccionId  = registroDetalleCuentaBancaria(delegator, itemListaTrans, currency, moduloActual, bancoId, cuentaBancariaId, organizationId, usuario);
							Debug.log("Omar-montoYTransaccionId: " + montoYTransaccionId);
							String transaccionId = montoYTransaccionId.get(0);
							String montoTransaccion = montoYTransaccionId.get(1);
														
							Debug.log("Afecta el saldo de cuentas bancarias en acctgTransEntry");
							//Afecta el saldo de cuentas bancarias en acctgTransEntry
							actualizaCuentaBancariaAcctgTransEntry(delegator, moduloActual, cuentaBancariaId, transaccionId, montoTransaccion);
						}
						// agrupador = trans.get(0).getString("agrupador");
					}
				}
			}
		}
		
		valores.add(agrupador);
		valores.add(tipoPoliza);
		valores.add(tipoTransaccion);
		
		return valores;
	}

	public static GenericValue validaCuenta(Delegator delegator, String cuenta,
			String organizationPartyId) throws GenericEntityException {
		GenericValue generic = delegator.findByPrimaryKey(
				"GlAccountOrganization", UtilMisc.toMap("glAccountId", cuenta,
						"organizationPartyId", organizationPartyId));

		return generic;
	}
	
	public static List<GenericValue> creaTransaccionCierreCont (Delegator delegator,
			Timestamp fechaRegistro, Timestamp fechaContable, String tipoDocumento,
			String usuario, String organizationId, String partyId, BigDecimal monto,
			Map<String, String> cuentas, String currency, String auxiliar, String producto,
			String operacionNoRegistrada, String agrupador)
			throws GenericEntityException, RepositoryException
	{

		Debug.log("Comienza la creacion de la transaccion para el cierre contable");
		List<String> poliza = null;

		// En caso de que no se tenga la Entidad Contable, se busca
		if (partyId == null || partyId.isEmpty())
			partyId = obtenPadre(delegator, organizationId);

		// AcctgTrans
		GenericValue trans = GenericValue.create(delegator
				.getModelEntity("AcctgTrans"));
		trans.setNextSeqId();

		List<GenericValue> transacciones = FastList.newInstance();
		trans.set("transactionDate", fechaRegistro);
		trans.set("postedDate", fechaContable);
		trans.set("isPosted", "Y");
		trans.set("acctgTransTypeId", "PERIOD_CLOSING");
		trans.set("createdByUserLogin", usuario);
		trans.set("lastModifiedByUserLogin", usuario);
		trans.set("partyId", partyId);
		trans.set("organizationPartyId", organizationId);
		trans.set("postedAmount", monto);
		trans.set("idTipoDoc", tipoDocumento);

		String idTrans = trans.getString("acctgTransId");
		trans.set("glFiscalTypeId",cuentas.get("GlFiscalTypeContable"));

		// En caso de que se tenga el agrupador se asigna, si no, se crea.
		if (agrupador != null) {
			GenericValue generic = null;
			generic = obtenDocumento(delegator, tipoDocumento);
			generic = obtenEvento(delegator, generic.getString("acctgTransTypeId"));
			String tipoPoliza = generic.getString("tipoPolizaIdCont");
			trans.set("agrupador", agrupador);
			trans.set("tipoPolizaId", tipoPoliza);
			trans.set("consecutivo",
					obtenConsecutivo(delegator, agrupador, organizationId,
							tipoPoliza));

		} else {

			poliza = creaIdPoliza(delegator, tipoDocumento, fechaContable,
					organizationId, operacionNoRegistrada);
			trans.set("agrupador", poliza.get(0));
			trans.set("consecutivo", Long.parseLong(poliza.get(1)));
			trans.set("tipoPolizaId", poliza.get(2));
		}	

		// History
		Debug.log("Busca periodos");
		List<GenericValue> periodos = obtenPeriodos(delegator, organizationId,
				fechaContable);

		//checamos referencia
		boolean esReferenciaM = false;
		
		// Si existe referencia a la matriz dentro del evento, se procede a
		// realizar una afectacion contable
		if (cuentas.get("Cuenta Cargo Contable") != null) {

			Debug.log("Cuenta Contable");
			trans.set("acctgTransId", "CC-" + idTrans);

			delegator.createOrStore(trans);
			transacciones.add(trans);

			// AcctgTransEntry
			Debug.log("commit CCC");
			GenericValue acctgEntryD = generaAcctgTransEntry(delegator, trans,
					organizationId, partyId, "00001", "D",
					cuentas.get("Cuenta Cargo Contable"), currency, auxiliar,
					producto, tipoDocumento, esReferenciaM, null);

			delegator.createOrStore(acctgEntryD);

			actualizaGlAccountOrganization(delegator, monto,
					cuentas.get("Cuenta Cargo Contable"), organizationId,
					partyId, "DEBIT");

			// GlAccountHistory
			Debug.log("Busca histories");
			List<GenericValue> histories = actualizaGlAccountHistories(
					delegator, periodos, cuentas.get("Cuenta Cargo Contable"),
					monto, "Debit");

			Debug.log("Se impactan las histories regresadas");
			for (GenericValue history : histories) {
				delegator.createOrStore(history);
			}

			Debug.log("commit CAC");
			GenericValue acctgEntryC = generaAcctgTransEntry(delegator, trans,
					organizationId, partyId, "00002", "C",
					cuentas.get("Cuenta Abono Contable"), currency, auxiliar,
					producto, tipoDocumento, esReferenciaM, null);

			delegator.createOrStore(acctgEntryC);

			Debug.log("commit GlAO");
			actualizaGlAccountOrganization(delegator, monto,
					cuentas.get("Cuenta Abono Contable"), organizationId,
					partyId, "CREDIT");

			// GlAccountHistory
			Debug.log("Busca histories");
			histories = actualizaGlAccountHistories(delegator, periodos,
					cuentas.get("Cuenta Abono Contable"), monto, "Credit");

			Debug.log("Se impactan las histories regresadas");
			for (GenericValue history : histories) {
				delegator.createOrStore(history);
			}
		}

		return transacciones;
	}
	
	public static void actualizaSaldoCuentasBancarias(Delegator delegator,
			String cuentaBancariaId, String modulo, BigDecimal monto,
			Timestamp periodo) throws GenericEntityException {
		GenericValue saldoCuentaBancaria = buscaSaldoCuenta(delegator,
				cuentaBancariaId, periodo);
		Debug.log("Omar-montoActualDecimal: "
				+ saldoCuentaBancaria.getBigDecimal("monto"));
		Debug.log("Omar-monto: " + monto);
		saldoCuentaBancaria.set("monto", modulo
				.equalsIgnoreCase("DEVOLUCION_DINERO") ? saldoCuentaBancaria
				.getBigDecimal("monto").add(monto) : saldoCuentaBancaria
				.getBigDecimal("monto").subtract(monto));

		delegator.createOrStore(saldoCuentaBancaria);
	}
	
	public static List<String> registroDetalleCuentaBancaria(Delegator delegator, GenericValue itemListaTrans, String currency, String modulo, String bancoId, 
			String cuentaBancariaId, String organizationPartyId, String userLogin) throws GenericEntityException 
	{	Debug.log("Entra a registroDetalleCuentaBancaria");
		List<String> montoYTransaccionId = new ArrayList<String>();
		if(itemListaTrans.getString("acctgTransId").contains("C-"))
	    {	Debug.log("Es transaccion C");
			String transaccionId = itemListaTrans.getString("acctgTransId");
			String montoTransaccion = itemListaTrans.getString("postedAmount");
			String descripcion = itemListaTrans.getString("description");
		    //Genera el registro de la transaccion en cuantas bancarias            
			GenericValue traspasoBanco = delegator.makeValue("TraspasoBanco");
			String idTraspaso = delegator.getNextSeqId("TraspasoBanco");
			traspasoBanco.put("acctgTransId", transaccionId);
			traspasoBanco.put("idTraspaso", idTraspaso);
			traspasoBanco.put("moneda", currency);
			if(modulo.equalsIgnoreCase("DEVOLUCION_DINERO"))
			{	Debug.log("Modulo es Devolucion");
				traspasoBanco.put("bancoIdDestino", bancoId);
				traspasoBanco.put("cuentaDestino", cuentaBancariaId);
				traspasoBanco.put("tipotransaccion", "DEVOLUCION");	
			}
			else
			{	Debug.log("NO ES Modulo es Devolucion");
				traspasoBanco.put("bancoIdOrigen", bancoId);
				traspasoBanco.put("cuentaOrigen", cuentaBancariaId);
				traspasoBanco.put("tipotransaccion", "EGRESOS");			        				
			}    					 			
			traspasoBanco.put("monto", montoTransaccion);
			traspasoBanco.put("partyId", organizationPartyId);
			traspasoBanco.put("descripcion", descripcion);    			    			
			traspasoBanco.put("createdByUserLogin", userLogin);
			Debug.log("Va a crear traspasoBanco");
			delegator.create(traspasoBanco);
			montoYTransaccionId.add(transaccionId);
			montoYTransaccionId.add(montoTransaccion);
			Debug.log("Omar-traspasoBanco: " + traspasoBanco);		
	    }
		return montoYTransaccionId;
	
	}
	
	public static void actualizaCuentaBancariaAcctgTransEntry(Delegator delegator, String modulo, String cuentaBancariaId, String transaccionId, String montoTransaccion) throws GenericEntityException 
	{	EntityCondition conditionsEntry;
		if(modulo.equalsIgnoreCase("DEVOLUCION_DINERO"))
		{		Debug.log("Es DEVOLUCION_DINERO");
				conditionsEntry = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition(AcctgTransEntry.Fields.acctgTransId.name(), EntityOperator.EQUALS, transaccionId),
				EntityCondition.makeCondition(AcctgTransEntry.Fields.debitCreditFlag.name(), EntityOperator.EQUALS, "C"));							
		}
		else
		{		Debug.log("NO es DEVOLUCION_DINERO");	
				conditionsEntry = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition(AcctgTransEntry.Fields.acctgTransId.name(), EntityOperator.EQUALS, transaccionId),
				EntityCondition.makeCondition(AcctgTransEntry.Fields.debitCreditFlag.name(), EntityOperator.EQUALS, "D"));
		}
		List<GenericValue> acctgEntry = delegator.findByCondition("AcctgTransEntry", conditionsEntry, null, null);
		Debug.log("Va a hacer el update en AcctgTransEntry");
		Iterator<GenericValue> acctgEntryIter = acctgEntry.iterator();
		if(acctgEntryIter.hasNext())
		{	GenericValue acctgTransEntry = acctgEntryIter.next();								
			acctgTransEntry.set("cuentaBancariaId", cuentaBancariaId);
			delegator.createOrStore(acctgTransEntry);
			Debug.log("Omar-acctgTransEntry: " + acctgTransEntry);
		}
	}
	
	/**
	 * autor JRRM
	 * @param delegator
	 * @param idTipoDoc
	 * @param clave
	 * @param fecha
	 * @param monto
	 * @return
	 * @throws GenericEntityException
	 */
	public static String montoInsuficiencia(Delegator delegator,
			String idTipoDoc, String clave, Date fecha, BigDecimal monto)
			throws GenericEntityException {
		Debug.log("Entra monto insuficiencia");
		GenericValue documento = obtenDocumento(delegator, idTipoDoc);
		@SuppressWarnings("deprecation")
		String mes = obtenMesString(fecha.getMonth());
		List<GenericValue> generics = delegator.findByAnd(
				"ControlPresupuestal", UtilMisc.toMap("clavePresupuestal",
						clave, "mesId", mes, "momentoId",
						documento.getString("mComparativo")));
		if(generics == null || generics.isEmpty())
			throw new GenericEntityException("No Existe Suficiencia Presupuestal para la clave "+clave+": "+monto);
			GenericValue generic = generics.get(0);
		BigDecimal original = generic.getBigDecimal("monto");
		return "No Existe Suficiencia Presupuestal para la clave "+clave+": "+original.subtract(monto).toString();
	}
	
	public static GenericValue buscaSaldoCuenta(Delegator delegator, String cuenta,
			Timestamp periodo) throws GenericEntityException {
		// Rango de periodos.
		Calendar date = Calendar.getInstance();
		date.setTime(periodo);
		date.set(Calendar.DAY_OF_MONTH, 1);
		date.set(Calendar.HOUR, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		date.set(Calendar.AM_PM, Calendar.AM);
		periodo = new Timestamp(date.getTimeInMillis());
		
		List<GenericValue> generics = delegator.findByAnd("SaldoCatalogo", "catalogoId", cuenta, "tipo", "B", "periodo", periodo);
		GenericValue catalogo;
		if(generics.isEmpty()){
			// Si esta vacio se crea el registro trayendo la info de un mes almacenado.
			List<GenericValue> catalogos = delegator.findByAnd("SaldoCatalogo", "catalogoId", cuenta, "tipo", "B");
			catalogo = catalogos.get(0);
			catalogo.set("secuenciaId", delegator.getNextSeqId("ControlAuxiliar"));
			catalogo.set("periodo", periodo);
			catalogo.set("monto", BigDecimal.ZERO);
		}else{
			catalogo=generics.get(0);
		}
		return catalogo;
	}
	
//	public static List<GenericValue> creaTransaccion(Delegator delegator,
//			String modulo, Timestamp fechaRegistro, Timestamp fechaContable,
//			String tipoDocumento, String usuario, String organizationId,
//			String partyId, GenericValue estructura, BigDecimal monto,
//			String agrupador, String secuencia, List<String> clavePres,
//			Map<String, String> cuentas, String currency, String tipoClave,
//			String clavePresupuestal, String invoiceId, String paymentId,
//			String shipmentId, String receiptId, String workEffortId,
//			String roleTypeId, String auxiliar, String producto,
//			String agrupadorAnterior, String operacionNoRegistrada,
//			String description, String tipoTransaccion)
//			throws GenericEntityException, RepositoryException {
//		return creaTransaccion(delegator, modulo, fechaRegistro, fechaContable,
//				tipoDocumento, usuario, organizationId, partyId, estructura,
//				monto, agrupador, secuencia, clavePres, cuentas, currency,
//				tipoClave, clavePresupuestal, invoiceId, paymentId, shipmentId,
//				receiptId, workEffortId, roleTypeId, auxiliar, producto,
//				agrupadorAnterior, operacionNoRegistrada, description,
//				tipoTransaccion, null);
//	}
	
	
}