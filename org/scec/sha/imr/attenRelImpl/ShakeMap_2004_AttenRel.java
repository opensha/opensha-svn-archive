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
import org.scec.data.function.DiscretizedFuncAPI;
import org.scec.calc.GaussianDistCalc;
import org.scec.util.*;

/**
* <b>Title:</b> ShakeMap_2004_AttenRel<p>
*
* <b>Description:</b> This attenuation relationship computes a mean IML, exceedance
* probabilty at IML, or IML at exceedance probability that represents an average of
* 3-4 previously published relationships (the ones used for California in the 2002
* National Seismic Hazard Maps; these are listed below).  For each relationship,
* the predicted rock-site mean is multiplied by Borcherdt's nonlinear amplification
* factor (1994, Earthquake Spectra, Vol. 10, No. 4, 617-653) as described below.
* That is, the original site effect model of each relationship is not used.  The
* averaging is performed after the site-depenent value for each relationship is
* computed.  The exceedance probabilities are log-averaged, whereas the other two
* (mean and IML at prob.) are linearly averaged because they are already in log domain.<p>
*
* Supported Intensity-Measure Parameters:
* <UL>
* <LI>Peak Ground Acceleration (PGA)
* <LI>Spectral Acceleration (SA) at the following periods: 0.0, 0.1, 0.2 0.3, 0.4,
* 0.5, 0.75 1.0, 1.5, 2.0, 3.0, and 4.0 seconds
* <LI>Peak Ground Velocity (PGV) - computed from 1-sec SA using the Newmark-Hall (1982) scalar
* (applied after the amplification)
* <LI>Modified Mercalli Intensity (MMI) computed from PGA and PGV as in Wald et al.
* (1999, Earthquake Spectra, Vol. 15, No. 3, 557-564))
* </UL><p>
*
* Attenuation Relationships used for the average:
* <UL>
* <LI>Abrahamson & Silva (1997) with site type "Rock/Shallow-Soil"
* <LI>Boore, Joyner & Fumal (1997) with Vs30 = 760 m/sec
* <LI>Sadigh et al (1997) with site type "Rock"
* <LI>Campbell and Bozorgnia (2003) with site type "BC_Boundary"
* </UL><p>
* Independent Parameters:
* <UL>
* <LI>vs30Param - The average 30-meter shear-wave velocity (m/sec)
* <LI>componentParam - Component of shaking (either "Average Horizontal" or "Greater of Two Horz.")
* <LI>stdDevTypeParam - The type of standard deviation (either "Total" or "None (zero)")
* </UL><p>
* Important Notes:
* <UL>
* The Borcherdt (1994) nonlinear amplification factors are applied as given in appendix-equations
* 7a or 7b (for periods � 0.5 and  > 0.5 seconds, respectively) using a reference velocity of 760 m/sec
* (and with the mv and ma coefficients linearly interpolated at intermediate input ground motions).
* Applying the mid-period amplification factors above 2.0 seconds for SA may not be legitimate. <p>
* For the one relationship that has a site-type dependent standard deviation
* (Sadigh et al., 1997) only the rock-site value is used (the difference is minor). <p>
* The Boore, Joyner & Fumal (1997) relationship is not included in the average for SA periods
* above 2.0 seconds. <p>
* For Boore, Joyner & Fumal (1997) the component is set as "Random Horizontal"
* (rather than "Average Horizontal") to be consistent with how this was set in the
* 2002 National Seismic Hazard Maps.  All others are set as "Average Horizontal". <p>
* For Campbell and Bozorgnia (2003) the magnitude dependent standard deviation is used. <p>
* This class supports a "Greater of Two Horz." component by multiplying the average horizontal
* component  median by a factor of 1.15.  This value was taken directly from the official ShakeMap
* documentation.  The standard deviation for this component is set the same as the average
* horizontal (not sure if this is correct).  <p>
* </UL><p>
* Developer Notes:
* <UL>
* Regarding the Modified Mercalli Intensity (MMI) IMT, note that what is returned by
* the getMean() method is the natural-log of MMI.  Although this is not technically
* correct (since MMI is not log-normally distributed), it was the easiest way to implement
* it for now.  Furthermore, because the probability distribution of MMI (computed from PGA
* or PGV) is presently unknown, we cannot compute the standard deviation, probability of
* exceedance, or the IML at any probability other than 0.5.  Therefore, a RuntimeException
* is thrown if one tries any of these when the chosen IMT is MMI.  We can relax this when
* someone comes up with the probability distribution (which can't be Gaussian because
* MMI values below 1 and above 10 are not allowed).<p>
* Several methods for this class have been overridden to throw Runtime Exceptions, either because
* it was not clear what to return or because the info is complicated and not necessarily useful.
* For example, it's not clear what to return from getStdDev(); one could return the
* average of the std. dev. of the four relationships, but nothing actually uses such an average (the probability of exceedance calculation
* uses the mean/stdDev for each relationship separately).  Another example is what to return
* from the getPropagationEffectParamsIterator - all of the three distance measures
* used by the four relationships? - this would lead to confusion and possible inconsistencies
* in the AttenuationRelationshipApplet.  The bottom line is we've maintained the
* IntensityMeasureRelationshipAPI, but not the AttenuationRelationshipAPI (so this
* relationship cannot be added to the AttenuationRelationshipApplet).  This class could
* simply be a subclass of IntensityMeasureRelationship.  however, it's not because it
* uses some of the private methods of AttenuationRelationship. <p>
*
* @author     Edward H. Field
* @created    May, 2004
* @version    1.0
*/


