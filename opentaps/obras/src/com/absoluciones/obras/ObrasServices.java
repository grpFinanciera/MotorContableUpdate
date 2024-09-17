package com.absoluciones.obras;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.common.AB.UtilMessage;
import org.ofbiz.common.AB.UtilWorkflow;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFieldMap;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.order.finaccount.UtilClavePresupuestal;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import javolution.util.FastMap;

public final class ObrasServices {
	
	private ObrasServices() {
	}

	private static String MODULE = ObrasServices.class.getName();
	public final static String CorreoAutorizacion = "CorreoMensajeAutorizar";
	public final static String CorreoComentario = "CorreoMensajeComentario";
	public final static String CorreoAutorizado = "CorreoMensajeAutorizado";
	public final static String CorreoAutorizadoAuto = "CorreoMensajeAutorizadoAutorizador";
	public final static String CorreoRechazo = "CorreoMensajeRechazo";
	public final static String CorreoModificado = "CorreoMensajeModificado";
	
	public final static String estatusComentadaW = "COMENTADA_W"; //StatusWorkFlow
	public final static String estatusRechazadaW = "RECHAZADA_W"; //StatusWorkFlow
	public final static String estatusAutorizada = "AUTORIZADA"; //StatusWorkFlow
	
	public final static String tipoWorkFlowId = "OBRAS"; //StatusWorkFlow
	
	//StatusObra
	public final static String obraAprobado = "APROBADA_O";
	public final static String obraCancelado = "CANCELADA_O";
	public final static String obraComentado = "COMENTADA_O";
	public final static String obraCreado = "CREADA_O";
	public final static String obraEnviado = "ENVIADA_O";
	public final static String obraRechazado = "RECHAZADA_O";
	public final static String obraIniciado = "INICIADA_O";
	public final static String obraTerminado = "TERMINADA_O";
	public final static String obraAnticipoPendiente = "ANTICIPO_PENDIENTE_O";
	public final static String contratoAnticipoPendiente = "ANTICIPO_PENDIENTE_C";
	public final static String obraSinAnticipo = "SIN_ANTICIPO_O";
	public final static String contratoSinAnticipo = "SIN_ANTICIPO_C";
	public final static String contratoIniciado = "INICIADO_C";
	public final static String contratoTerminado = "TERMINADO_C";
	
	
	public final static String iva = "16";
	public final static String rolTesorero = "TESORERO";
	
	
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
		String obraId = (String) context.get("obraId");
		String areaId = (String) context.get("areaId");
		String comentario = (String) context.get("comentario");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		
		//Obtiene la organizacion en base al Id de el obra  
		GenericValue obra = delegator.findByPrimaryKey("Obra", UtilMisc.toMap("obraId",obraId));
		
		//Seteamos los valores de area y solicitante
		obra.set("personaSolicitanteId",userLogin.getString("partyId"));
		obra.set("areaPartyId",areaId);
		delegator.store(obra);
		
		String organizationPartyId = obra.getString("organizationPartyId");

		//Cambiar estatus de obra a ENVIADA
		cambiaStatusObra(dctx, context, obraId, obraEnviado);
		
		//Valida si la obra ya tiene un Workflow asociado
		EntityCondition condicionesWorkflowId = EntityCondition.makeCondition(EntityOperator.AND,
							   					EntityCondition.makeCondition("origenId", EntityOperator.EQUALS, obraId),
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
		UtilWorkflow.registroControlWorkflow(dctx, context, obraId, tipoWorkFlowId, workFlowId);				
				
		GenericValue autorizador = null;
		if(esNuevoWorkflow)
		{	//Obtener autorizador(es) por area. Registro en Status_Workflow (autorizadores, status PENDIENTE y secuencia de autorizadores		
			UtilWorkflow.registroEstatusWorkflow(dctx, context, areaId, organizationPartyId, workFlowId, estatusWorkflowPendiente, tipoWorkFlowId);
			autorizador = UtilWorkflow.obtenAutorizador(areaId, organizationPartyId, 1, delegator, tipoWorkFlowId);
		}
		else
		{	//Actualiza status en Status_Workflow
			List<GenericValue> statusWorkFlowList = UtilWorkflow.getStatusWorkFlow(workFlowId, estatusComentadaW, delegator);
			if(statusWorkFlowList.isEmpty()){
				//puede que sea una solicitud rechazada
				statusWorkFlowList = UtilWorkflow.getStatusWorkFlow(workFlowId, estatusRechazadaW, delegator);
			}
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
					autorizador = UtilWorkflow.obtenAutorizador(areaId, organizationPartyId, nivel.intValue(), delegator, tipoWorkFlowId);
				}
			}
			//Se envian correos de notificacion a los autorizadores anteriores
//			List<GenericValue> statusWorkFlowAuto = UtilWorkflow.getStatusWorkFlow(workFlowId, estatusAutorizada, delegator);
//			for (GenericValue statusWF : statusWorkFlowAuto) {
//				String correoOrigen = obra.getString("personaSolicitanteId");
//				String correoDestino = statusWF.getString("personParentId");
//				UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,tipoWorkFlowId,CorreoModificado,
//						urlHost,obraId,delegator,locale,dispatcher);
//			}
			
		}
		
		//Enviar correo de notificacion al autorizador
		String correoOrigen = obra.getString("personaSolicitanteId");
		String correoDestino = autorizador.getString("autorizadorId");
		UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,tipoWorkFlowId,CorreoAutorizacion,
				urlHost,obraId,null,delegator,locale,dispatcher);
		
		//Comenta workflow
		UtilWorkflow.insertarComentario(dctx, context, comentario, workFlowId, userLogin.getString("partyId"), obraId);
		
		return result;
	}
	
	/**
	 * Cambia estatus de el obra en tabel obra
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> cambiaStatusObra(DispatchContext dctx, Map<?, ?> context, String obraId, String estatus) throws GenericEntityException 
	{	Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		try
		{	GenericValue obra = delegator.findByPrimaryKey("Obra", UtilMisc.toMap("obraId",obraId));
			obra.set("statusId", estatus);
			if(estatus.equalsIgnoreCase(obraAprobado)){
				obra.set("fechaAutorizacion",new Timestamp(Calendar.getInstance().getTimeInMillis()));
			}
			delegator.store(obra);
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
		String obraId = (String) context.get("obraId");
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
		{	GenericValue obra = delegator.findByPrimaryKey("Obra", UtilMisc.toMap("obraId",obraId));
		
			//Cambiar status de STATUS_WORKFLOW para el autorizador en curso*/			
			workFlowId = UtilWorkflow.obtenerWorkFlowId(dctx, context, obraId, tipoWorkFlowId);
			UtilWorkflow.cambiarStatusAutorizadorActual(dctx, context, workFlowId, estatusAutorizada, userLoginpartyId);
			//Validar si contiene otro autorizador
			//Obtener area de el obra
			EntityCondition condicionesArea = EntityCondition.makeCondition(EntityOperator.AND,
					  EntityCondition.makeCondition("obraId", EntityOperator.EQUALS, obraId));
			List<GenericValue> areaSolicitante = delegator.findByCondition("Obra", condicionesArea, UtilMisc.toList("areaPartyId"), null);
			Debug.log("Omar - areaSolicitante: " + areaSolicitante);
			if(areaSolicitante.isEmpty())
			{	return ServiceUtil.returnError("el obra no contiene con un area solicitante");
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
					  EntityCondition.makeCondition("tipoWorkFlowId", EntityOperator.EQUALS, tipoWorkFlowId));
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
				String correoOrigen = obra.getString("personaSolicitanteId");
				String correoDestino = secuenciaNuevoAutorizador.get(0).getString("autorizadorId");
				//Enviar correo electronico al solicitante
				UtilWorkflow.armarEnviarCorreo(userLoginpartyId,correoOrigen,tipoWorkFlowId,CorreoAutorizado,
						urlHost,obraId,userLoginpartyId,delegator,locale,dispatcher);
				//Enviar correo electronico al nuevo autorizador
				UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,tipoWorkFlowId,CorreoAutorizado,
						urlHost,obraId,null,delegator,locale,dispatcher);
			}
			else
			{	Debug.log("Omar - cambiaStatusObra");
				cambiaStatusObra(dctx, context, obraId, obraAprobado);
				Debug.log("Omar - cambiaStatusDetallaObra");
				String correoOrigen = userLoginpartyId;
				String correoDestino = obra.getString("personaSolicitanteId");
				//Enviar correo electronico al solicitante
				UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,tipoWorkFlowId,CorreoAutorizado,
						urlHost,obraId,correoOrigen,delegator,locale,dispatcher);
				
				//Se envia el correo de notificacion a todos los autorizadores 
