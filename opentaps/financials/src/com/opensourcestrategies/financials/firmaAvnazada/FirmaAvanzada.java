package com.opensourcestrategies.financials.firmaAvnazada;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.ssl.PKCS8Key;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.common.AB.UtilWorkflow;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.domain.DomainsDirectory;
import org.opentaps.domain.DomainsLoader;
import org.opentaps.domain.party.Party;
import org.opentaps.domain.party.PartyRepositoryInterface;
import org.opentaps.foundation.entity.EntityNotFoundException;
import org.opentaps.foundation.infrastructure.Infrastructure;
import org.opentaps.foundation.infrastructure.InfrastructureException;
import org.opentaps.foundation.infrastructure.User;
import org.opentaps.foundation.repository.RepositoryException;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;



public class FirmaAvanzada {
	
	public final static String FIRMADO = "FIRMADO";
	public final static String FIRMADOTOTALMENTE = "FIRMADO_E";
	public final static String PENDIENTE = "PENDIENTE_E";
	
	public static Map<String,? extends Object> firmaPDF(DispatchContext dctx, Map<String,? extends Object> context) {
		
		Map<String,? extends Object> retorno = ServiceUtil.returnSuccess();
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Locale locale = (Locale) context.get("locale");
		String reporteId = (String) context.get("reporteId");
		String urlHost = (String) context.get("urlHost");
		ByteBuffer pdf = (ByteBuffer) context.get("pdf");
		//ByteBuffer certificado = (ByteBuffer) context.get("certificado");
		ByteBuffer clavePrivada = (ByteBuffer) context.get("clavePrivada");
		String clave = (String) context.get("clave");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String workFlowId = "";
		DomainsLoader domainLoader = new DomainsLoader(new Infrastructure(dctx.getDispatcher()), new User(userLogin));
		DomainsDirectory dd = domainLoader.getDomainsDirectory();
        Party firmante = null;
		
		try{
			PartyRepositoryInterface partyRepository = dd.getPartyDomain().getPartyRepository();
			firmante = partyRepository.getPartyById(userLogin.getString("partyId"));
			
			//validaCertificado(certificado.array());
			
			PrivateKey privKey = validaLlavePrivada(clavePrivada.array(), clave.toCharArray());
			
			workFlowId = registraFirmantes(delegator, obtenerFirmantes(delegator, reporteId), reporteId, userLogin, pdf, privKey, locale, dispatcher, urlHost);
			
			guardarPDF(delegator, pdf, workFlowId);		
			
			if(!tienePedientesFirma(delegator, workFlowId)){
				firmaGuardaPDF(dctx.getDispatcher(), userLogin, delegator, workFlowId, pdf.array());
			}
			
		} catch(CertificateException e){
			e.printStackTrace();
			return ServiceUtil.returnError("Error al leer el certificado");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("El algoritmo del cretificado es incompatible");
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("El certificado es incorrecto");
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("No se pudo autenticar su firma");
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("No se pudo autenticar su firma");
		} catch (IOException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("Error al leer la llave privada");
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		} catch (EntityNotFoundException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		} catch (RepositoryException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		} catch (DocumentException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		
		retorno = ServiceUtil.returnSuccess(firmante.getName()+" firm\u00f3 correctamente");
		
		return retorno;
	}
	
	/**
	 * Valida el archivo .cer ingresado
	 * @param arregloCer
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	private static void validaCertificado(byte[] arregloCer) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException{
		
        CertificateFactory cf =   CertificateFactory.getInstance("X509");
        X509Certificate cer = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(arregloCer));
        PublicKey pk = cer.getPublicKey();
        cer.checkValidity();
        Signature dsa = Signature.getInstance("SHA1withRSA");
        dsa.initVerify(pk);

	}
	
	/**
	 * Valida la llave privada .key y tambien la contrasenia ingresada
	 * @param arregloCer
	 * @param pass
	 * @return
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	private static PrivateKey validaLlavePrivada(byte[] arregloCer, char[] pass) throws GeneralSecurityException, IOException{
		
        InputStream isKey = new ByteArrayInputStream(arregloCer);
        PKCS8Key pkcs8 = new PKCS8Key(isKey, pass);
        return pkcs8.getPrivateKey();
        
	}
	
	/**
	 * Obtiene la cadena de firma del documento
	 * @param documento
	 * @param privKey
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	private static String firmaDocumento(ByteBuffer documento, PrivateKey privKey) 
												throws NoSuchAlgorithmException, NoSuchPaddingException,
														InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        sha1.update(documento);
        
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privKey);
        byte[] encripta = cipher.doFinal(sha1.digest());
        
        return Base64.encode(encripta);
        
	}
	
	/**
	 * Obtiene lista generica de firmantes para el reportId seleccionado
	 * @param delegator
	 * @param reporteId
	 * @return
	 * @throws GenericEntityException 
	 */
	private static List<GenericValue> obtenerFirmantes(Delegator delegator, String reporteId) throws GenericEntityException 
	{	EntityCondition condicion = EntityCondition.makeCondition(EntityCondition.makeCondition("reporteId", EntityOperator.EQUALS, reporteId));		
		List<GenericValue> firmantesList = delegator.findByCondition("ReporteFirmante", condicion, null, null);
		Debug.log("firmantesList: " + firmantesList);
		return firmantesList;
	}		
	
	/**
	 * Guardar el archivo pdf en la base de datos
	 * @param documento
	 * @param delegator
	 * @param reporteId
	 * @return 
	 * @return
	 * @throws GenericEntityException 
	 */
	private static void guardarPDF(Delegator delegator, ByteBuffer documento, String workFlowId) throws GenericEntityException 
	{	byte[] cadena = documento.array();
		GenericValue reportePdfWorkFlow = GenericValue.create(delegator.getModelEntity("ReportePdfWorkFlow"));						
		String reportePdfWorkFlowId = delegator.getNextSeqId("ReportePdfWorkFlow");
		reportePdfWorkFlow.set("reportePdfWorkFlowId", reportePdfWorkFlowId);
		reportePdfWorkFlow.set("workFlowId", workFlowId);
		reportePdfWorkFlow.setBytes("pdfOriginal", cadena);
		reportePdfWorkFlow.set("fechaEmision", UtilDateTime.nowTimestamp());
		reportePdfWorkFlow.set("statusId", PENDIENTE);
		delegator.create(reportePdfWorkFlow);
	}
        
	/**
	 * Se registran los firmantes 
	 * @param delegator
	 * @param listFirmantes
	 * @param reporteId
	 * @param userLogin
	 * @param pdf
	 * @param privKey
	 * @return workflowId
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws GenericEntityException 
	 */
	private static String registraFirmantes(Delegator delegator, List<GenericValue> listFirmantes,
								String reporteId, GenericValue userLogin, ByteBuffer pdf, PrivateKey privKey, Locale locale, LocalDispatcher dispatcher, String urlHost) 
										throws InvalidKeyException, NoSuchAlgorithmException, 
											NoSuchPaddingException, IllegalBlockSizeException, 
												BadPaddingException, GenericEntityException{
		
		String workFlowId = "";
		if(!listFirmantes.isEmpty())
		{	Iterator<GenericValue> firmantesIter = listFirmantes.iterator();
			GenericValue reporteWorkFlow = GenericValue.create(delegator.getModelEntity("ReporteWorkFlow"));						
			workFlowId = delegator.getNextSeqId("ReporteWorkFlow");
			while (firmantesIter.hasNext())
			{	GenericValue firmantes = firmantesIter.next();										
				reporteWorkFlow.set("workFlowId", workFlowId);
				reporteWorkFlow.set("reporteId", reporteId);
				reporteWorkFlow.set("firmanteId", firmantes.getString("firmanteId"));													
				if(firmantes.getString("firmanteId").equalsIgnoreCase(userLogin.getString("partyId")))
				{	reporteWorkFlow.set("firma", firmaDocumento(pdf, privKey));
					reporteWorkFlow.set("statusId", FIRMADO);
				}else{
						String correoOrigen = userLogin.getString("partyId");
						String correoDestino = firmantes.getString("firmanteId");
						UtilWorkflow.armarEnviarCorreo(correoOrigen,correoDestino,"FIRMA_ELECTRONICA","CorreoMensajeAutorizar",
								urlHost,reporteId,null,delegator,locale,dispatcher);
						reporteWorkFlow.set("statusId", PENDIENTE);	
					
					
				}
				delegator.create(reporteWorkFlow);
			}
			return workFlowId;
		}
		else
		{	throw new GenericEntityException("No se encontraron firmantes asociados al reporte seleccionado");
		}
		
	}
        
	/**
	 * Imprime PDF de Workflow de reportes
	 * @param request
	 * @param response
	 * @return
	 * @throws DocumentException 
	 * @throws RepositoryException 
	 * @throws EntityNotFoundException 
	 * @throws InfrastructureException 
	 */
	public static String imprimePdfWorkFlow(HttpServletRequest request, HttpServletResponse response) throws DocumentException, EntityNotFoundException, RepositoryException, InfrastructureException{
		
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String workFlowId = (String) request.getParameter("workFlowId");
		String campoPDF = (String) request.getParameter("campoPDF");		
		
		try {
			
			List<GenericValue> listReportePdf = delegator.findByAnd("ReportePdfWorkFlow", UtilMisc.toMap("workFlowId",workFlowId));
			
			GenericValue reporte = null;
			if(!UtilValidate.isEmpty(listReportePdf)){
				
				reporte = listReportePdf.get(0);
				
				response.setContentType("application/pdf");
				response.setContentLength(reporte.getBytes(campoPDF).length);
				OutputStream os = response.getOutputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				baos.write(reporte.getBytes(campoPDF).clone());
				baos.writeTo(os);
				os.flush();
				os.close();
				
			} else {
				return "error";
			}
			
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return "error";
		} catch (IOException e) {
			e.printStackTrace();
			return "error";
		}
		
		return "success";
		
	}
	
	/**
	 * Metodo para firmar y guardar un pdf 
	 * @param dispatcher
	 * @param userLogin
	 * @param delegator
	 * @param workFlowId
	 * @param pdf
	 * @throws GenericEntityException
	 * @throws RepositoryException
	 * @throws IOException
	 * @throws DocumentException
	 * @throws EntityNotFoundException
	 */
	private static void firmaGuardaPDF(LocalDispatcher dispatcher,GenericValue userLogin, 
										Delegator delegator, String workFlowId , byte[] pdf)
						throws GenericEntityException, RepositoryException, IOException, 
												DocumentException, EntityNotFoundException{
		
		DomainsLoader domainLoader = new DomainsLoader(new Infrastructure(dispatcher), new User(userLogin));
		DomainsDirectory dd = domainLoader.getDomainsDirectory();
        PartyRepositoryInterface partyRepository = dd.getPartyDomain().getPartyRepository();
		
        List<GenericValue> listReportePdf = delegator.findByAnd("ReportePdfWorkFlow", UtilMisc.toMap("workFlowId",workFlowId));
		List<GenericValue> listReporte = delegator.findByAnd("ReporteWorkFlow", UtilMisc.toMap("workFlowId",workFlowId));
	
		PdfReader pdfRea = new PdfReader(pdf);
		int paginas = pdfRea.getNumberOfPages();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		PdfStamper stamper = new PdfStamper(pdfRea, out);
		PdfContentByte contentOvr = stamper.getOverContent(paginas);
		
		int alturaY = 100;
		
		for (GenericValue flow : listReporte) {
			Party party = partyRepository.getPartyById(flow.getString("firmanteId"));
			
			Font fuente = FontFactory.getFont(FontFactory.HELVETICA, 7, Font.BOLDITALIC, Color.BLACK);
			
			ColumnText.showTextAligned(contentOvr,
	                Element.MULTI_COLUMN_TEXT, new Phrase(party.getName(),fuente), 34, alturaY, 0);
			
			int numCarac = flow.getString("firma").length();
			int numRenglon = (numCarac/125);
			int inicio = 0;
			int tamanio = 125;
			
			for(int i=0; i<= numRenglon; i++){
				alturaY=alturaY-8;
				if(i==numRenglon){
					ColumnText.showTextAligned(contentOvr,
			                Element.PARAGRAPH, new Phrase(flow.getString("firma").substring(inicio),fuente), 34, (alturaY) , 0);
				}else{
					ColumnText.showTextAligned(contentOvr,
			                Element.PARAGRAPH, new Phrase(flow.getString("firma").substring(inicio, tamanio),fuente), 34, (alturaY) , 0);
				}
				inicio = inicio+125+1;
				tamanio = tamanio+125;
				
			}
			
			alturaY = alturaY-10;
		}
		
		stamper.close();
		pdfRea.close();
		
		if(!UtilValidate.isEmpty(listReportePdf)){
			GenericValue reportePDF = listReportePdf.get(0);
			reportePDF.setBytes("pdfFirmado", out.toByteArray());
			reportePDF.setString("statusId", FIRMADO);
			reportePDF.store();
		}
		
	}
	
	/**
	 * Verifica si el work flow de reportes tiene firmantes pendientes por firmar
	 * @param delegator
	 * @param workFlowId
	 * @return
	 * @throws GenericEntityException
	 */
	public static boolean tienePedientesFirma(Delegator delegator, String workFlowId) throws GenericEntityException{
		
		long numPendientes = delegator.findCountByAnd("ReporteWorkFlow", UtilMisc.toMap("workFlowId", workFlowId,"statusId","PENDIENTE_E"));
		return (numPendientes > 0);
			
	}
	
public static Map<String,? extends Object> guardNuevoFirmante(DispatchContext dctx, Map<String,? extends Object> context) throws EntityNotFoundException, RepositoryException, DocumentException {
		
		Map<String,? extends Object> retorno = ServiceUtil.returnSuccess();
		Delegator delegator = dctx.getDelegator();
		String workFlowId = (String) context.get("workFlowId");
		String reporteId = (String) context.get("reporteId");
		//ByteBuffer certificado = (ByteBuffer) context.get("certificado");
		ByteBuffer clavePrivada = (ByteBuffer) context.get("clavePrivada");
		String clave = (String) context.get("clave");
		GenericValue userLogin = (GenericValue) context.get("userLogin");		
		
		try{		
			
			//validaCertificado(certificado.array());
			
			PrivateKey privKey = validaLlavePrivada(clavePrivada.array(), clave.toCharArray());
			
			ByteBuffer pdf = obtenerPDF(delegator, workFlowId);					
			
			//Obtener la lista de firmante
			registraNuevoFirmante(delegator, userLogin.getString("partyId"), reporteId, workFlowId, pdf, privKey);												
			
			if(!tienePedientesFirma(delegator, workFlowId)){
				firmaGuardaPDF(dctx.getDispatcher(), userLogin, delegator, workFlowId, pdf.array());
			}						
											
			
		} catch(CertificateException e){
			e.printStackTrace();
			return ServiceUtil.returnError("Error al leer el certificado");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("El algoritmo del certificado es incompatible");
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("El certificado es incorrecto");
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("No se pudo autenticar su firma");
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("No se pudo autenticar su firma");
		} catch (IOException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("Error al leer la llave privada");
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		
		return retorno;
	}

	/**
	 * Obtener el archivo pdf en la base de datos
	 * 
	 * @param delegator
	 * @param workFlowId
	 * @return 
	 * @return 
	 * @return
	 * @throws GenericEntityException 
	 */
	private static ByteBuffer obtenerPDF(Delegator delegator, String workFlowId) throws GenericEntityException 
	{	EntityCondition condicion = EntityCondition.makeCondition(EntityCondition.makeCondition("workFlowId", EntityOperator.EQUALS, workFlowId));		
		List<GenericValue> pdfList = delegator.findByCondition("ReportePdfWorkFlow", condicion, null, null);
		byte[] documentoByte;
		ByteBuffer documento = null;
		if(!pdfList.isEmpty())
		{	Iterator<GenericValue> pdfIter = pdfList.iterator();			
			while (pdfIter.hasNext())
			{	GenericValue pdf = pdfIter.next();										
				documentoByte = pdf.getBytes("pdfOriginal");
				documento = ByteBuffer.wrap(documentoByte);
			}
		}
		return documento;
	}
	
	/**
	 * Se registra el nuevo firmante 
	 * @param delegator
	 * @param partyId
	 * @param reporteId
	 * @param workFlowId
	 * @param pdf
	 * @param privKey
	 * @return workflowId
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws GenericEntityException 
	 */	
	private static void registraNuevoFirmante(Delegator delegator, String partyId, String reporteId, String workFlowId, ByteBuffer pdf, PrivateKey privKey) 
				throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, GenericEntityException{								
		GenericValue reporteWorkFlow = delegator.findByPrimaryKey("ReporteWorkFlow", UtilMisc.toMap("workFlowId", workFlowId, "reporteId", reporteId, "firmanteId", partyId));												
		reporteWorkFlow.set("statusId", FIRMADO);																				
		reporteWorkFlow.set("firma", firmaDocumento(pdf, privKey));										
		delegator.createOrStore(reporteWorkFlow);								
	}
	

}
