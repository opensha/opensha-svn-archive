package javaDevelopers.ned;


import org.opensha.util.FileUtils;
import org.opensha.sha.fault.FaultTrace;
import org.opensha.data.Location;
import java.util.*;
import java.io.*;

/**
 * <p>Title: VC_Analysis</p>
 *
 * <p>Description:
 * </p>
 *
 * @author Edward (Ned) Field
 * @version 1.0
 */
public class VC_Analysis {

  final static boolean D = true; // debugging flag

  private String faultNames[];
  private int firstIndex[], lastIndex[];
  private double seg_x_west[],seg_y_west[],seg_x_east[],seg_y_east[],seg_slipRate[], seg_area[];
  private ArrayList segSlipInfoList;
  private TreeMap timeSegMapping;
  private ArrayList faultList;

  /* these are the lat & lon of the first point on the first segment (Bartlett Strings fault),
     used for converting the x & y values to lat & lon.  These were determined by looking at the
     NSHMP-2002 fault model.
  */
  private double lat0 = 38.9334, lon0 = -122.5041; // lat * lon of first point on first segment


  public VC_Analysis() {

    Read_VC_Faults_2001v6 read_vc_faults_2001v6 = new Read_VC_Faults_2001v6();
    seg_x_west = read_vc_faults_2001v6.getSeg_x_west();
    seg_y_west = read_vc_faults_2001v6.getSeg_y_west();
    seg_x_east = read_vc_faults_2001v6.getSeg_x_east();
    seg_y_east = read_vc_faults_2001v6.getSeg_y_east();
    seg_slipRate = read_vc_faults_2001v6.getSeg_slipRate();

    Read_VC_FaultNamesSegs read_VC_FaultNamesSegs = new Read_VC_FaultNamesSegs();
    faultNames = read_VC_FaultNamesSegs.getFaultNames();
    firstIndex = read_VC_FaultNamesSegs.getFirstIndex();
    lastIndex = read_VC_FaultNamesSegs.getLastIndex();

    Read_VC_FaultActivity read_vc_faultactivity_saf = new
        Read_VC_FaultActivity("javaDevelopers/ned/RundleVC_data/VC_Fault_Activity_SAF.txt");
    seg_area = read_vc_faultactivity_saf.getSegmentAreas();
    segSlipInfoList = read_vc_faultactivity_saf.getSegmentsSlipTimeHistories();
    timeSegMapping = read_vc_faultactivity_saf.getTimeSegmentMapping();

    if(D) {
      System.out.println(seg_x_west.length + "  " + seg_y_west.length + "  " +
                         seg_x_east.length + "  " + seg_y_east.length + "  " +
                         seg_slipRate.length + "  " + seg_area.length + "  " +
                         segSlipInfoList.size());
    }
    makeSeparateEventsList();

/*
    makeFaultTraces();
    if(D) {
      FaultTrace tempFlt;
      int num=0;
      for(int i = 0; i<faultList.size(); i++) {
        tempFlt = (FaultTrace) faultList.get(i);
        System.out.println(tempFlt.getNumLocations()+"  "+tempFlt.getName());
        num += tempFlt.getNumLocations();
      }
      System.out.println(num-faultList.size());
    }

    try {
      writeFaultTraces();
    }
    catch (IOException ex1) {
      ex1.printStackTrace();
      System.exit(0);
    }
*/
  }



  private void makeSeparateEventsList() {
//    FileWriter fw = new FileWriter(fileName);
    ArrayList eventsList;
    Set keySet = timeSegMapping.keySet();
    Iterator it = keySet.iterator();
//    fw.write("Year  Segment-Numbers\n");
    //while (it.hasNext()) {
      Integer timePd = (Integer) it.next();
      ArrayList segmentList = (ArrayList) timeSegMapping.get(timePd);
      int size = segmentList.size();
      eventsList = separateEvents(segmentList);
//      fw.write(timePd.intValue() + " ");
//      for (int i = 0; i < size - 1; ++i)
//        fw.write( ( (Integer) segmentList.get(i)).intValue() + " ");
//      fw.write("" + ( (Integer) segmentList.get(size - 1)).intValue());
//      fw.write("\n");
    //}
//    fw.close();
  }

