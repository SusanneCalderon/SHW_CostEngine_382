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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;

import org.compiere.model.MCommission;
import org.compiere.model.MCommissionAmt;
import org.compiere.model.MCommissionDetail;
import org.compiere.model.MCommissionLine;
import org.compiere.model.MCommissionRun;
import org.compiere.model.MCurrency;
import org.compiere.model.MUser;import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;

import org.compiere.util.AdempiereSystemError;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Language;

/**
 *	Commission Calculation	
 *	
 *  @author Jorg Janke
 *  @version $Id: CommissionCalc.java,v 1.3 2006/09/25 00:59:41 jjanke Exp $
 */
public class SHW_CommissionCalc extends SvrProcess
{
	private Timestamp		p_StartDate;
	//
	private Timestamp		p_EndDate;
	private MCommission		m_com;
	private Boolean			p_includeVoid = false;
	private String 			m_docstatus = "('CO', 'CL')";
	private Boolean			p_reset = false;
	//

	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
        ProcessInfoParameter[] parameters = getParameter();
		for (ProcessInfoParameter parameter : parameters) {
            String name = parameter.getParameterName();
            if (parameter.getParameter() == null)
                ;
            if (name.equals("StartDate"))
            {
            	p_StartDate = parameter.getParameterAsTimestamp();
            	p_EndDate   = parameter.getParameterToAsTimestamp();
            }
            if (name.equals("IncludeVoid"))
            	p_includeVoid = parameter.getParameterAsBoolean();
            
			/*String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("StartDate"))
				p_StartDate = (Timestamp)para[i].getParameter();*/
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}	//	prepare

