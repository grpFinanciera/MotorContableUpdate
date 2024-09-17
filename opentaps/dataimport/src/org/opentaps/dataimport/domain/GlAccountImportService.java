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

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.LocalDispatcher;
import org.opentaps.base.constants.StatusItemConstants;
import org.opentaps.base.entities.AcctgTrans;
import org.opentaps.base.entities.AcctgTransEntry;
import org.opentaps.base.entities.CustomTimePeriod;
import org.opentaps.base.entities.DataImportGlAccount;
import org.opentaps.base.entities.GlAccount;
import org.opentaps.base.entities.GlAccountClass;
import org.opentaps.base.entities.GlAccountClassTypeMap;
import org.opentaps.base.entities.GlAccountOrganization;
import org.opentaps.base.entities.NivelContable;
import org.opentaps.base.entities.PartyGroup;
import org.opentaps.base.entities.TipoCuenta;
import org.opentaps.dataimport.MotorContable;
import org.opentaps.domain.DomainService;
import org.opentaps.domain.dataimport.AccountingDataImportRepositoryInterface;
import org.opentaps.domain.dataimport.GlAccountImportServiceInterface;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.entity.hibernate.Session;
import org.opentaps.foundation.entity.hibernate.Transaction;
import org.opentaps.foundation.infrastructure.Infrastructure;
import org.opentaps.foundation.infrastructure.InfrastructureException;
import org.opentaps.foundation.infrastructure.User;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.service.ServiceException;

/**
 * Import General Ledger accounts via intermediate DataImportGlAccount entity.
 */
