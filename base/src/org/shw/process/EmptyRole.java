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
 *                                                                            *
 * Copyright (C) 2005 Robert KLEIN. robeklein@gmail.com                       *
 * Contributor(s): ______________________________________.                    *
 *****************************************************************************/
package org.shw.process;

import org.compiere.model.I_AD_Role_Included;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;


/**
 *	Copy role access records
 *	
 *  @author Robert Klein
 *  @ author Paul Bowden
 *  @version $Id: CopyRole.java,v 1.0$
 *  
 */

public class EmptyRole extends SvrProcess
{
	private int m_AD_Role_ID_From = 0;
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("AD_Role_ID"))
				m_AD_Role_ID_From = para[i].getParameterAsInt();
		}		
	}	//	prepare

	/**
	 * 	Copy the role access records
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt() throws Exception
	{	
		String[] tables = new String[] {"AD_Window_Access", "AD_Process_Access", "AD_Form_Access",
				"AD_Workflow_Access", "AD_Task_Access","AD_Browse_Access",
				I_AD_Role_Included.Table_Name,
		};
		
		
		for ( int i = 0; i < tables.length; i++ )
		{
			String table = tables[i];
			
			String sql = "Update " + table + " set isactive = 'N'  WHERE AD_Role_ID = " + m_AD_Role_ID_From;
			int no = DB.executeUpdateEx(sql, get_TrxName());
			
			
		}
	
		return "Role empied";
	}	//	doIt
			}	//	CopyRole
