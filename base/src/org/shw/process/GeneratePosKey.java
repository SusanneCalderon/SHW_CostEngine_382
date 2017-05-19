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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.compiere.model.MPOSKey;
import org.compiere.model.MPOSKeyLayout;
import org.compiere.model.MProduct;
import org.compiere.model.MProductCategory;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.Env;

/**
 *  Creates Payment from c_invoice, including Aging
 *
 *  @author Susanne Calderon
 */

public class GeneratePosKey  extends SvrProcess

{	

	private int p_M_Product_Category_ID = 0;
	private MPOSKeyLayout pkl = null;;
    @Override    
    protected void prepare()
    {    			
		for (ProcessInfoParameter para : getParameter())
		{
			String name = para.getParameterName();
			if (para.getParameter() == null)
				;
			if (name.equals(MProductCategory.COLUMNNAME_M_Product_Category_ID))
				p_M_Product_Category_ID = para.getParameterAsInt();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}


    }
    
      
    @Override
    protected String doIt() throws Exception
    {
		pkl = new MPOSKeyLayout(getCtx(), getRecord_ID(), get_TrxName());
		if (MPOSKeyLayout.POSKEYLAYOUTTYPE_Keyboard.equals(pkl.getPOSKeyLayoutType())
			|| MPOSKeyLayout.POSKEYLAYOUTTYPE_Numberpad.equals(pkl.getPOSKeyLayoutType()))
			return "";
		else if (MPOSKeyLayout.POSKEYLAYOUTTYPE_Product.equals(pkl.getPOSKeyLayoutType()))
			createProductKeys();
		else 
			createsubKeys();
    	return "";
    }
    
    
    private void createProductKeys()
    {
    	ArrayList<Object> params = new ArrayList<>();
    	StringBuffer whereClause = new StringBuffer();
    	if (p_M_Product_Category_ID!= 0)
    	{
        	whereClause.append("M_Product_Category_ID =? and ");
        	params.add(p_M_Product_Category_ID);
    	}
    	whereClause.append("m_product_ID not in (select m_product_ID from c_poskey where c_poskeylayout_ID=?)");
    	params.add(pkl.getC_POSKeyLayout_ID());
    	List<MProduct> products = new Query(getCtx(), MProduct.Table_Name, whereClause.toString(), get_TrxName())
    		.setParameters(params.toArray())
    		.setOnlyActiveRecords(true)
    		.setClient_ID()
    		.list();
    	int count =0;
    	for (MProduct product:products)
    	{
        	MPOSKey key = new MPOSKey(getCtx(), 0, get_TrxName());
    		key.setName(product.getName());
    		key.setM_Product_ID(product.getM_Product_ID());
    		key.setC_POSKeyLayout_ID(pkl.getC_POSKeyLayout_ID());
    		key.setSeqNo(count*10);
    		key.setQty(Env.ONE);
    		key.setIsActive(false);
    		key.saveEx();    		
    	}    			
    }   

    private void createsubKeys()
    {
    	ArrayList<Object> params = new ArrayList<>();
    	StringBuffer whereClause = new StringBuffer();
    	whereClause.append("c_poskeylayout_ID <>? and ");
    	whereClause.append("c_poskeylayout_ID not in (select SubKeyLayout_ID from c_poskey where c_poskeylayout_ID=? and SubKeyLayout_ID is not null)");
    	if (p_M_Product_Category_ID !=0)
    	{
    		MProductCategory cat = new MProductCategory(getCtx(), p_M_Product_Category_ID, get_TrxName());
    		whereClause.append(" and name like '%" + cat.getName() + "%'");
    	}
    	params.add(pkl.getC_POSKeyLayout_ID());
    	params.add(pkl.getC_POSKeyLayout_ID());
    	List<MPOSKeyLayout> layouts = new Query(getCtx(), MPOSKeyLayout.Table_Name, whereClause.toString(), get_TrxName())
    		.setOnlyActiveRecords(true)
    		.setClient_ID()
    		.setParameters(params.toArray())
    		.list();
    	int count =0;
    	for (MPOSKeyLayout subkl: layouts)
    	{
        	MPOSKey key = new MPOSKey(getCtx(), 0, get_TrxName());
    		key.setName(subkl.getName());
    		key.setC_POSKeyLayout_ID(pkl.getC_POSKeyLayout_ID());
    		key.setSeqNo(count*10);
    		key.setQty(Env.ONE);
    		key.setIsActive(false);
    		key.saveEx();    		    		
    	}
    }

}
