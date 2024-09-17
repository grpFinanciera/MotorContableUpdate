
<#--
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
-->

<#-- 
To use these macros in your page, first put this at the top:

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

Then each one can be used as a macro right in an FTL like this:
<@displayCurrency amount=10.00 currencyUomId="MXN" />

For more information, please see documentation/opentapsFormMacros.html
-->


<@import location="component://opentaps-common/webapp/common/includes/lib/flexAreaMacros.ftl"/>


<#-- ###################################################################### -->
<#-- ###                                                                 ## -->
<#-- ###                Atomic "Building Block" Macros                   ## -->
<#-- ###                                                                 ## -->
<#-- ###################################################################### -->


<#-- -------------------------- -->
<#-- --    Display Macros    -- -->
<#-- -------------------------- -->


<#macro display text="" class="tabletext" style="">
  <span class="${class}" style="${style}">${text}</span>
</#macro>

<#macro displayCustomId id="" text="" class="tabletext" style="">
  <span id="${id}" class="${class}" style="${style}">${text}</span>
</#macro>

<#macro displayCellCustom name text="" class="tabletext" style="" blockStyle="" blockClass="">
  <td align="right" class="${blockClass}" style="${blockStyle}"><span class="${class}" style="${style}">${text}</span></td>
</#macro>

<#macro displayCell text="" class="tabletext" style="" blockStyle="" blockClass="">
  <td class="${blockClass}" style="${blockStyle}"><span class="${class}" style="${style}">${text}</span></td>
</#macro>

<#macro displayTitleCell title titleClass="tableheadtext"><td class="titleCell"><span class="${titleClass}">${title}</span></td></#macro>

<#macro displayRow title text="" class="tabletext" blockClass="" titleClass="tableheadtext">
  <tr>
    <@displayTitleCell title=title titleClass=titleClass />
    <@displayCell text=text class=class blockClass=blockClass />
  </tr>
</#macro>

<#macro displayLink href text class="linktext" style="" target="" id="">
<#compress>
  <#assign idText = id?has_content?string("id=\"" + id + "\"", "")/>
  <#if href?starts_with("/")>
    <a href="${href}<#if externalKeyParam?has_content>${StringUtil.wrapString(externalKeyParam)}</#if>" ${idText} style="${style}" class="${class}" target="${target}">${text}</a>
  <#elseif href?starts_with("javascript:")>
    <a href="${href}" ${idText} style="${style}" class="${class}">${text}</a>
  <#else>
    <a href="<@ofbizUrl>${href}</@ofbizUrl>" ${idText} style="${style}" class="${class}" target="${target}">${text}</a>
  </#if>
</#compress>
</#macro>

<#macro displayLinkButton href text class="linktext" style="" target="" id="">
<#compress>
  <#assign idText = id?has_content?string("id=\"" + id + "\"", "")/>
  <#if href?starts_with("/")>
    <a href="${href}<#if externalKeyParam?has_content>${StringUtil.wrapString(externalKeyParam)}</#if>" ${idText} style="${style}" target="${target}">
		<input type="button" value="${text}" class="${class}"/>
	</a>
  <#elseif href?starts_with("javascript:")>
    <a href="${href}" ${idText} style="${style}"><input type="button" value="${text}" class="${class}"/></a>
  <#else>
    <a href="${href}" ${idText} style="${style}" target="${target}"><input type="button" value="${text}" class="${class}"/></a>
  </#if>
</#compress>
</#macro>

<#macro displayLinkCell href text class="linktext" style="" blockStyle="" blockClass="" target="" id="">
  <td class="${blockClass}" style="${blockStyle}"><@displayLink text=text href=href style=style class=class target=target id=id /></td>
</#macro>

<#macro displayLinkRow href title text class="linktext" blockClass="" titleClass="tableheadtext" target="" id="">
<tr>
  <td class="titleCell"><span class="${titleClass}">${title}</span></td>
  <@displayLinkCell text=text href=href class=class blockClass=blockClass target=target id=id />
</tr>
</#macro>

<#-- for linking to a party view page in CRMSFA or party manager -->

<#macro displayPartyLink partyId text="" class="linktext" target="" id="">
  <#assign idText = id?has_content?string("id=\"" + id + "\"", "")/>
  <#assign href = Static["org.opentaps.common.party.PartyHelper"].createViewPageURL(partyId, delegator) />
  <#if href?has_content>
    <#assign href = StringUtil.wrapString(href) />
  </#if>
  <#assign name = text />
  <#if !name?has_content>
    <#assign name = Static["org.opentaps.common.party.PartyHelper"].getCrmsfaPartyName(partyId, delegator) />
  </#if>
  <@displayLink text=name href=href class=class target=target id=id />
</#macro>

<#macro displayPartyLinkCell partyId text="" class="linktext" blockClass="" target="" id="">
  <td class="${blockClass}"><@displayPartyLink text=text partyId=partyId class=class target=target id=id /></td>
</#macro>

<#macro displayPartyLinkRow partyId title text="" class="linktext" blockClass="" titleClass="tableheadtext" target="" id="">
<tr>
  <td class="titleCell"><span class="${titleClass}">${title}</span></td>
  <@displayPartyLinkCell text=text partyId=partyId class=class blockClass=blockClass target=target id=id />
</tr>
</#macro>

<#macro expandLabel label params={} >
  <#-- If locale is empty, get it from the request -->
  <#if !locale?has_content><#assign locale = Static["org.ofbiz.base.util.UtilHttp"].getLocale(request)/></#if>
  ${Static["org.opentaps.common.util.UtilMessage"].expandLabel( label, locale, params)}
</#macro>

<#function getNow>
  <#return getLocalizedDate(Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp()) />
</#function>

<#function getToday>
  <#return getLocalizedDate(Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp(), "DATE") />
</#function>

<#-- Format should be one of the DATE_TIME, DATE, TIME, DATE_ONLY -->
<#function getLocalizedDate date="" format="DATE_TIME" encode=false>

  <#-- returns String unchanged -->
  <#if date?is_string>
  	<#return date/>
  </#if>

  <#-- If locale or timeZone are empty, get them from the request -->
  <#if !locale?has_content><#assign locale = Static["org.ofbiz.base.util.UtilHttp"].getLocale(request)/></#if>
  <#if !timeZone?has_content><#assign timeZone = Static["org.ofbiz.base.util.UtilHttp"].getTimeZone(request)/></#if>
  <#assign isDateTime = true />  
  <#if date?has_content && date?is_date>
    <#if format == "DATE_TIME">
      <#assign fmt = Static["org.ofbiz.base.util.UtilDateTime"].getDateTimeFormat(locale)/>
    <#elseif format == "DATE">
      <#assign fmt = Static["org.ofbiz.base.util.UtilDateTime"].getDateFormat(locale)/>
      <#assign isDateTime = false />  	
    <#elseif format == "TIME">
      <#assign fmt = Static["org.ofbiz.base.util.UtilDateTime"].getTimeFormat(locale)/>
    <#elseif format == "DATE_ONLY">
      <#assign fmt = Static["org.ofbiz.base.util.UtilDateTime"].getDateFormat(locale)/>
	  <#assign isDateTime = false />
      <#-- If format is DATE_ONLY, just format the date portion and return (this seems to be rendered identically to DATE)-->
      <#return date?date?string(fmt) />
    </#if>
	<#if isDateTime>
		<#assign dateString = Static["org.ofbiz.base.util.UtilDateTime"].timeStampToString(date?datetime,fmt, timeZone, locale)/>
	<#else>	
		<#assign dateString = Static["org.opentaps.common.util.UtilDate"].dateToString(date?date,fmt,timeZone,locale) />
	</#if>
    <#if encode>
        <#return Static["org.ofbiz.base.util.UtilHttp"].encodeBlanks(dateString)/>
    <#else>
        <#return dateString/>
    </#if>
  <#else>
    <#return ""/>
  </#if> 
</#function>

<#macro displayDate date="" format="DATE_TIME" class="tabletext" style="" default="" compareTo="" highlightAfter=false highlightBefore=false highlightStyle="color:red;">
  <#if compareTo?has_content && compareTo?is_date && date?has_content && date?is_date && ((highlightAfter && date.after(compareTo)) || (highlightBefore && date.before(compareTo)))>
    <#assign applyStyle="${style};${highlightStyle}"/>
  <#else/>
    <#assign applyStyle=style />
  </#if>
  <span class="${class}" style="${applyStyle}"><#if date?has_content>${getLocalizedDate(date, format)}<#else>${default}</#if></span>
</#macro>

<#macro displayDateCell date="" format="DATE_TIME" class="tabletext" style="" blockStyle="" blockClass="" default="" compareTo="" highlightAfter=false highlightBefore=false highlightStyle="color:red;">
  <td class="${blockClass}" style="${blockStyle}"><@displayDate date=date format=format class=class style=style default=default compareTo=compareTo highlightAfter=highlightAfter highlightBefore=highlightBefore highlightStyle=highlightStyle/></td>
</#macro>

<#macro displayDateRow title date="" format="DATE_TIME" class="tabletext" blockClass="" titleClass="tableheadtext" default="" compareTo="" highlightAfter=false highlightBefore=false highlightStyle="color:red;">
  <tr>
    <td class="titleCell"><span class="${titleClass}">${title}</span></td>
    <@displayDateCell date=date format=format class=class blockClass=blockClass default=default compareTo=compareTo highlightAfter=highlightAfter highlightBefore=highlightBefore highlightStyle=highlightStyle />
  </tr>
</#macro>

 
<#macro displayCurrency currencyUomId="" amount=0 class="tabletext" id="" style="">
  <#if currencyUomId == "">
    <#assign isoCode = Static["org.opentaps.common.util.UtilConfig"].getPropertyValue(opentapsApplicationName?default("opentaps"), "defaultCurrencyUomId") />
  <#else>
    <#assign isoCode = currencyUomId />
  </#if>
	<#if style?exists >	
		<#assign selectedStyle = style+(amount?is_number && amount < 0)?string("color: #AA0000", "")/>
	<#else>
		<#assign selectedStyle = (amount?is_number && amount < 0)?string("color: #AA0000", "") />
	</#if>
  <span id="${id}" class="${class}" style="${selectedStyle}" align="right"><@ofbizCurrency amount=amount isoCode=isoCode /></span>
</#macro>

<#macro displayCurrencyCell currencyUomId="" amount=0 class="currencyCell" id="" style="" colspan="" >
  <td class="${class}" colspan="${colspan}"><@displayCurrency currencyUomId=currencyUomId amount=amount class=class id=id style=style /></td>
</#macro>

<#macro displayCurrencyRow title currencyUomId="" amount=0 class="currencyCell" titleClass="tableheadtext" id="" colspan="">
  <tr>
    <td class="titleCell"><span class="${titleClass}">${title}</span></td>
    <@displayCurrencyCell currencyUomId=currencyUomId amount=amount class=class id=id colspan=colspan/>
  </tr>
</#macro>


<#macro displayIndicator value="" class="tabletext" yesLabel=uiLabelMap.CommonYes noLabel=uiLabelMap.CommonNo>
  <span class="${class}"><#if value == "Y">${yesLabel}<#elseif value == "N">${noLabel}<#else>${uiLabelMap.OpentapsUnknown}</#if></span>
</#macro>

<#macro displayIndicatorCell value="" class="tabletext" blockClass="" yesLabel=uiLabelMap.CommonYes noLabel=uiLabelMap.CommonNo>
  <td class="${blockClass}"><@displayIndicator value=value class=class yesLabel=yesLabel noLabel=noLabel /></td>
</#macro>

<#macro displayIndicatorRow title value="" class="tabletext" blockClass="" titleClass="tableheadtext" yesLabel=uiLabelMap.CommonYes noLabel=uiLabelMap.CommonNo>
  <tr>
    <td class="titleCell"><span class="${titleClass}">${title}</span></td>
    <@displayIndicatorCell value=value class=class blockClass=blockClass yesLabel=yesLabel noLabel=noLabel />
  </tr>
</#macro>

<#--
  Display reports for specified report group.
    Attributes:
      group - One of the ReportGroup.reportGroupId
      runtimeData - for future extension
      updater - URL that should used to update report summary data
      nameOnly - show only report names if true (default), otherwise attach description on the right of report name.
      ignoreSetupScreen - ignore setup screen, display pdf directly
-->
<#macro displayReportGroup group runtimeData="" updater="" nameOnly=true ignoreSetupScreen=false>
  <#assign reportGroupedList = Static["org.opentaps.common.reporting.UtilReports"].getManagedReports(parameters.componentName, group?default(null), delegator, Static["org.ofbiz.base.util.UtilHttp"].getLocale(request))?default([])/> 

  <#list reportGroupedList as reportGroup>
  <p><b>${reportGroup.description}</b>
  <#if updater?has_content>
    <#if runtimeData?has_content>
      <#assign transformTimestamp = Static["org.opentaps.common.reporting.UtilReports"].getTrasformationTimeByType("ENCUMB_GL_ENTRY", delegator)?if_exists />
      <br/><i><#if transformTimestamp?has_content>${uiLabelMap.OpentapsDataAsOf}&nbsp;<@displayDate date=transformTimestamp/><#else>No data</#if></i>&nbsp;(<a class="linktext" href="<@ofbizUrl>${updater}</@ofbizUrl>">${uiLabelMap.FinancialsEncumbranceRefresh}</a>)
    <#else>
      &nbsp;(<a class="linktext" href="<@ofbizUrl>${updater}</@ofbizUrl>">${uiLabelMap.FinancialsEncumbranceRefresh}</a>)
    </#if>  
  </#if>
  </p>
    <ul class="bulletList">
    <#assign reports = reportGroup.reports/>
    <#list reports as report>
      <#if ignoreSetupScreen>
        <li><a href="<@ofbizUrl>runReport?</@ofbizUrl>reportId=${report.reportId}&reportType=application/pdf&organizationPartyId=${organizationPartyId}">${report.shortName}</a><#if report.description?has_content && !nameOnly>: ${report.description}</#if></li>
      <#else>
        <#if report.setupUri?has_content>
          <#assign setupUri = "${report.setupUri}"/>
        <#else>
          <#assign setupUri = "setupReport?"/>
        </#if>
        <li><a href="<@ofbizUrl>${setupUri}</@ofbizUrl>reportId=${report.reportId}">${report.shortName}</a><#if report.description?has_content && !nameOnly>: ${report.description}</#if></li>
      </#if>
    </#list>
    <#nested>
    </ul>
  </#list>

</#macro>

<#-- ------------------------ -->
<#-- --    Input Macros    -- -->
<#-- ------------------------ -->


<#function getIndexedName name index=-1>
  <#if index == -1>
    <#return name>
  <#else>
    <#return name + "_o_" + index>
  </#if>
</#function>

<#function getDefaultValue name default="" index=-1 ignoreParameters=false>
  <#assign key = getIndexedName(name, index)>
  <#if !ignoreParameters && parameters?exists && parameters.containsKey(key)>
    <#return parameters.get(key)?default("")>
  </#if>
  <#return default>
</#function>

<#macro displayTooltip text>
  <span class="tooltip">${text}</span>
</#macro>


<#macro inputHidden name value index=-1 id="">
  <input type="hidden" id="${id}" name="${getIndexedName(name, index)}" value="${value}"/>
</#macro>


<#macro inputText name size=30 maxlength="" default="" index=-1 password=false readonly=false onChange="" id="" ignoreParameters=false errorField="" tabIndex="" tooltip="">
  <#if id == ""><#assign idVal = name><#else><#assign idVal = id></#if>
  <input id="${getIndexedName(idVal, index)}" name="${getIndexedName(name, index)}" type="<#if password>password<#else>text</#if>" size="${size}" maxlength="${maxlength}" class="inputBox" <#if !password>value="${getDefaultValue(name, default, index, ignoreParameters)}"</#if> <#if readonly>readonly="readonly"</#if> onChange="${onChange}" <#if tabIndex?has_content>tabindex="${tabIndex}"</#if>/>
  <#if tooltip?has_content><@displayTooltip text=tooltip/></#if>
  <#if errorField?has_content><@displayError name=errorField index=index /></#if>
