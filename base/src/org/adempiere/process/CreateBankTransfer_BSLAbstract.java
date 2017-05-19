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

package org.adempiere.process;

import java.math.BigDecimal;
import java.sql.Timestamp;
import org.compiere.process.SvrProcess;
/** Generated Process for (groovy:CreateBankTransfer_BSL)
 *  @author ADempiere (generated) 
 *  @version Release 3.9.0
 */
public abstract class CreateBankTransfer_BSLAbstract extends SvrProcess
{
	/** Process Value 	*/
	private static final String VALUE = "CreateBankTransfer_BSL";
	/** Process Name 	*/
	private static final String NAME = "groovy:CreateBankTransfer_BSL";
	/** Process Id 	*/
	private static final int ID = 3000252;
 
	/**	Parameter Name for From_C_BankAccount_ID	*/
	public static final String From_C_BankAccount_ID = "From_C_BankAccount_ID";
	/**	Parameter Name for To_C_BankAccount_ID	*/
	public static final String To_C_BankAccount_ID = "To_C_BankAccount_ID";
	/**	Parameter Name for TenderType	*/
	public static final String TenderType = "TenderType";
	/**	Parameter Name for CheckNo	*/
	public static final String CheckNo = "CheckNo";
	/**	Parameter Name for Amount	*/
	public static final String Amount = "Amount";
	/**	Parameter Name for Description	*/
	public static final String Description = "Description";
	/**	Parameter Name for StatementDate	*/
	public static final String StatementDate = "StatementDate";
	/**	Parameter Name for DateAcct	*/
	public static final String DateAcct = "DateAcct";
	/**	Parameter Name for C_Currency_ID	*/
	public static final String C_Currency_ID = "C_Currency_ID";
	/**	Parameter Name for C_BPartner_ID	*/
	public static final String C_BPartner_ID = "C_BPartner_ID";
	/**	Parameter Name for C_Charge_ID	*/
	public static final String C_Charge_ID = "C_Charge_ID";
	/**	Parameter Name for C_ConversionType_ID	*/
	public static final String C_ConversionType_ID = "C_ConversionType_ID";
	/**	Parameter Name for AD_Org_ID	*/
	public static final String AD_Org_ID = "AD_Org_ID";

	/**	Parameter Value for bankAccountFromId	*/
	private int bankAccountFromId;
	/**	Parameter Value for bankAccountToId	*/
	private int bankAccountToId;
	/**	Parameter Value for tendertype	*/
	private String tendertype;
	/**	Parameter Value for checkNo	*/
	private String checkNo;
	/**	Parameter Value for amount	*/
	private BigDecimal amount;
	/**	Parameter Value for description	*/
	private String description;
	/**	Parameter Value for statementdate	*/
	private Timestamp statementdate;
	/**	Parameter Value for accountDate	*/
	private Timestamp accountDate;
	/**	Parameter Value for currencyId	*/
	private int currencyId;
	/**	Parameter Value for businessPartnerId	*/
	private int businessPartnerId;
	/**	Parameter Value for chargeId	*/
	private int chargeId;
	/**	Parameter Value for currencyTypeId	*/
	private int currencyTypeId;
	/**	Parameter Value for organizationId	*/
	private int organizationId;
 

	@Override
	protected void prepare()
	{
		bankAccountFromId = getParameterAsInt(From_C_BankAccount_ID);
		bankAccountToId = getParameterAsInt(To_C_BankAccount_ID);
		tendertype = getParameterAsString(TenderType);
		checkNo = getParameterAsString(CheckNo);
		amount = getParameterAsBigDecimal(Amount);
		description = getParameterAsString(Description);
		statementdate = getParameterAsTimestamp(StatementDate);
		accountDate = getParameterAsTimestamp(DateAcct);
		currencyId = getParameterAsInt(C_Currency_ID);
		businessPartnerId = getParameterAsInt(C_BPartner_ID);
		chargeId = getParameterAsInt(C_Charge_ID);
		currencyTypeId = getParameterAsInt(C_ConversionType_ID);
		organizationId = getParameterAsInt(AD_Org_ID);
	}

	/**	 Getter Parameter Value for bankAccountFromId	*/
	protected int getBankAccountFromId() {
		return bankAccountFromId;
	}

	/**	 Getter Parameter Value for bankAccountToId	*/
	protected int getBankAccountToId() {
		return bankAccountToId;
	}

	/**	 Getter Parameter Value for tendertype	*/
	protected String getTendertype() {
		return tendertype;
	}

	/**	 Getter Parameter Value for checkNo	*/
	protected String getCheckNo() {
		return checkNo;
	}

	/**	 Getter Parameter Value for amount	*/
	protected BigDecimal getAmount() {
		return amount;
	}

	/**	 Getter Parameter Value for description	*/
	protected String getDescription() {
		return description;
	}

	/**	 Getter Parameter Value for statementdate	*/
	protected Timestamp getStatementdate() {
		return statementdate;
	}

	/**	 Getter Parameter Value for accountDate	*/
	protected Timestamp getAccountDate() {
		return accountDate;
	}

	/**	 Getter Parameter Value for currencyId	*/
	protected int getCurrencyId() {
		return currencyId;
	}

	/**	 Getter Parameter Value for businessPartnerId	*/
	protected int getBusinessPartnerId() {
		return businessPartnerId;
	}

	/**	 Getter Parameter Value for chargeId	*/
	protected int getChargeId() {
		return chargeId;
	}

	/**	 Getter Parameter Value for currencyTypeId	*/
	protected int getCurrencyTypeId() {
		return currencyTypeId;
	}

	/**	 Getter Parameter Value for organizationId	*/
	protected int getOrganizationId() {
		return organizationId;
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