package com.opensourcestrategies.financials.transactions;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilTimer;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.common.AB.UtilCommon;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.LineaMotorContable;
import org.ofbiz.order.finaccount.UtilClavePresupuestal;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.base.entities.AcctgTrans;
import org.opentaps.base.entities.BuscaAfectacion;
import org.opentaps.common.builder.EntityListBuilder;
import org.opentaps.common.builder.ListBuilderException;
import org.opentaps.common.builder.PageBuilder;
import org.opentaps.domain.DomainsDirectory;
import org.opentaps.domain.DomainsLoader;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.financials.motor.MotorContable;
import org.opentaps.foundation.action.ActionContext;
import org.opentaps.foundation.infrastructure.Infrastructure;
import org.opentaps.foundation.infrastructure.User;
import org.opentaps.foundation.repository.RepositoryException;

import com.ibm.icu.util.Calendar;

public class ControladorAfectacion {
	
	public static Map<?,? extends Object> creaAfectacion(DispatchContext dctx, Map<?,? extends Object> context){
		
		Delegator delegator = dctx.getDelegator();
		
		String acctgTransTypeId = (String) context.get("acctgTransTypeId");
		String organizationPartyId = (String) context.get("organizationPartyId");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		String moduloId = (String) context.get("moduloId");
		String tipoMovimiento = (String) context.get("tipoMovimiento");
		String tipoAfectacion = new String();
		
		if(moduloId.equals("CONTABILIDAD_E") || moduloId.equals("MODIFICACION_E")){
			tipoAfectacion = "EGRESO";
		} else {
			tipoAfectacion = "INGRESO";
		}
		
		try {
			//Valida si se necesita ingresar cuenta bancaria
	        //validaCuentaBanco(delegator, acctgTransTypeId, context);
	        // validamos si hay periodos abiertos
	        List<GenericValue> periodosAbiertos = MotorContable.getPeriodos(delegator, organizationPartyId, fechaContable,false);
	        if (periodosAbiertos.isEmpty())
				return ServiceUtil.returnError("El o los periodos ha(n) sido cerrado(s) o no ha(n) sido creado(s)");
	        
	        GenericValue afectacion = delegator.makeValue("Afectacion");
	        String afectacionId = delegator.getNextSeqId("Afectacion");
	        afectacion.set("afectacionId", afectacionId);
	        afectacion.set("tipoAfectacion", tipoAfectacion);
	        afectacion.set("tipoMovimiento", tipoMovimiento);
	        afectacion.setNonPKFields(context);
	        delegator.create(afectacion);
			
	        Map<String,Object> retorno = FastMap.newInstance();
	        retorno.put("afectacionId", afectacion.getString("afectacionId"));
	        
	        return retorno;
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		
		
	}
	
	/**
	 * Valida si el evento cuenta con auxiliar banco y si es asi valida que sea ingresado por el usuario
	 * @param delegator
	 * @param acctgTransTypeId
	 * @param context
	 * @throws GenericEntityException
	 */
	private static void validaCuentaBanco(Delegator delegator, String acctgTransTypeId, Map<?,? extends Object> context) throws GenericEntityException{
		
		String bancoId = (String) context.get("bancoId");
		String cuentaBancariaId = (String) context.get("cuentaBancariaId");
		
        if(tieneCatalogoBanco(delegator, acctgTransTypeId)){
			if(bancoId == null || bancoId.isEmpty()){
				throw new GenericEntityException("El parametro Banco no puede estar vacio");
			}
			if(cuentaBancariaId == null || cuentaBancariaId.isEmpty()){
				throw new GenericEntityException("El parametro Cuenta Bancaria no puede estar vacio");
			}
        }

	}
	
	
	/**
	 * Verifica si el evento tiene en algun catalogo auxiliar el banco
	 * @param delegator
	 * @param acctgTransTypeId
	 * @return
	 * @throws GenericEntityException
	 */
	public static boolean tieneCatalogoBanco(Delegator delegator, String acctgTransTypeId) throws GenericEntityException{
		
        EntityConditionList<EntityExpr> condicionesBanco = EntityCondition.makeCondition(UtilMisc.toList(
                EntityCondition.makeCondition("catalogoCargo",EntityOperator.EQUALS,"BANCO"),
                EntityCondition.makeCondition("catalogoAbono",EntityOperator.EQUALS,"BANCO")), EntityOperator.OR);
        
        EntityConditionList<EntityCondition> condiciones = EntityCondition.makeCondition(UtilMisc.toList(
        		condicionesBanco,
                EntityCondition.makeCondition("acctgTransTypeId",EntityOperator.EQUALS,acctgTransTypeId)),
                EntityOperator.AND);
		
        List<GenericValue> listCont = delegator.findList("LineaContable", condiciones, null, null, null, false);
        List<GenericValue> listPresup = delegator.findList("LineaPresupuestal", condiciones, null, null, null, false);
        
        return (listCont.size() > 0 || listPresup.size() > 0);
        
	}

	/**
	 * Agrega y valida la existencia de la clave presupuestal a la Afectacion
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<?,? extends Object> agregarClaveAfecta(DispatchContext dctx, Map context){
		
		Delegator delegator = dctx.getDelegator();
		BigDecimal monto = (BigDecimal) context.get("monto");
		String partyId = (String) context.get("partyId");
		String afectacionId = (String) context.get("afectacionId");
		String ciclo = (String) context.get("ciclo");
		
		Map<String,String> mapaRegreso = FastMap.newInstance();
		
		try {
			
			GenericValue Afectacion = delegator.findByPrimaryKey("Afectacion",UtilMisc.toMap("afectacionId",afectacionId));
			//Buscamos los detalles si existen
			List<GenericValue> listaDetalle = delegator.findByAnd("AfectacionDetalle", UtilMisc.toMap("afectacionId",afectacionId));
			String afectacionSeqId = String.valueOf(listaDetalle.size()+1);
			
			GenericValue AfectacionDetalle = delegator.makeValue("AfectacionDetalle");
			AfectacionDetalle.set("afectacionId",afectacionId);
			AfectacionDetalle.set("afectacionSeqId",afectacionSeqId);
			
			String clavePres = UtilClavePresupuestal.almacenaClavePresupuestal(context, AfectacionDetalle, delegator, 
					Afectacion.getString("tipoAfectacion"), Afectacion.getString("organizationPartyId"), true,ciclo);
			
			//Buscamos claves ya ingresadas
//			List<GenericValue> listaDetalleClave = delegator.findByAnd("AfectacionDetalle", UtilMisc.toMap("afectacionId",afectacionId,"clavePresupuestal",clavePres,"productId",productId));
//			if(!listaDetalleClave.isEmpty()){
//				return ServiceUtil.returnError("La clave y el producto ya existe en la afectacion"); 
//			} /*** Ya no se valida si se repite la clave-prodcuto **/
			
			AfectacionDetalle.set("statusId","AFECTAR");
			AfectacionDetalle.set("monto",monto);
			AfectacionDetalle.set("montoRestante",monto);
			AfectacionDetalle.set("clavePresupuestal",clavePres);
			delegator.create(AfectacionDetalle);
			
			Map<String,Object> clavePresupuestalMap = FastMap.newInstance();
			Map<String,Object> mapaMontoClave = FastMap.newInstance();
			Map<String,Object> mapaProducto = FastMap.newInstance();
			clavePresupuestalMap.put("0",clavePres);
			mapaMontoClave.put("0",monto.toString());
			mapaProducto.put("0","");
			
			context.put("clavePresupuestalMap", clavePresupuestalMap);
			
			LinkedList<LineaMotorContable> lineasMotor = UtilCommon.getLineasContables(Afectacion.getString("acctgTransTypeId"), delegator, context, partyId, null, mapaMontoClave, mapaProducto);
			
			if(lineasMotor.size() > 0){
				LineaMotorContable lineaMotor = lineasMotor.get(0);
				Map<String, GenericValue> mapaCont = lineaMotor.getLineasContables();
				Map<String, GenericValue> mapaPres = lineaMotor.getLineasPresupuestales();
				
				int secuencia = 1;
				String tipoLinea = "PRESUPUESTO";
				for(Entry<String, GenericValue> regPres : mapaPres.entrySet()) {
					guardaDetalleLinea(delegator, afectacionId, afectacionSeqId,String.valueOf(secuencia), tipoLinea, regPres,Afectacion.getEntityName(),lineaMotor.getMontoPresupuestal());
					secuencia++;
				}
				tipoLinea = "CONTABILIDAD";
				for(Entry<String, GenericValue> regCont : mapaCont.entrySet()) {
					guardaDetalleLinea(delegator, afectacionId, afectacionSeqId,String.valueOf(secuencia), tipoLinea, regCont,Afectacion.getEntityName(),lineaMotor.getMontoPresupuestal());
					secuencia++;
				}
			}
			
			mapaRegreso.put("afectacionId",afectacionId);
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		
		return mapaRegreso;
	}
	
