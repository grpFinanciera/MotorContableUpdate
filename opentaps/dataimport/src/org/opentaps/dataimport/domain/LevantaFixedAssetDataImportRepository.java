package org.opentaps.dataimport.domain;

import java.util.List;

import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.opentaps.base.constants.StatusItemConstants;
import org.opentaps.base.entities.DataImportLevantaActFijo;
import org.opentaps.domain.DomainRepository;
import org.opentaps.domain.dataimport.LevantaFixedAssetDataImportRepositoryInterface;
import org.opentaps.foundation.infrastructure.Infrastructure;
import org.opentaps.foundation.infrastructure.User;
import org.opentaps.foundation.repository.RepositoryException;

public class LevantaFixedAssetDataImportRepository extends DomainRepository implements LevantaFixedAssetDataImportRepositoryInterface{

	/**
     * Default constructor.
     */
    public LevantaFixedAssetDataImportRepository() {
        super();
    }

     /**
     * If you want the full infrastructure including the dispatcher, then you must have the User.
     * @param infrastructure the domain infrastructure
     * @param user the domain user
     * @throws RepositoryException if an error occurs
     */
    public LevantaFixedAssetDataImportRepository(Infrastructure infrastructure, User user) throws RepositoryException {
        super(infrastructure, user);
    }
	
	@Override
	public List<DataImportLevantaActFijo> findNotProcessesDataImportLevantaFixedAssetEntries(String userLoginId)
			throws RepositoryException {
		EntityCondition statusCond = EntityCondition.makeCondition(EntityOperator.OR,
				EntityCondition.makeCondition("importStatusId", EntityOperator.EQUALS, StatusItemConstants.Dataimport.DATAIMP_NOT_PROC),
				EntityCondition.makeCondition("importStatusId", EntityOperator.EQUALS, StatusItemConstants.Dataimport.DATAIMP_FAILED),
				EntityCondition.makeCondition("importStatusId", EntityOperator.EQUALS, null));
				
				EntityCondition userCond = EntityCondition.makeCondition(EntityOperator.OR,
				EntityCondition.makeCondition("userLoginId", EntityOperator.EQUALS, userLoginId));
				            	
				EntityCondition combinedCond = EntityCondition.makeCondition(EntityOperator.AND, statusCond, userCond);
				return this.findList(DataImportLevantaActFijo.class, combinedCond);
	}
	
	

}
