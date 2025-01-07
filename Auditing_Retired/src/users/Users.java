package users;

import com.crystaldecisions.sdk.framework.*;
import com.crystaldecisions.sdk.occa.infostore.*;
import com.crystaldecisions.sdk.plugin.desktop.user.*;
import java.util.Iterator;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Properties;

public class Users {

	public static void Users() throws Exception {

// LOGON TO CMS
// Load Property File
		Properties CMSProperties = new Properties();
		FileInputStream CMSfile;
// Path to Property File
		String CMSpath = "C:\\Auditing_SDKs\\43fedProdAuditSDKs\\AuditProperties.properties";
// Loading files
		CMSfile = new FileInputStream(CMSpath);
		CMSProperties.load(CMSfile);
		String CMSboUser = CMSProperties.getProperty("boUser");
		String CMSboPassword = CMSProperties.getProperty("boPassword");
		String CMSServerName = CMSProperties.getProperty("boCmsName");
		String CMSboAuthType = CMSProperties.getProperty("boAuthType");
		IInfoStore boInfoStore = null;
		IInfoObjects boInfoObjects = null;
		IEnterpriseSession boEnterpriseSession = null;
		boEnterpriseSession = CrystalEnterprise.getSessionMgr().logon(CMSboUser, CMSboPassword, CMSServerName,
				CMSboAuthType);
		boInfoStore = (IInfoStore) boEnterpriseSession.getService("", "InfoStore");
		boInfoObjects = (IInfoObjects) boInfoStore.query(
				"SELECT TOP 10000 SI_ID, SI_NAME, SI_ALIASES, SI_USERFULLNAME, SI_DESCRIPTION, SI_EMAIL_ADDRESS, SI_UPDATE_TS, SI_LASTLOGONTIME, SI_CREATION_TIME, SI_CUSTOM_MAPPED_ATTRIBUTES FROM CI_SYSTEMOBJECTS WHERE SI_KIND = 'USER' ORDER BY SI_ID ASC");
		int UsersSelectedfromCMS = boInfoObjects.size();
		int UserAliasesCounter = 0;
		int UserCount = 0;

		// SDK FOR USER_DETAILS
		String[] UserIDs = new String[UsersSelectedfromCMS];
		String[] UserNames = new String[UsersSelectedfromCMS];
		String[] FullNames = new String[UsersSelectedfromCMS];
		String[] UserDescriptions = new String[UsersSelectedfromCMS];
		String[] UserEmails = new String[UsersSelectedfromCMS];
		String[] UserCreatedTimes = new String[UsersSelectedfromCMS];
		String[] UserLastLogonTimes = new String[UsersSelectedfromCMS];
		String[] UserCustomorAddedDepts = new String[UsersSelectedfromCMS];
		String[] UserEmarsTemplates = new String[UsersSelectedfromCMS];
		String[] UserAliases = new String[UsersSelectedfromCMS];
		String[] UserAliasStatuses = new String[UsersSelectedfromCMS];
		String[] AliasIDAuthorization = new String[UsersSelectedfromCMS];
		String[] AliasNames = new String[UsersSelectedfromCMS];

		for (int i = 0; i < UsersSelectedfromCMS; i++) {
			IUser boUserAccount = (IUser) boInfoObjects.get(i);

			try {
				int UserID = boUserAccount.getID();
				String USERID = Integer.toString(UserID);
				UserIDs[i] = USERID;
				String UserName = boUserAccount.getTitle();
				UserNames[i] = UserName;
				String FullName = boUserAccount.getFullName();
				FullNames[i] = FullName;
				String Desc = boUserAccount.getDescription();
				UserDescriptions[i] = Desc;
				String EmailAddress = boUserAccount.getEmailAddress();
				UserEmails[i] = EmailAddress;
				com.crystaldecisions.sdk.properties.IProperty usercreatedate = boUserAccount.properties()
						.getProperty("SI_CREATION_TIME");
				java.util.Date UserCreatedTime = (java.util.Date) ((com.crystaldecisions.sdk.properties.IProperty) usercreatedate)
						.getValue();
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-YY hh.mm.ss.SSSSSSSSS");
				String USERCREATEDTIME = sdf.format(UserCreatedTime);
				UserCreatedTimes[i] = USERCREATEDTIME;
				
				//USER LAST LOGONTIME
				String USERLASTLOGINTIME = "";
				
				try {
					com.crystaldecisions.sdk.properties.IProperty lastlogon = boUserAccount.properties()
							.getProperty("SI_LASTLOGONTIME");
					java.util.Date UserLastLoginTime = (java.util.Date) ((com.crystaldecisions.sdk.properties.IProperty) lastlogon)
							.getValue();
					if (UserLastLoginTime != null) {
						SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MMM-YY hh.mm.ss.SSSSSSSSS");
						USERLASTLOGINTIME = sdf2.format(UserLastLoginTime);
						UserLastLogonTimes[i] = USERLASTLOGINTIME;
					} else {
						
						UserLastLogonTimes[i] = null;
					}
				} catch (NullPointerException e) {
					UserLastLogonTimes[i] = null;
				}
				
				IMappedAttributes UserDepartment = boUserAccount.getCustomMappedAttributes();
				String USERDEPT = UserDepartment.getAttribute("SI_USERDEPARTMENT");
				UserCustomorAddedDepts[i] = USERDEPT;

				IMappedAttributes UserEmarsTemplate = boUserAccount.getCustomMappedAttributes();
				String UserEmarsTemp = UserDepartment.getAttribute("SI_USERAGENCY");
				UserEmarsTemplates[i] = UserEmarsTemp;

				UserCount++;
				System.out.println("Loop on user detail record sucessful");
			} catch (Exception f) {
				System.out.println("Error with user details loop: " + f.getMessage());
			}
// SDK FOR USER_ALIASES

			try {
				int UserAccount = boUserAccount.getID();
				String UA = Integer.toString(UserAccount);
				UserAliases[i] = UA;

				for (Iterator boAliases = boUserAccount.getAliases().iterator(); boAliases.hasNext();) {
					IUserAlias boAlias = (IUserAlias) boAliases.next();
					String AliasAuth = boAlias.getAuthentication();
					String AliasID = boAlias.getID();

					String AliasIDAuth = AliasAuth.concat(AliasID);
					AliasIDAuthorization[i] = AliasIDAuth;

					String AliasName = boAlias.getName();
					AliasNames[i] = AliasName;

					String AS = "";
					UserAliasStatuses[i] = AS;

					boolean AliasStatus = boAlias.isDisabled();
					if (AliasStatus == true) {
						AS = "Disabled";
					} else {
						AS = "Enabled";
					}
					System.out.println("Loop on user aliases record successful");
					UserAliasesCounter++;
				}
			} catch (Exception a) {
				System.out.println("Error with user aliases loop: " + a.getMessage());
			}
			boEnterpriseSession.logoff();
		}
// CREATE DATABASE CONNECTION
		Properties DBProperties = new Properties();
		FileInputStream DBfile;
		String DBpath = "C:\\Auditing_SDKs\\43fedProdAuditSDKs\\AuditProperties.properties";
		DBfile = new FileInputStream(DBpath);
		DBProperties.load(DBfile);
		String DBurl = DBProperties.getProperty("url");
		String DBusername = DBProperties.getProperty("username");
		String DBpassword = DBProperties.getProperty("password");

//CLEANUP DATABASE
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection deleteconnection = DriverManager.getConnection(DBurl, DBusername, DBpassword);
			
			Statement deletestatement = deleteconnection.createStatement();
			String sql1 = "DELETE FROM SDK_USER_ALIASES";
			String sql2 = "DELETE FROM SDK_USER_DETAILS";
			int delsql1 = deletestatement.executeUpdate(sql1);
			int delsql2 = deletestatement.executeUpdate(sql2);

     		//LOG FILE FOR DATABASE CLEANUP
			System.out.println("Number of records deleted from SDK_USERS_ALIASES table: " + delsql1);
			System.out.println("Number of records deleted from SDK_USERS_DETAILS table: " + delsql2);
			File UserDeletedLog = new File("C:\\Auditing_SDKs\\43fedProdAuditSDKs\\SDKlogs\\UserDeletedLog.txt");
			if (UserDeletedLog.exists()) {
				UserDeletedLog.createNewFile();
			}
			PrintWriter outputStream = new PrintWriter(UserDeletedLog);
			outputStream.println("Records deleted from SDK_USER_ALIASES table: " + delsql1);
			outputStream.println("Records deleted from SDK_USER_DETAILS table: " + delsql2);
			outputStream.close();
			deletestatement.close();
			deleteconnection.close();

		} catch (Exception d) {
			System.out.println("Error with database clean up: ");
			System.out.println(d.getMessage());

			File UserDeletedLog = new File("C:\\Auditing_SDKs\\43fedProdAuditSDKs\\SDKlogs\\Database_Cleanup_Log.txt");

			if (UserDeletedLog.exists()) {
				UserDeletedLog.createNewFile();
			}
			PrintWriter outputStream = new PrintWriter(UserDeletedLog);
			outputStream.println("Error cleaning up database: " + d.getMessage());
			outputStream.close();
		}
//INSERTING RECORDS INTO DATABASE

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection conn = DriverManager.getConnection(DBurl, DBusername, DBpassword);
			String query = "INSERT INTO SDK_USER_DETAILS(USER_ID,USER_NAME,USER_FULL_NAME,USER_DESCRIPTION,USER_DEPARTMENT,USER_EMARS_TEMPLATE,USER_EMAIL,USER_CREATION_TIME,USER_LAST_LOGIN)VALUES(?,?,?,?,?,?,?,?,?)";
			String query2 = "INSERT INTO SDK_USER_ALIASES(USER_ID,ALIAS_ID, ALIAS_NAME, ALIAS_STATUS)VALUES(?,?,?,?)";
			PreparedStatement st = conn.prepareStatement(query);
			PreparedStatement st2 = conn.prepareStatement(query2);