</#macro>

<#macro inputTextCell name size=30 maxlength="" default="" index=-1 password=false readonly=false onChange="" id="" ignoreParameters=false errorField="" tabIndex="" colspan="" tooltip="">
  <td<#if colspan?has_content> colspan="${colspan}"</#if>><@inputText name=name id=id size=size maxlength=maxlength default=default index=index password=password readonly=readonly onChange=onChange ignoreParameters=ignoreParameters errorField=errorField tabIndex=tabIndex tooltip=tooltip/></td>
</#macro>

<#macro inputTextCellCantidad name size=5 maxlength="" default="" index=-1 password=false readonly=false onChange="" id="" ignoreParameters=false errorField="" tabIndex="" colspan="" tooltip="">
  <td<#if colspan?has_content> colspan="${colspan}"</#if>><@inputText name=name id=id size=size maxlength=maxlength default=default index=index password=password readonly=readonly onChange=onChange ignoreParameters=ignoreParameters errorField=errorField tabIndex=tabIndex tooltip=tooltip/></td>
</#macro>

<#macro inputTextCellDisplay name size=30 maxlength="" default="" index=-1 password=false readonly=true onChange="" id="" ignoreParameters=false errorField="" tabIndex="" colspan="" tooltip="">
  <td<#if colspan?has_content> colspan="${colspan}"</#if>><@inputText name=name id=id size=size maxlength=maxlength default=default index=index password=password readonly=readonly onChange=onChange ignoreParameters=ignoreParameters errorField=errorField tabIndex=tabIndex tooltip=tooltip/></td>
</#macro>

<#macro inputTextRow name title size=30 maxlength="" default="" titleClass="tableheadtext" index=-1 rowId="" hidden=false password=false readonly=false onChange="" id="" ignoreParameters=false errorField="" tabIndex="" colspan="" tooltip="">
  <tr<#if rowId?length != 0> id="${getIndexedName(rowId, index)}"</#if><#if hidden> style="display:none"</#if>>
    <td class="titleCell"><span class="${titleClass}">${title}</span></td>
    <@inputTextCell name=name id=id size=size maxlength=maxlength default=default index=index password=password readonly=readonly onChange=onChange ignoreParameters=ignoreParameters errorField=errorField tabIndex=tabIndex colspan=colspan tooltip=tooltip/>
  </tr>
</#macro>

<#macro inputTextRowCustom name title size=30 maxlength="" default="" titleClass="tableheadtext" index=-1 rowId="" hidden=false password=false readonly=true onChange="" id="" ignoreParameters=false errorField="" tabIndex="" colspan="" tooltip="">
  <tr<#if rowId?length != 0> id="${getIndexedName(rowId, index)}"</#if><#if hidden> style="display:none"</#if>>
    <td class="titleCell"><span class="${titleClass}">${title}</span></td>
    <@inputTextCell name=name id=id size=size maxlength=maxlength default=default index=index password=password readonly=readonly onChange=onChange ignoreParameters=ignoreParameters errorField=errorField tabIndex=tabIndex colspan=colspan tooltip=tooltip/>
  </tr>
</#macro>

<#macro inputRangeRow title fromName thruName titleClass="tableheadtext" size=20 ignoreParameters=false colspan="" tooltip="">
  <tr>
    <@displayTitleCell title=title titleClass=titleClass/>
    <@inputRangeCell fromName=fromName thruName=thruName size=size ignoreParameters=ignoreParameters colspan=colspan tooltip=tooltip/>
  </tr>
</#macro>

<#macro inputRangeCell fromName thruName size=20 ignoreParameters=false colspan="" tooltip="">
  <td<#if colspan?has_content> colspan="${colspan}"</#if>><@inputRange fromName=fromName thruName=thruName size=size ignoreParameters=ignoreParameters tooltip=tooltip/></td>
</#macro>

<#macro inputRange fromName thruName size=20 ignoreParameters=false tooltip="">
  <span class="tabletext">
    ${uiLabelMap.CommonFrom} 
    <@inputText name=fromName size=size ignoreParameters=ignoreParameters/>
    &nbsp;&nbsp;
    ${uiLabelMap.CommonThru} 
    <@inputText name=thruName size=size ignoreParameters=ignoreParameters/>
  </span>
  <#if tooltip?has_content><@displayTooltip text=tooltip/></#if>
</#macro>


<#macro inputCheckbox name value="Y" index=-1 onChange="" onClick="" id="" ignoreParameters=false errorField="" tabIndex="" default="" tooltip="">
  <#if id == ""><#assign idVal = name><#else><#assign idVal = id></#if>
  <input id="${getIndexedName(idVal, index)}" name="${getIndexedName(name, index)}" type="checkbox" value="${value}" onChange="${onChange}" onClick="${onClick}" <#if tabIndex?has_content>tabindex="${tabIndex}"</#if> <#if value == getDefaultValue(name, default, index, ignoreParameters)>checked="checked"</#if>/>
  <#if tooltip?has_content><@displayTooltip text=tooltip/></#if>
  <#if errorField?has_content><@displayError name=errorField index=index /></#if>
</#macro>

<#macro inputCheckboxCell name value="Y" index=-1 onChange="" onClick="" id="" ignoreParameters=false errorField="" tabIndex="" default="" colspan="" tooltip="">
  <td<#if colspan?has_content> colspan="${colspan}"</#if>><@inputCheckbox name=name value=value id=id default=default index=index onChange=onChange onClick=onClick ignoreParameters=ignoreParameters errorField=errorField tabIndex=tabIndex tooltip=tooltip /></td>
</#macro>

<#macro inputCheckboxRow name title value="Y" index=-1 rowId="" hidden=false onChange="" onClick="" id="" ignoreParameters=false errorField="" tabIndex="" default="" titleClass="tableheadtext" colspan="" tooltip="">
  <tr<#if rowId?length != 0> id="${getIndexedName(rowId, index)}"</#if><#if hidden> style="display:none"</#if>>
    <td class="titleCell"><span class="${titleClass}">${title}</span></td>
    <@inputCheckboxCell name=name value=value id=id default=default index=index onChange=onChange onClick=onClick ignoreParameters=ignoreParameters errorField=errorField tabIndex=tabIndex colspan=colspan tooltip=tooltip/>
  </tr>
</#macro>

<#macro inputFile name size=30 maxlength="" default="" index=-1 onChange="" id="" errorField="" tabIndex="" tooltip="">
  <#if id == ""><#assign idVal = name><#else><#assign idVal = id></#if>
  <input id="${getIndexedName(idVal, index)}" name="${getIndexedName(name, index)}" type="file" size="${size}" maxlength="${maxlength}" class="inputBox" onChange="${onChange}" <#if tabIndex?has_content>tabindex="${tabIndex}"</#if>/>
  <#if tooltip?has_content><@displayTooltip text=tooltip/></#if>
  <#if errorField?has_content><@displayError name=errorField index=index /></#if>
</#macro>

<#macro inputFileCell name size=30 maxlength="" default="" index=-1 onChange="" id="" errorField="" tabIndex="" colspan="" tooltip="">
  <td<#if colspan?has_content> colspan="${colspan}"</#if>><@inputFile name=name id=id size=size maxlength=maxlength default=default index=index onChange=onChange errorField=errorField tabIndex=tabIndex tooltip=tooltip /></td>
</#macro>

<#macro inputFileRow name title size=30 maxlength="" default="" titleClass="tableheadtext" index=-1 rowId="" hidden=false onChange="" id="" errorField="" tabIndex="" colspan="" tooltip="">
  <tr<#if rowId?length != 0> id="${getIndexedName(rowId, index)}"</#if><#if hidden> style="display:none"</#if>>
    <td class="titleCell"><span class="${titleClass}">${title}</span></td>
    <@inputFileCell name=name id=id size=size maxlength=maxlength default=default index=index onChange=onChange errorField=errorField tabIndex=tabIndex colspan=colspan tooltip=tooltip/>
  </tr>
</#macro>

<#macro inputTextarea name rows=5 cols=60 default="" index=-1 ignoreParameters=false errorField="" tabIndex="" readonly=false tooltip="">
  <textarea rows="${rows}" cols="${cols}" name="${getIndexedName(name, index)}" class="inputBox" <#if tabIndex?has_content>tabindex="${tabIndex}"</#if> <#if readonly>readonly="readonly"</#if>>${getDefaultValue(name, default, index, ignoreParameters)}</textarea>
  <#if tooltip?has_content><@displayTooltip text=tooltip/></#if>
  <#if errorField?has_content><@displayError name=errorField index=index /></#if>
</#macro>

<#macro inputTextareaCell name rows=5 cols=60 default="" index=-1 ignoreParameters=false errorField="" tabIndex="" colspan="" readonly=false tooltip="">
  <td<#if colspan?has_content> colspan="${colspan}"</#if>><@inputTextarea name=name rows=rows cols=cols default=default index=index ignoreParameters=ignoreParameters errorField=errorField tabIndex=tabIndex readonly=readonly tooltip=tooltip/></td>
</#macro>

<#macro inputTextareaRow name title rows=5 cols=60 default="" titleClass="tableheadtext" index=-1 ignoreParameters=false errorField="" tabIndex="" colspan="" readonly=false tooltip="">
  <tr>
    <td class="titleCellTop"><span class="${titleClass}">${title}</span></td>
    <@inputTextareaCell name=name rows=rows cols=cols default=default index=index ignoreParameters=ignoreParameters errorField=errorField tabIndex=tabIndex colspan=colspan readonly=readonly tooltip=tooltip/>
  </tr>
</#macro>


<#-- these are duplicated because #nested only goes one level up -->

<#macro inputSelectHash name hash required=true default="" index=-1 onChange="" ignoreParameters=false errorField="" tabIndex="" tooltip="" id="">
 <#assign defaultValue = getDefaultValue(name, default, index, ignoreParameters)>
 <select name="${getIndexedName(name, index)}" class="inputBox" onChange="${onChange}" id="${id}"<#if tabIndex?has_content>tabindex="${tabIndex}"</#if>>
   <#if !required><option value=""></option></#if>
   <#assign keys = hash?keys />
   <#list keys as k>
     <option <#if defaultValue = k>selected="selected"</#if> value="${k}">${hash[k]}</option>
   </#list>
 </select>
  <#if tooltip?has_content><@displayTooltip text=tooltip/></#if>
  <#if errorField?has_content><@displayError name=errorField index=index /></#if>
</#macro>

<#macro inputSelectHashCell name hash required=true default="" index=-1 onChange="" ignoreParameters=false errorField="" tabIndex="" colspan="" tooltip="" id="">
 <td<#if colspan?has_content> colspan="${colspan}"</#if>><@inputSelectHash name=name required=required default=default hash=hash index=index onChange=onChange ignoreParameters=ignoreParameters errorField=errorField tabIndex=tabIndex tooltip=tooltip id=id/></td>
</#macro>

<#macro inputSelectHashRow name title hash required=true default="" index=-1 titleClass="tableheadtext" onChange="" ignoreParameters=false errorField="" tabIndex="" colspan="" tooltip="">
 <tr>
   <td class="titleCell"><span class="${titleClass}">${title}</span></td>
   <@inputSelectHashCell name=name required=required default=default hash=hash index=index onChange=onChange ignoreParameters=ignoreParameters errorField=errorField tabIndex=tabIndex colspan=colspan tooltip=tooltip/>
 </tr>
</#macro>

<#macro inputSelect name list key="" displayField="" default="" index=-1 required=true defaultOptionText="" onChange="" id="" ignoreParameters=false errorField="" tabIndex="" readonly=false class="inputBox" tooltip="">
  <#if key == ""><#assign listKey = name><#else><#assign listKey = key></#if>
  <#if id == ""><#assign idVal = name><#else><#assign idVal = id></#if>
  <#assign defaultValue = getDefaultValue(name, default, index, ignoreParameters)/>
  <select id="${getIndexedName(idVal, index)}" name="${getIndexedName(name, index)}" class="${class}" onChange="${onChange}" <#if tabIndex?has_content>tabindex="${tabIndex}"</#if> <#if readonly>disabled="disabled"</#if>>
    <#if !required><option value="">${defaultOptionText}</option></#if>
    <#list list as option>
      <#if option.get(listKey) == defaultValue || listKey == defaultValue><#assign selected = "selected=\"selected\""><#else><#assign selected = ""></#if>
      <option ${selected} value="${option.get(listKey)}">
        <#if displayField==""><#nested option><#else>${option.get(displayField)?if_exists}</#if>
      </option>
    </#list>
  </select>
  <#if tooltip?has_content><@displayTooltip text=tooltip/></#if>
  <#if errorField?has_content><@displayError name=errorField index=index /></#if>
</#macro>

<#macro inputSelectCell name list key="" displayField="" default="" index=-1 required=true defaultOptionText="" onChange="" id="" ignoreParameters=false errorField="" tabIndex="" readonly=false class="inputBox" colspan="" tooltip="" onBlur="">
  <#if key == ""><#assign listKey = name><#else><#assign listKey = key></#if>
  <#if id == ""><#assign idVal = name><#else><#assign idVal = id></#if>
  <#assign defaultValue = getDefaultValue(name, default, index, ignoreParameters)>
  <td<#if colspan?has_content> colspan="${colspan}"</#if>>
  <select id="${getIndexedName(idVal, index)}" name="${getIndexedName(name, index)}" class="${class}" onChange="${onChange}" <#if tabIndex?has_content>tabindex="${tabIndex}"</#if> <#if readonly>disabled="disabled"</#if> onBlur="${onBlur}">
    <#if !required><option value="">${defaultOptionText}</option></#if>
    <#list list as option>
      <#if option.get(listKey) == defaultValue || listKey == defaultValue><#assign selected = "selected=\"selected\""><#else><#assign selected = ""></#if>
      <option ${selected} value="${option.get(listKey)}">
        <#if displayField==""><#nested option><#else>${getDescriptionCombo(option,displayField,listKey)}</#if>
      </option>
    </#list>
  </select>
  <#if tooltip?has_content><@displayTooltip text=tooltip/></#if>
  <#if errorField?has_content><@displayError name=errorField index=index /></#if>
  </td>
</#macro>

<#macro inputSelectRow title name list key="" displayField="" default="" index=-1 required=true defaultOptionText="" titleClass="tableheadtext" onChange="" id="" ignoreParameters=false errorField="" tabIndex="" readonly=false class="inputBox" colspan="" tooltip="">
  <#if key == ""><#assign listKey = name><#else><#assign listKey = key></#if>
  <#if id == ""><#assign idVal = name><#else><#assign idVal = id></#if>
  <#assign defaultValue = getDefaultValue(name, default, index, ignoreParameters)>
  <tr>
  <td class="titleCell"><span class="${titleClass}">${title}</span></td>
  <td<#if colspan?has_content> colspan="${colspan}"</#if>>
  <select id="${getIndexedName(idVal, index)}" name="${getIndexedName(name, index)}" class="${class}" onChange="${onChange}" <#if tabIndex?has_content>tabindex="${tabIndex}"</#if> <#if readonly>disabled="disabled"</#if>>
    <#if !required><option value="">${defaultOptionText}</option></#if>
    <#list list as option>
      <#if option.get(listKey) == defaultValue || listKey == defaultValue><#assign selected = "selected=\"selected\""><#else><#assign selected = ""></#if>
      <option ${selected!''} value="${option.get(listKey)}">
        <#if displayField==""><#nested option><#else>${option.get(displayField)?if_exists}</#if>
      </option>
    </#list>
  </select>
  <#if tooltip?has_content><@displayTooltip text=tooltip/></#if>
  <#if errorField?has_content><@displayError name=errorField index=index /></#if>
  </td>
  </tr>
</#macro>

