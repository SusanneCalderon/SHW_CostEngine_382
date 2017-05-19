/******************************************************************************
 * Product: ADempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 2003-2014 e-Evolution Consultants. All Rights Reserved.      *
 * Copyright (C) 2003-2014 Victor Pérez Juárez 								  *
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
 * Contributor(s): Victor Pérez Juárez  (victor.perez@e-evolution.com)		  *
 * Sponsors: e-Evolution Consultants (http://www.e-evolution.com/)            *
 *****************************************************************************/

package org.shw.process;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.adempiere.engine.CostEngineFactory;
import org.compiere.model.I_M_InOutLine;
import org.compiere.model.MDocType;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MLandedCost;
import org.compiere.model.MLandedCostAllocation;
import org.compiere.model.MPeriod;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;

/**
 * Fill the Landed Cost based on Material Receipts Smart Browser Filter
 *
 * @author victor.perez@e-evolution.com, www.e-evolution.com
 */
public class SHW_GenerateLandedCost extends SvrProcess {

    /** Record ID */
    protected int pRecordId = 0;
    protected int pCostElementId = 0;
    protected String pLandedCostDistribution = null;
    protected boolean pCreateByProduct = false;
    protected String pSHW_TipoCostoRetaceo = null;
    protected List<MInOutLine> records = null;
    protected MInvoiceLine invoiceLine = null;
	private Integer		m_precision = null;

    /**
     * Get Parameters
     */
    protected void prepare() {
        pRecordId = getRecord_ID();
        if (records != null)
        	records.clear();
        
        for (ProcessInfoParameter para : getParameter()) {
            String name = para.getParameterName();
            if (para.getParameter() == null)
                ;
            else if (name.equals(MLandedCost.COLUMNNAME_LandedCostDistribution))
                pLandedCostDistribution = (String) para.getParameter();
            else if (name.equals(MLandedCost.COLUMNNAME_M_CostElement_ID))
                pCostElementId = para.getParameterAsInt();
            else if (name.equals("CreateByProduct"))
                pCreateByProduct = para.getParameterAsBoolean();
            else if (name.equals("SHW_TipoCostoRetaceo"))
            	pSHW_TipoCostoRetaceo = para.getParameterAsString();
            else
                log.log(Level.SEVERE, "Unknown Parameter: " + name);
        }
    }

    /**
     * Process - Generate Export Format
     *
     * @return info
     */
    protected String doIt() throws Exception { 	
    	
        String receipts = "";

        invoiceLine = new MInvoiceLine(getCtx(), getRecord_ID(), get_TrxName());
        MInvoice invoice = (MInvoice)invoiceLine.getC_Invoice();
        final String sqlUpdate = "UPDATE " + invoice.get_TableName() + " SET Posted = 'N' WHERE "+ invoice.get_TableName() + "_ID=?";
		DB.executeUpdate(sqlUpdate, new Object[] {invoice.get_ID()}, false , invoice.get_TrxName());
		//Delete account
		final String sqldelete = "DELETE FROM Fact_Acct WHERE Record_ID =? AND AD_Table_ID=?";		
		DB.executeUpdate (sqldelete ,new Object[] { invoice.get_ID(),
				invoice.get_Table_ID() }, false , invoice.get_TrxName());
        getRecords();
        MInOutLine iol = records.get(0);
        String error = PeriodOpen(iol.getParent());
        if (!error.equals(""))
        	return error;
        deleteOldValues();
        if (pCreateByProduct == true) {
            for (MInOutLine inOutLine : getRecords()) {
            	if (!error.equals("")) {
            		return error;					
				}
                createLandedCost(null, inOutLine);
                receipts = receipts.concat(
                        inOutLine.getParent().getDocumentNo() + " "
                                + inOutLine.getM_Product().getValue()).concat(" ");
            }
        } 
        else 
        {
            LinkedHashMap<Integer, MInOut> inOutList = new LinkedHashMap<Integer, MInOut>();
            for (MInOutLine inOutLine : getRecords()) {
                MInOut inOut = inOutLine.getParent();
                if (inOutList.containsKey(inOut.getM_InOut_ID()))
                    continue;

                inOutList.put(inOut.getM_InOut_ID(), inOut);
            }

            for (Entry<Integer, MInOut> entry : inOutList.entrySet()) {
                MInOut inOut = entry.getValue();
            	if (!error.equals("")) {
            		return error;					
				}
                createLandedCost(inOut, null);
                receipts = receipts.concat(inOut.getDocumentNo()).concat(" ");
            }
        } 
        allocateLandedCosts();
        generateCostDetail();
        return error;
    }

