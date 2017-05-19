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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.compiere.apps.AEnv;
import org.compiere.apps.AWindow;
import org.compiere.model.I_C_BPartner;
import org.compiere.model.I_C_DocType;
import org.compiere.model.I_C_Order;
import org.compiere.model.I_M_RequisitionLine;
import org.compiere.model.MBPartner;
import org.compiere.model.MDocType;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MPriceList;
import org.compiere.model.MProduct;
import org.compiere.model.MQuery;
import org.compiere.model.MRequisitionLine;
import org.compiere.model.MSession;
import org.compiere.model.MTable;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.eevolution.grid.Browser;

/**
 * 	Create PO from Requisition 
 *	
 *	
 *  @author SCalderon
 */
public class SHWOrderLineInoutLineCreate extends SvrProcess
{
	private int			p_C_Doctype_ID 				= 0;

	/** Consolidate			*/
	private boolean		p_ConsolidateDocument 		= false;
	
	private int 		p_C_Bpartner_ID 			= 0;
	private String 		p_POReference				= "";
	private int 		p_C_OrderSource_ID			= 0;

	/** Order				*/
	private MOrder		m_order = null;
	/** Order Line			*/
	private MOrderLine	m_orderLine = null;
	/** Orders Cache : (C_BPartner_ID, DateRequired, M_PriceList_ID) -> MOrder */
	private HashMap<MultiKey, MOrder> m_cacheOrders = new HashMap<MultiKey, MOrder>();
	private int 		m_M_Requisition_ID = 0;
	private int 		m_M_Product_ID = 0;
	private int			m_M_AttributeSetInstance_ID = 0;
	/** BPartner				*/
	private MBPartner	m_bpartner = null;
	private Timestamp			p_dateOrdered;
	protected LinkedHashMap<Integer, LinkedHashMap<String, Object>> m_values = null;
	protected List<MRequisitionLine> m_records = null;
	protected List<MOrder> m_orders = null;
	
	private String orderDocumentno = "Ordenes generados: ";
	
	
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
			else if (name.equals(I_C_DocType.COLUMNNAME_C_DocType_ID))
				p_C_Doctype_ID = para.getParameterAsInt();
			else if (name.equals(I_C_Order.COLUMNNAME_C_OrderSource_ID))
				p_C_OrderSource_ID = para.getParameterAsInt();
			else if (name.equals(I_C_BPartner.COLUMNNAME_C_BPartner_ID))
				p_C_Bpartner_ID = para.getParameterAsInt();
			else if (name.equals("ConsolidateDocument"))
				p_ConsolidateDocument = "Y".equals(para.getParameter());
			else if (name.equals(MOrder.COLUMNNAME_DateOrdered))
				p_dateOrdered = para.getParameterAsTimestamp();
			else if (name.equals(I_C_Order.COLUMNNAME_POReference))
				p_POReference = para.getParameterAsString();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
		if (p_C_Bpartner_ID != 0)
			m_bpartner = new MBPartner(getCtx(), p_C_Bpartner_ID, get_TrxName());
		

