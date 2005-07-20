package org.opensha.param.estimate;


import org.opensha.data.estimate.*;
import java.util.ArrayList;

/**
 * <p>Title: NumEventsEstParameter.java </p>
 * <p>Description: This is an integer Estimate parameter. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class NumEventsEstParameter extends EstimateParameter {
   /** Class name for debugging. */
   protected final static String C = "NumEventsEstParameter";

   public NumEventsEstParameter( String name ) {
       this( name, null);
   }

    public NumEventsEstParameter( String name, IntegerEstimate value) {
        super(name, "", 0, Integer.MAX_VALUE, EstimateConstraint.createConstraintForPositiveIntValues());
        setValue(value);
   }
}
