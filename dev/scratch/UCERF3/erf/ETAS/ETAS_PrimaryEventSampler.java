package scratch.UCERF3.erf.ETAS;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.BorderType;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.LocationVector;
import org.opensha.commons.geo.Region;
import org.opensha.commons.mapping.gmt.GMT_MapGenerator;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.mapping.gmt.gui.GMT_MapGuiBean;
import org.opensha.commons.param.impl.CPTParameter;
import org.opensha.commons.util.FileUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.AbstractNthRupERF;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.earthquake.param.AleatoryMagAreaStdDevParam;
import org.opensha.sha.earthquake.param.ApplyGardnerKnopoffAftershockFilterParam;
import org.opensha.sha.earthquake.param.BPTAveragingTypeOptions;
import org.opensha.sha.earthquake.param.BPTAveragingTypeParam;
import org.opensha.sha.earthquake.param.BackgroundRupParam;
import org.opensha.sha.earthquake.param.BackgroundRupType;
import org.opensha.sha.earthquake.param.HistoricOpenIntervalParam;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;
import org.opensha.sha.earthquake.param.MagDependentAperiodicityOptions;
import org.opensha.sha.earthquake.param.MagDependentAperiodicityParam;
import org.opensha.sha.earthquake.param.ProbabilityModelOptions;
import org.opensha.sha.earthquake.param.ProbabilityModelParam;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.sha.gui.infoTools.ImageViewerWindow;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.analysis.FaultSystemSolutionCalc;
import scratch.UCERF3.analysis.GMT_CA_Maps;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.erf.FaultSystemSolutionPoissonERF;
import scratch.UCERF3.erf.UCERF2_Mapped.UCERF2_FM2pt1_FaultSysSolTimeDepERF;
import scratch.UCERF3.erf.utils.ProbabilityModelsCalc;
import scratch.UCERF3.griddedSeismicity.FaultPolyMgr;
import scratch.UCERF3.griddedSeismicity.GridSourceProvider;
import scratch.UCERF3.inversion.InversionFaultSystemRupSet;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.utils.MatrixIO;
import scratch.UCERF3.utils.RELM_RegionUtils;

/**
 * This class divides the supplied gridded region (and specified depth extent) into cubes, and computes
 * long-term rates of events for each cube, as well as the sources (and their fractions) that nucleate in
 * each cube.  The cube locations are defined by their centers.  For sampling aftershocks, parent ruptures
 * are assumed to be located at the intersecting corners of these cubes; thus, the number of parent locations
 * with depth is one greater than the number of cubes.
 * @author field
 *
 */
public class ETAS_PrimaryEventSampler {
	
	final static boolean D=ETAS_Simulator.D;
	
	String defaultFractSrcInCubeCacheFilename="dev/scratch/UCERF3/data/scratch/InversionSolutions/fractSrcInCubeCache";
	String defaultSrcInCubeCacheFilename="dev/scratch/UCERF3/data/scratch/InversionSolutions/srcInCubeCache";
	
	boolean applyGR_Corr = true;
	
	// these define the cubes in space
	int numCubeDepths, numCubesPerDepth, numCubes, numParDepths, numParLocsPerDepth, numParLocs;
	double maxDepth, depthDiscr;
	GriddedRegion origGriddedRegion;
	GriddedRegion gridRegForCubes; // the center of each cube in lat/lon space
	GriddedRegion gridRegForParentLocs;
	double cubeLatLonSpacing;
	
	AbstractNthRupERF erf;
	int numFltSystSources=-1, totNumSrc;
	ArrayList<Integer> srcIndexList; // this is so different lists can point to the same src index Integer object
	
	int numPtSrcSubPts;
	double pointSrcDiscr;
	
	// this is for each cube
	double[] latForCubeCenter, lonForCubeCenter, depthForCubeCenter;
	
	// this will hold the rate of each ERF source (which can be modified by external objects)
	double sourceRates[];
	
	// this stores the rates of erf ruptures that go unassigned (outside the region here)
	double rateUnassigned;
	
	double totRate;
	
	List<float[]> fractionSrcInCubeList;
	List<int[]> srcInCubeList;
	
	IntegerPDF_FunctionSampler cubeSamplerRatesOnly; 
	
	IntegerPDF_FunctionSampler[] cachedSamplers;
	Hashtable<Integer,Integer> numForthcomingEventsForParLocIndex;  // key is the parLocIndex and value is the number of events to process
//	int[] numForthcomingEventsAtParentLoc;
	int numCachedSamplers=0;
	int incrForReportingNumCachedSamplers=100;
	int nextNumCachedSamplers=incrForReportingNumCachedSamplers;
	
	double[] grCorrFactorForCellArray;

	// ETAS distance decay params
	double etasDistDecay, etasMinDist;
	ETAS_LocationWeightCalculator etas_LocWeightCalc;
	
	SummedMagFreqDist[] mfdForSrcArray;
	
	boolean includeERF_Rates, includeSpatialDecay;
	
	ETAS_Utils etas_utils;
	
