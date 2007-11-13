/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.MeanUCERF;

import java.io.File;
import java.io.FileWriter;

import org.opensha.data.Location;
import org.opensha.data.Site;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.DiscretizedFuncAPI;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.param.DoubleParameter;
import org.opensha.param.WarningParameterAPI;
import org.opensha.param.event.ParameterChangeWarningEvent;
import org.opensha.param.event.ParameterChangeWarningListener;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.AttenuationRelationshipAPI;
import org.opensha.sha.imr.attenRelImpl.CB_2006_AttenRel;

/**
 * This class creates a bunch of hazard curves using MeanUCERF2 
 * for verification with NSHMP
 * 
 * @author vipingupta
 *
 */
public class HazardCurvesVerificationApp implements ParameterChangeWarningListener {
	private final static String HAZ_CURVES_DIRECTORY_NAME = "org/opensha/sha/earthquake/rupForecastImpl/MeanUCERF/HazardCurvesVerification";
	private final static DoubleParameter VS_30_PARAM = new DoubleParameter("Vs30", 760.0);
	private final static DoubleParameter DEPTH_2_5KM_PARAM = new DoubleParameter("Depth 2.5 km/sec", 1.0);
	private MeanUCERF2 meanUCERF2;
	private AttenuationRelationshipAPI imr;
	private ArbitrarilyDiscretizedFunc function; // X-Values function
	private HazardCurveCalculator hazardCurveCalculator;
	
	// First Lat profiling
	private final static double LAT1 = 34.0;
	private final static double MIN_LON1 = -119.0;
	private final static double MAX_LON1 = -115.0;
	
	// Second Lat profiling
	private final static double LAT2 = 37.7;
	private final static double MIN_LON2 = -123.0;
	private final static double MAX_LON2 = -120.0;
	
	// grid spacing in degrees
	private final static double GRID_SPACING = 0.05;

	public HazardCurvesVerificationApp() {
		System.out.println("Setting up ERF...");
		setupERF();
		System.out.println("Setting up IMR...");
		setupIMR();
		
		// Generate Hazard Curves for PGA
		imr.setIntensityMeasure("PGA");
		createUSGS_PGA_Function();
		String dirName = HAZ_CURVES_DIRECTORY_NAME+"/PGA";
		generateHazardCurves(dirName, LAT1, MIN_LON1, MIN_LON1);
		generateHazardCurves(dirName, LAT2, MIN_LON2, MIN_LON2);
		
		// Generate Hazard Curves for SA 0.2s
		imr.setIntensityMeasure("SA");
		createUSGS_SA_01_AND_02_Function();
		imr.getParameter(AttenuationRelationship.PERIOD_NAME).setValue(0.2);
		dirName = HAZ_CURVES_DIRECTORY_NAME+"/SA_0.2sec";
		generateHazardCurves(dirName, LAT1, MIN_LON1, MIN_LON1);
		generateHazardCurves(dirName, LAT2, MIN_LON2, MIN_LON2);
		
		// Generate hazard curves for SA 1.0s
		imr.setIntensityMeasure("SA");
		imr.getParameter(AttenuationRelationship.PERIOD_NAME).setValue(1.0);
		createUSGS_SA_Function();
		dirName = HAZ_CURVES_DIRECTORY_NAME+"/SA_1sec";
		generateHazardCurves(dirName, LAT1, MIN_LON1, MIN_LON1);
		generateHazardCurves(dirName, LAT2, MIN_LON2, MIN_LON2);
	}
	
