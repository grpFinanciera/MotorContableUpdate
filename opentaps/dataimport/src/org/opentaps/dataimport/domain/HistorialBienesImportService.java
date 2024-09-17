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

import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.opentaps.base.constants.StatusItemConstants;
import org.opentaps.base.entities.DataImportHistorialBienes;
import org.opentaps.base.entities.HistorialBienesActivoFijo;
import org.opentaps.domain.DomainService;
import org.opentaps.domain.dataimport.HistorialBienesImportServiceInterface;
import org.opentaps.domain.dataimport.HistorialBienesDataImportRepositoryInterface;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.entity.hibernate.Session;
import org.opentaps.foundation.infrastructure.Infrastructure;
import org.opentaps.foundation.infrastructure.InfrastructureException;
import org.opentaps.foundation.infrastructure.User;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.service.ServiceException;

/**
 * Import fixed assets via intermediate DataImportHistorialBienes entity.
 */
public class HistorialBienesImportService extends DomainService implements HistorialBienesImportServiceInterface {

	private static final String MODULE = HistorialBienesImportService.class.getName();	 
	
	// session object, using to store/search pojos.
	private Session session;
	public String organizationPartyId;
	public int importedRecords;
	static Logger logger = Logger.getLogger(HistorialBienesImportService.class);

	public HistorialBienesImportService() {
		super();
	}

	public HistorialBienesImportService(Infrastructure infrastructure, User user,
			Locale locale) throws ServiceException {
		super(infrastructure, user, locale);
	}

	/** {@inheritDoc} */
	public int getImportedRecords() {
		return importedRecords;
	}

	public void setOrganizationPartyId(String organizationPartyId) {
		this.organizationPartyId = organizationPartyId;
	}

	public String getOrganizationPartyId() {
		return organizationPartyId;
	}

	/** {@inheritDoc} */
	public void importHistorialBienes() throws ServiceException {
		try {

			Debug.logInfo("Entra a importHistorialBienes()", MODULE);
			this.session = this.getInfrastructure().getSession();
						
			HistorialBienesDataImportRepositoryInterface imp_repo = this
					.getDomainsDirectory().getDataImportDomain().getHistorialBienesDataImportRepository();					

			String userLoginId = imp_repo.getUser().getUserId();

			LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
					.getLedgerDomain().getLedgerRepository();

			List<DataImportHistorialBienes> dataforimp = imp_repo.findNotProcessesDataImportHistorialBienesEntries(userLoginId);						

			int imported = 0;
			for (DataImportHistorialBienes rowdata : dataforimp)
			{
				try
				{	if (rowdata.getFixedAssetId() != null && !rowdata.getFixedAssetId().equals(""))
					{	HistorialBienesActivoFijo historialBienes = new HistorialBienesActivoFijo();						
						historialBienes.setFixedAssetId(rowdata.getFixedAssetId());
						historialBienes.setIdActivoAnterior(rowdata.getIdActivoAnterior());
						historialBienes.setFechaAdquisicion(rowdata.getFechaAdquisicion());
						historialBienes.setValorAdquisicion(rowdata.getValorAdquisicion());
						historialBienes.setAniosVidaUtil(rowdata.getAniosVidaUtil());
						historialBienes.setFechaFinalVida(rowdata.getFechaFinalVida());
						historialBienes.setDepreciacionAcumulada(rowdata.getDepreciacionAcumulada());																
						ledger_repo.createOrUpdate(historialBienes);
						String message = "Se ha imprtado correctamente el Fixed Asset ["+ rowdata.getFixedAssetId() + "].";
						this.storeImportHistorialBienesSuccess(rowdata, imp_repo);
						Debug.logInfo(message, MODULE);
						imported = imported + 1;
					}
					else
					{	String message = "Error al importar el historico del activo fijo. Es necesario ingresar los campos obligatorios.";
						Debug.log(message);
						storeImportHistorialBienesError(rowdata, message, imp_repo);
						continue;
					}

				} catch (Exception ex) {
					String message = "Error al importar el historial de bienes: " + ex.getMessage();									
					storeImportHistorialBienesError(rowdata, message, imp_repo);														
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
	 * <code>DataImportHistorialBienes</code> entity row.
	 * 
	 * @param rawdata
	 *            item of <code>DataImportHistorialBienes</code> entity that was
	 *            successfully imported
	 * @param imp_repo
	 *            repository of accounting
	 * @throws org.opentaps.foundation.repository.RepositoryException
	 */
	private void storeImportHistorialBienesSuccess(DataImportHistorialBienes rowdata,
			HistorialBienesDataImportRepositoryInterface imp_repo)
			throws RepositoryException {
		// mark as success
		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_IMPORTED);
		rowdata.setImportError(null);
		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
		imp_repo.createOrUpdate(rowdata);
	}

	/**
	 * Helper method to store fixed asset import error into
	 * <code>DataImportHistorialBienes</code> entity row.
	 * 
	 * @param rawdata
	 *            item of <code>DataImportHistorialBienes</code> entity that was
	 *            unsuccessfully imported
	 * @param message
	 *            error message
	 * @param imp_repo
	 *            repository of accounting
	 * @throws org.opentaps.foundation.repository.RepositoryException
	 */
	private void storeImportHistorialBienesError(DataImportHistorialBienes rowdata, String message,
			HistorialBienesDataImportRepositoryInterface imp_repo)
			throws RepositoryException {
		Debug.log("storeImportHistorialBienesError");
		// store the exception and mark as failed
		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_FAILED);
		rowdata.setImportError(message);
		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
		imp_repo.createOrUpdate(rowdata);
	}		

}
