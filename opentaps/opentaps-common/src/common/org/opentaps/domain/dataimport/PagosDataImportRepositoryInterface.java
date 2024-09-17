package org.opentaps.domain.dataimport;

import java.util.List;

import org.opentaps.base.entities.DataImportPayment;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.repository.RepositoryInterface;

public interface PagosDataImportRepositoryInterface extends RepositoryInterface{
	  /**
     * Finds not imported General Ledger accounts.
     * @throws org.opentaps.foundation.repository.RepositoryException
     */
    public List<DataImportPayment> findNotProcessesDataImportPagos(String userLoginId) throws RepositoryException;

	public List<DataImportPayment> buscarPorReferencia(String numReferencia) throws RepositoryException;

	public List<DataImportPayment> buscarSinError() throws RepositoryException;

}
