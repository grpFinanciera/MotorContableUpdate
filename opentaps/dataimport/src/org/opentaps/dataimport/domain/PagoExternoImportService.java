package org.opentaps.dataimport.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilNumber;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.util.LineaMotorContable;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.dataimport.UtilImport;

public class PagoExternoImportService {

	private static String module = PagoExternoImportService.class.getName();

	private static String eventoContableId;
	private static String organizationPartyId;
	private static String tipoClave = "EGRESO";
	private static GenericValue userLogin;
	private static String userLoginId;
	
	/**
	 * Metodo para importar pagos a externos
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> importPagoExterno(DispatchContext dctx, Map<String, ?> context){
		
		boolean error = false;
		
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		
		Map<String, Object> regreso = ServiceUtil.returnSuccess();
		
		eventoContableId = (String) context.get("eventoContableId");
		organizationPartyId = (String) context.get("organizationPartyId");
		userLogin = (GenericValue) context.get("userLogin");
		userLoginId = userLogin.getString("userLoginId");
		
		try {
			
			GenericValue partyOrganization = delegator.findByPrimaryKeyCache("Party",UtilMisc.toMap("partyId", organizationPartyId));
			if (UtilValidate.isEmpty(partyOrganization)){
				throw new GenericEntityException("El identificador de la organizaci\u00f3n es incorrecto");
			}
			
			GenericValue EventoContable = delegator.findByPrimaryKeyCache("EventoContable",UtilMisc.toMap("acctgTransTypeId", eventoContableId));
			if (UtilValidate.isEmpty(EventoContable)){
				throw new GenericEntityException("El evento contable no se encuentra configurado en el sistema");
			}
			
			List<GenericValue> listLineasPresupuestales = delegator.findByCondition("LineaPresupuestal",
					EntityCondition.makeCondition(EntityOperator.AND,
							EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, eventoContableId)),
							UtilMisc.toList("momentoCompara","comparacion","secuencia"), UtilMisc.toList("secuencia"));
			// ---- Obtiene la linea primer presupuestal , presupuestalmente de donde se obtiene el dinero
			GenericValue PrimeraLineaPresupuestal = EntityUtil.getFirst(listLineasPresupuestales);
			
			//Se crea el mapa de Lineas Presupuestales, el motor contable lo requiere , sin embargo en este proceso no se utiliza
			Map<String,GenericValue> mapaSecuenciaPresupuestal = FastMap.newInstance();
			for (GenericValue LineaPresupuestal : listLineasPresupuestales) {
				mapaSecuenciaPresupuestal.put(LineaPresupuestal.getString("secuencia"), LineaPresupuestal);
			}
			
			List<String> fieldsToSelect = delegator.getModelEntity("ValidaPagoExterno").getAllFieldNames();
			
			EntityCondition condicionValida = UtilImport.condicionesSinProcesar(userLoginId);
			
			String momentoIdCompara = new String();
			boolean validacionCompara = true;
			if(UtilValidate.isNotEmpty(PrimeraLineaPresupuestal)){
				validacionCompara = UtilValidate.isNotEmpty(PrimeraLineaPresupuestal.getString("comparacion"));
				momentoIdCompara = PrimeraLineaPresupuestal.getString("momentoCompara");
			}
			
			// ---- Se obtiene la validacion presupuestal
			if(validacionCompara){
				//Se guarda el momento a comparar , para esto se genera una transaccion temporal
				TransactionUtil.begin();
				delegator.storeByCondition("DataImportPagoExterno", 
						UtilMisc.toMap("momentoId",momentoIdCompara), UtilImport.condicionesSinProcesar(userLoginId));
				TransactionUtil.commit();
				
			} else {
				fieldsToSelect.remove("montoControl");
			}
			
			List<String> orderBy = UtilMisc.toList("dataImportPagoExternoId");
			List<GenericValue> listDataImportPagoExterno = delegator.findByCondition("DataImportPagoExterno", 
					UtilImport.condicionesSinProcesar(userLoginId), null, orderBy);
			
			List<GenericValue> listValidaPagoExterno = delegator.findByCondition("ValidaPagoExterno", 
					condicionValida , fieldsToSelect , orderBy);
			
			Map<String,GenericValue> mapDataImportPagoExterno = FastMap.newInstance();
			
			for (GenericValue DataImportPagoExterno : listDataImportPagoExterno) {
				mapDataImportPagoExterno.put(DataImportPagoExterno.getString("dataImportPagoExternoId"), DataImportPagoExterno);
			}
			
			Map<String,Map<String,Object>> mapRenglonPagoExternoLinea = FastMap.newInstance();
			Map<String,Object> mapTipoPagoExternoLinea = FastMap.newInstance();
			List<LineaMotorContable> listLineaMotorContable = FastList.newInstance();
			List<GenericValue> listPagoExterno = FastList.newInstance();
			String dataImportPagoExternoId = new String();
			BigDecimal monto = BigDecimal.ZERO;
			BigDecimal montoCompara = BigDecimal.ZERO;
			Map<String,String> mapaCuentaBanco = FastMap.newInstance();
			for (GenericValue ValidaPagoExterno : listValidaPagoExterno) {
				
				LineaMotorContable LineaMotorContable = new LineaMotorContable();
				
				dataImportPagoExternoId = ValidaPagoExterno.getString("dataImportPagoExternoId");
				GenericValue DataImportPagoExterno = mapDataImportPagoExterno.get(dataImportPagoExternoId);
				
				UtilImport.limpiarRegistroError(DataImportPagoExterno);
				
				if(UtilValidate.isEmpty(ValidaPagoExterno.getString("clavePresupuestalCve"))){
					UtilImport.registrarError(DataImportPagoExterno, "No existe la clave presupuestal");
					error = true;
				} else {
					LineaMotorContable.setClavePresupuestal(ValidaPagoExterno.getString("clavePresupuestalCve"));
				}
				
				if(UtilValidate.isNotEmpty(ValidaPagoExterno.getString("inactivo"))){
					UtilImport.registrarError(DataImportPagoExterno, 
							"La clave presupuestal ["+ValidaPagoExterno.getString("clavePresupuestal")+"] esta inactiva");
					error = true;
				}
				
				monto = UtilNumber.getBigDecimal(ValidaPagoExterno.getBigDecimal("monto"));
				if(UtilValidate.isEmpty(DataImportPagoExterno.getBigDecimal("monto"))){
					UtilImport.registrarError(DataImportPagoExterno, "Es necesario ingresar el monto ");
					error = true;
				}
				if(validacionCompara){
					montoCompara = UtilNumber.getBigDecimal(ValidaPagoExterno.getBigDecimal("montoControl"));
					if(monto.compareTo(montoCompara) > 0){
						UtilImport.registrarError(DataImportPagoExterno, 
								"No existe suficiencia presupuestal : "+
									montoCompara.subtract(monto));
						error = true;
					} else {
						LineaMotorContable.setMontoPresupuestal(monto);
					}
				} else {
					LineaMotorContable.setMontoPresupuestal(monto);
				}
				
				if(UtilValidate.isEmpty(ValidaPagoExterno.getDate("fechaContable"))){
					UtilImport.registrarError(DataImportPagoExterno, "Debe ingresar la fecha contable");
					error = true;
				} else {
					LineaMotorContable.setFecha(UtilDateTime.toTimestamp(ValidaPagoExterno.getDate("fechaContable")));
				}
				
				if(UtilValidate.isEmpty(ValidaPagoExterno.getString("conceptoPago"))){
					UtilImport.registrarError(DataImportPagoExterno, "Debe ingresar el concepto del pago");
					error = true;
				}
				
				if(UtilValidate.isEmpty(ValidaPagoExterno.getString("cuentaBancariaId"))){
					UtilImport.registrarError(DataImportPagoExterno, "Debe ingresar la cuenta bancaria");
					error = true;
				} else {
					if(UtilValidate.isEmpty(ValidaPagoExterno.getString("cuentaBancariaIdCta"))){
						UtilImport.registrarError(DataImportPagoExterno, "El identificador de la cuenta bancaria es incorrecto o no existe");
						error = true;
					} else {
						mapaCuentaBanco.put(ValidaPagoExterno.getString("cuentaBancariaIdCta"),ValidaPagoExterno.getString("bancoId"));
					}
				}
				
				if(UtilValidate.isEmpty(ValidaPagoExterno.getString("moneda"))){
					UtilImport.registrarError(DataImportPagoExterno, 
							"Debe ingresar la moneda");
					error = true;
				} else {
					if(UtilValidate.isEmpty(ValidaPagoExterno.getString("uomId"))){
						UtilImport.registrarError(DataImportPagoExterno, 
								"El identificador de la moneda es incorrecto o no existe");
						error = true;
					}
				}
				
				if(UtilValidate.isEmpty(ValidaPagoExterno.getString("metodoPago"))){
					UtilImport.registrarError(DataImportPagoExterno, 
							"Debe ingresar el m\u00f3todo de pago");
					error = true;
				} else {
					if(UtilValidate.isEmpty(ValidaPagoExterno.getString("paymentTypeId"))){
						UtilImport.registrarError(DataImportPagoExterno, 
								"El identificador del m\u00f3todo de pago es incorrecto o no existe");
						error = true;
					}
				}
				
				LineaMotorContable.setLineasPresupuestales(mapaSecuenciaPresupuestal);
				//Se agrega la linea motor para la primera poliza
				listLineaMotorContable.add(LineaMotorContable);
				mapTipoPagoExternoLinea.put("LineaMotorContable", LineaMotorContable);
				//Ahora se crea el registro de pago externo
				GenericValue PagoExterno = GenericValue.create(delegator.getModelEntity("PagoExterno"));
				PagoExterno.setAllFields(ValidaPagoExterno.getAllFields(), false, null, false);
				listPagoExterno.add(PagoExterno);
				mapTipoPagoExternoLinea.put("PagoExterno", PagoExterno);
				//Por ultimo se crea el registro de pendiente de tesoreria
				//Agregamos la informacion para el tesorero.
				GenericValue PendientesTesoreria = GenericValue.create(delegator.getModelEntity("PendientesTesoreria"));
				PendientesTesoreria.setAllFields(ValidaPagoExterno.getAllFields(), false, null, false);
				PendientesTesoreria.set("fechaContable", UtilDateTime.toTimestamp(ValidaPagoExterno.getDate("fechaContable")));
				PendientesTesoreria.set("tipo", "PAGO_EXTERNO");
				PendientesTesoreria.set("descripcion", "Importacion para pago a : "+ValidaPagoExterno.getString("nombreExterno"));
				PendientesTesoreria.set("estatus","ORDEN_PAGO_PENDIENTE");
				PendientesTesoreria.set("fechaTransaccion", UtilDateTime.nowTimestamp());
				mapTipoPagoExternoLinea.put("PendientesTesoreria", PendientesTesoreria);
				
				mapRenglonPagoExternoLinea.put(dataImportPagoExternoId, mapTipoPagoExternoLinea);
				mapTipoPagoExternoLinea = FastMap.newInstance();
			}
			
			
			TransactionUtil.begin(43200);
			
			if(!error){
				
				Map<String,Object> result = ServiceUtil.returnSuccess();
				
				for (Entry<String, Map<String, Object>> RenglonPagoExternoLinea : mapRenglonPagoExternoLinea.entrySet())
				{
					GenericValue DataImportPagoExterno = mapDataImportPagoExterno.get(RenglonPagoExternoLinea.getKey());
					
					LineaMotorContable lineaMotor = (LineaMotorContable) RenglonPagoExternoLinea.getValue().get("LineaMotorContable");
					GenericValue PagoExterno = (GenericValue) RenglonPagoExternoLinea.getValue().get("PagoExterno");
					GenericValue PendientesTesoreria = (GenericValue) RenglonPagoExternoLinea.getValue().get("PendientesTesoreria");
					
					Map<String, Object> input = FastMap.newInstance();
					input.put("fechaRegistro", UtilDateTime.nowTimestamp());
					input.put("fechaContable", lineaMotor.getFecha());
					input.put("eventoContableId", eventoContableId);
					input.put("usuario", userLoginId);
					input.put("organizationId", organizationPartyId);
					input.put("currency", "MXN");
					input.put("tipoClaveId", tipoClave);
					input.put("lineasMotor", UtilMisc.toList(lineaMotor));
					input.put("descripcion", "Importaci\u00f3n del ejercido para pago a : "+DataImportPagoExterno.getString("nombreExterno"));
					
					result = dispatcher.runSync("creaTransaccionMotor",input, 43200, false);
					
					if (ServiceUtil.isError(result)) {
						Debug.logError("Hubo Error al crear la poliza del renglon "+DataImportPagoExterno.getString("renglon"),module);
						return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
					} else {
						GenericValue AcctgTrans = (GenericValue) result.get("transaccion");
						PagoExterno.put("acctgTransId", AcctgTrans.getString("acctgTransId"));
					}
					
					PagoExterno.setNextSeqId();
					PagoExterno.create();
					
					PendientesTesoreria.set("idPendienteTesoreria", PagoExterno.getString("pagoExternoId"));
					PendientesTesoreria.set("pagoExternoId", PagoExterno.getString("pagoExternoId"));
					PendientesTesoreria.create();
					
				}
				
				if (ServiceUtil.isSuccess(result)) {
					regreso = ServiceUtil.returnSuccess("Se realiz\u00f3 correctamente la importaci\u00f3n de los ("+listDataImportPagoExterno.size()+") pagos ,"
							+ " pida al tesorero que recibe sus pendientes por pagar");
					for (GenericValue DataImportIngresoPres : listDataImportPagoExterno) {
						UtilImport.registrarExitoso(DataImportIngresoPres);
					}
				}
				
			}

			delegator.storeAll(listDataImportPagoExterno);
			
			TransactionUtil.commit();
			
		} catch (GenericEntityException | NullPointerException | GenericServiceException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		
		return regreso;
	}
	
}
