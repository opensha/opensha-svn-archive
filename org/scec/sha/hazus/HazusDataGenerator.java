package org.scec.sha.hazus;

import org.scec.sha.imr.attenRelImpl.Campbell_1997_AttenRel;
import org.scec.param.event.*;
import org.scec.param.*;
import org.scec.sha.earthquake.rupForecastImpl.Frankel96.*;
import org.scec.data.region.SitesInGriddedRegion;
import org.scec.sha.calc.HazusMapCalculator;
import org.scec.sha.earthquake.*;
import org.scec.util.*;
import org.scec.sha.gui.infoTools.*;
import org.scec.data.function.*;

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
  private final static String PGA_DIR_NAME = "pga/";
  private final static String SA_1_DIR_NAME = "sa_1/";
  private final static String SA_DIR_NAME = "sa_.3/";
  private final static String PGV_DIR_NAME = "pgv/";

  private DefaultHazardCurveForIMTs defaultXVals = new DefaultHazardCurveForIMTs();

  public HazusDataGenerator() {

    Campbell_1997_AttenRel attenRel = new Campbell_1997_AttenRel(this);
    Frankel96_EqkRupForecast forecast = new Frankel96_EqkRupForecast();
    attenRel.setIntensityMeasure(attenRel.PGA_NAME);
    //((DoubleDiscreteParameter)attenRel.getParameter(attenRel.PERIOD_NAME)).setValue(new Double(0.3));
    //make the Gridded Region object
    SitesInGriddedRegion region = new SitesInGriddedRegion(MIN_LAT, MAX_LAT, MIN_LON,
        MAX_LON, GRID_SPACING);
    attenRel.getParameter(attenRel.SITE_TYPE_NAME).setValue(attenRel.SITE_TYPE_GEN_ROCK);
    region.addSiteParams(attenRel.getSiteParamsIterator());
    forecast.updateForecast();

    HazusMapCalculator calc = new HazusMapCalculator();
    calc.showProgressBar(false);
    String metaData = "For Hazus Values\n\n"+
                      "ERF: "+forecast.getName()+"\n"+
                      "IMR Name: "+attenRel.getName()+"\n"+
                      "\t"+"Site Name: "+ attenRel.SITE_TYPE_GEN_ROCK+"\n"+
                      "Region Info: "+
                      "\t MIN LAT: "+region.getMinLat()+" MAX LAT:"+region.getMaxLat()+
                      " MIN LON: "+region.getMinLon()+" MAX LON: "+region.getMaxLon()+
                      " Grid Spacing: "+region.getGridSpacing()+"\n";
    //doing ofr PGA
    ArbitrarilyDiscretizedFunc function = defaultXVals.getHazardCurve(attenRel.PGA_NAME);
    double[] xValues =new double[function.getNum()];
    for(int i=0;i<function.getNum();++i)
      xValues[i] = function.getX(i);
    calc.getHazardMapCurves(PGA_DIR_NAME,true,xValues,region,attenRel,forecast,metaData);

    //Doing for SA
    function = defaultXVals.getHazardCurve(attenRel.SA_NAME);
    xValues =new double[function.getNum()];
    for(int i=0;i<function.getNum();++i)
      xValues[i] = function.getX(i);
    attenRel.setIntensityMeasure(attenRel.SA_NAME);
    ((DoubleDiscreteParameter)attenRel.getParameter(attenRel.PERIOD_NAME)).setValue(new Double(0.3));
    calc.getHazardMapCurves(SA_DIR_NAME,true,xValues,region,attenRel,forecast,metaData);
    ((DoubleDiscreteParameter)attenRel.getParameter(attenRel.PERIOD_NAME)).setValue(new Double(1.0));
    calc.getHazardMapCurves(SA_1_DIR_NAME,true,xValues,region,attenRel,forecast,metaData);

    //Doing for PGV
    function = defaultXVals.getHazardCurve(attenRel.PGV_NAME);
    xValues =new double[function.getNum()];
    for(int i=0;i<function.getNum();++i)
      xValues[i] = function.getX(i);
    attenRel.setIntensityMeasure(attenRel.PGV_NAME);
    calc.getHazardMapCurves(PGV_DIR_NAME,true,xValues,region,attenRel,forecast,metaData);
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