package org.scec.sha.calc;


import org.scec.data.function.*;
import org.scec.data.Site;
import org.scec.sha.imr.*;
import org.scec.sha.earthquake.*;
/**
 * <p>Title: HazardCurveCalculator </p>
 * <p>Description: This class calculates the Hazard curve based on the
 * input parameters imr, site and eqkRupforecast</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta & Vipin Gupta
 * @date Oct 28, 2002
 * @version 1.0
 */

public class HazardCurveCalculator {

  protected final static String C = "HazardCurveCalculator";
  protected final static boolean D = true;

  // maximum permitted distance between fault and site to consider source in hazard analysis for that site
  protected final double MAX_DISTANCE = 200;

  /**
   * this function determines the hazard curve based on the parameters
   *
   * @param func : it has X values set and result will be returned in this function
   * @param site  : site parameter
   * @param imr  :selected IMR object
   * @param eqkRupForecast  : selected Earthquake rup forecast
   * @return
   */
  public void getHazardCurve(DiscretizedFuncAPI hazFunction,
        Site site, ClassicIMRAPI imr, EqkRupForecast eqkRupForecast) {

    try {
      // set the site in IMR
      imr.setSite(site);
     }catch (Exception ex) {
      if(D) System.out.println(C + ":Param warning caught"+ex);
      ex.printStackTrace();
    }

    // get total sources
    int numSources = eqkRupForecast.getNumSources();
    ArbitrarilyDiscretizedFunc condProbFunc = new ArbitrarilyDiscretizedFunc();
    for(int i=0;i < numSources ;i++) {

      if(D)  {
        if(i == (int) (numSources*0.9))
              System.out.println(C + " 10% done");
        if(i == (int) (numSources*0.8))
              System.out.println(C + " 20% done");
        if(i == (int) (numSources*0.7))
              System.out.println(C + " 30% done");
        if(i == (int) (numSources*0.6))
              System.out.println(C + " 40% done");
        if(i == (int) (numSources*0.5))
              System.out.println(C + " 50% done");
        if(i == (int) (numSources*0.4))
              System.out.println(C + " 60% done");
        if(i == (int) (numSources*0.3))
              System.out.println(C + " 70% done");
        if(i == (int) (numSources*0.2))
              System.out.println(C + " 80% done");
        if(i == (int) (numSources*0.1))
              System.out.println(C + " 90% done");
      }

      // get source and get its distance from the site
      ProbEqkSource source = eqkRupForecast.getSource(i);
      double distance = source.getMinDistance(site);

      // if source is greater than the MAX_DISTANCE, ignore the source
      if(distance > MAX_DISTANCE)
        continue;

      // for each source, get the number of ruptures
      int numRuptures = eqkRupForecast.getNumRuptures(i);
      for(int n=0; n < numRuptures ;n++) {
        // for each rupture, set in IMR and do computation
        double qkProb = ((ProbEqkRupture)source.getRupture(n)).getProbability();

        // initialize the values in condProbfunc with log values as passed in hazFunction
        initLogDiscretizeValues(hazFunction, condProbFunc);
        try {
          imr.setProbEqkRupture((ProbEqkRupture)eqkRupForecast.getRupture(i,n));
        } catch (Exception ex) {
          System.out.println("Parameter change warning caught");
        }
        // get the exceed probabillties for this IMR
        condProbFunc=(ArbitrarilyDiscretizedFunc)imr.getExceedProbabilities(condProbFunc);

        // calculate the hazard function
        int numPoints = condProbFunc.getNum();
        for(int k=0;k<numPoints;k++)
          hazFunction.set(k,hazFunction.getY(k)*Math.pow(1-qkProb,condProbFunc.getY(k)));
      }
    }

    int  numPoints = condProbFunc.getNum();

    // finalize the hazard function
    for(int i=0;i<numPoints;++i)
      hazFunction.set(i,1-hazFunction.getY(i));

  }


  /**
  * set x values in log space for condition Prob function to be passed to IMR
  * It accepts 2 parameters
  *
  * @param originalFunc :  this is the function with X values set
  * @param logFunc : this is the functin in which log X values are set
  */
 private void initLogDiscretizeValues(DiscretizedFuncAPI originalFunc,
                                      DiscretizedFuncAPI logFunc){

   int numPoints = originalFunc.getNum();
   for(int i=0; i<numPoints; ++i)
     logFunc.set(Math.log(originalFunc.getX(i)), 1);
 }

}