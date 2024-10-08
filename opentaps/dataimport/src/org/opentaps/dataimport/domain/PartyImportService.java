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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ofbiz.base.util.Debug;

import java.util.Locale;

import org.ofbiz.base.util.UtilDateTime;
import org.opentaps.base.constants.StatusItemConstants;
import org.opentaps.base.entities.DataImportParty;
import org.opentaps.base.entities.Party;
import org.opentaps.base.entities.PartyGroup;
import org.opentaps.base.entities.PartyRelationship;
import org.opentaps.base.entities.PartyRole;
import org.opentaps.base.entities.RoleType;
import org.opentaps.base.entities.Uom;
import org.opentaps.dataimport.UtilImport;
import org.opentaps.domain.DomainService;
import org.opentaps.domain.dataimport.PartyDataImportRepositoryInterface;
import org.opentaps.domain.dataimport.PartyImportServiceInterface;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.entity.hibernate.Session;
import org.opentaps.foundation.entity.hibernate.Transaction;
import org.opentaps.foundation.infrastructure.Infrastructure;
import org.opentaps.foundation.infrastructure.InfrastructureException;
import org.opentaps.foundation.infrastructure.User;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.service.ServiceException;


/**
 * Import different organizations via intermediate DataImportParty entity.
 */
public class PartyImportService extends DomainService implements PartyImportServiceInterface {

	private static final String MODULE = PartyImportService.class.getName();
	// session object, using to store/search pojos.
	private Session session;
	public String organizationPartyId;
	public int importedRecords;
	//private Object ContactMechServices;
	static Logger logger = Logger.getLogger(PartyImportService.class);

	public PartyImportService() {
		super();
	}

