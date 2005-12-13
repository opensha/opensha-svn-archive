package org.opensha.sha.calc.sort;

/**
 * <p>Title: DisaggregationSourceInfo</p>
 *
 * <p>Description: Stores the Source info. required for Disaggregation.</p>
 *
 * @author
 * @version 1.0
 */
public class DisaggregationSourceRuptureInfo {

  private String name;
  private double rate;
  private double eventRate;
  private int id;

  public DisaggregationSourceRuptureInfo(String name, double rate, int id) {

    this.name = name;
    this.rate = rate;
    this.id = id;
  }


  public DisaggregationSourceRuptureInfo(String name, double eventRate, double rate, int id) {

    this.name = name;
    this.rate = rate;
    this.id = id;
    this.eventRate = eventRate;
  }

  public int getId(){
    return id;
  }


  public double getRate(){
    return rate;
  }

  public String getName(){
    return name;
  }

  public double getEventRate(){
    return eventRate;
  }

}
