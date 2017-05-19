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

import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;

import org.compiere.model.MInvoice;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;

/**
 *  Copy Project Details
 *
 *	@author Jorg Janke
 *	@version $Id: CopyFromProject.java,v 1.2 2006/07/30 00:51:01 jjanke Exp $
 */
public class Process_Dailyaplication_New extends SvrProcess
{
	Integer 		p_Letzte_Partida = 0;
	Integer			p_AD_Client_ID = 0;
	Timestamp 		p_dateacct = null;
	Timestamp 		p_dateacctTo = null;

	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] parameters = getParameter();
		for (ProcessInfoParameter para : parameters) {
			String name = para.getParameterName();
			if (name.equals("DateAcctTo"))
			{
				p_dateacctTo = para.getParameterAsTimestamp();
			}
				
			else
				log.log(Level.SEVERE, "Unknown Parameter:   " + name);
		}
		p_AD_Client_ID = Env.getAD_Client_ID(getCtx());
	}	//	prepare

	/**
	 *  Perform process.
	 *  @return Message (clear text)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception
	{
		java.util.Properties A_Ctx = getCtx();
		String A_TrxName = getTableName();
		List<MInvoice> invoices = new Query(A_Ctx, MInvoice.Table_Name, "issotrx = 'N' and documentno_record is null", A_TrxName)
			.setOnlyActiveRecords(true)
			.setClient_ID()
			.list();
		return "";
	}	//	doIt

	
	private String createSelect(Timestamp datacctactual)
	{
		String sql = 
				" SELECT  f.description," 
				+" shw_fact_acct_documentno_parent(record_ID, line_ID, ad_table_ID) as documentno, 1 as detail,  " +
				" f.amtacctdr, f.amtacctcr,  " +
				" f.GL_CATEGORY_ID, account_ID,dateacct  , c_bpartner_ID, m_product_ID, " +
				" shw_fact_acct_recordID_parent(record_ID, line_ID, ad_table_ID) as parent_ID, " +
				" f.record_ID as record_ID, line_ID as line_ID, f.ad_table_ID" +
				" FROM SHW_dailyAccounting f  " +
				" WHERE f.AD_CLIENT_ID = "+ p_AD_Client_ID + "  " +
				" and dateacct = " + DB.TO_DATE(datacctactual) +
				 " Order by gl_category_ID";
		return sql;
	}
}	//	CopyFromProject