    public void createLandedCost(MInOut inOut, MInOutLine inOutLine) {
        MInOut document = inOut;
    if (document == null)
            document = inOutLine.getParent();
        
        MLandedCost landedCost = new MLandedCost(getCtx(), 0, get_TrxName());
        landedCost.setAD_Org_ID(document.getAD_Org_ID());
        landedCost.setC_InvoiceLine_ID(pRecordId);
        landedCost.setM_InOut_ID(document.getM_InOut_ID());
        landedCost.setDescription(document.getPOReference());
        landedCost.setLandedCostDistribution(pLandedCostDistribution);
        landedCost.setM_CostElement_ID(pCostElementId);
        landedCost.set_ValueOfColumn("SHW_TipoCostoRetaceo", pSHW_TipoCostoRetaceo);
        landedCost.set_ValueOfColumn("SHW_CostDistribution_ID", document.get_Value("SHW_CostDistribution_ID"));
        if (inOut == null) {
            landedCost.setM_InOutLine_ID(inOutLine.getM_InOutLine_ID());
            landedCost.setM_Product_ID(inOutLine.getM_Product_ID());
        }
        
        
        landedCost.saveEx();
        
    }

    private List<MInOutLine> getRecords() {
        if (records != null && records.size() !=0)
            return records;

        StringBuilder whereClause = new StringBuilder("EXISTS (SELECT T_Selection_ID FROM T_Selection ");
        whereClause.append("WHERE T_Selection.AD_PInstance_ID=? AND T_Selection.T_Selection_ID=M_InOutLine.M_InOutLine_ID )");
        //if (pCreateByProduct)
          //  whereClause.append("AND NOT EXISTS (SELECT 1 FROM C_LandedCost lc WHERE lc.C_InvoiceLine_ID=? AND lc.M_InOutLine_ID=M_InOutLine.M_InOutLine_ID))");
        //else
         //   whereClause.append("AND NOT EXISTS (SELECT 1 FROM C_LandedCost lc WHERE lc.C_InvoiceLine_ID=? AND lc.M_InOut_ID=M_InOutLine.M_InOut_ID)) ");

        records = new Query(getCtx(), I_M_InOutLine.Table_Name,
                whereClause.toString(), get_TrxName()).setClient_ID()
                .setParameters(getAD_PInstance_ID())
                .list();
        return records;
    }

	
	private void generateCostDetail()
	{
		for (MLandedCostAllocation allocation : MLandedCostAllocation.getOfInvoiceLine(getCtx(), invoiceLine.getC_InvoiceLine_ID(), get_TrxName()))
		{
			CostEngineFactory.getCostEngine(getAD_Client_ID()).createCostDetailForLandedCostAllocation(allocation);
		}
	}
	

