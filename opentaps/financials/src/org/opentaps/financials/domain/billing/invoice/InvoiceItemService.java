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
package org.opentaps.financials.domain.billing.invoice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javolution.context.LocalContext;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilObject;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.order.finaccount.UtilClavePresupuestal;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.common.util.UtilAccountingTags;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.common.util.UtilConfig;
import org.opentaps.domain.DomainService;
import org.opentaps.base.entities.InvoiceItem;
import org.opentaps.base.entities.SupplierProduct;
import org.opentaps.domain.billing.invoice.Invoice;
import org.opentaps.domain.billing.invoice.InvoiceItemServiceInterface;
import org.opentaps.domain.billing.invoice.InvoiceRepositoryInterface;
import org.opentaps.domain.organization.AccountingTagConfigurationForOrganizationAndUsage;
import org.opentaps.domain.product.Product;
import org.opentaps.domain.product.ProductRepositoryInterface;
import org.opentaps.domain.purchasing.PurchasingRepositoryInterface;
import org.opentaps.foundation.entity.EntityNotFoundException;
import org.opentaps.foundation.infrastructure.InfrastructureException;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.service.ServiceException;

import com.ibm.icu.util.Calendar;

/**
 * POJO implementation of services which create/update invoice item using the
 * opentaps Service foundation class.
 */
public class InvoiceItemService extends DomainService implements InvoiceItemServiceInterface {

    private static final String MODULE = InvoiceItemService.class.getName();
    private static int INVOICE_ITEM_PADDING = 4;
    private String invoiceId;
    private Boolean validateAccountingTags = false;

    private String invoiceItemSeqId;
    private String invoiceItemTypeId;
    private String overrideGlAccountId;
    private String inventoryItemId;
    private String productId;
    private String productFeatureId;
    private String parentInvoiceId;
    private String parentInvoiceItemSeqId;
    private String uomId;
    private String taxableFlag;
    private BigDecimal quantity;
    private BigDecimal amount;
    private BigDecimal montoRestante;
    private String description;
    private String taxAuthPartyId;
    private String taxAuthGeoId;
    private String taxAuthorityRateSeqId;
    private String acctgTagEnumId1;
    private String acctgTagEnumId2;
    private String acctgTagEnumId3;
    private String acctgTagEnumId4;
    private String acctgTagEnumId5;
    private String acctgTagEnumId6;
    private String acctgTagEnumId7;
    private String acctgTagEnumId8;
    private String acctgTagEnumId9;
    private String acctgTagEnumId10;
    private String acctgTagEnumId11;
    private String acctgTagEnumId12;
    private String acctgTagEnumId13;
    private String acctgTagEnumId14;
    private String acctgTagEnumId15;
    private String acctgTagEnumIdAdmin;
    private String clasificacion1;
    private String clasificacion2;
    private String clasificacion3;
    private String clasificacion4;
    private String clasificacion5;
    private String clasificacion6;
    private String clasificacion7;
    private String clasificacion8;
    private String clasificacion9;
    private String clasificacion10;
    private String clasificacion11;
    private String clasificacion12;
    private String clasificacion13;
    private String clasificacion14;
    private String clasificacion15;
    private String overrideOrgPartyId;
    private String clasificacionAdmin;
    private int indiceClasAdmin;
    private String clavePresupuestal = new String();
    private Map montoMap;
    private Map catalogoCargoContMap;
    private Map catalogoAbonoContMap;
    private Map catalogoCargoPresMap;
    private Map catalogoAbonoPresMap;
    private Map clavePresupuestalMap;

    private List<String> parametersAlreadySet = new ArrayList<String>();
    private Map<String, String> mapaParameters = FastMap.newInstance();
    private Map<String, Map> mapaParametersMap = FastMap.newInstance();
    
	private GenericValue estructuraClave;

    /**
     * Default constructor.
     */
    public InvoiceItemService() {
        super();
    }

    /** {@inheritDoc} */
    public String getInvoiceItemSeqId() {
        return invoiceItemSeqId;
    }
    

    /** {@inheritDoc} */
    public void setValidateAccountingTags(Boolean validateAccountingTags) {
        this.validateAccountingTags = validateAccountingTags;
    }

    /** {@inheritDoc} */
    public void setAcctgTagEnumId1(String acctgTagEnumId1) {
        this.acctgTagEnumId1 = acctgTagEnumId1;
        parametersAlreadySet.add("acctgTagEnumId1");
        mapaParameters.put("acctgTagEnumId1", acctgTagEnumId1);
    }

    /** {@inheritDoc} */
    public void setAcctgTagEnumId10(String acctgTagEnumId10) {
        this.acctgTagEnumId10 = acctgTagEnumId10;
        parametersAlreadySet.add("acctgTagEnumId10");
        mapaParameters.put("acctgTagEnumId10", acctgTagEnumId10);
    }

    /** {@inheritDoc} */
    public void setAcctgTagEnumId2(String acctgTagEnumId2) {
        this.acctgTagEnumId2 = acctgTagEnumId2;
        parametersAlreadySet.add("acctgTagEnumId2");
        mapaParameters.put("acctgTagEnumId2", acctgTagEnumId2);
    }

    /** {@inheritDoc} */
    public void setAcctgTagEnumId3(String acctgTagEnumId3) {
        this.acctgTagEnumId3 = acctgTagEnumId3;
        parametersAlreadySet.add("acctgTagEnumId3");
        mapaParameters.put("acctgTagEnumId3", acctgTagEnumId3);
    }

    /** {@inheritDoc} */
    public void setAcctgTagEnumId4(String acctgTagEnumId4) {
        this.acctgTagEnumId4 = acctgTagEnumId4;
        parametersAlreadySet.add("acctgTagEnumId4");
        mapaParameters.put("acctgTagEnumId4", acctgTagEnumId4);
    }

    /** {@inheritDoc} */
    public void setAcctgTagEnumId5(String acctgTagEnumId5) {
        this.acctgTagEnumId5 = acctgTagEnumId5;
        parametersAlreadySet.add("acctgTagEnumId5");
        mapaParameters.put("acctgTagEnumId5", acctgTagEnumId5);
    }

    /** {@inheritDoc} */
    public void setAcctgTagEnumId6(String acctgTagEnumId6) {
        this.acctgTagEnumId6 = acctgTagEnumId6;
        parametersAlreadySet.add("acctgTagEnumId6");
        mapaParameters.put("acctgTagEnumId6", acctgTagEnumId6);
    }

