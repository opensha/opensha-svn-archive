package org.opensha.param.estimate;

import org.opensha.param.DoubleEstimateConstraint;
import org.opensha.data.estimate.*;
import java.util.ArrayList;
/**
 * <p>Title: DisplacementEstParameter.java </p>
 * <p>Description: Displacement Estimate Parameter. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class DisplacementEstParameter extends DoubleEstimateParameter {

   /** Class name for debugging. */
   protected final static String C = "DisplacementEstParameter";

   public DisplacementEstParameter( String name ) {
       this( name, null);
   }

   public DisplacementEstParameter( String name, Estimate value ) {
     super(name, 0, Double.MAX_VALUE);
     setUnits("cm/yr");
     // negative values are not allowed. so, normal and lognormal are not allowed
     setConstraint(DoubleEstimateConstraint.createConstraintForPositiveAllowedValues());
     setValue(value);
   }

}