		setColumnsValues();
	}	//	prepare
	
	/**
	 * 	Process
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt() throws Exception
	{
		for (MRequisitionLine mrp : getMRPRecords()) {

			saveBrowseValues(mrp, "RL");
		}
		StringBuffer orderClause = new StringBuffer();
		m_orders = new ArrayList<MOrder>();
		if (!p_ConsolidateDocument)
		{
			orderClause.append("M_Requisition_ID, ");
		}
		orderClause.append("C_BPartner_ID, ");
		orderClause.append(" M_Product_ID, C_Charge_ID, user1_ID, user2_id, c_project_ID, c_campaign_ID, c_salesregion_ID");
		
		String whereClause = "EXISTS (SELECT T_Selection_ID FROM T_Selection WHERE  T_Selection.AD_PInstance_ID=? " +
				" AND T_Selection.T_Selection_ID=M_RequisitionLine.M_RequisitionLine_ID)";
		m_records = new Query(getCtx(), MRequisitionLine.Table_Name, whereClause, get_TrxName())
											.setParameters(getAD_PInstance_ID())
											.setOrderBy(orderClause.toString())
											.setClient_ID()
											.list();
		for (MRequisitionLine rLine:m_records)
			process(rLine);
		
		closeOrder();
		for (MOrder order:m_orders)
		{
			orderDocumentno = orderDocumentno + " " + order.getDocumentNo();
		}

	//	AEnv.zoom(MBPartner.Table_ID, C_BPartner_ID.intValue(), true);	//	SO
		String whereClauseWindow = "c_order_ID in (";
		for (MOrder order:m_orders)
		{
			whereClauseWindow = whereClauseWindow + order.getC_Order_ID() + ",";
		}
		whereClauseWindow = whereClauseWindow.substring(0, whereClauseWindow.length() -1) + ")";
		MQuery query = new MQuery("");
		MTable table = new MTable(getCtx(), MOrder.Table_ID, get_TrxName());
		query.addRestriction(whereClauseWindow);
		query.setRecordCount(m_orders.size());
		int AD_WindowNo = table.getPO_Window_ID();
		int ad_session_ID  = Env.getContextAsInt(getCtx(), "AD_Session_ID");
		MSession session = new MSession(getCtx(), ad_session_ID, null);
		
		if (session.getWebSession() == null ||session.getWebSession().length() == 0)
		{
			commitEx();
			zoom (AD_WindowNo, query);
			return "";
			
		}
		return "";
	}	//	doit
	
	/**
	 * 	Process Line
	 *	@param rLine request line
	 * 	@throws Exception
	 */
	private void process (MRequisitionLine rLine) throws Exception
	{
		if (rLine.getM_Product_ID() == 0 && rLine.getC_Charge_ID() == 0)
		{
			log.warning("Ignored Line" + rLine.getLine() 
				+ " " + rLine.getDescription()
				+ " - " + rLine.getLineNetAmt());
			return;
		}
		
		if (!p_ConsolidateDocument && rLine.getM_Requisition_ID() != m_M_Requisition_ID)
		{
			closeOrder();
		}
		if (m_orderLine == null
			|| rLine.getM_Product_ID() != m_M_Product_ID
			|| rLine.getM_AttributeSetInstance_ID() != m_M_AttributeSetInstance_ID
			|| rLine.getC_Charge_ID() != 0		//	single line per charge
			|| m_order == null
			)
		{
			newLine(rLine);
			// No Order Line was produced (vendor was not valid/allowed) => SKIP
			if (m_orderLine == null)
				return;
		}

		//	Update Order Line
		m_orderLine.setQty(m_orderLine.getQtyOrdered().add(rLine.getQty()));
		m_orderLine.saveEx();
		//	Update Requisition Line
		rLine.setC_OrderLine_ID(m_orderLine.getC_OrderLine_ID());
		rLine.saveEx();
	}	//	process
	
	/**
	 * 	Create new Order
	 *	@param rLine request line
	 *	@param C_BPartner_ID b.partner
	 * 	@throws Exception
	 */
	private void newOrder(MRequisitionLine rLine, int C_BPartner_ID) throws Exception
	{
		if (m_order != null)
		{
			closeOrder();
		}
		
		//	Order
		Timestamp DateRequired = rLine.getDateRequired();
		MultiKey key = new MultiKey(rLine.getC_BPartner_ID(), DateRequired);
		m_order = m_cacheOrders.get(key);
		if (m_order == null)
		{
			m_order = new MOrder(getCtx(), 0, get_TrxName());
			m_order.setAD_Org_ID(rLine.getParent().getM_Warehouse().getAD_Org_ID());
			m_order.setM_Warehouse_ID(rLine.getParent().getM_Warehouse_ID());
			if (p_dateOrdered == null)
				m_order.setDatePromised(DateRequired);
			else
				m_order.setDatePromised(p_dateOrdered);
			m_order.setIsSOTrx(false);
			if (p_C_Doctype_ID > 0)
				m_order.setC_DocTypeTarget_ID(p_C_Doctype_ID);
			else
				m_order.setC_DocTypeTarget_ID();

			if (p_C_OrderSource_ID > 0)
				m_order.setC_OrderSource_ID(p_C_OrderSource_ID);
			if (!p_POReference.equals(""))
				m_order.setPOReference(p_POReference);
			m_bpartner = new MBPartner(getCtx(), rLine.getC_BPartner_ID(), get_TrxName());
			m_order.setBPartner(m_bpartner);
			int M_PriceList_ID = m_bpartner.getPO_PriceList_ID();
			if (M_PriceList_ID <= 0)
			M_PriceList_ID = rLine.getParent().getM_PriceList_ID();
			if (M_PriceList_ID <= 0)
			M_PriceList_ID = MPriceList.getDefault(getCtx(), false).getM_PriceList_ID();
			m_order.setM_PriceList_ID(M_PriceList_ID);
			m_order.setSalesRep_ID(rLine.getCreatedBy());
			m_order.setDescription(rLine.getParent().getDescription());
			m_order.setDatePromised(rLine.getParent().getDateRequired());
			m_order.setDocAction(MOrder.DOCACTION_Complete);
			//	default po document type
			if (!p_ConsolidateDocument)
			{
				m_order.setDescription(Msg.getElement(getCtx(), "M_Requisition_ID") 
					+ ": " + rLine.getParent().getDocumentNo());
			}
			//	Prepare Save
			m_order.saveEx();
			// Put to cache
			m_cacheOrders.put(key, m_order);
		}
		m_M_Requisition_ID = rLine.getM_Requisition_ID();
	}	//	newOrder

	/**
	 * 	Close Order
	 * 	@throws Exception
	 */
	private void closeOrder() throws Exception
	{
		if (m_orderLine != null)
		{
			m_orderLine.saveEx();
		}
		if (m_order != null)
		{
			m_order.load(get_TrxName());
			addLog(0, null, m_order.getGrandTotal(), m_order.getDocumentNo());
			m_orders.add(m_order);
		}
		m_order = null;
		m_orderLine = null;
		m_bpartner = null;
	}	//	closeOrder

	
	/**
	 * 	New Order Line (different Product)
	 *	@param rLine request line
	 * 	@throws Exception
	 */
	private void newLine(MRequisitionLine rLine) throws Exception
	{
		if (m_orderLine != null)
		{
			m_orderLine.saveEx();
		}
		//	New Order - Different Vendor
		if (m_order == null 
			|| m_order.getC_BPartner_ID() != rLine.getC_BPartner_ID()
			)
		{
			newOrder(rLine, rLine.getC_BPartner_ID());
		}
		
		//	No Order Line
		m_orderLine = getOrderLine(rLine);
		m_orderLine.setDatePromised(rLine.getDateRequired());
		if (rLine.getM_Product_ID() != 0)
		{	
			MProduct product = MProduct.get(getCtx(), rLine.getM_Product_ID());		
			m_orderLine.setProduct(product);
			m_orderLine.setM_AttributeSetInstance_ID(rLine.getM_AttributeSetInstance_ID());
			if (rLine.getPriceActual().compareTo(Env.ZERO) == 0)
			{
				if (rLine.getC_BPartner_ID() != 0 && rLine.getC_BPartner().getPO_PriceList_ID() != 0)
					rLine.setPrice(rLine.getC_BPartner().getPO_PriceList_ID());
				else
					rLine.setPrice(rLine.getM_Requisition().getM_PriceList_ID());
				
			}
			m_orderLine.setPrice(rLine.getPriceActual());
			m_orderLine.setIsConsumesForecast(false);
				m_orderLine.setDescription(rLine.getDescription());
		}
		else
		{
			m_orderLine.setC_Charge_ID(rLine.getC_Charge_ID());
			m_orderLine.setPriceActual(rLine.getPriceActual());
			m_orderLine.set_ValueOfColumn("Comments", rLine.get_ValueAsString("Comments"));
			m_orderLine.set_ValueOfColumn("productNote", rLine.get_ValueAsString("productNote"));
		}
		if (rLine.get_ValueAsInt(MOrderLine.COLUMNNAME_User1_ID) != 0)
			m_orderLine.setUser1_ID(rLine.get_ValueAsInt(MOrderLine.COLUMNNAME_User1_ID));	
		if (rLine.get_ValueAsInt(MOrderLine.COLUMNNAME_User2_ID) != 0)	
			m_orderLine.setUser2_ID(rLine.get_ValueAsInt(MOrderLine.COLUMNNAME_User2_ID));
		if (rLine.get_ValueAsInt(MOrderLine.COLUMNNAME_C_Campaign_ID) != 0)	
			m_orderLine.setC_Campaign_ID(rLine.get_ValueAsInt(MOrderLine.COLUMNNAME_C_Campaign_ID));		
		if (rLine.get_ValueAsInt(MOrderLine.COLUMNNAME_C_Project_ID) != 0)	
			m_orderLine.setC_Project_ID(rLine.get_ValueAsInt(MOrderLine.COLUMNNAME_C_Project_ID));	
		
		if (rLine.get_ValueAsInt("C_SalesRegion_ID") != 0)	
			m_orderLine.set_ValueOfColumn("C_SalesRegion_ID", rLine.get_ValueAsInt("C_SalesRegion_ID"));		
		//	Prepare Save
		m_M_Product_ID = rLine.getM_Product_ID();
		m_M_AttributeSetInstance_ID = rLine.getM_AttributeSetInstance_ID();
		m_orderLine.saveEx();
	}	//	newLine
	
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
	

	private LinkedHashMap<Integer, LinkedHashMap<String, Object>> setColumnsValues() {
		if (m_values != null)
			return m_values;

		m_values = new LinkedHashMap<Integer, LinkedHashMap<String, Object>>();

		for (MRequisitionLine record : getMRPRecords()) {
			m_values.put(
					record.get_ID(),
					Browser.getBrowseValues(getAD_PInstance_ID(), null,
							record.get_ID(), null));
		}
		return m_values;
	}
	

	private MOrderLine getOrderLine(MRequisitionLine rline)
	{
		StringBuffer whereClause = new StringBuffer( "C_Order_ID =? and m_product_ID =? ");

	    ArrayList<Object> searchParameters = null;
		searchParameters = new ArrayList<Object>();
		searchParameters.add(m_order.getC_Order_ID());
		searchParameters.add(rline.getM_Product_ID());
		MOrderLine line = new Query(getCtx(), MOrderLine.Table_Name, whereClause.toString(), get_TrxName())
			.setParameters(searchParameters)
			.first();
		if (line == null)
			line = new MOrderLine(m_order);
		return line;
	}
	


	

	
	private List<MRequisitionLine> getMRPRecords() {
		if (m_records != null)
			return m_records;

		String whereClause = "EXISTS (SELECT T_Selection_ID FROM T_Selection WHERE  T_Selection.AD_PInstance_ID=? AND T_Selection.T_Selection_ID=m_requisitionline.m_requisitionline_ID)";
		m_records = new Query(getCtx(), I_M_RequisitionLine.Table_Name, whereClause,
				get_TrxName()).setClient_ID()
				.setParameters(getAD_PInstance_ID()).list();
		return m_records;
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

	/**
	 * Do we need to generate Purchase Orders for given Vendor 
	 * @param C_BPartner_ID
	 * @return true if it's allowed
	 */
	
}	//	RequisitionPOCreate
