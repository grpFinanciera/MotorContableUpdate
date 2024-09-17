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
package org.ofbiz.order.order;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.common.AB.UtilWorkflow;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.order.finaccount.UtilClavePresupuestal;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import com.ibm.icu.util.Calendar;

/**
 *	Servicios de Requisicion
 */

public class RequisicionServices {
	
	public final static String CorreoAutorizacion = "CorreoMensajeAutorizar";
	public final static String CorreoComentario = "CorreoMensajeComentario";
	public final static String CorreoAutorizado = "CorreoMensajeAutorizado";
	public final static String CorreoAutorizadoAuto = "CorreoMensajeAutorizadoAutorizador";
	public final static String CorreoRechazo = "CorreoMensajeRechazo";
	public final static String CorreoModificado = "CorreoMensajeModificado";
	
	
	public final static String idTipoWorkflowREQ = "REQUISICION"; //Requisiciones
	
	public final static String estatusComentadaW = "COMENTADA_W"; //StatusWorkFlow
	public final static String estatusAutorizada = "AUTORIZADA"; //StatusWorkFlow
//	private static final String MODULE = RequisicionServices.class.getName();

	/**
	 * Crea la requisicion
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> guardaRequisicion(DispatchContext dctx,
			Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		String requisicionId = (String) context.get("requisicionId");
		String descripcion = (String) context.get("descripcion");
		String justificacion = (String) context.get("justificacion");
		String areaPartyId = (String) context.get("areaPartyId");
		String almacenId = (String) context.get("almacenId");
		String tipoMoneda = (String) context.get("tipoMoneda");
		String organizationPartyId = (String) context.get("organizationPartyId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");

		try {
			
			GenericValue requisicion = GenericValue.create(delegator.getModelEntity("Requisicion"));
			if(requisicionId != null && !requisicionId.isEmpty())
				requisicion.set("requisicionId", requisicionId);
			else
				requisicion.setNextSeqId();
			
			requisicion.set("fechaContable", fechaContable);
			requisicion.set("descripcion", descripcion);
			requisicion.set("justificacion", justificacion);
			requisicion.set("areaPartyId", areaPartyId);
			requisicion.set("almacenId", almacenId);
			requisicion.set("tipoMoneda", tipoMoneda);
			requisicion.set("areaPartyId", areaPartyId);
			requisicion.set("organizationPartyId", organizationPartyId);
			requisicion.set("statusId", "CREADA");
			requisicion.set("personaSolicitanteId",
					userLogin.getString("partyId"));
			delegator.create(requisicion);
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("requisicionId", requisicion.getString("requisicionId"));
			return result;
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}
	
	/**
	 * Metodo para recargar la pagina
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> cargarPagina(DispatchContext dctx,
			Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		
		String requisicionId = (String) context.get("requisicionId");		

		GenericValue requisicion = GenericValue.create(delegator.getModelEntity("Requisicion"));
		if(requisicionId != null && !requisicionId.isEmpty())
			requisicion.set("requisicionId", requisicionId);
		
		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("requisicionId", requisicion.getString("requisicionId"));
		return result;
	}
	
	/**
	 * Agrega un articulo a la requisicion , realizando validaciones necesarias
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> agregaItemRequisicion(DispatchContext dctx,
			Map<String, Object> context){

		String estatusProducto = "PENDIENTE";
		Delegator delegator = dctx.getDelegator();
		String organizationPartyId = (String) context.get("organizationPartyId");
        String ciclo = (String) context.get("ciclo");
        String requisicionId = (String) context.get("requisicionId");
        BigDecimal monto = (BigDecimal) context.get("monto");
        BigDecimal cantidad = (BigDecimal) context.get("cantidad");
        String customTimePeriodId = (String) context.get("customTimePeriodId");
        BigDecimal cantidadMeses = (BigDecimal) context.get("cantidadMeses");
        String iva = (String) context.get("iva");
		
		try {
			
			GenericValue requisicion = delegator.findByPrimaryKey("Requisicion", UtilMisc.toMap("requisicionId",requisicionId)); 
			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
										  EntityCondition.makeCondition("requisicionId", EntityOperator.EQUALS, requisicionId));
			
			int meses = cantidadMeses.intValue();
			int customTime = Integer.valueOf(customTimePeriodId);
			for (int i=1; i<=meses; i++){
				
				List<GenericValue> listDetallesRequisicion = delegator.findByCondition("DetalleRequisicion",condiciones, null, UtilMisc.toList("detalleRequisicionId DESC"));
				
				long detalleId = 0;
				
				if(!listDetallesRequisicion.isEmpty()){
					detalleId = Long.valueOf(EntityUtil.getFirst(listDetallesRequisicion).getString("detalleRequisicionId"));
				}
				
				String detalleRequisicionId = UtilFormatOut.formatPaddedNumber((detalleId + 1), 4);
				
				GenericValue detRequisicion = GenericValue.create(delegator.getModelEntity("DetalleRequisicion"));
				detRequisicion.setAllFields(context, true, null, null);
				detRequisicion.set("customTimePeriodId", customTimePeriodId);
				detRequisicion.set("detalleRequisicionId", detalleRequisicionId);
				detRequisicion.set("estatusProducto", estatusProducto);
				detRequisicion.set("iva", UtilValidate.isNotEmpty(iva) ? iva : "N");
				//Se valida la clave presupuestal solo en el caso que exista
				String clavePresupuestal = UtilClavePresupuestal.almacenaClavePresupuestal(
							context,detRequisicion,delegator,UtilClavePresupuestal.EGRESO_TAG, organizationPartyId,true,ciclo);
				detRequisicion.set("clavePresupuestal", clavePresupuestal);
				
				validaCombinacionUnica(context,clavePresupuestal,listDetallesRequisicion,detalleRequisicionId, customTimePeriodId);
				
				//Se agrega a la lista la nueva requisicion para validar la suficiencia presupuestal
				listDetallesRequisicion.add(detRequisicion);
				delegator.create(detRequisicion);
				customTime++;
				customTimePeriodId = String.valueOf(customTime);
			}
			
			//Se actualiza el monto total de la requisicion
			BigDecimal total = requisicion.getBigDecimal("montoTotal");
			if(total==null)
			{	
				total=BigDecimal.ZERO;				
			}
			BigDecimal totalReq = monto.multiply(cantidad);
			total = total.add(totalReq.multiply(cantidadMeses));
			requisicion.set("montoTotal", total);
			delegator.store(requisicion);
			
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("requisicionId", requisicionId);
			
			return result;
			
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
		
	}
	
	/**
	 * Valida que la combinacion : productId , monto unitario, clave presupuestal , mes (customTimePeriodId) y descripcion no se repita
	 * @param context
	 * @param clavePresupuestal
	 * @param listDetallesRequisicion
	 * @throws GenericEntityException 
	 */
	public static void validaCombinacionUnica(Map<String, Object> context, String clavePresupuestal,
			List<GenericValue> listDetallesRequisicion, String detalleRequisicionId, String customTimePeriodId) throws GenericEntityException {
		
        String productId = (String) context.get("productId");
        BigDecimal monto = (BigDecimal) context.get("monto");
        String descripcion = (String) context.get("descripcion");
        
        for (GenericValue DetalleRequisicion : listDetallesRequisicion) {
        	if(detalleRequisicionId.equals(DetalleRequisicion.getString("detalleRequisicionId"))){
        		continue;
        	}
			if(productId.equals(DetalleRequisicion.getString("productId")) &&
					customTimePeriodId.equals(DetalleRequisicion.getString("customTimePeriodId")) &&
					clavePresupuestal.equals(DetalleRequisicion.getString("clavePresupuestal")) &&
					monto.compareTo(DetalleRequisicion.getBigDecimal("monto")) == 0  &&
					descripcion.toUpperCase().trim().equals(DetalleRequisicion.getString("descripcion").toUpperCase().trim())){
				throw new GenericEntityException("Este registro ya existe en la requisición");
			}
		}
		
	}

