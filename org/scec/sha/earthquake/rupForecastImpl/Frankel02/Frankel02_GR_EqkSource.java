package org.scec.sha.earthquake.rupForecastImpl.Frankel02;

import java.util.ArrayList;
import java.util.Iterator;

import org.scec.sha.surface.EvenlyGriddedSurface;
import org.scec.sha.magdist.IncrementalMagFreqDist;
import org.scec.data.*;
import org.scec.calc.RelativeLocation;
import org.scec.sha.earthquake.*;


import org.scec.sha.fault.*;
import org.scec.sha.magdist.GutenbergRichterMagFreqDist;
;



/**
 * <p>Title: Frankel02_GR_EqkSource </p>
 * <p>Description: This implements Frankel's floating-rupture Gutenberg Richter
 * source used in the 2002 version of his code.  We made this, rather than using
 * the more general FloatingPoissonFaultSource only for enhances performance (e.g.,
 * no need to float down dip or to support Area(Mag) uncertainties.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta & Vipin Gupta
 * @date Sep 2, 2002
 * @version 1.0
 */

public class Frankel02_GR_EqkSource extends ProbEqkSource {


  //for Debug purposes
  private static String  C = "Frankel02_GR_EqkSource";
  private boolean D = false;

  private double rake;
  private double duration;
  //these are the static static defined varibles to be used to find the number of ruptures.
  private final static double RUPTURE_WIDTH =100.0;
  private double rupOffset;
  private int totNumRups;
  private EvenlyGriddedSurface surface;
  private ArrayList mags, rates;

  /**
   * constructor specifying the values needed for the source
   *
   * @param magFreqDist - any IncrementalMagFreqDist
   * @param surface - any EvenlyGriddedSurface
   * @param rupOffset - floating rupture offset (km)
   * @param rake - rake for all ruptures
   * @param duration - forecast duration (yrs)
   * @param sourceName - source name
   */
  public Frankel02_GR_EqkSource(IncrementalMagFreqDist magFreqDist,
                                EvenlyGriddedSurface surface,
                                double rupOffset,
                                double rake,
                                double duration,
                                String sourceName) {

    this.surface=surface;
    this.rupOffset = rupOffset;
    this.rake=rake;
    this.duration = duration;
    this.name = sourceName;

    probEqkRupture = new ProbEqkRupture();
    probEqkRupture.setAveRake(rake);

    // get a list of mags and rates for non-zero rates
    mags = new ArrayList();
    rates = new ArrayList();
    for (int i=0; i<magFreqDist.getNum(); ++i){
      if(magFreqDist.getY(i) > 0){
        //magsAndRates.set(magFreqDist.getX(i),magFreqDist.getY(i));
        mags.add(new Double(magFreqDist.getX(i)));
        rates.add(new Double(magFreqDist.getY(i)));
      }
    }

    // Determine number of ruptures
    int numMags = mags.size();
    totNumRups=0;
    for(int i=0;i<numMags;++i){
      double rupLen = Math.pow(10.0,-3.22+0.69*((Double)mags.get(i)).doubleValue());
      totNumRups += getNumRuptures(rupLen);
    }
  }

  /**
   * this functions sums up all the ruptures for all magnitudes
   * @return the total num of rutures for all magnitudes
   */
  public int getNumRuptures() { return totNumRups; }


  /**
   * This method sets the probability of the different rupture surface for different mag
   * @param nthRupture : it is to find the mag and rate to which that rupture number correspond
   * @return the object of the ProbEqkRupture class after setting the probability
   */
  public ProbEqkRupture getRupture(int nthRupture){
    int numMags = mags.size();
    double mag=0, rupLen=0;
    int numRups=0, tempNumRups=0, iMag=-1;

    if(nthRupture < 0 || nthRupture>=getNumRuptures())
       throw new RuntimeException("Invalid rupture index. This index does not exist");

    // this finds the magnitude:
    for(int i=0;i<numMags;++i){
      mag = ((Double)mags.get(i)).doubleValue();
      iMag = i;
      rupLen = Math.pow(10.0,-3.22+0.69*mag);
      if(D) System.out.println("mag="+mag+"; rupLen="+rupLen);
      numRups = getNumRuptures(rupLen);
      tempNumRups += numRups;
      if(nthRupture < tempNumRups)
        break;
    }

    probEqkRupture.setMag(mag);
    // set probability
    double rate = ((Double)rates.get(iMag)).doubleValue();
    double prob = 1- Math.exp(-duration*rate/numRups);
    probEqkRupture.setProbability(prob);

    // set rupture surface
    probEqkRupture.setRuptureSurface( surface.getNthSubsetSurface(rupLen,
                                      RUPTURE_WIDTH,rupOffset,
                                      nthRupture+numRups-tempNumRups));

    return probEqkRupture;
  }


  /** Set the time span in years
   *
   * @param yrs : timeSpan as specified in  Number of years
   */
  public void setDuration(double yrs) {
   //set the time span in yrs
    duration = yrs;
  }



  /**
   * @param mag
   * @return the total number of ruptures associated with the given mag
   */
  private int getNumRuptures(double rupLen){
    return surface.getNumSubsetSurfaces(rupLen,RUPTURE_WIDTH,rupOffset);
 }


   /**
   * This returns the shortest dist to either end of the fault trace, or to the
   * mid point of the fault trace.
   * @param site
   * @return minimum distance
   */
   public  double getMinDistance(Site site) {

      double min;

      // get first location on fault trace
      Direction dir = RelativeLocation.getDirection(site.getLocation(), (Location) surface.get(0,0));
      min = dir.getHorzDistance();

      // get last location on fault trace
      dir = RelativeLocation.getDirection(site.getLocation(),(Location) surface.get(0,surface.getNumCols()-1));
      if (min > dir.getHorzDistance())
          min = dir.getHorzDistance();

      // get mid location on fault trace
      dir = RelativeLocation.getDirection(site.getLocation(),(Location) surface.get(0,(int) surface.getNumCols()/2));
      if (min > dir.getHorzDistance())
          min = dir.getHorzDistance();

      return min;
    }

 /**
  * get the name of this class
  *
  * @return
  */
 public String getName() {
   return name;
  }


  /**
   * this is to test the code
   * @param args
   */
  public static void main(String[] args) {
    FaultTrace fltTr = new FaultTrace("name");
    fltTr.addLocation(new Location(33.0,-122,0));
    fltTr.addLocation(new Location(34.0,-122,0));
    FrankelGriddedFaultFactory factory = new FrankelGriddedFaultFactory(fltTr,90,0,10,1);

    GutenbergRichterMagFreqDist gr = new GutenbergRichterMagFreqDist(6.5,3,0.5,6.5,7.5,1.0e14,1.0);
    System.out.println("cumRate="+(float)gr.getTotCumRate());

    Frankel02_GR_EqkSource src = new Frankel02_GR_EqkSource(gr,(EvenlyGriddedSurface)factory.getGriddedSurface(),
                                                            10.0,0.0,1,"name");
    ProbEqkRupture rup;
    for(int i=0; i< src.getNumRuptures();i++) {
      rup = src.getRupture(i);
      System.out.print("rup #"+i+":\n\tmag="+rup.getMag()+"\n\tprob="+
                          rup.getProbability()+"\n\tRup Ends: "+
                          (float)rup.getRuptureSurface().getLocation(0,0).getLatitude()+"  "+
                          (float)rup.getRuptureSurface().getLocation(0,rup.getRuptureSurface().getNumCols()-1).getLatitude()+
                          "\n\n");
    }

  }
}