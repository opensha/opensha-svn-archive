package org.scec.sha.earthquake.rupForecastImpl;

import org.scec.sha.magdist.*;
import org.scec.data.*;
import org.scec.data.function.*;
import org.scec.calc.RelativeLocation;
import org.scec.sha.earthquake.*;
import org.scec.sha.fault.FaultTrace;
import org.scec.sha.fault.FrankelGriddedFaultFactory;
import org.scec.calc.magScalingRelations.MagLengthRelationship;
import org.scec.sha.surface.PointSurface;
import org.scec.sha.surface.GriddedSurfaceAPI;

// temp for testing
import org.scec.sha.magdist.GutenbergRichterMagFreqDist;
import org.scec.calc.magScalingRelations.magScalingRelImpl.WC1994_MagLengthRelationship;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * <p>Title: Point2Vert_SS_FaultPoisSource </p>
 * <p>Description: For a given Location, IncrementalMagFreqDist (of Poissonian
 * rates), MagLengthRelationship, and duration, this creates a vertically dipping,
 * strike-slip ProbEqkRupture for each magnitude (that has a non-zero rate).  Each finite
 * rupture is centered on the given Location.  A user-defined strike will be used if given,
 * otherwise an random stike will be computed and applied.  One can also specify a
 * magCutOff (magnitudes less than or equal to this will be treated as point sources)
 * and a minMag (magnitudes below this will be ignored).  This assumes that the duration
 * units are the same as those for the rates in the IncrementalMagFreqDist.</p>
 * This class is loosely modeled after Frankels fortran program "hazgridXv3.f".  However,
 * their use of a random strike means we will never be able to exactly reproduce their
 * results.  Also, they choose a different random strike for each magnitude (and even
 * each site location), whereas we apply one random strike to the entire source (all mags
 * in the given magFreqDist).
 * @author Edward Field
 * @date March 24, 2004
 * @version 1.0
 */

public class Point2Vert_SS_FaultPoisSource extends ProbEqkSource implements java.io.Serializable{


  //for Debug purposes
  private static String  C = new String("PointPoissonEqkSource");
  private boolean D = false;

  private IncrementalMagFreqDist magFreqDist;
  private double aveDip=90;
  private double aveRake=0.0;
  private double duration;
  private MagLengthRelationship magLengthRelationship;
  private double magCutOff;
  private double minMag = 0.0;

  // to hold the non-zero mags, rates, and rupture surfaces
  ArrayList mags, rates, rupSurfaces;

 /**
   * The Full Constructor
   * @param loc - the Location of the point source
   * @param magFreqDist - the mag-freq-dist for the point source
   * @param magLengthRelationship - A relationship for computing length from magnitude
   * @param strike - the strike of the finite SS fault
   * @param duration - the forecast duration
   * @param magCutOff - below (and eqaul) to this value a PointSurface will be applied
   * @param minMag - magnitudes below this will be completely ignored
   */
  public Point2Vert_SS_FaultPoisSource(Location loc, IncrementalMagFreqDist magFreqDist,
                                       MagLengthRelationship magLengthRelationship,
                                       double strike, double duration, double magCutOff,
                                       double minMag){
    this.magCutOff = magCutOff;
    this.minMag=minMag;

    // make the prob qk rupture
    probEqkRupture = new ProbEqkRupture();
    probEqkRupture.setAveRake(aveRake);

    if(D) {
      System.out.println("magCutOff="+magCutOff);
      System.out.println("minMag="+minMag);
      System.out.println("num pts in magFreqDist="+magFreqDist.getNum());
    }

    // set the mags, rates, and rupture surfaces
    setAll(loc,magFreqDist,magLengthRelationship,strike,duration);
  }


  /**
    * The Constructor for the case where a random strike is computed (rather than assigned)
    * @param loc - the Location of the point source
    * @param magFreqDist - the mag-freq-dist for the point source
    * @param magLengthRelationship - A relationship for computing length from magnitude
    * @param duration - the forecast duration
    * @param magCutOff - below (and eqaul) to this value a PointSurface will be applied
    * @param minMag - magnitudes below this will be completely ignored
    */
   public Point2Vert_SS_FaultPoisSource(Location loc, IncrementalMagFreqDist magFreqDist,
                                        MagLengthRelationship magLengthRelationship,
                                        double duration, double magCutOff, double minMag){
     this.magCutOff = magCutOff;
     this.minMag=minMag;

     // make the prob qk rupture
     probEqkRupture = new ProbEqkRupture();
     probEqkRupture.setAveRake(aveRake);

     // set the mags, rates, and rupture surfaces
     setAll(loc,magFreqDist,magLengthRelationship,duration);

   }



   /**
    * This computes a random strike and then builds the list of magnitudes,
    * rates, and finite-rupture surfaces using the given MagLenthRelationship.
    * This also sets the duration.
    * @param loc
    * @param magFreqDist
    * @param magLengthRelationship
    * @param duration
    */
   public void setAll(Location loc, IncrementalMagFreqDist magFreqDist,
                      MagLengthRelationship magLengthRelationship,
                      double duration) {

     // get a random strike between -90 and 90
     double strike = (Math.random()-0.5)*180.0;
//     if(D) System.out.println(C+" random strike = "+strike);
     setAll(loc,magFreqDist,magLengthRelationship,strike,duration);
   }