	/**
	 *  Perform process.
	 *  @return Message (text with variables)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception
	{
		if (p_includeVoid)
			m_docstatus = "('CO', 'CL', 'VO', 'RE')";
		log.info("C_Commission_ID=" + getRecord_ID() + ", StartDate=" + p_StartDate);
		if (p_StartDate == null)
			p_StartDate = new Timestamp (System.currentTimeMillis());
		m_com = new MCommission (getCtx(), getRecord_ID(), get_TrxName());
		if (m_com.get_ID() == 0)
			throw new AdempiereUserError ("No Commission");
			
		//	Create Commission
		String whereClause = "C_Commission_ID=? and StartDate=?";
		ArrayList< Object> params = new ArrayList<>();
		params.add(m_com.getC_Commission_ID());
		params.add(p_StartDate);
		MCommissionRun comRun = new Query(getCtx(), MCommissionRun.Table_Name, whereClause, get_TrxName())
			.setParameters(params)
			.first();
		if (comRun == null)
			comRun = new MCommissionRun (m_com);
		
		if (p_EndDate == null)
			setStartEndDate();
		
		comRun.setStartDate(p_StartDate);	
		if (p_reset && comRun.getC_CommissionRun_ID() !=0)
		{
			List<MCommissionAmt> camts = new Query(getCtx(), MCommissionAmt.Table_Name, "c_commissionrun_ID=?", get_TrxName())
				.setParameters(comRun.getC_CommissionRun_ID())
				.list();
			for(MCommissionAmt camt:camts)
				camt.deleteEx(true);
		}	
		//	01-Jan-2000 - 31-Jan-2001 - USD
		SimpleDateFormat format = DisplayType.getDateFormat(DisplayType.Date);
		String description = format.format(p_StartDate) 
			+ " - " + format.format(p_EndDate)
			+ " - " + MCurrency.getISO_Code(getCtx(), m_com.getC_Currency_ID());
		comRun.setDescription(description);
		if (!comRun.save())
			throw new AdempiereSystemError ("Could not save Commission Run");
		
		MCommissionLine[] lines = m_com.getLines();
		for (int i = 0; i < lines.length; i++)
		{
			//	Amt for Line - Updated By Trigger
			MCommissionAmt comAmt = new MCommissionAmt (comRun, lines[i].getC_CommissionLine_ID());
			if (!comAmt.save())
				throw new AdempiereSystemError ("Could not save Commission Amt");
			//
			StringBuffer sql = new StringBuffer();
			if (MCommission.DOCBASISTYPE_Receipt.equals(m_com.getDocBasisType()))
			{
				if (m_com.isListDetails())
				{
					sql.append("SELECT h.C_Currency_ID, (l.LineNetAmt*al.Amount/h.GrandTotal) AS Amt,"
						+ " (l.QtyInvoiced*al.Amount/h.GrandTotal) AS Qty,"
						+ " NULL, l.C_InvoiceLine_ID, p.DocumentNo||'_'||h.DocumentNo,"
						+ " COALESCE(prd.Value,l.Description), h.DateInvoiced "
						+ "FROM C_Payment p"
						+ " INNER JOIN C_AllocationLine al ON (p.C_Payment_ID=al.C_Payment_ID)"
						+ " INNER JOIN C_Invoice h ON (al.C_Invoice_ID = h.C_Invoice_ID)"
						+ " INNER JOIN C_InvoiceLine l ON (h.C_Invoice_ID = l.C_Invoice_ID) "
						+ " left JOIN c_orderline ol on l.c_orderline_ID = ol.c_orderline_ID "
						+ " left join c_order o on ol.c_order_ID = o.c_order_ID "
						+ " LEFT OUTER JOIN M_Product prd ON (l.M_Product_ID = prd.M_Product_ID) "
						+ " WHERE p.DocStatus IN ('CO','CL')" 
						+ " AND h.IsSOTrx='Y'"
						+ " AND p.AD_Client_ID = ?"
						+ " AND p.DateTrx BETWEEN ? AND ?");
				}
				else
				{
					sql.append("SELECT h.C_Currency_ID, SUM(l.LineNetAmt*al.Amount/h.GrandTotal) AS Amt,"
						+ " SUM(l.QtyInvoiced*al.Amount/h.GrandTotal) AS Qty,"
						+ " NULL, NULL, NULL, NULL, MAX(h.DateInvoiced) "
						+ "FROM C_Payment p"
						+ " INNER JOIN C_AllocationLine al ON (p.C_Payment_ID=al.C_Payment_ID)"
						+ " INNER JOIN C_Invoice h ON (al.C_Invoice_ID = h.C_Invoice_ID)"
						+ " INNER JOIN C_InvoiceLine l ON (h.C_Invoice_ID = l.C_Invoice_ID) "
						+ "WHERE p.DocStatus IN ('CL','CO','RE')"
						+ " AND h.IsSOTrx='Y'"
						+ " AND p.AD_Client_ID = ?"
						+ " AND p.DateTrx BETWEEN ? AND ?");
				}
			}
			else if (MCommission.DOCBASISTYPE_Order.equals(m_com.getDocBasisType()))
			{
				if (m_com.isListDetails())
				{
					sql.append("SELECT h.C_Currency_ID, l.LineNetAmt, l.QtyOrdered, "
						+ "l.C_OrderLine_ID, NULL, h.DocumentNo,"
						+ " COALESCE(prd.Value,l.Description),h.DateOrdered "
						+ "FROM C_Order h"
						+ " INNER JOIN C_OrderLine l ON (h.C_Order_ID = l.C_Order_ID)"
						+ " LEFT OUTER JOIN M_Product prd ON (l.M_Product_ID = prd.M_Product_ID) "
						+ "WHERE h.DocStatus IN ('CL','CO')"
						+ " AND h.IsSOTrx='Y'"
						+ " AND h.AD_Client_ID = ?"
						+ " AND h.DateOrdered BETWEEN ? AND ?");
				}
				else
				{
					sql.append("SELECT h.C_Currency_ID, SUM(l.LineNetAmt) AS Amt,"
						+ " SUM(l.QtyOrdered) AS Qty, "
						+ "NULL, NULL, NULL, NULL, MAX(h.DateOrdered) "
						+ "FROM C_Order h"
						+ " INNER JOIN C_OrderLine l ON (h.C_Order_ID = l.C_Order_ID) "
						+ "WHERE h.DocStatus IN ('CL','CO')"
						+ " AND h.IsSOTrx='Y'"
						+ " AND h.AD_Client_ID = ?"
						+ " AND h.DateOrdered BETWEEN ? AND ?");
				}
			}
			else 	//	Invoice Basis
			{
				if (m_com.isListDetails())
				{
					sql.append("SELECT h.C_Currency_ID, l.LineNetAmt, l.QtyInvoiced, "
						+ "NULL, l.C_InvoiceLine_ID, h.DocumentNo,"
						+ " COALESCE(prd.Value,l.Description),h.DateInvoiced "
						+ "FROM C_Invoice h"
						+ " INNER JOIN rv_c_invoiceline l ON (h.C_Invoice_ID = l.C_Invoice_ID)"
						+ " LEFT OUTER JOIN M_Product prd ON (l.M_Product_ID = prd.M_Product_ID) "
						//+ " left JOIN c_orderline ol on l.c_orderline_ID = ol.c_orderline_ID "
						//+ " left join c_order o on ol.c_order_ID = o.c_order_ID "
						+ " WHERE h.DocStatus IN  " + m_docstatus
						+ " AND h.IsSOTrx='Y'"
						+ " AND h.AD_Client_ID = ?"
						+ " AND h.DateInvoiced BETWEEN ? AND ?");
				}
				else
				{
					sql.append("SELECT h.C_Currency_ID, SUM(l.LineNetAmt) AS Amt,"
						+ " SUM(l.QtyInvoiced) AS Qty, "
						+ "NULL, NULL, NULL, NULL, MAX(h.DateInvoiced) "
						+ "FROM C_Invoice h"
						+ " INNER JOIN C_InvoiceLine l ON (h.C_Invoice_ID = l.C_Invoice_ID) "
						+ " left JOIN c_orderline ol on l.c_orderline_ID = ol.c_orderline_ID "
						+ " left join c_order o on ol.c_order_ID = o.c_order_ID "
						+ "WHERE h.DocStatus IN " + m_docstatus
						+ " AND h.IsSOTrx='Y'"
						+ " AND h.AD_Client_ID = ?"
						+ " AND h.DateInvoiced BETWEEN ? AND ?");
				}
			}
			//	CommissionOrders/Invoices
			if (lines[i].isCommissionOrders())
			{
				MUser[] users = MUser.getOfBPartner(getCtx(), m_com.getC_BPartner_ID(), get_TrxName());
				if (users == null || users.length == 0)
					throw new AdempiereUserError ("Commission Business Partner has no Users/Contact");
				if (users.length == 1)
				{
					int SalesRep_ID = users[0].getAD_User_ID();
					sql.append(" AND coalesce(l.salesrep_ID, h.salesrep_ID) =").append(SalesRep_ID);
				}
				else
				{
					log.warning("Not 1 User/Contact for C_BPartner_ID=" 
						+ m_com.getC_BPartner_ID() + " but " + users.length);
					sql.append(" AND h.SalesRep_ID IN (SELECT AD_User_ID FROM AD_User WHERE C_BPartner_ID=")
						.append(m_com.getC_BPartner_ID()).append(")");
				}
			}
			//	Organization
			if (lines[i].getOrg_ID() != 0)
				sql.append(" AND h.AD_Org_ID=").append(lines[i].getOrg_ID());
			//	BPartner
			if (lines[i].getC_BPartner_ID() != 0)
				sql.append(" AND h.C_BPartner_ID=").append(lines[i].getC_BPartner_ID());
			//	BPartner Group
			if (lines[i].getC_BP_Group_ID() != 0)
				sql.append(" AND h.C_BPartner_ID IN "
					+ "(SELECT C_BPartner_ID FROM C_BPartner WHERE C_BP_Group_ID=").append(lines[i].getC_BP_Group_ID()).append(")");
			//	Sales Region
			if (lines[i].getC_SalesRegion_ID() != 0)
				sql.append(" AND h.C_BPartner_Location_ID IN "
					+ "(SELECT C_BPartner_Location_ID FROM C_BPartner_Location WHERE C_SalesRegion_ID=").append(lines[i].getC_SalesRegion_ID()).append(")");
			//	Product
			if (lines[i].getM_Product_ID() != 0)
				sql.append(" AND l.M_Product_ID=").append(lines[i].getM_Product_ID());
			//	Product Category
			if (lines[i].getM_Product_Category_ID() != 0)
				sql.append(" AND l.M_Product_ID IN "
					+ "(SELECT M_Product_ID FROM M_Product WHERE M_Product_Category_ID=").append(lines[i].getM_Product_Category_ID()).append(")");
			//	Payment Rule
			if (lines[i].getPaymentRule() != null)
				sql.append(" AND h.PaymentRule='").append(lines[i].getPaymentRule()).append("'");
			//	Grouping
			if (!m_com.isListDetails())
				sql.append(" GROUP BY h.C_Currency_ID");
			//
			log.fine("Line=" + lines[i].getLine() + " - " + sql);
			//
			sql.append(" and c_invoiceline_ID not in (select c_invoiceline_ID from c_commissiondetail cd" );
			sql.append(" inner join C_CommissionAmt ca on cd.C_CommissionAmt_ID = ca.C_CommissionAmt_ID ");
			sql.append(" inner join c_commissionrun cr on ca.c_commissionrun_ID = cr.c_commissionrun_ID ");
			sql.append(" where cr.c_commission_ID = " + m_com.getC_Commission_ID());
			sql.append(" and cr.startdate = " + DB.TO_DATE(p_StartDate) + ")" );
							
			createDetail(sql.toString(), comAmt);
			comAmt.calculateCommission(); 
			comAmt.saveEx();
		}	//	for all commission lines
		
	//	comRun.updateFromAmt();
	//	comRun.saveEx();
		
		//	Save Last Run
		m_com.setDateLastRun (p_StartDate);
		m_com.saveEx();
		
		return "@C_CommissionRun_ID@ = " + comRun.getDocumentNo() 
			+ " - " + comRun.getDescription();
	}	//	doIt

	/**
	 * 	Set Start and End Date
	 */
	private void setStartEndDate()
	{
		GregorianCalendar cal = new GregorianCalendar(Language.getLoginLanguage().getLocale());
		cal.setTimeInMillis(p_StartDate.getTime());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		//	Yearly
		if (MCommission.FREQUENCYTYPE_Yearly.equals(m_com.getFrequencyType()))
		{
			cal.set(Calendar.DAY_OF_YEAR, 1);
			p_StartDate = new Timestamp (cal.getTimeInMillis());
			//
			cal.add(Calendar.YEAR, 1);
			cal.add(Calendar.DAY_OF_YEAR, -1); 
			p_EndDate = new Timestamp (cal.getTimeInMillis());
			
		}
		//	Quarterly
		else if (MCommission.FREQUENCYTYPE_Quarterly.equals(m_com.getFrequencyType()))
		{
			cal.set(Calendar.DAY_OF_MONTH, 1);
			int month = cal.get(Calendar.MONTH);
			if (month < Calendar.APRIL)
				cal.set(Calendar.MONTH, Calendar.JANUARY);
			else if (month < Calendar.JULY)
				cal.set(Calendar.MONTH, Calendar.APRIL);
			else if (month < Calendar.OCTOBER)
				cal.set(Calendar.MONTH, Calendar.JULY);
			else
				cal.set(Calendar.MONTH, Calendar.OCTOBER);
			p_StartDate = new Timestamp (cal.getTimeInMillis());
			//
			cal.add(Calendar.MONTH, 3);
			cal.add(Calendar.DAY_OF_YEAR, -1); 
			p_EndDate = new Timestamp (cal.getTimeInMillis());
		}
		//	Weekly
		else if (MCommission.FREQUENCYTYPE_Weekly.equals(m_com.getFrequencyType()))
		{
			cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			p_StartDate = new Timestamp (cal.getTimeInMillis());
			//
			cal.add(Calendar.DAY_OF_YEAR, 7); 
			p_EndDate = new Timestamp (cal.getTimeInMillis());
		}
		//	Monthly
		else if (MCommission.FREQUENCYTYPE_Monthly.equals(m_com.getFrequencyType()))
		{
			cal.set(Calendar.DAY_OF_MONTH, 1);
			p_StartDate = new Timestamp (cal.getTimeInMillis());
			//
			cal.add(Calendar.MONTH, 1);
			cal.add(Calendar.DAY_OF_YEAR, -1); 
			p_EndDate = new Timestamp (cal.getTimeInMillis());
		}
		
		else 
		{
			if (cal.get(Calendar.DAY_OF_MONTH) <= 15)
			{
				cal.set(Calendar.DAY_OF_MONTH, 1);
				p_StartDate = new Timestamp (cal.getTimeInMillis()); 
				cal.add(Calendar.DAY_OF_YEAR, 15); 
				cal.add(Calendar.MONTH, 1);
				cal.add(Calendar.DAY_OF_YEAR, -1); 
				p_EndDate = new Timestamp (cal.getTimeInMillis());
			}
			else
			{
				cal.set(Calendar.DAY_OF_MONTH, 15);
				p_StartDate = new Timestamp (cal.getTimeInMillis()); 
				cal.add(Calendar.DAY_OF_YEAR, 15); 
				p_EndDate = new Timestamp (cal.getTimeInMillis()); 
			}
		}
		log.fine("setStartEndDate = " + p_StartDate + " - " + p_EndDate);
		
		/**
		String sd = DB.TO_DATE(p_StartDate, true);
		StringBuffer sql = new StringBuffer ("SELECT ");
		if (MCommission.FREQUENCYTYPE_Quarterly.equals(m_com.getFrequencyType()))
			sql.append("TRUNC(").append(sd).append(", 'Q'), TRUNC(").append(sd).append("+92, 'Q')-1");
		else if (MCommission.FREQUENCYTYPE_Weekly.equals(m_com.getFrequencyType()))
			sql.append("TRUNC(").append(sd).append(", 'DAY'), TRUNC(").append(sd).append("+7, 'DAY')-1");
		else	//	Month
			sql.append("TRUNC(").append(sd).append(", 'MM'), TRUNC(").append(sd).append("+31, 'MM')-1");
		sql.append(" FROM DUAL");
		**/
	}	//	setStartEndDate

