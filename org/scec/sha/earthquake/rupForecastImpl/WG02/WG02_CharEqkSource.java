package org.scec.sha.earthquake.rupForecastImpl.WG02;

import org.scec.sha.surface.EvenlyGriddedSurface;
import org.scec.sha.earthquake.*;
import org.scec.data.*;
import org.scec.calc.RelativeLocation;
import org.scec.sha.magdist.GaussianMagFreqDist;

import java.util.Vector;

/**
 * <p>Title: WG02_CharEqkSource</p>
 * <p>Description: Working Group 2002 characteristic earthquake source </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Edward Field
 * @date April, 2003
 * @version 1.0
 */

public class WG02_CharEqkSource extends ProbEqkSource {

  private double prob;
  private EvenlyGriddedSurface rupSurface;
  private String ruptureName;
  private GaussianMagFreqDist gaussMagDist;
  private int numRupSurfaces, numMag;
  private double rupWidth, rupLength, rupOffset;

  /**
   * Name of this class
   */
  private String name = "WG02_CharEqkSource";

  boolean D = true;


  /**
   * Constructor for this class
   *
   * @param rake : ave rake of the surface
   * @param mag  : Magnitude of the earthquake
   * @param rate : Rate (events/yr) at this mag
   * @param surface : Fault Surface
   */
  public WG02_CharEqkSource(double prob, double meanMag, double magSigma,
                            double nSigmaTrunc, EvenlyGriddedSurface rupSurface,
                            double rupArea, double rupOffset, String ruptureName,
                            double rake) {

      this.prob = prob;
      probEqkRupture = new ProbEqkRupture();
      probEqkRupture.setAveRake(rake);
      probEqkRupture.setRuptureSurface(rupSurface);

      // compute upper and lower mag, rounding to the nearest 0.1 mag
      double tempMag = 10.0*(meanMag+nSigmaTrunc*magSigma);
      double maxMag = (double) Math.round((float)tempMag)/10;
      tempMag=10*(meanMag-nSigmaTrunc*magSigma);
      double minMag = (double) Math.round((float)tempMag) / 10;
      int numMag = Math.round((float)(maxMag-minMag)) + 1;

      // make the gaussian mag freq dist & normalize to unit area
      gaussMagDist = new GaussianMagFreqDist(minMag,maxMag,numMag,meanMag,magSigma,1.0,nSigmaTrunc,2);
      gaussMagDist.scaleToCumRate(0,1.0);

      // compute rupture width and length given the rupArea (rupArea is less than the
      // area of the fault surface (rupSurface) if the aseismic scaling factor (r) was
      // applied as a reduction of rupture area rather than slip rate)
      double faultLength = rupSurface.getNumCols()*rupSurface.getGridSpacing();
      double ddw = rupSurface.getNumRows()*rupSurface.getGridSpacing();
      rupWidth = Math.sqrt(rupArea);
      rupLength = rupWidth;
      // stretch it's length if the width exceeds the down dip width
      if(rupWidth > ddw) {
        rupWidth = ddw;
        rupLength = rupArea/ddw;
      }

      // the total number of ruptures is the number of mags times the number of subsurfaces
      numRupSurfaces = rupSurface.getNumSubsetSurfaces(rupLength,rupWidth,rupOffset);
      numMag = gaussMagDist.getNum();
      if (D) System.out.println("numMag="+numMag+"; numRupSurfaces="+numRupSurfaces);
  }


 /**
  * @return the total num of rutures for the mag which is 1 for the char type fault
  */
  public int getNumRuptures() {
   return numRupSurfaces*numMag;
 }

 /**
  * @param nRupture
  * @return the object for the ProbEqkRupture
  */
  public ProbEqkRupture getRupture(int nRupture){
    int iMag = nRupture/numRupSurfaces;
    int iRupSurf = nRupture - iMag*numRupSurfaces;
    if (D) System.out.println("iMag="+iMag+"; iRupSurf="+iRupSurf);
    return null;
  }


 /**
  * Returns the Vector consisting of all ruptures for this source
  * all the objects are cloned. so this vector can be saved by the user
  * It will only be cloning the first value becuase char type fault contain only
  * 1 probEqkSource object.
  * @return Vector consisting of
  */
  public Vector getRuptureList(){
    Vector v= new Vector();
    v.add(getRuptureClone(0));
    return v;
  }

  /**
   * This returns the shortest dist to either end of the fault trace, or to the
   * mid point of the fault trace.
   * @param site
   * @return minimum distance
   */
   public double getMinDistance(Site site) {

      double min;
      // get first location on fault trace
      Direction dir = RelativeLocation.getDirection(site.getLocation(),(Location) rupSurface.get(0,0));
      min = dir.getHorzDistance();

      // get last location on fault trace
      dir = RelativeLocation.getDirection(site.getLocation(), (Location) rupSurface.get(0,rupSurface.getNumCols()-1));
      if (min > dir.getHorzDistance())
          min = dir.getHorzDistance();

      // get mid location on fault trace
      dir = RelativeLocation.getDirection(site.getLocation(), (Location) rupSurface.get(0,(int) rupSurface.getNumCols()/2));
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

  public static void main( String[] args ) {
    //WG02_CharEqkSource test = new WG02_CharEqkSource(2.516929E-02,  7.18546,  0.120000,  2.00000
  }

}