package org.scec.sha.calc;


import javax.swing.JFrame;
import javax.swing.JProgressBar;
import java.awt.Rectangle;
import javax.swing.JOptionPane;
import javax.swing.JLabel;

import org.scec.data.function.*;
import org.scec.data.Site;
import org.scec.sha.imr.*;
import org.scec.sha.earthquake.*;
import org.scec.sha.propagation.DistanceRupParameter;
import org.scec.sha.gui.infoTools.*;
/**
 * <p>Title: DisaggregationCalculator </p>
 * <p>Description: This class disaggregates a hazard curve based on the
 * input parameters imr, site and eqkRupforecast.  See Bazzurro and Cornell
 * (1999, Bull. Seism. Soc. Am., 89, pp. 501-520) for a complete discussion
 * of disaggregation.  The Dbar computed here is for rupture distance.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta & Vipin Gupta
 * @date Oct 28, 2002
 * @version 1.0
 */

public class DisaggregationCalculator {

  protected final static String C = "DisaggregationCalculator";
  protected final static boolean D = false;


  // maximum permitted distance between fault and site to consider source in hazard analysis for that site
  protected double MAX_DISTANCE = 250;

  // disaggregation stuff - MIN and MAX are centers of first and last bins
  private double MIN_MAG = 5.0;
  private double MAX_MAG = 9.0;
  private int NUM_MAG = 41;
  private double deltaMag = (MAX_MAG-MIN_MAG)/(NUM_MAG-1);

  private double MIN_DIST = 5;
  private double MAX_DIST = 295;
  private int NUM_DIST = 30;
  private double deltaDist = (MAX_DIST-MIN_DIST)/(NUM_DIST-1);

  private double MIN_E = -5.0;
  private double MAX_E = 5;
  private int NUM_E = 53;
  private double deltaE = (MAX_E-MIN_E)/(NUM_E-3);
  // Note: the last two bins here are for -infinity & infinity (if stdDev = 0)

  private double[][][] pmf = new double[NUM_MAG][NUM_DIST][NUM_E];

  private int iMag, iDist, iEpsilon;
  private double mag, dist, epsilon;
  private boolean withinBounds;

  private double Mbar, Dbar, Ebar;
  private double M_mode1D, D_mode1D, E_mode1D;
  private double M_mode3D, D_mode3D, E_mode3D;

  private double iml, prob, totalRate;

