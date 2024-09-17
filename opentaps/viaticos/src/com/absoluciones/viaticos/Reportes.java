package com.absoluciones.viaticos;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilNumber;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.common.util.UtilMessage;
import org.opentaps.warehouse.inventory.UtilNumberToLetter;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

public class Reportes {
	
	public static String solicitudViaticos(HttpServletRequest request, HttpServletResponse response) 
	{
	
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		Locale locale = UtilHttp.getLocale(request);
		
		String viaticoId = UtilCommon.getParameter(request, "viaticoId");
		
	try {
		EntityCondition conditions = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("viaticoId", viaticoId),
				EntityCondition.makeCondition("tipoWorkFlowId",EntityOperator.LIKE, "VIATICOS%"));
		List<GenericValue> solicitudes =  delegator.findByCondition("RepSolicitudViaticos", conditions, null, null);
		GenericValue logoUrl = delegator.findByPrimaryKey("PartyGroup", UtilMisc.toMap("partyId", EntityUtil.getFirst(solicitudes).getString("organizationPartyId")));
		
		List<String> orderBy = new ArrayList<String>();
		
		orderBy.add("nivelAutor");
		
		List<String> selects = new ArrayList<String>();
		
		selects.add("firstName");
		selects.add("lastName");
		selects.add("firmaId");
		
		EntityCondition conditionsAutorizadores = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("workFlowId",EntityOperator.EQUALS, EntityUtil.getFirst(solicitudes).getString("workFlowId")),
				EntityCondition.makeCondition("statusId",EntityOperator.EQUALS, "AUTORIZADA"));
		List<GenericValue> autorizadores = delegator.findByCondition("BuscarUltimoAutorizador",conditionsAutorizadores , selects, orderBy);
		
		
		
		Map<String, Object> jrParameters = FastMap.newInstance();
		List<Map<String, Object>> plainList = FastList.<Map<String, Object>>newInstance();
		
		
		jrParameters.put("VIATICO_ID", viaticoId);
		jrParameters.put("ORGANIZACION", logoUrl.getString("groupNameLocal"));
		jrParameters.put("logoUrl", logoUrl.getString("logoImageUrl"));
		jrParameters.put("logoUrl2", logoUrl.getString("logoImageUrl2"));
		jrParameters.put("GROUP_NAME", EntityUtil.getFirst(solicitudes).getString("groupName"));
		
		if(UtilValidate.isNotEmpty(EntityUtil.getFirst(solicitudes).getString("description"))){
			jrParameters.put("PROGRAMA", EntityUtil.getFirst(solicitudes).getString("description"));
		}else{
			jrParameters.put("PROGRAMA", EntityUtil.getFirst(solicitudes).getString("nombrePrograma"));
		}
		
		jrParameters.put("NOMBRE_COMPLETO", EntityUtil.getFirst(solicitudes).getString("firstName")+' '+EntityUtil.getFirst(solicitudes).getString("lastName"));
		jrParameters.put("PERSONA_SOLICITANTE_ID", EntityUtil.getFirst(solicitudes).getString("partyId"));
		jrParameters.put("OCCUPATION", EntityUtil.getFirst(solicitudes).getString("occupation"));
		jrParameters.put("GEO_NAME", EntityUtil.getFirst(solicitudes).getString("geoName"));
		jrParameters.put("FECHA_AUTORIZACION", EntityUtil.getFirst(solicitudes).getTimestamp("fechaAutorizacion"));
		jrParameters.put("JUSTIFICACION", EntityUtil.getFirst(solicitudes).getString("justificacion"));
		jrParameters.put("FECHA_INICIAL", EntityUtil.getFirst(solicitudes).getTimestamp("fechaInicial"));
		jrParameters.put("FECHA_FINAL", EntityUtil.getFirst(solicitudes).getTimestamp("fechaFinal"));
		jrParameters.put("DIAS",UtilDateTime.getIntervalInDays(EntityUtil.getFirst(solicitudes).getTimestamp("fechaInicial"), 
																EntityUtil.getFirst(solicitudes).getTimestamp("fechaFinal")));
		
