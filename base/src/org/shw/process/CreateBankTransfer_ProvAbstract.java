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

package org.shw.process;

import java.math.BigDecimal;
import org.compiere.process.SvrProcess;
/** Generated Process for (CreateBankTransfer_Prov)
 *  @author ADempiere (generated) 
 *  @version Release 3.9.0
 */
public abstract class CreateBankTransfer_ProvAbstract extends SvrProcess
{
	/** Process Value 	*/
	private static final String VALUE = "CreateBankTransfer_Prov";
	/** Process Name 	*/
	private static final String NAME = "CreateBankTransfer_Prov";
	/** Process Id 	*/
	private static final int ID = 3000260;
 
	/**	Parameter Name for C_BankAccount_ID	*/
	public static final String C_BankAccount_ID = "C_BankAccount_ID";
	/**	Parameter Name for TrxAmt	*/
	public static final String TrxAmt = "TrxAmt";
	/**	Parameter Name for C_Charge_ID	*/
	public static final String C_Charge_ID = "C_Charge_ID";
	/**	Parameter Name for Percent	*/
	public static final String Percent = "Percent";

	/**	Parameter Value for bankAccountId	*/
	private int bankAccountId;
	/**	Parameter Value for transactionAmount	*/
	private BigDecimal transactionAmount;
	/**	Parameter Value for chargeId	*/
	private int chargeId;
	/**	Parameter Value for percent	*/
	private BigDecimal percent;
 

	@Override
	protected void prepare()
	{
		bankAccountId = getParameterAsInt(C_BankAccount_ID);
		transactionAmount = getParameterAsBigDecimal(TrxAmt);
		chargeId = getParameterAsInt(C_Charge_ID);
		percent = getParameterAsBigDecimal(Percent);
	}

	/**	 Getter Parameter Value for bankAccountId	*/
	protected int getBankAccountId() {
		return bankAccountId;
	}

	/**	 Getter Parameter Value for transactionAmount	*/
	protected BigDecimal getTransactionAmount() {
		return transactionAmount;
	}

	/**	 Getter Parameter Value for chargeId	*/
	protected int getChargeId() {
		return chargeId;
	}

	/**	 Getter Parameter Value for percent	*/
	protected BigDecimal getPercent() {
		return percent;
	}

	/**	 Getter Parameter Value for Process ID	*/
	public static final int getProcessId() {
		return ID;
	}

	/**	 Getter Parameter Value for Process Value	*/
	public static final String getProcessValue() {
		return VALUE;
	}

	/**	 Getter Parameter Value for Process Name	*/
	public static final String getProcessName() {
		return NAME;
	}
}