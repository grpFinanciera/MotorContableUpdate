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
package com.absoluciones.viaticos;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilFormatOut;
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
import org.ofbiz.entity.util.LineaMotorContable;
import org.ofbiz.order.finaccount.UtilClavePresupuestal;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import javolution.util.FastList;
import javolution.util.FastMap;


/**
 *	Servicios de Viatico
 */

public class ViaticosServices {
	
	public final static String CorreoAutorizacion = "CorreoMensajeAutorizar";
	public final static String CorreoComentario = "CorreoMensajeComentario";
	public final static String CorreoAutorizado = "CorreoMensajeAutorizado";
	public final static String CorreoAutorizadoAuto = "CorreoMensajeAutorizadoAutorizador";
	public final static String CorreoRechazo = "CorreoMensajeRechazo";
	public final static String CorreoModificado = "CorreoMensajeModificado";
	
	public final static String estatusComentadaW = "COMENTADA_W"; //StatusWorkFlow
	public final static String estatusAutorizada = "AUTORIZADA"; //StatusWorkFlow
	
	//StatusViaticos
	public final static String viaticoAprobado = "APROBADA_V";
	public final static String viaticoCancelado = "CANCELADA_V";
	public final static String viaticoComentado = "COMENTADA_V";
	public final static String viaticoCreado = "CREADA_V";
	public final static String viaticoEnviado = "ENVIADA_V";
	public final static String viaticoRechazado = "RECHAZADA_V";
	public final static String viaticoComprometido = "COMPROMETIDA_V";
	public final static String viaticoComprobado = "COMPROBADA_V";
	public final static String viaticoValidado = "VALIDADA_V";
	public final static String viaticoFinalizado = "FINALIZADA_V";
	public final static String viaticoDevolucion = "DEVOLUCION_V";
	
	private final static String module = ViaticosServices.class.getName();
	

