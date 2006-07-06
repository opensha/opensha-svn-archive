/**
 * 
 */
package org.opensha.refFaultParamDb.gui.view;

import java.io.File;
import java.io.FileWriter;

import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.fault.FaultTrace;
import org.opensha.sha.gui.infoTools.CalcProgressBar;

/**
 * @author vipingupta
 *
 */
public class SectionInfoFileWriter implements Runnable {
	private  PrefFaultSectionDataDB_DAO faultSectionPrefDAO = new PrefFaultSectionDataDB_DAO(DB_AccessAPI.dbConnection); 
	private CalcProgressBar progressBar;
	private int totSections;
	private int currSection;
	/**
	 * Write FaultSectionPrefData to file.
	 * @param faultSectionIds  array of faultsection Ids
	 * @param file
	 */
	public  void writeForFaultModel(int[] faultSectionIds, File file) {
		try {
			currSection=0;
			totSections = faultSectionIds.length;
			// make JProgressBar
			progressBar = new CalcProgressBar("Writing to file", "Writing Fault sections");
			progressBar.displayProgressBar();
			Thread t = new Thread(this);
			t.start();
			// write to file
			FileWriter fw = new FileWriter(file);
			fw.write(getFormatStringForFaultModel());
			
			for(currSection=0; currSection<totSections; ++currSection) {
				System.out.println(currSection);
				writeForFaultModel(faultSectionIds[currSection], fw);
			}
			fw.close();
			
			// dispose the progressbar
			progressBar.showProgress(false);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		try {
			while(currSection<totSections) {
				//System.out.println("Updating "+currSection+ " of "+totSections);
				progressBar.updateProgress(this.currSection, this.totSections);
				Thread.currentThread().sleep(500);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Write FaultSectionPrefData to the file. It does not contain slip rate and aseismic slip factor
	 * @param faultSectionId Fault section Id for which data needs to be written to file
	 * @param fw
	 */
	public  void writeForFaultModel(int faultSectionId, FileWriter fw) {
		try{
			writeForFaultModel(faultSectionPrefDAO.getFaultSectionPrefData(faultSectionId), fw);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get String for faultSectionPrefData ( excluding slip rate and aseismic slip factor)
	 * @param faultSectionId Fault section Id for which data needs to be retieved
	 */
	public  void getStringForFaultModel(int faultSectionId) {
		try{
			getStringForFaultModel(faultSectionPrefDAO.getFaultSectionPrefData(faultSectionId));
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Write FaultSectionPrefData to the file. It does not contain slip rate and aseismic slip factor
	 * @param faultSectionPrefData
	 * @param fw
	 */
	public  void writeForFaultModel(FaultSectionPrefData faultSectionPrefData, FileWriter fw) {
		try{
			fw.write(getStringForFaultModel(faultSectionPrefData));
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get String for faultSectionPrefData ( excluding slip rate and aseismic slip factor)
	 * @param faultSectionPrefData
	 * @return
	 */
	public  String getStringForFaultModel(FaultSectionPrefData faultSectionPrefData) {
		FaultTrace faultTrace = faultSectionPrefData.getFaultTrace(); 
		String str =  "#"+faultSectionPrefData.getSectionName()+"\n"+
			getValue(faultSectionPrefData.getAveUpperDepth())+"\n"+
			getValue(faultSectionPrefData.getAveLowerDepth())+"\n"+
			getValue(faultSectionPrefData.getAveDip()) +"\n"+
			getValue(faultSectionPrefData.getDipDirection())+"\n"+
			getValue(faultSectionPrefData.getAveRake())+"\n"+
			getValue(faultTrace.getTraceLength())+"\n"+
			faultTrace.getNumLocations()+"\n";
		// write all the point on the fault section trace
		for(int i=0; i<faultTrace.getNumLocations(); ++i)
			str+=(float)faultTrace.getLocationAt(i).getLatitude()+"\t"+(float)faultTrace.getLocationAt(i).getLongitude()+"\n";
		return str;
	}
	
	private  String getValue(double val) {
		if(Double.isNaN(val)) return "Not Available";
		else return GUI_Utils.decimalFormat.format(val);
	}
	
	/**
	 * File format for writing fault sections in a fault model file.
	 * Fault sections within a fault model do not have slip rate and aseismic slip factor
	 * 
	 * @return
	 */
	public  String getFormatStringForFaultModel() {
		return "********************************\n"+ 
			"#Section Name\n"+
			"#Ave Upper Seis Depth (km)\n"+
			"#Ave Lower Seis Depth (km)\n"+
			"#Ave Dip (degrees)\n"+
			"#Ave Dip Direction\n"+
			"#Ave Rake\n"+
			"#Trace Length (derivative value) (km)\n"+
			"#Num Trace Points\n"+
			"#lat1 lon1\n"+
			"#lat2 lon2\n"+
			"********************************\n";
	}
}
