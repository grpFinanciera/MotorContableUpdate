package org.opentaps.dataimport.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.base.constants.StatusItemConstants;
import org.opentaps.dataimport.UtilImport;

public class SaldoInicialAuxiliarImportService {
	
	private static final String MODULE = SaldoInicialAuxiliarImportService.class.getName();
	private static String userLoginId;

	public static Map<String, Object> importSaldoInicialAuxiliar(DispatchContext dctx, Map<String, ?> context){
		
		Debug.logInfo("Entrando a carga de saldos inicial de auxiliares", MODULE);
		
		Delegator delegator = dctx.getDelegator();
		
		userLoginId = ((GenericValue) context.get("userLogin")).getString("userLoginId");
		
		Map<String, Object> regreso = ServiceUtil.returnSuccess();
		
		EntityCondition condicionSinProcesar = UtilImport.condicionesSinProcesar(userLoginId);
		List<String> orderBy = UtilMisc.toList("renglon");
		
		try {
			List<GenericValue> listDataImportSaldoInicialAuxiliarValida = delegator.findByCondition(
					"DataImportSaldoInicialAuxiliarValida", condicionSinProcesar, null, orderBy);
			
			List<GenericValue> listDataImportSaldoInicialAuxiliar = delegator.findByCondition(
					"DataImportSaldoInicialAux", condicionSinProcesar, null, orderBy);
			
			Map<String,GenericValue> mapDataImportSaldoInicialAuxiliar = FastMap.newInstance();
			
			boolean error = false;
			
			for (GenericValue DataImportSaldoInicialAuxiliar : listDataImportSaldoInicialAuxiliar) {
				mapDataImportSaldoInicialAuxiliar.put(
						DataImportSaldoInicialAuxiliar.getString("idAuxiliar")+DataImportSaldoInicialAuxiliar.getString("auxiliarProducto"), 
						DataImportSaldoInicialAuxiliar);
			}
			
			String auxiliarProducto = new String();
			BigDecimal monto = BigDecimal.ZERO;
			for (GenericValue DataImportSaldoInicialAuxiliarValida : listDataImportSaldoInicialAuxiliarValida) {
				
				String nombreCampoValidar = new String();
				String nombreCampoTipoValidar = new String();
				String nombreCampoActivoValidar = new String();
				String estatusDeshabilitado = new String();
				
				GenericValue DataImportSaldoInicialAuxiliar = mapDataImportSaldoInicialAuxiliar.get(
						DataImportSaldoInicialAuxiliarValida.getString("idAuxiliar")+DataImportSaldoInicialAuxiliarValida.getString("auxiliarProducto"));
				
				UtilImport.limpiarRegistroError(DataImportSaldoInicialAuxiliar);
				
				auxiliarProducto = DataImportSaldoInicialAuxiliarValida.getString("auxiliarProducto");
				
				if(UtilValidate.isEmpty(DataImportSaldoInicialAuxiliarValida.getString("idAuxiliar"))){
					UtilImport.registrarError(DataImportSaldoInicialAuxiliar, "Debe ingresar el n\u00famero de auxiliar");
					error = true;
				}
				if(UtilValidate.isEmpty(auxiliarProducto)){
					UtilImport.registrarError(DataImportSaldoInicialAuxiliar, "Debe ingresar el tipo de auxiliar (A, P \u00f3 B)");
					error = true;
				} else {
					auxiliarProducto.trim().toUpperCase();
					if(!(auxiliarProducto.equals("A") || auxiliarProducto.equals("P") || auxiliarProducto.equals("B"))){
						UtilImport.registrarError(DataImportSaldoInicialAuxiliar, "El tipo de auxiliar debe ser (A, P \u00f3 B)");
						error = true;
					}
				}
				
				if(UtilValidate.isEmpty(DataImportSaldoInicialAuxiliarValida.getString("tipo"))){
					UtilImport.registrarError(DataImportSaldoInicialAuxiliar, "Debe ingresar el tipo de auxiliar");
					error = true;
				}
				
				if(UtilValidate.isEmpty(DataImportSaldoInicialAuxiliarValida.getString("organizationId"))){
					UtilImport.registrarError(DataImportSaldoInicialAuxiliar, "Debe ingresar el n\u00famero de organizaci\u00f3n");
					error = true;
				} else {
					if(UtilValidate.isEmpty(DataImportSaldoInicialAuxiliarValida.getString("partyIdOrg"))){
						UtilImport.registrarError(DataImportSaldoInicialAuxiliar, "La organizaci\u00f3n ["+DataImportSaldoInicialAuxiliarValida.getString("organizationId")+"] no existe en el sistema");
						error = true;
					} else {
						if(UtilValidate.isEmpty(DataImportSaldoInicialAuxiliarValida.getString("partyIdCont"))){
							UtilImport.registrarError(DataImportSaldoInicialAuxiliar, "La organizaci\u00f3n ingresada no esta configurada como una organizaci\u00f3n contabilizadora");
							error = true;
						}
					}
				}
				if(UtilValidate.isEmpty(DataImportSaldoInicialAuxiliarValida.getBigDecimal("monto"))){
					UtilImport.registrarError(DataImportSaldoInicialAuxiliar, "Debe ingresar el monto del saldo inicial");
					error = true;
				} else {
					monto = DataImportSaldoInicialAuxiliarValida.getBigDecimal("monto");
					if(monto.compareTo(BigDecimal.ZERO) == 0){
						UtilImport.registrarError(DataImportSaldoInicialAuxiliar, "El monto del saldo inicial no puede ser 0");
						error = true;	
					}
				}
				if(UtilValidate.isEmpty(DataImportSaldoInicialAuxiliarValida.getTimestamp("periodo"))){
					UtilImport.registrarError(DataImportSaldoInicialAuxiliar, "Debe ingresar periodo en el que ser\u00e1 registrado el saldo inicial");
					error = true;
				}
				
				switch (auxiliarProducto) {
					case "A":
						nombreCampoValidar = "partyId";
						nombreCampoTipoValidar = "partyTypeId";
						nombreCampoActivoValidar = "statusId";
						estatusDeshabilitado = StatusItemConstants.PartyStatus.PARTY_DISABLED;
						break;
					case "P":
						nombreCampoValidar = "productId";
						nombreCampoTipoValidar = "productTypeId";
						break;
					case "B":
						nombreCampoValidar = "cuentaBancariaId";
						nombreCampoActivoValidar = "habilitada";
						estatusDeshabilitado = "N";
						break;
					default:
						break;
				}
				
				if(UtilValidate.isNotEmpty(nombreCampoValidar)){
					if(UtilValidate.isEmpty(DataImportSaldoInicialAuxiliarValida.getString(nombreCampoValidar))){
						UtilImport.registrarError(DataImportSaldoInicialAuxiliar, "No se encontr\u00f3 el auxiliar ["+DataImportSaldoInicialAuxiliarValida.getString("idAuxiliar")+"]");
						error = true;
					}
				}
				
				if(UtilValidate.isNotEmpty(nombreCampoTipoValidar)){
					if(UtilValidate.isEmpty(DataImportSaldoInicialAuxiliarValida.getString(nombreCampoTipoValidar))){
						UtilImport.registrarError(DataImportSaldoInicialAuxiliar, "No se encontr\u00f3 el tipo ["+DataImportSaldoInicialAuxiliarValida.getString("tipo")+
								"] del auxiliar ["+DataImportSaldoInicialAuxiliarValida.getString("idAuxiliar")+"]");
						error = true;
					} else if (!DataImportSaldoInicialAuxiliarValida.getString(nombreCampoTipoValidar).equals(DataImportSaldoInicialAuxiliarValida.getString("tipo"))) {
						UtilImport.registrarError(DataImportSaldoInicialAuxiliar, "El tipo de auxiliar ["+DataImportSaldoInicialAuxiliarValida.getString("tipo")+
								"] no corresponde al auxiliar ["+DataImportSaldoInicialAuxiliarValida.getString("idAuxiliar")+"]");
						error = true;
					}
				}
				
				if(UtilValidate.isNotEmpty(nombreCampoActivoValidar) && 
						UtilValidate.isNotEmpty(DataImportSaldoInicialAuxiliarValida.getString(nombreCampoActivoValidar)) &&
							DataImportSaldoInicialAuxiliarValida.getString(nombreCampoActivoValidar).equals(estatusDeshabilitado)){
					UtilImport.registrarError(DataImportSaldoInicialAuxiliar, "El auxiliar ["+DataImportSaldoInicialAuxiliarValida.getString("idAuxiliar")+"] no esta activo");
					error = true;
				}
				
			}
			
			List<GenericValue> listSaldoCatalogo = FastList.newInstance(); 
			
			orderBy = UtilMisc.toList("idAuxiliar","tipo","organizationId","auxiliarProducto");
			
			TransactionUtil.begin(43200);
			
			if(!error){
				
				List<GenericValue> listAuxiliarSaldo = delegator.findByCondition("DataImportSaldoInicialAuxiliarSaldo", condicionSinProcesar, null, orderBy);
				
				for (GenericValue Auxiliar : listAuxiliarSaldo) {
					
					GenericValue SaldoCatalogo = Auxiliar.getRelatedOne("SaldoCatalogo");
					
					if(UtilValidate.isEmpty(SaldoCatalogo)){
				        //Creamos SaldoCatalogo
				        SaldoCatalogo = delegator.makeValue("SaldoCatalogo");
				        SaldoCatalogo.set("catalogoId",Auxiliar.getString("idAuxiliar"));
				        SaldoCatalogo.set("tipoId",Auxiliar.getString("tipo"));
				        SaldoCatalogo.set("partyId",Auxiliar.getString("organizationId"));
				        SaldoCatalogo.set("tipo",Auxiliar.getString("auxiliarProducto"));
				        SaldoCatalogo.set("monto",Auxiliar.getBigDecimal("monto"));
				        SaldoCatalogo.set("periodo",UtilDateTime.getMonthStart(UtilDateTime.nowSqlDate()));
				        SaldoCatalogo.setNextSeqId();
				        listSaldoCatalogo.add(SaldoCatalogo);
				        Debug.logInfo("SaldoCatalogo creado "+SaldoCatalogo, MODULE);
					} else {
						SaldoCatalogo.put("monto", SaldoCatalogo.getBigDecimal("monto").add(Auxiliar.getBigDecimal("monto")));
						Debug.logInfo("SaldoCatalogo actualizado "+SaldoCatalogo, MODULE);
						listSaldoCatalogo.add(SaldoCatalogo);
					}
					
				}
				
				regreso = ServiceUtil.returnSuccess("Se realiz\u00f3 correctamente la carga de saldos iniciales");
				for (GenericValue DataImportSaldoInicialAuxiliar : listDataImportSaldoInicialAuxiliar) {
					UtilImport.registrarExitoso(DataImportSaldoInicialAuxiliar);
				}
				Debug.logInfo("Terminando la carga de saldos inicial de auxiliares , importados : "+listDataImportSaldoInicialAuxiliar.size(), MODULE);
			}
			
			delegator.storeAll(listSaldoCatalogo);
			delegator.storeAll(listDataImportSaldoInicialAuxiliar);
			
			TransactionUtil.commit();
			
		} catch (GenericEntityException | NullPointerException e) {
			e.printStackTrace();
		}
		
		return regreso;
	}
	
}