	/**
	 * Genera un registro de AfectacionDetalleLinea
	 * @param delegator
	 * @param afectacionId
	 * @param afectacionSeqId
	 * @param iteracion
	 * @param tipoLinea
	 * @param entryLinea
	 * @return
	 * @throws GenericEntityException 
	 */
	private static boolean guardaDetalleLinea(Delegator delegator,String afectacionId, String afectacionSeqId,String secuencia,
			String tipoLinea,Entry<String, GenericValue> entryLinea,String nombreTabla,BigDecimal monto) throws GenericEntityException{
		
		GenericValue Linea = entryLinea.getValue();
		GenericValue AfectacionDetalleLinea = delegator.makeValue("AfectacionDetalleLinea");
		String valorCargo = Linea.getString("catalogoCargo");
		String valorAbono = Linea.getString("catalogoAbono");
		String valorMonto = Linea.getString("monto");
		valorCargo = (valorCargo == null ? "" : valorCargo);
		valorAbono = (valorAbono == null ? "" : valorAbono);
		valorMonto = (valorMonto == null ? "" : valorMonto);
		monto = (entryLinea.getValue().getBigDecimal("monto") == null ? monto : entryLinea.getValue().getBigDecimal("monto"));
		if(!valorCargo.isEmpty() || !valorAbono.isEmpty() || !valorMonto.isEmpty()){
			GenericValue linea = delegator.findByPrimaryKey(entryLinea.getValue().getEntityName(), entryLinea.getValue().getPrimaryKey());
			String catalogoCargo = (linea.get("catalogoCargo") != null ? (linea.getString("catalogoCargo").isEmpty() ? null : linea.getString("catalogoCargo") ) :null);
			String catalogoAbono = (linea.get("catalogoAbono") != null ? (linea.getString("catalogoAbono").isEmpty() ? null : linea.getString("catalogoAbono") ) :null);
			AfectacionDetalleLinea.set("afectacionId",afectacionId);
			AfectacionDetalleLinea.set("afectacionSeqId",afectacionSeqId);
			AfectacionDetalleLinea.set("secuencia", secuencia);
			AfectacionDetalleLinea.set("tipoLinea", tipoLinea);	
			AfectacionDetalleLinea.set("descripcion", entryLinea.getValue().getString("descripcion"));
			AfectacionDetalleLinea.set("nombreTabla", nombreTabla);
			AfectacionDetalleLinea.set("valorCargo", valorCargo);
			AfectacionDetalleLinea.set("valorAbono", valorAbono);
			AfectacionDetalleLinea.set("catalogoCargo", catalogoCargo);
			AfectacionDetalleLinea.set("catalogoAbono", catalogoAbono);
			AfectacionDetalleLinea.set("monto", monto);
			AfectacionDetalleLinea.create();
			return true;
		}
		return false;
	}
	
