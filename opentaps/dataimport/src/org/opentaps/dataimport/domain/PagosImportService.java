package org.opentaps.dataimport.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.LocalDispatcher;
import org.opentaps.base.constants.StatusItemConstants;
import org.opentaps.base.entities.DataImportPayment;
import org.opentaps.base.entities.Payment;
import org.opentaps.base.entities.PaymentApplication;
import org.opentaps.base.entities.UserLogin;
import org.opentaps.dataimport.domain.PagosImportService;
import org.opentaps.domain.DomainService;
import org.opentaps.domain.dataimport.PagosDataImportRepositoryInterface;
import org.opentaps.domain.dataimport.PagosImportServiceInterface;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.entity.hibernate.Session;
import org.opentaps.foundation.entity.hibernate.Transaction;
import org.opentaps.foundation.infrastructure.Infrastructure;
import org.opentaps.foundation.infrastructure.User;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.service.ServiceException;

public class PagosImportService extends DomainService implements
	PagosImportServiceInterface {	
	
	private static final String MODULE = PagosImportService.class.getName();
	// session object, using to store/search pojos.
	private Session session;
	public int importedRecords;
	static Logger logger = Logger.getLogger(PagosImportService.class);

	public PagosImportService() {
		super();
		Debug.log("Hola1");
	}

	public PagosImportService(Infrastructure infrastructure, User user,
			Locale locale) throws ServiceException {
		super(infrastructure, user, locale);
		Debug.log("Hola2");
	}

	/** {@inheritDoc} */
	public int getImportedRecords() {
		Debug.log("Hola3");
		return importedRecords;
	}
	
	/** {@inheritDoc} */
	
	@SuppressWarnings("unused")
	public void importPagos() throws ServiceException  {
	Debug.log("Hola4");
	try 
	{	this.session = this.getInfrastructure().getSession();
		Debug.log("Omar - Obtiene la sesion");	
		PagosDataImportRepositoryInterface imp_repo = this
				.getDomainsDirectory().getDataImportDomain()
				.getPagosDataImportRepository();		
		Debug.log("Omar - imp_repo: " + imp_repo);
		LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
				.getLedgerDomain().getLedgerRepository();
		Debug.log("Omar - ledger_repo: " + ledger_repo);
		
		LocalDispatcher dispatcher = this.getDomainsDirectory().getLedgerDomain().
				getInfrastructure().getDispatcher();
		Delegator delegator = dispatcher.getDelegator();	
		GenericValue userLogin = this.getDomainsDirectory()
				.getLedgerDomain().getInfrastructure().getSystemUserLogin();
		String userLoginId = this.getDomainsDirectory()
				.getLedgerDomain().getLedgerRepository().getUser().getOfbizUserLogin().getString("userLoginId");
		Debug.log("Omar-userLoginId: " + userLoginId);
		List<DataImportPayment> dataforimp = imp_repo
				.findNotProcessesDataImportPagos(userLoginId);
		
        
		Debug.log("Omar - dataforimp: " + dataforimp);
		Debug.log("Omar - dataforimp.size(): " + dataforimp.size());
		int imported = 0;
		Transaction imp_tx1 = null;
		
		
		
		
		Payment payment = null;
		PaymentApplication paymentApp = null;
		
		DataImportPayment rawdata = null;
		String idPago = "";
		String idPaymentApplication = "";
		
		
		String messageError = "";

			
		
		for (int i = 0; i < dataforimp.size(); i++) {
			rawdata = dataforimp.get(i);
			Debug.log("Omar - rawdata: " + rawdata);
			try 
			{	if(rawdata.getIdFactura()!=null)				
				{	if(rawdata.getTipoPago()!=null)
					{	if(rawdata.getPartyIdFrom()!=null)
						{	if(rawdata.getPartyIdTo()!=null)
							{	if(rawdata.getFechaContable()!=null)
								{	if(rawdata.getReferenciaPago()!=null)
									{	if(rawdata.getMoneda()!=null)
										{	if(rawdata.getTipoDocumento()!=null)
											{	payment = new Payment();
												idPago = ledger_repo.getNextSeqId("Payment");												
												payment.setAmount(rawdata.getMontoAplicado());																										
												payment.setPaymentId(idPago);																									
												payment.setPartyIdFrom(rawdata.getPartyIdFrom());
												payment.setPartyIdTo(rawdata.getPartyIdTo());
												payment.setStatusId("PMNT_NOT_PAID");
												payment.setEffectiveDate(rawdata.getFechaContable());
												payment.setPaymentRefNum(rawdata.getReferenciaPago());
												payment.setCurrencyUomId(rawdata.getMoneda());
												payment.setComments(rawdata.getDescripcionPago());
												payment.setAcctgTransTypeId(rawdata.getTipoDocumento());
												payment.setBancoId(rawdata.getBanco());
												payment.setCuentaBancariaId(rawdata.getCuentaBancaria());
												payment.setPaymentTypeId(rawdata.getTipoPago());
												Debug.log("Crea un pago");
												imp_tx1 = this.session.beginTransaction();
												ledger_repo.createOrUpdate(payment);
												imp_tx1.commit();
											}
											else
											{	messageError = "Falta ingresar el Tipo de Documento";
												storeImportPagosError(rawdata, messageError, imp_repo);												
											}
										}
										else
										{	messageError = "Falta ingresar la Moneda";
											storeImportPagosError(rawdata, messageError, imp_repo);											
										}
									}
									else	
									{	messageError = "Falta ingresar el N\u00famero de referencia del pago";
										storeImportPagosError(rawdata, messageError, imp_repo);										
									}
								}
								else
								{	messageError = "Falta ingresar la Fecha Contable";
									storeImportPagosError(rawdata, messageError, imp_repo);																	
								}
							}
							else
							{	messageError = "Falta ingresar la Organizaci\u00f3n Destino";
								storeImportPagosError(rawdata, messageError, imp_repo);								
							}
						}
						else
						{	messageError = "Falta ingresar la Entidad Contable";
							storeImportPagosError(rawdata, messageError, imp_repo);						
						}
					}
					else
					{	messageError = "Falta ingresar el Tipo de Pago";
						storeImportPagosError(rawdata, messageError, imp_repo);						
					}
				}
				else
				{	messageError = "Falta ingresar el identificador de la Orden de Pago";
					storeImportPagosError(rawdata, messageError, imp_repo);						
				}
			
			Debug.log("Va a aplicacion de pago");
				///APLICACION DE PAGO
				
				paymentApp = new PaymentApplication();		
				Debug.log("Crea applicaction");
				idPaymentApplication = ledger_repo.getNextSeqId("PaymentApplication");
				Debug.log("idPaymentApplication: " + idPaymentApplication);
				Debug.log("idPago: " + idPago);
				Debug.log("rawdata.getIdFactura: " + rawdata.getIdFactura().trim());
				Debug.log("rawdata.getMontoAplicado: " + rawdata.getMontoAplicado());
				paymentApp.setPaymentApplicationId(idPaymentApplication);
				paymentApp.setPaymentId(idPago);
				paymentApp.setInvoiceId(rawdata.getIdFactura().trim());
				paymentApp.setAmountApplied(rawdata.getMontoAplicado());
				imp_tx1 = this.session.beginTransaction();
				ledger_repo.createOrUpdate(paymentApp);
				imp_tx1.commit();
				Debug.log("Hace commit de payment");
							
				Map<String, Object> results;
				Debug.log("Va a setPaymentStatus");
				results = dispatcher.runSync("setPaymentStatus", UtilMisc.toMap("userLogin", userLogin, "paymentId", idPago, "statusId", "PMNT_SENT"));
				Debug.log("results:" + results);
						
				String message = "Se import\u00f3 correctamente la aplicacion de los pagos";
				this.storeImportPagosSuccess(rawdata, imp_repo);
				imported = imported + 1;			
				this.importedRecords = imported;
				
				Debug.logInfo(message, MODULE);

				
				
				
				
				
			
			}
			catch (Exception ex) 
			{	String idErr = ex.getMessage();
				if(messageError=="")
				{	messageError = "Error al importar los pagos. C\u00f3digo de error: ["
						         + idErr + "].";
				}
				storeImportPagosError(rawdata, messageError, imp_repo);
				if (imp_tx1 != null) {
					imp_tx1.rollback();
				}				
				Debug.logError(ex, messageError, MODULE);
				throw new ServiceException(ex.getMessage());
			} 			
		}
	}
	catch (Exception ex) 
	  {	String idErr = ex.getMessage();
		
		
		throw new ServiceException(ex.getMessage());
	 }
	finally 
	{	if (session != null) 
		{	session.close();
		}
	}
	}



	/*public void importPagos() throws ServiceException  {
		Debug.log("Hola4");
		try 
		{	this.session = this.getInfrastructure().getSession();
			Debug.log("Omar - Obtiene la sesion");	
			PagosDataImportRepositoryInterface imp_repo = this
					.getDomainsDirectory().getDataImportDomain()
					.getPagosDataImportRepository();
			Debug.log("Omar - imp_repo: " + imp_repo);
			LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
					.getLedgerDomain().getLedgerRepository();
			Debug.log("Omar - ledger_repo: " + ledger_repo);
			List<DataImportPayment> dataforimp = imp_repo
					.findNotProcessesDataImportPagos();
			LocalDispatcher dispatcher = this.getDomainsDirectory().getLedgerDomain().
					getInfrastructure().getDispatcher();
			Delegator delegator = dispatcher.getDelegator();	
			GenericValue userLogin = this.getDomainsDirectory().getLedgerDomain().
					getInfrastructure().getSystemUserLogin();
			
	        Locale locale = new Locale("Payment");
			Debug.log("Omar - dataforimp: " + dataforimp);
			Debug.log("Omar - dataforimp.size(): " + dataforimp.size());
			int imported = 0;
			Transaction imp_tx1 = null;
			Transaction imp_tx2 = null;
			String referenciaPago = "";
			String referenciaPagoAux = "";
			BigDecimal montoTotal = BigDecimal.ZERO;
			boolean esPrimero = true;
			Payment payment = new Payment();
			PaymentApplication paymentApplication = new PaymentApplication();
			DataImportPayment rawdata = null;
			String idPago = ledger_repo.getNextSeqId("Payment");
			ArrayList<String> nombreArrayList = new ArrayList<String>();
			boolean contiene = false;
			String messageError = "";

				
			
			for (int i = 0; i < dataforimp.size(); i++) {
				rawdata = dataforimp.get(i);
				Debug.log("Omar - rawdata: " + rawdata);
				// import accounts as many as possible
				try 
				{	referenciaPago = rawdata.getReferenciaPago();					
					// begin importing raw data item
					if(referenciaPago.equals(referenciaPagoAux) || esPrimero)
					{	Debug.log("rawdata.getMontoAplicado: " + rawdata.getMontoAplicado());
						Debug.log("montoTotal1: " + montoTotal);
						montoTotal = montoTotal.add(rawdata.getMontoAplicado());
						Debug.log("montoTotal2: " + montoTotal);
						Debug.log("referenciaPago: " + referenciaPago);
						Debug.log("referenciaPagoAux: " + referenciaPagoAux);
						
						
						payment.setPartyIdFrom(rawdata.getPartyIdFrom());
						payment.setPartyIdTo(rawdata.getPartyIdTo());
						payment.setStatusId("PMNT_NOT_PAID");
						payment.setEffectiveDate(rawdata.getFechaContable());
						payment.setPaymentRefNum(rawdata.getReferenciaPago());
						payment.setCurrencyUomId(rawdata.getMoneda());
						payment.setComments(rawdata.getDescripcionPago());
						payment.setIdTipoDoc(rawdata.getTipoDocumento());
						payment.setBancoId(rawdata.getBanco());
						payment.setCuentaBancariaId(rawdata.getCuentaBancaria());
						payment.setPaymentTypeId(rawdata.getTipoPago());							
						
						esPrimero = false;					
						referenciaPagoAux = referenciaPago;																																				
					}
					else
					{	Debug.log("Else");
						referenciaPagoAux = referenciaPago;						
						if(rawdata.getTipoPago()!=null)
						{	if(rawdata.getPartyIdFrom()!=null)
							{	if(rawdata.getPartyIdTo()!=null)
								{	if(rawdata.getFechaContable()!=null)
									{	if(rawdata.getReferenciaPago()!=null)
										{	if(rawdata.getMoneda()!=null)
											{	if(rawdata.getTipoDocumento()!=null)
												{	payment.setAmount(montoTotal);															
													payment.setPaymentId(idPago);	
													contiene = nombreArrayList.contains(idPago);
													Debug.log("contiene: " + contiene);
													Debug.log("idPagoContiene: " + idPago);
													if(contiene == false);
													{	Debug.log("Entra al if");
														Debug.log("pagoId: " + idPago);
														nombreArrayList.add(idPago);
													}
													imp_tx1 = this.session.beginTransaction();
													ledger_repo.createOrUpdate(payment);
													imp_tx1.commit();												
													idPago = ledger_repo.getNextSeqId("Payment");
													payment = new Payment();
													montoTotal = BigDecimal.ZERO;		
													Debug.log("montoTotal1: " + montoTotal);
													montoTotal = montoTotal.add(rawdata.getMontoAplicado());
													Debug.log("montoTotal2: " + montoTotal);
												}
												else
												{	Debug.log("Omar - Falta ingresar el Tipo de Documento");
													messageError = "Falta ingresar el Tipo de Documento";
													storeImportPagosErrorReferencia(rawdata, messageError, imp_repo, rawdata.getReferenciaPago(), dispatcher, delegator);
													//throw new ServiceException(String.format("Falta ingresar el Tipo de Documento"));												
												}
											}
											else
											{	Debug.log("Omar - Falta ingresar la Moneda");
												messageError = "Falta ingresar la Moneda";
												storeImportPagosErrorReferencia(rawdata, messageError, imp_repo, rawdata.getReferenciaPago(), dispatcher, delegator);
												//throw new ServiceException(String.format("Falta ingresar la Moneda"));
											}
										}
										else	
										{	Debug.log("Omar - Falta ingresar el N\u00famero de referencia del pago");
											messageError = "Falta ingresar el N\u00famero de referencia del pago";
											storeImportPagosErrorReferencia(rawdata, messageError, imp_repo, rawdata.getReferenciaPago(), dispatcher, delegator);
											//throw new ServiceException(String.format("Falta ingresar el N\u00famero de referencia del pago"));
										}
									}
									else
									{	Debug.log("Omar - Falta ingresar la Fecha Contable");
										messageError = "Falta ingresar la Fecha Contable";
										storeImportPagosErrorReferencia(rawdata, messageError, imp_repo, rawdata.getReferenciaPago(), dispatcher, delegator);
										//throw new ServiceException(String.format("Falta ingresar la Fecha Contable"));									
									}
								}
								else
								{	Debug.log("Omar - Falta ingresar la Organizaci\u00f3n Destino");
									messageError = "Falta ingresar la Organizaci\u00f3n Destino";
									storeImportPagosErrorReferencia(rawdata, messageError, imp_repo, rawdata.getReferenciaPago(), dispatcher, delegator);
									//throw new ServiceException(String.format("Falta ingresar la Organizaci\u00f3n Destino"));
								}
							}
							else
							{	Debug.log("Omar - Falta ingresar la Entidad Contable");
								messageError = "Falta ingresar la Entidad Contable";
								storeImportPagosErrorReferencia(rawdata, messageError, imp_repo, rawdata.getReferenciaPago(), dispatcher, delegator);
								//throw new ServiceException(String.format("Falta ingresar la Entidad Contable"));
							}
						}
						else
						{	Debug.log("Omar - Falta ingresar el Tipo de Pago");
							messageError = "Falta ingresar el Tipo de Pago";
							storeImportPagosErrorReferencia(rawdata, messageError, imp_repo, rawdata.getReferenciaPago(), dispatcher, delegator);
							//throw new ServiceException(String.format("Falta ingresar el Tipo de Pago"));
						}
					}
					String message = "Se import\u00f3 correctamente la aplicacion de los pagos";
					//this.storeImportPagosSuccess(rawdata, imp_repo);
					Debug.logInfo(message, MODULE);				
					
					
					

				} catch (Exception ex) 
				  {	String idErr = ex.getMessage();
					//messageError = "Error al importar los pagos. C\u00f3digo de error: ["
						//	         + idErr + "].";
					storeImportPagosError(rawdata, messageError, imp_repo);				
					Debug.logError(ex, messageError, MODULE);
					throw new ServiceException(ex.getMessage());
				 }
			}
			
			//Crea el registro para insertar el ultimo pago 
			try
			{	payment = new Payment();
				Debug.log("rawdata Final: "+ rawdata);
				if(rawdata.getTipoPago()!=null)
				{	if(rawdata.getPartyIdFrom()!=null)
					{	if(rawdata.getPartyIdTo()!=null)
						{	if(rawdata.getFechaContable()!=null)
							{	if(rawdata.getReferenciaPago()!=null)
								{	if(rawdata.getMoneda()!=null)
									{	if(rawdata.getTipoDocumento()!=null)
										{	payment.setPaymentId(idPago);
											contiene = nombreArrayList.contains(idPago);
											Debug.log("contiene: " + contiene);
											Debug.log("idPagoContiene: " + idPago);
											if(contiene == false);
											{	Debug.log("Entra al if");
												Debug.log("pagoId: " + idPago);
												nombreArrayList.add(idPago);
											}
											payment.setPartyIdFrom(rawdata.getPartyIdFrom());
											payment.setPartyIdTo(rawdata.getPartyIdTo());
											payment.setStatusId("PMNT_NOT_PAID");
											payment.setEffectiveDate(rawdata.getFechaContable());
											payment.setPaymentRefNum(rawdata.getReferenciaPago());
											payment.setCurrencyUomId(rawdata.getMoneda());
											payment.setComments(rawdata.getDescripcionPago());
											payment.setIdTipoDoc(rawdata.getTipoDocumento());
											payment.setBancoId(rawdata.getBanco());
											payment.setCuentaBancariaId(rawdata.getCuentaBancaria());
											payment.setPaymentTypeId(rawdata.getTipoPago());
											payment.setAmount(montoTotal);
											Debug.log("montoTotal5: " + montoTotal);
											montoTotal = BigDecimal.ZERO;
											Debug.log("montoTotal6: " + montoTotal);
											imp_tx1 = this.session.beginTransaction();
											ledger_repo.createOrUpdate(payment);
											imp_tx1.commit();
										}
									else
									{	Debug.log("Omar - Falta ingresar el Tipo de Documento");
										messageError = "Falta ingresar el Tipo de Documento";
										storeImportPagosErrorReferencia(rawdata, messageError, imp_repo, rawdata.getReferenciaPago(), dispatcher, delegator);
										//throw new ServiceException(String.format("Falta ingresar el Tipo de Documento"));												
									}
								}
								else
								{	Debug.log("Omar - Falta ingresar la Moneda");
									messageError = "Falta ingresar la Moneda";
									storeImportPagosErrorReferencia(rawdata, messageError, imp_repo, rawdata.getReferenciaPago(), dispatcher, delegator);
									//throw new ServiceException(String.format("Falta ingresar la Moneda"));
								}
							}
							else	
							{	Debug.log("Omar - Falta ingresar el N\u00famero de referencia del pago");
								messageError = "Falta ingresar el N\u00famero de referencia del pago";
								storeImportPagosErrorReferencia(rawdata, messageError, imp_repo, rawdata.getReferenciaPago(), dispatcher, delegator);
								//throw new ServiceException(String.format("Falta ingresar el N\u00famero de referencia del pago"));
							}
						}
						else
						{	Debug.log("Omar - Falta ingresar la Fecha Contable");
							messageError = "Falta ingresar la Fecha Contable";
							storeImportPagosErrorReferencia(rawdata, messageError, imp_repo, rawdata.getReferenciaPago(), dispatcher, delegator);
							//throw new ServiceException(String.format("Falta ingresar la Fecha Contable"));									
						}
					}
					else
					{	Debug.log("Omar - Falta ingresar la Organizaci\u00f3n Destino");
						messageError = "Falta ingresar la Organizaci\u00f3n Destino";
						storeImportPagosErrorReferencia(rawdata, messageError, imp_repo, rawdata.getReferenciaPago(), dispatcher, delegator);
						//throw new ServiceException(String.format("Falta ingresar la Organizaci\u00f3n Destino"));
					}
				}
				else
				{	Debug.log("Omar - Falta ingresar la Entidad Contable");
					messageError = "Falta ingresar la Entidad Contable";
					storeImportPagosErrorReferencia(rawdata, messageError, imp_repo, rawdata.getReferenciaPago(), dispatcher, delegator);
					//throw new ServiceException(String.format("Falta ingresar la Entidad Contable"));
				}
			}
			else
			{	Debug.log("Omar - Falta ingresar el Tipo de Pago");
				messageError = "Falta ingresar el Tipo de Pago";
				storeImportPagosErrorReferencia(rawdata, messageError, imp_repo, rawdata.getReferenciaPago(), dispatcher, delegator);
				//throw new ServiceException(String.format("Falta ingresar el Tipo de Pago"));
			}
				
				String message = "SSSe import\u00f3 correctamente la aplicacion de los pagos";
				//this.storeImportPagosSuccess(rawdata, imp_repo);
				Debug.logInfo(message, MODULE);
			}
			catch (Exception ex) 
			  {	String idErr = ex.getMessage();
				messageError = "Error al importar los pagos. C\u00f3digo de error: ["
						         + idErr + "].";
				storeImportPagosError(rawdata, messageError, imp_repo);

				// rollback all if there was an error when importing item
				if (imp_tx1 != null) {
					imp_tx1.rollback();
				}				

				Debug.logError(ex, messageError, MODULE);
				throw new ServiceException(ex.getMessage());
			 }
			Debug.log("Fin de la primera tramsaccion");						
			Debug.log("Hizo Commit");
			//Se crea la transaccion en la tabla PaymentApplication
			/*******************************************************************************/
			/**********************************REVISAR***********************************/
			/*REVISAR EL CAMBIO DE ESTATUS CUENDO YA ESTA MARCADO COMO FAILED*/
			/*try
			{	Debug.log("Se crea la transaccion en la tabla PaymentApplication");
				dataforimp = imp_repo.buscarSinError();
				Debug.log("Omar - dataforimpPayment: " + dataforimp);
				for (int j = 0; j < dataforimp.size(); j++) 
				{	Debug.log("Entra a aplicacion");
					rawdata = dataforimp.get(j);
					List<GenericValue> idPayment = null;
					String pagoId = "";					
					/*List<String> orderBy = UtilMisc.toList("paymentRefNum");
					Debug.log("Entra a aplicacion1");
					EntityConditionList<EntityExpr> conditions = EntityCondition.makeCondition(UtilMisc.toList(
			                EntityCondition.makeCondition("paymentReNum", EntityOperator.EQUALS, rawdata.getReferenciaPago())));*/
				/*	Debug.log("Entra a aplicacion3");
					//Debug.log("conditions: " + conditions);
					//idPayment = delegator.findList("Payment", conditions, null, orderBy, null, false);
					EntityCondition conditions = EntityCondition.makeCondition(EntityOperator.AND,
		                    EntityCondition.makeCondition("paymentRefNum", EntityOperator.EQUALS, rawdata.getReferenciaPago()));
					idPayment = delegator.findByCondition("Payment", conditions, UtilMisc.toList("paymentId"), UtilMisc.toList("paymentId"));				
					Debug.log("Entra a aplicacion4");
					Iterator<GenericValue> itemsPayment = idPayment.iterator();
		            while (itemsPayment.hasNext()) 
		            {	GenericValue itemPayment = itemsPayment.next();
		            	pagoId = itemPayment.getString("paymentId");		            			            	
		            }
					Debug.log("idPayment: " + pagoId);
					PaymentApplication paymentApp = new PaymentApplication();	
					paymentApp.setPaymentApplicationId(ledger_repo.getNextSeqId("PaymentApplication"));
					paymentApp.setPaymentId(pagoId);
					paymentApp.setInvoiceId(rawdata.getIdFactura());
					paymentApp.setAmountApplied(rawdata.getMontoAplicado());
					imp_tx2 = this.session.beginTransaction();
					ledger_repo.createOrUpdate(paymentApp);
					imp_tx2.commit();								
					String message = "Se import\u00f3 correctamente la aplicacion de los pagos";
					this.storeImportPagosSuccess(rawdata, imp_repo);
					Debug.logInfo(message, MODULE);
				}
				
				Debug.log("nombreArrayList: "+ nombreArrayList);
				Iterator<String> it = nombreArrayList.iterator();
				Map<String,Object> resultado = FastMap.newInstance();
				while(it.hasNext())
				{   String identificadorPago = it.next();	
					Debug.log("identificadorPago: " + identificadorPago);
					//Map<String,Object> input = FastMap.newInstance();
					//input.put("locale", locale);
					//input.put("userLogin", userLogin);
					//input.put("paymentId", identificadorPago);
					//resultado = dispatcher.runSync("postPaymentToGlCustom", input);
					//Debug.log("resultado:" + resultado);
					Map<String, Object> results;
					results = dispatcher.runSync("setPaymentStatus", UtilMisc.toMap("userLogin", userLogin, "paymentId", identificadorPago, "statusId", "PMNT_SENT"));
					Debug.log("results:" + results);
				}
				
				
											
				
				
			}
			catch (Exception ex) 
			{	String idErr = ex.getMessage();
				messageError = "Error al aplicar el pago: ["
						         + idPago + "].";
				storeImportPagosError(rawdata, messageError, imp_repo);
				
				if (imp_tx2 != null) {
					imp_tx2.rollback();
				}
				
				Debug.logError(ex, messageError, MODULE);
				throw new ServiceException(ex.getMessage());
			}

			imported = imported + 1;
			// / relacionar ParentId a enum
			//getParentEnumeration(ledger_repo, dataforimp, imp_repo);
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
	}*/
	
	/**
	 * Helper method to store Payment import success into
	 * <code>DataImportPayment</code> entity row.
	 * 
	 * @param rawdata
	 *            item of <code>DataImportPayment</code> entity that was
	 *            successfully imported
	 * @param imp_repo
	 *            repository of accounting
	 * @throws org.opentaps.foundation.repository.RepositoryException
	 */
	@SuppressWarnings("unused")
	private void storeImportPagosSuccess(DataImportPayment rawdata,
			PagosDataImportRepositoryInterface imp_repo)
			throws RepositoryException {
		// mark as success
		rawdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_IMPORTED);
		rawdata.setImportError(null);
		rawdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
		imp_repo.createOrUpdate(rawdata);
	}
	
	
	/**
	 * Helper method to store Payment import error into
	 * <code>DataImportPayment</code> entity row.
	 * 
	 * @param rawdata
	 *            item of <code>DataImportPayment</code> entity that was
	 *            unsuccessfully imported
	 * @param message
	 *            error message
	 * @param imp_repo
	 *            repository of accounting
	 * @throws org.opentaps.foundation.repository.RepositoryException
	 */
	private void storeImportPagosErrorReferencia(DataImportPayment rawdata, String messageError,
			PagosDataImportRepositoryInterface imp_repo, String numReferencia, LocalDispatcher dispatcher, Delegator delegator)
			throws RepositoryException {
		Transaction imp_txError = null;
		Debug.log("Omar - message: " + messageError);
		List<DataImportPayment> dataforError = imp_repo
				.buscarPorReferencia(numReferencia);
		Debug.log("dataforError: " + dataforError);
		DataImportPayment rawdataError = null;
		for (int j = 0; j < dataforError.size(); j++) 
		{	Debug.log("Entra a poner el ERROR");
			rawdataError = dataforError.get(j);
			rawdataError.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_FAILED);
			rawdataError.setImportError(messageError);
			rawdataError.setProcessedTimestamp(UtilDateTime.nowTimestamp());
			imp_txError = this.session.beginTransaction();
			imp_repo.createOrUpdate(rawdataError);								
			imp_txError.commit();
		}		
	}

	/**
	 * Helper method to store Payment import error into
	 * <code>DataImportPayment</code> entity row.
	 * 
	 * @param rawdata
	 *            item of <code>DataImportPayment</code> entity that was
	 *            unsuccessfully imported
	 * @param message
	 *            error message
	 * @param imp_repo
	 *            repository of accounting
	 * @throws org.opentaps.foundation.repository.RepositoryException
	 */
	private void storeImportPagosError(DataImportPayment rawdata, String messageError,
			PagosDataImportRepositoryInterface imp_repo)
			throws RepositoryException {
		Debug.log("Omar - message: " + messageError);
		// store the exception and mark as failed
		rawdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_FAILED);
		rawdata.setImportError(messageError);
		rawdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
		imp_repo.createOrUpdate(rawdata);
	}
}
