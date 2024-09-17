/*
 * Copyright (c) Open Source Strategies, Inc.
 *
 * Opentaps is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Opentaps is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Opentaps.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opentaps.dataimport.domain;

import org.opentaps.domain.dataimport.AccountingDataImportRepositoryInterface;
import org.opentaps.domain.dataimport.AfectacionEgresoDataImportRepositoryInterface;
import org.opentaps.domain.dataimport.DataImportDomainInterface;
import org.opentaps.domain.dataimport.FixedAssetDataImportRepositoryInterface;
import org.opentaps.domain.dataimport.GlAccountImportServiceInterface;
import org.opentaps.domain.dataimport.HistorialBienesDataImportRepositoryInterface;
import org.opentaps.domain.dataimport.LevantaFixedAssetDataImportRepositoryInterface;
import org.opentaps.domain.dataimport.OperacionPatrimonialDataImportRepositoryInterface;
import org.opentaps.domain.dataimport.OrdenesCobroDataImportRepositoryInterface;
import org.opentaps.domain.dataimport.OrdenesPagoDataImportRepositoryInterface;
import org.opentaps.domain.dataimport.PagosDataImportRepositoryInterface;
import org.opentaps.domain.dataimport.PartyDataImportRepositoryInterface;
import org.opentaps.domain.dataimport.ProductDataImportRepositoryInterface;
import org.opentaps.domain.dataimport.TagDataImportRepositoryInterface;
import org.opentaps.domain.dataimport.UserDataImportRepositoryInterface;
import org.opentaps.domain.dataimport.ValidacionPresupuestalDataImportRepositoryInterface;
import org.opentaps.foundation.domain.Domain;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.service.ServiceException;

/**
 * This is an implementation of the Data Import domain.
 */
public class DataImportDomain extends Domain implements DataImportDomainInterface{

    /** {@inheritDoc} */
    public AccountingDataImportRepositoryInterface getAccountingDataImportRepository() throws RepositoryException {
        return instantiateRepository(AccountingDataImportRepository.class);
    }

    /** {@inheritDoc} */
    public GlAccountImportServiceInterface getGlAccountImportService() throws ServiceException {
        return this.instantiateService(GlAccountImportService.class);
    }

    /** {@inheritDoc} */
    public ProductDataImportRepositoryInterface getProductDataImportRepository() throws RepositoryException {
        return instantiateRepository(ProductDataImportRepository.class);
    }

    /** {@inheritDoc} */
    public PartyDataImportRepositoryInterface getPartyDataImportRepository() throws RepositoryException {
        return instantiateRepository(PartyDataImportRepository.class);
    }
    
    /** {@inheritDoc} */
   	public TagDataImportRepositoryInterface getTagDataImportRepository()
   			throws RepositoryException { 
   		
   		return instantiateRepository(TagDataImportRepository.class);
   	}
   	
   	/** {@inheritDoc} */
   	public PagosDataImportRepositoryInterface getPagosDataImportRepository()
   			throws RepositoryException { 
   		
   		return instantiateRepository(PagosDataImportRepository.class);
   	}
   	
	@Override
	public OrdenesPagoDataImportRepositoryInterface getOrdenesPagoDataImportRepository() throws RepositoryException {
		return instantiateRepository(OrdenesPagoImportRepository.class);
	}

	@Override
	public OrdenesCobroDataImportRepositoryInterface getOrdenesCobroDataImportRepository()
			throws RepositoryException {
		return instantiateRepository(OrdenesCobroImportRepository.class);
	}
	
   	/** {@inheritDoc} */
   	public UserDataImportRepositoryInterface getUserDataImportRepository()
   			throws RepositoryException { 
   		
   		return instantiateRepository(UserDataImportRepository.class);
   	}
   	
   	/** {@inheritDoc} */
   	public FixedAssetDataImportRepositoryInterface getFixedAssetDataImportRepository()
   			throws RepositoryException { 
   		
   		return instantiateRepository(FixedAssetDataImportRepository.class);
   	}
   	
   	/** {@inheritDoc} */
   	public LevantaFixedAssetDataImportRepositoryInterface getLevantaFixedAssetDataImportRepository()
   			throws RepositoryException { 
   		
   		return instantiateRepository(LevantaFixedAssetDataImportRepository.class);
   	}

	@Override
	public AfectacionEgresoDataImportRepositoryInterface getAfectacionEgresoDataImportRepository()
			throws RepositoryException {
		return instantiateRepository(AfectacionEgresoDataImportRepository.class);
	}

	@Override
	public OperacionPatrimonialDataImportRepositoryInterface getOperacionPatrimonialDataImportRepository()
			throws RepositoryException {
		return instantiateRepository(OperacionPatrimonialDataImportRepository.class);
	}
	
	@Override
	public ValidacionPresupuestalDataImportRepositoryInterface getValidacionPresupuestalDataImportRepository()
			throws RepositoryException {
		return instantiateRepository(ValidacionPresupuestalDataImportRepository.class);
	}
	
   	/** {@inheritDoc} */
   	public HistorialBienesDataImportRepositoryInterface getHistorialBienesDataImportRepository()
   			throws RepositoryException { 
   		
   		return instantiateRepository(HistorialBienesDataImportRepository.class);
   	}
}