	/**
	 * Crea el viatico
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> guardarViatico(DispatchContext dctx,Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		String viaticoId = (String) context.get("viaticoId");
		String descripcion = (String) context.get("descripcion");
		String justificacion = (String) context.get("justificacion");
		String areaPartyId = (String) context.get("areaPartyId");
		String organizationPartyId = (String) context.get("organizationPartyId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String tipoMoneda = (String) context.get("tipoMoneda");
		String tipoTransporteId = (String) context.get("tipoTransporte");
		BigDecimal montoDiario = (BigDecimal) context.get("montoDiario");
		BigDecimal montoTransporte = (BigDecimal) context.get("montoTransporte");
		BigDecimal montoTrabCampo = (BigDecimal) context.get("montoTrabCampo");
		String geoOrigenId = (String) context.get("geoOrigen");
		String geoDestinoId = (String) context.get("geoDestino");
		Timestamp fechaInicio = (Timestamp) context.get("fechaInicio");
		Timestamp fechaFin = (Timestamp) context.get("fechaFin");
		String recurso = (String) context.get("recurso");
		String programa2 = (String) context.get("programa2");
		String fuenteFinanciamientoId = (String) context.get("fuenteFinanciamientoId");
		
			try {
				
				GenericValue usuarioInh = delegator.findByPrimaryKey("UsuarioInhViatico", UtilMisc.toMap("personaSolicitanteId", userLogin.getString("partyId")));
				
				if (UtilValidate.isEmpty(usuarioInh)||usuarioInh.getString("estatus").equals("ACTIVO")){
					
					if(!existeGeo(geoOrigenId,delegator)){
						return ServiceUtil.returnError("El origen no es v\u00e1lido");
					}
					
					if(!existeGeo(geoDestinoId,delegator)){
						return ServiceUtil.returnError("El destino no es v\u00e1lido");
					}
					
					GenericValue viatico = GenericValue.create(delegator.getModelEntity("Viatico"));
					if(viaticoId != null && !viaticoId.isEmpty())
						viatico.set("viaticoId", viaticoId);
					else
						viatico.setNextSeqId();
					
					viatico.set("descripcion", descripcion);
					viatico.set("justificacion", justificacion);
					viatico.set("areaPartyId", areaPartyId);
					viatico.set("areaPartyId", areaPartyId);
					viatico.set("organizationPartyId", organizationPartyId);
					viatico.set("tipoMoneda", tipoMoneda);
					viatico.set("tipoTransporteId", tipoTransporteId);
					viatico.set("montoTransporte", montoTransporte);
					viatico.set("montoTrabCampo", montoTrabCampo);
					viatico.set("geoOrigenId", geoOrigenId);
					viatico.set("geoDestinoId", geoDestinoId);
					viatico.set("fechaInicial", fechaInicio);
					viatico.set("fechaFinal", fechaFin);
					viatico.set("recurso", recurso);
					viatico.set("programa2", programa2);
					viatico.set("fuenteFinanciamientoId", fuenteFinanciamientoId);
					viatico.set("statusId", viaticoCreado);
					viatico.set("personaSolicitanteId",
							userLogin.getString("partyId"));
					
					if(montoDiario==null){
						montoDiario = calculaMontoDiario(delegator,viatico.getString("personaSolicitanteId"),tipoMoneda,geoDestinoId,fechaInicio,fechaFin);
					}
					if(montoDiario==null){
						return ServiceUtil.returnError("El solicitante no tiene una posicion asociada");
					}
					
					viatico.set("montoDiario",montoDiario);
					delegator.createOrStore(viatico);
					Map<String, Object> result = ServiceUtil.returnSuccess();
					result.put("viaticoId", viatico.getString("viaticoId"));
					return result;			
			}else{
				Map<String, Object> result = ServiceUtil.returnError("Su usuario esta inactivo, compruebe sus viaticos pendientes");
				
				return result;
			}
				
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}
	
	/**
	 * Envia la observacion
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> enviaObservacion(DispatchContext dctx, Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		String viaticoId = (String) context.get("viaticoId");
		String observacionComprobante = (String) context.get("observacionComprobante");
		
		
		try {
			
			GenericValue viatico = delegator.findByPrimaryKey("Viatico", UtilMisc.toMap("viaticoId", viaticoId));
			
			viatico.set("informeComision",observacionComprobante);
			
			delegator.store(viatico);
			Map<String, Object> result = ServiceUtil.returnSuccess("El informe ha sido guardado con \u00e9xito");
			result.put("viaticoId", viatico.getString("viaticoId"));
			return result;			
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}
	
	/**
	 * Edita el viatico
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> editarViatico(DispatchContext dctx, Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		String viaticoId = (String) context.get("viaticoId");
		String descripcion = (String) context.get("descripcion");
		String justificacion = (String) context.get("justificacion");
		String areaPartyId = (String) context.get("areaPartyId");
		String organizationPartyId = (String) context.get("organizationPartyId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String tipoMoneda = (String) context.get("tipoMoneda");
		String tipoTransporteId = (String) context.get("tipoTransporte");
		BigDecimal montoDiario = (BigDecimal) context.get("montoDiario");
		BigDecimal montoTransporte = (BigDecimal) context.get("montoTransporte");
		BigDecimal montoTrabCampo = (BigDecimal) context.get("montoTrabCampo");
		String geoOrigenId = (String) context.get("geoOrigen");
		String geoDestinoId = (String) context.get("geoDestino");
		Timestamp fechaInicio = (Timestamp) context.get("fechaInicio");
		Timestamp fechaFin = (Timestamp) context.get("fechaFin");
		String recurso = (String) context.get("recurso");
		String programa = (String) context.get("programa");
		String fuenteFinanciamientoId = (String) context.get("fuenteFinanciamientoId");
		
		try {
			
			if(!existeGeo(geoOrigenId,delegator)){
				return ServiceUtil.returnError("El origen no es v\u00e1lido");
			}
			
			if(!existeGeo(geoDestinoId,delegator)){
				return ServiceUtil.returnError("El destino no es v\u00e1lido");
			}
			
			GenericValue viatico = delegator.findByPrimaryKey("Viatico", UtilMisc.toMap("viaticoId", viaticoId));
			
			viatico.set("descripcion", descripcion);
			viatico.set("justificacion", justificacion);
			viatico.set("areaPartyId", areaPartyId);
			viatico.set("areaPartyId", areaPartyId);
			viatico.set("organizationPartyId", organizationPartyId);
			viatico.set("tipoMoneda", tipoMoneda);
			viatico.set("tipoTransporteId", tipoTransporteId);
			viatico.set("montoTransporte", montoTransporte);
			viatico.set("montoTrabCampo", montoTrabCampo);
			viatico.set("geoOrigenId", geoOrigenId);
			viatico.set("geoDestinoId", geoDestinoId);
			viatico.set("fechaInicial", fechaInicio);
			viatico.set("fechaFinal", fechaFin);
			viatico.set("recurso", recurso);
			viatico.set("programa", programa);
			viatico.set("fuenteFinanciamientoId", fuenteFinanciamientoId);
			viatico.set("statusId", viaticoCreado);
			viatico.set("personaSolicitanteId",
					userLogin.getString("partyId"));
			
			if(montoDiario==null){
				montoDiario = calculaMontoDiario(delegator,viatico.getString("personaSolicitanteId"),tipoMoneda,geoDestinoId,fechaInicio,fechaFin);
			}
			if(montoDiario==null){
				return ServiceUtil.returnError("El solicitante no tiene una posicion asociada");
			}
			
			viatico.set("montoDiario",montoDiario);
			delegator.store(viatico);
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("viaticoId", viatico.getString("viaticoId"));
			return result;			
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}
	
	private static boolean existeGeo(String geoId, Delegator delegator) throws GenericEntityException{
		
		return (delegator.findCountByAnd("Geo", UtilMisc.toMap("geoId",geoId)) > 0);
		
	}
	
	/**
	 * Cancela el viatico
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> cancelarViatico(DispatchContext dctx, Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		String viaticoId = (String) context.get("viaticoId");
		
		try {
			
			delegator.storeByCondition("Viatico", UtilMisc.toMap("statusId","CANCELADA_V"), EntityCondition.makeCondition("viaticoId", viaticoId));
			
			Map<String, Object> result = ServiceUtil.returnSuccess();
			return result;			
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}
	
	public static Map<String, Object> actualizaInfoViatico(DispatchContext dctx, Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		String viaticoId = (String) context.get("viaticoId");
		String tipoMoneda = (String) context.get("tipoMoneda");
		String tipoTransporteId = (String) context.get("tipoTransporte");
		BigDecimal montoTransporte = (BigDecimal) context.get("montoTransporte");
		String geoOrigenId = (String) context.get("geoOrigen");
		String geoDestinoId = (String) context.get("geoDestino");
		Timestamp fechaInicio = (Timestamp) context.get("fechaInicio");
		Timestamp fechaFin = (Timestamp) context.get("fechaFin");
		try {
			
			if(!existeGeo(geoOrigenId,delegator)){
				return ServiceUtil.returnError("El origen no es v\u00e1lido");
			}
			
			if(!existeGeo(geoDestinoId,delegator)){
				return ServiceUtil.returnError("El destino no es v\u00e1lido");
			}
			
			GenericValue viatico = delegator.findByPrimaryKey("Viatico", UtilMisc.toMap("viaticoId",viaticoId));
			
			viatico.set("tipoMoneda", tipoMoneda);
			viatico.set("tipoTransporteId", tipoTransporteId);
			viatico.set("montoTransporte", montoTransporte);
			viatico.set("geoOrigenId", geoOrigenId);
			viatico.set("geoDestinoId", geoDestinoId);
			viatico.set("fechaInicial", fechaInicio);
			viatico.set("fechaFinal", fechaFin);
			BigDecimal montoDiario = calculaMontoDiario(delegator,viatico.getString("personaSolicitanteId"),tipoMoneda,geoDestinoId,fechaInicio,fechaFin);
			if(montoDiario==null){
				return ServiceUtil.returnError("El solicitante no tiene una posicion asociada");
			}
			
			//Verificamos si el solicitante tiene saldo para viaticos.
			BigDecimal montoSolicitante = buscaSaldoSolicitante(delegator, viatico.getString("personaSolicitanteId"), fechaInicio);
			
			if(montoSolicitante.compareTo(montoDiario.add(montoTransporte))<0){
				return ServiceUtil.returnError("El solicitante no tiene saldo suficiente: "+montoSolicitante.subtract(montoDiario.add(montoTransporte)));
			}
			
			viatico.set("montoDiario",montoDiario);			
			delegator.store(viatico);
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("viaticoId", viatico.getString("viaticoId"));
			return result;
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}

	/**
	 * Agrega un articulo a el viatico , realizando validaciones necesarias
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> agregaComprobanteViatico(DispatchContext dctx, Map<?, ?> context){
		
		Delegator delegator = dctx.getDelegator();
		String viaticoId = (String) context.get("viaticoId");
		BigDecimal monto = (BigDecimal) context.get("monto");
		Timestamp fecha = (Timestamp) context.get("fecha");
		String conceptoViaticoId = (String) context.get("conceptoViatico");
		String referencia = (String) context.get("referencia");
		String descripcion = (String) context.get("descripcion");
	
		try {
			
			//Agregamos los datos del viatico.
			GenericValue viaticoComprobanteMonto = GenericValue.create(delegator.getModelEntity("ConceptoViaticoMonto"));
			viaticoComprobanteMonto.setNextSeqId();
			viaticoComprobanteMonto.set("viaticoId",viaticoId);
			viaticoComprobanteMonto.set("monto",monto);
			viaticoComprobanteMonto.set("fecha",fecha);
			viaticoComprobanteMonto.set("conceptoViaticoId",conceptoViaticoId);
			viaticoComprobanteMonto.set("referencia",referencia);
			viaticoComprobanteMonto.set("descripcion",descripcion);
			
			delegator.create(viaticoComprobanteMonto);
			
			//Aumentamos el monto Diario o monto Transporte Comprobado.
			GenericValue viatico = delegator.findByPrimaryKey("Viatico", UtilMisc.toMap("viaticoId", viaticoId));
			GenericValue concepto = delegator.findByPrimaryKey("ConceptoViatico", UtilMisc.toMap("conceptoViaticoId", conceptoViaticoId));
			String campo = concepto.getString("diarioTransporteFlag")
					.equalsIgnoreCase("D") ? "montoDiarioComprobado"
					: concepto.getString("diarioTransporteFlag")
							.equalsIgnoreCase("T") ? "montoTransporteComprobado"
							: "montoTrabCampoComprobado";
			BigDecimal montoTotal = viatico.getBigDecimal(campo);
			viatico.set(campo, montoTotal==null?monto:montoTotal.add(monto));
			delegator.store(viatico);
			
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("viaticoId", viaticoId);
			return result;
			
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
		
	}
	
	/**
	 * Agrega un articulo a el viatico , realizando validaciones necesarias
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> agregaItemViatico(DispatchContext dctx, Map<String, Object> context){
		
		Delegator delegator = dctx.getDelegator();
		String viaticoId = (String) context.get("viaticoId");
		BigDecimal monto = (BigDecimal) context.get("monto");
		String organizationPartyId = (String) context.get("organizationPartyId");
		String ciclo = (String) context.get("ciclo");
	
		try {
			
			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND, EntityCondition.makeCondition("viaticoId", EntityOperator.EQUALS, viaticoId));
			List<GenericValue> detallesViatico = delegator.findByCondition("DetalleViatico",condiciones, UtilMisc.toList("viaticoId","clavePresupuestal","monto", "detalleViaticoId"), null);
			
			String detalleViaticoId = "";
			long detalleId = 0;
			Iterator<GenericValue> detallesViaticoIter = detallesViatico.iterator();
			while (detallesViaticoIter.hasNext()) 
			{	GenericValue generic = detallesViaticoIter.next();
				detalleViaticoId = generic.getString("detalleViaticoId");
				detalleId = Long.parseLong(detalleViaticoId);
			}
			
			GenericValue detViatico = GenericValue.create(delegator.getModelEntity("DetalleViatico"));
			detViatico.set("viaticoId", viaticoId);
			detViatico.set("detalleViaticoId", UtilFormatOut.formatPaddedNumber((detalleId + 1), 4));
			detViatico.set("monto", monto);
			String clavePresupuestal = UtilClavePresupuestal.almacenaClavePresupuestal(context,detViatico,delegator,UtilClavePresupuestal.EGRESO_TAG, organizationPartyId,true,ciclo);
			detViatico.set("clavePresupuestal", clavePresupuestal);
			
			//Valida clave presupuestal y producto
			UtilClavePresupuestal.validaClaveProductoViatico(clavePresupuestal, delegator, viaticoId);
			
			//Se agrega a la lista la nueva viatico para validar la suficiencia presupuestal
			detallesViatico.add(detViatico);
			//Se valida suficiencia
			//UtilClavePresupuestal.validaSuficienciaClaves(clavePresupuestal, monto, "DISPONIBLE", delegator);
			delegator.create(detViatico);
						
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("viaticoId", viaticoId);
			return result;
			
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
		
	}
	
	
	/**
	 * Envia la solicitud a aprobacion de los autorizadores
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> enviarSolicitud(DispatchContext dctx, Map<String, Object> context) throws GenericEntityException 
	{	String estatusWorkflowPendiente = "PENDIENTE";
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Locale locale = (Locale) context.get("locale");
		String urlHost = (String) context.get("urlHost");
		Map<String, Object> result = ServiceUtil.returnSuccess();		
		String viaticoId = (String) context.get("viaticoId");
		
		
		//Obtiene el area y la organizacion en base al Id de el viatico  
		GenericValue viatico = delegator.findByPrimaryKey("Viatico", UtilMisc.toMap("viaticoId",viaticoId));			
		String areaPartyId = viatico.getString("areaPartyId");
		String organizationPartyId = viatico.getString("organizationPartyId");
		String tipoMoneda = viatico.getString("tipoMoneda");
		String tipoTransporteId = viatico.getString("tipoTransporteId");
		String fuenteFinanciamientoId = viatico.getString("fuenteFinanciamientoId");
		
		if(tipoMoneda==null||tipoMoneda.isEmpty()){
			return ServiceUtil.returnError("No se tiene una moneda configurada.");
		}
		
		if(tipoTransporteId==null||tipoTransporteId.isEmpty()){
			return ServiceUtil.returnError("No se tiene un tipo de transporte configurado.");
		}
		
		if(fuenteFinanciamientoId==null||fuenteFinanciamientoId.isEmpty()){
			return ServiceUtil.returnError("No se tiene un tipo de fuente de financiamiento configurado.");
		}
		
		//Cambiar estatus de viatico a ENVIADA
		cambiaStatusViatico(dctx, context, viaticoId, viaticoEnviado);
		
		//Verifica que tipo de workFlow es.
		String tipoWorkFlowId = buscaTipoWorkFlow(viatico);
		
		//Valida si el viatico ya tiene un Workflow asociado
		EntityCondition condicionesWorkflowId = EntityCondition.makeCondition(EntityOperator.AND,
							   					EntityCondition.makeCondition("origenId", EntityOperator.EQUALS, viaticoId),
							   					EntityCondition.makeCondition("tipoWorkFlowId", EntityOperator.EQUALS, tipoWorkFlowId));		
		List<GenericValue> workflowId = delegator.findByCondition("ControlWorkFlow",condicionesWorkflowId, null, null);
		String workFlowId = "";
		boolean esNuevoWorkflow = true;
		if(workflowId.isEmpty())
		{	workFlowId = delegator.getNextSeqId("ControlWorkFlow");			
		}
		else
		{	Iterator<GenericValue> workflowIter = workflowId.iterator();
			while (workflowIter.hasNext())
			{	GenericValue generic = workflowIter.next();
				workFlowId = generic.getString("workFlowId");
				esNuevoWorkflow = false;
			}
		}
					
		//Agrega registro en Control_Workflow
		UtilWorkflow.registroControlWorkflow(dctx, context, viaticoId, tipoWorkFlowId, workFlowId);				
				
		GenericValue autorizador = null;
		if(esNuevoWorkflow)
		{	//Obtener autorizador(es) por area. Registro en Status_Workflow (autorizadores, status PENDIENTE y secuencia de autorizadores		
			UtilWorkflow.registroEstatusWorkflow(dctx, context, areaPartyId, organizationPartyId, workFlowId, estatusWorkflowPendiente, tipoWorkFlowId);
			autorizador = UtilWorkflow.obtenAutorizador(areaPartyId, organizationPartyId, 1, delegator, tipoWorkFlowId);
		}
		else
		{	//Actualiza status en Status_Workflow
			List<GenericValue> statusWorkFlowList = UtilWorkflow.getStatusWorkFlow(workFlowId, estatusComentadaW, delegator);
			if(statusWorkFlowList.isEmpty())
			{	return ServiceUtil.returnError("No se han encontrado autorizadores asociados al Workflow");			
			}
			else
			{	Iterator<GenericValue> statusWorkFlowIter = statusWorkFlowList.iterator();
				while (statusWorkFlowIter.hasNext()) 
				{	GenericValue statusWorkFlow = statusWorkFlowIter.next();								
					statusWorkFlow.set("statusId", estatusWorkflowPendiente);
					delegator.store(statusWorkFlow);
					Long nivel = statusWorkFlow.getLong("nivelAutor");
					autorizador = UtilWorkflow.obtenAutorizador(areaPartyId, organizationPartyId, nivel.intValue(), delegator, tipoWorkFlowId);
				}
			}
			//Se envian correos de notificacion a los autorizadores anteriores
//			List<GenericValue> statusWorkFlowAuto = UtilWorkflow.getStatusWorkFlow(workFlowId, estatusAutorizada, delegator);
//			for (GenericValue statusWF : statusWorkFlowAuto) {
//				String correoOrigen = viatico.getString("personaSolicitanteId");
//				String correoDestino = statusWF.getString("personParentId");
//				UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,tipoWorkFlowId,CorreoModificado,
//						urlHost,viaticoId,delegator,locale,dispatcher);
//			}
			
		}
		
		//Enviar correo de notificacion al autorizador
		String correoOrigen = viatico.getString("personaSolicitanteId");
		String correoDestino = autorizador.getString("autorizadorId");
		UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,tipoWorkFlowId,CorreoAutorizacion,
				urlHost,viaticoId,null,delegator,locale,dispatcher);
		
		return result;
	}
	
	/**
	 * Cambia estatus de el viatico en tabel viatico
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> cambiaStatusViatico(DispatchContext dctx, Map<?, ?> context, String viaticoId, String estatus) throws GenericEntityException 
	{	Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		try
		{	GenericValue viatico = delegator.findByPrimaryKey("Viatico", UtilMisc.toMap("viaticoId",viaticoId));
			viatico.set("statusId", estatus);
			
			String atributo = estatus.equalsIgnoreCase(viaticoComprobado)
					|| estatus.equalsIgnoreCase(viaticoFinalizado) ? "fechaComprobacion"
					: estatus.equalsIgnoreCase(viaticoAprobado) ? "fechaAutorizacion"
							: "";
			
			if(!atributo.isEmpty()){
				viatico.set(atributo,new Timestamp(Calendar.getInstance().getTimeInMillis()));
			}
			
			delegator.store(viatico);
		}
		catch (GenericEntityException e) 
		{	return ServiceUtil.returnError(e.getMessage());
		}
		return result;
	}
	
	
	public static Map<String, Object> guardaAgrupador(DispatchContext dctx, Map<?, ?> context, String estatus, String viaticoId, String agrupador, String transId) throws GenericEntityException 
	{	Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		try
		{	
			String poliza = "",tipoPoliza = "";
			if (estatus.equalsIgnoreCase(viaticoComprometido)){
				poliza = "polizaComprometida";
				tipoPoliza = "acctTransPolizaComprometida";			
			}else if(estatus.equalsIgnoreCase(viaticoValidado)){
				poliza = "polizaComprobada";
				tipoPoliza = "acctTransPolizaComprobada";
			}else if(estatus.equalsIgnoreCase(viaticoComprobado)){
				poliza = "polizaComprobada";
				tipoPoliza = "acctTransPolizaComprobada";
			}else if(estatus.equalsIgnoreCase(viaticoDevolucion)){
				poliza = "polizaDevolucion";
				tipoPoliza = "acctTransPolizaDevolucion";
			}
			
			GenericValue viaticoPoliza = GenericValue.create(delegator.getModelEntity("ViaticoPoliza"));
			viaticoPoliza.set("viaticoId", viaticoId);
			viaticoPoliza.set(poliza, agrupador);
			viaticoPoliza.set(tipoPoliza, transId);
			delegator.createOrStore(viaticoPoliza);
		}
		catch (GenericEntityException e) 
		{	return ServiceUtil.returnError(e.getMessage());
		}
		return result;
	}
	
	/**
	 * Se obtienen los autorizadores por area. 
	 * Se genera un registro en Estatus_Workflow (autorizadores, status PENDIENTE y jerarquia de autorizadores)
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> autorizarSolicitud(DispatchContext dctx, Map<?, ?> context) throws GenericEntityException
	{	Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Locale locale = (Locale) context.get("locale");
		Map<String, Object> result = ServiceUtil.returnSuccess();
		String viaticoId = (String) context.get("viaticoId");
		String estatusWorkflowPendiente = "PENDIENTE";
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String userLoginpartyId = userLogin.getString("partyId");
		String urlHost = (String) context.get("urlHost");
		String comentario = (String) context.get("comentarioAutorizado");
		String workFlowId;
		String area = "";
		String secuencia = "";
		String nuevoAutorizador = "";
		long nuevaSecuencia = 0;
		try 
		{	GenericValue viatico = delegator.findByPrimaryKey("Viatico", UtilMisc.toMap("viaticoId",viaticoId));
		
			//buscamos el tipo de workFlow
			String tipoWorkFlowId = buscaTipoWorkFlow(viatico);
			
			//Cambiar status de STATUS_WORKFLOW para el autorizador en curso*/			
			workFlowId = UtilWorkflow.obtenerWorkFlowId(dctx, context, viaticoId, tipoWorkFlowId);
			UtilWorkflow.cambiarStatusAutorizadorActual(dctx, context, workFlowId, estatusAutorizada, userLoginpartyId);
			//Validar si contiene otro autorizador
			//Obtener area de el viatico
			EntityCondition condicionesArea = EntityCondition.makeCondition(EntityOperator.AND,EntityCondition.makeCondition("viaticoId", EntityOperator.EQUALS, viaticoId));
			List<GenericValue> areaSolicitante = delegator.findByCondition("Viatico", condicionesArea, UtilMisc.toList("areaPartyId"), null);
			//Debug.log("Omar - areaSolicitante: " + areaSolicitante);
			if(areaSolicitante.isEmpty())
			{	return ServiceUtil.returnError("el viatico no contiene con un area solicitante");
			}
			area = areaSolicitante.get(0).getString("areaPartyId");
			//Debug.log("Omar - area: " + area);
			
