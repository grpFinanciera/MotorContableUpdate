/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
/* This file has been modified by Open Source Strategies, Inc. */

package com.absoluciones.obras.contact;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;

/**
 * Worker methods for Contact Mechanisms
 */
public class ContactMechWorker {

    public static final String module = ContactMechWorker.class.getName();

    public static List<Map<String, Object>> geObraContactMechValueMaps(Delegator delegator, String obraId) {
       return getObraContactMechValueMaps(delegator, obraId, null);
    }

    public static List<Map<String, Object>> getObraContactMechValueMaps(Delegator delegator, String obraId, String contactMechTypeId) {
        List<Map<String, Object>> obraContactMechValueMaps = FastList.newInstance();

        List<GenericValue> allObraContactMechs = null;

        try {
            List<GenericValue> tempCol = delegator.findByAnd("ObraContactMech", UtilMisc.toMap("obraId", obraId));
            if (contactMechTypeId != null) {
                List<GenericValue> tempColTemp = FastList.newInstance();
                for (GenericValue obraContactMech: tempCol) {
                    GenericValue contactMech = delegator.getRelatedOne("ContactMech", obraContactMech);
                    if (contactMech != null && contactMechTypeId.equals(contactMech.getString("contactMechTypeId"))) {
                        tempColTemp.add(obraContactMech);
                    }

                }
                tempCol = tempColTemp;
            }
            allObraContactMechs = tempCol;
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
        }

        if (allObraContactMechs == null) return obraContactMechValueMaps;

        for (GenericValue obraContactMech: allObraContactMechs) {
            GenericValue contactMech = null;

            try {
                contactMech = obraContactMech.getRelatedOne("ContactMech");
            } catch (GenericEntityException e) {
                Debug.logWarning(e, module);
            }
            if (contactMech != null) {
                Map<String, Object> obraContactMechValueMap = FastMap.newInstance();

                obraContactMechValueMaps.add(obraContactMechValueMap);
                obraContactMechValueMap.put("contactMech", contactMech);
                obraContactMechValueMap.put("obraContactMech", obraContactMech);

                try {
                    obraContactMechValueMap.put("contactMechType", contactMech.getRelatedOneCache("ContactMechType"));
                } catch (GenericEntityException e) {
                    Debug.logWarning(e, module);
                }

                try {
                    List<GenericValue> obraContactMechPurposes = obraContactMech.getRelated("ObraContactMechPurpose");

                    obraContactMechValueMap.put("obraContactMechPurposes", obraContactMechPurposes);
                } catch (GenericEntityException e) {
                    Debug.logWarning(e, module);
                }

                try {
                    if ("POSTAL_ADDRESS".equals(contactMech.getString("contactMechTypeId"))) {
                        obraContactMechValueMap.put("postalAddress", contactMech.getRelatedOne("PostalAddress"));
                    } else if ("TELECOM_NUMBER".equals(contactMech.getString("contactMechTypeId"))) {
                        obraContactMechValueMap.put("telecomNumber", contactMech.getRelatedOne("TelecomNumber"));
                    }
                } catch (GenericEntityException e) {
                    Debug.logWarning(e, module);
                }
            }
        }

        return obraContactMechValueMaps;
    }

    public static void getObraContactMechAndRelated(ServletRequest request, String obraId, Map<String, Object> target) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");

        boolean tryEntity = true;
        if (request.getAttribute("_ERROR_MESSAGE_") != null) tryEntity = false;
        if ("true".equals(request.getParameter("tryEntity"))) tryEntity = true;

        String donePage = request.getParameter("DONE_PAGE");
        if (donePage == null) donePage = (String) request.getAttribute("DONE_PAGE");
        if (donePage == null || donePage.length() <= 0) donePage = "viewprofile";
        target.put("donePage", donePage);

        String contactMechTypeId = request.getParameter("preContactMechTypeId");

        if (contactMechTypeId == null) contactMechTypeId = (String) request.getAttribute("preContactMechTypeId");
        if (contactMechTypeId != null)
            tryEntity = false;

        String contactMechId = request.getParameter("contactMechId");

        if (request.getAttribute("contactMechId") != null)
            contactMechId = (String) request.getAttribute("contactMechId");

        GenericValue contactMech = null;

