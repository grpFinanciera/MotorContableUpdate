package org.opentaps.domain.dataimport;

import java.util.List;

import org.opentaps.base.entities.DataImportOrdenesPago;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.repository.RepositoryInterface;

public interface OrdenesPagoDataImportRepositoryInterface extends RepositoryInterface{

    public List<DataImportOrdenesPago> findNotProcessesDataImportOrdenesPagoEntries(String userLoginId) throws RepositoryException;
}
