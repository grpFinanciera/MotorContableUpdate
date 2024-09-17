
package org.ofbiz.order.finaccount;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.rpc.ServiceException;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;

import com.ibm.icu.util.Calendar;

import javolution.util.FastList;
import javolution.util.FastMap;

public class UtilClavePresupuestal {
	
    /** Number of tags defined in <code>AcctgTagEnumType</code>. */
    public static final int TAG_COUNT = 15;

    /** The standard prefix used in to post tag values, eg: the value for enumTypeId1 is posted as ${TAG_PARAM_PREFIX}1. */
    public static final String TAG_PARAM_PREFIX = "tag";
    /** Prefix for accounting tags on all the entities. */
    public static final String ENTITY_TAG_PREFIX = "acctgTagEnumId";
    
    public static final String VIEW_TAG_PREFIX = "clasificacion";
    
    public static final String EGRESO_TAG = "EGRESO";
    
    public static final String INGRESO_TAG = "INGRESO";
    
    private static final String module = UtilClavePresupuestal.class.getName();
    

    private static int indiceAdmin = 0;
    private static int indiceTipoGasto = 0;
    private static int indiceCOG = 0;
    private static int indiceCRI = 0;
    
    private static GenericValue estructuraPresup;
    
    public static final String DEVOLUCION_COMP = "DEVO_COMPROMETIDO";
    
    public static final String APROBADO = "APROBADO";
    public static final String COMPROMETIDO = "COMPROMETIDO";
    public static final String DEVENGADO_E = "DEVENGADO_E";
    public static final String DEVENGADO_I = "DEVENGADO_I";
    public static final String DISPONIBLE = "DISPONIBLE";
    public static final String EJECUTAR = "EJECUTAR";
    public static final String EJERCIDO = "EJERCIDO";
    public static final String ESTIMADO = "ESTIMADO";
    public static final String MODIFICADO_AMP_COM_E = "MODIFICADO_AMP_COM_E";
    public static final String MODIFICADO_AMP_COM_I = "MODIFICADO_AMP_COM_I";
    public static final String MODIFICADO_AMP_E = "MODIFICADO_AMP_E";
    public static final String MODIFICADO_AMP_I = "MODIFICADO_AMP_I";
    public static final String MODIFICADO_RED_COM_E = "MODIFICADO_RED_COM_E";
    public static final String MODIFICADO_RED_COM_I = "MODIFICADO_RED_COM_I";
    public static final String MODIFICADO_RED_E = "MODIFICADO_RED_E";
    public static final String MODIFICADO_RED_I = "MODIFICADO_RED_I";
    public static final String PAGADO = "PAGADO";
    public static final String RECAUDADO = "RECAUDADO";

    public static final List<String> MOMENTO_TEMP_E =UtilMisc.toList(APROBADO, MODIFICADO_AMP_COM_E, MODIFICADO_AMP_E, MODIFICADO_RED_COM_E, MODIFICADO_RED_E, PAGADO);
    public static final List<String> MOMENTO_TEMP_I = UtilMisc.toList(ESTIMADO, MODIFICADO_AMP_COM_I, MODIFICADO_AMP_I, MODIFICADO_RED_COM_I, MODIFICADO_RED_I, RECAUDADO);
    
    
    public UtilClavePresupuestal() {}
	
	/**
	 * Obtiene la estructura de la clave presupuestal de una organizacion
	 * @param tipoClave
	 * @param organizationPartyId
	 * @param delegator
	 * @return
	 * @throws GenericEntityException
	 * @throws ServiceException
	 */
	public static GenericValue obtenEstructPresupuestal(String acctgTagUsageTypeId,String organizationPartyId, Delegator delegator, String ciclo) throws GenericEntityException{
		
    	List<GenericValue> estructuraClaveList = delegator.findByAndCache("EstructuraClave",UtilMisc.toMap("organizationPartyId",organizationPartyId,
				"acctgTagUsageTypeId",acctgTagUsageTypeId,"ciclo",ciclo));
    	
    	if(estructuraClaveList.isEmpty()){
    		throw new GenericEntityException("No se encontro la estructura de la clave ingresada");
    	} else{
    		estructuraPresup = estructuraClaveList.get(0);
    		return estructuraPresup;
    	}
    		
    	
	}
	
