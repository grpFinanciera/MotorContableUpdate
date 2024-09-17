package org.opentaps.dataimport.domain;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.opentaps.dataimport.MotorContable;
import org.opentaps.dataimport.UtilImport;

public class PresupuestoInicialImportService {

	private static final String MODULE = PresupuestoInicialImportService.class.getName();
	private static final Map<String,String> mapaNumeroMes = UtilMisc.toMap("01","enero", "02","febrero", "03","marzo", 
			"04","abril", "05","mayo", "06","junio", "07","julio", "08","agosto", "09","septiembre","10","octubre","11","noviembre","12","diciembre");
	private String organizationPartyId;
	private String cicloId;
	private String tipoClave;
	private String eventoContable;
	private String descripcion;
	private GenericValue userLogin;
	private String userLoginId;
	
	/**
	 * Metodo de importacion de presupuesto incial
	 * @param dctx
	 * @param context
	 * @return
	 */
	public Map<String,Object> importPresupuestoInicial(DispatchContext dctx, Map<String, ?> context){
		
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		
		Map<String, Object> regreso = ServiceUtil.returnSuccess();
		
		organizationPartyId = (String)context.get("organizationPartyId");	
		cicloId = (String)context.get("cicloId");
		tipoClave = (String)context.get("tipoClave");
		eventoContable = (String)context.get("eventoContable");
		descripcion = (String)context.get("descripcion");
		userLogin = (GenericValue) context.get("userLogin");
		userLoginId = userLogin.getString("userLoginId");
		
		boolean error = false;
		
		if(UtilValidate.isNotEmpty(tipoClave)){
			tipoClave = tipoClave.toUpperCase();
		}
		
		try {
			
			if(!(tipoClave.equalsIgnoreCase("EGRESO") || tipoClave.equalsIgnoreCase("INGRESO"))){
				return ServiceUtil.returnError("El tipo de clave es incorrecto, ingrese EGRESO o INGRESO");
			}
			
			// Lista de las lineasMotor.
			List<LineaMotorContable> listLineaMotor = new FastList<LineaMotorContable>();
			
			Map<String,GenericValue> mapDataImportPresupuestoInicial = FastMap.newInstance();
			
			List<GenericValue> listDataImportPresupuestoInicial = delegator.findByCondition(
					"DataImportPresupuestoInicia", UtilImport.condicionesSinProcesar(userLoginId), null, UtilMisc.toList("renglon"));
			for (GenericValue DataImportPresupuestoInicial : listDataImportPresupuestoInicial) {
				mapDataImportPresupuestoInicial.put(DataImportPresupuestoInicial.getString("idPresInicial"), DataImportPresupuestoInicial);
			}
			
			List<GenericValue> listDataImportPresupuestoInicialClave = delegator.findByCondition(
					"DataImportPresupuestoInicialClave", UtilImport.condicionesSinProcesar(userLoginId), null, UtilMisc.toList("renglon"));
			
			GenericValue EstructuraClave = UtilClavePresupuestal.obtenEstructPresupuestal(tipoClave, organizationPartyId, delegator, cicloId);
			
			if(UtilValidate.isEmpty(EstructuraClave)){
				return ServiceUtil.returnError("No se encontr\u00f3 la estructura de la clave");
			}
			
			GenericValue LineaPresupuestal = EntityUtil.getFirst(delegator.findByAnd("LineaPresupuestal", UtilMisc.toMap("acctgTransTypeId",eventoContable)));
			if(UtilValidate.isEmpty(LineaPresupuestal)){
				return ServiceUtil.returnError("No se encontr\u00f3 la linea presupuestal del evento ["+eventoContable+"]");
			}
			String momentoEjecutar1 = LineaPresupuestal.getString("momentoEjecutar1");
			String momentoEjecutar2 = LineaPresupuestal.getString("momentoEjecutar2");
			
			//Separacion en un mapa de los tipos de clasificaciones (CL_CICLO,CL_ADMINISTRATIVA,...) para guardar las clasficaciones correspondientes 
			String tipoClasificacion = new String();
			int numeroClasificaciones = 0;
			Map<String,Set<String>> mapaClasificaciones = FastMap.newInstance();
			for (int i = 1; i <= UtilClavePresupuestal.TAG_COUNT; i++) {
				tipoClasificacion = EstructuraClave.getString(UtilClavePresupuestal.VIEW_TAG_PREFIX+i);
				if(UtilValidate.isNotEmpty(tipoClasificacion)){
					UtilMisc.addToSetInMap(new String(), mapaClasificaciones, tipoClasificacion);
					numeroClasificaciones++;
				} else {
					break;
				}
			}
			
			String clasificacion = new String();
			GenericValue DataImportPresupuestoInicial = null;
			Map<String,Map<String,String>> mapaClaveClasificaciones = FastMap.newInstance();
			Map<String,String> mapaPosicionClasificacion = FastMap.newInstance();
			//Validacion inicial (clave presupuestal, activo o incativo) y llenado de mapa agrupadas por tipo de clasificacion 
			for (GenericValue DataImportPresupuestoInicialClave : listDataImportPresupuestoInicialClave) {
				DataImportPresupuestoInicial = mapDataImportPresupuestoInicial.get(DataImportPresupuestoInicialClave.getString("idPresInicial"));
				UtilImport.limpiarRegistroError(DataImportPresupuestoInicial);
				int numeroClasificacionesIngresadas = 0;
				
				for (int i = 1; i <= UtilClavePresupuestal.TAG_COUNT; i++) {
					clasificacion = DataImportPresupuestoInicialClave.getString(UtilClavePresupuestal.VIEW_TAG_PREFIX+i);
					if(UtilValidate.isNotEmpty(clasificacion)){
						tipoClasificacion = EstructuraClave.getString(UtilClavePresupuestal.VIEW_TAG_PREFIX+i);
						UtilMisc.addToSetInMap(clasificacion, mapaClasificaciones, tipoClasificacion);
						numeroClasificacionesIngresadas++;
						mapaPosicionClasificacion.put(UtilClavePresupuestal.VIEW_TAG_PREFIX+i, clasificacion);
					} else {
						if(numeroClasificaciones != numeroClasificacionesIngresadas){
							UtilImport.registrarError(DataImportPresupuestoInicial, "El numero de clasificaciones no coincide con la estructura clave");
							error = true;
						}
						break;
					}
				}
				
				if(UtilValidate.isNotEmpty(DataImportPresupuestoInicialClave.getString("clavePresup"))){
					UtilImport.registrarError(DataImportPresupuestoInicial, "La clave presupuestal ya existe");
					error = true;
				} 
				mapaClaveClasificaciones.put(DataImportPresupuestoInicial.getString("clavePresupuestal"), mapaPosicionClasificacion);
				mapaPosicionClasificacion = FastMap.newInstance();
			}
			
			Map<String,Map<String,GenericValue>> mapIdClasificaciones = FastMap.newInstance();
			Map<String,GenericValue> mapaClasificacionEntidad = FastMap.newInstance();
			List<GenericValue> listClasificacion = FastList.newInstance();
			String nombreCampoClasif = new String();
			
			//Busqueda de las clasificaciones en Enumeration y Party y llenado de mapas para validar despues
			for(Map.Entry<String, Set<String>> EntryClasificaciones : mapaClasificaciones.entrySet()){
				
				mapaClasificacionEntidad = mapIdClasificaciones.get(EntryClasificaciones.getKey());
				
				if(UtilValidate.isEmpty(mapaClasificacionEntidad)){
					mapaClasificacionEntidad = FastMap.newInstance();
				}
				
				if (EntryClasificaciones.getKey().equalsIgnoreCase("CL_ADMINISTRATIVA")) {
					listClasificacion = delegator.findByCondition("Party", 
							EntityCondition.makeCondition("externalId",EntityOperator.IN,EntryClasificaciones.getValue()), null, null);
				} else {
			        EntityConditionList<EntityExpr> condicionEnum = EntityCondition.makeCondition(UtilMisc.toList(
			                EntityCondition.makeCondition("sequenceId",EntityOperator.IN,EntryClasificaciones.getValue()),
			                EntityCondition.makeCondition("enumTypeId", EntryClasificaciones.getKey())),EntityOperator.AND);
			        listClasificacion = delegator.findByCondition("Enumeration", condicionEnum, null, null);
				}
				
				for (GenericValue ClasificacionEntidad : listClasificacion) {
					if(EntryClasificaciones.getKey().equalsIgnoreCase("CL_ADMINISTRATIVA")){
						nombreCampoClasif = "externalId";
					} else {
						nombreCampoClasif = "sequenceId";
					}
					mapaClasificacionEntidad.put(ClasificacionEntidad.getString(nombreCampoClasif), ClasificacionEntidad);
				}
				mapIdClasificaciones.put(EntryClasificaciones.getKey(), mapaClasificacionEntidad);
			}
			
			GenericValue EntidadClasificacion = null;
			Date fechaValidar = null;
			List<MesMonto> listMesMonto = FastList.newInstance();
			BigDecimal montoInicial = BigDecimal.ZERO;
			Map<String,Map<String,BigDecimal>> mapaClaveMontoMes = FastMap.newInstance();
			Map<String,BigDecimal> mapaMesMonto = FastMap.newInstance();
			//Valdiacon de cada clasificacion (activo o inactivo)
			for (GenericValue DataImportPresupuestoInicialClave : listDataImportPresupuestoInicialClave) {
				
				DataImportPresupuestoInicial = mapDataImportPresupuestoInicial.get(DataImportPresupuestoInicialClave.getString("idPresInicial"));
				
				for (int i = 1; i <= UtilClavePresupuestal.TAG_COUNT; i++) {
					clasificacion = DataImportPresupuestoInicialClave.getString(UtilClavePresupuestal.VIEW_TAG_PREFIX+i);
					if(UtilValidate.isNotEmpty(clasificacion)){
						tipoClasificacion = EstructuraClave.getString(UtilClavePresupuestal.VIEW_TAG_PREFIX+i);
						
						mapaClasificacionEntidad = mapIdClasificaciones.get(tipoClasificacion);
						EntidadClasificacion = mapaClasificacionEntidad.get(clasificacion);
						
						if(UtilValidate.isEmpty(EntidadClasificacion)){
							UtilImport.registrarError(DataImportPresupuestoInicial, 
									"La clasificaci\u00f3n ["+clasificacion+"] no existe");
							error = true;
						} else {
							
							if(tipoClasificacion.equalsIgnoreCase("CL_ADMINISTRATIVA")){
								if(EntidadClasificacion.getString("state").equalsIgnoreCase("I")){
									UtilImport.registrarError(DataImportPresupuestoInicial, 
											"La clasificaci\u00f3n ["+clasificacion+"] esta inactiva");
									error = true;
								}
							} else {
								for(Map.Entry<String, String> EntryMes : mapaNumeroMes.entrySet()){
									fechaValidar = UtilDateTime.toSqlDate(Integer.parseInt(EntryMes.getKey()), 1, Integer.parseInt(cicloId));
									if(EntidadClasificacion.getDate("fechaInicio").compareTo(fechaValidar) > 0 || EntidadClasificacion.getDate("fechaFin").compareTo(fechaValidar) < 0){
										UtilImport.registrarError(DataImportPresupuestoInicial, 
												"La clasificaci\u00f3n ["+clasificacion+"] esta inactiva en el mes ["+EntryMes.getValue().toUpperCase()+"]");
										error = true;
									}
								}
							}
						}
						
					} else {
						break;
					}
					
				}
				String clavePresupuestal = DataImportPresupuestoInicial.getString("clavePresupuestal");
				Map<String, BigDecimal>  mapaMesesMontoAnt = FastMap.newInstance();
				if(mapaClaveMontoMes.containsKey(clavePresupuestal)){
					mapaMesesMontoAnt = mapaClaveMontoMes.get(clavePresupuestal);
				}
				BigDecimal montoMes = BigDecimal.ZERO;
				for(Map.Entry<String, String> EntryMes : mapaNumeroMes.entrySet()){
					
					
					fechaValidar = UtilDateTime.toSqlDate(Integer.parseInt(EntryMes.getKey()), 1, Integer.parseInt(cicloId));
					MesMonto MesMonto = new MesMonto();
					MesMonto.setFecha(UtilDateTime.toTimestamp(fechaValidar));
					MesMonto.setMesId(EntryMes.getKey());
					MesMonto.setMonto(UtilNumber.getBigDecimal(DataImportPresupuestoInicial.getBigDecimal(EntryMes.getValue())));
					montoInicial = montoInicial.add(MesMonto.getMonto());
					listMesMonto.add(MesMonto);
					
					if(UtilValidate.isNotEmpty(mapaMesesMontoAnt)){
						montoMes = MesMonto.getMonto().add(UtilNumber.getBigDecimal(mapaMesesMontoAnt.get(EntryMes.getKey())));
					} else {
						montoMes = MesMonto.getMonto();
					}
					
					mapaMesMonto.put(EntryMes.getKey(), montoMes);
					mapaClaveMontoMes.put(clavePresupuestal, mapaMesMonto);
					
					LineaMotorContable lineaMotor = new LineaMotorContable();
					lineaMotor.setClavePresupuestal(clavePresupuestal);
					lineaMotor.setMontoPresupuestal(MesMonto.getMonto());
					lineaMotor.setFecha(MesMonto.getFecha());
					listLineaMotor.add(lineaMotor);
					
				}
				
				mapaMesMonto = FastMap.newInstance();
			}
			
			TransactionUtil.begin(43200);
			
			if(!error){
				
				for(Entry<String, Map<String, String>> EntryClasificacion : mapaClaveClasificaciones.entrySet()){
					
					mapaPosicionClasificacion = EntryClasificacion.getValue();
					
					GenericValue ClavePresupuestal = delegator.makeValue("ClavePresupuestal");
					ClavePresupuestal.set("clavePresupuestal", EntryClasificacion.getKey().trim());
					ClavePresupuestal.set("tipo", tipoClave);
					ClavePresupuestal.set("idSecuencia", EstructuraClave.getString("idSecuencia"));
					for (int i = 1; i <= UtilClavePresupuestal.TAG_COUNT; i++) {
						ClavePresupuestal.set(UtilClavePresupuestal.VIEW_TAG_PREFIX+i, mapaPosicionClasificacion.get(UtilClavePresupuestal.VIEW_TAG_PREFIX+i));
					}
					delegator.create(ClavePresupuestal);
				}

				for(Entry<String, Map<String, BigDecimal>> EntryClaveMontoMes : mapaClaveMontoMes.entrySet()){
					
					mapaMesMonto = EntryClaveMontoMes.getValue();
					
					for(Entry<String, BigDecimal> EntryMontoMes : mapaMesMonto.entrySet()){
						GenericValue ControlPresupuestal = delegator.makeValue("ControlPresupuestal");
						ControlPresupuestal.set("clavePresupuestal", EntryClaveMontoMes.getKey().trim());
						ControlPresupuestal.set("mesId", EntryMontoMes.getKey());
						ControlPresupuestal.set("monto", BigDecimal.ZERO);
						ControlPresupuestal.set("ciclo", cicloId);
						ControlPresupuestal.set("momentoId", momentoEjecutar1);
						ControlPresupuestal.setNextSeqId();
						delegator.create(ControlPresupuestal);
						if(UtilValidate.isNotEmpty(momentoEjecutar2)){
							ControlPresupuestal = (GenericValue) ControlPresupuestal.clone();
							ControlPresupuestal.set("momentoId", momentoEjecutar2);
							ControlPresupuestal.setNextSeqId();
							delegator.create(ControlPresupuestal);
						}
					}
					
				}
				
				if(montoInicial.compareTo(BigDecimal.ZERO) != 0){
					
					// Hacemos la poliza.
					Debug.logInfo("PIIS-transacciones",MODULE);
					Debug.logInfo("Total de lineas motor.- " + listLineaMotor.size(),MODULE);

					Map<String, Object> input = FastMap.newInstance();
					input.put("fechaRegistro", UtilDateTime.toTimestamp("1", "1", cicloId, "0", "0", "0"));
					input.put("fechaContable", UtilDateTime.toTimestamp("1", "1", cicloId, "0", "0", "0"));
					input.put("eventoContableId", eventoContable);
					input.put("usuario", userLoginId);
					input.put("organizationId", organizationPartyId);
					input.put("currency", "MXN");
					input.put("tipoClaveId", tipoClave);
					input.put("descripcion", descripcion);
					input.put("lineasMotor", listLineaMotor);
					Map<String, Object> result = dispatcher.runSync("creaTransaccionMotor", input, 43200, false);
					
					if (ServiceUtil.isError(result)) {
						Debug.logError("Hubo Error al crear el la p\u00f3liza",MODULE);
						return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
					} else {
						GenericValue AcctgTrans = (GenericValue) result.get("transaccion");
						regreso = ServiceUtil.returnSuccess("Se gener\u00f3 la p\u00f3liza "+AcctgTrans.getString("poliza"));
						for (GenericValue DataImportPresupuestoInicial1 : listDataImportPresupuestoInicial) {
							UtilImport.registrarExitoso(DataImportPresupuestoInicial1);
						}
					}
					
				}
				
			}
			
			delegator.storeAll(listDataImportPresupuestoInicial);
			
			TransactionUtil.commit();
			
		} catch (GenericEntityException | GenericServiceException | NullPointerException e) {
			e.printStackTrace();
		}
		
		return regreso;
		
	}
	
	public class MesMonto {
		BigDecimal monto;
		Timestamp fecha;
		String mesId;
		String clavePresupuestal;

		public BigDecimal getMonto() {
			return monto;
		}

		public void setMonto(BigDecimal monto) {
			this.monto = monto;
		}

		public Timestamp getFecha() {
			return fecha;
		}

		public void setFecha(Timestamp fecha) {
			this.fecha = fecha;
		}
		
		public String getMesId() {
			return mesId;
		}

		public void setMesId(String mesId) {
			this.mesId = mesId;
		}
	}
	
}