public class GlAccountImportService extends DomainService implements
		GlAccountImportServiceInterface {

	private static final String MODULE = GlAccountImportService.class.getName();
	// session object, using to store/search pojos.
	private Session session;
	public String organizationPartyId;
	public int importedRecords;
	private String userLogin;

	public GlAccountImportService() {
		super();
	}

	public GlAccountImportService(Infrastructure infrastructure, User user,
			Locale locale) throws ServiceException {
		super(infrastructure, user, locale);
	}

	/** {@inheritDoc} */
	public void setOrganizationPartyId(String organizationPartyId) {
		this.organizationPartyId = organizationPartyId;
	}

	/** {@inheritDoc} */
	public int getImportedRecords() {
		return importedRecords;
	}

	/** {@inheritDoc} */
	public void importGlAccounts() throws ServiceException {
		try {
			this.session = this.getInfrastructure().getSession();

			AccountingDataImportRepositoryInterface imp_repo = this
					.getDomainsDirectory().getDataImportDomain()
					.getAccountingDataImportRepository();
			String userLoginId = imp_repo.getUser().getUserId();

			LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
					.getLedgerDomain().getLedgerRepository();

			List<DataImportGlAccount> dataforimp = imp_repo
					.findNotProcessesDataImportGlAccountEntries(userLoginId);

			BigDecimal saldoinicial;
			BigDecimal saldodebit = new BigDecimal(0.00);
			BigDecimal saldoCredit = new BigDecimal(0.00);
			String TipoParentId = "";
			String glAccountId = "";
			boolean isDebitOrCredit = false;
			boolean isRegistro = true;
			List<String> cuentas = new ArrayList<String>();
			LocalDispatcher dispatcher = this.getDomainsDirectory().getLedgerDomain().
					getInfrastructure().getDispatcher();
			Delegator delegator = dispatcher.getDelegator();

			int imported = 0;
			Transaction imp_tx1 = null;
			Transaction imp_tx2 = null;
			for (int i = 0; i < dataforimp.size(); i++) {
				DataImportGlAccount rawdata = dataforimp.get(i);
				// import accounts as many as possible
				try {
					imp_tx1 = null;
					imp_tx2 = null;

					// Validacion campos requeridos.
					String resultado = validaCampos(rawdata);

					if (!resultado.isEmpty()) {
						String message = "Error al importar cuenta(s) contable(s): ["
								+ rawdata.getGlAccountId()
								+ "]"
								+ ". "
								+ resultado;
						Debug.log("Valida Campos");
						storeImportGlAccountError(rawdata, message, imp_repo);
						continue;
					}

					// begin importing raw data item
					// GlAccountOrganization glAccountOrganization = new
					// GlAccountOrganization();

					GlAccount glAccount = new GlAccount();
					glAccount.setGlAccountId(rawdata.getGlAccountId());
					glAccount.setParentGlAccountId(rawdata
							.getParentGlAccountId());

					saldoinicial = rawdata.getSaldoinicial();

					if (rawdata.getClassification() != null) {
						// decode account clasification to type Id and class Id
						GlAccountClassTypeMap glAccountClassTypeMap = ledger_repo
								.findOne(
										GlAccountClassTypeMap.class,
										ledger_repo
												.map(GlAccountClassTypeMap.Fields.glAccountClassTypeKey,
														rawdata.getClassification()));

						if (glAccountClassTypeMap == null) {
							String message = "Error al importar cuenta(s) contable(s): ["
									+ rawdata.getGlAccountId()
									+ "]."
									+ " La clasificaci\u00f3n no existe";
							storeImportGlAccountError(rawdata, message,
									imp_repo);
							continue;
						}
						glAccount.setGlAccountTypeId(glAccountClassTypeMap
								.getGlAccountTypeId());
						glAccount.setGlAccountClassId(glAccountClassTypeMap
								.getGlAccountClassId());

						GlAccountClass glaccountclass = ledger_repo.findOne(
								GlAccountClass.class, ledger_repo.map(
										GlAccountClass.Fields.glAccountClassId,
										glAccount.getGlAccountClassId()));

						if (glaccountclass.getParentClassId() != null) {

							do {

								glAccountId = glaccountclass.getParentClassId()
										.toString();
								Debug.logInfo("variable1: " + glAccountId,
										MODULE);

								if (glAccountId.equals("DEBIT")
										|| glAccountId.equals("CREDIT")) {

									TipoParentId = glAccountId;
									isDebitOrCredit = true;
								} else {
									glaccountclass = ledger_repo
											.findOne(
													GlAccountClass.class,
													ledger_repo
															.map(GlAccountClass.Fields.glAccountClassId,
																	glAccountId));
									Debug.logInfo("variable2: "
											+ glaccountclass, MODULE);
									isDebitOrCredit = false;
								}

							} while (isDebitOrCredit == false);

							Debug.logInfo("variable3: " + TipoParentId, MODULE);
							if (rawdata.getGlAccountId().startsWith("1")
									&& 
									!rawdata.getSaldoinicial().toString()
											.trim().equals("0.00")) {
								if (TipoParentId.equals("DEBIT")) {
									cuentas.add(rawdata.getGlAccountId() + ";"
											+ saldoinicial.toString() + ";D");
									saldodebit = saldodebit.add(rawdata
											.getSaldoinicial());
								} else if (TipoParentId.equals("CREDIT")) {
									cuentas.add(rawdata.getGlAccountId() + ";"
											+ saldoinicial.toString() + ";C");
									saldodebit = saldodebit.subtract(rawdata
											.getSaldoinicial());
								}
							} else if (rawdata.getGlAccountId().startsWith("2")
									|| rawdata.getGlAccountId().startsWith("3")
									&& !rawdata.getSaldoinicial().toString()
											.trim().equals("0.00")) {
								if (TipoParentId.equals("CREDIT")) {
									cuentas.add(rawdata.getGlAccountId() + ";"
											+ saldoinicial.toString() + ";C");
									saldoCredit = saldoCredit.add(rawdata
											.getSaldoinicial());
								}
								else if (TipoParentId.equals("DEBIT")) {
									cuentas.add(rawdata.getGlAccountId() + ";"
											+ saldoinicial.toString() + ";D");
									saldoCredit = saldoCredit.subtract(rawdata
											.getSaldoinicial());
								}
							}					
						}
					}

					glAccount.setAccountName(rawdata.getAccountName());
					glAccount.setAccountCode(rawdata.getGlAccountId());
					glAccount.setDescription(rawdata.getDescripcionCorta());
					glAccount.setSiglas(rawdata.getSiglas());
					if (rawdata.getTipoCuenta() != null) {
						if (validaTipoCuenta(ledger_repo,
								rawdata.getTipoCuenta())) {
							glAccount.setTipoCuentaId(rawdata.getTipoCuenta());
						} else {
							String message = "Error al importar cuenta(s) contable(s): ["
									+ rawdata.getGlAccountId()
									+ "]."
									+ " El tipo de cuenta no existe.";
							storeImportGlAccountError(rawdata, message,
									imp_repo);
							continue;
						}
					}
					if ((!glAccount.getTipoCuentaId().equalsIgnoreCase("R"))
							&& (rawdata.getSaldoinicial().compareTo(
									BigDecimal.ZERO) == 1)) {
						isRegistro = false;
//						String message = "Error al importar cuenta(s) contable(s): ["
//								+ rawdata.getGlAccountId()
//								+ "]."
//								+ " No se puede poner saldo en una cuenta que no sea de tipo registro.";
//						storeImportGlAccountError(rawdata, message,
//								imp_repo);
					}
					String nivel = buscaNivel(ledger_repo,
							rawdata.getGlAccountId());
					if (!nivel.isEmpty()) {
						glAccount.setNivelId(nivel);
					} else {
						String message = "Error al importar cuenta(s) contable(s): ["
								+ rawdata.getGlAccountId()
								+ "]."
								+ " El nivel no es valido.";
						storeImportGlAccountError(rawdata, message, imp_repo);
						continue;
					}
					
					if (MotorContable.validaCuenta(delegator,
							rawdata.getGlAccountId(), organizationPartyId) == null) {
						imp_tx1 = this.session.beginTransaction();
						ledger_repo.createOrUpdate(glAccount);
						imp_tx1.commit();

						if (this.organizationPartyId != null) {
							// map organization party to GL accounts
							GlAccountOrganization glAccountOrganization = new GlAccountOrganization();
							glAccountOrganization
									.setOrganizationPartyId(this.organizationPartyId);
							glAccountOrganization.setGlAccountId(rawdata
									.getGlAccountId());
							glAccountOrganization.setFromDate(UtilDateTime
									.nowTimestamp());

							imp_tx2 = this.session.beginTransaction();
							ledger_repo.createOrUpdate(glAccountOrganization);
							imp_tx2.commit();
						}
					}

					String message = "Importaci\u00f3n correcta de cuenta(s) contable(s): ["
							+ rawdata.getGlAccountId() + "].";
					this.storeImportGlAccountSuccess(rawdata, imp_repo);
					Debug.logInfo(message, MODULE);
					
					imported = imported + 1;

				} catch (Exception ex) {
					String message = "Error al importar cuenta(s) contable(s): ["
							+ rawdata.getGlAccountId() + "].";
					storeImportGlAccountError(rawdata, message, imp_repo);

					// rollback all if there was an error when importing item
					if (imp_tx1 != null) {
						imp_tx1.rollback();
					}
					if (imp_tx2 != null) {
						imp_tx2.rollback();
					}

					Debug.logError(ex, message, MODULE);
					throw new ServiceException(ex.getMessage());
				}
			}

			// Si es tipo registro y los saldos son iguales, entonces crea la
			// transaccion.
			Debug.log("saldodebit"+saldodebit);
			Debug.log("saldoCredit"+saldoCredit);
			if (isRegistro && (saldodebit.compareTo(saldoCredit) == 0)) {
				if (saldodebit.compareTo(BigDecimal.ZERO) != 0) {
					GenerarTransaccion(cuentas, saldodebit);
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
	private void storeImportGlAccountSuccess(DataImportGlAccount rawdata,
			AccountingDataImportRepositoryInterface imp_repo)
			throws RepositoryException {
		// mark as success
		rawdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_IMPORTED);
		rawdata.setImportError(null);
		rawdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
		imp_repo.createOrUpdate(rawdata);
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
	private void storeImportGlAccountError(DataImportGlAccount rawdata,
			String message, AccountingDataImportRepositoryInterface imp_repo)
			throws RepositoryException {
		// store the exception and mark as failed
		rawdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_FAILED);
		rawdata.setImportError(message);
		rawdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
		imp_repo.createOrUpdate(rawdata);
	}

	private boolean validaTipoCuenta(LedgerRepositoryInterface ledger_repo,
			String id) throws RepositoryException {
		TipoCuenta tipoCuenta = ledger_repo.findOne(TipoCuenta.class,
				ledger_repo.map(TipoCuenta.Fields.tipoCuentaId, id));
		if (tipoCuenta == null) {
			Debug.log("Error, Tipo Cuenta No Existe");
			return false;
		}
		return true;
	}

	private String buscaNivel(LedgerRepositoryInterface ledger_repo, String id)
			throws RepositoryException {
		int nivel = 1;
		for (int i = 0; i < id.length(); i++) {
			if (id.charAt(i) == '.') {
				nivel++;
			}
		}
		id = nivel + "";
		NivelContable nivelContable = ledger_repo.findOne(NivelContable.class,
				ledger_repo.map(NivelContable.Fields.nivelId, id));
		if (nivelContable == null) {
			Debug.log("Error, Nivel No Valido");
			return "";
		}
		return nivelContable.getNivelId();
	}

	private String validaCampos(DataImportGlAccount rawdata) {
		String mensaje = "";
		if (rawdata.getGlAccountId() == null) {
			mensaje += "El id de la cuenta es obligatorio ";
		}

		if (rawdata.getClassification() == null) {
			mensaje += "La clasificacion es obligatoria ";
		}

		if (rawdata.getAccountName() == null) {
			mensaje += "La denominaci\u00f3n es obligatoria ";
		}

		if (rawdata.getTipoCuenta() == null) {
			mensaje += "El tipo de la cuenta es obligatorio ";
		}

		return mensaje;

	}

	/* Generar Transaccion */
	private void GenerarTransaccion(List<String> Listacuentas,
			BigDecimal saldototal) {

		Transaction imp_tx3 = null;
		Transaction imp_tx4 = null;

		Calendar cale = Calendar.getInstance();
		String annioactual = Integer.toString(cale.get(Calendar.YEAR));
		Boolean actual = false;
		List<GenericValue> histories = null;
		LocalDispatcher dispatcher = this.getDomainsDirectory().getLedgerDomain().
				getInfrastructure().getDispatcher();
		Delegator delegator = dispatcher.getDelegator();	
		try {
			
			userLogin = this.getDomainsDirectory().getUser().getUserId();
			
			LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
					.getLedgerDomain().getLedgerRepository();

			List<CustomTimePeriod> periodo = ledger_repo.findList(
					CustomTimePeriod.class, ledger_repo
							.map(CustomTimePeriod.Fields.periodTypeId,
									"FISCAL_YEAR"));

			if (!periodo.isEmpty()) {

				for (CustomTimePeriod customTimePeriod : periodo) {
					Calendar fechax = Calendar.getInstance();
					fechax.setTimeInMillis(customTimePeriod.getFromDate()
							.getTime());
					String annio = Integer.toString(fechax.get(Calendar.YEAR));
					Debug.log("Annio Lista " + annio);
					Debug.log("AnnioActual.- " + annioactual);
					if (annioactual.equals(annio.trim()))
						actual = true;
				}

				if (actual) {
					Debug.log("A\u00f1o actual " + annioactual);

					Calendar date = Calendar.getInstance();
					date.set(Integer.parseInt(annioactual), 00, 01, 00, 00, 00);
					date.set(Calendar.MILLISECOND,100);
					Timestamp timespan = new Timestamp(date.getTimeInMillis());
					int secuencia = 1;
					
					// History
					Debug.log("Busca periodos");
					List<GenericValue> periodos = MotorContable.obtenPeriodos(
							delegator , organizationPartyId,
							timespan);

					String id_trans = ledger_repo.getNextSeqId("AcctgTrans");
					String padre = obtenPadre(ledger_repo, organizationPartyId);

					AcctgTrans acctgsaldo = new AcctgTrans();
					acctgsaldo.setAcctgTransId("S-" + id_trans);
					acctgsaldo.setAcctgTransTypeId("SALDO_INICIAL");
					acctgsaldo.setDescription("Carga de Saldo Inicial");
					acctgsaldo.setTransactionDate(timespan);
					acctgsaldo.setIsPosted("Y");
					acctgsaldo.setPostedDate(timespan);
					acctgsaldo.setGlFiscalTypeId("ACTUAL");
					acctgsaldo.setCreatedByUserLogin(userLogin);
					acctgsaldo.setLastModifiedByUserLogin(userLogin);
					acctgsaldo.setPostedAmount(saldototal);
					acctgsaldo.setPartyId(padre);
					acctgsaldo.setOrganizationPartyId(organizationPartyId);
					acctgsaldo.setTipoPolizaId("DIARIO");
					acctgsaldo.setOrganizationPartyId(organizationPartyId);
					
					Map<String, Object> input = FastMap.newInstance();
					input.put("fechaContable", timespan);
					input.put("eventoContableId", "SALDO_INICIAL");
					Map<String, Object> result = dispatcher.runSync("generaNumeroPoliza", input);
					
					acctgsaldo.setPoliza((String)result.get("poliza"));

					imp_tx3 = this.session.beginTransaction();
					ledger_repo.createOrUpdate(acctgsaldo);
					imp_tx3.commit();

					String message = "Successfully generate AcctgTrans [GL-"
							+ id_trans + "].";
					Debug.logInfo(message, MODULE);

					for (String cuenta : Listacuentas) {

						String array[] = cuenta.split(";");
						BigDecimal saldo = new BigDecimal(array[1]);

						AcctgTransEntry acctgentry = new AcctgTransEntry();
						acctgentry.setAcctgTransId("S-" + id_trans);
						acctgentry.setAcctgTransEntrySeqId(String.format(
								"%05d", secuencia));
						acctgentry.setAcctgTransEntryTypeId("_NA_");
						acctgentry.setDescription("Carga de Saldo Inicial");
						acctgentry.setGlAccountId(array[0]);
						acctgentry.setOrganizationPartyId(organizationPartyId);
						acctgentry.setAmount(saldo);
						acctgentry.setCurrencyUomId("MXN");
						acctgentry.setDebitCreditFlag(array[2]);
						acctgentry.setReconcileStatusId("AES_NOT_RECONCILED");
						acctgentry.setPartyId(padre);
						
						// GlAccountHistory
						Debug.log("Busca histories y aztualiza cuentas en GlAccountOrganization");
						if (array[2].equals("D")) {
							histories = MotorContable
									.actualizaGlAccountHistories(delegator,
											periodos, array[0], saldo, "Debit");
							MotorContable.actualizaGlAccountOrganization(delegator,
									saldo, array[0], organizationPartyId,
									padre, "Debit");
						} else {
							histories = MotorContable
									.actualizaGlAccountHistories(delegator,
											periodos, array[0], saldo, "Credit");
							MotorContable.actualizaGlAccountOrganization(delegator,
									saldo, array[0], organizationPartyId,
									padre, "Credit");
						}

						
						Debug.log("Se impactan las histories regresadas");
						for (GenericValue history : histories) {
							delegator.createOrStore(history);
						}

						imp_tx4 = this.session.beginTransaction();
						ledger_repo.createOrUpdate(acctgentry);
						imp_tx4.commit();

						String message1 = "Successfully generate AcctgTransEntry [GL-"
								+ id_trans + "].";
						Debug.logInfo(message1, MODULE);

						secuencia = secuencia + 1;
					}

				}
			}

		} catch (Exception e) {
			String message = "Failed to import transaccion, Error message : "
					+ e.getMessage();
			// storeImportGlAccountError(rawdata, message, imp_repo);

			// rollback all if there was an error when importing item
			if (imp_tx3 != null) {
				imp_tx3.rollback();
			}
			if (imp_tx4 != null) {
				imp_tx4.rollback();
			}
			Debug.logError(e, message, MODULE);
			// throw new ServiceException(e.getMessage());
		}		

	}

	public String obtenPadre(LedgerRepositoryInterface ledger_repo,
			String cuenta) throws RepositoryException {
		PartyGroup pg = new PartyGroup();
		do {
			pg = ledger_repo.findOne(PartyGroup.class,
					ledger_repo.map(PartyGroup.Fields.partyId, cuenta));
			cuenta = pg.getParent_id();
		} while (pg.getParent_id() != null);
		return pg.getPartyId();
	}
	
	@SuppressWarnings("unused")
	public List<String> obtenerAgrupador(Delegator delegator, Timestamp fecha)
			throws GenericEntityException {
		
		List<String> idPoliza = new ArrayList<String>();
		List<GenericValue> agrupadores = null;
		
		String prefijo = MotorContable.obtenPrefijo (delegator, fecha);

		agrupadores = MotorContable.obtenerListaConsecutivos(delegator, "DIARIO", organizationPartyId, prefijo);
		
		return idPoliza = MotorContable.obtenAgrupadoryConsecutivo(delegator, agrupadores, prefijo);

	}
	
}