<#macro inputMultiSelect title name list key="" displayField="" default=[] index=-1 defaultOptionText="" titleClass="tableheadtext" size=5 onChange="" id="" errorField="" tabIndex="" tooltip="">
  <#if key == ""><#assign listKey = name><#else><#assign listKey = key></#if>
  <#if id == ""><#assign idVal = name><#else><#assign idVal = id></#if>
  <select id="${getIndexedName(idVal, index)}" name="${getIndexedName(name, index)}" class="inputBox" multiple="multiple" size="${size}" style="height:auto" onChange="${onChange}" <#if tabIndex?has_content>tabindex="${tabIndex}"</#if>>
    <#list list as option>
      <#if default?seq_contains(option.get(listKey))><#assign selected = "selected=\"selected\""><#else><#assign selected = ""></#if>
      <option ${selected} value="${option.get(listKey)}">
        <#if displayField==""><#nested option><#else>${option.get(displayField)?if_exists}</#if>
      </option>
    </#list>
  </select>
  <#if tooltip?has_content><@displayTooltip text=tooltip/></#if>
  <#if errorField?has_content><@displayError name=errorField index=index /></#if>
</#macro>

<#macro inputMultiSelectCell title name list key="" displayField="" default=[] index=-1 defaultOptionText="" titleClass="tableheadtext" size=5 onChange="" id="" errorField="" tabIndex="" colspan="" tooltip="">
  <#if key == ""><#assign listKey = name><#else><#assign listKey = key></#if>
  <#if id == ""><#assign idVal = name><#else><#assign idVal = id></#if>
  <td<#if colspan?has_content> colspan="${colspan}"</#if>>
  <select id="${getIndexedName(idVal, index)}" name="${getIndexedName(name, index)}" class="inputBox" multiple="multiple" size="${size}" style="height:auto" onChange="${onChange}" <#if tabIndex?has_content>tabindex="${tabIndex}"</#if>>
    <#list list as option>
      <#if default?seq_contains(option.get(listKey))><#assign selected = "selected=\"selected\""><#else><#assign selected = ""></#if>
      <option ${selected} value="${option.get(listKey)}">
        <#if displayField==""><#nested option><#else>${option.get(displayField)?if_exists}</#if>
      </option>
    </#list>
  </select>
  <#if tooltip?has_content><@displayTooltip text=tooltip/></#if>
  <#if errorField?has_content><@displayError name=errorField index=index /></#if>
  </td>
</#macro>

<#macro inputMultiSelectRow title name list key="" displayField="" default=[] index=-1 defaultOptionText="" titleClass="tableheadtext" size=5 onChange="" id="" errorField="" tabIndex="" colspan="" tooltip="">
  <#if key == ""><#assign listKey = name><#else><#assign listKey = key></#if>
  <#if id == ""><#assign idVal = name><#else><#assign idVal = id></#if>
  <tr>
  <td class="titleCellTop"><span class="${titleClass}">${title}</span></td>
  <td<#if colspan?has_content> colspan="${colspan}"</#if>>
  <select id="${getIndexedName(idVal, index)}" name="${getIndexedName(name, index)}" class="inputBox" multiple="multiple" size="${size}" style="height:auto" onChange="${onChange}" <#if tabIndex?has_content>tabindex="${tabIndex}"</#if>>
    <#list list as option>
      <#if default?seq_contains(option.get(listKey))><#assign selected = "selected=\"selected\""><#else><#assign selected = ""></#if>
      <option ${selected} value="${option.get(listKey)}">
        <#if displayField==""><#nested option><#else>${option.get(displayField)?if_exists}</#if>
      </option>
    </#list>
  </select>
  <#if tooltip?has_content><@displayTooltip text=tooltip/></#if>
  <#if errorField?has_content><@displayError name=errorField index=index /></#if>
  </td>
  </tr>
</#macro>


<#macro inputLookup name lookup form default="" size=20 maxlength=20 index=-1 onChange="" ignoreParameters=false errorField="" tabIndex="" readonly=false tooltip="">
  <#assign indexedName = getIndexedName(name, index)/>
  <input type="text" size="${size}" maxlength="${maxlength}" name="${indexedName}" class="inputBox" value="${getDefaultValue(name, default, index, ignoreParameters)}" onChange="${onChange}" <#if tabIndex?has_content>tabindex="${tabIndex}"</#if> <#if readonly>readonly="readonly"</#if>/>
  <#if !readonly>
    <a name="${name}l" id="${name}l" href="javascript:call_fieldlookup2(document.${form}.${indexedName},'${lookup}');"><img src="/images/fieldlookup.gif" alt="Lookup" border="0" height="14" width="15"></a>
  </#if>
  <#if tooltip?has_content><@displayTooltip text=tooltip/></#if>
  <#if errorField?has_content><@displayError name=errorField index=index /></#if>
</#macro>

<#macro inputLookupCell name lookup form default="" size=20 maxlength=20 index=-1 onChange="" ignoreParameters=false errorField="" tabIndex="" readonly=false colspan="" tooltip="">
<td nowrap="nowrap"<#if colspan?has_content> colspan="${colspan}"</#if>><@inputLookup name=name lookup=lookup form=form default=default size=size maxlength=maxlength index=index onChange=onChange ignoreParameters=ignoreParameters errorField=errorField tabIndex=tabIndex readonly=readonly tooltip=tooltip/></td>
</#macro>

<#macro inputLookupRow name title lookup form size=20 maxlength=20 default="" titleClass="tableheadtext" onChange="" ignoreParameters=false errorField="" tabIndex="" readonly=false colspan="" tooltip="">
  <tr>
    <td class="titleCell"><span class="${titleClass}">${title}</span></td>
    <@inputLookupCell name=name lookup=lookup form=form default=default size=size maxlength=maxlength index=index onChange=onChange ignoreParameters=ignoreParameters errorField=errorField tabIndex=tabIndex readonly=readonly colspan=colspan tooltip=tooltip/>
  </tr>
</#macro>


<#-- auto complete -->

<#-- TODO: the way ID is handled here should be done for all macros.  We can remove the form parameter if we use id. -->
<#macro inputAutoComplete name url form="" lookup="" styleClass="inputAutoCompleteQuick" id="" default="" index=-1 size=15 maxlength=20 ignoreParameters=false errorField="" tabIndex="" onChange="" tooltip="" filtro="" >
  <#assign indexedName = getIndexedName(name, index)/>
  <#assign realId = id/>
  <#if !realId?has_content>
    <#assign realId = indexedName/>
  </#if>
  <#assign defaultValue = getDefaultValue(name, default, index, ignoreParameters)/>
  <div dojoType="dojo.data.JsonItemStoreAutoComplete" 
    jsId="${realId}ComboBoxStore"
    url="${url}"
    style="display:inline">
  </div>  
  <input dojoType="dijit.form.ComboBox"
    store="${realId}ComboBoxStore"
	filtro="${filtro}"
    id="ComboBox_${realId}"
    hiddenId="${realId}"
    name="ComboBox_${indexedName}"
    hasDownArrow="false"
    autoComplete="false"
    searchAttr="name"
    searchDelay="1000"
    pageSize="15"
    size="${size}"
    maxlength="${maxlength}"
    value="${defaultValue}"
    type="text"
    cambio="${onChange}"
    <#if tabIndex?has_content>tabindex="${tabIndex}"</#if>/>
  <input type="hidden" name="${indexedName}" id="${realId}" value="${defaultValue}" />

  <#if lookup?has_content && realId?has_content>
    <#assign comboElement = "document.getElementById('ComboBox_${realId}')"/>
    <#assign formElement = "document.getElementById('${realId}')"/>
    <a href="javascript:call_fieldlookup2autocomplete(${comboElement},${formElement},'${lookup}');"><img src="/images/fieldlookup.gif" alt="Lookup" border="0" height="14" width="15"/></a>
  </#if>
  <span class="tooltip" id="description_${realId}" ></span>
  <#if tooltip?has_content><@displayTooltip text=tooltip/></#if>
  <#if errorField?has_content><@displayError name=errorField index=index /></#if>
</#macro>

<#-- auto complete any Party -->
<#macro inputAutoCompleteParty name id="" url="getAutoCompletePartyIds" lookup="LookupPartyName" styleClass="inputAutoCompleteQuick" form="" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="" filtro="">
  <@inputAutoComplete name=name form=form url=url id=id lookup="LookupPartyName" styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip filtro=filtro/>
</#macro>

<#macro inputAutoCompletePartyCell name id="" url="getAutoCompletePartyIds" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="" filtro="">
  <td><@inputAutoCompleteParty name=name id=id url=url styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip filtro=filtro /></td>
</#macro>

<#macro inputAutoCompletePartyRow title name titleClass="tableheadtext" id="" url="getAutoCompletePartyIds" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" filtro="">
  <tr>
    <td class="titleCell"><span class="${titleClass}">${title}</span></td>
    <@inputAutoCompletePartyCell name=name id=id url=url styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip filtro=filtro/>
  </tr>
</#macro>

<#macro inputAutoCompletePartyRowCustom title name titleClass="tableheadtext" id="" url="getAutoCompletePartyIds" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" filtro="">
    <td class="titleCell"><span class="${titleClass}">${title}</span></td>
    <@inputAutoCompletePartyCell name=name id=id url=url styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip filtro=filtro/>
</#macro>

<#-- auto complete any PartyGroup -->
<#macro inputAutoCompletePartyGroup name id="" url="getAutoCompletePartyGroupIds" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="">
  <@inputAutoComplete name=name url=url id=id lookup="LookupPartyGroup" styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/>
</#macro>

<#macro inputAutoCompletePartyGroupCell name id="" url="getAutoCompletePartyGroupIds" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="">
  <td><@inputAutoCompletePartyGroup name=name id=id url=url styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/></td>
</#macro>

<#macro inputAutoCompletePartyGroupRow title name titleClass="tableheadtext" id="" url="getAutoCompletePartyGroupIds" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="">
  <tr>
    <td class="titleCell"><span class="${titleClass}">${title}</span></td>
    <@inputAutoCompletePartyGroupCell name=name id=id url=url styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/>
  </tr>
</#macro>

<#-- auto complete any Person -->
<#macro inputAutoCompletePerson name id="" url="getAutoCompletePersonIds" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="">
  <@inputAutoComplete name=name url=url id=id lookup="LookupPartyName" styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/>
</#macro>

<#macro inputAutoCompletePersonCell name id="" url="getAutoCompletePersonIds" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="">
  <td><@inputAutoCompletePerson name=name id=id url=url styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/></td>
</#macro>

<#macro inputAutoCompletePersonRow title name titleClass="tableheadtext" id="" url="getAutoCompletePersonIds" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="">
  <tr>
    <td class="titleCell"><span class="${titleClass}">${title}</span></td>
    <@inputAutoCompletePersonCell name=name id=id url=url styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/>
  </tr>
</#macro>

<#-- auto complete any Party with User Login -->
<#macro inputAutoCompleteUserLoginParty name id="" url="getAutoCompleteUserLoginPartyIds" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="">
  <@inputAutoComplete name=name url=url id=id lookup="LookupUserLoginAndPartyDetails" styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/>
</#macro>

<#macro inputAutoCompleteUserLoginPartyCell name id="" url="getAutoCompleteUserLoginPartyIds" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="">
  <td><@inputAutoCompleteUserLoginParty name=name id=id url=url styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/></td>
</#macro>

<#macro inputAutoCompleteUserLoginPartyRow title name titleClass="tableheadtext" id="" url="getAutoCompleteUserLoginPartyIds" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="">
  <tr>
    <td class="titleCell"><span class="${titleClass}">${title}</span></td>
    <@inputAutoCompleteUserLoginPartyCell name=name id=id url=url styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/>
  </tr>
</#macro>

<#-- auto complete any CRM Party (contact, accout, lead, partner) -->
<#macro inputAutoCompleteCrmParty name id="" url="getAutoCompleteCrmPartyIds" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="">
  <@inputAutoComplete name=name url=url id=id lookup="LookupPartyName" styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/>
</#macro>

<#macro inputAutoCompleteCrmPartyCell name id="" url="getAutoCompleteCrmPartyIds" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="">
  <td><@inputAutoCompleteCrmParty name=name id=id url=url styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/></td>
</#macro>

<#macro inputAutoCompleteCrmPartyRow title name titleClass="tableheadtext" id="" url="getAutoCompleteCrmPartyIds" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="">
  <tr>
    <td class="titleCell"><span class="${titleClass}">${title}</span></td>
    <@inputAutoCompleteCrmPartyCell name=name id=id url=url styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/>
  </tr>
</#macro>

<#-- auto complete Supplier -->
<#macro inputAutoCompleteSupplier name id="" url="getAutoCompleteSupplierPartyIds" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="">
  <@inputAutoComplete name=name url=url id=id lookup="LookupSupplier" styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/>
</#macro>

<#macro inputAutoCompleteSupplierCell name id="" url="getAutoCompleteSupplierPartyIds" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="">
  <td><@inputAutoCompleteSupplier name=name id=id url=url styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/></td>
</#macro>

<#macro inputAutoCompleteSupplierRow title name titleClass="tableheadtext" id="" url="getAutoCompleteSupplierPartyIds" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="">
  <tr>
    <td class="titleCell"><span class="${titleClass}">${title}</span></td>
    <@inputAutoCompleteSupplierCell name=name id=id url=url styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/>
  </tr>
</#macro>

<#-- auto complete Account, Contact and Prospect -->
<#macro inputAutoCompleteClient name id="" url="getAutoCompleteClientPartyIds" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="">
  <@inputAutoComplete name=name url=url id=id lookup="LookupClients" styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/>
</#macro>

<#macro inputAutoCompleteClientCell name id="" url="getAutoCompleteClientPartyIds" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="">
  <td><@inputAutoCompleteAccount name=name id=id url=url styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/></td>
</#macro>

<#macro inputAutoCompleteClientRow title name titleClass="tableheadtext" id="" url="getAutoCompleteClientPartyIds" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="">
  <tr>
    <td class="titleCell"><span class="${titleClass}">${title}</span></td>
    <@inputAutoCompleteAccountCell name=name id=id url=url styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/>
  </tr>
</#macro>

<#-- auto complete Account -->
<#macro inputAutoCompleteAccount name id="" url="getAutoCompleteAccountPartyIds" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="">
  <@inputAutoComplete name=name url=url id=id  lookup="LookupAccounts" styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/>
</#macro>

<#macro inputAutoCompleteAccountCell name id="" url="getAutoCompleteAccountPartyIds" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="">
  <td><@inputAutoCompleteAccount name=name id=id url=url styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/></td>
</#macro>

<#macro inputAutoCompleteAccountRow title name titleClass="tableheadtext" id="" url="getAutoCompleteAccountPartyIds" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="">
  <tr>
    <td class="titleCell"><span class="${titleClass}">${title}</span></td>
    <@inputAutoCompleteAccountCell name=name id=id url=url styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/>
  </tr>
</#macro>

<#-- auto complete GlAccount -->
<#macro inputAutoCompleteGlAccount name id="" url="getAutoCompleteGlAccounts" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="">
  <@inputAutoComplete name=name url=url id=id lookup="LookupGlAccount" styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/>
</#macro>

<#macro inputAutoCompleteGlAccountCell name id="" url="getAutoCompleteGlAccounts" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="">
  <td><@inputAutoCompleteGlAccount name=name id=id url=url styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/></td>
</#macro>

<#macro inputAutoCompleteGlAccountRow title name titleClass="tableheadtext" id="" url="getAutoCompleteGlAccounts" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="">
  <tr>
    <td class="titleCell"><span class="${titleClass}">${title}</span></td>
    <@inputAutoCompleteGlAccountCell name=name id=id url=url styleClass=styleClass default=default index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/>
  </tr>
</#macro>

<#-- auto complete Product -->
<#macro inputAutoCompleteProduct name url="getAutoCompleteProduct" id="" form="" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 maxlength=50 errorField="" tabIndex="" onChange="" tooltip="">
  <@inputAutoComplete name=name url=url id=id form=form lookup="LookupProduct" styleClass=styleClass default=default index=index size=size maxlength=maxlength errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/>
</#macro>

<#macro inputAutoCompleteProductCell name url="getAutoCompleteProduct" id="" form="" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 maxlength=50 errorField="" tabIndex="" onChange="" tooltip="">
  <td><@inputAutoCompleteProduct name=name id=id url=url styleClass=styleClass default=default index=index size=size maxlength=maxlength errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/></td>
