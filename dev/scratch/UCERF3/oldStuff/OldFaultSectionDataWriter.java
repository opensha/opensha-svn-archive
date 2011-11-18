/**
 * 
 */
package scratch.UCERF3.oldStuff;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.finalReferenceFaultParamDb.UCERF2_FaultSectionPrefData;
import org.opensha.sha.faultSurface.FaultTrace;

/**
 * @author Ned Field
 *
 */
public class OldFaultSectionDataWriter {
		
	/**
	 * 
	 * @param subSectionPrefDataList
	 * @param metaData - each String in this list will have a "# " and "\n" added to the beginning and ending, respectively
	 * @param filePathAndName
	 */
	public final static void writeSectionsToFile(ArrayList<UCERF2_FaultSectionPrefData> subSectionPrefDataList, 
			ArrayList<String> metaData, String filePathAndName) {
		FileWriter fw;
		try {
			fw = new FileWriter(filePathAndName);
			
			String header1 = "# ******** MetaData **************\n";
			for(String metaDataLine: metaData)
				header1 += "# "+metaDataLine+"\n";
			fw.write(header1);
			String header2 = "# ******** Data Format ***********\n"+ 
			"# Section Index\n"+
			"# Section Name\n"+
			"# Parent Section ID\n"+
			"# Parent Section Name\n"+
			"# Ave Upper Seis Depth (km)\n"+
			"# Ave Lower Seis Depth (km)\n"+
			"# Ave Dip (degrees)\n"+
			"# Ave Dip Direction\n"+
			"# Ave Long Term Slip Rate\n"+
			"# Ave Aseismic Slip Factor\n"+
			"# Ave Rake\n"+
			"# Trace Length (derivative value) (km)\n"+
			"# Num Trace Points\n"+
			"# lat1 lon1\n"+
			"# lat2 lon2\n"+
			"# etc for all trace points\n"+
			"# ********************************\n";
			fw.write(header2);
			for(int i=0; i<subSectionPrefDataList.size(); i++) {
				UCERF2_FaultSectionPrefData sectData = subSectionPrefDataList.get(i);
				FaultTrace faultTrace = sectData.getFaultTrace(); 
				String str =  i+"\n"+sectData.getSectionName()+"\n"+
			    	getValue(sectData.getParentSectionId())+"\n"+
			    	getValue(sectData.getParentSectionName())+"\n"+
					getValue(sectData.getAveUpperDepth())+"\n"+
					getValue(sectData.getAveLowerDepth())+"\n"+
					getValue(sectData.getAveDip()) +"\n"+
					getValue(sectData.getDipDirection())+"\n"+
					getValue(sectData.getAveLongTermSlipRate())+"\n"+
					getValue(sectData.getAseismicSlipFactor())+"\n"+
					getValue(sectData.getAveRake())+"\n"+
					getValue(faultTrace.getTraceLength())+"\n"+
					faultTrace.getNumLocations()+"\n";
				// write all the point on the fault section trace
				for(int j=0; j<faultTrace.getNumLocations(); ++j)
					str+=(float)faultTrace.get(j).getLatitude()+"\t"+(float)faultTrace.get(j).getLongitude()+"\n";
				fw.write(str);
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private final static  String getValue(double val) {
		if(Double.isNaN(val)) return "Not Available";
		else return GUI_Utils.decimalFormat.format(val);
	}
	
	private final static  String getValue(int val) {
		if(val == -1) return "Not Available";
		else return Integer.toString(val);
	}

	
	private final static String getValue(String val) {
		if(val==null || val.equalsIgnoreCase("")) return "Not Available";
		else return val;
	}
	
}
