package scratch.UCERF3.erf.ETAS;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.FileUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.AbstractNthRupERF;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.sha.gui.infoTools.ImageViewerWindow;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.analysis.FaultBasedMapGen;
import scratch.UCERF3.analysis.FaultSystemSolutionCalc;
import scratch.UCERF3.analysis.GMT_CA_Maps;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
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
public class ETAS_PrimaryEventSampler extends CacheLoader<Integer, IntegerPDF_FunctionSampler> {
	
	private static boolean disable_cache = true;
	boolean applyGR_Corr;	// don't set here (set by constructor)
	
	final static boolean D=ETAS_Simulator.D;
	
	String defaultFractSectInCubeCacheFilename="dev/scratch/UCERF3/data/scratch/InversionSolutions/fractSectInCubeCache";
	String defaultSectInCubeCacheFilename="dev/scratch/UCERF3/data/scratch/InversionSolutions/sectInCubeCache";
	String defaultCubeInsidePolyCacheFilename="dev/scratch/UCERF3/data/scratch/InversionSolutions/cubeInsidePolyCache";
	
	
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
	InversionFaultSystemRupSet rupSet;
	FaultPolyMgr faultPolyMgr;

	
	int numPtSrcSubPts;
	double pointSrcDiscr;
	
	// this is for each cube
	double[] latForCubeCenter, lonForCubeCenter, depthForCubeCenter;
	
	// this will hold the rate of each ERF source (which can be modified by external objects)
	double sourceRates[];
	double totSectNuclRateArray[];
	ArrayList<HashMap<Integer,Float>> srcNuclRateOnSectList;
	
	// this stores the rates of erf ruptures that go unassigned (outside the region here)
//	double rateUnassigned;
	
	double totRate;
	
	List<float[]> fractionSectInCubeList;
	List<int[]> sectInCubeList;
	int[] isCubeInsideFaultPolygon;	// independent of depth, so number of elements the equal to numCubesPerDepth
	
	int[] origGridSeisTrulyOffVsSubSeisStatus;
	
	IntegerPDF_FunctionSampler cubeSamplerRatesOnly; 
	
//	IntegerPDF_FunctionSampler[] cachedSamplers;
	private LoadingCache<Integer, IntegerPDF_FunctionSampler> samplerCache;
	private long totLoadedWeight = 0;
	private static long max_weight;
	private static boolean soft_cache_values;
	
	// default maximum cache size in gigabytes. can be overridden by setting the etas.cache.size.gb property
	// this should be small if soft_cache_values = false, but can be large otherwise as cache values will be garbage
	// collected when needed
	private static double default_cache_size_gb = 8d;
	// default value for soft cache values. can be overridden by setting the etas.cache.soft property to 'true'/'false'
	private static boolean default_soft_cache_values = true;
	
	static {
		double cacheSizeGB = Double.parseDouble(System.getProperty("etas.cache.size.gb", default_cache_size_gb+""));
		max_weight = (long)(cacheSizeGB*1024d*1024d*1024d); // now in bytes
		soft_cache_values = Boolean.parseBoolean(System.getProperty("etas.cache.soft",
				default_soft_cache_values+"").toLowerCase().trim());
		if (D) System.out.println("ETAS Cache Size: "+(float)cacheSizeGB
				+" GB = "+max_weight+" bytes, soft values: "+soft_cache_values);
	}
	
	Hashtable<Integer,Integer> numForthcomingEventsForParLocIndex;  // key is the parLocIndex and value is the number of events to process TODO remove this
//	int[] numForthcomingEventsAtParentLoc;
//	int numCachedSamplers=0;
//	int incrForReportingNumCachedSamplers=100;
//	int nextNumCachedSamplers=incrForReportingNumCachedSamplers;
	
	double[] grCorrFactorForSectArray;

	// ETAS distance decay params
	double etasDistDecay, etasMinDist;
	ETAS_LocationWeightCalculator etas_LocWeightCalc;
	
	SummedMagFreqDist[] mfdForSrcArray;
	SummedMagFreqDist[] mfdForSrcSubSeisOnlyArray;
	SummedMagFreqDist[] mfdForTrulyOffOnlyArray;
	
	boolean includeERF_Rates, includeSpatialDecay;
	
	ETAS_Utils etas_utils;
	
	public static final double DEFAULT_MAX_DEPTH = 24;
	public static final double DEFAULT_DEPTH_DISCR = 2.0;
	public static final int DEFAULT_NUM_PT_SRC_SUB_PTS = 5;		// 5 is good for orig pt-src gridding of 0.1
	