</#macro>

<#macro inputAutoCompleteProductRow title name titleClass="tableheadtext" url="getAutoCompleteProduct" id="" form="" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 maxlength=50 errorField="" tabIndex="" onChange="" tooltip="">
  <tr>
    <td class="titleCell"><span class="${titleClass}">${title}</span></td>
    <@inputAutoCompleteProductCell name=name id=id url=url styleClass=styleClass default=default index=index size=size maxlength=maxlength errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/>
  </tr>
</#macro>

<#-- auto complete Zona Geografica -->
<#macro inputAutoCompleteZonaGeo name url="getZonaGeografica" id="" form="" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 maxlength=50 errorField="" tabIndex="" onChange="" tooltip="">
  <@inputAutoComplete name=name url=url id=id form=form styleClass=styleClass default=default index=index size=size maxlength=maxlength errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/>
</#macro>

<#macro inputAutoCompleteZonaGeoCell name url="getZonaGeografica" id="" form="" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 maxlength=50 errorField="" tabIndex="" onChange="" tooltip="">
  <td><@inputAutoCompleteZonaGeo name=name id=id url=url styleClass=styleClass default=default index=index size=size maxlength=maxlength errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/></td>
</#macro>

<#macro inputAutoCompleteZonaGeoRow title name titleClass="tableheadtext" url="getZonaGeografica" id="" form="" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 maxlength=50 errorField="" tabIndex="" onChange="" tooltip="">
  <tr>
    <td class="titleCell"><span class="${titleClass}">${title}</span></td>
    <@inputAutoCompleteZonaGeoCell name=name id=id url=url styleClass=styleClass default=default index=index size=size maxlength=maxlength errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/>
  </tr>
</#macro>

<#-- autocomplete CuentaBancaria: Consulta las cuentas activas y asociadas al ususario -->
<#macro inputAutoCompleteCuentaBanco name url="getCuentaBancoUsuario" id="" form="" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 maxlength=50 errorField="" tabIndex="" onChange="" tooltip="">
  <@inputAutoComplete name=name url=url id=id form=form styleClass=styleClass default=default index=index size=size maxlength=maxlength errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/>
</#macro>

<#macro inputAutoCompleteCuentaBancoCell name url="getCuentaBancoUsuario" id="" form="" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 maxlength=50 errorField="" tabIndex="" onChange="" tooltip="">
  <td><@inputAutoCompleteCuentaBanco name=name id=id url=url styleClass=styleClass default=default index=index size=size maxlength=maxlength errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/></td>
</#macro>

<#macro inputAutoCompleteCuentaBancoRow title name titleClass="tableheadtext" url="getCuentaBancoUsuario" id="" form="" styleClass="inputAutoCompleteQuick" default="" index=-1 size=15 maxlength=50 errorField="" tabIndex="" onChange="" tooltip="">
  <tr>
    <td class="titleCell"><span class="${titleClass}">${title}</span></td>
    <@inputAutoCompleteCuentaBancoCell name=name id=id url=url styleClass=styleClass default=default index=index size=size maxlength=maxlength errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip/>
  </tr>
</#macro>


<#macro inputCurrencySelect list=[] name="currencyUomId" defaultCurrencyUomId="" id="" index=-1 useDescription=false ignoreParameters=false tooltip="">
  <#if id == ""><#assign idVal = name><#else><#assign idVal = id></#if>
  <#assign currencyDefault = getDefaultValue(name, defaultCurrencyUomId, index, ignoreParameters)>
  <#if currencyDefault?length == 0>
    <#assign currencyDefault = Static["org.opentaps.common.util.UtilConfig"].getPropertyValue(opentapsApplicationName?default("opentaps"), "defaultCurrencyUomId")?default("USA") />
  </#if>
  <#assign currencies = list />
  <#if currencies?size == 0><#assign currencies = Static["org.opentaps.common.util.UtilCommon"].getCurrencies(delegator) /></#if>
  <select id="${getIndexedName(idVal, index)}" name="${getIndexedName(name, index)}" class="inputBox">
    <#list currencies as option>
      <#if option.uomId == currencyDefault><#assign selected = "selected=\"selected\""><#else><#assign selected = ""></#if>
      <option ${selected} value="${option.uomId}">${option.abbreviation}<#if useDescription> - ${option.description}</#if></option>
    </#list>
  </select>
  <#if tooltip?has_content><@displayTooltip text=tooltip/></#if>
</#macro>

<#macro inputCurrencySelectCell name="currencyUomId" list=[] defaultCurrencyUomId="" id="" index=-1 useDescription=false ignoreParameters=false tooltip="">
  <td nowrap="nowrap"><@inputCurrencySelect name=name id=id list=list defaultCurrencyUomId=defaultCurrencyUomId index=index useDescription=useDescription ignoreParameters=ignoreParameters tooltip=tooltip/></td>
</#macro>

<#macro inputCurrencySelectRow title name="currencyUomId" list=[] titleClass="tableheadtext" defaultCurrencyUomId="" id="" index=-1 useDescription=false ignoreParameters=false tooltip="">
  <tr>
    <td class="titleCell"><span class="${titleClass}">${title}</span></td>
    <@inputCurrencySelectCell name=name id=id list=list defaultCurrencyUomId=defaultCurrencyUomId index=index useDescription=useDescription ignoreParameters=ignoreParameters tooltip=tooltip/>
  </tr>
</#macro>


<#macro inputCurrency name list=[] currencyName="currencyUomId" default="" defaultCurrencyUomId="" disableCurrencySelect=false index=-1 ignoreParameters=false tooltip="" onChange="" id="">
<#assign formatoNum = "validaFormatoNumero(this);${onChange}"/>
  <input name="${getIndexedName(name, index)}" type="text" size="6" class="inputBox" value="${getDefaultValue(name, default, index, ignoreParameters)}" onChange="${formatoNum}" id="${id}"/>
  <#if disableCurrencySelect && defaultCurrencyUomId?length != 0>
    <input type="hidden" name="${getIndexedName(currencyName, index)}" value="${defaultCurrencyUomId}"/>
   ${defaultCurrencyUomId}
  <#else>
	<#if !disableCurrencySelect>
		<@inputCurrencySelect list=list name=currencyName defaultCurrencyUomId=defaultCurrencyUomId index=index ignoreParameters=ignoreParameters tooltip=tooltip/>
	</#if>
  </#if>
</#macro>

<#macro inputCurrencyCell name list=[] currencyName="currencyUomId" default="" defaultCurrencyUomId="" disableCurrencySelect=false index=-1 ignoreParameters=false tooltip="" onChange="" align="" id="">
  <td nowrap="nowrap" align="${align}"><@inputCurrency name=name list=list currencyName=currencyName default=default defaultCurrencyUomId=defaultCurrencyUomId
											 disableCurrencySelect=disableCurrencySelect index=index ignoreParameters=ignoreParameters tooltip=tooltip onChange=onChange id=id /></td>
</#macro>

<#macro inputCurrencyRow name title list=[] currencyName="currencyUomId" default="" titleClass="tableheadtext" defaultCurrencyUomId="" disableCurrencySelect=false index=-1 
		ignoreParameters=false tooltip="" id="" onChange="">
  <tr>
    <td class="titleCell"><span class="${titleClass}">${title}</span></td>
    <@inputCurrencyCell name=name list=list currencyName=currencyName default=default defaultCurrencyUomId=defaultCurrencyUomId 
			disableCurrencySelect=disableCurrencySelect index=index ignoreParameters=ignoreParameters tooltip=tooltip id=id onChange=onChange/>
  </tr>
</#macro>

<#macro inputCurrencyRangeCell fromName thruName ignoreParameters=false tooltip="" defaultCurrencyUomId="">
  <td><@inputCurrencyRange fromName=fromName thruName=thruName ignoreParameters=ignoreParameters tooltip=tooltip defaultCurrencyUomId=defaultCurrencyUomId/></td>
</#macro>

<#macro inputCurrencyRange fromName thruName size=20 ignoreParameters=false tooltip="" defaultCurrencyUomId="">
  <span class="tabletext">
    ${uiLabelMap.CommonFrom} 
    <@inputCurrency name=fromName ignoreParameters=ignoreParameters defaultCurrencyUomId=defaultCurrencyUomId/>
    &nbsp;&nbsp;
    ${uiLabelMap.CommonThru} 
    <@inputCurrency name=thruName ignoreParameters=ignoreParameters defaultCurrencyUomId=defaultCurrencyUomId/>
  </span>
  <#if tooltip?has_content><@displayTooltip text=tooltip/></#if>
</#macro>

<#macro inputSubmitIndexed title index onClick="submitFormWithSingleClick(this)" class="smallSubmit">
  <input type="submit" value="${title}" class="${class}" onclick="opentaps.markRowForSubmit(form, ${index});${onClick};"/>
</#macro>

<#macro inputSubmitIndexedCell title index onClick="submitFormWithSingleClick(this)" class="smallSubmit" blockClass="">
  <td class="${blockClass}"><input type="submit" value="${title}" class="${class}" onclick="opentaps.markRowForSubmit(form, ${index});${onClick};"/></td>
</#macro>

<#macro inputSubmit title onClick="submitFormWithSingleClick(this)" class="smallSubmit" tabIndex="">
  <input type="submit" value="${title}" class="${class}" onclick="${onClick}" <#if tabIndex?has_content>tabindex="${tabIndex}"</#if>/>
</#macro>

<#macro inputSubmitCell title onClick="submitFormWithSingleClick(this)" blockClass="" class="smallSubmit" tabIndex="">
  <td class="${blockClass}"><@inputSubmit title=title onClick=onClick class=class tabIndex=tabIndex /></td>
</#macro>

<#macro inputSubmitRow title onClick="submitFormWithSingleClick(this)" blockClass="" class="smallSubmit" tabIndex="">
  <tr>
    <td>&nbsp;</td>
    <@inputSubmitCell title=title onClick=onClick class=class blockClass=blockClass tabIndex=tabIndex />
  </tr>
</#macro>


<#macro inputButton title onClick="" id="">
  <#assign idText = id?has_content?string("id=\"" + id + "\"", "")/>
  <input type="submit" value="${title}" class="smallSubmit" onclick="${onClick}" ${idText} />
</#macro>

<#macro inputButtonCell title onClick="" id="">
  <td><@inputButton title=title onClick=onClick id=id /></td>
</#macro>

<#macro inputButtonRow title onClick="" id="" colspan="1">
  <tr>
    <td colspan="${colspan}">&nbsp;</td>
    <@inputButtonCell title=title onClick=onClick id=id />
  </tr>
</#macro>

<#macro inputButtonNoSubmit title onClick="" id="" >
  <#assign idText = id?has_content?string("id=\"" + id + "\"", "")/>
  <input type="button" value="${title}" class="smallSubmit" onClick="${onClick}" ${idText} />
</#macro>

<#macro inputButtonNoSubmitCell title onClick="" id="">
  <td><@inputButtonNoSubmit title=title onClick=onClick id=id /></td>
</#macro>

<#macro inputButtonNoSubmitRow title onClick="" id="" colspan="1">
  <tr>
    <td colspan="${colspan}">&nbsp;</td>
    <@inputButtonNoSubmitCell title=title onClick=onClick id=id />
  </tr>
</#macro>

<#macro inputButtonNoSubmitConfirm title onClick="" id="" >
  <#assign idText = id?has_content?string("id=\"" + id + "\"", "")/>
  <input type="button" value="${title}" class="buttonDangerous" onClick="if(confirm('${uiLabelMap.OpentapsAreYouSure}'))${onClick}" ${idText} />
</#macro>

<#macro inputButtonNoSubmitConfirmCell title onClick="" id="">
  <td><@inputButtonNoSubmitConfirm title=title onClick=onClick id=id /></td>
</#macro>

<#macro inputButtonNoSubmitConfirmRow title onClick="" id="" colspan="1">
  <tr>
    <td colspan="${colspan}">&nbsp;</td>
    <@inputButtonNoSubmitConfirmCell title=title onClick=onClick id=id />
  </tr>
</#macro>

<#macro inputIndicator name required=true default="" index=-1 onChange="" id="" ignoreParameters=false yesLabel=uiLabelMap.CommonYes noLabel=uiLabelMap.CommonNo tooltip="">
  <#assign defaultValue = getDefaultValue(name, default, index, ignoreParameters)>
  <select name="${getIndexedName(name, index)}" class="inputBox" onChange="${onChange}" id="${id}">
    <#if !required><option value=""></option></#if>
    <option <#if defaultValue == "Y">selected="selected"</#if> value="Y">${yesLabel}</option>
    <option <#if defaultValue == "N">selected="selected"</#if> value="N">${noLabel}</option>
  </select>
  <#if tooltip?has_content><@displayTooltip text=tooltip/></#if>
</#macro>

<#macro inputIndicatorCell name required=true default="" index=-1 onChange=""  id=""ignoreParameters=false yesLabel=uiLabelMap.CommonYes noLabel=uiLabelMap.CommonNo tooltip="">
  <td><@inputIndicator name=name required=required default=default index=index onChange=onChange id=id ignoreParameters=ignoreParameters yesLabel=yesLabel noLabel=noLabel tooltip=tooltip/></td>
</#macro>

<#macro inputIndicatorRow name title required=true default="" titleClass="tableheadtext" index=-1 onChange="" id="" ignoreParameters=false yesLabel=uiLabelMap.CommonYes noLabel=uiLabelMap.CommonNo tooltip="">
  <tr>
    <td class="titleCell"><span class="${titleClass}">${title}</span></td>
    <@inputIndicatorCell name=name required=required default=default index=index onChange=onChange id=id ignoreParameters=ignoreParameters yesLabel=yesLabel noLabel=noLabel tooltip=tooltip/>
  </tr>
</#macro>

<#-- Parameter 'form' is decreated and leaves here for compatibility w/ existent code. Don't use it any more. -->
<#macro inputDate name form="" default="" size=10 index=-1 popup=true weekNumbers=false onChange="" onUpdate="" onDateStatusFunc="" linkedName="" delta=0 id="" ignoreParameters=false errorField="" tabIndex="" calendarTabIndex="" tooltip="">
  <#assign indexedName = getIndexedName(name, index)>
  <#assign defaultValue = getDefaultValue(name, default, index, ignoreParameters)>
  <#assign elId = form + indexedName />
  <#if id?has_content><#assign elId = id /></#if>
  <input id="${elId}" type="text" size="${size}" maxlength="${size}" name="${indexedName}" class="inputBox" value="${getLocalizedDate(defaultValue, "DATE")}" onchange="${onChange}" <#if tabIndex?has_content>tabindex="${tabIndex}"</#if>/>
  <a href="javascript:opentaps.toggleClass(document.getElementById('${elId}-calendar-placeholder'), 'hidden');"><img id="${elId}-button" src="/images/cal.gif" alt="View Calendar" title="View Calendar" border="0" height="16" width="16" <#if tabIndex?has_content>tabindex="${calendarTabIndex}"</#if>/></a>
  <#if !popup><div id="${elId}-calendar-placeholder" style="border: 0px; width: auto;" class="hidden"></div></#if>
  <#if tooltip?has_content><@displayTooltip text=tooltip/></#if>
  <#if errorField?has_content><@displayError name=errorField index=index /></#if>
  <script type="text/javascript">
  /*<![CDATA[*/
    function ${elId}_onDateChange(calendar) {
      if (calendar.dateClicked) {
        var input = document.getElementById('${elId}');
        if (input) {
          input.value = opentaps.formatDate(calendar.date, '${StringUtil.wrapString(Static["org.ofbiz.base.util.UtilDateTime"].getJsDateTimeFormat(Static["org.ofbiz.base.util.UtilDateTime"].getDateFormat(locale)))}');
        }
        opentaps.addClass(document.getElementById('${elId}-calendar-placeholder'), 'hidden');
      }
    };
    <#if linkedName?has_content && !onUpdate?has_content>
    function ${elId}_calcAndApplyDifference(calendar) {
      var linkedInput = document.getElementById('${linkedName}');
      if (!linkedInput || linkedInput.nodeName != 'INPUT') {
        alert('Linked field with name ${linkedName} isn\'t accessible or have wrong type!');
        return;
      }
      
      var date = calendar.date;
      var time = date.getTime();
      time += (Date.DAY * ${delta});
      
      var linkedDate = new Date(time);
      linkedInput.value = opentaps.formatDate(linkedDate, '${StringUtil.wrapString(Static["org.ofbiz.base.util.UtilDate"].getJsDateTimeFormat(Static["org.ofbiz.base.util.UtilDateTime"].getDateFormat(locale)))}');
    };
    </#if>
    Calendar.setup(
      {
      <#if !popup>
        flat: "${elId}-calendar-placeholder",
        flatCallback: ${elId}_onDateChange,
      <#else>
        inputField: "${elId}",
        ifFormat: "${StringUtil.wrapString(Static["org.ofbiz.base.util.UtilDateTime"].getJsDateTimeFormat(Static["org.ofbiz.base.util.UtilDateTime"].getDateFormat(locale)))}",
        button: "${elId}-button",
        align: "Bl",
      </#if>
      <#if onUpdate?has_content>
        onUpdate: ${onUpdate},
      </#if>
      <#if linkedName?has_content && !onUpdate?has_content>
        onUpdate: ${elId}_calcAndApplyDifference,
      </#if>
      <#if weekNumbers?is_boolean>
        weekNumbers: <#if weekNumbers>true<#else>false</#if>,
      </#if>
        showOthers: true,
        cache: true
      }
    );
  /*]]>*/
  </script>
