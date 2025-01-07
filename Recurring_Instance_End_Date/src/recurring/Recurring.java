package recurring;

import com.crystaldecisions.sdk.occa.infostore.*;
import au.com.bytecode.opencsv.CSVWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import com.crystaldecisions.sdk.plugin.desktop.folder.IFolder;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.properties.*;

public class Recurring {

	public static void main(String[] args) throws Exception {

        //Setting up the .properties file
		Properties properties = new Properties();
		FileInputStream CMSfile;
		String CMSpath = "C:\\Recurring_Instances_End_Date\\Recurring_Instances_End_Date.properties";
		CMSfile = new FileInputStream(CMSpath);
		properties.load(CMSfile);

		// Recurring Instances End Date .csv file creation
		File file = new File("C:\\Recurring_Instances_End_Date\\Recurring_Instances_End_Date.csv");
		FileWriter outputfile = new FileWriter(file);
		CSVWriter writer = new CSVWriter(outputfile);
		String[] header = { "Folder Path", "Report ID", "Report Name", "Recurring Job ID", "Recurring Job Name",
				"Job Kind", "Expiration Date for Recurring Job", "Owner" };
		writer.writeNext(header);

		// Setting up string variables for .properties file
		String User = properties.getProperty("User");
		String Password = properties.getProperty("Password");
		String CmsName = properties.getProperty("CmsName");
		String AuthType = properties.getProperty("AuthType");
		String RootFolderID = properties.getProperty("RootFolderID");
		String Starting_EndTime = properties.getProperty("Starting_EndTime");
		String Ending_EndTime = properties.getProperty("Ending_EndTime");
		//String ExpiryDate = properties.getProperty("ExpiryDate");

		// Declare variables
		IInfoStore boInfoStore = null;
		IEnterpriseSession boEnterpriseSession = null;

		try {
			boEnterpriseSession = CrystalEnterprise.getSessionMgr().logon(User, Password, CmsName, AuthType);
			boInfoStore = (IInfoStore) boEnterpriseSession.getService("", "InfoStore");
		} catch (Exception e) {
			System.out.println("Error logging onto BI: " + e);
		}

        //Program Begins
		System.out.println("=========================================================================");
		System.out.println("Job for Expiring Recurring Jobs has started ...");
		// format time
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String formatDateTime = now.format(formatter);
		System.out.println("Time: " + formatDateTime);
		System.out.println("=========================================================================");

		IInfoObjects boInfoObjects = null;
		boInfoObjects = (IInfoObjects) boInfoStore.query(
				"SELECT TOP 100000 SI_NAME, SI_CUID, SI_ID, SI_OWNER, SI_KIND, SI_UPDATE_TS, SI_CREATION_TIME, SI_PARENT_FOLDER, SI_SCHEDULEINFO.SI_ENDTIME, SI_SCHEDULEINFO.SI_PROGID_SCHEDULE, SI_SCHEDULE_STATUS FROM CI_INFOOBJECTS WHERE SI_RECURRING=1 AND SI_SCHEDULE_STATUS=9 AND SI_ANCESTOR = '"
						+ RootFolderID+ "'AND SI_SCHEDULEINFO.SI_ENDTIME BETWEEN '" + Starting_EndTime + "' AND '" + Ending_EndTime + " ' " );
		

		// If you need a different type of query we are saving this here
		// "SELECT SI_ID, SI_NAME, SI_KIND, SI_NEXTRUNTIME, SI_SCHEDULEINFO, SI_OWNER
		// FROM CI_INFOOBJECTS WHERE SI_KIND = 'WEBI' AND SI_RECURRING = 1 AND
		// SI_ANCESTOR = '"
		// + RootFolderID + "' AND SI_SCHEDULEINFO.SI_ENDTIME BETWEEN '" +
		// Starting_EndTime + "' AND '"
		// + Ending_EndTime + "'");

		int expiringRecurringJobsPulled = boInfoObjects.size();
		for (int x = 0; x < expiringRecurringJobsPulled; x++) {

			try {

				IInfoObject data = (IInfoObject) boInfoObjects.get(x);

				// get report id
				int reportid = data.getParentID();
				String ReportID = String.valueOf(reportid);

				// get report name
				IInfoObjects GetReportName = null;
				GetReportName = boInfoStore
						.query("SELECT SI_NAME, SI_KIND, SI_PARENT_FOLDER FROM CI_INFOOBJECTS WHERE SI_ID = '" + ReportID + "' ");
				
				String ReportName = null;
				int ParentFolder = 0;
				int ReportPulled = GetReportName.size();
				for (int y = 0; y < ReportPulled; y++) {

					try {
				
				IInfoObject rn = (IInfoObject) GetReportName.get(y);
				ReportName = rn.getTitle();
				ParentFolder = rn.getParentID();
				
					}catch (Exception ReportNameError) {
						System.out.println(ReportNameError);
					{
						
					}
					}
				}
				// get recurring info
				String RecurringJobName = data.getTitle();
				int recurringjobid = data.getID();
				String RecurringJobID = String.valueOf(recurringjobid);

				// get scheduling info
				ISchedulingInfo SchedulingInfo = data.getSchedulingInfo();
				Date expirydate = SchedulingInfo.getEndDate();
				String ExpiryDate2 = expirydate.toString();

				// get kind
				String Kind = data.getKind();

				// Owner
				String Owner = data.getOwner();
				
				//Last Modified
				Date LastModified = data.getUpdateTimeStamp();
				
				
				// Get Folder Path
				IInfoObjects folder = null;
				folder = (IInfoObjects) boInfoStore.query(
						"select si_id,si_name,si_parentid,si_path from ci_infoobjects where si_id=" + ParentFolder);

				IInfoObject ifolder = (IInfoObject) folder.get(0);

				if (ifolder.getKind().equals("Folder")) {
					String finalFolderPath = "/";
					IFolder iifolder = (IFolder) ifolder;
					if (iifolder.getPath() != null) {
						String path[] = iifolder.getPath();
						for (int fi = path.length; fi-- > 0; ){
							finalFolderPath = finalFolderPath + path[fi] + "/";
						}
						finalFolderPath = finalFolderPath + iifolder.getTitle();
					} else {
						finalFolderPath = finalFolderPath + iifolder.getTitle();
					}

					// add data to csv
					String[] data1 = { finalFolderPath, ReportID, ReportName, RecurringJobID, RecurringJobName, Kind,
							ExpiryDate2, Owner };
					writer.writeNext(data1);

					writer.flush();

					System.out.println(finalFolderPath + " - " + ReportID + " - " + ReportName + " - " + RecurringJobID
							+ " - " + RecurringJobName + " - " + Kind + " -  " + ExpiryDate2 + " - " + Owner + " - "+LastModified);
				} else if (ifolder.getKind().equals("ObjectPackage")) {
					String ObjectPath = "/" + ifolder.getTitle() + "(ObjectPackage)/";

					IProperties prop1 = ifolder.properties();
					IProperty getProp1 = prop1.getProperty("SI_PARENTID");
					String parentid = getProp1.toString();
					IInfoObjects parentfolder = boInfoStore
							.query("select * from ci_infoobjects where si_id=" + parentid);
					IInfoObject iparentfolder = (IInfoObject) parentfolder.get(0);

					if (iparentfolder.getKind().equals("Folder")) {
						IFolder folders = (IFolder) iparentfolder;
						if (folders.getPath() != null) {
							String Opath[] = folders.getPath();
							for (int f1 = Opath.length; f1-- > 0; ){
								ObjectPath = ObjectPath + Opath[f1] + "/";
							}
							ObjectPath = ObjectPath + folders.getTitle();
						} else {
							ObjectPath = ObjectPath + folders.getTitle();
						}
					}
						// add data to csv
						String[] data1 = { ObjectPath, ReportID, ReportName, RecurringJobID, RecurringJobName, Kind,
								ExpiryDate2, Owner };
						writer.writeNext(data1);

						writer.flush();

						System.out.println(ObjectPath + " - " + ReportID + " - " + ReportName + " - " + RecurringJobID
								+ " - " + RecurringJobName + " - " + Kind + " - " + ExpiryDate2 + " - " + Owner);
					}
				
		}

			catch (Exception ErrorLoop) {
				System.out.println("Error with Webi Loop: " + ErrorLoop.getMessage());
			}
	}

		System.out.println("=========================================================================");
		System.out.println("Job for Expiring Recurring Jobs has completed ...");
		// format time
		LocalDateTime now5 = LocalDateTime.now();
		DateTimeFormatter formatter5 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String formatDateTime5 = now5.format(formatter5);
		System.out.println("Time: " + formatDateTime5);
		System.out.println("=========================================================================");

	}
}
