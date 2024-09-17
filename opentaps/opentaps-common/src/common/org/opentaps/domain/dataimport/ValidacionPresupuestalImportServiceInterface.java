package org.opentaps.domain.dataimport;

import org.opentaps.foundation.service.ServiceException;
import org.opentaps.foundation.service.ServiceInterface;

public interface ValidacionPresupuestalImportServiceInterface extends
		ServiceInterface {
	
	public void setTipoClave(String tipoClave);
	
	public void setMomento(String momento);
	
	public void setFechaContable(String fechaContable);
	
	public int getImportedRecords();

	public void importValidacionPresupuestal() throws ServiceException;

}