		jrParameters.put("TOTAL", UtilNumber.getBigDecimal(EntityUtil.getFirst(solicitudes).getBigDecimal("montoDiario")).
								add(UtilNumber.getBigDecimal(EntityUtil.getFirst(solicitudes).getBigDecimal("montoTransporte"))).
										add(UtilNumber.getBigDecimal(EntityUtil.getFirst(solicitudes).getBigDecimal("montoTrabCampo"))));
		jrParameters.put("TEXTO_MONTO_TOTAL",UtilNumberToLetter.convertNumberToLetter( UtilNumber.getBigDecimal(EntityUtil.getFirst(solicitudes).getBigDecimal("montoDiario")).
												add(UtilNumber.getBigDecimal(EntityUtil.getFirst(solicitudes).getBigDecimal("montoTransporte"))).
													add(UtilNumber.getBigDecimal(EntityUtil.getFirst(solicitudes).getBigDecimal("montoTrabCampo"))).doubleValue()));
		jrParameters.put("MONTO_DIARIO", UtilNumber.getBigDecimal(EntityUtil.getFirst(solicitudes).getBigDecimal("montoDiario")));
		jrParameters.put("TEXTO_MONTO_DIARIO", UtilNumberToLetter.convertNumberToLetter(UtilNumber.getBigDecimal(EntityUtil.getFirst(solicitudes).getBigDecimal("montoDiario")).doubleValue()));
		jrParameters.put("TRANSPORTE", EntityUtil.getFirst(solicitudes).getString("descripcion"));
		jrParameters.put("MONTO_TRANSP", UtilNumber.getBigDecimal(EntityUtil.getFirst(solicitudes).getBigDecimal("montoTransporte")));
		jrParameters.put("TEXTO_MONTO_TRANSPORTE", UtilNumberToLetter.convertNumberToLetter(UtilNumber.getBigDecimal(EntityUtil.getFirst(solicitudes).getBigDecimal("montoTransporte")).doubleValue()));
		jrParameters.put("MONTO_TRAB_C", UtilNumber.getBigDecimal(EntityUtil.getFirst(solicitudes).getBigDecimal("montoTrabCampo")));
		jrParameters.put("TEXTO_MONTO_TRABAJO_CAMPO", UtilNumberToLetter.convertNumberToLetter(UtilNumber.getBigDecimal(EntityUtil.getFirst(solicitudes).getBigDecimal("montoTrabCampo")).doubleValue()));
		jrParameters.put("SOLICITANTE", EntityUtil.getFirst(solicitudes).getString("firstName")+" "+EntityUtil.getFirst(solicitudes).getString("lastName"));
		jrParameters.put("FIRMASOLICITANTE", EntityUtil.getFirst(solicitudes).getString("firmaId"));
		
		int i = 1;
		for (GenericValue autorizador : autorizadores){
			jrParameters.put("AUTORIZADOR"+i, autorizador.getString("firstName")+" "+ autorizador.getString("lastName"));
			jrParameters.put("FIRMAAUTORIZADOR"+i, autorizador.getString("firmaId"));
			i++;
		}
		

		for(GenericValue solicitud : solicitudes){
			
			FastMap<String, Object> reportLine = FastMap.<String, Object>newInstance();
			
			String nombre;
			String apellido;
			String comentario;
			
			if(UtilValidate.isEmpty(solicitud.getString("firstName2"))){
				nombre = "";
			}else{
				nombre=solicitud.getString("firstName2");
			}
			
			if(UtilValidate.isEmpty(solicitud.getString("lastName2"))){
				apellido = "";
			}else{
				apellido=solicitud.getString("lastName2");
			}
			
			if(UtilValidate.isEmpty(solicitud.getString("comentario"))){
				comentario = "";
			}else{
				comentario=solicitud.getString("comentario");
			}
			
			reportLine.put("NOMBRE", nombre+" "+apellido);
			reportLine.put("COMENTARIO", comentario);
			
			plainList.add(reportLine);
			
		}
		request.setAttribute("jrParameters", jrParameters);
		request.setAttribute("jrDataSource", new JRMapCollectionDataSource(plainList));
			
		
	} catch (GenericEntityException e1) {
			
		e1.printStackTrace();
		Debug.log(e1.getMessage());
		
		UtilMessage.addError(request, UtilMessage.expandLabel("ErrorMessage_noSePudoCrearElReporte", locale));
	
	}
		
		
		return "success";
		
	}

}
