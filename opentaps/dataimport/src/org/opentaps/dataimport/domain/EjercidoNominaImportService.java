package org.opentaps.dataimport.domain;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

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
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.util.LineaMotorContable;
import org.ofbiz.order.finaccount.UtilClavePresupuestal;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.dataimport.UtilImport;

import com.ibm.icu.util.Calendar;

public class EjercidoNominaImportService {
	
	private static final String MODULE = EjercidoNominaImportService.class.getName();
	private static String organizationPartyId;
	private static String tipoClave;
	private static String eventoEjercidoId;
	private static String eventoPagadoId;
	private static Timestamp fechaContable;
	private static String descripcion;
	private static String tipoMovimiento;
	private static GenericValue userLogin;
	private static String userLoginId;

	/**
	 * Metodo que importa el el ejercido pagado nomina , hace el impacto contable-presupuestal y tambien registra 
	 * las ordenes de pago
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> importEjercidoNomina(DispatchContext dctx, Map<String, ?> context){
		
		Locale locale = (Locale) context.get("locale");
		TimeZone timeZone =  (TimeZone) context.get("timeZone");
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		
		Map<String, Object> regreso = ServiceUtil.returnSuccess();
		
		try {
			
			organizationPartyId = (String)context.get("organizationPartyId");
			tipoClave = (String) context.get("tipoClave");
			eventoEjercidoId = (String) context.get("eventoEjercidoId");
			eventoPagadoId = (String) context.get("eventoPagadoId");
			String fecha = (String) context.get("fechaContable");
			fechaContable = UtilDateTime.stringToTimeStamp(fecha, "dd-MM-yyyy", timeZone, locale);
			descripcion = (String) context.get("descripcion");
			tipoMovimiento = (String) context.get("tipoMovimiento");
			userLogin = (GenericValue) context.get("userLogin");
			userLoginId = userLogin.getString("userLoginId");
			
			Calendar fechaCalendario = UtilDateTime.toCalendar(fechaContable);
			
			int mes = fechaCalendario.get(Calendar.MONTH)+1;
			int anio = fechaCalendario.get(Calendar.YEAR);
			
			if(tipoMovimiento == null){
				tipoMovimiento = "INTERNA";
			}
			
			// Lista de las lineasMotor.
			List<LineaMotorContable> listLineaMotor = new FastList<LineaMotorContable>();
			
			boolean error = false;
			boolean tieneAuxAbono = false;
			boolean tieneAuxCargo = false;
			
			/*
			 * Presupuesto
			 */
			
			GenericValue LineaPresupuestal = getLineaPresupuestalMatriz(delegator);
			GenericValue LineaPresupuestalCopia = null;
			
			GenericValue PrimeraLineaPresupuestal = EntityUtil.getFirst(delegator.findByCondition("LineaPresupuestal",
					EntityCondition.makeCondition("acctgTransTypeId", eventoEjercidoId), UtilMisc.toList("momentoCompara","comparacion"), UtilMisc.toList("secuencia")));
			
			String momentoIdCompara = new String();
			boolean validacionCompara = true;
			if(UtilValidate.isNotEmpty(PrimeraLineaPresupuestal)){
				validacionCompara = UtilValidate.isNotEmpty(PrimeraLineaPresupuestal.getString("comparacion"));
				momentoIdCompara = PrimeraLineaPresupuestal.getString("momentoCompara");
			}
			
			if(UtilValidate.isNotEmpty(LineaPresupuestal)){
				if(UtilValidate.isNotEmpty(LineaPresupuestal.getString("catalogoAbono"))){
					tieneAuxAbono = true;
				}
				if(UtilValidate.isNotEmpty(LineaPresupuestal.getString("catalogoCargo"))){
					tieneAuxCargo = true;
				}
			}
			
			List<String> orderBy = UtilMisc.toList("renglon");
			List<String> fieldsToSelect = delegator.getModelEntity("DataImportEjerNomPresClave").getAllFieldNames();
			
			EntityCondition condicionClave = UtilImport.condicionesSinProcesar(userLoginId);
			
			if(validacionCompara){
				condicionClave = EntityCondition.makeCondition(EntityOperator.AND,
						condicionClave,
		                EntityCondition.makeCondition("momentoId", EntityOperator.EQUALS, momentoIdCompara),
		                EntityCondition.makeCondition("ciclo", EntityOperator.EQUALS, String.valueOf(anio)),
		    	        EntityCondition.makeCondition("mesId", EntityOperator.EQUALS, UtilImport.formatoMes.format(mes)));
			} else {
				fieldsToSelect.remove("momentoId");
				fieldsToSelect.remove("ciclo");
				fieldsToSelect.remove("mesId");
				fieldsToSelect.remove("montoControl");
			}
			
