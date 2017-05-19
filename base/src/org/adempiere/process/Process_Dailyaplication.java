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
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;

import org.compiere.model.I_AD_Client;
import org.compiere.model.I_Fact_Acct;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;

/**
 *  Copy Project Details
 *
 *	@author Jorg Janke
 *	@version $Id: CopyFromProject.java,v 1.2 2006/07/30 00:51:01 jjanke Exp $
 */
public class Process_Dailyaplication extends SvrProcess
{
	Integer 		p_Letzte_Partida = 0;
	Integer			p_AD_Client_ID = 0;
	Timestamp		p_dateacct = null;
	Timestamp 		p_dateacctTo = null;

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
			if (name.equals("Letzte_Partida"))
				p_Letzte_Partida = para.getParameterAsInt();
			else if (name.equals(I_AD_Client.COLUMNNAME_AD_Client_ID))
				p_AD_Client_ID = para.getParameterAsInt();
			else if (name.equals(I_Fact_Acct.COLUMNNAME_DateAcct))
			{
				p_dateacct = para.getParameterAsTimestamp();
				p_dateacctTo = para.getParameterToAsTimestamp();
			}
				
			else
				log.log(Level.SEVERE, "Unknown Parameter:   " + name);
		}
	}	//	prepare

	/**
	 *  Perform process.
	 *  @return Message (clear text)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception
	{	
		Timestamp dateacctTimestamp = p_dateacct;
		Calendar c = Calendar.getInstance();
		
		while (dateacctTimestamp.compareTo(p_dateacctTo) != 0)
		{
			DB.executeUpdateEx(createInsert(dateacctTimestamp), get_TrxName());
			p_Letzte_Partida = DB.getSQLValue(get_TrxName(), "select max(to_number(DailyAccountingNo,'9999999')) from T_DailyAccounting where ad_pinstance_ID =" + getAD_PInstance_ID());
			c.setTime(dateacctTimestamp);
			c.add(Calendar.DATE, 1);
			Date  dateacct = c.getTime();
			dateacctTimestamp = new java.sql.Timestamp(dateacct.getTime());
			commitEx();
		}
		return "";
	}	//	doIt

	
	private String createInsert(Timestamp datacctactual)
	{
		String sql = 
				" SELECT " + getAD_PInstance_ID() + " as ad_pinstance_ID, " +
				" f.description," +
				" z_shw_erzeuge_gl_correlativo_2(  " + p_Letzte_Partida + ", "+ p_AD_Client_ID + ",   " + DB.TO_DATE(datacctactual) + ",  GL_CATEGORY_ID) as CORR,  " +
				" null as documentno, 0 as detail,  " +
				" f.amtacctdr, f.amtacctcr, " +
				" f.GL_CATEGORY_ID, account_ID,dateacct  , c_bpartner_ID, m_product_ID , 0,0,0, 0" +
				" FROM shw_fact_acct_Cummulated f  " +
				" WHERE f.AD_CLIENT_ID = "+ p_AD_Client_ID + "  " +
				" and dateacct = " + DB.TO_DATE(datacctactual) +
				" UNION  " +
				" SELECT " + getAD_PInstance_ID() + " as ad_pinstance_ID, f.description," +
				" z_shw_erzeuge_gl_correlativo_2(  " + p_Letzte_Partida + ", "+ p_AD_Client_ID + ",   " + DB.TO_DATE(datacctactual) + ",  GL_CATEGORY_ID) as CORR,  " +
				" shw_fact_acct_documentno_parent(record_ID, line_ID, ad_table_ID) as documentno, 1 as detail,  " +
				" f.amtacctdr, f.amtacctcr,  " +
				" f.GL_CATEGORY_ID, account_ID,dateacct  , c_bpartner_ID, m_product_ID, " +
				"  shw_fact_acct_recordID_parent(record_ID, line_ID, ad_table_ID) as parent_ID, f.record_ID as record_ID, line_ID as line_ID, f.ad_table_ID" +
				" FROM shw_fact_acct_notCummulated f  " +
				" WHERE f.AD_CLIENT_ID = "+ p_AD_Client_ID + "  " +
				" and dateacct = " + DB.TO_DATE(datacctactual);
		String sqlInsert = " Insert into T_DailyAccounting (ad_pinstance_ID,description, DailyAccountingNo, documentno, detail, AMTACCTDR,   " +
				"AMTACCTCR, GL_CATEGORY_ID, account_ID,dateacct , c_bpartner_ID, m_product_ID,parent_ID, RECORD_id , line_ID, ad_table_ID)  " + sql;
		return sqlInsert;
	}
}	//	CopyFromProject
