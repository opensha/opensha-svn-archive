package org.scec.param.estimate;

import org.scec.param.DoubleEstimateConstraint;
import org.scec.data.estimate.*;
/**
 * <p>Title: DepthEstParameter.java </p>
 * <p>Description: Depth Estimate Parameter. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class DepthEstParameter extends DoubleEstimateParameter {

   /** Class name for debugging. */
   protected final static String C = "DepthEstParameter";

   public DepthEstParameter( String name ) {
       this( name, null);
   }

   public DepthEstParameter( String name, Estimate value ) {
     super(name, 0, Double.MAX_VALUE);
     setUnits("km");
     // negative values are not allowed. so, normal and lognormal are not allowed
     setConstraint(DoubleEstimateConstraint.createConstraintForPositiveAllowedValues());
     setValue(value);
   }

}
