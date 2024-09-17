package org.opentaps.domain.dataimport;

import org.opentaps.foundation.service.ServiceException;
import org.opentaps.foundation.service.ServiceInterface;

public interface UserImportServiceInterface extends
		ServiceInterface {
	/**
	 * Gets imported records count by service {@link #importPresupuestoInicial}.
	 * 
	 * @return imported records count
	 */
	public int getImportedRecords();

	/**
	 * Import users using <code>DataImportUser</code>. Note
	 * that this service is not wrapped in a transaction. Each product record
	 * imported is in its own transaction, so it can store as many good records
	 * as possible.
	 * 
	 * @throws ServiceException
	 *             if an error occurs
	 */
	public void importUser() throws ServiceException;

}
