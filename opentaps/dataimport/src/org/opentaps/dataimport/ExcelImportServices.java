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

package org.opentaps.dataimport;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javolution.util.FastList;

import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.order.finaccount.UtilClavePresupuestal;
import org.ofbiz.service.LocalDispatcher;
import org.opentaps.base.entities.DataImportACompensada;
import org.opentaps.base.entities.DataImportAfectacionEgreso;
import org.opentaps.base.entities.DataImportCompDevNomCont;
import org.opentaps.base.entities.DataImportCompDevNomPres;
import org.opentaps.base.entities.DataImportCustomer;
import org.opentaps.base.entities.DataImportDetRequisicion;
import org.opentaps.base.entities.DataImportDevIng;
import org.opentaps.base.entities.DataImportEjerNomCont;
import org.opentaps.base.entities.DataImportEjerNomPres;
import org.opentaps.base.entities.DataImportFixedAsset;
import org.opentaps.base.entities.DataImportGlAccount;
import org.opentaps.base.entities.DataImportHistorialBienes;
import org.opentaps.base.entities.DataImportIngreso;
import org.opentaps.base.entities.DataImportInventory;
import org.opentaps.base.entities.DataImportLevantaActFijo;
import org.opentaps.base.entities.DataImportOperPatr;
import org.opentaps.base.entities.DataImportOperacionDiaria;
import org.opentaps.base.entities.DataImportOrdenesCobro;
import org.opentaps.base.entities.DataImportOrdenesPago;
import org.opentaps.base.entities.DataImportPagoExterno;
import org.opentaps.base.entities.DataImportParty;
import org.opentaps.base.entities.DataImportPayment;
import org.opentaps.base.entities.DataImportPresupuestoInicia;
import org.opentaps.base.entities.DataImportProduct;
import org.opentaps.base.entities.DataImportRecIng;
import org.opentaps.base.entities.DataImportSaldoInicialAux;
import org.opentaps.base.entities.DataImportSupplier;
import org.opentaps.base.entities.DataImportTag;
import org.opentaps.base.entities.DataImportUser;
import org.opentaps.base.entities.DataImportValidPresup;
import org.opentaps.base.entities.MatrizConcepto;
import org.opentaps.base.entities.MatrizEgreso;
import org.opentaps.base.entities.MatrizIngreso;
import org.opentaps.domain.DomainService;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.domain.party.PartyRepositoryInterface;
import org.opentaps.foundation.entity.Entity;
import org.opentaps.foundation.entity.EntityInterface;
import org.opentaps.foundation.infrastructure.Infrastructure;
import org.opentaps.foundation.infrastructure.InfrastructureException;
import org.opentaps.foundation.infrastructure.User;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.service.ServiceException;

import com.ibm.icu.text.SimpleDateFormat;

/**
 * Common services and helper methods related to Excel files uploading and management.
 */
public final class ExcelImportServices extends DomainService {

    private static final String MODULE = ExcelImportServices.class.getName();

    private static final String EXCEL_PARTY_TAB = "Clasificacion Administrativa";
	private static final String EXCEL_CGUIDE_TAB = "GuiaContable";
	private static final String EXCEL_CGUIDE_TAB_CON = "GuiaContableConcepto";
	private static final String EXCEL_MATRIZ_CONVERSION_EGR_TAB = "Matriz_Conversion_Egresos";
	private static final String EXCEL_MATRIZ_CONVERSION_ING_TAB = "Matriz_Conversion_Ingresos";
	private static final String EXCEL_PRESUPUESTO_INICIAL_TAB = "Presupuesto Inicial";
	private static final String EXCEL_EGRESO_INGRESO_DIARIO_TAB = "OD Egreso e Ingreso";
	private static final String EXCEL_OPERACION_DIARIA_TAB = "ODiaria";
	private static final String EXCEL_ORDENES_PAGO_TAB = "Ordenes de Pago";
	private static final String EXCEL_ORDENES_COBRO_TAB = "Ordenes de Cobro";
	
    private static final String EXCEL_PRODUCT_TAB = "Productos";
    private static final String EXCEL_SUPPLIERS_TAB = "Auxiliares";
    private static final String EXCEL_CUSTOMERS_TAB = "Customers";
    private static final String EXCEL_INVENTORY_TAB = "Inventario";
    private static final String EXCEL_GL_ACCOUNTS_TAB = "Lista de Cuentas";
    private static final String EXCEL_TAG_TAB = "Clasificaciones";
    private static final String EXCEL_PAGOS = "Pagos";
    private static final String EXCEL_SALDO_INICIAL_AUXILIAR = "Saldo Inicial Auxiliar";
    private static final String EXCEL_SALDO_AFECTACION_EGRESO = "Afectacion Egreso";
    private static final String EXCEL_MATRIZ_CONCEPTO_TAB = "Matriz Concepto";
    private static final String EXCEL_USUARIO_TAB = "Usuarios";
    private static final String EXCEL_FIXED_ASSET_TAB = "Activo Fijo";
    private static final String EXCEL_LEVANTA_FIXED_ASSET_TAB = "Levanta Activo Fijo";
    
    private static final String EXCEL_COMP_DEV_NOM_PRES = "Comp. y Dev. de Nomina 1";
    private static final String EXCEL_COMP_DEV_NOM_CONT = "Comp. y Dev. de Nomina 2";
    private static final String EXCEL_EJER_NOM_PRES = "Ejer. de Nomina 1";
    private static final String EXCEL_EJER_NOM_CONT = "Ejer. de Nomina 2";
    private static final String EXCEL_OPER_PATRI = "Operacion Patrimonial";
    private static final String EXCEL_VAL_PRES = "Validacion Presupuestal";
    private static final String EXCEL_INGRESO = "Ingreso";
    private static final String EXCEL_DEV_ING = "Ingreso Devengado";
    private static final String EXCEL_REC_ING = "Ingreso Recaudado";
    private static final String EXCEL_REQUISICION = "Requisicion";
    private static final String EXCEL_COMPENSADA = "A. Compensada";
    private static final String EXCEL_PAGO_EXTERNO = "Pago a Externos";
    
    private static final String EXCEL_HISTORIAL_BIENES = "Historial de bienes";
    
    private static List<String> CUCOP = new ArrayList<String>();    
    
    private static final List<String> EXCEL_TABS = Arrays.asList(EXCEL_PRODUCT_TAB, EXCEL_SUPPLIERS_TAB,
                                                                 EXCEL_CUSTOMERS_TAB, EXCEL_INVENTORY_TAB,
                                                                 EXCEL_GL_ACCOUNTS_TAB, EXCEL_TAG_TAB,EXCEL_PARTY_TAB,
                                                                 EXCEL_MATRIZ_CONVERSION_EGR_TAB, EXCEL_MATRIZ_CONVERSION_ING_TAB,
                                                                 EXCEL_CGUIDE_TAB, EXCEL_CGUIDE_TAB_CON,
                                                                 EXCEL_PRESUPUESTO_INICIAL_TAB, EXCEL_EGRESO_INGRESO_DIARIO_TAB,
                                                                 EXCEL_OPERACION_DIARIA_TAB,EXCEL_ORDENES_PAGO_TAB,EXCEL_ORDENES_COBRO_TAB,
                                                                 EXCEL_PAGOS, EXCEL_SALDO_INICIAL_AUXILIAR, EXCEL_SALDO_AFECTACION_EGRESO,
                                                                 EXCEL_MATRIZ_CONCEPTO_TAB, EXCEL_USUARIO_TAB, EXCEL_FIXED_ASSET_TAB,
                                                                 EXCEL_LEVANTA_FIXED_ASSET_TAB,
                                                                 EXCEL_COMP_DEV_NOM_PRES, EXCEL_COMP_DEV_NOM_CONT,
                                                                 EXCEL_EJER_NOM_PRES, EXCEL_EJER_NOM_CONT,EXCEL_OPER_PATRI, EXCEL_VAL_PRES,
                                                                 EXCEL_INGRESO, EXCEL_DEV_ING, EXCEL_REC_ING, EXCEL_REQUISICION,
                                                                 EXCEL_COMPENSADA, EXCEL_HISTORIAL_BIENES, EXCEL_PAGO_EXTERNO);

    private String uploadedFileName;
    private static Logger logger = Logger.getLogger(ExcelImportServices.class);	

	private Session session;

    /**
     * Default constructor.
     */
    public ExcelImportServices() {
        super();
    }

    /**
     * Creates a new <code>ExcelImportServices</code> instance.
     *
     * @param infrastructure an <code>Infrastructure</code> value
     * @param user an <code>User</code> value
     * @param locale a <code>Locale</code> value
     * @exception ServiceException if an error occurs
     */
    public ExcelImportServices(Infrastructure infrastructure, User user, Locale locale) throws ServiceException {
        super(infrastructure, user, locale);
    }


    
    /**
     * Gets the specified Excel File in the given directory.
     * @param path the path <code>String</code> of the directory to look files into
     * @param fileName the name of the file to find in the path
     * @return the File found
     */
    public File getUploadedExcelFile(String path, String fileName) {
        String name = path;
        if (File.separatorChar == name.charAt(name.length() - 1)) {
            name += File.separatorChar;
        }
        name += fileName;

        if (UtilValidate.isNotEmpty(name)) {
            File file = new File(name);
            if (file.canRead()) {
                return file;
            } else {
                Debug.logWarning("File not found or can't be read " + name, MODULE);
                return null;
            }
        } else {
            Debug.logWarning("No path specified, doing nothing", MODULE);
            return null;
        }
    }

    /**
     * Gets the specified Excel File in the default directory.
     * @param fileName the name of the file to find in the path
     * @return the File found
     */
    public File getUploadedExcelFile(String fileName) {
        return getUploadedExcelFile(CommonImportServices.getUploadPath(), fileName);
    }

    /**
     * Helper method to check if an Excel row is empty.
     * @param row a <code>HSSFRow</code> value
     * @return a <code>boolean</code> value
     */
    public boolean isNotEmpty(Row row) {
        if (row == null) {
            return false;
        }
        String s = row.toString();
        if (s == null) {
            return false;
        }
        return !"".equals(s.trim());
    }

    /**
     * Metodo que lee las cadenas de las celdas del excel y valida los 0, 0.0 y null.
     * @return a <code>String</code> value
     */
    public String readStringCellPoint(Row row, int index) {
		Cell cell = row.getCell(index);
		if (cell == null) {
			return null;
		}

        switch (cell.getCellType()) {
			case Cell.CELL_TYPE_STRING:
			case Cell.CELL_TYPE_BLANK:
				return cell.toString().trim();
			case Cell.CELL_TYPE_NUMERIC:
				Long numeroEntero = Long.valueOf(
					 			BigDecimal.valueOf(
					 					cell.getNumericCellValue()).longValue());
				double diferencia = numeroEntero - BigDecimal.valueOf(cell.getNumericCellValue()).doubleValue(); 
				if(diferencia > 0){
					return BigDecimal.valueOf(
		 					cell.getNumericCellValue()).toString();
				} else {
					if(numeroEntero == 0){
						return null;
					} else {
						return numeroEntero.toString();
					}
				}
			default:
				return null;
		}
	}
    
    /**
     * Obtener la fecha de una celda
     * @param cell
     * @return
     */
    private java.sql.Date getFecha(Cell cell) {
    	
		java.sql.Date fecha = null;

		try {
			switch (cell.getCellType()) {
				case Cell.CELL_TYPE_BLANK:
					break;
	
				default:
					String fechaCelda = cell.toString().trim();
					fecha = UtilDateTime.getDateSqlFromString("dd-MM-yyyy", timeZone, locale, fechaCelda);
					break;
			}
		} catch (Exception e) {
			Debug.logError("Hubo un error al obtener la fecha del renglon "+cell.getRowIndex()+": " + e,MODULE);
		}

		return fecha;
	}
    
    /**
     * Helper method to read a String cell and auto trim it.
     * @param row a <code>HSSFRow</code> value
     * @param index the column index <code>int</code> value which is then casted to a short
     * @return a <code>String</code> value
     */
    public String readStringCell(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) {
            return null;
        }

        // check if cell contains a number
        BigDecimal bd = null;
        try {
            double d = cell.getNumericCellValue();
            bd = BigDecimal.valueOf(d);
        } catch (Exception e) {
            // do nothing
        }

        String s = null;
        if (bd == null) {
            s = cell.toString().trim();
        } else {
            // if cell contains number parse it as long
            s = Long.toString(bd.longValue());
        }

