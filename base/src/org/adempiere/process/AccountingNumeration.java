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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.compiere.model.MFactAcct;
import org.compiere.model.MPeriod;
import org.compiere.model.Query;
import org.compiere.util.DB;

/** Generated Process for (AccountingNumeration)
 *  @author ADempiere (generated) 
 *  @version Release 3.9.0
 */
public class AccountingNumeration extends AccountingNumerationAbstract
{
	
	private int C_Period_ID = 0;
	private int C_Year_ID = 0;
	@Override
	protected void prepare()
	{
		super.prepare();
	}

	@Override
	protected String doIt() throws Exception
	{
		
		int daysBetween = org.compiere.util.TimeUtil.getDaysBetween(getAccountDate(),getAccountDateTo()) + 1;
		GregorianCalendar day = new GregorianCalendar();
		day.setTimeInMillis(getAccountDate().getTime());
        ArrayList<Object> params = new ArrayList<Object>();
		for (int i=0;i<daysBetween;i++)
		{
			int inc = i==0?0:1;
			day.add(Calendar.DATE, inc);
			String whereClause = getFactAccts(new Timestamp(day.getTimeInMillis()));
			whereClause = whereClause.length()<=1?"":"where fact_Acct_ID in"  + whereClause;
			if (whereClause.equals(""))
				continue;
			int year = day.get(Calendar.YEAR);
			String Documentno = DB.getDocumentNo(getAD_Client_ID(), MFactAcct.Table_Name,get_TrxName()) + "_" + year;	
			String sql = "Update fact_acct set documentno = ? "+ whereClause;
			params.clear();
			params.add(Documentno);
			DB.executeUpdateEx(sql, params.toArray(), get_TrxName());
		}

		return "";
	}
	
	public String getFactAccts(Timestamp dateFrom) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append("dateacct =? and postingType='A'");
        ArrayList<Object> params = new ArrayList<Object>();
        params.add(dateFrom);
        String FactAcct_IDs = "(";
        int[] IDs =  new Query(getCtx(), MFactAcct.Table_Name, whereClause.toString(), get_TrxName())
                .setClient_ID()
                .setOnlyActiveRecords(true)
                .setParameters(params)
                .setOrderBy("dateacct, fact_Acct_ID")
                .getIDs();
        for (int ID:IDs)
        	FactAcct_IDs = FactAcct_IDs + ID + ",";
        FactAcct_IDs = FactAcct_IDs.substring(0, FactAcct_IDs.length()-1);
        FactAcct_IDs = FactAcct_IDs + ")";
        return FactAcct_IDs;
    }
}