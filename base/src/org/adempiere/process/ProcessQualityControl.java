/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2003 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.adempiere.process;

import java.math.BigDecimal;
import java.sql.Timestamp;

import org.adempiere.model.MPPOrderQualityControl;
import org.compiere.model.MUser;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.eevolution.model.MPPOrder;

/**
 * @author Susanne Calderon
 */
public class ProcessQualityControl extends SvrProcess
{
	protected Timestamp p_DueDate = null;
	protected int		p_salesrep_ID = -1;
	protected String	p_password = "";
	protected int		p_C_BP_Group_ID = -1;
	protected int		p_C_BPartner_ID = -1;
	protected boolean   p_IsIncludeOrders = false;
	protected boolean   p_IsApproved = false;
	protected boolean   p_IsIncludeInvoices = true;

	protected void prepare()
	{
		ProcessInfoParameter[] parameters = getParameter();
		for (ProcessInfoParameter para: parameters)
		{
			log.config("prepare - " + para);
			String name = para.getParameterName();
			if (para.getParameter() == null)
				;
			else if (name.equals("DueDate"))
				p_DueDate = (Timestamp)para.getParameter();
			else if (name.equals("SalesRep_ID"))
				p_salesrep_ID = ((BigDecimal)para.getParameter()).intValue();
			else if (name.equals("IsApproved"))
				p_IsApproved = "Y".equals(para.getParameter());
			else if (name.equals("Password"))
				p_password = (String)para.getParameter();
//			else
//				log.config("prepare - Unknown Parameter: " + name);
		}
	}	//	prepare


	protected String doIt() throws Exception
	{	
		MUser user = new MUser(getCtx(), p_salesrep_ID, get_TrxName());
		if (!user.getPassword().equals(p_password))
			return "clave equivocada";
		MPPOrderQualityControl oqc = new MPPOrderQualityControl(getCtx(), getRecord_ID(), get_TrxName());
		MPPOrder pporder = new MPPOrder(getCtx(), oqc.getPP_Order_ID(), get_TrxName());
		if (p_IsApproved)
			pporder.set_ValueOfColumn("IsInProduction", true);
		pporder.saveEx();
		return "";
	}
	
	
	
	

}