	/**
	 * Envia la solicitud a aprobacion de los autorizadores
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> enviarSolicitud(DispatchContext dctx, Map<String, Object> context) 
	{	String estatusEnviada = "ENVIADA";
		String estatusWorkflowPendiente = "PENDIENTE";
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Locale locale = (Locale) context.get("locale");
		String urlHost = (String) context.get("urlHost");
		Map<String, Object> result = ServiceUtil.returnSuccess();		
		String requisicionId = (String) context.get("requisicionId");
		
		try {
			
			//Valida si la requisicion tiene items solicitados
			validaItemsRequisicion(dctx, context, requisicionId);	
			
			//Obtiene el area y la organizacion en base al Id de la requisicion 
			GenericValue requisicion = delegator.findByPrimaryKey("Requisicion", UtilMisc.toMap("requisicionId",requisicionId));			
			String areaPartyId = requisicion.getString("areaPartyId");
			String organizationPartyId = requisicion.getString("organizationPartyId");
			
			//Cambiar estatus de requisicion a ENVIADA
			cambiaStatusRequisicion(dctx, context, requisicionId, estatusEnviada);
			
			//Valida si la requisicion ya tiene un Workflow asociado
			EntityCondition condicionesWorkflowId = EntityCondition.makeCondition(EntityOperator.AND,
								   					EntityCondition.makeCondition("origenId", EntityOperator.EQUALS, requisicionId),
								   					EntityCondition.makeCondition("tipoWorkFlowId", EntityOperator.EQUALS, idTipoWorkflowREQ));		
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
			UtilWorkflow.registroControlWorkflow(dctx, context, requisicionId, idTipoWorkflowREQ, workFlowId);				
					
			GenericValue autorizador = null;
			if(esNuevoWorkflow)
			{	//Obtener autorizador(es) por area. Registro en Status_Workflow (autorizadores, status PENDIENTE y secuencia de autorizadores		
				UtilWorkflow.registroEstatusWorkflow(dctx, context, areaPartyId, organizationPartyId, workFlowId, estatusWorkflowPendiente, idTipoWorkflowREQ);
				autorizador = UtilWorkflow.obtenAutorizador(areaPartyId, organizationPartyId, 1, delegator, idTipoWorkflowREQ);
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
						autorizador = UtilWorkflow.obtenAutorizador(areaPartyId, organizationPartyId, nivel.intValue(), delegator, idTipoWorkflowREQ);
					}
				}
				//Se envian correos de notificacion a los autorizadores anteriores
//				List<GenericValue> statusWorkFlowAuto = UtilWorkflow.getStatusWorkFlow(workFlowId, estatusAutorizada, delegator);
//				for (GenericValue statusWF : statusWorkFlowAuto) {
//					String correoOrigen = requisicion.getString("personaSolicitanteId");
//					String correoDestino = statusWF.getString("personParentId");
//					UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,idTipoWorkflowREQ,CorreoModificado,
//							urlHost,requisicionId,correoOrigen,
//							delegator,locale,dispatcher);
//				}
				
			}
			
			//Enviar correo de notificacion al autorizador
			String correoOrigen = requisicion.getString("personaSolicitanteId");
			String correoDestino = autorizador.getString("autorizadorId");
			UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,idTipoWorkflowREQ,CorreoAutorizacion,
					urlHost,requisicionId,null,
					delegator,locale,dispatcher);
		} catch (GenericEntityException e){
			return ServiceUtil.returnError(e.getMessage());
		}
		return result;
	}
	
	/**
	 * Valida si la requisicion contiene items
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static void validaItemsRequisicion(DispatchContext dctx, Map<?, ?> context, String requisicionId) throws GenericEntityException 
	{	Delegator delegator = dctx.getDelegator();
			EntityCondition condicionesItems = EntityCondition.makeCondition(EntityOperator.AND,
		  	   								   EntityCondition.makeCondition("requisicionId", EntityOperator.EQUALS, requisicionId));		
			List<GenericValue> items = delegator.findByCondition("DetalleRequisicion",condicionesItems, null, null);
			if(items.isEmpty())
			{	
				throw new GenericEntityException("La requisici\u00f3n no contiene articulos");			
			}
	}
		
	/**
	 * Cambia estatus de la requisicion en tabla REQUISICION
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> cambiaStatusRequisicion(DispatchContext dctx, Map<?, ?> context, String requisicionId, String estatus) throws GenericEntityException
	{	Delegator delegator = dctx.getDelegator();
		Timestamp fechaAutorizada = new Timestamp(Calendar.getInstance().getTimeInMillis());
		Map<String, Object> result = ServiceUtil.returnSuccess();
			GenericValue requisicion = delegator.findByPrimaryKey("Requisicion", UtilMisc.toMap("requisicionId",requisicionId));
			requisicion.set("statusId", estatus);	
			requisicion.set("fechaAutorizada", fechaAutorizada);
			delegator.store(requisicion);
		return result;
	}
	
	/**
	 * Cambia estatus de los productos en la tabla REQUISICION
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> cambiaStatusDetallaRequisicion(DispatchContext dctx, Map<?, ?> context, String requisicionId, String estatus) throws GenericEntityException 
	{	Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		try
		{	EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
				  EntityCondition.makeCondition("requisicionId", EntityOperator.EQUALS, requisicionId));					
			List<GenericValue> detalle = delegator.findByCondition("DetalleRequisicion", condiciones, null, null);
			if(!detalle.isEmpty())
			{	Iterator<GenericValue> detalleIter = detalle.iterator();
				while (detalleIter.hasNext())
				{	GenericValue detalleRequisicion = detalleIter.next();								
					detalleRequisicion.set("estatusProducto", estatus);
					delegator.store(detalleRequisicion);					
				}
			}
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
		String requisicionId = (String) context.get("requisicionId");
		String estatusRequisicion = "APROBADA";
		String estatusProductos = "PENDIENTE_P";
		String estatusWorkflowPendiente = "PENDIENTE";
		String comentario = (String) context.get("comentarioAutorizado");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String userLoginpartyId = userLogin.getString("partyId");
		String urlHost = (String) context.get("urlHost");
		String workFlowId;
		String area = "";
		String secuencia = "";
		String nuevoAutorizador = "";
		long nuevaSecuencia = 0;
		try 
		{	GenericValue requisicion = delegator.findByPrimaryKey("Requisicion", UtilMisc.toMap("requisicionId",requisicionId));
			//Cambiar status de STATUS_WORKFLOW para el autorizador en curso*/			
			workFlowId = UtilWorkflow.obtenerWorkFlowId(dctx, context, requisicionId, idTipoWorkflowREQ);
			Debug.log("Omar - workFlowId: " + workFlowId);
			UtilWorkflow.cambiarStatusAutorizadorActual(dctx, context, workFlowId, estatusAutorizada, userLoginpartyId);
		//Validar si contiene otro autorizador
			//Obtener area de la requisicion
			EntityCondition condicionesArea = EntityCondition.makeCondition(EntityOperator.AND,
					  EntityCondition.makeCondition("requisicionId", EntityOperator.EQUALS, requisicionId));
			List<GenericValue> areaSolicitante = delegator.findByCondition("Requisicion", condicionesArea, UtilMisc.toList("areaPartyId"), null);
			Debug.log("Omar - areaSolicitante: " + areaSolicitante);
			if(areaSolicitante.isEmpty())
			{	return ServiceUtil.returnError("La requisicion no contiene con un area solicitante");
			}
			area = areaSolicitante.get(0).getString("areaPartyId");
			Debug.log("Omar - area: " + area);
			
