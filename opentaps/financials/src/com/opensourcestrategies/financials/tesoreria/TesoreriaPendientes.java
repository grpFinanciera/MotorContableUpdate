package com.opensourcestrategies.financials.tesoreria;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilNumber;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.common.AB.UtilWorkflow;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.common.party.PartyHelper;
import org.opentaps.foundation.action.ActionContext;

import com.ibm.icu.util.Calendar;
import com.opensourcestrategies.financials.transactions.MotorContableFinanzas;
import com.opensourcestrategies.financials.util.UtilFinancial;

import javolution.util.FastList;
import javolution.util.FastMap;


public class TesoreriaPendientes extends MotorContableFinanzas{
	private static String MODULE = TesoreriaPendientes.class.getName();
	public final static String ordenPagoPendiente = "ORDEN_PAGO_PENDIENTE";
	public final static String rolTesorero = "TESORERO";
	public final static String tipoWorkFlowId = "ORDEN_PAGO_PATRI";
	public final static String CorreoOrdenPago = "CorreoMensajeOrdenPagoPatrimonial";
	public final static String ordenPagoPagada = "ORDEN_PAGO_PAGADA";
	
	/**
	 * Busqueda de pendientes de tesoreria
	 * @param context
	 * @throws GeneralException
	 * @throws ParseException
	 */
	public static void pendientesTesoreria(Map<String, Object> context)
			throws GeneralException, ParseException {
		final ActionContext ac = new ActionContext(context);
		final Delegator delegator = ac.getDelegator();

		// build search conditions
		List<EntityCondition> searchConditions = new FastList<EntityCondition>();
			searchConditions.add(EntityCondition.makeCondition("estatus",EntityOperator.EQUALS, "ANTICIPO_PENDIENTE_C"));
			searchConditions.add(EntityCondition.makeCondition("estatus",EntityOperator.EQUALS, "COMPROMETIDA_V"));
			searchConditions.add(EntityCondition.makeCondition("estatus",EntityOperator.EQUALS, "VALIDADA_V"));
			searchConditions.add(EntityCondition.makeCondition("estatus",EntityOperator.EQUALS, ordenPagoPendiente));	
			searchConditions.add(EntityCondition.makeCondition("estatus",EntityOperator.EQUALS, "APROBADA_GR"));
			searchConditions.add(EntityCondition.makeCondition("estatus",EntityOperator.EQUALS, "VALIDADA_GR"));
			searchConditions.add(EntityCondition.makeCondition("estatus",EntityOperator.EQUALS, "ENVIADO_PA"));
			searchConditions.add(EntityCondition.makeCondition("estatus",EntityOperator.EQUALS, "ORDER_CANCELLED"));

		// fields to select
		Debug.logInfo("Search conditions : " + searchConditions,MODULE);
		
		List<GenericValue> listPendientesTesoreria = delegator.findByCondition("BuscaPendientesTesoreria", 
				EntityCondition.makeCondition(searchConditions, EntityOperator.OR), null, null);
		
		List<Map<String,Object>> listaPendientesTesoreriaBuilder = FastList.newInstance();
		for (GenericValue BuscaPendientesTesoreria : listPendientesTesoreria) {
			Map<String, Object> PendienteTesoreria = BuscaPendientesTesoreria.getAllFields();
			if(UtilValidate.isNotEmpty(BuscaPendientesTesoreria.getString("partyId"))){
				PendienteTesoreria.put("auxNombre", PartyHelper.getCrmsfaPartyName(BuscaPendientesTesoreria));
			} else {
				PendienteTesoreria.put("auxNombre", "");
			}
			
			List<EntityCondition> condicionesMonto = new FastList<EntityCondition>();
			condicionesMonto.add(EntityCondition.makeCondition("idPendienteTesoreria",EntityOperator.EQUALS, BuscaPendientesTesoreria.getString("idPendienteTesoreria")));
			condicionesMonto.add(EntityCondition.makeCondition("tipo",EntityOperator.EQUALS, BuscaPendientesTesoreria.getString("tipo")));
			
			GenericValue PendientesTesoreriaCargoBanco = EntityUtil.getFirst(delegator.findByCondition("BuscaCargoBanco", 
					EntityCondition.makeCondition(condicionesMonto,EntityOperator.AND), null, null));
			
			GenericValue PendientesTesoreriaAbonoBanco = EntityUtil.getFirst(delegator.findByCondition("BuscaAbonoBanco", 
					EntityCondition.makeCondition(condicionesMonto,EntityOperator.AND), null, null));
			
			BigDecimal sumaMontoCargo = BigDecimal.ZERO;
			BigDecimal sumaMontoAbono = BigDecimal.ZERO;
			BigDecimal sumaMontoCargoAbono = BigDecimal.ZERO;
			
			if(UtilValidate.isNotEmpty(PendientesTesoreriaCargoBanco)){
				sumaMontoCargo = UtilNumber.getBigDecimal(PendientesTesoreriaCargoBanco.getBigDecimal("monto"));
			}
			
			if(UtilValidate.isNotEmpty(PendientesTesoreriaAbonoBanco)){
				sumaMontoAbono = UtilNumber.getBigDecimal(PendientesTesoreriaAbonoBanco.getBigDecimal("monto"));
			}
			
			sumaMontoCargoAbono = sumaMontoCargo.add(sumaMontoAbono);
			
			if(UtilValidate.isNotEmpty(PendientesTesoreriaCargoBanco) || UtilValidate.isNotEmpty(PendientesTesoreriaAbonoBanco)){
				PendienteTesoreria.put("monto", sumaMontoCargoAbono);
			}
			
			PendienteTesoreria.put("idPendienteTesoreria", Long.valueOf(BuscaPendientesTesoreria.getString("idPendienteTesoreria")));
			
			listaPendientesTesoreriaBuilder.add(PendienteTesoreria);
			PendienteTesoreria = FastMap.newInstance();
		}
		
        //Se crea un metodo comparator para ordenar los elementos del mapa
        Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
			public int compare(Map<String, Object> m1, Map<String, Object> m2) {
				int comp1 = ((Long) m1.get("idPendienteTesoreria")).compareTo((Long) m2.get("idPendienteTesoreria"));
				return comp1 == 0 ? ((String) m1.get("tipo")).compareTo((String) m2.get("tipo")) : comp1;
			}
		};

