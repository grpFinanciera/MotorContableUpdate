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
package com.opensourcestrategies.financials.transactions;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;

import org.ofbiz.base.util.GeneralException;
import org.opentaps.base.entities.Enumeration;
import org.opentaps.base.entities.NivelPresupuestal;
import org.opentaps.domain.DomainsDirectory;
import org.opentaps.domain.ledger.LedgerRepositoryInterface;
import org.opentaps.foundation.action.ActionContext;


/**
 * TransactionActions - Java Actions for Transactions.
 */
public class TransactionsFindIng {

    private static final String MODULE = TransactionActions.class.getName();

    
//Metodo que devuelve la lista que  se  genera el mapa para la exportacion a Excel    
    public static void findExcelTag(Map<String, Object> context) throws GeneralException, ParseException {
        final ActionContext ac = new ActionContext(context);
        DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
        final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();
//////////////////////////////////////////////////////////        ///
        List<NivelPresupuestal> nivelList = ledgerRepository.findAll(NivelPresupuestal.class);
        List<Map<String, Object>> nivelLists = new FastList<Map<String, Object>>();
        for (NivelPresupuestal s : nivelList) {
            Map<String, Object> map = s.toMap();
            nivelLists.add(map);
        }
        ac.put("nivelLists", nivelLists);
        
        List<Enumeration> enumList = ledgerRepository.findAll(Enumeration.class);
        List<Map<String, Object>> Enumlists = new FastList<Map<String, Object>>();
        for (Enumeration s : enumList) {
            Map<String, Object> map = s.toMap();
            Enumlists.add(map);
        }
        ac.put("Enumlists", Enumlists);
        
    }
    
}