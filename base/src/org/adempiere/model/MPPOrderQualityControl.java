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
package org.adempiere.model;

import java.io.File;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.process.DocAction;
import org.compiere.process.DocumentEngine;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.TimeUtil;

/**
 * 	
 *	
 *  @author Susanne Calderon
 *  
 */
public class MPPOrderQualityControl extends X_PP_Order_QualityControl implements DocAction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 560499885058913281L;

	/**
	 * 	Standard Constructors
	 *	@param ctx context
	 *	@param C_TaxDeclaration_ID ic
	 *	@param trxName trx
	 */
	public MPPOrderQualityControl (Properties ctx, int C_TaxDeclaration_ID, String trxName)
	{
		super (ctx, C_TaxDeclaration_ID, trxName);
	}	//	MPPOrderQualityControl

	/**
	 * 	Load Constructor
	 *	@param ctx context 
	 *	@param rs result set
	 *	@param trxName trx
	 */
	public MPPOrderQualityControl (Properties ctx, ResultSet rs, String trxName)
	{
		super (ctx, rs, trxName);
	}	//	MPPOrderQualityControl
	
	/**
	 * 	Before Save
	 *	@param newRecord new
	 *	@return true
	 */
	protected boolean beforeSave (boolean newRecord)
	{
		return true;
	}	//	beforeSave
	

	public boolean processIt (String processAction) throws Exception
	{
		m_processMsg = null;
		DocumentEngine engine = new DocumentEngine (this, getDocStatus());
		return engine.processIt (processAction, getDocAction());
	}
	

	/**	Process Message 			*/
	private String		m_processMsg = null;
	
	/**
	 * 	Unlock Document.
	 * 	@return true if success 
	 */
	public boolean unlockIt()
	{
		return true;
	}
	/**
	 * 	Invalidate Document
	 * 	@return true if success 
	 */
	public boolean invalidateIt()
	{
		return true;
	}
	/**
	 *	Prepare Document
	 * 	@return new status (In Progress or Invalid) 
	 */
	public String prepareIt()
	{
		return DocAction.STATUS_InProgress;
	}
	/**
	 * 	Approve Document
	 * 	@return true if success 
	 */
	public boolean  approveIt()
	{
		return true;
	}
	/**
	 * 	Reject Approval
	 * 	@return true if success 
	 */
	public boolean rejectIt()
	{
		return true;
	}
	/**
	 * 	Complete Document
	 * 	@return new status (Complete, In Progress, Invalid, Waiting ..)
	 */
	public String completeIt()
	{
		setProcessed(true);
		setDocAction(DOCACTION_Close);
		save();
		return DocAction.STATUS_Completed;
	}
	/**
	 * 	Void Document
	 * 	@return true if success 
	 */
	public boolean voidIt()
	{
		setProcessed(true);
		setDocAction(DOCACTION_None);
		setDocStatus(DOCSTATUS_Voided);
		return true;
	}
	/**
	 * 	Close Document
	 * 	@return true if success 
	 */
	public boolean closeIt()
	{
		setProcessed(true);
		setDocAction(DOCACTION_None);
		setDocStatus(DOCSTATUS_Closed);
		return true;
	}
	/**
	 * 	Reverse Correction
	 * 	@return true if success 
	 */
	public boolean reverseCorrectIt()
	{
		return true;
	}
	/**
	 * 	Reverse Accrual
	 * 	@return true if success 
	 */
	public boolean reverseAccrualIt()
	{
		return true;
	}
	/** 
	 * 	Re-activate
	 * 	@return true if success 
	 */
	public boolean reActivateIt()
	{
		return true;
	}

	/**************************************************************************
	 * 	Get Summary
	 *	@return Summary of Document
	 */
	public String getSummary()
	{
		return "";
	}

	/**
	 * 	Get Document No
	 *	@return Document No
	 */
	public String getDocumentNo()
	{
		return "";
	}

	/**
	 * 	Get Document Info
	 *	@return Type and Document No
	 */
	public String getDocumentInfo()
	{
		return "";
	}

	/**
	 * 	Create PDF
	 *	@return file
	 */
	public File createPDF ()
	{
		return null;
	}
	
	/**
	 * 	Get Process Message
	 *	@return clear text message
	 */
	public String getProcessMsg ()
	{
		return "";
	}
	
	/**
	 * 	Get Document Owner
	 *	@return AD_User_ID
	 */
	public int getDoc_User_ID()
	{
		return 0;
	}
	
	/**
	 * 	Get Document Currency
	 *	@return C_Currency_ID
	 */
	public int getC_Currency_ID()
	{
		return 0;
	}

	/**
	 * 	Get Document Approval Amount
	 *	@return amount
	 */
	public BigDecimal getApprovalAmt()
	{
		return Env.ZERO;
	}
		

	/**
	 * 	Get Document Client
	/**
	 * 	Get Document Organization
	 *	@return AD_Org_ID
	 */
	public int getAD_Org_ID()
	{
		return 0;
	}

	/**
	 * 	Get Doc Action
	 *	@return Document Action
	 */
	public String getDocAction()
	{
		return "";
	}

	/**
	 * 	Save Document
	 *	@return true if saved
	 */
	
	public String getDocStatus () 
	{
		return get_ValueAsString("DocStatus");
	}
	
}	//	MPPOrderQualityControl
