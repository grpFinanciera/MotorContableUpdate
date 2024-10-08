<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<#assign nowTimestamp = Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp()>

<#if (requestAttributes.externalLoginKey)?exists><#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists></#if>
<#if (externalLoginKey)?exists><#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists></#if>
<#assign ofbizServerName = application.getAttribute("_serverId")?default("default-server")>
<#assign contextPath = request.getContextPath()>
<#assign displayApps = Static["org.ofbiz.base.component.ComponentConfig"].getAppBarWebInfos(ofbizServerName, "secondary")>
<#--
<#if userLogin?has_content>
<center>
  <div id="secondary-navigation">
    <#list displayApps as display>
      <#assign thisApp = display.getContextRoot()>
      <#assign permission = true>
      <#assign selected = false>
      <#assign permissions = display.getBasePermission()>
      <#list permissions as perm>
        <#if (perm != "NONE" && !security.hasEntityPermission(perm, "_VIEW", session) && !authz.hasPermission(session, perm, requestParameters))>
          <#-- User must have ALL permissions in the base-permission list --><#--
          <#assign permission = false>
        </#if>
      </#list>
      <#if permission == true>
        <#if thisApp == contextPath || contextPath + "/" == thisApp>
          <#assign selected = true>
        </#if>
        <#assign thisURL = thisApp>
        <#if thisApp != "/">
          <#assign thisURL = thisURL + "/control/main">
        </#if>
        <a<#if selected> class="selected"</#if> href="${thisURL}${externalKeyParam}" <#if uiLabelMap?exists> title="${uiLabelMap[display.description]}">${uiLabelMap[display.title]}<#else> title="${display.description}"> ${display.title}</#if></a>
      </#if>
    </#list>
  </div>
</center>
</#if>
-->
<#--
<div id="footer">
  <p>${nowTimestamp?datetime?string.short} -
  <a href="<@ofbizUrl>ListTimezones</@ofbizUrl>">${timeZone.getDisplayName(timeZone.useDaylightTime(), Static["java.util.TimeZone"].LONG, locale)}</a>
  </p>
  <p><a href="http://jigsaw.w3.org/css-validator/"><img src="<@ofbizContentUrl>/images/vcss.gif</@ofbizContentUrl>" alt="Valid CSS!"/></a>
  <a href="http://validator.w3.org/check?uri=referer"><img src="<@ofbizContentUrl>/images/valid-xhtml10.png</@ofbizContentUrl>" alt="Valid XHTML 1.0!"/></a></p>
  <p>
    ${uiLabelMap.CommonCopyright} (c) 2001-${nowTimestamp?string("yyyy")} The Apache Software Foundation - <a href="http://www.apache.org" target="_blank">www.apache.org</a><br />
    ${uiLabelMap.CommonPoweredBy} <a href="http://ofbiz.apache.org" target="_blank">Apache OFBiz</a>
  </p>
  <p>
    <a href="http://www.opentaps.org">${uiLabelMap.OpentapsProductName}</a> ${uiLabelMap.OpentapsReleaseVersion}.<br />
    Opentaps is a trademark of <a href="http://www.opentaps.org">Open Source Strategies, Inc.</a>
  </p>
</div>
-->
<#if layoutSettings.VT_FTR_JAVASCRIPT?has_content>
  <#list layoutSettings.VT_FTR_JAVASCRIPT as javaScript>
    <script src="<@ofbizContentUrl>${StringUtil.wrapString(javaScript)}</@ofbizContentUrl>" type="text/javascript"></script>
  </#list>
</#if>

</div>
</body>
</html>
