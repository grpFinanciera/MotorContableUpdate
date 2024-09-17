package com.opensourcestrategies.financials.transactions;

import java.sql.Timestamp;
import java.util.Map;

import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import com.ibm.icu.util.Calendar;

public class CrearOperacionPatrimonial extends MotorContableFinanzas {
	
	@SuppressWarnings({ "rawtypes" })
	public static Map creaPatrimonio(DispatchContext dctx, Map context) throws Exception {
		
		Debug.log("Ingresa a afectaci\u00f3n: " + context);
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
		
		// obtenemos informacion de pantalla
		String organizationPartyId = (String) context
				.get("organizationPartyId"), currencyUomId = (String) context
				.get("currencyUomId"), description = (String) context
				.get("description"), acctgTransTypeId = (String) context
				.get("acctgTransTypeId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
        Map clavePresupuestalMap = (Map) context.get("clavePresupuestalMap");
		
		Map result = ServiceUtil.returnSuccess();
		
		clavePresupuestalMap.put("0", "");
        context.put("clavePresupuestalMap", clavePresupuestalMap);
        
		/************************************/
        /******** Motor Contable ************/
        /************************************/
        
        Timestamp fechaTrans = new Timestamp(Calendar.getInstance().getTimeInMillis());
        Map<String, Object> input = FastMap.newInstance();
        input.put("eventoContableId", acctgTransTypeId);
        input.put("tipoClaveId", "");
        input.put("fechaRegistro", fechaTrans);
        input.put("fechaContable", fechaContable);
        input.put("currency", currencyUomId);
        input.put("usuario", userLogin.getString("userLoginId"));
        input.put("organizationId", organizationPartyId);
        input.put("descripcion", description);
        input.put("roleTypeId", "BILL_FROM_VENDOR");
        input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContables(acctgTransTypeId, delegator, context, null,null, null, null));
        
        Map<String, Object> output = dispatcher.runSync("creaTransaccionMotor", input);
    	if(ServiceUtil.isError(output)){
    		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
    	}
    	
    	GenericValue transaccion = (GenericValue) output.get("transaccion");

    	Debug.log("TRansaccion: " + transaccion);
    	
    	result.put("acctgTransId", transaccion.getString("acctgTransId"));
		
		return result;
	}
//	// private static final String MODULE = CrearOperacionPatrimonial.class.getName();
//	 public static List<Map<String, Object>> generalList = null, documentos = null;
//
//	@SuppressWarnings({ "rawtypes" })
//	public static Map creaPatrimonio(Map<String, Object> context) throws GeneralException, ParseException, Exception {
//
//		final ActionContext ac = new ActionContext(context);
//		//final Locale locale = ac.getLocale();
//		List<GenericValue> concep = null;
//		String organizationPartyId = UtilCommon.getOrganizationPartyId(ac
//				.getRequest());
//		List<String> error = new ArrayList<String>();
//
//		GenericValue userLogin = (GenericValue) context.get("userLogin");
//
//	    DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
//	    final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();
//	        
//	    Delegator delegator = ac.getDelegator();
//	        
//		if ("Y".equals(ac.getParameter("performFind"))) {
//
//			Debug.log("Entra a crear operacion patrimonial");
//			
//			eliminaPatrimonio(delegator, ledgerRepository, userLogin.getString("userLoginId"));
//			
//			generalList = obtenGeneral(ledgerRepository, userLogin.getString("userLoginId"));
//			
//			if (generalList.size() == 0) {
//				
//				Timestamp fechaContable = convertirFecha(
//						ac.getParameter("fechaContable_c_date"),
//						ac.getParameter("fechaContable_c_hour"),
//						ac.getParameter("fechaContable_c_minutes"),
//						ac.getParameter("fechaContable_c_ampm"));
//				Timestamp fechaRegistro = convertirFecha(
//						ac.getParameter("fechaTransaccion_c_date"),
//						ac.getParameter("fechaTransaccion_c_hour"),
//						ac.getParameter("fechaTransaccion_c_minutes"),
//						ac.getParameter("fechaTransaccion_c_ampm"));
//				String description = ac.getParameter("description");
//
//	            if (description == null)
//	                error.add("Es necesario ingresar la descripcion");
//	            if (fechaContable == null)
//	            	error.add("Es necesario ingresar la fecha contable");
//	            if (fechaRegistro == null)
//	            	error.add("Es necesario ingresar la fecha de transaccion");
//	            
//				// validamos si hay periodos abiertos
//				if (fechaContable != null) {
//					List<GenericValue> vPeriodos = obtenPeriodos(delegator,
//							organizationPartyId, fechaContable);
//					if (vPeriodos.isEmpty())
//						error.add("El o los periodos ha(n) sido cerrado(s) o no han sido creados");
//				}
//	            
//	            if (!error.isEmpty()) {
//					ac.put("errores", error);
//					return ServiceUtil.returnError("Error");
//				}
//
//				GenericValue operacionP = crearOPatrimonial(delegator,
//						fechaRegistro, fechaContable, organizationPartyId,
//						userLogin.getString("userLoginId"), description);
//				delegator.createOrStore(operacionP);
//
//				generalList = obtenGeneral(ledgerRepository, userLogin.getString("userLoginId"));
//				ac.put("generalList", generalList);
//
//				documentos = obtenerDocumentos(ledgerRepository);
//				ac.put("listDocumentos", documentos);
//			} else {
//				error.add("Existen movimientos pendientes, favor de cerrarlos antes de seguir con otro");
//				ac.put("errores", error);
//				return ServiceUtil.returnError("Error");
//			}
//			
//			return ServiceUtil.returnError("Ingresar concepto");
//			
//		} else if ("D".equals(ac.getParameter("performFind"))) {
//
//			Debug.log("Entra a generar operacion patrimonial detalle");
//			
//			generalList = obtenGeneral(ledgerRepository, userLogin.getString("userLoginId"));
//	        ac.put("generalList", generalList);
//	        
//	        documentos = obtenerDocumentos(ledgerRepository);
//    		ac.put("listDocumentos", documentos);
//    		
//			String idTipoDoc = ac.getParameter("idTipoDoc"), auxiliarCargoId = ac
//					.getParameter("caParty"), auxiliarAbonoId = ac
//					.getParameter("caProductId"), auxiliarCargoId2 = ac
//					.getParameter("auxiliar2"), auxiliarAbonoId2 = ac
//					.getParameter("producto2"), auxiliar = null, 
//					producto = null, auxiliarA = null, productoA = null;
//			
//			Debug.log("Datos obtenidos: ");
//			Debug.log("Auxiliar1: " + ac.getParameter("caParty") + "Auxiliar2: " + ac.getParameter("auxiliar2"));
//			Debug.log("Producto1: " + ac.getParameter("caProductId") + "Producto2: " + ac.getParameter("producto2"));
//			
//    		//validamos datos requeridos
//    		List<String> auxiliares = obtenAuxiliares(delegator,
//            		idTipoDoc);
//    		
//			List<String> obligatorios = validaObligatorios(delegator,
//					idTipoDoc, ac.getParameter("monto"),
//					ac.getParameter("currency"), auxiliarCargoId,
//					auxiliarAbonoId, auxiliarCargoId2, auxiliarAbonoId2, auxiliares);
//			
//			if (!obligatorios.isEmpty()){
//            	Iterator<String> avisos = obligatorios.iterator();
//            	while(avisos.hasNext()){
//            		error.add(avisos.next());
//            	}
//            }
//			
//			if (!error.isEmpty()) {
//				ac.put("errores", error);
//				return ServiceUtil.returnError("Error");
//			}
//            
//			if (auxiliares.get(0).equals("A"))
//				auxiliar = auxiliarCargoId;
//		    if (auxiliares.get(1).equals("A"))
//				auxiliarA = auxiliarCargoId2;
//			if (auxiliares.get(0).equals("P"))
//				producto = auxiliarAbonoId;
//		    if (auxiliares.get(1).equals("P"))
//		    	productoA = auxiliarAbonoId2;
//		    	
//		    Debug.log("Los auxiliares seran: " + auxiliares);
//			Debug.log("Auxiliar1: " + auxiliar + "Auxiliar2: " + auxiliarA);
//			Debug.log("Producto1: " + producto + "Producto2: " + productoA);
//            
//            //validamos si hay periodos abiertos
//    		List<GenericValue> vPeriodos = obtenPeriodos(delegator,
//    				organizationPartyId, (Timestamp) generalList.get(0).get("fechaContable"));
//    		 if (vPeriodos.isEmpty()) {
//             	error.add("El o los periodos ha(n) sido cerrado(s)");
//             	ac.put("errores", error);
//				return ServiceUtil.returnError("Error");
//    		 }
//
//			GenericValue concepto = crearConcepto(delegator,
//					idTipoDoc, ac.getParameter("monto"),
//					ac.getParameter("currency"), auxiliar,
//					producto, generalList, auxiliarA, productoA);
//			delegator.createOrStore(concepto);
//
//    		 List<String> fieldsToSelect = UtilMisc.toList("idDetalle");
//    		 fieldsToSelect.add("description");
//    		 fieldsToSelect.add("monto");
//    		 fieldsToSelect.add("tipoMoneda");
//    		 fieldsToSelect.add("theirPartyId");
//    		 fieldsToSelect.add("productId");
//    		 fieldsToSelect.add("theirPartyId2");
//    		 fieldsToSelect.add("productId2");
//    		 
//			EntityListBuilder operacionDetalleListBuilder = new EntityListBuilder(
//					ledgerRepository,
//					OperacionPatrimonialDetalle.class,
//					EntityCondition.makeCondition(OperacionPatrimonialDetalle.Fields.agrupadorP.name(),
//							EntityOperator.EQUALS, generalList.get(0).get("agrupadorP")),
//					fieldsToSelect,
//					UtilMisc.toList(OperacionPatrimonialDetalle.Fields.idDetalle
//							.desc()));
//	         PageBuilder<OperacionPatrimonialDetalle> pageBuilder = new PageBuilder<OperacionPatrimonialDetalle>() {
//	                public List<Map<String, Object>> build(List<OperacionPatrimonialDetalle> page) throws Exception {
//	                    List<Map<String, Object>> newPage = FastList.newInstance();
//	                    for (OperacionPatrimonialDetalle oDetalle : page) {
//	                        Map<String, Object> newRow = FastMap.newInstance();
//	                        newRow.putAll(oDetalle.toMap());
//	                        newPage.add(newRow);
//	                    }
//	                    return newPage;
//	                }
//	            };
//	            operacionDetalleListBuilder.setPageBuilder(pageBuilder);
//	            ac.put("operacionDetalleListBuilder", operacionDetalleListBuilder);
//	            
//	            return ServiceUtil.returnError("Ingresar otro concepto si es necesario");
//    		
//		} else if ("C".equals(ac.getParameter("performFind"))) {
//			
//			Debug.log("Entra a crear la poliza");
//			
//			generalList = obtenGeneral(ledgerRepository, userLogin.getString("userLoginId"));
//
//	        Timestamp fechaRegistro = (Timestamp) generalList.get(0).get("fechaTransaccion"),
//	        		fechaContable = (Timestamp) generalList.get(0).get("fechaContable");
//	        String description = (String) generalList.get(0).get("description");
//			List<Map<String, Object>> conceptosList = obtenConceptos(
//					ledgerRepository, (String) generalList.get(0).get("agrupadorP"));
//			
//			// se obiene padre
//			String partyId = obtenPadre(delegator, organizationPartyId);
//			
//			Debug.log("Se obtienen " + conceptosList.size() + " en la tabla");
//			
//			int i = 0;
//			String agrupador = null;
//			//obtenemos el tipo de transaccion
//			String tipoTransaccion = obtenTipoTransDeDocumento(delegator,
//					(String) conceptosList.get(i).get("idTipoDoc"));
//			Iterator cons = conceptosList.iterator();
//			while(cons.hasNext() && conceptosList.size() >= (i+1)) {
//				
//				Debug.log("Ingresa a crar transaccion por " + i + "vez");
//				
//				concep = creaTransaccionConcepto(delegator, agrupador,
//						fechaRegistro, fechaContable, 
//					    (String) conceptosList.get(i).get("idTipoDoc"),
//						(BigDecimal) conceptosList.get(i).get("monto"),
//						(String) conceptosList.get(i).get("tipoMoneda"),
//						(String) conceptosList.get(i).get("theirPartyId"),
//						(String) conceptosList.get(i).get("productId"),
//						organizationPartyId, userLogin.getString("userLoginId"), 
//						partyId, description,
//						(String) conceptosList.get(i).get("theirPartyId2"),
//						(String) conceptosList.get(i).get("productId2"),
//						tipoTransaccion);
//				
//				if (agrupador == null) {
//					Iterator<GenericValue> polizaIds = concep.iterator();
//					if (polizaIds.hasNext()) {
//						agrupador = ((GenericValue)polizaIds.next()).getString("agrupador");
//						Debug.log("Obtiene agrupador sino existia: " + agrupador);
//					}
//				}
//				
//				i++;
//			}
//			
//			GenericValue operacionP = actualizaOPatrimonial(delegator,
//					(String) generalList.get(0).get("agrupadorP"));
//			delegator.createOrStore(operacionP);
//			
//			ac.put("generalList", generalList);
//			ac.put("listaTrans", concep);
//			ac.put("tipoPoliza",
//					obtenTipoPolizaDescripcion(delegator, concep));
//			
//			Map result = ServiceUtil.returnSuccess();
//			return result;
//		}
//
//		Map result = ServiceUtil.returnSuccess();
//		return result;
//	}
}
