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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import org.compiere.model.MBankAccount;
import org.compiere.model.MInvoice;
import org.compiere.model.MPayment;
import org.compiere.model.Query;
import org.eevolution.grid.Browser;


import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;






/**
 *  Creates BankStatement from cashier
 *
 *  @author Susanne Calderon
 */

public class BankStatement_CreateBankTransfer  extends SvrProcess

{	
	private int P_To_C_BankAccount_ID = 0;
	private int P_From_C_BankAccount_ID = 0;
	private int P_C_BPartner_ID = 0;
	private int P_C_Currency_ID = 0;
	private int P_C_ConversionType_ID = 0;
	private int P_C_Charge_ID = 0;
	private int P_AD_Org_ID = 0;
	private int P_C_BankStatement_ID = 0;
	
	private Timestamp P_StatementDate = null;
	private Timestamp P_DateAcct = null;
	

    @Override    
    protected void prepare()
    {  


    }
    
      
    @Override
    protected String doIt() throws Exception
    {
    	/*
    	        if (P_To_C_BankAccount_ID == P_From_C_BankAccount_ID)
    	            return "Banco origen = banco destino" ;
    	        
    	        
    	    
    	        if (P_Amount.compareTo(new BigDecimal(0)) == 0)
    	            return "El monto es cero";

    	        //    Login Date
    	        if (P_StatementDate == null)
    	            P_StatementDate = Env.getContextAsDate(A_Ctx, "#Date");
    	        if (P_StatementDate == null)
    	            P_StatementDate = new Timestamp(System.currentTimeMillis());            

    	        if (P_DateAcct == null)
    	            P_DateAcct = P_StatementDate;


    	        int         m_created = 0;
    	        MBankAccount mBankFrom = new MBankAccount(A_Ctx,P_From_C_BankAccount_ID, A_TrxName);
    	        MBankAccount mBankTo = new MBankAccount(A_Ctx,P_To_C_BankAccount_ID, A_TrxName); 
    	        String description = "Transferencia directa de " + P_Amount.toString()+ " desde " + mBankFrom.getC_Bank().getName() + 
    	                        " " + mBankFrom.getAccountNo() + " a " + mBankTo.getC_Bank().getName() + " " + mBankTo.getAccountNo();
    	        MPayment paymentBankFrom = new MPayment(A_Ctx, 0 ,  A_TrxName);
    	        paymentBankFrom.setC_BankAccount_ID(mBankFrom.getC_BankAccount_ID());
    	        paymentBankFrom.setC_DocType_ID(false);
    	        String value = DB.getDocumentNo(paymentBankFrom.getC_DocType_ID(),A_TrxName, false,  paymentBankFrom);
    	        paymentBankFrom.setDocumentNo(value);
    	        
    	        paymentBankFrom.setDateAcct(P_DateAcct);
    	        paymentBankFrom.setDateTrx(P_StatementDate);
    	        paymentBankFrom.setTenderType(P_TenderType);
    	        paymentBankFrom.setDescription(P_Description);
    	        paymentBankFrom.setC_BPartner_ID (P_C_BPartner_ID);
    	        paymentBankFrom.setC_Currency_ID(P_C_Currency_ID);
    	        if (P_C_ConversionType_ID > 0)
    	        paymentBankFrom.setC_ConversionType_ID(P_C_ConversionType_ID);    
    	        paymentBankFrom.setPayAmt(P_Amount);
    	        paymentBankFrom.setOverUnderAmt(Env.ZERO);
    	        paymentBankFrom.setC_Charge_ID(P_C_Charge_ID);
    	        if (P_AD_Org_ID != null)
    	            paymentBankFrom.setAD_Org_ID(P_AD_Org_ID);
    	        paymentBankFrom.setDescription(description);
    	        paymentBankFrom.saveEx();
    	        paymentBankFrom.processIt(MPayment.DOCACTION_Complete);
    	        paymentBankFrom.saveEx();
    	        
    	        MPayment paymentBankTo = new MPayment(A_Ctx, 0 ,  A_TrxName);
    	        paymentBankTo.setC_BankAccount_ID(mBankTo.getC_BankAccount_ID());
    	        paymentBankTo.setC_DocType_ID(true);
    	        value = DB.getDocumentNo(paymentBankTo.getC_DocType_ID(),A_TrxName, false,  paymentBankTo);
    	        paymentBankTo.setDocumentNo(value);
    	        paymentBankTo.setDateAcct(P_DateAcct);
    	        paymentBankTo.setDateTrx(P_StatementDate);
    	        paymentBankTo.setTenderType(P_TenderType);
    	        paymentBankTo.setDescription(P_Description);
    	        paymentBankTo.setC_BPartner_ID (P_C_BPartner_ID);
    	        paymentBankTo.setC_Currency_ID(P_C_Currency_ID);        
    	        paymentBankTo.setDescription(description);
    	        if (P_C_ConversionType_ID > 0)
    	            paymentBankFrom.setC_ConversionType_ID(P_C_ConversionType_ID);    
    	        paymentBankTo.setPayAmt(P_Amount);
    	        paymentBankTo.setOverUnderAmt(Env.ZERO);
    	        paymentBankTo.setC_Charge_ID(P_C_Charge_ID);
    	        if (P_AD_Org_ID != null)
    	            paymentBankTo.setAD_Org_ID(P_AD_Org_ID);
    	        paymentBankTo.saveEx();
    	        paymentBankTo.processIt(MPayment.DOCACTION_Complete);
    	        paymentBankTo.set_ValueOfColumn("c_payment_From_ID",  paymentBankFrom.getC_Payment_ID());
    	        paymentBankFrom.set_ValueOfColumn("c_payment_To_ID",  paymentBankTo.getC_Payment_ID());
    	        paymentBankTo.saveEx();
    	        paymentBankFrom.saveEx();
    	        m_created++;
    	        MBankStatement bs = new MBankStatement(A_Ctx, P_C_BankStatement_ID ,  A_TrxName);
    	        MBankStatementLine bsl = new MBankStatementLine(bs);
    	        bsl.setC_Payment_ID(paymentBankFrom.getC_Payment_ID());
    	        bsl.setStmtAmt(paymentBankFrom.getPayAmt().negate());
    	        bsl.setC_Currency_ID(P_C_Currency_ID);
    	        bsl.setTrxAmt(bsl.getStmtAmt());
    	        bsl.saveEx();

    	    */
    	        return "";}
    
}
