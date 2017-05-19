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

import java.util.List;

import org.compiere.model.MBPartner;
import org.compiere.model.MRfQTopic;
import org.compiere.model.MRfQTopicSubscriber;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;

/**
 * 
 *
 *  @author Susanne Calderon
 */

public class CreateTopicSubscriber  extends SvrProcess
{
    private String 	p_Name = "";
    private int 	p_C_RFQTopic_ID = 0;

	protected List<MBPartner> m_records = null;
    
    @Override    
    protected void prepare()
    {    	

    	for (ProcessInfoParameter para:getParameter())
    	{

    		String name = para.getParameterName();
    		if (para.getParameter() == null)
    			;
    		else if (name.equals(MRfQTopic.COLUMNNAME_Name))
    			p_Name = para.getParameterAsString();
    		else if (name.equals(MRfQTopic.COLUMNNAME_C_RfQ_Topic_ID))
    			p_C_RFQTopic_ID = para.getParameterAsInt();
    	}
    }
    
    
    
    @Override
    protected String doIt() throws Exception
    {		
		
		String whereClause = "EXISTS (SELECT T_Selection_ID FROM T_Selection WHERE  T_Selection.AD_PInstance_ID=? " +
				" AND T_Selection.T_Selection_ID=c_bpartner.C_BPartner_ID)";

		MRfQTopic topic = null;
		if (p_Name.equals("")&& p_C_RFQTopic_ID == 0)
			return "Hay que escoger una convocatoria o ingresar un nuevo nommbre";

		else if (p_C_RFQTopic_ID != 0)
			topic = new MRfQTopic(getCtx(), p_C_RFQTopic_ID, get_TrxName());
		else
		{
			topic = new MRfQTopic(getCtx(), 0, get_TrxName());
			topic.setName(p_Name);
			topic.saveEx();
		}
		m_records = new Query(getCtx(), MBPartner.Table_Name, whereClause, get_TrxName())
											.setParameters(getAD_PInstance_ID())
											.setClient_ID()
											.list();
		for (MBPartner bpartner:m_records)
		{
			MRfQTopicSubscriber ts = new MRfQTopicSubscriber(getCtx(), 0, get_TrxName());
			ts.setC_RfQ_Topic_ID(topic.getC_RfQ_Topic_ID());
			ts.setC_BPartner_ID(bpartner.getC_BPartner_ID());
			ts.setAD_User_ID(bpartner.getPrimaryAD_User_ID());
			ts.setAD_Org_ID(bpartner.getAD_Org_ID());
			ts.setC_BPartner_Location_ID(bpartner.getPrimaryC_BPartner_Location_ID());
			ts.saveEx();
		}
    	return "";
    }
    
    
    
    
    
}
