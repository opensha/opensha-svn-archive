package org.scec.sha.earthquake;

import java.util.Vector;
import java.util.Iterator;

import org.scec.calc.magScalingRelations.*;
import org.scec.sha.surface.EvenlyGriddedSurface;
import org.scec.sha.magdist.*;
import org.scec.data.*;
import org.scec.calc.RelativeLocation;
import org.scec.sha.earthquake.*;
import org.scec.sha.surface.*;


/**
 * <p>Title: SimplePoissonFaultSource </p>
 * <p>Description: This implements a basic Poisson fault source for arbitrary: <p>
 * <UL>
 * <LI>magDist - any IncrementalMagFreqDist
 * <LI>faultSurface - any EvenlyDiscretizedSurface
 * <LI>magScalingRel- any magLenthRelationship or magAreaRelalationship
 * <LI>magScalingSigma - the standard deviation of log(Lenth) or log(Area)
 * <LI>rupAspectRatio - the ratio of rutpure length to rutpure width (down-dip)
 * <LI>rupOffset - the amount by which ruptures are offset on the fault.
 * <LI>rake - that rake (in degrees) assigned to all ruptures.
 * <LI>timeSpan - the duration of the forecast (in same usints as in the magFreqDist)
 * </UL><p>
 * Note that none of these input objects are saved internally (after construction) in
 * order to conserve memory (this is why there are no associated get/set methods for each).<p>
 * The ruptures are placed uniformly across the fault surface (at rupOffset spacing), which
 * means there is a tapering of implied slip amounts at the ends of the fault.<p>
 * All magnitudes below 5.0 in the magDist are ignored in building the ruptures (unless
 * one uses the constructor that allows the "minMag" to be set by hand). <p>
 * Note that magScalingSigma can be either a MagAreaRelationship or a
 * MagLengthRelationship.  If a MagAreaRelationship is being used, and the rupture
 * width implied for a given mangitude exceeds the down-dip width of the faultSurface,
 * then the rupture length is increased accordingly and the rupture width is set as
 * the down-dip width.  If a MagLengthRelationship is being used, and the rupture
 * width impled by the rupAspectRatio exceeds the down-dip width, everything below
 * the bottom edge of the fault is simply cut off (ignored).  Thus, with a
 * MagLengthRelationship you can force rupture of the entire down-dip width by giving
 * rupAspecRatio a very small value.</p>
 * magScalingSigma is set by hand (rather than getting it from the magScalingRel) to
 * allow maximum flexibility (e.g., some relationships do not even give a sigma value).<p>
 * If magScalingSigma is non zero, then three branches are considered for the Area or Lengths
 * values: median and +/-1.64sigma, with relative weights of 0.6, 0.2, and 0.2, respectively.<p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Ned Field
 * @date Sept, 2003
 * @version 1.0
 */

public class SimplePoissonFaultSource extends ProbEqkSource {

  //for Debug purposes
  private static String  C = new String("SimplePoissonFaultSource");
  private boolean D = false;

  //name for this classs
  protected String  NAME = C;

  // private fields
  private int totNumRups;
  private Vector ruptureList;
  private Vector faultCornerLocations;   // used for the getMinDistance(Site) method
  private double timeSpan;

  /* Note that none of the input objects are saved after the ruptureList is created
     by the constructor.  This is deliberate to save memory.  A setName() method was
     added here to enable users to give a unique identifier once created.
  */

