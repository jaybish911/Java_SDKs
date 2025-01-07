package groups;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import com.crystaldecisions.sdk.framework.*;
import com.crystaldecisions.sdk.occa.infostore.*;
import com.crystaldecisions.sdk.plugin.desktop.user.*;
import com.crystaldecisions.sdk.plugin.desktop.usergroup.IUserGroup;
import com.crystaldecisions.sdk.properties.IProperty;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import com.crystaldecisions.sdk.properties.IProperties;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.lang.Integer;

public class Groups {

	public static void Groups() throws Exception {

// ONLY SECTION TO UPDATE FOR NEW SYSTEMS
		// NEED TO UPDATE THE 2 PATHS FOR C:
		File groups_log = new File("C:\\Auditing_SDKs\\43ProdAuditSDK\\SDKlogs\\groups_log.txt");
		PrintWriter outputStream = new PrintWriter(groups_log);

		String AuditPropertiesFile = "C:\\Auditing_SDKs\\43ProdAuditSDK\\AuditProperties.Properties";

		System.out.println(
				new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> " + "Groups SDK has started.....");

// LOGON TO BO AND QUERY CMS
		IInfoStore boInfoStore = null;
		IInfoObjects boInfoObjects = null;
		IEnterpriseSession boEnterpriseSession = null;

		try {
			Properties CMSProperties = new Properties();
			FileInputStream CMSfile;
			CMSfile = new FileInputStream(AuditPropertiesFile);
			CMSProperties.load(CMSfile);

			String CMSboUser = CMSProperties.getProperty("boUser");
			String CMSboPassword = CMSProperties.getProperty("boPassword");
			String CMSServerName = CMSProperties.getProperty("boCmsName");
			String CMSboAuthType = CMSProperties.getProperty("boAuthType");

			boEnterpriseSession = CrystalEnterprise.getSessionMgr().logon(CMSboUser, CMSboPassword, CMSServerName,
					CMSboAuthType);

			boInfoStore = (IInfoStore) boEnterpriseSession.getService("", "InfoStore");

			// write to groups_log file
			outputStream.println(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> "
					+ "Successfully logged onto BI!");
			outputStream.flush();

		} catch (Exception x) {
			// write to log file
			outputStream.println(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> "
					+ "Error logging into SAP BI: " + x.getMessage());
			outputStream.flush();
		}

		try {
			boInfoObjects = (IInfoObjects) boInfoStore.query(
					"SELECT TOP 100000 SI_ID, SI_NAME, SI_SUBGROUPS, SI_GROUP_MEMBERS, SI_DESCRIPTION, SI_CREATION_TIME FROM CI_SYSTEMOBJECTS WHERE SI_KIND = 'USERGROUP' ORDER BY SI_ID ASC");

			// write to log file
			outputStream.println(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> "
					+ "Successful query ran against CMS");
			outputStream.flush();

		} catch (Exception x) {
			// write to log file
			outputStream.println(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> "
					+ "Query failed to run against CMS: " + x.getMessage());
			outputStream.flush();
		}

		int GroupsSelectedfromCMS = boInfoObjects.size();
		int GrpDetailsLoopCntr = 0;
		int SubGrpLoopCntr = 0;
		int GrpUserLoopCntr = 0;
		int GrpUserSuccessfulinsert = 0;
		int SubGrpSuccessfulinsert = 0;
		int GrpDetailSuccessfulinsert = 0;

// USER GROUP DETAILS MODULE

		ArrayList ArrayGrpIDs = new ArrayList();
		ArrayList ArrayGrpNames = new ArrayList();
		ArrayList ArrayGrpDescs = new ArrayList();
		ArrayList ArrayGrpCreateTimes = new ArrayList();
		ArrayList ArrayParentGrpIDs = new ArrayList();
		ArrayList ArrayParentGrpNames = new ArrayList();
		ArrayList ArraySubGrpIDs = new ArrayList();
		ArrayList ArraySubGrpNames = new ArrayList();
		ArrayList ArrayParentGrpIDforUsers = new ArrayList();
		ArrayList ArrayParentGrpNamesforUsers = new ArrayList();
		ArrayList ArrayUserIDs = new ArrayList();
		ArrayList ArrayUserNames = new ArrayList();

		for (int i = 0; i < GroupsSelectedfromCMS; i++) {
			IUserGroup boUserGroup = (IUserGroup) boInfoObjects.get(i);
			try {
				int GroupID = boUserGroup.getID();
				String GROUPID = Integer.toString(GroupID);
				ArrayGrpIDs.add(i, GROUPID);

				String GroupName = boUserGroup.getTitle();
				ArrayGrpNames.add(i, GroupName);

				String GroupDesc = boUserGroup.getDescription();
				ArrayGrpDescs.add(i, GroupDesc);

				IProperty GroupCreateTime = boUserGroup.properties().getProperty("SI_CREATION_TIME");
				java.util.Date GROUPCREATETIME = (java.util.Date) ((com.crystaldecisions.sdk.properties.IProperty) GroupCreateTime)
						.getValue();
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-YY hh.mm.ss.SSSSSSSSS");
				String GrpCreateTime = sdf.format(GROUPCREATETIME);
				ArrayGrpCreateTimes.add(i, GrpCreateTime);

			} catch (Exception x) {
				// write to log file
				outputStream.println(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> "
						+ "User Groups Details Module Failed: " + x.getMessage());
				outputStream.flush();

			}

		}
		// write to log file
		outputStream.println(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> "
				+ "User Groups Details Module completed sucessfully");
		outputStream.flush();
// SUB GROUPS MODULE

		for (int i = 0; i < GroupsSelectedfromCMS; i++) {
			IUserGroup boUserGroup = (IUserGroup) boInfoObjects.get(i);
			try {
				IProperties GetSubGrps = (IProperties) boUserGroup.properties().getProperties("SI_SUBGROUPS");
				if (GetSubGrps != null) {

					IProperty GetNumberOfSubGroups = GetSubGrps.getProperty("SI_TOTAL");
					String TotalSubGroups = GetNumberOfSubGroups.getValue().toString();
					int CounterForTotalSubGrps = Integer.parseInt(TotalSubGroups);
					for (int j = 1; j <= CounterForTotalSubGrps; j++) {

						IProperties GetAllSubGroupIDs = (IProperties) boUserGroup.properties()
								.getProperties("SI_SUBGROUPS");
						IProperty IteratedSubGrpID = (IProperty) GetSubGrps.getProperty(j);
						String SubGrpID = IteratedSubGrpID.getValue().toString();
						int SubGrpIDint = Integer.parseInt(SubGrpID);
						String SubGrpName = null;
						IInfoObjects boInfoObjects2 = null;
						boInfoObjects2 = (IInfoObjects) boInfoStore
								.query("SELECT SI_NAME FROM CI_SYSTEMOBJECTS WHERE SI_KIND = 'USERGROUP' and SI_ID = "
										+ SubGrpIDint);

						IUserGroup boUserGroup2 = (IUserGroup) boInfoObjects2.get(0);
						SubGrpName = boUserGroup2.getTitle();

						int ParentGroupID = boUserGroup.getID();
						String ParentGrpID = Integer.toString(ParentGroupID);
						String ParentGrpName = boUserGroup.getTitle();

						ArrayParentGrpIDs.add(SubGrpLoopCntr, ParentGrpID);
						ArrayParentGrpNames.add(SubGrpLoopCntr, ParentGrpName);
						ArraySubGrpIDs.add(SubGrpLoopCntr, SubGrpID);
						ArraySubGrpNames.add(SubGrpLoopCntr, SubGrpName);

						SubGrpLoopCntr++;
					}
				} else {
					int ParentGroupID = boUserGroup.getID();
					String ParentGrpID = Integer.toString(ParentGroupID);
					String ParentGrpName = boUserGroup.getTitle();

					ArrayParentGrpIDs.add(SubGrpLoopCntr, ParentGrpID);
					ArrayParentGrpNames.add(SubGrpLoopCntr, ParentGrpName);
					ArraySubGrpIDs.add(SubGrpLoopCntr, null);
					ArraySubGrpNames.add(SubGrpLoopCntr, null);

					SubGrpLoopCntr++;
				}

			} catch (Exception x) {
				// write to log file
				outputStream.println(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> "
						+ "Sub Groups Module Failed: " + x.getMessage());
				outputStream.flush();

			}
		}
		// write to log file
		outputStream.println(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> "
				+ "Sub Groups Module completed sucessfully");
		outputStream.flush();

// GROUP USERS MODULE

		for (int z = 0; z < GroupsSelectedfromCMS; z++) {
			IUserGroup boUserGroup2 = (IUserGroup) boInfoObjects.get(z);
			try {
				IProperties GetGrpMembers = (IProperties) boUserGroup2.properties().getProperties("SI_GROUP_MEMBERS");
				if (GetGrpMembers != null) {

					IProperty GetNumberOfMembers = GetGrpMembers.getProperty("SI_TOTAL");
					String TotalMembers = GetNumberOfMembers.getValue().toString();
					int TotalMembersInt = Integer.parseInt(TotalMembers);

					for (int k = 1; k <= TotalMembersInt; k++) {

						IProperty IteratedUserID = (IProperty) GetGrpMembers.getProperty(k);
						String UserID = IteratedUserID.getValue().toString();
						int UserIDint = Integer.parseInt(UserID);
						String UserName = null;

						try {
							IInfoObjects boInfoObjects3;
							boInfoObjects3 = (IInfoObjects) boInfoStore
									.query("SELECT SI_NAME FROM CI_SYSTEMOBJECTS WHERE SI_KIND = 'USER' AND SI_ID = "
											+ UserIDint);

							IUser boUserAccount = (IUser) boInfoObjects3.get(0);
							UserName = boUserAccount.getTitle();

						} catch (Exception b) {
						}
						String ParentGrpNameforUser = boUserGroup2.getTitle();
						int ParentGrpIDforUser = boUserGroup2.getID();
						String PARENTGRPIDFORUSER = Integer.toString(ParentGrpIDforUser);

						ArrayParentGrpIDforUsers.add(GrpUserLoopCntr, PARENTGRPIDFORUSER);
						ArrayParentGrpNamesforUsers.add(GrpUserLoopCntr, ParentGrpNameforUser);
						ArrayUserIDs.add(GrpUserLoopCntr, UserID);
						ArrayUserNames.add(GrpUserLoopCntr, UserName);

						GrpUserLoopCntr++;

					}
				} else {
					int ParentGrpIDforUserInt = boUserGroup2.getID();
					String ParentGrpIDforUsers = Integer.toString(ParentGrpIDforUserInt);
					String ParentGrpNameforUsers = boUserGroup2.getTitle();

					ArrayParentGrpIDforUsers.add(GrpUserLoopCntr, ParentGrpIDforUsers);
					ArrayParentGrpNamesforUsers.add(GrpUserLoopCntr, ParentGrpNameforUsers);
					ArrayUserIDs.add(GrpUserLoopCntr, null);
					ArrayUserNames.add(GrpUserLoopCntr, null);

					GrpUserLoopCntr++;

				}

			} catch (Exception x) {
				// write to log file
				outputStream.println(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> "
						+ "Group Users Module Failed: " + x.getMessage());
				outputStream.flush();

			}
		}
		// write to log file
		outputStream.println(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> "
				+ "Group Users Module completed sucessfully");
		outputStream.flush();
		boEnterpriseSession.logoff();

// LOAD DATABASE CREDENTIALS
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

//CLEAN UP DATABASE MODULE
		try {
			Statement deletestatement = conn.createStatement();
			String sql1 = "DELETE FROM SDK_GROUP_USERS";
			String sql2 = "DELETE FROM SDK_SUB_GROUPS";
			String sql3 = "DELETE FROM SDK_GROUP_DETAILS";

			int delsql1 = deletestatement.executeUpdate(sql1);
			int delsql2 = deletestatement.executeUpdate(sql2);
			int delsql3 = deletestatement.executeUpdate(sql3);

			deletestatement.close();

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

//INSERT GROUPS MODULE

		try {
			String query1 = "INSERT INTO SDK_GROUP_DETAILS(GROUP_ID, GROUP_NAME, GROUP_DESCRIPTION, GROUP_CREATED_DATE)VALUES(?,?,?,?)";
			String query2 = "INSERT INTO SDK_SUB_GROUPS(GROUP_ID, GROUP_NAME, SUB_GROUP_ID, SUB_GROUP_NAME)VALUES(?,?,?,?)";
			String query3 = "INSERT INTO SDK_GROUP_USERS(GROUP_ID, GROUP_NAME, USER_ID, USER_NAME)VALUES(?,?,?,?)";
			PreparedStatement st1 = conn.prepareStatement(query1);
			PreparedStatement st2 = conn.prepareStatement(query2);
			PreparedStatement st3 = conn.prepareStatement(query3);

			for (int u = 0; u < ArrayGrpIDs.size(); u++) {
				st1.setString(1, (String) ArrayGrpIDs.get(u));
				st1.setString(2, (String) ArrayGrpNames.get(u));
				st1.setString(3, (String) ArrayGrpDescs.get(u));
				st1.setString(4, (String) ArrayGrpCreateTimes.get(u));
				st1.executeUpdate();
				GrpDetailSuccessfulinsert++;
			}
			st1.close();

			for (int u = 0; u < ArraySubGrpIDs.size(); u++) {
				st2.setString(1, (String) ArrayParentGrpIDs.get(u));
				st2.setString(2, (String) ArrayParentGrpNames.get(u));
				st2.setString(3, (String) ArraySubGrpIDs.get(u));
				st2.setString(4, (String) ArraySubGrpNames.get(u));
				st2.executeUpdate();
				SubGrpSuccessfulinsert++;
			}
			st2.close();

			for (int u = 0; u < ArrayParentGrpIDforUsers.size(); u++) {
				st3.setString(1, (String) ArrayParentGrpIDforUsers.get(u));
				st3.setString(2, (String) ArrayParentGrpNamesforUsers.get(u));
				st3.setString(3, (String) ArrayUserIDs.get(u));
				st3.setString(4, (String) ArrayUserNames.get(u));
				st3.executeUpdate();
				GrpUserSuccessfulinsert++;
			}
			st3.close();
			conn.close();

		} catch (Exception x) {
			// write to log file
			outputStream.println(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> "
					+ "Insert Groups into the Audit database failed: " + x.getMessage());
			outputStream.flush();
		}
		// write to log file
		outputStream.println(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> "
				+ "Insert Groups into the Audit database completed successfully.");
		outputStream.flush();

		System.out.println(
				new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()) + "-> " + "Groups SDK has finished!");
		outputStream.close();
		outputStream.flush();
	}
}
