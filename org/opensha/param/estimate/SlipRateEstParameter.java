package org.opensha.param.estimate;


import org.opensha.data.estimate.*;
import java.util.ArrayList;
/**
 * <p>Title: SlipRateEstParameter.java </p>
 * <p>Description: Slip Rate Estimate Parameter. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class SlipRateEstParameter extends EstimateParameter {

   /** Class name for debugging. */
   protected final static String C = "SlipRateEstParameter";

   public SlipRateEstParameter( String name ) {
       this( name, null);
   }

   public SlipRateEstParameter( String name, Estimate value ) {
     super(name, 0, Double.MAX_VALUE);
     setUnits("mm/yr");
     // negative values are not allowed. so, normal and lognormal are not allowed
     setConstraint(EstimateConstraint.createConstraintForPositiveDoubleValues());
     setValue(value);
   }

}
