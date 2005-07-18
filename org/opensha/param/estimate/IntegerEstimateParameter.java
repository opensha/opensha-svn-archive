package org.opensha.param.estimate;
import java.util.ArrayList;

import org.opensha.exceptions.ConstraintException;
/**
 * <p>Title: DoubleEstimateParameter.java </p>
 * <p>Description: Estimate parameter in which double values are allowed</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class IntegerEstimateParameter extends EstimateParameter{



  /**
   * Constructor with min/max specified.
   * It also accepts an arraylist of String which are classnames of allowed
   * estimate types
   * Also Sets the name of this parameter.
   */
  public IntegerEstimateParameter( String name, int min, int max ) {
    super(name, new IntegerEstimateConstraint(min,max), null, null);
  }


  /**
   * Constructor with min/max and list of allowed Estimate types specified
   * Sets the name and units of this parameter.
   */
  public IntegerEstimateParameter( String name, String units, int min, int max) throws ConstraintException {
      super(name, new IntegerEstimateConstraint(min,max), null, null);
  }


}
