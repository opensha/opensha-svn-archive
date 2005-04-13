package org.opensha.param.estimate;
import java.util.ArrayList;
import org.opensha.param.DoubleEstimateConstraint;
import org.opensha.exceptions.ConstraintException;
/**
 * <p>Title: DoubleEstimateParameter.java </p>
 * <p>Description: Estimate parameter in which double values are allowed</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class DoubleEstimateParameter extends EstimateParameter{

  /**
   * Constructor with min/max specified. All types of Estimates classes
   * are allowed
   * Also Sets the name of this parameter.
   */
  public DoubleEstimateParameter( String name, double min, double max ) {
    this(name,min,max,null);
  }

  /**
   * Constructor with min/max specified.
   * It also accepts an arraylist of String which are classnames of allowed
   * estimate types
   * Also Sets the name of this parameter.
   */
  public DoubleEstimateParameter( String name, double min, double max, ArrayList allowedEstimateTypes ) {
    super(name, new DoubleEstimateConstraint(min,max,allowedEstimateTypes), null, null);
  }

  /**
   * Constructor with min/max specified.
   * Sets the name and units of this parameter.
   */
  public DoubleEstimateParameter( String name, String units, double min, double max) throws ConstraintException {
      this(name,units,min,max,null);
  }

  /**
   * Constructor with min/max and list of allowed Estimate types specified
   * Sets the name and units of this parameter.
   */
  public DoubleEstimateParameter( String name, String units, double min, double max, ArrayList allowedEstimateTypes) throws ConstraintException {
      super(name, new DoubleEstimateConstraint(min,max,allowedEstimateTypes), null, null);
  }


}
