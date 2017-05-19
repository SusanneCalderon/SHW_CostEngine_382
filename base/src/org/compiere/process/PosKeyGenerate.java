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

import java.util.ArrayList;
import java.util.List;

import org.adempiere.exceptions.FillMandatoryException;
import org.compiere.model.MPOSKey;
import org.compiere.model.MPOSKeyLayout;
import org.compiere.model.MProduct;
import org.compiere.model.MProductCategory;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;


public class PosKeyGenerate extends SvrProcess {

	private int posKeyLayoutId = 0;
	private int productCategoryId = 0;
	
	@Override
	protected void prepare() {

		for ( ProcessInfoParameter para : getParameter())
		{
			
			if ( para.getParameterName().equals("C_POSKeyLayout_ID") )
				posKeyLayoutId = para.getParameterAsInt();
			if ( para.getParameterName().equals("M_Product_Category_ID") )
				productCategoryId = para.getParameterAsInt();
			else 
				log.info("Parameter not found " + para.getParameterName());
		}

		if ( posKeyLayoutId == 0 )
		{
			posKeyLayoutId = getRecord_ID();
		}
	}
	
	/**
	 * Generate keys for each product
	 */
	@Override
	protected String doIt() throws Exception {

		//if ( posKeyLayoutId == 0 )
		//	throw new FillMandatoryException("C_POSKeyLayout_ID");

		int count = 0;
		String whereClause = "";
		Object [] params = new Object[] {};
		if ( productCategoryId > 0 )
		{
			whereClause = "M_Product_Category_ID = ? ";
			params = new Object[] {productCategoryId};
		}
		List<MProductCategory> cats = new Query(getCtx(), MProductCategory.Table_Name, whereClause, get_TrxName())
		.setParameters(params)
		.setClient_ID()
		.setOnlyActiveRecords(true)
		.list();
		for (MProductCategory cat:cats)
		{
			MPOSKeyLayout pk = new Query(getCtx(), MPOSKeyLayout.Table_Name, "name =?", get_TrxName())
			.setParameters(cat.getName())
			.first();
			if (pk == null)
			{
				pk = new MPOSKeyLayout(getCtx(), 0, get_TrxName());
				String name = cat.getName();				
				if (cat.getM_Product_Category_Parent_ID() != 0)
					name = name  +" P: " + cat.getM_Product_Category_Parent().getName();
				pk.setName(name);
				pk.saveEx();
			}
			String where = "M_Product_category_ID=?";
			Query query = new Query(getCtx(), MProduct.Table_Name, where, get_TrxName())
			.setParameters(cat.getM_Product_Category_ID())
			.setOnlyActiveRecords(true)
			.setOrderBy("Value");
			List<MProduct> products = query.list();
			if (products.size() >0)
			{
				pk.setPOSKeyLayoutType(MPOSKeyLayout.POSKEYLAYOUTTYPE_Product);
				pk.saveEx();
			}
			for (MProduct product : products )
			{
				String sqlFind = "select count(*) from c_poskey where c_poskeylayout_ID=? and m_product_ID=?";

				ArrayList<Object> paramsFind = new ArrayList<Object>();
				paramsFind.add(pk.getC_POSKeyLayout_ID());
				paramsFind.add(product.getM_Product_ID());
				int countproduct = DB.getSQLValue(get_TrxName(), sqlFind, paramsFind.toArray());
				if (countproduct > 0)
					continue;
				MPOSKey key = new MPOSKey(getCtx(), 0, get_TrxName());
				key.setName(product.getName());
				key.setM_Product_ID(product.getM_Product_ID());
				key.setC_POSKeyLayout_ID(pk.getC_POSKeyLayout_ID());
				key.setSeqNo(count*10);
				key.setQty(Env.ONE);
				key.saveEx();
				count++;
			}
			/*
			String whereSons = "m_product_category_parent_ID=?";
			List<MProductCategory> listsons = new Query(getCtx(), MProductCategory.Table_Name, whereSons, get_TrxName())
				.setParameters(cat.getM_Product_Category_ID())
				.list();
			for (MProductCategory catSon:listsons)
			{
				MPOSKeyLayout psl = new Query(getCtx(), MPOSKeyLayout.Table_Name, "name=?", get_TrxName())
					.setParameters(catSon.getName())
					.first();

				String sqlFind = "select count(*) from c_poskey where c_poskeylayout_ID=? and SubKeyLayout_ID=?";

				ArrayList<Object> paramsFind = new ArrayList<Object>();
				paramsFind.add(pk.getC_POSKeyLayout_ID());
				paramsFind.add(psl.getC_POSKeyLayout_ID());
				int countproduct = DB.getSQLValue(get_TrxName(), sqlFind, paramsFind.toArray());
				if (countproduct > 0)
					continue;
				MPOSKey key = new MPOSKey(getCtx(), 0, get_TrxName());
				key.setName(catSon.getName());
				key.setC_POSKeyLayout_ID(pk.getC_POSKeyLayout_ID());
				key.setSeqNo(count*10);
				key.setQty(Env.ONE);
				key.setSubKeyLayout_ID(psl.getC_POSKeyLayout_ID());
				key.saveEx();
				
			}*/
		}


		return "@Created@ " + count;
	}

}
