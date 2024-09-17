/*
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
 */

import javolution.util.FastList;

import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.util.EntityUtil
import org.opentaps.base.constants.StatusItemConstants;

userLogin = session.getAttribute("userLogin");
userLoginId = userLogin.getString("userLoginId");

customersProcessed = 0;
customersNotProcessed = 0;
productsProcessed = 0;
productsNotProcessed = 0;
pagosProcessed = 0;
pagosNotProcessed = 0;
inventoryProcessed = 0;
inventoryNotProcessed = 0;
orderHeadersProcessed = 0;
orderHeadersNotProcessed = 0;
orderItemsProcessed = 0;
orderItemsNotProcessed = 0;
glAccountsProcessed = 0;
glAccountsNotProcessed = 0;
tagProcessed=0;
tagNotProcessed=0;
partyProcessed = 0;
partyNotProcessed = 0;
matrizEgresoProcessed = 0;
matrizEgresoNotProcessed = 0;
matrizIngresoProcessed = 0;
matrizIngresoNotProcessed = 0;
matrizConceptoProcessed = 0;
matrizConceptoNotProcessed = 0;
presupuestoInicialProcessed = 0;
presupuestoInicialNotProcessed = 0;
ingresoEgresoProcessed = 0;
ingresoEgresoNotProcessed = 0;
operacionDiariaProcessed = 0;
operacionDiariaNotProcessed = 0;
ordenesCompraProcessed = 0;
ordenesCompraNotProcessed = 0;
saldosInicialAuxProcessed = 0;
saldosInicialAuxNotProcessed = 0;
afectacionEgresoProcessed = 0;
afectacionEgresoNotProcessed = 0;
userProcessed = 0;
userNotProcessed = 0;
fixedAssetProcessed = 0;
levantaFixedAssetProcessed = 0;
fixedAssetNotProcessed = 0;
levantaFixedAssetNotProcessed = 0;
compDevNomPresProcessed = 0;
compDevNomPresNotProcessed = 0;
compDevNomContProcessed = 0;
compDevNomContNotProcessed = 0;
ejerNomPresProcessed = 0;
ejerNomPresNotProcessed = 0;
ejerNomContProcessed = 0;
ejerNomContNotProcessed = 0;
operacionPatrimonialProcessed = 0;
operacionPatrimonialNotProcessed = 0;
validacionPresupuestalProcessed = 0;
validacionPresupuestalNotProcessed = 0;
ingresoProcessed = 0;
ingresoNotProcessed = 0;
devIngProcessed = 0;
devIngNotProcessed = 0;
recIngProcessed = 0;
recIngNotProcessed = 0;
requisicionProcessed = 0;
requisicionNotProcessed = 0;
compensadaProcessed = 0;
compensadaNotProcessed = 0;
historialBienesProcessed = 0;
historialBienesNotProcessed = 0;
pagoExternoProcessed=0;
pagoExternoNotProcessed=0;

/*
  GET PROCESSED
*/
searchConditions = FastList.newInstance();
searchConditions.add(new EntityExpr("importStatusId", EntityOperator.EQUALS, StatusItemConstants.Dataimport.DATAIMP_IMPORTED));
searchConditions.add(new EntityExpr("userLoginId", EntityOperator.EQUALS, userLoginId));
allConditions = new EntityConditionList(searchConditions, EntityOperator.AND);

suppliersProcessed = delegator.findCountByCondition("DataImportSupplier", allConditions, null);
customersProcessed = delegator.findCountByCondition("DataImportCustomer", allConditions, null);
productsProcessed = delegator.findCountByCondition("DataImportProduct", allConditions, null);
pagosProcessed = delegator.findCountByCondition("DataImportPayment", allConditions, null);
inventoryProcessed = delegator.findCountByCondition("DataImportInventory", allConditions, null);
orderHeadersProcessed = delegator.findCountByCondition("DataImportOrderHeader", allConditions, null);
orderItemsProcessed = delegator.findCountByCondition("DataImportOrderItem", allConditions, null);
glAccountsProcessed = delegator.findCountByCondition("DataImportGlAccount", allConditions, null);
tagProcessed= delegator.findCountByCondition("DataImportTag", allConditions, null);
partyProcessed = delegator.findCountByCondition("DataImportParty", allConditions, null);

