

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

<#-- Parametrized find form for transactions. -->



	${tipoActivoFijoid}
    ${documentos}
        <table class="listTable">            
            <#list documentos as documento>
            <tr class="${tableRowClass(row_index)}">
                <td>tipoDoc</td>
                <td>tipoDoc1</td>
                <td>tipoDoc2</td>
                <td>tipoDoc3</td>
                
                                
            </tr>
            </#list>
        </table>
    