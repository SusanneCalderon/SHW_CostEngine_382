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

import java.util.List;

import org.compiere.model.MInOutLine;
import org.compiere.model.MMovement;
import org.compiere.model.MMovementLine;
import org.compiere.model.Query;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;






/**
 *  Creates Payment from c_invoice, including Aging
 *
 *  @author Susanne Calderon
 */

public class Create_MovementLineFromCostDistribution  extends SvrProcess

{	

    @Override    
    protected void prepare()
    {  
    	

    }
    
      
    @Override
    protected String doIt() throws Exception
    {
    	MMovement move = new MMovement(getCtx(), getRecord_ID(), get_TrxName());
    	int shw_costDistribution_ID = move.get_ValueAsInt("SHW_CostDistribution_ID");
    	if (shw_costDistribution_ID ==0)
    		return "Falta definir el retaceo";
    	String sql= "Select docstatus from shw_costdistribution where shw_costdistribution_ID = ?";
    	String status = DB.getSQLValueStringEx(get_TrxName(), sql, shw_costDistribution_ID);
    	if (!status.equals("CO"))
    		return "El retaceo no está completado";
    	String whereClause = "shw_costdistribution_ID =? or shw_costdistribution_ID in " +
    			" (select shw_costdistribution_ID from shw_Costdistribution where shw_costdistributionparent_ID=?)";
    	List<MInOutLine> list = new Query(getCtx(), MInOutLine.Table_Name, whereClause, get_TrxName())
    			.setParameters(shw_costDistribution_ID,shw_costDistribution_ID)
    			.list();
    	String sqlLoc = "select m_locator_ID from m_locator l inner join m_warehouse w on l.m_warehouse_ID = w.m_warehouse_ID where w.iscostdistribution = 'N' and l.isdefault = 'Y'";
    	int M_LocatorTo_ID = DB.getSQLValueEx(get_TrxName(), sqlLoc);
    	for (MInOutLine line:list)
    	{
    		if (line.getM_Product_ID() ==0)
    			continue;
    		MMovementLine mLine = new MMovementLine(move);
    		mLine.setM_Locator_ID(line.getM_Locator_ID());
    		mLine.setM_LocatorTo_ID(M_LocatorTo_ID);
    		mLine.setM_Product_ID(line.getM_Product_ID());
    		mLine.setMovementQty(line.getMovementQty());
    		if (line.getM_Product().getM_AttributeSet_ID() != 0)
    			mLine.setM_AttributeSetInstance_ID(line.getM_AttributeSetInstance_ID());
    		mLine.saveEx();
    	}
    	return "";
    }
    
}
