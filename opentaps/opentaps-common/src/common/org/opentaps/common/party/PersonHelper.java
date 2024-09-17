package org.opentaps.common.party;

import org.ofbiz.base.crypto.HashCrypt;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.party.party.PartyHelper;
import org.opentaps.base.entities.Person;

public class PersonHelper extends Person {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static String guardaFirmaPerson(GenericValue Person) throws GenericEntityException{
    	
    	String firmaId = new String();
    	
    	if(!usarFirmaSimple(Person)){
    		return new String();
    	}

	   	String concatenaFirma = Person.getString("partyId")+PartyHelper.getPartyName(Person)+Person.getString("gender");
	   	firmaId = HashCrypt.removeHashTypePrefix(HashCrypt.getDigestHash(concatenaFirma));
    	Person.set("firmaId", firmaId);
    	Person.store();
	
    	return firmaId;
    }
	
	/**
	 * Valida que exista la firma simple, si existe la regresa, si no existe la guarda y la regresa 
	 * @param Person
	 * @return
	 * @throws GenericEntityException
	 */
	public static String validaGuardaFirma(GenericValue Person) throws GenericEntityException{
		
    	if(!usarFirmaSimple(Person)){
    		return new String();
    	} else {
    		if(UtilValidate.isNotEmpty(Person.getString("firmaId"))){
    			return Person.getString("firmaId");
    		}
    		return guardaFirmaPerson(Person);
    	}
		
	}
	
	/**
	 * Valida si tiene que registrarse la firma simple y si existe el objeto ingresado Person
	 * @param Person
	 * @return
	 * @throws GenericEntityException
	 */
	public static boolean usarFirmaSimple(GenericValue Person) throws GenericEntityException{
		
    	if(UtilValidate.isEmpty(Person) || !Person.getEntityName().equals("Person")){
    		throw new GenericEntityException("Debe ingresar la persona para crear la firma simple");
    	}
		
		return "true".equals(UtilProperties.getPropertyValue(
				"security.properties", "password.encrypt"));
	}
}
