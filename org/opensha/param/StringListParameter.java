package org.opensha.param;

import java.util.ArrayList;
import org.opensha.exceptions.ConstraintException;
import org.opensha.exceptions.ParameterException;
import org.opensha.exceptions.EditableException;

/**
 * <p>Title: StringListParameter.java </p>
 * <p>Description: String List Parameter that accepts strings as it's values.
 * It is different from String Parameter because we can only set only one
 * String as the value for StringParameter. However, in this case, multiple Strings
 * can be set as value for this parameter.
 * The editor for this parameter is shown as a JList where multiple selections
 * are allowed.
 *
 * If constraints are present, setting the values must pass the constraint
 * check. Since the Parameter class in an ancestor, all Parameter's fields are
 * inherited. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class StringListParameter extends StringParameter {

  /** Class name for debugging. */
  protected final static String C = "StringListParameter";

  /**
   * Constructor doesn't specify a constraint, all values allowed. This
   * constructor sets the name of this parameter.
   */
  public StringListParameter( String name ) { super(name); }


     /**
      * Input vector is turned into StringConstraints object. If vector
      * contains no elements an exception is thrown. This constructor
      * also sets the name of this parameter.
      *
      * @param  name                     Name of the parametet
      * @param  strings                  Converted to the Constraint object
      * @exception  ConstraintException  Thrown if vector of allowed values is
      *      empty
      * @throws  ConstraintException     Thrown if vector of allowed values is
      *      empty
      */
     public StringListParameter( String name, ArrayList strings ) throws ConstraintException {
         this( name, new StringListConstraint( strings ), null, null );
     }


     /**
      * Constructor that sets the name and Constraint during initialization.
      *
      * @param  name                     Name of the parametet
      * @param  constraint               Constraint object
      * @exception  ConstraintException  Description of the Exception
      * @throws  ConstraintException     Is thrown if the value is not allowed
      */
     public StringListParameter( String name, StringListConstraint constraint ) throws ConstraintException {
         this( name, constraint, null, null );
     }




     /**
      * No constraints specified, all values allowed.
      * Sets the name, units and value.
      *
      * @param  name                     Name of the parameter
      * @param  units                    Units of the parameter
      * @param  value                    value of this parameter
      * @exception  ConstraintException  Description of the Exception
      */
     public StringListParameter( String name, String units, ArrayList values ) throws ConstraintException {
         this( name, null, units, values );
     }


     /**
      *  Sets the name, vector of string converted to a constraint, amd value.
      *
      * @param  name                     Name of the parametet
      * @param  strings                  vector of allowed values converted to a
      *      constraint
      * @param  value                    value of this parameter
      * @exception  ConstraintException  Is thrown if the value is not allowed
      * @throws  ConstraintException     Is thrown if the value is not allowed
      */
     public StringListParameter( String name, ArrayList strings, ArrayList values ) throws ConstraintException {
         this( name, new StringListConstraint( strings ), null, values );
     }


     /**
      *  Sets the name, constraint, and value.
      *
      * @param  name                     Name of the parametet
      * @param  constraint               List of allowed values
      * @param  value                    value of this parameter
      * @exception  ConstraintException  Is thrown if the value is not allowed
      * @throws  ConstraintException     Is thrown if the value is not allowed
      */
     public StringListParameter( String name, StringListConstraint constraint, ArrayList values ) throws ConstraintException {
         this( name, constraint, null, values );
     }



     /**
      *  This is the main constructor. All other constructors call this one.
      *  Constraints must be set first, because the value may not be an allowed
      *  one. Null values are always allowed in the constructor. All values are
      *  set in this constructor; name, value, units, and constructor
      *
      * @param  name                     Name of the parametet
      * @param  constraint               Lsit of allowed values
      * @param  value                    value object of this parameter
      * @param  units                    Units of this parameter
      * @exception  ConstraintException  Is thrown if the value is not allowed
      * @throws  ConstraintException     Is thrown if the value is not allowed
      */
     public StringListParameter( String name, StringListConstraint constraint, String units, ArrayList values )
              throws ConstraintException {
         super( name, constraint, units);
         this.value = values;
     }

     /**
     * Sets the constraint reference if it is a StringListConstraint
     * and the parameter is currently editable, else throws an exception.
     */
    public void setConstraint(ParameterConstraintAPI constraint) throws ParameterException, EditableException{

        String S = C + ": setConstraint(): ";
        checkEditable(S);

        if ( !(constraint instanceof StringListConstraint )) {
            throw new ParameterException( S +
                "This parameter only accepts StringConstraints, unable to set the constraint."
            );
        }
        else super.setConstraint( constraint );
    }

    /**
     * Gets the type attribute of the StringListParameter object. Returns
     * the class name if unconstrained, else "Constrained" + classname.
     * This is used to determine which type of GUI editor applies to this
     * parameter.
     *
     * @return    The GUI editor type
     */
    public String getType() {
        String type = C;
        // Modify if constrained
        ParameterConstraintAPI constraint = this.constraint;
        if (constraint != null) type = "Constrained" + type;
        return type;
    }

    /**
     * Compares the values to if this is less than, equal to, or greater than
     * the comparing objects. Implementation of comparable interface. Helps
     * with sorting a list of parameters.
     *
     * @param  obj                     The object to compare this to
     * @return                         -1 if this value < obj value, 0 if equal,
     *      +1 if this value > obj value
     * @exception  ClassCastException  Is thrown if the comparing object is not
     *      a StringParameter *
     * @see                            Comparable
     */
    public int compareTo( Object obj ) throws ClassCastException {

        String S = C + ":compareTo(): ";

        if ( !( obj instanceof StringListParameter ) ) {
            throw new ClassCastException( S + "Object not a StringListParameter, unable to compare" );
        }

        StringListParameter param = ( StringListParameter ) obj;

        if( ( this.value == null ) && ( param.value == null ) ) return 0;
        int result = 0;

        ArrayList n1 = ( ArrayList ) this.getValue();
        ArrayList n2 = ( ArrayList ) param.getValue();

        if (n1.containsAll( n2 ) && n2.containsAll(n1)) return 0;
        else return -1;
    }


    /**
     * Compares the passed in StringList parameter to see if it has
     * the same name and value. If the object is not a StringList parameter
     * an exception is thrown. If the values and names are equal true
     * is returned, otherwise false is returned.
     *
     * @param  obj                     The object to compare this to
     * @return                         True if the values are identical
     * @exception  ClassCastException  Is thrown if the comparing object is not
     *      a StringParameter
     */
    public boolean equals( Object obj ) throws ClassCastException {
        String S = C + ":equals(): ";

        if ( !( obj instanceof StringListParameter ) ) {
            throw new ClassCastException( S + "Object not a StringListParameter, unable to compare" );
        }

        String otherName = ( ( StringListParameter ) obj ).getName();
        if ( ( compareTo( obj ) == 0 ) && getName().equals( otherName ) ) {
            return true;
        }
        else { return false; }
    }


    /**
     *  Returns a copy so you can't edit or damage the origial.
     * Clones this object's value and all fields. The constraints
     * are also cloned.
     *
     * @return    Description of the Return Value
     */
    public Object clone() {

      StringListConstraint c1=null;
      if(constraint != null)
         c1 = ( StringListConstraint ) constraint.clone();

        StringListParameter param = null;
        if( value == null )  {
          param = new StringListParameter(name, c1);
          param.setUnits(units);
        }
        else param = new StringListParameter( name, c1, units,(ArrayList) (((ArrayList)this.value).clone()));
        if( param == null ) return null;
        param.editable = true;
        return param;

    }

}