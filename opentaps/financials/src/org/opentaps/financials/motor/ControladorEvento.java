package org.opentaps.financials.motor;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javolution.util.FastList;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

import com.opensourcestrategies.financials.util.UtilMotorContable;

public class ControladorEvento {
	
	
	/**
	 * Crea un evento
	 * @param dctx
	 * @param context
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,Object> creaEvento(DispatchContext dctx, Map<String, Object> context){
		
		Map<String,Object> mapaResultado = ServiceUtil.returnSuccess();
		try {
			
		Delegator delegator = dctx.getDelegator();
		
		//Se obtienen los Mapas de informacion presupuestal
		Map<String,String> descripcionPresup = (Map<String, String>) context.get("descripcionPresup");
		Map<String,String> tipoMatriz = (Map<String, String>) context.get("tipoMatriz");
		Map<String,String> cuentaCargoPresup = (Map<String, String>) context.get("cuentaCargoPresup");
		Map<String,String> cuentaAbonoPresup = (Map<String, String>) context.get("cuentaAbonoPresup");
		Map<String,String> comparacion = (Map<String, String>) context.get("comparacion");
		Map<String,String> catalogoCargoPresup = (Map<String, String>) context.get("catalogoCargoPresup");
		Map<String,String> catalogoAbonoPresup = (Map<String, String>) context.get("catalogoAbonoPresup");

		//Se obtienen los Mapas de informacion patrimonial
		Map<String,String> descripcionPatri = (Map<String, String>) context.get("descripcionPatri");
		Map<String,String> cuentaCargoPatri = (Map<String, String>) context.get("cuentaCargoPatri");
		Map<String,String> cuentaAbonoPatri = (Map<String, String>) context.get("cuentaAbonoPatri");
		Map<String,String> catalogoCargoPatri = (Map<String, String>) context.get("catalogoCargoPatri");
		Map<String,String> catalogoAbonoPatri = (Map<String, String>) context.get("catalogoAbonoPatri");
		Map<String,String> excepcion = (Map<String, String>) context.get("excepcion");
		
		//Se crea una lista con las cuentas seleccionadas en pantalla para obtener informacion de ellas
		List<String> listaCuentas = FastList.newInstance(); 
		listaCuentas.addAll(cuentaCargoPresup.values());
		listaCuentas.addAll(cuentaAbonoPresup.values());
		listaCuentas.addAll(cuentaCargoPatri.values());
		listaCuentas.addAll(cuentaAbonoPatri.values());
		
		//Se otiene la naturaleza de las cuentas
		Map<String,String> mapaCuentaNaturaleza = UtilMotorContable.getMapaCuentaNaturaleza(listaCuentas, delegator);
		
		//Se crea el tipo de transaccion
		GenericValue tipoTransaccion = delegator.makeValue("AcctgTransType");
		tipoTransaccion.set("description", context.get("descripcion"));
		tipoTransaccion.setPKFields(context);
		tipoTransaccion.setNonPKFields(context);
		tipoTransaccion.create();
		
		mapaResultado.put("acctgTransTypeId", tipoTransaccion.getString("acctgTransTypeId"));
		
		//Se crea el evento
		GenericValue evento = delegator.makeValue("EventoContable");
		evento.setPKFields(context);
		evento.setNonPKFields(context);
		evento.create();
		
		int secuencia = 1;
			//Se crean los registros de liena presupuestal
			SortedSet<String> llavePresup = new TreeSet<String>(descripcionPresup.keySet());
			for (String key : llavePresup) {
			   GenericValue lineaPresupuestal = delegator.makeValue("LineaPresupuestal");
			   lineaPresupuestal.setPKFields(context);
			   lineaPresupuestal.set("secuencia", secuencia);
			   String desc = descripcionPresup.get(key);
			   if(desc == null || desc.isEmpty()){break;}
			   validaInsertaCampo(delegator, lineaPresupuestal, "descripcion",descripcionPresup.get(key), true);
			   validaInsertaCampo(delegator, lineaPresupuestal, "tipoMatrizId",tipoMatriz.get(key), false);
			   validaInsertaCampo(delegator, lineaPresupuestal, "cuentaCargo",cuentaCargoPresup.get(key), true);
			   validaInsertaCampo(delegator, lineaPresupuestal, "cuentaAbono",cuentaAbonoPresup.get(key), true);
			   validaInsertaCampo(delegator, lineaPresupuestal, "catalogoCargo",catalogoCargoPresup.get(key), false);
			   validaInsertaCampo(delegator, lineaPresupuestal, "catalogoAbono",catalogoAbonoPresup.get(key), false);
			   setMomentosEvento(lineaPresupuestal, cuentaCargoPresup.get(key), cuentaAbonoPresup.get(key), comparacion.get(key), mapaCuentaNaturaleza);
			   //Validamos si existe un momento comparativo.
			   if(lineaPresupuestal.getString("momentoCompara")!=null&&cuentaCargoPresup.get(key).startsWith("8.2")){
				   validaInsertaCampo(delegator, lineaPresupuestal, "comparacion",comparacion.get(key), true);
			   }
				//Se crea la linea presupuestal
				lineaPresupuestal.create();
				secuencia++;
			}
			
			secuencia = 1;
			//Se crean los registros de liena patrimonial
			SortedSet<String> llavePatri = new TreeSet<String>(descripcionPatri.keySet());
			for (String key : llavePatri) {
			   GenericValue lineaPatrimonial = delegator.makeValue("LineaContable");
			   lineaPatrimonial.setPKFields(context);
			   lineaPatrimonial.set("secuencia", secuencia);		
			   String desc = descripcionPatri.get(key);
			   if(desc == null || desc.isEmpty()){break;}			   
			   validaInsertaCampo(delegator, lineaPatrimonial, "descripcion",descripcionPatri.get(key), true);
			   validaInsertaCampo(delegator, lineaPatrimonial, "cuentaCargo",cuentaCargoPatri.get(key), true);
			   validaInsertaCampo(delegator, lineaPatrimonial, "cuentaAbono",cuentaAbonoPatri.get(key), true);
			   validaInsertaCampo(delegator, lineaPatrimonial, "catalogoCargo",catalogoCargoPatri.get(key), false);
			   validaInsertaCampo(delegator, lineaPatrimonial, "catalogoAbono",catalogoAbonoPatri.get(key), false);
			   validaInsertaCampo(delegator, lineaPatrimonial, "excepcion",excepcion.get(key), false);
				//Se crea la linea presupuestal
				lineaPatrimonial.create();
				secuencia++;
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		
		return mapaResultado;
	}
	
	/**
	 * Valida los campos que no se ecuentren vacios y que las cuentas contables sean las correctas
	 * @param linea
	 * @param nombreCampo
	 * @param validaExistencia
	 * @throws GenericEntityException
	 */
	public static void validaInsertaCampo(Delegator delegator, GenericValue linea,String nombreCampo,String campo,boolean validaExistencia) throws GenericEntityException{
		String nEntidad = linea.getEntityName();
		GenericValue generic = null;
		if(campo != null && !campo.isEmpty()){
			if(nombreCampo.startsWith("cuenta")){
				if(nEntidad.equals("LineaPresupuestal")){
					if(!campo.startsWith("8")){
						throw new GenericEntityException("Modifique la cuenta "+campo+" ya que no es presupuestal");
					} else {
						linea.put(nombreCampo, campo);
					}
				} else {
					if(campo.startsWith("8")){
						throw new GenericEntityException("Modifique la cuenta "+campo+" ya que no es patrimonial");
					} else {
						linea.put(nombreCampo, campo);
					}
				}
				generic = delegator.findByPrimaryKey("GlAccount",
						UtilMisc.toMap("glAccountId", campo));
				if (!generic.getString("tipoCuentaId").equals("R")){
					throw new GenericEntityException("Modifique la cuenta "+campo+" ya que no es de registro");
				}
			} else {
				linea.put(nombreCampo, campo);
			}
		} else {
			if(validaExistencia){
				throw new GenericEntityException("Es necesario ingresar el campo "+nombreCampo+" ");
			}
		}
	}
	
