package org.opentaps.dataimport.domain;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.hibernate.HibernateException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.base.constants.StatusItemConstants;
import org.opentaps.base.entities.DataImportOrdenesCobro;
import org.opentaps.domain.DomainService;
import org.opentaps.domain.dataimport.OrdenesCobroDataImportRepositoryInterface;
import org.opentaps.domain.dataimport.OrdenesCobroImportServiceInterface;
import org.opentaps.foundation.entity.hibernate.Session;
import org.opentaps.foundation.entity.hibernate.Transaction;
import org.opentaps.foundation.infrastructure.Infrastructure;
import org.opentaps.foundation.infrastructure.InfrastructureException;
import org.opentaps.foundation.infrastructure.User;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.service.ServiceException;

public class OrdenesCobroImportService extends DomainService implements
OrdenesCobroImportServiceInterface{
	
	private static final String MODULE = OrdenesCobroImportService.class.getName();
	private int importedRecords;

	public OrdenesCobroImportService() {
		super();
	}
	
	public OrdenesCobroImportService(Infrastructure infrastructure,
			User user, Locale locale) throws ServiceException {
		super(infrastructure, user, locale);
	}
	
	@Override
	public int getImportedRecords() {
		return importedRecords;
	}

	@Override
	public void importOrdenesCobro() throws GenericServiceException, RepositoryException, InfrastructureException, HibernateException {

		Session sesion = this.getDomainsDirectory().getLedgerDomain().
		getInfrastructure().getSession();
		
		try {
		
			OrdenesCobroDataImportRepositoryInterface imp_repo = this
					.getDomainsDirectory().getDataImportDomain()
					.getOrdenesCobroDataImportRepository();
			
			String userLoginId = imp_repo.getUser().getUserId();
			
			List<DataImportOrdenesCobro> dataforimp = imp_repo.findNotProcessesDataImportOrdenesCobroEntries(userLoginId);
	
			LocalDispatcher dispatcher = this.getDomainsDirectory().getLedgerDomain()
					.getInfrastructure().getDispatcher(); 
			
			GenericValue loginUser = this.getDomainsDirectory().getLedgerDomain().
					getInfrastructure().getSystemUserLogin();
			
			Transaction tx = null;
			tx = sesion.beginTransaction();
			
			String invoiceIdAnt = new String();
			Map<String,Object> result = null;
			Map<String,Object> resultItem = FastMap.newInstance();
			
			List<DataImportOrdenesCobro> ordenesCorridas = FastList.newInstance();
			
			for (Iterator<DataImportOrdenesCobro> itOrdenesCobra = dataforimp.iterator(); itOrdenesCobra.hasNext();) {
				DataImportOrdenesCobro ordenesCobra = itOrdenesCobra.next();
				
				String ordenId = ordenesCobra.getInvoiceId();
				
				//Realiza una orden de cobro cada que encuentra una nueva
				if(!ordenId.equalsIgnoreCase(invoiceIdAnt)){
					
					if(ServiceUtil.isError(result)){
						tx.rollback();
						tx = sesion.beginTransaction();
						for (DataImportOrdenesCobro dataImportOrdenesCobro : ordenesCorridas) {
							this.storeImportOrdenesCobroError(dataImportOrdenesCobro, ServiceUtil.getErrorMessage(result), imp_repo);
						}
						ordenesCorridas = FastList.newInstance();
						tx.commit();
						tx = sesion.beginTransaction();
					} else if(result != null){ //Cuando la orden se completo correctamente se marca como lista
						
						Map<String,Object> resultado = FastMap.newInstance();
						Map<String,Object> input = FastMap.newInstance();
						input.put("invoiceId", invoiceIdAnt);
						input.put("userLogin", loginUser);
						resultado = dispatcher.runSync("setInvoiceReadyAndCheckIfPaid", input);
						
						if(ServiceUtil.isError(resultado)){
							tx.rollback();
							tx = sesion.beginTransaction();
							for (DataImportOrdenesCobro dataImportOrdenesCobro : ordenesCorridas) {
								this.storeImportOrdenesCobroError(dataImportOrdenesCobro, ServiceUtil.getErrorMessage(result), imp_repo);
							}
							ordenesCorridas = FastList.newInstance();
							tx.commit();
							tx = sesion.beginTransaction();
						} else {
							this.storeImportOrdenesCobroSuccess(ordenesCobra, imp_repo);
							tx.commit();
							tx = sesion.beginTransaction();
							result = FastMap.newInstance();
						}
						
					}
					
					Map<String,Object> input = FastMap.newInstance();
					input.put("agrupador", "/");
					input.put("userLogin", loginUser);
					input.put("invoiceId",ordenesCobra.getInvoiceId());
					input.put("partyId", ordenesCobra.getPartyId());
					input.put("partyIdFrom", ordenesCobra.getPartyIdFrom());
					input.put("idTipoDoc", ordenesCobra.getIdTipoDoc());
					input.put("invoiceTypeId","SALES_INVOICE");
					input.put("invoiceDate", ordenesCobra.getInvoiceDate());
					input.put("dueDate", ordenesCobra.getDueDate());
					input.put("invoiceMessage", ordenesCobra.getInvoiceMessage());
					input.put("referenceNumber", ordenesCobra.getReferenceNumber());
					input.put("descriptionInvoice", ordenesCobra.getDescriptionInvoice());
					input.put("currencyUomId", ordenesCobra.getUomId());					
					input.put("statusId", "INVOICE_IN_PROCESS");
					
					result = dispatcher.runSync("createInvoice", input);
					invoiceIdAnt = ordenesCobra.getInvoiceId();
				}
				
				if(!ServiceUtil.isError(result) && !ServiceUtil.isError(resultItem)){
					
					Map<String,Object> inputItem = FastMap.newInstance();
					inputItem.put("userLogin", loginUser);
					inputItem.put("invoiceId", ordenId);
					inputItem.put("invoiceItemSeqId", ordenesCobra.getInvoiceItemSeqId());
					inputItem.put("productId",ordenesCobra.getProductId());
					inputItem.put("uomId", ordenesCobra.getUomId());
					inputItem.put("quantity", ordenesCobra.getQuantity());
					inputItem.put("amount", ordenesCobra.getAmount());
					inputItem.put("taxAuthPartyId", ordenesCobra.getTaxAuthPartyId());
					inputItem.put("taxAuthGeoId", ordenesCobra.getTaxAuthGeoId());
					inputItem.put("clasificacion1", ordenesCobra.getClasificacion1());
					inputItem.put("clasificacion2", ordenesCobra.getClasificacion2());
					inputItem.put("clasificacion3", ordenesCobra.getClasificacion3());
					inputItem.put("clasificacion4", ordenesCobra.getClasificacion4());
					inputItem.put("clasificacion5", ordenesCobra.getClasificacion5());
					inputItem.put("clasificacion6", ordenesCobra.getClasificacion6());
					inputItem.put("clasificacion7", ordenesCobra.getClasificacion7());
					inputItem.put("clasificacion8", ordenesCobra.getClasificacion8());
					inputItem.put("clasificacion9", ordenesCobra.getClasificacion9());
					inputItem.put("clasificacion10", ordenesCobra.getClasificacion10());
					inputItem.put("clasificacion11", ordenesCobra.getClasificacion11());
					inputItem.put("clasificacion12", ordenesCobra.getClasificacion12());
					inputItem.put("clasificacion13", ordenesCobra.getClasificacion13());
					inputItem.put("clasificacion14", ordenesCobra.getClasificacion14());
					inputItem.put("clasificacion15", ordenesCobra.getClasificacion15());
					
					resultItem = dispatcher.runSync("createInvoiceItem", inputItem);
					
					if(ServiceUtil.isError(resultItem)){
						
						tx.rollback();
						tx = sesion.beginTransaction();
						this.storeImportOrdenesCobroError(ordenesCobra, ServiceUtil.getErrorMessage(resultItem), imp_repo);
						tx.commit();
						tx = sesion.beginTransaction();
						
					} else if(!itOrdenesCobra.hasNext()){
						
						Map<String,Object> resultado = FastMap.newInstance();
						Map<String,Object> input = FastMap.newInstance();
						input.put("invoiceId", ordenId);
						input.put("userLogin", loginUser);
						resultado = dispatcher.runSync("setInvoiceReadyAndCheckIfPaid", input);
						
						if(ServiceUtil.isError(resultado)){
							tx.rollback();
							tx = sesion.beginTransaction();
							for (DataImportOrdenesCobro dataImportOrdenesCobro : ordenesCorridas) {
								this.storeImportOrdenesCobroError(dataImportOrdenesCobro, ServiceUtil.getErrorMessage(resultado), imp_repo);
							}
							ordenesCorridas = FastList.newInstance();
							this.storeImportOrdenesCobroError(ordenesCobra, ServiceUtil.getErrorMessage(resultado), imp_repo);
							tx.commit();
						} else {
							this.storeImportOrdenesCobroSuccess(ordenesCobra, imp_repo);
							tx.commit();
						}
						
					} else {
						resultItem = FastMap.newInstance();
						this.storeImportOrdenesCobroSuccess(ordenesCobra, imp_repo);
						ordenesCorridas.add(ordenesCobra);
					}
					
				} else if(ServiceUtil.isError(result) && !ServiceUtil.isError(resultItem)){
					tx.rollback();
					tx = sesion.beginTransaction();
					this.storeImportOrdenesCobroError(ordenesCobra, ServiceUtil.getErrorMessage(result), imp_repo);
					tx.commit();
					tx = sesion.beginTransaction();
				}

			}
			
		} catch (GenericServiceException e) {
			throw new GenericServiceException(e.getMessage());
		} finally{
			sesion.close();
		}
	}
	
	/**
	 * Helper method to store Ordenes de Cobro import into
	 * <code>DataImportOrdenesCobro</code> entity row.
	 * 
	 * @param rowdata
	 *            item of <code>DataImportOrdenesCobro</code> entity that
	 *            was unsuccessfully imported
	 * @param message
	 *            error message
	 * @param imp_repo
	 *            repository of accounting
	 * @throws org.opentaps.foundation.repository.RepositoryException
	 */
	private void storeImportOrdenesCobroSuccess(
			DataImportOrdenesCobro rowdata,
			OrdenesCobroDataImportRepositoryInterface imp_repo)
			throws RepositoryException {
		// mark as success
		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_IMPORTED);
		rowdata.setImportError(null);
		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
		imp_repo.createOrUpdate(rowdata);
	}
	
	/**
	 * Helper method to store Orden de Cobro import error into
	 * <code>DataImportOrdenesCobro</code> entity row.
	 * 
	 * @param rowdata
	 *            item of <code>DataImportOrdenesCobro</code> entity that
	 *            was unsuccessfully imported
	 * @param message
	 *            error message
	 * @param imp_repo
	 *            repository of accounting
	 * @throws org.opentaps.foundation.repository.RepositoryException
	 */
	private void storeImportOrdenesCobroError(
			DataImportOrdenesCobro rowdata, String message,
			OrdenesCobroDataImportRepositoryInterface imp_repo)
			throws RepositoryException {
		// store the exception and mark as failed
		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_FAILED);
		rowdata.setImportError(message);
		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
		imp_repo.createOrUpdate(rowdata);
	}

}

