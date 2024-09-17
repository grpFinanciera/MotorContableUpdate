/* 
 */

package org.ofbiz.common.AB;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilHttp;
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

import javolution.util.FastMap;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * UtilAccountingTags - Utilities for the accounting tag system.
 */
public final class UtilWorkflow {

    private static final String MODULE = UtilWorkflow.class.getName();
    
	public final static String CorreoAutorizacion = "CorreoMensajeAutorizar";
	public final static String CorreoComentario = "CorreoMensajeComentario";
	public final static String CorreoAutorizado = "CorreoMensajeAutorizado";
	public final static String CorreoAutorizadoAuto = "CorreoMensajeAutorizadoAutorizador";
	public final static String CorreoRechazo = "CorreoMensajeRechazo";
	public final static String CorreoModificado = "CorreoMensajeModificado";
	
	public final static String estatusPendiente = "PENDIENTE";
	public final static String estatusAutorizada = "AUTORIZADA";
	public final static String estatusRechazada = "RECHAZADA_W";
	public final static String estatusCancelada = "CANCELADA_W";
	public final static String estatusComentada = "COMENTADA_W";
	
	public final static String tipoPedidoInterno = "PEDIDO_INTERNO";
	public final static String tipoTransferencia = "TRANSFERENCIA";
	
	
    /** Number of tags defined in <code>AcctgTagEnumType</code>. */
    public static final int TAG_COUNT = 15;

    private UtilWorkflow() { }
    
