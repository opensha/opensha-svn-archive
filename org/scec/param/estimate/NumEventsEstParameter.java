package org.scec.param.estimate;

import org.scec.param.IntegerConstraint;
import org.scec.data.estimate.EstimateAPI;

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

    public NumEventsEstParameter( String name, EstimateAPI value ) {
        super(name, constraint, units, value);
   }

}