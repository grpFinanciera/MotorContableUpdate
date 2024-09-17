package org.opentaps.common.autocomplete;

import static org.opentaps.common.autocomplete.UtilAutoComplete.makeSelectionJSONResponse;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastList;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.opentaps.common.util.UtilCommon;

public final class AutoCompleteAB {
	private AutoCompleteAB() {}
	
	private static final String MODULE = AutoCompleteAB.class.getName();
	
	public static final List<String> FIELDS_CUENTA_BANCO = Arrays.asList("partyId", "cuentaBancariaId", "nombreCuenta", "numeroCuenta", "tipoCuentaBancariaId", "usoCuentaBancariaId");
    public static final List<String> ORDER_BY_CUENTA_BANCO = Arrays.asList("nombreCuenta", "numeroCuenta");
	public static final List<String> FIELDS_ENUMERATION = Arrays.asList("enumId", "sequenceId", "description");
    public static final List<String> ORDER_BY_ENUMERATION = Arrays.asList("description", "enumCode");
    public static final List<String> FIELDS_ZONA_GEO = Arrays.asList("geoId", "geoName");
    public static final List<String> ORDER_BY_ZONA_GEO = Arrays.asList("geoName", "geoId");
    static Delegator delegator;
    
    public static String getZonaGeografica(HttpServletRequest request, HttpServletResponse response) {
		
		GenericValue userLogin = UtilCommon.getUserLogin(request);
        if (userLogin == null) {
            Debug.logError("Failed to retrieve the login user from the session.", MODULE);
            return "error";
        }
        
        String entityName = "Geo";

        Delegator delegator = (Delegator) request.getAttribute("delegator");

        String keyword = UtilCommon.getUTF8Parameter(request, "keyword");
        if (keyword == null) {
            Debug.log("Ignored the empty keyword string.", MODULE);
            return "success";
        }
        keyword = keyword.trim();

        List<GenericValue> zonaGeo = FastList.newInstance();
        if (keyword.length() > 0) {
            try {
            	
            	EntityCondition condition = EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("geoName"), EntityOperator.LIKE, "%" + keyword.toUpperCase() + "%");
                
            	
                zonaGeo = delegator.findByCondition(entityName, condition, FIELDS_ZONA_GEO, ORDER_BY_ZONA_GEO);
                
            } catch (GenericEntityException e) {
                Debug.logError(e, MODULE);
                return "error";
            }
        }


