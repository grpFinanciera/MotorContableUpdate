package org.opentaps.financials.motor;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.opentaps.base.entities.GlAccount;
import org.opentaps.base.entities.VistaMatrizEgreso;
import org.opentaps.base.entities.VistaMatrizIngreso;
import org.opentaps.common.builder.EntityListBuilder;
import org.opentaps.common.builder.PageBuilder;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.domain.DomainsDirectory;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.action.ActionContext;

public class ConsultaMatriz {
	
	 private static final String MODULE = ConsultaMatriz.class.getName();
	
	 @SuppressWarnings("unused")
	public static void consultaMatriz(Map<String, Object> context) throws GeneralException, ParseException {

	        final ActionContext ac = new ActionContext(context);
	        final Locale locale = ac.getLocale();
	        String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());

	        String tipoMatriz = ac.getParameter("acctgTagUsageTypeId");
	        String matrizId = ac.getParameter("matrizId");	
	        String cog = ac.getParameter("cog");
	        String cri = ac.getParameter("cri");
	        String nombreCog = ac.getParameter("nombreCog");
	        String nombreCri = ac.getParameter("nombreCri");
	        String tipoGasto = ac.getParameter("tipoGasto");
	        String cargo = ac.getParameter("cargo");
	        String abono = ac.getParameter("abono");
	        	        
	        DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
	        final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();
	        
	        Delegator delegator = ac.getDelegator();
	        	        
	       
	        List<Map<String, Object>> tipoList = new FastList<Map<String, Object>>();
			Map<String, Object> tipos = new HashMap<String, Object>();
			tipos.put("acctgTagUsageTypeId", "EGRESO");
			tipoList.add(tipos);
			tipos = new HashMap<String, Object>();
			tipos.put("acctgTagUsageTypeId", "INGRESO");
			tipoList.add(tipos);                  
	        
	        ac.put("tipoList", tipoList);
	        
	        ac.put("tipoMatriz", tipoMatriz);	        	        	        	        
									 
