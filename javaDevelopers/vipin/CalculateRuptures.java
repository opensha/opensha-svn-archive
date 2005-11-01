package javaDevelopers.vipin;

import java.io.FileWriter;
import java.util.HashMap;
import org.opensha.util.FileUtils;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.opensha.data.LocationList;
import org.opensha.data.Location;
import java.util.Iterator;
import org.opensha.calc.RelativeLocation;
import org.opensha.data.Direction;
import java.text.DecimalFormat;

/**
 * <p>Title: CalculateRuptures.java </p>
 * <p>Description: Calculates the ruptures and it includes inter fault jumps</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class CalculateRuptures {
  private final static String FAULT_TRACE_INPUT_FILE_NAME = "FaultSections2002FromDB.txt";
  private final static double RUP_LEN = 10;
  private final static double FAULT_JUMP_CUTOFF_DIST = 5;
  private HashMap faultTraceMapping;
  private HashMap faultProcessedMapping;
  private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0#####");


  public CalculateRuptures() {
    // read the file to load the fault names and corresponding fault Trace location list
    faultTraceMapping = new HashMap();
    // make the hashmap to signify which fault names have already been processed
    // so that faults are not processed more than once
    faultProcessedMapping = new HashMap();
    ArrayList fileLines=null;
    try{
      fileLines = FileUtils.loadFile(FAULT_TRACE_INPUT_FILE_NAME);
    }catch(Exception e) {
      e.printStackTrace();
    }
    // parse the fault section file to load the fault trace locations in LocationList
    String faultName=null;
    double lon, lat, depth;
    LocationList locList=null;
    for(int i=0; i<fileLines.size(); ++i) {
      String line = ((String)fileLines.get(i)).trim();
      if(line.equalsIgnoreCase("")) continue;
      if(line.startsWith("#")) { // if it is new fault name
        if(faultName!=null) faultTraceMapping.put(faultName, locList);
        faultName = line.substring(1);
        faultProcessedMapping.put(faultName, new Boolean(false));
        locList = new LocationList();
      }
      else { // fault trace location on current fault
        StringTokenizer tokenizer = new StringTokenizer(line);
        lon = Double.parseDouble(tokenizer.nextToken());
        lat = Double.parseDouble(tokenizer.nextToken());
        depth = Double.parseDouble(tokenizer.nextToken());
        locList.addLocation(new Location(lat, lon, depth));
      }
    }

  }

  /**
   * Create a list of all possible ruptures for this fault and rupture length.
   * It also includes ruptures on adjacent faults to allow fault to fault jumps
   * @param faultName
   * @param rupLength
   * @param interFaultCutOffDistance
   * @param fw
   */
  public void createRupFile(String faultName, double rupLength,
                            double interFaultCutOffDistance, FileWriter fw) {
    // if this fault has already been procesed, return
    boolean isProcessed = ((Boolean)faultProcessedMapping.get(faultName)).booleanValue();
    if(isProcessed) return;
    // process this fault
    faultProcessedMapping.put(faultName, new Boolean(true));
    LocationList locList = (LocationList)this.faultTraceMapping.get(faultName);
    ArrayList adjacentFaultNames = new ArrayList();
    Location prevLoc = locList.getLocationAt(0);
    Location nextLoc = locList.getLocationAt(1);
    double distance;
    try {
      fw.write(faultName + "\n");
    }catch(Exception e) {
      e.printStackTrace();
    }
    double diffDist = rupLength;
    LocationList ruptureLocList = new LocationList();
    int locIndex=2;
    while(true) {
      distance = RelativeLocation.getHorzDistance(prevLoc, nextLoc);
      if(distance>rupLength) {
        Direction direction  = RelativeLocation.getDirection(prevLoc, nextLoc);
        direction.setHorzDistance(rupLength);
        Location rupLoc = RelativeLocation.getLocation(prevLoc, direction);
        ruptureLocList.addLocation(prevLoc);
        ruptureLocList.addLocation(rupLoc);
        getAdjacentFaults(rupLoc, interFaultCutOffDistance, adjacentFaultNames);
        writeToFile(fw, ruptureLocList);
        ruptureLocList = new LocationList();
        prevLoc = rupLoc;
        diffDist = rupLength;
      } else {
        if(locIndex>locList.size()-1) break;
        diffDist = rupLength-distance;
        ruptureLocList.addLocation(prevLoc);
        prevLoc = nextLoc;
        nextLoc = locList.getLocationAt(locIndex++);
      }
    }
    // now process all the adjacent faults
    for(int i=0; i<adjacentFaultNames.size(); ++i)
      createRupFile((String)adjacentFaultNames.get(i),
                    rupLength, interFaultCutOffDistance, fw);
  }


  /**
   * Write locationlist (this locationList forms a rupture) to the file
   * @param fw
   * @param locationList
   */
  private void writeToFile(FileWriter fw, LocationList locationList) {
    try {
      for (int i = 0; i < locationList.size(); ++i) {
        Location loc = locationList.getLocationAt(i);
        fw.write("(" + DECIMAL_FORMAT.format(loc.getLatitude()) + "," +
                 DECIMAL_FORMAT.format(loc.getLongitude()) + "," +
                 DECIMAL_FORMAT.format(loc.getDepth()) + ")");
        if(i!=locationList.size()-1) fw.write(" to ");
      }
      fw.write("\n");
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Get all the faults within interFaultCutOffDistance kms of the location loc
   * This allowed to find adjacent faults for fault to fault jumps
   * @param loc
   * @param interFaultCutOffDistance
   * @param adjacentFaultNames
   */
  private void getAdjacentFaults(Location loc,
                                 double interFaultCutOffDistance,
                                 ArrayList adjacentFaultNames) {
    ArrayList faultNames = new ArrayList();
    Iterator it = faultTraceMapping.keySet().iterator();
    while(it.hasNext()) {
      String faultName = (String)it.next();
      // if this fault has already been processed or it has already been added
      // to adjacent faults list
      if(((Boolean)faultProcessedMapping.get(faultName)).booleanValue()==true ||
         adjacentFaultNames.contains(faultName)) continue;
      LocationList locList = (LocationList)this.faultTraceMapping.get(faultName);
      for(int i=0; i<locList.size(); ++i) {
        // if we are able to find a nearby fault whose distance is less that cutoof distance
        if(RelativeLocation.getHorzDistance(loc, locList.getLocationAt(i))<=interFaultCutOffDistance) {
          adjacentFaultNames.add(faultName);
          break;
        }
      }
    }
  }

  public static void main(String[] args) {
    CalculateRuptures calculateRuptures = new CalculateRuptures();
    try {
      String faultName = "San Andreas fault - 1906 rupture";
      FileWriter fw = new FileWriter(faultName+"_"+RUP_LEN+"km_Ruptures.txt");
      calculateRuptures.createRupFile(faultName, RUP_LEN, FAULT_JUMP_CUTOFF_DIST,
                                      fw);
      fw.close();
    }catch(Exception e) { e.printStackTrace(); }
  }

}