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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.compiere.apps.AEnv;
import org.compiere.apps.AWindow;
import org.compiere.model.MBPartner;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MQuery;
import org.compiere.model.MRequisitionLine;
import org.compiere.model.MTable;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.Env;
import org.eevolution.grid.Browser;
import org.eevolution.model.MWMInOutBoundLine;

/**
 *	Generate Invoices
 *	
 *  @author SHW
 */
public class SHW_CreateInvoiceFromOrder extends SvrProcess
{
	private boolean		p_ConsolidateDocument = true;
	/** Invoice Document Action	*/
	private String		p_docAction = DocAction.ACTION_Complete;
	
	/**	The current Invoice	*/
	private MInvoice 	m_invoice = null;
	private MBPartner 	m_bpartner = null;
	private int 		m_line = 0;
	/**	The current Shipment	*/
	protected LinkedHashMap<Integer, LinkedHashMap<String, Object>> m_values = null;
	protected List<MOrderLine> m_records = null;
	protected List<MInvoice> m_invoices = null;
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		for (ProcessInfoParameter para : getParameter()) {
			String name = para.getParameterName();
			if (para.getParameter() == null)
				;
			//SHW
			else if (name.equals("ConsolidateDocument"))
				p_ConsolidateDocument = para.getParameterAsBoolean();
			else if (name.equals("DocAction"))
				p_docAction = para.getParameterAsString();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}

		//	Login Date
		//	DocAction check
		if (!DocAction.ACTION_Complete.equals(p_docAction))
			p_docAction = DocAction.ACTION_Prepare;

