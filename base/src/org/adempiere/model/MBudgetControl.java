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
package org.adempiere.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Properties;

import org.compiere.model.MCalendar;
import org.compiere.model.MFactAcct;
import org.compiere.model.MOrder;
import org.compiere.model.MPeriod;
import org.compiere.model.Query;
import org.compiere.model.X_GL_Budget;
import org.compiere.model.X_GL_BudgetControl;
import org.compiere.util.DB;


/**
 *	BudgetControl Model
 *	
 *  @author Susanne Calderon
 */
public class MBudgetControl extends X_GL_BudgetControl
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2110541427179611810L;

	/**
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param C_Year_ID id
	 *	@param trxName transaction
	 */
	public MBudgetControl (Properties ctx, int GL_BudgetControl_ID, String trxName)
	{
		super (ctx, GL_BudgetControl_ID, trxName);
	}	//	MBudgetControl

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trxName transaction
	 */
	public MBudgetControl (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MBudgetControl
	

	
	
	/*public BigDecimal getBudgetAmt(MOrder order, int user1_id, int user2_id)
	{
		StringBuffer whereClause = new StringBuffer (" GL_Budget_ID =? and user1_id =? and user2_id=? and dateacct ");
		//Zeitraum definieren
		if (getBudgetControlScope().equals(X_GL_BudgetControl.BUDGETCONTROLSCOPE_PeriodOnly))
				whereClause.append(getPeriodWhere(order));
		else if (getBudgetControlScope().equals(X_GL_BudgetControl.BUDGETCONTROLSCOPE_YearToDate))
			whereClause.append(getYearWhere(order));
		else 
			whereClause.append(" <= " + DB.TO_DATE(order.getDateAcct()));
		whereClause.append(" AND postingtype = 'B'");
		BigDecimal budgetAmt = new Query(order.getCtx(), MFactAcct.Table_Name, whereClause.toString(), order.get_TrxName())
			.setParameters(getGL_Budget().getGL_Budget_ID(), user1_id, user2_id)
			.aggregate("amtacctdr - amtacctcr", Query.AGGREGATE_SUM);
		return budgetAmt.negate();		
	}*/
	
	public BigDecimal getBudgetAmt(Timestamp dateAcct,int GL_Budget_ID, int user1_id, int user2_id, int account_ID, int c_campaign_ID, int c_activity_ID,
			int c_project_ID)
	{
		StringBuffer whereClause = new StringBuffer (" GL_Budget_ID =? and dateacct ");
		//Zeitraum definieren
		if (getBudgetControlScope().equals(X_GL_BudgetControl.BUDGETCONTROLSCOPE_PeriodOnly))
				whereClause.append(getPeriodWhere(dateAcct));
		else if (getBudgetControlScope().equals(X_GL_BudgetControl.BUDGETCONTROLSCOPE_YearToDate))
			whereClause.append(getYearWhere(dateAcct));
		else 
			whereClause.append(" <= " + DB.TO_DATE(dateAcct));
		whereClause.append(" AND postingtype = 'B'");
		ArrayList<Object> params = new ArrayList<Object>();
		params.add(GL_Budget_ID);

		if (account_ID >0)
		{
			whereClause.append(" and account_ID=? ");
			params.add(account_ID);			
		}

		if (c_campaign_ID >0)
		{
			whereClause.append(" and c_campaign_ID=? ");
			params.add(c_campaign_ID);			
		}
		if (c_activity_ID >0)
		{
			whereClause.append(" and c_activity_ID=? ");
			params.add(c_activity_ID);			
		}
		if (c_project_ID >0)
		{
			whereClause.append(" and c_project_ID=? ");
			params.add(c_project_ID);			
		}
		if (user1_id >0)
		{
			whereClause.append(" and user1_ID=? ");
			params.add(user1_id);			
		}
		if (user2_id >0)
		{
			whereClause.append(" and user2_ID=? ");
			params.add(user2_id);			
		}
		BigDecimal budgetAmt = new Query(getCtx(), MFactAcct.Table_Name, whereClause.toString(), get_TrxName())
			.setParameters(params)
			.aggregate("amtacctcr - amtacctdr", Query.AGGREGATE_SUM);
		return budgetAmt;		
	}
	

	private String getPeriodWhere (MOrder order)
	{
		MPeriod per = MPeriod.get(order.getCtx(), order.getDateAcct(), order.getAD_Org_ID());
		
		StringBuffer sql = new StringBuffer ("BETWEEN ");
		sql.append(DB.TO_DATE(per.getStartDate()))
			.append(" AND ")
			.append(DB.TO_DATE(order.getDateAcct()));
		return sql.toString();
	}	//	getPeriodWhere
	
	private String getPeriodWhere (Timestamp dateAcct)
	{
		MPeriod per = MPeriod.get(getCtx(), dateAcct, 0);
		
		StringBuffer sql = new StringBuffer ("BETWEEN ");
		sql.append(DB.TO_DATE(per.getStartDate()))
			.append(" AND ")
			.append(DB.TO_DATE(dateAcct));
		return sql.toString();
	}	//	getPeriodWhere

	/**
	 * 	Get Year Info
	 * 	@return BETWEEN start AND end
	 */
	private String getYearWhere (MOrder order)
	{
		MPeriod per = MPeriod.get(order.getCtx(), order.getDateAcct(), order.getAD_Org_ID());
		MPeriod perFirst = new Query(order.getCtx(), MPeriod.Table_Name, "c_year_ID =? and ad_org_ID in (0,?)", order.get_TrxName())
			.setParameters(per.getC_Year_ID(), order.getAD_Org_ID())
			.setOrderBy(MPeriod.COLUMNNAME_StartDate)
			.first();
		StringBuffer sql = new StringBuffer ("BETWEEN ");
		sql.append(DB.TO_DATE(perFirst.getStartDate()))
			  .append(" AND ")
			  .append(DB.TO_DATE(order.getDateAcct()));
		return sql.toString();
	}	//	getPeriodWhere
	

	private String getYearWhere (Timestamp dateAcct)
	{
		MPeriod per = MPeriod.get(getCtx(), dateAcct, 0);
		MPeriod perFirst = new Query(getCtx(), MPeriod.Table_Name, "c_year_ID =? and ad_org_ID in (0,?)", get_TrxName())
			.setParameters(per.getC_Year_ID(), 0)
			.setOrderBy(MPeriod.COLUMNNAME_StartDate)
			.first();
		StringBuffer sql = new StringBuffer ("BETWEEN ");
		sql.append(DB.TO_DATE(perFirst.getStartDate()))
			  .append(" AND ")
			  .append(DB.TO_DATE(dateAcct));
		return sql.toString();
	}	//	getPeriodWheredatea
	
	
	public BigDecimal getUsedAmt(MOrder order, int user1_id, int user2_id)
	{		
		StringBuffer whereClause = new StringBuffer (" GL_Budget_ID =? and user1_id =? and user2_id=? and dateacct ");
		//Zeitraum definieren
		if (getBudgetControlScope().equals(X_GL_BudgetControl.BUDGETCONTROLSCOPE_PeriodOnly))
				whereClause.append(getPeriodWhere(order));
		else if (getBudgetControlScope().equals(X_GL_BudgetControl.BUDGETCONTROLSCOPE_YearToDate))
			whereClause.append(getYearWhere(order));
		else 
			whereClause.append(" <= " + DB.TO_DATE(order.getDateAcct()));
		//Commitmenttype
		if (getCommitmentType().equals(X_GL_BudgetControl.COMMITMENTTYPE_POCommitmentOnly))
			whereClause.append(" and postingType in ('E','A')");
		if (getCommitmentType().equals(X_GL_BudgetControl.COMMITMENTTYPE_POCommitmentReservation))
				whereClause.append("  and postingType in ('E','A', 'R')");
		BigDecimal usedAmt = new Query(order.getCtx(), MFactAcct.Table_Name, whereClause.toString(), order.get_TrxName())
			.setParameters(getGL_Budget().getGL_Budget_ID(), user1_id, user2_id)
			.aggregate("amtacctdr - amtacctcr", Query.AGGREGATE_SUM);
		return usedAmt;		
	}
	
	
	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("MBudgetControl[");
		sb.append(get_ID()).append("-")
			.append ("]");
		return sb.toString ();
	}	//	toString
	
	public BigDecimal getUsedAmt(Timestamp dateAcct,int GL_Budget_ID, int user1_id, int user2_id, int account_ID, int c_campaign_ID, int c_activity_ID,
			int c_project_ID)
	{
		StringBuffer whereClause = new StringBuffer (" GL_Budget_ID =? and dateacct ");
		//Zeitraum definieren
		if (getBudgetControlScope().equals(X_GL_BudgetControl.BUDGETCONTROLSCOPE_PeriodOnly))
				whereClause.append(getPeriodWhere(dateAcct));
		else if (getBudgetControlScope().equals(X_GL_BudgetControl.BUDGETCONTROLSCOPE_YearToDate))
			whereClause.append(getYearWhere(dateAcct));
		else 
			whereClause.append(" <= " + DB.TO_DATE(dateAcct));
		ArrayList<Object> params = new ArrayList<Object>();
		params.add(GL_Budget_ID);
		if (getCommitmentType().equals(X_GL_BudgetControl.COMMITMENTTYPE_POCommitmentOnly))
			whereClause.append(" and postingType in ('E','A')");
		if (getCommitmentType().equals(X_GL_BudgetControl.COMMITMENTTYPE_POCommitmentReservation))
				whereClause.append("  and postingType in ('E','A', 'R')");

		if (account_ID >0)
		{
			whereClause.append(" and account_ID=? ");
			params.add(account_ID);			
		}

		if (c_campaign_ID >0)
		{
			whereClause.append(" and c_campaign_ID=? ");
			params.add(c_campaign_ID);			
		}
		if (c_activity_ID >0)
		{
			whereClause.append(" and c_activity_ID=? ");
			params.add(c_activity_ID);			
		}
		if (c_project_ID >0)
		{
			whereClause.append(" and c_project_ID=? ");
			params.add(c_project_ID);			
		}
		if (user1_id >0)
		{
			whereClause.append(" and user1_ID=? ");
			params.add(user1_id);			
		}
		if (user2_id >0)
		{
			whereClause.append(" and user2_ID=? ");
			params.add(user2_id);			
		}
		BigDecimal budgetAmt = new Query(getCtx(), MFactAcct.Table_Name, whereClause.toString(), get_TrxName())
			.setParameters(params)
			.aggregate("amtacctdr - amtacctcr", Query.AGGREGATE_SUM);
		return budgetAmt.negate();		
	}
	
	
	
	
}	//	MYear
