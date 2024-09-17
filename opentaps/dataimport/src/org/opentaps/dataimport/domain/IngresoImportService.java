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

public class IngresoImportService {

	private static final String MODULE = IngresoImportService.class.getName();
	private static String organizationPartyId;
	private static String eventoDevengadoId;
	private static String eventoRecaudadoId;
	private static Timestamp fechaContable;
	private static String descripcion;
	private static final  String tipoClave = "INGRESO";
	private static GenericValue userLogin;
	private static String userLoginId;
	private static int anio;
	private static int mes;
	private static boolean error;
	private static List<GenericValue> listDataImportCompIngresoDevCont = FastList.newInstance();
	private static List<GenericValue> listDataImportCompIngresoRecCont = FastList.newInstance();
	private static Map<String, GenericValue> mapLineaContableRec = FastMap.newInstance();
	
	/**
	 * Realiza la importacion del ingreso , crea: Invoice, InvoiceItem, OrdenPago, OrdenPagoMulti y poliza  
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> importIngreso(DispatchContext dctx, Map<String, ?> context) {
		
		Locale locale = (Locale) context.get("locale");
		TimeZone timeZone =  (TimeZone) context.get("timeZone");
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		setError(false);
		
		Map<String, Object> regreso = ServiceUtil.returnSuccess();
		
		try{
			
			organizationPartyId = (String) context.get("organizationPartyId");
			eventoDevengadoId = (String) context.get("eventoDevengado");
			eventoRecaudadoId = (String) context.get("eventoRecaudado");
			descripcion = (String) context.get("descripcion");
			userLogin = (GenericValue) context.get("userLogin");
			userLoginId = userLogin.getString("userLoginId");
			String fecha = (String) context.get("fechaContable");
			fechaContable = UtilDateTime.stringToTimeStamp(fecha, "dd-MM-yyyy", timeZone, locale);
			Calendar fechaCalendario = UtilDateTime.toCalendar(fechaContable);
			mes = fechaCalendario.get(Calendar.MONTH)+1;
			anio = fechaCalendario.get(Calendar.YEAR);
			
			List<String> orderBy = UtilMisc.toList("renglon");
			
			List<GenericValue> listDataImportIngresoPres = delegator.findByCondition(
					"DataImportIngreso", UtilImport.condicionesSinProcesar(userLoginId), null, orderBy);
			
			Map<String,GenericValue> mapDataImportIngresoPres = FastMap.newInstance();
			Map<String,Map<String,String>> mapClavePresupuestalClasificaciones = FastMap.newInstance();
			
			String clavePresupuestal = new String();
			String clasificacion = new String();
			Map<String,String> mapaClasificiones = FastMap.newInstance();
			for (GenericValue DataImportIngresoPres : listDataImportIngresoPres) {
				mapDataImportIngresoPres.put(DataImportIngresoPres.getString("idIngreso"), DataImportIngresoPres);
				clavePresupuestal = DataImportIngresoPres.getString("clavePresupuestal");
				for (int i = 1; i <= UtilClavePresupuestal.TAG_COUNT; i++) {
					clasificacion = DataImportIngresoPres.getString(UtilClavePresupuestal.VIEW_TAG_PREFIX+i);
					if(UtilValidate.isNotEmpty(clasificacion)){
						mapaClasificiones.put(UtilClavePresupuestal.VIEW_TAG_PREFIX+i, DataImportIngresoPres.getString(UtilClavePresupuestal.VIEW_TAG_PREFIX+i));
					} else {
						break;
					}
				}
				if(UtilValidate.isNotEmpty(mapaClasificiones)){
					mapClavePresupuestalClasificaciones.put(clavePresupuestal, mapaClasificiones);
				}
				mapaClasificiones = FastMap.newInstance();
			}
			/**
			 * Devengado
			 */
			// Lista de las lineasMotor del devengaado
			List<LineaMotorContable> listLineaMotorDev = getLineaMotorValida(delegator, eventoDevengadoId, orderBy,mapDataImportIngresoPres);
			
			/**
			 * Recaudado
			 */
			List<LineaMotorContable> listLineaMotorRec = getLineaMotorValida(delegator, eventoRecaudadoId, orderBy,mapDataImportIngresoPres);
			
