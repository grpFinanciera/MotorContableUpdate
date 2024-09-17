package org.opentaps.domain.dataimport;

import org.opentaps.foundation.service.ServiceException;
import org.opentaps.foundation.service.ServiceInterface;

public interface LevantaFixedAssetImportServiceInterface extends ServiceInterface{
	
	/**
     * Gets imported records count by service {@link #importFixedAsset}.
     * @return imported records count
     */
    public int getImportedRecords();
    
    /**
     * Import fixed assets using <code>DataImportFixedAsset</code>.
     * Note that this service is not wrapped in a transaction.
     * Each fixed asset record imported is in its own transaction, so it can store as many good records as possible.
     *
     * @throws ServiceException if an error occurs
     */
    public void importLevantaFixedAsset() throws ServiceException;

}
