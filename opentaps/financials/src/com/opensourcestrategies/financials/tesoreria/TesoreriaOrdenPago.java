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

package com.opensourcestrategies.financials.tesoreria;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.common.AB.UtilCommon;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.LineaMotorContable;
import org.ofbiz.order.finaccount.UtilClavePresupuestal;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.foundation.action.ActionContext;

import com.ibm.icu.util.Calendar;
import com.opensourcestrategies.financials.util.UtilFinancial;



/**
 * 
 * 
 * 
 */
public final class TesoreriaOrdenPago {
	public final static String EstatusEjercidoParcial = "EJERCIDO_PARCIAL";
	public final static String EstatusEjercidoTotal = "EJERCIDO";
	public final static String OrdenPagoPendiente = "ORDEN_PAGO_PENDIENTE";
	public final static String OrdenPagoPagada = "ORDEN_PAGO_PAGADA";
	public final static String OrdenPagoParcial = "ORDEN_PAGO_PARCIAL";
	public final static String OrdenCobroParcial = "ORDEN_COBRO_PARCIAL";
	public final static String OrdenPagoBanco = "BANCO";
	public final static String OrdenPagoPresupuesto = "PRESUPUESTO";
	public final static String OrdenCobroPresupuesto = "PRESUPUESTO";
	public final static String EstatusDevengadoIngParcial = "DEVENGADO_PARCIAL";
	public final static String EstatusDevengadoIngTotal = "DEVENGADO";
	public final static String OrdenCobroBanco = "BANCO";
	public final static String OrdenCobroPendiente = "ORDEN_COBRO_PENDIENT";
	public final static String OrdenCobroRecaudar = "ORDEN_COBRO_RECAUDAR";
	public final static String TipoClaveEgreso = "EGRESO";
	public final static String TipoClaveIngreso = "INGRESO";	
	public static BigDecimal TOTAL_MONTO_PRES_PAGO = BigDecimal.ZERO;
	public static BigDecimal TOTAL_MONTO_PRES_COBRO = BigDecimal.ZERO;
	private TesoreriaOrdenPago() {
	}

	private static String MODULE = TesoreriaServices.class.getName();	
		
	/**
	 * Metodo que crea la nueva orden de pago para el tesorero.
	 * Con el detalle de los cargos, abonos y auxiliares configurados en el documento de pago  
	 */
	@SuppressWarnings({ "unchecked", "null" })
	public static Map enviarOrdenEjercer(DispatchContext dctx, Map context)
			throws ParseException, GeneralException {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		String invoiceId = (String) context.get("invoiceId");
		String organizationPartyId = (String) context.get("organizationPartyId");
		String acctgTransTypeId = (String) context.get("acctgTransTypeId"); //Evento de pago
		Map amountMap = (Map) context.get("amountMap"); //Mapa de montos a ejercer por Invoice_item
		Map montoMap = (Map) context.get("montoMap");	//Mapa de montos de lineas contables por Invoice_item
		Map catalogoCargoContMap = (Map) context.get("catalogoCargoContMap"); //Mapa de auxiliares de cargo por linea contable de cada Invoice_item
		Map catalogoAbonoContMap = (Map) context.get("catalogoAbonoContMap"); //Mapa de auxiliares de cargo por linea contable de cada Invoice_item		
		Map clavePresupuestalMap = (Map) context.get("clavePresupuestalMap");			
		Map invoiceItemSeqIdMap = (Map) context.get("invoiceItemSeqIdMap");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		Map <String, BigDecimal> listaMontosPres = FastMap.newInstance();
		boolean cambiaEstatusInvoice = false;
		BigDecimal montoAfectar = BigDecimal.ZERO;
		BigDecimal montoAfectarTotal = BigDecimal.ZERO;		
		BigDecimal montoRestanteTotal = BigDecimal.ZERO;
		Timestamp fechaPago = (Timestamp) context.get("fechaPago");
		String ciclo = (String) context.get("ciclo");
		
		Debug.log("context: " + context);
		try
		{	GenericValue invoice = delegator.findByPrimaryKey("Invoice", UtilMisc.toMap("invoiceId",invoiceId));					
			//Consultar datos de invoice e invoiceItem
			//Afectar el egreso												
			/********************************/
	        /********************************/
	        /*******Motor Contable***********/
	        /********************************/
	        /********************************/
	        Timestamp fechaTrans = new Timestamp(Calendar.getInstance().getTimeInMillis());
	        Timestamp fechaContable = invoice.getTimestamp("invoiceDate");
	        Map<String, Object> input = FastMap.newInstance();
	        input.put("eventoContableId",invoice.getString("acctgTransTypeId"));
	        input.put("tipoClaveId",TipoClaveEgreso);
	        input.put("fechaRegistro",fechaTrans);	        
	        input.put("fechaContable",fechaContable);
	        input.put("currency", context.get("moneda"));
	        input.put("usuario", userLogin.getString("userLoginId"));
	        input.put("organizationId",organizationPartyId);
	        input.put("descripcion", context.get("descripcion"));
	        input.put("roleTypeId", "BILL_FROM_VENDOR");
	        input.put("campo","invoiceId");
	        input.put("valorCampo", invoiceId);			        			        			
	        Debug.log("input: " + input);
	        
	        
	        List<String> orderByInvoiceItem = UtilMisc.toList("invoiceItemSeqId");
	        EntityCondition condicionInvoiceItem = EntityCondition.makeCondition(EntityOperator.AND,
  												   EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, invoiceId));					
	        List<GenericValue> invoiceItemList = delegator.findByCondition("InvoiceItem", condicionInvoiceItem, null, orderByInvoiceItem);
	        if(!invoiceItemList.isEmpty())
	        {	Iterator<GenericValue> invoiceItemIter = invoiceItemList.iterator();
		        Map<String, Object> output = FastMap.newInstance();
		        
		        int numClasAdmin = UtilClavePresupuestal.indiceClasAdmin(TipoClaveEgreso, organizationPartyId, delegator, ciclo);
		        
		        List<LineaMotorContable> listLineaContable = FastList.newInstance();
	        				        
	        	List<GenericValue> lineaPresupuestal = delegator.findByAnd("LineaPresupuestal", UtilMisc.toMap("acctgTransTypeId",invoice.getString("acctgTransTypeId")));
	        	boolean tienePresup = (lineaPresupuestal == null ? false : (lineaPresupuestal.isEmpty() ? false : true ));
	    		boolean productoCargo = false;
	    		GenericValue lineaPresup = null;
	    		if(tienePresup)
	    		{	lineaPresup = lineaPresupuestal.get(0);
	    			if(lineaPresup.getString("cuentaCargo").equals("PRODUCTO"))
	    			{	productoCargo = true;
	    			}
	    		}
	    		int contadorMontoAfectar = 0;
	    		int contador = 1;
	        	while (invoiceItemIter.hasNext()) 
	        	{	GenericValue lineaPresClon = (GenericValue) lineaPresup.clone();	        		
		        	GenericValue invoiceItem = (GenericValue) invoiceItemIter.next();
		        			        	
		        	String clavePresupuestal = "";
		        	for(int i=1; i<16; i++)
		        	{	if(Integer.valueOf(numClasAdmin) == i)
		        		clavePresupuestal = clavePresupuestal+(UtilFinancial.buscaExternalId(invoiceItem.getString("acctgTagEnumIdAdmin"),delegator));
	                	String clasif = (String)invoiceItem.getString("acctgTagEnumId" + i);
	                	if(clasif != null && !clasif.isEmpty())
	                		clavePresupuestal = clavePresupuestal+UtilFinancial.buscaSequenceId(clasif, delegator);
	                	clavePresupuestalMap.put(contador, clavePresupuestal);
	                }		        	
		        	
		        	LineaMotorContable lineaContable = new LineaMotorContable();
	        		
	    			String montoAfectarString = (String) amountMap.get(Integer.toString(contador));
					montoAfectar = new BigDecimal(montoAfectarString);
					//Validar montos
		        	if(montoAfectar.compareTo(invoiceItem.getBigDecimal("montoRestante")) > 0)
		        	{	return ServiceUtil.returnError("El monto a afectar es superior al monto restante");		        		
		        	}
					if(montoAfectar.compareTo(BigDecimal.ZERO) > 0)
					{	cambiaEstatusInvoice = true;
					}
	    			
		        	BigDecimal monto = montoAfectar;
		        	TOTAL_MONTO_PRES_PAGO = TOTAL_MONTO_PRES_PAGO.add(monto);
		        	listaMontosPres.put(Integer.toString(contadorMontoAfectar), montoAfectar);
		        	montoAfectarTotal = montoAfectarTotal.add(montoAfectar);
		        			        	
	        		if(montoAfectar.compareTo(BigDecimal.ZERO) > 0)
		        	{	lineaContable.setClavePresupuestal(UtilClavePresupuestal.getClavePresupuestal(invoiceItem, delegator, UtilClavePresupuestal.EGRESO_TAG, organizationPartyId,ciclo));
		        		lineaContable.setMontoPresupuestal(monto);
		        		lineaContable.setFecha(fechaContable);
		        		lineaContable.setLineasPresupuestales(UtilCommon.getLineaPresupuestal(lineaPresup, invoice.getString("partyIdFrom"), null, invoiceItem.getString("productId") ,"0"));
		        		listLineaContable.add(lineaContable);
		        	}
		        	
		        	
		        	
		        	
		        	//Modificar el registro de InvoiceItem
  					BigDecimal montoRestante = invoiceItem.getBigDecimal("montoRestante");			  					
  					invoiceItem.set("montoRestante", montoRestante.subtract(montoAfectar));
  					montoRestanteTotal = montoRestanteTotal.add(montoRestante.subtract(montoAfectar));
  					invoiceItem.set("invoiceId", invoiceId);
  					invoiceItem.set("invoiceItemSeqId", invoiceItem.getString("invoiceItemSeqId"));			  					
  					delegator.store(invoiceItem);
		        	contador++;
		        	contadorMontoAfectar++;
		        }
	        	if(TOTAL_MONTO_PRES_PAGO.compareTo(BigDecimal.ZERO) == 0)			
				{	throw new GenericEntityException("No se ha ingresado algun monto a pagar");
				}
		        input.put("lineasMotor", listLineaContable);
		        output = dispatcher.runSync("creaTransaccionMotor", input);
		        Debug.log("output: " + output);
				        
			    if(ServiceUtil.isError(output))
			    {	return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
			    }
	        }	        
  			//Generar un registro en tabla OrdenPago
  			GenericValue ordenPago = GenericValue.create(delegator.getModelEntity("OrdenPago"));
  			String ordenPagoId = delegator.getNextSeqId("OrdenPago");			
  			ordenPago.set("ordenPagoId", ordenPagoId);
  			ordenPago.set("invoiceId", invoiceId);
  			ordenPago.set("acctgTransTypeId", acctgTransTypeId);
  			ordenPago.set("statusId", OrdenPagoPendiente);
  			ordenPago.set("fechaPago", fechaPago);
  			delegator.create(ordenPago);  					  			
  			//Si cambia el status del Invoice
  			BigDecimal openAmount = invoice.getBigDecimal("openAmount");  			  			
  			if(cambiaEstatusInvoice && montoRestanteTotal.compareTo(BigDecimal.ZERO) > 0)
  			{	invoice.set("statusId", EstatusEjercidoParcial);
  				invoice.set("openAmount", openAmount.subtract(montoAfectarTotal));
  				delegator.store(invoice);
  			}
  			else if (cambiaEstatusInvoice && montoRestanteTotal.compareTo(BigDecimal.ZERO) == 0)
  			{	invoice.set("statusId", EstatusEjercidoTotal);
  				invoice.set("openAmount", openAmount.subtract(montoAfectarTotal));
				delegator.store(invoice);  				
  			}
  			//Generar un registro en tabla OrdenPagoMulti
  			List<String> orderByLineaCont = UtilMisc.toList("secuencia");
	        EntityCondition condLineaCont = EntityCondition.makeCondition(EntityOperator.AND,
  										EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, acctgTransTypeId));					
	        List<GenericValue> lineaContList = delegator.findByCondition("LineaContable", condLineaCont, null, orderByLineaCont);
	        Iterator<GenericValue> lineaContIter = null;
	        	       

