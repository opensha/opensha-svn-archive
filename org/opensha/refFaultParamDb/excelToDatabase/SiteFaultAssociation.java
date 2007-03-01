/**
 * 
 */
package org.opensha.refFaultParamDb.excelToDatabase;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.gui.infotools.SessionInfo;
import org.opensha.util.FileUtils;

/**
 * Java code to associate fault names with each combined event info.
 * data provided by Peter Bird
 * 
 * @author vipingupta
 *
 */
public class SiteFaultAssociation {
//	 update the sense of motion
	private final static String FILE_NAME = "org/opensha/refFaultParamDb/excelToDatabase/SiteFaultAssociation.csv";
	// database connection
	private  static final DB_AccessAPI dbConnection = DB_AccessAPI.dbConnection;
	 
	public static void main(String[] args) {
		SessionInfo.setUserName("vgupta");
		SessionInfo.setPassword("vgupta");
		try {
			ArrayList<String> lines = FileUtils.loadFile(FILE_NAME);
			for(int i=1; i<lines.size(); ++i) {
				String line = lines.get(i);
				StringTokenizer tokenizer = new StringTokenizer(line,",");
				String entryDate = tokenizer.nextToken();
				int faultSectionId= Integer.parseInt(tokenizer.nextToken());
				int siteId= Integer.parseInt(tokenizer.nextToken());
				dbConnection.insertUpdateOrDeleteData("update combined_events_info "+
						"  set FAULT_SECTION_ID="+faultSectionId+" where site_id="+siteId+
						"   and entry_date='"+entryDate+"'");
			}
			System.out.println("Lines in Excel sheet="+(lines.size()-1));
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
