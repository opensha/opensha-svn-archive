package org.opensha.sha.earthquake.rupForecastImpl.YuccaMountain;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.opensha.data.Location;
import org.opensha.data.TimeSpan;
import org.opensha.data.region.EvenlyGriddedGeographicRegionAPI;
import org.opensha.data.region.EvenlyGriddedRectangularGeographicRegion;
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.param.DoubleParameter;
import org.opensha.param.StringParameter;
import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.FaultRuptureSource;
import org.opensha.sha.earthquake.rupForecastImpl.GriddedRegionPoissonEqkSource;
import org.opensha.sha.fault.FaultTrace;
import org.opensha.sha.magdist.GaussianMagFreqDist;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.surface.EvenlyGriddedSurface;
import org.opensha.sha.surface.StirlingGriddedSurface;
import org.opensha.util.FileUtils;

public class YuccaMountainERF extends EqkRupForecast{

	//for Debug purposes
	private static String  C = new String("YuccaMountainERF");
	private boolean D = true;
	 // name of this ERF
	public final static String NAME = new String("Yucca mountain Adj. ERF");

	
	private String FAULT_SOURCE_FILENAME = "org/opensha/sha/earthquake/rupForecastImpl/YuccaMountain/FAULTmodelYM.txt";
	private String BG_FILE_NAME = "org/opensha/sha/earthquake/rupForecastImpl/YuccaMountain/BACKGROUNDmodelYM.txt";
	private double MIN_MAG = 5.5;
	private double MAX_MAG = 8.5;
	private int NUM_MAGS = 31;
	
	private String FAULT_SOURCE_PARAM_NAME = "Select Source";
	private String FAULT_SOURCE_PARAM_INFO = "choose from the provided list of fault source for Yucca Mountain";
	private StringParameter sourceNameParam;
	
	private String FAULT_SOURCE_MEAN_MAG_PARAM_NAME = "Mean Mag.";
	private String FAULT_SOURCE_MEAN_MAG_PARAM_INFO = "Mean-Mag of the selected source name";
	private DoubleParameter meanMagParam;
	
	private String FAULT_SOURCE_MEAN_SIGMA_PARAM_NAME = "Sigma";
	private String FAULT_SOURCE_MEAN_SIGMA_PARAM_INFO = "Sigma of the selected source name";
	private DoubleParameter sigmaParam;
	
	
	public final static String BACK_SEIS_NAME = new String ("Background Seismicity");
	public final static String BACK_SEIS_INCLUDE = new String ("Include");
	public final static String BACK_SEIS_EXCLUDE = new String ("Exclude");
	// make the fault-model parameter
	ArrayList backSeisOptionsStrings = new ArrayList();
	StringParameter backSeisParam;
	
	
	ArrayList<String> sourceNames = new ArrayList<String>();
	ArrayList<IncrementalMagFreqDist> magDistList = new ArrayList<IncrementalMagFreqDist>();
	private ArrayList <FaultRuptureSource> faultsources = new ArrayList<FaultRuptureSource> ();
	private ArrayList bgSources;
	private ArrayList allSources;
	
	public YuccaMountainERF(){
		   // create the timespan object with start time and duration in years
	    timeSpan = new TimeSpan(TimeSpan.NONE,TimeSpan.YEARS);
	    timeSpan.addParameterChangeListener(this);
	    timeSpan.setDuration(50);
	    createFaultSources();
	    initAdjParams();

	}
	
