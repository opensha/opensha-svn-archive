package org.scec.param;
import org.scec.data.estimate.Estimate;
import org.scec.exceptions.EditableException;
import java.util.ArrayList;

/**
 * <p>Title: InetgerEstimateConstraint.java </p>
 * <p>Description: A InetgerEstimateConstraint represents a range of allowed
 * values between a min and max integer value, inclusive and a list of allowed
 * Estimate types.
 * The main purpose of this class is to call isAllowed() which will return true
 * if the value is an object of one of the allowed Estimate types and
 * all the values are withing the range.  See the
 * InetgerConstraint javadocs for further documentation. <p>
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class IntegerEstimateConstraint extends IntegerConstraint {
    /** Class name for debugging. */
    protected final static String C = "IntegerEstimateConstraint";
    /** If true print out debug statements. */
    protected final static boolean D = false;

    /** It contains a list of Strings specifying the classnames of allowed Estimates */
    protected StringConstraint allowedEstimateList=null;

    /** No-Arg Constructor, constraints are null so all values allowed and
     * all estimate objects are allowed
     */
    public IntegerEstimateConstraint() {}


    /**
     * Accepts a list of classnames of allowed estimate objects. There is no
     * constraint on min and max values and there are assumed to be null in this case.
     *
     * @param allowedEstimateList List of classnames of allowed Estimate objects
     */
    public IntegerEstimateConstraint(StringConstraint allowedEstimateList) {
      super();
      setAllowedEstimateList(allowedEstimateList);
    }


    /**
     * Sets the min and max values, and a list of classnames of allowed estimate
     * types in this constraint.
     * No checks are performed that min and max are
     * consistant with each other.
     *
     * @param  min  The min value allowed
     * @param  max  The max value allowed
     * @param allowedEstimateList List of classnames of allowed Estimate objects
     */
    public IntegerEstimateConstraint( int min, int max, StringConstraint allowedEstimateList) {
        this(new Integer(min), new Integer(max), allowedEstimateList);
    }


    /** Sets the min and max values, and a list of classnames of allowed estimate
     * types in this constraint.
     * No checks are performed that min and max are
     * consistant with each other.
     *
     * @param  min  The min value allowed
     * @param  max  The max value allowed
     * @param allowedEstimateList List of classnames of allowed Estimate objects
     */
    public IntegerEstimateConstraint( Integer min, Integer max, StringConstraint allowedEstimateList ) {
        super(min,max);
        setAllowedEstimateList(allowedEstimateList);
    }

    /**
     * Set list of allowed Estimate classnames.
     *
     * @param allowedEstimateList object containing list of strings specfying classnames
     * of allowed estimates
     *
     * @throws EditableException This exception is thrown if this constraint is
     * non-editable but user tries to call this function
     */
    public void setAllowedEstimateList(StringConstraint allowedEstimateList) throws EditableException{
      String S = C + ": setAllowedEstimateList(StringConstraint): ";
      checkEditable(S);
      this.allowedEstimateList = allowedEstimateList;

    }

    /**
     * Get a list of classnames of allowed estimates
     *
     * @return StringConstraint object containing list of strings specfying classnames
     * of allowed estimates
     */
    public StringConstraint getAllowedEstimateList() {
      return allowedEstimateList;
    }




    /**
     * This function first checks whether null values are allowed and if passed
     * in value is a null value. If null values are allowed and passed in value
     * is null value, it returns true. If null values are not allowed and passed
     * in value is a null value, it return false.
     *
     * Then this function checks whether passed in value is an object of Estimate.
     * If it is not an object of estimate, false is returned else it calls
     * another function isAllowed(Estimate) to check whether this value of
     * Estimate is allowed.
     *
     *
     * @param  obj  The object to check if allowed.
     * @return      True if this is a Estimate and one of the allowed values.
     */
    public boolean isAllowed( Object obj ) {
        if( nullAllowed && ( obj == null ) ) return true;
        else if ( !( obj instanceof Estimate ) ) return false;
        else return isAllowed( ( Estimate ) obj );
    }


    /**
     *
     * This function checks first checks that estimate object is one of the
     * allowed estimates. Then it compares the min and max value of constraint
     * with the min and max value from the estimate.
     *
     * @param estimate
     * @return
     */
    public boolean isAllowed(Estimate estimate) {
      if(this.allowedEstimateList==null) return true;
      // get list of class names for allowed estimate values
      ArrayList list = allowedEstimateList.getAllowedStrings();
      for(int i=0;i<list.size();++i) {
        String classname = (String)list.get(i);
        if(estimate.getClass().getName().equalsIgnoreCase(classname)) {
          // if this object is among list of allowed estimates, check min/max value
          double allowedMinValue = this.min.intValue();
          double allowedMaxValue = this.max.intValue();
          if(estimate.getMinXValue()>=allowedMinValue &&
            estimate.getMaxXValue()<=allowedMaxValue)
           return true;
         break;
        }
      }
      // return false if this object is not among list of allowed estimate classes
      return false;
    }


    /**
     *
     * This function always returns false because this constraint only accepts
     * Estimate objects. Any other type of objects are not allowed
     *
     * @param  d  The object to check if allowed.
     * @return   Always return false as Double values are not allowed
     */
    public boolean isAllowed( Integer i ) {
        return isAllowed((Object)i);
    }


    /**
     *
     * This function always returns false because this constraint only accepts
     * Estimate objects. Any other type of objects are not allowed
     *
     * @param  d  The value to check if allowed.
     * @return   Always return false as double values are not allowed
     */
    public boolean isAllowed( int i ) { return isAllowed( new Integer( i ) ); }


    /** returns the classname of the constraint, and the min & max as a debug string */
    public String toString() {
        String TAB = "    ";
        StringBuffer b = new StringBuffer();
        b.append(super.toString());
        if(allowedEstimateList!=null) b.append(TAB+"Allowed Estimates = "+this.allowedEstimateList.toString());
        return b.toString();
    }


    /** Creates a copy of this object instance so the original cannot be altered. */
    public Object clone() {
        IntegerEstimateConstraint c1 = new IntegerEstimateConstraint( min, max,
            (StringConstraint)this.allowedEstimateList.clone());
        c1.setName( name );
        c1.setNullAllowed( nullAllowed );
        c1.editable = true;
        return c1;
    }
}
