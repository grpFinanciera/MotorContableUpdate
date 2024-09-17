package org.opentaps.dataimport.domain;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.order.finaccount.UtilClavePresupuestal;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.dataimport.UtilImport;

/*
 * Clase para imprtar requisiciones
 */
public class RequisicionImportService  {

	private static final String MODULE = RequisicionImportService.class.getName();
	private static String organizationPartyId;
	private static Timestamp fechaContable;
	private static String descripcion;
	private static String justificacion;
	private static String tipoMoneda;
	private static String requisicionId;
	private static GenericValue userLogin;
	private static String userLoginId;
	public int importedRecords;
	public static final String resource = "SecurityextUiLabels";
	protected LocalDispatcher dispatcher = null;

	public static Map<String,Object> importRequisicion(DispatchContext dctx, Map<String, ?> context)  {

		Locale locale = (Locale) context.get("locale");
		TimeZone timeZone =  (TimeZone) context.get("timeZone");
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();

		Map<String, Object> regreso = ServiceUtil.returnSuccess();

		try {

			organizationPartyId = (String)context.get("organizationPartyId");
			String fecha = (String) context.get("fechaContable");		
			fechaContable = UtilDateTime.stringToTimeStamp(fecha, "dd-MM-yyyy", timeZone, locale);		
			descripcion = (String) context.get("descripcion");
			justificacion = (String) context.get("justificacion");
			tipoMoneda = (String) context.get("tipoMoneda");
			userLogin = (GenericValue) context.get("userLogin");
			userLoginId = userLogin.getString("userLoginId");

			boolean error = false;
			
			List<String> orderBy = UtilMisc.toList("renglon");

			List<GenericValue> listImportRequisicion = delegator.findByCondition("DataImportDetRequisicion", UtilImport.condicionesSinProcesar(userLoginId), null, orderBy);
			List<GenericValue> listvalidaRequisicion = delegator.findByCondition("ValidaDetalleReq", UtilImport.condicionesSinProcesar(userLoginId), null, orderBy);

			Map<String,GenericValue> dataImportRequisicion = FastMap.newInstance();
			Map<String,GenericValue> validaDataImportRequisicion = FastMap.newInstance();

			GenericValue uom = delegator.findByPrimaryKey("Uom",
					UtilMisc.toMap("uomId", tipoMoneda));
			if (uom.isEmpty()) {
				error = true;
				throw new GenericEntityException("El tipo de moneda ingresado no es el correcto");
			}

			for(GenericValue importRequisicion : listImportRequisicion){				
				dataImportRequisicion.put(importRequisicion.getString("detalleId"), importRequisicion);
			}

			for(GenericValue validaRequisicion : listvalidaRequisicion){				
				validaDataImportRequisicion.put(validaRequisicion.getString("detalleId"), validaRequisicion);				
			}

			for(GenericValue validandoRequisicion : listvalidaRequisicion){

				GenericValue prueba = validaDataImportRequisicion.get(validandoRequisicion.getString("detalleId"));
				GenericValue prueba2 = dataImportRequisicion.get(validandoRequisicion.getString("detalleId"));
				
				UtilImport.limpiarRegistroError(prueba2);
				
				if(UtilValidate.isEmpty(prueba.getString("customTimePeriodId"))){	
					UtilImport.registrarError(prueba2, "El mes o a\u00f1o no estan abiertos");
					error = true;
				}

				if(UtilValidate.isEmpty(prueba.getString("productId"))){
					UtilImport.registrarError(prueba2, "El producto [" + prueba2.getString("productId") + "] no existe");
					error=true;
				}

				if(UtilValidate.isEmpty(prueba.getString("cantidad"))){
					UtilImport.registrarError(prueba2,"No se ingreso la cantidad");
					error=true;
				}

				if(UtilValidate.isEmpty(prueba.getString("monto"))){
					UtilImport.registrarError(prueba2,"No se ingreso el monto");
					error=true;
				}

				if(UtilValidate.isEmpty(prueba.getString("descripcion"))){
					UtilImport.registrarError(prueba2,"No se ingreso la descripci\u00f3n");
					error=true;
				}
				
				if(UtilValidate.isEmpty(prueba.getString("fechaEntrega"))){
					UtilImport.registrarError(prueba2,"No se ingreso la fecha de entrega");
					error=true;
				}
				
				if(UtilValidate.isEmpty(prueba.getString("procedencia"))){
					UtilImport.registrarError(prueba2,"No se ingreso la procedencia");
					error=true;
				} else {
					if(!(prueba.getString("procedencia").equals("E") || prueba.getString("procedencia").equals("N"))){
						UtilImport.registrarError(prueba2,"Los valores permitidos para la procedencia son E (Extranjera) \u00f3 N (Nacional)");
						error=true;
					}
				}

				if(UtilValidate.isEmpty(prueba.getString("iva"))){
					UtilImport.registrarError(prueba2,"No se ingreso el iva");
					error=true;
				} else {
					if(!(prueba.getString("iva").equals("Y") || prueba.getString("iva").equals("N"))){
						UtilImport.registrarError(prueba2,"Los valores permitidos para iva son Y \u00f3 N");
						error=true;
					}
				}
				
				if(UtilValidate.isEmpty(prueba.getString("iva"))){
					UtilImport.registrarError(prueba2,"No se ingreso el iva");
					error=true;
				}
			}

			TransactionUtil.begin(43200);

			if(!error){

				String areaId = obtenerArea(delegator, userLoginId);

				Map<String, Object> requisicionMap = FastMap.newInstance();
				requisicionMap.put("fechaContable", fechaContable);
				requisicionMap.put("descripcion", descripcion);
				requisicionMap.put("justificacion", justificacion);
				requisicionMap.put("areaPartyId", areaId);
				requisicionMap.put("tipoMoneda", tipoMoneda);
				requisicionMap.put("organizationPartyId", organizationPartyId);
				requisicionMap.put("userLogin", userLogin);

				Map<String, Object> result = dispatcher.runSync("guardaRequisicion", requisicionMap,43200,false);
				if (ServiceUtil.isError(result)) {
					
					Debug.logError("Hubo Error al crear la requisicion",MODULE);
					return ServiceUtil.returnError("Hubo un error al crear la requisicion");
				}

				requisicionId = (String) result.get("requisicionId");

				Map<String, Object> detRequisicionMap = FastMap.newInstance();
				for(GenericValue importRequisicion2 : listImportRequisicion){
					detRequisicionMap.put("organizationPartyId", organizationPartyId);
					detRequisicionMap.put("requisicionId", requisicionId);
					detRequisicionMap.put("cantidad", importRequisicion2.getBigDecimal("cantidad"));
					detRequisicionMap.put("productId", importRequisicion2.getString("productId"));
					detRequisicionMap.put("monto", importRequisicion2.getBigDecimal("monto"));
					detRequisicionMap.put("descripcion", importRequisicion2.getString("descripcion"));
					detRequisicionMap.put("customTimePeriodId", importRequisicion2.getString("customTimePeriodId"));
					detRequisicionMap.put("userLogin", userLogin);
					detRequisicionMap.put("ciclo", importRequisicion2.getString("anio"));
					detRequisicionMap.put("fechaEntrega", importRequisicion2.getTimestamp("fechaEntrega"));
					detRequisicionMap.put("iva", importRequisicion2.getString("iva"));
					detRequisicionMap.put("procedencia", (importRequisicion2.getString("procedencia").equals("E")? "Extranjera" : "Nacional" ));
					
					for(int i=1; i<=UtilClavePresupuestal.TAG_COUNT; i++){
						detRequisicionMap.put(UtilClavePresupuestal.VIEW_TAG_PREFIX+i, importRequisicion2.getString(UtilClavePresupuestal.VIEW_TAG_PREFIX+i));
					}
					
					Map<String, Object> result2 = dispatcher.runSync("agregaItemRequisicion", detRequisicionMap,43200,false);
					if (ServiceUtil.isError(result2)) {
						Debug.logError("Hubo Error al crear detalle requisicion",MODULE);
						return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result2));
					}
					
					for(GenericValue importRequisicionExito : listImportRequisicion){				
						UtilImport.registrarExitoso(importRequisicionExito);
					}
					
					regreso = ServiceUtil.returnSuccess("Se gener\u00f3 la requisici\u00f3n "+requisicionId);
				}
				
			}

			delegator.storeAll(listImportRequisicion);
			TransactionUtil.commit();
			

		} catch (ParseException | GenericEntityException | GenericServiceException | NullPointerException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage()); 
		}
		return regreso;
	}

	/**
	 * Obtiene el areaId
	 * @param delegator
	 * @param user
	 * @return
	 * @throws GenericEntityException
	 */
	public static String obtenerArea(Delegator delegator, String user)
			throws GenericEntityException {

		Debug.logInfo("Mike user: " + user,MODULE);
		GenericValue userLogin = delegator.findByPrimaryKey("UserLogin",
				UtilMisc.toMap("userLoginId", user));

		GenericValue person = delegator.findByPrimaryKey("Person",
				UtilMisc.toMap("partyId", userLogin.getString("partyId")));

		return person.getString("areaId");
	}

}