</#macro>

<#macro inputDateCell name form="" default="" size=10 index=-1 popup=true weekNumbers=false onUpdate="" onDateStatusFunc="" linkedName="" delta=0 id="" ignoreParameters=false errorField="" tabIndex="" calendarTabIndex="" tooltip="">
  <td><@inputDate name=name form=form default=default size=size index=index popup=popup weekNumbers=weekNumbers onUpdate=onUpdate onDateStatusFunc=onDateStatusFunc linkedName=linkedName delta=delta id=id ignoreParameters=ignoreParameters errorField=errorField tabIndex=tabIndex calendarTabIndex=calendarTabIndex tooltip=tooltip/></td>
</#macro>

<#macro inputDateRow name title form="" titleClass="tableheadtext" default="" size=10 index=-1 popup=true weekNumbers=false onUpdate="" onDateStatusFunc="" linkedName="" delta=0 id="" ignoreParameters=false errorField="" tabIndex="" calendarTabIndex="" tooltip="">
  <tr>
    <@displayTitleCell title=title titleClass=titleClass/>
    <@inputDateCell name=name form=form default=default size=size index=index popup=popup weekNumbers=weekNumbers onUpdate=onUpdate onDateStatusFunc=onDateStatusFunc linkedName=linkedName delta=delta id=id ignoreParameters=ignoreParameters errorField=errorField tabIndex=tabIndex calendarTabIndex=calendarTabIndex tooltip=tooltip/>
  </tr>
</#macro>


<#macro inputDateRangeRow title fromName thruName titleClass="tableheadtext" ignoreParameters=false tooltip="" defaultFrom="" defaultThru="">
  <tr>
    <@displayTitleCell title=title titleClass=titleClass/>
    <@inputDateRangeCell fromName=fromName thruName=thruName ignoreParameters=ignoreParameters tooltip=tooltip defaultFrom=defaultFrom defaultThru=defaultThru/>
  </tr>
</#macro>

<#macro inputDateRangeCell fromName thruName ignoreParameters=false tooltip="" defaultFrom="" defaultThru="">
  <td><@inputDateRange fromName=fromName thruName=thruName ignoreParameters=ignoreParameters tooltip=tooltip defaultFrom=defaultFrom defaultThru=defaultThru/></td>
</#macro>

<#macro inputDateRange fromName thruName ignoreParameters=false tooltip="" defaultFrom="" defaultThru="">
  <span class="tabletext">
    ${uiLabelMap.CommonFrom} 
    <@inputDate name=fromName ignoreParameters=ignoreParameters default=defaultFrom/>
    &nbsp;&nbsp;
    ${uiLabelMap.CommonThru} 
    <@inputDate name=thruName ignoreParameters=ignoreParameters default=defaultThru/>
  </span>
  <#if tooltip?has_content><@displayTooltip text=tooltip/></#if>
</#macro>

<#-- Enumeration Macros -->

<#macro inputEnumeration name enumTypeId index=-1 required=false default="" onChange="" ignoreParameters=false tooltip="" id="">
  <#assign enumerations = Static["org.opentaps.common.util.UtilCommon"].getEnumerations(enumTypeId, delegator)>
  <#assign defaultValue = getDefaultValue(name, default, index, ignoreParameters)>
  <select name="${getIndexedName(name, index)}" class="inputBox" onChange="${onChange}" id="${id}">
    <#if !required><option value=""></option></#if>
    <#list enumerations as enum>
      <option <#if defaultValue == enum.enumId>selected="selected"</#if> value="${enum.enumId}">${enum.get("description", locale)}</option>
    </#list>
  </select>
  <#if tooltip?has_content><@displayTooltip text=tooltip/></#if>
</#macro>

<#macro inputEnumerationCell name enumTypeId index=-1 required=false default="" onChange="" ignoreParameters=false tooltip="" id="">
  <td><@inputEnumeration name=name enumTypeId=enumTypeId index=index required=required default=default onChange=onChange ignoreParameters=ignoreParameters tooltip=tooltip id=id/></td>
</#macro>

<#macro inputEnumerationRow title name enumTypeId index=-1 required=false titleClass="tableheadtext" default="" onChange="" ignoreParameters=false tooltip="" id="">
  <td class="titleCell"><span class="${titleClass}">${title}</span></td>
  <@inputEnumerationCell name=name enumTypeId=enumTypeId index=index required=required default=default onChange=onChange ignoreParameters=ignoreParameters tooltip=tooltip id=id/>
</#macro>

<#macro displayEnumeration enumId="" class="tabletext" style="">
  <span class="${class}" style="${style}"><#if enumId?has_content>${Static["org.opentaps.common.util.UtilCommon"].getEnumerationDescription(enumId, locale, delegator)}</#if></span>
</#macro>

<#macro displayEnumerationCell enumId="" class="tabletext" style="" blockStyle="" blockClass="">
  <td class="${blockClass}" style="${blockStyle}"><#if enumId?has_content><@displayEnumeration enumId=enumId class=class style=style /></#if></td>
</#macro>


<#-- ###################################################################### -->
<#-- ###                                                                 ## -->
<#-- ###              Larger "Building Block" Macros                     ## -->
<#-- ###                                                                 ## -->
<#-- ###################################################################### -->

<#-- Parameter 'form' is decreated and leaves here for compatibility w/ existent code. Don't use it any more. -->
<#macro inputDateTime name form="" default="" popup=true weekNumbers=false onUpdate="" onDateStatusFunc="" linkedName="" delta=0 ignoreParameters=false errorField="">
  <#assign defaultValue = getDefaultValue(name, default, -1, ignoreParameters)>
  <#assign defaultTime = Static["org.opentaps.common.util.UtilDate"].timestampToAmPm(getLocalizedDate(defaultValue, "DATE_TIME"), Static["org.ofbiz.base.util.UtilHttp"].getTimeZone(request), Static["org.ofbiz.base.util.UtilHttp"].getLocale(request)) />
  <input type="hidden" name="${name}_c_compositeType" value="Timestamp"/>
      <input id="${name}_c_date" type="text" class="inputBox" name="${name}_c_date" size="10" maxlength="10" value="${defaultTime.date?if_exists}"/>
      <#if defaultTime.date?exists>
        <#assign lookupDefault = default>
      <#else>
        <#assign lookupDefault = "">
      </#if>
      <a href="javascript:opentaps.toggleClass(document.getElementById('${name}-calendar-placeholder'), 'hidden');"><img id="${name}-button" src="/images/cal.gif" alt="View Calendar" title="View Calendar" border="0" height="16" width="16"/></a>
      &nbsp;
      <select name="${name}_c_hour" class="inputBox">
        <#list 1..12 as hour>
          <option <#if defaultTime.hour?default(12) == hour>selected="selected"</#if> value="${hour}">${hour}</option>
        </#list>
      </select>
      :
      <select name="${name}_c_minutes" class="inputBox">
        <#list 0..59 as min>
          <option <#if defaultTime.minute?default(-1) == min>selected="selected"</#if> value="${min}">${min?string?left_pad(2,"0")}</option>
        </#list>
      </select>
      <select name="${name}_c_ampm" class="inputBox">
        <option value="AM">AM</option>
        <option <#if defaultTime.ampm?default("") == "PM">selected="selected"</#if> value="PM">PM</option>
      </select>
      <table id="${name}-calendar-placeholder" style="border: 0px; width: auto;" class="hidden"></table>
      <#if errorField?has_content><@displayError name=errorField /></#if>
      <script type="text/javascript">
      /*<![CDATA[*/
        function ${name}_onDateChange(calendar) {
          if (calendar.dateClicked) {
            var input = document.getElementById('${name}_c_date');
            if (input) {
              input.value = opentaps.formatDate(calendar.date, '${StringUtil.wrapString(Static["org.ofbiz.base.util.UtilDateTime"].getJsDateTimeFormat(Static["org.ofbiz.base.util.UtilDateTime"].getDateFormat(locale)))}');
            }
            opentaps.addClass(document.getElementById('${name}-calendar-placeholder'), 'hidden');
          }
        };
        <#if linkedName?has_content && !onUpdate?has_content>
        function ${name}_calcAndApplyDifference(calendar) {
          var linkedInput = document.getElementById('${linkedName}_c_date');
          if (!linkedInput || linkedInput.nodeName != 'INPUT') {
            alert('Linked field with name ${linkedName} isn\'t accessible or have wrong type!');
            return;
          }
      
          var date = calendar.date;
          var time = date.getTime();
          time += (Date.DAY * ${delta});
      
          var linkedDate = new Date(time);
          linkedInput.value = opentaps.formatDate(linkedDate, '${StringUtil.wrapString(Static["org.ofbiz.base.util.UtilDate"].getJsDateTimeFormat(Static["org.ofbiz.base.util.UtilDateTime"].getDateFormat(locale)))}');
        };
        </#if>
        Calendar.setup(
          {
          <#if !popup>
            flat: "${name}-calendar-placeholder",
            flatCallback: ${name}_onDateChange,
          <#else>
            inputField: "${name}_c_date",
            ifFormat: "${StringUtil.wrapString(Static["org.ofbiz.base.util.UtilDateTime"].getJsDateTimeFormat(Static["org.ofbiz.base.util.UtilDateTime"].getDateFormat(locale)))}",
            button: "${name}-button",
            align: "Bl",
          </#if>
          <#if onUpdate?has_content>
            onUpdate: ${onUpdate},
          </#if>
          <#if linkedName?has_content && !onUpdate?has_content>
            onUpdate: ${name}_calcAndApplyDifference,
          </#if>
          <#if weekNumbers?is_boolean>
            weekNumbers: <#if weekNumbers>true<#else>false</#if>,
          </#if>
            showOthers: true,
            cache: true
          }
      );
      /*]]>*/
      </script>
</#macro>

<#macro inputDateTimeCell name form="" default="" popup=true weekNumbers=false onUpdate="" onDateStatusFunc="" linkedName="" delta=0 ignoreParameters=false errorField="">
  <td><@inputDateTime name=name form=form default=default popup=popup weekNumbers=weekNumbers onUpdate=onUpdate onDateStatusFunc=onDateStatusFunc linkedName=linkedName delta=delta ignoreParameters=ignoreParameters errorField=errorField/></td>
</#macro>

<#macro inputDateTimeRow name title form="" default="" titleClass="tableheadtext" popup=true weekNumbers=false onUpdate="" onDateStatusFunc="" linkedName="" delta=0 ignoreParameters=false errorField="">
  <tr>
    <td class="titleCell"><span class="${titleClass}">${title}</span></td>
    <@inputDateTimeCell name=name form=form default=default popup=popup weekNumbers=weekNumbers onUpdate=onUpdate onDateStatusFunc=onDateStatusFunc linkedName=linkedName delta=delta ignoreParameters=ignoreParameters errorField=errorField/>
  </tr>
</#macro>


<#macro inputSelectTaxAuthority list defaultGeoId="" defaultPartyId="" required=false>
    <select name="taxAuthGeoId" class="inputBox" onChange="opentaps.changeTaxParty(this.form.taxAuthGeoId, this.form.taxAuthPartyId)">
      <#if !required><option value=""></option></#if>
      <#list list as auth>
        <#if auth.taxAuthGeoId == defaultGeoId><#assign selected = "selected=\"selected\""><#else><#assign selected = ""></#if>
        <option ${selected} value="${auth.taxAuthGeoId}" taxAuthPartyId="${auth.taxAuthPartyId}">${auth.geoName}</option>
      </#list>
    </select>

    <select name="taxAuthPartyId" class="inputBox">
      <#if !required><option value=""></option></#if>
      <#list list as auth>
        <#if auth.taxAuthPartyId == defaultPartyId><#assign selected = "selected=\"selected\""><#else><#assign selected = ""></#if>
        <option ${selected} value="${auth.taxAuthPartyId}" taxAuthGeoId="${auth.taxAuthGeoId}">${auth.groupName}</option>
      </#list>
    </select>
</#macro>

<#macro inputSelectTaxAuthorityCell list defaultGeoId="" defaultPartyId="" required=false>
  <td><@inputSelectTaxAuthority list=list defaultGeoId=defaultGeoId defaultPartyId=defaultPartyId required=required /></td>
</#macro>


<#macro inputState name="stateProvinceGeoId" countryInputName="countryGeoId" address={}>
    <#if address?size != 0>
      <#assign defaultStateGeoId = address.stateProvinceGeoId?default("") />
      <#assign defaultCountryGeoId = address.countryGeoId?default(configProperties.defaultCountryGeoId) />
    <#else>
      <#assign defaultStateGeoId = parameters.get(name)?default("") />
      <#assign defaultCountryGeoId = parameters.get(countryInputName)?default(configProperties.defaultCountryGeoId) />
    </#if>
    <#assign defaultStates = Static["org.opentaps.common.util.UtilCommon"].getStates(delegator, defaultCountryGeoId) />
    <select name="${name}" id="${name}" class="selectBox">
        <option></option>
        <#list defaultStates as state>
            <#if defaultStateGeoId == state.geoId><#assign selected="selected=\"selected\""><#else><#assign selected=""></#if>
            <option ${selected} value="${state.geoId}">${state.get("geoName", locale)}</option>
        </#list>
    </select>
</#macro>
<#macro inputCountry name="countryGeoId" stateInputName="stateProvinceGeoId" address={} required=false>
    <#if address?size != 0>
      <#assign defaultCountryGeoId = address.countryGeoId?default(configProperties.defaultCountryGeoId) />
    <#else>
      <#assign defaultCountryGeoId = parameters.get(name)?default(configProperties.defaultCountryGeoId) />
    </#if>
    <#assign countries = Static["org.opentaps.common.util.UtilCommon"].getCountries(delegator) />
    <select name="${name}" id="${name}" class="selectBox" onChange="opentaps.swapStatesInDropdown(this, '${stateInputName}')">
        <#if !required><option value=""></option></#if>    
        <#list countries as country>
            <#if defaultCountryGeoId == country.geoId><#assign selected="selected=\"selected\""><#else><#assign selected=""></#if>
            <option ${selected} value="${country.geoId}">${country.get("geoName", locale)}</option>
        </#list>
    </select>
</#macro>
<#macro inputStateCountry stateInputName="stateProvinceGeoId" countryInputName="countryGeoId" address={} required=false>
  <@inputState name=stateInputName countryInputName=countryInputName address=address /><@inputCountry name=countryInputName stateInputName=stateInputName address=address />
</#macro>
<#macro inputStateCountryCell stateInputName="stateProvinceGeoId" countryInputName="countryGeoId" address={} required=false>
    <td><@inputState name=stateInputName countryInputName=countryInputName address=address /><@inputCountry name=countryInputName stateInputName=stateInputName address=address required=false/></td>
