package org.scec.sha.earthquake.rupForecastImpl;

import java.util.Vector;
import java.util.Iterator;

import org.scec.sha.surface.EvenlyGriddedSurface;
import org.scec.data.*;
import org.scec.calc.RelativeLocation;
import org.scec.sha.earthquake.*;
import org.scec.sha.surface.*;


/**
 * <p>Title: SimpleFaultRuptureSource </p>
 * <p>Description: This implements a basic fault source for arbitrary: <p>
 * <UL>
 * <LI>magnitude - the magnitude of the event
 * <LI>ruptureSurface - any EvenlyDiscretizedSurface
 * <LI>rake - that rake (in degrees) assigned to all ruptures.
 * <LI>probability - the probabilisty of the source
 * </UL><p>
 * Note that none of these input objects are saved internally (after construction) in
 * order to conserve memory (this is why there are no associated get/set methods for each).<p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Ned Field
 * @date Sept, 2003
 * @version 1.0
 */

public class SimpleFaultRuptureSource extends ProbEqkSource {

  //for Debug purposes
  private static String  C = new String("SimpleFaultRuptureSource");
  private boolean D = false;


  //name for this classs
  protected String  NAME = C;

  private Vector ruptureList;  // keep this in case we add more mags later
  private Vector faultCornerLocations = new Vector();   // used for the getMinDistance(Site) method

  /* Note that none of the input objects are saved after the ruptureList is created
     by the constructor.  This is deliberate to save memory.  A setName() method was
     added here to enable users to give a unique identifier once created.
  */

  /**
   * Constructor.
   * @param magnitude
   * @param ruptureSurface - any EvenlyGriddedSurface representation of the fault
   * @param rake - average rake of the ruptures
   * @param probability - the probability of the source
   */
  public SimpleFaultRuptureSource(double magnitude,
                                  EvenlyGriddedSurface ruptureSurface,
                                  double rake,
                                  double probability) {

      this.isPoissonian = false;

      if (D) {
        System.out.println("mag: "+magnitude);
        System.out.println("surface rows, cols: "+ruptureSurface.getNumCols()+", "+ruptureSurface.getNumRows());
        System.out.println("rake: "+rake);
        System.out.println("probability: "+probability);

      }
      // make a list of a subset of locations on the fault for use in the getMinDistance(site) method
      makeFaultCornerLocs(ruptureSurface);

      // make the rupture list
      ruptureList = new Vector();

      probEqkRupture = new ProbEqkRupture();
      probEqkRupture.setAveRake(rake);
      probEqkRupture.setRuptureSurface(ruptureSurface);
      probEqkRupture.setMag(magnitude);
      probEqkRupture.setProbability(probability);

      ruptureList.add(probEqkRupture);

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