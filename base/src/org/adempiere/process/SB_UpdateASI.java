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

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.apps.AEnv;
import org.compiere.apps.AWindow;
import org.compiere.model.MAttributeSet;
import org.compiere.model.MClient;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInvoice;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.model.MStorage;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Trx;






/**
 *  Creates Payment from c_invoice, including Aging
 *
 *  @author Susanne Calderon
 */

public class SB_UpdateASI  extends SvrProcess

{	
	int P_C_Bpartner_ID = 0;
    int P_C_Invoice_ID;
    int P_AD_Org_ID = 0;
    int P_C_DocType_ID = 0;
    Timestamp P_DateInvoiced = null;
    int P_C_Charge1_ID = 0;
    int P_C_Charge2_ID = 0;
    Boolean P_IsSalesTrx = false;
    @Override    
    protected void prepare()
    {  


    }
    
      
    @Override
    protected String doIt() throws Exception
    {
    	String result = "";
    	if (getRecord_ID() != 0)
    	{
    		MOrder order = new MOrder(getCtx(), getRecord_ID(), get_TrxName());
    		for (MOrderLine oLine:order.getLines())
    			setAsi(oLine);
    		return "";
    	}
    	try
    	{
    	 List<MOrderLine> orderLines = (List<MOrderLine>) getInstancesForSelection(get_TrxName());
         orderLines.stream().filter(orderLine -> orderLine != null).forEach( orderLine -> {
             setAsi(orderLine);
         });
    	}

 		catch (Exception e)
 		{
 			;
 		}
    		
    	
    	return result;
    }
    
    private void setAsi(MOrderLine oLine)
    {

    	try {
    		Trx.run(trxName ->
    		{String AsiDescription = "";
    		oLine.set_ValueOfColumn("QtyBackOrdered", oLine.getQtyEntered());
    		oLine.setDescription(oLine.getM_Product().getName() + " " + oLine.getQtyOrdered().toString());
    		int lineNo=oLine.getLine();
    		BigDecimal qtyDelivered = Env.ZERO;
    		Boolean isInstance = true;
    		if (oLine.getM_Product().getM_AttributeSet_ID() !=0 && oLine.getM_AttributeSetInstance_ID() !=0)
    		{
    			MAttributeSet mas = MAttributeSet.get(oLine.getCtx(), oLine.getM_Product().getM_AttributeSet_ID());
    			if (mas.isInstanceAttribute() && !mas.isLotMandatory())
    				AsiDescription = oLine.getM_AttributeSetInstance().getDescription();
    			isInstance = mas.isInstanceAttribute() && mas.isLotMandatory();
    		}
    		if (isInstance && oLine.getM_AttributeSetInstance_ID() !=0)
    			return;
    		MProduct product = (MProduct)oLine.getM_Product();
    		if ( !product.isASIMandatory(true, oLine.getAD_Org_ID()))
    			return;
    		String MMPolicy = product.getMMPolicy();
    		Timestamp minGuaranteeDate = oLine.getParent().getDatePromised();
    		MStorage[] storages = MStorage.getWarehouse(oLine.getCtx(),oLine.getParent(). getM_Warehouse_ID(), oLine.getM_Product_ID(), 
    				oLine.getM_AttributeSetInstance_ID(),
    				minGuaranteeDate, MClient.MMPOLICY_FiFo.equals(MMPolicy), true, 0, oLine.get_TrxName());
    		if (storages.length ==0)
    			return;
    		BigDecimal qtyToDeliver = oLine.getQtyOrdered();
    		Boolean start = true;
    		for (MStorage storage: storages)
    		{
    			if (AsiDescription != "" && !storage.getM_AttributeSetInstance().getDescription().equals(AsiDescription)) 
    				continue;
    			BigDecimal qtyavailable = storage.getQtyOnHand();
    			BigDecimal qtyToOrder = Env.ZERO;
    			if (qtyavailable.compareTo(Env.ZERO) <= 0)
    				continue;
    			if (start)
    			{
    				qtyToOrder = qtyToDeliver.compareTo(qtyavailable)<0? qtyToDeliver: qtyavailable;
    				oLine.setQty(qtyToOrder);
    				oLine.setM_AttributeSetInstance_ID(storage.getM_AttributeSetInstance_ID());
    				oLine.saveEx();
    				qtyToDeliver = qtyToDeliver.subtract(qtyToOrder);
    				start = false;
    				if (qtyToDeliver.compareTo(Env.ZERO)==0)
    					break;
    			}
    			else
    			{
    				MOrderLine oLinenew = new MOrderLine(oLine.getParent());
    				MOrderLine.copyValues(oLine, oLinenew);
    				lineNo = lineNo + 10;
    				qtyToOrder = qtyToDeliver.compareTo(qtyavailable)<0? qtyToDeliver: qtyavailable;
    				qtyToDeliver = qtyToDeliver.subtract(qtyToOrder);
    				oLinenew.setQty(qtyToOrder);
    				oLinenew.setLine(lineNo);
    				oLinenew.setM_AttributeSetInstance_ID(storage.getM_AttributeSetInstance_ID());
    				oLinenew.saveEx();     
    				if (qtyToDeliver.signum() == 0)
    					break;               
    			}

    			if (qtyToDeliver.signum() == 0)
    				break;
    		}
    		if (qtyToDeliver.compareTo(Env.ZERO) !=0)
    		{
    			MOrderLine oLinenew = new MOrderLine(oLine.getParent());
    			MOrderLine.copyValues(oLine, oLinenew);
    			oLinenew.setQty(qtyToDeliver);
    			oLinenew.setM_AttributeSetInstance_ID(0);
    			oLinenew.saveEx();     
    		}
    		return;
    		});
    	}
    	catch (Exception e)
    	{
    		addLog(e.getMessage());
    		return ;
    	}
    	return ;
    }
    	



}
