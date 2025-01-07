package getuserinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.FileWriter;

import com.crystaldecisions.sdk.framework.*;
import com.crystaldecisions.sdk.occa.infostore.*;
import com.crystaldecisions.sdk.plugin.desktop.user.*;
import com.crystaldecisions.sdk.properties.IProperties;
import com.crystaldecisions.sdk.properties.IProperty;

import au.com.bytecode.opencsv.CSVWriter;

public class getuserinfo {

	public static void main(String[] args) throws Exception {

		// ONLY SECTION TO UPDATE FOR NEW SYSTEMS
				// NEED TO UPDATE THE 2 PATHS FOR C:
		File file = new File("C:\\UserInfo\\UserInfo.csv");
		// create FileWriter object with file as parameter
		FileWriter outputfile = new FileWriter(file);
		// create CSVWriter object filewriter object as parameter
		CSVWriter writer = new CSVWriter(outputfile);
		// adding header to csv
		String[] header = { "USER ID", "USER NAME", "FULL NAME","USER AGENCY","USER DEPT", "DESCRIPTION", "EMAIL ADDRESS", "CREATE TIME" };
		writer.writeNext(header);

				
			// Set the logon information
			String boUser = "Administrator";
			String boPassword = "Dream43prod!";
			String boCmsName = "ent1vp-apbi013";
			String boAuthType = "secEnterprise";

			// Declare Variables
			IInfoStore boInfoStore = null;
			IEnterpriseSession boEnterpriseSession = null;
			IInfoObjects boInfoObjects = null;

			try {

				// Retrieve the user
				boEnterpriseSession = CrystalEnterprise.getSessionMgr().logon(boUser, boPassword, boCmsName,
						boAuthType);
				boInfoStore = (IInfoStore) boEnterpriseSession.getService("", "InfoStore");
				boInfoObjects = (IInfoObjects) boInfoStore.query(
						"SELECT TOP 10000 SI_ID, SI_NAME, SI_ALIASES, SI_USERFULLNAME, SI_DESCRIPTION, SI_EMAIL_ADDRESS, SI_UPDATE_TS, SI_LASTLOGONTIME, SI_CREATION_TIME, SI_CUSTOM_MAPPED_ATTRIBUTES FROM CI_SYSTEMOBJECTS WHERE SI_KIND = 'USER' ORDER BY SI_ID ASC");

				
				
				int UsersSelectedfromCMS = boInfoObjects.size();
				int UserAliasesCounter = 0;
				int UserCount = 0;
				

				
				for (int i = 0; i < UsersSelectedfromCMS; i++) {
					IUser boUserAccount = (IUser) boInfoObjects.get(i);

					try {
						int UserID = boUserAccount.getID();
						String USERID = Integer.toString(UserID);
						
						String UserName = boUserAccount.getTitle();
						
						String FullName = boUserAccount.getFullName();
						
						String Desc = boUserAccount.getDescription();
						
						String EmailAddress = boUserAccount.getEmailAddress();
						
						com.crystaldecisions.sdk.properties.IProperty usercreatedate = boUserAccount.properties()
								.getProperty("SI_CREATION_TIME");
						java.util.Date UserCreatedTime = (java.util.Date) ((com.crystaldecisions.sdk.properties.IProperty) usercreatedate)
								.getValue();
						SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-YY hh.mm.ss.SSSSSSSSS");
						String USERCREATEDTIME = sdf.format(UserCreatedTime);
						
						IMappedAttributes UserDepartment = boUserAccount.getCustomMappedAttributes();
						String UserDept = UserDepartment.getAttribute("SI_USERDEPARTMENT");
						String UserAgency = UserDepartment.getAttribute("SI_USERAGENCY");
						
						
									
					// add data to csv
						String[] userdata = {USERID, UserName, FullName, UserAgency, UserDept, Desc, EmailAddress, USERCREATEDTIME};
						writer.writeNext(userdata);
							
					
					
					System.out.println(USERID+ " - " + UserName + " - " + FullName + " - "+ UserAgency + " - " + UserDept + " - " + Desc + " - " + EmailAddress + " - " + USERCREATEDTIME);
				} catch (Exception e) {
					System.out.println("Error - " + e.getMessage());
				}
				}
			} catch (Exception d) {
				System.out.print("Error - " + d.getMessage() + " / ");
			}
			writer.close();
			boEnterpriseSession.logoff();
			System.out.println("The job is finished");
			}
		}
	
