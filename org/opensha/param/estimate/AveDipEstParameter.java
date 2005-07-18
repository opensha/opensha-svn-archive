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

public class AveDipEstParameter extends DoubleEstimateParameter {

   /** Class name for debugging. */
   protected final static String C = "AveDipEstParameter";

   public AveDipEstParameter( String name ) {
       this( name, null);
   }

   public AveDipEstParameter( String name, Estimate value ) {
     super(name, -90, 90);
     setUnits("degrees");
     setValue(value);
   }

}