	public static final double DEFAULT_MAX_DEPTH = 24;
	public static final double DEFAULT_DEPTH_DISCR = 2.0;
	public static final int DEFAULT_NUM_PT_SRC_SUB_PTS = 5;		// 5 is good for orig pt-src gridding of 0.1
	
	
	/**
	 * Constructor that uses default values
	 * @param regionForRates
	 * @param erf
	 * @param sourceRates
	 * @param pointSrcDiscr
	 * @param oututFileNameWithPath
	 * @param includeERF_Rates
	 * @param includeSpatialDecay
	 */
	public ETAS_PrimaryEventSampler(GriddedRegion griddedRegion, AbstractNthRupERF erf, double sourceRates[],
			double pointSrcDiscr, String oututFileNameWithPath, boolean includeERF_Rates, ETAS_Utils etas_utils,
			double etasDistDecay_q, double etasMinDist_d, boolean applyGR_Corr, List<float[]> inputFractSrcInCubeList, 
			List<int[]> inputSrcInCubeList) {

		this(griddedRegion, DEFAULT_NUM_PT_SRC_SUB_PTS, erf, sourceRates, DEFAULT_MAX_DEPTH, DEFAULT_DEPTH_DISCR, 
				pointSrcDiscr, oututFileNameWithPath, etasDistDecay_q, etasMinDist_d, includeERF_Rates, true, etas_utils,
				applyGR_Corr, inputFractSrcInCubeList, inputSrcInCubeList);
	}

	
	/**
	 * 
	 * @param griddedRegion - original gridded region for etas sampling
	 * @param numPtSrcSubPts - this is how the sampling region will be discretized (discr = pointSrcDiscr/numPtSrcSubPts)
	 * @param erf
	 * @param sourceRates - pointer to an array of source rates (which may get updated externally)
	 * @param maxDepth
	 * @param depthDiscr
	 * @param pointSrcDiscr - the grid spacing of off-fault/background events
	 * @param oututFileNameWithPath - not yet used
	 * @param distDecay - ETAS distance decay parameter
	 * @param minDist - ETAS minimum distance parameter
	 * @param includeERF_Rates - tells whether to consider long-term rates in sampling aftershocks
	 * @param includeSpatialDecay - tells whether to include spatial decay in sampling aftershocks (for testing)
	 * @param etas_utils - this is for obtaining reproducible random numbers (seed set in this object)
	 * @param applyGR_Corr - whether or not to apply the GR correction
	 * @throws IOException 
	 */
	public ETAS_PrimaryEventSampler(GriddedRegion griddedRegion, int numPtSrcSubPts, AbstractNthRupERF erf, double sourceRates[],
			double maxDepth, double depthDiscr, double pointSrcDiscr, String oututFileNameWithPath, double distDecay, 
			double minDist, boolean includeERF_Rates, boolean includeSpatialDecay, ETAS_Utils etas_utils, boolean applyGR_Corr,
			List<float[]> inputFractSrcInCubeList, List<int[]> inputSrcInCubeList) {
		
		origGriddedRegion = griddedRegion;
		cubeLatLonSpacing = pointSrcDiscr/numPtSrcSubPts;	// TODO pointSrcDiscr from griddedRegion?
		if(D) System.out.println("Gridded Region has "+griddedRegion.getNumLocations()+" cells");
		
		this.numPtSrcSubPts = numPtSrcSubPts;
		this.erf = erf;
		
		this.maxDepth=maxDepth;	// the bottom of the deepest cube
		this.depthDiscr=depthDiscr;
		this.pointSrcDiscr = pointSrcDiscr;
		numCubeDepths = (int)Math.round(maxDepth/depthDiscr);
		
		this.etas_utils = etas_utils;
		this.applyGR_Corr=applyGR_Corr;
				
		
		Region regionForRates = new Region(griddedRegion.getBorder(),BorderType.MERCATOR_LINEAR);

		
		// need to set the region anchors so that the gridRegForRatesInSpace sub-regions fall completely inside the gridded seis regions
		// this assumes the point sources have an anchor of GriddedRegion.ANCHOR_0_0)
		if(numPtSrcSubPts % 2 == 0) {	// it's an even number
			gridRegForCubes = new GriddedRegion(regionForRates, cubeLatLonSpacing, new Location(cubeLatLonSpacing/2d,cubeLatLonSpacing/2d));
			// parent locs are mid way between rates in space:
			gridRegForParentLocs = new GriddedRegion(regionForRates, cubeLatLonSpacing, GriddedRegion.ANCHOR_0_0);			
		}
		else {	// it's odd
			gridRegForCubes = new GriddedRegion(regionForRates, cubeLatLonSpacing, GriddedRegion.ANCHOR_0_0);
			// parent locs are mid way between rates in space:
			gridRegForParentLocs = new GriddedRegion(regionForRates, cubeLatLonSpacing, new Location(cubeLatLonSpacing/2d,cubeLatLonSpacing/2d));			
		}
		
		
		numCubesPerDepth = gridRegForCubes.getNumLocations();
		numCubes = numCubesPerDepth*numCubeDepths;
		
		numParDepths = numCubeDepths+1;
		numParLocsPerDepth = gridRegForParentLocs.getNumLocations();
		numParLocs = numParLocsPerDepth*numParDepths;
		
		if(D) {
			System.out.println("numParLocsPerDepth="+numParLocsPerDepth);
			System.out.println("numCubesPerDepth="+numCubesPerDepth);
		}
		
		grCorrFactorForCellArray = getGR_CorrFactorsForOrigGridCells();
		
		// write out some gridding values
//		if(D) {
//			for(int i=0; i<=numPtSrcSubPts;i++) {
//				System.out.println("loc #"+i+" for gridRegForRatesInSpace:"+gridRegForRatesInSpace.getLocation(i));
//				System.out.println("loc #"+i+" for gridRegForParentLocs:"+gridRegForParentLocs.getLocation(i));							
//			}
//			for(int d=0; d<numRateDepths;d++) {
//				System.out.println("Rate depth #"+d+" is "+getDepth(d)+"; check #="+getDepthIndex(getDepth(d)));
//			}
//			for(int d=0; d<numParDepths;d++) {
//				System.out.println("Parent depth #"+d+" is "+getParDepth(d)+"; check #="+getParDepthIndex(getParDepth(d)));
//			}
//		}

		
		// this is for caching samplers (one for each possible parent location)
		cachedSamplers = new IntegerPDF_FunctionSampler[numParLocs];
		
		numForthcomingEventsForParLocIndex = new Hashtable<Integer,Integer>();
		
		this.sourceRates = sourceRates;
		
		this.etasDistDecay=distDecay;
		this.etasMinDist=minDist;
		
		this.includeERF_Rates=includeERF_Rates;
		this.includeSpatialDecay=includeSpatialDecay;

		latForCubeCenter = new double[numCubes];
		lonForCubeCenter = new double[numCubes];
		depthForCubeCenter = new double[numCubes];
		for(int i=0;i<numCubes;i++) {
			int[] regAndDepIndex = getCubeRegAndDepIndicesForIndex(i);
			Location loc = gridRegForCubes.getLocation(regAndDepIndex[0]);
			latForCubeCenter[i] = loc.getLatitude();
			lonForCubeCenter[i] = loc.getLongitude();
			depthForCubeCenter[i] = getCubeDepth(regAndDepIndex[1]);
			
			// test - turn off once done once
//			Location testLoc = this.getLocationForSamplerIndex(i);
//			if(Math.abs(testLoc.getLatitude()-latForPoint[i]) > 0.00001)
//				throw new RuntimeException("Lats diff by more than 0.00001");
//			if(Math.abs(testLoc.getLongitude()-lonForPoint[i]) > 0.00001)
//				throw new RuntimeException("Lons diff by more than 0.00001");
//			if(Math.abs(testLoc.getDepth()-depthForPoint[i]) > 0.00001)
//				throw new RuntimeException("Depths diff by more than 0.00001");
			
		}


				
// System.out.println("HERE numPtSrcSubPts="+numPtSrcSubPts+"\t"+pointSrcDiscr+"\t"+regSpacing);
	

		totNumSrc = erf.getNumSources();
		if(totNumSrc != sourceRates.length)
			throw new RuntimeException("Problem with number of sources");
		
		if(erf instanceof FaultSystemSolutionERF)
			numFltSystSources = ((FaultSystemSolutionERF)erf).getNumFaultSystemSources();
		else
			numFltSystSources=0;
		if(D) System.out.println("totNumSrc="+totNumSrc+"\tnumFltSystSources="+numFltSystSources+
				"\tnumPointsForRates="+numCubes);
		srcIndexList = new ArrayList<Integer>();	// make a list so src indices are not duplicated in lists
		for(int i=0; i<totNumSrc;i++)
			srcIndexList.add(new Integer(i));
		
		rateUnassigned=0;
		
		// read or make cache data if needed
		if(inputFractSrcInCubeList!=null && inputSrcInCubeList != null) {
				srcInCubeList = inputSrcInCubeList;
				fractionSrcInCubeList = inputFractSrcInCubeList;
		}
		else {
			File intListListFile = new File(defaultSrcInCubeCacheFilename);
			File floatListListFile = new File(defaultFractSrcInCubeCacheFilename);	
			if (intListListFile.exists() && floatListListFile.exists()) {
				try {
					if(D) ETAS_SimAnalysisTools.writeMemoryUse("Memory before reading fractionSrcAtPointList");
					fractionSrcInCubeList = MatrixIO.floatArraysListFromFile(floatListListFile);
					if(D) ETAS_SimAnalysisTools.writeMemoryUse("Memory before reading srcAtPointList");
					srcInCubeList = MatrixIO.intArraysListFromFile(intListListFile);
					// the following test is not better in terms of memory use
					//					ArrayList<ArrayList<Integer>> test = intArraysListFromFile(intListListFile);
					if(D) ETAS_SimAnalysisTools.writeMemoryUse("Memory after reading srcAtPointList");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {  // make cache file if they don't exist
				if(D) ETAS_SimAnalysisTools.writeMemoryUse("Memory before running generateAndWriteListListDataToFile");
				generateAndWriteListListDataToFile();
				if(D) ETAS_SimAnalysisTools.writeMemoryUse("Memory after running generateAndWriteListListDataToFile");
			}

		}
		// read from file if it exists
		
		if(D) System.out.println("Running makeETAS_LocWtCalcList()");
		double maxDistKm=1000;
		double midLat = (gridRegForCubes.getMaxLat() + gridRegForCubes.getMinLat())/2.0;
		etas_LocWeightCalc = new ETAS_LocationWeightCalculator(maxDistKm, maxDepth, cubeLatLonSpacing, depthDiscr, midLat, etasDistDecay, etasMinDist, etas_utils);
		if(D) ETAS_SimAnalysisTools.writeMemoryUse("Memory after making etas_LocWeightCalc");
		if(D) System.out.println("Done running makeETAS_LocWtCalcList()");
		
		// write results to file
//		if(oututFileNameWithPath != null)
//			writeEqksAtPointArrayToFile(oututFileNameWithPath);
		
	}
	
	public void setSrcAtPointCaches(List<float[]> fractionSrcAtPointList, List<int[]> srcAtPointList) {
		this.fractionSrcInCubeList = fractionSrcAtPointList;
		this.srcInCubeList = srcAtPointList;
	}
	
	
	public void addParentLocToProcess(Location parLoc) {
		int parLocIndex = this.getParLocIndexForLocation(parLoc);
		if(numForthcomingEventsForParLocIndex.keySet().contains(parLocIndex)) {
			int newNum = numForthcomingEventsForParLocIndex.get(parLocIndex) + 1;
			numForthcomingEventsForParLocIndex.put(parLocIndex, newNum);
		}
		else {
			numForthcomingEventsForParLocIndex.put(parLocIndex, 1);
		}
	}
	
	
	/**
	 * This generates the following and writes them to a file:
	 * 
	 * srcAtPointList
	 * fractionSrcAtPointList
	 * 
	 */
	private void generateAndWriteListListDataToFile() {
		if(D) System.out.println("Starting ETAS.ETAS_PrimaryEventSampler.generateAndWriteListListDataToFile(); THIS WILL TAKE TIME AND MEMORY!");
		long st = System.currentTimeMillis();
		CalcProgressBar progressBar = null;
		try {
			progressBar = new CalcProgressBar("Sources to process in ETAS_PrimaryEventSamplerAlt", "junk");
		} catch (Exception e1) {} // headless
		ArrayList<ArrayList<Integer>> sourcesAtPointList = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Float>> fractionsAtPointList = new ArrayList<ArrayList<Float>>();
		for(int i=0; i<numCubes;i++) {
			sourcesAtPointList.add(new ArrayList<Integer>());
			fractionsAtPointList.add(new ArrayList<Float>());
		}
		if (progressBar != null) progressBar.showProgress(true);
		for(int s=0;s<totNumSrc;s++) {
			ProbEqkSource src = erf.getSource(s);
			if (progressBar != null) progressBar.updateProgress(s, totNumSrc);

			// If it's not a gridded seismicity source:
			if(s<numFltSystSources) {
				Hashtable<Integer,Float> fractAtPointTable = new Hashtable<Integer,Float>(); // int is ptIndex and double is fraction there
				LocationList locsOnRupSurf = src.getRupture(0).getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();	// assuming all ruptures in souce have same surface
				int numLocs = locsOnRupSurf.size();
				float ptFract = 1f/(float)numLocs;
				for(Location loc: locsOnRupSurf) {
					int regIndex = gridRegForCubes.indexForLocation(loc);
//if(!doneOne) {
//	System.out.println("fault loc: "+loc);
//	System.out.println("assoc reg loc: "+gridRegForRatesInSpace.getLocation(regIndex));
//	doneOne=true;
//	//System.exit(0);
//}
					int depIndex = getCubeDepthIndex(loc.getDepth());
					if(depIndex >= numCubeDepths) {
						depIndex = numCubeDepths-1;	// TODO
						if(D) System.out.println("Depth below max for point on "+src.getName()+"\t depth="+loc.getDepth());
					}

					if(regIndex != -1) {
						int cubeIndex = this.getCubeIndexForRegAndDepIndices(regIndex, depIndex);
						if(cubeIndex>=numCubes) {
							throw new RuntimeException("Error: ptIndex="+cubeIndex+"/depIndex="+depIndex+"/tnumDepths="+numCubeDepths);
						}
						if(fractAtPointTable.containsKey(cubeIndex)) {
							float newFrac = fractAtPointTable.get(cubeIndex) + ptFract;
							fractAtPointTable.put(cubeIndex,newFrac);
						}
						else {
							fractAtPointTable.put(cubeIndex,ptFract);
						}
					}
					else {
						rateUnassigned += sourceRates[s]/numLocs;
					}
				}	
				// now assign this hashTable
				for(Integer ptIndex : fractAtPointTable.keySet()) {
					float fract = fractAtPointTable.get(ptIndex);
					sourcesAtPointList.get(ptIndex).add(srcIndexList.get(s));
					fractionsAtPointList.get(ptIndex).add(fract);
				}
			}
			else {	// It's a point source
				for(ProbEqkRupture rup: src)
					if(!rup.getRuptureSurface().isPointSurface())	// make sure they're all point surfaces
						throw new RuntimeException("All ruptures for source must have point surfaces here");

				Location centerLoc = src.getRupture(0).getRuptureSurface().getFirstLocOnUpperEdge();
				double numPts = numPtSrcSubPts*numPtSrcSubPts*numCubeDepths;
				double ptRate = sourceRates[s]/numPts;
				float ptFrac = 1f/(float)numPts;
				// distribution this among the locations within the space represented by the point source
				for(int iLat=0; iLat<numPtSrcSubPts;iLat++) {
					double lat = centerLoc.getLatitude()-pointSrcDiscr/2 + iLat*cubeLatLonSpacing + cubeLatLonSpacing/2.0;
					for(int iLon=0; iLon<numPtSrcSubPts;iLon++) {
						double lon = centerLoc.getLongitude()-pointSrcDiscr/2 + iLon*cubeLatLonSpacing + cubeLatLonSpacing/2.0;
						int regIndex = gridRegForCubes.indexForLocation(new Location(lat,lon));
// if(regIndex==1000) System.out.println("TEST HERE: "+lat+"\t"+lon+"\t"+gridRegForRatesInSpace.getLocation(regIndex));
						if(regIndex != -1){
							for(int iDep =0; iDep<numCubeDepths; iDep++) {
//								int ptIndex = iDep*numRegLocs+regIndex;
								int cubeIndex = getCubeIndexForRegAndDepIndices(regIndex,iDep);	// TODO method is miss named; should be cube not sampler' result is correct here
								sourcesAtPointList.get(cubeIndex).add(srcIndexList.get(s));
								fractionsAtPointList.get(cubeIndex).add(ptFrac);
							}
						}
						else {
							rateUnassigned += ptRate*numCubeDepths;
//							System.out.println("1\t"+centerLoc.getLatitude()+"\t"+centerLoc.getLongitude()+"\t"+centerLoc.getDepth());
						}
					}
				}
			}
		}
		if (progressBar != null) progressBar.showProgress(false);
		if(D) System.out.println("rateUnassigned="+rateUnassigned);
		
		ETAS_SimAnalysisTools.writeMemoryUse("Memory before writing files");
		File intListListFile = new File(defaultSrcInCubeCacheFilename);
		File floatListListFile = new File(defaultFractSrcInCubeCacheFilename);
		try {
			MatrixIO.intListListToFile(sourcesAtPointList,intListListFile);
			MatrixIO.floatListListToFile(fractionsAtPointList, floatListListFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(D) System.out.println("ETAS_PrimaryEventSampler.generateAndWriteListListDataToFile() took "+(System.currentTimeMillis()-st)/60000+ " min");


	}
	
	

	
	/**
	 * This loops over all points on the rupture surface and creates a net (average) point sampler.
	 * @param mainshock
	 * @return
	 */
	public IntegerPDF_FunctionSampler getAveSamplerForRupture(EqkRupture mainshock) {
		
		IntegerPDF_FunctionSampler aveSampler = new IntegerPDF_FunctionSampler(numCubes);
		
		LocationList locList = mainshock.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();
		for(Location loc: locList) {
			
			// set the sampler
			int parLocIndex = getParLocIndexForLocation(loc);
			IntegerPDF_FunctionSampler sampler = getCubeSampler(parLocIndex);
			
			for(int i=0;i <numCubes;i++) {
				aveSampler.add(i, sampler.getY(i));
			}
		}
		return aveSampler;
	}
	
	
	/**
	 * For the given main shock, this gives the trigger probability of each source in the ERF.
	 * This loops over all points on the main shock rupture surface, giving equal triggering
	 * potential to each.
	 * @param mainshock
	 * @return double[] the relative triggering probability of each ith source
	 */
	public double[] getTriggerProbOfEachSource(EqkRupture mainshock) {
		double[] trigProb = new double[erf.getNumSources()];
		
		IntegerPDF_FunctionSampler aveSampler = getAveSamplerForRupture(mainshock);

		// normalize so values sum to 1.0
		aveSampler.scale(1.0/aveSampler.getSumOfY_vals());
		
		// now loop over all the points for rates
		double total = 0;
		for(int i=0;i <numCubes;i++) {
			int[] sources = srcInCubeList.get(i);
			if(sources.length==0) {
				continue;
			}
			if (sources.length==1) {
				trigProb[sources[0]] += aveSampler.getY(i);
			}
			else {
				float[] fracts = fractionSrcInCubeList.get(i);
				// compute the relative probability of each source at this point
				double[] relProb = new double[sources.length];
				
				int indexForOrigGriddedRegion = -1;
				if(applyGR_Corr) {
					indexForOrigGriddedRegion = origGriddedRegion.indexForLocation(getCubeLocationForIndex(i));	// more efficient way?
//if(indexForOrigGriddedRegion == -1)
//	System.out.println("bad loc: "+getLocationForSamplerIndex(i));
				}
				for(int s=0; s<sources.length;s++) {
					if(applyGR_Corr && sources[s]<numFltSystSources && indexForOrigGriddedRegion != -1)
						relProb[s] = sourceRates[sources[s]]*(double)fracts[s]*grCorrFactorForCellArray[indexForOrigGriddedRegion];										
					else
						relProb[s] = sourceRates[sources[s]]*(double)fracts[s];		
				}
				total=0;
				for(int s=0; s<sources.length;s++)	// sum for normalization
					total += relProb[s];
				for(int s=0; s<sources.length;s++)
					trigProb[sources[s]] += aveSampler.getY(i)*relProb[s]/total;
			}
		}

//		double testSum=0;
//		for(int s=0; s<trigProb.length; s++)
//			testSum += trigProb[s];
		
		return trigProb;
	}
	
	
	/**
	 * 
	 * @param mainshock
	 * @return
	 */
	public SummedMagFreqDist getExpectedPrimaryMFD_PDF(EqkRupture mainshock) {
		double[] srcProbs = getTriggerProbOfEachSource(mainshock);
		SummedMagFreqDist magDist = new SummedMagFreqDist(2.05, 8.95, 70);
		for(int s=0; s<srcProbs.length;s++) {
			SummedMagFreqDist srcMFD = ERF_Calculator.getTotalMFD_ForSource(erf.getSource(s), 1.0, 2.05, 8.95, 70, true);
			srcMFD.normalizeByTotalRate();
			srcMFD.scale(srcProbs[s]);
			if(!Double.isNaN(srcMFD.getTotalIncrRate())) // not sure why this is needed
				magDist.addIncrementalMagFreqDist(srcMFD);
		}
		return magDist;
	}
	
	
	
	public double getDistDecay() {
		return etasDistDecay;
	}
	
	
	public double getMinDist() {
		return etasMinDist;
	}
	
	
	/**
	 * This returns the max depth in km
	 */
	public double getMaxDepth() {
		return maxDepth;
	}

	
	
	/**
	 * This method will populate the given rupToFillIn with attributes of a randomly chosen
	 * primary aftershock for the given main shock.  If a fault system rupture is sampled, the
	 * hypocenter is chosen randomly from the points on the surface that are at the point where
	 * the event was sampled from (uniform distribution, and not weighted by the distance each
	 * surface point at that location is from the source.
	 * from the source
	 * @return boolean tells whether it succeeded in setting the rupture
	 * @param mainshock
	 * @param rupToFillIn
	 */
	public boolean setRandomPrimaryEvent(ETAS_EqkRupture rupToFillIn) {
		
		EqkRupture parRup = rupToFillIn.getParentRup();
		
		// get the location on the parent that does the triggering
		Location actualParentLoc=rupToFillIn.getParentTriggerLoc();
			
//		// Check for problem region index
//		int parRegIndex = gridRegForParentLocs.indexForLocation(actualParentLoc);
//		if(parRegIndex <0) {
//			if(parRup instanceof ETAS_EqkRupture) {
//				System.out.println("Problem event generation: "+((ETAS_EqkRupture)parRup).getGeneration());
//			}
//			System.out.println("PROBLEM: parRegIndex<0; parentLoc="+actualParentLoc.toString()+
//					"\tNum pts on main shock surface: "+parRup.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface().size());
//			return false;
//		}

////System.out.println("actualParentLoc: "+actualParentLoc);
////System.out.println("parDepIndex: "+getParDepthIndex(actualParentLoc.getDepth()));
////System.out.println("getParDepth(parDepIndex): "+getParDepth(parDepIndex));
////System.out.println("translatedParLoc: "+translatedParLoc);
////System.exit(0);
		
		int parLocIndex = getParLocIndexForLocation(actualParentLoc);
		Location translatedParLoc = getParLocationForIndex(parLocIndex);
		IntegerPDF_FunctionSampler sampler = getCubeSampler(parLocIndex);
		
		int aftShCubeIndex = sampler.getRandomInt(etas_utils.getRandomDouble());
		int randSrcIndex = getRandomSourceIndexInCube(aftShCubeIndex);
		
		// following is needed for case where includeERF_Rates = false (point can be chosen that has no sources)
		if(randSrcIndex<0) {
//			System.out.println("working on finding a non-neg source index");
			while (randSrcIndex<0) {
				aftShCubeIndex = sampler.getRandomInt(etas_utils.getRandomDouble());
				randSrcIndex = getRandomSourceIndexInCube(aftShCubeIndex);
			}
		}
		
		ProbEqkSource src = erf.getSource(randSrcIndex);
		int r=0;
		if(src.getNumRuptures() > 1) {
			r = src.drawSingleRandomEqkRuptureIndex(etas_utils.getRandomDouble());
		}
		int nthRup = erf.getIndexN_ForSrcAndRupIndices(randSrcIndex,r);
		ProbEqkRupture erf_rup = src.getRupture(r);
		
		// set hypocenter location & rupture surface
		if(randSrcIndex < numFltSystSources) {	// if it's a fault system source
			// need to choose point on rup surface that is the hypocenter			
			LocationList locsOnRupSurf = erf_rup.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();
			LocationList locsToSampleFrom = new LocationList();
			for(Location loc: locsOnRupSurf) {
				if(aftShCubeIndex == getCubeIndexForLocation(loc)) {
					locsToSampleFrom.add(loc);
				}
			}	
if(locsToSampleFrom.size() == 0) {
	System.out.println("PROBLEM: randSrcIndex="+randSrcIndex+"\tName: = "+src.getName());
	for(int srcID:srcInCubeList.get(aftShCubeIndex))
		System.out.println(srcID);
	System.out.println("lat\tlon\tdepth");
	System.out.println(latForCubeCenter[aftShCubeIndex]+"\t"+lonForCubeCenter[aftShCubeIndex]+"\t"+depthForCubeCenter[aftShCubeIndex]);
	for(Location loc: locsOnRupSurf) {
		System.out.println(loc.getLatitude()+"\t"+loc.getLongitude()+"\t"+loc.getDepth());
	}	
}
			// choose one randomly
			int hypoLocIndex = etas_utils.getRandomInt(locsToSampleFrom.size()-1);
			rupToFillIn.setHypocenterLocation(locsToSampleFrom.get(hypoLocIndex));
			rupToFillIn.setRuptureSurface(erf_rup.getRuptureSurface());
			rupToFillIn.setFSSIndex(((FaultSystemSolutionERF)erf).getFltSysRupIndexForNthRup(nthRup));
		}
		else { // it's a gridded seis source
			double relLat = latForCubeCenter[aftShCubeIndex]-translatedParLoc.getLatitude();
			double relLon = lonForCubeCenter[aftShCubeIndex]-translatedParLoc.getLongitude();
			double relDep = depthForCubeCenter[aftShCubeIndex]-translatedParLoc.getDepth();
			
			rupToFillIn.setGridNodeIndex(randSrcIndex - numFltSystSources);
						
			Location deltaLoc = etas_LocWeightCalc.getRandomDeltaLoc(Math.abs(relLat), Math.abs(relLon), 
					depthForCubeCenter[aftShCubeIndex],translatedParLoc.getDepth());
			
			double newLat, newLon, newDep;
			if(relLat<0.0)	// neg value
				newLat = latForCubeCenter[aftShCubeIndex]-deltaLoc.getLatitude();
			else 
				newLat = latForCubeCenter[aftShCubeIndex]+deltaLoc.getLatitude();
			if(relLon<0.0)	// neg value
				newLon = lonForCubeCenter[aftShCubeIndex]-deltaLoc.getLongitude();
			else 
				newLon = lonForCubeCenter[aftShCubeIndex]+deltaLoc.getLongitude();

			newDep = depthForCubeCenter[aftShCubeIndex]+deltaLoc.getDepth();

			Location randLoc = new Location(newLat,newLon,newDep);
			
			// get a location vector pointing from the translated parent location to the actual parent location nearest point here to the srcLoc
			LocationVector corrVector = LocationUtils.vector(translatedParLoc, actualParentLoc);
			Location hypLoc = LocationUtils.location(randLoc, corrVector);

			
			// Issue: that last step could have moved the hypocenter outside the grid node of the source (by up to ~1 km);
			// move it back just inside if the new grid node does not go that high enough magnitude
			if(erf instanceof FaultSystemSolutionERF) {
				FaultSystemSolutionERF tempERF = (FaultSystemSolutionERF)erf;
				int gridSrcIndex = randSrcIndex-numFltSystSources;
				Location gridSrcLoc = tempERF.getSolution().getGridSourceProvider().getGriddedRegion().getLocation(gridSrcIndex);
				int testIndex = tempERF.getSolution().getGridSourceProvider().getGriddedRegion().indexForLocation(hypLoc);
				if(testIndex != gridSrcIndex) {
//					System.out.println("Region Index Problem:\t"+rupToFillIn.getID()+"\t"+(gridSrcLoc.getLatitude()-hypLoc.getLatitude())+"\t"+(gridSrcLoc.getLongitude()-hypLoc.getLongitude()));
					IncrementalMagFreqDist mfd = tempERF.getSolution().getGridSourceProvider().getNodeMFD(testIndex);
					int maxMagIndex = mfd.getClosestXIndex(mfd.getMaxMagWithNonZeroRate());
					int magIndex = mfd.getClosestXIndex(erf_rup.getMag());
					double tempLat=hypLoc.getLatitude();
					double tempLon= hypLoc.getLongitude();
					double tempDepth = hypLoc.getDepth();
					double halfGrid=pointSrcDiscr/2.0;
					if(maxMagIndex<magIndex) {
						if(hypLoc.getLatitude()-gridSrcLoc.getLatitude()>=halfGrid)
							tempLat=gridSrcLoc.getLatitude()+halfGrid*0.99;	// 0.99 makes sure it's inside
						else if (hypLoc.getLatitude()-gridSrcLoc.getLatitude()<=-halfGrid)
							tempLat=gridSrcLoc.getLatitude()-halfGrid*0.99;	// 0.99 makes sure it's inside
						if(hypLoc.getLongitude()-gridSrcLoc.getLongitude()>=halfGrid)
							tempLon=gridSrcLoc.getLongitude()+halfGrid*0.99;	// 0.99 makes sure it's inside
						else if (hypLoc.getLongitude()-gridSrcLoc.getLongitude()<=-halfGrid)
							tempLon=gridSrcLoc.getLongitude()-halfGrid*0.99;	// 0.99 makes sure it's inside
						hypLoc = new Location(tempLat,tempLon,tempDepth);
						int testIndex2 = tempERF.getSolution().getGridSourceProvider().getGriddedRegion().indexForLocation(hypLoc);
						if(testIndex2 != gridSrcIndex) {
							throw new RuntimeException("grid problem");
						}
					}
				}
			}
			
			rupToFillIn.setHypocenterLocation(hypLoc);
			rupToFillIn.setPointSurface(hypLoc);
		}
		
		// fill in the rest
		rupToFillIn.setAveRake(erf_rup.getAveRake());
		rupToFillIn.setMag(erf_rup.getMag());
		rupToFillIn.setNthERF_Index(nthRup);
		
		// distance of triggered event from parent
		double distToParent = LocationUtils.linearDistanceFast(actualParentLoc, rupToFillIn.getHypocenterLocation());
		rupToFillIn.setDistanceToParent(distToParent);
		
		return true;
	}
	
	
	
	private IntegerPDF_FunctionSampler getCubeSampler(int locIndexForPar) {
		IntegerPDF_FunctionSampler sampler=null;
		
		int numLeft = numForthcomingEventsForParLocIndex.get(locIndexForPar) - 1;
		if(numLeft == 0) {
			numForthcomingEventsForParLocIndex.remove(locIndexForPar);
		}
		else {
			numForthcomingEventsForParLocIndex.put(locIndexForPar, numLeft);
		}
//		System.out.print(numForthcomingEventsForParLocIndex.size()+", ");

		boolean cacheExisted=true;
		if(includeERF_Rates && includeSpatialDecay) {
			
			if(cachedSamplers[locIndexForPar] == null) {
				cacheExisted = false;
				sampler = getCubeSamplerWithDistDecay(locIndexForPar);			
//				cachedSamplers[locIndexForPar] = sampler;
//				numCachedSamplers += 1;
			}
			else {
				sampler = cachedSamplers[locIndexForPar];
			}
				
		}
		else if(includeERF_Rates && !includeSpatialDecay) {
			sampler = getCubeSamplerWithERF_RatesOnly();
		}
		else if(!includeERF_Rates && includeSpatialDecay) {
			if(cachedSamplers[locIndexForPar] == null) {
				sampler = getCubeSamplerWithOnlyDistDecay(locIndexForPar);
//				cachedSamplers[locIndexForPar] = sampler;
//				numCachedSamplers += 1;
//System.out.println("Used this one: getPointSamplerWithOnlyDistDecay(parentLoc)");
			}
			else {
				sampler = cachedSamplers[locIndexForPar];
			}

		}
		
//		if(cacheExisted && numLeft==0) 
//			numCachedSamplers -= 1;
//		if(numLeft==0) {
//			cachedSamplers[locIndexForPar] = null;
//			// TODO System.gc() ?
//		}
		
		
		if(D) {
			if(numCachedSamplers==nextNumCachedSamplers) {
				System.out.println("numCachedSamplers="+numCachedSamplers);
				nextNumCachedSamplers += incrForReportingNumCachedSamplers;
			}
		}
		return sampler;
	}
	
	
	

	/**
	 * This will force updating of all the samplers and other things
	 */
	public void declareRateChange() {
		if(D)ETAS_SimAnalysisTools.writeMemoryUse("Memory before discarding chached Samplers");
		cubeSamplerRatesOnly = null;
		cachedSamplers = new IntegerPDF_FunctionSampler[numParLocs];
		numCachedSamplers=0;
		nextNumCachedSamplers=incrForReportingNumCachedSamplers;
		if(mfdForSrcArray != null) {	// if using this array, update only fault system sources
			for(int s=0; s<numFltSystSources;s++) {
				mfdForSrcArray[s] = ERF_Calculator.getTotalMFD_ForSource(erf.getSource(s), erf.getTimeSpan().getDuration(), 5.05, 8.95, 40, true);
			}
		}
		System.gc();
		if(D)ETAS_SimAnalysisTools.writeMemoryUse("Memory after discarding chached Samplers");
	}
	
	
	
	/**
	 * This method
	 */
	private IntegerPDF_FunctionSampler getCubeSamplerWithERF_RatesOnly() {
		if(cubeSamplerRatesOnly == null) {
			cubeSamplerRatesOnly = new IntegerPDF_FunctionSampler(numCubes);
			for(int i=0;i<numCubes;i++) {
				int[] sources = srcInCubeList.get(i);
				float[] fract = fractionSrcInCubeList.get(i);
				double totRate=0;
				for(int j=0; j<sources.length;j++) {
					totRate += sourceRates[sources[j]]*(double)fract[j];
				}
				cubeSamplerRatesOnly.set(i,totRate);
			}
		}
		return cubeSamplerRatesOnly;
	}
	
	
	private IntegerPDF_FunctionSampler getCubeSamplerWithDistDecay(int parLocIndex) {
		Location parLoc = this.getParLocationForIndex(parLocIndex);
		getCubeSamplerWithERF_RatesOnly();	// this makes sure cubeSamplerRatesOnly (rates only) is updated
		IntegerPDF_FunctionSampler sampler = new IntegerPDF_FunctionSampler(numCubeDepths*numCubesPerDepth);
		for(int index=0; index<numCubes; index++) {
			double relLat = Math.abs(parLoc.getLatitude()-latForCubeCenter[index]);
			double relLon = Math.abs(parLoc.getLongitude()-lonForCubeCenter[index]);
			sampler.set(index,etas_LocWeightCalc.getProbAtPoint(relLat, relLon, depthForCubeCenter[index], parLoc.getDepth())*cubeSamplerRatesOnly.getY(index));
		}
		return sampler;
	}

	/**
	 * This sampler ignores the long-term rates
	 * @param mainshock
	 * @param etasLocWtCalc
	 * @return
	 */
	private IntegerPDF_FunctionSampler getCubeSamplerWithOnlyDistDecay(int parLocIndex) {
		Location parLoc = this.getParLocationForIndex(parLocIndex);
		IntegerPDF_FunctionSampler sampler = new IntegerPDF_FunctionSampler(numCubes);
//		try{
//			FileWriter fw1 = new FileWriter("test123.txt");
//			fw1.write("relLat\trelLon\trelDep\twt\n");
			for(int index=0; index<numCubes; index++) {
				double relLat = Math.abs(parLoc.getLatitude()-latForCubeCenter[index]);
				double relLon = Math.abs(parLoc.getLongitude()-lonForCubeCenter[index]);
				sampler.set(index,etas_LocWeightCalc.getProbAtPoint(relLat, relLon, depthForCubeCenter[index], parLoc.getDepth()));
//				if(relLat<0.25 && relLon<0.25)
//					fw1.write((float)relLat+"\t"+(float)relLon+"\t"+(float)relDep+"\t"+(float)sampler.getY(index)+"\n");
			}
//			fw1.close();
//		}catch(Exception e) {
//			e.printStackTrace();
//		}

		return sampler;
	}
	
	
	/**
	 * This returns a negative int (-1) if there is no source in the cube (which can happen if
	 * includeERF_Rates = false since these points won't be zero)
	 * @param ptIndex
	 * @return
	 */
	public int getRandomSourceIndexInCube(int cubeIndex) {
		int[] sources = srcInCubeList.get(cubeIndex);
		if(sources.length==0) {
			return -1;
		}
		else if (sources.length==1) {
			return sources[0];
		}
		else {
			float[] fracts = fractionSrcInCubeList.get(cubeIndex);
			IntegerPDF_FunctionSampler sampler = new IntegerPDF_FunctionSampler(sources.length);
			int indexForOrigGriddedRegion = -2;
			if(applyGR_Corr)
				indexForOrigGriddedRegion = origGriddedRegion.indexForLocation(getCubeLocationForIndex(cubeIndex));	// more efficient way?
			for(int s=0; s<sources.length;s++) {
				if(applyGR_Corr && sources[s]<numFltSystSources && indexForOrigGriddedRegion != -1) {
					sampler.set(s,sourceRates[sources[s]]*(double)fracts[s]*grCorrFactorForCellArray[indexForOrigGriddedRegion]);		
				}
				else
					sampler.set(s,sourceRates[sources[s]]*(double)fracts[s]);		
			}
			return sources[sampler.getRandomInt(etas_utils.getRandomDouble())];
		}
	}
	
	
	public double[] getGR_CorrFactorsForOrigGridCells() {
		
		double[] grCorrFactorForCellArray = new double[origGriddedRegion.getNodeCount()];
		
		if(erf instanceof FaultSystemSolutionERF) {
			FaultSystemSolutionERF tempERF = (FaultSystemSolutionERF)erf;
			SummedMagFreqDist[] subMFD_Array = FaultSystemSolutionCalc.getSubSeismNucleationMFD_inGridNotes((InversionFaultSystemSolution)tempERF.getSolution(), origGriddedRegion);
			SummedMagFreqDist[] supraMFD_Array = FaultSystemSolutionCalc.getSupraSeismNucleationMFD_inGridNotes((InversionFaultSystemSolution)tempERF.getSolution(), origGriddedRegion);

			double minCorr=Double.MAX_VALUE;
			int minCorrIndex = -1;
			for(int i=0;i<subMFD_Array.length;i++) {
				if(supraMFD_Array[i] != null) {
					double val = ETAS_Utils.getScalingFactorToImposeGR(supraMFD_Array[i], subMFD_Array[i]);
					if(val<1.0)
						grCorrFactorForCellArray[i]=val;
					else
						grCorrFactorForCellArray[i]=1.0;
					if(val<minCorr) {
						minCorr=val;
						minCorrIndex=i;
					}
				}
				else {	// no supra-seismogenic ruptures
					grCorrFactorForCellArray[i]=1.0;
				}
			}
			if(D) System.out.println("min GR Corr ("+minCorr+") at grid point: "+minCorrIndex+"\t"+origGriddedRegion.getLocation(minCorrIndex));
			return grCorrFactorForCellArray;

		}
		else {
			for(int i=0;i<grCorrFactorForCellArray.length;i++)
				grCorrFactorForCellArray[i] = 1.0;
			return grCorrFactorForCellArray;
		}
		
	}
	
	
	/**
	 * Region index is first element, and depth index is second
	 * @param index
	 * @return
	 */
	private int[] getCubeRegAndDepIndicesForIndex(int cubeIndex) {
		
		int[] indices = new int[2];
		indices[1] = (int)Math.floor((double)cubeIndex/(double)numCubesPerDepth);	// depth index
		if(indices[1] >= this.numCubeDepths )
			System.out.println("PROBLEM: "+cubeIndex+"\t"+numCubesPerDepth+"\t"+indices[1]+"\t"+numCubeDepths);
		indices[0] = cubeIndex - indices[1]*numCubesPerDepth;						// region index
		return indices;
	}
	
	public Location getCubeLocationForIndex(int cubeIndex) {
		int[] regAndDepIndex = getCubeRegAndDepIndicesForIndex(cubeIndex);
		Location regLoc = gridRegForCubes.getLocation(regAndDepIndex[0]);
		return new Location(regLoc.getLatitude(),regLoc.getLongitude(),getCubeDepth(regAndDepIndex[1]));
	}
	
	public int getCubeIndexForLocation(Location loc) {
		int iReg = gridRegForCubes.indexForLocation(loc);
		int iDep = getCubeDepthIndex(loc.getDepth());
		return getCubeIndexForRegAndDepIndices(iReg,iDep);
	}

	private int getCubeIndexForRegAndDepIndices(int iReg,int iDep) {
		return iDep*numCubesPerDepth+iReg;
	}
	
	private int getCubeDepthIndex(double depth) {
		int index = (int)Math.round((depth-depthDiscr/2.0)/depthDiscr);
//		if(index < numRateDepths && index >=0)
			return index;
//		else
//			throw new RuntimeException("Index "+index+" is out of bounds for depth="+depth);
	}
	
	private double getCubeDepth(int depthIndex) {
		return (double)depthIndex*depthDiscr + depthDiscr/2;
	}

	
	
	/**
	 * Region index is first element, and depth index is second
	 * @param index
	 * @return
	 */
	private int[] getParRegAndDepIndicesForIndex(int parLocIndex) {
		int[] indices = new int[2];
		indices[1] = (int)Math.floor((double)parLocIndex/(double)numParLocsPerDepth);	// depth index
		indices[0] = parLocIndex - indices[1]*numParLocsPerDepth;						// region index
		return indices;
	}
	
	public Location getParLocationForIndex(int parLocIndex) {
		int[] regAndDepIndex = getParRegAndDepIndicesForIndex(parLocIndex);
		Location regLoc = gridRegForParentLocs.getLocation(regAndDepIndex[0]);
		return new Location(regLoc.getLatitude(),regLoc.getLongitude(),getParDepth(regAndDepIndex[1]));
	}
	
	public int getParLocIndexForLocation(Location loc) {
		int iReg = gridRegForParentLocs.indexForLocation(loc);
		int iDep = getParDepthIndex(loc.getDepth());
		return getParLocIndexForRegAndDepIndices(iReg,iDep);
	}

	private int getParLocIndexForRegAndDepIndices(int iReg,int iDep) {
		return iDep*numParLocsPerDepth+iReg;
	}	
	
	private int getParDepthIndex(double depth) {
		return (int)Math.round(depth/depthDiscr);
	}
	
	private double getParDepth(int parDepthIndex) {
		return parDepthIndex*depthDiscr;
	}
	
	// this tests that the rates represented here (plus unassigned rates) match that in the ERF.
	public void testRates() {
		
		System.out.println("Testing total rate");
		getCubeSamplerWithERF_RatesOnly();
		
		totRate=this.cubeSamplerRatesOnly.calcSumOfY_Vals();
		totRate+=rateUnassigned;
		
		double testRate2=0;
		double duration = erf.getTimeSpan().getDuration();
		for(int s=0;s<erf.getNumSources();s++) {
			ProbEqkSource src = erf.getSource(s);
			int numRups = src.getNumRuptures();
			for(int r=0; r<numRups;r++) {
				testRate2 += src.getRupture(r).getMeanAnnualRate(duration);
			}
		}
		System.out.println("\ttotRateTest="+(float)totRate+" should equal Rate2="+(float)testRate2+";\tratio="+(float)(totRate/testRate2));
	}
	
	
	/**
	 * This compares the MFD represented here to that in the ERF.
	 * What about one including sources outside the region?
	 * @param erf
	 */
	public void testMagFreqDist() {
		
		System.out.println("Running testMagFreqDist()");
		SummedMagFreqDist magDist = new SummedMagFreqDist(2.05, 8.95, 70);
		double duration = erf.getTimeSpan().getDuration();
		if(mfdForSrcArray == null) {
			long st = System.currentTimeMillis();
			ETAS_SimAnalysisTools.writeMemoryUse("Memory before mfdForSrcArray");
			mfdForSrcArray = new SummedMagFreqDist[erf.getNumSources()];
			for(int s=0; s<erf.getNumSources();s++) {
				mfdForSrcArray[s] = ERF_Calculator.getTotalMFD_ForSource(erf.getSource(s), duration, 2.05, 8.95, 70, true);
			}
			ETAS_SimAnalysisTools.writeMemoryUse("Memory after mfdForSrcArray, which took (msec): "+(System.currentTimeMillis()-st));
		}
//		getPointSampler();	// make sure it exisits
		CalcProgressBar progressBar = new CalcProgressBar("Looping over all points", "junk");
		progressBar.showProgress(true);

		for(int i=0; i<numCubes;i++) {
			progressBar.updateProgress(i, numCubes);
			int[] sources = srcInCubeList.get(i);
			float[] fracts = fractionSrcInCubeList.get(i);
			for(int s=0; s<sources.length;s++) {
				SummedMagFreqDist mfd = mfdForSrcArray[sources[s]];
				for(int m=0;m<mfd.getNum();m++)
					magDist.add(m, mfd.getY(m)*(double)fracts[s]);
//				mfd.scale((double)fracts[s]);
//				magDist.addIncrementalMagFreqDist(mfd);
//				if(s>erf.getNumFaultSystemSources()) {
//					System.out.println("source "+s+"\n"+mfd);
//					System.exit(0);
//				}
			}
		}
		progressBar.showProgress(false);

		magDist.setName("MFD from EqksAtPoint list");
		ArrayList<EvenlyDiscretizedFunc> magDistList = new ArrayList<EvenlyDiscretizedFunc>();
		magDistList.add(magDist);
		magDistList.add(magDist.getCumRateDistWithOffset());
		
		SummedMagFreqDist erfMFD = ERF_Calculator.getTotalMFD_ForERF(erf, 2.05, 8.95, 70, true);
		erfMFD.setName("MFD from ERF");
		magDistList.add(erfMFD);
		magDistList.add(erfMFD.getCumRateDistWithOffset());

		// Plot these MFDs
		GraphWindow magDistsGraph = new GraphWindow(magDistList, "Mag-Freq Distributions"); 
		magDistsGraph.setX_AxisLabel("Mag");
		magDistsGraph.setY_AxisLabel("Rate");
		magDistsGraph.setY_AxisRange(1e-6, magDistsGraph.getY_AxisRange().getUpperBound());
		magDistsGraph.setYLog(true);

	}
	

	
	public void temp() {
		
//		int num=0;
//		int totNum =0;
//		for(int i=0;i < srcInCubeList.size();i++) {
//			totNum +=1;
//			if(srcInCubeList.get(i).length > 2) {
//				System.out.println(i+"\t"+srcInCubeList.get(i).length);
//				num += 1;
//			}
//		}
//		System.out.println(num+"\t out of\t"+totNum);
//		System.exit(-1);
		
		if(erf instanceof FaultSystemSolutionERF) {
			
			GridSourceProvider gridSrcProvider = ((FaultSystemSolutionERF)erf).getSolution().getGridSourceProvider();
			
			InversionFaultSystemRupSet rupSet = (InversionFaultSystemRupSet)((FaultSystemSolutionERF)erf).getSolution().getRupSet();
			FaultPolyMgr faultPolyMgr = rupSet.getInversionTargetMFDs().getGridSeisUtils().getPolyMgr();
			
			int numGridLocs = gridSrcProvider.getGriddedRegion().getNodeCount();
			int[] gridSeisStatus = new int[numGridLocs];
			int num = 0;
			int num0=0,num1=0,num2=0;
			double totFrac = 0;
			for(int i=0;i<numGridLocs; i++) {
				double frac = faultPolyMgr.getNodeFraction(i);
				totFrac += frac;	// fact inside fault polygons
				if(frac < 1e-6) {
					gridSeisStatus[i] = 0;
					num0 += 1;
				}
				else if (frac > 1-1e-6) {
					gridSeisStatus[i] = 1;
					num1 += 1;
				}
				else {
					gridSeisStatus[i] = 2;
					num2 += 1;
				}
					
				if(gridSrcProvider.getNodeSubSeisMFD(i) != null && gridSrcProvider.getNodeUnassociatedMFD(i) != null) {
					num += 1;
				}
			}
			System.out.println(num+"\t out of\t"+numGridLocs);
			System.out.println(num0+"\t (num0) out of\t"+numGridLocs);
			System.out.println(num1+"\t (num1) out of\t"+numGridLocs);
			System.out.println(num2+"\t (num2) out of\t"+numGridLocs);
			System.out.println("totFrac="+totFrac);
			
			long st = System.currentTimeMillis();
			CalcProgressBar progressBar = new CalcProgressBar("num to go", "junk");
				progressBar.showProgress(true);

			int numCubes = gridRegForCubes.getNodeCount();
			boolean[] insidePoly = new boolean[numCubes];
			int numBad = 0;
			for(int c=0;c<numCubes;c++) {
				progressBar.updateProgress(c, numCubes);
				Location loc = getCubeLocationForIndex(c);
				int gridIndex = gridSrcProvider.getGriddedRegion().indexForLocation(loc);
				if(gridIndex == -1)
					numBad += 1;
				else {
					if(gridSeisStatus[gridIndex] == 0)
						insidePoly[c]=false;
					else if (gridSeisStatus[gridIndex] == 1)
						insidePoly[c]=true;

					else {
						insidePoly[c]=false;
						for(int s=0; s< rupSet.getNumSections(); s++) {
							if(faultPolyMgr.getPoly(s).contains(loc)) {
								insidePoly[c] = true;
								break;
							}
						}
					}				
				}
			}
			
			int numCubesInside = 0;
			for(int c=0;c<numCubes;c++) {
				if(insidePoly[c])
					numCubesInside += 1;
			}
			System.out.println(numCubesInside+" are inside polygons, out of "+numCubes);
			System.out.println(numBad+" were bad");
			if (progressBar != null) progressBar.showProgress(false);
			
			st = System.currentTimeMillis()-st;
			float min = (float)st/(1000f*60f);
			System.out.println(" that took the following minutes: "+min);

			System.exit(-1);

			
//			FaultSystemRupSet rupSet = ((FaultSystemSolutionERF)erf).getSolution().getRupSet();
			List<FaultSectionPrefData> dataList = rupSet.getFaultSectionDataList();
			FaultSectionPrefData mojaveSubSect7_data = dataList.get(1844);
			System.out.println(mojaveSubSect7_data.getName());
			
			StirlingGriddedSurface fltSurf = mojaveSubSect7_data.getStirlingGriddedSurface(0.1, false, true);
			Hashtable<Integer,Integer> map = new Hashtable<Integer,Integer>();
			for(int r=0;r<fltSurf.getNumRows();r++) {
				for(int c=0; c<fltSurf.getNumCols();c++) {
					Location loc = fltSurf.getLocation(r, c);
					int cubeIndex = this.getCubeIndexForLocation(loc);
					Location loc2 = this.getCubeLocationForIndex(cubeIndex);
					if(loc2.getDepth() > 6.9 && loc2.getDepth() < 7.1) {	// get 7 km depth cube
//						System.out.println(cubeIndex+"\t"+loc2.getLatitude()+"\t"+loc2.getLongitude()+"\t"+loc2.getDepth()+"\t"+loc.getLatitude()+"\t"+loc.getLongitude()+"\t"+loc.getDepth());
						if(map.containsKey(cubeIndex)) {
							int newNum = map.get(cubeIndex)+1;
							map.put(cubeIndex, newNum);
						}
						else {
							map.put(cubeIndex, 1);
						}
					}
//					System.out.println(fltSurf.getLocation(7, c));
				}
			}
			
			for(int cubeIndex : map.keySet()) {
				Location loc = getCubeLocationForIndex(cubeIndex);
				System.out.println(map.get(cubeIndex)+"\t"+cubeIndex+"\t"+loc.getLatitude()+"\t"+loc.getLongitude()+"\t"+loc.getDepth());
			}
			
//			for(int i=0;i<dataList.size(); i++)
//				if(dataList.get(i).getName().contains("Mojave"))
//					System.out.println(i+"\t"+dataList.get(i).getName());
		}
		else
			throw new RuntimeException("problem");

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
//		File intListListFile = new File("junkHereFileInt");
//		File floatListListFile = new File("junkHereFileFloat");
//
//		writeMemoryUse();
//		long st = System.currentTimeMillis();
//		try {
////			List<float[]> test = MatrixIO.floatArraysListFromFile(floatListListFile);
////			List<int[]> test = loadIntegerData(intListListFile);
////			List<int[]> test = MatrixIO.intArraysListFromFile(intListListFile);
//			ArrayList<ArrayList<Integer>> test = intArraysListFromFile(intListListFile);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		if(D) System.out.println("Reading file took "+(float)(System.currentTimeMillis()-st)/60000f+ " min");
//		writeMemoryUse();
//		System.exit(0);
		
		CaliforniaRegions.RELM_TESTING_GRIDDED griddedRegion = RELM_RegionUtils.getGriddedRegionInstance();
		
		System.out.println(griddedRegion.indexForLocation(new Location(34.5,-118.0)));
		System.out.println(griddedRegion.getLocation(griddedRegion.indexForLocation(new Location(34.5,-118.0))));
		
		FaultSystemSolutionERF_ETAS erf = ETAS_Simulator.getU3_ETAS_ERF();


		if(D) System.out.println("Making ETAS_PrimaryEventSampler");
		// first make array of rates for each source
		double sourceRates[] = new double[erf.getNumSources()];
		double duration = erf.getTimeSpan().getDuration();
		for(int s=0;s<erf.getNumSources();s++)
			sourceRates[s] = erf.getSource(s).computeTotalEquivMeanAnnualRate(duration);
		boolean includeEqkRates = true;
		double gridSeisDiscr = 0.1;
		
		ETAS_PrimaryEventSampler etas_PrimEventSampler = new ETAS_PrimaryEventSampler(griddedRegion, erf, sourceRates, 
				gridSeisDiscr,null, includeEqkRates, new ETAS_Utils(), ETAS_Utils.distDecay_DEFAULT, ETAS_Utils.minDist_DEFAULT,
				true,null,null);
		
		etas_PrimEventSampler.temp();
		
		
//		etas_PrimEventSampler.getMaxMagInCubesAtDepth(7d);
//		etas_PrimEventSampler.plotMaxMagAtDepthMap(7d, "MaxMagAtDepth7km");
//		etas_PrimEventSampler.plotBulgeDepthMap(7d, "BulgeAtDepth7km");
//		etas_PrimEventSampler.plotRateAtDepthMap(7d,7.25,"RatesAboveM7pt2_AtDepth7km");
		
		
		
//		etas_PrimEventSampler.testMagFreqDist();
		
		// OLD STUFF BELOW
		
//		System.out.println("Instantiating ERF");
//		UCERF2_FM2pt1_FaultSysSolTimeDepERF erf = new UCERF2_FM2pt1_FaultSysSolTimeDepERF();
//		erf.updateForecast();
//		
//		double sourceRates[] = new double[erf.getNumSources()];
//		double duration = erf.getTimeSpan().getDuration();
//		for(int s=0;s<erf.getNumSources();s++)
//			sourceRates[s] = erf.getSource(s).computeTotalEquivMeanAnnualRate(duration);
//
////		CaliforniaRegions.RELM_TESTING_GRIDDED griddedRegion = RELM_RegionUtils.getGriddedRegionInstance();
//		
//		long startTime = System.currentTimeMillis();
//		System.out.println("Instantiating ETAS_PrimaryEventSamplerAlt");
//		
//		String testFileName = "/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_ERF/testBinaryFile";
//
//		ETAS_PrimaryEventSamplerAlt erf_RatesAtPointsInSpace = new ETAS_PrimaryEventSamplerAlt(new CaliforniaRegions.RELM_TESTING(), 5, erf, 
//				sourceRates, 24d,2d,0.1,testFileName, 2, 0.3,true,true);
//		System.out.println("Instantiating took "+(System.currentTimeMillis()-startTime)/1000+" sec");

		// TESTS
//		erf_RatesAtPointsInSpace.testRates(erf);
//		erf_RatesAtPointsInSpace.testMagFreqDist(erf);
//		erf_RatesAtPointsInSpace.plotRatesMap("test", true, "/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_ERF/mapTest");
//		erf_RatesAtPointsInSpace.plotOrigERF_RatesMap("orig test", true, "/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_ERF/mapOrigTest", erf);
//		erf_RatesAtPointsInSpace.plotRandomSampleRatesMap("random test", true, "/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_ERF/mapRandomTest", erf,10000);

		

	}
	
	/**
	 * This is a potentially more memory efficient way of reading/storing the int value, where there is
	 * only one int value/object for each index; turns out its not better than using int[] with duplicates.
	 * 
	 * Reads a file created by {@link MatrixIO.intListListToFile} or {@link MatrixIO.intArraysListToFile}
	 * into an integer array list.
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public ArrayList<ArrayList<Integer>> intArraysListFromFile(File file) throws IOException {
		Preconditions.checkNotNull(file, "File cannot be null!");
		Preconditions.checkArgument(file.exists(), "File doesn't exist!");

		long len = file.length();
		Preconditions.checkState(len > 0, "file is empty!");
		Preconditions.checkState(len % 4 == 0, "file size isn't evenly divisible by 4, " +
		"thus not a sequence of double & integer values.");

		return intArraysListFromInputStream(new FileInputStream(file));
	}

	/**
	 * Reads a file created by {@link MatrixIO.intListListToFile} or {@link MatrixIO.intArraysListToFile}
	 * into an integer array list.
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public ArrayList<ArrayList<Integer>> intArraysListFromInputStream(
			InputStream is) throws IOException {
		Preconditions.checkNotNull(is, "InputStream cannot be null!");
		if (!(is instanceof BufferedInputStream))
			is = new BufferedInputStream(is);

		DataInputStream in = new DataInputStream(is);

		int size = in.readInt();

		Preconditions.checkState(size > 0, "Size must be > 0!");

		ArrayList<ArrayList<Integer>> list = new ArrayList<ArrayList<Integer>>();
		
		ArrayList<Integer> idList = new ArrayList<Integer>();
		for(int i=0;i<totNumSrc;i++)
			idList.add(new Integer(i));
		
		for (int i=0; i<size; i++) {
			int listSize = in.readInt();
			ArrayList<Integer> intList = new ArrayList<Integer>();
			for(int j=0;j<listSize;j++)
				intList.add(idList.get(in.readInt()));
			list.add(intList);
		}

		in.close();

		return list;
	}
	
	
	public GriddedGeoDataSet getMaxMagInCubesAtDepth(double depth) {
		GriddedGeoDataSet maxMagData = new GriddedGeoDataSet(gridRegForCubes, true);
		int depthIndex = getCubeDepthIndex(depth);
		int numCubesAtDepth = maxMagData.size();
		
		CalcProgressBar progressBar = new CalcProgressBar("Looping over all points", "junk");
		progressBar.showProgress(true);
		
		double duration = erf.getTimeSpan().getDuration();
		if(mfdForSrcArray == null) {
			long st = System.currentTimeMillis();
			ETAS_SimAnalysisTools.writeMemoryUse("Memory before mfdForSrcArray");
			mfdForSrcArray = new SummedMagFreqDist[erf.getNumSources()];
			for(int s=0; s<erf.getNumSources();s++) {
				progressBar.updateProgress(s, erf.getNumSources());
				mfdForSrcArray[s] = ERF_Calculator.getTotalMFD_ForSource(erf.getSource(s), duration, 5.05, 8.95, 70, true);
			}
			ETAS_SimAnalysisTools.writeMemoryUse("Memory after mfdForSrcArray, which took (msec): "+(System.currentTimeMillis()-st));
		}

		for(int i=0; i<numCubesAtDepth;i++) {
			progressBar.updateProgress(i, numCubesAtDepth);
			int samplerIndex = getCubeIndexForRegAndDepIndices(i, depthIndex);
			int[] sources = srcInCubeList.get(samplerIndex);
			double mMax = 0;
			for(int s=0; s<sources.length;s++) {
				SummedMagFreqDist mfd = mfdForSrcArray[sources[s]];
				double tempMmax = mfd.getMaxMagWithNonZeroRate();
				if(tempMmax>mMax)
					mMax=tempMmax;
			}
			maxMagData.set(i, mMax);
			Location loc = maxMagData.getLocation(i);
//			System.out.println(loc.getLongitude()+"\t"+loc.getLatitude()+"\t"+maxMagData.get(i));
		}
		progressBar.showProgress(false);
		
		return maxMagData;
	}
	
	
	
	public GriddedGeoDataSet getBulgeInCubesAtDepth(double depth) {
		double min=5.05;
		double max = 8.95;
		int num=70;
		GriddedGeoDataSet bulgeData = new GriddedGeoDataSet(gridRegForCubes, true);
		int depthIndex = getCubeDepthIndex(depth);
		int numCubesAtDepth = bulgeData.size();
		
		CalcProgressBar progressBar = new CalcProgressBar("Looping over all points", "junk");
		progressBar.showProgress(true);
		
		double duration = erf.getTimeSpan().getDuration();
		if(mfdForSrcArray == null) {
			mfdForSrcArray = new SummedMagFreqDist[erf.getNumSources()];
			for(int s=0; s<erf.getNumSources();s++) {
				progressBar.updateProgress(s, erf.getNumSources());
				mfdForSrcArray[s] = ERF_Calculator.getTotalMFD_ForSource(erf.getSource(s), duration, min,max,num, true);
			}
		}

		for(int i=0; i<numCubesAtDepth;i++) {
			progressBar.updateProgress(i, numCubesAtDepth);
			int samplerIndex = getCubeIndexForRegAndDepIndices(i, depthIndex);
			int[] sources = srcInCubeList.get(samplerIndex);
			float[] fracts = fractionSrcInCubeList.get(samplerIndex);
			SummedMagFreqDist supraMFD = new SummedMagFreqDist(min,max,num);
			SummedMagFreqDist subMFD = new SummedMagFreqDist(min,max,num);

			for(int s=0; s<sources.length;s++) {
				SummedMagFreqDist mfd = mfdForSrcArray[sources[s]];
				IncrementalMagFreqDist mfdClone = mfd.deepClone();
				mfdClone.scale(fracts[s]);
				if(sources[s]<numFltSystSources)
					supraMFD.addIncrementalMagFreqDist(mfdClone);
				else
					subMFD.addIncrementalMagFreqDist(mfdClone);
			}
			double bulge = 1.0/ETAS_Utils.getScalingFactorToImposeGR(supraMFD, subMFD);
			if(Double.isInfinite(bulge))
				bulge = 1e3;
			bulgeData.set(i, bulge);
			Location loc = bulgeData.getLocation(i);
			System.out.println(loc.getLongitude()+"\t"+loc.getLatitude()+"\t"+bulgeData.get(i));
		}
		progressBar.showProgress(false);
		
		return bulgeData;
	}
	

	/**
	 * This computes the rate of events above the given magnitude for all cubes at a given depth
	 * @param depth
	 * @return
	 */
	public GriddedGeoDataSet getRateInCubesAtDepth(double depth, double mag) {
		double min=5.05;
		double max = 8.95;
		int num=70;
		GriddedGeoDataSet rateData = new GriddedGeoDataSet(gridRegForCubes, true);
		int depthIndex = getCubeDepthIndex(depth);
		int numCubesAtDepth = rateData.size();
		
		CalcProgressBar progressBar = new CalcProgressBar("Looping over all points", "junk");
		progressBar.showProgress(true);
		
		double duration = erf.getTimeSpan().getDuration();
		if(mfdForSrcArray == null) {
			mfdForSrcArray = new SummedMagFreqDist[erf.getNumSources()];
			for(int s=0; s<erf.getNumSources();s++) {
				progressBar.updateProgress(s, erf.getNumSources());
				mfdForSrcArray[s] = ERF_Calculator.getTotalMFD_ForSource(erf.getSource(s), duration, min,max,num, true);
			}
		}

		for(int i=0; i<numCubesAtDepth;i++) {
			progressBar.updateProgress(i, numCubesAtDepth);
			int samplerIndex = getCubeIndexForRegAndDepIndices(i, depthIndex);
			int[] sources = srcInCubeList.get(samplerIndex);
			float[] fracts = fractionSrcInCubeList.get(samplerIndex);
			double summedRate=0;
			for(int s=0; s<sources.length;s++) {
				SummedMagFreqDist srcMFD = mfdForSrcArray[sources[s]];
				int xIndex = srcMFD.getClosestXIndex(mag);
//				int xIndex = srcMFD.getXIndex(mag);
				if(xIndex >= 0 && mag<srcMFD.getMaxX()+srcMFD.getDelta()/2.0)
					summedRate += srcMFD.getCumRate(xIndex)*fracts[s];
			}
			if(summedRate == 0) 
				summedRate = 1e-16;
			rateData.set(i, summedRate);
			Location loc = rateData.getLocation(i);
//			System.out.println(loc.getLongitude()+"\t"+loc.getLatitude()+"\t"+rateData.get(i));
		}
		progressBar.showProgress(false);
		
		return rateData;
	}

	
	/**
	 * This plots the spatial distribution of probabilities implied by the given cubeSampler
	 * (probs are summed inside each spatial bin of gridRegForRatesInSpace).
	 * 
	 * @param label - plot label
	 * @param dirName
	 * @return
	 */
	public String plotSamplerMap(IntegerPDF_FunctionSampler cubeSampler, String label, String dirName) {
		
		GMT_MapGenerator mapGen = GMT_CA_Maps.getDefaultGMT_MapGenerator();
		
		CPTParameter cptParam = (CPTParameter )mapGen.getAdjustableParamsList().getParameter(GMT_MapGenerator.CPT_PARAM_NAME);
		cptParam.setValue(GMT_CPT_Files.MAX_SPECTRUM.getFileName());
		
		mapGen.setParameter(GMT_MapGenerator.MIN_LAT_PARAM_NAME,gridRegForCubes.getMinGridLat());
		mapGen.setParameter(GMT_MapGenerator.MAX_LAT_PARAM_NAME,gridRegForCubes.getMaxGridLat());
		mapGen.setParameter(GMT_MapGenerator.MIN_LON_PARAM_NAME,gridRegForCubes.getMinGridLon());
		mapGen.setParameter(GMT_MapGenerator.MAX_LON_PARAM_NAME,gridRegForCubes.getMaxGridLon());
		mapGen.setParameter(GMT_MapGenerator.GRID_SPACING_PARAM_NAME, gridRegForCubes.getLatSpacing());	// assume lat and lon spacing are same
//		mapGen.setParameter(GMT_MapGenerator.GRID_SPACING_PARAM_NAME, 0.05);	// assume lat and lon spacing are same
		mapGen.setParameter(GMT_MapGenerator.LOG_PLOT_NAME,true);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MODE_NAME,GMT_MapGenerator.COLOR_SCALE_MODE_FROMDATA);
//		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MODE_NAME,GMT_MapGenerator.COLOR_SCALE_MODE_MANUALLY);
//		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME,-3.5);
//		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME,1.5);

		//mapGen.setParameter(GMT_MapGenerator.GMT_SMOOTHING_PARAM_NAME, true);


		GriddedGeoDataSet xyzDataSet = new GriddedGeoDataSet(gridRegForCubes, true);
		
		// initialize values to zero
		for(int i=0; i<xyzDataSet.size();i++) xyzDataSet.set(i, 0);
		
		for(int i=0;i<numCubes;i++) {
			Location loc = getCubeLocationForIndex(i);
			int mapLocIndex = gridRegForCubes.indexForLocation(loc);
			if(mapLocIndex>=0) {
				double oldRate = xyzDataSet.get(mapLocIndex);
				xyzDataSet.set(mapLocIndex, cubeSampler.getY(i)+oldRate);					
			}
		}
		
		// normalize xyzDataSet (since cubeSamplers aren't necessarily normalized)
		
		// check sum
		double sum=0;
		for(int i=0; i<xyzDataSet.size();i++) sum += xyzDataSet.get(i);
		for(int i=0; i<xyzDataSet.size();i++) xyzDataSet.set(i,xyzDataSet.get(i)/sum);
		// check
//		sum=0;
//		for(int i=0; i<xyzDataSet.size();i++) sum += xyzDataSet.get(i);
//		System.out.println("sumTestForMaps="+sum);
		
		// remove any zeros because they blow up the log-plot
		System.out.println("xyzDataSet.getMinZ()="+xyzDataSet.getMinZ());
		if(xyzDataSet.getMinZ()==0) {
			double minNonZero = Double.MAX_VALUE;
			for(int i=0; i<xyzDataSet.size();i++) {
				if(xyzDataSet.get(i)>0 && xyzDataSet.get(i)<minNonZero)
					minNonZero=xyzDataSet.get(i);
			}
			for(int i=0; i<xyzDataSet.size();i++) {
				if(xyzDataSet.get(i)==0)
					xyzDataSet.set(i,minNonZero);
			}
			System.out.println("minNonZero="+minNonZero);
			System.out.println("xyzDataSet.getMinZ()="+xyzDataSet.getMinZ());
		}
		



//		System.out.println("Min & Max Z: "+xyzDataSet.getMinZ()+"\t"+xyzDataSet.getMaxZ());
		String metadata = "Map from calling plotSamplerMap(*) method";
		
		try {
				String url = mapGen.makeMapUsingServlet(xyzDataSet, "Prob from "+label, metadata, dirName);
				metadata += GMT_MapGuiBean.getClickHereHTML(mapGen.getGMTFilesWebAddress());
				ImageViewerWindow imgView = new ImageViewerWindow(url,metadata, true);		
				
				File downloadDir = new File(GMT_CA_Maps.GMT_DIR, dirName);
				if (!downloadDir.exists())
					downloadDir.mkdir();
				File zipFile = new File(downloadDir, "allFiles.zip");
				// construct zip URL
				String zipURL = url.substring(0, url.lastIndexOf('/')+1)+"allFiles.zip";
				FileUtils.downloadURL(zipURL, zipFile);
				FileUtils.unzipFile(zipFile, downloadDir);

//			System.out.println("GMT Plot Filename: "+name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "For Block Prob Map: "+mapGen.getGMTFilesWebAddress()+" (deleted at midnight)";
	}
	
	
	
	
	public String plotMaxMagAtDepthMap(double depth, String dirName) {
		
		GMT_MapGenerator mapGen = GMT_CA_Maps.getDefaultGMT_MapGenerator();
		
		CPTParameter cptParam = (CPTParameter )mapGen.getAdjustableParamsList().getParameter(GMT_MapGenerator.CPT_PARAM_NAME);
		cptParam.setValue(GMT_CPT_Files.MAX_SPECTRUM.getFileName());
		
		mapGen.setParameter(GMT_MapGenerator.MIN_LAT_PARAM_NAME,gridRegForCubes.getMinGridLat());
		mapGen.setParameter(GMT_MapGenerator.MAX_LAT_PARAM_NAME,gridRegForCubes.getMaxGridLat());
		mapGen.setParameter(GMT_MapGenerator.MIN_LON_PARAM_NAME,gridRegForCubes.getMinGridLon());
		mapGen.setParameter(GMT_MapGenerator.MAX_LON_PARAM_NAME,gridRegForCubes.getMaxGridLon());
		mapGen.setParameter(GMT_MapGenerator.GRID_SPACING_PARAM_NAME, gridRegForCubes.getLatSpacing());	// assume lat and lon spacing are same
		mapGen.setParameter(GMT_MapGenerator.LOG_PLOT_NAME,false);
//		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MODE_NAME,GMT_MapGenerator.COLOR_SCALE_MODE_FROMDATA);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MODE_NAME,GMT_MapGenerator.COLOR_SCALE_MODE_MANUALLY);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME,5.5);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME,8.5);

		GriddedGeoDataSet xyzDataSet = this.getMaxMagInCubesAtDepth(depth);
		String metadata = "Map from calling plotMaxMagAtDepthMap(*) method";
		
		try {
				String url = mapGen.makeMapUsingServlet(xyzDataSet, "Max Mag at depth="+depth, metadata, dirName);
				metadata += GMT_MapGuiBean.getClickHereHTML(mapGen.getGMTFilesWebAddress());
				ImageViewerWindow imgView = new ImageViewerWindow(url,metadata, true);		
				
				File downloadDir = new File(GMT_CA_Maps.GMT_DIR, dirName);
				if (!downloadDir.exists())
					downloadDir.mkdir();
				File zipFile = new File(downloadDir, "allFiles.zip");
				// construct zip URL
				String zipURL = url.substring(0, url.lastIndexOf('/')+1)+"allFiles.zip";
				FileUtils.downloadURL(zipURL, zipFile);
				FileUtils.unzipFile(zipFile, downloadDir);

//			System.out.println("GMT Plot Filename: "+name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "For Max Mag at depth Map: "+mapGen.getGMTFilesWebAddress()+" (deleted at midnight)";
	}

	
	
	public String plotBulgeDepthMap(double depth, String dirName) {
		
		GMT_MapGenerator mapGen = GMT_CA_Maps.getDefaultGMT_MapGenerator();
		
		CPTParameter cptParam = (CPTParameter )mapGen.getAdjustableParamsList().getParameter(GMT_MapGenerator.CPT_PARAM_NAME);
		cptParam.setValue(GMT_CPT_Files.MAX_SPECTRUM.getFileName());
		
		mapGen.setParameter(GMT_MapGenerator.MIN_LAT_PARAM_NAME,gridRegForCubes.getMinGridLat());
		mapGen.setParameter(GMT_MapGenerator.MAX_LAT_PARAM_NAME,gridRegForCubes.getMaxGridLat());
		mapGen.setParameter(GMT_MapGenerator.MIN_LON_PARAM_NAME,gridRegForCubes.getMinGridLon());
		mapGen.setParameter(GMT_MapGenerator.MAX_LON_PARAM_NAME,gridRegForCubes.getMaxGridLon());
		mapGen.setParameter(GMT_MapGenerator.GRID_SPACING_PARAM_NAME, gridRegForCubes.getLatSpacing());	// assume lat and lon spacing are same
		mapGen.setParameter(GMT_MapGenerator.LOG_PLOT_NAME,false);
//		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MODE_NAME,GMT_MapGenerator.COLOR_SCALE_MODE_FROMDATA);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MODE_NAME,GMT_MapGenerator.COLOR_SCALE_MODE_MANUALLY);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME,0d);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME,10d);

		GriddedGeoDataSet xyzDataSet = this.getBulgeInCubesAtDepth(depth);
		String metadata = "Map from calling plotBulgeDepthMap(*) method";
		
		try {
				String url = mapGen.makeMapUsingServlet(xyzDataSet, "Bulge at depth="+depth, metadata, dirName);
				metadata += GMT_MapGuiBean.getClickHereHTML(mapGen.getGMTFilesWebAddress());
				ImageViewerWindow imgView = new ImageViewerWindow(url,metadata, true);		
				
				File downloadDir = new File(GMT_CA_Maps.GMT_DIR, dirName);
				if (!downloadDir.exists())
					downloadDir.mkdir();
				File zipFile = new File(downloadDir, "allFiles.zip");
				// construct zip URL
				String zipURL = url.substring(0, url.lastIndexOf('/')+1)+"allFiles.zip";
				FileUtils.downloadURL(zipURL, zipFile);
				FileUtils.unzipFile(zipFile, downloadDir);

//			System.out.println("GMT Plot Filename: "+name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "For Bulge at depth Map: "+mapGen.getGMTFilesWebAddress()+" (deleted at midnight)";
	}

	
	/**
	 * This plots the event rates above the specified magnitude for cubes at the given depth
	 * (not including the spatial decay of any main shock)
	 * @param depth
	 * @param dirName
	 * @return
	 */
	public String plotRateAtDepthMap(double depth, double mag, String dirName) {
		
		GMT_MapGenerator mapGen = GMT_CA_Maps.getDefaultGMT_MapGenerator();
		
		CPTParameter cptParam = (CPTParameter )mapGen.getAdjustableParamsList().getParameter(GMT_MapGenerator.CPT_PARAM_NAME);
		cptParam.setValue(GMT_CPT_Files.MAX_SPECTRUM.getFileName());
		
		mapGen.setParameter(GMT_MapGenerator.MIN_LAT_PARAM_NAME,gridRegForCubes.getMinGridLat());
		mapGen.setParameter(GMT_MapGenerator.MAX_LAT_PARAM_NAME,gridRegForCubes.getMaxGridLat());
		mapGen.setParameter(GMT_MapGenerator.MIN_LON_PARAM_NAME,gridRegForCubes.getMinGridLon());
		mapGen.setParameter(GMT_MapGenerator.MAX_LON_PARAM_NAME,gridRegForCubes.getMaxGridLon());
		mapGen.setParameter(GMT_MapGenerator.GRID_SPACING_PARAM_NAME, gridRegForCubes.getLatSpacing());	// assume lat and lon spacing are same
		mapGen.setParameter(GMT_MapGenerator.LOG_PLOT_NAME,true);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MODE_NAME,GMT_MapGenerator.COLOR_SCALE_MODE_FROMDATA);
//		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MODE_NAME,GMT_MapGenerator.COLOR_SCALE_MODE_MANUALLY);
//		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME,0d);
//		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME,10d);

		GriddedGeoDataSet xyzDataSet = this.getRateInCubesAtDepth(depth, mag);
		String metadata = "Map from calling plotRateAtDepthMap(*) method";
		
		try {
				String url = mapGen.makeMapUsingServlet(xyzDataSet, "Rates at depth="+depth+" above M "+mag, metadata, dirName);
				metadata += GMT_MapGuiBean.getClickHereHTML(mapGen.getGMTFilesWebAddress());
				ImageViewerWindow imgView = new ImageViewerWindow(url,metadata, true);		
				
				File downloadDir = new File(GMT_CA_Maps.GMT_DIR, dirName);
				if (!downloadDir.exists())
					downloadDir.mkdir();
				File zipFile = new File(downloadDir, "allFiles.zip");
				// construct zip URL
				String zipURL = url.substring(0, url.lastIndexOf('/')+1)+"allFiles.zip";
				FileUtils.downloadURL(zipURL, zipFile);
				FileUtils.unzipFile(zipFile, downloadDir);

//			System.out.println("GMT Plot Filename: "+name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "For rates at depth above mag map: "+mapGen.getGMTFilesWebAddress()+" (deleted at midnight)";
	}

	/**
	 * 
	 * @param label - plot label
	 * @param local - whether GMT map is made locally or on server
	 * @param dirName
	 * @return
	 */
	public String old_plotOrigERF_RatesMap(String label, boolean local, String dirName, FaultSystemSolutionERF erf) {
		
		GMT_MapGenerator mapGen = new GMT_MapGenerator();
		mapGen.setParameter(GMT_MapGenerator.GMT_SMOOTHING_PARAM_NAME, false);
		mapGen.setParameter(GMT_MapGenerator.TOPO_RESOLUTION_PARAM_NAME, GMT_MapGenerator.TOPO_RESOLUTION_NONE);
		mapGen.setParameter(GMT_MapGenerator.MIN_LAT_PARAM_NAME,31.5);		// -R-125.4/-113.0/31.5/43.0
		mapGen.setParameter(GMT_MapGenerator.MAX_LAT_PARAM_NAME,43.0);
		mapGen.setParameter(GMT_MapGenerator.MIN_LON_PARAM_NAME,-125.4);
		mapGen.setParameter(GMT_MapGenerator.MAX_LON_PARAM_NAME,-113.0);
		mapGen.setParameter(GMT_MapGenerator.LOG_PLOT_NAME,true);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MODE_NAME,GMT_MapGenerator.COLOR_SCALE_MODE_MANUALLY);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME,-3.5);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME,1.5);


		CaliforniaRegions.RELM_TESTING_GRIDDED mapGriddedRegion = RELM_RegionUtils.getGriddedRegionInstance();
		GriddedGeoDataSet xyzDataSet = new GriddedGeoDataSet(mapGriddedRegion, true);
		
		// initialize values to zero
		for(int i=0; i<xyzDataSet.size();i++) xyzDataSet.set(i, 0);
		
		double duration = erf.getTimeSpan().getDuration();
		for(ProbEqkSource src : erf) {
			for(ProbEqkRupture rup : src) {
				LocationList locList = rup.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();
				double ptRate = rup.getMeanAnnualRate(duration)/locList.size();
				for(Location loc:locList) {
					int locIndex = mapGriddedRegion.indexForLocation(loc);
					if(locIndex>=0) {
						double oldRate = xyzDataSet.get(locIndex);
						xyzDataSet.set(locIndex, ptRate+oldRate);					
					}
				}
			}
		}
		
//		System.out.println("Min & Max Z: "+xyzDataSet.getMinZ()+"\t"+xyzDataSet.getMaxZ());
		String metadata = "no metadata";
		
		try {
			String name;
			if(local)
				name = mapGen.makeMapLocally(xyzDataSet, "Prob from "+label, metadata, dirName);
			else {
				name = mapGen.makeMapUsingServlet(xyzDataSet, "Prob from "+label, metadata, dirName);
				metadata += GMT_MapGuiBean.getClickHereHTML(mapGen.getGMTFilesWebAddress());
				ImageViewerWindow imgView = new ImageViewerWindow(name,metadata, true);				
			}

//			System.out.println("GMT Plot Filename: "+name);
		} catch (GMT_MapException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "For Block Prob Map: "+mapGen.getGMTFilesWebAddress()+" (deleted at midnight)";
	}

	/**
	 * 
	 * @param label - plot label
	 * @param local - whether GMT map is made locally or on server
	 * @param dirName
	 * @return
	 */
	public String old_plotRandomSampleRatesMap(String label, boolean local, String dirName, FaultSystemSolutionERF erf, int numYrs) {
		
		GMT_MapGenerator mapGen = new GMT_MapGenerator();
		mapGen.setParameter(GMT_MapGenerator.GMT_SMOOTHING_PARAM_NAME, false);
		mapGen.setParameter(GMT_MapGenerator.TOPO_RESOLUTION_PARAM_NAME, GMT_MapGenerator.TOPO_RESOLUTION_NONE);
		mapGen.setParameter(GMT_MapGenerator.MIN_LAT_PARAM_NAME,31.5);		// -R-125.4/-113.0/31.5/43.0
		mapGen.setParameter(GMT_MapGenerator.MAX_LAT_PARAM_NAME,43.0);
		mapGen.setParameter(GMT_MapGenerator.MIN_LON_PARAM_NAME,-125.4);
		mapGen.setParameter(GMT_MapGenerator.MAX_LON_PARAM_NAME,-113.0);
		mapGen.setParameter(GMT_MapGenerator.LOG_PLOT_NAME,true);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MODE_NAME,GMT_MapGenerator.COLOR_SCALE_MODE_MANUALLY);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME,-3.5);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME,1.5);


		CaliforniaRegions.RELM_TESTING_GRIDDED mapGriddedRegion = RELM_RegionUtils.getGriddedRegionInstance();
		GriddedGeoDataSet xyzDataSet = new GriddedGeoDataSet(mapGriddedRegion, true);
		
		// initialize values to zero
		for(int i=0; i<xyzDataSet.size();i++) xyzDataSet.set(i, 0);
		
		// get numYrs yrs worth of samples
		int numSamples = numYrs*(int)totRate;
		System.out.println("num random samples for map test = "+numSamples);
		// do this to make sure it exists
		getCubeSamplerWithERF_RatesOnly();
		
		for(int i=0;i<numSamples;i++) {
			int indexFromSampler = cubeSamplerRatesOnly.getRandomInt();
			int[] regAndDepIndex = getCubeRegAndDepIndicesForIndex(indexFromSampler);
			int indexForMap = mapGriddedRegion.indexForLocation(gridRegForCubes.locationForIndex(regAndDepIndex[0]));	// ignoring depth
			double oldNum = xyzDataSet.get(indexForMap)*numYrs;
			xyzDataSet.set(indexForMap, (1.0+oldNum)/(double)numYrs);
			
		}
		
		
//		System.out.println("Min & Max Z: "+xyzDataSet.getMinZ()+"\t"+xyzDataSet.getMaxZ());
		String metadata = "no metadata";
		
		try {
			String name;
			if(local)
				name = mapGen.makeMapLocally(xyzDataSet, "Prob from "+label, metadata, dirName);
			else {
				name = mapGen.makeMapUsingServlet(xyzDataSet, "Prob from "+label, metadata, dirName);
				metadata += GMT_MapGuiBean.getClickHereHTML(mapGen.getGMTFilesWebAddress());
				ImageViewerWindow imgView = new ImageViewerWindow(name,metadata, true);				
			}

//			System.out.println("GMT Plot Filename: "+name);
		} catch (GMT_MapException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "For Block Prob Map: "+mapGen.getGMTFilesWebAddress()+" (deleted at midnight)";
	}


}
