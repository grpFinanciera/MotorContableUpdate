package org.opentaps.financials.motor;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilNumber;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.LineaMotorContable;
import org.ofbiz.order.finaccount.UtilClavePresupuestal;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.common.util.UtilDate;
import org.opentaps.foundation.repository.RepositoryException;

import com.ibm.icu.util.Calendar;
import com.opensourcestrategies.financials.util.UtilMotorContable;

public class MotorContable {

	// Map<String, String> cuentas;
	private static final String clasificacionAdministrativa = "CL_ADMINISTRATIVA";
	private static Map<String, String> naturalezaCuentas; 
	private static GenericValue estructura;
	private static int contador;
	private static List<GenericValue> periodos;
	private static final DecimalFormat formatoMes = new DecimalFormat("00");
	
	private static final String module = MotorContable.class.getName(); 
	
	/**
	 * Regresa un mapa de las cuentas de registro con su naturaleza.
	 * @param delegator
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map<String, String> getNaturalezaCuentas(Delegator delegator) throws GenericEntityException{
		//buscamos todas las cuentas que sean de registro.
		List <GenericValue> cuentasRegistro = delegator.findByAndCache("GlAccount", UtilMisc.toMap("tipoCuentaId", "R"));
		List <String> cuentas = new FastList <String>();
		for(GenericValue cuenta : cuentasRegistro){
			cuentas.add(cuenta.getString("glAccountId"));
		}
		
		return UtilMotorContable.getMapaCuentaNaturaleza(cuentas, delegator);
		
	}
	
	
	/**
	 * Busca la estructura del ciclo y tipo ingresado 
	 * @param delegator
	 * @param ciclo
	 * @param tipoClave
	 * @return GenericValue Estructura
	 * @throws GenericEntityException
	 */
	public static GenericValue getEstructuraClave(Delegator delegator,
			String ciclo, String tipoClave) throws GenericEntityException {
		List<GenericValue> estructura = delegator
				.findByAndCache("EstructuraClave", UtilMisc.toMap("ciclo", ciclo,
						"acctgTagUsageTypeId", tipoClave));
		if (estructura.isEmpty()) {
			Debug.logInfo("Ocurrio algo raro",module);
			return null;
		} else {
			return estructura.get(0);
		}
	}

	/**
	 * Verifica que no exista la clave presupuestal.
	 * @param delegator
	 * @param clave
	 * @return true = existe : false =  no existe
	 * @throws GenericEntityException
	 */
	public static boolean existenciaClave(Delegator delegator, String clave)
			throws GenericEntityException {
		GenericValue generic = delegator.findByPrimaryKey("ClavePresupuestal",
				UtilMisc.toMap("clavePresupuestal", clave));
		if (generic == null) {
			return false;
		}
		return true;
	}

	/**
	 * Afecta todas las lineas presupuestales del eventoContable.
	 * @param delegator
	 * @param idTipoDoc
	 * @param clave
	 * @param fecha
	 * @param monto
	 * @param ciclo
	 * @param tipoClave
	 * @param mapControl 
	 * @return
	 * @throws GenericEntityException
	 */
	public static String controlPresupuestal(Delegator delegator,
			String clave, Timestamp fecha, BigDecimal monto,
			String ciclo,List <GenericValue> cuentasPresupuestales, 
			String tipoClave, boolean validaSuficiencia, 
			Map<String, GenericValue> mapControl) throws GenericEntityException {
		Debug.logInfo("Entra control presupuestal",module);
		
		String mesId = formatoMes.format(getMes(fecha));

		String mensaje="";
		
		//Para cada linea se hara el mov presupuestal.
		for(GenericValue lineaPresupuestal: cuentasPresupuestales){
			Debug.logInfo("lineaPresupuestal: " + lineaPresupuestal,module);
			String mComparativo = lineaPresupuestal.getString("momentoCompara");
			String mEjecutar1 = lineaPresupuestal.getString("momentoEjecutar1");
			String mEjecutar2 = lineaPresupuestal.getString("momentoEjecutar2");
			Debug.logInfo("mComparativo: " + mComparativo,module);
			if (mComparativo != null && !mComparativo.isEmpty()) {
				if(tipoClave.equalsIgnoreCase("EGRESO") && validaSuficiencia){
					if (comparaMontos(delegator, monto, lineaPresupuestal.getString("comparacion"),
							mapControl.get(clave+mComparativo+mesId))) {
						restaMonto(delegator, monto,mapControl.get(clave+mComparativo+mesId));
					} else {
						Debug.logInfo("Saldo insuficiente(Momento "+mComparativo+")",module);
						mensaje += montoInsuficiencia(fecha,monto,mapControl.get(clave+mComparativo+mesId),clave)+"\n";
					}					
				}else{
					restaMonto(delegator, monto,mapControl.get(clave+mComparativo+mesId));
				}				
				
			}
			if (mEjecutar1 != null && !mEjecutar1.isEmpty()) {
				GenericValue ControlPresupuestal = sumaMonto(delegator, clave, mEjecutar1, fecha, monto, ciclo,mapControl.get(clave+mEjecutar1+mesId));
				if(UtilValidate.isNotEmpty(ControlPresupuestal)){
					mapControl.put(ControlPresupuestal.getString("clavePresupuestal")+
							ControlPresupuestal.getString("momentoId")+
								ControlPresupuestal.getString("mesId"), ControlPresupuestal);
				}
			}

			if (mEjecutar2 != null && !mEjecutar2.isEmpty()) {
				GenericValue ControlPresupuestal = sumaMonto(delegator, clave, mEjecutar2, fecha, monto, ciclo,mapControl.get(clave+mEjecutar2+mesId));
				if(UtilValidate.isNotEmpty(ControlPresupuestal)){
					mapControl.put(ControlPresupuestal.getString("clavePresupuestal")+
							ControlPresupuestal.getString("momentoId")+
								ControlPresupuestal.getString("mesId"), ControlPresupuestal);
				}
			}
		}
		
		return mensaje;
	}
	
	/**
	 * Regresa el monto de diferencia
	 * @param delegator
	 * @param idTipoDoc
	 * @param clave
	 * @param fecha
	 * @param monto
	 * @return
	 * @throws GenericEntityException
	 */
	private static String montoInsuficiencia(Timestamp fecha, BigDecimal monto,
			GenericValue ControlPresupuestal, String clavePresupuestal) {
		Debug.logInfo("Entra monto insuficiencia",module);
		int mes = getMes(fecha);
		BigDecimal diferencia = BigDecimal.ZERO;
		if(UtilValidate.isNotEmpty(ControlPresupuestal)){
			diferencia = ControlPresupuestal.getBigDecimal("monto").subtract(monto);
		} else {
			diferencia = monto.negate();
		}
		return "La clave ["+clavePresupuestal+"] no cuenta con suficiencia ["+diferencia+"] en el mes ["+mes+"]";
	}

	/**
	 * Consulta que este activa la validación presupuestal
	 * @param delegator
	 * @return
	 * @throws GenericEntityException
	 */
	private static boolean suficienciaActiva(Delegator delegator) throws GenericEntityException {
		GenericValue suficiencia = delegator.findByPrimaryKeyCache("SuficienciaPresupuestal",UtilMisc.toMap("suficienciaPresupuestalId", "1"));
		return suficiencia.getString("flag").equalsIgnoreCase("Y")?true:false;		
	}


	/**
	 * Lanza excepcion en caso de que no tenga suficiencia presupuestal.
	 * @param delegator
	 * @param eventoContableId
	 * @param clave
	 * @param fecha
	 * @param monto
	 * @throws GenericEntityException
	 */
	public static String validaSuficienciaPresupuestal(Delegator delegator,
			String eventoContableId, String clave, Timestamp fecha, BigDecimal monto)
			throws GenericEntityException {
		Debug.logInfo("Entra valida suficiencia",module);
		
		//Traemos el mComparativo de la primer lineaPresupuestal del Evento.
		//Se busca si el evento tiene movimientos presupuestales.
		List <EntityCondition> conditions = new FastList<EntityCondition>(); 
		conditions.add(EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("acctgTransTypeId",
						EntityOperator.EQUALS, eventoContableId)));
		List<GenericValue> lineasPresupuestales = delegator.findByAnd("LineaPresupuestal", conditions, UtilMisc.toList("secuencia"));
		
