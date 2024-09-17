package com.opensourcestrategies.financials.reports;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilNumber;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.UtilXml;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.order.finaccount.UtilClavePresupuestal;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.base.entities.ElementoReporte;
import org.opentaps.common.builder.EntityListBuilder;
import org.opentaps.common.builder.PageBuilder;
import org.opentaps.common.event.AjaxEvents;
import org.opentaps.domain.DomainsDirectory;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.action.ActionContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.parser.XSOMParser;

public class ContabilidadElectronica {
	
	public static Map<String, String> mapaClassParent = null;
	public static List<String> listaTiposFinanciero = UtilMisc.toList("CMICatalogoCuentas","CMICatalogoFlujosEfectivo","CMICatalogoCuentasVariaciones");
	public static List<String> listaTiposPresupuestal = UtilMisc.toList("CMIFuenteDeIngresos","CMIEjercicioDelPresupuesto");
	public static final List<String> listaAtributosVariacion = UtilMisc.toList("patrimonioContribuido","patrimonioGeneradoEA","patrimonioGeneradoE","ajusteXCambioValor");
	public static final Map<String,Object> mapaElementosCRIFlujoEfectivo = UtilMisc.toMap(
										"OrigenIngresosPorCuotasYAportacionesDeSeguridadSocial","2",
										"OrigenIngresosPorVentaDeBienesYServicios","7",
										"OrigenTransferenciasYDonativos",UtilMisc.toList("9","912","922","962","982"),
										"OrigenSubsidiosYSubvenciones","93",
										"OrigenContribucionesCapital",UtilMisc.toList("311","62","629"),
										"OrigenVentasActivosFisicos",UtilMisc.toList("52","5215","5219","527"),
										"OrigenVentaDisposicionObjetosDeValor",UtilMisc.toList("5215","5219"),
										"OrigenVentaDisposicionActivosNoProducidos","527",
										"OrigenTransferenciasDeCapitalRecibidas",UtilMisc.toList("912","922","962","982"));
	public static final Map<String,Object> mapaElementosCOGFlujoEfectivo = UtilMisc.toMap(
										"AplicacionServiciosPersonales","1",
										"AplicacionMaterialesYSuministros","2",
										"AplicacionServiciosGenerales","3",
										"AplicacionTransferenciasAsignacionesSubsidiosOtrasAyudas","4",
										"AplicacionTransferenciasInternasYAsignacionesAlSectorPublico","41",
										"AplicacionTransferenciasAlRestoDelSectorPublico","42",
										"AplicacionSubsidiosYSubvenciones","43",
										"AplicacionAyudasSociales","44",
										"AplicacionPensionesYJubilaciones","45",
										"AplicacionTransferenciasFideicomisosMandatosContratosAnalogos","46",
										"AplicacionTransferenciasALaSeguridadSocial","47",
										"AplicacionDonativos","48",
										"AplicacionTransferenciasAlExterior","49",
										"AplicacionActivosFijos","6",
										"AplicacionObjetosDeValor",UtilMisc.toList("5","513","514","5700"),
										"AplicacionActivosNoProducidos",UtilMisc.toList("513","514"),
										"AplicacionOtros","57");
	public static final List<String> listActividadesDeFinanciamientoFlujoEfectivo = UtilMisc.toList("1.1.2","1.1.3","2.1.9");
	public static final String cuentaEfectivoYEquivalentesFlujoEfectivo = "1.1.1";
	
	private static final DecimalFormat df = new DecimalFormat("#0.0#");
	private static final String MODULE = ContabilidadElectronica.class.getName();
	
	public static String creaReporteContaXML(HttpServletRequest request, HttpServletResponse response){
		
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		Locale locale = UtilHttp.getLocale(request);
        TimeZone timeZone = UtilHttp.getTimeZone(request);
        
        Map<String, Object> parameters = UtilHttp.getParameterMap(request);
        
        String reporteId = (String) parameters.get("reporteId");
        String mes = (String) parameters.get("mes");
        String anio = (String) parameters.get("anio");
        String organizationPartyId = (String) parameters.get("organizationPartyId");
        
        try {
        	
        	GenericValue Organizacion = delegator.findByPrimaryKey("PartyGroup",UtilMisc.toMap("partyId",organizationPartyId));
        	
			DocumentBuilderFactory documentFactory = DocumentBuilderFactory
				     .newInstance();
				DocumentBuilder documentBuilder = documentFactory
				     .newDocumentBuilder();
        	
			String nombreArchivo = null;
	        Document documento = null;
	        if(reporteId.equals("CATALOGO_CUENTAS")){
	        	documento = creaCatalogoCuentas(delegator,documentBuilder,locale,timeZone,Organizacion,mes,anio);
	        	nombreArchivo = "catalogoCuentas.xml";
	        } else if(reporteId.equals("BALANZA")){
	        	documento = creaBalanza(delegator, documentBuilder, locale, timeZone, Organizacion, mes, anio);
	        	nombreArchivo = "balanza.xml";
	        } else if(reporteId.equals("POLIZAS")){
	        	documento = creaPolizas(delegator, documentBuilder, locale, timeZone, Organizacion, mes, anio);
	        	nombreArchivo = "polizas.xml";
	        } else if(reporteId.equals("FINANCIERO")) {
	        	documento = creaFinancieroXML(delegator, documentBuilder, locale, timeZone, Organizacion, mes, anio);
	        	nombreArchivo = "InformacionFinanciera.xml";
	        } else if(reporteId.equals("PRESUPUESTAL")) {
	        	documento = creaPresupuestalXML(delegator, documentBuilder, locale, timeZone, Organizacion, mes, anio);
	        	nombreArchivo = "InformacionPresupuestal.xml";
	        }
        
			creaArchivoXML(response, documento, nombreArchivo);
			
		} catch (IOException e) {
			e.printStackTrace();
			ServiceUtil.setMessages(request, "Ocurrio un error al generar el XML", null, null);
			return "error";
		} catch (GenericEntityException e) {
			e.printStackTrace();
			ServiceUtil.setMessages(request, "Ocurrio un error al generar el XML", null, null);
			return "error";
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			ServiceUtil.setMessages(request, "Ocurrio un error al generar el XML", null, null);
			return "error";
		} catch (SAXException e) {
			e.printStackTrace();
			ServiceUtil.setMessages(request, "Ocurrio un error al generar el XML", null, null);
			return "error";
		} catch (ClassCastException e) {
			e.printStackTrace();
			ServiceUtil.setMessages(request, "Ocurrio un error al generar el XML", null, null);
			return "error";
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			ServiceUtil.setMessages(request, "Ocurrio un error al generar el XML", null, null);
			return "error";
		} catch (InstantiationException e) {
			e.printStackTrace();
			ServiceUtil.setMessages(request, "Ocurrio un error al generar el XML", null, null);
			return "error";
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			ServiceUtil.setMessages(request, "Ocurrio un error al generar el XML", null, null);
			return "error";
		}

        
		return "success";
	}
	
	/**
	 * Crea el catalogo completo de cuentas
	 * @param delegator
	 * @param locale
	 * @param timeZone
	 * @param organizacion
	 * @param mes
	 * @param anio
	 * @return
	 * @throws GenericEntityException
	 * @throws ParserConfigurationException
	 */
	public static Document creaCatalogoCuentas(Delegator delegator,DocumentBuilder documentBuilder, Locale locale,
							TimeZone timeZone, GenericValue organizacion, String mes, String anio) 
												throws GenericEntityException, ParserConfigurationException{
		
	        //Obtiene un mapa de las clases (naturaleza de la cuentas)
	        getClasses(delegator); 
	        
			List<String> select = UtilMisc.toList("glAccountId","accountCode","accountName","nivelId","glAccountClassId","parentGlAccountId");
			List<String> orderBy = UtilMisc.toList("glAccountId");
			
			List<GenericValue> listCuentas = delegator.findByCondition("GlAccount", null, select, orderBy);
			
			// se crea el elemento principal
			Document document = documentBuilder.newDocument();
			Element Catalogo = document.createElement("Catalogo");
			Catalogo.setAttribute("version", "1.0");
			Catalogo.setAttribute("RFC",organizacion.getString("federalTaxId"));
			Catalogo.setAttribute("TotalCtas",Integer.toString(listCuentas.size()));
			Catalogo.setAttribute("Mes",mes);
			Catalogo.setAttribute("Ano",anio);
			document.appendChild(Catalogo);
			
			for (GenericValue objetoCuenta : listCuentas) {
				Element cuenta = document.createElement("Ctas");
				cuenta.setAttribute("CodAgrup",objetoCuenta.getString("glAccountId"));
				cuenta.setAttribute("NumCta",objetoCuenta.getString("glAccountId"));
				cuenta.setAttribute("Desc",objetoCuenta.getString("accountName"));
				if(objetoCuenta.getString("parentGlAccountId") != null){
					cuenta.setAttribute("SubCtaDe",objetoCuenta.getString("parentGlAccountId"));
				}
				cuenta.setAttribute("Nivel",objetoCuenta.getString("nivelId"));
				cuenta.setAttribute("Natur",getNaturaleza(objetoCuenta.getString("glAccountClassId")));
				Catalogo.appendChild(cuenta);
			}
			
			return document;
			
		}
	
	/**
	 * Crea el reporte de balanza en xml
	 * @param delegator
	 * @param documentBuilder
	 * @param locale
	 * @param timeZone
	 * @param organizacion
	 * @param mes
	 * @param anio
	 * @return
	 * @throws GenericEntityException
	 * @throws ParserConfigurationException
	 */
	public static Document creaBalanza(Delegator delegator,DocumentBuilder documentBuilder ,Locale locale,
			TimeZone timeZone, GenericValue organizacion, String mes, String anio) 
								throws GenericEntityException, ParserConfigurationException{
		
		//Se obtiene el periodo a consultar 
		Timestamp fechaInicioMes = UtilDateTime.toTimestamp(Integer.valueOf(mes), 1, Integer.valueOf(anio), 0, 0, 0);
		Timestamp fechaFinMes = UtilDateTime.getTimestamp(fechaInicioMes.getTime());
		fechaFinMes = UtilDateTime.adjustTimestamp(fechaFinMes, Calendar.MONTH, 1);
		Timestamp inicioAnio = UtilDateTime.getYearStart(fechaInicioMes);
		
        //Obtiene un mapa de las clases (naturaleza de la cuentas)
        getClasses(delegator); 
		
        //Se buscan las cuentas afectadas en el mes correspondiente
        Map<String,GenericValue> mapaSaldoActual = FastMap.newInstance();
        mapaSaldoActual = getSaldoMesActual(delegator, fechaInicioMes, fechaFinMes, organizacion,UtilMisc.toList());
        
		/*
		 * Aqui se obtienen los periodos anteriores para obtener el saldo inicial
		 */
        Map<String,BigDecimal> mapaSaldoInicial = FastMap.newInstance();
        mapaSaldoInicial = getSaldoInicial(delegator, fechaInicioMes, organizacion,UtilMisc.toList());
        
    	List<EntityExpr> condicionCuentasAfecta = UtilMisc.toList(EntityCondition.makeCondition("postedDate",EntityOperator.GREATER_THAN_EQUAL_TO,fechaInicioMes),
				EntityCondition.makeCondition("postedDate",EntityOperator.LESS_THAN_EQUAL_TO,fechaFinMes),
				EntityCondition.makeCondition("organizationPartyId",organizacion.getString("partyId")));
		
        List<GenericValue> listCuentasMes = delegator.findByCondition("CuentasAfectadasPeriodo", 
        		EntityCondition.makeCondition(condicionCuentasAfecta), UtilMisc.toList("glAccountId"), UtilMisc.toList("glAccountId"));
        List<String> listCuentasIds = FastList.newInstance();
        for (GenericValue CuentasAfectadasPeriodo : listCuentasMes) {
        	listCuentasIds.add(CuentasAfectadasPeriodo.getString("glAccountId"));
		}
        
	    /*
	     * Mapa de saldo de la carga inicial
	     */
	    Map<String,GenericValue> mapaSaldoCargaInicial = FastMap.newInstance();
	    if(mes.equals("01")){ //Se resta del saldo actual la carga inicial y se agrega al saldo inicial 
	    	mapaSaldoCargaInicial = getSaldoCargaInicial(delegator, listCuentasIds, organizacion, inicioAnio, timeZone, locale);
	    	restaCargaInicial(delegator, mapaSaldoActual, mapaSaldoCargaInicial);
	    	agregaCargaInicialSaldoInicial(delegator,mapaSaldoInicial, mapaSaldoCargaInicial,listCuentasIds);
	    }
		
		// se crea el elemento principal
		Document document = documentBuilder.newDocument();
		Element Balanza = document.createElement("Balanza");
		Balanza.setAttribute("version", "1.0");
		Balanza.setAttribute("RFC",organizacion.getString("federalTaxId"));
		Balanza.setAttribute("TotalCtas",Integer.toString(mapaSaldoActual.size()));
		Balanza.setAttribute("Mes",mes);
		Balanza.setAttribute("Ano",anio);
		document.appendChild(Balanza);
		
		if(mapaSaldoInicial != null && !mapaSaldoInicial.isEmpty()){
			for(Entry<String,BigDecimal> cuentasAnteriores : mapaSaldoInicial.entrySet()){
				String glAccountId = cuentasAnteriores.getKey();
				BigDecimal saldoInicial = cuentasAnteriores.getValue();
				saldoInicial = (saldoInicial == null ? BigDecimal.ZERO : saldoInicial);
				
				GenericValue GlHistoryActual = mapaSaldoActual.get(cuentasAnteriores.getKey());
				GenericValue GlAccount = null;
				if(GlHistoryActual == null){
					GlHistoryActual = delegator.makeValue("GlAccountHistory");
					GlAccount = delegator.findByPrimaryKeyCache("GlAccount", UtilMisc.toMap("glAccountId",cuentasAnteriores.getKey()));
				} else {
					GlAccount = GlHistoryActual.getRelatedOne("GlAccount");
				}
				String glAccountClassId = GlAccount.getString("glAccountClassId");
				BigDecimal saldoFinal = BigDecimal.ZERO;
				BigDecimal credit = ( ( GlHistoryActual == null || GlHistoryActual.getBigDecimal("postedCredits")== null ) ? BigDecimal.ZERO : GlHistoryActual.getBigDecimal("postedCredits"));
				BigDecimal debit = ( ( GlHistoryActual == null ||GlHistoryActual.getBigDecimal("postedDebits") == null ) ? BigDecimal.ZERO : GlHistoryActual.getBigDecimal("postedDebits"));
				if(getNaturaleza(glAccountClassId).equals("CREDIT")){
					saldoFinal = saldoInicial.add(credit.subtract(debit));
				} else if(getNaturaleza(glAccountClassId).equals("DEBIT")){
					saldoFinal = saldoInicial.add(debit.subtract(credit));
				}
				
				Element cuenta = document.createElement("Ctas");
				cuenta.setAttribute("NumCta",glAccountId);
				cuenta.setAttribute("SaldoIni",new DecimalFormat("0.00").format(saldoInicial));
				cuenta.setAttribute("Debe",new DecimalFormat("0.00").format(debit));
				cuenta.setAttribute("Haber",new DecimalFormat("0.00").format(credit));
				cuenta.setAttribute("SaldoFin",new DecimalFormat("0.00").format(saldoFinal));
				Balanza.appendChild(cuenta);
			
			}
		} else {
			for(Entry<String,GenericValue> cuentasHisEntry : mapaSaldoActual.entrySet()){
				String glAccountId = cuentasHisEntry.getKey();
				BigDecimal saldoInicial = BigDecimal.ZERO;

				GenericValue GlHistoryActual = mapaSaldoActual.get(glAccountId);
				GenericValue GlAccount = null;
				if(GlHistoryActual == null){
					GlHistoryActual = delegator.makeValue("GlAccountHistory");
					GlAccount = delegator.findByPrimaryKeyCache("GlAccount", UtilMisc.toMap("glAccountId",glAccountId));
				} else {
					GlAccount = GlHistoryActual.getRelatedOne("GlAccount");
				}
				String glAccountClassId = GlAccount.getString("glAccountClassId");
				BigDecimal saldoFinal = BigDecimal.ZERO;
				BigDecimal credit = ( ( GlHistoryActual == null || GlHistoryActual.getBigDecimal("postedCredits")== null )
												? BigDecimal.ZERO : GlHistoryActual.getBigDecimal("postedCredits"));
				BigDecimal debit = ( ( GlHistoryActual == null ||GlHistoryActual.getBigDecimal("postedDebits") == null ) 
												? BigDecimal.ZERO : GlHistoryActual.getBigDecimal("postedDebits"));
				if(getNaturaleza(glAccountClassId).equals("CREDIT")){
					saldoFinal = saldoInicial.add(credit.subtract(debit));
				} else if(getNaturaleza(glAccountClassId).equals("DEBIT")){
					saldoFinal = saldoInicial.add(debit.subtract(credit));
				}
				
				Element cuenta = document.createElement("Ctas");
				cuenta.setAttribute("NumCta",glAccountId);
				cuenta.setAttribute("SaldoIni",new DecimalFormat("0.00").format(saldoInicial));
				cuenta.setAttribute("Debe",new DecimalFormat("0.00").format(debit));
				cuenta.setAttribute("Haber",new DecimalFormat("0.00").format(credit));
				cuenta.setAttribute("SaldoFin",new DecimalFormat("0.00").format(saldoFinal));
				Balanza.appendChild(cuenta);
			
			}
		}

		
		return document;
	}
	
