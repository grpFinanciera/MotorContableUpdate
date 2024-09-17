/*
 * Copyright (c) Open Source Strategies, Inc.
 *
 * Opentaps is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Opentaps is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Opentaps.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.opensourcestrategies.financials.transactions;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.opentaps.common.builder.ListBuilderException;
import org.opentaps.common.party.PartyHelper;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.foundation.action.ActionContext;
import org.opentaps.foundation.repository.RepositoryException;


/**
 * BuscarPolizas - Informacion de Polizas Contables
 */
public class BuscarPolizas {

    //private static final String MODULE = BuscarPolizas.class.getName();
	
    
    /**
     * Metodo de busqueda de poliza , regresa un page builder para paginar el resultado
     * @param dctx
     * @param context
     * @return
     * @throws RepositoryException 
     * @throws ListBuilderException 
     * @throws ParseException 
     */
    public static void buscaPolizas(Map<String, Object> context) throws RepositoryException, ListBuilderException, ParseException {
    	
    	final ActionContext ac = new ActionContext(context);
    	final TimeZone timeZone = ac.getTimeZone();
    	Locale locale = ac.getLocale();
    	Delegator delegator = ac.getDelegator();
    	
        String poliza = ac.getParameter("poliza");
    	String tipoPolizaId = ac.getParameter("tipoPolizaId");
    	String areaId = ac.getParameter("areaId");
    	String usuario = ac.getParameter("usuario");
    	String fechaContableDesde = ac.getParameter("fechaContableDesde");
    	String fechaContableHasta = ac.getParameter("fechaContableHasta");
    	String montoDesde = ac.getParameter("montoDesde");
    	String montoHasta = ac.getParameter("montoHasta");    
    	
    	
    	try
    	{
    		
    		String dateFormat = UtilDateTime.getDateFormat(locale);
    		
    		Timestamp fechaInicialPeriodo = null;

    		if(UtilValidate.isNotEmpty(fechaContableDesde)){
    			fechaInicialPeriodo = UtilDateTime.getDayStart(UtilDateTime.stringToTimeStamp(fechaContableDesde, dateFormat, timeZone, locale), timeZone, locale);
    		}else{
    			fechaInicialPeriodo = UtilDateTime.getMonthStart(UtilDateTime.nowTimestamp());
    		}

    		Timestamp fechaFinalPeriodo = null;

    		if(UtilValidate.isNotEmpty(fechaContableHasta)){
    			fechaFinalPeriodo = UtilDateTime.getDayEnd(UtilDateTime.stringToTimeStamp(fechaContableHasta, dateFormat, timeZone, locale), timeZone, locale);
    		}else{
    			fechaFinalPeriodo = UtilDateTime.nowTimestamp();
    		}
	    	
	    	String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
	    	final String baseCurrencyUomId = UtilCommon.getOrgBaseCurrency(organizationPartyId, ac.getDelegator());
	    	
	    	List<EntityCondition> searchConditions = new FastList<EntityCondition>();
	        if (UtilValidate.isNotEmpty(poliza) && poliza != null) {
	            searchConditions.add(EntityCondition.makeCondition("poliza", EntityOperator.LIKE, "%"+poliza+"%"));
	        }         
	        if (UtilValidate.isNotEmpty(tipoPolizaId) && tipoPolizaId != null) {
	            searchConditions.add(EntityCondition.makeCondition("tipoPolizaId", EntityOperator.EQUALS, tipoPolizaId));        
	        }
	        if (UtilValidate.isNotEmpty(areaId) && areaId != null) {
	        	searchConditions.add(EntityCondition.makeCondition("areaId", EntityOperator.LIKE, "%"+areaId+"%"));
	        }
	        if (UtilValidate.isNotEmpty(usuario) && usuario != null) {
	        	searchConditions.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, usuario));
	        }
	        if (UtilValidate.isNotEmpty(fechaInicialPeriodo)) {
	        	searchConditions.add(EntityCondition.makeCondition("postedDate", EntityOperator.GREATER_THAN_EQUAL_TO, fechaInicialPeriodo));
	        }
	        if (UtilValidate.isNotEmpty(fechaFinalPeriodo)) {
	        	searchConditions.add(EntityCondition.makeCondition("postedDate", EntityOperator.LESS_THAN_EQUAL_TO, fechaFinalPeriodo));
	        }
	        if (UtilValidate.isNotEmpty(montoDesde) && montoDesde != null) {
	        	searchConditions.add(EntityCondition.makeCondition("postedAmount", EntityOperator.GREATER_THAN_EQUAL_TO, new BigDecimal(montoDesde)));
	        }
	        if (UtilValidate.isNotEmpty(montoHasta) && montoHasta != null) {
	        	searchConditions.add(EntityCondition.makeCondition("postedAmount", EntityOperator.LESS_THAN_EQUAL_TO, new BigDecimal(montoHasta)));
	        }
	        
	        EntityCondition condiciones = EntityCondition.makeCondition(searchConditions, EntityOperator.AND);
	        Debug.log("condiciones: " + condiciones );
	        
	        List<String> fieldsToSelect = UtilMisc.toList("acctgTransId","poliza","descripcionPoliza","descripcionEvento","description",
	        		"descripcionOrg","postedDate","nombreArea","createdByUserLogin","postedAmount");
	        
	        List<GenericValue> listPolizas = delegator.findByCondition("AcctgTransListaDetallePoliza", condiciones, fieldsToSelect, UtilMisc.toList("poliza"));
	        
