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
package org.opentaps.domain.billing.invoice;

import java.math.BigDecimal;

import org.opentaps.foundation.service.ServiceException;
import org.opentaps.foundation.service.ServiceInterface;

/**
 * POJO service which creates invoice item using the opentaps Service foundation
 * class.
 */
public interface InvoiceItemServiceInterface extends ServiceInterface {

    /**
     * Gets the invoiceItemSeqId created by the service.
     * @return the invoiceItem Seq ID
     */
    public String getInvoiceItemSeqId();

    /**
     * Sets if the services should validate the accounting tags, defaults to <code>false</code>.
     * @param validateAccountingTags a <code>Boolean</code> value
     */
    public void setValidateAccountingTags(Boolean validateAccountingTags);

    /**
     * Sets the required input parameter for service.
     * @param invoiceItemSeqId the invoiceItem seq Id
     */
    public void setInvoiceItemSeqId(String invoiceItemSeqId);

    /**
     * Sets the required input parameter for service.
     * @param invoiceId input parameter
     */
    public void setInvoiceId(String invoiceId);

    /**
     * Sets the required input parameter for service.
     * @param invoiceItemTypeId input parameter
     */
    public void setInvoiceItemTypeId(String invoiceItemTypeId);

    /**
     * Sets the required input parameter for service.
     * @param overrideGlAccountId input parameter
     */
    public void setOverrideGlAccountId(String overrideGlAccountId);

    /**
     * Sets the required input parameter for service.
     * @param inventoryItemId input parameter
     */
    public void setInventoryItemId(String inventoryItemId);

    /**
     * Sets the required input parameter for service.
     * @param productId input parameter
     */
    public void setProductId(String productId);

    /**
     * Sets the required input parameter for service.
     * @param productFeatureId input parameter
     */
    public void setProductFeatureId(String productFeatureId);

    /**
     * Sets the required input parameter for service.
     * @param parentInvoiceId input parameter
     */
    public void setParentInvoiceId(String parentInvoiceId);

    /**
     * Sets the required input parameter for service.
     * @param parentInvoiceItemSeqId input parameter
     */
    public void setParentInvoiceItemSeqId(String parentInvoiceItemSeqId);

    /**
     * Sets the required input parameter for service.
     * @param uomId input parameter
     */
    public void setUomId(String uomId);

    /**
     * Sets the required input parameter for service.
     * @param taxableFlag input parameter
     */
    public void setTaxableFlag(String taxableFlag);

    /**
     * Sets the required input parameter for service.
     * @param quantity input parameter
     */
    public void setQuantity(BigDecimal quantity);

    /**
     * Sets the required input parameter for service.
     * @param amount input parameter
     */
    public void setAmount(BigDecimal amount);

    /**
     * Sets the required input parameter for service.
     * @param description input parameter
     */
    public void setDescription(String description);

    /**
     * Sets the required input parameter for service.
     * @param taxAuthPartyId input parameter
     */
    public void setTaxAuthPartyId(String taxAuthPartyId);

    /**
     * Sets the required input parameter for service.
     * @param taxAuthGeoId input parameter
     */
    public void setTaxAuthGeoId(String taxAuthGeoId);

    /**
     * Sets the required input parameter for service.
     * @param taxAuthorityRateSeqId input parameter
     */
    public void setTaxAuthorityRateSeqId(String taxAuthorityRateSeqId);

    /**
     * Sets the required input parameter for service.
     * @param acctgTagEnumId1 input parameter
     */
    public void setAcctgTagEnumId1(String acctgTagEnumId1);

    /**
     * Sets the required input parameter for service.
     * @param acctgTagEnumId2 input parameter
     */
    public void setAcctgTagEnumId2(String acctgTagEnumId2);

    /**
     * Sets the required input parameter for service.
     * @param acctgTagEnumId3 input parameter
     */
    public void setAcctgTagEnumId3(String acctgTagEnumId3);

    /**
     * Sets the required input parameter for service.
     * @param acctgTagEnumId4 input parameter
     */
    public void setAcctgTagEnumId4(String acctgTagEnumId4);

    /**
     * Sets the required input parameter for service.
     * @param acctgTagEnumId5 input parameter
     */
    public void setAcctgTagEnumId5(String acctgTagEnumId5);