	/**
	 * Identifica y devuelve el indice en el que se encuentra la clasificacion administrativa 
	 * @param acctgTagUsageTypeId
	 * @param organizationPartyId
	 * @param delegator
	 * @return
	 * @throws ServiceException
	 * @throws GenericEntityException
	 */
	public static int indiceClasAdmin(String acctgTagUsageTypeId,String organizationPartyId, Delegator delegator, String ciclo) throws GenericEntityException{
		
		if(indiceAdmin != 0){
			return indiceAdmin;
		}

		GenericValue estructuraPres = UtilClavePresupuestal.obtenEstructPresupuestal(acctgTagUsageTypeId, organizationPartyId, delegator, ciclo);
			for (int i = 1; i <= UtilClavePresupuestal.TAG_COUNT; i++) {
				if(estructuraPres.getString("clasificacion"+i).equalsIgnoreCase("CL_ADMINISTRATIVA")){
					indiceAdmin = i;
					return i;
				}
					
			}
		
		throw new GenericEntityException("No se encontro la Clasificacion Administrativa"); 	
		
	}
	
	/**
	 * Identifica y devuelve el indice en el que se encuentra el COG 
	 * @param acctgTagUsageTypeId
	 * @param organizationPartyId
	 * @param delegator
	 * @return
	 * @throws ServiceException
	 * @throws GenericEntityException
	 */
	public static int indiceCog(String organizationPartyId, Delegator delegator, String ciclo) throws GenericEntityException{
		
		if(indiceCOG != 0){
			return indiceCOG;
		}

		GenericValue estructuraPres = UtilClavePresupuestal.obtenEstructPresupuestal(UtilClavePresupuestal.EGRESO_TAG, organizationPartyId, delegator, ciclo);
			for (int i = 1; i <= UtilClavePresupuestal.TAG_COUNT; i++) {
				if(estructuraPres.getString("clasificacion"+i).equalsIgnoreCase("CL_COG")){
					indiceCOG = i;
					return i;
				}
					
			}
		
		throw new GenericEntityException("No se encontro el COG"); 	
		
	}
	
	/**
	 * Identifica y devuelve el indice en el que se encuentra el Tipo de Gasto 
	 * @param acctgTagUsageTypeId
	 * @param organizationPartyId
	 * @param delegator
	 * @return
	 * @throws ServiceException
	 * @throws GenericEntityException
	 */
	public static int indiceTipoGasto(String organizationPartyId, Delegator delegator, String ciclo) throws GenericEntityException{
		
		if(indiceTipoGasto != 0){
			return indiceTipoGasto;
		}

		GenericValue estructuraPres = UtilClavePresupuestal.obtenEstructPresupuestal(UtilClavePresupuestal.EGRESO_TAG, organizationPartyId, delegator, ciclo);
			for (int i = 1; i <= UtilClavePresupuestal.TAG_COUNT; i++) {
				if(estructuraPres.getString("clasificacion"+i).equalsIgnoreCase("CL_TIPOGASTO")){
					indiceTipoGasto = i;
					return i;
				}
					
			}
		
		throw new GenericEntityException("No se encontro el COG"); 	
		
	}
	
	/**
	 * Identifica y devuelve el indice en el que se encuentra el CRI 
	 * @param acctgTagUsageTypeId
	 * @param organizationPartyId
	 * @param delegator
	 * @return
	 * @throws ServiceException
	 * @throws GenericEntityException
	 */
	public static int indiceCri(String organizationPartyId, Delegator delegator, String ciclo) throws GenericEntityException{
		
		if(indiceCRI != 0){
			return indiceCRI;
		}

		GenericValue estructuraPres = UtilClavePresupuestal.obtenEstructPresupuestal(UtilClavePresupuestal.INGRESO_TAG, organizationPartyId, delegator, ciclo);
			for (int i = 1; i <= UtilClavePresupuestal.TAG_COUNT; i++) {
				if(estructuraPres.getString("clasificacion"+i).equalsIgnoreCase("CL_CRI")){
					indiceCRI = i;
					return i;
				}
					
			}
		
		throw new GenericEntityException("No se encontro el CRI"); 	
		
	}
	
