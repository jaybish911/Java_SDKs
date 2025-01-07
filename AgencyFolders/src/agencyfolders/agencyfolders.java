package agencyfolders;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import com.businessobjects.ds.excel.poi.XLSXWorkbook;
import com.crystaldecisions.sdk.framework.*;
import com.crystaldecisions.sdk.occa.infostore.*;
import com.crystaldecisions.sdk.plugin.desktop.user.*;
import com.crystaldecisions.sdk.plugin.desktop.usergroup.IUserGroup;
import com.crystaldecisions.sdk.properties.IProperty;
import au.com.bytecode.opencsv.CSVWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;
import com.crystaldecisions.sdk.properties.IProperties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.Integer;
import java.security.Principal;

import com.crystaldecisions.sdk.plugin.desktop.folder.*;

public class agencyfolders {

	public static void main(String[] args) throws Exception {

//LOGON TO BO AND QUERY CMS
		Properties CMSProperties = new Properties();
		FileInputStream CMSfile;

//DEFINE PATH TO PROPERTIES FILE
		String CMSpath = "C:\\AgencyFolders\\AgencyFolders.properties";

//LOAD FILE
		CMSfile = new FileInputStream(CMSpath);
		CMSProperties.load(CMSfile);

		String Test43User = CMSProperties.getProperty("Test43User");
		String Test43Password = CMSProperties.getProperty("Test43Password");
		String Test43CmsName = CMSProperties.getProperty("Test43CmsName");
		String Test43AuthType = CMSProperties.getProperty("Test43AuthType");
		String Test43ClusterName = CMSProperties.getProperty("Test43ClusterName");

		String UAT43User = CMSProperties.getProperty("UAT43User");
		String UAT43Password = CMSProperties.getProperty("UAT43Password");
		String UAT43CmsName = CMSProperties.getProperty("UAT43CmsName");
		String UAT43AuthType = CMSProperties.getProperty("UAT43AuthType");
		String UAT43ClusterName = CMSProperties.getProperty("UAT43ClusterName");

		String Prod43User = CMSProperties.getProperty("Prod43User");
		String Prod43Password = CMSProperties.getProperty("Prod43Password");
		String Prod43CmsName = CMSProperties.getProperty("Prod43CmsName");
		String Prod43AuthType = CMSProperties.getProperty("Prod43AuthType");
		String Prod43ClusterName = CMSProperties.getProperty("Prod43ClusterName");

		String Fed43User = CMSProperties.getProperty("Fed43User");
		String Fed43Password = CMSProperties.getProperty("Fed43Password");
		String Fed43CmsName = CMSProperties.getProperty("Fed43CmsName");
		String Fed43AuthType = CMSProperties.getProperty("Fed43AuthType");
		String Fed43ClusterName = CMSProperties.getProperty("Fed43ClusterName");

		String Fed43FTUser = CMSProperties.getProperty("Fed43FTUser");
		String Fed43FTPassword = CMSProperties.getProperty("Fed43FTPassword");
		String Fed43FTCmsName = CMSProperties.getProperty("Fed43FTCmsName");
		String Fed43FTAuthType = CMSProperties.getProperty("Fed43FTAuthType");
		String Fed43FTClusterName = CMSProperties.getProperty("Fed43FTClusterName");

		String Fed43FUUser = CMSProperties.getProperty("Fed43FUUser");
		String Fed43FUPassword = CMSProperties.getProperty("Fed43FUPassword");
		String Fed43FUCmsName = CMSProperties.getProperty("Fed43FUCmsName");
		String Fed43FUAuthType = CMSProperties.getProperty("Fed43FUAuthType");
		String Fed43FUClusterName = CMSProperties.getProperty("Fed43FUClusterName");

		IInfoStore boInfoStore = null;
		IInfoObjects boInfoObjects = null;
		IEnterpriseSession boEnterpriseSession = null;

		File file = new File("C:\\AgencyFolders\\AgencyFolders.csv");
		// create FileWriter object with file as parameter
		FileWriter outputfile = new FileWriter(file);
		// create CSVWriter object filewriter object as parameter
		CSVWriter writer = new CSVWriter(outputfile);
		// adding header to csv
		String[] header = { "Environment", "Agency Folder", "Folder Creation Time" };
		writer.writeNext(header);

// AWS 43 Fed Test
		boEnterpriseSession = CrystalEnterprise.getSessionMgr().logon(Fed43FTUser, Fed43FTPassword, Fed43FTCmsName,
				Fed43FTAuthType);
		boInfoStore = (IInfoStore) boEnterpriseSession.getService("", "InfoStore");
		boInfoObjects = (IInfoObjects) boInfoStore.query(
				"SELECT * FROM CI_INFOOBJECTS WHERE SI_PARENTID=0 AND SI_NAME!='User Folders' AND SI_NAME!='System Configuration Wizard' AND SI_NAME!= 'LCM' AND SI_NAME!= 'Probes' AND SI_NAME!= 'Data Federation' AND SI_NAME!= 'Report Conversion Tool' AND SI_NAME!= 'Visual Difference' AND SI_NAME!= 'Platfrom Search Scheduling' AND SI_NAME!= 'Web Intelligence Samples' AND SI_NAME!='Monitoring Report Sample' AND SI_NAME!='Report Samples' AND SI_NAME!='Platform Search Scheduling'");

		int AWS43FTfromCMS = boInfoObjects.size();
		for (int x = 0; x < AWS43FTfromCMS; x++) {

			try {
				IFolder boFolder = (IFolder) boInfoObjects.get(x);
				String AgencyFolder = boFolder.getTitle();
				String ClusterName = Fed43FTClusterName;
				com.crystaldecisions.sdk.properties.IProperty Created = boFolder.properties()
						.getProperty("SI_CREATION_TIME");
				java.util.Date FolderCreatedTime = (java.util.Date) ((com.crystaldecisions.sdk.properties.IProperty) Created)
						.getValue();
				SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MMM-dd");
				String FolderCreationTime = sdf.format(FolderCreatedTime);

				// add data to csv
				String[] aws43ftdata = { ClusterName, AgencyFolder, FolderCreationTime };
				writer.writeNext(aws43ftdata);

			} catch (Exception g) {

				File AWSEBI43FTErrorLog = new File("C:\\AgencyFolders\\AWSEBI43FTErrorLog.txt");

				if (AWSEBI43FTErrorLog.exists()) {
					AWSEBI43FTErrorLog.createNewFile();
				}
				PrintWriter outputStream = new PrintWriter(AWSEBI43FTErrorLog);
				outputStream.println(g.getMessage());
				outputStream.close();
			}
		}

//43 Test
		boEnterpriseSession = CrystalEnterprise.getSessionMgr().logon(Test43User, Test43Password, Test43CmsName,
				Test43AuthType);

		boInfoStore = (IInfoStore) boEnterpriseSession.getService("", "InfoStore");

		boInfoObjects = (IInfoObjects) boInfoStore.query(
				"SELECT * FROM CI_INFOOBJECTS WHERE SI_PARENTID=0 AND SI_NAME!='User Folders' AND SI_NAME!='System Configuration Wizard' AND SI_NAME!= 'LCM' AND SI_NAME!= 'Probes' AND SI_NAME!= 'Data Federation' AND SI_NAME!= 'Report Conversion Tool' AND SI_NAME!= 'Visual Difference' AND SI_NAME!= 'Platfrom Search Scheduling' AND SI_NAME!= 'Web Intelligence Samples' AND SI_NAME!='Monitoring Report Sample' AND SI_NAME!='Report Samples' AND SI_NAME!='Platform Search Scheduling'");

		int Test43FoldersSelectedfromCMS = boInfoObjects.size();
		for (int x = 0; x < Test43FoldersSelectedfromCMS; x++) {

			try {
				IFolder boFolder = (IFolder) boInfoObjects.get(x);
				String AgencyFolder = boFolder.getTitle();
				String ClusterName = Test43ClusterName;
				com.crystaldecisions.sdk.properties.IProperty Created = boFolder.properties()
						.getProperty("SI_CREATION_TIME");
				java.util.Date FolderCreatedTime = (java.util.Date) ((com.crystaldecisions.sdk.properties.IProperty) Created)
						.getValue();
				SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MMM-dd");
				String FolderCreationTime = sdf.format(FolderCreatedTime);

				// add data to csv
				String[] data1 = { ClusterName, AgencyFolder, FolderCreationTime };
				writer.writeNext(data1);

			} catch (Exception g) {

				File EBI43TESTErrorLog = new File("C:\\AgencyFolders\\EBI41FEDPRODErrorLog.txt");

				if (EBI43TESTErrorLog.exists()) {
					EBI43TESTErrorLog.createNewFile();
				}
				PrintWriter outputStream = new PrintWriter(EBI43TESTErrorLog);
				outputStream.println(g.getMessage());
				outputStream.close();
			}
		}

// 4.3 UAT
		boEnterpriseSession = CrystalEnterprise.getSessionMgr().logon(UAT43User, UAT43Password, UAT43CmsName,
				UAT43AuthType);

		boInfoStore = (IInfoStore) boEnterpriseSession.getService("", "InfoStore");

		boInfoObjects = (IInfoObjects) boInfoStore.query(
				"SELECT * FROM CI_INFOOBJECTS WHERE SI_PARENTID=0 AND SI_NAME!='User Folders' AND SI_NAME!='System Configuration Wizard' AND SI_NAME!= 'LCM' AND SI_NAME!= 'Probes' AND SI_NAME!= 'Data Federation' AND SI_NAME!= 'Report Conversion Tool' AND SI_NAME!= 'Visual Difference' AND SI_NAME!= 'Platfrom Search Scheduling' AND SI_NAME!= 'Web Intelligence Samples' AND SI_NAME!='Monitoring Report Sample' AND SI_NAME!='Report Samples' AND SI_NAME!='Platform Search Scheduling'");

		int UAT43FoldersSelectedfromCMS = boInfoObjects.size();
		for (int x = 0; x < UAT43FoldersSelectedfromCMS; x++) {

			try {
				IFolder boFolder = (IFolder) boInfoObjects.get(x);
				String AgencyFolder = boFolder.getTitle();
				String ClusterName = UAT43ClusterName;
				com.crystaldecisions.sdk.properties.IProperty Created = boFolder.properties()
						.getProperty("SI_CREATION_TIME");
				java.util.Date FolderCreatedTime = (java.util.Date) ((com.crystaldecisions.sdk.properties.IProperty) Created)
						.getValue();
				SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MMM-dd");
				String FolderCreationTime = sdf.format(FolderCreatedTime);

				// add data to csv
				String[] data1 = { ClusterName, AgencyFolder, FolderCreationTime };
				writer.writeNext(data1);

			} catch (Exception g) {

				File EBI43UATErrorLog = new File("C:\\AgencyFolders\\EBI43UATErrorLog.txt");

				if (EBI43UATErrorLog.exists()) {
					EBI43UATErrorLog.createNewFile();
				}
				PrintWriter outputStream = new PrintWriter(EBI43UATErrorLog);
				outputStream.println(g.getMessage());
				outputStream.close();
			}
		}

// 4.3 Production
		boEnterpriseSession = CrystalEnterprise.getSessionMgr().logon(Prod43User, Prod43Password, Prod43CmsName,
				Prod43AuthType);

		boInfoStore = (IInfoStore) boEnterpriseSession.getService("", "InfoStore");

		boInfoObjects = (IInfoObjects) boInfoStore.query(
				"SELECT * FROM CI_INFOOBJECTS WHERE SI_PARENTID=0 AND SI_NAME!='User Folders' AND SI_NAME!='System Configuration Wizard' AND SI_NAME!= 'LCM' AND SI_NAME!= 'Probes' AND SI_NAME!= 'Data Federation' AND SI_NAME!= 'Report Conversion Tool' AND SI_NAME!= 'Visual Difference' AND SI_NAME!= 'Platfrom Search Scheduling' AND SI_NAME!= 'Web Intelligence Samples' AND SI_NAME!='Monitoring Report Sample' AND SI_NAME!='Report Samples' AND SI_NAME!='Platform Search Scheduling'");

		int Prod43FoldersSelectedfromCMS = boInfoObjects.size();
		for (int x = 0; x < Prod43FoldersSelectedfromCMS; x++) {

			try {
				IFolder boFolder = (IFolder) boInfoObjects.get(x);
				String AgencyFolder = boFolder.getTitle();
				String ClusterName = Prod43ClusterName;
				com.crystaldecisions.sdk.properties.IProperty Created = boFolder.properties()
						.getProperty("SI_CREATION_TIME");
				java.util.Date FolderCreatedTime = (java.util.Date) ((com.crystaldecisions.sdk.properties.IProperty) Created)
						.getValue();
				SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MMM-dd");
				String FolderCreationTime = sdf.format(FolderCreatedTime);

				// add data to csv
				String[] data1 = { ClusterName, AgencyFolder, FolderCreationTime };
				writer.writeNext(data1);

			} catch (Exception g) {

				File EBI43ProdErrorLog = new File("C:\\AgencyFolders\\EBI43UATErrorLog.txt");

				if (EBI43ProdErrorLog.exists()) {
					EBI43ProdErrorLog.createNewFile();
				}
				PrintWriter outputStream = new PrintWriter(EBI43ProdErrorLog);
				outputStream.println(g.getMessage());
				outputStream.close();
			}
		}



// 4.3 FED PROD
		boEnterpriseSession = CrystalEnterprise.getSessionMgr().logon(Fed43User, Fed43Password, Fed43CmsName,
				Fed43AuthType);

		boInfoStore = (IInfoStore) boEnterpriseSession.getService("", "InfoStore");

		boInfoObjects = (IInfoObjects) boInfoStore.query(
				"SELECT * FROM CI_INFOOBJECTS WHERE SI_PARENTID=0 AND SI_NAME!='User Folders' AND SI_NAME!='System Configuration Wizard' AND SI_NAME!= 'LCM' AND SI_NAME!= 'Probes' AND SI_NAME!= 'Data Federation' AND SI_NAME!= 'Report Conversion Tool' AND SI_NAME!= 'Visual Difference' AND SI_NAME!= 'Platfrom Search Scheduling' AND SI_NAME!= 'Web Intelligence Samples' AND SI_NAME!='Monitoring Report Sample' AND SI_NAME!='Report Samples' AND SI_NAME!='Platform Search Scheduling'");

		int FED43FoldersSelectedfromCMS = boInfoObjects.size();
		for (int x = 0; x < FED43FoldersSelectedfromCMS; x++) {

			try {
				IFolder boFolder = (IFolder) boInfoObjects.get(x);
				String AgencyFolder = boFolder.getTitle();
				String ClusterName = Fed43ClusterName;
				com.crystaldecisions.sdk.properties.IProperty Created = boFolder.properties()
						.getProperty("SI_CREATION_TIME");
				java.util.Date FolderCreatedTime = (java.util.Date) ((com.crystaldecisions.sdk.properties.IProperty) Created)
						.getValue();
				SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MMM-dd");
				String FolderCreationTime = sdf.format(FolderCreatedTime);

				// add data to csv
				String[] data1 = { ClusterName, AgencyFolder, FolderCreationTime };
				writer.writeNext(data1);

			} catch (Exception g) {

				File EBI43FEDErrorLog = new File("C:\\AgencyFolders\\EBI43FEDErrorLog.txt");

				if (EBI43FEDErrorLog.exists()) {
					EBI43FEDErrorLog.createNewFile();
				}
				PrintWriter outputStream = new PrintWriter(EBI43FEDErrorLog);
				outputStream.println(g.getMessage());
				outputStream.close();
			}
		}

//4.3 FED UAT on AWS
		boEnterpriseSession = CrystalEnterprise.getSessionMgr().logon(Fed43FUUser, Fed43FUPassword, Fed43FUCmsName,
				Fed43FUAuthType);

		boInfoStore = (IInfoStore) boEnterpriseSession.getService("", "InfoStore");

		boInfoObjects = (IInfoObjects) boInfoStore.query(
				"SELECT * FROM CI_INFOOBJECTS WHERE SI_PARENTID=0 AND SI_NAME!='User Folders' AND SI_NAME!='System Configuration Wizard' AND SI_NAME!= 'LCM' AND SI_NAME!= 'Probes' AND SI_NAME!= 'Data Federation' AND SI_NAME!= 'Report Conversion Tool' AND SI_NAME!= 'Visual Difference' AND SI_NAME!= 'Platfrom Search Scheduling' AND SI_NAME!= 'Web Intelligence Samples' AND SI_NAME!='Monitoring Report Sample' AND SI_NAME!='Report Samples' AND SI_NAME!='Platform Search Scheduling'");

		int UATFED43FoldersSelectedfromCMS = boInfoObjects.size();
		for (int x = 0; x < UATFED43FoldersSelectedfromCMS; x++) {

			try {
				IFolder boFolder = (IFolder) boInfoObjects.get(x);
				String AgencyFolder = boFolder.getTitle();
				String ClusterName = Fed43FUClusterName;
				com.crystaldecisions.sdk.properties.IProperty Created = boFolder.properties()
						.getProperty("SI_CREATION_TIME");
				java.util.Date FolderCreatedTime = (java.util.Date) ((com.crystaldecisions.sdk.properties.IProperty) Created)
						.getValue();
				SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MMM-dd");
				String FolderCreationTime = sdf.format(FolderCreatedTime);

				// add data to csv
				String[] data1 = { ClusterName, AgencyFolder, FolderCreationTime };
				writer.writeNext(data1);

			} catch (Exception g) {

				File EBI43UATFEDErrorLog = new File("C:\\AgencyFolders\\EBI43FEDErrorLog.txt");

				if (EBI43UATFEDErrorLog.exists()) {
					EBI43UATFEDErrorLog.createNewFile();
				}
				PrintWriter outputStream = new PrintWriter(EBI43UATFEDErrorLog);
				outputStream.println(g.getMessage());
				outputStream.close();
			}
		}

// closing writer connection
		writer.close();
//tell user where to go to find output
		System.out.println("Program Ended!  Check C://AgencyFolders for the .csv file");
	}
}
