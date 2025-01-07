package setpassword4rootagency;

import java.io.BufferedReader;
import java.io.FileReader;
import com.crystaldecisions.sdk.framework.*;
import com.crystaldecisions.sdk.occa.infostore.*;
import com.crystaldecisions.sdk.plugin.desktop.user.*;
import com.crystaldecisions.sdk.properties.IProperties;
import com.crystaldecisions.sdk.properties.IProperty;



public class setpassword4rootagency {

	public static void main(String[] args) throws Exception {

		// Set the logon information
		String boUser = "Administrator";
		String boPassword = "Dream42test!";
		String boCmsName = "fac1vt-ap001";
		String boAuthType = "secEnterprise";

		// Declare Variables
		IInfoStore boInfoStore = null;
		IEnterpriseSession boEnterpriseSession = null;
		IInfoObjects boInfoObjects = null;

		try {

			
			// Retrieve the user
			boEnterpriseSession = CrystalEnterprise.getSessionMgr().logon(boUser, boPassword, boCmsName, boAuthType);
			boInfoStore = (IInfoStore) boEnterpriseSession.getService("", "InfoStore");
			boInfoObjects = boInfoStore.query(
					"SELECT SI_ID, SI_NAME, SI_KIND, SI_USERGROUPS,SI_ALIASES FROM CI_SYSTEMOBJECTS WHERE DESCENDANTS(\"SI_NAME='USERGROUP-USER'\",\"SI_NAME='KBUD Agency Budget Preparation Basic Users'\")");

			System.out.println("Logged in and retrieved users from BI......");

		} catch (Exception a) {
			System.out.println("Error with retrieving the user: " + a);
		}
		int UsersPulled = boInfoObjects.size();

		for (int y = 0; y < UsersPulled; y++) {

			try {
				IUser user = (IUser) boInfoObjects.get(y);
				int userID = user.getID();
				String UserID = String.valueOf(userID);

				String UserName = user.getTitle();	

				user.setNewPassword("BudgetPrep@2224");
				user.setPasswordExpiryAllowed(true);
				
				boInfoStore.commit(boInfoObjects);

				System.out.println("Set non-expiring password for - " + UserName + " - " + UserID);

			} catch (Exception e) {
				System.out.println("Error with setting password loop: " + e);
			}
		}
	}
}