			//Obtener secuencia del autorizador actual
			EntityCondition condicionesSecuencia = EntityCondition.makeCondition(EntityOperator.AND,
					  EntityCondition.makeCondition("areaId", EntityOperator.EQUALS, area),
					  EntityCondition.makeCondition("autorizadorId", EntityOperator.EQUALS, userLoginpartyId),
					  EntityCondition.makeCondition("tipoWorkFlowId", EntityOperator.EQUALS, tipoWorkFlowId));
			List<String> orderBy = UtilMisc.toList("secuencia");
			List<GenericValue> secuenciaAutorizador = delegator.findByCondition("Autorizador", condicionesSecuencia, UtilMisc.toList("secuencia"), orderBy);
			//Debug.log("Omar - secuenciaAutorizador: " + secuenciaAutorizador);
			if(secuenciaAutorizador.isEmpty())
			{
				return ServiceUtil.returnError("El autorizador actual no contiene una secuencia");
			}			
			secuencia = secuenciaAutorizador.get(0).getString("secuencia");
			//Debug.log("Omar - secuencia: " + secuencia);
			
			//Validar si contiene otro autorizador obteniendo la secuencia siguiente
			List<String> usuariosActivos = UtilMisc.toList("PARTY_ENABLED", "LEAD_ASSIGNED");
			EntityCondition condicionesNuevoAutorizador = EntityCondition.makeCondition(EntityOperator.AND,
					  EntityCondition.makeCondition("areaId", EntityOperator.EQUALS, area),
					  EntityCondition.makeCondition("secuencia", EntityOperator.GREATER_THAN, secuencia),
					  EntityCondition.makeCondition("statusId", EntityOperator.IN, usuariosActivos),
					  EntityCondition.makeCondition("tipoWorkFlowId", EntityOperator.EQUALS, tipoWorkFlowId));
			List<GenericValue> secuenciaNuevoAutorizador = delegator.findByCondition("AutorizadoresActivos", condicionesNuevoAutorizador, null, orderBy);
			//Debug.log("Omar - secuenciaNuevoAutorizador: " + secuenciaNuevoAutorizador);
			if(!secuenciaNuevoAutorizador.isEmpty())
			{	//Se crea registro de nuevo autorizador en la tabla STATUS_WORKFLOW
				nuevoAutorizador = secuenciaNuevoAutorizador.get(0).getString("autorizadorId");
				nuevaSecuencia = secuenciaNuevoAutorizador.get(0).getLong("secuencia");
				//Debug.log("Omar - nuevoAutorizador: " + nuevoAutorizador);
				//Debug.log("Omar - nuevaSecuencia: " + nuevaSecuencia);
				GenericValue statusWorkFlow = GenericValue.create(delegator.getModelEntity("StatusWorkFlow"));
				String statusWorkFlowId = delegator.getNextSeqId("StatusWorkFlow");
				statusWorkFlow.set("statusWorkFlowId", statusWorkFlowId);
				statusWorkFlow.set("workFlowId", workFlowId);
				statusWorkFlow.set("personParentId", nuevoAutorizador);
				statusWorkFlow.set("statusId", estatusWorkflowPendiente);
				statusWorkFlow.set("nivelAutor", UtilFormatOut.formatPaddedNumber(nuevaSecuencia, 4));
				//Debug.log("Omar - statusWorkFlow: " + statusWorkFlow);
				delegator.create(statusWorkFlow);		
				String correoOrigen = viatico.getString("personaSolicitanteId");
				String correoDestino = secuenciaNuevoAutorizador.get(0).getString("autorizadorId");
				//Enviar correo electronico al solicitante
				UtilWorkflow.armarEnviarCorreo(userLoginpartyId,correoOrigen,tipoWorkFlowId,CorreoAutorizado,urlHost,viaticoId,userLoginpartyId,delegator,locale,dispatcher);
				//Enviar correo electronico al nuevo autorizador
				UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,tipoWorkFlowId,CorreoAutorizado,urlHost,viaticoId,null,delegator,locale,dispatcher);
				// Cambiar fecha de autorizacion por cada autorizador.
				viatico.put("fechaAutorizacion", UtilDateTime.nowTimestamp());
				viatico.store();
			}
			else
			{	//Debug.log("Omar - cambiaStatusViatico");
				cambiaStatusViatico(dctx, context, viaticoId, viaticoAprobado);
				//Debug.log("Omar - cambiaStatusDetallaViatico");
				String correoOrigen = userLoginpartyId;
				String correoDestino = viatico.getString("personaSolicitanteId");
				//Enviar correo electronico al solicitante
				UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,tipoWorkFlowId,CorreoAutorizado,urlHost,viaticoId,correoOrigen,delegator,locale,dispatcher);
				
				//Se envia el correo de notificacion a todos los autorizadores 
