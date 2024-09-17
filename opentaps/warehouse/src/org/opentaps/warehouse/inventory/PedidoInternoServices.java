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
package org.opentaps.warehouse.inventory;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

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
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.common.builder.ListBuilderException;
import org.opentaps.common.party.PartyHelper;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.foundation.action.ActionContext;
import org.opentaps.foundation.repository.RepositoryException;


/**
 *	Servicios de PedidoInterno
 */

public class PedidoInternoServices {

	public final static String CorreoAutorizacion = "CorreoMensajeAutorizar";
	public final static String CorreoComentario = "CorreoMensajeComentario";
	public final static String CorreoAutorizado = "CorreoMensajeAutorizado";
	public final static String CorreoAutorizadoAuto = "CorreoMensajeAutorizadoAutorizador";
	public final static String CorreoRechazo = "CorreoMensajeRechazo";
	public final static String CorreoModificado = "CorreoMensajeModificado";

	public final static String tipoWorkFlowId = "PEDIDO_INTERNO"; //PedidoInternoes

	public final static String estatusComentadaW = "COMENTADA_W"; //StatusWorkFlow
	public final static String estatusAutorizada = "AUTORIZADA"; //StatusWorkFlow

	private static final String MODULE = PedidoInternoServices.class.getName();
	/**
	 * Crea la pedidoInterno
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> guardaPedidoInterno(DispatchContext dctx,
			Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		String pedidoInternoId = (String) context.get("pedidoInternoId");
		String descripcion = (String) context.get("descripcion");
		String justificacion = (String) context.get("justificacion");
		String almacenId = (String) context.get("almacenId");
		String areaPartyId = (String) context.get("areaPartyId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");

		try {

			GenericValue pedidoInterno = GenericValue.create(delegator.getModelEntity("PedidoInterno"));
			if(pedidoInternoId != null && !pedidoInternoId.isEmpty())
				pedidoInterno.set("pedidoInternoId", pedidoInternoId);
			else
				pedidoInterno.setNextSeqId();

			pedidoInterno.set("descripcion", descripcion);
			pedidoInterno.set("justificacion", justificacion);
			pedidoInterno.set("almacenId", almacenId);
			//CHUNDADA
			//pedidoInterno.set("organizationPartyId", organizationPartyId);
			pedidoInterno.set("organizationPartyId", "1");
			pedidoInterno.set("statusId", "CREADA_PI");
			pedidoInterno.set("personaSolicitanteId",
					userLogin.getString("partyId"));
			pedidoInterno.set("areaPartyId",areaPartyId);
			delegator.create(pedidoInterno);
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("pedidoInternoId", pedidoInterno.getString("pedidoInternoId"));
			return result;
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}

	/**
	 * Agrega un articulo a la pedidoInterno , realizando validaciones necesarias
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> agregaItemPedidoInterno(DispatchContext dctx,
			Map<?, ?> context){

		Delegator delegator = dctx.getDelegator();
		String pedidoInternoId = (String) context.get("pedidoInternoId");
		BigDecimal cantidad = (BigDecimal) context.get("cantidad");
		String productId = (String) context.get("productId");

		try {

			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("pedidoInternoId", EntityOperator.EQUALS, pedidoInternoId));
			List<GenericValue> detallesPedidoInterno = delegator.findByCondition("DetallePedidoInterno",condiciones, UtilMisc.toList("pedidoInternoId", "detallePedidoInternoId"), null);

			String detallePedidoInternoId = "";
			long detalleId = 0;
			Iterator<GenericValue> detallesPedidoInternoIter = detallesPedidoInterno.iterator();
			while (detallesPedidoInternoIter.hasNext()) 
			{	GenericValue generic = detallesPedidoInternoIter.next();
			detallePedidoInternoId = generic.getString("detallePedidoInternoId");
			detalleId = Long.parseLong(detallePedidoInternoId);
			}

			GenericValue detPedidoInterno = GenericValue.create(delegator.getModelEntity("DetallePedidoInterno"));
			detPedidoInterno.set("pedidoInternoId", pedidoInternoId);
			detPedidoInterno.set("detallePedidoInternoId", UtilFormatOut.formatPaddedNumber((detalleId + 1), 4));
			detPedidoInterno.set("cantidad", cantidad);
			detPedidoInterno.set("productId", productId);
			detPedidoInterno.set("cantidadEntregada", BigDecimal.ZERO);

			delegator.create(detPedidoInterno);

			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("pedidoInternoId", pedidoInternoId);

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
	public static Map<String, Object> enviarPedidoInterno(DispatchContext dctx, Map<String, Object> context) throws GenericEntityException 
	{	
		//RUME
		String estatusEnviada = "ENVIADA_PI";
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();		
		String pedidoInternoId = (String) context.get("pedidoInternoId");
		String estatusWorkflowPendiente = "PENDIENTE";

		//Valida si el pedidoInterno tiene items solicitados
		validaItemsPedidoInterno(dctx, context, pedidoInternoId);						

		//Obtiene el area y la organizacion en base al Id de la pedidoInterno 
		GenericValue pedidoInterno = delegator.findByPrimaryKey("PedidoInterno", UtilMisc.toMap("pedidoInternoId",pedidoInternoId));
		String areaPartyId = pedidoInterno.getString("areaPartyId");
		String organizationPartyId = pedidoInterno.getString("organizationPartyId");

		//Cambiar estatus de pedidoInterno a ENVIADA
		cambiaStatusPedidoInterno(dctx, context, pedidoInternoId, estatusEnviada);

		//Valida si el pedido interno ya tiene un Workflow asociado
		EntityCondition condicionesWorkflowId = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("origenId", EntityOperator.EQUALS, pedidoInternoId),
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
		UtilWorkflow.registroControlWorkflow(dctx, context, pedidoInternoId, tipoWorkFlowId, workFlowId);				

		if(esNuevoWorkflow)
		{	//Obtener autorizador(es) por area. Registro en Status_Workflow (autorizadores, status PENDIENTE y secuencia de autorizadores		
			UtilWorkflow.registroEstatusWorkflow(dctx, context, areaPartyId, organizationPartyId, workFlowId, estatusWorkflowPendiente, tipoWorkFlowId);
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
			}
			}
		}

		return result;

	}




	/**
	 * Valida si la pedidoInterno contiene items
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> validaItemsPedidoInterno(DispatchContext dctx, Map<?, ?> context, String pedidoInternoId) throws GenericEntityException 
	{	Delegator delegator = dctx.getDelegator();
	Map<String, Object> result = ServiceUtil.returnSuccess();
	try
	{	EntityCondition condicionesItems = EntityCondition.makeCondition(EntityOperator.AND,
			EntityCondition.makeCondition("pedidoInternoId", EntityOperator.EQUALS, pedidoInternoId));		
	List<GenericValue> items = delegator.findByCondition("DetallePedidoInterno",condicionesItems, null, null);
	if(items.isEmpty())
	{	return ServiceUtil.returnError("El pedido interno no contiene articulos");			
	}
	}
	catch (GenericEntityException e) 
	{	return ServiceUtil.returnError(e.getMessage());
	}
	return result;
	}

	/**
	 * Cambia estatus de la pedidoInterno en tabla PEDIDOINTERNO
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> cambiaStatusPedidoInterno(DispatchContext dctx, Map<?, ?> context, String pedidoInternoId, String estatus) throws GenericEntityException 
	{	
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		try
		{	
			GenericValue pedidoInterno = delegator.findByPrimaryKey("PedidoInterno", UtilMisc.toMap("pedidoInternoId",pedidoInternoId));
			pedidoInterno.set("statusId", estatus);	
			delegator.store(pedidoInterno);
				
			if(estatus.equals("ATENDIDA")){
				Map<String,Object> can = cambiarCantidades(delegator, pedidoInternoId);
				if (ServiceUtil.isError(can)) {
	            	return ServiceUtil.returnError(ServiceUtil.getErrorMessage(can));
				}
			}
		} catch (GenericEntityException e) {	
			return ServiceUtil.returnError(e.getMessage());
		}
		return result;
	}

	
	/**
	 * Cambia las cantidades que pueden ser entregadas 
	 * al usuario
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String,Object> cambiarCantidades(Delegator delegator, String pedidoInternoId) throws GenericEntityException{
		Map<String, Object> result = ServiceUtil.returnSuccess();
		
		List<GenericValue> detallesPedido = delegator.findByAnd("DetallePedidoInterno", "pedidoInternoId", pedidoInternoId);
		Iterator<GenericValue> detallesPedidoInternoIter = detallesPedido.iterator();
		while(detallesPedidoInternoIter.hasNext()){
			GenericValue generic = detallesPedidoInternoIter.next();
			if(generic.getString("requisicionId") != null){
				GenericValue det = delegator.findByPrimaryKey("DetalleRequisicion",
        				UtilMisc.toMap("detalleRequisicionId", generic.getString("detalleRequisicionId"),
        						"requisicionId", generic.getString("requisicionId")));
				Long cantidad = generic.getLong("cantidad");
				Long cantidadMax = det.getLong("cantidad") - det.getLong("cantidadEntregada");
				if(cantidad == cantidadMax || cantidad <= cantidadMax){
					det.set("cantidadEntregada", cantidad + det.getLong("cantidadEntregada"));
					delegator.store(det);			
				}else{
					return ServiceUtil.returnError("La cantidad ingresada para el producto "+det.getString("productId")+" es superiror a la que se encuentra disponible");
				}
				cambiaStatusRequisicion(delegator, generic.getString("requisicionId"));
			}else if(generic.getString("orderId") != null){
				GenericValue oi = delegator.findByPrimaryKey("OrderItem",
        				UtilMisc.toMap("orderId", generic.getString("orderId"),
        						"orderItemSeqId", generic.getString("orderItemSeqId")));
				Long cantidad = generic.getLong("cantidad");
				Long cantidadMax = oi.getBigDecimal("quantity").longValue() - oi.getLong("cantidadEntregada");
				if(cantidad == cantidadMax || cantidad <= cantidadMax){
					if(oi.getString("requisicionId") != null){
						GenericValue det = delegator.findByPrimaryKey("DetalleRequisicion",
		        				UtilMisc.toMap("detalleRequisicionId", oi.getString("detalleRequisicionId"),
		        						"requisicionId", oi.getString("requisicionId")));
						Long cantidadR = generic.getLong("cantidad");
						if(cantidad == cantidadMax || cantidad <= cantidadMax){
							det.set("cantidadEntregada", cantidadR + det.getLong("cantidadEntregada"));
							delegator.store(det);		
							oi.set("cantidadEntregada", cantidadR + oi.getLong("cantidadEntregada"));
							delegator.store(oi);
						}else{
							return ServiceUtil.returnError("La cantidad ingresada para el producto "+det.getString("productId")+" es superiror a la que se encuentra disponible dentro de la requisicion "+det.getString("requisicionId"));
						}
						cambiaStatusRequisicion(delegator, oi.getString("requisicionId"));
					}else{
						oi.set("cantidadEntregada", cantidad + oi.getLong("cantidadEntregada"));
						delegator.store(oi);
					}			
				}else{
					return ServiceUtil.returnError("La cantidad ingresada para el producto "+oi.getString("productId")+" es superiror a la que se encuentra disponible");
				}
				cambiaStatusOrden(delegator, generic.getString("orderId"));
			}
		}
		return result;
	}
	
	/**
	 * cambia el estatus a la requisicion
	 * @throws GenericEntityException 
	 */
	public static void cambiaStatusRequisicion(Delegator delegator, String requisicionId) throws GenericEntityException{

		GenericValue requisicion = delegator.findByPrimaryKey("Requisicion", UtilMisc.toMap("requisicionId", requisicionId));
		List<GenericValue> detallesRequisicion = delegator.findByAnd("DetalleRequisicion", "requisicionId", requisicionId);

		long cantidadesEntregadas = 0;
		Iterator<GenericValue> detallesRequisicionIter = detallesRequisicion.iterator();
		while (detallesRequisicionIter.hasNext()) 
		{	GenericValue generic = detallesRequisicionIter.next();
			cantidadesEntregadas = cantidadesEntregadas + (generic.getLong("cantidad") - generic.getLong("cantidadEntregada"));
		}
		
		if(cantidadesEntregadas == 0){
			requisicion.set("statusPedidoId", "REQ_ALM_SURTIDA");
			delegator.store(requisicion);
		}
	}
	