	/**
	 * Crea el xml de las polizas
	 * @param delegator
	 * @param documentBuilder
	 * @param locale
	 * @param timeZone
	 * @param organizacion
	 * @param mes
	 * @param anio
	 * @return
	 * @throws GenericEntityException
	 */
	public static Document creaPolizas(Delegator delegator,DocumentBuilder documentBuilder ,Locale locale,
			TimeZone timeZone, GenericValue organizacion, String mes, String anio) throws GenericEntityException {
		
		//Se obtiene el periodo a consultar 
		Timestamp fechaInicioMes = UtilDateTime.toTimestamp(Integer.valueOf(mes), 1, Integer.valueOf(anio), 0, 0, 0);
		Timestamp fechaFinMes = UtilDateTime.getTimestamp(fechaInicioMes.getTime());
		fechaFinMes = UtilDateTime.adjustTimestamp(fechaFinMes, Calendar.MONTH, 1);
		
		List<String> orderBy = UtilMisc.toList("acctgTransId");
		List<EntityExpr> condiciones = UtilMisc.toList(
					EntityCondition.makeCondition("postedDate",EntityOperator.GREATER_THAN_EQUAL_TO,fechaInicioMes),
					EntityCondition.makeCondition("postedDate",EntityOperator.LESS_THAN_EQUAL_TO,fechaFinMes),
					EntityCondition.makeCondition("organizationPartyId",EntityOperator.EQUALS,organizacion.getString("partyId")));
		
		List<GenericValue> listCuentas = delegator.findByCondition("AcctgTrans", EntityCondition.makeCondition(condiciones), null, orderBy);
		
		List<String> acctgTransIds = FastList.newInstance();
		for (GenericValue acctgTrans : listCuentas) {
			acctgTransIds.add(acctgTrans.getString("acctgTransId"));
		}
		
		List<EntityExpr> condicion = UtilMisc.toList(EntityCondition.makeCondition("acctgTransId",EntityOperator.IN,acctgTransIds),
												EntityCondition.makeCondition("organizationPartyId",EntityOperator.EQUALS,organizacion.getString("partyId")));
		
		List<GenericValue> listCuentasEntry = delegator.findByCondition("AcctgTransEntrySumSimple", EntityCondition.makeCondition(condicion), null, orderBy);
		
		Map<String,List<GenericValue>> mapaEntries = FastMap.newInstance();
		for (GenericValue entry : listCuentasEntry) {
			List<GenericValue> listEntry = FastList.newInstance();
			String acctgTransId = entry.getString("acctgTransId");
			if(mapaEntries.containsKey(acctgTransId)){
				listEntry = mapaEntries.get(acctgTransId);
				listEntry.add(entry);
				mapaEntries.put(acctgTransId, listEntry);
			} else {
				listEntry.add(entry);
				mapaEntries.put(acctgTransId, listEntry);
			}
		}
		
		// se crea el elemento principal
		Document document = documentBuilder.newDocument();
		Element Polizas = document.createElement("Polizas");
		Polizas.setAttribute("version", "1.0");
		Polizas.setAttribute("RFC",organizacion.getString("federalTaxId"));
		Polizas.setAttribute("TotalCtas",Integer.toString(listCuentas.size()));
		Polizas.setAttribute("Mes",mes);
		Polizas.setAttribute("Ano",anio);
		document.appendChild(Polizas);
		
		for (GenericValue cuentasHis : listCuentas) {
			Element Poliza = document.createElement("Poliza");
			Poliza.setAttribute("NumUnIdenPol",cuentasHis.getString("poliza"));
			Poliza.setAttribute("Fecha",cuentasHis.getTimestamp("postedDate").toString());
			Poliza.setAttribute("Concepto",cuentasHis.getString("description"));
			
			List<GenericValue> listEntry = mapaEntries.get(cuentasHis.getString("acctgTransId"));
			for (GenericValue entry : listEntry) {
				Element Transaccion = document.createElement("Transaccion");
				Transaccion.setAttribute("NumCta",entry.getString("glAccountId"));
				Transaccion.setAttribute("Concepto",entry.getString("description"));
				if(entry.getString("debitCreditFlag").equals("D")){
					Transaccion.setAttribute("Debe",new DecimalFormat("0.00").format(entry.getBigDecimal("amount")));
					Transaccion.setAttribute("Haber",new DecimalFormat("0.00").format(BigDecimal.ZERO));
				} else if(entry.getString("debitCreditFlag").equals("C")) {
					Transaccion.setAttribute("Debe",new DecimalFormat("0.00").format(BigDecimal.ZERO));
					Transaccion.setAttribute("Haber",new DecimalFormat("0.00").format(entry.getBigDecimal("amount")));
				}
				Transaccion.setAttribute("Moneda","MXN");
				Transaccion.setAttribute("TipCamb","1");
				
				Poliza.appendChild(Transaccion);
			}
			
			Polizas.appendChild(Poliza);
		}
		
		return document;
	}
	
	/**
	 * Metodo generico para crear un archivo XML a partir de un "Document"
	 * @param response
	 * @param document
	 * @param nombreArchivo 
	 * @throws IOException
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 * @throws ClassCastException 
	 */
	public static void creaArchivoXML(HttpServletResponse response,Document document, String nombreArchivo) throws IOException, ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException{
		
		response.setHeader("Content-Disposition", "attachment; filename="+nombreArchivo);
		response.getWriter().write(UtilXml.writeXmlDocument(document));
		
	}

	/**
	 * Obtiene y guarda el mapa de clases de las cuentas , solo si no existe 
	 * @param delegator
	 * @throws GenericEntityException
	 */
	public static void getClasses(Delegator delegator) throws GenericEntityException{
			mapaClassParent = FastMap.newInstance();
			List<GenericValue> classes = delegator.findAllCache("GlAccountClass");
			if(classes.isEmpty()){
				classes = delegator.findAll("GlAccountClass");
			}
			for (GenericValue clase : classes) {
				mapaClassParent.put(clase.getString("glAccountClassId"),clase.getString("parentClassId"));
			}
	}
	
	/**
	 * Obtiene la naturaleza principal 
	 * @param glAccountClassId
	 * @return
	 */
	public static String getNaturaleza(String glAccountClassId){
		String naturalezaPadre = new String();
		
		if(mapaClassParent.containsKey(glAccountClassId)){
			naturalezaPadre = mapaClassParent.get(glAccountClassId);
			while(mapaClassParent.get(naturalezaPadre) != null){
				naturalezaPadre = mapaClassParent.get(naturalezaPadre);
			} 
		} else {
			return glAccountClassId;
		}
		
		return naturalezaPadre;
	}
	
	/**
	 * Guarda el elemento del reporte xml
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String,? extends Object> guardaElementoXML(DispatchContext dctx, Map<String,? extends Object> context) {
		
		Map<String, Object> mapaRegreso = FastMap.newInstance();
		Delegator delegator = dctx.getDelegator();
		
		try {
			GenericValue elementoReporte = GenericValue.create(delegator.getModelEntity("ElementoReporte"));
			if(UtilValidate.isNotEmpty(context.get("tipoReporteId")) //Verifica si el reporte es Financiero se borra el enumId 
					&& context.get("tipoReporteId").equals("FINANCIERO")){
				if(UtilValidate.isNotEmpty(context.get("enumId"))){
					context.put("enumId", null);
				}
				if(UtilValidate.isEmpty(context.get("glAccountId"))){
					throw new GenericEntityException("Necesita ingresar la cuenta");
				}
			} else {
				if(UtilValidate.isNotEmpty(context.get("glAccountId"))){
					context.put("glAccountId", null);
				}
				if(UtilValidate.isEmpty(context.get("enumId"))){
					throw new GenericEntityException("Necesita ingresar la clasificaci\u00f3n");
				}
			}
			elementoReporte.setAllFields(context, false, null, null);
			delegator.create(elementoReporte);
			
			mapaRegreso = ServiceUtil.returnSuccess("Se guard\u00f3 correctamente el elemento");
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
			mapaRegreso = ServiceUtil.returnError("No se pudo guardar el elemento "+e.getMessage());
		}
		
		return mapaRegreso;
	}
	
	
	/**
	 * Busqueda de Elementos del Reporte XML
	 * @param context
	 * @throws GeneralException
	 * @throws ParseException
	 */
	public static void consultaElementoReporte(Map<String, Object> context)
			throws GeneralException, ParseException {
		

		final ActionContext ac = new ActionContext(context);
		DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
		final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain()
				.getLedgerRepository();
		
		List<String> select = UtilMisc.toList("elementoReporteId","tipoReporteId","glAccountId","enumId");
		List<String> orderBy = UtilMisc.toList("tipoReporteId","elementoReporteId");
		EntityListBuilder elementoListBuilder = null;
		PageBuilder<ElementoReporte> pageBuilderElemento = null;
		
		elementoListBuilder = new EntityListBuilder(ledgerRepository, ElementoReporte.class, 
				null,select, orderBy);
		
		pageBuilderElemento = new PageBuilder<ElementoReporte>() {
			@Override
			public List<Map<String, Object>> build(List<ElementoReporte> page) throws Exception {
				List<Map<String, Object>> newPage = FastList.newInstance();
				for (ElementoReporte elemento : page) 
				{	Map<String, Object> newRow = FastMap.newInstance();
					newRow.putAll(elemento.toMap());
					
					if(UtilValidate.isNotEmpty(elemento.getGlAccount())){
						newRow.put("nombreCuenta", elemento.getGlAccount().getAccountName());
					}
					
					if(UtilValidate.isNotEmpty(elemento.getEnumeration())){
						newRow.put("nombreClasificacion", elemento.getEnumeration().getDescription());
					}
					
					newPage.add(newRow);
//					Debug.logInfo("NEW PAGE listElementoListBuilder Row "+newRow,MODULE);
				}
//				Debug.logInfo("NEW PAGE listElementoListBuilder Page "+newPage,MODULE);
				return newPage;
			
			}
		};
		
		elementoListBuilder.setPageBuilder(pageBuilderElemento);
        ac.put("elementoListBuilder", elementoListBuilder);
        
	}
	