        return s;
    }

    /**
     * Helper method to read a Long cell and auto trim it.
     * @param row a <code>HSSFRow</code> value
     * @param index the column index <code>int</code> value which is then casted to a short
     * @return a <code>Long</code> value
     */
    public Long readLongCell(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) {
            return null;
        }

        BigDecimal bd = BigDecimal.valueOf(cell.getNumericCellValue());
        if (bd == null) {
            return null;
        }
        return bd.longValue();
    }

    /**
     * Helper method to read a BigDecimal cell and auto trim it.
     * @param row a <code>HSSFRow</code> value
     * @param index the column index <code>int</code> value which is then casted to a short
     * @return a <code>BigDecimal</code> value
     */
    public BigDecimal readBigDecimalCell(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
		case Cell.CELL_TYPE_NUMERIC:
			 return BigDecimal.valueOf(cell.getNumericCellValue());

		case Cell.CELL_TYPE_STRING:
			double d = Double.parseDouble(cell.toString());
			return BigDecimal.valueOf(d);
			
		default:
			return null;
		}
       
    }

    /**
     * Takes each row of an Excel sheet and put it into DataImportProduct.
     * @param sheet the Excel sheet
     * @return a <code>Collection</code> of DataImportProduct entities
     * @throws RepositoryException if an error occurs
     * @throws InfrastructureException 
     */
    protected Collection<? extends EntityInterface> createDataImportProducts(Sheet sheet) throws RepositoryException, InfrastructureException {
        int sheetLastRowNumber = sheet.getLastRowNum();
        List<DataImportProduct> products = FastList.newInstance();

        for (int j = 1; j <= sheetLastRowNumber; j++) {
            Row row = sheet.getRow(j);
            if (isNotEmpty(row)) {
                // row index starts at 0 here but is actually 1 in Excel
                int rowNum = row.getRowNum() + 1;
                // read productId from first column "sheet column index
                // starts from 0"
                String id = readStringCellPoint(row, 0);

                if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1 || id.equalsIgnoreCase("productId")) {
                    Debug.logWarning("Row number " + rowNum + " not imported from Products tab: invalid ID value [" + id + "].", MODULE);
                    continue;
                }

                DataImportProduct product = new DataImportProduct();
                product.setRenglon(Long.valueOf(rowNum));
                product.setProductId(id);
                product.setProductName(readStringCellPoint(row, 1));
                product.setInternalName(readStringCellPoint(row, 2));
                product.setProductTypeId(readStringCellPoint(row, 3));
                product.setDescription(readStringCellPoint(row, 4));
                product.setClasificacion(readStringCellPoint(row, 5));
                product.setPrice(readBigDecimalCell(row, 6));
                product.setPriceCurrencyUomId(readStringCellPoint(row, 7));
                product.setSupplierPartyId(readStringCellPoint(row, 8));
                product.setPurchasePrice(readBigDecimalCell(row, 9));
                product.setAreaId(readStringCellPoint(row, 10));
                product.setBrandName(readStringCellPoint(row, 11));
                product.setQuantityUomId(readStringCellPoint(row, 12));
                product.setPrimaryProductCategoryId(readStringCellPoint(row, 13));
                product.setCodigoCucop(readStringCellPoint(row, 14));
                product.setCapitulo(readStringCellPoint(row, 15));
                product.setNombreCapitulo(readStringCellPoint(row, 16));
                product.setConcepto(readStringCellPoint(row, 17));
                product.setNombreConcepto(readStringCellPoint(row, 18));
                product.setPartidaGenerica(readStringCellPoint(row, 19));
                product.setNombrePartidaGenerica(readStringCellPoint(row, 20));
                product.setPartidaEspecifica(readStringCellPoint(row, 21));
                product.setNombrePartidaEspecifica(readStringCellPoint(row, 22));
                product.setPartidaGasto(readStringCellPoint(row, 23));
                product.setNombrePartidaGasto(readStringCellPoint(row, 24));
                product.setCabm(readStringCellPoint(row, 25));
                product.setProcedencia(readStringCellPoint(row, 26));
                product.setPaisOrigen(readStringCellPoint(row, 27));
                product.setGradoContenido(readStringCellPoint(row, 28));
                product.setPatentesDerechos(readStringCellPoint(row, 29));
                product.setModelo(readStringCellPoint(row, 30));
                product.setNumeroParte(readStringCellPoint(row, 31));
                product.setEditorial(readStringCellPoint(row, 32));                
                product.setColeccionId(readStringCellPoint(row, 33));
                product.setIsbn(readStringCellPoint(row, 34));                
                product.setAutor(readStringCellPoint(row, 35));
                product.setAnio(readStringCellPoint(row, 36));
                product.setNumPaginas(readStringCellPoint(row, 37));
                product.setCoediciones(readStringCellPoint(row, 38));   
                product.setUserLoginId(obtenUsuario());
                products.add(product);
            }
        }
        return products;
    }

    /**
     * Take each row of an Excel sheet and put it into DataImportSupplier.
     * @param sheet the Excel sheet
     * @return a <code>Collection</code> of DataImportSupplier entities
     * @throws RepositoryException if an error occurs
     * @throws InfrastructureException 
     */
    protected Collection<? extends EntityInterface> createDataImportSuppliers(Sheet sheet) throws RepositoryException, InfrastructureException {

        List<DataImportSupplier> suppliers = FastList.newInstance();
        int sheetLastRowNumber = sheet.getLastRowNum();
        for (int j = 1; j <= sheetLastRowNumber; j++) {
            Row row = sheet.getRow(j);
            if (isNotEmpty(row)) {
                // row index starts at 0 here but is actually 1 in Excel
                int rowNum = row.getRowNum() + 1;
                // read supplierId from first column "sheet column index
                // starts from 0"
                String id = readStringCellPoint(row, 0);

                if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1 || id.equalsIgnoreCase("supplierId")) {
                    Debug.logWarning("Row number " + rowNum + " not imported from Suppliers tab: invalid ID value [" + id + "].", MODULE);
                    continue;
                }

                DataImportSupplier supplier = new DataImportSupplier();
                supplier.setRenglon(Long.valueOf(rowNum));
                supplier.setSupplierId(id);
                supplier.setSupplierName(readStringCellPoint(row, 1));
                supplier.setTipo(readStringCellPoint(row, 2));
                supplier.setRegimenId(readStringCellPoint(row, 3));
                supplier.setTamanioAuxiliarId(readStringCellPoint(row, 4));
                supplier.setSectorEconomicoId(readStringCellPoint(row, 5));
                supplier.setOrigenCapitalId(readStringCellPoint(row, 6));
                supplier.setAddress1(readStringCellPoint(row, 7));
                supplier.setAddress2(readStringCellPoint(row, 8));
                supplier.setCity(readStringCellPoint(row, 9));
                supplier.setStateProvinceGeoId(readStringCellPoint(row, 10));
                supplier.setPostalCode(readStringCellPoint(row, 11));
                supplier.setCountryGeoId(readStringCellPoint(row, 12));
                supplier.setPrimaryPhoneCountryCode(readStringCellPoint(row, 13));
                supplier.setPrimaryPhoneAreaCode(readStringCellPoint(row, 14));
                supplier.setPrimaryPhoneNumber(readStringCellPoint(row, 15));
                supplier.setFederalTaxId(readStringCellPoint(row, 16));
                supplier.setRupc(readStringCellPoint(row, 17));
                supplier.setGiroEmpresa(readStringCellPoint(row, 18));
                supplier.setNacionalExtranjero(readStringCellPoint(row, 19));
                supplier.setCorreoElectronico(readStringCellPoint(row, 20));
                supplier.setEmailAddress(readStringCellPoint(row, 20));
                supplier.setPaginaWeb(readStringCellPoint(row, 21));
                supplier.setWebAddress(readStringCellPoint(row, 21));
                supplier.setNumActaConstitutiva(readStringCellPoint(row, 22));
                supplier.setNombreNotarioPublico(readStringCellPoint(row, 23));
                supplier.setNumNotaria(readStringCellPoint(row, 24));
                supplier.setNombreApoderadoLegal(readStringCellPoint(row, 25));
                supplier.setTipoIdApoderadoLegal(readStringCellPoint(row, 26));
                supplier.setNumIdApoderadoLegal(readStringCellPoint(row, 27));
                supplier.setNumTestimonioApoderado(readStringCellPoint(row, 28));
                supplier.setNombreNotarioApoderado(readStringCellPoint(row, 29));
                supplier.setNumNotariaApoderado(readStringCellPoint(row, 30));
                supplier.setCurpApoderadoLegal(readStringCellPoint(row, 31));
                supplier.setNombreRepLegal(readStringCellPoint(row, 32));
                supplier.setTipoIdRepLegal(readStringCellPoint(row, 33));
                supplier.setNumIdRepLegal(readStringCellPoint(row, 34));
                supplier.setNumTestimonioRepLegal(readStringCellPoint(row, 35));
                supplier.setNombreNotarioRepLegal(readStringCellPoint(row, 36));
                supplier.setNumNotariaRepLegal(readStringCellPoint(row, 37));
                supplier.setCurpRepLegal(readStringCellPoint(row, 38));
                supplier.setNombreAdministrador(readStringCellPoint(row, 39));
                supplier.setTipoIdAdministrador(readStringCellPoint(row, 40));
                supplier.setNumIdAdministrador(readStringCellPoint(row, 41));
                supplier.setNumTestimonioAdministrador(readStringCellPoint(row, 42));
                supplier.setNombreNotarioAdministrador(readStringCellPoint(row, 43));
                supplier.setNumNotariaAdministrador(readStringCellPoint(row, 44));
                supplier.setCurpAdministrador(readStringCellPoint(row, 45));
                supplier.setSaldoInicial(readBigDecimalCell(row, 46));
                supplier.setUserLoginId(obtenUsuario());
                suppliers.add(supplier);
            }
        }

        return suppliers;
    }

    /**
     * Take each row of an Excel sheet and put it into DataImportCustomer.
     * @param sheet the Excel sheet
     * @return a <code>Collection</code> of DataImportCustomer entities
     * @throws RepositoryException if an error occurs
     * @throws InfrastructureException 
     */
    protected Collection<? extends EntityInterface> createDataImportCustomers(Sheet sheet) throws RepositoryException, InfrastructureException {

        List<DataImportCustomer> customers = FastList.newInstance();
        int sheetLastRowNumber = sheet.getLastRowNum();
        for (int j = 1; j <= sheetLastRowNumber; j++) {
            Row row = sheet.getRow(j);
            if (isNotEmpty(row)) {
                // row index starts at 0 here but is actually 1 in Excel
                int rowNum = row.getRowNum() + 1;
                // read customerId from first column "sheet column index
                // starts from 0"
                String id = readStringCellPoint(row, 0);

                if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1 || id.equalsIgnoreCase("customerId")) {
                    Debug.logWarning("Row number " + rowNum + " not imported from Customers tab: invalid ID value [" + id + "].", MODULE);
                    continue;
                }

                DataImportCustomer customer = new DataImportCustomer();
                customer.setCustomerId(id);
                int rowCount = 1;  // keep track of the row 
                customer.setCompanyName(this.readStringCellPoint(row, rowCount++));
                customer.setFirstName(this.readStringCellPoint(row, rowCount++));
                customer.setLastName(this.readStringCellPoint(row, rowCount++));
                customer.setAttnName(this.readStringCellPoint(row, rowCount++));
                customer.setAddress1(this.readStringCellPoint(row, rowCount++));
                customer.setAddress2(this.readStringCellPoint(row, rowCount++));
                customer.setCity(this.readStringCellPoint(row, rowCount++));
                customer.setStateProvinceGeoId(this.readStringCellPoint(row, rowCount++));
                customer.setPostalCode(this.readStringCellPoint(row, rowCount++));
                customer.setPostalCodeExt(this.readStringCellPoint(row, rowCount++));
                customer.setCountryGeoId(this.readStringCellPoint(row, rowCount++));
                customer.setPrimaryPhoneCountryCode(this.readStringCellPoint(row, rowCount++));
                customer.setPrimaryPhoneAreaCode(this.readStringCellPoint(row, rowCount++));
                customer.setPrimaryPhoneNumber(this.readStringCellPoint(row, rowCount++));
                customer.setPrimaryPhoneExtension(this.readStringCellPoint(row, rowCount++));
                customer.setSecondaryPhoneCountryCode(this.readStringCellPoint(row, rowCount++));
                customer.setSecondaryPhoneAreaCode(this.readStringCellPoint(row, rowCount++));
                customer.setSecondaryPhoneNumber(this.readStringCellPoint(row, rowCount++));
                customer.setSecondaryPhoneExtension(this.readStringCellPoint(row, rowCount++));
                customer.setFaxCountryCode(this.readStringCellPoint(row, rowCount++));
                customer.setFaxAreaCode(this.readStringCellPoint(row, rowCount++));
                customer.setFaxNumber(this.readStringCellPoint(row, rowCount++));
                customer.setDidCountryCode(this.readStringCellPoint(row, rowCount++));
                customer.setDidAreaCode(this.readStringCellPoint(row, rowCount++));
                customer.setDidNumber(this.readStringCellPoint(row, rowCount++));
                customer.setDidExtension(this.readStringCellPoint(row, rowCount++));
                customer.setEmailAddress(this.readStringCellPoint(row, rowCount++));
                customer.setWebAddress(this.readStringCellPoint(row, rowCount++));
                customer.setDiscount(this.readBigDecimalCell(row, rowCount++));
                customer.setPartyClassificationTypeId(this.readStringCellPoint(row, rowCount++));
                customer.setCreditCardNumber(this.readStringCellPoint(row, rowCount++));
                customer.setCreditCardExpDate(this.readStringCellPoint(row, rowCount++));
                customer.setOutstandingBalance(this.readBigDecimalCell(row, rowCount++));
                customer.setCreditLimit(this.readBigDecimalCell(row, rowCount++));
                customer.setCurrencyUomId(this.readStringCellPoint(row, rowCount++));
                customer.setDisableShipping(this.readStringCellPoint(row, rowCount++));
                customer.setNetPaymentDays(this.readLongCell(row, rowCount++));
                customer.setShipToCompanyName(this.readStringCellPoint(row, rowCount++));
                customer.setShipToFirstName(this.readStringCellPoint(row, rowCount++));
                customer.setShipToLastName(this.readStringCellPoint(row, rowCount++));
                customer.setShipToAttnName(this.readStringCellPoint(row, rowCount++));
                customer.setShipToAddress1(this.readStringCellPoint(row, rowCount++));
                customer.setShipToAddress2(this.readStringCellPoint(row, rowCount++));
                customer.setShipToCity(this.readStringCellPoint(row, rowCount++));
                customer.setShipToStateProvinceGeoId(this.readStringCellPoint(row, rowCount++));
                customer.setShipToPostalCode(this.readStringCellPoint(row, rowCount++));
                customer.setShipToPostalCodeExt(this.readStringCellPoint(row, rowCount++));
                customer.setShipToStateProvGeoName(this.readStringCellPoint(row, rowCount++));
                customer.setShipToCountryGeoId(this.readStringCellPoint(row, rowCount++));
                customer.setNote(this.readStringCellPoint(row, rowCount++));
                customer.setUserLoginId(obtenUsuario());
                customers.add(customer);
            }
        }

        return customers;
    }

    /**
     * Take each row of an Excel sheet and put it into DataImportInventory.
     * @param sheet the Excel sheet
     * @return a <code>Collection</code> of DataImportInventory entities
     * @throws RepositoryException if an error occurs
     * @throws InfrastructureException 
     */
    protected Collection<? extends EntityInterface> createDataImportInventory(Sheet sheet) throws RepositoryException, InfrastructureException {

        List<DataImportInventory> inventory = FastList.newInstance();
        int sheetLastRowNumber = sheet.getLastRowNum();
        for (int j = 1; j <= sheetLastRowNumber; j++) {
            Row row = sheet.getRow(j);
            if (isNotEmpty(row)) {
                // row index starts at 0 here but is actually 1 in Excel
                int rowNum = row.getRowNum() + 1;
                // read itemId from first column "sheet column index
                // starts from 0"
                String id = readStringCellPoint(row, 0);

                if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1 || id.equalsIgnoreCase("itemId")) {
                    Debug.logWarning("Row number " + rowNum + " not imported from Inventory tab: invalid ID value [" + id + "].", MODULE);
                    continue;
                }

                DataImportInventory inventoryItem = new DataImportInventory();
                inventoryItem.setItemId(id);
                inventoryItem.setProductId(this.readStringCellPoint(row, 1));
                inventoryItem.setFacilityId(this.readStringCellPoint(row, 2));
                inventoryItem.setAvailableToPromise(this.readBigDecimalCell(row, 3));
                inventoryItem.setOnHand(this.readBigDecimalCell(row, 4));
                inventoryItem.setMinimumStock(this.readBigDecimalCell(row, 5));
                inventoryItem.setReorderQuantity(this.readBigDecimalCell(row, 6));
                inventoryItem.setDaysToShip(this.readBigDecimalCell(row, 7));
                inventoryItem.setInventoryValue(this.readBigDecimalCell(row, 8));
                inventoryItem.setFechaContable(this.getFechaHHMMSScustom(row.getCell(9)));
                inventoryItem.setUserLoginId(obtenUsuario());
                inventory.add(inventoryItem);
            }
        }

        return inventory;
    }

    /**
     * Take each row of an Excel sheet and put it into DataImportGlAccount.
     * @param sheet the Excel sheet
     * @return a <code>Collection</code> of DataImportGlAccount entities
     * @throws RepositoryException if an error occurs
     * @throws InfrastructureException 
     */
    protected Collection<? extends EntityInterface> createDataImportGlAccounts(Sheet sheet) throws RepositoryException, InfrastructureException {

        List<DataImportGlAccount> glAccounts = FastList.newInstance();
        int sheetLastRowNumber = sheet.getLastRowNum();
        for (int j = 1; j <= sheetLastRowNumber; j++) {
            Row row = sheet.getRow(j);
            if (isNotEmpty(row)) {
                // row index starts at 0 here but is actually 1 in Excel
                int rowNum = row.getRowNum() + 1;
                // read glAccountrId from first column "sheet column index
                // starts from 0"
                String id = readStringCellPoint(row, 0);

                if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1 || id.equalsIgnoreCase("glAccountId")) {
                    Debug.logWarning("Row number " + rowNum + " not imported from GL Accounts tab: invalid ID value [" + id + "].", MODULE);
                    continue;
                }

                DataImportGlAccount glAccount = new DataImportGlAccount();
                int rowCount = 1; // keep track of the row
				glAccount.setGlAccountId(id);
				glAccount.setParentGlAccountId(this.readStringCellPoint(row, rowCount++));
				glAccount.setClassification(this.readStringCellPoint(row, rowCount++));
				glAccount.setAccountName(this.readStringCellPoint(row, rowCount++));
				glAccount.setDescripcionCorta(this.readStringCellPoint(row, rowCount++));
				glAccount.setSiglas(this.readStringCellPoint(row, rowCount++));
				glAccount.setTipoCuenta(this.readStringCellPoint(row, rowCount++));
				glAccount.setSaldoinicial(this.readBigDecimalCell(row, rowCount++));
				glAccount.setUserLoginId(obtenUsuario());
				//Falta AUX
				glAccounts.add(glAccount);
            }
        }

        return glAccounts;
    }
    
    /**
	 * Take each row of an Excel sheet and put it into DataImportTag.
	 * 
	 * @param sheet
	 *            the Excel sheet
	 * @return a <code>Collection</code> of DataImportTag entities
	 * @throws RepositoryException
	 *             if an error occurs
	 */
	protected Collection<? extends EntityInterface> createDataImportTag(
			Sheet sheet) throws RepositoryException {

		LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
				.getLedgerDomain().getLedgerRepository();

		List<DataImportTag> tags = FastList.newInstance();
		// List<DataImportCustomer> customers = FastList.newInstance();
		int sheetLastRowNumber = sheet.getLastRowNum();
		try {
			for (int j = 1; j <= sheetLastRowNumber; j++) {
				Row row = sheet.getRow(j);
				if (isNotEmpty(row)) {
					// row index starts at 0 here but is actually 1 in Excel
					int rowNum = row.getRowNum() + 1;
					// read catalogoId from first column "sheet column index
					// starts from 0"
					String id = readStringCellPoint(row, 1);

					if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1
							|| id.equalsIgnoreCase("ID")) {
						Debug.log(
								"Row number "
										+ rowNum
										+ " no se importo correctamente, tab Tag. Id no valido ["
										+ id + "].", MODULE);
						continue;
					}

					String numConsecutivo = ledger_repo
							.getNextSeqId("DataImportTag");
					logger.debug("Numero Consecutivo tag " + numConsecutivo);
					DataImportTag tag = new DataImportTag();
					tag.setType(this.readStringCellPoint(row, 0));
					tag.setSequenceNum(readStringCellPoint(row, 1));
					tag.setId(numConsecutivo);
					tag.setName(this.readStringCellPoint(row, 2));
					tag.setDescription(this.readStringCellPoint(row, 3));
					tag.setParentId(this.readStringCellPoint(row, 4));
					tag.setNivel(this.readStringCellPoint(row, 5));
					tag.setFechaInicio(getFecha(row.getCell(6)));
					tag.setFechaFin(getFecha(row.getCell(7)));
					tag.setNode(this.readStringCellPoint(row, 8));
					tag.setUserLoginId(obtenUsuario());
					tags.add(tag);

				}
			}

			return tags;
		} catch (Exception e) {
			logger.debug("Error: " + e);
			return null;
		}
	}
	
	protected Collection<? extends EntityInterface> createDataImportParty(
			Sheet sheet) throws RepositoryException, InfrastructureException {

		List<DataImportParty> listparty = FastList.newInstance();
		// List<DataImportCustomer> customers = FastList.newInstance();
		int sheetLastRowNumber = sheet.getLastRowNum();
		for (int j = 1; j <= sheetLastRowNumber; j++) {
			Row row = sheet.getRow(j);
			if (isNotEmpty(row)) {
				// row index starts at 0 here but is actually 1 in Excel
				int rowNum = row.getRowNum() + 1;
				// read catalogoId from first column "sheet column index
				// starts from 0"
				String id = readStringCellPoint(row, 2);

				if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1
						|| id.equalsIgnoreCase("catalogoId")) {
					Debug.logWarning(
							"Fila no. "
									+ rowNum
									+ " no se importo correctamente, tab Clasificacion Administrativa. Id no valido ["
									+ id + "].", MODULE);
					continue;
				}

				DataImportParty party = new DataImportParty();
				int rowCount = 1;
				party.setGroupName(this.readStringCellPoint(row, 0));
				party.setGroupNameLocal(this.readStringCellPoint(row, rowCount++));
				party.setExternalId(readStringCellPoint(row, rowCount++));
				party.setParentExternalId(this.readStringCellPoint(row, rowCount++));
				party.setNivel(this.readStringCellPoint(row, rowCount++));
				//party.setRol(this.readStringCellPoint(row, rowCount++));
				party.setRfc(this.readStringCellPoint(row, rowCount++)); 	
				//party.setMoneda(this.readStringCellPoint(row, rowCount++));
				party.setState(this.readStringCellPoint(row, rowCount++));
				party.setUserLoginId(obtenUsuario());
				listparty.add(party);
			}
		}

		return listparty;
	}
	
	/**
	 * Take each row of an Excel sheet and put it into
	 * DataImportMatrizConversionEgresos.
	 * 
	 * @param sheet
	 *            the Excel sheet
	 * @return a <code>Collection</code> of DataImportMatrizConversionEgresos
	 *         entities
	 * @throws RepositoryException
	 *             if an error occurs
	 * @throws InfrastructureException 
	 */
	protected Collection<? extends EntityInterface> createMatrizEgreso(
			Sheet sheet) throws RepositoryException, InfrastructureException {
		LocalDispatcher dispatcher = this.getDomainsDirectory()
				.getLedgerDomain().getInfrastructure().getDispatcher();
		Delegator delegator = dispatcher.getDelegator();
		
		List<MatrizEgreso> matrizConversionEgr = FastList.newInstance();
		int sheetLastRowNumber = sheet.getLastRowNum();
		for (int j = 1; j <= sheetLastRowNumber; j++) {
			Row row = sheet.getRow(j);
			if (isNotEmpty(row)) {
				// row index starts at 0 here but is actually 1 in Excel
				int rowNum = row.getRowNum() + 1;
				// read idRegistroEgr from first column "sheet column index
				// starts from 0"
				String id = readStringCellPoint(row, 0);
				Debug.log("id: " + id);

				if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1
						|| id.equalsIgnoreCase("idRegistroEgr")) {
					Debug.logInfo("Fila no. "+ rowNum + " no se import\u00f3 correctamente, tab Matr\u00edz de conversi\u00f3n Egresos. Id no v\u00e1lido ["+ id + "].", MODULE);
					continue;
				}

				MatrizEgreso matrizEgresos = new MatrizEgreso();
				matrizEgresos.setMatrizEgresoId(id);
				matrizEgresos.setMatrizId(this.readStringCellPoint(row, 1));
				matrizEgresos.setCog(this.readStringCellPoint(row, 2));
				matrizEgresos.setNombreCog(this.readStringCellPoint(row, 3));
				matrizEgresos.setTipoGasto(this.readStringCellPoint(row, 4));
				matrizEgresos.setCaracteristicas(this.readStringCellPoint(row, 5));
				matrizEgresos.setMedioPago(this.readStringCellPoint(row, 6));
				String cuentaCargo = this.readStringCellPoint(row, 7);
				String cuentaAbono = this.readStringCellPoint(row, 9);				
				try 
				{	if ((validarCuentasRegistro(delegator, cuentaCargo, cuentaAbono)) == false)
					{ 	Debug.log("if validarCuentasRegistro");
						matrizEgresos.setImportStatusId("DATAIMP_FAILED");
						matrizEgresos.setImportError("Alguna de las cuentas no es de registro o no esta registrada en el plan de cuentas"
								+ "\n para la combinacion Matriz "+ matrizEgresos.getMatrizId()
								+" Cog "+ matrizEgresos.getCog() + "Tipo de Gasto "+ matrizEgresos.getTipoGasto());
					}
					else
					{	Debug.log("else validarCuentasRegistro");
						matrizEgresos.setImportStatusId("DATAIMP_IMPORTED");				
					}
				} 
				catch (GenericEntityException e) 
				{  	Debug.log("Error Subir");
					logger.debug("Error: " + e);
				}				
				matrizEgresos.setCargo(this.readStringCellPoint(row, 7));
				matrizEgresos.setCuentaCargo(this.readStringCellPoint(row, 8));
				matrizEgresos.setAbono(this.readStringCellPoint(row, 9));
				matrizEgresos.setCuentaAbono(this.readStringCellPoint(row, 10));				
				matrizEgresos.setUserLoginId(obtenUsuario());
				matrizConversionEgr.add(matrizEgresos);

				// DataImportMatrizEgr me= new DataImportMatrizEgr();

			}
		}

		return matrizConversionEgr;
	}

	/**
	 * Take each row of an Excel sheet and put it into
	 * DataImportMatrizConversionIngresos.
	 * 
	 * @param sheet
	 *            the Excel sheet
	 * @return a <code>Collection</code> of DataImportMatrizConversionIngresos
	 *         entities
	 * @throws RepositoryException
	 *             if an error occurs
	 * @throws InfrastructureException 
	 */
	protected Collection<? extends EntityInterface> createMatrizIngreso(
			Sheet sheet) throws RepositoryException, InfrastructureException {
		LocalDispatcher dispatcher = this.getDomainsDirectory()
				.getLedgerDomain().getInfrastructure().getDispatcher();
		
		Delegator delegator = dispatcher.getDelegator();
		List<MatrizIngreso> matrizConversionIng = FastList.newInstance();
		int sheetLastRowNumber = sheet.getLastRowNum();
		for (int j = 1; j <= sheetLastRowNumber; j++) {
			Row row = sheet.getRow(j);
			if (isNotEmpty(row)) {
				// row index starts at 0 here but is actually 1 in Excel
				int rowNum = row.getRowNum() + 1;
				// read glAccountrId from first column "sheet column index
				// starts from 0"
				String id = readStringCellPoint(row, 0);

				if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1
						|| id.equalsIgnoreCase("idRegistroIng")) {
					Debug.logInfo(
							"Row number " + rowNum + " no se import\u00f3 correctamente, tab Matr\u00edz de conversi\u00f3n Egresos. Id no v\u00e1lido ["+ id + "].", MODULE);
					continue;
				}

				MatrizIngreso matrizIngresos = new MatrizIngreso();
				matrizIngresos.setMatrizIngresoId(id);
				matrizIngresos.setMatrizId(this.readStringCellPoint(row, 1));
				matrizIngresos.setCri(this.readStringCellPoint(row, 2));
				matrizIngresos.setNombreCri(this.readStringCellPoint(row, 3));
				matrizIngresos.setCaracteristicas(this.readStringCellPoint(row, 4));
				matrizIngresos.setMedioPago(this.readStringCellPoint(row, 5));
				String cuentaCargo = this.readStringCellPoint(row, 6);
				String cuentaAbono = this.readStringCellPoint(row, 8);				
				try 
				{	if ((validarCuentasRegistro(delegator, cuentaCargo, cuentaAbono)) == false)
					{ 	matrizIngresos.setImportStatusId("DATAIMP_FAILED");
						matrizIngresos.setImportError("Alguna de las cuentas no es de registro o no esta registrada en el plan de cuentas"
								+ "\n para la combinacion Matriz "+ matrizIngresos.getMatrizId()
								+" Cri "+ matrizIngresos.getCri());
					}
					else
					{	matrizIngresos.setImportStatusId("DATAIMP_IMPORTED");				
					}
				} 
				catch (GenericEntityException e) 
				{	logger.debug("Error: " + e);
				}
				matrizIngresos.setCargo(this.readStringCellPoint(row, 6));
				matrizIngresos.setCuentaCargo(this.readStringCellPoint(row, 7));
				matrizIngresos.setAbono(this.readStringCellPoint(row, 8));
				matrizIngresos.setCuentaAbono(this.readStringCellPoint(row, 9));
				matrizIngresos.setUserLoginId(obtenUsuario());
				matrizConversionIng.add(matrizIngresos);

				// DataImportMatrizEgr me= new DataImportMatrizEgr();

			}
		}

		return matrizConversionIng;
	}
	
	protected Collection<? extends EntityInterface> createMatrizConcepto(
			Sheet sheet) throws RepositoryException, InfrastructureException {
		LocalDispatcher dispatcher = this.getDomainsDirectory()
				.getLedgerDomain().getInfrastructure().getDispatcher();
		Delegator delegator = dispatcher.getDelegator();
		List<MatrizConcepto> matrizConcepto = FastList.newInstance();
		int sheetLastRowNumber = sheet.getLastRowNum();
		for (int j = 1; j <= sheetLastRowNumber; j++) {
			Row row = sheet.getRow(j);
			if (isNotEmpty(row)) {
				// row index starts at 0 here but is actually 1 in Excel
				int rowNum = row.getRowNum() + 1;
				// read idRegistroEgr from first column "sheet column index
				// starts from 0"
				int rowCount = 0; // keep track of the row
				String id = readStringCellPoint(row, rowCount);

				if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1
						|| id.equalsIgnoreCase("CodigoConcepto")) {
					Debug.logInfo("Fila no. "+ rowNum + " no se import\u00f3 correctamente, tab Matr\u00edz de conceptos. Id no v\u00e1lido ["+ id + "].", MODULE);
					continue;
				}

				MatrizConcepto concepto = new MatrizConcepto();
				
				concepto.setCodigo(this.readStringCellPoint(row, rowCount++));
				concepto.setDescripcion(this.readStringCellPoint(row, rowCount++));
				concepto.setCuentaCargo(this.readStringCellPoint(row, rowCount++));
				concepto.setCuentaAbono(this.readStringCellPoint(row, rowCount++));
				concepto.setFlagAux(this.readStringCellPoint(row, rowCount++).equalsIgnoreCase("Y") ? "Y":"N");				
				
				try 
				{	if ((validarCuentasRegistro(delegator, concepto.getCuentaCargo(), concepto.getCuentaAbono())) == false)
					{ 	Debug.log("if validarCuentasRegistro");
						concepto.setImportStatusId("DATAIMP_FAILED");
						concepto.setImportError("Alguna de las cuentas no es de registro o no esta registrada en el plan de cuentas");
					}
					else
					{	Debug.log("else validarCuentasRegistro");
						concepto.setImportStatusId("DATAIMP_IMPORTED");				
					}
				} 
				catch (GenericEntityException e) 
				{  	Debug.log("Error Subir");
					logger.debug("Error: " + e);
				}				
				concepto.setUserLoginId(obtenUsuario());
				matrizConcepto.add(concepto);
			}
		}

		return matrizConcepto;
	}
	
	/**
	 * Take each row of an Excel sheet and put it into
	 * DataImportPresupuestoIngreso. Author: Jesus Rodrigo Ruiz Merlin
	 * 
	 * @param sheet
	 *            the Excel sheet
	 * @return a <code>Collection</code> of DataImportPresupuestoInicial
	 *         entities
	 * @throws RepositoryException
	 *             if an error occurs
	 * @throws InfrastructureException 
	 */
	protected Collection<? extends EntityInterface> createDataImportPresupuestoInicial(
			Sheet sheet) throws RepositoryException, InfrastructureException {

		List<DataImportPresupuestoInicia> pIniciales = FastList.newInstance();
		int sheetLastRowNumber = sheet.getLastRowNum();
		
		LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
				.getLedgerDomain().getLedgerRepository();
		
		for (int j = 1; j <= sheetLastRowNumber; j++) {
			Row row = sheet.getRow(j);
			if (isNotEmpty(row)) {
				// row index starts at 0 here but is actually 1 in Excel
				int rowNum = row.getRowNum() + 1;
				String valida = readStringCellPoint(row, 0);

				if (UtilValidate.isEmpty(valida) || valida.indexOf(" ") > -1
						|| valida.equalsIgnoreCase("Clasificacion 1")) {
					Debug.logWarning(
							"Fila no. "
									+ rowNum
									+ " no se importo correctamente, tab Presupuesto Inicial. Id no valido ["
									+ valida + "].", MODULE);
					continue;
				}

				DataImportPresupuestoInicia pInicial = new DataImportPresupuestoInicia();
				int rowCount = 1; // keep track of the row
				pInicial.setRenglon(Long.valueOf(row.getRowNum()+1));
				pInicial.setClasificacion1(valida);
				pInicial.setClasificacion2(this.readStringCellPoint(row, rowCount++));
				pInicial.setClasificacion3(this.readStringCellPoint(row, rowCount++));
				pInicial.setClasificacion4(this.readStringCellPoint(row, rowCount++));
				pInicial.setClasificacion5(this.readStringCellPoint(row, rowCount++));
				pInicial.setClasificacion6(this.readStringCellPoint(row, rowCount++));
				pInicial.setClasificacion7(this.readStringCellPoint(row, rowCount++));
				pInicial.setClasificacion8(this.readStringCellPoint(row, rowCount++));
				pInicial.setClasificacion9(this.readStringCellPoint(row, rowCount++));
				pInicial.setClasificacion10(this.readStringCellPoint(row, rowCount++));
				pInicial.setClasificacion11(this.readStringCellPoint(row, rowCount++));
				pInicial.setClasificacion12(this.readStringCellPoint(row, rowCount++));
				pInicial.setClasificacion13(this.readStringCellPoint(row, rowCount++));
				pInicial.setClasificacion14(this.readStringCellPoint(row, rowCount++));
				pInicial.setClasificacion15(this.readStringCellPoint(row, rowCount++));
				pInicial.setClavePresupuestal(getClavePresupuestal(pInicial));
				pInicial.setEnero(this.readBigDecimalCell(row, rowCount++));
				pInicial.setFebrero(this.readBigDecimalCell(row, rowCount++));
				pInicial.setMarzo(this.readBigDecimalCell(row, rowCount++));
				pInicial.setAbril(this.readBigDecimalCell(row, rowCount++));
				pInicial.setMayo(this.readBigDecimalCell(row, rowCount++));
				pInicial.setJunio(this.readBigDecimalCell(row, rowCount++));
				pInicial.setJulio(this.readBigDecimalCell(row, rowCount++));
				pInicial.setAgosto(this.readBigDecimalCell(row, rowCount++));
				pInicial.setSeptiembre(this.readBigDecimalCell(row, rowCount++));
				pInicial.setOctubre(this.readBigDecimalCell(row, rowCount++));
				pInicial.setNoviembre(this.readBigDecimalCell(row, rowCount++));
				pInicial.setDiciembre(this.readBigDecimalCell(row, rowCount++));
				//pInicial.setIdPresInicial(Integer.toString(id));
				pInicial.setIdPresInicial(ledger_repo.getNextSeqId("DataImportPresupuestoInicia"));
				Debug.log("idPresupuesto.- " + pInicial.getIdPresInicial());
				pInicial.setUserLoginId(obtenUsuario());
				pIniciales.add(pInicial);
			}
		}

		return pIniciales;
	}

	/**
	 * Take each row of an Excel sheet and put it into
	 * DataImportPresupuestoIngreso. Author: Jesus Rodrigo Ruiz Merlin
	 * 
	 * @param sheet
	 *            the Excel sheet
	 * @return a <code>Collection</code> of DataImportOperacionDiaria entities
	 * @throws RepositoryException
	 *             if an error occurs
	 * @throws InfrastructureException 
	 */
	protected Collection<? extends EntityInterface> createDataImportOperacionDiaria(
			Sheet sheet) throws RepositoryException, InfrastructureException {

		List<DataImportOperacionDiaria> operacionesDiarias = FastList
				.newInstance();
		int sheetLastRowNumber = sheet.getLastRowNum();
		for (int j = 1; j <= sheetLastRowNumber; j++) {
			Row row = sheet.getRow(j);
			if (isNotEmpty(row)) {
				// row index starts at 0 here but is actually 1 in Excel
				int rowNum = row.getRowNum() + 1;
				String tipo = readStringCellPoint(row, 0);

				if (UtilValidate.isEmpty(tipo) || tipo.indexOf(" ") > -1
						|| tipo.equalsIgnoreCase("Tipo Documento")) {
					Debug.logWarning(
							"Fila no. "
									+ rowNum
									+ " no se importo correctamente, tab Operacion Diaria. Id no valido ["
									+ tipo + "].", MODULE);
					continue;
				}

				DataImportOperacionDiaria operacionDiaria = new DataImportOperacionDiaria();
				int rowCount = 1; // keep track of the row
				operacionDiaria.setIdTipoDoc(tipo);
				operacionDiaria.setFechaRegistro(getFechaHHMMSS(row
						.getCell(rowCount++)));
				operacionDiaria.setFechaContable(getFechaHHMMSS(row
						.getCell(rowCount++)));
				operacionDiaria.setMonto(this.readBigDecimalCell(row,
						rowCount++));
				operacionDiaria.setIdEntidadContable(this.readStringCellPoint(row,
						rowCount++));
				operacionDiaria.setIdEntidadEjecutora(this.readStringCellPoint(
						row, rowCount++));
				operacionDiaria
						.setUsuario(this.readStringCellPoint(row, rowCount++));				
				operacionDiaria.setRefDoc(this.readStringCellPoint(row, rowCount++));
				operacionDiaria.setSecuencia(this.readStringCellPoint(row,
						rowCount++));
				operacionDiaria.setConcepto(this
						.readStringCellPoint(row, rowCount++));
				operacionDiaria.setSubconcepto(this.readStringCellPoint(row,
						rowCount++));
				operacionDiaria.setUserLoginId(obtenUsuario());
				operacionesDiarias.add(operacionDiaria);
			}
		}

		return operacionesDiarias;
	}
	

	/**
	 * Toma cada renglon del excel y lo guarda dentro de 
	 * DataImportOrdenesPago. Author: JSCF
	 * 
	 * @param sheet
	 *            the Excel sheet
	 * @return a <code>Collection</code> of DataImportOrdenesPago entities
	 * @throws RepositoryException
	 *             if an error occurs
	 * @throws InfrastructureException 
	 */
	protected Collection<? extends EntityInterface> createDataImportOrdenesDePago(
			Sheet sheet) throws RepositoryException, InfrastructureException {

		List<DataImportOrdenesPago> ordenesPago = FastList
				.newInstance();
		int sheetLastRowNumber = sheet.getLastRowNum();
		for (int j = 1; j <= sheetLastRowNumber; j++) {
			Row row = sheet.getRow(j);
			if (isNotEmpty(row)) {
				// row index starts at 0 here but is actually 1 in Excel
				int rowNum = row.getRowNum() + 1;
				String invoiceId = readStringCellPoint(row, 0);

				if (UtilValidate.isEmpty(invoiceId) || invoiceId.indexOf(" ") > -1 || invoiceId.equalsIgnoreCase("invoiceId")) {
					Debug.logWarning(
							"Fila no. "
									+ rowNum
									+ " no se importo correctamente, tab Ordenes de Compra. Id no valido ["
									+ invoiceId + "].", MODULE);
					continue;
				}

				DataImportOrdenesPago ordenPago = new DataImportOrdenesPago();
				int rowCount = 1; // keep track of the row
				ordenPago.setInvoiceId(invoiceId);
				ordenPago.setPartyId(this.readStringCellPoint(row, rowCount++));
				ordenPago.setPartyIdFrom(this.readStringCellPoint(row, rowCount++));
				ordenPago.setIdTipoDoc(this.readStringCellPoint(row, rowCount++));
				ordenPago.setDescriptionInvoice(this.readStringCellPoint(row, rowCount++));
				ordenPago.setInvoiceMessage(this.readStringCellPoint(row, rowCount++));
				ordenPago.setInvoiceDate(this.getFechaHHMMSScustom(row.getCell(rowCount++)));				
				ordenPago.setDueDate(this.getFechaHHMMSScustom(row.getCell(rowCount++)));
				ordenPago.setReferenceNumber(this.readStringCellPoint(row, rowCount++));
				ordenPago.setInvoiceItemSeqId(this.readStringCellPoint(row, rowCount++));
				ordenPago.setProductId(this.readStringCellPoint(row, rowCount++));
				ordenPago.setUomId(this.readStringCellPoint(row, rowCount++));
				ordenPago.setQuantity(this.readBigDecimalCell(row, rowCount++));
				ordenPago.setAmount(this.readBigDecimalCell(row, rowCount++));
				ordenPago.setDescriptionItem(this.readStringCellPoint(row, rowCount++));				
				ordenPago.setClasificacion1(this.readStringCellPoint(row, rowCount++));
				ordenPago.setClasificacion2(this.readStringCellPoint(row, rowCount++));
				ordenPago.setClasificacion3(this.readStringCellPoint(row, rowCount++));
				ordenPago.setClasificacion4(this.readStringCellPoint(row, rowCount++));
				ordenPago.setClasificacion5(this.readStringCellPoint(row, rowCount++));
				ordenPago.setClasificacion6(this.readStringCellPoint(row, rowCount++));
				ordenPago.setClasificacion7(this.readStringCellPoint(row, rowCount++));
				ordenPago.setClasificacion8(this.readStringCellPoint(row, rowCount++));
				ordenPago.setClasificacion9(this.readStringCellPoint(row, rowCount++));
				ordenPago.setClasificacion10(this.readStringCellPoint(row, rowCount++));
				ordenPago.setClasificacion11(this.readStringCellPoint(row, rowCount++));
				ordenPago.setClasificacion12(this.readStringCellPoint(row, rowCount++));
				ordenPago.setClasificacion13(this.readStringCellPoint(row, rowCount++));
				ordenPago.setClasificacion14(this.readStringCellPoint(row, rowCount++));
				ordenPago.setClasificacion15(this.readStringCellPoint(row, rowCount++));
				ordenPago.setUserLoginId(obtenUsuario());
				ordenesPago.add(ordenPago);
			}
		}

		return ordenesPago;
	}
	
	/**
	 * Toma cada renglon del excel y lo guarda dentro de 
	 * DataImportOrdenesCobro. Author: JSCF
	 * 
	 * @param sheet
	 *            the Excel sheet
	 * @return a <code>Collection</code> of DataImportOrdenesCobro entities
	 * @throws RepositoryException
	 *             if an error occurs
	 * @throws InfrastructureException 
	 */
	protected Collection<? extends EntityInterface> createDataImportOrdenesCobro(
			Sheet sheet) throws RepositoryException, InfrastructureException {

		List<DataImportOrdenesCobro> ordenesCobro = FastList
				.newInstance();
		int sheetLastRowNumber = sheet.getLastRowNum();
		for (int j = 1; j <= sheetLastRowNumber; j++) {
			Row row = sheet.getRow(j);
			if (isNotEmpty(row)) {
				// row index starts at 0 here but is actually 1 in Excel
				int rowNum = row.getRowNum() + 1;
				String invoiceId = readStringCellPoint(row, 0);

				if (UtilValidate.isEmpty(invoiceId) || invoiceId.indexOf(" ") > -1 || invoiceId.equalsIgnoreCase("invoiceId")) {
					Debug.logWarning(
							"Fila no. "
									+ rowNum
									+ " no se importo correctamente, tab Ordenes de Compra. Id no valido ["
									+ invoiceId + "].", MODULE);
					continue;
				}

				DataImportOrdenesCobro ordenCobro = new DataImportOrdenesCobro();
				int rowCount = 1; // keep track of the row
				ordenCobro.setInvoiceId(invoiceId);
				ordenCobro.setPartyId(this.readStringCellPoint(row, rowCount++));
				ordenCobro.setPartyIdFrom(this.readStringCellPoint(row, rowCount++));
				ordenCobro.setIdTipoDoc(this.readStringCellPoint(row, rowCount++));
				ordenCobro.setDescriptionInvoice(this.readStringCellPoint(row, rowCount++));
				ordenCobro.setInvoiceMessage(this.readStringCellPoint(row, rowCount++));
				ordenCobro.setInvoiceDate(this.getFechaHHMMSScustom(row.getCell(rowCount++)));
				ordenCobro.setDueDate(this.getFechaHHMMSScustom(row.getCell(rowCount++)));
				ordenCobro.setReferenceNumber(this.readStringCellPoint(row, rowCount++));
				ordenCobro.setInvoiceItemSeqId(this.readStringCellPoint(row, rowCount++));
				ordenCobro.setProductId(this.readStringCellPoint(row, rowCount++));
				ordenCobro.setUomId(this.readStringCellPoint(row, rowCount++));
				ordenCobro.setQuantity(this.readBigDecimalCell(row, rowCount++));
				ordenCobro.setAmount(this.readBigDecimalCell(row, rowCount++));
				ordenCobro.setDescriptionItem(this.readStringCellPoint(row, rowCount++));				
				ordenCobro.setClasificacion1(this.readStringCellPoint(row, rowCount++));
				ordenCobro.setClasificacion2(this.readStringCellPoint(row, rowCount++));
				ordenCobro.setClasificacion3(this.readStringCellPoint(row, rowCount++));
				ordenCobro.setClasificacion4(this.readStringCellPoint(row, rowCount++));
				ordenCobro.setClasificacion5(this.readStringCellPoint(row, rowCount++));
				ordenCobro.setClasificacion6(this.readStringCellPoint(row, rowCount++));
				ordenCobro.setClasificacion7(this.readStringCellPoint(row, rowCount++));
				ordenCobro.setClasificacion8(this.readStringCellPoint(row, rowCount++));
				ordenCobro.setClasificacion9(this.readStringCellPoint(row, rowCount++));
				ordenCobro.setClasificacion10(this.readStringCellPoint(row, rowCount++));
				ordenCobro.setClasificacion11(this.readStringCellPoint(row, rowCount++));
				ordenCobro.setClasificacion12(this.readStringCellPoint(row, rowCount++));
				ordenCobro.setClasificacion13(this.readStringCellPoint(row, rowCount++));
				ordenCobro.setClasificacion14(this.readStringCellPoint(row, rowCount++));
				ordenCobro.setClasificacion15(this.readStringCellPoint(row, rowCount++));
				ordenCobro.setUserLoginId(obtenUsuario());
				ordenesCobro.add(ordenCobro);
			}
		}

		return ordenesCobro;
	}	
	
	/**
	 * Take each row of an Excel sheet and put it into
	 * DataImportPagos
	 * 
	 * @param sheet
	 *            the Excel sheet
	 * @return a <code>Collection</code> of DataImportPagos
	 *         entities
	 * @throws RepositoryException
	 *             if an error occurs
	 * @throws InfrastructureException 
	 */
	protected Collection<? extends EntityInterface> createDataImportPagos(
			Sheet sheet) throws RepositoryException, InfrastructureException {
		List<DataImportPayment> dataImportPagos = FastList.newInstance();
		LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
				.getLedgerDomain().getLedgerRepository();
		int sheetLastRowNumber = sheet.getLastRowNum();
		for (int j = 1; j <= sheetLastRowNumber; j++) {
			Row row = sheet.getRow(j);
			if (isNotEmpty(row)) {
				// row index starts at 0 here but is actually 1 in Excel
				int rowNum = row.getRowNum() + 1;
				// read idRegistroEgr from first column "sheet column index
				// starts from 0"
				String id = readStringCellPoint(row, 0);

				if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1
						|| id.equalsIgnoreCase("Organizacion REALIZA pago")) {
					Debug.logInfo(
							"Fila no. "
									+ rowNum
									+ " no se importo correctamente, tab Pagos. Id de Entidad Contable no valido ["
									+ id + "].", MODULE);
					continue;
				}

				DataImportPayment dataImportPago = new DataImportPayment();
				dataImportPago.setIdRegistroPago(ledger_repo.getNextSeqId("DataImportPayment"));
				dataImportPago.setPartyIdFrom((id));
				dataImportPago.setPartyIdTo(this.readStringCellPoint(row, 1));
				dataImportPago.setTipoPago(this.readStringCellPoint(row, 2));
				dataImportPago.setTipoDocumento(this.readStringCellPoint(row, 3));
				dataImportPago.setFechaContable(getFechaHHMMSScustom(row.getCell(4)));													
				dataImportPago.setDescripcionPago(this.readStringCellPoint(row, 5));
				dataImportPago.setReferenciaPago(this.readStringCellPoint(row, 6));
				dataImportPago.setBanco(this.readStringCellPoint(row, 7));
				dataImportPago.setCuentaBancaria(this.readStringCellPoint(row, 8));
				dataImportPago.setMoneda(this.readStringCellPoint(row, 9));
				dataImportPago.setIdFactura(this.readStringCellPoint(row, 10));
				dataImportPago.setReferenciaFactura(this.readStringCellPoint(row, 11));
				dataImportPago.setMontoAplicado(this.readBigDecimalCell(row, 12));
				dataImportPago.setDescripcion(this.readStringCellPoint(row, 13));
				dataImportPago.setUserLoginId(obtenUsuario());
				dataImportPagos.add(dataImportPago);

				// DataImportMatrizEgr me= new DataImportMatrizEgr();

			}
		}

		return dataImportPagos;
	}
	
	protected Collection<? extends EntityInterface> createDataImportSaldoInicialAuxiliar(
			Sheet sheet) throws RepositoryException, InfrastructureException {

		List<DataImportSaldoInicialAux> listSaldos = FastList.newInstance();
		int sheetLastRowNumber = sheet.getLastRowNum();
		for (int j = 1; j <= sheetLastRowNumber; j++) {
			Row row = sheet.getRow(j);
			if (isNotEmpty(row)) {
				
				// row index starts at 0 here but is actually 1 in Excel
				int rowNum = row.getRowNum() + 1;
				// read catalogoId from first column "sheet column index
				// starts from 0"
				String id = readStringCellPoint(row, 0);

				if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1
						|| id.equalsIgnoreCase("auxiliarId")) {
					Debug.logWarning(
							"Fila no. "
									+ rowNum
									+ " no se importo correctamente, tab Saldo Inicial Auxiliar. Id no valido ["
									+ id + "].", MODULE);
					continue;
				}

				DataImportSaldoInicialAux saldos = new DataImportSaldoInicialAux();
				int rowCount = 1;
				saldos.setRenglon(Long.valueOf(rowNum));
				saldos.setIdAuxiliar(id);
				saldos.setTipo(this.readStringCellPoint(row, rowCount++));
				saldos.setOrganizationId(this.readStringCellPoint(row, rowCount++));
				saldos.setAuxiliarProducto(this.readStringCellPoint(row, rowCount++));
				saldos.setMonto(this.readBigDecimalCell(row, rowCount++));
				saldos.setPeriodo(UtilDateTime.getMonthStart(this.getFechaHHMMSScustom(row.getCell(rowCount++))));
				saldos.setUserLoginId(obtenUsuario());
				listSaldos.add(saldos);
			}
		}

		return listSaldos;
	}
	
	/**
	 * Take each row of an Excel sheet and put it into
	 * DataImportAfectacionEgreso. Author: Jesus Rodrigo Ruiz Merlin
	 * 
	 * @param sheet
	 *            the Excel sheet
	 * @return a <code>Collection</code> of DataImportPresupuestoInicial
	 *         entities
	 * @throws RepositoryException
	 *             if an error occurs
	 * @throws InfrastructureException 
	 */
	protected Collection<? extends EntityInterface> createDataImportAfectacionEgreso(
			Sheet sheet) throws RepositoryException, InfrastructureException {

		List<DataImportAfectacionEgreso> afectaciones = FastList.newInstance();
		int sheetLastRowNumber = sheet.getLastRowNum();
		LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
				.getLedgerDomain().getLedgerRepository();
		for (int j = 1; j <= sheetLastRowNumber; j++) {
			Row row = sheet.getRow(j);
			if (isNotEmpty(row)) {
				// row index starts at 0 here but is actually 1 in Excel
				int rowNum = row.getRowNum() + 1;
				String valida = readStringCellPoint(row, 0);

				if (UtilValidate.isEmpty(valida) || valida.indexOf(" ") > -1
						|| valida.equalsIgnoreCase("Clasificacion 1")) {
					Debug.logWarning(
							"Fila no. "
									+ rowNum
									+ " no se importo correctamente, tab Afectacion Egreso. Id no valido ["
									+ valida + "].", MODULE);
					continue;
				}

				DataImportAfectacionEgreso afectacionEgreso = new DataImportAfectacionEgreso();
				int rowCount = 1; // keep track of the row
				
				afectacionEgreso.setClasificacion1(valida);
				afectacionEgreso.setClasificacion2(this.readStringCellPoint(row, rowCount++));
				afectacionEgreso.setClasificacion3(this.readStringCellPoint(row, rowCount++));
				afectacionEgreso.setClasificacion4(this.readStringCellPoint(row, rowCount++));
				afectacionEgreso.setClasificacion5(this.readStringCellPoint(row, rowCount++));
				afectacionEgreso.setClasificacion6(this.readStringCellPoint(row, rowCount++));
				afectacionEgreso.setClasificacion7(this.readStringCellPoint(row, rowCount++));
				afectacionEgreso.setClasificacion8(this.readStringCellPoint(row, rowCount++));
				afectacionEgreso.setClasificacion9(this.readStringCellPoint(row, rowCount++));
				afectacionEgreso.setClasificacion10(this.readStringCellPoint(row, rowCount++));
				afectacionEgreso.setClasificacion11(this.readStringCellPoint(row, rowCount++));
				afectacionEgreso.setClasificacion12(this.readStringCellPoint(row, rowCount++));
				afectacionEgreso.setClasificacion13(this.readStringCellPoint(row, rowCount++));
				afectacionEgreso.setClasificacion14(this.readStringCellPoint(row, rowCount++));
				afectacionEgreso.setClasificacion15(this.readStringCellPoint(row, rowCount++));
				afectacionEgreso.setMonto(this.readBigDecimalCell(row, rowCount++));
				afectacionEgreso.setAuxiliar(this.readStringCellPoint(row, rowCount++));
				afectacionEgreso.setProducto(this.readStringCellPoint(row, rowCount++));
				afectacionEgreso.setUserLoginId(obtenUsuario());
				//afectacionEgreso.setId(Integer.toString(id));
				afectacionEgreso.setId(ledger_repo.getNextSeqId("DataImportPresupuestoInicia"));
				afectaciones.add(afectacionEgreso);
			}
		}

		return afectaciones;
	}

	protected Collection<? extends EntityInterface> createDataImportUsuario(
			Sheet sheet) throws RepositoryException, InfrastructureException {

		List<DataImportUser> listUsuarios = FastList.newInstance();
		int sheetLastRowNumber = sheet.getLastRowNum();
		for (int j = 1; j <= sheetLastRowNumber; j++) {
			Row row = sheet.getRow(j);
			if (isNotEmpty(row)) {
				
				// row index starts at 0 here but is actually 1 in Excel
				int rowNum = row.getRowNum() + 1;
				// read catalogoId from first column "sheet column index
				// starts from 0"
				String id = readStringCellPoint(row, 0);

				if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1
						|| id.equalsIgnoreCase("usuarioId")) {
					Debug.logWarning(
							"Fila no. "
									+ rowNum
									+ " no se importo correctamente, tab Usuario. Id no valido ["
									+ id + "].", MODULE);
					continue;
				}

				DataImportUser usuario = new DataImportUser();
				int rowCount = 1;
				usuario.setUsuarioId(id);
				usuario.setUsuarioLogId(this.readStringCellPoint(row, rowCount++));
				usuario.setCurrentPassword(this.readStringCellPoint(row, rowCount++));
				usuario.setVerifyCurrentPassword(this.readStringCellPoint(row, rowCount++));
				usuario.setPasswordHint(this.readStringCellPoint(row, rowCount++));
				usuario.setFirstName(this.readStringCellPoint(row, rowCount++));
				usuario.setMiddleName(this.readStringCellPoint(row, rowCount++));
				usuario.setLastName(this.readStringCellPoint(row, rowCount++));
				usuario.setPersonalTitle(this.readStringCellPoint(row, rowCount++));
				usuario.setGender(this.readStringCellPoint(row, rowCount++));
				usuario.setBirthDate(getFechaHHMMSScustom(row.getCell(9)));
				rowCount++;
				usuario.setMaritalStatus(this.readStringCellPoint(row, rowCount++));
				usuario.setSocialSecurityNumber(this.readStringCellPoint(row, rowCount++));
				usuario.setPassportNumber(this.readStringCellPoint(row, rowCount++));
				usuario.setPassportExpireDate(getFechaHHMMSScustom(row.getCell(13)));
				rowCount++;
				usuario.setTotalYearsWorkExperience(this.readBigDecimalCell(row, rowCount++));
				usuario.setComments(this.readStringCellPoint(row, rowCount++));
				usuario.setEmploymentStatusEnumId(this.readStringCellPoint(row, rowCount++));
				usuario.setResidenceStatusEnumId(this.readStringCellPoint(row, rowCount++));
				usuario.setOccupation(this.readStringCellPoint(row, rowCount++));
				usuario.setParentPartyId(this.readStringCellPoint(row, rowCount++));
				usuario.setPreferredCurrencyUomId(this.readStringCellPoint(row, rowCount++));
				usuario.setDescription(this.readStringCellPoint(row, rowCount++));
				usuario.setStatusId(this.readStringCellPoint(row, rowCount++));
				usuario.setAreaId(this.readStringCellPoint(row, rowCount++));
				usuario.setPartyId(this.readStringCellPoint(row, rowCount++));
				usuario.setInfoString(this.readStringCellPoint(row, rowCount++));
				usuario.setGroupId(this.readStringCellPoint(row, rowCount++));
				usuario.setRoleTypeId(this.readStringCellPoint(row, rowCount++));
				usuario.setEmplPositionId(this.readStringCellPoint(row, rowCount++));
				usuario.setUserLoginId(obtenUsuario());
				listUsuarios.add(usuario);
			}
		}

		return listUsuarios;
	}
	
	protected Collection<? extends EntityInterface> createDataImportFixedAsset(Sheet sheet) throws RepositoryException, InfrastructureException {
		List<DataImportFixedAsset> listFixedAsset = FastList.newInstance();
		Transaction imp_tx1 = null;
		LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory().getLedgerDomain().getLedgerRepository();

		
		int sheetLastRowNumber = sheet.getLastRowNum();
		for (int j = 1; j <= sheetLastRowNumber; j++) {
			Row row = sheet.getRow(j);
			if (isNotEmpty(row)) {
				imp_tx1 = null;				
				// row index starts at 0 here but is actually 1 in Excel
				int rowNum = row.getRowNum() + 1;
				Debug.log("rowNum: " + rowNum);
				// read fixedAssetId from first column "sheet column index
				// starts from 0"
				String id = readStringCell(row, 1);

				if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1|| id.equalsIgnoreCase("CUCOP")) {
					Debug.logWarning(
							"Fila no. "
									+ rowNum
									+ " no se importo correctamente, tab Numero de Inventario no valido ["
									+ id + "].", MODULE);
					continue;
				}

				DataImportFixedAsset fixedAsset = new DataImportFixedAsset();
				int rowCount = 0;
								
				fixedAsset.setFixedAssetId(ledger_repo.getNextSeqId("DataImportFixedAsset"));
				fixedAsset.setNumeroExpediente(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setProduct(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setFixedAssetName(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setCaracteristicas(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setDenominacionPartidaGen(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setIdActivoAnterior(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setMarca(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setColor(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setModelo(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setPlaca(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setNumTarjetaCirculacion(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setUsoVehiculo(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setNumeroMotor(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setTipoUnidadVehiculoId(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setSerialNumber(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setFactura(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setFechaFactura (getFechaHHMMSScustom(row.getCell(rowCount++)));
				fixedAsset.setTalon (this.readStringCellPoint(row, rowCount++));
				fixedAsset.setCheque (this.readStringCellPoint(row, rowCount++));
				fixedAsset.setFechaPago (getFechaHHMMSScustom(row.getCell(rowCount++)));
				fixedAsset.setMoneda(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setProveedor(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setDateAcquired(getFechaHHMMSScustom(row.getCell(rowCount++)));
				fixedAsset.setPurchaseCost(this.readBigDecimalCell(row, rowCount++));
				fixedAsset.setTipoAdjudicacion (this.readStringCellPoint(row, rowCount++));
				fixedAsset.setAniosVidaUtil(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setVencimientoGarantia(getFechaHHMMSScustom(row.getCell(rowCount++)));
				fixedAsset.setComments(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setClase(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setStatusId(this.readStringCellPoint(row, rowCount++));	
				fixedAsset.setObsolescencia(getFechaHHMMSScustom(row.getCell(rowCount++)));
				fixedAsset.setEstadoFisico(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setOrganizationPartyId(this.readStringCellPoint(row, rowCount++));			
				fixedAsset.setArea(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setSubUnidadResponsable (this.readStringCellPoint(row, rowCount++));
				fixedAsset.setUbicacionRapidaId(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setFacilityId(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setPartyId(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setFechaInicioResguardo(getFechaHHMMSScustom(row.getCell(rowCount++)));
				fixedAsset.setFechaFinResguardo(getFechaHHMMSScustom(row.getCell(rowCount++)));
				fixedAsset.setFechaAsignacion(getFechaHHMMSScustom(row.getCell(rowCount++)));
				fixedAsset.setComentarioResguardo(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setPolizaSeguro(this.readStringCellPoint(row, rowCount++));
				fixedAsset.setFechaInicioSeguro(getFechaHHMMSScustom(row.getCell(rowCount++)));
				fixedAsset.setFechaFinSeguro(getFechaHHMMSScustom(row.getCell(rowCount++)));
				fixedAsset.setDomicilio(this.readStringCellPoint(row, (rowCount++)));
				fixedAsset.setMunicipio(this.readStringCellPoint(row, (rowCount++)));
				fixedAsset.setLocalidad(this.readStringCellPoint(row, (rowCount++)));
				fixedAsset.setEjido(this.readStringCellPoint(row, (rowCount++)));
				fixedAsset.setTipoTerreno(this.readStringCellPoint(row, (rowCount++)));
				fixedAsset.setTipoDocumentoLegalPropiedad(this.readStringCellPoint(row, (rowCount++)));
				fixedAsset.setDescDocumentoLegalPropiedad(this.readStringCellPoint(row, (rowCount++)));
				fixedAsset.setOrigenAdquisicion(this.readStringCellPoint(row, (rowCount++)));
				fixedAsset.setFormaAdquisicion(this.readStringCellPoint(row, (rowCount++)));
				fixedAsset.setTipoEmisorTituloPropiedad(this.readStringCellPoint(row, (rowCount++)));
				fixedAsset.setValorContruccion(this.readBigDecimalCell(row, (rowCount++)));
				fixedAsset.setValorRazonable(this.readBigDecimalCell(row, (rowCount++)));
				fixedAsset.setFechaAvaluo(getFechaHHMMSScustom(row.getCell(rowCount++)));
				fixedAsset.setFechaTituloPropiedad(getFechaHHMMSScustom(row.getCell(rowCount++)));
				fixedAsset.setClaveCatastral(this.readStringCellPoint(row, (rowCount++)));
				fixedAsset.setFechaInicioClaveCatastral(getFechaHHMMSScustom(row.getCell(rowCount++)));
				fixedAsset.setVencimientoClaveCatastral(getFechaHHMMSScustom(row.getCell(rowCount++)));
				fixedAsset.setSuperficieTerreno(this.readBigDecimalCell(row, (rowCount++)));
				fixedAsset.setSuperficieConstruccion(this.readBigDecimalCell(row, (rowCount++)));
				fixedAsset.setSuperficieDisponible(this.readBigDecimalCell(row, (rowCount++)));
				fixedAsset.setFolioRppc(this.readStringCellPoint(row, (rowCount++)));
				fixedAsset.setFechaInscripcionRppc(getFechaHHMMSScustom(row.getCell(rowCount++)));
				fixedAsset.setCiudadInscripcionRPPC(this.readStringCellPoint(row, (rowCount++)));
				fixedAsset.setFechaIncorporacionInventario(getFechaHHMMSScustom(row.getCell(rowCount++)));
				fixedAsset.setDepreciation(this.readBigDecimalCell(row, rowCount++));
				fixedAsset.setUserLoginId(obtenUsuario());
				imp_tx1 = this.session.beginTransaction();
				ledger_repo.createOrUpdate(fixedAsset);
				imp_tx1.commit();							
				//listFixedAsset.add(fixedAsset);
			}
		}
		Debug.log("CUCOP: " + CUCOP);
		CUCOP = new ArrayList<String>();
		

		return listFixedAsset;
	}
	
	protected Collection<? extends EntityInterface> createDataImportLevantaFixedAsset(Sheet sheet) throws RepositoryException, InfrastructureException {
		List<DataImportLevantaActFijo> listLevantaFixedAsset = FastList.newInstance();
		Transaction imp_tx1 = null;
		LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory().getLedgerDomain().getLedgerRepository();

		
		int sheetLastRowNumber = sheet.getLastRowNum();
		for (int j = 1; j <= sheetLastRowNumber; j++) {
			Row row = sheet.getRow(j);
			if (isNotEmpty(row)) {
				imp_tx1 = null;				
				// row index starts at 0 here but is actually 1 in Excel
				int rowNum = row.getRowNum() + 1;
				Debug.log("rowNum: " + rowNum);
				// read fixedAssetId from first column "sheet column index
				// starts from 0"
				String id = readStringCell(row, 0);

				if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1|| id.equalsIgnoreCase("CUCOP")) {
					Debug.logWarning(
							"Fila no. "
									+ rowNum
									+ " no se importo correctamente, tab Numero de Inventario no valido ["
									+ id + "].", MODULE);
					continue;
				}

				DataImportLevantaActFijo levantaFixedAsset = new DataImportLevantaActFijo();
				int rowCount = 0;
								
				levantaFixedAsset.setFixedAssetId(this.readStringCellPoint(row, rowCount++));
				levantaFixedAsset.setUbicacion(this.readStringCellPoint(row, rowCount++));
				levantaFixedAsset.setUserLoginId(obtenUsuario());
				imp_tx1 = this.session.beginTransaction();
				ledger_repo.createOrUpdate(levantaFixedAsset);
				imp_tx1.commit();							
				//listFixedAsset.add(fixedAsset);
			}
		}
		Debug.log("CUCOP: " + CUCOP);
		CUCOP = new ArrayList<String>();
		

		return listLevantaFixedAsset;
	}
	
	protected Collection<? extends EntityInterface> createDataImportCompDevNomPres(
			Sheet sheet) throws RepositoryException, InfrastructureException {

		List<DataImportCompDevNomPres> nomina = FastList.newInstance();
		int sheetLastRowNumber = sheet.getLastRowNum();
		
		LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
				.getLedgerDomain().getLedgerRepository();
		
		for (int j = 1; j <= sheetLastRowNumber; j++) {
			Row row = sheet.getRow(j);
			if (isNotEmpty(row)) {
				// row index starts at 0 here but is actually 1 in Excel
				int rowNum = row.getRowNum() + 1;
				String valida = readStringCellPoint(row, 0);

				if (UtilValidate.isEmpty(valida) || valida.indexOf(" ") > -1
						|| valida.equalsIgnoreCase("Clasificacion 1")) {
					Debug.logWarning(
							"Fila no. "
									+ rowNum
									+ " no se importo correctamente, tab Comp. Dev. Nom. 1 Id no valido ["
									+ valida + "].", MODULE);
					continue;
				}

				DataImportCompDevNomPres presupuesto = new DataImportCompDevNomPres();
				int rowCount = 1; // keep track of the row
				presupuesto.setRenglon(Long.valueOf(row.getRowNum()+1));
				presupuesto.setClasificacion1(valida);
				presupuesto.setClasificacion2(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion3(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion4(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion5(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion6(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion7(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion8(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion9(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion10(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion11(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion12(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion13(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion14(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion15(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClavePresupuestal(getClavePresupuestal(presupuesto));
				presupuesto.setAuxiliarCargo(this.readStringCellPoint(row, rowCount++));
				presupuesto.setAuxiliarAbono(this.readStringCellPoint(row, rowCount++));
				presupuesto.setMonto(this.readBigDecimalCell(row, rowCount++));
				presupuesto.setIdCompDevNominaPres(ledger_repo.getNextSeqId("DataImportCompDevNomPres"));
				presupuesto.setUserLoginId(obtenUsuario());
				nomina.add(presupuesto);
			}
		}

		return nomina;
	}
	
	protected Collection<? extends EntityInterface> createDataImportCompDevNomCont(
			Sheet sheet) throws RepositoryException, InfrastructureException {

		List<DataImportCompDevNomCont> nomina = FastList.newInstance();
		int sheetLastRowNumber = sheet.getLastRowNum();
		
		LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
				.getLedgerDomain().getLedgerRepository();
		
		for (int j = 1; j <= sheetLastRowNumber; j++) {
			Row row = sheet.getRow(j);
			if (isNotEmpty(row)) {
				// row index starts at 0 here but is actually 1 in Excel
				int rowNum = row.getRowNum() + 1;
				String valida = readStringCellPoint(row, 0);

				if (UtilValidate.isEmpty(valida) || valida.equalsIgnoreCase("Descripcion de linea")) {
					Debug.logWarning(
							"Fila no. "
									+ rowNum
									+ " no se importo correctamente, tab Comp. Dev. Nom. 2 Id no valido ["
									+ valida + "].", MODULE);
					continue;
				}

				DataImportCompDevNomCont contable = new DataImportCompDevNomCont();
				int rowCount = 1; // keep track of the row
				contable.setRenglon(Long.valueOf(row.getRowNum()+1));
				contable.setDescripcionLinea(valida);
				contable.setAuxiliarCargo(this.readStringCellPoint(row, rowCount++));
				contable.setAuxiliarAbono(this.readStringCellPoint(row, rowCount++));
				contable.setMonto(this.readBigDecimalCell(row, rowCount++));
				//contable.setIdCompDevNomina(Integer.toString(id));
				contable.setIdCompDevNominaCont(ledger_repo.getNextSeqId("DataImportCompDevNomCont"));
				contable.setUserLoginId(obtenUsuario());
				nomina.add(contable);
			}
		}

		return nomina;
	}
	
	/**
	 * Guarda el excel de ejercido de nomina presupuestal en la tabla DataImportEjerNomPres
	 * @param sheet
	 * @return
	 * @throws RepositoryException
	 * @throws InfrastructureException
	 */
	protected Collection<? extends EntityInterface> createDataImportEjerNomPres(
			Sheet sheet) throws RepositoryException, InfrastructureException {

		List<DataImportEjerNomPres> nomina = FastList.newInstance();
		int sheetLastRowNumber = sheet.getLastRowNum();
		
		LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
				.getLedgerDomain().getLedgerRepository();
		
		for (int j = 1; j <= sheetLastRowNumber; j++) {
			Row row = sheet.getRow(j);
			if (isNotEmpty(row)) {
				// row index starts at 0 here but is actually 1 in Excel
				int rowNum = row.getRowNum() + 1;
				String valida = readStringCellPoint(row, 0);

				if (UtilValidate.isEmpty(valida) || valida.indexOf(" ") > -1
						|| valida.equalsIgnoreCase("Clasificacion 1")) {
					Debug.logWarning(
							"Fila no. "
									+ rowNum
									+ " no se importo correctamente, tab Comp. Dev. Nom. 1 Id no valido ["
									+ valida + "].", MODULE);
					continue;
				}

				DataImportEjerNomPres presupuesto = new DataImportEjerNomPres();
				int rowCount = 1; // keep track of the row
				presupuesto.setRenglon(Long.valueOf(row.getRowNum()+1));
				presupuesto.setClasificacion1(valida);
				presupuesto.setClasificacion2(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion3(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion4(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion5(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion6(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion7(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion8(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion9(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion10(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion11(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion12(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion13(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion14(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion15(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClavePresupuestal(getClavePresupuestal(presupuesto));
				presupuesto.setAuxiliarCargo(this.readStringCellPoint(row, rowCount++));
				presupuesto.setAuxiliarAbono(this.readStringCellPoint(row, rowCount++));
				presupuesto.setMonto(this.readBigDecimalCell(row, rowCount++));
				presupuesto.setIdEjerNominaPres(ledger_repo.getNextSeqId("DataImportEjerNomPres"));
				presupuesto.setUserLoginId(obtenUsuario());
				nomina.add(presupuesto);				
			}
		}

		return nomina;
	}
	
	/**
	 * Guarda el excel de ejercido de nomina contable en la tabla DataImportEjerNomCont
	 * @param sheet
	 * @return
	 * @throws RepositoryException
	 * @throws InfrastructureException
	 */
	protected Collection<? extends EntityInterface> createDataImportEjerNomCont(
			Sheet sheet) throws RepositoryException, InfrastructureException {

		List<DataImportEjerNomCont> nomina = FastList.newInstance();
		int sheetLastRowNumber = sheet.getLastRowNum();
		
		LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
				.getLedgerDomain().getLedgerRepository();
		
		for (int j = 1; j <= sheetLastRowNumber; j++) {
			Row row = sheet.getRow(j);
			if (isNotEmpty(row)) {
				// row index starts at 0 here but is actually 1 in Excel
				int rowNum = row.getRowNum() + 1;
				String valida = readStringCellPoint(row, 0);

				if (UtilValidate.isEmpty(valida) || valida.equalsIgnoreCase("Descripcion de linea")) {
					Debug.logWarning(
							"Fila no. "
									+ rowNum
									+ " no se importo correctamente, tab Comp. Dev. Nom. 2 Id no valido ["
									+ valida + "].", MODULE);
					continue;
				}

				DataImportEjerNomCont contable = new DataImportEjerNomCont();
				int rowCount = 1; // keep track of the row
				contable.setRenglon(Long.valueOf(row.getRowNum()+1));
				contable.setDescripcionLinea(valida);
				contable.setAuxiliarCargo(this.readStringCellPoint(row, rowCount++));
				contable.setAuxiliarAbono(this.readStringCellPoint(row, rowCount++));
				contable.setMonto(this.readBigDecimalCell(row, rowCount++));
				contable.setIdEjerNominaCont(ledger_repo.getNextSeqId("DataImportEjerNomCont"));
				contable.setUserLoginId(obtenUsuario());
				nomina.add(contable);
			}
		}

		return nomina;
	}
	
	protected Collection<? extends EntityInterface> createDataImportOperacionPatrimonial(
			Sheet sheet) throws RepositoryException, InfrastructureException {

		List<DataImportOperPatr> operacionPatrimonial = FastList.newInstance();
		int sheetLastRowNumber = sheet.getLastRowNum();
		
		LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
				.getLedgerDomain().getLedgerRepository();
		
		for (int j = 1; j <= sheetLastRowNumber; j++) {
			Row row = sheet.getRow(j);
			if (isNotEmpty(row)) {
				// row index starts at 0 here but is actually 1 in Excel
				int rowNum = row.getRowNum() + 1;
				String valida = readStringCellPoint(row, 0);

				if (UtilValidate.isEmpty(valida) || valida.equalsIgnoreCase("Descripcion de linea")) {
					Debug.logWarning(
							"Fila no. "
									+ rowNum
									+ " no se importo correctamente, tab Operacion Patrimonial Id no valido ["
									+ valida + "].", MODULE);
					continue;
				}

				DataImportOperPatr contable = new DataImportOperPatr();
				int rowCount = 1; // keep track of the row
				contable.setDescripcionLinea(valida);
				contable.setAuxiliarCargo(this.readStringCellPoint(row, rowCount++));
				contable.setAuxiliarAbono(this.readStringCellPoint(row, rowCount++));
				contable.setMonto(this.readBigDecimalCell(row, rowCount++));
				//contable.setIdOperacionPatrimonial(Integer.toString(id));
				contable.setIdOperacionPatrimonial(ledger_repo.getNextSeqId("DataImportOperPatr"));
				contable.setUserLoginId(obtenUsuario());
				operacionPatrimonial.add(contable);
			}
		}

		return operacionPatrimonial;
	}
	
	protected Collection<? extends EntityInterface> createDataImportValidacionPresupuestal(
			Sheet sheet) throws RepositoryException, InfrastructureException {

		List<DataImportValidPresup> claves = FastList.newInstance();
		int sheetLastRowNumber = sheet.getLastRowNum();
		
		LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
				.getLedgerDomain().getLedgerRepository();
		
		for (int j = 1; j <= sheetLastRowNumber; j++) {
			Row row = sheet.getRow(j);
			if (isNotEmpty(row)) {
				// row index starts at 0 here but is actually 1 in Excel
				int rowNum = row.getRowNum() + 1;
				String valida = readStringCellPoint(row, 0);

				if (UtilValidate.isEmpty(valida) || valida.indexOf(" ") > -1
						|| valida.equalsIgnoreCase("Clasificacion 1")) {
					Debug.logWarning(
							"Fila no. "
									+ rowNum
									+ " no se importo correctamente, tab Comp. Dev. Nom. 1 Id no valido ["
									+ valida + "].", MODULE);
					continue;
				}

				DataImportValidPresup presupuesto = new DataImportValidPresup();
				int rowCount = 1; // keep track of the row
				presupuesto.setClasificacion1(valida);
				presupuesto.setClasificacion2(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion3(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion4(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion5(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion6(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion7(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion8(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion9(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion10(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion11(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion12(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion13(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion14(this.readStringCellPoint(row, rowCount++));
				presupuesto.setClasificacion15(this.readStringCellPoint(row, rowCount++));
				presupuesto.setMonto(this.readBigDecimalCell(row, rowCount++));
				presupuesto.setIdValPres(ledger_repo.getNextSeqId("DataImportValidPresup"));
				presupuesto.setUserLoginId(obtenUsuario());
				claves.add(presupuesto);
			}
		}

		return claves;
	}
	
	protected Collection<? extends EntityInterface> createDataImportIngreso(
			Sheet sheet) throws RepositoryException, InfrastructureException {

		List<DataImportIngreso> ingresos = FastList.newInstance();
		int sheetLastRowNumber = sheet.getLastRowNum();
		
		LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
				.getLedgerDomain().getLedgerRepository();
		
		for (int j = 1; j <= sheetLastRowNumber; j++) {
			Row row = sheet.getRow(j);
			if (isNotEmpty(row)) {
				// row index starts at 0 here but is actually 1 in Excel
				int rowNum = row.getRowNum() + 1;
				String valida = readStringCellPoint(row, 0);

				if (UtilValidate.isEmpty(valida) || valida.indexOf(" ") > -1
						|| valida.equalsIgnoreCase("Clasificacion 1")) {
					Debug.logWarning(
							"Fila no. "
									+ rowNum
									+ " no se importo correctamente, tab Ingreso Id no valido ["
									+ valida + "].", MODULE);
					continue;
				}

				DataImportIngreso ingreso = new DataImportIngreso();
				int rowCount = 1; // keep track of the row
				ingreso.setRenglon(Long.valueOf(rowNum));
				ingreso.setClasificacion1(valida);
				ingreso.setClasificacion2(this.readStringCellPoint(row, rowCount++));
				ingreso.setClasificacion3(this.readStringCellPoint(row, rowCount++));
				ingreso.setClasificacion4(this.readStringCellPoint(row, rowCount++));
				ingreso.setClasificacion5(this.readStringCellPoint(row, rowCount++));
				ingreso.setClasificacion6(this.readStringCellPoint(row, rowCount++));
				ingreso.setClasificacion7(this.readStringCellPoint(row, rowCount++));
				ingreso.setClasificacion8(this.readStringCellPoint(row, rowCount++));
				ingreso.setClasificacion9(this.readStringCellPoint(row, rowCount++));
				ingreso.setClasificacion10(this.readStringCellPoint(row, rowCount++));
				ingreso.setClasificacion11(this.readStringCellPoint(row, rowCount++));
				ingreso.setClasificacion12(this.readStringCellPoint(row, rowCount++));
				ingreso.setClasificacion13(this.readStringCellPoint(row, rowCount++));
				ingreso.setClasificacion14(this.readStringCellPoint(row, rowCount++));
				ingreso.setClasificacion15(this.readStringCellPoint(row, rowCount++));
				ingreso.setClavePresupuestal(getClavePresupuestal(ingreso));
				ingreso.setAuxiliarCargo(this.readStringCellPoint(row, rowCount++));
				ingreso.setAuxiliarAbono(this.readStringCellPoint(row, rowCount++));
				ingreso.setMonto(this.readBigDecimalCell(row, rowCount++));
				//ingreso.setIdIngreso(Integer.toString(id));
				ingreso.setIdIngreso(ledger_repo.getNextSeqId("DataImportIngreso"));
				ingreso.setUserLoginId(obtenUsuario());
				ingresos.add(ingreso);
			}
		}

		return ingresos;
	}
	
	protected Collection<? extends EntityInterface> createDataImportDevIng(
			Sheet sheet) throws RepositoryException, InfrastructureException {

		List<DataImportDevIng> devengados = FastList.newInstance();
		int sheetLastRowNumber = sheet.getLastRowNum();
	
		LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
				.getLedgerDomain().getLedgerRepository();
		
		for (int j = 1; j <= sheetLastRowNumber; j++) {
			Row row = sheet.getRow(j);
			if (isNotEmpty(row)) {
				// row index starts at 0 here but is actually 1 in Excel
				int rowNum = row.getRowNum() + 1;
				String valida = readStringCellPoint(row, 0);

				if (UtilValidate.isEmpty(valida) || valida.equalsIgnoreCase("Descripcion de linea")) {
					Debug.logWarning(
							"Fila no. "
									+ rowNum
									+ " no se importo correctamente, tab Ingreso Devengado Id no valido ["
									+ valida + "].", MODULE);
					continue;
				}

				DataImportDevIng devengo = new DataImportDevIng();
				int rowCount = 1; // keep track of the row
				devengo.setRenglon(Long.valueOf(rowNum));
				devengo.setDescripcionLinea(valida);
				devengo.setAuxiliarCargo(this.readStringCellPoint(row, rowCount++));
				devengo.setAuxiliarAbono(this.readStringCellPoint(row, rowCount++));
				devengo.setMonto(this.readBigDecimalCell(row, rowCount++));
				//devengo.setIdDevIng(Integer.toString(id));
				devengo.setIdDevIng(ledger_repo.getNextSeqId("DataImportDevIng"));
				devengo.setUserLoginId(obtenUsuario());
				devengados.add(devengo);
			}
		}

		return devengados;
	}
	
	protected Collection<? extends EntityInterface> createDataImportRecIng(
			Sheet sheet) throws RepositoryException, InfrastructureException {

		List<DataImportRecIng> recaudados = FastList.newInstance();
		int sheetLastRowNumber = sheet.getLastRowNum();
		
		LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
				.getLedgerDomain().getLedgerRepository();
		
		for (int j = 1; j <= sheetLastRowNumber; j++) {
			Row row = sheet.getRow(j);
			if (isNotEmpty(row)) {
				// row index starts at 0 here but is actually 1 in Excel
				int rowNum = row.getRowNum() + 1;
				String valida = readStringCellPoint(row, 0);

				if (UtilValidate.isEmpty(valida) || valida.equalsIgnoreCase("Descripcion de linea")) {
					Debug.logWarning(
							"Fila no. "
									+ rowNum
									+ " no se importo correctamente, tab Ingreso Recaudado Id no valido ["
									+ valida + "].", MODULE);
					continue;
				}

				DataImportRecIng recaudado = new DataImportRecIng();
				int rowCount = 1; // keep track of the row
				recaudado.setRenglon(Long.valueOf(rowNum));
				recaudado.setDescripcionLinea(valida);
				recaudado.setAuxiliarCargo(this.readStringCellPoint(row, rowCount++));
				recaudado.setAuxiliarAbono(this.readStringCellPoint(row, rowCount++));
				recaudado.setMonto(this.readBigDecimalCell(row, rowCount++));
				//recaudado.setIdRecIng(Integer.toString(id));
				recaudado.setIdRecIng(ledger_repo.getNextSeqId("DataImportRecIng"));
				recaudado.setUserLoginId(obtenUsuario());
				recaudados.add(recaudado);
			}
		}

		return recaudados;
	}
	
	protected Collection<? extends EntityInterface> createDataImportRequisicion(
			Sheet sheet) throws RepositoryException, InfrastructureException, GenericEntityException {
		
		LocalDispatcher dispatcher = this.getDomainsDirectory()
				.getLedgerDomain().getInfrastructure().getDispatcher();
		Delegator delegator = dispatcher.getDelegator();
		
		LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
				.getLedgerDomain().getLedgerRepository();

		List<DataImportDetRequisicion> listDetalle = FastList.newInstance();
		int sheetLastRowNumber = sheet.getLastRowNum();
		for (int j = 1; j <= sheetLastRowNumber; j++) {
			Row row = sheet.getRow(j);
			if (isNotEmpty(row)) {
				
				// row index starts at 0 here but is actually 1 in Excel
				int rowNum = row.getRowNum() + 1;
				// read catalogoId from first column "sheet column index
				// starts from 0"
				String id = readStringCellPoint(row, 0);

				if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1
						|| id.equalsIgnoreCase("productId")) {
					Debug.logWarning(
							"Fila no. "
									+ rowNum
									+ " no se importo correctamente, tab Producto. Id no valido ["
									+ id + "].", MODULE);
					continue;
				}
				
				List<GenericValue> customTime = delegator.findByAnd("CustomTimePeriod", UtilMisc.toMap("periodTypeId", "FISCAL_MONTH", "isClosed", "N"));
				
				java.util.Date fecha = null;

				DataImportDetRequisicion detalleR = new DataImportDetRequisicion();
				int rowCount = 1;//
				
				detalleR.setDataImportRequisicionId(ledger_repo.getNextSeqId("DataImportDetRequisicion"));
				detalleR.setDetalleId(UtilFormatOut.formatPaddedNumber(j, 4));
				detalleR.setProductId(id);
				detalleR.setRenglon(Long.valueOf(rowNum));
				detalleR.setCantidad(this.readBigDecimalCell(row, rowCount++));
				detalleR.setMonto(this.readBigDecimalCell(row, rowCount++));
				detalleR.setMes(this.readLongCell(row, rowCount++));
				detalleR.setAnio(this.readLongCell(row, rowCount++));
				detalleR.setDescripcion(this.readStringCellPoint(row, rowCount++));
				detalleR.setFechaEntrega(this.getFechaHHMMSScustom(row.getCell(rowCount++)));
				detalleR.setProcedencia(this.readStringCellPoint(row, rowCount++));
				detalleR.setIva(this.readStringCellPoint(row, rowCount++));
				detalleR.setClasificacion1(this.readStringCellPoint(row, rowCount++));
				detalleR.setClasificacion2(this.readStringCellPoint(row, rowCount++));
				detalleR.setClasificacion3(this.readStringCellPoint(row, rowCount++));
				detalleR.setClasificacion4(this.readStringCellPoint(row, rowCount++));
				detalleR.setClasificacion5(this.readStringCellPoint(row, rowCount++));
				detalleR.setClasificacion6(this.readStringCellPoint(row, rowCount++));
				detalleR.setClasificacion7(this.readStringCellPoint(row, rowCount++));
				detalleR.setClasificacion8(this.readStringCellPoint(row, rowCount++));
				detalleR.setClasificacion9(this.readStringCellPoint(row, rowCount++));
				detalleR.setClasificacion10(this.readStringCellPoint(row, rowCount++));
				detalleR.setClasificacion11(this.readStringCellPoint(row, rowCount++));
				detalleR.setClasificacion12(this.readStringCellPoint(row, rowCount++));
				detalleR.setClasificacion13(this.readStringCellPoint(row, rowCount++));
				detalleR.setClasificacion14(this.readStringCellPoint(row, rowCount++));
				detalleR.setClasificacion15(this.readStringCellPoint(row, rowCount++));						
				detalleR.setClavePresupuestal(getClavePresupuestal(detalleR));
				detalleR.setCreatedStamp(UtilDateTime.nowTimestamp());
				detalleR.setCreatedTxStamp(UtilDateTime.nowTimestamp());
				
				fecha = UtilDateTime.toDate(detalleR.getMes().intValue(), 1, detalleR.getAnio().intValue(), 0, 0, 0);
				boolean flag = true;
				for(GenericValue custom:customTime){
					
					if(custom.getDate("fromDate").compareTo(fecha)<=0 && custom.getDate("thruDate").compareTo(fecha)>0){
						detalleR.setCustomTimePeriodId(custom.getString("customTimePeriodId"));
						flag=false;
						break;
					}
					
				}
				
				if(flag){
					detalleR.setCustomTimePeriodId(null);
				}
				
				detalleR.setUserLoginId(obtenUsuario());
				listDetalle.add(detalleR);
			}
		}

		return listDetalle;
	}
	
	protected Collection<? extends EntityInterface> createDataImportAfectacionCompensada(
			Sheet sheet) throws RepositoryException, InfrastructureException {

		List<DataImportACompensada> listDetalle = FastList.newInstance();
		int sheetLastRowNumber = sheet.getLastRowNum();
		for (int j = 1; j <= sheetLastRowNumber; j++) {
			Row row = sheet.getRow(j);
			if (isNotEmpty(row)) {
				
				// row index starts at 0 here but is actually 1 in Excel
				int rowNum = row.getRowNum() + 1;
				// read catalogoId from first column "sheet column index
				// starts from 0"
				String id = readStringCellPoint(row, 0);

				if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1
						|| id.equalsIgnoreCase("periodoA")) {
					Debug.logWarning(
							"Fila no. "
									+ rowNum
									+ " no se importo correctamente, tab periodoA. Id no valido ["
									+ id + "].", MODULE);
					continue;
				}

				DataImportACompensada detalleA = new DataImportACompensada();
				int rowCount = 1;
				detalleA.setDetalleId(Long.valueOf(j));
				detalleA.setRenglon(Long.valueOf(rowNum));
				detalleA.setPeriodoA(id);
				detalleA.setPeriodoR(this.readStringCellPoint(row, rowCount++));
				detalleA.setMoneda(this.readStringCellPoint(row, rowCount++));
				detalleA.setMonto(this.readBigDecimalCell(row, rowCount++));
				detalleA.setClasificacion1(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasificacion2(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasificacion3(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasificacion4(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasificacion5(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasificacion6(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasificacion7(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasificacion8(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasificacion9(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasificacion10(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasificacion11(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasificacion12(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasificacion13(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasificacion14(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasificacion15(this.readStringCellPoint(row, rowCount++));
				detalleA.setClavePresupuestalA(getClavePresupuestal(detalleA));
				detalleA.setClasif1(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasif2(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasif3(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasif4(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasif5(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasif6(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasif7(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasif8(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasif9(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasif10(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasif11(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasif12(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasif13(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasif14(this.readStringCellPoint(row, rowCount++));
				detalleA.setClasif15(this.readStringCellPoint(row, rowCount++));
				detalleA.setClavePresupuestalR(getClavePresupuestal(detalleA, "clasif"));
				detalleA.setUserLoginId(obtenUsuario());
				listDetalle.add(detalleA);
			}
		}

		return listDetalle;
	}

	/**
     * Cargar el historial de bienes en control patrimonial.
     * @throws RepositoryException if an error occurs
     * @throws InfrastructureException 
     */
    protected Collection<? extends EntityInterface> createDataImportHistorialBienes(Sheet sheet) throws RepositoryException, InfrastructureException {
        int sheetLastRowNumber = sheet.getLastRowNum();
        List<DataImportHistorialBienes> historialBienes = FastList.newInstance();

        for (int j = 1; j <= sheetLastRowNumber; j++) {
            Row row = sheet.getRow(j);
            if (isNotEmpty(row)) {
                int rowNum = row.getRowNum() + 1;
                String id = readStringCellPoint(row, 0);
                if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1 || id.equalsIgnoreCase("numeroInventarioId")) {
                    Debug.logWarning("La fila numero " + rowNum + " no ha sido importada de el tab Historial de bienes: El ID es invalido [" + id + "].", MODULE);
                    continue;
                }

                DataImportHistorialBienes historial = new DataImportHistorialBienes();
                historial.setFixedAssetId(id);
                historial.setIdActivoAnterior(readStringCellPoint(row, 1));
                historial.setFechaAdquisicion(getFechaHHMMSScustomFecha(row.getCell(2)));
                historial.setValorAdquisicion(readBigDecimalCell(row, 3));
                historial.setAniosVidaUtil(readBigDecimalCell(row, 4));
                historial.setFechaFinalVida(getFechaHHMMSScustomFecha(row.getCell(5)));
                historial.setDepreciacionAcumulada(readBigDecimalCell(row, 6));
                historial.setUserLoginId(obtenUsuario());
                historialBienes.add(historial);
            }
        }
        return historialBienes;
    }
    
    /**
     * Importacion de datos de Pago a externos
     * @param sheet
     * @return
     * @throws RepositoryException
     * @throws InfrastructureException
     */
    protected Collection<? extends EntityInterface> crearDataImportPagoExterno(Sheet sheet)  throws RepositoryException, InfrastructureException {
    	
    	int sheetLastRowNumber = sheet.getLastRowNum();
    	List<DataImportPagoExterno> listPagoExterno = FastList.newInstance();
    	
		LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory().getLedgerDomain().getLedgerRepository();
    	
        for (int j = 1; j <= sheetLastRowNumber; j++) {
            Row row = sheet.getRow(j);
            if (isNotEmpty(row)) {
                int rowNum = row.getRowNum() + 1;
                String id = readStringCellPoint(row, 0);
                if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1 || id.equalsIgnoreCase("clasificacion1")) {
                    Debug.logWarning("La fila numero " + rowNum + " no ha sido importada : El ID es invalido [" + id + "].", MODULE);
                    continue;
                }
                
                DataImportPagoExterno PagoExterno = new DataImportPagoExterno();
                int cellNumber = 0;
                PagoExterno.setDataImportPagoExternoId(ledger_repo.getNextSeqId("DataImportPagoExterno"));
                PagoExterno.setRenglon(Long.valueOf(rowNum));
                PagoExterno.setClasificacion1(this.readStringCellPoint(row, cellNumber++));
				PagoExterno.setClasificacion2(this.readStringCellPoint(row, cellNumber++));
				PagoExterno.setClasificacion3(this.readStringCellPoint(row, cellNumber++));
				PagoExterno.setClasificacion4(this.readStringCellPoint(row, cellNumber++));
				PagoExterno.setClasificacion5(this.readStringCellPoint(row, cellNumber++));
				PagoExterno.setClasificacion6(this.readStringCellPoint(row, cellNumber++));
				PagoExterno.setClasificacion7(this.readStringCellPoint(row, cellNumber++));
				PagoExterno.setClasificacion8(this.readStringCellPoint(row, cellNumber++));
				PagoExterno.setClasificacion9(this.readStringCellPoint(row, cellNumber++));
				PagoExterno.setClasificacion10(this.readStringCellPoint(row, cellNumber++));
				PagoExterno.setClasificacion11(this.readStringCellPoint(row, cellNumber++));
				PagoExterno.setClasificacion12(this.readStringCellPoint(row, cellNumber++));
				PagoExterno.setClasificacion13(this.readStringCellPoint(row, cellNumber++));
				PagoExterno.setClasificacion14(this.readStringCellPoint(row, cellNumber++));
				PagoExterno.setClasificacion15(this.readStringCellPoint(row, cellNumber++));
				PagoExterno.setClavePresupuestal(getClavePresupuestal(PagoExterno));
				PagoExterno.setNombreExterno(this.readStringCellPoint(row, cellNumber++));
				PagoExterno.setFechaContable(getFecha(row.getCell(cellNumber++)));
				PagoExterno.setConceptoPago(this.readStringCellPoint(row, cellNumber++));
				PagoExterno.setCuentaBancariaId(this.readStringCellPoint(row, cellNumber++));
				PagoExterno.setMonto(this.readBigDecimalCell(row, cellNumber++));
				PagoExterno.setMoneda(this.readStringCellPoint(row, cellNumber++));
				PagoExterno.setMetodoPago(this.readStringCellPoint(row, cellNumber++));
				PagoExterno.setNumeroCheque(this.readStringCellPoint(row, cellNumber++));
				PagoExterno.setMesId(
						UtilFormatOut.formatPaddedNumber(
								UtilDateTime.getMonth(UtilDateTime.toTimestamp(PagoExterno.getFechaContable()), timeZone, locale)+1
									,2));
				PagoExterno.setCiclo(
						UtilFormatOut.formatPaddedNumber(
								UtilDateTime.getYear(UtilDateTime.toTimestamp(PagoExterno.getFechaContable()), timeZone, locale)
									,4));
				PagoExterno.setUserLoginId(obtenUsuario());
                listPagoExterno.add(PagoExterno);
            }
        }
    	
    	return listPagoExterno;
    }
	
	/**
     * Uploads an Excel file in the correct directory.
     * @exception ServiceException if an error occurs
     * @throws InfrastructureException 
     */
    public void parseFileForDataImport() throws ServiceException, InfrastructureException, GenericEntityException {

        // Get the uploaded file
        File file = getUploadedExcelFile(getUploadedFileName());

        // set it up as an Excel workbook
        Workbook wb = null;
        try {
            // this will auto close the FileInputStream when the constructor completes
            wb = WorkbookFactory.create(file);
        } catch (IOException e) {
            throw new ServiceException("Unable to read or create workbook from file [" + getUploadedFileName() + "] " + e.getMessage());
        } catch (InvalidFormatException e) {
			e.printStackTrace();
			throw new ServiceException("Unable to read or create workbook from file [" + getUploadedFileName() + "] " + e.getMessage());
		}

        // loop through the tabs and import them one by one
        try {

            // a collection of all the records from all the excel spreadsheet tabs
            FastList<EntityInterface> entitiesToCreate = FastList.newInstance();

            for (String excelTab : EXCEL_TABS) {
                Sheet sheet = wb.getSheet(excelTab);
                if (sheet == null) {
                    Debug.logWarning("Did not find a sheet named " + excelTab + " in " + file.getName() + ".  Will not be importing anything.", MODULE);
                } else {
                    if (EXCEL_PRODUCT_TAB.equals(excelTab)) {
                    	deleteEntities("DataImportProduct");
                        entitiesToCreate.addAll(createDataImportProducts(sheet));
                    } else if (EXCEL_SUPPLIERS_TAB.equals(excelTab)) {
                    	deleteEntities("DataImportSupplier");
                        entitiesToCreate.addAll(createDataImportSuppliers(sheet));
                    } else if (EXCEL_CUSTOMERS_TAB.equals(excelTab)) {
                    	deleteEntities("DataImportCustomers");
                        entitiesToCreate.addAll(createDataImportCustomers(sheet));
                    } else if (EXCEL_INVENTORY_TAB.equals(excelTab)) {
                    	deleteEntities("DataImportInventory");
                        entitiesToCreate.addAll(createDataImportInventory(sheet));
                    } else if (EXCEL_GL_ACCOUNTS_TAB.equals(excelTab)) {
                    	deleteEntities("DataImportGlAccount");
                        entitiesToCreate.addAll(createDataImportGlAccounts(sheet));
                    } else if (EXCEL_TAG_TAB.equals(excelTab)) {
						deleteEntities("DataImportTag");
						entitiesToCreate.addAll(createDataImportTag(sheet));
					} else if (EXCEL_PARTY_TAB.equals(excelTab)) {
						deleteEntities("DataImportParty");
						entitiesToCreate.addAll(createDataImportParty(sheet));
					} else if (EXCEL_MATRIZ_CONVERSION_EGR_TAB.equals(excelTab)) {
						entitiesToCreate
						.addAll(createMatrizEgreso(sheet));
					} else if (EXCEL_MATRIZ_CONVERSION_ING_TAB.equals(excelTab)) {
						entitiesToCreate
						.addAll(createMatrizIngreso(sheet));						
					} else if (EXCEL_MATRIZ_CONCEPTO_TAB.equals(excelTab)) {
						entitiesToCreate
						.addAll(createMatrizConcepto(sheet));
					}else if (EXCEL_PRESUPUESTO_INICIAL_TAB.equals(excelTab)) {
						deleteEntities("DataImportPresupuestoInicia");
						entitiesToCreate
						.addAll(createDataImportPresupuestoInicial(sheet));
					} else if (EXCEL_OPERACION_DIARIA_TAB.equals(excelTab)) {
						deleteEntities("DataImportPresupuestoInicia");
						entitiesToCreate
						.addAll(createDataImportOperacionDiaria(sheet));
					} else if (EXCEL_ORDENES_PAGO_TAB.equalsIgnoreCase(excelTab)) {
						deleteEntities("DataImportPresupuestoInicia");
						entitiesToCreate
						.addAll(createDataImportOrdenesDePago(sheet));
					} else if (EXCEL_ORDENES_COBRO_TAB.equalsIgnoreCase(excelTab)) {
						deleteEntities("DataImportPresupuestoInicia");
						entitiesToCreate
						.addAll(createDataImportOrdenesCobro(sheet));
					} else if (EXCEL_PAGOS.equals(excelTab)) {
						deleteEntities("DataImportPresupuestoInicia");
						entitiesToCreate
						.addAll(createDataImportPagos(sheet));
					} else if (EXCEL_SALDO_INICIAL_AUXILIAR.equals(excelTab)) {
						deleteEntities("DataImportSaldoInicialAux");
							entitiesToCreate
							.addAll(createDataImportSaldoInicialAuxiliar(sheet));
					}else if (EXCEL_SALDO_AFECTACION_EGRESO.equals(excelTab)) {
						deleteEntities("DataImportAfectacionEgreso");
							entitiesToCreate
							.addAll(createDataImportAfectacionEgreso(sheet));
					}  else if (EXCEL_USUARIO_TAB.equals(excelTab)) {
						deleteEntities("DataImportUser");
						entitiesToCreate
								.addAll(createDataImportUsuario(sheet));
					} else if (EXCEL_FIXED_ASSET_TAB.equals(excelTab)) {
						deleteEntities("DataImportFixedAsset");
						entitiesToCreate.addAll(createDataImportFixedAsset(sheet));
					} else if (EXCEL_LEVANTA_FIXED_ASSET_TAB.equals(excelTab)) {
						deleteEntities("DataImportLevantaActFijo");
						entitiesToCreate.addAll(createDataImportLevantaFixedAsset(sheet));
					} else if (EXCEL_COMP_DEV_NOM_PRES.equals(excelTab)) {
						deleteEntities("DataImportCompDevNomPres");
						entitiesToCreate
								.addAll(createDataImportCompDevNomPres(sheet));
					} else if (EXCEL_COMP_DEV_NOM_CONT.equals(excelTab)) {
						deleteEntities("DataImportCompDevNomCont");
						entitiesToCreate
								.addAll(createDataImportCompDevNomCont(sheet));
					} else if (EXCEL_EJER_NOM_PRES.equals(excelTab)) {
						deleteEntities("DataImportEjerNomPres");
						entitiesToCreate
								.addAll(createDataImportEjerNomPres(sheet));
					} else if (EXCEL_EJER_NOM_CONT.equals(excelTab)) {
						deleteEntities("DataImportEjerNomCont");
						entitiesToCreate
								.addAll(createDataImportEjerNomCont(sheet));
					}else if (EXCEL_OPER_PATRI.equals(excelTab)) {
						deleteEntities("DataImportOperPatr");
						entitiesToCreate
								.addAll(createDataImportOperacionPatrimonial(sheet));
					}else if (EXCEL_VAL_PRES.equals(excelTab)) {
						deleteEntities("DataImportValidPresup");
						entitiesToCreate
								.addAll(createDataImportValidacionPresupuestal(sheet));
					}else if (EXCEL_INGRESO.equals(excelTab)) {
						deleteEntities("DataImportIngreso");
						entitiesToCreate
								.addAll(createDataImportIngreso(sheet));
					}else if (EXCEL_DEV_ING.equals(excelTab)) {
						deleteEntities("DataImportDevIng");
						entitiesToCreate
								.addAll(createDataImportDevIng(sheet));
					}else if (EXCEL_REC_ING.equals(excelTab)) {
						deleteEntities("DataImportRecIng");
						entitiesToCreate
								.addAll(createDataImportRecIng(sheet));
					}else if (EXCEL_REQUISICION.equals(excelTab)) {
						deleteEntities("DataImportDetRequisicion");
						entitiesToCreate.addAll(createDataImportRequisicion(sheet));
					}
					else if (EXCEL_COMPENSADA.equals(excelTab)) {
						deleteEntities("DataImportACompensada");
						entitiesToCreate
								.addAll(createDataImportAfectacionCompensada(sheet));
					} 
                    // etc ...
					else if (EXCEL_HISTORIAL_BIENES.equals(excelTab)) {
						deleteEntities("DataImportHistorialBienes");
						entitiesToCreate
								.addAll(createDataImportHistorialBienes(sheet));
					} else if (EXCEL_PAGO_EXTERNO.equals(excelTab)) {
						deleteEntities("DataImportPagoExterno");
						entitiesToCreate.addAll(crearDataImportPagoExterno(sheet));
					}
                    
                }
            }

            // create and store values from all the sheets in the workbook in database using the PartyRepositoryInterface
            // note we're just using the most basic repository method, so any repository could do here
            PartyRepositoryInterface partyRepo = this.getDomainsDirectory().getPartyDomain().getPartyRepository();
            Debug.logVerbose("entitiesToCreate: " + entitiesToCreate, MODULE);
            partyRepo.createOrUpdate(entitiesToCreate);
            wb.close();

        } catch (RepositoryException e) {
        	CUCOP = new ArrayList<String>();
            throw new ServiceException(e);
        } catch (IOException e) {
			e.printStackTrace();
		}
        

        // remove the uploaded file now
        if (!file.delete()) {
            Debug.logWarning("Could not delete the file : " + file.getName(), MODULE);
        }
    }

    public void setUploadedFileName(String uploadedFileName) {
        this.uploadedFileName = uploadedFileName;
    }

    public String getUploadedFileName() {
        return uploadedFileName;
    }
    
    /*
	 * Delete Entities
	 */
	private void deleteEntities(String entity) throws InfrastructureException {
		this.session = this.getInfrastructure().getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			// Query query =
			// session.createQuery("delete from DataImportGlAccount");
			Query query = session.createQuery("delete from " + entity + " where userLoginId = :USR ");
			query.setString("USR", obtenUsuario());
			int rowCount = query.executeUpdate();
			logger.debug("Filas afectadas: " + rowCount + " ," + entity);
			tx.commit();
		} catch (Exception e) {
			Debug.log("Error al borrar registros " + e);
			if (tx != null)
				tx.rollback();
		}
	}
	
	private Timestamp getFechaHHMMSS(Cell cell) {
		
		SimpleDateFormat formatoDelTexto = new SimpleDateFormat(
				"dd-MM-yyyy hh:mm:ss");

		Calendar cal = null;

		try {
			if(cell == null)
				return null;
			switch (cell.getCellType()) {
			case Cell.CELL_TYPE_BLANK:
				return null;

			default:

				String cadFecha = cell.toString().trim();
				cal = Calendar.getInstance();
				cal.setTime(formatoDelTexto.parse(cadFecha));
				break;
			}
		} catch (Exception e) {
			logger.debug("No se pudo hacer el parser de la fecha: " + e);
		}
		return new Timestamp(cal.getTimeInMillis());

	}
	
    private Timestamp getFechaHHMMSScustom(Cell cell) {

		SimpleDateFormat formatoDelTexto = new SimpleDateFormat("dd-MM-yyyy");
		Timestamp fechaContable = null;
		com.ibm.icu.util.Calendar calendarHoy = UtilDateTime.getCalendarInstance(timeZone, locale);

		try {
			switch (cell.getCellType()) {
			case Cell.CELL_TYPE_BLANK:
				break;

			default:

				String cadFecha = cell.toString().trim();
				com.ibm.icu.util.Calendar calFechaContable = 
						UtilDateTime.toCalendar(new Timestamp(formatoDelTexto.parse(cadFecha).getTime()));
				
				calFechaContable.set(Calendar.HOUR, calendarHoy.get(Calendar.HOUR));
				calFechaContable.set(Calendar.MINUTE, calendarHoy.get(Calendar.MINUTE));
				calFechaContable.set(Calendar.SECOND, calendarHoy.get(Calendar.SECOND));
				calFechaContable.set(Calendar.MILLISECOND, 100);
				
				fechaContable = new Timestamp(calFechaContable.getTimeInMillis());
				break;
			}
		} catch (Exception e) {
			logger.debug("No se pudo hacer el parser de la fecha: " + e);
		}
		return fechaContable;

	}
    
    @SuppressWarnings({ "deprecation" })
	private Timestamp getFechaHHMMSScustomFecha(Cell cell) {

		SimpleDateFormat formatoDelTexto = new SimpleDateFormat("dd-MMM-yyyy");
		Date fecha = null;
		Timestamp fechaTime = null;
		Timestamp fechaContable = null;
		Timestamp fechaActual = UtilDateTime.nowTimestamp();				

		try {			
			switch (cell.getCellType()) {
			case Cell.CELL_TYPE_BLANK:
				break;

			default:

				String cadFecha = cell.toString().trim();
				//fecha = new Timestamp(formatoDelTexto.parse(cadFecha).getTime());
				fecha = new Date(formatoDelTexto.parse(cadFecha).getTime());
				int horas = fechaActual.getHours();
				int minutos = fechaActual.getMinutes();
				int segundos = fechaActual.getSeconds();				
				fechaTime = new Timestamp(fecha.getYear(), fecha.getMonth(), fecha.getDate(), horas, minutos, segundos, 0);								
				Calendar date = Calendar.getInstance();
					date.setTime(fechaTime);
					date.set(Calendar.MILLISECOND,100);;
				fechaContable = new Timestamp(date.getTimeInMillis());
				break;
			}
		} catch (Exception e) {
			logger.debug("No se pudo hacer el parser de la fecha: " + e);
		}
		return fechaContable;

	}
    
    public String obtenUsuario() throws InfrastructureException {
    	return user.getOfbizUserLogin().getString("userLoginId");    	
    }
    
    public static boolean validarCuentasRegistro(Delegator delegator,String cuenta1, String cuenta2) throws GenericEntityException 
    {	GenericValue generic = null;
    	Debug.log("Declara GenericValue");
		if (cuenta1 != null) 
		{	Debug.log("Entra a cuentas 1");
			generic = obtenCuenta(delegator, cuenta1);
			Debug.log("obtiene Generic 1: " + generic);
			if(generic!=null) 
			{	Debug.log("Primer if 1");
				if(!generic.getString("tipoCuentaId").equals("R"))
				{	Debug.log("Segundo if 1");
					return false;
				}
			}
			else
			{	return false;				
			}
		}
		if (cuenta2 != null) 
		{	Debug.log("Entra a cuentas 2");
			generic = obtenCuenta(delegator, cuenta2);
			Debug.log("obtiene Generic 2: " + generic);			
			if(generic!=null)
			{	Debug.log("Primer if 2");
				if (!generic.getString("tipoCuentaId").equals("R"))
				{	Debug.log("Segundo if 2");	
					return false;
				}
			}
			else
			{	return false;				
			}
		}
		return true;
	}

	public static GenericValue obtenCuenta(Delegator delegator, String cuenta) throws GenericEntityException 
	{	Debug.log("obtenCuenta");
		GenericValue generic = delegator.findByPrimaryKey("GlAccount", UtilMisc.toMap("glAccountId", cuenta));
		Debug.log("returnObtenerCuenta");
		return generic;
	}
	
	
	/**
	 * Se arma la clave presupuestal
	 * @param Entity
	 * @param prefijo
	 * @return
	 */
	private String getClavePresupuestal(Entity Entidad, String prefijo){
		
		StringBuffer clavePresupuestal = new StringBuffer();
		String clasificacion = new String();
		for (int i = 1; i < UtilClavePresupuestal.TAG_COUNT; i++) {
			clasificacion = Entidad.getString(prefijo+i);
			if(UtilValidate.isNotEmpty(clasificacion)){
				clavePresupuestal.append(clasificacion.trim());
			} else {
				break;
			}
		}
		
		return clavePresupuestal.toString();
	}
	
	/**
	 * Arma la clave presupuestal con el prefijo por defecto (clasificacion)
	 * @param Entidad
	 * @return
	 */
	private String getClavePresupuestal(Entity Entidad){
		return getClavePresupuestal(Entidad, UtilClavePresupuestal.VIEW_TAG_PREFIX);
	}
	
    
}
