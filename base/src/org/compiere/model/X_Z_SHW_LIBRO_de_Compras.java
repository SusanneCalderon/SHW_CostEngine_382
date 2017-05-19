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
package org.compiere.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;

/** Generated Model for Z_SHW_LIBRO_de_Compras
 *  @author Adempiere (generated) 
 *  @version Release 3.5.4a - $Id$ */
public class X_Z_SHW_LIBRO_de_Compras extends PO implements I_Z_SHW_LIBRO_de_Compras, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20100207L;

    /** Standard Constructor */
    public X_Z_SHW_LIBRO_de_Compras (Properties ctx, int Z_SHW_LIBRO_de_Compras_ID, String trxName)
    {
      super (ctx, Z_SHW_LIBRO_de_Compras_ID, trxName);
      /** if (Z_SHW_LIBRO_de_Compras_ID == 0)
        {
        } */
    }

    /** Load Constructor */
    public X_Z_SHW_LIBRO_de_Compras (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_Z_SHW_LIBRO_de_Compras[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set Col_0.
		@param Col_0 Col_0	  */
	public void setCol_0 (BigDecimal Col_0)
	{
		set_Value (COLUMNNAME_Col_0, Col_0);
	}

	/** Get Col_0.
		@return Col_0	  */
	public BigDecimal getCol_0 () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Col_0);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Col_1.
		@param Col_1 Col_1	  */
	public void setCol_1 (BigDecimal Col_1)
	{
		set_Value (COLUMNNAME_Col_1, Col_1);
	}

	/** Get Col_1.
		@return Col_1	  */
	public BigDecimal getCol_1 () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Col_1);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Col_2.
		@param Col_2 Col_2	  */
	public void setCol_2 (BigDecimal Col_2)
	{
		set_Value (COLUMNNAME_Col_2, Col_2);
	}

	/** Get Col_2.
		@return Col_2	  */
	public BigDecimal getCol_2 () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Col_2);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Col_3.
		@param Col_3 Col_3	  */
	public void setCol_3 (BigDecimal Col_3)
	{
		set_Value (COLUMNNAME_Col_3, Col_3);
	}

	/** Get Col_3.
		@return Col_3	  */
	public BigDecimal getCol_3 () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Col_3);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Col_4.
		@param Col_4 Col_4	  */
	public void setCol_4 (BigDecimal Col_4)
	{
		set_Value (COLUMNNAME_Col_4, Col_4);
	}

	/** Get Col_4.
		@return Col_4	  */
	public BigDecimal getCol_4 () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Col_4);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Col_5.
		@param Col_5 Col_5	  */
	public void setCol_5 (BigDecimal Col_5)
	{
		set_Value (COLUMNNAME_Col_5, Col_5);
	}

	/** Get Col_5.
		@return Col_5	  */
	public BigDecimal getCol_5 () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Col_5);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Col_6.
		@param Col_6 Col_6	  */
	public void setCol_6 (BigDecimal Col_6)
	{
		set_Value (COLUMNNAME_Col_6, Col_6);
	}

	/** Get Col_6.
		@return Col_6	  */
	public BigDecimal getCol_6 () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Col_6);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	public I_C_Period getC_Period() throws RuntimeException
    {
		return (I_C_Period)MTable.get(getCtx(), I_C_Period.Table_Name)
			.getPO(getC_Period_ID(), get_TrxName());	}

	/** Set Period.
		@param C_Period_ID 
		Period of the Calendar
	  */
	public void setC_Period_ID (int C_Period_ID)
	{
		if (C_Period_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_C_Period_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_C_Period_ID, Integer.valueOf(C_Period_ID));
	}

	/** Get Period.
		@return Period of the Calendar
	  */
	public int getC_Period_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Period_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Date Invoiced.
		@param DateInvoiced 
		Date printed on Invoice
	  */
	public void setDateInvoiced (Timestamp DateInvoiced)
	{
		set_ValueNoCheck (COLUMNNAME_DateInvoiced, DateInvoiced);
	}

	/** Get Date Invoiced.
		@return Date printed on Invoice
	  */
	public Timestamp getDateInvoiced () 
	{
		return (Timestamp)get_Value(COLUMNNAME_DateInvoiced);
	}

	/** Set Document No.
		@param DocumentNo 
		Document sequence number of the document
	  */
	public void setDocumentNo (String DocumentNo)
	{
		set_ValueNoCheck (COLUMNNAME_DocumentNo, DocumentNo);
	}

	/** Get Document No.
		@return Document sequence number of the document
	  */
	public String getDocumentNo () 
	{
		return (String)get_Value(COLUMNNAME_DocumentNo);
	}

	/** Set D-U-N-S.
		@param DUNS 
		Dun & Bradstreet Number
	  */
	public void setDUNS (String DUNS)
	{
		set_ValueNoCheck (COLUMNNAME_DUNS, DUNS);
	}

	/** Get D-U-N-S.
		@return Dun & Bradstreet Number
	  */
	public String getDUNS () 
	{
		return (String)get_Value(COLUMNNAME_DUNS);
	}

	/** Set EXTCOMPRAS.
		@param EXTCOMPRAS EXTCOMPRAS	  */
	public void setEXTCOMPRAS (BigDecimal EXTCOMPRAS)
	{
		set_ValueNoCheck (COLUMNNAME_EXTCOMPRAS, EXTCOMPRAS);
	}

	/** Get EXTCOMPRAS.
		@return EXTCOMPRAS	  */
	public BigDecimal getEXTCOMPRAS () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_EXTCOMPRAS);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Grand Total.
		@param GrandTotal 
		Total amount of document
	  */
	public void setGrandTotal (BigDecimal GrandTotal)
	{
		set_ValueNoCheck (COLUMNNAME_GrandTotal, GrandTotal);
	}

	/** Get Grand Total.
		@return Total amount of document
	  */
	public BigDecimal getGrandTotal () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_GrandTotal);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set IMPCA.
		@param IMPCA IMPCA	  */
	public void setIMPCA (BigDecimal IMPCA)
	{
		set_ValueNoCheck (COLUMNNAME_IMPCA, IMPCA);
	}

	/** Get IMPCA.
		@return IMPCA	  */
	public BigDecimal getIMPCA () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_IMPCA);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set IMPEXT.
		@param IMPEXT IMPEXT	  */
	public void setIMPEXT (BigDecimal IMPEXT)
	{
		set_ValueNoCheck (COLUMNNAME_IMPEXT, IMPEXT);
	}

	/** Get IMPEXT.
		@return IMPEXT	  */
	public BigDecimal getIMPEXT () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_IMPEXT);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set IMPFUERA.
		@param IMPFUERA IMPFUERA	  */
	public void setIMPFUERA (BigDecimal IMPFUERA)
	{
		set_ValueNoCheck (COLUMNNAME_IMPFUERA, IMPFUERA);
	}

	/** Get IMPFUERA.
		@return IMPFUERA	  */
	public BigDecimal getIMPFUERA () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_IMPFUERA);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set IVACOMPRAS.
		@param IVACOMPRAS IVACOMPRAS	  */
	public void setIVACOMPRAS (BigDecimal IVACOMPRAS)
	{
		set_ValueNoCheck (COLUMNNAME_IVACOMPRAS, IVACOMPRAS);
	}

	/** Get IVACOMPRAS.
		@return IVACOMPRAS	  */
	public BigDecimal getIVACOMPRAS () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_IVACOMPRAS);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set IVAIMP.
		@param IVAIMP IVAIMP	  */
	public void setIVAIMP (BigDecimal IVAIMP)
	{
		set_ValueNoCheck (COLUMNNAME_IVAIMP, IVAIMP);
	}

	/** Get IVAIMP.
		@return IVAIMP	  */
	public BigDecimal getIVAIMP () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_IVAIMP);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Name.
		@param Name 
		Alphanumeric identifier of the entity
	  */
	public void setName (String Name)
	{
		set_ValueNoCheck (COLUMNNAME_Name, Name);
	}

	/** Get Name.
		@return Alphanumeric identifier of the entity
	  */
	public String getName () 
	{
		return (String)get_Value(COLUMNNAME_Name);
	}

    /** Get Record ID/ColumnName
        @return ID/ColumnName pair
      */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), getName());
    }

	/** Set NIT.
		@param NIT NIT	  */
	public void setNIT (String NIT)
	{
		set_Value (COLUMNNAME_NIT, NIT);
	}

	/** Get NIT.
		@return NIT	  */
	public String getNIT () 
	{
		return (String)get_Value(COLUMNNAME_NIT);
	}

	/** Set TAXIVA.
		@param TAXIVA TAXIVA	  */
	public void setTAXIVA (BigDecimal TAXIVA)
	{
		set_ValueNoCheck (COLUMNNAME_TAXIVA, TAXIVA);
	}

	/** Get TAXIVA.
		@return TAXIVA	  */
	public BigDecimal getTAXIVA () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_TAXIVA);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set TAXRET.
		@param TAXRET TAXRET	  */
	public void setTAXRET (BigDecimal TAXRET)
	{
		set_ValueNoCheck (COLUMNNAME_TAXRET, TAXRET);
	}

	/** Get TAXRET.
		@return TAXRET	  */
	public BigDecimal getTAXRET () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_TAXRET);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Z_SHW_NoCorr.
		@param Z_SHW_NoCorr Z_SHW_NoCorr	  */
	public void setZ_SHW_NoCorr (int Z_SHW_NoCorr)
	{
		set_ValueNoCheck (COLUMNNAME_Z_SHW_NoCorr, Integer.valueOf(Z_SHW_NoCorr));
	}

	/** Get Z_SHW_NoCorr.
		@return Z_SHW_NoCorr	  */
	public int getZ_SHW_NoCorr () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Z_SHW_NoCorr);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
}