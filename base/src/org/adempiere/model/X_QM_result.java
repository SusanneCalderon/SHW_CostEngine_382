/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2007 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
/** Generated Model - DO NOT CHANGE */
package org.adempiere.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;
import org.compiere.model.*;
import org.compiere.util.Env;

/** Generated Model for QM_result
 *  @author Adempiere (generated) 
 *  @version Release 3.8.0RC - $Id$ */
public class X_QM_result extends PO implements I_QM_result, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20140917L;

    /** Standard Constructor */
    public X_QM_result (Properties ctx, int QM_result_ID, String trxName)
    {
      super (ctx, QM_result_ID, trxName);
      /** if (QM_result_ID == 0)
        {
			setDateConfirm (new Timestamp( System.currentTimeMillis() ));
			setIsApproved (false);
// N
			setOperation (null);
			setqm_result_ID (0);
        } */
    }

    /** Load Constructor */
    public X_QM_result (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 3 - Client - Org 
      */
    protected int get_AccessLevel()
    {
      return accessLevel.intValue();
    }

    /** Load Meta Data */
    protected POInfo initPO (Properties ctx)
    {
      POInfo poi = POInfo.getPOInfo (ctx, Table_ID, get_TrxName());
      return poi;
    }

    public String toString()
    {
      StringBuffer sb = new StringBuffer ("X_QM_result[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public org.compiere.model.I_C_ProjectLine getC_ProjectLine() throws RuntimeException
    {
		return (org.compiere.model.I_C_ProjectLine)MTable.get(getCtx(), org.compiere.model.I_C_ProjectLine.Table_Name)
			.getPO(getC_ProjectLine_ID(), get_TrxName());	}

	/** Set Project Line.
		@param C_ProjectLine_ID 
		Task or step in a project
	  */
	public void setC_ProjectLine_ID (int C_ProjectLine_ID)
	{
		if (C_ProjectLine_ID < 1) 
			set_Value (COLUMNNAME_C_ProjectLine_ID, null);
		else 
			set_Value (COLUMNNAME_C_ProjectLine_ID, Integer.valueOf(C_ProjectLine_ID));
	}

	/** Get Project Line.
		@return Task or step in a project
	  */
	public int getC_ProjectLine_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_ProjectLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Date Confirm.
		@param DateConfirm 
		Date Confirm of this Order
	  */
	public void setDateConfirm (Timestamp DateConfirm)
	{
		set_Value (COLUMNNAME_DateConfirm, DateConfirm);
	}

	/** Get Date Confirm.
		@return Date Confirm of this Order
	  */
	public Timestamp getDateConfirm () 
	{
		return (Timestamp)get_Value(COLUMNNAME_DateConfirm);
	}

	/** Set Description.
		@param Description 
		Optional short description of the record
	  */
	public void setDescription (String Description)
	{
		set_Value (COLUMNNAME_Description, Description);
	}

	/** Get Description.
		@return Optional short description of the record
	  */
	public String getDescription () 
	{
		return (String)get_Value(COLUMNNAME_Description);
	}

	/** Set Approved.
		@param IsApproved 
		Indicates if this document requires approval
	  */
	public void setIsApproved (boolean IsApproved)
	{
		set_Value (COLUMNNAME_IsApproved, Boolean.valueOf(IsApproved));
	}

	/** Get Approved.
		@return Indicates if this document requires approval
	  */
	public boolean isApproved () 
	{
		Object oo = get_Value(COLUMNNAME_IsApproved);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Operation AD_Reference_ID=1000017 */
	public static final int OPERATION_AD_Reference_ID=1000017;
	/** < = << */
	public static final String OPERATION_Le = "<<";
	/** <= = <= */
	public static final String OPERATION_LeEq = "<=";
	/** = = == */
	public static final String OPERATION_Eq = "==";
	/** >= = >= */
	public static final String OPERATION_GtEq = ">=";
	/** > = >> */
	public static final String OPERATION_Gt = ">>";
	/** != = != */
	public static final String OPERATION_NotEq = "!=";
	/** between = BW */
	public static final String OPERATION_Between = "BW";
	/** Fulfill = FF */
	public static final String OPERATION_Fulfill = "FF";
	/** Not Fulfill = NF */
	public static final String OPERATION_NotFulfill = "NF";
	/** Set Operation.
		@param Operation 
		Compare Operation
	  */
	public void setOperation (String Operation)
	{

		set_Value (COLUMNNAME_Operation, Operation);
	}

	/** Get Operation.
		@return Compare Operation
	  */
	public String getOperation () 
	{
		return (String)get_Value(COLUMNNAME_Operation);
	}

	public org.adempiere.model.I_PP_Order_QualityControl getPP_Order_QualityControl() throws RuntimeException
    {
		return (org.adempiere.model.I_PP_Order_QualityControl)MTable.get(getCtx(), org.adempiere.model.I_PP_Order_QualityControl.Table_Name)
			.getPO(getPP_Order_QualityControl_ID(), get_TrxName());	}

	/** Set PP_Order_QualityControl ID.
		@param PP_Order_QualityControl_ID PP_Order_QualityControl ID	  */
	public void setPP_Order_QualityControl_ID (int PP_Order_QualityControl_ID)
	{
		if (PP_Order_QualityControl_ID < 1) 
			set_Value (COLUMNNAME_PP_Order_QualityControl_ID, null);
		else 
			set_Value (COLUMNNAME_PP_Order_QualityControl_ID, Integer.valueOf(PP_Order_QualityControl_ID));
	}

	/** Get PP_Order_QualityControl ID.
		@return PP_Order_QualityControl ID	  */
	public int getPP_Order_QualityControl_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_PP_Order_QualityControl_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set qm_result ID.
		@param qm_result_ID qm_result ID	  */
	public void setqm_result_ID (int qm_result_ID)
	{
		if (qm_result_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_qm_result_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_qm_result_ID, Integer.valueOf(qm_result_ID));
	}

	/** Get qm_result ID.
		@return qm_result ID	  */
	public int getqm_result_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_qm_result_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.eevolution.model.I_QM_SpecificationLine getQM_SpecificationLine() throws RuntimeException
    {
		return (org.eevolution.model.I_QM_SpecificationLine)MTable.get(getCtx(), org.eevolution.model.I_QM_SpecificationLine.Table_Name)
			.getPO(getQM_SpecificationLine_ID(), get_TrxName());	}

	/** Set QM Specification Line.
		@param QM_SpecificationLine_ID QM Specification Line	  */
	public void setQM_SpecificationLine_ID (int QM_SpecificationLine_ID)
	{
		if (QM_SpecificationLine_ID < 1) 
			set_Value (COLUMNNAME_QM_SpecificationLine_ID, null);
		else 
			set_Value (COLUMNNAME_QM_SpecificationLine_ID, Integer.valueOf(QM_SpecificationLine_ID));
	}

	/** Get QM Specification Line.
		@return QM Specification Line	  */
	public int getQM_SpecificationLine_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_QM_SpecificationLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Result.
		@param Result 
		Result of the action taken
	  */
	public void setResult (BigDecimal Result)
	{
		set_Value (COLUMNNAME_Result, Result);
	}

	/** Get Result.
		@return Result of the action taken
	  */
	public BigDecimal getResult () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Result);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Sequence.
		@param SeqNo 
		Method of ordering records; lowest number comes first
	  */
	public void setSeqNo (int SeqNo)
	{
		set_Value (COLUMNNAME_SeqNo, Integer.valueOf(SeqNo));
	}

	/** Get Sequence.
		@return Method of ordering records; lowest number comes first
	  */
	public int getSeqNo () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_SeqNo);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set ValueFrom.
		@param ValueFrom ValueFrom	  */
	public void setValueFrom (BigDecimal ValueFrom)
	{
		set_Value (COLUMNNAME_ValueFrom, ValueFrom);
	}

	/** Get ValueFrom.
		@return ValueFrom	  */
	public BigDecimal getValueFrom () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_ValueFrom);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set ValueTo.
		@param ValueTo ValueTo	  */
	public void setValueTo (BigDecimal ValueTo)
	{
		set_Value (COLUMNNAME_ValueTo, ValueTo);
	}

	/** Get ValueTo.
		@return ValueTo	  */
	public BigDecimal getValueTo () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_ValueTo);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}
}