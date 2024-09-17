package com.opensourcestrategies.financials.reports;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

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
import org.opentaps.common.util.UtilCommon;
import org.opentaps.common.util.UtilMessage;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

public class Reportes {

	public static DecimalFormat df = new DecimalFormat ("#.00");

	public static String reportePoliza(HttpServletRequest request, HttpServletResponse response) 
	{
		
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		Locale locale = UtilHttp.getLocale(request);

		String reportType = UtilCommon.getParameter(request, "tipoReporte");
		String agrupado = UtilCommon.getParameter(request, "isAgrupado");
		String organizationPartyId = UtilCommon.getParameter(request, "organizationPartyId");
		String acctgTransId = UtilCommon.getParameter(request, "acctgTransId");
		
		try {

			GenericValue acctTrans = delegator.findByPrimaryKey("AcctgTrans",UtilMisc.toMap("acctgTransId", acctgTransId));
			String tipoPoliza = acctTrans.getRelatedOne("tpcpTipoPoliza").getString("description").toUpperCase();
			GenericValue usuario = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", acctTrans.getString("createdByUserLogin")));
			GenericValue nombreUsuario = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", usuario.getString("partyId")));
			GenericValue logoUrl = delegator.findByPrimaryKey("PartyGroup", UtilMisc.toMap("partyId", acctTrans.getString("organizationPartyId")));
			
			List<String> selects = new ArrayList<String>();
			selects.add("postedDate");
			selects.add("glAccountId");
			selects.add("accountName");
			selects.add("amount");
			selects.add("debitCreditFlag");


			EntityCondition conditions = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("acctgTransId", acctgTransId),
					EntityCondition.makeCondition("organizationPartyId", organizationPartyId));
			
			Map<String, Object> jrParameters = FastMap.newInstance();
			
			jrParameters.put("organizacion", logoUrl.getString("groupNameLocal"));
			jrParameters.put("logoUrl", logoUrl.getString("logoImageUrl"));
			jrParameters.put("logoUrl2", logoUrl.getString("logoImageUrl2"));
			jrParameters.put("TipoPoliza", tipoPoliza);
			jrParameters.put("Poliza", acctTrans.getString("poliza"));
			jrParameters.put("Concepto", acctTrans.getString("description"));
			
			String emisor = getNombre(nombreUsuario);
			
			
			jrParameters.put("Emisor", emisor);

			List<Map<String, Object>> plainList = FastList.<Map<String, Object>>newInstance();

			if(Boolean.parseBoolean(agrupado)){
				
				List<GenericValue> poliza = delegator.findByCondition("ImprimePolizaAgrupada", conditions, selects, null);

				for(GenericValue polizas : poliza){

					FastMap<String, Object> reportLine = FastMap.<String, Object>newInstance();

					reportLine.put("Fecha", polizas.getTimestamp("postedDate"));
					reportLine.put("Cuenta", polizas.getString("glAccountId"));
					reportLine.put("Descripcion", polizas.getString("accountName"));

					if(polizas.getString("debitCreditFlag").equals("D")){

						reportLine.put("Cargos", polizas.getBigDecimal("amount"));
						reportLine.put("Abonos", BigDecimal.ZERO);

					}else{

						reportLine.put("Cargos", BigDecimal.ZERO);
						reportLine.put("Abonos", polizas.getBigDecimal("amount"));
						
					}

					plainList.add(reportLine);

				}
				
				request.setAttribute("jrParameters", jrParameters);
				request.setAttribute("jrDataSource", new JRMapCollectionDataSource(plainList));

				

			}else{
				
				selects.add("theirPartyId");
				selects.add("cuentaBancariaId");

				List<GenericValue> poliza = delegator.findByCondition("ImprimePolizaNoAgrupada", conditions, selects, null);

				for(GenericValue polizas : poliza){

					FastMap<String, Object> reportLine = FastMap.<String, Object>newInstance();

					reportLine.put("Fecha", polizas.getTimestamp("postedDate"));
					reportLine.put("Cuenta", polizas.getString("glAccountId"));
					reportLine.put("Descripcion", polizas.getString("accountName"));
					
					GenericValue party = polizas.getRelatedOne("PartyGroup");
					GenericValue person = polizas.getRelatedOne("Person");
					GenericValue cuenta = polizas.getRelatedOne("CuentaBancaria");
					
					if(polizas.getString("theirPartyId")!=null){
						
						if(party !=null){
							
							reportLine.put("Auxiliar", party.getString("groupName"));
							
						}else if(person != null){
							
							String auxiliar = getNombre(person);
							
							reportLine.put("Auxiliar", auxiliar);
							
						}
						
					}else if(polizas.getString("cuentaBancariaId")!=null){
						
						reportLine.put("Auxiliar", cuenta.getString("numeroCuenta") +" "+cuenta.getString("descripcion"));
						
					}else{
						
						reportLine.put("Auxiliar", null);
						
					}

					if(polizas.getString("debitCreditFlag").equals("D")){

						reportLine.put("Cargos", polizas.getBigDecimal("amount"));
						reportLine.put("Abonos", BigDecimal.ZERO);

					}else{

						reportLine.put("Cargos", BigDecimal.ZERO);
						reportLine.put("Abonos", polizas.getBigDecimal("amount"));
						
					}

					plainList.add(reportLine);

				}

				request.setAttribute("jrParameters", jrParameters);
				request.setAttribute("jrDataSource", new JRMapCollectionDataSource(plainList));
				
			}
		} catch (GenericEntityException e1) {
			
			e1.printStackTrace();
			Debug.log(e1.getMessage());
			
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
	
	public static String reporteConciliacion(HttpServletRequest request, HttpServletResponse response) 
	{
		
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		Locale locale = UtilHttp.getLocale(request);
		final TimeZone timeZone = UtilHttp.getTimeZone(request);
		String dateFormat = UtilDateTime.getDateFormat(locale);
		

		Map<String, Object> jrParameters = FastMap.newInstance();
		
		String reportType = UtilCommon.getParameter(request, "tipoReporte");
		String diaInicial = UtilCommon.getParameter(request, "fromDate");
		String diaFinal = UtilCommon.getParameter(request, "thruDate");
		String cierre = UtilCommon.getParameter(request, "cierre");
		
		try {
			
			GenericValue logoUrl = delegator.findByPrimaryKey("PartyGroup", UtilMisc.toMap("partyId", "1"));

			Date diaInicialSql = UtilDateTime.getDateSqlFromString(dateFormat, timeZone, locale, diaInicial);
			
			Timestamp fromDate = UtilDateTime.toTimestamp(diaInicialSql);
			
			fromDate = UtilDateTime.getDayStart(fromDate);
			
			Date diaFinalSql = UtilDateTime.getDateSqlFromString(dateFormat, timeZone, locale, diaFinal);
			
			Timestamp thruDate = UtilDateTime.toTimestamp(diaFinalSql);
			
			thruDate = UtilDateTime.getDayEnd(thruDate);
			
			List<EntityCondition> searchConditionsContables = new FastList<EntityCondition>();
			List<EntityCondition> searchConditionsPresupuestales = new FastList<EntityCondition>();
			
			List<String> polizasCierreCont = new FastList<String>();
			polizasCierreCont.add("CIPE-01");
			polizasCierreCont.add("CIPE-02");
			
			if(UtilValidate.isNotEmpty(fromDate) && fromDate != null){
				searchConditionsContables.add(EntityCondition.makeCondition("postedDate", EntityOperator.GREATER_THAN_EQUAL_TO, fromDate));
				searchConditionsPresupuestales.add(EntityCondition.makeCondition("postedDate", EntityOperator.GREATER_THAN_EQUAL_TO, fromDate));
			}
			
			if(UtilValidate.isNotEmpty(thruDate) && thruDate != null){
				searchConditionsContables.add(EntityCondition.makeCondition("postedDate", EntityOperator.LESS_THAN_EQUAL_TO, thruDate));
				searchConditionsPresupuestales.add(EntityCondition.makeCondition("postedDate", EntityOperator.LESS_THAN_EQUAL_TO, thruDate));
			}
			
			if(UtilValidate.isNotEmpty(cierre) && cierre != null){
				if(cierre.equals("false")){
					searchConditionsContables.add(EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.NOT_EQUAL, "PERIOD_CLOSING"));
					searchConditionsPresupuestales.add(EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.NOT_IN, polizasCierreCont));
				}
			}
			List<String> cuentasPres = new FastList<String>();
			cuentasPres.add("8.2.5");
			cuentasPres.add("8.2.6");
			cuentasPres.add("8.2.7");
			
			searchConditionsPresupuestales.add(EntityCondition.makeCondition("glAccountId", EntityOperator.IN, cuentasPres));
			
			searchConditionsPresupuestales.add(EntityCondition.makeCondition("enumTypeId", EntityOperator.EQUALS, "CL_COG"));
			
			List<EntityCondition> searchConditionsContablesOr = new FastList<EntityCondition>();
			List<String> cuentas = new FastList<String>();
			
			cuentas.add("5.2.3.1.01");
			cuentas.add("5.2.4.1");
			cuentas.add("1.2.4.1.03");
			cuentas.add("1.2.4.1.01");
			cuentas.add("1.2.4.1.05");
			cuentas.add("1.2.4.1.07");
			cuentas.add("1.2.4.6.05");
			cuentas.add("1.2.3.5.01");
			
			searchConditionsContablesOr.add(EntityCondition.makeCondition("glAccountId", EntityOperator.LIKE, "5.1%"));
			
			searchConditionsContablesOr.add(EntityCondition.makeCondition("glAccountId", EntityOperator.IN, cuentas));
			
			searchConditionsContables.add(EntityCondition.makeCondition(searchConditionsContablesOr, EntityOperator.OR));
			
			EntityCondition conditionsContables = EntityCondition.makeCondition(searchConditionsContables, EntityOperator.AND);
			EntityCondition conditionsPresupuestales = EntityCondition.makeCondition(searchConditionsPresupuestales, EntityOperator.AND);
			
			List<String> selectsContable = new ArrayList<String>();
			selectsContable.add("amount");
			selectsContable.add("glAccountId");
			selectsContable.add("debitCreditFlag");
			
			List<String> orderByContable = new ArrayList<String>();
			orderByContable.add("glAccountId");
			
			List<GenericValue> conciliacionContable = delegator.findByCondition("ConciliacionCont", conditionsContables, selectsContable, orderByContable);
			
			List<String> selectsPresupuestal = new ArrayList<String>();
			selectsPresupuestal.add("amount");
			selectsPresupuestal.add("sequenceId");
			selectsPresupuestal.add("debitCreditFlag");
			
			List<String> orderByPresupuestal = new ArrayList<String>();
			orderByPresupuestal.add("sequenceId");
			
			List<GenericValue> conciliacionPresupuestal = delegator.findByCondition("ConciliacionPres", conditionsPresupuestales, selectsPresupuestal, orderByPresupuestal);
			
			Map<String,BigDecimal> contable = FastMap.newInstance();
			BigDecimal auxiliarCont = BigDecimal.ZERO;
			BigDecimal totalContable = BigDecimal.ZERO;
			
			for (GenericValue conContable : conciliacionContable) {
				
				auxiliarCont= UtilNumber.getBigDecimal(contable.get(conContable.getString("glAccountId")));
					
				if(conContable.getString("debitCreditFlag").equals("C")){
					contable.put(conContable.getString("glAccountId"), auxiliarCont.subtract(conContable.getBigDecimal("amount")));
					totalContable = totalContable.subtract(conContable.getBigDecimal("amount"));
				}else{
					contable.put(conContable.getString("glAccountId"),auxiliarCont.add(conContable.getBigDecimal("amount")));
					totalContable = totalContable.add(conContable.getBigDecimal("amount"));
				}
				
				
				
			}
			
			Map<String,BigDecimal> presupuestal = FastMap.newInstance();
			BigDecimal auxiliarPres = BigDecimal.ZERO;
			BigDecimal totalPresupuestal = BigDecimal.ZERO;
			
			for (GenericValue conPresupuestal : conciliacionPresupuestal) {
				
				auxiliarPres= UtilNumber.getBigDecimal(presupuestal.get(conPresupuestal.getString("sequenceId")));
					
				if(conPresupuestal.getString("debitCreditFlag").equals("C")){
					presupuestal.put(conPresupuestal.getString("sequenceId"), auxiliarPres.subtract(conPresupuestal.getBigDecimal("amount")));
					totalPresupuestal = totalPresupuestal.subtract(conPresupuestal.getBigDecimal("amount"));
				}else{
					presupuestal.put(conPresupuestal.getString("sequenceId"), auxiliarPres.add(conPresupuestal.getBigDecimal("amount")));
					totalPresupuestal = totalPresupuestal.add(conPresupuestal.getBigDecimal("amount"));
				}
				
			}
			
			jrParameters.put("ORGANIZACION", logoUrl.getString("groupNameLocal"));
			jrParameters.put("logoUrl", logoUrl.getString("logoImageUrl"));
			jrParameters.put("logoUrl2", logoUrl.getString("logoImageUrl2"));
			
			//COGS
			
			jrParameters.put("cog11301", UtilNumber.getBigDecimal(presupuestal.get("11301")));
			jrParameters.put("cog12101", UtilNumber.getBigDecimal(presupuestal.get("12101")));
			jrParameters.put("cog13101", UtilNumber.getBigDecimal(presupuestal.get("13101")));
			jrParameters.put("cog13102", UtilNumber.getBigDecimal(presupuestal.get("13102")));
			jrParameters.put("cog13201", UtilNumber.getBigDecimal(presupuestal.get("13201")));
			jrParameters.put("cog13202", UtilNumber.getBigDecimal(presupuestal.get("13202")));
			jrParameters.put("cog13409", UtilNumber.getBigDecimal(presupuestal.get("13409")));
			jrParameters.put("cog14103", UtilNumber.getBigDecimal(presupuestal.get("14103")));
			jrParameters.put("cog14105", UtilNumber.getBigDecimal(presupuestal.get("14105")));
			jrParameters.put("cog14202", UtilNumber.getBigDecimal(presupuestal.get("14202")));
			jrParameters.put("cog14301", UtilNumber.getBigDecimal(presupuestal.get("14301")));
			jrParameters.put("cog14401", UtilNumber.getBigDecimal(presupuestal.get("14401")));
			jrParameters.put("cog14403", UtilNumber.getBigDecimal(presupuestal.get("14403")));
			jrParameters.put("cog15101", UtilNumber.getBigDecimal(presupuestal.get("15101")));
			jrParameters.put("cog15401", UtilNumber.getBigDecimal(presupuestal.get("15401")));
			jrParameters.put("cog15402", UtilNumber.getBigDecimal(presupuestal.get("15402")));
			jrParameters.put("cog15901", UtilNumber.getBigDecimal(presupuestal.get("15901")));
			jrParameters.put("cog17102", UtilNumber.getBigDecimal(presupuestal.get("17102")));
			jrParameters.put("cog21101", UtilNumber.getBigDecimal(presupuestal.get("21101")));
			jrParameters.put("cog21401", UtilNumber.getBigDecimal(presupuestal.get("21401")));
			jrParameters.put("cog21501", UtilNumber.getBigDecimal(presupuestal.get("21501")));
			jrParameters.put("cog21502", UtilNumber.getBigDecimal(presupuestal.get("21502")));
			jrParameters.put("cog22104", UtilNumber.getBigDecimal(presupuestal.get("22104")));
			jrParameters.put("cog22301", UtilNumber.getBigDecimal(presupuestal.get("22301")));
			jrParameters.put("cog24601", UtilNumber.getBigDecimal(presupuestal.get("24601")));
			jrParameters.put("cog24801", UtilNumber.getBigDecimal(presupuestal.get("24801")));
			jrParameters.put("cog24901", UtilNumber.getBigDecimal(presupuestal.get("24901")));
			jrParameters.put("cog25301", UtilNumber.getBigDecimal(presupuestal.get("25301")));
			jrParameters.put("cog26103", UtilNumber.getBigDecimal(presupuestal.get("26103")));
			jrParameters.put("cog26104", UtilNumber.getBigDecimal(presupuestal.get("26104")));
			jrParameters.put("cog27101", UtilNumber.getBigDecimal(presupuestal.get("27101")));
			jrParameters.put("cog27301", UtilNumber.getBigDecimal(presupuestal.get("27301")));
			jrParameters.put("cog29301", UtilNumber.getBigDecimal(presupuestal.get("29301")));
			jrParameters.put("cog29401", UtilNumber.getBigDecimal(presupuestal.get("29401")));
			jrParameters.put("cog29501", UtilNumber.getBigDecimal(presupuestal.get("29501")));
			jrParameters.put("cog31101", UtilNumber.getBigDecimal(presupuestal.get("31101")));
			jrParameters.put("cog31301", UtilNumber.getBigDecimal(presupuestal.get("31301")));
			jrParameters.put("cog31401", UtilNumber.getBigDecimal(presupuestal.get("31401")));
			jrParameters.put("cog31501", UtilNumber.getBigDecimal(presupuestal.get("31501")));
			jrParameters.put("cog31701", UtilNumber.getBigDecimal(presupuestal.get("31701")));
			jrParameters.put("cog31801", UtilNumber.getBigDecimal(presupuestal.get("31801")));
			jrParameters.put("cog32301", UtilNumber.getBigDecimal(presupuestal.get("32301")));
			jrParameters.put("cog32502", UtilNumber.getBigDecimal(presupuestal.get("32502")));
			jrParameters.put("cog32701", UtilNumber.getBigDecimal(presupuestal.get("32701")));
			jrParameters.put("cog33104", UtilNumber.getBigDecimal(presupuestal.get("33104")));
			jrParameters.put("cog33301", UtilNumber.getBigDecimal(presupuestal.get("33301")));
			jrParameters.put("cog33401", UtilNumber.getBigDecimal(presupuestal.get("33401")));
			jrParameters.put("cog33501", UtilNumber.getBigDecimal(presupuestal.get("33501")));
			jrParameters.put("cog33601", UtilNumber.getBigDecimal(presupuestal.get("33601")));
			jrParameters.put("cog33602", UtilNumber.getBigDecimal(presupuestal.get("33602")));
			jrParameters.put("cog33604", UtilNumber.getBigDecimal(presupuestal.get("33604")));
			jrParameters.put("cog33605", UtilNumber.getBigDecimal(presupuestal.get("33605")));
			jrParameters.put("cog33699", UtilNumber.getBigDecimal(presupuestal.get("33699")));
			jrParameters.put("cog33801", UtilNumber.getBigDecimal(presupuestal.get("33801")));
			jrParameters.put("cog33901", UtilNumber.getBigDecimal(presupuestal.get("33901")));
			jrParameters.put("cog33903", UtilNumber.getBigDecimal(presupuestal.get("33903")));
			jrParameters.put("cog34101", UtilNumber.getBigDecimal(presupuestal.get("34101")));
			jrParameters.put("cog34501", UtilNumber.getBigDecimal(presupuestal.get("34501")));
			jrParameters.put("cog34701", UtilNumber.getBigDecimal(presupuestal.get("34701")));
			jrParameters.put("cog35101", UtilNumber.getBigDecimal(presupuestal.get("35101")));
			jrParameters.put("cog35201", UtilNumber.getBigDecimal(presupuestal.get("35201")));
			jrParameters.put("cog35301", UtilNumber.getBigDecimal(presupuestal.get("35301")));
			jrParameters.put("cog35701", UtilNumber.getBigDecimal(presupuestal.get("35701")));
			jrParameters.put("cog35801", UtilNumber.getBigDecimal(presupuestal.get("35801")));
			jrParameters.put("cog37204", UtilNumber.getBigDecimal(presupuestal.get("37204")));
			jrParameters.put("cog37206", UtilNumber.getBigDecimal(presupuestal.get("37206")));
			jrParameters.put("cog37504", UtilNumber.getBigDecimal(presupuestal.get("37504")));
			jrParameters.put("cog37602", UtilNumber.getBigDecimal(presupuestal.get("37602")));
			jrParameters.put("cog37901", UtilNumber.getBigDecimal(presupuestal.get("37901")));
			jrParameters.put("cog38301", UtilNumber.getBigDecimal(presupuestal.get("38301")));
			jrParameters.put("cog38401", UtilNumber.getBigDecimal(presupuestal.get("38401")));
			jrParameters.put("cog38501", UtilNumber.getBigDecimal(presupuestal.get("38501")));
			jrParameters.put("cog39202", UtilNumber.getBigDecimal(presupuestal.get("39202")));
			jrParameters.put("cog39801", UtilNumber.getBigDecimal(presupuestal.get("39801")));
			jrParameters.put("cog43901", UtilNumber.getBigDecimal(presupuestal.get("43901")));
			jrParameters.put("cog44102", UtilNumber.getBigDecimal(presupuestal.get("44102")));
			jrParameters.put("cog51101", UtilNumber.getBigDecimal(presupuestal.get("51101")));
			jrParameters.put("cog51501", UtilNumber.getBigDecimal(presupuestal.get("51501")));
			jrParameters.put("cog51901", UtilNumber.getBigDecimal(presupuestal.get("51901")));
			jrParameters.put("cog52101", UtilNumber.getBigDecimal(presupuestal.get("52101")));
			jrParameters.put("cog56601", UtilNumber.getBigDecimal(presupuestal.get("56601")));
			jrParameters.put("cog62201", UtilNumber.getBigDecimal(presupuestal.get("62201")));
			//TotalCOG
			jrParameters.put("TotalCOG", totalPresupuestal);
			
			//CUENTAS
			
			jrParameters.put("cta511101", UtilNumber.getBigDecimal(contable.get("5.1.1.1.01")));
			jrParameters.put("cta511201", UtilNumber.getBigDecimal(contable.get("5.1.1.2.01")));
			jrParameters.put("cta511301", UtilNumber.getBigDecimal(contable.get("5.1.1.3.01")));
			jrParameters.put("cta511302", UtilNumber.getBigDecimal(contable.get("5.1.1.3.02")));
			jrParameters.put("cta511303", UtilNumber.getBigDecimal(contable.get("5.1.1.3.03")));
			jrParameters.put("cta511304", UtilNumber.getBigDecimal(contable.get("5.1.1.3.04")));
			jrParameters.put("cta511306", UtilNumber.getBigDecimal(contable.get("5.1.1.3.06")));
			jrParameters.put("cta511401", UtilNumber.getBigDecimal(contable.get("5.1.1.4.01")));
			jrParameters.put("cta511402", UtilNumber.getBigDecimal(contable.get("5.1.1.4.02")));
			jrParameters.put("cta511403", UtilNumber.getBigDecimal(contable.get("5.1.1.4.03")));
			jrParameters.put("cta511406", UtilNumber.getBigDecimal(contable.get("5.1.1.4.06")));
			jrParameters.put("cta511501", UtilNumber.getBigDecimal(contable.get("5.1.1.5.01")));
			jrParameters.put("cta511503", UtilNumber.getBigDecimal(contable.get("5.1.1.5.03")));
			jrParameters.put("cta511504", UtilNumber.getBigDecimal(contable.get("5.1.1.5.04")));
			jrParameters.put("cta511509", UtilNumber.getBigDecimal(contable.get("5.1.1.5.09")));
			jrParameters.put("cta511701", UtilNumber.getBigDecimal(contable.get("5.1.1.7.01")));
			jrParameters.put("cta512101", UtilNumber.getBigDecimal(contable.get("5.1.2.1.01")));
			jrParameters.put("cta512104", UtilNumber.getBigDecimal(contable.get("5.1.2.1.04")));
			jrParameters.put("cta512105", UtilNumber.getBigDecimal(contable.get("5.1.2.1.05")));
			jrParameters.put("cta512106", UtilNumber.getBigDecimal(contable.get("5.1.2.1.06")));
			jrParameters.put("cta512201", UtilNumber.getBigDecimal(contable.get("5.1.2.2.01")));
			jrParameters.put("cta512401", UtilNumber.getBigDecimal(contable.get("5.1.2.4.01")));
			jrParameters.put("cta512402", UtilNumber.getBigDecimal(contable.get("5.1.2.4.02")));
			jrParameters.put("cta512403", UtilNumber.getBigDecimal(contable.get("5.1.2.4.03")));
			jrParameters.put("cta512501", UtilNumber.getBigDecimal(contable.get("5.1.2.5.01")));
			jrParameters.put("cta512603", UtilNumber.getBigDecimal(contable.get("5.1.2.6.03")));
			jrParameters.put("cta512604", UtilNumber.getBigDecimal(contable.get("5.1.2.6.04")));
			jrParameters.put("cta512701", UtilNumber.getBigDecimal(contable.get("5.1.2.7.01")));
			jrParameters.put("cta512901", UtilNumber.getBigDecimal(contable.get("5.1.2.9.01")));
			jrParameters.put("cta512902", UtilNumber.getBigDecimal(contable.get("5.1.2.9.02")));
			jrParameters.put("cta512903", UtilNumber.getBigDecimal(contable.get("5.1.2.9.03")));
			jrParameters.put("cta513101", UtilNumber.getBigDecimal(contable.get("5.1.3.1.01")));
			jrParameters.put("cta513103", UtilNumber.getBigDecimal(contable.get("5.1.3.1.03")));
			jrParameters.put("cta513104", UtilNumber.getBigDecimal(contable.get("5.1.3.1.04")));
			jrParameters.put("cta513105", UtilNumber.getBigDecimal(contable.get("5.1.3.1.05")));
			jrParameters.put("cta513107", UtilNumber.getBigDecimal(contable.get("5.1.3.1.07")));
			jrParameters.put("cta513108", UtilNumber.getBigDecimal(contable.get("5.1.3.1.08")));
			jrParameters.put("cta513203", UtilNumber.getBigDecimal(contable.get("5.1.3.2.03")));
			jrParameters.put("cta513205", UtilNumber.getBigDecimal(contable.get("5.1.3.2.05")));
			jrParameters.put("cta513207", UtilNumber.getBigDecimal(contable.get("5.1.3.2.07")));
			jrParameters.put("cta513301", UtilNumber.getBigDecimal(contable.get("5.1.3.3.01")));
			jrParameters.put("cta513302", UtilNumber.getBigDecimal(contable.get("5.1.3.3.02")));
			jrParameters.put("cta513303", UtilNumber.getBigDecimal(contable.get("5.1.3.3.03")));
			jrParameters.put("cta513304", UtilNumber.getBigDecimal(contable.get("5.1.3.3.04")));
			jrParameters.put("cta513305", UtilNumber.getBigDecimal(contable.get("5.1.3.3.05")));
			jrParameters.put("cta513306", UtilNumber.getBigDecimal(contable.get("5.1.3.3.06")));
			jrParameters.put("cta513307", UtilNumber.getBigDecimal(contable.get("5.1.3.3.07")));
			jrParameters.put("cta513308", UtilNumber.getBigDecimal(contable.get("5.1.3.3.08")));
			jrParameters.put("cta513312", UtilNumber.getBigDecimal(contable.get("5.1.3.3.12")));
			jrParameters.put("cta513309", UtilNumber.getBigDecimal(contable.get("5.1.3.3.09")));
			jrParameters.put("cta513310", UtilNumber.getBigDecimal(contable.get("5.1.3.3.10")));
			jrParameters.put("cta513311", UtilNumber.getBigDecimal(contable.get("5.1.3.3.11")));
			jrParameters.put("cta513401", UtilNumber.getBigDecimal(contable.get("5.1.3.4.01")));
			jrParameters.put("cta513405", UtilNumber.getBigDecimal(contable.get("5.1.3.4.05")));
			jrParameters.put("cta513407", UtilNumber.getBigDecimal(contable.get("5.1.3.4.07")));
			jrParameters.put("cta513501", UtilNumber.getBigDecimal(contable.get("5.1.3.5.01")));
			jrParameters.put("cta513502", UtilNumber.getBigDecimal(contable.get("5.1.3.5.02")));
			jrParameters.put("cta513503", UtilNumber.getBigDecimal(contable.get("5.1.3.5.03")));
			jrParameters.put("cta513507", UtilNumber.getBigDecimal(contable.get("5.1.3.5.07")));
			jrParameters.put("cta513508", UtilNumber.getBigDecimal(contable.get("5.1.3.5.08")));
			jrParameters.put("cta513702", UtilNumber.getBigDecimal(contable.get("5.1.3.7.02")));
			jrParameters.put("cta513703", UtilNumber.getBigDecimal(contable.get("5.1.3.7.03")));
			jrParameters.put("cta513705", UtilNumber.getBigDecimal(contable.get("5.1.3.7.05")));
			jrParameters.put("cta513706", UtilNumber.getBigDecimal(contable.get("5.1.3.7.06")));
			jrParameters.put("cta513709", UtilNumber.getBigDecimal(contable.get("5.1.3.7.09")));
			jrParameters.put("cta513803", UtilNumber.getBigDecimal(contable.get("5.1.3.8.03")));
			jrParameters.put("cta513804", UtilNumber.getBigDecimal(contable.get("5.1.3.8.04")));
			jrParameters.put("cta513805", UtilNumber.getBigDecimal(contable.get("5.1.3.8.05")));
			jrParameters.put("cta513901", UtilNumber.getBigDecimal(contable.get("5.1.3.9.01")));
			jrParameters.put("cta513902", UtilNumber.getBigDecimal(contable.get("5.1.3.9.02")));
			jrParameters.put("cta523101", UtilNumber.getBigDecimal(contable.get("5.2.3.1.01")));
			jrParameters.put("cta5241", UtilNumber.getBigDecimal(contable.get("5.2.4.1")));
			jrParameters.put("cta124103", UtilNumber.getBigDecimal(contable.get("1.2.4.1.03")));
			jrParameters.put("cta124101", UtilNumber.getBigDecimal(contable.get("1.2.4.1.01")));
			jrParameters.put("cta124105", UtilNumber.getBigDecimal(contable.get("1.2.4.1.05")));
			jrParameters.put("cta124107", UtilNumber.getBigDecimal(contable.get("1.2.4.1.07")));
			jrParameters.put("cta124605", UtilNumber.getBigDecimal(contable.get("1.2.4.6.05")));
			jrParameters.put("cta123501", UtilNumber.getBigDecimal(contable.get("1.2.3.5.01")));
			
			//TotalCuentas
			jrParameters.put("TotalCta", totalContable);
			List<Map<String, Object>> plainList = FastList.<Map<String, Object>>newInstance();
			
			FastMap<String, Object> reportLine = FastMap.<String, Object>newInstance();
			
			reportLine.put("dummie", "dummie");
			plainList.add(reportLine);
			
			request.setAttribute("jrParameters", jrParameters);
			request.setAttribute("jrDataSource", new JRMapCollectionDataSource(plainList));
			
		} catch (Exception e) {
			e.printStackTrace();
			Debug.log(e.getMessage());
			
			UtilMessage.addError(request, UtilMessage.expandLabel("ErrorMessage_noSePudoCrearElReporte", locale));
		}
		
		return reportType;

	}
}