			//Obtener secuencia del autorizador actual
			EntityCondition condicionesSecuencia = EntityCondition.makeCondition(EntityOperator.AND,
					  EntityCondition.makeCondition("areaId", EntityOperator.EQUALS, area),
					  EntityCondition.makeCondition("autorizadorId", EntityOperator.EQUALS, userLoginpartyId),
					  EntityCondition.makeCondition("tipoWorkFlowId", EntityOperator.EQUALS, idTipoWorkflowREQ));
			List<String> orderBy = UtilMisc.toList("secuencia");
			List<GenericValue> secuenciaAutorizador = delegator.findByCondition("Autorizador", condicionesSecuencia, UtilMisc.toList("secuencia"), orderBy);
			Debug.log("Omar - secuenciaAutorizador: " + secuenciaAutorizador);
			if(secuenciaAutorizador.isEmpty())
			{	return ServiceUtil.returnError("El autorizador actual no contiene una secuencia");
			}			
			secuencia = secuenciaAutorizador.get(0).getString("secuencia");
			Debug.log("Omar - secuencia: " + secuencia);
			
			//Validar si contiene otro autorizador obteniendo la secuencia siguiente
			List<String> usuariosActivos = UtilMisc.toList("PARTY_ENABLED", "LEAD_ASSIGNED");
			EntityCondition condicionesNuevoAutorizador = EntityCondition.makeCondition(EntityOperator.AND,
					  EntityCondition.makeCondition("areaId", EntityOperator.EQUALS, area),
					  EntityCondition.makeCondition("secuencia", EntityOperator.GREATER_THAN, secuencia),
					  EntityCondition.makeCondition("statusId", EntityOperator.IN, usuariosActivos),
					  EntityCondition.makeCondition("tipoWorkFlowId", EntityOperator.EQUALS, idTipoWorkflowREQ));
			List<GenericValue> secuenciaNuevoAutorizador = delegator.findByCondition("AutorizadoresActivos", condicionesNuevoAutorizador, null, orderBy);
			Debug.log("Omar - secuenciaNuevoAutorizador: " + secuenciaNuevoAutorizador);
			if(!secuenciaNuevoAutorizador.isEmpty())
			{	//Se crea registro de nuevo autorizador en la tabla STATUS_WORKFLOW
				nuevoAutorizador = secuenciaNuevoAutorizador.get(0).getString("autorizadorId");
				nuevaSecuencia = secuenciaNuevoAutorizador.get(0).getLong("secuencia");
				Debug.log("Omar - nuevoAutorizador: " + nuevoAutorizador);
				Debug.log("Omar - nuevaSecuencia: " + nuevaSecuencia);
				GenericValue statusWorkFlow = GenericValue.create(delegator.getModelEntity("StatusWorkFlow"));
				String statusWorkFlowId = delegator.getNextSeqId("StatusWorkFlow");
				statusWorkFlow.set("statusWorkFlowId", statusWorkFlowId);
				statusWorkFlow.set("workFlowId", workFlowId);
				statusWorkFlow.set("personParentId", nuevoAutorizador);
				statusWorkFlow.set("statusId", estatusWorkflowPendiente);
				statusWorkFlow.set("nivelAutor", UtilFormatOut.formatPaddedNumber(nuevaSecuencia, 4));
				Debug.log("Omar - statusWorkFlow: " + statusWorkFlow);
				delegator.create(statusWorkFlow);		
				String correoOrigen = requisicion.getString("personaSolicitanteId");
				String correoDestino = secuenciaNuevoAutorizador.get(0).getString("autorizadorId");
				//Enviar correo electronico al solicitante
				UtilWorkflow.armarEnviarCorreo(userLoginpartyId,correoOrigen,idTipoWorkflowREQ,CorreoAutorizado,
						urlHost,requisicionId,userLoginpartyId,
						delegator,locale,dispatcher);
				//Enviar correo electronico al nuevo autorizador
				UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,idTipoWorkflowREQ,CorreoAutorizacion,
						urlHost,requisicionId,null,
						delegator,locale,dispatcher);
			}
			else
			{	Debug.log("Omar - cambiaStatusRequisicion");
				cambiaStatusRequisicion(dctx, context, requisicionId, estatusRequisicion);
				Debug.log("Omar - cambiaStatusDetallaRequisicion");
				cambiaStatusDetallaRequisicion(dctx, context, requisicionId, estatusProductos);
				String correoOrigen = userLoginpartyId;
				String correoDestino = requisicion.getString("personaSolicitanteId");
				//Enviar correo electronico al solicitante
				UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,idTipoWorkflowREQ,CorreoAutorizado,
						urlHost,requisicionId,correoOrigen,
						delegator,locale,dispatcher);
				
				//Se envia el correo de notificacion a todos los autorizadores 