	/**
	 * Obtiene las lineas contables y/o presupuestales de una clave presupuestal/producto
	 * @param delegator
	 * @param afectacionId
	 * @param afectacionSeqId
	 * @return
	 * @throws GenericEntityException
	 */
	public static List<GenericValue> getLineasClave(Delegator delegator,
			String afectacionId, String afectacionSeqId) throws GenericEntityException{
		
		List<GenericValue> listLineas = delegator.findByAnd("AfectacionDetalleLinea",
				UtilMisc.toMap("afectacionId",afectacionId,"afectacionSeqId",afectacionSeqId));
		
		return listLineas;
	}
	
	/**
	 * Actualiza el monto y los catalogos auxiliares si es que tiene de un renglon ingresado
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<?,? extends Object> actualizaDetalle(DispatchContext dctx, Map<String,? extends Object> context){
		
		Debug.log("Contextoo: " + context);
		Delegator delegator = dctx.getDelegator();
		String afectacionId = (String) context.get("afectacionId");
		String afectacionSeqId = (String) context.get("afectacionSeqId");
//		BigDecimal montoClave = (BigDecimal) context.get("montoClave");
		Map<String,String> montoClaveMap = (Map<String, String>) context.get("montoClave");
		Map<String,String> montoMap = (Map<String, String>) context.get("monto");
		Map<String,String> mapCargo = (Map<String,String>) context.get("catalogoCargo");
		Map<String,String> mapAbono = (Map<String,String>) context.get("catalogoAbono");
		
		Map<String,String> mapaRegreso = FastMap.newInstance();
		
		Debug.log("MontoCave.- " + montoClaveMap);

		try {
			
			String monto = null;
			for(String key : montoClaveMap.keySet()){
				   monto = montoClaveMap.get(key);
			}
			Debug.log("Monto.- " + monto);
			
			GenericValue AfectacionDetalle = delegator.findByPrimaryKey("AfectacionDetalle", 
					UtilMisc.toMap("afectacionId",afectacionId,"afectacionSeqId",afectacionSeqId));
			AfectacionDetalle.set("monto", monto);
			AfectacionDetalle.set("montoRestante", monto);
			AfectacionDetalle.store();
			
			List<GenericValue> listaDetalleLinea = delegator.findByAnd(
					"AfectacionDetalleLinea", UtilMisc.toMap("afectacionId",afectacionId,"afectacionSeqId",afectacionSeqId));
			for (GenericValue detalleLinea : listaDetalleLinea) {
				String llave = afectacionSeqId+detalleLinea.getString("secuencia");
				String cargo = (mapCargo.get(llave) == null ? detalleLinea.getString("valorCargo") : 
									mapCargo.get(llave).isEmpty() ? detalleLinea.getString("valorCargo") : mapCargo.get(llave));
				String abono = (mapAbono.get(llave) == null ? detalleLinea.getString("valorAbono") : 
									mapAbono.get(llave).isEmpty() ? detalleLinea.getString("valorAbono") : mapAbono.get(llave));
				detalleLinea.set("monto", montoMap.get(llave));
				detalleLinea.set("valorCargo", cargo);
				detalleLinea.set("valorAbono", abono);
				detalleLinea.store();
			}
			
			mapaRegreso.put("afectacionId", afectacionId);
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		
		
		return mapaRegreso;
	}
	
	/**
	 * Actualiza el estado de el detalle de afectacion 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<?,? extends Object> actualizaEstatusDetalle(DispatchContext dctx, Map<String,? extends Object> context){
		Delegator delegator = dctx.getDelegator();
		String afectacionId = (String) context.get("afectacionId");
		String afectacionSeqId = (String) context.get("afectacionSeqId");
		
		Map<String,String> mapaRegreso = FastMap.newInstance();
		
		try {
			GenericValue AfectacionDetalle = delegator.findByPrimaryKey("AfectacionDetalle", 
								UtilMisc.toMap("afectacionId",afectacionId,"afectacionSeqId",afectacionSeqId));
			String statusId = AfectacionDetalle.getString("statusId");
			if(statusId.equals("AFECTAR")){
				AfectacionDetalle.set("statusId", "NO_AFECTAR");
			} else {
				AfectacionDetalle.set("statusId", "AFECTAR");
			}
			
			AfectacionDetalle.store();
			
			mapaRegreso.put("afectacionId", afectacionId);
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		
		return mapaRegreso;
	}
	
	/**
	 * Crea las transacciones genera los impactos contables y presupuestales
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<?,? extends Object> creaTransaccionAfectacion(DispatchContext dctx, Map<String,? extends Object> context){
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		String afectacionId = (String) context.get("afectacionId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		
		Map<String,String> mapaRegreso = FastMap.newInstance();
		
		try {
			
			GenericValue Afectacion = delegator.findByPrimaryKey("Afectacion", 
								UtilMisc.toMap("afectacionId",afectacionId));
			List<GenericValue> listAfectaDetalle = delegator.findByAnd("AfectacionDetalle", UtilMisc.toMap("afectacionId",afectacionId,"statusId","AFECTAR"));
			
	        Map<String, Object> input = FastMap.newInstance();
	        input.put("eventoContableId",Afectacion.getString("acctgTransTypeId"));
	        input.put("tipoClaveId",Afectacion.getString("tipoAfectacion"));
	        input.put("fechaRegistro",Afectacion.get("fechaTransaccion"));
	        input.put("fechaContable",Afectacion.get("fechaContable"));
	        input.put("currency", Afectacion.get("currency"));
	        input.put("usuario", userLogin.getString("userLoginId"));
	        input.put("organizationId",Afectacion.getString("organizationPartyId"));
	        input.put("descripcion", Afectacion.getString("comentario"));
	        input.put("campo","afectacionId");
	        input.put("valorCampo",afectacionId);
	        input.put("tipoMovimiento", Afectacion.get("tipoMovimiento"));
	        
	        generaMapasLineaMotor(delegator, Afectacion, listAfectaDetalle, input);
	        
	        input = dispatcher.runSync("creaTransaccionMotor", input);
	        
	        if(ServiceUtil.isError(input)){
	        	return ServiceUtil.returnError(ServiceUtil.getErrorMessage(input));
	        } else {
            	GenericValue transaccion = (GenericValue) input.get("transaccion");
            	String acctgTransId = transaccion.getString("acctgTransId");
            	String poliza = transaccion.getString("poliza");
            	
            	for (GenericValue detalle : listAfectaDetalle) {
            		GenericValue AfectacionDetallePoliza = delegator.makeValue("AfectacionDetallePoliza");
            		AfectacionDetallePoliza.set("afectacionId", afectacionId);
            		AfectacionDetallePoliza.set("afectacionSeqId", detalle.getString("afectacionSeqId"));
            		AfectacionDetallePoliza.set("acctgTransId", acctgTransId);
            		AfectacionDetallePoliza.set("poliza", poliza);
            		AfectacionDetallePoliza.set("nombreTabla", Afectacion.getEntityName());
            		delegator.create(AfectacionDetallePoliza);
				}
            	Afectacion.set("statusId", "AFECTADA");
            	Afectacion.store();
            	mapaRegreso.put("acctgTransId", acctgTransId);
        	}
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		} catch (GenericServiceException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		
		return mapaRegreso;
	}	
	
	/**
	 * Metodo que genera los mapas necesarios para enviar al metodo que prepara las lineas motor y los guarda en el mapa
	 * @return
	 * @throws GenericEntityException 
	 */
	public static void generaMapasLineaMotor(Delegator delegator,GenericValue Afectacion, 
			List<GenericValue> listAfectaDetalle,Map<String, Object> input) throws GenericEntityException{
		
		Map<String,Object> mapaProducto = FastMap.newInstance();
		Map<String,Object> mapaMontoClave = FastMap.newInstance();
		Map<String,Object> clavePresupuestalMap = FastMap.newInstance();
		Map<String,Object> montoMap = FastMap.newInstance();
		Map<String,Object> catalogoCargoContMap = FastMap.newInstance();
		Map<String,Object> catalogoAbonoContMap = FastMap.newInstance();
		Map<String,Object> catalogoCargoPresMap = FastMap.newInstance();
		Map<String,Object> catalogoAbonoPresMap = FastMap.newInstance();
		
		int iteracion = 0;
		for (GenericValue afectaDetalle : listAfectaDetalle) {
			String llaveSimple = Integer.toString(iteracion);
			
			mapaMontoClave.put(llaveSimple, afectaDetalle.getString("monto"));
			clavePresupuestalMap.put(llaveSimple,afectaDetalle.getString("clavePresupuestal"));
			
			//Se buscan las lineas relacionadas a ese detalle para generar los siguientes mapas
			EntityConditionList<EntityExpr> condicion = EntityConditionList.makeCondition(
					UtilMisc.toList(EntityCondition.makeCondition("afectacionSeqId",EntityOperator.EQUALS, afectaDetalle.getString("afectacionSeqId")),
							EntityCondition.makeCondition("afectacionId",EntityOperator.EQUALS, Afectacion.getString("afectacionId"))));
		
			List<GenericValue> listAfectaDetalleLinea = delegator.findByCondition("AfectacionDetalleLinea", condicion, null, null);
			for (GenericValue DetalleLinea : listAfectaDetalleLinea) {
				String llaveCompuesta = DetalleLinea.getString("descripcion").replaceAll("\\s","");
				llaveCompuesta = llaveCompuesta+iteracion;
				
				if(DetalleLinea.getString("tipoLinea").equals("CONTABILIDAD")){
					catalogoCargoContMap.put(llaveCompuesta, DetalleLinea.getString("valorCargo"));
					catalogoAbonoContMap.put(llaveCompuesta, DetalleLinea.getString("valorAbono"));
				} else if (DetalleLinea.getString("tipoLinea").equals("PRESUPUESTO")){
					catalogoCargoPresMap.put(llaveCompuesta, DetalleLinea.getString("valorCargo"));
					catalogoAbonoPresMap.put(llaveCompuesta, DetalleLinea.getString("valorAbono"));
				}
				
				montoMap.put(llaveCompuesta,DetalleLinea.getString("monto"));
			}
			
			iteracion++;
		}
		
		Map context = FastMap.newInstance();
		context.put("clavePresupuestalMap", clavePresupuestalMap);
		context.put("montoMap", montoMap);
		context.put("catalogoCargoContMap", catalogoCargoContMap);
		context.put("catalogoAbonoContMap", catalogoAbonoContMap);
		context.put("catalogoCargoPresMap", catalogoCargoPresMap);
		context.put("catalogoAbonoPresMap", catalogoAbonoPresMap);
		
		input.put("lineasMotor",UtilCommon.getLineasContables(Afectacion.getString("acctgTransTypeId"), delegator, context, "" ,Afectacion.getString("cuentaBancariaId"), mapaMontoClave, mapaProducto));
	}
	