  // boolean for telling whether to show a progress bar
  boolean showProgressBar = true;

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
   * this function performs the disaggregation
   *
   * @param iml: the intensity measure level to disaggregate
   * @param site: site parameter
   * @param imr: selected IMR object
   * @param eqkRupForecast: selected Earthquake rup forecast
   * @return
   */
  public void disaggregate(double iml, Site site,
        AttenuationRelationshipAPI imr, EqkRupForecast eqkRupForecast) {

    double rate, mean, stdDev, condProb;
    int totRuptures=0, currRuptures=0;

    DistanceRupParameter distRup = new DistanceRupParameter();

    String S = C + ": disaggregate(): ";

    if( D ) System.out.println(S + "STARTING DISAGGREGATION");

    this.iml = iml;
    if( D ) System.out.println(S + "iml = " + iml);

    if( D )System.out.println(S + "deltaMag = " + deltaMag + "; deltaDist = " + deltaDist + "; deltaE = " + deltaE);



    // get total number of sources
    int numSources = eqkRupForecast.getNumSources();

    // check if progress bar is desired and set it up if so
    if(this.showProgressBar) {
      progressClass = new CalcProgressBar("Disaggregation Status"," Starting Disaggregation ...");
      progressClass.displayProgressBar();

      // compute the total number of ruptures for updating the progress bar
      totRuptures = 0;
      for(int i=0;i<numSources;++i)
        totRuptures+=eqkRupForecast.getSource(i).getNumRuptures();

      // init the current rupture number (also for progress bar)
      currRuptures = 0;

      // initialize the progress bar to zero ruptures
      progressClass.updateProgress(currRuptures, totRuptures);
    }

    try {
      // set the site in IMR
      imr.setSite(site);
     }catch (Exception ex) {
      if(D) System.out.println(C + ":Param warning caught"+ex);
      ex.printStackTrace();
    }

    for(int i=0;i<numSources;++i)
        totRuptures+=eqkRupForecast.getSource(i).getNumRuptures();

    // initialize
    Ebar = 0;
    Mbar = 0;
    Dbar = 0;
    totalRate = 0;

    for(int i=0; i<NUM_MAG; i++)
      for(int j=0; j<NUM_DIST; j++)
        for(int k=0; k<NUM_E; k++)
          pmf[i][j][k]=0;

    for(int i=0;i < numSources ;i++) {

      // get source and get its distance from the site
      ProbEqkSource source = eqkRupForecast.getSource(i);
      double distance = source.getMinDistance(site);

      // if source is greater than the MAX_DISTANCE, ignore the source
      if(distance > MAX_DISTANCE)
        continue;

      // for each source, get the number of ruptures
      int numRuptures = eqkRupForecast.getNumRuptures(i);

      // loop over ruptures
      for(int n=0; n < numRuptures ; n++,++currRuptures) {

        // update the progress bar is necessary
        if(showProgressBar)  progressClass.updateProgress(currRuptures, totRuptures);

          // get the rupture
          ProbEqkRupture rupture = source.getRupture(n);

          double qkProb = rupture.getProbability();

          // set the rupture in the imr
          try {
            imr.setProbEqkRupture(rupture);
          } catch (Exception ex) {
            System.out.println("Parameter change warning caught");
          }

          // get the cond prob, mean, stdDev, and epsilon
          condProb = imr.getExceedProbability(iml);

          // should the following throw and exception?
          if(condProb == 0 && D)
              System.out.println(S + "Exceedance probability is zero! (thus the NaNs below)");


          mean = imr.getMean();
          stdDev = imr.getStdDev();
          epsilon = (iml-mean)/stdDev;

          distRup.setValue(rupture,site);
          dist = ((Double) distRup.getValue()).doubleValue();

          mag = rupture.getMag();

          // get the equiv. Poisson rate over the time interval
          rate = condProb * Math.log(1-qkProb);

          /*
          if( D ) System.out.println("disaggregation():" + " rupture #" + currRuptures +
                                         " qkProb=" + qkProb +
                                         " condProb=" + condProb +
                                         " mean=" + mean +
                                         " stdDev=" + stdDev +
                                         " epsilon=" + epsilon +
                                         " dist=" + dist +
                                         " rate=" + rate);
*/
          // set the 3D array indices & check that all are in bounds
          setIndices();

          if (withinBounds)
              pmf[iMag][iDist][iEpsilon] += rate;
          else
              if( D ) System.out.println("disaggregation(): Some bin is out of range");

//          if( D ) System.out.println("disaggregation(): bins: " + iMag + "; " + iDist + "; " + iEpsilon);

          totalRate += rate;

          Mbar += rate * mag;
          Dbar += rate * dist;
          Ebar += rate * epsilon;

      }
    }

    Mbar /= totalRate;
    Dbar /= totalRate;
    Ebar /= totalRate;
    if( D ) System.out.println(S + "Mbar = " + Mbar);
    if( D ) System.out.println(S + "Dbar = " + Dbar);
    if( D ) System.out.println(S + "Ebar = " + Ebar);


    double maxRate = -1;
    int modeMagBin=-1, modeDistBin=-1, modeEpsilonBin=-1;
    for(int i=0; i<NUM_MAG; i++) {
      for(int j=0; j<NUM_DIST; j++) {
        for(int k=0; k<NUM_E; k++) {
          pmf[i][j][k] /= totalRate;
          if(pmf[i][j][k] > maxRate) {
              maxRate = pmf[i][j][k];
              modeMagBin = i;
              modeDistBin = j;
              modeEpsilonBin = k;
          }
        }
      }
    }

    M_mode3D = mag(modeMagBin);
    D_mode3D = dist(modeDistBin);
    E_mode3D = eps(modeEpsilonBin);

    if( D ) System.out.println(S + "MagMode = "  + M_mode3D + "; binNum = " + modeMagBin);
    if( D ) System.out.println(S + "DistMode = " + D_mode3D + "; binNum = " + modeDistBin);
    if( D ) System.out.println(S + "EpsMode = "  + E_mode3D + "; binNum = " + modeEpsilonBin);

   if(showProgressBar)  progressClass.dispose();

  }

