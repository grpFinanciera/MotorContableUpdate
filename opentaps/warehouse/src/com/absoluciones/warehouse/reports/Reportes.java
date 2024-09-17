package com.absoluciones.warehouse.reports;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
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
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilNumber;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.common.AB.UtilWorkflow;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.common.util.UtilMessage;

public class Reportes {
	
	public static String reporteAlmacen(HttpServletRequest request, HttpServletResponse response){
				
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		Locale locale = UtilHttp.getLocale(request);
		final TimeZone timeZone = UtilHttp.getTimeZone(request);
		String dateFormat = UtilDateTime.getDateFormat(locale);
		
		String reportType = UtilCommon.getParameter(request, "tipoReporte");
		String productTypeId = UtilCommon.getParameter(request, "productTypeId");
		String facilityId = UtilCommon.getParameter(request, "facilityId");
		String productId = UtilCommon.getParameter(request, "productId");
		String dia = UtilCommon.getParameter(request, "thruDate");
		
		try {
			Date diaSql = UtilDateTime.getDateSqlFromString(dateFormat, timeZone, locale, dia);
			
			Timestamp thruDate = UtilDateTime.toTimestamp(diaSql);
			
			thruDate = UtilDateTime.getDayEnd(thruDate);
		
			GenericValue producto = delegator.findByPrimaryKey("ProductType", UtilMisc.toMap("productTypeId", productTypeId));
			
			GenericValue logoUrl = delegator.findByPrimaryKey("PartyGroup", UtilMisc.toMap("partyId", "1"));
			GenericValue usuario = UtilCommon.getUserLogin(request);
			GenericValue nombreUsuario = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", usuario.getString("partyId")));
			
			Map<String, Object> jrParameters = FastMap.newInstance();
			
			jrParameters.put("organizacion", logoUrl.getString("groupNameLocal"));
			jrParameters.put("logoUrl", logoUrl.getString("logoImageUrl"));
			jrParameters.put("logoUrl2", logoUrl.getString("logoImageUrl2"));
			jrParameters.put("TipoProducto",producto.getString("description"));
			jrParameters.put("Fecha", thruDate);
			
			String emisor = getNombre(nombreUsuario);
			
			
			jrParameters.put("Emisor", emisor);
			
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();

			if(UtilValidate.isNotEmpty(thruDate) && thruDate != null){
				searchConditions.add(EntityCondition.makeCondition("fechaContable", EntityOperator.LESS_THAN_EQUAL_TO, thruDate));
			}
			if(UtilValidate.isNotEmpty(productTypeId) && productTypeId != null){
				searchConditions.add(EntityCondition.makeCondition("productTypeId", EntityOperator.EQUALS, productTypeId));
			}
			if(UtilValidate.isNotEmpty(facilityId) && facilityId != null){
				searchConditions.add(EntityCondition.makeCondition("facilityId", EntityOperator.EQUALS, facilityId));
			}
			if(UtilValidate.isNotEmpty(productId) && productId != null){
				searchConditions.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId));
			}
			EntityCondition conditions = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
			
			List<String> selects = new ArrayList<String>();
			selects.add("facilityName");
			selects.add("facilityId");
			selects.add("productId");
			selects.add("lotId");
			selects.add("descripcion");
			selects.add("uomId");
			selects.add("quantityOnHandDiff");
			selects.add("unitCost");
			selects.add("productName");
			selects.add("abbreviation");
			
			
			List<String> orderBy = new ArrayList<String>();
			orderBy.add("facilityName");
			orderBy.add("facilityId");
			orderBy.add("productId");
			
			List<Map<String, Object>> plainList = FastList.<Map<String, Object>>newInstance();
			
			List<GenericValue> almacenes = delegator.findByCondition("ReporteInventario", conditions, selects, orderBy);

			for(GenericValue almacen : almacenes){

				FastMap<String, Object> reportLine = FastMap.<String, Object>newInstance();

				reportLine.put("NombreAlmacen", almacen.getString("facilityName"));
				reportLine.put("CodigoAlmacen", almacen.getString("facilityId"));
				reportLine.put("CodigoProducto", almacen.getString("productId"));
				reportLine.put("CodigoLote", almacen.getString("lotId"));
				reportLine.put("NombreInterno", almacen.getString("productName"));
				reportLine.put("UnidadMedida", almacen.getString("abbreviation"));
				
				if(almacen.getBigDecimal("quantityOnHandDiff")==null){
					reportLine.put("CantidadDisponible", BigDecimal.ZERO);
				}else{
					reportLine.put("CantidadDisponible", almacen.getBigDecimal("quantityOnHandDiff"));
				}
				
				if(almacen.getBigDecimal("unitCost")==null){
					reportLine.put("PrecioUnitario", BigDecimal.ZERO);
				}else{
					reportLine.put("PrecioUnitario", almacen.getBigDecimal("unitCost"));
				}
				
				reportLine.put("CostoTotal", UtilNumber.getBigDecimal(almacen.getBigDecimal("quantityOnHandDiff")).multiply(UtilNumber.getBigDecimal(almacen.getBigDecimal("unitCost"))));
				reportLine.put("Familia", almacen.getString("descripcion"));

				plainList.add(reportLine);
			}
			
