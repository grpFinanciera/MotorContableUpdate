/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
/* This file has been modified by Open Source Strategies, Inc. */
package com.absoluciones.controlPatrimonial;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.common.util.UtilMessage;

/**
 *	Servicios de controlPatrimonial
 */

public class ControlPatrimonialServices {
		
	private static List<String> CUCOP = new ArrayList<String>();
	private static String MODULE = ControlPatrimonialServices.class.getName();
//	private static String ACTIVO = "ACT_FIJO_ACTIVO";
	private static String INACTIVO = "ACT_FIJO_INACTIVO";
	private static String COMODATO = "ACT_FIJO_COMODATO";
	private static String BAJA = "ACT_FIJO_BAJA";
//	private static String R_COMODATO = "ACT_FIJO_R_COMODATO";
//	private static String REASIGNAR = "ACT_FIJO_REASIGNAR";
	
	/**
	 * Crea un activo fijo
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> guardaActivoFijo(DispatchContext dctx, Map<?, ?> context) 
	{	Delegator delegator = dctx.getDelegator();
	
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Map<String, Object> input = FastMap.newInstance();
	    Map<String, Object> output = FastMap.newInstance();
	    Map<String, Object> montoMap = FastMap.newInstance();
	    Map<String, Object> clavePresupuestalMap = FastMap.newInstance();
	    Map<String, Object> catalogoCargoContMap = FastMap.newInstance();
	    Map<String, Object> catalogoAbonoContMap = FastMap.newInstance();
	    Map<String, Object> context2 = FastMap.newInstance();
	    String poliza = null;
	    GenericValue userLogin = (GenericValue) context.get("userLogin");
		try 
		{	if(context.get("resguardante") != null && !context.get("resguardante").equals(""))
			{	if((Timestamp) context.get("fechaInicioResguardo") == null)
				{	return ServiceUtil.returnError("Se ha ingresado un resguardante. Es necesario tambien ingresar la fecha de incio del resguardo");				
				}	
			}
			
			GenericValue fixedAsset = GenericValue.create(delegator.getModelEntity("FixedAsset"));
			Debug.log("context: " + context);
			String fixedAssetId = "";			
			fixedAssetId = obtenIdentificadorActivoFijo(delegator, context.get("productId").toString());						
			
			fixedAsset.set("fixedAssetId", fixedAssetId);
			fixedAsset.set("instanceOfProductId", context.get("productId"));
			fixedAsset.set("fixedAssetTypeId", context.get("fixedAssetTypeId"));
			fixedAsset.set("partyId", context.get("resguardante"));
			fixedAsset.set("fixedAssetName", context.get("nombreActivo"));
			fixedAsset.set("dateAcquired", (Timestamp) context.get("fechaAdquisicion"));			
			fixedAsset.set("expectedEndOfLife", context.get("fixedAssetTypeId"));				
			fixedAsset.set("serialNumber", context.get("numeroSerie"));
			fixedAsset.set("purchaseCost", (BigDecimal) context.get("monto"));
			fixedAsset.set("ubicacionRapidaId", context.get("ubicacionRapidaId"));
			fixedAsset.set("marca", context.get("marca"));
			fixedAsset.set("modelo", context.get("modelo"));
			fixedAsset.set("numeroPoliza", context.get("numeroPoliza"));
			fixedAsset.set("moneda", context.get("moneda"));
			fixedAsset.set("fechaVencGarantia", (Timestamp) context.get("fechaVencimientoGarantia"));
			fixedAsset.set("edoFisicoId", context.get("edoFisicoId"));
			fixedAsset.set("numeroFactura", context.get("numeroFactura"));
			fixedAsset.set("proveedor", context.get("proveedorId"));
			fixedAsset.set("areaPartyId", context.get("partyId"));			
			fixedAsset.set("fechaIniPoliza", (Timestamp) context.get("fechaInicioPoliza"));
			fixedAsset.set("fechaFinPoliza", (Timestamp) context.get("fechaFinPoliza"));
			fixedAsset.set("observaciones", context.get("observaciones"));		
			fixedAsset.set("caracteristicas", context.get("caracteristicas"));
			fixedAsset.set("denominacionPartidaGen", context.get("partidaGenerica"));
			fixedAsset.set("idActivoAnterior", context.get("fixedAssetIdAnterior"));
			fixedAsset.set("aniosVidaUtil", context.get("aniosVidaUtil"));
			fixedAsset.set("organizationPartyId", context.get("organizationId"));
			fixedAsset.set("acquireOrderId", context.get("orderId"));
			fixedAsset.set("acquireOrderItemSeqId", context.get("orderItemId"));
			fixedAsset.set("dateLastServiced", (Timestamp) context.get("fechaUltimoServicio"));
			fixedAsset.set("dateNextService", (Timestamp) context.get("fechaSiguienteServicio"));
			fixedAsset.set("expectedEndOfLife", (Timestamp) context.get("fechaFinalVidaUtil"));
			fixedAsset.set("actualEndOfLife", (Timestamp) context.get("fechaRealVidaUtil"));
			fixedAsset.set("salvageValue", (BigDecimal) context.get("valorSalvamento"));
			fixedAsset.set("depreciation", (BigDecimal) context.get("depreciacion"));			
			fixedAsset.set("statusId", context.get("statusId"));			 
			
			//Bienes inmuebles
			fixedAsset.set("domicilio", context.get("domicilio"));
			fixedAsset.set("municipio", context.get("municipio"));
			fixedAsset.set("localidad", context.get("localidad"));
			fixedAsset.set("ejido", context.get("ejido"));
			fixedAsset.set("tipoTerreno", context.get("tipoTerreno"));
			fixedAsset.set("tipoDocumentoLegalPropiedad", context.get("tipoDocLegalPropiedad"));
			fixedAsset.set("descDocumentoLegalPropiedad", context.get("descDocLegalPropiedad"));
			fixedAsset.set("origenAdquisicion", context.get("origenAdquisicion"));
			fixedAsset.set("formaAdquisicion", context.get("formaAdquisicion"));
			fixedAsset.set("tipoEmisorTituloPropiedad", context.get("tipoEmisorTituloPropiedad"));
			fixedAsset.set("valorContruccion", context.get("valorContruccion"));
			fixedAsset.set("valorRazonable", context.get("valorRazonable"));
			fixedAsset.set("fechaAvaluo", (Timestamp) context.get("fechaAvaluo"));
			fixedAsset.set("fechaTituloPropiedad", (Timestamp) context.get("fechaTituloPropiedad"));
			fixedAsset.set("claveCatastral", context.get("claveCatastral"));
			fixedAsset.set("fechaInicioClaveCatastral", (Timestamp) context.get("fechaInicioClaveCatastral"));
			fixedAsset.set("vencimientoClaveCatastral", (Timestamp) context.get("fechaVenciClaveCatastral"));
			fixedAsset.set("superficieTerreno", (BigDecimal) context.get("superficieTerreno"));
			fixedAsset.set("superficieConstruccion", (BigDecimal) context.get("superficieConstruccion"));
			fixedAsset.set("superficieDisponible", (BigDecimal) context.get("superficieDisponible"));
			fixedAsset.set("folioRppc", context.get("folioRegPubliPropieComercio"));
			fixedAsset.set("fechaInscripcionRppc", (Timestamp) context.get("fechaInscRegPropieComer"));
			fixedAsset.set("ciudadInscripcionRPPC", context.get("ciudadInscRegPubPropComer"));
			fixedAsset.set("fechaIncorporacionInventario", (Timestamp) context.get("fechaIncorInventario"));
			
			//Bienes inmuebles
			fixedAsset.set("placa", context.get("placa"));
			fixedAsset.set("tipoUnidadVehiculoId", context.get("tipoUnidadVehiculoId"));
			
			//Almacen
			fixedAsset.set("facilityId", context.get("facilityId"));								
			delegator.create(fixedAsset);
			
			if(context.get("resguardante") != null && !context.get("resguardante").equals(""))
			{	GenericValue partyFixedAssetAssignment = GenericValue.create(delegator.getModelEntity("PartyFixedAssetAssignment"));				
				partyFixedAssetAssignment.set("partyId", context.get("resguardante"));
				partyFixedAssetAssignment.set("roleTypeId", "EMPLEADO");
				partyFixedAssetAssignment.set("fixedAssetId", fixedAssetId);   
				partyFixedAssetAssignment.set("fromDate", (Timestamp) context.get("fechaInicioResguardo"));
				partyFixedAssetAssignment.set("thruDate", (Timestamp) context.get("fechaFinResguardo"));
				partyFixedAssetAssignment.set("allocatedDate", (Timestamp) context.get("fechaAsignacion"));
				partyFixedAssetAssignment.set("statusId", "ASIGN_ASIGNADO");
				partyFixedAssetAssignment.set("comments", context.get("comentariosAsignacion"));				
				delegator.create(partyFixedAssetAssignment);
			}
			EntityCondition condicionEvento = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("motivo", EntityOperator.EQUALS, context.get("idMotivo")),
					EntityCondition.makeCondition("tipoActivoFijo", EntityOperator.EQUALS, context.get("fixedAssetTypeId")));
			
			List<GenericValue> motivosActivo = delegator.findByCondition("MotivosActivoFijo",condicionEvento,null,null);
			
			if(UtilValidate.isEmpty(motivosActivo)){
				return ServiceUtil.returnError("No existe un evento configurado para esta combinacion: Motivo " + context.get("idMotivo") + " y el tipo de activo " + context.get("fixedAssetTypeId"));
			}
			
			List<String> orderByLinea = UtilMisc.toList("secuencia");
	    	EntityCondition condicionLinea = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, motivosActivo.get(0).getString("evento")));                    

			List<GenericValue> lineaList = delegator.findByCondition("LineaContable", condicionLinea, null, orderByLinea);
			Debug.logInfo("condicion: " + condicionLinea,MODULE);
			Debug.logInfo("lineaList: " + lineaList,MODULE);
			if(!lineaList.isEmpty())
			{	Iterator<GenericValue> listaIter = lineaList.iterator();	        
				while (listaIter.hasNext())
				{	GenericValue item = listaIter.next();
					String descripcionLinea = item.getString("descripcion");
					descripcionLinea = descripcionLinea.replaceAll(" ", "");
					Debug.logInfo("descripcionLinea: " + descripcionLinea,MODULE);						
		        	montoMap.put(descripcionLinea+"0", context.get("monto").toString());
		        	catalogoCargoContMap.put(descripcionLinea+"0", "");
		        	catalogoAbonoContMap.put(descripcionLinea+"0", "");
				}
			}        					
        	clavePresupuestalMap.put("0", "");	        	
        		        	
            context2.put("montoMap", montoMap);        
            context2.put("clavePresupuestalMap", clavePresupuestalMap);
            context2.put("catalogoCargoContMap", catalogoCargoContMap);
            context2.put("catalogoAbonoContMap", catalogoAbonoContMap);
            
            Debug.log("montoMap: " + montoMap);
            Debug.log("clavePresupuestalMap: " + clavePresupuestalMap);
            Debug.log("catalogoCargoContMap: " + catalogoCargoContMap);
            Debug.log("catalogoAbonoContMap: " + catalogoAbonoContMap);
			
			//Invocacion a motor contable
        	/*****MOTOR CONTABLE*****/

