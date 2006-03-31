package org.opensha.sha.earthquake.rupForecastImpl.Frankel02;

import org.opensha.sha.magdist.*;
import org.opensha.data.*;
import org.opensha.data.function.*;
import org.opensha.calc.RelativeLocation;
import org.opensha.sha.earthquake.*;
import org.opensha.sha.fault.FaultTrace;
import org.opensha.sha.fault.FrankelGriddedFaultFactory;
import org.opensha.calc.magScalingRelations.MagLengthRelationship;
import org.opensha.sha.surface.PointSurface;
import org.opensha.sha.surface.*;

// temp for testing
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.WC1994_MagLengthRelationship;

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
 * magCutOff (magnitudes less than or equal to this will be treated as point sources).
 * This assumes that the duration
 * units are the same as those for the rates in the IncrementalMagFreqDist.</p>
 * This class is loosely modeled after Frankels fortran program "hazgridXv3.f".  However,
 * their use of a random strike means we will never be able to exactly reproduce their
 * results.  Also, they choose a different random strike for each magnitude (and even
 * each site location), whereas we apply one random strike to the entire source (all mags
 * in the given magFreqDist).  NOTE - I put this here rather than in the parent directory
 * because the getMinDistance(Site) method is measured only relative to the grid location
 * (not strictly correct, but it is consistent with how Frankel's 2002 code works.
 * @author Edward Field
 * @date March 24, 2004
 * @version 1.0
 */

public class Point2Vert_SS_FaultPoisSource extends ProbEqkSource implements java.io.Serializable{


  //for Debug purposes
  private static String  C = new String("Point2Vert_SS_FaultPoisSource");
  private boolean D = false;

  private IncrementalMagFreqDist magFreqDist;
  private static final double aveDip=90;
  private static final  double aveRake=0.0;
  private double duration;
  private MagLengthRelationship magLengthRelationship;
  private double magCutOff;
  private PointSurface ptSurface;
  private EvenlyGriddedSurfaceAPI finiteFault;

  // to hold the non-zero mags, rates, and rupture surfaces
//  ArrayList mags, rates, rupSurfaces;

 /**
   * The Full Constructor
   * @param loc - the Location of the point source
   * @param magFreqDist - the mag-freq-dist for the point source
   * @param magLengthRelationship - A relationship for computing length from magnitude
   * @param strike - the strike of the finite SS fault
   * @param duration - the forecast duration
   * @param magCutOff - below (and eqaul) to this value a PointSurface will be applied
   */
  public Point2Vert_SS_FaultPoisSource(Location loc, IncrementalMagFreqDist magFreqDist,
                                       MagLengthRelationship magLengthRelationship,
                                       double strike, double duration, double magCutOff,
                                       FrankelGriddedFaultFactory frankelFaultFactory){
    this.magCutOff = magCutOff;

    // make the prob qk rupture
    probEqkRupture = new ProbEqkRupture();
    probEqkRupture.setAveRake(aveRake);

    if(D) {
      System.out.println("magCutOff="+magCutOff);
      System.out.println("num pts in magFreqDist="+magFreqDist.getNum());
    }

    // set the mags, rates, and rupture surfaces
    setAll(loc,magFreqDist,magLengthRelationship,strike,duration,frankelFaultFactory);
  }


  /**
    * The Constructor for the case where a random strike is computed (rather than assigned)
    * @param loc - the Location of the point source
    * @param magFreqDist - the mag-freq-dist for the point source
    * @param magLengthRelationship - A relationship for computing length from magnitude
    * @param duration - the forecast duration
    * @param magCutOff - below (and eqaul) to this value a PointSurface will be applied
    */
   public Point2Vert_SS_FaultPoisSource(Location loc, IncrementalMagFreqDist magFreqDist,
                                        MagLengthRelationship magLengthRelationship,
                                        double duration, double magCutOff,
                                        FrankelGriddedFaultFactory frankelFaultFactory){
     this.magCutOff = magCutOff;

     // make the prob qk rupture
     probEqkRupture = new ProbEqkRupture();
     probEqkRupture.setAveRake(aveRake);

     // set the mags, rates, and rupture surfaces
     setAll(loc,magFreqDist,magLengthRelationship,duration,frankelFaultFactory);

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
                      double duration, FrankelGriddedFaultFactory frankelFaultFactory) {

     // get a random strike between -90 and 90
     double strike = (Math.random()-0.5)*180.0;
     if (strike < 0.0) strike +=360;
// System.out.println(C+" random strike = "+strike);
     setAll(loc,magFreqDist,magLengthRelationship,strike,duration,frankelFaultFactory);
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
                     double strike, double duration,
                     FrankelGriddedFaultFactory frankelFaultFactory) {

    if(D) System.out.println("duration="+duration);
    if(D) System.out.println("strike="+strike);
    this.duration = duration;
    this.magFreqDist = magFreqDist;
    this.magLengthRelationship = magLengthRelationship;

    // make the point surface
    ptSurface = new PointSurface(loc);
    ptSurface.setAveDip(aveDip);
    ptSurface.setAveStrike(strike);
    double maxMag = magFreqDist.getX(magFreqDist.getNum()-1);
    // make finite source if necessary
    if(maxMag > magCutOff) {
      Location loc1, loc2;
      Direction dir;
      double halfLength = magLengthRelationship.getMedianLength(maxMag)/2.0;
      loc1 = RelativeLocation.getLocation(loc,new Direction(0.0,halfLength,strike,Double.NaN));
      dir = RelativeLocation.getDirection(loc1,loc);
      dir.setHorzDistance(dir.getHorzDistance()*2.0);
      loc2 = RelativeLocation.getLocation(loc1,dir);
      FaultTrace fault = new FaultTrace("");
      fault.addLocation(loc1);
      fault.addLocation(loc2);
      frankelFaultFactory.setAll(fault,aveDip,loc.getDepth(),loc.getDepth(),1.0);
      finiteFault = frankelFaultFactory.getGriddedSurface();
    }
  }

  /**
  * It returns a list of all the locations which make up the surface for this
  * source.
  *
  * @return LocationList - List of all the locations which constitute the surface
  * of this source
  */
  public LocationList getAllSourceLocs() {
    if(this.finiteFault!=null) return finiteFault.getLocationList();
    else return ptSurface.getLocationList();
  }




  /**
   * @return the number of rutures (equals number of mags with non-zero rates)
   */
  public int getNumRuptures() {
    //return magsAndRates.getNum();
    return magFreqDist.getNum();
  }


  /**
   * This makes and returns the nth probEqkRupture for this source.
   */
  public ProbEqkRupture getRupture(int nthRupture){

    // set the magnitude
    double mag = magFreqDist.getX(nthRupture);
    probEqkRupture.setMag(mag);

    // compute and set the probability
    double prob = 1 - Math.exp(-duration*magFreqDist.getY(nthRupture));
    probEqkRupture.setProbability(prob);

    // set the rupture surface
    if(mag <= this.magCutOff)
      probEqkRupture.setRuptureSurface(ptSurface);
    else {
      if(nthRupture == magFreqDist.getNum()-1) {
        probEqkRupture.setRuptureSurface(finiteFault);
      }
      else {
        double rupLen = magLengthRelationship.getMedianLength(mag);
        double startPoint = (double)finiteFault.getNumCols()/2.0 - 0.5 - rupLen/2.0;
        GriddedSubsetSurface rupSurf = new GriddedSubsetSurface(1,Math.round((float)rupLen+1),
                                                             0,Math.round((float)startPoint),
                                                             finiteFault);
        probEqkRupture.setRuptureSurface(rupSurf);
      }
    }

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

/*
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
     */
     return RelativeLocation.getHorzDistance(site.getLocation(),ptSurface);
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
    GutenbergRichterMagFreqDist dist = new GutenbergRichterMagFreqDist(5,16,0.2,1e17,0.9);
    WC1994_MagLengthRelationship wc_rel = new WC1994_MagLengthRelationship();
    FrankelGriddedFaultFactory fltFact = new FrankelGriddedFaultFactory();

//    Point2Vert_SS_FaultPoisSource src = new Point2Vert_SS_FaultPoisSource(loc, dist,
//                                       wc_rel,45, 1.0, 6.0, 5.0);
    Point2Vert_SS_FaultPoisSource src = new Point2Vert_SS_FaultPoisSource(loc, dist,
                                       wc_rel, 1.0, 6.0, fltFact);

    System.out.println("num rups ="+src.getNumRuptures());
    ProbEqkRupture rup;
    Location loc1, loc2;
    double length, aveLat, aveLon;
    System.out.println("Rupture mags and end locs:");
    for(int r=0; r<src.getNumRuptures();r++) {
      rup = src.getRupture(r);
      loc1 = rup.getRuptureSurface().getLocation(0,0);
      loc2 = rup.getRuptureSurface().getLocation(0,rup.getRuptureSurface().getNumCols()-1);
      length = RelativeLocation.getHorzDistance(loc1,loc2);
      aveLat = (loc1.getLatitude()+loc2.getLatitude())/2;
      aveLon = (loc1.getLongitude()+loc2.getLongitude())/2;
//      System.out.println("\t"+(float)rup.getMag()+"\t"+loc1+"\t"+loc2);
      System.out.println("\t"+(float)rup.getMag()+
                         "\tlen1="+(float)wc_rel.getMedianLength(rup.getMag())+
                         "\tlen2="+(float)length+"\taveLat="+(float)aveLat+
                         "\taveLon="+(float)aveLon);
    }
  }
}
