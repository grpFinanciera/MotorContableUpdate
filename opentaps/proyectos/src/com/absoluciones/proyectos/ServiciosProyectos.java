package com.absoluciones.proyectos;

import java.io.File;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.foundation.infrastructure.InfrastructureException;

import javolution.util.FastMap;

public class ServiciosProyectos {
	
	private static String MODULE = ServiciosProyectos.class.getName();
	
	/**
	 * Crea Proyecto
	 * @param dctx
	 * @param context
	 * @return
	 */
	
	public static Map<String, Object> guardaProyecto(DispatchContext dctx,Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String partyId = userLogin.getString("partyId");
		String organizationPartyId = (String) context.get("organizationPartyId");
		Timestamp fechaInicio = (Timestamp) context.get("fechaInicio");
		String tipoMoneda = (String) context.get("tipoMoneda");
		String auxiliar = (String) context.get("auxiliar");
		String auxiliarProyecto = (String) context.get("auxiliarProyecto");
		String auxiliarCuentaCobrar = (String) context.get("auxiliarCuentaCobrar");
		String bancoIdOrigen = (String) context.get("bancoIdOrigen");
		String cuentaOrigen = (String) context.get("cuentaOrigen");
        String numConvenio = (String) context.get("numConvenio");
        String nombreOrganismo = (String) context.get("nombreOrganismo");
        String responsable = (String) context.get("responsable");
        String area = (String) context.get("partyId");
    	Timestamp fromDate = (Timestamp) context.get("fromDate");
    	Timestamp thruDate = (Timestamp) context.get("thruDate");
    	String acctgTransTypeId = (String) context.get("acctgTransTypeId");
    	//BigDecimal monto = (BigDecimal) context.get("monto");
    	String responsableT = (String) context.get("responsableT");
    	String observaciones = (String) context.get("observaciones");
    	String objetivo = (String) context.get("objetivo");
    	String productos = (String) context.get("productos");
    	String validacion = (String) context.get("validacion");

		try	{
			
			GenericValue nuevoProyecto = GenericValue.create(delegator.getModelEntity("Proyecto"));
			nuevoProyecto.setAllFields(context, false, null, null);
			nuevoProyecto.setNextSeqId();
			nuevoProyecto.set("fecha", fechaInicio);
			nuevoProyecto.set("convenioContrato", numConvenio);
			nuevoProyecto.set("organismoExterno", nombreOrganismo);
			nuevoProyecto.set("responsable", responsable);
			nuevoProyecto.set("responsableTecnico", responsableT);
			nuevoProyecto.set("fechaDesde", fromDate);
			nuevoProyecto.set("fechaHasta", thruDate);
			nuevoProyecto.set("acctgTransTypeId", acctgTransTypeId);
			nuevoProyecto.set("areaId",area);
			nuevoProyecto.set("solicitanteId",partyId);
			nuevoProyecto.set("statusId", "CREADO");
			nuevoProyecto.set("organizationPartyId", organizationPartyId);
			nuevoProyecto.set("observacionesCreacion", observaciones);
			nuevoProyecto.set("objetivo", objetivo);
			nuevoProyecto.set("tipoMoneda", tipoMoneda);
			nuevoProyecto.set("auxiliar", auxiliar);
			nuevoProyecto.set("auxiliarProyecto", auxiliarProyecto);
			nuevoProyecto.set("auxiliarCuentaCobrar", auxiliarCuentaCobrar);
			nuevoProyecto.set("bancoIdOrigen", bancoIdOrigen);
			nuevoProyecto.set("cuentaOrigen", cuentaOrigen);
			nuevoProyecto.set("productos", productos);
			if(validacion=="Si"){
				nuevoProyecto.set("validacion", "Y");
			}else{
				nuevoProyecto.set("validacion", "N");
			}
			
			delegator.create(nuevoProyecto);
			for (int i=1; i<=2; i++){
				
				long detalleId = 0;
				String detalleParticipanteId = null;
				GenericValue participante = GenericValue.create(delegator.getModelEntity("ParticipantesProyecto"));
				
				if(i==1){
					GenericValue nombreResponsable = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", responsable));
					detalleParticipanteId = UtilFormatOut.formatPaddedNumber((detalleId + 1), 4);
					participante.set("detalleParticipanteId", detalleParticipanteId);
					participante.set("proyectoId", nuevoProyecto.getString("proyectoId"));
					participante.set("nombre", nombreResponsable.getString("firstName"));
					participante.set("apellidos", nombreResponsable.getString("lastName"));
					participante.set("puesto", "Administrador/Responsable");
					participante.set("activo", "Y");
					participante.create();
				}else{
					GenericValue nombreResponsableT = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", responsableT));
					detalleParticipanteId = UtilFormatOut.formatPaddedNumber((detalleId + 2), 4);
					participante.set("detalleParticipanteId", detalleParticipanteId);
					participante.set("proyectoId", nuevoProyecto.getString("proyectoId"));
					participante.set("nombre", nombreResponsableT.getString("firstName"));
					participante.set("apellidos", nombreResponsableT.getString("lastName"));
					participante.set("puesto", "Responsable t�cnico");
					participante.set("activo", "Y");
					participante.create();
				}
				
			}	
			
			File directorio = new File(getUploadPathDirectorio()+"Proyecto_"+nuevoProyecto.getString("proyectoId"));
			directorio.mkdir();
			
			
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("proyectoId", nuevoProyecto.getString("proyectoId"));
			return result;
		}catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}
	
	/**
	 * Actualiza Proyecto
	 * @param dctx
	 * @param context
	 * @return
	 */
	
	public static Map<String, Object> actualizarProyecto(DispatchContext dctx,Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		
		String proyectoId = (String) context.get("proyectoId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String partyId = userLogin.getString("partyId");
		String area = (String) context.get("partyId");
    	String productos = (String) context.get("productos");
    	String validacion = (String) context.get("validacion");

		try	{
			
			GenericValue nuevoProyecto = delegator.findByPrimaryKey("Proyecto", UtilMisc.toMap("proyectoId", proyectoId));
			
			nuevoProyecto.set("areaId",area);
			nuevoProyecto.set("productos", productos);
			if(validacion=="Si"){
				nuevoProyecto.set("validacion", "Y");
			}else{
				nuevoProyecto.set("validacion", "N");
			}
			
			delegator.store(nuevoProyecto);
			
			
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("proyectoId", nuevoProyecto.getString("proyectoId"));
			return result;
		}catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}
	
	/**
	 * Cancela Proyecto
	 * @param dctx
	 * @param context
	 * @return
	 */
	
	public static Map<String, Object> cancelarProyecto(DispatchContext dctx,Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		
		String proyectoId = (String) context.get("proyectoId");
		Map<String, Object> result = ServiceUtil.returnSuccess();

		try	{
			
			GenericValue nuevoProyecto = delegator.findByPrimaryKey("Proyecto", UtilMisc.toMap("proyectoId", proyectoId));
			
			String polizaCreacion = nuevoProyecto.getString("acctgTransId");
			
			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
					  EntityCondition.makeCondition("acctgTransId", EntityOperator.EQUALS, polizaCreacion),
					  EntityCondition.makeCondition("acctgTransEntrySeqId",EntityOperator.LIKE,"%R"));
			
			List<GenericValue> detallesPoliza = delegator.findByCondition("AcctgTransEntry", condiciones, null, null);
			
			List<GenericValue> viaticos = delegator.findByCondition("ViaticosProyecto", EntityCondition.makeCondition("proyectoId", EntityOperator.EQUALS, proyectoId), null, null);
			List<GenericValue> gastosXComp = delegator.findByCondition("GastosXCompProyecto", EntityCondition.makeCondition("proyectoId", EntityOperator.EQUALS, proyectoId), null, null);
			List<GenericValue> adquisiciones = delegator.findByCondition("AdquisicionesProyecto", EntityCondition.makeCondition("proyectoId", EntityOperator.EQUALS, proyectoId), null, null);
			List<GenericValue> serviciosPersonales = delegator.findByCondition("ServiciosPersonalesProy", EntityCondition.makeCondition("proyectoId", EntityOperator.EQUALS, proyectoId), null, null);
			
			if(UtilValidate.isNotEmpty(viaticos)){
				result = ServiceUtil.returnError("No se puede cancelar el proyecto ya que existen viaticos asignados al proyecto");
			}else if (UtilValidate.isNotEmpty(gastosXComp)){
				result = ServiceUtil.returnError("No se puede cancelar el proyecto ya que existen gastos por comprobar asignados al proyecto");
			}else if (UtilValidate.isNotEmpty(adquisiciones)){
				result = ServiceUtil.returnError("No se puede cancelar el proyecto ya que existen adquisiciones asignados al proyecto");
			}else if (UtilValidate.isNotEmpty(serviciosPersonales)){
				result = ServiceUtil.returnError("No se puede cancelar el proyecto ya que existen servicios personales asignados al proyecto");
			}else if(UtilValidate.isEmpty(detallesPoliza)){
				result = ServiceUtil.returnError("No se puede cancelar el proyecto ya que aun no se revierte la p�liza de creaci�n");
			}else{

				nuevoProyecto.set("statusId","CANCELADO");				
				delegator.store(nuevoProyecto);
			}			
						
			result.put("proyectoId", nuevoProyecto.getString("proyectoId"));
			return result;
		}catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}
	
