package org.scec.param.estimate;
import org.scec.data.estimate.EstimateAPI;

/**
 * <p>Title: DisplacementEstParameter.java </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class DisplacementEstParameter extends SlipRateEstParameter{
  public DisplacementEstParameter( String name ) {
       this( name, null);
   }
  public DisplacementEstParameter( String name, EstimateAPI value ) {
       super(name,  value);
  }
}