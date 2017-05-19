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

import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.model.MInvoice;
import org.compiere.model.MPInstance;
import org.compiere.model.MPInstancePara;
import org.compiere.model.MProcess;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.Trx;

/**
 *  Creates Payment from c_invoice, including Aging
 *
 *  @author Susanne Calderon
 */

public class shw_CalculateTaxBatch  extends SvrProcess
{
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] parameters = getParameter();
		for (ProcessInfoParameter para : parameters) {
			String name = para.getParameterName();
			if (para.getParameter() == null)
				;
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}

	}	//	prepare

	/**
	 * 	Generate Invoices
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt () throws Exception
	{
		Properties A_Ctx = getCtx();
		String A_TrxName = get_TrxName();
		int A_AD_PInstance_ID = getAD_PInstance_ID();


		List<MInvoice> m_records = null;

		String whereClause = "EXISTS (SELECT T_Selection_ID FROM T_Selection WHERE  T_Selection.AD_PInstance_ID=? " +
				" AND T_Selection.T_Selection_ID=C_Invoice.C_Invoice_ID)";
		m_records = new Query(A_Ctx, MInvoice.Table_Name, whereClause, A_TrxName)
		.setParameters(A_AD_PInstance_ID)
		.setClient_ID()
		.list();
		for (MInvoice invoice:m_records)
		{

			int AD_Process_ID = 53072;
			MPInstance instance = new MPInstance(A_Ctx, AD_Process_ID, 0);
			if (!instance.save())
			{
				return null;
			}
			//call process
			ProcessInfo pi = new ProcessInfo ("CalculateTax", AD_Process_ID);
			pi.setAD_PInstance_ID (instance.getAD_PInstance_ID());

			MPInstancePara ip = new MPInstancePara(instance, 10);
			ip.setParameter("C_Invoice_ID",invoice.getC_Invoice_ID());
			if (!ip.save())
			{
				
				String msg = "No Parameter added";  //  not translated
				log.log(Level.SEVERE, msg);
				return msg;
			}
				pi.setIsBatch(true);
				MProcess worker = new MProcess(getCtx(),AD_Process_ID,get_TrxName());
				worker.processIt(pi, Trx.get(get_TrxName(), true));
			} 
			
			//	doIt
		return "";
	}
}