</#macro>
<#macro inputStateCountryRow title stateInputName="stateProvinceGeoId" countryInputName="countryGeoId" titleClass="titleClass" address={} required=false>
    <tr>
        <td class="titleCell"><span class="${titleClass}">${title}</span></td>
        <td><@inputState name=stateInputName countryInputName=countryInputName address=address /><@inputCountry name=countryInputName stateInputName=stateInputName address=address required=false/></td>
    </tr>
</#macro>

<#macro displayGeoName geoId >
  <@display text=Static["org.opentaps.common.util.UtilCommon"].getGeoName(geoId, locale, delegator) />
</#macro>
<#macro displayGeoCode geoId >
  <@display text=Static["org.opentaps.common.util.UtilCommon"].getGeoCode(geoId, locale, delegator) />
</#macro>
<#macro displayGeo geoId >
  <#assign geoName = Static["org.opentaps.common.util.UtilCommon"].getGeoName(geoId, locale, delegator) />
  <#assign geoCode = Static["org.opentaps.common.util.UtilCommon"].getGeoCode(geoId, locale, delegator) />
  <@display text="${geoCode} (${geoName})" />
</#macro>


<#-- This macro is squished because of the way the menu buttons work.  -->
<#macro inputConfirm title href="" form="" confirmText=uiLabelMap.OpentapsAreYouSure class="buttonDangerous" onClick="">
	<a class="${class}" href="javascript:<#if onClick!="">${onClick}; </#if>opentaps.confirmAction('${StringUtil.wrapString(confirmText)}', '${href}', '${form}',null,'${uiLabelMap.OpentapsOrderSubmittingLabel}')">${title}</a>
</#macro>

<#macro inputConfirmWarehouse title href="" form="" confirmText=uiLabelMap.OpentapsAreYouSureWarehouse class="buttonDangerous" onClick="">
	<a class="${class}" href="javascript:<#if onClick!="">${onClick}; </#if>opentaps.confirmAction('${StringUtil.wrapString(confirmText)}', '${href}', '${form}',null,'${uiLabelMap.OpentapsOrderSubmittingLabel}')">${title}</a>
</#macro>

<#macro inputConfirmImage title src href="" form="" confirmText=uiLabelMap.OpentapsAreYouSure class="" border="0">
	<a class="${class}" href="javascript:opentaps.confirmAction('${StringUtil.wrapString(confirmText)}', '${href}', '${form}',null,'${uiLabelMap.OpentapsOrderSubmittingLabel}')">
	<img src="${src}" alt="${title}" title="${title}" border="${border}" /></a>
</#macro>

<#macro inputConfirmCell title href="" form="" confirmText=uiLabelMap.OpentapsAreYouSure class="buttonDangerous">
  <td><@inputConfirm title=title href=href form=form confirmText=confirmText /></td>
</#macro>

<#macro inputForceComplete title forceTitle confirmText=uiLabelMap.OpentapsAreYouSure href="" form="">
  <#if href != "">
    <#if parameters.forceComplete?default("false") == "true">
      <@inputConfirm title=forceTitle confirmText=confirmText href=(href + "&amp;forceComplete=true") />
    <#else>
      <@displayLink href=href text=title class="buttontext" />
    </#if>
  <#elseif form != "">
    <#if parameters.forceComplete?default("false") == "true">
      <@inputHidden name="forceComplete" value="true"/>
      <@inputConfirm title=forceTitle confirmText=confirmText form=form />
    <#else>
      <@inputSubmit title=title />
    </#if>
  </#if>
</#macro>
<#macro inputForceCompleteCell title forceTitle confirmText=uiLabelMap.OpentapsAreYouSure href="" form="">
  <td><@inputForceComplete title=title forceTitle=forceTitle confirmText=confirmText href=href form=form /></td>
</#macro>
<#macro inputForceCompleteRow title forceTitle confirmText=uiLabelMap.OpentapsAreYouSure href="" form="">
  <tr>
    <td>&nbsp;</td>
    <td><@inputForceComplete title=title forceTitle=forceTitle confirmText=confirmText href=href form=form /></td>
  </tr>
</#macro>

<#-- Macro selectAction implements drop-down based menu widget                     -->
<#-- Should be used jointly with macro <@action />                                 -->
<#-- Example:                                                                      -->
<#--    <@selectAction name="ID">                                                  -->
<#--        <@action url="somewhere/someThing?parameter=value" text="Label Text"/> -->
<#--        etc ...                                                          -->
<#--    </@selectAction>                                                           -->

<#macro action url text selected=false>
  <#if url?starts_with("/")>
    <option value="${url}${externalKeyParam?if_exists}" <#if selected>selected="selected"</#if>>${text}</option>
  <#elseif url?starts_with("javascript:")>
    <option value="${url}" <#if selected>selected="selected"</#if>>${text}</option>
  <#else>
    <option value="<@ofbizUrl>${url}</@ofbizUrl>" <#if selected>selected="selected"</#if>>${text}</option>
  </#if>
</#macro>

<#macro selectAction name prompt=uiLabelMap.OpentapsDefaultActionPrompt>
  <select id="${name}" name="${name}" class="inputBox" onchange="opentaps.changeLocation(null, '${name}');">
    <option value="">${prompt}</option>
    <option value="">${uiLabelMap.OpentapsDefaultActionSeparator}</option>
    <#nested>
  </select>
</#macro>


<#-- Macro selectActionForm implements drop-down based menu widget                 -->
<#--  like selectAction but instead of changing the location, it will post the     -->
<#--  given form                                                                   -->
<#-- Should be used jointly with macro <@actionForm />                             -->
<#-- Example:                                                                      -->
<#--    <@selectActionForm name="ID">                                              -->
<#--        <@actionForm form="someFormName" text="Label Text"/>                   -->
<#--        etc ...                                                                -->
<#--    </@selectActionForm>                                                       -->

<#macro actionForm form text selected=false>
  <option value="${form}" <#if selected>selected="selected"</#if>>${text}</option>
</#macro>

<#macro selectActionForm name prompt=uiLabelMap.OpentapsDefaultActionPrompt>
  <select id="${name}" name="${name}" class="inputBox" onchange="opentaps.selectForm('${name}');">
    <option value="">${prompt}</option>
    <option value="">${uiLabelMap.OpentapsDefaultActionSeparator}</option>
    <#nested>
  </select>
</#macro>

<#macro form name url id="" method="post" target="" formParams...>
  <form method="${method}" action="${url}" name="${name}" <#if target?has_content>target="${target}"</#if> id=${id}>
    <#if formParams?has_content>
      <#list formParams?keys as param>
        <@inputHidden name="${param}" value="${formParams[param]}" />
      </#list>
    </#if>
    <#nested>
  </form>
</#macro>

<#macro submitFormLink form text class="buttontext" id="" style="" formParams...>
<#compress>
  <#assign extraParams = "null"/>
  <#if formParams?has_content>
    <#assign extraParams>{
      <#list formParams?keys as param>
        '${param}':'${formParams[param]}'
        <#if param_has_next>,</#if>
      </#list>
      }
    </#assign>
  </#if>
  <a class="${class}" <#if style?has_content> style="${style}"</#if> <#if id?has_content> id="${id}"</#if> href="javascript:opentaps.submitForm('${form}', null, ${extraParams});">${text}</a>
</#compress>
</#macro>

<#macro submitFormLinkConfirm form text class="buttonDangerous" id="" style="" confirmText=uiLabelMap.OpentapsAreYouSure formParams...>
<#compress>
  <#assign extraParams = "null"/>
  <#if formParams?has_content>
    <#assign extraParams>{
      <#list formParams?keys as param>
        '${param}':'${formParams[param]}'
        <#if param_has_next>,</#if>
      </#list>
      }
    </#assign>
  </#if>
  <a class="${class}" <#if style?has_content> style="${style}"</#if> <#if id?has_content> id="${id}"
			</#if> href="javascript:opentaps.confirmAction('${confirmText}', null, '${form}', ${extraParams},'${uiLabelMap.OpentapsOrderSubmittingLabel}');">${text}</a>
</#compress>
</#macro>

<#macro paginate name list="" rememberOrderBy=true rememberPage=true renderExcelButton=true params...>
  <@paginateTransform name=name list=list rememberOrderBy=rememberOrderBy rememberPage=rememberPage renderExcelButton=renderExcelButton context=context params=params><#nested></@paginateTransform>
</#macro>


<#-- ###################################################################### -->
<#-- ###                                                                 ## -->
<#-- ###                     Multi Form Macros                           ## -->
<#-- ###                                                                 ## -->
<#-- ###################################################################### -->
        

<#macro inputHiddenRowSubmit index submit=true>
    <input type="hidden" name="_rowSubmit_o_${index}" value="<#if submit>Y<#else>N</#if>"/>
</#macro>

<#macro inputMultiSelectAll form onClick="">
  <input type="checkbox" name="selectAll" value="N" onClick="javascript:toggleAll(this, '${form}'); ${onClick}"/>
</#macro>
<#macro inputMultiSelectAllCell form onClick="">
  <td><@inputMultiSelectAll form=form onClick=onClick /></td>
</#macro>

<#macro inputMultiCheck index onClick="">
  <input type="checkbox" name="_rowSubmit_o_${index}" value="N" onclick="${onClick}cambiarValorCheckBox(this);" />
</#macro>
<#macro inputMultiCheckCell index onClick="">
  <td><@inputMultiCheck index=index onClick=onClick /></td>
</#macro>

<#macro inputHiddenRowCount list>
  <@inputHidden name="_rowCount" value=list?size />
</#macro>

<#macro inputHiddenUseRowSubmit>
  <@inputHidden name="_useRowSubmit" value="Y" />
</#macro>


<#-- ###################################################################### -->
<#-- ###                                                                 ## -->
<#-- ###                      Global Functions                           ## -->
<#-- ###                                                                 ## -->
<#-- ###################################################################### -->


<#function tableRowClass rowIndex rowClassOdd="rowWhite" rowClassEven="rowLightGray">
  <#return (rowIndex % 2 == 0)?string(rowClassOdd, rowClassEven)/>
</#function>


<#-- ###################################################################### -->
<#-- ###                                                                 ## -->
<#-- ###                      Convenience Macros                         ## -->
<#-- ###                                                                 ## -->
<#-- ###################################################################### -->


<#macro tooltip text="" class="tooltip"><#if text?has_content><span class="tabletext"><span class="${class}">${text}</span></span></#if></#macro>


<#macro displayError name index=-1><@tooltip text=opentapsErrors.field.get(getIndexedName(name,index)) class="errortooltip" /></#macro>

<#macro pagination viewIndex viewSize currentResultSize requestName totalResultSize=-1 extraParameters="">
    <#if ( ! totalResultSize?has_content ) || (totalResultSize?has_content && totalResultSize != 0)>
        <#assign beginIndex = viewIndex/>
        <#if beginIndex == 0>
            <#assign beginIndex = 1/>
        </#if>
        <#assign endIndex = beginIndex + currentResultSize - 1/>
        <#if totalResultSize?has_content && totalResultSize != -1>
            <#assign paginationText = "${StringUtil.wrapString(uiLabelMap.OpentapsPaginationWithTotal)}"?interpret>
        <#else>
            <#assign paginationText = "${StringUtil.wrapString(uiLabelMap.OpentapsPaginationWithoutTotal)}"?interpret>
        </#if>
        <div class="pagination">
            <#if beginIndex &gt; 1>
                <span class="paginationPrevious"><a href="<@ofbizUrl>${requestName}?VIEW_INDEX=${beginIndex - viewSize}&amp;VIEW_SIZE=${viewSize}${extraParameters?html}</@ofbizUrl>">${uiLabelMap.CommonPrevious}</a></span>
            </#if>
            <span class="paginationText"><@paginationText/></span>
            <#if endIndex &lt; totalResultSize>
                <span class="paginationNext"><a href="<@ofbizUrl>${requestName}?VIEW_INDEX=${beginIndex + viewSize}&amp;VIEW_SIZE=${viewSize}${extraParameters?html}</@ofbizUrl>">${uiLabelMap.CommonNext}</a></span>
            </#if>
        </div>
    </#if>
</#macro>

<#macro htmlTextArea textAreaId name="" value="" tagFileLocation="" rows=20 cols=80 class="" style="">
  <textarea name="${name?has_content?string(name, textAreaId)}" id="${textAreaId}" class="${class}" rows="${rows}" cols="${cols}" style="${style}">${value}</textarea>
  <#if tagFileLocation?has_content>
    <@import location = tagFileLocation/>
  </#if>
  <script type="text/javascript" src="/opentaps_js/fckeditor/fckeditor.js"></script>
	<script type="text/javascript">
    opentaps.addOnLoad(function() {
      <#if tagFileLocation?has_content>
        tags = [];
        <#list getTags()?default([]) as tagMap>
          <#list tagMap?default({})?keys as tag>
            tags.push({ 'tag' : '${tag}' , 'description' : '${tagMap[tag]?js_string}' });
          </#list>
        </#list>
        insertTagsLabel = '${uiLabelMap.OpentapsHtmlEditorInsertTagsLabel?js_string}';
      </#if>
      insertTags = ${tagFileLocation?has_content?string};
      var oFCKeditor = new FCKeditor( '${textAreaId}' ) ;
      oFCKeditor.BasePath = '/opentaps_js/fckeditor/' ;
      oFCKeditor.Height	= 400 ;
      oFCKeditor.ToolbarSet = '${tagFileLocation?has_content?string("OpentapsFormMerge", "OpentapsBasic")}';
      oFCKeditor.ReplaceTextarea() ;
    });
	</script>
</#macro>

<#function formatTelecomNumber number={}>
  <#assign telecomNumber>
    <#if number.countryCode?has_content>${number.countryCode}&nbsp;</#if><#if number.areaCode?has_content>(${number.areaCode})&nbsp;</#if><#if number.contactNumber?has_content>${number.contactNumber}</#if>          
  </#assign>
  <#return telecomNumber/>
</#function>

<#function getDescriptionCombo entity displayField id>
	<#assign optionDisplay =''>
    <#if entity.class.simpleName == 'GenericValue'>
	  <#assign entityName = entity.getEntityName() />
      <#assign displayName = entityName+'.'+displayField+'.'+entity.get(id)?if_exists />
      <#assign description = uiLabelMap.get(displayName) />
      <#assign optionDisplay ><#if description == displayName>${entity.get(displayField)?if_exists}<#else>${description}</#if></#assign>
    <#else>
      <#assign optionDisplay>${entity.get(displayField)?if_exists}</#assign>
    </#if>
    <#return optionDisplay />
</#function>

<#macro glAccountTree glAccountTree treeId className="opentaps.GLAccountTree" defaultState="expanded">
<#assign isExpanded = (defaultState?lower_case == "expanded") />
    <script type="text/javascript">/*<![CDATA[*/

        function init_${treeId}() {

            var glAccountData = { identifier: 'glAccountId', label: 'name', items: ${StringUtil.wrapString(glAccountTree.toJSONString())}};
            var glAccountStore = new opentaps.GLAccountJsonStore({data:glAccountData});

            var glAccountTree = new ${className}({
                    id: '${treeId}',
                    store: glAccountStore,
                    query: {type:'root'},
                    labelAttr: "name",
                    typeAttr: "type",
                    defaultExpanded: ${isExpanded?string},
                    currencyUomId: '${StringUtil.wrapString(glAccountTree.getCurrencyUomId())}',
                    organizationPartyId: '${StringUtil.wrapString(glAccountTree.getOrganizationPartyId())}'
                    }, document.getElementById("${treeId}"));
        }

        opentaps.addOnLoad(init_${treeId});

    /*]]>*/</script>

    <div class="tabletext"><div id="${treeId}"></div></div>
</#macro>

<#-- GWT related macros -->

<#--
  loads a given gwt module and widget using a special notation docmented at
  http://opentaps.org/docs/index.php/Opentaps_Google_Web_Toolkit#Adding_a_GWT_Widget_to_a_Screen