        // write the JSON data to the response stream
        return makeSelectionJSONResponse(response, zonaGeo, "geoId", new GeoBuilder(), UtilCommon.getLocale(request));
    }

	public static class GeoBuilder implements SelectionBuilder {
		public Map<String, Object> buildRow(Object element) {
	        GenericValue zonaGeo = (GenericValue) element;
	        String geoId = zonaGeo == null ? null : zonaGeo.getString("geoId");
	        String geoName = zonaGeo == null ? null : zonaGeo.getString("geoName");
	
	        return UtilMisc.<String, Object>toMap("name", geoName, "geoId", geoId);
    }
}
    
	/**
	 * Obtiene las cuentas bancarias que el usuario tiene permiso de utilizar
	 * @param request
	 * @param response
	 * @return
	 */
	public static String getCuentaBancoUsuario(HttpServletRequest request, HttpServletResponse response) {
		
		GenericValue userLogin = UtilCommon.getUserLogin(request);
        if (userLogin == null) {
            Debug.logError("Failed to retrieve the login user from the session.", MODULE);
            return "error";
        }
        
        String entityName = "AsignacionCuentaUsuario";

        delegator = (Delegator) request.getAttribute("delegator");

        String keyword = UtilCommon.getUTF8Parameter(request, "keyword");
        if (keyword == null) {
            Debug.log("Ignored the empty keyword string.", MODULE);
            return "success";
        }
        keyword = keyword.trim();

        List<GenericValue> asignaciones = FastList.newInstance();
        if (keyword.length() > 0) {
            try {
            	
            	EntityCondition condition = EntityCondition.makeCondition(EntityOperator.AND,
                        EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, userLogin.getString("partyId")),
                        EntityCondition.makeCondition("habilitada", EntityOperator.EQUALS, "S"));
                
                EntityCondition orCondition = EntityCondition.makeCondition(EntityOperator.OR,
                        EntityCondition.makeCondition("cuentaBancariaId", EntityOperator.LIKE, "%" + keyword + "%"),
                        EntityCondition.makeCondition("numeroCuenta", EntityOperator.LIKE, "%" + keyword + "%"),
                        EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("nombreCuenta"), EntityOperator.LIKE, "%" + keyword.toUpperCase() + "%")
                    );
                
                EntityCondition condicionAnd = EntityCondition.makeCondition(EntityOperator.AND, condition, orCondition);
            	
                asignaciones = delegator.findByCondition(entityName, condicionAnd, FIELDS_CUENTA_BANCO, ORDER_BY_CUENTA_BANCO);
                
            } catch (GenericEntityException e) {
                Debug.logError(e, MODULE);
                return "error";
            }
        }


        // write the JSON data to the response stream
        return makeSelectionJSONResponse(response, asignaciones, "cuentaBancariaId", new AsignacionCuentaUsuarioBuilder(), UtilCommon.getLocale(request));
    }
	
	private static String nombreNumeroCuenta(GenericValue asignacion){
		String resultado = "";
		try 
		{	String nombreUsoCuenta = "";
			
	        if(asignacion.getString("usoCuentaBancariaId") != null && !asignacion.get("usoCuentaBancariaId").equals(""))
			{	GenericValue usoCuentaBancaria;			
				usoCuentaBancaria = delegator.findByPrimaryKey("UsoCuentaBancaria", UtilMisc.toMap("usoCuentaId", asignacion.getString("usoCuentaBancariaId")));
				nombreUsoCuenta =  usoCuentaBancaria.getString("nombre");
			} 						
	        if(nombreUsoCuenta.equals(""))
	        {	resultado = asignacion.getString("nombreCuenta") + " - " + asignacion.getString("numeroCuenta");         	
	        }
	        else
	        {	resultado = asignacion.getString("nombreCuenta") + " - " + asignacion.getString("numeroCuenta") + " - " + nombreUsoCuenta;        	
	        }			
		}		
		catch (GenericEntityException e) 
		{	e.printStackTrace();
		}
		return resultado;
	}
	
	public static class AsignacionCuentaUsuarioBuilder implements SelectionBuilder {		
        public Map<String, Object> buildRow(Object element) {
            GenericValue asignacion = (GenericValue) element;            

            // cuentaBancariaId, nombre cuenta + numero cuenta                        		
            
            String cuentaBancariaId = asignacion == null ? null : asignacion.getString("cuentaBancariaId");
            
            String nombreNumeroCuenta = nombreNumeroCuenta(asignacion);

            return UtilMisc.<String, Object>toMap("name", nombreNumeroCuenta, "cuentaBancariaId", cuentaBancariaId);
        }
    }
	
	/**
	 * Consulta para llenar autocomplete de Clasificacion (Enumeration)
	 * @param request
	 * @param response
	 * @return
	 */
	public static String getClasificacionEnumeration(HttpServletRequest request, HttpServletResponse response){
		
		GenericValue userLogin = UtilCommon.getUserLogin(request);
        if (userLogin == null) {
            Debug.logError("Failed to retrieve the login user from the session.", MODULE);
            return "error";
        }
		
        String keyword = UtilCommon.getUTF8Parameter(request, "keyword");
        if (keyword == null) {
            Debug.log("Ignored the empty keyword string.", MODULE);
            return "success";
        }
        
        String filtro = UtilCommon.getUTF8Parameter(request, "filtro");
        
        keyword = keyword.trim();
        
        Timestamp fechaActual = UtilDateTime.nowTimestamp();
        
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        
        List<GenericValue> clasificaciones = FastList.newInstance();
        if (keyword.length() > 0) {
            try {
            	
            	EntityCondition condition = EntityCondition.makeCondition(EntityOperator.AND,
                        EntityCondition.makeCondition("fechaInicio", EntityOperator.LESS_THAN_EQUAL_TO, fechaActual),
                        EntityCondition.makeCondition("fechaFin", EntityOperator.GREATER_THAN_EQUAL_TO, fechaActual));
            	
                if(UtilValidate.isNotEmpty(filtro)){ //Se agrega el Tipo de clasificacion (Enumeration)
                	condition = EntityCondition.makeCondition(EntityOperator.AND,condition,
                	EntityCondition.makeCondition(EntityOperator.AND,EntityCondition.makeCondition("enumTypeId",EntityOperator.EQUALS,filtro)));
                }
                
                EntityCondition orCondition = EntityCondition.makeCondition(EntityOperator.OR,
                        EntityCondition.makeCondition("sequenceId", EntityOperator.LIKE, "%" + keyword + "%"),
                        EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("enumCode"), EntityOperator.LIKE, "%" + keyword.toUpperCase() + "%"),
                        EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("description"), EntityOperator.LIKE, "%" + keyword.toUpperCase() + "%")
                    );
                
                EntityCondition condicionAnd = EntityCondition.makeCondition(EntityOperator.AND, condition, orCondition);
            	
                clasificaciones = delegator.findByCondition("Enumeration", condicionAnd, FIELDS_ENUMERATION, ORDER_BY_ENUMERATION);
                
                
                
            } catch (GenericEntityException e) {
                Debug.logError(e, MODULE);
                return "error";
            }
        }
        
        return makeSelectionJSONResponse(response, clasificaciones, "enumId", new ClasificacionBuilder(), UtilCommon.getLocale(request));
	}
	
	private static class ClasificacionBuilder implements SelectionBuilder{
		public Map<String, Object> buildRow(Object element) {
			GenericValue clasificacion = (GenericValue) element;
			
			String enumerationId = new String();
			String nombre = new String();
			
			if(UtilValidate.isNotEmpty(clasificacion)){
				enumerationId = clasificacion.getString("enumId");
				nombre = clasificacion.getString("sequenceId")+" - "+clasificacion.getString("description");
			}
			
			return UtilMisc.<String, Object>toMap("name",nombre,"enumId",enumerationId);
			
		}
	}
	
}
