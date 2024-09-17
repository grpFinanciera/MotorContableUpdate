package org.opentaps.domain.dataimport;

import java.util.List;

import org.opentaps.base.entities.DataImportAfectacionEgreso;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.repository.RepositoryInterface;

public interface AfectacionEgresoDataImportRepositoryInterface  extends RepositoryInterface{
    
    /**
     * Finds not imported afectacion egreso.
     * @throws org.opentaps.foundation.repository.RepositoryException
     */
    public List<DataImportAfectacionEgreso> findNotProcessesDataImportAfectacionEgresoEntries(String userLoginId) throws RepositoryException;

}