		Collections.sort(listaPendientesTesoreriaBuilder, mapComparator);
		
		ac.put("listaPendientesTesoreriaBuilder", listaPendientesTesoreriaBuilder);
		
	}
	
	/**
	 * Metodo para pagar el pendiente de tesoreria
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericServiceException
	 * @throws GenericEntityException
	 */
	public static Map<String,Object> finalizarPendienteTesoreria(DispatchContext dctx, Map<String,Object> context)
	{	
		
		Map<String, Object> result = ServiceUtil.returnSuccess();
		
		try {
			Delegator delegator = dctx.getDelegator();
			LocalDispatcher dispatcher = dctx.getDispatcher();	
			String idPendienteTesoreria = (String) context.get("idPendienteTesoreria");
			String tipo = (String) context.get("tipo");
			String moneda = (String) context.get("moneda");
			String descripcion = (String) context.get("descripcion");		
			String acctgTransTypeId = (String) context.get("acctgTransTypeId");
			String cuentaBancariaId = (String) context.get("cuentaOrigen");
			String bancoId = (String) context.get("bancoIdOrigen");
			Timestamp fechaContable = (Timestamp) context.get("fechaContable");
			String urlHost = (String) context.get("urlHost");
			Locale locale = (Locale) context.get("locale");
			
			
			GenericValue userLogin = (GenericValue) context.get("userLogin");
			String organizationPartyId = (String) context.get("organizationPartyId");
			Map<String, Object> input = FastMap.newInstance();
	        Map<String, Object> output = FastMap.newInstance();
	        Map<String, Object> montoMap = FastMap.newInstance();
	        Map<String, Object> catalogoCargoContMap = FastMap.newInstance();
	        Map<String, Object> catalogoAbonoContMap = FastMap.newInstance();
	        Map<String, Object> clavePresupuestalMap = FastMap.newInstance();
	        Map<String,Object> mapaMontoClave = FastMap.newInstance();
	        Map<String,Object> mapaProducto = FastMap.newInstance();
	        
	        GenericValue PendientesTesoreria = delegator.findByPrimaryKey("PendientesTesoreria",UtilMisc.toMap("idPendienteTesoreria",idPendienteTesoreria,"tipo",tipo));
			
			//Obtener campos
			//Si No es ORDEN_PAGO_PATRI enviar llenbar los mapasContables con esos campos
			//Si es ORDEN_PAGO_PATRI, se consulta los items para llenar la linea contable
	        
	        if(!tipo.equalsIgnoreCase("ORDEN_PAGO_PATRI"))
	        {
	        	String auxiliar = (String) context.get("auxiliar");
	        	BigDecimal monto = (BigDecimal) context.get("monto");
	        	List<String> orderByLinea = UtilMisc.toList("secuencia");
		    	EntityCondition condicionLinea = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, acctgTransTypeId));                    
				List<GenericValue> lineaList = delegator.findByCondition("LineaContable", condicionLinea, null, orderByLinea);
				if(!lineaList.isEmpty())
				{	Iterator<GenericValue> listaIter = lineaList.iterator();	        
					while (listaIter.hasNext())
					{	GenericValue item = listaIter.next();
						String descripcionLinea = item.getString("descripcion");
						descripcionLinea = descripcionLinea.replaceAll(" ", "");
						catalogoCargoContMap
								.put(descripcionLinea + "0",
										item.getString("catalogoCargo") != null
												&& item.getString("catalogoCargo")
														.equalsIgnoreCase("BANCO") ? cuentaBancariaId
												: auxiliar);
						catalogoAbonoContMap
								.put(descripcionLinea + "0",
										item.getString("catalogoAbono") != null
												&& item.getString("catalogoAbono")
														.equalsIgnoreCase("BANCO") ? cuentaBancariaId
												: auxiliar);
			        	montoMap.put(descripcionLinea+"0", monto.toString());
					}
				}        	
	        }
	        else
	        {	
	        	List<String> orderBy = UtilMisc.toList("secuenciaLineaContable");
	        	EntityCondition condicion = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("idPendienteTesoreria", EntityOperator.EQUALS, idPendienteTesoreria),
					EntityCondition.makeCondition("tipo", EntityOperator.EQUALS, tipo));                    
				List<GenericValue> itemsList = delegator.findByCondition("PendientesTesoreriaItem", condicion, null, orderBy);
				if(!itemsList.isEmpty())
				{					
					Iterator<GenericValue> itemsIter = itemsList.iterator();	        
					while (itemsIter.hasNext())
					{	GenericValue item = itemsIter.next();
						String descripcionLinea = item.getString("descripcion");
						descripcionLinea = descripcionLinea.replaceAll(" ", "");
						if(item.getString("idCatalogoCargo") != null && !item.getString("idCatalogoCargo").equals(""))
						{	if(item.getString("idCatalogoCargo").equals("BANCO"))
							{	
								catalogoCargoContMap.put(descripcionLinea+"0", cuentaBancariaId);
							}					
							else
							{	
								catalogoCargoContMap.put(descripcionLinea+"0", item.getString("idCatalogoCargo"));
							}						
						}
						if(item.getString("idCatalogoAbono") != null && !item.getString("idCatalogoAbono").equals(""))
						{	if(item.getString("idCatalogoAbono").equals("BANCO"))
							{	
							catalogoAbonoContMap.put(descripcionLinea+"0", cuentaBancariaId);
							}					
							else
							{	
								catalogoAbonoContMap.put(descripcionLinea+"0", item.getString("idCatalogoAbono"));							
							}
						}
						if(item.getString("monto") != null && !item.getString("monto").equals(""))
						{	montoMap.put(descripcionLinea+"0", item.getString("monto"));					
						}
					}
			        
				}               	
	        }
	        
	        if(tipo.equals("PAGO_EXTERNO")){
	        	GenericValue PagoExterno = PendientesTesoreria.getRelatedOne("PagoExterno");
	        	clavePresupuestalMap.put("0",PagoExterno.getString("clavePresupuestal"));
	        	mapaMontoClave.put("0", PendientesTesoreria.getString("monto"));
	        } else {
	        	clavePresupuestalMap.put("0", "");
	        }
	        
	        context.put("catalogoCargoContMap", catalogoCargoContMap);
	        context.put("catalogoAbonoContMap", catalogoAbonoContMap);
	        context.put("montoMap", montoMap);        
	        context.put("clavePresupuestalMap", clavePresupuestalMap);
	        context.put("mapaMontoClave", mapaMontoClave);
	        							
	        /*****MOTOR CONTABLE*****/
	        input.put("eventoContableId", acctgTransTypeId);
	    	input.put("tipoClaveId", "EGRESO");
	    	input.put("fechaRegistro", UtilDateTime.nowTimestamp());
	    	input.put("fechaContable", fechaContable);
	    	input.put("currency", moneda);
	    	input.put("usuario", userLogin.getString("userLoginId"));
	    	input.put("organizationId", organizationPartyId);
	    	input.put("descripcion", descripcion);
	    	input.put("roleTypeId", "BILL_FROM_VENDOR");
	    	input.put("valorCampo", idPendienteTesoreria);
	    	/*if(tipo.equals("CONTRATO"))
	    	{	input.put("campo", "contratoId");
	    	}    	
	    	else if(tipo.equals("VIATICOS"))
	    	{	input.put("campo", "viaticoId");
	    	}    	
	    	else if(tipo.equals("ORDEN_PAGO_PATRI"))
	    	{	input.put("campo", "ordenPagoPatri");
	    	} */   
	    	if (tipo.startsWith("GASTORESERVA") || tipo.startsWith("DEVO_GASTOS")){
	    		input.put("campo", "gastosReservaId");
 			}
	    	input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(acctgTransTypeId, delegator, context, null, cuentaBancariaId, mapaMontoClave, mapaProducto));
	    	    	
	    	output = dispatcher.runSync("creaTransaccionMotor", input);
	    	Debug.logInfo("output: " + output,MODULE);
	    	if(ServiceUtil.isError(output)){
	    		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
	    	}
	    	
	    	GenericValue transaccion = (GenericValue) output.get("transaccion");
	    	if(tipo.equals("PAGO_EXTERNO")){
		    	transaccion.put("numeroCheque", PendientesTesoreria.getString("numeroCheque"));
		    	transaccion.put("beneficiarioCheque", PendientesTesoreria.getString("nombreExterno"));
		    	transaccion.store();
	    	}

	    	
	    	//Actualiza el algunos campos de PendientesTesoreria
			String siguienteStatus = obtenSiguienteStatus((String) context.get("estatus"));
	    	GenericValue pendientesTesoreria = delegator.findByPrimaryKey("PendientesTesoreria", UtilMisc.toMap("idPendienteTesoreria", idPendienteTesoreria, "tipo", tipo));
			pendientesTesoreria.set("estatus", siguienteStatus);
			pendientesTesoreria.set("poliza", transaccion.getString("poliza"));
			pendientesTesoreria.set("bancoId", bancoId);
			pendientesTesoreria.set("cuentaBancariaId", cuentaBancariaId);
			pendientesTesoreria.set("acctgTransId", transaccion.getString("acctgTransId"));
			pendientesTesoreria.set("acctgTransTypeId", acctgTransTypeId);
			delegator.store(pendientesTesoreria);
			
			//Actualiza el estatus en la tabla correspondiente
			String nombreEntidad = new String();
			String nombreCampoPoliza = new String();
			String nombreCampoStatusId = new String("statusId");
		    // Si todo sale bien, se afecta el estatus del contrato o viatico
			if (tipo.equals("CONTRATO"))
			{
				nombreEntidad = "ContratoObra";
			}
			else if(tipo.startsWith("PAGO_ANTICIPADO"))
			{
				nombreEntidad="OrderHeader";
			}
			else if (tipo.startsWith("VIATICOS"))
			{
				nombreEntidad = "Viatico";
			} 
			else if (tipo.startsWith("GASTORESERVA") || tipo.startsWith("DEVO_GASTOS")){
				nombreEntidad = "GastoReservaComp";
				if(tipo.startsWith("GASTORESERVA")){
					nombreCampoPoliza = "acctgTransIdOtorga";
				} else {
					nombreCampoPoliza = "acctgTransIdDevuelve";
				}
 			}
			
			if(UtilValidate.isNotEmpty(nombreEntidad)){
				GenericValue EntidadEstatus = delegator.findByPrimaryKey(
						nombreEntidad,UtilMisc.toMap(delegator.getModelEntity(nombreEntidad).getFirstPkFieldName(),idPendienteTesoreria));
				EntidadEstatus.set(nombreCampoStatusId, siguienteStatus);
				if(UtilValidate.isNotEmpty(nombreCampoPoliza)){
					EntidadEstatus.set(nombreCampoPoliza, transaccion.getString("acctgTransId"));
				}
				EntidadEstatus.store();
			}
			
			result = ServiceUtil.returnSuccess("La Orden de pago patrimonial ha sido pagada con \u00e9xito");
			result.put("idPendienteTesoreria", String.valueOf(idPendienteTesoreria));
			result.put("tipo", tipo);
			
			if (tipo.equals("VIATICOS")||tipo.equals("VIATICOSGI")||tipo.equals("VIATICOSVI")){
				String correoOrigen = "7";
    			String correoDestino = pendientesTesoreria.getString("auxiliar");
    			String mensaje = "Se le recuerda que cuenta con 10 dias habiles, a partir de la fecha final de su viaje, para efectuar la comprobacion del mismo. En caso de no hacerlo, la funcion para solicitar viaticos sera desactivada temporalmente para su usuario";
    			UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,tipo,mensaje,
    					urlHost,"",null,delegator,locale,dispatcher);
			}
			
		} catch(GenericEntityException | GenericServiceException | NullPointerException e){
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
			
		return result;
	}
	
	public static String obtenSiguienteStatus(String status)
	{
		if (status.equals("ANTICIPO_PENDIENTE_C")){
			return "ANTICIPO_PAGADO_C";
		} else if (status.equals("COMPROMETIDA_V")){
			return "PAGADA_V";
		} else if (status.equals(ordenPagoPendiente)){
			return "ORDEN_PAGO_PAGADA";
		} else if (status.equals("VALIDADA_V")){
			return "FINALIZADA_V";
		} else if(status.equals("APROBADA_GR")){
			return "OTORGADA_GR";
		}  else if(status.equals("VALIDADA_GR")){
			return "FINALIZADA_GR";
		}  else if(status.equals("ENVIADO_PA")){
			return "ORDER_APPROVED";
		}	else if(status.equals("ORDER_CANCELLED")){
			return "ORDER_DEVO";
		}
		else
		{
			return status;
		}
		

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
	
	/**
	 * Guarda la orden de pago patrimonial
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String,?> guardaOrdenPagoPatrimonial(DispatchContext dctx, Map<String,?> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Locale locale = (Locale) context.get("locale");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String organizationPartyId = (String) context.get("organizationPartyId");
		String tipo = (String) context.get("tipo");
		String acctgTransTypeId = (String) context.get("acctgTransTypeId");		
		String urlHost = (String) context.get("urlHost");
		String descripcion = (String) context.get("descripcion");		
		String moneda = (String) context.get("moneda");		
		Timestamp fechaTransaccion = (Timestamp) context.get("fechaTransaccion");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		Map<String,String> montoMap = UtilGenerics.checkMap(context.get("montoMap"));	//Mapa de montos de lineas contables por Invoice_item
		Map<String,String> catalogoCargoContMap = UtilGenerics.checkMap(context.get("catalogoCargoContMap")); //Mapa de auxiliares de cargo por linea contable de cada Invoice_item
		Map<String,String> catalogoAbonoContMap =  UtilGenerics.checkMap(context.get("catalogoAbonoContMap")); //Mapa de auxiliares de cargo por linea contable de cada Invoice_item
		long idPendienteTesoreria = 0;
		try 
		{	BigDecimal conversionFactor = UtilFinancial.determineUomConversionFactor(delegator, dispatcher, organizationPartyId, moneda);
			Debug.log("conversionFactor: " + conversionFactor);
			
			idPendienteTesoreria = delegator.findCountByAnd("PendientesTesoreria",UtilMisc.toMap("tipo",tipo));
			Debug.log("idPendienteTesoreria: " + idPendienteTesoreria);
			idPendienteTesoreria++;			
			
			GenericValue pendientesTesoreria = GenericValue.create(delegator.getModelEntity("PendientesTesoreria"));
			pendientesTesoreria.set("idPendienteTesoreria", String.valueOf(idPendienteTesoreria));
			pendientesTesoreria.set("tipo", tipo);
			pendientesTesoreria.set("fechaTransaccion", fechaTransaccion);
			pendientesTesoreria.set("fechaContable", fechaContable);
			pendientesTesoreria.set("acctgTransTypeId", acctgTransTypeId);			
			pendientesTesoreria.set("descripcion", descripcion);
			pendientesTesoreria.set("moneda", moneda);
			pendientesTesoreria.set("estatus", ordenPagoPendiente);
			Debug.log("pendientesTesoreria: " + pendientesTesoreria);
			delegator.create(pendientesTesoreria);
			
			
			
			//Generar un registro en tabla PendientesTesoreriaItem
  			List<String> orderByLineaCont = UtilMisc.toList("secuencia");
	        EntityCondition condLineaCont = EntityCondition.makeCondition(EntityOperator.AND,
  										EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, acctgTransTypeId));					
	        List<GenericValue> lineaContList = delegator.findByCondition("LineaContable", condLineaCont, null, orderByLineaCont);
	        Iterator<GenericValue> lineaContIter = null;
	        	       

	        
	        				  				  				  				  				
			if(!lineaContList.isEmpty())
	        {	lineaContIter = lineaContList.iterator();
		        while (lineaContIter.hasNext())
				{	GenericValue PendientesTesoreriaItem = GenericValue.create(delegator.getModelEntity("PendientesTesoreriaItem"));  					
					PendientesTesoreriaItem.set("idPendienteTesoreria", idPendienteTesoreria);
		        	GenericValue lineaContItem = (GenericValue) lineaContIter.next();
					Debug.log("lineaContItem: " + lineaContItem);					
					String descCont = lineaContItem.getString("descripcion");
					String stringMapas = descCont+String.valueOf(0);						
					stringMapas = stringMapas.replaceAll(" ", "");										
					String catalogoCargo = (String) catalogoCargoContMap.get(stringMapas);
					String catalogoAbono = (String) catalogoAbonoContMap.get(stringMapas);						
					String montoContString = (String) montoMap.get(stringMapas);						
					BigDecimal montoCont = BigDecimal.ZERO;
					if(montoContString != null && !montoContString.equals(""))
					{	montoCont = new BigDecimal(montoContString);							
					}																		
					PendientesTesoreriaItem.set("secuenciaLineaContable", lineaContItem.getString("secuencia"));					
					PendientesTesoreriaItem.set("descripcion", lineaContItem.getString("descripcion"));
					if(lineaContItem.get("catalogoCargo") != null && lineaContItem.get("catalogoCargo").equals("BANCO"))
					{	PendientesTesoreriaItem.set("idCatalogoCargo", "BANCO");						
					}
					else	
					{	PendientesTesoreriaItem.set("idCatalogoCargo", catalogoCargo);						
					}
					if(lineaContItem.get("catalogoAbono") != null && lineaContItem.get("catalogoAbono").equals("BANCO"))
					{	PendientesTesoreriaItem.set("idCatalogoAbono", "BANCO");						
					}
					else
					{	PendientesTesoreriaItem.set("idCatalogoAbono", catalogoAbono);
					}					
					PendientesTesoreriaItem.set("tipo", tipo);
					if(!conversionFactor.equals("") && conversionFactor != null)
					{	PendientesTesoreriaItem.set("monto", montoCont.multiply(conversionFactor));
					}
					else
					{	PendientesTesoreriaItem.set("monto", montoCont);
					}
					delegator.create(PendientesTesoreriaItem);					
				}
	        }			
			
			
			//Envia corre electronico al tesorero					
			String correoOrigen = userLogin.getString("partyId");
			
			EntityCondition condTesorero = EntityCondition.makeCondition(EntityOperator.AND,
   					EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, rolTesorero));
			List<GenericValue> correoDestinatario = delegator.findByCondition("PartyRole", condTesorero, UtilMisc.toList("partyId"), null);
			if(correoDestinatario.isEmpty())
			{	return ServiceUtil.returnError("No existe al menos un usuario con Rol de TESORERO para realizar la Orden de pago");			
			}
			else
			{	Iterator<GenericValue> tesoreroIter = correoDestinatario.iterator();
				while (tesoreroIter.hasNext())
				{	GenericValue generic = tesoreroIter.next();
					String correoDestino = generic.getString("partyId");					
					UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,tipoWorkFlowId,CorreoOrdenPago,urlHost,String.valueOf(idPendienteTesoreria),null,delegator,locale,dispatcher);
				}
			}
		} 
		catch (GeneralException e) 
		{
			return ServiceUtil.returnError("Ocurrio un error : "+e.getMessage());
		}
		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("idPendienteTesoreria", String.valueOf(idPendienteTesoreria));
		result.put("tipo", String.valueOf(tipo));
		return result;
	}
}