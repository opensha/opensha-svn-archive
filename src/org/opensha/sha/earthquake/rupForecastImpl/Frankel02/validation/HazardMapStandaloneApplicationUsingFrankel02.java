package org.opensha.sha.earthquake.rupForecastImpl.Frankel02.validation;



import java.util.ArrayList;
import java.net.*;
import java.io.*;

import org.opensha.sha.imr.*;
import org.opensha.sha.imr.attenRelImpl.BJF_1997_AttenRel;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;
import org.opensha.sha.imr.param.OtherParams.ComponentParam;
import org.opensha.sha.imr.param.OtherParams.SigmaTruncLevelParam;
import org.opensha.sha.imr.param.OtherParams.SigmaTruncTypeParam;
import org.opensha.sha.imr.param.OtherParams.StdDevTypeParam;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;


import org.opensha.commons.data.Location;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.region.EvenlyGriddedGeographicRegion;
import org.opensha.commons.data.region.SitesInGriddedRectangularRegion;
import org.opensha.commons.data.region.SitesInGriddedRegion;
import org.opensha.commons.exceptions.ParameterException;
import org.opensha.commons.exceptions.RegionConstraintException;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.WarningParameterAPI;
import org.opensha.commons.param.event.ParameterChangeWarningEvent;
import org.opensha.commons.param.event.ParameterChangeWarningListener;
import org.opensha.sha.gui.controls.X_ValuesInCurveControlPanelAPI;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.gui.infoTools.IMT_Info;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast;
import org.opensha.sha.calc.hazardMap.HazardMapCalculatorOld;

/**
 * <p>Title: HazardMapStandaloneApplicationUsingFrankel02</p>
 * <p>Description: </p>
 * @author: Ned Field & Nitin Gupta & Vipin Gupta
 * @created : March 15,2004
 * @version 1.0
 */

