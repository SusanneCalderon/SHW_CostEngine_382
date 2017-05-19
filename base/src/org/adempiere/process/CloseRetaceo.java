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

package org.adempiere.process;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import org.adempiere.engine.CostEngineFactory;
import org.compiere.apps.AEnv;
import org.compiere.apps.AWindow;
import org.compiere.model.MAccount;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MCampaign;
import org.compiere.model.MCostDetail;
import org.compiere.model.MDocType;
import org.compiere.model.MInOut;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MJournal;
import org.compiere.model.MJournalBatch;
import org.compiere.model.MJournalLine;
import org.compiere.model.MLandedCostAllocation;
import org.compiere.model.MPeriod;
import org.compiere.model.MQuery;
import org.compiere.model.MSession;
import org.compiere.model.MTable;
import org.compiere.model.ProductCost;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;
/**
 *  Creates Payment from c_invoice, including Aging
 *
 *  @author Susanne Calderon
 */

public class CloseRetaceo extends SvrProcess
{
    int c_campaign_ID = 0;
    int p_C_AcctSchema_ID;
    MJournal journal = null;
    MJournalBatch journalBatch = null;
    int charge_Acct = 0;
    MAcctSchema as = null;
    MCampaign retaceo = null;
    
    @Override    
    protected void prepare()
    {
    	ProcessInfoParameter[] parameters = getParameter();
    	for (ProcessInfoParameter parameter : parameters) {
    		String name = parameter.getParameterName();
    		if (parameter.getParameter() == null)
    			;

    		if (name.equals(MCostDetail.COLUMNNAME_C_AcctSchema_ID)) {
    			p_C_AcctSchema_ID = parameter.getParameterAsInt();
    		}
    	}

        c_campaign_ID = getRecord_ID();
        retaceo = new MCampaign(getCtx(), c_campaign_ID, get_TrxName());
        
    }
    
    
    
    @Override
    protected String doIt() throws Exception
    {
    	
    	as = new MAcctSchema(getCtx(), p_C_AcctSchema_ID, get_TrxName());
    	completeReceipt();
    	distributeLandedCosts();
    	retaceo.set_ValueOfColumn("DocStatus", "CL");
    	retaceo.set_ValueOfColumn(MJournalBatch.COLUMNNAME_GL_JournalBatch_ID, journalBatch.getGL_JournalBatch_ID());
    	retaceo.saveEx();

		int ad_session_ID  = Env.getContextAsInt(getCtx(), "AD_Session_ID");
		MSession session = new MSession(getCtx(), ad_session_ID, null);
		
		if (session.getWebSession() == null ||session.getWebSession().length() == 0)
		{
			String whereClauseWindow = "gl_journalbatch_ID = " + journalBatch.getGL_JournalBatch_ID();
			MQuery query = new MQuery("");
			MTable table = new MTable(getCtx(), MJournalBatch.Table_ID, get_TrxName());
			query.addRestriction(whereClauseWindow);
			query.setRecordCount(1);
			int AD_WindowNo = table.getAD_Window_ID();
			commitEx();
			zoom (AD_WindowNo, query);
			return "";
			
		}

    	return journalBatch.getDocumentNo();
    }
    
    private Boolean completeReceipt()
    {
    	List<MInOut> receipts = new Query(getCtx(), MInOut.Table_Name, "c_campaign_ID=? and docstatus in ('DR','IP')", get_TrxName())
    		.setParameters(getRecord_ID())
    		.list();
    	for (MInOut io:receipts)
    	{
    		io.processIt(MInOut.DOCACTION_Complete);
    		io.saveEx();    		
    	}
    	return true;
    }
    private Boolean distributeLandedCosts()
    {
    	String whereClause = " c_invoice_ID IN (select c_invoice_ID from c_invoice where c_campaign_ID =?)"
    			+ " and exists (select 1 from c_landedcostallocation where c_invoiceline_ID = c_invoiceline.c_invoiceline_ID)" 
    			+ "  and c_invoiceline_ID not in (select coalesce(c_invoiceline_ID,0)  from gl_journal)";
    	List<MInvoiceLine> invoices = new Query(getCtx(), MInvoiceLine.Table_Name, whereClause, get_TrxName())
    		.setParameters(c_campaign_ID)
    		.list();
    	for (MInvoiceLine ivl:invoices)
    	 {
    		generateCostDetail(ivl);
    		createJournalBatch(ivl);
    		createJournal(ivl);
    		CreateHeaderLine(ivl);
    		distributeLandedCostsAmount(ivl);
    	 }
    	return true;
    }

