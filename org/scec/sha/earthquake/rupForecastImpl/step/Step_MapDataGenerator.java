package org.scec.sha.earthquake.rupForecastImpl.step;

import javax.swing.JOptionPane;
import java.util.Calendar;

import org.scec.sha.earthquake.rupForecastImpl.step.STEP_EqkRupForecast;
import org.scec.sha.imr.attenRelImpl.ShakeMap_2003_AttenRel;
import org.scec.param.event.ParameterChangeWarningListener;
import org.scec.param.event.ParameterChangeWarningEvent;
import org.scec.param.WarningParameterAPI;
import org.scec.data.region.SitesInGriddedRegion;
import org.scec.sha.calc.HazardMapCalculator;

/**
 * <p>Title: Step_MapDataGenerator </p>
 * <p>Description: It reads STEP file every day and creates the data set </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class Step_MapDataGenerator implements ParameterChangeWarningListener{

  // flag for debug purposes
  private final static boolean D = false;
  private final  static String C = "Step_MapDataGenerator";

  // make a array for saving the X values
  private  double [] xValues = { .001, .01, .05, .1, .15, .2, .25, .3, .4, .5,
                               .6, .7, .8, .9, 1, 1.1, 1.2, 1.3, 1.4, 1.5}  ;

  // VS 30 value to be set in the IMR
  private final Double VS_30= new Double(760);

  // min/max lat/lon to be used
  private double minLat = 32.5;
  private double maxLat = 36.5;
  private double minLon = -121.5;
  private double maxLon= -115.0;
  private double gridSpacing= 0.1;

  public Step_MapDataGenerator() {
    // make the forecast
    STEP_EqkRupForecast forecast = new STEP_EqkRupForecast();
    forecast.updateForecast();
    // make the imr
    ShakeMap_2003_AttenRel imr = new ShakeMap_2003_AttenRel(this);
    // set the im as PGA
    imr.setIntensityMeasure(imr.PGA_NAME);
    // set the vs30
    imr.getParameter(imr.VS30_NAME).setValue(VS_30);
    //make the Gridded Region object
    SitesInGriddedRegion region = new SitesInGriddedRegion(minLat, maxLat, minLon,
        maxLon, gridSpacing);
    region.addSiteParams(imr.getSiteParamsIterator());

    // make the calculator
    HazardMapCalculator calc = new HazardMapCalculator();
    calc.showProgressBar(false);
    Calendar calendar = Calendar.getInstance();
    // name of new directory
    String newDir = new String("STEP::"+(calendar.get(Calendar.MONTH)+1)+"-"+
                               calendar.get(Calendar.DAY_OF_MONTH)+"-"+
                               calendar.get(Calendar.YEAR)+"("+
                               calendar.get(Calendar.HOUR_OF_DAY)+":"+
                               calendar.get(Calendar.MINUTE)+":"+
                               calendar.get(Calendar.SECOND)+")");

    calc.getHazardMapCurves("/export/home/scec-00/scecweb/jsdk2.1/HazardMapDataSets/"+newDir,
                            true, xValues, region, imr, forecast, newDir +
                            forecast.toString() + imr.toString());

  }

  public static void main(String[] args) {
    Step_MapDataGenerator step = new Step_MapDataGenerator();
  }

  /**
    *  Function that must be implemented by all Listeners for
    *  ParameterChangeWarnEvents.
    *
    * @param  event  The Event which triggered this function call
    */
   public void parameterChangeWarning( ParameterChangeWarningEvent e ){

     String S = C + " : parameterChangeWarning(): ";
     if(D) System.out.println(S + "Starting");
     WarningParameterAPI param = e.getWarningParameter();

     //System.out.println(b);
     param.setValueIgnoreWarning(e.getNewValue());

     if(D) System.out.println(S + "Ending");
   }
}