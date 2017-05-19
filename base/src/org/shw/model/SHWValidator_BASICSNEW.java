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
package org.shw.model;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.exceptions.DBException;
import org.compiere.acct.Doc;
import org.compiere.acct.Fact;
import org.compiere.acct.FactLine;
import org.compiere.model.MAccount;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MAllocationHdr;
import org.compiere.model.MAllocationLine;
import org.compiere.model.MAttributeSet;
import org.compiere.model.MBPartner;
import org.compiere.model.MBankAccount;
import org.compiere.model.MBankStatement;
import org.compiere.model.MCash;
import org.compiere.model.MCashLine;
import org.compiere.model.MClient;
import org.compiere.model.MCostDetail;
import org.compiere.model.MCurrency;
import org.compiere.model.MDocType;
import org.compiere.model.MFactAcct;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutConfirm;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInOutLineConfirm;
import org.compiere.model.MInventory;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MInvoiceTax;
import org.compiere.model.MJournal;
import org.compiere.model.MLandedCostAllocation;
import org.compiere.model.MMatchInv;
import org.compiere.model.MMovement;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MOrderTax;
import org.compiere.model.MPayment;
import org.compiere.model.MPaymentAllocate;
import org.compiere.model.MProduct;
import org.compiere.model.MProductPO;
import org.compiere.model.MProduction;
import org.compiere.model.MProject;
import org.compiere.model.MProjectIssue;
import org.compiere.model.MRequisition;
import org.compiere.model.MRequisitionLine;
import org.compiere.model.MRole;
import org.compiere.model.MStorage;
import org.compiere.model.MSysConfig;
import org.compiere.model.MTax;
import org.compiere.model.MTaxCategory;
import org.compiere.model.MTransaction;
import org.compiere.model.MWarehouse;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.model.ProductCost;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Trx;
import org.eevolution.model.MDDOrder;
import org.eevolution.model.MHRAttribute;
import org.eevolution.model.MHRConcept;
import org.eevolution.model.MHREmployee;
import org.eevolution.model.MHRMovement;
import org.eevolution.model.MHRProcess;
import org.eevolution.model.MPPOrder;
import org.eevolution.model.X_HR_EmployeeType;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;



/**
 *	Validator Example Implementation
 *	
 *	@author Jorg Janke
 *	@version $Id: MyValidator.java,v 1.2 2006/07/30 00:51:57 jjanke Exp $
 */
public class SHWValidator_BASICSNEW implements ModelValidator
{
	/**
	 *	Constructor.
	 */
	public SHWValidator_BASICSNEW ()
	{
		super ();
	}	//	MyValidator

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(SHWValidator_BASICSNEW.class);
	/** Client			*/
	private int		m_AD_Client_ID = -1;
	/** User	*/
	private int		m_AD_User_ID = -1;
	/** Role	*/
	private int		m_AD_Role_ID = -1;

	/**
	 *	Initialize Validation
	 *	@param engine validation engine 
	 *	@param client client
	 */
	public void initialize (ModelValidationEngine engine, MClient client)
	{
		//client = null for global validatorALG
		if (client != null) {	
			m_AD_Client_ID = client.getAD_Client_ID();
			log.info(client.toString());
		}
		else  {
			log.info("Initializing global validator: "+this.toString());
		}

		//	We want to be informed when C_Order is created/changed
		engine.addDocValidate(MPayment.Table_Name, this);
		engine.addDocValidate(MAllocationHdr.Table_Name, this);
		engine.addDocValidate(MMovement.Table_Name, this);
		engine.addDocValidate(MInOut.Table_Name, this);
		engine.addDocValidate(MInvoice.Table_Name, this);
		engine.addDocValidate(MPayment.Table_Name, this);
		engine.addDocValidate(MHRProcess.Table_Name, this);
		engine.addDocValidate(MOrder.Table_Name, this);

		engine.addDocValidate(MBankStatement.Table_Name, this);
		engine.addDocValidate(MCash.Table_Name, this);
		engine.addDocValidate(MMatchInv.Table_Name, this);
		engine.addDocValidate(MJournal.Table_Name, this);
		engine.addDocValidate(MInOutConfirm.Table_Name, this);


		engine.addModelChange(MInvoiceLine.Table_Name, this);
		engine.addModelChange(MInvoice.Table_Name, this);
		engine.addModelChange(MRequisitionLine.Table_Name, this);
		engine.addModelChange(MTransaction.Table_Name, this);
		engine.addModelChange(MPayment.Table_Name, this);
		engine.addModelChange(MHREmployee.Table_Name, this);
		engine.addModelChange(MProject.Table_Name, this);
		engine.addModelChange(MOrderLine.Table_Name, this);
		engine.addModelChange(MInOutLine.Table_Name, this);
		engine.addModelChange(MInOut.Table_Name, this);
		engine.addModelChange(MInvoiceTax.Table_Name, this);
		engine.addModelChange(MOrderTax.Table_Name, this);
	}	//	initialize

