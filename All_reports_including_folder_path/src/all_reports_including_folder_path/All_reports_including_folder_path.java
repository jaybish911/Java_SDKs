package all_reports_including_folder_path;

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

public class All_reports_including_folder_path {

	public static void main(String[] args) throws Exception {

        //Setting up the .properties file
		Properties properties = new Properties();
		FileInputStream CMSfile;
		String CMSpath = "C:\\All_reports_including_folder_path\\All_reports_inlcuding_folder_path.properties";
		CMSfile = new FileInputStream(CMSpath);
		properties.load(CMSfile);

		// Recurring Instances End Date .csv file creation
		File file = new File("C:\\All_reports_including_folder_path\\All_reports_inlcuding_folder_path.csv");
		FileWriter outputfile = new FileWriter(file);
		CSVWriter writer = new CSVWriter(outputfile);
		String[] header = { "Folder Path", "Report ID", "Report Name", "Doc Kind", "Owner" };
		writer.writeNext(header);

		// Setting up string variables for .properties file
		String User = properties.getProperty("User");
		String Password = properties.getProperty("Password");
		String CmsName = properties.getProperty("CmsName");
		String AuthType = properties.getProperty("AuthType");
		

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
		System.out.println("Job for All reports including folder path has started ...");
		// format time
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String formatDateTime = now.format(formatter);
		System.out.println("Time: " + formatDateTime);
		System.out.println("=========================================================================");

		IInfoObjects boInfoObjects = null;
		boInfoObjects = (IInfoObjects) boInfoStore.query("Select TOP 100000 SI_ID, SI_NAME, SI_KIND, SI_OWNER, SI_PARENT_FOLDER, SI_FILES FROM CI_INFOOBJECTS WHERE SI_INSTANCE = 0 AND SI_ANCESTOR = 13435");
		

		int BOE_infostore_data = boInfoObjects.size();		
		for (int x = 0; x < BOE_infostore_data; x++) {

			try {

				IInfoObject data = (IInfoObject) boInfoObjects.get(x);

				// get id
				int reportid = data.getID();
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
				
				// get kind
				String Kind = data.getKind();

				// Owner
				String Owner = data.getOwner();	
				
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
					String[] data1 = { finalFolderPath, ReportID, ReportName, Kind,
							Owner };
					writer.writeNext(data1);

					writer.flush();

					System.out.println(finalFolderPath + " - " + ReportID + " - " + ReportName + " - " + Kind + " -  " + " - " + Owner);
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
						String[] data1 = { ObjectPath, ReportID, ReportName, Kind, Owner };
						writer.writeNext(data1);

						writer.flush();

						System.out.println(ObjectPath + " - " + ReportID + " - " + ReportName + " - " + Kind + " - " +Owner);
					}
				
		}

			catch (Exception ErrorLoop) {
				System.out.println("Error with Webi Loop: " + ErrorLoop.getMessage());
			}
	}

		System.out.println("=========================================================================");
		System.out.println("Job for All reports including folder path has completed ...");
		// format time
		LocalDateTime now5 = LocalDateTime.now();
		DateTimeFormatter formatter5 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String formatDateTime5 = now5.format(formatter5);
		System.out.println("Time: " + formatDateTime5);
		System.out.println("=========================================================================");

	}
}
