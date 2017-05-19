/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/

package org.shw.process;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.logging.Level;

import org.compiere.apps.AEnv;
import org.compiere.apps.AWindow;
import org.compiere.model.MPayment;
import org.compiere.model.MProductCategory;
import org.compiere.model.MQuery;
import org.compiere.model.MSession;
import org.compiere.model.MTable;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;






/**
 *  Creates Payment from c_invoice, including Aging
 *
 *  @author Susanne Calderon
 */

public class InvoicePay  extends SvrProcess

{	
	int P_C_BankAccount_ID = 0;
	String P_TenderType = "";	
    Timestamp P_StatementDate = null;
    Timestamp P_DateAcct = null;
    BigDecimal P_Amount = Env.ZERO;
    @Override    
    protected void prepare()
    {  
		for (ProcessInfoParameter para : getParameter())
		{
			String name = para.getParameterName();
			if (para.getParameter() == null)
				;
			if (name.equals(MPayment.COLUMNNAME_C_BP_BankAccount_ID))
				P_C_BankAccount_ID = para.getParameterAsInt();
			if (name.equals(MPayment.COLUMNNAME_TenderType))
				P_TenderType = para.getParameterAsString();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}


    }
    
      
    @Override
    protected String doIt() throws Exception
    {
        return "";}
    
}
