package org.opentaps.dataimport.domain;

import java.util.List;

import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.opentaps.base.constants.StatusItemConstants;
import org.opentaps.base.entities.DataImportValidPresup;
import org.opentaps.domain.DomainRepository;
import org.opentaps.domain.dataimport.ValidacionPresupuestalDataImportRepositoryInterface;
import org.opentaps.foundation.repository.RepositoryException;

/** {@inheritDoc}. */
public class ValidacionPresupuestalDataImportRepository extends DomainRepository
		implements ValidacionPresupuestalDataImportRepositoryInterface {

	/**
	 * Default constructor.
	 */
	public ValidacionPresupuestalDataImportRepository() {
		super();
	}

	/** {@inheritDoc}. */
	public List<DataImportValidPresup> findNotProcessesDataImportValidacionPresupuestalEntries(String userLoginId)
			throws RepositoryException {
		EntityCondition statusCondPrimera = EntityCondition.makeCondition(EntityOperator.OR, 
				EntityCondition.makeCondition("importStatusId", EntityOperator.EQUALS,StatusItemConstants.Dataimport.DATAIMP_NOT_PROC),
				EntityCondition.makeCondition("importStatusId", EntityOperator.EQUALS,StatusItemConstants.Dataimport.DATAIMP_FAILED),
				EntityCondition.makeCondition("importStatusId", EntityOperator.EQUALS, null));	
		
		EntityCondition statusCond = EntityCondition.makeCondition(EntityOperator.AND,
        		statusCondPrimera,
                EntityCondition.makeCondition("userLoginId", EntityOperator.EQUALS, userLoginId));
		return this.findList(DataImportValidPresup.class, statusCond);
	}
}
