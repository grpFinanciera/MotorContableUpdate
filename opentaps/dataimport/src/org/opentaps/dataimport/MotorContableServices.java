package org.opentaps.dataimport;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.LineaMotorContable;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.base.entities.TipoDocumento;
import org.opentaps.domain.DomainsLoader;
import org.opentaps.foundation.action.ActionContext;
import org.opentaps.foundation.infrastructure.Infrastructure;
import org.opentaps.foundation.infrastructure.User;
import org.opentaps.foundation.repository.RepositoryException;

import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;

public final class MotorContableServices extends MotorContable {
	private static Map<String, String> naturalezaCuentas;
	private static String MODULE = MotorContableServices.class.getName();

	/**
	 * Registra todo el impacto contable y presupuestal en las cuentas del Plan
	 * de Cuentas, tambien valida las claves presupuestales.
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map registroContable_Presupuestal(DispatchContext dctx,
			Map context) throws Exception {
		Debug.log("Entra a registroContable_Presupuestal");
		Delegator delegator = dctx.getDelegator();

		List<String> cvePres = new ArrayList<String>();
		String clavePresupuestal = "";
		String tipoClave = (String) context.get("tipoClave");// Tipo de transaccion a registrar (INGRESO o EGRESO)
		String idTipoDocumento = (String) context.get("idTipoDoc");
		Timestamp fechaRegistro = (Timestamp) context.get("fechaTransaccion");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		String organizationPartyId = (String) context.get("organizationPartyId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String partyId = null;
		BigDecimal monto = (BigDecimal) context.get("monto");
		String agrupador = (String) context.get("referenciaDocumento");
		String currency = (String) context.get("currency");
		String secuencia = (String) context.get("secuencia");
		String agrupadorOrigen = (String) context.get("agrupadorOrigen");
		String tipoPolizaId = (String) context.get("tipoPolizaId");
		String tipoTransaccion = (String) context.get("tipoTransaccion");

		String auxiliarCargoId = (String) context.get("caParty");
		String auxiliarAbonoId = (String) context.get("caProductId");
		
		Debug.log("Producto: " + auxiliarAbonoId + " Auxiliar: " + auxiliarCargoId);
		Debug.log("Agrupador: " + agrupador + " tipoPolizaId: " + tipoPolizaId);
		
		String auxiliar = null;
		String producto = null;

		String invoiceId = (String) context.get("invoiceId");
		String paymentId = (String) context.get("paymentId");
		String shipmentId = (String) context.get("shipmentId");
		String receiptId = (String) context.get("receiptId");
		String workEffortId = (String) context.get("workEffortId");
		String roleTypeId = (String) context.get("roleTypeId");
		String descripcion = (String) context.get("descripcion");
		String orderId = (String) context.get("orderId");
		String contratoId = (String) context.get("contratoId");
		

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(fechaRegistro.getTime());
		String ciclo = new SimpleDateFormat("yyyy").format(fechaContable);
		String modulo = buscarModulo(delegator, idTipoDocumento);

		try {

			// validamos si hay periodos abiertos
			List<GenericValue> vPeriodos = obtenPeriodos(delegator,
					organizationPartyId, fechaContable);
			if (vPeriodos.isEmpty())
				return ServiceUtil.returnError("El o los periodos ha(n) sido cerrado(s) o no ha(n) sido creado(s)");

			Map<String, String> cuentas = FastMap.newInstance();
			GenericValue estructura = buscaEstructuraClave(delegator, ciclo,
					tipoClave);

			List<String> auxiliares = obtenAuxiliares(delegator,
					idTipoDocumento);

			if (auxiliares.isEmpty())
				return ServiceUtil.returnError("No estan espicificados los catalogos auxiliares en el documento "
								+ idTipoDocumento);
			
			if ((auxiliares.get(0).equals("A") && auxiliarCargoId == null)
					|| (auxiliares.get(1).equals("A") && auxiliarAbonoId == null)) 
				return ServiceUtil
						.returnError("Es necesario ingresar el auxiliar");
				
			if ((auxiliares.get(0).equals("P") && auxiliarCargoId == null)
					|| (auxiliares.get(1).equals("P") && auxiliarAbonoId == null))
				return ServiceUtil
						.returnError("Es necesario ingresar el producto");
			
			if (auxiliares.get(0).equals("A") || auxiliares.get(1).equals("A"))
				auxiliar = auxiliarCargoId;
			if (auxiliares.get(0).equals("P") || auxiliares.get(1).equals("P"))
				producto = auxiliarAbonoId;


			for (int i = 1; i < 16; i++) {
				String clasif = (String) context.get("clasificacion" + i);
				if (clasif != null && !clasif.isEmpty())
					cvePres.add(clasif);
			}

			String validacion = validaClave(delegator, estructura, cvePres);

			if (validacion != null && !validacion.isEmpty())
				return ServiceUtil.returnError(validacion);

			for (String cve : cvePres) {
				clavePresupuestal += cve.trim();
			}
			
			Debug.log("ClavePres CHRV: " + clavePresupuestal);

			GenericValue cvePrespu = obtenerCvePresupuestal(delegator,
					clavePresupuestal);

			if (cvePrespu == null || cvePrespu.isEmpty())
				return ServiceUtil
						.returnError("No existe la clave presupuestal");

			if (tipoClave.equalsIgnoreCase("INGRESO"))
				cuentas = cuentasIngreso(delegator, idTipoDocumento,
						buscaCri(delegator, estructura, cvePres));
			else if (tipoClave.equalsIgnoreCase("EGRESO"))
				cuentas = cuentasEgreso(delegator, idTipoDocumento,
						buscaCog(delegator, estructura, cvePres),
						buscaTipoGasto(delegator, estructura, cvePres));

			if (validarCuentasRegistro(delegator,
					cuentas.get("Cuenta Cargo Presupuesto"),
					cuentas.get("Cuenta Abono Presupuesto"),
					cuentas.get("Cuenta Cargo Contable"),
					cuentas.get("Cuenta Abono Contable")) == false)
				return ServiceUtil.returnError("Alguna(s) de la(s) cuenta(s) no es de registro");

			if (controlPresupuestal(delegator, idTipoDocumento,
					clavePresupuestal, fechaContable, monto, ciclo) == false)
				return ServiceUtil.returnError(clavePresupuestal+" "+montoInsuficiencia(delegator, idTipoDocumento,
						clavePresupuestal, fechaContable, monto));

			List<GenericValue> listaTrans = creaTransaccion(delegator, modulo,
					fechaRegistro, fechaContable, idTipoDocumento,
					userLogin.getString("userLoginId"), organizationPartyId,
					partyId, estructura, monto, agrupador, secuencia, cvePres,
					cuentas, currency, tipoClave, clavePresupuestal, invoiceId,
					paymentId, shipmentId, receiptId, workEffortId, roleTypeId,
					auxiliar, producto, agrupadorOrigen, tipoPolizaId, descripcion, tipoTransaccion, orderId,
					null, null, null, contratoId);

			Map result = ServiceUtil.returnSuccess();
			result.put("listaTrans", listaTrans);
			return result;

		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}

	/**
	 * Metodo utilizado para hacer un registro contable sin impacto presupuestal
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map registroContable(DispatchContext dctx, Map context) {
		Debug.log("Entra a registroContable");

		Delegator delegator = dctx.getDelegator();

		try {

			List<String> cvePres = new ArrayList<String>();
			String clavePresupuestal = null;
			String tipoClave = (String) context.get("tipoClave");// Tipo de
																	// transaccion
																	// a
																	// registrar
																	// (INGRESO
																	// o EGRESO)
			String idTipoDocumento = (String) context.get("idTipoDoc");
			Timestamp fechaRegistro = (Timestamp) context
					.get("fechaTransaccion");
			Timestamp fechaContable = (Timestamp) context.get("fechaContable");
			String organizationPartyId = (String) context
					.get("organizationPartyId");
			GenericValue userLogin = (GenericValue) context.get("userLogin");
			// String partyId = (String) context.get("partyId");
			BigDecimal monto = (BigDecimal) context.get("monto");
			String agrupador = (String) context.get("referenciaDocumento");
			String currency = (String) context.get("currency");
			String secuencia = (String) context.get("secuencia");
			String auxiliar = null;
			if (context.containsKey("banderaAuxiliar") &&
				(Boolean) context.get("banderaAuxiliar"))
			{
				auxiliar = (String) context.get("auxiliar");
			}
			String producto = (String) context.get("caProductId");
			
			String invoiceId = (String) context.get("invoiceId");
			String paymentId = (String) context.get("paymentId");
			String shipmentId = (String) context.get("shipmentId");
			String receiptId = (String) context.get("receiptId");
			String workEffortId = (String) context.get("workEffortId");
			String roleTypeId = (String) context.get("roleTypeId");
			String descripcion = (String) context.get("descripcion");
			String orderId = (String) context.get("orderId");
			String inventoryItemId = (String) context.get("inventoryItemId");
			String physicalInventoryId = (String) context.get("physicalInventoryId");
			String fixedAssetId = (String) context.get("fixedAssetId");			

			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(fechaRegistro.getTime());
			String modulo = buscarModulo(delegator, idTipoDocumento);

			GenericValue estructura = GenericValue.create(delegator
					.getModelEntity("EstructuraClave"));

			// if(subconcepto == null || subconcepto.isEmpty())
			// if(concepto == null || concepto.isEmpty())
			// return
			// ServiceUtil.returnError("Tiene que ingresar un concepto o subconcepto");

			Map<String, String> cuentas = FastMap.newInstance();

			cuentas = cuentasDiarias(delegator, idTipoDocumento);

			List<GenericValue> listaTrans = creaTransaccion(delegator, modulo,
					fechaRegistro, fechaContable, idTipoDocumento,
					userLogin.getString("userLoginId"), organizationPartyId,
					null, estructura, monto, agrupador, secuencia, cvePres,
					cuentas, currency, tipoClave, clavePresupuestal, invoiceId,
					paymentId, shipmentId, receiptId, workEffortId, roleTypeId,
					auxiliar, producto, null, null, descripcion, null, orderId,
					inventoryItemId, physicalInventoryId, fixedAssetId, null);

			Map result = ServiceUtil.returnSuccess();
			result.put("listaTrans", listaTrans);
			return result;

		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		} catch (RepositoryException e) {
			return ServiceUtil.returnError(e.getMessage());
		}

	}

	/**
	 * Metodo que da reversa al presupuesto
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map reversaPresupuestal(DispatchContext dctx, Map context)
			throws GenericEntityException {

		Delegator delegator = dctx.getDelegator();

		String acctgTransId = (String) context.get("acctgTransId");

		GenericValue acctgTrans = delegator.findByPrimaryKey("AcctgTrans",
				UtilMisc.toMap("acctgTransId", acctgTransId));
		Timestamp fechaContable = acctgTrans.getTimestamp("postedDate");
		String clave = acctgTrans.getString("clavePresupuestal");
		String ciclo = new SimpleDateFormat("yyyy").format(fechaContable);
		BigDecimal monto = acctgTrans.getBigDecimal("postedAmount");
		monto = monto.negate();// Se pone negativo para hacer el movimiento
								// contrario
		GenericValue tipoDoc = acctgTrans.getRelatedOne("TipoDocumento");
		String m_comparativo = tipoDoc.getString("mComparativo");
		String mEjecutar1 = tipoDoc.getString("mEjecutar1");
		String mEjecutar2 = tipoDoc.getString("mEjecutar2");

		if (m_comparativo != null)
			restaMonto(delegator, clave, m_comparativo, fechaContable, monto);
		if (mEjecutar1 != null)
			sumaMonto(delegator, clave, mEjecutar1, fechaContable, monto, ciclo);
		if (mEjecutar2 != null)
			sumaMonto(delegator, clave, mEjecutar2, fechaContable, monto, ciclo);

		Map result = ServiceUtil.returnSuccess();
		result.put("acctgTransId", acctgTransId);
		return result;
	}

	/**
	 * Metodo que crea una poliza para agrupar claves presupuestales
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map crearAgrupador(DispatchContext dctx, Map context)
			throws GenericEntityException {

		Delegator delegator = dctx.getDelegator();
		final ActionContext ac = new ActionContext(context);

		String idTipoDoc = (String) context.get("idTipoDoc");
		String partyId = (String) context.get("partyId");
		String bancoId = null;
		String cuentaBancariaId = null;
		Timestamp fechaTransaccion = (Timestamp) context
				.get("fechaTransaccion");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		String transaccionRapida = (String) context.get("transaccionRapida");
		String comentario = (String) context.get("comentario");
		String agrupadorPolizaAux = "";
		String esTranNoReg = "N";
		
		if (comentario == null)
			comentario = (String) context.get("description");

		if (transaccionRapida != null) {
			esTranNoReg = "Y";
		}
		
		String modulo = buscarModulo(delegator, idTipoDoc);
		Debug.log("AAAModulo: " + modulo);
		if(modulo.equals("TESORERIA_CXP_PAG") || modulo.equals("DEVOLUCION_DINERO"))
		{	bancoId = (String) context.get("bancoId");
			cuentaBancariaId = (String) context.get("cuentaBancariaId");
			Debug.log("MMMbancoId: " + bancoId);
			Debug.log("MMMcuentaBancariaId: " + cuentaBancariaId);
			if(bancoId.equals(" "))
				return ServiceUtil
						.returnError("El parametro Banco no puede estar vacio");
			if(cuentaBancariaId.equals(" "))
				return ServiceUtil
						.returnError("El parametro Cuenta Bancaria no puede estar vacio");							
		}

		try {
			if (idTipoDoc.equals(" "))
				return ServiceUtil
						.returnError("El parametro Tipo de Documento no puede estar vacio");
			if (partyId.equals(" "))
				return ServiceUtil
						.returnError("El parametro Organizaci\u00f3n no puede estar vacio");
			if (fechaTransaccion == null)
				return ServiceUtil
						.returnError("El parametro Fecha de Transacci\u00f3n no puede estar vacio");
			if (fechaContable == null)
				return ServiceUtil
						.returnError("El parametro Fecha Contable no puede estar vacio");

			//validamos si hay periodos abiertos
			Date fechaContablePeriodos = fechaContable;
			Debug.log("Omar-fechaContablePeriodos: " + fechaContablePeriodos);
			Debug.log("Omar-fechaContable: " + fechaContable);
    		List<GenericValue> vPeriodos = obtenPeriodos(delegator,
    				partyId, fechaContablePeriodos);
    		 if (vPeriodos.isEmpty())
    			 return ServiceUtil
 						.returnError("El o los periodos ha(n) sido cerrado(s) o no han sido creados");
    		 
			GenericValue polizaAgrupa = delegator.makeValue("PolizaAgrupa");

			polizaAgrupa.setNonPKFields(context);
			agrupadorPolizaAux = delegator.getNextSeqId("PolizaAgrupa");
			polizaAgrupa.put("idPolizaAgrupador", agrupadorPolizaAux);
			polizaAgrupa.put("partyId", partyId);
			polizaAgrupa.put("statusId", "1");
			polizaAgrupa.put("fechaTransaccion", fechaTransaccion);
			polizaAgrupa.put("fechaContable", fechaContable);
			polizaAgrupa.put("idTipoDoc", idTipoDoc);
			polizaAgrupa.put("transaccionRapida", esTranNoReg);
			polizaAgrupa.put("bancoId", bancoId);
			polizaAgrupa.put("cuentaBancariaId", cuentaBancariaId);
			polizaAgrupa.put("comentario", comentario);			
			delegator.create(polizaAgrupa);
			Map result = ServiceUtil.returnSuccess();
			result.put("agrupadorPolizaAux", agrupadorPolizaAux);
			result.put("comentario", comentario);			
			ac.put("polizaAgrupaList", agrupadorPolizaAux);
			return result;
		} catch (GeneralException e) {
			return ServiceUtil.returnError(e.toString());
		}
	}

	/**
	 * Metodo que actualiza el status de una clave presupuestal para ser
	 * afectada o no
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map actualizarStatusAcctg(DispatchContext dctx, Map context)
			throws GenericEntityException {
		Delegator delegator = dctx.getDelegator();
		final ActionContext ac = new ActionContext(context);
		final Locale locale = ac.getLocale();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		User user = new User(userLogin);
		Infrastructure infrastructure = new Infrastructure(dispatcher);
		DomainsLoader dl = new DomainsLoader(infrastructure, user);

		String clavePresupuestal = (String) context.get("clavePresupuestal");
		String numPolizaId = (String) context.get("numPolizaId");
		String acctgTransId = (String) context.get("acctgTransId");
		String idTipoDoc = (String) context.get("idTipoDoc");
		String tipoPolizaId = (String) context.get("tipoPolizaId");
		String tag_index = (String) context.get("tag_index");

		EntityCondition conditions;
		conditions = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition(
						TipoDocumento.Fields.predecesor.name(),
						EntityOperator.EQUALS, idTipoDoc));
		List<GenericValue> tipoDocActual = delegator.findByCondition(
				"TipoDocumento", conditions, UtilMisc.toList("idTipoDoc"),
				UtilMisc.toList("idTipoDoc"));

		Iterator<GenericValue> itemsTipoDocActual = tipoDocActual.iterator();
		itemsTipoDocActual.hasNext();
		GenericValue itemTipoDoc = itemsTipoDocActual.next();
		String idTipoDocumento = itemTipoDoc.getString("idTipoDoc");

		try {
			GenericValue polizaAcctgTrans = delegator.findByPrimaryKey(
					"AcctgTrans", UtilMisc.toMap("acctgTransId", acctgTransId));
			String claveContabilizada = polizaAcctgTrans.getString(
					"claveContabilizada").trim();
			if (claveContabilizada.equals("N"))
				polizaAcctgTrans.set("claveContabilizada", "I");
			else
				polizaAcctgTrans.set("claveContabilizada", "N");

			delegator.store(polizaAcctgTrans);

			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("numPolizaId", numPolizaId);
			ac.put("numPolizaId", numPolizaId);
			result.put("idTipoDoc", idTipoDocumento);
			ac.put("idTipoDoc", idTipoDocumento);
			result.put("tipoPolizaId", tipoPolizaId);
			ac.put("tipoPolizaId", tipoPolizaId);
			return result;
		} catch (GeneralException e) {
			return ServiceUtil.returnError(e.toString());
		}
	}
	
	/**
	 * Realiza el cierre anual del presupuesto
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException
	 * @throws RepositoryException
	 * @throws GenericServiceException
	 */
	public static Map<String,Object> cierrePresupuestal(DispatchContext dctx, Map<String,Object> context) {
		
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		String ciclo = (String) context.get("enumCode");
		String organizationId = (String) context.get("organizationPartyId");
		
		try{
			
		Map<String, Object> input = FastMap.newInstance();
		Map<String, Object> mapCuentas = dispatcher.runSync("getNaturalezaCuentas", input);
		
		if (ServiceUtil.isError(mapCuentas)) {
			Debug.logError("Hubo Error al obtener la naturaleza de las cuentas",MODULE);
			return ServiceUtil.returnError(ServiceUtil.getErrorMessage(mapCuentas));
		}
		naturalezaCuentas = UtilGenerics.checkMap(mapCuentas.get("naturalezaCuentas"));
		
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		
		Debug.logInfo("ciclo.- " + ciclo, MODULE);
		Debug.logInfo("organizationId.- " + organizationId, MODULE);
		
		// Se valida que exista un traspaso presupuestal del mes de noviembre.
		GenericValue mesTraspasado = delegator.findByPrimaryKey("MesTraspaso", UtilMisc.toMap("mesId", "11", "ciclo", ciclo));
				
		if(mesTraspasado==null){
			return ServiceUtil.returnError("No se puede hacer el cierre presupuestal,"
					+ " debido a que no se ha traspasado el mes de Noviembre.");
		}
		
		//Se cierra el presupuesto de diciembre.
		cierraDiciembre(ciclo, delegator);
		
		//Se verifica si se tiene Superavit o deficit
		//Obtenemos el Saldo de las cuentas segun el anio a cerrar.
		List<GenericValue> periodos = delegator.findByAnd("CustomTimePeriod", "periodName", ciclo,"periodTypeId","FISCAL_YEAR");
		
		if(periodos.isEmpty()){
			return ServiceUtil
					.returnError("No existe ciclo a cerrar.");
		}
		
		Timestamp fechaContable = new Timestamp(periodos.get(0).getDate("thruDate").getTime() - 1000);
		
		List<EntityCondition> searchConditions = new FastList<EntityCondition>();
		searchConditions.add(EntityCondition.makeCondition("customTimePeriodId",
				EntityOperator.EQUALS, periodos.get(0).getString("customTimePeriodId")));
		searchConditions.add(EntityCondition.makeCondition("organizationPartyId",
				EntityOperator.EQUALS, organizationId));
		searchConditions.add(EntityCondition.makeCondition("glAccountId",
				EntityOperator.IN, UtilMisc.toList("8.1.4","8.1.5","8.2.5","8.2.6","8.2.7")));

		List<String> fieldsToSelect = UtilMisc.toList("glAccountId", "postedDebits", "postedCredits");
		List<String> orderBy = UtilMisc.toList("glAccountId");
		
		List<GenericValue> cuentas = delegator.findByCondition("GlAccountHistory", EntityCondition.makeCondition(
				searchConditions, EntityOperator.AND), fieldsToSelect, orderBy);
		
		if(periodos.isEmpty()){
			return ServiceUtil.returnError("No se encontraron las cuentas presupuestales para el cierre presupuestal.");
		}
		
		BigDecimal balance = BigDecimal.ZERO;
		for(GenericValue cuenta : cuentas){
			//Sumamos Credits y restamos Debits.
			balance = balance.add(cuenta.getBigDecimal("postedCredits")!=null?cuenta.getBigDecimal("postedCredits"):BigDecimal.ZERO);
			balance = balance.subtract(cuenta.getBigDecimal("postedDebits")!=null?cuenta.getBigDecimal("postedDebits"):BigDecimal.ZERO);
		}
		
		String eventoId = balance.compareTo(BigDecimal.ZERO)>=0?"CIPE-01":"CIPE-02";
		Debug.logInfo("Evento a ejecutar.- "+ eventoId,MODULE);
		orderBy = UtilMisc.toList("secuencia");
		List<GenericValue> lineasContables = delegator.findByAnd("LineaContable", UtilMisc.toMap("acctgTransTypeId",eventoId), orderBy);
		GenericValue trans=null;
		for(int i =0; i<lineasContables.size();i++){
			List<LineaMotorContable> lineas = new FastList<LineaMotorContable>();
			LineaMotorContable linea = new LineaMotorContable();
				
				GenericValue lc = lineasContables.get(i);
				
				//buscamos el saldo de la cuenta a cancelar.
				lc.set("monto", montoCancelar(delegator, lc.getString("cuentaCancelar"), organizationId, periodos.get(0).getString("customTimePeriodId")));
	
				// Creamos el mapaContable.
				Map<String, GenericValue> mapaContable = new HashMap<String, GenericValue>();
				mapaContable.put(lc.getString("descripcion"), lc);
				linea.setLineasContables(mapaContable);
				lineas.add(linea);
				
				if(i==0){
					input = FastMap.newInstance();
					input.put("fechaRegistro", UtilDateTime.nowTimestamp());
					input.put("fechaContable", fechaContable);
					input.put("eventoContableId", eventoId);
					input.put("usuario", userLogin.getString("userLoginId"));
					input.put("organizationId", organizationId);
					input.put("currency", "MXN");
					input.put("descripcion", "Cierre Presupuestal a\u00f1o "+ ciclo);
					input.put("lineasMotor", lineas);
	
					Map<String, Object> result = dispatcher.runSync("creaTransaccionMotor", input);
	
					if (ServiceUtil.isError(result)) {
						Debug.log("Hubo Error en poliza");
						return ServiceUtil
								.returnError(ServiceUtil.getErrorMessage(result));
					}
					trans = (GenericValue) result.get("transaccion");
				}else{
					input = FastMap.newInstance();
					input.put("eventoContableId", eventoId);
					input.put("transaccion", trans);
					input.put("lineasMotor", lineas);
					Map<String, Object> result = dispatcher.runSync("agregaEventoTransaccion", input);
	
					if (ServiceUtil.isError(result)) {
						Debug.log("Hubo Error en poliza");
						return ServiceUtil
								.returnError(ServiceUtil.getErrorMessage(result));
					}
				}
			}
			
			return ServiceUtil.returnSuccess("Se ha realizado el cierre presupuestal.");
		} catch (GenericServiceException | NullPointerException | GenericEntityException e){
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
	}

//	public static BigDecimal montoCancelar(Delegator delegator,
//			Map<String, String> cuentas, String organizationId)
//			throws GenericEntityException {
//		// Se busca la naturaleza de la cuenta cargo.
//		String naturalezaCuenta = naturaleza(delegator,
//				cuentas.get("Cuenta Cargo Presupuesto"));
//		GenericValue glAccountOrganizationParty;
//
//		// Si la cuenta es de naturaleza contraria, se asigna el monto.
//		if (!naturalezaCuenta.equalsIgnoreCase("DEBIT")) {
//			glAccountOrganizationParty = delegator.findByPrimaryKey(
//					"GlAccountOrganization", UtilMisc.toMap("glAccountId",
//							cuentas.get("Cuenta Cargo Presupuesto"),
//							"organizationPartyId", organizationId));
//			Debug.log("Se toma monto cuenta cargo.- "
//					+ cuentas.get("Cuenta Cargo Presupuesto"));
//		} else {
//			// De lo contrario se busca el saldo de la otra cuenta.
//			glAccountOrganizationParty = delegator.findByPrimaryKey(
//					"GlAccountOrganization", UtilMisc.toMap("glAccountId",
//							cuentas.get("Cuenta Abono Presupuesto"),
//							"organizationPartyId", organizationId));
//			Debug.log("Se toma monto cuenta abono.- "
//					+ cuentas.get("Cuenta Abono Presupuesto"));
//		}
//
//		return glAccountOrganizationParty.getBigDecimal("postedBalance") != null ? glAccountOrganizationParty
//				.getBigDecimal("postedBalance") : BigDecimal.ZERO;
//	}
	
	public static BigDecimal montoCancelar(Delegator delegator,
			String cuenta, String organizationId, String periodoId) throws GenericEntityException {
		
		GenericValue generic = delegator.findByPrimaryKey("GlAccountHistory",
				UtilMisc.toMap("glAccountId", cuenta, "organizationPartyId",
						organizationId, "customTimePeriodId", periodoId));
		
		if (generic == null) {
			return BigDecimal.ZERO;
		}
		
		BigDecimal monto;
		//Segun la naturaleza de la cuenta regresamos el monto.
		if(naturaleza(cuenta).equalsIgnoreCase("DEBIT")){
			monto = generic.getBigDecimal("postedDebits")!=null?generic.getBigDecimal("postedDebits"):BigDecimal.ZERO;
			return generic.getBigDecimal("postedCredits")!=null?monto.subtract(generic.getBigDecimal("postedCredits")):monto;
		}else{
			//CREDIT
			monto = generic.getBigDecimal("postedCredits")!=null?generic.getBigDecimal("postedCredits"):BigDecimal.ZERO;
			return generic.getBigDecimal("postedDebits")!=null?monto.subtract(generic.getBigDecimal("postedDebits")):monto;			
		}
	}
	
	/**
	 * Metodo que actualiza los montos de las organizaciones
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException
	 */
	@SuppressWarnings("unchecked")
	public static Map actualizaGlAccountOrganization(DispatchContext dctx, Map context){
		Delegator delegator = dctx.getDelegator();
		
		BigDecimal monto = (BigDecimal) context.get("monto");
		String cuenta = (String) context.get("cuenta");
		String organizacionPartyId = (String) context.get("organizacionPartyId");
		String partyId = (String) context.get("partyId");
		String naturaleza = (String) context.get("naturaleza");
		Date postedDate = (Date) context.get("postedDate");
		
		try {
			actualizaGlAccountOrganization(delegator, monto, cuenta, organizacionPartyId, partyId, naturaleza);
			List<GenericValue> periodos = obtenPeriodos(delegator, organizacionPartyId, postedDate);
			List<GenericValue> histories = actualizaGlAccountHistories(delegator, periodos, cuenta, monto, naturaleza);
			Debug.log("Se impactan las histories regresadas");
			for (GenericValue history : histories) {
				delegator.createOrStore(history);
			}
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
		
		return ServiceUtil.returnSuccess();
		
	}
	
	/**
	 * Obtiene la naturaleza de la cuenta
	 * @param dctx
	 * @param context
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map obtenerNaturalezaCuenta(DispatchContext dctx, Map context)
	{
        Delegator delegator = dctx.getDelegator();
        try
        {
	        String naturaleza = naturaleza(delegator, (String)context.get("glAccountId"));
			Map result = ServiceUtil.returnSuccess();
			result.put("naturaleza", naturaleza);
			return result;
		}
        catch (GenericEntityException e)
        {
			return ServiceUtil.returnError(e.getMessage());
		}
		
	}
	
	/**
	 * Genera las transacciones para el cierre contable
	 * @param dctx
	 * @param context
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map generaTransaccionCierreContable(DispatchContext dctx, Map context)
	{
        Delegator delegator = dctx.getDelegator();
        try
        {
        	List<GenericValue> transacciones = FastList.newInstance();
	        transacciones = creaTransaccionCierreCont(delegator,
	    			(Timestamp)context.get("fechaRegistro"), (Timestamp)context.get("fechaContable"),
	    			(String)context.get("tipoDocumento"), (String)context.get("usuario"),
	    			(String)context.get("organizationId"), null, (BigDecimal)context.get("monto"),
	    			(Map<String, String>)context.get("cuentas"), (String)context.get("currency"),
	    			null, null, null, (String)context.get("agrupador"));
			Map result = ServiceUtil.returnSuccess();
			result.put("transacciones", transacciones);
			return result;
		}
        catch (GenericEntityException e)
        {
			return ServiceUtil.returnError(e.getMessage());
		}
        catch (RepositoryException e)
        {
			return ServiceUtil.returnError(e.getMessage());
		}
		
	}
	
	public static Map actualizaCatalogos(DispatchContext dctx, Map context)
	{
        Delegator delegator = dctx.getDelegator();
        try
        {
        	Debug.log("Entra a servicio actualizaCatalogos");
        	//Obtenemos los parametros.
        	String tipoDoc = (String)context.get("idTipoDoc");
//        	String partyId = (String)context.get("partyId");
        	String organizationId = (String)context.get("organizationPartyId");
        	String auxiliar = (String)context.get("auxiliar");
        	String producto = (String)context.get("producto");
        	BigDecimal monto = (BigDecimal)context.get("monto");
        	String cargo = (String)context.get("cargo");
        	String abono = (String)context.get("abono");
        	String auxiliarAbono = (String)context.get("auxiliarAbono");
        	String productoAbono = (String)context.get("productoAbono");
        	String reversa = (String)context.get("reversa");
        	Timestamp periodo = (Timestamp) context.get("fechaContable");
        	
        	if(reversa!=null){
        		actualizaCatalogos(delegator, auxiliar, producto, monto,
						organizationId, tipoDoc, cargo, abono, periodo,auxiliarAbono, productoAbono,true);
        	}else{
        		actualizaCatalogos(delegator, auxiliar, producto, monto,
						organizationId, tipoDoc, cargo, abono, periodo,auxiliarAbono, productoAbono);	
        	}
        	
        	return ServiceUtil.returnSuccess();
		}
        catch (Exception e)
        {
			return ServiceUtil.returnError(e.getMessage());
		}		
	}
	
	public static Map obtenSaldoInicialFinal(DispatchContext dctx, Map context)
	{
        Delegator delegator = dctx.getDelegator();
        try
        {
        	Debug.log("Entramos a servicio obtenSaldoInicialFinal");
        	//Obtenemos los parametros.
        	String tipoDoc = (String)context.get("idTipoDoc");
        	String flag = (String)context.get("flag");
        	//String auxiliar = (String)context.get("auxiliar");
        	//String producto = (String)context.get("producto");
        	List<BigDecimal> montosAux = (List<BigDecimal>)context.get("montosAux");
        	
        	Debug.log("Llamamos a metodo obtenAuxiliares");
        	List<String> auxiliares = obtenAuxiliares(delegator, tipoDoc);
			String auxiliarC = auxiliares.get(0), auxiliarA = auxiliares.get(1);

			Map result = ServiceUtil.returnSuccess();
			
			if (flag.equals("C")) {
				if (!auxiliarC.equals("N") && montosAux.size() != 0){
					if (montosAux.size() == 2) {
						result.put("saldoInicial", montosAux.get(0));
						result.put("saldoActual", montosAux.get(1));
					} else {
						result.put("saldoInicial", montosAux.get(0));
						result.put("saldoActual", montosAux.get(2));
					}
				}					
			}
			
			if (flag.equals("D")) {
				if (!auxiliarA.equals("N") && montosAux.size() != 0){
					if (montosAux.size() == 2) {
						result.put("saldoInicial", montosAux.get(0));
						result.put("saldoActual", montosAux.get(1));
					} else {
						result.put("saldoInicial", montosAux.get(1));
						result.put("saldoActual", montosAux.get(3));
					}
				}
			}
			return result;	
		}
        catch (Exception e)
        {
			return ServiceUtil.returnError(e.getMessage());
		}		
	}
	
	public static Map actualizaSaldoCuentasBancarias(DispatchContext dctx, Map context){
		Delegator delegator = dctx.getDelegator();
        try
        {
        	Debug.log("Entramos a servicio actualizaSaldoCuentasBancarias");
        	//Obtenemos los parametros.
        	String cuentaBancariaId = (String)context.get("cuentaBancariaId");
        	String modulo = (String)context.get("modulo");
        	BigDecimal monto = (BigDecimal)context.get("monto");
        	String debitCreditFlag = (String)context.get("debitCreditFlag");
        	Timestamp periodo = (Timestamp)context.get("periodo");
        	Debug.log("Omar-debitCreditFlagS: - " + debitCreditFlag);
        	BigDecimal montoFlag = BigDecimal.ZERO;
        	if(debitCreditFlag.equalsIgnoreCase("C"))
        		montoFlag = monto.negate();        	        	
        	else
        		montoFlag=monto;
        	actualizaSaldoCuentasBancarias(delegator, cuentaBancariaId, modulo, montoFlag, periodo);
        	        	
        	Map result = ServiceUtil.returnSuccess();
        	return result;	
		}
        catch (Exception e)
        {
			return ServiceUtil.returnError(e.getMessage());
		}
	}
	
	public static void cierraDiciembre(String ciclo, Delegator delegator)
			throws GenericEntityException {
		String mesId = "12";
		List<GenericValue> movimientoMensual = delegator.findAll("MovimientoMensual");

		// Se crea la lista de los momentos.
		List<GenericValue> controlPresupuestal = new ArrayList<GenericValue>();
		for (GenericValue movimiento : movimientoMensual) {
			controlPresupuestal.addAll(delegator.findByAnd(
					"ControlPresupuestal", "momentoId",
					movimiento.getString("movimientoMensualId"), "mesId",
					mesId, "ciclo", ciclo));
		}

		for (GenericValue control : controlPresupuestal) {
			// Se hace solo si el monto es mayor que 0.
			if (control.getBigDecimal("monto").signum() == 1) {
				// Se crea traspaso.
				GenericValue traspaso = delegator.makeValue("Traspaso");
				traspaso.setNextSeqId();
				traspaso.set("idSecuenciaViene",
						control.getString("idSecuencia"));
				traspaso.set("idSecuenciaVa", control.getString("idSecuencia"));
				traspaso.set("monto", control.getBigDecimal("monto"));
				// GenericValue user = (GenericValue) context.get("userLogin");
				// traspaso.set("usuario", user.getString("userLoginId"));
				traspaso.set("fechaRegistro", UtilDateTime.nowTimestamp());
				delegator.create(traspaso);

				// Poniendo en 0 el registro actual.
				control.set("monto", BigDecimal.ZERO);
				delegator.store(control);
			}
		}
		
		//Registramos el traspaso.
		GenericValue mesTraspasado = delegator.makeValue("MesTraspaso");
		mesTraspasado.set("mesId", mesId);
		mesTraspasado.set("ciclo", ciclo);
		delegator.create(mesTraspasado);
	}
	
	public static String naturaleza(String cuenta)
			throws GenericEntityException {
		Debug.log("Naturaleza");

		return naturalezaCuentas.get(cuenta);
	}
	
}