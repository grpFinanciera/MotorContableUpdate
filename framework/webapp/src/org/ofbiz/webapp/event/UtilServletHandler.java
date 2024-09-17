package org.ofbiz.webapp.event;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;

public class UtilServletHandler {

    public static final String SET_ORGANIZATION_FORM = "selectOrganizationForm";
    public static final String SET_FACILITY_FORM = "selectFacilityForm";
    public static final String OPTION_DEF_ORGANIZATION = "organizationPartyId";
    public static final String OPTION_DEF_FACILITY = "facilityId";
    public static final String OPTION_DEF_CICLO = "cicloId";
    
    public static final String module = UtilServletHandler.class.getName();
	
    /**
     * obtiene el ciclo por defecto del usuario
     * @param request a <code>HttpServletRequest</code> value
     * @return the organizationPartyId from the session, or <code>null</code> if not set
     */
    public static String getCicloId(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session == null) {
            return null;
        }

        Boolean cicloContextSet = (Boolean) session.getAttribute("cicloContextSet");
        if (cicloContextSet == null) {
            checkDefaultCiclo(request);
        }

        String cicloId = (String) session.getAttribute("cicloId");
        if (cicloId == null || cicloId.isEmpty()) {
            return null;
        }

        return cicloId;
    }
    
    /**
     * 
     * @param request
     */
    public static void checkDefaultCiclo(HttpServletRequest request) {

        HttpSession session = request.getSession();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
        TimeZone timeZone = UtilHttp.getTimeZone(request);
        Locale locale = UtilHttp.getLocale(request);
        
        if (userLogin == null) {
            return;
        }

        String cicloId = null;

        try {

            cicloId = getUserLoginViewPreference(request, "opentaps", SET_ORGANIZATION_FORM, OPTION_DEF_CICLO);
            if (UtilValidate.isEmpty(cicloId)) {
                return;
            }
            
            GenericValue CustomTimePeriod = EntityUtil.getFirst(delegator.findByAndCache("CustomTimePeriod", UtilMisc.toMap("periodName", cicloId, "periodTypeId","FISCAL_YEAR","isClosed","N")));
            if(UtilValidate.isNotEmpty(CustomTimePeriod)){
        		cicloId = String.valueOf(UtilDateTime.getYear(UtilDateTime.getTimestamp(CustomTimePeriod.getDate("fromDate").getTime()), timeZone, locale));
        	} else {
        		return;
        	}

            session.setAttribute("cicloId", cicloId);
            session.setAttribute("cicloContextSet", Boolean.TRUE);

        } catch (GenericEntityException e) {
            Debug.logError(e, "Error while retrieve default organization", module);
            return;
        }
    }
    
    /**
     * Gets a <code>UserLoginViewPreference</code> value from the request.
     * @param request a <code>HttpServletRequest</code> value
     * @param applicationName the application to get the preference for
     * @param screenName the screen to get the preference for
     * @param option the option to get the value for
     * @return the option value, or <code>null</code> if not found
     * @throws GenericEntityException if an error occurs
     */
    public static String getUserLoginViewPreference(HttpServletRequest request, String applicationName, String screenName, String option) throws GenericEntityException {
        GenericValue userLogin = getUserLogin(request);
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String,Object> mapAnd = new HashMap<String, Object>();
        mapAnd.put("userLoginId", userLogin.get("userLoginId"));
        mapAnd.put("applicationName", applicationName);
        mapAnd.put("screenName", screenName);
        mapAnd.put("preferenceName", option);
        GenericValue pref = delegator.findByPrimaryKeyCache("UserLoginViewPreference",mapAnd);
        if (pref == null) {
            return null;
        }
        return pref.getString("preferenceValue");
    }
    
    /**
     * Gets the UserLogin <code>GenericValue</code> from the request.
     * @param request a <code>HttpServletRequest</code> value
     * @return a <code>GenericValue</code> value
     */
    public static GenericValue getUserLogin(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return (GenericValue) session.getAttribute("userLogin");
    }
	
}
