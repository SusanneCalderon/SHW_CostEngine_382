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
 * Contributor(s): Victor Perez www.e-evolution.com                           *
 *****************************************************************************/
package org.shw.process;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.compiere.model.I_S_Resource;
import org.compiere.model.MPInstance;
import org.compiere.model.MPInstancePara;
import org.compiere.model.MProcess;
import org.compiere.model.MSequence;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.Env;
import org.compiere.util.Trx;
import org.eevolution.grid.Browser;
import org.eevolution.model.I_PP_MRP;
import org.eevolution.model.I_PP_Order;
import org.eevolution.model.MPPMRP;
import org.eevolution.model.MPPOrder;

/**
 * 
 * MRP Schedule
 * 
 * @author victor.perez@e-evolution.com , www.e-evolution.com
 */
public class SHW_ChangeSequenceNO extends SvrProcess {

	/**
	 * Prepare - e.g., get Parameters.
	 */
	protected Integer p_s_resource_ID =0;
	

	protected void prepare() {
		for (ProcessInfoParameter para : getParameter()) {
			String name = para.getParameterName();
			if (para.getParameter() == null)
				;
		}

	} // prepare

	/**
	 * Perform process.
	 * 
	 * @return Message (clear text)
	 * @throws Exception
	 *             if not successful
	 */
	protected String doIt() throws Exception {

		LinkedHashMap<Integer, LinkedHashMap<String, Object>> m_values = null;
		List<MSequence> m_records = null;m_values = new LinkedHashMap<Integer, LinkedHashMap<String, Object>>();

		String whereClause = "EXISTS (SELECT T_Selection_ID FROM T_Selection WHERE  T_Selection.AD_PInstance_ID=? " + 
		" AND T_Selection.T_Selection_ID=ad_sequence_ID)";
		m_records = new Query(getCtx(), MSequence.Table_Name, whereClause,
				get_TrxName()).setClient_ID()
				.setParameters(getAD_PInstance_ID())
				.setOrderBy(MSequence.COLUMNNAME_AD_Sequence_ID)
				.list();
		for (MSequence record : m_records) {
			m_values.put(
					record.get_ID(),
					Browser.getBrowseValues(getAD_PInstance_ID(), null,
							record.get_ID(), null));
		}
		for (MSequence seq : m_records) {

			LinkedHashMap<String, Object> values = m_values.get(seq.get_ID());

			for (Entry<String, Object> entry : values.entrySet()) {
				String columnName = entry.getKey();
				if (columnName.contains("SEQ".toUpperCase() + "_")) {
					columnName = columnName.substring(columnName.indexOf("_") + 1);
					seq.set_ValueOfColumn(columnName, entry.getValue());
				}
			}
			seq.saveEx();
		}
		

		return "";
	} // doIt

	




	
}
