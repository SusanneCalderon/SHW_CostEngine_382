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
package org.adempiere.model;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LoggingPermission;

import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.model.*;


/**
 *	Callouts zum Lesen der Waage
 *
 *  @author SCalderon
 *  */
public class CalloutSHW_WeightStation extends CalloutEngine
{
	

	private boolean steps = false;
	
	
	public String weight_net (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		//if (isCalloutActive())		//	assuming it is resetting value
		//	return "";
		//int iol_ID = Env.getContextAsInt(ctx, WindowNo, "M_InoutLine_ID");
		//MInOutLine iol = new MInOutLine(Env.getCtx(), iol_ID , null);
		if (Env.isSOTrx(ctx, WindowNo) && mField.getColumnName().equals("Weight_tare"))
			return "";

		if (!Env.isSOTrx(ctx, WindowNo) && mField.getColumnName().equals("Weight_gross"))
			return "";
		
		BigDecimal weightGross = (BigDecimal)mTab.getValue("Weight_gross");
		BigDecimal weightTare = (BigDecimal)mTab.getValue("Weight_tare");
		BigDecimal weightPacking = (BigDecimal)mTab.getValue("Weight_PackingMaterial");
		BigDecimal netweight =   weightGross.subtract(weightTare).subtract(weightPacking);
		mTab.setValue("Weight_net",  netweight);

		//Umrechnung in Einheit der Zeile
		BigDecimal factor = Env.ZERO;
		int c_UOM_ID = Env.getContextAsInt(ctx, WindowNo, "C_UOM_ID");
		int M_Product_ID = Env.getContextAsInt(ctx, WindowNo, "M_Product_ID");

		int m_Product_ID = Env.getContextAsInt(ctx, WindowNo, "M_Product_ID");
		int uom_Libra = 1000017;
		MUOMConversion[] rates = MUOMConversion.getProductConversions(ctx, m_Product_ID);
		//factor = MUOMConversion.shw_probe(rates, c_UOM_ID, uom_Libra);

		for (MUOMConversion rate: rates)
		{
			if (rate.getC_UOM_To_ID() == uom_Libra && rate.getC_UOM_ID() ==  c_UOM_ID)
			{
				factor = rate.getDivideRate();
				break;
			}
		}
		BigDecimal qty = (BigDecimal)mTab.getValue("Weight_net");
		qty = qty.multiply(factor);
		mTab.setValue("QtyEntered",  qty);
		int C_UOM_To_ID = Env.getContextAsInt(ctx, WindowNo, "C_UOM_ID");
		BigDecimal QtyEntered = qty;
		BigDecimal QtyEntered1 = QtyEntered.setScale(MUOM.getPrecision(ctx, C_UOM_To_ID), BigDecimal.ROUND_HALF_UP);
		if (QtyEntered.compareTo(QtyEntered1) != 0)
		{
			log.fine("Corrected QtyEntered Scale UOM=" + C_UOM_To_ID
					+ "; QtyEntered=" + QtyEntered + "->" + QtyEntered1);
			QtyEntered = QtyEntered1;
			mTab.setValue("QtyEntered", QtyEntered);
		}
		BigDecimal MovementQty = MUOMConversion.convertProductFrom (ctx, M_Product_ID,
				C_UOM_To_ID, QtyEntered);
		if (MovementQty == null)
			MovementQty = QtyEntered;
		boolean conversion = QtyEntered.compareTo(MovementQty) != 0;
		Env.setContext(ctx, WindowNo, "UOMConversion", conversion ? "Y" : "N");
		mTab.setValue("MovementQty", MovementQty);


		return "";
	}	//	
	
	public String weight_total (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		int M_Inout_ID = Env.getContextAsInt(ctx, WindowNo, "M_InOut_ID");
		BigDecimal c = Env.ZERO;
		BigDecimal peso = Env.ZERO;

		MInOut inout = new MInOut(ctx, M_Inout_ID, null);
		MInOutLine[] lines = inout.getLines();
		for (MInOutLine line : lines)
		{
		    if (line.getC_UOM_ID() == 1000017)
		    c = Env.ONE;
		    else
		    c = MUOMConversion.getProductRateTo (ctx,line.getM_Product_ID() ,1000017 );
		    if (c == null)
		    return "No hay conversión a libra para "  + line.getM_Product().getName();
		    peso = peso.add(c.multiply(line.getMovementQty()));
		}
		peso = peso.add((BigDecimal)mTab.getValue("Weight_PackingMaterial"));
		mTab.setValue("Weight", peso);
		return "";
	}
	
	public String weight_net_total (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{		
		BigDecimal weightGross = (BigDecimal)mTab.getValue("Weight_gross");
		BigDecimal weightTare = (BigDecimal)mTab.getValue("Weight_tare");
		BigDecimal netweight =   weightGross.subtract(weightTare);
		mTab.setValue("Weight_net",  netweight);
		return "";
	}
	
	

	



}	//	