            input.put("eventoContableId", motivosActivo.get(0).getString("evento"));
        	input.put("tipoClaveId", "EGRESO");
        	input.put("fechaRegistro", UtilDateTime.nowTimestamp());
        	input.put("fechaContable", context.get("fechaContable"));
        	input.put("currency", context.get("moneda"));
        	input.put("usuario", userLogin.getString("userLoginId"));
        	input.put("organizationId", context.get("organizationId"));
        	input.put("descripcion", "Alta de "+ context.get("nombreActivo"));
        	input.put("roleTypeId", "BILL_FROM_VENDOR");
        	input.put("valorCampo", fixedAssetId);
        	input.put("campo", "fixedAssetId");      
        	input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(motivosActivo.get(0).getString("evento"), delegator, context2, null, null, null, null));
        	
        	
        	//Genera registro en DepreciacionResumen
        	output = dispatcher.runSync("creaTransaccionMotor", input);
        	Debug.log("output: " + output);
        	if(ServiceUtil.isError(output)){
        		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
        	}
        	
        	GenericValue transaccion = (GenericValue) output.get("transaccion");
        	poliza = transaccion.getString("poliza");

			Map<String, Object> result = ServiceUtil.returnSuccess("El activo fijo ha sido creado con \u00e9xito con la poliza " +poliza);		

			result.put("fixedAssetId", fixedAssetId);
			return result;
		} 
		catch (GenericEntityException | GenericServiceException e) 
		{	return ServiceUtil.returnError("Error al crear el activo fijo: " + e.getMessage());
		}
	}
	
	/**
	 * Metodo para crear el nuevo identificador de un activo 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static String obtenIdentificadorActivoFijo(Delegator delegator, String cucop) throws GenericEntityException  	
	{	String idActivoFijo = "";
		long contadorCucop = 0;		
		
		EntityCondition condicion = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("instanceOfProductId", EntityOperator.EQUALS, cucop));
		contadorCucop = delegator.findCountByCondition("FixedAsset", condicion, null, null);
		
		idActivoFijo = cucop.concat(UtilFormatOut.formatPaddedNumber((contadorCucop + 1), 6));
		CUCOP.add(cucop);
		
		return idActivoFijo;
	}
		
	/**
	 * Editar un activo fijo
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> editarActivoFijo(DispatchContext dctx, Map<?, ?> context) 
	{	Delegator delegator = dctx.getDelegator();
		Debug.log("context: " + context);
		try 
		{	String fixedAssetId = (String) context.get("fixedAssetId");
			GenericValue fixedAsset = delegator.findByPrimaryKey("FixedAsset", UtilMisc.toMap("fixedAssetId", fixedAssetId));
			fixedAsset.set("instanceOfProductId", context.get("productId"));
			fixedAsset.set("fixedAssetTypeId", context.get("fixedAssetTypeId"));
			fixedAsset.set("partyId", context.get("resguardante"));
			fixedAsset.set("fixedAssetName", context.get("nombreActivo"));
			fixedAsset.set("dateAcquired", (Timestamp) context.get("fechaAdquisicion"));			
			fixedAsset.set("expectedEndOfLife", context.get("fixedAssetTypeId"));				
			fixedAsset.set("serialNumber", context.get("numeroSerie"));
			fixedAsset.set("purchaseCost", (BigDecimal) context.get("monto"));
			fixedAsset.set("ubicacionRapidaId", context.get("ubicacionRapidaId"));
			fixedAsset.set("marca", context.get("marca"));
			fixedAsset.set("modelo", context.get("modelo"));
			fixedAsset.set("numeroPoliza", context.get("numeroPoliza"));
			fixedAsset.set("moneda", context.get("moneda"));
			fixedAsset.set("fechaVencGarantia", (Timestamp) context.get("fechaVencimientoGarantia"));
			fixedAsset.set("edoFisicoId", context.get("edoFisicoId"));
			fixedAsset.set("numeroFactura", context.get("numeroFactura"));
			fixedAsset.set("proveedor", context.get("proveedorId"));
			fixedAsset.set("areaPartyId", context.get("partyId"));			
			fixedAsset.set("fechaIniPoliza", (Timestamp) context.get("fechaInicioPoliza"));
			fixedAsset.set("fechaFinPoliza", (Timestamp) context.get("fechaFinPoliza"));
			fixedAsset.set("observaciones", context.get("observaciones"));		
			fixedAsset.set("caracteristicas", context.get("caracteristicas"));
			fixedAsset.set("denominacionPartidaGen", context.get("partidaGenerica"));
			fixedAsset.set("idActivoAnterior", context.get("fixedAssetIdAnterior"));
			fixedAsset.set("aniosVidaUtil", context.get("aniosVidaUtil"));
			fixedAsset.set("organizationPartyId", context.get("organizationId"));
			fixedAsset.set("acquireOrderId", context.get("orderId"));
			fixedAsset.set("acquireOrderItemSeqId", context.get("orderItemId"));
			fixedAsset.set("dateLastServiced", (Timestamp) context.get("fechaUltimoServicio"));
			fixedAsset.set("dateNextService", (Timestamp) context.get("fechaSiguienteServicio"));
			fixedAsset.set("expectedEndOfLife", (Timestamp) context.get("fechaFinalVidaUtil"));
			fixedAsset.set("actualEndOfLife", (Timestamp) context.get("fechaRealVidaUtil"));
			fixedAsset.set("salvageValue", (BigDecimal) context.get("valorSalvamento"));
			fixedAsset.set("depreciation", (BigDecimal) context.get("depreciacion"));			
			fixedAsset.set("statusId", context.get("statusId"));			 
			
			//Bienes inmuebles
			fixedAsset.set("domicilio", context.get("domicilio"));
			fixedAsset.set("municipio", context.get("municipio"));
			fixedAsset.set("localidad", context.get("localidad"));
			fixedAsset.set("ejido", context.get("ejido"));
			fixedAsset.set("tipoTerreno", context.get("tipoTerreno"));
			fixedAsset.set("tipoDocumentoLegalPropiedad", context.get("tipoDocLegalPropiedad"));
			fixedAsset.set("descDocumentoLegalPropiedad", context.get("descDocLegalPropiedad"));
			fixedAsset.set("origenAdquisicion", context.get("origenAdquisicion"));
			fixedAsset.set("formaAdquisicion", context.get("formaAdquisicion"));
			fixedAsset.set("tipoEmisorTituloPropiedad", context.get("tipoEmisorTituloPropiedad"));
			fixedAsset.set("valorContruccion", context.get("valorContruccion"));
			fixedAsset.set("valorRazonable", context.get("valorRazonable"));
			fixedAsset.set("fechaAvaluo", (Timestamp) context.get("fechaAvaluo"));
			fixedAsset.set("fechaTituloPropiedad", (Timestamp) context.get("fechaTituloPropiedad"));
			fixedAsset.set("claveCatastral", context.get("claveCatastral"));
			fixedAsset.set("fechaInicioClaveCatastral", (Timestamp) context.get("fechaInicioClaveCatastral"));
			fixedAsset.set("vencimientoClaveCatastral", (Timestamp) context.get("fechaVenciClaveCatastral"));
			fixedAsset.set("superficieTerreno", (BigDecimal) context.get("superficieTerreno"));
			fixedAsset.set("superficieConstruccion", (BigDecimal) context.get("superficieConstruccion"));
			fixedAsset.set("superficieDisponible", (BigDecimal) context.get("superficieDisponible"));
			fixedAsset.set("folioRppc", context.get("folioRegPubliPropieComercio"));
			fixedAsset.set("fechaInscripcionRppc", (Timestamp) context.get("fechaInscRegPropieComer"));
			fixedAsset.set("ciudadInscripcionRPPC", context.get("ciudadInscRegPubPropComer"));
			fixedAsset.set("fechaIncorporacionInventario", (Timestamp) context.get("fechaIncorInventario"));
			
			//Bienes inmuebles
			fixedAsset.set("placa", context.get("placa"));
			fixedAsset.set("tipoUnidadVehiculoId", context.get("tipoUnidadVehiculoId"));
			
			//Almacen
			fixedAsset.set("facilityId", context.get("facilityId"));	
			
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();
			searchConditions.add(EntityCondition.makeCondition("fixedAssetId", EntityOperator.EQUALS, fixedAssetId));
			searchConditions.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ASIGN_ASIGNADO"));
			
			EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
			
			List<GenericValue> resguardante = delegator.findByCondition("PartyFixedAssetAssignment", condiciones,null,UtilMisc.toList("fromDate DESC"));
			String resguarda = (String) context.get("resguardante");
			String resguarda2 = resguardante.get(0).getString("partyId");
			if(!resguarda.equals(resguarda2)){
				//Resguardante
				GenericValue partyFixedAssetAssignment = delegator.findByPrimaryKey("PartyFixedAssetAssignment", UtilMisc.toMap("fixedAssetId", fixedAssetId, "partyId",
						resguardante.get(0).getString("partyId"), "roleTypeId", "EMPLEADO", "fromDate", resguardante.get(0).getTimestamp("fromDate")));
				
				partyFixedAssetAssignment.set("thruDate", (Timestamp) context.get("fechaInicioResguardo"));
				partyFixedAssetAssignment.set("statusId", "ASIGN_LIBERADO");
				
				GenericValue partyFixedAssetAssignment2 = GenericValue.create(delegator.getModelEntity("PartyFixedAssetAssignment"));				
				partyFixedAssetAssignment2.set("partyId", context.get("resguardante"));
				partyFixedAssetAssignment2.set("roleTypeId", "EMPLEADO");
				partyFixedAssetAssignment2.set("fixedAssetId", fixedAssetId);   
				partyFixedAssetAssignment2.set("fromDate", (Timestamp) context.get("fechaInicioResguardo"));
				partyFixedAssetAssignment2.set("thruDate", (Timestamp) context.get("fechaFinResguardo"));
				partyFixedAssetAssignment2.set("allocatedDate", (Timestamp) context.get("fechaAsignacion"));
				partyFixedAssetAssignment2.set("statusId", "ASIGN_ASIGNADO");
				partyFixedAssetAssignment2.set("comments", context.get("comentariosAsignacion"));				
				delegator.create(partyFixedAssetAssignment2);
				
				delegator.store(partyFixedAssetAssignment);
			}
			delegator.store(fixedAsset);

			Map<String, Object> result = ServiceUtil.returnSuccess("El activo fijo ha sido editado con \u00e9xito");			
			result.put("fixedAssetId", fixedAssetId);
			return result;
		} 
		catch (GenericEntityException e) 
		{	return ServiceUtil.returnError("Error al editar el activo fijo: " + e.getMessage());
		}
	}
	
	/**
	 * Borra los registros del levantamiento de inventario de activo fijo
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static String borrarLevantamiento(HttpServletRequest request, HttpServletResponse response){
		final Delegator delegator = (Delegator)request.getAttribute("delegator");
		
		try {
			delegator.removeAll("LevantamientoActivo");
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return "error";
		}
		ServiceUtil.setMessages(request,null , "Se limpio el levantamiento para inicar", null);
		return "success";
	}
	
	/**
	 * Realiza la depreciacion de una categoria de activo fijo en un periodo dado
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String,?> procesoDepreciacion(DispatchContext dctx, Map<String,Object> context) throws GenericServiceException, GenericEntityException{
		
		Delegator delegator = dctx.getDelegator();
		
		LocalDispatcher dispatcher = dctx.getDispatcher();
		 
		String tipoActivoFijoId = (String) context.get("fixedAssetTypeId");
		String mesId = (String) context.get("mesId");
		String cicloId = (String) context.get("enumCode");
		String organizationPartyId = (String) context.get("organizationPartyId");
		int mesInt = Integer.parseInt(mesId);
		int cicloInt = Integer.parseInt(cicloId);
		GenericValue userLogin = (GenericValue) context.get("userLogin");		
		String acctgTransTypeId = obtenerEventoContable(delegator, tipoActivoFijoId);
		if(acctgTransTypeId == null || acctgTransTypeId.equals(""))
		{	
			return ServiceUtil.returnError("No existe un evento contable configurado para el tipo de bien ["+tipoActivoFijoId+"]");			
		}		
		String comentario = (String) context.get("comentario");
		Timestamp fechaTrans = new Timestamp(Calendar.getInstance().getTimeInMillis());
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		
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
		
		try
		{	
			
			//Valida que no exista un periodo despues del periodo seleccionado 
			List<String> orderBy = UtilMisc.toList("idDepreciacionPeriodo");
			EntityCondition condicionesPeriodo = EntityCondition.makeCondition(EntityOperator.AND,										  
										  EntityCondition.makeCondition("tipoActivoFijo", EntityOperator.EQUALS, tipoActivoFijoId));
			List<GenericValue> periodoAnterior = delegator.findByCondition("DepreciacionPeriodo", condicionesPeriodo, null, orderBy);
			Debug.logInfo("Omar-periodoAnterior: " + periodoAnterior,MODULE);						
			
			if(!periodoAnterior.isEmpty())			
			{	GenericValue compara = periodoAnterior.get(0);
				String mesCompara = compara.getString("mes");
				String cicloCompara = compara.getString("ciclo");
				if(cicloInt < Integer.parseInt(cicloCompara))
				{	
					return ServiceUtil.returnError("El periodo seleccionado es anterior al primer periodo depreciado en el sistema");							
				}
				else if(cicloInt == Integer.parseInt(cicloCompara))
				{
					if(mesInt < Integer.parseInt(mesCompara))
					{
						return ServiceUtil.returnError("El periodo seleccionado es anterior al primer periodo depreciado en el sistema");							
					}
					else if(mesInt == Integer.parseInt(mesCompara))
					{
						return ServiceUtil.returnError("El periodo seleccionado ya ha sido depreciado anteriormente");							
					}
				}
			}
							
			//Se obtienen los activos fijos y datos necesarios para realizar la depreciacion
			EntityCondition condTipoActivo = EntityCondition.makeCondition(EntityOperator.AND,
					  EntityCondition.makeCondition("fixedAssetTypeId", EntityOperator.EQUALS, tipoActivoFijoId),
					  EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, INACTIVO),
					  EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, BAJA),
					  EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, COMODATO));
			List<GenericValue> itemsActivoFijo = delegator.findByCondition("ItemsPorcentajeActivoFijo", condTipoActivo, null, null);
			if(!itemsActivoFijo.isEmpty())
			{
				for (GenericValue ActivoFijo : itemsActivoFijo) {
					
					Timestamp fechaAdquisicion = (Timestamp)ActivoFijo.getTimestamp("dateAcquired");
					BigDecimal precioCompra = ActivoFijo.getBigDecimal("purchaseCost");
					BigDecimal porcentajeDecimal = ActivoFijo.getBigDecimal("montoDepreciacion");
					BigDecimal aniosVidaUtil = ActivoFijo.getBigDecimal("aniosVidaUtil");
					moneda = ActivoFijo.getString("moneda");
					String fixedAssetId = ActivoFijo.getString("fixedAssetId");
					
					if(fechaAdquisicion != null){
					
						/* Operacion de depreciacion
						 * Revisar si se va a tener un valor historico del activo (valuar actual depreciado)
						 * o se va a calcular desde su fecha de adquisicion*/
						
						
						BigDecimal meses = ((BigDecimal.valueOf(100).divide(porcentajeDecimal, 6, RoundingMode.HALF_UP))).multiply(BigDecimal.valueOf(12));
						Debug.logInfo("Omar-meses: " + meses,MODULE);
						
						Calendar finalDepre = Calendar.getInstance();
						Calendar calAdquisicion = Calendar.getInstance();
						calAdquisicion.setTime(fechaAdquisicion);
						
						finalDepre.set(Calendar.DATE, calAdquisicion.get(Calendar.DATE));
						finalDepre.set(Calendar.MONTH, calAdquisicion.get(Calendar.MONTH));
						finalDepre.set(Calendar.YEAR, calAdquisicion.get(Calendar.YEAR));
						
						
						//Sumamos meses de la operacion para obtener fecha final de depreciacion
						finalDepre.add(Calendar.MONTH, Integer.valueOf(meses.intValue())); //Sumamos los meses del resultado
						
						/*Comparar la fecha 
						Fecha FormateadafechaDesdeCal: 01/09/2014
						Fecha FormateadafechaHastaCal: 01/10/2014
						*/
																		
						if(finalDepre.after(fechaHastaCal))
						{	
							if(fechaAdquisicion.before(fechaDesdeCal.getTime()))
							{	
								depreciacionMensual = porcentajeDecimal.divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);
								valorDepreciacion = BigDecimal.ZERO;
								valorDepreciacion = precioCompra.divide(aniosVidaUtil.multiply(BigDecimal.valueOf(12)),2,RoundingMode.HALF_UP);
								//valorDepreciacion = ((precioCompra.multiply(depreciacionMensual)).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
								
								GenericValue activo = delegator.findByPrimaryKey("FixedAsset", UtilMisc.toMap("fixedAssetId", fixedAssetId));
								
								BigDecimal depreActivo = activo.getBigDecimal("depreciation");
								
								BigDecimal valorDepreciacion2 = valorDepreciacion;
								
								if(UtilValidate.isEmpty(depreActivo)){
									activo.set("depreciation", valorDepreciacion2.add(BigDecimal.ZERO));
								}else{
									activo.set("depreciation", valorDepreciacion2.add(depreActivo));
								}
								delegator.store(activo);
								
								totalValorDepreciacion = totalValorDepreciacion.add(valorDepreciacion);
								registraDepreciacionResumen(delegator, fixedAssetId, poliza, mesId, cicloId, valorDepreciacion, depreciacionMensual, tipoActivoFijoId, organizationPartyId, acctgTransId);
								existenActivos=true;
							}
						}	
					}
				}
			}
			else
			{	
				return ServiceUtil.returnError("No existen Activos registrados con el Tipo de activo fijo: ["+ tipoActivoFijoId+ "]");				
			}
			
			
            if(existenActivos)
            {
            	List<String> orderByLinea = UtilMisc.toList("secuencia");
		    	EntityCondition condicionLinea = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, acctgTransTypeId));                    
				List<GenericValue> lineaList = delegator.findByCondition("LineaContable", condicionLinea, null, orderByLinea);
				Debug.logInfo("condicion: " + condicionLinea,MODULE);
				Debug.logInfo("lineaList: " + lineaList,MODULE);
				if(!lineaList.isEmpty())
				{	Iterator<GenericValue> listaIter = lineaList.iterator();	        
					while (listaIter.hasNext())
					{	GenericValue item = listaIter.next();
						String descripcionLinea = item.getString("descripcion");
						descripcionLinea = descripcionLinea.replaceAll(" ", "");
						Debug.logInfo("descripcionLinea: " + descripcionLinea,MODULE);						
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
            	
            	Debug.logInfo("itemsActivoFijo: " + itemsActivoFijo,MODULE);
            	
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
		
		Map<String, Object> result = FastMap.newInstance();
		result.put("poliza", poliza);		
		
		if(existenActivos){
			result = ServiceUtil.returnSuccess("Se creo correctamente la p\u00f3liza de depreciaci\u00f3n : "+poliza);
		} else {
			result = ServiceUtil.returnError("No se realizo ninguna operaci\u00f3n de depreciaci\u00f3n ");
		}
		
		return result;		
	}
	
	public static void registraDepreciacionResumen(Delegator delegator, String fixedAssetId, String poliza, String mesId, String cicloId, 
			BigDecimal valorDepreciacion, BigDecimal depreciacionMensual, String tipoActivoFijoId, String organizationPartyId, String acctgTransId) throws GenericEntityException			
	{	Debug.log("Entra a registraDepreciacionResumen");								
		//Genera registro por cada activo y su monto de depreciacion
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
		Debug.log("Omar - depreciacionResumen: " + depreciacionResumen);				
		delegator.create(depreciacionResumen);
				
	}
	
	public static void actualizaPolizaAcctgTrans(Delegator delegator, String poliza, String acctgTransId, String mesId, String cicloId) throws GenericEntityException			
	{	EntityCondition cond = EntityCondition.makeCondition(EntityOperator.AND,
				  			   EntityCondition.makeCondition("mes", EntityOperator.EQUALS, mesId),
				  			   EntityCondition.makeCondition("ciclo", EntityOperator.EQUALS, cicloId));
		List<GenericValue> items = delegator.findByCondition("DepreciacionResumen", cond, null, null);
		Debug.log("items: " + items);
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
	
	
	/**
	 * Crea un nuevo registro de resguardo de activo fijo
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> verResguardoActivoFijo(DispatchContext dctx, Map<?, ?> context) 
	{	Delegator delegator = dctx.getDelegator();
		String fixedAssetId = (String) context.get("fixedAssetId");
		String partyId = (String) context.get("partyId");
		try 
		{	
			GenericValue fixedAsset = delegator.findByPrimaryKey("FixedAsset", UtilMisc.toMap("fixedAssetId", fixedAssetId));	
			String estatus = fixedAsset.getString("statusId");
			if(estatus.equals("ACT_FIJO_BAJA")){
				return ServiceUtil.returnError("No se puede reasignar un activo con estatus Baja");
			}
			
			if(context.get("partyId") != null && !context.get("partyId").equals(""))
			{	if((Timestamp) context.get("fechaDesde") == null)
				{	return ServiceUtil.returnError("Se ha ingresado un resguardante. Es necesario tambien ingresar la fecha de incio del resguardo");				
				}	
			}												
			
			/*EntityCondition condicion = EntityCondition.makeCondition(EntityOperator.AND,
										EntityCondition.makeCondition("fixedAssetId", EntityOperator.EQUALS, fixedAssetId),
										EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId));
			long contadorCucop = delegator.findCountByCondition("PartyFixedAssetAssignment", condicion, null, null);
			
			if(contadorCucop > 0)
			{	return ServiceUtil.returnError("El activo fijo seleccionado ya ha sido asignado previamente a la persona: " + partyId);				
			}	*/			
			
			//Libera los que esten asignados
			List<String> orderBy = UtilMisc.toList("fixedAssetId");
			EntityCondition condicionActual = EntityCondition.makeCondition(EntityOperator.AND,										  
											  EntityCondition.makeCondition("fixedAssetId", EntityOperator.EQUALS, fixedAssetId),
											  EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ASIGN_ASIGNADO"));
			List<GenericValue> resguardoActualList = delegator.findByCondition("PartyFixedAssetAssignment", condicionActual, null, orderBy);
			Debug.log("resguardoActualList: " + resguardoActualList);
			//Actualizar status a sin asignar
			if(!resguardoActualList.isEmpty())
			{	Iterator<GenericValue> resguardoActualIter = resguardoActualList.iterator();
				while (resguardoActualIter.hasNext())
				{	GenericValue generic = (GenericValue) resguardoActualIter.next();
					generic.set("statusId", "ASIGN_LIBERADO");
					delegator.store(generic);
				}
			}
		
			GenericValue partyFixedAssetAssignment = GenericValue.create(delegator.getModelEntity("PartyFixedAssetAssignment"));				
			partyFixedAssetAssignment.set("partyId", partyId);
			partyFixedAssetAssignment.set("roleTypeId", "EMPLEADO");
			partyFixedAssetAssignment.set("fixedAssetId", fixedAssetId);   
			partyFixedAssetAssignment.set("fromDate", (Timestamp) context.get("fechaDesde"));
			partyFixedAssetAssignment.set("thruDate", (Timestamp) context.get("fechaHasta"));
			partyFixedAssetAssignment.set("allocatedDate", (Timestamp) context.get("fechaAsignacion"));
			partyFixedAssetAssignment.set("statusId", context.get("statusId"));
			partyFixedAssetAssignment.set("comments", context.get("comentario"));				
			delegator.create(partyFixedAssetAssignment);
			
						
			fixedAsset.set("partyId", partyId);						
			delegator.store(fixedAsset);

			Map<String, Object> result = ServiceUtil.returnSuccess("La asignacion de resguardante ha sido creada con \u00e9xito");			
			result.put("fixedAssetId", fixedAssetId);
			return result;
		} 
		catch (GenericEntityException e) 
		{	return ServiceUtil.returnError("Error al asignar resguardante: " + e.getMessage());
		}
	}
	
	/**
	 * Metodo para recargar la pagina
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> cargarPaginaActivo(DispatchContext dctx,
			Map<?, ?> context) {	
		
		String fixedAssetId = (String) context.get("fixedAssetId");
		Debug.log("fixedAssetId: " + fixedAssetId);
		
		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("fixedAssetId", fixedAssetId);
		return result;
	}
	
	/**
	 * Metodo para actualizar masivamente las polzias de seguro de activo fijo
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> actualizaPolizaSeguro(DispatchContext dctx, Map<?, ?> context) throws GenericEntityException 
	{	
		String numPolizaAnt = (String) context.get("numPolizaAnt");
		String numPolizaNuevo = (String) context.get("numPolizaNuevo");
		Timestamp fechaDesdeNueva = (Timestamp) context.get("fechaDesdeNueva");
		Timestamp fechaHastaNueva = (Timestamp) context.get("fechaHastaNueva");
		Timestamp fechaCambio = new Timestamp(Calendar.getInstance().getTimeInMillis());
		String comentario = (String) context.get("comentario");
		Locale locale = (Locale) context.get("locale");
		
		try 
		{	Delegator delegator = dctx.getDelegator();		
			List<GenericValue> activos = delegator.findByAnd("FixedAsset",UtilMisc.toMap("numeroPoliza", numPolizaAnt));
			if(activos.size() == 0)
			{	return ServiceUtil.returnError("No existe alg\u00fan art\u00edculo registrado con la p\u00f3liza de seguro registrada");				
			}				
			for (GenericValue activo : activos) 
			{	activo.set("numeroPoliza", numPolizaNuevo);
				activo.set("fechaIniPoliza", fechaDesdeNueva);
				activo.set("fechaFinPoliza", fechaHastaNueva);
			}			
			delegator.storeAll(activos);
			GenericValue fixedAssetPolizaSeguro = GenericValue.create(delegator.getModelEntity("FixedAssetPolizaSeguro"));				
			fixedAssetPolizaSeguro.set("numPolizaAnt", numPolizaAnt);
			fixedAssetPolizaSeguro.set("numPolizaNuevo", numPolizaNuevo);
			fixedAssetPolizaSeguro.set("fechaDesdeNueva", fechaDesdeNueva);   
			fixedAssetPolizaSeguro.set("fechaHastaNueva", fechaHastaNueva);
			fixedAssetPolizaSeguro.set("fechaCambio", fechaCambio);
			fixedAssetPolizaSeguro.set("comentario", comentario);						
			delegator.create(fixedAssetPolizaSeguro);
			
			String mensaje = UtilMessage.expandLabel("ActualizaPolizaMssg", locale, UtilMisc.toMap("numeroAnt",numPolizaAnt,"numeroNuevo",numPolizaNuevo));		
			return ServiceUtil.returnSuccess(mensaje);
		} 
		catch (GenericEntityException e) 
		{	return ServiceUtil.returnError("Ocurri\u00f3 un error al actualizar la p\u00f3liza de seguro: " + e);
		}				
		
	}
	
	/**
	 * Metodo para asignar un permiso a un usuario sobre un almacen de activo fijo
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> asignarPermisoAlmacen(DispatchContext dctx, Map<?, ?> context) throws GenericEntityException 
	{	
		String partyId = (String) context.get("partyId");
		String facilityId = (String) context.get("facilityId");		
		
		try 
		{	Delegator delegator = dctx.getDelegator();		
			GenericValue fixedAssetPersonaAlmacen = GenericValue.create(delegator.getModelEntity("FixedAssetPersonaAlmacen"));				
			fixedAssetPersonaAlmacen.set("partyId", partyId);
			fixedAssetPersonaAlmacen.set("facilityId", facilityId);								
			delegator.create(fixedAssetPersonaAlmacen);
			
			Map<String, Object> result = ServiceUtil.returnSuccess("Se ha realizado la asignaci\u00f3n del almacen con \u00e9xito");			
			return result;
		} 
		catch (GenericEntityException e) 
		{	return ServiceUtil.returnError("Ocurri\u00f3 un error al realizar la asignaci\u00f3n del almacen: " + e);
		}				
		
	}
	
	/**
	 * Metodo para asignar un responsable de las recepciones en el almacen de activo fijo
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> asignarResponsablePermisoAlmacen(DispatchContext dctx, Map<?, ?> context) throws GenericEntityException 
	{	
		String partyId = (String) context.get("partyId");
		String facilityId = (String) context.get("facilityId");		
		
		try 
		{	Delegator delegator = dctx.getDelegator();		
			GenericValue responsableActivoFijo = GenericValue.create(delegator.getModelEntity("ResponsableActivoFijo"));				
			responsableActivoFijo.set("partyId", partyId);
			responsableActivoFijo.set("facilityId", facilityId);								
			delegator.create(responsableActivoFijo);
			
			Map<String, Object> result = ServiceUtil.returnSuccess("Se ha realizado la asignaci\u00f3n del responsable del almacen con \u00e9xito");			
			return result;
		} 
		catch (GenericEntityException e) 
		{	return ServiceUtil.returnError("Ocurri\u00f3 un error al realizar la asignaci\u00f3n del responsable del almacen: " + e);
		}				
		
	}
	
	
		
	/**
	 * Metodo para actualizar masivamente los resguardos de una persona a otra
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> actualizarResguardos(DispatchContext dctx, Map<?, ?> context) throws GenericEntityException 
	{	
		Delegator delegator = dctx.getDelegator();		
		Map<String, Object> fixedAssetId = UtilGenerics.toMap(context.get("fixedAssetId"));
		Map<String, Object> excepcion = UtilGenerics.toMap(context.get("excepcion"));
		String nuevoResguardante = (String) context.get("nuevoResguardante");
		String resguardante = (String) context.get("resguardante");		
		Timestamp fechaInicioResguardo = (Timestamp) context.get("fechaInicioResguardo");
		Timestamp fechaFinResguardo = (Timestamp) context.get("fechaFinResguardo");		
		Timestamp fechaAsignacion = (Timestamp) context.get("fechaAsignacion");
		String comentariosAsignacion = (String) context.get("comentariosAsignacion");
		
		try 
		{	for(int i=0; i<fixedAssetId.size(); i++)		
			{	if(excepcion.get(Integer.toString(i+1)) != null && excepcion.get(Integer.toString(i+1)).equals("Y"))
				{	//Consulta los resguardos actuales 
					List<String> orderBy = UtilMisc.toList("fixedAssetId");
					EntityCondition condicionActual = EntityCondition.makeCondition(EntityOperator.AND,										  
													  EntityCondition.makeCondition("fixedAssetId", EntityOperator.EQUALS, fixedAssetId.get(Integer.toString(i+1))),
													  EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, resguardante));
					List<GenericValue> resguardoActualList = delegator.findByCondition("PartyFixedAssetAssignment", condicionActual, null, orderBy);
					Debug.log("resguardoActualList: " + resguardoActualList);
					//Actualizar status a sin asignar
					if(!resguardoActualList.isEmpty())
					{	Iterator<GenericValue> resguardoActualIter = resguardoActualList.iterator();
						while (resguardoActualIter.hasNext())
						{	GenericValue generic = (GenericValue) resguardoActualIter.next();
							generic.set("statusId", "ASIGN_LIBERADO");
							delegator.store(generic);
						}
					}
					//Crear nuevo registro para el nuevo resgurdante
					GenericValue partyFixedAssetAssignment = GenericValue.create(delegator.getModelEntity("PartyFixedAssetAssignment"));				
					partyFixedAssetAssignment.set("partyId", nuevoResguardante);
					partyFixedAssetAssignment.set("roleTypeId", "EMPLEADO");
					partyFixedAssetAssignment.set("fixedAssetId", fixedAssetId.get(Integer.toString(i+1)));   
					partyFixedAssetAssignment.set("fromDate", fechaInicioResguardo);
					partyFixedAssetAssignment.set("thruDate", fechaFinResguardo);
					partyFixedAssetAssignment.set("allocatedDate", fechaAsignacion);
					partyFixedAssetAssignment.set("statusId", "ASIGN_ASIGNADO");
					partyFixedAssetAssignment.set("comments", comentariosAsignacion);
					delegator.create(partyFixedAssetAssignment);
					//Se actualiza el partyId en FixedAsset
					GenericValue fixedAsset = delegator.findByPrimaryKey("FixedAsset", UtilMisc.toMap("fixedAssetId", fixedAssetId.get(Integer.toString(i+1))));
					fixedAsset.set("partyId", nuevoResguardante);						
					delegator.store(fixedAsset);									
				}					
			}			
			Map<String, Object> result = ServiceUtil.returnSuccess("El resguardo ha sido actualizado con \u00e9xito");			
			return result;
		} 
		catch (GenericEntityException e) 
		{	return ServiceUtil.returnError("Error al actualizar los resguardos: " + e.getMessage());
		}												
	}
	
	/**
	 * Metodo para obtener el evento contable de acuerdo al tipo de activo fijo para depreciacion 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static String obtenerEventoContable(Delegator delegator, String tipoActivoFijoId) throws GenericEntityException  	
	{	String acctgTransTypeId = "";
		EntityCondition condition = EntityCondition.makeCondition(EntityOperator.AND, 
									EntityCondition.makeCondition("fixedAssetTypeId", EntityOperator.EQUALS, tipoActivoFijoId));			

		List<GenericValue> lista = delegator.findByCondition("FixedAssetType", condition, null, null);		
		if (!lista.isEmpty()) 
		{	acctgTransTypeId = lista.get(0).getString("acctgTransTypeId");			
		}
		return acctgTransTypeId;
	}
	
	/**
	 * Dar de baja un activo fijo
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> bajaActivoFijo(DispatchContext dctx, Map<?, ?> context) 
	{	
		Delegator delegator = dctx.getDelegator();
		Debug.log("context: " + context);		
		
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		String fixedAssetId = (String) context.get("fixedAssetId");
		String fixedAssetTypeId = (String) context.get("fixedAssetTypeId");
		String idMotivo = (String) context.get("idMotivo");
		String nombreActivo = (String) context.get("nombreActivo");
		String monto = (String) context.get("monto");
		String montoDepreciacion = (String) context.get("montoDepreciacion");
		String montoActivo = null;
		BigDecimal montoDepreciacionBD = BigDecimal.ZERO;
		if(UtilValidate.isNotEmpty(montoDepreciacion)){
			Double montoDepreciacionInt = Double.valueOf(montoDepreciacion);
			montoDepreciacionBD = BigDecimal.valueOf(montoDepreciacionInt);
		}
		
		if (UtilValidate.isNotEmpty(monto)){
			Double montoInt = Double.valueOf(monto);
			
			BigDecimal montoBD = BigDecimal.valueOf(montoInt);
			
			montoActivo = montoBD.subtract(montoDepreciacionBD).toString();
		}
		
		if(UtilValidate.isEmpty(fechaContable)){
			return ServiceUtil.returnError("No se ingreso la fecha contable");
		}
		
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Map<String, Object> input = FastMap.newInstance();
	    Map<String, Object> output = FastMap.newInstance();
		Map<String, Object> montoMap = FastMap.newInstance();        
	    Map<String, Object> clavePresupuestalMap = FastMap.newInstance();
	    Map<String, Object> catalogoCargoContMap = FastMap.newInstance();
	    Map<String, Object> catalogoAbonoContMap = FastMap.newInstance();
	    Map<String, Object> context2 = FastMap.newInstance();
	    String poliza = null;
	    GenericValue userLogin = (GenericValue) context.get("userLogin");

		EntityCondition condicionEvento = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("motivo", EntityOperator.EQUALS, idMotivo),
				EntityCondition.makeCondition("tipoActivoFijo", EntityOperator.EQUALS, fixedAssetTypeId));
		
		List<GenericValue> motivosActivo;
		try {
			GenericValue fixedAsset = delegator.findByPrimaryKey("FixedAsset", UtilMisc.toMap("fixedAssetId", fixedAssetId));
			String estatus = fixedAsset.getString("statusId");
			if(estatus.equals("ACT_FIJO_BAJA")||estatus.equals("ACT_FIJO_COMODATO")||estatus.equals("ACT_FIJO_INACTIVO")){
				GenericValue estat = delegator.findByPrimaryKey("Estatus", UtilMisc.toMap("statusId", estatus));
				return ServiceUtil.returnError("No se puede dar de baja un activo con estatus " + estat.getString("descripcion"));
			}
			motivosActivo = delegator.findByCondition("MotivosActivoFijo",condicionEvento,null,null);
			if(UtilValidate.isEmpty(motivosActivo)){
				return ServiceUtil.returnError("No existe un evento configurado para esta combinacion: Motivo " + idMotivo + " y el tipo de activo " + fixedAssetTypeId);
			}
			
			EntityCondition condicionLinea = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, motivosActivo.get(0).getString("evento")));
			List<String> orderByLinea = UtilMisc.toList("secuencia");
			List<GenericValue> lineaList = delegator.findByCondition("LineaContable", condicionLinea, null, orderByLinea);
			Debug.logInfo("condicion: " + condicionLinea,MODULE);
			Debug.logInfo("lineaList: " + lineaList,MODULE);
			int i = lineaList.size();
			if(!lineaList.isEmpty())
			{	Iterator<GenericValue> listaIter = lineaList.iterator();	        
				while (listaIter.hasNext())
				{	GenericValue item = listaIter.next();
					String descripcionLinea = item.getString("descripcion");
					descripcionLinea = descripcionLinea.replaceAll(" ", "");
					Debug.logInfo("descripcionLinea: " + descripcionLinea,MODULE);
					if(i==1 && UtilValidate.isNotEmpty(montoActivo)){
						montoMap.put(descripcionLinea+"0", montoActivo);
					}else if(i==2 && UtilValidate.isNotEmpty(montoDepreciacion)){
						montoMap.put(descripcionLinea+"0", montoDepreciacion);
					}else{
						
					}
		        	
		        	catalogoCargoContMap.put(descripcionLinea+"0", "");
		        	catalogoAbonoContMap.put(descripcionLinea+"0", "");
		        	i--;
				}
			}        					
        	clavePresupuestalMap.put("0", "");
        	
        	context2.put("montoMap", montoMap);        
            context2.put("clavePresupuestalMap", clavePresupuestalMap);
            context2.put("catalogoCargoContMap", catalogoCargoContMap);
            context2.put("catalogoAbonoContMap", catalogoAbonoContMap);
            
            Debug.log("montoMap: " + montoMap);
            Debug.log("clavePresupuestalMap: " + clavePresupuestalMap);
            Debug.log("catalogoCargoContMap: " + catalogoCargoContMap);
            Debug.log("catalogoAbonoContMap: " + catalogoAbonoContMap);
			
			//Invocacion a motor contable
        	/*****MOTOR CONTABLE*****/
            input.put("eventoContableId", motivosActivo.get(0).getString("evento"));
        	input.put("tipoClaveId", "EGRESO");
        	input.put("fechaRegistro", UtilDateTime.nowTimestamp());
        	input.put("fechaContable", fechaContable);
        	input.put("currency", "MXN");
        	input.put("usuario", userLogin.getString("userLoginId"));
        	input.put("organizationId", "1");
        	input.put("descripcion", "Baja de "+ nombreActivo);
        	input.put("roleTypeId", "BILL_FROM_VENDOR");
        	input.put("valorCampo", fixedAssetId);
        	input.put("campo", "fixedAssetId");            	    	   
        	input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(motivosActivo.get(0).getString("evento"), delegator, context2, null, null, null, null));
        	
        	
        	//Genera registro en DepreciacionResumen
        	output = dispatcher.runSync("creaTransaccionMotor", input);
        	Debug.log("output: " + output);
        	if(ServiceUtil.isError(output)){
        		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
        	}
        	
        	GenericValue transaccion = (GenericValue) output.get("transaccion");
        	poliza = transaccion.getString("poliza");
        	
        	fixedAsset.set("statusId", "ACT_FIJO_BAJA");
        	fixedAsset.set("comentarioBaja", context.get("comentario"));
        	delegator.store(fixedAsset);
			
			Map<String, Object> result = ServiceUtil.returnSuccess("El activo fijo ha sido dado de baja con \u00e9xito " + poliza);
			return result;
		} catch (GenericEntityException | GenericServiceException e) {
			// TODO Auto-generated catch block
			return ServiceUtil.returnError("Error al crear el activo fijo: " + e.getMessage());
		}
	}
	
	
}