presupuestoInicialProcessed = delegator.findCountByCondition("DataImportPresupuestoInicia", allConditions, null);
operacionDiariaProcessed = delegator.findCountByCondition("DataImportOperacionDiaria", allConditions, null);
ordenesPagoProcessed = delegator.findCountByCondition("DataImportOrdenesPago", allConditions, null);
ordenesCobroProcessed = delegator.findCountByCondition("DataImportOrdenesCobro", allConditions, null);
saldosInicialAuxProcessed = delegator.findCountByCondition("DataImportSaldoInicialAux", allConditions, null);
afectacionEgresoProcessed = delegator.findCountByCondition("DataImportAfectacionEgreso", allConditions, null);
matrizEgresoProcessed = delegator.findCountByCondition("MatrizEgreso", allConditions, null);
matrizIngresoProcessed = delegator.findCountByCondition("MatrizIngreso", allConditions, null);
matrizConceptoProcessed = delegator.findCountByCondition("MatrizConcepto", allConditions, null);
userProcessed = delegator.findCountByCondition("DataImportUser", allConditions, null);
fixedAssetProcessed = delegator.findCountByCondition("DataImportFixedAsset", allConditions, null);
levantaFixedAssetProcessed = delegator.findCountByCondition("DataImportLevantaActFijo", allConditions, null);
compDevNomPresProcessed = delegator.findCountByCondition("DataImportCompDevNomPres", allConditions, null);
compDevNomContProcessed = delegator.findCountByCondition("DataImportCompDevNomCont", allConditions, null);
ejerNomPresProcessed = delegator.findCountByCondition("DataImportEjerNomPres", allConditions, null);
ejerNomContProcessed = delegator.findCountByCondition("DataImportEjerNomCont", allConditions, null);
operacionPatrimonialProcessed = delegator.findCountByCondition("DataImportOperPatr", allConditions, null);
validacionPresupuestalProcessed = delegator.findCountByCondition("DataImportValidPresup", allConditions, null);
ingresoProcessed = delegator.findCountByCondition("DataImportIngreso", allConditions, null);
devIngProcessed = delegator.findCountByCondition("DataImportDevIng", allConditions, null);
recIngProcessed = delegator.findCountByCondition("DataImportRecIng", allConditions, null);
requisicionProcessed = delegator.findCountByCondition("DataImportDetRequisicion", allConditions, null);
compensadaProcessed = delegator.findCountByCondition("DataImportACompensada", allConditions, null);
historialBienesProcessed = delegator.findCountByCondition("DataImportHistorialBienes", allConditions, null);
pagoExternoProcessed = delegator.findCountByCondition("DataImportPagoExterno", allConditions, null);


/*
  GET NOT-PROCESSED
*/
                  
EntityCondition statusCond1 = EntityCondition.makeCondition(EntityOperator.OR,
                        EntityCondition.makeCondition("importStatusId", EntityOperator.EQUALS, StatusItemConstants.Dataimport.DATAIMP_NOT_PROC),
         EntityCondition.makeCondition("importStatusId", EntityOperator.EQUALS, StatusItemConstants.Dataimport.DATAIMP_FAILED),
         EntityCondition.makeCondition("importStatusId", EntityOperator.EQUALS, null));
            
EntityCondition statusCond = EntityCondition.makeCondition(EntityOperator.AND,
                        statusCond1,
                        EntityCondition.makeCondition("userLoginId", EntityOperator.EQUALS, userLoginId));
         

