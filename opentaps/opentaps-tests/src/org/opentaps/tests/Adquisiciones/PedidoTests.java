package org.opentaps.tests.Adquisiciones;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.GeneralException;
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
import org.ofbiz.order.finaccount.UtilClavePresupuestal;
import org.opentaps.foundation.infrastructure.InfrastructureException;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.tests.OpentapsTestCase;
import org.opentaps.tests.purchasing.PurchasingOrderTests;
import org.opentaps.tests.webapp.FakeHttpServletRequest;
import org.opentaps.tests.webapp.FakeHttpServletResponse;
import org.ofbiz.order.shoppingcart.CartItemModifyException;
import org.ofbiz.order.shoppingcart.CheckOutHelper;
import org.ofbiz.order.shoppingcart.ItemNotFoundException;
import org.ofbiz.order.shoppingcart.ShoppingCart;
import org.ofbiz.order.shoppingcart.ShoppingCartItem;
import org.ofbiz.service.GenericServiceException;

public class PedidoTests extends OpentapsTestCase {
	
	private GenericValue user;
	private List<GenericValue> listProductos;
	private List<GenericValue> listClavePresupuestal;
	private Timestamp fechaHoy = UtilDateTime.nowTimestamp();
	private SimpleDateFormat sdfMes = new SimpleDateFormat("MM");
	public static SimpleDateFormat sdfAnio = new SimpleDateFormat("yyyy");
	private int numeroProductosAprobar = 5;
	private GenericValue supplier;
	private ShoppingCart shoppingCart;
	
	
	private static final String MODULE = PurchasingOrderTests.class.getName();
	
	
    @Override
    public void setUp() throws Exception {
        super.setUp();
        admin = delegator.findByPrimaryKeyCache("UserLogin", UtilMisc.toMap("userLoginId", "admin"));
        user = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", "lzamarron"));//mcornejo
        supplier = delegator.findByPrimaryKeyCache("Party", UtilMisc.toMap("partyId", "Proveedor_001"));//PRBI00001
        //se obtienen n claves al azar que tengan presupuesto del mes en corriente
        EntityConditionList<EntityExpr> listaCondiciones = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("mesId",sdfMes.format(this.fechaHoy)),
				EntityCondition.makeCondition("ciclo",sdfAnio.format(this.fechaHoy)),
				EntityCondition.makeCondition("momentoId","DISPONIBLE"),
				EntityCondition.makeCondition("monto",EntityOperator.GREATER_THAN,numeroProductosAprobar*10.0)));
        EntityListIterator itClavePresupuestal = delegator.findListIteratorByCondition("ControlPresupuestal", 
        		listaCondiciones, null, null);
        listClavePresupuestal = itClavePresupuestal.getPartialList(0, numeroProductosAprobar);