public class HazardMapStandaloneApplicationUsingFrankel02
    implements  ParameterChangeWarningListener{


  /**
   * Name of the class
   */
  protected final static String C = "HazardMapApplet";
  // for debug purpose
  protected final static boolean D = false;
  public static String SERVLET_URL  = "http://gravity.usc.edu/OpenSHA/servlet/HazardMapCalcServlet";
  public static String DATASET_CHECK_SERVLET_URL = "http://gravity.usc.edu/OpenSHA/servlet/DatasetIdAndMetadataCheckServlet";


  //store the site values for each site in the griddded region
  private SitesInGriddedRegion griddedRegionSites;

  //gets the instance of the selected AttenuationRelationship
  private AttenuationRelationship attenRel;


   //holds the ArbitrarilyDiscretizedFunc
  private ArbitrarilyDiscretizedFunc function;
  //instance to get the default IMT X values for the hazard Curve
  private IMT_Info imtInfo = new IMT_Info();


  // make the Frankel-02 forecast
  private Frankel02_AdjustableEqkRupForecast forecast=null;

  // region bounds
  private final double MIN_LAT= 33.5;
  private final double MAX_LAT= 34.7;
  private final double MIN_LON = -119.5;
  private final double MAX_LON= -117.0;
  private final double GRID_SPACING= 0.1;


  //Construct the application
  public HazardMapStandaloneApplicationUsingFrankel02() throws
      RegionConstraintException {
    init();
    run();
  }

  //Initialize the application
  public void init() throws RegionConstraintException {
    try{
      initIMRGuiBean();
    }catch(RuntimeException e){
      e.printStackTrace();
      return;
    }

      this.initGriddedRegionGuiBean();
    this.initIMTGuiBean();
    try{
        this.initERFSelector_GuiBean();
        initTimeSpanGuiBean();
      }catch(RuntimeException e){
        e.printStackTrace();
      return;
      }
  }



  //Main method
  public static void main(String[] args) {
    try{
      HazardMapStandaloneApplicationUsingFrankel02 app = new HazardMapStandaloneApplicationUsingFrankel02();
    }
    catch(Exception e){
      System.out.println(e.getMessage());
      e.printStackTrace();
      System.exit(0);
    }
  }




  /**
   * Initialise the Gridded Region sites gui bean
   *
   */
  private void initGriddedRegionGuiBean() throws RegionConstraintException {

    //make the Gridded Region object
//	  EvenlyGriddedGeographicRegion eggr = 
//		  new EvenlyGriddedGeographicRegion(
//				  MIN_LAT, MAX_LAT, MIN_LON,MAX_LON, GRID_SPACING);
	  EvenlyGriddedGeographicRegion eggr = 
		  new EvenlyGriddedGeographicRegion(
	    		new Location(MIN_LAT, MIN_LON),
	    		new Location(MAX_LAT, MAX_LON),
	    		GRID_SPACING, new Location(0,0));    
	  griddedRegionSites = new SitesInGriddedRegion(eggr);

    griddedRegionSites.addSiteParams(attenRel.getSiteParamsIterator());
    griddedRegionSites.setSameSiteParams();
  }

  /**
   * Initialise the IMT gui Bean
   */
  private void initIMTGuiBean(){
    // set the im as PGA
    attenRel.setIntensityMeasure(PGA_Param.NAME);
  }

  /**
   * Initialize the IMR Gui Bean
   */
  private void initIMRGuiBean() {
    // make the imr
    attenRel = new BJF_1997_AttenRel(this);

    attenRel.getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_1SIDED);
    attenRel.getParameter(SigmaTruncLevelParam.NAME).setValue(new Double(3.0));
    attenRel.getParameter(ComponentParam.NAME).setValue(ComponentParam.COMPONENT_RANDOM_HORZ);
    attenRel.getParameter(StdDevTypeParam.NAME).setValue(StdDevTypeParam.STD_DEV_TYPE_TOTAL);
    // set the vs30
    attenRel.getParameter(Vs30_Param.NAME).setValue(new Double(760.0));
  }

  /**
   * Initialize the ERF Gui Bean
   */
  private void initERFSelector_GuiBean() {
    try{
      forecast = new Frankel02_AdjustableEqkRupForecast();
      ParameterList forecastParamList = forecast.getAdjustableParameterList();
      forecastParamList.getParameter(Frankel02_AdjustableEqkRupForecast.FAULT_MODEL_NAME).setValue(Frankel02_AdjustableEqkRupForecast.FAULT_MODEL_FRANKEL);
      forecastParamList.getParameter(Frankel02_AdjustableEqkRupForecast.BACK_SEIS_NAME).setValue(Frankel02_AdjustableEqkRupForecast.BACK_SEIS_INCLUDE);
      forecastParamList.getParameter(Frankel02_AdjustableEqkRupForecast.BACK_SEIS_RUP_NAME).setValue(Frankel02_AdjustableEqkRupForecast.BACK_SEIS_RUP_FINITE);
      forecastParamList.getParameter(Frankel02_AdjustableEqkRupForecast.RUP_OFFSET_PARAM_NAME).setValue(new Double(Frankel02_AdjustableEqkRupForecast.RUP_OFFSET_PARAM_MIN));
    }catch(Exception e){
      e.printStackTrace();
    }
  }



  /**
   * Initialize the TimeSpan gui bean
   */
  private void initTimeSpanGuiBean() {}



   /**
    * Sets the hazard curve x-axis values (if user wants custom values x-axis values).
    * Note that what's passed in is not cloned (the y-axis values will get modified).
    * @param func
    */
   public double[] getX_ValuesForHazardCurve() {
     double [] pgaXVals = {0.0050,0.0070,0.0098,0.0137,0.0192,0.0269,0.0376,
       0.0527,0.0738,0.103,0.145,0.203,0.284,0.397,0.556,0.778,1.09,1.52,2.13};

     return pgaXVals;
   }



   /**
    * runs the stanalone application on the system and submit the job on grid
    */
   public void run(){

     try{
       forecast.updateForecast();
       HazardMapCalculatorOld calc = new HazardMapCalculatorOld();
       calc.getHazardMapCurves("frankel02_hazardmap_standalone",true,getX_ValuesForHazardCurve(),
                               griddedRegionSites,attenRel,forecast,getParametersInfo(),"niting@usc.edu");

     }catch(ParameterException ee){
       ee.printStackTrace();
       return;
     }
     catch(Exception ee){
       ee.printStackTrace();
       return;
     }

   }


   /**
    * Returns the metadata associated with this calculation
    *
    * @returns the String containing the values selected for different parameters
    */
   public String getParametersInfo() {
     return "IMR Param List:\n"+
         "---------------\n"+
         "IMR = Boore, Joyner & Fumal (1997); Gaussian Truncation = 1 Sided; Truncation Level = 3.0; Component = Random Horizontal; Std Dev Type = Total\n"+
         "\nRegion Param List: \n"+
         "----------------\n"+
         "Min Longitude = -119.5; Max Longitude = -117.0; Min  Latitude = 33.5; Max  Latitude = 34.7; Grid Spacing = 0.1; Set Site Params = Apply same site parameter(s) to all locations; Vs30 = 760.0\n"+
         "\nIMT Param List: \n"+
         "---------------\n"+
         "IMT = PGA\n"+
         "\nForecast Param List:\n"+
         "--------------------\n"+
         "Eqk Rup Forecast = USGS/CGS 2002 Adj. Cal. ERF; Fault Model = Frankel's; Rupture Offset = 1.0; Background Seismicity = Include; Treat Background Seismicity As = Finite Sources\n"+
         "\nTimeSpan Param List:\n"+
         "--------------------\n"+
         "Duration = 50.0\n"+
         "\n Control Panel \n"+
         "-------------------\n"+
         "Site Source distance Control Panel =200kms\n"+
         "\nX Values Control panel\n"+
         "----------------------\n"+
         "0.0050,0.0070,0.0098,0.0137,0.0192,0.0269,0.0376,0.0527,0.0738,0.103,0.145,0.203,0.284,0.397,0.556,0.778,1.09,1.52,2.13\n";

   }


 /**
 *  Function that must be implemented by all Listeners for
 *  ParameterChangeWarnEvents.
 *
 * @param  event  The Event which triggered this function call
 */
 public void parameterChangeWarning( ParameterChangeWarningEvent e ){

   String S =  " : parameterChangeWarning(): ";

   WarningParameterAPI param = e.getWarningParameter();

   //System.out.println(b);
   param.setValueIgnoreWarning(e.getNewValue());

 }

}
