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
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.base.entities.DataImportCompDevNomPresClave;
import org.opentaps.dataimport.UtilImport;

import com.ibm.icu.util.Calendar;


public class CompromisoDevengoNominaImportService {
	
	private static final String MODULE = CompromisoDevengoNominaImportService.class.getName();
	private static String organizationPartyId;
	private static String tipoClave;
	private static String eventoContableId;
	private static Timestamp fechaContable;
	private static String descripcion;
	private static String tipoMovimiento;
	private static String userLoginId;
	public int importedRecords;

	/**
	 * Metodo que importa el el compromiso devengo nomina , hace el impacto contable-presupuestal
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> importCompromisoDevengoNomina(DispatchContext dctx, Map<String, ?> context) {
		
		Locale locale = (Locale) context.get("locale");
		TimeZone timeZone =  (TimeZone) context.get("timeZone");
		Delegator delegator = dctx.getDelegator();
		
		
		Map<String, Object> regreso = ServiceUtil.returnSuccess();
		
		try {
			
			organizationPartyId = (String)context.get("organizationPartyId");
			tipoClave = (String) context.get("tipoClave");
			eventoContableId = (String) context.get("eventoContable");
			String fecha = (String) context.get("fechaContable");
			fechaContable = UtilDateTime.stringToTimeStamp(fecha, "dd-MM-yyyy", timeZone, locale);
			descripcion = (String) context.get("descripcion");
			tipoMovimiento = (String) context.get("tipoMovimiento");
			userLoginId = ((GenericValue) context.get("userLogin")).getString("userLoginId");
			
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
					EntityCondition.makeCondition("acctgTransTypeId", eventoContableId), UtilMisc.toList("momentoCompara","comparacion"), UtilMisc.toList("secuencia")));
			
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
			List<String> fieldsToSelect = delegator.getModelEntity("DataImportCompDevNomPresClave").getAllFieldNames();
			
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
			
			List<GenericValue> listDataImportCompDevNomPresClave = delegator.findByCondition(
					"DataImportCompDevNomPresClave", condicionClave, fieldsToSelect, orderBy);
			
			List<GenericValue> listDataImportCompDevNomPres = delegator.findByCondition(
					"DataImportCompDevNomPres", UtilImport.condicionesSinProcesar(userLoginId), null, orderBy);
			
			Map<String,GenericValue> mapDataImportCompDevNomPresClave = FastMap.newInstance();
			
			for (GenericValue DataImportCompDevNomPresClave : listDataImportCompDevNomPresClave) {
				mapDataImportCompDevNomPresClave.put(DataImportCompDevNomPresClave.getString("idCompDevNominaPres"), DataImportCompDevNomPresClave);
			}

			BigDecimal monto =  BigDecimal.ZERO;
			BigDecimal montoCompara =  BigDecimal.ZERO;
			String idCompDevNominaPres = new String();
			for (GenericValue DataImportCompDevNomPres : listDataImportCompDevNomPres) {
				
				idCompDevNominaPres = DataImportCompDevNomPres.getString("idCompDevNominaPres");
				GenericValue DataImportCompDevNomPresClave = mapDataImportCompDevNomPresClave.get(idCompDevNominaPres);
				
				UtilImport.limpiarRegistroError(DataImportCompDevNomPres);
				
				if(UtilValidate.isEmpty(DataImportCompDevNomPresClave)){
					UtilImport.registrarError(DataImportCompDevNomPres, "No existe la clave presupuestal");
					error = true;
				}
				if(UtilValidate.isNotEmpty(DataImportCompDevNomPresClave)){
					if(UtilValidate.isNotEmpty(DataImportCompDevNomPresClave.getString("inactivo"))){
						UtilImport.registrarError(DataImportCompDevNomPres, 
								"La clave presupuestal ["+DataImportCompDevNomPres.getString("clavePresupuestal")+"] esta inactiva");
						error = true;
					}
				}
				monto = UtilNumber.getBigDecimal(DataImportCompDevNomPres.getBigDecimal("monto"));
				if(UtilValidate.isEmpty(DataImportCompDevNomPres.getBigDecimal("monto"))){
					UtilImport.registrarError(DataImportCompDevNomPres, 
							"Es necesario ingresar el monto  ");
					error = true;
				}
				if(validacionCompara && UtilValidate.isNotEmpty(DataImportCompDevNomPresClave)){
					montoCompara = UtilNumber.getBigDecimal(DataImportCompDevNomPresClave.getBigDecimal("montoControl"));
					if(monto.compareTo(montoCompara) > 0){
						UtilImport.registrarError(DataImportCompDevNomPres, 
								"No existe suficiencia presupuestal : "+
									montoCompara.subtract(monto));
						error = true;
					}
				}
				
				// Creamos la lineaMotor y se llena
				LineaMotorContable lineaMotorContable = new LineaMotorContable();
				lineaMotorContable.setClavePresupuestal(DataImportCompDevNomPres.getString("clavePresupuestal"));
				lineaMotorContable.setMontoPresupuestal(DataImportCompDevNomPres.getBigDecimal("monto"));
				
				if(UtilValidate.isNotEmpty(LineaPresupuestal)){
					LineaPresupuestalCopia = (GenericValue) LineaPresupuestal.clone();
	
					if(tieneAuxAbono && UtilValidate.isEmpty(DataImportCompDevNomPresClave.getString("auxiliarAbono"))){
						UtilImport.registrarError(DataImportCompDevNomPres, 
								"Es necesario ingresar el auxiliar de abono");
						error = true;
					} else if(tieneAuxAbono) {
						LineaPresupuestalCopia.set("catalogoAbono",
								LineaPresupuestal.getString("catalogoAbono").equalsIgnoreCase("BANCO") ? "BANCO" : 
									DataImportCompDevNomPresClave.getString("auxiliarAbono"));
					}
					if(tieneAuxCargo && UtilValidate.isEmpty(DataImportCompDevNomPresClave.getString("auxiliarCargo"))){
						UtilImport.registrarError(DataImportCompDevNomPres, 
								"Es necesario ingresar el auxiliar de cargo");
						error = true;
					} else if(tieneAuxCargo) {
						LineaPresupuestalCopia.set("catalogoCargo",
								LineaPresupuestal.getString("catalogoCargo").equalsIgnoreCase("BANCO") ? "BANCO" : 
									DataImportCompDevNomPresClave.getString("auxiliarCargo"));
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
			
			List<GenericValue> listDataImportCompDevNomCont = delegator.findByCondition(
					"DataImportCompDevNomCont", UtilImport.condicionesSinProcesar(userLoginId), null, orderBy);
			
			List<GenericValue> lineasContables = delegator.findByAnd("LineaContable", "acctgTransTypeId", eventoContableId);
			
			Map<String, GenericValue> mapLineaContable = FastMap.newInstance();
			for (GenericValue LineaContable : lineasContables) {
				mapLineaContable.put(LineaContable.getString("descripcion").trim().toUpperCase(), LineaContable);
			}
			
			Map<String, GenericValue> mapaLineasContable = FastMap.newInstance();
			
			for (GenericValue DataImportCompDevNomCont : listDataImportCompDevNomCont) {
				//Obtenemos la lineaContable del mapa
				GenericValue LineaContable = (GenericValue) mapLineaContable.get(
						DataImportCompDevNomCont.getString("descripcionLinea").trim().toUpperCase()).clone();
				
				if(UtilValidate.isEmpty(LineaContable)){
					UtilImport.registrarError(DataImportCompDevNomCont, 
							"La descripci\u00f3n ["+DataImportCompDevNomCont.getString("descripcionLinea")+
								"] no existe en el evento ["+eventoContableId+"]");
					continue;
				}
				
				if(UtilValidate.isNotEmpty(LineaContable.getString("catalogoAbono"))){
					if(UtilValidate.isEmpty(DataImportCompDevNomCont.getString("auxiliarAbono"))){
						UtilImport.registrarError(DataImportCompDevNomCont,
								"Es necesario ingresar el auxiliar de abono  ");
						error = true;
					}
					LineaContable.set("catalogoAbono", DataImportCompDevNomCont.getString("auxiliarAbono"));
				}
				
				if(UtilValidate.isNotEmpty(LineaContable.getString("catalogoCargo"))){
					if(UtilValidate.isEmpty(DataImportCompDevNomCont.getString("auxiliarCargo"))){
						UtilImport.registrarError(DataImportCompDevNomCont,
								"Es necesario ingresar el auxiliar de cargo  ");
						error = true;
					}
					LineaContable.set("catalogoCargo", DataImportCompDevNomCont.getString("auxiliarCargo"));
				}
				
				LineaContable.set("monto", DataImportCompDevNomCont.getBigDecimal("monto"));
				mapaLineasContable.put(DataImportCompDevNomCont.getString("descripcionLinea"),LineaContable);
			}
			
			if(!listLineaMotor.isEmpty()){
				listLineaMotor.get(0).setLineasContables(mapaLineasContable);
			}
			
			TransactionUtil.begin(43200);
			
			if(!error){
				
				// Hacemos la poliza.
				Debug.logInfo("CDN-transacciones",MODULE);
				Debug.logInfo("Total de lineas motor.- " + listLineaMotor.size(),MODULE);

				Map<String, Object> input = FastMap.newInstance();
				input.put("fechaRegistro", UtilDateTime.nowTimestamp());
				input.put("fechaContable", fechaContable);
				input.put("eventoContableId", eventoContableId);
				input.put("usuario", userLoginId);
				input.put("organizationId", organizationPartyId);
				input.put("currency", "MXN");
				input.put("descripcion", descripcion);
				input.put("tipoClaveId", tipoClave);
				input.put("lineasMotor", listLineaMotor);
				input.put("tipoMovimiento", tipoMovimiento);
				Map<String, Object> result = dctx.getDispatcher().runSync("creaTransaccionMotor", input, 43200, false);
				
				if (ServiceUtil.isError(result)) {
					return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
				} else {
					GenericValue AcctgTrans = (GenericValue) result.get("transaccion");
					regreso = ServiceUtil.returnSuccess("Se gener\u00f3 la p\u00f3liza "+AcctgTrans.getString("poliza"));
					for (GenericValue DataImportCompDevNomPres : listDataImportCompDevNomPres) {
						UtilImport.registrarExitoso(DataImportCompDevNomPres);
					}
					for (GenericValue DataImportCompDevNomCont : listDataImportCompDevNomCont) {
						UtilImport.registrarExitoso(DataImportCompDevNomCont);
					}
				}
			}
			
			delegator.storeAll(listDataImportCompDevNomPres);
			delegator.storeAll(listDataImportCompDevNomCont);
			
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
	private static GenericValue getLineaPresupuestalMatriz(Delegator delegator) throws GenericEntityException{
		
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

}