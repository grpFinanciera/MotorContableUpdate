package org.opentaps.dataimport.domain;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

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
import org.ofbiz.order.finaccount.UtilClavePresupuestal;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.dataimport.UtilImport;

import com.ibm.icu.util.Calendar;

/*
 * Clase para imprtar requisiciones
 */
public class CompensadaImportService {
	
	private static String module = CompensadaImportService.class.getName();

	private static String organizationPartyId;
	private static Timestamp fechaContable;
	private static String descripcion;
	private static String tipoClave;
	private static String agrupador;
	private static String tipoMovimiento;
	private static GenericValue userLogin;
	private static String userLoginId;
	
	/**
	 * Metodo para importar afectaciones compensadas
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> importCompensada(DispatchContext dctx, Map<String, ?> context){
		
		Locale locale = (Locale) context.get("locale");
		TimeZone timeZone =  (TimeZone) context.get("timeZone");
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		
		Map<String, Object> regreso = ServiceUtil.returnSuccess();
		
		try {
			
			organizationPartyId = (String)context.get("organizationPartyId");
			tipoClave = (String) context.get("tipoClave");
			String fecha = (String) context.get("fechaContable");
			fechaContable = UtilDateTime.stringToTimeStamp(fecha, "dd-MM-yyyy", timeZone, locale);
			descripcion = (String) context.get("descripcion");
			tipoMovimiento = (String) context.get("tipoMovimiento");
			userLogin = (GenericValue) context.get("userLogin");
			userLoginId = userLogin.getString("userLoginId");
			
			if(tipoMovimiento == null){
				tipoMovimiento = "INTERNA";
			}
			
			boolean error = false;
			
			GenericValue partyOrganization = delegator.findByPrimaryKeyCache("Party",UtilMisc.toMap("partyId", organizationPartyId));
			if (UtilValidate.isEmpty(partyOrganization)){
				throw new GenericEntityException("El identificador de la organización es incorrecto");
			}
			
			if (!(tipoClave.equals("EGRESO") || tipoClave.equals("INGRESO"))) {
				throw new GenericEntityException("El tipo de clave es incorrecto");
			}
			
			List<String> orderBy = UtilMisc.toList("detalleId");
			List<GenericValue> listDataImportAfectacionCompensadaClave = delegator.findByCondition("DataImportAfectacionCompensadaClave", 
					UtilImport.condicionesSinProcesar(userLoginId), null, orderBy);
			
			List<GenericValue> listDataImportAfectacionCompensada = delegator.findByCondition("DataImportACompensada", 
					UtilImport.condicionesSinProcesar(userLoginId), null, orderBy);
			
			Map<String,GenericValue> mapDataImportAfectacionCompensada = FastMap.newInstance();
			
			for (GenericValue DataImportAfectacionCompensada : listDataImportAfectacionCompensada) {
				mapDataImportAfectacionCompensada.put(DataImportAfectacionCompensada.getString("detalleId"), DataImportAfectacionCompensada);
			}
			
			//Lsta que funcionara para guardar las claves presupuestales de reduccion para poder validar la suficiencia
			LinkedHashSet<String> listClavesPresupR = new LinkedHashSet<String>();
			
			//Validacion de datos
			String periodoA = new String();
			String periodoR = new String();
			String moneda = new String();
			BigDecimal monto = BigDecimal.ZERO;
			
			String clavePresupR = new String();
			String clavePresupA = new String();
			String inactivoR = new String();
			String inactivoA = new String();
			
			String detalleId = new String();
			
			for (GenericValue DataImportAfectacionCompensadaClave : listDataImportAfectacionCompensadaClave) {
				
				detalleId = DataImportAfectacionCompensadaClave.getString("detalleId");
				GenericValue DataImportAfectacionCompensada = mapDataImportAfectacionCompensada.get(detalleId);
				
				UtilImport.limpiarRegistroError(DataImportAfectacionCompensada);
				
				periodoA = DataImportAfectacionCompensadaClave.getString("periodoA");
				periodoR = DataImportAfectacionCompensadaClave.getString("periodoR");
				moneda = DataImportAfectacionCompensadaClave.getString("moneda");
				monto = DataImportAfectacionCompensadaClave.getBigDecimal("monto");
				clavePresupR = DataImportAfectacionCompensadaClave.getString("clavePresupR");
				clavePresupA = DataImportAfectacionCompensadaClave.getString("clavePresupA");
				inactivoR = DataImportAfectacionCompensadaClave.getString("inactivoR");
				inactivoA = DataImportAfectacionCompensadaClave.getString("inactivoA");
				
				if (UtilValidate.isEmpty(periodoA) || periodoA.length() != 2) {
					UtilImport.registrarError(DataImportAfectacionCompensada, "No se ingres\u00f3 corectamente el periodo de ampliaci\u00f3n");
					error = true;
				}
				
				if (UtilValidate.isEmpty(periodoR) || periodoR.length() != 2) {
					UtilImport.registrarError(DataImportAfectacionCompensada, "No se ingres\u00f3 corectamente el periodo de reducci\u00f3n");
					error = true;
				} 
			
				if (UtilValidate.isEmpty(moneda)) {
					UtilImport.registrarError(DataImportAfectacionCompensada, "No se ingres\u00f3 corectamente la moneda");
					error = true;
				}
				
				if (UtilValidate.isEmpty(monto)) {
					UtilImport.registrarError(DataImportAfectacionCompensada, "No se ingres\u00f3 corectamente el monto");
					error = true;
				}
				
				if(UtilValidate.isEmpty(clavePresupR)){
					UtilImport.registrarError(DataImportAfectacionCompensada, 
							"No existe la clave presupuestal "+DataImportAfectacionCompensadaClave.getString("clavePresupuestalR"));
					error = true;
				}
				
				if(UtilValidate.isEmpty(clavePresupA)){
					UtilImport.registrarError(DataImportAfectacionCompensada, 
							"No existe la clave presupuestal "+DataImportAfectacionCompensadaClave.getString("clavePresupuestalA"));
					error = true;
				}
				
				if(UtilValidate.isNotEmpty(inactivoR)){
					UtilImport.registrarError(DataImportAfectacionCompensada, 
							"La clave presupuestal ["+DataImportAfectacionCompensadaClave.getString("clavePresupuestalR")+"] esta inactiva");
					error = true;
				}
				
				if(UtilValidate.isNotEmpty(inactivoA)){
					UtilImport.registrarError(DataImportAfectacionCompensada, 
							"La clave presupuestal ["+DataImportAfectacionCompensadaClave.getString("clavePresupuestalA")+"] esta inactiva");
					error = true;
				}
				
				listClavesPresupR.add(clavePresupR);
				
			}
			
			String eventoId =  new String();
			if(tipoClave.equalsIgnoreCase(UtilClavePresupuestal.EGRESO_TAG)){
				eventoId = "PEAD-01";
			} else {
				eventoId = "LIAD-01";
			}
			
			GenericValue PrimeraLineaPresupuestal = EntityUtil.getFirst(delegator.findByCondition("LineaPresupuestal",
					EntityCondition.makeCondition("acctgTransTypeId", eventoId), UtilMisc.toList("momentoCompara","comparacion"), UtilMisc.toList("secuencia")));
			
			String momentoIdCompara = new String();
			boolean validacionCompara = true;
			if(UtilValidate.isNotEmpty(PrimeraLineaPresupuestal)){
				validacionCompara = UtilValidate.isNotEmpty(PrimeraLineaPresupuestal.getString("comparacion"));
				momentoIdCompara = PrimeraLineaPresupuestal.getString("momentoCompara");
			}
			
			
			//Valida la suficiencia presupuestaria de las claves de reducción, solo si el evento contable tiene la validación
			if(validacionCompara){
				
				Calendar calFechaContable = UtilDateTime.toCalendar(fechaContable);
				List<String> fieldsToSelect = UtilMisc.toList("clavePresupuestal","mesId","monto");
				EntityCondition condicionControl = EntityCondition.makeCondition(EntityOperator.AND,
		                EntityCondition.makeCondition("clavePresupuestal", EntityOperator.IN, listClavesPresupR),
		                EntityCondition.makeCondition("ciclo", calFechaContable.get(Calendar.YEAR)),
		                EntityCondition.makeCondition("momentoId", momentoIdCompara));
				List<GenericValue> listControlPresupuestal = delegator.findByCondition("ControlPresupuestal", condicionControl, fieldsToSelect, null);
	
				Map<String,Object> mapaMontoClaveMes = FastMap.newInstance();
				Map<String,GenericValue> mapaMontoControl = FastMap.newInstance();
				String llave = new String();
				for (GenericValue ControlPresupuestal : listControlPresupuestal) {
					llave = ControlPresupuestal.getString("clavePresupuestal")+ControlPresupuestal.getString("mesId");
					UtilMisc.addToBigDecimalInMap(mapaMontoClaveMes, llave, ControlPresupuestal.getBigDecimal("monto"));
					mapaMontoControl.put(llave, ControlPresupuestal);
				}
				
				BigDecimal montoControl = BigDecimal.ZERO;
				
				GenericValue ControlPresupuestal = delegator.makeValue("ControlPresupuestal");
				for (GenericValue DataImportAfectacionCompensadaClave : listDataImportAfectacionCompensadaClave) {
					
					detalleId = DataImportAfectacionCompensadaClave.getString("detalleId");
					GenericValue DataImportAfectacionCompensada = mapDataImportAfectacionCompensada.get(detalleId);
					
					clavePresupR = DataImportAfectacionCompensadaClave.getString("clavePresupR");
					periodoR = DataImportAfectacionCompensadaClave.getString("periodoR");
					monto = UtilNumber.getBigDecimal(DataImportAfectacionCompensadaClave.getBigDecimal("monto"));
					
					if(UtilValidate.isEmpty(mapaMontoClaveMes.get(clavePresupR+periodoR))){
						montoControl = BigDecimal.ZERO;
					} else {
						montoControl = (BigDecimal) mapaMontoClaveMes.get(clavePresupR+periodoR);
						ControlPresupuestal = mapaMontoControl.get(clavePresupR+periodoR);
						ControlPresupuestal.set("monto", montoControl.subtract(monto));
					}
	
					if(monto.compareTo(montoControl) > 0){
						UtilImport.registrarError(DataImportAfectacionCompensada, 
								"No existe suficiencia presupuestal : "+ montoControl.subtract(monto)+
								"para la clave ["+DataImportAfectacionCompensadaClave.getString("clavePresupuestalR")+"]");
						error = true;
					}
					
				}
			
			}

			
			TransactionUtil.begin(43200);
			if(!error){
				Debug.logInfo("Ingresa a crear la afectacion",module);
				
				Map<String, Object> afectacionMap = FastMap.newInstance();
				afectacionMap.put("fechaContable", fechaContable);
				afectacionMap.put("description", descripcion);
				afectacionMap.put("performFind", "Y");
				afectacionMap.put("organizationPartyId", organizationPartyId);
				afectacionMap.put("userLogin", userLogin);
				afectacionMap.put("tipoMovimiento", tipoMovimiento);

				Map<String, Object> result = dispatcher.runSync("ACompensadaE", afectacionMap, 43200 ,false);

				if (ServiceUtil.isError(result)) {
					return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
				}
				
				GenericValue afectacion = (GenericValue) result.get("datos");
				agrupador = (String) afectacion.get("agrupadorP");
				
				for (GenericValue DataImportAfectacionCompensada : listDataImportAfectacionCompensada) {
					
					monto = UtilNumber.getBigDecimal(DataImportAfectacionCompensada.getBigDecimal("monto"));
					
					Map<String, Object> detalleMap = FastMap.newInstance();
					detalleMap.put("performFind", "B");
					detalleMap.put("agrupadorP", agrupador);
					detalleMap.put("monto", monto);
					detalleMap.put("currency", DataImportAfectacionCompensada.getString("moneda"));
					detalleMap.put("perdiodoIR", DataImportAfectacionCompensada.getString("periodoR"));
					detalleMap.put("perdiodoIA", DataImportAfectacionCompensada.getString("periodoA"));
					detalleMap.put("clasificacion1", DataImportAfectacionCompensada.getString("clasificacion1"));
					detalleMap.put("clasificacion2", DataImportAfectacionCompensada.getString("clasificacion2"));
					detalleMap.put("clasificacion3", DataImportAfectacionCompensada.getString("clasificacion3"));
					detalleMap.put("clasificacion4", DataImportAfectacionCompensada.getString("clasificacion4"));
					detalleMap.put("clasificacion5", DataImportAfectacionCompensada.getString("clasificacion5"));
					detalleMap.put("clasificacion6", DataImportAfectacionCompensada.getString("clasificacion6"));
					detalleMap.put("clasificacion7", DataImportAfectacionCompensada.getString("clasificacion7"));
					detalleMap.put("clasificacion8", DataImportAfectacionCompensada.getString("clasificacion8"));
					detalleMap.put("clasificacion9", DataImportAfectacionCompensada.getString("clasificacion9"));
					detalleMap.put("clasificacion10", DataImportAfectacionCompensada.getString("clasificacion10"));
					detalleMap.put("clasificacion11", DataImportAfectacionCompensada.getString("clasificacion11"));
					detalleMap.put("clasificacion12", DataImportAfectacionCompensada.getString("clasificacion12"));
					detalleMap.put("clasificacion13", DataImportAfectacionCompensada.getString("clasificacion13"));
					detalleMap.put("clasificacion14", DataImportAfectacionCompensada.getString("clasificacion14"));
					detalleMap.put("clasificacion15", DataImportAfectacionCompensada.getString("clasificacion15"));
					detalleMap.put("clasif1", DataImportAfectacionCompensada.getString("clasif1"));
					detalleMap.put("clasif2", DataImportAfectacionCompensada.getString("clasif2"));
					detalleMap.put("clasif3", DataImportAfectacionCompensada.getString("clasif3"));
					detalleMap.put("clasif4", DataImportAfectacionCompensada.getString("clasif4"));
					detalleMap.put("clasif5", DataImportAfectacionCompensada.getString("clasif5"));
					detalleMap.put("clasif6", DataImportAfectacionCompensada.getString("clasif6"));
					detalleMap.put("clasif7", DataImportAfectacionCompensada.getString("clasif7"));
					detalleMap.put("clasif8", DataImportAfectacionCompensada.getString("clasif8"));
					detalleMap.put("clasif9", DataImportAfectacionCompensada.getString("clasif9"));
					detalleMap.put("clasif10", DataImportAfectacionCompensada.getString("clasif10"));
					detalleMap.put("clasif11", DataImportAfectacionCompensada.getString("clasif11"));
					detalleMap.put("clasif12", DataImportAfectacionCompensada.getString("clasif12"));
					detalleMap.put("clasif13", DataImportAfectacionCompensada.getString("clasif13"));
					detalleMap.put("clasif14", DataImportAfectacionCompensada.getString("clasif14"));
					detalleMap.put("clasif15", DataImportAfectacionCompensada.getString("clasif15"));
					
					Map<String, Object> result2 = dispatcher.runSync("ACompensadaE", detalleMap, 43200 ,false);

					if (ServiceUtil.isError(result2)) {
						return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result2));
					}
				}
				
				Map<String, Object> crearMap = FastMap.newInstance();
				crearMap.put("agrupadorP", agrupador);
				crearMap.put("performFind", "C");
				crearMap.put("tipoClave", tipoClave);
				crearMap.put("userLogin", userLogin);

				Map<String, Object> result3 = dispatcher.runSync("ACompensadaE", crearMap, 43200 ,false);

				if (ServiceUtil.isError(result3)) {
					return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result3));
				} else {
					String acctgTransId = (String) result3.get("acctgTransId");
					GenericValue AcctgTrans = delegator.findByPrimaryKey("AcctgTrans",UtilMisc.toMap("acctgTransId",acctgTransId));
					regreso = ServiceUtil.returnSuccess("Se gener\u00f3 la p\u00f3liza "+AcctgTrans.getString("poliza"));
					for (GenericValue DataImportAfectacionCompensada : listDataImportAfectacionCompensada) {
						UtilImport.registrarExitoso(DataImportAfectacionCompensada);
					}
				}
				
				Debug.logInfo("Afectacion compensada creada" + result3, module);
				
			}
			
			delegator.storeAll(listDataImportAfectacionCompensada);
			TransactionUtil.commit();
			
			
		} catch (ParseException | GenericEntityException | GenericServiceException | NullPointerException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		
		return regreso;
	}
	
}