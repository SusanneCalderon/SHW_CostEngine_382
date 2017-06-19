/******************************************************************************
 * Product: ADempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 2006-2016 ADempiere Foundation, All Rights Reserved.         *
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
 * or via info@adempiere.net or http://www.adempiere.net/license.html         *
 *****************************************************************************/

package org.adempiere.process;

import java.util.ArrayList;

import org.compiere.model.MProductPrice;
import org.compiere.util.DB;
import org.compiere.util.Env;

/** Generated Process for (CreateProductEntriesInPriceList)
 *  @author ADempiere (generated) 
 *  @version Release 3.9.0
 */
public class CreateProductEntriesInPriceList extends CreateProductEntriesInPriceListAbstract
{

	private int M_PriceListVersion_ID =0;
	@Override
	protected void prepare()
	{
		super.prepare();
		M_PriceListVersion_ID = getRecord_ID();
	}

	@Override
	protected String doIt() throws Exception
	{
		String sql = "select LG_Route_ID from LG_Route where ad_client_id=? and LG_Route_ID in (1000017, 1000018, 1000019)";
		ArrayList<Object> params = new ArrayList<>();
		params.add(Env.getAD_Client_ID(getCtx()));
			
		int LGRoute_ID = DB.getSQLValueEx(get_TrxName(), sql, params);
		for(Integer key : getSelectionKeys()) {
			MProductPrice pp = new MProductPrice(getCtx(), M_PriceListVersion_ID, key, get_TrxName());
			if (pp != null) {
				pp.set_ValueOfColumn("LG_Route_ID", LGRoute_ID);
				pp.saveEx();
			}
			
		}
		return "";
	}
}