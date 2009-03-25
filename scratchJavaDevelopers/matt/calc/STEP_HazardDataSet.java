package scratchJavaDevelopers.matt.calc;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.StringTokenizer;

import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.Site;
import org.opensha.data.region.SitesInGriddedRectangularRegion;
import org.opensha.data.region.SitesInGriddedRegion;
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.param.ParameterAPI;
import org.opensha.param.WarningParameterAPI;
import org.opensha.param.event.ParameterChangeWarningEvent;
import org.opensha.param.event.ParameterChangeWarningListener;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.PointEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.step.STEP_BackSiesDataAdditionObject;
import org.opensha.sha.gui.infoTools.ConnectToCVM;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.AttenuationRelationshipAPI;
import org.opensha.sha.imr.attenRelImpl.ShakeMap_2003_AttenRel;
import org.opensha.sha.imr.attenRelImpl.BA_2006_AttenRel;
import org.opensha.sha.util.SiteTranslator;
import org.opensha.util.FileUtils;


public class STEP_HazardDataSet implements ParameterChangeWarningListener{


	private boolean willSiteClass = true;
	//private boolean willSiteClass = false;
	private AttenuationRelationship attenRel;
	private static final String STEP_BG_FILE_NAME = RegionDefaults.backgroundHazardPath;
	private static final String STEP_HAZARD_OUT_FILE_NAME = RegionDefaults.outputHazardPath;
	private static final double IML_VALUE = Math.log(0.126);
	private static final double SA_PERIOD = 1;
	private static final String STEP_AFTERSHOCK_OBJECT_FILE = RegionDefaults.STEP_AftershockObjectFile;
	private DecimalFormat locFormat = new DecimalFormat("0.0000");
	private STEP_main stepMain ;


