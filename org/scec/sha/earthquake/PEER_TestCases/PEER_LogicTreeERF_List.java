package org.scec.sha.earthquake.PEER_TestCases;

import java.util.ListIterator;
import org.scec.sha.earthquake.*;
import org.scec.data.*;

/**
 * <p>Title: PEER_LogicTreeERF_List </p>
 * <p>Description: This class is needed for Logic Tree for Set 2 Case 5 </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class PEER_LogicTreeERF_List extends ERF_EpistemicList {

  // declare the slip rates
  private double SLIP_RATE_1 = 0.1;
  private double SLIP_RATE_2 = 0.2;
  private double SLIP_RATE_3 = 0.5;

  //declare the mag Upper
  private double MAG_1 = 7.2;
  private double MAG_2 = 6.5;

  //declare the weights
  private double REL_WEIGHT_1 = 0.1;
  private double REL_WEIGHT_2 = 0.3;
  private double REL_WEIGHT_3 = 0.02;
  private double REL_WEIGHT_4 = 0.06;


  /**
   * default constructor for this class
   */
  public PEER_LogicTreeERF_List() {
    // this constructor will create the instances of the non-planar with various parameters
    // thes instances will be added to the the list

    // add Unsegmented
    this.addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_NONE, SLIP_RATE_1, MAG_1), REL_WEIGHT_1);
    this.addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_NONE, SLIP_RATE_2, MAG_1), REL_WEIGHT_2);
    this.addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_NONE, SLIP_RATE_3, MAG_1), REL_WEIGHT_1);

    //add segment A
    this.addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_A, SLIP_RATE_1, MAG_2), REL_WEIGHT_3);
    this.addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_A, SLIP_RATE_2, MAG_2), REL_WEIGHT_4);
    this.addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_A, SLIP_RATE_3, MAG_2), REL_WEIGHT_3);

    // add segment B
    this.addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_B, SLIP_RATE_1, MAG_2), REL_WEIGHT_3);
    this.addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_B, SLIP_RATE_2, MAG_2), REL_WEIGHT_4);
    this.addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_B, SLIP_RATE_3, MAG_2), REL_WEIGHT_3);

    //add segment C
    this.addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_C, SLIP_RATE_1, MAG_2), REL_WEIGHT_3);
    this.addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_C, SLIP_RATE_2, MAG_2), REL_WEIGHT_4);
    this.addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_C, SLIP_RATE_3, MAG_2), REL_WEIGHT_3);

    //add segment D
    this.addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_D, SLIP_RATE_1, MAG_2), REL_WEIGHT_3);
    this.addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_D, SLIP_RATE_2, MAG_2), REL_WEIGHT_4);
    this.addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_D, SLIP_RATE_3, MAG_2), REL_WEIGHT_3);

    //add segment E
    this.addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_E, SLIP_RATE_1, MAG_2), REL_WEIGHT_3);
    this.addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_E, SLIP_RATE_2, MAG_2), REL_WEIGHT_4);
    this.addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_E, SLIP_RATE_3, MAG_2), REL_WEIGHT_3);


  }

  /**
   * this method will create the instance of the non-planar fault based on the
   * provided segmenattion, slip rate and mag upper
   * @param slipRate
   * @param maxMag
   * @return
   */
  private PEER_NonPlanarFaultForecast createERF(String segmentation,
                                        double slipRate, double magUpper) {
    PEER_NonPlanarFaultForecast forecast = new PEER_NonPlanarFaultForecast();
    forecast.segModelParam.setValue(segmentation);
    forecast.slipRateParam.setValue(slipRate);
    forecast.magUpperParam.setValue(magUpper);
    return forecast;
  }


  public ListIterator getAdjustableParamsList() {
    /**@todo Implement this org.scec.sha.earthquake.ERF_List abstract method*/
    throw new java.lang.UnsupportedOperationException("Method getAdjustableParamsList() not yet implemented.");
  }

  public void updateERF_List() {
    /**@todo Implement this org.scec.sha.earthquake.ERF_List abstract method*/
  }

  public boolean isLocWithinApplicableRegion(Location loc) {
    /**@todo Implement this org.scec.sha.earthquake.ERF_List abstract method*/
    throw new java.lang.UnsupportedOperationException("Method isLocWithinApplicableRegion() not yet implemented.");
  }

}