			List<GenericValue> listDataImportEjerNomPresClave = delegator.findByCondition(
					"DataImportEjerNomPresClave", condicionClave, fieldsToSelect, orderBy);
			
			List<GenericValue> listDataImportEjerNomPres = delegator.findByCondition(
					"DataImportEjerNomPres", UtilImport.condicionesSinProcesar(userLoginId), null, orderBy);
			
			Map<String,GenericValue> mapDataImportEjerNomPres = FastMap.newInstance();
			Map<String,GenericValue> mapClaveClasificaciones = FastMap.newInstance();
			
			for (GenericValue DataImportEjerNomPres : listDataImportEjerNomPres) {
				mapDataImportEjerNomPres.put(DataImportEjerNomPres.getString("idEjerNominaPres"), DataImportEjerNomPres);
				mapClaveClasificaciones.put(DataImportEjerNomPres.getString("clavePresupuestal"), DataImportEjerNomPres);
			}
			
			BigDecimal monto =  BigDecimal.ZERO;
			BigDecimal montoCompara =  BigDecimal.ZERO;
			
			String idEjerNominaPres = new String();
			for (GenericValue DataImportEjerNomPresClave : listDataImportEjerNomPresClave) {
				
				idEjerNominaPres = DataImportEjerNomPresClave.getString("idEjerNominaPres");
				GenericValue DataImportEjerNomPres = mapDataImportEjerNomPres.get(idEjerNominaPres);
				
				UtilImport.limpiarRegistroError(DataImportEjerNomPres);
				
				if(UtilValidate.isEmpty(DataImportEjerNomPresClave.getString("clavePresup"))){
					UtilImport.registrarError(DataImportEjerNomPres, "No existe la clave presupuestal");
					error = true;
				}
				if(UtilValidate.isNotEmpty(DataImportEjerNomPresClave.getString("inactivo"))){
					UtilImport.registrarError(DataImportEjerNomPres, 
							"La clave presupuestal ["+DataImportEjerNomPresClave.getString("clavePresupuestal")+"] esta inactiva");
					error = true;
				}
				monto = UtilNumber.getBigDecimal(DataImportEjerNomPresClave.getBigDecimal("monto"));
				if(UtilValidate.isEmpty(DataImportEjerNomPres.getBigDecimal("monto"))){
					UtilImport.registrarError(DataImportEjerNomPres, 
							"Es necesario ingresar el monto  ");
					error = true;
				}
				if(validacionCompara){
					montoCompara = UtilNumber.getBigDecimal(DataImportEjerNomPresClave.getBigDecimal("montoControl"));
					if(monto.compareTo(montoCompara) > 0){
						UtilImport.registrarError(DataImportEjerNomPres, 
								"No existe suficiencia presupuestal : "+
									montoCompara.subtract(monto));
						error = true;
					}
				}
				
				// Creamos la lineaMotor y se llena
				LineaMotorContable lineaMotorContable = new LineaMotorContable();
				lineaMotorContable.setClavePresupuestal(DataImportEjerNomPres.getString("clavePresupuestal"));
				lineaMotorContable.setMontoPresupuestal(DataImportEjerNomPres.getBigDecimal("monto"));
				
				if(UtilValidate.isNotEmpty(LineaPresupuestal)){
					LineaPresupuestalCopia = (GenericValue) LineaPresupuestal.clone();
	
					if(tieneAuxAbono && UtilValidate.isEmpty(DataImportEjerNomPresClave.getString("auxiliarAbono"))){
						UtilImport.registrarError(DataImportEjerNomPres, 
								"Es necesario ingresar el auxiliar de abono");
						error = true;
					} else if(tieneAuxAbono){
						LineaPresupuestalCopia.set("catalogoAbono",
								LineaPresupuestal.getString("catalogoAbono").equalsIgnoreCase("BANCO") ? "BANCO" : 
									DataImportEjerNomPresClave.getString("auxiliarAbono"));
					}
					if(tieneAuxCargo && UtilValidate.isEmpty(DataImportEjerNomPresClave.getString("auxiliarCargo"))){
						UtilImport.registrarError(DataImportEjerNomPres, 
								"Es necesario ingresar el auxiliar de cargo");
						error = true;
					} else if(tieneAuxCargo){
						LineaPresupuestalCopia.set("catalogoCargo",
								LineaPresupuestal.getString("catalogoCargo").equalsIgnoreCase("BANCO") ? "BANCO" : 
									DataImportEjerNomPresClave.getString("auxiliarCargo"));
					}
					
					Map<String, GenericValue> mapalineasPresupuestales = FastMap.newInstance();
					mapalineasPresupuestales.put(LineaPresupuestalCopia.getString("secuencia"), LineaPresupuestalCopia);
					
					lineaMotorContable.setLineasPresupuestales(mapalineasPresupuestales);
				}
				
				listLineaMotor.add(lineaMotorContable);
				
			}
			
