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

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@paginate name="listEditarCuentasBancos" list=cuentasBancariasListBuilder rememberPage=true>
    <#noparse>
        <@navigationHeader/>                
        <table class="listTable">
            <tr class="listTableHeader">
                <@headerCell title=uiLabelMap.IdCuenta orderBy="cuentaBancariaId"/>
                <@headerCell title=uiLabelMap.NombreCuenta orderBy="nombreCuenta"/>
                <@headerCell title=uiLabelMap.NumeroCuenta orderBy="numeroCuenta"/>
                <@headerCell title=uiLabelMap.Descripcion orderBy="descripcion"/>
                <@headerCell title=uiLabelMap.Banco orderBy="nombreBanco"/>
                <@headerCell title=uiLabelMap.Sucursal orderBy="nombreSucursal"/>                
                <@headerCell title=uiLabelMap.Saldo orderBy="saldo"/>
                <@headerCell title=uiLabelMap.Moneda orderBy="moneda"/> 
                <@headerCell title=uiLabelMap.cuentaHabilitada orderBy="habilitada"/>               
            </tr>
            <#list pageRows as row>       
            <tr class="${tableRowClass(row_index)}">
                <@displayLinkCell text=row.cuentaBancariaId href="verEditarCuenta?cuentaId=${row.cuentaBancariaId}&&bancoId=${row.bancoId}"/>
                <@displayCell text=row.nombreCuenta/>
                <@displayCell text=row.numeroCuenta/>
                <@displayCell text=row.descripcion/>
                <@displayCell text=row.nombreBanco/>
                <@displayCell text=row.nombreSucursal/>                
                <@displayCurrencyCell amount=row.saldo currencyUomId=row.moneda class="textright"/>
                <@displayCell text=row.moneda/> 
                <@displayCell text=row.habilitada/>           
            </tr>
            </#list>
        </table>
    </#noparse>
</@paginate>