	public STEP_HazardDataSet(boolean includeWillsSiteClass){
		this.willSiteClass = includeWillsSiteClass;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		STEP_HazardDataSet step = new STEP_HazardDataSet(false);
		//read the aftershock file
		/*try {
			ArrayList stepAftershockList = (ArrayList)FileUtils.loadFile(STEP_AFTERSHOCK_OBJECT_FILE);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		//while(true)
		step.runSTEP();
		System.out.println("STEP is finito!");

	}



	public void runSTEP(){
		//1. create step main
		runStepmain();
		System.out.println("STEP earthquake rates are done.");
		
		//2. 
		createShakeMapAttenRelInstance();

		//3.
		SitesInGriddedRectangularRegion region = getDefaultRegion();//

		
		//4. calc probability values
		double[] stepBothProbVals = calcStepProbValues(region);
		
		//5. output
		createFile(stepBothProbVals,region);
		//5.1. backup aftershocks
		ArrayList stepAftershockList= stepMain.getSTEP_AftershockForecastList();
		//saving the STEP_Aftershock list object to the file
		FileUtils.saveObjectInFile(STEP_AFTERSHOCK_OBJECT_FILE, stepAftershockList);
	}


	public void runStepmain() {
		stepMain = new STEP_main();
		//1. step main
		stepMain.calc_STEP();
		
	}

	public SitesInGriddedRectangularRegion getDefaultRegion() {
		try {
			//?? slightly different from the RegionDefaults, 32.5,42.2,-124.8,-112.4,0.1
			return new SitesInGriddedRectangularRegion(RegionDefaults.searchLatMin, RegionDefaults.searchLatMax,
					RegionDefaults.searchLongMin, RegionDefaults.searchLongMax,
					RegionDefaults.gridSpacing);
		} catch (RegionConstraintException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public double[] calcStepProbValues(SitesInGriddedRectangularRegion region ) {	
		region.addSiteParams(attenRel.getSiteParamsIterator());
		//getting the Attenuation Site Parameters Liat
		ListIterator it = attenRel.getSiteParamsIterator();
		//creating the list of default Site Parameters, so that site parameter values can be filled in
		//if Site params file does not provide any value to us for it.
		ArrayList defaultSiteParams = new ArrayList();
		SiteTranslator siteTrans= new SiteTranslator();
		while(it.hasNext()){
			//adding the clone of the site parameters to the list
			ParameterAPI tempParam = (ParameterAPI)((ParameterAPI)it.next()).clone();
			//getting the Site Param Value corresponding to the Will Site Class "DE" for the seleted IMR  from the SiteTranslator
			siteTrans.setParameterValue(tempParam, siteTrans.WILLS_DE, Double.NaN);
			defaultSiteParams.add(tempParam);
		}
		if(willSiteClass){
			region.setDefaultSiteParams(defaultSiteParams);
			region.setSiteParamsForRegionFromServlet(true);
		}

		double[] bgVals = getBGVals(region.getNumGridLocs(),STEP_BG_FILE_NAME);
		double[] probVal = this.getProbVals(attenRel, region, stepMain.getSourceList());
		//combining the backgound and Addon dataSet and wrinting the result to the file
		STEP_BackSiesDataAdditionObject addStepData = new STEP_BackSiesDataAdditionObject();
		return  addStepData.addDataSet(bgVals,probVal);

	}

	public void createShakeMapAttenRelInstance(){
		// make the imr
		//attenRel = new ShakeMap_2003_AttenRel(this);
		attenRel = new BA_2006_AttenRel(this);
		// set the im as PGA
		//attenRel.setIntensityMeasure(((ShakeMap_2003_AttenRel)attenRel).PGA_NAME);
		//attenRel.setIntensityMeasure(((ShakeMap_2003_AttenRel)attenRel).SA_NAME, SA_PERIOD);
		attenRel.setParamDefaults();
		attenRel.setIntensityMeasure(((BA_2006_AttenRel)attenRel).SA_NAME, SA_PERIOD);

	}
	//}



	/**
	 * craetes the output xyz files
	 * @param probVals : Probablity values ArrayList for each Lat and Lon
	 * @param fileName : File to create
	 */
	private void createFile(double[] probVals,SitesInGriddedRectangularRegion region){
		int size = probVals.length;
		LocationList locList = region.getGridLocationsList();
		int numLocations = locList.size();

		try{
			FileWriter fr = new FileWriter(STEP_HAZARD_OUT_FILE_NAME);
			for(int i=0;i<numLocations;++i){
				Location loc = locList.getLocationAt(i);
				// System.out.println("Size of the Prob ArrayList is:"+size);
				fr.write(locFormat.format(loc.getLatitude())+"    "+locFormat.format(loc.getLongitude())+"      "+convertToProb(probVals[i])+"\n");
			}
			fr.close();
		}catch(IOException ee){
			ee.printStackTrace();
		}
	}

	private double convertToProb(double rate){
		return (1-Math.exp(-1*rate*RegionDefaults.forecastLengthDays));
	}

	/**
	 * returns the prob for the file( fileName)
	 * 
	 * @param fileName : Name of the file from which we collect the values
	 */
	public double[] getBGVals(int numSites,String fileName){
		double[] vals = new double[numSites];
		try{
			ArrayList fileLines = FileUtils.loadFile(fileName);
			ListIterator it = fileLines.listIterator();
			int i=0;
			while(it.hasNext()){
				StringTokenizer st = new StringTokenizer((String)it.next());
				st.nextToken();
				st.nextToken();
				String val =st.nextToken().trim();
				double temp =0;
				if(!val.equalsIgnoreCase("NaN")){
					temp=(new Double(val)).doubleValue();
					vals[i++] = convertToRate(temp);
				}
				else{
					temp=(new Double(Double.NaN)).doubleValue();
					vals[i++] = convertToRate(temp);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return vals;
	}


	private double convertToRate(double prob){
		return (-1*Math.log(1-prob)/RegionDefaults.forecastLengthDays);
	}
	/**
	 * HazardCurve Calculator for the STEP
	 * @param imr : ShakeMap_2003_AttenRel for the STEP Calculation
	 * @param region
	 * @param eqkRupForecast : STEP Forecast
	 * @returns the ArrayList of Probability values for the given region
	 */
	public double[] getProbVals(AttenuationRelationship imr,SitesInGriddedRectangularRegion region,
			ArrayList sourceList){

		double[] probVals = new double[region.getNumGridLocs()];
		double MAX_DISTANCE = 500;

		// declare some varibles used in the calculation
		double qkProb, distance;
		int k,i;
		try{
			// get total number of sources
			int numSources = sourceList.size();

			// this boolean will tell us whether a source was actually used
			// (e.g., all could be outside MAX_DISTANCE)
			boolean sourceUsed = false;

			int numSites = region.getNumGridLocs();
			int numSourcesSkipped =0;
			long startCalcTime = System.currentTimeMillis();


			for(int j=0;j<numSites;++j){
				double hazVal =1;
				double condProb =0;
				Site site = region.getSite(j);
				imr.setSite(site);
				//adding the wills site class value for each site
				// String willSiteClass = willSiteClassVals[j];
				//only add the wills value if we have a value available for that site else leave default "D"
				//if(!willSiteClass.equals("NA"))
				//imr.getSite().getParameter(imr.WILLS_SITE_NAME).setValue(willSiteClass);
				//else
				// imr.getSite().getParameter(imr.WILLS_SITE_NAME).setValue(imr.WILLS_SITE_D);

				// loop over sources
				for(i=0;i < numSources ;i++) {

					// get the ith source
					ProbEqkSource source = (ProbEqkSource)sourceList.get(i);

					// compute it's distance from the site and skip if it's too far away
					distance = source.getMinDistance(region.getSite(j));
					if(distance > MAX_DISTANCE){
						++numSourcesSkipped;
						//update progress bar for skipped ruptures
						continue;
					}

					// indicate that a source has been used
					sourceUsed = true;
					hazVal *= (1.0 - imr.getTotExceedProbability((PointEqkSource)source,IML_VALUE));
				}

				// finalize the hazard function
				if(sourceUsed) {
					//System.out.println("HazVal:"+hazVal);
					hazVal = 1-hazVal;
				}
				else
					hazVal = 0.0;
				//System.out.println("HazVal: "+hazVal);

				probVals[j]=this.convertToRate(hazVal);
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return probVals;
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

	public STEP_main getStepMain() {
		return stepMain;
	}

	public void setStepMain(STEP_main stepMain) {
		this.stepMain = stepMain;
	}

	public AttenuationRelationship getAttenRel() {
		return attenRel;
	}  
	
	
}
