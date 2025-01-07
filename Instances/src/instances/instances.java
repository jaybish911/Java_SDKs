package instances;

import com.businessobjects.sdk.plugin.desktop.webi.IWebi;
import com.crystaldecisions.sdk.occa.infostore.*;
import au.com.bytecode.opencsv.CSVWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import com.crystaldecisions.sdk.plugin.desktop.report.IReport;
import com.crystaldecisions.sdk.plugin.desktop.folder.IFolder;
import com.crystaldecisions.sdk.plugin.desktop.objectpackage.IObjectPackage;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.properties.*;
import com.crystaldecisions.sdk.plugin.desktop.*;

public class instances {

	public static void main(String[] args) throws Exception {

//Setting up the .properties file
		Properties properties = new Properties();
		FileInputStream CMSfile;
		String CMSpath = "C:\\Instances\\Instances.properties";
		CMSfile = new FileInputStream(CMSpath);
		properties.load(CMSfile);

		// .csv file creation
		File webifile = new File("C:\\Instances\\Instances.csv");
		FileWriter outputfile = new FileWriter(webifile);
		CSVWriter writer = new CSVWriter(outputfile);
		String[] header = { "Folder Path", "Report Name", "Report ID", "Report CUID", "Report Kind" , "Instance ID", "Instance_CUID", "Instance Name", "Instance Kind", "Instance Update TS"  };
		writer.writeNext(header);

		// Setting up string variables for .properties file
		String User = properties.getProperty("User");
		String Password = properties.getProperty("Password");
		String CmsName = properties.getProperty("CmsName");
		String AuthType = properties.getProperty("AuthType");
		String RootFolderID = properties.getProperty("RootFolderID");
		String EnterDateOfEarliestInstance = properties.getProperty("EnterDateOfEarliestInstance");

		// Declare variables
		IInfoStore boInfoStore = null;
		IInfoObjects boInfoObjects = null;
		IInfoObjects boInfoObjects2 = null;
		IEnterpriseSession boEnterpriseSession = null;

		try {
			boEnterpriseSession = CrystalEnterprise.getSessionMgr().logon(User, Password, CmsName, AuthType);
			boInfoStore = (IInfoStore) boEnterpriseSession.getService("", "InfoStore");
		} catch (Exception e) {
			System.out.println("Error logging onto BI: " + e);
		}

//Program Begins
		System.out.println("===================================================");
		System.out.println("Job for Instances has started ...");
		// format time
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String formatDateTime = now.format(formatter);

		System.out.println("Time: " + formatDateTime);
		System.out.println("===================================================");

		boInfoObjects = (IInfoObjects) boInfoStore.query(
				"SELECT TOP 100000 SI_ID, SI_CUID, SI_NAME, SI_KIND, SI_PARENT_FOLDER FROM CI_INFOOBJECTS WHERE SI_KIND != FOLDER AND SI_INSTANCE=0 AND SI_ANCESTOR='" + RootFolderID + "'");
		
		int ReportsPulled = boInfoObjects.size();
			
		for (int x = 0; x < ReportsPulled; x++) {

			try {
				IInfoObject ReportObject = (IInfoObject) boInfoObjects.get(x);
                
				// Get Report ID
				int reportid = ReportObject.getID();
				String ReportID = String.valueOf(reportid);
				
				//Get Report CUID
				String ReportCUID = ReportObject.getCUID();

				// Get Report Name
				String reportname = ReportObject.getTitle();
				String ReportName = reportname.toString();
				
				// Get Report Kind
				String reportkind = ReportObject.getKind();
				String ReportKind = reportkind.toString();
				

				// Get Instance Count
				boInfoObjects2 = boInfoStore
						.query("select TOP 100000 * from ci_infoobjects where si_instance=1 and si_parentid = '"
								+ ReportID + "' and SI_CREATION_TIME > '" + EnterDateOfEarliestInstance + "'");

				int instancesize = boInfoObjects2.size();
				String TotalNumberOfInstances = Integer.toString(instancesize);
				
				if(boInfoObjects2.size() == 0)

				  {
					// Get Folder ID
					int parentfolderID = ReportObject.getParentID();
					String ParentFolderID = String.valueOf(parentfolderID);

					// Get Folder Path

					IInfoObjects folder = null;
					folder = (IInfoObjects) boInfoStore.query(
							"select si_id,si_name,si_parentid,si_path from ci_infoobjects where si_id=" + ParentFolderID);

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
						String[] webidata = { finalFolderPath, ReportName, ReportID, ReportCUID, ReportKind, null, null, null, null };
						writer.writeNext(webidata);

						// flush webi writer
						writer.flush();

						// write to console
						System.out.println(finalFolderPath + " - " + ReportName + " - " + ReportID + " - " + ReportCUID + " - "+ReportKind);

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
						String[] webidata = { ObjectPath, ReportName, ReportID, ReportCUID, ReportKind, null, null, null, null };
						writer.writeNext(webidata);

						// flush webi writer
						writer.flush();

						// write to console
						System.out.println(ObjectPath + " - " + ReportName + " - " + ReportID + " - " + ReportCUID + " - " +ReportKind);
				  }
				  }
				 else
				 
				 {
				 
				  for(int j=0;j<boInfoObjects2.size();j++)
				 
				  {
				 
				   IInfoObject obj = (IInfoObject)boInfoObjects2.get(j);
				   
				 int instanceid = obj.getID();
				 String InstanceID = String.valueOf(instanceid);
				 String InstanceCUID = obj.getCUID();
				 String InstanceName =  obj.getTitle();
				 Date instanceupdateTS =  obj.getUpdateTimeStamp();
					SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-YY hh.mm.ss.SSSSSSSSS");
					String InstanceUpdateTS = sdf.format(instanceupdateTS);
				 String InstanceKind = obj.getKind();
				  
				// Get Folder ID
				int parentfolderID2 = ReportObject.getParentID();
				String ParentFolderID2 = String.valueOf(parentfolderID2);

				// Get Folder Path

				IInfoObjects folder2 = null;
				folder2 = (IInfoObjects) boInfoStore.query(
						"select si_id,si_name,si_parentid,si_path from ci_infoobjects where si_id=" + ParentFolderID2);

				IInfoObject ifolder2 = (IInfoObject) folder2.get(0);

				if (ifolder2.getKind().equals("Folder")) {
					String finalFolderPath2 = "/";
					IFolder iifolder2 = (IFolder) ifolder2;
					if (iifolder2.getPath() != null) {
						String path2[] = iifolder2.getPath();
						for (int fi = path2.length; fi-- > 0; ){
							finalFolderPath2 = finalFolderPath2 + path2[fi] +  "/";
						}
						finalFolderPath2 = finalFolderPath2 + iifolder2.getTitle();
					} else {
						finalFolderPath2 = finalFolderPath2 + iifolder2.getTitle();
					}

					// add data to csv
					String[] webidata = { finalFolderPath2, ReportName, ReportID, ReportCUID, ReportKind, InstanceID, InstanceCUID, InstanceName, InstanceKind, InstanceUpdateTS };
					writer.writeNext(webidata);

					// flush webi writer
					writer.flush();

					// write to console
					System.out.println(finalFolderPath2 + " - " + ReportName + " - " + ReportID + " - " + ReportCUID + " - " + ReportKind + " - " + InstanceID + " - " + InstanceCUID + " - " +InstanceName + " - " + InstanceKind + " - " + InstanceUpdateTS);

				} else if (ifolder2.getKind().equals("ObjectPackage")) {
					String ObjectPath2 = "/" + ifolder2.getTitle() + "(ObjectPackage)/";

					IProperties prop2 = ifolder2.properties();
					IProperty getProp2 = prop2.getProperty("SI_PARENTID");
					String parentid2 = getProp2.toString();
					IInfoObjects parentfolder2 = boInfoStore
							.query("select * from ci_infoobjects where si_id=" + parentid2);
					IInfoObject iparentfolder2 = (IInfoObject) parentfolder2.get(0);

					if (iparentfolder2.getKind().equals("Folder")) {
						IFolder folders2 = (IFolder) iparentfolder2;
						if (folders2.getPath() != null) {
							String Opath2[] = folders2.getPath();
							for (int f1 = 0; f1 < Opath2.length; f1++) {
								ObjectPath2 = ObjectPath2 + Opath2[f1] + "/";
							}
							ObjectPath2 = ObjectPath2 + folders2.getTitle() ;
						} else {
							ObjectPath2 = ObjectPath2 + folders2.getTitle();
						}
					}
					// add data to csv
					String[] webidata = { ObjectPath2, ReportName, ReportID, ReportCUID, ReportKind, InstanceID, InstanceCUID, InstanceName, InstanceKind, InstanceUpdateTS };
					writer.writeNext(webidata);

					// flush webi writer
					writer.flush();

					// write to console
					System.out.println(ObjectPath2 + " - " + ReportName + " - " + ReportID + " - " + ReportCUID + " - " +ReportKind +  " - " + InstanceID + " - " + InstanceCUID + " - " + InstanceName + " - " + InstanceKind + " - " + InstanceUpdateTS);
				}
				 
				  } 
				}
				  
			} catch (Exception g) {
				File ErrorLog = new File("C:\\Instances\\InstancesErrorLog.txt");

				if (ErrorLog.exists()) {
					ErrorLog.createNewFile();
				}
				PrintWriter outputStream = new PrintWriter(ErrorLog);
				outputStream.println(g.getMessage());
				outputStream.close();
			}
		}
		writer.close();

		System.out.println("=============================================================================");
		System.out.println("Job for Instances has completed please check C:\\Instances ...");
		// format time
		LocalDateTime now1 = LocalDateTime.now();
		DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String formatDateTime1 = now1.format(formatter1);

		System.out.println("Time: " + formatDateTime1);
		System.out.println("=============================================================================");

	}
}
