package org.scec.param.estimate;

import org.scec.param.IntegerConstraint;
import org.scec.data.estimate.IntegerEstimate;

/**
 * <p>Title: NumEventsEstParameter.java </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class NumEventsEstParameter extends EstimateParameter {
  private final static String units = "";
  private final static IntegerConstraint constraint = new IntegerConstraint(0, Integer.MAX_VALUE);

   /** Class name for debugging. */
   protected final static String C = "NumEventsEstParameter";
   /** If true print out debug statements. */
   protected final static boolean D = false;


   public NumEventsEstParameter( String name ) {
       this( name, null);
   }

    public NumEventsEstParameter( String name, IntegerEstimate value ) {
        super(name, constraint, units, value);
   }

   /**
   * Determine if the new value being set is allowed by validating
   * against the constraints.
   *
   * @param  val  Object to check if allowed via constraints
   * @return      True if the value is allowed
   */
  public boolean isAllowed( Object val ){
      if(val!=null && !(val instanceof IntegerEstimate)) return false;
      return true;
  }

}