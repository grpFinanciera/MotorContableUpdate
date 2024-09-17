package org.opentaps.domain.dataimport;

import java.util.List;

import org.opentaps.base.entities.DataImportDevIng;
import org.opentaps.base.entities.DataImportIngreso;
import org.opentaps.base.entities.DataImportRecIng;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.repository.RepositoryInterface;

public interface IngresoDataImportRepositoryInterface  extends RepositoryInterface{
    
    public List<DataImportIngreso> findNotProcessesDataImportIngresoEntries(String userLoginId) throws RepositoryException;
    
    public List<DataImportDevIng> findNotProcessesDataImportDevengoIngresoEntries(String userLoginId) throws RepositoryException;
    
    public List<DataImportRecIng> findNotProcessesDataImportRecaudadoIngresoEntries(String userLoginId) throws RepositoryException;

}