	/**
	 *	Model Change of a monitored Table.
	 *	Called after PO.beforeSave/PO.beforeDelete
	 *	when you called addModelChange for the table
	 *	@param po persistent object
	 *	@param type TYPE_
	 *	@return error message or null
	 *	@exception Exception if the recipient wishes the change to be not accept.
	 */
	public String modelChange (PO po, int type) throws Exception
	{
		String error = null;
		if (po.get_TableName().equals(MInvoiceLine.Table_Name))
		{

			if (type == TYPE_BEFORE_NEW)
				;
			if (type==TYPE_BEFORE_CHANGE)
				;
			if (type == TYPE_AFTER_NEW)
				;
			if (type == TYPE_AFTER_CHANGE)
				;
			if (type == TYPE_BEFORE_DELETE)
				error = InvoiceLineBeforeDelete(po);
			if (type == TYPE_AFTER_DELETE)
				;
			if (type==TYPE_DELETE)
				;			
		}

		if (po.get_TableName().equals(MRequisitionLine.Table_Name))
		{
			if (type == TYPE_BEFORE_NEW)
				error = RequisitionUpdatePriceLineNetamt(po);
			if (type==TYPE_BEFORE_CHANGE)
				error = RequisitionUpdatePriceLineNetamt(po);
			if (type == TYPE_AFTER_NEW)
				;
			if (type == TYPE_AFTER_CHANGE)
				;
			if (type == TYPE_BEFORE_DELETE)
				;
			if (type == TYPE_AFTER_DELETE)
				;
			if (type==TYPE_DELETE)
				;			
		}
		if (po.get_TableName().equals(MTransaction.Table_Name))
		{

			if (type == TYPE_BEFORE_NEW)
				error = TransactionOrg_ID(po);
			if (type==TYPE_BEFORE_CHANGE)
				error = TransactionOrg_ID(po);
			if (type == TYPE_AFTER_NEW)
				;
			if (type == TYPE_AFTER_CHANGE)
				;
			if (type == TYPE_BEFORE_DELETE);
			if (type == TYPE_AFTER_DELETE)
				;
			if (type==TYPE_DELETE)
				;			
		}

		if (po.get_TableName().equals(MPayment.Table_Name))
		{

			if (type == TYPE_BEFORE_NEW)
				//error = checkCashOpen(po)
				;
			if (type==TYPE_BEFORE_CHANGE)
				if (po.is_ValueChanged(MPayment.COLUMNNAME_C_CashBook_ID))
					//	error = checkCashOpen(po
					;
			if (type == TYPE_AFTER_NEW)
				;
			if (type == TYPE_AFTER_CHANGE)
				;
			if (type == TYPE_BEFORE_DELETE);
			if (type == TYPE_AFTER_DELETE)
				;
			if (type==TYPE_DELETE)
				;			
		}

		if (po.get_TableName().equals(MOrder.Table_Name))
		{

			if (type == TYPE_BEFORE_NEW)
				error = OrderSearchDocType(po);
			if (type==TYPE_BEFORE_CHANGE);
			if (type == TYPE_AFTER_NEW)
				;
			if (type == TYPE_AFTER_CHANGE)

				;
			if (type == TYPE_BEFORE_DELETE);
			if (type == TYPE_AFTER_DELETE)
				;
			if (type==TYPE_DELETE)
				;			
		}

		if (po.get_TableName().equals(MInvoice.Table_Name))
		{

			if (type == TYPE_BEFORE_NEW)
				;
			if (type==TYPE_BEFORE_CHANGE)
				;
			if (type == TYPE_AFTER_NEW)
				;
			if (type == TYPE_AFTER_CHANGE)
				;
			if (type == TYPE_BEFORE_DELETE);
			if (type == TYPE_AFTER_DELETE)
				;
			if (type==TYPE_DELETE)
				;			
		}

		if (po.get_TableName().equals(MHREmployee.Table_Name))
		{

			if (type == TYPE_BEFORE_NEW);
			if (type==TYPE_BEFORE_CHANGE);
			if (type == TYPE_AFTER_NEW)
			{
				Boolean changed = po.is_ValueChanged(MHREmployee.COLUMNNAME_HR_EmployeeType_ID)
						|| po.is_ValueChanged(MHREmployee.COLUMNNAME_MonthlySalary)
						|| po.is_ValueChanged(MHREmployee.COLUMNNAME_DailySalary);
				if (changed)
					error = EmployeeCreateAttribute(po);
			}
			if (type == TYPE_AFTER_CHANGE)
			{
				Boolean changed = po.is_ValueChanged(MHREmployee.COLUMNNAME_HR_EmployeeType_ID)
						|| po.is_ValueChanged(MHREmployee.COLUMNNAME_MonthlySalary)
						|| po.is_ValueChanged(MHREmployee.COLUMNNAME_DailySalary);
				if (changed)
					error = EmployeeCreateAttribute(po);
			}
			if (type == TYPE_BEFORE_DELETE);
			if (type == TYPE_AFTER_DELETE)
				;
			if (type==TYPE_DELETE)
				;			
		}
		if (po.get_TableName().equals(MInOutLine.Table_Name))
		{

			if (type == TYPE_BEFORE_NEW)
				//InoutLineChangeRetaceo(po)
				;
			if (type==TYPE_BEFORE_CHANGE)
				//InoutLineChangeRetaceo(po)
				;
			if (type == TYPE_AFTER_NEW)
				//error = test(po)
				;
			if (type == TYPE_AFTER_CHANGE)
				//error = test(po)
				;
			if (type == TYPE_BEFORE_DELETE)
				;
			if (type == TYPE_AFTER_DELETE)
				;
			if (type==TYPE_DELETE)
				;			
		}
		if (po.get_TableName().equals(MInOut.Table_Name))
		{

			if (type == TYPE_BEFORE_NEW)
				//InoutLineChangeRetaceo(po);
			if (type==TYPE_BEFORE_CHANGE)
				//InoutLineChangeRetaceo(po);
			if (type == TYPE_AFTER_NEW)
				//error = test(po)
				;
			if (type == TYPE_AFTER_CHANGE)
				//error = test(po)
				;
			if (type == TYPE_BEFORE_DELETE)
				;
			if (type == TYPE_AFTER_DELETE)
				;
			if (type==TYPE_DELETE)
				;			
		}
		if (po.get_TableName().equals(MOrderLine.Table_Name))
		{

			if (type == TYPE_BEFORE_NEW)
				;
			if (type==TYPE_BEFORE_CHANGE)
				;
			if (type == TYPE_AFTER_NEW)
				//error = OrderLineUpdateASI(po);
				;
			if (type == TYPE_AFTER_CHANGE)
				;
			if (type == TYPE_BEFORE_DELETE);
			if (type == TYPE_AFTER_DELETE)
				;
			if (type==TYPE_DELETE)
				;			
		}



		if (type == ModelValidator.TYPE_AFTER_DELETE) {

		}
		

		if (po.get_TableName().equals(MInOutLine.Table_Name))
		{

			if (type == TYPE_BEFORE_NEW)

				//OrderLineUpdateASI(po, type)
				;
			if (type==TYPE_BEFORE_CHANGE)
				//OrderLineUpdateASI(po, type)
				;
			if (type == TYPE_AFTER_NEW)
				;
			if (type == TYPE_AFTER_CHANGE)

				;
			if (type == TYPE_AFTER_DELETE)
				;
			if (type==TYPE_DELETE)
				;			
		}

		if (po.get_TableName().equals(MAllocationLine.Table_Name))
		{

			if (type == TYPE_BEFORE_NEW)
				;
			if (type==TYPE_BEFORE_CHANGE)
				;
			if (type == TYPE_AFTER_NEW)
				//test(po)
				;
			if (type == TYPE_AFTER_CHANGE)
				;
			if (type == TYPE_AFTER_DELETE)
				;
			if (type==TYPE_DELETE)
				;			
		}
		if (po.get_TableName().equals(MInvoiceTax.Table_Name))
		{
			if (type == TYPE_BEFORE_CHANGE || type == TYPE_BEFORE_NEW)
				error = controlInvoiceTax(po)
				;
		}

		if (po.get_TableName().equals(MOrderTax.Table_Name))
		{
			if (type == TYPE_BEFORE_CHANGE || type == TYPE_BEFORE_NEW)
				error = controlOrderTax(po);
		}
		/*if (type == ModelValidator.TYPE_BEFORE_DELETE)
		{
			if (po.get_TableName().equals(MInvoiceLine.Table_Name))
				InvoiceLineBeforeDelete(po);
		}*/
		if (type == ModelValidator.TYPE_BEFORE_NEW || type == ModelValidator.TYPE_BEFORE_CHANGE)
		{
			//if (po.get_TableName().equals(MRequisitionLine.Table_Name))
			//	error = RequisitionUpdatePriceLineNetamt(po);
			//if (po.get_TableName().equals(MTransaction.Table_Name))
			//	error = TransactionOrg_ID(po);
			//if (po.get_TableName().equals(MPayment.Table_Name) && po.is_ValueChanged(MPayment.COLUMNNAME_C_CashBook_ID))
			//	error = checkCashOpen(po);
		}
		if (type == ModelValidator.TYPE_BEFORE_NEW )
		{
			//if (po.get_TableName().equals(MInvoice.Table_Name))
			//	error = InvoiceSearchDocType(po);
			//if (po.get_TableName().equals(MOrder.Table_Name))
			//error = OrderSearchDocType(po);

		}

		/*if (type == ModelValidator.TYPE_AFTER_NEW || type == ModelValidator.TYPE_AFTER_CHANGE){
			if (po.get_TableName().equals(MHREmployee.Table_Name))
			{
				Boolean changed = po.is_ValueChanged(MHREmployee.COLUMNNAME_HR_EmployeeType_ID)
						|| po.is_ValueChanged(MHREmployee.COLUMNNAME_MonthlySalary)
						|| po.is_ValueChanged(MHREmployee.COLUMNNAME_DailySalary);
				if (changed)
					error = EmployeeCreateAttribute(po);
			}

		}*/
		return error;
	}	//	modelChange