			/*
			 * Contabilidad
			 */
			
			List<GenericValue> listDataImportEjerNomCont = delegator.findByCondition(
					"DataImportEjerNomCont", UtilImport.condicionesSinProcesar(userLoginId), null, orderBy);
			
			List<GenericValue> lineasContables = delegator.findByAnd("LineaContable", "acctgTransTypeId", eventoPagadoId);
			
			Map<String, GenericValue> mapLineaContable = FastMap.newInstance();
			for (GenericValue LineaContable : lineasContables) {
				mapLineaContable.put(LineaContable.getString("descripcion").trim().toUpperCase(), LineaContable);
			}
			
			
			for (GenericValue DataImportEjerNomCont : listDataImportEjerNomCont) {
				UtilImport.limpiarRegistroError(DataImportEjerNomCont);
				//Obtenemos la lineaContable del mapa
				GenericValue LineaContable = (GenericValue) mapLineaContable.get(
						DataImportEjerNomCont.getString("descripcionLinea").trim().toUpperCase()).clone();
				
				if(UtilValidate.isEmpty(LineaContable)){
					UtilImport.registrarError(DataImportEjerNomCont, 
							"La descripci\u00f3n ["+DataImportEjerNomCont.getString("descripcionLinea")+
								"] no existe en el evento ["+eventoPagadoId+"]");
					continue;
				}
				
				if(UtilValidate.isNotEmpty(LineaContable.getString("catalogoAbono"))){
					if(UtilValidate.isEmpty(DataImportEjerNomCont.getString("auxiliarAbono")) &&
							!LineaContable.getString("catalogoAbono").equalsIgnoreCase("BANCO")){
						UtilImport.registrarError(DataImportEjerNomCont,
								"Es necesario ingresar el auxiliar de abono  ");
						error = true;
					}
				}
				
				if(UtilValidate.isNotEmpty(LineaContable.getString("catalogoCargo"))){
					if(UtilValidate.isEmpty(DataImportEjerNomCont.getString("auxiliarCargo")) &&
							!LineaContable.getString("catalogoCargo").equalsIgnoreCase("BANCO")){
						UtilImport.registrarError(DataImportEjerNomCont,
								"Es necesario ingresar el auxiliar de cargo  ");
						error = true;
					}
				}
			
			}
			
			TransactionUtil.begin(43200);
			
