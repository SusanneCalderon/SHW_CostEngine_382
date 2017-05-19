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
package org.compiere.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.logging.Level;
import org.compiere.model.X_Z_SHW_LIBRO_de_Compras;
import org.compiere.util.AdempiereSystemError;
import org.compiere.util.DB;

/**
 * 	Create Tax Declaration
 *	
 *  @author Jorg Janke
 *  @version $Id: TaxDeclarationCreate.java,v 1.2 2006/07/30 00:51:01 jjanke Exp $
 */
public class SHW_LibroComprasCreate extends SvrProcess
{
	/**	Tax Declaration			*/
	private int 				p_C_TaxDeclaration_ID = 0;
	/** Delete Old Lines		*/
	private boolean				p_DeleteOld = true;
	/** TDLines					*/
	private int					m_noLines = 0;
	/** TDAccts					*/
	private int					m_noAccts = 0;
	

	private Timestamp			p_DateInvoiced_From = null;
	private Timestamp			p_DateInvoiced_To = null;
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare ()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("DeleteOld"))
				p_DeleteOld = "Y".equals(para[i].getParameter());
			else if (name.equals("DateInvoiced"))
			{
				p_DateInvoiced_From = (Timestamp)para[i].getParameter();
				p_DateInvoiced_To = (Timestamp)para[i].getParameter_To();
			}
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
		p_C_TaxDeclaration_ID = getRecord_ID();
	}	//	prepare

	
	/**
	 * 	Process
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt () throws Exception
	{
		//	Get Invoices
		String sql = "Insert into z_shw_libro_de_compras (" +
		/*" created, " +
	    " createdby, " +
		" updated, " +
	    " updatedby, " +*/
		"AD_Client_ID,"+
		"AD_Org_ID,"+
		"C_Period_ID,"+
		"DUNS,"+
		"DateInvoiced,"+
		"DocumentNo,"+
		"EXTCOMPRAS,"+
		"GrandTotal,"+
		"IMPCA,"+
		"IMPEXT,"+
		"IMPFUERA,"+
		"IVACOMPRAS,"+
		"IVAIMP,"+
		"NIT,"+
		"Name,"+
		"TAXIVA,"+
		"TAXRET,"+
		"nosujcompras," +
		"c_invoice_id, COL_1)"+			
			"SELECT AD_Client_ID, " +  //" created, createdby, updated, updatedby
			"AD_Org_ID,"+
			"C_Period_ID,"+
			"DUNS,"+
			"DateInvoiced,"+
			"DocumentNo,"+
			"EXTCOMPRAS,"+
			"GrandTotal,"+
			"IMPCA,"+
			"IMPEXT,"+
			"IMPFUERA,"+
			"IVACOMPRAS,"+
			"IVAIMP,"+
			"NIT,"+
			"Name,"+
			"TAXIVA,"+
			"TAXRET,"+
			"taxnosujeto," +
			"c_invoice_id , ContribucionesEspeciales "+
			"FROM z_shw_librodecompras i "
			+ "WHERE TRUNC(DateInvoiced) >= ? AND TRUNC(DateInvoiced) <= ? "
			+ " AND NOT EXISTS (SELECT * FROM z_shw_libro_de_compras lc "
				+ "WHERE i.C_Invoice_ID=lc.C_Invoice_ID)";
		int no = 0;
		try
		{
			Object[] params = new Object[]{p_DateInvoiced_From, p_DateInvoiced_To};
			no = DB.executeUpdate(sql, params, false, get_TrxName());
					; 
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql.toString(), e);
		}		
		commitEx();
		String sql_letzteNummer = "select max(Z_SHW_NoCorr) from z_shw_libro_de_compras where Z_SHW_NoCorr is not null";
		String sql_nummerieren = " select * from z_shw_libro_de_compras " 
		+ "WHERE Z_SHW_NoCorr is null and dateinvoiced between firstof(?, 'YY') and ? order by dateinvoiced ";
		int Startnummer = DB.getSQLValue(get_TrxName(), sql_letzteNummer);
		
		Durchnummerieren(Startnummer, sql_nummerieren, getCtx(), 
				get_TrxName(), p_DateInvoiced_From, p_DateInvoiced_To);		
		return "Facturas ingresadas: " +  no; 
	}	//	doIt
	
	public static void Durchnummerieren(int startnummer, 
			String sql, Properties ctx, String trxName, Timestamp dateinvoicedfrom, Timestamp dateinvoicedTo)
	{
		int no = startnummer;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement (sql, trxName);
			pstmt.setTimestamp(1, dateinvoicedfrom);
			pstmt.setTimestamp(2, dateinvoicedTo);
			rs = pstmt.executeQuery ();
			while (rs.next ())
			{
				no ++;
				X_Z_SHW_LIBRO_de_Compras lbc = new X_Z_SHW_LIBRO_de_Compras(ctx, rs, trxName);
				lbc.setZ_SHW_NoCorr(no);
				lbc.saveEx();
			}
		}
		catch (Exception e)
		{
			
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
	}
	
	/**
	 * 	Create Data
	 *	@param invoice invoice
	 */
	
	
}	//	TaxDeclarationCreate