//        listClavePresupuestal = delegator.findByCondition("ControlPresupuestal",listaCondiciones,null,null);
        itClavePresupuestal.close(); 
    }
	
    @Override
    public void tearDown() throws Exception {
        admin = null;
        user = null;
        listProductos = null;
        super.tearDown();
    }

    public void testCompraNveces() throws GeneralException{
		
		for (int i = 0; i < 1; i++) {
			pruebaCompra(String.valueOf(i));
		}
		
	}
	
	public void pruebaCompra(String numero) throws GenericServiceException, CartItemModifyException, ItemNotFoundException, GenericEntityException, RepositoryException, InfrastructureException{
		
		this.shoppingCart = new ShoppingCart(this.delegator, null ,this.locale, "MXN");
		this.shoppingCart.setAcctgTransTypeId("PECO-02");
		this.shoppingCart.setArticuloId("10005");
		this.shoppingCart.setBillFromVendorPartyId(this.supplier.getString("partyId"));
		this.shoppingCart.setBillToCustomerPartyId("1");
		this.shoppingCart.setChannelType("UNKNWN_SALES_CHANNEL");
		this.shoppingCart.setFechaCreacion(this.fechaHoy);
		this.shoppingCart.setOrderName("Prueba Pedido "+numero);
		this.shoppingCart.setOrderType("PURCHASE_ORDER");
		this.shoppingCart.setUserLogin(this.user, this.dispatcher);
		this.shoppingCart.setTipoAdjudicacionId("10002");
		this.shoppingCart.setShipAfterDate(this.fechaHoy);
		
		String nombre = this.shoppingCart.getOrderName();
		
		
		FakeHttpServletRequest request = new FakeHttpServletRequest();
		FakeHttpServletResponse response = new FakeHttpServletResponse();
		request.setAttribute("dispatcher", this.dispatcher);
		request.setAttribute("delegator", this.delegator);
		request.setAttribute("userLogin", this.user);
		request.setAttribute("shoppingCart", this.shoppingCart);
		request.setAttribute("login.username", this.user.getString("userLoginId"));
		request.getSession().setAttribute("delegatorName", this.delegator.getDelegatorName());
		request.getSession().setAttribute("_WEBAPP_NAME_", "purchasing");
		request.getSession().setAttribute("login.username", this.user.getString("userLoginId"));
		request.getSession().setAttribute("userLogin", this.user);
		request.getSession().setAttribute("locale", this.locale);
		
		listProductos = FastList.newInstance();
		for (int i = 0; i < numeroProductosAprobar; i++) {
			
			Map<String,String> mapaTags = FastMap.newInstance();
			mapaTags = getMapaAcctg(listClavePresupuestal.get(i));
			
			GenericValue product = createTestProduct("prueba de Producto "+i, admin);
			ShoppingCartItem item = ShoppingCartItem.makePurchaseOrderItem(null, product.getString("productId"), BigDecimal.valueOf(i), 
					BigDecimal.ZERO, null, mapaTags, null, null, null, null, dispatcher, this.shoppingCart, 
					null, null, null, null, null, 
					null, null, null, null,null); 
			listProductos.add(product);
		}
		
		Map<String, Object> callCtxt = new HashMap<String, Object>();
		callCtxt.put("request", request);
		callCtxt.put("response", response);
		callCtxt.put("userLogin", this.user);
//		Map<String, Object> callResults = dispatcher.runSync("creaOrden", callCtxt);
		CheckOutHelper checkOutHelper = new CheckOutHelper(dispatcher, delegator, this.shoppingCart);
		Map callResult = checkOutHelper.createOrder(this.user);
		String orderId = (String) callResult.get("orderId");
		
       // Verificar el estatus
       GenericValue pOrder = delegator.findByPrimaryKeyCache("OrderHeader", UtilMisc.toMap("orderId", orderId));
       assertEquals(String.format("Wrong status for order [%1$s]", orderId), "ORDER_CREATED", pOrder.getString("statusId"));
       
//		//Verificar la poliza
       List<GenericValue> listPoliza = delegator.findByAnd("AcctgTrans", UtilMisc.toMap("orderId",orderId));
       assertNotEmpty("No se genero poliza ", listPoliza);
       assertEquals("Se creo mas de una poliza ", listPoliza.size(), 1);
       GenericValue poliza = listPoliza.get(0);
       assertNotSame("La descripcion no es igual ",nombre, poliza.getString("description"));
       
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
       
       assertEquals("No se registraron los cargos y/o abonos correctos ", listPolizaDetalle.size(),listClavePresupuestal.size()*2);
       assertEquals("La suma de cargos y abonos no esta balanceada", sumaCargos,sumaAbonos);
		
	}
	
	public Map<String,String> getMapaAcctg(GenericValue articuloClave) throws GenericEntityException{
		
		Map<String,String> mapaTags = FastMap.newInstance();
		
		String clasificacion = new String();
		for (int i = 1; i < UtilClavePresupuestal.TAG_COUNT; i++) {	
			clasificacion = articuloClave.getRelatedOne("cpcpClavePresupuestal").getString("clasificacion"+i);
			if(UtilValidate.isNotEmpty(clasificacion)){
					mapaTags.put(UtilClavePresupuestal.ENTITY_TAG_PREFIX+i,clasificacion);
			} else {
				break;
			}
		}
		
		return mapaTags;
	}
	
	
	/**
	 * 
	 * @param articuloClave
	 * @return
	 * @throws GenericEntityException
	 */
	public Map<String,String> getMapaTags(GenericValue articuloClave) throws GenericEntityException{
		
		Map<String,String> mapaTags = FastMap.newInstance();
		String ciclo = sdfAnio.format(this.fechaHoy);
		int indice = UtilClavePresupuestal.indiceClasAdmin(UtilClavePresupuestal.EGRESO_TAG, this.organizationPartyId, delegator,ciclo);
		GenericValue estructuraPresup = UtilClavePresupuestal.obtenEstructPresupuestal(UtilClavePresupuestal.EGRESO_TAG, this.organizationPartyId, delegator,ciclo);
		
		String clasificacion = new String();
		String acctgTagEnumId = new String();
		for (int i = 1; i < UtilClavePresupuestal.TAG_COUNT; i++) {	
			clasificacion = articuloClave.getRelatedOne("cpcpClavePresupuestal").getString("clasificacion"+i);
			if(UtilValidate.isNotEmpty(clasificacion)){
				if(indice == i){
					mapaTags.put(UtilClavePresupuestal.ENTITY_TAG_PREFIX+"Admin",clasificacion);
				} else {
					acctgTagEnumId = UtilClavePresupuestal.obtenEnumId(clasificacion, estructuraPresup.getString("clasificacion"+i), delegator);
					mapaTags.put(UtilClavePresupuestal.ENTITY_TAG_PREFIX+i,acctgTagEnumId);
				}
				
				
			} else {
				break;
			}
			
		}
		
		return mapaTags;
	}

	
}