	/**
	 * Generate hazard curves for a bunch of sites
	 *
	 */
	private void generateHazardCurves(String dirName, double lat, double minLon, double maxLon) {
		//		create directory for hazard curves
		File file = new File(dirName);
		if(!file.isDirectory()) file.mkdirs();
		try {
			hazardCurveCalculator = new HazardCurveCalculator();
		
			// Do for First Lat
			for(double lon=minLon; lon<=maxLon; lon+=GRID_SPACING) {
				String fileName = dirName+"/"+lat+"_"+lon+".txt";
				System.out.println("Generating file:"+fileName);
				Site site = new Site(new Location(lat, lon));
				site.addParameter(VS_30_PARAM);
				site.addParameter(DEPTH_2_5KM_PARAM);
				DiscretizedFuncAPI hazFunc = this.function.deepClone();
				this.hazardCurveCalculator.getHazardCurve(hazFunc, site, imr, meanUCERF2);
				FileWriter fw = new FileWriter(fileName);
				for(int i=0; i<hazFunc.getNum(); ++i)
					fw.write(hazFunc.getX(i)+"\t"+hazFunc.getY(i)+"\n");
				fw.close();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Set up ERF Parameters
	 *
	 */
	private void setupERF() {
		meanUCERF2 = new MeanUCERF2();
		meanUCERF2.setParameter(MeanUCERF2.RUP_OFFSET_PARAM_NAME, new Double(5.0));
		meanUCERF2.setParameter(MeanUCERF2.CYBERSHAKE_DDW_CORR_PARAM_NAME, false);
		meanUCERF2.setParameter(UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_POISSON);
		meanUCERF2.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
		meanUCERF2.setParameter(UCERF2.BACK_SEIS_RUP_NAME, UCERF2.BACK_SEIS_RUP_POINT);
		meanUCERF2.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME, UCERF2.CENTERED_DOWNDIP_FLOATER);
		meanUCERF2.getTimeSpan().setDuration(50.0);
		meanUCERF2.updateForecast();
	}
	
	/**
	 * Set up IMR parameters
	 *
	 */
	private void setupIMR() {
		imr = new CB_2006_AttenRel(this);
		imr.setParamDefaults(); // default is rock site
	}
	

	/**
	 *  Function that must be implemented by all Listeners for
	 *  ParameterChangeWarnEvents.
	 *
	 * @param  event  The Event which triggered this function call
	 */
	public void parameterChangeWarning(ParameterChangeWarningEvent e) {
		String S = " : parameterChangeWarning(): ";
		WarningParameterAPI param = e.getWarningParameter();
		param.setValueIgnoreWarning(e.getNewValue());
	}
	
	
	  /**
	   * initialises the function with the x and y values if the user has chosen the USGS-PGA X Vals
	   * the y values are modified with the values entered by the user
	   */
	  private void createUSGS_PGA_Function(){
	    function= new ArbitrarilyDiscretizedFunc();
	    function.set(.005,1);
	    function.set(.007,1);
	    function.set(.0098,1);
	    function.set(.0137,1);
	    function.set(.0192,1);
	    function.set(.0269,1);
	    function.set(.0376,1);
	    function.set(.0527,1);
	    function.set(.0738,1);
	    function.set(.103,1);
	    function.set(.145,1);
	    function.set(.203,1);
	    function.set(.284,1);
	    function.set(.397,1);
	    function.set(.556,1);
	    function.set(.778,1);
	    function.set(1.09,1);
	    function.set(1.52,1);
	    function.set(2.13,1);
	  }

	  
	  /**
	   * initialises the function with the x and y values if the user has chosen the USGS-PGA X Vals
	   * the y values are modified with the values entered by the user
	   */
	  private void createUSGS_SA_01_AND_02_Function(){
	    function= new ArbitrarilyDiscretizedFunc();
	                   
	    function.set(.005,1);
	    function.set(.0075,1);
	    function.set(.0113 ,1);
	    function.set(.0169,1);
	    function.set(.0253,1);
	    function.set(.0380,1);
	    function.set(.0570,1);
	    function.set(.0854,1);
	    function.set(.128,1);
	    function.set(.192,1);
	    function.set(.288,1);
	    function.set(.432,1);
	    function.set(.649,1);
	    function.set(.973,1);
	    function.set(1.46,1);
	    function.set(2.19,1);
	    function.set(3.28,1);
	    function.set(4.92,1);
	    function.set(7.38,1);
	    
	  }
	  
	  /**
	   * initialises the function with the x and y values if the user has chosen the USGS-PGA X Vals
	   * the y values are modified with the values entered by the user
	   */
	  private void createUSGS_SA_Function(){
	    function= new ArbitrarilyDiscretizedFunc();
	 
	    function.set(.0025,1);
	    function.set(.00375,1);
	    function.set(.00563 ,1);
	    function.set(.00844,1);
	    function.set(.0127,1);
	    function.set(.0190,1);
	    function.set(.0285,1);
	    function.set(.0427,1);
	    function.set(.0641,1);
	    function.set(.0961,1);
	    function.set(.144,1);
	    function.set(.216,1);
	    function.set(.324,1);
	    function.set(.487,1);
	    function.set(.730,1);
	    function.set(1.09,1);
	    function.set(1.64,1);
	    function.set(2.46,1);
	    function.set(3.69,1);
	    function.set(5.54,1);
	  }
	
	public static void main(String []args) {
		new HazardCurvesVerificationApp();
	}
}