    /**
     * Sets the required input parameter for service.
     * @param acctgTagEnumId6 input parameter
     */
    public void setAcctgTagEnumId6(String acctgTagEnumId6);

    /**
     * Sets the required input parameter for service.
     * @param acctgTagEnumId7 input parameter
     */
    public void setAcctgTagEnumId7(String acctgTagEnumId7);

    /**
     * Sets the required input parameter for service.
     * @param acctgTagEnumId8 input parameter
     */
    public void setAcctgTagEnumId8(String acctgTagEnumId8);

    /**
     * Sets the required input parameter for service.
     * @param acctgTagEnumId9 input parameter
     */
    public void setAcctgTagEnumId9(String acctgTagEnumId9);

    
    /**
     * Sets the required input parameter for service.
     * @param acctgTagEnumId10 input parameter
     */
    public void setAcctgTagEnumId10(String acctgTagEnumId10);
    
    /**
     * Sets the required input parameter for service.
     * @param acctgTagEnumId11 input parameter
     */
    public void setAcctgTagEnumId11(String acctgTagEnumId11);
    
    /**
     * Sets the required input parameter for service.
     * @param acctgTagEnumId12 input parameter
     */
    public void setAcctgTagEnumId12(String acctgTagEnumId12);
    
    /**
     * Sets the required input parameter for service.
     * @param acctgTagEnumId13 input parameter
     */
    public void setAcctgTagEnumId13(String acctgTagEnumId13);
    
    /**
     * Sets the required input parameter for service.
     * @param acctgTagEnumId14 input parameter
     */
    public void setAcctgTagEnumId14(String acctgTagEnumId14);
    
    /**
     * Sets the required input parameter for service.
     * @param acctgTagEnumId15 input parameter
     */
    public void setAcctgTagEnumId15(String acctgTagEnumId15);
    
    
    /**
     * Sets the required input parameter for service.
     * @param clasificacion1 input parameter
     */
    public void setClasificacion1(String clasificacion1);
    
    /**
     * Sets the required input parameter for service.
     * @param clasificacion2 input parameter
     */
    public void setClasificacion2(String clasificacion2);
    
    /**
     * Sets the required input parameter for service.
     * @param clasificacion3 input parameter
     */
    public void setClasificacion3(String clasificacion3);
    
    /**
     * Sets the required input parameter for service.
     * @param clasificacion4 input parameter
     */
    public void setClasificacion4(String clasificacion4);
    
    /**
     * Sets the required input parameter for service.
     * @param clasificacion5 input parameter
     */
    public void setClasificacion5(String clasificacion5);
    
    /**
     * Sets the required input parameter for service.
     * @param clasificacion6 input parameter
     */
    public void setClasificacion6(String clasificacion6);
    
    /**
     * Sets the required input parameter for service.
     * @param clasificacion7 input parameter
     */
    public void setClasificacion7(String clasificacion7);
    
    /**
     * Sets the required input parameter for service.
     * @param clasificacion8 input parameter
     */
    public void setClasificacion8(String clasificacion8);
    
    /**
     * Sets the required input parameter for service.
     * @param clasificacion9 input parameter
     */
    public void setClasificacion9(String clasificacion9);
    
    /**
     * Sets the required input parameter for service.
     * @param clasificacion10 input parameter
     */
    public void setClasificacion10(String clasificacion10);
    
    /**
     * Sets the required input parameter for service.
     * @param clasificacion11 input parameter
     */
    public void setClasificacion11(String clasificacion11);
    
    /**
     * Sets the required input parameter for service.
     * @param clasificacion12 input parameter
     */
    public void setClasificacion12(String clasificacion12);
    
    /**
     * Sets the required input parameter for service.
     * @param clasificacion13 input parameter
     */
    public void setClasificacion13(String clasificacion13);
    
    /**
     * Sets the required input parameter for service.
     * @param clasificacion14 input parameter
     */
    public void setClasificacion14(String clasificacion14);

    /**
     * Sets the required input parameter for service.
     * @param clasificacion15 input parameter
     */
    public void setClasificacion15(String clasificacion15);

    /**
     * 
     * Service to create InvoiceItem.
     * @throws ServiceException if an error occurs
     */
    public void createInvoiceItem() throws ServiceException;

    /**
     * Service to update InvoiceItem.
     * @throws ServiceException if an error occurs
     */
    public void updateInvoiceItem() throws ServiceException;

}
