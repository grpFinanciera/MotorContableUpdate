package com.opensourcestrategies.financials.transactions;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.opentaps.base.entities.OperacionPatrimonial;
import org.opentaps.base.entities.OperacionPatrimonialDetalle;
import org.opentaps.base.entities.TipoDocumento;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.service.ServiceException;

import com.ibm.icu.util.Calendar;


public class MotorContableFinanzas {

	public static List<GenericValue> obtenerListTrans(Delegator delegator,
			String moduloR, Timestamp fechaRegistro, Timestamp fechaContable,
			String documentoR, GenericValue userLogin,
			String organizationPartyId, GenericValue estructura,
			BigDecimal monto, String agrupador, List<String> claveReduccion,
			Map<String, String> cuentasR, String moneda, String tipoClave,
			String clavePresupuestalR, String moduloA, String documentoA,
			List<String> claveAmpliacion, Map<String, String> cuentasA,
			String clavePresupuestalA, String description)
			throws RepositoryException, GenericEntityException {
		
		String tipoTransaccion = obtenTipoTransDeDocumento(delegator,
				documentoA);

		List<GenericValue> listaTrans = creaTransaccionAfectacion(delegator,
				moduloR, fechaRegistro, fechaContable, documentoR,
				userLogin.getString("userLoginId"), organizationPartyId, null,
				estructura, monto, agrupador, claveReduccion, cuentasR, moneda,
				tipoClave, clavePresupuestalR, description, tipoTransaccion);

		if (agrupador == null) {
			Iterator<GenericValue> polizaIds = listaTrans.iterator();
			if (polizaIds.hasNext()) {
				agrupador = ((GenericValue) polizaIds.next())
						.getString("agrupador");
				Debug.log("Obtiene agrupador sino existia: " + agrupador);
			}
		}

		listaTrans = creaTransaccionAfectacion(delegator, moduloA,
				fechaRegistro, fechaContable, documentoA,
				userLogin.getString("userLoginId"), organizationPartyId, null,
				estructura, monto, agrupador, claveAmpliacion, cuentasA,
				moneda, tipoClave, clavePresupuestalA, description, tipoTransaccion);

		return listaTrans;

	}
	
	public static List<String> validaObligatoriosAfectacion(
			Delegator delegator, String periodoA, String periodoR,
			String moneda, BigDecimal amount) {

		Debug.log("Entra a validar obligatorios");

		List<String> aviso = new ArrayList<String>();
		
		if (periodoA == null)
			aviso.add("Es necesario ingresar el periodo de ampliaci\u00f3n");
		if (periodoR == null)
			aviso.add("Es necesario ingresar el periodo de reducci\u00f3n");
		if (moneda == null)
			aviso.add("Es necesario ingresar el tipo de moneda");
		if (amount == null)
			aviso.add("Es necesario ingresar el monto");
		
		return aviso;
	}
	
	public static List<String> validaObligatoriosTPatrimonio(
			Delegator delegator, String idTipoDoc, String bancoIdOrigen,
			String proveedor, String descripcion, String amount,
			String currency, String tipo) throws GenericEntityException {

		Debug.log("Entra a validar obligatorios");

		List<String> aviso = new ArrayList<String>();
		
		if(idTipoDoc == null)
			aviso.add("Es necesario ingresar el evento");
		if(bancoIdOrigen == null)
			aviso.add("Es necesario ingresar el banco");
		if(amount == null)
			aviso.add("Es necesario ingresar el monto");
		if(currency == null)
			aviso.add("Es necesario ingresar el tipo de moneda");
		if(tipo == null)
			aviso.add("Es necesario ingresar el tipo de traspaso");
		if(descripcion == null)
			aviso.add("Es necesario ingresar la descripci\u00f3n");
		if (idTipoDoc != null) {
			List<String> auxiliares = obtenAuxiliares(delegator, idTipoDoc);

			String auxiliarC = auxiliares.get(0), auxiliarA = auxiliares.get(1);

			if ((auxiliarC.equals("A") && auxiliarA.equals("A"))
					|| auxiliarC.equals("P") || auxiliarA.equals("P"))
				aviso.add("El evento seleccionado a sido mal configurado para el traspaso patrimonial");
			else if ((auxiliarC.equals("A") && proveedor == null)
					|| (auxiliarA.equals("A") && proveedor == null))
				aviso.add("Es necesario ingresar el proveedor");
		}
		
		return aviso;
	}

