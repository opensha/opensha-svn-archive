package org.scec.sha.earthquake.rupForecastImpl.step;

import java.io.*;
import java.net.*;
import java.util.*;
import org.scec.sha.earthquake.rupForecastImpl.step.*;
import org.scec.sha.earthquake.rupForecastImpl.step.STEP_EqkRupForecast;
import org.scec.sha.imr.attenRelImpl.ShakeMap_2003_AttenRel;
import org.scec.param.event.ParameterChangeWarningListener;
import org.scec.param.event.ParameterChangeWarningEvent;
import org.scec.param.WarningParameterAPI;
import org.scec.data.region.SitesInGriddedRegion;
import org.scec.sha.calc.HazardMapCalculator;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class STEP_DataSetGenerator implements ParameterChangeWarningListener{


  private final static String DELTA_RATES_FILE_NAME = "http://www.relm.org/models/step/SoCalDeltaRates.txt";
  // make a array for saving the X values
  private  double [] xValues = { .001, .01, .05, .1, .15, .2, .25, .3, .4, .5,
    .6, .7, .8, .9, 1, 1.1, 1.2, 1.3, 1.4, 1.5}  ;

  // VS 30 value to be set in the IMR
  private final Double VS_30= new Double(760);

  private final double MIN_LAT= 32.45;
  private final double MAX_LAT= 36.6;
  private final double MIN_LON = -121.45 ;
  private final double MAX_LON= -114.5;
  private final double GRID_SPACING= .05;
  private static final String BACKGROUND_STEP_DIR ="backGround/";
  private static final String STEP_DIR = "step/";


  public STEP_DataSetGenerator() {
    // make the forecast
    STEP_EqkRupForecast forecast=null;
    try{
      forecast = new STEP_EqkRupForecast();
    }catch(Exception e){
      System.out.println("No internet connection available");
    }

    // make the imr
    ShakeMap_2003_AttenRel imr = new ShakeMap_2003_AttenRel(this);
    // set the im as PGA
    imr.setIntensityMeasure(imr.PGA_NAME);
    // set the vs30
    imr.getParameter(imr.VS30_NAME).setValue(VS_30);
    //make the Gridded Region object
    SitesInGriddedRegion region = new SitesInGriddedRegion(MIN_LAT, MAX_LAT, MIN_LON,
        MAX_LON, GRID_SPACING);
    region.addSiteParams(imr.getSiteParamsIterator());

    // make the calculator
    HazardMapCalculator calc = new HazardMapCalculator();
    calc.showProgressBar(false);
    File file = new File(this.STEP_DIR);
    if(!file.isDirectory()){
      boolean success = (new File(STEP_DIR)).mkdir();
    }
    File backFile = new File(STEP_DIR+BACKGROUND_STEP_DIR);
    String metadata = "IMR Info: \n"+
                      "\t"+"Name: "+imr.getName()+"\n"+
                      "\t"+"Intensity Measure Type: "+ imr.getIntensityMeasure().getName()+"\n"+
                      "\n\n"+
                      "Region Info: \n"+
                      "\t"+"MinLat: "+region.getMinLat()+"\n"+
                      "\t"+"MaxLat: "+region.getMaxLat()+"\n"+
                      "\t"+"MinLon: "+region.getMinLon()+"\n"+
                      "\t"+"MaxLon: "+region.getMaxLon()+"\n"+
                      "\t"+"GridSpacing: "+region.getGridSpacing()+"\n"+
                      "\t"+"Site Params: "+imr.getParameter(imr.VS30_NAME).getName()+ " = "+imr.getParameter(imr.VS30_NAME).getValue().toString()+"\n"+
                      "\n\n"+
                      "Forecast Info: \n"+
                      "\t"+"Name: "+forecast.getName()+"\n";
    if(!backFile.isDirectory()){
      String dataInfo = "Step Back Ground DataSet\n\n"+metadata;
      forecast.getParameter(forecast.SEIS_TYPE_NAME).setValue(forecast.SEIS_TYPE_BACKGROUND);
      forecast.updateForecast();
      calc.getHazardMapCurves(STEP_DIR+BACKGROUND_STEP_DIR,
                              true, xValues, region, imr, forecast, dataInfo);
    }

    String dataInfo = "Step Addon Data Set for :\n"+
               "\t"+this.getSTEP_DateTimeInfo()+"\n\n"+
               metadata;
    forecast.getParameter(forecast.SEIS_TYPE_NAME).setValue(forecast.SEIS_TYPE_ADD_ON);
    forecast.updateForecast();
    String stepAddonDirName = STEP_DIR+this.getStepDirName()+"_Addon";
    calc.getHazardMapCurves(stepAddonDirName,
                            true, xValues, region, imr, forecast, dataInfo );

    STEP_BackSiesDataAdditionObject addStepData = new STEP_BackSiesDataAdditionObject();
    String stepBothDirName = STEP_DIR+this.getStepDirName()+"_Both";
    addStepData.addDataSet(STEP_DIR+this.BACKGROUND_STEP_DIR,stepAddonDirName,stepBothDirName);
    try{
      FileWriter fr = new FileWriter(stepBothDirName+"/metadata.dat");
      dataInfo = "Step Combined(Added) Data Set for :\n"+
               "\t"+this.getSTEP_DateTimeInfo()+"\n\n"+
               metadata;
      fr.write(dataInfo);
      fr.close();
      fr=new FileWriter(stepBothDirName+"/sites.dat");
      fr.write(region.getMinLat()+" "+region.getMaxLat()+" "+
               region.getGridSpacing()+"\n"+region.getMinLon()+" "+
               region.getMaxLon()+" "+ region.getGridSpacing()+"\n");
      fr.close();
    }catch(IOException ee){
      ee.printStackTrace();
    }

  }

  /**
   * generates the output directories for the step with timestamp labelling.
   * @return
   */
  private String getStepDirName(){
    String str =getSTEP_DateTimeInfo().replace(' ','_');
    return str;
  }

  public static void main(String[] args) {
    STEP_DataSetGenerator stepDataGenerator = new STEP_DataSetGenerator();
  }

  /**
   * reads the first line from the STEP Delta rates file on thw website to get its
   * data and time info
   * @return
   */
  private String getSTEP_DateTimeInfo(){
    try{
      URL url = new URL(this.DELTA_RATES_FILE_NAME);
      URLConnection uc = url.openConnection();
      BufferedReader tis =
          new BufferedReader(new InputStreamReader((InputStream) uc.getContent()));
      String str = tis.readLine();
      return str;
    }catch(Exception e){
      System.out.println("No Internet Connection");
    }
    return null;
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