//				List<GenericValue> autorizadores = UtilWorkflow.obtenAutorizadores(area, obra.getString("organizationPartyId"), delegator, tipoWorkFlowId);
//				for (GenericValue autoriza : autorizadores) {
//					correoDestino = autoriza.getString("autorizadorId");
//					UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,tipoWorkFlowId,CorreoAutorizadoAuto,
//							urlHost,obraId,delegator,locale,dispatcher);
//				}
			}
			//Insertar comentario
			if(comentario != null && !comentario.equals(""))
			{	UtilWorkflow.insertarComentario(dctx, context, comentario, workFlowId, userLoginpartyId, obraId);					
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
		String obraId = (String) context.get("obraId");
		String comentario = (String) context.get("comentarioComentar");
		String workFlowId = "";
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String userLoginpartyId = userLogin.getString("partyId");		
		String urlHost = (String) context.get("urlHost");
		try
		{	
			GenericValue obra = delegator.findByPrimaryKey("Obra", UtilMisc.toMap("obraId",obraId));
			//Actualizar el obra en rechazada
			cambiaStatusObra(dctx, context, obraId, obraComentado);
			
			//Obtener el workFlowId		
			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
					  					  EntityCondition.makeCondition("origenId", EntityOperator.EQUALS, obraId),
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
				{	UtilWorkflow.insertarComentario(dctx, context, comentario, workFlowId, userLoginpartyId, obraId);					
				}			
			}
			
			//Enviar correo de notificacion al solicitante
			String correoOrigen = userLogin.getString("partyId");
			String correoDestino = obra.getString("personaSolicitanteId");
			UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,tipoWorkFlowId,CorreoComentario,
					urlHost,obraId,correoOrigen,delegator,locale,dispatcher);
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
		String obraId = (String) context.get("obraId");
		String comentario = (String) context.get("comentarioRechazo");
		String workFlowId = "";
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String userLoginpartyId = userLogin.getString("partyId");	
		String urlHost = (String) context.get("urlHost");
		
		try
		{	
			GenericValue obra = delegator.findByPrimaryKey("Obra", UtilMisc.toMap("obraId",obraId));
			//Actualizar el obra en rechazada
			cambiaStatusObra(dctx, context, obraId, obraCreado);
			
			//Obtener el workFlowId		
			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
					  					  EntityCondition.makeCondition("origenId", EntityOperator.EQUALS, obraId),
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
				{	UtilWorkflow.insertarComentario(dctx, context, comentario, workFlowId, userLoginpartyId, obraId);					
				}			
			}
			
			//Enviar correo de notificacion al solicitante
			String correoOrigen = obra.getString("personaSolicitanteId");
			String correoDestino = userLogin.getString("partyId");
			UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,tipoWorkFlowId,CorreoRechazo,
					urlHost,obraId,correoDestino,delegator,locale,dispatcher);
		}
		catch (GenericEntityException e) 
		{	return ServiceUtil.returnError(e.getMessage());
		}						
		return result;		
	}
	
	@SuppressWarnings("rawtypes")
	public static Map actualizarModificacionesObrasPrecio(DispatchContext dctx, Map context) {

		Debug.log("Entro al servico actualizarModificacionesObrasPrecio");

		Delegator delegator = dctx.getDelegator();

		String obraId = (String) context.get("obraId");
		Debug.log("obraId: " + obraId);
		
		try {

			GenericValue obra = delegator.findByPrimaryKey("Obra", UtilMisc.toMap("obraId", obraId));
			obra.set("numOficioJustificacionTec", 
					(String) context.get("numOficioJustificacionTec"));
			obra.set("numOficioAutorizacionSFP", 
					(String) context.get("numOficioAutorizacionSFP"));
			obra.set("numOficioAutorizaContratant", 
					(String) context.get("numOficioAutorizaContratant"));
			obra.set("numOficioTarjetas", 
					(String) context.get("numOficioTarjetas"));
			obra.set("numOficioConvenioModif", 
					(String) context.get("numOficioConvenioModif"));
			obra.set("numOficioPresupuesto", 
					(String) context.get("numOficioPresupuesto"));
			obra.set("numOficioFianzaCumplimiento", 
					(String) context.get("numOficioFianzaCumplimiento"));
			obra.set("numOficioFianzaAmpliacion", 
					(String) context.get("numOficioFianzaAmpliacion"));

			delegator.store(obra);

			Debug.log("Return Success");
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("obraId", obraId);
			return result;

		} catch (GeneralException e) {
			return UtilMessage.createAndLogServiceError(e, MODULE);
		}

	}
	
	@SuppressWarnings("rawtypes")
	public static Map actualizarModificacionesObrasCalendario(DispatchContext dctx, Map context) {

		Debug.log("Entro al servico actualizarModificacionesObrasCalendario");

		Delegator delegator = dctx.getDelegator();

		String obraId = (String) context.get("obraId");
		Debug.log("obraId: " + obraId);
		
		try {

			GenericValue obra = delegator.findByPrimaryKey("Obra", UtilMisc.toMap("obraId", obraId));
			obra.set("numOficioJustificaciTecMpa", 
					(String) context.get("numOficioJustificaciTecMpa"));
			obra.set("numOficioAutorizacioSfpMpa", 
					(String) context.get("numOficioAutorizacioSfpMpa"));
			obra.set("numOficioDictamenContrate", 
					(String) context.get("numOficioDictamenContrate"));
			obra.set("numOficioConvenio", 
					(String) context.get("numOficioConvenio"));
			obra.set("numOficioCalendarEjecucion", 
					(String) context.get("numOficioCalendarEjecucion"));
			obra.set("numOficioModifFianzaCumpl", 
					(String) context.get("numOficioModifFianzaCumpl"));

			delegator.store(obra);

			Debug.log("Return Success");
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("obraId", obraId);
			return result;

		} catch (GeneralException e) {
			return UtilMessage.createAndLogServiceError(e, MODULE);
		}

	}
	
	/**
	 * Metodo que crea un registro de obra
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,?> crearObra(DispatchContext dctx,
			Map<String,Object> context) throws GenericEntityException {
		
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> regreso = ServiceUtil.returnSuccess();
		String ciclo = (String) context.get("ciclo");
		
		try {
			Map<String, String> clasificacionesMap = (Map<String, String>) context.get("clasificacionesMap");
			clasificacionesMap = UtilClavePresupuestal.cambiaNombreMapa((Map<String, String>) clasificacionesMap);
	        // Creando la Obra
	        String obraId = delegator.getNextSeqId("Obra");
	        GenericValue obraGeneric = delegator.makeValue("Obra", UtilMisc.toMap("obraId", obraId));
	        obraGeneric.setNonPKFields(context);
	        String clave = UtilClavePresupuestal.almacenaClavePresupuestal(clasificacionesMap, obraGeneric, 
	        		delegator, UtilClavePresupuestal.EGRESO_TAG, (String) context.get("organizationPartyId"),true,ciclo);
	        obraGeneric.put("clavePresupuestal", clave);
	        obraGeneric.put("valorObra", BigDecimal.ZERO);
	        obraGeneric.create();
        
	        regreso.put("obraId", obraId);
	        
	        } catch (GenericEntityException e) {
	            Debug.logError(e, MODULE);
	            return ServiceUtil.returnError(e.getMessage());
	        }

		return regreso;
	}
	
	public static Map<String,?> actualizaObra(DispatchContext dctx,
			Map<String,?> context) throws GenericEntityException {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> regreso = ServiceUtil.returnSuccess();
		
		try {
			String obraId = (String) context.get("obraId");
			GenericValue obraGeneric = delegator.findByPrimaryKey("Obra", UtilMisc.toMap("obraId",obraId));
			obraGeneric.setNonPKFields(context);
			obraGeneric.store();
			regreso.put("obraId", obraId);
			
        } catch (GenericEntityException e) {
            Debug.logError(e, MODULE);
            return ServiceUtil.returnError(e.getMessage());
        }
		
		return regreso;
	}
	
	public static Map<String,?> guardaDireccion(DispatchContext dctx,
			Map<String,?> context) throws GenericEntityException {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> regreso = ServiceUtil.returnSuccess();
		
		try {
			
			String contactMechId = (String) context.get("contactMechId");
			String obraId = (String) context.get("obraId");
			//Datos a transformar de PostalAddress
			String country = (String) context.get("COUNTRY");
			String state = (String) context.get("STATE");
			String municipality = (String) context.get("MUNICIPALITY");
			String city = (String) context.get("CITY");
			
			if(contactMechId != null && !contactMechId.isEmpty()){
			    //Crear PostalAddress
			    GenericValue postalAddress = delegator.findByPrimaryKey("PostalAddress",UtilMisc.toMap("contactMechId",contactMechId));
			    postalAddress.set("countryGeoId",country);
			    postalAddress.set("stateProvinceGeoId",state);
			    postalAddress.set("municipalityGeoId",municipality);
			    postalAddress.set("city",city);
			    postalAddress.setNonPKFields(context);
			    postalAddress.store();
			} else {
				//Crear Contact Mech
				contactMechId = delegator.getNextSeqId("ContactMech");
			    GenericValue contactMechNew = delegator.makeValue("ContactMech", 
			    		UtilMisc.toMap("contactMechId", contactMechId,"contactMechTypeId","POSTAL_ADDRESS"));
			    contactMechNew.create();
			    
			    //Crear ObraContactMech
			    GenericValue obraContactMechNew = delegator.makeValue("ObraContactMech", 
			    		UtilMisc.toMap("obraId", obraId,"contactMechId",contactMechId));
			    obraContactMechNew.create();
			    
			    //Crear ObraContactMechPurpose
			    GenericValue obraContactMechPurNew = delegator.makeValue("ObraContactMechPurpose", 
			    		UtilMisc.toMap("obraId", obraId,"contactMechPurposeTypeId",
			    				"PRIMARY_LOCATION","contactMechId",contactMechId));
			    obraContactMechPurNew.create();
			    
			    //Crear PostalAddress
			    GenericValue postalAddress = delegator.makeValue("PostalAddress");
			    postalAddress.setPKFields(UtilMisc.toMap("contactMechId",contactMechId));
			    postalAddress.set("countryGeoId",country);
			    postalAddress.set("stateProvinceGeoId",state);
			    postalAddress.set("municipalityGeoId",municipality);
			    postalAddress.set("city",city);
			    postalAddress.setNonPKFields(context);
			    postalAddress.create();
			    
			    //Se actualiza la obra
			    GenericValue obra = delegator.findByPrimaryKey("Obra", UtilMisc.toMap("obraId",obraId));
			    obra.set("ubicacionFisica", contactMechId);
			    obra.store();
			    
			}
			regreso.put("contactMechId", contactMechId);
		     
        } catch (GenericEntityException e) {
            Debug.logError(e, MODULE);
            e.printStackTrace();
            return ServiceUtil.returnError("No se pudo guardar la direcci\u00f3n");
        }
		
		return regreso;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<String,?> finiquitoContratoObra(DispatchContext dctx, Map context) {
		Delegator delegator = dctx.getDelegator();			
		String obraId = (String) context.get("obraId");
		String contratoId = (String) context.get("contratoId");
		LocalDispatcher dispatcher = dctx.getDispatcher();
		String numOficioEntregaFisica = (String) context.get("numOficioEntregaFisica");
		String numOficioEntregaRecepcion = (String) context.get("numOficioEntregaRecepcion");
		String numOficioViciosOcultos = (String) context.get("numOficioViciosOcultos");
		String numOficioFiniquitoContrato = (String) context.get("numOficioFiniquitoContrato");
		String numOficioExtincionDerechos = (String) context.get("numOficioExtincionDerechos");
		String numOficioPlanosDefinitivos = (String) context.get("numOficioPlanosDefinitivos");
		String numOficioCatalogoConcepto = (String) context.get("numOficioCatalogoConcepto");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		Debug.log("context: " + context);
		Timestamp fechaActual = new Timestamp(Calendar.getInstance().getTimeInMillis());
		String organizationPartyId = null;
		int mesActual = Calendar.MONTH+1;
		String tipoClave = "EGRESO";		
		BigDecimal totalContrato = BigDecimal.ZERO;
		BigDecimal avance = BigDecimal.ZERO;
		String caParty = null;
		String currency = null;
		String caProductId = null;
		String ciclo = (String) context.get("ciclo");
		
		try 
		{				
			//Obtiene la clave presupuestaria de la obra
            EntityCondition condObra = EntityCondition.makeCondition(EntityOperator.AND,
				  EntityCondition.makeCondition("obraId", EntityOperator.EQUALS, obraId));	
			List<GenericValue> itemObraList = delegator.findByCondition("Obra", condObra, UtilMisc.toList("statusId", "organizationPartyId", "moneda", "productId", "acctgTagEnumIdAdmin",
																	"acctgTagEnumId1", "acctgTagEnumId2", "acctgTagEnumId3", "acctgTagEnumId4", "acctgTagEnumId5", "acctgTagEnumId6", "acctgTagEnumId7", 
																	"acctgTagEnumId8", "acctgTagEnumId9", "acctgTagEnumId10", "acctgTagEnumId11", "acctgTagEnumId12", "acctgTagEnumId13", "acctgTagEnumId14", 
																	"acctgTagEnumId15", "descripcion"), null);
			
            
           //Obtiene el monto total del contrato            
    		EntityCondition condContrato = EntityCondition.makeCondition(EntityOperator.AND,
    				  EntityCondition.makeCondition("obraId", EntityOperator.EQUALS, obraId),
    				  EntityCondition.makeCondition("contratoId", EntityOperator.EQUALS, contratoId));
    		Debug.log("condContrato: "+ condContrato);
    		List<GenericValue> itemContratoList = delegator.findByCondition("ContratoObra", condContrato, null, null);
    		Iterator<GenericValue> itemsContrato = itemContratoList.iterator();
    		Debug.log("itemContratoList: "+ itemContratoList);
    	    while (itemsContrato.hasNext()) 
    	    {	GenericValue itemContrato = itemsContrato.next();
	    	    caParty = itemContrato.getString("contratistaId");
	    	    currency = itemContrato.getString("moneda");
	    	    caProductId = itemContrato.getString("productId");
	    	    totalContrato = itemContrato.getBigDecimal("valorContratoConIva");
    	    	avance = totalContrato;
    	    	if(itemContrato.getString("statusId").equalsIgnoreCase(contratoTerminado))    	    	
    	    	{	EntityCondition condFiniquito = EntityCondition.makeCondition(EntityOperator.AND,
        				  EntityCondition.makeCondition("obraId", EntityOperator.EQUALS, obraId),
        				  EntityCondition.makeCondition("contratoId", EntityOperator.EQUALS, contratoId));
  		      		List<GenericValue> itemFiniquitoList = delegator.findByCondition("FiniquitoContrato", condFiniquito, null, null);
  		      		Iterator<GenericValue> itemsFiniquito = itemFiniquitoList.iterator();
  		      		Debug.log("itemFiniquitoList: " + itemFiniquitoList);
  		      	    while (itemsFiniquito.hasNext()) 
  		      	    {	GenericValue itemFiniquito = itemsFiniquito.next();
  		      	    	Debug.log("itemFiniquito: " + itemFiniquito);		      	    
  		    			itemFiniquito.set("contratoId", contratoId);
  		    			itemFiniquito.set("obraId", obraId);
  		    			itemFiniquito.set("numOficioEntregaFisica", numOficioEntregaFisica);
  		    			itemFiniquito.set("numOficioEntregaRecepcion", numOficioEntregaRecepcion);
  		    			itemFiniquito.set("numOficioViciosOcultos", numOficioViciosOcultos);			
  		    			itemFiniquito.set("numOficioFiniquitoContrato", numOficioFiniquitoContrato);
  		    			itemFiniquito.set("numOficioExtincionDerechos", numOficioExtincionDerechos);
  		    			itemFiniquito.set("numOficioPlanosDefinitivos", numOficioPlanosDefinitivos);
  		    			itemFiniquito.set("numOficioCatalogoConcepto", numOficioCatalogoConcepto);
  		    			delegator.store(itemFiniquito);   
  		    			Map<String, Object> result = ServiceUtil.returnSuccess("El contrato se ha actualizado");
  		    			result.put("obraId", obraId);
  		    			result.put("contratoId", contratoId);
  		    			return result; 
  		      	    }    	    	    	    	
      	    	}    	    		    	    	
    	    }
    	    Debug.log("totalContrato1: " +totalContrato);
    	    Debug.log("avance1: " +avance);
            
            
            //Obtiene los periodos del contrato que se va a finiquitar
            List<String> orderByDesc = UtilMisc.toList("mes DESC");
            EntityCondition condPeriodo = EntityCondition.makeCondition(EntityOperator.AND,
				  EntityCondition.makeCondition("obraId", EntityOperator.EQUALS, obraId),
				  EntityCondition.makeCondition("contratoId", EntityOperator.EQUALS, contratoId));	
            Debug.log("condPeriodo: " +condPeriodo);
			List<GenericValue> itemPeriodosList = delegator.findByCondition("ContratoPeriodo", condPeriodo, null, orderByDesc);
			Debug.log("itemPeriodosList: " +itemPeriodosList);
			Iterator<GenericValue> itemPeriodoIter = itemPeriodosList.iterator();
			
			Map<String, Object> montoMap = FastMap.newInstance();
            Map<String, Object> clavePresupuestalMap = FastMap.newInstance();
            Map<String, Object> mapaProducto = FastMap.newInstance();
            Map<String, Object> input = FastMap.newInstance();	
			Map<String, Object> output = FastMap.newInstance();
			
			Debug.log("itemPeriodoIter: " +itemPeriodoIter);
            while (itemPeriodoIter.hasNext()) 
            {               	
            	GenericValue itemPeriodo = itemPeriodoIter.next();
            	BigDecimal totalPeriodo = itemPeriodo.getBigDecimal("monto");
            	int mesPeriodo = Integer.parseInt(itemPeriodo.getString("mes"));
            	int cicloPeriodo = Integer.parseInt(itemPeriodo.getString("ciclo"));
            	Debug.log("mesPeriodo: " + mesPeriodo);
            	Debug.log("cicloPeriodo: " + cicloPeriodo);
            		
            	
            	
            	//Valida si el periodo obtenido tiene al menos un avance regitrado
            	Calendar fechaInicioPeriodoCal = GregorianCalendar.getInstance();
            	fechaInicioPeriodoCal.set(Calendar.DATE, 1);		    				            						    			    		
            	fechaInicioPeriodoCal.set(Calendar.MONTH, mesPeriodo-1);
            	fechaInicioPeriodoCal.set(Calendar.YEAR, cicloPeriodo);	    			                                    		         
	            Timestamp fechaInicioPeriodo = new Timestamp(fechaInicioPeriodoCal.getTimeInMillis());
	            
	            Calendar fechaFinPeriodoCal = GregorianCalendar.getInstance();
	            fechaFinPeriodoCal.set(Calendar.DATE, 1);
	            if(mesPeriodo==11)	           
	            {   fechaFinPeriodoCal.set(Calendar.MONTH, 1);
	            	fechaFinPeriodoCal.set(Calendar.YEAR, cicloPeriodo+1);
	            }
	            else
	            {	fechaFinPeriodoCal.set(Calendar.MONTH, mesPeriodo);
            		fechaFinPeriodoCal.set(Calendar.YEAR, cicloPeriodo);	            	
	            }
	            Timestamp fechaFinPeriodo = new Timestamp(fechaFinPeriodoCal.getTimeInMillis());
	            Debug.log("fechaInicioPeriodo: " + fechaInicioPeriodo);
	            Debug.log("fechaFinPeriodo: " + fechaFinPeriodo);
	            
	            List<String> orderByAvance = UtilMisc.toList("fechaAvance DESC");
	            EntityCondition condAvance = EntityCondition.makeCondition(EntityOperator.AND,
					  EntityCondition.makeCondition("obraId", EntityOperator.EQUALS, obraId),
					  EntityCondition.makeCondition("contratoId", EntityOperator.EQUALS, contratoId),
					  EntityCondition.makeCondition("fechaAvance", EntityOperator.GREATER_THAN_EQUAL_TO, fechaInicioPeriodo),
	            	  EntityCondition.makeCondition("fechaAvance", EntityOperator.LESS_THAN, fechaFinPeriodo));
	            Debug.log("condAvance: " +condAvance);
				List<GenericValue> itemAvanceList = delegator.findByCondition("SeguimientoObra", condAvance, null, orderByAvance);
				Debug.log("itemAvanceList: " +itemAvanceList);
				Iterator<GenericValue> itemAvanceIter = itemAvanceList.iterator();
				BigDecimal totalAvance = BigDecimal.ZERO;
	            while (itemAvanceIter.hasNext()) 
	            {   GenericValue itemAvance = itemAvanceIter.next();
	            	totalAvance = totalAvance.add(itemAvance.getBigDecimal("montoAvanceConIva"));	            		            		           
	            }
	            
	            String clavePresupuestal = "";
	            
	            if(UtilValidate.isNotEmpty(itemObraList)){
	            	organizationPartyId = itemObraList.get(0).getString("organizationPartyId");
	            	clavePresupuestal = UtilClavePresupuestal.getClavePresupuestal(itemObraList.get(0), delegator, tipoClave, organizationPartyId, ciclo);
	            } else {
	            	throw new GenericEntityException("No se encuentran elementos para la obra");
	            }
	            
		        Debug.log("clavePresupuestal: " + clavePresupuestal);
	            
	            Calendar fechaContableCal = GregorianCalendar.getInstance();
	            fechaContableCal.set(Calendar.DATE, 1);		    				            						    			    		
	    		fechaContableCal.set(Calendar.MONTH, mesPeriodo-1);
	    		fechaContableCal.set(Calendar.YEAR, cicloPeriodo);
	    		Debug.log("mesPeriodo: " + mesPeriodo);
	    		Debug.log("cicloPeriodo: " + cicloPeriodo);
	                                    		            
	            Timestamp fechaContable = new Timestamp(fechaContableCal.getTimeInMillis());
	            Debug.log("fechaContable: " + fechaContable);
	         
	           /************************************/
               /******** Motor Contable ************/
               /************************************/

	            clavePresupuestalMap.put("0", clavePresupuestal);
	            mapaProducto.put("0", caProductId);
	            
	            context.put("montoMap", montoMap);
	            context.put("clavePresupuestalMap", clavePresupuestalMap);
	            
	            Debug.log("map1");
	            if(itemPeriodo.getString("mes").equals(String.valueOf(mesActual)))
	            {	montoMap.put("0", (avance.subtract(totalAvance)).toString());
	            }	            
	            else
	            {	if(totalAvance.compareTo(totalPeriodo) < 0)
	            	{	montoMap.put("0", (totalPeriodo.subtract(totalAvance)).toString());
				        if(totalAvance.compareTo(BigDecimal.ZERO) != 0)
				        	avance = avance.subtract(totalAvance);
				        else
				        	avance = avance.subtract(totalPeriodo);
	            	}	            		            	
	            	else
	            	{	if(totalAvance.compareTo(BigDecimal.ZERO) != 0)
			        		avance = avance.subtract(totalAvance);
			        	else
			        		avance = avance.subtract(totalPeriodo);
	            	}	            	
	            }	    
	       
	            Debug.log("map2");
	        	input.put("eventoContableId", "COM_LIBERACION");
	        	input.put("tipoClaveId", "EGRESO");
	        	input.put("fechaRegistro", fechaActual);
	        	input.put("fechaContable", fechaContable);
	        	input.put("currency", currency);
	        	input.put("usuario", userLogin.getString("userLoginId"));
	        	input.put("organizationId", organizationPartyId);
	        	input.put("descripcion", "Finiquito de contrato " + contratoId + ". Mes " +itemPeriodo.getString("mes")+". Ciclo "+itemPeriodo.getString("ciclo"));
	        	input.put("roleTypeId", "BILL_FROM_VENDOR");
	        	input.put("campo", "contratoId");
	        	input.put("valorCampo", contratoId);
	        	input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables("COM_LIBERACION", delegator, context, caParty, null, montoMap, mapaProducto)); 
	        	
	        	Debug.log("Entra: " + input);
	        	output = dispatcher.runSync("creaTransaccionMotor", input);
	            
	            Debug.log("totalContrato: " + totalContrato);
	            Debug.log("avance: " + avance);
	            Debug.log("totalAvance: " + totalAvance);
	            Debug.log("totalPeriodo: " + totalPeriodo);	            
	            
	            if(itemPeriodo.getString("mes").equals(String.valueOf(mesActual)))
	            {	
			        break;
	            }
	            
	            montoMap.clear();
	            clavePresupuestalMap.clear();
	            mapaProducto.clear();
	            input.clear();
	            output.clear();

            }		
			
            //Actualiza el status del contrato
            GenericValue contrato = delegator.findByPrimaryKey("ContratoObra", UtilMisc.toMap("contratoId",contratoId));    	
            contrato.set("statusId", contratoTerminado);	    		
	    	delegator.store(contrato);            	            	

            
            GenericValue obra = GenericValue.create(delegator.getModelEntity("FiniquitoContrato"));						
			String finiquitoId = delegator.getNextSeqId("FiniquitoContrato");
			obra.set("finiquitoId", finiquitoId);
			obra.set("contratoId", contratoId);
			obra.set("obraId", obraId);
			obra.set("numOficioEntregaFisica", numOficioEntregaFisica);
			obra.set("numOficioEntregaRecepcion", numOficioEntregaRecepcion);
			obra.set("numOficioViciosOcultos", numOficioViciosOcultos);			
			obra.set("numOficioFiniquitoContrato", numOficioFiniquitoContrato);
			obra.set("numOficioExtincionDerechos", numOficioExtincionDerechos);
			obra.set("numOficioPlanosDefinitivos", numOficioPlanosDefinitivos);
			obra.set("numOficioCatalogoConcepto", numOficioCatalogoConcepto);
			delegator.create(obra);					
		} catch (GeneralException e) {
			return ServiceUtil.returnError("Error al finiquitar el contrato de obra. " + e.getMessage());
		}
		Map<String, Object> result = ServiceUtil.returnSuccess("El contrato se ha finiquitado");
		result.put("obraId", obraId);
		result.put("contratoId", contratoId);
		return result;
	}
	
	public static Map<String,?> contratoObra(DispatchContext dctx, Map<String,?> context) {
		Delegator delegator = dctx.getDelegator();			
		String obraId = (String) context.get("obraId");
		String numContrato = (String) context.get("numContrato");
		String numReciboAnticipo = (String) context.get("numReciboAnticipo");
		String numGarantiaAnticipo = (String) context.get("numGarantiaAnticipo");
		String numGarantiaCumplimiento = (String) context.get("numGarantiaCumplimiento");
		String proveedor = (String) context.get("proveedor");													
		try 
		{	GenericValue obra = GenericValue.create(delegator.getModelEntity("Obra"));						
			obra.set("obraId", obraId);
			obra.set("numContrato", numContrato);
			obra.set("numReciboAnticipo", numReciboAnticipo);
			obra.set("numGarantiaAnticipo", numGarantiaAnticipo);			
			obra.set("numGarantiaCumplimiento", numGarantiaCumplimiento);
			obra.set("proveedor", proveedor);			
			delegator.create(obra);					
		} catch (GeneralException e) {
			return ServiceUtil.returnError("Error al actualizar los datos");
		}
		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("obraId", obraId);
		return result;
	}
	
	@SuppressWarnings({ "unchecked" })
	public static Map<String,?> iniciarObra(DispatchContext dctx, Map<String,?> context) {
		Delegator delegator = dctx.getDelegator();		
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String obraId = (String) context.get("obraId");
		BigDecimal valorObra = (BigDecimal) context.get("valorObra");		
		BigDecimal porcentajeAnticipoObra = (BigDecimal) context.get("porcentajeAnticipoObra");
		BigDecimal montoAnticipo = (BigDecimal) context.get("montoAnticipo");
		BigDecimal retencionMillarIVDGLE = (BigDecimal) context.get("retencionMillarIVDGLE");
		BigDecimal retencionMillarICIC = (BigDecimal) context.get("retencionMillarICIC");
		Timestamp fechaContable = null;
		String ciclo = (String)context.get("ciclo");
		
		if(valorObra == null || valorObra.toString().trim().equals("") || valorObra.equals(BigDecimal.ZERO))
		{	return ServiceUtil.returnError("Es necesario ingresar el Valor de la obra sin IVA");			
		}		
			
		BigDecimal valorObraConIva = valorObra.multiply(BigDecimal.valueOf(1.16));
		BigDecimal montoIva = valorObra.multiply(BigDecimal.valueOf(0.16));
		String auxRetencionMillarIVDGLE = (String) context.get("auxRetencionMillarIVDGLE");
		String auxRetencionMillarICIC = (String) context.get("auxRetencionMillarICIC");
		String mesInicio = (String) context.get("mesInicio");
		String anioInicio = (String) context.get("anioInicio");		
		String urlHost = (String) context.get("urlHost");
		Locale locale = (Locale) context.get("locale");
		String tipoClave = "EGRESO";
		boolean tieneAnticipo = false;
		Debug.log("porcentajeAnticipoObra:"+porcentajeAnticipoObra+"Trim");
		if(!porcentajeAnticipoObra.equals(BigDecimal.ZERO) && !porcentajeAnticipoObra.equals(""))
		{	tieneAnticipo = true;			
		}
		Map<String, String > montosMeses = (Map<String, String>) context.get("montosMeses");
		Debug.log("Omar - context: " + context);		
		try
		{
			int sizeMap = montosMeses.size();			
			BigDecimal total = BigDecimal.ZERO;
			for(int i=1; i<=sizeMap; i++)
			{	Debug.log("MontosMeses: " + montosMeses.get(Integer.toString(i))+"TRIM");
				if(montosMeses.get(Integer.toString(i)) == null || montosMeses.get(Integer.toString(i)).trim().equals(""))
				{	return ServiceUtil.returnError("Es necesario ingresar todos los montos de los Periodos de la obra");					
				}
				else
				{	BigDecimal monto= new BigDecimal(montosMeses.get(Integer.toString(i)));
					total = total.add(monto);
				}
				
			}			
			if(valorObra.compareTo(total) != 0)
			{	return ServiceUtil.returnError("La suma de los montos mensuales no coincide con el valor de la obra");		
			}			
			EntityCondition condObra = EntityCondition.makeCondition(EntityOperator.AND,
					  EntityCondition.makeCondition("obraId", EntityOperator.EQUALS, obraId));	
			List<GenericValue> itemObraList = delegator.findByCondition("Obra", condObra, UtilMisc.toList("statusId", "organizationPartyId", "moneda", "productId", "acctgTagEnumIdAdmin",
																		"acctgTagEnumId1", "acctgTagEnumId2", "acctgTagEnumId3", "acctgTagEnumId4", "acctgTagEnumId5", "acctgTagEnumId6", "acctgTagEnumId7", 
																		"acctgTagEnumId8", "acctgTagEnumId9", "acctgTagEnumId10", "acctgTagEnumId11", "acctgTagEnumId12", "acctgTagEnumId13", "acctgTagEnumId14", 
																		"acctgTagEnumId15", "descripcion"), null);
			Debug.log("Omar - itemObraList: " + itemObraList);			
			
			Map mapaMotor = null;
			Iterator<GenericValue> itemsObra = itemObraList.iterator();            
            if(itemsObra.hasNext()) 
            {   GenericValue item = itemsObra.next();                                        	
            	if(item.getString("statusId").equals(obraIniciado))
            	{	return ServiceUtil.returnError("La obra ya ha sido iniciada");            	
            	}
            	else if(!item.getString("statusId").equals(obraAprobado))
            	{	return ServiceUtil.returnError("La obra no puede ser iniciada. No se encuenta en estatus APROBADA");            		
            	}
            	String organizationPartyId = item.getString("organizationPartyId");
            	String caParty = item.getString("proveedor");
            	String currency = item.getString("moneda");
            	String caProductId = item.getString("productId");
            	int mesInicioInt = Integer.parseInt(mesInicio);
	            int anioInicioInt = Integer.parseInt(anioInicio);
            	Timestamp fechaTrans = new Timestamp(Calendar.getInstance().getTimeInMillis());
            	long secuencia = 0;
            	for(int i=1; i<=sizeMap; i++)
            	{   
            		Map input = FastMap.newInstance();	                
            		int indiceAdmin = UtilClavePresupuestal.indiceClasAdmin(tipoClave, organizationPartyId, delegator, ciclo);
		            for(int j=1; j<16; j++)
		            {	if(indiceAdmin == j)
		            		input.put("clasificacion"+j,UtilClavePresupuestal.buscaExternalId(item.getString("acctgTagEnumIdAdmin"),delegator));
		            	String clasif = (String)item.getString("acctgTagEnumId" + j);
		            	if(clasif != null && !clasif.isEmpty())
		            		input.put("clasificacion"+j,UtilClavePresupuestal.buscaSequenceId(clasif, delegator));
		            }
		            
		            BigDecimal montoAfectar=new BigDecimal(montosMeses.get(Integer.toString(i)));
		            montoAfectar = montoAfectar.multiply(BigDecimal.valueOf(1.16));
		            Calendar fechaContableCal = GregorianCalendar.getInstance();
		            fechaContableCal.set(Calendar.DATE, 1);		    				            						    		
		    		if(mesInicioInt == 13)
		    		{	mesInicioInt = 1;
		    			fechaContableCal.set(Calendar.MONTH, mesInicioInt-1);
		    			fechaContableCal.set(Calendar.YEAR, anioInicioInt+1);		            	
		            }
		    		else
		    		{	fechaContableCal.set(Calendar.MONTH, mesInicioInt-1);
		    			fechaContableCal.set(Calendar.YEAR, anioInicioInt);		    			    		
		    		}
		    		mesInicioInt++;
		                                    		            
		            fechaContable = new Timestamp(fechaContableCal.getTimeInMillis());

		            input.put("referenciaDocumento", null);
		            input.put("tipoClave", tipoClave);                
		            input.put("idTipoDoc", "COM_PE");
		            input.put("fechaContable", fechaContable);
		            input.put("fechaTransaccion", fechaTrans);
		            input.put("organizationPartyId", organizationPartyId);
		            input.put("partyId", null);
		            input.put("monto", montoAfectar);
		            input.put("caParty", caParty);
		            input.put("currency", currency);
		            input.put("secuencia", UtilFormatOut.formatPaddedNumber((secuencia+1), 4));
		            input.put("obraId", obraId);
		            input.put("userLogin", userLogin);
		            input.put("agrupadorOrigen", "");
		            input.put("caProductId", caProductId);                
		            Debug.log("Va a invocar motor contable: " + input);
		            mapaMotor = dispatcher.runSync("registroContable_Presupuestal", input);
		            Debug.log("mapaMotor: " + mapaMotor);		            		            
            	}
            	GenericValue obra = GenericValue.create(delegator.getModelEntity("Obra"));						
    			obra.set("obraId", obraId);
    			obra.set("valorObra", valorObra);
    			obra.set("valorObraConIva", valorObraConIva);
    			obra.set("iva", iva);
    			obra.set("montoIva", montoIva);
    			obra.set("porcentajeAnticipoObra", porcentajeAnticipoObra);
    			obra.set("montoAnticipo", montoAnticipo);			
    			obra.set("retencionMillarIVDGLE", retencionMillarIVDGLE);
    			obra.set("retencionMillarICIC", retencionMillarICIC);
    			obra.set("proveedorMillarIVDGLE", auxRetencionMillarIVDGLE);
    			obra.set("proveedorMillarICIC", auxRetencionMillarICIC);
    			obra.set("statusId", obraIniciado);
    			if(tieneAnticipo)
    			{	obra.set("statusAnticipoId", obraAnticipoPendiente);
    			}
    			else
    			{	obra.set("statusAnticipoId", obraSinAnticipo);    				
    			}
    			delegator.create(obra);    			
    			
    			if(tieneAnticipo)
    			{	GenericValue pendientesTesoreria = GenericValue.create(delegator.getModelEntity("PendientesTesoreria"));
	    			pendientesTesoreria.set("idPendienteTesoreria", obraId);
	    			pendientesTesoreria.set("tipo", "OBRAS");
	    			pendientesTesoreria.set("descripcion", item.getString("descripcion"));
	    			pendientesTesoreria.set("auxiliar", caParty);
	    			pendientesTesoreria.set("monto", montoAnticipo);    			
	    			pendientesTesoreria.set("estatus", obraAnticipoPendiente);
	    			pendientesTesoreria.set("fechaTransaccion", fechaTrans);
	    			pendientesTesoreria.set("fechaContable", fechaContable);	    			
	    			pendientesTesoreria.set("moneda", currency);
	    			delegator.create(pendientesTesoreria);	    			
	    			
	    			String correoOrigen = userLogin.getString("partyId");
	    			
	    			EntityCondition condTesorero = EntityCondition.makeCondition(EntityOperator.AND,
		   					EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, rolTesorero));		
					List<GenericValue> correoDestinatario = delegator.findByCondition("PartyRole", condTesorero, UtilMisc.toList("partyId"), null);					
					if(correoDestinatario.isEmpty())
					{	return ServiceUtil.returnError("No existe al menos un usuario con Rol de TESORERO para realizar el anticipo");			
					}
					else
					{	Iterator<GenericValue> tesoreroIter = correoDestinatario.iterator();
						while (tesoreroIter.hasNext())
						{	GenericValue generic = tesoreroIter.next();
							String correoDestino = generic.getString("partyId");
							UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,tipoWorkFlowId,CorreoModificado,
														   urlHost,obraId,correoOrigen,delegator,locale,dispatcher);
						}
					}	    				    													
    			}
            }
									
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("obraId", obraId);
			return result;
		}
		catch (GeneralException e) {
			return ServiceUtil.returnError(e.getMessage());
		}		
	}	
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public static Map<String,?> iniciarContratoObra(DispatchContext dctx, Map context) {
		Delegator delegator = dctx.getDelegator();		
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		Debug.log("Omar - context: " + context);
		String urlHost = (String) context.get("urlHost");
		Locale locale = (Locale) context.get("locale");
		String obraId = (String) context.get("obraId");
		String contratoId = "";
		String numContrato = (String) context.get("numContrato");
		String descripcion = (String) context.get("descripcion");		
		String numReciboAnticipo = (String) context.get("numReciboAnticipo");
		String numGarantiaAnticipo = (String) context.get("numGarantiaAnticipo");
		String numGarantiaCumplimiento = (String) context.get("numGarantiaCumplimiento");
		String contratistaId = (String) context.get("contratistaId");
		String productId = (String) context.get("productId");		
		BigDecimal valorContrato = new BigDecimal((String) context.get("valorContrato")) ;
		BigDecimal porcentajeAnticipoContrato = new BigDecimal((String) context.get("porcentajeAnticipoContrato"));
		BigDecimal montoAnticipo = new BigDecimal((String) context.get("montoAnticipo"));
		String mesInicio = (String) context.get("mesInicio");
		String anioInicio = (String) context.get("anioInicio");
		String numRetenciones = (String) context.get("numRetenciones");
		String moneda = (String) context.get("moneda");
		String tipoClave = "EGRESO";
		Timestamp fechaRealInicio = (Timestamp) context.get("fechaRealInicio");
		Timestamp fechaRealFin = (Timestamp) context.get("fechaRealFin");
		Timestamp fechaContable = null;
		String ciclo = (String) context.get("ciclo");
		
		
		if(valorContrato == null || valorContrato.toString().trim().equals("") || valorContrato.equals(BigDecimal.ZERO))
		{	return ServiceUtil.returnError("Es necesario ingresar el Valor de la obra sin IVA");			
		}					
		BigDecimal valorContratoConIva = valorContrato.multiply(BigDecimal.valueOf(1.16));
		BigDecimal montoIva = valorContrato.multiply(BigDecimal.valueOf(0.16));		
		
		boolean tieneAnticipo = false;
		Debug.log("porcentajeAnticipoContrato:"+porcentajeAnticipoContrato+"Trim");
		if(!porcentajeAnticipoContrato.equals(BigDecimal.ZERO) && !porcentajeAnticipoContrato.equals(""))
		{	tieneAnticipo = true;			
		}
		Map<String, String > montosMeses = (Map<String, String>) context.get("montosMeses");
		Map<String, String > mapRetenciones = (Map<String, String>) context.get("mapRetenciones");
		Debug.log("mapRetenciones: " + mapRetenciones);
		try
		{	int sizeMap = montosMeses.size();			
			BigDecimal total = BigDecimal.ZERO;
			for(int i=1; i<=sizeMap; i++)
			{	Debug.log("MontosMeses: " + montosMeses.get(Integer.toString(i))+"TRIM");
				if(montosMeses.get(Integer.toString(i)) != null && !montosMeses.get(Integer.toString(i)).trim().equals(""))
				{	BigDecimal monto= new BigDecimal(montosMeses.get(Integer.toString(i)));
					total = total.add(monto);
				}				
			}			
			if(valorContrato.compareTo(total) != 0)
			{	return ServiceUtil.returnError("La suma de los montos mensuales no coincide con el valor del contrato sin IVA");		
			}			
			EntityCondition condObra = EntityCondition.makeCondition(EntityOperator.AND,
					  EntityCondition.makeCondition("obraId", EntityOperator.EQUALS, obraId));
			Debug.log("Omar - condObra: " + condObra);
			List<GenericValue> itemObraList = delegator.findByCondition("Obra", condObra, UtilMisc.toList("statusId", "organizationPartyId", "moneda", "productId", "acctgTagEnumIdAdmin",
																		"acctgTagEnumId1", "acctgTagEnumId2", "acctgTagEnumId3", "acctgTagEnumId4", "acctgTagEnumId5", "acctgTagEnumId6", "acctgTagEnumId7", 
																		"acctgTagEnumId8", "acctgTagEnumId9", "acctgTagEnumId10", "acctgTagEnumId11", "acctgTagEnumId12", "acctgTagEnumId13", "acctgTagEnumId14", 
																		"acctgTagEnumId15", "descripcion", "valorObra", "montoAutorizado"), null);
			Debug.log("Omar - itemObraList: " + itemObraList);			
			
			Map mapaMotor = null;
			Iterator<GenericValue> itemsObra = itemObraList.iterator();            
            if(itemsObra.hasNext()) 
            {   GenericValue item = itemsObra.next();                                        	
            	Debug.log("Status: " + item.getString("statusId"));
            	if(item.getString("statusId").equals(obraCreado) || item.getString("statusId").equals(obraTerminado))
            	{	return ServiceUtil.returnError("El contrato no puede ser iniciado. La obra no ha sido aprobada o ha sido finalizada");            		
            	}
            	if(item.getString("statusId").equals(obraAprobado))
            	{	//GenericValue obra = GenericValue.create(delegator.getModelEntity("Obra"));						    			
            		item.set("statusId", obraIniciado);    			
    				delegator.store(item);            	
            	}
            	
            	
            	BigDecimal valorObra = item.getBigDecimal("valorObra");
            	BigDecimal montoAutorizado = item.getBigDecimal("montoAutorizado");
            	Debug.log("valorObra: " + valorObra);
            	Debug.log("montoAutorizado: " + montoAutorizado);            	            	
            	
            	
            	if(montoAutorizado.compareTo(valorObra.add(valorContratoConIva)) < 0)
            		return ServiceUtil.returnError("La suma del monto de los periodos del contrato, excede el monto autorizado para la obra");
            	
            	
            	String organizationPartyId = item.getString("organizationPartyId");
            	String caParty = contratistaId;
            	String currency = item.getString("moneda");
            	String caProductId = productId;
            	int mesInicioInt = Integer.parseInt(mesInicio);
	            int anioInicioInt = Integer.parseInt(anioInicio);
            	Timestamp fechaTrans = new Timestamp(Calendar.getInstance().getTimeInMillis());
            	long secuencia = 0;
            	contratoId = delegator.getNextSeqId("ContratoObra");
            	EntityFieldMap ecl = EntityCondition.makeCondition(UtilMisc.toMap("obraId", obraId), EntityOperator.AND);
                long numeroContratos = delegator.findCountByCondition("ContratoObra", ecl, null, null);
                Debug.log("numeroContratos: " + numeroContratos);
                                            	            	
            	GenericValue contratoObra = GenericValue.create(delegator.getModelEntity("ContratoObra"));            	
            	contratoObra.set("contratoId", contratoId);
            	contratoObra.set("secuencia", UtilFormatOut.formatPaddedNumber((numeroContratos+1), 4));
            	contratoObra.set("valorContrato", valorContrato);
            	contratoObra.set("valorContratoConIva", valorContratoConIva);
            	contratoObra.set("iva", iva);
            	contratoObra.set("montoIva", montoIva);
            	contratoObra.set("porcentajeAnticipo", porcentajeAnticipoContrato);
            	contratoObra.set("montoAnticipo", montoAnticipo);
            	contratoObra.set("numContrato", numContrato);
            	contratoObra.set("descripcion", descripcion);            	
            	contratoObra.set("numReciboAnticipo", numReciboAnticipo);
            	contratoObra.set("numGarantiaAnticipo", numGarantiaAnticipo);
            	contratoObra.set("numGarantiaCumplimiento", numGarantiaCumplimiento);
            	contratoObra.set("contratistaId", contratistaId);
            	contratoObra.set("productId", productId);            	
            	contratoObra.set("obraId", obraId);
            	contratoObra.set("statusId", contratoIniciado);
            	contratoObra.set("moneda", moneda);
            	contratoObra.set("fechaRealInicio", fechaRealInicio);
            	contratoObra.set("fechaRealFin", fechaRealFin);
    			if(tieneAnticipo)
    			{	contratoObra.set("statusAnticipoId", contratoAnticipoPendiente);
    			}
    			else
    			{	contratoObra.set("statusAnticipoId", contratoSinAnticipo);    				
    			}
    			delegator.create(contratoObra);
    			    					            	
            	for(int i=1; i<=sizeMap; i++)
            	{   if(montosMeses.get(Integer.toString(i)) != null && !montosMeses.get(Integer.toString(i)).trim().equals(""))
            		{	Map<String, Object> input = FastMap.newInstance();	
            			Map<String, Object> output = FastMap.newInstance();
            			int indiceAdmin = UtilClavePresupuestal.indiceClasAdmin(tipoClave, organizationPartyId, delegator, ciclo);
			            String clavePresupuestal = "";
			            for(int j=1; j<16; j++)
			            {	
			            	String clasif = (String)item.getString("acctgTagEnumId" + j);
			            	
			            	if(indiceAdmin == j){
			            		clavePresupuestal = clavePresupuestal + UtilClavePresupuestal.buscaExternalId(item.getString("acctgTagEnumIdAdmin"),delegator);
			            	}
			            	if(clasif != null && !clasif.isEmpty()){
			            		clavePresupuestal = clavePresupuestal + UtilClavePresupuestal.buscaSequenceId(clasif, delegator);
			            	}
			            }
			            
			            Debug.log("clavePresupuestal: " + clavePresupuestal);
			            BigDecimal montoAfectar=new BigDecimal(montosMeses.get(Integer.toString(i)));
			            montoAfectar = montoAfectar.multiply(BigDecimal.valueOf(1.16));
			            Calendar fechaContableCal = GregorianCalendar.getInstance();
			            fechaContableCal.set(Calendar.DATE, 1);		    				            						    		
			    		if(mesInicioInt == 13)
			    		{	mesInicioInt = 1;
			    			fechaContableCal.set(Calendar.MONTH, mesInicioInt-1);
			    			fechaContableCal.set(Calendar.YEAR, anioInicioInt+1);		            	
			            }
			    		else
			    		{	fechaContableCal.set(Calendar.MONTH, mesInicioInt-1);
			    			fechaContableCal.set(Calendar.YEAR, anioInicioInt);		    			    		
			    		}
			    		mesInicioInt++;
			                                    		            
			            fechaContable = new Timestamp(fechaContableCal.getTimeInMillis());
			            Debug.log("Mes: " + fechaContable.getMonth()+1);
			            Debug.log("Ciclo: " + fechaContable.getYear()+1900);
	
//			            input.put("referenciaDocumento", null);
//			            input.put("tipoClave", tipoClave);                
//			            input.put("idTipoDoc", "COM_PE");
//			            input.put("fechaContable", fechaContable);
//			            input.put("fechaTransaccion", fechaTrans);
//			            input.put("organizationPartyId", organizationPartyId);
//			            input.put("partyId", null);
//			            input.put("monto", montoAfectar);
//			            input.put("caParty", caParty);
//			            input.put("currency", currency);
//			            input.put("secuencia", UtilFormatOut.formatPaddedNumber((secuencia+1), 4));
//			            input.put("contratoId", contratoId);
//			            input.put("userLogin", userLogin);
//			            input.put("agrupadorOrigen", "");
//			            input.put("caProductId", caProductId);                
//			            Debug.log("Va a invocar motor contable: " + input);
//			            mapaMotor = dispatcher.runSync("registroContable_Presupuestal", input);
//			            Debug.log("mapaMotor: " + mapaMotor);
			            
			            /************************************/
		                /******** Motor Contable ************/
		                /************************************/
			            
			            Map<String, Object> montoMap = FastMap.newInstance();
			            Map<String, Object> clavePresupuestalMap = FastMap.newInstance();
			            Map<String, Object> mapaProducto = FastMap.newInstance();
			            
			            montoMap.put("0", montoAfectar.toString());
			            clavePresupuestalMap.put("0", clavePresupuestal);
			            mapaProducto.put("0", productId);
			            
			            context.put("montoMap", montoMap);
			            context.put("clavePresupuestalMap", clavePresupuestalMap);
			       
			        	input.put("eventoContableId", "COM_PE");
			        	input.put("tipoClaveId", "EGRESO");
			        	input.put("fechaRegistro", fechaTrans);
			        	input.put("fechaContable", fechaContable);
			        	input.put("currency", currency);
			        	input.put("usuario", userLogin.getString("userLoginId"));
			        	input.put("organizationId", organizationPartyId);
			        	input.put("descripcion", "Creacion de contrato: " + contratoId);
			        	input.put("roleTypeId", "BILL_FROM_VENDOR");
			        	input.put("campo", "contratoId");
			        	input.put("valorCampo", contratoId);
			        	input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables("COM_PE", delegator, context, contratistaId, null, montoMap, mapaProducto));
			        	
			        	Debug.log("Entra: " + input);
			        	output = dispatcher.runSync("creaTransaccionMotor", input);
			        	
			        	if(ServiceUtil.isError(output)){
			        		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
			        	}
			        	GenericValue transaccion = (GenericValue) output.get("transaccion");
			        	Debug.log("Transaccion: " + output);
			            
			            GenericValue contratoPeriodo = GenericValue.create(delegator.getModelEntity("ContratoPeriodo"));
			            String contratoPeriodoId = delegator.getNextSeqId("ContratoPeriodo");
			            contratoPeriodo.set("contratoPeriodoId", contratoPeriodoId);
			            contratoPeriodo.set("contratoId", contratoId);
			            contratoPeriodo.set("secuencia", i);
			            contratoPeriodo.set("obraId", obraId);
			            contratoPeriodo.set("mes", fechaContable.getMonth()+1);
			            contratoPeriodo.set("ciclo", fechaContable.getYear()+1900);
			            contratoPeriodo.set("monto", montoAfectar);			            
		    			delegator.create(contratoPeriodo);		    					    				    			
            		}
            	}
            	            	            	
            	
    			
    			//Guardar retenciones
            	guardaRetencion(mapRetenciones, Integer.parseInt(numRetenciones), contratoId, delegator);
            	
            	//Actualiza el valor de la obra, utilizando el valor de los contratos que se van creando para esa obra            					
    			item.set("valorObra", valorObra.add(valorContratoConIva));    					
    			delegator.store(item);		
    			
    			if(tieneAnticipo)
    			{	GenericValue pendientesTesoreria = GenericValue.create(delegator.getModelEntity("PendientesTesoreria"));
	    			pendientesTesoreria.set("idPendienteTesoreria", contratoId);
	    			pendientesTesoreria.set("tipo", "CONTRATO");
	    			pendientesTesoreria.set("descripcion", descripcion);
	    			pendientesTesoreria.set("auxiliar", caParty);
	    			pendientesTesoreria.set("monto", montoAnticipo);    			
	    			pendientesTesoreria.set("estatus", contratoAnticipoPendiente);
	    			pendientesTesoreria.set("fechaTransaccion", fechaTrans);
	    			pendientesTesoreria.set("fechaContable", fechaContable);	    			
	    			pendientesTesoreria.set("moneda", currency);
	    			delegator.create(pendientesTesoreria);
	    			
	    			String correoOrigen = userLogin.getString("partyId");
	    			
	    			EntityCondition condTesorero = EntityCondition.makeCondition(EntityOperator.AND,
		   					EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, rolTesorero));		
					List<GenericValue> correoDestinatario = delegator.findByCondition("PartyRole", condTesorero, UtilMisc.toList("partyId"), null);					
					if(correoDestinatario.isEmpty())
					{	return ServiceUtil.returnError("No existe al menos un usuario con Rol de TESORERO para realizar el anticipo");			
					}
					else
					{	Iterator<GenericValue> tesoreroIter = correoDestinatario.iterator();
						while (tesoreroIter.hasNext())
						{	GenericValue generic = tesoreroIter.next();
							String correoDestino = generic.getString("partyId");
							UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,tipoWorkFlowId,CorreoModificado,
														   urlHost,obraId,correoOrigen,delegator,locale,dispatcher);
						}
					}	    				    													
    			}
    			Map<String, Object> result = ServiceUtil.returnSuccess();
    			result.put("obraId", obraId);
    			result.put("contratoId", contratoId);
    			return result;
            }								
			
		}
		catch (GenericEntityException e) {
            Debug.logError(e, MODULE);
            return ServiceUtil.returnError(e.getMessage());
        } catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String, Object> result = ServiceUtil.returnSuccess();		
		return result;		
	}

	private static void guardaRetencion(Map<String, String> mapRetenciones, int numRetenciones, String contratoId, Delegator delegator) throws GenericEntityException 
	{	for(int i=1; i<=numRetenciones; i++)
		{	Debug.log("mapRetenciones: " + mapRetenciones.get(Integer.toString(i))+"TRIM");
			if(mapRetenciones.get(Integer.toString(i)) != null && !mapRetenciones.get(Integer.toString(i)).trim().equals(""))
			{	String retencionId= mapRetenciones.get(Integer.toString(i));
				GenericValue ContratoRetencion = GenericValue.create(delegator.getModelEntity("ContratoRetencion"));
				ContratoRetencion.set("contratoId", contratoId);
				ContratoRetencion.set("retencionId", retencionId);
				ContratoRetencion.set("tipoContrato", "OBRA");				
				delegator.store(ContratoRetencion);
			}				
		}
		
	}
	
	public static Map finalizarObra(DispatchContext dctx, Map context) throws GenericEntityException{
		
		Delegator delegator = dctx.getDelegator();
		String obraId = (String) context.get("obraId");
		Map resultado = ServiceUtil.returnSuccess();
		
		EntityCondition condition = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("obraId", EntityOperator.EQUALS, obraId));
        
        List<GenericValue> contratos = delegator.findByCondition("ContratoObra", condition, null,
				null); 
        
        if(!contratos.isEmpty()){
        	Iterator<GenericValue> cont = contratos.iterator();

            while(cont.hasNext()){
            	GenericValue contrato = cont.next();
            	if(!contrato.getString("statusId").equals("TERMINADO_C")){
            		return ServiceUtil.returnError("Deben estar terminados los contratos de esta obra");
            	}
            }
        }
        
        return resultado;
        
		
	}	
	
	
	public static BigDecimal obtieneMontoContrato(Delegator delegator, String obraId, String contratoId) throws GenericEntityException{
		
		BigDecimal total = BigDecimal.ZERO;
		EntityCondition condContrato = EntityCondition.makeCondition(EntityOperator.AND,
				  EntityCondition.makeCondition("obraId", EntityOperator.EQUALS, obraId),
				  EntityCondition.makeCondition("contratoId", EntityOperator.EQUALS, contratoId));	
		List<GenericValue> itemContratoList = delegator.findByCondition("ContratoObra", condContrato, UtilMisc.toList("valorContratoConIva"), null);
		Iterator<GenericValue> itemsContrato = itemContratoList.iterator();
	    while (itemsContrato.hasNext()) 
	    {	GenericValue itemContrato = itemsContrato.next();
	    	total = itemContrato.getBigDecimal("valorContratoConIva");
	    }
        
        return total;
        
		
	}	 
	
}
