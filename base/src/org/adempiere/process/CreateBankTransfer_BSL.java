/******************************************************************************
 * Product: ADempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 2006-2016 ADempiere Foundation, All Rights Reserved.         *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * or via info@adempiere.net or http://www.adempiere.net/license.html         *
 *****************************************************************************/

package org.adempiere.process;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Properties;

import org.compiere.model.MBankStatementLine;
import org.compiere.model.MPayment;
import org.compiere.model.MPaymentBatch;
import org.compiere.util.DB;
import org.compiere.util.Env;

/** Generated Process for (groovy:CreateBankTransfer_BSL)
 *  @author ADempiere (generated) 
 *  @version Release 3.9.0
 */
public class CreateBankTransfer_BSL extends CreateBankTransfer_BSLAbstract
{
	@Override
	protected void prepare()
	{
		super.prepare();
	}

	@Override
	protected String doIt() throws Exception
	{
		Properties A_Ctx = getCtx();
		String A_TrxName = get_TrxName();
		int A_Record_ID = getRecord_ID();
		
		int P_To_C_BankAccount_ID = getBankAccountToId();
		int P_From_C_BankAccount_ID = getBankAccountFromId();
		Timestamp P_StatementDate = getStatementdate();
		Timestamp P_DateAcct = getAccountDate();
		BigDecimal P_Amount = getAmount();
		String P_TenderType = getTendertype();
		String P_Description = getDescription();
		int P_C_ConversionType_ID = 0;
		int P_C_BPartner_ID = getBusinessPartnerId();
		int P_C_Currency_ID = getCurrencyId();
		int P_C_Charge_ID = getChargeId();
		int P_AD_Org_ID = getOrganizationId();
		String P_CheckNo = getCheckNo();
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
        MPaymentBatch pBatch = new MPaymentBatch(A_Ctx, 0 ,  A_TrxName);
        String description = "Transferencia";
        String name = "Transferencia";
        pBatch.setName("Transferencia");
        pBatch.saveEx();
        MPayment paymentBankFrom = new MPayment(A_Ctx, 0 ,  A_TrxName);
        paymentBankFrom.setC_BankAccount_ID(P_From_C_BankAccount_ID);
        paymentBankFrom.setC_DocType_ID(false);
        String value = DB.getDocumentNo(paymentBankFrom.getC_DocType_ID(),A_TrxName, false,  paymentBankFrom);
        paymentBankFrom.setDocumentNo(value);
       // paymentBankFrom.setDocumentNo(P_DocumentNo);
        
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
        paymentBankFrom.setCheckNo(P_CheckNo);
        if (P_AD_Org_ID != 0)
            paymentBankFrom.setAD_Org_ID(P_AD_Org_ID);
        paymentBankFrom.setC_PaymentBatch_ID(pBatch.getC_PaymentBatch_ID());
        paymentBankFrom.saveEx();
        description = description + " desde" +  paymentBankFrom.getC_BankAccount().getAccountNo();
        paymentBankFrom.processIt(MPayment.DOCACTION_Complete);
        paymentBankFrom.saveEx();
        
        MPayment paymentBankTo = new MPayment(A_Ctx, 0 ,  A_TrxName);
        paymentBankTo.setC_BankAccount_ID(P_To_C_BankAccount_ID);
        paymentBankTo.setC_DocType_ID(true);
        value = DB.getDocumentNo(paymentBankTo.getC_DocType_ID(),A_TrxName, false,  paymentBankTo);
        paymentBankTo.setDocumentNo(value);        
        paymentBankTo.setC_PaymentBatch_ID(pBatch.getC_PaymentBatch_ID());
      //  paymentBankTo.setDocumentNo(P_DocumentNo);
        paymentBankTo.setDateAcct(P_DateAcct);
        paymentBankTo.setDateTrx(P_StatementDate);
        paymentBankTo.setTenderType(P_TenderType);
        paymentBankTo.setDescription(P_Description);
        paymentBankTo.setC_BPartner_ID (P_C_BPartner_ID);
        paymentBankTo.setC_Currency_ID(P_C_Currency_ID);        
        paymentBankTo.setCheckNo(P_CheckNo);
        if (P_C_ConversionType_ID > 0)
            paymentBankFrom.setC_ConversionType_ID(P_C_ConversionType_ID);    
        paymentBankTo.setPayAmt(P_Amount);
        paymentBankTo.setOverUnderAmt(Env.ZERO);
        paymentBankTo.setC_Charge_ID(P_C_Charge_ID);
        if (P_AD_Org_ID != 0)
            paymentBankTo.setAD_Org_ID(P_AD_Org_ID);
        paymentBankTo.saveEx();
        description = description + " a " +  paymentBankTo.getC_BankAccount().getAccountNo();
        paymentBankTo.processIt(MPayment.DOCACTION_Complete);
        paymentBankTo.saveEx();
        
        pBatch.setName(description);        
        description = description + " Monto:" +  paymentBankTo.getPayAmt();
        pBatch.set_ValueOfColumn("Description", description);
        pBatch.setProcessingDate(P_StatementDate);
        pBatch.saveEx();
        MBankStatementLine bsl = new MBankStatementLine(A_Ctx, A_Record_ID ,  A_TrxName);
        bsl.setPayment( paymentBankFrom);
        bsl.saveEx();
        m_created++;  

        
        return paymentBankFrom.getDocumentNo();
    
	}
}