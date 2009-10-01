package org.opensha.sha.calc.disaggregation;

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
  private double mag;
  private double distance;
  private double epsilon;
  private double rake;
  private int id;

  public DisaggregationSourceRuptureInfo(String name, double rate, int id) {

    this.name = name;
    this.rate = rate;
    this.id = id;
  }

  public DisaggregationSourceRuptureInfo(String name, double eventRate, double rate,
                                         int id,double mag,double distance) {
   this.name = name;
   this.rate = rate;
   this.id = id;
   this.eventRate = eventRate;
   this.mag = mag;
   this.distance = distance;
 }


  public DisaggregationSourceRuptureInfo(String name, double eventRate, double rate, int id) {

    this.name = name;
    this.rate = rate;
    this.id = id;
    this.eventRate = eventRate;
  }
  
  public DisaggregationSourceRuptureInfo(String name, double eventRate, int id,double mag,double distance, double epsilon, double rake) {
	this.name = name;
	this.id = id;
	this.eventRate = eventRate;
	this.mag = mag;
	this.distance = distance;
	this.epsilon = epsilon;
	this.rake = rake;
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

  public double getMag(){
    return mag;
  }

  public double getDistance(){
	    return distance;
  }
  
  public double getEpsilon(){
	    return epsilon;
  }
  
  public double getAveRake(){
	    return rake;
  }
}
