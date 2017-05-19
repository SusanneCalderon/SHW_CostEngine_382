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
package org.compiere.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import org.compiere.util.KeyNamePair;

/** Generated Interface for Z_SHW_LIBRO_de_Compras
 *  @author Adempiere (generated) 
 *  @version Release 3.5.4a
 */
public interface I_Z_SHW_LIBRO_de_Compras 
{

    /** TableName=Z_SHW_LIBRO_de_Compras */
    public static final String Table_Name = "Z_SHW_LIBRO_de_Compras";

    /** AD_Table_ID=1200000 */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 3 - Client - Org 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(3);

    /** Load Meta Data */

    /** Column name AD_Client_ID */
    public static final String COLUMNNAME_AD_Client_ID = "AD_Client_ID";

	/** Get Client.
	  * Client/Tenant for this installation.
	  */
	public int getAD_Client_ID();

    /** Column name AD_Org_ID */
    public static final String COLUMNNAME_AD_Org_ID = "AD_Org_ID";

	/** Set Organization.
	  * Organizational entity within client
	  */
	public void setAD_Org_ID (int AD_Org_ID);

	/** Get Organization.
	  * Organizational entity within client
	  */
	public int getAD_Org_ID();

    /** Column name Col_0 */
    public static final String COLUMNNAME_Col_0 = "Col_0";

	/** Set Col_0	  */
	public void setCol_0 (BigDecimal Col_0);

	/** Get Col_0	  */
	public BigDecimal getCol_0();

    /** Column name Col_1 */
    public static final String COLUMNNAME_Col_1 = "Col_1";

	/** Set Col_1	  */
	public void setCol_1 (BigDecimal Col_1);

	/** Get Col_1	  */
	public BigDecimal getCol_1();

    /** Column name Col_2 */
    public static final String COLUMNNAME_Col_2 = "Col_2";

	/** Set Col_2	  */
	public void setCol_2 (BigDecimal Col_2);

	/** Get Col_2	  */
	public BigDecimal getCol_2();

    /** Column name Col_3 */
    public static final String COLUMNNAME_Col_3 = "Col_3";

	/** Set Col_3	  */
	public void setCol_3 (BigDecimal Col_3);

	/** Get Col_3	  */
	public BigDecimal getCol_3();

    /** Column name Col_4 */
    public static final String COLUMNNAME_Col_4 = "Col_4";

	/** Set Col_4	  */
	public void setCol_4 (BigDecimal Col_4);

	/** Get Col_4	  */
	public BigDecimal getCol_4();

    /** Column name Col_5 */
    public static final String COLUMNNAME_Col_5 = "Col_5";

	/** Set Col_5	  */
	public void setCol_5 (BigDecimal Col_5);

	/** Get Col_5	  */
	public BigDecimal getCol_5();

    /** Column name Col_6 */
    public static final String COLUMNNAME_Col_6 = "Col_6";

	/** Set Col_6	  */
	public void setCol_6 (BigDecimal Col_6);

	/** Get Col_6	  */
	public BigDecimal getCol_6();

    /** Column name C_Period_ID */
    public static final String COLUMNNAME_C_Period_ID = "C_Period_ID";

	/** Set Period.
	  * Period of the Calendar
	  */
	public void setC_Period_ID (int C_Period_ID);

	/** Get Period.
	  * Period of the Calendar
	  */
	public int getC_Period_ID();

	public I_C_Period getC_Period() throws RuntimeException;

    /** Column name DateInvoiced */
    public static final String COLUMNNAME_DateInvoiced = "DateInvoiced";

	/** Set Date Invoiced.
	  * Date printed on Invoice
	  */
	public void setDateInvoiced (Timestamp DateInvoiced);

	/** Get Date Invoiced.
	  * Date printed on Invoice
	  */
	public Timestamp getDateInvoiced();

    /** Column name DocumentNo */
    public static final String COLUMNNAME_DocumentNo = "DocumentNo";

	/** Set Document No.
	  * Document sequence number of the document
	  */
	public void setDocumentNo (String DocumentNo);

