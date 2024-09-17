/* 
 */

package org.ofbiz.common.AB;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.LineaMotorContable;

/**
 * UtilAccountingTags - Utilities for the accounting tag system.
 */
public final class UtilCommon {

    private static final String MODULE = UtilCommon.class.getName();

    /** Number of tags defined in <code>AcctgTagEnumType</code>. */
    public static final int TAG_COUNT = 15;
    public static BigDecimal TOTAL_MONTOS_CONT = BigDecimal.ZERO;
    public static BigDecimal TOTAL_MONTOS_PRES = BigDecimal.ZERO;
    

    private UtilCommon() { }
    
    /**
	 * Regresa una lista de StatusWorkFlow
	 * @param workFlowId
	 * @param statusId
	 * @param delegator
	 * @return
	 * @throws GenericEntityException
	 */
	public static String getOrganizationPartyId(String userLoginId, Delegator delegator) throws GenericEntityException
	{	EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
				  EntityCondition.makeCondition("userLoginId", EntityOperator.EQUALS, userLoginId),
				  EntityCondition.makeCondition("userPrefTypeId", EntityOperator.EQUALS, "ORGANIZATION_PARTY"));
		List<GenericValue> organizatioPartyList = delegator.findByCondition("UserPreference", condiciones, null, null);
		String organizationPartyId = null;
		if(!organizatioPartyList.isEmpty())
		{	GenericValue orgaPartyId = organizatioPartyList.get(0);
			organizationPartyId = orgaPartyId.getString("userPrefValue");
			
		}
		return organizationPartyId;
	}		
	
	/**
	 * Obtiene lineas contables a partir del contexto de la pantalla 
	 * @param acctgTransTypeId
	 * @param montoMapa
	 * @param catalogoCargoMapa
	 * @param catalogoAbonoMapa
	 * @param delegator
	 * @param context
	 * @return
	 * @throws GenericEntityException
	 */
	public static LinkedList<LineaMotorContable> getLineasContables(String acctgTransTypeId,
							Delegator delegator, Map<?, ?> context, String catalogoParty, String cuentaBancariaId,
							Map<String, Object> mapaMontoClave,//(indice,montoclave)
							Map<String, Object> mapaProducto//(indice,productId)
							) throws GenericEntityException{
		
		Map<String,Object> mapaClave = UtilGenerics.checkMap(context.get("clavePresupuestalMap"));//(indice,clave)
		Map<String,Object> mapaClaveFecha = UtilGenerics.checkMap(context.get("fechaPresupuestalMap"));//(indice,FechaPresupuestal)
        Map<String,Object> mapaMonto = UtilGenerics.checkMap(context.get("montoMap"));//(descr+indice,monto)
        Map<String,Object> mapaCargoCont = UtilGenerics.checkMap(context.get("catalogoCargoContMap"));//(descr+indice,catalogoCargoCont)
        Map<String,Object> mapaAbonoCont = UtilGenerics.checkMap(context.get("catalogoAbonoContMap"));//(descr+indice,catalogoAbonoCont)
        Map<String,Object> mapaCargoPres = UtilGenerics.checkMap(context.get("catalogoCargoPresMap"));//(descr+indice,catalogoCargoPres)
        Map<String,Object> mapaAbonoPres = UtilGenerics.checkMap(context.get("catalogoAbonoPresMap"));//(descr+indice,catalogoAbonoPres)
		
        Debug.logInfo("contextcontext: " + context,MODULE);
        Debug.logInfo("mapaMontomapaMonto: " + mapaMonto,MODULE);
        Debug.logInfo("mapaCargoPres: " + mapaCargoPres,MODULE);
        Debug.logInfo("mapaAbonoPres: " + mapaAbonoPres,MODULE);
        
		LinkedList<LineaMotorContable> listLineasMotor = new LinkedList<LineaMotorContable>();
		
		List<String> orderBy = UtilMisc.toList("secuencia");
		List<GenericValue> listaLineasContables = delegator.findByAnd("LineaContable", UtilMisc.toMap("acctgTransTypeId",acctgTransTypeId),orderBy);
		List<GenericValue> listaLineasPresup = delegator.findByAnd("LineaPresupuestal", UtilMisc.toMap("acctgTransTypeId",acctgTransTypeId),orderBy);
		
		boolean tieneContables = (listaLineasContables == null ? false : (listaLineasContables.isEmpty() ? false : true ));
		boolean tienePresup = (listaLineasPresup == null ? false : (listaLineasPresup.isEmpty() ? false : true ));
		boolean esMasDeUnoPresup = (tienePresup ? (listaLineasPresup.size() > 1)  : false );
		
		boolean tieneAuxiliarPres = false;
		for (GenericValue lineaPres : listaLineasPresup) {
			String catCargo = lineaPres.getString("catalogoCargo");
			String catAbono = lineaPres.getString("catalogoAbono");
			if((catCargo != null && !catCargo.isEmpty()) || (catAbono != null && !catAbono.isEmpty())){
				tieneAuxiliarPres = true;
			}
		}
		
		GenericValue lineaPresup = null;
		if(tienePresup){
			lineaPresup = listaLineasPresup.get(0);
		}
		
		BigDecimal suma = BigDecimal.ZERO;

		//Evento con lineas contables y presupuestales
		if(tieneContables && tienePresup){

			for (Entry<String, Object> mapa : mapaClave.entrySet()) {
				BigDecimal monto = BigDecimal.ZERO;
				if(mapaMontoClave.get(mapa.getKey()) != null)
				{	
					monto = BigDecimal.valueOf(Double.valueOf((String) mapaMontoClave.get(mapa.getKey())));
				}					
				
				GenericValue lineaMonto = delegator.makeValue("LineaContable");
				
				Map<String,GenericValue> mapaLineasCont = getListaLineasContables(mapaMonto, mapaCargoCont, mapaAbonoCont, listaLineasContables, lineaMonto, mapa.getKey());
				Map<String,GenericValue> mapaLineasPres = FastMap.newInstance();
				
				if(lineaMonto.getBigDecimal("monto").equals(BigDecimal.ZERO)){
					suma = monto;
				} else {
					suma = lineaMonto.getBigDecimal("monto");
				}
				
				if(esMasDeUnoPresup && tieneAuxiliarPres){
					mapaLineasPres = getListaLineasPresup(mapaCargoPres, mapaAbonoPres, listaLineasPresup, mapa.getKey());
				} else if(esMasDeUnoPresup && !tieneAuxiliarPres){
					int i = 0;
					for (GenericValue lineaP : listaLineasPresup) {
						mapaLineasPres.putAll(getLineaPresupuestal(lineaP, catalogoParty, cuentaBancariaId, 
								(String) mapaProducto.get(mapa.getKey()), String.valueOf(i)));
						i++;
					}
				} else if(tienePresup){
					mapaLineasPres = getLineaPresupuestal(lineaPresup, catalogoParty, cuentaBancariaId, (String) mapaProducto.get(0),"0");
				} 
								
				TOTAL_MONTOS_PRES = TOTAL_MONTOS_PRES.add(suma);
				if(suma.compareTo(BigDecimal.ZERO) != 0)
				{	
					listLineasMotor.add(getLineaDeMapa((String) mapa.getValue(), mapaLineasCont, mapaLineasPres, suma,mapa.getKey(), mapaClaveFecha));
				}								
			}
			if(TOTAL_MONTOS_PRES.compareTo(BigDecimal.ZERO) == 0)			
			{	
				throw new GenericEntityException("No se han ingresado montos presupuestales para afectar");
			}			
		
		//Evento con solo lineas contables
		} else if (tieneContables && !tienePresup){

			for (Entry<String, Object> mapa : mapaClave.entrySet()) {
				
				GenericValue lineaMonto = delegator.makeValue("LineaContable");
				
				Map<String,GenericValue> mapaLineasContables = getListaLineasContables(mapaMonto, mapaCargoCont, mapaAbonoCont, listaLineasContables, lineaMonto, mapa.getKey());
				Map<String,GenericValue> mapaLineasPres = FastMap.newInstance();
				
				if(lineaMonto.getBigDecimal("monto").equals(BigDecimal.ZERO)){
					throw new GenericEntityException("El monto ingresado es incorrecto, o el evento esta mal configurado, verifiquelo ");
				}
				else {
					suma = lineaMonto.getBigDecimal("monto");
				}
				TOTAL_MONTOS_PRES = TOTAL_MONTOS_PRES.add(suma);
				if(suma.compareTo(BigDecimal.ZERO) != 0)
				{	
					listLineasMotor.add(getLineaDeMapa(null, mapaLineasContables, mapaLineasPres, suma,mapa.getKey(),mapaClaveFecha));
				}				
			}
			if(TOTAL_MONTOS_PRES.compareTo(BigDecimal.ZERO) == 0)			
			{	
				throw new GenericEntityException("No se han ingresado montos presupuestales para afectar");
			}
		//Evento con solo lineas presupuestales
		} else if (tienePresup && !tieneContables){

			Debug.logInfo("esMasDeUnoPresup: " + esMasDeUnoPresup,MODULE);
			Debug.logInfo("tieneAuxiliarPres: " + tieneAuxiliarPres,MODULE);
			Debug.logInfo("tienePresup: " + tienePresup,MODULE);
			for (Entry<String, Object> montoClave : mapaMontoClave.entrySet()) {
				Map<String,GenericValue> mapaLineasContables = FastMap.newInstance();
				Map<String,GenericValue> mapaLineasPres = FastMap.newInstance();
				
				if(esMasDeUnoPresup && tieneAuxiliarPres){
					mapaLineasPres = getListaLineasPresup(mapaCargoPres, mapaAbonoPres, listaLineasPresup, montoClave.getKey());
				} else if(esMasDeUnoPresup && !tieneAuxiliarPres){
					int i = 0;
					for (GenericValue lineaP : listaLineasPresup) {
						mapaLineasPres.putAll(getLineaPresupuestal(lineaP, catalogoParty, cuentaBancariaId, 
								(String) mapaProducto.get(montoClave.getKey()), String.valueOf(i)));
						i++;
					}
				} else if(tienePresup){
					mapaLineasPres = getLineaPresupuestal(lineaPresup, catalogoParty, cuentaBancariaId, (String) mapaProducto.get(0),"0");
				} 
				Debug.logInfo("mapaMontoClave: " + mapaMontoClave,MODULE);
				String montoString = (String) mapaMontoClave.get(montoClave.getKey());
				BigDecimal monto = new BigDecimal(montoString);
				listLineasMotor.add(getLineaDeMapa((String) mapaClave.get(montoClave.getKey()), mapaLineasContables, mapaLineasPres,
						monto,montoClave.getKey(),mapaClaveFecha));
			}
			
		}
		
		return listLineasMotor;
	}
	
	/**
	 * Obtiene una linea presupuestal del mapa de lineas contables
	 * @param clavePresupuestal
	 * @param mapaLineasContables
	 * @param monto
	 * @param catalogoCargo
	 * @param catalogoAbono
	 * @return
	 */
	public static LineaMotorContable getLineaDeMapa(String clavePresupuestal, Map<String,GenericValue> mapaLineasContables,
				Map<String,GenericValue> mapaLineasPresupuestales,BigDecimal monto,String indice, Map<String,Object> mapaClaveFecha){
		
		LineaMotorContable linea = new LineaMotorContable();
		
		if(UtilValidate.isNotEmpty(clavePresupuestal)){
			linea.setClavePresupuestal(clavePresupuestal);
		}
		if(UtilValidate.isNotEmpty(mapaClaveFecha)){
			Timestamp fechaPresupuestal = (Timestamp) mapaClaveFecha.get(indice);
			if(UtilValidate.isNotEmpty(fechaPresupuestal)){
				linea.setFecha(fechaPresupuestal);
			}
		}

		linea.setMontoPresupuestal(monto);
		linea.setLineasContables(mapaLineasContables);
		linea.setLineasPresupuestales(mapaLineasPresupuestales);
		linea.setIndice(indice);
		
		Debug.logInfo("la linea es: " + linea,MODULE);
		return linea;
	}
	
	/**
	 * Actualiza y obtiene una linea presupuestal
	 * @param lineaPresup
	 * @param catalogoCargo
	 * @param catalogoAbono
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map<String,GenericValue> getLineaPresupuestal(GenericValue lineaPresup, String catalogoParty, String cuentaBancariaId, String productId ,String indice) throws GenericEntityException{
		
		Map<String,GenericValue> mapaRegreso = FastMap.newInstance();
		
		GenericValue lineaPresupclon = (GenericValue) lineaPresup.clone();
		
		String secuencia = lineaPresupclon.getString("secuencia");
		//secuencia = indice+secuencia;
		
		String catCargo = lineaPresupclon.getString("catalogoCargo");
		boolean tieneCatalogoCargo = (catCargo == null ? false : !catCargo.isEmpty());		
		
		String catAbono = lineaPresupclon.getString("catalogoAbono");
		boolean tieneCatalogoAbono = (catAbono == null ? false : !catAbono.isEmpty());
		
		String auxCargo = obtenValorAuxiliar(catCargo, catalogoParty, cuentaBancariaId, productId);
		String auxAbono = obtenValorAuxiliar(catAbono, catalogoParty, cuentaBancariaId, productId);
		
		Debug.logInfo("auxCargo: " + auxCargo,MODULE);
		Debug.logInfo("auxAbono: " + auxAbono,MODULE);
		
		if(tieneCatalogoCargo && (auxCargo==null || auxCargo.isEmpty())){
			throw new GenericEntityException("Debe ingresar el auxiliar cargo de "+lineaPresupclon.getString("descripcion"));
		} else {
			lineaPresupclon.set("catalogoCargo", auxCargo);
		}
		if(tieneCatalogoAbono && (auxAbono==null || auxAbono.isEmpty())){
			throw new GenericEntityException("Debe ingresar el auxiliar abono de "+lineaPresupclon.getString("descripcion"));
		} else {
			lineaPresupclon.set("catalogoAbono", auxAbono);
		}
		
		mapaRegreso.put(secuencia, lineaPresupclon);
		
		Debug.logInfo("mapaRegreso: " + mapaRegreso,MODULE);
		
		return mapaRegreso;
	}
	
	/**
	 * Obtiene el auxiliar dependiendo del tipo de auxiliar
	 * @param tipoAuxiliarId
	 * @param catalogoParty
	 * @param cuentaBancariaId
	 * @param productId
	 * @return
	 */
	public static String obtenValorAuxiliar(String tipoAuxiliarId, String catalogoParty, String cuentaBancariaId, String productId){
		
		if(tipoAuxiliarId == null){
			return null;
		} else if (tipoAuxiliarId.equals("PRODUCTO")){
			return productId;
		} else if (tipoAuxiliarId.equals("BANCO")){
			return cuentaBancariaId;
		} else { //Party
			return catalogoParty;
		}
		
	}
	
	/**
	 * Obtiene una lista de lineas presupuestales a partir de los datos en contexto 
	 * @param mapaCargo
	 * @param mapaAbono
	 * @param listaLineasPresupuestales
	 * @param indice
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map<String,GenericValue> getListaLineasPresup(Map<String,Object> mapaCargo,Map<String,Object> mapaAbono,
			List<GenericValue> listaLineasPresupuestales,String indice) throws GenericEntityException{
	
		Map<String,GenericValue> mapaRegreso = FastMap.newInstance(); 
		
		for (GenericValue lineaPr : listaLineasPresupuestales) {
			
			GenericValue lineaPresup = (GenericValue) lineaPr.clone();
			
			String descrSinEspacios = lineaPresup.getString("descripcion").replaceAll("\\s","");
			String llaveMapa = descrSinEspacios+indice;
			
			String secuencia = lineaPresup.getString("secuencia");

			String catCargo = lineaPresup.getString("catalogoCargo");
			boolean tieneCatalogoCargo = (catCargo == null ? false : !catCargo.isEmpty());
			Debug.logInfo("catCargo: " + catCargo + ":" + llaveMapa,MODULE);
			
			String catAbono = lineaPresup.getString("catalogoAbono");
			boolean tieneCatalogoAbono = (catAbono == null ? false : !catAbono.isEmpty());
			Debug.logInfo("catAbono: " + catAbono + ":" + llaveMapa,MODULE);
			
			if(tieneCatalogoCargo && !mapaCargo.containsKey(llaveMapa)){
				throw new GenericEntityException("Debe ingresar el auxiliar cargo de "+lineaPresup.getString("descripcion"));
			} else {
				Debug.logInfo("EL cargo es: " + mapaCargo.get(llaveMapa),MODULE);
				lineaPresup.set("catalogoCargo", mapaCargo.get(llaveMapa));
			}
			if(tieneCatalogoAbono && !mapaAbono.containsKey(llaveMapa)){
				throw new GenericEntityException("Debe ingresar el auxiliar abono de "+lineaPresup.getString("descripcion"));
			} else {
				Debug.logInfo("EL abono es: " + mapaAbono.get(llaveMapa),MODULE);
				lineaPresup.set("catalogoAbono", mapaAbono.get(llaveMapa));
			}
			
			
			mapaRegreso.put(secuencia, lineaPresup);
			
		}
		return mapaRegreso;
	}
	
	/**
	 * Obtiene una lista de lineas contables a partir de los datos en contexto 
	 * @param mapaMonto
	 * @param mapaCargo
	 * @param mapaAbono
	 * @param listaLineasContables
	 * @param suma
	 * @param indice
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map<String,GenericValue> getListaLineasContables(Map<String,Object> mapaMonto,
				Map<String,Object> mapaCargo,Map<String,Object> mapaAbono,
				List<GenericValue> listaLineasContables,GenericValue lineaContableMonto,String indice) throws GenericEntityException{
		
		Map<String,GenericValue> mapaRegreso = FastMap.newInstance(); 

		BigDecimal suma = BigDecimal.ZERO;

		for (GenericValue lineaPatr : listaLineasContables) {
			
			GenericValue lineaCont = (GenericValue) lineaPatr.clone();
			
			String exep = lineaCont.getString("excepcion");
			boolean excepcion = false;
			if(exep != null && exep.equals("Y")){excepcion = true;}
			
			String descrSinEspacios = lineaCont.getString("descripcion").replaceAll("\\s","");
			Debug.logInfo("descrSinEspacios: " + descrSinEspacios,MODULE);		
			String llaveMapa = descrSinEspacios+indice;
			Debug.logInfo("llaveMapa: " + llaveMapa,MODULE);		
			String secuencia = lineaCont.getString("secuencia");
			
			String catCargo = lineaCont.getString("catalogoCargo");
			boolean tieneCatalogoCargo = (catCargo == null ? false : !catCargo.isEmpty());
			
			String catAbono = lineaCont.getString("catalogoAbono");
			boolean tieneCatalogoAbono = (catAbono == null ? false : !catAbono.isEmpty());

			if(mapaMonto.containsKey(llaveMapa)){
				String montoString = (String) mapaMonto.get(llaveMapa);
				BigDecimal monto = BigDecimal.ZERO;
				if(montoString == null || montoString.equalsIgnoreCase("")){
					//throw new GenericEntityException("Debe ingresar el monto de "+lineaCont.getString("descripcion"));
					continue;
				}
				else
				{	monto = BigDecimal.valueOf(Double.valueOf(montoString));
					TOTAL_MONTOS_CONT=TOTAL_MONTOS_CONT.add(monto);
				}				
				boolean montoCero = monto.equals(BigDecimal.ZERO);
				lineaCont.set("monto", monto);
				
				if(tieneCatalogoCargo && !mapaCargo.containsKey(llaveMapa) && !montoCero){
					throw new GenericEntityException("Debe ingresar el auxiliar cargo de "+lineaCont.getString("descripcion"));
				} else {
					lineaCont.set("catalogoCargo", mapaCargo.get(llaveMapa));
				}
				if(tieneCatalogoAbono && !mapaAbono.containsKey(llaveMapa) && !montoCero){
					throw new GenericEntityException("Debe ingresar el auxiliar abono de "+lineaCont.getString("descripcion"));
				} else {
					lineaCont.set("catalogoAbono", mapaAbono.get(llaveMapa));	
				}
			} else {
				//throw new GenericEntityException("Debe ingresar el monto de "+lineaCont.getString("descripcion"));
				continue;
			}
			
			if(!excepcion){
				suma = suma.add(BigDecimal.valueOf(Double.valueOf((String) mapaMonto.get(llaveMapa))));
				Debug.logInfo("sumaaa: " + suma,MODULE);
			}
			
			mapaRegreso.put(secuencia, lineaCont);
			
		}
						
		lineaContableMonto.set("monto", suma);
		return mapaRegreso;
	}
	
	/**
	 * Obtiene lineas contables a partir de los datos de la Orden de pago y cobro 
	 * @param acctgTransTypeId
	 * @param montoMapa
	 * @param catalogoCargoMapa
	 * @param catalogoAbonoMapa
	 * @param delegator
	 * @param context
	 * @return
	 * @throws GenericEntityException
	 */
	public static LinkedList<LineaMotorContable> getLineasContablesPagaReca(String acctgTransTypeId,
							Delegator delegator, Map<?, ?> context, String catalogoParty, String cuentaBancariaId,
							Map<String, Object> mapaMontoClave,//(indice,montoclave)
							Map<String, Object> mapaProducto//(indice,productId)
							) throws GenericEntityException{
		
		Map<String,Object> mapaClave = UtilGenerics.checkMap(context.get("clavePresupuestalMap"));//(indice,clave)
		Map<String,Object> mapaClaveFecha = UtilGenerics.checkMap(context.get("fechaPresupuestalMap"));//(indice,FechaPresupuestal)
        Map<String,Object> mapaMonto = UtilGenerics.checkMap(context.get("montoMap"));//(descr+indice,monto)
        Map<String,Object> mapaCargoCont = UtilGenerics.checkMap(context.get("catalogoCargoContMap"));//(descr+indice,catalogoCargoCont)
        Map<String,Object> mapaAbonoCont = UtilGenerics.checkMap(context.get("catalogoAbonoContMap"));//(descr+indice,catalogoAbonoCont)
        Map<String,Object> mapaCargoPres = UtilGenerics.checkMap(context.get("catalogoCargoPresMap"));//(descr+indice,catalogoCargoPres)
        Map<String,Object> mapaAbonoPres = UtilGenerics.checkMap(context.get("catalogoAbonoPresMap"));//(descr+indice,catalogoAbonoPres)
		
        Debug.logInfo("contextcontext: " + context,MODULE);
        Debug.logInfo("mapaMontomapaMonto: " + mapaMonto,MODULE);
        Debug.logInfo("mapaCargoPres: " + mapaCargoPres,MODULE);
        Debug.logInfo("mapaAbonoPres: " + mapaAbonoPres,MODULE);
        
		LinkedList<LineaMotorContable> listLineasMotor = new LinkedList<LineaMotorContable>();
		
		List<String> orderBy = UtilMisc.toList("secuencia");
		List<GenericValue> listaLineasContables = delegator.findByAnd("LineaContable", UtilMisc.toMap("acctgTransTypeId",acctgTransTypeId),orderBy);
		List<GenericValue> listaLineasPresup = delegator.findByAnd("LineaPresupuestal", UtilMisc.toMap("acctgTransTypeId",acctgTransTypeId),orderBy);
		
		boolean tieneContables = (listaLineasContables == null ? false : (listaLineasContables.isEmpty() ? false : true ));
		boolean tienePresup = (listaLineasPresup == null ? false : (listaLineasPresup.isEmpty() ? false : true ));
		boolean esMasDeUnoPresup = (tienePresup ? (listaLineasPresup.size() > 1)  : false );
		
		boolean tieneAuxiliarPres = false;
		for (GenericValue lineaPres : listaLineasPresup) {
			String catCargo = lineaPres.getString("catalogoCargo");
			String catAbono = lineaPres.getString("catalogoAbono");
			if((catCargo != null && !catCargo.isEmpty()) || (catAbono != null && !catAbono.isEmpty())){
				tieneAuxiliarPres = true;
			}
		}
		
		GenericValue lineaPresup = null;
		if(tienePresup){
			lineaPresup = listaLineasPresup.get(0);
		}
		
		BigDecimal suma = BigDecimal.ZERO;
		
		//Evento con lineas contables y presupuestales
		if(tieneContables && tienePresup){

			for (Entry<String, Object> mapa : mapaClave.entrySet()) {				
				BigDecimal monto = BigDecimal.ZERO;
				if(mapaMontoClave.get(mapa.getKey()) != null)
				{	monto = BigDecimal.valueOf(Double.valueOf((String) mapaMontoClave.get(mapa.getKey())));
					
				}					
				
				GenericValue lineaMonto = delegator.makeValue("LineaContable");
				
				Map<String,GenericValue> mapaLineasCont = getListaLineasContablesPagaReca(mapaMonto, mapaCargoCont, mapaAbonoCont, listaLineasContables, lineaMonto, mapa.getKey());
				Debug.logInfo("mapaLineasCont: " + mapaLineasCont,MODULE);
				Map<String,GenericValue> mapaLineasPres = FastMap.newInstance();
				
				if(lineaMonto.getBigDecimal("monto").equals(BigDecimal.ZERO)){
					suma = monto;
				} else {
					suma = lineaMonto.getBigDecimal("monto");
				}
				
				if(esMasDeUnoPresup && tieneAuxiliarPres){
					mapaLineasPres = getListaLineasPresup(mapaCargoPres, mapaAbonoPres, listaLineasPresup, mapa.getKey());
					Debug.logInfo("mapaLineasPres1: " + mapaLineasPres,MODULE);
				} else if(esMasDeUnoPresup && !tieneAuxiliarPres){
					int i = 0;
					for (GenericValue lineaP : listaLineasPresup) {
						mapaLineasPres.putAll(getLineaPresupuestal(lineaP, catalogoParty, cuentaBancariaId, 
								(String) mapaProducto.get(mapa.getKey()), String.valueOf(i)));
						i++;
					}
					Debug.logInfo("mapaLineasPres2: " + mapaLineasPres,MODULE);
				} else if(tienePresup){
					mapaLineasPres = getLineaPresupuestal(lineaPresup, catalogoParty, cuentaBancariaId, (String) mapaProducto.get(0),"0");
					Debug.logInfo("mapaLineasPres3: " + mapaLineasPres,MODULE);
				} 
								
				TOTAL_MONTOS_PRES = TOTAL_MONTOS_PRES.add(suma);
				Debug.logInfo("suma: " + suma,MODULE);
				listLineasMotor.add(getLineaDeMapa((String) mapa.getValue(), mapaLineasCont, mapaLineasPres, suma,mapa.getKey(),mapaClaveFecha));
				Debug.logInfo("listLineasMotor0: " + listLineasMotor,MODULE);
												
			}
			if(TOTAL_MONTOS_PRES.compareTo(BigDecimal.ZERO) == 0)			
			{	
				Debug.logWarning("No se han ingresado montos presupuestales para afectar",MODULE);
			}
			Debug.logInfo("listLineasMotor: " + listLineasMotor,MODULE);
		
		//Evento con solo lineas contables
		} else if (tieneContables && !tienePresup){

			for (Entry<String, Object> mapa : mapaClave.entrySet()) {
				
				GenericValue lineaMonto = delegator.makeValue("LineaContable");
				
				Map<String,GenericValue> mapaLineasContables = getListaLineasContablesPagaReca(mapaMonto, mapaCargoCont, mapaAbonoCont, listaLineasContables, lineaMonto, mapa.getKey());
				Map<String,GenericValue> mapaLineasPres = FastMap.newInstance();
				
				if(lineaMonto.getBigDecimal("monto").equals(BigDecimal.ZERO)){
					throw new GenericEntityException("El monto ingresado es incorrecto, o el evento esta mal configurado, verifiquelo ");
				}
				else {
					suma = lineaMonto.getBigDecimal("monto");
				}
				TOTAL_MONTOS_PRES = TOTAL_MONTOS_PRES.add(suma);
				if(suma.compareTo(BigDecimal.ZERO) != 0)
				{	listLineasMotor.add(getLineaDeMapa(null, mapaLineasContables, mapaLineasPres, suma,mapa.getKey(),mapaClaveFecha));
				}				
			}
			if(TOTAL_MONTOS_PRES.compareTo(BigDecimal.ZERO) == 0)			
			{	throw new GenericEntityException("No se han ingresado montos presupuestales para afectar");
			}
		//Evento con solo lineas presupuestales
		} else if (tienePresup && !tieneContables){

			Debug.logInfo("esMasDeUnoPresup: " + esMasDeUnoPresup,MODULE);
			Debug.logInfo("tieneAuxiliarPres: " + tieneAuxiliarPres,MODULE);
			Debug.logInfo("tienePresup: " + tienePresup,MODULE);
			for (Entry<String, Object> montoClave : mapaMontoClave.entrySet()) {
				Map<String,GenericValue> mapaLineasContables = FastMap.newInstance();
				Map<String,GenericValue> mapaLineasPres = FastMap.newInstance();
				
				if(esMasDeUnoPresup && tieneAuxiliarPres){
					mapaLineasPres = getListaLineasPresup(mapaCargoPres, mapaAbonoPres, listaLineasPresup, montoClave.getKey());
				} else if(esMasDeUnoPresup && !tieneAuxiliarPres){
					int i = 0;
					for (GenericValue lineaP : listaLineasPresup) {
						mapaLineasPres.putAll(getLineaPresupuestal(lineaP, catalogoParty, cuentaBancariaId, 
								(String) mapaProducto.get(montoClave.getKey()), String.valueOf(i)));
						i++;
					}
				} else if(tienePresup){
					mapaLineasPres = getLineaPresupuestal(lineaPresup, catalogoParty, cuentaBancariaId, (String) mapaProducto.get(0),"0");
				} 
				Debug.logInfo("mapaMontoClave: " + mapaMontoClave,MODULE);
				String montoString = (String) mapaMontoClave.get(montoClave.getKey());
				BigDecimal monto = new BigDecimal(montoString);
				listLineasMotor.add(getLineaDeMapa((String) mapaClave.get(montoClave.getKey()), mapaLineasContables, mapaLineasPres,
						monto,montoClave.getKey(), mapaClaveFecha));
			}
			
		}
		Debug.logInfo("listLineasMotor2: " + listLineasMotor,MODULE);

		return listLineasMotor;
	}
	
	/**
	 * Obtiene una lista de lineas contables a partir de los datos en contexto para el momento de pagado y recaudado
	 * @param mapaMonto
	 * @param mapaCargo
	 * @param mapaAbono
	 * @param listaLineasContables
	 * @param suma
	 * @param indice
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map<String,GenericValue> getListaLineasContablesPagaReca(Map<String,Object> mapaMonto,
				Map<String,Object> mapaCargo,Map<String,Object> mapaAbono,
				List<GenericValue> listaLineasContables,GenericValue lineaContableMonto,String indice) throws GenericEntityException{
		
		Map<String,GenericValue> mapaRegreso = FastMap.newInstance(); 

		BigDecimal suma = BigDecimal.ZERO;

		for (GenericValue lineaPatr : listaLineasContables) {
			
			GenericValue lineaCont = (GenericValue) lineaPatr.clone();
			
			String descrSinEspacios = lineaCont.getString("descripcion").replaceAll("\\s","");
			Debug.logInfo("descrSinEspacios: " + descrSinEspacios, MODULE);		
			String llaveMapa = descrSinEspacios+indice;
			Debug.logInfo("llaveMapa: " + llaveMapa, MODULE);		
			String secuencia = lineaCont.getString("secuencia");
			
			String catCargo = lineaCont.getString("catalogoCargo");
			boolean tieneCatalogoCargo = (catCargo == null ? false : !catCargo.isEmpty());
			
			String catAbono = lineaCont.getString("catalogoAbono");
			boolean tieneCatalogoAbono = (catAbono == null ? false : !catAbono.isEmpty());

			if(mapaMonto.containsKey(llaveMapa)){
				String montoString = (String) mapaMonto.get(llaveMapa);
				BigDecimal monto = BigDecimal.ZERO;
				if(montoString == null || montoString.equalsIgnoreCase("")){
					//throw new GenericEntityException("Debe ingresar el monto de "+lineaCont.getString("descripcion"));
					continue;
				}
				else
				{	monto = BigDecimal.valueOf(Double.valueOf(montoString));
					TOTAL_MONTOS_CONT=TOTAL_MONTOS_CONT.add(monto);
				}				
				boolean montoCero = monto.equals(BigDecimal.ZERO);
				lineaCont.set("monto", monto);
				
				if(tieneCatalogoCargo && !mapaCargo.containsKey(llaveMapa) && !montoCero){
					throw new GenericEntityException("Debe ingresar el auxiliar cargo de "+lineaCont.getString("descripcion"));
				} else {
					lineaCont.set("catalogoCargo", mapaCargo.get(llaveMapa));
				}
				if(tieneCatalogoAbono && !mapaAbono.containsKey(llaveMapa) && !montoCero){
					throw new GenericEntityException("Debe ingresar el auxiliar abono de "+lineaCont.getString("descripcion"));
				} else {
					lineaCont.set("catalogoAbono", mapaAbono.get(llaveMapa));	
				}
			} else {
				//throw new GenericEntityException("Debe ingresar el monto de "+lineaCont.getString("descripcion"));
				continue;
			}						
			
			mapaRegreso.put(secuencia, lineaCont);
			
		}
						
		lineaContableMonto.set("monto", suma);
		return mapaRegreso;
	}
	
}