			int UserDetailsSuccessfulInsert = 0;
			for (int u = 0; u < UsersSelectedfromCMS; u++) {
				st.setString(1, UserIDs[u]);
				st.setString(2, UserNames[u]);
				st.setString(3, FullNames[u]);
				st.setString(4, UserDescriptions[u]);
				st.setString(5, UserCustomorAddedDepts[u]);
				st.setString(6, UserEmarsTemplates[u]);
				st.setString(7, UserEmails[u]);
				st.setString(8, UserCreatedTimes[u]);
				st.setString(9, UserLastLogonTimes[u]);
				st.executeUpdate();
				UserDetailsSuccessfulInsert++;
			}
			st.close();

			int UserAliasesSuccessfulInsert = 0;
			for (int u = 0; u < UsersSelectedfromCMS; u++) {
				st2.setString(1, UserAliases[u]);
				st2.setString(2, AliasIDAuthorization[u]);
				st2.setString(3, AliasNames[u]);
				st2.setString(4, UserAliasStatuses[u]);
				st2.executeUpdate();
				UserAliasesSuccessfulInsert++;
			}
			st2.close();
			System.out.println("Users have been successfully inserted into the database");

// LOG FILE
			try {
				File UsersInsertedLog = new File("C:\\Auditing_SDKs\\43fedProdAuditSDKs\\SDKlogs\\UsersInsertedLog.txt");

				if (UsersInsertedLog.exists()) {
					UsersInsertedLog.createNewFile();
				}
				PrintWriter outputStream = new PrintWriter(UsersInsertedLog);
				outputStream.println("Users pulled from CMS: " + UserCount);
				outputStream.println("---------------------------------------------");
				outputStream.println("Total count of User Details loop: " + UserCount);
				outputStream.println("Records inserted SDK_USER_DETAILS: " + UserDetailsSuccessfulInsert);
				outputStream.println("---------------------------------------------");
				outputStream.println("Total count of User Aliases loop: " + UserAliasesCounter);
				outputStream.println("Records inserted into SDK_USER_ALIASES: " + UserAliasesSuccessfulInsert);
				outputStream.close();
				conn.close();

			} catch (Exception d) {
				System.out.println("Error counting new records in the database after SDK insert: ");
				System.out.println(d.getMessage());

				File UsersInsertedLog = new File("C:\\Auditing_SDKs\\43fedProdAuditSDKs\\SDKlogs\\UsersInsertedLog.txt");

				if (UsersInsertedLog.exists()) {
					UsersInsertedLog.createNewFile();
				}
				PrintWriter outputStream = new PrintWriter(UsersInsertedLog);
				outputStream.println("Error inserting records into the SDK_USER_ALIASES table: " + d.getMessage());
				outputStream.close();
				conn.close();
			}
		} catch (Exception r) {
			System.out.println("Error with inserting new records into database: ");
			System.out.println(r.getMessage());

			File UsersInsertedLog = new File("C:\\Auditing_SDKs\\43fedProdAuditSDKs\\SDKlogs\\UsersInsertedLog.txt");

			if (UsersInsertedLog.exists()) {
				UsersInsertedLog.createNewFile();
			}
			PrintWriter outputStream = new PrintWriter(UsersInsertedLog);
			outputStream.println("Error inserting records into the database: " + r.getMessage());
			outputStream.close();
		}
	}
}
