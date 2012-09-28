package scratch.UCERF3.utils.paleoRateConstraints;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.dom4j.DocumentException;
import org.opensha.commons.geo.Location;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.gui.infoTools.GraphPanel;


import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.inversion.InversionFaultSystemRupSetFactory;
import scratch.UCERF3.inversion.InversionInputGenerator;
import scratch.UCERF3.utils.UCERF3_DataUtils;
import scratch.UCERF3.utils.aveSlip.AveSlipConstraint;

public class UCERF3_PaleoRateConstraintFetcher {
	
	private final static String PALEO_DATA_SUB_DIR = "paleoRateData";
	private final static String PALEO_DATA_FILE_NAME = "UCERF3_PaleoRateData_v05.xls";
	
	protected final static boolean D = true;  // for debugging
	
	public static ArrayList<PaleoRateConstraint> getConstraints(
			List<FaultSectionPrefData> faultSectionData) throws IOException {
		
		ArrayList<PaleoRateConstraint> paleoRateConstraints   = new ArrayList<PaleoRateConstraint>();
		if(D) System.out.println("Reading Paleo Seg Rate Data from "+PALEO_DATA_FILE_NAME);
		InputStream is =
			UCERF3_DataUtils.locateResourceAsStream(PALEO_DATA_SUB_DIR, PALEO_DATA_FILE_NAME);
		POIFSFileSystem fs = new POIFSFileSystem(is);
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet sheet = wb.getSheetAt(0);
		int lastRowIndex = sheet.getLastRowNum();
		double lat, lon, meanRate, lower68Conf, upper68Conf;
		String siteName;
		for(int r=1; r<=lastRowIndex; ++r) {	
			HSSFRow row = sheet.getRow(r);
			if(row==null) continue;
			HSSFCell cell = row.getCell(1);
			if(cell==null || cell.getCellType()==HSSFCell.CELL_TYPE_STRING) continue;
			lat = cell.getNumericCellValue();
			siteName = row.getCell(0).getStringCellValue().trim();
			lon = row.getCell(2).getNumericCellValue();
			// skipping MRI cells
			meanRate = row.getCell(6).getNumericCellValue();
			lower68Conf = row.getCell(8).getNumericCellValue();	// note the labels are swapped in the *_v1 file
			upper68Conf =  row.getCell(7).getNumericCellValue();
			
			if (lower68Conf == upper68Conf) {
				// TODO we don't want any of these
				System.out.println("Skipping value at "+siteName+" because upper and lower " +
						"values are equal: meanRate="+(float)meanRate+
					"\tlower68="+(float)lower68Conf+"\tupper68="+(float)upper68Conf);
				continue;
			}
				
			// get Closest section
			double minDist = Double.MAX_VALUE, dist;
			int closestFaultSectionIndex=-1;
			Location loc = new Location(lat,lon);
			
			// these hacks along with SCEC-VDO images are described in an e-mail from
			// Kevin on 3/6/12, subject "New MRI table"
			boolean blindThrustHack = siteName.equals("Compton") || siteName.equals("Puente Hills");
			boolean safOffshoreHack = siteName.equals("N. San Andreas -Offshore Noyo");
			
			for(int sectionIndex=0; sectionIndex<faultSectionData.size(); ++sectionIndex) {
				FaultSectionPrefData data = faultSectionData.get(sectionIndex);
				// TODO this is a hack for blind thrust faults
				if (blindThrustHack && !data.getSectionName().contains(siteName))
					continue;
				dist  = data.getFaultTrace().minDistToLine(loc);
				if(dist<minDist) {
					minDist = dist;
					closestFaultSectionIndex = sectionIndex;
				}
			}
			if(minDist>2 && !blindThrustHack && !safOffshoreHack || closestFaultSectionIndex < 0) {
				if (D) {
					if (D) System.out.print("No match for: "+siteName+" (lat="+lat+", lon="+lon
							+") closest was "+minDist+" away: "+closestFaultSectionIndex);
					if (closestFaultSectionIndex >= 0)
						System.out.println(". "+faultSectionData.get(closestFaultSectionIndex).getSectionName());
					else
						System.out.println();
				}
				continue; // closest fault section is at a distance of more than 2 km
			}
			System.out.println("Matching constraint for closest index: "+closestFaultSectionIndex+" site name: "+siteName);
			// add to Seg Rate Constraint list
			String name = faultSectionData.get(closestFaultSectionIndex).getSectionName();
			PaleoRateConstraint paleoRateConstraint = new PaleoRateConstraint(name, loc, closestFaultSectionIndex, 
					meanRate, lower68Conf, upper68Conf);
			paleoRateConstraint.setPaleoSiteName(siteName);
			if(D) System.out.println("\t"+siteName+" (lat="+lat+", lon="+lon+") associated with "+name+
					" (section index = "+closestFaultSectionIndex+")\tdist="+(float)minDist+"\tmeanRate="+(float)meanRate+
					"\tlower68="+(float)lower68Conf+"\tupper68="+(float)upper68Conf);
			paleoRateConstraints.add(paleoRateConstraint);
		}
		return paleoRateConstraints;
	}
	
	public static void main(String args[]) throws IOException, DocumentException {
		
   		FaultSystemRupSet faultSysRupSet = InversionFaultSystemRupSetFactory.cachedForBranch(DeformationModels.GEOLOGIC);
   		UCERF3_PaleoRateConstraintFetcher.getConstraints(faultSysRupSet.getFaultSectionDataList());

//		File rupSetsDir = new File(precomp, "FaultSystemRupSets");
//		ArrayList<FaultSystemSolution> sols = new ArrayList<FaultSystemSolution>();
//		sols.add(SimpleFaultSystemSolution.fromFile(new File(rupSetsDir, "UCERF2.xml")));
//		sols.add(SimpleFaultSystemSolution.fromFile(new File(rupSetsDir, "Model1.xml")));
//		
//		showSegRateComparison(getConstraints(precomp, sols.get(0).getFaultSectionDataList()), sols);
	}
}