  private ArrayList separateEvents(ArrayList segments) {
    ArrayList eventsList = new ArrayList();
    ArrayList availSegs = (ArrayList) segments.clone();

    ArrayList segs = new ArrayList();
    segs.add(availSegs.get(0));
    availSegs.remove(0);
    Iterator it = availSegs.iterator();
//    while(it.hasNext())


//  System.out.println(segments.size()+"  "+ availSegs.size());
//  for(int j = 0; j < availSegs.size(); j++)
//    System.out.println(segments.get(j)+"  "+ availSegs.get(j));
    return eventsList;
  }


  /**
   * This gets the minimum distance between either ends of the two input segments
   * @param seg1 int
   * @param seg2 int
   * @return double
   */
  private double getMinDist(int seg1, int seg2) {
    double min = Math.sqrt( Math.pow(seg_x_west[seg1]-seg_x_west[seg2],2.0) + Math.pow(seg_y_west[seg1]-seg_y_west[seg2],2.0) );
    double dist = Math.sqrt( Math.pow(seg_x_west[seg1]-seg_x_east[seg2],2.0) + Math.pow(seg_y_west[seg1]-seg_y_east[seg2],2.0) );
    if (dist < min) min = dist;
    dist = Math.sqrt( Math.pow(seg_x_east[seg1]-seg_x_east[seg2],2.0) + Math.pow(seg_y_east[seg1]-seg_y_east[seg2],2.0) );
    if (dist < min) min = dist;
    dist = Math.sqrt( Math.pow(seg_x_east[seg1]-seg_x_west[seg2],2.0) + Math.pow(seg_y_east[seg1]-seg_y_west[seg2],2.0) );
    if (dist < min) min = dist;
    return min;
  }



  // these methods make and write approx fault traces; they don't look right
  private void makeFaultTraces() {
    faultList = new ArrayList();
    FaultTrace fault;
    Location loc;
    double lat, lon;
    double x0 = seg_x_west[0];
    double y0 = seg_y_west[0];
    double lonCorr = Math.cos(Math.PI*lat0/180);
    for(int i=0; i<faultNames.length; i++) {
      fault = new FaultTrace(faultNames[i]);
      for(int j = firstIndex[i]; j <= lastIndex[i]; j++) {
        lon = (seg_x_west[j]-x0)/(111.111*lonCorr) + lon0;
        lat = (seg_y_west[j]-y0)/111.111 + lat0;
        loc = new Location(lat, lon);
        fault.addLocation(loc);
      }
      // get the last point
      lon = (seg_x_east[lastIndex[i]]-x0)/(111.111*lonCorr) + lon0;
      lat = (seg_y_east[lastIndex[i]]-y0)/111.111 + lat0;
      loc = new Location(lat, lon);
      fault.addLocation(loc);
      faultList.add(fault);
    }
  }


  private void writeFaultTraces() throws IOException {

    FileWriter fw = new FileWriter("javaDevelopers/ned/RundleVC_data/VC_faultTraces.txt");
    FaultTrace trace;
    Location tempLoc;
    for(int i=0; i< faultList.size();i++) {
      trace = (FaultTrace) faultList.get(i);
      fw.write(trace.getName()+"\t"+trace.getNumLocations()+"\n");
      for(int j=0; j<trace.getNumLocations();j++) {
        tempLoc = trace.getLocationAt(j);
        fw.write((float)tempLoc.getLongitude()+"\t"+(float)tempLoc.getLatitude()+"\n");
      }
    }
    fw.close();


    fw = new FileWriter("javaDevelopers/ned/RundleVC_data/VC_faultNamesNumPts.txt");
    for(int i=0; i< faultList.size();i++) {
      trace = (FaultTrace) faultList.get(i);
      fw.write(trace.getName()+"\t"+trace.getNumLocations()+"\n");
    }
    fw.close();

  }


  public static void main(String[] args) {
    VC_Analysis analysis = new VC_Analysis();
  }
}
