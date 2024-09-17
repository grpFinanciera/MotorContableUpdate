package org.opentaps.dataimport.domain;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javolution.util.FastList;

import org.hibernate.Query;
import org.ofbiz.base.crypto.HashCrypt;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericModelException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.security.Security;
import org.ofbiz.service.LocalDispatcher;
import org.opentaps.base.constants.StatusItemConstants;
import org.opentaps.base.entities.ContactMech;
import org.opentaps.base.entities.DataImportUser;
import org.opentaps.base.entities.EmplPosition;
import org.opentaps.base.entities.Party;
import org.opentaps.base.entities.PartyContactMech;
import org.opentaps.base.entities.PartyContactMechPurpose;
import org.opentaps.base.entities.PartyRole;
import org.opentaps.base.entities.Person;
import org.opentaps.base.entities.RoleType;
import org.opentaps.base.entities.UserLogin;
import org.opentaps.base.entities.UserLoginSecurityGroup;
import org.opentaps.domain.DomainService;
import org.opentaps.domain.dataimport.UserDataImportRepositoryInterface;
import org.opentaps.domain.dataimport.UserImportServiceInterface;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.entity.hibernate.Session;
import org.opentaps.foundation.entity.hibernate.Transaction;
import org.opentaps.foundation.infrastructure.Infrastructure;
import org.opentaps.foundation.infrastructure.InfrastructureException;
import org.opentaps.foundation.infrastructure.User;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.service.ServiceException;

/*
 * Clase para imprtar usuarios
 */