        if (contactMechId != null) {
            target.put("contactMechId", contactMechId);

            // try to find a PartyContactMech with a valid date range
            List<GenericValue> obraContactMechs = null;

            try {
                obraContactMechs = EntityUtil.filterByDate(delegator.findByAnd("ObraContactMech", UtilMisc.toMap("orderId", obraId, "contactMechId", contactMechId)), true);
            } catch (GenericEntityException e) {
                Debug.logWarning(e, module);
            }

            GenericValue obraContactMech = EntityUtil.getFirst(obraContactMechs);

            if (obraContactMech != null) {
                target.put("obraContactMech", obraContactMech);

                Collection<GenericValue> obraContactMechPurposes = null;

                try {
                    obraContactMechPurposes = EntityUtil.filterByDate(obraContactMech.getRelated("ObraContactMechPurpose"), true);
                } catch (GenericEntityException e) {
                    Debug.logWarning(e, module);
                }
                if (UtilValidate.isNotEmpty(obraContactMechPurposes))
                    target.put("obraContactMechPurposes", obraContactMechPurposes);
            }

            try {
                contactMech = delegator.findByPrimaryKey("ContactMech", UtilMisc.toMap("contactMechId", contactMechId));
            } catch (GenericEntityException e) {
                Debug.logWarning(e, module);
            }

            if (contactMech != null) {
                target.put("contactMech", contactMech);
                contactMechTypeId = contactMech.getString("contactMechTypeId");
            }
        }

        if (contactMechTypeId != null) {
            target.put("contactMechTypeId", contactMechTypeId);

            try {
                GenericValue contactMechType = delegator.findByPrimaryKey("ContactMechType", UtilMisc.toMap("contactMechTypeId", contactMechTypeId));

                if (contactMechType != null)
                    target.put("contactMechType", contactMechType);
            } catch (GenericEntityException e) {
                Debug.logWarning(e, module);
            }

            Collection<GenericValue> purposeTypes = FastList.newInstance();
            Iterator<GenericValue> typePurposes = null;

            try {
                typePurposes = UtilMisc.toIterator(delegator.findByAnd("ContactMechTypePurpose", UtilMisc.toMap("contactMechTypeId", contactMechTypeId)));
            } catch (GenericEntityException e) {
                Debug.logWarning(e, module);
            }
            while (typePurposes != null && typePurposes.hasNext()) {
                GenericValue contactMechTypePurpose = typePurposes.next();
                GenericValue contactMechPurposeType = null;

                try {
                    contactMechPurposeType = contactMechTypePurpose.getRelatedOne("ContactMechPurposeType");
                } catch (GenericEntityException e) {
                    Debug.logWarning(e, module);
                }
                if (contactMechPurposeType != null) {
                    purposeTypes.add(contactMechPurposeType);
                }
            }
            if (purposeTypes.size() > 0)
                target.put("purposeTypes", purposeTypes);
        }

        String requestName;

        if (contactMech == null) {
            // create
            if ("POSTAL_ADDRESS".equals(contactMechTypeId)) {
                if (request.getParameter("contactMechPurposeTypeId") != null || request.getAttribute("contactMechPurposeTypeId") != null) {
                    requestName = "createPostalAddressAndPurpose";
                } else {
                    requestName = "createPostalAddress";
                }
            } else if ("TELECOM_NUMBER".equals(contactMechTypeId)) {
                requestName = "createTelecomNumber";
            } else if ("EMAIL_ADDRESS".equals(contactMechTypeId)) {
                requestName = "createEmailAddress";
            } else {
                requestName = "createContactMech";
            }
        } else {
            // update
            if ("POSTAL_ADDRESS".equals(contactMechTypeId)) {
                requestName = "updatePostalAddress";
            } else if ("TELECOM_NUMBER".equals(contactMechTypeId)) {
                requestName = "updateTelecomNumber";
            } else if ("EMAIL_ADDRESS".equals(contactMechTypeId)) {
                requestName = "updateEmailAddress";
            } else {
                requestName = "updateContactMech";
            }
        }
        target.put("requestName", requestName);

        if ("POSTAL_ADDRESS".equals(contactMechTypeId)) {
            GenericValue postalAddress = null;

            try {
                if (contactMech != null) postalAddress = contactMech.getRelatedOne("PostalAddress");
            } catch (GenericEntityException e) {
                Debug.logWarning(e, module);
            }
            if (postalAddress != null) target.put("postalAddress", postalAddress);
        } else if ("TELECOM_NUMBER".equals(contactMechTypeId)) {
            GenericValue telecomNumber = null;

            try {
                if (contactMech != null) telecomNumber = contactMech.getRelatedOne("TelecomNumber");
            } catch (GenericEntityException e) {
                Debug.logWarning(e, module);
            }
            if (telecomNumber != null) target.put("telecomNumber", telecomNumber);
        }

        if ("true".equals(request.getParameter("useValues"))) tryEntity = true;
        target.put("tryEntity", Boolean.valueOf(tryEntity));

