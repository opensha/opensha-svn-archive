package org.scec.sha.param;

import java.util.*;

import org.scec.exceptions.*;
import org.scec.param.*;
import org.scec.sha.magdist.*;

/**
 *  <b>Title:</b> MagFreqDistParameter<p>
 *
 *  <b>Description:</b> Generic Parameter that contains a IncremnetalMagFreqDist and
 *  optionally a list of allowed values stored in a constraint object. If no
 *  constraint object is present then all MagFreDists are permitted<p>
 *
 * @author     Nitin Gupta, Vipin Gupta
 * @created    Oct 18, 2002
 * @version    1.0
 */

public class MagFreqDistParameter
    extends DependentParameter
    implements java.io.Serializable
{

    /** Class name for debugging. */
    protected final static String C = "MagFreqDistParameter";
    /** If true print out debug statements. */
    protected final static boolean D = false;

    /**
     * the string for the distribution choice parameter
     */
    public final static String DISTRIBUTION_NAME="Choose Distribution";
//    public final static String DISTRIBUTION_NAME="Distribution Type";

    /**
     * Name and Info strings of params needed by all distributions
    */
   public static final String MIN=new String("Min");
   public static final String MIN_INFO=new String("Minimum magnitude of the discetized function");
   public static final String MAX=new String("Max");
   public static final String MAX_INFO=new String("Maximum magnitude of the discetized function");
   public static final String NUM=new String("Num");
   public static final String NUM_INFO=new String("Number of points in  the discetized function");

   /**
    * Name, units, and info strings for parameters needed by more than one distribution (shared)
    */
   // Moment rate stuff:
   public static final String TOT_MO_RATE = new String("Total Moment Rate");
   public static final String MO_RATE_UNITS=new String("Nm/yr");
   // Total cumulative rate stuff:
   public static final String TOT_CUM_RATE=new String("Total Cumulative Rate");
   public static final String RATE_UNITS=new String("/yr");
   // Gutenberg-Richter dist stuff (used by Y&C dist as well):
   public static final String GR_MAG_UPPER=new String("Mag Upper");
   public static final String GR_MAG_UPPER_INFO=new String("Magnitude of the last non-zero rate");
   public static final String GR_MAG_LOWER=new String("Mag Lower");
   public static final String GR_MAG_LOWER_INFO=new String("Magnitude of the first non-zero rate");
   public static final String GR_BVALUE=new String("b Value");
   public static final String BVALUE_INFO=new String("b in: log(rate) = a-b*magnitude");
   // Set all params but
   public final static String SET_ALL_PARAMS_BUT=new String("Set All Params But");
   // The fix (constrain) options
   public static final String FIX=new String("Constrain");
   public static final String FIX_INFO=new String("Only one of these can be matched exactly due to mag discretization");
   public static final String FIX_TOT_MO_RATE=new String("Total Moment Rate");
   public static final String FIX_TO_CUM_RATE=new String("Total Cum. Rate");
   public static final String FIX_RATE=new String("Rate");



 /**
  * Single Magnitude Frequency Distribution Parameter names
  */
   public static final String RATE=new String("Rate");
   public static final String MAG=new String("Mag");
   public static final String MO_RATE=new String("Moment Rate");
   public static final String SINGLE_PARAMS_TO_SET=new String("Params To Set");
   public static final String RATE_AND_MAG =new String("Rate & Mag");
   public static final String MAG_AND_MO_RATE =new String("Mag & Moment Rate");
   public static final String RATE_AND_MO_RATE=new String("Rate & Moment Rate");

  /**
   * Young and Coppersmith, 1985 Char dist. parameter names
   */
  public static final String YC_DELTA_MAG_CHAR = new String("Delta Mag Char");
  public static final String YC_DELTA_MAG_CHAR_INFO = new String("Width of the characteristic part (below Mag Upper)");
  public static final String YC_MAG_PRIME = new String("Mag Prime");
  public static final String YC_MAG_PRIME_INFO = new String("Last magnitude of the GR part");
  public static final String YC_DELTA_MAG_PRIME = new String("Delta Mag Prime");
  public static final String YC_DELTA_MAG_PRIME_INFO = new String("Distance below Mag Prime where rate on GR equals that on the char. part");
  public static final String YC_TOT_CHAR_RATE = new String("Total Char. Rate");
  public static final String YC_TOT_CHAR_RATE_INFO = new String("Total rate of events above (magUpper-deltaMagChar)");


 /**
  * Gaussian Magnitude Frequency Distribution Parameter string list constant
  */
  public static final String MEAN=new String("Mean");
  public static final String STD_DEV=new String("Std Dev");
  public static final String TRUNCATION_REQ=new String("Truncation Type");
  public static final String TRUNCATE_UPPER_ONLY= new String("Upper");
  public static final String TRUNCATE_ON_BOTH_SIDES= new String("Upper and Lower");
  public static final String TRUNCATE_NUM_OF_STD_DEV= new String("Truncation Level(# of Std Devs)");
  public static final String NONE= new String("None");

  // String Constraints
  private StringConstraint sdFixOptions,  grSetAllButOptions, grFixOptions,
      ycSetAllButOptions, gdSetAllButOptions;




    /**
     *  No constraints specified, all MagFreqDists allowed. Sets the name of this
     *  parameter.
     *
     * @param  name  Name of the parameter
     */
    public MagFreqDistParameter( String name ) {
        super( name, null, null, null );
    }



    /**
     *  Sets the name, defines the constraints as Vector of String values. Creates the
     *  constraint object from these values.
     *
     * @param  name                     Name of the parameter
     * @param  allowedMagDists          Vector of allowed values
     * @exception  ConstraintException  thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public MagFreqDistParameter( String name, Vector allowedMagDists ) throws ConstraintException {
        super( name, new MagFreqDistConstraint( allowedMagDists ), null, null );
    }


    /**
     *  Sets the name and Constraints object.
     *
     * @param  name                     Name of the parameter
     * @param  constraint               defines vector of allowed values
     * @exception  ConstraintException  thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public MagFreqDistParameter( String name, MagFreqDistConstraint constraint ) throws ConstraintException {
        super( name, constraint, null, null );
    }



    /**
     *  No constraints specified, all values allowed. Sets the name and value.
     *
     * @param  name   Name of the parameter
     * @param  value  IncrementalMagFreqDist  object
     */
    public MagFreqDistParameter( String name, IncrementalMagFreqDist value ) {
        super(name, null, null, value);
    }


    /**
     *  Sets the name, and value. Also defines the min and max from which the
     *  constraint is constructed.
     *
     * @param  name                     Name of the parameter
     * @param  value                    IncrementalMagFreqDist object of this parameter
     * @param  allowedMagDists          Vector  of allowed Mag Dists
     * @exception  ConstraintException  thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public MagFreqDistParameter( String name, Vector allowedMagDists, IncrementalMagFreqDist value )
                                throws ConstraintException {
        super( name, new MagFreqDistConstraint( allowedMagDists ), null, value );
    }



    /**
     *  Sets the name, value and constraint. The value is checked if it is
     *  within constraints.
     *
     * @param  name                     Name of the parameter
     * @param  constraint               vector of allowed Mag Dists
     * @param  value                    IncrementalMagFreqDist object
     * @exception  ConstraintException  thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public MagFreqDistParameter( String name, MagFreqDistConstraint constraint,
                                IncrementalMagFreqDist value ) throws ConstraintException {
        super( name, constraint, null, value );

    }




    /**
     *  Sets the constraint if it is a StringConstraint and the parameter
     *  is currently editable.
     */
    public void setConstraint(ParameterConstraintAPI constraint)
        throws ParameterException, EditableException
    {

        String S = C + ": setConstraint(): ";
        checkEditable(S);

        if ( !(constraint instanceof MagFreqDistConstraint )) {
            throw new ParameterException( S +
                "This parameter only accepts DoubleConstraints, unable to set the constraint."
            );
        }
        else super.setConstraint( constraint );

    }

    /**
     *  Gets the min value of the constraint object.
     *
     * @return                Vector of allowed Mag Dists
     */
    public Vector getAllowedMagDists()  {
        if ( constraint != null )
            return ( ( MagFreqDistConstraint ) constraint ).getAllowedMagDists();
        else return null;
    }




    /**
     *  Compares the values to if this is less than, equal to, or greater than
     *  the comparing objects.
     *
     * @param  obj                     The object to compare this to
     * @return                         -1 if this value < obj value, 0 if equal,
     *      +1 if this value > obj value
     * @exception  ClassCastException  Is thrown if the comparing object is not
     *      a DoubleParameter, or DoubleDiscreteParameter.
     */
    public int compareTo( Object obj ) throws UnsupportedOperationException {
      throw new java.lang.UnsupportedOperationException("This method not implemented yet");

    }


    /**
     *  Set's the parameter's value.
     *
     * @param  value                 The new value for this Parameter
     * @throws  ParameterException   Thrown if the object is currenlty not
     *      editable
     * @throws  ConstraintException  Thrown if the object value is not allowed
     */
    public void setValue( IncrementalMagFreqDist value ) throws ConstraintException, ParameterException {
        setValue( (Object) value );
    }

    /**
     *  Uses the constraint object to determine if the new value being set is
     *  allowed. If no Constraints are present all values are allowed. This
     *  function is now available to all subclasses, since any type of
     *  constraint object follows the same api.
     *
     * @param  obj  Object to check if allowed via constraints
     * @return      True if the value is allowed
     */
    public boolean isAllowed( IncrementalMagFreqDist d ) {
        return isAllowed( (Object)d );
    }



    /**
     *  Compares value to see if equal.
     *
     * @param  obj                     The object to compare this to
     * @return                         True if the values are identical
     * @exception  ClassCastException  Is thrown if the comparing object is not
     *      a DoubleParameter, or DoubleDiscreteParameter.
     */
    public boolean equals( Object obj ) throws UnsupportedOperationException {
      throw new java.lang.UnsupportedOperationException("This method not implemented yet");

    }


    /**
     *  Returns a copy so you can't edit or damage the origial.
     *
     * @return    Exact copy of this object's state
     */
    public Object clone() throws UnsupportedOperationException {
      throw new java.lang.UnsupportedOperationException("This method not implemented yet");

    }

    /**
     * Returns the type(full path with the classname) of the MagDist Classes
     */
    public String getType() {
      String type = C;
      return type;
    }

    /**
     * Return the adjustable params for the MagFreqDistParameter
     *
     * @return
     */
    public ParameterList getAdjustableParams() {
      /**
        * Make the parameter that lists the choice of distributions
        */
       ParameterList parameterList = new ParameterList();
       StringParameter distributionName =new StringParameter(DISTRIBUTION_NAME,getAllowedMagDists(),
           (String) getAllowedMagDists().get(0));

       // make the min, delta and num Parameters
       DoubleParameter minParameter = new DoubleParameter(MIN,new Double(0));
       minParameter.setInfo(MIN_INFO);
       DoubleParameter maxParameter = new DoubleParameter(MAX,new Double(10));
       maxParameter.setInfo(MAX_INFO);
       IntegerParameter numParameter = new IntegerParameter(NUM, (int) 0, Integer.MAX_VALUE, new Integer(101));
       numParameter.setInfo(NUM_INFO);

        // Make the other common parameters (used by more than one distribution)
        DoubleParameter totMoRate=new DoubleParameter(TOT_MO_RATE, 0, Double.POSITIVE_INFINITY,
                                                      MO_RATE_UNITS, new Double(1e19));
        DoubleParameter magLower = new DoubleParameter(GR_MAG_LOWER, new Double(5));
        magLower.setInfo(GR_MAG_LOWER_INFO);
        DoubleParameter magUpper = new DoubleParameter(GR_MAG_UPPER, new Double(8));
        magUpper.setInfo(GR_MAG_UPPER_INFO);
        DoubleParameter bValue = new DoubleParameter(GR_BVALUE,0, Double.POSITIVE_INFINITY, new Double(1));
        bValue.setInfo(BVALUE_INFO);
        DoubleParameter totCumRate = new DoubleParameter(TOT_CUM_RATE, 0, Double.POSITIVE_INFINITY,
                                                         RATE_UNITS, new Double(3.33));

        // add Parameters for single Mag freq dist
        DoubleParameter rate=new DoubleParameter(RATE, 0, Double.POSITIVE_INFINITY, RATE_UNITS, new Double(0.005));
        DoubleParameter moRate=new DoubleParameter(MO_RATE, 0, Double.POSITIVE_INFINITY, MO_RATE_UNITS, new Double(1e19));
        DoubleParameter mag = new DoubleParameter(MAG, new Double(8));
        Vector vStrings=new Vector();
        vStrings.add(RATE_AND_MAG);
        vStrings.add(MAG_AND_MO_RATE);
        vStrings.add(RATE_AND_MO_RATE);
        StringParameter singleParamsToSet=new StringParameter(SINGLE_PARAMS_TO_SET,
                                          vStrings,(String)vStrings.get(0));
        Vector vStrings3 = new Vector ();
        vStrings3.add(FIX_RATE);
        vStrings3.add(FIX_TOT_MO_RATE);
        sdFixOptions = new StringConstraint(vStrings3);

        /**
         * Make parameters for Gaussian distribution
         */
        DoubleParameter mean = new DoubleParameter(MEAN, new Double(8));
        DoubleParameter stdDev = new DoubleParameter(STD_DEV, 0, Double.POSITIVE_INFINITY, new Double(0.25));
        vStrings=new Vector();
        vStrings.add(TOT_CUM_RATE);
        vStrings.add(TOT_MO_RATE);
        gdSetAllButOptions = new StringConstraint(vStrings);
        vStrings=new Vector();
        vStrings.add(NONE);
        vStrings.add(TRUNCATE_UPPER_ONLY);
        vStrings.add(TRUNCATE_ON_BOTH_SIDES);
        StringParameter truncType=new StringParameter(TRUNCATION_REQ,vStrings,TRUNCATE_UPPER_ONLY);
        DoubleParameter truncLevel = new DoubleParameter(TRUNCATE_NUM_OF_STD_DEV, 0, Double.POSITIVE_INFINITY, new Double (1));

         /**
          * Make parameters for Gutenberg-Richter distribution
          */
        Vector vStrings1 = new Vector ();
        vStrings1.add(FIX_TO_CUM_RATE);
        vStrings1.add(FIX_TOT_MO_RATE);
        grFixOptions = new StringConstraint(vStrings1);

        /**
         * Make paramters for Youngs and Coppersmith 1985 char distribution
         */
        DoubleParameter deltaMagChar = new DoubleParameter(YC_DELTA_MAG_CHAR, 0,
                                                           Double.POSITIVE_INFINITY, new Double(1));
        deltaMagChar.setInfo(YC_DELTA_MAG_CHAR_INFO);
        DoubleParameter magPrime = new DoubleParameter(YC_MAG_PRIME, new Double(7));
        magPrime.setInfo(YC_MAG_PRIME_INFO);
        DoubleParameter deltaMagPrime = new DoubleParameter(YC_DELTA_MAG_PRIME, 0, Double.POSITIVE_INFINITY, new Double(1));
        deltaMagPrime.setInfo(YC_DELTA_MAG_PRIME_INFO);
        DoubleParameter totCharRate = new DoubleParameter(YC_TOT_CHAR_RATE, 0, Double.POSITIVE_INFINITY, new Double(0.01));
        totCharRate.setInfo(YC_TOT_CHAR_RATE_INFO);
        vStrings=new Vector();
        vStrings.add(YC_TOT_CHAR_RATE);
        vStrings.add(TOT_MO_RATE);
        ycSetAllButOptions = new StringConstraint(vStrings);

        // make the set all but paramter needed by YC, Gaussian and GR
        StringParameter setAllBut=new StringParameter(SET_ALL_PARAMS_BUT,
                                     ycSetAllButOptions,
                                     (String)ycSetAllButOptions.getAllowedStrings().get(0));

        // make the fix parameter needed by Single and GR dists
        StringParameter fixParam = new StringParameter(FIX,grFixOptions,FIX_TO_CUM_RATE);
        fixParam.setInfo(FIX_INFO);

        // for Gutenberg-Richter SET ALL BUT option
        vStrings=new Vector();
        vStrings.add(MagFreqDistParameter.TOT_MO_RATE);
        vStrings.add(MagFreqDistParameter.TOT_CUM_RATE);
        vStrings.add(MagFreqDistParameter.GR_MAG_UPPER);
        grSetAllButOptions = new StringConstraint(vStrings);


        // Add the parameters to the list (order is preserved)
        parameterList.addParameter(distributionName);
        parameterList.addParameter( minParameter );
        parameterList.addParameter( numParameter );
        parameterList.addParameter( maxParameter );
        // put ones that are always shown next
        parameterList.addParameter(magLower);
        parameterList.addParameter(magUpper);
        parameterList.addParameter(bValue);
        parameterList.addParameter(deltaMagChar);
        parameterList.addParameter(magPrime);
        parameterList.addParameter(deltaMagPrime);
        parameterList.addParameter(mean);
        parameterList.addParameter(stdDev);
        parameterList.addParameter(truncType);
        parameterList.addParameter(truncLevel);
        // now add the params that present choices
        parameterList.addParameter(setAllBut);
        parameterList.addParameter(singleParamsToSet);
        // now add params that depend on choices
        parameterList.addParameter(totCharRate);
        parameterList.addParameter(mag);
        parameterList.addParameter(rate);
        parameterList.addParameter(moRate);
        parameterList.addParameter(totMoRate);
        parameterList.addParameter(totCumRate);
        // now add params that present choice dependent on above choice
        parameterList.addParameter(fixParam);
        return parameterList;
    }


    /**
     * return the IncrementalMagFreqDist object based on parameters given
     * @param parameterList
     * @return
     */
    public void setMagDist(ParameterList parameterList) {
      String S = C + ": getMagDist():";
      String distributionName = parameterList.getParameter(MagFreqDistParameter.
          DISTRIBUTION_NAME).getValue().toString();

      //stores the visible parameters for the MagFreqDist parameter as the independent parameters
      ParameterList independentParamList = new ParameterList();
      independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
          DISTRIBUTION_NAME));

      IncrementalMagFreqDist magDist = null;
      try {
        Double min = (Double) parameterList.getParameter(MagFreqDistParameter.MIN).
            getValue();
        Double max = (Double) parameterList.getParameter(MagFreqDistParameter.MAX).
            getValue();
        Integer num = (Integer) parameterList.getParameter(MagFreqDistParameter.
            NUM).getValue();

        if (min.doubleValue() > max.doubleValue()) {
          throw new java.lang.RuntimeException(
              "Min Value cannot be less than the Max Value");
        }

        independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.MIN));
        independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.MAX));
        independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.NUM));

        /*
         * If Single MagDist is selected
         */
        if (distributionName.equalsIgnoreCase(SingleMagFreqDist.NAME)) {
          if (D)
            System.out.println(S + " selected distribution is SINGLE");
          SingleMagFreqDist single = new SingleMagFreqDist(min.doubleValue(),
              max.doubleValue(), num.intValue());
          String paramToSet = parameterList.getParameter(MagFreqDistParameter.
              SINGLE_PARAMS_TO_SET).getValue().toString();
          independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              SINGLE_PARAMS_TO_SET));

          // if rate and mag are set
          if (paramToSet.equalsIgnoreCase(MagFreqDistParameter.RATE_AND_MAG)) {
            if (D)
              System.out.println(S + " Rate and mag is selected in SINGLE");
            Double rate = (Double) parameterList.getParameter(
                MagFreqDistParameter.RATE).getValue();
            Double mag = (Double) parameterList.getParameter(MagFreqDistParameter.
                MAG).getValue();
            if (mag.doubleValue() > max.doubleValue() ||
                mag.doubleValue() < min.doubleValue()) {
              throw new java.lang.RuntimeException(
                  "Value of Mag must lie between the min and max value");
            }

            independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
                RATE));
            independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              MAG));
            try {
              single.setMagAndRate(mag.doubleValue(), rate.doubleValue());
            }
            catch (RuntimeException e) {
              throw new java.lang.RuntimeException(
                  "The chosen magnitude must fall on one of the discrete x-axis values");
            }

            if (D)
              System.out.println(S + " after setting SINGLE DIST");
          }
          // if mag and moment rate are set
          else if (paramToSet.equalsIgnoreCase(MagFreqDistParameter.MAG_AND_MO_RATE)) {
            Double mag = (Double) parameterList.getParameter(MagFreqDistParameter.
                MAG).getValue();
            Double moRate = (Double) parameterList.getParameter(
                MagFreqDistParameter.MO_RATE).getValue();
            if (mag.doubleValue() > max.doubleValue() ||
                mag.doubleValue() < min.doubleValue()) {
              throw new java.lang.RuntimeException(
                  "Value of Mag must lie between the min and max value");
            }
            independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
                MO_RATE));
            independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              MAG));
            try {
              single.setMagAndMomentRate(mag.doubleValue(), moRate.doubleValue());
            }
            catch (RuntimeException e) {
              throw new java.lang.RuntimeException(
                  "The chosen magnitude must fall on one of the discrete x-axis values");
            }
          }
          // if rate and moment rate are set
          else if (paramToSet.equalsIgnoreCase(MagFreqDistParameter.RATE_AND_MO_RATE)) {
            String fix = parameterList.getParameter(MagFreqDistParameter.FIX).
                         getValue().toString();
            Double rate = (Double) parameterList.getParameter(
                MagFreqDistParameter.RATE).getValue();
            Double moRate = (Double) parameterList.getParameter(
                MagFreqDistParameter.MO_RATE).getValue();
            if (fix.equals(MagFreqDistParameter.FIX_RATE))
              single.setRateAndMomentRate(rate.doubleValue(), moRate.doubleValue(), true);
            else
              single.setRateAndMomentRate(rate.doubleValue(), moRate.doubleValue(), false);

            independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
                FIX));
            independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
                MO_RATE));
            independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
                RATE));
          }
          magDist = (IncrementalMagFreqDist) single;
        }

        /*
         * If Gaussian MagDist is selected
         */
        else if (distributionName.equalsIgnoreCase(GaussianMagFreqDist.NAME)) {
          Double mean = (Double) parameterList.getParameter(MagFreqDistParameter.
              MEAN).getValue();
          Double stdDev = (Double) parameterList.getParameter(
              MagFreqDistParameter.STD_DEV).getValue();
          String truncTypeValue = parameterList.getParameter(MagFreqDistParameter.
              TRUNCATION_REQ).getValue().toString();
          if (mean.doubleValue() > max.doubleValue() ||
              mean.doubleValue() < min.doubleValue()) {
            throw new java.lang.RuntimeException(
                "Value of Mean must lie between the min and max value");
          }
          independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              MEAN));
          independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              STD_DEV));
          independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              TRUNCATION_REQ));

          int truncType = 0;
          if (truncTypeValue.equalsIgnoreCase(MagFreqDistParameter.TRUNCATE_UPPER_ONLY))
            truncType = 1;
          else if (truncTypeValue.equalsIgnoreCase(MagFreqDistParameter.TRUNCATE_ON_BOTH_SIDES))
            truncType = 2;

          Double truncLevel = new Double(Double.NaN);
          if (truncType != 0) {
            truncLevel = (Double) parameterList.getParameter(
                MagFreqDistParameter.TRUNCATE_NUM_OF_STD_DEV).getValue();
            if (truncLevel.doubleValue() < 0)
              throw new java.lang.RuntimeException("Value of " +
                                                   MagFreqDistParameter.
                                                   TRUNCATE_NUM_OF_STD_DEV +
                                                   " must be  positive");

            independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              TRUNCATE_NUM_OF_STD_DEV));
          }
          String setAllParamsBut = parameterList.getParameter(
              MagFreqDistParameter.SET_ALL_PARAMS_BUT).getValue().toString();

          independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              SET_ALL_PARAMS_BUT));

          GaussianMagFreqDist gaussian = new GaussianMagFreqDist(min.doubleValue(),
                                             max.doubleValue(), num.intValue());

          if (setAllParamsBut.equalsIgnoreCase(MagFreqDistParameter.
                                                 TOT_CUM_RATE)) {
              Double totMoRate = (Double) parameterList.getParameter(
                  MagFreqDistParameter.TOT_MO_RATE).getValue();
              independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              TOT_MO_RATE));
              gaussian.setAllButCumRate(mean.doubleValue(), stdDev.doubleValue(),
                                        totMoRate.doubleValue(),
                                        truncLevel.doubleValue(), truncType);
          }
          else {
              Double totCumRate = (Double) parameterList.getParameter(
                  MagFreqDistParameter.TOT_CUM_RATE).getValue();
              independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              TOT_CUM_RATE));
              gaussian.setAllButTotMoRate(mean.doubleValue(), stdDev.doubleValue(),
                                          totCumRate.doubleValue(),
                                          truncLevel.doubleValue(), truncType);
          }
          magDist = (IncrementalMagFreqDist) gaussian;
        }

        /*
         * If Gutenberg Richter MagDist is selected
         */
        else if (distributionName.equalsIgnoreCase(GutenbergRichterMagFreqDist.NAME)) {
          GutenbergRichterMagFreqDist gR =
              new GutenbergRichterMagFreqDist(min.doubleValue(), max.doubleValue(),
                                              num.intValue());

          Double magLower = (Double) parameterList.getParameter(
              MagFreqDistParameter.GR_MAG_LOWER).getValue();
          Double bValue = (Double) parameterList.getParameter(
              MagFreqDistParameter.GR_BVALUE).getValue();
          String setAllParamsBut = parameterList.getParameter(
              MagFreqDistParameter.SET_ALL_PARAMS_BUT).getValue().toString();
          if (magLower.doubleValue() > max.doubleValue() ||
              magLower.doubleValue() < min.doubleValue()) {
            throw new java.lang.RuntimeException(
                "Value of MagLower must lie between the min and max value");
          }

          independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              GR_MAG_LOWER));
          independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              GR_BVALUE));
          independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              SET_ALL_PARAMS_BUT));

          // if set all parameters except total moment rate
          if (setAllParamsBut.equalsIgnoreCase(MagFreqDistParameter.TOT_MO_RATE)) {
            Double magUpper = (Double) parameterList.getParameter(
                MagFreqDistParameter.GR_MAG_UPPER).getValue();
            Double totCumRate = (Double) parameterList.getParameter(
                MagFreqDistParameter.TOT_CUM_RATE).getValue();

            if (magUpper.doubleValue() > max.doubleValue() ||
                magUpper.doubleValue() < min.doubleValue()) {
              throw new java.lang.RuntimeException(
                  "Value of MagUpper must lie between the min and max value");
            }
            if (magLower.doubleValue() > magUpper.doubleValue()) {
              throw new java.lang.RuntimeException(
                  "Value of MagLower must be <= to MagUpper");
            }
            try {
              gR.setAllButTotMoRate(magLower.doubleValue(), magUpper.doubleValue(),
                                    totCumRate.doubleValue(), bValue.doubleValue());
            }
            catch (RuntimeException e) {
              throw new java.lang.RuntimeException(
                  "magUpper and MagLower must fall on one of the discrete x-axis values");
            }

            independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              GR_MAG_UPPER));
            independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              TOT_CUM_RATE));
          }
          // if set all parameters except total cumulative rate
          else if (setAllParamsBut.equalsIgnoreCase(MagFreqDistParameter.TOT_CUM_RATE)) {
            Double magUpper = (Double) parameterList.getParameter(
                MagFreqDistParameter.GR_MAG_UPPER).getValue();
            Double toMoRate = (Double) parameterList.getParameter(
                MagFreqDistParameter.TOT_MO_RATE).getValue();
            if (magUpper.doubleValue() > max.doubleValue() ||
                magUpper.doubleValue() < min.doubleValue()) {
              throw new java.lang.RuntimeException(
                  "Value of MagUpper must lie between the min and max value");
            }
            if (magLower.doubleValue() > magUpper.doubleValue()) {
              throw new java.lang.RuntimeException(
                  "Value of MagLower must be <= to MagUpper");
            }
            try {
              gR.setAllButTotCumRate(magLower.doubleValue(), magUpper.doubleValue(),
                                     toMoRate.doubleValue(), bValue.doubleValue());
            }
            catch (RuntimeException e) {
              throw new java.lang.RuntimeException(
                  "magUpper and MagLower must fall on one of the discrete x-axis values");
            }
            independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              GR_MAG_UPPER));
            independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              TOT_MO_RATE));
          }
          // if set all parameters except mag upper
          else if (setAllParamsBut.equalsIgnoreCase(MagFreqDistParameter.GR_MAG_UPPER)) {
            Double toCumRate = (Double) parameterList.getParameter(
                MagFreqDistParameter.TOT_CUM_RATE).getValue();
            Double toMoRate = (Double) parameterList.getParameter(
                MagFreqDistParameter.TOT_MO_RATE).getValue();
            String fix = parameterList.getParameter(MagFreqDistParameter.FIX).
                getValue().toString();
            boolean relaxTotMoRate = true;
            if (fix.equalsIgnoreCase(MagFreqDistParameter.FIX_TOT_MO_RATE))
              relaxTotMoRate = false;
            try {
              gR.setAllButMagUpper(magLower.doubleValue(), toMoRate.doubleValue(),
                                   toCumRate.doubleValue(), bValue.doubleValue(),
                                   relaxTotMoRate);
            }
            catch (RuntimeException e) {
              throw new java.lang.RuntimeException(
                  "MagLower must fall on one of the discrete x-axis values");
            }
            independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              TOT_CUM_RATE));
            independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              TOT_MO_RATE));
            independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              FIX));
          }
          magDist = (IncrementalMagFreqDist) gR;
        }

        /*
         * If Young and Coppersmith 1985 MagDist is selected
         */
        else if (distributionName.equalsIgnoreCase(YC_1985_CharMagFreqDist.NAME)) {

          double magLower = ( (Double) parameterList.getParameter(
              MagFreqDistParameter.GR_MAG_LOWER).getValue()).doubleValue();
          double magUpper = ( (Double) parameterList.getParameter(
              MagFreqDistParameter.GR_MAG_UPPER).getValue()).doubleValue();
          double deltaMagChar = ( (Double) parameterList.getParameter(
              MagFreqDistParameter.YC_DELTA_MAG_CHAR).getValue()).doubleValue();
          double magPrime = ( (Double) parameterList.getParameter(
              MagFreqDistParameter.YC_MAG_PRIME).getValue()).doubleValue();
          double deltaMagPrime = ( (Double) parameterList.getParameter(
              MagFreqDistParameter.YC_DELTA_MAG_PRIME).getValue()).doubleValue();
          double bValue = ( (Double) parameterList.getParameter(
              MagFreqDistParameter.GR_BVALUE).getValue()).doubleValue();


          // check that maglowe r value lies betwenn min and max
          if (magLower > max.doubleValue() || magLower < min.doubleValue()) {
            throw new java.lang.RuntimeException(
                "Value of MagLower must lie between the min and max value");
          }
          // check that magUpper value lies between min and max
          if (magUpper > max.doubleValue() || magUpper < min.doubleValue()) {
            throw new java.lang.RuntimeException(
                "Value of MagUpper must lie between the min and max value");
          }

          // creat the distribution
          YC_1985_CharMagFreqDist yc =
              new YC_1985_CharMagFreqDist(min.doubleValue(), max.doubleValue(),
                                          num.intValue());

          // Check that the mags fall on valid x increments:
          int trialInt;
          try {
            trialInt = yc.getXIndex(magLower);
          }
          catch (RuntimeException e) {
            throw new java.lang.RuntimeException(
                "MagLower must fall on one of the discrete x-axis values");
          }
          try {
            trialInt = yc.getXIndex(magUpper);
          }
          catch (RuntimeException e) {
            throw new java.lang.RuntimeException(
                "MagUpper must fall on one of the discrete x-axis values");
          }
          try {
            trialInt = yc.getXIndex(magPrime);
          }
          catch (RuntimeException e) {
            throw new java.lang.RuntimeException(
                "MagPrime must fall on one of the discrete x-axis values");
          }
          try {
            trialInt = yc.getXIndex(magPrime - deltaMagPrime);
          }
          catch (RuntimeException e) {
            throw new java.lang.RuntimeException(
                "MagPrime-DeltaMagPrime must fall on one of the discrete x-axis values");
          }
          try {
            trialInt = yc.getXIndex(magUpper - deltaMagChar);
          }
          catch (RuntimeException e) {
            throw new java.lang.RuntimeException(
                "MagUpper-DeltaMagChar must fall on one of the discrete x-axis values");
          }
          independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              GR_MAG_LOWER));
          independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              GR_MAG_UPPER));
          independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              YC_DELTA_MAG_CHAR));
          independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              YC_MAG_PRIME));
          independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              YC_DELTA_MAG_PRIME));
          independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              GR_BVALUE));
          String setAllParamsBut = parameterList.getParameter(
              MagFreqDistParameter.SET_ALL_PARAMS_BUT).getValue().toString();
          independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              SET_ALL_PARAMS_BUT));

          if (setAllParamsBut.equalsIgnoreCase(MagFreqDistParameter.
                                               YC_TOT_CHAR_RATE)) {
            double totMoRate = ( (Double) parameterList.getParameter(
                MagFreqDistParameter.TOT_MO_RATE).getValue()).doubleValue();
            yc.setAllButTotCharRate(magLower, magUpper,
                                    deltaMagChar, magPrime,
                                    deltaMagPrime, bValue,
                                    totMoRate);
            independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              TOT_MO_RATE));
          }
          else {
            double totCharRate = ( (Double) parameterList.getParameter(
                MagFreqDistParameter.YC_TOT_CHAR_RATE).getValue()).doubleValue();
            yc.setAllButTotMoRate(magLower, magUpper,
                                  deltaMagChar, magPrime,
                                  deltaMagPrime, bValue,
                                  totCharRate);
            independentParamList.addParameter(parameterList.getParameter(MagFreqDistParameter.
              YC_TOT_CHAR_RATE));
          }

          magDist = (IncrementalMagFreqDist) yc;
        }

      }
      catch (java.lang.NumberFormatException e) {
        throw new NumberFormatException(
            "Value entered must be a valid Numerical Value");
      }
      catch (java.lang.NullPointerException e) {
        throw new NullPointerException("Enter All values");
      }
      this.setValue(magDist);
      //creates the independent parameterList for the MagFreqDist.
      //It only saves those parameters for visible distribution.

      setIndependentParameters(independentParamList);
    }

    /**
     *
     * @returns the matadata string for parameter.
     * This function returns the metadata which can be used to reset the values
     * of the parameters created.
     * *NOTE : Look at the function getMetadataXML() which return the values of
     * these parameters in the XML format and can used recreate the parameters
     * from scratch.
     */
    public String getMetadataString() {
      StringBuffer metadata = new StringBuffer();
      metadata.append(getName()+" [ ");
      ListIterator list = getIndependentParametersIterator();
      while(list.hasNext()){
       ParameterAPI tempParam = (ParameterAPI)list.next();
       metadata.append(tempParam.getMetadataString()+" ; ");
      }
      metadata.replace(metadata.length()-2,metadata.length()," ]");
      return metadata.toString();
    }

    /**
     * Return the FIX and SET_ALL_PARAMS_BUT constraints for each dist
     *
     * @return
     */
    public StringConstraint getSingleDistFixOptions() { return this.sdFixOptions; }
    public StringConstraint getGRSetAllButOptions() { return this.grSetAllButOptions; }
    public StringConstraint getGRFixOptions() { return this.grFixOptions; }
    public StringConstraint getYCSetAllButOptions() { return this.ycSetAllButOptions; }
    public StringConstraint getGaussianDistSetAllButOptions() { return this.gdSetAllButOptions; }
}
