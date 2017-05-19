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

package org.adempiere.process;

import java.math.BigDecimal;
import java.util.logging.Level;

import org.compiere.model.MInvoice;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.eevolution.model.MHRConcept;
import org.eevolution.model.MHREmployee;
import org.eevolution.model.MHRMovement;
import org.eevolution.model.MHRProcess;



public class HR_SM_CreateMovement_Payment extends SvrProcess
{
    
    int p_hr_process_id = 0;
    
    @Override    
    protected void prepare()
    {
    	ProcessInfoParameter[] para = getParameter();
    	for (int i = 0; i < para.length; i++)
    	{
    		String name = para[i].getParameterName();
    		if (para[i].getParameter() == null)
    			;
    		else if (name.equals(MHRProcess.COLUMNNAME_HR_Process_ID))
    			p_hr_process_id = para[i].getParameterAsInt();
    		else
    			log.log(Level.SEVERE, "Unknown Parameter: " + name);
    	}  
    }
    
    
    
    @Override
    protected String doIt() throws Exception
    {
    	String whereClause = "EXISTS (SELECT T_Selection_ID FROM T_Selection WHERE  T_Selection.AD_PInstance_ID=? AND T_Selection.T_Selection_ID=C_Invoice.c_Invoice_ID)";		
    	int[] invoice_ids  = new Query(getCtx(), MInvoice.Table_Name, whereClause, get_TrxName())
										.setClient_ID()
										.setParameters(new Object[]{getAD_PInstance_ID()})
										.getIDs();
		
		MInvoice invoice = null;
		MHRProcess process = new MHRProcess(getCtx(), p_hr_process_id, get_TrxName());
		for (int c_invoice_id : invoice_ids)
		{ 
			String sqlOpenamt = "select sum(openamt) from rv_openitem where c_invoice_id =? and duedate <=?";
			invoice = new MInvoice(getCtx(), c_invoice_id, get_TrxName());

			BigDecimal openamt = DB.getSQLValueBD(get_TrxName(), sqlOpenamt,  new Object[]{c_invoice_id, process.getHR_Period().getEndDate()});
			MHRConcept conceptOK   = new MHRConcept(getCtx(),1000086 , get_TrxName());
			MHRMovement movementOK = new MHRMovement(Env.getCtx(),0,null);
			movementOK.setDescription("Factura pendiente " + invoice.getDocumentNo());
			movementOK.setHR_Process_ID(p_hr_process_id);
			movementOK.setC_BPartner_ID(invoice.getC_BPartner_ID());
			movementOK.setHR_Concept_ID(1000086);
			movementOK.setHR_Concept_Category_ID(conceptOK.getHR_Concept_Category_ID());
			movementOK.setColumnType(conceptOK.getColumnType());
			movementOK.setQty(Env.ONE);
			movementOK.setAmount(openamt );
			movementOK.setTextMsg("");
			movementOK.setServiceDate(process.getHR_Period().getEndDate());
			movementOK.setValidFrom(process.getHR_Period().getStartDate());
			movementOK.setValidTo(process.getHR_Period().getEndDate());
			MHREmployee employee  = MHREmployee.getActiveEmployee(Env.getCtx(), movementOK.getC_BPartner_ID(), null);
			if (employee != null) {
				movementOK.setAD_Org_ID(employee.getAD_Org_ID());
				movementOK.setHR_Department_ID(employee.getHR_Department_ID());
				movementOK.setHR_Job_ID(employee.getHR_Job_ID());
				movementOK.setC_Activity_ID(employee.getC_Activity_ID() > 0 ? employee.getC_Activity_ID() : employee.getHR_Department().getC_Activity_ID());
			}
			movementOK.set_ValueOfColumn(MInvoice.COLUMNNAME_C_Invoice_ID, c_invoice_id);
			movementOK.setIsManual(true);
			movementOK.saveEx();
			
			}
    	
    	return "";
    }

}