    /** {@inheritDoc} */
    public void setAcctgTagEnumId7(String acctgTagEnumId7) {
        this.acctgTagEnumId7 = acctgTagEnumId7;
        parametersAlreadySet.add("acctgTagEnumId7");
        mapaParameters.put("acctgTagEnumId7", acctgTagEnumId7);
    }

    /** {@inheritDoc} */
    public void setAcctgTagEnumId8(String acctgTagEnumId8) {
        this.acctgTagEnumId8 = acctgTagEnumId8;
        parametersAlreadySet.add("acctgTagEnumId8");
        mapaParameters.put("acctgTagEnumId8", acctgTagEnumId8);
    }
    
    /** {@inheritDoc} */
    public void setAcctgTagEnumId9(String acctgTagEnumId9) {
        this.acctgTagEnumId9 = acctgTagEnumId9;
        parametersAlreadySet.add("acctgTagEnumId9");
        mapaParameters.put("acctgTagEnumId9", acctgTagEnumId9);
    }
    
	@Override
	public void setAcctgTagEnumId11(String acctgTagEnumId11) {
        this.acctgTagEnumId11 = acctgTagEnumId11;
        parametersAlreadySet.add("acctgTagEnumId11");
        mapaParameters.put("acctgTagEnumId11", acctgTagEnumId11);
	}

	@Override
	public void setAcctgTagEnumId12(String acctgTagEnumId12) {
        this.acctgTagEnumId12 = acctgTagEnumId12;
        parametersAlreadySet.add("acctgTagEnumId12");
        mapaParameters.put("acctgTagEnumId12", acctgTagEnumId12);
	}

	@Override
	public void setAcctgTagEnumId13(String acctgTagEnumId13) {
        this.acctgTagEnumId13 = acctgTagEnumId13;
        parametersAlreadySet.add("acctgTagEnumId13");
        mapaParameters.put("acctgTagEnumId13", acctgTagEnumId13);
	}

	@Override
	public void setAcctgTagEnumId14(String acctgTagEnumId14) {
        this.acctgTagEnumId14 = acctgTagEnumId14;
        parametersAlreadySet.add("acctgTagEnumId14");
        mapaParameters.put("acctgTagEnumId14", acctgTagEnumId14);
	}

	@Override
	public void setAcctgTagEnumId15(String acctgTagEnumId15) {
        this.acctgTagEnumId15 = acctgTagEnumId15;
        parametersAlreadySet.add("acctgTagEnumId15");
        mapaParameters.put("acctgTagEnumId15", acctgTagEnumId15);
	}

    /** {@inheritDoc} */
    public void setClasificacion1(String clasificacion1) {
        this.clasificacion1 = clasificacion1;
        parametersAlreadySet.add("clasificacion1");
        mapaParameters.put("clasificacion1", clasificacion1);
    }

    /** {@inheritDoc} */
    public void setClasificacion2(String clasificacion2) {
        this.clasificacion2 = clasificacion2;
        parametersAlreadySet.add("clasificacion2");
        mapaParameters.put("clasificacion2", clasificacion2);
    }

    /** {@inheritDoc} */
    public void setClasificacion3(String clasificacion3) {
        this.clasificacion3 = clasificacion3;
        parametersAlreadySet.add("clasificacion3");
        mapaParameters.put("clasificacion3", clasificacion3);
    }

    /** {@inheritDoc} */
    public void setClasificacion4(String clasificacion4) {
        this.clasificacion4 = clasificacion4;
        parametersAlreadySet.add("clasificacion4");
        mapaParameters.put("clasificacion4", clasificacion4);
    }

    /** {@inheritDoc} */
    public void setClasificacion5(String clasificacion5) {
        this.clasificacion5 = clasificacion5;
        parametersAlreadySet.add("clasificacion5");
        mapaParameters.put("clasificacion5", clasificacion5);
    }

    /** {@inheritDoc} */
    public void setClasificacion6(String clasificacion6) {
        this.clasificacion6 = clasificacion6;
        parametersAlreadySet.add("clasificacion6");
        mapaParameters.put("clasificacion6", clasificacion6);
    }

    /** {@inheritDoc} */
    public void setClasificacion7(String clasificacion7) {
        this.clasificacion7 = clasificacion7;
        parametersAlreadySet.add("clasificacion7");
        mapaParameters.put("clasificacion7", clasificacion7);
    }

    /** {@inheritDoc} */
    public void setClasificacion8(String clasificacion8) {
        this.clasificacion8 = clasificacion8;
        parametersAlreadySet.add("clasificacion8");
        mapaParameters.put("clasificacion8", clasificacion8);
    }

    /** {@inheritDoc} */
    public void setClasificacion9(String clasificacion9) {
        this.clasificacion9 = clasificacion9;
        parametersAlreadySet.add("clasificacion9");
        mapaParameters.put("clasificacion9", clasificacion9);
    }

    /** {@inheritDoc} */
    public void setClasificacion10(String clasificacion10) {
        this.clasificacion10 = clasificacion10;
        parametersAlreadySet.add("clasificacion10");
        mapaParameters.put("clasificacion10", clasificacion10);
    }

    /** {@inheritDoc} */
    public void setClasificacion11(String clasificacion11) {
        this.clasificacion11 = clasificacion11;
        parametersAlreadySet.add("clasificacion11");
        mapaParameters.put("clasificacion11", clasificacion11);
    }

    /** {@inheritDoc} */
    public void setClasificacion12(String clasificacion12) {
        this.clasificacion12 = clasificacion12;
        parametersAlreadySet.add("clasificacion12");
        mapaParameters.put("clasificacion12", clasificacion12);
    }

    /** {@inheritDoc} */
    public void setClasificacion13(String clasificacion13) {
        this.clasificacion13 = clasificacion13;
        parametersAlreadySet.add("clasificacion13");
        mapaParameters.put("clasificacion13", clasificacion13);
    }

    /** {@inheritDoc} */
    public void setClasificacion14(String clasificacion14) {
        this.clasificacion14 = clasificacion14;
        parametersAlreadySet.add("clasificacion14");
        mapaParameters.put("clasificacion14", clasificacion14);
    }

    /** {@inheritDoc} */
    public void setClasificacion15(String clasificacion15) {
        this.clasificacion15 = clasificacion15;
        parametersAlreadySet.add("clasificacion15");
        mapaParameters.put("clasificacion15", clasificacion15);
    }
    