    /**
	 * Regresa una lista de StatusWorkFlow
	 * @param workFlowId
	 * @param statusId
	 * @param delegator
	 * @return
	 * @throws GenericEntityException
	 */
	public static List<GenericValue> getStatusWorkFlow(String workFlowId, String statusId,Delegator delegator) throws GenericEntityException{
		EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
				  EntityCondition.makeCondition("workFlowId", EntityOperator.EQUALS, workFlowId),
				  EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, statusId));
		List<GenericValue> statusWorkFlowList = delegator.findByCondition("StatusWorkFlow", condiciones, null, null);
		return statusWorkFlowList;
	}
	
	
	/**
	 * Arma el correo electronico para enviarlo
	 * @param partyIdOrigen
	 * @param partyIdDestino
	 * @param idTipoWorkflow
	 * @param mensaje
	 * @param url
	 * @param idTablaModulo
	 * @param partyIdMensaje1
	 * @param partyIdMensaje2
	 * @param delegator
	 * @param locale
	 * @param dispatcher
	 * @throws GenericEntityException
	 */
	public static void armarEnviarCorreo(String partyIdOrigen, String partyIdDestino,
			String idTipoWorkflow, String mensaje,String url,String idTablaModulo,
			String partyIdMensaje1,Delegator delegator,Locale locale, LocalDispatcher dispatcher) throws GenericEntityException {
		
		try {
			
			Map<String,Object> mapaParties = FastMap.newInstance();
			mapaParties.put("correoOrigen",partyIdOrigen);
			mapaParties.put("correoDestino",partyIdDestino);
			Map<String,Object> inputCorreo = FastMap.newInstance();
			inputCorreo.put("mapaParties", mapaParties);
			
			Map<String,Object> resultadoCorreos = dispatcher.runSync("obtenCorreosParty", inputCorreo);
			if(ServiceUtil.isError(resultadoCorreos)){
				throw new GenericEntityException(ServiceUtil.getErrorMessage(resultadoCorreos));
			}
			
			Map<?,?> mapaCorreos = (Map<?, ?>) resultadoCorreos.get("mapaParties");
			
			GenericValue person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId",partyIdMensaje1));
			StringBuffer nombre = new StringBuffer();
			if(UtilValidate.isNotEmpty(person)){
				nombre.append(UtilValidate.isEmpty(person.getString("firstName"))? "":person.getString("firstName")+" ");
				nombre.append(UtilValidate.isEmpty(person.getString("middleName"))? "":person.getString("middleName")+" ");
				nombre.append(UtilValidate.isEmpty(person.getString("lastName"))? "":person.getString("lastName"));
			}
			
			
			Map<String,Object> input = FastMap.newInstance();
			input.put("locale", locale);
			input.put("urlHost", url);
			input.put("correoOrigen", mapaCorreos.get("correoOrigen"));
			input.put("correoDestino", mapaCorreos.get("correoDestino"));
			input.put("tipoCorreo",mensaje);
			input.put("idTipoWorkflow",idTipoWorkflow);
			input.put("idTablaModulo",idTablaModulo);
			input.put("persona1",nombre.toString());
			
			Map<String,Object> resultado = dispatcher.runSync("armarEnviarCorreoWorkFlow", input);
			if(ServiceUtil.isError(resultado)){
				throw new GenericEntityException("No se pudo enviar el correo");
			}
		} catch (GenericServiceException e) {
			e.printStackTrace();
			throw new GenericEntityException(e.getMessage());
		}
	}

	/**
	 * Obtiene el autorizador en el nivel de autorizacion que se envia (secuencia)
	 * @param areaPartyId
	 * @param organizationPartyId
	 * @param secuencia
	 * @param delegator
	 * @return
	 * @throws GenericEntityException
	 */
	public static GenericValue obtenAutorizador(String areaPartyId, 
				String organizationPartyId,int secuencia, Delegator delegator, String tipoWorkFlow) throws GenericEntityException{
		List <GenericValue> autorizadores = obtenAutorizadores(areaPartyId, organizationPartyId, delegator, tipoWorkFlow);
		int cont = 1;
		for (GenericValue autor : autorizadores) {
			if(cont == secuencia){
				return autor;
			}
			cont++;
		}
		throw new GenericEntityException("No se pudo obtener el autorizador");
	}
	
	/**
	 * Se genera un registro en tabla CONTROL_WORKFLOW para el manejo del workflow
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> registroControlWorkflow(DispatchContext dctx, Map<?, ?> context, String origenId, String tipoWorkflow, String idWorkflow) throws GenericEntityException 
	{	Delegator delegator = dctx.getDelegator();
			Map<String, Object> result = ServiceUtil.returnSuccess();		
			GenericValue controlWorkflow = GenericValue.create(delegator.getModelEntity("ControlWorkFlow"));		
			controlWorkflow.set("workFlowId", idWorkflow);
			controlWorkflow.set("origenId", origenId);		
			controlWorkflow.set("tipoWorkFlowId", tipoWorkflow);
			delegator.createOrStore(controlWorkflow);
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
	public static Map<String, Object> registroEstatusWorkflow(DispatchContext dctx, Map<?, ?> context, String areaPartyId, String organizationPartyId, String idWorkflow, String estatus, String tipoWorkFlowId) throws GenericEntityException 
	{	
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		String estatusWorkflowVar = estatus;
		String autorizadorId = null;
		long secuencia = 0;
		
		try
		{		
			List<GenericValue> autorizadores = obtenAutorizadores(areaPartyId, organizationPartyId, delegator, tipoWorkFlowId);
			if(autorizadores.isEmpty())
			{	return ServiceUtil.returnError("El solicitante no tiene configurado un autorizador");
			}
			else
			{	Iterator<GenericValue> autorizadoresIter = autorizadores.iterator();
				
				while(autorizadoresIter.hasNext()){
					GenericValue generic = autorizadoresIter.next();
					if(tipoWorkFlowId.equals("PEDIDO_INTERNO")){
						if(validaAlmacenista(delegator, generic.getString("autorizadorId"), idWorkflow)){
							autorizadorId = generic.getString("autorizadorId");
							secuencia = generic.getLong("secuencia");
							break;
						}
					}else{
						autorizadorId = generic.getString("autorizadorId");
						secuencia = generic.getLong("secuencia");
						break;
					}
				}
				if(UtilValidate.isNotEmpty(autorizadorId)){
					GenericValue statusWorkFlow = GenericValue.create(delegator.getModelEntity("StatusWorkFlow"));
					String statusWorkFlowId = delegator.getNextSeqId("StatusWorkFlow");
					statusWorkFlow.set("statusWorkFlowId", statusWorkFlowId);
					statusWorkFlow.set("workFlowId", idWorkflow);
					statusWorkFlow.set("personParentId", autorizadorId);
					statusWorkFlow.set("statusId", estatusWorkflowVar);
					statusWorkFlow.set("nivelAutor", UtilFormatOut.formatPaddedNumber(secuencia, 4));
					delegator.create(statusWorkFlow);		
				}else{
					return ServiceUtil.returnError("El autorizador o los autorizadores registrados son encargados de otro almacen, por lo cual no puede ser utilizado en este flujo");
				}
			}
		}
		catch (GenericEntityException e) 
		{	return ServiceUtil.returnError(e.getMessage());
		}
		return result;
	}
	
	/**
	 * Metodo que valida si el autorizador es almacenista
	 * y en caso de que si, si pertenece a el almacen que se esta solicitando
	 * y de ser asi, se utiliza al usuario, sino se salta del flujo
	 * Esto solo es para pedidos internos
	 * @throws GenericEntityException 
	 */
	public static boolean validaAlmacenista(Delegator delegator, String autorizador, String idWorkflow) throws GenericEntityException{
		
		EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
				  EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, autorizador),
				  EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "ALMACENISTA"));
		List<GenericValue> almacenista = delegator.findByCondition("PartyRole",condiciones, null, null);
		if(!almacenista.isEmpty()){
			GenericValue work = delegator.findByPrimaryKey("ControlWorkFlow", UtilMisc.toMap("workFlowId",idWorkflow));
			GenericValue pedido = delegator.findByPrimaryKey("PedidoInterno", UtilMisc.toMap("pedidoInternoId",work.getString("origenId")));
			EntityCondition cond = EntityCondition.makeCondition(EntityOperator.AND,
					  EntityCondition.makeCondition("facilityId", EntityOperator.EQUALS, pedido.getString("almacenId")),
					  EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, autorizador));
			
			List<GenericValue> alm = delegator.findByCondition("FacilityPartyPermission", cond, null, null);
			if(!alm.isEmpty()){
				return true;
			}else{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Metodo que obtiene los autorizadores de un area 
	 * @param areaPartyId
	 * @param organizationPartyId
	 * @param delegator
	 * @return
	 * @throws GenericEntityException
	 */
	public static List<GenericValue> obtenAutorizadores(String areaPartyId, 
							String organizationPartyId,Delegator delegator, String tipoWorkFlowId) throws GenericEntityException{
		
		EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
				  EntityCondition.makeCondition("areaId", EntityOperator.EQUALS, areaPartyId),
				  EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS, organizationPartyId),
				  EntityCondition.makeCondition("tipoWorkFlowId", EntityOperator.EQUALS, tipoWorkFlowId),
				  EntityCondition.makeCondition("secuencia",EntityOperator.GREATER_THAN, "0"));
		Debug.log("Omar - condiciones: " + condiciones);
		List<String> orderBy = UtilMisc.toList("secuencia");
		Debug.log("Omar - areaPartyId: " + areaPartyId);
		Debug.log("Omar - organizationPartyId: " + organizationPartyId);
		Debug.log("Omar - tipoWorkFlowId: " + tipoWorkFlowId);
		List<GenericValue> autorizadores = delegator.findByCondition("Autorizador",condiciones, 
				UtilMisc.toList("autorizadorId","secuencia"), orderBy);
		Debug.log("Omar - autorizadores: " + autorizadores);
		
		return autorizadores;
	}
	
	/**
	 * Se obtienen los autorizadores por area. 
	 * Cambia el estatus de todos los autorizadores
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> cambiarStatusAutorizadores(DispatchContext dctx, Map<?, ?> context, String workFlowId, String estatus) throws GenericEntityException 
	{	Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();						
		try
		{	EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
					  					  EntityCondition.makeCondition("workFlowId", EntityOperator.EQUALS, workFlowId));
			List<GenericValue> statusWorkFlowList = delegator.findByCondition("StatusWorkFlow",condiciones, null, null);
			if(statusWorkFlowList.isEmpty())
			{	return ServiceUtil.returnError("No se han encontrado autorizadores asociados al Workflow");			
			}
			else
			{	Iterator<GenericValue> statusWorkFlowIter = statusWorkFlowList.iterator();
				while (statusWorkFlowIter.hasNext()) 
				{	GenericValue statusWorkFlow = statusWorkFlowIter.next();								
					statusWorkFlow.set("statusId", estatus);
					delegator.store(statusWorkFlow);
				}
			}
		}
		catch (GenericEntityException e) 
		{	return ServiceUtil.returnError(e.getMessage());
		}	
		return result;
	}
	
	/**
	 * Se crea un registro de comentario en la entidad ComentarioWorkFlow 
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> insertarComentario(DispatchContext dctx, Map<?, ?> context, String comentario, String workFlowId, String userLoginPartyId, String origenId) throws GenericEntityException 
	{	Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();						
		try
		{	GenericValue comentarioWorkFlow = GenericValue.create(delegator.getModelEntity("ComentarioWorkFlow"));
			String comentarioId = delegator.getNextSeqId("ComentarioWorkFlow");
			comentarioWorkFlow.set("comentarioId", comentarioId);
			comentarioWorkFlow.set("workFlowId", workFlowId);
			comentarioWorkFlow.set("comentario", comentario);
			comentarioWorkFlow.set("personaId", userLoginPartyId);
			comentarioWorkFlow.set("origenId", origenId);
			delegator.create(comentarioWorkFlow);				
		}
		catch (GenericEntityException e) 
		{	return ServiceUtil.returnError(e.getMessage());
		}
		
		return result;
	}
	
	/**
	 * Se obtienen los autorizadores por area. 
	 * Cambia el estatus de todos los autorizadores
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> cambiarStatusAutorizadorActual(DispatchContext dctx, Map<?, ?> context, String workFlowId, String estatus, String userLoginPartyId) throws GenericEntityException
	{	Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();						
		try
		{	EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
					  					  EntityCondition.makeCondition("workFlowId", EntityOperator.EQUALS, workFlowId),
					  					  EntityCondition.makeCondition("personParentId", EntityOperator.EQUALS, userLoginPartyId));
			List<GenericValue> statusWorkFlowList = delegator.findByCondition("StatusWorkFlow",condiciones, null, null);
			if(statusWorkFlowList.isEmpty())
			{	return ServiceUtil.returnError("No se han encontrado autorizadores asociados al Workflow");			
			}
			else
			{	Iterator<GenericValue> statusWorkFlowIter = statusWorkFlowList.iterator();
				while (statusWorkFlowIter.hasNext()) 
				{	GenericValue statusWorkFlow = statusWorkFlowIter.next();								
					statusWorkFlow.set("statusId", estatus);
					delegator.store(statusWorkFlow);
				}
			}
		}
		catch (GenericEntityException e) 
		{	return ServiceUtil.returnError(e.getMessage());
		}	
		return result;
	}
	
	/**
	 * Se obtienen el workflowId 
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static String obtenerWorkFlowId(DispatchContext dctx, Map<?, ?> context, String origenId, String tipoWorkFlowId) throws GenericEntityException
	{	Delegator delegator = dctx.getDelegator();
		String workFlowId = "";
		try
		{	EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
				  EntityCondition.makeCondition("origenId", EntityOperator.EQUALS, origenId),
				  EntityCondition.makeCondition("tipoWorkFlowId", EntityOperator.EQUALS, tipoWorkFlowId));
			List<GenericValue> workFlow = delegator.findByCondition("ControlWorkFlow", condiciones, UtilMisc.toList("workFlowId","tipoWorkFlowId"), null);		
			if(workFlow.isEmpty())
			{	throw new GenericEntityException("No se ha encontrado un Workflow asociado");								
			}			
			else
			{	Iterator<GenericValue> workFlowIter = workFlow.iterator();
				while (workFlowIter.hasNext()) 
				{	GenericValue generic = workFlowIter.next();								
					workFlowId = generic.getString("workFlowId");			
				}
			}
		}
		catch (GenericEntityException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return workFlowId;
	}
	
	/**
	 * Valida si el work flow es nuevo y regresa el id
	 * @param delegator
	 * @param origenId
	 * @param tipoWorkFlowId
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map<String,Object> validaNuevoWorkFlow(Delegator delegator, String origenId, String tipoWorkFlowId) throws GenericEntityException{
		
		EntityCondition condicionesWorkflowId = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("origenId", EntityOperator.EQUALS, origenId),
					EntityCondition.makeCondition("tipoWorkFlowId", EntityOperator.EQUALS, tipoWorkFlowId));		
		List<GenericValue> workflowId = delegator.findByCondition("ControlWorkFlow",condicionesWorkflowId, null, null);
		
		Map<String,Object> mapaRegreso = FastMap.newInstance();
		
		String workFlowId = "";
		Boolean nuevoWorkflow = true;
		if(workflowId.isEmpty())
		{	
			workFlowId = delegator.getNextSeqId("ControlWorkFlow");			
		}
		else
		{	
			Iterator<GenericValue> workflowIter = workflowId.iterator();
			while (workflowIter.hasNext())
			{	
				GenericValue generic = workflowIter.next();
				workFlowId = generic.getString("workFlowId");
				nuevoWorkflow = false;
			}
		}
		
		mapaRegreso.put("workFlowId", workFlowId);
		mapaRegreso.put("nuevoWorkflow", nuevoWorkflow);
		
		return mapaRegreso;
	}
	
	/**
	 * Obtiene la secuencia del autorizador
	 * @param delegator
	 * @param areaPartyId
	 * @param userLoginPartyId
	 * @param tipoWorkFlowId
	 * @return
	 * @throws GenericEntityException
	 */
	public static Long getSecuenciaAutorizador(Delegator delegator, String areaPartyId, 
			String userLoginPartyId, String tipoWorkFlowId) throws GenericEntityException{
		
		Long secuencia = new Long(0);
		//Obtener secuencia del autorizador actual
		EntityCondition condicionesSecuencia = EntityCondition.makeCondition(EntityOperator.AND,
				  EntityCondition.makeCondition("areaId", EntityOperator.EQUALS, areaPartyId),
				  EntityCondition.makeCondition("autorizadorId", EntityOperator.EQUALS, userLoginPartyId),
				  EntityCondition.makeCondition("tipoWorkFlowId", EntityOperator.EQUALS, tipoWorkFlowId));
		List<String> orderBy = UtilMisc.toList("secuencia");
		List<GenericValue> secuenciaAutorizador = delegator.findByCondition("Autorizador", condicionesSecuencia, UtilMisc.toList("secuencia"), orderBy);
		if(secuenciaAutorizador.isEmpty())
		{	
			throw new GenericEntityException("El autorizador actual tiene una secuencia");
		}			
		secuencia = secuenciaAutorizador.get(0).getLong("secuencia");
		
		return secuencia;
	}
	
	/**
	 * Obtiene el siguiente autorizador de una area a partir de una secuencia anterior
	 * @param delegator
	 * @param areaPartyId
	 * @param secuencia
	 * @return
	 * @throws GenericEntityException
	 */
	public static GenericValue getSiguienteAutorizador(Delegator delegator, String areaPartyId, Long secuenciaAnterior) throws GenericEntityException{
		
		List<String> usuariosActivos = UtilMisc.toList("PARTY_ENABLED", "LEAD_ASSIGNED");
		EntityCondition condicionesNuevoAutorizador = EntityCondition.makeCondition(EntityOperator.AND,
				  EntityCondition.makeCondition("areaId", EntityOperator.EQUALS, areaPartyId),
				  EntityCondition.makeCondition("secuencia", EntityOperator.GREATER_THAN, secuenciaAnterior),
				  EntityCondition.makeCondition("statusId", EntityOperator.IN, usuariosActivos),
				  EntityCondition.makeCondition("tipoWorkFlowId", EntityOperator.EQUALS, UtilWorkflow.tipoTransferencia));
		List<String> orderBy = UtilMisc.toList("secuencia");
		List<GenericValue> secuenciaNuevoAutorizador = delegator.findByCondition("AutorizadoresActivos", condicionesNuevoAutorizador, null, orderBy);
		
		if(UtilValidate.isNotEmpty(secuenciaNuevoAutorizador)){
			return secuenciaNuevoAutorizador.get(0);
		} else {
			return null;
		}
	}
	
	/**
	 * Guarda la entidad StatusWorkFlow
	 * @param delegator
	 * @param AutorizadorSiguiente
	 * @param workFlowId
	 * @throws GenericEntityException
	 */
	public static void guardaStatusWorkFlow(Delegator delegator, GenericValue AutorizadorSiguiente, String workFlowId) throws GenericEntityException{
		
		GenericValue statusWorkFlow = delegator.makeValue("StatusWorkFlow");
		statusWorkFlow.setNextSeqId();
		statusWorkFlow.set("workFlowId", workFlowId);
		statusWorkFlow.set("personParentId", AutorizadorSiguiente.getString("autorizadorId"));
		statusWorkFlow.set("statusId", UtilWorkflow.estatusPendiente);
		statusWorkFlow.set("nivelAutor", UtilFormatOut.formatPaddedNumber(AutorizadorSiguiente.getLong("secuencia"), 4));
		delegator.create(statusWorkFlow);
		
	}
	
	public static List<GenericValue> getComentarios(Delegator delegator, String origenId, String tipoWorkFlowId) 
			throws GenericServiceException {
    	List<GenericValue> comentario = null;
        
    	try 
    	{	EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
				  EntityCondition.makeCondition("origenId", EntityOperator.EQUALS, origenId),
				  EntityCondition.makeCondition("tipoWorkFlowId", EntityOperator.EQUALS, tipoWorkFlowId));
			List<String> orderBy = UtilMisc.toList("comentarioId");
			comentario = delegator.findByCondition("ObtenerComentarios", condiciones, null, orderBy);
        }
        catch (GenericEntityException e) 
        {	// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Debug.log("comentario: " + comentario);
        
        return comentario;
    }
    
    public static List<GenericValue> getAutorizadoresAprobados(Delegator delegator, String origenId, String tipoWorkFlowId) throws GenericServiceException {
    	List<GenericValue> autorizadores = null;
        
    	try {
    		List<GenericValue> generics = delegator.findByAnd(    	
				"ControlWorkFlow", "origenId", origenId,
				"tipoWorkFlowId", tipoWorkFlowId);
    		if(!generics.isEmpty()){
    			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
    					EntityCondition.makeCondition("workFlowId",
								EntityOperator.EQUALS, generics.get(0).getString("workFlowId")),
    					  EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "AUTORIZADA"));
    			autorizadores = delegator.findByCondition("AutorizadorAprobado", condiciones, null,UtilMisc.toList("lastUpdatedStamp DESC"));
    		}
        }
        catch (GenericEntityException e) 
        {	// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return autorizadores;
    }
    
    public static List<GenericValue> getComentariosViatico(Delegator delegator, String origenId) throws GenericServiceException {
    	List<GenericValue> comentario = null;
        
    	try 
    	{	EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
				  EntityCondition.makeCondition("origenId", EntityOperator.EQUALS, origenId),
				  EntityCondition.makeCondition("tipoWorkFlowId", EntityOperator.IN, UtilMisc.toList("VIATICOS","VIATICOSVI","VIATICOSGI")));
			List<String> orderBy = UtilMisc.toList("comentarioId");
			comentario = delegator.findByCondition("ObtenerComentarios", condiciones, null, orderBy);
        }
        catch (GenericEntityException e) 
        {	// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Debug.log("comentario: " + comentario);
        
        return comentario;
    }
    /**
     * Metodo generico para administrar las opciones de flujo (autorizar, rechazar, comentar)
     * 
     */
    public static String accionWorkFlow(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException
	{	
    	Delegator delegator = (Delegator) request.getAttribute("delegator");
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		DispatchContext dctx = dispatcher.getDispatchContext();
		Locale locale = (Locale) UtilHttp.getLocale(request);
		String entidad = (String) request.getParameter("entidad");
		String entidadId = (String) request.getParameter("entidadId");
		String tipoWorkFlowId = (String) request.getParameter("tipoWorkFlowId");
		String estatus = (String) request.getParameter("estatus");
		String accion = (String) request.getParameter("accion");
		GenericValue userLogin =(GenericValue) request.getSession().getAttribute("userLogin");
		String userLoginpartyId = userLogin.getString("partyId");
		String urlHost = (String) request.getParameter("urlHost");
		String comentario = (String) request.getParameter("comentario");
		String avisoTesorero = (String) request.getParameter("avisoTesorero");
		boolean crearPendienteTesoreria = (UtilValidate.isNotEmpty(avisoTesorero) && avisoTesorero.equals("Y"));
		String workFlowId=null;
		String area = "";
		String secuencia = "";
		String nuevoAutorizador = "";
		long nuevaSecuencia = 0;
		
		Map<String, String> context = FastMap.newInstance();
		
		try 
		{	
			String nombreLlave = delegator.getModelEntity(entidad).getFirstPkFieldName();
			
			if(UtilValidate.isEmpty(nombreLlave)){
				context.put("mensaje", "ERROR:El identificador de " + entidad + " no existe, por favor contacte al administrador");
				return doJSONResponse(response, context);
			}
			
			GenericValue nombreEntidad = delegator.findByPrimaryKey(entidad, UtilMisc.toMap(nombreLlave,entidadId));
			
			if(accion.equals("autorizar")){
				
				
				//Cambiar status de STATUS_WORKFLOW para el autorizador en curso*/			
				workFlowId = UtilWorkflow.obtenerWorkFlowId(dctx, context, entidadId, tipoWorkFlowId);
				UtilWorkflow.cambiarStatusAutorizadorActual(dctx, context, workFlowId, estatusAutorizada, userLoginpartyId);
				GenericValue areaSolicitante = delegator.findByPrimaryKey(entidad, UtilMisc.toMap(nombreLlave,entidadId));
				if(areaSolicitante.isEmpty())
				{	
					context.put("mensaje", "ERROR:El (La)" + entidad + "no contiene un area solicitante");
					return doJSONResponse(response, context);				
				}
				area = areaSolicitante.getString("areaId");
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
					context.put("mensaje", "ERROR:El autorizador actual no contiene una secuencia");
					return doJSONResponse(response, context);
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
				
				if(!secuenciaNuevoAutorizador.isEmpty())
				{	//Se crea registro de nuevo autorizador en la tabla STATUS_WORKFLOW
					nuevoAutorizador = secuenciaNuevoAutorizador.get(0).getString("autorizadorId");
					nuevaSecuencia = secuenciaNuevoAutorizador.get(0).getLong("secuencia");
					GenericValue statusWorkFlow = GenericValue.create(delegator.getModelEntity("StatusWorkFlow"));
					String statusWorkFlowId = delegator.getNextSeqId("StatusWorkFlow");
					statusWorkFlow.set("statusWorkFlowId", statusWorkFlowId);
					statusWorkFlow.set("workFlowId", workFlowId);
					statusWorkFlow.set("personParentId", nuevoAutorizador);
					statusWorkFlow.set("statusId", estatusPendiente);
					statusWorkFlow.set("nivelAutor", UtilFormatOut.formatPaddedNumber(nuevaSecuencia, 4));
					delegator.create(statusWorkFlow);		
					String correoOrigen = nombreEntidad.getString("solicitanteId");
					String correoDestino = secuenciaNuevoAutorizador.get(0).getString("autorizadorId");
					//Enviar correo electronico al solicitante
					UtilWorkflow.armarEnviarCorreo(userLoginpartyId,correoOrigen,tipoWorkFlowId,CorreoAutorizado,urlHost,entidadId,userLoginpartyId,delegator,locale,dispatcher);
					//Enviar correo electronico al nuevo autorizador
					UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,tipoWorkFlowId,CorreoAutorizado,urlHost,entidadId,null,delegator,locale,dispatcher);
					// Cambiar fecha de autorizacion por cada autorizador.
					nombreEntidad.put("fechaAutorizacion", UtilDateTime.nowTimestamp());
					nombreEntidad.store();
				}
				else
				{	
					nombreEntidad.set("statusId", estatus);
					nombreEntidad.store();
					String correoOrigen = userLoginpartyId;
					String correoDestino = nombreEntidad.getString("solicitanteId");
					//Enviar correo electronico al solicitante
					UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,tipoWorkFlowId,CorreoAutorizado,urlHost,entidadId,correoOrigen,delegator,locale,dispatcher);
					
					if(crearPendienteTesoreria){
						registraPendienteTesoreria(delegator, tipoWorkFlowId, nombreEntidad, estatus);
					}
					
				}
				//Insertar comentario
				if(comentario != null && !comentario.equals(""))
				{	
					UtilWorkflow.insertarComentario(dctx, context, comentario, workFlowId, userLoginpartyId, entidadId);					
				}
			}else if(accion.equals("comentar")){
				
				nombreEntidad.set("statusId", estatus);
				nombreEntidad.store();
				
				//Obtener el workFlowId		
				EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
						  					  EntityCondition.makeCondition("origenId", EntityOperator.EQUALS, entidadId),
						  					EntityCondition.makeCondition("tipoWorkFlowId", EntityOperator.EQUALS, tipoWorkFlowId));
				List<GenericValue> workFlow = delegator.findByCondition("ControlWorkFlow", condiciones, UtilMisc.toList("workFlowId","tipoWorkFlowId"), null);		
				if(workFlow.isEmpty())
				{
					context.put("mensaje", "ERROR:No se ha encontrado un Workflow asociado");
					return doJSONResponse(response, context);		
				}
				else
				{	Iterator<GenericValue> workFlowIter = workFlow.iterator();
					while (workFlowIter.hasNext()) 
					{	GenericValue generic = workFlowIter.next();								
						workFlowId = generic.getString("workFlowId");	
						tipoWorkFlowId = generic.getString("tipoWorkFlowId");
					}			
					//Actualizar STATUS_WORK_FLOW en rechazado SOLO para el autorizador actual
					if(UtilValidate.isNotEmpty(workFlowId))
					{	
						UtilWorkflow.cambiarStatusAutorizadorActual(dctx, context, workFlowId, estatusComentada, userLoginpartyId);
					}				
					//Insertar comentario
					if(comentario != null && !comentario.equals(""))
					{	UtilWorkflow.insertarComentario(dctx, context, comentario, workFlowId, userLoginpartyId, entidadId);					
					}			
				}
				
				//Enviar correo de notificacion al solicitante
				String correoOrigen = userLogin.getString("partyId");
				String correoDestino = nombreEntidad.getString("solicitanteId");
				UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,tipoWorkFlowId,CorreoComentario,
						urlHost,entidadId,correoOrigen,delegator,locale,dispatcher);
				
			}else{
				
				nombreEntidad.set("statusId", estatus);
				nombreEntidad.store();
				
				//Obtener el workFlowId		
				EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
						  					  EntityCondition.makeCondition("origenId", EntityOperator.EQUALS, entidadId),
						  					  EntityCondition.makeCondition("tipoWorkFlowId", EntityOperator.EQUALS, tipoWorkFlowId));
				List<GenericValue> workFlow = delegator.findByCondition("ControlWorkFlow",condiciones, UtilMisc.toList("workFlowId","tipoWorkFlowId"), null);		
				if(workFlow.isEmpty())
				{
					context.put("mensaje", "ERROR:No se ha encontrado un Workflow asociado");
					return doJSONResponse(response, context);		
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
					{	UtilWorkflow.cambiarStatusAutorizadores(dctx, context, workFlowId, estatusRechazada);
					}				
					//Insertar comentario
					if(comentario != null && !comentario.equals(""))
					{	UtilWorkflow.insertarComentario(dctx, context, comentario, workFlowId, userLoginpartyId, entidadId);					
					}			
				}
				
				//Enviar correo de notificacion al solicitante
				String correoOrigen = nombreEntidad.getString("solicitanteId");
				String correoDestino = userLogin.getString("partyId");
				UtilWorkflow.armarEnviarCorreo(correoDestino,correoOrigen,tipoWorkFlowId,CorreoRechazo,
						urlHost,entidadId,correoDestino,delegator,locale,dispatcher);
				
			}
		} 
		catch (GenericEntityException | NullPointerException e)
		{	
			context.put("mensaje", "ERROR:"+e.getMessage());
			Debug.logError(e.getMessage(), MODULE);
			return doJSONResponse(response, context);
		}
		context.put("mensaje", "SUCCESS");
		return doJSONResponse(response, context);
	}
    
    /**
     * Registra el pendiente de tesoreria en caso de ser necesario al autorizar un work flow
     * @param delegator
     * @param tipo
     * @param userLoginpartyId 
     * @param estatus 
     * @throws GenericEntityException 
     */
    public static void registraPendienteTesoreria(Delegator delegator, String tipo, GenericValue entidad, String estatus) throws GenericEntityException{
    	
		String campoDescripcion = new String("descripcion");
		String campoMoneda = new String("moneda");
		String campoMonto = new String("monto");
		String conceptoPago = new String("conceptoPago");
		String campoAuxiliar = null;
		
		switch (tipo) {
			case "GASTORESERVA":
				campoDescripcion = "concepto";
				campoMoneda = "tipoMoneda";
				conceptoPago = campoDescripcion;
				campoAuxiliar = "auxiliarId";
				break;
			default:
				break;
		}
		
		GenericValue pendientesTesoreria = GenericValue.create(delegator.getModelEntity("PendientesTesoreria"));
		pendientesTesoreria.set("idPendienteTesoreria", entidad.getString(entidad.getModelEntity().getFirstPkFieldName()));
		pendientesTesoreria.set("tipo", tipo);
		pendientesTesoreria.set("fechaTransaccion", UtilDateTime.nowTimestamp());
		pendientesTesoreria.set("fechaContable", UtilDateTime.nowTimestamp());
		pendientesTesoreria.set("descripcion", entidad.getString(campoDescripcion));
		pendientesTesoreria.set("moneda", entidad.getString(campoMoneda));
		pendientesTesoreria.set("estatus", estatus);
		pendientesTesoreria.set("monto", entidad.getBigDecimal(campoMonto));
		pendientesTesoreria.set("conceptoPago", entidad.getString(conceptoPago));
		if(UtilValidate.isNotEmpty(campoAuxiliar)){
			pendientesTesoreria.set("auxiliar", entidad.getString(campoAuxiliar));
		}
		
		delegator.create(pendientesTesoreria);
		
    }
    
    public static String doJSONResponse(HttpServletResponse response, JSONObject jsonObject) {
        return doJSONResponse(response, jsonObject.toString());
    }

    public static String doJSONResponse(HttpServletResponse response, Collection<?> collection) {
        return doJSONResponse(response, JSONArray.fromObject(collection).toString());
    }

    public static String doJSONResponse(HttpServletResponse response, Map map) {
        return doJSONResponse(response, JSONObject.fromObject(map));
    }

    public static String doJSONResponse(HttpServletResponse response, String jsonString) {
        String result = "success";

        response.setContentType("application/x-json");
        try {
            response.setContentLength(jsonString.getBytes("UTF-8").length);
        } catch (UnsupportedEncodingException e) {
            Debug.logWarning("Could not get the UTF-8 json string due to UnsupportedEncodingException: " + e.getMessage(), MODULE);
            response.setContentLength(jsonString.length());
        }

        Writer out;
        try {
            out = response.getWriter();
            out.write(jsonString);
            out.flush();
        } catch (IOException e) {
            Debug.logError(e, "Failed to get response writer", MODULE);
            result = "error";
        }
        return result;
    }
    
}
