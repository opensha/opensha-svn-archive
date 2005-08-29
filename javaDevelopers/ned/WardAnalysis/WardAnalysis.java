package javaDevelopers.ned.WardAnalysis;

import javaDevelopers.ned.RundleAnalysis.SegmentSlipTimeInfo;
import org.opensha.util.FileUtils;
import java.util.*;
import java.io.*;

/**
 * <p>Title: WardAnalysis</p>
 *
 * <p>Description:
 *
 * NOTE that an element ("elem") here corresponds to a "segment" in the Rundle analysis,
 * as ruptures are composed of elements here.
 * </p>
 *
 * @author Edward (Ned) Field
 * @version 1.0
 */
public class WardAnalysis {

  final static boolean D = false; // debugging flag

  // segment info (segment number is the index in the following):
  private double seg_slipRate[], seg_ddw[];
  private String seg_code[], seg_name[];

  // element info(element number is the index in the following):
  private int elem_fltNum[], elem_segNum[];
  private double elem_length[], elem_azim[], elem_lat[], elem_lon[];

  // simulation data (qk number is the index in the following):
  private int eventStartElem[], eventEndElem[];
  private double eventMo[], eventMag[], eventYear[];

  //computed info:
  private double elem_area[];
  private ArrayList elemSlipInfoList, randomElemSlipInfoList;
  private TreeMap timeElemMapping, randomTimeElemMapping;
  private double eventAveSlip[], eventArea[];
  private double eventYearPred1[], aveLastEvTime1[], eventYearPred2[], aveLastEvTime2[];


  public WardAnalysis() {

    // read segment info: seg_slipRate[], seg_ddw, seg_code[], & seg_name[]
    // and element info:  elem_fltNum[], elem_segNum[],elem_length[], elem_azim[],
    //                    elem_lat, elem_lon
    read_fort_11();

    // read event info: eventStartElem[], eventEndElem[], eventMo[], eventMag[], & eventYear[]
    read_FOR088_DAT();

    // make other data
    make_computed_data();


    // get computed info:
    // elemSlipInfoList ;
    // timeElemMapping ;
    /*
    eventAveSlip = new double[eventYear.size()];
    eventArea = new double[eventYear.size()];
    eventYearPred1 = new double[eventYear.size()];
    eventYearPred2 = new double[eventYear.size()];
    aveLastEvTime1 = new double[eventYear.size()];
    aveLastEvTime2 = new double[eventYear.size()];
*/

  /*
    try {
      writeSegNumYearSlip();
    }
    catch (IOException ex1) {
      ex1.printStackTrace();
      System.exit(0);
    }


    try {
      writeSegmentStats();
    }
    catch (IOException ex1) {
      ex1.printStackTrace();
      System.exit(0);
    }



    getEventStats();

    try {
      writeEventData();
    }
    catch (IOException ex1) {
      ex1.printStackTrace();
      System.exit(0);
    }




        // test
        SegmentSlipTimeInfo info = (SegmentSlipTimeInfo) elemSlipInfoList.get(0);
        ArrayList times = info.getTimeHistories();
        Iterator it = times.iterator();
        Integer tempInt;
        while(it.hasNext()) {
          tempInt = (Integer) it.next();
          System.out.println(tempInt+"\t"+info.getPreviousSlipTime(tempInt));
        }
        System.out.println(info.getPreviousSlipTime(new Integer(12000000)));
 */
  }


  private void make_computed_data() {
    // elem_area[], eventAveSlip[], eventArea[],
    // ArrayList elemSlipInfoList
    // TreeMap timeElemMapping

    // make elem_area
    int num_elem = elem_length.length;
    elem_area = new double[num_elem];
    int i;
    for(i=0;i<num_elem;i++) {
      elem_area[i] = elem_length[i] * this.seg_ddw[elem_segNum[i]];
    }



  }

