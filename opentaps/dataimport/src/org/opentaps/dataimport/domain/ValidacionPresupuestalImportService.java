package org.opentaps.dataimport.domain;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.opentaps.base.constants.StatusItemConstants;
import org.opentaps.base.entities.DataImportValidPresup;
import org.opentaps.domain.DomainService;
import org.opentaps.domain.dataimport.ValidacionPresupuestalDataImportRepositoryInterface;
import org.opentaps.domain.dataimport.ValidacionPresupuestalImportServiceInterface;
import org.opentaps.foundation.entity.hibernate.Session;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.service.ServiceException;

import com.ibm.icu.text.SimpleDateFormat;


public class ValidacionPresupuestalImportService extends DomainService implements
ValidacionPresupuestalImportServiceInterface {
	@SuppressWarnings("unused")
	private static final String MODULE = ValidacionPresupuestalImportService.class
			.getName();
	// session object, using to store/search pojos.
	@SuppressWarnings("unused")
	private Session session;
	private String tipoClave;
	private String momento;
	private String fechaContable;
	public int importedRecords;

	public ValidacionPresupuestalImportService() throws ServiceException {
		super();
	}
	/** {@inheritDoc} */
	public void setFechaContable(String fechaContable) {
		this.fechaContable = fechaContable;
	}
	
	/** {@inheritDoc} */
	public void setMomento(String momento) {
		this.momento = momento;
	}
	
	/** {@inheritDoc} */
	public void setTipoClave(String tipoClave) {
		this.tipoClave = tipoClave;
	}
	
	/** {@inheritDoc} */
	public int getImportedRecords() {
		return importedRecords;
	}

	/** {@inheritDoc} */
	public void importValidacionPresupuestal() throws ServiceException {
		try {
			this.session = this.getInfrastructure().getSession();

			ValidacionPresupuestalDataImportRepositoryInterface imp_repo = this
					.getDomainsDirectory().getDataImportDomain()
					.getValidacionPresupuestalDataImportRepository();

			String userLoginId = this.getDomainsDirectory()
					.getLedgerDomain().getLedgerRepository().getUser().getOfbizUserLogin().getString("userLoginId");
			Debug.log("Omar-userLoginId: " + userLoginId);
			
			
			List<DataImportValidPresup> dataImport = imp_repo
					.findNotProcessesDataImportValidacionPresupuestalEntries(userLoginId);
			
			Delegator delegator = this.getDomainsDirectory().getLedgerDomain()
					.getInfrastructure().getDelegator();

			boolean isOk = true;
			
			//Verificamos si el momento existe.
			GenericValue momentoPres = delegator.findByPrimaryKey("Momento", UtilMisc.toMap("momentoId", momento));
			String mensaje="";
			
			if(momentoPres==null){
				isOk = false;
				mensaje = "No existe el momento: "+ momento+". ";
			}
			
			Timestamp fechaC = generaFecha();
			if(fechaC==null){
				isOk=false;
				mensaje += "El formato de fecha no es el correcto";
			}
			
			if(isOk){
				// Traemos las clavesPresupuestales
				Map<String, GenericValue> claves = generaMapa(delegator);

				// Traemos los valores segun el momento y fecha.
				Map<String, GenericValue> clavesMomento = generaMapaMomento(delegator, fechaC);

				Debug.log("VP-Inica for-each");
				for (DataImportValidPresup rowdata : dataImport) {
					// Concatenamos las clasificaciones.
					String clave = "";
					Debug.log("CDN- se forma la clave presupuestal");
					for (int i = 1; i < 16; i++) {
						String campo = "clasificacion" + i;
						String valor = rowdata.getString(campo);
						if (valor != null) {
							clave += valor;
						} else {
							break;
						}
					}

					// Verificamos si existe la clave presupuestal.
					GenericValue generic = claves.get(clave);
					if (generic == null) {
						storeImportValidacionPresupuestalError(rowdata,
								"No existe la clave presupuestal", imp_repo);
						continue;
					}
					
					//Validamos suficiencia.
					generic =  clavesMomento.get(clave);
					BigDecimal montoFinal;
					if(generic==null){
						montoFinal = BigDecimal.ZERO.subtract(rowdata.getMonto());
					}else{
						montoFinal = generic.getBigDecimal("monto").subtract(rowdata.getMonto());
					}
					
					storeImportValidacionPresupuestalSuccess(rowdata, montoFinal.toString(), imp_repo);

				}
			}else{
				storeImportValidacionPresupuestalError(dataImport.get(0), mensaje, imp_repo);
			}
		} catch (Exception ex) {
			Debug.log("JRRM.- " + ex.getMessage());

		}
	}
	
	private void storeImportValidacionPresupuestalSuccess(
			DataImportValidPresup rowdata, String message,
			ValidacionPresupuestalDataImportRepositoryInterface imp_repo)
			throws RepositoryException {
		// mark as success
		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_IMPORTED);
		rowdata.setImportError(message);
		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
		imp_repo.createOrUpdate(rowdata);
	}

	private void storeImportValidacionPresupuestalError(
			DataImportValidPresup rowdata, String message,
			ValidacionPresupuestalDataImportRepositoryInterface imp_repo)
			throws RepositoryException {
		// store the exception and mark as failed
		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_FAILED);
		rowdata.setImportError(message);
		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
		imp_repo.createOrUpdate(rowdata);
	}
	
	private Timestamp generaFecha(){
		SimpleDateFormat formatoDelTexto = new SimpleDateFormat("dd-MM-yyyy");
		String cadFecha = fechaContable.trim();
		
		try {			
			return new Timestamp(formatoDelTexto.parse(cadFecha).getTime());
		} catch (ParseException e) {
			return null;			
		}
	}
	
	private Map<String, GenericValue> generaMapa(Delegator delegator)
			throws GenericEntityException {
		List<GenericValue> claves = delegator.findByAnd("ClavePresupuestal",
				"tipo", tipoClave);
		Map<String, GenericValue> mapaClave = new HashMap<String, GenericValue>();
		for (GenericValue clave : claves) {
			mapaClave.put(clave.getString("clavePresupuestal"), clave);
		}

		return mapaClave;
	}
	
	private Map<String, GenericValue> generaMapaMomento(Delegator delegator, Timestamp fecha)
			throws GenericEntityException {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(fecha.getTime());
		String mes = obtenMesString(cal.get(Calendar.MONTH));
		List<GenericValue> claves = delegator.findByAnd("ControlPresupuestal",
				"momentoId", momento,"mesId",mes);
		Map<String, GenericValue> mapaClave = new HashMap<String, GenericValue>();
		for (GenericValue clave : claves) {
			mapaClave.put(clave.getString("clavePresupuestal"), clave);
		}

		return mapaClave;
	}
	
	private static String obtenMesString(int mes) {
		mes++;
		return mes < 10 ? "0" + new Integer(mes).toString() : new Integer(mes)
				.toString();
	};

}
