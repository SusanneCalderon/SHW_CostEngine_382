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
import java.util.Properties;
import java.util.logging.Level;
import org.compiere.model.X_Z_SHW_LIBRO_de_Compras;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.AdempiereSystemError;
import org.compiere.util.DB;

/**
 * 	Create Tax Declaration
 *	
 *  @author Jorg Janke
 *  @version $Id: TaxDeclarationCreate.java,v 1.2 2006/07/30 00:51:01 jjanke Exp $
 */
public class SHW_LibroComprasBorrar extends SvrProcess
{
	/**	Tax Declaration			*/
	private int 				p_C_period_ID = 0;
	private int 				p_z_shw_nocorr = 0;
	private int				p_ID;
	

	
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
			else if (name.equals("C_Period_ID"))
				p_C_period_ID = para[i].getParameterAsInt();
			else if (name.equals("Z_SHW_NoCorr"))
					p_z_shw_nocorr = para[i].getParameterAsInt();
			
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
			p_ID = getRecord_ID();
		}
		
	}	//	prepare

	
	/**
	 * 	Process
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt () throws Exception
	{
		//	Get Invoices
		String sql = "delete from z_shw_libro_de_compras where z_shw_nocorr = ? " +
		" and c_period_id = ?";
		Object[] params = new Object[]{p_z_shw_nocorr, p_C_period_ID};
		int no = DB.executeUpdate(sql, params, false, get_TrxName());
		
		commitEx();
		int Startnummer = p_z_shw_nocorr - 1;		
		String sql_nummerieren = " select * from z_shw_libro_de_compras " 
		+ "WHERE Z_SHW_NoCorr > ? and c_period_id = ? order by dateinvoiced ";
		Durchnummerieren(Startnummer, sql_nummerieren, getCtx(), 
				get_TrxName(), p_z_shw_nocorr, p_C_period_ID);		
		return "Juhuu" + no; 
	}	//	doIt
	
	public static void Durchnummerieren(int startnummer, 
			String sql, Properties ctx, String trxName, int nocorr, int c_period_id)
	{
		int no = startnummer;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement (sql, trxName);
			pstmt.setInt(1, nocorr);
			pstmt.setInt(2, c_period_id);
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
