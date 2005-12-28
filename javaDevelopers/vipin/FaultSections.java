package javaDevelopers.vipin;

import java.util.HashMap;
import org.opensha.data.Location;
import org.opensha.data.LocationList;
import java.util.StringTokenizer;
import org.opensha.util.FileUtils;
import java.util.ArrayList;

/**
 * <p>Title: FaultSections.java </p>
 * <p>Description: It loads the fault sections from text files.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class FaultSections {
  private final static String INPUT_FILE_NAME1 = "javaDevelopers\\ned\\NSHMP02_CA_Traces_N.txt";
  private final static String INPUT_FILE_NAME2 = "javaDevelopers\\ned\\NSHMP02_CA_Traces_RV.txt";
  private final static String INPUT_FILE_NAME3 = "javaDevelopers\\ned\\NSHMP02_CA_Traces_SS.txt";
  private HashMap faultTraceMapping; // fault section and their correpsonding traces
  private final static double LAT_CUTOFF = 40.0; // any fault section have a location above this CUTOFF is neglected

  // load the fault sections from files
  public FaultSections() {
    this(INPUT_FILE_NAME1, INPUT_FILE_NAME2, INPUT_FILE_NAME3);

  }

  public FaultSections(String fileName1, String fileName2, String fileName3) {
    faultTraceMapping = new HashMap();
    loadFaultSections(fileName1, faultTraceMapping);
    loadFaultSections(fileName2, faultTraceMapping);
    loadFaultSections(fileName3, faultTraceMapping);
  }

  /**
   * Load the fault sections into the hashmap
   * @param fileName
   * @param faultMap
   */
  private void loadFaultSections(String fileName, HashMap faultMap) {
    ArrayList fileLines=null;
    try{
      fileLines = FileUtils.loadFile(fileName);
    }catch(Exception e) {
      e.printStackTrace();
    }
    // parse the fault section file to load the fault trace locations in LocationList
    String faultName=null;
    double lon, lat, depth;
    LocationList locList=null;
    boolean lowerThanCutoff = true;
    for(int i=0; i<fileLines.size(); ++i) {
      String line = ((String)fileLines.get(i)).trim();
      if(line.equalsIgnoreCase("")) continue;
      if(line.startsWith("#")) { // if it is new fault name
        if(faultName!=null && lowerThanCutoff) faultMap.put(faultName, locList);
        faultName = line.substring(1);
        locList = new LocationList();
        lowerThanCutoff = true;
      }
      else { // fault trace location on current fault
        StringTokenizer tokenizer = new StringTokenizer(line);
        lon = Double.parseDouble(tokenizer.nextToken());
        lat = Double.parseDouble(tokenizer.nextToken());
        if(lat>this.LAT_CUTOFF) lowerThanCutoff = false;
        depth = Double.parseDouble(tokenizer.nextToken());
        locList.addLocation(new Location(lat, lon, depth));
      }
    }
    if(lowerThanCutoff)
      faultMap.put(faultName, locList);

  }


  /**
   * Get all the fault sections
   * @return
   */
  public HashMap getAllFaultSections() {
    return faultTraceMapping;
  }

}