  private void read_fort_11() {
    String inputFileName = "javaDevelopers/ned/WardAnalysis/Warddata/fort.11";
    int numSegs = 101;
    int numElems = 1500;

    seg_slipRate = new double[numSegs];
    seg_ddw = new double[numSegs];
    seg_code = new String[numSegs];
    seg_name = new String[numSegs];

    // element info(element number is the index in the following):
    elem_fltNum = new int[numElems];
    elem_segNum = new int[numElems];
    elem_length = new double[numElems];
    elem_azim = new double[numElems];
    elem_lat = new double[numElems];
    elem_lon = new double[numElems];


    try {
      ArrayList fileLines = FileUtils.loadFile(inputFileName);
      char charStr[];

      // read the segment stuff
      for(int i=0; i<numSegs; ++i){
        String line = (String) fileLines.get(i+1); // added 1 to skip first line
        charStr = new char[4];
        line.getChars(19,23,charStr,0);
        seg_slipRate[i] = Double.parseDouble(new String(charStr));
        charStr = new char[4];
        line.getChars(24,28,charStr,0);
        seg_ddw[i] = Double.parseDouble(new String(charStr));
        line.getChars(35,39,charStr,0);
        seg_code[i] = new String(charStr);
        int numChars = line.toCharArray().length;
        charStr = new char[numChars-41];
        line.getChars(41,numChars,charStr,0);
        seg_name[i] = (new String(charStr)).trim();
        System.out.println(i+"\t"+seg_slipRate[i]+"\t"+seg_ddw[i]+"\t"+seg_code[i]+"\t"+seg_name[i]);

      }

      // now read the element stuff
      for(int i=0; i<numElems; ++i){
        String line = (String)fileLines.get(i+1+numSegs); // added 1 to skip first line
        charStr = new char[3];
        line.getChars(8,11,charStr,0);
        elem_fltNum[i] = Integer.parseInt((new String(charStr)).trim());
        line.getChars(12,15,charStr,0);
        elem_segNum[i] = Integer.parseInt((new String(charStr)).trim());
        charStr = new char[8];
        line.getChars(40,48,charStr,0);
        elem_length[i] = Double.parseDouble((new String(charStr)).trim());
        charStr = new char[10];
        line.getChars(49,59,charStr,0);
        elem_azim[i] = Double.parseDouble((new String(charStr)).trim());
        charStr = new char[6];
        line.getChars(84,90,charStr,0);
        elem_lat[i] = Double.parseDouble((new String(charStr)).trim());
        charStr = new char[8];
        line.getChars(91,99,charStr,0);
        elem_lon[i] = Double.parseDouble((new String(charStr)).trim());
//        System.out.println(i+"\t"+elem_fltNum[i]+"\t"+elem_segNum[i]+"\t"+elem_length[i]+"\t"+
//                           elem_azim[i]+"\t"+elem_lat[i]+"\t"+elem_lon[i]);

      }
    }
    catch (FileNotFoundException ex) {
      ex.printStackTrace();
      System.exit(0);
    }
    catch (IOException ex) {
      ex.printStackTrace();
      System.exit(0);
}
  }



  private void read_FOR088_DAT() {
    String inputFileName = "javaDevelopers/ned/WardAnalysis/Warddata/FOR088.DAT";
    ArrayList fileLines = new ArrayList();
    try {
        //reading the file
        fileLines = FileUtils.loadFile(inputFileName);
    }
    catch (FileNotFoundException ex) {
        ex.printStackTrace();
        System.exit(0);
    }
    catch (IOException ex) {
        ex.printStackTrace();
        System.exit(0);
    }

    StringTokenizer st;
    int numEvents = fileLines.size();
    eventStartElem = new int[numEvents];
    eventEndElem = new int[numEvents];
    eventMo = new double[numEvents];
    eventMag = new double[numEvents];
    eventYear = new double[numEvents];
    for(int i=0; i<numEvents; ++i){
      st = new StringTokenizer( (String) fileLines.get(i));
      st.nextToken();
      st.nextToken();
      eventStartElem[i] = Integer.parseInt(st.nextToken().trim());
      eventEndElem[i] = Integer.parseInt(st.nextToken().trim());
      eventMo[i] = Double.parseDouble(st.nextToken().trim());
      eventMag[i] = Double.parseDouble(st.nextToken().trim());
      eventYear[i] = Double.parseDouble(st.nextToken().trim());

    }
  }


