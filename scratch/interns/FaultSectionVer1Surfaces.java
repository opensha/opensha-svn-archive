
package scratch.interns;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.HashMap;


import org.opensha.commons.data.Location;
import org.opensha.commons.util.FileUtils;
import org.opensha.refFaultParamDb.vo.FaultSection2002;
import org.opensha.refFaultParamDb.vo.FaultSectionSummary;

import org.opensha.sha.faultSurface.*;

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

	private final static double DEFAULT_GRID_SPACING = 1.0;
	private double gridSpacing = DEFAULT_GRID_SPACING;
	private final static String DEFAULT_INPUT_FILENAME = "FaultSections_Trace2002.txt";
	private ArrayList faultSectionsSummaryList; // saves fault section Id and corresponding name
	private HashMap faultSectionsMap; // saves fault section Id and fault section object mapping

	public FaultSectionVer1Surfaces() throws FileNotFoundException, IOException{
		this(DEFAULT_INPUT_FILENAME);
	}

	public FaultSectionVer1Surfaces(URL url) throws Exception {
		this(FileUtils.loadFile(url));
	}
	
	public FaultSectionVer1Surfaces(ArrayList fileLines) {

		try {
			//read the file and load the fault sections
			faultSectionsSummaryList = new ArrayList();
			faultSectionsMap = new HashMap();
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
		          faultSection.getFaultTrace().addLocation(new Location(lat, lon, faultSection.getAveUpperSeisDepth()));
		        }
		      }

		    }catch(Exception e) {
		      e.printStackTrace();
		    }
	}
	
	public FaultSectionVer1Surfaces(String fileName) throws FileNotFoundException, IOException {
		this(FileUtils.loadFile(fileName));
	
	}
	
	/**
	 * This function allows the user to refresh the fault section data for this Id from the database.
	 *  This is used for DB Ver 2 only because ver 1 data is  loaded from text files only.
	 *  
	 * @param faultSectionId
	 */
	public void reloadFaultSectionFromDatabase(int faultSectionId) {
		
	}
	
	/**
	 * Refresh all the fault sections which are currently in cache
	 *
	 */
	public void reloadAllFaultSectionsFromDatabase() {
		
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
		SimpleFaultData simpleFaultData = faultSection.getSimpleFaultData();
//		 frankel fault factory
		return new FrankelGriddedSurface(simpleFaultData, gridSpacing);
	}

	/**
	 * Get Stirling's surface representation for a specific fault section Id
	 */
	public EvenlyGriddedSurfaceAPI getStirlingSurface(int faultSectionId) {
		FaultSection2002 faultSection = (FaultSection2002)faultSectionsMap.get(new Integer(faultSectionId));
		SimpleFaultData simpleFaultData = faultSection.getSimpleFaultData();
		// stirling fault factory
		return new StirlingGriddedSurface(simpleFaultData, gridSpacing);
	}

	
	/**
	 * Get the Minimum value for slip rate 
	 * @return
	 */
	public double getMinSlipRate() { return 0;}
	
	/**
	 * Get the maximum value for slip rate
	 * @return
	 */
	public double getMaxSlipRate(){ return 0;}
	
	/**
	 * Get the slip rate for a fault section Id
	 * @param faultSectionId
	 * @return
	 */
	public double getSlipRate(int faultSectionId) { return 0;}
	
	public void setGridSpacing(double gridSpacing) {
		this.gridSpacing = gridSpacing;
	}
}