			TransactionUtil.begin(43200);
			
			if(!isError()){
				
				// Generamos el invoice.
				Map<String, Object> invoiceMap = FastMap.newInstance();
				invoiceMap.put("userLogin", userLogin);
				if(UtilValidate.isEmpty(listDataImportCompIngresoRecCont)){
					throw new GenericEntityException("Debe ingresar al menos un elemento en la pestaña Ingreso Recaudado");
				}
				String partyId = listDataImportCompIngresoRecCont.get(0).getString("auxiliarAbono");
				invoiceMap.put("partyId", partyId!=null?partyId:organizationPartyId);//auxiliar;
				invoiceMap.put("partyIdFrom", organizationPartyId);
				invoiceMap.put("acctgTransTypeId", eventoDevengadoId);
				invoiceMap.put("invoiceTypeId", "SALES_INVOICE");
				invoiceMap.put("invoiceDate", fechaContable);
				invoiceMap.put("dueDate", fechaContable);
				invoiceMap.put("currencyUomId", "MXN");
				invoiceMap.put("openAmount", BigDecimal.ZERO);
				invoiceMap.put("statusId", "DEVENGADO");
				invoiceMap.put("description", descripcion);

				Map<String, Object> result = dispatcher.runSync("createInvoice", invoiceMap, 43200, false);
				
				if (ServiceUtil.isError(result)) {
					Debug.logError("Hubo Error al crear Invoice",MODULE);
					return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
				}
				
				String invoiceId = (String) result.get("invoiceId");
				String invoiceItemSeqId = "";
				
				// Generar un registro en tabla OrdenCobro
				GenericValue ordenCobro = GenericValue.create(delegator
						.getModelEntity("OrdenCobro"));
				String ordenCobroId = delegator.getNextSeqId("OrdenCobro");
				ordenCobro.set("ordenCobroId", ordenCobroId);
				ordenCobro.set("invoiceId", invoiceId);
				ordenCobro.set("acctgTransTypeId", eventoRecaudadoId);
				ordenCobro.set("statusId", "ORDEN_COBRO_PENDIENT");
				delegator.create(ordenCobro);
				
				for (LineaMotorContable lineaMotorContable : listLineaMotorRec) {
					Map<String, Object> inputItem = FastMap.newInstance();
					inputItem.put("userLogin", userLogin);
					inputItem.put("invoiceId", invoiceId);
					inputItem.put("uomId", "MXN");
					inputItem.put("amount", lineaMotorContable.getMontoPresupuestal());
					inputItem.put("quantity", BigDecimal.ONE);
					inputItem.putAll(mapClavePresupuestalClasificaciones.get(lineaMotorContable.getClavePresupuestal()));
					
					result = dispatcher.runSync("createInvoiceItem",inputItem, 43200, false);
					
					if (ServiceUtil.isError(result)) {
						Debug.logError("Hubo Error al crear InvoiceItem",MODULE);
						return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
					}
					
					invoiceItemSeqId = (String) result.get("invoiceItemSeqId");
					GenericValue invoiceItem = delegator.findByPrimaryKey("InvoiceItem", 
							UtilMisc.toMap("invoiceId",invoiceId, "invoiceItemSeqId",invoiceItemSeqId));
					
					// Creamos la linea orden pagoMulti
					GenericValue ordenCobroMulti = GenericValue.create(delegator.getModelEntity("OrdenCobroMulti"));
					ordenCobroMulti.set("ordenCobroId", ordenCobroId);
					ordenCobroMulti.set("invoiceId", invoiceId);
					ordenCobroMulti.set("invoiceItemSeqId", invoiceItemSeqId);
					
					invoiceItem.set("montoRestante", BigDecimal.ZERO);
					delegator.store(invoiceItem);
					
					// Buscamos los auxiliares
					// Iteramos el mapa contable para generar las entries.
					if (UtilValidate.isNotEmpty(lineaMotorContable.getLineasPresupuestales())) {
						//Buscamos si el evento usa auxiliares RUME
						for (Map.Entry<String, GenericValue> entryLineaMotor : lineaMotorContable.getLineasPresupuestales().entrySet()) {
							ordenCobroMulti.set("idCatalogoAbono", entryLineaMotor.getValue().getString("catalogoAbono"));
							ordenCobroMulti.set("idCatalogoCargo", entryLineaMotor.getValue().getString("catalogoCargo"));
						}
					}

					ordenCobroMulti.set("secuenciaLineaContable", "0");
					ordenCobroMulti.set("monto",lineaMotorContable.getMontoPresupuestal());
					ordenCobroMulti.set("montoRestante",lineaMotorContable.getMontoPresupuestal());
					ordenCobroMulti.set("idCatalogoPres", "PRESUPUESTO");
					delegator.create(ordenCobroMulti);
					
				}
				
				String catalogoAbono = new String();
				String catalogoAbonoLC = new String();
				String catalogoCargo = new String();
				String catalogoCargoLC = new String();
				for (GenericValue DataImportCompIngresoRecCont : listDataImportCompIngresoRecCont) {
					GenericValue LineaContable = mapLineaContableRec.get(DataImportCompIngresoRecCont.getString("descripcionLinea").trim().toUpperCase());
					// Generamos el ordenCobroMulti para contabilidad
					GenericValue ordenCobroMulti = GenericValue.create(delegator.getModelEntity("OrdenCobroMulti"));
					ordenCobroMulti.set("ordenCobroId", ordenCobroId);
					ordenCobroMulti.set("invoiceId", invoiceId);
					ordenCobroMulti.set("invoiceItemSeqId", invoiceItemSeqId);
					ordenCobroMulti.set("secuenciaLineaContable",LineaContable.getString("secuencia"));
					ordenCobroMulti.set("monto", DataImportCompIngresoRecCont.getBigDecimal("monto"));
					ordenCobroMulti.set("montoRestante", DataImportCompIngresoRecCont.getBigDecimal("monto"));
					if(UtilValidate.isNotEmpty(LineaContable)){
						catalogoAbono = DataImportCompIngresoRecCont.getString("auxiliarAbono");
						catalogoCargo = DataImportCompIngresoRecCont.getString("auxiliarCargo");
						catalogoAbonoLC = LineaContable.getString("catalogoAbono");
						catalogoCargoLC = LineaContable.getString("catalogoCargo");
						if(UtilValidate.isNotEmpty(catalogoAbonoLC)){
							if(!catalogoAbonoLC.equalsIgnoreCase("BANCO") && UtilValidate.isEmpty(catalogoAbonoLC)){
								throw new GenericEntityException("Es necesario ingresar el auxiliar abono en el rengl\u00f3n "+DataImportCompIngresoRecCont.getLong("renglon"));
							}
							ordenCobroMulti.set("idCatalogoAbono",catalogoAbonoLC.equalsIgnoreCase("BANCO") ? "BANCO": catalogoAbono);
						}
						if(UtilValidate.isNotEmpty(catalogoCargoLC)){
							if(!catalogoCargoLC.equalsIgnoreCase("BANCO") && UtilValidate.isEmpty(catalogoCargoLC)){
								throw new GenericEntityException("Es necesario ingresar el auxiliar cargo en el rengl\u00f3n "+DataImportCompIngresoRecCont.getLong("renglon"));
							}
							ordenCobroMulti.set("idCatalogoCargo",catalogoCargoLC.equalsIgnoreCase("BANCO") ? "BANCO": catalogoCargo);
						}
					} else {
						Debug.logWarning("No se encontro la linea contable con descripcion "+
								DataImportCompIngresoRecCont.getString("descripcionLinea").trim().toUpperCase(), MODULE);
					}
					delegator.create(ordenCobroMulti);
				}
				
				Map<String, Object> input = FastMap.newInstance();
				input.put("fechaRegistro", UtilDateTime.nowTimestamp());
				input.put("fechaContable", fechaContable);
				input.put("eventoContableId", eventoDevengadoId);
				input.put("usuario", userLoginId);
				input.put("organizationId", organizationPartyId);
				input.put("currency", "MXN");
				input.put("tipoClaveId", tipoClave);
				input.put("lineasMotor", listLineaMotorDev);
				input.put("descripcion", descripcion);
				input.put("roleTypeId", "BILL_FROM_VENDOR");
				input.put("campo", "invoiceId");
				input.put("valorCampo", invoiceId);
				
				result = dispatcher.runSync("creaTransaccionMotor",input, 43200, false);
				
				if (ServiceUtil.isError(result)) {
					Debug.logError("Hubo Error al crear Invoice",MODULE);
					return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
				} else {
					GenericValue AcctgTrans = (GenericValue) result.get("transaccion");
					regreso = ServiceUtil.returnSuccess("Se gener\u00f3 la p\u00f3liza "+AcctgTrans.getString("poliza")+" y la orden de cobro "+ordenCobroId);
					for (GenericValue DataImportIngresoPres : listDataImportIngresoPres) {
						UtilImport.registrarExitoso(DataImportIngresoPres);
					}
					for (GenericValue DataImportIngresoDevCont : listDataImportCompIngresoDevCont) {
						UtilImport.registrarExitoso(DataImportIngresoDevCont);
					}
					for (GenericValue DataImportIngresoRecCont : listDataImportCompIngresoRecCont) {
						UtilImport.registrarExitoso(DataImportIngresoRecCont);
					}
				}
			}
			