			if(!error){
				// Generamos el invoice.
				Map<String, Object> invoiceMap = FastMap.newInstance();
				invoiceMap.put("userLogin", userLogin);
				invoiceMap.put("partyId", organizationPartyId);
				String partyIdFrom = new String();
				if(!listDataImportEjerNomPres.isEmpty()){
					partyIdFrom = listDataImportEjerNomPres.get(0).getString("auxiliarAbono");
				}
				invoiceMap.put("partyIdFrom", UtilValidate.isNotEmpty(partyIdFrom) ? partyIdFrom : organizationPartyId);//auxiliar
				invoiceMap.put("acctgTransTypeId", eventoEjercidoId);
				invoiceMap.put("invoiceTypeId", "PURCHASE_INVOICE");
				invoiceMap.put("invoiceDate", fechaContable);
				invoiceMap.put("dueDate", fechaContable);
				invoiceMap.put("currencyUomId", "MXN");
				invoiceMap.put("openAmount", BigDecimal.ZERO);
				invoiceMap.put("statusId", "EJERCIDO");
				invoiceMap.put("description", descripcion);
				Map<String, Object> result = dispatcher.runSync("createInvoice", invoiceMap, 43200, false);

				//Revisar Rollback
				if (ServiceUtil.isError(result)) {
					Debug.logError("Hubo Error al crear el Invoice",MODULE);
					return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
				}
				
				String invoiceId = (String) result.get("invoiceId");
				String invoiceItemSeqId = "";

				// Generar un registro en tabla OrdenPago
				GenericValue ordenPago = GenericValue.create(delegator.getModelEntity("OrdenPago"));
				String ordenPagoId = delegator.getNextSeqId("OrdenPago");
				ordenPago.set("ordenPagoId", ordenPagoId);
				ordenPago.set("invoiceId", invoiceId);
				ordenPago.set("acctgTransTypeId", eventoPagadoId);
				ordenPago.set("statusId", "ORDEN_PAGO_PENDIENTE");
				delegator.create(ordenPago);
				
				// Recorremos cada lineaMotor para crear un ItemInvoice
				for (LineaMotorContable lineaMotorContable : listLineaMotor) {
					Map<String, Object> inputItem = FastMap.newInstance();
					inputItem.put("userLogin", userLogin);
					inputItem.put("invoiceId", invoiceId);
					inputItem.put("uomId", "MXN");
					inputItem.put("amount", lineaMotorContable.getMontoPresupuestal());
					inputItem.put("quantity", BigDecimal.ONE);
					inputItem.put("montoRestante", BigDecimal.ZERO);
					// Guardamos las clasificaciones.
					guardaClasificaciones(inputItem, mapClaveClasificaciones.get(lineaMotorContable.getClavePresupuestal()));

					result = dispatcher.runSync("createInvoiceItem", inputItem, 43200, false);

					if (ServiceUtil.isError(result)) {
						Debug.logError("Hubo Error al crear el InvoiceItem",MODULE);
						return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
					}
					
					invoiceItemSeqId = (String) result.get("invoiceItemSeqId");
					GenericValue invoiceItem = delegator.findByPrimaryKey("InvoiceItem", 
							UtilMisc.toMap("invoiceId",invoiceId, "invoiceItemSeqId",invoiceItemSeqId));
					
					invoiceItem.set("montoRestante", BigDecimal.ZERO);
					delegator.store(invoiceItem);

					// Creamos la linea orden pagoMulti
					GenericValue ordenPagoMulti = GenericValue.create(delegator.getModelEntity("OrdenPagoMulti"));
					ordenPagoMulti.set("ordenPagoId", ordenPagoId);
					ordenPagoMulti.set("invoiceId", invoiceId);
					ordenPagoMulti.set("invoiceItemSeqId", invoiceItemSeqId);

					// Buscamos los auxiliares
					// Iteramos el mapa contable para generar las entries
					// contables.
					if (lineaMotorContable.getLineasPresupuestales()!=null) {
						for (Map.Entry<String, GenericValue> entry : lineaMotorContable.getLineasPresupuestales().entrySet()) {
							ordenPagoMulti.set("idCatalogoAbono", entry.getValue().getString("catalogoAbono"));
							ordenPagoMulti.set("idCatalogoCargo", entry.getValue().getString("catalogoCargo"));
						}
					}

					ordenPagoMulti.set("secuenciaLineaContable", "0");
					ordenPagoMulti.set("monto",lineaMotorContable.getMontoPresupuestal());
					ordenPagoMulti.set("montoRestante",lineaMotorContable.getMontoPresupuestal());
					ordenPagoMulti.set("idCatalogoPres", "PRESUPUESTO");
					delegator.create(ordenPagoMulti);
				}
				
				//Se generan las lineas OrdenPagoMulti que contienen la parte contable
				for (GenericValue DataImportEjerNomCont : listDataImportEjerNomCont) {
					//Obtenemos la lineaContable del mapa
					GenericValue LineaContable = (GenericValue) mapLineaContable.get(
							DataImportEjerNomCont.getString("descripcionLinea").trim().toUpperCase()).clone();
					
					// Generamos el OrdenPagoMulti
					GenericValue ordenPagoMulti = GenericValue.create(delegator.getModelEntity("OrdenPagoMulti"));
					ordenPagoMulti.set("ordenPagoId", ordenPagoId);
					ordenPagoMulti.set("invoiceId", invoiceId);
					ordenPagoMulti.set("invoiceItemSeqId", invoiceItemSeqId);
					ordenPagoMulti.set("secuenciaLineaContable",LineaContable.get("secuencia"));
					ordenPagoMulti.set("monto", DataImportEjerNomCont.getBigDecimal("monto"));
					ordenPagoMulti.set("montoRestante", DataImportEjerNomCont.getBigDecimal("monto"));
					if (LineaContable.getString("catalogoAbono") != null) {
						if(LineaContable.getString("catalogoAbono").equalsIgnoreCase("BANCO"))
						{	
							ordenPagoMulti.set("idCatalogoAbono", "BANCO");								
						}
						ordenPagoMulti.set("idCatalogoAbono",
								LineaContable.getString("catalogoAbono").equalsIgnoreCase("BANCO") ? "BANCO": 
									DataImportEjerNomCont.getString("auxiliarAbono"));
					}						
					
					if (LineaContable.getString("catalogoCargo") != null &&
							!LineaContable.getString("catalogoCargo").equalsIgnoreCase("BANCO")) {
						if(LineaContable.getString("catalogoCargo").equalsIgnoreCase("BANCO"))
						{
							ordenPagoMulti.set("idCatalogoCargo", "BANCO");								
						}
						ordenPagoMulti.set("idCatalogoCargo",
								LineaContable.getString("catalogoCargo").equalsIgnoreCase("BANCO") ? "BANCO": 
									DataImportEjerNomCont.getString("auxiliarCargo"));
					}						
					delegator.create(ordenPagoMulti);
				}
				
				// Hacemos la poliza del ejercido.
				Debug.logInfo("CDN-transacciones",MODULE);
				Debug.logInfo("Total de lineas motor.- " + listLineaMotor.size(),MODULE);

				Map<String, Object> input = FastMap.newInstance();
				input.put("fechaRegistro", UtilDateTime.nowTimestamp());
				input.put("fechaContable", fechaContable);
				input.put("eventoContableId", eventoEjercidoId);
				input.put("usuario", userLoginId);
				input.put("organizationId", organizationPartyId);
				input.put("currency", "MXN");
				input.put("tipoClaveId", tipoClave);
				input.put("lineasMotor", listLineaMotor);
				input.put("descripcion", descripcion);
				input.put("roleTypeId", "BILL_FROM_VENDOR");
				input.put("campo", "invoiceId");
				input.put("valorCampo", invoiceId);
				input.put("tipoMovimiento", tipoMovimiento);
				
				result = dispatcher.runSync("creaTransaccionMotor", input, 43200, false);
				
				
				if (ServiceUtil.isError(result)) {
					return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
				} else {
					GenericValue AcctgTrans = (GenericValue) result.get("transaccion");
					regreso = ServiceUtil.returnSuccess("Se gener\u00f3 la p\u00f3liza "+AcctgTrans.getString("poliza")+" y la orden de pago "+ordenPagoId);
					for (GenericValue DataImportCompDevNomPres : listDataImportEjerNomPres) {
						UtilImport.registrarExitoso(DataImportCompDevNomPres);
					}
					for (GenericValue DataImportCompDevNomCont : listDataImportEjerNomCont) {
						UtilImport.registrarExitoso(DataImportCompDevNomCont);
					}
				}
				
			}
			
