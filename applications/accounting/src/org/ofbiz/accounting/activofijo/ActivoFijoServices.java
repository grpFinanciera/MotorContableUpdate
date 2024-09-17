package org.ofbiz.accounting.activofijo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.common.AB.UtilCommon;
import org.ofbiz.common.AB.UtilMessage;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

public final class ActivoFijoServices {

	public static Map<String,?> actualizaPoliza(DispatchContext dctx,
			Map<String,?> context) throws GenericEntityException{
		
		String numeroAnterior = (String) context.get("numeroPolizaAnt");
		String numeroNuevo = (String) context.get("numeroPolizaNueva");
		Locale locale = (Locale) context.get("locale");
		
		try {
			
			Delegator delegator = dctx.getDelegator();
			
			List<GenericValue> activos = delegator.findByAnd("FixedAsset",UtilMisc.toMap("numeroPoliza",numeroAnterior ));
			
			for (GenericValue activo : activos) {
				activo.set("numeroPoliza", numeroNuevo);
			}
			
			delegator.storeAll(activos);
				
		} catch (GenericEntityException e) {
			e.printStackTrace();
			throw new GenericEntityException("No se pudo actualizar la p\u00f3liza");
		}
		
		
		String mensaje = UtilMessage.expandLabel(
				"ActualizaPolizaMssg", locale, 
					UtilMisc.toMap("numeroAnt",numeroAnterior,"numeroNuevo",numeroNuevo));
		
		return ServiceUtil.returnSuccess(mensaje);
		
	}
	
