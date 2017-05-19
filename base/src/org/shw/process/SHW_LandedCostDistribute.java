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

import java.math.BigDecimal;
import java.util.ArrayList;

import org.adempiere.engine.CostEngineFactory;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MLandedCost;
import org.compiere.model.MLandedCostAllocation;
import org.compiere.process.SvrProcess;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.compiere.util.Env;

/**
 * 	Distribute Landed Costs
 *	
 *  @author Jorg Janke
 *  @version $Id: LandedCostDistribute.java,v 1.2 2006/07/30 00:51:02 jjanke Exp $
 */
public class SHW_LandedCostDistribute extends SvrProcess
{
	/** Parameter			*/
	private int			p_C_LandedCost_ID = 0;
	/** LC					*/
	private MLandedCost	m_lc = null;
	
	/**
	 * 	Prepare
	 */
	protected void prepare ()
	{
		p_C_LandedCost_ID = getRecord_ID();
	}	//	prepare

	/**
	 * 	Process
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt () throws Exception
	{
		m_lc = new MLandedCost (getCtx(), p_C_LandedCost_ID, get_TrxName());
		log.info(m_lc.toString());
		if (m_lc.get_ID() == 0)
			throw new AdempiereUserError("@NotFound@: @C_LandedCost_ID@ - " + p_C_LandedCost_ID);
		MInvoiceLine ivl = (MInvoiceLine)m_lc.getC_InvoiceLine();
		allocateLandedCosts(ivl);
		String error = m_lc.allocateCosts();
		for (MLandedCostAllocation lca:MLandedCostAllocation.getOfInvoiceLine(getCtx(), m_lc.getC_InvoiceLine_ID(), get_TrxName()))
		{
			lca.set_ValueOfColumn("SHW_TipoCostoRetaceo", m_lc.get_Value("SHW_TipoCostoRetaceo"));
			lca.set_ValueOfColumn(MLandedCost.COLUMNNAME_C_LandedCost_ID, m_lc.getC_LandedCost_ID());
			lca.saveEx();
		}
		generateCostDetail();
		if (error == null || error.length() == 0)
			return "@OK@";
		return error;
	}	//	doIt	
	
	private void generateCostDetail()
	{
		for (MLandedCostAllocation allocation : MLandedCostAllocation.getOfInvoiceLine(getCtx(), m_lc.getC_InvoiceLine_ID(), get_TrxName()))
		{
			CostEngineFactory.getCostEngine(getAD_Client_ID()).createCostDetailForLandedCostAllocation(allocation);
		}
	}
	

	public String allocateLandedCosts(MInvoiceLine ivl)
	{
	MLandedCost[] lcs = MLandedCost.getLandedCosts(ivl);
	if (lcs.length == 0)
		return "";
	String sql = "DELETE C_LandedCostAllocation WHERE C_InvoiceLine_ID=" + ivl.getC_InvoiceLine_ID();
	int no = DB.executeUpdate(sql, get_TrxName());
	if (no != 0)
		log.info("Deleted #" + no);

	int inserted = 0;
	//	*** Single Criteria ***
	if (lcs.length == 1)
	{
		MLandedCost lc = lcs[0];
		if (lc.getM_InOut_ID() != 0 && lc.getM_InOutLine_ID() == 0)
		{
			//	Create List
			ArrayList<MInOutLine> list = new ArrayList<MInOutLine>();
			MInOut ship = new MInOut (getCtx(), lc.getM_InOut_ID(), get_TrxName());
			MInOutLine[] lines = ship.getLines();
			for (int i = 0; i < lines.length; i++)
			{
				if (lines[i].isDescription() || lines[i].getM_Product_ID() == 0)
					continue;
				if (lc.getM_Product_ID() == 0
					|| lc.getM_Product_ID() == lines[i].getM_Product_ID())
					list.add(lines[i]);
			}
			if (list.size() == 0)
				return "No Matching Lines (with Product) in Shipment";
			//	Calculate total & base
			BigDecimal total = Env.ZERO;
			for (int i = 0; i < list.size(); i++)
			{
				MInOutLine iol = (MInOutLine)list.get(i);
				total = total.add(iol.getBase(lc.getLandedCostDistribution()));
			}
			if (total.signum() == 0)
				return "Total of Base values is 0 - " + lc.getLandedCostDistribution();
			//	Create Allocations
			for (int i = 0; i < list.size(); i++)
			{
				MInOutLine iol = (MInOutLine)list.get(i);
				MLandedCostAllocation lca = new MLandedCostAllocation (ivl, lc.getM_CostElement_ID());
				lca.setM_Product_ID(iol.getM_Product_ID());
				lca.setM_AttributeSetInstance_ID(iol.getM_AttributeSetInstance_ID());
				lca.setM_InOutLine_ID(iol.getM_InOutLine_ID());//SHW
				BigDecimal base = iol.getBase(lc.getLandedCostDistribution());
				lca.setBase(base);
				// MZ Goodwill
				// add set Qty from InOutLine
				lca.setQty(iol.getMovementQty());
				// end MZ
				if (base.signum() != 0)
				{
					double result = ivl.getLineNetAmt().multiply(base).doubleValue();
					result /= total.doubleValue();
					lca.setAmt(result, ivl.getPrecision());
				}
				if (!lca.save())
					return "Cannot save line Allocation = " + lca;
				inserted++;
			}
			log.info("Inserted " + inserted);
			allocateLandedCostRounding(ivl);
			return "";
		}
		//	Single Line
		else if (lc.getM_InOutLine_ID() != 0)
		{
			MInOutLine iol = new MInOutLine (getCtx(), lc.getM_InOutLine_ID(), get_TrxName());
			if (iol.isDescription() || iol.getM_Product_ID() == 0)
				return "Invalid Receipt Line - " + iol;
			MLandedCostAllocation lca = new MLandedCostAllocation (ivl, lc.getM_CostElement_ID());
			lca.setM_Product_ID(iol.getM_Product_ID());
			lca.setM_AttributeSetInstance_ID(iol.getM_AttributeSetInstance_ID());
			BigDecimal base = iol.getBase(lc.getLandedCostDistribution()); 
			lca.setBase(base);
			lca.setAmt(ivl.getLineNetAmt());
			// MZ Goodwill
			// add set Qty from InOutLine
			lca.setQty(iol.getMovementQty());
			// end MZ
			if (lca.save())
				return "";
			return "Cannot save single line Allocation = " + lc;
		}
		//	Single Product
		else if (lc.getM_Product_ID() != 0)
		{
			MLandedCostAllocation lca = new MLandedCostAllocation (ivl, lc.getM_CostElement_ID());
			lca.setM_Product_ID(lc.getM_Product_ID());	//	No ASI
			lca.setAmt(ivl.getLineNetAmt());
			if (lca.save())
				return "";
			return "Cannot save Product Allocation = " + lc;
		}
		else
			return "No Reference for " + lc;
	}

	//	*** Multiple Criteria ***
	String LandedCostDistribution = lcs[0].getLandedCostDistribution();
	int M_CostElement_ID = lcs[0].getM_CostElement_ID();
	for (int i = 0; i < lcs.length; i++)
	{
		MLandedCost lc = lcs[i];
		if (!LandedCostDistribution.equals(lc.getLandedCostDistribution()))
			return "Multiple Landed Cost Rules must have consistent Landed Cost Distribution";
		if (lc.getM_Product_ID() != 0 && lc.getM_InOut_ID() == 0 && lc.getM_InOutLine_ID() == 0)
			return "Multiple Landed Cost Rules cannot directly allocate to a Product";
		if (M_CostElement_ID != lc.getM_CostElement_ID())
			return "Multiple Landed Cost Rules cannot different Cost Elements";
	}
	//	Create List
	ArrayList<MInOutLine> list = new ArrayList<MInOutLine>();
	for (int ii = 0; ii < lcs.length; ii++)
	{
		MLandedCost lc = lcs[ii];
		if (lc.getM_InOut_ID() != 0 && lc.getM_InOutLine_ID() == 0)		//	entire receipt
		{
			MInOut ship = new MInOut (getCtx(), lc.getM_InOut_ID(), get_TrxName());
			MInOutLine[] lines = ship.getLines();
			for (int i = 0; i < lines.length; i++)
			{
				if (lines[i].isDescription()		//	decription or no product
					|| lines[i].getM_Product_ID() == 0)
					continue;
				if (lc.getM_Product_ID() == 0		//	no restriction or product match
					|| lc.getM_Product_ID() == lines[i].getM_Product_ID())
					list.add(lines[i]);
			}
		}
		else if (lc.getM_InOutLine_ID() != 0)	//	receipt line
		{
			MInOutLine iol = new MInOutLine (getCtx(), lc.getM_InOutLine_ID(), get_TrxName());
			if (!iol.isDescription() && iol.getM_Product_ID() != 0)
				list.add(iol);
		}
	}
	if (list.size() == 0)
		return "No Matching Lines (with Product)";
	//	Calculate total & base
	BigDecimal total = Env.ZERO;
	for (int i = 0; i < list.size(); i++)
	{
		MInOutLine iol = (MInOutLine)list.get(i);
		total = total.add(iol.getBase(LandedCostDistribution));
	}
	if (total.signum() == 0)
		return "Total of Base values is 0 - " + LandedCostDistribution;
	//	Create Allocations
	for (int i = 0; i < list.size(); i++)
	{
		MInOutLine iol = (MInOutLine)list.get(i);
		MLandedCostAllocation lca = new MLandedCostAllocation (ivl, lcs[0].getM_CostElement_ID());
		lca.setM_Product_ID(iol.getM_Product_ID());
		lca.setM_AttributeSetInstance_ID(iol.getM_AttributeSetInstance_ID());
		BigDecimal base = iol.getBase(LandedCostDistribution);
		lca.setBase(base);
		// MZ Goodwill
		// add set Qty from InOutLine
		lca.setQty(iol.getMovementQty());
		// end MZ
		if (base.signum() != 0)
		{
			double result = ivl.getLineNetAmt().multiply(base).doubleValue();
			result /= total.doubleValue();
			lca.setAmt(result, ivl.getPrecision());
		}
		if (!lca.save())
			return "Cannot save line Allocation = " + lca;
		inserted++;
	}

	log.info("Inserted " + inserted);
	allocateLandedCostRounding(ivl);
	return "";
	}
	

	private void allocateLandedCostRounding(MInvoiceLine ivl)
	{
		MLandedCostAllocation[] allocations = MLandedCostAllocation.getOfInvoiceLine(
			getCtx(), ivl.getC_InvoiceLine_ID(), get_TrxName());
		MLandedCostAllocation largestAmtAllocation = null;
		BigDecimal allocationAmt = Env.ZERO;
		for (int i = 0; i < allocations.length; i++)
		{
			MLandedCostAllocation allocation = allocations[i];
			if (largestAmtAllocation == null
				|| allocation.getAmt().compareTo(largestAmtAllocation.getAmt()) > 0)
				largestAmtAllocation = allocation;
			allocationAmt = allocationAmt.add(allocation.getAmt());
		}
		BigDecimal difference = ivl.getLineNetAmt().subtract(allocationAmt);
		if (difference.signum() != 0)
		{
			largestAmtAllocation.setAmt(largestAmtAllocation.getAmt().add(difference));
			largestAmtAllocation.saveEx();
			log.config("Difference=" + difference
				+ ", C_LandedCostAllocation_ID=" + largestAmtAllocation.getC_LandedCostAllocation_ID()
				+ ", Amt" + largestAmtAllocation.getAmt());
		}
	}	//	allocateLandedCostRounding

}	//	LandedCostDistribute
