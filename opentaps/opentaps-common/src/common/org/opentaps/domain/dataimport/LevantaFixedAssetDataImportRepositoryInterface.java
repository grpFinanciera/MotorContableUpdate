package org.opentaps.domain.dataimport;

import java.util.List;

import org.opentaps.base.entities.DataImportLevantaActFijo;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.repository.RepositoryInterface;

public interface LevantaFixedAssetDataImportRepositoryInterface extends RepositoryInterface{
	
	/**
    * Finds not imported fixed assets.
    * @throws org.opentaps.foundation.repository.RepositoryException
    */
   public List<DataImportLevantaActFijo> findNotProcessesDataImportLevantaFixedAssetEntries(String userLoginId) throws RepositoryException;

}