  /**
   * This creates the Simple Poisson Fault Source.
   * @param magDist - any incremental mag. freq. dist. object
   * @param faultSurface - any EvenlyGriddedSurface representation of the fault
   * @param magScalingRel - any magAreaRelationship or magLengthRelationthip
   * @param magScalingSigma - uncertainty of the length(mag) or area(mag) relationship
   * @param rupAspectRatio - ratio of rupture length to rupture width
   * @param rupOffset - amount of offset for floating ruptures
   * @param rake - average rake of the ruptures
   * @param timeSpan - the timeSpan of interest in years (this is a Poissonian source)
   * @param minMag - the minimum magnitude to be considered from magDist (lower mags are ignored)
   */
  public SimplePoissonFaultSource(IncrementalMagFreqDist magDist,
                                  EvenlyGriddedSurface faultSurface,
                                  MagScalingRelationship magScalingRel,
                                  double magScalingSigma,
                                  double rupAspectRatio,
                                  double rupOffset,
                                  double rake,
                                  double timeSpan,
                                  double minMag) {

      this.timeSpan = timeSpan;

      // make a list of a subset of locations on the fault for use in the getMinDistance(site) method
      makeFaultCornerLocs(faultSurface);

      // make the rupture list
      ruptureList = new Vector();
      if(magScalingSigma == 0.0)
        addRupturesToList(magDist, faultSurface, magScalingRel, magScalingSigma, rupAspectRatio, rupOffset, rake, minMag, 0.0, 1.0);
      else {
//        The branch-tip weights (0.6, 0.2, and 0.2) for the mean, -1.64sigma, and +1.64sigma,
//       respectively, are from WG99's Table 1.1
        addRupturesToList(magDist, faultSurface, magScalingRel, magScalingSigma,
                          rupAspectRatio, rupOffset, rake, minMag, 0.0, 0.6);
        addRupturesToList(magDist, faultSurface, magScalingRel,
                          magScalingSigma, rupAspectRatio, rupOffset, rake, minMag, 1.64, 0.2);
        addRupturesToList(magDist, faultSurface, magScalingRel,
                          magScalingSigma, rupAspectRatio, rupOffset, rake, minMag, -1.64, 0.2);
      }
  }


  /**
   * Same as other constuctor, but where minMag defaults to 5.0.
   */
  public SimplePoissonFaultSource(IncrementalMagFreqDist magDist,
                                  EvenlyGriddedSurface faultSurface,
                                  MagScalingRelationship magScalingRel,
                                  double magScalingSigma,
                                  double rupAspectRatio,
                                  double rupOffset,
                                  double rake,
                                  double timeSpan) {
    this( magDist, faultSurface, magScalingRel,magScalingSigma,rupAspectRatio,rupOffset,rake,timeSpan,5.0);
  }

  /**
   * This computes the rupture length from the information supplied
   * @param magScalingRel - a MagLengthRelationship or a MagAreaRelationship
   * @param magScalingSigma - the standard deviation of the Area or Length estimate
   * @param numSigma - the number of sigmas from the mean for which the estimate is for
   * @param rupAspectRatio
   * @param mag
   * @return
   */
  private double getRupLength(MagScalingRelationship magScalingRel,
                              double magScalingSigma,
                              double numSigma,
                              double rupAspectRatio,
                              double mag) throws RuntimeException {

    // if it's a mag-area relationship
    if(magScalingRel instanceof MagAreaRelationship) {
      double area = magScalingRel.getMedianScale(mag) * Math.pow(10,numSigma*magScalingSigma);
      return Math.sqrt(area*rupAspectRatio);
    }
    else if (magScalingRel instanceof MagLengthRelationship) {
      return magScalingRel.getMedianScale(mag) * Math.pow(10,numSigma*magScalingSigma);
    }
    else throw new RuntimeException("bad type of MagScalingRelationship");
  }