			delegator.storeAll(listDataImportIngresoPres);
			delegator.storeAll(listDataImportCompIngresoDevCont);
			delegator.storeAll(listDataImportCompIngresoRecCont);
			
			TransactionUtil.commit();
			
			
		}  catch (NullPointerException | ParseException | GenericEntityException | GenericServiceException e){
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		
		return regreso;
	}
	
	/**
	 * Regresa la lista de lineas motor contable y valida todos los campos y montos
	 * @param delegator
	 * @param eventoContableId
	 * @param anio
	 * @param mes
	 * @param orderBy
	 * @param mapDataImportIngresoPres 
	 * @return
	 * @throws GenericEntityException
	 */
	public static List<LineaMotorContable> getLineaMotorValida(Delegator delegator,String eventoContableId, 
			List<String> orderBy, Map<String, GenericValue> mapDataImportIngresoPres) throws GenericEntityException, NullPointerException{
		// Obtenemos la descripcion de las lineasPresupuestales que usan la
		// matriz de conversion.
		GenericValue LineaPresupuestalMatriz = getLineaPresupuestalMatriz(delegator,eventoContableId);
		GenericValue LineaPresupuestalCopia = null;
		
		GenericValue PrimeraLineaPresupuestal = EntityUtil.getFirst(delegator.findByCondition("LineaPresupuestal",
				EntityCondition.makeCondition(EntityOperator.AND,
						EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, eventoContableId),
						EntityCondition.makeCondition("secuencia", EntityOperator.EQUALS, Long.valueOf(1))),
						UtilMisc.toList("momentoCompara","comparacion"), UtilMisc.toList("secuencia")));
		
		String momentoIdCompara = new String();
		boolean validacionCompara = true;
		if(UtilValidate.isNotEmpty(PrimeraLineaPresupuestal)){
			validacionCompara = UtilValidate.isNotEmpty(PrimeraLineaPresupuestal.getString("comparacion"));
			momentoIdCompara = PrimeraLineaPresupuestal.getString("momentoCompara");
		}
		
		boolean tieneAuxAbono = false;
		boolean tieneAuxCargo = false;
		
		if(UtilValidate.isNotEmpty(LineaPresupuestalMatriz)){
			if(UtilValidate.isNotEmpty(LineaPresupuestalMatriz.getString("catalogoAbono"))){
				tieneAuxAbono = true;
			}
			if(UtilValidate.isNotEmpty(LineaPresupuestalMatriz.getString("catalogoCargo"))){
				tieneAuxCargo = true;
			}
		}
		
		List<String> fieldsToSelect = delegator.getModelEntity("DataImportIngresoClave").getAllFieldNames();
		
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
		
		//Se obtiene Tabla presupuestal con la clave y los montos para validaciones
		List<GenericValue> listDataImportIngresoClave = delegator.findByCondition("DataImportIngresoClave", UtilImport.condicionesSinProcesar(userLoginId), fieldsToSelect, orderBy);
		
		List<LineaMotorContable> listLineaMotorContable = FastList.newInstance();
		BigDecimal monto =  BigDecimal.ZERO;
		BigDecimal montoCompara =  BigDecimal.ZERO;
		String idIngreso = new String();
		for (GenericValue DataImportIngresoClave : listDataImportIngresoClave) {
			idIngreso = DataImportIngresoClave.getString("idIngreso");
			GenericValue DataImportIngresoPres = mapDataImportIngresoPres.get(idIngreso);
			
			UtilImport.limpiarRegistroError(DataImportIngresoPres);
			
			if(UtilValidate.isEmpty(DataImportIngresoClave.getString("clavePresup"))){
				UtilImport.registrarError(DataImportIngresoPres, "No existe la clave presupuestal");
				setError(true);
			}
			if(UtilValidate.isNotEmpty(DataImportIngresoClave.getString("inactivo"))){
				UtilImport.registrarError(DataImportIngresoPres, 
						"La clave presupuestal ["+DataImportIngresoClave.getString("clavePresupuestal")+"] esta inactiva");
				setError(true);
			}
			monto = UtilNumber.getBigDecimal(DataImportIngresoClave.getBigDecimal("monto"));
			if(UtilValidate.isEmpty(DataImportIngresoPres.getBigDecimal("monto"))){
				UtilImport.registrarError(DataImportIngresoPres, 
						"Es necesario ingresar el monto  ");
				setError(true);
			}
			if(validacionCompara){
				montoCompara = UtilNumber.getBigDecimal(DataImportIngresoClave.getBigDecimal("montoControl"));
				if(monto.compareTo(montoCompara) > 0){
					UtilImport.registrarError(DataImportIngresoPres, 
							"No existe suficiencia presupuestal : "+
								montoCompara.subtract(monto));
					setError(true);
				}
			}
			
			// Creamos la lineaMotor y se llena la parte presupuestal
			LineaMotorContable lineaMotorContable = new LineaMotorContable();
			lineaMotorContable.setClavePresupuestal(DataImportIngresoPres.getString("clavePresupuestal"));
			lineaMotorContable.setMontoPresupuestal(DataImportIngresoPres.getBigDecimal("monto"));
			
			if(UtilValidate.isNotEmpty(LineaPresupuestalMatriz)){
				LineaPresupuestalCopia = (GenericValue) LineaPresupuestalMatriz.clone();

				if(tieneAuxAbono && UtilValidate.isEmpty(DataImportIngresoClave.getString("auxiliarAbono"))){
					UtilImport.registrarError(DataImportIngresoPres, 
							"Es necesario ingresar el auxiliar de abono en el evento ["+eventoContableId+"]");
					setError(true);
				} else if(tieneAuxAbono) {
					LineaPresupuestalCopia.set("catalogoAbono",
							LineaPresupuestalMatriz.getString("catalogoAbono").equalsIgnoreCase("BANCO") ? "BANCO" : 
								DataImportIngresoClave.getString("auxiliarAbono"));
				}
				if(tieneAuxCargo && UtilValidate.isEmpty(DataImportIngresoClave.getString("auxiliarCargo"))){
					UtilImport.registrarError(DataImportIngresoPres, 
							"Es necesario ingresar el auxiliar de cargo en el evento ["+eventoContableId+"]");
					setError(true);
				} else if(tieneAuxCargo) {
					LineaPresupuestalCopia.set("catalogoCargo",
							LineaPresupuestalMatriz.getString("catalogoCargo").equalsIgnoreCase("BANCO") ? "BANCO" : 
								DataImportIngresoClave.getString("auxiliarCargo"));
				}
				
				Map<String, GenericValue> mapalineasPresupuestales = FastMap.newInstance();
				mapalineasPresupuestales.put(LineaPresupuestalCopia.getString("secuencia"), LineaPresupuestalCopia);
				
				lineaMotorContable.setLineasPresupuestales(mapalineasPresupuestales);
			}
			
			listLineaMotorContable.add(lineaMotorContable);
			
			
		}
		
		/*
		 * Contabilidad
		 */
		
		Map<String, GenericValue> mapaLineaContable = FastMap.newInstance();
		List<GenericValue> listDataImportIngresoCont = FastList.newInstance();
		if(eventoContableId.equals(eventoDevengadoId)){
			listDataImportCompIngresoDevCont = listDataImportIngresoCont = delegator.findByCondition(
					"DataImportDevIng", UtilImport.condicionesSinProcesar(userLoginId), null, orderBy);
		} else {
			mapLineaContableRec = mapaLineaContable;
			listDataImportCompIngresoRecCont = listDataImportIngresoCont = delegator.findByCondition(
					"DataImportRecIng", UtilImport.condicionesSinProcesar(userLoginId), null, orderBy);
		}
		
		List<GenericValue> lineasContables = delegator.findByAnd("LineaContable", "acctgTransTypeId", eventoContableId);
		
		
		for (GenericValue LineaContable : lineasContables) {
			mapaLineaContable.put(LineaContable.getString("descripcion").trim().toUpperCase(), LineaContable);
		}
		
		Map<String, GenericValue> mapaLineasContable = FastMap.newInstance();
		
		if(UtilValidate.isNotEmpty(lineasContables)){
			for (GenericValue DataImportIngresoCont : listDataImportIngresoCont) {
				//Obtenemos la lineaContable del mapa
				GenericValue LineaContableCopia = (GenericValue) mapaLineaContable.get(
						DataImportIngresoCont.getString("descripcionLinea").trim().toUpperCase()).clone();
				
				UtilImport.limpiarRegistroError(DataImportIngresoCont);
				
				if(UtilValidate.isEmpty(LineaContableCopia)){
					UtilImport.registrarError(DataImportIngresoCont, 
							"La descripci\u00f3n ["+DataImportIngresoCont.getString("descripcionLinea")+
								"] no existe en el evento ["+eventoContableId+"]");
					continue;
				}
				
				if(UtilValidate.isNotEmpty(LineaContableCopia.getString("catalogoAbono"))){
					if(UtilValidate.isEmpty(DataImportIngresoCont.getString("auxiliarAbono"))){
						UtilImport.registrarError(DataImportIngresoCont,
								"Es necesario ingresar el auxiliar de abono ");
						setError(true);
					}
					LineaContableCopia.set("catalogoAbono", DataImportIngresoCont.getString("auxiliarAbono"));
				}
				
				if(UtilValidate.isNotEmpty(LineaContableCopia.getString("catalogoCargo"))){
					if(UtilValidate.isEmpty(DataImportIngresoCont.getString("auxiliarCargo"))){
						UtilImport.registrarError(DataImportIngresoCont,
								"Es necesario ingresar el auxiliar de cargo ");
						setError(true);
					}
					LineaContableCopia.set("catalogoCargo", DataImportIngresoCont.getString("auxiliarCargo"));
				}
				
				LineaContableCopia.set("monto", DataImportIngresoCont.getBigDecimal("monto"));
				mapaLineasContable.put(DataImportIngresoCont.getString("descripcionLinea"),LineaContableCopia);
			}
		}

		if(!listLineaMotorContable.isEmpty()){
			listLineaMotorContable.get(0).setLineasContables(mapaLineasContable);
		}
		
		return listLineaMotorContable;
	}
	
	/**
	 * Metodo que regresa el objeto LineaPresupuestal que tiene matriz de conversion 'tipoMatrizId'
	 * @param delegator
	 * @return
	 * @throws GenericEntityException
	 */
	public static GenericValue getLineaPresupuestalMatriz(Delegator delegator, String eventoContableId) throws GenericEntityException{
		
		EntityConditionList<EntityExpr> matrizCondition = EntityCondition.makeCondition(
					UtilMisc.toList(EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS,eventoContableId),
							EntityCondition.makeCondition("tipoMatrizId", EntityOperator.NOT_EQUAL, null)));

		List<GenericValue> lineaPresupuestal = delegator.findByCondition("LineaPresupuestal", matrizCondition, null, null);				

		if (lineaPresupuestal.isEmpty()) {
			Debug.logWarning("No se encontr\u00f3 alguna linea presupuestal que utilice matriz en el evento ["+eventoContableId+"]",MODULE);
			return null;
		} else {
			if(lineaPresupuestal.size()>1){
				throw new GenericEntityException("Evento mal configurado, solo una linea presupuestal debe usar la matriz");
			}
			return lineaPresupuestal.get(0);
		}
	}
	
	public static boolean isError() {
		return error;
	}
	
	public static void setError(boolean error) {
		IngresoImportService.error = error;
	}
	
}
