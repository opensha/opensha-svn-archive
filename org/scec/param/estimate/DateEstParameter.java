package org.scec.param.estimate;

/**
 * <p>Title: DateEstParameter.java </p>
 * <p>Description: Date Estimate Parameter </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class DateEstParameter extends EstimateParameter {
  public final static int BC = 1;
  public final static int AD = 2;

  private final String UNITS = "years";
  private int era = AD;

  public DateEstParameter(String name) {
    super(name);
  }

 }