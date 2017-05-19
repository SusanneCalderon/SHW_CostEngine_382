/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                        *
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
package org.adempiere.process;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;


import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.model.MAccount;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MDocType;
import org.compiere.model.MElementValue;
import org.compiere.model.MGLCategory;
import org.compiere.model.MJournal;
import org.compiere.model.MJournalBatch;
import org.compiere.model.MJournalLine;
import org.compiere.model.Query;
import org.compiere.util.DB;
import org.compiere.util.Env;
 

/**
 *	Balances expense/income account to a selected account
 *	
 *  @author Susanne Calderon
 *  @version $Id: yearendclosing.java,v 1.2 2011/01/20$
 */
public class YearEndClosing extends SvrProcess
{
	private int					p_c_Elementvalue_ID = 0;
	private int					p_AD_Org_ID = 0;
	private int					p_AD_Client_ID = 0;
	private java.sql.Timestamp	p_date_acct = null;
	private int					c_vc_result_ID = 0;
	private int 				p_C_AcctSchema_ID = 0;
	private int 				cat_id = 0;
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			p_AD_Client_ID = Env.getAD_Client_ID(getCtx());

			p_AD_Org_ID = Env.getAD_Org_ID(getCtx());
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("C_ElementValue_ID"))
				p_c_Elementvalue_ID = para[i].getParameterAsInt();
			else if (name.equals("DateAcct"))
				p_date_acct = (java.sql.Timestamp)para[i].getParameter();
			else if (name.equals("C_AcctSchema_ID"))
				p_C_AcctSchema_ID = ((BigDecimal)para[i].getParameter()).intValue();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
			
		}
	}	//	prepare

	/**
	 *  Perrform process.
	 *  @return Message (clear text)
	 *  @throws Exception if not successful
	 */
	protected String doIt () throws Exception
	{
		MAcctSchema as = new MAcctSchema(getCtx(), p_C_AcctSchema_ID, get_TrxName());
		c_vc_result_ID = CreateAccount(p_c_Elementvalue_ID);
		if (c_vc_result_ID == 0)
			return "";
		MJournalBatch journalBatch = new MJournalBatch(getCtx(), 0, get_TrxName());
		if (journalBatch == null)
			return "No fue posible crear la partida";		
		
		MDocType doctype = new Query(getCtx(), MDocType.Table_Name, 
				" isyearendclosing = 'Y'", get_TrxName())	
		.setClient_ID()
		.firstOnly();
		
		if (doctype == null)
			return "Falta tipo de documento de Cierre";		
		SetCategory();
		BigDecimal saldo = Env.ZERO;
		journalBatch.setAD_Org_ID(p_AD_Org_ID);
		journalBatch.setDateAcct(p_date_acct);
		journalBatch.setDateDoc(p_date_acct);
		journalBatch.setDescription("Cierre  " + p_date_acct);
		journalBatch.setC_DocType_ID(doctype.getC_DocType_ID());
		journalBatch.setDocumentNo("Cierre_" + p_date_acct);
		journalBatch.setGL_Category_ID(cat_id);
		journalBatch.setControlAmt(Env.ZERO);
		journalBatch.setC_Currency_ID(as.getC_Currency_ID());
		journalBatch.save();
		
		String whereclause = "accounttype in ('E', 'R')  and ISSUMMARY = 'N' ";
		List<MElementValue> accounts = new Query(getCtx(), MElementValue.Table_Name, 
				whereclause, get_TrxName())
		.setOrderBy("value")
		.list();
		for (MElementValue el : accounts) 
		{			
			saldo = SearchSaldo(el.getC_ElementValue_ID());
			if (saldo.equals(Env.ZERO))
				continue;
			CreateJournal(el.getC_ElementValue_ID(), journalBatch, as, saldo);
		}
		return "No. de Aplicacion: " + journalBatch.getDocumentNo();		
	}	//	doIt
	
	private int CreateAccount(int c_element_value_ID)
	{   
		
		
		MAccount acc = MAccount.get(getCtx(), p_AD_Client_ID,
		p_AD_Org_ID, p_C_AcctSchema_ID, 
		c_element_value_ID, 0,
		0, 0, 0, 
		0, 0, 0, 
		0, 0, 0,
		0, 0, 0,0);
		if (acc == null)
			return 0;
		return acc.get_ID();
		
	}
	
	private Boolean CreateJournal(int c_elementvalue_ID, MJournalBatch journalBatch, 
			MAcctSchema as, BigDecimal saldo)
	{
		MJournal journal = new MJournal(journalBatch);
		if (journal == null)
			return false;
		int acc_ID = CreateAccount(c_elementvalue_ID);
		MElementValue el = new MElementValue(getCtx(), c_elementvalue_ID, get_TrxName() );
		
		journal.setC_Currency_ID(as.getC_Currency_ID());
		journal.setC_AcctSchema_ID(as.get_ID());
		journal.setC_ConversionType_ID(114);
		journal.setDescription("Cierre " + el.getValue() + " " + el.getName());
		journal.setDocumentNo(DB.getDocumentNo(getAD_Client_ID(), MJournal.Table_Name, get_TrxName()));
		journal.setGL_Category_ID(cat_id);
		journal.save();
		if (!CreateLines(acc_ID, journal, c_elementvalue_ID, saldo))
			return false;				
		return true;
	}
	
	private Boolean CreateLines(int account_ID, MJournal journal, 
			int c_elementvalue_ID, BigDecimal saldo)
	{
		MElementValue el = new MElementValue(getCtx(), c_elementvalue_ID, get_TrxName());
		String accounttype = el.getAccountType();
		MJournalLine DR = new MJournalLine(journal);
		MJournalLine CR = new MJournalLine(journal);
		if (saldo.signum() == 0)
			return true;
		if (accounttype.equals('R'))
		{
			DR.setC_ValidCombination_ID(c_vc_result_ID);
			DR.setAmtSourceDr(saldo.negate());
			DR.save();
			CR.setC_ValidCombination_ID(account_ID);
			CR.setAmtSourceCr(saldo.negate());
			CR.save();
		}
		else
		{
			DR.setC_ValidCombination_ID(c_vc_result_ID);
			DR.setAmtSourceDr(saldo);
			DR.save();
			CR.setC_ValidCombination_ID(account_ID);
			CR.setAmtSourceCr(saldo);
			CR.save();
		}
		return true;
	}
	
	private BigDecimal SearchSaldo(int c_elementvalue_ID)
	{
		String sql = " select coalesce(sum(amtacctDr - amtacctCr), 0) from fact_acct f" +
			" where dateacct between Firstof( " + DB.TO_DATE(p_date_acct) +
			", 'YY' ) and " + DB.TO_DATE(p_date_acct) + 
			" and f.ad_client_ID = " + Env.getAD_Client_ID(getCtx()) + 
			" and f.account_ID = " + c_elementvalue_ID;
		BigDecimal saldo = DB.getSQLValueBD(get_TrxName(), sql);
		return saldo;
	}
	

	private Boolean SetCategory()
	{		
		MGLCategory cat = new Query(getCtx(), MGLCategory.Table_Name, 
				" isyearendclosing = 'Y'", get_TrxName())
		.setClient_ID()
		.firstOnly();		
		if (cat == null)
			return false;
		cat_id = cat.getGL_Category_ID();
		return true;
	}
}	//	A_SHW_YearEndClosing