	/**
	 * Metodo para buscar Afectaciones realizadas 
	 * @param context
	 */
	public static void buscaAfectacion (Map<String, Object> context){
		
        try {
        	
        	ActionContext ac = new ActionContext(context);
            DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
            final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();
			
			final String acctgTransTypeId = ac.getParameter("acctgTransTypeId");
			String numeroPoliza = ac.getParameter("numeroPoliza");
			Timestamp fechaTransaccion = ac.getTimestamp("fechaTransaccion");
			Timestamp fechaContable = ac.getTimestamp("fechaContable");
			String comentario = ac.getParameter("comentario");
			
			if(UtilValidate.isNotEmpty(acctgTransTypeId)){
				
			GenericValue Evento = ac.getDelegator().findByPrimaryKey("EventoContable", UtilMisc.toMap("acctgTransTypeId",acctgTransTypeId));
			
	    	List<EntityCondition> condicionesBusqueda = new FastList<EntityCondition>();
			condicionesBusqueda.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "AFECTADA"));
			condicionesBusqueda.add(EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, Evento.getString("predecesor")));
	        if (UtilValidate.isNotEmpty(numeroPoliza)) {
	            condicionesBusqueda.add(EntityCondition.makeCondition("poliza", EntityOperator.LIKE, "%"+numeroPoliza+"%"));
	        } 
	        if (UtilValidate.isNotEmpty(fechaTransaccion)) {
	            condicionesBusqueda.add(EntityCondition.makeCondition("fechaTransaccion", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.getDayStart(fechaTransaccion)));
	            condicionesBusqueda.add(EntityCondition.makeCondition("fechaTransaccion", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.getDayEnd(fechaTransaccion)));
	        } 
	        if (UtilValidate.isNotEmpty(fechaContable)) {
	        	condicionesBusqueda.add(EntityCondition.makeCondition("fechaContable", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.getDayStart(fechaTransaccion)));
	            condicionesBusqueda.add(EntityCondition.makeCondition("fechaContable", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.getDayEnd(fechaTransaccion)));
	        } 
	        if (UtilValidate.isNotEmpty(comentario)) {
	            condicionesBusqueda.add(EntityCondition.makeCondition("comentario", EntityOperator.LIKE, "%"+comentario+"%"));
	        }  	     
	        