  /**
   * This writes out the date and amount of slip for each section (so the slips can
   * be plotted as segment number versus year)
   */
  private void writeSegNumYearSlip() throws IOException {
    FileWriter fw = new FileWriter("javaDevelopers/ned/RundleAnalysis/RundleVC_data/VC_segNumYearSlip.txt");
    SegmentSlipTimeInfo segInfo;
    ArrayList years, slips;
    int segNum;
    int numSegs = elemSlipInfoList.size();
    fw.write("segNum\tSegYear\tsegSlip\n");
    for(int j=0;j<numSegs;j++) { // loop over segments
      segInfo = (SegmentSlipTimeInfo) elemSlipInfoList.get(j);
      years = segInfo.getTimeHistories();
      slips = segInfo.getSlipHistories();
      segNum = segInfo.getSegmentNumber();
      for (int i = 0; i < years.size(); i++) {
          fw.write(segNum + "\t" + years.get(i) + "\t" + slips.get(i) + "\n");
      }
    }
    fw.close();
  }



  /**
   * This writes out the normalized recurrence intervals and the average recurrence interval
   * for each segment (excluding creeping section)
   */
  private void writeSegmentStats() throws IOException {
    FileWriter fw = new FileWriter("javaDevelopers/ned/RundleAnalysis/RundleVC_data/VC_segRecurIntervals.txt");
    SegmentSlipTimeInfo segInfo;
    ArrayList intervals;
    double aveRecur, normInt;
    int segNum;
//    for(int j=0;j<1;j++) { // loop over segments
    int numSegs = elemSlipInfoList.size();
    fw.write("segNumForInterval\tSegNormInterval\taveSegInterval\n");
    for(int j=0;j<numSegs;j++) { // loop over segments
      segInfo = (SegmentSlipTimeInfo) elemSlipInfoList.get(j);
      aveRecur = segInfo.getAveRecurrenceInterval();
      segNum = segInfo.getSegmentNumber();
      intervals = segInfo.getRecurrenceIntervals();
      if (!(segNum >= 264 && segNum <= 273)) // exclude creaping sections
        for (int i = 0; i < intervals.size(); i++) {
          normInt = ( (Integer) intervals.get(i)).doubleValue() / aveRecur;
          fw.write(segNum + "\t" + normInt + "\t" + aveRecur + "\n");
        }
    }
    fw.close();
  }




/*
  private void getEventStats() {
    Integer year, yearLastInt;
    double totArea, totPot, sum1, sum2, totPotRate, yearLast, slipLast, sumForT_last1, sumForT_last2;
    int seg;
    ArrayList segs;
    for(int i=0;i<eventYear.size();i++) {
      year = (Integer) eventYears.get(i);
      segs = (ArrayList) eventElems.get(i);
      totArea = 0.0;
      totPot  = 0.0;
      sumForT_last1 = 0.0;
      sumForT_last2 = 0.0;
      sum1 = 0.0;
      sum2 = 0;
      totPotRate = 0.;
      for(int j=0;j<segs.size();j++) { // loop over segments
        seg = ((Integer) segs.get(j)).intValue();
        SegmentSlipTimeInfo info = (SegmentSlipTimeInfo) elemSlipInfoList.get(seg);
        if(seg != info.getSegmentNumber())
          throw new RuntimeException("problem");
        totArea += elem_area[seg]*1e6;                    // converted from km to m-squared
        totPot += elem_area[seg]*Math.abs(info.getSlip(year))*1e4;  // converted to meters
        yearLastInt = info.getPreviousSlipTime(year);
        if (yearLastInt != null)
          yearLast = yearLastInt.doubleValue();
        else
          yearLast = Double.NaN;
        slipLast = info.getPreviousSlip(year); // will be NaN is not available
        sum1 += yearLast*Math.abs(seg_slipRate[seg])*elem_area[seg]*1e4;
        sum2 += elem_area[seg]*1e6*(Math.abs(slipLast)/Math.abs(seg_slipRate[seg])+yearLast);
        totPotRate += Math.abs(seg_slipRate[seg])*elem_area[seg]*1e4;
        sumForT_last1 += Math.abs(seg_slipRate[seg])*elem_area[seg]*1e4*yearLast;
        sumForT_last2 += elem_area[seg]*1e6*yearLast;
// if(year.intValue() == 25726)
//          System.out.println(seg+"\t"+elem_area[seg]+"\t"+info.getSlip(year)+"\t"+yearLast+"\t"+slipLast);
      }
      eventAveSlip[i]=(totPot/totArea);   //meters
      eventArea[i]=totArea;             //meters-sq
//      eventMag[i]= MomentMagCalc.getMag(FaultMomentCalc.getMoment(totArea,totPot/totArea));
      eventYearPred1[i] = (totPot+sum1)/totPotRate;
      eventYearPred2[i] = sum2/totArea;
      aveLastEvTime1[i] = sumForT_last1/totPotRate;
      aveLastEvTime2[i] = sumForT_last2/totArea;
// if(year.intValue() == 25726)
//        System.out.println(year+"\t"+eventAveSlips[i]+"\t"+eventAreas[i]+"\t"+eventMags[i]+
//            "\t"+eventYearPred1[i]+"\t"+eventYearPred2[i]+"\t"+aveLastEvTime1[i]+"\t"+aveLastEvTime2[i]);
    }
  }



  private void writeEventData() throws IOException  {
    String filename1 = "javaDevelopers/ned/RundleAnalysis/RundleVC_data/VC_EventTimesNumSegs.txt";
    String filename2 = "javaDevelopers/ned/RundleAnalysis/RundleVC_data/VC_EventSegs.txt";
    String evName;
    ArrayList tempSegs;
    Integer year;
    int lastYear=-1, counter=-1;

    FileWriter fw1 = new FileWriter(filename1);
    fw1.write("evTimes\tevNumSegs\tevMags\tevAreas\tevSlips\tevYearPred1\taveLastEvTime1\tevYearPred2\taveLastEvTime2\n");
    FileWriter fw2 = new FileWriter(filename2);
    fw2.write("evSegs\n");
    for(int i=0; i < eventYear.size(); i++) {
      year = (Integer) eventYears.get(i);
      if(year.intValue() != lastYear)
        counter = 0;
      else
        counter += 1;
      evName = year.toString()+"_"+Integer.toString(counter);
      lastYear = year.intValue();
      tempSegs = (ArrayList) eventElems.get(i);
      fw1.write(year+"\t"+tempSegs.size()+"\t"+(float)eventMag[i]+"\t"+(float)eventArea[i]+"\t"+
                (float)eventAveSlip[i]+"\t"+eventYearPred1[i]+"\t"+aveLastEvTime1[i]+
                "\t"+eventYearPred2[i]+"\t"+aveLastEvTime2[i]+"\n");
      fw2.write(evName+"\n");
      for(int j=0; j < tempSegs.size(); j++)
        fw2.write((Integer) tempSegs.get(j)+"\n");
    }
    fw1.close();
    fw2.close();
  }




  private boolean creepingNotInvolved(ArrayList newEvent) {
    boolean crNotInv = true;
    Iterator it = newEvent.iterator();
    int seg;
    while(it.hasNext()) {
      seg = ((Integer) it.next()).intValue();
      if (seg >= 264 && seg <= 273) crNotInv = false;
    }
    return crNotInv;
  }

*/


  public static void main(String[] args) {
    WardAnalysis analysis = new WardAnalysis();
  }
}