	public String allocateLandedCosts()
	{
		MLandedCost[] lcs = MLandedCost.getLandedCosts(invoiceLine);
		if (lcs.length == 0)
			return "";
		String sql = "DELETE C_LandedCostAllocation WHERE C_InvoiceLine_ID=" + invoiceLine.getC_InvoiceLine_ID();
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
					MLandedCostAllocation lca = new MLandedCostAllocation (invoiceLine, lc.getM_CostElement_ID());
					lca.setM_Product_ID(iol.getM_Product_ID());
					lca.setM_AttributeSetInstance_ID(iol.getM_AttributeSetInstance_ID());
					lca.setM_InOutLine_ID(iol.getM_InOutLine_ID());//SHW
					BigDecimal base = iol.getBase(lc.getLandedCostDistribution());
					lca.setBase(base);
					// MZ Goodwill
					// add set Qty from InOutLine
					lca.setQty(iol.getMovementQty());
					lca.set_ValueOfColumn("SHW_TipoCostoRetaceo", pSHW_TipoCostoRetaceo);
					lca.set_ValueOfColumn("SHW_CostDistribution_ID", lc.get_Value("SHW_CostDistribution_ID"));
					// end MZ
					if (base.signum() != 0)
					{
						double result = invoiceLine.getLineNetAmt().multiply(base).doubleValue();
						result /= total.doubleValue();
						lca.setAmt(result, getPrecision(invoiceLine));
					}
					if (!lca.save())
						return "Cannot save line Allocation = " + lca;
					inserted++;
				}
				log.info("Inserted " + inserted);
				allocateLandedCostRounding(invoiceLine);
				return "";
			}
			//	Single Line
			else if (lc.getM_InOutLine_ID() != 0)
			{
				MInOutLine iol = new MInOutLine (getCtx(), lc.getM_InOutLine_ID(), get_TrxName());
				if (iol.isDescription() || iol.getM_Product_ID() == 0)
					return "Invalid Receipt Line - " + iol;
				MLandedCostAllocation lca = new MLandedCostAllocation (invoiceLine, lc.getM_CostElement_ID());
				lca.setM_Product_ID(iol.getM_Product_ID());
				lca.setM_AttributeSetInstance_ID(iol.getM_AttributeSetInstance_ID());
				BigDecimal base = iol.getBase(lc.getLandedCostDistribution()); 
				lca.setBase(base);
				lca.setAmt(invoiceLine.getLineNetAmt());
				lca.setM_InOutLine_ID(iol.getM_InOutLine_ID());
				lca.setM_AttributeSetInstance_ID(iol.getM_AttributeSetInstance_ID());
				// MZ Goodwill
				// add set Qty from InOutLine
				lca.setQty(iol.getMovementQty());
				lca.set_ValueOfColumn("SHW_TipoCostoRetaceo", pSHW_TipoCostoRetaceo);
				lca.set_ValueOfColumn("SHW_CostDistribution_ID", lc.get_Value("SHW_CostDistribution_ID"));
				
				// end MZ
				if (lca.save())
					return "";
				return "Cannot save single line Allocation = " + lc;
			}
			//	Single Product
			else if (lc.getM_Product_ID() != 0)
			{
				MLandedCostAllocation lca = new MLandedCostAllocation (invoiceLine, lc.getM_CostElement_ID());
				lca.setM_Product_ID(lc.getM_Product_ID());	//	No ASI
				lca.setAmt(invoiceLine.getLineNetAmt());
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
			MLandedCostAllocation lca = new MLandedCostAllocation (invoiceLine, lcs[0].getM_CostElement_ID());
			lca.setM_Product_ID(iol.getM_Product_ID());
			lca.setM_AttributeSetInstance_ID(iol.getM_AttributeSetInstance_ID());
			BigDecimal base = iol.getBase(LandedCostDistribution);
			lca.setBase(base);
			// MZ Goodwill
			// add set Qty from InOutLine
			lca.setQty(iol.getMovementQty());
			lca.set_ValueOfColumn("SHW_TipoCostoRetaceo", pSHW_TipoCostoRetaceo);
			lca.set_ValueOfColumn("SHW_CostDistribution_ID", iol.get_Value("SHW_CostDistribution_ID"));
			lca.setM_InOutLine_ID(iol.getM_InOutLine_ID());
			
			// end MZ
			if (base.signum() != 0)
			{
				double result = invoiceLine.getLineNetAmt().multiply(base).doubleValue();
				result /= total.doubleValue();
				lca.setAmt(result, getPrecision(invoiceLine));
			}
			if (!lca.save())
				return "Cannot save line Allocation = " + lca;
			inserted++;
		}

		log.info("Inserted " + inserted);
		allocateLandedCostRounding(invoiceLine);
		return "";
	}	//	allocate Costs

	private void allocateLandedCostRounding(MInvoiceLine invoiceLine)
	{
		MLandedCostAllocation[] allocations = MLandedCostAllocation.getOfInvoiceLine(
			getCtx(), invoiceLine.getC_InvoiceLine_ID(), get_TrxName());
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
		BigDecimal difference = invoiceLine.getLineNetAmt().subtract(allocationAmt);
		if (difference.signum() != 0)
		{
			largestAmtAllocation.setAmt(largestAmtAllocation.getAmt().add(difference));
			largestAmtAllocation.saveEx();
			log.config("Difference=" + difference
				+ ", C_LandedCostAllocation_ID=" + largestAmtAllocation.getC_LandedCostAllocation_ID()
				+ ", Amt" + largestAmtAllocation.getAmt());
		}
	}	//	allocateLandedCostRounding
	

	public int getPrecision(MInvoiceLine invoiceLine)
	{
		if (m_precision != null)
			return m_precision.intValue();

		String sql = "SELECT c.stdprecision "
			+ "FROM C_Currency c INNER JOIN C_Invoice x ON (x.C_Currency_ID=c.C_Currency_ID) "
			+ "WHERE x.C_Invoice_ID=?";
		int i = DB.getSQLValue(get_TrxName(), sql, invoiceLine.getC_Invoice_ID());
		if (i < 0)
		{
			log.warning("getPrecision = " + i + " - set to 2");
			i = 2;
		}
		m_precision = new Integer(i);
		return m_precision.intValue();
	}	//	getPrecision
	
	private String PeriodOpen(MInOut inout)
	{
		MDocType dt = MDocType.get(getCtx(), inout.getC_DocType_ID());
		String processMsg = "";
		//	Std Period open?
		if (!MPeriod.isOpen(getCtx(), inout.getDateAcct(), dt.getDocBaseType(), inout.getAD_Org_ID()))
		{
			processMsg = "@PeriodClosed@";
			return DocAction.STATUS_Invalid;
		}
		return processMsg;
	}
	
	private void deleteOldValues()
	{
		String deleteCostdetail = "delete from m_costdetail where c_landedcostallocation_ID=?";
		ArrayList<Object> params = new ArrayList<>();
        for (MLandedCostAllocation lca: MLandedCostAllocation.getOfInvoiceLine(getCtx(), 
        		invoiceLine.getC_InvoiceLine_ID(), get_TrxName()))
        {
        	params.clear();
        	params.add(lca.getC_LandedCostAllocation_ID());
        	int no = DB.executeUpdateEx(deleteCostdetail,params.toArray(), get_TrxName());
        	lca.deleteEx(true);			
        }       	
       
        for (MLandedCost lc:MLandedCost.getLandedCosts(invoiceLine))
        	lc.deleteEx(true);
	}


}