    private Boolean distributeLandedCostsAmount(MInvoiceLine ivl)
    {
    	String whereClause = "c_invoiceLine_ID =?";
    	
    	BigDecimal lineNetAmt = ivl.getLineNetAmt();
    	BigDecimal controlamt = Env.ZERO;
    	List<MLandedCostAllocation> lcas = new Query(getCtx(), MLandedCostAllocation.Table_Name, whereClause, get_TrxName())
    		.setParameters(ivl.getC_InvoiceLine_ID())
    		.list();
    	for (MLandedCostAllocation lca:lcas)
    	{
    		controlamt = controlamt.add(lca.getAmt());
    		CreateLcaLine(lca);
    	}
    	if (lineNetAmt.compareTo(controlamt) != 0)
    		return false;
    	return true;
    }
    
    private String createJournalBatch(MInvoiceLine ivl)
    {
    	if (journalBatch != null)
    		return "";
		journalBatch = new MJournalBatch(getCtx(), 0, get_TrxName());
		if (journalBatch == null)
			return "No fue posible crear la partida";		
		
		MDocType doctype = new Query(getCtx(), MDocType.Table_Name, 
				MDocType.COLUMNNAME_DocBaseType + " = ?" , get_TrxName())
		.setParameters(MDocType.DOCBASETYPE_GLJournal)
		.setClient_ID()
		.first();
		
		if (doctype == null)
			return "Falta tipo de documento de Cierre";		
		journalBatch.setGL_Category_ID(doctype.getGL_Category_ID());
		journalBatch.setAD_Org_ID(ivl.getAD_Org_ID());
		MPeriod per = MPeriod.get (getCtx()	, Env.getContextAsDate(getCtx(), "#Date"),0);
		journalBatch.setC_Period_ID(per.getC_Period_ID());
		journalBatch.setDateAcct(new Timestamp(System.currentTimeMillis()));
		journalBatch.setDateDoc(new Timestamp(System.currentTimeMillis()));
		journalBatch.setDescription("Distribución de Costos  " + ivl.getC_Invoice().getDocumentNo());
		journalBatch.setC_DocType_ID(doctype.getC_DocType_ID());
		journalBatch.setDocumentNo("Distribución de Costos "  + retaceo.getName());
		journalBatch.setControlAmt(Env.ZERO);
		journalBatch.setC_Currency_ID(as.getC_Currency_ID());
		journalBatch.saveEx();		
    	return "";
    }

    private String createJournal(MInvoiceLine ivl)
    {

		journal = new MJournal(journalBatch);

		MAcctSchema as = new MAcctSchema(getCtx(), p_C_AcctSchema_ID, get_TrxName());
		if (journal == null)
			return "No fue posible crear la partida";
		String sql = "SELECT CH_Expense_Acct FROM C_Charge_Acct WHERE C_Charge_ID=? AND C_AcctSchema_ID=?";
		charge_Acct = DB.getSQLValue(get_TrxName(), sql, ivl.getC_Charge_ID(), p_C_AcctSchema_ID);
		if (charge_Acct == 0)
			return "El cargo no esta definido";
		
		journal.setC_Currency_ID(as.getC_Currency_ID());
		journal.setC_AcctSchema_ID(as.get_ID());
		journal.setC_ConversionType_ID(114);
		journal.setDocumentNo(DB.getDocumentNo(getAD_Client_ID(), MJournal.Table_Name, get_TrxName()));
		journal.setDescription(ivl.getC_Invoice().getDocumentNo() + " ");
		journal.setGL_Category_ID(journalBatch.getGL_Category_ID());
		journal.save();
		journal.setControlAmt(ivl.getLineNetAmt());
		journal.set_ValueOfColumn(MInvoiceLine.COLUMNNAME_C_InvoiceLine_ID, ivl.getC_InvoiceLine_ID());
    	return "";
    }
    