    /** {@inheritDoc} */
    public void setMontoMap(Map montoMap) {
        this.montoMap = montoMap;
        parametersAlreadySet.add("montoMap");
        mapaParametersMap.put("montoMap", montoMap);
    }
    
    /** {@inheritDoc} */
    public void setCatalogoCargoContMap(Map catalogoCargoContMap) {
        this.catalogoCargoContMap = catalogoCargoContMap;
        parametersAlreadySet.add("catalogoCargoContMap");
        mapaParametersMap.put("catalogoCargoContMap", catalogoCargoContMap);
    }
    
    /** {@inheritDoc} */
    public void setCatalogoAbonoContMap(Map catalogoAbonoContMap) {
        this.catalogoAbonoContMap = catalogoAbonoContMap;
        parametersAlreadySet.add("catalogoAbonoContMap");
        mapaParametersMap.put("catalogoAbonoContMap", catalogoAbonoContMap);
    }
    
    /** {@inheritDoc} */
    public void setCatalogoCargoPresMap(Map catalogoCargoPresMap) {
        this.catalogoCargoPresMap = catalogoCargoPresMap;
        parametersAlreadySet.add("catalogoCargoPresMap");
        mapaParametersMap.put("catalogoCargoPresMap", catalogoCargoPresMap);
    }
    
    /** {@inheritDoc} */
    public void setCatalogoAbonoPresMap(Map catalogoAbonoPresMap) {
        this.catalogoAbonoPresMap = catalogoAbonoPresMap;
        parametersAlreadySet.add("catalogoAbonoPresMap");
        mapaParametersMap.put("catalogoAbonoPresMap", catalogoAbonoPresMap);
    }
    
    /** {@inheritDoc} */
    public void setClavePresupuestalMap(Map clavePresupuestalMap) {
        this.clavePresupuestalMap = clavePresupuestalMap;
        parametersAlreadySet.add("clavePresupuestalMap");
        mapaParametersMap.put("clavePresupuestalMap", clavePresupuestalMap);
    }
    
    
    
    
    /** {@inheritDoc} */
    public void setAcctgTagEnumIdAdmin(String acctgTagEnumIdAdmin) {
        this.acctgTagEnumIdAdmin = acctgTagEnumIdAdmin;
        parametersAlreadySet.add("acctgTagEnumIdAdmin");
        mapaParameters.put("acctgTagEnumIdAdmin", acctgTagEnumIdAdmin);
    }
    