	/**
	 *	Validate Document.
	 *	Called as first step of DocAction.prepareIt 
	 *	when you called addDocValidate for the table.
	 *	Note that totals, etc. may not be correct.
	 *	@param po persistent object
	 *	@param timing see TIMING_ constants
	 *	@return error message or null
	 */
	public String docValidate (PO po, int timing)
	{
		String error = null;

		if (po.get_TableName().equals(MPayment.Table_Name))
		{
			if (timing == TIMING_BEFORE_PREPARE)
				
				;

			if (timing == TIMING_AFTER_COMPLETE)
			{
				error = PaymentUpdatePrepayment(po);
			}
			if (timing == TIMING_BEFORE_COMPLETE)
				;
			if (timing == TIMING_BEFORE_POST)
			{
				error = updatePostingPayment(po);				
			}

		}

		if (po.get_TableName().equals(MAllocationHdr.Table_Name))
		{
			if (timing == TIMING_AFTER_COMPLETE)
			{
				//error = CreatePaymentFromAllocationReembolso(po);
			}
			if (timing == TIMING_BEFORE_POST)
			{
				error = updatePostingAllocation(po);
				error = AfterPost_CorrectGL_Category(po);
			}
		}
		if (po.get_TableName().equals(MInvoice.Table_Name))
		{
			if (timing == TIMING_AFTER_COMPLETE)
			{
				error = InvoiceUpdateM_Product_PO(po);
				error = assignPrepayment(po);
			}
			if (timing == TIMING_BEFORE_COMPLETE)
				error = MInvoiceControlCostdistribution(po);
			if (timing == TIMING_BEFORE_PREPARE)
				;
		}
		if (po.get_TableName().equals(MInOut.Table_Name))
		{
			if (timing == TIMING_BEFORE_COMPLETE)
			{
				;
			}
		}


		if (po.get_TableName().equals(MHRProcess.Table_Name))
		{
			if (timing == TIMING_AFTER_COMPLETE)
			{
				error = CreateHRMovementAssignation(po);
			}
		}


		if (po.get_TableName().equals(MMovement.Table_Name))
		{
			if (timing == TIMING_BEFORE_POST)
				error = MovementAfterPost(po);
		}
		if (po.get_TableName().equals(MInOutConfirm.Table_Name))
		{
			if (timing == TIMING_AFTER_COMPLETE)
				//error = confirmCompleteInout(po
;
		}


		if (po.get_TableName().equals(MOrder.Table_Name))
		{
			;
		}

		if (isDocument(po))
		{
			if (timing == TIMING_BEFORE_REVERSECORRECT || timing == TIMING_BEFORE_VOID)
			{
				//error = changeDateacctVO(po);
			}
			if (timing == TIMING_AFTER_COMPLETE)
				//error = DateacctBeforeReverseVoid(po)
				;			
		}

		if (timing == TIMING_BEFORE_POST )
		{
			factAcct_UpdateDocumentNO(po);
			//PostCOGS_Invoice(po);
		}		

		return error;
	}	//	docValidate

	/**
	 *	User Login.
	 *	Called when preferences are set
	 *	@param AD_Org_ID org
	 *	@param AD_Role_ID role
	 *	@param AD_User_ID user
	 *	@return error message or null
	 */
	public String login (int AD_Org_ID, int AD_Role_ID, int AD_User_ID)
	{
		log.info("AD_User_ID=" + AD_User_ID);
		m_AD_User_ID = AD_User_ID;
		m_AD_Role_ID = AD_Role_ID;
		return null;
	}	//	login

