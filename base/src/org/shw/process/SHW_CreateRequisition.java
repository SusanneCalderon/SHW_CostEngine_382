/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
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
 * Copyright (C) 2003-2012 e-Evolution,SC. All Rights Reserved.               *
 * Contributor(s): victor.perez@e-evolution.com www.e-evolution.com   		  *
 *****************************************************************************/
package org.shw.process;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.compiere.apps.AEnv;
import org.compiere.apps.AWindow;
import org.compiere.model.MDocType;
import org.compiere.model.MProduct;
import org.compiere.model.MQuery;
import org.compiere.model.MRequisition;
import org.compiere.model.MRequisitionLine;
import org.compiere.model.MSession;
import org.compiere.model.MTable;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.Env;
import org.eevolution.grid.Browser;

/**
 * Scheduling Activities
 * 
 * @author victor.perez@e-evolution.com, www.e-evolution.com
 */

public class SHW_CreateRequisition extends SvrProcess {

	protected List<MProduct> m_records = null;
	protected LinkedHashMap<Integer, LinkedHashMap<String, Object>> m_values = null;
	protected int processRecords = 0;
	private MRequisition req = null;
	private int p_C_Doctype_ID = 0;
	private Timestamp p_daterequired = null;
	private int p_M_Warehouse_ID =0;
	private int p_M_PriceList_ID =0;
	private String alias = "";
	
	/**
	 * Prepare - e.g., get Parameters.
	 */
	protected void prepare() {
		ProcessInfoParameter[] parameters = getParameter();
		for (ProcessInfoParameter para : parameters) {
			String name = para.getParameterName();
			if (para.getParameter() == null)
				;
			else if (name.equals(MDocType.COLUMNNAME_C_DocType_ID))
				p_C_Doctype_ID = para.getParameterAsInt();
			else if (name.equals(MRequisition.COLUMNNAME_DateRequired))
				p_daterequired = para.getParameterAsTimestamp();
			else if (name.equals(MRequisition.COLUMNNAME_M_Warehouse_ID))
				p_M_Warehouse_ID = para.getParameterAsInt();
			else if (name.equals(MRequisition.COLUMNNAME_M_PriceList_ID))
				p_M_PriceList_ID = para.getParameterAsInt();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
		setColumnsValues();
	} // prepare

	/**
	 * Perform process.
	 * 
	 * @return Message (clear text)
	 * @throws Exception
	 *             if not successful
	 */
	protected String doIt() throws Exception {

		for (MProduct product : getRecords()) 
		{
			if (req == null)
			{
				req = new MRequisition(getCtx(), 0, get_TrxName());
				req.setAD_User_ID(Env.getAD_User_ID(getCtx()));
				req.setC_DocType_ID(p_C_Doctype_ID);
				req.setDateDoc(p_daterequired);
				req.setDateRequired(p_daterequired);
				req.setM_PriceList_ID(p_M_PriceList_ID);
				req.setM_Warehouse_ID(p_M_Warehouse_ID);
				req.setAD_Org_ID(req.getM_Warehouse().getAD_Org_ID());
				req.setPriorityRule("5");
				req.saveEx();
				}
			GenerateLine(req, product);
				
		}


		String whereClauseWindow = "m_requisition_ID = " + req.getM_Requisition_ID() ;
		MQuery query = new MQuery("");
		MTable table = new MTable(getCtx(), MRequisition.Table_ID, get_TrxName());
		query.addRestriction(whereClauseWindow);
		query.setRecordCount(1);
		int AD_WindowNo = table.getAD_Window_ID();
		int ad_session_ID  = Env.getContextAsInt(getCtx(), "AD_Session_ID");
		MSession session = new MSession(getCtx(), ad_session_ID, null);
		
		if (session.getWebSession() == null ||session.getWebSession().length() == 0)
		{
			commitEx();
			zoom (AD_WindowNo, query);
			return "";
			
		}
		return req.getDocumentInfo();
	} // doIt
	/*
	private void saveBrowseValues(PO po, String alias) {

		LinkedHashMap<String, Object> values = m_values.get(po.get_ID());

		for (Entry<String, Object> entry : values.entrySet()) {
			String columnName = entry.getKey();
			if (columnName.contains(alias.toUpperCase() + "_")) {
				columnName = columnName.substring(columnName.indexOf("_") + 1);
				if(entry.getValue() != null)
					po.set_ValueOfColumn(columnName, entry.getValue());
			}
		}

	}
*/
	private List<MProduct> getRecords() {
		if (m_records != null)
			return m_records;

		String whereClause = "EXISTS (SELECT T_Selection_ID FROM T_Selection WHERE  T_Selection.AD_PInstance_ID=? AND T_Selection.T_Selection_ID=M_Product.M_Product_ID)";
		m_records = new Query(getCtx(), MProduct.Table_Name, whereClause,
				get_TrxName()).setClient_ID()
				.setParameters(getAD_PInstance_ID()).list();
		return m_records;
	}

	private LinkedHashMap<Integer, LinkedHashMap<String, Object>> setColumnsValues() {
		if (m_values != null)
			return m_values;

		m_values = new LinkedHashMap<Integer, LinkedHashMap<String, Object>>();

		for (MProduct record : getRecords()) {
			m_values.put(
					record.get_ID(),
					Browser.getBrowseValues(getAD_PInstance_ID(), null,
							record.get_ID(), null));
		}
		return m_values;
	}
	private void GenerateLine(MRequisition req, MProduct product)
	{
		MRequisitionLine rLine = new MRequisitionLine(req);

		LinkedHashMap<String, Object> values = m_values.get(product.get_ID());
		for (Entry<String, Object> entry : values.entrySet()) {
			String columnName = entry.getKey();
			if (columnName.contains(alias.toUpperCase() + "_")) {
				columnName = columnName.substring(columnName.indexOf("_") + 1);
				rLine.set_ValueOfColumn(columnName, entry.getValue());
				if (columnName.equals("orderqty"))
					rLine.set_ValueOfColumn(MRequisitionLine.COLUMNNAME_Qty, entry.getValue());
				if (columnName.equals(MRequisitionLine.COLUMNNAME_C_BPartner_ID))
				{
					BigDecimal ID = (BigDecimal)entry.getValue();
					if (ID.intValue()==0)
						rLine.set_ValueOfColumn(columnName, null);
				}
			}
		}
		rLine.saveEx();
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
} // Import Inventory Move
