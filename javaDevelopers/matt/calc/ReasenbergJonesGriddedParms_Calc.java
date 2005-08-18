package javaDevelopers.matt.calc;

import org.opensha.data.region.EvenlyGriddedGeographicRegionAPI;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import java.util.*;
import org.opensha.data.region.CircularGeographicRegion;
import org.opensha.data.Location;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ReasenbergJonesGriddedParms_Calc {

  private static boolean useFixed_cValue = true;
  private double[] grid_pVal,grid_cVal,grid_kVal,grid_aVal,grid_bVal;
  private double constantAddToMc = .2;

  // IF setGridd is CALLED HERE, HOW CAN THE DEFAULT FOR FIXED_C BE CHANGED B4 CALCULATING?
  public void ReasenbergJonesGriddedParms_Calc(EvenlyGriddedGeographicRegionAPI gridNodes, ObsEqkRupList eventList) {
   setGriddedMags(gridNodes, eventList);
  }

  /**
   * setUseFixed_cVal
   * if true c will be fixed for the Omori calculations
   * default is fixed
   */
  public void setUseFixed_cVal(boolean fix_cVal) {
    useFixed_cValue = fix_cVal;
  }

  /**
   * set_constantAddToCompleteness
   */
  public void set_constantAddToCompleteness(double mcConst) {
    constantAddToMc = mcConst;
  }

  /**
   * get_Gridded_pVals
   */
  public double[] get_Gridded_pVals() {
    return grid_pVal;
  }

  /**
   * get_Gridded_cVals
   */
  public double[] get_Gridded_cVals() {
    return grid_cVal;
  }

  /**
   * get_Gridded_kVals
   */
  public double[] get_Gridded_kVals() {
    return grid_kVal;
  }

  /**
   * get_Gridded_aVals
   */
  public double[] get_Gridded_aVals() {
    return grid_aVal;
  }

  /**
   * get_Gridded_bVals
   */
  public double[] get_Gridded_bVals() {
    return grid_bVal;
  }

  /**
     * getAllGriddedVals
     * Returns an arraylist of all RJ Parms
     * 1 - a value
     * 2 - b value
     * 3 - p value
     * 4 - k value
     * 5 - c value
     */
    public ArrayList getAllGriddedVals() {
      ArrayList RJParms = new ArrayList(5);
      RJParms.add(1,grid_aVal);
      RJParms.add(2,grid_bVal);
      RJParms.add(3,grid_pVal);
      RJParms.add(4,grid_kVal);
      RJParms.add(5,grid_cVal);
      return RJParms;
  }


  /**
   * setGriddedMags
   * calculate the Reasenberg & Jones parms (a,b,p,c,k) on the evenly gridded region grid.
   *
   */
  public void setGriddedMags(EvenlyGriddedGeographicRegionAPI gridNodes, ObsEqkRupList eventList) {
    calc_RJParmsOnGrid(gridNodes, eventList);
  }

  /**
   * calc_RJParmsOnGrid
   */
  private void calc_RJParmsOnGrid(EvenlyGriddedGeographicRegionAPI gridNodes, ObsEqkRupList eventList){
    ListIterator gridIt = gridNodes.getGridLocationsIterator();
    int numNodes = gridNodes.getNumGridLocs();
    grid_aVal = new double[numNodes];
    grid_bVal = new double[numNodes];
    grid_pVal = new double[numNodes];
    grid_kVal = new double[numNodes];
    grid_cVal = new double[numNodes];

    ListIterator eventIt = eventList.listIterator();
    int numEvents = eventList.size();
    double[] eventDist = new double[numEvents];
    double searchRadius;
    double completenessMag;
    int ind = 0;

    // set the appropriate radius to use for collecting events for the node
    if (numEvents < 1000) searchRadius = 15;
    else if (numEvents < 1500) searchRadius = 12;
    else if (numEvents < 2000) searchRadius = 10;
    else searchRadius = 7.5;


    while (gridIt.hasNext()) {
      CircularGeographicRegion gridRegion = new CircularGeographicRegion((Location)gridIt.next(),searchRadius);
      ObsEqkRupList regionList = new ObsEqkRupList();
      while (eventIt.hasNext()) {
        if (gridRegion.isLocationInside((Location)eventIt.next()));
          regionList.addObsEqkEvent((ObsEqkRupture)eventIt.previous());  // HOW DO I DO THIS AND GET THE SAME AS THE ABOVE LINE??
      }

      // Calculate the completeness of the events selected for the node and remove
      // events below this mag.
      CompletenessMagCalc.setMcBest(regionList);
      completenessMag = CompletenessMagCalc.getMcBest();
      //WHY DOES THE BELOW RETURN AN ARRAYLIST?!?!!?
      ObsEqkRupList completeRegionList = regionList.getObsEqkRupsAboveMag(completenessMag+constantAddToMc);

      // Calculate the Gutenberg-Richter parms
      MaxLikeGR_Calc.setMags(completeRegionList);
      grid_aVal[ind] = MaxLikeGR_Calc.get_aValueMaxLike();
      grid_bVal[ind] = MaxLikeGR_Calc.get_bValueMaxLike();

      // If there are 100 events of more, calculate the Omori parms.
      if (completeRegionList.size() >= 100){
        MaxLikeOmori_Calc omoriCalc = new MaxLikeOmori_Calc();
        if (useFixed_cValue)
          omoriCalc.set_AfterShockListFixed_c(completeRegionList);
        else
          omoriCalc.set_AfterShockList(completeRegionList);

        grid_cVal[ind] = omoriCalc.get_c_value();
        grid_pVal[ind] = omoriCalc.get_p_value();
        grid_kVal[ind++] = omoriCalc.get_k_value();
      }
      // if less than 100 events, fill the parms with dummy vals.
      else {
        grid_cVal[ind] = Double.NaN;
        grid_pVal[ind] = Double.NaN;
        grid_kVal[ind++] = Double.NaN;
      }
    }
  }



  public static void main(String[] args) {
    ReasenbergJonesGriddedParms_Calc reasenbergjonesgriddedparms_calc = new
        ReasenbergJonesGriddedParms_Calc();
  }
}
