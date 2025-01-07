package ChangeAccountName;

import java.io.BufferedReader;
import java.io.FileReader;
import com.crystaldecisions.sdk.framework.*;
import com.crystaldecisions.sdk.occa.infostore.*;
import com.crystaldecisions.sdk.plugin.desktop.user.*;
import com.crystaldecisions.sdk.properties.IProperties;
import com.crystaldecisions.sdk.properties.IProperty;

public class changeaccountname {

	public static void main(String[] args) throws Exception {
		String file = "C:\\ChangeUserName.csv";

		BufferedReader br = new BufferedReader(new FileReader(file));
		String strLine = "";
		String Splitby = ",";

		while ((strLine = br.readLine()) != null) {
			String[] enterprise = strLine.split(Splitby);
			String enterpriseUser = enterprise[0].toString();
			String new_name = enterprise[1].toString();
			
					
			// Set the logon information
			String boUser = "Administrator";
			String boPassword = "Dream43prod!";
			String boCmsName = "ENT1VP-APBI013";
			String boAuthType = "secEnterprise";

			// Declare Variables
			IInfoStore boInfoStore = null;
			IEnterpriseSession boEnterpriseSession = null;

			try {

				// Retrieve the user
				boEnterpriseSession = CrystalEnterprise.getSessionMgr().logon(boUser, boPassword, boCmsName,
						boAuthType);
				boInfoStore = (IInfoStore) boEnterpriseSession.getService("", "InfoStore");
				IInfoObjects boInfoObjects = boInfoStore
						.query("Select TOP 5000 * from CI_SYSTEMOBJECTS where SI_KIND = 'User' and SI_NAME = '"
								+ enterpriseUser + "'");
				try {
					// Change User Name
					IUser EnterpriseUser = (IUser) boInfoObjects.get(0);
					EnterpriseUser.setTitle(new_name);
					
					boInfoStore.commit(boInfoObjects);
					
					System.out.println(enterpriseUser + " - " + new_name);
				} catch (Exception e) {
					System.out.println("Error - " + enterpriseUser + " - " + e.getMessage());
				}
			
			} catch (Exception e) {
				System.out.print("Failure retrieving objects from BO " + e.getMessage() + " / ");
			}
			boEnterpriseSession.logoff();
		}
		System.out.println("The job is finished");
	}
}