   /**
    * This builds the list of magnitudes, rates, and finite-rupture surfaces using
    * the given strike and MagLenthRelationship.  This also sets the duration.
    *
    * @param loc
    * @param magFreqDist
    * @param magLengthRelationship
    * @param strike
    * @param duration
    */
  public void setAll(Location loc, IncrementalMagFreqDist magFreqDist,
                     MagLengthRelationship magLengthRelationship,
                     double strike, double duration) {

    if(D) System.out.println("duration="+duration);
    if(D) System.out.println("strike="+strike);
    this.duration = duration;
    mags = new ArrayList();
    rates = new ArrayList();
    for (int i=0; i<magFreqDist.getNum(); ++i){
        if(magFreqDist.getY(i) > 0 && magFreqDist.getX(i) >= minMag){
          mags.add(new Double(magFreqDist.getX(i)));
          rates.add(new Double(magFreqDist.getY(i)));
        }
    }
/*
    if(D) {
      System.out.println("mags & rates:");
      for(int i=0;i<mags.size();i++)
         System.out.println("\t"+ (float)((Double)mags.get(i)).doubleValue()+
                            "\t"+ (float)((Double)rates.get(i)).doubleValue());
    }
*/
    // make the rupture surfaces
    rupSurfaces = new ArrayList();
    FrankelGriddedFaultFactory frFltFactory = new FrankelGriddedFaultFactory();
    Location loc1, loc2;
    Direction dir;
    double mag, halfLength;
    Iterator it = mags.iterator();
    PointSurface ptSurface = new PointSurface(loc);
    while(it.hasNext()) {
      mag = ((Double) it.next()).doubleValue();
      if(mag <= magCutOff) {
        rupSurfaces.add(ptSurface);
      }
      /* it might save memory to used GriddedSubsetSurfaces for all but the largest
         surface below; however, this would presumably be slower as there would be
         more pointers involved */
      else {
        halfLength = magLengthRelationship.getMedianLength(mag)/2.0;
        loc1 = RelativeLocation.getLocation(loc,new Direction(0.0,halfLength,strike,Double.NaN));
        dir = RelativeLocation.getDirection(loc1,loc);
        dir.setHorzDistance(dir.getHorzDistance()*2.0);
        loc2 = RelativeLocation.getLocation(loc1,dir);
        FaultTrace fault = new FaultTrace("");
        fault.addLocation(loc1);
        fault.addLocation(loc2);
        frFltFactory.setAll(fault,aveDip,0.0,0.0,1.0);
        rupSurfaces.add(frFltFactory.getGriddedSurface());
      }
    }
  }



  /**
   * @return the number of rutures (equals number of mags with non-zero rates)
   */
  public int getNumRuptures() {
    //return magsAndRates.getNum();
    return mags.size();
  }


  /**
   * This makes and returns the nth probEqkRupture for this source.
   */
  public ProbEqkRupture getRupture(int nthRupture){

    // set the magnitude
    probEqkRupture.setMag(((Double)mags.get(nthRupture)).doubleValue());

    // compute and set the probability
    double prob = 1 - Math.exp(-duration*((Double)rates.get(nthRupture)).doubleValue());
    probEqkRupture.setProbability(prob);

    // set the rupture surface
    probEqkRupture.setRuptureSurface((GriddedSurfaceAPI) rupSurfaces.get(nthRupture));

    // return the ProbEqkRupture
    return probEqkRupture;
  }


  /**
   * This sets the duration used in computing Poisson probabilities.  This assumes
   * the same units as in the magFreqDist rates.
   * @param duration
   */
  public void setDuration(double duration) {
    this.duration=duration;
  }


  /**
  * This gets the duration used in computing Poisson probabilities
  * @param duration
  */
  public double getDuration() {
    return duration;
  }


     /**
   * This returns the shortest horizontal dist to the point source.
   * @param site
   * @return minimum distance
   */
   public  double getMinDistance(Site site) {

      // get the largest rupture surface (the last one)
      GriddedSurfaceAPI surf = (GriddedSurfaceAPI) rupSurfaces.get(rupSurfaces.size()-1);

      double tempMin, min = Double.MAX_VALUE;
      int nCols = surf.getNumCols();

      // find the minimum to the ends and the center)
      tempMin = RelativeLocation.getHorzDistance(site.getLocation(),surf.getLocation(0,0));
      if(tempMin < min) min = tempMin;
      tempMin = RelativeLocation.getHorzDistance(site.getLocation(),surf.getLocation(0,(int)nCols/2));
      if(tempMin < min) min = tempMin;
      tempMin = RelativeLocation.getHorzDistance(site.getLocation(),surf.getLocation(0,nCols-1));
      if(tempMin < min) min = tempMin;
      return min;

    }

 /**
  * get the name of this class
  *
  * @return
  */
 public String getName() {
   return C;
  }

  // this is temporary for testing purposes
  public static void main(String[] args) {
    Location loc = new Location(34,-118,0);
    GutenbergRichterMagFreqDist dist = new GutenbergRichterMagFreqDist(5,31,0.1,1e17,0.9);
    WC1994_MagLengthRelationship wc_rel = new WC1994_MagLengthRelationship();

//    Point2Vert_SS_FaultPoisSource src = new Point2Vert_SS_FaultPoisSource(loc, dist,
//                                       wc_rel,45, 1.0, 6.0, 5.0);
    Point2Vert_SS_FaultPoisSource src = new Point2Vert_SS_FaultPoisSource(loc, dist,
                                       wc_rel, 1.0, 6.0, 5.0);

    System.out.println("num rups ="+src.getNumRuptures());
    ProbEqkRupture rup;
    Location loc1, loc2;
    System.out.println("Rupture mags and end locs:");
    for(int r=0; r<src.getNumRuptures();r++) {
      rup = src.getRupture(r);
      loc1 = rup.getRuptureSurface().getLocation(0,0);
      loc2 = rup.getRuptureSurface().getLocation(0,rup.getRuptureSurface().getNumCols()-1);
//      System.out.println("\t"+(float)rup.getMag()+"\t"+loc1+"\t"+loc2);
    }
  }
}
