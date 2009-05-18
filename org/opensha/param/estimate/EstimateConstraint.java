package org.opensha.param.estimate;
import org.opensha.commons.data.estimate.DiscreteValueEstimate;
import org.opensha.commons.data.estimate.Estimate;
import org.opensha.commons.data.estimate.FractileListEstimate;
import org.opensha.commons.data.estimate.IntegerEstimate;
import org.opensha.commons.data.estimate.MinMaxPrefEstimate;
import org.opensha.commons.data.estimate.NormalEstimate;
import org.opensha.commons.data.estimate.PDF_Estimate;
import org.opensha.commons.exceptions.EditableException;


import java.util.ArrayList;
import org.opensha.param.*;

/**
 * <p>Title: DoubleEstimateConstraint.java </p>
 * <p>Description: A DoubleEstimateConstraint represents a range of allowed
 * values between a min and max double value, inclusive and a list of allowed
 * Estimate types.
 * The main purpose of this class is to call isAllowed() which will return true
 * if the value is an object of one of the allowed Estimate types and
 * all the values are within the range specified here.  See the
 * DoubleConstraint javadocs for further documentation. <p>
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class EstimateConstraint extends DoubleConstraint {
    /** Class name for debugging. */
    protected final static String C = "DoubleEstimateConstraint";
    /** If true print out debug statements. */
    protected final static boolean D = false;

    /** It contains a list of Strings specifying the classnames of allowed Estimates */
    protected StringConstraint allowedEstimateList=null;

    /** No-Arg Constructor, constraints are null so all values allowed and
     * all estimate objects are allowed
     */
    public EstimateConstraint() {}


    /**
     * Accepts a list of classnames of allowed estimate objects. There is no
     * constraint on min and max values and there are assumed to be null in this case.
     *
     * @param allowedEstimateList List of classnames of allowed Estimate objects
     */
    public EstimateConstraint(ArrayList allowedEstimateList) {
      super();
      setAllowedEstimateList(allowedEstimateList);
    }

    /**
     * Accepts min/max values. There is no constraint on Estimate type and so
     * all the estimate types are allowed.
     *
     * @param min
     * @param max
     */
    public EstimateConstraint(double min, double max) {
      this(min,max,null);
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
    public EstimateConstraint( double min, double max, ArrayList allowedEstimateList) {
        this(new Double(min), new Double(max), allowedEstimateList);
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
    public EstimateConstraint( Double min, Double max, ArrayList allowedEstimateList ) {
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
    public void setAllowedEstimateList(ArrayList allowedEstimateList) throws EditableException{
      String S = C + ": setAllowedEstimateList(StringConstraint): ";
      checkEditable(S);
      this.allowedEstimateList = new StringConstraint(allowedEstimateList);

    }

    /**
     * Get a list of classnames of allowed estimates
     *
     * @return StringConstraint object containing list of strings specfying classnames
     * of allowed estimates
     */
    public ArrayList getAllowedEstimateList() {
      return allowedEstimateList.getAllowedStrings();
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
     * This function first checks that estimate object is one of the
     * allowed estimates. Then it compares the min and max value of constraint
     * with the min and max value from the estimate (the latter must be within
     * the former). It also makes sure that both,
     * Estimate object and EstimateParameter have same units
     *
     * @param estimate
     * @return
     */
    public boolean isAllowed(Estimate estimate) {
      if(this.allowedEstimateList==null) return true;
      // get list of class names for allowed estimate values
      ArrayList list = allowedEstimateList.getAllowedStrings();
      for(int i=0;i<list.size();++i) {
        String name = (String)list.get(i);
        if(estimate.getName().equalsIgnoreCase(name)) {
          // if this object is among list of allowed estimates, check min/max value
          Double estimateMinValue = new Double(estimate.getMin());
          Double estimateMaxValue = new Double(estimate.getMax());
          if(min==null && max==null) return true;
          if(min!=null && max!=null && estimateMinValue.compareTo(min)>=0 &&  estimateMaxValue.compareTo(max)<=0)
            return true;
          break;
        }
      }
      // return false if this object is not among list of allowed estimate classes
      return false;
    }


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
        EstimateConstraint c1 = new EstimateConstraint( min, max,
            (ArrayList)this.allowedEstimateList.getAllowedStrings().clone());
        c1.setName( name );
        c1.setNullAllowed( nullAllowed );
        c1.editable = true;
        return c1;
    }

    /**
     * create  constraint so that only postive double values are allowed.
     * This function will automatically exclude normal
     *
     * @return
     */
    public static ArrayList createConstraintForPositiveDoubleValues() {
      ArrayList allowedEstimateTypes = new ArrayList();
      allowedEstimateTypes.add(NormalEstimate.NAME);
      //allowedEstimateTypes.add(LogNormalEstimate.NAME);
      allowedEstimateTypes.add(FractileListEstimate.NAME);
      allowedEstimateTypes.add(DiscreteValueEstimate.NAME);
      allowedEstimateTypes.add(MinMaxPrefEstimate.NAME);
      allowedEstimateTypes.add(PDF_Estimate.NAME);
      return allowedEstimateTypes;
    }

    /**
   * create  constraint so that only postive int values are allowed.
   *
   * @return
   */
  public static ArrayList createConstraintForPositiveIntValues() {
    ArrayList allowedEstimateTypes = new ArrayList();
    allowedEstimateTypes.add(IntegerEstimate.NAME);
    return allowedEstimateTypes;
  }

  /**
    * create  constraint so that all estimates are allowed. (It does not include
    * date estimates)
    *
    * @return
    */
   public static ArrayList createConstraintForAllEstimates() {
     ArrayList allowedEstimateTypes = new ArrayList();
     allowedEstimateTypes.add(NormalEstimate.NAME);
     //allowedEstimateTypes.add(LogNormalEstimate.NAME);
     allowedEstimateTypes.add(FractileListEstimate.NAME);
     allowedEstimateTypes.add(DiscreteValueEstimate.NAME);
     allowedEstimateTypes.add(IntegerEstimate.NAME);
     allowedEstimateTypes.add(MinMaxPrefEstimate.NAME);
     allowedEstimateTypes.add(PDF_Estimate.NAME);
     return allowedEstimateTypes;
   }

   /**
    * Return a list of classnames which can be used to specify the date Estimate
    * @return
    */

   public static ArrayList createConstraintForDateEstimates() {
     ArrayList allowedEstimateTypes = new ArrayList();
     allowedEstimateTypes.add(NormalEstimate.NAME);
     allowedEstimateTypes.add(DiscreteValueEstimate.NAME);
     allowedEstimateTypes.add(MinMaxPrefEstimate.NAME);
     allowedEstimateTypes.add(PDF_Estimate.NAME);
     return allowedEstimateTypes;

   }




}
