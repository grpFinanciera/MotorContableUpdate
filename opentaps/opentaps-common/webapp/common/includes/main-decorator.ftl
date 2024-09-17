<#include "header.ftl">
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
This file controls the standard layout of all opentaps applications, including the headers, footers and so on.

There are certain parameters sthat should be set in the context to help this file work:

opentapsApplicationName:  The name of the application in lowercase letters (crmsfa, financials, etc.)
configProperties:  Map containing the config parameters from a file named ${opentapsApplication}.properties
sectionName: What section of the application we are in, which is equivalent to the tab bar name (e.g., accounts, contacts, leads in crmsfa)
-->
${screens.render("component://opentaps-common/widget/screens/common/CommonScreens.xml#applicationTabBar")}
<#--<div id="nav1">
  ${screens.render("component://opentaps-common/widget/screens/common/CommonScreens.xml#navigationHistory")}
</div>-->
<div id="numRequi">
  ${screens.render("component://opentaps-common/widget/screens/common/CommonScreens.xml#numeroRequisiciones")}
</div>
<div id="numOrdenesPago">
  ${screens.render("component://opentaps-common/widget/screens/common/CommonScreens.xml#numeroOrdenesPago")}
</div>
<div id="numViatico">
  ${screens.render("component://opentaps-common/widget/screens/common/CommonScreens.xml#numeroViaticos")}
</div>
<div id="numeroObras">
  ${screens.render("component://opentaps-common/widget/screens/common/CommonScreens.xml#numeroObras")}
</div>
<div id="numPedidoInterno">
  ${screens.render("component://opentaps-common/widget/screens/common/CommonScreens.xml#numeroPedidosInternos")}
</div>
<div id="numPendientesTeso">
  ${screens.render("component://opentaps-common/widget/screens/common/CommonScreens.xml#numeroPendientesTesoreria")}
</div>
<div id="numPendientesMaxMin">
  ${screens.render("component://opentaps-common/widget/screens/common/CommonScreens.xml#numeroPendientesMaxMin")}
</div>
<div id="notifTipoDeCambio">
  ${screens.render("component://opentaps-common/widget/screens/common/CommonScreens.xml#notificacionTipoDeCambio")}
</div>
<div id="numReportesPend">
  ${screens.render("component://opentaps-common/widget/screens/common/CommonScreens.xml#numeroReportes")}
</div>
<div id="numRecepcionesActivos">
  ${screens.render("component://opentaps-common/widget/screens/common/CommonScreens.xml#numRecepcionesActivos")}
</div>
<div id="numTransferencias">
  ${screens.render("component://opentaps-common/widget/screens/common/CommonScreens.xml#numeroTransferenciasAlmacen")}
</div>
<div id="numGastosPorAutorizar">
  ${screens.render("component://opentaps-common/widget/screens/common/CommonScreens.xml#numeroGastosPorAutorizar")}
</div>
<div id="numPagosAPorAutorizar">
  ${screens.render("component://opentaps-common/widget/screens/common/CommonScreens.xml#numeroPagosAntiPorAutorizar")}
</div>
<#include "keyboard-shortcuts.ftl"/>

<div class="centerarea">
  <#include "messages.ftl">

  <div class="contentarea">

    <#if requiredPermission?exists>
      <#if !allowed?exists || (allowed?exists && allowed)>
        <#assign allowed = Static["org.opentaps.common.security.OpentapsSecurity"].checkSectionSecurity(opentapsApplicationName, sectionName, requiredPermission, request)>
      </#if>
    </#if>

    <#if !userLogin?exists>
      <#-- always render normally when no login exists, that way the login page gets rendered first thing. 
      In practice main-body should never happen because OFBIZ will always intercept you and re-direct to login page first -->
      ${screens.render("component://opentaps-common/widget/screens/common/CommonScreens.xml#main-body")}
    <#elseif allowed?exists && !allowed>
      <div class="tableheadtext">${uiLabelMap.OpentapsError_PermissionDenied}</div>
    <#elseif applicationSetupScreen?exists && !session.getAttribute("applicationContextSet")?default(false)>
      ${screens.render(applicationSetupScreen)}
    <#else>
      ${screens.render("component://opentaps-common/widget/screens/common/CommonScreens.xml#main-body")}
    </#if>

  </div>
</div>

</body>
</html>
