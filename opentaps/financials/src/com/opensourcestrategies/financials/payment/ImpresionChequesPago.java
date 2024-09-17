package com.opensourcestrategies.financials.payment;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.opentaps.domain.DomainsDirectory;
import org.opentaps.domain.DomainsLoader;
import org.opentaps.domain.party.Party;
import org.opentaps.domain.party.PartyRepositoryInterface;
import org.opentaps.foundation.entity.EntityNotFoundException;
import org.opentaps.foundation.infrastructure.InfrastructureException;
import org.opentaps.foundation.repository.RepositoryException;
import com.opensourcestrategies.financials.payment.UtilNumberToLetter;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

public class ImpresionChequesPago {
	
	/**
	 * Prepara la informacion para llenar el formato de cheques 
	 * @param request
	 * @param response
	 * @return
	 */
	public static String preparaChequePoliza(HttpServletRequest request, HttpServletResponse response){
		
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		Locale locale = UtilHttp.getLocale(request);
		TimeZone timeZone = UtilHttp.getTimeZone(request);
		
		String dateTimeFormat = UtilDateTime.getDateFormat(locale);
		
    	String paymentId = (String) request.getParameter("paymentId");
    	
    	String lugar = (String) request.getParameter("lugar");
    	String titular1 = (String) 	request.getParameter("titular1");
    	String puestoTitular1 = (String) request.getParameter("puestoTitular1");
    	String titular2 = (String) request.getParameter("titular2");
    	String puestoTitular2 = (String) request.getParameter("puestoTitular2");
    	String formuladoPor = (String) request.getParameter("formuladoPor");
    	String revisadoPor = (String) request.getParameter("revisadoPor");
    	String autorizadoPor = (String) request.getParameter("autorizadoPor");
    	String noCheque = (String) request.getParameter("noCheque");
    	String auxiliarPor = (String) request.getParameter("auxiliarPor"); 
    	String recibidoPor = (String) request.getParameter("recibidoPor");
    	Date fechaImpresion = null;
    	
    	String fechaString = request.getParameter("fechaImpresion");
    	
    	if(UtilValidate.isNotEmpty(fechaString)){
    		fechaImpresion = UtilDateTime.getDateFromString(dateTimeFormat, timeZone, locale, fechaString);
    	} else {
    		fechaImpresion = UtilDateTime.nowDate();
    	}
    	
    	
    	//Nombre del cheque a imprimir
    	String nombreCheque = (String) request.getParameter("nombreCheque");
    	//Se guardan los datos de la poliza
		try {
			
	    	DomainsLoader dl = new DomainsLoader(request);
	        DomainsDirectory dd = dl.getDomainsDirectory();
	        PartyRepositoryInterface partyRepository = dd.getPartyDomain().getPartyRepository();
			
			GenericValue Payment = delegator.findByPrimaryKey("Payment", UtilMisc.toMap("paymentId",paymentId));
			
			GenericValue PartyGroup = delegator.findByPrimaryKey("PartyGroup", UtilMisc.toMap("partyId",Payment.getString("partyIdTo")));
			GenericValue Person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId",Payment.getString("partyIdTo")));
			
			StringBuffer nombre = new StringBuffer();
			if(UtilValidate.isNotEmpty(PartyGroup)){
				nombre.append(PartyGroup.getString("groupName"));
			} else if (UtilValidate.isNotEmpty(Person)){
				nombre.append(Person.getString("firstName")+" ");
				nombre.append(Person.getString("middleName")+" ");
				nombre.append(Person.getString("lastName"));
			}
			
			
			List<GenericValue> listAcctg = delegator.findByAnd("AcctgTrans", UtilMisc.toMap("paymentId",paymentId));
			String descripcionPoliza = new String();
			String noPoliza = new String();
			if(!UtilValidate.isEmpty(listAcctg)){
				GenericValue AcctgTrans = listAcctg.get(0);
				descripcionPoliza = AcctgTrans.getString("description");
				String acctgTransId = AcctgTrans.getString("acctgTransId");
				noPoliza = AcctgTrans.getString("poliza");
				guardaTransacciones(delegator, request, acctgTransId);
				//Se actualizan los datos del cheque
				AcctgTrans.set("beneficiarioCheque", nombre.toString());
				AcctgTrans.set("numeroCheque", noCheque);
				delegator.store(AcctgTrans);
			}
			
			BigDecimal decimales = Payment.getBigDecimal("amount").remainder(BigDecimal.ONE);
			String decimalStr = new DecimalFormat(".00").format(decimales);
			decimalStr = decimalStr.replace(".", "");
			
			Party partyTit1 = partyRepository.getPartyById(titular1);
			Party partyTit2 = partyRepository.getPartyById(titular2);
			Party partyFor = partyRepository.getPartyById(formuladoPor);
			Party partyRev = partyRepository.getPartyById(revisadoPor);
			Party partyAut = partyRepository.getPartyById(autorizadoPor);
			Party partyAux = partyRepository.getPartyById(auxiliarPor);
			Party partyRec = partyRepository.getPartyById(recibidoPor);
			
	        Map<String, Object> jrParameters = new FastMap<String, Object>();
	        jrParameters.put("Lugar", lugar);
	        jrParameters.put("Beneficiario", PartyGroup.getString("groupName"));
	        jrParameters.put("MontoTotal", new DecimalFormat("###,###,###,##0.00##").format(Payment.getBigDecimal("amount")));
	        jrParameters.put("MontoTotalTexto", "SON ("+UtilNumberToLetter.convertNumberToLetter(Payment.getBigDecimal("amount").doubleValue())+" M.N.)");
	        jrParameters.put("Descripcion", descripcionPoliza);
	        jrParameters.put("Titular1", (partyTit1 == null ? "" : partyTit1.getName()));
	        jrParameters.put("PuestoTitular1", puestoTitular1);
	        jrParameters.put("Titular2", (partyTit2 == null ? "" : partyTit2.getName()));
	        jrParameters.put("PuestoTitular2", puestoTitular2);
	        jrParameters.put("Formulo", (partyFor == null ? "" : partyFor.getName()));
	        jrParameters.put("Reviso", (partyRev == null ? "" : partyRev.getName()));
	        jrParameters.put("Autorizo", (partyAut == null ? "" : partyAut.getName()));
	        jrParameters.put("NumeroCheque", noCheque);
	        jrParameters.put("NumeroCuenta", Payment.getRelatedOne("CuentaBancaria").getString("numeroCuenta"));
	        jrParameters.put("Auxiliar", (partyAux == null ? "" : partyAux.getName()));
	        jrParameters.put("Recibido", (partyRec == null ? "" : partyRec.getName()));
	        jrParameters.put("NumeroPoliza", noPoliza);
	        jrParameters.put("FechaImpresion", fechaImpresion);
	        
	        
	        request.setAttribute("jrParameters", jrParameters);
	        
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return "error";
		} catch (InfrastructureException e) {
			e.printStackTrace();
			return "error";
		} catch (RepositoryException e) {
			e.printStackTrace();
			return "error";
		} catch (EntityNotFoundException e) {
			e.printStackTrace();
			return "error";
		}    	
		
		return nombreCheque;
	}
	
