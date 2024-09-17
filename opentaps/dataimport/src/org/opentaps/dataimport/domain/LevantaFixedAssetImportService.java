package org.opentaps.dataimport.domain;

import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.opentaps.base.constants.StatusItemConstants;
import org.opentaps.base.entities.DataImportLevantaActFijo;
import org.opentaps.base.entities.LevantamientoActivo;
import org.opentaps.domain.DomainService;
import org.opentaps.domain.dataimport.LevantaFixedAssetDataImportRepositoryInterface;
import org.opentaps.domain.dataimport.LevantaFixedAssetImportServiceInterface;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.entity.hibernate.Session;
import org.opentaps.foundation.entity.hibernate.Transaction;
import org.opentaps.foundation.infrastructure.Infrastructure;
import org.opentaps.foundation.infrastructure.InfrastructureException;
import org.opentaps.foundation.infrastructure.User;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.service.ServiceException;

public class LevantaFixedAssetImportService extends DomainService implements LevantaFixedAssetImportServiceInterface{

private static final String MODULE = FixedAssetImportService.class.getName();
		
	// session object, using to store/search pojos.
	private Session session;
	public String organizationPartyId;
	public int importedRecords;
	static Logger logger = Logger.getLogger(LevantaFixedAssetImportService.class);

	public LevantaFixedAssetImportService() {
		super();
	}

	public LevantaFixedAssetImportService(Infrastructure infrastructure, User user,
			Locale locale) throws ServiceException {
		super(infrastructure, user, locale);
	}

	public void setOrganizationPartyId(String organizationPartyId) {
		this.organizationPartyId = organizationPartyId;
	}

	public String getOrganizationPartyId() {
		return organizationPartyId;
	}
	
	@Override
	public int getImportedRecords() {
		return importedRecords;
	}

	@Override
	public void importLevantaFixedAsset() throws ServiceException {
		
		try {
			Debug.logInfo("Entra a importFixedAsset()", MODULE);
			this.session = this.getInfrastructure().getSession();
			LevantaFixedAssetDataImportRepositoryInterface imp_repo = this.getDomainsDirectory().getDataImportDomain().getLevantaFixedAssetDataImportRepository();
			String userLoginId = imp_repo.getUser().getUserId();
			LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory().getLedgerDomain().getLedgerRepository();
			List<DataImportLevantaActFijo> dataforimp = imp_repo.findNotProcessesDataImportLevantaFixedAssetEntries(userLoginId);
			//LocalDispatcher dispatcher = this.getDomainsDirectory().getLedgerDomain().getInfrastructure().getDispatcher();
			//Delegator delegator = dispatcher.getDelegator();
			int imported = 0;
			Transaction imp_tx1 = null;

			for (DataImportLevantaActFijo rowdata : dataforimp){
				try{
					imp_tx1 = null;
					// begin importing row data item
					if (rowdata.getFixedAssetId()!=null){
						LevantamientoActivo levantaActivo = new LevantamientoActivo();
						String idActivoFijo = "";
						Debug.log("IdDataimport: " + rowdata.getFixedAssetId());
						
						
						levantaActivo.setFixedAssetId(rowdata.getFixedAssetId());
						levantaActivo.setUbicacionRapidaId(rowdata.getUbicacion());
						
						imp_tx1 = this.session.beginTransaction();
						ledger_repo.createOrUpdate(levantaActivo);
						imp_tx1.commit();
						
						String message = "Successfully imported Fixed Asset ["
								+ idActivoFijo
								+ "].";

						this.storeImportLevantaFixedAssetSuccess(rowdata, imp_repo);
						Debug.logInfo(message, MODULE);
						imported = imported + 1;
					}
					else
					{
						String message = "Error al importar el activo fijo. "
								+ "Es necesario ingresar los campos obligatorios.";
						Debug.log(message);
						storeImportLevantaFixedAssetError(rowdata, message, imp_repo);
						continue;
					}

				} catch (Exception ex) {
					Debug.log("Catch");
					String message = ex.getMessage();									
					storeImportLevantaFixedAssetError(rowdata, message, imp_repo);
					if (imp_tx1 != null) {
						imp_tx1.rollback();
					}									
					Debug.logError(ex, message, MODULE);
					throw new ServiceException(ex.getMessage());
				}
			}

			this.importedRecords = imported;

		} catch (InfrastructureException ex) {
			Debug.logError(ex, MODULE);
			throw new ServiceException(ex.getMessage());
		} catch (RepositoryException ex) {
			Debug.logError(ex, MODULE);
			throw new ServiceException(ex.getMessage());
		} finally {
			if (session != null) {
				session.close();
			}
		}
		
	}
	
	/**
	 * Helper method to store fixed asset import success into
	 * <code>DataImportFixedAsset</code> entity row.
	 * 
	 * @param rawdata
	 *            item of <code>DataImportFixedAsset</code> entity that was
	 *            successfully imported
	 * @param imp_repo
	 *            repository of accounting
	 * @throws org.opentaps.foundation.repository.RepositoryException
	 */
	private void storeImportLevantaFixedAssetSuccess(DataImportLevantaActFijo rowdata,
			LevantaFixedAssetDataImportRepositoryInterface imp_repo)
			throws RepositoryException {
		// mark as success
		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_IMPORTED);
		rowdata.setImportError(null);
		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
		imp_repo.createOrUpdate(rowdata);
	}
	
	/**
	 * Helper method to store fixed asset import error into
	 * <code>DataImportFixedAsset</code> entity row.
	 * 
	 * @param rawdata
	 *            item of <code>DataImportFixedAsset</code> entity that was
	 *            unsuccessfully imported
	 * @param message
	 *            error message
	 * @param imp_repo
	 *            repository of accounting
	 * @throws org.opentaps.foundation.repository.RepositoryException
	 */
	private void storeImportLevantaFixedAssetError(DataImportLevantaActFijo rowdata, String message,
			LevantaFixedAssetDataImportRepositoryInterface imp_repo)
			throws RepositoryException {
		Debug.log("storeImportFixedAssetError");
		// store the exception and mark as failed
		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_FAILED);
		rowdata.setImportError(message);
		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
		imp_repo.createOrUpdate(rowdata);
	}

}
