package org.scec.sha.calc;


import javax.swing.JFrame;
import javax.swing.JProgressBar;
import java.awt.Rectangle;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import org.scec.data.function.*;
import org.scec.data.Site;
import org.scec.sha.imr.*;
import org.scec.sha.earthquake.*;
import org.scec.sha.gui.infoTools.*;
/**
 * <p>Title: HazardCurveCalculator </p>
 * <p>Description: This class calculates the Hazard curve based on the
 * input parameters imr, site and eqkRupforecast</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Ned Field & Nitin Gupta & Vipin Gupta
 * @date Oct 28, 2002
 * @version 1.0
 */

public class HazardCurveCalculator {

  protected final static String C = "HazardCurveCalculator";
  protected final static boolean D = false;


  /* maximum permitted distance between fault and site to consider source in
  hazard analysis for that site; this default value is to allow all PEER test
  cases to pass through
  */
  protected double MAX_DISTANCE = 300;

  // boolean for telling whether to show a progress bar
  boolean showProgressBar = false;

  private CalcProgressBar progressClass ;

  /**
   * This sets the maximum distance of sources to be considered in the calculation
   * (as determined by the getMinDistance(Site) method of ProbEqkSource subclasses).
   * Sources more than this distance away are ignored.
   * Default value is 250 km.
   *
   * @param distance: the maximum distance in km
   */
  public void setMaxSourceDistance(double distance) {
    MAX_DISTANCE = distance;
  }


  /**
   * This allows tuning on or off the showing of a progress bar
   * @param show - set as true to show it, or false to not show it
   */
  public void showProgressBar(boolean show) {
    this.showProgressBar=show;
  }

  /**
   * This function determines the hazard curve based on the parameters.  Note that
   * two functions are taken here only for computational efficiency (otherwise there
   * is overhead in duplicating a function each time this method is called from,
   * for example, the HazardMapCalculator).  The first function contains the X-values
   * (IMLs) that will be used, and the second function will contain the desired Y
   * values when done.  The X values on the second funtion are ignored (so those on
   * the first can be the log of those on the second as needed of for SA, PGA, and PGV),
   * and the Y values on the first function are meaningless on entry and exit.  These
   * two functions must have the same number of points.
   *
   * @param condProbFunc: contains the X values used (IMLs) for computing the hazard
   * curve.
   * @param hazFunction: This contains the desired curve (Y values) for the
   * specified site.
   * @param site: site object
   * @param imr: selected IMR object
   * @param eqkRupForecast: selected Earthquake rup forecast
   * @return
   */
  public void getHazardCurve(DiscretizedFuncAPI hazFunction,
                             Site site, AttenuationRelationshipAPI imr, EqkRupForecast eqkRupForecast) {

    ArbitrarilyDiscretizedFunc test = (ArbitrarilyDiscretizedFunc) hazFunction.deepClone();


    // declare some varibles used in the calculation
    double qkProb, distance;
    int numPoints,k,i,totRuptures=0,currRuptures=0;

    // get total number of sources
    int numSources = eqkRupForecast.getNumSources();


    // check if progress bar is desired and set it up if so
    if(this.showProgressBar) {
      progressClass = new CalcProgressBar("Hazard-Curve Calc Status", "Beginning Calculation ");
      progressClass.displayProgressBar();

      // compute the total number of ruptures for updating the progress bar
      totRuptures = 0;
      for(i=0;i<numSources;++i)
        totRuptures+=eqkRupForecast.getSource(i).getNumRuptures();

      // init the current rupture number (also for progress bar)
      currRuptures = 0;

      // initialize the progress bar to zero ruptures
      progressClass.updateProgress(currRuptures, totRuptures);
    }

    // initialize the hazard function to 1.0
    initDiscretizeValues(hazFunction);

    // set the Site in IMR
    try {
      imr.setSite(site);
    }catch (Exception ex) {
      if(D) System.out.println(C + ":Param warning caught"+ex);
      ex.printStackTrace();
    }

    // this boolean will tell us whether a source was actually used
    // (e.g., all could be outside MAX_DISTANCE)
    boolean sourceUsed = false;

    if (D) System.out.println(C+": starting hazard curve calculation");

    // loop over sources
    for(i=0;i < numSources ;i++) {

      if (D) System.out.println(C+": getting source #"+i);
      // get the ith source
      ProbEqkSource source = eqkRupForecast.getSource(i);

      // compute it's distance from the site and skip if it's too far away
      distance = source.getMinDistance(site);
      if(distance > MAX_DISTANCE) {
        //update progress bar for skipped ruptures
        if(this.showProgressBar) {
          currRuptures += source.getNumRuptures();
          progressClass.updateProgress(currRuptures, totRuptures);
        }
        continue;
      }

      // indicate that a source has been used
      sourceUsed = true;

      // get the number of ruptures for the current source
      int numRuptures = source.getNumRuptures();

      // loop over these ruptures
      for(int n=0; n < numRuptures ; n++,++currRuptures) {

        // update the progress bar is necessary
        if(showProgressBar)
          progressClass.updateProgress(currRuptures, totRuptures);

        // get the rupture probability
        qkProb = ((ProbEqkRupture)source.getRupture(n)).getProbability();

        // set the PQkRup in the IMR
        try {
          imr.setProbEqkRupture((ProbEqkRupture)source.getRupture(n));
        } catch (Exception ex) {
          System.out.println("Parameter change warning caught");
        }

        // get the conditional probability of exceedance from the IMR
        test=(ArbitrarilyDiscretizedFunc)imr.getExceedProbabilities(test);

        // calculate the hazard function
        numPoints = test.getNum();
        for(k=0;k<numPoints;k++)
          hazFunction.set(k,hazFunction.getY(k)*Math.pow(1-qkProb,test.getY(k)));
      }
    }

    numPoints = hazFunction.getNum();

    // finalize the hazard function
    if(sourceUsed)
      for(i=0;i<numPoints;++i)
        hazFunction.set(i,1-hazFunction.getY(i));
    else
      for(i=0;i<numPoints;++i)
        hazFunction.set(i,0.0);

    if(showProgressBar)
      progressClass.dispose();

  }


  /**
   * Initialize the prob as 1 for the Hazard function
   *
   * @param arb
   */
  private void initDiscretizeValues(DiscretizedFuncAPI arb){
    for(int i=0;i<arb.getNum();++i)
      arb.set(i,1);
  }



}



