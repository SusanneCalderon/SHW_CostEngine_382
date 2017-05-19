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
package  org.shw.model;

import java.awt.font.GlyphJustificationInfo;
import java.io.File;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.adempiere.engine.CostEngineFactory;
import org.compiere.acct.FactLine;
import org.compiere.apps.ADialog;
import org.compiere.model.MAccount;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MCostDetail;
import org.compiere.model.MCostType;
import org.compiere.model.MDocType;
import org.compiere.model.MInOut;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MJournal;
import org.compiere.model.MJournalBatch;
import org.compiere.model.MJournalLine;
import org.compiere.model.MLandedCostAllocation;
import org.compiere.model.MPeriod;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.ProductCost;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.process.DocumentEngine;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;

/**
 * 	
 *  @author Susanne Calderon
 *  
 */
public class MSHWCostDistribution extends X_SHW_CostDistribution  implements DocAction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6181691633960939054L;
	List<MInvoiceLine> ivLines = null;

	/**************************************************************************
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param W_MailMsg_ID id
	 *	@param trxName trx
	 */
	public MSHWCostDistribution (Properties ctx, int SHW_CostDistribution_ID, String trxName)
	{
		super (ctx, SHW_CostDistribution_ID, trxName);
	}	//	MLGRoute

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trxName trx
	 */
	/**
	 * 
	 * 	Get Document Info
	 *	@return document info (untranslated)
	 */

	/**
	 *  Load Constructor
	 *  @param ctx context
	 *  @param rs result set record
	 *	@param trxName transaction
	 */
	public MSHWCostDistribution (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MInvoice
	public String getDocumentInfo()
	{
		return Msg.getElement(getCtx(), "SHW_CostDistribution") + " " + getDocumentNo();
	}	//	getDocumentInfo

	/**
	 * 	Create PDF
	 *	@return File or null
	 */
	public File createPDF ()
	{
		try
		{
			File temp = File.createTempFile(get_TableName()+get_ID()+"_", ".pdf");
			return createPDF (temp);
		}
		catch (Exception e)
		{
			log.severe("Could not create PDF - " + e.getMessage());
		}
		return null;
	}	//	getPDF

	/**
	 * 	Create PDF file
	 *	@param file output file
	 *	@return file if success
	 */
	public File createPDF (File file)
	{
	//	ReportEngine re = ReportEngine.get (getCtx(), ReportEngine.INVOICE, getC_Invoice_ID());
	//	if (re == null)
			return null;
	//	return re.getPDF(file);
	}	//	createPDF

	/**
	 * 	Before Save
	 *	@param newRecord
	 *	@return true
	 */
	protected boolean beforeSave (boolean newRecord)
	{
		if (getAD_Org_ID() == 0)
		{
			log.saveError("Error", Msg.parseTranslation(getCtx(), "@AD_Org_ID@"));
			return false;
		}
		return true;
	}	//	beforeSave
	
	
	/**************************************************************************
	 * 	Process document
	 *	@param processAction document action
	 *	@return true if performed
	 */
	public boolean processIt (String processAction)
	{
		m_processMsg = null;
		DocumentEngine engine = new DocumentEngine (this, getDocStatus());
		return engine.processIt (processAction, getDocAction());
	}	//	process
	
	/**	Process Message 			*/
	private String		m_processMsg = null;
	/**	Just Prepared Flag			*/
	private boolean		m_justPrepared = false;

	/**
	 * 	Unlock Document.
	 * 	@return true if success 
	 */
	public boolean unlockIt()
	{
		log.info(toString());
		setProcessing(false);
		return true;
	}	//	unlockIt
	
	/**
	 * 	Invalidate Document
	 * 	@return true if success 
	 */
	public boolean invalidateIt()
	{
		log.info(toString());
		setDocAction(DOCACTION_Prepare);
		return true;
	}	//	invalidateIt
	
	/**
	 *	Prepare Document
	 * 	@return new status (In Progress or Invalid) 
	 */
	public String prepareIt()
	{
		log.info(toString());
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_BEFORE_PREPARE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_AFTER_PREPARE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;

		String sql = "select count(*) from c_landedcostallocation lca " +
				" where lca.c_invoiceline_ID=? ";
		String whereClause = "shw_costdistribution_ID=? ";
		String result = "Los siguientes productos no han sido distribuidos: \n";
		ivLines = new Query(getCtx(), MInvoiceLine.Table_Name, whereClause, get_TrxName())
			.setParameters(getSHW_CostDistribution_ID())
			.list();
		for (MInvoiceLine ivLine:ivLines)
		{
			if (ivLine.getParent().getDocStatus().equals(DOCSTATUS_Reversed)
					|| ivLine.getParent().getDocStatus().equals(DOCSTATUS_Voided)
					|| ivLine.getC_Charge_ID() == 0)
				continue;
			if (ivLine.getParent().getDocStatus().equals(DOCSTATUS_InProgress)
						|| ivLine.getParent().getDocStatus().equals(DOCSTATUS_Drafted)
						|| ivLine.getParent().getDocStatus().equals(DOCSTATUS_Invalid))
					return "La factura " + ivLine.getParent().getDocumentNo() + " no esta completada";	
			if (ivLine.getC_Charge_ID() != 0)
			{
				int count = DB.getSQLValueEx(get_TrxName(), sql, ivLine.getC_InvoiceLine_ID());
				if (count== 0)
					result = result + "Factura=" + ivLine.getParent().getDocumentNo()  + ", Cargo=" + ivLine.getC_Charge().getName() + " \n";				
			}							
		}
		sql = "select count(*) from m_inoutline iol inner join m_inout io on iol.m_inout_ID = io.m_inout_ID where io.docstatus in ('DR','IP', 'IN') and iol.shw_costdistribution_ID=?";
		int count = DB.getSQLValueEx(get_TrxName(), sql, getSHW_CostDistribution_ID());
		if (count>0)
			return "Hay recibos de material sin completar";
		if (result.length() >0)
		{
			result = result + " \n" + "Si no necesario distribuir esos costos, continúe.";				
			if (!ADialog.ask(0,null, "Facturas no distribuídas", result))
				return "Favor revisar facturas";
		}
		
		m_justPrepared = true;
		if (!DOCACTION_Complete.equals(getDocAction()))
			setDocAction(DOCACTION_Complete);
		return DocAction.STATUS_InProgress;
	}	//	prepareIt
	
	/**
	 * 	Approve Document
	 * 	@return true if success 
	 */
	public boolean  approveIt()
	{
		log.info(toString());
		setIsApproved(true);
		return true;
	}	//	approveIt
	
	/**
	 * 	Reject Approval
	 * 	@return true if success 
	 */
	public boolean rejectIt()
	{
		log.info(toString());
		setIsApproved(false);
		return true;
	}	//	rejectIt
	
	/**
	 * 	Complete Document
	 * 	@return new status (Complete, In Progress, Invalid, Waiting ..)
	 */
	public String completeIt()
	{
		//	Re-Check
		if (!m_justPrepared)
		{
			String status = prepareIt();
			if (!DocAction.STATUS_InProgress.equals(status))
				return status;
		}
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_BEFORE_COMPLETE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;

		//	Implicit Approval
		if (!isApproved())
			approveIt();
		//
		log.info(toString());
		if (get_ValueAsBoolean("IsSummary"))
		{
			 List<MSHWCostDistribution> dists = new Query(getCtx(), MSHWCostDistribution.Table_Name,
					 "shw_costdistributionparent_ID=? and docstatus not in ('CO','CL')", get_TrxName())
					 .setParameters(getSHW_CostDistribution_ID())
					 .list();
			 for (MSHWCostDistribution dist:dists)
			 {
				 if (dist.getDocStatus().equals("CO") || dist.getDocStatus().equals("CL"))
					 continue;
				 if (dist.processIt("CO"))
					 continue;
					 else
						 return "No fue posible completar el retaceo " + dist.getDocumentNo();
			 }
		}

		String valid = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_AFTER_COMPLETE);
		if (valid != null)
		{
			m_processMsg = valid;
			return DocAction.STATUS_Invalid;
		}
		//
		setProcessed(true);
		setDocAction(DOCACTION_Close);
		return DocAction.STATUS_Completed;
	}	//	completeIt
	
	/**
	 * 	Void Document.
	 * 	Same as Close.
	 * 	@return true if success 
	 */
	public boolean voidIt()
	{
		log.info(toString());
		// Before Void
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_VOID);
		if (m_processMsg != null)
			return false;

		//FR [ 1866214 ]
		boolean retValue = reverseIt();
		
		if (retValue) {
			// After Void
			m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_VOID);
			if (m_processMsg != null)
				return false;		
			setDocAction(DOCACTION_None);
		}

		return retValue;
	}	//	voidIt
	
	//FR [ 1866214 ]
	/**************************************************************************
	 * 	Reverse Cash
	 * 	Period needs to be open
	 *	@return true if reversed
	 */
	private boolean reverseIt() 
	{
		if (DOCSTATUS_Closed.equals(getDocStatus())
			|| DOCSTATUS_Reversed.equals(getDocStatus())
			|| DOCSTATUS_Voided.equals(getDocStatus()))
		{
			m_processMsg = "Document Closed: " + getDocStatus();
			setDocAction(DOCACTION_None);
			return false;
		}

		//	Reverse Allocations

		
		setDocumentNo(getDocumentNo()+"^");
		addDescription(Msg.getMsg(getCtx(), "Voided"));
		setDocStatus(DOCSTATUS_Reversed);	//	for direct calls
		setProcessed(true);
		setPosted(true);
		setDocAction(DOCACTION_None);
		saveEx();
		
		return true;
	}	//	reverse
	
	/**
	 * 	Add to Description
	 *	@param description text
	 */
	public void addDescription (String description)
	{
		String desc = getDescription();
		if (desc == null)
			setDescription(description);
		else
			setDescription(desc + " | " + description);
	}	//	addDescription

	/**
	 * 	Close Document.
	 * 	Cancel not delivered Quantities
	 * 	@return true if success 
	 */
	public boolean closeIt()
	{
		log.info(toString());
		// Before Close
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_CLOSE);
		if (m_processMsg != null)
			return false;
		
		setDocAction(DOCACTION_None);

		// After Close
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_CLOSE);
		if (m_processMsg != null)
			return false;
		return true;
	}	//	closeIt
	
	/**
	 * 	Reverse Correction
	 * 	@return true if success 
	 */
	public boolean reverseCorrectIt()
	{
		log.info(toString());
		// Before reverseCorrect
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_REVERSECORRECT);
		if (m_processMsg != null)
			return false;
		
		//FR [ 1866214 ]
		boolean retValue = reverseIt();
		
		if (retValue) {
			// After reverseCorrect
			m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_REVERSECORRECT);
			if (m_processMsg != null)
				return false;		
		}
		
		return retValue;
	}	//	reverseCorrectionIt
	
	/**
	 * 	Reverse Accrual - none
	 * 	@return true if success 
	 */
	public boolean reverseAccrualIt()
	{
		log.info(toString());
		// Before reverseAccrual
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_REVERSEACCRUAL);
		if (m_processMsg != null)
			return false;
		
		// After reverseAccrual
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_REVERSEACCRUAL);
		if (m_processMsg != null)
			return false;
				
		return false;
	}	//	reverseAccrualIt
	
	/** 
	 * 	Re-activate
	 * 	@return true if success 
	 */
	public boolean reActivateIt()
	{
		log.info(toString());
		// Before reActivate
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_REACTIVATE);
		if (m_processMsg != null)
			return false;	
				
		setProcessed(false);
		if (reverseCorrectIt())
			return true;

		// After reActivate
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_REACTIVATE);
		if (m_processMsg != null)
			return false;		
		return false;
	}	//	reActivateIt
	
	/**
	 * 	Set Processed
	 *	@param processed processed
	 */
	public void setProcessed (boolean processed)
	{
		super.setProcessed (processed);
		log.fine(processed + "");
	}	//	setProcessed
	
	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("MCostDistribution[");
		sb.append (get_ID ())
			.append ("-").append (getDocumentNo());
		return sb.toString ();
	}	//	toString
	
	/*************************************************************************
	 * 	Get Summary
	 *	@return Summary of Document
	 */
	public String getSummary()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getDocumentNo());
		//	: Total Lines = 123.00 (#1)
		//	 - Description
		if (getDescription() != null && getDescription().length() > 0)
			sb.append(" - ").append(getDescription());
		return sb.toString();
	}	//	getSummary
	
	/**
	 * 	Get Process Message
	 *	@return clear text error message
	 */
	public String getProcessMsg()
	{
		return m_processMsg;
	}	//	getProcessMsg
	
	/**
	 * 	Get Document Owner (Responsible)
	 *	@return AD_User_ID
	 */
	public int getDoc_User_ID()
	{
		return getCreatedBy();
	}	//	getDoc_User_ID

	/**
	 * 	Get Document Approval Amount
	 *	@return amount difference
	 */
	public BigDecimal getApprovalAmt()
	{
		return Env.ZERO;
	}	//	getApprovalAmt


	/**
	 * 	Document Status is Complete or Closed
	 *	@return true if CO, CL or RE
	 */
	public boolean isComplete()
	{
		return true;
	}	//	isComplete
	

	/**
	 * 	Get Document Currency
	 *	@return C_Currency_ID
	 */
	public int getC_Currency_ID()
	{
		return 0;
	}
    private String distributeLandedCosts(MAcctSchema as, MJournalBatch journalBatch)
    {
    	String sqlcount= "select count(*) from gl_journal where c_invoiceline_ID =?";
    	String sql = "SELECT CH_Expense_Acct FROM C_Charge_Acct WHERE C_Charge_ID=? AND C_AcctSchema_ID=?";
    	String sqllca = "SELECT count(*)  FROM C_LandedCostAllocation WHERE C_InvoiceLine_ID=?";
    	//String whereClause = "SHW_CostDistribution_ID=?";

    	for (MInvoiceLine ivl:ivLines)
    	{
    		int count = DB.getSQLValueEx(get_TrxName(), sqlcount, ivl.getC_InvoiceLine_ID());
    		if (count != 0)
    			continue;
    		count = DB.getSQLValueEx(get_TrxName(), sqllca, ivl.getC_InvoiceLine_ID());
    		if (count == 0)
    			continue;
    		int charge_Acct_ID = DB.getSQLValue(get_TrxName(), sql, ivl.getC_Charge_ID(), as.getC_AcctSchema_ID());
    		if (charge_Acct_ID == 0)
    			return "El cargo no esta definido";
    		MAccount charge_Acct = MAccount.get(getCtx(), charge_Acct_ID);
    		MJournal journal = createJournal(ivl, as, journalBatch);
    		CreateHeaderLine(ivl,charge_Acct, journal);
    		distributeLandedCostsAmount(ivl, as, journal);
    	}
    	return "";
    }

    private Boolean distributeLandedCostsAmount(MInvoiceLine ivl, MAcctSchema as, MJournal journal)
    {

		int invoiceLineId = ivl.get_ID();
		MLandedCostAllocation[] landedCostAllocations = MLandedCostAllocation.getOfInvoiceLine(
			getCtx(), invoiceLineId, get_TrxName());
		if (landedCostAllocations.length == 0)
			return false;


		//	Create New
		Arrays.stream(landedCostAllocations)
				.filter(landedCostAllocation -> landedCostAllocation.getBase().signum() != 0) // only cost allocation with base > 0
				.forEach(landedCostAllocation -> {
			//	Accounting
			ProductCost productCost = new ProductCost (Env.getCtx(),
				landedCostAllocation.getM_Product_ID(), landedCostAllocation.getM_AttributeSetInstance_ID(), get_TrxName());
			BigDecimal debitAmount = BigDecimal.ZERO;
			BigDecimal creditAmount = BigDecimal.ZERO;
			MCostType costType = MCostType.get(as, landedCostAllocation.getM_Product_ID() , landedCostAllocation.getAD_Org_ID());
			if(MCostType.COSTINGMETHOD_AverageInvoice.equals(costType.getCostingMethod()))
			{
				//Cost to inventory asset
				BigDecimal assetAmount = Optional.ofNullable(MCostDetail.getByDocLineLandedCost(
						landedCostAllocation,
						as.getC_AcctSchema_ID(),
						costType.get_ID())).orElse(BigDecimal.ZERO);
				//cost to Cost Adjustment
				BigDecimal costAdjustment = landedCostAllocation.getAmt().subtract(assetAmount);
				if (assetAmount.signum() != 0)
				{
					if (landedCostAllocation.getAmt().compareTo(Env.ZERO)>0)
						debitAmount = assetAmount;
					else
						creditAmount = assetAmount;					
					CreateLcaLine(landedCostAllocation, as, journal, productCost.getAccount(ProductCost.ACCTTYPE_P_Asset, as),creditAmount, debitAmount);
				}
				if (costAdjustment.signum() != 0) {
					if (costAdjustment.compareTo(Env.ZERO)>0)
						debitAmount = costAdjustment;
					else
						creditAmount = costAdjustment;
					CreateLcaLine(landedCostAllocation, as, journal, productCost.getAccount(ProductCost.ACCTTYPE_P_CostAdjustment, as),creditAmount, debitAmount);
					
				}
			}	
			else
			{	
				CreateLcaLine(landedCostAllocation, as, journal, productCost.getAccount(ProductCost.ACCTTYPE_P_CostAdjustment, as),creditAmount, debitAmount);
				
			}	
			
		});
		log.config("Created #" + landedCostAllocations.length);
		return true;
	
    	/*
    	String whereClause = "c_invoiceLine_ID =? and c_landedcostallocation_ID is not null";
    	
    	BigDecimal lineNetAmt = ivl.getLineNetAmt();
    	BigDecimal controlamt = Env.ZERO;
    	List<MCostDetail> cds = new Query(getCtx(), MCostDetail.Table_Name, whereClause, get_TrxName())
    		.setParameters(ivl.getC_InvoiceLine_ID())
    		.list();
    	for (MCostDetail cd:cds)
    	{
    		journal.setDateAcct(cd.getDateAcct());
    		controlamt = controlamt.add(cd.getCostAdjustment());
    		CreateLcaLine(cd,as, journal);
    	}
    	if (lineNetAmt.compareTo(controlamt) != 0)
    		return false;
    	journal.saveEx();
    	return true;*/
    }
    
    private MJournalBatch createJournalBatch(MAcctSchema as)
    {
    	MJournalBatch journalBatch = new MJournalBatch(getCtx(), 0, get_TrxName());
		
		MDocType doctype = new Query(getCtx(), MDocType.Table_Name, 
				MDocType.COLUMNNAME_DocBaseType + " = ?" , get_TrxName())
		.setParameters(MDocType.DOCBASETYPE_GLJournal)
		.setClient_ID()
		.first();
		
		if (doctype == null)
			return null;		
		journalBatch.setGL_Category_ID(doctype.getGL_Category_ID());
		journalBatch.setAD_Org_ID(getAD_Org_ID());
		MPeriod per = MPeriod.get (getCtx()	, Env.getContextAsDate(getCtx(), "#Date"),0);
		journalBatch.setC_Period_ID(per.getC_Period_ID());
		journalBatch.setDateAcct(new Timestamp(System.currentTimeMillis()));
		journalBatch.setDateDoc(new Timestamp(System.currentTimeMillis()));
		journalBatch.setDescription("Distribuci�n de Costos  " + getDocumentNo());
		journalBatch.setC_DocType_ID(doctype.getC_DocType_ID());
		journalBatch.setDocumentNo("Distribuci�n "  + getDocumentNo());
		journalBatch.set_ValueOfColumn("SHW_CostDistribution_ID", getSHW_CostDistribution_ID());
		journalBatch.setControlAmt(Env.ZERO);
		journalBatch.setC_Currency_ID(as.getC_Currency_ID());
		journalBatch.saveEx();		
    	return journalBatch;
    }

    private MJournal createJournal(MInvoiceLine ivl, MAcctSchema as, MJournalBatch journalBatch )
    {

		MJournal journal = new MJournal(journalBatch);
		journal.setC_ConversionType_ID(114);
		journal.setDocumentNo(DB.getDocumentNo(getAD_Client_ID(), MJournal.Table_Name, get_TrxName()));
		journal.setDescription(ivl.getC_Invoice().getDocumentNo() + " ");
		journal.setGL_Category_ID(journalBatch.getGL_Category_ID());
		journal.saveEx();
		journal.setControlAmt(ivl.getLineNetAmt());
		journal.set_ValueOfColumn(MInvoiceLine.COLUMNNAME_C_InvoiceLine_ID, ivl.getC_InvoiceLine_ID());
    	return journal;
    }
    

	private Boolean CreateHeaderLine(MInvoiceLine ivl, MAccount charge_Acct, MJournal journal)
	{
		MJournalLine CR = new MJournalLine(journal);
		CR.setC_ValidCombination_ID(charge_Acct);
		CR.setAmtSourceCr(ivl.getLineNetAmt());
		CR.setDescription(ivl.getC_Charge().getName());
		CR.saveEx();
		return true;
	}
	

	private Boolean CreateLcaLine(MLandedCostAllocation lca,MAcctSchema as, MJournal journal, MAccount account, BigDecimal creditAmt, BigDecimal debitAmt)
	{
		MJournalLine DR = new MJournalLine(journal);
		
		
		DR.setDescription(lca.getM_Product().getName());
		DR.set_ValueOfColumn("Account_ID", account.getAccount_ID());
		DR.set_ValueOfColumn("C_SubAcct_ID", account.getC_SubAcct_ID() > 0 ? account.getC_SubAcct_ID() : null);
		DR.set_ValueOfColumn("M_Product_ID", lca.getM_Product_ID());
		DR.set_ValueOfColumn("C_BPartner_ID", lca.getC_InvoiceLine().getC_Invoice().getC_BPartner_ID());
		DR.set_ValueOfColumn("AD_OrgTrx_ID", account.getAD_OrgTrx_ID() > 0 ? account.getAD_OrgTrx_ID() : null);
		DR.set_ValueOfColumn("C_LocFrom_ID", account.getC_LocFrom_ID() > 0 ? account.getC_LocFrom_ID() : null);
		DR.set_ValueOfColumn("C_LocTo_ID", account.getC_LocTo_ID() > 0 ? account.getC_LocTo_ID() : null);
		DR.set_ValueOfColumn("C_SalesRegion_ID", account.getC_SalesRegion_ID() > 0 ? account.getC_SalesRegion_ID() : null);
		DR.set_ValueOfColumn("C_Project_ID",lca.getC_InvoiceLine().getC_Project_ID());
		DR.set_ValueOfColumn("C_Campaign_ID", lca.getC_InvoiceLine().getC_Invoice().getC_Campaign_ID());
		DR.set_ValueOfColumn("SHW_CostDistribution_ID",getSHW_CostDistribution_ID());
		DR.set_ValueOfColumn("C_Activity_ID", lca.getC_InvoiceLine().getC_Invoice().getC_Activity_ID());
		DR.set_ValueOfColumn("User1_ID", lca.getC_InvoiceLine().getC_Invoice().getUser1_ID());
		DR.set_ValueOfColumn("User2_ID", lca.getC_InvoiceLine().getC_Invoice().getUser2_ID());
		DR.setAmtSourceDr(debitAmt);
		DR.setAmtSourceCr(creditAmt);
		DR.set_ValueOfColumn(MLandedCostAllocation.COLUMNNAME_C_LandedCostAllocation_ID, lca.getC_LandedCostAllocation_ID());
		DR.saveEx();
		return true;
	}

	/*
	private void generateCostDetail(MInvoiceLine ivl)
	{
		for (MLandedCostAllocation allocation : MLandedCostAllocation.getOfInvoiceLine(getCtx(), ivl.getC_InvoiceLine_ID(), get_TrxName()))
		{
			CostEngineFactory.getCostEngine(getAD_Client_ID()).createCostDetailForLandedCostAllocation(allocation);
		}
	}
	*/

	
	
	
}	//	MLGProductPriceRate
