package org.scec.param.estimate;

import org.scec.param.*;
import org.scec.exceptions.*;
import org.scec.data.estimate.EstimateAPI;


/**
 * <p>Title: EstimateParameter.java </p>
 * <p>Description: </p>
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
       this( name, null, null);
   }


   /**
    * Constructor with no No constraints specified, all values are allowed.
    * Sets the name and untis of this parameter.
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
   public EstimateParameter( String name, EstimateAPI value ) {
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
   public EstimateParameter( String name, ParameterConstraint constraint,
                             String units, EstimateAPI value ) throws ConstraintException {
       super( name, null, units, value );
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
   public EstimateParameter( String name, String units, EstimateAPI value ) throws ConstraintException {
       this( name, null, units, value );
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




  public int compareTo(Object parm1) {
    /**@todo Implement this org.scec.param.Parameter abstract method*/
    throw new java.lang.UnsupportedOperationException(
        "Method compareTo() not yet implemented.");
  }

  public Object clone() {
    /**@todo Implement this org.scec.param.Parameter abstract method*/
    throw new java.lang.UnsupportedOperationException(
        "Method clone() not yet implemented.");
  }


}