package javaDevelopers.ned;

import java.util.*;

/**
 * <p>Title: SegmentSlipTimeInfo</p>
 *
 * <p>Description: This class stores the Segment Slip time histories, sorting the list of
 * times/slips in order of time.</p>
 *
 * @author Edward (NEd) Field
 * @version 1.0
 */
public class SegmentSlipTimeInfo {


  //Segment number
  private int segmentNumber;
  //Mapping between the Time Pd and Slip during that time pd. at that segment number
  private TreeMap slipTimeHistoriesMap;


  /**
   * Class constructor
   * @param segmentNum int
   * @param timeHist ArrayList
   * @param slipHist ArrayList
   */
  public SegmentSlipTimeInfo(int segmentNum,ArrayList timeHist, ArrayList slipHist){
    segmentNumber = segmentNum;
    createSlipTimeSortedFunction(timeHist,slipHist);
  }


  /**
   * Set all the segment slip time info
   * @param segmentNum int
   * @param timeHist ArrayList
   * @param slipHist ArrayList
   */
  public void setAllForSegment(int segmentNum, ArrayList timeHist, ArrayList slipHist){
    segmentNumber = segmentNum;
    createSlipTimeSortedFunction(timeHist,slipHist);
  }


  /**
   * This function creates a sorted Slip time history function for a given segment number
   */
  private void createSlipTimeSortedFunction(ArrayList timeHist, ArrayList slipHist){
    slipTimeHistoriesMap = new TreeMap();
    int size = timeHist.size();
    for(int i=0;i<size;++i)
      slipTimeHistoriesMap.put(timeHist.get(i),slipHist.get(i));

  }

  /**
   * Return the Segment number
   * @return int
   */
  public int getSegmentNumber(){
    return segmentNumber;
  }

  /**
   * Return the Time histories for this segment
   * @return ArrayList
   */
  public ArrayList getTimeHistories(){
    Set timeHistSet = slipTimeHistoriesMap.keySet();
    Iterator it = timeHistSet.iterator();
    ArrayList timeHistList = new ArrayList();
    while(it.hasNext())
      timeHistList.add(it.next());
    return timeHistList;
  }


  /**
   * Return Slip histories occured during the given time pds on the segment.
   * @return ArrayList
   */
  public ArrayList getSlipHistories(){
    Set timeHistSet = slipTimeHistoriesMap.keySet();
    Iterator it = timeHistSet.iterator();
    ArrayList slipHistList = new ArrayList();
    while(it.hasNext())
      slipHistList.add(slipTimeHistoriesMap.get(it.next()));

    return slipHistList;

  }

  /**
   * This returns the slip corresponding the given year
   */
  public double getSlip(Integer year){
    return ((Double) slipTimeHistoriesMap.get(year)).doubleValue();
  }

}