	/**
	 * Ajax que obtiene los elementos de un reporte para asociarlos con su cuenta correspondiente
	 * @param request
	 * @param response
	 * @return
	 */
	public static String getElementosXML(HttpServletRequest request, HttpServletResponse response){
		
		String tipoReporteId = (String) request.getParameter("tipoReporteId");
		
		Map<String,String> resultado = FastMap.newInstance();
		
		String urlReporte = new String();
		List<String> listaTipos = FastList.newInstance();
		if(UtilValidate.isNotEmpty(tipoReporteId)){
			if(tipoReporteId.equals("FINANCIERO")){
				urlReporte = "/opentaps/financials/config/Financiero.xsd";
				listaTipos = listaTiposFinanciero;
			} else if (tipoReporteId.equals("PRESUPUESTAL")){
				urlReporte = "/opentaps/financials/config/CMISIFASCPIPresupuestal_v2.xsd";
				listaTipos = listaTiposPresupuestal;
			}
			
			try {
				
				resultado = getElementosReporteXML(urlReporte, listaTipos);
				
			} catch (SAXException e) {
				e.printStackTrace();
				return AjaxEvents.doJSONResponse(response, resultado);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return AjaxEvents.doJSONResponse(response, resultado);
			}
		}

		return AjaxEvents.doJSONResponse(response, resultado);
	}
	
	/**
	 * Obtiene los elementos del archivo xml para reportes
	 * @param urlReporte
	 * @param listaTipos
	 * @return
	 * @throws SAXException
	 * @throws IOException 
	 */
	private static Map<String, String> getElementosReporteXML(String urlReporte, List<String> listaTipos) throws SAXException, IOException{
			
		Map<String, String> mapaDatos = FastMap.newInstance();
		String filePath = new File("").getAbsolutePath();
		filePath = filePath+urlReporte;
		
		XSOMParser xsom = new XSOMParser(SAXParserFactory.newInstance());
		xsom.parse(new File(filePath));
		XSSchemaSet schema = xsom.getResult();
		XSSchema s = schema.getSchema(1);
		for (String tipo : listaTipos) {
			XSParticle[] particles = getElementosDelTipo(s, tipo);
		    for (XSParticle xsParticle : particles) {
		    	String nombreCampo = xsParticle.getTerm().asElementDecl().getName();
		    	if(urlReporte.contains("Financiero") && (nombreCampo.startsWith("c")  || nombreCampo.startsWith("v"))){
		    		mapaDatos.put(nombreCampo, nombreCampo);
		    	} else if(urlReporte.contains("Presupuestal")) {
		    		mapaDatos.put(nombreCampo, nombreCampo);
		    	}
			}
		}
		
		return mapaDatos;
	}
	
	/**
	 * Obtiene el Xschema del reporte XML para obtener sus elementos
	 * @param urlReporte
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 */
	private static XSSchema getXSSchemaReporteXML(String urlReporte) throws SAXException, IOException{
		
			String filePath = new File("").getAbsolutePath();
			filePath = filePath+urlReporte;
			XSOMParser xsom = new XSOMParser(SAXParserFactory.newInstance());
			xsom.parse(new File(filePath));
			XSSchemaSet schema = xsom.getResult();
		
		return schema.getSchema(1); 
	}
	
	/**
	 * Obtiene los elementos de un tipo complejo de un xsschema
	 * @param XSSchema 
	 * @param String
	 * @return
	 */
	private static XSParticle[] getElementosDelTipo(XSSchema s, String tipo){
		
		XSComplexType CMICatalogoCuentas = s.getComplexType(tipo);
		XSContentType content = CMICatalogoCuentas.getContentType();
		XSParticle p2 = content.asParticle();
		
		return p2.getTerm().asModelGroup().getChildren();
		
	}
	
	/**
	 * Obtiene una lista de GenericValue con periodos de tiempo (CustomTimePeriod)
	 * @param delegator
	 * @param fechaInicial
	 * @param fechaFinal
	 * @param periodTypeId
	 * @param organizationPartyId
	 * @return
	 * @throws GenericEntityException
	 */
	private static List<GenericValue> getListaPeriodos(Delegator delegator,Timestamp fechaInicial, 
							Timestamp fechaFinal, String periodTypeId,String organizationPartyId) throws GenericEntityException{
		
		
		List<EntityExpr> condicionPeriodo = UtilMisc.toList(EntityCondition.makeCondition("fromDate",EntityOperator.GREATER_THAN_EQUAL_TO,fechaInicial),
				EntityCondition.makeCondition("thruDate",EntityOperator.LESS_THAN_EQUAL_TO,fechaFinal),
				EntityCondition.makeCondition("periodTypeId",EntityOperator.EQUALS,periodTypeId),
				EntityCondition.makeCondition("organizationPartyId",EntityOperator.EQUALS,organizationPartyId));
		
		return delegator.findByAnd("CustomTimePeriod",condicionPeriodo);
	}
	
	/**
	 * Obtiene la lista de GenericValue con periodos de tiempo para generar el saldo inicial
	 * @param delegator
	 * @param fechaInicialMes
	 * @param periodTypeId
	 * @param organizationPartyId
	 * @return
	 * @throws GenericEntityException
	 */
	private static List<GenericValue> getListaPeriodosSaldoInicial(Delegator delegator,Timestamp fechaInicialMes
			, String periodTypeId,String organizationPartyId) throws GenericEntityException{

		List<EntityExpr> condicionPeriodo = UtilMisc.toList(EntityCondition.makeCondition("fromDate",EntityOperator.LESS_THAN,fechaInicialMes),
		EntityCondition.makeCondition("periodTypeId",EntityOperator.EQUALS,periodTypeId),
		EntityCondition.makeCondition("organizationPartyId",EntityOperator.EQUALS,organizationPartyId));
		
		return delegator.findByAnd("CustomTimePeriod",condicionPeriodo);
	
	}
	