	@SuppressWarnings("deprecation")
	public static Map<String,?> ProcesoDepreciacionActivo(DispatchContext dctx, Map context) throws GenericServiceException, GenericEntityException{
		
		Delegator delegator = dctx.getDelegator();
		
		
		LocalDispatcher dispatcher = dctx.getDispatcher();
		 
		String tipoActivoFijoId = (String) context.get("tipoActivoFijoId");
		String mesId = (String) context.get("mesId");
		String cicloId = (String) context.get("cicloId");
		int mesInt = Integer.parseInt(mesId);
		int cicloInt = Integer.parseInt(cicloId);
		GenericValue userLogin = (GenericValue) context.get("userLogin");		
		String organizationPartyId = UtilCommon.getOrganizationPartyId(userLogin.getString("userLoginId"), delegator);
		String partyId = (String) context.get("partyId");					
		Debug.log("Omar-organizationPartyId: " + organizationPartyId);
		Debug.log("Omar-partyId: " + partyId);
		String acctgTransTypeId = (String) context.get("acctgTransTypeId");
		String comentario = (String) context.get("comentario");
		Timestamp fechaTrans = new Timestamp(Calendar.getInstance().getTimeInMillis());
		Timestamp fechaContable = fechaTrans;
		String poliza = null;
		String acctgTransId = null;
		boolean existenActivos = false;
		Map<String, Object> input = FastMap.newInstance();
        Map<String, Object> output = FastMap.newInstance();
        Map<String, Object> montoMap = FastMap.newInstance();        
        Map<String, Object> clavePresupuestalMap = FastMap.newInstance();
        Map<String, Object> catalogoCargoContMap = FastMap.newInstance();
        Map<String, Object> catalogoAbonoContMap = FastMap.newInstance();
        BigDecimal totalValorDepreciacion = BigDecimal.ZERO;
        BigDecimal valorDepreciacion = BigDecimal.ZERO;
        BigDecimal depreciacionMensual = BigDecimal.ZERO;
        String moneda = "";
		
		Calendar fechaDesdeCal = GregorianCalendar.getInstance();
		Calendar fechaHastaCal = GregorianCalendar.getInstance();
		
		fechaDesdeCal.set(Calendar.DATE, 1);
		fechaHastaCal.set(Calendar.DATE, 1);
		
		fechaDesdeCal.set(Calendar.YEAR, cicloInt);
		fechaDesdeCal.set(Calendar.MONTH, mesInt-1);				
		
		if(mesId.equals("12"))
		{	fechaHastaCal.set(Calendar.YEAR, cicloInt+1);
        	fechaHastaCal.set(Calendar.MONTH, 1);
        }
		else
		{	fechaHastaCal.set(Calendar.YEAR, cicloInt);
			fechaHastaCal.set(Calendar.MONTH, mesInt);    		
		}
                                
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        Debug.log("Fecha FormateadafechaDesdeCal: "+sdf.format(fechaDesdeCal.getTime()));
        Debug.log("Fecha FormateadafechaHastaCal: "+sdf.format(fechaHastaCal.getTime()));
                
		Debug.log("Omar-tipoActivoFijoId: " + tipoActivoFijoId);
		Debug.log("Omar-mesId: " + mesId);
		Debug.log("Omar-cicloId: " + cicloId);
		Debug.log("Omar-acctgTransTypeId: " + acctgTransTypeId);
		Debug.log("Omar-comentario: " + comentario);
		
		
		try
		{	
			
			
			//Valida que no exista un periodo despues del periodo seleccionado 
			List<String> orderBy = UtilMisc.toList("idDepreciacionPeriodo");
			EntityCondition condicionesPeriodo = EntityCondition.makeCondition(EntityOperator.AND,										  
										  EntityCondition.makeCondition("tipoActivoFijo", EntityOperator.EQUALS, tipoActivoFijoId));
			List<GenericValue> periodoAnterior = delegator.findByCondition("DepreciacionPeriodo", condicionesPeriodo, null, orderBy);
			Debug.log("Omar-periodoAnterior: " + periodoAnterior);						
			
			if(!periodoAnterior.isEmpty())			
			{	GenericValue compara = periodoAnterior.get(0);
				String mesCompara = compara.getString("mes");
				String cicloCompara = compara.getString("ciclo");
				Debug.log("Omar-mesCompara: " + mesCompara);
				Debug.log("Omar-cicloCompara: " + cicloCompara);
				if(cicloInt < Integer.parseInt(cicloCompara))
				{	return ServiceUtil.returnError("El periodo seleccionado es anterior al primer periodo depreciado en el sistema");							
				}
				else if(cicloInt == Integer.parseInt(cicloCompara))
				{	Debug.log("Omar-elseIfCompara");
					if(mesInt < Integer.parseInt(mesCompara))
					{	return ServiceUtil.returnError("El periodo seleccionado es anterior al primer periodo depreciado en el sistema");							
					}
					else if(mesInt == Integer.parseInt(mesCompara))
					{	return ServiceUtil.returnError("El periodo seleccionado ya ha sido depreciado anteriormente");							
					}									
				}
			}
							
			
			EntityCondition condTipoActivo = EntityCondition.makeCondition(EntityOperator.AND,
					  EntityCondition.makeCondition("fixedAssetTypeId", EntityOperator.EQUALS, tipoActivoFijoId));
			List<GenericValue> itemsActivoFijo = delegator.findByCondition("ItemsPorcentajeActivoFijo", condTipoActivo, null, null);
			Debug.log("Omar-itemsActivoFijo: " + itemsActivoFijo);
			if(!itemsActivoFijo.isEmpty())
			{	Iterator<GenericValue> activoFijoIter = itemsActivoFijo.iterator();
			
				while (activoFijoIter.hasNext())
				{	GenericValue generic = activoFijoIter.next();
					Timestamp fechaAdquisicion = (Timestamp)generic.getTimestamp("dateAcquired");
					Date vidaUtilEsperada = generic.getDate("expectedEndOfLife");
					Date vidaUtilActual = generic.getDate("actualEndOfLife");
					BigDecimal precioCompra = generic.getBigDecimal("purchaseCost");
					BigDecimal porcentajeDecimal = generic.getBigDecimal("montoDepreciacion");
					moneda = generic.getString("moneda");
					String fixedAssetId = generic.getString("fixedAssetId");
					
					Debug.log("Omar-fechaAdquisicion: " + fechaAdquisicion);
					Debug.log("Omar-vidaUtilEsperada: " + vidaUtilEsperada);
					Debug.log("Omar-vidaUtilActual: " + vidaUtilActual);
					Debug.log("Omar-precioCompra: " + precioCompra);
					Debug.log("Omar-porcentajeDecimal: " + porcentajeDecimal);
					
					if(fechaAdquisicion != null)
					{	Debug.log("Omar-fechaAdquisicion.getDate: " + fechaAdquisicion.getDate());
						Debug.log("Omar-fechaAdquisicion.getMonth: " + fechaAdquisicion.getMonth());
						Debug.log("Omar-fechaAdquisicion.getYear: " + fechaAdquisicion.getYear());					
					
						/* Operacion de depreciacion
						 * Revisar si se va a tener un valor historico del activo (valuar actual depreciado)
						 * o se va a calcular desde su fecha de adquisicion*/
						
						
						BigDecimal meses = ((BigDecimal.valueOf(100).divide(porcentajeDecimal, 6, RoundingMode.HALF_UP))).multiply(BigDecimal.valueOf(12));
						Debug.log("Omar-meses: " + meses);
													
						Calendar adqui = Calendar.getInstance();				
						
						adqui.set(Calendar.DATE, fechaAdquisicion.getDate());
						adqui.set(Calendar.MONTH, fechaAdquisicion.getMonth());
						adqui.set(Calendar.YEAR, fechaAdquisicion.getYear()+1900);
						Debug.log("Omar-adqui: " + adqui.getTime());
						Debug.log("Omar-adquiFormat: " + sdf.format(adqui.getTime()));
						
						
						
						
						//Sumamos meses de la operacion
						adqui.add(Calendar.MONTH, Integer.valueOf(meses.intValue())); //Sumamos los meses del resultado
						Debug.log("Omar-adquiSuma: " + sdf.format(adqui.getTime()));
						
						Debug.log("Omar-adquiFechaFormateada: " + sdf.format(adqui.getTime()));
						
						/*Comparar la fecha 
						Fecha FormateadafechaDesdeCal: 01/09/2014
						Fecha FormateadafechaHastaCal: 01/10/2014
						*/
						
						if(sdf.format(fechaHastaCal.getTime()).compareTo(sdf.format(adqui.getTime())) < 0)
						{	Debug.log("Fecha es menor, se va a depreciar");
							depreciacionMensual = porcentajeDecimal.divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);
							Debug.log("Omar-depreciacionMensual: " + depreciacionMensual);
							valorDepreciacion = BigDecimal.ZERO;
							valorDepreciacion = ((precioCompra.multiply(depreciacionMensual)).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
							Debug.log("Omar-valorDepreciacion: " + valorDepreciacion);
							
							totalValorDepreciacion = totalValorDepreciacion.add(valorDepreciacion);
							registraDepreciacionResumen(delegator, fixedAssetId, poliza, mesId, cicloId, valorDepreciacion, depreciacionMensual, tipoActivoFijoId, organizationPartyId, acctgTransId);
							existenActivos=true;		            													
						}	
					}
				}
			}
			else
			{	return ServiceUtil.returnError("No existen Activos registrados con el Tipo de activo fijo: ["+ tipoActivoFijoId+ "]");				
			}
			
			
            if(existenActivos)
            {	List<String> orderByLinea = UtilMisc.toList("secuencia");
		    	EntityCondition condicionLinea = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, acctgTransTypeId));                    
				List<GenericValue> lineaList = delegator.findByCondition("LineaContable", condicionLinea, null, orderByLinea);
				Debug.log("condicion: " + condicionLinea);
				Debug.log("lineaList: " + lineaList);
				if(!lineaList.isEmpty())
				{	Iterator<GenericValue> listaIter = lineaList.iterator();	        
					while (listaIter.hasNext())
					{	GenericValue item = listaIter.next();
						String descripcionLinea = item.getString("descripcion");
						descripcionLinea = descripcionLinea.replaceAll(" ", "");
						Debug.log("descripcionLinea: " + descripcionLinea);						
			        	montoMap.put(descripcionLinea+"0", totalValorDepreciacion.toString());
			        	catalogoCargoContMap.put(descripcionLinea+"0", "");
			        	catalogoAbonoContMap.put(descripcionLinea+"0", "");
					}
				}        					
	        	clavePresupuestalMap.put("0", "");	        	
            		        	
	            context.put("montoMap", montoMap);        
	            context.put("clavePresupuestalMap", clavePresupuestalMap);
	            context.put("catalogoCargoContMap", catalogoCargoContMap);
	            context.put("catalogoAbonoContMap", catalogoAbonoContMap);
	            
	            Debug.log("montoMap: " + montoMap);
	            Debug.log("clavePresupuestalMap: " + clavePresupuestalMap);
	            Debug.log("catalogoCargoContMap: " + catalogoCargoContMap);
	            Debug.log("catalogoAbonoContMap: " + catalogoAbonoContMap);
            	
            	//Invocacion a motor contable
            	/*****MOTOR CONTABLE*****/
                input.put("eventoContableId", acctgTransTypeId);
            	input.put("tipoClaveId", "EGRESO");
            	input.put("fechaRegistro", fechaTrans);
            	input.put("fechaContable", fechaContable);
            	input.put("currency", moneda);
            	input.put("usuario", userLogin.getString("userLoginId"));
            	input.put("organizationId", organizationPartyId);
            	input.put("descripcion", comentario);
            	input.put("roleTypeId", "BILL_FROM_VENDOR");
            	//input.put("valorCampo", "fixedAssetId");
            	//input.put("campo", "fixedAssetId");            	    	   
            	input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(acctgTransTypeId, delegator, context, null, null, null, null));
            	
            	
            	//Genera registro en DepreciacionResumen
            	output = dispatcher.runSync("creaTransaccionMotor", input);
            	Debug.log("output: " + output);
            	if(ServiceUtil.isError(output)){
            		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
            	}
            	
