package users;

import com.crystaldecisions.sdk.framework.*;
import com.crystaldecisions.sdk.occa.infostore.*;
import com.crystaldecisions.sdk.plugin.desktop.user.*;

import java.util.Date;
import java.util.Iterator;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Properties;

public class Users {

	public static void Users() throws Exception {

		
// ONLY SECTION TO UPDATE FOR NEW SYSTEMS
		// NEED TO UPDATE THE 2 PATHS FOR C:
		File users_log = new File("C:\\Auditing_SDKs\\43ProdAuditSDK\\SDKlogs\\users_log.txt");
		PrintWriter outputStream = new PrintWriter(users_log);

		String AuditPropertiesFile = "C:\\Auditing_SDKs\\43ProdAuditSDK\\AuditProperties.Properties";

		System.out.println(
				new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> " + "Users SDK has started.....");

// LOGON TO BO AND QUERY CMS

		Properties CMSProperties = new Properties();
		FileInputStream CMSfile;
		CMSfile = new FileInputStream(AuditPropertiesFile);
		CMSProperties.load(CMSfile);

		String CMSboUser = CMSProperties.getProperty("boUser");
		String CMSboPassword = CMSProperties.getProperty("boPassword");
		String CMSServerName = CMSProperties.getProperty("boCmsName");
		String CMSboAuthType = CMSProperties.getProperty("boAuthType");

		IInfoStore boInfoStore = null;
		IInfoObjects boInfoObjects = null;
		IEnterpriseSession boEnterpriseSession = null;

		try {
			boEnterpriseSession = CrystalEnterprise.getSessionMgr().logon(CMSboUser, CMSboPassword, CMSServerName,
					CMSboAuthType);
			boInfoStore = (IInfoStore) boEnterpriseSession.getService("", "InfoStore");
			boInfoObjects = (IInfoObjects) boInfoStore.query(
					"SELECT TOP 10000 SI_ID, SI_NAME, SI_ALIASES, SI_USERFULLNAME, SI_DESCRIPTION, SI_EMAIL_ADDRESS, SI_UPDATE_TS, SI_LASTLOGONTIME, SI_CREATION_TIME, SI_CUSTOM_MAPPED_ATTRIBUTES FROM CI_SYSTEMOBJECTS WHERE SI_KIND = 'USER' ORDER BY SI_ID ASC");

		} catch (Exception x) {
			// write to log file
			outputStream.println(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> "
					+ "Error logging into SAP BI: " + x.getMessage());
			outputStream.flush();
		}

		// write to log file
		outputStream.println(
				new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> " + "Logged onto SAP BI!");
		outputStream.flush();

		int UsersSelectedfromCMS = boInfoObjects.size();
		int UserAliasesCounter = 0;
		int UserCount = 0;

// USER DETAILS MODULE
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

				// USER LAST LOGONTIME
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

			} catch (Exception x) {
				// write to log file
				outputStream.println(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> "
						+ "Error with the User Details Module: " + x.getMessage());
				outputStream.flush();
			}

// USER ALIASES MODULE

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
				
					boolean AliasStatus = boAlias.isDisabled();
					String AS = String.valueOf(AliasStatus);
					UserAliasStatuses[i] = AS;
					
					UserAliasesCounter++;
				}

			} catch (Exception x) {
				// write to log file
				outputStream.println(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> "
						+ "Error with the User Aliases Module: " + x.getMessage());
				outputStream.flush();
			}

		}
		// write to log file
		outputStream.println(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> "
				+ "User Details Module and User Alias Module has completed successfully!");
		outputStream.flush();

		boEnterpriseSession.logoff();

// CREATE DATABASE CONNECTION
		Connection conn = null;

		try {
			Properties DBProperties = new Properties();
			FileInputStream DBfile;
			DBfile = new FileInputStream(AuditPropertiesFile);
			DBProperties.load(DBfile);

			String DBurl = DBProperties.getProperty("url");
			String DBusername = DBProperties.getProperty("username");
			String DBpassword = DBProperties.getProperty("password");

			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(DBurl, DBusername, DBpassword);

		} catch (Exception x) {
			// write to log file
			outputStream.println(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> "
					+ "Failed to logon to Audit database: " + x.getMessage());
			outputStream.flush();
		}

		// write to log file
		outputStream.println(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> "
				+ "Successfully logged onto Audit database.");
		outputStream.flush();

//CLEANUP DATABASE
		try {
			Statement deletestatement = conn.createStatement();
			String sql1 = "DELETE FROM SDK_USER_ALIASES";
			String sql2 = "DELETE FROM SDK_USER_DETAILS";
			int delsql1 = deletestatement.executeUpdate(sql1);
			int delsql2 = deletestatement.executeUpdate(sql2);

		} catch (Exception x) {
			// write to log file
			outputStream.println(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> "
					+ "Failed to purge Audit database. " + x.getMessage());
			outputStream.flush();
		}

		// write to log file
		outputStream.println(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> "
				+ "Successfully purged Audit database.");
		outputStream.flush();

//INSERT USERS MODULE

		try {
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
			conn.close();

		} catch (Exception x) {
			// write to log file
			outputStream.println(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> "
					+ "Insert Users Module Failed: " + x.getMessage());
			outputStream.flush();
		}

		// write to log file
		outputStream.println(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> "
				+ "Insert Users into the Audit database completed successfully.");
		outputStream.flush();

		System.out.println(
				new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> " + "Users SDK has finished!");
		outputStream.close();
		outputStream.flush();
	}
}
