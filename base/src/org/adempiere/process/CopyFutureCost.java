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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.compiere.model.MCost;
import org.compiere.model.MProduct;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;

/**
 *  Copy Project Details
 *
 *	@author Jorg Janke
 *	@version $Id: CopyFromProject.java,v 1.2 2006/07/30 00:51:01 jjanke Exp $
 */
public class CopyFutureCost extends SvrProcess
{
	private int p_M_Product_category_ID 		= 0;
	private int p_M_Product_ID 					= 0;
	private int p_M_CostType_ID					= 0;
	private int p_M_CostElement_ID 				= 0;
	private int p_C_AcctSchema_ID 				= 0;

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
			if (name.equals(MProduct.COLUMNNAME_M_Product_Category_ID))
				p_M_Product_category_ID = para.getParameterAsInt();
			else if (name.equals(MProduct.COLUMNNAME_M_Product_ID))
				p_M_Product_ID = para.getParameterAsInt();
			else if (name.equals(MCost.COLUMNNAME_C_AcctSchema_ID))
				p_C_AcctSchema_ID = para.getParameterAsInt();
			else if (name.equals(MCost.COLUMNNAME_M_CostElement_ID))
				p_M_CostElement_ID = para.getParameterAsInt();
			else if (name.equals(MCost.COLUMNNAME_M_CostType_ID))
				p_M_CostType_ID = para.getParameterAsInt();
				
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
		for (MCost cost:getCosts())
		{
			cost.setFutureCostPrice(cost.getCurrentCostPrice());
			cost.setFutureCostPriceLL(cost.getCurrentCostPriceLL());
			cost.saveEx();
		}
		return "";
	}	//	doIt

	
	private MCost[] getCosts()
	{
		ArrayList<Object> finalParams = new ArrayList<Object>();
		StringBuffer finalWhereClause = new StringBuffer();

		if (p_M_Product_ID != 0)
		{
			finalWhereClause.append("M_Product_ID =?");
			finalParams.add(p_M_Product_ID);
		}
		else if (p_M_Product_category_ID !=0 )
		{
			finalWhereClause.append(" M_Product_ID in (select M_product_ID from m_product where m_product_category_ID=?)");
			finalParams.add(p_M_Product_category_ID);
		}
		if (p_M_CostType_ID != 0)
		{
			finalWhereClause.append(" AND " + MCost.COLUMNNAME_M_CostType_ID +  "=?");
			finalParams.add(p_M_CostType_ID);
		}

		if (p_M_CostElement_ID != 0)
		{
			finalWhereClause.append(" AND " + MCost.COLUMNNAME_M_CostElement_ID +  "=?");
			finalParams.add(p_M_CostElement_ID);
		}
		if (p_C_AcctSchema_ID != 0)
		{
			finalWhereClause.append(" AND " + MCost.COLUMNNAME_C_AcctSchema_ID +  "=?");
			finalParams.add(p_C_AcctSchema_ID);
		}
		List<MCost> list = new Query(getCtx(), MCost.Table_Name, finalWhereClause.toString(), get_TrxName())
			.setClient_ID()
			.setOnlyActiveRecords(true)
			.setParameters(finalParams)
			.list();
		return list.toArray(new MCost[list.size()]);
	}
}	//	CopyFromProject