suppliersNotProcessed = delegator.findCountByCondition("DataImportSupplier", statusCond, null);
customersNotProcessed = delegator.findCountByCondition("DataImportCustomer", statusCond, null);
productsNotProcessed = delegator.findCountByCondition("DataImportProduct", statusCond, null);
pagosNotProcessed = delegator.findCountByCondition("DataImportPayment", statusCond, null);
inventoryNotProcessed = delegator.findCountByCondition("DataImportInventory", statusCond, null);
orderHeadersNotProcessed = delegator.findCountByCondition("DataImportOrderHeader", statusCond, null);
orderItemsNotProcessed = delegator.findCountByCondition("DataImportOrderItem", statusCond, null);
glAccountsNotProcessed = delegator.findCountByCondition("DataImportGlAccount", statusCond, null);
tagNotProcessed = delegator.findCountByCondition("DataImportTag", statusCond, null);
partyNotProcessed = delegator.findCountByCondition("DataImportParty", statusCond, null);
matrizEgresoNotProcessed = delegator.findCountByCondition("MatrizEgreso", statusCond, null);
matrizIngresoNotProcessed = delegator.findCountByCondition("MatrizIngreso", statusCond, null);
matrizConceptoNotProcessed = delegator.findCountByCondition("MatrizConcepto", statusCond, null);

presupuestoInicialNotProcessed = delegator.findCountByCondition("DataImportPresupuestoInicia", statusCond, null);
operacionDiariaNotProcessed = delegator.findCountByCondition("DataImportOperacionDiaria", statusCond, null);
ordenesPagoNotProcessed = delegator.findCountByCondition("DataImportOrdenesPago", statusCond, null);
ordenesCobroNotProcessed = delegator.findCountByCondition("DataImportOrdenesCobro", statusCond, null);
saldosInicialAuxNotProcessed = delegator.findCountByCondition("DataImportSaldoInicialAux", statusCond, null);
afectacionEgresoNotProcessed = delegator.findCountByCondition("DataImportAfectacionEgreso", statusCond, null);
userNotProcessed = delegator.findCountByCondition("DataImportUser", statusCond, null);
fixedAssetNotProcessed = delegator.findCountByCondition("DataImportFixedAsset", statusCond, null);
levantaFixedAssetNotProcessed = delegator.findCountByCondition("DataImportLevantaActFijo", statusCond, null);
compDevNomPresNotProcessed = delegator.findCountByCondition("DataImportCompDevNomPres", statusCond, null);
compDevNomContNotProcessed = delegator.findCountByCondition("DataImportCompDevNomCont", statusCond, null);
ejerNomPresNotProcessed = delegator.findCountByCondition("DataImportEjerNomPres", statusCond, null);
ejerNomContNotProcessed = delegator.findCountByCondition("DataImportEjerNomCont", statusCond, null);
operacionPatrimonialNotProcessed = delegator.findCountByCondition("DataImportOperPatr", statusCond, null);
validacionPresupuestalNotProcessed = delegator.findCountByCondition("DataImportValidPresup", statusCond, null);
ingresoNotProcessed = delegator.findCountByCondition("DataImportIngreso", statusCond, null);
devIngNotProcessed = delegator.findCountByCondition("DataImportDevIng", statusCond, null);
recIngNotProcessed = delegator.findCountByCondition("DataImportRecIng", statusCond, null);
requisicionNotProcessed = delegator.findCountByCondition("DataImportDetRequisicion", statusCond, null);
compensadaNotProcessed = delegator.findCountByCondition("DataImportACompensada", statusCond, null);
historialBienesNotProcessed = delegator.findCountByCondition("DataImportHistorialBienes", statusCond, null);
pagoExternoNotProcessed = delegator.findCountByCondition("DataImportPagoExterno", statusCond, null);