			delegator.storeAll(listDataImportEjerNomPres);
			delegator.storeAll(listDataImportEjerNomCont);
			
			TransactionUtil.commit();
			
		} catch (ParseException | GenericEntityException | GenericServiceException | NullPointerException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		
		return regreso;
	}
	
	/**
	 * Metodo que regresa el objeto LineaPresupuestal que tiene matriz de conversion 'tipoMatrizId'
	 * @param delegator
	 * @return
	 * @throws GenericEntityException
	 */
	public static GenericValue getLineaPresupuestalMatriz(Delegator delegator) throws GenericEntityException{
		
		EntityConditionList<EntityExpr> matrizCondition = EntityCondition.makeCondition(
					UtilMisc.toList(EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS,eventoPagadoId),
							EntityCondition.makeCondition("tipoMatrizId", EntityOperator.NOT_EQUAL, null)));

		List<GenericValue> lineaPresupuestal = delegator.findByCondition("LineaPresupuestal", matrizCondition, null, null);				

		if (lineaPresupuestal.isEmpty()) {
			Debug.logWarning("No se encontr\u00f3 alguna linea presupuestal que utilice matriz en el evento ["+eventoPagadoId+"]",MODULE);
			return null;
		} else {
			if(lineaPresupuestal.size()>1){
				throw new GenericEntityException("Evento mal configurado, solo una linea presupuestal debe usar la matriz");
			}
			return lineaPresupuestal.get(0);
		}
	}
	
	/**
	 * Guarda las clasificaciones en el mapa del contexto
	 * @param inputItem
	 * @param DataImportObject
	 */
	public static void guardaClasificaciones(Map<String, Object> inputItem, GenericValue DataImportObject){
		
		String clasificacion = new String();
		String nombreCampo =  new String();
		for (int i = 1; i < 16; i++) {
			nombreCampo = UtilClavePresupuestal.VIEW_TAG_PREFIX+i;
			clasificacion = DataImportObject.getString(nombreCampo);
			if(UtilValidate.isNotEmpty(clasificacion)){
				inputItem.put(nombreCampo, clasificacion);
			} else {
				break;
			}
		}
		
	}
	
}
