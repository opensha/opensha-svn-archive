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
  protected final static boolean D = true;


  // maximum permitted distance between fault and site to consider source in hazard analysis for that site
  protected final double MAX_DISTANCE = 200;

  // progress bar stuff:
  private int FRAME_WIDTH = 250;
  private int FRAME_HEIGHT = 50;
  private int FRAME_STARTX = 400;
  private int FRAME_STARTY = 200;
  private JProgressBar progress;

  // disaggregation stuff
  private double MIN_MAG = 5.0;
  private double MAX_MAG = 9.0;
  private int NUM_MAG = 41;
  private double deltaMag = (MAX_MAG-MIN_MAG)/(NUM_MAG-1);

  private double MIN_DIST = 5;
  private double MAX_DIST = 195;
  private int NUM_DIST = 20;
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

  private double iml, prob;


  /**
   * this function performs the disaggregation
   *
   * @param HazCurve: the curve to disaggregate (already computed)
   * @param iml: the intensity measure level to disaggregate
   * @param site: site parameter
   * @param imr: selected IMR object
   * @param eqkRupForecast: selected Earthquake rup forecast
   * @return
   */
  public void disaggregate(double iml, Site site,
        AttenuationRelationshipAPI imr, EqkRupForecast eqkRupForecast) {

    double totalRate, rate, mean, stdDev, condProb;

    DistanceRupParameter distRup = new DistanceRupParameter();

    String S = C + ": disaggregate(): ";

    if( D ) System.out.println(S + "STARTING DISAGGREGATION");

    this.iml = iml;
    if( D ) System.out.println(S + "iml = " + iml);

    if( D )System.out.println(S + "deltaMag = " + deltaMag + "; deltaDist = " + deltaDist + "; deltaE = " + deltaE);

    // make the progress bar
    JFrame frame = new JFrame("Disaggregation Status");
    frame.setLocation(this.FRAME_STARTX, this.FRAME_STARTY);
    frame.setSize(this.FRAME_WIDTH, this.FRAME_HEIGHT);

    progress = new JProgressBar(0,100);
    progress.setStringPainted(true); // display the percentage completed also
    JLabel label = new JLabel(" Starting Disaggregation ...");
    frame.getContentPane().add(label);
    frame.show();
    frame.validate();
    frame.repaint();

    // add the progress bar
    frame.getContentPane().remove(label);
    frame.getContentPane().add(progress);
    frame.getContentPane().validate();
    frame.getContentPane().repaint();


    try {
      // set the site in IMR
      imr.setSite(site);
     }catch (Exception ex) {
      if(D) System.out.println(C + ":Param warning caught"+ex);
      ex.printStackTrace();
    }

    // get number of sources
    int numSources = eqkRupForecast.getNumSources();

    // totRuptures holds the total number of ruptures for all sources
    int totRuptures = 0;
    for(int i=0;i<numSources;++i)
        totRuptures+=eqkRupForecast.getSource(i).getNumRuptures();

    // rupture number presently being processed
    int currRuptures = 0;

    // initialize
    Ebar = 0;
    Mbar = 0;
    Dbar = 0;
    totalRate = 0;

    for(int i=0; i<NUM_MAG; i++)
      for(int j=0; j<NUM_DIST; j++)
        for(int k=0; k<NUM_E; k++)
          pmf[i][j][k]=0;

    updateProgress(currRuptures, totRuptures);

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

          //check the progress
          updateProgress(currRuptures, totRuptures);

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


    //remove the frame
    frame.dispose();

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
              iEpsilon = NUM_E + 1;
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

  /**
   * update the calculation progress
   * @param num:    the current number
   * @param totNum: the total number
   */
   private void updateProgress(int num, int totNum) {

        int val=0;
        boolean update = false;

        // update the progress bar
        if(num == (int) (totNum*0.9)) { // 90% complete
              val = 90;
              update = true;
        }
        else if(num == (int) (totNum*0.8)) { // 80% complete
              val = 80;
              update = true;        }
        else if(num == (int) (totNum*0.7)) { // 70% complete
              val = 70;
              update = true;        }
        else if(num == (int) (totNum*0.6)) { // 60% complete
              val = 60;
              update = true;        }
        else if(num == (int) (totNum*0.5)) { // 50% complete
              val = 50;
              update = true;        }
        else if(num == (int) (totNum*0.4)) { // 40% complete
              val = 40;
              update = true;        }
        else if(num == (int) (totNum*0.3)) { // 30% complete
              val = 30;
              update = true;        }
        else if(num == (int) (totNum*0.2)) { // 20% complete
              val = 20;
              update = true;        }
        else if(num == (int) (totNum*0.1)) { // 10% complete
              val = 10;
              update = true;        }

        if(update == true) {
            progress.setString(Integer.toString((int) (totNum*val/100)) + "  of  " + Integer.toString(totNum) + "  Eqk Ruptures");
            progress.setValue(val);
            Rectangle rect = progress.getBounds();
            progress.paintImmediately(rect);
        }
   }

}