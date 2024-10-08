/*
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
 */

import org.ofbiz.base.util.*;
import org.ofbiz.entity.condition.*;
import org.opentaps.common.util.UtilCommon;
import org.ofbiz.entity.GenericValue;

//partyAcctgPrefAndGroupList
partyAcctgPrefAndGroupList = [];
/*partyAcctgPrefAndGroup = delegator.findList("PartyAcctgPrefAndGroup", null, null, null, null, false);
iter = partyAcctgPrefAndGroup.iterator();
while (iter.hasNext()) {
   group = iter.next()
   partyAcctgPrefAndGroupList.add(["key":group.partyId,"value":group.groupName]);
}
globalContext.PartyAcctgPrefAndGroupList = partyAcctgPrefAndGroupList;*/
options = UtilCommon.DISTINCT_READ_OPTIONS;
orderBy = UtilMisc.toList("groupName");
userLogin = request.getAttribute("userLogin");
if (userLogin != null)
{
	userLoginStr = userLogin.getString("userLoginId");
}
else
{
	userLoginStr = "";
}
prefOrganizationStr = "";

orgCondition = new EntityConditionList( UtilMisc.toList(
               new EntityExpr("userLoginId", EntityOperator.EQUALS, userLoginStr),
               new EntityExpr("preferenceName", EntityOperator.EQUALS, "organizationPartyId")
               ), EntityOperator.AND);
prefOrganization = delegator.findByCondition("UserLoginViewPreference", orgCondition, null, UtilMisc.toList("preferenceValue"));
if (prefOrganization.isEmpty())
{
	prefOrganizationStr = null;
}

prefOrgIterator = prefOrganization.iterator();
while (prefOrgIterator.hasNext())
{
	prefOrg = prefOrgIterator.next();
	prefOrganizationStr = prefOrg.getString("preferenceValue");
}

conditions = new EntityConditionList( UtilMisc.toList(
             new EntityExpr("roleTypeId", EntityOperator.EQUALS, "INTERNAL_ORGANIZATIO"),
             new EntityExpr("ptyAcctgPrefPartyId", EntityOperator.NOT_EQUAL, null)
             ), EntityOperator.AND);
configuredOrganizations = delegator.findByCondition("PartyAcctgPreferenceRoleAndDetail", conditions, null, null, orderBy, options);
               
if (prefOrganizationStr != null)
{
	confOrgsIterator = configuredOrganizations.iterator();
	configuredOrganization = null;
	while (confOrgsIterator.hasNext())
	{
		confOrg = confOrgsIterator.next();
		listOrganization = confOrg.getString("partyId");
		if (listOrganization.equals(prefOrganizationStr))
		{
			configuredOrganization = confOrg;
	    }    
	}
	configuredOrganizationList = UtilMisc.toList(configuredOrganization);
	globalContext.PartyAcctgPrefAndGroupList = configuredOrganizationList;
}
else
{
	partyAcctgPrefAndGroup = delegator.findList("PartyAcctgPrefAndGroup", null, null, null, null, false);
	iter = partyAcctgPrefAndGroup.iterator();
	while (iter.hasNext()) {
		group = iter.next()
		partyAcctgPrefAndGroupList.add(["key":group.partyId,"value":group.groupName]);
	}
	globalContext.PartyAcctgPrefAndGroupList = partyAcctgPrefAndGroupList;
}
//hiddenFileds
hiddenFields = [];
hiddenFields.add([name : "userPrefTypeId", value : "ORGANIZATION_PARTY"]);
hiddenFields.add([name : "userPrefGroupTypeId", value : "GLOBAL_PREFERENCES"]);
globalContext.hiddenFields = hiddenFields;
