package org.opensha.sha.hazus;

import java.util.ArrayList;
import java.util.ListIterator;

import org.opensha.sha.imr.attenRelImpl.*;
import org.opensha.sha.imr.*;
import org.opensha.sha.util.SiteTranslator;
import org.opensha.param.event.*;
import org.opensha.param.*;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.*;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF1.WGCEP_UCERF1_EqkRupForecast;
import org.opensha.data.region.SitesInGriddedRectangularRegion;
import org.opensha.sha.calc.HazusMapCalculator;
import org.opensha.sha.earthquake.*;
import org.opensha.util.*;
import org.opensha.sha.gui.infoTools.*;
import org.opensha.data.function.*;
import org.opensha.exceptions.RegionConstraintException;

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
  private final double GRID_SPACING= 0.05;
  private final static String PGA_DIR_NAME = "pga/";
  private final static String SA_1_DIR_NAME = "sa_1/";
  private final static String SA_DIR_NAME = "sa_.3/";
  private final static String PGV_DIR_NAME = "pgv/";


  private Frankel02_AdjustableEqkRupForecast forecast;
  private USGS_Combined_2004_AttenRel attenRel;
  private SitesInGriddedRectangularRegion region;

  private IMT_Info defaultXVals = new IMT_Info();

  public HazusDataGenerator() throws RegionConstraintException {

	createAttenRel_Instance();
	createERF_Instance();
    attenRel.setIntensityMeasure(attenRel.PGA_NAME);
    createRegion();
    getSiteParamsForRegion();
    HazusMapCalculator calc = new HazusMapCalculator();
    calc.showProgressBar(false);
    String metaData = "Hazus Run 3(b) for the finer Grid spacing of 0.05km with Soil Effects with Background:\n"+
    	                "\n"+
                      "ERF: "+forecast.getName()+"\n"+
                      "IMR Name: "+attenRel.getName()+"\n"+
                      "\t"+"Site Name: "+ attenRel.VS30_NAME+"\n"+
                      "Region Info: "+
                      "\t MIN LAT: "+region.getMinLat()+" MAX LAT:"+region.getMaxLat()+
                      " MIN LON: "+region.getMinLon()+" MAX LON: "+region.getMaxLon()+
                      " Grid Spacing: "+region.getGridSpacing()+"\n";
    //doing ofr PGA
    ArbitrarilyDiscretizedFunc function = defaultXVals.getDefaultHazardCurve(attenRel.PGA_NAME);
    double[] xValues =new double[function.getNum()];
    for(int i=0;i<function.getNum();++i)
      xValues[i] = function.getX(i);
    calc.getHazardMapCurves(PGA_DIR_NAME,region,attenRel,forecast,metaData);

    //Doing for SA
    function = defaultXVals.getDefaultHazardCurve(attenRel.SA_NAME);
    xValues =new double[function.getNum()];
    for(int i=0;i<function.getNum();++i)
      xValues[i] = function.getX(i);
    attenRel.setIntensityMeasure(attenRel.SA_NAME);
    ((DoubleDiscreteParameter)attenRel.getParameter(attenRel.PERIOD_NAME)).setValue(new Double(0.3));
    calc.getHazardMapCurves(SA_DIR_NAME,region,attenRel,forecast,metaData);
    ((DoubleDiscreteParameter)attenRel.getParameter(attenRel.PERIOD_NAME)).setValue(new Double(1.0));
    calc.getHazardMapCurves(SA_1_DIR_NAME,region,attenRel,forecast,metaData);

    //Doing for PGV
    function = defaultXVals.getDefaultHazardCurve(attenRel.PGV_NAME);
    xValues =new double[function.getNum()];
    for(int i=0;i<function.getNum();++i)
      xValues[i] = function.getX(i);
    attenRel.setIntensityMeasure(attenRel.PGV_NAME);
    calc.getHazardMapCurves(PGV_DIR_NAME,region,attenRel,forecast,metaData);
  }


  public static void main(String[] args) {
    try {
      HazusDataGenerator hazusDataGenerator1 = new HazusDataGenerator();
      HazusIML_FileGenerator hazusIML_FileGenerator1 = new HazusIML_FileGenerator();
    }
    catch (RegionConstraintException ex) {
      System.out.println(ex.getMessage());
      System.exit(0);
    }
  }



  /**
   * Gets the wills site class for the given sites
   */
  private void getSiteParamsForRegion() {
    region.addSiteParams(attenRel.getSiteParamsIterator());
    //getting Wills Site Class
    region.setSiteParamsForRegionFromServlet(true);
    //getting the Attenuation Site Parameters Liat
    ListIterator it = attenRel.getSiteParamsIterator();
    //creating the list of default Site Parameters, so that site parameter values can be filled in
    //if Site params file does not provide any value to us for it.
    ArrayList defaultSiteParams = new ArrayList();
    SiteTranslator siteTrans = new SiteTranslator();
    while (it.hasNext()) {
      //adding the clone of the site parameters to the list
      ParameterAPI tempParam = (ParameterAPI) ( (ParameterAPI) it.next()).clone();
      //getting the Site Param Value corresponding to the default Wills site class selected by the user
      // for the seleted IMR  from the SiteTranslator
      siteTrans.setParameterValue(tempParam, SiteTranslator.WILLS_BC, Double.NaN);
      defaultSiteParams.add(tempParam);
    }
    region.setDefaultSiteParams(defaultSiteParams);
  }

  private void createERF_Instance(){
	   forecast = new Frankel02_AdjustableEqkRupForecast();
	   forecast.getAdjustableParameterList().getParameter(Frankel02_AdjustableEqkRupForecast.
	        BACK_SEIS_NAME).setValue(Frankel02_AdjustableEqkRupForecast.BACK_SEIS_EXCLUDE);
	   forecast.getTimeSpan().setDuration(50.0);
	  /* forecast.getAdjustableParameterList().getParameter(
               WGCEP_UCERF1_EqkRupForecast.BACK_SEIS_NAME).setValue(WGCEP_UCERF1_EqkRupForecast.
                                        BACK_SEIS_EXCLUDE);*/
	   forecast.getAdjustableParameterList().getParameter(
	                WGCEP_UCERF1_EqkRupForecast.BACK_SEIS_NAME).setValue(WGCEP_UCERF1_EqkRupForecast.
	                                         BACK_SEIS_INCLUDE);
	   forecast.getAdjustableParameterList().getParameter(
	                WGCEP_UCERF1_EqkRupForecast.BACK_SEIS_RUP_NAME).setValue(
	                    WGCEP_UCERF1_EqkRupForecast.BACK_SEIS_RUP_FINITE);
	   forecast.updateForecast();
  }


  private void createAttenRel_Instance(){
	  attenRel = new USGS_Combined_2004_AttenRel(this);
	  //attenRel.getParameter(attenRel.VS30_NAME).setValue(new Double(760));
	  attenRel.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).
	  setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED);
	  attenRel.getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME).
	  setValue(new Double(3.0));
	  attenRel.getParameter(AttenuationRelationship.COMPONENT_NAME).
	  setValue(USGS_Combined_2004_AttenRel.COMPONENT_AVE_HORZ);
  }


 private void createRegion() throws RegionConstraintException{
	 //	make the Gridded Region object
	 region = new SitesInGriddedRectangularRegion(MIN_LAT, MAX_LAT, MIN_LON,
	        MAX_LON, GRID_SPACING);
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