	/**
	 * Constructor that uses default values
	 * @param regionForRates - the gridded region for gridded seismicity (must be same as in GridSourceGenerator)
	 * @param erf
	 * @param sourceRates
	 * @param pointSrcDiscr
	 * @param oututFileNameWithPath
	 * @param includeERF_Rates
	 * @param includeSpatialDecay
	 */
	public ETAS_PrimaryEventSampler(GriddedRegion griddedRegion, AbstractNthRupERF erf, double sourceRates[],
			double pointSrcDiscr, String oututFileNameWithPath, boolean includeERF_Rates, ETAS_Utils etas_utils,
			double etasDistDecay_q, double etasMinDist_d, boolean applyGR_Corr, List<float[]> inputFractSectInCubeList, 
			List<int[]> inputSectInCubeList,  int[] inputIsCubeInsideFaultPolygon) {

		this(griddedRegion, DEFAULT_NUM_PT_SRC_SUB_PTS, erf, sourceRates, DEFAULT_MAX_DEPTH, DEFAULT_DEPTH_DISCR, 
				pointSrcDiscr, oututFileNameWithPath, etasDistDecay_q, etasMinDist_d, includeERF_Rates, true, etas_utils,
				applyGR_Corr, inputFractSectInCubeList, inputSectInCubeList, inputIsCubeInsideFaultPolygon);
	}

	
	/**
	 * 
	 * @param griddedRegion - the gridded region for gridded seismicity (must be same as in GridSourceGenerator)
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
			List<float[]> inputFractSectInCubeList, List<int[]> inputSectInCubeList, int[] inputIsCubeInsideFaultPolygon) {
		
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
		
		
		// fill in rupSet and faultPolyMgr is erf is a FaultSystemSolutionERF
		if(erf instanceof FaultSystemSolutionERF) {
			FaultSystemRupSet fltRupSet = ((FaultSystemSolutionERF)erf).getSolution().getRupSet();
			if(fltRupSet instanceof InversionFaultSystemRupSet) {
				rupSet = (InversionFaultSystemRupSet)fltRupSet;
				faultPolyMgr = rupSet.getInversionTargetMFDs().getGridSeisUtils().getPolyMgr();	
			}
			else
				throw new RuntimeException("if erf is FaultSystemSolutionERF, its rupSet must also be an InversionFaultSystemRupSet");
		}
		else {
			rupSet = null;
			faultPolyMgr = null;				
		}

				
		
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
		
		computeGR_CorrFactorsForSections();
		
//		for(double val:grCorrFactorForCellArray)
//			System.out.println(val);
//		System.exit(-1);
		
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
//		cachedSamplers = new IntegerPDF_FunctionSampler[numParLocs];
		// this will weigh cache elements by their size
		Weigher<Integer, IntegerPDF_FunctionSampler> weigher = new Weigher<Integer, IntegerPDF_FunctionSampler>(){

			@Override
			public int weigh(Integer key, IntegerPDF_FunctionSampler value) {
				int numInts = value.getNum();
				// convert to size in bytes. each IntegerPDF_FunctionSampler has 2 double arrays of lengh numInts
				// each double value is 8 bytes
				int weight = 2*8*numInts+30; // pad for object overhead and other primitives
				totLoadedWeight += weight;
				return weight;
			}
			
		};
		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder().maximumWeight(max_weight);
		if (soft_cache_values)
			builder = builder.softValues();
		samplerCache = builder.weigher(weigher).build(this);
		
		numForthcomingEventsForParLocIndex = new Hashtable<Integer,Integer>();
		
		
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

		
		this.sourceRates = sourceRates;
		
		System.gc();
		ETAS_SimAnalysisTools.writeMemoryUse("Memory before making data");
		if(erf instanceof FaultSystemSolutionERF) {
			// create the arrays that will store section nucleation info
			totSectNuclRateArray = new double[rupSet.getNumSections()];
			// this is a hashmap for each section, which contains the source index (key) and nucleation rate (value)
			srcNuclRateOnSectList = new ArrayList<HashMap<Integer,Float>>();
			for(int sect=0;sect<rupSet.getNumSections();sect++) {
				srcNuclRateOnSectList.add(new HashMap<Integer,Float>());
			}
			FaultSystemSolutionERF fssERF = (FaultSystemSolutionERF)erf;
			for(int src=0; src<numFltSystSources; src++) {
				int fltSysRupIndex = fssERF.getFltSysRupIndexForSource(src);
				List<Integer> sectIndexList = rupSet.getSectionsIndicesForRup(fltSysRupIndex);
				for(int sect:sectIndexList) {
					srcNuclRateOnSectList.get(sect).put(src,0f);
				}
			}
			// now compute initial values for these arrays
			computeSectNucleationRates();
			System.gc();
			ETAS_SimAnalysisTools.writeMemoryUse("Memory after making data");
//			System.exit(-1);
		}


		
		
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
	

				
		origGridSeisTrulyOffVsSubSeisStatus = this.getOrigGridSeisTrulyOffVsSubSeisStatus();
		
//		int testNum=0;
//		for(int val :origGridSeisTrulyOffVsSubSeisStatus)
//			if(val == 2) testNum +=1;
//				System.out.println("TEST HERE testNum="+testNum);
		
		// read or make cache data if needed
		if(inputFractSectInCubeList!=null && inputSectInCubeList != null && inputIsCubeInsideFaultPolygon != null) {
				sectInCubeList = inputSectInCubeList;
				fractionSectInCubeList = inputFractSectInCubeList;
				isCubeInsideFaultPolygon = inputIsCubeInsideFaultPolygon;
		}
		else {
			File intListListFile = new File(defaultSectInCubeCacheFilename);
			File floatListListFile = new File(defaultFractSectInCubeCacheFilename);	
			File cubeInsidePolyFile = new File(defaultCubeInsidePolyCacheFilename);	
			if (intListListFile.exists() && floatListListFile.exists() && cubeInsidePolyFile.exists()) {
				try {
					if(D) ETAS_SimAnalysisTools.writeMemoryUse("Memory before reading fractionSrcAtPointList");
					fractionSectInCubeList = MatrixIO.floatArraysListFromFile(floatListListFile);
					if(D) ETAS_SimAnalysisTools.writeMemoryUse("Memory before reading srcAtPointList");
					sectInCubeList = MatrixIO.intArraysListFromFile(intListListFile);
					// the following test is not better in terms of memory use
					//					ArrayList<ArrayList<Integer>> test = intArraysListFromFile(intListListFile);
					if(D) ETAS_SimAnalysisTools.writeMemoryUse("Memory after reading srcAtPointList");
					isCubeInsideFaultPolygon = MatrixIO.intArrayFromFile(cubeInsidePolyFile);
					if(D) ETAS_SimAnalysisTools.writeMemoryUse("Memory after reading isCubeInsideFaultPolygon");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {  // make cache file if they don't exist
				if(D) ETAS_SimAnalysisTools.writeMemoryUse("Memory before running generateAndWriteListListDataToFile");
				generateAndWriteCacheDataToFiles();
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
	
	
	
	private void computeSectNucleationRates() {
		long st= System.currentTimeMillis();
		for(int s=0;s<totSectNuclRateArray.length;s++) {	// intialized totals to zero
			totSectNuclRateArray[s] = 0d;
		}
		//
		FaultSystemSolutionERF fssERF = (FaultSystemSolutionERF)erf;
		double[] sectNormTimeSince = fssERF.getNormTimeSinceLastForSections();
		for(int src=0; src<numFltSystSources; src++) {
			int fltSysRupIndex = fssERF.getFltSysRupIndexForSource(src);
			List<Integer> sectIndexList = rupSet.getSectionsIndicesForRup(fltSysRupIndex);
			double[] normTimeSinceOnSectArray = new double[sectIndexList.size()];
			double sum=0;
			for(int s=0;s<normTimeSinceOnSectArray.length;s++) {
				int sectIndex = sectIndexList.get(s);
				double normTS = sectNormTimeSince[sectIndex];
				// TODO should check for poisson model here
				if(Double.isNaN(normTS)) 
					normTimeSinceOnSectArray[s] = 1.0;	// assume it's 1.0
				else
					normTimeSinceOnSectArray[s]=normTS;
				sum += normTimeSinceOnSectArray[s];
			}
			for(int s=0;s<normTimeSinceOnSectArray.length;s++) {
				int sectIndex = sectIndexList.get(s);
				double sectNuclRate;
				if(sum>0)
					sectNuclRate = grCorrFactorForSectArray[sectIndex]*normTimeSinceOnSectArray[s]*sourceRates[src]/sum;
				else
					sectNuclRate = 0d;
				srcNuclRateOnSectList.get(sectIndex).put(src, (float)sectNuclRate);
				totSectNuclRateArray[sectIndex] += sectNuclRate;
				
//double tempTest = (float)sectNuclRate;
//	if(tempTest == 0) {
//		System.out.println("TEST HERE: "+sectIndex+"\t"+sum+"\t"+normTimeSinceOnSectArray[s]+"\t"+grCorrFactorForSectArray[sectIndex]
//				+"\t"+sourceRates[src]+"\t"+tempTest+"\t"+sectNuclRate+"\t"+erf.getSource(src).getName());
//	}
			}
		}
		
//		System.out.println("HERE for 1847 totSectNuclRateArray="+totSectNuclRateArray[1847]);
		
		// TESTS
		for(int sect=0;sect<rupSet.getNumSections(); sect++) {
			double testTotRate = 0;
			for(float vals:srcNuclRateOnSectList.get(sect).values()) 
				testTotRate += vals;
			double ratio = testTotRate/totSectNuclRateArray[sect];
			if(ratio<0.9999 || ratio>1.0001) {
				throw new RuntimeException("Test failed in computeSectNucleationRates(); ratio ="+ratio+" for sect "+sect);
			}
		}
		// test that nucleation rates give back source rates
		double[] testSrcRates = new double[numFltSystSources];
		for(int sectIndex=0;sectIndex<rupSet.getNumSections();sectIndex++) {
			HashMap<Integer,Float> map = srcNuclRateOnSectList.get(sectIndex);
			for(int srcIndex:map.keySet())
				testSrcRates[srcIndex] += map.get(srcIndex)/grCorrFactorForSectArray[sectIndex];
		}
		for(int srcIndex=0;srcIndex<this.numFltSystSources;srcIndex++) {
			double testRatio = testSrcRates[srcIndex]/sourceRates[srcIndex];
			if(testRatio<0.9999 || testRatio>1.0001) {
				throw new RuntimeException("Source rate test failed in computeSectNucleationRates(); testRatio ="+
			testRatio+" for srcIndex "+srcIndex+"\ntestSrcRates="+testSrcRates[srcIndex]+"\nsourceRates="+sourceRates[srcIndex]);
			}
		}
		
		double timeSec = (double)(System.currentTimeMillis()-st)/1000d;
		System.out.println("computeSectNucleationRates runtime(sec) = "+timeSec);
	}
	
	
	public void setSectInCubeCaches(List<float[]> fractionSectAtPointList, List<int[]> sectInCubeList) {
		this.fractionSectInCubeList = fractionSectAtPointList;
		this.sectInCubeList = sectInCubeList;
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
	
	private HashMap<Integer,Double> getCubesAndDistancesInsideSectionPolygon(int sectionIndex) {
		HashMap<Integer,Double> cubeDistMap = new HashMap<Integer,Double>();
		Region faultPolygon = faultPolyMgr.getPoly(sectionIndex);
		StirlingGriddedSurface surface = rupSet.getFaultSectionData(sectionIndex).getStirlingGriddedSurface(0.25, false, true);
//FaultTrace trace = surface.getFaultTrace();
//for(Location loc:trace) {
//	System.out.println(loc.getLongitude()+"\t"+loc.getLatitude());
//		}
		double lowerSeisDepth = surface.getLowerSeismogenicDepth();
		for(int i=0; i<numCubes;i++) {
			Location cubeLoc = getCubeLocationForIndex(i);
			double cubeDepthBelowSurf = cubeLoc.getDepth()-lowerSeisDepth;
			if(cubeDepthBelowSurf < 4.0 && faultPolygon.contains(cubeLoc)) {
				double dist = LocationUtils.distanceToSurf(cubeLoc, surface);
				cubeDistMap.put(i, dist);
			}
		}
		return cubeDistMap;
	}
	
	private HashMap<Integer,Float> getCubesAndFractForFaultSection(int sectionIndex) {
		HashMap<Integer,Float> wtMap = new HashMap<Integer,Float>();
		
		InversionFaultSystemSolution fss = (InversionFaultSystemSolution)((FaultSystemSolutionERF) erf).getSolution();
		
		IncrementalMagFreqDist totSectMFD = fss.getFinalTotalNucleationMFD_forSect(sectionIndex, 2.55, 8.95, 65);
		double minSupraSeisMag = fss.getFinalSubSeismoOnFaultMFD_List().get(sectionIndex).getMaxMagWithNonZeroRate() + 0.1;
		
		if(Double.isNaN(minSupraSeisMag)) {	// this happens for Mendocino sections outside the RELM region
//			System.out.println(sectionIndex+"\t"+fss.getRupSet().getFaultSectionData(sectionIndex).getName());
//			System.out.println(totSectMFD.toString());
//			System.out.println(fss.getFinalSubSeismoOnFaultMFD_List().get(sectionIndex).toString());
			return null;
		}
		
		double totSupraSeisRate = totSectMFD.getCumRate(minSupraSeisMag);
		
		IncrementalMagFreqDist trulyOffMFD = fss.getFinalTrulyOffFaultMFD().deepClone();
		trulyOffMFD.scaleToCumRate(2.55, totSectMFD.getCumRate(2.55));
		double targetRateAtMaxDist = trulyOffMFD.getCumRate(minSupraSeisMag);	// nearly the same rate as at cube outside polygon
		
		
		// solve for linear-trend values a and b (r=ad+b, where d is distance and r is rate)
		HashMap<Integer,Double> distMap = getCubesAndDistancesInsideSectionPolygon(sectionIndex);
		double n = distMap.size();
		if(targetRateAtMaxDist>totSupraSeisRate) {
			for(int cubeIndex:distMap.keySet()) {
				wtMap.put(cubeIndex, (float)(1.0/n));
			}
			return wtMap;
		}

		double dMax=0, dSum=0;
		for(int cubeIndex:distMap.keySet()) {
			double dist = distMap.get(cubeIndex);
			dSum += dist;
			if(dMax<dist)
				dMax=dist;
		}
		double a = (totSupraSeisRate-targetRateAtMaxDist)/(dSum-n*dMax);
		double b = targetRateAtMaxDist/distMap.size() - a*dMax;
		
//		System.out.println("N="+distMap.size()+"\tdMax="+dMax+"\tdSum="+dSum+"\ta="+a+"\tb="+b);
//		System.out.println("totSupraSeisRate="+totSupraSeisRate+"targetRateAtMaxDist"+targetRateAtMaxDist);
		
		for(int cubeIndex:distMap.keySet()) {
			double rate = a*distMap.get(cubeIndex) + b;
			wtMap.put(cubeIndex, (float)(rate/totSupraSeisRate));
//System.out.println(cubeIndex+"\t"+distMap.get(cubeIndex)+"\t"+wtMap.get(cubeIndex));
		}
		
		// test
		double sum=0;
		for(int cubeIndex: wtMap.keySet()) {
			double val = wtMap.get(cubeIndex);
			if(val<0) {
				System.out.println("N="+distMap.size()+"\tdMax="+dMax+"\tdSum="+dSum+"\ta="+a+"\tb="+b);
				System.out.println("totSupraSeisRate="+totSupraSeisRate+"\ttargetRateAtMaxDist"+targetRateAtMaxDist);
				System.out.println("val="+val);
				System.out.println(sectionIndex+"\t"+fss.getRupSet().getFaultSectionData(sectionIndex).getName());
				System.out.println(totSectMFD.toString());
				System.out.println(fss.getFinalSubSeismoOnFaultMFD_List().get(sectionIndex).toString());

				System.exit(-1);
			}
			else sum += val;
		}

		return wtMap;
	}
	
	
	/**
	 * This generates the following and writes them to a file:
	 * 
	 * sectInCubeList
	 * fractionSectInCubeList
	 * 
	 */
	private void generateAndWriteCacheDataToFiles() {
		if(D) System.out.println("Starting ETAS.ETAS_PrimaryEventSampler.generateAndWriteListListDataToFile(); THIS WILL TAKE TIME AND MEMORY!");
		long st = System.currentTimeMillis();
		CalcProgressBar progressBar = null;
		try {
			progressBar = new CalcProgressBar("Sources to process in ETAS_PrimaryEventSamplerAlt", "junk");
		} catch (Exception e1) {} // headless
		ArrayList<ArrayList<Integer>> sectAtPointList = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Float>> fractionsAtPointList = new ArrayList<ArrayList<Float>>();

		int numSect = rupSet.getNumSections();
		for(int i=0; i<numCubes;i++) {
			sectAtPointList.add(new ArrayList<Integer>());
			fractionsAtPointList.add(new ArrayList<Float>());
		}
		
		boolean distributeOverPolygon = true;
		double surfGridSpacing = 1.0;	// TODO decrease for greater accuracy!
		if (progressBar != null) progressBar.showProgress(true);
		for(int s=0;s<numSect;s++) {
			if (progressBar != null) progressBar.updateProgress(s, numSect);
			
			if(distributeOverPolygon) {
				HashMap<Integer,Float> cubeFractMap = getCubesAndFractForFaultSection(s);
				if(cubeFractMap != null) {	// null for Mendocino outside RELM
					for(int cubeIndex:cubeFractMap.keySet()) {
						sectAtPointList.get(cubeIndex).add(s);
						fractionsAtPointList.get(cubeIndex).add(cubeFractMap.get(cubeIndex));
					}			
				}
			}
			else {
				Hashtable<Integer,Float> fractAtPointTable = new Hashtable<Integer,Float>(); // int is ptIndex and double is fraction there
				LocationList locsOnSectSurf = rupSet.getFaultSectionData(s).getStirlingGriddedSurface(surfGridSpacing, false, true).getEvenlyDiscritizedListOfLocsOnSurface();
				//				LocationList locsOnRupSurf = src.getRupture(0).getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();	// assuming all ruptures in souce have same surface
				int numLocs = locsOnSectSurf.size();
				float ptFract = 1f/(float)numLocs;
				for(Location loc: locsOnSectSurf) {
					int regIndex = gridRegForCubes.indexForLocation(loc);
					int depIndex = getCubeDepthIndex(loc.getDepth());
					if(depIndex >= numCubeDepths) {
						depIndex = numCubeDepths-1;	// TODO
						if(D) System.out.println("Depth below max for point on "+rupSet.getFaultSectionData(s).getName()+"\t depth="+loc.getDepth());
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
				}	
				// now assign this hashTable
				for(Integer cubeIndex : fractAtPointTable.keySet()) {
					float fract = fractAtPointTable.get(cubeIndex);
					sectAtPointList.get(cubeIndex).add(s);
					fractionsAtPointList.get(cubeIndex).add(fract);
				}
			}

		}

		
		ETAS_SimAnalysisTools.writeMemoryUse("Memory before writing files");
		File intListListFile = new File(defaultSectInCubeCacheFilename);
		File floatListListFile = new File(defaultFractSectInCubeCacheFilename);
		try {
			MatrixIO.intListListToFile(sectAtPointList,intListListFile);
			MatrixIO.floatListListToFile(fractionsAtPointList, floatListListFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		// Now compute which cubes are inside polygons
		if (D) System.out.println("Starting on insidePoly calculation");
		int numCubes = gridRegForCubes.getNodeCount();
		int[] insidePoly = new int[numCubes];	// TODO default values are 0???
		
		if(erf instanceof FaultSystemSolutionERF) {	// otherwise all our outside polygons, and default values of 0 are appropriate
						
			GridSourceProvider gridSrcProvider = ((FaultSystemSolutionERF)erf).getSolution().getGridSourceProvider();
			InversionFaultSystemRupSet rupSet = (InversionFaultSystemRupSet)((FaultSystemSolutionERF)erf).getSolution().getRupSet();
			FaultPolyMgr faultPolyMgr = rupSet.getInversionTargetMFDs().getGridSeisUtils().getPolyMgr();
			int numBad = 0;
			for(int c=0;c<numCubes;c++) {
				if (progressBar != null) progressBar.updateProgress(c, numCubes);
				Location loc = getCubeLocationForIndex(c);
				int gridIndex = gridSrcProvider.getGriddedRegion().indexForLocation(loc);
				if(gridIndex == -1)
					numBad += 1;
				else {
					if(origGridSeisTrulyOffVsSubSeisStatus[gridIndex] == 0)
						insidePoly[c]=0;
					else if (origGridSeisTrulyOffVsSubSeisStatus[gridIndex] == 1)
						insidePoly[c]=1;

					else {	// check if loc is within any of the subsection polygons
						insidePoly[c]=0;
						for(int s=0; s< rupSet.getNumSections(); s++) {
							if(faultPolyMgr.getPoly(s).contains(loc)) {
								insidePoly[c] = 1;
								break;
							}
						}
					}				
				}
			}
			
			int numCubesInside = 0;
			for(int c=0;c<numCubes;c++) {
				if(insidePoly[c] == 1)
					numCubesInside += 1;
			}
			System.out.println(numCubesInside+" are inside polygons, out of "+numCubes);
			System.out.println(numBad+" were bad");
			if (progressBar != null) progressBar.showProgress(false);
			
		}
		
		if (progressBar != null) progressBar.showProgress(false);
		
		File cubeInsidePolyFile = new File(defaultCubeInsidePolyCacheFilename);
		try {
			MatrixIO.intArrayToFile(insidePoly,cubeInsidePolyFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// test
		try {
			int[] insidePoly2 = MatrixIO.intArrayFromFile(cubeInsidePolyFile);
			boolean ok = true;
			for(int i=0;i<insidePoly2.length;i++)
				if(insidePoly[i] != insidePoly2[i])
					ok = false;
			System.out.println("TEMP test result: "+ok);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(D) System.out.println("ETAS_PrimaryEventSampler.generateAndWriteListListDataToFile() took "+(System.currentTimeMillis()-st)/60000+ " min");

	}
	
	
	/**
	 * For each original gridded seismicity point, this tells: 
	 * 
	 * 0 the entire grid cell is truly off fault (outside fault polygons)
	 * 1 the entire grid cell is subseismogenic (within fault polygons)
	 * 2 a mix of the two types
	 * @return
	 */
	private int[] getOrigGridSeisTrulyOffVsSubSeisStatus() {
		GridSourceProvider gridSrcProvider = ((FaultSystemSolutionERF)erf).getSolution().getGridSourceProvider();
		InversionFaultSystemRupSet rupSet = (InversionFaultSystemRupSet)((FaultSystemSolutionERF)erf).getSolution().getRupSet();
		FaultPolyMgr faultPolyMgr = rupSet.getInversionTargetMFDs().getGridSeisUtils().getPolyMgr();
		int numGridLocs = gridSrcProvider.getGriddedRegion().getNodeCount();
		int[] gridSeisStatus = new int[numGridLocs];
		
		int num0=0,num1=0,num2=0;
		for(int i=0;i<numGridLocs; i++) {
			IncrementalMagFreqDist subSeisMFD = gridSrcProvider.getNodeSubSeisMFD(i);
			IncrementalMagFreqDist trulyOffMFD = gridSrcProvider.getNodeUnassociatedMFD(i);
			double frac = faultPolyMgr.getNodeFraction(i);
			if(subSeisMFD == null && trulyOffMFD != null) {
				gridSeisStatus[i] = 0;	// no cubes are inside; all are truly off
				num0 += 1;
				if(frac > 1e-10)	// should be 0.0
					throw new RuntimeException("Problem: frac > 1e-10");
			}
			else if (subSeisMFD != null && trulyOffMFD == null) {
				gridSeisStatus[i] = 1;	// all cubes are inside; all are subseimo
				num1 += 1;
				if(frac < 1.0 -1e-10)	// should be 1.0
					throw new RuntimeException("Problem: frac < 1.0 -1e-10");

			}
			else if (subSeisMFD != null && trulyOffMFD != null) {
				gridSeisStatus[i] = 2;	// some cubes are inside
				num2 += 1;
				if(frac ==0 || frac == 1) {
					System.out.println("Location:\t"+origGriddedRegion.getLocation(i));
					System.out.println("subSeisMFD:\n"+subSeisMFD.toString());
					System.out.println("trulyOffMFD:\n"+trulyOffMFD.toString());
					throw new RuntimeException("Problem: frac ==0 || frac == 1; "+frac);
				}
			}
			else {
				throw new RuntimeException("Problem");
			}
			
		}
		if(D) {
			System.out.println(num0+"\t (num0) out of\t"+numGridLocs);
			System.out.println(num1+"\t (num1) out of\t"+numGridLocs);
			System.out.println(num2+"\t (num2) out of\t"+numGridLocs);
		}
		return gridSeisStatus;
	}
	


	
	/**
	 * This loops over all points on the rupture surface and creates a net (average) point sampler.
	 * @param mainshock
	 * @return
	 */
	public IntegerPDF_FunctionSampler getAveSamplerForRupture(EqkRupture mainshock) {
		long st = System.currentTimeMillis();
		IntegerPDF_FunctionSampler aveSampler = new IntegerPDF_FunctionSampler(numCubes);
		
		LocationList locList = mainshock.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();
		for(Location loc: locList) {
			
			// set the sampler
			int parLocIndex = getParLocIndexForLocation(loc);
			IntegerPDF_FunctionSampler sampler = getCubeSampler(parLocIndex, false);
			
			for(int i=0;i <numCubes;i++) {
				aveSampler.add(i, sampler.getY(i));
			}
		}
		if(D) {
			double sec = ((double)(System.currentTimeMillis()-st))/1000d;
			System.out.println("getAveSamplerForRupture() took (sec): "+(float)sec);
		}
		return aveSampler;
	}
	
	public void tempAveSamplerAtFaults(EqkRupture mainshock) {
		IntegerPDF_FunctionSampler aveSampler = getAveSamplerForRupture(mainshock);
		
		double fltNuclRate[] = new double[numCubes];
		double totCubeProb[] = new double[numCubes];
		
		for(int c=0;c<numCubes;c++) {
			fltNuclRate[c] = 0d;
			if(sectInCubeList.get(c).length > 0) {
				int[] sectInCube = sectInCubeList.get(c);
				float[] fractInCube = fractionSectInCubeList.get(c);				
				for(int s=0; s<sectInCube.length;s++)
					fltNuclRate[c] += totSectNuclRateArray[sectInCube[s]]*fractInCube[s];
			}
			totCubeProb[c] = fltNuclRate[c]*aveSampler.getY(c);
		}
		
		
		// get top-prob cubes
		int[] topCubeIndices = ETAS_SimAnalysisTools.getIndicesForHighestValuesInArray(totCubeProb, 50);
		System.out.print("cubeIndex\ttotFltProb\tcubeProb\tgrdSeisRate\tfltNuclRate\tlat\tlon\tdepth\tsect data...");
		for(int cubeIndex : topCubeIndices) {
			double gridSeisRateInCube = this.getGridSourcRateInCube(cubeIndex);
			Location loc = getCubeLocationForIndex(cubeIndex);
			int[] sectInCube = sectInCubeList.get(cubeIndex);
			float[] fractInCube = fractionSectInCubeList.get(cubeIndex);
			System.out.print(cubeIndex+"\t"+totCubeProb[cubeIndex]+"\t"+aveSampler.getY(cubeIndex)+"\t"+gridSeisRateInCube+"\t"+fltNuclRate[cubeIndex]+
					"\t"+loc.getLongitude()+"\t"+loc.getLatitude()+"\t"+loc.getDepth());
			
			List<FaultSectionPrefData> fltDataList = ((FaultSystemSolutionERF)erf).getSolution().getRupSet().getFaultSectionDataList();
			for(int s=0;s<sectInCube.length;s++) {
				int sectIndex = sectInCube[s];
				double sectRate = totSectNuclRateArray[sectIndex]*fractInCube[s];
				System.out.print("\t"+fltDataList.get(sectIndex).getName()+"\t"+totSectNuclRateArray[sectIndex]+"\t"+fractInCube[s]);
			}
			System.out.print("\n");
		}
		
		System.out.print(getCubeMFD(426462).toString());
		Location loc = getCubeLocationForIndex(426462);
		Location newLoc = new Location(loc.getLatitude()+0.04,loc.getLongitude()-0.04,loc.getDepth());
		System.out.print(newLoc.toString());
		System.out.print(getCubeMFD(getCubeIndexForLocation(newLoc)).toString());

		
	}

	
	
	/**
	 *  This returns the relative probability of triggering each source that exists within the cube.
	 *  null is returned if no sources exist in cube.
	 * @param cubeIndex
	 * @return Hashtable where key is src index and value is relative probability
	 */
	public Hashtable<Integer,Double> getRelativeTriggerProbOfSourcesInCube(int cubeIndex) {
		Hashtable<Integer,Double> probForSrcHashtable = getNucleationRatesOfSourcesInCube(cubeIndex);
		
		if(probForSrcHashtable == null) {
			return null;
		}
		else {
			//Normalize rates to relative probabilities
			double sum=0;
			for(int srcIndex:probForSrcHashtable.keySet())
				sum += probForSrcHashtable.get(srcIndex);
			for(int srcIndex:probForSrcHashtable.keySet()) {
				double normVal = probForSrcHashtable.get(srcIndex)/sum;
				probForSrcHashtable.put(srcIndex, normVal);
			}
			
			if(D) {	// test that sum equals 1.0
				double testVal = 0;
				for(int srcIndex:probForSrcHashtable.keySet())
					testVal += probForSrcHashtable.get(srcIndex);
				if(testVal<0.9999 || testVal>1.0001)
					throw new RuntimeException("PROBLEM");				
			}

			return probForSrcHashtable;
		}
	}
	
	
	
	
	
	/**
	 *  This returns the nucleation rate of each source that exists within the cube.
	 *  null is returned if no sources exist in cube.
	 * @param cubeIndex
	 * @return Hashtable where key is src index and value is rate
	 */
	public Hashtable<Integer,Double> getNucleationRatesOfSourcesInCube(int cubeIndex) {
		Hashtable<Integer,Double> rateForSrcHashtable = new Hashtable<Integer,Double>();
		
		// compute nucleation rate of gridded-seis source in this cube
		int gridSrcIndex = -1;
		double gridSrcRate=0;
		int griddeSeisRegionIndex = origGriddedRegion.indexForLocation(getCubeLocationForIndex(cubeIndex));
		if(griddeSeisRegionIndex != -1)	{
			gridSrcIndex = numFltSystSources + griddeSeisRegionIndex;
			gridSrcRate = sourceRates[gridSrcIndex]/(numPtSrcSubPts*numPtSrcSubPts*numCubeDepths);	// divide rate among all the cubes in grid cell
		}
		
		int[] sectInCubeArray = sectInCubeList.get(cubeIndex);
		
		if(gridSrcIndex == -1 && sectInCubeArray.length==0) {
			return null;
		}
		
		if(gridSrcIndex != -1 && sectInCubeArray.length==0) {
			rateForSrcHashtable.put(gridSrcIndex, gridSrcRate);	// only gridded source in this cube
			return rateForSrcHashtable;
		}
		
		if(gridSrcIndex != -1) 
			rateForSrcHashtable.put(gridSrcIndex, gridSrcRate);	// add gridded source rate
		
		// now fill in nucleation rate of remaining sources
		float[] fracts = fractionSectInCubeList.get(cubeIndex);
		for(int s=0;s<sectInCubeArray.length;s++) {
			int sectIndex = sectInCubeArray[s];
			double fracSectInCube = fracts[s];
			HashMap<Integer,Float> srcRateOnSectMap = srcNuclRateOnSectList.get(sectIndex);
			for(int srcIndex:srcRateOnSectMap.keySet()) {
				double srcNuclRateInCube = srcRateOnSectMap.get(srcIndex)*fracSectInCube;
				if(rateForSrcHashtable.containsKey(srcIndex)) {
					double newRate = rateForSrcHashtable.get(srcIndex) + srcNuclRateInCube;
					rateForSrcHashtable.put(srcIndex, newRate);
				}
				else {
					rateForSrcHashtable.put(srcIndex, srcNuclRateInCube);
				}
			}
		}
		
		return rateForSrcHashtable;
	}

	
	
	/**
	 * For the given main shock, this gives the relative trigger probability of each source in the ERF.
	 * This loops over all points on the main shock rupture surface, giving equal triggering
	 * potential to each.
	 * @param mainshock
	 * @return double[] the relative triggering probability of each ith source
	 */
	public double[] getRelativeTriggerProbOfEachSource(EqkRupture mainshock) {
//		long st = System.currentTimeMillis();
		double[] trigProb = new double[erf.getNumSources()];
		
		IntegerPDF_FunctionSampler aveSampler = getAveSamplerForRupture(mainshock);

		// normalize so values sum to 1.0
		aveSampler.scale(1.0/aveSampler.getSumOfY_vals());
		
		// now loop over all cubes
		for(int i=0;i <numCubes;i++) {
			Hashtable<Integer,Double>  relSrcProbForCube = getRelativeTriggerProbOfSourcesInCube(i);
			if(relSrcProbForCube != null) {
				for(int srcKey:relSrcProbForCube.keySet()) {
					trigProb[srcKey] += aveSampler.getY(i)*relSrcProbForCube.get(srcKey);
				}
			}
//			else {
//				// I confirmed that all of these are around the edges
//				Location loc = getCubeLocationForIndex(i);
//				System.out.println("relSrcProbForCube is null for cube index "+i+"\t"+loc.getLongitude()+"\t"+loc.getLatitude()+"\t"+loc.getDepth());
//			}
		}

		double testSum=0;
		for(int s=0; s<trigProb.length; s++)
			testSum += trigProb[s];
		if(testSum<0.9999 || testSum>1.0001)
			throw new RuntimeException("PROBLEM");
		
//		st = System.currentTimeMillis()-st;
//		System.out.println("###########"+((float)st/1000f)+" sec");
		
		return trigProb;
	}
	
	
	/**
	 * 
	 * @param mainshock
	 * @return ArrayList<SummedMagFreqDist>; index 0 has total MFD, and index 1 has supra-seis MFD
	 */
	public List<SummedMagFreqDist> getExpectedPrimaryMFD_PDF(EqkRupture mainshock) {
		double[] srcProbs = getRelativeTriggerProbOfEachSource(mainshock);
		SummedMagFreqDist magDist = new SummedMagFreqDist(2.05, 8.95, 70);
		SummedMagFreqDist supraMagDist = new SummedMagFreqDist(2.05, 8.95, 70);
		for(int s=0; s<srcProbs.length;s++) {
			SummedMagFreqDist srcMFD = ERF_Calculator.getTotalMFD_ForSource(erf.getSource(s), 1.0, 2.05, 8.95, 70, true);
			srcMFD.normalizeByTotalRate();
			srcMFD.scale(srcProbs[s]);
			if(!Double.isNaN(srcMFD.getTotalIncrRate())) {// not sure why this is needed
				magDist.addIncrementalMagFreqDist(srcMFD);
				if(s<numFltSystSources)
					supraMagDist.addIncrementalMagFreqDist(srcMFD);
			}
		}
		ArrayList<SummedMagFreqDist> mfdList = new ArrayList<SummedMagFreqDist>();
		mfdList.add(magDist);
		mfdList.add(supraMagDist);

		return mfdList;
	}
	
	
	/**
	 * 
	 * @param mainshock
	 * @return ArrayList<SummedMagFreqDist>; index 0 has total MFD, and index 1 has supra-seis MFD
	 */
	public List<SummedMagFreqDist> getExpectedPrimaryMFD_PDF_Alt(EqkRupture mainshock) {
		IntegerPDF_FunctionSampler aveCubeSampler = getAveSamplerForRupture(mainshock);
		// normalize so values sum to 1.0
		aveCubeSampler.scale(1.0/aveCubeSampler.getSumOfY_vals());

		SummedMagFreqDist magDist = new SummedMagFreqDist(2.05, 8.95, 70);
		SummedMagFreqDist supraMagDist = new SummedMagFreqDist(2.05, 8.95, 70);
		for(int i=0;i<aveCubeSampler.getNum(); i++) {
			SummedMagFreqDist mfd = getCubeMFD(i);
			if(mfd != null) {
				double total = mfd.getTotalIncrRate();
				mfd.scale(aveCubeSampler.getY(i)/total);
				magDist.addIncrementalMagFreqDist(mfd);
				SummedMagFreqDist mfdSupra = getCubeMFD_SupraSeisOnly(i);
				if(mfdSupra != null) {
					mfdSupra.scale(aveCubeSampler.getY(i)/total);
					supraMagDist.addIncrementalMagFreqDist(mfdSupra);
				}
			}
		}
		
		ArrayList<SummedMagFreqDist> mfdList = new ArrayList<SummedMagFreqDist>();
		mfdList.add(magDist);
		mfdList.add(supraMagDist);

		return mfdList;
	}

	
	/**
	 * This plots the relative probability that each subsection will trigger a
	 * supra-seis primary aftershocks, given one has been triggered.  
	 * 
	 * This could also return a String with a list of the top numToList fault-based 
	 * sources and top sections (see next method below).
	 */
	public String plotSubSectRelativeTriggerProbGivenSupraSeisRupture(ETAS_EqkRupture mainshock, File resultsDir, int numToList, String nameSuffix) {
		String info = "";
		if(erf instanceof FaultSystemSolutionERF) {
			FaultSystemSolutionERF tempERF = (FaultSystemSolutionERF)erf;

			IntegerPDF_FunctionSampler aveSampler = getAveSamplerForRupture(mainshock);

			// normalize so values sum to 1.0
			aveSampler.scale(1.0/aveSampler.getSumOfY_vals());

			double[] sectProbArray = new double[rupSet.getNumSections()];

			// now loop over all cubes
			for(int i=0;i <numCubes;i++) {
				int[] sectInCubeArray = sectInCubeList.get(i);
				float[] fractInCubeArray = fractionSectInCubeList.get(i);
				for(int s=0;s<sectInCubeArray.length;s++) {
					int sectIndex = sectInCubeArray[s];
					sectProbArray[sectIndex] += totSectNuclRateArray[sectIndex]*fractInCubeArray[s]*aveSampler.getY(i);
				}
			}

			// normalize:
			double sum=0;
			for(double val:sectProbArray)
				sum += val;
			double min=Double.MAX_VALUE, max=0.0;
			for(int sect=0;sect<sectProbArray.length;sect++) {
				sectProbArray[sect] /= sum;
				if(sectProbArray[sect]<1e-16)
					sectProbArray[sect]=1e-16;
				//				if(min>sectProbArray[sect])
				//					min=sectProbArray[sect];
				//				if(max<sectProbArray[sect])
				//					max=sectProbArray[sect];
			}
			//			min = Math.floor(Math.log10(min));
			//			max = Math.ceil(Math.log10(max));
			min = -5;
			max = 0;
			CPT cpt = FaultBasedMapGen.getParticipationCPT().rescale(min, max);;
			List<FaultSectionPrefData> faults = rupSet.getFaultSectionDataList();

			//			// now log space
			double[] values = FaultBasedMapGen.log10(sectProbArray);


			String name = "SectRelativeTriggerProb"+nameSuffix;
			String title = "Log10(Relative Trigger Prob)";
			// this writes all the value to a file
			try {
				FileWriter fr = new FileWriter(new File(resultsDir, name+".txt"));
				for(int s=0; s<sectProbArray.length;s++) {
					fr.write(s +"\t"+(float)sectProbArray[s]+"\t"+faults.get(s).getName()+"\n");
				}
				fr.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}


			try {
				FaultBasedMapGen.makeFaultPlot(cpt, FaultBasedMapGen.getTraces(faults), values, origGriddedRegion, resultsDir, name, true, false, title);
			} catch (Exception e) {
				e.printStackTrace();
			} 

			// list top sections
			int[] topValueIndices = ETAS_SimAnalysisTools.getIndicesForHighestValuesInArray(sectProbArray, numToList);
			info += "\nThe following sections are most likely to be triggered:\n\n";
			List<FaultSectionPrefData> fltDataList = tempERF.getSolution().getRupSet().getFaultSectionDataList();
			for(int sectIndex : topValueIndices) {
				info += "\t"+sectProbArray[sectIndex]+"\t"+sectIndex+"\t"+fltDataList.get(sectIndex).getName()+"\n";
			}
			
			return info;
		}
		else {
			throw new RuntimeException("erf must be instance of FaultSystemSolutionERF");
		}



	}
			
			
	/**
	 * This plots the relative probability that each subsection will participate
	 * given a primary aftershocks of the supplied mainshock.  This also returns
	 * a String with a list of the top numToList fault-based sources and top sections.
	 */
	public String plotSubSectParticipationProbGivenRuptureAndReturnInfo(ETAS_EqkRupture mainshock, File resultsDir, int numToList, String nameSuffix) {
		String info = "";
		if(erf instanceof FaultSystemSolutionERF) {
			FaultSystemSolutionERF tempERF = (FaultSystemSolutionERF)erf;
			double[] srcProbs = getRelativeTriggerProbOfEachSource(mainshock);
			double[] sectProbArray = new double[rupSet.getNumSections()];
			for(int srcIndex=0; srcIndex<numFltSystSources; srcIndex++) {
				int fltSysIndex = tempERF.getFltSysRupIndexForSource(srcIndex);
				for(Integer sectIndex:rupSet.getSectionsIndicesForRup(fltSysIndex)) {
					sectProbArray[sectIndex] += srcProbs[srcIndex];
				}
			}			
			
			CPT cpt = FaultBasedMapGen.getParticipationCPT().rescale(-8, -3);;
			List<FaultSectionPrefData> faults = rupSet.getFaultSectionDataList();

//			// now log space
			double[] values = FaultBasedMapGen.log10(sectProbArray);
			
			
			String name = "SectPrimaryParticipationProb"+nameSuffix;
			String title = "Log10(Primary Participation Prob)";
			// this writes all the value to a file
			try {
				FileWriter fr = new FileWriter(new File(resultsDir, name+".txt"));
				for(int s=0; s<sectProbArray.length;s++) {
					fr.write(s +"\t"+(float)sectProbArray[s]+"\t"+faults.get(s).getName()+"\n");
				}
				fr.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			try {
				FaultBasedMapGen.makeFaultPlot(cpt, FaultBasedMapGen.getTraces(faults), values, origGriddedRegion, resultsDir, name, true, false, title);
			} catch (Exception e) {
				e.printStackTrace();
			} 
			
			// write out probability of mainshock if it's a fault system source
			if(mainshock.getFSSIndex() >=0 && erf instanceof FaultSystemSolutionERF) {
				info += "\nProbability of sampling the scenarioRup:\n";
				int srcIndex = ((FaultSystemSolutionERF)erf).getSrcIndexForFltSysRup(mainshock.getFSSIndex());
				info += "\n\t"+srcProbs[srcIndex]+"\t"+srcIndex+"\t"+erf.getSource(srcIndex).getName()+"\n";
			}
			
			
			// below creates the string of top values; first sources, then sections			
			
			// list top fault-based ruptures
			double[] fltSrcProbs = new double[this.numFltSystSources];
			for(int i=0;i<fltSrcProbs.length;i++)
				fltSrcProbs[i]=srcProbs[i];
			int[] topValueIndices = ETAS_SimAnalysisTools.getIndicesForHighestValuesInArray(fltSrcProbs, numToList);
			info += "\nScenario is most likely to trigger the following fault-based sources (prob, srcIndex, mag, name):\n\n";
			for(int srcIndex : topValueIndices) {
				info += "\t"+fltSrcProbs[srcIndex]+"\t"+srcIndex+"\t"+erf.getSource(srcIndex).getRupture(0).getMag()+"\t"+erf.getSource(srcIndex).getName()+"\n";
			}
			
			// list top fault section participations
			topValueIndices = ETAS_SimAnalysisTools.getIndicesForHighestValuesInArray(sectProbArray, numToList);
			info += "\nThe following sections are most likely to participate in a triggered event:\n\n";
			List<FaultSectionPrefData> fltDataList = tempERF.getSolution().getRupSet().getFaultSectionDataList();
			for(int sectIndex :topValueIndices) {
				info += "\t"+sectProbArray[sectIndex]+"\t"+sectIndex+"\t"+fltDataList.get(sectIndex).getName()+"\n";
			}

		}
		else {
			throw new RuntimeException("erf must be instance of FaultSystemSolutionERF");
		}
		return info;
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
	 * @param rupToFillIn
	 */
	public boolean setRandomPrimaryEvent(ETAS_EqkRupture rupToFillIn) {
		
		EqkRupture parRup = rupToFillIn.getParentRup();
		
		// get the location on the parent that does the triggering
		Location actualParentLoc=rupToFillIn.getParentTriggerLoc();
			
		// Check for problem region index
		int parRegIndex = gridRegForParentLocs.indexForLocation(actualParentLoc);
		if(parRegIndex <0) {
			if(D) {
				System.out.print("Warning: parent location outside of region; parRegIndex="+parRegIndex+"; parentLoc="+actualParentLoc.toString()+
						"; Num pts on main shock surface: "+parRup.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface().size());
				if(parRup instanceof ETAS_EqkRupture) {
					System.out.println("; Problem event generation: "+((ETAS_EqkRupture)parRup).getGeneration());
				}
				else {
					System.out.println(" ");
				}
			}
			return false;
		}

////System.out.println("actualParentLoc: "+actualParentLoc);
////System.out.println("parDepIndex: "+getParDepthIndex(actualParentLoc.getDepth()));
////System.out.println("getParDepth(parDepIndex): "+getParDepth(parDepIndex));
////System.out.println("translatedParLoc: "+translatedParLoc);
////System.exit(0);
		
		int parLocIndex = getParLocIndexForLocation(actualParentLoc);
		Location translatedParLoc = getParLocationForIndex(parLocIndex);
		IntegerPDF_FunctionSampler sampler = getCubeSampler(parLocIndex, true);
		
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
		
		if(randSrcIndex < numFltSystSources) {	// if it's a fault system source
			ProbEqkSource src = erf.getSource(randSrcIndex);
			int r=0;
			if(src.getNumRuptures() > 1) {
				r = src.drawSingleRandomEqkRuptureIndex(etas_utils.getRandomDouble());
			}
			int nthRup = erf.getIndexN_ForSrcAndRupIndices(randSrcIndex,r);
			ProbEqkRupture erf_rup = src.getRupture(r);
			// need to choose point on rup surface that is the hypocenter			
			LocationList locsOnRupSurf = erf_rup.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();
			
//			// collect those inside the cube and choose one randomly
//			LocationList locsToSampleFrom = new LocationList();
//			for(Location loc: locsOnRupSurf) {
//				if(aftShCubeIndex == getCubeIndexForLocation(loc)) {
//					locsToSampleFrom.add(loc);
//				}
//			}	
//			if(locsToSampleFrom.size() == 0) {
//				System.out.println("PROBLEM: randSrcIndex="+randSrcIndex+"\tName: = "+src.getName());
//				System.out.println("lat\tlon\tdepth");
//				System.out.println(latForCubeCenter[aftShCubeIndex]+"\t"+lonForCubeCenter[aftShCubeIndex]+"\t"+depthForCubeCenter[aftShCubeIndex]);
//				for(Location loc: locsOnRupSurf) {
//					System.out.println(loc.getLatitude()+"\t"+loc.getLongitude()+"\t"+loc.getDepth());
//				}	
//				throw new RuntimeException("problem");
//			}
//			// choose one randomly
//			int hypoLocIndex = etas_utils.getRandomInt(locsToSampleFrom.size()-1);
//			rupToFillIn.setHypocenterLocation(locsToSampleFrom.get(hypoLocIndex));
			
			// choose the closest point on surface as the hypocenter
			Location hypoLoc=null;
			Location cubeLoc= getCubeLocationForIndex(aftShCubeIndex);
			double minDist = Double.MAX_VALUE;
			for(Location loc:locsOnRupSurf) {
				double dist = LocationUtils.linearDistanceFast(cubeLoc, loc);
				if(dist<minDist) {
					hypoLoc = loc;
					minDist = dist;
				}	
			}
			rupToFillIn.setHypocenterLocation(hypoLoc);

			rupToFillIn.setRuptureSurface(erf_rup.getRuptureSurface());
			rupToFillIn.setFSSIndex(((FaultSystemSolutionERF)erf).getFltSysRupIndexForNthRup(nthRup));
			rupToFillIn.setAveRake(erf_rup.getAveRake());
			rupToFillIn.setMag(erf_rup.getMag());
			rupToFillIn.setNthERF_Index(nthRup);

		}
		else { // it's a gridded seis source
			
			int gridRegionIndex = randSrcIndex-numFltSystSources;
			ProbEqkSource src=null;
			if(origGridSeisTrulyOffVsSubSeisStatus[gridRegionIndex] == 2) {	// it has both truly off and sub-seismo components
				// get
				int[] regAndDepIndex = getCubeRegAndDepIndicesForIndex(aftShCubeIndex);
				int isSubSeismo = isCubeInsideFaultPolygon[regAndDepIndex[0]];
				if(isSubSeismo == 1) {
					src = ((FaultSystemSolutionERF)erf).getSourceSubSeisOnly(randSrcIndex);
				}
				else {
					src = ((FaultSystemSolutionERF)erf).getSourceTrulyOffOnly(randSrcIndex);
				}
			}
			else {
				src = erf.getSource(randSrcIndex);
			}
			
			
			int r=0;
			if(src.getNumRuptures() > 1) {
				r = src.drawSingleRandomEqkRuptureIndex(etas_utils.getRandomDouble());
			}
			int nthRup = erf.getIndexN_ForSrcAndRupIndices(randSrcIndex,r);
			ProbEqkRupture erf_rup = src.getRupture(r);

			double relLat = latForCubeCenter[aftShCubeIndex]-translatedParLoc.getLatitude();
			double relLon = lonForCubeCenter[aftShCubeIndex]-translatedParLoc.getLongitude();
			double relDep = depthForCubeCenter[aftShCubeIndex]-translatedParLoc.getDepth();	// TODO why not used (remove or bug?)
			
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
					// check whether hypLoc is now out of region, and return false if so
					if(testIndex == -1)
						return false;
					IncrementalMagFreqDist mfd = tempERF.getSolution().getGridSourceProvider().getNodeMFD(testIndex);
//					if(mfd==null) {
//						throw new RuntimeException("testIndex="+testIndex+"\thypLoc= "+hypLoc+"\tgridLoc= "+tempERF.getSolution().getGridSourceProvider().getGriddedRegion().getLocation(testIndex));
//					}
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
			// fill in the rest
			rupToFillIn.setAveRake(erf_rup.getAveRake());
			rupToFillIn.setMag(erf_rup.getMag());
			rupToFillIn.setNthERF_Index(nthRup);

		}
		
		
		// distance of triggered event from parent
		double distToParent = LocationUtils.linearDistanceFast(actualParentLoc, rupToFillIn.getHypocenterLocation());
		rupToFillIn.setDistanceToParent(distToParent);
		
		return true;
	}
	
	
	
	private IntegerPDF_FunctionSampler getCubeSampler(int locIndexForPar, boolean updateForthcomingEvents) {
		
		if(updateForthcomingEvents) {
			int numLeft = numForthcomingEventsForParLocIndex.get(locIndexForPar) - 1;
			if(numLeft == 0) {
				numForthcomingEventsForParLocIndex.remove(locIndexForPar);
			}
			else {
				numForthcomingEventsForParLocIndex.put(locIndexForPar, numLeft);
			}			
		}
		
		if (disable_cache || max_weight <= 0l) {
			try {
				return load(locIndexForPar);
			} catch (Exception e) {
				throw ExceptionUtils.asRuntimeException(e);
			}
		}
//		System.out.println("Loaded with cache size "+samplerCache.size()+", weight: "+totLoadedWeight+"/"+max_weight);
		
//		System.out.print(numForthcomingEventsForParLocIndex.size()+", ");
		try {
			// get it from the cache, loading the value via load(key) below if necessary
			return samplerCache.get(locIndexForPar);
		} catch (Throwable e) {
//			Throwable subE = e.getCause();
//			while (subE != null) {
//				if (e.getCause() != null) {
//					System.out.println("Cause:");
//					e.printStackTrace();
//				}
//			}
			throw ExceptionUtils.asRuntimeException(e);
		}
	}
	
	
	

	/**
	 * This will force updating of all the samplers and other things
	 */
	public void declareRateChange() {
		if(D)ETAS_SimAnalysisTools.writeMemoryUse("Memory before discarding chached Samplers");
		cubeSamplerRatesOnly = null;
		computeSectNucleationRates();
		// clear the cache
		samplerCache.invalidateAll();
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
	 * The commented out elements below show that we can't ignore supraseismogenic events
	 * in these rates due to extreme characteristic MFDs in some locations (in part because
	 * gridded seis rates are spread but fault rates arent)
	 */
	private synchronized IntegerPDF_FunctionSampler getCubeSamplerWithERF_RatesOnly() {
		if(cubeSamplerRatesOnly == null) {
			cubeSamplerRatesOnly = new IntegerPDF_FunctionSampler(numCubes);
//			double maxTest = 0;
//			int testCubeIndex = -1;
			for(int i=0;i<numCubes;i++) {
				// compute rate of gridded-seis source in this cube
				double gridSrcRate=0.0;
				int griddeSeisRegionIndex = origGriddedRegion.indexForLocation(getCubeLocationForIndex(i));
				if(griddeSeisRegionIndex != -1)	 {
					int gridSrcIndex = numFltSystSources + griddeSeisRegionIndex;
					gridSrcRate = sourceRates[gridSrcIndex]/(numPtSrcSubPts*numPtSrcSubPts*numCubeDepths);	// divide rate among all the cubes in grid cell
				}
				double totRate= gridSrcRate; // start with gridded seis rate
				int[] sections = sectInCubeList.get(i);
				float[] fracts = fractionSectInCubeList.get(i);
				for(int j=0; j<sections.length;j++) {
//					if(Double.isNaN(totSectNuclRateArray[sections[j]])) throw new RuntimeException("totSectNuclRateArray");
//					if(Float.isNaN(fracts[j])) throw new RuntimeException("fracts");
					totRate += totSectNuclRateArray[sections[j]]*(double)fracts[j];
				}
				if(!Double.isNaN(totRate)) {
					cubeSamplerRatesOnly.set(i,totRate);
				}
				else {
//					Location loc = this.getCubeLocationForIndex(i);
//					System.out.println("NaN\t"+loc.getLongitude()+"\t"+loc.getLatitude()+"\t"+loc.getDepth());
					throw new RuntimeException("totRate="+Double.NaN+" for cube "+i+"; "+this.getCubeLocationForIndex(i));
				}
			}
// System.out.println("HERE maxTest="+maxTest+"\ttestCubeIndex="+testCubeIndex+"\tloc: "+this.getCubeLocationForIndex(testCubeIndex));
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
	 * This returns the gridded seismicity nucleation rate inside the given cube.
	 * Double.NaN is returned if there is no associated gridded seismicity cell
	 * @param cubeIndex
	 * @return
	 */
	public double getGridSourcRateInCube(int cubeIndex) {
		int griddeSeisRegionIndex = origGriddedRegion.indexForLocation(getCubeLocationForIndex(cubeIndex));
		if(griddeSeisRegionIndex != -1) {
			int gridSrcIndex = numFltSystSources + griddeSeisRegionIndex;
			return sourceRates[gridSrcIndex]/(numPtSrcSubPts*numPtSrcSubPts*numCubeDepths);	// divide rate among all the cubes in grid cell
		}
		else {
			return Double.NaN;
		}
	}
	
	
	/**
	 * This returns -1 if ??????
	 * 
	 * getRelativeTriggerProbOfSourcesInCube(int cubeIndex) has a lot of redundant code, but this might be faster?
	 * 
	 * TODO this other method implies this will crash for certain cubes that have no gridded cell mapping
	 * @param srcIndex
	 * @return
	 */
	public int getRandomSourceIndexInCube(int cubeIndex) {
		
		// compute rate of gridded-seis source in this cube
		int griddeSeisRegionIndex = origGriddedRegion.indexForLocation(getCubeLocationForIndex(cubeIndex));
		if(griddeSeisRegionIndex == -1)	// TODO THROW EXCEPTION FOR NOW UNTIL I UNDERSTAND CONDITIONS BETTER
			throw new RuntimeException("No gridded source index for cube at: "+getCubeLocationForIndex(cubeIndex).toString());
		
		int gridSrcIndex = numFltSystSources + griddeSeisRegionIndex;
		
		int[] sectInCubeArray = sectInCubeList.get(cubeIndex);
		if(sectInCubeArray.length==0) {
			return gridSrcIndex;	// only gridded source in this cube
		}
		else {		// choose between gridded seis and a section nucleation
			IntegerPDF_FunctionSampler sampler = new IntegerPDF_FunctionSampler(sectInCubeArray.length+1);  // plus 1 for gridded source
			double gridSrcRate = sourceRates[gridSrcIndex]/(numPtSrcSubPts*numPtSrcSubPts*numCubeDepths);	// divide rate among all the cubes in grid cell
			sampler.set(0,gridSrcRate);	// first is the gridded source
			float[] fracts = fractionSectInCubeList.get(cubeIndex);
			for(int s=0; s<sectInCubeArray.length;s++) {
//				if(applyGR_Corr) {
//					sampler.set(s+1,totSectNuclRateArray[sectInCubeArray[s]]*(double)fracts[s]*grCorrFactorForSectArray[sectInCubeArray[s]]);		
//				}
//				else
					sampler.set(s+1,totSectNuclRateArray[sectInCubeArray[s]]*(double)fracts[s]);		
			}
			int randSampleIndex = sampler.getRandomInt(etas_utils.getRandomDouble());
			if(randSampleIndex == 0)	// gridded source chosen
				return gridSrcIndex;
			else {	// choose a source that nucleates on the section
				
				int sectIndex = sectInCubeArray[randSampleIndex-1];
				HashMap<Integer,Float> srcNuclRateHashMap = srcNuclRateOnSectList.get(sectIndex);
				IntegerPDF_FunctionSampler srcSampler = new IntegerPDF_FunctionSampler(srcNuclRateHashMap.size());
				int[] srcIndexArray = new int[srcNuclRateHashMap.size()];
				int index=0;
				for(int srcIndex:srcNuclRateHashMap.keySet()) {
					srcIndexArray[index] = srcIndex;
					srcSampler.set(index, srcNuclRateHashMap.get(srcIndex));
					index+=1;
				}
				return srcIndexArray[srcSampler.getRandomInt(etas_utils.getRandomDouble())];
			}
		}
	}
	
	
	
	/**
	 * This computes a scale factor for each original grid cell, whereby multiplying the supra-seismogenic MFD
	 * therein by this factor will produced the same number of expected primary aftershocks as for a perfect GR
	 * (extrapolating the sub-seismogenic MFD to the maximum magnitude of the supra-seismogenic MFD).
	 * 
	 * The array is all 1.0 values if the erf is not a FaultSystemSolutionERF or if applyGR_Corr=false
	 * 
	 * @return
	 */
	private void computeGR_CorrFactorsForSections() {
		
		grCorrFactorForSectArray = new double[rupSet.getNumSections()];
		
		if(erf instanceof FaultSystemSolutionERF && applyGR_Corr) {
			FaultSystemSolutionERF tempERF = (FaultSystemSolutionERF)erf;
			InversionFaultSystemSolution invSol = (InversionFaultSystemSolution)tempERF.getSolution();
			
			double minMag = 5.05;
			double maxMag = 8.95;
			int numMag = 40;
			
			// get Subseismo nucleation MFD for each subsection
			List<GutenbergRichterMagFreqDist> subSeisMFD_List = invSol.getFinalSubSeismoOnFaultMFD_List();
			List<IncrementalMagFreqDist> supraSeisMFD_List = invSol.getFinalSupraSeismoOnFaultMFD_List(minMag, maxMag, numMag);
			
			double minCorr=Double.MAX_VALUE;
			int minCorrIndex = -1;
			for(int sectIndex=0;sectIndex<grCorrFactorForSectArray.length;sectIndex++) {
				if(supraSeisMFD_List.get(sectIndex) != null) {
					double val = ETAS_Utils.getScalingFactorToImposeGR(supraSeisMFD_List.get(sectIndex), subSeisMFD_List.get(sectIndex));
					if(val<1.0)
						grCorrFactorForSectArray[sectIndex]=val;
					else
						grCorrFactorForSectArray[sectIndex]=1.0;
					if(val<minCorr) {
						minCorr=val;
						minCorrIndex=sectIndex;
					}
				}
				else {	// no supra-seismogenic ruptures
					grCorrFactorForSectArray[sectIndex]=1.0;
				}
//				if(grCorrFactorForSectArray[sectIndex]==0)
//					System.out.println(sectIndex+"\t"+(float)grCorrFactorForSectArray[sectIndex]+"\t"+invSol.getRupSet().getFaultSectionData(sectIndex).getName());
			}
			if(D) System.out.println("min GR Corr ("+minCorr+") at sect index: "+minCorrIndex+"\t"+invSol.getRupSet().getFaultSectionData(minCorrIndex).getName());
		}
		else {
			for(int i=0;i<grCorrFactorForSectArray.length;i++)
				grCorrFactorForSectArray[i] = 1.0;
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
		
		// compute the rate of unassigned sections
		double[] sectRatesTest = new double[this.rupSet.getNumSections()];
		for(int i=0;i<numCubes;i++) {
			int[] sections = sectInCubeList.get(i);
			float[] fracts = fractionSectInCubeList.get(i);
			for(int j=0; j<sections.length;j++) {
				sectRatesTest[sections[j]] += totSectNuclRateArray[sections[j]]*(double)fracts[j];
			}
		}
		double rateUnassigned = 0;
		for(int s=0;s<sectRatesTest.length;s++)
			rateUnassigned += (this.totSectNuclRateArray[s]-sectRatesTest[s]);
		
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
		System.out.println("\ttotRateTest="+(float)totRate+" should equal Rate2="+(float)testRate2+";\tratio="+(float)(totRate/testRate2)+
				";\t rateUnassigned"+rateUnassigned);
	}
	
	
	/**
	 * This computes MFDs for each source, not including any GR correction
	 * 
	 * @param minMag
	 * @param maxMag
	 * @param numMag
	 */
	private void computeMFD_ForSrcArrays(double minMag, double maxMag, int numMag) {
//		long st = System.currentTimeMillis();
//		ETAS_SimAnalysisTools.writeMemoryUse("Memory before mfdForSrcArray");
		mfdForSrcArray = new SummedMagFreqDist[erf.getNumSources()];
		mfdForSrcSubSeisOnlyArray = new SummedMagFreqDist[erf.getNumSources()];
		mfdForTrulyOffOnlyArray = new SummedMagFreqDist[erf.getNumSources()];
		double duration = erf.getTimeSpan().getDuration();
		for(int s=0; s<erf.getNumSources();s++) {
			mfdForSrcArray[s] = ERF_Calculator.getTotalMFD_ForSource(erf.getSource(s), duration, minMag, maxMag, numMag, true);
			if(erf instanceof FaultSystemSolutionERF) {
				FaultSystemSolutionERF fssERF = (FaultSystemSolutionERF)erf;
				if(s >= numFltSystSources) {	// gridded seismicity source
					int gridRegionIndex = s-numFltSystSources;
					if(origGridSeisTrulyOffVsSubSeisStatus[gridRegionIndex] == 2) {	// it has both truly off and sub-seismo components
						mfdForSrcSubSeisOnlyArray[s] = ERF_Calculator.getTotalMFD_ForSource(fssERF.getSourceSubSeisOnly(s), duration, minMag, maxMag, numMag, true);;
						mfdForTrulyOffOnlyArray[s] = ERF_Calculator.getTotalMFD_ForSource(fssERF.getSourceTrulyOffOnly(s), duration, minMag, maxMag, numMag, true);;					}
					else {
						mfdForSrcSubSeisOnlyArray[s] = null;
						mfdForTrulyOffOnlyArray[s] = null;
					}
				}
			}
			else {
				mfdForSrcSubSeisOnlyArray[s] = null;
				mfdForTrulyOffOnlyArray[s] = null;
			}
		}
//		ETAS_SimAnalysisTools.writeMemoryUse("Memory after mfdForSrcArray, which took (msec): "+(System.currentTimeMillis()-st));
	}
	
	/**
	 * This assumes that the computeMFD_ForSrcArrays(*) has already been run (exception is thrown if not)
	 * 
	 * TODO this can be constructed from what's returned by getCubeMFD_GriddedSeisOnly(int cubeIndex) and 
	 * getCubeMFD_SupraSeisOnly(int cubeIndex) if the max-mag tests here are no longer needed.
	 * @param cubeIndex
	 * 
	 * TODO this does not make any GR correction
	 * @return
	 */
	private SummedMagFreqDist getCubeMFD(int cubeIndex) {
		
		if(mfdForSrcArray == null) {
			computeMFD_ForSrcArrays(2.05, 8.95, 70);
//			throw new RuntimeException("must run computeMFD_ForSrcArrays(*) first");
		}
		
		Hashtable<Integer,Double> rateForSrcHashtable = getNucleationRatesOfSourcesInCube(cubeIndex);
		
		if(rateForSrcHashtable == null)
			return null;
		
		double maxMagTest=0;
		int testSrcIndex=-1;
		SummedMagFreqDist magDist = new SummedMagFreqDist(mfdForSrcArray[0].getMinX(), mfdForSrcArray[0].getMaxX(), mfdForSrcArray[0].getNum());
		for(int srcIndex:rateForSrcHashtable.keySet()) {
			SummedMagFreqDist mfd=null;
			double srcNuclRate = rateForSrcHashtable.get(srcIndex);
			int gridIndex = srcIndex-numFltSystSources;
			int cubeRegIndex = getCubeRegAndDepIndicesForIndex(cubeIndex)[0];
			if(srcIndex >= numFltSystSources && origGridSeisTrulyOffVsSubSeisStatus[gridIndex] == 2) { // gridded seismicity and cell has both types
				if(isCubeInsideFaultPolygon[cubeRegIndex] == 1) {
					mfd = mfdForSrcSubSeisOnlyArray[srcIndex];
//					System.out.println("Got inside fault poly "+cubeIndex);
				}
				else {
					mfd = mfdForTrulyOffOnlyArray[srcIndex];
//					System.out.println("Got truly off "+cubeIndex);
				}
			}
			else {
				mfd = mfdForSrcArray[srcIndex];
			}
			double totRate = mfd.getTotalIncrRate();
			if(totRate>0) {
				for(int m=0;m<mfd.getNum();m++) {
					magDist.add(m, mfd.getY(m)*srcNuclRate/totRate);
				}
				double maxMag = mfd.getMaxMagWithNonZeroRate();
				if(maxMagTest < maxMag && srcNuclRate>0) {
					maxMagTest = maxMag;	
					testSrcIndex = srcIndex;
				}
			}
		}
		
		
		double maxMagTest2 = magDist.getMaxMagWithNonZeroRate();
		if(Double.isNaN(maxMagTest2))
			maxMagTest2=0;
		if(maxMagTest != maxMagTest2) {
			System.out.println("testSrcIndex="+testSrcIndex);
			System.out.println(mfdForSrcArray[testSrcIndex]);
			System.out.println(magDist+"\nmaxMagTest="+maxMagTest+"\nmaxMagTest2="+maxMagTest2);
			throw new RuntimeException("problem with max mag at cube index "+cubeIndex);
		}
		return magDist;
	}
	
	
	
	/**
	 * This assumes that the computeMFD_ForSrcArrays(*) has already been run (exception is thrown if not)
	 * @param cubeIndex
	 * @return
	 */
	private SummedMagFreqDist getCubeMFD_GriddedSeisOnly(int cubeIndex) {
		
		if(mfdForSrcArray == null) {
			throw new RuntimeException("must run computeMFD_ForSrcArrays(*) first");
		}
		
		Hashtable<Integer,Double> rateForSrcInCubeHashtable = getNucleationRatesOfSourcesInCube(cubeIndex);
		
		if(rateForSrcInCubeHashtable == null)
			return null;
		
		SummedMagFreqDist magDist = new SummedMagFreqDist(mfdForSrcArray[0].getMinX(), mfdForSrcArray[0].getMaxX(), mfdForSrcArray[0].getNum());
		for(int srcIndex:rateForSrcInCubeHashtable.keySet()) {
			if(srcIndex < numFltSystSources)
				continue;	// skip fault based sources
			SummedMagFreqDist mfd=null;
			double srcNuclRate = rateForSrcInCubeHashtable.get(srcIndex);
			int gridIndex = srcIndex-numFltSystSources;
			int cubeRegIndex = getCubeRegAndDepIndicesForIndex(cubeIndex)[0];
			if(srcIndex >= numFltSystSources && origGridSeisTrulyOffVsSubSeisStatus[gridIndex] == 2) { // gridded seismicity and cell has both types
				if(isCubeInsideFaultPolygon[cubeRegIndex] == 1) {
					mfd = mfdForSrcSubSeisOnlyArray[srcIndex];
//					System.out.println("Got inside fault poly "+cubeIndex);
				}
				else {
					mfd = mfdForTrulyOffOnlyArray[srcIndex];
//					System.out.println("Got truly off "+cubeIndex);
				}
			}
			else {
				mfd = mfdForSrcArray[srcIndex];
			}
			double totRate = mfd.getTotalIncrRate();
			if(totRate>0) {
				for(int m=0;m<mfd.getNum();m++)
					magDist.add(m, mfd.getY(m)*srcNuclRate/totRate);
			}
		}
		
		return magDist;
		
//		if(mfdForSrcArray == null) {
//			throw new RuntimeException("must run computeMFD_ForSrcArrays(*) first");
//		}
//		
//		SummedMagFreqDist magDist = new SummedMagFreqDist(mfdForSrcArray[0].getMinX(), mfdForSrcArray[0].getMaxX(), mfdForSrcArray[0].getNum());
//		int[] sources = srcInCubeList.get(cubeIndex);
//		float[] fracts = fractionSrcInCubeList.get(cubeIndex);
//		for(int s=0; s<sources.length;s++) {
//			SummedMagFreqDist mfd=null;
//			int srcIndex = sources[s];
//			if(srcIndex<numFltSystSources)	// skip if it's a fault-based source
//				continue;
//			int gridIndex = srcIndex-numFltSystSources;
//			int cubeRegIndex = getCubeRegAndDepIndicesForIndex(cubeIndex)[0];
//			double wt = (double)fracts[s];
//			if(origGridSeisTrulyOffVsSubSeisStatus[gridIndex] == 2) { // gridded seismicity and cell has both types
//				double fracInsideFaultPoly = faultPolyMgr.getNodeFraction(gridIndex);
//				if(isCubeInsideFaultPolygon[cubeRegIndex] == 1) {
//					mfd = mfdForSrcSubSeisOnlyArray[srcIndex];
//					wt = 1.0/(fracInsideFaultPoly*numPtSrcSubPts*numPtSrcSubPts*numCubeDepths);
////					System.out.println("Got inside fault poly "+cubeIndex);
//				}
//				else {
//					mfd = mfdForTrulyOffOnlyArray[srcIndex];
//					wt = 1.0/((1.0-fracInsideFaultPoly)*numPtSrcSubPts*numPtSrcSubPts*numCubeDepths);
////					System.out.println("Got truly off "+cubeIndex);
//				}
//			}
//			else {
//				mfd = mfdForSrcArray[srcIndex];
//			}
//			for(int m=0;m<mfd.getNum();m++)
//				magDist.add(m, mfd.getY(m)*wt);
//		}
//		return magDist;
	}

	
	
	/**
	 * This assumes that the computeMFD_ForSrcArrays(*) has already been run (exception is thrown if not).
	 * The MFD has all zeros if there are no fault-based sources in the cube.
	 * 
	 * TODO this does not make any GR correction
	 * 
	 * @param cubeIndex
	 * @return
	 */
	private SummedMagFreqDist getCubeMFD_SupraSeisOnly(int cubeIndex) {
		
		
		if(mfdForSrcArray == null) {
			throw new RuntimeException("must run computeMFD_ForSrcArrays(*) first");
		}
		
		Hashtable<Integer,Double> rateForSrcHashtable = getNucleationRatesOfSourcesInCube(cubeIndex);
		
		if(rateForSrcHashtable == null)
			return null;
		
		SummedMagFreqDist magDist = new SummedMagFreqDist(mfdForSrcArray[0].getMinX(), mfdForSrcArray[0].getMaxX(), mfdForSrcArray[0].getNum());
		for(int srcIndex:rateForSrcHashtable.keySet()) {
			if(srcIndex < numFltSystSources) {
				SummedMagFreqDist mfd = mfdForSrcArray[srcIndex];
				double srcNuclRate = rateForSrcHashtable.get(srcIndex);
				double totRate = mfd.getTotalIncrRate();
				if(totRate>0) {
					for(int m=0;m<mfd.getNum();m++) {
						magDist.add(m, mfd.getY(m)*srcNuclRate/totRate);
				
					}
				}
			}
		}
		
		return magDist;
		

//		if(mfdForSrcArray == null) {
//			throw new RuntimeException("must run computeMFD_ForSrcArrays(*) first");
//		}
//		
//		SummedMagFreqDist magDist = new SummedMagFreqDist(mfdForSrcArray[0].getMinX(), mfdForSrcArray[0].getMaxX(), mfdForSrcArray[0].getNum());
//		int[] sources = srcInCubeList.get(cubeIndex);
//		float[] fracts = fractionSrcInCubeList.get(cubeIndex);
//		for(int s=0; s<sources.length;s++) {
//			int srcIndex = sources[s];
//			if(srcIndex<numFltSystSources) {
//				SummedMagFreqDist mfd = mfdForSrcArray[srcIndex];
//				for(int m=0;m<mfd.getNum();m++)
//					magDist.add(m, mfd.getY(m)*(double)fracts[s]);
//			}
//		}
//		return magDist;
	}

	
	/**
	 * This compares the MFD represented here to that in the ERF.
	 * What about one including sources outside the region?
	 * @param erf
	 */
	public void testMagFreqDist() {
		
		System.out.println("Running testMagFreqDist()");
		SummedMagFreqDist magDist = new SummedMagFreqDist(2.05, 8.95, 70);
		if(mfdForSrcArray == null) {
			computeMFD_ForSrcArrays(2.05, 8.95, 70);
		}

//		getPointSampler();	// make sure it exisits
		CalcProgressBar progressBar = new CalcProgressBar("Looping over all points", "junk");
		progressBar.showProgress(true);

		for(int i=0; i<numCubes;i++) {
			progressBar.updateProgress(i, numCubes);
			SummedMagFreqDist mfd = getCubeMFD(i);
			if(mfd != null)
				magDist.addIncrementalMagFreqDist(getCubeMFD(i));

//			int[] sources = srcInCubeList.get(i);
//			float[] fracts = fractionSrcInCubeList.get(i);
//			for(int s=0; s<sources.length;s++) {
//				SummedMagFreqDist mfd=null;
//				int gridIndex = s-numFltSystSources;
//				double wt = (double)fracts[s];
//				if(s >= numFltSystSources && origGridSeisTrulyOffVsSubSeisStatus[gridIndex] == 2) { // gridded seismicity and cell has both types
//					double fracInsideFaultPoly = faultPolyMgr.getNodeFraction(gridIndex);
//					if(isCubeInsideFaultPolygon[i] == 1) {
//						mfd = mfdForSrcSubSeisOnlyArray[s];
//						wt = 1.0/(fracInsideFaultPoly*numPtSrcSubPts*numPtSrcSubPts*numCubeDepths);
//					}
//					else {
//						mfd = mfdForTrulyOffOnlyArray[s];
//						wt = 1.0/((1.0-fracInsideFaultPoly)*numPtSrcSubPts*numPtSrcSubPts*numCubeDepths);
//					}
//				}
//				else {
//					mfd = mfdForSrcArray[sources[s]];
//				}
//				for(int m=0;m<mfd.getNum();m++)
//					magDist.add(m, mfd.getY(m)*wt);
//			}
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
	
	
	public void testGriddedSeisRatesInCubes() {
		CaliforniaRegions.RELM_TESTING_GRIDDED mapGriddedRegion = RELM_RegionUtils.getGriddedRegionInstance();

		GriddedGeoDataSet xyzDataSet = new GriddedGeoDataSet(mapGriddedRegion, true);	
		// initialize values to zero
		for(int i=0; i<xyzDataSet.size();i++) xyzDataSet.set(i, 0);
		double duration = erf.getTimeSpan().getDuration();
		CalcProgressBar progressBar = new CalcProgressBar("Looping over sources", "junk");
		progressBar.showProgress(true);
		int iSrc=0;
		int numSrc = erf.getNumSources();
		for(ProbEqkSource src : erf) {
			iSrc += 1;
			progressBar.updateProgress(iSrc, numSrc);
			if(iSrc<this.numFltSystSources)	// skip fault-based sources
				continue;
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
		progressBar.showProgress(false);
		
		if(mfdForSrcArray == null) {
			computeMFD_ForSrcArrays(5.05, 8.95, 70);
		}

		GriddedGeoDataSet xyzDataSet2 = new GriddedGeoDataSet(mapGriddedRegion, true);	
		// initialize values to zero
		for(int i=0; i<xyzDataSet2.size();i++) xyzDataSet2.set(i, 0);
		progressBar = new CalcProgressBar("Looping over cubes", "junk");
		progressBar.showProgress(true);
		for(int i=0; i<numCubes;i++) {
			progressBar.updateProgress(i, numCubes);
			SummedMagFreqDist mfd = this.getCubeMFD_GriddedSeisOnly(i);
			if(mfd != null) {
				int locIndex = mapGriddedRegion.indexForLocation(this.getCubeLocationForIndex(i));
				if(locIndex>=0) {
					double oldRate = xyzDataSet2.get(locIndex);
					xyzDataSet2.set(locIndex, mfd.getTotalIncrRate()+oldRate);							
				}
			}
		}
		progressBar.showProgress(false);
	
		for(int i=0; i<xyzDataSet2.size();i++) {
			double ratio = xyzDataSet2.get(i)/xyzDataSet.get(i);
			Location loc = xyzDataSet2.getLocation(i);
			if(ratio>1.001 || ratio<0.999)
				System.out.println(ratio+"\t"+loc.getLongitude()+"\t"+loc.getLatitude());
		}
		
	}


	
	public void temp() {
		
		getOrigGridSeisTrulyOffVsSubSeisStatus();
		System.exit(-1);
		
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
	
	public double tempGetSampleProbForAllCubesOnFaultSection(int sectIndex, EqkRupture mainshock) {
		double prob=0;
		IntegerPDF_FunctionSampler aveSampler = getAveSamplerForRupture(mainshock);
		ArrayList<Integer> usedCubesIndices = new ArrayList<Integer>();
		FaultSectionPrefData fltSectData = rupSet.getFaultSectionData(sectIndex);
		for(Location loc: fltSectData.getStirlingGriddedSurface(1, false, true).getEvenlyDiscritizedListOfLocsOnSurface()) {
			int cubeIndex = getCubeIndexForLocation(loc);
			if(!usedCubesIndices.contains(cubeIndex)) {
				prob += aveSampler.getY(cubeIndex);
				usedCubesIndices.add(cubeIndex);
			}
		}
		return prob;
	}
	
	
	public SummedMagFreqDist tempGetSampleMFD_ForAllCubesOnFaultSection(int sectIndex, EqkRupture mainshock) {
		
		if(mfdForSrcArray == null)
			computeMFD_ForSrcArrays(2.05,8.95,70);
		SummedMagFreqDist magDist = new SummedMagFreqDist(mfdForSrcArray[0].getMinX(), mfdForSrcArray[0].getMaxX(), mfdForSrcArray[0].getNum());
		IntegerPDF_FunctionSampler aveSampler = getAveSamplerForRupture(mainshock);
		ArrayList<Integer> usedCubesIndices = new ArrayList<Integer>();
		FaultSectionPrefData fltSectData = rupSet.getFaultSectionData(sectIndex);
		for(Location loc: fltSectData.getStirlingGriddedSurface(1, false, true).getEvenlyDiscritizedListOfLocsOnSurface()) {
			int cubeIndex = getCubeIndexForLocation(loc);
			if(!usedCubesIndices.contains(cubeIndex)) {
				SummedMagFreqDist cubeMFD = getCubeMFD(cubeIndex);
				cubeMFD.scaleToCumRate(0, 1.0); //make PDF
				cubeMFD.scale(aveSampler.getY(cubeIndex));
				magDist.addIncrementalMagFreqDist(cubeMFD);
				usedCubesIndices.add(cubeIndex);
			}
		}
		return magDist;
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
		
//		System.out.println(griddedRegion.indexForLocation(new Location(34.5,-118.0)));
//		System.out.println(griddedRegion.getLocation(griddedRegion.indexForLocation(new Location(34.5,-118.0))));
		
		FaultSystemSolutionERF_ETAS erf = ETAS_Simulator.getU3_ETAS_ERF();


		if(D) System.out.println("Making ETAS_PrimaryEventSampler");
		// first make array of rates for each source
		double sourceRates[] = new double[erf.getNumSources()];
		double duration = erf.getTimeSpan().getDuration();
		for(int s=0;s<erf.getNumSources();s++) {
			sourceRates[s] = erf.getSource(s).computeTotalEquivMeanAnnualRate(duration);
//			if(sourceRates[s] == 0)
//				System.out.println("HERE "+erf.getSource(s).getName());
		}
		boolean includeEqkRates = true;
		double gridSeisDiscr = 0.1;
		boolean applyGRcorr = false;
		
		ETAS_PrimaryEventSampler etas_PrimEventSampler = new ETAS_PrimaryEventSampler(griddedRegion, erf, sourceRates, 
				gridSeisDiscr,null, includeEqkRates, new ETAS_Utils(), ETAS_Utils.distDecay_DEFAULT, ETAS_Utils.minDist_DEFAULT,
				applyGRcorr,null,null,null);
		
//		for(int s=0;s<erf.getSolution().getRupSet().getNumSections();s++) {
//			System.out.println(s);
//			etas_PrimEventSampler.getCubesAndFractForFaultSection(s);
//		}
			
//		etas_PrimEventSampler.getCubesAndFractForFaultSection(1846);	// Mojave section
		
//		HashMap<Integer,Double> map = etas_PrimEventSampler.getCubesAndDistancesInsideSectionPolygon(1846);
//		for(int cubeIndex : map.keySet()) {
//			Location loc = etas_PrimEventSampler.getCubeLocationForIndex(cubeIndex);
//			System.out.println(cubeIndex+"\t"+loc.getLongitude()+"\t"+loc.getLatitude()+"\t"+loc.getDepth()+"\t"+map.get(cubeIndex));
//			
//		}
		
//		etas_PrimEventSampler.getCubeMFD(1378679);
		
		
//		System.out.println("testing rates and MFD");
//		etas_PrimEventSampler.testRates();
//		etas_PrimEventSampler.testMagFreqDist();

//		System.out.println("testGriddedSeisRatesInCubes()");
//		etas_PrimEventSampler.testGriddedSeisRatesInCubes();

//		etas_PrimEventSampler.temp();
//		etas_PrimEventSampler.temp2();
		
		
		
//		etas_PrimEventSampler.plotMaxMagAtDepthMap(7d, "MaxMagAtDepth7km");
//		etas_PrimEventSampler.plotBulgeDepthMap(7d, "BulgeAtDepth7km");
//		etas_PrimEventSampler.plotRateAtDepthMap(7d,7.25,"RatesAboveM7pt2_AtDepth7km");
		etas_PrimEventSampler.plotRateAtDepthMap(7d,6.65,"RatesAboveM6pt6_AtDepth7km");
//		etas_PrimEventSampler.plotOrigERF_RatesMap("OrigERF_RatesMap");
//		etas_PrimEventSampler.plotRandomSampleRatesMap("RandomSampleRatesMap", 10000000);

		
		
		
		
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
	
	
		


	
	/**
	 * This plots the spatial distribution of probabilities implied by the given cubeSampler
	 * (probs are summed inside each spatial bin of gridRegForRatesInSpace).
	 * 
	 * @param label - plot label
	 * @param dirName - the name of the directory
	 * @param path - where to put the dir
	 * @return
	 */
	public String plotSamplerMap(IntegerPDF_FunctionSampler cubeSampler, String label, String dirName, File path) {
		
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
				String url = mapGen.makeMapUsingServlet(xyzDataSet, label, metadata, dirName);
				metadata += GMT_MapGuiBean.getClickHereHTML(mapGen.getGMTFilesWebAddress());
				ImageViewerWindow imgView = new ImageViewerWindow(url,metadata, true);		
				
				File downloadDir = null;
				if(path != null)
					downloadDir = new File(path, dirName);
				else
					downloadDir = new File(dirName);
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

		GriddedGeoDataSet maxMagData = new GriddedGeoDataSet(gridRegForCubes, true);
		int depthIndex = getCubeDepthIndex(depth);
		int numCubesAtDepth = maxMagData.size();
		CalcProgressBar progressBar = new CalcProgressBar("Looping over all points", "junk");
		progressBar.showProgress(true);
		
		if(mfdForSrcArray == null) {
			computeMFD_ForSrcArrays(5.05, 8.95, 70);
		}

		for(int i=0; i<numCubesAtDepth;i++) {
			progressBar.updateProgress(i, numCubesAtDepth);
			int samplerIndex = getCubeIndexForRegAndDepIndices(i, depthIndex);
			SummedMagFreqDist mfd = getCubeMFD(samplerIndex);
			if(mfd != null)
				maxMagData.set(i, mfd.getMaxMagWithNonZeroRate());
//			Location loc = maxMagData.getLocation(i);
//			System.out.println(loc.getLongitude()+"\t"+loc.getLatitude()+"\t"+maxMagData.get(i));
		}
		progressBar.showProgress(false);

		
		String metadata = "Map from calling plotMaxMagAtDepthMap(*) method";
		
		try {
				String url = mapGen.makeMapUsingServlet(maxMagData, "Max Mag at depth="+depth, metadata, dirName);
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
		
		
		GriddedGeoDataSet bulgeData = new GriddedGeoDataSet(gridRegForCubes, true);
		int depthIndex = getCubeDepthIndex(depth);
		int numCubesAtDepth = bulgeData.size();
		CalcProgressBar progressBar = new CalcProgressBar("Looping over all points", "junk");
		progressBar.showProgress(true);
		
		if(mfdForSrcArray == null) {
			computeMFD_ForSrcArrays(5.05, 8.95, 70);
		}

		for(int i=0; i<numCubesAtDepth;i++) {
			progressBar.updateProgress(i, numCubesAtDepth);
			int cubeIndex = getCubeIndexForRegAndDepIndices(i, depthIndex);
			SummedMagFreqDist mfdSupra = getCubeMFD_SupraSeisOnly(cubeIndex);
			SummedMagFreqDist mfdGridded = getCubeMFD_GriddedSeisOnly(cubeIndex);
			double bulge = 1.0;
			if(mfdSupra != null &&  mfdGridded != null) {
				bulge = 1.0/ETAS_Utils.getScalingFactorToImposeGR(mfdSupra, mfdGridded);
				if(Double.isInfinite(bulge))
					bulge = 1e3;				
			}
			bulgeData.set(i, bulge);
//			Location loc = maxMagData.getLocation(i);
//			System.out.println(loc.getLongitude()+"\t"+loc.getLatitude()+"\t"+maxMagData.get(i));
		}
		progressBar.showProgress(false);

		String metadata = "Map from calling plotBulgeDepthMap(*) method";
		
		try {
				String url = mapGen.makeMapUsingServlet(bulgeData, "Bulge at depth="+depth, metadata, dirName);
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

		GriddedGeoDataSet xyzDataSet = new GriddedGeoDataSet(gridRegForCubes, true);
		int depthIndex = getCubeDepthIndex(depth);
		int numCubesAtDepth = xyzDataSet.size();
		CalcProgressBar progressBar = new CalcProgressBar("Looping over all points", "junk");
		progressBar.showProgress(true);
		
		if(mfdForSrcArray == null) {
			computeMFD_ForSrcArrays(5.05, 8.95, 70);
		}
		
		int magIndex = mfdForSrcArray[0].getClosestXIndex(mag);


		for(int i=0; i<numCubesAtDepth;i++) {
			progressBar.updateProgress(i, numCubesAtDepth);
			int samplerIndex = getCubeIndexForRegAndDepIndices(i, depthIndex);
			SummedMagFreqDist mfd = getCubeMFD(samplerIndex);
			double rate = 0.0;
			if(mfd != null)
				rate = getCubeMFD(samplerIndex).getCumRate(magIndex);
			if(rate == 0.0)
				rate = 1e-16;
			xyzDataSet.set(i, rate);
//			Location loc = xyzDataSet.getLocation(i);
//			System.out.println(loc.getLongitude()+"\t"+loc.getLatitude()+"\t"+xyzDataSet.get(i));
		}
		progressBar.showProgress(false);
		
		mapGen.setParameter(GMT_MapGenerator.LOG_PLOT_NAME,true);
//		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MODE_NAME,GMT_MapGenerator.COLOR_SCALE_MODE_FROMDATA);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MODE_NAME,GMT_MapGenerator.COLOR_SCALE_MODE_MANUALLY);
		double maxZ = Math.ceil(Math.log10(xyzDataSet.getMaxZ()));
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME,maxZ-7);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME,maxZ);


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
	public String plotOrigERF_RatesMap(String dirName) {
		
		GMT_MapGenerator mapGen = new GMT_MapGenerator();
		mapGen.setParameter(GMT_MapGenerator.GMT_SMOOTHING_PARAM_NAME, false);
		mapGen.setParameter(GMT_MapGenerator.TOPO_RESOLUTION_PARAM_NAME, GMT_MapGenerator.TOPO_RESOLUTION_NONE);
		mapGen.setParameter(GMT_MapGenerator.MIN_LAT_PARAM_NAME,31.5);		// -R-125.4/-113.0/31.5/43.0
		mapGen.setParameter(GMT_MapGenerator.MAX_LAT_PARAM_NAME,43.0);
		mapGen.setParameter(GMT_MapGenerator.MIN_LON_PARAM_NAME,-125.4);
		mapGen.setParameter(GMT_MapGenerator.MAX_LON_PARAM_NAME,-113.0);
		mapGen.setParameter(GMT_MapGenerator.LOG_PLOT_NAME,true);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MODE_NAME,GMT_MapGenerator.COLOR_SCALE_MODE_MANUALLY);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME,-6.5);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME,-1.5);


		CaliforniaRegions.RELM_TESTING_GRIDDED mapGriddedRegion = RELM_RegionUtils.getGriddedRegionInstance();
		GriddedGeoDataSet xyzDataSet = new GriddedGeoDataSet(mapGriddedRegion, true);
		
		// initialize values to zero
		for(int i=0; i<xyzDataSet.size();i++) xyzDataSet.set(i, 0);
		
		double duration = erf.getTimeSpan().getDuration();
		CalcProgressBar progressBar = new CalcProgressBar("Looping random samples", "junk");
		progressBar.showProgress(true);
		int iSrc=0;
		int numSrc = erf.getNumSources();
		for(ProbEqkSource src : erf) {
			iSrc += 1;
			progressBar.updateProgress(iSrc, numSrc);
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
		progressBar.showProgress(false);
		
		if(D) 
			System.out.println("OrigERF_RatesMap: min="+xyzDataSet.getMinZ()+"; max="+xyzDataSet.getMaxZ());
		
		String metadata = "Map from calling plotOrigERF_RatesMap() method";
		
		try {
				String url = mapGen.makeMapUsingServlet(xyzDataSet, "OrigERF_RatesMap", metadata, dirName);
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
		return "For OrigERF_RatesMap: "+mapGen.getGMTFilesWebAddress()+" (deleted at midnight)";
	}

	/**
	 * 
	 * @param label - plot label
	 * @param local - whether GMT map is made locally or on server
	 * @param dirName
	 * @return
	 */
	public String plotRandomSampleRatesMap(String dirName, int numYrs) {
		
		GMT_MapGenerator mapGen = new GMT_MapGenerator();
		mapGen.setParameter(GMT_MapGenerator.GMT_SMOOTHING_PARAM_NAME, false);
		mapGen.setParameter(GMT_MapGenerator.TOPO_RESOLUTION_PARAM_NAME, GMT_MapGenerator.TOPO_RESOLUTION_NONE);
		mapGen.setParameter(GMT_MapGenerator.MIN_LAT_PARAM_NAME,31.5);		// -R-125.4/-113.0/31.5/43.0
		mapGen.setParameter(GMT_MapGenerator.MAX_LAT_PARAM_NAME,43.0);
		mapGen.setParameter(GMT_MapGenerator.MIN_LON_PARAM_NAME,-125.4);
		mapGen.setParameter(GMT_MapGenerator.MAX_LON_PARAM_NAME,-113.0);
		mapGen.setParameter(GMT_MapGenerator.LOG_PLOT_NAME,true);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MODE_NAME,GMT_MapGenerator.COLOR_SCALE_MODE_MANUALLY);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME,-6.5);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME,-1.5);


		CaliforniaRegions.RELM_TESTING_GRIDDED mapGriddedRegion = RELM_RegionUtils.getGriddedRegionInstance();
		GriddedGeoDataSet xyzDataSet = new GriddedGeoDataSet(mapGriddedRegion, true);
		
		// initialize values to zero
		for(int i=0; i<xyzDataSet.size();i++) xyzDataSet.set(i, 0);
		
		getCubeSamplerWithERF_RatesOnly();
		
		// get numYrs yrs worth of samples
		totRate=cubeSamplerRatesOnly.calcSumOfY_Vals();
		int numSamples = numYrs*(int)totRate;
		System.out.println("num random samples for map test = "+numSamples);
		// do this to make sure it exists
		getCubeSamplerWithERF_RatesOnly();
		
		CalcProgressBar progressBar = new CalcProgressBar("Looping random samples", "junk");
		progressBar.showProgress(true);
		for(int i=0;i<numSamples;i++) {
			progressBar.updateProgress(i, numSamples);
			int indexFromSampler = cubeSamplerRatesOnly.getRandomInt(etas_utils.getRandomDouble());
			int[] regAndDepIndex = getCubeRegAndDepIndicesForIndex(indexFromSampler);
			int indexForMap = mapGriddedRegion.indexForLocation(gridRegForCubes.locationForIndex(regAndDepIndex[0]));	// ignoring depth
			double oldNum = xyzDataSet.get(indexForMap)*numYrs;
			xyzDataSet.set(indexForMap, (1.0+oldNum)/(double)numYrs);
			
		}
		progressBar.showProgress(false);
		
		if(D) 
			System.out.println("RandomSampleRatesMap: min="+xyzDataSet.getMinZ()+"; max="+xyzDataSet.getMaxZ());

		String metadata = "Map from calling RandomSampleRatesMap() method";
		
		try {
				String url = mapGen.makeMapUsingServlet(xyzDataSet, "RandomSampleRatesMap", metadata, dirName);
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
		return "For RandomSampleRatesMap: "+mapGen.getGMTFilesWebAddress()+" (deleted at midnight)";
	}

	@Override
	public IntegerPDF_FunctionSampler load(Integer locIndexForPar) throws Exception {
		if(includeERF_Rates && includeSpatialDecay) {
			return getCubeSamplerWithDistDecay(locIndexForPar);
		}
		else if(includeERF_Rates && !includeSpatialDecay) {
			return getCubeSamplerWithERF_RatesOnly();
		}
		else if(!includeERF_Rates && includeSpatialDecay) {
			return getCubeSamplerWithOnlyDistDecay(locIndexForPar);
		}
		throw new IllegalStateException("include ERF rates and include spatial decay both false?");
	}


}