public class UserImportService extends DomainService implements
		UserImportServiceInterface {

	private static final String MODULE = UserImportService.class.getName();
	private Session session;
	public int importedRecords;
	public static final String resource = "SecurityextUiLabels";
	protected LocalDispatcher dispatcher = null;

	public UserImportService() {
		super();
	}

	public UserImportService(Infrastructure infrastructure, User user,
			Locale locale) throws ServiceException {
		super(infrastructure, user, locale);
	}

	/** {@inheritDoc} */
	public int getImportedRecords() {
		return importedRecords;
	}

	public LocalDispatcher getDispatcher() {
		return this.dispatcher;
	}

	/** {@inheritDoc} */

	public void importUser() throws ServiceException {
		Transaction imp_tx1 = null, imp_tx2 = null, imp_tx3 = null;
		try {
			Debug.log("Entra a import de usuarios");
			this.session = this.getInfrastructure().getSession();

			//Declaracion de variables
			UserDataImportRepositoryInterface imp_repo = this
					.getDomainsDirectory().getDataImportDomain()
					.getUserDataImportRepository();

			LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
					.getLedgerDomain().getLedgerRepository();

			Delegator delegator = this.getDomainsDirectory().getLedgerDomain()
					.getInfrastructure().getDelegator();

			Security security = this.getSecurity();
			
			String userLoginId = this.getDomainsDirectory()
					.getLedgerDomain().getLedgerRepository().getUser().getOfbizUserLogin().getString("userLoginId");

			List<DataImportUser> dataforimp = imp_repo
					.findNotProcessesDataImportUserEntries(userLoginId);
			
			Debug.log("Usuarios: " + dataforimp);

			int imported = 0;

			Timestamp nowTimestamp = UtilDateTime.nowTimestamp();

			//creacion de usuario
			for (DataImportUser rowdata : dataforimp) {
				Debug.log("Entra con el usuario: " + rowdata);
				//variables que necesitamos para ingresar en las tablas correspondientes
				//en caso de que tengan informacion
				String genero = null, estadoC = null, empleo = null, residencia = null, 
						moneda = null, estatus = null, area = null, parent = null, rol = null,
						entidadContable = null;
				Date birthDate = null;
				Date passportExpireDate = null;
				
				//Se valida que se haya ingresado el identificador del usuario
				if (rowdata.getUsuarioId() == null
						|| rowdata.getUsuarioId().equals("")) {
					String message = "Error al importar al usuario: ["
							+ rowdata.getUsuarioId() + "]" + ". "
							+ "Es necesario ingresar el numero de empleado del usuario";
					Debug.log(message);
					storeImportUserError(rowdata, message, imp_repo);
					continue;
				}
				// Se valida si ya se dio de alta el usuario
				Party party = ledger_repo.findOne(
						Party.class,
						ledger_repo.map(Party.Fields.partyId,
								rowdata.getUsuarioId()));
				if (party != null) {
					String message = "Error al importar al usuario: ["
							+ rowdata.getUsuarioId() + "]" + ". "
							+ "El numero de empleado ya existe.";
					Debug.log(message);
					storeImportUserError(rowdata, message, imp_repo);
					continue;
				}
				//Se valida que se haya ingresado el identificador del usuario
				if (rowdata.getUsuarioLogId() == null
						|| rowdata.getUsuarioLogId().equals("")) {
					String message = "Error al importar al usuario: ["
							+ rowdata.getUsuarioId() + "]" + ". "
							+ "Es necesario ingresar el identificador de usuario";
					Debug.log(message);
					storeImportUserError(rowdata, message, imp_repo);
					continue;
				}
				// Se valida si ya se dio de alta el usuerLoginId
				UserLogin userL = ledger_repo.findOne(
						UserLogin.class,
						ledger_repo.map(UserLogin.Fields.userLoginId,
								rowdata.getUsuarioLogId()));
				if (userL != null) {
					String message = "Error al importar al usuario: ["
							+ rowdata.getUsuarioId() + "]" + ". "
							+ "El usuario ya existe.";
					Debug.log(message);
					storeImportUserError(rowdata, message, imp_repo);
					continue;
				}
				//Se valida que se haya ingresado la contrasenia
				if (rowdata.getCurrentPassword() == null
						|| rowdata.getCurrentPassword().equals("")) {
					String message = "Error al importar al usuario: ["
							+ rowdata.getUsuarioId() + "]" + ". "
							+ "La contrase\u00f1a no ha sido ingresada.";
					Debug.log(message);
					storeImportUserError(rowdata, message, imp_repo);
					continue;
				}
				//Se valida que se haya ingresado la verificacion de la contrasenia
				if (rowdata.getVerifyCurrentPassword() == null
						|| rowdata.getVerifyCurrentPassword().equals("")) {
					Debug.log("Entra a validar password");
					String message = "Error al importar al usuario: ["
							+ rowdata.getUsuarioId()
							+ "]"
							+ ". "
							+ "La verificaci\u00f3n de la contrase\u00f1a no ha sido ingresada.";
					Debug.log(message);
					storeImportUserError(rowdata, message, imp_repo);
					continue;
				}
				//Se valida que se haya ingresado el nombre del usuario
				if (rowdata.getFirstName() == null
						|| rowdata.getFirstName().equals("")) {
					Debug.log("Entra a validar nombre del usuario");
					String message = "Error al importar al usuario: ["
							+ rowdata.getUsuarioId() + "]" + ". "
							+ "El nombre del usuario no ha sido ingresado.";
					Debug.log(message);
					storeImportUserError(rowdata, message, imp_repo);
					continue;
				}
				//Se valida que se haya ingresado el apellido del usuario
				if (rowdata.getLastName() == null
						|| rowdata.getLastName().equals("")) {
					Debug.log("Entra a validar apellido del usuario");
					String message = "Error al importar al usuario: ["
							+ rowdata.getUsuarioId() + "]" + ". "
							+ "Los apellidos no ha sido ingresado.";
					Debug.log(message);
					storeImportUserError(rowdata, message, imp_repo);
					continue;
				}
				//Se valida que se haya ingresado la moneda
				if (rowdata.getPreferredCurrencyUomId() == null
						|| rowdata.getPreferredCurrencyUomId().equals("")) {
					Debug.log("Entra a validar la moneda");
					String message = "Error al importar al usuario: ["
							+ rowdata.getUsuarioId() + "]" + ". "
							+ "La moneda no ha sido ingresada.";
					Debug.log(message);
					storeImportUserError(rowdata, message, imp_repo);
					continue;
				}
				//Se valida que se haya ingresado el area
				if (rowdata.getAreaId() == null
						|| rowdata.getAreaId().equals("")) {
					Debug.log("Entra a validar el area");
					String message = "Error al importar al usuario: ["
							+ rowdata.getUsuarioId() + "]" + ". "
							+ "El \u00e1rea no ha sido ingresada.";
					Debug.log(message);
					storeImportUserError(rowdata, message, imp_repo);
					continue;
				}
				//Se valida que el correo haya sido cargado y que se haya ingresado correctamente
				if (rowdata.getInfoString() == null
						|| rowdata.getInfoString().equals("")) {
					String message = "Error al importar al usuario: ["
							+ rowdata.getUsuarioId() + "]" + ". "
							+ "El correo no ha sido ingresada.";
					Debug.log(message);
					storeImportUserError(rowdata, message, imp_repo);
					continue;
				} else if (!rowdata.getInfoString().contains("@")) {
					String message = "Error al importar al usuario: ["
							+ rowdata.getUsuarioId() + "]" + ". "
							+ "El correo no se ha ingresado correctamente.";
					Debug.log(message);
					storeImportUserError(rowdata, message, imp_repo);
					continue;
				}
				//Se valida que el rol haya sido ingresado
				Debug.log("Grupo: " + rowdata.getGroupId());
				if (rowdata.getGroupId() == null) {
					String message = "Error al importar al usuario: ["
							+ rowdata.getUsuarioId() + "]" + ". "
							+ "El rol no ha sido ingresado.";
					Debug.log(message);
					storeImportUserError(rowdata, message, imp_repo);
					continue;
				}
				//Se valida que se haya ingresado el genero y en caso de que si, que sea F o M
				if (rowdata.getGender() != null  && (rowdata.getGender().equals("M")
						|| rowdata.getGender().equals("F"))) {
					genero = rowdata.getGender();
				} else {
					String message = "Error al importar al usuario: ["
							+ rowdata.getUsuarioId() + "]" + ". "
							+ "El genero debe ser M o F.";
					Debug.log(message);
					storeImportUserError(rowdata, message, imp_repo);
					continue;
				}
				//se valida que la fecha de naciemiento tenga informacion para almacenarla
				if(rowdata.getBirthDate() != null){
					//se cambia a tipo Date las fechas
					birthDate = new Date(rowdata.getBirthDate().getTime());
				}
				Debug.log("Ya valido fecha");
				//Se valida que se haya ingresado el estado civil y en caso de que si,
				//que sea S,C,P,D,V y su es C o V se cambia a M y W respectivamente
				if (rowdata.getMaritalStatus() != null){
					if(rowdata.getMaritalStatus().equals("S")
							|| rowdata.getMaritalStatus().equals("C")
							|| rowdata.getMaritalStatus().equals("P")
							|| rowdata.getMaritalStatus().equals("D")
							|| rowdata.getMaritalStatus().equals("V")) {
						estadoC = rowdata.getMaritalStatus();
						if (estadoC.equals("C")) {
							estadoC = "M";
						} else if (estadoC.equals("V")) {
							estadoC = "W";
						}
					} else {
						String message = "Error al importar al usuario: ["
								+ rowdata.getUsuarioId() + "]" + ". "
								+ "El estado civil debe ser S, C, P, D o V.";
						Debug.log(message);
						storeImportUserError(rowdata, message, imp_repo);
						continue;
					}
				} 
				Debug.log("Ya valido el estado civil");
				//se valida que la fecha de expiracion del pasaporte tenga informacion para almacenarla
				if(rowdata.getPassportExpireDate() != null){
					//se cambia a tipo Date las fechas
					passportExpireDate = new Date(rowdata
							.getPassportExpireDate().getTime());
				}
				Debug.log("Ya valido fecha de pasaporte");
				//Se valida que se haya ingresado el estatus del empleo y a su vez que sea el correcto
				if (rowdata.getEmploymentStatusEnumId() != null
						&& !rowdata.getEmploymentStatusEnumId().equals("")) {
					empleo = obtenTipoEmpleo(delegator,
							rowdata.getEmploymentStatusEnumId(), "EMPLOY_STTS");

					if (empleo == null) {
						String message = "Error al importar al usuario: ["
								+ rowdata.getUsuarioId()
								+ "]"
								+ ". "
								+ "El codigo del estado del empleado debe ser del 01 al 07.";
						Debug.log(message);
						storeImportUserError(rowdata, message, imp_repo);
						continue;
					}
				}
				Debug.log("Ya valido estatus empleado");
				//Se valida que se haya ingresaddo el tipo de casa donde vive y a su vez que sea el correcto
				if (rowdata.getResidenceStatusEnumId() != null
						&& !rowdata.getResidenceStatusEnumId().equals("")) {
					residencia = obtenTipoEmpleo(delegator,
							rowdata.getResidenceStatusEnumId(),
							"PTY_RESID_STTS");

					if (residencia == null) {
						String message = "Error al importar al usuario: ["
								+ rowdata.getUsuarioId()
								+ "]"
								+ ". "
								+ "El codigo de la residencia del empleado debe ser del 01 al 04.";
						Debug.log(message);
						storeImportUserError(rowdata, message, imp_repo);
						continue;
					}
				}
				//Se valida que el tipo de moneda sea correcta
				if (rowdata.getPreferredCurrencyUomId().equals("MXN")
						|| rowdata.getPreferredCurrencyUomId().equals("USD")
						|| rowdata.getPreferredCurrencyUomId().equals("EUR")) {
					moneda = rowdata.getPreferredCurrencyUomId();
				} else {
					String message = "Error al importar al usuario: ["
							+ rowdata.getUsuarioId() + "]" + ". "
							+ "El tipo de moneda debe ser MXN, USD o EUR";
					Debug.log(message);
					storeImportUserError(rowdata, message, imp_repo);
					continue;
				}
				//Se valida el estatus de residencia
				if (rowdata.getStatusId() != null
						&& !rowdata.getStatusId().equals("")) {
					estatus = obtenEstatus(delegator, rowdata.getStatusId(),
							"PARTY_STATUS");
					if (estatus == null) {
						String message = "Error al importar al usuario: ["
								+ rowdata.getUsuarioId()
								+ "]"
								+ ". "
								+ "El codigo de la residencia del empleado debe ser del 01 o 02.";
						Debug.log(message);
						storeImportUserError(rowdata, message, imp_repo);
						continue;
					}
				}
				Debug.log("Ya valido residencia");
				//Se valida que el area ingresada sea correcta
				if (rowdata.getAreaId() != null
						&& !rowdata.getAreaId().equals("")) {
					area = obtenParty(delegator, rowdata.getAreaId());
					
					if (area == null) {
						String message = "Error al importar al usuario: ["
								+ rowdata.getUsuarioId() + "]" + ". "
								+ "El codigo del area no es el indicado.";
						Debug.log(message);
						storeImportUserError(rowdata, message, imp_repo);
						continue;
					}
				}
				//Se valida que el jefe exista
				if (rowdata.getParentPartyId() != null
						&& !rowdata.getParentPartyId().equals("")) {
					parent = obtenParty(delegator, rowdata.getParentPartyId());

					if (parent == null) {
						String message = "Error al importar al usuario: ["
								+ rowdata.getUsuarioId() + "]" + ". "
								+ "El codigo del padre no es el indicado.";
						Debug.log(message);
						storeImportUserError(rowdata, message, imp_repo);
						continue;
					}
				}
				//Se valida que la entidad contable ingresada sea correcta
				if (rowdata.getPartyId() != null
						&& !rowdata.getPartyId().equals("")) {
					entidadContable = obtenParty(delegator, rowdata.getPartyId());
					
					if (entidadContable == null) {
						String message = "Error al importar al usuario: ["
								+ rowdata.getUsuarioId() + "]" + ". "
								+ "El codigo de la entidad contable no es el indicado.";
						Debug.log(message);
						storeImportUserError(rowdata, message, imp_repo);
						continue;
					}
				}
				Debug.log("Ya valido entidad");
				//Se valida que el rol de seguridad exista
				if (rowdata.getGroupId() != null
						&& !rowdata.getGroupId().equals("")) {
					rol = obtenRol(delegator, rowdata.getGroupId());

					if (rol == null) {
						String message = "Error al importar al usuario: ["
								+ rowdata.getUsuarioId() + "]" + ". "
								+ "El codigo del rol de seguridad no existe.";
						Debug.log(message);
						storeImportUserError(rowdata, message, imp_repo);
						continue;
					}
				}
				
				//Se valida que el rol exista
				if (rowdata.getRoleTypeId() != null
						&& !rowdata.getRoleTypeId().equals("")) {
					RoleType partyRole = ledger_repo.findOne(
							RoleType.class,
							ledger_repo.map(RoleType.Fields.roleTypeId,
									rowdata.getRoleTypeId()));

					if (partyRole == null) {
						String message = "Error al importar al usuario: ["
								+ rowdata.getUsuarioId() + "]" + ". "
								+ "El codigo del tipo de rol no existe.";
						Debug.log(message);
						storeImportUserError(rowdata, message, imp_repo);
						continue;
					}
				}
				
				//Se valida que el nivel jerarquico
				if (rowdata.getEmplPositionId() != null
						&& !rowdata.getEmplPositionId().equals("")) {
					EmplPosition emplPosition = ledger_repo.findOne(
							EmplPosition.class,
							ledger_repo.map(EmplPosition.Fields.emplPositionId,
									rowdata.getEmplPositionId()));

					if (emplPosition == null) {
						String message = "Error al importar al usuario: ["
								+ rowdata.getUsuarioId() + "]" + ". "
								+ "El codigo del jerarquia no existe.";
						Debug.log(message);
						storeImportUserError(rowdata, message, imp_repo);
						continue;
					}
				}
				
				boolean useEncryption = "true".equals(UtilProperties.getPropertyValue(
						"security.properties", "password.encrypt"));
				String firmaId = rowdata.getUsuarioId() + rowdata.getUsuarioLogId()
						+ rowdata.getFirstName() + rowdata.getLastName() + genero;
				firmaId = useEncryption ? HashCrypt.getDigestHash(firmaId,
						getHashType()) : firmaId;
				firmaId = firmaId.replaceFirst("SHA", "") ;
				firmaId = firmaId.replace("{}", "");
				
				if(estatus == null){
					estatus = "PARTY_ENABLED";
				}

				Debug.log("va crear usuario: ");
				//Se crea party
				Party personParty = new Party();
				personParty.setPartyId(rowdata.getUsuarioId());
				personParty.setPartyTypeId("PERSON");
				personParty.setPreferredCurrencyUomId(moneda);
				personParty.setDescription(rowdata.getDescription());
				personParty.setStatusId(estatus);
				personParty.setCreatedByUserLogin(obtenUsuario());

				Debug.log("party: " + personParty);
				imp_tx2 = this.session.beginTransaction();
				ledger_repo.createOrUpdate(personParty);
				imp_tx2.commit();

				//se crea person
				Person persona = new Person();
				persona.setPartyId(rowdata.getUsuarioId());
				persona.setFirstName(rowdata.getFirstName());
				persona.setMiddleName(rowdata.getMiddleName());
				persona.setLastName(rowdata.getLastName());
				persona.setPersonalTitle(rowdata.getPersonalTitle());
				persona.setGender(genero);
				persona.setBirthDate(birthDate);
				persona.setMaritalStatus(estadoC);
				persona.setSocialSecurityNumber(rowdata
						.getSocialSecurityNumber());
				persona.setPassportNumber(rowdata.getPassportNumber());
				persona.setPassportExpireDate(passportExpireDate);
				persona.setTotalYearsWorkExperience(rowdata
						.getTotalYearsWorkExperience());
				persona.setComments(rowdata.getComments());
				persona.setEmploymentStatusEnumId(empleo);
				persona.setResidenceStatusEnumId(residencia);
				persona.setOccupation(rowdata.getOccupation());
				persona.setParentPartyId(parent);
				persona.setAreaId(area);
				persona.setEmplPositionId(rowdata.getEmplPositionId());
				persona.setFirmaId(firmaId);

				Debug.log("persona: " + persona);
				imp_tx1 = this.session.beginTransaction();
				ledger_repo.createOrUpdate(persona);
				imp_tx1.commit();

				imp_tx3 = this.session.beginTransaction();
				//Se crea el userLogin del usuario
				String verifica = createUserLogin(delegator, security,
						rowdata.getUsuarioLogId(), rowdata.getUsuarioId(),
						rowdata.getCurrentPassword(),
						rowdata.getVerifyCurrentPassword(), "Y",
						rowdata.getPasswordHint(), "Y", entidadContable);

				//en caso de que haya algun error en la creacion del usuario se dice
				//en el reporte y a su vez se elimina lo que ya se habia almacenado
				if (verifica != null) {
					deleteEntities(delegator, "UserPreference", "userLoginId",
							rowdata.getUsuarioId(), "user_Login_Id");
					deleteEntities(delegator, "UserLoginViewPreference",
							"userLoginId", rowdata.getUsuarioId(),
							"user_Login_Id");
					deleteEntities(delegator, "UserLoginPasswordHistory",
							"userLoginId", rowdata.getUsuarioId(),
							"user_Login_Id");
					deleteEntities(delegator, "UserLoginSecurityGroup",
							"userLoginId", rowdata.getUsuarioId(),
							"user_Login_Id");
					deleteEntities(delegator, "UserLogin", "userLoginId",
							rowdata.getUsuarioId(), "user_Login_Id");
					deleteEntities(delegator, "Person", "partyId",
							rowdata.getUsuarioId(), "party_Id");
					deleteEntities(delegator, "Party", "partyId",
							rowdata.getUsuarioId(), "party_Id");

					storeImportUserError(rowdata, verifica, imp_repo);
					continue;
				}

				//se crea el rol al usuario
				UserLoginSecurityGroup group = new UserLoginSecurityGroup();
				group.setUserLoginId(rowdata.getUsuarioLogId());
				group.setGroupId(rol);
				group.setFromDate(nowTimestamp);

				Debug.log("group: " + group);
				ledger_repo.createOrUpdate(group);
				imp_tx3.commit();

				//se crea el correo del usuario
				ContactMech contact = new ContactMech();
				contact.setContactMechId(rowdata.getUsuarioId());
				contact.setContactMechTypeId("EMAIL_ADDRESS");
				contact.setInfoString(rowdata.getInfoString());

				Debug.log("contact: " + contact);
				imp_tx3 = this.session.beginTransaction();
				ledger_repo.createOrUpdate(contact);
				imp_tx3.commit();

				//se crea el link del correo con la party
				PartyContactMech partyContact = new PartyContactMech();
				partyContact.setPartyId(rowdata.getUsuarioId());
				partyContact.setContactMechId(rowdata.getUsuarioId());
				partyContact.setFromDate(nowTimestamp);
				partyContact.setAllowSolicitation("Y");

				Debug.log("partyContact: " + partyContact);
				imp_tx3 = this.session.beginTransaction();
				ledger_repo.createOrUpdate(partyContact);
				imp_tx3.commit();

				//Se crea el proposito  del correo para facturacion
				PartyContactMechPurpose partyContactPurpose = new PartyContactMechPurpose();
				partyContactPurpose.setPartyId(rowdata.getUsuarioId());
				partyContactPurpose.setContactMechId(rowdata.getUsuarioId());
				partyContactPurpose
						.setContactMechPurposeTypeId("BILLING_EMAIL");
				partyContactPurpose.setFromDate(nowTimestamp);

				Debug.log("partyContactPurpose: " + partyContactPurpose);
				imp_tx3 = this.session.beginTransaction();
				ledger_repo.createOrUpdate(partyContactPurpose);
				imp_tx3.commit();

				//Se crea el proposito  del correo para pedidos
				partyContactPurpose.setContactMechPurposeTypeId("ORDER_EMAIL");
				Debug.log("partyContactPurpose2: " + partyContactPurpose);
				imp_tx3 = this.session.beginTransaction();
				ledger_repo.createOrUpdate(partyContactPurpose);
				imp_tx3.commit();

				//Se crea el proposito  del correo para pagos
				partyContactPurpose
						.setContactMechPurposeTypeId("PAYMENT_EMAIL");
				Debug.log("partyContactPurpose3: " + partyContactPurpose);
				imp_tx3 = this.session.beginTransaction();
				ledger_repo.createOrUpdate(partyContactPurpose);
				imp_tx3.commit();

				//Se crea el proposito  del correo principal
				partyContactPurpose
						.setContactMechPurposeTypeId("PRIMARY_EMAIL");
				Debug.log("partyContactPurpose4: " + partyContactPurpose);
				imp_tx3 = this.session.beginTransaction();
				ledger_repo.createOrUpdate(partyContactPurpose);
				imp_tx3.commit();
				
				//Se crean los roles que tendra el usuario
				PartyRole partyRole = new PartyRole();
				partyRole.setPartyId(rowdata.getUsuarioId());
				partyRole.setRoleTypeId("SOLICITANTE");

				Debug.log("partyRole: " + partyRole);
				imp_tx3 = this.session.beginTransaction();
				ledger_repo.createOrUpdate(partyRole);
				imp_tx3.commit();
				
				//Se crea el rol empleado
				partyRole.setRoleTypeId("EMPLEADO");
				Debug.log("partyRole2: " + partyRole);
				imp_tx3 = this.session.beginTransaction();
				ledger_repo.createOrUpdate(partyRole);
				imp_tx3.commit();
				
				//Se crea el rol ingresado por el usuario
				if(rowdata.getRoleTypeId() != null
						&& !rowdata.getRoleTypeId().equals("")){ 
					partyRole.setRoleTypeId(rowdata.getRoleTypeId());
					imp_tx3 = this.session.beginTransaction();
					ledger_repo.createOrUpdate(partyRole);
					imp_tx3.commit();
				}

				storeImportUserSuccess(rowdata, imp_repo);
				imported = imported + 1;
			}
		} catch (Exception ex) {

		}
	}

	/**
	 * Helper method to store User import success into
	 * <code>DataImportUser</code> entity row.
	 * 
	 * @param rowdata
	 *            item of <code>DataImportUser</code> entity that was
	 *            successfully imported
	 * @param imp_repo
	 *            repository of accounting
	 * @throws org.opentaps.foundation.repository.RepositoryException
	 */
	private void storeImportUserSuccess(DataImportUser rowdata,
			UserDataImportRepositoryInterface imp_repo)
			throws RepositoryException {
		// mark as success
		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_IMPORTED);
		rowdata.setImportError(null);
		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
		imp_repo.createOrUpdate(rowdata);
	}

	/**
	 * Helper method to store User import error into <code>DataImportUser</code>
	 * entity row.
	 * 
	 * @param rowdata
	 *            item of <code>DataImportUser</code> entity that was
	 *            unsuccessfully imported
	 * @param message
	 *            error message
	 * @param imp_repo
	 *            repository of accounting
	 * @throws org.opentaps.foundation.repository.RepositoryException
	 */
	private static void storeImportUserError(DataImportUser rowdata,
			String message, UserDataImportRepositoryInterface imp_repo)
			throws RepositoryException {
		// store the exception and mark as failed
		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_FAILED);
		rowdata.setImportError(message);
		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
		Debug.log("Error user: "+ rowdata);
		imp_repo.createOrUpdate(rowdata);
	}

	/*
	 * Obtiene usuario
	 */
	public static String obtenUsuario() throws InfrastructureException {
		Debug.log("ingresa a obtener usuario");
		Debug.log("Usuario o: " + user.getUserId());
		return user.getOfbizUserLogin().getString("userLoginId");
	}

	/*
	 * Se va creando el historial del usuario que se ha ingresado
	 */
	@SuppressWarnings("unused")
	private static void createUserLoginPasswordHistory(Delegator delegator,
			String userLoginId, String currentPassword)
			throws GenericEntityException, RepositoryException {
		int passwordChangeHistoryLimit = 0;
		try {
			Debug.log("Crear History");
			passwordChangeHistoryLimit = Integer.parseInt(UtilProperties
					.getPropertyValue("security.properties",
							"password.change.history.limit", "0"));
		} catch (NumberFormatException nfe) {
			// No valid value is found so don't bother to save any password
			// history
			passwordChangeHistoryLimit = 0;
		}
		
		// save this password in history
		Timestamp nowTimestamp = UtilDateTime.nowTimestamp();
		GenericValue userLoginPwdHistToCreate = delegator.makeValue(
				"UserLoginPasswordHistory", UtilMisc.toMap("userLoginId",
						userLoginId, "fromDate", nowTimestamp));
		boolean useEncryption = "true".equals(UtilProperties.getPropertyValue(
				"security.properties", "password.encrypt"));
		userLoginPwdHistToCreate.set(
				"currentPassword",
				useEncryption ? HashCrypt.getDigestHash(currentPassword,
						getHashType()) : currentPassword);
		userLoginPwdHistToCreate.create();

	}

	/**
	 * Creates a UserLogin
	 * 
	 * @param ctx
	 *            The DispatchContext that this service is operating in
	 * @param context
	 *            Map containing the input parameters
	 * @return Map with the result of the service, the output parameters
	 * @throws RepositoryException
	 */
	public static String createUserLogin(Delegator delegator,
			Security security, String userLoginId, String partyId,
			String currentPassword, String currentPasswordVerify,
			String enabled, String passwordHint, String requirePasswordChange,
			String entidadContable) throws RepositoryException {

		GenericValue loggedInUserLogin = user.getOfbizUserLogin();
		List<String> errorMessageList = FastList.newInstance();
		Locale locale = Locale.getDefault();

		boolean useEncryption = "true".equals(UtilProperties.getPropertyValue(
				"security.properties", "password.encrypt"));

		String errMsg = null;

		// security: don't create a user login if the specified partyId (if not
		// empty) already exists
		// unless the logged in user has permission to do so (same partyId or
		// PARTYMGR_CREATE)
		if (UtilValidate.isNotEmpty(partyId)) {
			GenericValue party = null;

			try {
				party = delegator.findOne("Party", false, "partyId", partyId);
			} catch (GenericEntityException e) {
				Debug.logWarning(e, "", MODULE);
			}

			if (party != null) {
				if (loggedInUserLogin != null) {
					// <b>security check</b>: userLogin partyId must equal
					// partyId, or must have PARTYMGR_CREATE permission
					if (!partyId.equals(loggedInUserLogin.getString("partyId"))) {
						if (!security.hasEntityPermission("PARTYMGR",
								"_CREATE", loggedInUserLogin)) {

							errMsg = UtilProperties
									.getMessage(
											resource,
											"loginservices.party_with_specified_party_ID_exists_not_have_permission",
											locale);
							errorMessageList.add(errMsg);
							String message = "Error al generar la contrase\u00f1a al usuario: ["
									+ userLoginId
									+ "]"
									+ ". "
									+ "El usuario que lo esta creando no tiene permisos.";
							Debug.log(message);
							return message;
						}
					}
				} else {
					errMsg = UtilProperties
							.getMessage(
									resource,
									"loginservices.must_be_logged_in_and_permission_create_login_party_ID_exists",
									locale);
					errorMessageList.add(errMsg);
					String message = "Error al generar la contrase\u00f1a al usuario: ["
							+ userLoginId
							+ "]"
							+ ". "
							+ "El usuario que lo esta creando no esta conectado en el sistema.";
					Debug.log(message);
					return message;
				}
			}
		}

		String verifica = checkNewPassword(null, null, currentPassword,
				currentPasswordVerify, passwordHint, errorMessageList, true,
				locale);

		if (verifica != null) {
			return verifica;
		}

		GenericValue userLoginToCreate = delegator.makeValue("UserLogin",
				UtilMisc.toMap("userLoginId", userLoginId));
		userLoginToCreate.set("passwordHint", passwordHint);
		userLoginToCreate.set("enabled", enabled);
		userLoginToCreate.set("requirePasswordChange", requirePasswordChange);
		userLoginToCreate.set("partyId", partyId);
		userLoginToCreate.set("lastLocale", "es_MX");
		userLoginToCreate.set(
				"currentPassword",
				useEncryption ? HashCrypt.getDigestHash(currentPassword,
						getHashType()) : currentPassword);

		try {
			EntityCondition condition = EntityCondition.makeCondition(
					EntityFunction.UPPER_FIELD("userLoginId"),
					EntityOperator.EQUALS, EntityFunction.UPPER(userLoginId));
			if (UtilValidate.isNotEmpty(delegator.findList("UserLogin",
					condition, null, null, null, false))) {
				Map<String, String> messageMap = UtilMisc.toMap("userLoginId",
						userLoginId);
				errMsg = UtilProperties
						.getMessage(
								resource,
								"loginservices.could_not_create_login_user_with_ID_exists",
								messageMap, locale);
				errorMessageList.add(errMsg);
				String message = "Error al generar la contrase\u00f1a al usuario: ["
						+ userLoginId
						+ "]"
						+ ". "
						+ "No se puede generar este usuario porquecon el identificador que se proporciono.";
				Debug.log(message);
				return message;
			}
		} catch (GenericEntityException e) {
			Debug.logWarning(e, "", MODULE);
			Map<String, String> messageMap = UtilMisc.toMap("errorMessage",
					e.getMessage());
			errMsg = UtilProperties.getMessage(resource,
					"loginservices.could_not_create_login_user_read_failure",
					messageMap, locale);
			errorMessageList.add(errMsg);
			String message = "Error al generar la contrase\u00f1a al usuario: ["
					+ userLoginId + "]" + ". "
					+ "No se ha permitido crear al usuario.";
			Debug.log(message);
			return message;
		}

		try {
			userLoginToCreate.create();
			createUserLoginPasswordHistory(delegator, userLoginId,
					currentPassword);
		} catch (GenericEntityException e) {
			Debug.logWarning(e, "", MODULE);
			Map<String, String> messageMap = UtilMisc.toMap("errorMessage",
					e.getMessage());
			errMsg = UtilProperties.getMessage(resource,
					"loginservices.could_not_create_login_user_write_failure",
					messageMap, locale);
			String message = "Error al generar la contrase\u00f1a al usuario: ["
					+ userLoginId + "]" + ". "
					+ "No se puede generar este usuario porque ya existe.";
			Debug.log(message);
			return message;
		}

		// Se generan los inserts para las preferencias del userLoginId creado
		// en USER_LOGIN_VIEW_PREFERENCE y USER_PREFERENCE
		try {
			String entidad = null;
			if (entidadContable != null) {
				entidad = obtenParty(delegator, entidadContable);
			}
			// USER_LOGIN_VIEW_PREFERENCE
			GenericValue userLoginViewPref = GenericValue.create(delegator
					.getModelEntity("UserLoginViewPreference"));
			userLoginViewPref.set("userLoginId", userLoginId);
			userLoginViewPref.set("applicationName", "opentaps");
			userLoginViewPref.set("screenName", "selectOrganizationForm");
			userLoginViewPref.set("preferenceName", "organizationPartyId");
			userLoginViewPref.set("preferenceValue", entidad);
			delegator.createOrStore(userLoginViewPref);
			
			userLoginViewPref.set("applicationName", "financials");
			userLoginViewPref.set("screenName", "viewInvoice");
			userLoginViewPref.set("preferenceName", "useGwt");
			userLoginViewPref.set("preferenceValue", "N");
			delegator.createOrStore(userLoginViewPref);
			
			userLoginViewPref.set("applicationName", "purchasing");
			userLoginViewPref.set("screenName", "cartItemsEntry");
			userLoginViewPref.set("preferenceName", "useGwt");
			userLoginViewPref.set("preferenceValue", "N");
			delegator.createOrStore(userLoginViewPref);

			// USER_PREFERENCE
			// Organizacion
			GenericValue userPref = GenericValue.create(delegator
					.getModelEntity("UserPreference"));
			userPref.set("userLoginId", userLoginId);
			userPref.set("userPrefTypeId", "ORGANIZATION_PARTY");
			userPref.set("userPrefGroupTypeId", "GLOBAL_PREFERENCES");
			userPref.set("userPrefValue", entidad);
			userPref.set("userPrefDataType", null);
			delegator.createOrStore(userPref);
			// Tema Visual
			userPref.set("userPrefTypeId", "VISUAL_THEME");
			userPref.set("userPrefValue", "FLAT_GREY");
			delegator.createOrStore(userPref);

		} catch (GenericEntityException e) {
			Debug.logWarning(e, "", MODULE);
			Map<String, String> messageMap = UtilMisc.toMap("errorMessage",
					e.getMessage());
			errMsg = UtilProperties.getMessage(resource,
					"loginservices.could_not_create_login_user_write_failure",
					messageMap, locale);
			String message = "Error al generar la contrase\u00f1a al usuario: ["
					+ userLoginId + "]" + ". "
					+ "No se puede generar este usuario para sus permisos.";
			Debug.log(message);
			return message;
		}

		return null;
	}

	public static String checkNewPassword(GenericValue userLogin,
			String currentPassword, String newPassword,
			String newPasswordVerify, String passwordHint,
			List<String> errorMessageList, boolean ignoreCurrentPassword,
			Locale locale) {
		boolean useEncryption = "true".equals(UtilProperties.getPropertyValue(
				"security.properties", "password.encrypt"));

		String errMsg = null;

		Debug.log("Ingresa a checar el nuevo password");

		if (!ignoreCurrentPassword) {

			String encodedPassword = useEncryption ? HashCrypt.getDigestHash(
					currentPassword, getHashType()) : currentPassword;
			String encodedPasswordOldFunnyHexEncode = useEncryption ? HashCrypt
					.getDigestHashOldFunnyHexEncode(currentPassword,
							getHashType()) : currentPassword;
			String encodedPasswordUsingDbHashType = encodedPassword;

			String oldPassword = userLogin.getString("currentPassword");
			if (useEncryption && oldPassword != null
					&& oldPassword.startsWith("{")) {
				// get encode according to the type in the database
				String dbHashType = HashCrypt
						.getHashTypeFromPrefix(oldPassword);
				if (dbHashType != null) {
					encodedPasswordUsingDbHashType = HashCrypt.getDigestHash(
							currentPassword, dbHashType);
				}
			}

			// if the password.accept.encrypted.and.plain property in security
			// is set to true allow plain or encrypted passwords
			// if this is a system account don't bother checking the passwords
			boolean passwordMatches = (oldPassword != null && (HashCrypt
					.removeHashTypePrefix(encodedPassword).equals(
							HashCrypt.removeHashTypePrefix(oldPassword))
					|| HashCrypt.removeHashTypePrefix(
							encodedPasswordOldFunnyHexEncode).equals(
							HashCrypt.removeHashTypePrefix(oldPassword))
					|| HashCrypt.removeHashTypePrefix(
							encodedPasswordUsingDbHashType).equals(
							HashCrypt.removeHashTypePrefix(oldPassword)) || ("true"
					.equals(UtilProperties.getPropertyValue(
							"security.properties",
							"password.accept.encrypted.and.plain")) && currentPassword
					.equals(oldPassword))));

			if ((currentPassword == null)
					|| (userLogin != null && currentPassword != null && !passwordMatches)) {
				errMsg = UtilProperties.getMessage(resource,
						"loginservices.old_password_not_correct_reenter",
						locale);
				errorMessageList.add(errMsg);
			}
			if (currentPassword.equals(newPassword)
					|| encodedPassword.equals(newPassword)) {
				errMsg = UtilProperties.getMessage(resource,
						"loginservices.new_password_is_equal_to_old_password",
						locale);
				errorMessageList.add(errMsg);
			}

		}

		if (!UtilValidate.isNotEmpty(newPassword)
				|| !UtilValidate.isNotEmpty(newPasswordVerify)) {
			Debug.log("Miguel - password8");
			errMsg = UtilProperties.getMessage(resource,
					"loginservices.password_or_verify_missing", locale);
			errorMessageList.add(errMsg);
			String message = "Error al generar la contrase\u00f1a al usuario" + ". "
					+ "Las contrase\u00f1as ingresadas no coinciden.";
			Debug.log(message);
			return message;
		} else if (!newPassword.equals(newPasswordVerify)) {
			Debug.log("Miguel - password9");
			errMsg = UtilProperties.getMessage(resource,
					"loginservices.password_did_not_match_verify_password",
					locale);
			errorMessageList.add(errMsg);
			String message = "Error al generar la contrase\u00f1a al usuario" + ". "
					+ "Las contrase\u00f1as ingresadas no coinciden.";
			Debug.log("Miguel - password10");
			Debug.log(message);
			return message;
		}

		int passwordChangeHistoryLimit = 0;
		try {
			Debug.log("Miguel - password");
			passwordChangeHistoryLimit = Integer.parseInt(UtilProperties
					.getPropertyValue("security.properties",
							"password.change.history.limit", "0"));
		} catch (NumberFormatException nfe) {
			// No valid value is found so don't bother to save any password
			// history
			passwordChangeHistoryLimit = 0;
		}
		Debug.logInfo(" password.change.history.limit is set to "
				+ passwordChangeHistoryLimit, MODULE);
		Debug.logInfo(" userLogin is set to " + userLogin, MODULE);
		if (passwordChangeHistoryLimit > 0 && userLogin != null) {
			Debug.logInfo(
					" checkNewPassword Checking if user is tyring to use old password "
							+ passwordChangeHistoryLimit, MODULE);
			Delegator delegator = userLogin.getDelegator();
			String newPasswordHash = newPassword;
			Debug.log("Miguel - password2");
			if (useEncryption) {
				newPasswordHash = HashCrypt.getDigestHash(newPassword,
						getHashType());
			}
			try {
				Debug.log("Miguel - password3");
				List<GenericValue> pwdHistList = delegator.findByAnd(
						"UserLoginPasswordHistory", UtilMisc.toMap(
								"userLoginId",
								userLogin.getString("userLoginId"),
								"currentPassword", newPasswordHash));
				Debug.logInfo(" checkNewPassword pwdHistListpwdHistList "
						+ pwdHistList.size(), MODULE);
				if (pwdHistList.size() > 0) {
					Map<String, Integer> messageMap = UtilMisc.toMap(
							"passwordChangeHistoryLimit",
							passwordChangeHistoryLimit);
					errMsg = UtilProperties
							.getMessage(
									resource,
									"loginservices.password_must_be_different_from_last_passwords",
									messageMap, locale);
					errorMessageList.add(errMsg);
					Debug.logInfo(
							" checkNewPassword errorMessageListerrorMessageList "
									+ pwdHistList.size(), MODULE);
				}
			} catch (GenericEntityException e) {
				Debug.logWarning(e, "", MODULE);
				Map<String, String> messageMap = UtilMisc.toMap("errorMessage",
						e.getMessage());
				errMsg = UtilProperties.getMessage(resource,
						"loginevents.error_accessing_password_change_history",
						messageMap, locale);
			}

		}

		int minPasswordLength = 0;

		try {
			Debug.log("Miguel - password4");
			minPasswordLength = Integer.parseInt(UtilProperties
					.getPropertyValue("security.properties",
							"password.length.min", "0"));
		} catch (NumberFormatException nfe) {
			minPasswordLength = 0;
		}

		if (newPassword != null) {
			if (!(newPassword.length() >= minPasswordLength)) {
				Map<String, String> messageMap = UtilMisc.toMap(
						"minPasswordLength",
						Integer.toString(minPasswordLength));
				errMsg = UtilProperties.getMessage(resource,
						"loginservices.password_must_be_least_characters_long",
						messageMap, locale);
				errorMessageList.add(errMsg);
				String message = "Error al generar la contrase\u00f1a al usuario"
						+ ". " + "La contrase\u00f1a debe tener menos caracteres.";
				Debug.log(message);
				return message;
			}
			if (userLogin != null
					&& newPassword.equalsIgnoreCase(userLogin
							.getString("userLoginId"))) {
				errMsg = UtilProperties
						.getMessage(
								resource,
								"loginservices.password_may_not_equal_username",
								locale);
				errorMessageList.add(errMsg);
				String message = "Error al generar la contrase\u00f1a al usuario"
						+ ". " + "La contrase\u00f1a no debe ser igual al usuario.";
				Debug.log(message);
				return message;
			}
			if (UtilValidate.isNotEmpty(passwordHint)
					&& (passwordHint.toUpperCase().indexOf(
							newPassword.toUpperCase()) >= 0)) {
				errMsg = UtilProperties.getMessage(resource,
						"loginservices.password_hint_may_not_contain_password",
						locale);
				errorMessageList.add(errMsg);
				String message = "Error al generar la contrase\u00f1a al usuario"
						+ ". " + "La pista no puede contener la contrase\u00f1a.";
				Debug.log(message);
				return message;
			}
		}

		return null;
	}

	public static String getHashType() {
		String hashType = UtilProperties.getPropertyValue(
				"security.properties", "password.encrypt.hash.type");

		if (UtilValidate.isEmpty(hashType)) {
			Debug.logWarning(
					"Password encrypt hash type is not specified in security.properties, use SHA",
					MODULE);
			hashType = "SHA";
		}

		return hashType;
	}

	/*
	 * Metodo para verificar los deferentes tipos de empleos del usuario
	 */
	public static String obtenTipoEmpleo(Delegator delegator, String tipo,
			String codigo) throws GenericEntityException {
		Debug.log("Tipo de codigo: " + tipo);

		String empleado = null;
		tipo = "0" + tipo;

		EntityCondition conditions1 = EntityCondition.makeCondition(
				EntityOperator.AND, EntityCondition.makeCondition("sequenceId",
						EntityOperator.EQUALS, tipo), EntityCondition
						.makeCondition("enumTypeId", EntityOperator.EQUALS,
								codigo));

		List<GenericValue> tipos = delegator.findByCondition("Enumeration",
				conditions1, UtilMisc.toList("enumId"),
				UtilMisc.toList("enumId DESC"));

		if (!tipos.isEmpty()) {
			Iterator<GenericValue> enumId = tipos.iterator();
			GenericValue enumeration = enumId.next();
			empleado = enumeration.getString("enumId");
		}

		Debug.log("Empleados: " + empleado);
		return empleado;
	}

	/*
	 * Metodo para verificar que existe el party
	 */
	public static String obtenParty(Delegator delegator, String party) throws GenericEntityException{

		Debug.log("El area q se ingresa es: " + party);
		
		String part = null;

		EntityCondition conditions1 = EntityCondition.makeCondition(
				EntityOperator.AND, EntityCondition.makeCondition("partyId",
						EntityOperator.EQUALS, party));

		Debug.log("Condicion: " + conditions1);

		List<GenericValue> parties = delegator.findByCondition("Party",
				conditions1, UtilMisc.toList("partyId"),
				UtilMisc.toList("partyId DESC"));

		if (!parties.isEmpty()) {
			Iterator<GenericValue> enumId = parties.iterator();
			GenericValue partiesId = enumId.next();
			part = partiesId.getString("partyId");
		}

		Debug.log("El area q se ingresa es: " + part);
		return part;
	}

	/*
	 * Metodo para verificar el estatus del usuario
	 */
	public static String obtenEstatus(Delegator delegator, String tipo,
			String codigo) throws GenericEntityException {
		Debug.log("Tipo de estatus: " + tipo);

		String empleado = null;
		if (tipo.equals("2")) {
			tipo = "99";
		} else {
			tipo = "0" + tipo;
		}

		EntityCondition conditions1 = EntityCondition.makeCondition(
				EntityOperator.AND, EntityCondition.makeCondition("sequenceId",
						EntityOperator.EQUALS, tipo), EntityCondition
						.makeCondition("statusTypeId", EntityOperator.EQUALS,
								codigo));

		Debug.log("Condicion: " + conditions1);

		List<GenericValue> tipos = delegator.findByCondition("StatusItem",
				conditions1, UtilMisc.toList("statusId"),
				UtilMisc.toList("statusId DESC"));

		if (!tipos.isEmpty()) {
			Iterator<GenericValue> enumId = tipos.iterator();
			GenericValue enumeration = enumId.next();
			empleado = enumeration.getString("statusId");
		}

		Debug.log("Resultado del tipo: " + empleado);
		return empleado;
	}

	/*
	 * Metodo para verificar el rol
	 */
	public static String obtenRol(Delegator delegator, String rol)
			throws GenericEntityException {

		String sec = null;
		
		EntityCondition conditions1 = EntityCondition.makeCondition(
				EntityOperator.AND, EntityCondition.makeCondition("groupId",
						EntityOperator.EQUALS, rol));

		Debug.log("Condicion: " + conditions1);

		List<GenericValue> parties = delegator.findByCondition("SecurityGroup",
				conditions1, UtilMisc.toList("groupId"),
				UtilMisc.toList("groupId DESC"));

		if (!parties.isEmpty()) {
			Iterator<GenericValue> enumId = parties.iterator();
			GenericValue partiesId = enumId.next();
			sec = partiesId.getString("groupId");
		}

		Debug.log("Seguridad: " + sec);
		return sec;
	}

	/*
	 * Delete Entities
	 */
	private void deleteEntities(Delegator delegator, String entity,
			String campoId, String condicion, String campoIdS)
			throws InfrastructureException, GenericEntityException,
			GenericModelException {

		// this.session = this.getInfrastructure().getSession();
		Transaction tx = null;
		List<GenericValue> busqueda = null;

		try {

			EntityCondition conditions1 = EntityCondition.makeCondition(
					EntityOperator.AND, EntityCondition.makeCondition(campoId,
							EntityOperator.EQUALS, condicion));

			busqueda = delegator.findByCondition(entity, conditions1,
					UtilMisc.toList(campoId), null);

			Debug.log("busqueda: " + busqueda);

			if (busqueda != null && !busqueda.isEmpty()) {
				Debug.log("Borra: " + entity);
				tx = this.session.beginTransaction();
				Query query = session.createQuery("delete from " + entity
						+ " where " + campoIdS + " = '" + condicion + "'");
				query.executeUpdate();
				tx.commit();
			}

		} catch (Exception e) {
			Debug.log("Error al borrar registro " + e);
			if (tx != null)
				tx.rollback();
		}
	}
}