-->
<#macro gwtModule widget>
  <#-- widget should be 'module/java_path' -->
  <#assign arr = widget.split("/")>
  <#if arr?size == 2>
    <#assign module=arr[0] />
    <#assign path=arr[1] />
    <script type="text/javascript" language="javascript" src="/${module}/${path}/${path}.nocache.js"></script>
  <#else>
    <@tooltip class="errortooltip" text="gwtModule Error: Must specify module and path of GWT widget to load.  Expected \"module/path\" but got \"${widget}\""/>
  </#if>
</#macro>

<#-- 
  Use in a FTL to load a widget by ID, the ID matching the ID in the entry class.
  Extra parameters are transformed in JS variables that can be retrieved in the GWT widget.

  Example:
  <@gwtWidget id="invoiceItems" invoiceId="10000"/>
-->
<#macro gwtWidget id class="" gwtParameters...>
<div class="gwtWidget<#if class?has_content> ${class}</#if>">
  <#if gwtParameters?has_content>
    <script type="text/javascript">
    /*<![CDATA[*/
    <#list gwtParameters?keys as param>
      var ${param} = "${gwtParameters[param]}";
    </#list>
    var ${id} = {
      <#list gwtParameters?keys as param>
        ${param}: "${gwtParameters[param]}" <#if param_has_next>,</#if>
      </#list>
    };
    /*]]>*/
    </script>
  </#if>
  <div id="${id}" <#if class?has_content>class="${class}"</#if>></div>
</div>
</#macro>

<#-- Accounting tags macros -->
<#macro accountingTagsHidden tags entity="" prefix="tag" suffix="">
  <#list tags as tag>
    <#if entity?has_content>
      <#assign tagValue=entity.get("acctgTagEnumId${tag.index}")! />
    <#else>
      <#assign tagValue=parameters.get("${prefix}${tag.index}${suffix}")! />
    </#if>
    <@inputHidden name="${prefix}${tag.index}${suffix}" value=tagValue />
  </#list>
</#macro>

<#macro accountingTagsSelect tag prefix="tag" suffix="" entity="" tabIndex="" index=-1 activeOnly=true>
  <#if entity?has_content>
    <#assign default=entity.get("acctgTagEnumId${tag.index}")! />
  <#else>
    <#if tag.hasDefaultValue()>
      <#assign default=tag.defaultValue />
    </#if>
  </#if>
  <#if activeOnly>
    <#assign tagValues = tag.activeTagValues />
  <#else>
    <#assign tagValues = tag.tagValues />
  </#if>
  <@inputSelect name="${prefix}${tag.index}${suffix}" errorField="${prefix}${tag.index}${suffix}" list=tagValues key="enumId" required=false default=default tabIndex=ti index=index ; tagValue>
    ${tagValue.description}
  </@inputSelect>
</#macro>

<#macro accountingTagsSelectRows tags prefix="tag" suffix="" entity="" tabIndex="">
  <#assign ti=tabIndex />
  <#list tags as tag>
    <#if entity?has_content>
      <#assign default=entity.get("acctgTagEnumId${tag.index}")! />
    </#if>
    <#if !default?has_content && tag.hasDefaultValue()>
      <#assign default=tag.defaultValue />
    </#if>
    <#if tag.isRequired()>
      <#assign titleClass="requiredField" />
    <#else>
      <#assign titleClass="tableheadtext" />
    </#if>
    <@inputSelectRow titleClass=titleClass title=tag.description name="${prefix}${tag.index}${suffix}" errorField="${prefix}${tag.index}${suffix}" list=tag.activeTagValues key="enumId" required=false default=default tabIndex=ti ; tagValue>
      ${tagValue.description}
    </@inputSelectRow>
    <#if ti?has_content>
      <#assign ti=ti+1 />
    </#if>
  </#list>
</#macro>

<#macro cvePresupSelectRows tags prefix="tag" suffix="" entity="" tabIndex="">
  <#assign ti=tabIndex />
  <#list tags as tag>
	<#if tag.type.equals("CL_ADMINISTRATIVA")>
		<#assign esAdmin = true/>
		<#assign llave = "externalId">
	<#else>
		<#assign esAdmin = false/>
		<#assign llave = "sequenceId">
	</#if>
    <#if entity?has_content>
    	<#if esAdmin == true>
			<#assign default=entity.get("acctgTagEnumIdAdmin")! />
		<#else>
			<#assign default=Static["org.ofbiz.order.finaccount.UtilClavePresupuestal"].obtenSequenceId(entity.get("acctgTagEnumId${tag.index}")!,tag.type,delegator)!'' />
		</#if>
    </#if>
    <#if !default?has_content && tag.hasDefaultValue()>
      <#assign default=tag.defaultValue />
    </#if>
    <#if tag.isRequired()>
      <#assign titleClass="requiredField" />
    <#else>
      <#assign titleClass="tableheadtext" />
    </#if>
    <@inputSelectRow titleClass=titleClass title=tag.description name="${prefix}${tag.index}${suffix}" errorField="${prefix}${tag.index}${suffix}" list=tag.activeTagValues key=llave required=false default=default tabIndex=ti ; tagValue>
    	<#if esAdmin == true>
    		${tagValue.externalId}-${tagValue.groupName}
    	<#else>
    		${tagValue.sequenceId}-${tagValue.enumCode}
    	</#if>
    </@inputSelectRow>
    <#if ti?has_content>
      <#assign ti=ti+1 />
    </#if>
  </#list>
</#macro>

<#macro accountingTagsSelectRowsForCart tags item index prefix="tag">
  <#list tags as tag>
    <#assign tagName="${prefix}${tag.index}" />
    <#if tag.isRequired()>
      <#assign titleClass="requiredField" />
    <#else>
      <#assign titleClass="tableheadtext" />
    </#if> 
    <#assign default=item.getAttribute(tagName)! />
    <#if !default?has_content && tag.hasDefaultValue()>
      <#assign default=tag.defaultValue />
    </#if>  
    <@inputSelectRow titleClass=titleClass title=tag.description name="${tagName}_${index}" list=tag.activeTagValues key="enumId" required=false default=default ; tagValue>
      ${tagValue.description}
    </@inputSelectRow>
  </#list>
</#macro>

<#macro accountingTagsDisplayRows tags entity>
  <#list tags as tag>
    <#list tag.tagValues as tagValue>
      <#if tagValue.enumId == entity.get("acctgTagEnumId${tag.index}")!>
        <@displayRow title=tag.description text=tagValue.description />
      </#if>
    </#list>
  </#list>
</#macro>

<#macro accountingTagsDisplay tags entity>
  <#list tags as tag>
    <#list tag.tagValues as tagValue>
      <#if tagValue.enumId == entity.get("acctgTagEnumId${tag.index}")!>
        <b>${tag.description}:</b> ${tagValue.description}
      </#if>
    </#list>
  </#list>
</#macro>

<#macro accountingTagsDisplayForCart tags item>
  <#list tags as tag>
    <#list tag.tagValues as tagValue>
      <#if tagValue.enumId == item.getAttribute("tag${tag.index}")!>
        <b>${tag.description}:</b> ${tagValue.description}
      </#if>
    </#list>
  </#list>
</#macro>

<#macro accountingTagsInputCells tags prefix="tag" tagTitleColSpan="1" tagColSpan="1" suffix="" entity="" tabIndex="" readonly="false">
  <#assign ti=tabIndex />
  <#list tags as tag>
    <#assign tagName="${prefix}${tag.index}" />
    <#-- only display tags that are set to something -->
    <#assign fieldName = "acctgTagEnumId${tag.index}"/>
    <#if entity?has_content>
      <#assign default=entity.get("acctgTagEnumId${tag.index}")! />
    <#elseif tag.hasDefaultValue()>
      <#assign default=tag.defaultValue />
    </#if>
    <#if tag.isRequired()>
      <#assign tagTitleClass="tableheadtext requiredField" />
    <#else>
      <#assign tagTitleClass="tableheadtext" />
    </#if>
    <#if readonly == "true">
      <#if entity?has_content && entity.get(fieldName)?has_content>
        <tr class="viewManyTR2">
          <td/>
          <td colspan="${tagTitleColSpan}" align="left"><span class="${tagTitleClass}">${tag.description}</span></td>
          <td colspan="${tagColSpan}" align="left">
            <#list tag.tagValues as tagValue>
              <#if tagValue.enumId == entity.get(fieldName)>
                ${tagValue.description}
              </#if>
            </#list>
          </td>
        </tr>
      </#if>
    <#else>
      <tr class="viewManyTR2">
        <td/>
        <td colspan="${tagTitleColSpan}" align="left"><span class="${tagTitleClass}">${tag.description}</span></td>
        <td colspan="${tagColSpan}" align="left">
          <@inputSelect name="${tagName}" list=tag.activeTagValues key="enumId" displayField="description" required=false default=default ignoreParameters=true/>
        </td>
      </tr>
    </#if>
    <#if ti?has_content>
      <#assign ti=ti+1 />
    </#if>
  </#list>
</#macro>

<#macro cvesPresupSelectRowsForCart tags item index prefix="tag">
  <#list tags as tag>
    <#assign tagName="${prefix}${tag.index}" />
    <#if tag.isRequired()>
      <#assign titleClass="requiredField" />
    <#else>
      <#assign titleClass="tableheadtext" />
    </#if> 
    <#assign default=item.getAttribute(tagName)! />
    <#if !default?has_content && tag.hasDefaultValue()>
      <#assign default=tag.defaultValue />
    </#if>
		<#if tag.type.equals("CL_ADMINISTRATIVA")>
			<@inputSelectRow titleClass=titleClass name="clasificacion${tag.index}_${index}" title=tag.description
					list=tag.activeTagValues key="externalId" required=false default=default! ; tagValue>
				${tagValue.externalId}-${tagValue.groupName}
			</@inputSelectRow>
		<#else>
			<@inputSelectRow titleClass=titleClass name="clasificacion${tag.index}_${index}" title=tag.description
					list=tag.activeTagValues key="sequenceId" required=false default=default! ; tagValue>
				${tagValue.sequenceId}-${tagValue.enumCode}
			</@inputSelectRow>
		</#if>
  </#list>
</#macro>

<#macro cvePresupItemsAprov tags item index prefix="clasificacion" onChange="" >
  <#list tags as tag>
    <#assign tagName="${prefix}${tag.index}" />
    <#if tag.isRequired()>
      <#assign titleClass="requiredField" />
    <#else>
      <#assign titleClass="tableheadtext" />
    </#if>
    <#assign default=item.get(tagName)! />
    <#if !default?has_content && tag.hasDefaultValue()>
      <#assign default=tag.defaultValue />
    </#if>
		<#if tag.type.equals("CL_ADMINISTRATIVA")>
			<@inputSelectRow titleClass=titleClass name="clasificacion${tag.index}" id="clasificacion${tag.index}_${index}" title=tag.description
					list=tag.activeTagValues key="externalId" required=false onChange=onChange default=default! ; tagValue>
				${tagValue.externalId}-${tagValue.groupName}
			</@inputSelectRow>            
		<#else>
			<@inputSelectRow titleClass=titleClass name="clasificacion${tag.index}" id="clasificacion${tag.index}_${index}" title=tag.description
					list=tag.activeTagValues key="sequenceId" required=false onChange=onChange default=default! ; tagValue >
				${tagValue.sequenceId}-${tagValue.enumCode}
			</@inputSelectRow>
		</#if>
  </#list>
</#macro>

<#macro displayCvesPresupRow tagTypes class="tabletext" titleClass="tableheadtext" name="" class="inputBox" onChange="" activas=true >
<#if tagTypes?has_content>
	<tr>
		<td colspan="2">
			<@display text=uiLabelMap.clavePresupuestal class="tableheadtext"/>
		</td>
	</tr>
	<tr>
		<td colspan="2">
			<hr>
		</td>
	</tr>
	<#assign cont = 0>
       <#list tagTypes as tag>
	        <#if tag.isRequired()>
	          <#assign titleClass="requiredField" />
			<#else>
	          <#assign titleClass="tableheadtext" />
	        </#if>
			<#if tag.type.equals("CL_CICLO")>
				<#assign mesesCiclo = onChange+"obtenMesCicloSimple(clasificacion${tag.index})" />
			<#else>
				<#assign mesesCiclo = onChange />
			</#if>
	        <tr>
	          <@displayTitleCell title=tag.description titleClass=titleClass />
	          <#if tag.type.equals("CL_ADMINISTRATIVA")>
		          <@inputSelectCell name="clasificacion${tag.index}" 
		          		errorField="acctgTagEnumId${tag.index}" list=activas?then(tag.activeTagValues,tag.tagValues)  
		          		key="externalId" required=false default=tag.defaultValue! ; tagValue>
		            ${tagValue.externalId}-${tagValue.groupName}
		          </@inputSelectCell>            
		      <#else>         
		          <@inputSelectCell name="clasificacion${tag.index}" id="clasificacion${tag.index}"
						errorField="acctgTagEnumId${tag.index}" list=activas?then(tag.activeTagValues,tag.tagValues)  
						key="sequenceId" required=false onChange=mesesCiclo! default=tag.defaultValue! ; tagValue >
		            ${tagValue.sequenceId}-${tagValue.enumCode}
		          </@inputSelectCell>
	          </#if>     
	        </tr>
	        <#assign cont = cont + 1>
      </#list>
</#if>          
</#macro>

<#macro cvePresupDisplayForCart tags item>
<#assign cont = 0/>
<#assign clave = ""/>
	<#list tags as tag>
		<#list tag.tagValues as tagValue>
	    <#--
	      <#if tagValue.enumId == item.getAttribute("tag${tag.index}")!>
	        <b>${tag.description}:</b> ${tagValue.description}
	      </#if> --> 
    		<#if tag.type.equals("CL_ADMINISTRATIVA")>
    			<#if tagValue.externalId?exists>
					<#if tagValue.externalId == item.getAttribute("tag${tag.index}")!>
	    			    <#assign cont = cont+1/>
	    			    <#if cont == 1>
	    			    	<tr class="${class}">
	    			    </#if>	
	    			    <#assign clave = clave + tagValue.externalId/>
	    				<@displayTitleCell title=tag.description/>
	    				<@displayCell text=tagValue.groupName />
	    				<#if cont == 3>
	    			    	</tr>
	    			    	<#assign cont = 0/>
	    			    </#if>					
					</#if>
				</#if>
			<#else>
				<#if tagValue.sequenceId?exists>
					<#assign idItem = tagValue.sequenceId />
				<#else>
					<#assign idItem = tagValue.enumId />
				</#if>
				<#if idItem == item.getAttribute("tag${tag.index}")!>
	    			<#assign clave = clave + tagValue.sequenceId/>
					<#assign cont = cont+1/>
					    <#if cont == 1>
				    	<tr class="${class}">
				    </#if>				
	    			<@displayTitleCell title=tag.description/>
	    			<@displayCell text=tagValue.enumCode />
					<#if cont == 3>
				    	</tr>
				    	<#assign cont = 0/>
				    </#if>    					
				</#if>
			</#if>
		</#list>
	</#list>
	<tr>
	&nbsp;
	    <td colspan="6" align="center">
	  	  <@display text=uiLabelMap.PurchasingClave class="tableheadtext"/>
	 	  <@display text=clave />
	 	</td>
  	</tr>
</#macro>