    /** {@inheritDoc} */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
        parametersAlreadySet.add("amount");
    }
    
    /** {@inheritDoc} */
    public void setMontoRestante(BigDecimal montoRestante) {
        this.montoRestante = montoRestante;
        parametersAlreadySet.add("montoRestante");
    }

    /** {@inheritDoc} */
    public void setDescription(String description) {
        this.description = description;
        parametersAlreadySet.add("description");
    }

    /** {@inheritDoc} */
    public void setInventoryItemId(String inventoryItemId) {
        this.inventoryItemId = inventoryItemId;
        parametersAlreadySet.add("inventoryItemId");
    }

    /** {@inheritDoc} */
    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
        parametersAlreadySet.add("invoiceId");
    }

    /** {@inheritDoc} */
    public void setInvoiceItemSeqId(String invoiceItemSeqId) {
        this.invoiceItemSeqId = invoiceItemSeqId;
        parametersAlreadySet.add("invoiceItemSeqId");
    }

    /** {@inheritDoc} */
    public void setInvoiceItemTypeId(String invoiceItemTypeId) {
        this.invoiceItemTypeId = invoiceItemTypeId;
        parametersAlreadySet.add("invoiceItemTypeId");
    }

    /** {@inheritDoc} */
    public void setOverrideGlAccountId(String overrideGlAccountId) {
        this.overrideGlAccountId = overrideGlAccountId;
        parametersAlreadySet.add("overrideGlAccountId");
    }
    
    public void setOverrideOrgPartyId(String overrideOrgPartyId){
    	this.overrideOrgPartyId = overrideOrgPartyId;
    	parametersAlreadySet.add("overrideOrgPartyId");
    }

    /** {@inheritDoc} */
    public void setParentInvoiceId(String parentInvoiceId) {
        this.parentInvoiceId = parentInvoiceId;
        parametersAlreadySet.add("parentInvoiceId");
    }

    /** {@inheritDoc} */
    public void setParentInvoiceItemSeqId(String parentInvoiceItemSeqId) {
        this.parentInvoiceItemSeqId = parentInvoiceItemSeqId;
        parametersAlreadySet.add("parentInvoiceItemSeqId");
    }

    /** {@inheritDoc} */
    public void setProductFeatureId(String productFeatureId) {
        this.productFeatureId = productFeatureId;
        parametersAlreadySet.add("productFeatureId");
    }

    /** {@inheritDoc} */
    public void setProductId(String productId) {
        this.productId = productId;
        parametersAlreadySet.add("productId");
    }

    /** {@inheritDoc} */
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        parametersAlreadySet.add("quantity");
    }

    /** {@inheritDoc} */
    public void setTaxAuthGeoId(String taxAuthGeoId) {
        this.taxAuthGeoId = taxAuthGeoId;
        parametersAlreadySet.add("taxAuthGeoId");
    }

    /** {@inheritDoc} */
    public void setTaxAuthPartyId(String taxAuthPartyId) {
        this.taxAuthPartyId = taxAuthPartyId;
        parametersAlreadySet.add("taxAuthPartyId");
    }

    /** {@inheritDoc} */
    public void setTaxAuthorityRateSeqId(String taxAuthorityRateSeqId) {
        this.taxAuthorityRateSeqId = taxAuthorityRateSeqId;
        parametersAlreadySet.add("taxAuthorityRateSeqId");
    }

    /** {@inheritDoc} */
    public void setTaxableFlag(String taxableFlag) {
        this.taxableFlag = taxableFlag;
        parametersAlreadySet.add("taxableFlag");
    }

    /** {@inheritDoc} */
    public void setUomId(String uomId) {
        this.uomId = uomId;
        parametersAlreadySet.add("uomId");
    }

    /** {@inheritDoc} */
    public void createInvoiceItem() throws ServiceException {
        try {
        	Delegator delegator = getDomainsDirectory().getInfrastructure().getDelegator();
            if (productId != null) {
                setInvoiceItemDefaultProperties();
            }
            // create InvoiceItem by hibernate
            InvoiceRepositoryInterface invoiceRepository = getDomainsDirectory().getBillingDomain().getInvoiceRepository();
            Invoice invoice = invoiceRepository.getInvoiceById(invoiceId);
            this.clasificacionAdmin = obtenClasifAdmin(invoice);
            InvoiceItem invoiceItem = new InvoiceItem();
            invoiceItem.initRepository(invoiceRepository);
            // write parameters of service to invoiceItem object
            setParametersToInvoiceItem(invoiceItem);
            // validate tags parameters if necessary
            if (validateAccountingTags) {
                List<AccountingTagConfigurationForOrganizationAndUsage> missings = invoiceRepository.validateTagParameters(invoice, invoiceItem);
                if (!missings.isEmpty()) {
                    throw new ServiceException("OpentapsError_ServiceErrorRequiredTagNotFound", UtilMisc.toMap("tagName", missings.get(0).getDescription()));
                }
            }
            // if invoiceItemSeqId is null, then get next seq for it.
            if (invoiceItem.getInvoiceItemSeqId() == null) {
                invoiceItem.setNextSubSeqId(InvoiceItem.Fields.invoiceItemSeqId.name(), INVOICE_ITEM_PADDING);
            }
            
            //Valida que exista la clave presupuestal
			GenericValue cvePrespu = delegator.findByPrimaryKey("ClavePresupuestal",
    				UtilMisc.toMap("clavePresupuestal", this.clavePresupuestal));
			if (cvePrespu == null || cvePrespu.isEmpty())
				throw new ServiceException("No existe la clave presupuestal");
            
            invoiceRepository.createOrUpdate(invoiceItem);
            Debug.logInfo("create InvoiceItem InvoiceId : [" + invoiceItem.getInvoiceId() + "], invoiceItemSeqId : [" + invoiceItem.getInvoiceItemSeqId() + "]", MODULE);
            setInvoiceItemSeqId(invoiceItem.getInvoiceItemSeqId());
            
            
            crearInvoiceItemLinea(delegator, invoiceItem.getInvoiceId(), invoiceItem.getInvoiceItemSeqId(), invoice.getAcctgTransTypeId());
            
            
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            Debug.logError(e, MODULE);
            throw new ServiceException(e);
        }
    }

	/** {@inheritDoc} */
    public void updateInvoiceItem() throws ServiceException {
        // Call the updateInvoiceItem service
        // search the InvoiceItem by hibernate
        try {
            InvoiceRepositoryInterface invoiceRepository = getDomainsDirectory().getBillingDomain().getInvoiceRepository();
            Invoice invoice = invoiceRepository.getInvoiceById(invoiceId);
            this.clasificacionAdmin = obtenClasifAdmin(invoice);
            InvoiceItem invoiceItem = invoiceRepository.getInvoiceItemById(invoiceId, invoiceItemSeqId);
            // check if the productNumber is updated, when yes retrieve product
            // description and price
            if (!UtilObject.equalsHelper(invoiceItem.getProductId(), productId)) {
                if (productId != null) {
                    setInvoiceItemDefaultProperties();
                }
            }
            // write parameters of service to invoiceItem object
            setParametersToInvoiceItem(invoiceItem);
            // validate tags parameters if necessary
            if (validateAccountingTags) {
                List<AccountingTagConfigurationForOrganizationAndUsage> missings = invoiceRepository.validateTagParameters(invoice, invoiceItem);
                if (!missings.isEmpty()) {
                    throw new ServiceException("OpentapsError_ServiceErrorRequiredTagNotFound", UtilMisc.toMap("tagName", missings.get(0).getDescription()));
                }
            }
            invoiceRepository.update(invoiceItem);
            Debug.logInfo("update InvoiceItem InvoiceId : [" + invoiceItem.getInvoiceId() + "], invoiceItemSeqId : [" + invoiceItem.getInvoiceItemSeqId() + "]", MODULE);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            Debug.logError(e, MODULE);
            throw new ServiceException(e);
        }
    }
    
    /**
     * Metodo que busca la clasificacion administrativa
     * @param invoice
     * @return
     * @throws GenericEntityException
     * @throws ServiceException 
     * @throws InfrastructureException 
     */
    private String obtenClasifAdmin(Invoice invoice) throws GenericEntityException, ServiceException, InfrastructureException {
    	
    	Delegator delegator = getDomainsDirectory().getInfrastructure().getDelegator();
    	
    	String organizationPartyId = new String();
    	String acctgTagUse = new String();
    	if(invoice.isPayable()){
    		organizationPartyId = invoice.getPartyId();
    		acctgTagUse = UtilAccountingTags.EGRESO_TAG;
    	} else if (invoice.isReceivable()){
    		organizationPartyId = invoice.getPartyIdFrom(); 
    		acctgTagUse = UtilAccountingTags.INGRESO_TAG;
    	}
    	
    	String ciclo = getCiclo();
    	
    	//si no se encuentra en el contexto se obtiene de las preferencias del usuario
    	if(UtilValidate.isEmpty(ciclo)){
    		GenericValue prefUser = delegator.findByPrimaryKeyCache("UserLoginViewPreference", 
    				UtilMisc.toMap("userLoginId", getUser().getUserId(), "applicationName", UtilConfig.SYSTEM_WIDE, "screenName", 
    						UtilConfig.SET_ORGANIZATION_FORM, "preferenceName", UtilConfig.OPTION_DEF_CICLO));
    		if(UtilValidate.isEmpty(prefUser)){
    			throw new GenericEntityException("Debe seleccionar el ciclo en el que esta operando desde algun m\u00F3dulo del sistema");
    		}
    		ciclo = prefUser.getString("preferenceValue");
    		
    	}
    	
    	this.estructuraClave = UtilClavePresupuestal.obtenEstructPresupuestal(acctgTagUse, organizationPartyId, delegator, ciclo);
    	this.indiceClasAdmin = UtilClavePresupuestal.indiceClasAdmin(acctgTagUse, organizationPartyId, delegator, ciclo);
    	String clasifAdmin = UtilClavePresupuestal.VIEW_TAG_PREFIX+this.indiceClasAdmin;
		return clasifAdmin;
    	
	}
    
    
    /**
     * Busca el enumId para insertarlo en los tags
     * @param clasificacion
     * @return
     * @throws ServiceException 
     */
    private String buscaEnumId(String sequenceId,String enumTypeId) throws ServiceException {

    	Delegator delegator = getDomainsDirectory().getInfrastructure().getDelegator();
    	
    	if(sequenceId != null && !sequenceId.isEmpty() && enumTypeId != null && !enumTypeId.isEmpty())
    	{
        	try {
    			List<GenericValue> enumerationList = delegator.findByAnd("Enumeration",UtilMisc.toMap("enumTypeId",enumTypeId,
    					"sequenceId",sequenceId));
    			
    			if(enumerationList.isEmpty())
    				throw new ServiceException("No se encuentra el Enumeration con tipo "+enumTypeId+" y el sequenceId "+sequenceId); 	
    			else
    				return enumerationList.get(0).getString("enumId");
    			
    		} catch (GenericEntityException e) {
    			throw new ServiceException(e.getMessage());
    		} 
    	} else {
    		return null;
    	}
    	
	}
    
    /**
     * Busca el sequenceId de los tags
     * @param clasificacion
     * @return
     * @throws ServiceException 
     */
    private String buscaSequenceId(String enumId) throws ServiceException {

    	Delegator delegator = getDomainsDirectory().getInfrastructure().getDelegator();
    	
    	if(enumId != null && !enumId.isEmpty())
    	{
        	try {
        		GenericValue enumeration = delegator.findByPrimaryKey("Enumeration", UtilMisc.toMap("enumId",enumId));
    			if(enumeration.isEmpty())
    				throw new ServiceException("No se encuentra el Enumeration "+enumId); 	
    			else
    				return enumeration.getString("sequenceId");
    			
    		} catch (GenericEntityException e) {
    			throw new ServiceException(e.getMessage());
    		} 
    	} else {
    		return null;
    	}
    	
	}
    
    /**
     * Set parameters of service to <code>InvoiceItem</code>.
     * @param invoiceItem a <code>InvoiceItem</code> value
     * @throws ServiceException 
     * @throws RepositoryException 
     */
    private void setParametersToInvoiceItem(InvoiceItem invoiceItem) throws ServiceException, RepositoryException {
    	int contClasif = 1;
        // write the field to invoiceItem when it changed
    	Debug.logInfo("parametersAlreadySet: " + parametersAlreadySet,MODULE);
        if (parametersAlreadySet.contains("acctgTagEnumId1")) {
            invoiceItem.setAcctgTagEnumId1(acctgTagEnumId1);
        }
        if (parametersAlreadySet.contains("acctgTagEnumId2")) {
            invoiceItem.setAcctgTagEnumId2(acctgTagEnumId2);
        }
        if (parametersAlreadySet.contains("acctgTagEnumId3")) {
            invoiceItem.setAcctgTagEnumId3(acctgTagEnumId3);
        }
        if (parametersAlreadySet.contains("acctgTagEnumId4")) {
            invoiceItem.setAcctgTagEnumId4(acctgTagEnumId4);
        }
        if (parametersAlreadySet.contains("acctgTagEnumId5")) {
            invoiceItem.setAcctgTagEnumId5(acctgTagEnumId5);
        }
        if (parametersAlreadySet.contains("acctgTagEnumId6")) {
            invoiceItem.setAcctgTagEnumId6(acctgTagEnumId6);
        }
        if (parametersAlreadySet.contains("acctgTagEnumId7")) {
            invoiceItem.setAcctgTagEnumId7(acctgTagEnumId7);
        }
        if (parametersAlreadySet.contains("acctgTagEnumId8")) {
            invoiceItem.setAcctgTagEnumId8(acctgTagEnumId8);
        }
        if (parametersAlreadySet.contains("acctgTagEnumId9")) {
            invoiceItem.setAcctgTagEnumId9(acctgTagEnumId9);
        }
        if (parametersAlreadySet.contains("acctgTagEnumId10")) {
            invoiceItem.setAcctgTagEnumId10(acctgTagEnumId10);
        }
        if (parametersAlreadySet.contains("acctgTagEnumId11")) {
            invoiceItem.setAcctgTagEnumId11(acctgTagEnumId11);
        }
        if (parametersAlreadySet.contains("acctgTagEnumId12")) {
            invoiceItem.setAcctgTagEnumId12(acctgTagEnumId12);
        }
        if (parametersAlreadySet.contains("acctgTagEnumId13")) {
            invoiceItem.setAcctgTagEnumId13(acctgTagEnumId13);
        }
        if (parametersAlreadySet.contains("acctgTagEnumId14")) {
            invoiceItem.setAcctgTagEnumId14(acctgTagEnumId14);
        }
        if (parametersAlreadySet.contains("acctgTagEnumId15")) {
            invoiceItem.setAcctgTagEnumId15(acctgTagEnumId15);
        }
        if (parametersAlreadySet.contains("acctgTagEnumIdAdmin")) {
            invoiceItem.setAcctgTagEnumIdAdmin(acctgTagEnumIdAdmin);
        }
        for (int i = 1; i < UtilAccountingTags.TAG_COUNT_CLASSI; i++) {
        	if(this.indiceClasAdmin == i){
        		this.clavePresupuestal = this.clavePresupuestal.concat((acctgTagEnumIdAdmin == null ? "" : acctgTagEnumIdAdmin));
        	} else {
            	String enumId = mapaParameters.get(UtilAccountingTags.ENTITY_TAG_PREFIX+i);
            	if(enumId != null){
                 	String clavePres = buscaSequenceId(enumId);
                	this.clavePresupuestal = this.clavePresupuestal.concat((clavePres == null ? "" : clavePres));
            	}
        	}
			
		}
        
        if (parametersAlreadySet.contains("clasificacion1") && clasificacion1 != null) {
        	if(this.clasificacionAdmin.equalsIgnoreCase("clasificacion1"))
        		invoiceItem.setAcctgTagEnumIdAdmin(clasificacion1);
        	else
        		invoiceItem.setAcctgTagEnumId1(buscaEnumId(clasificacion1,this.estructuraClave.getString("clasificacion"+contClasif)));
        	this.clavePresupuestal = this.clavePresupuestal.concat(clasificacion1.trim());
        	contClasif++;
        }
        if (parametersAlreadySet.contains("clasificacion2") && clasificacion2 != null) {
        	if(this.clasificacionAdmin.equalsIgnoreCase("clasificacion2"))
        		invoiceItem.setAcctgTagEnumIdAdmin(clasificacion2);
        	else
        		invoiceItem.setAcctgTagEnumId2(buscaEnumId(clasificacion2,this.estructuraClave.getString("clasificacion"+contClasif)));
        	this.clavePresupuestal = this.clavePresupuestal.concat(clasificacion2.trim());
        	contClasif++;
        }
        if (parametersAlreadySet.contains("clasificacion3") && clasificacion3 != null) {
        	if(this.clasificacionAdmin.equalsIgnoreCase("clasificacion3"))
        		invoiceItem.setAcctgTagEnumIdAdmin(clasificacion3);
        	else
        		invoiceItem.setAcctgTagEnumId3(buscaEnumId(clasificacion3,this.estructuraClave.getString("clasificacion"+contClasif)));
        	this.clavePresupuestal = this.clavePresupuestal.concat(clasificacion3.trim());
        	contClasif++;
        } 
        if (parametersAlreadySet.contains("clasificacion4") && clasificacion4 != null) {
        	if(this.clasificacionAdmin.equalsIgnoreCase("clasificacion4"))
        		invoiceItem.setAcctgTagEnumIdAdmin(clasificacion4);
        	else
        		invoiceItem.setAcctgTagEnumId4(buscaEnumId(clasificacion4,this.estructuraClave.getString("clasificacion"+contClasif)));
        	this.clavePresupuestal = this.clavePresupuestal.concat(clasificacion4.trim());
        	contClasif++;
        }
        if (parametersAlreadySet.contains("clasificacion5") && clasificacion5 != null) {
        	if(this.clasificacionAdmin.equalsIgnoreCase("clasificacion5"))
        		invoiceItem.setAcctgTagEnumIdAdmin(clasificacion5);
        	else
        		invoiceItem.setAcctgTagEnumId5(buscaEnumId(clasificacion5,this.estructuraClave.getString("clasificacion"+contClasif)));
        	this.clavePresupuestal = this.clavePresupuestal.concat(clasificacion5.trim());
        	contClasif++;
        }
        if (parametersAlreadySet.contains("clasificacion6") && clasificacion6 != null) {
        	if(this.clasificacionAdmin.equalsIgnoreCase("clasificacion6"))
        		invoiceItem.setAcctgTagEnumIdAdmin(clasificacion6);
        	else
        		invoiceItem.setAcctgTagEnumId6(buscaEnumId(clasificacion6,this.estructuraClave.getString("clasificacion"+contClasif)));
        	this.clavePresupuestal = this.clavePresupuestal.concat(clasificacion6.trim());
        	contClasif++;
        }
        if (parametersAlreadySet.contains("clasificacion7") && clasificacion7 != null) {
        	if(this.clasificacionAdmin.equalsIgnoreCase("clasificacion7"))
        		invoiceItem.setAcctgTagEnumIdAdmin(clasificacion7);
        	else
        		invoiceItem.setAcctgTagEnumId7(buscaEnumId(clasificacion7,this.estructuraClave.getString("clasificacion"+contClasif)));
        	this.clavePresupuestal = this.clavePresupuestal.concat(clasificacion7.trim());
        	contClasif++;
        }
        if (parametersAlreadySet.contains("clasificacion8") && clasificacion8 != null) {
        	if(this.clasificacionAdmin.equalsIgnoreCase("clasificacion8"))
        		invoiceItem.setAcctgTagEnumIdAdmin(clasificacion8);
        	else
        		invoiceItem.setAcctgTagEnumId8(buscaEnumId(clasificacion8,this.estructuraClave.getString("clasificacion"+contClasif)));
        	this.clavePresupuestal = this.clavePresupuestal.concat(clasificacion8.trim());
        	contClasif++;
        }
        if (parametersAlreadySet.contains("clasificacion9") && clasificacion9 != null) {
        	if(this.clasificacionAdmin.equalsIgnoreCase("clasificacion9"))
        		invoiceItem.setAcctgTagEnumIdAdmin(clasificacion9);
        	else
        		invoiceItem.setAcctgTagEnumId9(buscaEnumId(clasificacion9,this.estructuraClave.getString("clasificacion"+contClasif)));
        	this.clavePresupuestal = this.clavePresupuestal.concat(clasificacion9.trim());
        	contClasif++;
        }
        if (parametersAlreadySet.contains("clasificacion10") && clasificacion10 != null) {
        	if(this.clasificacionAdmin.equalsIgnoreCase("clasificacion10"))
        		invoiceItem.setAcctgTagEnumIdAdmin(clasificacion10);
        	else
        		invoiceItem.setAcctgTagEnumId10(buscaEnumId(clasificacion10,this.estructuraClave.getString("clasificacion"+contClasif)));
        	this.clavePresupuestal = this.clavePresupuestal.concat(clasificacion10.trim());
        	contClasif++;
        }
        if (parametersAlreadySet.contains("clasificacion11") && clasificacion11 != null) {
        	if(this.clasificacionAdmin.equalsIgnoreCase("clasificacion11"))
        		invoiceItem.setAcctgTagEnumIdAdmin(clasificacion11);
        	else
        		invoiceItem.setAcctgTagEnumId11(buscaEnumId(clasificacion11,this.estructuraClave.getString("clasificacion"+contClasif)));
        	this.clavePresupuestal = this.clavePresupuestal.concat(clasificacion11.trim());
        	contClasif++;
        }
        if (parametersAlreadySet.contains("clasificacion12") && clasificacion12 != null) {
        	if(this.clasificacionAdmin.equalsIgnoreCase("clasificacion12"))
        		invoiceItem.setAcctgTagEnumIdAdmin(clasificacion12);
        	else
        		invoiceItem.setAcctgTagEnumId12(buscaEnumId(clasificacion12,this.estructuraClave.getString("clasificacion"+contClasif)));
        	this.clavePresupuestal = this.clavePresupuestal.concat(clasificacion12.trim());
        	contClasif++;
        }
        if (parametersAlreadySet.contains("clasificacion13") && clasificacion13 != null) {
        	if(this.clasificacionAdmin.equalsIgnoreCase("clasificacion13"))
        		invoiceItem.setAcctgTagEnumIdAdmin(clasificacion13);
        	else
        		invoiceItem.setAcctgTagEnumId13(buscaEnumId(clasificacion13,this.estructuraClave.getString("clasificacion"+contClasif)));
        	this.clavePresupuestal = this.clavePresupuestal.concat(clasificacion13.trim());
        	contClasif++;
        }
        if (parametersAlreadySet.contains("clasificacion14") && clasificacion14 != null) {
        	if(this.clasificacionAdmin.equalsIgnoreCase("clasificacion14"))
        		invoiceItem.setAcctgTagEnumIdAdmin(clasificacion14);
        	else
        		invoiceItem.setAcctgTagEnumId14(buscaEnumId(clasificacion14,this.estructuraClave.getString("clasificacion"+contClasif)));
        	this.clavePresupuestal = this.clavePresupuestal.concat(clasificacion14.trim());
        	contClasif++;
        }
        if (parametersAlreadySet.contains("clasificacion15") && clasificacion15 != null) {
        	if(this.clasificacionAdmin.equalsIgnoreCase("clasificacion15"))
        		invoiceItem.setAcctgTagEnumIdAdmin(clasificacion15);
        	else
        		invoiceItem.setAcctgTagEnumId15(buscaEnumId(clasificacion15,this.estructuraClave.getString("clasificacion"+contClasif)));
        	this.clavePresupuestal = this.clavePresupuestal.concat(clasificacion15.trim());
        }
        
        invoiceItem.setClavePresupuestal(clavePresupuestal);

        if(amount == null || amount.equals("0"))
        {	throw new ServiceException("El monto debe ser mayor a cero");	
        }
        if(quantity == null)
        {	throw new ServiceException("Es necesario ingresar la cantidad");	
        }
        if (parametersAlreadySet.contains("amount")) {
            invoiceItem.setAmount(invoiceItem.convertToBigDecimal(amount));
            invoiceItem.setMontoRestante(invoiceItem.convertToBigDecimal(amount.multiply(quantity)));
        }
        if (parametersAlreadySet.contains("quantity")) {
            invoiceItem.setQuantity(invoiceItem.convertToBigDecimal(quantity));
        }
        if (parametersAlreadySet.contains("description")) {
            invoiceItem.setDescription(description);
        }
        if (parametersAlreadySet.contains("inventoryItemId")) {
            invoiceItem.setInventoryItemId(inventoryItemId);
        }
        if (parametersAlreadySet.contains("invoiceId")) {
            invoiceItem.setInvoiceId(invoiceId);
        }
        if (parametersAlreadySet.contains("invoiceItemSeqId")) {
            invoiceItem.setInvoiceItemSeqId(invoiceItemSeqId);
        }
        if (parametersAlreadySet.contains("invoiceItemTypeId")) {
            invoiceItem.setInvoiceItemTypeId(invoiceItemTypeId);
        }
        if (parametersAlreadySet.contains("overrideGlAccountId")) {
            invoiceItem.setOverrideGlAccountId(overrideGlAccountId);
        }
        if (parametersAlreadySet.contains("parentInvoiceId")) {
            invoiceItem.setParentInvoiceId(parentInvoiceId);
        }
        if (parametersAlreadySet.contains("parentInvoiceItemSeqId")) {
            invoiceItem.setParentInvoiceItemSeqId(parentInvoiceItemSeqId);
        }
        if (parametersAlreadySet.contains("productFeatureId")) {
            invoiceItem.setProductFeatureId(productFeatureId);
        }
//        if(productId == null)
//        {	throw new ServiceException("Es necesario ingresar el producto");	
//        }
        if (parametersAlreadySet.contains("productId")) {
            invoiceItem.setProductId(productId);
            if(invoiceItem.getDescription() == null || !invoiceItem.getDescription().isEmpty())
            	invoiceItem.setDescription(invoiceItem.getRelatedOne("Product").getString("productName"));
        }
        if (parametersAlreadySet.contains("taxAuthGeoId")) {
            invoiceItem.setTaxAuthGeoId(taxAuthGeoId);
        }
        if (parametersAlreadySet.contains("taxableFlag")) {
            invoiceItem.setTaxableFlag(taxableFlag);
        }
        if (parametersAlreadySet.contains("taxAuthPartyId")) {
            invoiceItem.setTaxAuthPartyId(taxAuthPartyId);
        }
        if (parametersAlreadySet.contains("taxAuthorityRateSeqId")) {
            invoiceItem.setTaxAuthorityRateSeqId(taxAuthorityRateSeqId);
        }
    }

	/**
     * Set default value to <code>InvoiceItem</code> .
     *
     * @throws RepositoryException if an exception occurs
     * @throws EntityNotFoundException if an exception occurs
     * @throws GenericEntityException if an exception occurs
     * @throws ServiceException if an exception occurs
     */
    private void setInvoiceItemDefaultProperties() throws GenericEntityException, RepositoryException, EntityNotFoundException, ServiceException {
        // set the repositories needed
        InvoiceRepositoryInterface invoiceRepository = getDomainsDirectory().getBillingDomain().getInvoiceRepository();
        ProductRepositoryInterface productRepository = getDomainsDirectory().getProductDomain().getProductRepository();
        PurchasingRepositoryInterface purchasingRepository = getDomainsDirectory().getPurchasingDomain().getPurchasingRepository();
        // if productId is not null, then
        if (productId != null) {
            // load product and invoice domain object
            Product product = productRepository.getProductById(productId);
            Invoice invoice = invoiceRepository.getInvoiceById(invoiceId);

            // If invoiceItemTypeId is null, then use the ProductInvoiceItemType
            // to fill in the invoiceItemTypeId (see below for this entity)
            if (invoiceItemTypeId == null) {
                String newInvoiceItemTypeId = invoiceRepository.getInvoiceItemTypeIdForProduct(invoice, product);
                Debug.logInfo("set new InvoiceItemTypeId [" + newInvoiceItemTypeId + "]", MODULE);
                setInvoiceItemTypeId(newInvoiceItemTypeId);
            }

            if (invoice.isSalesInvoice()) {
                // If description is null, then use Product productName to fill in
                // the description
                if (description == null) {
                    setDescription(product.getProductName());
                }
                // If price is null, then call calculateProductPrice and fill in the
                // default name
                if (amount == null) {
                    // get the price of the product
                    String currencyUomId = uomId;
                    if (currencyUomId == null) {
                        currencyUomId = invoice.getCurrencyUomId();
                    }
                    // set amount
                    if (quantity != null) {
                        BigDecimal price = productRepository.getUnitPrice(product, BigDecimal.valueOf(quantity.doubleValue()), invoice.getCurrencyUomId(), invoice.getPartyId());
                        setAmount(price);
                        Debug.logVerbose("Set unitPrice " + this.amount + " for Sale Invoice Item with party [" + invoice.getPartyId() + "], product [" + productId + "], quantity [" + quantity + "] and currency [" + invoice.getCurrencyUomId() + "]", MODULE);
                    } else {
                        setAmount(productRepository.getUnitPrice(product, invoice.getCurrencyUomId()));
                        Debug.logVerbose("Set unitPrice " + this.amount + " for Sale Invoice Item product [" + productId + "], and currency [" + invoice.getCurrencyUomId() + "]", MODULE);
                    }
                }
            } else if (invoice.isPurchaseInvoice()) {
                // if the type is purchase invoice, then use the getSupplierProduct and set:
                // 1. amount = SupplierProduct.lastPrice
                // 2. description = SupplierProduct.supplierProductId + " " + SuppierProduct.supplierProductName
                SupplierProduct supplierProduct = purchasingRepository.getSupplierProduct(invoice.getPartyIdFrom(), productId, invoice.convertToBigDecimal(quantity), invoice.getCurrencyUomId());
                //if supplierProduct not null
                if (supplierProduct != null) {
                    // If description is null and supplierProductName not null, then use SupplierProduct productName to fill in
                    // the description
                    if (UtilValidate.isEmpty(description)) {
                        if (UtilValidate.isNotEmpty(supplierProduct.getSupplierProductName())) {
                            setDescription(supplierProduct.getSupplierProductId() + " " + supplierProduct.getSupplierProductName());
                        } else {
                            setDescription(product.getProductName());
                        }
                    }
                    // If price is null, then use SupplierProduct lastPrice to fill in
                    // default name
                    if (amount == null) {
                        // set amount
                        setAmount(supplierProduct.getLastPrice());
                    }
                }
            }
        }
    }
    
    /**
     * Set parameters of service to <code>InvoiceItem</code>.
     * @param invoiceItem a <code>InvoiceItem</code> value
     * @throws ServiceException 
     * @throws RepositoryException 
     */
    private void crearInvoiceItemLinea(Delegator delegator, String invoiceId, String invoiceItemSeqId, String acctgTransTypeId) throws ServiceException, RepositoryException 
    {	try 
    	{	Debug.logInfo("montoMap: " + montoMap,MODULE);
	    	Debug.logInfo("catalogoCargoContMap: " + catalogoCargoContMap,MODULE);
	    	Debug.logInfo("catalogoAbonoContMap: " + catalogoAbonoContMap,MODULE);
	    	Debug.logInfo("catalogoCargoPresMap: " + catalogoCargoPresMap,MODULE);
	    	Debug.logInfo("catalogoAbonoPresMap: " + catalogoAbonoPresMap,MODULE);
	    	Debug.logInfo("clavePresupuestalMap: " + clavePresupuestalMap,MODULE);
	    	if(montoMap != null)
	    	{	if (parametersAlreadySet.contains("montoMap") && !montoMap.isEmpty()) 
		    	{	for(int i=0; i<montoMap.size(); i++)
		    		{	String catalogoCargo = null;
			    		String catalogoAbono = null;
			    		String descripcion = null;
		    		
		    			    		    			
		    			List<String> orderBy = UtilMisc.toList("secuencia");
			            EntityCondition condicion = EntityCondition.makeCondition(EntityOperator.AND,
			    												EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, acctgTransTypeId),
			    												EntityCondition.makeCondition("secuencia", EntityOperator.EQUALS, Integer.toString(i+1)));					
			            List<GenericValue> lineaContable = delegator.findByCondition("LineaContable", condicion, UtilMisc.toList("catalogoCargo", "catalogoAbono", "descripcion"), orderBy);
			            Debug.logInfo("lineaContable: " + lineaContable,MODULE);
			            if(!lineaContable.isEmpty())
			            {	Iterator<GenericValue> lineaContableIter = lineaContable.iterator();
				        	while (lineaContableIter.hasNext())
				        	{	GenericValue item = lineaContableIter.next();
				        		catalogoCargo = item.getString("catalogoCargo");
				        		catalogoAbono = item.getString("catalogoAbono");
				        		descripcion = item.getString("descripcion");
			            	}  		
			            }
			            String descripcionEspacios = descripcion;
			            descripcion = descripcion.replaceAll(" ", "");
			            if(montoMap.get(descripcion+Integer.toString(i)).toString()!=null && !montoMap.get(descripcion+Integer.toString(i)).toString().equals("") )
			            {	GenericValue invoiceItemLinea = GenericValue.create(delegator.getModelEntity("InvoiceItemLinea"));
				            invoiceItemLinea.set("invoiceId", invoiceId);
			    			invoiceItemLinea.set("invoiceItemSeqId", invoiceItemSeqId);
			    			invoiceItemLinea.set("secuenciaLineaContable", i+1);
			    			invoiceItemLinea.set("descripcion", descripcionEspacios);
			    			invoiceItemLinea.set("catalogoCargo", catalogoCargo);
			    			invoiceItemLinea.set("catalogoAbono", catalogoAbono);
			    			invoiceItemLinea.set("tipoLinea", "CONTABLE");
		    						            	
		    						    		
			    			
			    			if(!montoMap.get(descripcion+Integer.toString(i)).toString().equals("") && montoMap.get(descripcion+Integer.toString(i)).toString()!=null)
			    			{	BigDecimal montoContable = new BigDecimal(montoMap.get(descripcion+Integer.toString(i)).toString());
			    				invoiceItemLinea.set("monto", montoContable);
			    			}
			    			if(catalogoCargoContMap != null)
			    			{	if(!catalogoCargoContMap.get(descripcion+Integer.toString(i)).toString().equals("") && catalogoCargoContMap.get(descripcion+Integer.toString(i)).toString()!=null)
				    			{	invoiceItemLinea.set("valorCatalogoCargo", catalogoCargoContMap.get(descripcion+Integer.toString(i)).toString());	    		
								}
			    			}
			    			if(catalogoAbonoContMap != null)
			    			{	if(!catalogoAbonoContMap.get(descripcion+Integer.toString(i)).toString().equals("") && catalogoAbonoContMap.get(descripcion+Integer.toString(i)).toString()!=null)
				    			{	invoiceItemLinea.set("valorCatalogoAbono", catalogoAbonoContMap.get(descripcion+Integer.toString(i)).toString());
				    			}
			    			}
				    		Debug.logInfo("invoiceItemLinea: " + invoiceItemLinea,MODULE);
							delegator.create(invoiceItemLinea);
			            }
		    		}
		    	}
	    	}
    	}
    	catch (GenericEntityException e) 
    	{	e.printStackTrace();
    	}    	
    }

}