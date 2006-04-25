
package javaDevelopers.interns;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.HashMap;


import org.opensha.sha.fault.SimpleFaultData;
import org.opensha.data.Location;
import org.opensha.refFaultParamDb.vo.FaultSection2002;
import org.opensha.refFaultParamDb.vo.FaultSectionSummary;

import org.opensha.sha.surface.*;
import org.opensha.sha.fault.FaultTrace;
import org.opensha.util.FileUtils;

/**
 * This class reads the Fault Section Ver 1 and provides API so that they can be viewed in Geo3D.
 *  Fault Section Ver 1 are the fault sections that were used in 2002 NSHMP
 *  This class reads from static file. This file was created by Vipin Gupta on Apr 3, 2006 after reading the
 *  table Fault_Section_Ght_ca from Oracle database in Golden.
 *
 * @author vipingupta
 *
 */
public class FaultSectionVer1Surfaces implements FaultSectionSurfaces{

	private final static double GRID_SPACING = 1.0;
	private final static String DEFAULT_INPUT_FILENAME = "FaultSections_Trace2002.txt";
	private ArrayList faultSectionsSummaryList; // saves fault section Id and corresponding name
	private HashMap faultSectionsMap; // saves fault section Id and fault section object mapping

	public FaultSectionVer1Surfaces() {
		this(DEFAULT_INPUT_FILENAME);
	}

	public FaultSectionVer1Surfaces(String fileName) {
		faultSectionsSummaryList = new ArrayList();
		faultSectionsMap = new HashMap();

		try {
			//read the file and load the fault sections
			ArrayList fileLines =  FileUtils.loadFile(fileName);
			// parse the fault section file to load the fault trace locations in LocationList
			double lon, lat, depth;
			FaultSection2002 faultSection=null;
			int sectionId=1;
		    for (int i = 1; i < fileLines.size(); ++i) { // ignore the first line it is header line
		        String line = ( (String) fileLines.get(i)).trim();
		        if (line.equalsIgnoreCase(""))
		          continue;
		        if (line.startsWith("#")) { // if it is new fault name
		        	faultSection = new FaultSection2002();
		            StringTokenizer tokenizer = new StringTokenizer(line.substring(1),",");
		            faultSection.setSectionName(tokenizer.nextToken());
		            faultSection.setAveUpperSeisDepth(Float.parseFloat(tokenizer.nextToken()));
		            faultSection.setAveLowerSeisDepth(Float.parseFloat(tokenizer.nextToken()));
		            faultSection.setAveDip(Float.parseFloat(tokenizer.nextToken()));
		            FaultTrace faultTrace = new FaultTrace(faultSection.getSectionName());
		            faultSection.setFaultTrace(faultTrace);
		            faultSectionsMap.put(new Integer(sectionId), faultSection);
		            faultSectionsSummaryList.add(new FaultSectionSummary(sectionId, faultSection.getSectionName()));
		            sectionId++;
		          }
		        else { // fault trace location on current fault section
		          StringTokenizer tokenizer = new StringTokenizer(line);
		          lon = Double.parseDouble(tokenizer.nextToken());
		          lat = Double.parseDouble(tokenizer.nextToken());
		          depth = Double.parseDouble(tokenizer.nextToken());
		          faultSection.getFaultTrace().addLocation(new Location(lat, lon, depth));
		        }
		      }

		    }catch(Exception e) {
		      e.printStackTrace();
		    }
	}

	/**
	 * Get the names and id of all fault sections
	 * @return
	 */
	public ArrayList getAllFaultSectionsSummary() {
		return this.faultSectionsSummaryList;
	}

	/**
	 * Get Frankel surface representation for a specific fault section Id
	 */
	public EvenlyGriddedSurfaceAPI getFrankelSurface(int faultSectionId) {
		FaultSection2002 faultSection = (FaultSection2002)faultSectionsMap.get(new Integer(faultSectionId));
		SimpleFaultData simpleFaultData = getSimpleFaultData(faultSection);
//		 frankel fault factory
		return new FrankelGriddedSurface(simpleFaultData, GRID_SPACING);
	}

	/**
	 * Get Stirling's surface representation for a specific fault section Id
	 */
	public EvenlyGriddedSurfaceAPI getStirlingSurface(int faultSectionId) {
		FaultSection2002 faultSection = (FaultSection2002)faultSectionsMap.get(new Integer(faultSectionId));
		SimpleFaultData simpleFaultData = getSimpleFaultData(faultSection);
		// stirling fault factory
		return new StirlingGriddedSurface(simpleFaultData, GRID_SPACING);
	}


	private SimpleFaultData getSimpleFaultData(FaultSection2002 faultSection) {
		SimpleFaultData simpleFaultData = new SimpleFaultData(faultSection.getAveDip(), faultSection.getAveLowerSeisDepth(),
				faultSection.getAveUpperSeisDepth(), faultSection.getFaultTrace());
		return simpleFaultData;
	}
}