	/**
	 *	Get Client to be monitored
	 *	@return AD_Client_ID client
	 */
	public int getAD_Client_ID()
	{
		return m_AD_Client_ID;
	}	//	getAD_Client_ID


	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("SHWValidator");
		sb.append ("]");
		return sb.toString ();
	}	//	toString

	/**
	 * Sample Validator Before Save Properties - to set mandatory properties on users
	 * avoid users changing properties
	 */
	public void beforeSaveProperties() {
		// not for SuperUser or role SysAdmin
		if (   m_AD_User_ID == 0  // System
				|| m_AD_User_ID == 100   // SuperUser
				|| m_AD_Role_ID == 0  // System Administrator
				|| m_AD_Role_ID == 1000000)  // ECO Admin
			return;

		log.info("Setting default Properties");

		MRole role = MRole.get(Env.getCtx(), m_AD_Role_ID);

		// Example - if you don't want user to select auto commit property
		// Ini.setProperty(Ini.P_A_COMMIT, false);

		// Example - if you don't want user to select auto login
		// Ini.setProperty(Ini.P_A_LOGIN, false);

		// Example - if you don't want user to select store password
		// Ini.setProperty(Ini.P_STORE_PWD, false);

		// Example - if you want your user inherit ALWAYS the show accounting from role
		// Ini.setProperty(Ini.P_SHOW_ACCT, role.isShowAcct());

		// Example - if you want to avoid your user from changing the working date
		/*
		Timestamp DEFAULT_TODAY =	new Timestamp(System.currentTimeMillis());
		//  Date (remove seconds)
		DEFAULT_TODAY.setHours(0);
		DEFAULT_TODAY.setMinutes(0);
		DEFAULT_TODAY.setSeconds(0);
		DEFAULT_TODAY.setNanos(0);
		Ini.setProperty(Ini.P_TODAY, DEFAULT_TODAY.toString());
		Env.setContext(Env.getCtx(), "#Date", DEFAULT_TODAY);
		 */

	}	// beforeSaveProperties




	//-----------------------------------------------------------------------------------	

	private String updatePostingPayment(PO A_PO)
	{
		String error = "";
        
		MPayment pay = (MPayment)A_PO;
		Boolean isEmployee = pay.getC_BPartner().isEmployee();
		Boolean isPrepayment = pay.isPrepayment();
		Boolean istoUpdate = isEmployee && isPrepayment;
		Boolean isreceipt = pay.getC_DocType().getDocBaseType().equals(MDocType.DOCBASETYPE_ARInvoice);
		if (!istoUpdate)
			return "";
		Doc doc = pay.getDoc();

		ArrayList<Fact> facts = doc.getFacts();
		// one fact per acctschema
		for (int i = 0; i < facts.size(); i++)
		{
			Fact fact = facts.get(i);
			MAcctSchema as = fact.getAcctSchema();
			MAccount Prepayment = null;
			if (isreceipt)
				Prepayment = doc.getAccount(Doc.ACCTTYPE_C_Prepayment, as);
			else
				Prepayment = doc.getAccount(Doc.ACCTTYPE_V_Prepayment, as);
			String sql = "SELECT  e_prepayment_acct FROM c_bp_employee_acct WHERE C_BPartner_ID=? AND C_AcctSchema_ID=?";
			int C_ValidCombination_ID = DB.getSQLValueEx(null, sql,pay.getC_BPartner_ID());
			MAccount Emp_PrePayment = MAccount.get (as.getCtx(), C_ValidCombination_ID);
			//MAccount Emp_PrePayment =  doc.getAccount(Doc.ACCTTYPE_e_prepayment_acct, as);
			for (FactLine fline : fact.getLines())
			{
				if (fline.getAccount_ID() != Prepayment.getAccount_ID())
					continue;
				fline.setAccount_ID(Emp_PrePayment.getAccount_ID());
			}			
		}		
		return error;
	}



	private String updatePostingAllocation(PO A_PO)
	{
		String error = "";
		MAllocationHdr ah = (MAllocationHdr)A_PO;
		Doc doc = ah.getDoc();
		for (MAllocationLine aLine : ah.getLines(true))
		{
			if (aLine.getC_Payment_ID() ==0 && aLine.getC_Invoice_ID() !=0)
			{
				continue;
			}
			if ((aLine.getC_Payment_ID() != 0 && aLine.getC_Payment().isPrepayment())
					|| (aLine.getC_Invoice_ID() !=0))
			{


				MPayment pay = (MPayment)aLine.getC_Payment();
				Boolean isEmployee = pay.getC_BPartner().isEmployee();
				Boolean isPrepayment = pay.isPrepayment();
				MInvoice invoice = (MInvoice)aLine.getC_Invoice();
				if (!(invoice.getDocStatus().equals("CO")||invoice.getDocStatus().equals("CL")))
					continue;
				Boolean istoUpdate = ((isEmployee && isPrepayment) || 
						(invoice.get_ValueAsBoolean("isContract") && isEmployee));
				Boolean isreceipt = pay.getC_DocType().getDocBaseType().equals(MDocType.DOCBASETYPE_ARInvoice);
				if (!istoUpdate)
					continue;

				ArrayList<Fact> facts = doc.getFacts();
				for (int i = 0; i < facts.size(); i++)
				{
					MAccount Prepayment = null;
					Fact fact = facts.get(i);
					MAcctSchema as = fact.getAcctSchema();
					doc.setC_BPartner_ID(aLine.getC_BPartner_ID());

					if (isPrepayment)
					{
						if (isreceipt)
							Prepayment = doc.getAccount(Doc.ACCTTYPE_C_Prepayment, as);
						else
							Prepayment = doc.getAccount(Doc.ACCTTYPE_V_Prepayment, as);					
					}
					else
					{
						if (isreceipt)
							Prepayment = doc.getAccount(Doc.ACCTTYPE_UnallocatedCash, as);
						else
							Prepayment = doc.getAccount(Doc.ACCTTYPE_PaymentSelect, as);					
					}					
					doc.setC_BPartner_ID(aLine.getC_Payment().getC_BPartner_ID());
					MAccount Emp_PrePayment = null;
					if (isEmployee)
					{
						String sql = "SELECT  e_prepayment_acct FROM c_bp_employee_acct WHERE C_BPartner_ID=? AND C_AcctSchema_ID=?";
						int C_ValidCombination_ID = DB.getSQLValueEx(null, sql,pay.getC_BPartner_ID());
						Emp_PrePayment = MAccount.get (as.getCtx(), C_ValidCombination_ID);
					}

					for (FactLine fline : fact.getLines())
					{
						if (fline.getLine_ID() != aLine.getC_AllocationLine_ID())
							continue;
						if (fline.getAccount_ID() != Prepayment.getAccount_ID())
							continue;
						fline.setAccount_ID(Emp_PrePayment.getAccount_ID());
						fline.setC_BPartner_ID(pay.getC_BPartner_ID());
					}	
				}
				// one fact per acctschema
			}
		}		
		return error;
	}

	private String PaymentUpdatePrepayment(PO A_PO)
	{
		MPayment pay = (MPayment)A_PO;
		MPaymentAllocate[] pAllocs = MPaymentAllocate.get(pay);
		int pas = pAllocs.length;
		Boolean isEmployee = pay.getC_BPartner().isEmployee();
		Boolean isfreePayment = pay.getC_Charge_ID() == 0 && pay.getC_Invoice_ID() == 0 && isEmployee && pas == 0;
		if (isfreePayment){
			pay.setIsPrepayment(true);	
			pay.saveEx();
			return"";
		}	
		if (pay.getC_Charge_ID() == 0 && pay.getC_Invoice_ID() != 0 && pay.getC_Order_ID()!= 0)
		{
			pay.setIsPrepayment(false);
		}
		return "";
	}



	private String InvoiceLineBeforeDelete(PO A_PO)
	{
		MInvoiceLine ivl = (MInvoiceLine)A_PO;
		MLandedCostAllocation[] lcas = MLandedCostAllocation.getOfInvoiceLine(A_PO.getCtx(), ivl.getC_InvoiceLine_ID(), A_PO.get_TrxName());
		String whereClause = "c_invoiceline_ID=?";
		List<MCostDetail> cds = new Query(A_PO.getCtx(), MCostDetail.Table_Name,whereClause , A_PO.get_TrxName())
				.setParameters(ivl.getC_InvoiceLine_ID())
				.list();
		for (MCostDetail cd:cds)
			cd.deleteEx(true);
		for (MLandedCostAllocation lca:lcas)
		{

			lca.deleteEx(true);
		}
		return "";
	}


	private String InvoiceUpdateM_Product_PO(PO A_PO)
	{

		MInvoice invoice = (MInvoice)A_PO;
		if (invoice.isSOTrx())
			return "";
		final String whereClause = "M_Product_ID=?  and c_bpartner_ID =" + invoice.getC_BPartner_ID(); 
		for (MInvoiceLine ivl:invoice.getLines())
		{
			if (ivl.getM_Product_ID()==0)
				continue;
			MProductPO ppo = new Query(A_PO.getCtx(), MProductPO.Table_Name, whereClause, A_PO.get_TrxName())
					.setParameters(ivl.getM_Product_ID())
					.setOnlyActiveRecords(true)
					.setOrderBy("Discontinued desc")
					.first();
			if (ppo==null)
			{
				ppo = new MProductPO(A_PO.getCtx(), 0, A_PO.get_TrxName());
				ppo.setC_BPartner_ID(invoice.getC_BPartner_ID());
				ppo.setM_Product_ID(ivl.getM_Product_ID());
				ppo.setC_UOM_ID(ivl.getM_Product().getC_UOM_ID());
				ppo.setVendorProductNo(ivl.getM_Product().getValue());
				ppo.setPriceList(ivl.getPriceActual());
				ppo.setC_Currency_ID(invoice.getC_Currency_ID());
				ppo.saveEx();
			}
			if (ppo.isDiscontinued())
			{
				ppo.setDiscontinued(false);
				ppo.saveEx();
			}
		}
		return "";
	}


	private String RequisitionUpdatePriceLineNetamt(PO A_PO)
	{
		MRequisitionLine rLine = (MRequisitionLine)A_PO;
		if (A_PO.is_ValueChanged(MRequisitionLine.COLUMNNAME_M_Product_ID) || rLine.getC_BPartner_ID() == 0)
		{
			MProductPO[] ppos = MProductPO.getOfProduct(A_PO.getCtx(), rLine.getM_Product_ID(), A_PO.get_TrxName());
			for (MProductPO ppo:ppos)
			{
				rLine.setC_BPartner_ID(ppo.getC_BPartner_ID());
				if (rLine.getC_BPartner().getPO_PriceList_ID() != 0)
				{
					rLine.setPrice(rLine.getC_BPartner().getPO_PriceList_ID());
					rLine.setLineNetAmt();
				}

				break;
			}
		}
		if (A_PO.is_ValueChanged(MRequisitionLine.COLUMNNAME_Qty) 
				&& rLine.getC_BPartner_ID() != 0)
			rLine.setLineNetAmt();
		return "";
	}


	private String TransactionOrg_ID(PO A_PO)
	{
		MTransaction trx = (MTransaction)A_PO;
		MWarehouse wh = new MWarehouse(A_PO.getCtx(), trx.getM_Warehouse_ID(), A_PO.get_TrxName());
		trx.setAD_Org_ID(wh.getAD_Org_ID());
		return "";
	}


	private String MovementAfterPost(PO A_PO)
	{
		MMovement movement = (MMovement)A_PO;

		Doc doc = movement.getDoc();

		ArrayList<Fact> facts = doc.getFacts();
		// one fact per acctschema
		for (Fact fact:facts)
		{
			MAcctSchema as = fact.getAcctSchema();
			for (FactLine fLine:fact.getLines())
			{
				if (fLine.getM_Locator().getPriorityNo() != 100)
					continue;
				ProductCost m_productCost = new ProductCost (Env.getCtx(),
						fLine.getM_Product_ID(), 0, movement.get_TrxName());
				MAccount pedTransito = m_productCost.getAccount (ProductCost.ACCTTYPE_P_InventoryClearing, as);
				fLine.setAccount_ID(pedTransito.getAccount_ID());				
			}
		}

		return "";
	}







	private String assignPrepayment(PO A_PO)
	{
		MInvoice invoice = (MInvoice)A_PO;
		if (invoice.getC_Order_ID() == 0)
			return "";
		if (invoice.getC_Order_ID() != 0 && invoice.getC_Order().getC_POS_ID() != 0)
			return "";
		String whereClause = "C_ORDER_ID=? and docstatus in ('CO','CL') and isallocated = 'N'";
		List<MPayment> prepayments = new Query(A_PO.getCtx(), MPayment.Table_Name, whereClause, A_PO.get_TrxName())
				.setParameters(invoice.getC_Order_ID())
				.list();
		for (MPayment pay:prepayments)
		{
			MAllocationHdr alloc = new MAllocationHdr (Env.getCtx(), true,	//	manual
					invoice.getDateInvoiced()	, invoice.getC_Currency_ID(), Env.getContext(Env.getCtx(), "#AD_User_Name"), 
					A_PO.get_TrxName());
			alloc.setAD_Org_ID(invoice.getAD_Org_ID());
			alloc.saveEx();
			BigDecimal PaymentAmt = pay.getPayAmt();

			BigDecimal amount = PaymentAmt;
			if (amount.abs().compareTo(PaymentAmt.abs()) > 0)  // if there's more open on the invoice
				amount = PaymentAmt;							// than left in the payment
			BigDecimal OverUnderAmt = invoice.getGrandTotal().subtract(pay.getPayAmt());
			//	Allocation Line
			MAllocationLine aLine = new MAllocationLine (alloc, amount, 
					Env.ZERO, Env.ZERO, OverUnderAmt);
			aLine.setDocInfo(invoice.getC_BPartner_ID(), invoice.getC_Order_ID(), invoice.getC_Invoice_ID());
			aLine.setPaymentInfo(pay.getC_Payment_ID(), 0);
			aLine.saveEx();		
			if (!alloc.processIt(DocAction.ACTION_Complete)) //@Trifon
				throw new AdempiereException("Cannot complete allocation: " + alloc.getProcessMsg()); //@Trifon
			alloc.saveEx();
		}
		return "";
	}


	private String OrderSearchDocType(PO A_PO)
	{
		MOrder order = (MOrder)A_PO;
		MBPartner bp = (MBPartner)order.getC_BPartner();
		int c_DocType_ID =0;
		if (order.getC_DocTypeTarget_ID() !=0)
			return "";
		if (order.isSOTrx())
		{
			c_DocType_ID = bp.get_ValueAsInt("C_DocType_ID");
			if (c_DocType_ID ==0)
				return"";
		}
		else
		{
			c_DocType_ID = bp.get_ValueAsInt("C_DocTypePO_ID");
			if (c_DocType_ID ==0)
				return"";
		}	
		MDocType dt = new MDocType(A_PO.getCtx(), c_DocType_ID, A_PO.get_TrxName());
		if (dt.getC_DocTypeInvoice_ID() !=0)
			order.setC_DocTypeTarget_ID(dt.getC_DocTypeInvoice_ID());

		return "";
	}


	


	private String MInvoiceControlCostdistribution(PO A_PO)
	{
		MInvoice inv = (MInvoice)A_PO;
		if (inv.isSOTrx()|| inv.getReversal_ID()!=0)
			return"";
		MDocType dt = (MDocType)inv.getC_DocType();
		if (dt.get_ValueAsBoolean("isCostDistribution") && inv.get_ValueAsInt("SHW_CostDistribution_ID") ==0)
			return "Falta la definición del retaceo";
		int shw_CostDistribution_ID = inv.get_ValueAsInt("SHW_CostDistribution_ID");
		if (shw_CostDistribution_ID == 0)
			return "";
		for (MInvoiceLine invLine:inv.getLines())
		{
			if(invLine.get_ValueAsInt("shw_CostDistribution_ID") !=0)
			{
				if (invLine.getC_OrderLine_ID() ==0)
					continue;
				MOrderLine oLine = (MOrderLine)invLine.getC_OrderLine();
				oLine.set_ValueOfColumn("SHW_CostDistribution_ID", invLine.get_ValueAsInt("shw_CostDistribution_ID"));
				oLine.saveEx();
				continue;
			}
			invLine.set_ValueOfColumn("SHW_CostDistribution_ID", shw_CostDistribution_ID);
			invLine.saveEx();
			MOrderLine oLine = (MOrderLine)invLine.getC_OrderLine();
			if(!oLine.is_new()) {
				oLine.set_ValueOfColumn("SHW_CostDistribution_ID", invLine.get_ValueAsInt("shw_CostDistribution_ID"));
				oLine.saveEx();
			}
		}
		return "";
	}




	private Boolean isDocument(PO A_PO)
	{
		String sql = "select count(*) from ad_column where ad_table_ID=? and columnname = 'DateAcct'";
		ArrayList<Object> params = new ArrayList<Object>();
		params.add(A_PO.get_Table_ID());
		int no = DB.getSQLValueEx(A_PO.get_TrxName(), sql, params.toArray());
		return no!=0;
	}






	private String CreateHRMovementAssignation(PO A_PO)
	{

		MHRProcess process = (MHRProcess)A_PO;
		MBPartner[] linesEmployee = MHREmployee.getEmployees(process);
		ArrayList<Object> params = new ArrayList<>();
		for (MBPartner bpartner:linesEmployee)
		{
			params.clear();
			params.add(bpartner.getC_BPartner_ID());
			params.add(process.getHR_Process_ID());
			String whereClause = " c_invoice_ID is not null and c_bpartner_ID=? and hr_process_ID=?";
			List<MHRMovement> movements = new Query(process.getCtx(), MHRMovement.Table_Name,
					whereClause, process.get_TrxName()) 
					.setParameters(params)
					.setOnlyActiveRecords(true)
					.list();
			for (MHRMovement move:movements)
			{BigDecimal allocationAmt = move.getAmount();			//	underpayment
			MInvoice invoice = new MInvoice(process.getCtx(), move.get_ValueAsInt("C_Invoice_ID"), process.get_TrxName());
			MAllocationHdr alloc = new MAllocationHdr(process.getCtx(), false, 
					process.getDateAcct(), 100, "planilla " + process.getName(), process.get_TrxName());
			alloc.setAD_Org_ID(process.getAD_Org_ID());
			alloc.setDateAcct(process.getDateAcct()); // in case date acct is different from datetrx in payment
			alloc.saveEx();
			MAllocationLine aLine = null;
			aLine = new MAllocationLine (alloc, allocationAmt, 
					Env.ZERO, Env.ZERO	, invoice.getOpenAmt().subtract(allocationAmt));
			aLine.setDocInfo(move.getC_BPartner_ID(), 0, invoice.getC_Invoice_ID());
			aLine.set_ValueOfColumn("HR_Movement_ID", move.getHR_Movement_ID());
			aLine.saveEx(process.get_TrxName());
			//	Should start WF
			alloc.processIt(DocAction.ACTION_Complete);
			alloc.setPosted(true);
			alloc.saveEx(process.get_TrxName());
			}
		}
		return "";
	}

	private String EmployeeCreateAttribute(PO A_PO)
	{
		MHREmployee employee = (MHREmployee)A_PO;
		Timestamp now = Env.getContextAsDate(employee.getCtx(), "#Date");
		StringBuffer whereClause = new StringBuffer();
		ArrayList<Object> params = new ArrayList<>();
		params.add(employee.getC_BPartner_ID());
		params.add(1000195);
		params.add(now);
		params.add(now);
		whereClause.append(MHRAttribute.COLUMNNAME_C_BPartner_ID + "=? AND ");
		whereClause.append(MHRAttribute.COLUMNNAME_HR_Concept_ID + "=? AND ");
		whereClause.append(MHRAttribute.COLUMNNAME_ValidFrom + ">=? AND ");
		whereClause.append("(" + MHRAttribute.COLUMNNAME_ValidTo + " is null or " + MHRAttribute.COLUMNNAME_ValidTo + "<?)");
		MHRAttribute att = new Query(employee.getCtx(), MHRAttribute.Table_Name, whereClause.toString(), employee.get_TrxName())
				.setParameters(params)	
				.setOnlyActiveRecords(true)
				.first();
		if (att == null)
		{
			att = new MHRAttribute(employee.getCtx(), 0, employee.get_TrxName());
			att.setHR_Concept_ID(1000195);
			att.setC_BPartner_ID(employee.getC_BPartner_ID());
			att.setValidFrom(now);
			att.setColumnType("A");
		}
		if (employee.getMonthlySalary().signum()!=0)
			att.setAmount(employee.getMonthlySalary());
		else 
			att.setAmount(employee.getDailySalary());
		att.saveEx();
		params.clear();
		params.add(employee.getC_BPartner_ID());
		params.add(1000206);
		params.add(now);
		params.add(now);
		att = new Query(employee.getCtx(), MHRAttribute.Table_Name, whereClause.toString(), employee.get_TrxName())
				.setOnlyActiveRecords(true)
				.setParameters(params)
				.first();
		if (att == null)
		{
			att = new MHRAttribute(employee.getCtx(), 0, employee.get_TrxName());
			att.setHR_Concept_ID(1000206);
			att.setC_BPartner_ID(employee.getC_BPartner_ID());
			att.setValidFrom(now);
			att.setColumnType("Q");
		}
		if (employee.getHR_EmployeeType().getWageLevel().equals(X_HR_EmployeeType.WAGELEVEL_Monthly))
		{			
			att.setQty(Env.ONE);			
		}
		else if (employee.getHR_EmployeeType().getWageLevel().equals(X_HR_EmployeeType.WAGELEVEL_Daily))
			att.setQty(new BigDecimal(6.0));
		else att.setQty(new BigDecimal(2.0));
		att.saveEx();
		return "";
	}


	private String test(PO A_PO)
	{
			return "";
	}
	
	
	private String InoutLineChangeRetaceo(PO A_PO)
	{
		if (!A_PO.is_ValueChanged("C_OrderLine_ID"))
			return "";
		int C_OrderLine_ID = A_PO.get_ValueAsInt("C_OrderLine_ID");
		String sql = "Select shw_Costdistribution_ID from c_orderline where C_Orderline_ID=?";
		int SHW_CostDistribution_ID = DB.getSQLValueEx(null, sql, C_OrderLine_ID);
		if (SHW_CostDistribution_ID >0)
		{
			A_PO.set_ValueOfColumn("SHW_CostDistribution_ID", SHW_CostDistribution_ID);
		}

		return "";
	}

	private String factAcct_UpdateDocumentNO(PO A_PO)

	{	
		Doc doc = A_PO.getDoc();
		String dateacct = "DateAcct";
		String Documentno = "";
		if (A_PO instanceof MInventory
				|| A_PO instanceof MMovement
				|| A_PO instanceof MProjectIssue
				|| A_PO instanceof MProduction)
			dateacct = "MovementDate";
		else if (A_PO instanceof MBankStatement)
			dateacct = "StatementDate";
		else if (A_PO instanceof MDDOrder)
			dateacct = "DateOrdered";
		else if (A_PO instanceof MRequisition)
			dateacct = "DateRequired";
		else if (A_PO instanceof MPPOrder)
			dateacct = "DateOrdered";
		Timestamp date = (Timestamp)A_PO.get_Value(dateacct);
		String whereClause = "dateacct =? and documentno is not null and documentno <> ''  and postingtype = 'A' ";
		MFactAcct factacct = new Query(A_PO.getCtx(), MFactAcct.Table_Name, whereClause, A_PO.get_TrxName())
				.setParameters(A_PO.get_Value(dateacct))
				.setClient_ID()
				.first();
		if (factacct != null)
			Documentno = factacct.get_ValueAsString("DocumentNo");
		else
		{
			Documentno = DB.getDocumentNo(A_PO.getAD_Client_ID(), MFactAcct.Table_Name, A_PO.get_TrxName());			
		}


		ArrayList<Fact> facts = doc.getFacts();
		// one fact per acctschema
		for (Fact fact:facts)
		{
			for (FactLine fLine:fact.getLines())
			{
				fLine.set_ValueOfColumn("DocumentNo", Documentno);
			}
		}		
		return "";
	}

	private String CreateBankStatement(PO A_PO)
	{
		MPayment pay = (MPayment)A_PO;
		MBankAccount ba = (MBankAccount)pay.getC_BankAccount();
		if (!ba.get_ValueAsBoolean("IsCreateBankStatementLine"))
			return "";
		
		return "";
	}



	public String AfterPost_CorrectGL_Category(PO po)	
	{
		MAllocationHdr ah = (MAllocationHdr)po;

		Doc doc = ah.getDoc();

		ArrayList<Fact> facts = doc.getFacts();
		// one fact per acctschema
		String description = "";
		for (Fact fact:facts)
		{
			for (FactLine fLine:fact.getLines())
			{
				description = "";
				Boolean isPayment = false;
				MAllocationLine alo = new MAllocationLine(po.getCtx(), fLine.getLine_ID(), po.get_TrxName());
				if (alo.getC_Payment_ID() !=0)
				{
					fLine.setGL_Category_ID(alo.getC_Payment().getC_DocType().getGL_Category_ID());

					description = "Pago: " + alo.getC_Payment().getDocumentNo();
					continue;
				}
				else if (alo.getC_CashLine_ID()!=0)
				{
					if (alo.getC_CashLine().getC_Invoice_ID() != 0)
						isPayment = alo.getC_Invoice().getC_DocType().getDocBaseType().equals(MDocType.DOCBASETYPE_APPayment)?
								true:false;
					else 
					{
						isPayment = alo.getAmount().compareTo(Env.ZERO)>=0?true:false;
					}

				}
				MDocType dt = null;
				if (isPayment)
					dt = new Query(po.getCtx(), MDocType.Table_Name, "Docbasetype = 'APP'", null)
					.first();
				else
					dt = new Query(po.getCtx(), MDocType.Table_Name, "Docbasetype = 'ARR'", null)
					.first();
				fLine.setGL_Category_ID(dt.getGL_Category_ID());
				if (alo.getC_Invoice_ID() != 0)
					description = description + " Factura: " + alo.getC_Invoice().getDocumentNo();
				else if (alo.getC_Charge_ID() != 0)
					description = description + " Cargo: " + alo.getC_Charge().getName();
			}
		}	
		return "";
	}
	public String OrderLineUpdateASI(PO A_PO, int A_Type)
	{
	//import org.compiere.model.MOrderLine;
	//import org.compiere.model.MProduct;
	//import org.compiere.model.MStorage;
	//import org.compiere.model.MClient;
	//import org.compiere.util.Env;

	//import java.math.BigDecimal;
	//import java.sql.Timestamp;

	        MInOutLine ioLine = (MInOutLine)A_PO;
	        BigDecimal qtyDelivered = Env.ZERO;
	        MOrderLine oLine = (MOrderLine)ioLine.getC_OrderLine();
	        String AsiDescription = "";
	        Boolean isInstance = true;
	        if (!oLine.getParent().isSOTrx())
	            return "";
	        if (ioLine.getM_Product().getM_AttributeSet_ID() !=0 && oLine.getM_AttributeSetInstance_ID() !=0)
	        {
	        	MAttributeSet mas = MAttributeSet.get(ioLine.getCtx(), oLine.getM_Product().getM_AttributeSet_ID());
	        	if (mas.isInstanceAttribute() && !mas.isLotMandatory())
	        		AsiDescription = oLine.getM_AttributeSetInstance().getDescription();
				isInstance = mas.isInstanceAttribute() && mas.isLotMandatory();
	        }
	        if (isInstance && oLine.getM_AttributeSetInstance_ID() !=0)
	        	return "";
	        if (A_Type==1
	        		|| (A_Type == 2 && A_PO.is_ValueChanged(MOrderLine.COLUMNNAME_QtyEntered) && ioLine.getM_AttributeSetInstance_ID()!=0))
	        		ioLine.setM_AttributeSetInstance_ID(0);
	            if (ioLine.getM_Product_ID() ==0 || ioLine.getM_AttributeSetInstance_ID() != 0)
	                return "";
	            MProduct product = (MProduct)ioLine.getM_Product();
	            if ( !product.isASIMandatory(true, ioLine.getAD_Org_ID()))
	                    return"";
	            String MMPolicy = product.getMMPolicy();
	            MOrderLine orderLine = (MOrderLine)ioLine.getC_OrderLine();
	            Timestamp minGuaranteeDate = orderLine.getParent().getDatePromised();
	            MStorage[] storages = MStorage.getWarehouse(ioLine.getCtx(),ioLine.getParent(). getM_Warehouse_ID(), ioLine.getM_Product_ID(), 
	            		ioLine.getM_AttributeSetInstance_ID(),
	                    minGuaranteeDate, MClient.MMPOLICY_FiFo.equals(MMPolicy), true, 0, ioLine.get_TrxName());
	            BigDecimal qtyToDeliver = orderLine.getQtyOrdered();
	            Boolean start = true;
	            for (MStorage storage: storages)
	            {
	            	if (AsiDescription != "" && !storage.getM_AttributeSetInstance().getDescription().equals(AsiDescription)) 
            		continue;
	                BigDecimal qtyavailable = storage.getQtyOnHand();
	                if (qtyavailable.compareTo(Env.ZERO) <= 0)
	                	continue;
	            	if (!start)
	            	{
	                	MInOutLine oLinenew = new MInOutLine(ioLine.getParent());
	                	MInOutLine.copyValues(ioLine, oLinenew);
	                	BigDecimal qtyToOrder = qtyToDeliver.compareTo(qtyavailable)<0? qtyToDeliver: qtyavailable;
	                	qtyToDeliver = qtyToDeliver.subtract(qtyToOrder);
	                	oLinenew.setQty(qtyToOrder);
	                	oLinenew.setM_AttributeSetInstance_ID(0);
	                	oLinenew.saveEx();                    
	            	}
	            	
	                if (qtyavailable.compareTo(qtyToDeliver) >= 0)
	                {
	                	ioLine.setQty(qtyToDeliver);
	                	ioLine.setM_AttributeSetInstance_ID(storage.getM_AttributeSetInstance_ID());
	                	ioLine.setM_Locator_ID(storage.getM_Locator_ID());
	                    qtyToDeliver = Env.ZERO;
	                    qtyDelivered = ioLine.getMovementQty();
	                    start = false;
	                }
	                else
	                {
	                	ioLine.setQty(qtyavailable);
	                	ioLine.setM_AttributeSetInstance_ID(storage.getM_AttributeSetInstance_ID());
	                	ioLine.setM_Locator_ID(storage.getM_Locator_ID());
	                    qtyDelivered = ioLine.getMovementQty();
	                	start = false;
	                }    
	                if (qtyToDeliver.signum() == 0)
	                    break;
	            }
	        return "";
	    }

	public String PaymentCompleteNote(PO A_PO, int A_Type)
	{
		MPayment pay = (MPayment)A_PO;
		if (pay.getC_Charge_ID() != 0)
			return "";
		String note = "";
		if (pay.getC_Invoice_ID() !=0)
		{
			note = "Factura # " + pay.getC_Invoice().getDocumentNo() + " de " + pay.getC_BPartner().getName();
			pay.set_ValueOfColumn("Note", note);
		}
		String whereClause = "C_Payment_ID=?";
		List< MAllocationLine> alos = new Query(A_PO.getCtx(), MAllocationLine.Table_Name, whereClause, A_PO.get_TrxName())
				.list();
		for (MAllocationLine alo:alos)
		{
			note = "Facturas # ";
			if (alo.getC_Invoice_ID() !=0)
				note = note + ", "  + alo.getC_Invoice().getDocumentNo() + " de " + alo.getC_BPartner().getName();
		}
		pay.set_ValueOfColumn("Note", note);
		return "";
	
	}
	


	public String confirmCompleteInout(PO A_PO)
	{
		MInOutConfirm confirm = (MInOutConfirm)A_PO;
		confirm.saveEx();
		log.severe("Reingesrpungen" + confirm.getDocumentInfo());
		Boolean fullconfirm = true;
		for (MInOutLineConfirm line:confirm.getLines(true))
		{
			if (line.getTargetQty().compareTo(line.getConfirmedQty())!=0)
			{
				fullconfirm = false;

				log.severe("Nicht fullconfirm" + confirm.getDocumentInfo());
				break;
			}
		}
		if (fullconfirm)
		{
			MInOut inout = (MInOut)confirm.getM_InOut();
			if (inout.processIt("CO"))
			{
				log.severe("Complete " + inout.getDocumentInfo());
				inout.saveEx();
			}
		}
		return "";
	}
	

	public String controlInvoiceTax(PO A_PO)
	{
		return "";
	}
	

	public String controlOrderTax(PO A_PO)
	{
		MOrderTax it = (MOrderTax)A_PO;
		MTax cTax = (MTax)it.getC_Tax();
		BigDecimal amt = it.isTaxIncluded()?it.getTaxAmt().add(it.getTaxBaseAmt()):it.getTaxBaseAmt();
		BigDecimal taxAmt = cTax.calculateTax(amt, it.getC_Order().isTaxIncluded(), MCurrency.getStdPrecision(A_PO.getCtx(), it.getC_Order().getC_Currency_ID()));
		if (taxAmt.compareTo(it.getTaxAmt()) !=0)
		{
			it.setTaxAmt(taxAmt);
		}
		return "";
	}

	public String InvoiceUpdateActivity(PO A_PO)
	{
		MAllocationHdr ah = (MAllocationHdr)A_PO;
		for (MAllocationLine alo:ah.getLines(true))
		{
			if (alo.getC_Invoice_ID() ==0 || alo.getC_Invoice()== null)
				return "";
			if (alo.getC_Invoice().getDateAcct() == ah.getDateAcct())
			{
				MInvoice invoice = (MInvoice)alo.getC_Invoice();
				int no = DB.executeUpdateEx("Delete from fact_Acct where ad_table_ID = 318 and record_ID = " + invoice.getC_Invoice_ID(), alo.get_TrxName());
				invoice.setPosted(false);
				invoice.setC_Activity_ID(1000145);
				invoice.saveEx();
			}
		}
		return "";
	}
	
	

}	//	MyValidator
