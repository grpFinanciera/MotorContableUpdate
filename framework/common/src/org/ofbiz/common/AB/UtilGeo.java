package org.ofbiz.common.AB;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;

public final class UtilGeo {

	/**
	 * Obtiene la jerarquia de un pais determinado
	 * Ej. : Pais -> Estado -> Municipio -> Ciudad
	 * @param delegator
	 * @param tipoGeo
	 * @return
	 * @throws GenericEntityException
	 */
	public final static LinkedList<GenericValue> obtenJerarquiaPaisGeo(Delegator delegator,String tipoGeo) throws GenericEntityException{
		
		LinkedList<GenericValue> jerarquiaList = new LinkedList<GenericValue>();
		GenericValue tipoGeoGeneric = delegator.findByPrimaryKey("GeoType",UtilMisc.toMap("geoTypeId",tipoGeo));
		jerarquiaList.add(tipoGeoGeneric);
		GenericValue tipoGeoGen = null;
		do{
			tipoGeoGen = obtenHijoTipoGeo(delegator, tipoGeo);
			if(tipoGeoGen != null && !tipoGeoGen.isEmpty()){
				tipoGeo = tipoGeoGen.getString("geoTypeId");
				jerarquiaList.add(tipoGeoGen);
			}
		}while(tipoGeoGen != null);
		
		
		return jerarquiaList;
	}
	
	/**
	 * Obtiene el padre del tipo geografico 
	 * @param delegator
	 * @param tipoGeo
	 * @return
	 * @throws GenericEntityException
	 */
	public final static GenericValue obtenHijoTipoGeo(Delegator delegator, String tipoGeo) throws GenericEntityException{
		List<GenericValue> tipoGeoGeneric = delegator.findByAnd("GeoType",UtilMisc.toMap("parentTypeId",tipoGeo));
		if(tipoGeoGeneric != null && !tipoGeoGeneric.isEmpty()){
			return tipoGeoGeneric.get(0);
		}
		return null;
	}
	
	public final static Map<String,String> equivalenciasPostalAddress = 
				UtilMisc.toMap("COUNTRY","countryGeoId","STATE","stateProvinceGeoId","MUNICIPALITY","municipalityGeoId","CITY","city");
	 
	
}