  /**
   * This method makes and adds ruptures to the list
   */
  private void addRupturesToList(IncrementalMagFreqDist magDist,
                             EvenlyGriddedSurface faultSurface,
                             MagScalingRelationship magScalingRel,
                             double magScalingSigma,
                             double rupAspectRatio,
                             double rupOffset,
                             double rake,
                             double minMag,
                             double numSigma,
                             double weight) {

    int numMags = magDist.getNum();  // Note that some of these may have zero rates!

    double rupLen;
    double rupWidth;
    double numRup;
    double mag;
    double rate;
    double prob=Double.NaN;

    // get down-dip width of fault (from first column)
    Location loc1 = (Location) faultSurface.get(0,0);
    Location loc2 = (Location) faultSurface.get(faultSurface.getNumRows()-1,0);
    double ddw = RelativeLocation.getTotalDistance(loc1,loc2);

    if( D ) System.out.println(C+": ddw="+ddw);

    if( D ) System.out.println(C+": magScalingSigma="+magScalingSigma);

    for(int i=0;i<numMags;++i){
      mag = magDist.getX(i);
      // make sure it has a non-zero rate & the mag is >= minMag
      if(magDist.getY(i) > 0 && mag >= minMag) {

        rupLen = getRupLength(magScalingRel,magScalingSigma,numSigma,rupAspectRatio,mag);
        rupWidth= rupLen/rupAspectRatio;

        // if magScalingRel is a MagAreaRelationship, then rescale rupLen if rupWidth
        // exceeds the down-dip width (don't do anything for MagLengthRelationship)
        if(magScalingRel instanceof MagAreaRelationship  && rupWidth > ddw) {
          rupLen *= rupWidth/ddw;
          rupWidth = ddw;
        }

        numRup = faultSurface.getNumSubsetSurfaces(rupLen,rupWidth,rupOffset);
        rate = magDist.getY(mag);
        // Create the ruptures and add to the list
        for(int r=0; r < numRup; ++r) {
          probEqkRupture = new ProbEqkRupture();
          probEqkRupture.setAveRake(rake);
          probEqkRupture.setRuptureSurface(faultSurface.getNthSubsetSurface(rupLen,rupWidth,rupOffset,r));
          probEqkRupture.setMag(mag);
          prob = weight*(1.0 - Math.exp(-timeSpan*rate/numRup));
          probEqkRupture.setProbability(prob);
          ruptureList.add(probEqkRupture);
        }
          if( D ) System.out.println(C+": mag="+mag+"; rupLen="+rupLen+"; rupWidth="+rupWidth+
                                      "; rate="+rate+"; timeSpan="+timeSpan+"; numRup="+numRup+
                                      "; weight="+weight+"; prob="+prob);
      }
    }
  }


  /**
   * @return the total num of rutures for all magnitudes
   */
  public int getNumRuptures() { return ruptureList.size(); }


  /**
   * This method returns the nth Rupture in the list
   */
  public ProbEqkRupture getRupture(int nthRupture){ return (ProbEqkRupture) ruptureList.get(nthRupture); }


   /**
   * This returns the shortest dist to either end of the fault trace, or to the
   * mid point of the fault trace (done also for the bottom edge of the fault).
   * @param site
   * @return minimum distance in km
   */
   public  double getMinDistance(Site site) {

      double min = Double.MAX_VALUE;
      double tempMin;

      Iterator it = faultCornerLocations.iterator();

      while(it.hasNext()) {
        tempMin = RelativeLocation.getHorzDistance(site.getLocation(),(Location)it.next());
        if(tempMin < min) min = tempMin;
      }

      return min;
    }


    /**
     * This makes the vector of fault corner location used by the getMinDistance(site)
     * method.
     * @param faultSurface
     */
    private void makeFaultCornerLocs(EvenlyGriddedSurface faultSurface) {

      int nRows = faultSurface.getNumRows();
      int nCols = faultSurface.getNumCols();
      faultCornerLocations.add(faultSurface.get(0,0));
      faultCornerLocations.add(faultSurface.get(0,(int)(nCols/2)));
      faultCornerLocations.add(faultSurface.get(0,nCols-1));
      faultCornerLocations.add(faultSurface.get(nRows-1,0));
      faultCornerLocations.add(faultSurface.get(nRows-1,(int)(nCols/2)));
      faultCornerLocations.add(faultSurface.get(nRows-1,nCols-1));


    }

    /**
     * set the name of this class
     *
     * @return
     */
    public void setName(String name) {
      NAME=name;
     }

 /**
  * get the name of this class
  *
  * @return
  */
 public String getName() {
   return NAME;
  }
}