package org.opentaps.dataimport.domain;

import java.util.List;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.opentaps.base.constants.StatusItemConstants;
import org.opentaps.base.entities.DataImportGlAccount;
import org.opentaps.base.entities.DataImportPayment;
import org.opentaps.domain.DomainRepository;
import org.opentaps.domain.dataimport.AccountingDataImportRepositoryInterface;
import org.opentaps.domain.dataimport.PagosDataImportRepositoryInterface;
import org.opentaps.foundation.infrastructure.Infrastructure;
import org.opentaps.foundation.infrastructure.User;
import org.opentaps.foundation.repository.RepositoryException;

public class PagosDataImportRepository extends DomainRepository implements PagosDataImportRepositoryInterface{

	
	 /**
     * Default constructor.
     */
    public PagosDataImportRepository() {
        super();
    }

     /**
     * If you want the full infrastructure including the dispatcher, then you must have the User.
     * @param infrastructure the domain infrastructure
     * @param user the domain user
     * @throws RepositoryException if an error occurs
     */
    public PagosDataImportRepository(Infrastructure infrastructure, User user) throws RepositoryException {
        super(infrastructure, user);
    }

    /** {@inheritDoc}. */
    public List<DataImportPayment> findNotProcessesDataImportPagos(String userLoginId) throws RepositoryException {
    	List<String> orderBy = UtilMisc.toList("referenciaPago");	
    	EntityCondition statusCondPrimera = EntityCondition.makeCondition(EntityOperator.OR,
        EntityCondition.makeCondition("importStatusId", EntityOperator.EQUALS, StatusItemConstants.Dataimport.DATAIMP_NOT_PROC),
        EntityCondition.makeCondition("importStatusId", EntityOperator.EQUALS, StatusItemConstants.Dataimport.DATAIMP_FAILED),
        EntityCondition.makeCondition("importStatusId", EntityOperator.EQUALS, null));
    	
    	EntityCondition statusCond = EntityCondition.makeCondition(EntityOperator.AND,
        		statusCondPrimera,
                EntityCondition.makeCondition("userLoginId", EntityOperator.EQUALS, userLoginId));
        return this.findList(DataImportPayment.class, statusCond, orderBy);
    }
    
    /** {@inheritDoc}. */
    public List<DataImportPayment> buscarPorReferencia(String numReferencia) throws RepositoryException {
    	List<String> orderBy = UtilMisc.toList("referenciaPago");	
    	EntityCondition statusCond = EntityCondition.makeCondition(EntityOperator.AND,
        EntityCondition.makeCondition("referenciaPago", EntityOperator.EQUALS, numReferencia));    	
        return this.findList(DataImportPayment.class, statusCond, orderBy);
    }
    
    /** {@inheritDoc}. */
    public List<DataImportPayment> buscarSinError() throws RepositoryException {
    	List<String> orderBy = UtilMisc.toList("referenciaPago");	
    	EntityCondition statusCond = EntityCondition.makeCondition(EntityOperator.OR,
    	        EntityCondition.makeCondition("importStatusId", EntityOperator.NOT_EQUAL, StatusItemConstants.Dataimport.DATAIMP_NOT_PROC),
    	        EntityCondition.makeCondition("importStatusId", EntityOperator.NOT_EQUAL, StatusItemConstants.Dataimport.DATAIMP_FAILED));    	
        return this.findList(DataImportPayment.class, statusCond, orderBy);
    }

	

}
