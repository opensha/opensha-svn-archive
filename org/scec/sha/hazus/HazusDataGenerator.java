package org.scec.sha.hazus;

import org.scec.sha.imr.attenRelImpl.Campbell_1997_AttenRel;
import org.scec.param.event.*;
import org.scec.param.*;
import org.scec.sha.earthquake.rupForecastImpl.Frankel96.*;
import org.scec.data.region.SitesInGriddedRegion;
import org.scec.sha.calc.HazardMapCalculator;
import org.scec.sha.earthquake.*;
import org.scec.util.*;

/**
 * <p>Title: HazusDataGenerator</p>
 * <p>Description: This class generates the Hazard Curve data to be
 * provided as the input to the Hazus.</p>
 * @author : Edward (Ned) Field and Nitin Gupta
 * @version 1.0
 */

public class HazusDataGenerator implements ParameterChangeWarningListener{

  //grid region for the LA County provide by the Hazus
  private final double MIN_LAT=  33.705860;
  private final double MAX_LAT= 34.823168;
  private final double MIN_LON = -118.943793 ;
  private final double MAX_LON= -117.644716;
  private final double GRID_SPACING= .05;
  private final static String DIR_NAME = "hazus/";
  // make a array for saving the X values
  private  double [] xValues = { .001, .01, .05, .1, .15, .2, .25, .3, .4, .5,
    .6, .7, .8, .9, 1, 1.1, 1.2, 1.3, 1.4, 1.5}  ;
  private double [] pgvXValues = { 1, 15, 30, 45, 60, 75, 90, 105, 120, 135,
    150, 165, 180, 195, 210, 225, 240, 255, 270, 285, 300} ;
  public HazusDataGenerator() {

    Campbell_1997_AttenRel attenRel = new Campbell_1997_AttenRel(this);
    Frankel96_EqkRupForecast forecast = new Frankel96_EqkRupForecast();
    attenRel.setIntensityMeasure(attenRel.PGV_NAME);
    //((DoubleDiscreteParameter)attenRel.getParameter(attenRel.PERIOD_NAME)).setValue(new Double(1.0));
    //make the Gridded Region object
    SitesInGriddedRegion region = new SitesInGriddedRegion(MIN_LAT, MAX_LAT, MIN_LON,
        MAX_LON, GRID_SPACING);
    attenRel.getParameter(attenRel.SITE_TYPE_NAME).setValue(attenRel.SITE_TYPE_GEN_SOIL);
    region.addSiteParams(attenRel.getSiteParamsIterator());
    forecast.updateForecast();

    HazardMapCalculator calc = new HazardMapCalculator();
    calc.showProgressBar(false);
    String metaData = "For PGV Values\n\n"+
                      "ERF: "+forecast.getName()+"\n"+
                      "IMR Name: "+attenRel.getName()+"\n"+
                      "\t"+"Site Name: "+ attenRel.SITE_TYPE_GEN_ROCK+"\n"+
                      "Region Info: "+region.toString()+"\n";

    calc.getHazardMapCurves(DIR_NAME,true,pgvXValues,region,attenRel,forecast,metaData);

  }
  public static void main(String[] args) {
    HazusDataGenerator hazusDataGenerator1 = new HazusDataGenerator();
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