	/**
	 * 	Create Commission Detail
	 *	@param sql sql statement
	 *	@param comAmt parent
	 * @throws Exception 
	 */
	private void createDetail (String sql, MCommissionAmt comAmt) throws Exception
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, get_TrxName());
			pstmt.setInt(1, m_com.getAD_Client_ID());
			pstmt.setTimestamp(2, p_StartDate);
			pstmt.setTimestamp(3, p_EndDate);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				//	CommissionAmount, C_Currency_ID, Amt, Qty,
				MCommissionDetail cd = new MCommissionDetail (comAmt,
					rs.getInt(1), rs.getBigDecimal(2), rs.getBigDecimal(3));
					
				//	C_OrderLine_ID, C_InvoiceLine_ID,
				cd.setLineIDs(rs.getInt(4), rs.getInt(5));
				
				//	Reference, Info,
				String s = rs.getString(6);
				if (s != null)
					cd.setReference(s);
				s = rs.getString(7);
				if (s != null)
					cd.setInfo(s);
				
				//	Date
				Timestamp date = rs.getTimestamp(8);
				cd.setConvertedAmt(date);
				
				//
				if (!cd.save())
					throw new IllegalArgumentException ("CommissionCalc - Detail Not saved");
			}
			pstmt = null;
		}
		catch (Exception e)
		{
			throw new AdempiereSystemError("System Error: " + e.getLocalizedMessage(), e);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
	}	//	createDetail

}	//	CommissionCalc
