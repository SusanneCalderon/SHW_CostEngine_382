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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.compiere.apps.AEnv;
import org.compiere.apps.AWindow;
import org.compiere.model.MAllocationHdr;
import org.compiere.model.MAllocationLine;
import org.compiere.model.MBPartner;
import org.compiere.model.MClient;
import org.compiere.model.MDocType;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MInvoiceSchedule;
import org.compiere.model.MLocation;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MPayment;
import org.compiere.model.MQuery;
import org.compiere.model.MTable;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.Language;
import org.eevolution.model.MHREmployee;
import org.eevolution.model.MHRMovement;
import org.eevolution.model.MHRProcess;

/**
 *	Generate Invoices
 *	
 *  @author SHW
 *  @version $Id: SHW_InvoiceGenerate.java,v 1.2 2015/01/24 00:51:01 mc Exp $
 */
public class SHW_MovementCreateAllocation extends SvrProcess
{
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}

		//	Login Date
	}	//	prepare

	/**
	 * 	Generate Invoices
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt () throws Exception
	{

		MHRProcess process = new MHRProcess(getCtx(), getRecord_ID(), get_TrxName());
		if (process.getReversal_ID() !=0)
			return "";
		ArrayList<Object> params = new ArrayList<>();
			params.add(process.getHR_Process_ID());
			String whereClause = " c_invoice_ID is not null and hr_process_ID=?";
			List<MHRMovement> movements = new Query(process.getCtx(), MHRMovement.Table_Name,
					whereClause, process.get_TrxName()) 
			.setParameters(params)
			.setOnlyActiveRecords(true)
			.list();
			for (MHRMovement move:movements)
			{
				BigDecimal allocationAmt = move.getAmount();			//	underpayment
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
		return "";
	}	//	doIt
	
	
	
}	//	InvoiceGenerate
