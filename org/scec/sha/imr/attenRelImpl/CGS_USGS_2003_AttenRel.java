 package org.scec.sha.imr.attenRelImpl;

import java.util.*;

import org.scec.data.*;
import org.scec.exceptions.*;
import org.scec.param.*;
import org.scec.param.event.*;
import org.scec.sha.earthquake.*;
import org.scec.sha.imr.*;
import org.scec.sha.imr.attenRelImpl.*;
import org.scec.sha.imr.attenRelImpl.calc.*;
import org.scec.sha.param.*;
import org.scec.util.*;

/**
 * <b>Title:</b> ShakeMap_2003_AttenRel<p>
 *
 * <b>Description:</b> This implements the Attenuation Relationship used
 * by . <p>
 *
 * Supported Intensity-Measure Parameters:
 * <UL>
 * <LI>pgaParam - Peak Ground Acceleration
 * <LI>SAParam  - Spectral Acceleration at 0.0, 0.1, 0.2 0.3, 0.4, 0.5, 0.75 1.0, 1.5, 2.0, 3.0, and 4.0 second periods
 * <LI>pgvParam - Peak Ground Velocity (computed from 1-sec SA using the Newmark-Hall (1982) scalar)
 * <LI>mmiParam - Modified Mercalli Intensity computed from PGA and PGV as in Wald et al. (1999, Earthquake Spectra)
 * </UL><p>
 * Other Independent Parameters:
 * <UL>
 * <LI>magParam - moment Magnitude
 * <LI>distanceJBParam - closest distance to surface projection of fault
 * <LI>willsSiteParam - The site classes used in the Wills et al. (2000) map
 * <LI>fltTypeParam - Style of faulting
 * <LI>componentParam - Component of shaking (only one)
 * <LI>stdDevTypeParam - The type of standard deviation
 * </UL><p>
 * Important Notes:
 * <UL>
 * This class supports a "Greater of Two Horz." component by multiplying the average horizontal
 * component  median by a factor of 1.15.  This value was taken directly from the official ShakeMap
 * documentation.  The standard deviation for this component is set the same as the average
 * horizontal (not sure if this is correct).  <p>
 * Regarding the Modified Mercalli Intensity (MMI) IMT, note that what is returned by
 * the getMean() method is the natural-log of MMI.  Although this is not technically
 * correct (since MMI is not log-normally distributed), it was the easiest way to implement
 * it for now.  Furthermore, because the probability distribution of MMI (computed from PGA
 * or PGV) is presently unknown, we cannot compute the standard deviation, probability of
 * exceedance, or the IML at any probability other than 0.5.  Therefore, a RuntimeException
 * is thrown if one tries any of these when the chosen IMT is MMI.  We can relax this when
 * someone comes up with the probability distribution (which can't be Gaussian because
 * MMI values below 1 and above 10 are not allowed).<p>
 *
 * @author     Edward H. Field
 * @created    April, 2003
 * @version    1.0
 */