//				List<GenericValue> autorizadores = UtilWorkflow.obtenAutorizadores(area, requisicion.getString("organizationPartyId"), delegator, idTipoWorkflowREQ);
//				for (GenericValue autoriza : autorizadores) {
//					correoDestino = autoriza.getString("autorizadorId");
//					UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,idTipoWorkflowREQ,CorreoAutorizadoAuto,
//							urlHost,requisicionId,correoOrigen,
//							delegator,locale,dispatcher);
//				}
			}
			
			//Insertar comentario
			if(comentario != null && !comentario.equals(""))
			{	UtilWorkflow.insertarComentario(dctx, context, comentario, workFlowId, userLoginpartyId, requisicionId);					
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
		String requisicionId = (String) context.get("requisicionId");
		String comentario = (String) context.get("comentarioComentar");
		String workFlowId = "";
		String tipoWorkFlowId = new String();
		String estatusComentada = "COMENTADA";
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String userLoginpartyId = userLogin.getString("partyId");		
		String urlHost = (String) context.get("urlHost");
		try
		{	
			GenericValue requisicion = delegator.findByPrimaryKey("Requisicion", UtilMisc.toMap("requisicionId",requisicionId));
			//Actualizar la requisicion en rechazada
			cambiaStatusRequisicion(dctx, context, requisicionId, estatusComentada);
			
			//Obtener el workFlowId		
			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
					  					  EntityCondition.makeCondition("origenId", EntityOperator.EQUALS, requisicionId),
					  					EntityCondition.makeCondition("tipoWorkFlowId", EntityOperator.EQUALS, idTipoWorkflowREQ));
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
				{	UtilWorkflow.insertarComentario(dctx, context, comentario, workFlowId, userLoginpartyId, requisicionId);					
				}			
			}
			
			//Enviar correo de notificacion al solicitante
			String correoOrigen = userLogin.getString("partyId");
			String correoDestino = requisicion.getString("personaSolicitanteId");
			UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,tipoWorkFlowId,CorreoComentario,
					urlHost,requisicionId,correoOrigen,
					delegator,locale,dispatcher);
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
		String requisicionId = (String) context.get("requisicionId");
		String comentario = (String) context.get("comentarioRechazo");
		String estatusRechazadaW = "RECHAZADA_W";
		String estatusCancelada = "CANCELADA";
		String workFlowId = "";
		String tipoWorkFlowId = new String();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String userLoginpartyId = userLogin.getString("partyId");	
		String urlHost = (String) context.get("urlHost");
		
		try
		{	
			GenericValue requisicion = delegator.findByPrimaryKey("Requisicion", UtilMisc.toMap("requisicionId",requisicionId));
			//Actualizar la requisicion en rechazada
			cambiaStatusRequisicion(dctx, context, requisicionId, estatusCancelada);
			cambiaStatusDetallaRequisicion(dctx, context, requisicionId, estatusCancelada);
			
			//Obtener el workFlowId		
			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
					  					  EntityCondition.makeCondition("origenId", EntityOperator.EQUALS, requisicionId),
					  					  EntityCondition.makeCondition("tipoWorkFlowId", EntityOperator.EQUALS, idTipoWorkflowREQ));
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
				{	UtilWorkflow.insertarComentario(dctx, context, comentario, workFlowId, userLoginpartyId, requisicionId);					
				}			
			}
			
			//Enviar correo de notificacion al solicitante
			String correoOrigen = requisicion.getString("personaSolicitanteId");
			String correoDestino = userLogin.getString("partyId");
			UtilWorkflow.armarEnviarCorreo(correoDestino,correoOrigen,tipoWorkFlowId,CorreoRechazo,
					urlHost,requisicionId,correoDestino,
					delegator,locale,dispatcher);
		}
		catch (GenericEntityException e) 
		{	return ServiceUtil.returnError(e.getMessage());
		}						
		return result;		
	}	
	
	/**
	 * Crea la requisicion
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> editarRequisicion(DispatchContext dctx,
			Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		String requisicionId = (String) context.get("requisicionId");
		String descripcion = (String) context.get("descripcion");
		String justificacion = (String) context.get("justificacion");
		String tipoMoneda = (String) context.get("tipoMoneda");

		try {
			
			GenericValue requisicion = delegator.findByPrimaryKey("Requisicion",
		                UtilMisc.toMap("requisicionId", requisicionId));

			requisicion.set("requisicionId", requisicionId);
			requisicion.set("descripcion", descripcion);
			requisicion.set("justificacion", justificacion);
			requisicion.set("tipoMoneda", tipoMoneda);
			delegator.store(requisicion);
			
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("requisicionId", requisicion.getString("requisicionId"));
			
			return result;
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}
	
	/**
	 * Realiza el traspaso al mes siguiente de los elementos de la requisicion que coincidan con un mes traspasado (Presupuesto)
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map<String, Object> traspasoRequisicion(DispatchContext dctx, Map<String, Object> context) {
		
		
		try{
		
			Delegator delegator = dctx.getDelegator();
			String mesId = (String) context.get("mesId");
			String ciclo = (String) context.get("ciclo");
					
			
			java.util.Date fecha = null;
			java.util.Date fechaSiguiente = null;
			
			fecha = UtilDateTime.toDate(Integer.parseInt(mesId), 1, Integer.parseInt(ciclo), 0, 0, 0);
			fechaSiguiente = UtilDateTime.toDate(Integer.parseInt(mesId)+1, 1, Integer.parseInt(ciclo), 0, 0, 0);
			
			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN_EQUAL_TO,fecha),
					EntityCondition.makeCondition("thruDate", EntityOperator.GREATER_THAN, fecha));
			
			EntityCondition condicionesSiguiente = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN_EQUAL_TO,fechaSiguiente),
					EntityCondition.makeCondition("thruDate", EntityOperator.GREATER_THAN, fechaSiguiente));
			
			EntityCondition condicionesOr = EntityCondition.makeCondition(EntityOperator.OR, condiciones,condicionesSiguiente);
			
			EntityCondition condicionesAnd = EntityCondition.makeCondition(EntityOperator.AND, 
					EntityCondition.makeCondition("periodTypeId",EntityOperator.EQUALS, "FISCAL_MONTH"),condicionesOr);
			
			List<String> orderBy = UtilMisc.toList("fromDate");
			
			List<GenericValue> customTime = delegator.findByCondition("CustomTimePeriod", condicionesAnd, null, orderBy);
			
			if(customTime.size()>2){
				return ServiceUtil.returnError("No se puede realizar el traspaso entre mas de dos meses");
			}
			
			List<String> periodos = new ArrayList<String>();
			
			for(GenericValue timeP : customTime){
				periodos.add(timeP.getString("customTimePeriodId"));
			}
			
			EntityCondition condicionesRequisicion = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("estatusProducto", EntityOperator.LIKE,"PENDIENTE%"),
					EntityCondition.makeCondition("customTimePeriodId", EntityOperator.IN, periodos));
			
			List<String> orderByReq = UtilMisc.toList("requisicionId", "detalleRequisicionId DESC");
			
			List<GenericValue> detalleRequisicion = delegator.findByCondition("DetalleRequisicion", condicionesRequisicion, null, orderByReq);
			
			Map<String,LinkedList<GenericValue>> mapDetalleRequisicion = FastMap.newInstance();
			Map<String,LinkedList<GenericValue>> mapDetalleRequisicionAnterior = FastMap.newInstance();
			Map<String,LinkedList<GenericValue>> mapDetalleRequisicionSiguiente = FastMap.newInstance();
			
			for(GenericValue detReq : detalleRequisicion){
				
				UtilMisc.addToLinkedListInMap(detReq, mapDetalleRequisicion, detReq.getString("requisicionId"),false);
				
				if(detReq.getString("customTimePeriodId").equals(customTime.get(0).getString("customTimePeriodId"))){
					UtilMisc.addToLinkedListInMap(detReq, mapDetalleRequisicionAnterior, detReq.getString("requisicionId"),true);
				}else if(detReq.getString("customTimePeriodId").equals(customTime.get(1).getString("customTimePeriodId"))){
					UtilMisc.addToLinkedListInMap(detReq, mapDetalleRequisicionSiguiente, detReq.getString("requisicionId"),true);
				}
				
			}
			
			
			
			for (Entry<String, LinkedList<GenericValue>> entry : mapDetalleRequisicionAnterior.entrySet())
			{
				
				LinkedList<GenericValue> detalleAnterior = (LinkedList<GenericValue>) entry.getValue();
			    for(GenericValue detAnterior : detalleAnterior){
			    	
			    	boolean flag = false;
			    	LinkedList<GenericValue> detalleSiguiente = (LinkedList<GenericValue>) mapDetalleRequisicionSiguiente.get(detAnterior.getString("requisicionId"));
			    	LinkedList<GenericValue> detalle = (LinkedList<GenericValue>) mapDetalleRequisicion.get(detAnterior.getString("requisicionId"));
			    	
			    	if(!UtilValidate.isEmpty(detalleSiguiente)){
			    		
				    	for(GenericValue detSiguiente: detalleSiguiente){
				    		
				    		if(detAnterior.getString("clavePresupuestal").equals(detSiguiente.getString("clavePresupuestal")) &&
				    				detAnterior.getString("monto").equals(detSiguiente.getString("monto")) &&
				    				detAnterior.getString("productId").equals(detSiguiente.getString("productId")) &&
				    				detAnterior.getString("descripcion").toUpperCase().trim().equals(detSiguiente.getString("descripcion").toUpperCase().trim())){
				    			
				    			detSiguiente.set("cantidad",detSiguiente.getLong("cantidad")+(detAnterior.getLong("cantidad")));
				    			flag = true;
				    			
				    		}
				    	}
			    	}
			    	
			    	if(!flag){
				    	GenericValue nuevoDetalle = (GenericValue) detAnterior.clone();
		    			nuevoDetalle.set("customTimePeriodId", customTime.get(1).getString("customTimePeriodId"));		    			
		    			int numDetReq = Integer.parseInt(detalle.getFirst().getString("detalleRequisicionId"))+1;
		    			String strDetReq = UtilFormatOut.formatPaddedNumber(numDetReq, 4);
		    			nuevoDetalle.set("detalleRequisicionId", strDetReq);
		    			
		    			UtilMisc.addToLinkedListInMap(nuevoDetalle, mapDetalleRequisicion, nuevoDetalle.getString("requisicionId"),true);
		    			UtilMisc.addToLinkedListInMap(nuevoDetalle, mapDetalleRequisicionSiguiente, nuevoDetalle.getString("requisicionId"),true);
			    	}
				    	
			    	detAnterior.set("cantidad", 0);
		    		detAnterior.set("estatusProducto", "TRASPASADO");
			    }
				
			}
			
			List<GenericValue> det = new ArrayList<GenericValue>();
			List<GenericValue> detAnt = new ArrayList<GenericValue>();
			
			for(Entry<String, LinkedList<GenericValue>> entry : mapDetalleRequisicionSiguiente.entrySet()){
				
				det.addAll(entry.getValue());
			}
			
			for(Entry<String, LinkedList<GenericValue>> entry : mapDetalleRequisicionAnterior.entrySet()){
				
				detAnt.addAll(entry.getValue());
			}
			
	//		det = Arrays.asList(mapDetalleRequisicionSiguiente.values().toArray(new GenericValue[mapDetalleRequisicionSiguiente.values().size()]));
	//		detAnt = Arrays.asList(mapDetalleRequisicionAnterior.values().toArray(new GenericValue[mapDetalleRequisicionAnterior.values().size()]));
			
			delegator.storeAll(det);
			delegator.storeAll(detAnt);
			
		} catch(NullPointerException | GenericEntityException e){
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		
		return ServiceUtil.returnSuccess("Se ha realizado el traspaso presupuestal.");
	}
	
	/**
	 * Realiza el traspaso al mes siguiente del monto restante de los detalles del pedido que coincidan con un mes traspasado (Presupuesto)
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map<String, Object> traspasoMontoRestantePedido(DispatchContext dctx, Map<String, Object> context) {
		
		
		try{
		
			Delegator delegator = dctx.getDelegator();
			String mesId = (String) context.get("mesId");
			String ciclo = (String) context.get("ciclo");
					
			
			java.util.Date fecha = null;
			java.util.Date fechaSiguiente = null;
			
			fecha = UtilDateTime.toDate(Integer.parseInt(mesId), 1, Integer.parseInt(ciclo), 0, 0, 0);
			fechaSiguiente = UtilDateTime.toDate(Integer.parseInt(mesId)+1, 1, Integer.parseInt(ciclo), 0, 0, 0);
			
			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN_EQUAL_TO,fecha),
					EntityCondition.makeCondition("thruDate", EntityOperator.GREATER_THAN, fecha));
			
			EntityCondition condicionesSiguiente = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN_EQUAL_TO,fechaSiguiente),
					EntityCondition.makeCondition("thruDate", EntityOperator.GREATER_THAN, fechaSiguiente));
			
			EntityCondition condicionesOr = EntityCondition.makeCondition(EntityOperator.OR, condiciones,condicionesSiguiente);
			
			EntityCondition condicionesAnd = EntityCondition.makeCondition(EntityOperator.AND, 
					EntityCondition.makeCondition("periodTypeId",EntityOperator.EQUALS, "FISCAL_MONTH"),condicionesOr);
			
			List<String> orderBy = UtilMisc.toList("fromDate");
			
			List<GenericValue> customTime = delegator.findByCondition("CustomTimePeriod", condicionesAnd, null, orderBy);
			
			if(customTime.size()>2){
				return ServiceUtil.returnError("No se puede realizar el traspaso entre mas de dos meses");
			}
			
			List<String> periodos = new ArrayList<String>();
			
			for(GenericValue timeP : customTime){
				periodos.add(timeP.getString("customTimePeriodId"));
			}
			
			EntityCondition condicionesPedidoActual = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("statusId", EntityOperator.EQUALS,"ITEM_COMPLETED"),
					EntityCondition.makeCondition("customTimePeriodId", EntityOperator.EQUALS, periodos.get(0)));
			
			EntityCondition condicionesPedidoSiguiente = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("statusId", EntityOperator.EQUALS,"ITEM_APPROVED"),
					EntityCondition.makeCondition("customTimePeriodId", EntityOperator.EQUALS, periodos.get(1)));
			
			List<String> orderByPed = UtilMisc.toList("orderId", "orderItemSeqId DESC");
			
			List<GenericValue> detallePedidoActual = delegator.findByCondition("OrderItem", condicionesPedidoActual, null, orderByPed);
			List<GenericValue> detallePedidoSiguiente = delegator.findByCondition("OrderItem", condicionesPedidoSiguiente, null, orderByPed);
			
			for(GenericValue detAct : detallePedidoActual){
				
				BigDecimal monto = detAct.getBigDecimal("montoRestante");
				
				for(GenericValue detSig : detallePedidoSiguiente){
					
					if(detAct.getString("orderId").equals(detSig.getString("orderId"))&&
							detAct.getString("productId").equals(detSig.getString("productId"))&&
							detAct.getBigDecimal("unitPrice").equals(detSig.getBigDecimal("unitPrice"))){
						
						if(UtilValidate.isNotEmpty(detSig.getBigDecimal("montoRestante"))){
							detSig.set("montoRestante", monto.add(detSig.getBigDecimal("montoRestante")).add(detSig.getBigDecimal("selectedAmount")));
						}else{
							detSig.set("montoRestante", monto.add(detSig.getBigDecimal("selectedAmount")));
						}					

						detAct.set("montoRestante", BigDecimal.ZERO);
						detAct.store();
						detSig.store();
						break;
					}
					
				}
				
			}
			
			
			
		} catch(NullPointerException | GenericEntityException e){
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		
		return ServiceUtil.returnSuccess("Se ha realizado el traspaso presupuestal.");
	}
}