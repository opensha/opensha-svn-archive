package org.scec.param.estimate;

import org.scec.data.estimate.Estimate;

/**
 * <p>Title: DateEstParameter.java </p>
 * <p>Description: Date Estimate Parameter </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class DateEstParameter extends DoubleEstimateParameter {
  public final static int BC = 1;
  public final static int AD = 2;

  private final String UNITS = "years";
  private int era = AD;

  public DateEstParameter(String name) {
    this(name,null, AD);
  }

  public DateEstParameter(String name, Estimate value, int era) {
    super(name, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    setUnits("years");
    setEra(era);
  }

  /**
   * Set the era. Era can have value DateEstParameter.AD or it can have
   * value DateEstParameter.BC
   *
   * @param era
   */
  public void setEra(int era) {
    if(era!=AD && era!=BC) {
      throw new RuntimeException("Invalid era value");
    }
    this.era  =era;
  }

  // get the era.
  public int getEra() { return era; }

 }