	/**
	 * Metodo que guarda el detalle de la poliza para el reporte 
	 * @param delegator
	 * @param request
	 * @param acctgTransId
	 * @throws GenericEntityException
	 */
	public static void guardaTransacciones(Delegator delegator,HttpServletRequest request,String acctgTransId) throws GenericEntityException{
		
        List<Map<String, Object>> reportData = FastList.newInstance();
		List<GenericValue> listAcctgEntry = delegator.findByAnd("AcctgTransEntry", UtilMisc.toMap("acctgTransId",acctgTransId));
		for (GenericValue acctgTransEntry : listAcctgEntry) {
			Map<String, Object> mapaPoliza = FastMap.newInstance();
			mapaPoliza.put("accountCode",acctgTransEntry.getString("glAccountId"));
			mapaPoliza.put("accountName",acctgTransEntry.getRelatedOne("GlAccount").getString("accountName"));
			mapaPoliza.put("description",acctgTransEntry.getString("description"));
			if(acctgTransEntry.getString("debitCreditFlag").equals("C")){
				mapaPoliza.put("amountDebit",BigDecimal.ZERO);
				mapaPoliza.put("amountCredit",acctgTransEntry.getBigDecimal("amount"));
			} else {
				mapaPoliza.put("amountDebit",acctgTransEntry.getBigDecimal("amount"));
				mapaPoliza.put("amountCredit",BigDecimal.ZERO);
			}
			
			reportData.add(mapaPoliza);
		}
		
        JRMapCollectionDataSource datasource = new JRMapCollectionDataSource(reportData);
        request.setAttribute("jrDataSource", datasource);
        
	}
	
    public static String getNombreCorto(Party party){
    	
    	if(party != null){
    		int indiceNombre = party.getFirstName().indexOf(" ");
    		int indiceApellido = party.getLastName().indexOf(" ");
        	String nombre = party.getFirstName().substring(0, (indiceNombre>0 ? indiceNombre : party.getFirstName().length()));
        	String apellido = party.getLastName().substring(0, (indiceApellido>0 ? indiceApellido : party.getLastName().length()));
        	return nombre+" "+apellido;
        	
    	} else {
    		return "";
    	}

    }
    