			request.setAttribute("jrParameters", jrParameters);
			request.setAttribute("jrDataSource", new JRMapCollectionDataSource(plainList));
			
		} catch (Exception e) {
			e.printStackTrace();
			Debug.log(e.getMessage());
			
			UtilMessage.addError(request, UtilMessage.expandLabel("ErrorMessage_noSePudoCrearElReporte", locale));
		}
				
		return reportType;
	}
	
	public static String getNombre(GenericValue nombre){
			
			String nombreUsuario="";
			
			if(UtilValidate.isNotEmpty(nombre.getString("firstName"))){
				
				nombreUsuario=nombre.getString("firstName");	
				
			}
			
			if(UtilValidate.isNotEmpty(nombre.getString("middleName"))){
				
				nombreUsuario=nombreUsuario +" " +nombre.getString("middleName");
				
			}
			
			if(UtilValidate.isNotEmpty(nombre.getString("lastName"))){
				
				nombreUsuario= nombreUsuario +" "+nombre.getString("lastName");
				
			}
			
			return nombreUsuario;
		}
	
	/**
	 * Genera el reporte de solicitud de transferencia
	 * @param request
	 * @param response
	 * @return
	 */
	public static String reporteSolicitudTransferencia(HttpServletRequest request, HttpServletResponse response){
		
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String solicitudTransferenciaId = UtilCommon.getParameter(request, "solicitudTransferenciaId");
		
		try {
			
			GenericValue SolicitudTransferencia = delegator.findByPrimaryKey("SolicitudTransferencia",UtilMisc.toMap("solicitudTransferenciaId",solicitudTransferenciaId));
			
			GenericValue logoUrl = delegator.findByPrimaryKey("PartyGroup", UtilMisc.toMap("partyId", "1"));
			
			Map<String, Object> jrParameters = FastMap.newInstance();
			
			List<EntityCondition> searchConditions = new FastList<EntityCondition>();
			searchConditions.add(EntityCondition.makeCondition("statusId",UtilWorkflow.estatusAutorizada));
			searchConditions.add(EntityCondition.makeCondition("origenId",solicitudTransferenciaId));
			searchConditions.add(EntityCondition.makeCondition("tipoWorkFlowId",UtilWorkflow.tipoTransferencia));
			
			List<GenericValue> listStatusAutorizacion = delegator.findByCondition("StatusAutorizacionWorkFlow", EntityCondition.makeCondition(searchConditions), null, null);
			
			StringBuffer autorizadores = new StringBuffer();
			for (GenericValue Autorizador : listStatusAutorizacion) {
				autorizadores.append(getNombre(delegator.findByPrimaryKey("Person",UtilMisc.toMap("partyId",Autorizador.getString("personParentId"))))+"\n");
			}
			
			jrParameters.put("organizacion", logoUrl.getString("groupNameLocal"));
			jrParameters.put("logoUrl", logoUrl.getString("logoImageUrl"));
			jrParameters.put("logoUrl2", logoUrl.getString("logoImageUrl2"));
			jrParameters.putAll(SolicitudTransferencia.getAllFields());
			jrParameters.put("almacenOrigen",SolicitudTransferencia.getRelatedOne("OrigenFacility").getString("facilityName"));
			jrParameters.put("almacenDestino",SolicitudTransferencia.getRelatedOne("DestinoFacility").getString("facilityName"));
			jrParameters.put("solicitadoPor",getNombre(SolicitudTransferencia.getRelatedOne("Person")));
			jrParameters.put("autorizadoPor",autorizadores.toString());
			
			List<GenericValue> listInventoryItem = delegator.findByAnd("SolicitudTransferenciaCostoUnitario",UtilMisc.toMap("solicitudTransferenciaId",solicitudTransferenciaId));
			List<Map<String, Object>> plainList = FastList.<Map<String, Object>>newInstance();
			
			Long cantidadTransferida = new Long(0);
			BigDecimal unitCost = BigDecimal.ZERO;
			BigDecimal total = BigDecimal.ZERO;
			for (GenericValue InventoryItem : listInventoryItem) {
				
				FastMap<String, Object> reportLine = FastMap.<String, Object>newInstance();
				
				cantidadTransferida = InventoryItem.getLong("cantidadTransferida");
				unitCost = UtilNumber.getBigDecimal(InventoryItem.getBigDecimal("unitCost"));
				total = BigDecimal.valueOf(cantidadTransferida.doubleValue() * unitCost.doubleValue());
				
				reportLine.put("productId", InventoryItem.getString("productId"));
				reportLine.put("productName", InventoryItem.getString("productName"));
				reportLine.put("cantidadTransferida", cantidadTransferida);
				reportLine.put("unitCost", unitCost);
				reportLine.put("total",total);
				
				
				plainList.add(reportLine);
			}
			
			request.setAttribute("jrParameters", jrParameters);
			request.setAttribute("jrDataSource", new JRMapCollectionDataSource(plainList));
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		return "success";
	}
	

}