	@SuppressWarnings("rawtypes")
	public static List<GenericValue> creaTransaccionAfectacion(
			Delegator delegator, String modulo, Timestamp fechaRegistro,
			Timestamp fechaContable, String tipoDocumento, String usuario,
			String organizationId, String partyId, GenericValue estructura,
			BigDecimal monto, String agrupador, List<String> clavePres,
			Map<String, String> cuentas, String currency, String tipoClave,
			String clavePresupuestal, String description, String tipotransaccion)
			throws GenericEntityException, RepositoryException {

		Debug.log("comienza la creacion de la transaccion");
		List<String> poliza = null;

		// en caso de que no se tenga la Entidad Contable, se busca
		if (partyId == null || partyId.isEmpty())
			partyId = obtenPadre(delegator, organizationId);

		// AcctgTrans
		GenericValue trans = GenericValue.create(delegator
				.getModelEntity("AcctgTrans"));
		trans.setNextSeqId();

		// se obtienen los momentos contables afectadodos positivamente
		List<String> momentos = obtenMomentos(delegator, tipoDocumento);

		List<GenericValue> transacciones = FastList.newInstance();
		trans.set("transactionDate", fechaRegistro);
		trans.set("postedDate", fechaContable);
		trans.set("isPosted", "Y");
		trans.set("description", description);
		trans.set("acctgTransTypeId", tipotransaccion);
		trans.set("createdByUserLogin", usuario);
		trans.set("lastModifiedByUserLogin", usuario);
		trans.set("partyId", partyId);
		trans.set("organizationPartyId", organizationId);
		trans.set("postedAmount", monto);
		trans.set("idTipoDoc", tipoDocumento);
		trans.set("idTipoClave", tipoClave);
		trans.set("clavePresupuestal", clavePresupuestal);

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

		// En caso de que se tenga el agrupador se asigna, sino se crea
		if (agrupador != null)
			trans.set("agrupador", agrupador);
		else {
			poliza = creaIdPoliza(delegator, tipoDocumento, fechaContable,
					organizationId);
			trans.set("agrupador", poliza.get(0));
		}

		
		trans.set("glFiscalTypeId", cuentas.get("GlFiscalTypePresupuesto"));
		trans.set("tipoPolizaId", cuentas.get("tipoPolizaP"));

		// en caso de que el agrupador se tenga, se busca el consecutivo de
		// acuerdo a la poliza presupuestal, la organizacion y el agrupador
		// que
		// se dio, sino se ingresa el nuevo generado de acuerdo a la poliza
		// generada
		if (agrupador != null)
			trans.set(
					"consecutivo",
					obtenConsecutivo(delegator, agrupador, organizationId,
							cuentas.get("tipoPolizaP")));
		else
			trans.set("consecutivo", poliza.get(1));
		

		// History
		Debug.log("Busca periodos");
		List<GenericValue> periodos = obtenPeriodos(delegator, organizationId,
				fechaContable);
		
		//checamos referencia
		boolean esReferenciaM = cuentas.get("referencia").equals("M")
				|| cuentas.get("referencia").equals("G") ? true : false;

		// Si existen cuentas 8 dentro del evento, se procede a realizar una
		// afectacion presupuestal
		if (cuentas.get("Cuenta Cargo Presupuesto") != null) {
			Debug.log("Cuenta Presupuestal");
			trans.set("acctgTransId", "A-" + idTrans);

			// se crea Acctgtrans y se realiza un clone para retornar la
			// informacion
			delegator.create(trans);
			GenericValue trans2 = (GenericValue) trans.clone();
			transacciones.add(trans2);
			Debug.log("commit CCP");

			// AcctgTransEntry
			GenericValue acctgEntryD = generaAcctgTransEntry(delegator, trans,
					organizationId, partyId, "00001", "D",
					cuentas.get("Cuenta Cargo Presupuesto"), currency, null,
					null, tipoDocumento, esReferenciaM);


			// Tags seteados.
			acctgEntryD = asignaTags(
					delegator,
					acctgEntryD,
					buscaClasifAdministrativa(delegator, clavePres, estructura),
					clavePres, estructura);
			delegator.create(acctgEntryD);

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
				delegator.store(history);
			}

			Debug.log("commit CAP");
			GenericValue acctgEntryC = generaAcctgTransEntry(delegator, trans,
					organizationId, partyId, "00002", "C",
					cuentas.get("Cuenta Abono Presupuesto"), currency, null,
					null, tipoDocumento, esReferenciaM);
			// Tags seteados.
			acctgEntryC = asignaTags(
					delegator,
					acctgEntryC,
					buscaClasifAdministrativa(delegator, clavePres, estructura),
					clavePres, estructura);
			delegator.create(acctgEntryC);

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
				delegator.store(history);
			}

		}

		return transacciones;
	}
	
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
	
	public static GenericValue obtenerCvePresupuestal(Delegator delegator,
			String cvePresupuestalId) throws GenericEntityException {

		Debug.log("Obtener clave presupuestal");
		
		GenericValue cvePresupuestal = delegator.findByPrimaryKey(
				"ClavePresupuestal",
				UtilMisc.toMap("clavePresupuestal", cvePresupuestalId));

		return cvePresupuestal;

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
	
	public static boolean controlPresupuestal(Delegator delegator,
			String idTipoDoc, String clave, String mes, BigDecimal monto,
			String ciclo) throws GenericEntityException {
		Debug.log("Entra control presupuestal");

		GenericValue generic = obtenDocumento(delegator, idTipoDoc);
		String mComparativo = generic.getString("mComparativo");
		String mEjecutar1 = generic.getString("mEjecutar1");
		String mEjecutar2 = generic.getString("mEjecutar2");
		if (mComparativo != null && !mComparativo.isEmpty()) {
			if (comparaMontos(delegator, clave, mComparativo, mes, monto,
					generic.getString("comparacion"))) {
				restaMonto(delegator, clave, mComparativo, mes, monto);

			} else {
				Debug.log("Saldo insuficiente(momentos)");
				return false;
			}
		}
		if (mEjecutar1 != null && !mEjecutar1.isEmpty()) {
			sumaMonto(delegator, clave, mEjecutar1, mes, monto, ciclo);
		}

		if (mEjecutar2 != null && !mEjecutar2.isEmpty()) {
			sumaMonto(delegator, clave, mEjecutar2, mes, monto, ciclo);
		}
		return true;
	}

	public static String obtenMesString(int mes) {
		mes++;
		return mes < 10 ? "0" + new Integer(mes).toString() : new Integer(mes)
				.toString();
	};

	public static void sumaMonto(Delegator delegator, String clave,
			String momento, String mes, BigDecimal monto, String ciclo)
			throws GenericEntityException {

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
			delegator.store(generic);
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

	public static void restaMonto(Delegator delegator, String clave,
			String momento, String mes, BigDecimal monto)
			throws GenericEntityException {

		List<GenericValue> generics = delegator.findByAnd(
				"ControlPresupuestal", UtilMisc.toMap("clavePresupuestal",
						clave, "mesId", mes, "momentoId", momento));
		if (!generics.isEmpty()) {
			GenericValue generic = generics.get(0);
			BigDecimal original = generic.getBigDecimal("monto");
			generic.set("monto", original.subtract(monto));
			delegator.store(generic);
		} else {
			// No debe pasar esto.
		}
	}

	public static boolean comparaMontos(Delegator delegator, String clave,
			String momento, String mes, BigDecimal monto, String comparacion)
			throws GenericEntityException {

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
						"Error, elemento en Matriz no existe");
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
	
	public static int numClasificaciones(GenericValue estructura) {

		int cont = 0;
		for (int i = 1; i < 16; i++) {
			if (estructura.getString("clasificacion" + i) != null)
				cont++;
		}

		return cont;
	}
	
	public static List<Map<String, Object>> obtenGeneral(
			LedgerRepositoryInterface ledgerRepository, String userLoginId)
			throws RepositoryException {

		EntityCondition conditions1 = EntityCondition.makeCondition(EntityOperator.AND, 
				  EntityCondition.makeCondition(OperacionPatrimonial.Fields.status.name(), EntityOperator.EQUALS, "N"),
				  EntityCondition.makeCondition(OperacionPatrimonial.Fields.userLoginId.name(), EntityOperator.EQUALS, userLoginId));
		
		List<OperacionPatrimonial> operacion = ledgerRepository.findList(
				OperacionPatrimonial.class, conditions1);
		List<Map<String, Object>> generalList = new FastList<Map<String, Object>>();
		for (OperacionPatrimonial s : operacion) {
			Map<String, Object> map = s.toMap();
			generalList.add(map);
		}

		return generalList;
	}
	
	public static GenericValue crearOPatrimonial(Delegator delegator,
			Timestamp fechaRegistro, Timestamp fechaContable,
			String organizationPartyId, String userLoginId, String description) {
		
		GenericValue operacionP = GenericValue.create(delegator
				.getModelEntity("OperacionPatrimonial"));
		operacionP.set("agrupadorP",
				delegator.getNextSeqId("OperacionPatrimonial"));
		operacionP.set("fechaTransaccion", fechaRegistro);
		operacionP.set("fechaContable", fechaContable);
		operacionP.set("status", "N");
		operacionP.set("organizationPartyId", organizationPartyId);
		operacionP.set("userLoginId", userLoginId);
		operacionP.set("description", description);

		return operacionP;
	}
	
	public static GenericValue actualizaOPatrimonial(Delegator delegator,
			String agrupadorP) {
		
		Debug.log("Ingresa a actualizar la operacion patrimonial");
		
		GenericValue operacionP = GenericValue.create(delegator
				.getModelEntity("OperacionPatrimonial"));
		operacionP.set("agrupadorP",
				agrupadorP);
		operacionP.set("status", "Y");

		return operacionP;
	}
	
	public static List<Map<String, Object>> obtenerDocumentos(
			LedgerRepositoryInterface ledgerRepository)
			throws RepositoryException {

		List<TipoDocumento> documentos = ledgerRepository.findList(
				TipoDocumento.class, EntityCondition.makeCondition(
						TipoDocumento.Fields.moduloId.name(),
						EntityOperator.EQUALS, "CONTABILIDAD_P"));
		List<Map<String, Object>> documentosList = new FastList<Map<String, Object>>();
		for (TipoDocumento s : documentos) {
			Map<String, Object> map = s.toMap();
			documentosList.add(map);
		}

		return documentosList;

	}
	
	public static GenericValue crearConcepto(Delegator delegator,
			String idTipoDoc, String monto, String currency, String theirParty,
			String productId, List<Map<String, Object>> generalList,
			String theirParty2, String productId2)
			throws GenericEntityException {

		BigDecimal money = new BigDecimal(monto);
		
		GenericValue doc = obtenDocumento(delegator, idTipoDoc);

		GenericValue concepto = GenericValue.create(delegator
				.getModelEntity("OperacionPatrimonialDetalle"));
		concepto.set("idDetalle",
				delegator.getNextSeqId("OperacionPatrimonialDetalle"));
		concepto.set("agrupadorP", generalList.get(0).get("agrupadorP"));
		concepto.set("idTipoDoc", idTipoDoc);
		concepto.set("description", doc.getString("descripcion"));
		concepto.set("monto", money);
		concepto.set("tipoMoneda", currency);
		concepto.set("theirPartyId", theirParty);
		concepto.set("productId", productId);
		concepto.set("theirPartyId2", theirParty2);
		concepto.set("productId2", productId2);

		return concepto;
	}
	
	public static GenericValue obtenDocumento(Delegator delegator,
			String idTipoDoc) throws GenericEntityException {

		GenericValue generic = delegator.findByPrimaryKey("TipoDocumento",
				UtilMisc.toMap("idTipoDoc", idTipoDoc));

		Debug.log("Se obtiene doc: " + generic.getString("idTipoDoc"));
		return generic;
	}
	
	public static List<String> validaObligatorios(Delegator delegator,
			String idTipoDoc, String monto, String currency, String theirParty,
			String productId, String theirParty2, String productId2,
			List<String> auxiliares) throws GenericEntityException {

		Debug.log("Entra a validar obligatorios");
		
		List<String> aviso = new ArrayList<String>();

		if(idTipoDoc == null)
			aviso.add("Es neccesario ingresar el concepto");
		if(monto == null)
			aviso.add("Es neccesario ingresar el monto");
		if(currency == null)
			aviso.add("Es neccesario ingresar el tipo de moneda");
		
		if(idTipoDoc != null) {

			if (auxiliares.isEmpty())
				aviso.add("No estan espicificados los catalogos auxiliares en el documento "
								+ idTipoDoc);
			if (auxiliares.get(0).equals("A") && theirParty == null)
				aviso.add("Es necesario ingresar el auxiliar de cargo");
			if (auxiliares.get(1).equals("A") && theirParty2 == null) 
				aviso.add("Es necesario ingresar el auxiliar de abono");	
			if (auxiliares.get(0).equals("P") && productId == null)
				aviso.add("Es necesario ingresar el producto de cargo");
			if ((auxiliares.get(1).equals("P") && productId2 == null))
				aviso.add("Es necesario ingresar el producto de abono");
		}

		return aviso;
		
	}
	
	public static List<String> obtenAuxiliares(Delegator delegator, String docId)
			throws GenericEntityException {
		GenericValue generic = null;
		List<String> auxiliares = new ArrayList<String>();

		generic = obtentTipoTransaccion(delegator,
				obtenTipoTransDeDocumento(delegator, docId));

		auxiliares.add(generic.getString("catalogoCargo"));
		auxiliares.add(generic.getString("catalogoAbono"));

		return auxiliares;
	}
	
	public static GenericValue obtentTipoTransaccion(Delegator delegator,
			String tipoT) throws GenericEntityException {
		GenericValue generic = delegator.findByPrimaryKey("AcctgTransType",
				UtilMisc.toMap("acctgTransTypeId", tipoT));

		return generic;
	}
	
	public static String obtenTipoTransDeDocumento(Delegator delegator,
			String tipoDocumento) throws GenericEntityException {
		Debug.log("Obten tipoTrans");

		GenericValue generic = obtenDocumento(delegator, tipoDocumento);
		return generic.getString("acctgTransTypeId");
	}
	
	public static List<Map<String, Object>> obtenConceptos(
			LedgerRepositoryInterface ledgerRepository, String agrupadorP)
			throws RepositoryException {

		List<OperacionPatrimonialDetalle> conceptos = ledgerRepository.findList(
				OperacionPatrimonialDetalle.class, EntityCondition.makeCondition(
						OperacionPatrimonialDetalle.Fields.agrupadorP.name(),
						EntityOperator.EQUALS, agrupadorP));
		List<Map<String, Object>> conceptosList = new FastList<Map<String, Object>>();
		for (OperacionPatrimonialDetalle s : conceptos) {
			Map<String, Object> map = s.toMap();
			conceptosList.add(map);
		}
        
		Debug.log("Obten concepto: " + conceptosList);
		return conceptosList;
	}
	
	public static String obtenPadre(Delegator delegator, String organizationId)
			throws RepositoryException, GenericEntityException {
		String parentId = organizationId;
		do {
			Debug.log("partyId: " + parentId);
			organizationId = obtenPartyGroup(delegator, organizationId);

			if (organizationId != null)
				parentId = organizationId;
			Debug.log("partyId2: " + parentId);
		} while (organizationId != null);
		return parentId;
	}
	
	public static String obtenPartyGroup(Delegator delegator, String party)
			throws GenericEntityException {
		String parentId = null;
		
		Debug.log("Ingresa a partyGroup con: " + party);
		GenericValue generic = delegator.findByPrimaryKey("PartyGroup",
				UtilMisc.toMap("partyId", party));
		
		Debug.log("Se obtiene padre: " + generic);
		parentId = generic.getString("Parent_id");
		
		return parentId;
	}
	
	public static List<GenericValue> creaTransaccionConcepto(
			Delegator delegator, String agrupador, Timestamp fechaRegistro,
			Timestamp fechaContable, String idTipoDoc, BigDecimal monto,
			String tipoMoneda, String theirPartyId, String productId,
			String organizationPartyId, String userLogin, String partyId,
			String description, String theirPartyId2, String productId2,
			String tipoTransaccion)
			throws RepositoryException, GenericEntityException,
			ServiceException {

		Debug.log("comienza la creacion de la transaccion");
		List<String> poliza = null;

		// AcctgTrans
		GenericValue trans = GenericValue.create(delegator
				.getModelEntity("AcctgTrans"));
		trans.setNextSeqId();
		
		List<GenericValue> transacciones = FastList.newInstance();
		trans.set("transactionDate", fechaRegistro);
		trans.set("postedDate", fechaContable);
		trans.set("isPosted", "Y");
		trans.set("createdByUserLogin", userLogin);
		trans.set("lastModifiedByUserLogin", userLogin);
		trans.set("partyId", partyId);
		trans.set("organizationPartyId", organizationPartyId);
		trans.set("postedAmount", monto);
		trans.set("idTipoDoc", idTipoDoc);
		trans.set("description", description);
		
		//ingresamos tipo de transaccion
		if (tipoTransaccion != null)
			trans.set("acctgTransTypeId", tipoTransaccion);
		else
			trans.set("acctgTransTypeId",
				obtenTipoTransDeDocumento(delegator, idTipoDoc));

		//En caso de que se tengan catalogos contables se asignan
		if(theirPartyId != null)
			trans.set("theirPartyId", theirPartyId);
		if (productId != null)
			trans.set("productId", productId);
		
		String idTrans = trans.getString("acctgTransId");

		//En caso de que se tenga el agrupador se asigna, sino se crea
		if (agrupador != null)
			trans.set("agrupador", agrupador);
		else {
			poliza = creaIdPoliza(delegator, idTipoDoc, fechaContable,
					organizationPartyId);
			trans.set("agrupador", poliza.get(0));
		}
		
		Map<String, String> cuentas = FastMap.newInstance();
		cuentas = cuentasPatrimonio (delegator, idTipoDoc);
		trans.set("glFiscalTypeId", cuentas.get("GlFiscalTypeContable"));
		trans.set("tipoPolizaId", cuentas.get("tipoPolizaC"));
		
		// obtiene los montos de los catalogos
		actualizaCatalogos(delegator, theirPartyId, productId, monto,
				organizationPartyId, idTipoDoc,
				cuentas.get("CuentaCargo"), cuentas.get("CuentaAbono"),fechaContable,
				theirPartyId2, productId2);

		// en caso de que el agrupador se tenga, se busca el consecutivo de
		// acuerdo a la poliza contable, la organizacion y el agrupador
		// que
		// se dio, sino se ingresa el nuevo generado de acuerdo a la poliza
		// generada
		if (agrupador != null)
			trans.set(
					"consecutivo",
					obtenConsecutivo(delegator, agrupador, organizationPartyId,
							cuentas.get("tipoPolizaC")));
		else
			trans.set("consecutivo", poliza.get(1));
		
		// History
		Debug.log("Busca periodos");
		List<GenericValue> periodos = obtenPeriodos(delegator, organizationPartyId,
				fechaContable);
		
		//checamos referencia
		boolean esReferenciaM = cuentas.get("referencia").equals("M")
				|| cuentas.get("referencia").equals("G") ? true : false;

		Debug.log("Cuenta Contable Patrimonio");
		trans.set("acctgTransId", "PT-" + idTrans);			

		delegator.create(trans);
		transacciones.add(trans);

		// AcctgTransEntry
		Debug.log("commit CCC");
		GenericValue acctgEntryD = generaAcctgTransEntry(delegator, trans,
				organizationPartyId, partyId, "00001", "D",
				cuentas.get("CuentaCargo"), tipoMoneda, theirPartyId,
				productId, idTipoDoc, esReferenciaM, theirPartyId2,
				productId2);

		delegator.create(acctgEntryD);

		actualizaGlAccountOrganization(delegator, monto,
							cuentas.get("CuentaCargo"), organizationPartyId,
							partyId, "DEBIT");

		// GlAccountHistory
		Debug.log("Busca histories");
		List<GenericValue> histories = actualizaGlAccountHistories(
							delegator, periodos, cuentas.get("CuentaCargo"),
							monto, "Debit");

		Debug.log("Se impactan las histories regresadas");
		for (GenericValue history : histories) {
			delegator.store(history);
		}

		Debug.log("commit CAC");
		GenericValue acctgEntryC = generaAcctgTransEntry(delegator, trans,
				organizationPartyId, partyId, "00002", "C",
				cuentas.get("CuentaAbono"), tipoMoneda, theirPartyId,
				productId, idTipoDoc, esReferenciaM, theirPartyId2,
				productId2);

		delegator.create(acctgEntryC);

		Debug.log("commit GlAO");
		actualizaGlAccountOrganization(delegator, monto,
				cuentas.get("CuentaAbono"), organizationPartyId,
							partyId, "CREDIT");

		// GlAccountHistory
		Debug.log("Busca histories");
		histories = actualizaGlAccountHistories(delegator, periodos,
							cuentas.get("CuentaAbono"), monto, "Credit");

		Debug.log("Se impactan las histories regresadas");
		for (GenericValue history : histories) {
			delegator.store(history);
		}
		
		return transacciones;

	}
	
	public static List<String> creaIdPoliza(Delegator delegator,
			String tipoDocumento, Timestamp fechaContable, String organizationId)
			throws GenericEntityException {

	    Debug.log("Se comienza a crear poliza");
		GenericValue generic = null;
		List<GenericValue> agrupadores = null;

		generic = obtenDocumento(delegator, tipoDocumento);
		generic = obtenEvento(delegator, generic.getString("acctgTransTypeId"));
		String prefijo = obtenPrefijo(delegator, fechaContable);

		agrupadores = generic.getString("tipoPolizaIdCont") != null ? obtenerListaConsecutivos(
				delegator, generic.getString("tipoPolizaIdCont"),
				organizationId, prefijo) : obtenerListaConsecutivos(delegator,
				generic.getString("tipoPolizaIdPres"), organizationId, prefijo);

		return obtenAgrupadoryConsecutivo(delegator, agrupadores, prefijo);
	}
	
	public static GenericValue obtenEvento(Delegator delegator, String eventoId)
			throws GenericEntityException {

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
	
	public static Map<String, String> cuentasPatrimonio(Delegator delegator,
			String idTipoDocumento) throws GenericEntityException,
			ServiceException {
		Debug.log("cuentas patrimoniales");
		
		Map<String, String> cuentas = new HashMap<String, String>();
		GenericValue tipoDocumento = obtenDocumento(delegator, idTipoDocumento);

		GenericValue evento = obtenEvento(delegator,
				tipoDocumento.getString("acctgTransTypeId"));

		cuentas.put("GlFiscalTypeContable",
				evento.getString("glFiscalTypeIdCont"));
		cuentas.put("CuentaCargo", evento.getString("cuentaCargoPatri"));
		cuentas.put("CuentaAbono", evento.getString("cuentaAbonoPatri"));
		cuentas.put("tipoPolizaC", evento.getString("tipoPolizaIdCont"));
		cuentas.put("referencia", evento.getString("referencia"));

		return cuentas;
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

		Debug.log("consecutivo " + Long.toString(consecutivo));
		return Long.toString(consecutivo);
	}
	
	public static List<GenericValue> obtenPeriodos(Delegator delegator,
			String organizationId, Date fecha) throws GenericEntityException {
		List<GenericValue> periodos = delegator.findByAnd("CustomTimePeriod",
				UtilMisc.toMap("organizationPartyId", organizationId,
						"isClosed", "N"));
		List<GenericValue> periodosAplicables = new ArrayList<GenericValue>();
		for (GenericValue periodo : periodos) {
			java.sql.Date fromDate = periodo.getDate("fromDate");
			java.sql.Date thruDate = periodo.getDate("thruDate");
			if (fecha.after(fromDate) && fecha.before(thruDate)) {
				periodosAplicables.add(periodo);
			}
		}
		Debug.log("Periodos regresados.- " + periodosAplicables.size());
		return periodosAplicables;
	}
	
	public static GenericValue generaAcctgTransEntry(Delegator delegator,
			GenericValue transaccion, String organizacionPartyId,
			String partyId, String seqId, String flag, String cuenta,
			String currency, String auxiliar, String producto,
			String idTipoDoc, boolean esReferenciaM)
			throws GenericEntityException {
		return generaAcctgTransEntry(delegator, transaccion,
				organizacionPartyId, partyId, seqId, flag, cuenta, currency,
				auxiliar, producto, idTipoDoc, esReferenciaM, null, null);
	}

	public static GenericValue generaAcctgTransEntry(Delegator delegator,
			GenericValue transaccion, String organizacionPartyId,
			String partyId, String seqId, String flag, String cuenta,
			String currency, String auxiliar, String producto,
			String idTipoDoc, boolean esReferenciaM, 
			String auxiliar2, String producto2)
			throws GenericEntityException {
		// C/D
		Debug.log("Empieza AcctgTransEntry " + cuenta);
		Debug.log("Auxiliar1: " + auxiliar + "Auxiliar2: " + auxiliar2);
		Debug.log("Producto1: " + producto + "Producto2: " + producto2);

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

		if (esReferenciaM && !cuenta.startsWith("8")) {
			List<String> auxiliares = obtenAuxiliares(delegator, idTipoDoc);
			String auxiliarC = auxiliares.get(0), auxiliarA = auxiliares.get(1);
			
			if (flag.equals("D")) {
				if (auxiliarC.equals("A")){
					if (auxiliar != null)
						acctgEntry.set("theirPartyId", auxiliar);
					else if (auxiliar2 != null)
						acctgEntry.set("theirPartyId", auxiliar2);
				}
				else if (auxiliarC.equals("P")){
					Debug.log("producto cargo: " + producto + producto2);
					if (producto != null)
						acctgEntry.set("productId", producto);
					else if (producto2 != null)
						acctgEntry.set("productId", producto2);
				}
					
			}
			if (flag.equals("C")) {
				if (auxiliarA.equals("A")){
					if (auxiliar2 != null)
						acctgEntry.set("theirPartyId", auxiliar2);
					else if (auxiliar != null)
						acctgEntry.set("theirPartyId", auxiliar);
				}
				else if (auxiliarA.equals("P")){
					Debug.log("producto abono: " + producto + producto2);
					if (producto2 != null)
						acctgEntry.set("productId", producto2);
					else if (producto != null)
						acctgEntry.set("productId", producto);
				}
			}
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
				glAccountOrganizationParty
						.set("postedBalance", BigDecimal.ZERO);
			}

			BigDecimal montoActual = glAccountOrganizationParty
					.getBigDecimal("postedBalance");
			glAccountOrganizationParty.set("postedBalance",
					montoActual.subtract(monto));

			if (glAccountOrganization.getBigDecimal("postedBalance") == null) {
				glAccountOrganization.set("postedBalance", BigDecimal.ZERO);
			}

			montoActual = glAccountOrganization.getBigDecimal("postedBalance");
			glAccountOrganization.set("postedBalance",
					montoActual.subtract(monto));
		}

		delegator.store(glAccountOrganizationParty);
		delegator.store(glAccountOrganization);
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
	
	public static GenericValue obtenCuenta(Delegator delegator, String cuenta)
			throws GenericEntityException {
		GenericValue generic = delegator.findByPrimaryKey("GlAccount",
				UtilMisc.toMap("glAccountId", cuenta));

		return generic;
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

	public static String validaClave(Delegator delegator,
			GenericValue estructura, List<String> clavePres)
			throws GenericEntityException {
		return validaClaveVigencia(delegator, estructura, clavePres, false,
				null);
	}
	
	public static String validaClaveVigencia(Delegator delegator,
			GenericValue estructura, List<String> clavePres, boolean vigencia,
			Date fecha) throws GenericEntityException {
		
		Debug.log("Valida la clave");
		
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
					mensaje += "Clasificaci\u00f3n " + tipoClasificacion
							+ "No existe\n";
				} else {
					if (vigencia
							&& clasificacion.get(0).getString("state")
									.equalsIgnoreCase("I")) {
						mensaje += "Clasificaci\u00f3n " + tipoClasificacion
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
	
	public static String validaVigenciaEnum(String mensaje,
			String tipoClasificacion, GenericValue enumeration, Date fechaTrans)
			throws GenericEntityException {
		java.sql.Date fechaInicio = enumeration.getDate("fechaInicio");
		java.sql.Date fechaFin = enumeration.getDate("fechaFin");
		if (!fechaInicio.before(fechaTrans) || !fechaFin.after(fechaTrans)) {
			Debug.log("Clasificaci\u00f3n no vigente");
			mensaje += "Clasificaci\u00f3n " + tipoClasificacion + " No vigente\n";
		}
		return mensaje;
	}	
	
	public static String obtenTipoPolizaDescripcion(Delegator delegator,
			List<GenericValue> trans) throws GenericEntityException {
		Iterator<GenericValue> transId = trans.iterator();
		GenericValue transa = transId.next();

		GenericValue generic = obtenTipoPoliza(delegator,
				transa.getString("tipoPolizaId"));

		return generic.getString("description");

	}

	public static GenericValue obtenTipoPoliza(Delegator delegator,
			String tipoPolizaId) throws GenericEntityException {

		GenericValue generic = delegator.findByPrimaryKey("TipoPoliza",
				UtilMisc.toMap("tipoPolizaId", tipoPolizaId));

		Debug.log("Se obtiene el tipo de poliza: "
				+ generic.getString("tipoPolizaId"));
		return generic;
	}
	
	public static void eliminaPatrimonio(Delegator delegator,
			LedgerRepositoryInterface ledgerRepository, String userLoginId)
			throws GenericEntityException, RepositoryException {

		Debug.log("Ingresa a eliminar la operacion patrimonial");

		List<Map<String, Object>> generalList = obtenGeneral(ledgerRepository, userLoginId);

		if (generalList.size() != 0) {
			GenericValue operacionP = actualizaOPatrimonial(delegator,
					(String) generalList.get(0).get("agrupadorP"));
			delegator.store(operacionP);
		}
	}
	
	public static Map<String, String> cuentasDiarias(Delegator delegator,
			String idTipoDocumento) throws GenericEntityException {

		Debug.log("cuentas Diarias");
		Map<String, String> cuentas = new HashMap<String, String>();

		GenericValue tipoDocumento = obtenDocumento(delegator, idTipoDocumento);

		GenericValue evento = obtenEvento(delegator,
				tipoDocumento.getString("acctgTransTypeId"));

		cuentas.put("GlFiscalTypeContable",
				evento.getString("glFiscalTypeIdCont"));
		cuentas.put("Cuenta Cargo Contable", evento.getString("cuentaCargoPatri"));
		cuentas.put("Cuenta Abono Contable", evento.getString("cuentaAbonoPatri"));
		cuentas.put("referencia", evento.getString("referencia"));

		return cuentas;
	}
	
	public static GenericValue creteOrStoreControlAuxiliar(Delegator delegator,
			String partyId, String auxiliarId, String cuenta, BigDecimal monto,
			String secuencia, String tipoAux) throws GenericEntityException {
		
		Debug.log("ingresa a actualizar el auxiliar o crear");

		GenericValue controlAux = GenericValue.create(delegator
				.getModelEntity("ControlAuxiliar"));
		if (secuencia == null) {
			controlAux.set("idSecuencia",
					delegator.getNextSeqId("ControlAuxiliar"));
			controlAux.set("partyId", partyId);
			controlAux.set("auxiliarId", auxiliarId);
			controlAux.set("monto", monto);
			controlAux.set("tipo", tipoAux);
			if (tipoAux.equals("A"))
				controlAux.set("auxiliarRoleTypeId",
						obtenRolAuxiliar(delegator, auxiliarId));
			else
				controlAux.set("auxiliarRoleTypeId",
						obtenRolProducto(delegator, auxiliarId));
			if (cuenta.startsWith("4") || cuenta.startsWith("5"))
				controlAux.set("flagCierre", "M");
			else
				controlAux.set("flagCierre", "N");
		} else {
			controlAux.set("idSecuencia", secuencia);
			controlAux.set("monto", monto);
		}
		

		return controlAux;
	}
	
	@SuppressWarnings("static-access")
	public static List<GenericValue> obtenerMontoAuxiliar(Delegator delegator,
			String auxiliar, String organizationId, String flag, String tipoRol)
			throws GenericEntityException {

		EntityCondition conditions = EntityCondition.makeCondition(
				EntityOperator.AND, EntityCondition.makeCondition("partyId",
						EntityOperator.EQUALS, organizationId), EntityCondition
						.makeCondition("auxiliarId", EntityOperator.EQUALS,
								auxiliar), EntityCondition
						.makeCondition("flagCierre", EntityOperator.EQUALS,
								flag));
		
		if (tipoRol.equals("A"))
			conditions.makeCondition("auxiliarRoleTypeId",
					EntityOperator.EQUALS,
					obtenRolAuxiliar(delegator, auxiliar));
		else
			conditions.makeCondition("auxiliarRoleTypeId",
					EntityOperator.EQUALS,
					obtenRolProducto(delegator, auxiliar));
		
		Debug.log("condiciones: "+ conditions);

		List<GenericValue> valores = delegator.findByCondition(
				"ControlAuxiliar", conditions, null, null);
		
		Debug.log("valores: "+valores);

		return valores;

	}
	
	public static String obtenRolAuxiliar(Delegator delegator,
			String auxiliarId) throws GenericEntityException {

		EntityCondition conditions = EntityCondition.makeCondition(
				EntityOperator.AND, EntityCondition.makeCondition("partyId",
						EntityOperator.EQUALS, auxiliarId));
		
		List<GenericValue> roles = delegator.findByCondition(
				"Party", conditions, null, null);

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

		// Cargo
		Debug.log("Cargo");
		//Se actualiza el catalogo, validando que el evento tenga asociado un catalogo en el Cargo y que este sea A 'Auxiliar' o P 'Producto'
		if (auxiliares.get(0)!=null
				&& (auxiliares.get(0).equalsIgnoreCase("A") || auxiliares
						.get(0).equalsIgnoreCase("P"))) {
			String naturaleza = naturaleza(delegator, cuentaCargo);
			String tipoCatalogo;
			String catalogoId;
			if (auxiliares.get(0).equalsIgnoreCase("A")) {
				tipoCatalogo = obtenRolAuxiliar(delegator, auxiliar);
				catalogoId = auxiliar;

			} else {
				tipoCatalogo = obtenRolProducto(delegator, producto);
				catalogoId = producto;
			}
			// Obtenemos Saldo del catalogo.
			GenericValue catalogo = obtenCatalogo(delegator, catalogoId,
					tipoCatalogo, organizationId, auxiliares.get(0), periodo);
			Debug.log("montoInicial del catalogo "+ catalogo
							.getBigDecimal("monto"));
			catalogo.set("monto",
					naturaleza.equalsIgnoreCase("DEBIT") ? catalogo
							.getBigDecimal("monto").add(monto) : catalogo
							.getBigDecimal("monto").subtract(monto));
			Debug.log(naturaleza.equalsIgnoreCase("DEBIT") ? "+" : "-" + monto);
			Debug.log("montoFinal del catalogo "+ catalogo
					.getBigDecimal("monto"));
			delegator.store(catalogo);
		}

		// Abono
		Debug.log("Abono");
		//Se actualiza el catalogo, validando que el evento tenga asociado un catalogo en el Abono y que este sea A 'Auxiliar' o P 'Producto'
		if (auxiliares.get(1)!=null
				&& (auxiliares.get(1).equalsIgnoreCase("A") || auxiliares
						.get(1).equalsIgnoreCase("P"))) {
			String naturaleza = naturaleza(delegator, cuentaAbono);
			String tipoCatalogo;
			String catalogoId;
			if (auxiliares.get(1).equalsIgnoreCase("A")) {
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
					tipoCatalogo, organizationId, auxiliares.get(1), periodo);
			Debug.log("montoInicial del catalogo "+ catalogo
					.getBigDecimal("monto"));
			catalogo.set("monto",
					naturaleza.equalsIgnoreCase("CREDIT") ? catalogo
							.getBigDecimal("monto").add(monto) : catalogo
							.getBigDecimal("monto").subtract(monto));
			Debug.log(naturaleza.equalsIgnoreCase("CREDIT") ? "+" : "-" + monto);
			Debug.log("montoFinal del catalogo "+ catalogo
					.getBigDecimal("monto"));
			delegator.store(catalogo);
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
			String idTipoDoc, String clave, String mes, BigDecimal monto)
			throws GenericEntityException {
		Debug.log("Entra monto insuficiencia");
		GenericValue documento = obtenDocumento(delegator, idTipoDoc);
		List<GenericValue> generics = delegator.findByAnd(
				"ControlPresupuestal", UtilMisc.toMap("clavePresupuestal",
						clave, "mesId", mes, "momentoId",
						documento.getString("mComparativo")));
		GenericValue generic = generics.get(0);
		return "No Existe Suficiencia Presupuestal: "+generic.getBigDecimal("monto").subtract(monto).toString();
	}
}