context.put("suppliersProcessed", suppliersProcessed);
context.put("suppliersNotProcessed", suppliersNotProcessed);
context.put("customersProcessed", customersProcessed);
context.put("customersNotProcessed", customersNotProcessed);
context.put("productsProcessed", productsProcessed);
context.put("productsNotProcessed", productsNotProcessed);
context.put("pagosProcessed", pagosProcessed);
context.put("pagosNotProcessed", pagosNotProcessed);
context.put("inventoryProcessed", inventoryProcessed);
context.put("inventoryNotProcessed", inventoryNotProcessed);
context.put("orderHeadersProcessed", orderHeadersProcessed);
context.put("orderHeadersNotProcessed", orderHeadersNotProcessed);
context.put("orderItemsProcessed", orderItemsProcessed);
context.put("orderItemsNotProcessed", orderItemsNotProcessed);
context.put("glAccountsProcessed", glAccountsProcessed);
context.put("glAccountsNotProcessed", glAccountsNotProcessed);
context.put("tagProcessed", tagProcessed);
context.put("tagNotProcessed", tagNotProcessed);
context.put("partyProcessed", partyProcessed);
context.put("partyNotProcessed", partyNotProcessed);
context.put("matrizEgresoProcessed", matrizEgresoProcessed);
context.put("matrizEgresoNotProcessed", matrizEgresoNotProcessed);
context.put("matrizIngresoProcessed", matrizIngresoProcessed);
context.put("matrizIngresoNotProcessed", matrizIngresoNotProcessed);
context.put("matrizConceptoProcessed", matrizConceptoProcessed);
context.put("matrizConceptoNotProcessed", matrizConceptoNotProcessed);
context.put("presupuestoInicialProcessed", presupuestoInicialProcessed);
context.put("presupuestoInicialNotProcessed", presupuestoInicialNotProcessed);
context.put("ingresoEgresoProcessed", ingresoEgresoProcessed);
context.put("ingresoEgresoNotProcessed", ingresoEgresoNotProcessed);
context.put("operacionDiariaProcessed", operacionDiariaProcessed);
context.put("operacionDiariaNotProcessed", operacionDiariaNotProcessed);
context.put("ordenesPagoProcessed",ordenesPagoProcessed);
context.put("ordenesPagoNotProcessed",ordenesPagoNotProcessed);
context.put("ordenesCobroProcessed",ordenesCobroProcessed);
context.put("ordenesCobroNotProcessed",ordenesCobroNotProcessed);
context.put("saldosInicialAuxProcessed",saldosInicialAuxProcessed);
context.put("saldosInicialAuxNotProcessed",saldosInicialAuxNotProcessed);
context.put("afectacionEgresoProcessed",afectacionEgresoProcessed);
context.put("afectacionEgresoNotProcessed",afectacionEgresoNotProcessed);
context.put("saldosInicialAuxNotProcessed",saldosInicialAuxNotProcessed);
context.put("userProcessed",userProcessed);
context.put("userNotProcessed",userNotProcessed);
context.put("fixedAssetProcessed",fixedAssetProcessed);
context.put("levantaFixedAssetProcessed",levantaFixedAssetProcessed);
context.put("fixedAssetNotProcessed",fixedAssetNotProcessed);
context.put("levantaFixedAssetNotProcessed",levantaFixedAssetNotProcessed);
context.put("compDevNomPresProcessed",compDevNomPresProcessed);
context.put("compDevNomPresNotProcessed",compDevNomPresNotProcessed);
context.put("compDevNomContProcessed",compDevNomContProcessed);
context.put("compDevNomContNotProcessed",compDevNomContNotProcessed);
context.put("ejerNomPresProcessed",ejerNomPresProcessed);
context.put("ejerNomPresNotProcessed",ejerNomPresNotProcessed);
context.put("ejerNomContProcessed",ejerNomContProcessed);
context.put("ejerNomContNotProcessed",ejerNomContNotProcessed);
context.put("operacionPatrimonialProcessed",operacionPatrimonialProcessed);
context.put("operacionPatrimonialNotProcessed",operacionPatrimonialNotProcessed);
context.put("validacionPresupuestalProcessed",validacionPresupuestalProcessed);
context.put("validacionPresupuestalNotProcessed",validacionPresupuestalNotProcessed);
context.put("ingresoProcessed",ingresoProcessed);
context.put("ingresoNotProcessed",ingresoNotProcessed);
context.put("devIngProcessed",devIngProcessed);
context.put("devIngNotProcessed",devIngNotProcessed);
context.put("recIngProcessed",recIngProcessed);
context.put("recIngNotProcessed",recIngNotProcessed);
context.put("requisicionProcessed",requisicionProcessed);
context.put("requisicionNotProcessed",requisicionNotProcessed);
context.put("compensadaProcessed",compensadaProcessed);
context.put("compensadaNotProcessed",compensadaNotProcessed);
context.put("historialBienesProcessed",historialBienesProcessed);
context.put("historialBienesNotProcessed",historialBienesNotProcessed);
context.put("pagoExternoProcessed",pagoExternoProcessed);
context.put("pagoExternoNotProcessed",pagoExternoNotProcessed);