	/**
	 * cambia el estatus a la orden
	 * @throws GenericEntityException 
	 */
	public static void cambiaStatusOrden(Delegator delegator, String orderId) throws GenericEntityException{

		Debug.log("mike cambio de estatus de la orden");
		GenericValue orden = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
		List<GenericValue> detallesOrden = delegator.findByAnd("OrderItem", "orderId", orderId);

		long cantidadesEntregadas = 0;
		Iterator<GenericValue> detallesOrdenIter = detallesOrden.iterator();
		while (detallesOrdenIter.hasNext()) 
		{	GenericValue generic = detallesOrdenIter.next();
			cantidadesEntregadas = cantidadesEntregadas + (generic.getBigDecimal("quantity").longValue() - generic.getLong("cantidadEntregada"));
		}
		
		if(cantidadesEntregadas == 0){
			orden.set("statusPedidoId", "REQ_ALM_SURTIDA");
			delegator.store(orden);
		}
	}
	/**
	 * Se genera un registro en tabla CONTROL_WORKFLOW para el manejo del workflow
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> registroControlWorkflow(DispatchContext dctx, Map<?, ?> context, String pedidoInternoId, String tipoWorkflow, String idWorkflow) throws GenericEntityException 
	{	Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();		
		try
		{	GenericValue controlWorkflow = GenericValue.create(delegator.getModelEntity("ControlWorkFlow"));		
			controlWorkflow.set("workFlowId", idWorkflow);
			controlWorkflow.set("pedidoInternoId", pedidoInternoId);		
			controlWorkflow.set("tipoWorkFlowId", tipoWorkflow);
			delegator.create(controlWorkflow);
		}
		catch (GenericEntityException e) 
		{	
			return ServiceUtil.returnError(e.getMessage());
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
	public static Map<String, Object> registroEstatusWorkflow(DispatchContext dctx, Map<?, ?> context, String areaPartyId, String organizationPartyId, String idWorkflow, String estatus) throws GenericEntityException 
	{	Delegator delegator = dctx.getDelegator();
	Map<String, Object> result = ServiceUtil.returnSuccess();
	String estatusWorkflowVar = estatus;
	try
	{		
		List<GenericValue> autorizadores = obtenAutorizadores(areaPartyId, organizationPartyId, delegator);
		if(autorizadores.isEmpty())
		{	
			return ServiceUtil.returnError("El solicitante no tiene configurado un autorizador");
		}
		else
		{	Iterator<GenericValue> autorizadoresIter = autorizadores.iterator();
			if(autorizadoresIter.hasNext())
			{	GenericValue generic = autorizadoresIter.next();
				GenericValue statusWorkFlow = GenericValue.create(delegator.getModelEntity("StatusWorkFlow"));
				String statusWorkFlowId = delegator.getNextSeqId("StatusWorkFlow");
				statusWorkFlow.set("statusWorkFlowId", statusWorkFlowId);
				statusWorkFlow.set("workFlowId", idWorkflow);
				statusWorkFlow.set("personParentId", generic.getString("autorizadorId"));
				statusWorkFlow.set("statusId", estatusWorkflowVar);
				statusWorkFlow.set("nivelAutor", UtilFormatOut.formatPaddedNumber(generic.getLong("secuencia"), 4));
				delegator.create(statusWorkFlow);		
			}
		}
	}
	catch (GenericEntityException e) 
	{	return ServiceUtil.returnError(e.getMessage());
	}
	return result;
	}

	/**
	 * Metodo que obtiene los autorizadores de un area 
	 * @param areaPartyId
	 * @param organizationPartyId
	 * @param delegator
	 * @return
	 * @throws GenericEntityException
	 */
	private static List<GenericValue> obtenAutorizadores(String areaPartyId, 
			String organizationPartyId,Delegator delegator) throws GenericEntityException{

		EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("areaId", EntityOperator.EQUALS, areaPartyId),
				EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS, organizationPartyId));
		List<String> orderBy = UtilMisc.toList("secuencia");
		Debug.log("Omar - areaPartyId: " + areaPartyId);
		Debug.log("Omar - organizationPartyId: " + organizationPartyId);
		List<GenericValue> autorizadores = delegator.findByCondition("Autorizador",condiciones, 
				UtilMisc.toList("autorizadorId","secuencia"), orderBy);

		return autorizadores;
	}

	/**
	 * Se obtienen los autorizadores por area. 
	 * Se genera un registro en Estatus_Workflow (autorizadores, status PENDIENTE y jerarquia de autorizadores)
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> autorizarPedidoInterno(DispatchContext dctx, Map<?, ?> context) throws GenericEntityException
	{	Delegator delegator = dctx.getDelegator();
		String comentario = (String) context.get("comentarioAutorizado");
		Map<String, Object> result = ServiceUtil.returnSuccess();
		String pedidoInternoId = (String) context.get("pedidoInternoId");
		String estatusPedidoInterno = "ATENDIDA";
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String userLoginpartyId = userLogin.getString("partyId");
		Debug.log("autorizarPedido");
		String estatusWorkflowPendiente = "PENDIENTE";
		String workFlowId;
		String area = "";
		String secuencia = "";
		String nuevoAutorizador = "";
		long nuevaSecuencia = 0;
		GenericValue autorizador = null;


		try 
		{
			//Cambiar status de STATUS_WORKFLOW para el autorizador en curso*/			
			workFlowId = UtilWorkflow.obtenerWorkFlowId(dctx, context, pedidoInternoId, tipoWorkFlowId);
			UtilWorkflow.cambiarStatusAutorizadorActual(dctx, context, workFlowId, estatusAutorizada, userLoginpartyId);
			//Validar si contiene otro autorizador
			//Obtener area de el pedidoInterno
			EntityCondition condicionesArea = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("pedidoInternoId", EntityOperator.EQUALS, pedidoInternoId));
			List<GenericValue> areaSolicitante = delegator.findByCondition("PedidoInterno", condicionesArea, UtilMisc.toList("areaPartyId"), null);
			Debug.log("Omar - areaSolicitante: " + areaSolicitante);
			if(areaSolicitante.isEmpty())
			{	return ServiceUtil.returnError("el pedidoInterno no contiene con un area solicitante");
			}
			area = areaSolicitante.get(0).getString("areaPartyId");
			Debug.log("Omar - area: " + area);
	
			//Obtener secuencia del autorizador actual
			EntityCondition condicionesSecuencia = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("areaId", EntityOperator.EQUALS, area),
					EntityCondition.makeCondition("autorizadorId", EntityOperator.EQUALS, userLoginpartyId),
					EntityCondition.makeCondition("tipoWorkFlowId", EntityOperator.EQUALS, tipoWorkFlowId));
			List<String> orderBy = UtilMisc.toList("secuencia");
			List<GenericValue> secuenciaAutorizador = delegator.findByCondition("Autorizador", condicionesSecuencia, UtilMisc.toList("secuencia"), orderBy);
			if(secuenciaAutorizador.isEmpty())
			{	return ServiceUtil.returnError("El autorizador actual no contiene una secuencia");
			}			
			secuencia = secuenciaAutorizador.get(0).getString("secuencia");
	
			//Validar si contiene otro autorizador obteniendo la secuencia siguiente
			List<String> usuariosActivos = UtilMisc.toList("PARTY_ENABLED", "LEAD_ASSIGNED");
			EntityCondition condicionesNuevoAutorizador = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("areaId", EntityOperator.EQUALS, area),
					EntityCondition.makeCondition("secuencia", EntityOperator.GREATER_THAN, secuencia),
					EntityCondition.makeCondition("statusId", EntityOperator.IN, usuariosActivos),
					EntityCondition.makeCondition("tipoWorkFlowId", EntityOperator.EQUALS, tipoWorkFlowId));
			List<GenericValue> secuenciaNuevoAutorizador = delegator.findByCondition("AutorizadoresActivos", condicionesNuevoAutorizador, null, orderBy);
			if(!secuenciaNuevoAutorizador.isEmpty())
			{	//Se crea registro de nuevo autorizador en la tabla STATUS_WORKFLOW
				
				for (GenericValue genericValue : secuenciaNuevoAutorizador) {
					if(tipoWorkFlowId.equals("PEDIDO_INTERNO")){
						if(UtilWorkflow.validaAlmacenista(delegator, genericValue.getString("autorizadorId"), workFlowId)){
							autorizador = genericValue;
							break;
						}
					}else{
						autorizador = genericValue;
						break;
					}
				}
			}
			
			Debug.logInfo("autorizarPedidoInterno autorizador: "+autorizador,MODULE);
			
			if(UtilValidate.isNotEmpty(autorizador))
			{	
				nuevoAutorizador = autorizador.getString("autorizadorId");
				nuevaSecuencia = autorizador.getLong("secuencia");
				GenericValue statusWorkFlow = GenericValue.create(delegator.getModelEntity("StatusWorkFlow"));
				String statusWorkFlowId = delegator.getNextSeqId("StatusWorkFlow");
				statusWorkFlow.set("statusWorkFlowId", statusWorkFlowId);
				statusWorkFlow.set("workFlowId", workFlowId);
				statusWorkFlow.set("personParentId", nuevoAutorizador);
				statusWorkFlow.set("statusId", estatusWorkflowPendiente);
				statusWorkFlow.set("nivelAutor", UtilFormatOut.formatPaddedNumber(nuevaSecuencia, 4));
				Debug.log("Omar - statusWorkFlow: " + statusWorkFlow);
				delegator.create(statusWorkFlow);		
			}
			else
			{	
				Map<String,Object> resultCambiaEstatus = cambiaStatusPedidoInterno(dctx, context, pedidoInternoId, estatusPedidoInterno);
				if (ServiceUtil.isError(resultCambiaEstatus)) {
	                return ServiceUtil.returnError(ServiceUtil.getErrorMessage(resultCambiaEstatus));
	            }
			}
			//Insertar comentario
			if(comentario != null && !comentario.equals(""))
			{	
				insertarComentario(dctx, context, comentario, userLoginpartyId, pedidoInternoId);
			}			
	
		} catch (GenericEntityException e)
		{	
			e.printStackTrace();
		}						
	return result;



	}

	/**
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> comentarPedidoInterno(DispatchContext dctx, Map<?, ?> context) throws GenericEntityException 
	{	Delegator delegator = dctx.getDelegator();
	Map<String, Object> result = ServiceUtil.returnSuccess();
	String pedidoInternoId = (String) context.get("pedidoInternoId");
	String comentario = (String) context.get("comentarioComentar");
	String rol = (String) context.get("rol");
	Debug.log("rol.- " + rol);
	String estatus = rol.equalsIgnoreCase("AUTORIZADOR")?"COMENTADA_PI":"ENVIADA_PI";
	GenericValue userLogin = (GenericValue) context.get("userLogin");
	String userLoginpartyId = userLogin.getString("partyId");		
	String workFlowId = "";
	Debug.log("comentarPedido");		
	try
	{	
		//Actualizar la pedidoInterno en comentada
		cambiaStatusPedidoInterno(dctx, context, pedidoInternoId, estatus);

		//Insertar comentario
		if(comentario != null && !comentario.equals(""))
		{	insertarComentario(dctx, context, comentario, userLoginpartyId, pedidoInternoId);					
		}

		//Obtener el workFlowId		
		EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("origenId", EntityOperator.EQUALS, pedidoInternoId),
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
		}			
		//Actualizar STATUS_WORK_FLOW en rechazado SOLO para el autorizador actual
		if(!workFlowId.equals(""))
		{	UtilWorkflow.cambiarStatusAutorizadorActual(dctx, context, workFlowId, estatusComentadaW, userLoginpartyId);
		}				
		//Insertar comentario
		if(comentario != null && !comentario.equals(""))
		{	UtilWorkflow.insertarComentario(dctx, context, comentario, workFlowId, userLoginpartyId, pedidoInternoId);					
		}			
		}

		//Enviar correo de notificacion al solicitante
		//			String correoOrigen = userLogin.getString("partyId");
		//			String correoDestino = pedidoInterno.getString("personaSolicitanteId");
		//			armarEnviarCorreo(correoOrigen,correoDestino,tipoWorkFlowId,CorreoComentario,
		//					urlHost,pedidoInternoId,delegator,locale,dispatcher);
	}
	catch (GenericEntityException e) 
	{	return ServiceUtil.returnError(e.getMessage());
	}						
	return result;
	}

	/**
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> rechazarPedidoInterno(DispatchContext dctx, Map<?, ?> context) throws GenericEntityException 
	{	Delegator delegator = dctx.getDelegator();
	Map<String, Object> result = ServiceUtil.returnSuccess();
	String pedidoInternoId = (String) context.get("pedidoInternoId");
	String comentario = (String) context.get("comentarioRechazo");
	String estatusRechazada = "RECHAZADA_PI";
	GenericValue userLogin = (GenericValue) context.get("userLogin");
	String userLoginpartyId = userLogin.getString("partyId");	
	String estatusRechazadaW = "RECHAZADA_W";
	String workFlowId = "";
	Debug.log("rechazarPedido");
	try
	{	
		//Actualizar la pedidoInterno en rechazada
		cambiaStatusPedidoInterno(dctx, context, pedidoInternoId, estatusRechazada);

		//Insertar comentario
		if(comentario != null && !comentario.equals(""))
		{	insertarComentario(dctx, context, comentario, userLoginpartyId, pedidoInternoId);					
		}			

		//Obtener el workFlowId		
		EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("origenId", EntityOperator.EQUALS, pedidoInternoId),
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
		}			
		//Actualizar STATUS_WORK_FLOW en rechazado para todos los autorizadores
		if(!workFlowId.equals(""))
		{	UtilWorkflow.cambiarStatusAutorizadores(dctx, context, workFlowId, estatusRechazadaW);
		}				
		//Insertar comentario
		if(comentario != null && !comentario.equals(""))
		{	UtilWorkflow.insertarComentario(dctx, context, comentario, workFlowId, userLoginpartyId, pedidoInternoId);					
		}			
		}

		//Borramos la relacion de Requicision y/o Orden de cobro que pudieran existir.
		List <GenericValue> requisiciones = delegator.findByAnd("Requisicion", "pedidoInternoId", pedidoInternoId);
		List <GenericValue> ordenes = delegator.findByAnd("OrderHeader", "pedidoInternoId", pedidoInternoId);

		for(GenericValue req : requisiciones){
			req.set("pedidoInternoId", null);
			delegator.store(req);
		}

		for(GenericValue ord : ordenes){
			ord.set("pedidoInternoId", null);
			delegator.store(ord);
		}

	}
	catch (GenericEntityException e) 
	{	return ServiceUtil.returnError(e.getMessage());
	}						
	return result;

	}


	/**
	 * Se crea un registro de comentario en la entidad ComentarioPedidoInterno 
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> insertarComentario(DispatchContext dctx, Map<?, ?> context, String comentario, String userLoginPartyId, String pedidoInternoId) throws GenericEntityException 
	{	Delegator delegator = dctx.getDelegator();
	Map<String, Object> result = ServiceUtil.returnSuccess();						
	try
	{	GenericValue comentarioPedidoInterno = GenericValue.create(delegator.getModelEntity("ComentarioPedidoInterno"));
	String comentarioId = delegator.getNextSeqId("ComentarioPedidoInterno");
	comentarioPedidoInterno.set("comentarioId", comentarioId);
	comentarioPedidoInterno.set("pedidoInternoId", pedidoInternoId);
	comentarioPedidoInterno.set("comentario", comentario);
	comentarioPedidoInterno.set("personaId", userLoginPartyId);
	delegator.create(comentarioPedidoInterno);				
	}
	catch (GenericEntityException e) 
	{	return ServiceUtil.returnError(e.getMessage());
	}

	return result;
	}

	/**
	 * Agrega una requisicion al pedidoInterno , realizando validaciones necesarias
	 * @param dctx
	 * @param context
	 * @return 
	 */
	public static Map<String, Object> agregaRequisicionPedidoInterno(DispatchContext dctx,
			Map<?, ?> context){

		Delegator delegator = dctx.getDelegator();
		String pedidoInternoId = (String) context.get("pedidoInternoId");
		String requisicionId = (String) context.get("requisicionId");

		try {

			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("pedidoInternoId", EntityOperator.EQUALS, pedidoInternoId));
			List<GenericValue> detallesPedidoInterno = delegator.findByCondition("DetallePedidoInterno",condiciones, UtilMisc.toList("pedidoInternoId", "detallePedidoInternoId"), null);

			GenericValue requisicion = delegator.findByPrimaryKey("Requisicion", UtilMisc.toMap("requisicionId", requisicionId));
			List<GenericValue> detallesRequisicion = delegator.findByAnd("DetalleRequisicion", "requisicionId", requisicionId);

			 if(requisicion.getString("statusPedidoId") != null && requisicion.getString("statusPedidoId").equals("REQ_ALM_SURTIDA")){
				 return ServiceUtil.returnError("La requisici\u00f3n se ha surtido por completo, por lo cual no puede utilizarse");
			 }
			
			String detallePedidoInternoId = "";
			long detalleId = 0;
			Iterator<GenericValue> detallesPedidoInternoIter = detallesPedidoInterno.iterator();
			while (detallesPedidoInternoIter.hasNext()) 
			{	
				GenericValue generic = detallesPedidoInternoIter.next();
				detallePedidoInternoId = generic.getString("detallePedidoInternoId");
				detalleId = Long.parseLong(detallePedidoInternoId);
			}

			for(GenericValue req : detallesRequisicion){
				detalleId++;
				GenericValue detPedidoInterno = GenericValue.create(delegator.getModelEntity("DetallePedidoInterno"));
				detPedidoInterno.set("pedidoInternoId", pedidoInternoId);
				detPedidoInterno.set("detallePedidoInternoId", UtilFormatOut.formatPaddedNumber((detalleId), 4));
				detPedidoInterno.set("productId", req.getString("productId"));
				detPedidoInterno.set("cantidadEntregada", BigDecimal.ZERO);
				detPedidoInterno.set("detalleRequisicionId", req.getString("detalleRequisicionId"));
				detPedidoInterno.set("requisicionId", req.getString("requisicionId"));

				if(requisicion.getString("statusPedidoId") == null){
					detPedidoInterno.set("cantidad", req.get("cantidad"));
					delegator.create(detPedidoInterno);
				}else{
					detPedidoInterno.set("cantidad", req.getLong("cantidad") - req.getLong("cantidadEntregada"));
					delegator.create(detPedidoInterno);
				}
			}

			requisicion.set("pedidoInternoId", pedidoInternoId);
			delegator.store(requisicion);

			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("pedidoInternoId", pedidoInternoId);

			return result;

		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}

	}

	/**
	 * Agrega una requisicion al pedidoInterno , realizando validaciones necesarias
	 * @param dctx
	 * @param context
	 * @return 
	 */
	public static Map<String, Object> agregaOrdenPedidoInterno(DispatchContext dctx,
			Map<?, ?> context){

		Delegator delegator = dctx.getDelegator();
		String pedidoInternoId = (String) context.get("pedidoInternoId");
		String orderId = (String) context.get("orderId");

		try {

			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("pedidoInternoId", EntityOperator.EQUALS, pedidoInternoId));
			List<GenericValue> detallesPedidoInterno = delegator.findByCondition("DetallePedidoInterno",condiciones, UtilMisc.toList("pedidoInternoId", "detallePedidoInternoId"), null);

			GenericValue orden = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
			List<GenericValue> items = delegator.findByAnd("OrderItem", "orderId", orderId);
			
			if(orden.getString("statusPedidoId") != null && orden.getString("statusPedidoId").equals("REQ_ALM_SURTIDA")){
				 return ServiceUtil.returnError("La orden se ha surtido por completo, por lo cual no puede utilizarse");
			 }

			String detallePedidoInternoId = "";
			long detalleId = 0;
			Iterator<GenericValue> detallesPedidoInternoIter = detallesPedidoInterno.iterator();
			while (detallesPedidoInternoIter.hasNext()) 
			{	GenericValue generic = detallesPedidoInternoIter.next();
			detallePedidoInternoId = generic.getString("detallePedidoInternoId");
			detalleId = Long.parseLong(detallePedidoInternoId);
			}

			for(GenericValue item : items){
				detalleId++;
				GenericValue detPedidoInterno = GenericValue.create(delegator.getModelEntity("DetallePedidoInterno"));
				detPedidoInterno.set("pedidoInternoId", pedidoInternoId);
				detPedidoInterno.set("detallePedidoInternoId", UtilFormatOut.formatPaddedNumber((detalleId), 4));
				detPedidoInterno.set("productId", item.getString("productId"));
				detPedidoInterno.set("cantidadEntregada", BigDecimal.ZERO);
				detPedidoInterno.set("orderId", item.getString("orderId"));
				detPedidoInterno.set("orderItemSeqId", item.getString("orderItemSeqId"));
				
				Long quantity = item.getBigDecimal("quantity").longValue();
				
				if(orden.getString("statusPedidoId") == null){
					detPedidoInterno.set("cantidad", item.get("quantity"));
					delegator.create(detPedidoInterno);
				}else{
					detPedidoInterno.set("cantidad", quantity - item.getLong("cantidadEntregada"));
					delegator.create(detPedidoInterno);
				}
			}

			orden.set("pedidoInternoId", pedidoInternoId);
			delegator.store(orden);

			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("pedidoInternoId", pedidoInternoId);

			return result;

		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}

	}

	/**
	 * Realiza la busqueda de los pedidos internos para surtirlos
	 * @param context
	 * @throws RepositoryException
	 * @throws ListBuilderException
	 * @throws ParseException
	 * @throws GenericEntityException 
	 */
	public static void buscaPedidoInterno(Map<String, Object> context) throws RepositoryException, ListBuilderException, ParseException, GenericEntityException {

		final ActionContext ac = new ActionContext(context);
		final TimeZone timeZone = ac.getTimeZone();
		Locale locale = ac.getLocale();

		final Delegator delegator = ac.getDelegator();

		String almacenId = ac.getParameter("almacenId");
		String pedidoInternoId = ac.getParameter("pedidoInternoId");
		String descripcion = ac.getParameter("descripcion");
		String personaSolicitanteId = ac.getParameter("personaSolicitanteId");
		String statusId = ac.getParameter("statusId");
		String fechaDesde = ac.getParameter("fechaDesde");
		String fechaHasta = ac.getParameter("fechaHasta");

		String dateFormat = UtilDateTime.getDateFormat(locale);
		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
		List<Map<String,Object>> listBuilderPedidoInterno = FastList.newInstance();

		if ("Y".equals(ac.getParameter("performFind"))) { 	

			Timestamp fechaInicialPeriodo = null;

			if(UtilValidate.isNotEmpty(fechaDesde)){
				fechaInicialPeriodo = UtilDateTime.getDayStart(UtilDateTime.stringToTimeStamp(fechaDesde, dateFormat, timeZone, locale), timeZone, locale);
			}else{
				fechaInicialPeriodo = UtilDateTime.getMonthStart(UtilDateTime.nowTimestamp());
			}

			Timestamp fechaFinalPeriodo = null;

			if(UtilValidate.isNotEmpty(fechaHasta)){
				fechaFinalPeriodo = UtilDateTime.getDayEnd(UtilDateTime.stringToTimeStamp(fechaHasta, dateFormat, timeZone, locale), timeZone, locale);
			}else{
				fechaFinalPeriodo = UtilDateTime.nowTimestamp();
			}

			List<EntityCondition> searchConditions = new FastList<EntityCondition>();
			if (UtilValidate.isNotEmpty(almacenId)) {
				searchConditions.add(EntityCondition.makeCondition("almacenId", EntityOperator.EQUALS, almacenId));
			}
			if (UtilValidate.isNotEmpty(pedidoInternoId)) {
				searchConditions.add(EntityCondition.makeCondition("pedidoInternoId", EntityOperator.EQUALS, pedidoInternoId));
			}   
			if (UtilValidate.isNotEmpty(personaSolicitanteId)) {
				searchConditions.add(EntityCondition.makeCondition("personaSolicitanteId", EntityOperator.EQUALS, personaSolicitanteId));
			}   
			if (UtilValidate.isNotEmpty(statusId)) {
				searchConditions.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, statusId));
			} else {
				EntityConditionList<EntityExpr> orCondition = EntityCondition.makeCondition(UtilMisc.toList(
						EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ATENDIDA"),
						EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "SURTIDO")), EntityOperator.OR);
				searchConditions.add(orCondition);
			}
			if (UtilValidate.isNotEmpty(organizationPartyId)) {
				searchConditions.add(EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS, organizationPartyId));
			}   
			if (UtilValidate.isNotEmpty(descripcion)) {
				searchConditions.add(EntityCondition.makeCondition("descripcion", EntityOperator.LIKE, "%"+descripcion+"%"));
			}   
			if (UtilValidate.isNotEmpty(fechaInicialPeriodo)) {
				searchConditions.add(EntityCondition.makeCondition("createdStamp", EntityOperator.GREATER_THAN_EQUAL_TO, fechaInicialPeriodo));
			}
			if (UtilValidate.isNotEmpty(fechaFinalPeriodo)) {
				searchConditions.add(EntityCondition.makeCondition("createdStamp", EntityOperator.LESS_THAN_EQUAL_TO,fechaFinalPeriodo));
			}

			EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
			Debug.logInfo("condiciones buscaPedidoInterno : " + condiciones, MODULE);

			List<String> orderBy = UtilMisc.toList("pedidoInternoId");



			List<GenericValue> listPedidoInterno = delegator.findByCondition("BuscarPedidoInterno", condiciones, null, orderBy);

			GenericValue PersonEsqueleto = delegator.makeValue("Person");
			GenericValue PartyGroupEsqueleto = delegator.makeValue("PartyGroup");
			Map<String,Object> mapaPedidoInterno = FastMap.newInstance();
			for (GenericValue PedidoInterno : listPedidoInterno) {

				PersonEsqueleto.put("partyId",PedidoInterno.getString("partyIdPerson"));
				PersonEsqueleto.put("firstName",PedidoInterno.getString("firstName"));
				PersonEsqueleto.put("lastName",PedidoInterno.getString("lastName"));

				PartyGroupEsqueleto.put("partyId",PedidoInterno.getString("partyIdGroup"));
				PartyGroupEsqueleto.put("groupName",PedidoInterno.getString("groupName"));

				mapaPedidoInterno.put("pedidoInternoId", Long.valueOf(PedidoInterno.getString("pedidoInternoId")));
				mapaPedidoInterno.put("descripcion", PedidoInterno.getString("descripcion"));
				mapaPedidoInterno.put("personaSolicitanteId", PartyHelper.getCrmsfaPartyName(PersonEsqueleto));
				mapaPedidoInterno.put("statusId", PedidoInterno.getString("descripcionEstatus"));
				mapaPedidoInterno.put("areaPartyId", PartyHelper.getCrmsfaPartyName(PartyGroupEsqueleto));
				listBuilderPedidoInterno.add(mapaPedidoInterno);
				mapaPedidoInterno = FastMap.newInstance();
			}

			Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
				public int compare(Map<String, Object> m1, Map<String, Object> m2) {
					return ((Long) m1.get("pedidoInternoId")).compareTo((Long) m2.get("pedidoInternoId"));
				}
			};

			Collections.sort(listBuilderPedidoInterno, mapComparator);

		}
		ac.put("listBuilderPedidoInterno", listBuilderPedidoInterno);

	}

	/**
	 *Prepara y envia la informacion para e reporte de pedidos surtidos
	 * @param request
	 * @param response
	 * @return
	 */
	public static String preparaReporteSurtirPedidoInterno(HttpServletRequest request, HttpServletResponse response){

		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String pedidoInternoId = UtilCommon.getParameter(request, "pedidoInternoId");
		String acctgTransId = UtilCommon.getParameter(request, "acctgTransId");
		Map<String, Object> jrParameters = FastMap.newInstance();
		List<Map<String, Object>> listProductoSurtir = FastList.newInstance();

		try {	

			GenericValue PedidoInterno = delegator.findByPrimaryKey("PedidoInterno", UtilMisc.toMap("pedidoInternoId",pedidoInternoId));
			jrParameters.putAll(PedidoInterno.getAllFields());

			GenericValue AcctgTrans = delegator.findByPrimaryKey("AcctgTrans", UtilMisc.toMap("acctgTransId",acctgTransId));
			GenericValue EventoContable = delegator.findByPrimaryKey("EventoContable", UtilMisc.toMap("acctgTransTypeId",AcctgTrans.getString("acctgTransTypeId")));
			jrParameters.put("noPoliza", AcctgTrans.getString("poliza"));
			jrParameters.put("postedDate", new java.util.Date(AcctgTrans.getTimestamp("postedDate").getTime()));
			jrParameters.put("descripcionEvento", EventoContable.getString("descripcion"));

			GenericValue PersonaSolicitante = PedidoInterno.getRelatedOne("Person");
			jrParameters.put("solicitante", PartyHelper.getCrmsfaPartyName(PersonaSolicitante));

			String organizationPartyId = PedidoInterno.getString("organizationPartyId");

			String areaId = PedidoInterno.getString("areaPartyId");
			EntityCondition condicionWF = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("tipoWorkFlowId","PEDIDO_INTERNO"),
					EntityCondition.makeCondition("areaId",areaId));
			List<String> orderBy = UtilMisc.toList("secuencia");
			List<GenericValue> listAutorizadores = delegator.findByCondition("Autorizador", condicionWF, null, orderBy);
			int cont = 1;
			for (GenericValue Autorizador : listAutorizadores) {
				if(cont > 2){
					break;
				}
				jrParameters.put("firma"+cont, PartyHelper.getCrmsfaPartyName(Autorizador.getRelatedOne("PERAUTPerson")));
				jrParameters.put("titulo"+cont, "Autorizador "+cont);
				cont++;	
			}

			GenericValue Organization = delegator.findByPrimaryKey("PartyGroup", UtilMisc.toMap("partyId",organizationPartyId));
			jrParameters.put("organizacion", Organization.getString("groupNameLocal").toUpperCase());
			jrParameters.put("logoUrl", Organization.getString("logoImageUrl"));
			jrParameters.put("logoUrl2", Organization.getString("logoImageUrl2"));

			List<GenericValue> listPedidoInternoEntregado = delegator.findByAnd("PedidoInternoEntregado",UtilMisc.toMap("pedidoInternoId",pedidoInternoId,"acctgTransId",acctgTransId));
			Map<String, Object> mapPedidoInterno =  FastMap.newInstance();
			BigDecimal cantidad = BigDecimal.ZERO;
			for (GenericValue pedidoInternoEntregado : listPedidoInternoEntregado) {
				mapPedidoInterno = pedidoInternoEntregado.getAllFields();
				cantidad = (BigDecimal) mapPedidoInterno.get("cantidad");
				cantidad = cantidad.negate();
				mapPedidoInterno.put("cantidad", cantidad);
				mapPedidoInterno.put("total", pedidoInternoEntregado.getBigDecimal("monto").multiply(cantidad));
				listProductoSurtir.add(mapPedidoInterno);
			}


		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		JRMapCollectionDataSource jrDataSource = new JRMapCollectionDataSource(listProductoSurtir);
		request.setAttribute("jrDataSource", jrDataSource);
		request.setAttribute("jrParameters", jrParameters);


		return "success";
	}

}