	private void initAdjParams(){
		
		int size = faultsources.size();
		sourceNameParam= new StringParameter(FAULT_SOURCE_PARAM_NAME,sourceNames,sourceNames.get(0));
		sourceNameParam.setInfo(FAULT_SOURCE_PARAM_INFO);
		sourceNameParam.addParameterChangeListener(this);
		
		int rupSourceIndex = getSelectedFaultSourceName();
		
		GaussianMagFreqDist magDist = (GaussianMagFreqDist)magDistList.get(rupSourceIndex);
		meanMagParam = new DoubleParameter(FAULT_SOURCE_MEAN_MAG_PARAM_NAME,MIN_MAG,MAX_MAG,new Double(magDist.getMean()));
		meanMagParam.setInfo(FAULT_SOURCE_MEAN_MAG_PARAM_INFO);
		meanMagParam.addParameterChangeListener(this);
		
		sigmaParam = new DoubleParameter(FAULT_SOURCE_MEAN_SIGMA_PARAM_NAME,new Double(magDist.getStdDev()));
		sigmaParam.setInfo(FAULT_SOURCE_MEAN_SIGMA_PARAM_INFO);
		sigmaParam.addParameterChangeListener(this);
		
		backSeisOptionsStrings.add(BACK_SEIS_INCLUDE);
		backSeisOptionsStrings.add(BACK_SEIS_EXCLUDE);

		backSeisParam = new StringParameter(BACK_SEIS_NAME,backSeisOptionsStrings,BACK_SEIS_INCLUDE);
		backSeisParam.addParameterChangeListener(this);
		
		adjustableParams.addParameter(sourceNameParam);
		adjustableParams.addParameter(meanMagParam);
		adjustableParams.addParameter(sigmaParam);
		adjustableParams.addParameter(backSeisParam);
	}
	
