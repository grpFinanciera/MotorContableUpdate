package org.opentaps.domain.dataimport;

import java.util.List;

import org.opentaps.base.entities.DataImportUser;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.repository.RepositoryInterface;

public interface UserDataImportRepositoryInterface  extends RepositoryInterface{
    
    /**
     * Finds not imported users.
     * @throws org.opentaps.foundation.repository.RepositoryException
     */
    public List<DataImportUser> findNotProcessesDataImportUserEntries(String userLoginId) throws RepositoryException;

}