	/** Get Document No.
	  * Document sequence number of the document
	  */
	public String getDocumentNo();

    /** Column name DUNS */
    public static final String COLUMNNAME_DUNS = "DUNS";

	/** Set D-U-N-S.
	  * Dun & Bradstreet Number
	  */
	public void setDUNS (String DUNS);

	/** Get D-U-N-S.
	  * Dun & Bradstreet Number
	  */
	public String getDUNS();

    /** Column name EXTCOMPRAS */
    public static final String COLUMNNAME_EXTCOMPRAS = "EXTCOMPRAS";

	/** Set EXTCOMPRAS	  */
	public void setEXTCOMPRAS (BigDecimal EXTCOMPRAS);

	/** Get EXTCOMPRAS	  */
	public BigDecimal getEXTCOMPRAS();

    /** Column name GrandTotal */
    public static final String COLUMNNAME_GrandTotal = "GrandTotal";

	/** Set Grand Total.
	  * Total amount of document
	  */
	public void setGrandTotal (BigDecimal GrandTotal);

	/** Get Grand Total.
	  * Total amount of document
	  */
	public BigDecimal getGrandTotal();

    /** Column name IMPCA */
    public static final String COLUMNNAME_IMPCA = "IMPCA";

	/** Set IMPCA	  */
	public void setIMPCA (BigDecimal IMPCA);

	/** Get IMPCA	  */
	public BigDecimal getIMPCA();

    /** Column name IMPEXT */
    public static final String COLUMNNAME_IMPEXT = "IMPEXT";

	/** Set IMPEXT	  */
	public void setIMPEXT (BigDecimal IMPEXT);

	/** Get IMPEXT	  */
	public BigDecimal getIMPEXT();

    /** Column name IMPFUERA */
    public static final String COLUMNNAME_IMPFUERA = "IMPFUERA";

	/** Set IMPFUERA	  */
	public void setIMPFUERA (BigDecimal IMPFUERA);

	/** Get IMPFUERA	  */
	public BigDecimal getIMPFUERA();

    /** Column name IVACOMPRAS */
    public static final String COLUMNNAME_IVACOMPRAS = "IVACOMPRAS";

	/** Set IVACOMPRAS	  */
	public void setIVACOMPRAS (BigDecimal IVACOMPRAS);

	/** Get IVACOMPRAS	  */
	public BigDecimal getIVACOMPRAS();

    /** Column name IVAIMP */
    public static final String COLUMNNAME_IVAIMP = "IVAIMP";

	/** Set IVAIMP	  */
	public void setIVAIMP (BigDecimal IVAIMP);

	/** Get IVAIMP	  */
	public BigDecimal getIVAIMP();

    /** Column name Name */
    public static final String COLUMNNAME_Name = "Name";

	/** Set Name.
	  * Alphanumeric identifier of the entity
	  */
	public void setName (String Name);

	/** Get Name.
	  * Alphanumeric identifier of the entity
	  */
	public String getName();

    /** Column name NIT */
    public static final String COLUMNNAME_NIT = "NIT";

	/** Set NIT	  */
	public void setNIT (String NIT);

	/** Get NIT	  */
	public String getNIT();

    /** Column name TAXIVA */
    public static final String COLUMNNAME_TAXIVA = "TAXIVA";

	/** Set TAXIVA	  */
	public void setTAXIVA (BigDecimal TAXIVA);

	/** Get TAXIVA	  */
	public BigDecimal getTAXIVA();

    /** Column name TAXRET */
    public static final String COLUMNNAME_TAXRET = "TAXRET";

	/** Set TAXRET	  */
	public void setTAXRET (BigDecimal TAXRET);

	/** Get TAXRET	  */
	public BigDecimal getTAXRET();

    /** Column name Z_SHW_NoCorr */
    public static final String COLUMNNAME_Z_SHW_NoCorr = "Z_SHW_NoCorr";

	/** Set Z_SHW_NoCorr	  */
	public void setZ_SHW_NoCorr (int Z_SHW_NoCorr);

	/** Get Z_SHW_NoCorr	  */
	public int getZ_SHW_NoCorr();
}
