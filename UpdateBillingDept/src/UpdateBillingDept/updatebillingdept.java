package UpdateBillingDept;

import java.io.BufferedReader;
import java.io.FileReader;
import com.crystaldecisions.sdk.framework.*;
import com.crystaldecisions.sdk.occa.infostore.*;
import com.crystaldecisions.sdk.plugin.desktop.user.*;
import com.crystaldecisions.sdk.properties.IProperties;
import com.crystaldecisions.sdk.properties.IProperty;

public class updatebillingdept {

	public static void main(String[] args) throws Exception {
		String file = "C:\\UploadFile.csv";

		BufferedReader br = new BufferedReader(new FileReader(file));
		String strLine = "";
		String Splitby = ",";

		while ((strLine = br.readLine()) != null) {
			String[] enterprise = strLine.split(Splitby);
			String enterpriseUser = enterprise[0].toString();
			String fullname = enterprise[1].toString();
			String emarstemplate = enterprise[2].toString();
			String dept = enterprise[3].toString();

			// Set the logon information
			String boUser = "Administrator";
			String boPassword = "CGIAdmin41fedprod";
			String boCmsName = "ent1vp-apbi001";
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
					// Adding FullName, Agency, UserDepartment
					IUser EnterpriseUser = (IUser) boInfoObjects.get(0);
					EnterpriseUser.setFullName(fullname);
					EnterpriseUser.getCustomMappedAttributes().setAttribute("SI_USERAGENCY", emarstemplate);
					EnterpriseUser.getCustomMappedAttributes().setAttribute("SI_USERDEPARTMENT", dept);
					
					boInfoStore.commit(boInfoObjects);
					
					System.out.println(enterpriseUser + " - " + fullname + " - " + emarstemplate + " - "+ dept);
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