    /**
     * Prepara los datos a enviar al jasper para generar un cheque simple con transacciones
     * @param request
     * @param response
     * @return
     */
    public static String preparaChequeSimple(HttpServletRequest request, HttpServletResponse response){
    	
    	Delegator delegator = (Delegator) request.getAttribute("delegator");
    	Locale locale = UtilHttp.getLocale(request);
		
    	String beneficiario = (String) request.getParameter("beneficiario");
    	String lugar = (String) request.getParameter("lugar");
    	String titular1 = (String) 	request.getParameter("titular1");
    	String puestoTitular1 = (String) request.getParameter("puestoTitular1");
    	String titular2 = (String) request.getParameter("titular2");
    	String puestoTitular2 = (String) request.getParameter("puestoTitular2");
    	
    	BigDecimal montoTotal = BigDecimal.valueOf(Double.valueOf(request.getParameter("monto")));
    	
    	String acctgTransId = (String) request.getParameter("acctgTransId");
    	String formuladoPor = (String) request.getParameter("formuladoPor");
    	String revisadoPor = (String) request.getParameter("revisadoPor");
    	String autorizadoPor = (String) request.getParameter("autorizadoPor");
    	String recibidoPor = (String) request.getParameter("recibidoPor");
    	String noCheque = (String) request.getParameter("noCheque");
    	
    	Date fechaImpresion = null;
    	
    	String fechaString = request.getParameter("fechaImpresion");
    	String dateFormat = UtilDateTime.getDateFormat(locale);
    	
    	if(UtilValidate.isNotEmpty(fechaString)){
    		fechaImpresion = UtilDateTime.stringToDate(fechaString, dateFormat);
    	} else {
    		fechaImpresion = UtilDateTime.nowDate();
    	}
    	
    	//Nombre del cheque a imprimir
    	String nombreCheque = (String) request.getParameter("nombreCheque");
    	
    	try {
    		
			guardaTransacciones(delegator, request, acctgTransId);
			
	    	DomainsLoader dl = new DomainsLoader(request);
	        DomainsDirectory dd = dl.getDomainsDirectory();
	        PartyRepositoryInterface partyRepository = dd.getPartyDomain().getPartyRepository();
	    	
			Party partyFor = partyRepository.getPartyById(formuladoPor);
			Party partyRev = partyRepository.getPartyById(revisadoPor);
			Party partyAut = partyRepository.getPartyById(autorizadoPor);
			Party partyRec = partyRepository.getPartyById(recibidoPor);
			
			BigDecimal decimales = montoTotal.remainder(BigDecimal.ONE);
			String decimalStr = new DecimalFormat(".00").format(decimales);
			decimalStr = decimalStr.replace(".", "");
			
			GenericValue AcctgTrans = delegator.findByPrimaryKey("AcctgTrans",UtilMisc.toMap("acctgTransId", acctgTransId));
			//Se actualizan los datos del cheque
			AcctgTrans.set("beneficiarioCheque", beneficiario);
			AcctgTrans.set("numeroCheque", noCheque);
			delegator.store(AcctgTrans);
			
			Party partyTit1 = partyRepository.getPartyById(titular1);
			Party partyTit2 = partyRepository.getPartyById(titular2);
			
	        Map<String, Object> jrParameters = new FastMap<String, Object>();
	        jrParameters.put("Lugar", lugar);
	        jrParameters.put("Titular1", (partyTit1 == null ? "" : partyTit1.getName()));
	        jrParameters.put("PuestoTitular1", puestoTitular1);
	        jrParameters.put("Titular2", (partyTit2 == null ? "" : partyTit2.getName()));
	        jrParameters.put("PuestoTitular2", puestoTitular2);
	        jrParameters.put("Beneficiario", beneficiario);
	        jrParameters.put("MontoTotal", new DecimalFormat("###,###,###,##0.00##").format(montoTotal));
	        jrParameters.put("MontoTotalTexto", "SON ("+UtilNumberToLetter.convertNumberToLetter(montoTotal.doubleValue())+" M.N.)");
	        jrParameters.put("Formulo", (partyFor == null ? "" : partyFor.getName()));
	        jrParameters.put("Reviso", (partyRev == null ? "" : partyRev.getName()));
	        jrParameters.put("Autorizo", (partyAut == null ? "" : partyAut.getName()));
	        jrParameters.put("Recibido", (partyRec == null ? "" : partyRec.getName()));
	        jrParameters.put("NumeroPoliza", AcctgTrans.getString("poliza"));
	        jrParameters.put("NumeroCheque", noCheque);
	        jrParameters.put("FechaImpresion", fechaImpresion);
	        
	        request.setAttribute("jrParameters", jrParameters);
        
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return "error";
		} catch (InfrastructureException e) {
			e.printStackTrace();
			return "error";
		} catch (RepositoryException e) {
			e.printStackTrace();
			return "error";
		} catch (EntityNotFoundException e) {
			e.printStackTrace();
			return "error";
		}
    	
    	return nombreCheque;
    }

}