	public static Map<String, Object> agregaUsuariosProyecto(DispatchContext dctx,
			Map<String, Object> context){

		Delegator delegator = dctx.getDelegator();
        String proyectoId = (String) context.get("proyectoId");
		
		try {
			
			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
										  EntityCondition.makeCondition("proyectoId", EntityOperator.EQUALS, proyectoId));
			List<GenericValue> listParticipantes = delegator.findByCondition("ParticipantesProyecto",condiciones, null, UtilMisc.toList("detalleParticipanteId DESC"));
			
			long detalleId = 0;
			
			if(!listParticipantes.isEmpty()){
				detalleId = Long.valueOf(EntityUtil.getFirst(listParticipantes).getString("detalleParticipanteId"));
			}
			
			String detalleParticipanteId = UtilFormatOut.formatPaddedNumber((detalleId + 1), 4);
			
			GenericValue participante = GenericValue.create(delegator.getModelEntity("ParticipantesProyecto"));
			participante.setAllFields(context, true, null, null);
			participante.set("detalleParticipanteId", detalleParticipanteId);
			participante.set("activo", "Y");
			
			//Se agrega a la lista la nueva requisicion para validar la suficiencia presupuestal
			listParticipantes.add(participante);
			delegator.create(participante);
			
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("proyectoId", proyectoId);
			
			return result;
			
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
		
	}
	
	public static Map<String, Object> agregaPresupuestoProyecto(DispatchContext dctx,
			Map<String, Object> context){

		Delegator delegator = dctx.getDelegator();
        String proyectoId = (String) context.get("proyectoId");
        Timestamp fechaInicio = (Timestamp) context.get("fechaInicio");
		Timestamp fechaFin = (Timestamp) context.get("fechaFin");
        BigDecimal montoMes = (BigDecimal) context.get("monto");
		
		try {
			
			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
										  EntityCondition.makeCondition("proyectoId", EntityOperator.EQUALS, proyectoId));
			List<GenericValue> listPresupuesto = delegator.findByCondition("PresupuestoProyecto",condiciones, null, UtilMisc.toList("detallePresupuestoId DESC"));
			
			long detalleId = 0;
			
			if(!listPresupuesto.isEmpty()){
				detalleId = Long.valueOf(EntityUtil.getFirst(listPresupuesto).getString("detallePresupuestoId"));
			}
			
			String detallePresupuestoId = UtilFormatOut.formatPaddedNumber((detalleId + 1), 4);
			
			GenericValue presupuesto = GenericValue.create(delegator.getModelEntity("PresupuestoProyecto"));
			presupuesto.setAllFields(context, true, null, null);
			presupuesto.set("detallePresupuestoId", detallePresupuestoId);
			presupuesto.set("fechaInicial", fechaInicio);
			presupuesto.set("fechaFinal", fechaFin);
			
			//Se agrega a la lista la nueva requisicion para validar la suficiencia presupuestal
			listPresupuesto.add(presupuesto);
			
			GenericValue montoProyecto = delegator.findByPrimaryKey("Proyecto", UtilMisc.toMap("proyectoId", proyectoId));
		
			BigDecimal monto = BigDecimal.ZERO;
			
			if(UtilValidate.isNotEmpty(montoProyecto.getBigDecimal("monto"))){
				monto = montoProyecto.getBigDecimal("monto");
			}
			
			BigDecimal montoFinal = monto.add(montoMes);
			montoProyecto.set("monto", montoFinal);
			montoProyecto.set("montoRestante", BigDecimal.ZERO);
			
			delegator.store(montoProyecto);
			delegator.create(presupuesto);
			
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("proyectoId", proyectoId);
			
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
	public static Map<String, Object> cargarPaginaProyecto(DispatchContext dctx,
			Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		
		String proyectoId = (String) context.get("proyectoId");		

		GenericValue proyecto = GenericValue.create(delegator.getModelEntity("Proyecto"));
		if(proyectoId != null && !proyectoId.isEmpty())
			proyecto.set("proyectoId", proyectoId);
		
		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("proyectoId", proyecto.getString("proyectoId"));
		return result;
	}
	
	public static Map<String, Object> guardarProyecto(DispatchContext dctx,Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String proyectoId = (String) context.get("proyectoId");
		Map<String, Object> input = FastMap.newInstance();
	    Map<String, Object> output = FastMap.newInstance();
	    Map<String, Object> montoMap = FastMap.newInstance();
	    Map<String, Object> clavePresupuestalMap = FastMap.newInstance();
	    Map<String, Object> catalogoCargoContMap = FastMap.newInstance();
	    Map<String, Object> catalogoAbonoContMap = FastMap.newInstance();
	    Map<String, Object> context2 = FastMap.newInstance();
	    String poliza = null;
		

		try	{
			
			GenericValue nuevoProyecto = delegator.findByPrimaryKey("Proyecto", UtilMisc.toMap("proyectoId", proyectoId));
			String cuentaBancariaId = nuevoProyecto.getString("cuentaOrigen");
			String auxiliar = nuevoProyecto.getString("auxiliarCuentaCobrar");
			List<String> orderByLinea = UtilMisc.toList("secuencia");
	    	EntityCondition condicionLinea = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, nuevoProyecto.getString("acctgTransTypeId")));
			
			List<GenericValue> lineaList = delegator.findByCondition("LineaContable", condicionLinea, null, orderByLinea);
			Debug.logInfo("condicion: " + condicionLinea,MODULE);
			Debug.logInfo("lineaList: " + lineaList,MODULE);
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
		        	montoMap.put(descripcionLinea+"0", nuevoProyecto.getBigDecimal("monto").toString());
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
            Timestamp fechaContable = new Timestamp(nuevoProyecto.getDate("fecha").getTime());
            
            input.put("eventoContableId", nuevoProyecto.getString("acctgTransTypeId"));
        	input.put("tipoClaveId", "EGRESO");
        	input.put("fechaRegistro", UtilDateTime.nowTimestamp());
        	input.put("fechaContable", fechaContable);
        	input.put("currency", nuevoProyecto.getString("tipoMoneda"));
        	input.put("usuario", userLogin.getString("userLoginId"));
        	input.put("organizationId", nuevoProyecto.getString("organizationPartyId"));
        	input.put("descripcion", "Alta de "+ nuevoProyecto.getString("nombreProyecto"));
        	input.put("roleTypeId", "BILL_FROM_VENDOR");
        	input.put("valorCampo", proyectoId);
        	input.put("campo", "proyectoId");      
        	input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(nuevoProyecto.getString("acctgTransTypeId"), delegator, context2, null, null, null, null));
        	
        	
        	//Genera registro en DepreciacionResumen
        	output = dispatcher.runSync("creaTransaccionMotor", input);
        	Debug.log("output: " + output);
        	if(ServiceUtil.isError(output)){
        		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
        	}
        	
