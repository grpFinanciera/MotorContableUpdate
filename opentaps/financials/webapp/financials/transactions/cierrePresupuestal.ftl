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
 *  
-->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<form method="POST" action="<@ofbizUrl>cierrePresupuestal</@ofbizUrl>" name=cierrePresupuestal> <#-- action set by the screen -->
<input type="hidden" name="c" value="${organizationPartyId}"/>
  <div class="form" style="border:0">
    <table class="fourColumnForm" style="border:0">
      <tr>
    	<@displayTitleCell title=uiLabelMap.FinancialCiclo />
    	</td>
	    <td>	   	    
     <select name="enumCode" size="1"  >
		    	<#list ciclos as ciclo>
		        	<option  value="${ciclo.enumCode}">${ciclo.get("description",locale)}</option>
		        </#list>
		    </select>	 
	    </td>
    </tr>
          <@inputSubmitRow title=uiLabelMap.FinancialCierrePres />
    </table>
  </div>
</form>