	        EntityCondition condiciones = EntityCondition.makeCondition(condicionesBusqueda, EntityOperator.AND);
			
	        List<String> orderBy = UtilMisc.toList("afectacionId");
	        EntityListBuilder afectacionListBuilder = new EntityListBuilder(ledgerRepository, BuscaAfectacion.class, condiciones, null, orderBy);
	        PageBuilder<BuscaAfectacion> pageBuilder = new PageBuilder<BuscaAfectacion>() {
	            public List<Map<String, Object>> build(List<BuscaAfectacion> page) throws Exception {
	                List<Map<String, Object>> newPage = FastList.newInstance();
	                for (BuscaAfectacion afectacion : page) {
	                    Map<String, Object> newRow = FastMap.newInstance();
	                    newRow.putAll(afectacion.toMap());
	                    String evento = (afectacion.getEventoContable() ==  null? "":
	                    					afectacion.getEventoContable().getDescripcion());
	                    newRow.put("acctgTransTypeId", evento);
	                    newRow.put("eventoSeleccionado",acctgTransTypeId);
	                    newPage.add(newRow);
	                }
	                return newPage;
	            }
	        };
	        
	        afectacionListBuilder.setPageBuilder(pageBuilder);
	        ac.put("afectacionListBuilder", afectacionListBuilder);
	        
			}
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (ListBuilderException e) {
			e.printStackTrace();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Metodo que registra el momento a afectar y registra las lineas contables / presupuestales
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String,? extends Object> crearMomento(DispatchContext dctx, Map<String,? extends Object> context){
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Delegator delegator = dctx.getDelegator();
		String afectacionId = (String) context.get("afectacionId");
		String acctgTransTypeId = (String) context.get("acctgTransTypeId");
		String statusId = (String) context.get("statusId");
		String comentario = (String) context.get("comentario");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		Map<String,Object> montoClaveMap = (Map<String, Object>) context.get("montoClaveMap");
		Map<String,Object> productoMap = (Map<String,Object>) context.get("productoMap");
		Map<String,Object> clavePresupuestalMap = (Map<String,Object>) context.get("clavePresupuestalMap");
		Map<String,Object> afectacionSeqIdMap = (Map<String,Object>) context.get("afectacionSeqIdMap");
		Debug.log("context: " + context);
		
		Map<String,String> mapaRegreso = FastMap.newInstance();
		
		try {
			
			//Aqui se actualiza el monto restante de cada clave presupuestal
			Map<String,String> mapaClaveMonto = FastMap.newInstance();
			for (Entry<String, Object> mapaClave : clavePresupuestalMap.entrySet()) {
				String llave = (String) mapaClave.getValue()+afectacionSeqIdMap.get(mapaClave.getKey());//ClavePresupuestal+afectacionSeqId
				mapaClaveMonto.put(llave, (String) montoClaveMap.get(mapaClave.getKey()));
			}
			
			List<GenericValue> listAfectaDetalle = delegator.findByAnd("AfectacionDetalle", UtilMisc.toMap("afectacionId",afectacionId,"statusId","AFECTAR"));
			for (GenericValue afectaDetalle : listAfectaDetalle) {
				String llave = afectaDetalle.getString("clavePresupuestal")+afectaDetalle.getString("afectacionSeqId");
				BigDecimal monto = BigDecimal.valueOf(Double.valueOf(mapaClaveMonto.get(llave)));
				BigDecimal montoRestante = afectaDetalle.getBigDecimal("montoRestante");
				if(montoRestante.compareTo(monto) < 0){
					throw new GenericEntityException("El monto de la clave :"+afectaDetalle.getString("clavePresupuestal")+" es mayor , verifiquelo");
				}
				BigDecimal montoGuarda = montoRestante.subtract(monto);
				afectaDetalle.set("montoRestante", montoGuarda);
				delegator.store(afectaDetalle);
			}
			
			
			GenericValue Afectacion = delegator.findByPrimaryKey("Afectacion",UtilMisc.toMap("afectacionId",afectacionId));
			
			GenericValue MomentoAfecta = delegator.makeValue("MomentoAfecta");
			String momentoAfectacionId = delegator.getNextSeqId("MomentoAfecta");
			MomentoAfecta.set("momentoAfectacionId", momentoAfectacionId);
			MomentoAfecta.set("afectacionId", afectacionId);
			MomentoAfecta.set("acctgTransTypeId", acctgTransTypeId);
			MomentoAfecta.set("statusId", statusId);
			delegator.create(MomentoAfecta);
			
			long cuenta = delegator.findCountByAnd("AfectacionDetalleLinea", UtilMisc.toMap("afectacionId",afectacionId,"nombreTabla",MomentoAfecta.getEntityName()));
			
			LinkedList<LineaMotorContable> lineasMotor = UtilCommon.getLineasContables(
					MomentoAfecta.getString("acctgTransTypeId"), delegator, context, Afectacion.getString("partyId"),
					Afectacion.getString("cuentaBancariaId"), montoClaveMap, productoMap);
			
    		List<GenericValue> listDetallePoliza = FastList.newInstance();
    		long iteracion = cuenta+1;
			//Se registran los detalle linea y los de poliza
			for (LineaMotorContable lineaMotor : lineasMotor) {
				
				String indice = String.valueOf(Integer.parseInt(lineaMotor.getIndice())+1);
				Map<String, GenericValue> mapaCont = lineaMotor.getLineasContables();
				Map<String, GenericValue> mapaPres = lineaMotor.getLineasPresupuestales();
				
				String tipoLinea = "PRESUPUESTO";
				for(Entry<String, GenericValue> regPres : mapaPres.entrySet()) {
					if(guardaDetalleLinea(delegator, afectacionId, indice ,String.valueOf(iteracion), tipoLinea, regPres,MomentoAfecta.getEntityName(),lineaMotor.getMontoPresupuestal())){
						iteracion++;
					}
				}
				tipoLinea = "CONTABILIDAD";
				for(Entry<String, GenericValue> regCont : mapaCont.entrySet()) {
					if(guardaDetalleLinea(delegator, afectacionId, indice,String.valueOf(iteracion), tipoLinea, regCont,MomentoAfecta.getEntityName(),lineaMotor.getMontoPresupuestal())){
						iteracion++;
					}
				}
				
				GenericValue AfectacionDetallePoliza = delegator.makeValue("AfectacionDetallePoliza");
				AfectacionDetallePoliza.set("afectacionId", afectacionId);
				AfectacionDetallePoliza.set("afectacionSeqId", indice);
				AfectacionDetallePoliza.set("nombreTabla", MomentoAfecta.getEntityName());
				listDetallePoliza.add(AfectacionDetallePoliza);
			}
			
			Calendar calFechaRegistro = Calendar.getInstance();
			Timestamp fechaRegistro = UtilDateTime.getTimestamp(calFechaRegistro.getTimeInMillis());
			
	        Map<String, Object> input = FastMap.newInstance();
	        input.put("eventoContableId",acctgTransTypeId);
	        input.put("tipoClaveId",Afectacion.get("tipoAfectacion"));
	        input.put("fechaRegistro",fechaRegistro);
	        input.put("fechaContable",fechaContable);
	        input.put("currency", Afectacion.get("currency"));
	        input.put("usuario", userLogin.getString("userLoginId"));
	        input.put("organizationId",Afectacion.getString("organizationPartyId"));
	        input.put("descripcion", comentario);
	        input.put("lineasMotor", UtilCommon.getLineasContables(acctgTransTypeId, delegator, context, 
	        							Afectacion.getString("partyId"),Afectacion.getString("cuentaBancariaId"), montoClaveMap, productoMap));
	        input.put("campo","momentoAfectacionId");
	        input.put("valorCampo",momentoAfectacionId);

	        Map<String, Object> output = dispatcher.runSync("creaTransaccionMotor", input);
        	if(ServiceUtil.isError(output)){
        		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
        	}
        	
        	GenericValue Transaccion = (GenericValue) output.get("transaccion");
        	String acctgTransId = Transaccion.getString("acctgTransId");
        	String poliza = Transaccion.getString("poliza");
        	for (GenericValue detallePoliza : listDetallePoliza) {
        		detallePoliza.set("acctgTransId", acctgTransId);
        		detallePoliza.set("poliza", poliza);
        		delegator.create(detallePoliza);
			}
        	
        	mapaRegreso.put("acctgTransId", acctgTransId);
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		} catch (GenericServiceException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		
		return mapaRegreso;
	}

}
