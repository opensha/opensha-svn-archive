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
 * @author Nitin Gupta & Vipin Gupta
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
  protected double MAX_DISTANCE = 250;

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
   * this function determines the hazard curve based on the parameters
   *
   * @param func : it has X values set and result will be returned in this function
   * @param site  : site parameter
   * @param imr  :selected IMR object
   * @param eqkRupForecast  : selected Earthquake rup forecast
   * @return
   */
  public void getHazardCurve(DiscretizedFuncAPI hazFunction,
                             Site site, AttenuationRelationshipAPI imr, EqkRupForecastAPI eqkRupForecast) {


    progressClass = new CalcProgressBar("HazardCurve Calc status", "Updating Forecast");
    // initiliaze the progress bar frame in which to show progress bar
    progressClass.initProgressFrame();

    // update the forecast. any constraint exception is caught by the GuiBean
    eqkRupForecast.updateForecast();

    // now show the progress bar
    progressClass.displayProgressBar();


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

    // totRuptures holds the total ruptures for all sources
    int totRuptures = 0;
    if (D) System.out.println(C+":  starting totNumRup compuation");
    for(int i=0;i<numSources;++i)
      totRuptures+=eqkRupForecast.getSource(i).getNumRuptures();

    // rupture number presently being processed
    int currRuptures = 0;

    updateProgress(currRuptures, totRuptures);

    // this makes sure a source is actually used
    boolean sourceUsed = false;

    if (D) System.out.println(C+": starting hazard curve calculation");
    for(int i=0;i < numSources ;i++) {

      if (D) System.out.println(C+": getting source #"+i);
      // get source and get its distance from the site
      ProbEqkSource source = eqkRupForecast.getSource(i);
      double distance = source.getMinDistance(site);

      // if source is greater than the MAX_DISTANCE, ignore the source
      if(distance > MAX_DISTANCE)  continue;

      // to indicate that a source has been used
      sourceUsed = true;

      // for each source, get the number of ruptures
      int numRuptures = source.getNumRuptures();
      for(int n=0; n < numRuptures ; n++,++currRuptures) {

        //check the progress
        updateProgress(currRuptures, totRuptures);

        // for each rupture, set in IMR and do computation
        double qkProb = ((ProbEqkRupture)source.getRupture(n)).getProbability();

        // initialize the values in condProbfunc with log values as passed in hazFunction
        initLogDiscretizeValues(hazFunction, condProbFunc);
        try {
          imr.setProbEqkRupture((ProbEqkRupture)source.getRupture(n));
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

    int  numPoints = hazFunction.getNum();

    // finalize the hazard function
    if(sourceUsed)
      for(int i=0;i<numPoints;++i)
        hazFunction.set(i,1-hazFunction.getY(i));
    else
      for(int i=0;i<numPoints;++i)
        hazFunction.set(i,0.0);

    //remove the progress frame
    progressClass.dispose();

  }




  /**
   * update the calculation progress
   * @param num:    the current number
   * @param totNum: the total number
   */
  private void updateProgress(int num, int totNum) {

    int val=0;
    boolean update = false;

    // find if we're at a point to update
    if(num == (int) (totNum*0.9)) { // 90% complete
      val = 90;
      update = true;
    } else if(num == (int) (totNum*0.8)) { // 80% complete
      val = 80;
      update = true;
    } else if(num == (int) (totNum*0.7)) { // 70% complete
      val = 70;
     update = true;
    } else if(num == (int) (totNum*0.6)) { // 60% complete
      val = 60;
      update = true;
    } else if(num == (int) (totNum*0.5)) { // 50% complete
      val = 50;
      update = true;
    } else if(num == (int) (totNum*0.4)) { // 40% complete
      val = 40;
      update = true;
    } else if(num == (int) (totNum*0.3)) { // 30% complete
      val = 30;
      update = true;
    } else if(num == (int) (totNum*0.2)) { // 20% complete
      val = 20;
      update = true;
    } else if(num == (int) (totNum*0.1)) { // 10% complete
      val = 10;
      update = true;
    }

    // update the progress bar
    if(update == true)
      progressClass.updateProgressBar(val,Integer.toString((int) (totNum*val/100)) + "  of  " + Integer.toString(totNum) + "  Eqk Ruptures");
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



