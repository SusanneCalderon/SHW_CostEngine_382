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
 * or via info@compiere.org or http://www.compiere.org/license.html 		  *
 * @contributor Karsten Thiemann / Schaeffer AG - kthiemann@adempiere.org     *
 *****************************************************************************/
package org.adempiere.process;

import java.sql.Timestamp;
import java.util.List;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.I_C_Order;
import org.compiere.model.MOrder;
import org.compiere.model.Query;
import org.compiere.process.SvrProcess;
import org.compiere.util.Trx;


/**
 *	Order Batch Processing
 *	
 *  @author Jorg Janke
 *  eEvolution author Victor Perez <victor.perez@e-evolution.com>
 *  @version $Id: OrderBatchProcess.java,v 1.2 2006/07/30 00:51:02 jjanke Exp $
 */
public class SB_OrderBatchProcess extends SvrProcess
{

	/**
	 * 	Prepare
	 */
	protected void prepare ()
	{}	//	prepare

	/**
	 * 	Process
	 *	@return msg
	 *	@throws Exception
	 */
	protected String doIt () throws Exception
	{
		int counter = 0;
		int errCounter = 0;
		Trx dbTransaction = null;
		for(Integer key : getSelectionKeys()) {
			
			 if (dbTransaction != null) {
                 dbTransaction.commit(true);
                 dbTransaction.close();
             }

             //Create new transaction for this product
             dbTransaction = Trx.get(key.toString(), true);
			if (process(key,dbTransaction.getTrxName()))
				counter++;
			else
				errCounter++;
		}
		
		return "@Updated@=" + counter + ", @Errors@=" + errCounter;
	}	//	doIt
	
	/**
	 * 	Process Order
	 *	@param orderId order ID
	 *	@return true if ok
	 */
	private boolean process (int orderId, String Trx_Name)
	{
		try {
				MOrder order = new MOrder(getCtx(), orderId, Trx_Name);
				log.severe(order.getDocumentNo());
				log.info(order.toString());
				//
				order.setDocAction("CO");
				if (order.processIt("CO")) {
					order.saveEx();
					addLog(0, null, null, order.getDocumentNo() + ": OK");
				}
				else {
					addLog(0, null, null, order.getDocumentNo() + ": Error " + order.getProcessMsg());
					throw new AdempiereException(order.getDocumentNo() + ": Error " + order.getProcessMsg());
				}
			
	}
	catch (Exception e)
	{
		addLog(e.getMessage());
		return false;
	}
	return true;
}	//	process
	
}	//	OrderBatchProcess