	private int getSelectedFaultSourceName(){
		String sourceName = (String)sourceNameParam.getValue();
		//System.out.println("Selected RupSource Param Name= "+sourceName);
		int size = faultsources.size();
		for(int i=0;i<size;++i){
			FaultRuptureSource rupSource = faultsources.get(i);
			//System.out.println("source="+rupSource.getName());
			if(rupSource.getName().equals(sourceName))
				return i;
		}
		return -1;
	}
	
	
	private void createBackGroundSources(){
		bgSources = new ArrayList();
		try {
			ArrayList<String> fileLines = FileUtils.loadJarFile(BG_FILE_NAME);
			int size = fileLines.size();
			for(int i=4;i<size;++i){
				String sourceName = fileLines.get(i);
				if(sourceName.trim().equals(""))
					continue;
				StringTokenizer st = new StringTokenizer(sourceName);
				String srcCode = st.nextToken();
				int srcCodeLength = srcCode.length();
				String sourceNameString = sourceName.substring(srcCodeLength);
				++i;
				String magDistInfo  = fileLines.get(i);
				st = new StringTokenizer(magDistInfo);
				double aVal = Double.parseDouble(st.nextToken().trim());
				double uncertainity = Double.parseDouble(st.nextToken().trim());
				double bVal = Double.parseDouble(st.nextToken().trim());
				double sigma = Double.parseDouble(st.nextToken().trim());
				double minMag = Double.parseDouble(st.nextToken().trim());
				double maxMag = Double.parseDouble(st.nextToken().trim());
				int numMag = Integer.parseInt(st.nextToken().trim());
				double totCumRate = Double.parseDouble(st.nextToken().trim());
				IncrementalMagFreqDist magDist = new GutenbergRichterMagFreqDist(bVal,totCumRate,minMag,maxMag,numMag);
				++i;
				String regionInfo = fileLines.get(i);
				st = new StringTokenizer(regionInfo);
				double minLat = Double.parseDouble(st.nextToken().trim());
				double maxLat = Double.parseDouble(st.nextToken().trim());
				double minLon = Double.parseDouble(st.nextToken().trim());
				double maxLon = Double.parseDouble(st.nextToken().trim());
				double gridSpacing = Double.parseDouble(st.nextToken().trim());
				try {
					EvenlyGriddedGeographicRegionAPI region = new EvenlyGriddedRectangularGeographicRegion(minLat, 
							                                          maxLat, minLon, maxLon, gridSpacing);
					GriddedRegionPoissonEqkSource grSource = new GriddedRegionPoissonEqkSource(region,magDist,
							                                     timeSpan.getDuration(),-90.0,60,minMag);
					bgSources.add(grSource);
				} catch (RegionConstraintException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private void createFaultSources(){
	try {
		ArrayList<String> fileLines = FileUtils.loadJarFile(FAULT_SOURCE_FILENAME);
		int size = fileLines.size();
		for(int i=6;i<size;++i){
			String sourceName = fileLines.get(i);
			if(sourceName.trim().equals(""))
				continue;
			StringTokenizer st = new StringTokenizer(sourceName);
			String srcCode = st.nextToken();
			int srcCodeLength = srcCode.length();
			String sourceNameString = sourceName.substring(srcCodeLength);
			sourceNames.add(sourceNameString);
			++i;
			String sourceDipInfo = fileLines.get(i);
			st = new StringTokenizer(sourceDipInfo);
			double dip = Double.parseDouble(st.nextToken().trim());
			double strike = Double.parseDouble(st.nextToken().trim());
			double rake = Double.parseDouble(st.nextToken().trim());
			double upperSeis = Double.parseDouble(st.nextToken().trim());
			double lowerSeis = Double.parseDouble(st.nextToken().trim());
			++i;
			String sourceMFD = fileLines.get(i);
			st = new StringTokenizer(sourceMFD);
			double meanMag = Double.parseDouble(st.nextToken().trim());
			double sigma = Double.parseDouble(st.nextToken().trim());
			double seisMomentRate = Double.parseDouble(st.nextToken().trim());
			++i;
			int numSourceLocations = Integer.parseInt(fileLines.get(i));
			FaultTrace fltTrace = new FaultTrace(sourceNameString);
			int numLinesDone = i;
			for(i=i+1;i<=(numLinesDone+numSourceLocations);++i){
				String location = fileLines.get(i);
				st = new StringTokenizer(location);
				double lon = Double.parseDouble(st.nextToken().trim());
				double lat = Double.parseDouble(st.nextToken().trim());
				fltTrace.addLocation(new Location(lat,lon));
			}
			--i;
			EvenlyGriddedSurface surface = new StirlingGriddedSurface(fltTrace, dip,upperSeis,lowerSeis,1.0);
			IncrementalMagFreqDist magDist = new GaussianMagFreqDist(this.MIN_MAG,MAX_MAG,NUM_MAGS,meanMag,sigma,seisMomentRate);
			magDistList.add(magDist);
			FaultRuptureSource rupSource = new FaultRuptureSource(magDist,surface,rake,timeSpan.getDuration());
			rupSource.setName(sourceNameString);
			faultsources.add(rupSource);
		}
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  
	}

	  /**
	    *  This is the main function of this interface. Any time a control
	    *  paramater or independent paramater is changed by the user in a GUI this
	    *  function is called, and a paramater change event is passed in.
	    *
	    *  This sets the flag to indicate that the sources need to be updated
	    *
	    * @param  event
	    */
	   public void parameterChange(ParameterChangeEvent event) {
	     super.parameterChange(event);
	     String paramName = event.getParameterName();

	     /**
	      * If change is made to the Back Seis param then
	      * remove/add the backSeisParam from the list of
	      * adjustable parameters and send that event to
	      * listening class for the changes in the
	      * parameter list.
	      */
	     if(paramName.equals(FAULT_SOURCE_PARAM_NAME)){
	       int rupSourceIndex = getSelectedFaultSourceName();
	       GaussianMagFreqDist magDist = (GaussianMagFreqDist)magDistList.get(rupSourceIndex);
	       meanMagParam.setValue(magDist.getMean());
	       sigmaParam.setValue(magDist.getStdDev());
	     }
	  }
	
	@Override
	public int getNumSources() {
		return allSources.size();
	}

	@Override
	public ProbEqkSource getSource(int source) {
		// TODO Auto-generated method stub
		return (ProbEqkSource)allSources.get(source);
	}

	@Override
	public ArrayList getSourceList() {
		// TODO Auto-generated method stub
		return allSources;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return NAME;
	}

	/**
	 * Update the fault Sources with the change in duration.
	 */
	public void updateForecast() {
		// make sure something has changed
	     if(parameterChangeFlag) {
	    	 allSources = new ArrayList();
	    	 allSources.addAll(faultsources);
	    	 String bgVal = (String)backSeisParam.getValue();
	    	 if(bgVal.equals(BACK_SEIS_INCLUDE)){
	    		 createBackGroundSources();
	    		 allSources.addAll(bgSources);
	    	 }
	    	 int size = faultsources.size();
	    	 for(int i=0;i<size;++i){
	    		FaultRuptureSource rupSource = faultsources.get(i);
	    		rupSource.setDuration(timeSpan.getDuration());
	    	 }
	     }
	}

	
}
