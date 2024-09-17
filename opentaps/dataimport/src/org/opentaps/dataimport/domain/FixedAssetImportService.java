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

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.LocalDispatcher;
import org.opentaps.base.constants.StatusItemConstants;
import org.opentaps.base.entities.DataImportFixedAsset;
import org.opentaps.base.entities.FixedAsset;
import org.opentaps.base.entities.PartyFixedAssetAssignment;
import org.opentaps.domain.DomainService;
import org.opentaps.domain.dataimport.FixedAssetDataImportRepositoryInterface;
import org.opentaps.domain.dataimport.FixedAssetImportServiceInterface;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.entity.hibernate.Session;
import org.opentaps.foundation.entity.hibernate.Transaction;
import org.opentaps.foundation.infrastructure.Infrastructure;
import org.opentaps.foundation.infrastructure.InfrastructureException;
import org.opentaps.foundation.infrastructure.User;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.service.ServiceException;

/**
 * Import fixed assets via intermediate DataImportFixedAsset entity.
 */
public class FixedAssetImportService extends DomainService implements FixedAssetImportServiceInterface {

	private static final String MODULE = FixedAssetImportService.class.getName();
	
	private static List<String> CUCOP = new ArrayList<String>(); 
	
	// session object, using to store/search pojos.
	private Session session;
	public String organizationPartyId;
	public int importedRecords;
	static Logger logger = Logger.getLogger(FixedAssetImportService.class);

	public FixedAssetImportService() {
		super();
	}