			for(int i=0; i<invoiceItemSeqIdMap.size(); i++)
			{	GenericValue ordenPagoMulti = GenericValue.create(delegator.getModelEntity("OrdenPagoMulti"));  					
				ordenPagoMulti.set("ordenPagoId", ordenPagoId);
	  			ordenPagoMulti.set("invoiceId", invoiceId);
	  			ordenPagoMulti.set("invoiceItemSeqId", invoiceItemSeqIdMap.get(Integer.toString(i+1)));	  				  				  						
				if(!lineaContList.isEmpty())
		        {	lineaContIter = lineaContList.iterator();
			        while (lineaContIter.hasNext())
					{	GenericValue lineaContItem = (GenericValue) lineaContIter.next();
						Debug.log("lineaContItem: " + lineaContItem);
						ordenPagoMulti.set("idCatalogoAbono", null);
						ordenPagoMulti.set("idCatalogoCargo", null);
						String descripcion = lineaContItem.getString("descripcion");
						String stringMapas = descripcion+String.valueOf(i);						
						stringMapas = stringMapas.replaceAll(" ", "");						
						String catalogoCargo = (String) catalogoCargoContMap.get(stringMapas);
						String catalogoAbono = (String) catalogoAbonoContMap.get(stringMapas);						
						String montoContString = (String) montoMap.get(stringMapas);						
						BigDecimal montoCont = BigDecimal.ZERO;
						if(montoContString != null && !montoContString.equals(""))
						{	montoCont = new BigDecimal(montoContString);							
						}												
						if(catalogoCargo != null && !catalogoCargo.equals(""))
						{	if(lineaContItem.getString("catalogoAbono") != null && lineaContItem.getString("catalogoAbono").equalsIgnoreCase(OrdenPagoBanco) )
							{	ordenPagoMulti.set("idCatalogoAbono", OrdenPagoBanco);
								ordenPagoMulti.set("idCatalogoCargo", catalogoCargo);
							}
							else
							{	ordenPagoMulti.set("idCatalogoAbono", catalogoAbono);
								ordenPagoMulti.set("idCatalogoCargo", catalogoCargo);
							}
							Debug.log("lineaContItem.getString(catalogoCargo): " + lineaContItem.getString("catalogoCargo"));
							if(lineaContItem.getString("catalogoCargo") != null)
							{	if(lineaContItem.getString("catalogoCargo").equalsIgnoreCase(OrdenPagoBanco))
								{	ordenPagoMulti.set("idCatalogoCargo", OrdenPagoBanco);								
								}
							}
							ordenPagoMulti.set("secuenciaLineaContable", lineaContItem.getString("secuencia"));						
							ordenPagoMulti.set("monto", montoCont);
							ordenPagoMulti.set("montoRestante", montoCont);
							delegator.create(ordenPagoMulti);
						}
						else if(montoCont.compareTo(BigDecimal.ZERO) > 0)
						{	Debug.log("catalogoAbono: " + catalogoAbono);
							if(catalogoAbono != null && !catalogoAbono.equals(""))
							{	ordenPagoMulti.set("idCatalogoAbono", catalogoAbono);						
							}
							if(lineaContItem.getString("catalogoAbono") != null && lineaContItem.getString("catalogoAbono").equalsIgnoreCase(OrdenPagoBanco) )
							{	ordenPagoMulti.set("idCatalogoAbono", OrdenPagoBanco);
								ordenPagoMulti.set("idCatalogoCargo", catalogoCargo);
							}
							if(lineaContItem.getString("catalogoCargo") != null)								
							{	if(lineaContItem.getString("catalogoCargo").equalsIgnoreCase(OrdenPagoBanco))
								{	ordenPagoMulti.set("idCatalogoCargo", OrdenPagoBanco);								
								}
							}														
							ordenPagoMulti.set("secuenciaLineaContable", lineaContItem.getString("secuencia"));							
							ordenPagoMulti.set("monto", montoCont);
							ordenPagoMulti.set("montoRestante", montoCont);
							delegator.create(ordenPagoMulti);
						}
					}
		        }
				ordenPagoMulti.set("idCatalogoAbono", null);									
				ordenPagoMulti.set("secuenciaLineaContable", "0");				
				ordenPagoMulti.set("idCatalogoCargo", null);							
				ordenPagoMulti.set("monto", listaMontosPres.get(Integer.toString(i)));
				ordenPagoMulti.set("montoRestante", listaMontosPres.get(Integer.toString(i)));
				ordenPagoMulti.set("idCatalogoPres", OrdenPagoPresupuesto);				
				delegator.create(ordenPagoMulti);										    					
			}				  			  			  			
		}		
		catch (GenericEntityException e) 
		{	return ServiceUtil.returnError(e.getMessage());
		} 
		catch (GenericServiceException e) 
		{	return ServiceUtil.returnError(e.getMessage());
		}														
		return ServiceUtil.returnSuccess("La Orden de pago ha sido creada con \u00e9xito");
	}			   
	
	/**
	 * Metodo que crea el pago aplicado para una orden de pago. Esta accion la hace el tesorero
	 * Se utilizan las tablas de payment  
	 */
	@SuppressWarnings({ "unchecked", "null" })
	public static Map enviarOrdenPagado(DispatchContext dctx, Map context)
			throws ParseException, GeneralException {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();		
		Debug.log("context: " + context);		
		String ordenPagoId = (String) context.get("ordenPagoId");
		String invoiceId = (String) context.get("invoiceId");
		String acctgTransTypeId = (String) context.get("acctgTransTypeId");
		String organizationPartyId = (String) context.get("organizationPartyId");
		String comentario = (String) context.get("comentario");
		String paymentTypeId = (String) context.get("paymentTypeId");
		String bancoId = (String) context.get("bancoId");
		String cuentaBancariaId = (String) context.get("cuentaBancariaId");
		String paymentRefNum = (String) context.get("paymentRefNum");		
		BigDecimal montoPago = (BigDecimal) context.get("montoPago");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		Timestamp fechaTrans = new Timestamp(Calendar.getInstance().getTimeInMillis());		
		GenericValue userLogin = (GenericValue) context.get("userLogin");	
		boolean tieneMatriz = false;
		String ciclo = (String) context.get("ciclo");
		
		if(1==1)
		{	return ServiceUtil.returnSuccess("Regresa :)");
		}
		
        Map<String, Object> input = FastMap.newInstance();
        Map<String, Object> output = FastMap.newInstance();
        Map<String, Object> montoMap = FastMap.newInstance();
        Map<String, Object> mapaMontoClave = FastMap.newInstance();
        Map<String, Object> clavePresupuestalMap = FastMap.newInstance();
        Map<String, Object> productIds = FastMap.newInstance();
        Map<String, Object> catalogoCargoContMap = FastMap.newInstance();
        Map<String, Object> catalogoAbonoContMap = FastMap.newInstance();
        Map<String, Object> catalogoCargoPresMap = FastMap.newInstance();
        Map<String, Object> catalogoAbonoPresMap = FastMap.newInstance();

        
		if (bancoId == null || bancoId.trim().equals("")) 
		{	return ServiceUtil.returnError("El p\u00E1rametro Banco no puede estar vacio");
		}
		if (cuentaBancariaId == null || cuentaBancariaId.trim().equals(""))
		{	return ServiceUtil.returnError("El p\u00E1rametro Cuenta bancaria no puede estar vacio");
		}
		if (comentario == null || comentario.trim().equals(""))
		{	return ServiceUtil.returnError("El p\u00E1rametro Comentario no puede estar vacio");
		}
		if (fechaContable == null || fechaContable.equals(""))
		{	return ServiceUtil.returnError("El p\u00E1rametro Fecha Contable no puede estar vacio");
		}		
		
		
		GenericValue invoice = delegator.findByPrimaryKey("Invoice", UtilMisc.toMap("invoiceId",invoiceId));			
		
		
		//Generar un registro en la tabla Payment
		GenericValue payment = GenericValue.create(delegator.getModelEntity("Payment"));
		String paymentId = delegator.getNextSeqId("Payment");			
		payment.set("paymentId", paymentId);
		payment.set("paymentTypeId", paymentTypeId);
		payment.set("partyIdFrom", invoice.getString("partyId"));
		payment.set("partyIdTo", invoice.getString("partyIdFrom"));
		payment.set("statusId", "PMNT_SENT");
		payment.set("effectiveDate", fechaContable);
		payment.set("paymentRefNum", paymentRefNum);
		payment.set("amount", montoPago);
		payment.set("currencyUomId", invoice.getString("currencyUomId"));
		payment.set("comments", comentario);
		payment.set("acctgTransTypeId", acctgTransTypeId);
		payment.set("bancoId", bancoId);
		payment.set("cuentaBancariaId", cuentaBancariaId);		
		payment.set("appliedAmount", montoPago);
		payment.set("openAmount", BigDecimal.ZERO);		 
		delegator.create(payment);  	
				
		//VALIDACION DE MATRIZ A.2
		EntityCondition condicionMatriz = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, acctgTransTypeId),
				EntityCondition.makeCondition("tipoMatrizId", EntityOperator.EQUALS, "A.2"));					
		List<GenericValue> LineaMatrizList = delegator.findByCondition("LineaPresupuestal", condicionMatriz, null, null);

		
		//Generar registros en la tabla PaymentApplication obteniendo el monto correspondiente a cada auxiliar de bancos
		List<String> orderBySumaMontoPaymentApp = UtilMisc.toList("invoiceItemSeqId");
		EntityCondition condicionSumaMontoPaymentApp = null;		
		if(!LineaMatrizList.isEmpty())
		{	condicionSumaMontoPaymentApp = EntityCondition.makeCondition(EntityOperator.AND,
														   EntityCondition.makeCondition("ordenPagoId", EntityOperator.EQUALS, ordenPagoId),
														   EntityCondition.makeCondition(UtilMisc.toList(
													       EntityCondition.makeCondition("idCatalogoPres", EntityOperator.EQUALS, OrdenPagoPresupuesto),
													       EntityCondition.makeCondition("idCatalogoCargo", EntityOperator.EQUALS, OrdenPagoBanco), 
													       EntityCondition.makeCondition("idCatalogoAbono", EntityOperator.EQUALS, OrdenPagoBanco)),
													       EntityOperator.OR));
		}
		else
		{	condicionSumaMontoPaymentApp = EntityCondition.makeCondition(EntityOperator.AND,
													   EntityCondition.makeCondition("ordenPagoId", EntityOperator.EQUALS, ordenPagoId),
													   EntityCondition.makeCondition(UtilMisc.toList(
												       EntityCondition.makeCondition("idCatalogoCargo", EntityOperator.EQUALS, OrdenPagoBanco), 
												       EntityCondition.makeCondition("idCatalogoAbono", EntityOperator.EQUALS, OrdenPagoBanco)),
												       EntityOperator.OR));					
		}
		List<GenericValue> sumaSumaMontoPaymentApp = delegator.findByCondition("ObtenerMontoPaymentApplication", condicionSumaMontoPaymentApp, UtilMisc.toList("monto", "ordenPagoId", "invoiceItemSeqId"), orderBySumaMontoPaymentApp);
		Debug.log("sumaSumaMontoPaymentApp: " + sumaSumaMontoPaymentApp);
		if(!sumaSumaMontoPaymentApp.isEmpty())
        {	Iterator<GenericValue> sumaSumaMontoPaymentAppIter = sumaSumaMontoPaymentApp.iterator();
        	Debug.log("sumaSumaMontoPaymentAppIter: " + sumaSumaMontoPaymentAppIter);
        	while (sumaSumaMontoPaymentAppIter.hasNext()) 
        	{	GenericValue item = sumaSumaMontoPaymentAppIter.next();
	    		GenericValue paymentApplication = GenericValue.create(delegator.getModelEntity("PaymentApplication"));
	    		String paymentApplicationId = delegator.getNextSeqId("PaymentApplication");
	    		paymentApplication.set("amountApplied",item.getBigDecimal("monto"));
	    		paymentApplication.set("paymentApplicationId", paymentApplicationId);
	    		paymentApplication.set("paymentId", paymentId);
	    		paymentApplication.set("invoiceId", invoiceId);
	    		paymentApplication.set("invoiceItemSeqId", item.getString("invoiceItemSeqId"));
	    		paymentApplication.set("ordenPagoId", ordenPagoId);
	    		Debug.log("paymentApplication: " + paymentApplication);
	    		delegator.create(paymentApplication);
        	}
        }
		
		
		
		
		/*List<String> orderBySumaBancosCargo = UtilMisc.toList("invoiceItemSeqId");
		EntityCondition condicionSumaBancosCargo = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("ordenPagoId", EntityOperator.EQUALS, ordenPagoId),
				EntityCondition.makeCondition("idCatalogoCargo", EntityOperator.EQUALS, OrdenPagoBanco));
		List<GenericValue> sumaBancosCargo = delegator.findByCondition("OrdenPagoMulti", condicionSumaBancosCargo, UtilMisc.toList("monto"), orderBySumaBancosCargo);
		
		List<String> orderBySumaBancosAbono = UtilMisc.toList("invoiceItemSeqId");
		EntityCondition condicionSumaBancosAbono = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("ordenPagoId", EntityOperator.EQUALS, ordenPagoId),
				EntityCondition.makeCondition("idCatalogoAbono", EntityOperator.EQUALS, OrdenPagoBanco));
		List<GenericValue> sumaBancosAbono = delegator.findByCondition("OrdenPagoMulti", condicionSumaBancosAbono, UtilMisc.toList("monto"), orderBySumaBancosAbono);*/
				
		/*if(!sumaBancosCargo.isEmpty())
        {	Iterator<GenericValue> sumaBancosCargoIter = sumaBancosCargo.iterator();	        
        	while (sumaBancosCargoIter.hasNext()) 
        	{	GenericValue generic = sumaBancosCargoIter.next();
	    		GenericValue paymentApplication = GenericValue.create(delegator.getModelEntity("PaymentApplication"));
	    		String paymentApplicationId = delegator.getNextSeqId("PaymentApplication");
	    		paymentApplication.set("amountApplied",generic.getBigDecimal("monto"));
	    		paymentApplication.set("paymentApplicationId", paymentApplicationId);
	    		paymentApplication.set("paymentId", paymentId);
	    		paymentApplication.set("invoiceId", invoiceId);
	    		paymentApplication.set("ordenPagoId", ordenPagoId);	    									
	    		delegator.create(paymentApplication);
        	}
        }
		else if(!sumaBancosAbono.isEmpty())
        {	Iterator<GenericValue> sumaBancosAbonoIter = sumaBancosAbono.iterator();	        
        	while (sumaBancosAbonoIter.hasNext()) 
        	{	GenericValue generic = sumaBancosAbonoIter.next();        	
	    		GenericValue paymentApplication = GenericValue.create(delegator.getModelEntity("PaymentApplication"));
	    		String paymentApplicationId = delegator.getNextSeqId("PaymentApplication");
	    		paymentApplication.set("amountApplied",generic.getBigDecimal("monto"));
	    		paymentApplication.set("paymentApplicationId", paymentApplicationId);
	    		paymentApplication.set("paymentId", paymentId);
	    		paymentApplication.set("invoiceId", invoiceId);
	    		paymentApplication.set("ordenPagoId", ordenPagoId);	    									
	    		delegator.create(paymentApplication);
        	}
        }*/
						
		
		
		
        if(!LineaMatrizList.isEmpty())
        {		tieneMatriz = true;
	        	//Consultar los registros presupuestales en la orden de pago
	        	List<String> orderByOrdenPagoPres = UtilMisc.toList("invoiceItemSeqId");
	            EntityCondition condOrdenPagoPres = EntityCondition.makeCondition(EntityOperator.AND,
	    												EntityCondition.makeCondition("idCatalogoPres", EntityOperator.EQUALS, OrdenPagoPresupuesto),
	    												EntityCondition.makeCondition("ordenPagoId", EntityOperator.EQUALS, ordenPagoId));					
	            List<GenericValue> ordenPagoPresList = delegator.findByCondition("OrdenPagoMulti", condOrdenPagoPres, UtilMisc.toList("monto"), orderByOrdenPagoPres);
	            Debug.log("condOrdenPagoPres: " + condOrdenPagoPres);
	            Debug.log("ordenPagoPresList: " + ordenPagoPresList);
	            if(!ordenPagoPresList.isEmpty())
	            {	if(ordenPagoPresList.size() > 1)
	            	{	Iterator<GenericValue> ordenPagoPresIter = ordenPagoPresList.iterator();
	            		int contador = 0;
		        		while (ordenPagoPresIter.hasNext())
		        		{	GenericValue item = ordenPagoPresIter.next();
			        		catalogoCargoPresMap.put(Integer.toString(contador), invoice.getString("partyIdFrom"));
				        	catalogoAbonoPresMap.put(Integer.toString(contador), cuentaBancariaId);
		        			mapaMontoClave.put(Integer.toString(contador), item.getBigDecimal("monto").toString());
		        			contador++;
		        		}		            		
	            	}	            	
	            	else
	            	{	Iterator<GenericValue> ordenPagoPresIter = ordenPagoPresList.iterator();
            			while (ordenPagoPresIter.hasNext())
		        		{	GenericValue item = ordenPagoPresIter.next();
			        		mapaMontoClave.put("0", item.getBigDecimal("monto").toString());	            		
		            	}
	            	}
	            } 
	            clavePresupuestalMap = llenaMapClavePresupuestalMap(dctx, context, delegator, invoiceId, TipoClaveEgreso,organizationPartyId,ciclo);        	
        }
        else
        {	//Consultar los registros presupuestales en la orden de pago
        	List<String> orderByOrdenPagoPres = UtilMisc.toList("invoiceItemSeqId");
            EntityCondition condOrdenPagoPres = EntityCondition.makeCondition(EntityOperator.AND,
    												EntityCondition.makeCondition("idCatalogoPres", EntityOperator.EQUALS, OrdenPagoPresupuesto),
    												EntityCondition.makeCondition("ordenPagoId", EntityOperator.EQUALS, ordenPagoId));					
            List<GenericValue> ordenPagoPresList = delegator.findByCondition("OrdenPagoMulti", condOrdenPagoPres, UtilMisc.toList("monto"), orderByOrdenPagoPres);            
            if(!ordenPagoPresList.isEmpty())
            {	Iterator<GenericValue> ordenPagoPresIter = ordenPagoPresList.iterator();
        		int contador = 0;
        		while (ordenPagoPresIter.hasNext())
        		{	GenericValue item = ordenPagoPresIter.next();	        		
        			mapaMontoClave.put(Integer.toString(contador), item.getBigDecimal("monto").toString());
        			contador++;
        		}
            }
        }
        
        	//Consultar las lineas de la orden de pago y comparar con el flag de linea contable
    		//para obtener los montos presupuestales que sumen y menar el mapa montoPago
            List<String> orderByMontosPresu = UtilMisc.toList("invoiceItemSeqId");
            EntityCondition condicionMontosPresu = EntityCondition.makeCondition(EntityOperator.AND,
    											EntityCondition.makeCondition("ordenPagoId", EntityOperator.EQUALS, ordenPagoId),
    											EntityCondition.makeCondition("idCatalogoPres", EntityOperator.EQUALS, null));                    
            List<GenericValue> montosPresupuList = delegator.findByCondition("ConsultaLineasContablesOrdenPago", condicionMontosPresu, null, orderByMontosPresu);
            Debug.log("condicionMontosPresu: " + condicionMontosPresu);
            Debug.log("montosPresupuList: " + montosPresupuList);
            if(!montosPresupuList.isEmpty())
            {	String secuenciaAux = null;
            	int contador = 0;
            	int contadorMapa = 0;
            	BigDecimal sumaMontoPres = BigDecimal.ZERO;
            	Iterator<GenericValue> montosPresupuIter = montosPresupuList.iterator();	        
            	while (montosPresupuIter.hasNext()) 
            	{	GenericValue item = montosPresupuIter.next();
            		Debug.log("item: " + item);
            		String secuencia = item.getString("invoiceItemSeqId");
            		Debug.log("secuencia: " + secuencia);
            		Debug.log("secuenciaAux: " + secuenciaAux);
            		/*if(secuenciaAux == null)
            		{	Debug.log("If secuencia es null");
            			secuenciaAux = secuencia;
            			Debug.log("secuenciaAuxIf: " + secuenciaAux);
            			if(item.getString("excepcion") == null)
            			{	Debug.log("if Excepcion es null");
            				sumaMontoPres = sumaMontoPres.add(item.getBigDecimal("monto"));
            				Debug.log("sumaMontoPres: " + sumaMontoPres);
            			}            			
            			contador++;            			
            		}
            		else if(!secuenciaAux.equals(secuencia))
            		{	Debug.log("else If secuenciaAux es igual a secuencia");            			
            			secuenciaAux = secuencia;
            			Debug.log("secuenciaAuxElse: " + secuenciaAux);
            			if(!tieneMatriz)
            			{	Debug.log("No tiene matriz");
            				mapaMontoClave.put(Integer.toString(contadorMapa), sumaMontoPres.toString());
            				Debug.log("mapaMontoClave: " + mapaMontoClave);
            				Debug.log("sumaMontoPresElse: " + sumaMontoPres);
            			}
            			contadorMapa++;
            			Debug.log("contadorMapa: " + contadorMapa);
            			sumaMontoPres = BigDecimal.ZERO;
            			Debug.log("Inicializa sumaMontoPres");
            			if(item.getString("excepcion") == null)
            			{	Debug.log("If excepcion es null");
            				sumaMontoPres = sumaMontoPres.add(item.getBigDecimal("monto"));
            				Debug.log("sumaMontoPresIfItem: " + sumaMontoPres);
            			}            			
            			contador++;        			
            		}
            		else
            		{	Debug.log("else");
            			if(item.getString("excepcion") == null)
            			{	Debug.log("else item excepcion es null");
            				sumaMontoPres = sumaMontoPres.add(item.getBigDecimal("monto"));
            				Debug.log("sumaMontoPresElse: " + sumaMontoPres);
            			}            			
            			contador++;
            		}*/
            		String descripcion = item.getString("descripcion");            		
    				descripcion = descripcion.replaceAll(" ", "");
    				Debug.log("descripcion: " + descripcion);
					if(item.getString("idCatalogoCargo") != null && !item.getString("idCatalogoCargo").equalsIgnoreCase(""))
        			{	Debug.log("idCatalogoCargo no es null ni vacio");
						if(item.getString("idCatalogoCargo").equalsIgnoreCase(OrdenPagoBanco))
						{	catalogoCargoContMap.put(descripcion+contadorMapa, cuentaBancariaId);
							Debug.log("Llena catalogoCargoContMap: " + catalogoCargoContMap);
						}						
    					else
    					{	catalogoCargoContMap.put(descripcion+contadorMapa, item.getString("idCatalogoCargo"));
    						Debug.log("Llena catalogoCargoContMap2: " + catalogoCargoContMap);
    					}    					        				    				
        			}
            		if(item.getString("idCatalogoAbono") != null && !item.getString("idCatalogoAbono").equalsIgnoreCase(""))
        			{	Debug.log("idCatalogoAbono no es null ni vacio");
            			if(item.getString("idCatalogoAbono").equalsIgnoreCase(OrdenPagoBanco))
            			{	catalogoAbonoContMap.put(descripcion+contadorMapa, cuentaBancariaId);
            				Debug.log("Llena catalogoAbonoContMap: " + catalogoAbonoContMap);
            			}
    					else
    					{	catalogoAbonoContMap.put(descripcion+contadorMapa, item.getString("idCatalogoAbono"));
    						Debug.log("Llena catalogoAbonoContMap2: " + catalogoAbonoContMap);
    					}
        				    				
        			}
            		Debug.log("Llena monto mapa");
            		montoMap.put(descripcion+contadorMapa, item.getString("monto"));
            	}
            	
            	//
            	/*if(sumaMontoPres.compareTo(BigDecimal.ZERO) > 0)
            	{	Debug.log("sumaMontoPres es mayor a 0: " + sumaMontoPres);
            		if(!tieneMatriz)
            		{	Debug.log("No tiene matriz final");
            			mapaMontoClave.put(Integer.toString(contadorMapa), sumaMontoPres.toString());
            			Debug.log("mapaMontoClaveFinal: " + mapaMontoClave);
            		}            		
            		contadorMapa++;
            	} */       	
            }
            if(!tieneMatriz)
            {	clavePresupuestalMap = llenaMapClavePresupuestalMap(dctx, context, delegator, invoiceId, TipoClaveEgreso,organizationPartyId,ciclo);            
            }
        
        
        
        Debug.log("clavePresupuestalMap: " + clavePresupuestalMap);
        Debug.log("catalogoCargoPresMap: " + catalogoCargoPresMap);
        Debug.log("catalogoAbonoPresMap: " + catalogoAbonoPresMap);
        Debug.log("mapaMontoClave: " + mapaMontoClave);        							
        Debug.log("catalogoCargoContMap: " + catalogoCargoContMap);
        Debug.log("catalogoAbonoContMap: " + catalogoAbonoContMap);
        Debug.log("montoMap: " + montoMap);                       		               
        context.put("clavePresupuestalMap", clavePresupuestalMap);
        context.put("catalogoCargoContMap", catalogoCargoContMap);
        context.put("catalogoAbonoContMap", catalogoAbonoContMap);
        context.put("catalogoCargoPresMap", catalogoCargoPresMap);
        context.put("catalogoAbonoPresMap", catalogoAbonoPresMap);
        context.put("mapaMontoClave", mapaMontoClave);
        context.put("montoMap", montoMap);
                
		/*****MOTOR CONTABLE*****/
        input.put("eventoContableId", acctgTransTypeId);
    	input.put("tipoClaveId", TipoClaveEgreso);
    	input.put("fechaRegistro", fechaTrans);
    	input.put("fechaContable", fechaContable);
    	input.put("currency", invoice.getString("currencyUomId"));
    	input.put("usuario", userLogin.getString("userLoginId"));
    	input.put("organizationId", organizationPartyId);
    	input.put("descripcion", comentario);
    	input.put("roleTypeId", "BILL_FROM_VENDOR");
    	input.put("campo", "paymentId");
    	input.put("valorCampo", paymentId);
    	input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContablesPagaReca(acctgTransTypeId, delegator, context, invoice.getString("partyIdFrom"),cuentaBancariaId, mapaMontoClave, productIds));
		
		 
		
    	
    	Debug.log("input: " + input);
    	output = dispatcher.runSync("creaTransaccionMotor", input);
    	Debug.log("output: " + output);
    	if(ServiceUtil.isError(output)){
    		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
    	}
    	
    	//Actualiza el status de la orden de pago
		GenericValue ordenPago = delegator.findByPrimaryKey("OrdenPago", UtilMisc.toMap("ordenPagoId",ordenPagoId));						
		ordenPago.set("statusId", OrdenPagoPagada);
		ordenPago.set("paymentId", paymentId);
		delegator.store(ordenPago);  										
		
		return ServiceUtil.returnSuccess("La Orden de pago ha sido pagada con \u00e9xito");
	}
	
	/**
	 * Metodo que regresa el mapa lleno de las claves presupuestales que hay en el invoice
	 * Se utilizan las tablas de payment  
	 * @throws GenericServiceException 
	 */
	@SuppressWarnings({ "unchecked", "null" })
	public static Map llenaMapClavePresupuestalMap(DispatchContext dctx, Map context, Delegator delegator, String invoiceId, String tipoClave,String organizationPartyId, String ciclo) throws GenericServiceException
	{	LocalDispatcher dispatcher = dctx.getDispatcher();
		Map<String, Object> clavePresMap = FastMap.newInstance();
		try
		{	//Consultar las claves presupuestarias para llenar el map clavePresupuestalMap
	        List<String> orderByInvoiceItem = UtilMisc.toList("invoiceItemSeqId");
	        EntityCondition condicionInvoiceItem = EntityCondition.makeCondition(EntityOperator.AND,
													EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, invoiceId));					
	        List<GenericValue> invoiceItemList = delegator.findByCondition("InvoiceItem", condicionInvoiceItem, null, orderByInvoiceItem);
	        if(!invoiceItemList.isEmpty())
	        {	Iterator<GenericValue> invoiceItemIter = invoiceItemList.iterator();
	        	int contador = 0;
	        	while (invoiceItemIter.hasNext()) 
	        	{	GenericValue invoiceItem = invoiceItemIter.next();
		            int indiceAdmin = UtilClavePresupuestal.indiceClasAdmin(tipoClave, organizationPartyId, delegator, ciclo);
		            String clavePresupuestal = "";
		            for(int i=1; i<16; i++)
		            {	if(indiceAdmin == i)
		            		clavePresupuestal = clavePresupuestal.concat(UtilFinancial.buscaExternalId(invoiceItem.getString("acctgTagEnumIdAdmin"),delegator));
		            	String clasif = (String)invoiceItem.getString("acctgTagEnumId" + i);
		            	if(clasif != null && !clasif.isEmpty())
		            		clavePresupuestal = clavePresupuestal.concat(UtilFinancial.buscaSequenceId(clasif, delegator));
		            }
		            clavePresMap.put(Integer.toString(contador), clavePresupuestal);		          
		            contador++;
	        	}
	        }
		}
		catch (GenericEntityException e) 
		{	return ServiceUtil.returnError(e.getMessage());
		}
		return clavePresMap;		
	}
	
	/**
	 * Metodo que regresa el string de la clave presupuestal que hay en el invoiceItemSeqId
	 * Se utilizan las tablas de payment  
	 * @throws GenericServiceException 
	 * @throws GenericEntityException 
	 */
	@SuppressWarnings({ "unchecked" })
	public static String obtieneClavePresupuestal(DispatchContext dctx, Map context, Delegator delegator, String invoiceId, String invoiceItemSeqId, String tipoClave,String organizationPartyId, String ciclo) throws GenericServiceException, GenericEntityException
	{	LocalDispatcher dispatcher = dctx.getDispatcher();
		String clavePresupuestal = "";
		List<String> fieldsToSelect = UtilMisc.toList("acctgTagEnumId1", "acctgTagEnumId2", "acctgTagEnumId3", "acctgTagEnumId4", "acctgTagEnumId5", 
													  "acctgTagEnumId6", "acctgTagEnumId7", "acctgTagEnumId8", "acctgTagEnumId9", "acctgTagEnumId10", 
													  "acctgTagEnumId11", "acctgTagEnumId12", "acctgTagEnumId13", "acctgTagEnumId14", "acctgTagEnumId15", "acctgTagEnumIdAdmin");
		//Consultar las claves presupuestarias para llenar el map clavePresupuestalMap
        EntityCondition condicionInvoiceItem = EntityCondition.makeCondition(EntityOperator.AND,
												EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, invoiceId),
												EntityCondition.makeCondition("invoiceItemSeqId", EntityOperator.EQUALS, invoiceItemSeqId));					
        List<GenericValue> invoiceItemList = delegator.findByCondition("InvoiceItem", condicionInvoiceItem, fieldsToSelect, null);        
        Debug.logInfo("invoiceItemList: " + invoiceItemList, MODULE);
        if(!invoiceItemList.isEmpty())
        {	Iterator<GenericValue> invoiceItemIter = invoiceItemList.iterator();
        	while (invoiceItemIter.hasNext()) 
        	{	GenericValue invoiceItem = invoiceItemIter.next();
        		int indiceAdmin = UtilClavePresupuestal.indiceClasAdmin(tipoClave, organizationPartyId, delegator, ciclo);
	            for(int i=1; i<16; i++)
	            {	if(indiceAdmin == i)
	            	{	clavePresupuestal = clavePresupuestal.concat(UtilFinancial.buscaExternalId(invoiceItem.getString("acctgTagEnumIdAdmin"),delegator));
	            	}		            	
	            	String clasif = (String)invoiceItem.getString("acctgTagEnumId" + i);
	            	if(clasif != null && !clasif.isEmpty())
	            	{	clavePresupuestal = clavePresupuestal.concat(UtilFinancial.buscaSequenceId(clasif, delegator));
	            	}		            	
	            }		          
        	}
        }			
		return clavePresupuestal;		
	}
	
	
	/**
	 * Metodo que crea la nueva orden de cobro para el tesorero.
	 * Con el detalle de los cargos, abonos y auxiliares configurados en el documento de cobro  
	 */
	@SuppressWarnings({ "unchecked", "null" })
	public static Map enviarOrdenIngresosDevengo(DispatchContext dctx, Map context)
			throws ParseException, GeneralException {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		String invoiceId = (String) context.get("invoiceId");
		String organizationPartyId = (String) context.get("organizationPartyId");
		String acctgTransTypeId = (String) context.get("acctgTransTypeId"); //Evento de cobro
		Map amountMap = (Map) context.get("amountMap"); 
		Map montoMap = (Map) context.get("montoMap");	
		Map catalogoCargoContMap = (Map) context.get("catalogoCargoContMap"); 
		Map catalogoAbonoContMap = (Map) context.get("catalogoAbonoContMap"); 		
		Map clavePresupuestalMap = (Map) context.get("clavePresupuestalMap");			
		Map invoiceItemSeqIdMap = (Map) context.get("invoiceItemSeqIdMap");		
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		Map <String, BigDecimal> listaMontosPres = FastMap.newInstance();
		boolean cambiaEstatusInvoice = false;
		BigDecimal montoAfectar = BigDecimal.ZERO;
		BigDecimal montoAfectarTotal = BigDecimal.ZERO;		
		BigDecimal montoRestanteTotal = BigDecimal.ZERO;
		Timestamp fechaCobro = (Timestamp) context.get("fechaCobro");
		String ciclo = (String) context.get("ciclo");
		Debug.log("context: " + context);
		try
		{	GenericValue invoice = delegator.findByPrimaryKey("Invoice", UtilMisc.toMap("invoiceId",invoiceId));					
			//Consultar datos de invoice e invoiceItem											
			/********************************/
	        /********************************/
	        /*******Motor Contable***********/
	        /********************************/
	        /********************************/
	        Timestamp fechaTrans = new Timestamp(Calendar.getInstance().getTimeInMillis());
	        Timestamp fechaContable = invoice.getTimestamp("invoiceDate");
	        Map<String, Object> input = FastMap.newInstance();
	        input.put("eventoContableId",invoice.getString("acctgTransTypeId"));
	        input.put("tipoClaveId",TipoClaveIngreso);
	        input.put("fechaRegistro",fechaTrans);	        
	        input.put("fechaContable",fechaContable);
	        input.put("currency", context.get("moneda"));
	        input.put("usuario", userLogin.getString("userLoginId"));
	        input.put("organizationId",organizationPartyId);
	        input.put("descripcion", context.get("descripcion"));
	        input.put("roleTypeId", "CUENTAS_C");
	        input.put("campo","invoiceId");
	        input.put("valorCampo", invoiceId);	
	        
	        Debug.log("input: " + input);
	        
	        List<String> orderByInvoiceItem = UtilMisc.toList("invoiceItemSeqId");
	        EntityCondition condicionInvoiceItem = EntityCondition.makeCondition(EntityOperator.AND,
  												   EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, invoiceId));					
	        List<GenericValue> invoiceItemList = delegator.findByCondition("InvoiceItem", condicionInvoiceItem, null, orderByInvoiceItem);
	        if(!invoiceItemList.isEmpty())
	        {	Iterator<GenericValue> invoiceItemIter = invoiceItemList.iterator();
		        Map<String, Object> output = FastMap.newInstance();
		        
		        int indiceAdmin = UtilClavePresupuestal.indiceClasAdmin(TipoClaveIngreso, organizationPartyId, delegator, ciclo);
		        List<LineaMotorContable> listLineaContable = FastList.newInstance();
	        				        
	        	List<GenericValue> lineaPresupuestal = delegator.findByAnd("LineaPresupuestal", UtilMisc.toMap("acctgTransTypeId",invoice.getString("acctgTransTypeId")));
	        	boolean tienePresup = (lineaPresupuestal == null ? false : (lineaPresupuestal.isEmpty() ? false : true ));
	    		boolean productoCargo = false;
	    		GenericValue lineaPresup = null;
	    		if(tienePresup)
	    		{	lineaPresup = lineaPresupuestal.get(0);
	    			if(lineaPresup.getString("cuentaCargo").equals("PRODUCTO"))
	    			{	productoCargo = true;
	    			}
	    		}
	    		int contadorMontoAfectar = 0;
	    		int contador = 1;
	    		List<GenericValue> lineasContables = delegator.findByAnd("LineaContable", "acctgTransTypeId", invoice.getString("acctgTransTypeId"));
	        	while (invoiceItemIter.hasNext()) 
	        	{	Debug.log("While invoiceItemIter: " + invoiceItemIter);
	        		GenericValue lineaPresClon = (GenericValue) lineaPresup.clone();	        		
		        	GenericValue invoiceItem = (GenericValue) invoiceItemIter.next();
		        			        	
		        	String clavePresupuestal = "";
		        	for(int i=1; i<16; i++)
		        	{	if(indiceAdmin == i)
		        		clavePresupuestal = clavePresupuestal+(UtilFinancial.buscaExternalId(invoiceItem.getString("acctgTagEnumIdAdmin"),delegator));
	                	String clasif = (String)invoiceItem.getString("acctgTagEnumId" + i);
	                	if(clasif != null && !clasif.isEmpty())
	                		clavePresupuestal = clavePresupuestal+UtilFinancial.buscaSequenceId(clasif, delegator);
	                	clavePresupuestalMap.put(contador, clavePresupuestal);
	                }		        	
		        	
		        	LineaMotorContable lineaContable = new LineaMotorContable();
	        		
	    			String montoAfectarString = (String) amountMap.get(Integer.toString(contador));
					montoAfectar = new BigDecimal(montoAfectarString);
					//Validar montos
		        	if(montoAfectar.compareTo(invoiceItem.getBigDecimal("montoRestante")) > 0)
		        	{	return ServiceUtil.returnError("El monto a afectar es superior al monto restante");		        		
		        	}
					if(montoAfectar.compareTo(BigDecimal.ZERO) > 0)
					{	cambiaEstatusInvoice = true;
					}
	    			
		        	BigDecimal monto = montoAfectar;
		        	TOTAL_MONTO_PRES_COBRO = TOTAL_MONTO_PRES_COBRO.add(monto);
		        	listaMontosPres.put(Integer.toString(contadorMontoAfectar), montoAfectar);
		        	montoAfectarTotal = montoAfectarTotal.add(montoAfectar);
		        			        	
	        		lineaContable.setClavePresupuestal(UtilClavePresupuestal.getClavePresupuestal(invoiceItem, delegator, UtilClavePresupuestal.INGRESO_TAG, organizationPartyId,ciclo));
	        		lineaContable.setMontoPresupuestal(monto);
	        		lineaContable.setFecha(fechaContable);
	        		lineaContable.setLineasPresupuestales(UtilCommon.getLineaPresupuestal(lineaPresup, invoice.getString("partyId"), null, invoiceItem.getString("productId") ,"0"));	        		
	        		
	        		
	        		
	        		//Obtener Lineas contables
	        			            	
	        		List<String> orderByInvoiceItemLinea = UtilMisc.toList("secuenciaLineaContable");
		        	EntityCondition condInvoiceItemLinea = EntityCondition.makeCondition(EntityOperator.AND,
		        										   EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, invoiceId),
		        										   EntityCondition.makeCondition("invoiceItemSeqId", EntityOperator.EQUALS, invoiceItem.getString("invoiceItemSeqId")));					
		        	List<GenericValue> invoiceItemLineaList = delegator.findByCondition("InvoiceItemLinea", condInvoiceItemLinea, null, orderByInvoiceItemLinea);
		        	Debug.log("invoiceItemLineaList: " + invoiceItemLineaList);
		        	Iterator<GenericValue> invoiceItemLineaIter = null;
		        	Debug.log("listLineaContable1: " + listLineaContable); 	  				  				  						
		        	if(!invoiceItemLineaList.isEmpty())
		        	{	Map<String, GenericValue> lineasC = FastMap.newInstance();
		        		Debug.log("IF1");
		        		invoiceItemLineaIter = invoiceItemLineaList.iterator();
		        		while (invoiceItemLineaIter.hasNext())
		        		{	Debug.log("listLineaContable2: " + listLineaContable);
		        			Debug.log("WHILE2");
		        			GenericValue item = null;
		        			item = invoiceItemLineaIter.next();		        			
		        			GenericValue lc = null;
		        			lc = getLineaContablePorDescripcion(lineasContables, item.getString("descripcion"));
		        			Debug.log("lc: " + lc);
		        			Debug.log("item: " + item);
				        	if (item.getString("catalogoAbono") != null) 
				        	{	Debug.log("IF2");
				        		if (item.getString("valorCatalogoAbono") != null) 
								{	Debug.log("IF3");
				        			lc.set("catalogoAbono", item.getString("valorCatalogoAbono"));
								}								
							}
				        	Debug.log("listLineaContable3: " + listLineaContable);
							if (item.getString("catalogoCargo") != null) 
							{	Debug.log("IF4");
								if (item.getString("valorCatalogoCargo") != null) 
								{	Debug.log("IF5");
									lc.set("catalogoCargo", item.getString("valorCatalogoCargo"));
								}								
							}
							Debug.log("listLineaContable4: " + listLineaContable);
							if(lc.get("monto") == null)
								lc.set("monto", item.getBigDecimal("monto"));
							Debug.log("listLineaContable44: " + listLineaContable);
							Debug.log("lineasC1: " + lineasC);
							Debug.log("lc: " + lc);
							lineasC.put(item.getString("descripcion"),lc);
							Debug.log("listLineaContable5: " + listLineaContable);
		        		}
		        		Debug.log("lineasC: " + lineasC);
		        		lineaContable.setLineasContables(lineasC);
		        	}	        			        				        			        	
//		        	Debug.log("lineaContable: " + lineaContable);	        		        		
		        	listLineaContable.add(contadorMontoAfectar, lineaContable);
//		        	Debug.log("listLineaContable6: " + listLineaContable);
		        	
		        	
		        	
		        	
		        	//Modificar el registro de InvoiceItem
  					BigDecimal montoRestante = invoiceItem.getBigDecimal("montoRestante");			  					
  					invoiceItem.set("montoRestante", montoRestante.subtract(montoAfectar));
  					montoRestanteTotal = montoRestanteTotal.add(montoRestante.subtract(montoAfectar));
  					invoiceItem.set("invoiceId", invoiceId);
  					invoiceItem.set("invoiceItemSeqId", invoiceItem.getString("invoiceItemSeqId"));			  					
  					delegator.store(invoiceItem);
		        	contador++;
		        	contadorMontoAfectar++;
		        }
	        		        	        	
	    
	        	if(TOTAL_MONTO_PRES_COBRO.compareTo(BigDecimal.ZERO) == 0)			
				{	throw new GenericEntityException("No se ha ingresado algun monto a cobrar");
				}
		        input.put("lineasMotor", listLineaContable);
		        Debug.log("input: " + input);
		        output = dispatcher.runSync("creaTransaccionMotor", input);
		        Debug.log("output: " + output);
				        
			    if(ServiceUtil.isError(output))
			    {	return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
			    }
	        }	        
  			//Generar un registro en tabla OrdenCobro
  			GenericValue ordenCobro = GenericValue.create(delegator.getModelEntity("OrdenCobro"));
  			String ordenCobroId = delegator.getNextSeqId("OrdenCobro");			
  			ordenCobro.set("ordenCobroId", ordenCobroId);
  			ordenCobro.set("invoiceId", invoiceId);
  			ordenCobro.set("acctgTransTypeId", acctgTransTypeId);
  			ordenCobro.set("statusId", OrdenCobroPendiente);
  			ordenCobro.set("fechaCobro", fechaCobro);
  			delegator.create(ordenCobro);  					  			
  			//Si cambia el status del Invoice
  			BigDecimal openAmount = invoice.getBigDecimal("openAmount");  			  			
  			if(cambiaEstatusInvoice && montoRestanteTotal.compareTo(BigDecimal.ZERO) > 0)
  			{	invoice.set("statusId", EstatusDevengadoIngParcial);
  				invoice.set("openAmount", openAmount.subtract(montoAfectarTotal));
  				delegator.store(invoice);
  			}
  			else if (cambiaEstatusInvoice && montoRestanteTotal.compareTo(BigDecimal.ZERO) == 0)
  			{	invoice.set("statusId", EstatusDevengadoIngTotal);
  				invoice.set("openAmount", openAmount.subtract(montoAfectarTotal));
				delegator.store(invoice);  				
  			}
  			//Generar un registro en tabla OrdenCobroMulti
  			List<String> orderByLineaCont = UtilMisc.toList("secuencia");
	        EntityCondition condLineaCont = EntityCondition.makeCondition(EntityOperator.AND,
  										EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, acctgTransTypeId));					
	        List<GenericValue> lineaContList = delegator.findByCondition("LineaContable", condLineaCont, null, orderByLineaCont);
	        Iterator<GenericValue> lineaContIter = null;
	        	       

			for(int i=0; i<invoiceItemSeqIdMap.size(); i++)
			{	GenericValue ordenCobroMulti = GenericValue.create(delegator.getModelEntity("OrdenCobroMulti"));  					
				ordenCobroMulti.set("ordenCobroId", ordenCobroId);
	  			ordenCobroMulti.set("invoiceId", invoiceId);
	  			ordenCobroMulti.set("invoiceItemSeqId", invoiceItemSeqIdMap.get(Integer.toString(i+1)));	  				  				  						
				if(!lineaContList.isEmpty())
		        {	lineaContIter = lineaContList.iterator();
			        while (lineaContIter.hasNext())
					{	GenericValue lineaContItem = (GenericValue) lineaContIter.next();
						Debug.log("lineaContItem: " + lineaContItem);
						ordenCobroMulti.set("idCatalogoAbono", null);
						ordenCobroMulti.set("idCatalogoCargo", null);
						String descripcion = lineaContItem.getString("descripcion");
						String stringMapas = descripcion+String.valueOf(i);						
						stringMapas = stringMapas.replaceAll(" ", "");						
						String catalogoCargo = (String) catalogoCargoContMap.get(stringMapas);
						String catalogoAbono = (String) catalogoAbonoContMap.get(stringMapas);
						Debug.log("catalogoCargo: " + catalogoCargo);
						Debug.log("catalogoAbono: " + catalogoAbono);
						String montoContString = (String) montoMap.get(stringMapas);						
						BigDecimal montoCont = BigDecimal.ZERO;
						if(montoContString != null && !montoContString.equals(""))
						{	montoCont = new BigDecimal(montoContString);							
						}											
						Debug.log("montoCont: " + montoCont);
						if(catalogoCargo != null && !catalogoCargo.equals(""))
						{	if(lineaContItem.getString("catalogoAbono") != null && lineaContItem.getString("catalogoAbono").equalsIgnoreCase(OrdenCobroBanco) )
							{	ordenCobroMulti.set("idCatalogoAbono", OrdenCobroBanco);
							}
							else
							{	ordenCobroMulti.set("idCatalogoAbono", catalogoAbono);
								ordenCobroMulti.set("idCatalogoCargo", catalogoCargo);
							}
							
							if(lineaContItem.getString("catalogoCargo") != null && lineaContItem.getString("catalogoCargo").equalsIgnoreCase(OrdenCobroBanco));
							{	ordenCobroMulti.set("idCatalogoCargo", OrdenCobroBanco);								
							}																					
							ordenCobroMulti.set("secuenciaLineaContable", lineaContItem.getString("secuencia"));														
							ordenCobroMulti.set("monto", montoCont);
							ordenCobroMulti.set("montoRestante", montoCont);
							delegator.create(ordenCobroMulti);
						}
						else if(montoCont.compareTo(BigDecimal.ZERO) > 0)
						{	if(catalogoAbono != null && !catalogoAbono.equals(""))
							{	ordenCobroMulti.set("idCatalogoAbono", catalogoAbono);						
							}
							if(lineaContItem.getString("catalogoAbono") != null && lineaContItem.getString("catalogoAbono").equalsIgnoreCase(OrdenCobroBanco) )
							{	ordenCobroMulti.set("idCatalogoAbono", OrdenCobroBanco);
							}
							if(lineaContItem.getString("catalogoCargo") != null)								
							{	if(lineaContItem.getString("catalogoCargo").equalsIgnoreCase(OrdenCobroBanco))
								ordenCobroMulti.set("idCatalogoCargo", OrdenCobroBanco);								
							}														
							ordenCobroMulti.set("secuenciaLineaContable", lineaContItem.getString("secuencia"));							
							ordenCobroMulti.set("monto", montoCont);
							ordenCobroMulti.set("montoRestante", montoCont);
							delegator.create(ordenCobroMulti);
						}
					}
		        }
				ordenCobroMulti.set("idCatalogoAbono", null);									
				ordenCobroMulti.set("secuenciaLineaContable", "0");				
				ordenCobroMulti.set("idCatalogoCargo", null);							
				ordenCobroMulti.set("monto", listaMontosPres.get(Integer.toString(i)));
				ordenCobroMulti.set("montoRestante", listaMontosPres.get(Integer.toString(i)));
				ordenCobroMulti.set("idCatalogoPres", OrdenPagoPresupuesto);
				delegator.create(ordenCobroMulti);										    					
			}				  			  			  			
		}		
		catch (GenericEntityException e) 
		{	return ServiceUtil.returnError(e.getMessage());
		} 
		catch (GenericServiceException e) 
		{	return ServiceUtil.returnError(e.getMessage());
		}														
		return ServiceUtil.returnSuccess("La Orden de Cobro ha sido creada con \u00e9xito");
	}
	
	/**
	 * Metodo que crea el cobro aplicado para una orden de cobro. Esta accion la hace el tesorero
	 * Se utilizan las tablas de payment  
	 */
	@SuppressWarnings({ "unchecked", "null" })
	public static Map enviarOrdenRecaudado(DispatchContext dctx, Map context)
			throws ParseException, GeneralException {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();		
		Debug.log("context: " + context);		
		String ordenCobroId = (String) context.get("ordenCobroId");
		String invoiceId = (String) context.get("invoiceId");
		String acctgTransTypeId = (String) context.get("acctgTransTypeId");
		String organizationPartyId = (String) context.get("organizationPartyId");
		String comentario = (String) context.get("comentario");
		String paymentTypeId = (String) context.get("paymentTypeId");
		String bancoId = (String) context.get("bancoId");
		String cuentaBancariaId = (String) context.get("cuentaBancariaId");
		String paymentRefNum = (String) context.get("paymentRefNum");		
		BigDecimal montoCobro = (BigDecimal) context.get("montoCobro");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		Timestamp fechaTrans = new Timestamp(Calendar.getInstance().getTimeInMillis());		
		GenericValue userLogin = (GenericValue) context.get("userLogin");	
		boolean tieneMatriz = false;
		String ciclo = (String) context.get("ciclo");
		
        Map<String, Object> input = FastMap.newInstance();
        Map<String, Object> output = FastMap.newInstance();
        Map<String, Object> montoMap = FastMap.newInstance();
        Map<String, Object> mapaMontoClave = FastMap.newInstance();
        Map<String, Object> clavePresupuestalMap = FastMap.newInstance();
        Map<String, Object> productIds = FastMap.newInstance();
        Map<String, Object> catalogoCargoContMap = FastMap.newInstance();
        Map<String, Object> catalogoAbonoContMap = FastMap.newInstance();
        Map<String, Object> catalogoCargoPresMap = FastMap.newInstance();
        Map<String, Object> catalogoAbonoPresMap = FastMap.newInstance();

        
		if (bancoId == null || bancoId.trim().equals("")) 
		{	return ServiceUtil.returnError("El p\u00E1rametro Banco no puede estar vacio");
		}
		if (cuentaBancariaId == null || cuentaBancariaId.trim().equals(""))
		{	return ServiceUtil.returnError("El p\u00E1rametro Cuenta bancaria no puede estar vacio");
		}
		if (comentario == null || comentario.trim().equals(""))
		{	return ServiceUtil.returnError("El p\u00E1rametro Comentario no puede estar vacio");
		}
		if (fechaContable == null || fechaContable.equals(""))
		{	return ServiceUtil.returnError("El p\u00E1rametro Fecha Contable no puede estar vacio");
		}		
		
		
		GenericValue invoice = delegator.findByPrimaryKey("Invoice", UtilMisc.toMap("invoiceId",invoiceId));			
		
		
		//Generar un registro en la tabla Payment
		GenericValue payment = GenericValue.create(delegator.getModelEntity("Payment"));
		String paymentId = delegator.getNextSeqId("Payment");			
		payment.set("paymentId", paymentId);
		payment.set("paymentTypeId", paymentTypeId);
		payment.set("partyIdFrom", invoice.getString("partyId"));
		payment.set("partyIdTo", invoice.getString("partyIdFrom"));
		payment.set("statusId", "PMNT_RECEIVED");
		payment.set("effectiveDate", fechaContable);
		payment.set("paymentRefNum", paymentRefNum);
		payment.set("amount", montoCobro);
		payment.set("currencyUomId", invoice.getString("currencyUomId"));
		payment.set("comments", comentario);
		payment.set("acctgTransTypeId", acctgTransTypeId);
		payment.set("bancoId", bancoId);
		payment.set("cuentaBancariaId", cuentaBancariaId);		
		payment.set("appliedAmount", montoCobro);
		payment.set("openAmount", BigDecimal.ZERO);		 
		delegator.create(payment);  	
				
		//VALIDACION DE MATRIZ B.2
		EntityCondition condicionMatriz = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, acctgTransTypeId),
				EntityCondition.makeCondition("tipoMatrizId", EntityOperator.EQUALS, "B.2"));					
		List<GenericValue> LineaMatrizList = delegator.findByCondition("LineaPresupuestal", condicionMatriz, null, null);

		
		//Generar registros en la tabla PaymentApplication obteniendo el monto correspondiente a cada auxiliar de bancos
		List<String> orderBySumaMontoPaymentApp = UtilMisc.toList("invoiceItemSeqId");
		EntityCondition condicionSumaMontoPaymentApp = null;		
		if(!LineaMatrizList.isEmpty())
		{	condicionSumaMontoPaymentApp = EntityCondition.makeCondition(EntityOperator.AND,
														   EntityCondition.makeCondition("ordenCobroId", EntityOperator.EQUALS, ordenCobroId),
														   EntityCondition.makeCondition(UtilMisc.toList(
													       EntityCondition.makeCondition("idCatalogoPres", EntityOperator.EQUALS, OrdenPagoPresupuesto),
													       EntityCondition.makeCondition("idCatalogoCargo", EntityOperator.EQUALS, OrdenCobroBanco), 
													       EntityCondition.makeCondition("idCatalogoAbono", EntityOperator.EQUALS, OrdenCobroBanco)),
													       EntityOperator.OR));
		}
		else
		{	condicionSumaMontoPaymentApp = EntityCondition.makeCondition(EntityOperator.AND,
													   EntityCondition.makeCondition("ordenCobroId", EntityOperator.EQUALS, ordenCobroId),
													   EntityCondition.makeCondition(UtilMisc.toList(
												       EntityCondition.makeCondition("idCatalogoCargo", EntityOperator.EQUALS, OrdenCobroBanco), 
												       EntityCondition.makeCondition("idCatalogoAbono", EntityOperator.EQUALS, OrdenCobroBanco)),
												       EntityOperator.OR));					
		}
		List<GenericValue> sumaSumaMontoPaymentApp = delegator.findByCondition("ObtenerMontoPaymentApplicationIngreso", condicionSumaMontoPaymentApp, UtilMisc.toList("monto", "ordenCobroId", "invoiceItemSeqId"), orderBySumaMontoPaymentApp);
		Debug.log("sumaSumaMontoPaymentApp: " + sumaSumaMontoPaymentApp);
		if(!sumaSumaMontoPaymentApp.isEmpty())
        {	Iterator<GenericValue> sumaSumaMontoPaymentAppIter = sumaSumaMontoPaymentApp.iterator();
        	Debug.log("sumaSumaMontoPaymentAppIter: " + sumaSumaMontoPaymentAppIter);
        	while (sumaSumaMontoPaymentAppIter.hasNext()) 
        	{	GenericValue item = sumaSumaMontoPaymentAppIter.next();
	    		GenericValue paymentApplication = GenericValue.create(delegator.getModelEntity("PaymentApplication"));
	    		String paymentApplicationId = delegator.getNextSeqId("PaymentApplication");
	    		paymentApplication.set("amountApplied",item.getBigDecimal("monto"));
	    		paymentApplication.set("paymentApplicationId", paymentApplicationId);
	    		paymentApplication.set("paymentId", paymentId);
	    		paymentApplication.set("invoiceId", invoiceId);
	    		paymentApplication.set("invoiceItemSeqId", item.getString("invoiceItemSeqId"));
	    		paymentApplication.set("ordenCobroId", ordenCobroId);
	    		Debug.log("paymentApplication: " + paymentApplication);
	    		delegator.create(paymentApplication);
        	}
        }										
		
        if(!LineaMatrizList.isEmpty())
        {		tieneMatriz = true;
	        	//Consultar los registros presupuestales en la orden de cobro
	        	List<String> orderByOrdenCobroPres = UtilMisc.toList("invoiceItemSeqId");
	            EntityCondition condOrdenCobroPres = EntityCondition.makeCondition(EntityOperator.AND,
	    												EntityCondition.makeCondition("idCatalogoPres", EntityOperator.EQUALS, OrdenPagoPresupuesto),
	    												EntityCondition.makeCondition("ordenCobroId", EntityOperator.EQUALS, ordenCobroId));					
	            List<GenericValue> ordenCobroPresList = delegator.findByCondition("OrdenCobroMulti", condOrdenCobroPres, UtilMisc.toList("monto"), orderByOrdenCobroPres);
	            Debug.log("condOrdenCobroPres: " + condOrdenCobroPres);
	            Debug.log("ordenCobroPresList: " + ordenCobroPresList);
	            if(!ordenCobroPresList.isEmpty())
	            {	if(ordenCobroPresList.size() > 1)
	            	{	Iterator<GenericValue> ordenCobroPresIter = ordenCobroPresList.iterator();
	            		int contador = 0;
		        		while (ordenCobroPresIter.hasNext())
		        		{	GenericValue item = ordenCobroPresIter.next();
			        		catalogoCargoPresMap.put(Integer.toString(contador), invoice.getString("partyId"));
				        	catalogoAbonoPresMap.put(Integer.toString(contador), cuentaBancariaId);
		        			mapaMontoClave.put(Integer.toString(contador), item.getBigDecimal("monto").toString());
		        			contador++;
		        		}		            		
	            	}	            	
	            	else
	            	{	Iterator<GenericValue> ordenCobroPresIter = ordenCobroPresList.iterator();
            			while (ordenCobroPresIter.hasNext())
		        		{	GenericValue item = ordenCobroPresIter.next();
			        		mapaMontoClave.put("0", item.getBigDecimal("monto").toString());	            		
		            	}
	            	}
	            } 
	            clavePresupuestalMap = llenaMapClavePresupuestalMap(dctx, context, delegator, invoiceId, TipoClaveIngreso,organizationPartyId,ciclo);        	
        }
        
        	//Consultar las lineas de la orden de cobro y comparar con el flag de linea contable
    		//para obtener los montos presupuestales que sumen y llenar el mapa montoCobro
            List<String> orderByMontosPresu = UtilMisc.toList("invoiceItemSeqId");
            EntityCondition condicionMontosPresu = EntityCondition.makeCondition(EntityOperator.AND,
    											EntityCondition.makeCondition("ordenCobroId", EntityOperator.EQUALS, ordenCobroId),
    											EntityCondition.makeCondition("idCatalogoPres", EntityOperator.EQUALS, null));                    
            List<GenericValue> montosPresupuList = delegator.findByCondition("ConsultaLineasContablesOrdenCobro", condicionMontosPresu, null, orderByMontosPresu);
            Debug.log("condicionMontosPresu: " + condicionMontosPresu);
            Debug.log("montosPresupuList: " + montosPresupuList);
            if(!montosPresupuList.isEmpty())
            {	String secuenciaAux = null;
            	int contador = 0;
            	int contadorMapa = 0;
            	BigDecimal sumaMontoPres = BigDecimal.ZERO;
            	Iterator<GenericValue> montosPresupuIter = montosPresupuList.iterator();	        
            	while (montosPresupuIter.hasNext()) 
            	{	GenericValue item = montosPresupuIter.next();
            		String secuencia = item.getString("invoiceItemSeqId");            		
            		if(secuenciaAux == null)
            		{	secuenciaAux = secuencia;
            			if(item.getString("excepcion") == null)
            			{	sumaMontoPres = sumaMontoPres.add(item.getBigDecimal("monto"));
            			}            			
            			contador++;            			
            		}
            		else if(!secuenciaAux.equals(secuencia))
            		{	secuenciaAux = secuencia;
            			if(!tieneMatriz)
            			{	mapaMontoClave.put(Integer.toString(contadorMapa), sumaMontoPres.toString());            			
            			}
            			contadorMapa++;
            			sumaMontoPres = BigDecimal.ZERO;
            			if(item.getString("excepcion") == null)
            			{	sumaMontoPres = sumaMontoPres.add(item.getBigDecimal("monto"));
            			}            			
            			contador++;        			
            		}
            		else
            		{	if(item.getString("excepcion") == null)
            			{	sumaMontoPres = sumaMontoPres.add(item.getBigDecimal("monto"));
            			}            			
            			contador++;
            		}
            		String descripcion = item.getString("descripcion");
    				descripcion = descripcion.replaceAll(" ", "");
					if(item.getString("idCatalogoCargo") != null && !item.getString("idCatalogoCargo").equalsIgnoreCase(""))
        			{	if(item.getString("idCatalogoCargo").equalsIgnoreCase(OrdenCobroBanco))
						{	catalogoCargoContMap.put(descripcion+contadorMapa, cuentaBancariaId);
						}						
    					else
    					{	catalogoCargoContMap.put(descripcion+contadorMapa, item.getString("idCatalogoCargo"));
    					}    					        				    				
        			}
            		if(item.getString("idCatalogoAbono") != null && !item.getString("idCatalogoAbono").equalsIgnoreCase(""))
        			{	if(item.getString("idCatalogoAbono").equalsIgnoreCase(OrdenCobroBanco))
            			{	catalogoAbonoContMap.put(descripcion+contadorMapa, cuentaBancariaId);
            			}
    					else
    					{	catalogoAbonoContMap.put(descripcion+contadorMapa, item.getString("idCatalogoAbono"));    					
    					}
        				    				
        			}
            		montoMap.put(descripcion+contadorMapa, item.getString("monto"));
            	}
            	if(sumaMontoPres.compareTo(BigDecimal.ZERO) > 0)
            	{	if(!tieneMatriz)
            		{	mapaMontoClave.put(Integer.toString(contadorMapa), sumaMontoPres.toString());
            		}            		
            		contadorMapa++;
            	}        	
            }
            if(!tieneMatriz)
            {	clavePresupuestalMap = llenaMapClavePresupuestalMap(dctx, context, delegator, invoiceId, TipoClaveIngreso,organizationPartyId,ciclo);            
            }
        
        
        
        Debug.log("clavePresupuestalMap: " + clavePresupuestalMap);
        Debug.log("catalogoCargoPresMap: " + catalogoCargoPresMap);
        Debug.log("catalogoAbonoPresMap: " + catalogoAbonoPresMap);
        Debug.log("mapaMontoClave: " + mapaMontoClave);        							
        Debug.log("catalogoCargoContMap: " + catalogoCargoContMap);
        Debug.log("catalogoAbonoContMap: " + catalogoAbonoContMap);
        Debug.log("montoMap: " + montoMap);                       		               
        context.put("clavePresupuestalMap", clavePresupuestalMap);
        context.put("catalogoCargoContMap", catalogoCargoContMap);
        context.put("catalogoAbonoContMap", catalogoAbonoContMap);
        context.put("catalogoCargoPresMap", catalogoCargoPresMap);
        context.put("catalogoAbonoPresMap", catalogoAbonoPresMap);
        context.put("mapaMontoClave", mapaMontoClave);
        context.put("montoMap", montoMap);
                
		/*****MOTOR CONTABLE*****/
        input.put("eventoContableId", acctgTransTypeId);
    	input.put("tipoClaveId", TipoClaveIngreso);
    	input.put("fechaRegistro", fechaTrans);
    	input.put("fechaContable", fechaContable);
    	input.put("currency", invoice.getString("currencyUomId"));
    	input.put("usuario", userLogin.getString("userLoginId"));
    	input.put("organizationId", organizationPartyId);
    	input.put("descripcion", comentario);
    	input.put("roleTypeId", "CUENTAS_C");
    	input.put("campo", "paymentId");
    	input.put("valorCampo", paymentId);
    	input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContablesPagaReca(acctgTransTypeId, delegator, context, invoice.getString("partyId"),cuentaBancariaId, mapaMontoClave, productIds));
		
		 
		
    	output = dispatcher.runSync("creaTransaccionMotor", input);
    	Debug.log("output: " + output);
    	if(ServiceUtil.isError(output)){
    		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
    	}
    	
    	//Actualiza el status de la orden de cobro
		GenericValue ordenCobro = delegator.findByPrimaryKey("OrdenCobro", UtilMisc.toMap("ordenCobroId",ordenCobroId));						
		ordenCobro.set("statusId", OrdenCobroRecaudar);
		ordenCobro.set("paymentId", paymentId);
		delegator.store(ordenCobro);  										
		
		return ServiceUtil.returnSuccess("La Orden de cobro ha sido recaudada con \u00e9xito");
	}
	
	private static GenericValue getLineaContablePorDescripcion(List <GenericValue> lineasContables, String descripcion){
		Debug.log("lineasContables: " + lineasContables);
		Debug.log("descripcion: " + descripcion);
		for(GenericValue linea : lineasContables){
			if(linea.getString("descripcion").replaceAll("\\s","").equalsIgnoreCase(descripcion.replaceAll("\\s",""))){
				Debug.log("linea: " + linea);
				return (GenericValue) linea.clone();
			}
		}
		return null;
	}
	
	
	
	/**
	 * Metodo que crea el pago parcial aplicado para una orden de pago. Esta accion la hace el tesorero
	 * Se utilizan las tablas de payment  
	 */
	@SuppressWarnings({ "unchecked" })
	public static Map enviarOrdenPagadoParcial(DispatchContext dctx, Map context)
			throws ParseException, GeneralException {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();		
		Debug.log("context: " + context);		
		String ordenPagoId = (String) context.get("ordenPagoId");
		String invoiceId = (String) context.get("invoiceId");
		String acctgTransTypeId = (String) context.get("acctgTransTypeId");
		String organizationPartyId = (String) context.get("organizationPartyId");
		String comentario = (String) context.get("comentario");
		String paymentTypeId = (String) context.get("paymentTypeId");
		String bancoId = (String) context.get("bancoId");
		String cuentaBancariaId = (String) context.get("cuentaBancariaId");
		String paymentRefNum = (String) context.get("paymentRefNum");		
		BigDecimal montoPago = (BigDecimal) context.get("montoPago");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		Timestamp fechaTrans = new Timestamp(Calendar.getInstance().getTimeInMillis());		
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		
		Map montoBancoContable = (Map) context.get("montoBancoContable");
		Map secuenciaConBanco = (Map) context.get("secuenciaConBanco");
		Map montoContableSinBanco = (Map) context.get("montoContableSinBanco");
		Map secuenciaSinBanco = (Map) context.get("secuenciaSinBanco");
		BigDecimal montoPresupuestal = (BigDecimal) context.get("montoPresupuestal");
		String ciclo = (String) context.get("ciclo");
		
		if(montoPresupuestal.compareTo(BigDecimal.ZERO)<0){
			return ServiceUtil.returnError("El monto presupuestal no puede ser negativo");
		}
		for(int i=1; i<=montoBancoContable.size(); i++)
		{	
			if(new BigDecimal(montoBancoContable.get(Integer.toString(i)).toString()).compareTo(BigDecimal.ZERO)<0){
				return ServiceUtil.returnError("No pueden existir montos negativos en la afectaci\u00f3n a Bancos");
			}
		}
		for(int i=1; i<=montoContableSinBanco.size(); i++)
		{	
			if(new BigDecimal(montoContableSinBanco.get(Integer.toString(i)).toString()).compareTo(BigDecimal.ZERO)<0){
				return ServiceUtil.returnError("No pueden existir montos negativos en la afectaci\u00f3n contables");
			}
		}		
		
		boolean tieneMatriz = false;
		
		Map<String, Object> input = FastMap.newInstance();
        Map<String, Object> output = FastMap.newInstance();
        Map<String, Object> montoMap = FastMap.newInstance();
        Map<String, Object> mapaMontoClave = FastMap.newInstance();
        Map<String, Object> clavePresupuestalMap = FastMap.newInstance();
        Map<String, Object> productIds = FastMap.newInstance();
        Map<String, Object> catalogoCargoContMap = FastMap.newInstance();
        Map<String, Object> catalogoAbonoContMap = FastMap.newInstance();
        Map<String, Object> catalogoCargoPresMap = FastMap.newInstance();
        Map<String, Object> catalogoAbonoPresMap = FastMap.newInstance();

        
		if (bancoId == null || bancoId.trim().equals("")) 
		{	return ServiceUtil.returnError("El p\u00E1rametro Banco no puede estar vacio");
		}
		if (cuentaBancariaId == null || cuentaBancariaId.trim().equals(""))
		{	return ServiceUtil.returnError("El p\u00E1rametro Cuenta bancaria no puede estar vacio");
		}
		if (comentario == null || comentario.trim().equals(""))
		{	return ServiceUtil.returnError("El p\u00E1rametro Comentario no puede estar vacio");
		}
		if (fechaContable == null || fechaContable.equals(""))
		{	return ServiceUtil.returnError("El p\u00E1rametro Fecha contable no puede estar vacio");
		}
		if (montoPresupuestal == null || montoPresupuestal.equals(""))
		{	return ServiceUtil.returnError("El p\u00E1rametro Monto presupuestal no puede estar vacio");
		}
				
		GenericValue invoice = delegator.findByPrimaryKey("Invoice", UtilMisc.toMap("invoiceId",invoiceId));
		
		//VALIDACION DE MATRIZ A.2
		EntityCondition condicionMatriz = EntityCondition.makeCondition(EntityOperator.AND,
										  EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, acctgTransTypeId),
										  EntityCondition.makeCondition("tipoMatrizId", EntityOperator.EQUALS, "A.2"));					
		List<GenericValue> LineaMatrizList = delegator.findByCondition("LineaPresupuestal", condicionMatriz, null, null);
		
		BigDecimal totalPago = BigDecimal.ZERO;
		BigDecimal totalPagoContable = BigDecimal.ZERO;
		for(int i=1; i<=montoBancoContable.size(); i++)
		{	totalPago = totalPago.add(new BigDecimal(montoBancoContable.get(Integer.toString(i)).toString()));
			totalPagoContable = totalPagoContable.add(new BigDecimal(montoBancoContable.get(Integer.toString(i)).toString()));
		}
		if(!LineaMatrizList.isEmpty())
		{	totalPago = totalPago.add(montoPresupuestal);
		}
		Debug.log("totalPago: " + totalPago);
		Debug.log("totalPagoContable: " + totalPagoContable);
		
		//Generar un registro en la tabla Payment
		GenericValue payment = GenericValue.create(delegator.getModelEntity("Payment"));
		String paymentId = delegator.getNextSeqId("Payment");			
		payment.set("paymentId", paymentId);
		payment.set("paymentTypeId", paymentTypeId);
		payment.set("partyIdFrom", invoice.getString("partyId"));
		payment.set("partyIdTo", invoice.getString("partyIdFrom"));
		payment.set("statusId", "PMNT_SENT");
		payment.set("effectiveDate", fechaContable);
		payment.set("paymentRefNum", paymentRefNum);
		payment.set("amount", totalPago);
		payment.set("currencyUomId", invoice.getString("currencyUomId"));
		payment.set("comments", comentario);
		payment.set("acctgTransTypeId", acctgTransTypeId);
		payment.set("bancoId", bancoId);
		payment.set("cuentaBancariaId", cuentaBancariaId);		
		payment.set("appliedAmount", totalPago);
		payment.set("openAmount", BigDecimal.ZERO);		 
		delegator.create(payment);  	
		
		
				
		int contadorMapaCont = 0;
		//Conultar lineas contables que afectan banco
		for(int i=1; i<=montoBancoContable.size(); i++)
		{	if(secuenciaConBanco.get(Integer.toString(i)) == null)
			{	continue;			
			}
			EntityCondition condicionMontoContable = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("ordenPagoId", EntityOperator.EQUALS, ordenPagoId),
				EntityCondition.makeCondition("secuenciaLineaContable", EntityOperator.EQUALS, secuenciaConBanco.get(Integer.toString(i)).toString()),
				EntityCondition.makeCondition("idCatalogoPres", EntityOperator.EQUALS, null));
			Debug.log("condicionMontoContable: " + condicionMontoContable);
			List<GenericValue> PagosContableList = delegator.findByCondition("OrdenPagoMulti", condicionMontoContable, null, null);
			Debug.log("PagosContableList: " + PagosContableList);
			contadorMapaCont = 0;
			if(!PagosContableList.isEmpty())
	        {	BigDecimal montoLineaConBanco = BigDecimal.ZERO;
    			if(montoBancoContable.get(Integer.toString(i)).toString() != null && !montoBancoContable.get(Integer.toString(i)).toString().equals(""))
    			{	montoLineaConBanco = new BigDecimal(montoBancoContable.get(Integer.toString(i)).toString());	        		
    			}
				Iterator<GenericValue> PagosContableIter = PagosContableList.iterator();
	        	while (PagosContableIter.hasNext()) 
	        	{	boolean consultarAuxiliar = false;
	        		GenericValue ordenPagoMulti = PagosContableIter.next();
	        		String descripcion = obtenerDescripcionLineaContable(context, delegator, ordenPagoId, acctgTransTypeId, ordenPagoMulti.getString("secuenciaLineaContable"));
	        		descripcion = descripcion.replaceAll(" ", "");
	        		Debug.log("descripcion: " + descripcion);	        			        		
	        		Debug.log("montoLineaConBanco: " + montoLineaConBanco);
	        		if(montoLineaConBanco.compareTo(BigDecimal.ZERO) == 0)
	        		{	Debug.log("If1");
	        			consultarAuxiliar = false;
	        		}	        			        		
	        		else if(montoLineaConBanco.compareTo(BigDecimal.ZERO) < 0)
	        		{	Debug.log("If2");
	        			return ServiceUtil.returnError("El monto a pagar por la linea contable [" +descripcion + "] no puede ser menor a cero");	        			
	        		}
	        		else if(montoLineaConBanco.compareTo(ordenPagoMulti.getBigDecimal("montoRestante")) <= 0)
	        		{	Debug.log("If3");
	        			consultarAuxiliar = true;
	        			ordenPagoMulti.set("montoRestante", ordenPagoMulti.getBigDecimal("montoRestante").subtract(montoLineaConBanco));	
        				delegator.store(ordenPagoMulti);
        				montoMap.put(descripcion+contadorMapaCont, montoLineaConBanco.toString());
        				montoLineaConBanco = BigDecimal.ZERO;
        			}
	        		else if(montoLineaConBanco.compareTo(ordenPagoMulti.getBigDecimal("montoRestante")) > 0)
	        		{	Debug.log("If4");
	        			consultarAuxiliar = true;
	        			montoLineaConBanco = montoLineaConBanco.subtract(ordenPagoMulti.getBigDecimal("montoRestante"));
	        			montoMap.put(descripcion+contadorMapaCont, ordenPagoMulti.getBigDecimal("montoRestante").toString());
        				ordenPagoMulti.set("montoRestante", BigDecimal.ZERO);	
        				delegator.store(ordenPagoMulti);        				
        			}
        			else
        			{	return ServiceUtil.returnError("El monto para afectar la linea contable [" +descripcion + "] es mayor al monto restante");
        			}
	        		
	        		if(consultarAuxiliar)
	        		{	if(ordenPagoMulti.getString("idCatalogoCargo") != null && !ordenPagoMulti.getString("idCatalogoCargo").equalsIgnoreCase(""))
	        			{	if(ordenPagoMulti.getString("idCatalogoCargo").equalsIgnoreCase(OrdenPagoBanco))
							{	catalogoCargoContMap.put(descripcion+contadorMapaCont, cuentaBancariaId);
							}						
	    					else
	    					{	catalogoCargoContMap.put(descripcion+contadorMapaCont, ordenPagoMulti.getString("idCatalogoCargo"));
	    					}    					        				    				
	        			}
	            		if(ordenPagoMulti.getString("idCatalogoAbono") != null && !ordenPagoMulti.getString("idCatalogoAbono").equalsIgnoreCase(""))
	        			{	if(ordenPagoMulti.getString("idCatalogoAbono").equalsIgnoreCase(OrdenPagoBanco))
	            			{	catalogoAbonoContMap.put(descripcion+contadorMapaCont, cuentaBancariaId);
	            			}
	    					else
	    					{	catalogoAbonoContMap.put(descripcion+contadorMapaCont, ordenPagoMulti.getString("idCatalogoAbono"));
	    					}	        				    				
	        			}
	            		GenericValue paymentApplication = GenericValue.create(delegator.getModelEntity("PaymentApplication"));
	    	    		String paymentApplicationId = delegator.getNextSeqId("PaymentApplication");
	    	    		paymentApplication.set("amountApplied",montoLineaConBanco);
	    	    		paymentApplication.set("paymentApplicationId", paymentApplicationId);
	    	    		paymentApplication.set("paymentId", paymentId);
	    	    		paymentApplication.set("invoiceId", invoiceId);
	    	    		paymentApplication.set("invoiceItemSeqId", ordenPagoMulti.getString("invoiceItemSeqId"));
	    	    		paymentApplication.set("ordenPagoId", ordenPagoId);
	    	    		delegator.create(paymentApplication);
	        		}
	        		contadorMapaCont++;
	        	}	        		        		        
	        }
			
		}
		
		//Conultar lineas contables que no afectan banco
		for(int i=1; i<=montoContableSinBanco.size(); i++)
		{	if(secuenciaSinBanco.get(Integer.toString(i)) == null)
			{	continue;			
			}
			EntityCondition condicionMontoContableSinBanco = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("ordenPagoId", EntityOperator.EQUALS, ordenPagoId),
				EntityCondition.makeCondition("secuenciaLineaContable", EntityOperator.EQUALS, secuenciaSinBanco.get(Integer.toString(i)).toString()),
				EntityCondition.makeCondition("idCatalogoPres", EntityOperator.EQUALS, null));
			Debug.log("condicionMontoContableSinBanco: " + condicionMontoContableSinBanco);
			List<GenericValue> PagosContableSinBancoList = delegator.findByCondition("OrdenPagoMulti", condicionMontoContableSinBanco, null, null);
			Debug.log("PagosContableSinBancoList: " + PagosContableSinBancoList);
			contadorMapaCont = 0;
			if(!PagosContableSinBancoList.isEmpty())
	        {	BigDecimal montoLineaSinBanco = BigDecimal.ZERO;
    			if(montoContableSinBanco.get(Integer.toString(i)).toString() != null && !montoContableSinBanco.get(Integer.toString(i)).toString().equals(""))
    			{	montoLineaSinBanco = new BigDecimal(montoContableSinBanco.get(Integer.toString(i)).toString());	        		
    			}
				boolean consultarAuxiliar = false;
				Iterator<GenericValue> PagosContableSinBancoIter = PagosContableSinBancoList.iterator();
	        	while (PagosContableSinBancoIter.hasNext()) 
	        	{	GenericValue ordenPagoMulti = PagosContableSinBancoIter.next();
	        		
	        		String descripcion = obtenerDescripcionLineaContable(context, delegator, ordenPagoId, acctgTransTypeId, ordenPagoMulti.getString("secuenciaLineaContable"));
	        		descripcion = descripcion.replaceAll(" ", "");
	        		Debug.log("descripcion: " + descripcion);	        			        		
	        		///////////////VALIDARLO CON EL MONTO QUE VIENE EN EL MAP. NO HACER LA SUMATORIA.
	        		if(montoLineaSinBanco.compareTo(BigDecimal.ZERO) == 0)
	        		{	consultarAuxiliar = false;
	        		}	        			        		
	        		else if(montoLineaSinBanco.compareTo(BigDecimal.ZERO) < 0)
	        		{	return ServiceUtil.returnError("El monto para afectar la linea contable [" +descripcion + "] no puede ser menor a cero");	        			
	        		}
	        		else if(montoLineaSinBanco.compareTo(ordenPagoMulti.getBigDecimal("montoRestante")) <= 0)
	        		{	consultarAuxiliar = true;
	        			ordenPagoMulti.set("montoRestante", ordenPagoMulti.getBigDecimal("montoRestante").subtract(montoLineaSinBanco));	
        				delegator.store(ordenPagoMulti);
        				montoMap.put(descripcion+contadorMapaCont, montoLineaSinBanco.toString());
        				montoLineaSinBanco = BigDecimal.ZERO;
        			}
	        		else if(montoLineaSinBanco.compareTo(ordenPagoMulti.getBigDecimal("montoRestante")) > 0)
	        		{	consultarAuxiliar = true;
	        			montoLineaSinBanco = montoLineaSinBanco.subtract(ordenPagoMulti.getBigDecimal("montoRestante"));
	        			montoMap.put(descripcion+contadorMapaCont, ordenPagoMulti.getBigDecimal("montoRestante").toString());
        				ordenPagoMulti.set("montoRestante", BigDecimal.ZERO);	
        				delegator.store(ordenPagoMulti);        				
        			}	        		
        			else
        			{	return ServiceUtil.returnError("El monto para afectar la linea contable [" +descripcion + "] es mayor al monto restante");
        			}	        				        				        			        		
	        		
	        		if(consultarAuxiliar)
	        		{	if(ordenPagoMulti.getString("idCatalogoCargo") != null && !ordenPagoMulti.getString("idCatalogoCargo").equalsIgnoreCase(""))
	        			{	if(ordenPagoMulti.getString("idCatalogoCargo").equalsIgnoreCase(OrdenPagoBanco))
							{	catalogoCargoContMap.put(descripcion+contadorMapaCont, cuentaBancariaId);
							}						
	    					else
	    					{	catalogoCargoContMap.put(descripcion+contadorMapaCont, ordenPagoMulti.getString("idCatalogoCargo"));
	    					}    					        				    				
	        			}
	            		if(ordenPagoMulti.getString("idCatalogoAbono") != null && !ordenPagoMulti.getString("idCatalogoAbono").equalsIgnoreCase(""))
	        			{	if(ordenPagoMulti.getString("idCatalogoAbono").equalsIgnoreCase(OrdenPagoBanco))
	            			{	catalogoAbonoContMap.put(descripcion+contadorMapaCont, cuentaBancariaId);
	            			}
	    					else
	    					{	catalogoAbonoContMap.put(descripcion+contadorMapaCont, ordenPagoMulti.getString("idCatalogoAbono"));
	    					}
	        				    				
	        			}
	            		GenericValue paymentApplication = GenericValue.create(delegator.getModelEntity("PaymentApplication"));
	    	    		String paymentApplicationId = delegator.getNextSeqId("PaymentApplication");
	    	    		paymentApplication.set("amountApplied",montoLineaSinBanco);
	    	    		paymentApplication.set("paymentApplicationId", paymentApplicationId);
	    	    		paymentApplication.set("paymentId", paymentId);
	    	    		paymentApplication.set("invoiceId", invoiceId);
	    	    		paymentApplication.set("invoiceItemSeqId", ordenPagoMulti.getString("invoiceItemSeqId"));
	    	    		paymentApplication.set("ordenPagoId", ordenPagoId);
	    	    		delegator.create(paymentApplication);
	        		}            		
    	    		contadorMapaCont++;
	        	}
	        }
		}		                						        				
		
        
      //Consultar lineas presupuestales que alcence para pagar
		List<String> fieldsToSelect = UtilMisc.toList("montoRestante", "invoiceItemSeqId", "ordenPagoId", "invoiceId", "secuenciaLineaContable", "idCatalogoPres");
		EntityCondition condicionMontoPres = EntityCondition.makeCondition(EntityOperator.AND,
			EntityCondition.makeCondition("ordenPagoId", EntityOperator.EQUALS, ordenPagoId),
			EntityCondition.makeCondition("idCatalogoPres", EntityOperator.EQUALS, OrdenPagoPresupuesto));
		Debug.log("condicionMontoPres: " + condicionMontoPres);
		List<GenericValue> PagosPresList = delegator.findByCondition("OrdenPagoMulti", condicionMontoPres, fieldsToSelect, null);
		Debug.logInfo("PagosPresList: " + PagosPresList,MODULE);
		if(!PagosPresList.isEmpty())
        {	int contadorMapa = 0;
        	boolean consultarAuxiliar = false;
        	boolean incrementaContador = false;
			Iterator<GenericValue> PagosPresIter = PagosPresList.iterator();  	        	
        	while (PagosPresIter.hasNext()) 
        	{	GenericValue ordenPagoMulti = PagosPresIter.next();
        		BigDecimal montoPresAplicar= BigDecimal.ZERO;
        		if(montoPresupuestal.compareTo(BigDecimal.ZERO) == 0)
        		{	Debug.log("montoPresupuestal1: " + montoPresupuestal);
        			consultarAuxiliar = false;
        			if(mapaMontoClave.isEmpty())
        			{	clavePresupuestalMap.put("0", obtieneClavePresupuestal(dctx, context, delegator, invoiceId, ordenPagoMulti.getString("invoiceItemSeqId"), TipoClaveEgreso,organizationPartyId,ciclo));
        				mapaMontoClave.put("0", "0.00");
        			}
        			incrementaContador = false;
        			break;	        		
        		}
        		else if(ordenPagoMulti.getBigDecimal("montoRestante").compareTo(BigDecimal.ZERO) == 0)
        		{	incrementaContador = false;
        			consultarAuxiliar = false;			
        		}
        		else if(montoPresupuestal.compareTo(ordenPagoMulti.getBigDecimal("montoRestante")) >= 0)
        		{	consultarAuxiliar = true;
        			montoPresupuestal = montoPresupuestal.subtract(ordenPagoMulti.getBigDecimal("montoRestante"));
        			montoPresAplicar = ordenPagoMulti.getBigDecimal("montoRestante");
        			mapaMontoClave.put(Integer.toString(contadorMapa), ordenPagoMulti.getBigDecimal("montoRestante").toString());
        			ordenPagoMulti.set("montoRestante", BigDecimal.ZERO);	        		
        			delegator.store(ordenPagoMulti);  	       
        			clavePresupuestalMap.put(Integer.toString(contadorMapa), obtieneClavePresupuestal(dctx, context, delegator, invoiceId, ordenPagoMulti.getString("invoiceItemSeqId"), TipoClaveEgreso,organizationPartyId,ciclo));
        			incrementaContador = true;
        		}
        		else
        		{	consultarAuxiliar = true;
        			ordenPagoMulti.set("montoRestante", ordenPagoMulti.getBigDecimal("montoRestante").subtract(montoPresupuestal));
        			montoPresAplicar = montoPresupuestal;
        			mapaMontoClave.put(Integer.toString(contadorMapa), montoPresupuestal.toString());
        			montoPresupuestal = BigDecimal.ZERO;
        			delegator.store(ordenPagoMulti);
        			clavePresupuestalMap.put(Integer.toString(contadorMapa), obtieneClavePresupuestal(dctx, context, delegator, invoiceId, ordenPagoMulti.getString("invoiceItemSeqId"), TipoClaveEgreso,organizationPartyId,ciclo));
        			incrementaContador = true;
        		}        		
        		
        		
        		if(consultarAuxiliar)
        		{	if(!LineaMatrizList.isEmpty())
	        		{	String catalogoCargoEvento = obtieneCatalogoEvento(context, delegator, acctgTransTypeId, "catalogoCargo", "LineaPresupuestal");
	        			String catalogoAbonoEvento = obtieneCatalogoEvento(context, delegator, acctgTransTypeId, "catalogoAbono", "LineaPresupuestal");
	        			if(catalogoCargoEvento != null && !catalogoCargoEvento.equalsIgnoreCase(""))
	          			{	if(catalogoCargoEvento.equalsIgnoreCase(OrdenPagoBanco))
	  						{	catalogoCargoPresMap.put(Integer.toString(contadorMapa), cuentaBancariaId);  							
	  						}						
	      					else
	      					{	catalogoCargoPresMap.put(Integer.toString(contadorMapa), invoice.getString("partyIdFrom"));
	      					}    					        				    				
	          			}
	              		if(catalogoAbonoEvento != null && !catalogoAbonoEvento.equalsIgnoreCase(""))
	          			{	if(catalogoAbonoEvento.equalsIgnoreCase(OrdenPagoBanco))
	              			{	catalogoAbonoPresMap.put(Integer.toString(contadorMapa), cuentaBancariaId);
	              			}
	      					else
	      					{	catalogoAbonoPresMap.put(Integer.toString(contadorMapa), invoice.getString("partyIdFrom"));
	      					}          				    			
	          			}
	        		}
	        		GenericValue paymentApplication = GenericValue.create(delegator.getModelEntity("PaymentApplication"));
		    		String paymentApplicationId = delegator.getNextSeqId("PaymentApplication");
		    		paymentApplication.set("amountApplied",montoPresAplicar);
		    		paymentApplication.set("paymentApplicationId", paymentApplicationId);
		    		paymentApplication.set("paymentId", paymentId);
		    		paymentApplication.set("invoiceId", invoiceId);
		    		paymentApplication.set("invoiceItemSeqId", ordenPagoMulti.getString("invoiceItemSeqId"));
		    		paymentApplication.set("ordenPagoId", ordenPagoId);
		    		delegator.create(paymentApplication);
		    		if(incrementaContador)
		    		{	contadorMapa++;
		    		}		    		
        		}         			    	
        	}
        }
		
		Debug.log("catalogoCargoPresMap: " + catalogoCargoPresMap);
        Debug.log("catalogoAbonoPresMap: " + catalogoAbonoPresMap);
        Debug.log("clavePresupuestalMap: " + clavePresupuestalMap);
        Debug.log("mapaMontoClave: " + mapaMontoClave);
        Debug.log("catalogoCargoContMap: " + catalogoCargoContMap);
        Debug.log("catalogoAbonoContMap: " + catalogoAbonoContMap);
        Debug.log("montoMap: " + montoMap);
        
        context.put("clavePresupuestalMap", clavePresupuestalMap);
        context.put("catalogoCargoContMap", catalogoCargoContMap);
        context.put("catalogoAbonoContMap", catalogoAbonoContMap);
        context.put("catalogoCargoPresMap", catalogoCargoPresMap);
        context.put("catalogoAbonoPresMap", catalogoAbonoPresMap);
        context.put("mapaMontoClave", mapaMontoClave);
        context.put("montoMap", montoMap);                               
        
		/*****MOTOR CONTABLE*****/
        input.put("eventoContableId", acctgTransTypeId);
    	input.put("tipoClaveId", TipoClaveEgreso);
    	input.put("fechaRegistro", fechaTrans);
    	input.put("fechaContable", fechaContable);
    	input.put("currency", invoice.getString("currencyUomId"));
    	input.put("usuario", userLogin.getString("userLoginId"));
    	input.put("organizationId", organizationPartyId);
    	input.put("descripcion", comentario);
    	input.put("roleTypeId", "BILL_FROM_VENDOR");
    	input.put("campo", "paymentId");
    	input.put("valorCampo", paymentId);
    	input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContablesPagaReca(acctgTransTypeId, delegator, context, invoice.getString("partyIdFrom"),cuentaBancariaId, mapaMontoClave, productIds));
		
		 
		
    	
    	Debug.log("input: " + input);
    	output = dispatcher.runSync("creaTransaccionMotor", input);
    	Debug.log("output: " + output);
    	if(ServiceUtil.isError(output)){
    		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
    	}
    	
    	GenericValue transaccion = (GenericValue) output.get("transaccion");
    	String poliza = transaccion.getString("poliza");
    	String acctgTransId = transaccion.getString("acctgTransId");
    	//Valida si la orden de pago tiene montos pendientes    	
		EntityCondition condicionSumaMontoRestante = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("ordenPagoId", EntityOperator.EQUALS, ordenPagoId));
		List<GenericValue> sumaMontoRestanteList = delegator.findByCondition("OrdenPagoMulti", condicionSumaMontoRestante, UtilMisc.toList("montoRestante"), null);
		BigDecimal sumaMontoRestante = BigDecimal.ZERO; 		
		if(!sumaMontoRestanteList.isEmpty())
        {	Iterator<GenericValue> sumaMontoRestanteIter = sumaMontoRestanteList.iterator();	        
        	while (sumaMontoRestanteIter.hasNext()) 
        	{	GenericValue generic = sumaMontoRestanteIter.next();
        		sumaMontoRestante = sumaMontoRestante.add(generic.getBigDecimal("montoRestante"));
        	}
        }
    	
    	//Actualiza el status de la orden de pago    	
    	GenericValue ordenPago = delegator.findByPrimaryKey("OrdenPago", UtilMisc.toMap("ordenPagoId",ordenPagoId));
    	if(sumaMontoRestante.compareTo(BigDecimal.ZERO) > 0)
    		ordenPago.set("statusId", OrdenPagoParcial);
    	else
    		ordenPago.set("statusId", OrdenPagoPagada);
		delegator.store(ordenPago);
		
		//Actualiza el numero de poliza contable en Payment
		payment.set("poliza", poliza);
		payment.set("acctgTransId", acctgTransId);		
		payment.set("ordenPagoId", ordenPagoId);
		delegator.store(payment);
		
		return ServiceUtil.returnSuccess("La Orden de pago ha sido pagada con \u00e9xito");
	}
	
	/**
	 * Metodo que crea el cobro parcial aplicado para una orden de cobro. Esta accion la hace el tesorero
	 * Se utilizan las tablas de payment  
	 */
	@SuppressWarnings({ "unchecked" })
	public static Map enviarOrdenRecaudadoParcial(DispatchContext dctx, Map context)
			throws ParseException, GeneralException {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();		
		Debug.log("context: " + context);		
		String ordenCobroId = (String) context.get("ordenCobroId");
		String invoiceId = (String) context.get("invoiceId");
		String acctgTransTypeId = (String) context.get("acctgTransTypeId");
		String organizationPartyId = (String) context.get("organizationPartyId");
		String comentario = (String) context.get("comentario");
		String paymentTypeId = (String) context.get("paymentTypeId");
		String bancoId = (String) context.get("bancoId");
		String cuentaBancariaId = (String) context.get("cuentaBancariaId");
		String paymentRefNum = (String) context.get("paymentRefNum");		
		BigDecimal montoCobro = (BigDecimal) context.get("montoCobro");
		Timestamp fechaContable = (Timestamp) context.get("fechaContable");
		Timestamp fechaTrans = new Timestamp(Calendar.getInstance().getTimeInMillis());		
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		
		Map montoBancoContable = (Map) context.get("montoBancoContable");
		Map secuenciaConBanco = (Map) context.get("secuenciaConBanco");
		Map montoContableSinBanco = (Map) context.get("montoContableSinBanco");
		Map secuenciaSinBanco = (Map) context.get("secuenciaSinBanco");
		BigDecimal montoPresupuestal = (BigDecimal) context.get("montoPresupuestal");
		String ciclo = (String) context.get("ciclo");
		
		if(montoPresupuestal.compareTo(BigDecimal.ZERO)<0){
			return ServiceUtil.returnError("El monto presupuestal no puede ser negativo");
		}
		for(int i=1; i<=montoBancoContable.size(); i++)
		{	
			if(new BigDecimal(montoBancoContable.get(Integer.toString(i)).toString()).compareTo(BigDecimal.ZERO)<0){
				return ServiceUtil.returnError("No pueden existir montos negativos en la afectaci\u00f3n a Bancos");
			}
		}
		for(int i=1; i<=montoContableSinBanco.size(); i++)
		{	
			if(new BigDecimal(montoContableSinBanco.get(Integer.toString(i)).toString()).compareTo(BigDecimal.ZERO)<0){
				return ServiceUtil.returnError("No pueden existir montos negativos en la afectaci\u00f3n contables");
			}
		}	
		
		boolean tieneMatriz = false;
		
		Map<String, Object> input = FastMap.newInstance();
        Map<String, Object> output = FastMap.newInstance();
        Map<String, Object> montoMap = FastMap.newInstance();
        Map<String, Object> mapaMontoClave = FastMap.newInstance();
        Map<String, Object> clavePresupuestalMap = FastMap.newInstance();
        Map<String, Object> productIds = FastMap.newInstance();
        Map<String, Object> catalogoCargoContMap = FastMap.newInstance();
        Map<String, Object> catalogoAbonoContMap = FastMap.newInstance();
        Map<String, Object> catalogoCargoPresMap = FastMap.newInstance();
        Map<String, Object> catalogoAbonoPresMap = FastMap.newInstance();

        
		if (bancoId == null || bancoId.trim().equals("")) 
		{	return ServiceUtil.returnError("El p\u00E1rametro Banco no puede estar vacio");
		}
		if (cuentaBancariaId == null || cuentaBancariaId.trim().equals(""))
		{	return ServiceUtil.returnError("El p\u00E1rametro Cuenta bancaria no puede estar vacio");
		}
		if (comentario == null || comentario.trim().equals(""))
		{	return ServiceUtil.returnError("El p\u00E1rametro Comentario no puede estar vacio");
		}
		if (fechaContable == null || fechaContable.equals(""))
		{	return ServiceUtil.returnError("El p\u00E1rametro Fecha contable no puede estar vacio");
		}
		if (montoPresupuestal == null || montoPresupuestal.equals(""))
		{	return ServiceUtil.returnError("El p\u00E1rametro Monto presupuestal no puede estar vacio");
		}
				
		GenericValue invoice = delegator.findByPrimaryKey("Invoice", UtilMisc.toMap("invoiceId",invoiceId));
		
		//VALIDACION DE MATRIZ B.2
		EntityCondition condicionMatriz = EntityCondition.makeCondition(EntityOperator.AND,
										  EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, acctgTransTypeId),
										  EntityCondition.makeCondition("tipoMatrizId", EntityOperator.EQUALS, "B.2"));					
		List<GenericValue> LineaMatrizList = delegator.findByCondition("LineaPresupuestal", condicionMatriz, null, null);
		
		BigDecimal totalCobro = BigDecimal.ZERO;
		BigDecimal totalCobroContable = BigDecimal.ZERO;
		for(int i=1; i<=montoBancoContable.size(); i++)
		{	totalCobro = totalCobro.add(new BigDecimal(montoBancoContable.get(Integer.toString(i)).toString()));
			totalCobroContable = totalCobroContable.add(new BigDecimal(montoBancoContable.get(Integer.toString(i)).toString()));
		}
		if(!LineaMatrizList.isEmpty())
		{	totalCobro = totalCobro.add(montoPresupuestal);
		}
		Debug.log("totalCobro: " + totalCobro);
		Debug.log("totalCobroContable: " + totalCobroContable);
		
		//Generar un registro en la tabla Payment
		GenericValue payment = GenericValue.create(delegator.getModelEntity("Payment"));
		String paymentId = delegator.getNextSeqId("Payment");			
		payment.set("paymentId", paymentId);
		payment.set("paymentTypeId", paymentTypeId);		
		payment.set("partyIdFrom", invoice.getString("partyId"));
		payment.set("partyIdTo", invoice.getString("partyIdFrom"));
		payment.set("statusId", "PMNT_RECEIVED");		
		payment.set("effectiveDate", fechaContable);
		payment.set("paymentRefNum", paymentRefNum);
		payment.set("amount", totalCobro);
		payment.set("currencyUomId", invoice.getString("currencyUomId"));
		payment.set("comments", comentario);
		payment.set("acctgTransTypeId", acctgTransTypeId);
		payment.set("bancoId", bancoId);
		payment.set("cuentaBancariaId", cuentaBancariaId);		
		payment.set("appliedAmount", totalCobro);
		payment.set("openAmount", BigDecimal.ZERO);		 
		delegator.create(payment);  	
		
		
				
		int contadorMapaCont = 0;
		//Conultar lineas contables que afectan banco
		for(int i=1; i<=montoBancoContable.size(); i++)
		{	if(secuenciaConBanco.get(Integer.toString(i)) == null)
			{	continue;			
			}
			EntityCondition condicionMontoContable = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("ordenCobroId", EntityOperator.EQUALS, ordenCobroId),
				EntityCondition.makeCondition("secuenciaLineaContable", EntityOperator.EQUALS, secuenciaConBanco.get(Integer.toString(i)).toString()),
				EntityCondition.makeCondition("idCatalogoPres", EntityOperator.EQUALS, null));
			Debug.log("condicionMontoContable: " + condicionMontoContable);
			List<GenericValue> CobrosContableList = delegator.findByCondition("OrdenCobroMulti", condicionMontoContable, null, null);
			Debug.log("CobrosContableList: " + CobrosContableList);
			contadorMapaCont = 0;
			if(!CobrosContableList.isEmpty())
	        {	BigDecimal montoLineaConBanco = BigDecimal.ZERO;
    			if(montoBancoContable.get(Integer.toString(i)).toString() != null && !montoBancoContable.get(Integer.toString(i)).toString().equals(""))
    			{	montoLineaConBanco = new BigDecimal(montoBancoContable.get(Integer.toString(i)).toString());	        		
    			}
				Iterator<GenericValue> CobrosContableIter = CobrosContableList.iterator();
	        	while (CobrosContableIter.hasNext()) 
	        	{	boolean consultarAuxiliar = false;
	        		GenericValue ordenCobroMulti = CobrosContableIter.next();
	        		String descripcion = obtenerDescripcionLineaContable(context, delegator, ordenCobroId, acctgTransTypeId, ordenCobroMulti.getString("secuenciaLineaContable"));
	        		descripcion = descripcion.replaceAll(" ", "");
	        		Debug.log("descripcion: " + descripcion);	        			        		
	        		Debug.log("montoLineaConBanco: " + montoLineaConBanco);
	        		if(montoLineaConBanco.compareTo(BigDecimal.ZERO) == 0)
	        		{	Debug.log("If1");
	        			consultarAuxiliar = false;
	        		}	        			        		
	        		else if(montoLineaConBanco.compareTo(BigDecimal.ZERO) < 0)
	        		{	Debug.log("If2");
	        			return ServiceUtil.returnError("El monto a recaudar por la linea contable [" +descripcion + "] no puede ser menor a cero");	        			
	        		}
	        		else if(montoLineaConBanco.compareTo(ordenCobroMulti.getBigDecimal("montoRestante")) <= 0)
	        		{	Debug.log("If3");
	        			consultarAuxiliar = true;
	        			ordenCobroMulti.set("montoRestante", ordenCobroMulti.getBigDecimal("montoRestante").subtract(montoLineaConBanco));	
        				delegator.store(ordenCobroMulti);
        				montoMap.put(descripcion+contadorMapaCont, montoLineaConBanco.toString());
        				montoLineaConBanco = BigDecimal.ZERO;
        			}
	        		else if(montoLineaConBanco.compareTo(ordenCobroMulti.getBigDecimal("montoRestante")) > 0)
	        		{	Debug.log("If4");
	        			consultarAuxiliar = true;
	        			montoLineaConBanco = montoLineaConBanco.subtract(ordenCobroMulti.getBigDecimal("montoRestante"));
	        			montoMap.put(descripcion+contadorMapaCont, ordenCobroMulti.getBigDecimal("montoRestante").toString());
        				ordenCobroMulti.set("montoRestante", BigDecimal.ZERO);	
        				delegator.store(ordenCobroMulti);        				
        			}
        			else
        			{	return ServiceUtil.returnError("El monto para afectar la linea contable [" +descripcion + "] es mayor al monto restante");
        			}
	        		
	        		if(consultarAuxiliar)
	        		{	if(ordenCobroMulti.getString("idCatalogoCargo") != null && !ordenCobroMulti.getString("idCatalogoCargo").equalsIgnoreCase(""))
	        			{	if(ordenCobroMulti.getString("idCatalogoCargo").equalsIgnoreCase(OrdenCobroBanco))
							{	catalogoCargoContMap.put(descripcion+contadorMapaCont, cuentaBancariaId);
							}						
	    					else
	    					{	catalogoCargoContMap.put(descripcion+contadorMapaCont, ordenCobroMulti.getString("idCatalogoCargo"));
	    					}    					        				    				
	        			}
	            		if(ordenCobroMulti.getString("idCatalogoAbono") != null && !ordenCobroMulti.getString("idCatalogoAbono").equalsIgnoreCase(""))
	        			{	if(ordenCobroMulti.getString("idCatalogoAbono").equalsIgnoreCase(OrdenCobroBanco))
	            			{	catalogoAbonoContMap.put(descripcion+contadorMapaCont, cuentaBancariaId);
	            			}
	    					else
	    					{	catalogoAbonoContMap.put(descripcion+contadorMapaCont, ordenCobroMulti.getString("idCatalogoAbono"));
	    					}	        				    				
	        			}
	            		GenericValue paymentApplication = GenericValue.create(delegator.getModelEntity("PaymentApplication"));
	    	    		String paymentApplicationId = delegator.getNextSeqId("PaymentApplication");
	    	    		paymentApplication.set("amountApplied",montoLineaConBanco);
	    	    		paymentApplication.set("paymentApplicationId", paymentApplicationId);
	    	    		paymentApplication.set("paymentId", paymentId);
	    	    		paymentApplication.set("invoiceId", invoiceId);
	    	    		paymentApplication.set("invoiceItemSeqId", ordenCobroMulti.getString("invoiceItemSeqId"));
	    	    		paymentApplication.set("ordenCobroId", ordenCobroId);
	    	    		delegator.create(paymentApplication);
	        		}
	        		contadorMapaCont++;
	        	}	        		        		        
	        }
			
		}
		
		//Conultar lineas contables que no afectan banco
		for(int i=1; i<=montoContableSinBanco.size(); i++)
		{	if(secuenciaSinBanco.get(Integer.toString(i)) == null)
			{	continue;			
			}
			EntityCondition condicionMontoContableSinBanco = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("ordenCobroId", EntityOperator.EQUALS, ordenCobroId),
				EntityCondition.makeCondition("secuenciaLineaContable", EntityOperator.EQUALS, secuenciaSinBanco.get(Integer.toString(i)).toString()),
				EntityCondition.makeCondition("idCatalogoPres", EntityOperator.EQUALS, null));
			Debug.log("condicionMontoContableSinBanco: " + condicionMontoContableSinBanco);
			List<GenericValue> CobrosContableSinBancoList = delegator.findByCondition("OrdenCobroMulti", condicionMontoContableSinBanco, null, null);
			Debug.log("CobrosContableSinBancoList: " + CobrosContableSinBancoList);
			contadorMapaCont = 0;
			if(!CobrosContableSinBancoList.isEmpty())
	        {	BigDecimal montoLineaSinBanco = BigDecimal.ZERO;
    			if(montoContableSinBanco.get(Integer.toString(i)).toString() != null && !montoContableSinBanco.get(Integer.toString(i)).toString().equals(""))
    			{	montoLineaSinBanco = new BigDecimal(montoContableSinBanco.get(Integer.toString(i)).toString());	        		
    			}
				boolean consultarAuxiliar = false;
				Iterator<GenericValue> CobrosContableSinBancoIter = CobrosContableSinBancoList.iterator();
	        	while (CobrosContableSinBancoIter.hasNext()) 
	        	{	GenericValue ordenCobroMulti = CobrosContableSinBancoIter.next();
	        		
	        		String descripcion = obtenerDescripcionLineaContable(context, delegator, ordenCobroId, acctgTransTypeId, ordenCobroMulti.getString("secuenciaLineaContable"));
	        		descripcion = descripcion.replaceAll(" ", "");
	        		Debug.log("descripcion: " + descripcion);	        			        		
	        		///////////////VALIDARLO CON EL MONTO QUE VIENE EN EL MAP. NO HACER LA SUMATORIA.
	        		if(montoLineaSinBanco.compareTo(BigDecimal.ZERO) == 0)
	        		{	consultarAuxiliar = false;
	        		}	        			        		
	        		else if(montoLineaSinBanco.compareTo(BigDecimal.ZERO) < 0)
	        		{	return ServiceUtil.returnError("El monto para afectar la linea contable [" +descripcion + "] no puede ser menor a cero");	        			
	        		}
	        		else if(montoLineaSinBanco.compareTo(ordenCobroMulti.getBigDecimal("montoRestante")) <= 0)
	        		{	consultarAuxiliar = true;
	        			ordenCobroMulti.set("montoRestante", ordenCobroMulti.getBigDecimal("montoRestante").subtract(montoLineaSinBanco));	
        				delegator.store(ordenCobroMulti);
        				montoMap.put(descripcion+contadorMapaCont, montoLineaSinBanco.toString());
        				montoLineaSinBanco = BigDecimal.ZERO;
        			}
	        		else if(montoLineaSinBanco.compareTo(ordenCobroMulti.getBigDecimal("montoRestante")) > 0)
	        		{	consultarAuxiliar = true;
	        			montoLineaSinBanco = montoLineaSinBanco.subtract(ordenCobroMulti.getBigDecimal("montoRestante"));
	        			montoMap.put(descripcion+contadorMapaCont, ordenCobroMulti.getBigDecimal("montoRestante").toString());
        				ordenCobroMulti.set("montoRestante", BigDecimal.ZERO);	
        				delegator.store(ordenCobroMulti);        				
        			}	        		
        			else
        			{	return ServiceUtil.returnError("El monto para afectar la linea contable [" +descripcion + "] es mayor al monto restante");
        			}	        				        				        			        		
	        		
	        		if(consultarAuxiliar)
	        		{	if(ordenCobroMulti.getString("idCatalogoCargo") != null && !ordenCobroMulti.getString("idCatalogoCargo").equalsIgnoreCase(""))
	        			{	if(ordenCobroMulti.getString("idCatalogoCargo").equalsIgnoreCase(OrdenCobroBanco))
							{	catalogoCargoContMap.put(descripcion+contadorMapaCont, cuentaBancariaId);
							}						
	    					else
	    					{	catalogoCargoContMap.put(descripcion+contadorMapaCont, ordenCobroMulti.getString("idCatalogoCargo"));
	    					}    					        				    				
	        			}
	            		if(ordenCobroMulti.getString("idCatalogoAbono") != null && !ordenCobroMulti.getString("idCatalogoAbono").equalsIgnoreCase(""))
	        			{	if(ordenCobroMulti.getString("idCatalogoAbono").equalsIgnoreCase(OrdenCobroBanco))
	            			{	catalogoAbonoContMap.put(descripcion+contadorMapaCont, cuentaBancariaId);
	            			}
	    					else
	    					{	catalogoAbonoContMap.put(descripcion+contadorMapaCont, ordenCobroMulti.getString("idCatalogoAbono"));
	    					}
	        				    				
	        			}
	            		GenericValue paymentApplication = GenericValue.create(delegator.getModelEntity("PaymentApplication"));
	    	    		String paymentApplicationId = delegator.getNextSeqId("PaymentApplication");
	    	    		paymentApplication.set("amountApplied",montoLineaSinBanco);
	    	    		paymentApplication.set("paymentApplicationId", paymentApplicationId);
	    	    		paymentApplication.set("paymentId", paymentId);
	    	    		paymentApplication.set("invoiceId", invoiceId);
	    	    		paymentApplication.set("invoiceItemSeqId", ordenCobroMulti.getString("invoiceItemSeqId"));
	    	    		paymentApplication.set("ordenCobroId", ordenCobroId);
	    	    		delegator.create(paymentApplication);
	        		}            		
    	    		contadorMapaCont++;
	        	}
	        }
		}		                						        				
		
        
      //Consultar lineas presupuestales que alcence para recaudar
		List<String> fieldsToSelect = UtilMisc.toList("montoRestante", "invoiceItemSeqId", "ordenCobroId", "invoiceId", "secuenciaLineaContable", "idCatalogoPres");
		EntityCondition condicionMontoPres = EntityCondition.makeCondition(EntityOperator.AND,
			EntityCondition.makeCondition("ordenCobroId", EntityOperator.EQUALS, ordenCobroId),
			EntityCondition.makeCondition("idCatalogoPres", EntityOperator.EQUALS, OrdenCobroPresupuesto));
		Debug.log("condicionMontoPres: " + condicionMontoPres);
		List<GenericValue> CobrosPresList = delegator.findByCondition("OrdenCobroMulti", condicionMontoPres, fieldsToSelect, null);
		Debug.log("CobrosPresList: " + CobrosPresList);
		if(!CobrosPresList.isEmpty())
        {	int contadorMapa = 0;
        	boolean consultarAuxiliar = false;
        	boolean incrementaContador = false;
			Iterator<GenericValue> CobrosPresIter = CobrosPresList.iterator();  	        	
        	while (CobrosPresIter.hasNext()) 
        	{	GenericValue ordenCobroMulti = CobrosPresIter.next();
        		BigDecimal montoPresAplicar= BigDecimal.ZERO;
        		if(montoPresupuestal.compareTo(BigDecimal.ZERO) == 0)
        		{	Debug.log("montoPresupuestal1: " + montoPresupuestal);
        			consultarAuxiliar = false;
        			if(mapaMontoClave.isEmpty())
        			{	clavePresupuestalMap.put("0", obtieneClavePresupuestal(dctx, context, delegator, invoiceId, ordenCobroMulti.getString("invoiceItemSeqId"), TipoClaveEgreso,organizationPartyId,ciclo));
        				mapaMontoClave.put("0", "0.00");
        			}
        			incrementaContador = false;
        			break;	        		
        		}
        		else if(ordenCobroMulti.getBigDecimal("montoRestante").compareTo(BigDecimal.ZERO) == 0)
        		{	incrementaContador = false;
        			consultarAuxiliar = false;			
        		}
        		else if(montoPresupuestal.compareTo(ordenCobroMulti.getBigDecimal("montoRestante")) >= 0)
        		{	consultarAuxiliar = true;
        			montoPresupuestal = montoPresupuestal.subtract(ordenCobroMulti.getBigDecimal("montoRestante"));
        			montoPresAplicar = ordenCobroMulti.getBigDecimal("montoRestante");
        			mapaMontoClave.put(Integer.toString(contadorMapa), ordenCobroMulti.getBigDecimal("montoRestante").toString());
        			ordenCobroMulti.set("montoRestante", BigDecimal.ZERO);	        		
        			delegator.store(ordenCobroMulti);  	       
        			clavePresupuestalMap.put(Integer.toString(contadorMapa), obtieneClavePresupuestal(dctx, context, delegator, invoiceId, ordenCobroMulti.getString("invoiceItemSeqId"), TipoClaveEgreso,organizationPartyId,ciclo));
        			incrementaContador = true;
        		}
        		else
        		{	consultarAuxiliar = true;
        			ordenCobroMulti.set("montoRestante", ordenCobroMulti.getBigDecimal("montoRestante").subtract(montoPresupuestal));
        			montoPresAplicar = montoPresupuestal;
        			mapaMontoClave.put(Integer.toString(contadorMapa), montoPresupuestal.toString());
        			montoPresupuestal = BigDecimal.ZERO;
        			delegator.store(ordenCobroMulti);
        			clavePresupuestalMap.put(Integer.toString(contadorMapa), obtieneClavePresupuestal(dctx, context, delegator, invoiceId, ordenCobroMulti.getString("invoiceItemSeqId"), TipoClaveEgreso,organizationPartyId,ciclo));
        			incrementaContador = true;
        		}        		
        		
        		
        		if(consultarAuxiliar)
        		{	if(!LineaMatrizList.isEmpty())
	        		{	String catalogoCargoEvento = obtieneCatalogoEvento(context, delegator, acctgTransTypeId, "catalogoCargo", "LineaPresupuestal");
	        			String catalogoAbonoEvento = obtieneCatalogoEvento(context, delegator, acctgTransTypeId, "catalogoAbono", "LineaPresupuestal");
	        			if(catalogoCargoEvento != null && !catalogoCargoEvento.equalsIgnoreCase(""))
	          			{	if(catalogoCargoEvento.equalsIgnoreCase(OrdenCobroBanco))
	  						{	catalogoCargoPresMap.put(Integer.toString(contadorMapa), cuentaBancariaId);  							
	  						}						
	      					else
	      					{	catalogoCargoPresMap.put(Integer.toString(contadorMapa), invoice.getString("partyIdFrom"));
	      					}    					        				    				
	          			}
	              		if(catalogoAbonoEvento != null && !catalogoAbonoEvento.equalsIgnoreCase(""))
	          			{	if(catalogoAbonoEvento.equalsIgnoreCase(OrdenCobroBanco))
	              			{	catalogoAbonoPresMap.put(Integer.toString(contadorMapa), cuentaBancariaId);
	              			}
	      					else
	      					{	catalogoAbonoPresMap.put(Integer.toString(contadorMapa), invoice.getString("partyIdFrom"));
	      					}          				    			
	          			}
	        		}
	        		GenericValue paymentApplication = GenericValue.create(delegator.getModelEntity("PaymentApplication"));
		    		String paymentApplicationId = delegator.getNextSeqId("PaymentApplication");
		    		paymentApplication.set("amountApplied",montoPresAplicar);
		    		paymentApplication.set("paymentApplicationId", paymentApplicationId);
		    		paymentApplication.set("paymentId", paymentId);
		    		paymentApplication.set("invoiceId", invoiceId);
		    		paymentApplication.set("invoiceItemSeqId", ordenCobroMulti.getString("invoiceItemSeqId"));
		    		paymentApplication.set("ordenCobroId", ordenCobroId);
		    		delegator.create(paymentApplication);
		    		if(incrementaContador)
		    		{	contadorMapa++;
		    		}		    		
        		}         			    	
        	}
        }
		
		Debug.log("catalogoCargoPresMap: " + catalogoCargoPresMap);
        Debug.log("catalogoAbonoPresMap: " + catalogoAbonoPresMap);
        Debug.log("clavePresupuestalMap: " + clavePresupuestalMap);
        Debug.log("mapaMontoClave: " + mapaMontoClave);
        Debug.log("catalogoCargoContMap: " + catalogoCargoContMap);
        Debug.log("catalogoAbonoContMap: " + catalogoAbonoContMap);
        Debug.log("montoMap: " + montoMap);
        
        context.put("clavePresupuestalMap", clavePresupuestalMap);
        context.put("catalogoCargoContMap", catalogoCargoContMap);
        context.put("catalogoAbonoContMap", catalogoAbonoContMap);
        context.put("catalogoCargoPresMap", catalogoCargoPresMap);
        context.put("catalogoAbonoPresMap", catalogoAbonoPresMap);
        context.put("mapaMontoClave", mapaMontoClave);
        context.put("montoMap", montoMap);                               
        
		/*****MOTOR CONTABLE*****/
        input.put("eventoContableId", acctgTransTypeId);
    	input.put("tipoClaveId", TipoClaveIngreso);
    	input.put("fechaRegistro", fechaTrans);
    	input.put("fechaContable", fechaContable);
    	input.put("currency", invoice.getString("currencyUomId"));
    	input.put("usuario", userLogin.getString("userLoginId"));
    	input.put("organizationId", organizationPartyId);
    	input.put("descripcion", comentario);
    	input.put("roleTypeId", "CUENTAS_C");
    	input.put("campo", "paymentId");
    	input.put("valorCampo", paymentId);
    	input.put("lineasMotor", org.ofbiz.common.AB.UtilCommon.getLineasContablesPagaReca(acctgTransTypeId, delegator, context, invoice.getString("partyId"),cuentaBancariaId, mapaMontoClave, productIds));
		
		 
		
    	
    	Debug.log("input: " + input);
    	output = dispatcher.runSync("creaTransaccionMotor", input);
    	Debug.log("output: " + output);
    	if(ServiceUtil.isError(output)){
    		return ServiceUtil.returnError(ServiceUtil.getErrorMessage(output));
    	}
    	
    	GenericValue transaccion = (GenericValue) output.get("transaccion");
    	String poliza = transaccion.getString("poliza");
    	String acctgTransId = transaccion.getString("acctgTransId");
    	//Valida si la orden de cobro tiene montos pendientes    	
		EntityCondition condicionSumaMontoRestante = EntityCondition.makeCondition(EntityOperator.AND,
				EntityCondition.makeCondition("ordenCobroId", EntityOperator.EQUALS, ordenCobroId));
		List<GenericValue> sumaMontoRestanteList = delegator.findByCondition("OrdenCobroMulti", condicionSumaMontoRestante, UtilMisc.toList("montoRestante"), null);
		BigDecimal sumaMontoRestante = BigDecimal.ZERO; 		
		if(!sumaMontoRestanteList.isEmpty())
        {	Iterator<GenericValue> sumaMontoRestanteIter = sumaMontoRestanteList.iterator();	        
        	while (sumaMontoRestanteIter.hasNext()) 
        	{	GenericValue generic = sumaMontoRestanteIter.next();
        		sumaMontoRestante = sumaMontoRestante.add(generic.getBigDecimal("montoRestante"));
        	}
        }
    	
    	//Actualiza el status de la orden de cobro    	
    	GenericValue ordenCobro = delegator.findByPrimaryKey("OrdenCobro", UtilMisc.toMap("ordenCobroId",ordenCobroId));
    	if(sumaMontoRestante.compareTo(BigDecimal.ZERO) > 0)
    		ordenCobro.set("statusId", OrdenCobroParcial);
    	else
    		ordenCobro.set("statusId", OrdenCobroRecaudar);
		delegator.store(ordenCobro);
		
		//Actualiza el numero de poliza contable en Payment
		payment.set("poliza", poliza);
		payment.set("acctgTransId", acctgTransId);		
		payment.set("ordenCobroId", ordenCobroId);
		delegator.store(payment);
		
		return ServiceUtil.returnSuccess("La Orden de cobro ha sido recaudada con \u00e9xito");
	}
	
	
	/**
	 * Metodo que obtiene la descripcion de una linea contable definida en el evento contable
	 * @throws GenericServiceException 
	 * @throws GenericEntityException 
	 */
	@SuppressWarnings({ "unchecked" })
	public static String obtenerDescripcionLineaContable(Map context, Delegator delegator, String ordenPagoId, String acctgTransTypeId, String secuencia) throws GenericServiceException, GenericEntityException
	{	String descripcion = "";
		EntityCondition condicionLineaContable = EntityCondition.makeCondition(EntityOperator.AND,
												EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, acctgTransTypeId),
												EntityCondition.makeCondition("secuencia", EntityOperator.EQUALS, secuencia));
        Debug.log("condicionLineaContable: " + condicionLineaContable);
        List<GenericValue> LineaContableList = delegator.findByCondition("LineaContable", condicionLineaContable, UtilMisc.toList("descripcion"), null);
        if(!LineaContableList.isEmpty())
        {	Iterator<GenericValue> LineaContableIter = LineaContableList.iterator();
        	while (LineaContableIter.hasNext()) 
        	{	GenericValue lineaContable = LineaContableIter.next();
        		descripcion = lineaContable.getString("descripcion");		          
        	}
        }			
		return descripcion;		
	}
	
	/**
	 * Metodo que obtiene el valor de catalogo cargo y catalogo abono de la definicion del evento
	 * @throws GenericServiceException 
	 * @throws GenericEntityException 
	 */
	@SuppressWarnings({ "unchecked" })
	public static String obtieneCatalogoEvento(Map context, Delegator delegator, String acctgTransTypeId, String catalogo, String tabla) throws GenericServiceException, GenericEntityException
	{	String descripcion = "";	
		EntityCondition condicionCatalogoEvento = EntityCondition.makeCondition(EntityOperator.AND,
												EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, acctgTransTypeId));
        Debug.log("condicionCatalogoEvento: " + condicionCatalogoEvento);
        List<GenericValue> CatalogoEventoList = delegator.findByCondition(tabla, condicionCatalogoEvento, UtilMisc.toList(catalogo), null);
        if(!CatalogoEventoList.isEmpty())
        {	Iterator<GenericValue> CatalogoEventoIter = CatalogoEventoList.iterator();
        	while (CatalogoEventoIter.hasNext()) 
        	{	GenericValue catalogoEvento = CatalogoEventoIter.next();
        		descripcion = catalogoEvento.getString(catalogo);		          
        	}
        }			
		return descripcion;		
	}
		
	/**
	 * Metodo que actualiza la fecha de pago de una orden de pago  
	 */
	public static Map actualizarFechaPago(DispatchContext dctx, Map context)
			throws ParseException, GeneralException {
		Delegator delegator = dctx.getDelegator();
		String ordenPagoId = (String) context.get("ordenPagoId");
		String invoiceId = (String) context.get("invoiceId");
		Timestamp fechaPago = (Timestamp) context.get("fechaPagoValor");
		Debug.log("context: " + context);
		
//		if(fechaPago.before(UtilDateTime.nowTimestamp()))
//		{	return ServiceUtil.returnError("La fecha de pago no puede menor a la fecha actual");			
//		}
	
		GenericValue ordenPago = delegator.findByPrimaryKey("OrdenPago", UtilMisc.toMap("ordenPagoId",ordenPagoId));						
		ordenPago.set("fechaPago", fechaPago);
		delegator.store(ordenPago);
		
		Map<String, Object> result = ServiceUtil.returnSuccess("La la fecha de pago de la Orden de pago " + ordenPagoId+ " ha sido actualizada con \u00e9xito");
		result.put("ordenPagoId", ordenPagoId);
		result.put("invoiceId", invoiceId);
		
		return result;				
	}
	
	/**
	 * Metodo que actualiza la fecha de cobro de una orden de cobro  
	 */
	public static Map actualizarFechaCobro(DispatchContext dctx, Map context)
			throws ParseException, GeneralException {
		Delegator delegator = dctx.getDelegator();
		String ordenCobroId = (String) context.get("ordenCobroId");
		String invoiceId = (String) context.get("invoiceId");
		Timestamp fechaCobro = (Timestamp) context.get("fechaCobroValor");
		Debug.log("context: " + context);
		
//		if(fechaCobro.before(UtilDateTime.nowTimestamp()))
//		{	return ServiceUtil.returnError("La fecha de cobro no puede menor a la fecha actual");			
//		}
	
		GenericValue ordenCobro = delegator.findByPrimaryKey("OrdenCobro", UtilMisc.toMap("ordenCobroId",ordenCobroId));						
		ordenCobro.set("fechaCobro", fechaCobro);
		delegator.store(ordenCobro);
		
		Map<String, Object> result = ServiceUtil.returnSuccess("La la fecha de cobro de la Orden de cobro " + ordenCobroId+ " ha sido actualizada con \u00e9xito");
		result.put("ordenCobroId", ordenCobroId);
		result.put("invoiceId", invoiceId);
		
		return result;				
	}
	
}

