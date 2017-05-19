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
 * Copyright (C) 2003-2007 e-Evolution,SC. All Rights Reserved.               *
 * Contributor(s): Victor Perez www.e-evolution.com                           *
 *****************************************************************************/
package org.eevolution.process;



import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;

import org.adempiere.exceptions.FillMandatoryException;
import org.compiere.model.MBPartner;
import org.compiere.model.MDocType;
import org.compiere.model.MFactAcct;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoicePaySchedule;
import org.compiere.model.MInvoiceTax;
import org.compiere.model.MPeriod;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;


/**
 * Re-calculate Invoice Tax (and unpost the document)
 *  @author victor.perez@e-evolution.com, e-Evolution http://www.e-evolution.com
 * 				<li>FR [ 2520591 ] Support multiples calendar for Org 
 * 				@see http://sourceforge.net/tracker2/?func=detail&atid=879335&aid=2520591&group_id=176962
 * @author Teo Sarca, www.arhipac.ro
 */
public class InvoiceCalculateTax extends SvrProcess
{
	public static final String PARAM_C_Invoice_ID = "C_Invoice_ID";
	
	private int p_C_Invoice_ID = 0;

	@Override
	protected void prepare() 
	{
		for (ProcessInfoParameter para : getParameter())
		{
			String name = para.getParameterName();
			if (para.getParameter() == null)
			{
				;
			}
			else if (name.equals(PARAM_C_Invoice_ID))
			{
				p_C_Invoice_ID = para.getParameterAsInt();
			}
		}
		
		if (p_C_Invoice_ID <= 0)
		{
			throw new FillMandatoryException(PARAM_C_Invoice_ID);
		}
	}

	@Override
	protected String doIt() throws Exception
	{
		MInvoice invoice = new MInvoice(getCtx(), p_C_Invoice_ID, get_TrxName());
		recalculateTax(invoice);
		//
		return "@ProcessOK@";
	}
	
	public void recalculateTax(MInvoice invoice)
	{
		//
		// Delete accounting /UnPost
		MPeriod.testPeriodOpen(invoice.getCtx(), invoice.getDateAcct(), invoice.getC_DocType_ID(), invoice.getAD_Org_ID());
		MFactAcct.deleteEx(MInvoice.Table_ID, invoice.get_ID(), invoice.get_TrxName());
		//
		// Update Invoice
		invoice.calculateTaxTotal();
		invoice.setPosted(false);
		invoice.saveEx();
		//

		translateWithholdingToTaxes(invoice);
		//
		// Update balance
		MBPartner bp = new MBPartner (invoice.getCtx(), invoice.getC_BPartner_ID(), invoice.get_TrxName());
		bp.setTotalOpenBalance();
		bp.setSOCreditStatus();
		bp.saveEx();
	}
	
	private String translateWithholdingToTaxes(MInvoice inv) {
		BigDecimal sumit = new BigDecimal(0);
		
		MDocType dt = new MDocType(inv.getCtx(), inv.getC_DocTypeTarget_ID(), inv.get_TrxName());
		String genwh = dt.get_ValueAsString("GenerateWithholding");
		if (genwh == null || genwh.equals("N")) {
			// document configured to not manage withholdings - delete any
			String sqldel = "DELETE FROM LCO_InvoiceWithholding "
				+ " WHERE C_Invoice_ID = ?";
			PreparedStatement pstmtdel = null;
			try
			{
				// Delete previous records generated
				pstmtdel = DB.prepareStatement(sqldel,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_UPDATABLE, inv.get_TrxName());
				pstmtdel.setInt(1, inv.getC_Invoice_ID());
				int nodel = pstmtdel.executeUpdate();
				log.config("LCO_InvoiceWithholding deleted="+nodel);
			} catch (Exception e) {
				log.log(Level.SEVERE, sqldel, e);
				return "Error creating C_InvoiceTax from LCO_InvoiceWithholding -delete";
			} finally {
				DB.close(pstmtdel);
				pstmtdel = null;
			}
			inv.set_CustomColumn("WithholdingAmt", Env.ZERO);
			
		} else {
			// translate withholding to taxes
			String sql = 
				  "SELECT C_Tax_ID, NVL(SUM(TaxBaseAmt),0) AS TaxBaseAmt, NVL(SUM(TaxAmt),0) AS TaxAmt "
				 + " FROM LCO_InvoiceWithholding "
				+ " WHERE C_Invoice_ID = ? AND IsCalcOnPayment = 'N' AND IsActive = 'Y' "
				+ "GROUP BY C_Tax_ID";
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try
			{
				pstmt = DB.prepareStatement(sql, inv.get_TrxName());
				pstmt.setInt(1, inv.getC_Invoice_ID());
				rs = pstmt.executeQuery();
				while (rs.next()) {
					MInvoiceTax it = new MInvoiceTax(inv.getCtx(), 0, inv.get_TrxName());
					it.setAD_Org_ID(inv.getAD_Org_ID());
					it.setC_Invoice_ID(inv.getC_Invoice_ID());
					it.setC_Tax_ID(rs.getInt(1));
					it.setTaxBaseAmt(rs.getBigDecimal(2));
					it.setTaxAmt(rs.getBigDecimal(3).negate());
					sumit = sumit.add(rs.getBigDecimal(3));
					if (!it.save())
						return "Error creating C_InvoiceTax from LCO_InvoiceWithholding - save InvoiceTax";
				}
				BigDecimal actualamt = (BigDecimal) inv.get_Value("WithholdingAmt");
				if (actualamt == null)
					actualamt = new BigDecimal(0);
				if (actualamt.compareTo(sumit) != 0 || sumit.signum() != 0) {
					inv.set_CustomColumn("WithholdingAmt", sumit);
					// Subtract to invoice grand total the value of withholdings
					BigDecimal gt = inv.getGrandTotal();
					inv.setGrandTotal(gt.subtract(sumit));
					inv.save();  // need to save here in order to let apply get the right total
				}
				
				if (sumit.signum() != 0) {
					// GrandTotal changed!  If there are payment schedule records they need to be recalculated
					// subtract withholdings from the first installment
					BigDecimal toSubtract = sumit;
					for (MInvoicePaySchedule ips : MInvoicePaySchedule.getInvoicePaySchedule(inv.getCtx(), inv.getC_Invoice_ID(), 0, inv.get_TrxName())) {
						if (ips.getDueAmt().compareTo(toSubtract) >= 0) {
							ips.setDueAmt(ips.getDueAmt().subtract(toSubtract));
							toSubtract = Env.ZERO;
						} else {
							toSubtract = toSubtract.subtract(ips.getDueAmt());
							ips.setDueAmt(Env.ZERO);
						}
						if (!ips.save()) {
							return "Error saving Invoice Pay Schedule subtracting withholdings";
						}
						if (toSubtract.signum() <= 0)
							break;
					}
				}
			} catch (Exception e) {
				log.log(Level.SEVERE, sql, e);
				return "Error creating C_InvoiceTax from LCO_InvoiceWithholding - select InvoiceTax";
			} finally {
				DB.close(rs, pstmt);
				rs = null; pstmt = null;
			}
		}

		return null;
	}
}