	public FixedAssetImportService(Infrastructure infrastructure, User user,
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
	public void importFixedAsset() throws ServiceException {
		try {
			Debug.logInfo("Entra a importFixedAsset()", MODULE);
			this.session = this.getInfrastructure().getSession();
			FixedAssetDataImportRepositoryInterface imp_repo = this.getDomainsDirectory().getDataImportDomain().getFixedAssetDataImportRepository();
			String userLoginId = imp_repo.getUser().getUserId();
			LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory().getLedgerDomain().getLedgerRepository();
			List<DataImportFixedAsset> dataforimp = imp_repo.findNotProcessesDataImportFixedAssetEntries(userLoginId);
			LocalDispatcher dispatcher = this.getDomainsDirectory().getLedgerDomain().getInfrastructure().getDispatcher();
			Delegator delegator = dispatcher.getDelegator();
			int imported = 0;
			Transaction imp_tx1 = null;

			for (DataImportFixedAsset rowdata : dataforimp){
				try{
					imp_tx1 = null;
					// begin importing row data item
					if (rowdata.getFixedAssetId()!=null && rowdata.getFixedAssetName()!=null && rowdata.getPurchaseCost()!=null){
						FixedAsset fixedAsset = new FixedAsset();
						String idActivoFijo = "";
						Debug.log("IdDataimport: " + rowdata.getFixedAssetId());
						
						idActivoFijo = obtenIdentificadorActivoFijo(delegator, rowdata.getProduct());
						
						fixedAsset.setFixedAssetId(idActivoFijo);
						fixedAsset.setInstanceOfProductId(rowdata.getProduct());
						fixedAsset.setFixedAssetTypeId(rowdata.getClase());
						fixedAsset.setPartyId(rowdata.getPartyId());
						fixedAsset.setFixedAssetName(rowdata.getFixedAssetName());
						fixedAsset.setDateAcquired(rowdata.getDateAcquired());
						if(rowdata.getObsolescencia() != null) {
							fixedAsset.setExpectedEndOfLife(new Date(rowdata.getObsolescencia().getTime()));	
						}
						fixedAsset.setSerialNumber(rowdata.getSerialNumber());
						fixedAsset.setPurchaseCost(rowdata.getPurchaseCost());
						fixedAsset.setUbicacionRapidaId(rowdata.getUbicacionRapidaId());
						fixedAsset.setMarca(rowdata.getMarca());
						fixedAsset.setModelo(rowdata.getModelo());
						fixedAsset.setNumTarjetaCirculacion(rowdata.getNumTarjetaCirculacion());
						fixedAsset.setUsoVehiculo(rowdata.getUsoVehiculo());
						fixedAsset.setNumeroMotor(rowdata.getNumeroMotor());
						fixedAsset.setNumeroPoliza(rowdata.getPolizaSeguro());
						fixedAsset.setMoneda(rowdata.getMoneda());
						fixedAsset.setFechaVencGarantia(rowdata.getVencimientoGarantia());
						fixedAsset.setEdoFisicoId(rowdata.getEstadoFisico());
						fixedAsset.setNumeroFactura(rowdata.getFactura());
						fixedAsset.setProveedor(rowdata.getProveedor());
						fixedAsset.setAreaPartyId(rowdata.getArea());						
						fixedAsset.setFechaIniPoliza(rowdata.getFechaInicioSeguro());
						fixedAsset.setFechaFinPoliza(rowdata.getFechaFinSeguro());
						fixedAsset.setObservaciones(rowdata.getComments());						
						fixedAsset.setCaracteristicas(rowdata.getCaracteristicas());
						fixedAsset.setDenominacionPartidaGen(rowdata.getDenominacionPartidaGen());
						fixedAsset.setIdActivoAnterior(rowdata.getIdActivoAnterior());
						fixedAsset.setAniosVidaUtil(rowdata.getAniosVidaUtil());
						fixedAsset.setOrganizationPartyId(rowdata.getOrganizationPartyId());
						fixedAsset.setStatusId(rowdata.getStatusId());
						fixedAsset.setDepreciation(rowdata.getDepreciation());
						
						//Bienes inmuebles
						fixedAsset.setDomicilio(rowdata.getDomicilio());
						fixedAsset.setMunicipio(rowdata.getMunicipio());
						fixedAsset.setLocalidad(rowdata.getLocalidad());
						fixedAsset.setEjido(rowdata.getEjido());
						fixedAsset.setTipoTerreno(rowdata.getTipoTerreno());
						fixedAsset.setTipoDocumentoLegalPropiedad(rowdata.getTipoDocumentoLegalPropiedad());
						fixedAsset.setDescDocumentoLegalPropiedad(rowdata.getDescDocumentoLegalPropiedad());
						fixedAsset.setOrigenAdquisicion(rowdata.getOrigenAdquisicion());
						fixedAsset.setFormaAdquisicion(rowdata.getFormaAdquisicion());
						fixedAsset.setTipoEmisorTituloPropiedad(rowdata.getTipoEmisorTituloPropiedad());
						fixedAsset.setValorContruccion(rowdata.getValorContruccion());
						fixedAsset.setValorRazonable(rowdata.getValorRazonable());
						fixedAsset.setFechaAvaluo(rowdata.getFechaAvaluo());
						fixedAsset.setFechaTituloPropiedad(rowdata.getFechaTituloPropiedad());
						fixedAsset.setClaveCatastral(rowdata.getClaveCatastral());
						fixedAsset.setFechaInicioClaveCatastral(rowdata.getFechaInicioClaveCatastral());
						fixedAsset.setVencimientoClaveCatastral(rowdata.getVencimientoClaveCatastral());
						fixedAsset.setSuperficieTerreno(rowdata.getSuperficieTerreno());
						fixedAsset.setSuperficieConstruccion(rowdata.getSuperficieConstruccion());
						fixedAsset.setSuperficieDisponible(rowdata.getSuperficieDisponible());
						fixedAsset.setFolioRppc(rowdata.getFolioRppc());
						fixedAsset.setFechaInscripcionRppc(rowdata.getFechaInscripcionRppc());
						fixedAsset.setCiudadInscripcionRPPC(rowdata.getCiudadInscripcionRPPC());
						fixedAsset.setFechaIncorporacionInventario(rowdata.getFechaIncorporacionInventario());
						
						//Bienes inmuebles
						fixedAsset.setPlaca(rowdata.getPlaca());
						fixedAsset.setTipoUnidadVehiculo(rowdata.getTipoUnidadVehiculo());
						fixedAsset.setFechaFactura (rowdata.getFechaFactura());
						fixedAsset.setTalon (rowdata.getTalon());
						fixedAsset.setCheque (rowdata.getCheque());
						fixedAsset.setFechaPago (rowdata.getFechaPago());
						fixedAsset.setTipoAdjudicacionId(rowdata.getTipoAdjudicacion());
						fixedAsset.setSubUnidadResponsable (rowdata.getSubUnidadResponsable());
						
						//Almacen
						fixedAsset.setFacilityId(rowdata.getFacilityId());											

						imp_tx1 = this.session.beginTransaction();
						ledger_repo.createOrUpdate(fixedAsset);
						imp_tx1.commit();
						Debug.log("rowdata.getPartyId():" + rowdata.getPartyId());
						if(rowdata.getPartyId() != null && !rowdata.getPartyId().equals(""))
						{	Transaction imp_tx2 = null;
							// begin importing row data item						

							PartyFixedAssetAssignment partyFixedAssetAssignment = new PartyFixedAssetAssignment();
							
							partyFixedAssetAssignment.setPartyId(rowdata.getPartyId());
							partyFixedAssetAssignment.setRoleTypeId("EMPLEADO");
							partyFixedAssetAssignment.setFixedAssetId(idActivoFijo);   
							partyFixedAssetAssignment.setFromDate(rowdata.getFechaInicioResguardo());
							partyFixedAssetAssignment.setThruDate(rowdata.getFechaFinResguardo());
							partyFixedAssetAssignment.setAllocatedDate(rowdata.getFechaAsignacion());
							partyFixedAssetAssignment.setStatusId("ASIGN_ASIGNADO");
							partyFixedAssetAssignment.setComments(rowdata.getComentarioResguardo());
							
							imp_tx2 = this.session.beginTransaction();
							ledger_repo.createOrUpdate(partyFixedAssetAssignment);
							imp_tx2.commit();
						}

						String message = "Successfully imported Fixed Asset ["
								+ idActivoFijo
								+ "].";

						this.storeImportFixedAssetSuccess(rowdata, imp_repo);
						Debug.logInfo(message, MODULE);
						imported = imported + 1;
					}
					else
					{
						String message = "Error al importar el activo fijo. "
								+ "Es necesario ingresar los campos obligatorios.";
						Debug.log(message);
						storeImportFixedAssetError(rowdata, message, imp_repo);
						continue;
					}

				} catch (Exception ex) {
					Debug.log("Catch");
					String message = ex.getMessage();									
					storeImportFixedAssetError(rowdata, message, imp_repo);
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
	private void storeImportFixedAssetSuccess(DataImportFixedAsset rowdata,
			FixedAssetDataImportRepositoryInterface imp_repo)
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
	private void storeImportFixedAssetError(DataImportFixedAsset rowdata, String message,
			FixedAssetDataImportRepositoryInterface imp_repo)
			throws RepositoryException {
		Debug.log("storeImportFixedAssetError");
		// store the exception and mark as failed
		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_FAILED);
		rowdata.setImportError(message);
		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
		imp_repo.createOrUpdate(rowdata);
	}
	
	public static String obtenIdentificadorActivoFijo(Delegator delegator, String cucop) throws GenericEntityException  	
	{	String idActivoFijo = "";
		long contadorCucop = 0;		
		
		Debug.log("cucop: " + cucop);
		
		int tamanioSecuencia = cucop.startsWith("58") ? 3 : 6;
		
		EntityCondition condicion = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("instanceOfProductId", EntityOperator.EQUALS, cucop));
		contadorCucop = delegator.findCountByCondition("FixedAsset", condicion, null, null);
		
		Debug.log("contadorCucop: " + contadorCucop);
		
		idActivoFijo = cucop.concat(UtilFormatOut.formatPaddedNumber((contadorCucop + 1), tamanioSecuencia));
		CUCOP.add(cucop);
		
		Debug.log("idActivoFijo: " + idActivoFijo);
		
         if(idActivoFijo.length() > 20){
        	 throw new GenericEntityException("El numero de activo fijo supera el limite permitido (20 caracteres)");
         }
				
		return idActivoFijo;
	}

}
