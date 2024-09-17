package org.opentaps.domain.dataimport;

import org.opentaps.foundation.service.ServiceException;
import org.opentaps.foundation.service.ServiceInterface;

public interface IngresoImportServiceInterface extends
		ServiceInterface {
	public void setOrganizationPartyId(String organizationPartyId);
	
	public void setEventoDevengado(String eventoDevengado);
	
	public void setEventoRecaudado(String eventoRecaudado);
	
	public void setFechaContable(String fechaContable);
	
	public void setDescripcion(String descripcion);
	
	public int getImportedRecords();

	public void importIngreso() throws ServiceException;

}
