 package org.scec.sha.imr.attenRelImpl;

import java.util.*;

import org.scec.data.*;
import org.scec.exceptions.*;
import org.scec.param.*;
import org.scec.param.event.*;
import org.scec.sha.earthquake.*;
import org.scec.sha.imr.*;
import org.scec.sha.imr.attenRelImpl.*;
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

    // the site object for the BC boundary
    private Site site_BC;

    // PGA thresholds for computing amp factors (convert from gals to g)
    private final static double pga_low  = -1.87692; // Math.log(150/980);
    private final static double pga_mid  = -1.36609; // Math.log(250/980);
    private final static double pga_high = -1.02962; // Math.log(350/980);

    /**
     * Thier maximum horizontal component option.
     */
    public final static String COMPONENT_GREATER_OF_TWO_HORZ = "Greater of Two Horz.";

    private StringParameter willsSiteParam = null;
    public final static String WILLS_SITE_NAME = "Wills Site Class";
    public final static String WILLS_SITE_INFO = "Site classification defined by Wills et al. (2000, BSSA)";
    public final static String WILLS_SITE_B = "B";
    public final static String WILLS_SITE_BC = "BC";
    public final static String WILLS_SITE_C = "C";
    public final static String WILLS_SITE_CD = "CD";
    public final static String WILLS_SITE_D = "D";
    public final static String WILLS_SITE_DE = "DE";
    public final static String WILLS_SITE_E = "E";
    public final static String WILLS_SITE_DEFAULT = WILLS_SITE_BC;


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

        ParameterAPI willsClass = site.getParameter( this.WILLS_SITE_NAME );
       // This may throw a constraint exception
        try{
          this.willsSiteParam.setValue( willsClass.getValue() );
        } catch (WarningException e){
          if(D) System.out.println(C+"Warning Exception:"+e);
        }

        // Now pass function up to super to set the site
        super.setSite( site );

        // set the location of the BC bounday site object
        site_BC.setLocation(site.getLocation());

    }



    /**
     * Note that for MMI this returns the natural log of MMI (this should be changed later)
     * @return
     * @throws IMRException
     */
    public double getMean() throws IMRException{

      String imt = im.getName();
      if(!imt.equals(MMI_NAME)) {
        return 0.0;
      }
      else
        return 0.0;  // return the log for now (until I figure a better way)

    }

    /**
     * This computes the nonlinear amplification factor according to the Wills site class,
     * IMT, and BC_PGA.
     * @return
     */
    private double getAmpFactor(String imt) {

      String S = ".getAmpFactor()";

      // get the PGA for B category

      // ??????????????????????????
      // set the imt to PGA
      double b_pga = getBC_Mean();

      if(D) {
        System.out.println(C+S+" b_pag (gals) = "+Math.exp(b_pga)*980.0);
//        System.out.println(C+"pga_low = "+pga_low);
//        System.out.println(C+"pga_mid = "+pga_mid);
//        System.out.println(C+"pga_high = "+pga_high);
      }

      // figure out whether we need short-period or mid-period amps
      boolean shortPeriod;
      if(imt.equals(PGA_NAME))
         shortPeriod = true;
      else if(imt.equals(PGV_NAME))
         shortPeriod = false;
      else if(imt.equals(SA_NAME)) {
        double per = ((Double)periodParam.getValue()).doubleValue();
        if(per <= 0.45)
          shortPeriod = true;
        else
          shortPeriod = false;
      }
      else
        throw new RuntimeException(C+"IMT not supported");

      if(D) {
        System.out.println(C+S+" shortPeriod = "+shortPeriod);
      }

      //now get the amp factor
      // These are from an email from Bruce Worden on 12/04/03
      // (also sent by Vince in an email earlier)
      String sType = (String) willsSiteParam.getValue();
      double amp=0;
      if(shortPeriod) {
        if(b_pga <= pga_low) {
          if     (sType.equals(WILLS_SITE_E))
             amp = 1.65;
          else if(sType.equals(WILLS_SITE_DE))
             amp = 1.34;
          else if(sType.equals(WILLS_SITE_D))
             amp = 1.33;
          else if(sType.equals(WILLS_SITE_CD))
             amp = 1.24;
          else if(sType.equals(WILLS_SITE_C))
             amp = 1.15;
          else if(sType.equals(WILLS_SITE_BC))
             amp = 0.98;
          else if(sType.equals(WILLS_SITE_B))
             amp = 1.00;
        }
        else if(b_pga <= pga_mid) {
          if     (sType.equals(WILLS_SITE_E))
             amp = 1.43;
          else if(sType.equals(WILLS_SITE_DE))
             amp = 1.23;
          else if(sType.equals(WILLS_SITE_D))
             amp = 1.23;
          else if(sType.equals(WILLS_SITE_CD))
             amp = 1.17;
          else if(sType.equals(WILLS_SITE_C))
             amp = 1.10;
          else if(sType.equals(WILLS_SITE_BC))
             amp = 0.99;
          else if(sType.equals(WILLS_SITE_B))
             amp = 1.00;
        }
        else if(b_pga <= pga_high) {
          if     (sType.equals(WILLS_SITE_E))
             amp = 1.15;
          else if(sType.equals(WILLS_SITE_DE))
             amp = 1.09;
          else if(sType.equals(WILLS_SITE_D))
             amp = 1.09;
          else if(sType.equals(WILLS_SITE_CD))
             amp = 1.06;
          else if(sType.equals(WILLS_SITE_C))
             amp = 1.04;
          else if(sType.equals(WILLS_SITE_BC))
             amp = 0.99;
          else if(sType.equals(WILLS_SITE_B))
             amp = 1.00;
        }
        else {
          if     (sType.equals(WILLS_SITE_E))
             amp = 0.93;
          else if(sType.equals(WILLS_SITE_DE))
             amp = 0.96;
          else if(sType.equals(WILLS_SITE_D))
             amp = 0.96;
          else if(sType.equals(WILLS_SITE_CD))
             amp = 0.97;
          else if(sType.equals(WILLS_SITE_C))
             amp = 0.98;
          else if(sType.equals(WILLS_SITE_BC))
             amp = 1.00;
          else if(sType.equals(WILLS_SITE_B))
             amp = 1.00;
        }
      }
      else {
        if(b_pga <= pga_low) {
          if     (sType.equals(WILLS_SITE_E))
             amp = 2.55;
          else if(sType.equals(WILLS_SITE_DE))
             amp = 1.72;
          else if(sType.equals(WILLS_SITE_D))
             amp = 1.71;
          else if(sType.equals(WILLS_SITE_CD))
             amp = 1.49;
          else if(sType.equals(WILLS_SITE_C))
             amp = 1.29;
          else if(sType.equals(WILLS_SITE_BC))
             amp = 0.97;
          else if(sType.equals(WILLS_SITE_B))
             amp = 1.00;
        }
        else if(b_pga <= pga_mid) {
          if     (sType.equals(WILLS_SITE_E))
             amp = 2.37;
          else if(sType.equals(WILLS_SITE_DE))
             amp = 1.65;
          else if(sType.equals(WILLS_SITE_D))
             amp = 1.64;
          else if(sType.equals(WILLS_SITE_CD))
             amp = 1.44;
          else if(sType.equals(WILLS_SITE_C))
             amp = 1.26;
          else if(sType.equals(WILLS_SITE_BC))
             amp = 0.97;
          else if(sType.equals(WILLS_SITE_B))
             amp = 1.00;
        }
        else if(b_pga <= pga_high) {
          if     (sType.equals(WILLS_SITE_E))
             amp = 2.14;
          else if(sType.equals(WILLS_SITE_DE))
             amp = 1.56;
          else if(sType.equals(WILLS_SITE_D))
             amp = 1.55;
          else if(sType.equals(WILLS_SITE_CD))
             amp = 1.38;
          else if(sType.equals(WILLS_SITE_C))
             amp = 1.23;
          else if(sType.equals(WILLS_SITE_BC))
             amp = 0.97;
          else if(sType.equals(WILLS_SITE_B))
             amp = 1.00;
        }
        else {
          if     (sType.equals(WILLS_SITE_E))
             amp = 1.91;
          else if(sType.equals(WILLS_SITE_DE))
             amp = 1.46;
          else if(sType.equals(WILLS_SITE_D))
             amp = 1.45;
          else if(sType.equals(WILLS_SITE_CD))
             amp = 1.32;
          else if(sType.equals(WILLS_SITE_C))
             amp = 1.19;
          else if(sType.equals(WILLS_SITE_BC))
             amp = 0.98;
          else if(sType.equals(WILLS_SITE_B))
             amp = 1.00;
        }
      }

      if(D) {
        System.out.println(C+S+"amp = "+amp);
      }

      // return the value
      return amp;

    }

    /**
     * This computes MMI (from PGA and PGV) using the relationship given by
     * Wald et al. (1999, Earthquake Spectra).  The code is a modified version
     * of what Bruce Worden sent me (Ned) on 12/04/03.  This could be a separate
     * utility (that takes pgv and pga as arguments) since others might want to use it.
     * @return
     */
    private double getMMI(){
      double pgv, pga;
      String S = ".getMMI()";

      // get PGA
      //coeffBJF = ( BJF_1997_AttenRelCoefficients )coefficientsBJF.get( PGA_NAME );
      //coeffSM = ( BJF_1997_AttenRelCoefficients )coefficientsSM.get( PGA_NAME );
      double b_pga = getBC_Mean();
      pga = b_pga + Math.log(getAmpFactor(PGA_NAME));
      // Convert to linear domain in gals (what's needed below)
      pga = Math.exp(pga)*980.0;

      if(D) System.out.println(C+S+" pga = "+(float) pga);

      // get PGV
      double b_pgv = getBC_Mean();
      pgv = b_pgv + Math.log(getAmpFactor(PGV_NAME));
      // Convert to linear domain (what's needed below)
      pgv = Math.exp(pgv);

      if(D) System.out.println(" pgv = "+(float) pgv);

      // now compute MMI
      double a_scale, v_scale;
      double sma     =  3.6598;
      double ba      = -1.6582;
      double sma_low =  2.1987;
      double ba_low  =  1;

      double smv     =  3.4709;
      double bv      =  2.3478;
      double smv_low =  2.0951;
      double bv_low  =  3.3991;

      double ammi; // Intensity from acceleration
      double vmmi; // Intensity from velocity

      ammi = (0.43429*Math.log(pga) * sma) + ba;
      if (ammi <= 5.0)
        ammi = (0.43429*Math.log(pga) * sma_low) + ba_low;

      vmmi = (0.43429*Math.log(pgv) * smv) + bv;
      if (vmmi <= 5.0)
        vmmi = (0.43429*Math.log(pgv) * smv_low) + bv_low;

      if (ammi < 1) ammi = 1;
      if (vmmi < 1) vmmi = 1;

      // use linear ramp between MMI 5 & 7 (ammi below and vmmi above, respectively)
      a_scale = (ammi - 5) / 2; // ramp
      if (a_scale > 1);
        a_scale = 1;
      if (a_scale < 0);
        a_scale = 0;

      a_scale = 1 - a_scale;

      v_scale = 1 - a_scale;

      double mmi = (a_scale * ammi) + (v_scale * vmmi);
      if (mmi < 1) mmi = 1 ;
      if (mmi > 10) mmi = 10;
//      return ((int) (mmi * 100)) / 100;
      return mmi;
    }


    private void setupAttenRels() {

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

      // set the EqkRup
      as_1997_attenRel.setProbEqkRupture(probEqkRupture);
      bjf_1997_attenRel.setProbEqkRupture(probEqkRupture);
      scemy_1997_attenRel.setProbEqkRupture(probEqkRupture);
      cb_2003_attenRel.setProbEqkRupture(probEqkRupture);

      // set the Site
      as_1997_attenRel.setSite(site_BC);
      bjf_1997_attenRel.setSite(site_BC);
      scemy_1997_attenRel.setSite(site_BC);
      cb_2003_attenRel.setSite(site_BC);

      // set the IMT
      as_1997_attenRel.setIntensityMeasure(im);
      bjf_1997_attenRel.setIntensityMeasure(im);
      scemy_1997_attenRel.setIntensityMeasure(im);
      cb_2003_attenRel.setIntensityMeasure(im);

    }

    /**
     * @return    The mean value
     */
    private double getBC_Mean(){

      setupAttenRels();

      String imt = (String) im.getValue();
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

       setupAttenRels();

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
        willsSiteParam.setValue( WILLS_SITE_DEFAULT );
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
        meanIndependentParams.addParameter( willsSiteParam );
        meanIndependentParams.addParameter( magParam );
        meanIndependentParams.addParameter( componentParam );

        // params that the stdDev depends upon
        stdDevIndependentParams.clear();
        stdDevIndependentParams.addParameter(stdDevTypeParam);
        stdDevIndependentParams.addParameter( componentParam );
        stdDevIndependentParams.addParameter( magParam );

        // params that the exceed. prob. depends upon
        exceedProbIndependentParams.clear();
        exceedProbIndependentParams.addParameter( willsSiteParam );
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

        // add it to the siteParams list:
        siteParams.clear();
        siteParams.addParameter( willsSiteParam );

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



