package org.scec.sha.calc;


import javax.swing.JFrame;
import javax.swing.JProgressBar;
import java.awt.Rectangle;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.scec.data.function.*;
import org.scec.data.Site;
import org.scec.sha.imr.*;
import org.scec.sha.earthquake.*;
import org.scec.util.FileUtils;
import org.scec.sha.earthquake.ERF_API;
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

public class HazardCurveCalculator extends UnicastRemoteObject
    implements HazardCurveCalculatorAPI{

  protected final static String C = "HazardCurveCalculator";
  protected final static boolean D = false;


  /* maximum permitted distance between fault and site to consider source in
  hazard analysis for that site; this default value is to allow all PEER test
  cases to pass through
  */
  public final static double MAX_DISTANCE_DEFAULT = 1e6;
  protected double MAX_DISTANCE = MAX_DISTANCE_DEFAULT;

  private int currRuptures = -1;
  private int totRuptures=0;
  private int numForecasts=0;


  /**
   * creates the HazardCurveCalculator object
   *
   * @throws java.rmi.RemoteException
   * @throws IOException
   */
  public HazardCurveCalculator()
      throws java.rmi.RemoteException {}


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

  public void setNumForecasts(int num) throws java.rmi.RemoteException{
    this.numForecasts = num;
  }



  /**
   * This function computes a hazard curve for the given Site, IMR, and ERF.  The curve
   * in place in the passed in hazFunction (with the X-axis values being the IMLs for which
   * exceedance probabilites are desired).
   * @param hazFunction: This function is where the hazard curve is placed
   * @param site: site object
   * @param imr: selected IMR object
   * @param eqkRupForecast: selected Earthquake rup forecast
   * @return
   */
  public void getHazardCurve(DiscretizedFuncAPI hazFunction,
                             Site site, AttenuationRelationshipAPI imr, ERF_API eqkRupForecast)
  throws java.rmi.RemoteException{

    this.currRuptures = -1;

    /* this determines how the calucations are done (doing it the way it's outlined
    in the paper SRL gives probs greater than 1 if the total rate of events for the
    source exceeds 1.0, even if the rates of individual ruptures are << 1).
    */
    boolean poissonSource = false;

    ArbitrarilyDiscretizedFunc condProbFunc = (ArbitrarilyDiscretizedFunc) hazFunction.deepClone();
    ArbitrarilyDiscretizedFunc sourceHazFunc = (ArbitrarilyDiscretizedFunc) hazFunction.deepClone();

    //System.out.println("hazFunction: "+hazFunction.toString());

    // declare some varibles used in the calculation
    double qkProb, distance;
    int k,i;

    // get the number of points
    int numPoints = hazFunction.getNum();

    // set the maximum distance in the attenuation relationship
    // (Note- other types of IMRs may not have this method so we should really check type here)
    imr.setUserMaxDistance(MAX_DISTANCE);

    // get total number of sources
    int numSources = eqkRupForecast.getNumSources();
    //System.out.println("Number of Sources: "+numSources);
    //System.out.println("ERF info: "+ eqkRupForecast.getClass().getName());
    // compute the total number of ruptures for updating the progress bar
    totRuptures = 0;
    for(i=0;i<numSources;++i)
      totRuptures+=eqkRupForecast.getSource(i).getNumRuptures();

    //System.out.println("Total number of ruptures:"+ totRuptures);


    // init the current rupture number (also for progress bar)
    currRuptures = 0;

    // initialize the hazard function to 1.0
    initDiscretizeValues(hazFunction, 1.0);

    // set the Site in IMR
    imr.setSite(site);

    // this boolean will tell us whether a source was actually used
    // (e.g., all could be outside MAX_DISTANCE)
    boolean sourceUsed = false;

    if (D) System.out.println(C+": starting hazard curve calculation");

    // loop over sources
    for(i=0;i < numSources ;i++) {

      // get the ith source
      ProbEqkSource source = eqkRupForecast.getSource(i);

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

      // initialize the source hazard function to 0.0 if it's a non-poisson source
      if(!poissonSource)
        initDiscretizeValues(sourceHazFunc, 0.0);

      // get the number of ruptures for the current source
      int numRuptures = source.getNumRuptures();

      // loop over these ruptures
      for(int n=0; n < numRuptures ; n++,++currRuptures) {

        // get the rupture probability
        qkProb = ((ProbEqkRupture)source.getRupture(n)).getProbability();

        // set the PQkRup in the IMR
        imr.setEqkRupture(source.getRupture(n));

        // get the conditional probability of exceedance from the IMR
        condProbFunc=(ArbitrarilyDiscretizedFunc)imr.getExceedProbabilities(condProbFunc);


        // For poisson source
        if(poissonSource)
          for(k=0;k<numPoints;k++)
            hazFunction.set(k,hazFunction.getY(k)*Math.pow(1-qkProb,condProbFunc.getY(k)));
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

    // finalize the hazard function
    if(sourceUsed)
      for(i=0;i<numPoints;++i)
        hazFunction.set(i,1-hazFunction.getY(i));
    else
      for(i=0;i<numPoints;++i)
        hazFunction.set(i,0.0);
    --numForecasts;
    if (D) System.out.println(C+"hazFunction.toString"+hazFunction.toString());
// double tempVal = -1.0*Math.log(1.0-hazFunction.getY(1));
// System.out.println(tempVal);
  }


  public int getCurrRuptures() throws java.rmi.RemoteException{
    return this.currRuptures;
  }

  public int getTotRuptures() throws java.rmi.RemoteException{
    return this.totRuptures;
  }

  public boolean done() throws java.rmi.RemoteException{
    return (currRuptures==totRuptures && (numForecasts==0));
  }


  /**
   * Initialize the prob as 1 for the Hazard function
   *
   * @param arb
   */
  private void initDiscretizeValues(DiscretizedFuncAPI arb, double val){
    int num = arb.getNum();
    for(int i=0;i<num;++i)
      arb.set(i,val);
  }

}



