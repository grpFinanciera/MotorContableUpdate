package org.opentaps.dataimport.domain;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.hibernate.HibernateException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.base.constants.StatusItemConstants;
import org.opentaps.base.entities.DataImportOrdenesCobro;
import org.opentaps.base.entities.DataImportOrdenesPago;
import org.opentaps.domain.DomainService;
import org.opentaps.domain.dataimport.OrdenesCobroDataImportRepositoryInterface;
import org.opentaps.domain.dataimport.OrdenesPagoDataImportRepositoryInterface;
import org.opentaps.domain.dataimport.OrdenesPagoImportServiceInterface;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.entity.hibernate.Session;
import org.opentaps.foundation.entity.hibernate.Transaction;
import org.opentaps.foundation.infrastructure.Infrastructure;
import org.opentaps.foundation.infrastructure.InfrastructureException;
import org.opentaps.foundation.infrastructure.User;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.service.ServiceException;

public class OrdenesPagoImportService extends DomainService implements
OrdenesPagoImportServiceInterface{
	
	private static final String MODULE = OrdenesPagoImportService.class.getName();
	private int importedRecords;

	public OrdenesPagoImportService() {
		super();
	}
	
	public OrdenesPagoImportService(Infrastructure infrastructure,
			User user, Locale locale) throws ServiceException {
		super(infrastructure, user, locale);
	}
	
	@Override
	public int getImportedRecords() {
		return importedRecords;
	}

	@Override
	public void importOrdenesPago() throws GenericServiceException, RepositoryException, InfrastructureException, HibernateException {

		Session sesion = this.getDomainsDirectory().getLedgerDomain().
		getInfrastructure().getSession();
		
		try {
		
			OrdenesPagoDataImportRepositoryInterface imp_repo = this
					.getDomainsDirectory().getDataImportDomain()
					.getOrdenesPagoDataImportRepository();
			
			String userLoginId = imp_repo.getUser().getUserId();
			
			List<DataImportOrdenesPago> dataforimp = imp_repo.findNotProcessesDataImportOrdenesPagoEntries(userLoginId);
	
			LocalDispatcher dispatcher = this.getDomainsDirectory().getLedgerDomain()
					.getInfrastructure().getDispatcher(); 
			
			GenericValue loginUser = this.getDomainsDirectory().getLedgerDomain().
					getInfrastructure().getSystemUserLogin();
			
			Transaction tx = null;
			tx = sesion.beginTransaction();
			
			String invoiceIdAnt = new String();
			Map<String,Object> result = null;
			Map<String,Object> resultItem = FastMap.newInstance();
			
			List<DataImportOrdenesPago> ordenesCorridas = FastList.newInstance();
			
			for (Iterator<DataImportOrdenesPago> itOrdenesPago = dataforimp.iterator(); itOrdenesPago.hasNext();) {
				DataImportOrdenesPago ordenesPago = itOrdenesPago.next();
				
				String ordenId = ordenesPago.getInvoiceId();
				
				//Realiza una orden de pago cada que encuentra una nueva
				if(!ordenId.equalsIgnoreCase(invoiceIdAnt)){
					
					if(ServiceUtil.isError(result)){
						tx.rollback();
						tx = sesion.beginTransaction();
						for (DataImportOrdenesPago dataImportOrdenesPago : ordenesCorridas) {
							this.storeImportOrdenesPagoError(dataImportOrdenesPago, ServiceUtil.getErrorMessage(result), imp_repo);
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
							for (DataImportOrdenesPago dataImportOrdenesPago : ordenesCorridas) {
								this.storeImportOrdenesPagoError(dataImportOrdenesPago, ServiceUtil.getErrorMessage(result), imp_repo);
							}
							ordenesCorridas = FastList.newInstance();
							tx.commit();
							tx = sesion.beginTransaction();
						} else {
							this.storeImportOrdenesPagoSuccess(ordenesPago, imp_repo);
							tx.commit();
							tx = sesion.beginTransaction();
							result = FastMap.newInstance();
						}
						
					}
					
					Map<String,Object> input = FastMap.newInstance();
					input.put("agrupador", "/");
					input.put("userLogin", loginUser);
					input.put("invoiceId",ordenesPago.getInvoiceId());
					input.put("partyId", ordenesPago.getPartyId());
					input.put("partyIdFrom", ordenesPago.getPartyIdFrom());
					input.put("idTipoDoc", ordenesPago.getIdTipoDoc());
					input.put("invoiceTypeId","PURCHASE_INVOICE");
					input.put("invoiceDate", ordenesPago.getInvoiceDate());
					input.put("dueDate", ordenesPago.getDueDate());
					input.put("invoiceMessage", ordenesPago.getInvoiceMessage());
					input.put("referenceNumber", ordenesPago.getReferenceNumber());
					input.put("descriptionInvoice", ordenesPago.getDescriptionInvoice());
					input.put("currencyUomId", ordenesPago.getUomId());	
					input.put("statusId", "INVOICE_IN_PROCESS");
					
					result = dispatcher.runSync("createInvoice", input);
					invoiceIdAnt = ordenesPago.getInvoiceId();
				
				}
				
				if(!ServiceUtil.isError(result) && !ServiceUtil.isError(resultItem)){
					
					Map<String,Object> inputItem = FastMap.newInstance();
					inputItem.put("userLogin", loginUser);
					inputItem.put("invoiceId", ordenId);
					inputItem.put("invoiceItemSeqId", ordenesPago.getInvoiceItemSeqId());
					inputItem.put("productId",ordenesPago.getProductId());
					inputItem.put("uomId", ordenesPago.getUomId());
					inputItem.put("quantity", ordenesPago.getQuantity());
					inputItem.put("amount", ordenesPago.getAmount());
					inputItem.put("taxAuthPartyId", ordenesPago.getTaxAuthPartyId());
					inputItem.put("taxAuthGeoId", ordenesPago.getTaxAuthGeoId());
					inputItem.put("clasificacion1", ordenesPago.getClasificacion1());
					inputItem.put("clasificacion2", ordenesPago.getClasificacion2());
					inputItem.put("clasificacion3", ordenesPago.getClasificacion3());
					inputItem.put("clasificacion4", ordenesPago.getClasificacion4());
					inputItem.put("clasificacion5", ordenesPago.getClasificacion5());
					inputItem.put("clasificacion6", ordenesPago.getClasificacion6());
					inputItem.put("clasificacion7", ordenesPago.getClasificacion7());
					inputItem.put("clasificacion8", ordenesPago.getClasificacion8());
					inputItem.put("clasificacion9", ordenesPago.getClasificacion9());
					inputItem.put("clasificacion10", ordenesPago.getClasificacion10());
					inputItem.put("clasificacion11", ordenesPago.getClasificacion11());
					inputItem.put("clasificacion12", ordenesPago.getClasificacion12());
					inputItem.put("clasificacion13", ordenesPago.getClasificacion13());
					inputItem.put("clasificacion14", ordenesPago.getClasificacion14());
					inputItem.put("clasificacion15", ordenesPago.getClasificacion15());
					
					result = dispatcher.runSync("createInvoiceItem", inputItem);
					
					if(ServiceUtil.isError(resultItem)){
						
						tx.rollback();
						tx = sesion.beginTransaction();
						this.storeImportOrdenesPagoError(ordenesPago, ServiceUtil.getErrorMessage(resultItem), imp_repo);
						tx.commit();
						tx = sesion.beginTransaction();
						
					} else if(!itOrdenesPago.hasNext()){
						
						Map<String,Object> resultado = FastMap.newInstance();
						Map<String,Object> input = FastMap.newInstance();
						input.put("invoiceId", ordenId);
						input.put("userLogin", loginUser);
						resultado = dispatcher.runSync("setInvoiceReadyAndCheckIfPaid", input);
						
						if(ServiceUtil.isError(resultado)){
							tx.rollback();
							tx = sesion.beginTransaction();
							for (DataImportOrdenesPago dataImportOrdenesPago : ordenesCorridas) {
								this.storeImportOrdenesPagoError(dataImportOrdenesPago, ServiceUtil.getErrorMessage(resultado), imp_repo);
							}
							ordenesCorridas = FastList.newInstance();
							this.storeImportOrdenesPagoError(ordenesPago, ServiceUtil.getErrorMessage(resultado), imp_repo);
							tx.commit();
						} else {
							this.storeImportOrdenesPagoSuccess(ordenesPago, imp_repo);
							tx.commit();
						}
						
					} else {
						resultItem = FastMap.newInstance();
						this.storeImportOrdenesPagoSuccess(ordenesPago, imp_repo);
						ordenesCorridas.add(ordenesPago);
					}
					
				} else if(ServiceUtil.isError(result) && !ServiceUtil.isError(resultItem)){
					tx.rollback();
					tx = sesion.beginTransaction();
					this.storeImportOrdenesPagoError(ordenesPago, ServiceUtil.getErrorMessage(result), imp_repo);
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
	 * Helper method to store Ordenes de Pago import into
	 * <code>DataImportOrdenesPago</code> entity row.
	 * 
	 * @param rowdata
	 *            item of <code>DataImportOrdenesPago</code> entity that
	 *            was unsuccessfully imported
	 * @param message
	 *            error message
	 * @param imp_repo
	 *            repository of accounting
	 * @throws org.opentaps.foundation.repository.RepositoryException
	 */
	private void storeImportOrdenesPagoSuccess(
			DataImportOrdenesPago rowdata,
			OrdenesPagoDataImportRepositoryInterface imp_repo)
			throws RepositoryException {
		// mark as success
		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_IMPORTED);
		rowdata.setImportError(null);
		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
		imp_repo.createOrUpdate(rowdata);
	}
	
	/**
	 * Helper method to store Orden de Pago import error into
	 * <code>DataImportOrdenesPago</code> entity row.
	 * 
	 * @param rowdata
	 *            item of <code>DataImportOrdenesPago</code> entity that
	 *            was unsuccessfully imported
	 * @param message
	 *            error message
	 * @param imp_repo
	 *            repository of accounting
	 * @throws org.opentaps.foundation.repository.RepositoryException
	 */
	private void storeImportOrdenesPagoError(
			DataImportOrdenesPago rowdata, String message,
			OrdenesPagoDataImportRepositoryInterface imp_repo)
			throws RepositoryException {
		// store the exception and mark as failed
		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_FAILED);
		rowdata.setImportError(message);
		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
		imp_repo.createOrUpdate(rowdata);
	}
	

}

