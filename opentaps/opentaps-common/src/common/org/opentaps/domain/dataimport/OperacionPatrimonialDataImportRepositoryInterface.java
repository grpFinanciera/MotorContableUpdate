package org.opentaps.domain.dataimport;

import java.util.List;

import org.opentaps.base.entities.DataImportOperPatr;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.repository.RepositoryInterface;

public interface OperacionPatrimonialDataImportRepositoryInterface  extends RepositoryInterface{
    
    public List<DataImportOperPatr> findNotProcessesDataImportOperacionPatrimonialEntries(String userLoginId) throws RepositoryException;

}
