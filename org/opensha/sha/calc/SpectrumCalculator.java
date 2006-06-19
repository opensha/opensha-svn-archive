package org.opensha.sha.calc;

import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.data.Site;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.data.function.DiscretizedFuncAPI;
import org.opensha.sha.imr.AttenuationRelationshipAPI;
import org.opensha.sha.earthquake.ProbEqkSource;
import java.util.ArrayList;

/**
 * <p>Title: SpectrumCalculator</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SpectrumCalculator{


  protected final static String C = "SpectrumCalculator";
  protected final static boolean D = false;

  /* maximum permitted distance between fault and site to consider source in
   hazard analysis for that site; this default value is to allow all PEER test
   cases to pass through
   */
  public final static double MAX_DISTANCE_DEFAULT = 200;
  protected double MAX_DISTANCE = MAX_DISTANCE_DEFAULT;

  protected int currRuptures = -1;
  protected int totRuptures = 0;

  //index to keep track how many sources have been traversed
  protected int sourceIndex;
  // get total number of sources
  protected int numSources;

  /**
   * This sets the maximum distance of sources to be considered in the calculation
   * (as determined by the getMinDistance(Site) method of ProbEqkSource subclasses).
   * Sources more than this distance away are ignored.
   * Default value is 250 km.
   *
   * @param distance: the maximum distance in km
   */
  public void setMaxSourceDistance(double distance) throws java.rmi.RemoteException{
    MAX_DISTANCE = distance;
  }

  /**
   * This function computes a spectrum curve for the given Site, IMR, and ERF.  The curve
   * in place in the passed in hazFunction (with the X-axis values being the SA
   * Periods for which exceedance probabilites are desired).
   * @param hazFunction: This function is where the hazard curve is placed
   * @param site: site object
   * @param imr: selected IMR object
   * @param eqkRupForecast: selected Earthquake rup forecast
   * @return
   */
  public DiscretizedFuncAPI getSpectrumCurve(Site site,
                                             AttenuationRelationshipAPI imr,
                                             EqkRupForecastAPI eqkRupForecast,
                                             double imlVal,
                                             ArrayList supportedSA_Periods) {

    //creating the Master function that initializes the Function with supported SA Periods Vals
    DiscretizedFuncAPI hazFunction = new ArbitrarilyDiscretizedFunc();
    initDiscretizeValues(hazFunction, supportedSA_Periods, 1.0);
    int numPoints = hazFunction.getNum();


     this.currRuptures = -1;

     /* this determines how the calucations are done (doing it the way it's outlined
     in the paper SRL gives probs greater than 1 if the total rate of events for the
     source exceeds 1.0, even if the rates of individual ruptures are << 1).
     */
     boolean poissonSource = false;

     //resetting the Parameter change Listeners on the AttenuationRelationship
     //parameters. This allows the Server version of our application to listen to the
     //parameter changes.
     ((AttenuationRelationship)imr).resetParameterEventListeners();

     //System.out.println("hazFunction: "+hazFunction.toString());

     // declare some varibles used in the calculation
     double qkProb, distance;
     int k;


     // set the maximum distance in the attenuation relationship
     // (Note- other types of IMRs may not have this method so we should really check type here)
     imr.setUserMaxDistance(MAX_DISTANCE);



     //Source func
     DiscretizedFuncAPI sourceHazFunc = new ArbitrarilyDiscretizedFunc();
     initDiscretizeValues(sourceHazFunc,supportedSA_Periods,0.0);

     // get total number of sources
     numSources = eqkRupForecast.getNumSources();
     //System.out.println("Number of Sources: "+numSources);
     //System.out.println("ERF info: "+ eqkRupForecast.getClass().getName());
     // compute the total number of ruptures for updating the progress bar
     totRuptures = 0;
     sourceIndex =0;
     for(sourceIndex=0;sourceIndex<numSources;++sourceIndex)
       totRuptures+=eqkRupForecast.getSource(sourceIndex).getNumRuptures();

     //System.out.println("Total number of ruptures:"+ totRuptures);


     // init the current rupture number (also for progress bar)
     currRuptures = 0;


     // set the Site in IMR
     imr.setSite(site);

     // this boolean will tell us whether a source was actually used
     // (e.g., all could be outside MAX_DISTANCE)
     boolean sourceUsed = false;

     if (D) System.out.println(C+": starting hazard curve calculation");

     // loop over sources
     for(sourceIndex=0;sourceIndex < numSources ;sourceIndex++) {

       // get the ith source
       ProbEqkSource source = eqkRupForecast.getSource(sourceIndex);

       // compute the source's distance from the site and skip if it's too far away
       distance = source.getMinDistance(site);
       if(distance > MAX_DISTANCE) {
         //update progress bar for skipped ruptures
         /*
         if(source.getRupture(0).getRuptureSurface().getNumCols() != 1) throw new RuntimeException("prob");
         System.out.println("rejected "+
         (float)source.getRupture(0).getRuptureSurface().getLocation(0,0).getLongitude()+"  "+
         (float)source.getRupture(0).getRuptureSurface().getLocation(0,0).getLatitude());
         */
         currRuptures += source.getNumRuptures();
         continue;
       }

       // indicate that a source has been used
       sourceUsed = true;

       // determine whether it's poissonian
       poissonSource = source.isSourcePoissonian();

       // get the number of ruptures for the current source
       int numRuptures = source.getNumRuptures();

       // loop over these ruptures
       for(int n=0; n < numRuptures ; n++,++currRuptures) {

         EqkRupture rupture = source.getRupture(n);
         // get the rupture probability
         qkProb = ((ProbEqkRupture)rupture).getProbability();

         // set the EqkRup in the IMR
         imr.setEqkRupture(rupture);

         DiscretizedFuncAPI condProbFunc = null;


         // get the conditional probability of exceedance from the IMR
         condProbFunc = (DiscretizedFuncAPI) imr.getSA_ExceedProbSpectrum(Math.log(
             imlVal));
         // For poisson source
         if(poissonSource) {
           /* First make sure the probability isn't 1.0 (or too close); otherwise rates are
              infinite and all IMLs will be exceeded (because of ergodic assumption).  This
              can happen if the number of expected events (over the timespan) exceeds ~37,
              because at this point 1.0-Math.exp(-num) = 1.0 by numerical precision (and thus,
              an infinite number of events).  The number 30 used in the check below provides a
              safe margin.
           */
           if(Math.log(1.0-qkProb) < -30.0)
             throw new RuntimeException("Error: The probability for this ProbEqkRupture ("+qkProb+
                                       ") is too high for a Possion source (~infinite number of events)");

           for(k=0;k<numPoints;k++)
             hazFunction.set(k,hazFunction.getY(k)*Math.pow(1-qkProb,condProbFunc.getY(k)));
         }
         // For non-Poissin source
         else
           for(k=0;k<numPoints;k++)
             sourceHazFunc.set(k,sourceHazFunc.getY(k) + qkProb*condProbFunc.getY(k));
       }
       // for non-poisson source:
       if(!poissonSource)
         for(k=0;k<numPoints;k++)
           hazFunction.set(k,hazFunction.getY(k)*(1-sourceHazFunc.getY(k)));
     }

     int i;
     // finalize the hazard function
     if(sourceUsed)
       for(i=0;i<numPoints;++i)
         hazFunction.set(i,1-hazFunction.getY(i));
     else
       for(i=0;i<numPoints;++i)
         hazFunction.set(i,0.0);
     if (D) System.out.println(C+"hazFunction.toString"+hazFunction.toString());
     return hazFunction;
   }



   /**
    *
    * @returns the current rupture being traversed
    * @throws java.rmi.RemoteException
    */
   public int getCurrRuptures() throws java.rmi.RemoteException {
     return this.currRuptures;
   }

   /**
    *
    * @returns the total number of ruptures in the earthquake rupture forecast model
    * @throws java.rmi.RemoteException
    */
   public int getTotRuptures() throws java.rmi.RemoteException {
     return this.totRuptures;
   }



   /**
    * Initialize the prob as 1 for the Hazard function
    *
    * @param arb
    */
   protected void initDiscretizeValues(DiscretizedFuncAPI arb, ArrayList supportedSA_Periods,
                                       double val){
     int num = supportedSA_Periods.size();
     for(int i=0;i<num;++i)
       arb.set(((Double)supportedSA_Periods.get(i)).doubleValue(),val);
   }


   /**
    * This function computes a deterministic exceedance curve for the given Site, IMR, and ProbEqkrupture.  The curve
    * in place in the passed in hazFunction (with the X-axis values being the IMLs for which
    * exceedance probabilites are desired).
    * @param hazFunction: This function is where the hazard curve is placed
    * @param site: site object
    * @param imr: selected IMR object
    * @param rupture: Single Earthquake Rupture
    * @return
    */
   public DiscretizedFuncAPI getDeterministicSpectrumCurve(
       Site site, AttenuationRelationshipAPI imr, EqkRupture rupture,
        boolean probAtIML, double imlProbVal) {


     //resetting the Parameter change Listeners on the AttenuationRelationship
     //parameters. This allows the Server version of our application to listen to the
     //parameter changes.
     ( (AttenuationRelationship) imr).resetParameterEventListeners();

     //System.out.println("hazFunction: "+hazFunction.toString());

     // set the Site in IMR
     imr.setSite(site);

     if (D) System.out.println(C + ": starting hazard curve calculation");

     // set the EqkRup in the IMR
     imr.setEqkRupture(rupture);

     DiscretizedFuncAPI hazFunction = null;
     if(probAtIML)
       // get the conditional probability of exceedance from the IMR
       hazFunction = (DiscretizedFuncAPI) imr.getSA_ExceedProbSpectrum(Math.log(imlProbVal));
     else{
       hazFunction = (DiscretizedFuncAPI) imr.getSA_IML_AtExceedProbSpectrum(
           imlProbVal);
       int numPoints = hazFunction.getNum();
       for(int i=0;i<numPoints;++i){
         hazFunction.set(i,Math.exp(hazFunction.getY(i)));
       }
     }
     if (D) System.out.println(C + "hazFunction.toString" + hazFunction.toString());
     return hazFunction;
  }
}