	/**
	 * Guarda los momentos correspondientes en los campos del evento momentoCompara,momentoEjecutar1 o momentoEjecutar2
	 * @param lineaPresup
	 * @param cuentaCargo
	 * @param cuentaAbono
	 * @param comparacion
	 * @throws GenericEntityException 
	 */
	public static void setMomentosEvento(GenericValue lineaPresup, String cuentaCargo, String cuentaAbono, 
											String comparacion,Map<String,String> mapaCuentaNaturaleza) throws GenericEntityException{
		Debug.log("Entra a crear momentos en caso de que se valide Suficiencia Presupuestal");
		
		String naturalezaC = mapaCuentaNaturaleza.get(cuentaCargo), 
				   naturalezaA = mapaCuentaNaturaleza.get(cuentaAbono),
				   momentoCompara = null, momentoEjecutar1 = null,
				   momentoEjecutar2 = null;
		
		if (naturalezaC.equals("DEBIT")){
			momentoEjecutar1 = UtilMotorContable.mapaMomentos.get(cuentaCargo);
		}
		else {
			momentoCompara = UtilMotorContable.mapaMomentos.get(cuentaCargo);
		}

		if (naturalezaA.equals("CREDIT")) {
			if (momentoEjecutar1 == null){
				momentoEjecutar1 = UtilMotorContable.mapaMomentos.get(cuentaAbono);
			}
			else{
				momentoEjecutar2 = UtilMotorContable.mapaMomentos.get(cuentaAbono);
			}
		} else {
			if (momentoCompara == null) {
				momentoCompara = UtilMotorContable.mapaMomentos.get(cuentaAbono);
			}
		}
		
		if (momentoCompara == null && momentoEjecutar1 == null && momentoEjecutar2 == null) {
			throw new GenericEntityException("Ocurrio un error al generar los momentos");
		}
		
		if (momentoCompara != null && comparacion == null) {
			throw new GenericEntityException("Es necesario ingresar la validaci\u00f3n de la suficiencia presupuestaria ["+lineaPresup.getString("descripcion")+"]");
		}
		
		lineaPresup.set("momentoCompara", momentoCompara);
		lineaPresup.set("momentoEjecutar1", momentoEjecutar1);
		lineaPresup.set("momentoEjecutar2", momentoEjecutar2);
		
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String,Object> editarRegistroEvento(DispatchContext dctx, Map<String, Object> context){
		
		Map<String,Object> mapaResultado = ServiceUtil.returnSuccess();
		try {
			
		Delegator delegator = dctx.getDelegator();
		String acctgTransTypeId = String.valueOf(context.get("acctgTransTypeId"));
		
		String eliminado = eliminarEvento(delegator, acctgTransTypeId); 
		
		if (eliminado.equals("error"))
		{
			return ServiceUtil.returnError("No fue posible editar el evento");
		}
		
		//Se obtienen los Mapas de informacion presupuestal
		Map<String,String> descripcionPresup = (Map<String, String>) context.get("descripcionPresup");
		Map<String,String> tipoMatriz = (Map<String, String>) context.get("tipoMatriz");
		Map<String,String> cuentaCargoPresup = (Map<String, String>) context.get("cuentaCargoPresup");
		Map<String,String> cuentaAbonoPresup = (Map<String, String>) context.get("cuentaAbonoPresup");
		Map<String,String> comparacion = (Map<String, String>) context.get("comparacion");
		Map<String,String> catalogoCargoPresup = (Map<String, String>) context.get("catalogoCargoPresup");
		Map<String,String> catalogoAbonoPresup = (Map<String, String>) context.get("catalogoAbonoPresup");

		//Se obtienen los Mapas de informacion patrimonial
		Map<String,String> descripcionPatri = (Map<String, String>) context.get("descripcionPatri");
		Map<String,String> cuentaCargoPatri = (Map<String, String>) context.get("cuentaCargoPatri");
		Map<String,String> cuentaAbonoPatri = (Map<String, String>) context.get("cuentaAbonoPatri");
		Map<String,String> catalogoCargoPatri = (Map<String, String>) context.get("catalogoCargoPatri");
		Map<String,String> catalogoAbonoPatri = (Map<String, String>) context.get("catalogoAbonoPatri");
		Map<String,String> excepcion = (Map<String, String>) context.get("excepcion");
		
		//Se crea una lista con las cuentas seleccionadas en pantalla para obtener informacion de ellas
		List<String> listaCuentas = FastList.newInstance(); 
		listaCuentas.addAll(cuentaCargoPresup.values());
		listaCuentas.addAll(cuentaAbonoPresup.values());
		listaCuentas.addAll(cuentaCargoPatri.values());
		listaCuentas.addAll(cuentaAbonoPatri.values());
		
		//Se otiene la naturaleza de las cuentas
		Map<String,String> mapaCuentaNaturaleza = UtilMotorContable.getMapaCuentaNaturaleza(listaCuentas, delegator);
		
		//Se crea el tipo de transaccion
		GenericValue tipoTransaccion = delegator.makeValue("AcctgTransType");
		tipoTransaccion.set("description", context.get("descripcion"));
		tipoTransaccion.setPKFields(context);
		tipoTransaccion.setNonPKFields(context);
		tipoTransaccion.create();
		
		mapaResultado.put("acctgTransTypeId", tipoTransaccion.getString("acctgTransTypeId"));
		
		//Se crea el evento
		GenericValue evento = delegator.makeValue("EventoContable");
		evento.setPKFields(context);
		evento.setNonPKFields(context);
		evento.create();
		
		int secuencia = 1;
			//Se crean los registros de liena presupuestal
			SortedSet<String> llavePresup = new TreeSet<String>(descripcionPresup.keySet());
			for (String key : llavePresup) {
			   GenericValue lineaPresupuestal = delegator.makeValue("LineaPresupuestal");
			   lineaPresupuestal.setPKFields(context);
			   lineaPresupuestal.set("secuencia", secuencia);
			   String desc = descripcionPresup.get(key);
			   if(desc == null || desc.isEmpty()){break;}
			   validaInsertaCampo(delegator, lineaPresupuestal, "descripcion",descripcionPresup.get(key), true);
			   validaInsertaCampo(delegator, lineaPresupuestal, "tipoMatrizId",tipoMatriz.get(key), false);
			   validaInsertaCampo(delegator, lineaPresupuestal, "cuentaCargo",cuentaCargoPresup.get(key), true);
			   validaInsertaCampo(delegator, lineaPresupuestal, "cuentaAbono",cuentaAbonoPresup.get(key), true);
			   validaInsertaCampo(delegator, lineaPresupuestal, "catalogoCargo",catalogoCargoPresup.get(key), false);
			   validaInsertaCampo(delegator, lineaPresupuestal, "catalogoAbono",catalogoAbonoPresup.get(key), false);
			   setMomentosEvento(lineaPresupuestal, cuentaCargoPresup.get(key), cuentaAbonoPresup.get(key), comparacion.get(key), mapaCuentaNaturaleza);
			   //Validamos si existe un momento comparativo.
			   if(lineaPresupuestal.getString("momentoCompara")!=null&&cuentaCargoPresup.get(key).startsWith("8.2")){
				   validaInsertaCampo(delegator, lineaPresupuestal, "comparacion",comparacion.get(key), true);
			   }
				//Se crea la linea presupuestal
				lineaPresupuestal.create();
				secuencia++;
			}
			
			secuencia = 1;
			//Se crean los registros de liena patrimonial
			SortedSet<String> llavePatri = new TreeSet<String>(descripcionPatri.keySet());
			for (String key : llavePatri) {
			   GenericValue lineaPatrimonial = delegator.makeValue("LineaContable");
			   lineaPatrimonial.setPKFields(context);
			   lineaPatrimonial.set("secuencia", secuencia);		
			   String desc = descripcionPatri.get(key);
			   if(desc == null || desc.isEmpty()){break;}			   
			   validaInsertaCampo(delegator, lineaPatrimonial, "descripcion",descripcionPatri.get(key), true);
			   validaInsertaCampo(delegator, lineaPatrimonial, "cuentaCargo",cuentaCargoPatri.get(key), true);
			   validaInsertaCampo(delegator, lineaPatrimonial, "cuentaAbono",cuentaAbonoPatri.get(key), true);
			   validaInsertaCampo(delegator, lineaPatrimonial, "catalogoCargo",catalogoCargoPatri.get(key), false);
			   validaInsertaCampo(delegator, lineaPatrimonial, "catalogoAbono",catalogoAbonoPatri.get(key), false);
			   validaInsertaCampo(delegator, lineaPatrimonial, "excepcion",excepcion.get(key), false);
				//Se crea la linea presupuestal
				lineaPatrimonial.create();
				secuencia++;
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		
		return mapaResultado;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String,Object> editarRegistroEventoLineasCont(DispatchContext dctx, Map<String, Object> context){
		
		Map<String,Object> mapaResultado = ServiceUtil.returnSuccess();
		try {			
			
		Delegator delegator = dctx.getDelegator();
		String acctgTransTypeId = String.valueOf(context.get("acctgTransTypeId"));

		//Se obtienen los Mapas de informacion patrimonial
		Map<String,String> descripcionPatri = (Map<String, String>) context.get("descripcionPatri");
		Map<String,String> cuentaCargoPatri = (Map<String, String>) context.get("cuentaCargoPatri");
		Map<String,String> cuentaAbonoPatri = (Map<String, String>) context.get("cuentaAbonoPatri");
		Map<String,String> catalogoCargoPatri = (Map<String, String>) context.get("catalogoCargoPatri");
		Map<String,String> catalogoAbonoPatri = (Map<String, String>) context.get("catalogoAbonoPatri");
		Map<String,String> excepcion = (Map<String, String>) context.get("excepcion");
		Map<String,String> secuenciaLineaCont = (Map<String, String>) context.get("secuenciaLineaCont");		
		
		//Se crea una lista con las cuentas seleccionadas en pantalla para obtener informacion de ellas
		List<String> listaCuentas = FastList.newInstance(); 
		listaCuentas.addAll(cuentaCargoPatri.values());
		listaCuentas.addAll(cuentaAbonoPatri.values());
		
		//Se actualiza el tipo de transaccion
		GenericValue tipoTransaccion = delegator.makeValue("AcctgTransType");
		tipoTransaccion.set("description", context.get("descripcion"));
		tipoTransaccion.setPKFields(context);
		tipoTransaccion.setNonPKFields(context);
		tipoTransaccion.store();
		
		//Se actualiza el evento
		GenericValue evento = delegator.makeValue("EventoContable");
		evento.setPKFields(context);
		evento.setNonPKFields(context);
		evento.store();
		
		mapaResultado.put("acctgTransTypeId", acctgTransTypeId);
		
		String eliminado = eliminarLineasContables(delegator, acctgTransTypeId);
		if (eliminado.equals("error"))
		{
			return ServiceUtil.returnError("No fue posible editar el evento");
		}
			
		//Se crean los registros de linea patrimonial
		SortedSet<String> llavePatri = new TreeSet<String>(descripcionPatri.keySet());
		for (String key : llavePatri) {			
		   GenericValue lineaPatrimonial = delegator.makeValue("LineaContable");
		   lineaPatrimonial.setPKFields(context);
		   if(secuenciaLineaCont.get(key) != null)
			   lineaPatrimonial.set("secuencia", secuenciaLineaCont.get(key));
		   else
			   lineaPatrimonial.set("secuencia", key);
		   String desc = descripcionPatri.get(key);
		   if(desc == null || desc.isEmpty()){break;}			   
		   validaInsertaCampo(delegator, lineaPatrimonial, "descripcion",descripcionPatri.get(key), true);
		   validaInsertaCampo(delegator, lineaPatrimonial, "cuentaCargo",cuentaCargoPatri.get(key), true);
		   validaInsertaCampo(delegator, lineaPatrimonial, "cuentaAbono",cuentaAbonoPatri.get(key), true);
		   validaInsertaCampo(delegator, lineaPatrimonial, "catalogoCargo",catalogoCargoPatri.get(key), false);
		   validaInsertaCampo(delegator, lineaPatrimonial, "catalogoAbono",catalogoAbonoPatri.get(key), false);
		   validaInsertaCampo(delegator, lineaPatrimonial, "excepcion",excepcion.get(key), false);
			//Se crea la linea presupuestal
			lineaPatrimonial.create();
		}
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		
		return mapaResultado;
	}
	
	private static String eliminarEvento(Delegator delegator, String acctgTransTypeId)
	{
		
		try {
			
            EntityCondition condition = EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, acctgTransTypeId);
            delegator.removeByCondition("LineaPresupuestal", condition);
            delegator.removeByCondition("LineaContable", condition);
            delegator.removeByCondition("EventoContable", condition);
            delegator.removeByCondition("AcctgTransType", condition);
            
            return "success";

        } catch (GenericEntityException e) {
            return "error";
        }
		
	}
	
	private static String eliminarLineasContables(Delegator delegator, String acctgTransTypeId)
	{
		
		try {
			
            EntityCondition condition = EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, acctgTransTypeId);
            delegator.removeByCondition("LineaContable", condition);
            return "success";

        } catch (GenericEntityException e) {
            return "error";
        }
		
	}
	
	public static Map<String,Object> eliminarRegistroEvento(DispatchContext dctx, Map<String, Object> context){
		
		Map<String,Object> mapaResultado = ServiceUtil.returnSuccess("El evento fue eliminado satisfactoriamente");
		Delegator delegator = dctx.getDelegator();
		String acctgTransTypeId = String.valueOf(context.get("acctgTransTypeId"));
		
		String eliminado = eliminarEvento(delegator, acctgTransTypeId); 
		
		if (eliminado.equals("error"))
		{
			return ServiceUtil.returnError("No fue posible eliminar el evento");
		}
		
		return mapaResultado;
	}
	
	public static Map<String,Object> desactivarEvento(DispatchContext dctx, Map<String, Object> context){
		
		Map<String,Object> resultadoSuccessActivar = ServiceUtil.returnSuccess("El evento fue activado satisfactoriamente");
		Map<String,Object> resultadoSuccessDesactivar = ServiceUtil.returnSuccess("El evento fue desactivado satisfactoriamente");
		Delegator delegator = dctx.getDelegator();
		String acctgTransTypeId = String.valueOf(context.get("acctgTransTypeId"));
		boolean activar = true;
		
		try {
			GenericValue evento = delegator.findByPrimaryKey("EventoContable", UtilMisc.toMap("acctgTransTypeId", acctgTransTypeId));
			if (evento.getString("moduloId") != null)
			{
				if (evento.getString("moduloId").equals("DESHABILITADO"))
				{
					evento.set("moduloId", evento.getString("moduloDeshabilitadoId"));
					activar = true;
				}
				else
				{
					evento.set("moduloDeshabilitadoId", evento.getString("moduloId"));
					evento.set("moduloId", "DESHABILITADO");
					activar = false;
				}
				delegator.store(evento);
			}
			else
			{
				return ServiceUtil.returnError("No fue posible desactivar el evento");
			}
			
			if (activar)
			{
				return resultadoSuccessActivar;
			}
			else
			{
				return resultadoSuccessDesactivar;
			}
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("No fue posible desactivar el evento");
		}
		
	}
	
}
