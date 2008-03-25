package org.opensha.param.estimate;

import org.dom4j.Element;
import org.opensha.param.*;
import org.opensha.exceptions.*;
import org.opensha.data.estimate.Estimate;
import java.util.ArrayList;



/**
 * <p>Title: EstimateParameter.java </p>
 * <p>Description: EstimateParameter wraps the Estimate object. constraints are specified
 * in EstimateConstraint which is alist of allowed estimate type names. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class EstimateParameter extends DependentParameter
    implements DependentParameterAPI, ParameterAPI {


  /** Class name for debugging. */
   protected final static String C = "EstimateParameter";
   /** If true print out debug statements. */
   protected final static boolean D = false;


   /**
    * Constructor with no constraints specified, all values are allowed.
    * Also Sets the name of this parameter.
    */
   public EstimateParameter( String name ) {
       super( name, null, null, null);
   }



   /**
    * Constructor with no No constraints specified, all values are allowed.
    * Sets the name and units of this parameter.
    */
   public EstimateParameter( String name, String units ) throws ConstraintException {
       this( name,  units, null );
   }



   /**
    *  No constraints specified, all values allowed. Sets the name and value.
    *
    * @param  name   Name of the parameter
    * @param  value  Integer value of this parameter
    */
   public EstimateParameter( String name, Estimate value ) {
       this(name,  null, value);
   }


   /**
    *  Sets the name, units and value.
    *
    * @param  name                     Name of the parametet
    * @param  value                    Integer value of this parameter
    * @param  units                    Units of this parameter
    * @exception  ConstraintException  thrown if the value is not allowed
    * @throws  ConstraintException     Is thrown if the value is not allowed
    */
   public EstimateParameter( String name, EstimateConstraint constraint,
                             String units, Estimate value ) throws ConstraintException {
       super( name, constraint, units, value );
   }




   /**
    *  Sets the name, units and value. All values allowed because constraints
    *  not set.
    *
    * @param  name                     Name of the parametet
    * @param  value                    Integer value of this parameter
    * @param  units                    Units of this parameter
    * @exception  ConstraintException  thrown if the value is not allowed
    * @throws  ConstraintException     Is thrown if the value is not allowed
    */
   public EstimateParameter( String name, String units, Estimate value ) throws ConstraintException {
       super( name, null, units, value );
   }

   /**
    * Constructor with min/max specified. All types of Estimates classes
    * are allowed
    * Also Sets the name of this parameter.
    */
   public EstimateParameter(String name, double min, double max) {
     this(name, min, max, null);
   }

   /**
    * Constructor with min/max specified.
    * It also accepts an arraylist of String which are classnames of allowed
    * estimate types
    * Also Sets the name of this parameter.
    */
   public EstimateParameter(String name, double min, double max,
                            ArrayList allowedEstimateTypes) {
     super(name, new EstimateConstraint(min, max, allowedEstimateTypes), null, null);
   }

   /**
    * Constructor with min/max specified.
    * Sets the name and units of this parameter.
    */
   public EstimateParameter(String name, String units, double min, double max) throws
       ConstraintException {
     this(name, units, min, max, null);
   }

/**
 * Constructor with min/max and list of allowed Estimate types specified
 * Sets the name and units of this parameter.
 */
public EstimateParameter( String name, String units, double min, double max, ArrayList allowedEstimateTypes) throws ConstraintException {
    super(name, new EstimateConstraint(min,max,allowedEstimateTypes), units, null);
}



  /**
    * Returns the type of this parameter. The type is just the classname
    * if no constraints are present, else "Constrained" is prepended to the
    * classname. The type is used to determine which parameter GUI editor
    * to use.
    */
   public String getType() {
       String type = C;
       // Modify if constrained
       ParameterConstraintAPI constraint = this.constraint;
       if (constraint != null) type = "Constrained" + type;
       return type;
   }

   /**
    * Determine if the new value being set is allowed by validating
    * against the constraints.
    *
    * @param  val  Object to check if allowed via constraints
    * @return      True if the value is allowed
    */
   public boolean isAllowed( Object val ){
       if(val!=null && !(val instanceof Estimate)) return false;
       // check that estimate object and parameter have same units
       /*Estimate estimate = (Estimate)val;
       if (estimate.getUnits() == null && this.units != null)
         return false;
       else if (this.units == null && estimate.getUnits() != null)
         return false;
       else if (this.units != null && estimate.getUnits() != null &&
                !units.equalsIgnoreCase(estimate.getUnits()))
         return false;*/
     return super.isAllowed(val);
   }



  public int compareTo(Object parm1) {
    /**@todo Implement this org.opensha.param.Parameter abstract method*/
    throw new java.lang.UnsupportedOperationException(
        "Method compareTo() not yet implemented.");
  }

  public Object clone() {
    /**@todo Implement this org.opensha.param.Parameter abstract method*/
    throw new java.lang.UnsupportedOperationException(
        "Method clone() not yet implemented.");
  }


public boolean setValueFromXMLMetadata(Element el) {
	// TODO Auto-generated method stub
	return false;
}

}
