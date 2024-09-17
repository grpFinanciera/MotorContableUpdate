package org.opentaps.tests.Finanzas;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.minilang.method.entityops.FindByAnd;
import org.ofbiz.order.finaccount.UtilClavePresupuestal;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.tests.OpentapsTestCase;
import org.opentaps.tests.Adquisiciones.PedidoTests;

import com.opensourcestrategies.financials.tesoreria.TesoreriaOrdenPago;

public class OrdenPagoTests extends OpentapsTestCase {
	
	private GenericValue user;
	private List<GenericValue> listCuentaBancariaId;
	private List<GenericValue> listProductos;
	private List<GenericValue> listClavePresupuestal;
	private List<GenericValue> listPaymentType;
	private Timestamp fechaHoy = UtilDateTime.nowTimestamp();
	private SimpleDateFormat sdfMes = new SimpleDateFormat("MM");
	private SimpleDateFormat sdfAnio = new SimpleDateFormat("yyyy");
	private int numeroProductosAprobar = 70;
	private GenericValue partyFrom;
	private GenericValue partyTo;
	private String invoiceType = "PURCHASE_INVOICE";
	private final static String INVOICE_IN_PROCESS = "INVOICE_IN_PROCESS";
	private final static String eventoEjercido = "PEEJ-02";
	private final static String eventoPagado = "PEPA-06";//PEPA-08
	private final static String currencyUomId = "MXN";
	private final static int factorMultiplicacion = 100; //Montos alrededor de los cientos de pesos
	private Map<String,Object> mapaMontoXAuxiliar = FastMap.newInstance();	//Mapa que almacena la cantidad a afectar por auxiliar (no por tipo de auxiliar)
	private Map<String,List<String>> mapaAuxiliaresNoBancos = FastMap.newInstance(); 	//Mapa que almacena listas de auxiliares agrupados por su tipo que no sean bancos <"Tipo",["Auxiliar1","Auxiliar2",...]>
	
    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.admin = delegator.findByPrimaryKeyCache("UserLogin", UtilMisc.toMap("userLoginId", "admin"));
        this.user = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", "jchavez"));
        this.listCuentaBancariaId = delegator.findByAnd("PartyCuentaBancaria", UtilMisc.toMap("partyId",this.user.getString("partyId")));
        this.listPaymentType = delegator.findByAnd("PaymentType", UtilMisc.toMap("parentTypeId","DISBURSEMENT"));
        this.partyFrom = delegator.findByPrimaryKeyCache("Party", UtilMisc.toMap("partyId", "Proveedor_001"));//PRBI00001
        this.partyTo = delegator.findByAnd("Party", UtilMisc.toMap("partyTypeId", "PARTY_GROUP","Nivel_id","TIPO_INSTITUCION")).get(0);
        //se obtienen n claves al azar que tengan presupuesto del mes en corriente
        EntityConditionList<EntityExpr> listaCondiciones = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("mesId",sdfMes.format(this.fechaHoy)),
				EntityCondition.makeCondition("ciclo",sdfAnio.format(this.fechaHoy)),
				EntityCondition.makeCondition("momentoId","DEVENGADO_E"),
				EntityCondition.makeCondition("monto",EntityOperator.GREATER_THAN,BigDecimal.valueOf(numeroProductosAprobar*10.0))));
        EntityListIterator itClavePresupuestal = delegator.findListIteratorByCondition("ControlPresupuestal", 
        		listaCondiciones, null, null);
        listClavePresupuestal = itClavePresupuestal.getPartialList(0, numeroProductosAprobar);
        itClavePresupuestal.close(); 
    }
    
    @Override
    public void tearDown() throws Exception {
        admin = null;
        user = null;
        listProductos = null;
        mapaMontoXAuxiliar = FastMap.newInstance();
        mapaAuxiliaresNoBancos = FastMap.newInstance();
        listCuentaBancariaId = FastList.newInstance();
        super.tearDown();
    }

	public void testPagoNomina1Nveces() throws GenericEntityException{
		for (int i = 0; i < 100; i++) {
			pagoNomina1(i);
		}
	}
	
	//Crear pre orden de pago de nomina
	public void pagoNomina1(int numero) throws GenericEntityException{
		
		//Se crea la preorden de pago
		Map<String,Object> resultadoPreOrden = crearPreordenPago(numero);
		String invoiceId = (String) resultadoPreOrden.get("invoiceId"); //preordenId
		GenericValue preOrden = this.delegator.findByPrimaryKey("Invoice", UtilMisc.toMap("invoiceId",invoiceId));
		assertNotNull("No se registro la preoden de pago",preOrden);
		
		//Registro de los articulos 
		agregarItemsPreOrden(invoiceId);
		
		//Ejercer preorden
		Map<String,Object> resultadoEjercerPreOrden = ejercerPreOrden(invoiceId);
		
		
		//Revisar contabilidad
		//Verificar la poliza
		List<GenericValue> listPoliza = delegator.findByAnd("AcctgTrans", UtilMisc.toMap("invoiceId",invoiceId));
		assertNotEmpty("No se genero poliza ", listPoliza);
		assertEquals("Se creo mas de una poliza ", listPoliza.size(), 1);
		GenericValue poliza = listPoliza.get(0);
		
		List<GenericValue> listPolizaDetalle = delegator.findByAnd("AcctgTransEntry", UtilMisc.toMap("acctgTransId",poliza.getString("acctgTransId")));
		BigDecimal sumaCargos = BigDecimal.ZERO;
		BigDecimal sumaAbonos = BigDecimal.ZERO;
			for (GenericValue genericValue : listPolizaDetalle) {
				if(genericValue.getString("debitCreditFlag").equals("C")){
					sumaAbonos = sumaAbonos.add(genericValue.getBigDecimal("amount"));
				} else {
					sumaCargos = sumaCargos.add(genericValue.getBigDecimal("amount"));
				}
			}
	       
		assertEquals("No se registraron los cargos y/o abonos correctos "+poliza.getString("acctgTransId"), listPolizaDetalle.size(),listClavePresupuestal.size()*2);
		assertEquals("La suma de cargos y abonos no esta balanceada", sumaCargos,sumaAbonos);
		
		
		//Busqueda de Saldos en auxiliares
//		for (Entry<String, Object> montoXAuxiliar : mapaMontoXAuxiliar.entrySet()) {
//			
//		}
		
		//Numero de polizas antes de pagar
		long numPolizas = delegator.findCountByAnd("AcctgTrans");
		
		pagarParcialmente(invoiceId);
		numPolizas++;
		//Pagar Pre orden
		while(getMontoBancoRestante(invoiceId).compareTo(BigDecimal.ZERO) > 0){
			pagarParcialmente(invoiceId);
			numPolizas++;
			long numPolizasReales = delegator.findCountByAnd("AcctgTrans");
			assertEquals("Se genero mas de una poliza al pagar", numPolizas, numPolizasReales);
		}
		
	}
	
	protected Map<String,Object> crearPreordenPago(int i){
		
		Map<String,Object> inputInvoice = FastMap.newInstance();
		
		inputInvoice.put("userLogin", this.admin);
		inputInvoice.put("invoiceTypeId", this.invoiceType);
		inputInvoice.put("statusId", OrdenPagoTests.INVOICE_IN_PROCESS);
		inputInvoice.put("partyIdFrom", this.partyFrom.getString("partyId"));
		inputInvoice.put("partyId", this.partyTo.getString("partyId"));
		inputInvoice.put("acctgTransTypeId", OrdenPagoTests.eventoEjercido);
		inputInvoice.put("currencyUomId", OrdenPagoTests.currencyUomId);
		inputInvoice.put("description", "Prueba Nomina "+i);
		inputInvoice.put("invoiceMessage", "Mensaje Nomina "+i);
		inputInvoice.put("referenceNumber", "Referencia Nomina "+i);
		
		Map<String, Object> output = runAndAssertServiceSuccess("createInvoice", inputInvoice);
		
		return output;
		
	}
	
	
	protected void agregarItemsPreOrden(String invoiceId) throws GenericEntityException{
		
		Map<String,Object> inputInvoiceItem = FastMap.newInstance();
		
		inputInvoiceItem.put("userLogin", this.admin);
		inputInvoiceItem.put("invoiceId", invoiceId);
		inputInvoiceItem.put("quantity", BigDecimal.ONE);
		
		String clasificacion = new String();
		int numeroItem = 1;
		DecimalFormat df= new DecimalFormat("0000");
		for (GenericValue clavePresup : this.listClavePresupuestal) {
			
			inputInvoiceItem.put("invoiceItemSeqId", df.format(numeroItem));
			
			for (int i = 1; i < 16; i++) {
				clasificacion = clavePresup.getRelatedOne("cpcpClavePresupuestal").getString("clasificacion"+i);
				if(UtilValidate.isNotEmpty(clasificacion)){
					inputInvoiceItem.put(UtilClavePresupuestal.VIEW_TAG_PREFIX+i, clasificacion);
				} else {
					break;
				}
				
			}
			
			BigDecimal monto = BigDecimal.valueOf(Math.random()*factorMultiplicacion);
			
			inputInvoiceItem.put("amount", monto);
			inputInvoiceItem.put("montoRestante", monto);
			
			Map<String, Object> output = runAndAssertServiceSuccess("createInvoiceItem", inputInvoiceItem);
			if(ServiceUtil.isError(output)){
				throw new GenericEntityException(ServiceUtil.getErrorMessage(output));
			}
			numeroItem++;
		}
		
	}
	
	protected Map<String,Object> ejercerPreOrden(String invoiceId) throws GenericEntityException{
		
		Map<String,Object> inputEjercer = FastMap.newInstance();
		
		inputEjercer.put("invoiceId", invoiceId);
		inputEjercer.put("userLogin", this.admin);
		inputEjercer.put("organizationPartyId", OpentapsTestCase.organizationPartyId);
		inputEjercer.put("acctgTransTypeId", OrdenPagoTests.eventoPagado);
		inputEjercer.put("moneda", OrdenPagoTests.currencyUomId);
		
		llenaMapasPago(inputEjercer, invoiceId);
		
		Map<String, Object> output = runAndAssertServiceSuccess("enviarOrdenEjercer", inputEjercer);
		
		return output;
		
	}
	
	protected void llenaMapasPago(Map<String,Object> inputEjercer, String invoiceId) throws GenericEntityException{
		
		Map<String,Object> amountMap = FastMap.newInstance();
		Map<String,Object> montoMap = FastMap.newInstance();
		Map<String,Object> catalogoCargoContMap = FastMap.newInstance();
		Map<String,Object> catalogoAbonoContMap = FastMap.newInstance();
		Map<String,Object> clavePresupuestalMap = FastMap.newInstance();
		Map<String,Object> invoiceItemSeqIdMap = FastMap.newInstance();
		
		//Aqui puede buscar las lineas contables dependiendo del tipo de evento
		List<GenericValue> listContables = delegator.findByAnd("LineaContable",UtilMisc.toMap("acctgTransTypeId",OrdenPagoTests.eventoPagado),UtilMisc.toList("secuencia"));
		
		//Mapa para guardar las decripciones de las lineas concatenadas
		Map<Long,String> mapaLineaContableDes = FastMap.newInstance();
		//Se llenan las lineas contables
		for (GenericValue lineaContable : listContables) {
			Long numLinea = lineaContable.getLong("secuencia");
			String descripcion = lineaContable.getString("descripcion").trim().replaceAll(" ", "");
			mapaLineaContableDes.put(numLinea, descripcion);
			
			String catalogoCargo = lineaContable.getString("catalogoCargo");
			String catalogoAbono = lineaContable.getString("catalogoAbono");
			
			if(catalogoCargo != null && !catalogoCargo.equals(TesoreriaOrdenPago.OrdenPagoBanco) ){
				mapaAuxiliaresNoBancos.put(catalogoCargo, new ArrayList<String>());
			}
			
			if(catalogoAbono != null && !catalogoAbono.equals(TesoreriaOrdenPago.OrdenPagoBanco) ){
				mapaAuxiliaresNoBancos.put(catalogoAbono,  new ArrayList<String>());
			}
			
		}
		
		if(UtilValidate.isNotEmpty(mapaAuxiliaresNoBancos)){
			//Busqueda de auxiliares por tipo e incorporacion al mapa
			List<GenericValue> listAuxiliares = delegator.findByCondition("Party", 
													EntityCondition.makeCondition("partyTypeId", 
															EntityOperator.IN, mapaAuxiliaresNoBancos.keySet().toArray()), 
																UtilMisc.toList("partyId"), null);
			for (GenericValue Auxiliar : listAuxiliares) {
				String tipoAuxiliar = Auxiliar.getString("partyTypeId");
				List<String> listaAuxiliaresIngresados = mapaAuxiliaresNoBancos.get(tipoAuxiliar);
				listaAuxiliaresIngresados.add(Auxiliar.getString("partyId"));
				mapaAuxiliaresNoBancos.put(tipoAuxiliar, listaAuxiliaresIngresados);
			}
		}

		
		
		List<GenericValue> listItem = delegator.findByAnd("InvoiceItem", UtilMisc.toMap("invoiceId",invoiceId),UtilMisc.toList("invoiceItemSeqId"));
		
		int i = 1;
		for (GenericValue Item : listItem) {
			
			String clavePresupuestal = UtilClavePresupuestal.getClavePresupuestal(Item, delegator, 
											UtilClavePresupuestal.EGRESO_TAG, OpentapsTestCase.organizationPartyId,PedidoTests.sdfAnio.format(this.fechaHoy));
			amountMap.put(Integer.toString(i), Item.getString("amount"));
			clavePresupuestalMap.put(Integer.toString(i), clavePresupuestal);
			invoiceItemSeqIdMap.put(Integer.toString(i), Item.getString("invoiceItemSeqId"));
			
			//llenado de mapas para lineas contables
			int j = 1;
			for (GenericValue lineaContable : listContables) {
				
				String descripcionSinEspacios = mapaLineaContableDes.get(Long.valueOf(j));
				String catalogoCargo = lineaContable.getString("catalogoCargo");	
				String catalogoAbono = lineaContable.getString("catalogoAbono");
				
				if((catalogoCargo != null && catalogoCargo.equals(TesoreriaOrdenPago.OrdenPagoBanco) ) ||
						(catalogoAbono != null &&  catalogoAbono.equals(TesoreriaOrdenPago.OrdenPagoBanco)) ){ //Si tiene bancos si se registra y el monto es por el total del presupuesto
					
					BigDecimal monto = BigDecimal.valueOf(Item.getDouble("amount"));
					guardaDatosEnMapasPago(descripcionSinEspacios, catalogoCargo, catalogoAbono, 
											catalogoCargoContMap, catalogoAbonoContMap, montoMap, monto,j-1);
					
				} else { // si no tiene bancos registra la linea contable aleatoriamente
					
					double valorAleatorio = Math.random();
					if(valorAleatorio > 0.5 || valorAleatorio < 0.2){
						
						BigDecimal monto = BigDecimal.valueOf(Math.random()*factorMultiplicacion);
						guardaDatosEnMapasPago(descripcionSinEspacios, catalogoCargo, catalogoAbono, 
								catalogoCargoContMap, catalogoAbonoContMap, montoMap, monto,j-1);
						
					} else {
						continue;
					}
					
				}
				
				j++;//Contador de lineas contables
				
			}
			
			i++;//Contador de items
		}
		

		
		inputEjercer.put("amountMap", amountMap);
		inputEjercer.put("montoMap", montoMap);
		inputEjercer.put("catalogoCargoContMap", catalogoCargoContMap);
		inputEjercer.put("catalogoAbonoContMap", catalogoAbonoContMap);
		inputEjercer.put("clavePresupuestalMap", clavePresupuestalMap);
		inputEjercer.put("invoiceItemSeqIdMap", invoiceItemSeqIdMap);
		
	}
	
	private void guardaDatosEnMapasPago(String descripcionSinEspacios, String catalogoCargo, String catalogoAbono, 
											Map<String, Object> catalogoCargoContMap, Map<String, Object> catalogoAbonoContMap,
											Map<String,Object> montoMap, BigDecimal monto, int i) throws GenericEntityException{
		
		if(catalogoCargo != null && catalogoCargo.equals(TesoreriaOrdenPago.OrdenPagoBanco)){
			catalogoCargoContMap.put(descripcionSinEspacios+i, TesoreriaOrdenPago.OrdenPagoBanco);
		} else  if (catalogoCargo != null){
			catalogoCargoContMap.put(descripcionSinEspacios+i,obtenAuxiliarGuardaSuma(catalogoCargo,monto));
		}
		
		if(catalogoAbono != null && catalogoAbono.equals(TesoreriaOrdenPago.OrdenPagoBanco)){
			catalogoAbonoContMap.put(descripcionSinEspacios+i, TesoreriaOrdenPago.OrdenPagoBanco);
		} else if(catalogoAbono != null){
			catalogoAbonoContMap.put(descripcionSinEspacios+i,obtenAuxiliarGuardaSuma(catalogoAbono,monto));
		}
		
		montoMap.put(descripcionSinEspacios+i, monto.toString());
		
	}
	
	protected String obtenAuxiliarGuardaSuma (String tipoAuxiliar, BigDecimal monto){
		
		List<String> listAuxiliar = mapaAuxiliaresNoBancos.get(tipoAuxiliar);
		
		int numeroAuxiliar = (int) (Math.random()*listAuxiliar.size());
		
		String auxiliar = listAuxiliar.get(numeroAuxiliar);
		
		UtilMisc.addToBigDecimalInMap(mapaMontoXAuxiliar, auxiliar, monto);
		
		return auxiliar;
	}
	
	
	protected Map<String,Object> pagarParcialmente(String invoiceId) throws GenericEntityException{
		
		List<GenericValue> listOrdenPago = delegator.findByAnd("OrdenPago", UtilMisc.toMap("invoiceId",invoiceId));
		
		Map<String,String> secuenciaConBanco = FastMap.newInstance();
		Map<String,String> secuenciaSinBanco = FastMap.newInstance();
		Map<String,String> montoBancoContable = FastMap.newInstance();
		Map<String,String> montoContableSinBanco = FastMap.newInstance();
		BigDecimal montoPresupuestal = BigDecimal.ZERO;
		
		assertNotEmpty("No se genero la orden de pago", listOrdenPago);
		assertEquals("Se genero mas de una orden de pago", 1, listOrdenPago.size());
		
		GenericValue OrdenPago = null;
		String ordenPagoId = new String();
		
		if(UtilValidate.isNotEmpty(listOrdenPago)){
			OrdenPago = listOrdenPago.get(0);
			ordenPagoId = OrdenPago.getString("ordenPagoId");
		}
		
		EntityConditionList<EntityExpr> listCondiciones = EntityCondition.makeCondition(
				UtilMisc.toList(
				EntityCondition.makeCondition("ordenPagoId",ordenPagoId),
				EntityCondition.makeCondition("idCatalogoPres","PRESUPUESTO"),
				EntityCondition.makeCondition("montoRestante",EntityOperator.GREATER_THAN,BigDecimal.ZERO)
				));
		
		List<GenericValue> listOrdenPagoMulti = delegator.findByCondition("OrdenPagoMulti", listCondiciones,UtilMisc.toList("montoRestante"),null);
		for (GenericValue OrdenPagoMulti : listOrdenPagoMulti) {
			montoPresupuestal = montoPresupuestal.add(OrdenPagoMulti.getBigDecimal("montoRestante"));
		}
		
		EntityConditionList<EntityExpr> condicionesOR = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("idCatalogoCargo", EntityOperator.EQUALS, "BANCO"),
				EntityCondition.makeCondition("idCatalogoAbono", EntityOperator.EQUALS, "BANCO")),
				EntityOperator.OR);
		
        EntityConditionList<EntityCondition> condicionesBanco = EntityCondition.makeCondition(UtilMisc.toList(
        		condicionesOR,
                EntityCondition.makeCondition("ordenPagoId", EntityOperator.EQUALS, ordenPagoId)),
                EntityOperator.AND);
        
		
		List<GenericValue> montoPagarContable = delegator.findByCondition("ObtenerMontoContableAPagar", condicionesBanco , null, null);
		int numPagar = 1;
		BigDecimal montoRestante = BigDecimal.ZERO;
		String secuencia = new String();
		for (GenericValue LineaContableBanco : montoPagarContable) {
			montoRestante = LineaContableBanco.getBigDecimal("montoRestante");
			secuencia = LineaContableBanco.getString("secuencia");
			if(montoRestante.compareTo(BigDecimal.ZERO) > 0){
				montoBancoContable.put(Integer.toString(numPagar), montoRestante.multiply(BigDecimal.valueOf(Math.random())).toString());
				secuenciaConBanco.put(Integer.toString(numPagar), secuencia);
				numPagar++;
			}
		}
		
		EntityCondition condicionesPeriodo = EntityCondition.makeCondition(EntityOperator.AND,										  
				EntityCondition.makeCondition("ordenPagoId", EntityOperator.EQUALS, ordenPagoId),
				EntityCondition.makeCondition("montoRestante", EntityOperator.GREATER_THAN, BigDecimal.ZERO));
		
		List<GenericValue> datosLineasCont = delegator.findByCondition("ConsultaLineasContablesOrdenPago", condicionesPeriodo , null,
														 UtilMisc.toList("descripcion"));
		String idCatalogoCargo = new String();
		String idCatalogoAbono = new String();
		int numSinBanco = 1;
		for (GenericValue LineaCont : datosLineasCont) {
			secuencia = LineaCont.getString("secuencia");
			montoRestante = LineaCont.getBigDecimal("montoRestante");
			idCatalogoCargo = LineaCont.getString("idCatalogoCargo");
			idCatalogoAbono = LineaCont.getString("idCatalogoAbono");
			
			if( ( idCatalogoCargo != null && idCatalogoCargo.equals(TesoreriaOrdenPago.OrdenPagoBanco) ) ||
					( idCatalogoAbono != null && idCatalogoAbono.equals(TesoreriaOrdenPago.OrdenPagoBanco) ) ){
				//Nada
			} else {
				montoContableSinBanco.put(Integer.toString(numSinBanco), montoRestante.toString());
				secuenciaSinBanco.put(Integer.toString(numSinBanco), secuencia);
				numSinBanco++;
			}
			
		}
		
		
		
		Map<String,Object> inputPago = FastMap.newInstance();
		
		int indiceCuenta = (int) Math.random()*this.listCuentaBancariaId.size();
		GenericValue PartyCuenta = this.listCuentaBancariaId.get(indiceCuenta);
		String cuentaBancariaId = PartyCuenta.getString("cuentaBancariaId");
		GenericValue CuentaBancaria = delegator.findByPrimaryKey("CuentaBancaria", UtilMisc.toMap("cuentaBancariaId",cuentaBancariaId));
		String bancoId = CuentaBancaria.getString("bancoId");
		
		GenericValue PaymentType = this.listPaymentType.get((int) Math.random()*this.listPaymentType.size());
		
		inputPago.put("userLogin", this.user);
		inputPago.put("invoiceId", invoiceId);
		inputPago.put("ordenPagoId", ordenPagoId);
		inputPago.put("bancoId", bancoId);
		inputPago.put("cuentaBancariaId",cuentaBancariaId);
		inputPago.put("comentario", "Pago Parcial 1");
		inputPago.put("organizationPartyId", organization.get("partyId"));
		inputPago.put("paymentRefNum", "1");
		inputPago.put("paymentTypeId", PaymentType.getString("paymentTypeId"));
		inputPago.put("acctgTransTypeId", OrdenPagoTests.eventoPagado);
		inputPago.put("comentario", "Pago Parcial 1");
		inputPago.put("montoPago", BigDecimal.ZERO);
		inputPago.put("fechaContable", this.fechaHoy);
		inputPago.put("montoBancoContable", montoBancoContable);
		inputPago.put("secuenciaConBanco", secuenciaConBanco);
		inputPago.put("montoContableSinBanco", montoContableSinBanco);
		inputPago.put("secuenciaSinBanco", secuenciaSinBanco);
		inputPago.put("montoPresupuestal", montoPresupuestal);
		
		Map<String,Object> outputPago = runAndAssertServiceSuccess("enviarOrdenPagadoParcial", inputPago);
		
		return outputPago;
		
	}
	
	protected BigDecimal getMontoBancoRestante(String invoiceId) throws GenericEntityException{
		
		List<GenericValue> listOrdenPago = delegator.findByAnd("OrdenPago", UtilMisc.toMap("invoiceId",invoiceId));
		GenericValue OrdenPago = null;
		String ordenPagoId = new String();
		if(UtilValidate.isNotEmpty(listOrdenPago)){
			OrdenPago = listOrdenPago.get(0);
			ordenPagoId = OrdenPago.getString("ordenPagoId");
		}
		
		EntityConditionList<EntityExpr> condicionesOR = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("idCatalogoCargo", EntityOperator.EQUALS, "BANCO"),
				EntityCondition.makeCondition("idCatalogoAbono", EntityOperator.EQUALS, "BANCO")),
				EntityOperator.OR);
		
        EntityConditionList<EntityCondition> condicionesBanco = EntityCondition.makeCondition(UtilMisc.toList(
        		condicionesOR,
                EntityCondition.makeCondition("ordenPagoId", EntityOperator.EQUALS, ordenPagoId)),
                EntityOperator.AND);
        List<GenericValue> listOrdenPagoMulti = delegator.findByCondition("ObtenerMontoContableAPagar", condicionesBanco, UtilMisc.toList("montoRestante"), null);
        BigDecimal montoRestante = BigDecimal.ZERO; 
        for (GenericValue genericValue : listOrdenPagoMulti) {
        	montoRestante = montoRestante.add(genericValue.getBigDecimal("montoRestante"));
		}
        
        return montoRestante;
	}
	
	
}
