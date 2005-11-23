package org.opensha.sha.calc.sort;

/**
 * <p>Title: DisaggregationSourceInfo</p>
 *
 * <p>Description: Stores the Source info. required for Disaggregation.</p>
 *
 * @author
 * @version 1.0
 */
public class DisaggregationSourceInfo {

  private String sourceName;
  private double sourceRate;
  private int sourceId;

  public DisaggregationSourceInfo(String srcName, double srcRate, int srcId) {

    sourceName = srcName;
    sourceRate = srcRate;
    sourceId = srcId;
  }


  public int getSourceId(){
    return sourceId;
  }


  public double getSourceRate(){
    return sourceRate;
  }

  public String getSourceName(){
    return sourceName;
  }


}
