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

import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.apps.AEnv;
import org.compiere.apps.AWindow;
import org.compiere.model.MInOut;
import org.compiere.model.MInvoice;
import org.compiere.model.MOrder;
import org.compiere.model.MQuery;
import org.compiere.model.MRequest;
import org.compiere.model.MSession;
import org.compiere.model.MTable;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.Env;
 
/**
 * Create (Generate) Shipment from Invoice
 *	
 * @author Jorg Janke
 * @version $Id: InvoiceCreateInOut.java,v 1.2 2006/07/30 00:51:02 jjanke Exp $
 * 
 * @author Teo Sarca, www.arhipac.ro
 * 			<li>FR [ 1895317 ] InvoiceCreateInOut: you can create many receipts
 */
public class SHW_OrderCreateInOut extends SvrProcess
{
	public static final String PARAM_M_Warehouse_ID = MInOut.COLUMNNAME_M_Warehouse_ID;
	
	/**	Warehouse			*/
	private int p_M_Warehouse_ID = 0;
	private int p_R_Request_ID = 0;
	private int p_C_DocType_ID = 0;
	/** Receipt				*/
	private MInOut m_inout = null;

	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		for (ProcessInfoParameter para : getParameter())
		{
			String name = para.getParameterName();
			if (para.getParameter() == null)
				;
			else if (name.equals(PARAM_M_Warehouse_ID))
				p_M_Warehouse_ID = para.getParameterAsInt();
			else if (name.equals(MInOut.COLUMNNAME_C_DocType_ID))
				p_C_DocType_ID = para.getParameterAsInt();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
		p_R_Request_ID = getRecord_ID();
	}	//	prepare

	
	/**
	 * 	Create Shipment
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt () throws Exception
	{
		MRequest req = new MRequest(getCtx(), p_R_Request_ID, get_TrxName());
		if (req.getC_Order_ID() ==0)

			throw new AdempiereException("@NotFound@ @C_Order_ID@");
		//
		MOrder order = (MOrder)req.getC_Order();
		if (!MInvoice.DOCSTATUS_Completed.equals(order.getDocStatus()))
			throw new AdempiereException("@InvoiceCreateDocNotCompleted@");
		//
		m_inout = new MInOut(order, p_C_DocType_ID, Env.getContextAsDate(getCtx(), "#Date"));
		m_inout.setM_Warehouse_ID(p_M_Warehouse_ID);
		m_inout.saveEx();
		req.setM_InOut_ID(m_inout.getM_InOut_ID());
		req.saveEx();
		//
		String whereClauseWindow = "m_inout_ID=" + m_inout.getM_InOut_ID();
		MQuery query = new MQuery("");
		MTable table = new MTable(getCtx(), MInOut.Table_ID, get_TrxName());
		query.addRestriction(whereClauseWindow);
		query.setRecordCount(1);
		int AD_WindowNo = table.getPO_Window_ID();
		int ad_session_ID  = Env.getContextAsInt(getCtx(), "AD_Session_ID");
		MSession session = new MSession(getCtx(), ad_session_ID, null);
		
		if (session.getWebSession() == null ||session.getWebSession().length() == 0)
		{
			commitEx();
			zoom (AD_WindowNo, query);
			return "";
			
		}
		return m_inout.getDocumentNo();
	}	//	doIt
	


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
}	//	InvoiceCreateInOut
