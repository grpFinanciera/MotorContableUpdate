package org.opentaps.domain.dataimport;

import java.util.List;

import org.opentaps.base.entities.DataImportOrdenesCobro;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.repository.RepositoryInterface;

public interface OrdenesCobroDataImportRepositoryInterface extends RepositoryInterface{

    public List<DataImportOrdenesCobro> findNotProcessesDataImportOrdenesCobroEntries(String userLoginId) throws RepositoryException;
}
