/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
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
 * Copyright (C) 2003-2013 E.R.P. Consultores y Asociados.                    *
 * All Rights Reserved.                                                       *
 * Contributor(s): Carlos Parada www.erpconsultoresyasociados.com             *
 *****************************************************************************/
package org.shw.model;

import java.math.BigDecimal;
import java.util.Properties;
import org.compiere.model.GridField;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.eevolution.grid.BrowserCallOutEngine;
import org.eevolution.grid.BrowserRow;


/**
 * @author carlosaparada@gmail.com Carlos Parada, ERP Consultores y asociados
 *
 */
public class CalloutSBQuotation extends BrowserCallOutEngine {

	
	public String methodExample(Properties ctx,  int WindowNo,BrowserRow row, GridField field, Object value, Object oldValue,int currentRow, int currentColumn)
	{
		System.out.println("Hi! this is a example of implementation callouts");
		row.getValueOfColumn(currentRow, "C_UOM_ID");
		int c_uom_ID = Env.getContextAsInt(Env.getCtx(), "@PF_C_UOM_ID@");
		System.out.println("This is a Value for :"+value);
		System.out.println("This is a Old Value :"+oldValue);
		System.out.println("This is a Value for GridField:" + field.getValue());
		System.out.println("This is a Old Value for GridField:" + field.getOldValue());
		field.setValue(new BigDecimal(9999.33), true);
		row.setValueOfColumn(currentRow, "SO_CreditUsed", field);
		return "";
	}
	
	public String changeComments(Properties ctx,  int WindowNo,BrowserRow row, GridField field, Object value, Object oldValue,int currentRow, int currentColumn)
	{
		field.setValue("Ausprobieren", true);
		row.setValueOfColumn(currentRow, "SO_CreditUsed", field);
		return "";
	}

	public String setContextM_ProductOfLine(Properties ctx,  int WindowNo,BrowserRow row, GridField field, Object value, Object oldValue,int currentRow, int currentColumn)
	{
		GridField gf = (GridField)row.getValueOfColumn(currentRow, "OLINE_M_Product_ID");
		Integer M_Product_ID = (Integer)gf.getValue();
		Env.setContext(Env.getCtx(),WindowNo, "OLINE_M_Product_ID", M_Product_ID.intValue());
		return "";
	}

	public String setContextWeight(Properties ctx,  int WindowNo,BrowserRow row, GridField field, Object value, Object oldValue,int currentRow, int currentColumn)
	{
		Integer C_UOM_ID = (Integer)value;
		Env.setContext(Env.getCtx(), "@PF_C_UOMWeight_ID@", C_UOM_ID);
		return "";
	}
	

	
	static CLogger log = CLogger.getCLogger(CalloutSB.class);
}//BrowserCallOutExample