    /**
     * Busca el enumId a traves de un sequenceId para insertarlo en los tags
     * @param clasificacion
     * @return
     * @throws GenericEntityException 
     * @throws ServiceException 
     */
    public static String obtenEnumId(String sequenceId,String enumTypeId,Delegator delegator) throws GenericEntityException {
    	
    	if(sequenceId != null && !sequenceId.isEmpty() && enumTypeId != null && !enumTypeId.isEmpty())
    	{
    			List<GenericValue> enumerationList = delegator.findByAnd("Enumeration",UtilMisc.toMap("enumTypeId",enumTypeId,
    					"sequenceId",sequenceId));
    			
    			if(enumerationList.isEmpty())
    				throw new GenericEntityException("No se encuentra el Enumeration con tipo "+enumTypeId+" y el sequenceId "+sequenceId); 	
    			else
    				return enumerationList.get(0).getString("enumId");
    	} else {
    		return null;
    	}
    	
	}
    
    /**
     * Busca el sequenceId a traves de un sequenceId para insertarlo en los tags
     * @param clasificacion
     * @return
     * @throws GenericEntityException 
     * @throws ServiceException 
     */
    public static String obtenSequenceId(String enumId,String enumTypeId,Delegator delegator) throws GenericEntityException {
    	
    	if(enumId != null && !enumId.isEmpty() && enumTypeId != null && !enumTypeId.isEmpty())
    	{
    			List<GenericValue> enumerationList = delegator.findByAnd("Enumeration",UtilMisc.toMap("enumTypeId",enumTypeId,
    					"enumId",enumId));
    			
    			if(enumerationList.isEmpty())
    				throw new GenericEntityException("No se encuentra la secuencia con tipo "+enumTypeId+" y el enumId "+enumId); 	
    			else
    				return enumerationList.get(0).getString("sequenceId");
    	} else {
    		return null;
    	}
    	
	}
    
    /**
     * Busca el externalId a traves de un partyId
     * @param partyId
     * @return
     * @throws GenericEntityException 
     * @throws ServiceException 
     */
    public static String obtenExternalId(String partyId,Delegator delegator) throws GenericEntityException {
    	
    	if(partyId != null && !partyId.isEmpty())
    	{
    			GenericValue party = delegator.findByPrimaryKey("Party",UtilMisc.toMap("partyId",partyId));
    			
    			if(party.isEmpty())
    				throw new GenericEntityException("No se encuentra la clasificacion administrativa "+partyId); 	
    			else
    				return party.getString("externalId");
    	} else {
    		return null;
    	}
    	
	}    
    
    /**
     * Obtiene la clave presupuestal de la tabla "ClavePresupuestal" para validar su existencia
     * @param clavePresupuestal
     * @param acctgTagUsageTypeId
     * @param delegator
     * @return
     * @throws GenericEntityException
     */
    public static void existeClavePresu(String clavePresupuestal,String acctgTagUsageTypeId,Delegator delegator) 
    			throws GenericEntityException{
    	
		List<GenericValue> clavePresu = delegator.findByAnd("ClavePresupuestal",UtilMisc.toMap("clavePresupuestal",clavePresupuestal,
				"tipo",acctgTagUsageTypeId));
		
		if(clavePresu == null || clavePresu.isEmpty())
			throw new GenericEntityException("No se encuentra la clave presupuestal "+clavePresupuestal);
		
    }
    