public class CGS_USGS_2003_AttenRel
         extends AttenuationRelationship
         implements
        AttenuationRelationshipAPI,
        NamedObjectAPI {

    // debugging stuff:
    private final static String C = "CGS_USGS_2003_AttenRel";
    private final static boolean D = false;
    public final static String NAME = "CGS_USGS (2003)";

    // attenuation relationships used.
    private final AS_1997_AttenRel as_1997_attenRel;
    private final CB_2003_AttenRel cb_2003_attenRel;
    private final SCEMY_1997_AttenRel scemy_1997_attenRel;
    private final BJF_1997_AttenRel bjf_1997_attenRel;

    // this is a separate im for use in the calculations
    ParameterAPI im_forCalc;

    // The Borcherdt (2004) site amplification calculator
    Borcherdt2004_SiteAmpCalc borcherdtAmpCalc = new Borcherdt2004_SiteAmpCalc();

    // the site object for the BC boundary
    private Site site_BC;

    private double vs30_ref = 760;

    protected final static Double VS30_WARN_MIN = new Double(180.0);
    protected final static Double VS30_WARN_MAX = new Double(3500.0);

    /**
     * Thier maximum horizontal component option.
     */
    public final static String COMPONENT_GREATER_OF_TWO_HORZ = "Greater of Two Horz.";

    /**
     * MMI parameter, the natural log of the "Modified Mercalli Intensity" IMT.
     */
    protected  DoubleParameter mmiParam = null;
    public final static String MMI_NAME = "MMI";
    protected final static Double MMI_DEFAULT = new Double( Math.log( 5.0 ) );
    public final static String MMI_INFO = "Modified Mercalli Intensity";
    protected final static Double MMI_MIN = new Double( Math.log(1.0) );
    protected final static Double MMI_MAX = new Double( Math.log(10.0) );
    public final static String MMI_ERROR_STRING = "Problem: "+
        NAME + " cannot complete\n the requested computation for MMI.\n\n" +
        "This has occurred because you attempted to compute the\n"+
        "standard deviation (or something else such as probability \n"+
        "of exceedance which depends on the standard deviation).  \n"+
        "The inability to compute these will remain until someone comes up\n"+
        "with the probability distribution for MMI (when computed from\n"+
        "PGA or PGV).  For now you can compute the median or the\n"+
        "IML that has exactly a 0.5 chance of being exceeded (assuming\n"+
        "this application supports such computations).\n";


    // for issuing warnings:
    private transient  ParameterChangeWarningListener warningListener = null;



    /**
     *  No-Arg constructor. This initializes several ParameterList objects.
     */
    public CGS_USGS_2003_AttenRel( ParameterChangeWarningListener warningListener ) {

        super();

        this.warningListener = warningListener;

        initSupportedIntensityMeasureParams( );

        initProbEqkRuptureParams(  );
        initPropagationEffectParams( );
        initSiteParams();
        initOtherParams( );

        initIndependentParamLists(); // Do this after the above

        // init the attenuation relationships
        as_1997_attenRel = new AS_1997_AttenRel(warningListener);
        cb_2003_attenRel = new CB_2003_AttenRel(warningListener);
        scemy_1997_attenRel = new SCEMY_1997_AttenRel(warningListener);
        bjf_1997_attenRel = new BJF_1997_AttenRel(warningListener);

        // init the BC boundary site object:
        site_BC = new Site();

        as_1997_attenRel.getParameter(as_1997_attenRel.SITE_TYPE_NAME).setValue(as_1997_attenRel.SITE_TYPE_ROCK);
        site_BC.addParameter(as_1997_attenRel.getParameter(as_1997_attenRel.SITE_TYPE_NAME));

        cb_2003_attenRel.getParameter(cb_2003_attenRel.SITE_TYPE_NAME).setValue(cb_2003_attenRel.SITE_TYPE_NEHRP_BC);
        site_BC.addParameter(cb_2003_attenRel.getParameter(cb_2003_attenRel.SITE_TYPE_NAME));

        scemy_1997_attenRel.getParameter(scemy_1997_attenRel.SITE_TYPE_NAME).setValue(scemy_1997_attenRel.SITE_TYPE_ROCK);
        site_BC.addParameter(scemy_1997_attenRel.getParameter(scemy_1997_attenRel.SITE_TYPE_NAME));

        bjf_1997_attenRel.getParameter(bjf_1997_attenRel.VS30_NAME).setValue(new Double(760.0));
        site_BC.addParameter(bjf_1997_attenRel.getParameter(bjf_1997_attenRel.VS30_NAME));

        // set the components in the attenuation relationships
        as_1997_attenRel.getParameter(COMPONENT_NAME).setValue(COMPONENT_AVE_HORZ);
        cb_2003_attenRel.getParameter(COMPONENT_NAME).setValue(COMPONENT_AVE_HORZ);
        scemy_1997_attenRel.getParameter(COMPONENT_NAME).setValue(COMPONENT_AVE_HORZ);
        // the next one is different to be consistent with Frankel's implementation
        bjf_1997_attenRel.getParameter(COMPONENT_NAME).setValue(bjf_1997_attenRel.COMPONENT_RANDOM_HORZ);

    }


    /**
     *  This sets the potential-earthquake related parameters (magParam
     *  and fltTypeParam) based on the probEqkRupture passed in.
     *  The internally held probEqkRupture object is also set as that
     *  passed in. Since this object updates more than one parameter, an
     *  attempt is made to rollback to the original parameter values in case
     *  there are any errors thrown in the process.
     *
     * @param  pe  The new probEqkRupture value
     */
    public void setProbEqkRupture( ProbEqkRupture probEqkRupture ) throws ConstraintException{

        // Set the probEqkRupture
        this.probEqkRupture = probEqkRupture;

        // set the EqkRup in the atten relations
        as_1997_attenRel.setProbEqkRupture(probEqkRupture);
        bjf_1997_attenRel.setProbEqkRupture(probEqkRupture);
        scemy_1997_attenRel.setProbEqkRupture(probEqkRupture);
        cb_2003_attenRel.setProbEqkRupture(probEqkRupture);

    }


    /**
     *  This sets the site-related parameter (willsSiteParam) based on what is in
     *  the Site object passed in (the Site object must have a parameter with
     *  the same name as that in willsSiteParam).  This also sets the internally held
     *  Site object as that passed in.
     *
     * @param  site             The new site value which contains a Wills site Param.
     * @throws ParameterException Thrown if the Site object doesn't contain a
     * Wills site parameter
     */
    public void setSite( Site site ) throws ParameterException, IMRException, ConstraintException {


        // This will throw a parameter exception if the Wills Param doesn't exist
        // in the Site object

        ParameterAPI vs30 = site.getParameter( this.VS30_NAME );
       // This may throw a constraint exception
        try{
          this.vs30Param.setValue( vs30.getValue() );
        } catch (WarningException e){
          if(D) System.out.println(C+"Warning Exception:"+e);
        }

        // Now pass function up to super to set the site
        super.setSite( site );

        // set the location of the BC bounday site object
        site_BC.setLocation(site.getLocation());

        // set the  BC Site in the attenuation relations
        as_1997_attenRel.setSite(site_BC);
        bjf_1997_attenRel.setSite(site_BC);
        scemy_1997_attenRel.setSite(site_BC);
        cb_2003_attenRel.setSite(site_BC);

   }



    /**
     * Note that for MMI this returns the natural log of MMI (this should be changed later)
     * @return
     * @throws IMRException
     */
    public double getMean() throws IMRException{

      String imt = im.getName();
      double ave_bc, pga_bc, amp, vs30=0.0;

      vs30 = ((Double) vs30Param.getValue()).doubleValue();

      if(imt.equals(PGA_NAME)) {
        im_forCalc = im;
        pga_bc = getBC_Mean();
        amp = borcherdtAmpCalc.getShortPeriodAmp(vs30,vs30_ref,pga_bc);
        return pga_bc + Math.log(amp);
      }
      else if (imt.equals(SA_NAME)) {
        im_forCalc = im;
        ave_bc = getBC_Mean();
        im_forCalc = pgaParam;
        pga_bc = getBC_Mean();
        double per = ((Double) periodParam.getValue()).doubleValue();
        if(per <= 0.5)
          amp = borcherdtAmpCalc.getShortPeriodAmp(vs30,vs30_ref,pga_bc);
        else
          amp = borcherdtAmpCalc.getMidPeriodAmp(vs30,vs30_ref,pga_bc);
        return ave_bc + Math.log(amp);
      }
      else if (imt.equals(PGV_NAME)) {
        im_forCalc = saParam;
        periodParam.setValue(new Double(1.0));
        ave_bc = getBC_Mean();
        im_forCalc = pgaParam;
        pga_bc = getBC_Mean();
        amp = borcherdtAmpCalc.getMidPeriodAmp(vs30,vs30_ref,pga_bc);
        return ave_bc + Math.log(amp) + Math.log(37.27*2.54);
      }
      else { // it must be MMI
        im_forCalc = saParam;
        periodParam.setValue(new Double(1.0));
        ave_bc = getBC_Mean();
        im_forCalc = pgaParam;
        pga_bc = getBC_Mean();
        amp = borcherdtAmpCalc.getMidPeriodAmp(vs30,vs30_ref,pga_bc);
        double pgv = ave_bc + Math.log(amp) + Math.log(37.27*2.54);
        amp = borcherdtAmpCalc.getShortPeriodAmp(vs30,vs30_ref,pga_bc);
        double pga = pga_bc + Math.log(amp);
        double mmi = Wald_MMI_Calc.getMMI(Math.exp(pga),Math.exp(pgv));
        return Math.log(mmi);
      }
    }


    /**
     * @return    The mean value
     */
    private double getBC_Mean(){

      // set the IMT in the attenuation relations
      as_1997_attenRel.setIntensityMeasure(im_forCalc);
      bjf_1997_attenRel.setIntensityMeasure(im_forCalc);
      scemy_1997_attenRel.setIntensityMeasure(im_forCalc);
      cb_2003_attenRel.setIntensityMeasure(im_forCalc);

      String imt = (String) im_forCalc.getValue();
      double per = ((Double) periodParam.getValue()).doubleValue();
      double mean = 0;
      if(imt.equals(this.SA_NAME) && ( per >= 3.0 )) {
        mean += as_1997_attenRel.getMean();
        mean += cb_2003_attenRel.getMean();
        mean += scemy_1997_attenRel.getMean();
        return mean/3.0;
      }
      else {
        mean += as_1997_attenRel.getMean();
        mean += cb_2003_attenRel.getMean();
        mean += bjf_1997_attenRel.getMean();
        mean += scemy_1997_attenRel.getMean();
        return mean/4.0;
      }
    }




    /**
     * This sets the standard deviation type for all the attenuation relations.  Note that
     */
    private void setAttenRelsStdDevTypes() {

      // set the stdDevTypes
      String stdTyp = (String) stdDevTypeParam.getValue();
      as_1997_attenRel.getParameter(STD_DEV_TYPE_NAME).setValue(stdTyp);
      scemy_1997_attenRel.getParameter(STD_DEV_TYPE_NAME).setValue(stdTyp);
      bjf_1997_attenRel.getParameter(STD_DEV_TYPE_NAME).setValue(stdTyp);
      if(stdTyp.equals(STD_DEV_TYPE_TOTAL)) {
         cb_2003_attenRel.getParameter(STD_DEV_TYPE_NAME).setValue(cb_2003_attenRel.STD_DEV_TYPE_TOTAL_MAG_DEP);
      }
      else {
        cb_2003_attenRel.getParameter(STD_DEV_TYPE_NAME).setValue(STD_DEV_TYPE_NONE);
      }
    }


    /**
     *  This overides the parent to take care if MMI is the chosen IMT.
     *
     * @return                         The intensity-measure level
     * @exception  ParameterException  Description of the Exception
     */
    public double getIML_AtExceedProb() throws ParameterException {

        if(im.getName().equals(MMI_NAME)) {
          double exceedProb = ( ( Double ) ( ( ParameterAPI ) exceedProbParam ).getValue() ).doubleValue();
          if(exceedProb == 0.5) {
            if ( sigmaTruncTypeParam.getValue().equals( SIGMA_TRUNC_TYPE_1SIDED ) )
              throw new RuntimeException(MMI_ERROR_STRING);
            else
              return getMean();
          }
          else
            throw new RuntimeException(MMI_ERROR_STRING);
        }
        else
          return super.getIML_AtExceedProb();
    }

    /**
     * @return    The stdDev value
     *
     * The only one that's site-type dependent is Sadigh et al. (1997), and the max diff
     * is 6% (for PGA at mag=4.0); this one can be safely ignored.
     */
    public double getStdDev() throws IMRException {

       // throw a runtime exception if trying for MMI
       if(im.getName().equals(MMI_NAME))
         throw new RuntimeException(MMI_ERROR_STRING);

       //setupAttenRels();

       String imt = (String) im.getValue();
       double per = ((Double) periodParam.getValue()).doubleValue();
       double std = 0;
       if(imt.equals(this.SA_NAME) && ( per >= 3.0 )) {
         std += as_1997_attenRel.getStdDev();
         std += cb_2003_attenRel.getStdDev();
         std += scemy_1997_attenRel.getStdDev();
         return std/3.0;
       }
       else {
         std += as_1997_attenRel.getStdDev();
         std += cb_2003_attenRel.getStdDev();
         std += bjf_1997_attenRel.getStdDev();
         std += scemy_1997_attenRel.getStdDev();
         return std/4.0;
       }
    }


    public void setParamDefaults(){

        //((ParameterAPI)this.iml).setValue( IML_DEFAULT );
        vs30Param.setValue( VS30_DEFAULT );
        magParam.setValue( MAG_DEFAULT );
        saParam.setValue( SA_DEFAULT );
        periodParam.setValue( PERIOD_DEFAULT );
        dampingParam.setValue(DAMPING_DEFAULT);
        pgaParam.setValue( PGA_DEFAULT );
        pgvParam.setValue(PGV_DEFAULT);
        mmiParam.setValue(MMI_DEFAULT);
        componentParam.setValue( COMPONENT_DEFAULT );
        stdDevTypeParam.setValue( STD_DEV_TYPE_DEFAULT );

    }


    /**
     * This creates the lists of independent parameters that the various dependent
     * parameters (mean, standard deviation, exceedance probability, and IML at
     * exceedance probability) depend upon. NOTE: these lists do not include anything
     * about the intensity-measure parameters or any of thier internal
     * independentParamaters.
     */
    protected void initIndependentParamLists(){

        // params that the mean depends upon
        meanIndependentParams.clear();
        meanIndependentParams.addParameter( vs30Param );
        meanIndependentParams.addParameter( magParam );
        meanIndependentParams.addParameter( componentParam );

        // params that the stdDev depends upon
        stdDevIndependentParams.clear();
        stdDevIndependentParams.addParameter(stdDevTypeParam);
        stdDevIndependentParams.addParameter( componentParam );
        stdDevIndependentParams.addParameter( magParam );

        // params that the exceed. prob. depends upon
        exceedProbIndependentParams.clear();
        exceedProbIndependentParams.addParameter( vs30Param );
        exceedProbIndependentParams.addParameter( magParam );
        exceedProbIndependentParams.addParameter( componentParam );
        exceedProbIndependentParams.addParameter(stdDevTypeParam);
        exceedProbIndependentParams.addParameter(this.sigmaTruncTypeParam);
        exceedProbIndependentParams.addParameter(this.sigmaTruncLevelParam);

        // params that the IML at exceed. prob. depends upon
        imlAtExceedProbIndependentParams.addParameterList(exceedProbIndependentParams);
        imlAtExceedProbIndependentParams.addParameter(exceedProbParam);

    }


    /**
     *  Creates the willsSiteParam site parameter and adds it to the siteParams list.
     *  Makes the parameters noneditable.
     */
    protected void initSiteParams( ) {

        // create willsSiteType Parameter:
        super.initSiteParams();
/*
        // create and add the warning constraint:
        ArrayList willsSiteTypes = new ArrayList();
        willsSiteTypes.add(WILLS_SITE_B);
        willsSiteTypes.add(WILLS_SITE_BC);
        willsSiteTypes.add(WILLS_SITE_C);
        willsSiteTypes.add(WILLS_SITE_CD);
        willsSiteTypes.add(WILLS_SITE_D);
        willsSiteTypes.add(WILLS_SITE_DE);
        willsSiteTypes.add(WILLS_SITE_E);

        willsSiteParam = new StringParameter(WILLS_SITE_NAME,willsSiteTypes,WILLS_SITE_DEFAULT);
        willsSiteParam.setInfo( WILLS_SITE_INFO );
        willsSiteParam.setNonEditable();
*/
        // create vs30 Parameter:
        DoubleConstraint warn = new DoubleConstraint(VS30_WARN_MIN, VS30_WARN_MAX);
        warn.setNonEditable();
        vs30Param.setWarningConstraint(warn);
        vs30Param.addParameterChangeWarningListener( warningListener );
        vs30Param.setNonEditable();



        // add it to the siteParams list:
        siteParams.clear();
        siteParams.addParameter( vs30Param );

    }


    /**
     *  Creates the two Potential Earthquake parameters (magParam and
     *  fltTypeParam) and adds them to the probEqkRuptureParams
     *  list. Makes the parameters noneditable.
     */
    protected void initProbEqkRuptureParams(  ) {

        // Create magParam - is this even used?
        super.initProbEqkRuptureParams();

    }

    /**
     *  Creates the single Propagation Effect parameter and adds it to the
     *  propagationEffectParams list. Makes the parameters noneditable.
     */
    protected void initPropagationEffectParams( ) {

    }


    /**
     *  Creates the two supported IM parameters (PGA and SA), as well as the
     *  independenParameters of SA (periodParam and dampingParam) and adds
     *  them to the supportedIMParams list. Makes the parameters noneditable.
     */
    protected void initSupportedIntensityMeasureParams( ) {

        // Create saParam (& its dampingParam) and pgaParam:
        super.initSupportedIntensityMeasureParams();

        supportedIMParams.clear();

        // Create saParam's "Period" independent parameter:
        DoubleDiscreteConstraint periodConstraint = new DoubleDiscreteConstraint();
        periodConstraint.addDouble( 0.0 );
        periodConstraint.addDouble( 0.1 );
        periodConstraint.addDouble( 0.2 );
        periodConstraint.addDouble( 0.3 );
        periodConstraint.addDouble( 0.4 );
        periodConstraint.addDouble( 0.5 );
        periodConstraint.addDouble( 0.75 );
        periodConstraint.addDouble( 1.0 );
        periodConstraint.addDouble( 1.5 );
        periodConstraint.addDouble( 2.0 );
        periodConstraint.addDouble( 3.0 );
        periodConstraint.addDouble( 4.0 );
        periodConstraint.setNonEditable();
        periodParam = new DoubleDiscreteParameter( PERIOD_NAME, periodConstraint, PERIOD_UNITS, new Double(1.0) );
        periodParam.setInfo( PERIOD_INFO );
        periodParam.setNonEditable();

        // Set damping constraint as non editable since no other options exist
        dampingConstraint.setNonEditable();

        // Add SA's independent parameters:
        saParam.addIndependentParameter(dampingParam);
        saParam.addIndependentParameter(periodParam);

        // Now Make the parameter noneditable:
        saParam.setNonEditable();

        // Add the warning listeners:
        saParam.addParameterChangeWarningListener( warningListener );

        // Put parameters in the supportedIMParams list:
        supportedIMParams.addParameter( saParam );

        // now do the PGA param
        pgaParam.addParameterChangeWarningListener( warningListener );
        pgaParam.setNonEditable();
        supportedIMParams.addParameter( pgaParam );

        //Create PGV Parameter (pgvParam):
        DoubleConstraint pgvConstraint = new DoubleConstraint(PGV_MIN, PGV_MAX);
        pgvConstraint.setNonEditable();
        pgvParam = new WarningDoubleParameter( PGV_NAME, pgvConstraint, PGV_UNITS);
        pgvParam.setInfo( PGV_INFO );
        DoubleConstraint warn = new DoubleConstraint(PGV_WARN_MIN, PGV_WARN_MAX);
        warn.setNonEditable();
        pgvParam.setWarningConstraint(warn);
        pgvParam.addParameterChangeWarningListener( warningListener );
        pgvParam.setNonEditable();
        supportedIMParams.addParameter( pgvParam );

        // The MMI parameter
        mmiParam = new DoubleParameter(MMI_NAME,MMI_MIN,MMI_MAX);
        mmiParam.setInfo( MMI_INFO );
        mmiParam.setNonEditable();
        supportedIMParams.addParameter( mmiParam );


    }

    /**
     *  Creates other Parameters that the mean or stdDev depends upon,
     *  such as the Component or StdDevType parameters.
     */
    protected void initOtherParams( ) {

        // init other params defined in parent class
        super.initOtherParams();

        // the Component Parameter
        StringConstraint constraint = new StringConstraint();
        constraint.addString( COMPONENT_AVE_HORZ );
        constraint.addString( COMPONENT_GREATER_OF_TWO_HORZ );
        constraint.setNonEditable();
        componentParam = new StringParameter( COMPONENT_NAME, constraint, COMPONENT_DEFAULT );
        componentParam.setInfo( COMPONENT_INFO );
        componentParam.setNonEditable();

        // the stdDevType Parameter
        StringConstraint stdDevTypeConstraint = new StringConstraint();
        stdDevTypeConstraint.addString( STD_DEV_TYPE_TOTAL );
        stdDevTypeConstraint.addString( STD_DEV_TYPE_NONE );
        stdDevTypeConstraint.setNonEditable();
        stdDevTypeParam = new StringParameter( STD_DEV_TYPE_NAME, stdDevTypeConstraint, STD_DEV_TYPE_DEFAULT );
        stdDevTypeParam.setInfo( STD_DEV_TYPE_INFO );
        stdDevTypeParam.setNonEditable();

        // add these to the list
        otherParams.addParameter( componentParam );
        otherParams.addParameter( stdDevTypeParam );

    }

       /**
        * get the name of this IMR
        *
        * @returns the name of this IMR
        */
    public String getName() {
       return NAME;
    }




    /**
     *  <b>Title:</b> BJF_1997_AttenRelCoefficients<br>
     *  <b>Description:</b> This class encapsulates all the
     *  coefficients needed to calculate the Mean and StdDev for
     *  the BJF_1997_AttenRel.  One instance of this class holds the set of
     *  coefficients for each period (one row of their table 8).<br>
     *  <b>Copyright:</b> Copyright (c) 2001 <br>
     *  <b>Company:</b> <br>
     *
     *
     * @author     Steven W Rock
     * @created    February 27, 2002
     * @version    1.0
     */


    // this method, required by the API, does nothing here (it's not needed).
    protected void initCoefficients(  ) {

    }

    // this method, required by the API, does nothing here (it's not needed).
    protected void setPropagationEffectParams(  ) {

    }


    // this is temporary for testing purposes
    public static void main(String[] args) {
      CGS_USGS_2003_AttenRel ar = new CGS_USGS_2003_AttenRel(null);
    }

}