  public String getResultsString() {

    String results;

    float mm_l = (float) (M_mode3D-deltaMag/2.0);
    float mm_u = (float) (M_mode3D+deltaMag/2.0);
    float dm_l = (float) (D_mode3D-deltaDist/2.0);
    float dm_u = (float) (D_mode3D+deltaDist/2.0);
    float em_l = (float) (E_mode3D-deltaE/2.0);
    float em_u = (float) (E_mode3D+deltaE/2.0);

    results = "Disaggregation Results:\n" +
              "\n  Mbar = " + (float) Mbar +
              "\n  Dbar = " + (float) Dbar +
              "\n  Ebar = " + (float) Ebar + "\n" +
              "\n  " + mm_l+" ² Mmode < " + mm_u +
              "\n  " + dm_l+" ² Dmode < " + dm_u;
    if( E_mode3D == Double.NEGATIVE_INFINITY || E_mode3D == Double.POSITIVE_INFINITY)
      results += "\n  Emode = " + E_mode3D;
    else
      results += "\n  " + em_l+" ² Emode < " + em_u;

    if(totalRate == 0.0)
      results += "\n\nNote:\n" +
                 "The above NaN values result from the chosen IML\n" +
                 "(or that interpolated from the chosen probability)\n" +
                 "never being exceeded.";

/*
        results = "Disaggregation Result:\n\n\tMbar = " + Mbar + "\n\tDbar = " +
              Dbar + "\n\tEbar = " + Ebar + "\n\n\tMmode = " + M_mode3D +
              "\n\tDmode = " + D_mode3D + "\n\tEmode = " + E_mode3D;
*/

    return results;

  }

  private void setIndices() {
      withinBounds = true;
      iMag     =  Math.round( (float) ((mag-MIN_MAG)/deltaMag) );
      iDist    =  Math.round((float) ((dist-MIN_DIST)/deltaDist));
      if(epsilon == Double.POSITIVE_INFINITY)
          iEpsilon = NUM_E-1;
      else if (epsilon == Double.NEGATIVE_INFINITY)
          iEpsilon = NUM_E-2;
      else {
          iEpsilon = Math.round((float) ((epsilon-MIN_E)/deltaE));
          // check to make sure it didn't fall onto the last two bins here
          if(iEpsilon == NUM_E-1 || iEpsilon == NUM_E-2)
              iEpsilon = NUM_E + 1;  // make it fall outside
      }

      if( iMag < 0 || iMag >= NUM_MAG ) withinBounds = false;
      if( iDist < 0 || iDist >= NUM_DIST ) withinBounds = false;
      if( iEpsilon < 0 || iEpsilon >= NUM_E ) withinBounds = false;
  }

  private double mag(int iMag) {
      if( iMag >=0 && iMag <= NUM_MAG)
          return  MIN_MAG + iMag*deltaMag;
      else return Double.NaN;
  }

  private double dist(int iDist) {
      if( iDist >=0 && iDist <= NUM_DIST)
          return  MIN_DIST + iDist*deltaDist;
      else return Double.NaN;
      }

  private double eps(int iEpsilon) {
      if(iEpsilon >= 0 && iEpsilon < NUM_E) {
          if(iEpsilon == NUM_E-1) return Double.POSITIVE_INFINITY;
          else if(iEpsilon == NUM_E-2) return Double.NEGATIVE_INFINITY;
          else return  MIN_E + iEpsilon*deltaE;
      }
      else return Double.NaN;
  }


}