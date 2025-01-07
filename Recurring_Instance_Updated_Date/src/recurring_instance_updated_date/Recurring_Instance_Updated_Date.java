package recurring_instance_updated_date;

import com.crystaldecisions.sdk.occa.infostore.*;
import au.com.bytecode.opencsv.CSVWriter;
import java.time.format.DateTimeFormatter;
import java.time.*;
import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import com.crystaldecisions.sdk.plugin.desktop.folder.IFolder;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.properties.*;

public class Recurring_Instance_Updated_Date {

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
		String[] header = { "Folder Path", "Job Name", "Job ID", "Job CUID", "Job Updated Time Stamp", "Job Owner",
				"Job Kind", "Old Owner", "New Owner" };
		writer.writeNext(header);

		// Setting up string variables for .properties file
		String User = properties.getProperty("User");
		String Password = properties.getProperty("Password");
		String CmsName = properties.getProperty("CmsName");
		String AuthType = properties.getProperty("AuthType");
		String RootFolderID = properties.getProperty("RootFolderID");
		String ExpiryDate = properties.getProperty("ExpiryDate");

		// Declare variables
		IInfoStore boInfoStore = null;
		IEnterpriseSession boEnterpriseSession = null;
		// Date SetExpiredDate = 2033/01/01;

		try {
			boEnterpriseSession = CrystalEnterprise.getSessionMgr().logon(User, Password, CmsName, AuthType);
			boInfoStore = (IInfoStore) boEnterpriseSession.getService("", "InfoStore");
		} catch (Exception e) {
			System.out.println("Error logging onto BI: " + e);
		}

//Program Begins
		System.out.println("=========================================================================");
		System.out.println("Job for Expiring Recurring Instances has started ...");
		// format time
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String formatDateTime = now.format(formatter);
		System.out.println("Time: " + formatDateTime);
		System.out.println("=========================================================================");

		IInfoObjects boInfoObjects = null;
		//boInfoObjects = (IInfoObjects) boInfoStore.query(
				//"SELECT TOP 100000 SI_NAME, SI_CUID, SI_ID, SI_OWNER, SI_KIND, SI_UPDATE_TS, SI_CREATION_TIME, SI_PARENT_FOLDER, SI_SCHEDULEINFO.SI_ENDTIME, SI_SCHEDULEINFO.SI_PROGID_SCHEDULE FROM CI_INFOOBJECTS WHERE SI_RECURRING=1 AND SI_ANCESTOR = '"
						//+ RootFolderID + "'AND SI_SCHEDULEINFO.SI_ENDTIME > '" + ExpiryDate + "'");
		
		boInfoObjects = (IInfoObjects) boInfoStore.query(
		"SELECT TOP 100000 * FROM CI_INFOOBJECTS WHERE SI_RECURRING=1 AND SI_ANCESTOR = '" + RootFolderID + "'");
				