	/**
	 * Obtiene el mapa con las cuentas del saldo del mes de busqueda <Cuenta, GlAccountHistory>
	 * @param delegator
	 * @param mes
	 * @param anio
	 * @param organizacion
	 * @return
	 * @throws GenericEntityException
	 */
	private static Map<String,GenericValue> getSaldoMesActual(Delegator delegator,Timestamp fechaInicioMes, Timestamp fechaFinMes, GenericValue organizacion, List<String> listCuentasId) throws GenericEntityException{
		
		//Se obtiene el periodo a buscar depende del mes y anio
		List<GenericValue> listPeriodo = getListaPeriodos(delegator, fechaInicioMes, fechaFinMes, "FISCAL_MONTH", organizacion.getString("partyId"));
		
		GenericValue Periodo = null;
		if(listPeriodo != null && !listPeriodo.isEmpty()){
			Periodo = listPeriodo.get(0);
		}
		
		if(Periodo == null){
			return FastMap.newInstance();
		}
		
		List<EntityExpr> listaCondicionesId = FastList.newInstance();
		//Se buscan las cuentas 
		for (String cuentas : listCuentasId) {
			listaCondicionesId.add(EntityCondition.makeCondition("glAccountId",EntityOperator.LIKE,cuentas+"%"));
		}
		
		
		EntityCondition condiciones = null;
		if(UtilValidate.isEmpty(listCuentasId)){
			condiciones = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("customTimePeriodId",Periodo.getString("customTimePeriodId")),
					EntityCondition.makeCondition("organizationPartyId",organizacion.getString("partyId"))
					);
		} else {
			condiciones = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition("customTimePeriodId",Periodo.getString("customTimePeriodId")),
					EntityCondition.makeCondition("organizationPartyId",organizacion.getString("partyId")),
					EntityCondition.makeCondition(listaCondicionesId,EntityOperator.OR)
					);
		}
		
		//Se obtienen las cuentas afectadas en el mes
		List<GenericValue> listCuentas = delegator.findByCondition("GlAccountHistory", condiciones, null, null);
		Map<String,GenericValue> mapaSaldoActual = FastMap.newInstance();
		for (GenericValue cuentaHis : listCuentas) {
			mapaSaldoActual.put(cuentaHis.getString("glAccountId"), cuentaHis);
		}
		
		return mapaSaldoActual;
		
	}
	
	/**
	 * Obtiene el saldo inicial de la lista de cuentas de un periodo dado <Cuenta, Monto>
	 * @param delegator
	 * @param fechaInicioAnio
	 * @param fechaInicioMes
	 * @param organizacion
	 * @return
	 * @throws GenericEntityException
	 */
	private static Map<String,BigDecimal> getSaldoInicial(Delegator delegator, Timestamp fechaInicioMes, 
								GenericValue organizacion, List<String> listCuentasId) throws GenericEntityException{
		
		List<GenericValue> listPeriodoSI = getListaPeriodosSaldoInicial(delegator, fechaInicioMes, "FISCAL_MONTH", organizacion.getString("partyId"));
		
		List<String> periodosIds = FastList.newInstance();
		for (GenericValue periodo : listPeriodoSI) {
			periodosIds.add(periodo.getString("customTimePeriodId"));
		}
		
		List<EntityExpr> listaCondicionesId = FastList.newInstance();
		//Se buscan las cuentas 
		for (String cuentas : listCuentasId) {
			listaCondicionesId.add(EntityCondition.makeCondition("glAccountId",EntityOperator.LIKE,cuentas+"%"));
		}
		
		EntityCondition condiciones = null;
		if(UtilValidate.isEmpty(listCuentasId)){
		 	condiciones = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("customTimePeriodId",EntityOperator.IN,periodosIds),
				EntityCondition.makeCondition("organizationPartyId",organizacion.getString("partyId"))
				);
		} else {
		 	condiciones = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("customTimePeriodId",EntityOperator.IN,periodosIds),
				EntityCondition.makeCondition("organizationPartyId",organizacion.getString("partyId")),
				EntityCondition.makeCondition(listaCondicionesId,EntityOperator.OR)
				);
		}
		
		List<String> select = UtilMisc.toList("glAccountId","postedDebits","postedCredits");
		List<String> orderBy = UtilMisc.toList("glAccountId");
		
		List<GenericValue> listCuentasAnteriores = delegator.findByCondition("GlAccountHistorySumPeriod", condiciones, select, orderBy);
		Map<String,BigDecimal> mapaSaldoInicial = FastMap.newInstance();
		
		for (String glAccountId : listCuentasId) {
			
			for (GenericValue cuentasHis : listCuentasAnteriores){
				
				if(cuentasHis.getString("glAccountId").startsWith(glAccountId)){
					
					BigDecimal montoInicial = BigDecimal.ZERO;
					BigDecimal montoAnterior = BigDecimal.ZERO;
					GenericValue GlAccountClass = cuentasHis.getRelatedOne("GlAccount");
					BigDecimal credit = (cuentasHis.getBigDecimal("postedCredits") == null ? BigDecimal.ZERO : cuentasHis.getBigDecimal("postedCredits"));
					BigDecimal debit = (cuentasHis.getBigDecimal("postedDebits") == null ? BigDecimal.ZERO : cuentasHis.getBigDecimal("postedDebits"));
					if(getNaturaleza(GlAccountClass.getString("glAccountClassId")).equals("CREDIT")){
						montoInicial = credit.subtract(debit);
					} else if(getNaturaleza(GlAccountClass.getString("glAccountClassId")).equals("DEBIT")){
						montoInicial = debit.subtract(credit);
					}
					
					if(UtilValidate.isNotEmpty(mapaSaldoInicial.get(glAccountId))){
						montoAnterior = mapaSaldoInicial.get(glAccountId);
					}
					
					mapaSaldoInicial.put(glAccountId, montoInicial.add(montoAnterior));
					
					continue;
					
				}
				

			}
			
		}
		

		
		return mapaSaldoInicial;
	}
	
	/**
	 * Obtiene el saldo inicial de la carga incial 'SALDO_INICIAL'
	 * @param delegator
	 * @param listCuentasId
	 * @param organizacion
	 * @param fechaInicioAnio
	 * @param timeZone
	 * @param locale
	 * @return
	 * @throws GenericEntityException
	 */
	private static Map<String,GenericValue> getSaldoCargaInicial (Delegator delegator, List<String> listCuentasId,GenericValue organizacion,
																	Timestamp fechaInicioAnio, TimeZone timeZone, Locale locale) throws GenericEntityException{
		
		String organizacionId = organizacion.getString("partyId");
		
		List<EntityExpr> listaCondicionesId = FastList.newInstance();
		//Se buscan las cuentas 
		for (String cuentas : listCuentasId) {
			listaCondicionesId.add(EntityCondition.makeCondition("glAccountId",EntityOperator.LIKE,cuentas+"%"));
		}
		
		//Se obtienen los saldos iniciales , cargados como 'SALDO_INICIAL'
		List<String>select = UtilMisc.toList("glAccountId","debitCreditFlag","amount","glAccountClassId");
		List<String> orderBy = UtilMisc.toList("glAccountId");
		
		EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
	 		EntityCondition.makeCondition("acctgTransTypeId",EntityOperator.EQUALS,"SALDO_INICIAL"),
	 		EntityCondition.makeCondition("organizationPartyId",EntityOperator.EQUALS,organizacionId),
	 		EntityCondition.makeCondition("isPosted",EntityOperator.EQUALS,"Y"),
			EntityCondition.makeCondition("postedDate",EntityOperator.GREATER_THAN_EQUAL_TO,fechaInicioAnio),
			EntityCondition.makeCondition("postedDate",EntityOperator.LESS_THAN_EQUAL_TO,UtilDateTime.getYearEnd(fechaInicioAnio, timeZone, locale)),
			EntityCondition.makeCondition(listaCondicionesId,EntityOperator.OR)
			);
	 	
	 	List<GenericValue> listCuentasCargaInicial = delegator.findByCondition("AcctgTransEntrySumTipoTrans", condiciones, select, orderBy);
	 	
	 	Map<String,GenericValue> mapaSaldoCargaInicial = FastMap.newInstance();
	 	
	 	if(UtilValidate.isNotEmpty(listCuentasCargaInicial)){
	 		
	 		for (String glAccountId : listCuentasId) {
				
			 	for (GenericValue acctgTransEntry : listCuentasCargaInicial) {
			 		
			 		String glAccountIdEntry = acctgTransEntry.getString("glAccountId");
			 		
			 		GenericValue GlAccountHistoryBusqueda = null;
			 		
			 		if(glAccountIdEntry.startsWith(glAccountId)){
			 			
			 			GlAccountHistoryBusqueda = mapaSaldoCargaInicial.get(glAccountId);
			 			
			 			if(UtilValidate.isNotEmpty(GlAccountHistoryBusqueda)){
				 			
				 			if(acctgTransEntry.getString("debitCreditFlag").equals("C")){
				 				GlAccountHistoryBusqueda.put("postedCredits",
				 						validaMonto(GlAccountHistoryBusqueda,"postedCredits").add(validaMonto(acctgTransEntry,"amount")));
				 			} else {
				 				GlAccountHistoryBusqueda.put("postedDebits",
				 						validaMonto(GlAccountHistoryBusqueda,"postedDebits").add(validaMonto(acctgTransEntry,"amount")));
				 			}
				 			
				 			mapaSaldoCargaInicial.put(glAccountId, GlAccountHistoryBusqueda);
				 			
				 		} else {
				 			//Se crea un GlAccountHistory temporal
				 			GenericValue GlAccountHistory = delegator.makeValue("GlAccountAndHistory");
				 			GlAccountHistory.put("glAccountId", glAccountId);
					 		GlAccountHistory.put("organizationPartyId", organizacionId);
					 		GlAccountHistory.put("glAccountClassId", acctgTransEntry.getString("glAccountClassId"));
				 			
				 			if(acctgTransEntry.getString("debitCreditFlag").equals("C")){
				 				GlAccountHistory.put("postedCredits", validaMonto(acctgTransEntry, "amount"));
				 			} else {
				 				GlAccountHistory.put("postedDebits", validaMonto(acctgTransEntry, "amount"));
				 			}
				 			
				 			mapaSaldoCargaInicial.put(glAccountId, GlAccountHistory);
				 			
				 		}
			 			
			 		}
			 		
				}
	 			
			}
	 		
	 	}
	 	
	 	return mapaSaldoCargaInicial;
	}
	
	/**
	* Crea el reporte Financiero XML para COMIMSA
	* @param delegator
	* @param documentBuilder
	* @param locale
	* @param timeZone
	* @param organizacion
	* @param mes
	* @param anio
	* @return
	* @throws GenericEntityException
	 * @throws SAXException 
	 * @throws IOException 
	*/

	private static Document creaFinancieroXML(Delegator delegator,DocumentBuilder documentBuilder ,Locale locale,
	TimeZone timeZone, GenericValue organizacion, String mes, String anio) throws GenericEntityException, SAXException, IOException {

		//Se obtiene el periodo a consultar 
		Timestamp fechaInicioMes = UtilDateTime.toTimestamp(Integer.valueOf(mes), 1, Integer.valueOf(anio), 0, 0, 0);
		Timestamp fechaFinMes = UtilDateTime.getTimestamp(fechaInicioMes.getTime());
		fechaFinMes = UtilDateTime.adjustTimestamp(fechaFinMes, Calendar.MONTH, 1);
		Timestamp inicioAnio = UtilDateTime.getYearStart(fechaInicioMes);
		Timestamp inicioAnioAnterior = UtilDateTime.adjustTimestamp(inicioAnio, Calendar.YEAR, -1);
		
		Timestamp fechaInicioAnioMenosUnAnio = UtilDateTime.adjustTimestamp(fechaInicioMes, Calendar.YEAR, -1);
		fechaInicioAnioMenosUnAnio = UtilDateTime.getYearStart(fechaInicioAnioMenosUnAnio);
		Timestamp fechaFinMesMenosUnAnio = UtilDateTime.adjustTimestamp(fechaFinMes, Calendar.YEAR, -1);
	
	    //Obtiene un mapa de las clases (naturaleza de la cuentas)
	    getClasses(delegator); 
	
		List<String> cuentaIds = FastList.newInstance();
		Map<String,String> mapaElemento = FastMap.newInstance();
		Map<String,String> mapaCuentas = FastMap.newInstance();
		List<GenericValue> listaElementos = delegator.findByAnd("ElementoReporte", UtilMisc.toMap("tipoReporteId","FINANCIERO"));
		for (GenericValue elementoRepo : listaElementos) {
			mapaElemento.put(elementoRepo.getString("elementoReporteId"), elementoRepo.getString("glAccountId"));
			mapaCuentas.put(elementoRepo.getString("glAccountId"), elementoRepo.getString("glAccountId"));
		}
		
		cuentaIds = UtilMisc.toListArray(mapaCuentas.keySet().toArray(new String[mapaCuentas.size()]));
		
	    //Se buscan las cuentas afectadas en el mes correspondiente
	    Map<String,GenericValue> mapaSaldoActual = FastMap.newInstance();
	    mapaSaldoActual = getSaldoMesActual(delegator, fechaInicioMes, fechaFinMes, organizacion,cuentaIds);
	    //Se realiza la suma de las cuentas hijas en el mes seleccionado
	    Map<String,GenericValue> mapaSaldoActualSuma = getMapaSaldo(cuentaIds, mapaSaldoActual, delegator);
	    
	    /**
	     * 'Saldo inicial'
	     */
	    Map<String,BigDecimal> mapaSaldoInicial = FastMap.newInstance();
	    mapaSaldoInicial = getSaldoInicial(delegator, fechaInicioMes, organizacion,cuentaIds);
	    
	    /**
	     * 'Carga Inicial'
	     */
	    Map<String,GenericValue> mapaSaldoCargaInicial = FastMap.newInstance();
	    mapaSaldoCargaInicial = getSaldoCargaInicial(delegator, cuentaIds, organizacion, inicioAnio, timeZone, locale);
	    if(mes.equals("01")){ //Se resta del saldo actual la carga inicial y se agrega al saldo inicial 
	    	restaCargaInicial(delegator, mapaSaldoActualSuma, mapaSaldoCargaInicial);
	    	agregaCargaInicialSaldoInicial(delegator,mapaSaldoInicial, mapaSaldoCargaInicial,cuentaIds);
	    }
	    
	    /**
	     * ========================================================================
	     * 'Estado de Variación en la Hacienda Pública'
	     */
	    //Se buscan las cuentas afectadas en el mes correspondiente
	    Map<String,GenericValue> mapaSaldoAcumuladoAlMes = FastMap.newInstance();
	    mapaSaldoAcumuladoAlMes = getSaldoMesActual(delegator, inicioAnio, fechaFinMes, organizacion,cuentaIds);
	    //Se realiza la suma de las cuentas hijas del acumulado al mes seleccionado
	    Map<String,GenericValue> mapaSaldoAcumuladoAlMesSuma = getMapaSaldo(cuentaIds, mapaSaldoAcumuladoAlMes, delegator);
	    ///////////////////////////////	Anio Anterior ////////////////////////////////
	    //Se buscan las cuentas afectadas en el mes correspondiente del anio anterior
	    Map<String,GenericValue> mapaSaldoAcumuladoAlMesAnioAnt = FastMap.newInstance();
	    mapaSaldoAcumuladoAlMesAnioAnt = getSaldoMesActual(delegator, inicioAnioAnterior, fechaFinMesMenosUnAnio, organizacion,cuentaIds);
	    //Se realiza la suma de las cuentas hijas del acumulado al mes seleccionado
	    Map<String,GenericValue> mapaSaldoAcumuladoAlMesSumaAnt = getMapaSaldo(cuentaIds, mapaSaldoAcumuladoAlMesAnioAnt, delegator);

	    //Se resta la 'carga inicial' del acumulado al mes seleccionado
//	    restaCargaInicial(delegator, mapaSaldoAcumuladoAlMesSuma, mapaSaldoCargaInicial);
	    //Se agrega la 'carga inicial' del acumulado al mes seleccionado del anio anterior
//	    agregaCargaInicialAnioAnterior(delegator,mapaSaldoAcumuladoAlMesSumaAnt, mapaSaldoCargaInicial,cuentaIds); (se comenta porque COLSAN quiere que no se tome en cuenta la caga inicial)
	    /**
	     * ========================================================================
	     */
	    
	    
	    //Se buscan las cuentas afectadas acumuladas del inicio del anio al mes seleccionado del anio anterior
	    Map<String,GenericValue> mapaSaldoAnioAnterior = FastMap.newInstance();
	    mapaSaldoAnioAnterior = getSaldoMesActual(delegator, fechaInicioAnioMenosUnAnio, fechaFinMesMenosUnAnio, organizacion,cuentaIds);
	    
	    //De anio anterior
	    Map<String,GenericValue> mapaSaldoAnioAnteriorSuma = getMapaSaldo(cuentaIds, mapaSaldoAnioAnterior, delegator);
	    
		// se crea el elemento principal
		Document document = documentBuilder.newDocument();
		Element InfFinanciera = document.createElement("cmis:InformacionFinanciera");
		InfFinanciera.setAttribute("anio", anio);
		//InfFinanciera.setAttribute("centro","COLSAN");
		InfFinanciera.setAttribute("centro","FINANCIERA");
		InfFinanciera.setAttribute("mes",mes);
		InfFinanciera.setAttribute("xmlns:cmis","http://www.sifascpi.gob.mx/sifascpi");
		InfFinanciera.setAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
		InfFinanciera.setAttribute("xsi:schemaLocation","http://www.sifascpi.gob.mx/sifascpi Financiero.xsd");
		
		XSSchema s = getXSSchemaReporteXML("/opentaps/financials/config/Financiero.xsd");
		for (String tipo : listaTiposFinanciero) {
			
			XSParticle[] particles = getElementosDelTipo(s, tipo);
			
			Element tipoCatalogo = document.createElement(tipo.substring(3));
		    for (XSParticle xsParticle : particles) {
		    	String nombreCampo = xsParticle.getTerm().asElementDecl().getName();
		    	String glAccountId = mapaElemento.get(nombreCampo);
		    	if(glAccountId == null){
		    		Element elemento = document.createElement(nombreCampo.trim());
		    		if(tipo.equals("CMICatalogoCuentas")){
		    			setAtributosCatCtas(elemento, "0.0", "0.0", "0.0");
		    		} else if(tipo.equals("CMICatalogoFlujosEfectivo")){
		    			setAtributosFlujo(elemento, "0.0");
		    		} else if(tipo.equals("CMICatalogoCuentasVariaciones")) {
		    			setAtributosVariaciones(elemento, "0.0", "0.0", "0.0", "0.0");
		    		}
		    		
		    		tipoCatalogo.appendChild(elemento);
		    		continue;
		    	}
		    	Element elemento = document.createElement(nombreCampo);
		    	GenericValue GlAccount = delegator.findByPrimaryKey("GlAccount",UtilMisc.toMap("glAccountId",glAccountId));
		    	GenericValue GlAccountHistory = mapaSaldoActualSuma.get(glAccountId);
		    	GenericValue GlAccountHistoryAcumuladoAlMes = mapaSaldoAcumuladoAlMesSuma.get(glAccountId);
		    	GenericValue GlAccountHistoryAcumuladoAlMesAnioAnterior = mapaSaldoAcumuladoAlMesSumaAnt.get(glAccountId);

	    		BigDecimal credits = BigDecimal.ZERO;
	    		BigDecimal debits = BigDecimal.ZERO;
	    		BigDecimal total = BigDecimal.ZERO;
	    		
	    		BigDecimal creditsAlMes = BigDecimal.ZERO;
	    		BigDecimal debitsAlMes = BigDecimal.ZERO;
	    		BigDecimal totalAlMes = BigDecimal.ZERO;
	    		
	    		BigDecimal creditsAlMesAnt = BigDecimal.ZERO;
	    		BigDecimal debitsAlMesAnt = BigDecimal.ZERO;
	    		BigDecimal totalAlMesAnioAnt = BigDecimal.ZERO;
	    		
	    		String glAccountClassId = GlAccount.getString("glAccountClassId");
	    		
		    	if(GlAccountHistory != null){
		    		credits = validaMonto(GlAccountHistory, "postedCredits");
		    		debits = validaMonto(GlAccountHistory, "postedDebits");
		    		total = sumaNaturaleza(glAccountClassId, credits, debits);
		    	}
		    	
		    	if(GlAccountHistoryAcumuladoAlMes != null){
		    		creditsAlMes = validaMonto(GlAccountHistoryAcumuladoAlMes, "postedCredits");
		    		debitsAlMes = validaMonto(GlAccountHistoryAcumuladoAlMes, "postedDebits");
		    		totalAlMes = sumaNaturaleza(glAccountClassId, creditsAlMes, debitsAlMes);
		    	}
		    	
		    	if(GlAccountHistoryAcumuladoAlMesAnioAnterior != null){
		    		creditsAlMesAnt = validaMonto(GlAccountHistoryAcumuladoAlMesAnioAnterior, "postedCredits");
		    		debitsAlMesAnt = validaMonto(GlAccountHistoryAcumuladoAlMesAnioAnterior, "postedDebits");
		    		totalAlMesAnioAnt = sumaNaturaleza(glAccountClassId, creditsAlMesAnt, debitsAlMesAnt);
		    	}
		    	
		    	if(tipo.equals("CMICatalogoCuentas")){
		    		
		    		BigDecimal saldoInicial = BigDecimal.ZERO;
		    		saldoInicial = mapaSaldoInicial.get(glAccountId) == null ? saldoInicial : mapaSaldoInicial.get(glAccountId);
		    		setAtributosCatCtas(elemento, df.format(credits), df.format(debits), df.format(saldoInicial));
		    		
		    	} else if(tipo.equals("CMICatalogoFlujosEfectivo")){
		    		
		    		setAtributosFlujo(elemento, total.subtract(totalAlMesAnioAnt).toString());
		    		
		    	} else if(tipo.equals("CMICatalogoCuentasVariaciones")) {
		    		
		    		if(nombreCampo.startsWith("v1004") || nombreCampo.startsWith("v1005")){
		    			setAtributosVariaciones(elemento, df.format(totalAlMesAnioAnt), "0.0", "0.0", "0.0");
		    			reemplazaElementov1001SumaValor(document,tipoCatalogo, totalAlMesAnioAnt, 1);
		    		}
		    		if(nombreCampo.startsWith("v1007")){
		    			setAtributosVariaciones(elemento, "0.0", "0.0", df.format(totalAlMesAnioAnt), "0.0");
		    			reemplazaElementov1001SumaValor(document,tipoCatalogo, totalAlMesAnioAnt, 1);
		    		}
		    		if(nombreCampo.startsWith("v1008")){
		    			setAtributosVariaciones(elemento, "0.0", df.format(totalAlMesAnioAnt),"0.0", "0.0");
		    			reemplazaElementov1001SumaValor(document,tipoCatalogo, totalAlMesAnioAnt, 2);
		    		}
		    		if(nombreCampo.startsWith("v1009") || nombreCampo.startsWith("v1010")){
		    			setAtributosVariaciones(elemento, "0.0", "0.0", "0.0", df.format(totalAlMesAnioAnt));
		    			reemplazaElementov1001SumaValor(document,tipoCatalogo, totalAlMesAnioAnt, 4);
		    		}
		    		if(nombreCampo.startsWith("v1016") || nombreCampo.startsWith("v1017")){
		    			setAtributosVariaciones(elemento,df.format(totalAlMes), "0.0","0.0", "0.0");
		    			reemplazaElementov1001SumaValor(document,tipoCatalogo, totalAlMes, 1);
		    		}
		    		if(nombreCampo.startsWith("v1019")){
		    			setAtributosVariaciones(elemento, "0.0", "0.0", df.format(totalAlMes), "0.0");
		    			reemplazaElementov1001SumaValor(document,tipoCatalogo, totalAlMes, 3);
		    		}
		    		if(nombreCampo.startsWith("v1020")){
		    			setAtributosVariaciones(elemento, "0.0", df.format(totalAlMes),"0.0", "0.0");
		    			reemplazaElementov1001SumaValor(document,tipoCatalogo, totalAlMes, 2);
		    		}
		    		if(nombreCampo.startsWith("v1021") || nombreCampo.startsWith("v1022")){
		    			setAtributosVariaciones(elemento, "0.0","0.0", "0.0",df.format(totalAlMes));
		    			reemplazaElementov1001SumaValor(document,tipoCatalogo, totalAlMes, 4);
		    		}
		    		
		    	}
		    	
		    	tipoCatalogo.appendChild(elemento);
			}
		    
		    InfFinanciera.appendChild(tipoCatalogo);
		}
		
		document.appendChild(InfFinanciera);
		
		generaFlujoEfectivo(delegator, document, organizacion, fechaInicioMes, fechaFinMes,anio);
	
		return document;

	}
	
	/**
	 * Realiza el reporte de Flujo de Efectivo 
	 * @param delegator
	 * @param document
	 * @param organizacionId
	 * @param fechaInicio
	 * @param fechaFin
	 * @param anio 
	 */
	
	private static void generaFlujoEfectivo(Delegator delegator, Document document, GenericValue organizacion,Timestamp fechaInicio, Timestamp fechaFin, String anio) throws GenericEntityException{
		
		/*
		 * Anio actual
		 */
		Timestamp fechaInicioAnioAnterior = UtilDateTime.adjustTimestamp(fechaInicio, Calendar.YEAR, -1);
		Timestamp fechaFinAnioAnterior = UtilDateTime.adjustTimestamp(fechaFin, Calendar.YEAR, -1);
		
		Map<String,Object> mapSaldosCRI = FastMap.newInstance();
		Map<String,Object> mapSaldosCOG = FastMap.newInstance();
		Map<String,GenericValue> mapSaldosCuentas = FastMap.newInstance();
		Map<String,BigDecimal> mapaSaldoInicialFinal = FastMap.newInstance();
		
		Map<String,Map<String,Object>> mapaElementoMontoCRI = getMapaSaldoClasificacion(delegator, mapSaldosCRI, UtilClavePresupuestal.INGRESO_TAG, organizacion, fechaInicio, fechaFin,anio);
		Map<String,Map<String,Object>> mapaElementoMontoCOG = getMapaSaldoClasificacion(delegator, mapSaldosCOG, UtilClavePresupuestal.EGRESO_TAG, organizacion, fechaInicio, fechaFin,anio);
		getMapaSaldoCuentas(delegator, mapSaldosCuentas, organizacion, fechaInicio, fechaFin);
		getMapaSaldoInicialFinal(delegator, mapaSaldoInicialFinal, organizacion, fechaInicio, fechaFin);
		
//		Debug.log("mapSaldosCRI  --> "+mapaElementoMontoCRI);
//		Debug.log("mapSaldosCOG  --> "+mapaElementoMontoCOG);
//		Debug.log("mapSaldosCuentas  --> "+mapSaldosCuentas);
//		Debug.log("mapaSaldoInicialFinal  --> "+mapaSaldoInicialFinal);
		
		/*
		 * Anio anterior
		 */	
		
		Map<String,Object> mapSaldosCRIAnt = FastMap.newInstance();
		Map<String,Object> mapSaldosCOGAnt = FastMap.newInstance();
		Map<String,GenericValue> mapSaldosCuentasAnt = FastMap.newInstance();
		Map<String,BigDecimal> mapaSaldoInicialFinalAnt = FastMap.newInstance();
		
		Map<String,Map<String,Object>> mapaElementoMontoCRIAnt = getMapaSaldoClasificacion(delegator, mapSaldosCRIAnt, UtilClavePresupuestal.INGRESO_TAG, organizacion, fechaInicioAnioAnterior, fechaFinAnioAnterior,anio);
		Map<String,Map<String,Object>> mapaElementoMontoCOGAnt = getMapaSaldoClasificacion(delegator, mapSaldosCOGAnt, UtilClavePresupuestal.EGRESO_TAG, organizacion, fechaInicioAnioAnterior, fechaFinAnioAnterior,anio);
		getMapaSaldoCuentas(delegator, mapSaldosCuentasAnt, organizacion, fechaInicioAnioAnterior, fechaFinAnioAnterior);
		getMapaSaldoInicialFinal(delegator, mapaSaldoInicialFinalAnt, organizacion, fechaInicioAnioAnterior, fechaFinAnioAnterior);
		
//		Debug.log("mapSaldosCRIAnt  --> "+mapSaldosCRIAnt);
//		Debug.log("mapSaldosCOGAnt  --> "+mapSaldosCOGAnt);
//		Debug.log("mapSaldosCuentasAnt  --> "+mapSaldosCuentasAnt);
//		Debug.log("mapaSaldoInicialFinalAnt  --> "+mapaSaldoInicialFinalAnt);
		
		
		BigDecimal total = BigDecimal.ZERO;
		BigDecimal suma = BigDecimal.ZERO;
		BigDecimal sumaAnt = BigDecimal.ZERO;
		for(Entry<String,Map<String,Object>>  elementoMontoCRI : mapaElementoMontoCRI.entrySet()){
			Map<String,Object> mapSaldos =  elementoMontoCRI.getValue();
			Map<String,Object> mapSaldosAnt =  mapaElementoMontoCRIAnt.get(elementoMontoCRI.getKey());
			
			Object objetoXElemento = mapaElementosCRIFlujoEfectivo.get(elementoMontoCRI.getKey());
			
			if(objetoXElemento instanceof String){
				total = UtilNumber.getBigDecimal((BigDecimal) mapSaldos.get(objetoXElemento)).subtract(
						UtilNumber.getBigDecimal((BigDecimal) mapSaldosAnt.get(objetoXElemento)));
			} else {
				if(elementoMontoCRI.getKey().equals("OrigenTransferenciasYDonativos")){
					suma = UtilNumber.getBigDecimal((BigDecimal) mapSaldos.get("9")).subtract(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldos.get("912")).add(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldos.get("922"))).add(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldos.get("962"))).add(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldos.get("982"))));
					sumaAnt = UtilNumber.getBigDecimal((BigDecimal) mapSaldosAnt.get("9")).subtract(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldosAnt.get("912")).add(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldosAnt.get("922"))).add(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldosAnt.get("962"))).add(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldosAnt.get("982"))));
				}
				if(elementoMontoCRI.getKey().equals("OrigenContribucionesCapital")){
					suma = UtilNumber.getBigDecimal((BigDecimal) mapSaldos.get("311")).add(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldos.get("62")).subtract(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldos.get("629"))));
					sumaAnt = UtilNumber.getBigDecimal((BigDecimal) mapSaldosAnt.get("311")).add(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldosAnt.get("62")).subtract(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldosAnt.get("629"))));
				}
				if(elementoMontoCRI.getKey().equals("OrigenVentasActivosFisicos")){
					suma = UtilNumber.getBigDecimal((BigDecimal) mapSaldos.get("52")).subtract(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldos.get("5215")).add(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldos.get("5219"))).add(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldos.get("527"))));
					sumaAnt = UtilNumber.getBigDecimal((BigDecimal) mapSaldosAnt.get("52")).subtract(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldosAnt.get("5215")).add(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldosAnt.get("5219"))).add(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldosAnt.get("527"))));
				}
				if(elementoMontoCRI.getKey().equals("OrigenVentaDisposicionObjetosDeValor")){
					suma = UtilNumber.getBigDecimal((BigDecimal) mapSaldos.get("5215")).add(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldos.get("5219")));
					sumaAnt = UtilNumber.getBigDecimal((BigDecimal) mapSaldosAnt.get("5215")).add(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldosAnt.get("5219")));
				}
				if(elementoMontoCRI.getKey().equals("OrigenTransferenciasDeCapitalRecibidas")){
					suma = UtilNumber.getBigDecimal((BigDecimal) mapSaldos.get("912")).add(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldos.get("922"))).add(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldos.get("962"))).add(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldos.get("982")));
					sumaAnt = UtilNumber.getBigDecimal((BigDecimal) mapSaldosAnt.get("912")).add(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldosAnt.get("922"))).add(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldosAnt.get("962"))).add(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldosAnt.get("982")));
				}
				
				total = suma.subtract(sumaAnt);
				
			}
			
			reemplazaElementoFlujoEfectivo(document, elementoMontoCRI.getKey(), total);

		}
		
		
		for(Entry<String,Map<String,Object>>  elementoMontoCOG : mapaElementoMontoCOG.entrySet()){
			Map<String,Object> mapSaldos =  elementoMontoCOG.getValue();
			Map<String,Object> mapSaldosAnt =  mapaElementoMontoCOGAnt.get(elementoMontoCOG.getKey());
			
			Object objetoXElemento = mapaElementosCOGFlujoEfectivo.get(elementoMontoCOG.getKey());
			
			if(objetoXElemento instanceof String){
				total = UtilNumber.getBigDecimal((BigDecimal) mapSaldos.get(objetoXElemento)).subtract(
						UtilNumber.getBigDecimal((BigDecimal) mapSaldosAnt.get(objetoXElemento)));
			} else {
				if(elementoMontoCOG.getKey().equals("AplicacionObjetosDeValor")){// 5000 + (5130 - 5140) +5700
					suma = UtilNumber.getBigDecimal((BigDecimal) mapSaldos.get("5")).subtract(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldos.get("513")).add(UtilNumber.getBigDecimal((BigDecimal) mapSaldos.get("514"))))
							.add(UtilNumber.getBigDecimal((BigDecimal) mapSaldos.get("57")));
					sumaAnt = UtilNumber.getBigDecimal((BigDecimal) mapSaldosAnt.get("5")).subtract(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldosAnt.get("513")).add(UtilNumber.getBigDecimal((BigDecimal) mapSaldosAnt.get("514"))))
							.add(UtilNumber.getBigDecimal((BigDecimal) mapSaldosAnt.get("57")));
				}
				
				if(elementoMontoCOG.getKey().equals("AplicacionActivosNoProducidos")){// (5130 + 5140)
					suma = UtilNumber.getBigDecimal((BigDecimal) mapSaldos.get("513")).add(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldos.get("514")));
					sumaAnt = UtilNumber.getBigDecimal((BigDecimal) mapSaldosAnt.get("513")).add(
							UtilNumber.getBigDecimal((BigDecimal) mapSaldosAnt.get("514")));
				}
			}
			
			reemplazaElementoFlujoEfectivo(document, elementoMontoCOG.getKey(), total);
		}
		
		GenericValue GlAccountHistory112 =  mapSaldosCuentas.get("1.1.2");
		GenericValue GlAccountHistory113 =  mapSaldosCuentas.get("1.1.3");
		GenericValue GlAccountHistory219 =  mapSaldosCuentas.get("2.1.9");
		
		BigDecimal saldo112 = BigDecimal.ZERO;
		BigDecimal saldo113 = BigDecimal.ZERO;
		
		BigDecimal saldo112Ant = BigDecimal.ZERO;
		BigDecimal saldo113Ant = BigDecimal.ZERO;
		
		if(UtilValidate.isNotEmpty(GlAccountHistory112)){
			String glAccountClassId112 = GlAccountHistory112.getRelatedOne("GlAccount").getString("glAccountClassId");
			saldo112 = sumaNaturaleza(glAccountClassId112, validaMonto(GlAccountHistory112, "postedCredits"), 
					validaMonto(GlAccountHistory112, "postedDebits"));
		}

		if(UtilValidate.isNotEmpty(GlAccountHistory113)){
			String glAccountClassId113 = GlAccountHistory113.getRelatedOne("GlAccount").getString("glAccountClassId");
			saldo113 = sumaNaturaleza(glAccountClassId113, validaMonto(GlAccountHistory113, "postedCredits"), 
					validaMonto(GlAccountHistory113, "postedDebits"));
		}
		
		BigDecimal saldoAcreedor112113 = BigDecimal.ZERO;
		BigDecimal saldoDeudor112113 = BigDecimal.ZERO;
		
		if(saldo112.compareTo(BigDecimal.ZERO) >= 0){
			saldoAcreedor112113 = saldoAcreedor112113.add(saldo112);
		}
		if(saldo113.compareTo(BigDecimal.ZERO) >= 0){
			saldoAcreedor112113 = saldoAcreedor112113.add(saldo113);
		}
		if(saldo112.compareTo(BigDecimal.ZERO) < 0){
			saldoDeudor112113 = saldoAcreedor112113.add(saldo112);
		}
		if(saldo113.compareTo(BigDecimal.ZERO) < 0){
			saldoAcreedor112113 = saldoAcreedor112113.add(saldo113);
		}
		
		
		GenericValue GlAccountHistory112Ant =  mapSaldosCuentasAnt.get("1.1.2");
		GenericValue GlAccountHistory113Ant =  mapSaldosCuentasAnt.get("1.1.3");
		GenericValue GlAccountHistory219Ant =  mapSaldosCuentasAnt.get("2.1.9");
		
		if(UtilValidate.isNotEmpty(GlAccountHistory112Ant)){
			String glAccountClassId112 = GlAccountHistory112Ant.getRelatedOne("GlAccount").getString("glAccountClassId");
			saldo112Ant = sumaNaturaleza(glAccountClassId112, validaMonto(GlAccountHistory112Ant, "postedCredits"), 
					validaMonto(GlAccountHistory112Ant, "postedDebits"));
		}

		if(UtilValidate.isNotEmpty(GlAccountHistory113Ant)){
			String glAccountClassId113 = GlAccountHistory113Ant.getRelatedOne("GlAccount").getString("glAccountClassId");
			saldo113Ant = sumaNaturaleza(glAccountClassId113, validaMonto(GlAccountHistory113Ant, "postedCredits"), 
					validaMonto(GlAccountHistory113Ant, "postedDebits"));
		}		
		
		BigDecimal saldoAcreedor112113Ant = BigDecimal.ZERO;
		BigDecimal saldoDeudor112113Ant = BigDecimal.ZERO;
		
		if(saldo112Ant.compareTo(BigDecimal.ZERO) >= 0){
			saldoAcreedor112113Ant = saldoAcreedor112113Ant.add(saldo112Ant);
		}
		if(saldo113Ant.compareTo(BigDecimal.ZERO) >= 0){
			saldoAcreedor112113Ant = saldoAcreedor112113Ant.add(saldo113Ant);
		}
		if(saldo112Ant.compareTo(BigDecimal.ZERO) < 0){
			saldoDeudor112113Ant = saldoAcreedor112113Ant.add(saldo112Ant);
		}
		if(saldo113Ant.compareTo(BigDecimal.ZERO) < 0){
			saldoAcreedor112113Ant = saldoAcreedor112113Ant.add(saldo113Ant);
		}
		
		reemplazaElementoFlujoEfectivo(document, "OrigenDisminucionActivosFinancierosExcluidoEfectivoEquivalente", saldoAcreedor112113.subtract(saldoAcreedor112113Ant));
		reemplazaElementoFlujoEfectivo(document, "OrigenIncrementoOtrosPasivos", validaMonto(GlAccountHistory219, "postedCredits").subtract(validaMonto(GlAccountHistory219Ant, "postedCredits")));
		reemplazaElementoFlujoEfectivo(document, "AplicacionIncrementoActivosFinancierosExcluidoEfectivoEquivalente", saldoDeudor112113.subtract(saldoDeudor112113Ant));
		reemplazaElementoFlujoEfectivo(document, "AplicacionDisminucionOtrosPasivos", validaMonto(GlAccountHistory219, "postedDebits").subtract(validaMonto(GlAccountHistory219Ant, "postedDebits")));
		
		reemplazaElementoFlujoEfectivo(document, "EfectivoYEquivalentesAlEfectivoAlInicioDelEjercicio", mapaSaldoInicialFinal.get("SaldoInicial").subtract(mapaSaldoInicialFinalAnt.get("SaldoInicial")));
		reemplazaElementoFlujoEfectivo(document, "EfectivoYEquivalentesAlEfectivoAlFinalDelEjercicio", mapaSaldoInicialFinal.get("SaldoFinal").subtract(mapaSaldoInicialFinalAnt.get("SaldoFinal")));
		
	}
	
	/**
	 * Reemplaza el elemento (nombre) con el nuevo valor (total)
	 * @param document
	 * @param nombre
	 * @param total
	 */
	private static void reemplazaElementoFlujoEfectivo(Document document, String nombre, BigDecimal total){
		
		NodeList nodes = document.getElementsByTagName(nombre);
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			Debug.log("Atributos ---> "+node.getAttributes().getNamedItem("Total"));
			Element nuevo = document.createElement(nombre);
			setAtributosFlujo(nuevo, df.format(total));
			node.getParentNode().replaceChild(nuevo, node);
		}
		
	}
	
	/**
	 * Regresa un mapa con saldo de las cuentas Flujo de efectivo de las actividades de Financiamiento
	 * @param delegator
	 * @param mapSaldosCuentas
	 * @param organizacionId
	 * @param fechaInicio
	 * @param fechaFin
	 */
	public static void getMapaSaldoCuentas(Delegator delegator, Map<String,GenericValue> mapSaldosCuentas,
					GenericValue Organizacion,Timestamp fechaInicio, Timestamp fechaFin)  throws GenericEntityException{
		
		Map<String, GenericValue> mapCuentasMes = getSaldoMesActual(delegator, fechaInicio, fechaFin, Organizacion, listActividadesDeFinanciamientoFlujoEfectivo);
		mapSaldosCuentas.putAll(getMapaSaldo(listActividadesDeFinanciamientoFlujoEfectivo, mapCuentasMes, delegator));
		
	}
	
	/**
	 * Regresa un mapa con el saldo inicial y final de las cuentas Efectivo y equivalentes al efectivo
	 * @param delegator
	 * @param mapSaldosCuentas
	 * @param organizacionId
	 * @param fechaInicio
	 * @param fechaFin
	 * @return
	 */
	public static void getMapaSaldoInicialFinal(Delegator delegator, Map<String,BigDecimal> mapaSaldoInicialFinal,
			GenericValue Organizacion,Timestamp fechaInicio, Timestamp fechaFin)  throws GenericEntityException{
		
		BigDecimal saldoInicial = BigDecimal.ZERO;
		Map<String,BigDecimal> mapSaldoInicial = getSaldoInicial(delegator, fechaInicio, Organizacion, UtilMisc.toList(cuentaEfectivoYEquivalentesFlujoEfectivo));
		if(UtilValidate.isNotEmpty(mapSaldoInicial)){
			saldoInicial = UtilNumber.getBigDecimal(mapSaldoInicial.get(0));
		}
		
		
		Map<String, GenericValue> mapCuentasMes = getSaldoMesActual(delegator, fechaInicio, fechaFin, Organizacion, UtilMisc.toList(cuentaEfectivoYEquivalentesFlujoEfectivo));
		Map<String,GenericValue> mapaSaldo = getMapaSaldo(UtilMisc.toList(cuentaEfectivoYEquivalentesFlujoEfectivo), mapCuentasMes, delegator);
		
		BigDecimal saldo = BigDecimal.ZERO;
		for (Entry<String, GenericValue> CuentaMesEntry : mapaSaldo.entrySet()) {
			GenericValue GlAccountHistory = mapaSaldo.get(CuentaMesEntry.getKey());
			saldo = saldo.add(sumaNaturaleza(GlAccountHistory.getRelatedOne("GlAccount").getString("glAccountClassId"), 
					GlAccountHistory.getBigDecimal("postedCredits"), GlAccountHistory.getBigDecimal("postedDebits")));
		}
		
		mapaSaldoInicialFinal.put("SaldoInicial", saldoInicial);
		mapaSaldoInicialFinal.put("SaldoFinal", saldoInicial.add(saldo));
	}
	
	
	/**
	 * Obtiene el saldo de cada clasificacion ingresada 
	 * @param delegator
	 * @param mapSaldos
	 * @param tipoClave
	 * @param organizacionId
	 * @param fechaInicio
	 * @param fechaFin
	 * @param anio 
	 */
	public static Map<String,Map<String,Object>> getMapaSaldoClasificacion(Delegator delegator,Map<String,Object> mapSaldos, 
			String tipoClave, GenericValue Organizacion,Timestamp fechaInicio, Timestamp fechaFin, String anio) throws GenericEntityException{
		
		Map<String,Map<String,Object>> mapaResultados = FastMap.newInstance();
		Map<String,Object> mapaMontos = FastMap.newInstance();
		
		String tipoClasificacion = new String();
		String bandera = new String();
		Map<String, Object> mapaItera = FastMap.newInstance();
		if(tipoClave.equals(UtilClavePresupuestal.EGRESO_TAG)){
			tipoClasificacion = "CL_COG";
			mapaItera = mapaElementosCOGFlujoEfectivo;
			bandera = "D";
		} else {
			tipoClasificacion = "CL_CRI";
			mapaItera = mapaElementosCRIFlujoEfectivo;
			bandera = "C";
		}
		
		int indiceCRICOG = UtilClavePresupuestal.obtenerIndiceClasificacion(tipoClave, Organizacion.getString("partyId"), tipoClasificacion, delegator,anio);
		EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("postedDate",EntityOperator.GREATER_THAN_EQUAL_TO,fechaInicio),
				EntityCondition.makeCondition("postedDate",EntityOperator.LESS_THAN_EQUAL_TO,fechaFin),
				EntityCondition.makeCondition("debitCreditFlag",EntityOperator.EQUALS,bandera),
				EntityCondition.makeCondition("tipo",EntityOperator.EQUALS,tipoClave));
		
		List<String> select = UtilMisc.toList("amount");
		for(Entry<String, Object> ElementoClas : mapaItera.entrySet()){
			String nombreElementoXML = ElementoClas.getKey();
			if(ElementoClas.getValue() instanceof String){
				EntityCondition condicionClas = EntityCondition.makeCondition(EntityOperator.AND,
						EntityCondition.makeCondition(("clasificacion"+indiceCRICOG),EntityOperator.LIKE,ElementoClas.getValue()+"%"),
						condiciones);
				List<GenericValue> listCRIElemento = delegator.findByCondition("SumaPartidasClasificacionPresup", condicionClas, select, null);
				if(UtilValidate.isNotEmpty(listCRIElemento)){
					UtilMisc.addToBigDecimalInMap(mapaMontos, ElementoClas.getValue().toString(), listCRIElemento.get(0).getBigDecimal("amount"));
				}
			} else if (ElementoClas.getValue() instanceof List){
				List<String> listCRI = (List<String>) ElementoClas.getValue();
				//Se buscan las cuentas 
				for (String clasifica : listCRI) {
					EntityCondition condicionClas = EntityCondition.makeCondition(condiciones,
							EntityCondition.makeCondition(("clasificacion"+indiceCRICOG),EntityOperator.LIKE,clasifica+"%"));
					List<GenericValue> listCRIElemento = delegator.findByCondition("SumaPartidasClasificacionPresup", condicionClas, select, null);
					if(UtilValidate.isNotEmpty(listCRIElemento)){
						UtilMisc.addToBigDecimalInMap(mapaMontos, clasifica, listCRIElemento.get(0).getBigDecimal("amount"));
					}
				}
			}
			if(mapaResultados.containsKey(nombreElementoXML)){
				Map<String,Object> mapaMontosTemp = mapaResultados.get(nombreElementoXML);
				mapaMontosTemp.putAll(mapaMontos);
				mapaResultados.put(nombreElementoXML, mapaMontosTemp);
			} else {
				mapaResultados.put(nombreElementoXML, mapaMontos);
			}
			mapaMontos = FastMap.newInstance();
		}
		
		return mapaResultados;
	}
	
	/**
	 * Reemplaza un elemenento de variacion en la hacienda 
	 * @param document
	 * @param nombreNodo
	 * @param nombreAtributo
	 * @param valorNuevo
	 * @param posicion
	 */
	private static void reemplazaElementov1001SumaValor(Document document, Element element,BigDecimal valorNuevo,int posicion){
		
		String nombreNodo = "v1001-PatrimonioAlFinalDelEjercicio";
		String nombreAtributo = listaAtributosVariacion.get(posicion-1);
		NodeList nodes = element.getElementsByTagName(nombreNodo);
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			String valorAnterior = node.getAttributes().getNamedItem(nombreAtributo).getNodeValue();
			BigDecimal montoAnterior = BigDecimal.valueOf(Double.valueOf(valorAnterior));
			String suma = df.format(montoAnterior.add(valorNuevo));
			
			//Valores anteriores (no deben perderse)
			String valor1 = new String();
			String valor2 = new String();
			String valor3 = new String();
			
			Element nuevo = document.createElement(nombreNodo);
			switch (posicion) {
				case 1:
					valor1 = node.getAttributes().getNamedItem(listaAtributosVariacion.get(1)).getNodeValue();
					valor2 = node.getAttributes().getNamedItem(listaAtributosVariacion.get(2)).getNodeValue();
					valor3 = node.getAttributes().getNamedItem(listaAtributosVariacion.get(3)).getNodeValue();
					setAtributosVariaciones(nuevo, suma, valor1,valor2,valor3);
					break;
				case 2:
					valor1 = node.getAttributes().getNamedItem(listaAtributosVariacion.get(0)).getNodeValue();
					valor2 = node.getAttributes().getNamedItem(listaAtributosVariacion.get(2)).getNodeValue();
					valor3 = node.getAttributes().getNamedItem(listaAtributosVariacion.get(3)).getNodeValue();
					setAtributosVariaciones(nuevo, valor1, suma,valor2, valor3);
					break;
				case 3:
					valor1 = node.getAttributes().getNamedItem(listaAtributosVariacion.get(0)).getNodeValue();
					valor2 = node.getAttributes().getNamedItem(listaAtributosVariacion.get(1)).getNodeValue();
					valor3 = node.getAttributes().getNamedItem(listaAtributosVariacion.get(3)).getNodeValue();
					setAtributosVariaciones(nuevo, valor1, valor2, suma , valor3);
					break;
				case 4:
					valor1 = node.getAttributes().getNamedItem(listaAtributosVariacion.get(0)).getNodeValue();
					valor2 = node.getAttributes().getNamedItem(listaAtributosVariacion.get(1)).getNodeValue();
					valor3 = node.getAttributes().getNamedItem(listaAtributosVariacion.get(2)).getNodeValue();
					setAtributosVariaciones(nuevo, valor1, valor2, valor3, suma);
					break;
				default:
					break;
			}
			node.getParentNode().replaceChild(nuevo, node);
		}
	}
	
	/**
	 * Metodo para restar la carga inicial del saldo del mes actual
	 * @param delegator
	 * @param mapaSaldoActualSuma
	 * @param mapaSaldoCargaInicial
	 */
	private static void restaCargaInicial(Delegator delegator,
			Map<String, GenericValue> mapaSaldoActualSuma,
			Map<String, GenericValue> mapaSaldoCargaInicial) {
		
		for (Map.Entry<String, GenericValue> saldoActual : mapaSaldoActualSuma.entrySet()){
			String glAccountId = saldoActual.getKey();
			for (Map.Entry<String, GenericValue> saldoInicial : mapaSaldoCargaInicial.entrySet()){
				String glAccountIdSI = saldoInicial.getKey();
				if(glAccountIdSI.equals(glAccountId)){
					GenericValue GlAccountHistory = saldoActual.getValue();
					GlAccountHistory.put("postedDebits", validaMonto(saldoActual.getValue(), "postedDebits").subtract(validaMonto(saldoInicial.getValue(),"postedDebits")));
					GlAccountHistory.put("postedCredits",validaMonto(saldoActual.getValue(), "postedCredits").subtract(validaMonto(saldoInicial.getValue(),"postedCredits")));
					continue;
				}
			}
		}
			
		
	}

	/**
	 * Se agrega el saldo incial de la carga inicial
	 * @param delegator
	 * @param mapaSaldoInicialSuma
	 * @param mapaSaldoCargaInicial
	 * @param cuentaIds
	 */
	private static void agregaCargaInicialSaldoInicial(Delegator delegator,
			Map<String, BigDecimal> mapaSaldoInicialSuma, Map<String, GenericValue> mapaSaldoCargaInicial, List<String> cuentaIds) {
		
		for (String glAccountId : cuentaIds) {
			
			BigDecimal montoFinal = BigDecimal.ZERO;
			
			for (Map.Entry<String, GenericValue> saldoCargaInicial : mapaSaldoCargaInicial.entrySet()){
				String glAccountIdCarga = saldoCargaInicial.getValue().getString("glAccountId");
				if(glAccountIdCarga.equals(glAccountId)){
					montoFinal = sumaNaturaleza(saldoCargaInicial.getValue().getString("glAccountClassId"), 
							validaMonto(saldoCargaInicial.getValue(), "postedCredits"), 
							validaMonto(saldoCargaInicial.getValue(), "postedDebits"));
					continue;
				}

			}
			
			if(mapaSaldoInicialSuma.containsKey(glAccountId)){
				montoFinal = mapaSaldoInicialSuma.get(glAccountId).add(montoFinal);
			} 
			
			mapaSaldoInicialSuma.put(glAccountId, montoFinal);
		}
		
	}
	
	/**
	 * Agrega la 'Carga inicial' al anio anterior
	 * @param delegator 
	 * @param mapaSaldoAcumuladoAlMesAnioAnt
	 * @param mapaSaldoCargaInicial
	 * @param cuentaIds
	 */
	private static void agregaCargaInicialAnioAnterior(Delegator delegator,
			Map<String, GenericValue> mapaSaldoAcumuladoAlMesAnioAnt,
			Map<String, GenericValue> mapaSaldoCargaInicial,
			List<String> cuentaIds) {
		
		for (String glAccountId : cuentaIds) {
			
			
			GenericValue GlAccountHistoryCargaIni = mapaSaldoCargaInicial.get(glAccountId);
			GenericValue GlAccountHistory = null;
			
			if(mapaSaldoAcumuladoAlMesAnioAnt.containsKey(glAccountId)){
				GlAccountHistory = mapaSaldoAcumuladoAlMesAnioAnt.get(glAccountId);
				GlAccountHistory.put("postedCredits", validaMonto(GlAccountHistory,"postedCredits").
															add(validaMonto(GlAccountHistoryCargaIni,"postedCredits")));
				GlAccountHistory.put("postedDebits", validaMonto(GlAccountHistory,"postedDebits").
															add(validaMonto(GlAccountHistoryCargaIni,"postedDebits")));
			} else {
				GlAccountHistory = delegator.makeValue("GlAccountHistory");
				GlAccountHistory.put("glAccountId",glAccountId);
				GlAccountHistory.put("postedCredits",validaMonto(GlAccountHistoryCargaIni,"postedCredits"));
				GlAccountHistory.put("postedDebits",validaMonto(GlAccountHistoryCargaIni,"postedDebits"));
			}
			
			mapaSaldoAcumuladoAlMesAnioAnt.put(glAccountId, GlAccountHistory);
		}
		
	}

	/**
	 * Obtiene el campo Bigdecimal de un GenericValue  regresa cero o el valor, no nulo
	 * @param GlAccountHistory
	 * @param campo
	 * @return
	 */
	private static BigDecimal validaMonto(GenericValue GenericValue,String campo){
		if(UtilValidate.isEmpty(GenericValue)){
			return BigDecimal.ZERO;
		}
		return GenericValue.getBigDecimal(campo) == null ? BigDecimal.ZERO : GenericValue.getBigDecimal(campo);
	}
	
	/**
	 * Regresa el total de una cuenta dependiendo de su naturaleza
	 * @param glAccountClassId
	 * @param credits
	 * @param debits
	 * @return
	 */
	private static BigDecimal sumaNaturaleza(String glAccountClassId,BigDecimal credits, BigDecimal debits){
		
		if(getNaturaleza(glAccountClassId).equals("CREDIT")){//ACREEDORA
			return credits.subtract(debits);
		} else if (getNaturaleza(glAccountClassId).equals("DEBIT")){//DEUDORA
			return debits.subtract(credits);
		}
		
		return BigDecimal.ZERO;
	}
	
	/**
	 * Ingresa elementos al tipo CMICatalogoCuentas
	 * @param elemento
	 * @param abono
	 * @param cargo
	 * @param saldoInicial
	 */
	private static void setAtributosCatCtas(Element elemento, String abono, String cargo, String saldoInicial){
		elemento.setAttribute("abonos",abono);
		elemento.setAttribute("cargos",cargo);
		elemento.setAttribute("saldoInicial",saldoInicial);
	}	
	
	/**
	 * Ingresa elementos al tipo CMICatalogoFlujosEfectivo
	 * @param elemento
	 * @param total
	 */
	private static void setAtributosFlujo(Element elemento, String total){
		elemento.setAttribute("Total",total);
	}
	/**
	 * Ingresa elementos al tipo CMICatalogoCuentasVariaciones
	 * @param elemento
	 * @param abono
	 * @param cargo
	 * @param saldoInicial
	 */
	private static void setAtributosVariaciones(Element elemento, String patrimonioContribuido,
							String patrimonioGeneradoEA, String patrimonioGeneradoE, String ajusteXCambioValor){
		elemento.setAttribute("patrimonioContribuido", patrimonioContribuido);
		elemento.setAttribute("patrimonioGeneradoEA", patrimonioGeneradoEA);
		elemento.setAttribute("patrimonioGeneradoE", patrimonioGeneradoE);
		elemento.setAttribute("ajusteXCambioValor", ajusteXCambioValor);
	}
	
	
	/**
	 * Genera el mapa de de saldos actuales
	 * @param listaCuentas
	 * @param mapaSaldoActual
	 * @param delegator
	 * @return
	 */
	private static Map<String,GenericValue> getMapaSaldo(List<String> listaCuentas, Map<String,GenericValue> mapaSaldoActual, Delegator delegator){
		
		Map<String,GenericValue> mapaSaldoActualSuma = FastMap.newInstance();
		
	    for(String cuenta : listaCuentas){
	    	GenericValue GlAccountHistory = delegator.makeValue("GlAccountHistory");
		    for(Entry<String,GenericValue> entryActual : mapaSaldoActual.entrySet()){
		    	String cuentaActual = entryActual.getKey();
		    	if(cuentaActual.startsWith(cuenta)){
		    		if(mapaSaldoActualSuma.isEmpty() || !mapaSaldoActualSuma.containsKey(cuenta)){
		    			BigDecimal debits = entryActual.getValue().getBigDecimal("postedDebits");
		    			BigDecimal credits = entryActual.getValue().getBigDecimal("postedCredits");
		    			GlAccountHistory.put("glAccountId", cuenta);
		    			GlAccountHistory.put("postedDebits", debits == null ? BigDecimal.ZERO : debits );
		    			GlAccountHistory.put("postedCredits", credits == null ? BigDecimal.ZERO : credits );
		    			mapaSaldoActualSuma.put(cuenta, GlAccountHistory);
		    		} else {
		    			if(mapaSaldoActualSuma.containsKey(cuenta)){
			    			BigDecimal debits = entryActual.getValue().getBigDecimal("postedDebits");
			    			BigDecimal credits = entryActual.getValue().getBigDecimal("postedCredits");
			    			debits = debits == null ? BigDecimal.ZERO : debits ;
			    			credits = credits == null ? BigDecimal.ZERO : credits ;
		    				GlAccountHistory = mapaSaldoActualSuma.get(cuenta);
			    			GlAccountHistory.put("postedDebits", debits.add(GlAccountHistory.getBigDecimal("postedDebits")));
			    			GlAccountHistory.put("postedCredits", credits.add(GlAccountHistory.getBigDecimal("postedCredits")));
			    			mapaSaldoActualSuma.put(cuenta, GlAccountHistory);
		    			}
		    		}
		    	}
		    }
	    }
		
		return mapaSaldoActualSuma;
		
	}
	
	/**
	 * Genera el mapa de saldo inicial
	 * @param listaElementos
	 * @param mapaSaldoActual
	 * @param mapaSaldoInicial
	 * @param delegator
	 * @return
	 */
	private static Map<String,BigDecimal> getMapaSaldoInicial(List<GenericValue> listaElementos, 
					Map<String,GenericValue> mapaSaldoActual,Map<String,BigDecimal> mapaSaldoInicial, 
						Delegator delegator){
		
		Map<String,BigDecimal> mapaSaldoInicialSuma = FastMap.newInstance();
		
		for(GenericValue elemento : listaElementos){
	    	String cuenta = elemento.getString("glAccountId");
		    for(Entry<String,GenericValue> entryActual : mapaSaldoActual.entrySet()){
		    	String cuentaActual = entryActual.getKey();
				BigDecimal saldoInicial = mapaSaldoInicial.get(cuentaActual) == null ? BigDecimal.ZERO : mapaSaldoInicial.get(cuentaActual);
				if(mapaSaldoInicialSuma.isEmpty() || !mapaSaldoInicialSuma.containsKey(cuenta)){
					mapaSaldoInicialSuma.put(cuenta, saldoInicial);
				} else {
					if(mapaSaldoInicialSuma.containsKey(cuenta)){
						mapaSaldoInicialSuma.put(cuenta, saldoInicial.add(mapaSaldoInicialSuma.get(cuenta)));
					}
				}
		    }

		}

		return mapaSaldoInicialSuma;	
		
	}
	

	
	/**
	 * Crea el reporte Presupuestal XML
	 * @param delegator
	 * @param documentBuilder
	 * @param locale
	 * @param timeZone
	 * @param organizacion
	 * @param mes
	 * @param anio
	 * @return
	 * @throws GenericEntityException
	 * @throws SAXException
	 * @throws IOException 
	 */
	private static Document creaPresupuestalXML(Delegator delegator,DocumentBuilder documentBuilder ,Locale locale,
	TimeZone timeZone, GenericValue organizacion, String mes, String anio) throws GenericEntityException, SAXException, IOException {
		
		getClasses(delegator);
		
		//Se obtiene el periodo a consultar 
		Timestamp fechaInicioMes = UtilDateTime.toTimestamp(Integer.valueOf(mes), 1, Integer.valueOf(anio), 0, 0, 0);
		Timestamp fechaFinMes = UtilDateTime.getTimestamp(fechaInicioMes.getTime());
		fechaFinMes = UtilDateTime.adjustTimestamp(fechaFinMes, Calendar.MONTH, 1);
		
		Map<String,String> mapaElemento = FastMap.newInstance();
		List<GenericValue> listaElementos = delegator.findByAnd("ElementoReporte", UtilMisc.toMap("tipoReporteId","PRESUPUESTAL"));
		for (GenericValue elementoRepo : listaElementos) {
			String elementoId = elementoRepo.getString("elementoReporteId");
			mapaElemento.put(elementoId, elementoRepo.getString("enumId"));
		}
		
		// se crea el elemento principal
		Document document = documentBuilder.newDocument();
		Element InfPresupuestal = document.createElement("cmis:InformacionPresupuestal");
		InfPresupuestal.setAttribute("anio", anio);
		//InfPresupuestal.setAttribute("centro","COLSAN");
		InfPresupuestal.setAttribute("centro","FINANCIERA");
		InfPresupuestal.setAttribute("mes",mes);
		InfPresupuestal.setAttribute("xmlns:cmis","http://www.sifascpi.gob.mx/sifascpi");
		InfPresupuestal.setAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
		InfPresupuestal.setAttribute("xsi:schemaLocation","http://www.sifascpi.gob.mx/sifascpi CMISIFASCPIPresupuestal_v2.xsd");
		
		String tipoElemento = "";
		XSSchema s = getXSSchemaReporteXML("/opentaps/financials/config/CMISIFASCPIPresupuestal_v2.xsd");
		for (String tipo : listaTiposPresupuestal) {
			
			Element elementoPrl = null;
			if(tipo.equals("CMIFuenteDeIngresos")){
				elementoPrl = document.createElement("IngresosPresupuestarios");
				tipoElemento = UtilClavePresupuestal.INGRESO_TAG;
			} else {
				elementoPrl = document.createElement("EgresosPresupuestarios");
				tipoElemento = UtilClavePresupuestal.EGRESO_TAG;
			}
			
			XSParticle[] particles = getElementosDelTipo(s, tipo);
			
			Element tipoCatalogo = document.createElement(tipo.substring(3));
		    for (XSParticle xsParticle : particles) {
		    	String nombreCampo = xsParticle.getTerm().asElementDecl().getName();

		    	Element elemento = document.createElement(nombreCampo);
		    	String enumId = mapaElemento.get(nombreCampo);
		    	if(UtilValidate.isNotEmpty(enumId)){
		    		
		    		elemento = getElementPresupuestal(delegator, elemento, tipoElemento, enumId, organizacion, fechaInicioMes, fechaFinMes, anio, mes);
		    		
		    	} else {
		    		if(tipoElemento.equals(UtilClavePresupuestal.EGRESO_TAG)){
		    			setAtributosEgreso(elemento, "0.0", "0.0", "0.0", "0.0", "0.0", "0.0");
		    		} else if(tipoElemento.equals(UtilClavePresupuestal.INGRESO_TAG)){
		    			setAtributosIngreso(elemento, "0.0", "0.0", "0.0", "0.0");
		    		}
		    		
		    	}
		    	tipoCatalogo.appendChild(elemento);
		    	
		    }
		    
		    elementoPrl.appendChild(tipoCatalogo);
		    
		    InfPresupuestal.appendChild(elementoPrl);
		}
		
		document.appendChild(InfPresupuestal);
		
		return document;
		
	}
	
	/**
	 * Ingresa atributos de elemento de Egreso presupuestal
	 * @param elemento
	 * @param egresoAprobado
	 * @param egresoComprometido
	 * @param egresoDevengado
	 * @param egresoEjercido
	 * @param egresoPagado
	 */
	private static void setAtributosEgreso(Element elemento, String egresoAprobado,String ampliacionesReducciones,  String egresoComprometido,
										String egresoDevengado, String egresoEjercido,String egresoPagado){
		elemento.setAttribute("egresoAprobado",egresoAprobado);
		elemento.setAttribute("ampliacionesReducciones",ampliacionesReducciones);
		elemento.setAttribute("egresoComprometido",egresoComprometido);
		elemento.setAttribute("egresoDevengado",egresoDevengado);
		elemento.setAttribute("egresoEjercido",egresoEjercido);
		elemento.setAttribute("egresoPagado",egresoPagado);
	}
	
	
	private static void setAtributosIngreso(Element elemento, String ingresoAprobado,String ampliacionesReducciones,
											String ingresoDevengado, String ingresoCobrado){
		elemento.setAttribute("ingresoAprobado",ingresoAprobado);
		elemento.setAttribute("ampliacionesReducciones",ampliacionesReducciones);
		elemento.setAttribute("ingresoDevengado",ingresoDevengado);
		elemento.setAttribute("ingresoCobrado",ingresoCobrado);
	}
	
	/**
	 * Obtiene un Element por glAccountId con sus campos necesarios 
	 * @param delegator
	 * @param elemento
	 * @param tipoElemento
	 * @param organizacion 
	 * @param glAccountId
	 * @param fechaInicioMes
	 * @param fechaFinMes
	 * @param anio
	 * @param mes
	 * @return
	 * @throws GenericEntityException
	 */
	private static Element getElementPresupuestal(Delegator delegator,Element elemento,String tipoElemento,String enumId, GenericValue organizacion, 
			Timestamp fechaInicioMes, Timestamp fechaFinMes,String anio, String mes) throws GenericEntityException{

		LinkedList<String> listMomentos = new LinkedList<String>();
		int indiceCRIoCOG = -1;
		if(tipoElemento.equals(UtilClavePresupuestal.EGRESO_TAG)){
			indiceCRIoCOG = UtilClavePresupuestal.obtenerIndiceClasificacion(UtilClavePresupuestal.EGRESO_TAG, organizacion.getString("partyId"), "CL_COG", delegator,anio);
			listMomentos.add("COMPROMETIDO");
			listMomentos.add("DEVENGADO_E");
			listMomentos.add("EJERCIDO");
			listMomentos.add("PAGADO");
		} else if (tipoElemento.equals(UtilClavePresupuestal.INGRESO_TAG)){
			indiceCRIoCOG = UtilClavePresupuestal.obtenerIndiceClasificacion(UtilClavePresupuestal.INGRESO_TAG, organizacion.getString("partyId"), "CL_CRI", delegator,anio);
			listMomentos.add("DEVENGADO_I");
			listMomentos.add("RECAUDADO");
		}
		
		String campoEnumeration = UtilClavePresupuestal.ENTITY_TAG_PREFIX+indiceCRIoCOG;
		String campoClasificacion = UtilClavePresupuestal.VIEW_TAG_PREFIX+indiceCRIoCOG;
		
		List<String> listClasificacion = FastList.newInstance();
		listClasificacion.add(delegator.findByPrimaryKey("Enumeration",UtilMisc.toMap("enumId",enumId)).getString("sequenceId"));
		List<String> listEnumeration = FastList.newInstance();
		listEnumeration.add(enumId);
		//Se obtienen los hijos de la clasificacion enumId
		List<String> listEnumerationHijos = FastList.newInstance();
		listEnumerationHijos.add(enumId);
		while(UtilValidate.isNotEmpty(listEnumerationHijos)){
			listClasificacion.addAll(UtilClavePresupuestal.obtenerHijosSequenceId(listEnumerationHijos, delegator));
			listEnumerationHijos = UtilClavePresupuestal.obtenerHijosEnumerationId(listEnumerationHijos, delegator);
			listEnumeration.addAll(listEnumerationHijos);
		}
		
		EntityCondition condiciones = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("postedDate",EntityOperator.GREATER_THAN_EQUAL_TO,fechaInicioMes),
				EntityCondition.makeCondition("postedDate",EntityOperator.LESS_THAN,fechaFinMes),
				EntityCondition.makeCondition("momentoId",EntityOperator.IN,listMomentos),
				EntityCondition.makeCondition(campoEnumeration,EntityOperator.IN,listEnumeration)
				);

		List<String> select = UtilMisc.toList("momentoId","amount","debitCreditFlag","glAccountId");
		List<String> orderBy = UtilMisc.toList("momentoId","debitCreditFlag");
		List<GenericValue> listaMontosxMomento = delegator.findByCondition("SumaXMomento", condiciones, select, orderBy);
		listaMontosxMomento = getMontosXMomento(delegator, listaMontosxMomento,listMomentos);
		
		EntityCondition condicionesCtrlOR = null;
		if(tipoElemento.equals(UtilClavePresupuestal.EGRESO_TAG)){
			condicionesCtrlOR = EntityCondition.makeCondition(EntityOperator.OR,
					EntityCondition.makeCondition("momentoId",EntityOperator.LIKE,"APROBADO"),
					EntityCondition.makeCondition("momentoId",EntityOperator.LIKE,"MODIFICADO%")
					);
		} else if (tipoElemento.equals(UtilClavePresupuestal.INGRESO_TAG)){
			condicionesCtrlOR = EntityCondition.makeCondition(EntityOperator.OR,
					EntityCondition.makeCondition("momentoId",EntityOperator.LIKE,"ESTIMADO")
					);
		}
		
		EntityCondition condicionesCtrl = EntityCondition.makeCondition(EntityOperator.AND,
		EntityCondition.makeCondition("ciclo",anio),
		EntityCondition.makeCondition("mesId",mes),
		EntityCondition.makeCondition(campoClasificacion,EntityOperator.IN,listClasificacion),
		condicionesCtrlOR
		);
		
		//Busqueda de claves presupuestales de los montos por momento ejecutado en tabla AcctgTransEntry
		List<String> selectClaves = UtilMisc.toList("momentoId","monto");
		List<GenericValue> listaCtrlMomento = delegator.findByCondition("SumaClavesCtrlPresup", condicionesCtrl, selectClaves, null);
		
		
		if(tipoElemento.equals(UtilClavePresupuestal.EGRESO_TAG)){
			
			BigDecimal egresoAprobado = BigDecimal.ZERO;
			BigDecimal ampliacionesReducciones = BigDecimal.ZERO;
			BigDecimal egresoComprometido = BigDecimal.ZERO;
			BigDecimal egresoDevengado = BigDecimal.ZERO;
			BigDecimal egresoEjercido = BigDecimal.ZERO;
			BigDecimal egresoPagado = BigDecimal.ZERO;
			
			for(GenericValue egresoMomento : listaCtrlMomento){
				if(egresoMomento.getString("momentoId").startsWith("APROBADO")){
					egresoAprobado = egresoMomento.getBigDecimal("monto");
				}
				else if(egresoMomento.getString("momentoId").startsWith("MODIFICADO")){
					if(egresoMomento.getString("momentoId").startsWith("MODIFICADO_AMP")){
						ampliacionesReducciones = ampliacionesReducciones.add(egresoMomento.getBigDecimal("monto"));
					} else {
						ampliacionesReducciones = ampliacionesReducciones.subtract(egresoMomento.getBigDecimal("monto"));
					}
				}
			}
			
			for(GenericValue egresoMomento : listaMontosxMomento){
				if(egresoMomento.getString("momentoId").startsWith("COMPROMETIDO")){
					egresoComprometido = egresoMomento.getBigDecimal("amount");
				}
				if(egresoMomento.getString("momentoId").startsWith("DEVENGADO")){
					egresoDevengado = egresoMomento.getBigDecimal("amount");
				}
				if(egresoMomento.getString("momentoId").startsWith("EJERCIDO")){
					egresoEjercido = egresoMomento.getBigDecimal("amount");
				}
				if(egresoMomento.getString("momentoId").startsWith("PAGADO")){
					egresoPagado = egresoMomento.getBigDecimal("amount");
				}
				
			}
			
			setAtributosEgreso(elemento, df.format(egresoAprobado), df.format(ampliacionesReducciones), df.format(egresoComprometido),
					df.format(egresoDevengado), df.format(egresoEjercido), df.format(egresoPagado));

		} else if (tipoElemento.equals(UtilClavePresupuestal.INGRESO_TAG)){
			
			
			BigDecimal ingresoAprobado = BigDecimal.ZERO;
			BigDecimal ampliacionesReducciones = BigDecimal.ZERO;
			BigDecimal ingresoDevengado = BigDecimal.ZERO;
			BigDecimal ingresoCobrado = BigDecimal.ZERO;
			for(GenericValue egresoMomento : listaCtrlMomento){
				if(egresoMomento.getString("momentoId").startsWith("ESTIMADO")){
					ingresoAprobado = egresoMomento.getBigDecimal("monto");
				}
				else if(egresoMomento.getString("momentoId").startsWith("MODIFICADO")){
					if(egresoMomento.getString("momentoId").startsWith("MODIFICADO_AMP")){
						ampliacionesReducciones = ampliacionesReducciones.add(egresoMomento.getBigDecimal("monto"));
					} else {
						ampliacionesReducciones = ampliacionesReducciones.subtract(egresoMomento.getBigDecimal("monto"));
					}
				}
			}
			for(GenericValue egresoMomento : listaMontosxMomento){
				if(egresoMomento.getString("momentoId").startsWith("DEVENGADO")){
					ingresoDevengado = egresoMomento.getBigDecimal("amount");
				}
				if(egresoMomento.getString("momentoId").startsWith("RECAUDADO")){
					ingresoCobrado = egresoMomento.getBigDecimal("amount");
				}
				
			}
			
			setAtributosIngreso(elemento, df.format(ingresoAprobado), df.format(ampliacionesReducciones), 
					df.format(ingresoDevengado), df.format(ingresoCobrado));

		}
		
		return elemento;
		
	}
	
	/**
	 * Obtiene la suma de los momentos 
	 * Suma los momentos subsecuentes para obtener el monto actual EJ. (COMPROMETIDO = SALDO_COMPROMETIDO + SALDO_DEVENGADO + SALDO_EJERCIDO + SALDO_PAGADO)
	 * @param delegator
	 * @param listaMontosxMomento
	 * @param listMomentos
	 * @return
	 * @throws GenericEntityException
	 */
	private static List<GenericValue> getMontosXMomento(Delegator delegator, 
			List<GenericValue> listaMontosxMomento, LinkedList<String> listMomentos) throws GenericEntityException{
		List<GenericValue> listaMontosxMomentoNuevo = FastList.newInstance();
		
		Map<String,Object> mapaSaldoMomento = FastMap.newInstance();
		
		String naturaleza = new String();
		String glAccountClassId = new String();
		String debitCreditFlag = new String();
		BigDecimal monto = BigDecimal.ZERO;
		String momentoId = new String();
		for (GenericValue SumaXMonto : listaMontosxMomento) {
			glAccountClassId = SumaXMonto.getRelatedOne("GlAccount").getString("glAccountClassId");
			naturaleza = getNaturaleza(glAccountClassId).substring(0, 1);
			debitCreditFlag = SumaXMonto.getString("debitCreditFlag");
			monto = SumaXMonto.getBigDecimal("amount");
			momentoId = SumaXMonto.getString("momentoId");
			if(naturaleza.equals(debitCreditFlag)){
				UtilMisc.addToBigDecimalInMap(mapaSaldoMomento,momentoId , monto);
			} else {
				UtilMisc.addToBigDecimalInMap(mapaSaldoMomento,momentoId , monto.negate());
			}
		}
		
		BigDecimal montoSaldo = BigDecimal.ZERO;
		BigDecimal montoSaldoSuma = BigDecimal.ZERO;
		for (int i = 0; i < listMomentos.size(); i++) {
			String momento = listMomentos.get(i);
			
			for (int j = listMomentos.indexOf(momento); j < listMomentos.size(); j++) {
				montoSaldo = (BigDecimal) mapaSaldoMomento.get(listMomentos.get(j));
				montoSaldo = UtilNumber.getBigDecimal(montoSaldo);
				montoSaldoSuma = montoSaldoSuma.add(montoSaldo);
			}
			
			GenericValue SumaxMomento = delegator.makeValue("SumaXMomento");
			SumaxMomento.put("momentoId", momento);
			SumaxMomento.put("amount", montoSaldoSuma);
			listaMontosxMomentoNuevo.add(SumaxMomento);
			
			montoSaldoSuma = BigDecimal.ZERO;
			
		}
		
		
		return listaMontosxMomentoNuevo;
	}
	
}