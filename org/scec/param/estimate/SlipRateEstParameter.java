package org.scec.param.estimate;

import org.scec.param.DoubleConstraint;
import org.scec.data.estimate.EstimateAPI;
/**
 * <p>Title: SlipRateEstParameter.java </p>
 * <p>Description: Slip Rate Estimate Parameter. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class SlipRateEstParameter extends EstimateParameter {

  private final static String units = "cm/yr";
  private final static DoubleConstraint constraint = new DoubleConstraint(0, Double.MAX_VALUE);

  /** Class name for debugging. */
  protected final static String C = "SlipRateEstParameter";
  /** If true print out debug statements. */
  protected final static boolean D = false;



   public SlipRateEstParameter( String name ) {
       this( name, null);
   }

   public SlipRateEstParameter( String name, EstimateAPI value ) {
       super(name, constraint, units, value);
   }

}