    /**
     * Valida la suficiencia de una lista de GenericValue con clave presupuestal 
     * @param listaItems
     * @param momentoId
     * @param delegator
     * @param monto
     * @throws GenericEntityException
     */
    public static void validaSuficienciaClaves(String clavePresupuestal, BigDecimal monto, String momentoId, Delegator delegator) throws GenericEntityException{
    	
    	Calendar cal =  Calendar.getInstance();
    	String ciclo = String.valueOf(cal.get(Calendar.YEAR));
    	Long mes = Long.valueOf(cal.get(Calendar.MONTH));
    	String month = UtilFormatOut.formatPaddedNumber((mes + 1), 2);
    	try{
    		
			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("clavePresupuestal", EntityOperator.EQUALS, clavePresupuestal),
					EntityCondition.makeCondition("ciclo", EntityOperator.EQUALS, ciclo),
					EntityCondition.makeCondition("mesId", EntityOperator.EQUALS, month),
					EntityCondition.makeCondition("momentoId", EntityOperator.EQUALS, momentoId));
    		
			List<GenericValue> ctrlPresupuestal = delegator.findByCondition("ControlPresupuestal", condiciones, UtilMisc.toList("clavePresupuestal","monto"), null);
			
			if(ctrlPresupuestal != null && !ctrlPresupuestal.isEmpty())
			{	for (GenericValue controlClave : ctrlPresupuestal) 
				{	BigDecimal montoMomento = controlClave.getBigDecimal("monto");
					int diferencia = montoMomento.compareTo(monto);
					if(diferencia < 0)
					{	throw new GenericEntityException("La clave presupuestal "+clavePresupuestal+" no tiene suficiencia");						
					}
					else 
					{	Debug.logInfo("La clave presupuestal "+clavePresupuestal+" tiene suficiencia : "+montoMomento, module);
					}
				}
			}			
			else 
			{	throw new GenericEntityException("La clave presupuestal no tiene suficiencia");
			}
		
    	}catch (GenericEntityException e) {
			throw new GenericEntityException(e);
		}
    	
    }
    
    
    /**
     * Valida la suficiencia de una lista de GenericValue con clave presupuestal 
     * @param listaItems
     * @param momentoId
     * @param delegator
     * @param monto
     * @throws GenericEntityException
     */
    public static void validaClaveProducto(String clavePresupuestal, Delegator delegator, String productId, String requisicionId) throws GenericEntityException{
    	
    	try{
    		
			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("clavePresupuestal", EntityOperator.EQUALS, clavePresupuestal),					
					EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId),
					EntityCondition.makeCondition("requisicionId", EntityOperator.EQUALS, requisicionId));
    		
			List<GenericValue> validaClaveProduct = delegator.findByCondition("DetalleRequisicion", condiciones, null, null);
			
			if(!validaClaveProduct.isEmpty())
			{	//throw new GenericEntityException("La solicitud ya contiene un elemento con la clave presupuestal "+clavePresupuestal+" y el c\u00f3digo de producto "+productId);						
			}
			else 
			{	Debug.logInfo("Se ha validado que la solicitud no contiene la clave presupuestal "+clavePresupuestal+" ni el producto "+productId, module);
			}
			
		
    	}catch (GenericEntityException e) {
			throw new GenericEntityException(e);
		}
    	
    }
    
    public static void validaClaveProductoViatico(String clavePresupuestal, Delegator delegator,  String viaticoId) throws GenericEntityException{
    	
    	try{
    		
			EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("clavePresupuestal", EntityOperator.EQUALS, clavePresupuestal),
					EntityCondition.makeCondition("viaticoId", EntityOperator.EQUALS, viaticoId));
    		
			List<GenericValue> validaClaveProduct = delegator.findByCondition("DetalleViatico", condiciones, null, null);
			
			if(!validaClaveProduct.isEmpty())
			{	throw new GenericEntityException("La solicitud ya contiene un elemento con la clave presupuestal "+clavePresupuestal);						
			}
			else 
			{	Debug.logInfo("Se ha validado que la solicitud no contiene la clave presupuestal "+clavePresupuestal, module);
			}
			
		
    	}catch (GenericEntityException e) {
			throw new GenericEntityException(e);
		}
    	
    }
    
    /**
     * Genera un mapa con el siguiente formato <CvePresupuestal,Monto> se necesita una lista
     * de GenericValues con los campos de clavepresupuestal y monto
     * Suma los montos de las claves que estan duplicadas
     * @param listaItems
     * @return
     */
    public static Map<String, BigDecimal> creaMapaCvePresupuestalMontos(List<GenericValue> listaItems){
    	
    	//Aqui se guardan los montos sumados, si es el caso de las claves a validar
    	Map<String, BigDecimal> mapaClavesMontos = FastMap.newInstance();
    	
    	for (GenericValue itemObjeto : listaItems) {
    		String llaveClave = itemObjeto.getString("clavePresupuestal");
    		BigDecimal montoACompara = itemObjeto.getBigDecimal("monto");
    		if(mapaClavesMontos.containsKey(llaveClave)){
    			BigDecimal montoFinal = montoACompara.add(mapaClavesMontos.get(llaveClave));
    			mapaClavesMontos.put(llaveClave, montoFinal);
    		} else {
    			mapaClavesMontos.put(llaveClave, montoACompara);
    		}
		}
    	
    	return mapaClavesMontos;
    }

    /**
     * Almacena la clave presupuestal en los campos acctgTagEnumId de el item ingresado 
     * y regresa la clave presupuestal concatenada
     * @param itemValue
     * @param delegator
     * @param acctgTagUsageTypeId
     * @param organizationPartyId
     * @throws GenericEntityException
     */
	public static String almacenaClavePresupuestal(Map<?, ?> context,GenericValue itemValue,
			Delegator delegator, String acctgTagUsageTypeId, String organizationPartyId,boolean validaClave, String ciclo) throws GenericEntityException {
		
		indiceAdmin = UtilClavePresupuestal.indiceClasAdmin(acctgTagUsageTypeId, organizationPartyId, delegator, ciclo);
		estructuraPresup = UtilClavePresupuestal.obtenEstructPresupuestal(acctgTagUsageTypeId, organizationPartyId, delegator, ciclo);
		
		StringBuffer clavePresupuestal = new StringBuffer(); 
		
		for (int i = 1; i <= TAG_COUNT; i++) {
			String clasificacion = (String) context.get(VIEW_TAG_PREFIX+i);
			if(clasificacion != null)
				clavePresupuestal.append(clasificacion);
			if(indiceAdmin == i)
				itemValue.set(ENTITY_TAG_PREFIX+"Admin", clasificacion);
			else
				itemValue.set(ENTITY_TAG_PREFIX+i, 
						UtilClavePresupuestal.obtenEnumId(clasificacion, estructuraPresup.getString("clasificacion"+i), delegator));
				
		}
		
		if(validaClave)
			UtilClavePresupuestal.existeClavePresu(clavePresupuestal.toString(), acctgTagUsageTypeId, delegator);
		
		return clavePresupuestal.toString();
		
	}
	
    /**
     * Guarda las clasificaciones en el mapa corespondiente que llama al servicio del motor contable 
     * para registrar la contabilidad y/o presupuesto
     * @param itemValue
     * @param delegator
     * @param acctgTagUsageTypeId
     * @param organizationPartyId
     * @throws GenericEntityException
     */
	public static String almacenaClaveMapa(Map<String, String> input,GenericValue itemValue,
			Delegator delegator, String acctgTagUsageTypeId, String organizationPartyId, String ciclo) throws GenericEntityException {
		
		indiceAdmin = UtilClavePresupuestal.indiceClasAdmin(acctgTagUsageTypeId, organizationPartyId, delegator, ciclo);
		estructuraPresup = UtilClavePresupuestal.obtenEstructPresupuestal(acctgTagUsageTypeId, organizationPartyId, delegator, ciclo);
		
		StringBuffer clavePresupuestal = new StringBuffer(); 
		
		for (int i = 1; i <= TAG_COUNT; i++) {
			if(indiceAdmin == i){
				input.put("clasificacion"+i, obtenExternalId(itemValue.getString(ENTITY_TAG_PREFIX+"Admin"), delegator));
				clavePresupuestal.append(itemValue.getString(ENTITY_TAG_PREFIX+"Admin"));
			}
			else{
				input.put("clasificacion"+i, obtenSequenceId(itemValue.getString(ENTITY_TAG_PREFIX+i), estructuraPresup.getString("clasificacion"+i), delegator));
				clavePresupuestal.append(itemValue.getString(ENTITY_TAG_PREFIX+i));
			}
		}
		
		return clavePresupuestal.toString();
		
	}
	
    /**
     * Guarda los acctgTagEnumId en el item separando el tag de party en acctgTagEnumIdAdmin
     * @param itemValue
     * @param delegator
     * @param acctgTagUsageTypeId
     * @param organizationPartyId
     * @throws GenericEntityException
     */
	public static void almacenaClaveItemGuardar(GenericValue itemValue,
			Delegator delegator, String acctgTagUsageTypeId, String organizationPartyId, String ciclo) throws GenericEntityException {
		
		indiceAdmin = UtilClavePresupuestal.indiceClasAdmin(acctgTagUsageTypeId, organizationPartyId, delegator, ciclo);
		estructuraPresup = UtilClavePresupuestal.obtenEstructPresupuestal(acctgTagUsageTypeId, organizationPartyId, delegator, ciclo);
		
		for(int i=1 ; i <= UtilClavePresupuestal.TAG_COUNT ; i++){
        	if(indiceAdmin==i){
        		itemValue.put(UtilClavePresupuestal.ENTITY_TAG_PREFIX+"Admin", itemValue.getString(UtilClavePresupuestal.ENTITY_TAG_PREFIX+i));
        		itemValue.put(UtilClavePresupuestal.ENTITY_TAG_PREFIX+i,null);
        	} else {
				String enumId = UtilClavePresupuestal.obtenEnumId(itemValue.getString(UtilClavePresupuestal.ENTITY_TAG_PREFIX+i), estructuraPresup.getString("clasificacion"+i), delegator);
				itemValue.put(UtilClavePresupuestal.ENTITY_TAG_PREFIX+i,enumId);
        	}
        }
		
	}
	
    /**
     * Obtiene la clavePresupuestal de un Generic Value
     * @param itemValue
     * @param delegator
     * @param acctgTagUsageTypeId
     * @param organizationPartyId
     * @throws GenericEntityException
     */
	public static String getClavePresupuestal(GenericValue itemValue,
			Delegator delegator, String acctgTagUsageTypeId, String organizationPartyId, String ciclo) throws GenericEntityException {
		
		if(UtilValidate.isEmpty(ciclo)){
			Debug.logWarning("No se ingreso el ciclo para poder obtener la clave presupuestal",module);
			return "";
		}
		
		indiceAdmin = UtilClavePresupuestal.indiceClasAdmin(acctgTagUsageTypeId, organizationPartyId, delegator, ciclo);
		estructuraPresup = UtilClavePresupuestal.obtenEstructPresupuestal(acctgTagUsageTypeId, organizationPartyId, delegator, ciclo);
		String clavePresupuestal="";
		for(int i=1 ; i <= UtilClavePresupuestal.TAG_COUNT ; i++){
			String id = UtilClavePresupuestal.ENTITY_TAG_PREFIX+i;
        	if(indiceAdmin==i){
        		String idAdmin = UtilClavePresupuestal.ENTITY_TAG_PREFIX+"Admin";
        		String partyId = itemValue.getString(idAdmin);
        		if(partyId == null || partyId.isEmpty()){break;}
        		clavePresupuestal = clavePresupuestal+buscaExternalId(partyId, delegator);
        	} else {
        		String enumId = itemValue.getString(id);
        		if(enumId == null || enumId.isEmpty()){break;}
        		clavePresupuestal = clavePresupuestal+buscaSequenceId(enumId,delegator);
        	}
					
        }
		
		return clavePresupuestal;
		
	}	
	
	/**
	 * Metodo que agrega el prefijo "clasificacion" en un mapa tipo <numero,clasificacion"n">
	 * @param mapa
	 * @return
	 */
	public static Map<String,String> cambiaNombreMapa(Map<String,String> mapa){
		Map<String,String> mapaSalida = FastMap.newInstance();
		for (Entry<String,String> mapaEntry : mapa.entrySet()) {
			String numero = mapaEntry.getKey();
			String nuevoNombre = VIEW_TAG_PREFIX.concat(numero);
			mapaSalida.put(nuevoNombre, mapaEntry.getValue());
		}
		return mapaSalida;
	}
	
	/**
	 * Metodo que busca elexternalId a traves de el partyId
	 * @param partyId
	 * @param delegator
	 * @return
	 * @throws GenericEntityException
	 */
    public static String buscaExternalId(String partyId, Delegator delegator) throws GenericEntityException {
			GenericValue party = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId",partyId));
			if(party == null)
				throw new GenericEntityException("No se encontro el party con id "+partyId);
		return party.getString("externalId").trim();
	}

    /**
     *Metodo que obtiene el sequenceId a traces del enumId
     * @param enumId
     * @param delegator
     * @return
     * @throws GenericEntityException
     */
    public static String buscaSequenceId(String enumId, Delegator delegator) throws GenericEntityException {
		GenericValue enumeration = delegator.findByPrimaryKey("Enumeration", UtilMisc.toMap("enumId",enumId));
		if(enumeration == null)
			throw new GenericEntityException("No se encontro el enumeration con id "+enumId);
		return enumeration.getString("sequenceId").trim();
	}
    
    /**
     * Obtiene el indice de cualquier clasificacion
     * @param acctgTagUsageTypeId
     * @param organizationPartyId
     * @param clasificacionId
     * @param delegator
     * @return
     * @throws GenericEntityException
     */
    public static int obtenerIndiceClasificacion(String acctgTagUsageTypeId,String organizationPartyId,String clasificacionId, Delegator delegator, String ciclo) throws GenericEntityException {
    	
    	try {
    		
			GenericValue estructuraPres = UtilClavePresupuestal.obtenEstructPresupuestal(acctgTagUsageTypeId, organizationPartyId, delegator, ciclo);
			for (int i = 1; i <= UtilClavePresupuestal.TAG_COUNT; i++) {
				if(estructuraPres.getString("clasificacion"+i).equalsIgnoreCase(clasificacionId.trim())){
					return i;
				}
					
			}
    	
		} catch (GenericEntityException e) {
			e.printStackTrace();
			throw new GenericEntityException("No se encuentra la clasificacion "+clasificacionId);
		}
	
    	return -1;
    	
    }
    
    /**
     * Obtiene los hijos de enumeration (clasificacion)
     * @param enumId
     * @param delegator
     * @return
     * @throws GenericEntityException
     */
    public static List<String> obtenerHijosEnumerationId(List<String> listEnumId, Delegator delegator) throws GenericEntityException{
    	
    	List<GenericValue> listClasif = delegator.findByCondition("Enumeration", 
    			EntityCondition.makeCondition("parentEnumId",EntityOperator.IN,listEnumId), null, null);
    	
    	List<String> listRegresa = FastList.newInstance();
    	for (GenericValue enumeration : listClasif) {
    		listRegresa.add(enumeration.getString("enumId"));
		}
    	
    	return listRegresa;
	}
    
    
    /**
     * Obtiene los hijos de enumeration (clasificacion) , regresa sequenceiId
     * @param enumId
     * @param delegator
     * @return
     * @throws GenericEntityException
     */
    public static List<String> obtenerHijosSequenceId(List<String> listEnumId, Delegator delegator) throws GenericEntityException{
    	
    	List<GenericValue> listClasif = delegator.findByCondition("Enumeration", 
    			EntityCondition.makeCondition("parentEnumId",EntityOperator.IN,listEnumId), null, null);
    	
    	List<String> listRegresa = FastList.newInstance();
    	for (GenericValue enumeration : listClasif) {
    		listRegresa.add(enumeration.getString("sequenceId"));
		}
    	
    	return listRegresa;
	}
    
    /**
     * Valida la suficiencia y existencia de una clave presupuestal en una fecha elegida
     * @param clavePresupuestal
     * @param monto
     * @param momentoId
     * @param delegator
     * @param fechaPresupuestal
     * @param acctgTagUsageTypeId (INGRESO,EGRESO)
     * @throws GenericEntityException
     */
    public static void validaSuficienciaExistenciaClave(String clavePresupuestal, BigDecimal monto,
    		String momentoId, Delegator delegator,
    		Timestamp fechaPresupuestal, String acctgTagUsageTypeId) throws GenericEntityException{
    	
    	Calendar cal =  Calendar.getInstance();
    	cal.setTimeInMillis(fechaPresupuestal.getTime());
    	String ciclo = String.valueOf(cal.get(Calendar.YEAR));
    	Long mes = Long.valueOf(cal.get(Calendar.MONTH));
    	String month = UtilFormatOut.formatPaddedNumber((mes + 1), 2);
    		
		existeClavePresu(clavePresupuestal, acctgTagUsageTypeId, delegator);
		
		EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("clavePresupuestal", EntityOperator.EQUALS, clavePresupuestal),
				EntityCondition.makeCondition("ciclo", EntityOperator.EQUALS, ciclo),
				EntityCondition.makeCondition("mesId", EntityOperator.EQUALS, month),
				EntityCondition.makeCondition("momentoId", EntityOperator.EQUALS, momentoId));
		
		List<GenericValue> ctrlPresupuestal = delegator.findByCondition("ControlPresupuestal", condiciones, UtilMisc.toList("clavePresupuestal","monto"), null);
		
		if(ctrlPresupuestal != null && !ctrlPresupuestal.isEmpty())
		{	for (GenericValue controlClave : ctrlPresupuestal) 
			{	
				BigDecimal montoMomento = controlClave.getBigDecimal("monto");
				int diferencia = montoMomento.compareTo(monto);
				if(diferencia < 0)
				{	
					throw new GenericEntityException("La clave presupuestal "+clavePresupuestal+" no tiene suficiencia");						
				}
				else 
				{	
					Debug.logInfo("La clave presupuestal "+clavePresupuestal+" tiene suficiencia : "+montoMomento, module);
				}
			}
		}			
		else 
		{	
			throw new GenericEntityException("La clave presupuestal no tiene suficiencia");
		}
		
    }
    
    
    /**
     * Obtiene un listado de las clasificaciones disponilbes Ciclo, Aministrativa, COG, ...
     * @param delegator
     * @param tipo
     * @param ciclo
     * @param organizationPartyId
     * @return
     */
    public static List<Clasificacion> listaClasificaciones(Delegator delegator, String tipo, String ciclo, String organizationPartyId){
    
    	
    	List<Clasificacion> listClasificacion = FastList.newInstance();
    	Map<String, Object> mapaBusqueda = FastMap.newInstance();
    	mapaBusqueda.put("organizationPartyId", organizationPartyId);
    	mapaBusqueda.put("acctgTagUsageTypeId", tipo);
    	mapaBusqueda.put("ciclo", ciclo);
    	try {
    		
			GenericValue Estructura = EntityUtil.getFirst(delegator.findByAndCache("EstructuraClave", mapaBusqueda));
			
			List<GenericValue> listClasifPresup = delegator.findAllCache("ClasifPresupuestal");
			
			if(UtilValidate.isEmpty(listClasifPresup)){
				Debug.logWarning("No se encuentran configuradas las clasificaciones presupuestales",module);
				return listClasificacion;
			}
			
			Map<String,String> mapaClasifDescripcion = FastMap.newInstance();
			for (GenericValue ClasifPresupuestal : listClasifPresup) {
				mapaClasifDescripcion.put(ClasifPresupuestal.getString("clasificacionId"), ClasifPresupuestal.getString("descripcion"));
			}
			
			if(UtilValidate.isNotEmpty(Estructura)){
				String clasificacionId = new String();
				for (int i = 1; i <= UtilClavePresupuestal.TAG_COUNT; i++) {
					clasificacionId = Estructura.getString(UtilClavePresupuestal.VIEW_TAG_PREFIX+i);
					if(UtilValidate.isNotEmpty(clasificacionId)){
						Clasificacion Clasificacion = new Clasificacion();
						Clasificacion.orden = i;
						Clasificacion.clasificacionId = clasificacionId;
						Clasificacion.descripcion = mapaClasifDescripcion.get(clasificacionId);
						listClasificacion.add(Clasificacion);
					}
				}
			} else {
				Debug.logWarning("No se encontr\u00f3 la estructura configurada para el tipo "+tipo+" , ciclo "+ciclo+""
						+ " y la organizaci\u00f3n "+organizationPartyId, module);
			}

		} catch (NullPointerException | GenericEntityException e) {
			e.printStackTrace();
		}
    	
		return listClasificacion;
    }
    

}