		int expiringinstancesPulled = boInfoObjects.size();
		for (int x = 0; x < expiringinstancesPulled; x++) {

			try {

				IInfoObject data = (IInfoObject) boInfoObjects.get(x);

				// Recurring Job Name
				String JobName = data.getTitle();

				// Recurring Job ID
				int jobid = data.getID();
				String JobID = Integer.toString(jobid);

				// Recurring Job CUID
				String JobCUID = data.getCUID();

				//// Recurring Job Parent Folder ID
				IProperties tester = data.properties();
				IProperty te = tester.getProperty("SI_PARENT_FOLDER");
				String JobParentID = te.toString();

				// Recurring Job Update TS
				Date updatets = data.getUpdateTimeStamp();
				String JobUpdateTS = updatets.toString();

				// Recurring Job Owner
				String JobOwner = data.getOwner();
			    
				int JobOwnerid = data.getOwnerID();
			    String JobOwnerID = Integer.toString(JobOwnerid);

				// Recurring Job Kind
				String JobKind = data.getKind();

				// Recurring Job Expiry Date
				//ISchedulingInfo schedulinginfo = data.getSchedulingInfo();
				//Date JobExpiryDate = schedulinginfo.getEndDate();
				//String ExpiryDateforJob = JobExpiryDate.toString();

				//String DateRep = null;
				//try {
					//Calendar cal = Calendar.getInstance();
					//cal.set(Calendar.YEAR, 2043);
					//cal.set(Calendar.MONTH, Calendar.JANUARY);
					//cal.set(Calendar.DAY_OF_MONTH, 1);
					//cal.set(Calendar.AM_PM, Calendar.AM);
					//cal.set(Calendar.HOUR, 12);
					//cal.set(Calendar.MINUTE, 0);
					//cal.set(Calendar.SECOND, 0);

					//Date dateRepresentation = cal.getTime();

					//IProperties tmpProps = data.getSchedulingInfo().properties();
					//tmpProps.setProperty("SI_ENDTIME", dateRepresentation);
					//DateRep = dateRepresentation.toString();
					//boInfoStore.commit(boInfoObjects);

				//} catch (Exception set) {
					//System.out.println("Error with setting endtime date: " + set.getMessage());
				//}
				
				// Change Owner
				// VERY IMPORTANT:  This section will change the owner_name and owner_id but the job still fails.  The new owner must go to the old job and use the 'replace' option to create a new job.
				int newowner = 8724;
				String NewOwner = Integer.toString(newowner);
				
				String NewOwnerName = "kathryn.gagel";
				
				IProperty oldOwner = data.properties().getProperty("SI_OWNERID");
				IProperty oldOwnerName = data.properties().getProperty("SI_OWNER");
				
				oldOwner.setValue(newowner);
				oldOwnerName.setValue(NewOwnerName);
				
				boInfoStore.commit(boInfoObjects);
				
				data.properties().setProperty(JobOwnerID, newowner);
				// Get Folder Path

				IInfoObjects folder = null;
				folder = (IInfoObjects) boInfoStore.query(
						"select si_id,si_name,si_parentid,si_path from ci_infoobjects where si_id=" + JobParentID);

				IInfoObject ifolder = (IInfoObject) folder.get(0);

				if (ifolder.getKind().equals("Folder")) {
					String finalFolderPath = "/";
					IFolder iifolder = (IFolder) ifolder;
					if (iifolder.getPath() != null) {
						String path[] = iifolder.getPath();
						for (int fi = path.length; fi-- > 0;) {
							finalFolderPath = finalFolderPath + path[fi] + "/";
						}
						finalFolderPath = finalFolderPath + iifolder.getTitle();
					} else {
						finalFolderPath = finalFolderPath + iifolder.getTitle();
					}

					// Add to CSV
					String[] data1 = { finalFolderPath, JobName, JobID, JobCUID, JobUpdateTS, JobOwner, JobKind,
							JobOwnerID, };
					writer.writeNext(data1);

					writer.flush();

					System.out.println(
							finalFolderPath + " - " + JobName + " - " + JobID + " - " + JobCUID + " - " + JobUpdateTS
									+ " - " + JobOwner + " - " + JobKind + " - " + JobOwnerID  +  " - " + NewOwner);
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
							for (int f1 = Opath.length; f1-- > 0;) {
								ObjectPath = ObjectPath + Opath[f1] + "/";
							}
							ObjectPath = ObjectPath + folders.getTitle();
						} else {
							ObjectPath = ObjectPath + folders.getTitle();
						}
					}
					// add data to csv
					String[] data1 = { ObjectPath, JobName, JobID, JobCUID, JobUpdateTS, JobOwner, JobKind,
							JobOwnerID, NewOwner };
					writer.writeNext(data1);

					writer.flush();

					System.out.println(
							ObjectPath + " - " + JobName + " - " + JobID + " - " + JobCUID + " - " + JobUpdateTS + " - "
									+ JobOwner + " - " + JobKind + " - " + JobOwnerID + " - " + NewOwner);
				}
			}

			catch (Exception Loop) {
				System.out.println("Error with loop: " + Loop.getMessage());
			}
		}
		System.out.println("=========================================================================");
		System.out.println("Jobs Expiring for Recurring Instances has completed ...");
		// format time
		LocalDateTime now5 = LocalDateTime.now();
		DateTimeFormatter formatter5 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String formatDateTime5 = now5.format(formatter5);
		System.out.println("Time: " + formatDateTime5);
		System.out.println("=========================================================================");

	}
}
