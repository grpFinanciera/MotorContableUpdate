package com.opensourcestrategies.financials.tesoreria;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.common.util.UtilMessage;

import com.ibm.icu.util.Calendar;
import com.opensourcestrategies.financials.transactions.MotorContableFinanzas;


public class TraspasoPatrimonial extends MotorContableFinanzas{
	private static String MODULE = TraspasoPatrimonial.class.getName();

	@SuppressWarnings({ "rawtypes", "unchecked"})
	public static Map crearTraspaso(DispatchContext dctx, Map context) throws GeneralException, ParseException, Exception {

		
		Delegator delegator = dctx.getDelegator();				
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Debug.log("Mike.- " + context);
		String organizationPartyId = (String) context.get("organizationPartyId");
		String descripcion = (String) context.get("descripcion");
		String currency = (String) context.get("moneda");
		String acctgTransTypeId = (String) context.get("acctgTransTypeId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		Timestamp fechaTrans = new Timestamp(Calendar.getInstance().getTimeInMillis());
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		
		if(fechaTrans.compareTo(fechaContable) < 0)
		{	return UtilMessage.createAndLogServiceError(
				"No es posible realizar un traspaso bancario en una fecha posterior a la actual", MODULE);				
		}
		
		if (organizationPartyId.equals(" ")) {
			return UtilMessage.createAndLogServiceError(
					"El parametro Organizaci\u00f3n no puede estar vacio", MODULE);
		}

		if (fechaContable.equals(" ")) {
			return UtilMessage.createAndLogServiceError(
					"El parametro Fecha Contable no puede estar vacio", MODULE);
		}
		
		if (acctgTransTypeId == null) {
			return UtilMessage.createAndLogServiceError(
					"El evento no puede estar vacio", MODULE);
		}
		
		/************************************/
        /******** Motor Contable ************/
        /************************************/
		
		 Map catalogoCargoMap = (Map) context.get("catalogoCargoContMap");
         Map catalogoAbonoMap = (Map) context.get("catalogoAbonoContMap");
         Map montoMap = (Map) context.get("montoMap");
         Map clavePresupuestalMap = (Map) context.get("clavePresupuestalMap");
         String descrSinEspacios = "", catalogoCargo = "", catalogoAbono = "";
         
         List<String> orderBy = UtilMisc.toList("secuencia");
 		 List<GenericValue> listaLineasContables = delegator.findByAndCache("LineaContable", UtilMisc.toMap("acctgTransTypeId",acctgTransTypeId),orderBy);
 		 for (GenericValue lineaCont : listaLineasContables) {
 			descrSinEspacios = lineaCont.getString("descripcion").replaceAll("\\s","")+0;
 			Debug.log("Descripcioooon: " + descrSinEspacios);
 			catalogoCargo = lineaCont.getString("catalogoCargo");
 			catalogoAbono = lineaCont.getString("catalogoAbono");
 		 }
 		 
 		clavePresupuestalMap.put("0", "");
        context.put("clavePresupuestalMap", clavePresupuestalMap);
        
        Map<String, Object> input = FastMap.newInstance();
        input.put("eventoContableId", acctgTransTypeId);
        input.put("tipoClaveId", "");
        input.put("fechaRegistro", fechaTrans);
        input.put("fechaContable", fechaContable);
        input.put("currency", currency);
        input.put("usuario", userLogin.getString("userLoginId"));
        input.put("organizationId", organizationPartyId);
        input.put("descripcion", descripcion);
        input.put("roleTypeId", "BILL_FROM_VENDOR");
        input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(acctgTransTypeId, delegator, context, null,null, null, null));
        
        Map<String, Object> output = dispatcher.runSync("creaTransaccionMotor", input);
    	if(ServiceUtil.isError(output)){
    		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
    	}
    	
    	GenericValue transaccion = (GenericValue) output.get("transaccion");

			//Actualiza los saldos de las cuentas
//			if(catalogoAbono.equals("BANCO")) {
//				
//				GenericValue cuentaAbono = delegator.findByPrimaryKey("CuentaBancaria",
//	    				UtilMisc.toMap("cuentaBancariaId", catalogoAbonoMap.get(descrSinEspacios)));
//	        	
//	        	Debug.log("Mike cuenta cargo: " + cuentaAbono);
//				
//				GenericValue traspasoBanco = delegator.makeValue("TraspasoBanco");
//				traspasoBanco.setNonPKFields(context);			
//				traspasoBanco.put("idTraspaso", delegator.getNextSeqId("TraspasoBanco"));
//				traspasoBanco.put("bancoIdOrigen",  cuentaAbono.getString("bancoId"));			
//				traspasoBanco.put("acctgTransId", transaccion.getString("acctgTransId"));
//				traspasoBanco.put("cuentaOrigen", cuentaAbono.getString("cuentaBancariaId"));
//				traspasoBanco.put("debitCreditFlagOrigen", "C");
//				traspasoBanco.put("depositoRetiroFlagOrigen", "R");
//				traspasoBanco.put("monto", BigDecimal.valueOf(Double.valueOf((String) montoMap.get(descrSinEspacios))));
//				traspasoBanco.put("tipotransaccion", "TRASPASO");
//				traspasoBanco.put("partyId", organizationPartyId);
//				traspasoBanco.put("descripcion", descripcion);
//				traspasoBanco.put("createdByUserLogin", userLogin.getString("userLoginId"));																
//				delegator.create(traspasoBanco);
//
//			} else if(catalogoCargo.equals("BANCO")){
//				
//				GenericValue cuentaCargo = delegator.findByPrimaryKey("CuentaBancaria",
//	    				UtilMisc.toMap("cuentaBancariaId", catalogoCargoMap.get(descrSinEspacios)));
//	        	
//	        	Debug.log("Mike cuenta cargo: " + cuentaCargo);
//
//				GenericValue traspasoBanco = delegator.makeValue("TraspasoBanco");
//				traspasoBanco.setNonPKFields(context);			
//				traspasoBanco.put("idTraspaso", delegator.getNextSeqId("TraspasoBanco"));
//				traspasoBanco.put("bancoIdDestino", cuentaCargo.getString("bancoId"));			
//				traspasoBanco.put("acctgTransId", transaccion.getString("acctgTransId"));
//				traspasoBanco.put("cuentaDestino", cuentaCargo.getString("cuentaBancariaId"));
//				traspasoBanco.put("debitCreditFlagDestino", "D");
//				traspasoBanco.put("depositoRetiroFlagDestino", "D");
//				traspasoBanco.put("monto", BigDecimal.valueOf(Double.valueOf((String) montoMap.get(descrSinEspacios))));
//				traspasoBanco.put("tipotransaccion", "TRASPASO");
//				traspasoBanco.put("partyId", organizationPartyId);
//				traspasoBanco.put("descripcion", descripcion);
//				traspasoBanco.put("createdByUserLogin", userLogin.getString("userLoginId"));																
//				delegator.create(traspasoBanco);
//			}

			Map result = ServiceUtil.returnSuccess();
			result.put("acctgTransId", transaccion.getString("acctgTransId"));
			return result;
	}
	
