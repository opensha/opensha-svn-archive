/**
 * 
 */
package scratch.UCERF3.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.FaultTrace;

import scratch.UCERF3.FaultSystemRupSet;

/**
 * @author Ned Field
 *
 */
public class FaultSectionDataWriter {

	/**
	 * 
	 * @param subSectionPrefDataList
	 * @param metaData - each String in this list will have a "# " and "\n" added to the beginning and ending, respectively
	 * @param filePathAndName
	 */
	public final static void writeSectionsToFile(ArrayList<FaultSectionPrefData> subSectionPrefDataList, 
			ArrayList<String> metaData, String filePathAndName) {
		try {
			FileWriter fw = new FileWriter(filePathAndName);
			fw.write(getSectionsASCII(subSectionPrefDataList, metaData).toString());
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param subSectionPrefDataList
	 * @param metaData - each String in this list will have a "# " and "\n" added to the beginning and ending, respectively
	 * @param filePathAndName
	 */
	public final static StringBuffer getSectionsASCII(List<FaultSectionPrefData> subSectionPrefDataList, 
			ArrayList<String> metaData) {
		StringBuffer buff = new StringBuffer();
		if (metaData != null && !metaData.isEmpty()) {
			String header1 = "# ******** MetaData **************\n";
			for(String metaDataLine: metaData)
				header1 += "# "+metaDataLine+"\n";
			buff.append(header1);
		}
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
		buff.append(header2);
		for(int i=0; i<subSectionPrefDataList.size(); i++) {
			FaultSectionPrefData sectData = subSectionPrefDataList.get(i);
			FaultTrace faultTrace = sectData.getFaultTrace(); 
			String str =  i+"\n"+sectData.getSectionName()+"\n"+
					getValue(sectData.getParentSectionId())+"\n"+
					getValue(sectData.getParentSectionName())+"\n"+
					getValue(sectData.getOrigAveUpperDepth())+"\n"+
					getValue(sectData.getAveLowerDepth())+"\n"+
					getValue(sectData.getAveDip()) +"\n"+
					getValue(sectData.getDipDirection())+"\n"+
					getValue(sectData.getOrigAveSlipRate())+"\n"+
					getValue(sectData.getAseismicSlipFactor())+"\n"+
					getValue(sectData.getAveRake())+"\n"+
					getValue(faultTrace.getTraceLength())+"\n"+
					faultTrace.getNumLocations()+"\n";
			// write all the point on the fault section trace
			for(int j=0; j<faultTrace.getNumLocations(); ++j)
				str+=(float)faultTrace.get(j).getLatitude()+"\t"+(float)faultTrace.get(j).getLongitude()+"\n";
			buff.append(str);
		}
		return buff;
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

	/**
	 * This writes the rupture sections to an ASCII file
	 * @param filePathAndName
	 * @throws IOException 
	 */
	public static void writeRupsToFiles(String filePathAndName, FaultSystemRupSet rupSet) throws IOException {
		FileWriter fw = new FileWriter(filePathAndName);
		fw.write(getRupsASCII(rupSet).toString());
		fw.close();
	}

	/**
	 * This writes the rupture sections to an ASCII file
	 * @param filePathAndName
	 */
	public static StringBuffer getRupsASCII(FaultSystemRupSet rupSet) {
		StringBuffer buff = new StringBuffer();
		buff.append("rupID\tclusterID\trupInClustID\tmag\tnumSectIDs\tsect1_ID\tsect2_ID\t...\n");	// header
		//			  int rupIndex = 0;
		for(int c=0;c<rupSet.getNumClusters();c++) {
			List<Integer> rups = rupSet.getRupturesForCluster(c);
			//				  ArrayList<ArrayList<Integer>>  rups = rupSet.getCluster(c).getSectionIndicesForRuptures();
			for(int rupIndex : rups) {
				//					  ArrayList<Integer> rup = rups.get(r);
				List<Integer> sections = rupSet.getSectionsIndicesForRup(rupIndex);
				String line = Integer.toString(rupIndex)+"\t"+Integer.toString(c)+"\t"+Integer.toString(rupIndex)+"\t"+
						+(float)rupSet.getMagForRup(rupIndex)+"\t"+sections.size();
				for(Integer sectID: sections) {
					line += "\t"+sectID;
				}
				line += "\n";
				buff.append(line);
				rupIndex+=1;
			}				  
		}
		return buff;
	}

}