	private Boolean CreateHeaderLine(MInvoiceLine ivl)
	{
		MJournalLine CR = new MJournalLine(journal);
		CR.setC_ValidCombination_ID(charge_Acct);
		CR.setAmtSourceCr(ivl.getLineNetAmt());
		CR.setDescription(ivl.getC_Charge().getName());
		CR.saveEx();
		return true;
	}
	

	private Boolean CreateLcaLine(MLandedCostAllocation lca)
	{
		MJournalLine DR = new MJournalLine(journal);
		
		ProductCost pc = new ProductCost (Env.getCtx(), 
			lca.getM_Product_ID(), lca.getM_AttributeSetInstance_ID(),get_TrxName());
		MAccount combi = pc.getAccount(ProductCost.ACCTTYPE_P_Asset, as);
		DR.setDescription(lca.getM_Product().getName());
		DR.set_ValueOfColumn("Account_ID", combi.getAccount_ID());
		DR.set_ValueOfColumn("C_SubAcct_ID", combi.getC_SubAcct_ID() > 0 ? combi.getC_SubAcct_ID() : null);
		DR.set_ValueOfColumn("M_Product_ID", lca.getM_Product_ID());
		DR.set_ValueOfColumn("C_BPartner_ID", lca.getC_InvoiceLine().getC_Invoice().getC_BPartner_ID());
		DR.set_ValueOfColumn("AD_OrgTrx_ID", combi.getAD_OrgTrx_ID() > 0 ? combi.getAD_OrgTrx_ID() : null);
		DR.set_ValueOfColumn("C_LocFrom_ID", combi.getC_LocFrom_ID() > 0 ? combi.getC_LocFrom_ID() : null);
		DR.set_ValueOfColumn("C_LocTo_ID", combi.getC_LocTo_ID() > 0 ? combi.getC_LocTo_ID() : null);
		DR.set_ValueOfColumn("C_SalesRegion_ID", combi.getC_SalesRegion_ID() > 0 ? combi.getC_SalesRegion_ID() : null);
		DR.set_ValueOfColumn("C_Project_ID",lca.getC_InvoiceLine().getC_Project_ID());
		DR.set_ValueOfColumn("C_Campaign_ID", c_campaign_ID);
		DR.set_ValueOfColumn("C_Activity_ID", lca.getC_InvoiceLine().getC_Invoice().getC_Activity_ID());
		DR.set_ValueOfColumn("User1_ID", lca.getC_InvoiceLine().getC_Invoice().getUser1_ID());
		DR.set_ValueOfColumn("User2_ID", lca.getC_InvoiceLine().getC_Invoice().getUser2_ID());
		DR.setAmtSourceDr(lca.getAmt());
		DR.set_ValueOfColumn(MLandedCostAllocation.COLUMNNAME_C_LandedCostAllocation_ID, lca.getC_LandedCostAllocation_ID());
		DR.saveEx();
		return true;
	}


	protected void zoom (int AD_Window_ID, MQuery zoomQuery)
	{
		final AWindow frame = new AWindow();
		if (!frame.initWindow(AD_Window_ID, zoomQuery))
			return;
		AEnv.addToWindowManager(frame);
		//	VLookup gets info after method finishes
		new Thread()
		{
			public void run()
			{
				try
				{
					sleep(50);
				}
				catch (Exception e)
				{
				}
				AEnv.showCenterScreen(frame);
			}
		}.start();
	}	//	zoom
	
	private void generateCostDetail(MInvoiceLine ivl)
	{
		for (MLandedCostAllocation allocation : MLandedCostAllocation.getOfInvoiceLine(getCtx(), ivl.getC_InvoiceLine_ID(), get_TrxName()))
		{
			CostEngineFactory.getCostEngine(getAD_Client_ID()).createCostDetailForLandedCostAllocation(allocation);
		}
	}
	
    
    
    
    
}