	/**
	 * autor JRRM
	 * @param delegator
	 * @param cuenta
	 * @param periodo
	 * @param monto
	 * @return saldo actual de la cuenta
	 * @throws GenericEntityException 
	 */
	public static BigDecimal obtenSaldoCuentaActual(Delegator delegator, String cuenta,
			Timestamp periodo) throws GenericEntityException {
		Debug.log("Obten Saldo Cuenta Actual");
		// Rango de periodos.
		Calendar date = Calendar.getInstance();
		date.setTime(periodo);
		date.set(Calendar.DAY_OF_MONTH, 1);
		date.set(Calendar.HOUR, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		date.set(Calendar.AM_PM, Calendar.AM);
		Timestamp periodoFin = new Timestamp(date.getTimeInMillis());
		date.set(Calendar.MONTH, 0);// Enero
		Timestamp periodoInicio = new Timestamp(date.getTimeInMillis());
		
		// Se debe hacer la suma de todos los periodos anteriores al periodo
		// seleccionado para obtener el saldo actual de la cuenta.
		EntityConditionList<EntityExpr> conditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"catalogoId", EntityOperator.EQUALS, cuenta),
						EntityCondition.makeCondition("tipo",
								EntityOperator.EQUALS, "B"), EntityCondition.makeCondition(
								"periodo",
								EntityOperator.GREATER_THAN_EQUAL_TO,
								periodoInicio), EntityCondition.makeCondition(
								"periodo", EntityOperator.LESS_THAN_EQUAL_TO,
								periodoFin)), EntityOperator.AND);
		
		List<GenericValue> saldoCuenta = delegator.findByCondition(
				"SaldoCatalogo", conditions, null, null);

		// Recorremos la lista sumando el monto
		BigDecimal sumaMonto = BigDecimal.ZERO;
		for (GenericValue saldo : saldoCuenta) {
			Debug.log("saldo: "+saldo.getBigDecimal("monto"));
			sumaMonto = sumaMonto.add(saldo.getBigDecimal("monto"));
		}
		return sumaMonto;
	}
	
	/**
	 * autor JRRM
	 * @param delegator
	 * @param cuenta
	 * @param periodo
	 * @return
	 * @throws GenericEntityException
	 */
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
}