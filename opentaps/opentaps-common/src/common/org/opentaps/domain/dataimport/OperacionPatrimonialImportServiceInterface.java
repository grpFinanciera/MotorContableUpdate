package org.opentaps.domain.dataimport;

import org.opentaps.foundation.service.ServiceException;
import org.opentaps.foundation.service.ServiceInterface;

public interface OperacionPatrimonialImportServiceInterface extends
		ServiceInterface {
	public void setOrganizationPartyId(String organizationPartyId);
	
	public void setEventoContable(String eventoContable);
	
	public void setDescripcion(String descripcion);
	
	public void setFechaContable(String fechaContable);
	
	public int getImportedRecords();

	public void importOperacionPatrimonial() throws ServiceException;

}