	        List<GenericValue> matrizList = delegator.findAll("TipoMatriz");
	        ac.put("matrizList", matrizList);	        	    	       	        
	        
	        
	        if ("Y".equals(ac.getParameter("performFind"))) 
	        {	if(tipoMatriz.equals("EGRESO"))
	        	{	List<EntityCondition> searchConditions = new FastList<EntityCondition>();
		           
		            if (UtilValidate.isNotEmpty(matrizId)) {
		                searchConditions.add(EntityCondition.makeCondition("matrizId", EntityOperator.EQUALS, matrizId));
		            }		            
		            if (UtilValidate.isNotEmpty(nombreCog)) {
		                searchConditions.add(EntityCondition.makeCondition("nombreCog", EntityOperator.LIKE, nombreCog));
		            }
		            if (UtilValidate.isNotEmpty(cog)) {
		                searchConditions.add(EntityCondition.makeCondition("cog", EntityOperator.EQUALS, cog));
		            }
		            if (UtilValidate.isNotEmpty(tipoGasto)) {
		                searchConditions.add(EntityCondition.makeCondition("tipoGasto", EntityOperator.EQUALS, tipoGasto));
		            }
		            if (UtilValidate.isNotEmpty(cargo)) {
		                searchConditions.add(EntityCondition.makeCondition("cargo", EntityOperator.EQUALS, cargo));
		            }
		            if (UtilValidate.isNotEmpty(abono)) {
		                searchConditions.add(EntityCondition.makeCondition("abono", EntityOperator.EQUALS, abono));
		            }
		            
		            
					List<String> orderBy = UtilMisc.toList("matrizEgresoId");
					Debug.logInfo("search conditions egre : " + EntityCondition.makeCondition(searchConditions, EntityOperator.AND).toString(), MODULE);
					EntityListBuilder VistaMatrizListBuilder = null;
					PageBuilder<VistaMatrizEgreso> pageBuilder = null;			

					
					VistaMatrizListBuilder = new EntityListBuilder(
							ledgerRepository, VistaMatrizEgreso.class,EntityCondition.makeCondition(searchConditions,EntityOperator.AND), null, orderBy);					
					pageBuilder = new PageBuilder<VistaMatrizEgreso>() 
					{	public List<Map<String, Object>> build(List<VistaMatrizEgreso> page) throws Exception 
						{	List<Map<String, Object>> newPage = FastList.newInstance();
							for (VistaMatrizEgreso matrizEgreso : page) 
							{	Map<String, Object> newRow = FastMap.newInstance();
								newRow.putAll(matrizEgreso.toMap());		
								
								if(matrizEgreso.get("cargo") != null && !matrizEgreso.get("cargo").equals(""))
								{	GlAccount glAccountCargo = ledgerRepository.findOneCache(GlAccount.class, ledgerRepository.map(GlAccount.Fields.glAccountId, matrizEgreso.get("cargo")));
		                        	newRow.put("cuentaCargo", glAccountCargo.get(GlAccount.Fields.accountName.name()));
								}
		                        
								if(matrizEgreso.get("abono") != null && !matrizEgreso.get("abono").equals(""))
								{	GlAccount glAccountAbono = ledgerRepository.findOneCache(GlAccount.class, ledgerRepository.map(GlAccount.Fields.glAccountId, matrizEgreso.get("abono")));
		                        	newRow.put("cuentaAbono", glAccountAbono.get(GlAccount.Fields.accountName.name()));
								}								
		                        newPage.add(newRow);
							}
							return newPage;
						}
					};
					VistaMatrizListBuilder.setPageBuilder(pageBuilder);
					ac.put("VistaMatrizListBuilder", VistaMatrizListBuilder);
		            
	        	}
	        	else if(tipoMatriz.equals("INGRESO"))
	        	{	List<EntityCondition> searchConditions = new FastList<EntityCondition>();
		           
		            if (UtilValidate.isNotEmpty(matrizId)) {
		                searchConditions.add(EntityCondition.makeCondition("matrizId", EntityOperator.EQUALS, matrizId));
		            }		            
		            if (UtilValidate.isNotEmpty(nombreCri)) {
		                searchConditions.add(EntityCondition.makeCondition("nombreCri", EntityOperator.LIKE, nombreCri));
		            }
		            if (UtilValidate.isNotEmpty(cri)) {
		                searchConditions.add(EntityCondition.makeCondition("cri", EntityOperator.EQUALS, cri));
		            }
		            if (UtilValidate.isNotEmpty(tipoGasto)) {
		                searchConditions.add(EntityCondition.makeCondition("tipoGasto", EntityOperator.EQUALS, tipoGasto));
		            }
		            if (UtilValidate.isNotEmpty(cargo)) {
		                searchConditions.add(EntityCondition.makeCondition("cargo", EntityOperator.EQUALS, cargo));
		            }
		            if (UtilValidate.isNotEmpty(abono)) {
		                searchConditions.add(EntityCondition.makeCondition("abono", EntityOperator.EQUALS, abono));
		            }
		            
		            List<String> orderBy = UtilMisc.toList("matrizIngresoId");
					Debug.logInfo("search conditions ingr : " + EntityCondition.makeCondition(searchConditions, EntityOperator.AND).toString(), MODULE);
					EntityListBuilder VistaMatrizListBuilder = null;
					PageBuilder<VistaMatrizIngreso> pageBuilder = null;			

					
					VistaMatrizListBuilder = new EntityListBuilder(
							ledgerRepository, VistaMatrizIngreso.class,EntityCondition.makeCondition(searchConditions,EntityOperator.AND), null, orderBy);					
					pageBuilder = new PageBuilder<VistaMatrizIngreso>() 
					{	public List<Map<String, Object>> build(List<VistaMatrizIngreso> page) throws Exception 
						{	List<Map<String, Object>> newPage = FastList.newInstance();
							for (VistaMatrizIngreso matrizIngreso : page) 
							{	Map<String, Object> newRow = FastMap.newInstance();
								newRow.putAll(matrizIngreso.toMap());		
								
								if(matrizIngreso.get("cargo") != null && !matrizIngreso.get("cargo").equals(""))
								{	GlAccount glAccountCargo = ledgerRepository.findOneCache(GlAccount.class, ledgerRepository.map(GlAccount.Fields.glAccountId, matrizIngreso.get("cargo")));
		                        	newRow.put("cuentaCargo", glAccountCargo.get(GlAccount.Fields.accountName.name()));
								}		                        
								if(matrizIngreso.get("abono") != null && !matrizIngreso.get("abono").equals(""))
								{	GlAccount glAccountAbono = ledgerRepository.findOneCache(GlAccount.class, ledgerRepository.map(GlAccount.Fields.glAccountId, matrizIngreso.get("abono")));
		                        	newRow.put("cuentaAbono", glAccountAbono.get(GlAccount.Fields.accountName.name()));
								}								
		                        newPage.add(newRow);
							}
							return newPage;
						}
					};
					VistaMatrizListBuilder.setPageBuilder(pageBuilder);
					ac.put("VistaMatrizListBuilder", VistaMatrizListBuilder);
	        	}
	        }
	 }
	 
	 public static void editarMatriz(Map<String, Object> context) throws GeneralException, ParseException {

	        final ActionContext ac = new ActionContext(context);	        

	        String tipoMatriz = ac.getParameter("acctgTagUsageTypeId");
	        String matrizId = ac.getParameter("matrizId");	
	        String cog = ac.getParameter("cog");
	        String cri = ac.getParameter("cri");
	        String nombreCog = ac.getParameter("nombreCog");
	        String nombreCri = ac.getParameter("nombreCri");
	        String tipoGasto = ac.getParameter("tipoGasto");
	        String cargo = ac.getParameter("cargo");
	        String abono = ac.getParameter("abono");	        	        

	        DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
	        final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();
	        
	        Delegator delegator = ac.getDelegator();
	        	        
	       
	        List<Map<String, Object>> tipoList = new FastList<Map<String, Object>>();
			Map<String, Object> tipos = new HashMap<String, Object>();
			tipos.put("acctgTagUsageTypeId", "EGRESO");
			tipoList.add(tipos);
			tipos = new HashMap<String, Object>();
			tipos.put("acctgTagUsageTypeId", "INGRESO");
			tipoList.add(tipos);                  
	        
	        ac.put("tipoList", tipoList);
	        	        
	        ac.put("tipoMatriz", tipoMatriz);
	        ac.put("matrizId", matrizId);
	        ac.put("nombreCog", nombreCog);
	        ac.put("nombreCri", nombreCri);
	        ac.put("cog", cog);
	        ac.put("cri", cri);
	        ac.put("tipoGasto", tipoGasto);
	        ac.put("cargo", cargo);
	        ac.put("abono", abono);
	        
	        List<GenericValue> listTipoMatriz = delegator.findAll("TipoMatriz");
	        ac.put("matrizList", listTipoMatriz);	        	    	       	        
	        
	        
	        if (tipoMatriz != null) 
	        {	if(tipoMatriz.equals("EGRESO"))
	        	{	List<EntityCondition> searchConditions = new FastList<EntityCondition>();
		           
		            if (UtilValidate.isNotEmpty(matrizId)) {
		                searchConditions.add(EntityCondition.makeCondition("matrizId", EntityOperator.EQUALS, matrizId));
		            }		            
		            if (UtilValidate.isNotEmpty(nombreCog)) {
		                searchConditions.add(EntityCondition.makeCondition("nombreCog", EntityOperator.LIKE, nombreCog));
		            }
		            if (UtilValidate.isNotEmpty(cog)) {
		                searchConditions.add(EntityCondition.makeCondition("cog", EntityOperator.EQUALS, cog));
		            }
		            if (UtilValidate.isNotEmpty(tipoGasto)) {
		                searchConditions.add(EntityCondition.makeCondition("tipoGasto", EntityOperator.EQUALS, tipoGasto));
		            }
		            if (UtilValidate.isNotEmpty(cargo)) {
		                searchConditions.add(EntityCondition.makeCondition("cargo", EntityOperator.EQUALS, cargo));
		            }
		            if (UtilValidate.isNotEmpty(abono)) {
		                searchConditions.add(EntityCondition.makeCondition("abono", EntityOperator.EQUALS, abono));
		            }
		            List<String> orderBy = UtilMisc.toList("matrizEgresoId");
		            Debug.logInfo("search conditions egre : " + EntityCondition.makeCondition(searchConditions, EntityOperator.AND).toString(), MODULE);	
	        	
	        	
		        	List<VistaMatrizEgreso> matrizEgreso = ledgerRepository.findList(VistaMatrizEgreso.class, searchConditions, orderBy);
		        	List<Map<String, Object>> detalleMatriz = new FastList<Map<String, Object>>();

		        	for (VistaMatrizEgreso s : matrizEgreso) 
		        	{	Map<String, Object> map = s.toMap();
						
		        		VistaMatrizEgreso glAccount = matrizEgreso.get(0);
		        		if(glAccount.getString("cargo") != null && !glAccount.getString("cargo").equals(""))
		        		{	String cargoGl = glAccount.getString("cargo");
		        			GlAccount glAccountCargo = ledgerRepository.findOneCache(GlAccount.class, ledgerRepository.map(GlAccount.Fields.glAccountId, cargoGl));
		        			map.put("cuentaCargo", glAccountCargo.get(GlAccount.Fields.accountName.name()));
		        		}
		        		if(glAccount.getString("abono") != null && !glAccount.getString("abono").equals(""))
		        		{	String abonoGl = glAccount.getString("abono");		        				                       
		        			GlAccount glAccountAbono = ledgerRepository.findOneCache(GlAccount.class, ledgerRepository.map(GlAccount.Fields.glAccountId, abonoGl));
		        			map.put("cuentaAbono", glAccountAbono.get(GlAccount.Fields.accountName.name()));
		        		}
		        		detalleMatriz.add(map);
			        }
		    		ac.put("detalleMatriz", detalleMatriz);           
	        	}     	
	        	else
	        	{	List<EntityCondition> searchConditions = new FastList<EntityCondition>();
		           
		            if (UtilValidate.isNotEmpty(matrizId)) {
		                searchConditions.add(EntityCondition.makeCondition("matrizId", EntityOperator.EQUALS, matrizId));
		            }		            
		            if (UtilValidate.isNotEmpty(nombreCri)) {
		                searchConditions.add(EntityCondition.makeCondition("nombreCri", EntityOperator.LIKE, nombreCri));
		            }
		            if (UtilValidate.isNotEmpty(cri)) {
		                searchConditions.add(EntityCondition.makeCondition("cri", EntityOperator.EQUALS, cri));
		            }
		            if (UtilValidate.isNotEmpty(tipoGasto)) {
		                searchConditions.add(EntityCondition.makeCondition("tipoGasto", EntityOperator.EQUALS, tipoGasto));
		            }
		            if (UtilValidate.isNotEmpty(cargo)) {
		                searchConditions.add(EntityCondition.makeCondition("cargo", EntityOperator.EQUALS, cargo));
		            }
		            if (UtilValidate.isNotEmpty(abono)) {
		                searchConditions.add(EntityCondition.makeCondition("abono", EntityOperator.EQUALS, abono));
		            }
		            List<String> orderBy = UtilMisc.toList("cri");
		            Debug.logInfo("search conditions ingr : " + EntityCondition.makeCondition(searchConditions, EntityOperator.AND).toString(), MODULE);	
	        	
	        	
		        	List<VistaMatrizIngreso> matrizIngreso = ledgerRepository.findList(VistaMatrizIngreso.class, searchConditions, orderBy);
		        	List<Map<String, Object>> detalleMatriz = new FastList<Map<String, Object>>();
	
		        	for (VistaMatrizIngreso s : matrizIngreso) 
		        	{	Map<String, Object> map = s.toMap();
						
			        	VistaMatrizIngreso glAccount = matrizIngreso.get(0);
			        	if(glAccount.getString("cargo") != null && !glAccount.getString("cargo").equals(""))
		        		{	String cargoGl = glAccount.getString("cargo");
		        			GlAccount glAccountCargo = ledgerRepository.findOneCache(GlAccount.class, ledgerRepository.map(GlAccount.Fields.glAccountId, cargoGl));
		        			map.put("cuentaCargo", glAccountCargo.get(GlAccount.Fields.accountName.name()));
		        		}
			        	if(glAccount.getString("abono") != null && !glAccount.getString("abono").equals(""))
			        	{	String abonoGl = glAccount.getString("abono");
			        		GlAccount glAccountAbono = ledgerRepository.findOneCache(GlAccount.class, ledgerRepository.map(GlAccount.Fields.glAccountId, abonoGl));
			        		map.put("cuentaAbono", glAccountAbono.get(GlAccount.Fields.accountName.name()));
			        	}
			        	String cogGl = "";
			        	if(glAccount.getString("cog") != null && !glAccount.getString("cog").equals(""))
			        	{	cogGl = glAccount.getString("cog");
			        	}			        		                
		        		
		        		EntityCondition condicion = EntityCondition.makeCondition(EntityOperator.AND,
								   					EntityCondition.makeCondition("enumTypeId", EntityOperator.EQUALS, "CL_COG"),
								   					EntityCondition.makeCondition("sequenceId", EntityOperator.EQUALS, cogGl));					

		        		List<GenericValue> enumeration = delegator.findByCondition("Enumeration", condicion, UtilMisc.toList("description"), null);
		        			
		        		if(!enumeration.isEmpty())
		        		{	Iterator<GenericValue> enumerationIter = enumeration.iterator();
							while (enumerationIter.hasNext()) 
							{	GenericValue item = enumerationIter.next();
								map.put("description", item.getString("description"));								
							}
		        		}
		        		detalleMatriz.add(map);
		        		
			        }
		    		ac.put("detalleMatriz", detalleMatriz);
	        		
	        	}	        	
	        }
	 }
	 
	 
}
