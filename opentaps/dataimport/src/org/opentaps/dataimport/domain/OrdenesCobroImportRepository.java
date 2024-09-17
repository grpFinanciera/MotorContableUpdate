package org.opentaps.dataimport.domain;

import java.util.List;

import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.opentaps.base.constants.StatusItemConstants;
import org.opentaps.base.entities.DataImportOrdenesCobro;
import org.opentaps.domain.DomainRepository;
import org.opentaps.domain.dataimport.OrdenesCobroDataImportRepositoryInterface;
import org.opentaps.foundation.infrastructure.Infrastructure;
import org.opentaps.foundation.infrastructure.User;
import org.opentaps.foundation.repository.RepositoryException;

public class OrdenesCobroImportRepository extends DomainRepository
implements OrdenesCobroDataImportRepositoryInterface{

	public OrdenesCobroImportRepository() {
		super();
	}
	
	public OrdenesCobroImportRepository(
			Infrastructure infrastructure, User user)
			throws RepositoryException {
		super(infrastructure, user);
	}

	/** {@inheritDoc}. */
	public List<DataImportOrdenesCobro> findNotProcessesDataImportOrdenesCobroEntries(String userLoginId)
			throws RepositoryException {
		EntityCondition statusCond = EntityCondition.makeCondition(
				EntityOperator.OR, EntityCondition.makeCondition(
						"importStatusId", EntityOperator.EQUALS,
						StatusItemConstants.Dataimport.DATAIMP_NOT_PROC),
				EntityCondition.makeCondition("importStatusId",
						EntityOperator.EQUALS,
						StatusItemConstants.Dataimport.DATAIMP_FAILED),
				EntityCondition.makeCondition("importStatusId",
						EntityOperator.EQUALS, null));
		
		EntityCondition userCond = EntityCondition.makeCondition(EntityOperator.OR,
		EntityCondition.makeCondition("userLoginId", EntityOperator.EQUALS, userLoginId));
						            	
		EntityCondition combinedCond = EntityCondition.makeCondition(EntityOperator.AND, statusCond, userCond);
		return this.findList(DataImportOrdenesCobro.class, combinedCond);
	}
	
}