	public PartyImportService(Infrastructure infrastructure, User user,
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
	public void importParty() throws ServiceException {
		try {

			Debug.logInfo("Entra a importParty()", MODULE);
			this.session = this.getInfrastructure().getSession();

			PartyDataImportRepositoryInterface imp_repo = this
					.getDomainsDirectory().getDataImportDomain()
					.getPartyDataImportRepository();
			
			String userLoginId = imp_repo.getUser().getUserId();

			LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
					.getLedgerDomain().getLedgerRepository();

			List<DataImportParty> dataforimp = imp_repo.findNotProcessesDataImportPartyEntries(userLoginId);
			
			
			int imported = 0;
			Transaction imp_tx1 = null;
			Transaction imp_tx2 = null;
			
			Debug.logInfo("dataforimp" + dataforimp, MODULE);
			for (DataImportParty rowdata : dataforimp) {
				// import parties as many as possible
				
				try {
					imp_tx1 = null;
					imp_tx2 = null;
					// begin importing raw data item
					if (rowdata.getGroupName() != null) {
						if (rowdata.getExternalId() != null) {
							if (rowdata.getNivel() != null) {
								if (rowdata.getGroupNameLocal() != null) {
									if (rowdata.getState() != null) {
										
										
										
										Party party = new Party();

										if (!UtilImport
												.validaNivel(ledger_repo,
														rowdata.getNivel()))
											throw new ServiceException(
													String.format("Nivel no valido - No se encuentra registrado"));

										
										
										party.setExternalId(rowdata.getExternalId());
										party.setNivel_id(rowdata.getNivel());
										party.setPartyTypeId("PARTY_GROUP");
										party.setPartyId(rowdata.getExternalId());
										//party.setNode(rowdata.getNode());
										party.setState(rowdata.getState());

										party.setPreferredCurrencyUomId("MXN");

										imp_tx1 = this.session
												.beginTransaction();
										ledger_repo.createOrUpdate(party);
										imp_tx1.commit();

										
										
										PartyGroup partygroup = new PartyGroup();

										if (!UtilImport.validaPadreParty(
												ledger_repo,
												rowdata.getNivel(),
												rowdata.getParentExternalId()))
											throw new ServiceException(
													String.format("Padre no valido"));
										
										

										partygroup.setGroupName(rowdata.getGroupName());
										partygroup.setGroupNameLocal(rowdata.getGroupNameLocal());
										partygroup.setParent_id(rowdata.getParentExternalId());
										partygroup.setPartyId(rowdata.getExternalId());
										partygroup.setFederalTaxId(rowdata.getRfc());

										imp_tx2 = this.session
												.beginTransaction();
										ledger_repo.createOrUpdate(partygroup);
										imp_tx2.commit();

										
										
											List<String> listaRol = new ArrayList<String>();
											// Valores por default
											listaRol.add("ORGANIZATION_UNIT");
											listaRol.add("PARENT_ORGANIZATION");

											listaRol.add("INTERNAL_ORGANIZATIO");
											listaRol.add("MAIN_ROLE");
											

											for (Object rol : listaRol) {

												getRolOrganization(ledger_repo,
														rowdata, rol.toString());
											}
											
											

										// /Relacion entre organizaciones
										if (rowdata.getParentExternalId() != null) {
											getRelationshipOrganization(
													ledger_repo, rowdata);
										}
										
										

										String message = "Successfully imported Party ["
												+ rowdata.getExternalId()
												+ "].";
										
										this.storeImportPartySuccess(rowdata, imp_repo);
										
										Debug.logInfo(message, MODULE);

										imported = imported + 1;
									} else
										throw new ServiceException(
												String.format("Falta Ingresar Estado"));
								} else
									throw new ServiceException(
											String.format("Falta Ingresar Descripci\u00f3n"));
							} else
								throw new ServiceException(
										String.format("Falta Ingresar Nivel"));
						} else
							throw new ServiceException(
									String.format("Falta Ingresar Codigo Presupuestal"));
					} else
						throw new ServiceException(
								String.format("Falta Ingresar Nombre"));

				} catch (Exception ex) {
					String message = ex.getMessage();
					storeImportPartyError(rowdata, message, imp_repo);

					// rollback all if there was an error when importing item
					if (imp_tx1 != null) {
						imp_tx1.rollback();
					}
					if (imp_tx2 != null) {
						imp_tx2.rollback();
					}
					// Debug.logError(ex, message, MODULE);
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
	 * Generar la relacion entre organizaciones
	 * 
	 * @param ledger_repo
	 * @param rowdata
	 */
	private void getRelationshipOrganization(
			LedgerRepositoryInterface ledger_repo, DataImportParty rowdata) {
		// TODO Auto-generated method stub
		Transaction imp_tx1 = null;

		try {
			PartyRelationship relationship = new PartyRelationship();
			// hijo
			relationship.setPartyIdTo(rowdata.getExternalId());
			relationship.setRoleTypeIdTo("ORGANIZATION_UNIT");

			// Padre
			relationship.setPartyIdFrom(rowdata.getParentExternalId());
			relationship.setRoleTypeIdFrom("PARENT_ORGANIZATION");
			relationship.setPartyRelationshipTypeId("GROUP_ROLLUP");
			relationship.setFromDate(UtilDateTime.nowTimestamp());
			relationship.setCreatedStamp(UtilDateTime.nowTimestamp());
			relationship.setCreatedTxStamp(UtilDateTime.nowTimestamp());
			relationship.setLastUpdatedStamp(UtilDateTime.nowTimestamp());
			relationship.setLastUpdatedTxStamp(UtilDateTime.nowTimestamp());

			imp_tx1 = this.session.beginTransaction();
			ledger_repo.createOrUpdate(relationship);
			imp_tx1.commit();

		} catch (Exception e) {
			// TODO: handle exception
			String message = "Failed to import PartyRelationship ["
					+ rowdata.getExternalId() + "], Error message : "
					+ e.getMessage();
			Debug.log(message, MODULE);

			if (imp_tx1 != null) {
				imp_tx1.rollback();
			}
		}

	}

	/**
	 * Obtener Rol para la organizacion <code>PartyRole</code> entity row.
	 * 
	 * @param imp_repo
	 * 
	 * @param imp_repo
	 *            repository of PartyRole
	 * @throws RepositoryException
	 * 
	 */

	private void getRolOrganization(LedgerRepositoryInterface ledger_repo,
			DataImportParty rowdata, String rol) {

		Transaction imp_tx1 = null;

		try {

			RoleType tiporol = ledger_repo.findOne(RoleType.class,
					ledger_repo.map(RoleType.Fields.roleTypeId, rol));

			logger.debug("Si entro a la funcion getRolOrganization");
			logger.debug("********************* Tipo Rol " + tiporol);
			PartyRole partyrol = new PartyRole();
			partyrol.setPartyId(rowdata.getExternalId());
			partyrol.setRoleTypeId(tiporol.getRoleTypeId());
			partyrol.setLastUpdatedStamp(UtilDateTime.nowTimestamp());
			partyrol.setLastUpdatedTxStamp(UtilDateTime.nowTimestamp());
			partyrol.setCreatedStamp(UtilDateTime.nowTimestamp());
			partyrol.setCreatedTxStamp(UtilDateTime.nowTimestamp());

			imp_tx1 = this.session.beginTransaction();
			ledger_repo.createOrUpdate(partyrol);
			imp_tx1.commit();

			String message = "Successfully imported PartyRole ["
					+ rowdata.getExternalId() + "].";
			Debug.logInfo(message, MODULE);

		} catch (Exception e) {

			String message = "Failed to import PartyRol ["
					+ rowdata.getExternalId() + "], Error message : "
					+ e.getMessage();
			Debug.logInfo(message, MODULE);

			if (imp_tx1 != null) {
				imp_tx1.rollback();
			}
		}

	}

	/**
	 * Helper method to store GL account import succes into
	 * <code>DataImportGlAccount</code> entity row.
	 * 
	 * @param rawdata
	 *            item of <code>DataImportGlAccount</code> entity that was
	 *            successfully imported
	 * @param imp_repo
	 *            repository of accounting
	 * @throws org.opentaps.foundation.repository.RepositoryException
	 */
	private void storeImportPartySuccess(DataImportParty rowdata,
			PartyDataImportRepositoryInterface imp_repo)
			throws RepositoryException {
		// mark as success
		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_IMPORTED);
		rowdata.setImportError(null);
		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
		imp_repo.createOrUpdate(rowdata);
	}

	/**
	 * Helper method to store GL account import error into
	 * <code>DataImportGlAccount</code> entity row.
	 * 
	 * @param rawdata
	 *            item of <code>DataImportGlAccount</code> entity that was
	 *            unsuccessfully imported
	 * @param message
	 *            error message
	 * @param imp_repo
	 *            repository of accounting
	 * @throws org.opentaps.foundation.repository.RepositoryException
	 */
	private void storeImportPartyError(DataImportParty rowdata, String message,
			PartyDataImportRepositoryInterface imp_repo)
			throws RepositoryException {
		// store the exception and mark as failed
		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_FAILED);
		rowdata.setImportError(message);
		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
		imp_repo.createOrUpdate(rowdata);
	}

}