		String mComparativo = lineasPresupuestales.get(0).getString("momentoCompara");
		Calendar calFecha = UtilDateTime.toCalendar(fecha);
		if(mComparativo!=null && !mComparativo.isEmpty()){
			String mes = obtenMesString(calFecha.get(Calendar.MONTH));
			List<GenericValue> generics = delegator.findByAnd(
					"ControlPresupuestal", UtilMisc.toMap("clavePresupuestal",
							clave, "mesId", mes, "momentoId",
							mComparativo));
			if(generics == null || generics.isEmpty())
				throw new GenericEntityException("No Existe Suficiencia Presupuestal para la clave "+clave+": "+monto);
				GenericValue generic = generics.get(0);
			BigDecimal montoFinal = generic.getBigDecimal("monto").subtract(monto);
			
			if(montoFinal.compareTo(BigDecimal.ZERO)==-1){
				return "No Existe Suficiencia Presupuestal para la clave "+clave+": "+montoFinal.toString();
			}			
		}
		return "";
	}

	/**
	 * Recibe un mes de tipo int y lo transforma a string de 2 digitos.
	 * @param mes
	 * @return
	 */
	public static String obtenMesString(int mes) {
		mes++;
		return mes < 10 ? "0" + new Integer(mes).toString() : new Integer(mes).toString();
	};
	
	/**
	 * Aumenta el monto de la clave presupuestal en el mes y momento. 
	 * @param delegator
	 * @param clave
	 * @param momento
	 * @param fecha
	 * @param monto
	 * @param ciclo
	 * @throws GenericEntityException
	 */
	public static GenericValue sumaMonto(Delegator delegator, String clave,
			String momento, Timestamp fecha, BigDecimal monto, String ciclo, GenericValue ControlPresupuestal)
			throws GenericEntityException {
		String mes = formatoMes.format(getMes(fecha));
		Debug.logInfo("Momento: " + momento,module);
		if (UtilValidate.isNotEmpty(ControlPresupuestal)) {
			Debug.logInfo("Si existe controlPres",module);
			Debug.logInfo("CP.- " + ControlPresupuestal.getString("idSecuencia"),module);
			Debug.logInfo("Monto.- " + ControlPresupuestal.getBigDecimal("monto"),module);
			BigDecimal original = ControlPresupuestal.getBigDecimal("monto");
			ControlPresupuestal.set("monto", original.add(monto));
			Debug.logInfo("monto.- " + ControlPresupuestal.getString("monto"),module);
			return null;
		} else {
			ControlPresupuestal = GenericValue.create(delegator
					.getModelEntity("ControlPresupuestal"));
			ControlPresupuestal.setNextSeqId();
			ControlPresupuestal.set("clavePresupuestal", clave);
			ControlPresupuestal.set("mesId", mes);
			ControlPresupuestal.set("momentoId", momento);
			ControlPresupuestal.set("monto", monto);
			ControlPresupuestal.set("ciclo", ciclo);
			return ControlPresupuestal;
		}
	}

	/**
	 * Disminuye el monto de la clave presupuestal en el mes y momento.
	 * @param delegator
	 * @param clave
	 * @param momento
	 * @param fecha
	 * @param monto
	 * @throws GenericEntityException
	 */
	public static void restaMonto(Delegator delegator,BigDecimal monto, GenericValue ControlPresupuestal)
			throws GenericEntityException {
		
		if (UtilValidate.isNotEmpty(ControlPresupuestal)) {
			BigDecimal original = ControlPresupuestal.getBigDecimal("monto");
			ControlPresupuestal.set("monto", original.subtract(monto));
		} else {
			throw new GenericEntityException("No existe registro de la clave presupuestal.");
		}
	}
	
	/**
	 * Disminuye el monto de la clave presupuestal en el mes y momento.
	 * @param delegator
	 * @param clave
	 * @param momento
	 * @param fecha
	 * @param monto
	 * @throws GenericEntityException
	 */
	public static void restaMontoReversa(Delegator delegator, BigDecimal monto,GenericValue ControlPresupuestal)
			throws GenericEntityException {
		if (UtilValidate.isNotEmpty(ControlPresupuestal)) {
			BigDecimal restante = ControlPresupuestal.getBigDecimal("monto").subtract(monto);
			if(restante.compareTo(BigDecimal.ZERO)==-1){
				throw new GenericEntityException("No Existe Suficiencia Presupuestal para la clave "+ControlPresupuestal.getString("clavePresupuestal")+": "+restante.toString());
			}
			ControlPresupuestal.set("monto", restante);
		} else {
			throw new GenericEntityException("No existe registro de la clave presupuestal.");
		}
	}

	/**
	 * Compara que la clave presupuestal tenga suficiencia en el momento, mes y ciclo ingresados.
	 * @param delegator
	 * @param fecha 
	 * @param clave
	 * @param momento
	 * @param fecha
	 * @param monto
	 * @param comparacion
	 * @return
	 * @throws GenericEntityException
	 */
	public static boolean comparaMontos(Delegator delegator, BigDecimal monto, String comparacion,GenericValue ControlPresupuestal)
			throws GenericEntityException {
		
		
		if (UtilValidate.isNotEmpty(ControlPresupuestal)) {
			BigDecimal original = ControlPresupuestal.getBigDecimal("monto");
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
		}
		
		return false;
	}


	/**
	 * Crea la transaccion (poliza) impactando presupuestal y contablemente en caso de que asi se requiera
	 * @param delegator
	 * @param fechaRegistro
	 * @param fechaContable
	 * @param eventoContableId
	 * @param usuario
	 * @param organizationId
	 * @param currency
	 * @param tipoClaveId
	 * @param roleTypeId
	 * @param description
	 * @param lineasMotor
	 * @param campo
	 * @param valorCampo
	 * @return
	 * @throws GenericEntityException
	 * @throws RepositoryException
	 */
	public static GenericValue creaTransaccion(Delegator delegator,
			Timestamp fechaRegistro, Timestamp fechaContable,
			String eventoContableId, String usuario, String organizationId,
			String currency, String tipoClaveId, String roleTypeId, String description,
			List<LineaMotorContable> lineasMotor, String campo, String valorCampo,
			String tipoMovimiento)
			throws GenericEntityException, RepositoryException {
		
		Timestamp fechaContableAux = (Timestamp) fechaContable.clone();
		
		GenericValue UserLogin = delegator.findByPrimaryKey("UserLogin",UtilMisc.toMap("userLoginId",usuario));
		
		Debug.logInfo("comienza la creacion de la transaccion",module);
		//Traemos la naturaleza de las cuentas.
		naturalezaCuentas = getNaturalezaCuentas(delegator);

		//En caso de que no se tenga la Entidad Contable, se busca
		String partyId = obtenPadre(delegator, organizationId);
		
		//Obtenemos los datos del evento contable
		GenericValue EventoContable = delegator.findByPrimaryKeyCache("EventoContable", UtilMisc.toMap("acctgTransTypeId", eventoContableId));
		
		//Se busca si el evento tiene lineas presupuestales.
		List<GenericValue> lineasPresupuestales = delegator.findByAndCache("LineaPresupuestal", 
				UtilMisc.toMap("acctgTransTypeId",eventoContableId), UtilMisc.toList("secuencia"));
		
		//Se obtiene un mapa con las lineas contables , para buscarlo una sola vez
		Map<Long,GenericValue> mapLineaContable = getMapaLineasContables(delegator, eventoContableId);
		

		// AcctgTrans
		GenericValue trans = GenericValue.create(delegator.getModelEntity("AcctgTrans"));
		trans.setNextSeqId();

		trans.set("transactionDate", fechaRegistro);
		trans.set("postedDate", fechaContable);
		trans.set("description", description);
		trans.set("isPosted", "Y");
		trans.set("createdByUserLogin", usuario);
		trans.set("lastModifiedByUserLogin", usuario);
		trans.set("partyId", partyId);
		trans.set("organizationPartyId", organizationId);
		trans.set("acctgTransTypeId",eventoContableId);
		trans.set("roleTypeId", roleTypeId);
		if(tipoMovimiento!=null){trans.set("tipoMovimiento", tipoMovimiento);}
		
		//Monto transaccion.
		BigDecimal montoTrans = BigDecimal.ZERO;
		
		Debug.logInfo("Busca periodos",module);
		periodos = getPeriodos(delegator, organizationId,
							fechaContable,false);
						
		// inventoryItemId, physicalInventoryId, fixedAssetId, contratoId,
		// invoiceId, paymentId, shipmentId, receiptId, workEffortId, orderId
		if(campo!=null&&!campo.isEmpty())
			trans.set(campo,valorCampo);
		
		//Generamos el numero de poliza y lo agregamos a la transaccion 
		String ciclo = getCiclo(fechaContable);
		generaNumeroPoliza(delegator, EventoContable,fechaContable,trans,eventoContableId);
		
		Map<String,String> mapCamposTipoClave = FastMap.newInstance();
		Map<String,GenericValue> mapMatrizCuentas = FastMap.newInstance();

		List<LinkedHashSet<String>> listaClavesPresup = getListClavesPresup(lineasMotor);
		
		if(UtilValidate.isNotEmpty(tipoClaveId)){
			estructura = getEstructuraClave(delegator, ciclo, tipoClaveId);
			trans.set("estructuraId", estructura.getString("idSecuencia"));
			mapCamposTipoClave = getMapCamposTipoClave(delegator,tipoClaveId,organizationId, ciclo);
			mapMatrizCuentas = getMatrizCuentas(delegator, listaClavesPresup, tipoClaveId, mapCamposTipoClave,eventoContableId);
		}else{
			validaExistenciaLineaPresupuestal(delegator,eventoContableId);
		}
		
		//Verificamos si se valida suficiencia presupuestal.
		boolean validaSuficiencia = suficienciaActiva(delegator);
		
		validaClavesInactivas(delegator,listaClavesPresup);
		Map<String,GenericValue> mapControl = getMapControlPresupuestal(delegator, fechaContable, lineasMotor);
		List<GenericValue> listAcctgTransEntry = FastList.newInstance();
		List<GenericValue> listGlAccountHistory = FastList.newInstance();
		List<GenericValue> listGlAccountOrganization = FastList.newInstance();
		List<GenericValue> listTraspasoBanco = FastList.newInstance();
		Map<String,Map<String,GenericValue>> mapaAuxiliaresContables =  getAuxiliaresEvento(delegator, eventoContableId,
													lineasMotor, UserLogin, organizationId, 
														fechaContable);
		
		//Creamos la transaccion		
		delegator.create(trans);
		
				
		String transId = trans.getString("acctgTransId");
		
		//Se crea un contador para el numero de lineas.
		contador = 0;
		
		//Se itera por cada linea del motor contable.
		for(LineaMotorContable linea : lineasMotor){
			contador++;
			
			//Se verifica si se tiene una fecha la linea
			if(linea.getFecha()!=null){
				fechaContable = linea.getFecha();				
			}
			
			if(lineasPresupuestales!=null&&!lineasPresupuestales.isEmpty()){
				//Hacemos el control presupuestal.
				String mensajeError = controlPresupuestal(delegator,
						linea.getClavePresupuestal(),
						fechaContable, linea.getMontoPresupuestal(), ciclo,
						lineasPresupuestales,tipoClaveId,validaSuficiencia,mapControl);
				if(!mensajeError.isEmpty()){
					throw new GenericEntityException(mensajeError);
				}
			
				//Creamos el acctTransEntry Generico.
				GenericValue acctgTransGen = generaAcctgTransPresupuestalGenerica(
						delegator, organizationId, linea.getClavePresupuestal(),
						estructura, transId, currency,
						linea.getMontoPresupuestal(), partyId,fechaContable);
				for(GenericValue lineaP : lineasPresupuestales){
					//Generamos los entries de cada linea.
					montoTrans = generaAcctgTransEntriesPresupuestal(delegator, lineaP, acctgTransGen, montoTrans,
							listGlAccountHistory, listGlAccountOrganization,listAcctgTransEntry);
					
					String tipoMatrizId = lineaP.getString("tipoMatrizId"); 
					if(usaMatriz(tipoMatrizId)){
						Map<String, GenericValue> mapaLineas = linea.getLineasPresupuestales();
						if(UtilValidate.isEmpty(mapaLineas)){
							throw new GenericEntityException("Necesita ingresar las lineas presupuestales de la clave ["+linea.getClavePresupuestal()+"]");
						}
						Debug.logInfo("secuenciaaaa: " + lineaP.getString("secuencia"),module);
						GenericValue lineaAux = mapaLineas.get(lineaP.getString("secuencia"));
						Debug.logInfo("mapaLineas: " + mapaLineas,module);
						Debug.logInfo("lineaAux: " + lineaAux,module);
						if (lineaAux != null) {
							String catalogoCargo = lineaAux.getString("catalogoCargo");
							String catalogoAbono = lineaAux.getString("catalogoAbono");
							GenericValue MatrizCuentas = mapMatrizCuentas.get(linea.getClavePresupuestal()+tipoMatrizId);
							if(UtilValidate.isEmpty(MatrizCuentas)){
								throw new GenericEntityException("No se encontraron cuentas configuradas"
										+ " en la matriz para la clave ["+linea.getClavePresupuestal()+"] y el tipo de matriz ["+tipoMatrizId+"]");
							}

							// Generamos los entries dada la matriz de
							// conversion.
							montoTrans = generaAcctgTransEntriesMatriz(delegator, lineaP, acctgTransGen, 
									tipoMatrizId, catalogoCargo, catalogoAbono, fechaContable, montoTrans, 
									description, usuario, 
									MatrizCuentas, //Cuentas Contables por matriz de conversion
									mapCamposTipoClave.get("campoClasificacion"),
									organizationId,
									listGlAccountHistory, listGlAccountOrganization,
									listTraspasoBanco,
									listAcctgTransEntry,
									mapaAuxiliaresContables);//nombre del campo a seleccionar dependiendo del tipo de clave Ej. EGRESO -> clasificacion6
						}
					}
				}
			}
			
			if(linea.getLineasContables()!=null&&!linea.getLineasContables().isEmpty()){
				//Creamos el acctTransEntry Generico.
				GenericValue acctgTransGen = generaAcctgTransContableGenerica(delegator, organizationId, transId, currency, partyId);	
		    
				//Iteramos el mapa contable para generar las entries contables.
		    	for (Map.Entry<String, GenericValue> entry : linea.getLineasContables().entrySet()) {
		    		montoTrans = generaAcctgTransEntriesContable(delegator, entry.getValue(), acctgTransGen,fechaContable,
		    							montoTrans,description, usuario, organizationId, listAcctgTransEntry,mapLineaContable,
		    							listGlAccountHistory,listGlAccountOrganization,listTraspasoBanco,mapaAuxiliaresContables);			    
		    	}
		    }
			
			fechaContable = fechaContableAux;//Se asigna el valor original de la fecha contable ingresada por el usuario
		}

		guardaDatosPoliza(delegator, listAcctgTransEntry, listTraspasoBanco, listGlAccountHistory, 
				listGlAccountOrganization, mapaAuxiliaresContables, mapControl);
		
		//Indicamos el valor de la transaccion.
		if(montoTrans.compareTo(BigDecimal.ZERO) == 0&& !(eventoContableId.equals("CIPE-01") || eventoContableId.equals("CIPE-02"))){
			throw new GenericEntityException("No se puede crear la poliza con monto total igual a cero");
		}
		trans.set("postedAmount", montoTrans);
		delegator.store(trans);

		return trans;
	}
	
	
	/**
	 * Regresa un mapa del lineas contables con llave secuencia
	 * @param delegator
	 * @param eventoContableId
	 * @return
	 * @throws GenericEntityException
	 */
	private static Map<Long, GenericValue> getMapaLineasContables(
			Delegator delegator, String eventoContableId) throws GenericEntityException {
		
		Map<Long, GenericValue> mapLineasContables = FastMap.newInstance();
		
		List<GenericValue> lineasContables = delegator.findByAndCache("LineaContable", 
				UtilMisc.toMap("acctgTransTypeId",eventoContableId), UtilMisc.toList("secuencia"));
		
		for (GenericValue LineaContable : lineasContables) {
			mapLineasContables.put(LineaContable.getLong("secuencia"), LineaContable);
		}
		
		return mapLineasContables;
	}


	/**
	 * Obtiene el nombre de los campos a selecionar dependiendo del tipo de clave 
	 * <tipoCampo,nombreCampo>
	 * @param delegator
	 * @param tipoClaveId
	 * @param organizationPartyId
	 * @return
	 * @throws GenericEntityException
	 */
	private static Map<String, String> getMapCamposTipoClave(
			Delegator delegator, String tipoClaveId, String organizationPartyId, String ciclo) throws GenericEntityException {
		
		Map<String, String> mapCamposTipoClave = FastMap.newInstance();
		
		int indice = -1;
		int indiceTipoGasto = -1;
		String campoClasificacion = new String();
		String campoTipoGasto = new String();
		
		if(tipoClaveId.equalsIgnoreCase("EGRESO")){
			
			indice = UtilClavePresupuestal.indiceCog(organizationPartyId, delegator, ciclo);
			indiceTipoGasto = UtilClavePresupuestal.indiceTipoGasto(organizationPartyId, delegator, ciclo);
			campoClasificacion = UtilClavePresupuestal.VIEW_TAG_PREFIX+indice; //clasificacionN (CL_COG) = cog
			campoTipoGasto = UtilClavePresupuestal.VIEW_TAG_PREFIX+indiceTipoGasto;// tipoGasto = clasificacionN (CL_TIPOGASTO)
			
			mapCamposTipoClave.put("campoClasificacion", campoClasificacion);
			mapCamposTipoClave.put("campoTipoGasto", campoTipoGasto);
			
		} else {
			
			indice = UtilClavePresupuestal.indiceCri(organizationPartyId, delegator, ciclo);
			campoClasificacion = UtilClavePresupuestal.VIEW_TAG_PREFIX+indice; //clasificacionN (CL_CRI) = cri
			
			mapCamposTipoClave.put("campoClasificacion", campoClasificacion);
		}
		
		return mapCamposTipoClave;
	}


	/**
	 * Obtiene un mapa de Cuentas Contables por medio de Matriz de conversion
	 * <clasificacionId+matrizId,GenerciValue 'CuentasMatriz' >
	 * @param delegator
	 * @param listaClavesPresup
	 * @param tipoClaveId
	 * @param mapCamposTipoClave
	 * @return
	 * @throws GenericEntityException
	 */
	private static Map<String, GenericValue> getMatrizCuentas(
			Delegator delegator, List<LinkedHashSet<String>> listaClavesPresup,
			String tipoClaveId, Map<String, String> mapCamposTipoClave,String eventoContableId) throws GenericEntityException {
		
		List<EntityCondition> listCondiciones = FastList.newInstance();
		String nombreEntidad = new String();
		String campoClasificacion = mapCamposTipoClave.get("campoClasificacion");
		String campoTipoGasto = mapCamposTipoClave.get("campoTipoGasto");
		List<String> fieldsToSelect = FastList.newInstance();
		
		if(tipoClaveId.equalsIgnoreCase("EGRESO")){
			
			listCondiciones = UtilMisc.toList(
					EntityCondition.makeCondition("tipo",tipoClaveId),
					EntityCondition.makeConditionWhere("CVE."+campoClasificacion.toUpperCase()+" = "+"ME.COG"),
					EntityCondition.makeConditionWhere("CVE."+campoTipoGasto.toUpperCase()+" = ME.TIPO_GASTO"), //Se pone el nombre del campo en bd
					EntityCondition.makeConditionWhere("LP.TIPO_MATRIZ_ID = ME.MATRIZ_ID")
					);
			
			nombreEntidad = "CuentasMatrizEgreso";
				
			fieldsToSelect = UtilMisc.toList("clavePresupuestal",campoClasificacion,campoTipoGasto,"matrizId","cargo","abono");
		} else {
			
			listCondiciones = UtilMisc.toList(
					EntityCondition.makeCondition("tipo",tipoClaveId),
					EntityCondition.makeConditionWhere("CVE."+campoClasificacion.toUpperCase()+" = "+"MI.CRI"),
					EntityCondition.makeConditionWhere("LP.TIPO_MATRIZ_ID = MI.MATRIZ_ID")
					);
			
			nombreEntidad = "CuentasMatrizIngreso";
			
			fieldsToSelect = UtilMisc.toList("clavePresupuestal",campoClasificacion,"matrizId","cargo","abono");
		}
		
		
		Map<String, GenericValue> mapMatrizCuentas = FastMap.newInstance();
		
		String clavePresupuestal = new String();
		String matrizId = new String();

		for (LinkedHashSet<String> listaClaves : listaClavesPresup) {

			listCondiciones.add(EntityCondition.makeCondition("acctgTransTypeId",eventoContableId));
			listCondiciones.add(EntityCondition.makeCondition("clavePresupuestal",EntityOperator.IN,listaClaves));
			
			
			List<GenericValue> listCuentasMatriz = delegator.findByCondition(nombreEntidad, EntityCondition.makeCondition(listCondiciones), fieldsToSelect, null);
			
			for (GenericValue CuentaMatriz : listCuentasMatriz) {
				clavePresupuestal = CuentaMatriz.getString("clavePresupuestal");
				matrizId = CuentaMatriz.getString("matrizId");
				
				if(UtilValidate.isEmpty(CuentaMatriz.getString("cargo"))){
					throw new GenericEntityException(
							"Error, el cargo del elemento [" + CuentaMatriz.getString(campoClasificacion)
							+ " , " + CuentaMatriz.getString(campoTipoGasto) + "]  no esta configurado en la matriz de conversi\u00f3n ");
				}
				if(UtilValidate.isEmpty(CuentaMatriz.getString("abono"))){
					throw new GenericEntityException(
							"Error, el abono del elemento [" + CuentaMatriz.getString(campoClasificacion)
							+ " , " + CuentaMatriz.getString(campoTipoGasto) + "] no esta configurado en la matriz de conversi\u00f3n ");
				}
				
				mapMatrizCuentas.put(clavePresupuestal+matrizId, CuentaMatriz);
			}
				
		}
		
		return mapMatrizCuentas;
	}


	/**
	 * Busca claves presupuestales inactivas
	 * @param delegator
	 * @param lineasMotor
	 * @throws GenericEntityException
	 */
	private static void validaClavesInactivas(Delegator delegator,
			List<LinkedHashSet<String>> list2milClavesPresup) throws GenericEntityException {
		
		EntityCondition condicionPrl = EntityCondition.makeCondition("inactivo",EntityOperator.EQUALS,"Y");
		
		for (LinkedHashSet<String> listaClaves : list2milClavesPresup) {
			
			EntityCondition condicionTemp = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("clavePresupuestal",EntityOperator.IN,listaClaves),condicionPrl);
			
			List<GenericValue> listClaveInactiva = delegator.findByCondition("ClavePresupuestal", condicionTemp, null,null);
			
			if(UtilValidate.isNotEmpty(listClaveInactiva)){
				String clave = listClaveInactiva.get(0).getString("clavePresupuestal");
				throw new GenericEntityException("La clave presupuestal "+clave+" esta inactiva");
			}
		}
		
	}
	
	/**
	 * Obtiene las claves presupuestales que se envian a motor contable , las agrega a una o varias listas
	 * cada lista puede tener hasta 1000 claves , debido a que en la sentencia IN de SQL SERVER solo acepta ese numero
	 * @param lineasMotor
	 * @return
	 */
	private static List<LinkedHashSet<String>> getListClavesPresup(List<LineaMotorContable> lineasMotor){
		
		List<LinkedHashSet<String>> list2milClavesPresup = FastList.newInstance();
		LinkedHashSet<String> listClavesPresup = new LinkedHashSet<String>();
		
		int cont = 1;
		for (LineaMotorContable lineaMotorContable : lineasMotor) {
			if(cont > 1000){
				list2milClavesPresup.add(listClavesPresup);
				listClavesPresup = new LinkedHashSet<String>();
				cont = 1;	
			}
			if(UtilValidate.isNotEmpty(lineaMotorContable.getClavePresupuestal())){
				listClavesPresup.add(lineaMotorContable.getClavePresupuestal());
			}
			cont++;
		}
		list2milClavesPresup.add(listClavesPresup);
		
		return list2milClavesPresup;
		
	}
	 
	/**
	 * Obtiene las claves presupuestales que se envian a motor contable , las agrega a una o varias listas
	 * cada lista puede tener hasta 1000 claves , debido a que en la sentencia IN de SQL SERVER solo acepta ese numero
	 * @param lineasMotor
	 * @return
	 */
	private static List<LinkedHashSet<String>> getListClavesPresupGeneric(List<GenericValue> lineasMotor){
		
		List<LinkedHashSet<String>> list2milClavesPresup = FastList.newInstance();
		LinkedHashSet<String> listClavesPresup = new LinkedHashSet<String>();
		
		int cont = 1;
		for (GenericValue lineaMotorContable : lineasMotor) {
			if(cont > 1000){
				list2milClavesPresup.add(listClavesPresup);
				listClavesPresup = new LinkedHashSet<String>();
				cont = 1;	
			}
			listClavesPresup.add(lineaMotorContable.getString("clavePresupuestal"));
			cont++;
		}
		list2milClavesPresup.add(listClavesPresup);
		
		return list2milClavesPresup;
		
	}
	
	/**
	 * Obtiene los registros a modificar y los agrega a un mapa <clavePresupuestal+momentoId+mesId,GenericValue>
	 * NOTA no se filtra por momento en esta consulta , esto se realiza por la llave para que se filtre despues
	 * @param delegator
	 * @param fechaContable
	 * @param listLineaMotor
	 * @return
	 * @throws GenericEntityException 
	 */
	private static Map<String,GenericValue> getMapControlPresupuestal(Delegator delegator,
				Timestamp fechaContable, List<LineaMotorContable> listLineaMotor) throws GenericEntityException{
		
		Map<String,GenericValue> mapControl = FastMap.newInstance();
		
        Calendar fechaCal = Calendar.getInstance();
        fechaCal.setTime(fechaContable);
		String mesContable = formatoMes.format(getMes(fechaContable)); 
		String ciclo = getCiclo(fechaContable);
		
		Map<String,List<String>> mapMesClave = FastMap.newInstance();
		
		List<String> listClave = FastList.newInstance();
		
		String mesId = new String();
		
		for (LineaMotorContable lineaMotorContable : listLineaMotor) {
			
			if(UtilValidate.isEmpty(lineaMotorContable.getFecha())){
				mesId = mesContable;
			} else {
				mesId = formatoMes.format(getMes(lineaMotorContable.getFecha()));
			}
			
			if(mapMesClave.containsKey(mesId)){
				listClave = mapMesClave.get(mesId);
			}
			listClave.add(lineaMotorContable.getClavePresupuestal());
			mapMesClave.put(mesId, listClave);
			
			listClave = FastList.newInstance();
			
		}
		
		List<GenericValue> listControl = FastList.newInstance();
		
		for (Map.Entry<String,List<String>> entryClave : mapMesClave.entrySet()){
			
			List<String> listMesClave = entryClave.getValue();
			Set<List<String>> listListMesClave = new LinkedHashSet<List<String>>();
			
			//Se crea sublistas de 1000 elementos debido a que la base de datos solo permite esa cantidad al utilizar la sentencia 'IN'
			if(listMesClave.size() >= 1000){
				int noVecesMil = (int) Math.ceil(listMesClave.size() / 1000d);
				for (int i = 0; i < noVecesMil; i++) {
					int multiplo = 1000*i;
					int indiceFin = (i == noVecesMil-1 ? listMesClave.size() : 1000+multiplo);
					listListMesClave.add(listMesClave.subList(multiplo, indiceFin));
				}
				
			} else {
				listListMesClave.add(listMesClave);
			}
			
			for (List<String> listClaves : listListMesClave) {
				EntityCondition condicion = EntityCondition.makeCondition(EntityOperator.AND,
						EntityCondition.makeCondition("clavePresupuestal",EntityOperator.IN,listClaves),
						EntityCondition.makeCondition("ciclo",EntityOperator.EQUALS,ciclo),
						EntityCondition.makeCondition("mesId",EntityOperator.EQUALS,entryClave.getKey()));
				listControl.addAll(delegator.findByAnd("ControlPresupuestal", condicion));
			}
		}
		
		for (GenericValue ControlPresupuestal : listControl) {
			mapControl.put(ControlPresupuestal.getString("clavePresupuestal")+
					ControlPresupuestal.getString("momentoId")+
					ControlPresupuestal.getString("mesId"), ControlPresupuestal);
		}
		
		return mapControl;
	}


	/**
	 * 
	 * @param delegator
	 * @param trans
	 * @param eventoContableId
	 * @param lineasMotor
	 * @param tipoClaveId
	 * @param usuario
	 * @return
	 * @throws GenericEntityException
	 * @throws RepositoryException
	 */
	public static GenericValue agregaEventoTransaccion(Delegator delegator,
			GenericValue trans, String eventoContableId, 
			List<LineaMotorContable> lineasMotor, String tipoClaveId,
			String usuario)
			throws GenericEntityException, RepositoryException {
		
		//Traemos la naturaleza de las cuentas.
		naturalezaCuentas = getNaturalezaCuentas(delegator);
		
		//Monto transaccion.
		BigDecimal montoTrans = trans.getBigDecimal("postedAmount");
		
		Debug.logInfo("Busca periodos",module);
		periodos = getPeriodos(delegator, trans.getString("organizationPartyId"),
							trans.getTimestamp("postedDate"),false);
		
		GenericValue UserLogin = delegator.findByPrimaryKey("UserLogin",UtilMisc.toMap("userLoginId",usuario));
		
		Timestamp fechaContable = trans.getTimestamp("postedDate");
		Timestamp fechaContableAux = (Timestamp) fechaContable.clone();
		String organizationId = trans.getString("organizationPartyId");
		String currency = trans.getString("currency");
		String partyId = trans.getString("partyId");

		String ciclo = getCiclo(fechaContable);
		
		
		Map<String,String> mapCamposTipoClave = FastMap.newInstance();
		Map<String,GenericValue> mapMatrizCuentas = FastMap.newInstance();

		List<LinkedHashSet<String>> listaClavesPresup = getListClavesPresup(lineasMotor);
		
		if(UtilValidate.isNotEmpty(tipoClaveId)){
			estructura = getEstructuraClave(delegator, ciclo, tipoClaveId);
			trans.set("estructuraId", estructura.getString("idSecuencia"));
			mapCamposTipoClave = getMapCamposTipoClave(delegator,tipoClaveId,organizationId, ciclo);
			mapMatrizCuentas = getMatrizCuentas(delegator, listaClavesPresup, tipoClaveId, mapCamposTipoClave,eventoContableId);
		}else{
			validaExistenciaLineaPresupuestal(delegator,eventoContableId);
		}
		
		validaClavesInactivas(delegator,listaClavesPresup);
		Map<String,GenericValue> mapControl = getMapControlPresupuestal(delegator, fechaContable, lineasMotor);
		Map<String,Map<String,GenericValue>> mapaAuxiliaresContables =  getAuxiliaresEvento(delegator, eventoContableId, 
												lineasMotor, UserLogin, organizationId, 
													fechaContable);
		List<GenericValue> listAcctgTransEntry = FastList.newInstance();
		List<GenericValue> listGlAccountHistory = FastList.newInstance();
		List<GenericValue> listGlAccountOrganization = FastList.newInstance();
		List<GenericValue> listTraspasoBanco = FastList.newInstance();
		
		//Creamos la transaccion
		delegator.store(trans);
				
		String transId = trans.getString("acctgTransId");
		
		Debug.logInfo("Currency: "+currency,module);
		if(currency == null){
			EntityCondition conditions1 = EntityCondition.makeCondition(
					EntityOperator.AND, EntityCondition.makeCondition(
							"acctgTransId", EntityOperator.EQUALS, transId));
			Debug.logInfo("condicion: "+conditions1,module);
			List<GenericValue> transEntry = delegator.findByCondition("AcctgTransEntry", conditions1, null,	null);
			Debug.logInfo("transEntry: "+transEntry,module);
			Iterator<GenericValue> entries = transEntry.iterator();
			GenericValue entry = entries.next();
			currency = entry.getString("currencyUomId");
		}
		
		//Obtenemos el numero del contador.
		if(true){
			List <GenericValue> entries = delegator.findByAnd("AcctgTransEntry", "acctgTransId", trans.getString("acctgTransId"));
			contador = entries.size()/2;			
		}
		
		//Verificamos si se valida suficiencia presupuestal.
		boolean validaSuficiencia = suficienciaActiva(delegator);
		
		//Se busca si el evento tiene movimientos presupuestales.
		List <EntityCondition> conditions = new FastList<EntityCondition>(); 
		conditions.add(EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("acctgTransTypeId",
						EntityOperator.EQUALS, eventoContableId)));
		List<GenericValue> lineasPresupuestales = delegator.findByAnd("LineaPresupuestal", conditions, UtilMisc.toList("secuencia"));
		
		//Se obtiene un mapa con las lineas contables , para buscarlo una sola vez
		Map<Long,GenericValue> mapLineaContable = getMapaLineasContables(delegator, eventoContableId);
		
		
		//Se itera por cada linea del motor contable.
		for(LineaMotorContable linea : lineasMotor){
			contador++;
			
			//Se verifica si se tiene una fecha la linea
			if(linea.getFecha()!=null){
				fechaContable = linea.getFecha();				
			}
			
			
			if(lineasPresupuestales!=null&&!lineasPresupuestales.isEmpty()){
				//Hacemos el control presupuestal.
				String mensajeError = controlPresupuestal(delegator,
						linea.getClavePresupuestal(),
						fechaContable, linea.getMontoPresupuestal(), ciclo,
						lineasPresupuestales,tipoClaveId,validaSuficiencia,mapControl);
				if(!mensajeError.isEmpty()){
					throw new GenericEntityException(mensajeError);
				}
				
				//Creamos el acctTransEntry Generico.
				GenericValue acctgTransGen = generaAcctgTransPresupuestalGenerica(
						delegator, organizationId, linea.getClavePresupuestal(),
						estructura, transId, currency,
						linea.getMontoPresupuestal(), partyId, fechaContable);
				for(GenericValue lineaPresupuestalBD : lineasPresupuestales){
					//Generamos los entries de cada linea.
					montoTrans = generaAcctgTransEntriesPresupuestal(delegator, lineaPresupuestalBD, acctgTransGen, montoTrans,
		    				listGlAccountHistory, listGlAccountOrganization,listAcctgTransEntry);
					
					String tipoMatrizId = lineaPresupuestalBD.getString("tipoMatrizId"); 
					if(usaMatriz(tipoMatrizId)){	
						Map<String, GenericValue> mapaLineas = linea.getLineasPresupuestales();
						GenericValue lineaAux = mapaLineas.get(lineaPresupuestalBD.getString("secuencia"));
						String catalogoCargo = lineaAux.getString("catalogoCargo");
						String catalogoAbono = lineaAux.getString("catalogoAbono");
						GenericValue MatrizCuentas = mapMatrizCuentas.get(linea.getClavePresupuestal()+tipoMatrizId);
						if(UtilValidate.isEmpty(MatrizCuentas)){
							throw new GenericEntityException("No se encontraron cuentas configuradas"
									+ " en la matriz de la clave ["+linea.getClavePresupuestal()+"] y el tipo de matriz ["+tipoMatrizId+"]");
						}
						
						//Generamos los entries dada la matriz de conversion.
						montoTrans = generaAcctgTransEntriesMatriz(
								delegator, lineaPresupuestalBD, acctgTransGen, 
								tipoMatrizId, catalogoCargo, catalogoAbono, 
								fechaContable, montoTrans, trans.getString("description"), 
								usuario, 
								MatrizCuentas, //Cuentas Contables por matriz de conversion
								mapCamposTipoClave.get("campoClasificacion"),
								organizationId,
								listGlAccountHistory, listGlAccountOrganization,
								listTraspasoBanco,
								listAcctgTransEntry,
								mapaAuxiliaresContables);//nombre del campo a seleccionar dependiendo del tipo de clave Ej. EGRESO -> clasificacion6

					}
				}
			}
			
			if(linea.getLineasContables()!=null&&!linea.getLineasContables().isEmpty()){
				//Creamos el acctTransEntry Generico.
				GenericValue acctgTransGen = generaAcctgTransContableGenerica(delegator, organizationId, transId, currency, partyId);
		    
				//Iteramos el mapa contable para generar las entries contables.
		    	for (Map.Entry<String, GenericValue> entry : linea.getLineasContables().entrySet()) {
		    		montoTrans = generaAcctgTransEntriesContable(delegator, entry.getValue(), acctgTransGen,fechaContable,
		    				montoTrans, trans.getString("description"), usuario, organizationId, listAcctgTransEntry,mapLineaContable,
		    				listGlAccountHistory, listGlAccountOrganization,listTraspasoBanco,mapaAuxiliaresContables);			    
		    	}
		    }
			fechaContable = fechaContableAux;//Se asigna el valor original de la fecha contable ingresada por el usuario
		}

		//Indicamos el valor de la transaccion.
		trans.set("postedAmount", montoTrans);
		delegator.store(trans);
		guardaDatosPoliza(delegator, listAcctgTransEntry, listTraspasoBanco, listGlAccountHistory, 
				listGlAccountOrganization, mapaAuxiliaresContables, mapControl);

		return trans;
	}
	
	/**
	 * Guarda todos los registros afectados que corresponden a la poliza
	 * @param delegator
	 * @param listAcctgTransEntry
	 * @param listTraspasoBanco
	 * @param listGlAccountHistory
	 * @param listGlAccountOrganization
	 * @param mapaAuxiliaresContables
	 * @param mapaAuxiliaresPresupuestales
	 * @param mapControl
	 * @throws GenericEntityException
	 */
	private static void guardaDatosPoliza(Delegator delegator, List<GenericValue> listAcctgTransEntry, 
			List<GenericValue> listTraspasoBanco, List<GenericValue> listGlAccountHistory, 
			List<GenericValue> listGlAccountOrganization, 
			Map<String, Map<String, GenericValue>> mapaAuxiliaresContables,
			Map<String, GenericValue> mapControl ) throws GenericEntityException{
		delegator.storeAll(listAcctgTransEntry);
		delegator.storeAll(listTraspasoBanco);
		delegator.storeAll(listGlAccountHistory);
		delegator.storeAll(listGlAccountOrganization);
		delegator.storeAll(new ArrayList<GenericValue>(mapaAuxiliaresContables.get("SaldoCatalogoParty").values()));
		delegator.storeAll(new ArrayList<GenericValue>(mapaAuxiliaresContables.get("SaldoCatalogoCuentaBancaria").values()));
		delegator.storeAll(new ArrayList<GenericValue>(mapaAuxiliaresContables.get("SaldoCatalogoProducto").values()));
		delegator.storeAll(new ArrayList<GenericValue>(mapControl.values()));
	}
	
	/**
	 * Realiza una suma final, para no duplicar en la tabla saldo catalogo 
	 */
	private static Map<String, GenericValue> sumaAuxiliaresContablePrespuestales(Map<String, Map<String, GenericValue>> mapaAuxiliaresContables, 
							Map<String, Map<String, GenericValue>> mapaAuxiliaresPresupuestales,String nombreCatalogo){
		
		Map<String, GenericValue> mapaSaldoContable = mapaAuxiliaresContables.get(nombreCatalogo);
		mapaSaldoContable = mapaSaldoContable == null ? new HashMap<String, GenericValue>() : mapaAuxiliaresContables.get(nombreCatalogo);
		Map<String, GenericValue> mapaSaldoPresupuestal = mapaAuxiliaresPresupuestales.get(nombreCatalogo);
		mapaSaldoPresupuestal = mapaSaldoPresupuestal == null ? new HashMap<String, GenericValue>() : mapaAuxiliaresPresupuestales.get(nombreCatalogo);
		
		Map<String, GenericValue> mapaSaldo = FastMap.newInstance();
		
		GenericValue SaldoCatalogo = null;
		GenericValue SaldoCatalogoOrigen = null;
		for(Entry<String, GenericValue> mapLineaContable : mapaSaldoContable.entrySet()){
			if(mapaSaldoPresupuestal.containsKey(mapLineaContable.getKey())){
				SaldoCatalogo = (GenericValue) mapLineaContable.getValue();
				SaldoCatalogoOrigen = mapaSaldoPresupuestal.get(mapLineaContable.getKey());
				BigDecimal suma = UtilNumber.getBigDecimal(SaldoCatalogo.getBigDecimal("monto")).add(
										UtilNumber.getBigDecimal(SaldoCatalogoOrigen.getBigDecimal("monto")));
				SaldoCatalogo.set("monto", suma);
				mapaSaldoPresupuestal.remove(mapLineaContable.getKey());
			}
		}
		
		for(Entry<String, GenericValue> mapLineaPresup : mapaSaldoPresupuestal.entrySet()){
			if(mapaSaldoContable.containsKey(mapLineaPresup.getKey())){
				SaldoCatalogo = (GenericValue) mapLineaPresup.getValue();
				SaldoCatalogoOrigen = mapaSaldoContable.get(mapLineaPresup.getKey());
				BigDecimal suma = UtilNumber.getBigDecimal(SaldoCatalogo.getBigDecimal("monto")).add(
										UtilNumber.getBigDecimal(SaldoCatalogoOrigen.getBigDecimal("monto")));
				SaldoCatalogo.set("monto", suma);
				mapaSaldoContable.remove(mapLineaPresup.getKey());
			}
		}
		
		mapaSaldo.putAll(mapaSaldoContable);
		mapaSaldo.putAll(mapaSaldoPresupuestal);
		
		return mapaSaldo;
		
	}
	
	/**
	 * Actualiza la secuencia del tipo de poliza
	 * @param delegator
	 * @param eventoContableId
	 * @param fechaContable
	 * @param eventoContableId 
	 * @return
	 * @throws GenericEntityException
	 */
	public static String generaNumeroPoliza(Delegator delegator, GenericValue EventoContable, Timestamp fechaContable, GenericValue trans, String eventoContableId) throws GenericEntityException{
		int anio = new Integer(getCiclo(fechaContable));
		int mes = getMes(fechaContable);
		
		String numeroPoliza = null;
		String tipoPolizaId = EventoContable.getString("tipoPolizaId");
		
		if(UtilValidate.isEmpty(EventoContable)){
			throw new GenericEntityException("El evento ["+eventoContableId+"] no se encuentra configurado en el sistema");
		}
		GenericValue secuenciaPoliza = delegator.findByPrimaryKey(
				"SecuenciaPoliza", UtilMisc.toMap("tipoPolizaId", tipoPolizaId,"anio", new Long(anio),"mes", new Long(mes)));
		
		if(secuenciaPoliza==null){
			//Si no se encuentra registro entonces se crea.
			GenericValue secuencia = GenericValue.create(delegator
					.getModelEntity("SecuenciaPoliza"));
			secuencia.set("tipoPolizaId", tipoPolizaId);
			secuencia.set("anio", new Long(anio));
			secuencia.set("mes", new Long(mes));
			secuencia.set("secuencia", new Long(1));
			secuencia.create();
			numeroPoliza = formatoNumeroPoliza(anio,--mes,secuencia.getLong("secuencia").intValue());
		}else{
			//Si existe entonces sumamos uno.
			secuenciaPoliza.set("secuencia", secuenciaPoliza.getLong("secuencia")+new Long(1));
			secuenciaPoliza.store();
			numeroPoliza = formatoNumeroPoliza(anio,--mes,secuenciaPoliza.getLong("secuencia").intValue());
		}
		
		trans.set("poliza", numeroPoliza);
		trans.set("tipoPolizaId", tipoPolizaId);
		
		return numeroPoliza;
	}
	
	public static String generaNumeroPolizaSinEvento(Delegator delegator,String tipoPolizaId, Timestamp fechaContable) throws GenericEntityException{
		int anio = new Integer(getCiclo(fechaContable));
		int mes = getMes(fechaContable);
		
		GenericValue secuenciaPoliza = delegator.findByPrimaryKey("SecuenciaPoliza", UtilMisc.toMap("tipoPolizaId", tipoPolizaId, "anio", new Long(anio),
						"mes", new Long(mes)));
		
		if(secuenciaPoliza==null){
			//Si no se encuentra registro entonces se crea.
			GenericValue secuencia = GenericValue.create(delegator.getModelEntity("SecuenciaPoliza"));
			secuencia.set("tipoPolizaId", tipoPolizaId);
			secuencia.set("anio", new Long(anio));
			secuencia.set("mes",  new Long(mes));
			secuencia.set("secuencia", new Long(1));
			secuencia.create();
			return formatoNumeroPoliza(anio,--mes,secuencia.getLong("secuencia").intValue());
		}else{
			//Si existe entonces sumamos uno.
			secuenciaPoliza.set("secuencia", secuenciaPoliza.getLong("secuencia")+new Long(1));
			secuenciaPoliza.store();
			return formatoNumeroPoliza(anio,--mes,secuenciaPoliza.getLong("secuencia").intValue());
		}
	}
	
	/**
	 * Genera el formato al numero de poliza
	 * @param anio
	 * @param mes
	 * @param secuencia
	 * @return
	 */
	public static String formatoNumeroPoliza(int anio, int mes, int secuencia){
		return new Integer(anio).toString() + obtenMesString(mes) +"-"+getSecuenciaString(secuencia); 
		
	}
	
	/**
	 * Genera el folio a la secuencia.
	 * @param secuencia
	 * @return
	 */
	public static String getSecuenciaString(int secuencia){
		return secuencia < 10 ? "0000" + new Integer(secuencia).toString()
				: secuencia < 100 ? "000" + new Integer(secuencia).toString()
						: secuencia < 1000 ? "00" + new Integer(secuencia).toString()
							: secuencia < 10000 ? "0" + new Integer(secuencia).toString()
									: new Integer(secuencia).toString();
	}
	/**
	 * Regresa el tipo de producto
	 * @param delegator
	 * @param auxiliarId
	 * @return
	 * @throws GenericEntityException
	 */
	public static String obtenRolProducto(Delegator delegator,
			String auxiliarId) throws GenericEntityException {

		GenericValue generic = delegator.findByPrimaryKey("Product",
				UtilMisc.toMap("productId", auxiliarId));

		Debug.logInfo("Se obtiene producto: " + generic.getString("productTypeId"),module);
		return generic.getString("productTypeId");
	}
	
	/**
	 * Regresa el id del banco a traves de una cuenta bancaria
	 * @param delegator
	 * @param auxiliarId
	 * @return
	 * @throws GenericEntityException
	 */
	public static String obtenBancoCuenta(Delegator delegator,
			String auxiliarId) throws GenericEntityException {

		GenericValue generic = delegator.findByPrimaryKey("CuentaBancaria",
				UtilMisc.toMap("cuentaBancariaId", auxiliarId));

		Debug.logInfo("Se obtiene banco: " + generic.getString("bancoId"),module);
		return generic.getString("bancoId");
	}

	/**
	 * Obtiene los periodos a impactar.
	 * @param delegator
	 * @param organizationId
	 * @param fecha
	 * @return
	 * @throws GenericEntityException
	 */
	public static List<GenericValue> getPeriodos(Delegator delegator,
			String organizationId, Timestamp fecha,boolean cierre) throws GenericEntityException {
		
		Date fechaSql = UtilDate.timestampToSqlDate(fecha);
		EntityCondition conditions = EntityCondition.makeCondition(
				EntityOperator.AND, 
				EntityCondition.makeCondition("organizationPartyId", organizationId), 
				EntityCondition.makeCondition("isClosed", EntityOperator.EQUALS, "N"),
				EntityCondition.makeCondition("fromDate",EntityOperator.LESS_THAN_EQUAL_TO, fechaSql),
				EntityCondition.makeCondition("thruDate",EntityOperator.GREATER_THAN, fechaSql));
		List<GenericValue> periodos = delegator.findByConditionCache("CustomTimePeriod", conditions, null, null);
		if(periodos.isEmpty()){
			throw new GenericEntityException("No hay periodos abiertos");
		}else if(periodos.size()==1){
			if(periodos.get(0).getString("periodTypeId").equals("FISCAL_YEAR") && !cierre){
				throw new GenericEntityException("No hay periodos abiertos");
			}
		}
		Debug.logInfo("Periodos regresados.- " + periodos.size(),module);
		return periodos;
	}
	
	/**
	 * Valida que el evento no tenga linea(s) presupuestal(es)
	 * @param delegator
	 * @param eventoContableId
	 * @throws GenericEntityException
	 */
	public static void validaExistenciaLineaPresupuestal(Delegator delegator,String eventoContableId) throws GenericEntityException{
		List <GenericValue> lineas = delegator.findByAnd("LineaPresupuestal", "acctgTransTypeId", eventoContableId);
		if(lineas!=null&&!lineas.isEmpty()){
			throw new GenericEntityException("El Evento debe tener un tipo de Clave");
		}
	}
	
	/**
	 * Genera un acctgTransEntry de tipo presupuestal generico.
	 * @param delegator
	 * @param organizationId
	 * @param clavePres
	 * @param estructura
	 * @param acctgTransId
	 * @param currency
	 * @param monto
	 * @param partyId
	 * @param fechaContable 
	 * @return
	 * @throws GenericEntityException
	 */
	public static GenericValue generaAcctgTransPresupuestalGenerica(Delegator delegator,
				String organizationId, String clavePres, GenericValue estructura, String acctgTransId, 
				String currency, BigDecimal monto, String partyId, Timestamp fechaContable)
				throws GenericEntityException {
		GenericValue acctgEntry = GenericValue.create(delegator
				.getModelEntity("AcctgTransEntry"));
		acctgEntry.set("acctgTransId", acctgTransId);
		acctgEntry.set("organizationPartyId", organizationId);
		acctgEntry.set("currencyUomId", currency);
		acctgEntry.set("partyId", partyId);
		acctgEntry.set("amount", monto);
		acctgEntry.set("acctgTransEntryTypeId", "_NA_");
		acctgEntry.set("fechaPresupuestal", fechaContable);
		return asignaTags(delegator, acctgEntry, clavePres,estructura);
	}
	
	/**
	 * Genera las entries para la lineaPresupuestal ingresada
	 * @param delegator
	 * @param lineaPresupuestal
	 * @param listGlAccountOrganization 
	 * @param listGlAccountHistory 
	 * @param monto
	 * @param acctgTransId
	 * @param organizacionPartyId
	 * @param partyId
	 * @param currency
	 * @return
	 * @throws GenericEntityException 
	 */
	public static BigDecimal generaAcctgTransEntriesPresupuestal(
			Delegator delegator, GenericValue lineaPresupuestal, GenericValue acctgTransEntry, BigDecimal montoTransaccion,
			List<GenericValue> listGlAccountHistory, List<GenericValue> listGlAccountOrganization, List<GenericValue> listAcctgTransEntry) throws GenericEntityException {
		Debug.logInfo("Empieza AcctgTransEntry Presupuestal",module);
		
		//Cargo->Debit
		GenericValue acctgEntryCargo =  (GenericValue) acctgTransEntry.clone();
		acctgEntryCargo.set("acctgTransEntrySeqId", lineaPresupuestal.getLong("secuencia").toString()+"PD"+contador);
		acctgEntryCargo.set("description", lineaPresupuestal.getString("descripcion"));
		acctgEntryCargo.set("glAccountId", lineaPresupuestal.getString("cuentaCargo"));
		acctgEntryCargo.set("debitCreditFlag", "D");
		acctgEntryCargo.set("reconcileStatusId", "AES_NOT_RECONCILED");
		montoTransaccion = impactaAcctgTransEntry(delegator, acctgEntryCargo, montoTransaccion,listGlAccountHistory,listGlAccountOrganization);
		
		listAcctgTransEntry.add(acctgEntryCargo);
		
		//Abono->Credit
		GenericValue acctgEntryAbono = (GenericValue) acctgEntryCargo.clone();
		acctgEntryAbono.set("acctgTransEntrySeqId", lineaPresupuestal.getLong("secuencia").toString()+"PC"+contador);
		acctgEntryAbono.set("glAccountId", lineaPresupuestal.getString("cuentaAbono"));
		acctgEntryAbono.set("debitCreditFlag", "C");
		montoTransaccion = impactaAcctgTransEntry(delegator, acctgEntryAbono, montoTransaccion,listGlAccountHistory,listGlAccountOrganization);
		
		listAcctgTransEntry.add(acctgEntryAbono);
		
		return montoTransaccion;
	}
	
	/**
	 * Genera las entries dada la Matriz de Conversion
	 * @param delegator
	 * @param lineaPresupuestalBD
	 * @param acctgTransEntry
	 * @param tipoMatrizId
	 * @param clavePresupuestal
	 * @param estructura
	 * @return
	 * @throws GenericEntityException
	 */
	public static BigDecimal generaAcctgTransEntriesMatriz(
			Delegator delegator, GenericValue lineaPresupuestalBD,
			GenericValue acctgTransEntry, String tipoMatrizId, 
			String catalogoCargo, String catalogoAbono,
			Timestamp fechaContable, BigDecimal montoTransaccion, 
			String descripcion,String usuario,GenericValue MatrizCuentas,
			String campoClasificacion,
			String organizationPartyId,
			List<GenericValue> listGlAccountHistory,
			List<GenericValue> listGlAccountOrganization,
			List<GenericValue> listTraspasoBanco,
			List<GenericValue> listAcctgTransEntry,
			Map<String,Map<String,GenericValue>> mapaAuxiliares)
			throws GenericEntityException {
		Debug.logInfo("Empieza AcctgTransEntry Matriz",module);
		
		Map<String,GenericValue> mapAuxiliaresTipo = mapaAuxiliares.get("mapAuxiliaresTipoPresupuestal");
		Map<String,GenericValue> mapCuentaBancaria = mapaAuxiliares.get("CuentaBancaria");
		Map<String,GenericValue> mapCuentaBancariaSaldo = mapaAuxiliares.get("CuentaBancariaSaldo");
		
		String cuentaCargo = MatrizCuentas.getString("cargo");
		String cuentaAbono = MatrizCuentas.getString("abono");
		
		Timestamp fechaInicioMes = UtilDateTime.getMonthStart(fechaContable);
		
		//Cargo->Debit
		GenericValue acctgEntryCargo =  clonaEntrySinAuxiliares(acctgTransEntry);
		acctgEntryCargo.set("acctgTransEntrySeqId", lineaPresupuestalBD.getLong("secuencia").toString()+"MD"+contador);
		acctgEntryCargo.set("description", lineaPresupuestalBD.getString("descripcion"));
		acctgEntryCargo.set("glAccountId", cuentaCargo);
		acctgEntryCargo.set("debitCreditFlag", "D");
		acctgEntryCargo.set("reconcileStatusId", "AES_NOT_RECONCILED");
		
		// Seteamos los auxiliares en caso de que tenga.
		if (lineaPresupuestalBD.getString("catalogoCargo") != null) {
			if(catalogoCargo==null||catalogoCargo.isEmpty()){
				throw new GenericEntityException("Es necesario ingresar el auxiliar Cargo en la linea Presupuestal.- " + MatrizCuentas.getString("clavePresupuestal"));
			}
			if(UtilValidate.isEmpty(cuentaCargo)){
				throw new GenericEntityException("Se necesita configurar la cuenta de cargo para la matriz "+tipoMatrizId+" y el valor "+MatrizCuentas.getString(campoClasificacion));
			}
			
			String tipo = getTipoCatalogo(delegator,lineaPresupuestalBD.getString("catalogoCargo"),lineaPresupuestalBD.getString("secuencia"),"Cargo","N",mapAuxiliaresTipo);
			
			if(tipo==null){
				throw new GenericEntityException("El auxiliar de Cargo: " + catalogoCargo + "  no es el indicado, debe ser del tipo: "
						+ lineaPresupuestalBD.getString("catalogoCargo"));
			}

			String campo = tipo.equalsIgnoreCase("A") ? "theirPartyId" : tipo
					.equalsIgnoreCase("P") ? "productId" : "cuentaBancariaId";

			if (tipo.equalsIgnoreCase("B")) {
				validaSuficienciaCuentaBancaria(cuentaCargo, "DEBIT", 
						acctgEntryCargo.getBigDecimal("amount"), 
						mapCuentaBancariaSaldo.get(catalogoCargo));
				
				registraTraspasoBanco(delegator, 
						acctgEntryCargo.getString("acctgTransId"), 
						catalogoCargo, "D", acctgEntryCargo.getBigDecimal("amount"), 
						descripcion, usuario, 
						listTraspasoBanco, 
						mapCuentaBancaria.get(catalogoCargo));
			}

			acctgEntryCargo.set(campo, catalogoCargo);
			
			GenericValue SaldoCatalogo = getSaldoCatalogoPorTipo(delegator, catalogoCargo, tipo, organizationPartyId, fechaInicioMes, mapaAuxiliares);

			actualizaCatalogo(catalogoCargo, lineaPresupuestalBD.getString("catalogoCargo") , cuentaCargo, acctgEntryCargo.getBigDecimal("amount"), "DEBIT", SaldoCatalogo);
			
		}
		
		listAcctgTransEntry.add(acctgEntryCargo);
		
		montoTransaccion = impactaAcctgTransEntry(delegator, acctgEntryCargo, montoTransaccion,listGlAccountHistory, listGlAccountOrganization);
		
		//Abono->Credit
		GenericValue acctgEntryAbono = clonaEntrySinAuxiliares(acctgEntryCargo);
		acctgEntryAbono.set("acctgTransEntrySeqId", lineaPresupuestalBD.getLong("secuencia").toString()+"MC"+contador);
		acctgEntryAbono.set("glAccountId", cuentaAbono);
		acctgEntryAbono.set("debitCreditFlag", "C");
		
		// Seteamos los auxiliares en caso de que tenga.
		if (lineaPresupuestalBD.getString("catalogoAbono") != null) {
			if(UtilValidate.isEmpty(catalogoAbono)){
				throw new GenericEntityException("Es necesario ingresar el auxiliar abono en la linea Presupuestal.- " + MatrizCuentas.getString("clavePresupuestal"));
			}
			if(UtilValidate.isEmpty(cuentaAbono)){
				throw new GenericEntityException("Se necesita configurar la cuenta de abono para la matriz "+tipoMatrizId+" y el valor "+MatrizCuentas.getString(campoClasificacion));
			}
		String tipo = getTipoCatalogo(delegator,lineaPresupuestalBD.getString("catalogoAbono"),lineaPresupuestalBD.getString("secuencia"),"Abono","N",mapAuxiliaresTipo);
		
		if(tipo==null){
			throw new GenericEntityException("El auxiliar de Abono: " + catalogoAbono + "  no es el indicado, debe ser del tipo: "
						+ lineaPresupuestalBD.getString("catalogoAbono"));
		}
		
		String campo = tipo.equalsIgnoreCase("A") ? "theirPartyId" : tipo
				.equalsIgnoreCase("P") ? "productId" : "cuentaBancariaId";
		
		if (tipo.equalsIgnoreCase("B")) {
			validaSuficienciaCuentaBancaria(cuentaAbono, "CREDIT", 
					acctgEntryAbono.getBigDecimal("amount"), 
					mapCuentaBancariaSaldo.get(catalogoAbono));
			
			registraTraspasoBanco(delegator,
					acctgEntryAbono.getString("acctgTransId"),
					catalogoAbono, "C",
					acctgEntryAbono.getBigDecimal("amount"),
					descripcion, usuario,
					listTraspasoBanco,
					mapCuentaBancaria.get(catalogoAbono));
		}
		
		acctgEntryAbono.set(campo, catalogoAbono);
		
		GenericValue SaldoCatalogo = getSaldoCatalogoPorTipo(delegator, catalogoAbono, tipo, organizationPartyId, fechaInicioMes, mapaAuxiliares);
			
		actualizaCatalogo(catalogoAbono, lineaPresupuestalBD.getString("catalogoAbono"), cuentaAbono, acctgEntryAbono.getBigDecimal("amount"), "CREDIT", SaldoCatalogo);
		}
		
		montoTransaccion = impactaAcctgTransEntry(delegator, acctgEntryAbono, montoTransaccion
				,listGlAccountHistory, listGlAccountOrganization);
		
		listAcctgTransEntry.add(acctgEntryAbono);
		
		return montoTransaccion;
	}
	
	/**
	 * Indica si se usa la matriz presupuestal.
	 * @param referenciaMatriz
	 * @return
	 */
	public static boolean usaMatriz(String referenciaMatriz){
		if(referenciaMatriz==null)
			return false;
		return !referenciaMatriz.isEmpty();
	}
	
	/**
	 * Genera un acctgTransEntry de tipo contable generico.
	 * @param delegator
	 * @param organizationId
	 * @param clavePres
	 * @param estructura
	 * @param acctgTransId
	 * @param currency
	 * @param monto
	 * @param partyId
	 * @return
	 * @throws GenericEntityException
	 */
	public static GenericValue generaAcctgTransContableGenerica(Delegator delegator,
			String organizationId, String acctgTransId, String currency, String partyId)
			throws GenericEntityException {
	GenericValue acctgEntry = GenericValue.create(delegator
			.getModelEntity("AcctgTransEntry"));
	acctgEntry.set("acctgTransId", acctgTransId);
	acctgEntry.set("organizationPartyId", organizationId);
	acctgEntry.set("currencyUomId", currency);
	acctgEntry.set("partyId", partyId);
	acctgEntry.set("acctgTransEntryTypeId", "_NA_");
	return acctgEntry;
}	
	
	/**
	 * Genera las entries para la lineaContable ingresada
	 * @param delegator
	 * @param lineaContable
	 * @param acctgTransEntry
	 * @param mapLineaContable 
	 * @param listGlAccountOrganization 
	 * @param listGlAccountHistory 
	 * @param mapaAuxiliares 
	 * @return
	 * @throws GenericEntityException
	 */
	public static BigDecimal generaAcctgTransEntriesContable(
			Delegator delegator, GenericValue lineaContable,
			GenericValue acctgTransEntry, Timestamp fechaContable, 
			BigDecimal montoTransaccion, String descripcion, 
			String usuario,
			String organizationPartyId,
			List<GenericValue> listAcctgTransEntry,
			Map<Long, GenericValue> mapLineaContable, 
			List<GenericValue> listGlAccountHistory, 
			List<GenericValue> listGlAccountOrganization,
			List<GenericValue> listTraspasoBanco,
			Map<String, Map<String, GenericValue>> mapaAuxiliares
			) throws GenericEntityException {
		Debug.logInfo("Empieza AcctgTransEntry Contable",module);
		
		//Cargo->Debit
		GenericValue acctgEntryCargo =  (GenericValue) acctgTransEntry.clone();
		acctgEntryCargo.set("acctgTransEntrySeqId", lineaContable.getLong("secuencia").toString()+"CD"+contador);
		acctgEntryCargo.set("description", lineaContable.getString("description"));
		acctgEntryCargo.set("glAccountId", lineaContable.getString("cuentaCargo"));
		acctgEntryCargo.set("amount", lineaContable.getBigDecimal("monto"));
		acctgEntryCargo.set("debitCreditFlag", "D");
		acctgEntryCargo.set("reconcileStatusId", "AES_NOT_RECONCILED");
		
		GenericValue lineaContableBD = mapLineaContable.get(lineaContable.getLong("secuencia"));
		
		Map<String, GenericValue> mapaAuxiliaresTipo = mapaAuxiliares.get("mapAuxiliaresTipoContable");
		Map<String, GenericValue> CuentaBancariaSaldo = mapaAuxiliares.get("CuentaBancariaSaldo");
		Map<String, GenericValue> CuentaBancaria = mapaAuxiliares.get("CuentaBancaria");
		
		Timestamp fechaInicioMes = UtilDateTime.getMonthStart(fechaContable);
		
		
		//Verificamos si usa auxiliar.
		if(UtilValidate.isNotEmpty(lineaContableBD.getString("catalogoCargo"))){
			//Verificamos que el 'usuario' ingrese ese auxiliar
			if(UtilValidate.isNotEmpty(lineaContable.getString("catalogoCargo"))){
				//Se obtiene el tipo de la linea contable
				String tipo = getTipoCatalogo(delegator, lineaContableBD.getString("catalogoCargo"), lineaContableBD.getString("secuencia"), "Cargo", null, mapaAuxiliaresTipo);
				
				if(tipo==null){
					throw new GenericEntityException("El auxiliar de Cargo: " + lineaContable.getString("catalogoCargo") + " no es el indicado, debe ser del tipo: "
						+ lineaContableBD.getString("catalogoCargo"));
				}
				
				String campo = tipo.equalsIgnoreCase("A") ? "theirPartyId" : tipo
						.equalsIgnoreCase("P") ? "productId" : "cuentaBancariaId";
				
				if (tipo.equalsIgnoreCase("B")) {
					validaSuficienciaCuentaBancaria(lineaContable.getString("cuentaCargo"),
							"DEBIT", lineaContable.getBigDecimal("monto"), 
							CuentaBancariaSaldo.get(lineaContable.getString("catalogoCargo")));
					
					registraTraspasoBanco(delegator,
							acctgEntryCargo.getString("acctgTransId"),
							lineaContable.getString("catalogoCargo"), "D",
							lineaContable.getBigDecimal("monto"),
							descripcion, usuario,
							listTraspasoBanco,
							CuentaBancaria.get(lineaContable.getString("catalogoCargo")));
				}
				
				acctgEntryCargo.set(campo, lineaContable.getString("catalogoCargo"));
				
				GenericValue SaldoCatalogo = getSaldoCatalogoPorTipo(delegator, lineaContable.getString("catalogoCargo"), tipo, organizationPartyId, fechaInicioMes, mapaAuxiliares);
					
				actualizaCatalogo(lineaContable.getString("catalogoCargo"), 
						lineaContableBD.getString("catalogoCargo"),
						lineaContable.getString("cuentaCargo"), 
						lineaContable.getBigDecimal("monto"), 
						"DEBIT", SaldoCatalogo);
			}else{
				throw new GenericEntityException("Es necesario ingresar el auxiliar Cargo.");
			}
		}
		
		montoTransaccion = impactaAcctgTransEntry(delegator, acctgEntryCargo, montoTransaccion,listGlAccountHistory,listGlAccountOrganization);
		listAcctgTransEntry.add(acctgEntryCargo);
		
		//Abono->Credit
		GenericValue acctgEntryAbono = clonaEntrySinAuxiliares(acctgEntryCargo);
		acctgEntryAbono.set("acctgTransEntrySeqId", lineaContable.getLong("secuencia").toString()+"CC"+contador);
		acctgEntryAbono.set("glAccountId", lineaContable.getString("cuentaAbono"));
		acctgEntryAbono.set("debitCreditFlag", "C");
		
		//Verificamos si usa auxiliar.
		if(UtilValidate.isNotEmpty(lineaContableBD.getString("catalogoAbono"))){
			//Verificamos que el 'usuario' ingrese ese auxiliar
			if(UtilValidate.isNotEmpty(lineaContable.getString("catalogoAbono"))){
				//Se obtiene el tipo de la linea contable
				String tipo = getTipoCatalogo(delegator,lineaContableBD.getString("catalogoAbono"), lineaContableBD.getString("secuencia"), "Abono", null, mapaAuxiliaresTipo);
				
				if(tipo==null){
					throw new GenericEntityException("El auxiliar de Abono: " + lineaContable.getString("catalogoAbono") + "  no es el indicado, debe ser del tipo: "
						+ lineaContableBD.getString("catalogoAbono"));
				}
				
				String campo = tipo.equalsIgnoreCase("A") ? "theirPartyId" : tipo
						.equalsIgnoreCase("P") ? "productId" : "cuentaBancariaId";
				
				if (tipo.equalsIgnoreCase("B")) {
					validaSuficienciaCuentaBancaria(lineaContable.getString("cuentaAbono"),
							"CREDIT", lineaContable.getBigDecimal("monto"), 
							CuentaBancariaSaldo.get(lineaContable.getString("catalogoAbono")));
					
					registraTraspasoBanco(delegator,
							acctgEntryCargo.getString("acctgTransId"),
							lineaContable.getString("catalogoAbono"), "C",
							lineaContable.getBigDecimal("monto"),
							descripcion, usuario,
							listTraspasoBanco,
							CuentaBancaria.get(lineaContable.getString("catalogoAbono")));
				}
				
				acctgEntryAbono.set(campo, lineaContable.getString("catalogoAbono"));
				
				GenericValue SaldoCatalogo = getSaldoCatalogoPorTipo(delegator, lineaContable.getString("catalogoAbono"), tipo, organizationPartyId, fechaInicioMes, mapaAuxiliares);
				
				actualizaCatalogo(lineaContable.getString("catalogoAbono"), 
						lineaContableBD.getString("catalogoAbono"),
						lineaContable.getString("cuentaAbono"), 
						lineaContable.getBigDecimal("monto"), 
						"CREDIT", SaldoCatalogo);
				
			}else{
				throw new GenericEntityException("Es necesario ingresar el auxiliar Cargo.");
			}
		}
		
		montoTransaccion = impactaAcctgTransEntry(delegator, acctgEntryAbono, montoTransaccion,listGlAccountHistory, listGlAccountOrganization);
		listAcctgTransEntry.add(acctgEntryAbono);
		
		return montoTransaccion;
	}
	
	/**
	 * Obtiene el SaldoCatalogo de un auxiliar dependiendo el tipo que sea (A,B,P), 
	 * si no existe lo crea y guarda en el mapa de auxiliares
	 * @param delegator
	 * @param catalogoId
	 * @param tipo
	 * @param organizationPartyId
	 * @param periodo
	 * @param mapaAuxiliares
	 * @return
	 * @throws GenericEntityException
	 */
	private static GenericValue getSaldoCatalogoPorTipo(Delegator delegator,String catalogoId, 
			String tipo,String organizationPartyId, Timestamp periodo,
			Map<String, Map<String, GenericValue>> mapaAuxiliares) throws GenericEntityException {
		
		GenericValue SaldoCatalogo = null;
		GenericValue ObjetoCatalogo = null;
		String campoTipo = new String();
		String tipoAuxiliarId = new String();
		Map<String,GenericValue> mapaSaldo = FastMap.newInstance();
		Map<String,GenericValue> mapaTipo = FastMap.newInstance();
		
		if(tipo.equals("A")){
			mapaSaldo = mapaAuxiliares.get("SaldoCatalogoParty");
			mapaTipo = mapaAuxiliares.get("Party");
			campoTipo = "partyTypeId";
			SaldoCatalogo = mapaSaldo.get(catalogoId);
		} else if (tipo.equals("B")){
			mapaSaldo = mapaAuxiliares.get("SaldoCatalogoCuentaBancaria");
			SaldoCatalogo = mapaSaldo.get(catalogoId);
		} else if (tipo.equals("P")){
			mapaSaldo = mapaAuxiliares.get("SaldoCatalogoProducto");
			mapaTipo = mapaAuxiliares.get("Product");
			campoTipo = "productTypeId";
			SaldoCatalogo = mapaSaldo.get(catalogoId);
		}
		
		ObjetoCatalogo = mapaTipo.get(catalogoId);
		
		if(UtilValidate.isEmpty(ObjetoCatalogo) && !tipo.equals("B")){
			throw new GenericEntityException("El auxiliar "+catalogoId+" no tiene configurado el tipo o es incorrecto");
		} else {
			if(tipo.equals("B")){
				tipoAuxiliarId = "BANCO";
			} else {
				tipoAuxiliarId = ObjetoCatalogo.getString(campoTipo);
			}
		}
		
		if (UtilValidate.isEmpty(SaldoCatalogo)) {
			// Si esta vacio se crea el registro.
			SaldoCatalogo = GenericValue.create(delegator.getModelEntity("SaldoCatalogo"));
			SaldoCatalogo.set("secuenciaId",delegator.getNextSeqId("SaldoCatalogo"));
			SaldoCatalogo.set("catalogoId", catalogoId);
			SaldoCatalogo.set("tipoId", tipoAuxiliarId);
			SaldoCatalogo.set("partyId", organizationPartyId);
			SaldoCatalogo.set("tipo", tipo);
			SaldoCatalogo.set("periodo", periodo);
			SaldoCatalogo.set("monto", BigDecimal.ZERO);
			mapaSaldo.put(catalogoId, SaldoCatalogo);
		}
		
		return SaldoCatalogo;
	}

	/**
	 * Obtiene todos los auxilares de un evento contable por medio de mapas , puede consultar los auxiliares contables o los presupuestales
	 * @param delegator
	 * @param eventoContableId
	 * @param lineasMotor
	 * @param UserLogin
	 * @param organizationPartyId
	 * @param fechaContable
	 * @param EntidadBusqueda
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map<String,Map<String,GenericValue>> getAuxiliaresEvento(Delegator delegator, String eventoContableId,
			List<LineaMotorContable> lineasMotor, GenericValue UserLogin,String organizationPartyId, Timestamp fechaContable) throws GenericEntityException{
		
		Map<String, GenericValue> mapaLineaCorrespondiente = FastMap.newInstance(); 
		
		Timestamp fechaInicioMes = UtilDateTime.getMonthStart(fechaContable);
		
		LinkedHashSet<String> listaAuxParty = new LinkedHashSet<String>();
		LinkedHashSet<String> listaAuxBanco = new LinkedHashSet<String>();
		LinkedHashSet<String> listaAuxProduct = new LinkedHashSet<String>();
		
		Long secuencia = new Long(-1);
		String auxiliarIdCargo = new String();
		String auxiliarIdAbono = new String();
		GenericValue LineaContableUsuario = null;
		GenericValue LineaContableCargo = null;
		GenericValue LineaContableAbono = null;
		String tipoCatalogoCargo = new String();
		String tipoCatalogoAbono = new String();
		String tipoAuxiliar = new String();
		
		Map<String,Map<String,GenericValue>> mapaRegresa = FastMap.newInstance();
		
		List<String> listEntidades = UtilMisc.toList("LineaContableAuxiliar","LineaPresupuestalAuxiliar");
		
		
			for (String EntidadBusqueda : listEntidades) {
				
					/**
					 * Se guardan en un mapa los tipos de auxiliar de cada linea 
					 * por cada tipo de linea : contable y presupuestal
					 */
					//<Secuencia+TipoMovimiento(Cargo,Abono),LineaContableAuxiliar>
					Map<String,GenericValue> mapAuxiliaresTipo = FastMap.newInstance();
					
					
					//Se obtienen el mapa de las lineas contables/presupuestales del evento contable
					EntityCondition condicionAux = EntityCondition.makeCondition("acctgTransTypeId",eventoContableId);
					List<GenericValue> listLineaAux = delegator.findByConditionCache(EntidadBusqueda, 
							condicionAux, null, UtilMisc.toList("secuencia"));
					
					//Se separa la lineas en 2 mapas : Cargo y Abono
					for (GenericValue LineaAuxiliar : listLineaAux) {
						if(UtilValidate.isNotEmpty(LineaAuxiliar.getString("catalogoCargo"))){
							mapAuxiliaresTipo.put(LineaAuxiliar.getString("secuencia")+"Cargo", LineaAuxiliar);
						}
						if(UtilValidate.isNotEmpty(LineaAuxiliar.getString("catalogoAbono"))){
							mapAuxiliaresTipo.put(LineaAuxiliar.getString("secuencia")+"Abono", LineaAuxiliar);
						}
					}
					
					//Se clasifican los tipos de auxiliar , se guardan en listas diferentes 
					for (LineaMotorContable lineaMotorContable : lineasMotor) {
						
						if(EntidadBusqueda.equals("LineaContableAuxiliar")){
							mapaLineaCorrespondiente = lineaMotorContable.getLineasContables();
						} else {
							mapaLineaCorrespondiente = lineaMotorContable.getLineasPresupuestales();
						}
						
						if(UtilValidate.isNotEmpty(mapaLineaCorrespondiente)){
							//En el siguiente for se valida que el ususario ingreso algun auxiliar en el lugar indicado (cargo o abono)
							for(Entry<String, GenericValue> mapLineaContable : mapaLineaCorrespondiente.entrySet()){
								
								LineaContableUsuario = mapLineaContable.getValue();//Esta es la linea ingresada por el usuario desde pantalla
								secuencia = LineaContableUsuario.getLong("secuencia");
								
								auxiliarIdCargo = LineaContableUsuario.getString("catalogoCargo");//Identificador del auxiliar de cargo ingresado por el usuario
								LineaContableCargo = mapAuxiliaresTipo.get(secuencia+"Cargo"); //Linea que contiene del tipo de auxiliar de cargo configurado en el evento contable
								
								//Aqui se valida que existe en la base de datos
								if(UtilValidate.isNotEmpty(LineaContableCargo)){
									tipoCatalogoCargo = LineaContableCargo.getString("tipoEntidadCargo");
									tipoAuxiliar = LineaContableCargo.getString("tipoCargo");//Este es el identificador del tipo de auxiliar que debe ingresar el usuario
									
									if(UtilValidate.isNotEmpty(tipoCatalogoCargo) && UtilValidate.isEmpty(auxiliarIdCargo)){
										throw new GenericEntityException("Debe ingresar el auxiliar "+LineaContableCargo.getString("descripcion")+" del tipo ["+tipoCatalogoCargo+"]");
									}
									
									guardaListaPorTipo(listaAuxParty, listaAuxBanco, listaAuxProduct, tipoAuxiliar, auxiliarIdCargo);
								}
								
								auxiliarIdAbono = LineaContableUsuario.getString("catalogoAbono");//Identificador del auxiliar de abono ingresado por el usuario
								LineaContableAbono = mapAuxiliaresTipo.get(secuencia+"Abono");//Linea que contiene del tipo de auxiliar de abono configurado en el evento contable
								if(UtilValidate.isNotEmpty(LineaContableAbono)){
									tipoCatalogoAbono = LineaContableAbono.getString("tipoEntidadAbono");
									tipoAuxiliar = LineaContableAbono.getString("tipoAbono");//Este es el identificador del tipo de auxiliar que debe ingresar el usuario
											
									if(UtilValidate.isNotEmpty(tipoCatalogoAbono) && UtilValidate.isEmpty(auxiliarIdAbono)){
										throw new GenericEntityException("Debe ingresar el auxiliar "+LineaContableAbono.getString("descripcion")+" del tipo ["+tipoCatalogoAbono+"]");
									}
									
									guardaListaPorTipo(listaAuxParty, listaAuxBanco, listaAuxProduct, tipoAuxiliar, auxiliarIdAbono);
								}
								
								secuencia = new Long(-1);
								auxiliarIdCargo = new String();
								auxiliarIdAbono = new String();
								LineaContableUsuario = null;
								LineaContableCargo = null;
								LineaContableAbono = null;
								tipoCatalogoCargo = new String();
								tipoCatalogoAbono = new String();
								tipoAuxiliar = new String();
							}
			
						}
					}
					
					if(EntidadBusqueda.equals("LineaContableAuxiliar")){
						mapaRegresa.put("mapAuxiliaresTipoContable", mapAuxiliaresTipo);
					} else {
						mapaRegresa.put("mapAuxiliaresTipoPresupuestal", mapAuxiliaresTipo);
					}
					
					Debug.logInfo("mapAuxiliaresTipo -->"+mapAuxiliaresTipo,module);
					
			}
		
			Debug.logInfo("Lista Auxiliares Party -->"+listaAuxParty,module);
			Debug.logInfo("Lista Auxiliares Cuenta Bancaria -->"+listaAuxBanco,module);
			Debug.logInfo("Lista Auxiliares Product -->"+listaAuxProduct,module);
				
			getMapaAuxiliaresContables(delegator, fechaInicioMes, UserLogin, mapaRegresa, listaAuxParty, listaAuxBanco, listaAuxProduct);
			
			Map<String,GenericValue> mapPartyType = mapaRegresa.get("Party");
			Map<String,GenericValue> mapProductType = mapaRegresa.get("Product");
			
			GenericValue Product = null;
			GenericValue Party = null;
			//Validamos que los tipos de auxiliares sean del mismo tipo que necesita el evento
			for (String EntidadBusqueda : listEntidades) {
				
				Map<String,GenericValue> mapAuxiliaresTipo = FastMap.newInstance();
				
				for (LineaMotorContable lineaMotorContable : lineasMotor) {
					
					if(EntidadBusqueda.equals("LineaContableAuxiliar")){
						mapaLineaCorrespondiente = lineaMotorContable.getLineasContables();
						mapAuxiliaresTipo = mapaRegresa.get("mapAuxiliaresTipoContable");
					} else {
						mapaLineaCorrespondiente = lineaMotorContable.getLineasPresupuestales();
						mapAuxiliaresTipo = mapaRegresa.get("mapAuxiliaresTipoPresupuestal");
					}
					
					if(UtilValidate.isNotEmpty(mapaLineaCorrespondiente)){
						for(Entry<String, GenericValue> mapLineaContable : mapaLineaCorrespondiente.entrySet()){
							LineaContableUsuario = mapLineaContable.getValue();
							
							auxiliarIdAbono = LineaContableUsuario.getString("catalogoAbono");
							auxiliarIdCargo = LineaContableUsuario.getString("catalogoCargo");
							
							secuencia = LineaContableUsuario.getLong("secuencia");
							
							LineaContableCargo = mapAuxiliaresTipo.get(secuencia+"Cargo");
							if(UtilValidate.isNotEmpty(LineaContableCargo)){
								tipoCatalogoCargo = LineaContableCargo.getString("tipoEntidadCargo");
								
								tipoAuxiliar = LineaContableCargo.getString("tipoCargo");
								
								Party = mapPartyType.get(auxiliarIdCargo);
								Product = mapProductType.get(auxiliarIdCargo);
								
								if(tipoAuxiliar.equals("A")){
									if(UtilValidate.isNotEmpty(Party)){
										if(!UtilValidate.areEqual(tipoCatalogoCargo, Party.getString("partyTypeId"))){
											throw new GenericEntityException("El auxiliar ["+auxiliarIdCargo+"] es incorrecto debe ser del tipo ["+tipoCatalogoCargo+"]");
										}
									}
								} else if (tipoAuxiliar.equals("P")){
									if(UtilValidate.isNotEmpty(Product)){
										if(!UtilValidate.areEqual(tipoCatalogoCargo, Product.getString("productTypeId"))){
											throw new GenericEntityException("El auxiliar ["+auxiliarIdCargo+"] es incorrecto debe ser del tipo ["+tipoCatalogoCargo+"]");
										}
									}
								}
							}
			
							
							LineaContableAbono = mapAuxiliaresTipo.get(secuencia+"Abono");
							if(UtilValidate.isNotEmpty(LineaContableAbono)){
								tipoCatalogoAbono = LineaContableAbono.getString("tipoEntidadAbono");
								
								tipoAuxiliar = LineaContableAbono.getString("tipoAbono");
								Party = mapPartyType.get(auxiliarIdAbono);
								Product = mapProductType.get(auxiliarIdAbono);
								
								if(tipoAuxiliar.equals("A")){
									if(UtilValidate.isNotEmpty(Party)){
										if(!UtilValidate.areEqual(tipoCatalogoAbono, Party.getString("partyTypeId"))){
											throw new GenericEntityException("El auxiliar ["+auxiliarIdAbono+"] es incorrecto debe ser del tipo ["+tipoCatalogoAbono+"]");
										}
									}
								} else if (tipoAuxiliar.equals("P")){
									if(UtilValidate.isNotEmpty(Product)){
										if(!UtilValidate.areEqual(tipoCatalogoAbono, Product.getString("productTypeId"))){
											throw new GenericEntityException("El auxiliar ["+auxiliarIdCargo+"] es incorrecto debe ser del tipo ["+tipoCatalogoAbono+"]");
										}
									}
								}
							}
			
							
						}
					}
					
					secuencia = new Long(-1);
					auxiliarIdCargo = new String();
					auxiliarIdAbono = new String();
					LineaContableUsuario = null;
					LineaContableCargo = null;
					LineaContableAbono = null;
					tipoCatalogoCargo = new String();
					tipoCatalogoAbono = new String();
					tipoAuxiliar = new String();
				}
			}

		
		return mapaRegresa;
	}
	
	/**
	 * Obtiene los mapas de los auxiliares y sus saldos
	 * @param delegator
	 * @param fechaInicioMes
	 * @param UserLogin
	 * @param mapaRegresa
	 * @param listaAuxParty
	 * @param listaAuxBanco
	 * @param listaAuxProduct
	 * @throws GenericEntityException
	 */
	private static void getMapaAuxiliaresContables(
			Delegator delegator,
			Timestamp fechaInicioMes,
			GenericValue UserLogin,
			Map<String, Map<String, GenericValue>> mapaRegresa,
			LinkedHashSet<String> listaAuxParty,
			LinkedHashSet<String> listaAuxBanco,
			LinkedHashSet<String> listaAuxProduct) throws GenericEntityException {

				//Se obtienen los Auxiliaries tipo Party y se guardan en un mapa
				Map<String,GenericValue> mapPartyType = FastMap.newInstance();
				
				EntityCondition condicionParty = EntityCondition.makeCondition("partyId",EntityOperator.IN,listaAuxParty);
				List<String> fieldsToSelectParty = UtilMisc.toList("partyId","partyTypeId");
				List<GenericValue> listaParty = delegator.findByCondition("Party", condicionParty, fieldsToSelectParty, null);
				
				for (GenericValue Party : listaParty) {
					mapPartyType.put(Party.getString("partyId"), Party);
				}
				
				mapaRegresa.put("Party", mapPartyType);
				
				//Se obtiene Saldo Catalogo de los Party
				EntityCondition condicionSaldoParty = EntityCondition.makeCondition(EntityOperator.AND,
						EntityCondition.makeCondition("catalogoId",EntityOperator.IN,listaAuxParty),
						EntityCondition.makeCondition("tipo","A"),
						EntityCondition.makeCondition("periodo",EntityOperator.EQUALS,fechaInicioMes));
				List<GenericValue> listaSaldoParty = delegator.findByCondition("SaldoCatalogo", condicionSaldoParty, null, null);
				
				
				Map<String,GenericValue> mapaSaldoCatalogoParty = FastMap.newInstance();
				
				String catalogoId = new String();
				String tipoId = new String();
				GenericValue Party = null;
				for (GenericValue SaldoCatalogo : listaSaldoParty) {
					catalogoId = SaldoCatalogo.getString("catalogoId");
					tipoId = SaldoCatalogo.getString("tipoId");
					Party = mapPartyType.get(catalogoId);
					if(UtilValidate.isNotEmpty(Party)){
						if(mapPartyType.containsKey(catalogoId) && UtilValidate.areEqual(tipoId, Party.getString("partyTypeId"))){
							mapaSaldoCatalogoParty.put(catalogoId, SaldoCatalogo);
						}
					}

				}
				
				mapaRegresa.put("SaldoCatalogoParty", mapaSaldoCatalogoParty);
				
				//Se guardan las asiganaciones de cuentas bancarias 
				Map<String,GenericValue> mapPartyCuentaBancaria = FastMap.newInstance();
				
				EntityCondition condicionPartyCuenta = EntityCondition.makeCondition("partyId",UserLogin.getString("partyId"));
				List<String> fieldsToSelectCuentaParty = UtilMisc.toList("cuentaBancariaId");
				List<GenericValue> listPartyCuenta = delegator.findByCondition("PartyCuentaBancaria", condicionPartyCuenta, fieldsToSelectCuentaParty, null);
				
				for (GenericValue PartyCuenta : listPartyCuenta) {
					mapPartyCuentaBancaria.put(PartyCuenta.getString("cuentaBancariaId"), PartyCuenta);
				}
				
				Debug.logInfo("Lista Asignacion Cuenta Bancaria -->"+listPartyCuenta,module);
				
				//Se obtiene Saldo Catalogo de las Cuentas Bancarias
				EntityCondition condicionSaldoCuentaBancaria = EntityCondition.makeCondition(EntityOperator.AND,
						EntityCondition.makeCondition("catalogoId",EntityOperator.IN,listaAuxBanco),
						EntityCondition.makeCondition("tipo","B"),
						EntityCondition.makeCondition("periodo",EntityOperator.EQUALS,fechaInicioMes));
				List<GenericValue> listaSaldoCuentaBancaria = delegator.findByCondition("SaldoCatalogo", condicionSaldoCuentaBancaria, null, null);
				
				
				Map<String,GenericValue> mapaSaldoCatalogoCuentaBancaria = FastMap.newInstance();
				for (GenericValue SaldoCatalogo : listaSaldoCuentaBancaria) {
					mapaSaldoCatalogoCuentaBancaria.put(SaldoCatalogo.getString("catalogoId"), SaldoCatalogo);
				}
				
				mapaRegresa.put("SaldoCatalogoCuentaBancaria", mapaSaldoCatalogoCuentaBancaria);
				
				/*
				 * Se valida que la cuenta bancaria este activa y este asociada al usuario
				 */
				Map<String,GenericValue> mapCuentaBancaria = FastMap.newInstance();
				
				EntityCondition condicionCuentaBanco = EntityCondition.makeCondition("cuentaBancariaId",EntityOperator.IN,listaAuxBanco);
				List<String> fieldsToSelectCuentaBanco = UtilMisc.toList("cuentaBancariaId","nombreCuenta","habilitada");
				List<GenericValue> listaCuentaBanco = delegator.findByCondition("CuentaBancaria", condicionCuentaBanco, fieldsToSelectCuentaBanco, null);

				String cuentaBancariaId = new String();
				for (GenericValue CuentaBancaria : listaCuentaBanco) {
					cuentaBancariaId = CuentaBancaria.getString("cuentaBancariaId");
					if(UtilValidate.isEmpty(mapPartyCuentaBancaria.get(cuentaBancariaId))){
						throw new GenericEntityException("La cuenta bancaria ["+cuentaBancariaId+" -  "+CuentaBancaria.getString("nombreCuenta")+"] no esta asignada a usted");
					}
					if(CuentaBancaria.getString("habilitada").equals("N")){
						throw new GenericEntityException("La cuenta bancaria ["+cuentaBancariaId+" -  "+CuentaBancaria.getString("nombreCuenta")+"] esta deshabilitada");
					}
					mapCuentaBancaria.put(cuentaBancariaId, CuentaBancaria);
				}
				
				Debug.logInfo("Lista Asignacion Cuenta Bancaria -->"+listaCuentaBanco,module);
				
				mapaRegresa.put("CuentaBancaria", mapCuentaBancaria);
				
				//Se obtiene el saldo de las cuentas bancarias , detecta si la cuenta no tiene saldo por primera vez , posteriormente se valida despues de modificar
				Map<String,GenericValue> mapCuentaBancariaSaldo = FastMap.newInstance();
				
				EntityCondition condicionSaldoCuentaBanco = EntityCondition.makeCondition(EntityOperator.AND,
						EntityCondition.makeCondition("catalogoId",EntityOperator.IN,listaAuxBanco),
						EntityCondition.makeCondition("tipoId","BANCO"),
						EntityCondition.makeCondition("tipo","B")
						);
				List<String> fieldsToSelectSaldoCuentaBanco = UtilMisc.toList("catalogoId","tipoId","tipo","monto");
				List<GenericValue> listaSaldoCuentaBanco = delegator.findByCondition("SaldoCuentaBanco", condicionSaldoCuentaBanco, fieldsToSelectSaldoCuentaBanco, null);
				
				for (GenericValue SaldoCuentaBancaria : listaSaldoCuentaBanco) {
					cuentaBancariaId = SaldoCuentaBancaria.getString("catalogoId");
					mapCuentaBancariaSaldo.put(cuentaBancariaId, SaldoCuentaBancaria);
				}
				
				Debug.logInfo("Lista Asignacion Cuenta Bancaria Saldo-->"+listaSaldoCuentaBanco,module);
				
				mapaRegresa.put("CuentaBancariaSaldo", mapCuentaBancariaSaldo);
				
				Map<String,GenericValue> mapProductType = FastMap.newInstance();
				
				EntityCondition condicionProduct = EntityCondition.makeCondition("productId",EntityOperator.IN,listaAuxProduct);
				List<String> fieldsToSelectProduct = UtilMisc.toList("productId","productTypeId");
				List<GenericValue> listaProduct = delegator.findByCondition("Product", condicionProduct, fieldsToSelectProduct, null);
				
				for (GenericValue Product : listaProduct) {
					mapProductType.put(Product.getString("productId"), Product);
				}
				
				Debug.logInfo("Lista Product-->"+listaProduct,module);
				
				mapaRegresa.put("Product", mapProductType);
				
				//Se obtiene Saldo Catalogo de los Productos
				EntityCondition condicionSaldoProductos = EntityCondition.makeCondition(EntityOperator.AND,
						EntityCondition.makeCondition("catalogoId",EntityOperator.IN,listaAuxProduct),
						EntityCondition.makeCondition("tipo","P"),
						EntityCondition.makeCondition("periodo",EntityOperator.EQUALS,fechaInicioMes));
				List<GenericValue> listaSaldoProducto = delegator.findByCondition("SaldoCatalogo", condicionSaldoProductos, null, null);
				
				Map<String,GenericValue> mapaSaldoCatalogoProducto = FastMap.newInstance();
				for (GenericValue SaldoCatalogo : listaSaldoProducto) {
					mapaSaldoCatalogoProducto.put(SaldoCatalogo.getString("catalogoId"), SaldoCatalogo);
				}
				
				mapaRegresa.put("SaldoCatalogoProducto", mapaSaldoCatalogoProducto);
		
	}


	/**
	 * Se obtienen los auxiliares contables de una poliza registrada
	 * @param delegator
	 * @param listAcctgTransEntry
	 * @param fechaContable
	 * @param UserLogin
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map<String,Map<String,GenericValue>> getAuxiliaresLineaContablePoliza(Delegator delegator, 
			List<GenericValue> listAcctgTransEntry,Timestamp fechaContable, GenericValue UserLogin) throws GenericEntityException{
		
		Timestamp fechaInicioMes = UtilDateTime.getMonthStart(fechaContable);
		
		LinkedHashSet<String> listaAuxParty = new LinkedHashSet<String>();
		LinkedHashSet<String> listaAuxBanco = new LinkedHashSet<String>();
		LinkedHashSet<String> listaAuxProduct = new LinkedHashSet<String>();
		
		String theirPartyId = new String();
		String cuentaBancariaid = new String();
		String productId = new String();
		for (GenericValue AcctgTransEntry : listAcctgTransEntry) {
			theirPartyId = AcctgTransEntry.getString("theirPartyId");
			cuentaBancariaid = AcctgTransEntry.getString("cuentaBancariaId");
			productId = AcctgTransEntry.getString("productId");
			if(UtilValidate.isNotEmpty(theirPartyId)){
				listaAuxParty.add(theirPartyId);
			} else if (UtilValidate.isNotEmpty(cuentaBancariaid)){
				listaAuxBanco.add(cuentaBancariaid);
			} else if (UtilValidate.isNotEmpty(productId)){
				listaAuxProduct.add(productId);
			}
		}
		
		Map<String,Map<String,GenericValue>> mapaRegresa = FastMap.newInstance();

		getMapaAuxiliaresContables(delegator, fechaInicioMes, UserLogin, mapaRegresa, listaAuxParty, listaAuxBanco, listaAuxProduct);
		
		return mapaRegresa;
	}
	
	/**
	 * Guarda en la lista indicada de acuerdo al tipo de auxiliar (Party,Banco,Producto)
	 * @param listaAuxParty
	 * @param listaAuxBanco
	 * @param listaAuxProduct
	 * @param tipo
	 * @param auxiliarId
	 */
	private static void guardaListaPorTipo(LinkedHashSet<String> listaAuxParty, 
			LinkedHashSet<String> listaAuxBanco, LinkedHashSet<String> listaAuxProduct, 
			String tipo, String auxiliarId){
		
		if(tipo.equals("A")){
			listaAuxParty.add(auxiliarId);
		} else if (tipo.equals("B")){
			listaAuxBanco.add(auxiliarId);
		} else if (tipo.equals("P")){
			listaAuxProduct.add(auxiliarId);
		}
		
	}
	
	/**
	 * Regresa el tipo de auxiliar (A,B,P)
	 * @param delegator
	 * @param tipoAuxiliarId
	 * @param mapaAuxiliaresTipo 
	 * @return
	 * @throws GenericEntityException
	 */
	public static String getTipoCatalogo(Delegator delegator, String tipoAuxiliarId, String secuencia, String tipoMovimiento, String matriz,
			Map<String, GenericValue> mapaAuxiliaresTipo) throws GenericEntityException{
		
		if(UtilValidate.isEmpty(matriz)){
			return mapaAuxiliaresTipo.get(secuencia+tipoMovimiento).getString("tipo"+tipoMovimiento);
		} else {
			GenericValue tipoAuxiliar = delegator.findByPrimaryKey("TipoAuxiliar", UtilMisc.toMap("tipoAuxiliarId", tipoAuxiliarId));
			if(UtilValidate.isEmpty(tipoAuxiliar)){
				return null;
			}
			return tipoAuxiliar.getString("tipo");
		}
		
	}
	
	/**
	 *  Regresa la naturaleza de la cuenta
	 * @param cuenta
	 * @return
	 * @throws GenericEntityException
	 */
	public static String naturaleza(String cuenta)
			throws GenericEntityException {
		Debug.logInfo("Naturaleza",module);

		return naturalezaCuentas.get(cuenta);
	}
	
	/**
	 * Actualiza el saldo del catalogo ingresado.
	 * @param delegator
	 * @param catalogoId
	 * @param cuenta
	 * @param monto
	 * @param organizationId
	 * @param tipoCatalogo
	 * @param fechaContable
	 * @param isDebitOrCredit
	 * @throws GenericEntityException
	 */
	public static void actualizaCatalogo(String catalogoId, String tipoCatalogo,
			String cuenta, BigDecimal monto, String isDebitOrCredit, GenericValue SaldoCatalogo)
			throws GenericEntityException {

		Debug.logInfo("ingresa a actualizar catalogo",module);
		
		//Obtenemos la naturaleza de la cuenta.
		String naturaleza = naturaleza(cuenta);
		
		//Obtenemos el tipo de catalogo.
		// Obtenemos Saldo del catalogo.
		Debug.logInfo("montoInicial del catalogo "+ SaldoCatalogo.getBigDecimal("monto"),module);
		//Dependiendo la naturaleza de la cuenta y si fue un cargo o abono, se suma o resta.
		/**
		 * FIX TEMPORAL PARA CUENTAS BANCARIAS SIEMPRE SE REALIZA CARGO - ABONO
		 */
		BigDecimal montoSuma = BigDecimal.ZERO;
		if(tipoCatalogo.equalsIgnoreCase("BANCO")){
			montoSuma = (isDebitOrCredit.startsWith("D") ? 
					SaldoCatalogo.getBigDecimal("monto").add(monto) :
						SaldoCatalogo.getBigDecimal("monto").subtract(monto)); 
		} else {
			montoSuma = (naturaleza.equalsIgnoreCase(isDebitOrCredit) ?
					SaldoCatalogo.getBigDecimal("monto").add(monto) : 
						SaldoCatalogo.getBigDecimal("monto").subtract(monto));
		}
		SaldoCatalogo.set("monto",montoSuma);
		Debug.log(naturaleza.equalsIgnoreCase(isDebitOrCredit) ? "+" : "-" + monto);
		Debug.log("montoFinal del catalogo "+ SaldoCatalogo.getBigDecimal("monto"));

	}

	/**
	 * Asigna los tags de la clave presupuestal al entry.
	 * @param delegator
	 * @param acctgEntry
	 * @param clavePres
	 * @param estructura
	 * @return
	 * @throws GenericEntityException
	 */
	public static GenericValue asignaTags(Delegator delegator,
			GenericValue acctgEntry, String clavePres, GenericValue estructura)
			throws GenericEntityException {
		Debug.logInfo("Asigna Tags",module);
		
		//Traemos el campo de la clave presupuestal.
		GenericValue clavePresupuestal = delegator.findByPrimaryKey(
				"ClavePresupuestal",
				UtilMisc.toMap("clavePresupuestal", clavePres));
		
		//Se recorre por las 15 clasificaciones.
		for(int i=1; i<15;i++){
			String clasificacion = "clasificacion"+i;
			String tipoClasificacion = estructura.getString(clasificacion);
			if(tipoClasificacion!=null){
				if(tipoClasificacion.equalsIgnoreCase(clasificacionAdministrativa)){
					List<GenericValue> party = delegator.findByAnd("Party",
							UtilMisc.toMap("externalId", clavePresupuestal.getString(clasificacion)));
				acctgEntry.set("acctgTagEnumIdAdmin", party.get(0).getString("partyId"));
					
					
				}else{
					String field = "acctgTagEnumId" + i;
					List<GenericValue> enumeration = delegator.findByAnd(
							"Enumeration", UtilMisc.toMap("sequenceId", clavePresupuestal.getString(clasificacion),
									"enumTypeId", tipoClasificacion));
						if(UtilValidate.isEmpty(enumeration)){
							throw new GenericEntityException("No se encontr\u00f3 la clasificaci\u00f3n "
									+clavePresupuestal.getString(clasificacion)
									+" del tipo "+tipoClasificacion);
						} else {
							acctgEntry.set(field, enumeration.get(0).getString("enumId"));
						}
					}
			}			
		}
		//Seteamos la clavePres.
		acctgEntry.set("clavePresupuestal", clavePres);		
		return acctgEntry;
	}
	
	/**
	 * Actualiza el monto en GlAccountOrganization 
	 * @param delegator
	 * @param monto
	 * @param cuenta
	 * @param organizacionPartyId
	 * @param partyId
	 * @param naturaleza
	 * @param listGlAccountOrganization 
	 * @throws GenericEntityException
	 */
	public static void actualizaGlAccountOrganization(Delegator delegator,
			BigDecimal monto, String cuenta, String organizacionPartyId,
			String partyId, String naturaleza, List<GenericValue> listGlAccountOrganization) throws GenericEntityException {

		Debug.logInfo("Empieza GlAccountOrganization " + cuenta,module);

		// Se busca la naturaleza de la cuenta.
		String naturalezaCuenta = naturaleza(cuenta);
		if(UtilValidate.isEmpty(naturalezaCuenta)){
			throw new GenericEntityException("Se necesita configurar la naturaleza de la cuenta ["+cuenta+"]");
		}
		
		naturaleza = naturaleza.equalsIgnoreCase("D")? "DEBIT":"CREDIT";
		
		//Se verifica si el elemento que se va a actualizar ya fue consultado anteriormente para no volver a buscar y no sobreescribirlo
		GenericValue glAccountOrganization = null;
		GenericValue glAccountOrganizationParty = null;
		String glAccountId = new String();
		String glAcctOrganizationPartyId = new String();
		
		GenericValue GlAccountOrganization = null;
		Iterator<GenericValue> itGLAOrganization = listGlAccountOrganization.iterator();
		while(itGLAOrganization.hasNext()){
			GlAccountOrganization = itGLAOrganization.next();
			glAccountId = GlAccountOrganization.getString("glAccountId");
			glAcctOrganizationPartyId = GlAccountOrganization.getString("organizationPartyId");
			
			if(glAccountId.equals(cuenta) && glAcctOrganizationPartyId.equals(organizacionPartyId)){
				glAccountOrganization = GlAccountOrganization;
				itGLAOrganization.remove();
				break;
			}
		}
		
		if(UtilValidate.isEmpty(glAccountOrganization)){
			glAccountOrganization = delegator.findByPrimaryKey(
					"GlAccountOrganization", UtilMisc.toMap("glAccountId", cuenta,
							"organizationPartyId", organizacionPartyId));
		}
		
		Debug.logInfo("Organization antes ---> "+cuenta+" postedBalance : "+UtilNumber.getBigDecimal(glAccountOrganization.getBigDecimal("postedBalance"))+" monto a afectar : "+monto, module);
		
		//Impactamos glAccountOrganization
		if (glAccountOrganization.getBigDecimal("postedBalance") == null) {
			glAccountOrganization.set("postedBalance", BigDecimal.ZERO);
		}
		
		glAccountOrganization.set("postedBalance", naturalezaCuenta.equalsIgnoreCase(naturaleza) ? glAccountOrganization
				.getBigDecimal("postedBalance").add(monto):glAccountOrganization
				.getBigDecimal("postedBalance").subtract(monto)); 
		
		
		Debug.logInfo("Organization ---> "+cuenta+" postedBalance : "+UtilNumber.getBigDecimal(glAccountOrganization.getBigDecimal("postedBalance")), module);
		
		listGlAccountOrganization.add(glAccountOrganization);
		
		if (!UtilValidate.areEqual(organizacionPartyId, partyId)) {
			
			itGLAOrganization = listGlAccountOrganization.iterator();
			while(itGLAOrganization.hasNext()){
				GlAccountOrganization = itGLAOrganization.next();
				glAccountId = GlAccountOrganization.getString("glAccountId");
				glAcctOrganizationPartyId = GlAccountOrganization.getString("organizationPartyId");
				
				if(glAccountId.equals(cuenta) && glAcctOrganizationPartyId.equals(partyId)){
					glAccountOrganizationParty = GlAccountOrganization;
					itGLAOrganization.remove();
					break;
				}
			}
			
			
			if(UtilValidate.isEmpty(glAccountOrganizationParty)){
				glAccountOrganizationParty = delegator.findByPrimaryKey(
						"GlAccountOrganization", UtilMisc.toMap("glAccountId", cuenta,
								"organizationPartyId", partyId));
			}
			
			
			if (glAccountOrganizationParty.getBigDecimal("postedBalance") == null) {
				glAccountOrganizationParty.set("postedBalance", BigDecimal.ZERO);
			}
			
			glAccountOrganizationParty.set("postedBalance", naturalezaCuenta.equalsIgnoreCase(naturaleza) ? glAccountOrganizationParty
					.getBigDecimal("postedBalance").add(monto):glAccountOrganizationParty
					.getBigDecimal("postedBalance").subtract(monto)); 
			
			listGlAccountOrganization.add(glAccountOrganizationParty);
		}
	}

	/**
	 * Impacta los GlAccountHistory que correspondan a los periodos y cuenta ingresados.
	 * @param delegator
	 * @param periodos
	 * @param cuenta
	 * @param monto
	 * @param tipo
	 * @param listGlAccountHistory 
	 * @throws GenericEntityException
	 */
	public static void actualizaGlAccountHistories(
			Delegator delegator, List<GenericValue> periodos, String cuenta,
			BigDecimal monto, String tipo,
			List<GenericValue> listGlAccountHistory) throws GenericEntityException {
		tipo = tipo.equalsIgnoreCase("D")? "DEBIT":"CREDIT";
		
		GenericValue glAccountHistory = null;
		String glAccountId = new String();
		String organizationPartyId = new String();
		String customTimePeriodId = new String();
		
		Iterator<GenericValue> itGLAHistory = listGlAccountHistory.iterator();
		GenericValue GlAccountHistory = null;
		for (GenericValue periodo : periodos) {
			
			while(itGLAHistory.hasNext()){
				GlAccountHistory = itGLAHistory.next();
				glAccountId = GlAccountHistory.getString("glAccountId");
				organizationPartyId = GlAccountHistory.getString("organizationPartyId");
				customTimePeriodId = GlAccountHistory.getString("customTimePeriodId");
				
				if(glAccountId.equals(cuenta) && 
						organizationPartyId.equals(periodo.getString("organizationPartyId")) && 
							customTimePeriodId.equals(periodo.getString("customTimePeriodId"))){
					glAccountHistory = GlAccountHistory;
					itGLAHistory.remove();
					break;
				}
			}
			
			if(UtilValidate.isEmpty(glAccountHistory)){
				glAccountHistory = delegator.findByPrimaryKey(
						"GlAccountHistory", UtilMisc.toMap("glAccountId", cuenta,
								"organizationPartyId",
								periodo.getString("organizationPartyId"),
								"customTimePeriodId",
								periodo.getString("customTimePeriodId")));
			}

			if (glAccountHistory == null) {
				Debug.logInfo("No existe History",module);
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
			
			listGlAccountHistory.add(glAccountHistory);
			glAccountHistory = null;
		}
		
	}

	/**
	 * Obtiene el padre de un party
	 * @param delegator
	 * @param organizationId
	 * @return
	 * @throws RepositoryException
	 * @throws GenericEntityException
	 */
	public static String obtenPadre(Delegator delegator, String organizationId)
			throws RepositoryException, GenericEntityException {
		String parentId = organizationId;
		do {
			GenericValue generic = getPartyGroup(delegator, organizationId);

			organizationId = generic.getString("Parent_id");
			if (organizationId != null)
				parentId = generic.getString("Parent_id");
		} while (organizationId != null);
		return parentId;
	}

	/**
	 * Obtiene la PartyGroup
	 * @param delegator
	 * @param party
	 * @return
	 * @throws GenericEntityException
	 */
	public static GenericValue getPartyGroup(Delegator delegator, String party)
			throws GenericEntityException {

		GenericValue generic = delegator.findByPrimaryKey("PartyGroup",
				UtilMisc.toMap("partyId", party));

		return generic;
	}

	/**
	 * Valida la suficiencia de la cuenta bancaria y actualiza el valor del monto por cada iteracion
	 * @param cuentaContable
	 * @param isDebitOrCredit
	 * @param monto
	 * @param SaldoCatalogoCuenta
	 * @throws GenericEntityException
	 */
	public static void validaSuficienciaCuentaBancaria(String cuentaContable, String isDebitOrCredit,
				BigDecimal monto, GenericValue SaldoCatalogoCuenta) throws GenericEntityException{
		//Obtenemos la naturaleza de la cuenta.
		String naturaleza = naturaleza(cuentaContable);
		if(!naturaleza.equalsIgnoreCase(isDebitOrCredit)){
			BigDecimal resultado = SaldoCatalogoCuenta.getBigDecimal("monto").subtract(monto);
			SaldoCatalogoCuenta.put("monto", resultado);//Se actualiza el valor
			if (resultado.compareTo(BigDecimal.ZERO) == -1){
				throw new GenericEntityException("La cuenta bancaria no cuenta con el saldo suficiente: "+ resultado);
			}
		}		
	}
	
	/**
	 * Obtiene el ciclo de la fecha ingresada
	 * @param fecha
	 * @return
	 */
	public static String getCiclo(Timestamp fecha){
		long timestamp = fecha.getTime();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		return new Integer(cal.get(Calendar.YEAR)).toString();
	}
	
	/**
	 * 
	 * @param fechaContable
	 * @return
	 */
	public static int getMes(Timestamp fechaContable){
		Calendar calFecha = UtilDateTime.toCalendar(fechaContable);
		return calFecha.get(Calendar.MONTH)+1;
	}
	
	/**
	 * Revierte todas las lineas presupuestales del eventoContable.
	 * @param delegator
	 * @param clave
	 * @param fecha
	 * @param monto
	 * @param ciclo
	 * @param cuentasPresupuestales
	 * @param suficiencia 
	 * @return
	 * @throws GenericEntityException
	 */
	public static String reversaControlPresupuestal(Delegator delegator,
			String clave, Timestamp fecha, BigDecimal monto,
			String ciclo,List <GenericValue> cuentasPresupuestales,String tipoClave, 
			Map<String, GenericValue> mapControl, boolean suficiencia) throws GenericEntityException {
		Debug.logInfo("Entra a reversa control presupuestal",module);

		String mensaje="";
		String mesId = formatoMes.format(getMes(fecha));
		
		//Para cada linea se hara el mov presupuestal.
		for(GenericValue lineaPresupuestal: cuentasPresupuestales){
			String mComparativo = lineaPresupuestal.getString("momentoCompara");
			String mEjecutar1 = lineaPresupuestal.getString("momentoEjecutar1");
			String mEjecutar2 = lineaPresupuestal.getString("momentoEjecutar2");
			
			//Como es una reversa, se verifica que los momentos Ejecutar tengan saldo para una reversa.
			if (mEjecutar1 != null && !mEjecutar1.isEmpty()) {
				if(tipoClave.equalsIgnoreCase("EGRESO")&&lineaPresupuestal.getString("comparacion")!=null && suficiencia){
					if (comparaMontos(delegator, monto, lineaPresupuestal.getString("comparacion"),
							mapControl.get(clave+mEjecutar1+mesId))) {
						restaMonto(delegator, monto,mapControl.get(clave+mEjecutar1+mesId));

					} else {
						Debug.logInfo("Saldo insuficiente(Momento "+mEjecutar1+")",module);
						mensaje += montoInsuficiencia(fecha,monto,mapControl.get(clave+mEjecutar1+mesId),clave)+"\n";
					}
				}else{
					//Para Ingreso o ampliaciones de Egreso
					restaMontoReversa(delegator, monto, mapControl.get(clave+mEjecutar1+mesId));
				}				
			}

			if (mEjecutar2 != null && !mEjecutar2.isEmpty()) {
				if(tipoClave.equalsIgnoreCase("EGRESO")&&lineaPresupuestal.getString("comparacion")!=null && suficiencia){
					if (comparaMontos(delegator, monto, lineaPresupuestal.getString("comparacion"),
							mapControl.get(clave+mEjecutar2+mesId))) {
						restaMonto(delegator, monto, mapControl.get(clave+mEjecutar2+mesId));
					} else {
						Debug.logInfo("Saldo insuficiente(Momento "+mEjecutar2+")",module);
						mensaje += montoInsuficiencia(fecha,monto,mapControl.get(clave+mEjecutar2+mesId),clave)+"\n";
					}
				}else{
					//Para Ingreso o ampliaciones de Egreso
					restaMontoReversa(delegator, monto, mapControl.get(clave+mEjecutar2+mesId));
				}
			}
			
			if (mComparativo != null && !mComparativo.isEmpty()) {
				sumaMonto(delegator, tipoClave, mComparativo, fecha, monto, ciclo, mapControl.get(clave+mComparativo+mesId));
			}			
		}		
		return mensaje;
	}
	
	/**
	 * Revierte la transaccion ingresada.
	 * @param delegator
	 * @param acctgTransId
	 * @param usuario
	 * @return
	 * @throws GenericEntityException
	 * @throws RepositoryException
	 */
	public static String reversaTransaccion(Delegator delegator,
			String acctgTransId,String usuario)
			throws GenericEntityException, RepositoryException {
		
		naturalezaCuentas = getNaturalezaCuentas(delegator);
		
		boolean suficiencia = suficienciaActiva(delegator);
		
		//Obtenemos la transaccion.
		GenericValue acctgTrans = delegator.findByPrimaryKey("AcctgTrans",
				UtilMisc.toMap("acctgTransId", acctgTransId));

		if (acctgTrans == null) {
			throw new RepositoryException("No se encontr\u00f3 la transacci\u00f3n");
		}
		
		// History
		Debug.logInfo("Busca periodos",module);
		boolean cierre = false;//variable para saber si el evento es de cierre contable
		if(acctgTrans.getString("acctgTransTypeId").startsWith("CIPE")){
			cierre = true;
		} 
		List<GenericValue> periodos = getPeriodos(delegator, acctgTrans.getString("organizationPartyId"),
						acctgTrans.getTimestamp("postedDate"),cierre);
		
		//Verificamos si el tipo de transaccion es de una afectacion compensada.
		if(acctgTrans.getString("acctgTransTypeId").equalsIgnoreCase("LIAD-01")||acctgTrans.getString("acctgTransTypeId").equalsIgnoreCase("PEAD-01")){
			return reversaAfectacionCompensada(delegator,
					acctgTrans,usuario);
		}
		
		//Obtenemos las entries asociadas.
		List<GenericValue> acctgTransEntries = delegator.findByAnd(
				"AcctgTransEntry",
				UtilMisc.toMap("acctgTransId", acctgTransId),
				UtilMisc.toList("acctgTransEntrySeqId"));
		if (acctgTransEntries == null || acctgTransEntries.isEmpty()) {
			throw new RepositoryException("No se ha encontrado entries en la transacci\u00f3n");
		}
		
		String eventoContableId = acctgTrans.getString("acctgTransTypeId");
		
		List<GenericValue> listGlAccountHistory = FastList.newInstance();
		List<GenericValue> listGlAccountOrganization = FastList.newInstance();
		
		//Generamos la nueva transaccion.
		acctgTrans.set("lastModifiedByUserLogin", usuario);
		acctgTrans.remove("lastUpdatedStamp");
		acctgTrans.remove("lastUpdatedTxStamp");
		acctgTrans.set("isReversa","Y");
		acctgTrans.set("postedAmount", acctgTrans.getBigDecimal("postedAmount").multiply(new BigDecimal("2")));
		delegator.store(acctgTrans);
		
		//Se busca si el evento tiene movimientos presupuestales.
		List <EntityCondition> conditions = new FastList<EntityCondition>(); 
		conditions.add(EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("acctgTransTypeId",
						EntityOperator.EQUALS, eventoContableId)));
		List<GenericValue> lineasPresupuestales = delegator.findByAnd("LineaPresupuestal", conditions, UtilMisc.toList("secuencia DESC"));
		Timestamp fechaContable = acctgTrans.getTimestamp("postedDate");
		
		Timestamp fechaInicioMes = UtilDateTime.getMonthStart(fechaContable);
		
		String estructuraId = acctgTrans.getString("estructuraId");
		String tipoClave = "";
		if(estructuraId!=null&&!estructuraId.isEmpty()){
			GenericValue estr = delegator.findByPrimaryKey("EstructuraClave",UtilMisc.toMap("idSecuencia", estructuraId));
			tipoClave = estr.getString("acctgTagUsageTypeId");
		}
		
		GenericValue UserLogin = delegator.findByPrimaryKey("UserLogin",UtilMisc.toMap("userLoginId",usuario));
		
		Map<String,GenericValue> mapControl = FastMap.newInstance();
		
		if(lineasPresupuestales!=null&&!lineasPresupuestales.isEmpty()){
			
			//Buscamos el monto de cada clave.
			List<GenericValue> listClavePresMomentoMontoMes = delegator.findByCondition("CveMontoPoliza", EntityCondition.makeCondition("acctgTransId",acctgTransId),null,null);
			
			//Se obtienen las claves presupuestales y un mapa de ControlPresupuestal
			List<LinkedHashSet<String>> listaClavesPresup = getListClavesPresupGeneric(listClavePresMomentoMontoMes);
			List<LineaMotorContable> listLineaMotor = getLineaMotorAcctgTransEntry(listClavePresMomentoMontoMes);
			validaClavesInactivas(delegator,listaClavesPresup);
			mapControl = getMapControlPresupuestal(delegator, fechaContable, listLineaMotor);
			
			//Para cada clave encontrada.
			String mensajeError="";
			Timestamp fechaPresupuestal = UtilDateTime.nowTimestamp();
			for(GenericValue clave : listClavePresMomentoMontoMes){
				fechaPresupuestal = clave.getTimestamp("fechaPresupuestal");
				if(UtilValidate.isEmpty(fechaPresupuestal)){
					fechaPresupuestal = (Timestamp) fechaContable.clone();
				}
				//Hacemos la reversa del control presupuestal.
				mensajeError += reversaControlPresupuestal(delegator,
						clave.getString("clavePresupuestal"), fechaPresupuestal,
						clave.getBigDecimal("amount"), getCiclo(fechaContable),
						lineasPresupuestales,tipoClave,mapControl,suficiencia);
			}
			
			if(!mensajeError.isEmpty()){
				throw new GenericEntityException(mensajeError);
			}
			
		}
		
		List<GenericValue> listTraspasoBanco = FastList.newInstance();
		List<GenericValue> listAcctgTransEntry = FastList.newInstance();
		Map<String,Map<String,GenericValue>> mapaAuxiliares = getAuxiliaresLineaContablePoliza(delegator, acctgTransEntries, fechaContable, UserLogin);
		Map<String,GenericValue> mapPartyType = mapaAuxiliares.get("Party");
		Map<String,GenericValue> mapProductType = mapaAuxiliares.get("Product");
		Map<String,GenericValue> CuentaBancariaSaldo = mapaAuxiliares.get("CuentaBancariaSaldo");
		Map<String,GenericValue> CuentaBancaria = mapaAuxiliares.get("CuentaBancaria");
			
		//Se recorren las entries.
		for (GenericValue acctgTransEntry : acctgTransEntries) {
			//cambiamos el flag.
			acctgTransEntry.set("debitCreditFlag", acctgTransEntry.getString("debitCreditFlag")
					.equalsIgnoreCase("D")?"C":"D");
			
			//movemos el acctgTransId de la transaccion original a los campos padre.			
//			acctgTransEntry.set("acctgTransId",reversaAcctgTransId);
			acctgTransEntry.set("parentAcctgTransId", acctgTransId);
			acctgTransEntry.set("acctgTransEntrySeqId", acctgTransEntry.getString("acctgTransEntrySeqId")+"-R");
			acctgTransEntry.set("parentAcctgTransEntrySeqId", acctgTransEntry.getString("acctgTransEntrySeqId"));
			acctgTransEntry.remove("createdStamp");
			acctgTransEntry.remove("createdTxStamp");
			acctgTransEntry.remove("lastUpdatedStamp");
			acctgTransEntry.remove("lastUpdatedTxStamp");
			
			listAcctgTransEntry.add(acctgTransEntry);
						
			//Auxiliares y Bancos
			String catalogo = "";
			String tipo = "";
			String tipoAuxiliar = null;
			
			//Buscamos el tipo de axiliar que es.
			if(acctgTransEntry.getString("theirPartyId")!=null){
				tipo="A";
				catalogo = acctgTransEntry.getString("theirPartyId");
				if(UtilValidate.isNotEmpty(mapPartyType.get(catalogo))){
					tipoAuxiliar = mapPartyType.get(catalogo).getString("partyTypeId");
				}
			}else if(acctgTransEntry.getString("productId")!=null){
				tipo="P";
				catalogo = acctgTransEntry.getString("productId");
				if(UtilValidate.isNotEmpty(mapProductType.get(catalogo))){
					tipoAuxiliar = mapProductType.get(catalogo).getString("productTypeId");
				}
			}else if(acctgTransEntry.getString("cuentaBancariaId")!=null){
				tipo="B";
				catalogo = acctgTransEntry.getString("cuentaBancariaId");
				tipoAuxiliar = "BANCO";
			}  
			
			//Tiene auxiliares
			if(!tipo.isEmpty()){
				if(tipo.equalsIgnoreCase("B")){
					validaSuficienciaCuentaBancaria(acctgTransEntry.getString("glAccountId"), 
							acctgTransEntry.getString("debitCreditFlag").equalsIgnoreCase("D") ? "DEBIT" : "CREDIT",
							acctgTransEntry.getBigDecimal("amount"),
							CuentaBancariaSaldo.get(catalogo));
					
					registraTraspasoBanco(delegator, acctgTransEntry.getString("acctgTransId"), 
							acctgTransEntry.getString("cuentaBancariaId"),
							acctgTransEntry.getString("debitCreditFlag"),
							acctgTransEntry.getBigDecimal("amount"), acctgTransEntry.getString("description"), 
							usuario, 
							listTraspasoBanco,
							CuentaBancaria.get(catalogo));
					
				}
				
				GenericValue SaldoCatalogo = getSaldoCatalogoPorTipo(delegator, catalogo, tipo, 
						acctgTrans.getString("organizationPartyId"), fechaInicioMes, mapaAuxiliares);
				
					actualizaCatalogo(catalogo, tipoAuxiliar,
							acctgTransEntry.getString("glAccountId"), 
							acctgTransEntry.getBigDecimal("amount"),
							acctgTransEntry.getString("debitCreditFlag").equalsIgnoreCase("D") ? "DEBIT" : "CREDIT",
							SaldoCatalogo);
				
			}
						
			//Actualizamos GlAccountOrganization.
			actualizaGlAccountOrganization(delegator, acctgTransEntry.getBigDecimal("amount"),
					acctgTransEntry.getString("glAccountId"), acctgTrans.getString("organizationPartyId"),
					acctgTrans.getString("partyId"), acctgTransEntry.getString("debitCreditFlag"),
					listGlAccountOrganization);

			// Actualizamos GlAccountHistory
			Debug.logInfo("Busca histories",module);
			actualizaGlAccountHistories(delegator, periodos,
					acctgTransEntry.getString("glAccountId"),
					acctgTransEntry.getBigDecimal("amount"),
					acctgTransEntry.getString("debitCreditFlag"),
					listGlAccountHistory);			
			
		}
		
		guardaDatosPoliza(delegator, listAcctgTransEntry, listTraspasoBanco, listGlAccountHistory,
				listGlAccountOrganization, mapaAuxiliares, mapControl);
		
		return acctgTransId;
	}

	/**
	 * Genera una lista de LineaMotorContable para la reversa, a partir de una poliza
	 * @param clavesPres
	 * @return
	 */
	private static List<LineaMotorContable> getLineaMotorAcctgTransEntry(
			List<GenericValue> clavesPres) {
		
		List<LineaMotorContable> listLineaMotor = FastList.newInstance();
		
		for (GenericValue ClavePresPoliza : clavesPres) {
			LineaMotorContable lineaMotor = new LineaMotorContable();
			lineaMotor.setClavePresupuestal(ClavePresPoliza.getString("clavePresupuestal"));
			lineaMotor.setFecha(ClavePresPoliza.getTimestamp("fechaPresupuestal"));
			listLineaMotor.add(lineaMotor);
		}
		
		return listLineaMotor;
	}


	/**
	 * Revierte la transaccion ingresada.
	 * @param delegator
	 * @param acctgTransId
	 * @param usuario
	 * @return
	 * @throws GenericEntityException
	 * @throws RepositoryException
	 */
	public static String reversaAfectacionCompensada(Delegator delegator,
			GenericValue acctgTrans,String usuario)
			throws GenericEntityException, RepositoryException {
		Debug.logInfo("Entramos a reversaAfectacionCompensada",module);
		String eventoReduccion;
		String eventoAmpliacion;
		String tipoClaveId;
		
		if (acctgTrans.getString("acctgTransTypeId")
				.equalsIgnoreCase("LIAD-01")) {
			eventoReduccion = "LIAD-01";
			eventoAmpliacion = "LIAD-02";
			tipoClaveId = "INGRESO";
		} else {
			eventoReduccion = "PEAD-01";
			eventoAmpliacion = "PEAD-02";
			tipoClaveId = "EGRESO";

		}
		
		//buscamos las claves para la reduccion.
		List <LineaMotorContable> lineasR = buscaLineasReduccionReversa(delegator,acctgTrans.getString("acctgTransId"), "A"); 
		acctgTrans = agregaEventoTransaccion(delegator, acctgTrans, eventoReduccion, lineasR, tipoClaveId, usuario);
		
		//buscamos las claves para la ampliacion.
		List <LineaMotorContable> lineasA = buscaLineasReduccionReversa(delegator,acctgTrans.getString("acctgTransId"), "R"); 
		acctgTrans = agregaEventoTransaccion(delegator, acctgTrans, eventoAmpliacion, lineasA, tipoClaveId, usuario);
		
		//Generamos la nueva transaccion.
		acctgTrans.set("lastModifiedByUserLogin", usuario);
		acctgTrans.remove("lastUpdatedStamp");
		acctgTrans.remove("lastUpdatedTxStamp");
		acctgTrans.set("isReversa","Y");
		delegator.store(acctgTrans);

		return acctgTrans.getString("acctgTransId");
	}
	
	private static List<LineaMotorContable> buscaLineasReduccionReversa(
			Delegator delegator, String acctgTransId, String flag) throws GenericEntityException {
		List<String> orderBy = UtilMisc.toList("agrupadorD");
        
        EntityCondition condition = EntityCondition.makeCondition(EntityOperator.AND, 
				  EntityCondition.makeCondition("acctgTransId", EntityOperator.EQUALS, acctgTransId),
				  EntityCondition.makeCondition("flag", EntityOperator.EQUALS, flag));
        
        List<GenericValue> listDetalle = delegator.findByCondition("AfectacionCompensadaDetalle", condition, null,
        		orderBy);
        
        List<LineaMotorContable> lineas = FastList.newInstance();
        
        if(listDetalle.isEmpty()){
        	throw new GenericEntityException("No se puede hacer la reversa de la p\u00f3liza.");
        }
        
        for(GenericValue detalle : listDetalle){
        	LineaMotorContable lineaContable = new LineaMotorContable();
			lineaContable.setClavePresupuestal(detalle.getString("clavePresupuestal"));
			lineaContable.setMontoPresupuestal(detalle.getBigDecimal("monto"));
			lineaContable.setFecha(detalle.getTimestamp("fecha"));
				
			lineas.add(lineaContable);
        }
        
        return lineas;
	}

	/**
	 * Registra el traspaso realizado al pagar por medio de una cuenta bancaria
	 * @param delegator
	 * @param transaccionId
	 * @param cuentaBancariaId
	 * @param debitCreditFlag
	 * @param monto
	 * @param descripcion
	 * @param userLoginId
	 * @param listTraspasoBanco
	 * @param CuentaBancaria
	 * @throws GenericEntityException
	 */
	public static void registraTraspasoBanco(Delegator delegator,
			String transaccionId, String cuentaBancariaId,
			String debitCreditFlag, BigDecimal monto,
			String descripcion, String userLoginId,
			List<GenericValue> listTraspasoBanco, 
			GenericValue CuentaBancaria)
			throws GenericEntityException {
	
		// Creamos el registro de la transaccion
		GenericValue traspasoBanco = delegator.makeValue("TraspasoBanco");
		traspasoBanco.set("idTraspaso", delegator.getNextSeqId("TraspasoBanco"));
		traspasoBanco.put("acctgTransId", transaccionId);
		traspasoBanco.put("tipotransaccion", "TRASPASO");
		traspasoBanco.put("monto", monto);
		traspasoBanco.put("descripcion", descripcion);
		traspasoBanco.put("createdByUserLogin", userLoginId);
		if (debitCreditFlag.equalsIgnoreCase("C")) {
			traspasoBanco.put("bancoIdOrigen",CuentaBancaria.getString("bancoId"));
			traspasoBanco.put("cuentaOrigen",CuentaBancaria.getString("cuentaBancariaId"));
			traspasoBanco.put("debitCreditFlagOrigen", "C");
			traspasoBanco.put("depositoRetiroFlagOrigen", "R");
		} else {
			traspasoBanco.put("bancoIdDestino",CuentaBancaria.getString("bancoId"));
			traspasoBanco.put("cuentaDestino",CuentaBancaria.getString("cuentaBancariaId"));
			traspasoBanco.put("debitCreditFlagDestino", "D");
			traspasoBanco.put("depositoRetiroFlagDestino", "D");
		}

		listTraspasoBanco.add(traspasoBanco);
		
	}
	
	public static BigDecimal impactaAcctgTransEntry(Delegator delegator,
			GenericValue acctgTransEntry, BigDecimal monto,
			List<GenericValue> listGlAccountHistory,
			List<GenericValue> listGlAccountOrganization)
			throws GenericEntityException {
		Debug.logInfo("Impacta Entry",module);

		// Actualizamos GlAccountOrganization.
		actualizaGlAccountOrganization(delegator,
				acctgTransEntry.getBigDecimal("amount"),
				acctgTransEntry.getString("glAccountId"),
				acctgTransEntry.getString("organizationPartyId"),
				acctgTransEntry.getString("partyId"),
				acctgTransEntry.getString("debitCreditFlag"),
				listGlAccountOrganization);

		// Actualizamos GlAccountHistory
		Debug.logInfo("Busca histories",module);
		actualizaGlAccountHistories(delegator, periodos,
				acctgTransEntry.getString("glAccountId"),
				acctgTransEntry.getBigDecimal("amount"),
				acctgTransEntry.getString("debitCreditFlag"),
				listGlAccountHistory);

		// Sumamos solo los entries que sean Debit.
		return acctgTransEntry.getString("debitCreditFlag").equalsIgnoreCase(
				"D") ? monto.add(acctgTransEntry.getBigDecimal("amount"))
				: monto;
	}
	
	public static GenericValue clonaEntrySinAuxiliares(GenericValue entry){
		GenericValue clon = (GenericValue) entry.clone();
		clon.set("theirPartyId", null);
		clon.set("productId", null);
		clon.set("cuentaBancariaId", null);
		return clon;
	}
	
	public static GenericValue creaTransaccionCierreContable(Delegator delegator,
			List<Map<String, Object>> lineasContables)
			throws GenericEntityException, RepositoryException
	{
		
		List<GenericValue> listaLineasContables = new ArrayList<GenericValue>();
		Map<String, Object> mapaMonto = FastMap.newInstance();
		String descMonto = "";
		String organizationId = "";
		Timestamp fechaRegistro = null;
		Timestamp fechaContable = null;
		String usuario = "";
		String eventoContableId = "";
		String currency = "MXN";
		
		List<GenericValue> listGlAccountHistory = FastList.newInstance();
		List<GenericValue> listGlAccountOrganization = FastList.newInstance();
		List<GenericValue> listAcctgTransEntry = FastList.newInstance();
		
		for (Map<String, Object> lineaContable : lineasContables)
		{
			GenericValue lineaContableEntity = delegator.makeValue("LineaContable");
			lineaContableEntity.put("acctgTransTypeId", lineaContable.get("tipoDocumento"));
			lineaContableEntity.put("secuencia", lineaContable.get("secuencia"));
			lineaContableEntity.put("cuentaCargo", lineaContable.get("cuentaCargo"));
			lineaContableEntity.put("cuentaAbono", lineaContable.get("cuentaAbono"));
			lineaContableEntity.put("descripcion", lineaContable.get("descripcion"));
			listaLineasContables.add(lineaContableEntity);
			descMonto = String.valueOf(lineaContable.get("descripcion")) + "0"; 
			mapaMonto.put(descMonto, lineaContable.get("monto"));
			organizationId = String.valueOf(lineaContable.get("organizationId"));
			fechaRegistro = (Timestamp) lineaContable.get("fechaRegistro");
			fechaContable = (Timestamp) lineaContable.get("fechaContable");
			usuario = String.valueOf(lineaContable.get("usuario"));
			eventoContableId = String.valueOf(lineaContable.get("tipoDocumento"));
			currency = String.valueOf(lineaContable.get("currency"));
		}
		Map<String, Object> clavePresupuestalMap = FastMap.newInstance();
		clavePresupuestalMap.put("0", "");
		Map<String,Object> mapaClave = (Map<String, Object>) clavePresupuestalMap;//(indice,clave)
		LinkedList<LineaMotorContable> listLineasMotor = new LinkedList<LineaMotorContable>();
		BigDecimal suma = BigDecimal.ZERO;
		
		for (Entry<String, Object> mapa : mapaClave.entrySet())
		{
			
			GenericValue lineaMonto = delegator.makeValue("LineaContable");
			
			Map<String,GenericValue> mapaLineasContables = getListaLineasContablesCierre(mapaMonto, listaLineasContables, lineaMonto, mapa.getKey());
			Map<String,GenericValue> mapaLineasPres = FastMap.newInstance();
			
			if(lineaMonto.getBigDecimal("monto").equals(BigDecimal.ZERO)){
				throw new GenericEntityException("El monto ingresado es incorrecto, o el evento esta mal configurado, verifiquelo ");
			}
			else {
				suma = lineaMonto.getBigDecimal("monto");
			}
			if(suma.compareTo(BigDecimal.ZERO) != 0)
			{
				listLineasMotor.add(getLineaDeMapaCierre(mapaLineasContables, mapaLineasPres, suma, mapa.getKey()));
			}				
		}
		
		Debug.logInfo("comienza la creacion de la transaccion",module);
		//Traemos la naturaleza de las cuentas.
		naturalezaCuentas = getNaturalezaCuentas(delegator);

		//En caso de que no se tenga la Entidad Contable, se busca
		String partyId = obtenPadre(delegator, organizationId);

		// AcctgTrans
		GenericValue trans = GenericValue.create(delegator.getModelEntity("AcctgTrans"));
		trans.setNextSeqId();

		trans.set("transactionDate", fechaRegistro);
		trans.set("postedDate", fechaContable);
		trans.set("description", "Cierre Contable");
		trans.set("isPosted", "Y");
		trans.set("createdByUserLogin", usuario);
		trans.set("lastModifiedByUserLogin", usuario);
		trans.set("partyId", partyId);
		trans.set("organizationPartyId", organizationId);
		trans.set("acctgTransTypeId", eventoContableId);
		trans.set("roleTypeId", null);
		
		Timestamp fechaContableAux = (Timestamp) fechaContable.clone();
		
		//Monto transaccion.
		BigDecimal montoTrans = BigDecimal.ZERO;
		
		Debug.logInfo("Busca periodos",module);
		periodos = getPeriodos(delegator, organizationId,
							fechaContable,true);
			
		//Obtenemos los datos del evento contable
				GenericValue EventoContable = delegator.findByPrimaryKeyCache("EventoContable", UtilMisc.toMap("acctgTransTypeId", eventoContableId));
		
		//Generamos el numero de poliza.
		generaNumeroPoliza(delegator, EventoContable, fechaContable, trans, getCiclo(fechaContable));

		String transId = trans.getString("acctgTransId");
		
		//Se crea un contador para el numero de lineas.
		contador = 0;
		
		//Se itera por cada linea del motor contable.
		for(LineaMotorContable linea : listLineasMotor)
		{
			contador++;
			
			//Se verifica si se tiene una fecha la linea
			if(linea.getFecha()!=null)
			{
				fechaContable = linea.getFecha();				
			}
						
			if(linea.getLineasContables() != null && !linea.getLineasContables().isEmpty())
			{
				//Creamos el acctTransEntry Generico.
				GenericValue acctgTransGen = generaAcctgTransContableGenerica(delegator, organizationId, transId, currency, partyId);
		    
				//Iteramos el mapa contable para generar las entries contables.
		    	for (Map.Entry<String, GenericValue> entry : linea.getLineasContables().entrySet())
		    	{
		    		montoTrans = generaAcctgTransEntriesContableCierre(delegator, entry.getValue(), acctgTransGen, fechaContable,
		    				montoTrans,listAcctgTransEntry,listGlAccountHistory,listGlAccountOrganization);			    
		    	}
		    }
			fechaContable = fechaContableAux;//Se asigna el valor original de la fecha contable ingresada por el usuario	
		}
		
		//Indicamos el valor de la transaccion.
		trans.set("postedAmount", montoTrans);
		if(montoTrans.compareTo(BigDecimal.ZERO) == 0){
			throw new GenericEntityException("No se puede crear la poliza con monto total igual a cero");
		}
		
		delegator.create(trans);
		delegator.storeAll(listAcctgTransEntry);
		delegator.storeAll(listGlAccountHistory);
		delegator.storeAll(listGlAccountOrganization);
		

		return trans;
	}
	
	public static Map<String,GenericValue> getListaLineasContablesCierre(Map<String,Object> mapaMonto,
			List<GenericValue> listaLineasContables, GenericValue lineaContableMonto, String indice) throws GenericEntityException
	{
	
	Map<String,GenericValue> mapaRegreso = FastMap.newInstance(); 

	BigDecimal suma = BigDecimal.ZERO;
	BigDecimal TOTAL_MONTOS_CONT = BigDecimal.ZERO;

	for (GenericValue lineaPatr : listaLineasContables)
	{
		
		GenericValue lineaCont = (GenericValue) lineaPatr.clone();
		
		boolean excepcion = false;
		String descrSinEspacios = lineaCont.getString("descripcion");
		Debug.logInfo("descrSinEspacios: " + descrSinEspacios,module);
		String llaveMapa = descrSinEspacios + indice;
		Debug.logInfo("llaveMapa: " + llaveMapa,module);
		String secuencia = lineaCont.getString("secuencia");

		if(mapaMonto.containsKey(llaveMapa))
		{
			String montoString = ((BigDecimal) mapaMonto.get(llaveMapa)).toString();
			BigDecimal monto = BigDecimal.ZERO;
			if(montoString == null || montoString.equalsIgnoreCase("")){
				//throw new GenericEntityException("Debe ingresar el monto de "+lineaCont.getString("descripcion"));
				continue;
			}
			else
			{	monto = BigDecimal.valueOf(Double.valueOf(montoString));
				TOTAL_MONTOS_CONT=TOTAL_MONTOS_CONT.add(monto);
			}
			lineaCont.set("monto", monto);
			
		} else {
			//throw new GenericEntityException("Debe ingresar el monto de "+lineaCont.getString("descripcion"));
			continue;
		}
		
		if(!excepcion){
			suma = suma.add(BigDecimal.valueOf(Double.valueOf(((BigDecimal) mapaMonto.get(llaveMapa)).toString())));
			Debug.logInfo("sumaaa: " + suma,module);
		}
		
		mapaRegreso.put(secuencia, lineaCont);
		
	}
					
	lineaContableMonto.set("monto", suma);
	return mapaRegreso;
	}
	
	public static LineaMotorContable getLineaDeMapaCierre(Map<String,GenericValue> mapaLineasContables,
			Map<String,GenericValue> mapaLineasPresupuestales, BigDecimal monto, String indice)
	{
	
	LineaMotorContable linea = new LineaMotorContable();
	
	linea.setMontoPresupuestal(monto);
	linea.setLineasContables(mapaLineasContables);
	linea.setLineasPresupuestales(mapaLineasPresupuestales);
	linea.setIndice(indice);
	
	Debug.logInfo("la linea es: " + linea,module);
	return linea;
	}
	
	public static BigDecimal generaAcctgTransEntriesContableCierre(
			Delegator delegator, GenericValue lineaContable,
			GenericValue acctgTransEntry, Timestamp fechaContable, BigDecimal montoTransaccion,
			List<GenericValue> listAcctgTransEntry,
			List<GenericValue> listGlAccountHistory,List<GenericValue> listGlAccountOrganization) throws GenericEntityException {
		
		
		Debug.logInfo("Empieza AcctgTransEntry Contable Cierre",module);
		
		//Cargo->Debit
		GenericValue acctgEntryCargo =  (GenericValue) acctgTransEntry.clone();
		acctgEntryCargo.set("acctgTransEntrySeqId", lineaContable.getLong("secuencia").toString()+"CD"+contador);
		acctgEntryCargo.set("description", lineaContable.getString("description"));
		acctgEntryCargo.set("glAccountId", lineaContable.getString("cuentaCargo"));
		acctgEntryCargo.set("amount", lineaContable.getBigDecimal("monto"));
		acctgEntryCargo.set("debitCreditFlag", "D");
		acctgEntryCargo.set("reconcileStatusId", "AES_NOT_RECONCILED");
		
		montoTransaccion = impactaAcctgTransEntry(delegator, acctgEntryCargo, montoTransaccion,listGlAccountHistory,listGlAccountOrganization);
		
		listAcctgTransEntry.add(acctgEntryCargo);
		
		//Abono->Credit
		GenericValue acctgEntryAbono = clonaEntrySinAuxiliares(acctgEntryCargo);
		acctgEntryAbono.set("acctgTransEntrySeqId", lineaContable.getLong("secuencia").toString()+"CC"+contador);
		acctgEntryAbono.set("glAccountId", lineaContable.getString("cuentaAbono"));
		acctgEntryAbono.set("debitCreditFlag", "C");
		
		montoTransaccion = impactaAcctgTransEntry(delegator, acctgEntryAbono, montoTransaccion,listGlAccountHistory,listGlAccountOrganization);
		
		listAcctgTransEntry.add(acctgEntryAbono);
		
		return montoTransaccion;
	}
	
	/**
	 * Realiza la poliza de una operacion si un evento contable
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String,Object> creaTransaccionSinEvento(DispatchContext dctx, Map<String,Object> context){
	
	Delegator delegator = dctx.getDelegator();
	Timestamp fechaRegistro = (Timestamp) context.get("fechaRegistro");
	Timestamp fechaContable = (Timestamp) context.get("fechaContable");
	String usuario = (String) context.get("usuario");
	String organizationId = (String) context.get("organizationId");
	String currency = (String) context.get("currency");
	String descripcion = (String) context.get("descripcion");
	String tipoPolizaId = (String) context.get("tipoPolizaId");
	List<GenericValue> acctgTransEntries = UtilGenerics.checkList(context.get("entries"));
	
	Map<String,Object> regreso = FastMap.newInstance();
	
		Debug.logInfo("comienza la creacion de la transaccion sin evento",module);
		//Traemos la naturaleza de las cuentas.
		try {
			
			naturalezaCuentas = getNaturalezaCuentas(delegator);
	
			//En caso de que no se tenga la Entidad Contable, se busca
			String partyId = obtenPadre(delegator, organizationId);
	
			// AcctgTrans
			GenericValue trans = GenericValue.create(delegator
					.getModelEntity("AcctgTrans"));
			trans.setNextSeqId();
	
			trans.set("transactionDate", fechaRegistro);
			trans.set("postedDate", fechaContable);
			trans.set("description", descripcion);
			trans.set("isPosted", "Y");
			trans.set("createdByUserLogin", usuario);
			trans.set("lastModifiedByUserLogin", usuario);
			trans.set("partyId", partyId);
			trans.set("organizationPartyId", organizationId);
			trans.set("acctgTransTypeId","SinEvento");
			
			//Monto transaccion.
			BigDecimal montoTrans = BigDecimal.ZERO;
			
			Debug.logInfo("Busca periodos",module);
			periodos = getPeriodos(delegator, organizationId,
								fechaContable,false);
							
			//Generamos el numero de poliza.
			String numeroPoliza = generaNumeroPolizaSinEvento(delegator,tipoPolizaId, fechaContable);
			trans.set("poliza", numeroPoliza);
			trans.set("tipoPolizaId", tipoPolizaId);
			
			List<GenericValue> listGlAccountHistory = FastList.newInstance();
			List<GenericValue> listGlAccountOrganization = FastList.newInstance();
			
			//Creamos la transaccion		
			delegator.create(trans);
					
			String transId = trans.getString("acctgTransId");
			
			GenericValue UserLogin = delegator.findByPrimaryKeyCache("UserLogin", UtilMisc.toMap("userLoginId",usuario));
			
			List<GenericValue> listTraspasoBanco = FastList.newInstance();
			Map<String,Map<String,GenericValue>> mapaAuxiliares = getAuxiliaresLineaContablePoliza(delegator, acctgTransEntries, fechaContable, UserLogin);
			Map<String,GenericValue> mapPartyType = mapaAuxiliares.get("Party");
			Map<String,GenericValue> mapProductType = mapaAuxiliares.get("Product");
			Map<String,GenericValue> CuentaBancariaSaldo = mapaAuxiliares.get("CuentaBancariaSaldo");
			Map<String,GenericValue> CuentaBancaria = mapaAuxiliares.get("CuentaBancaria");
			
			Timestamp fechaInicioMes = UtilDateTime.getMonthStart(fechaContable);
			
			//Se itera por cada linea del motor contable.
			for(GenericValue acctgTransEntry : acctgTransEntries){
				Debug.logInfo("Empieza AcctgTransEntry sin Evento",module);
			    		
			    //Cargo->Debit
				acctgTransEntry.set("acctgTransId", transId);
				acctgTransEntry.set("organizationPartyId", organizationId);
				acctgTransEntry.set("currencyUomId", currency);
				acctgTransEntry.set("partyId", partyId);
				acctgTransEntry.set("acctgTransEntryTypeId", "_NA_");
				acctgTransEntry.set("description", descripcion);
				acctgTransEntry.set("reconcileStatusId", "AES_NOT_RECONCILED");
			    		    		
			    //Verificamos si usa auxiliar.
				String auxiliar = null;
				String tipo = null;
				String tipoAuxiliar = null;
				
				if(acctgTransEntry.getString("theirPartyId")!=null){
					auxiliar = acctgTransEntry.getString("theirPartyId");
					tipo = "A";
					if(UtilValidate.isNotEmpty(mapPartyType.get(auxiliar))){
						tipoAuxiliar = mapPartyType.get(auxiliar).getString("partyTypeId");
					}
				} else if (acctgTransEntry.getString("productId")!=null){
					auxiliar = acctgTransEntry.getString("productId");
					tipo = "P";
					if(UtilValidate.isNotEmpty(mapProductType.get(auxiliar))){
						tipoAuxiliar = mapProductType.get(auxiliar).getString("productTypeId");
					}
				}else if(acctgTransEntry.getString("cuentaBancariaId")!=null){
					auxiliar = acctgTransEntry.getString("cuentaBancariaId");
					tipo = "B";
					tipoAuxiliar = "BANCO";
				}
				
				
				if(UtilValidate.isNotEmpty(auxiliar) && UtilValidate.isEmpty(tipoAuxiliar)){
					throw new GenericEntityException("No se encontro el tipo de auxiliar para el auxiliar ["+auxiliar+"]");
				}
				
				if(auxiliar!=null){
					if(tipo.equalsIgnoreCase("B")){
						validaSuficienciaCuentaBancaria(
								acctgTransEntry.getString("glAccountId"), 
								acctgTransEntry.getString("debitCreditFlag").equalsIgnoreCase("D") ? "DEBIT" : "CREDIT",
								acctgTransEntry.getBigDecimal("amount"), 
								CuentaBancariaSaldo.get(auxiliar));
						registraTraspasoBanco(delegator, 
								acctgTransEntry.getString("acctgTransId"), 
								auxiliar,
								acctgTransEntry.getString("debitCreditFlag"), 
								acctgTransEntry.getBigDecimal("amount"), 
								descripcion, 
								usuario, 
								listTraspasoBanco, 
								CuentaBancaria.get(auxiliar));
					}
					
					GenericValue SaldoCatalogo = getSaldoCatalogoPorTipo(delegator, auxiliar, tipo, organizationId, fechaInicioMes, mapaAuxiliares);
					
					actualizaCatalogo(
							auxiliar, tipoAuxiliar,
							acctgTransEntry.getString("glAccountId"), 
							acctgTransEntry.getBigDecimal("amount"), 
							acctgTransEntry.getString("debitCreditFlag").equalsIgnoreCase("D") ? "DEBIT" : "CREDIT", 
							SaldoCatalogo);
					
				}
				montoTrans = impactaAcctgTransEntry(delegator, acctgTransEntry, montoTrans,listGlAccountHistory,listGlAccountOrganization);		    		
			}
	
			// Indicamos el valor de la transaccion.
			trans.set("postedAmount", montoTrans);
			if(montoTrans.compareTo(BigDecimal.ZERO) == 0){
				throw new GenericEntityException("No se puede crear la poliza con monto total igual a cero");
			}
			delegator.store(trans);
			delegator.storeAll(listGlAccountHistory);
			delegator.storeAll(listGlAccountOrganization);
			delegator.storeAll(acctgTransEntries);
			delegator.storeAll(new ArrayList<GenericValue>(mapaAuxiliares.get("SaldoCatalogoParty").values()));//Se guardan SaldoCatalogo tipo Party
			delegator.storeAll(new ArrayList<GenericValue>(mapaAuxiliares.get("SaldoCatalogoCuentaBancaria").values()));//Se guardan SaldoCatalogo tipo CuentaBancaria
			delegator.storeAll(new ArrayList<GenericValue>(mapaAuxiliares.get("SaldoCatalogoProducto").values()));//Se guardan SaldoCatalogo tipo Product
	
			regreso.put("transaccion", trans);
			return regreso;
		} catch (GenericEntityException | RepositoryException | NullPointerException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		
	}
	
}