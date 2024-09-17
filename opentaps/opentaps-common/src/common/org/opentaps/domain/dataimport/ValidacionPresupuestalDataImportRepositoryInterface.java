package org.opentaps.domain.dataimport;

import java.util.List;

import org.opentaps.base.entities.DataImportValidPresup;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.repository.RepositoryInterface;

public interface ValidacionPresupuestalDataImportRepositoryInterface  extends RepositoryInterface{
    
    public List<DataImportValidPresup> findNotProcessesDataImportValidacionPresupuestalEntries(String userLoginId) throws RepositoryException;
}