        	GenericValue transaccion = (GenericValue) output.get("transaccion");
        	poliza = transaccion.getString("poliza");
        	
        	nuevoProyecto.set("statusId", "VIGENTE");
        	nuevoProyecto.set("acctgTransId", transaccion.getString("acctgTransId"));
        	nuevoProyecto.store();

			Map<String, Object> result = ServiceUtil.returnSuccess("El proyecto ha sido creado con \u00e9xito con la poliza " +poliza);		

			result.put("proyectoId", proyectoId);
			return result;
		}catch (GenericEntityException | GenericServiceException e) 
			{	return ServiceUtil.returnError("Error al crear el proyecto: " + e.getMessage());
			}
	}
	
	public static Map<String, Object> guardaModProyecto(DispatchContext dctx,Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String proyectoId = (String) context.get("proyectoId");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		Map<String, Object> input = FastMap.newInstance();
	    Map<String, Object> output = FastMap.newInstance();
	    Map<String, Object> montoMap = FastMap.newInstance();
	    Map<String, Object> clavePresupuestalMap = FastMap.newInstance();
	    Map<String, Object> catalogoCargoContMap = FastMap.newInstance();
	    Map<String, Object> catalogoAbonoContMap = FastMap.newInstance();
	    Map<String, Object> context2 = FastMap.newInstance();
	    String poliza = null;
		BigDecimal montoSumado = BigDecimal.ZERO;
		Map<String, Object> result = ServiceUtil.returnSuccess();
		String evento = null;

		try	{
			
			
			EntityCondition condicionEtapas = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("proyectoId", EntityOperator.EQUALS, proyectoId));
			List<GenericValue> etapasProyecto = delegator.findByCondition("PresupuestoProyecto", condicionEtapas, null, null);
			EntityCondition condicionEtapasMod = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("proyectoId", EntityOperator.EQUALS, proyectoId),
					EntityCondition.makeCondition("status", EntityOperator.EQUALS, "NO_APLICADO"));
			List<GenericValue> etapasModProyecto = delegator.findByCondition("ModificacionPresupuestoProyecto", condicionEtapasMod, null, null);
			GenericValue nuevoProyecto = delegator.findByPrimaryKey("Proyecto", UtilMisc.toMap("proyectoId", proyectoId));
			
			for (int i=0; i<etapasModProyecto.size();i++){
				boolean crear=true;
				for(int j=0; j<etapasProyecto.size(); j++){
					if(etapasModProyecto.get(i).getString("detallePresupuestoId").equals(etapasProyecto.get(j).getString("detallePresupuestoId"))){
						if(etapasModProyecto.get(i).getString("tipoMovimiento").equals("Ampliacion")){
							BigDecimal monto = etapasProyecto.get(j).getBigDecimal("monto").add(etapasModProyecto.get(i).getBigDecimal("monto"));
							montoSumado = montoSumado.add(etapasModProyecto.get(i).getBigDecimal("monto"));
							etapasProyecto.get(j).set("monto", monto);
						}else{
							BigDecimal monto = etapasProyecto.get(j).getBigDecimal("monto").subtract(etapasModProyecto.get(i).getBigDecimal("monto"));
							montoSumado = montoSumado.subtract(etapasModProyecto.get(i).getBigDecimal("monto"));
							etapasProyecto.get(j).set("monto", monto);
						}
						delegator.store(etapasProyecto.get(j));
						crear=false;
					}
				}
				
				if(crear){
					
					montoSumado = montoSumado.add(etapasModProyecto.get(i).getBigDecimal("monto"));
					GenericValue presupuesto = GenericValue.create(delegator.getModelEntity("PresupuestoProyecto"));
					presupuesto.set("proyectoId", proyectoId);
					presupuesto.set("detallePresupuestoId", etapasModProyecto.get(i).getString("detallePresupuestoId"));
					presupuesto.set("mes", etapasModProyecto.get(i).getString("mes"));
					presupuesto.set("fechaInicial", etapasModProyecto.get(i).getString("fechaInicial"));
					presupuesto.set("fechaFinal", etapasModProyecto.get(i).getString("fechaFinal"));
					presupuesto.set("monto", etapasModProyecto.get(i).getBigDecimal("monto"));
					delegator.create(presupuesto);
				}
				
				etapasModProyecto.get(i).set("status", "APLICADO");
				delegator.store(etapasModProyecto.get(i));
			}
			
			BigDecimal montoProyecto = nuevoProyecto.getBigDecimal("monto");
			BigDecimal montoRestanteProyecto = nuevoProyecto.getBigDecimal("montoRestante");
			
			nuevoProyecto.set("monto", montoProyecto.add(montoSumado));
			//nuevoProyecto.set("montoRestante", montoRestanteProyecto.add(montoSumado));
			delegator.store(nuevoProyecto);
			
			
			if(!(montoSumado.compareTo(BigDecimal.ZERO)==0)){
				String cuentaBancariaId = nuevoProyecto.getString("cuentaOrigen");
				String auxiliar = nuevoProyecto.getString("auxiliar");
				List<String> orderByLinea = UtilMisc.toList("secuencia");
				
				if(montoSumado.compareTo(BigDecimal.ZERO)<0){
					evento="RED_PROY";
				}else if (montoSumado.compareTo(BigDecimal.ZERO)>0){
					evento="AMP_PROY";
				}
		    	EntityCondition condicionLinea = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, evento));
				
				List<GenericValue> lineaList = delegator.findByCondition("LineaContable", condicionLinea, null, orderByLinea);
				Debug.logInfo("condicion: " + condicionLinea,MODULE);
				Debug.logInfo("lineaList: " + lineaList,MODULE);
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
			        	montoMap.put(descripcionLinea+"0", montoSumado.abs().toString());
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
	            
	            input.put("eventoContableId", evento);
	        	input.put("tipoClaveId", "EGRESO");
	        	input.put("fechaRegistro", UtilDateTime.nowTimestamp());
	        	input.put("fechaContable", fechaContable);
	        	input.put("currency", nuevoProyecto.getString("tipoMoneda"));
	        	input.put("usuario", userLogin.getString("userLoginId"));
	        	input.put("organizationId", nuevoProyecto.getString("organizationPartyId"));
	        	input.put("descripcion", "Modificaci�n de "+ nuevoProyecto.getString("nombreProyecto"));
	        	input.put("roleTypeId", "BILL_FROM_VENDOR");
	        	input.put("valorCampo", proyectoId);
	        	input.put("campo", "proyectoId");      
	        	input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(evento, delegator, context2, null, null, null, null));
	        	
	        	
	        	//Genera registro en DepreciacionResumen
	        	output = dispatcher.runSync("creaTransaccionMotor", input);
	        	Debug.log("output: " + output);
	        	if(ServiceUtil.isError(output)){
	        		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
	        	}
	        	
	        	GenericValue transaccion = (GenericValue) output.get("transaccion");
	        	poliza = transaccion.getString("poliza");
	        	
	        	nuevoProyecto.set("statusId", "VIGENTE");
	        	nuevoProyecto.set("acctgTransId", transaccion.getString("acctgTransId"));
	        	nuevoProyecto.store();

				result = ServiceUtil.returnSuccess("El proyecto ha sido modificado con \u00e9xito con la poliza " +poliza);		

				result.put("proyectoId", proyectoId);
				return result;
			}
			return result;
		}catch (GenericEntityException | GenericServiceException e) 
			{	return ServiceUtil.returnError("Error al modificar el proyecto: " + e.getMessage());
			}
	}
	
	public static Map<String, Object> concluirInteresComisionProyecto(DispatchContext dctx,Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String proyectoId = (String) context.get("proyectoId");
		String etapa = (String) context.get("detallePresupuestoId");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		BigDecimal monto = (BigDecimal) context.get("monto");
		String tipoMovimiento = (String) context.get("tipoMovimiento");
		Map<String, Object> input = FastMap.newInstance();
	    Map<String, Object> output = FastMap.newInstance();
	    Map<String, Object> montoMap = FastMap.newInstance();
	    Map<String, Object> clavePresupuestalMap = FastMap.newInstance();
	    Map<String, Object> catalogoCargoContMap = FastMap.newInstance();
	    Map<String, Object> catalogoAbonoContMap = FastMap.newInstance();
	    Map<String, Object> context2 = FastMap.newInstance();
	    String poliza = null;
		Map<String, Object> result = ServiceUtil.returnSuccess();
		String evento = null;

		try	{
			
			GenericValue nuevoProyecto = delegator.findByPrimaryKey("Proyecto", UtilMisc.toMap("proyectoId", proyectoId));
			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
					  EntityCondition.makeCondition("proyectoId", EntityOperator.EQUALS, proyectoId));
			
			
			List<GenericValue> listPresupuesto = delegator.findByCondition("InteresComisionProyecto",condiciones, null, UtilMisc.toList("intComId DESC"));
			
			long detalleId = 0;
			
			if(!listPresupuesto.isEmpty()){
				detalleId = Long.valueOf(EntityUtil.getFirst(listPresupuesto).getString("intComId"));
			}
			
			String detallePresupuestoId = UtilFormatOut.formatPaddedNumber((detalleId + 1), 4);
						
			GenericValue presupuesto = GenericValue.create(delegator.getModelEntity("InteresComisionProyecto"));
			presupuesto.set("intComId",detallePresupuestoId);
			presupuesto.set("proyectoId", proyectoId);
			presupuesto.set("fechaContable", fechaContable);
			presupuesto.set("detallePresupuestoId", etapa);
			presupuesto.set("monto", monto);
			presupuesto.set("tipoMovimiento", tipoMovimiento);
			
				String cuentaBancariaId = nuevoProyecto.getString("cuentaOrigen");
				String auxiliar = nuevoProyecto.getString("auxiliarProyecto");
				List<String> orderByLinea = UtilMisc.toList("secuencia");
				
				if(tipoMovimiento.equals("Intereses")){
					evento="INTER_PROY";
				}else if (tipoMovimiento.equals("Comisiones")){
					evento="COMIS_PROY";
				}
		    	EntityCondition condicionLinea = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, evento));
				
				List<GenericValue> lineaList = delegator.findByCondition("LineaContable", condicionLinea, null, orderByLinea);
				Debug.logInfo("condicion: " + condicionLinea,MODULE);
				Debug.logInfo("lineaList: " + lineaList,MODULE);
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
			        	montoMap.put(descripcionLinea+"0", monto.abs().toString());
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
	            
	            input.put("eventoContableId", evento);
	        	input.put("tipoClaveId", "EGRESO");
	        	input.put("fechaRegistro", UtilDateTime.nowTimestamp());
	        	input.put("fechaContable", fechaContable);
	        	input.put("currency", nuevoProyecto.getString("tipoMoneda"));
	        	input.put("usuario", userLogin.getString("userLoginId"));
	        	input.put("organizationId", nuevoProyecto.getString("organizationPartyId"));
	        	input.put("descripcion", "Modificaci�n de "+ nuevoProyecto.getString("nombreProyecto"));
	        	input.put("roleTypeId", "BILL_FROM_VENDOR");
	        	input.put("valorCampo", proyectoId);
	        	input.put("campo", "proyectoId");      
	        	input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(evento, delegator, context2, null, null, null, null));
	        	
	        	
	        	//Genera registro en DepreciacionResumen
	        	output = dispatcher.runSync("creaTransaccionMotor", input);
	        	Debug.log("output: " + output);
	        	if(ServiceUtil.isError(output)){
	        		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
	        	}
	        	
	        	GenericValue transaccion = (GenericValue) output.get("transaccion");
	        	poliza = transaccion.getString("poliza");
	        	
	        	presupuesto.set("acctgTransId", transaccion.getString("acctgTransId"));
	        	

				delegator.create(presupuesto);

				result = ServiceUtil.returnSuccess("Se ha registrado el interes/comision correctamente con la p�liza " +poliza);		

				result.put("proyectoId", proyectoId);
				
			return result;
		}catch (GenericEntityException | GenericServiceException e) 
			{	return ServiceUtil.returnError("Error al modificar el proyecto: " + e.getMessage());
			}
	}
	
	public static Map<String, Object> cierreProyecto(DispatchContext dctx,Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String proyectoId = (String) context.get("proyectoId");
		Timestamp fechaCierre = (Timestamp) context.get("fechaCierre");
		String comentarioCierre = (String) context.get("comentarioCierre");
		String acctgTransTypeIdCierre = (String) context.get("acctgTransTypeIdCierre");
		Map<String, Object> input = FastMap.newInstance();
	    Map<String, Object> output = FastMap.newInstance();
	    Map<String, Object> montoMap = FastMap.newInstance();
	    Map<String, Object> clavePresupuestalMap = FastMap.newInstance();
	    Map<String, Object> catalogoCargoContMap = FastMap.newInstance();
	    Map<String, Object> catalogoAbonoContMap = FastMap.newInstance();
	    Map<String, Object> context2 = FastMap.newInstance();
	    Map<String, Object> inputBienes = FastMap.newInstance();
	    Map<String, Object> outputBienes = FastMap.newInstance();
	    Map<String, Object> montoMapBienes = FastMap.newInstance();
	    Map<String, Object> clavePresupuestalMapBienes = FastMap.newInstance();
	    Map<String, Object> catalogoCargoContMapBienes = FastMap.newInstance();
	    Map<String, Object> catalogoAbonoContMapBienes = FastMap.newInstance();
	    Map<String, Object> context2Bienes = FastMap.newInstance();
	    String poliza = null;
	    String polizaBienes = null;
		

		try	{
			
			GenericValue nuevoProyecto = delegator.findByPrimaryKey("Proyecto", UtilMisc.toMap("proyectoId", proyectoId));
			
			List<String> orderByLinea = UtilMisc.toList("secuencia");
	    	EntityCondition condicionLinea = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, acctgTransTypeIdCierre));
			
			List<GenericValue> lineaList = delegator.findByCondition("LineaContable", condicionLinea, null, orderByLinea);
			Debug.logInfo("condicion: " + condicionLinea,MODULE);
			Debug.logInfo("lineaList: " + lineaList,MODULE);
			
			EntityCondition condicionBienesMuebles = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("proyectoId", EntityOperator.EQUALS, proyectoId),
					EntityCondition.makeCondition("productTypeId", EntityOperator.EQUALS, "BIEN_MUEBLE"),
					EntityCondition.makeCondition("statusId", EntityOperator.IN, UtilMisc.toList("FINALIZADO", "PAGADO")));
			
			List<GenericValue> bienesMuebles = delegator.findByCondition("BuscarBienesMueblesProyectos", condicionBienesMuebles, null, null);
			
			BigDecimal totalBienes = BigDecimal.ZERO;
			
			for(GenericValue bienes : bienesMuebles){
				BigDecimal cantidad = new BigDecimal(bienes.getLong("cantidad"));
				totalBienes = totalBienes.add(cantidad.multiply(bienes.getBigDecimal("monto")));
			}
			
			BigDecimal totalProyecto = nuevoProyecto.getBigDecimal("monto").subtract(nuevoProyecto.getBigDecimal("montoRestante"));
			
			
			
			
			if(!lineaList.isEmpty())
			{	Iterator<GenericValue> listaIter = lineaList.iterator();	        
				while (listaIter.hasNext())
				{
					GenericValue item = listaIter.next();
					String descripcionLinea = item.getString("descripcion");
					descripcionLinea = descripcionLinea.replaceAll(" ", "");
					catalogoCargoContMap.put(descripcionLinea + "0",
							item.getString("catalogoCargo") != null
									&& item.getString("catalogoCargo").equalsIgnoreCase("BANCO") ? nuevoProyecto.getString("cuentaOrigen")
											: nuevoProyecto.getString("auxiliar"));
					catalogoAbonoContMap.put(descripcionLinea + "0",
							item.getString("catalogoAbono") != null
									&& item.getString("catalogoAbono").equalsIgnoreCase("BANCO") ? nuevoProyecto.getString("cuentaOrigen")
											: nuevoProyecto.getString("auxiliarProyecto"));
					if(totalBienes.compareTo(BigDecimal.ZERO)==1){
						BigDecimal totalProyectoSinMuebles = totalProyecto.subtract(totalBienes);
						montoMap.put(descripcionLinea+"0", totalProyectoSinMuebles.toString());
					}else{
						montoMap.put(descripcionLinea+"0", totalProyecto.toString());
					}
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
            Timestamp fechaContable = fechaCierre;
            
            if(totalProyecto.compareTo(BigDecimal.ZERO)!=0){
            
	            input.put("eventoContableId", acctgTransTypeIdCierre);
	        	input.put("tipoClaveId", "EGRESO");
	        	input.put("fechaRegistro", UtilDateTime.nowTimestamp());
	        	input.put("fechaContable", fechaContable);
	        	input.put("currency", nuevoProyecto.getString("tipoMoneda"));
	        	input.put("usuario", userLogin.getString("userLoginId"));
	        	input.put("organizationId", nuevoProyecto.getString("organizationPartyId"));
	        	input.put("descripcion", "Cierre de "+ nuevoProyecto.getString("nombreProyecto"));
	        	input.put("roleTypeId", "BILL_FROM_VENDOR");
	        	input.put("valorCampo", proyectoId);
	        	input.put("campo", "proyectoId");      
	        	input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(acctgTransTypeIdCierre, delegator, context2, null, null, null, null));
        	
        	
        	//Genera registro contable del cierre del proyecto en caso de que el monto restante del mismo no sea 0
	        	output = dispatcher.runSync("creaTransaccionMotor", input);
	        	Debug.log("output: " + output);
	        	if(ServiceUtil.isError(output)){
	        		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
	        	}
	        	GenericValue transaccion = (GenericValue) output.get("transaccion");
	        	poliza = transaccion.getString("poliza");
	        	nuevoProyecto.set("acctgTransIdCierre", transaccion.getString("acctgTransId"));
        	}
        	        	
        	
        	
        	if (totalBienes.compareTo(BigDecimal.ZERO) == 1) {
				List<String> orderByLineaDevo = UtilMisc.toList("secuencia");
				EntityCondition condicionLineaDevo = EntityCondition.makeCondition(EntityOperator.AND,
						EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, acctgTransTypeIdCierre));
				List<GenericValue> lineaListDevo = delegator.findByCondition("LineaContable", condicionLineaDevo, null,
						orderByLineaDevo);
				Debug.logInfo("condicion: " + condicionLinea, MODULE);
				Debug.logInfo("lineaList: " + lineaList, MODULE);

				if (!lineaListDevo.isEmpty()) {
					Iterator<GenericValue> listaIter = lineaListDevo.iterator();
					while (listaIter.hasNext()) {
						
						GenericValue item = listaIter.next();
						String descripcionLinea = item.getString("descripcion");
						descripcionLinea = descripcionLinea.replaceAll(" ", "");
						catalogoCargoContMapBienes.put(descripcionLinea + "0",
								item.getString("catalogoCargo") != null
										&& item.getString("catalogoCargo").equalsIgnoreCase("BANCO") ? nuevoProyecto.getString("cuentaOrigen")
												: nuevoProyecto.getString("auxiliar"));
						catalogoAbonoContMapBienes.put(descripcionLinea + "0",
								item.getString("catalogoAbono") != null
										&& item.getString("catalogoAbono").equalsIgnoreCase("BANCO") ? nuevoProyecto.getString("cuentaOrigen")
												: nuevoProyecto.getString("auxiliarProyecto"));
						montoMapBienes.put(descripcionLinea + "0", totalBienes.toString());
					}
				}
				clavePresupuestalMapBienes.put("0", "");

				context2Bienes.put("montoMap", montoMapBienes);
				context2Bienes.put("clavePresupuestalMap", clavePresupuestalMapBienes);
				context2Bienes.put("catalogoCargoContMap", catalogoCargoContMapBienes);
				context2Bienes.put("catalogoAbonoContMap", catalogoAbonoContMapBienes);

				Debug.log("montoMap: " + montoMapBienes);
				Debug.log("clavePresupuestalMap: " + clavePresupuestalMapBienes);
				Debug.log("catalogoCargoContMap: " + catalogoCargoContMapBienes);
				Debug.log("catalogoAbonoContMap: " + catalogoAbonoContMapBienes);

				// Invocacion a motor contable
				/***** MOTOR CONTABLE *****/

				inputBienes.put("eventoContableId", acctgTransTypeIdCierre);
				inputBienes.put("tipoClaveId", "EGRESO");
				inputBienes.put("fechaRegistro", UtilDateTime.nowTimestamp());
				inputBienes.put("fechaContable", fechaContable);
				inputBienes.put("currency", nuevoProyecto.getString("tipoMoneda"));
				inputBienes.put("usuario", userLogin.getString("userLoginId"));
				inputBienes.put("organizationId", nuevoProyecto.getString("organizationPartyId"));
				inputBienes.put("descripcion", "Cierre de "+ nuevoProyecto.getString("nombreProyecto"));
				inputBienes.put("roleTypeId", "BILL_FROM_VENDOR");
				inputBienes.put("valorCampo", proyectoId);
				inputBienes.put("campo", "proyectoId");      
				inputBienes.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(acctgTransTypeIdCierre, delegator, context2Bienes, null, null, null, null));

				// Genera registro en DepreciacionResumen
				outputBienes = dispatcher.runSync("creaTransaccionMotor", inputBienes);
				Debug.log("output: " + outputBienes);
				if (ServiceUtil.isError(outputBienes)) {
					return ServiceUtil.returnError(ServiceUtil.getErrorMessage(outputBienes));
				}

				GenericValue transaccionBienes = (GenericValue) outputBienes.get("transaccion");
				polizaBienes = transaccionBienes.getString("poliza");
				nuevoProyecto.set("acctgTransIdCierreBienes", transaccionBienes.getString("acctgTransId"));
			}
        	
        	
        	if(nuevoProyecto.getBigDecimal("montoRestante").compareTo(BigDecimal.ZERO)==1){
        		nuevoProyecto.set("statusId", "DEVOLUCION");
        	}else{
        		nuevoProyecto.set("statusId", "FINALIZADO");
        	}
        	
        	nuevoProyecto.set("fechaCierre", fechaCierre);
        	if(UtilValidate.isNotEmpty(comentarioCierre)){
        		nuevoProyecto.set("observacionesCierre", comentarioCierre);
        	}
        	nuevoProyecto.store();
        	
			Map<String, Object> result = null;		
			if (totalBienes.compareTo(BigDecimal.ZERO) == 1) {
				if(UtilValidate.isNotEmpty(poliza)){
					result = ServiceUtil.returnSuccess("El proyecto ha sido finalizado con \u00e9xito con las polizas " +poliza +", " + polizaBienes);
				}else{
					result = ServiceUtil.returnSuccess("El proyecto ha sido finalizado con \u00e9xito con la poliza de bienes " + polizaBienes);
				}
			}else{
				if(UtilValidate.isNotEmpty(poliza)){
					result = ServiceUtil.returnSuccess("El proyecto ha sido finalizado con \u00e9xito con la poliza " +poliza);
				}else{
					result = ServiceUtil.returnSuccess("El proyecto ha sido finalizado con \u00e9xito");
				}
			}
			result.put("proyectoId", proyectoId);
			return result;
		}catch (GenericEntityException | GenericServiceException e) 
			{	return ServiceUtil.returnError("Error al crear el proyecto: " + e.getMessage());
			}
	}
	
	/**
	 * Crea Objetos Gasto para proyectos
	 * @param dctx
	 * @param context
	 * @return
	 */
	
	public static Map<String, Object> guardaObjetoGasto(DispatchContext dctx,Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String partyId = userLogin.getString("partyId");
		String organizationPartyId = (String) context.get("organizationPartyId");

		try	{
			
			GenericValue nuevoGastoProy = GenericValue.create(delegator.getModelEntity("ObjetoGastoProyecto"));
			nuevoGastoProy.setAllFields(context, false, null, null);
			nuevoGastoProy.setNextSeqId();
			
			delegator.create(nuevoGastoProy);			
			
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("gastoProyId", nuevoGastoProy.getString("gastoProyId"));
			return result;
		}catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
	}
	
	public static Map<String, Object> agregaPresupuestoModProyecto(DispatchContext dctx,
			Map<String, Object> context){

		Delegator delegator = dctx.getDelegator();
        String proyectoId = (String) context.get("proyectoId");
        BigDecimal montoMes = (BigDecimal) context.get("monto");
        String detallePresupuestoId = (String) context.get("detallePresupuestoId");
        String etapa = (String) context.get("etapa");
        Timestamp fechaInicio = (Timestamp) context.get("fechaInicio");
        Timestamp fechaFin = (Timestamp) context.get("fechaFin");
		
		try {
			
			if(UtilValidate.isEmpty(detallePresupuestoId)&& UtilValidate.isEmpty(etapa)){
				return ServiceUtil.returnError("Debe ingresar la etapa ya sea nueva o existente");
			}else if(UtilValidate.isNotEmpty(detallePresupuestoId)&& UtilValidate.isNotEmpty(etapa)){
				return ServiceUtil.returnError("No se deben llenar ambos campos para la etapa");
			}
			
			if(UtilValidate.isNotEmpty(etapa)){
				if(UtilValidate.isEmpty(fechaInicio)||UtilValidate.isEmpty(fechaFin)){
					return ServiceUtil.returnError("Si va a generar una nueva etapa debe proporcionar las fechas para la misma");
				}
			}
			
			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
										  EntityCondition.makeCondition("proyectoId", EntityOperator.EQUALS, proyectoId));
			List<GenericValue> listPresupuesto = delegator.findByCondition("PresupuestoProyecto",condiciones, null, UtilMisc.toList("detallePresupuestoId DESC"));
			List<GenericValue> listModPresupuesto = delegator.findByCondition("ModificacionPresupuestoProyecto",condiciones, null, UtilMisc.toList("modDetallePresupuestoId DESC"));
			EntityCondition condicionesModificadoActual = EntityCondition.makeCondition(EntityOperator.AND,
					  EntityCondition.makeCondition("proyectoId", EntityOperator.EQUALS, proyectoId),
					  EntityCondition.makeCondition("mes", EntityOperator.NOT_EQUAL, null));
			List<GenericValue> listModPresupuestoActual = delegator.findByCondition("ModificacionPresupuestoProyecto",condicionesModificadoActual, null, UtilMisc.toList("modDetallePresupuestoId DESC"));
			
			long detalleId = 0;
			long modDetalleId = 0;
			
			if(!listModPresupuesto.isEmpty()){
				modDetalleId = Long.valueOf(EntityUtil.getFirst(listModPresupuesto).getString("modDetallePresupuestoId"));
			}
			
			if(!listPresupuesto.isEmpty()){
				detalleId = Long.valueOf(EntityUtil.getFirst(listPresupuesto).getString("detallePresupuestoId"));
			}
			
			if(!listModPresupuestoActual.isEmpty()){
				detalleId = Long.valueOf(EntityUtil.getFirst(listModPresupuestoActual).getString("detallePresupuestoId"));
			}
			
			
			if(UtilValidate.isEmpty(detallePresupuestoId)){
				detallePresupuestoId = UtilFormatOut.formatPaddedNumber((detalleId + 1), 4);
			}
			
			String modDetallePresupuestoId = UtilFormatOut.formatPaddedNumber((modDetalleId + 1), 4);
			GenericValue presupuestoMod = GenericValue.create(delegator.getModelEntity("ModificacionPresupuestoProyecto"));
			presupuestoMod.setAllFields(context, true, null, null);
			presupuestoMod.set("detallePresupuestoId", detallePresupuestoId);
			presupuestoMod.set("modDetallePresupuestoId", modDetallePresupuestoId);
			presupuestoMod.set("status", "NO_APLICADO");
			if(UtilValidate.isNotEmpty(etapa)){
				presupuestoMod.set("mes", etapa);
				presupuestoMod.set("fechaInicial", fechaInicio);
				presupuestoMod.set("fechaFinal", fechaFin);
			}
			
			//Se agrega a la lista la nueva requisicion para validar la suficiencia presupuestal
			listModPresupuesto.add(presupuestoMod);
			
			delegator.create(presupuestoMod);
			
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("proyectoId", proyectoId);
			
			return result;
			
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
		
	}
	
	public static Map<String, Object> devoProyectos(DispatchContext dctx,Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String proyectoId = (String) context.get("proyectoId");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		String eventoPago = (String) context.get("eventoPago");
		String paymentTypeId = (String) context.get("paymentTypeId");
		Map<String, Object> input = FastMap.newInstance();
	    Map<String, Object> output = FastMap.newInstance();
	    Map<String, Object> montoMap = FastMap.newInstance();
	    Map<String, Object> clavePresupuestalMap = FastMap.newInstance();
	    Map<String, Object> catalogoCargoContMap = FastMap.newInstance();
	    Map<String, Object> catalogoAbonoContMap = FastMap.newInstance();
	    Map<String, Object> context2 = FastMap.newInstance();
	    String poliza = null;
		

		try	{
			
			GenericValue proyecto = delegator.findByPrimaryKey("Proyecto", UtilMisc.toMap("proyectoId", proyectoId));
			
			String auxiliar = proyecto.getString("auxiliarCuentaCobrar");
			
			BigDecimal montoRestante = proyecto.getBigDecimal("montoRestante");
			
			String cuentaBancariaId = proyecto.getString("cuentaOrigen");
			
			List<String> orderByLinea = UtilMisc.toList("secuencia");
	    	EntityCondition condicionLinea = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, eventoPago));
			
			List<GenericValue> lineaList = delegator.findByCondition("LineaContable", condicionLinea, null, orderByLinea);
			Debug.logInfo("condicion: " + condicionLinea,MODULE);
			Debug.logInfo("lineaList: " + lineaList,MODULE);
			
				if(!lineaList.isEmpty())
				{	Iterator<GenericValue> listaIter = lineaList.iterator();	  
				
					while (listaIter.hasNext())
					{
						GenericValue item = listaIter.next();
						String descripcionLinea = item.getString("descripcion");
						descripcionLinea = descripcionLinea.replaceAll(" ", "");
						catalogoCargoContMap.put(descripcionLinea + "0",
								item.getString("catalogoCargo") != null
										&& item.getString("catalogoCargo").equalsIgnoreCase("BANCO") ? cuentaBancariaId
												: auxiliar);
						catalogoAbonoContMap.put(descripcionLinea + "0",
								item.getString("catalogoAbono") != null
										&& item.getString("catalogoAbono").equalsIgnoreCase("BANCO") ? cuentaBancariaId
												: auxiliar);
							montoMap.put(descripcionLinea+"0", montoRestante.toString());
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
            
            input.put("eventoContableId", eventoPago);
        	input.put("tipoClaveId", "EGRESO");
        	input.put("fechaRegistro", UtilDateTime.nowTimestamp());
        	input.put("fechaContable", fechaContable);
        	input.put("currency", proyecto.getString("tipoMoneda"));
        	input.put("usuario", userLogin.getString("userLoginId"));
        	input.put("organizationId", "1");
        	input.put("descripcion", "Reintegro - "+ proyecto.getString("nombreProyecto"));
        	input.put("roleTypeId", "BILL_FROM_VENDOR");
        	input.put("valorCampo", proyecto.getString("proyectoId"));
        	input.put("campo", "proyectoId");      
        	input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(eventoPago, delegator, context2, null, null, null, null));
        	
        	
        	//Genera registro en DepreciacionResumen
        	output = dispatcher.runSync("creaTransaccionMotor", input);
        	Debug.log("output: " + output);
        	if(ServiceUtil.isError(output)){
        		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
        	}
        	
        	GenericValue transaccion = (GenericValue) output.get("transaccion");
        	poliza = transaccion.getString("poliza");
        	proyecto.set("statusId", "FINALIZADO");
        	
        	proyecto.store();

			Map<String, Object> result = ServiceUtil.returnSuccess("Se ha realizado el pago con \u00e9xito y se ha creado la poliza " +poliza);		

			result.put("proyectoId", proyectoId);
			return result;
		}catch (GenericEntityException | GenericServiceException e) 
			{	return ServiceUtil.returnError("Error al crear el pago: " + e.getMessage());
			}
	}
	
	/**
	 * Metodo para recargar la pagina
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> cargarPaginaObjetoProyecto(DispatchContext dctx,
			Map<?, ?> context) {
		
		Map<String, Object> result = ServiceUtil.returnSuccess();
		
		return result;
	}
	
	/**
	 * Gets the path for uploaded files.
	 * @return a <code>String</code> value
	 */
	public static String getUploadPath(String proyectoId) {
		return "E:"+ File.separatorChar+"Repo"+ File.separatorChar+"Proyectos"+ File.separatorChar+"Proyecto_"+proyectoId +File.separatorChar;
	    //return System.getProperty("user.dir") + File.separatorChar + "runtime" + File.separatorChar + "data" + File.separatorChar + "Proyectos" + File.separatorChar;
	}
	public static String getUploadPathDirectorio() {
		return "E:"+ File.separatorChar+"Repo"+ File.separatorChar+"Proyectos" +File.separatorChar;
	    //return System.getProperty("user.dir") + File.separatorChar + "runtime" + File.separatorChar + "data" + File.separatorChar + "Proyectos" + File.separatorChar;
	}
	
	public static Map<String, Object> cargaDocumentosProyecto(DispatchContext dctx, Map<String, ? extends Object> context) throws InfrastructureException {
	    LocalDispatcher dispatcher = dctx.getDispatcher();
	    Delegator delegator = dctx.getDelegator();
	    String proyectoId = (String) context.get("proyectoId");
	    String fileFormat = (String) context.get("fileFormat");
	    String tipoDocumento = (String) context.get("tipoDocumento");
	    String fileName = (String) context.get("_uploadedFile_fileName");
	    String mimeTypeId = (String) context.get("_uploadedFile_contentType");
	    GenericValue userLogin = (GenericValue) context.get("userLogin");
	    ByteBuffer binData = (ByteBuffer) context.get("uploadedFile");
	    String error = "Ocurrio un error";
	    

        Debug.log("tamanio de archivo " +binData.capacity());
	
	    if (mimeTypeId != null && mimeTypeId.length() > 60) {
	        // XXX This is a fix to avoid problems where an OS gives us a mime type that is too long to fit in 60 chars
	        // (ex. MS .xlsx as application/vnd.openxmlformats-officedocument.spreadsheetml.sheet)
	        Debug.logWarning("Truncating mime type [" + mimeTypeId + "] to 60 characters.", MODULE);
	        mimeTypeId = mimeTypeId.substring(0, 60);
	    }
	    
	    if(binData.capacity()>16321472){
	    	return ServiceUtil.returnError("El archivo es demasiado grande para guardarlo");
	    }
	    
	    if(!mimeTypeId.equals("application/pdf")){
	    	return ServiceUtil.returnError("El tipo de archivo no es aceptado");
	    }
		
	
	    String fullFileName = getUploadPath(proyectoId)+ fileName;
	
	    // save the file to the system using the ofbiz service
	    Map<String, Object> input = UtilMisc.toMap("dataResourceId", null, "binData", context.get("uploadedFile"), "dataResourceTypeId", "LOCAL_FILE", "objectInfo", fullFileName);
	    try {
	    	
	    	GenericValue proyecto = delegator.findByPrimaryKey("Proyecto", UtilMisc.toMap("proyectoId", proyectoId));
	    	EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
					  EntityCondition.makeCondition("proyectoId", EntityOperator.EQUALS, proyectoId));
	    	EntityCondition condiciones2 = EntityCondition.makeCondition(EntityOperator.AND,
					  EntityCondition.makeCondition("proyectoId", EntityOperator.EQUALS, proyectoId),
					  EntityCondition.makeCondition("nombre", EntityOperator.EQUALS, fileName));
			List<GenericValue> listDocumentos = delegator.findByCondition("DocumentosProyecto",condiciones, null, UtilMisc.toList("detalleDocumentoId DESC"));
			List<GenericValue> listDocumentosValida = delegator.findByCondition("DocumentosProyecto",condiciones2, null, null);
			
			 if(listDocumentosValida.size()!=0){
					return ServiceUtil.returnError("Ya existe un archivo con el mismo nombre para este proyecto");
				}
			
			long detalleId = 0;
			
			if(!listDocumentos.isEmpty()){
			detalleId = Long.valueOf(EntityUtil.getFirst(listDocumentos).getString("detalleDocumentoId"));
			}
			
			String detalleDocumentoId = UtilFormatOut.formatPaddedNumber((detalleId + 1), 4);
	    	if(UtilValidate.isNotEmpty(proyecto)){
	    		GenericValue documentos = GenericValue.create(delegator.getModelEntity("DocumentosProyecto"));
	    		documentos.set("proyectoId", proyectoId);
	    		documentos.set("detalleDocumentoId", detalleDocumentoId);
	    		documentos.set("fileFormat", mimeTypeId);
	    		documentos.set("nombre", fileName);
	    		documentos.set("tipoDocumento", tipoDocumento);
	    		documentos.set("direccion", fullFileName);
	    		
	    		 Map<String, Object> results = dispatcher.runSync("createAnonFile", input);
	 	        if (ServiceUtil.isError(results)) {
	 	            return results;
	 	        }
	    		
				delegator.create(documentos);
				
	    	}else{
	    		return ServiceUtil.returnError("El proyecto no existe");
	    	}
	    } catch (GenericServiceException | GenericEntityException e) {
	        return ServiceUtil.returnError(error+" "+e);
	    }
	
	    return ServiceUtil.returnSuccess("El documento se ha subido con �xito");
	}
	
	

	/**
	 * Metodo para registrar las previsiones de ingresos en proyectos
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> pagoProvisionProyectos(DispatchContext dctx,Map<?, ?> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String proyectoId = (String) context.get("proyectoId");
		String etapa = (String)context.get("detallePresupuestoId");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		String eventoPagoPrev = (String) context.get("eventoPagoPrev");
		String paymentTypeId = (String) context.get("paymentTypeId");
		String cuentaBancariaId = (String) context.get("cuentaOrigen");
		String bancoId = (String)context.get("bancoIdOrigen");
		BigDecimal montoTotal = (BigDecimal) context.get("montoTotal");
		Map<String, Object> input = FastMap.newInstance();
	    Map<String, Object> output = FastMap.newInstance();
	    Map<String, Object> montoMap = FastMap.newInstance();
	    Map<String, Object> clavePresupuestalMap = FastMap.newInstance();
	    Map<String, Object> catalogoCargoContMap = FastMap.newInstance();
	    Map<String, Object> catalogoAbonoContMap = FastMap.newInstance();
	    Map<String, Object> context2 = FastMap.newInstance();
	    String poliza = null;
	    
		

		try	{
			
			GenericValue proyecto = delegator.findByPrimaryKey("Proyecto", UtilMisc.toMap("proyectoId", proyectoId));
			
			String auxiliar = proyecto.getString("auxiliarProyecto");
			String nombreProyecto = proyecto.getString("nombreProyecto");
			BigDecimal montoRestante = proyecto.getBigDecimal("montoRestante");
			proyecto.set("montoRestante", montoRestante.add(montoTotal));
			List<String> orderByLinea = UtilMisc.toList("secuencia");
	    	EntityCondition condicionLinea = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, eventoPagoPrev));
			
			List<GenericValue> lineaList = delegator.findByCondition("LineaContable", condicionLinea, null, orderByLinea);
			Debug.logInfo("condicion: " + condicionLinea,MODULE);
			Debug.logInfo("lineaList: " + lineaList,MODULE);
			int i = lineaList.size();
			if(i>1){
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
						montoMap.put(descripcionLinea+"0", montoTotal.toString());
						
						auxiliar = proyecto.getString("auxiliar");
						
						i--;
					}
				}
			}else{	Iterator<GenericValue> listaIter = lineaList.iterator();	        
			   
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
											:proyecto.getString("auxiliarCancelaPasivo") );
		        	montoMap.put(descripcionLinea+"0", montoTotal.toString());
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
            
            input.put("eventoContableId", eventoPagoPrev);
        	input.put("tipoClaveId", "EGRESO");
        	input.put("fechaRegistro", UtilDateTime.nowTimestamp());
        	input.put("fechaContable", fechaContable);
        	input.put("currency", proyecto.getString("tipoMoneda"));
        	input.put("usuario", userLogin.getString("userLoginId"));
        	input.put("organizationId", "1");
        	input.put("descripcion", "PROVISION - "+ proyecto.getString("nombreProyecto"));
        	input.put("roleTypeId", "BILL_FROM_VENDOR");
        	input.put("valorCampo", proyecto.getString("proyectoId"));
        	input.put("campo", "proyectoId");      
        	input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(eventoPagoPrev, delegator, context2, null, null, null, null));
        	
        	
        	//Genera registro en DepreciacionResumen
        	output = dispatcher.runSync("creaTransaccionMotor", input);
        	Debug.log("output: " + output);
        	if(ServiceUtil.isError(output)){
        		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
        	}
        	
        	GenericValue transaccion = (GenericValue) output.get("transaccion");
        	poliza = transaccion.getString("poliza");
        	
        	EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
					  EntityCondition.makeCondition("proyectoId", EntityOperator.EQUALS, proyectoId));
			List<GenericValue> listProvision = delegator.findByCondition("ProvisionesProyecto",condiciones, null, UtilMisc.toList("detalleProvisionId DESC"));
			
			long detalleId = 0;
			
			if(!listProvision.isEmpty()){
			detalleId = Long.valueOf(EntityUtil.getFirst(listProvision).getString("detalleProvisionId"));
			}
			
			String detalleProvisionId = UtilFormatOut.formatPaddedNumber((detalleId + 1), 4);
			
			GenericValue provision = GenericValue.create(delegator.getModelEntity("ProvisionesProyecto"));
			provision.setAllFields(context, true, null, null);
			provision.set("acctgTransIdProvision", transaccion.getString("acctgTransId"));
			provision.set("bancoId", bancoId);
			provision.set("cuenta", cuentaBancariaId);
			provision.set("monto", montoTotal);
			provision.set("eventoProvision", eventoPagoPrev);
			provision.set("detalleProvisionId", detalleProvisionId);
			
			//Se agrega a la lista la nueva requisicion para validar la suficiencia presupuestal
			listProvision.add(provision);
			
			
			delegator.create(provision);

        	proyecto.store();

			Map<String, Object> result = ServiceUtil.returnSuccess("Se ha realizado el pago con \u00e9xito y se ha creado la poliza " +poliza);		

			result.put("proyectoId", proyectoId);
			return result;
		}catch (GenericEntityException | GenericServiceException e) 
			{	return ServiceUtil.returnError("Error al crear el pago: " + e.getMessage());
			}
	}
	
	/**
	 * Metodo para recargar la pagina
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> cargarPaginaProyectoDocumentos(DispatchContext dctx,
			Map<?, ?> context) {
		
		Delegator delegator = dctx.getDelegator();
		
		String proyectoId = (String) context.get("proyectoId");		

		GenericValue proyecto = GenericValue.create(delegator.getModelEntity("Proyecto"));
		if(proyectoId != null && !proyectoId.isEmpty())
			proyecto.set("proyectoId", proyectoId);
		
		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("proyectoId", proyecto.getString("proyectoId"));
		return result;
	}

}
