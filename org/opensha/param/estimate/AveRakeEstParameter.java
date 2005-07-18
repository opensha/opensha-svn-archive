package org.opensha.param.estimate;


import org.opensha.data.estimate.*;
/**
 * <p>Title: AveDipEstParameter.java </p>
 * <p>Description: average Dip Estimate Parameter. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AveRakeEstParameter extends DoubleEstimateParameter {

   /** Class name for debugging. */
   protected final static String C = "AveRakeEstParameter";

   public AveRakeEstParameter( String name ) {
       this( name, null);
   }

   public AveRakeEstParameter( String name, Estimate value ) {
     super(name, -180, 180);
     setUnits("degrees");
     setValue(value);
   }

}