<#macro clavesPresupDisplay tags entity partyExternal="" class="" colspan="6">
<#assign cont = 0/>
<#assign clave = ""/>
  <#list tags as tag>
    <#list tag.tagValues as tagValue>
    	<#if tag.type.equals("CL_ADMINISTRATIVA")>
    		<#if tagValue.externalId?exists>
    			<#if tagValue.externalId == partyExternal>
    			    <#assign cont = cont+1/>
    			    <#if cont == 1>
    			    	<tr class="${class}">
    			    </#if>	
    			    <#assign clave = clave + tagValue.externalId/>
    				<@displayTitleCell title=tag.description/>
    				<@displayCell text=tagValue.groupName />
    				<#if cont == (colspan?number/2)?round>
    			    	</tr>
    			    	<#assign cont = 0/>
    			    </#if>
    			</#if>
    		</#if>
    	<#else>
    		<#if entity.get("acctgTagEnumId${tag.index}")?has_content>
	    		<#if tagValue.enumId == entity.get("acctgTagEnumId${tag.index}")>
	    			<#assign clave = clave + tagValue.sequenceId/>
					<#assign cont = cont+1/>
					    <#if cont == 1>
				    	<tr class="${class}">
				    </#if>				
	    			<@displayTitleCell title=tag.description/>
	    			<@displayCell text=tagValue.enumCode />
					<#if cont == (colspan?number/2)?round>
				    	</tr>
				    	<#assign cont = 0/>
				    </#if>    			
	    		</#if>
    		</#if>
    	</#if>
    </#list>
  </#list>
  <tr>
  	<td colspan="${colspan}" align="center">
		<hr>
	</td>
  </tr>
  <tr>
	&nbsp;
    <td colspan="${colspan}" align="center">
  	  <@display text=uiLabelMap.clavePresupuestal+" : " class="tableheadtext"/>
 	  <@display text=clave />
 	</td>
  </tr>
</#macro>

<#macro displayAfectPresupuestariaRow tagTypes class="tabletext" titleClass="tableheadtext" name="" class="inputBox" onChange="">
       <#list tagTypes as tag>
	        <#if tag.isRequired()>
	          <#assign titleClass="requiredField" />
			<#else>
	          <#assign titleClass="tableheadtext" />
	        </#if>        
	        <tr>
	          <@displayTitleCell title=tag.description titleClass=titleClass />
	          <#if tag.type.equals("CL_ADMINISTRATIVA")>
		          <@inputSelectCell name="clasif${tag.index}" errorField="acctgTagEnumId${tag.index}" list=tag.activeTagValues key="externalId" required=false default=tag.defaultValue! ; tagValue>
		            ${tagValue.externalId}-${tagValue.groupName}
		          </@inputSelectCell>            
		      <#else>         
		          <@inputSelectCell name="clasif${tag.index}" errorField="acctgTagEnumId${tag.index}" list=tag.activeTagValues key="sequenceId" required=false default=tag.defaultValue! ; tagValue>
		            ${tagValue.sequenceId}-${tagValue.enumCode}
		          </@inputSelectCell>
	          </#if>     
	        </tr>        
      </#list>       
</#macro>

<#macro displayClaveFlexArea tagTypes entity index="1" colspan="4">
    <tr class="${tableRowClass(index?number)}">
      <td colspan="${colspan}" align="left">
      <@flexArea targetId="ClavePresupuestal${index}" title=uiLabelMap.ClavePresupuestal controlStyle="font-weight: bold;" >
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
          <#if tagTypes?has_content>
          	<@clavesPresupDisplay tags=tagTypes entity=entity colspan=colspan />
          </#if>
        </table>
       </@flexArea>
      </td>
    </tr>
</#macro>

<#macro jerarquiaGeo titleClass="tableheadtext" class="tabletext" onChange="" tipolistaGeoAct="COUNTRY" idlistaGeoAct="MEX" >
    <script type="text/javascript">/*<![CDATA[*/
    
		opentaps.addOnLoad(cargaGeo);
	
	  	function cargaGeo() {
			var contact = document.getElementById('contactMechId');
			var contactMechId = contact.value;
			var tipo = document.getElementById('tipoGeo');
			var tipoGeo = tipo.value;
			var requestData = {'contactMechId': contactMechId , 'tipoGeo' : tipoGeo};
			opentaps.sendRequest('obtenCamposSeleccionar', requestData, function(data) {cargaCombos(data)});
		}
		
		function cargaCombos(data){
			for (var key in data) 
			{	
				var idPadre = "padre"+key;
				var hidHijoType = document.getElementById(idPadre);
				var geoTypeHijo = hidHijoType.value;
				var geoTypePadre = key;
				var geoIdPadre = data[key];
				var geoIdHijo = data[geoTypeHijo];
				llenaGeoOnLoad(geoIdPadre,geoTypePadre,geoIdHijo,geoTypeHijo);
			}			
		}
		
		
		function llenaGeoOnLoad(geoIdPadre,geoTypePadre,geoIdHijo,geoTypeHijo) {
			var requestData = {'geoId' : geoIdPadre};		    			    
		    opentaps.sendRequest('obtenHijosGeo', requestData, function(data) {cargaComboHijoSinLimpiar(data,geoIdPadre,geoTypePadre,geoIdHijo,geoTypeHijo)});
        }
        
		function cargaComboHijoSinLimpiar(data,geoIdPadre,geoTypePadre,geoIdHijo,geoTypeHijo){
        	var i = 0;	
			var select = document.getElementById(geoTypeHijo);
			select.innerHTML = "";
			for (var key in data) 
			{
					select.options[i] = new Option(data[key]);
					select.options[i].value = key;
					if(select.options[i].value == geoIdHijo){
						select.options[i].selected = true;
					}					
				  	i++;
			}
				var select = document.getElementById(geoTypePadre);
				for (var j = 0; j < select.length; j++)
				{
				  if(select.options[j].value == geoIdPadre){
					select.options[j].selected = true;
					//alert("se encontro conincidencia  "+geoIdPadre );
					//break;				  		
				  }
				}			
        }

        function llenaGeo(geoType,geoPadreId,geoHijoType) {
			//alert("Tipo  "+geoType);
			//alert("Padre "+geoPadreId);
			//alert("Hijo  "+geoHijoType);
			var requestData = {'geoId' : geoPadreId};		    			    
		    opentaps.sendRequest('obtenHijosGeo', requestData, function(data) {cargaComboHijo(data,geoHijoType)});
        }
        
        function cargaComboHijo(data,geoHijoType){
        	i = 0;	
			var select = document.getElementById(geoHijoType);
			select.innerHTML = "";
			var requestData = {'tipoGeo' : geoHijoType};		    			    
		    opentaps.sendRequest('obtenHijosTipoGeo', requestData, function(datos) {limpiaHijosGeo(datos)});			
			for (var key in data) 
			{	if(data[key]=="ERROR")
				{	
				}
				else
				{	select.options[i] = new Option(data[key]);
					select.options[i].value = key;					
				  	i++;
				}	    		
			}		
        }
        
        function limpiaHijosGeo(datos){
        	for (var key in datos) 
			{
				var select = document.getElementById(key);
				select.innerHTML = "";
			}
        }

    /*]]>*/</script>
    <@inputHidden id="tipoGeo" name="tipoGeo" value=tipolistaGeoAct />
	<#assign listaTipoGeo = Static["org.ofbiz.common.AB.UtilGeo"].obtenJerarquiaPaisGeo(delegator,tipolistaGeoAct) />
	<#if idlistaGeoAct=="">
		<#assign mapaGeo = Static["org.ofbiz.base.util.UtilMisc"].toMap("geoTypeId","COUNTRY")/>
	<#else>
		<#assign mapaGeo = Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId",idlistaGeoAct)/>
	</#if>
	<#assign listaGeoAct = delegator.findByAnd("Geo",mapaGeo)?sort_by("geoName")  />
	<#assign idGeoParent = idlistaGeoAct />
	<#list listaTipoGeo as tipoGeoGen>
		<#assign geoTypeHijo = Static["org.ofbiz.common.AB.UtilGeo"].obtenHijoTipoGeo(delegator,tipoGeoGen.geoTypeId)! />
		<#assign geoHijoId = geoTypeHijo.geoTypeId! />
		<@inputHidden id="padre${tipoGeoGen.geoTypeId}" name="padre${tipoGeoGen.geoTypeId}" value=geoHijoId />
		<tr>	
			<#assign nombreCampo = "GeoType.description."+tipoGeoGen.geoTypeId />
			<@displayTitleCell title="${uiLabelMap.get(nombreCampo)}" titleClass=titleClass />
			<@inputSelectCell name=tipoGeoGen.geoTypeId list=listaGeoAct key="geoId" displayField="geoName" 
				onChange="llenaGeo(this.name,this.options[this.selectedIndex].value,'${geoHijoId}')" 
				onBlur="llenaGeo(this.name,this.options[this.selectedIndex].value,'${geoHijoId}')" />
		</tr>
		<#assign mapaGeo = Static["org.ofbiz.base.util.UtilMisc"].toMap("parentGeoId",idGeoParent)/>
		<#assign listaGeoAct = delegator.findByAnd("Geo",mapaGeo)?sort_by("geoName")  />
		<#assign ObjGeo = listaGeoAct[0]! />
		<#assign idGeoParent = ObjGeo.geoId! />
	</#list>
</#macro>

<#macro inputSelectDireccionObraRow title obraId titleClass="tableheadtext" class="inputBox" onChange="">
	<#assign listaMapa = Static["com.absoluciones.obras.contact.ContactMechWorker"].getObraPostalAddresses(delegator,obraId,"") />
	<#assign listaCombo =  [] />
	<#assign cont = 1 />
	<#assign default = "" />
	<#assign selected = "selected" />
	<#list listaMapa as objMapa>
		<#assign listaCombo = listaCombo + [objMapa.get("postalAddress")] />
		<#if cont == 1><#assign default = objMapa.get("postalAddress").get("contactMechId") /></#if>
		<#assign cont = cont + 1 />
	</#list>
	<tr>		
		<@displayTitleCell title=title titleClass=titleClass />
		<td>
		  <select id="ubicacionFisica" name="ubicacionFisica" class="${class}" onChange="${onChange}" >
		    <option value="_NA_">${uiLabelMap.ObraSinDireccion}</option>
		    <#list listaCombo as option>
				<#assign displayFields = option.address1!+" "+option.address2 />
		      <option <#if option.contactMechId == default>${selected}</#if> value="${option.contactMechId}">
		        ${displayFields}
		      </option>
		    </#list>
		  </select>
		  <#assign etiquetaBoton = "" />
		  <#if cont == 1 ><#assign etiquetaBoton = uiLabelMap.CommonAdd /><#else><#assign etiquetaBoton = uiLabelMap.CommonEdit /></#if>
		  <@submitFormLink form="verEditarDireccion" text=etiquetaBoton class="subMenuButton"/>
		</td>
</#macro>

<#macro seccionRow colspan title checkInhabilitar="false" nombreCheck="" defaultCheck="" >
		<tr><td></br></td></tr>
		<tr><td></br></td></tr>
		<tr>
			<td colspan="${colspan}" align="center">
				<@display text=title class="areaheader"/>
			</td>
		</tr>
		<#if checkInhabilitar == "true">
			<tr>
			<td colspan="${colspan}" align="right">
				<@display text="Inhabilitar" class="areaheader"/>
				<#assign nombreCheckBox ><#if nombreCheck == "">${title}<#else>${nombreCheck}</#if></#assign> 
				<#assign defaultCheckBox ><#if defaultCheck == "">"N"<#else>${defaultCheck}</#if></#assign>
				<@inputCheckbox name=nombreCheckBox id=nombreCheckBox default=defaultCheckBox />
			</td>
			</tr>
		</#if>
		<tr><td colspan="${colspan}"><hr></td></tr>
</#macro>

<#macro inputAutoCompleteEnumerationRow name title titleClass="tableheadtext" id="" url="getClasificacionEnumeration" styleClass="inputAutoCompleteQuick" form="" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="" filtro="">
	<tr>
		<td class="titleCell"><span class="${titleClass}">${title}</span></td>
		<td>
		  <@inputAutoComplete name=name form=form url=url id=id lookup="" styleClass=styleClass default=default 
								index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip filtro=filtro/>
		</td>
	</tr>
</#macro>

<#macro inputAutoCompleteEnumerationCell name titleClass="tableheadtext" id="" url="getClasificacionEnumeration" styleClass="inputAutoCompleteQuick" form="" default="" index=-1 size=15 errorField="" tabIndex="" onChange="" tooltip="" filtro="">
		<td>
		  <@inputAutoComplete name=name form=form url=url id=id lookup="" styleClass=styleClass default=default 
								index=index size=size errorField=errorField tabIndex=tabIndex onChange=onChange tooltip=tooltip filtro=filtro/>
		</td>
</#macro>

<#macro clavePresupRow class="tabletext" titleClass="requiredField" name="" class="inputBox" onChange="" activas="true" tipo="" >
	<#assign cicloId = cicloId!Static["org.opentaps.common.util.UtilCommon"].getCicloId(request)>
	<#assign organizationPartyId = organizationPartyId!Static["org.opentaps.common.util.UtilCommon"].getOrganizationPartyId(request)>
	<#assign listClasificadores = Static["org.ofbiz.order.finaccount.UtilClavePresupuestal"].listaClasificaciones(delegator,tipo,cicloId,organizationPartyId) />
	<#assign maxNumClasif = Static["org.ofbiz.order.finaccount.UtilClavePresupuestal"].TAG_COUNT />
	<script type="text/javascript">/*<![CDATA[*/
	 
	 	opentaps.addOnLoad(llenaCombo(1,''));
	 	
	 
	 	function llenaCombo(numeroClasificacion, valor){

	 		for (i = numeroClasificacion ; i <= ${maxNumClasif} ; i++) {
	 			var select = document.getElementById('clasificacion'+i);
	 			if(select){
	 				select.innerHTML = "";
	 			}
			}
			
			if(numeroClasificacion > 1 && !valor){
	 			return;
	 		}
			
	 		requestData = {'tipo' : '${tipo}' , 'ciclo': '${cicloId}', 'organizationPartyId' : '${organizationPartyId}',
	 					'activas' : '${activas}', 'clasificacion' : 'clasificacion'+numeroClasificacion, 'valorClasif': valor };
			opentaps.sendRequest('getListaClasificador', requestData, function(data) {cargaComboClasificacion(data,numeroClasificacion)});
	 	}
	 	
	 	function cargaComboClasificacion(data,numeroClasificacion){
			var select = document.getElementById('clasificacion'+numeroClasificacion);
			select.innerHTML = "";
			var i = 1;
			for (var key in data) 
			{
				if(data[key]=="ERROR")
				{
					break;	
				} else {
					select.options[i] = new Option(data[key]);
					select.options[i].value = key;	
				  	i++;
				}

			}	 	
	 	}
	 	
	 /*]]>*/</script>
	<#if listClasificadores?has_content>
		<tr>
			<td colspan="2">
				<@display text=uiLabelMap.clavePresupuestal class="tableheadtext"/>
			</td>
		</tr>
		<tr>
			<td colspan="2">
				<hr>
			</td>
		</tr>
		<#list listClasificadores as Clasificacion>
			<#assign siguienteClasif = Clasificacion.orden+1 >
			<#if Clasificacion.clasificacionId.equals("CL_CICLO")>
				<#assign change = onChange+"obtenMesCicloSimple(clasificacion${Clasificacion.orden});llenaCombo(${siguienteClasif},this.value)" />
			<#else>
				<#assign change = onChange+";llenaCombo(${siguienteClasif},this.value)" />
			</#if>
			<@inputSelectRow title=Clasificacion.descripcion name="clasificacion${Clasificacion.orden}" list=[]
				 titleClass=titleClass onChange=change id="clasificacion${Clasificacion.orden}"/>
		</#list>
	</#if>
</#macro>
<#macro displayLinkReporte href text class="linktext" style="" target="" id="">
<#compress>
  <#assign idText = id?has_content?string("id=\"" + id + "\"", "")/>
  <#if href?starts_with("/")>
    <a href="${href}<#if externalKeyParam?has_content>${StringUtil.wrapString(externalKeyParam)}</#if>" ${idText} style="${style}" class="${class}" target="${target}">${text}</a>
  <#elseif href?starts_with("javascript:")>
    <a href="${href}" ${idText} style="${style}" class="${class}">${text}</a>
  <#else>
    <a href="${href}" ${idText} style="${style}" class="${class}" target="${target}">${text}</a>
  </#if>
</#compress>
</#macro>
<#macro displayLinkCellReporte href text class="linktext" style="" blockStyle="" blockClass="" target="" id="">
  <td class="${blockClass}" style="${blockStyle}"><@displayLinkReporte text=text href=href style=style class=class target=target id=id /></td>
</#macro>