            	GenericValue transaccion = (GenericValue) output.get("transaccion");
            	poliza = transaccion.getString("poliza");
            	acctgTransId = transaccion.getString("acctgTransId");
            	
            	actualizaPolizaAcctgTrans(delegator, poliza, acctgTransId, mesId, cicloId);
            	
            	Debug.log("itemsActivoFijo: " + itemsActivoFijo);
            	
            	
            	
            	//Genera registro por el periodo depreciado
            	Timestamp fechaDesde = new Timestamp(fechaDesdeCal.getTimeInMillis());
            	Timestamp fechaHasta = new Timestamp(fechaHastaCal.getTimeInMillis());
            	GenericValue depreciacionPeriodo = GenericValue.create(delegator.getModelEntity("DepreciacionPeriodo"));
				String idDepPeriodo = delegator.getNextSeqId("DepreciacionPeriodo");
				depreciacionPeriodo.set("idDepreciacionPeriodo", idDepPeriodo);			
				depreciacionPeriodo.set("mes", mesId);
				depreciacionPeriodo.set("ciclo", cicloId);
				depreciacionPeriodo.set("tipoActivoFijo", tipoActivoFijoId);
				depreciacionPeriodo.set("fechaDesde", fechaDesde);
				depreciacionPeriodo.set("fechaHasta", fechaHasta);				
				Debug.log("Omar - depreciacionPeriodo: " + depreciacionPeriodo);
				delegator.create(depreciacionPeriodo);
            }
		}
		catch (GenericEntityException e) {
			return ServiceUtil.returnError("Error al realizar la depreciaci\u00f3n");
		}
		
							
		
		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("poliza", poliza);
		
		return result;
		
	}
	
	
	public static void registraDepreciacionResumen(Delegator delegator, String fixedAssetId, String poliza, String mesId, String cicloId, 
			BigDecimal valorDepreciacion, BigDecimal depreciacionMensual, String tipoActivoFijoId, String organizationPartyId, String acctgTransId) throws GenericEntityException			
	{	//Genera registro por cada activo y su monto de depreciacion
	    GenericValue depreciacionResumen = GenericValue.create(delegator.getModelEntity("DepreciacionResumen"));
		String idResumen = delegator.getNextSeqId("DepreciacionResumen");
		depreciacionResumen.set("idDepreciacionResumen", idResumen);
		depreciacionResumen.set("fixedAssetId", fixedAssetId);
		depreciacionResumen.set("mes", mesId);
		depreciacionResumen.set("ciclo", cicloId);
		depreciacionResumen.set("monto", valorDepreciacion);
		depreciacionResumen.set("porcentajeDepreciacion", depreciacionMensual);
		depreciacionResumen.set("tipoActivoFijo", tipoActivoFijoId);
		depreciacionResumen.set("poliza", poliza);
		depreciacionResumen.set("acctgTransId", acctgTransId);
		depreciacionResumen.set("organizationPartyId", organizationPartyId);				
		delegator.create(depreciacionResumen);
				
	}
	
	public static void actualizaPolizaAcctgTrans(Delegator delegator, String poliza, String acctgTransId, String mesId, String cicloId) throws GenericEntityException			
	{	EntityCondition cond = EntityCondition.makeCondition(EntityOperator.AND,
				  			   EntityCondition.makeCondition("mes", EntityOperator.EQUALS, mesId),
				  			   EntityCondition.makeCondition("ciclo", EntityOperator.EQUALS, cicloId));
		List<GenericValue> items = delegator.findByCondition("DepreciacionResumen", cond, null, null);		
		if(!items.isEmpty())
		{	Iterator<GenericValue> depreciacionResumenIter = items.iterator();
		
			while (depreciacionResumenIter.hasNext())
			{	GenericValue depreciacionResumen = depreciacionResumenIter.next();
				depreciacionResumen.set("poliza", poliza);
				depreciacionResumen.set("acctgTransId", acctgTransId);
				delegator.store(depreciacionResumen);
			}
		}
	}
	
	
	
	
	
}
