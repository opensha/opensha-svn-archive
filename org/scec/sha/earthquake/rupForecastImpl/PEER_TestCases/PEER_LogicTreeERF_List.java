package org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases;

import java.util.ListIterator;
import java.util.Vector;

import org.scec.sha.earthquake.*;
import org.scec.data.*;
import org.scec.param.ParameterList;
import org.scec.param.DoubleParameter;
import org.scec.param.StringParameter;
import org.scec.param.event.ParameterChangeListener;
import org.scec.param.event.ParameterChangeEvent;
/**
 * <p>Title: PEER_LogicTreeERF_List </p>
 * <p>Description: This class is needed for Logic Tree for Set 2 Case 5 </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class PEER_LogicTreeERF_List extends ERF_EpistemicList
    implements ParameterChangeListener{

  /**
   * @todo variables
   */
  //for Debug purposes
  public static String  NAME = new String("PEER Logic Tree");

  // declare the slip rates
  private double SLIP_RATE_1 = 0.1;
  private double SLIP_RATE_2 = 2;
  private double SLIP_RATE_3 = 5;

  //declare the mag Upper
  private double MAG_1 = 7.15;
  private double MAG_2 = 6.45;

  //declare the weights
  private double REL_WEIGHT_1 = 0.1;
  private double REL_WEIGHT_2 = 0.3;
  private double REL_WEIGHT_3 = 0.02;
  private double REL_WEIGHT_4 = 0.06;

  // grid spacing parameter stuff
 public final static String GRID_PARAM_NAME =  "Fault Grid Spacing";
 private Double DEFAULT_GRID_VAL = new Double(1);
 public final static String GRID_PARAM_UNITS = "kms";
 private final static double GRID_PARAM_MIN = .001;
 private final static double GRID_PARAM_MAX = 1000;

 //rupture offset parameter stuff
 public final static String OFFSET_PARAM_NAME =  "Offset";
 private Double DEFAULT_OFFSET_VAL = new Double(1);
 public final static String OFFSET_PARAM_UNITS = "kms";
 private final static double OFFSET_PARAM_MIN = .01;
 private final static double OFFSET_PARAM_MAX = 10000;


 // Mag-length sigma parameter stuff
 public final static String SIGMA_PARAM_NAME =  "Mag Length Sigma";
 private double SIGMA_PARAM_MIN = 0;
 private double SIGMA_PARAM_MAX = 1;
 public Double DEFAULT_SIGMA_VAL = new Double(0.0);

 // fault-model parameter stuff
 public final static String FAULT_MODEL_NAME = new String ("Fault Model");
 public final static String FAULT_MODEL_FRANKEL = new String ("Frankel's");
 public final static String FAULT_MODEL_STIRLING = new String ("Stirling's");

 // make the grid spacing parameter
 private DoubleParameter gridParam=new DoubleParameter(GRID_PARAM_NAME,GRID_PARAM_MIN,
     GRID_PARAM_MAX,GRID_PARAM_UNITS,DEFAULT_GRID_VAL);

 // make the rupture offset parameter
 private DoubleParameter offsetParam = new DoubleParameter(OFFSET_PARAM_NAME,OFFSET_PARAM_MIN,
     OFFSET_PARAM_MAX,OFFSET_PARAM_UNITS,DEFAULT_OFFSET_VAL);

 // make the mag-length sigma parameter
 private DoubleParameter lengthSigmaParam = new DoubleParameter(SIGMA_PARAM_NAME,
     SIGMA_PARAM_MIN, SIGMA_PARAM_MAX, DEFAULT_SIGMA_VAL);


 // make the fault-model parameter
 private Vector faultModelNamesStrings = new Vector();
 private StringParameter faultModelParam;



  /**
   * default constructor for this class
   */
  public PEER_LogicTreeERF_List() {

    // create the timespan object with start time and duration in years
    timeSpan = new TimeSpan(TimeSpan.NONE,TimeSpan.YEARS);
    timeSpan.addParameterChangeListener(this);

    // make the faultModelParam
    faultModelNamesStrings.add(FAULT_MODEL_FRANKEL);
    faultModelNamesStrings.add(FAULT_MODEL_STIRLING);
    faultModelParam = new StringParameter(FAULT_MODEL_NAME, faultModelNamesStrings,(String)faultModelNamesStrings.get(0));

    // now add the parameters to the adjustableParams list
    adjustableParams.addParameter(gridParam);
    adjustableParams.addParameter(offsetParam);
    adjustableParams.addParameter(lengthSigmaParam);
    adjustableParams.addParameter(faultModelParam);

    // listen for change in the parameters
    gridParam.addParameterChangeListener(this);
    offsetParam.addParameterChangeListener(this);
    lengthSigmaParam.addParameterChangeListener(this);
    faultModelParam.addParameterChangeListener(this);

    // create the ERFs for each branch

    // add Unsegmented
    addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_NONE, SLIP_RATE_1, MAG_1), REL_WEIGHT_1);
    addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_NONE, SLIP_RATE_2, MAG_1), REL_WEIGHT_2);
    addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_NONE, SLIP_RATE_3, MAG_1), REL_WEIGHT_1);

    //add segment A
    addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_A, SLIP_RATE_1, MAG_2), REL_WEIGHT_3);
    addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_A, SLIP_RATE_2, MAG_2), REL_WEIGHT_4);
    addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_A, SLIP_RATE_3, MAG_2), REL_WEIGHT_3);

    // add segment B
    addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_B, SLIP_RATE_1, MAG_2), REL_WEIGHT_3);
    addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_B, SLIP_RATE_2, MAG_2), REL_WEIGHT_4);
    addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_B, SLIP_RATE_3, MAG_2), REL_WEIGHT_3);

    //add segment C
    addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_C, SLIP_RATE_1, MAG_2), REL_WEIGHT_3);
    addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_C, SLIP_RATE_2, MAG_2), REL_WEIGHT_4);
    addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_C, SLIP_RATE_3, MAG_2), REL_WEIGHT_3);

    //add segment D
    addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_D, SLIP_RATE_1, MAG_2), REL_WEIGHT_3);
    addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_D, SLIP_RATE_2, MAG_2), REL_WEIGHT_4);
    addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_D, SLIP_RATE_3, MAG_2), REL_WEIGHT_3);

    //add segment E
    addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_E, SLIP_RATE_1, MAG_2), REL_WEIGHT_3);
    addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_E, SLIP_RATE_2, MAG_2), REL_WEIGHT_4);
    addERF(createERF(PEER_NonPlanarFaultForecast.SEGMENTATION_E, SLIP_RATE_3, MAG_2), REL_WEIGHT_3);

  }

  /**
   * this method will create the instance of the non-planar fault based on the
   * provided segmentation, slip rate and mag upper
   * @param slipRate
   * @param maxMag
   * @return
   */
  private PEER_NonPlanarFaultForecast createERF(String segmentation,
                                        double slipRate, double magUpper) {
    PEER_NonPlanarFaultForecast forecast = new PEER_NonPlanarFaultForecast();
    forecast.getParameter(PEER_NonPlanarFaultForecast.SEGMENTATION_NAME).setValue(segmentation);
    forecast.getParameter(PEER_NonPlanarFaultForecast.SLIP_RATE_NAME).setValue(new Double(slipRate));
    forecast.getParameter(PEER_NonPlanarFaultForecast.GR_MAG_UPPER).setValue(new Double(magUpper));
    forecast.getParameter(PEER_NonPlanarFaultForecast.DIP_DIRECTION_NAME).setValue(PEER_NonPlanarFaultForecast.DIP_DIRECTION_EAST);
    return forecast;
  }

  /**
   * Return the name for this class
   *
   * @return : return the name for this class
   */
   public String getName(){
     return NAME;
   }

   /**
    * this function is called whenever any parameter changes in the
    * adjustable parameter list
    * @param e
    */
  public void parameterChange(ParameterChangeEvent e) {
    // set the parameter change flag to indicate that forecast needs to be updated
    this.parameterChangeFlag = true;
  }

  /**
   * Update the EqkRupForecasts with the new set of parameters
   */
  public void updateForecast() {
    // set the new values for the parameters in all the EqkRupForecasts in the list
    if(parameterChangeFlag) {
      // set this new value of param in all the EqkRupForecast in the list
      int num = getNumERFs();
      for(int i=0; i<num; ++i) {
        EqkRupForecast eqkRupForecast = (EqkRupForecast)this.getERF(i);
        // see the new parameter values in all the forecasts in the list
        eqkRupForecast.getParameter(GRID_PARAM_NAME).setValue(gridParam.getValue());
        eqkRupForecast.getParameter(OFFSET_PARAM_NAME).setValue(offsetParam.getValue());
        eqkRupForecast.getParameter(SIGMA_PARAM_NAME).setValue(lengthSigmaParam.getValue());
        eqkRupForecast.getParameter(FAULT_MODEL_NAME).setValue(faultModelParam.getValue());
        eqkRupForecast.getTimeSpan().setDuration(timeSpan.getDuration());
      }
    }
    super.updateForecast();
  }
}


