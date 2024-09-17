package org.opentaps.proyectos.principal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JFileChooser;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.opentaps.base.entities.CuentasBanco;
import org.opentaps.base.entities.ParticipantesProyecto;
import org.opentaps.base.entities.TipoAdjudicacionDoc;
import org.opentaps.common.event.PaginationEvents;
import org.opentaps.common.util.UtilCommon;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.json.JSONObject;

public class AjaxEvents {
	
	public static final String module = AjaxEvents.class.getName();
    
	public static String actualizaParticipantes(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
		Map<String, Object> resultado = new HashMap<String, Object>();
		String ciclo = UtilCommon.getCicloId(request);
		String clavePresupuestal = "";
		try
		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
			Map<String,Object> context = FastMap.newInstance();
		
			String proyectoId = (String) request.getParameter("proyectoId");
    		String detalleParticipanteId = (String) request.getParameter("detalleParticipanteId");
    		String nombre = (String) request.getParameter("nombre");
    		String apellidoPaterno = (String)request.getParameter("apellidoPaterno");
    		String puesto = (String)request.getParameter("puesto");
    		String organizationPartyId = (String)request.getParameter("organizationPartyId");
            	           
    		//Actualiza el monto y la cantidad en el detalle de la solicitud
    		GenericValue participanteProyectoGeneric = delegator.findByPrimaryKey("ParticipantesProyecto", UtilMisc.toMap("proyectoId", proyectoId, "detalleParticipanteId", detalleParticipanteId));											
    		participanteProyectoGeneric.set("nombre", nombre);
    		participanteProyectoGeneric.set("apellidos", apellidoPaterno);
    		participanteProyectoGeneric.set("puesto", puesto);
			
			context.putAll(participanteProyectoGeneric.getAllFields());
			
			delegator.store(participanteProyectoGeneric);
			resultado.put("mensaje", "SUCCESS");
			return doJSONResponse(response, resultado);
			
			}
		catch(GenericEntityException e)
		{	
			resultado.put("mensaje", "ERROR:"+e.getMessage());
			return doJSONResponse(response, resultado);
		}
	}
	
	/** Return the objects which related with product in receive inventory form.
     * @throws GenericEntityException
     */
    public static String obtieneCuentasBancarias(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
		try
		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
			String bancoId = (String) request.getParameter("bancoId");
			String organizationPartyId = (String) request.getParameter("organizationPartyId");
			
			List<String> orderBy = org.ofbiz.base.util.UtilMisc.toList("numeroCuenta");			
			
			EntityCondition condicionBan;	        
			condicionBan = EntityCondition.makeCondition(EntityOperator.AND,
                EntityCondition.makeCondition(CuentasBanco.Fields.bancoId.name(), EntityOperator.EQUALS, bancoId),
                EntityCondition.makeCondition(CuentasBanco.Fields.partyId.name(), EntityOperator.EQUALS, organizationPartyId),
                EntityCondition.makeCondition(CuentasBanco.Fields.habilitada.name(), EntityOperator.EQUALS, "S"));
			
			
			List<GenericValue> cuentas = delegator.findByCondition("CuentasBanco", condicionBan , UtilMisc.toList("numeroCuenta", "nombreCuenta", "cuentaBancariaId", "bancoId"), orderBy);
			Map<String, Object> resultado = new HashMap<String, Object>();
			
			
			if (!cuentas.isEmpty()) 
			{	Iterator<GenericValue> cuentasIter = cuentas.iterator();
				while (cuentasIter.hasNext()) 
				{	GenericValue cuentasListaTrans = cuentasIter.next();
					resultado.put(cuentasListaTrans.getString("cuentaBancariaId"), cuentasListaTrans.getString("numeroCuenta")+" - "+cuentasListaTrans.getString("nombreCuenta"));										
				}
				return doJSONResponse(response, resultado);
			}
			else			
			{	resultado.put("cuentaBancariaId", "ERROR");
				return doJSONResponse(response, resultado);
			}
		}
		catch(GeneralException e)
		{	return org.opentaps.common.event.AjaxEvents.doJSONResponse(response, FastList.newInstance());
		}
	}
	
	public static String eliminaParticipantes(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
		Map<String, Object> resultado = new HashMap<String, Object>();
		try
		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
		
			String proyectoId = (String) request.getParameter("proyectoId");
    		String detalleParticipanteId = (String) request.getParameter("detalleParticipanteId");
    		
    		//Actualiza el monto y la cantidad en el detalle de la solicitud
    		GenericValue participantesProyectoGeneric = delegator.findByPrimaryKey("ParticipantesProyecto", UtilMisc.toMap("proyectoId", proyectoId, "detalleParticipanteId", detalleParticipanteId));
    		participantesProyectoGeneric.set("activo", "N");
    		participantesProyectoGeneric.store();			
			resultado.put("mensaje", "SUCCESS");
			return doJSONResponse(response, resultado);
		}
		catch(GeneralException e)
		{	resultado.put("mensaje", "ERROR");
			return doJSONResponse(response, resultado);
		}
	}
	
	/** Return the objects which related with project
     * @throws GenericEntityException
     */
    public static String obtieneParticipantes(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
		try
		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
			String proyectoId = (String) request.getParameter("proyectoId");
			
			List<String> orderBy = org.ofbiz.base.util.UtilMisc.toList("detalleParticipanteId");
			
			EntityCondition condicionDocs;	        
			condicionDocs = EntityCondition.makeCondition(EntityOperator.AND,
                EntityCondition.makeCondition(ParticipantesProyecto.Fields.proyectoId.name(), EntityOperator.EQUALS, proyectoId),
                EntityCondition.makeCondition(ParticipantesProyecto.Fields.activo.name(), EntityOperator.EQUALS, "Y"));    			
			
			List<GenericValue> participantes = delegator.findByCondition("ParticipantesProyecto", condicionDocs , null, orderBy);
			Map<String, Object> resultado = new HashMap<String, Object>();    			
			
			if (!participantes.isEmpty()) 
			{	Iterator<GenericValue> participantesIter = participantes.iterator();
				while (participantesIter.hasNext()) 
				{	GenericValue documentosListaTrans = participantesIter.next();
					resultado.put(documentosListaTrans.getString("detalleParticipanteId"), documentosListaTrans.getString("nombre")+"  "+documentosListaTrans.getString("apellidos"));
				}
				return doJSONResponse(response, resultado);
			}
			else			
			{	resultado.put("detalleParticipanteId", "ERROR");
				return doJSONResponse(response, resultado);
			}
			
			
		}
		catch(GeneralException e)
		{	return org.opentaps.common.event.AjaxEvents.doJSONResponse(response, FastList.newInstance());
		}
	}
    
    /** Return the objects which related with project
     * @throws GenericEntityException
     */
    public static String obtienePresupuesto(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
		try
		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
			String proyectoId = (String) request.getParameter("proyectoId");
			
			List<String> orderBy = org.ofbiz.base.util.UtilMisc.toList("detallePresupuestoId");
			
			EntityCondition condicionDocs;	        
			condicionDocs = EntityCondition.makeCondition(EntityOperator.AND,
                EntityCondition.makeCondition(ParticipantesProyecto.Fields.proyectoId.name(), EntityOperator.EQUALS, proyectoId));    			
			
			List<GenericValue> presupuesto = delegator.findByCondition("PresupuestoProyecto", condicionDocs , null, orderBy);
			Map<String, Object> resultado = new HashMap<String, Object>();    			
						
			if (!presupuesto.isEmpty()) 
			{	Iterator<GenericValue> presupuestoIter = presupuesto.iterator();
				while (presupuestoIter.hasNext()) 
				{	GenericValue documentosListaTrans = presupuestoIter.next();
					resultado.put(documentosListaTrans.getString("detallePresupuestoId"), documentosListaTrans.getString("mes"));
				}
				return doJSONResponse(response, resultado);
			}
			else			
			{	resultado.put("detallePresupuestoId", "ERROR");
				return doJSONResponse(response, resultado);
			}
			
		}
		catch(GeneralException e)
		{	return org.opentaps.common.event.AjaxEvents.doJSONResponse(response, FastList.newInstance());
		}
	}
    
    public static String eliminaComprobanteViaticoProy(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
		Map<String, Object> resultado = new HashMap<String, Object>();
		try
		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
		
			String conceptoViaticoMontoProyId = (String) request.getParameter("conceptoViaticoMontoProyId");
			
    		GenericValue detalleViaticoGeneric = delegator.findByPrimaryKey("ConceptoViaticoMontoProy", UtilMisc.toMap("conceptoViaticoMontoProyId", conceptoViaticoMontoProyId));
    		
    		
    		//Disminuimos el monto Diario o monto Transporte Comprobado.
			GenericValue viatico = delegator.findByPrimaryKey("ViaticosProyecto", UtilMisc.toMap("viaticoProyectoId", detalleViaticoGeneric.getString("viaticoProyectoId")));
			GenericValue concepto = delegator.findByPrimaryKey("ConceptoViatico", UtilMisc.toMap("conceptoViaticoId", detalleViaticoGeneric.getString("conceptoViaticoId")));
			String campo = concepto.getString("diarioTransporteFlag")
					.equalsIgnoreCase("D") ? "montoDiarioComprobado"
					: concepto.getString("diarioTransporteFlag")
							.equalsIgnoreCase("T") ? "montoTransporteComprobado"
							: "montoTrabCampoComprobado";
			BigDecimal montoTotal = viatico.getBigDecimal(campo);
			viatico.set(campo, montoTotal.subtract(detalleViaticoGeneric.getBigDecimal("monto")));
			delegator.store(viatico);
			
			detalleViaticoGeneric.remove();
			
    		resultado.put("mensaje", "SUCCESS");
			return doJSONResponse(response, resultado);
			
		}
		catch(GeneralException e)
		{	resultado.put("mensaje", "ERROR");
			return doJSONResponse(response, resultado);
		}
	}
    
    public static String eliminaFacturasProy(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> resultado = new HashMap<String, Object>();
		try{	
			Delegator delegator = (Delegator) request.getAttribute("delegator");
			String gastoProyectoId = (String) request.getParameter("gastoProyectoId");
			String detalleGId = (String) request.getParameter("detalleGId");
    		GenericValue detalleGasto = delegator.findByPrimaryKey("DetalleGastoProyecto", UtilMisc.toMap("gastoProyectoId", gastoProyectoId, "detalleGId", detalleGId));
    		detalleGasto.remove();
    		resultado.put("mensaje", "SUCCESS");
			return doJSONResponse(response, resultado);
		}
		catch(GeneralException | NullPointerException e)
		{	
			e.printStackTrace();
			resultado.put("mensaje", "ERROR");
			return doJSONResponse(response, resultado);
		}
	}
    
    public static String eliminaProductos(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> resultado = new HashMap<String, Object>();
		try{	
			Delegator delegator = (Delegator) request.getAttribute("delegator");
			String adqProyectoId = (String) request.getParameter("adqProyectoId");
			String detalleProductoId = (String) request.getParameter("detalleProductoId");
    		GenericValue detalleGasto = delegator.findByPrimaryKey("ProductosAdqProyecto", UtilMisc.toMap("adqProyectoId", adqProyectoId, "detalleProductoId", detalleProductoId));
    		GenericValue adquisicionProy = delegator.findByPrimaryKey("AdquisicionesProyecto", UtilMisc.toMap("adqProyectoId", adqProyectoId));
    		BigDecimal monto = detalleGasto.getBigDecimal("monto");
    		BigDecimal cantidad = new BigDecimal(detalleGasto.getLong("cantidad"));
    		BigDecimal montoAdq = adquisicionProy.getBigDecimal("montoTotal").subtract(monto.multiply(cantidad));
    		adquisicionProy.set("montoTotal", montoAdq);
    		adquisicionProy.store();
    		detalleGasto.remove();
    		resultado.put("mensaje", "SUCCESS");
			return doJSONResponse(response, resultado);
		}
		catch(GeneralException | NullPointerException e)
		{	
			e.printStackTrace();
			resultado.put("mensaje", "ERROR");
			return doJSONResponse(response, resultado);
		}
	}
    
    public static String eliminaObjetoGastoProyecto(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
		Map<String, Object> resultado = new HashMap<String, Object>();
		try
		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
		
    		String gastoProyId = (String) request.getParameter("gastoProyId");
    		
    		List<GenericValue> viaticos = delegator.findByCondition("ConceptoViaticoMontoProy", EntityCondition.makeCondition("gastoProyId", EntityOperator.EQUALS, gastoProyId), null, null);
    		List<GenericValue> gastos = delegator.findByCondition("DetalleGastoProyecto", EntityCondition.makeCondition("gastoProyId", EntityOperator.EQUALS, gastoProyId), null, null);
    		List<GenericValue> servPersonales = delegator.findByCondition("ServiciosPersonalesProy", EntityCondition.makeCondition("gastoProyId", EntityOperator.EQUALS, gastoProyId), null, null);
    		List<GenericValue> adquisiciones = delegator.findByCondition("AdquisicionesProyecto", EntityCondition.makeCondition("gastoProyId", EntityOperator.EQUALS, gastoProyId), null, null);
    		
    		if(UtilValidate.isNotEmpty(viaticos)||UtilValidate.isNotEmpty(gastos)||UtilValidate.isNotEmpty(servPersonales)||UtilValidate.isNotEmpty(adquisiciones)){
    			resultado.put("mensaje", "ERROR2");
    			return doJSONResponse(response, resultado);
    		}else{
        		GenericValue objetosGastoProyectoGeneric = delegator.findByPrimaryKey("ObjetoGastoProyecto", UtilMisc.toMap("gastoProyId", gastoProyId));
        		objetosGastoProyectoGeneric.remove();			
    			resultado.put("mensaje", "SUCCESS");
    			return doJSONResponse(response, resultado);
    		}
    		
    		
		}
		catch(GeneralException e)
		{	resultado.put("mensaje", "ERROR");
			return doJSONResponse(response, resultado);
		}
	}
    
    public static String modificarPresupuesto(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
		Map<String, Object> resultado = new HashMap<String, Object>();
		try
		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
			Map<String,Object> context = FastMap.newInstance();
			final Locale locale = request.getLocale();
			String dateFormat = UtilDateTime.getDateFormat(locale);
			TimeZone timeZone = UtilHttp.getTimeZone(request);
			String proyectoId = (String) request.getParameter("proyectoId");
    		String detallePresupuestoId = (String) request.getParameter("detallePresupuestoId");
    		String fechaInicioString = (String) request.getParameter("fechaInicio");
    		String fechaFinString = (String)request.getParameter("fechaFin");
    		
    		Timestamp fechaInicio = null;
    		Timestamp fechaFin = null;
    		if(fechaInicioString != null && !fechaInicioString.equals(""))
    			fechaInicio = UtilDateTime.stringToTimeStamp(fechaInicioString, dateFormat, timeZone, locale);
    		if(fechaFinString != null && !fechaFinString.equals(""))
    			fechaFin = UtilDateTime.stringToTimeStamp(fechaFinString, dateFormat, timeZone, locale);
            	           
    		//Actualiza el monto y la cantidad en el detalle de la solicitud
    		GenericValue presupuestoProyectoGeneric = delegator.findByPrimaryKey("PresupuestoProyecto", UtilMisc.toMap("proyectoId", proyectoId, "detallePresupuestoId", detallePresupuestoId));											
    		presupuestoProyectoGeneric.set("fechaInicial", fechaInicio);
    		presupuestoProyectoGeneric.set("fechaFinal", fechaFin);
			
			context.putAll(presupuestoProyectoGeneric.getAllFields());
			
			delegator.store(presupuestoProyectoGeneric);
			resultado.put("mensaje", "SUCCESS");
			return doJSONResponse(response, resultado);
			
			}
		catch(GenericEntityException e)
		{	
			resultado.put("mensaje", "ERROR:"+e.getMessage());
			return doJSONResponse(response, resultado);
		} catch (ParseException e) {
			resultado.put("mensaje", "ERROR:"+e.getMessage());
			return doJSONResponse(response, resultado);
		}
	}
    
    public static String eliminaModificacionesProyecto(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
		Map<String, Object> resultado = new HashMap<String, Object>();
		try
		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
		
			String proyectoId = (String) request.getParameter("proyectoId");
    		String modDetallePresupuestoId = (String) request.getParameter("modDetallePresupuestoId");
    		
    		//Actualiza el monto y la cantidad en el detalle de la solicitud
    		GenericValue modificacionPresupuesto = delegator.findByPrimaryKey("ModificacionPresupuestoProyecto", UtilMisc.toMap("proyectoId", proyectoId, "modDetallePresupuestoId", modDetallePresupuestoId));
    		if(modificacionPresupuesto.getString("status").equals("APLICADO")){
    			resultado.put("mensaje", "ERROR2");
    		}else{
    			modificacionPresupuesto.remove();
    			resultado.put("mensaje", "SUCCESS");
    		}
			return doJSONResponse(response, resultado);
		}
		catch(GeneralException e)
		{	resultado.put("mensaje", "ERROR");
			return doJSONResponse(response, resultado);
		}
	}
    
    public static String actualizaEtapasPresupuesto(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
		Map<String, Object> resultado = new HashMap<String, Object>();
		String ciclo = UtilCommon.getCicloId(request);
		String clavePresupuestal = "";
		try
		{	Delegator delegator = (Delegator) request.getAttribute("delegator");
			Map<String,Object> context = FastMap.newInstance();
			final Locale locale = request.getLocale();
			String dateFormat = UtilDateTime.getDateFormat(locale);
			TimeZone timeZone = UtilHttp.getTimeZone(request);
			String proyectoId = (String) request.getParameter("proyectoId");
    		String detallePresupuestoId = (String) request.getParameter("detallePresupuestoId");
    		String mes = (String) request.getParameter("mes");
    		String fechaInicioString = (String)request.getParameter("fechaInicio");
    		String fechaFinString = (String)request.getParameter("fechaFin");
    		String monto = (String)request.getParameter("monto");
    		Long montoLong = Long.valueOf(monto);
    		BigDecimal montoBD = BigDecimal.valueOf(montoLong);
            BigDecimal montoFinal = BigDecimal.ZERO;
            Timestamp fechaInicio = null;
    		Timestamp fechaFin = null;
    		if(fechaInicioString != null && !fechaInicioString.equals(""))
    			fechaInicio = UtilDateTime.stringToTimeStamp(fechaInicioString, dateFormat, timeZone, locale);
    		if(fechaFinString != null && !fechaFinString.equals(""))
    			fechaFin = UtilDateTime.stringToTimeStamp(fechaFinString, dateFormat, timeZone, locale);
            
    		
    		//Actualiza el monto y la cantidad en el detalle de la solicitud
    		GenericValue presupuestoProyectoGeneric = delegator.findByPrimaryKey("PresupuestoProyecto", UtilMisc.toMap("proyectoId", proyectoId, "detallePresupuestoId", detallePresupuestoId));
    		GenericValue proyecto = delegator.findByPrimaryKey("Proyecto", UtilMisc.toMap("proyectoId", proyectoId));
    		BigDecimal montoProyecto = proyecto.getBigDecimal("monto");
    		BigDecimal montoRestanteProyecto = proyecto.getBigDecimal("montoRestante");
    		
    		presupuestoProyectoGeneric.set("mes", mes);
    		presupuestoProyectoGeneric.set("fechaInicial", fechaInicio);
    		presupuestoProyectoGeneric.set("fechaFinal", fechaFin);
    		BigDecimal montoAnterior = presupuestoProyectoGeneric.getBigDecimal("monto");
    		if(montoAnterior.compareTo(montoBD)==-1){
    			montoFinal = montoBD.subtract(montoAnterior);
    			proyecto.set("monto", montoProyecto.add(montoFinal));
    			proyecto.set("montoRestante", montoRestanteProyecto.add(montoFinal));
    			presupuestoProyectoGeneric.set("monto", montoBD);
    		}else if(montoAnterior.compareTo(montoBD)==1){
    			montoFinal = montoAnterior.subtract(montoBD);
    			proyecto.set("monto", montoProyecto.subtract(montoFinal));
    			proyecto.set("montoRestante", montoRestanteProyecto.subtract(montoFinal));
    			presupuestoProyectoGeneric.set("monto", montoBD);
    		}
    		
			context.putAll(presupuestoProyectoGeneric.getAllFields());
			
			delegator.store(presupuestoProyectoGeneric);
			delegator.store(proyecto);
			resultado.put("mensaje", "SUCCESS");
			return doJSONResponse(response, resultado);
			
			}
		catch(GenericEntityException e)
		{	
			resultado.put("mensaje", "ERROR:"+e.getMessage());
			return doJSONResponse(response, resultado);
		} catch (ParseException e) {
			resultado.put("mensaje", "ERROR:"+e.getMessage());
			return doJSONResponse(response, resultado);
		
		}
	}
    
    public static String eliminaEtapaPresupuesto(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
		Map<String, Object> resultado = new HashMap<String, Object>();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		
		String proyectoId = (String) request.getParameter("proyectoId");
		String detallePresupuestoId = (String) request.getParameter("detallePresupuestoId");
		try
		{	
    		
    		//Actualiza el monto y la cantidad en el detalle de la solicitud
    		GenericValue presupuestoProyectoGeneric = delegator.findByPrimaryKey("PresupuestoProyecto", UtilMisc.toMap("proyectoId", proyectoId, "detallePresupuestoId", detallePresupuestoId));
    		GenericValue proyecto = delegator.findByPrimaryKey("Proyecto", UtilMisc.toMap("proyectoId", proyectoId));
    		
    		BigDecimal montoResta = presupuestoProyectoGeneric.getBigDecimal("monto");
    		BigDecimal montoProyecto = proyecto.getBigDecimal("monto");
    		BigDecimal montoRestanteProyecto = proyecto.getBigDecimal("montoRestante");
    		proyecto.set("monto", montoProyecto.subtract(montoResta));
    		proyecto.set("montoRestante", montoRestanteProyecto.subtract(montoResta));
    		
    		delegator.store(proyecto);
    		presupuestoProyectoGeneric.remove();;			
			resultado.put("mensaje", "SUCCESS");
			return doJSONResponse(response, resultado);
		}
		catch(GeneralException e)
		{	resultado.put("mensaje", "ERROR");
			return doJSONResponse(response, resultado);
		}
	}
	
    public static String descargaDocumento(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
    File file = null;
    ServletOutputStream out = null;
    FileInputStream fileToDownload = null;
    String MODULE = AjaxEvents.class.getName();

    try {
    	Delegator delegator = (Delegator) request.getAttribute("delegator");
    	
    	String proyectoId = (String) request.getParameter("proyectoId");
		String detalleDocumentoId = (String) request.getParameter("detalleDocumentoId");
		
		//Actualiza el monto y la cantidad en el detalle de la solicitud
		GenericValue documentosProyectoGeneric = delegator.findByPrimaryKey("DocumentosProyecto", UtilMisc.toMap("proyectoId", proyectoId, "detalleDocumentoId", detalleDocumentoId));
        out = response.getOutputStream();

        file = new File(documentosProyectoGeneric.getString("direccion"));
        fileToDownload = new FileInputStream(file);
        /*if(documentosProyectoGeneric.getString("fileFormat").equals("Excel")){
        	response.setContentType("application/vnd.ms-excel");
        }else if(documentosProyectoGeneric.getString("fileFormat").equals("Documento de texto")){
        	response.setContentType("application/msword");
        }else if(documentosProyectoGeneric.getString("fileFormat").equals("PowerPoint")){
        	response.setContentType("application/vnd.ms-powerpoint");
        }else{
        	response.setContentType("application/pdf");
        }*/
        
        response.setContentType(documentosProyectoGeneric.getString("fileFormat"));
        response.setHeader("Content-Disposition", "attachment; filename=" + "\""+documentosProyectoGeneric.getString("nombre")+"\"");
        response.setContentLength(fileToDownload.available());

        int c;
        while ((c = fileToDownload.read()) != -1) {
            out.write(c);
        }

        out.flush();
    } catch (FileNotFoundException e) {
        Debug.logError("Failed to open the file: ", MODULE);
        return "error";
    } catch (IOException ioe) {
        Debug.logError("IOException is thrown while trying to download the Excel file: " + ioe.getMessage(), MODULE);
        return "error";
    } catch (GenericEntityException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} finally {
        try {
            out.close();
            if (fileToDownload != null) {
                fileToDownload.close();
                // Delete the file under /runtime/output/ this is optional
            }
        } catch (IOException ioe) {
            Debug.logError("IOException is thrown while trying to download the Excel file: " + ioe.getMessage(), MODULE);
            return "error";
        }
    }

    return "success";
}
    
    public static String eliminaDocumento(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
    	Map<String, Object> resultado = new HashMap<String, Object>();
        File file = null;

        try {
        	Delegator delegator = (Delegator) request.getAttribute("delegator");
        	
        	String proyectoId = (String) request.getParameter("proyectoId");
    		String detalleDocumentoId = (String) request.getParameter("detalleDocumentoId");
    		
    		//Actualiza el monto y la cantidad en el detalle de la solicitud
    		GenericValue documentosProyectoGeneric = delegator.findByPrimaryKey("DocumentosProyecto", UtilMisc.toMap("proyectoId", proyectoId, "detalleDocumentoId", detalleDocumentoId));
    		file = new File(documentosProyectoGeneric.getString("direccion"));
    		file.delete();
    		documentosProyectoGeneric.remove();
    		
        } catch (GenericEntityException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	 
        }

        resultado.put("mensaje", "SUCCESS");
		return doJSONResponse(response, resultado);
    }
	
	private static String doJSONResponse(HttpServletResponse response, @SuppressWarnings("rawtypes") Map map) {
        return org.opentaps.common.event.AjaxEvents.doJSONResponse(response, JSONObject.fromObject(map));
    }

}
