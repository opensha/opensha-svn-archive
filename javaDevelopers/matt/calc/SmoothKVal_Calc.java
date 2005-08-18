package javaDevelopers.matt.calc;

import java.util.*;

import org.opensha.calc.*;
import org.opensha.data.*;
import org.opensha.data.region.*;
import org.opensha.sha.earthquake.*;
import org.opensha.sha.earthquake.observedEarthquake.*;
import org.opensha.sha.fault.*;

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
public class SmoothKVal_Calc {
  private  STEP_AftershockHypoMagForecast aftershockModel;
  private  EvenlyGriddedGeographicRegionAPI aftershockZone;
  private double global_aVal, global_bVal, global_Mc;
  private ObsEqkRupture mainshock;
  private FaultTrace faultTrace;
  private double[] nodeTaper_k;

  public SmoothKVal_Calc() {

  }

  /**
  * setAftershockModel
  */
 public void setAftershockModel(STEP_AftershockHypoMagForecast aftershockModel) {
   aftershockZone = aftershockModel.getAfterShockZone();
   double[] grid_aVal = aftershockModel.get_Gridded_aVal();
   global_aVal = grid_aVal[0];
   double[] grid_bVal = aftershockModel.get_Gridded_bVal();
   global_bVal = grid_bVal[0];
   double[] gridMc = aftershockModel.get_nodeCompletenessMag();
   global_Mc = gridMc[0];
   SimpleFaultData mainshockFault = aftershockModel.get_FaultModel();
   mainshock = aftershockModel.getMainShock();
   faultTrace = mainshockFault.getFaultTrace();

   //now do the calculations
   set_k_value();

 }


      /**
     * set_k_value
     * This will taper the generic k value.  Each grid node will be assigned
     * a k value based on the distance from the fault.
     */
    private void set_k_value() {
      double sumInvDist = 0;

      int numLocs = aftershockZone.getNumGridLocs();
      nodeTaper_k =  new double[numLocs];
      double[] nodeDistFromFault = new double[numLocs];
      double[] invDist = new double[numLocs];
      double[] nodePerc = new double[numLocs];

      //get the iterator of all the locations within that region
      ListIterator it = aftershockZone.getGridLocationsIterator();
      int ind = 0;
      int numFaultPoints = faultTrace.size();
      double totDistFromFault = 0;
      while (it.hasNext()) {
        nodeDistFromFault[ind++] = RelativeLocation.getApproxHorzDistToLine( (
            Location) (it.next()), faultTrace.getLocationAt(0),
            faultTrace.getLocationAt(numFaultPoints));
        totDistFromFault = totDistFromFault +
            Math.pow(nodeDistFromFault[ind - 1], 2.0);
      }

      for (int indLoop = 0; indLoop < numLocs; ++indLoop) {
        invDist[indLoop] = totDistFromFault /
            Math.pow(nodeDistFromFault[indLoop], 2.0);
        sumInvDist = sumInvDist + invDist[indLoop];
      }

      for (int indLoop = 0; indLoop < ind - 1; ++indLoop) {
        nodePerc[indLoop] = invDist[indLoop] / sumInvDist;
      }


      double mainshockMag = mainshock.getMag();
      double rightSide = global_aVal + global_bVal * (mainshockMag - global_Mc);
      double generic_k = Math.pow(10,rightSide);
      for (int indLoop = 0; indLoop < ind -1; ++indLoop) {
           nodeTaper_k[indLoop] = generic_k * nodePerc[indLoop];
      }
    }

  /**
   * get_Smooth_kVal
   */
  public double[] get_Smooth_kVal() {
    return nodeTaper_k;
  }


}
