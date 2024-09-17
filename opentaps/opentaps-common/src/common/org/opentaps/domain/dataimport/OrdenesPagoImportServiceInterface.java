package org.opentaps.domain.dataimport;

import org.hibernate.HibernateException;
import org.ofbiz.service.GenericServiceException;
import org.opentaps.foundation.infrastructure.InfrastructureException;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.service.ServiceInterface;

public interface OrdenesPagoImportServiceInterface extends
ServiceInterface {
	
	/**
	 * Gets imported records count by service {@link #importPresupuestoInicial}.
	 * 
	 * @return imported records count
	 */
	public int getImportedRecords();

	/**
	 * Import products using <code>DataImportPresupuestoInicia</code>. Note
	 * that this service is not wrapped in a transaction. Each product record
	 * imported is in its own transaction, so it can store as many good records
	 * as possible.
	 * 
	 * @throws GenericServiceException 
	 * @throws RepositoryException 
	 * @throws HibernateException 
	 * @throws InfrastructureException 
	 */
	public void importOrdenesPago() throws GenericServiceException, RepositoryException, InfrastructureException, HibernateException;


}