//				List<GenericValue> autorizadores = UtilWorkflow.obtenAutorizadores(area, viatico.getString("organizationPartyId"), delegator, tipoWorkFlowId);
//				for (GenericValue autoriza : autorizadores) {
//					correoDestino = autoriza.getString("autorizadorId");
//					UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,tipoWorkFlowId,CorreoAutorizadoAuto,
//							urlHost,viaticoId,delegator,locale,dispatcher);
//				}
			}
			//Insertar comentario
			if(comentario != null && !comentario.equals(""))
			{	UtilWorkflow.insertarComentario(dctx, context, comentario, workFlowId, userLoginpartyId, viaticoId);					
			}
		} 
		catch (GenericEntityException e)
		{	e.printStackTrace();
		}						
		return result;
	}
	
	/**
	 * Se obtienen los autorizadores por area. 
	 * Se genera un registro en Estatus_Workflow (autorizadores, status PENDIENTE y jerarquia de autorizadores)
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> comentarSolicitud(DispatchContext dctx, Map<?, ?> context) throws GenericEntityException 
	{	Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Locale locale = (Locale) context.get("locale");
		Map<String, Object> result = ServiceUtil.returnSuccess();
		String viaticoId = (String) context.get("viaticoId");
		String comentario = (String) context.get("comentarioComentar");
		String workFlowId = "";
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String userLoginpartyId = userLogin.getString("partyId");		
		String urlHost = (String) context.get("urlHost");
		try
		{	
			GenericValue viatico = delegator.findByPrimaryKey("Viatico", UtilMisc.toMap("viaticoId",viaticoId));
			//Actualizar el viatico en rechazada
			cambiaStatusViatico(dctx, context, viaticoId, viaticoComentado);
			
			//Verifica que tipo de workFlow es.
			String tipoWorkFlowId = buscaTipoWorkFlow(viatico);
			
			//Obtener el workFlowId		
			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
					  					  EntityCondition.makeCondition("origenId", EntityOperator.EQUALS, viaticoId),
					  					EntityCondition.makeCondition("tipoWorkFlowId", EntityOperator.EQUALS, tipoWorkFlowId));
			List<GenericValue> workFlow = delegator.findByCondition("ControlWorkFlow", condiciones, UtilMisc.toList("workFlowId","tipoWorkFlowId"), null);		
			if(workFlow.isEmpty())
			{	return ServiceUtil.returnError("No se ha encontrado un Workflow asociado");			
			}
			else
			{	Iterator<GenericValue> workFlowIter = workFlow.iterator();
				while (workFlowIter.hasNext()) 
				{	GenericValue generic = workFlowIter.next();								
					workFlowId = generic.getString("workFlowId");	
					tipoWorkFlowId = generic.getString("tipoWorkFlowId");
				}			
				//Actualizar STATUS_WORK_FLOW en rechazado SOLO para el autorizador actual
				if(!workFlowId.equals(""))
				{	UtilWorkflow.cambiarStatusAutorizadorActual(dctx, context, workFlowId, estatusComentadaW, userLoginpartyId);
				}				
				//Insertar comentario
				if(comentario != null && !comentario.equals(""))
				{	UtilWorkflow.insertarComentario(dctx, context, comentario, workFlowId, userLoginpartyId, viaticoId);					
				}			
			}
			
			//Enviar correo de notificacion al solicitante
			String correoOrigen = userLogin.getString("partyId");
			String correoDestino = viatico.getString("personaSolicitanteId");
			UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,tipoWorkFlowId,CorreoComentario,
					urlHost,viaticoId,correoOrigen,delegator,locale,dispatcher);
		}
		catch (GenericEntityException e) 
		{	return ServiceUtil.returnError(e.getMessage());
		}						
		return result;
	}
	
	/**
	 * Se obtienen los autorizadores por area. 
	 * Se genera un registro en Estatus_Workflow (autorizadores, status PENDIENTE y jerarquia de autorizadores)
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> rechazarSolicitud(DispatchContext dctx, Map<?, ?> context) throws GenericEntityException 
	{	Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Locale locale = (Locale) context.get("locale");
		Map<String, Object> result = ServiceUtil.returnSuccess();
		String viaticoId = (String) context.get("viaticoId");
		String comentario = (String) context.get("comentarioRechazo");
		String estatusRechazadaW = "RECHAZADA_W";
		String workFlowId = "";
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String userLoginpartyId = userLogin.getString("partyId");	
		String urlHost = (String) context.get("urlHost");
		
		try
		{	
			GenericValue viatico = delegator.findByPrimaryKey("Viatico", UtilMisc.toMap("viaticoId",viaticoId));
			//Actualizar el viatico en rechazada
			cambiaStatusViatico(dctx, context, viaticoId, viaticoRechazado);
			
			//buscamos el tipo de workFlow
			String tipoWorkFlowId = buscaTipoWorkFlow(viatico);
			
			//Obtener el workFlowId		
			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
					  					  EntityCondition.makeCondition("origenId", EntityOperator.EQUALS, viaticoId),
					  					  EntityCondition.makeCondition("tipoWorkFlowId", EntityOperator.EQUALS, tipoWorkFlowId));
			List<GenericValue> workFlow = delegator.findByCondition("ControlWorkFlow",condiciones, UtilMisc.toList("workFlowId","tipoWorkFlowId"), null);		
			if(workFlow.isEmpty())
			{	return ServiceUtil.returnError("No se ha encontrado un Workflow asociado");			
			}
			else
			{	Iterator<GenericValue> workFlowIter = workFlow.iterator();
				while (workFlowIter.hasNext()) 
				{	GenericValue generic = workFlowIter.next();								
					workFlowId = generic.getString("workFlowId");
					tipoWorkFlowId = generic.getString("tipoWorkFlowId");
				}			
				//Actualizar STATUS_WORK_FLOW en rechazado para todos los autorizadores
				if(!workFlowId.equals(""))
				{	UtilWorkflow.cambiarStatusAutorizadores(dctx, context, workFlowId, estatusRechazadaW);
				}				
				//Insertar comentario
				if(comentario != null && !comentario.equals(""))
				{	UtilWorkflow.insertarComentario(dctx, context, comentario, workFlowId, userLoginpartyId, viaticoId);					
				}			
			}
			
			//Enviar correo de notificacion al solicitante
			String correoOrigen = viatico.getString("personaSolicitanteId");
			String correoDestino = userLogin.getString("partyId");
			UtilWorkflow.armarEnviarCorreo(correoDestino,correoOrigen,tipoWorkFlowId,CorreoRechazo,
					urlHost,viaticoId,correoDestino,delegator,locale,dispatcher);
		}
		catch (GenericEntityException e) 
		{	return ServiceUtil.returnError(e.getMessage());
		}						
		return result;		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map<String, Object> comprometerViatico(DispatchContext dctx,
			Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		String viaticoId = (String) context.get("viaticoId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String tipoClave = "EGRESO";
		String eventoContableId = (String) context.get("tipoDocumento");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		String tipoMoneda = (String) context.get("tipoMoneda");
		Timestamp fechaTrans = UtilDateTime.nowTimestamp();
		BigDecimal montoTotal = BigDecimal.ZERO;
		String ciclo = (String) context.get("ciclo");
		
		try {
			//Buscamos el viatico a comprometer
			GenericValue viatico = delegator.findByPrimaryKey("Viatico", UtilMisc.toMap("viaticoId", viaticoId));
			String organizationPartyId = viatico.getString("organizationPartyId");
			String currency = viatico.getString("tipoMoneda");
			
			//Si trae la moneda , significa que se va a modificar , por lo tanto se actualiza
			if(UtilValidate.isNotEmpty(tipoMoneda)){
				viatico.set("tipoMoneda", tipoMoneda);
				delegator.store(viatico);
			}
			
			//Buscamos sus lineas.
			List<GenericValue> detallesViatico = delegator.findByAnd("DetalleViatico", UtilMisc.toMap("viaticoId", viaticoId));
			
			//Creamos la lista de lineasMotor.
			List <LineaMotorContable> lineas = new FastList<LineaMotorContable>();
			
			for(GenericValue detalle : detallesViatico){
				Map input = FastMap.newInstance();
				int indiceAdmin = UtilClavePresupuestal.indiceClasAdmin(tipoClave, organizationPartyId, delegator, ciclo);
				
                for(int i=1; i<16; i++){
					if(indiceAdmin == i)
                		input.put("clasificacion"+i,buscaExternalId(detalle.getString("acctgTagEnumIdAdmin"),delegator));
                	String clasif = (String)detalle.getString("acctgTagEnumId" + i);
                	if(clasif != null && !clasif.isEmpty())
                		input.put("clasificacion"+i,buscaSequenceId(clasif, delegator));
                }
                
                //Seteamos la informacion en una lineaMotor.
                LineaMotorContable linea = new LineaMotorContable();
                
                linea.setClavePresupuestal(detalle.getString("clavePresupuestal"));
                linea.setMontoPresupuestal(detalle.getBigDecimal("monto"));
                montoTotal = montoTotal.add(detalle.getBigDecimal("monto"));
                lineas.add(linea);
            }
			
			//Hacemos la poliza.
			Debug.log("Total de lineas motor.- "+ lineas.size());
			
			Map<String, Object> input = FastMap.newInstance();
			input.put("fechaRegistro", fechaTrans);
			input.put("fechaContable", fechaContable);
			input.put("eventoContableId", eventoContableId);
			input.put("usuario", userLogin.getString("userLoginId"));
			input.put("organizationId", organizationPartyId);
			input.put("currency", "MXN");
			input.put("tipoClaveId", tipoClave);
			input.put("descripcion", viatico.getString("descripcion")+" - Compromiso");
			input.put("lineasMotor", lineas);
			Map<String, Object> result = dispatcher.runSync("creaTransaccionMotor", input);

			if (ServiceUtil.isError(result)) {
				Debug.log("Hubo Error");
				return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
			}
			
			//Actualizamos el estatus del viatico
			cambiaStatusViatico(dctx, context, viaticoId, viaticoComprometido);
			
			GenericValue trans = (GenericValue) result.get("transaccion");
			
			//Guardamos el agrupador y tipoPoliza del viatico.
			String poliza = trans.getString("poliza");
			String transId = trans.getString("acctgTransId");
			guardaAgrupador(dctx, context, viaticoComprometido, viaticoId, poliza, transId);
			
			//Verificamos si el solicitante tiene saldo para viaticos.
			BigDecimal montoSolicitante = buscaSaldoSolicitante(delegator, viatico.getString("personaSolicitanteId"), fechaContable);
			BigDecimal montoNuevo = montoSolicitante.subtract(montoTotal);
			
			if(montoSolicitante.compareTo(montoTotal)<0){
				return ServiceUtil.returnError("El solicitante no tiene saldo suficiente: "+montoNuevo);
			}
			
			//Actualizamos el saldo del solicitante.
			actualizaSaldoSolicitante(delegator, viatico.getString("personaSolicitanteId"), fechaContable, montoNuevo);
			
			//Agregamos la informacion para el tesorero.
			GenericValue pendienteTesoreria = GenericValue.create(delegator.getModelEntity("PendientesTesoreria"));
			pendienteTesoreria.set("idPendienteTesoreria",viaticoId);
			pendienteTesoreria.set("tipo", "VIATICOS");
			pendienteTesoreria.set("descripcion", viatico.getString("descripcion"));
			pendienteTesoreria.set("auxiliar", viatico.getString("personaSolicitanteId"));
			pendienteTesoreria.set("monto",montoTotal);
			pendienteTesoreria.set("estatus",viaticoComprometido);
			pendienteTesoreria.set("fechaTransaccion", fechaTrans);
			pendienteTesoreria.set("fechaContable", fechaContable);	    			
			pendienteTesoreria.set("moneda", currency);
			delegator.create(pendienteTesoreria);								
			
			Map<String, Object> tesoreria = ServiceUtil.returnSuccess();
			tesoreria.put("viaticoId", viaticoId);
			return tesoreria;
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		} catch (GenericServiceException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}
	
	/**
	 * Realiza la comprobacion del viatico 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> comprobarViaticoSolicitante(DispatchContext dctx,
			Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		String viaticoId = (String) context.get("viaticoId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		
		try{
			
			Map<String, Object> result = null;
			//Actualizamos el estatus del viatico
			cambiaStatusViatico(dctx, context, viaticoId, viaticoComprobado);
			result = ServiceUtil.returnSuccess();
			result.put("viaticoId", viaticoId);
			
			GenericValue configDias = delegator.findByPrimaryKey("ConfiguracionViatico", UtilMisc.toMap("configViaticoId","DIAS_COMPROBACION"));
			GenericValue configSolicitudes = delegator.findByPrimaryKey("ConfiguracionViatico", UtilMisc.toMap("configViaticoId","SOLICITUDES_COMPROBACION"));
			int solicitudes = Integer.valueOf(configSolicitudes.getString("valor"));
			int dias = Integer.valueOf(configDias.getString("valor"));			
			
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();
			searchConditions.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "PAGADA_V"));
			searchConditions.add(EntityCondition.makeCondition("personaSolicitanteId", EntityOperator.EQUALS, userLogin.getString("partyId")));
			
			EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
			
			List<GenericValue> viaticos = delegator.findByCondition("Viatico", condiciones, null, UtilMisc.toList("personaSolicitanteId"));
			
			Boolean validafecha = true;
			
			for (GenericValue viatico : viaticos){
				
				Calendar calendar = Calendar.getInstance();
				Calendar calendar2 = Calendar.getInstance();
				calendar.setTime(viatico.getTimestamp("fechaFinal"));
				calendar2.setTime(UtilDateTime.nowTimestamp());
				calendar.add(Calendar.DAY_OF_YEAR, dias);
				
				int comparacion = calendar.compareTo(calendar2);
				
				if(comparacion<=0){
					
					validafecha=false;
					
				}else{
					
				}
				
			}
			
			if(validafecha && viaticos.size()<solicitudes){
			
				GenericValue usuario = delegator.findByPrimaryKey("UsuarioInhViatico", UtilMisc.toMap("personaSolicitanteId", userLogin.getString("partyId")));
				
				if(UtilValidate.isNotEmpty(usuario)){
				
					usuario.set("estatus", "ACTIVO");
					delegator.store(usuario);
					
				}
			}
			
			return result;
		} catch (Exception ex) {
			return ServiceUtil.returnError(ex.getMessage());
		}
	}
	
	/**
	 * Realiza la comprobacion del viatico , afectacion contable-presupuestal 
	 * @param dctx
	 * @param context
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public static Map<String, Object> comprobarViatico(DispatchContext dctx,
			Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		String viaticoId = (String) context.get("viaticoId");
		Boolean devolucion100 = (Boolean) context.get("devolucion");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		Map<String,String> detallesViaticoId = (Map<String,String>) context.get("detallesViaticoId"); 
		Map<String,String> montosDetalle = (Map<String,String>) context.get("montosDetalle");
		String tipoClave = "EGRESO";
		String eventoContableId = (String) context.get("tipoDocumento");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		Timestamp fechaTrans = UtilDateTime.nowTimestamp();
		BigDecimal montoTotal = BigDecimal.ZERO;
		
		
		//Creamos un mapa de los id con sus montos.
		Map<String,BigDecimal> detalleMontos = new HashMap<String, BigDecimal>();
		BigDecimal monto = BigDecimal.ZERO;
		for(Map.Entry<String, String> entry : detallesViaticoId.entrySet()) {
			monto = UtilNumber.getBigDecimal(montosDetalle.get(entry.getKey()));
			detalleMontos.put(entry.getValue(), monto);
			montoTotal = montoTotal.add(monto);
		}
		Debug.logVerbose("detalleMontos.- "+detalleMontos,module);
		
		try{
			//Verificamos si la suma de conceptos es igual a la ingresada en las claves presupuestales.
//			Map<String,String> conceptosViatico = (Map<String,String>) context.get("conceptosViatico"); 
			
			//Buscamos el viatico a comprobar
			GenericValue viatico = delegator.findByPrimaryKey("Viatico", UtilMisc.toMap("viaticoId", viaticoId));
			String organizationPartyId = viatico.getString("organizationPartyId");
			String currency = viatico.getString("tipoMoneda");
			String solicitante = viatico.getString("personaSolicitanteId");
			String descripcion = viatico.getString("descripcion");
			
			//Buscamos sus lineas.
			List<GenericValue> detallesViatico = delegator.findByAnd("DetalleViatico", UtilMisc.toMap("viaticoId", viaticoId));
			
			//Devoluciones
			boolean devolucion = false;
			Map<String,BigDecimal> devoluciones = new HashMap<String, BigDecimal>();
			
			List <LineaMotorContable> lineas = new FastList<LineaMotorContable>();
			

			Map<String, Object> result = FastMap.newInstance();
			
			if(!devolucion100){
				
				for(GenericValue detalle : detallesViatico){
					//Se crea la linea motor.
					LineaMotorContable linea = new LineaMotorContable();
					
					//Buscamos el monto ingresado por el usuario, deacuerdo al id del detalle.
	                monto = detalleMontos.get(detalle.getString("detalleViaticoId"));
	                
	                linea.setClavePresupuestal(detalle.getString("clavePresupuestal"));
	                linea.setMontoPresupuestal(monto);
	                GenericValue lineaMatriz = getLineaMatriz(delegator, eventoContableId);
	                if(lineaMatriz!=null){
	                	//Lleva auxiliares.
	                	if(lineaMatriz.getString("catalogoCargo")!=null&&!lineaMatriz.getString("catalogoCargo").isEmpty()){
	                		lineaMatriz.set("catalogoCargo", solicitante);
	                	}else if (lineaMatriz.getString("catalogoAbono")!=null&&!lineaMatriz.getString("catalogoAbono").isEmpty()){
	                		lineaMatriz.set("catalogoAbono", solicitante);
	                	}

						Map<String, GenericValue> lineasPresupuestales = FastMap
								.newInstance();
						lineasPresupuestales.put(lineaMatriz.getString("secuencia"), lineaMatriz);

						linea.setLineasPresupuestales(lineasPresupuestales);
	                }
	                lineas.add(linea);
	                
	                //Hacemos la devolucion en caso de que exista.
					BigDecimal montoDev = detalle.getBigDecimal("monto").subtract(monto); 
					if(montoDev.compareTo(BigDecimal.ZERO)>0){
						devolucion = true;
						devoluciones.put(detalle.getString("clavePresupuestal"), montoDev);					
					}
				}	
				
				//Hacemos la poliza.
				Debug.log("Total de lineas motor.- "+ lineas.size());
				
				Map<String, Object> input = FastMap.newInstance();
				input.put("fechaRegistro", fechaTrans);
				input.put("fechaContable", fechaContable);
				input.put("eventoContableId", eventoContableId);
				input.put("usuario", userLogin.getString("userLoginId"));
				input.put("organizationId", organizationPartyId);
				input.put("currency", currency);
				input.put("tipoClaveId", tipoClave);
				input.put("descripcion", descripcion+" - Comprobaci\u00f3n");
				input.put("lineasMotor", lineas);
				result = dispatcher.runSync("creaTransaccionMotor", input);

				if (ServiceUtil.isError(result)) {
					Debug.log("Hubo Error");
					return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
				}

				GenericValue trans = (GenericValue) result.get("transaccion");
				
				//Guardamos el agrupador y tipoPoliza del viatico.
				String poliza = trans.getString("poliza");
				String transId = trans.getString("acctgTransId");
				guardaAgrupador(dctx, context, viaticoValidado, viaticoId, poliza, transId);
			} else {
				for(GenericValue detalle : detallesViatico){
					devoluciones.put(detalle.getString("clavePresupuestal"), detalle.getBigDecimal("monto"));	
				}
			}

			
			//Actualizamos el estatus del viatico
			cambiaStatusViatico(dctx, context, viaticoId, devolucion || devolucion100 ? viaticoValidado:viaticoFinalizado);
			
			
			//Mandamos llamar la devolucion
			if(devolucion || devolucion100){
				result = null;
				result = devolucionViatico(dctx, context, devoluciones);
			}			
			
			if(ServiceUtil.isError(result)){
	           	return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
	        }
			
			result = ServiceUtil.returnSuccess();
			result.put("viaticoId", viaticoId);
			return result;
		} catch (Exception ex) {
			return ServiceUtil.returnError(ex.getMessage());
		}
	}
	
	public static Map<String, Object> cancelarViaticoPresupuesto(DispatchContext dctx, Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		String viaticoId = (String) context.get("viaticoId");
		
		try {
			
			delegator.storeByCondition("Viatico", UtilMisc.toMap("statusId","CANCELADA_V"), EntityCondition.makeCondition("viaticoId", viaticoId));
			
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("viaticoId", viaticoId);
			return result;			
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}
	
	public static GenericValue getLineaMatriz(Delegator delegator, String eventoContable) throws GenericEntityException{
		
		EntityConditionList<EntityExpr> matrizCondition = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"acctgTransTypeId", EntityOperator.EQUALS,
						eventoContable), EntityCondition.makeCondition(
						"tipoMatrizId", EntityOperator.NOT_EQUAL, null)));

		List<GenericValue> lineaPresupuestal = delegator.findByCondition("LineaPresupuestal", matrizCondition, null, null);				

		if (lineaPresupuestal.isEmpty()) {
			Debug.log("No se encontraron lineasPresupuestales que usen matriz");
			return null;
		} else {
			if(lineaPresupuestal.size()>1){
				throw new GenericEntityException("Evento mal configurado, solo una linea presupuestal debe usar la matriz");
			}
			return lineaPresupuestal.get(0);
		}
	}
	
	public static Map<String, Object> devolucionViatico(DispatchContext dctx,
			Map<?, ?> context, Map<String,BigDecimal> devoluciones) {
		Debug.log("Entra a devolucion de viatico");
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		String viaticoId = (String) context.get("viaticoId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String tipoClave = "EGRESO";
		String eventoContableId = "DEVO_VIATICO";
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		Timestamp fechaTrans = UtilDateTime.nowTimestamp();
		
		try {
			//Buscamos el viatico a devolver
			GenericValue viatico = delegator.findByPrimaryKey("Viatico", UtilMisc.toMap("viaticoId", viaticoId));
			String organizationPartyId = viatico.getString("organizationPartyId");
			String currency = viatico.getString("tipoMoneda");
			
			//Creamos la lista de lineasMotor.
			List <LineaMotorContable> lineas = new FastList<LineaMotorContable>();
			
			BigDecimal montoDevuelto = BigDecimal.ZERO;
			
			//Iteramos el mapa contable para generar las entries contables.
	    	for (Map.Entry<String, BigDecimal> entry : devoluciones.entrySet()) {
	    		LineaMotorContable linea = new LineaMotorContable();
	    		linea.setClavePresupuestal(entry.getKey());
	    		linea.setMontoPresupuestal(entry.getValue());
	    		montoDevuelto = montoDevuelto.add(entry.getValue());
	    		lineas.add(linea);
	    	}
	    	
	    	//Hacemos la poliza.
			Debug.log("Total de lineas motor.- "+ lineas.size());
			
			Map<String, Object> input = FastMap.newInstance();
			input.put("fechaRegistro", fechaTrans);
			input.put("fechaContable", fechaContable);
			input.put("eventoContableId", eventoContableId);
			input.put("usuario", userLogin.getString("userLoginId"));
			input.put("organizationId", organizationPartyId);
			input.put("currency", "MXN");
			input.put("tipoClaveId", tipoClave);
			input.put("descripcion", viatico.getString("descripcion")+" - Devoluci\u00f3n");
			input.put("lineasMotor", lineas);
			Map<String, Object> result = dispatcher.runSync("creaTransaccionMotor", input);

			if (ServiceUtil.isError(result)) {
				Debug.log("Hubo Error");
				return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
			}

			GenericValue trans = (GenericValue) result.get("transaccion");
			
			//Guardamos el agrupador y tipoPoliza del viatico.
			String poliza = trans.getString("poliza");
			String transId = trans.getString("acctgTransId");
			guardaAgrupador(dctx, context, viaticoDevolucion, viaticoId, poliza, transId);
			
			//Verificamos si el solicitante tiene saldo para viaticos.
			BigDecimal montoSolicitante = buscaSaldoSolicitante(delegator, viatico.getString("personaSolicitanteId"), fechaContable);
			BigDecimal montoNuevo = montoSolicitante.add(montoDevuelto);
			
			//Actualizamos el saldo del solicitante.
			actualizaSaldoSolicitante(delegator, viatico.getString("personaSolicitanteId"), fechaContable, montoNuevo);
			
			//Agregamos la informacion para el tesorero.
			GenericValue pendienteTesoreria = GenericValue.create(delegator.getModelEntity("PendientesTesoreria"));
			pendienteTesoreria.set("idPendienteTesoreria",viaticoId);
			pendienteTesoreria.set("tipo", "VIATICOS_D");
			pendienteTesoreria.set("descripcion", viatico.getString("descripcion"));
			pendienteTesoreria.set("auxiliar", viatico.getString("personaSolicitanteId"));
			pendienteTesoreria.set("monto",montoDevuelto);
			pendienteTesoreria.set("estatus",viaticoValidado);
			pendienteTesoreria.set("fechaTransaccion", fechaTrans);
			pendienteTesoreria.set("fechaContable", fechaContable);	    			
			pendienteTesoreria.set("moneda", currency);
			delegator.create(pendienteTesoreria);
			
			result = ServiceUtil.returnSuccess();
			result.put("viaticoId", viaticoId);
			return result;
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		} catch (GenericServiceException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}
	
	/**
	 * Metodo que calcula el monto diario que se le dara al solicitante para el viaje
	 * @param tipoMoneda
	 * @param destinoId
	 * @param fechaInicio
	 * @param fechaFin
	 * @return
	 * @throws GenericEntityException 
	 */
	public static BigDecimal calculaMontoDiario(Delegator delegator, String solicitante, String tipoMoneda, String destinoId, Timestamp fechaInicio, Timestamp fechaFin) throws GenericEntityException{
		BigDecimal cantidad;
		//Verificamos el tipo de moneda
		Debug.log("tipoMoneda.- "+tipoMoneda);
		if(tipoMoneda.equalsIgnoreCase("MXN")){
			//Checamos la posicion del solicitante para saber el monto
			GenericValue generic = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", solicitante));
			String posicion = generic.getString("emplPositionId");
			if(posicion==null||posicion.isEmpty()){
				Debug.log("CalculaMontoDiario.- El solicitante no tiene una posicion asociada");
				return null;
			}
			generic = delegator.findByPrimaryKey("EmplPosition", UtilMisc.toMap("emplPositionId", posicion));
			cantidad = generic.getBigDecimal("montoViatico");
			//Verificamos si el destino es una zona alta.
			generic = delegator.findByPrimaryKey("ZonaGeoAlta", UtilMisc.toMap("geoId", destinoId, "emplPositionId", posicion));
			if(generic!=null){
				cantidad = generic.getBigDecimal("monto");
			}						
		}else{
			cantidad=BigDecimal.valueOf(Double.valueOf("450"));
		}
		long diferencia = fechaFin.getTime() - fechaInicio.getTime();
		BigDecimal dias = BigDecimal.valueOf(diferencia / (24 * 60 * 60 * 1000));
		//Sumamos un dia en caso de que se haga en el mismo dia.
		dias = dias.add(BigDecimal.ONE);
		Debug.log("dias.- "+dias);
		Debug.log("cantidad.-"+cantidad);
		cantidad = dias.compareTo(BigDecimal.ONE)>0?cantidad.multiply(dias):cantidad.divide(BigDecimal.valueOf(Double.valueOf(2)));
		return cantidad;
	}	
	
	public static String buscaTipoWorkFlow(GenericValue viatico){
		String tipoMoneda = viatico.getString("tipoMoneda");
		String tipoTransporteId = viatico.getString("tipoTransporteId");
		String fuenteFinanciamientoId = viatico.getString("fuenteFinanciamientoId");
		if(!tipoMoneda.equalsIgnoreCase("MXN")||tipoTransporteId.equalsIgnoreCase("AEREO")){
			return "VIATICOSVI";
		}else if(fuenteFinanciamientoId.equals("ASIGNACION_ANUAL")){
			return "VIATICOS";
		}else{
			return "VIATICOSGI";
		}
		/*if(fuenteFinanciamientoId.equals("ASIGNACION_ANUAL")){
			return (!tipoMoneda.equalsIgnoreCase("MXN")||tipoTransporteId.equalsIgnoreCase("AEREO"))?"VIATICOSVI":"VIATICOS";
		}else{
			return "VIATICOSGI";
		}*/
	}
	
	/**
	 * Metodo que busca elexternalId a traves de el partyId
	 * @param partyId
	 * @param delegator
	 * @return
	 * @throws GenericEntityException
	 */
    public static String buscaExternalId(String partyId, Delegator delegator) throws GenericEntityException {
			GenericValue party = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId",partyId));
			if(party == null)
				throw new GenericEntityException("No se encontro el party con id "+partyId);
		return party.getString("externalId");
	}

    /**
     *Metodo que obtiene el sequenceId a traces del enumId
     * @param enumId
     * @param delegator
     * @return
     * @throws GenericEntityException
     */
    public static String buscaSequenceId(String enumId, Delegator delegator) throws GenericEntityException {
		GenericValue enumeration = delegator.findByPrimaryKey("Enumeration", UtilMisc.toMap("enumId",enumId));
		if(enumeration == null)
			throw new GenericEntityException("No se encontro el enumeration con id "+enumId);
	return enumeration.getString("sequenceId");
	}
    
    public static BigDecimal buscaSaldoSolicitante(Delegator delegator, String solicitante,
			Timestamp periodo) throws GenericEntityException {
		periodo = nuevoPeriodo(periodo);
		
		List<GenericValue> generics = delegator.findByAnd("SaldoCatalogo", "catalogoId", solicitante, "tipo", "V", "periodo", periodo);
		return generics.isEmpty()?BigDecimal.ZERO:generics.get(0).getBigDecimal("monto");
	}
    
    public static void actualizaSaldoSolicitante(Delegator delegator, String solicitante,
			Timestamp periodo, BigDecimal monto) throws GenericEntityException {
    	periodo = nuevoPeriodo(periodo);
		
		List<GenericValue> generics = delegator.findByAnd("SaldoCatalogo", "catalogoId", solicitante, "tipo", "V", "periodo", periodo);
		GenericValue saldoSolicitante = generics.get(0);
		saldoSolicitante.set("monto", monto);
		delegator.store(saldoSolicitante);		
	}
    
    public static Map<String, Object> crearSaldoViatico(DispatchContext dctx,
			Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		String catalogoId= (String) context.get("catalogoId");
		BigDecimal saldo= (BigDecimal) context.get("saldo");
		String periodoString = (String) context.get("enumCode");
		String organizationPartyId = (String) context.get("organizationPartyId");
		
		try {
			
			GenericValue saldoCatalogo = GenericValue.create(delegator.getModelEntity("SaldoCatalogo"));
			Timestamp periodo = nuevoPeriodoString(periodoString);
			String tipoId = obtenRolSolicitante(delegator, catalogoId);
			
			//Vemos si existe el catalogoAuxiliar
			List<GenericValue> catalogos = delegator.findByAnd("SaldoCatalogo",
					"catalogoId", catalogoId, "tipoId", tipoId, "partyId", organizationPartyId,
					"tipo", "V", "periodo", periodo);
			if (catalogos.isEmpty()) {
				saldoCatalogo.setNextSeqId();
				saldoCatalogo.set("catalogoId", catalogoId);
				saldoCatalogo.set("tipoId", tipoId);
				saldoCatalogo.set("partyId", organizationPartyId);
				saldoCatalogo.set("tipo", "V");
				saldoCatalogo.set("monto", saldo);
				saldoCatalogo.set("periodo", periodo);
				delegator.create(saldoCatalogo);
				
				//Guardamos en bitacora
				historicoViatico(delegator, catalogoId, "Saldo Inicial", saldo, periodo);
				
				Map<String, Object> result = ServiceUtil.returnSuccess("Se ha registrado el saldo del solicitante.");
				return result;
			}else{
				return ServiceUtil.returnError("El solicitante ya tiene saldo asignado para el periodo ingresado.");
			}
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}
    
    public static Map<String, Object> traspasoSaldoViatico(DispatchContext dctx,
			Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		String catalogoIdViene= (String) context.get("catalogoIdViene");
		String catalogoIdVa= (String) context.get("catalogoIdVa");
		BigDecimal saldo= (BigDecimal) context.get("saldo");
		String periodoString = (String) context.get("enumCode");
		String organizationPartyId = (String) context.get("organizationPartyId");
		
		try {
			
			GenericValue saldoCatalogo;
			Timestamp periodo = nuevoPeriodoString(periodoString);
			
			
			if(catalogoIdViene!=null){
				String tipoId = obtenRolSolicitante(delegator, catalogoIdViene);
				//Vemos si existe el catalogoAuxiliarViene
				List<GenericValue> catalogos = delegator.findByAnd("SaldoCatalogo",
					"catalogoId", catalogoIdViene, "tipoId", tipoId, "partyId", organizationPartyId,
					"tipo", "V", "periodo", periodo);
			
				if(catalogos.isEmpty()) {
					return ServiceUtil.returnError("El solicitante "+ catalogoIdViene +"no tiene un saldo asignado.");
				}
			
				if(catalogos.get(0).getBigDecimal("monto").compareTo(saldo)<0){
					return ServiceUtil.returnError("El solicitante "+ catalogoIdViene +"no tiene saldo suficiente.");
				}
				
				//actualizamos el saldo.
				saldoCatalogo =catalogos.get(0); 
				saldoCatalogo.set("monto", saldoCatalogo.getBigDecimal("monto").subtract(saldo));
				delegator.store(saldoCatalogo);
				
				//Guardamos en bitacora
				historicoViatico(delegator, catalogoIdViene, "Traspaso", saldo.negate(), periodo);
			
			}
			String tipoId = obtenRolSolicitante(delegator, catalogoIdVa);
			//Vemos si existe el catalogoAuxiliarVa
			List<GenericValue> catalogos = delegator.findByAnd("SaldoCatalogo",
					"catalogoId", catalogoIdVa, "tipoId", tipoId, "partyId", organizationPartyId,
					"tipo", "V", "periodo", periodo);
			
			if(UtilValidate.isEmpty(catalogos)){
				//Si no existe registro "Va" se crea uno nuevo para realizar el traspaso
				GenericValue saldoCatalogoVa = GenericValue.create(delegator.getModelEntity("SaldoCatalogo"));
				saldoCatalogoVa.setNextSeqId();
				saldoCatalogoVa.set("catalogoId", catalogoIdVa);
				saldoCatalogoVa.set("tipoId", tipoId);
				saldoCatalogoVa.set("partyId", organizationPartyId);
				saldoCatalogoVa.set("tipo", "V");
				saldoCatalogoVa.set("monto", saldo);
				saldoCatalogoVa.set("periodo", periodo);
				delegator.create(saldoCatalogoVa);
				
			} else {
				//actualizamos el saldo.
				saldoCatalogo =catalogos.get(0); 
				saldoCatalogo.set("monto", saldoCatalogo.getBigDecimal("monto").add(saldo));
				delegator.store(saldoCatalogo);
				
			}
			
			//Guardamos en bitacora
			historicoViatico(delegator, catalogoIdVa, "Traspaso", saldo, periodo);

			Map<String, Object> result = ServiceUtil.returnSuccess("Se ha registrado el traspaso.");
				return result;
				
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}
    
	public static void historicoViatico(Delegator delegator, String solicitanteId, String movimiento, BigDecimal monto, Timestamp fecha) throws GenericEntityException {
		Debug.log("Se impacta el historico de los viaticos");
		GenericValue historico = GenericValue.create(delegator.getModelEntity("HistoricoMontoViatico"));
		historico.setNextSeqId();
		historico.set("solicitanteId", solicitanteId);
		historico.set("movimiento", movimiento);
		historico.set("monto", monto);
		historico.set("periodo", fecha);
		
		delegator.create(historico);	
	}
    
    public static Timestamp nuevoPeriodo(Timestamp periodo){
    	Calendar date = Calendar.getInstance();
		date.setTime(periodo);
		date.set(Calendar.DAY_OF_MONTH, 1);
		date.set(Calendar.MONTH, 0);
		date.set(Calendar.HOUR, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		date.set(Calendar.AM_PM, Calendar.AM);
		return new Timestamp(date.getTimeInMillis());
    }
    
    public static Timestamp nuevoPeriodoString(String periodo){
    	Calendar date = Calendar.getInstance();
		date.set(Calendar.DAY_OF_MONTH, 1);
		date.set(Calendar.YEAR,Integer.parseInt(periodo));
		date.set(Calendar.MONTH, 0);
		date.set(Calendar.HOUR, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		date.set(Calendar.AM_PM, Calendar.AM);
		return new Timestamp(date.getTimeInMillis());
    }
    
    public static String obtenRolSolicitante(Delegator delegator,
			String auxiliarId) throws GenericEntityException {
		Debug.log("Entra a obtenRolSolicitante");

		EntityCondition conditions = EntityCondition.makeCondition(
				EntityOperator.AND, EntityCondition.makeCondition("partyId",
						EntityOperator.EQUALS, auxiliarId));
		
		Debug.log("conditions - obtenerRolAuxiliar: " + conditions);
		
		List<GenericValue> roles = delegator.findByCondition(
				"Party", conditions, null, null);
		Debug.log("roles: " + roles);
		
		if(UtilValidate.isEmpty(roles)){
			throw new GenericEntityException("No se encontro a la persona "+auxiliarId);
		}

		Debug.log("Se obtiene auxiliar: " + roles.get(0).getString("partyTypeId"));
		return roles.get(0).getString("partyTypeId");
	}
    
    /**
	 * Crea Programas para viaticos
	 * @param dctx
	 * @param context
	 * @return
	 */
	
	public static Map<String, Object> guardaPrograma(DispatchContext dctx,Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String partyId = userLogin.getString("partyId");
		String organizationPartyId = (String) context.get("organizationPartyId");

		try	{
			
			GenericValue nuevoPrograma = GenericValue.create(delegator.getModelEntity("ProgramaViatico"));
			nuevoPrograma.setAllFields(context, false, null, null);
			nuevoPrograma.setNextSeqId();
			nuevoPrograma.setString("estatus", "Activo");
			
			delegator.create(nuevoPrograma);			
			
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("programaId", nuevoPrograma.getString("programaId"));
			return result;
		}catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}
	
	/**
	 * Metodo para recargar la pagina
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> cargarPaginaPrograma(DispatchContext dctx,
			Map<?, ?> context) {
		
		Map<String, Object> result = ServiceUtil.returnSuccess();
		
		return result;
	}
    
}