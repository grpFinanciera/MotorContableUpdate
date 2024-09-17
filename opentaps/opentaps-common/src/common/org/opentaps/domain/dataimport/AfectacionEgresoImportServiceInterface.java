package org.opentaps.domain.dataimport;

import org.opentaps.foundation.service.ServiceException;
import org.opentaps.foundation.service.ServiceInterface;

public interface AfectacionEgresoImportServiceInterface extends
		ServiceInterface {
	
	public void setOrganizationPartyId(String organizationPartyId);
	public void setTipoDocumentoId(String tipoDocumentoId);
//	public void setFechaContable(java.sql.Timestamp fechaContable);
	public void setFechaContable(String fechaContable);
	public void setIndicador(String indicador);
	public void setBancoId(String bancoId);
	public void setCuentaId(String cuentaId);
	public void setDescription(String description);
	/**
	 * Gets imported records count by service {@link #importPresupuestoInicial}.
	 * 
	 * @return imported records count
	 */
	public int getImportedRecords();

	/**
	 * Import products using <code>DataImportPresupuestoInicia</code>. Note
	 * that this service is not wrapped in a transaction. Each product record
	 * imported is in its own transaction, so it can store as many good records
	 * as possible.
	 * 
	 * @throws ServiceException
	 *             if an error occurs
	 */
	public void importAfectacionEgreso() throws ServiceException;

}
