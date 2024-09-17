package com.absoluciones.controlPatrimonial;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

import org.apache.commons.validator.GenericValidator;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.GenericServiceException;
import org.opentaps.foundation.entity.EntityNotFoundException;
import org.opentaps.foundation.infrastructure.InfrastructureException;
import org.opentaps.foundation.repository.RepositoryException;

public class ImpresionPlanillaCodigoBarras {
	
	@SuppressWarnings({ })
	public static String preparaPlanillaCodigosServlet(HttpServletRequest request, HttpServletResponse response) throws InfrastructureException, RepositoryException, GenericEntityException, EntityNotFoundException, GenericServiceException{
		
		Debug.log("Llega a preparaPlanillaCodigosServlet");
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		List<Map<String, String>> reportData = new FastList<Map<String, String>>();
		
    	String productIdList = (String) request.getParameter("productIdList");    	
    	String idTamano = (String) request.getParameter("idTamano");
    	String posicionImpresion = (String) request.getParameter("posicionImpresion");
    	String organizationPartyId = (String) request.getParameter("organizationPartyId");
    	int posicionImpresionInt = Integer.parseInt(posicionImpresion);
    	String organismo = "";
    	
    	GenericValue partyGroup = delegator.findByPrimaryKey("PartyGroup", UtilMisc.toMap("partyId", organizationPartyId));
    	organismo = (String) partyGroup.get("groupName");
    	
    	String reporteId = "error";     	    
    	
    	if(idTamano.equals("4x2"))
    	{	if(posicionImpresionInt < 0 || posicionImpresionInt > 10)
    		{	throw new GenericServiceException("Error en el campo Posici\u00f3n de impresi\u00f3n ");    		
    		}
    	}
    	else if(idTamano.equals("2x1"))
    	{	if(posicionImpresionInt < 0 || posicionImpresionInt > 30)
    		{	throw new GenericServiceException("Error en el campo Posici\u00f3n de impresi\u00f3n ");    		
    		}
    	}
    	
    	//Nombre del cheque a imprimir

		Map<String, Object> jrParameters = new FastMap<String, Object>();		
		
		int contador = 0;
		int contadorInicio = 0;
		Map <String, String> productIdMap = FastMap.newInstance();
		Map <String, String> productNameMap = FastMap.newInstance();
		Map <String, String> dateAcquiredMap = FastMap.newInstance();
		Map <String, String> organizacionMap = FastMap.newInstance();
		if(posicionImpresionInt > 1)
		{	for(int i=0; i<posicionImpresionInt-1; i++)
			{	productIdMap.put(Integer.toString(i), "");
				productNameMap.put(Integer.toString(i), "");
				dateAcquiredMap.put(Integer.toString(i), "");
				organizacionMap.put(Integer.toString(i), "");				
				contadorInicio = i+1;
			}
		}
		contador = contadorInicio;
		String[] listaSplit = productIdList.split("|");
		String productIdString = "";
		for (String listaS : listaSplit) 
		{	if(!GenericValidator.isBlankOrNull(listaS))
			{	if(!listaS.equals("|"))
				{	productIdString = productIdString + listaS;			
				}
				else
				{	productIdMap.put(Integer.toString(contador), productIdString);
					organizacionMap.put(Integer.toString(contador), organismo);					
					productIdString = "";
					contador++;
				}        	
		    }	
			
		}		
		for(int i=0; i<productIdMap.size(); i++)
		{	if(productIdMap.get(Integer.toString(i)) != null && !productIdMap.get(Integer.toString(i)).equals(""))
			{	GenericValue fixedAsset = delegator.findByPrimaryKey("FixedAsset", UtilMisc.toMap("fixedAssetId", productIdMap.get(Integer.toString(i)).toString()));
				productNameMap.put(Integer.toString(i), fixedAsset.getString("fixedAssetName"));
				String fecha = fixedAsset.getTimestamp("dateAcquired").toString();
				dateAcquiredMap.put(Integer.toString(i), fecha.substring(0, 7));				
			}
		}
				
		if(idTamano.equals("4x2"))
		{	reporteId = "reporte1";
			for (int i=0; i < productIdMap.size(); i=i+1) 
			{ 	Map<String, String> reportLine = new FastMap<String, String>();
				if(productIdMap.get(Integer.toString(i)) != null && !productIdMap.get(Integer.toString(i)).equals(""))
				{	reportLine.put("idProduct1", productIdMap.get(Integer.toString(i)));
					reportLine.put("organizacion1", organizacionMap.get(Integer.toString(i)));
				}
				/*if(productIdMap.get(Integer.toString(i+1)) != null && !productIdMap.get(Integer.toString(i+1)).equals(""))
				{	reportLine.put("idProduct2", productIdMap.get(Integer.toString(i+1)));
					reportLine.put("organizacion2", organizacionMap.get(Integer.toString(i+1)));
				}*/
				if(productNameMap.get(Integer.toString(i)) != null && !productNameMap.get(Integer.toString(i)).equals(""))
				{	reportLine.put("productName1", productNameMap.get(Integer.toString(i)));
				}
				/*if(productNameMap.get(Integer.toString(i+1)) != null && !productNameMap.get(Integer.toString(i+1)).equals(""))
				{	reportLine.put("productName2", productNameMap.get(Integer.toString(i+1)));
				}*/
				if(dateAcquiredMap.get(Integer.toString(i)) != null && !dateAcquiredMap.get(Integer.toString(i)).equals(""))
				{	String fecha = dateAcquiredMap.get(Integer.toString(i));
					reportLine.put("fechaAdqui1", fecha.substring(0, 7));
				}
				/*if(dateAcquiredMap.get(Integer.toString(i+1)) != null && !dateAcquiredMap.get(Integer.toString(i+1)).equals(""))
				{	String fecha = dateAcquiredMap.get(Integer.toString(i+1));
					reportLine.put("fechaAdqui2", fecha.substring(0, 7));
				}*/
				reportData.add(reportLine);            
	        }
		}
		else if(idTamano.equals("2x1"))
		{	reporteId = "reporte2";
			for (int i=0; i < productIdMap.size(); i=i+5) 
			{ 	Map<String, String> reportLine = new FastMap<String, String>();
				if(productIdMap.get(Integer.toString(i)) != null && !productIdMap.get(Integer.toString(i)).equals(""))
				{	reportLine.put("idProduct1", productIdMap.get(Integer.toString(i)));
					reportLine.put("organizacion1", organizacionMap.get(Integer.toString(i)));
				}
				if(productIdMap.get(Integer.toString(i+1)) != null && !productIdMap.get(Integer.toString(i+1)).equals(""))
				{	reportLine.put("idProduct2", productIdMap.get(Integer.toString(i+1)));
					reportLine.put("organizacion2", organizacionMap.get(Integer.toString(i+1)));
				}
				if(productIdMap.get(Integer.toString(i+2)) != null && !productIdMap.get(Integer.toString(i+2)).equals(""))
				{	reportLine.put("idProduct3", productIdMap.get(Integer.toString(i+2)));
					reportLine.put("organizacion3", organizacionMap.get(Integer.toString(i+2)));
				}
				if(productIdMap.get(Integer.toString(i+3)) != null && !productIdMap.get(Integer.toString(i+3)).equals(""))
				{	reportLine.put("idProduct4", productIdMap.get(Integer.toString(i+3)));
					reportLine.put("organizacion4", organizacionMap.get(Integer.toString(i+3)));
				}
				if(productIdMap.get(Integer.toString(i+4)) != null && !productIdMap.get(Integer.toString(i+4)).equals(""))
				{	reportLine.put("idProduct5", productIdMap.get(Integer.toString(i+4)));
					reportLine.put("organizacion5", organizacionMap.get(Integer.toString(i+4)));
				}
				if(productNameMap.get(Integer.toString(i)) != null && !productNameMap.get(Integer.toString(i)).equals(""))
				{	reportLine.put("productName1", productNameMap.get(Integer.toString(i)));
				}
				if(productNameMap.get(Integer.toString(i+1)) != null && !productNameMap.get(Integer.toString(i+1)).equals(""))
				{	reportLine.put("productName2", productNameMap.get(Integer.toString(i+1)));
				}
				if(productNameMap.get(Integer.toString(i+2)) != null && !productNameMap.get(Integer.toString(i+2)).equals(""))
				{	reportLine.put("productName3", productNameMap.get(Integer.toString(i+2)));
				}
				if(productNameMap.get(Integer.toString(i+3)) != null && !productNameMap.get(Integer.toString(i+3)).equals(""))
				{	reportLine.put("productName4", productNameMap.get(Integer.toString(i+3)));
				}
				if(productNameMap.get(Integer.toString(i+4)) != null && !productNameMap.get(Integer.toString(i+4)).equals(""))
				{	reportLine.put("productName5", productNameMap.get(Integer.toString(i+4)));
				}
				if(dateAcquiredMap.get(Integer.toString(i)) != null && !dateAcquiredMap.get(Integer.toString(i)).equals(""))
				{	String fecha = dateAcquiredMap.get(Integer.toString(i));
					reportLine.put("fechaAdqui1", fecha.substring(0, 7));
				}
				if(dateAcquiredMap.get(Integer.toString(i+1)) != null && !dateAcquiredMap.get(Integer.toString(i+1)).equals(""))
				{	String fecha = dateAcquiredMap.get(Integer.toString(i+1));
					reportLine.put("fechaAdqui2", fecha.substring(0, 7));
				}
				if(dateAcquiredMap.get(Integer.toString(i+2)) != null && !dateAcquiredMap.get(Integer.toString(i+2)).equals(""))
				{	String fecha = dateAcquiredMap.get(Integer.toString(i+2));
					reportLine.put("fechaAdqui3", fecha.substring(0, 7));
				}
				if(dateAcquiredMap.get(Integer.toString(i+3)) != null && !dateAcquiredMap.get(Integer.toString(i+3)).equals(""))
				{	String fecha = dateAcquiredMap.get(Integer.toString(i+3));
					reportLine.put("fechaAdqui4", fecha.substring(0, 7));
				}
				if(dateAcquiredMap.get(Integer.toString(i+4)) != null && !dateAcquiredMap.get(Integer.toString(i+4)).equals(""))
				{	String fecha = dateAcquiredMap.get(Integer.toString(i+4));
					reportLine.put("fechaAdqui5", fecha.substring(0, 7));
				}
				reportData.add(reportLine);            
	        }
		}

		JRMapCollectionDataSource jrDataSource = new JRMapCollectionDataSource(reportData);
        request.setAttribute("jrDataSource", jrDataSource);
        request.setAttribute("jrParameters", jrParameters);                
		
		return reporteId;
	}	
}
