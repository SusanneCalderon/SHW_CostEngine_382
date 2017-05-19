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

/**
 *	Generate Invoices
 *	
 *  @author SHW
 *  @version $Id: SHW_InvoiceGenerate.java,v 1.2 2015/01/24 00:51:01 mc Exp $
 */
public class SHW_CancelSalesOrder extends SvrProcess
{
	/**	Date Invoiced			*/
	private Timestamp	p_DateInvoiced = null;
	/** Consolidate				*/
	private boolean		p_ConsolidateDocument = true;
	/** Invoice Document Action	*/
	private String		p_docAction = DocAction.ACTION_Complete;
	
	/**	The current Invoice	*/
	private MInvoice 	m_invoice = null;
	/**	The current Shipment	*/
	private MInOut	 	m_ship = null;
	/** Number of Invoices		*/
	private int			m_created = 0;
	/**	Line Number				*/
	private int			m_line = 0;
	/**	Business Partner		*/
	private MBPartner	m_bp = null;
	protected List<MOrder> m_records = null;
	protected List<MInvoice> m_invoices = null;
	
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
			else if (name.equals("ConsolidateDocument"))
				p_ConsolidateDocument = "Y".equals(para[i].getParameter());
			else if (name.equals("DocAction"))
				p_docAction = (String)para[i].getParameter();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}

		//	Login Date
		if (p_DateInvoiced == null)
			p_DateInvoiced = Env.getContextAsDate(getCtx(), "#Date");
		if (p_DateInvoiced == null)
			p_DateInvoiced = new Timestamp(System.currentTimeMillis());
	}	//	prepare

	/**
	 * 	Generate Invoices
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt () throws Exception
	{
	    	String whereClause = "c_order_ID =?";
	    	MOrder order_original = new MOrder(getCtx()	, getRecord_ID(), get_TrxName());
	    	MDocType dt = (MDocType)order_original.getC_DocType();
	    	if (dt.get_ValueAsInt("shw_c_doctype_orderReverse_ID") <=0)
	    		return "No se trata de orden de venta a factura: Tiene que generar una devolución";
	    	Timestamp now = Env.getContextAsDate(getCtx(), "Date");
	    	MOrder order_cancel = 
	    	MOrder.copyFrom(order_original, now, dt.get_ValueAsInt("shw_c_doctype_orderReverse_ID"), order_original.isSOTrx()	, false, false, get_TrxName());
	    	order_cancel.setC_DocType_ID(dt.get_ValueAsInt("shw_c_doctype_orderReverse_ID"));
	    	order_cancel.setDocumentNo(order_original.getDocumentNo() + "Anulación");
	    	order_cancel.saveEx();
	    	for (MOrderLine oLine:order_cancel.getLines())
	    	{
	    		oLine.setQty(oLine.getQtyEntered().negate());
	    		oLine.saveEx();
	    	}
	    	if (order_cancel.getC_DocTypeTarget().getDocSubTypeSO().equals(MDocType.DOCSUBTYPESO_OnCreditOrder))
	    	{
	    		
	    		order_cancel.processIt(MOrder.DOCACTION_Complete);
	    		order_cancel.setDocAction(MOrder.DOCACTION_Close);
	    		order_cancel.setDocStatus(MOrder.DOCACTION_Complete);
	    		order_cancel.saveEx();
	    	}
	    	int[] Invoice_IDs = MInvoice.getAllIDs(MInvoice.Table_Name, "c_Order_ID=" +getRecord_ID() , get_TrxName());	
	    	int laenge = Invoice_IDs.length;
	    	for (int i  = 0; i < laenge; i++)
	    	{
			int invoice_ID = Invoice_IDs[i];
	    		int[] Alo_IDs = MAllocationLine.getAllIDs(MAllocationLine.Table_Name, "C_Invoice_ID=" + invoice_ID, get_TrxName());
	    		for (int alo_ID:Alo_IDs)
	    		{
	    			MAllocationLine alo = new MAllocationLine(getCtx(), alo_ID, get_TrxName());
	    			if (alo.getC_Payment_ID() != 0)
	    			{
	    				MPayment pay_cancel = new MPayment(getCtx(), 0, get_TrxName());
	    				MPayment.copyValues((MPayment)alo.getC_Payment(), pay_cancel);
	    				pay_cancel.setDateTrx(order_cancel.getDateOrdered());
	    				pay_cancel.setPayAmt(pay_cancel.getPayAmt().negate());
	    				pay_cancel.setDescription(alo.getC_Payment().getDocumentNo() + "_Anulación");
	    				pay_cancel.save();
	    				for (int j:	MInvoice.getAllIDs(MInvoice.Table_Name, "c_Order_ID=" + order_cancel.getC_Order_ID(), get_TrxName()))
	    				{
	    					pay_cancel.setC_Invoice_ID(j);
	    					pay_cancel.setC_Order_ID(order_cancel.getC_Order_ID());
	    					pay_cancel.setDocStatus(MPayment.DOCSTATUS_Drafted);
	    					pay_cancel.setDocAction(MPayment.DOCACTION_Complete);
	    					pay_cancel.saveEx();
	    					pay_cancel.processIt(MPayment.DOCACTION_Complete);
	    				}
	    			}
	    			else
	    			{
	    				MAllocationHdr ahd = new MAllocationHdr(getCtx(), 0, get_TrxName());
	    				for (int k:	MInvoice.getAllIDs(MInvoice.Table_Name, "c_Order_ID=" + order_cancel.getC_Order_ID(), get_TrxName()))
	    				{
	        				MAllocationLine alo_new = new MAllocationLine(ahd);
	        				alo_new.setC_Invoice_ID(k);
	        				alo_new.setAmount(alo.getAmount().negate());
	        				alo_new.saveEx();
	    				}
	    				ahd.processIt(MAllocationHdr.DOCACTION_Complete);
	    			}
	    		}
	    	}
	    	return "No. Documento:" + " " +  order_cancel.getDocumentNo();
	}	//	doIt
	
	
	
}	//	InvoiceGenerate