public class ShakeMap_2004_AttenRel
        extends AttenuationRelationship
        implements
       AttenuationRelationshipAPI,
       NamedObjectAPI {

   // debugging stuff:
   private final static String C = "ShakeMap_2004_AttenRel";
   private final static boolean D = false;
   public final static String NAME = "ShakeMap (2004)";

   // attenuation relationships used.
   private final AS_1997_AttenRel as_1997_attenRel;
   private final CB_2003_AttenRel cb_2003_attenRel;
   private final SCEMY_1997_AttenRel scemy_1997_attenRel;
   private final BJF_1997_AttenRel bjf_1997_attenRel;

   private double vs30;
   private static final double VS30_REF = 760;

   // The Borcherdt (2004) site amplification calculator
   Borcherdt2004_SiteAmpCalc borcherdtAmpCalc = new Borcherdt2004_SiteAmpCalc();

   // the site object for the BC boundary
   private Site site_BC;

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
   public final static String UNSUPPORTED_METHOD_ERROR = "This method is not supprted";


   // for issuing warnings:
   private transient  ParameterChangeWarningListener warningListener = null;



   /**
    *  No-Arg constructor. This initializes several ParameterList objects.
    */
   public ShakeMap_2004_AttenRel( ParameterChangeWarningListener warningListener ) {

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

       // init the BC boundary site object, and set it in the attenuation relationships:
       site_BC = new Site();
       this.propEffect = new PropagationEffect();

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
    *  This sets the probEqkRupture related parameters (magParam
    *  and fltTypeParam) based on the probEqkRupture passed in.
    *  The internally held probEqkRupture object is also set as that
    *  passed in.  Warning constrains are ingored.
    *
    * @param  probEqkRupture  The new probEqkRupture value
    */
   public void setPropagationEffect(PropagationEffect propEffect) {

     this.site = propEffect.getSite();
     this.probEqkRupture = propEffect.getProbEqkRupture();

     this.propEffect.setProbEqkRupture(probEqkRupture);

     vs30Param.setValueIgnoreWarning( site.getParameter( VS30_NAME ).getValue() );

     // set the location of the BC bounday site object
     site_BC.setLocation(site.getLocation());
     this.propEffect.setSite(site_BC);

     as_1997_attenRel.setPropagationEffect(propEffect);
     bjf_1997_attenRel.setPropagationEffect(propEffect);
     scemy_1997_attenRel.setPropagationEffect(propEffect);
     cb_2003_attenRel.setPropagationEffect(propEffect);
   }


   /**
    *  This sets the probEqkRupture.
    *
    * @param  probEqkRupture
    */
   public void setProbEqkRupture( ProbEqkRupture probEqkRupture ) throws ConstraintException{

       // Set the probEqkRupture
       this.probEqkRupture = probEqkRupture;
/*
       // set the EqkRup in the atten relations
       as_1997_attenRel.setProbEqkRupture(probEqkRupture);
       bjf_1997_attenRel.setProbEqkRupture(probEqkRupture);
       scemy_1997_attenRel.setProbEqkRupture(probEqkRupture);
       cb_2003_attenRel.setProbEqkRupture(probEqkRupture);
*/

       this.propEffect.setProbEqkRupture(probEqkRupture);
       if(propEffect.getSite() != null) {
         as_1997_attenRel.setPropagationEffect(propEffect);
         bjf_1997_attenRel.setPropagationEffect(propEffect);
         scemy_1997_attenRel.setPropagationEffect(propEffect);
         cb_2003_attenRel.setPropagationEffect(propEffect);
       }


   }


   /**
    *  This sets the site-related parameter (vs30Param) based on what is in
    *  the Site object passed in (the Site object must have a parameter with
    *  the same name as that in willsSiteParam).  This also sets the internally held
    *  Site object as that passed in.  Warning constrains are ingored.
    *
    * @param  site             The new site value which contains a Wills site Param.
    * @throws ParameterException Thrown if the Site object doesn't contain a
    * Wills site parameter
    */
   public void setSite( Site site ) throws ParameterException, IMRException, ConstraintException {


       vs30Param.setValueIgnoreWarning( site.getParameter( this.VS30_NAME ).getValue() );
       this.site = site;

       // set the location of the BC bounday site object
       site_BC.setLocation(site.getLocation());

/*
       // set the  BC Site in the attenuation relations
       as_1997_attenRel.setSite(site_BC);
       bjf_1997_attenRel.setSite(site_BC);
       scemy_1997_attenRel.setSite(site_BC);
       cb_2003_attenRel.setSite(site_BC);
*/
       this.propEffect.setSite(site_BC);
       if(this.probEqkRupture != null) {
         as_1997_attenRel.setPropagationEffect(propEffect);
         bjf_1997_attenRel.setPropagationEffect(propEffect);
         scemy_1997_attenRel.setPropagationEffect(propEffect);
         cb_2003_attenRel.setPropagationEffect(propEffect);
       }


  }



   /**
    * Note that for MMI this returns the natural log of MMI (this should be changed later)
    * @return
    * @throws IMRException
    */
   public double getMean() throws IMRException{

     vs30 = ((Double) vs30Param.getValue()).doubleValue();

     // set the IMT in the various relationships
     setAttenRelsIMT();

     String imt = (String) im.getName();
     double per = ((Double) periodParam.getValue()).doubleValue();
     double mean = 0;
     if(imt.equals(this.SA_NAME) && ( per >= 3.0 )) {
       mean += getMean(as_1997_attenRel);
       mean += getMean(cb_2003_attenRel);
       mean += getMean(scemy_1997_attenRel);
       return mean/3.0;
     }
     else {
       mean += getMean(as_1997_attenRel);
       mean += getMean(cb_2003_attenRel);
       mean += getMean(bjf_1997_attenRel);
       mean += getMean(scemy_1997_attenRel);
       return mean/4.0;
     }
   }

   /**
    * This assumes that vs30 has been set, and that the setAttenRelsStdDevTypes()
    * and setAttenRelsIMT() methods have already been called.
    * @param attenRel
    * @param iml
    * @return
    */
   private double getExceedProbability(AttenuationRelationship attenRel, double iml) {

     double mean = getMean(attenRel);
     double stdDev = attenRel.getStdDev();
     return getExceedProbability(mean, stdDev, iml);

   }




   /**
    * This returns the mean for the given attenuation relationship after assigning
    * the Borcherdt amplification factor.  This assumes that vs30 has been set and that
    * the setAttenRelsIMT(*) method has been called.
    * @param attenRel
    * @return
    */
   private double getMean(AttenuationRelationship attenRel) {

     double ave_bc, pga_bc, amp, mean;

     String imt = im.getName();

     if(imt.equals(PGA_NAME)) {
       pga_bc = attenRel.getMean();
       amp = borcherdtAmpCalc.getShortPeriodAmp(vs30,VS30_REF,pga_bc);
       mean = pga_bc + Math.log(amp);
     }
     else if (imt.equals(SA_NAME)) {
       ave_bc = attenRel.getMean();
       // now get PGA for amp factor
       attenRel.setIntensityMeasure(PGA_NAME);
       pga_bc = attenRel.getMean();
       attenRel.setIntensityMeasure(SA_NAME); // revert back
       double per = ((Double) periodParam.getValue()).doubleValue();
       if(per <= 0.5)
         amp = borcherdtAmpCalc.getShortPeriodAmp(vs30,VS30_REF,pga_bc);
       else
         amp = borcherdtAmpCalc.getMidPeriodAmp(vs30,VS30_REF,pga_bc);
       mean = ave_bc + Math.log(amp);
     }
     else if (imt.equals(PGV_NAME)) {
       ave_bc = attenRel.getMean();
       // now get PGA for amp factor
       attenRel.setIntensityMeasure(PGA_NAME);
       pga_bc = attenRel.getMean();
       attenRel.setIntensityMeasure(SA_NAME); // revert back
       amp = borcherdtAmpCalc.getMidPeriodAmp(vs30,VS30_REF,pga_bc);
       mean = ave_bc + Math.log(amp) + Math.log(37.27*2.54);  // last term is the PGV conversion
     }
     else { // it must be MMI
       // here we must set the imt because it wasn't done in the setAttenRelsIMT(*) method
       attenRel.setIntensityMeasure(SA_NAME);
       attenRel.getParameter(PERIOD_NAME).setValue(new Double(1.0));
       ave_bc = attenRel.getMean();
       attenRel.setIntensityMeasure(PGA_NAME);
       pga_bc = attenRel.getMean();
       amp = borcherdtAmpCalc.getMidPeriodAmp(vs30,VS30_REF,pga_bc);
       double pgv = ave_bc + Math.log(amp) + Math.log(37.27*2.54);
       amp = borcherdtAmpCalc.getShortPeriodAmp(vs30,VS30_REF,pga_bc);
       double pga = pga_bc + Math.log(amp);
       double mmi = Wald_MMI_Calc.getMMI(Math.exp(pga),Math.exp(pgv));
       mean = Math.log(mmi);
     }

     // correct for component if necessary
     String comp = (String) componentParam.getValue();
     if(comp.equals(COMPONENT_GREATER_OF_TWO_HORZ))
       mean += 0.139762;        // add ln(1.15)

     return mean;

   }



   /**
    * This sets the intensity measure for each of the four relationships.  This doesn nothing
    * if imt = MMI.
    */
   private void setAttenRelsIMT() {
     String imt = im.getName();
     if(imt.equals(PGA_NAME)) {
       as_1997_attenRel.setIntensityMeasure(PGA_NAME);
       scemy_1997_attenRel.setIntensityMeasure(PGA_NAME);
       cb_2003_attenRel.setIntensityMeasure(PGA_NAME);
       bjf_1997_attenRel.setIntensityMeasure(PGA_NAME);
     }
     else if(imt.equals(SA_NAME)) {
       Double per = (Double) periodParam.getValue();
       as_1997_attenRel.setIntensityMeasure(SA_NAME);
       as_1997_attenRel.getParameter(PERIOD_NAME).setValue(per);
       scemy_1997_attenRel.setIntensityMeasure(SA_NAME);
       scemy_1997_attenRel.getParameter(PERIOD_NAME).setValue(per);
       cb_2003_attenRel.setIntensityMeasure(SA_NAME);
       cb_2003_attenRel.getParameter(PERIOD_NAME).setValue(per);
       if(per.doubleValue() <= 2.0) {
         bjf_1997_attenRel.setIntensityMeasure(SA_NAME);
         bjf_1997_attenRel.getParameter(PERIOD_NAME).setValue(per);
       }
     }
     else if (imt.equals(PGV_NAME)) {
       Double per = new Double(1.0);
       as_1997_attenRel.setIntensityMeasure(SA_NAME);
       as_1997_attenRel.getParameter(PERIOD_NAME).setValue(per);
       scemy_1997_attenRel.setIntensityMeasure(SA_NAME);
       scemy_1997_attenRel.getParameter(PERIOD_NAME).setValue(per);
       cb_2003_attenRel.setIntensityMeasure(SA_NAME);
       cb_2003_attenRel.getParameter(PERIOD_NAME).setValue(per);
       if(per.doubleValue() <= 2.0) {
         bjf_1997_attenRel.setIntensityMeasure(SA_NAME);
         bjf_1997_attenRel.getParameter(PERIOD_NAME).setValue(per);
       }
     }
   }



   /**
    * This sets the standard deviation type for all the attenuation relations.
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
    *  This overrides the parent class method.
    *
    * @return                         The intensity-measure level
    * @exception  ParameterException  Description of the Exception
    */
   public double getIML_AtExceedProb() throws ParameterException {

       if ( ( exceedProbParam == null ) || ( exceedProbParam.getValue() == null ) )
           throw new ParameterException( C +
                   ": getExceedProbability(): " + "exceedProbParam or its value is null, unable to run this calculation."
                    );

       double exceedProb = ( ( Double ) ( ( ParameterAPI ) exceedProbParam ).getValue() ).doubleValue();
       double stRndVar;
       String sigTrType = (String) sigmaTruncTypeParam.getValue();


       // compute the iml from exceed probability based on truncation type:

       // check for the simplest, most common case (median from symmectric truncation)
       if( !sigTrType.equals( SIGMA_TRUNC_TYPE_1SIDED ) && exceedProb == 0.5 ) {
         return getMean();
       }
       else {
         //  throw exception if it's MMI
         if(im.getName().equals(MMI_NAME))
           throw new RuntimeException (MMI_ERROR_STRING);

         // get the stRndVar dep on sigma truncation type and level
         if ( sigTrType.equals( SIGMA_TRUNC_TYPE_NONE ) )
           stRndVar = GaussianDistCalc.getStandRandVar(exceedProb, 0, 0, 1e-6);
         else {
           double numSig = ( ( Double ) ( ( ParameterAPI ) sigmaTruncLevelParam ).getValue() ).doubleValue();
           if ( sigTrType.equals( SIGMA_TRUNC_TYPE_1SIDED ) )
             stRndVar = GaussianDistCalc.getStandRandVar(exceedProb, 1, numSig, 1e-6);
           else
             stRndVar = GaussianDistCalc.getStandRandVar(exceedProb, 2, numSig, 1e-6);
         }

         // now comput the average IML over all the attenuation relationships
         double ave_iml=0;
         vs30 = ((Double) vs30Param.getValue()).doubleValue();
         setAttenRelsStdDevTypes();
         setAttenRelsIMT();

         String imt = (String) im.getName();
         double per = ((Double) periodParam.getValue()).doubleValue();
         if(imt.equals(this.SA_NAME) && ( per >= 3.0 )) {
           ave_iml += getMean(as_1997_attenRel)+stRndVar*as_1997_attenRel.getStdDev();
           ave_iml += getMean(scemy_1997_attenRel)+stRndVar*scemy_1997_attenRel.getStdDev();
           ave_iml += getMean(cb_2003_attenRel)+stRndVar*cb_2003_attenRel.getStdDev();
           return ave_iml/3.0;
         }
         else {
           ave_iml += getMean(as_1997_attenRel)+stRndVar*as_1997_attenRel.getStdDev();
           ave_iml += getMean(scemy_1997_attenRel)+stRndVar*scemy_1997_attenRel.getStdDev();
           ave_iml += getMean(bjf_1997_attenRel)+stRndVar*bjf_1997_attenRel.getStdDev();
           ave_iml += getMean(cb_2003_attenRel)+stRndVar*cb_2003_attenRel.getStdDev();
           return ave_iml/4.0;
         }
       }
   }

   /**
    * This throws and exception because the method is not supported
    *
    */
   public double getStdDev() throws IMRException {
     throw new RuntimeException(UNSUPPORTED_METHOD_ERROR);
   }


   /**
    *  This calculates the probability that the given iml will be exceeded.
    * This assumes that vs30 has been set, and that the setAttenRelsStdDevTypes()
    * and setAttenRelsIMT() methods have already been called.
    *
    * @return                         The exceedProbability value
    * @exception  ParameterException  Description of the Exception
    * @exception  IMRException        Description of the Exception
    */
   private double getExceedProbability(double vs30, double iml) throws ParameterException, IMRException {

     double per = ((Double) periodParam.getValue()).doubleValue();
     double prob = 0;
     if(im.getName().equals(SA_NAME) && ( per >= 3.0 )) {
       prob += Math.log(getExceedProbability(as_1997_attenRel, iml));
       prob += Math.log(getExceedProbability(cb_2003_attenRel, iml));
       prob += Math.log(getExceedProbability(scemy_1997_attenRel, iml));
       return Math.exp(prob/3.0);
     }
     else {
       prob += Math.log(getExceedProbability(as_1997_attenRel, iml));
       prob += Math.log(getExceedProbability(cb_2003_attenRel, iml));
       prob += Math.log(getExceedProbability(bjf_1997_attenRel, iml));
       prob += Math.log(getExceedProbability(scemy_1997_attenRel, iml));
       return Math.exp(prob/4.0);
     }
   }




   /**
    *  This calculates the probability that the intensity-measure level
    *  (the value in the Intensity-Measure Parameter) will be exceeded
    *  given the mean and stdDev computed from current independent parameter
    *  values.  Note that the answer is not stored in the internally held
    *  exceedProbParam (this latter param is used only for the
    *  getIML_AtExceedProb() method).
    *
    * @return                         The exceedProbability value
    * @exception  ParameterException  Description of the Exception
    * @exception  IMRException        Description of the Exception
    */
   public double getExceedProbability() throws ParameterException, IMRException {

     // throw exception if MMI was chosen
     if(im.getName().equals(MMI_NAME))
        throw new RuntimeException (MMI_ERROR_STRING);

     // set vs30
     vs30 = ((Double) vs30Param.getValue()).doubleValue();

     // set the standard deviation types
     setAttenRelsStdDevTypes();

     // set the IMT in the various relationships
     setAttenRelsIMT();

     return getExceedProbability(vs30,((Double)im.getValue()).doubleValue());
   }


   /**
    *  This fills in the exceedance probability for multiple intensityMeasure
    *  levels (often called a "hazard curve"); the levels are obtained from
    *  the X values of the input function, and Y values are filled in with the
    *  asociated exceedance probabilities. NOTE: THE PRESENT IMPLEMENTATION IS
    *  STRANGE IN THAT WE DON'T NEED TO RETURN ANYTHING SINCE THE FUNCTION PASSED
    *  IN IS WHAT CHANGES (SHOULD RETURN NULL?).
    *
    * @param  intensityMeasureLevels  The function to be filled in
    * @return                         The function filled in
    * @exception  ParameterException  Description of the Exception
    */
   public DiscretizedFuncAPI getExceedProbabilities(
       DiscretizedFuncAPI intensityMeasureLevels
       ) throws ParameterException {

     // throw exception if MMI was chosen
     if(im.getName().equals(MMI_NAME))
        throw new RuntimeException (MMI_ERROR_STRING);

     DataPoint2D point;

     // set vs30
     vs30 = ((Double) vs30Param.getValue()).doubleValue();

     // set the standard deviation types in the various relationships
     setAttenRelsStdDevTypes();

     // set the IMT in the various relationships
     setAttenRelsIMT();

     Iterator it = intensityMeasureLevels.getPointsIterator();
     while ( it.hasNext() ) {
       point = ( DataPoint2D ) it.next();
       point.setY(getExceedProbability(vs30,point.getX()));
     }
     return intensityMeasureLevels;

   }



   public void setParamDefaults(){

       //((ParameterAPI)this.iml).setValue( IML_DEFAULT );
       vs30Param.setValue( VS30_DEFAULT );
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
       meanIndependentParams.addParameter( componentParam );

       // params that the stdDev depends upon
       stdDevIndependentParams.clear();
       stdDevIndependentParams.addParameter(stdDevTypeParam);
       stdDevIndependentParams.addParameter( componentParam );

       // params that the exceed. prob. depends upon
       exceedProbIndependentParams.clear();
       exceedProbIndependentParams.addParameter( vs30Param );
       exceedProbIndependentParams.addParameter( componentParam );
       exceedProbIndependentParams.addParameter(stdDevTypeParam);
       exceedProbIndependentParams.addParameter(sigmaTruncTypeParam);
       exceedProbIndependentParams.addParameter(sigmaTruncLevelParam);

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
    *  This does nothing
    */
   protected void initProbEqkRuptureParams(  ) {
   }

   /**
    *  This does nothing
    */
   protected void initPropagationEffectParams( ) {
   }


   /**
    *  Creates the supported IM parameters (PGA, PGV, MMI and SA), as well as the
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


   // this method, required by the API, does nothing here (it's not needed).
   protected void initCoefficients(  ) {

   }

   // this method, required by the API, does nothing here (it's not needed).
   protected void setPropagationEffectParams(  ) {

   }

   /**
    *  This is overridden to throw a runtine exception (the method is not supported).
    */
   public ListIterator getProbEqkRuptureParamsIterator() {
     throw new RuntimeException(UNSUPPORTED_METHOD_ERROR);
   }

   /**
    *  This is overridden to throw a runtine exception (the method is not supported).
    */
   public ListIterator getPropagationEffectParamsIterator() {
     throw new RuntimeException(UNSUPPORTED_METHOD_ERROR);
   }

   /**
    *  This is overridden to throw a runtine exception (the method is not supported).
    */
   public ListIterator getExceedProbIndependentParamsIterator() {
     throw new RuntimeException(UNSUPPORTED_METHOD_ERROR);
   }

   /**
    *  This is overridden to throw a runtine exception (the method is not supported).
    */
   public ListIterator getMeanIndependentParamsIterator() {
     throw new RuntimeException(UNSUPPORTED_METHOD_ERROR);
   }

   /**
    *  This is overridden to throw a runtine exception (the method is not supported).
    */
   public ListIterator getStdDevIndependentParamsIterator() {
     throw new RuntimeException(UNSUPPORTED_METHOD_ERROR);
   }

   /**
    *  This is overridden to throw a runtine exception (the method is not supported).
    */
   public ListIterator getIML_AtExceedProbIndependentParamsIterator() {
     throw new RuntimeException(UNSUPPORTED_METHOD_ERROR);
   }


   // this is temporary for testing purposes
   public static void main(String[] args) {
     ShakeMap_2004_AttenRel ar = new ShakeMap_2004_AttenRel(null);
   }

}