		setColumnsValues();
	}	//	prepare

	/**
	 * 	Generate Invoices
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt () throws Exception
	{
/*
		for (MOrderLine oLine : getRecords()) {

			saveBrowseValues(oLine, "oLine");
		}*/
		getProcessInfo().setTableSelectionId(MOrderLine.Table_ID);
		List<MOrderLine> m_records = (List<MOrderLine>) getInstancesForSelection(get_TrxName());
		m_records.stream().forEach(oLine -> {
			saveBrowseValues(oLine, "oLine");
		});
		m_records.stream().forEach(oLine -> {
			generate(oLine);
		});

		String whereClauseWindow = "c_invoice_ID in (";
		for (MInvoice inv:m_invoices)
		{
			whereClauseWindow = whereClauseWindow + inv.getC_Invoice_ID() + ",";
		}
		whereClauseWindow = whereClauseWindow.substring(0, whereClauseWindow.length() -1) + ")";
		MQuery query = new MQuery("");
		MTable table = new MTable(getCtx(), MInvoice.Table_ID, get_TrxName());
		query.addRestriction(whereClauseWindow);
		query.setRecordCount(m_invoices.size());
		int AD_WindowNo = table.getAD_Window_ID();
		commitEx();
		zoom (AD_WindowNo, query);
		return "";
	}	//	doIt
	
	
	/**
	 * 	Generate Shipments
	 * 	@param pstmt order query 
	 *	@return info
	 */
	private String generate (MOrderLine oLine)
	{
		
		{
			createInvoice(oLine);
		
		}
		return "";
	}	//	generate
	

	private List<MOrderLine> getRecords() {
		if (m_records != null)
			return m_records;

		String whereClause = "EXISTS (SELECT T_Selection_ID FROM T_Selection WHERE  T_Selection.AD_PInstance_ID=? AND T_Selection.T_Selection_ID=c_orderLine.C_Orderline_ID)";
		m_records = new Query(getCtx(), MOrderLine.Table_Name, whereClause,
				get_TrxName()).setClient_ID()
				.setParameters(getAD_PInstance_ID()).list();
		return m_records;
	}
	

	
	private void saveBrowseValues(PO po, String alias) {

		LinkedHashMap<String, Object> values = m_values.get(po.get_ID());

		for (Entry<String, Object> entry : values.entrySet()) {
			String columnName = entry.getKey();
			if (columnName.contains(alias.toUpperCase() + "_")) {
				columnName = columnName.substring(columnName.indexOf("_") + 1);
				po.set_ValueOfColumn(columnName, entry.getValue());
				po.saveEx();
			}
		}
	}
	
	private void	createInvoice(MOrderLine oLine)
	{

		if (m_invoice == null || m_bpartner.getC_BPartner_ID() != oLine.getC_BPartner_ID())
		{
			//if (m_invoice != null)
			//	m_invoice.processIt(MInvoice.DOCACTION_Complete);
			m_invoice = new MInvoice(oLine.getParent(), oLine.getC_Order().getC_DocType().getC_DocTypeInvoice_ID(), Env.getContextAsDate(getCtx(), "#Date"));
			
			m_invoice.set_ValueOfColumn("DocumentoDeTransporte", oLine.get_ValueAsString("DocumentoDeTransporte"));
			m_invoice.set_ValueOfColumn("ReferenciaDeDeclaracion", oLine.get_ValueAsString("ReferenciaDeDeclaracion"));
			m_invoice.set_ValueOfColumn("Provider", oLine.get_ValueAsString("Provider"));
			m_invoice.set_ValueOfColumn("ProviderPO", oLine.get_ValueAsString("ProviderPO"));
			m_invoice.set_ValueOfColumn("CodigoDeDeclaracion", oLine.get_ValueAsString("CodigoDeDeclaracion"));	
			m_invoice.setDescription(oLine.getParent().getC_Project().getName());
			m_invoice.saveEx();
			m_bpartner = (MBPartner)m_invoice.getC_BPartner();
			m_line = 0;
			m_invoices.add(m_invoice);
		}
		MInvoiceLine iLine = new MInvoiceLine(m_invoice);
		String DocumentoDeTransporte = m_invoice.get_ValueAsString("DocumentoDeTransporte").concat(", ").concat(oLine.get_ValueAsString("DocumentoDeTransporte"));
		String ReferenciaDeDeclaracion = m_invoice.get_ValueAsString("ReferenciaDeDeclaracion").concat(", ").concat(oLine.get_ValueAsString("ReferenciaDeDeclaracion"));
		String Provider = m_invoice.get_ValueAsString("Provider").concat(", ").concat(oLine.get_ValueAsString("Provider"));
		String ProviderPO = m_invoice.get_ValueAsString("ProviderPO").concat(", ").concat(oLine.get_ValueAsString("ProviderPO"));
		String CodigoDeDeclaracion = m_invoice.get_ValueAsString("CodigoDeDeclaracion").concat(", ").concat(oLine.get_ValueAsString("CodigoDeDeclaracion"));
		
		m_invoice.set_ValueOfColumn("DocumentoDeTransporte", DocumentoDeTransporte);
		m_invoice.set_ValueOfColumn("ReferenciaDeDeclaracion", ReferenciaDeDeclaracion);
		m_invoice.set_ValueOfColumn("Provider", Provider);
		m_invoice.set_ValueOfColumn("ProviderPO", ProviderPO);
		m_invoice.set_ValueOfColumn("CodigoDeDeclaracion", CodigoDeDeclaracion);		
		m_invoice.setDescription(m_invoice.getDescription().concat(", ").concat(oLine.getParent().getC_Project().getName()));
		m_invoice.saveEx();

		iLine.setOrderLine(oLine);
		iLine.setQtyInvoiced(oLine.getQtyInvoiced());
		LinkedHashMap<String, Object> values = m_values.get(oLine.get_ID());
		for (Entry<String, Object> entry : values.entrySet()) {
			String columnName = entry.getKey();
			if (columnName.contains("OLINE".toUpperCase() + "_")) {
				columnName = columnName.substring(columnName.indexOf("_") + 1);
				if (columnName.equals(MOrderLine.COLUMNNAME_QtyInvoiced))
					iLine.setQtyEntered((BigDecimal)entry.getValue());
				
			}
		}
		iLine.setQtyInvoiced(iLine.getQtyInvoiced());
		iLine.setLine(m_line + 10);
		iLine.saveEx();
	}
	

	private LinkedHashMap<Integer, LinkedHashMap<String, Object>> setColumnsValues() {
		if (m_values != null)
			return m_values;

		m_values = new LinkedHashMap<Integer, LinkedHashMap<String, Object>>();

		for (MOrderLine record : getRecords()) {
			m_values.put(
					record.get_ID(),
					Browser.getBrowseValues(getAD_PInstance_ID(), null,
							record.get_ID(), null));
		}
		return m_values;
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

	
}	//	InvoiceGenerate