        try {
            Collection<GenericValue> contactMechTypes = delegator.findList("ContactMechType", null, null, null, null, true);

            if (contactMechTypes != null) {
                target.put("contactMechTypes", contactMechTypes);
            }
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
        }
    }


    public static List<Map<String, Object>> getObraPostalAddresses(Delegator delegator, String obraId, String curContactMechId) {
        List<Map<String, Object>> postalAddressInfos = FastList.newInstance();

        List<GenericValue> allObraContactMechs = null;

        try {
        	EntityCondition condicion = EntityCondition.makeCondition(EntityOperator.AND,
        								EntityCondition.makeCondition("obraId", EntityOperator.EQUALS, obraId));
            allObraContactMechs = delegator.findByCondition("ObraContactMech", condicion, null, null);
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
        }

        if (allObraContactMechs == null) return postalAddressInfos;

        for (GenericValue obraContactMech: allObraContactMechs) {
            GenericValue contactMech = null;

            try {
                contactMech = obraContactMech.getRelatedOne("ContactMech");
            } catch (GenericEntityException e) {
                Debug.logWarning(e, module);
            }
            if (contactMech != null && "POSTAL_ADDRESS".equals(contactMech.getString("contactMechTypeId")) && !contactMech.getString("contactMechId").equals(curContactMechId)) {
                Map<String, Object> postalAddressInfo = FastMap.newInstance();

                postalAddressInfos.add(postalAddressInfo);
                postalAddressInfo.put("contactMech", contactMech);
                postalAddressInfo.put("obraContactMech", obraContactMech);

                try {
                    GenericValue postalAddress = contactMech.getRelatedOne("PostalAddress");
                    postalAddressInfo.put("postalAddress", postalAddress);
                } catch (GenericEntityException e) {
                    Debug.logWarning(e, module);
                }

                try {
                    List<GenericValue> obraContactMechPurposes = delegator.findByAnd(
                    			"ObraContactMechPurpose", UtilMisc.toMap("obraId",obraId));
                    postalAddressInfo.put("obraContactMechPurposes", obraContactMechPurposes);
                } catch (GenericEntityException e) {
                    Debug.logWarning(e, module);
                }
            }
        }

        return postalAddressInfos;
    }

    public static Map<String, Object> getCurrentPostalAddress(ServletRequest request, String obraId, String curContactMechId) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, Object> results = FastMap.newInstance();

        if (curContactMechId != null) {
            List<GenericValue> partyContactMechs = null;

            try {
                partyContactMechs = delegator.findByAnd("ObraContactMech", UtilMisc.toMap("obraId", obraId, "contactMechId", curContactMechId));
            } catch (GenericEntityException e) {
                Debug.logWarning(e, module);
            }
            GenericValue curPartyContactMech = EntityUtil.getFirst(partyContactMechs);
            results.put("curObraContactMech", curPartyContactMech);

            GenericValue curContactMech = null;
            if (curPartyContactMech != null) {
                try {
                    curContactMech = curPartyContactMech.getRelatedOne("ContactMech");
                } catch (GenericEntityException e) {
                    Debug.logWarning(e, module);
                }

                Collection<GenericValue> curPartyContactMechPurposes = null;
                try {
                    curPartyContactMechPurposes = EntityUtil.filterByDate(curPartyContactMech.getRelated("ObraContactMechPurpose"), true);
                } catch (GenericEntityException e) {
                    Debug.logWarning(e, module);
                }
                results.put("curObraContactMechPurposes", curPartyContactMechPurposes);
            }
            results.put("curContactMech", curContactMech);

            GenericValue curPostalAddress = null;
            if (curContactMech != null) {
                try {
                    curPostalAddress = curContactMech.getRelatedOne("PostalAddress");
                } catch (GenericEntityException e) {
                    Debug.logWarning(e, module);
                }
            }

            results.put("curPostalAddress", curPostalAddress);
        }
        return results;
    }

    public static boolean isUspsAddress(GenericValue postalAddress) {
        if (postalAddress == null) {
            // null postal address is not a USPS address
            return false;
        }
        if (!"PostalAddress".equals(postalAddress.getEntityName())) {
            // not a postal address not a USPS address
            return false;
        }

        // get and clean the address strings
        String addr1 = postalAddress.getString("address1");
        String addr2 = postalAddress.getString("address2");

        // get the matching string from general.properties
        String matcher = UtilProperties.getPropertyValue("general.properties", "usps.address.match");
        if (UtilValidate.isNotEmpty(matcher)) {
            if (addr1 != null && addr1.toLowerCase().matches(matcher)) {
                return true;
            }
            if (addr2 != null && addr2.toLowerCase().matches(matcher)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isCompanyAddress(GenericValue postalAddress, String companyPartyId) {
        if (postalAddress == null) {
            // null postal address is not an internal address
            return false;
        }
        if (!"PostalAddress".equals(postalAddress.getEntityName())) {
            // not a postal address not an internal address
            return false;
        }
        if (companyPartyId == null) {
            // no partyId not an internal address
            return false;
        }

        String state = postalAddress.getString("stateProvinceGeoId");
        String addr1 = postalAddress.getString("address1");
        String addr2 = postalAddress.getString("address2");
        if (state != null) {
            state = state.replaceAll("\\W", "").toLowerCase();
        } else {
            state = "";
        }
        if (addr1 != null) {
            addr1 = addr1.replaceAll("\\W", "").toLowerCase();
        } else {
            addr1 = "";
        }
        if (addr2 != null) {
            addr2 = addr2.replaceAll("\\W", "").toLowerCase();
        } else {
            addr2 = "";
        }

        // get all company addresses
        Delegator delegator = postalAddress.getDelegator();
        List<GenericValue> postalAddresses = FastList.newInstance();
        try {
            List<GenericValue> partyContactMechs = delegator.findByAnd("PartyContactMech", UtilMisc.toMap("partyId", companyPartyId));
            partyContactMechs = EntityUtil.filterByDate(partyContactMechs);
            if (partyContactMechs != null) {
                for (GenericValue pcm: partyContactMechs) {
                    GenericValue addr = pcm.getRelatedOne("PostalAddress");
                    if (addr != null) {
                        postalAddresses.add(addr);
                    }
                }
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, "Unable to get party postal addresses", module);
        }

        if (postalAddresses != null) {
            for (GenericValue addr: postalAddresses) {
                String thisAddr1 = addr.getString("address1");
                String thisAddr2 = addr.getString("address2");
                String thisState = addr.getString("stateProvinceGeoId");
                if (thisState != null) {
                    thisState = thisState.replaceAll("\\W", "").toLowerCase();
                } else {
                    thisState = "";
                }
                if (thisAddr1 != null) {
                    thisAddr1 = thisAddr1.replaceAll("\\W", "").toLowerCase();
                } else {
                    thisAddr1 = "";
                }
                if (thisAddr2 != null) {
                    thisAddr2 = thisAddr2.replaceAll("\\W", "").toLowerCase();
                } else {
                    thisAddr2 = "";
                }
                if (thisAddr1.equals(addr1) && thisAddr2.equals(addr2) && thisState.equals(state)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static String getContactMechAttribute(Delegator delegator, String contactMechId, String attrName) {
        GenericValue attr = null;
        try {
            attr = delegator.findByPrimaryKey("ContactMechAttribute", UtilMisc.toMap("contactMechId", contactMechId, "attrName", attrName));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        if (attr == null) {
            return null;
        } else {
            return attr.getString("attrValue");
        }
    }

    public static String getPostalAddressPostalCodeGeoId(GenericValue postalAddress, Delegator delegator) throws GenericEntityException {
        // if postalCodeGeoId not empty use that
        if (UtilValidate.isNotEmpty(postalAddress.getString("postalCodeGeoId"))) {
            return postalAddress.getString("postalCodeGeoId");
        }

        // no postalCodeGeoId, see if there is a Geo record matching the countryGeoId and postalCode fields
        if (UtilValidate.isNotEmpty(postalAddress.getString("countryGeoId")) && UtilValidate.isNotEmpty(postalAddress.getString("postalCode"))) {
            // first try the shortcut with the geoId convention for "{countryGeoId}-{postalCode}"
            GenericValue geo = delegator.findByPrimaryKeyCache("Geo", UtilMisc.toMap("geoId", postalAddress.getString("countryGeoId") + "-" + postalAddress.getString("postalCode")));
            if (geo != null) {
                // save the value to the database for quicker future reference
                postalAddress.set("postalCodeGeoId", geo.getString("geoId"));
                postalAddress.store();

                return geo.getString("geoId");
            }

            // no shortcut, try the longcut to see if there is something with a geoCode associated to the countryGeoId
            List<GenericValue> geoAssocAndGeoToList = delegator.findByAndCache("GeoAssocAndGeoTo",
                    UtilMisc.toMap("geoIdFrom", postalAddress.getString("countryGeoId"), "geoCode", postalAddress.getString("postalCode"), "geoAssocTypeId", "REGIONS"));
            GenericValue geoAssocAndGeoTo = EntityUtil.getFirst(geoAssocAndGeoToList);
            if (geoAssocAndGeoTo != null) {
                // save the value to the database for quicker future reference
                postalAddress.set("postalCodeGeoId", geoAssocAndGeoTo.getString("geoId"));
                postalAddress.store();

                return geoAssocAndGeoTo.getString("geoId");
            }
        }

        // nothing found, return null
        return null;
    }
}
