package org.opentaps.dataimport.domain;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.LineaMotorContable;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.base.constants.StatusItemConstants;
import org.opentaps.base.entities.DataImportOperPatr;
import org.opentaps.domain.DomainService;
import org.opentaps.domain.dataimport.OperacionPatrimonialDataImportRepositoryInterface;
import org.opentaps.domain.dataimport.OperacionPatrimonialImportServiceInterface;
import org.opentaps.foundation.entity.hibernate.Session;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.service.ServiceException;

import com.ibm.icu.text.SimpleDateFormat;

public class OperacionPatrimonialImportService extends DomainService implements
		OperacionPatrimonialImportServiceInterface {
	@SuppressWarnings("unused")
	private static final String MODULE = OperacionPatrimonialImportService.class
			.getName();
	// session object, using to store/search pojos.
	@SuppressWarnings("unused")
	private Session session;
	private String organizationPartyId;
	private String descripcion;
	private String eventoContable;
	private String fechaContable;
	public int importedRecords;

	public OperacionPatrimonialImportService() throws ServiceException {
		super();
	}

	/** {@inheritDoc} */
	public void setOrganizationPartyId(String organizationPartyId) {
		this.organizationPartyId = organizationPartyId;
	}

	/** {@inheritDoc} */
	public void setFechaContable(String fechaContable) {
		this.fechaContable = fechaContable;
	}

	/** {@inheritDoc} */
	public void setEventoContable(String eventoContable) {
		this.eventoContable = eventoContable;
	}

	/** {@inheritDoc} */
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	/** {@inheritDoc} */
	public int getImportedRecords() {
		return importedRecords;
	}

	/** {@inheritDoc} */
	public void importOperacionPatrimonial() throws ServiceException {
		try {
			this.session = this.getInfrastructure().getSession();

			OperacionPatrimonialDataImportRepositoryInterface imp_repo = this
					.getDomainsDirectory().getDataImportDomain()
					.getOperacionPatrimonialDataImportRepository();

			LocalDispatcher dispatcher = this.getDomainsDirectory()
					.getLedgerDomain().getInfrastructure().getDispatcher();

			String userLoginId = this.getDomainsDirectory().getLedgerDomain()
					.getLedgerRepository().getUser().getOfbizUserLogin()
					.getString("userLoginId");

			Debug.log("Omar-userLoginId: " + userLoginId);

			List<DataImportOperPatr> dataImport = imp_repo
					.findNotProcessesDataImportOperacionPatrimonialEntries(userLoginId);

			Delegator delegator = this.getDomainsDirectory().getLedgerDomain()
					.getInfrastructure().getDelegator();

			boolean isOk = true;

			// Lista de las lineasMotor.
			List<LineaMotorContable> lineas = new FastList<LineaMotorContable>();

			Timestamp fechaC = generaFecha();
			if (fechaC == null) {
				isOk = false;
				storeImportOperacionPatrimonialError(dataImport.get(0),
						"El formato de fecha no es el correcto", imp_repo);
			}

			if (isOk) {
				Debug.log("OP-Inica for-each");
				for (DataImportOperPatr rowdata : dataImport) {
					// Cada renglon es una lineaMotor con su respectivo mapa.
					LineaMotorContable linea = new LineaMotorContable();

					List<GenericValue> lineasContables = delegator
							.findByAnd("LineaContable", "acctgTransTypeId",
									eventoContable);
					// Obtenemos la lineaContable.
					GenericValue lc = getLineaContablePorDescripcion(
							lineasContables, rowdata.getDescripcionLinea());
					
					if (lc == null) {
						storeImportOperacionPatrimonialError(rowdata,
								"La descripci\u00f3n no existe", imp_repo);
						isOk = false;
						continue;
					}
					
					lc.set("monto", rowdata.getMonto());

					if (lc.getString("catalogoAbono") != null) {
						if(rowdata.getAuxiliarAbono()==null||rowdata.getAuxiliarAbono().isEmpty()){
							storeImportOperacionPatrimonialError(rowdata,
									"Es necesario ingresar el auxiliar Abono", imp_repo);
							isOk= false;
							continue;
						}
						lc.set("catalogoAbono", rowdata.getAuxiliarAbono());
					}

					if (lc.getString("catalogoCargo") != null) {
						if(rowdata.getAuxiliarCargo()==null||rowdata.getAuxiliarCargo().isEmpty()){
							storeImportOperacionPatrimonialError(rowdata,
									"Es necesario ingresar el auxiliar Cargo", imp_repo);
							isOk= false;
							continue;
						}
						lc.set("catalogoCargo", rowdata.getAuxiliarCargo());
					}

					// Creamos el mapaContable.
					Map<String, GenericValue> mapaContable = new HashMap<String, GenericValue>();
					mapaContable.put(lc.getString("descripcion"), lc);
					linea.setLineasContables(mapaContable);
					lineas.add(linea);
				}
			}

			if (isOk) {
				// Hacemos la poliza.
				Debug.log("OP-transacciones");
				Debug.log("Total de lineas motor.- " + lineas.size());

				Map<String, Object> input = FastMap.newInstance();
				input.put("fechaRegistro", UtilDateTime.nowTimestamp());
				input.put("fechaContable", fechaC);
				input.put("eventoContableId", eventoContable);
				input.put("usuario", userLoginId);
				input.put("organizationId", organizationPartyId);
				input.put("currency", "MXN");
				input.put("descripcion", descripcion);
				input.put("lineasMotor", lineas);

				Map<String, Object> result = dispatcher.runSync(
						"creaTransaccionMotor", input);

				if (ServiceUtil.isError(result)) {
					Debug.log("Hubo Error en poliza");

					for (DataImportOperPatr rowdata : dataImport) {
						storeImportOperacionPatrimonialError(rowdata,
								ServiceUtil.getErrorMessage(result), imp_repo);
					}
				} else {
					for (DataImportOperPatr rowdata : dataImport) {
						storeImportOperacionPatrimonialSuccess(rowdata,
								imp_repo);
					}
				}
			}

		} catch (Exception ex) {
			Debug.log("JRRM.- " + ex.getMessage());

		}
	}

	private GenericValue getLineaContablePorDescripcion(
			List<GenericValue> lineasContables, String descripcion) {
		for (GenericValue linea : lineasContables) {
			if (linea.getString("descripcion").equalsIgnoreCase(descripcion)) {
				return linea;
			}
		}
		return null;
	}

	/**
	 * Helper method to store import success into
	 * <code>DataImportCompromisoDevengoNomina</code> entity row.
	 * 
	 * @param rowdata
	 *            item of <code>DataImportCompromisoDevengoNomina</code> entity
	 *            that was successfully imported
	 * @param imp_repo
	 *            repository of accounting
	 * @throws org.opentaps.foundation.repository.RepositoryException
	 */
	private void storeImportOperacionPatrimonialSuccess(
			DataImportOperPatr rowdata,
			OperacionPatrimonialDataImportRepositoryInterface imp_repo)
			throws RepositoryException {
		// mark as success
		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_IMPORTED);
		rowdata.setImportError(null);
		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
		imp_repo.createOrUpdate(rowdata);
	}

	/**
	 * Helper method to store import error into
	 * <code>DataImportCompromisoDevengoNomina</code> entity row.
	 * 
	 * @param rowdata
	 *            item of <code>DataImportCompromisoDevengoNomina</code> entity
	 *            that was unsuccessfully imported
	 * @param message
	 *            error message
	 * @param imp_repo
	 *            repository of accounting
	 * @throws org.opentaps.foundation.repository.RepositoryException
	 */
	private void storeImportOperacionPatrimonialError(
			DataImportOperPatr rowdata, String message,
			OperacionPatrimonialDataImportRepositoryInterface imp_repo)
			throws RepositoryException {
		// store the exception and mark as failed
		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_FAILED);
		rowdata.setImportError(message);
		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
		imp_repo.createOrUpdate(rowdata);
	}

	private Timestamp generaFecha() {
		SimpleDateFormat formatoDelTexto = new SimpleDateFormat("dd-MM-yyyy");
		String cadFecha = fechaContable.trim();

		try {
			return new Timestamp(formatoDelTexto.parse(cadFecha).getTime());
		} catch (ParseException e) {
			return null;
		}
	}
}