	        List<Map<String,Object>> listPolizasBuilder = FastList.newInstance();
	        Map<String,Object> mapaPoliza = FastMap.newInstance();
	        for (GenericValue Poliza : listPolizas) {
	        	mapaPoliza.putAll(Poliza.getAllFields());
	        	mapaPoliza.put("currencyUomId", baseCurrencyUomId);
	        	listPolizasBuilder.add(mapaPoliza);
	        	mapaPoliza = FastMap.newInstance();
			}
	        
	        ac.put("acctgTransListBuilder", listPolizasBuilder);
    	}
    	catch (ParseException e) {		
			e.printStackTrace();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Metodo para buscar el detalle de auxiliares en los entries
     * @param dctx
     * @param context
     * @return
     * @throws RepositoryException 
     * @throws ListBuilderException 
     * @throws ParseException 
     */
    public static void buscaDetalleEntries(Map<String, Object> context) throws RepositoryException, ListBuilderException, ParseException {
    	
    	final ActionContext ac = new ActionContext(context);
    	final Delegator delegator = ac.getDelegator();
    	
        String acctgTransId = ac.getParameter("acctgTransId");
        Debug.log("Entra a buscar el detalle de la poliza.- " + acctgTransId);
		
        if(UtilValidate.isEmpty(acctgTransId)){
        	return;
        }
        
		EntityConditionList<EntityExpr> condicionOR = EntityCondition.makeCondition(UtilMisc.toList(
	                EntityCondition.makeCondition("theirPartyId", EntityOperator.NOT_EQUAL, null),
	                EntityCondition.makeCondition("cuentaBancariaId", EntityOperator.NOT_EQUAL, null),
	                EntityCondition.makeCondition("productId", EntityOperator.NOT_EQUAL, null)), EntityOperator.OR);
		
        EntityConditionList<EntityCondition> condiciones = EntityCondition.makeCondition(UtilMisc.toList(
        		condicionOR,
        		EntityCondition.makeCondition("acctgTransId", EntityOperator.EQUALS, acctgTransId)),
                EntityOperator.AND);
		
		Debug.log("condiciones: " + condiciones );
		
		List<String> orderBy = UtilMisc.toList("debitCreditFlag");
		List<Map<String,Object>> listAuxiliarBuilder = FastList.newInstance();
		try {
			List<GenericValue> listAcctgTransEntry = delegator.findByCondition("AuxiliarPoliza", condiciones, null,orderBy);
			
			String productId = new String();
			String theirPartyId = new String();
			String cuentaBancariaId = new String();
			String debitCreditFlag = new String();
	        Map<String,Object> mapaAuxiliar = FastMap.newInstance();
	        GenericValue partyAux = null;
	        for (GenericValue AuxiliarPoliza : listAcctgTransEntry) {
	        	productId = AuxiliarPoliza.getString("productId");
	        	theirPartyId = AuxiliarPoliza.getString("theirPartyId");
	        	cuentaBancariaId = AuxiliarPoliza.getString("cuentaBancariaId");
	        	debitCreditFlag = AuxiliarPoliza.getString("debitCreditFlag");
	        	
	        	if(UtilValidate.isNotEmpty(theirPartyId)){
	        		if(UtilValidate.isNotEmpty(AuxiliarPoliza.getString("groupName"))){
	        			partyAux = delegator.makeValue("PartyGroup");
	        		} else {
	        			partyAux = delegator.makeValue("Person");
	        		}
	        		partyAux.setAllFields(AuxiliarPoliza, false, null, null);
	        		
	        		
	        		mapaAuxiliar.put("identificadorAuxiliar", theirPartyId);
	        		mapaAuxiliar.put("tipoAuxiliar", AuxiliarPoliza.getString("decripcionTipoParty"));
	        		mapaAuxiliar.put("nombreAuxiliar", PartyHelper.getCrmsfaPartyName(partyAux));
	        		
	        	} else if(UtilValidate.isNotEmpty(cuentaBancariaId)){
	        		
	        		mapaAuxiliar.put("identificadorAuxiliar", cuentaBancariaId);
	        		mapaAuxiliar.put("tipoAuxiliar", "Cuenta Bancaria");
	        		mapaAuxiliar.put("nombreAuxiliar", AuxiliarPoliza.getString("nombreCuenta"));
	        		
	        	} else if(UtilValidate.isNotEmpty(productId)){
	        		
	        		mapaAuxiliar.put("identificadorAuxiliar", productId);
	        		mapaAuxiliar.put("tipoAuxiliar", AuxiliarPoliza.getString("decripcionTipoProducto"));
	        		mapaAuxiliar.put("nombreAuxiliar", AuxiliarPoliza.getString("productName"));
	        		
	        	} else {
	        		continue;
	        	}
	        	
	        	mapaAuxiliar.put("glAccountId", AuxiliarPoliza.getString("glAccountId"));
	        	
            	if(debitCreditFlag.equals("D")){
            		mapaAuxiliar.put("debitCreditFlag", "Cargo");
            	}else{
            		mapaAuxiliar.put("debitCreditFlag", "Abono");
            	}
            	
            	mapaAuxiliar.put("amount", AuxiliarPoliza.getBigDecimal("amount"));
            	mapaAuxiliar.put("currencyUomId", AuxiliarPoliza.getString("currencyUomId"));
	        	
	        	listAuxiliarBuilder.add(mapaAuxiliar);
	        	mapaAuxiliar = FastMap.newInstance();
			}
			
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		ac.put("acctgTransEntryAuxListBuilder", listAuxiliarBuilder);
		
    }

}
