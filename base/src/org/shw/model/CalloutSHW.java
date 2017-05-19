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
package org.shw.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Properties;

import org.compiere.model.CalloutEngine;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.MBPartner;
import org.compiere.model.MBankStatement;
import org.compiere.model.MDocType;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MLocator;
import org.compiere.model.MOrderLine;
import org.compiere.model.MPayment;
import org.compiere.model.MPaymentTerm;
import org.compiere.model.MPriceList;
import org.compiere.model.MProductPO;
import org.compiere.model.MProject;
import org.compiere.model.MProjectType;
import org.compiere.model.MRole;
import org.compiere.model.MUOMConversion;
import org.compiere.model.Query;
import org.compiere.util.DB;
import org.compiere.util.Env;

import com.mchange.v2.codegen.bean.Property;


/**
 *	Callouts zum Lesen der Waage
 *
 *  @author SCalderon
 *  */
public class CalloutSHW extends CalloutEngine
{
	

	private boolean steps = false;
	
	
	
	
	public String payment_Contract_ID (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{			
		Integer contract_ID = (Integer)value;
		if (contract_ID == null || contract_ID.intValue() == 0)
			return "";
		MInvoice contract = new MInvoice(Env.getCtx(), contract_ID, null);
		BigDecimal paidamt = DB.getSQLValueBDEx(null, "Select coalesce(sum(payamt), 0) from c_payment where shw_contract_ID=?", contract_ID);
		BigDecimal openAmt = contract.getGrandTotal(false).subtract(paidamt);		
		mTab.setValue(MPayment.COLUMNNAME_PayAmt,  openAmt);
		if (openAmt.compareTo(Env.ZERO)==0)
			return "";
		for (MInvoiceLine ivl : contract.getLines())
		{
			mTab.setValue(MPayment.COLUMNNAME_C_Charge_ID,  ivl.getC_Charge_ID());
		}
		return "";
	}
	

	
	public String invoiceDocType (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{			
		Integer c_doctype_ID = (Integer)value;
		if (c_doctype_ID == null || c_doctype_ID.intValue() == 0)
			return "";
		MDocType dt = new MDocType(Env.getCtx(), c_doctype_ID, null);
		
		mTab.setValue("isContract",  dt.get_Value("isContract"));
		return "";
	}
	

	public String test (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{	
		//org.shw.model.CalloutSHW.test
		//import org.compiere.model.MProjectType
		Object A_Value = value;
		GridField A_Field = mField;
		GridTab A_Tab = mTab;
		Properties A_Ctx = ctx;
		

		//import java.util.ArrayList;
		//import org.compiere.util.DB;
		
		if (this.isCalloutActive() || A_Value == null)
			return "";
		int C_DocType_ID = Env.getContextAsInt(A_Ctx, "0|C_DocType_Target_ID");
		Integer C_Activity_ID = (Integer)A_Value;
		ArrayList< Object> params = new ArrayList<>();/*
		params.add(M_Product_ID);
		String sql = "Select m_attributeset_ID from m_product where m_product_ID =?";
		int M_Attributeset_ID = DB.getSQLValueEx(null, sql	, params);
		if (M_Attributeset_ID == 1000002)
			A_Tab.setValue("M_AttributesetInstance_ID", 1000167);		
		int C_Order_ID = Env.getContextAsInt(A_Ctx, "C_Order_ID");
		if ( C_Order_ID == 0)
			return "";
		String sqlUpdate = "Update c_orderline set c_activity_ID =? where c_Order_ID=?";
		ArrayList<Object> params = new ArrayList<>();
		params.add(C_Activity_ID);
		params.add(C_Order_ID);
		int no = DB.executeUpdateEx(sql, params, trxName)*/
		return "";
	}
	

	public String updateVendorPrice (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{	
		if (isCalloutActive() || value == null)
			return "";
		Integer M_Product_ID = (Integer)value;
		if (Env.getContext(ctx, "0|" + MInvoice.COLUMNNAME_IsSOTrx).equals("Y"))
			return "";
		int C_BPartner_ID = Env.getContextAsInt(ctx, WindowNo, "C_BPartner_ID");
		StringBuffer sql = new StringBuffer("select ");
		sql.append(MProductPO.COLUMNNAME_PriceLastInv + " from m_product_PO where M_Product_ID=? ");
		sql.append(" and c_bpartner_ID =" + C_BPartner_ID);
		sql.append(" and isactive = 'Y' and discontinued = 'N' ");
		BigDecimal pricelastInv = DB.getSQLValueBD(null, sql.toString(), M_Product_ID);
		if (pricelastInv == null)
			return "";
		if (pricelastInv.compareTo(Env.ZERO) != 0)
			mTab.setValue(MProductPO.COLUMNNAME_PriceLastInv, pricelastInv);
		
        return "";
	}
	

	public String locator_Org (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{	
		if (value == null)
			return "";
		Integer M_Locator_ID = (Integer)value;
		MLocator loc = new MLocator(ctx, M_Locator_ID, null);

		mTab.setValue(MLocator.COLUMNNAME_AD_Org_ID, loc.getAD_Org_ID());
		return "";
	}
	
	

	public String DT_Org (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{	
		/*Integer c_invoicebatch_ID = (Integer)value;
		 
		if (c_invoicebatch_ID == null || c_invoicebatch_ID.intValue() == 0)
			return "";
		MInvoiceBatch quedan = new MInvoiceBatch(Env.getCtx(), c_invoicebatch_ID, null);
		
		mTab.setValue(MInvoiceLine.COLUMNNAME_PriceEntered,  quedan.getDocumentAmt());*/
		Integer c_invoicebatch_ID = (Integer)value;
		return "";
	}
	
	public String ProjectweightVolume (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{	
		BigDecimal WeightEntered = (BigDecimal)mTab.getValue("WeightEntered");
		BigDecimal VolumeEntered = (BigDecimal)mTab.getValue("VolumeEntered");
		Integer C_Project_Parent_ID = (Integer)mTab.getValue("C_Project_Parent_ID");
		Integer C_UOM_ID = (Integer)mTab.getValue("C_UOM_ID");
		Integer C_UOM_Volume_ID = (Integer)mTab.getValue("C_UOM_Volume_ID");
		Boolean IsSummary = (Boolean)mTab.getValue("IsSummary");
		if (C_Project_Parent_ID == null || C_Project_Parent_ID == 0)
		{
			if (IsSummary)
				return"";
			mTab.setValue("Weight",  mTab.getValue("WeightEntered"));
			mTab.setValue("Volume",  mTab.getValue("VolumeEntered"));
            return "";
		}
		MProject parent = new MProject(ctx, C_Project_Parent_ID, null);	
			BigDecimal rateWeight = MUOMConversion.convert(C_UOM_ID, 
				parent.get_ValueAsInt("C_UOM_ID"), WeightEntered, true);

			BigDecimal rateVolume = MUOMConversion.convert(C_UOM_Volume_ID, 
					parent.get_ValueAsInt("C_UOM_Volume_ID"), VolumeEntered, true);
			mTab.setValue("Weight", rateWeight);
			mTab.setValue("Volume", rateVolume);		
		return "";
	}
	
	public String IsSalesOrderInmediate (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{	
		Object A_Value = value;
		Properties A_Ctx = ctx;
		int A_WindowNo = WindowNo;
		GridTab A_Tab = mTab;
		
		

		Boolean activeCallout = A_Tab != null ? A_Tab.getActiveCallouts().length > 1 : false;
        if (activeCallout || A_Value == null)
            return "";
		Integer M_Product_ID = (Integer)A_Tab.getValue("M_Product_ID");
		Integer C_DocTypeTarget_ID = Env.getContextAsInt(A_Ctx, A_WindowNo, "C_DocTypeTarget_ID");
		if (M_Product_ID == null || M_Product_ID == 0)
			return "" ;
		if (!(C_DocTypeTarget_ID == 1000453 || C_DocTypeTarget_ID ==1000452
				|| C_DocTypeTarget_ID== 1000443 || C_DocTypeTarget_ID == 1000444))
			return "";
		Integer C_Project_ID = Env.getContextAsInt(A_Ctx, A_WindowNo, "C_Project_ID");
		MProject project = new MProject(A_Ctx, C_Project_ID, null);
		Integer C_B_Partner_ID = project.getC_BPartner_ID();
		Integer Provider_ID = Env.getContextAsInt(A_Ctx, A_WindowNo, "0|C_BPartner_ID");
		int LG_Route_ID = project.get_ValueAsInt("LG_Route_ID");
		StringBuffer sql = new StringBuffer("select coalesce(po.isSalesOrderImmediate, p.isSalesOrderImmediate) ");
		sql.append(" from m_product p ") ;
		sql.append(" left join m_product_PO  po on p.m_product_ID = po.m_product_ID and po.c_bpartner_ID =?");
		sql.append("  left join c_bpartner_product  bpp on p.m_product_ID = bpp.m_product_ID and po.c_bpartner_ID =? ");
		sql.append(" left join lg_route_Product   rp on p.m_product_ID = rp.m_product_ID and rp.lg_route_ID =?");
		sql.append("  where p.m_product_ID =?");
		
		ArrayList<Object> params = new ArrayList<>();
		params.add(Provider_ID);
		params.add(C_B_Partner_ID);
		params.add(LG_Route_ID);
		params.add(M_Product_ID);
		String result = DB.getSQLValueStringEx(null, sql.toString(), params.toArray());
		boolean isSalesOrderImmediate =    "Y".equals(result)? true:false;
		A_Tab.setValue("isSalesOrderImmediate", isSalesOrderImmediate);
		return "";
	}
	
	
	public String BankAccount (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{	
		GridTab A_Tab = mTab;
		Object A_Value = value;
		Properties A_Ctx = ctx;
		
		 Integer C_BankAccount_ID = (Integer)A_Tab.getValue("C_BankAccount_ID");
		 Integer C_BankStatement_ID = (Integer)A_Tab.getValue("C_BankStatement_ID");
		 Timestamp StatementDate = (Timestamp)mTab.getValue("StatementDate");
		 if (C_BankAccount_ID == 0 || C_BankAccount_ID == null || StatementDate == null)
			 return "";
		 ArrayList<Object> params = new ArrayList<>();
		 params.add(C_BankAccount_ID);
		 params.add(StatementDate);
		 params.add(C_BankStatement_ID);
		 String whereClause = "C_Bankaccount_ID = ? and docstatus = 'CO' and StatementDate <=? and c_bankstatement_ID <>?";
		 MBankStatement bs = new Query(A_Ctx, MBankStatement.Table_Name	, whereClause, null)
		 	.setParameters(params.toArray())
		 	.setOrderBy("statementdate desc, c_Bankstatement_ID desc")
		 	.first();
		 A_Tab.setValue("BeginningBalance", bs.getEndingBalance());
		 
		 	

		return "";
	}

	public String Skonto (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{	

		GridTab A_Tab = mTab;
		Object A_Value = value;
		Properties A_Ctx = ctx;	
		Integer M_Product_ID = (Integer)A_Tab.getValue("M_Product_ID");
		 Integer C_Charge_ID = (Integer)A_Tab.getValue("C_Charge_ID");
		 if ((M_Product_ID == null ||M_Product_ID==0 )  && (C_Charge_ID==null || C_Charge_ID == 0))
			 return "";
		int C_PaymentTerm_ID = Env.getContextAsInt(A_Ctx, WindowNo + "|0|C_PaymentTerm_ID");
		int C_DocType_ID = Env.getContextAsInt(A_Ctx, WindowNo + "|0|C_DocTypeTarget_ID");
		MDocType dt = new MDocType(A_Ctx, C_DocType_ID, null);
		if (!dt.getDocSubTypeSO().equals(MDocType.DOCSUBTYPESO_POSOrder))
			return "";
		MPaymentTerm pt = new MPaymentTerm(A_Ctx, C_PaymentTerm_ID, null);
		if (pt.getDiscount().compareTo(Env.ZERO) !=0)
		{
			BigDecimal discount = (BigDecimal)A_Tab.getValue(MOrderLine.COLUMNNAME_Discount);
			discount = discount.add(pt.getDiscount());
			 A_Tab.setValue(MOrderLine.COLUMNNAME_Discount, discount);
			 int C_UOM_To_ID = Env.getContextAsInt(ctx, WindowNo, "C_UOM_ID");
				BigDecimal PriceEntered = (BigDecimal)mTab.getValue("PriceEntered");
				BigDecimal PriceActual = (BigDecimal)mTab.getValue("PriceActual");
				BigDecimal Discount = (BigDecimal)mTab.getValue("Discount");
				BigDecimal PriceLimit = (BigDecimal)mTab.getValue("PriceLimit");
				BigDecimal PriceList = (BigDecimal)mTab.getValue("PriceList"); 
				BigDecimal QtyOrdered = (BigDecimal)mTab.getValue("QtyOrdered");
				int M_PriceList_ID = Env.getContextAsInt(ctx, WindowNo, "M_PriceList_ID");
				int StdPrecision = MPriceList.getStandardPrecision(ctx, M_PriceList_ID);
				log.fine("PriceList=" + PriceList + ", Limit=" + PriceLimit + ", Precision=" + StdPrecision);
				log.fine("PriceEntered=" + PriceEntered + ", Actual=" + PriceActual + ", Discount=" + Discount);

				if ( PriceList.doubleValue() != 0 )
					PriceActual = new BigDecimal ((100.0 - Discount.doubleValue()) / 100.0 * PriceList.doubleValue());
				
				PriceEntered = MUOMConversion.convertProductFrom (ctx, M_Product_ID, 
					C_UOM_To_ID, PriceActual);
				if (PriceEntered == null)
					PriceEntered = PriceActual;
				mTab.setValue("PriceActual", PriceActual);
				mTab.setValue("PriceEntered", PriceEntered);//	Check PriceLimit
				String epl = Env.getContext(ctx, WindowNo, "EnforcePriceLimit");
				boolean enforce = Env.isSOTrx(ctx, WindowNo) && epl != null && epl.equals("Y");
				if (enforce && MRole.getDefault().isOverwritePriceLimit())
					enforce = false;
				//	Check Price Limit?
				if (enforce && PriceLimit.doubleValue() != 0.0
				  && PriceActual.compareTo(PriceLimit) < 0)
				{
					PriceActual = PriceLimit;
					PriceEntered = MUOMConversion.convertProductFrom (ctx, M_Product_ID, 
						C_UOM_To_ID, PriceLimit);
					if (PriceEntered == null)
						PriceEntered = PriceLimit;
					log.fine("(under) PriceEntered=" + PriceEntered + ", Actual" + PriceLimit);
					mTab.setValue ("PriceActual", PriceLimit);
					mTab.setValue ("PriceEntered", PriceEntered);
					mTab.fireDataStatusEEvent ("UnderLimitPrice", "", false);
					//	Repeat Discount calc
					if (PriceList.intValue() != 0)
					{
						Discount = new BigDecimal ((PriceList.doubleValue () - PriceActual.doubleValue ()) / PriceList.doubleValue () * 100.0);
						if (Discount.scale () > 2)
							Discount = Discount.setScale (2, BigDecimal.ROUND_HALF_UP);
						mTab.setValue ("Discount", Discount);
					}
				}
				//	Line Net Amt
				BigDecimal LineNetAmt = QtyOrdered.multiply(PriceActual);
				if (LineNetAmt.scale() > StdPrecision)
					LineNetAmt = LineNetAmt.setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);
				log.info("LineNetAmt=" + LineNetAmt);
				mTab.setValue("LineNetAmt", LineNetAmt);
		}

		

		return "";
	}
	
	
	
		
	
	
	

	



}	//	