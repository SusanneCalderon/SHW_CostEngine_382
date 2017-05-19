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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.*;
import org.compiere.model.*;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
 

/**
 *	Create Checks from Payment Selection Line
 *	
 *  @author Jorg Janke
 *  @version $Id: PaySelectionCreateCheck.java,v 1.2 2006/07/30 00:51:01 jjanke Exp $
 */
public class YearEndClosingBalanceSheet extends SvrProcess
{
	private int			p_AD_Org_ID = 0;
	private int			p_AD_Client_ID = 0;
	private java.sql.Timestamp	p_date_acct = null;
	private java.sql.Timestamp	p_date_acctNewYear = null;
	private int 				p_C_AcctSchema_ID = 0;
	private int 				cat_id = 0;
	private MJournal 			journal = null;
	private MJournal 			journalNewYear = null;
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			p_AD_Client_ID = Env.getAD_Client_ID(getCtx());
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("DateAcct"))
				p_date_acct = (java.sql.Timestamp)para[i].getParameter();
			else if (name.equals("DateAcctNewYear"))
					p_date_acctNewYear = (java.sql.Timestamp)para[i].getParameter();
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
		
		//Batch fuer Abschluss des alten Jahres
		MJournalBatch journalBatch = new MJournalBatch(getCtx(), 0, get_TrxName());
		if (journalBatch == null)
			return "";
		MDocType doctype = new Query(getCtx(), MDocType.Table_Name, 
					" yearendclosing = 'Y'", get_TrxName())
			.setClient_ID()
			.firstOnly();
			
			if (doctype == null)
				return "Falta tipo de documento de Cierre";					
		if (!SetCategory())
			return "Falta una categoría contable para el cierre del año";
		BigDecimal saldo = Env.ZERO;
		journalBatch.setDateAcct(p_date_acct);
		journalBatch.setDateDoc(p_date_acct);
		journalBatch.setDescription("Cierre  " + p_date_acct);
		journalBatch.setC_DocType_ID(doctype.getC_DocType_ID());
		journalBatch.setDocumentNo("" + journalBatch.get_ID());
		journalBatch.setGL_Category_ID(cat_id);
		journalBatch.setControlAmt(Env.ZERO);
		journalBatch.setC_Currency_ID(as.getC_Currency_ID());
		journalBatch.save();
		
		//Batch fuer die eroeffnungsbilanz des neuen Jahres

		MJournalBatch journalBatchNewYear = new MJournalBatch(getCtx(), 0, get_TrxName());
		if (journalBatchNewYear == null)
			return "";
		journalBatchNewYear.setDateAcct(p_date_acctNewYear);
		journalBatchNewYear.setDateDoc(p_date_acctNewYear);
		journalBatchNewYear.setDescription("Inicio  " + p_date_acctNewYear);
		journalBatchNewYear.setC_DocType_ID(doctype.getC_DocType_ID());
		journalBatchNewYear.setDocumentNo("" + journalBatchNewYear.get_ID());
		journalBatchNewYear.setGL_Category_ID(cat_id);
		journalBatchNewYear.setControlAmt(Env.ZERO);
		journalBatchNewYear.setC_Currency_ID(as.getC_Currency_ID());
		journalBatchNewYear.save();
		journal = new MJournal(journalBatch);
		if (journal == null)
			return "";
		journalNewYear = new MJournal(journalBatchNewYear);
			if (journal == null)
			return "";		
		journal.setC_Currency_ID(as.getC_Currency_ID());
		journal.setC_AcctSchema_ID(as.get_ID());
		journal.setC_ConversionType_ID(114);
		journal.setDescription("Cierre del año");
		journal.setDocumentNo(DB.getDocumentNo(getAD_Client_ID(), MJournal.Table_Name, get_TrxName()));
		journal.setGL_Category_ID(cat_id);
		journal.save();		
		journalNewYear.setC_Currency_ID(as.getC_Currency_ID());
		journalNewYear.setC_AcctSchema_ID(as.get_ID());
		journalNewYear.setC_ConversionType_ID(114);
		journalNewYear.setDescription("Inicio del año");
		journalNewYear.setDocumentNo(DB.getDocumentNo(getAD_Client_ID(), MJournal.Table_Name, get_TrxName()));
		journalNewYear.setGL_Category_ID(cat_id);
		journalNewYear.save();		
		String whereclause = "accounttype in ('A', 'L', 'O')  and ISSUMMARY = 'N' ";
		List<MElementValue> accounts = new Query(getCtx(), MElementValue.Table_Name, 
				whereclause, get_TrxName())
		.setOrderBy("value")
		.list();
		for (MElementValue el : accounts) 
		{			
			MAccount acc = CreateAccount(el.getC_ElementValue_ID());
			saldo = SearchSaldo(el.getC_ElementValue_ID(), acc.isActiva());			
			if (saldo.equals(Env.ZERO))
				continue;
			if (!CreateLines(acc.get_ID(), journal,journalNewYear, el.getC_ElementValue_ID(), saldo, acc.isActiva()))
				return "";	
		}		
		return "No. de Aplicacion: " + journalBatch.getDescription();		
	}	//	doIt
	
	private MAccount CreateAccount(int c_element_value_ID)
	{   MAccount acc = MAccount.get(getCtx(), p_AD_Client_ID,
		p_AD_Org_ID, p_C_AcctSchema_ID, 
		c_element_value_ID, 0,
		0, 0, 0, 
		0, 0, 0, 
		0, 0, 0,
		0, 0, 0,0);
		return acc;
		
	}	
	private Boolean CreateLines(int account_ID, MJournal journal, MJournal journalNeu,
			int c_elementvalue_ID, BigDecimal saldo, Boolean isActiva)
	{
		if(isActiva)
		{
			MJournalLine DR = new MJournalLine(journalNeu);
			MJournalLine CR = new MJournalLine(journal);
			if (saldo.signum() == 0)
				return true;
			DR.setC_ValidCombination_ID(account_ID);
			DR.setAmtSourceDr(saldo);
			DR.save();
			CR.setC_ValidCombination_ID(account_ID);
			CR.setAmtSourceCr(saldo);
			CR.save();
			
		}
		else
		{
			MJournalLine DR = new MJournalLine(journal);
			MJournalLine CR = new MJournalLine(journalNeu);
			if (saldo.signum() == 0)
				return true;
			DR.setC_ValidCombination_ID(account_ID);
			DR.setAmtSourceDr(saldo);
			DR.save();
			CR.setC_ValidCombination_ID(account_ID);
			CR.setAmtSourceCr(saldo);
			CR.save();			
		}
		return true;
	}
	
	private BigDecimal SearchSaldo(int c_elementvalue_ID, Boolean isActiva)
	{
		String sql = " select coalesce(sum(amtacctDr - amtacctCr), 0) from fact_acct f" +
			" where dateacct between Firstof( " + DB.TO_DATE(p_date_acct) +
			", 'YY' ) and " + DB.TO_DATE(p_date_acct) + 
			" and f.ad_client_ID = " + Env.getAD_Client_ID(getCtx()) + 
			" and f.account_ID = " + c_elementvalue_ID;
		BigDecimal saldo = DB.getSQLValueBD(get_TrxName(), sql);
		if (!isActiva)
			saldo = saldo.negate();
		return saldo;
	}
	private Boolean SetCategory()
	{
		MGLCategory cat = new Query(getCtx(), MGLCategory.Table_Name, 
				" yearendclosing = 'Y'", get_TrxName())
		.setClient_ID()
		.firstOnly();		
		if (cat == null)
			return false;
		cat_id = cat.getGL_Category_ID();
		return true;
	}
		
}	//	Z_SHW_YearEndClosingBslsnceSheet
