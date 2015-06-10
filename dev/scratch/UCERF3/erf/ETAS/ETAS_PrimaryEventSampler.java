package scratch.UCERF3.erf.ETAS;

import java.awt.Color;
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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.opensha.commons.data.TimeSpan;
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
import org.opensha.commons.mapping.gmt.gui.ImageViewerWindow;
import org.opensha.commons.param.Parameter;
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
import org.opensha.sha.earthquake.param.ProbabilityModelOptions;
import org.opensha.sha.earthquake.param.ProbabilityModelParam;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;
import com.google.common.collect.Maps;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.analysis.FaultBasedMapGen;
import scratch.UCERF3.analysis.FaultSysSolutionERF_Calc;
import scratch.UCERF3.analysis.FaultSystemSolutionCalc;
import scratch.UCERF3.analysis.GMT_CA_Maps;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.griddedSeismicity.FaultPolyMgr;
import scratch.UCERF3.griddedSeismicity.GridSourceProvider;
import scratch.UCERF3.griddedSeismicity.UCERF3_GridSourceGenerator;
import scratch.UCERF3.inversion.InversionFaultSystemRupSet;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.inversion.InversionTargetMFDs;
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
	
	private static boolean disable_cache = false;
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
	FaultSystemSolutionERF fssERF;
	int numFltSystSources=-1, totNumSrc;
	FaultSystemRupSet rupSet;
	FaultPolyMgr faultPolyMgr;

	
	int numPtSrcSubPts;
	double pointSrcDiscr;
	
	// this is for each cube
	double[] latForCubeCenter, lonForCubeCenter, depthForCubeCenter;
	
	// this will hold the rate of each ERF source (which can be modified by external objects)
	double sourceRates[];
	double totSectNuclRateArray[];
	double totalSectRateInCubeArray[];
	ArrayList<HashMap<Integer,Float>> srcNuclRateOnSectList;
	SummedMagFreqDist[] longTermSupraSeisMFD_OnSectArray;
	List<? extends IncrementalMagFreqDist> longTermSubSeisMFD_OnSectList;
	
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
	private static long cache_size_bytes;
	private static boolean soft_cache_values;
	
	// default maximum cache size in gigabytes. can be overridden by setting the etas.cache.size.gb property
	// this should be small if soft_cache_values = false, but can be large otherwise as cache values will
	// be garbage collected when needed
	private static double default_cache_size_gb = 4d;
	// default value for soft cache values. can be overridden by setting the etas.cache.soft property
	// to 'true'/'false'. Only applicable to guava cache
	private static boolean default_soft_cache_values = true;
	// use custom cache which uses smart eviction
	private static boolean use_custom_cache = false;
	
	static {
		double cacheSizeGB = Double.parseDouble(System.getProperty("etas.cache.size.gb", default_cache_size_gb+""));
		cache_size_bytes = ((long)(cacheSizeGB*1024d)*1024l*1024l); // now in bytes
		soft_cache_values = Boolean.parseBoolean(System.getProperty("etas.cache.soft",
				default_soft_cache_values+"").toLowerCase().trim());
		if (D) System.out.println("ETAS Cache Size: "+(float)cacheSizeGB
				+" GB = "+cache_size_bytes+" bytes, soft values: "+soft_cache_values);
	}
	
	Map<Integer,Integer> numForthcomingEventsForParLocIndex;  // key is the parLocIndex and value is the number of events to process TODO remove this
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
	 * 
	 * @param griddedRegion
	 * @param erf
	 * @param sourceRates
	 * @param pointSrcDiscr
	 * @param outputFileNameWithPath
	 * @param includeERF_Rates
	 * @param etas_utils
	 * @param etasDistDecay_q
	 * @param etasMinDist_d
	 * @param applyGR_Corr
	 * @param inputFractSectInCubeList
	 * @param inputSectInCubeList
	 * @param inputIsCubeInsideFaultPolygon
	 */
	public ETAS_PrimaryEventSampler(GriddedRegion griddedRegion, AbstractNthRupERF erf, double sourceRates[],
			double pointSrcDiscr, String outputFileNameWithPath, boolean includeERF_Rates, ETAS_Utils etas_utils,
			double etasDistDecay_q, double etasMinDist_d, boolean applyGR_Corr, List<float[]> inputFractSectInCubeList, 
			List<int[]> inputSectInCubeList,  int[] inputIsCubeInsideFaultPolygon) {

		this(griddedRegion, DEFAULT_NUM_PT_SRC_SUB_PTS, erf, sourceRates, DEFAULT_MAX_DEPTH, DEFAULT_DEPTH_DISCR, 
				pointSrcDiscr, outputFileNameWithPath, etasDistDecay_q, etasMinDist_d, includeERF_Rates, true, etas_utils,
				applyGR_Corr, inputFractSectInCubeList, inputSectInCubeList, inputIsCubeInsideFaultPolygon);
	}

	
	/**
	 * TODO
	 * 
	 * 		resolve potential ambiguities between attributes of griddedRegion and pointSrcDiscr
	 * 
	 * @param griddedRegion
	 * @param numPtSrcSubPts - the
	 * @param erf
	 * @param sourceRates - pointer to an array of source rates (which may get updated externally)
	 * @param maxDepth
	 * @param depthDiscr
	 * @param pointSrcDiscr - the grid spacing of gridded seismicity in the ERF
	 * @param outputFileNameWithPath - TODO not used
	 * @param distDecay - ETAS distance decay parameter
	 * @param minDist - ETAS minimum distance parameter
	 * @param includeERF_Rates - tells whether to consider long-term rates in sampling aftershocks
	 * @param includeSpatialDecay - tells whether to include spatial decay in sampling aftershocks (for testing)
	 * @param etas_utils - this is for obtaining reproducible random numbers (seed set in this object)
	 * @param applyGR_Corr - whether or not to apply the GR correction
	 * @param inputFractSectInCubeList
	 * @param inputSectInCubeList
	 * @param inputIsCubeInsideFaultPolygon
	 */
	public ETAS_PrimaryEventSampler(GriddedRegion griddedRegion, int numPtSrcSubPts, AbstractNthRupERF erf, double sourceRates[],
			double maxDepth, double depthDiscr, double pointSrcDiscr, String outputFileNameWithPath, double distDecay, 
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
			rupSet = ((FaultSystemSolutionERF)erf).getSolution().getRupSet();
			faultPolyMgr = FaultPolyMgr.create(rupSet.getFaultSectionDataList(), InversionTargetMFDs.FAULT_BUFFER);	// this works, but not generalized
			fssERF = (FaultSystemSolutionERF) erf;
		}
		else {
			rupSet = null;
			faultPolyMgr = null;	
			fssERF = null;
		}
		
		Region regionForRates = new Region(griddedRegion.getBorder(),BorderType.MERCATOR_LINEAR);
		
		// need to set the region anchors so that the gridRegForRatesInSpace sub-regions fall completely inside the gridded seis regions
		// this assumes the point sources have an anchor of GriddedRegion.ANCHOR_0_0)
		if(numPtSrcSubPts % 2 == 0) {	// it's an even number
			gridRegForCubes = new GriddedRegion(regionForRates, cubeLatLonSpacing, new Location(cubeLatLonSpacing/2d,cubeLatLonSpacing/2d));
			// parent locs are mid way between cubes:
			gridRegForParentLocs = new GriddedRegion(regionForRates, cubeLatLonSpacing, GriddedRegion.ANCHOR_0_0);			
		}
		else {	// it's odd
			gridRegForCubes = new GriddedRegion(regionForRates, cubeLatLonSpacing, GriddedRegion.ANCHOR_0_0);
			// parent locs are mid way between cubes:
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
		
		
		// this is for caching samplers (one for each possible parent location)
//		cachedSamplers = new IntegerPDF_FunctionSampler[numParLocs];
		
		// Kevin's code below - Ned does not yet understand
		// this will weigh cache elements by their size
		
		// this is used by the custom cache for smart evictions
		numForthcomingEventsForParLocIndex = Maps.newHashMap();
		
//		// convert to size in bytes. each IntegerPDF_FunctionSampler has 2 double arrays of lengh numCubeDepths*numCubesPerDepth
//		// each double value is 8 bytes
		int samplerSizeBytes = 2*8*(numCubeDepths*numCubesPerDepth)+30; // pad for object overhead and other primitives
		int cacheSize = (int)(cache_size_bytes/samplerSizeBytes);
		if (!disable_cache && cacheSize > 0) {
			double fractCached = (double)cacheSize/(double)numCubes;
			System.out.println("Sampler Size Bytes: "+samplerSizeBytes);
			System.out.println("Cache will store at most "+cacheSize+"/"+numCubes+" = "
					+(float)(100d*fractCached)+" % samplers");
		}
		if (use_custom_cache) {
			samplerCache = new CubeSamplerCache(cacheSize, this, numForthcomingEventsForParLocIndex);
		} else {
			CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder().maximumSize(cacheSize);
			if (soft_cache_values)
				builder = builder.softValues();
			samplerCache = builder.build(this);
		}
		
		totNumSrc = erf.getNumSources();
		if(totNumSrc != sourceRates.length)
			throw new RuntimeException("Problem with number of sources");
		
		if(erf instanceof FaultSystemSolutionERF)
			numFltSystSources = ((FaultSystemSolutionERF)erf).getNumFaultSystemSources();
		else
			numFltSystSources=0;
		
		if(D) System.out.println("totNumSrc="+totNumSrc+"\tnumFltSystSources="+numFltSystSources+
				"\tnumPointsForRates="+numCubes);
				
		this.sourceRates = sourceRates;	// pointer to current source rates
		
		System.gc();	// garbage collect
		
		if(D)  ETAS_SimAnalysisTools.writeMemoryUse("Memory before making data");
		
		if(erf instanceof FaultSystemSolutionERF) {
			// create the arrays that will store section nucleation info
			totSectNuclRateArray = new double[rupSet.getNumSections()];
			// this is a hashmap for each section, which contains the source index (key) and nucleation rate (value)
			srcNuclRateOnSectList = new ArrayList<HashMap<Integer,Float>>();
			for(int sect=0;sect<rupSet.getNumSections();sect++) {
				srcNuclRateOnSectList.add(new HashMap<Integer,Float>());
			}
			for(int src=0; src<numFltSystSources; src++) {
				int fltSysRupIndex = fssERF.getFltSysRupIndexForSource(src);
				List<Integer> sectIndexList = rupSet.getSectionsIndicesForRup(fltSysRupIndex);
				for(int sect:sectIndexList) {
					srcNuclRateOnSectList.get(sect).put(src,0f);
				}
			}	
			// now compute initial values for these arrays (nucleation rates of of sections given norm time since last and any GR corr)
			computeSectNucleationRates();
			System.gc();	// garbage collect
			if (D) ETAS_SimAnalysisTools.writeMemoryUse("Memory after making data");
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
				
		origGridSeisTrulyOffVsSubSeisStatus = getOrigGridSeisTrulyOffVsSubSeisStatus();
		
		// read or make cache data if needed
		if(inputFractSectInCubeList!=null && inputSectInCubeList != null && inputIsCubeInsideFaultPolygon != null) {
				sectInCubeList = inputSectInCubeList;
				fractionSectInCubeList = inputFractSectInCubeList;
				isCubeInsideFaultPolygon = inputIsCubeInsideFaultPolygon;
		}
		else {
			File sectInCubeCacheFilename = new File(defaultSectInCubeCacheFilename);
			File fractSectInCubeCacheFilename = new File(defaultFractSectInCubeCacheFilename);	
			File cubeInsidePolyCacheFilename = new File(defaultCubeInsidePolyCacheFilename);	
			if (sectInCubeCacheFilename.exists() && fractSectInCubeCacheFilename.exists() && cubeInsidePolyCacheFilename.exists()) { // read from file if it exists
				try {
					if(D) ETAS_SimAnalysisTools.writeMemoryUse("Memory before reading "+fractSectInCubeCacheFilename);
					fractionSectInCubeList = MatrixIO.floatArraysListFromFile(fractSectInCubeCacheFilename);
					if(D) ETAS_SimAnalysisTools.writeMemoryUse("Memory before reading "+sectInCubeCacheFilename);
					sectInCubeList = MatrixIO.intArraysListFromFile(sectInCubeCacheFilename);
					if(D) ETAS_SimAnalysisTools.writeMemoryUse("Memory before reading "+cubeInsidePolyCacheFilename);
					isCubeInsideFaultPolygon = MatrixIO.intArrayFromFile(cubeInsidePolyCacheFilename);
					if(D) ETAS_SimAnalysisTools.writeMemoryUse("Memory after reading isCubeInsideFaultPolygon");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {  // make cache file if they don't exist
				if(D) ETAS_SimAnalysisTools.writeMemoryUse("Memory before running generateAndWriteCacheDataToFiles()");
				generateAndWriteCacheDataToFiles();
				if(D) ETAS_SimAnalysisTools.writeMemoryUse("Memory after running generateAndWriteCacheDataToFiles()");
				System.gc();
				// now read the data from files (because the generate method above does not set these)
				try {
					fractionSectInCubeList = MatrixIO.floatArraysListFromFile(fractSectInCubeCacheFilename);
					sectInCubeList = MatrixIO.intArraysListFromFile(sectInCubeCacheFilename);
					isCubeInsideFaultPolygon = MatrixIO.intArrayFromFile(cubeInsidePolyCacheFilename);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		if(D) ETAS_SimAnalysisTools.writeMemoryUse("Memory before computeTotSectRateInCubesArray()");
		computeTotSectRateInCubesArray();
		if(D) ETAS_SimAnalysisTools.writeMemoryUse("Memory after computeTotSectRateInCubesArray()");
		
		
		if(D) System.out.println("Running makeETAS_LocWtCalcList()");
		double maxDistKm=1000;
		double midLat = (gridRegForCubes.getMaxLat() + gridRegForCubes.getMinLat())/2.0;
		etas_LocWeightCalc = new ETAS_LocationWeightCalculator(maxDistKm, maxDepth, cubeLatLonSpacing, depthDiscr, midLat, etasDistDecay, etasMinDist, etas_utils);
		if(D) ETAS_SimAnalysisTools.writeMemoryUse("Memory after making etas_LocWeightCalc");
		if(D) System.out.println("Done running makeETAS_LocWtCalcList()");
		
	}
	
	
	/**
	 * This computes/updates the section nucleation rates (srcNuclRateOnSectList and totSectNuclRateArray), 
	 * where the time-dependent fss source rates are mapped onto different sections in proportion to the 
	 * normalized time since last event (the latter being 1.0 if unknown).  This also applies the GR
	 * correction to each section (which is 1.0 if correction not requested).
	 */
	private void computeSectNucleationRates() {
		
		long st= System.currentTimeMillis();
		
		for(int s=0;s<totSectNuclRateArray.length;s++) {	// intialized totals to zero
			totSectNuclRateArray[s] = 0d;
		}
		//
		double[] sectNormTimeSince = fssERF.getNormTimeSinceLastForSections();
		for(int src=0; src<numFltSystSources; src++) {
			int fltSysRupIndex = fssERF.getFltSysRupIndexForSource(src);
			List<Integer> sectIndexList = rupSet.getSectionsIndicesForRup(fltSysRupIndex);
			// this will store the normalized time since last event on each section:
			double[] normTimeSinceOnSectArray = new double[sectIndexList.size()];	
			double sum=0;
			for(int s=0;s<normTimeSinceOnSectArray.length;s++) {
				int sectIndex = sectIndexList.get(s);
				double normTS = sectNormTimeSince[sectIndex];
				// TODO should check for poisson model here
				if(Double.isNaN(normTS)) 
					normTimeSinceOnSectArray[s] = 1.0;	// assume it's 1.0 if value unavailable
				else
					normTimeSinceOnSectArray[s]=normTS;
				sum += normTimeSinceOnSectArray[s];	// this will be used to avoid dividing by zero later
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
		
		// TESTS TODO do this only in debug mode?
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
		
		if(D) {
			double timeSec = (double)(System.currentTimeMillis()-st)/1000d;
			System.out.println("computeSectNucleationRates runtime(sec) = "+timeSec);			
		}
		


	}
	
	
	/**
	 * This computes the total rate of all sections that nucleate inside each cube
	 */
	public void computeTotSectRateInCubesArray() {

		long st = System.currentTimeMillis();
		totalSectRateInCubeArray = new double[numCubes];
		for(int c=0;c<numCubes;c++) {
			int[] sectInCubeArray = sectInCubeList.get(c);
			float[] fracts = fractionSectInCubeList.get(c);
			for(int s=0; s<sectInCubeArray.length;s++) {
				totalSectRateInCubeArray[c] += totSectNuclRateArray[sectInCubeArray[s]]*(double)fracts[s];
			}
		}
		
		if(D) {
			double timeSec = (double)(System.currentTimeMillis()-st)/1000d;
			System.out.println("tempSectRateInCubeArray runtime(sec) = "+timeSec);	
		}

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
	
	
	/**
	 * This returns a map giving the cubes that are inside the fault-section's polygon (cube index is map key),
	 * plus the distance (map value) of each cube center from the fault-section surface.
	 * @param sectionIndex
	 * @param distBelowBottomThresh - this determines how far below the lower seismogenic depth we go for including cubes
	 * @return
	 */
	private HashMap<Integer,Double> getCubesAndDistancesInsideSectionPolygon(int sectionIndex, double distBelowBottomThresh) {
		HashMap<Integer,Double> cubeDistMap = new HashMap<Integer,Double>();
		Region faultPolygon = faultPolyMgr.getPoly(sectionIndex);
		StirlingGriddedSurface surface = rupSet.getFaultSectionData(sectionIndex).getStirlingGriddedSurface(0.25, false, true);
		double lowerSeisDepth = surface.getLowerSeismogenicDepth();
		for(int i=0; i<numCubes;i++) {
			Location cubeLoc = getCubeLocationForIndex(i);
			double cubeDepthBelowSurf = cubeLoc.getDepth()-lowerSeisDepth;
			if(cubeDepthBelowSurf < distBelowBottomThresh && faultPolygon.contains(cubeLoc)) {
				double dist = LocationUtils.distanceToSurf(cubeLoc, surface);
				cubeDistMap.put(i, dist);
			}
		}
		return cubeDistMap;
	}
	
	
	// TODO remove
	private double getFarthestCubeDistAtDepthForSection(int sectionIndex, double depthKm, Set<Integer> cubeList) {
		
		// get the surface and find the row index corresponding to depthKm
		StirlingGriddedSurface surface = rupSet.getFaultSectionData(sectionIndex).getStirlingGriddedSurface(1.0, false, true);
		double min = Double.MAX_VALUE;
		int rowIndex=-1;
		for(int r=0;r<surface.getNumRows();r++) {
			double diff = Math.abs(surface.get(r,0).getDepth()-depthKm);
			if(diff < min) {
				min = diff;
				rowIndex = r;
			}
		}
		// get the cube depth index for depthKm 
		int cubeDepthIndex = getCubeDepthIndex(depthKm);
		
		// now find the distance of farthest cube at depthKm perpendicular to a line on the surface at depthKm
		double maxDist = -1;
		for(int cubeIndex:cubeList) {
			Location cubeLoc = getCubeLocationForIndex(cubeIndex);
			if(this.getCubeDepthIndex(cubeLoc.getDepth()) != cubeDepthIndex)
				continue;	// skip those that aren't at depthKm
			// find the mind dist to the line on the surface 
			double minDist = Double.MAX_VALUE;
			for(int c=0; c<surface.getNumCols();c++) {
				Location surfLoc= surface.getLocation(rowIndex, c);
				double dist = LocationUtils.linearDistanceFast(cubeLoc, surfLoc);
				if(dist<minDist)
					minDist = dist;
			}
			// is this the farthest yet found?
			if(maxDist<minDist)
				maxDist=minDist;
		}
		return maxDist;
	}
	
	/**
	 * This computes the half width of the fault-section polygon (perpendicular to the strike) in a
	 * somewhat weird way.
	 * 
	 * TODO Remove?
	 * 
	 * @param sectionIndex
	 * @param cubeList
	 * @return
	 */
	private double getFaultSectionPolygonHalfWidth(int sectionIndex, Set<Integer> cubeList) {
		
		// get the surface and find the row index corresponding to depthKm
		StirlingGriddedSurface surface = rupSet.getFaultSectionData(sectionIndex).getStirlingGriddedSurface(1.0, false, false);
		
//System.out.println("Surface Dip: "+surface.getAveDip());
//System.out.println("Surface:");
//for(int r=0;r<surface.getNumRows();r++) {
//	for(int c=0;c<surface.getNumCols();c++) {
//		Location loc = surface.get(r, c);
//		System.out.println(loc.getLongitude()+"\t"+loc.getLatitude()+"\t"+loc.getDepth());
//	}
//}
//Region reg = faultPolyMgr.getPoly(sectionIndex);
//System.out.println("Polygon:");
//for(Location loc:reg.getBorder()) {
//	System.out.println(loc.getLongitude()+"\t"+loc.getLatitude()+"\t"+loc.getDepth());
//}

		int rowIndexHalfWayDown = surface.getNumRows()/2;
		double depthHalfWayDown = surface.getLocation(rowIndexHalfWayDown,0).getDepth();
		// get the cube depth index for depthKm 
		int cubeDepthIndex = getCubeDepthIndex(depthHalfWayDown);
		
		// now find the distance of farthest cube at depthKm perpendicular to a line on the surface at depthKm
		double maxDist = -1;
		for(int cubeIndex:cubeList) {
			Location cubeLoc = getCubeLocationForIndex(cubeIndex);
			if(this.getCubeDepthIndex(cubeLoc.getDepth()) != cubeDepthIndex)
				continue;	// skip those that aren't at depthKm
			// find the min dist to the line on the surface 
			double minDist = Double.MAX_VALUE;
			for(int c=0; c<surface.getNumCols();c++) {
				Location surfLoc= surface.getLocation(rowIndexHalfWayDown, c);
				double dist = LocationUtils.linearDistanceFast(cubeLoc, surfLoc);
				if(dist<minDist)
					minDist = dist;
			}
			// is this the farthest yet found?
			if(maxDist<minDist)
				maxDist=minDist;
		}
// System.out.println(sectionIndex+"\t"+maxDist+"\t"+this.fssERF.getSolution().getRupSet().getFaultSectionData(sectionIndex).getName());
		return maxDist;
	}

	
	/**
	 * For the given section, this returns all the cubes that are within the section polygon, together
	 * with the fractional rate of the section that each cube gets using a linear distance decay
	 * from the surface and constrained by the total section rate and that the farthest cubes above the 
	 * seismogenic depth plus 4km have a perfect GR in cumulative M6.5 rate (plus any time dependence).  
	 * If the section MFD is non-characteristic, then the rates are distributed uniformly among cubes.
	 * 
	 * The problem here is that all cubes below seismo depth plus 4 km are ignored (zero rate), and trying
	 * to include them would produce negative rates at farthest cubes (unless we use a constaint where the
	 * farthest cube has zero rate, ignoring any implied GRness).
	 * @param sectionIndex
	 * @return
	 */
	private HashMap<Integer,Float> getCubesAndFractForFaultSectionLinear(int sectionIndex) {
		HashMap<Integer,Float> wtMap = new HashMap<Integer,Float>();
		
		// Make sure long-term MFDs are created
		makeLongTermSectMFDs();

		IncrementalMagFreqDist subSeisMFD = longTermSubSeisMFD_OnSectList.get(sectionIndex);
		IncrementalMagFreqDist supraSeisMFD = longTermSupraSeisMFD_OnSectArray[sectionIndex];
		
		double minSupraSeisMag = subSeisMFD.getMaxMagWithNonZeroRate() + 0.1;
		if(Double.isNaN(minSupraSeisMag)) {	// this happens for Mendocino sections outside the RELM region
//			System.out.println(sectionIndex+"\t"+fssERF.getSolution().getRupSet().getFaultSectionData(sectionIndex).getName());
			return null;
		}
		
		double totSupraSeisRate = supraSeisMFD.getCumRate(6.55);
		double targetRateAtTargetDist = subSeisMFD.getCumRate(2.55)*Math.pow(10, -1*(6.5-2.5)); // assumes supra-seis does not contribute to M>2.5 rate

//		double totSupraSeisRate = supraSeisMFD.getCumRate(minSupraSeisMag);
//		IncrementalMagFreqDist trulyOffMFD = fss.getFinalTrulyOffFaultMFD().deepClone();
//		trulyOffMFD.scaleToCumRate(2.55, totSectMFD.getCumRate(2.55));
//		double targetRateAtMaxDist = trulyOffMFD.getCumRate(minSupraSeisMag);	// nearly the same rate as at cube outside polygon
			
//		// TEST HERE *****************************
//		ArrayList<IncrementalMagFreqDist> mfdList = new ArrayList<IncrementalMagFreqDist>();
//		mfdList.add(subSeisMFD);
//		mfdList.add(supraSeisMFD);
//		SummedMagFreqDist sumMFD = new SummedMagFreqDist( 2.55, 8.95, 65);
//		sumMFD.addIncrementalMagFreqDist(subSeisMFD);
//		sumMFD.addIncrementalMagFreqDist(supraSeisMFD);		
//		computeMFD_ForSrcArrays(2.05, 8.95, 70);
//		SummedMagFreqDist testSum = getCubeMFD_GriddedSeisOnly(41949); // this is just to get an MFD object
//		testSum.scaleToCumRate(0, 0d);
//		HashMap<Integer,Double> distMapTemp = getCubesAndDistancesInsideSectionPolygon(sectionIndex);
//		for(int cubeIndex:distMapTemp.keySet())
//			// but some of the grid node MFD may come from other sections with a different total rates, so the following shouldn't necessarily be the same as the above
//			testSum.addIncrementalMagFreqDist(getCubeMFD_GriddedSeisOnly(cubeIndex));
//		mfdList.add(testSum);
//		
//		GraphWindow mfd_Graph = new GraphWindow(mfdList, "MFDs"); 
//		mfd_Graph.setX_AxisLabel("Mag");
//		mfd_Graph.setY_AxisLabel("Rate");
//		mfd_Graph.setYLog(true);
//		mfd_Graph.setPlotLabelFontSize(22);
//		mfd_Graph.setAxisLabelFontSize(20);
//		mfd_Graph.setTickLabelFontSize(18);	
//		
//		ArrayList<EvenlyDiscretizedFunc> mfdListCum = new ArrayList<EvenlyDiscretizedFunc>();
//		mfdListCum.add(sumMFD.getCumRateDistWithOffset());
//		mfdListCum.add(testSum.getCumRateDistWithOffset());
//		GraphWindow mfdCum_Graph = new GraphWindow(mfdListCum, "MFDs"); 
//		mfdCum_Graph.setX_AxisLabel("Mag");
//		mfdCum_Graph.setY_AxisLabel("Cumulative Rate");
//		mfdCum_Graph.setYLog(true);
//		mfdCum_Graph.setPlotLabelFontSize(22);
//		mfdCum_Graph.setAxisLabelFontSize(20);
//		mfdCum_Graph.setTickLabelFontSize(18);	
//		
//		System.out.println("minSupraSeisMag="+minSupraSeisMag);
//		System.out.println("totSupraSeisRate="+totSupraSeisRate);
//		System.out.println("targetRateAtMaxDist="+targetRateAtMaxDist);
		
		
		HashMap<Integer,Double> distMap = getCubesAndDistancesInsideSectionPolygon(sectionIndex, 4.0);
		
//		// find farthest distance at 7 km
//		int cubeDepthIndex = getCubeDepthIndex(7.0);
//		
//		// now find the distance of farthest cube at depthKm perpendicular to a line on the surface at depthKm
//		double maxDistAt7km = -1;
//		for(int cubeIndex:distMap.keySet()) {
//			Location cubeLoc = getCubeLocationForIndex(cubeIndex);
//			if(this.getCubeDepthIndex(cubeLoc.getDepth()) != cubeDepthIndex)
//				continue;	// skip those that aren't at 7 km depth
//			if(distMap.get(cubeIndex) > maxDistAt7km)
//				maxDistAt7km = distMap.get(cubeIndex);
//		}
		
		
//System.out.println("HERE maxDistAt7km = "+maxDistAt7km);
		
		double n = distMap.size();
		if(targetRateAtTargetDist>totSupraSeisRate) {	// distribute uniformly
			for(int cubeIndex:distMap.keySet()) {
				wtMap.put(cubeIndex, (float)(1.0/n));
			}
			return wtMap;
		}

		// solve for linear-trend values a and b (r=ad+b, where d is distance and r is rate)
		double dMax=0, dMin=Double.MAX_VALUE, dSum=0;
		int cubeIndexMax=-1, cubeIndexMin=-1;
		for(int cubeIndex:distMap.keySet()) {
			double dist = distMap.get(cubeIndex);
			dSum += dist;
			if(dMax<dist) {
				dMax=dist;
				cubeIndexMax = cubeIndex;
			}
			if(dMin>dist) {
				dMin=dist;
				cubeIndexMin = cubeIndex;
			}
		}
		
//		double targetDist = getFaultSectionPolygonHalfWidth(sectionIndex, distMap.keySet());
		double targetDist = dMax;

		double a = (totSupraSeisRate-targetRateAtTargetDist)/(dSum-n*targetDist);
		double b = targetRateAtTargetDist/distMap.size() - a*targetDist;
		
		if(a > 0) {
			System.out.println("a>0 for\t"+sectionIndex+"\t"+fssERF.getSolution().getRupSet().getFaultSectionData(sectionIndex).getName()+"\t"+(dSum-n*targetDist));
//			throw new RuntimeException("Problem here");
		}
		
//		// check for negative value at maximum distance
//		if(a*dMax+b < 0.0) {
//			targetDist = dMax+1.0;
//			targetRateAtTargetDist = 0;
//			double ratio = a*targetDist+b;
//			a = (totSupraSeisRate-targetRateAtTargetDist)/(dSum-n*targetDist);
//			b = targetRateAtTargetDist/distMap.size() - a*targetDist;
//			ratio /= a*targetDist+b;
//			System.out.println("CORR\t"+ratio+"\t"+sectionIndex+"\t"+fssERF.getSolution().getRupSet().getFaultSectionData(sectionIndex).getName());
//		}
		
//System.out.println("dMin="+dMin+"\tcubeIndexMin="+cubeIndexMin+"\t"+this.getCubeLocationForIndex(cubeIndexMin));
// this will plot the closest and farthest cube MFD
//SummedMagFreqDist closestMFD = getCubeMFD(cubeIndexMin);
//SummedMagFreqDist farthestMFD = getCubeMFD(cubeIndexMax);
//ArrayList<EvenlyDiscretizedFunc> closeFarListCum = new ArrayList<EvenlyDiscretizedFunc>();
//closeFarListCum.add(closestMFD.getCumRateDistWithOffset());
//closeFarListCum.add(farthestMFD.getCumRateDistWithOffset());
//GraphWindow closeFar_Graph = new GraphWindow(closeFarListCum, "Closest, Farthest MFDs"); 
//closeFar_Graph.setX_AxisLabel("Mag");
//closeFar_Graph.setY_AxisLabel("Cumulative Rate");
//closeFar_Graph.setYLog(true);
//closeFar_Graph.setPlotLabelFontSize(22);
//closeFar_Graph.setAxisLabelFontSize(20);
//closeFar_Graph.setTickLabelFontSize(18);	

//System.out.println("N="+distMap.size()+"\tdMax="+dMax+"\tdSum="+dSum+"\ta="+a+"\tb="+b);
//System.out.println("totSupraSeisRate="+totSupraSeisRate+"\ttargetRateAtTargetDist"+targetRateAtTargetDist);
//System.out.println("targetDist = "+targetDist);

//System.out.println("getCubesAndFractForFaultSectionLinear:");		
		for(int cubeIndex:distMap.keySet()) {
			double rate = a*distMap.get(cubeIndex) + b;
			wtMap.put(cubeIndex, (float)(rate/totSupraSeisRate));
//Location loc = this.getCubeLocationForIndex(cubeIndex);
//System.out.println(cubeIndex+"\t"+distMap.get(cubeIndex)+"\t"+wtMap.get(cubeIndex)+"\t"+rate+"\t"+loc.getLongitude()+"\t"+loc.getLatitude()+"\t"+loc.getDepth());
		}
		
		// test
		double sum=0;
		boolean negValPrinted = false;
		for(int cubeIndex: wtMap.keySet()) {
			double val = wtMap.get(cubeIndex);
			if(val<0) {
				throw new RuntimeException("problem");
//				if(!negValPrinted) {
//					System.out.println("NEG VALUE\t"+val+"\t"+sectionIndex+"\t"+fssERF.getSolution().getRupSet().getFaultSectionData(sectionIndex).getName());
//					negValPrinted=true;
//				}
//				System.out.println("N="+distMap.size()+"\ttargetDist="+targetDist+"\tdSum="+dSum+"\ta="+a+"\tb="+b);
//				System.out.println("totSupraSeisRate="+totSupraSeisRate+"\ttargetRateAtTargetDist="+targetRateAtTargetDist);
//				System.out.println("val="+val+"\tdist="+distMap.get(cubeIndex));
//				System.out.println(sectionIndex+"\t"+fssERF.getSolution().getRupSet().getFaultSectionData(sectionIndex).getName());
//				System.out.println(subSeisMFD.toString());
//				System.out.println(supraSeisMFD.toString());
//
//				System.exit(-1);
			}
			sum += val;
		}
		
		if(sum == 0) {
			System.out.println("sum=null for "+fssERF.getSolution().getRupSet().getFaultSectionData(sectionIndex).getName());
			return null;
		}
		
		if(sum<0.999 || sum>1.001) {
			System.out.println("N="+distMap.size()+"\tdMax="+dMax+"\tdSum="+dSum+"\ta="+a+"\tb="+b);
			System.out.println("totSupraSeisRate="+totSupraSeisRate+"\ttargetRateAtMaxDist"+targetRateAtTargetDist);
			System.out.println(subSeisMFD.toString());
			throw new RuntimeException("problem; sum="+sum+" for sectionIndex="+sectionIndex+"\t"+
					fssERF.getSolution().getRupSet().getFaultSectionData(sectionIndex).getName());
		}

//System.out.println("sum="+sum);
//for(int cubeIndex:distMap.keySet()) {
//	System.out.println(cubeIndex+"\t"+distMap.get(cubeIndex)+"\t"+wtMap.get(cubeIndex));
//}

		return wtMap;
	}
	
	
//	/**
//	 * This shows that trace numbers are one more than sectiction numbers because the former starts at 1 for subsections
//	 */
//	public void tempSectTest() {
//		for(int s=0;s<this.rupSet.getNumSections();s++)
//			System.out.println(rupSet.getFaultSectionData(s).getName()+"\t"+rupSet.getFaultSectionData(s).getFaultTrace().getName());
//	}
	
	
	
	/**
	 * For the given section, this returns all the cubes that are within the section polygon, together
	 * with the fractional rate of the section that each cube gets using an exponential distance decay
	 * from the surface and constrained by the total section rate and that the farthest cubes above the 
	 * seismogenic depth have a perfect GR in cumulative M6.5 rate (plus any time dependence).  If the 
	 * section MFD is non-characteristic, then the rates are distributed uniformly among cubes.
	 * @param sectionIndex
	 * @return
	 */
	private HashMap<Integer,Float> getCubesAndFractForFaultSectionExponential(int sectionIndex) {
		
		double targetFractDiff = 0.0001;
		HashMap<Integer,Float> wtMap = new HashMap<Integer,Float>();
		
// System.out.println("working on sect index "+sectionIndex+"\t"+rupSet.getFaultSectionData(sectionIndex).getName());
		// Make sure long-term MFDs are created
		makeLongTermSectMFDs();

		IncrementalMagFreqDist subSeisMFD = longTermSubSeisMFD_OnSectList.get(sectionIndex);
		IncrementalMagFreqDist supraSeisMFD = longTermSupraSeisMFD_OnSectArray[sectionIndex];
		
		double minSupraSeisMag = subSeisMFD.getMaxMagWithNonZeroRate() + 0.1;
		if(Double.isNaN(minSupraSeisMag)) {	// this happens for Mendocino sections outside the RELM region
			System.out.println("NULL for section:\t"+sectionIndex+"\t"+fssERF.getSolution().getRupSet().getFaultSectionData(sectionIndex).getName());
			return null;
		}
		
//		double totSupraSeisRate = supraSeisMFD.getCumRate(6.55);
//		double targetRateAtTargetDist = subSeisMFD.getCumRate(2.55)*Math.pow(10, -1*(6.5-2.5)); // assumes supra-seis does not contribute to M>2.5 rate
		double totSupraSeisRate = supraSeisMFD.getTotalIncrRate();
		double targetRateAtTargetDist = totSupraSeisRate*ETAS_Utils.getScalingFactorToImposeGR(supraSeisMFD, subSeisMFD, false);

		HashMap<Integer,Double> cubeDistMap = new HashMap<Integer,Double>();
		Region faultPolygon = faultPolyMgr.getPoly(sectionIndex);
		StirlingGriddedSurface surface = rupSet.getFaultSectionData(sectionIndex).getStirlingGriddedSurface(0.25, false, true);
		double lowerSeisDepth = surface.getLowerSeismogenicDepth();
		double targetDist = 0; // max dist within seismo depth
		for(int i=0; i<numCubes;i++) {
			Location cubeLoc = getCubeLocationForIndex(i);
			if(faultPolygon.contains(cubeLoc)) {
				double dist = LocationUtils.distanceToSurf(cubeLoc, surface);
				cubeDistMap.put(i, dist);
				if(cubeLoc.getDepth()<=lowerSeisDepth && dist>targetDist) {
					targetDist = dist;
				}				
			}
		}
						
		double n = cubeDistMap.size();
		
		// this is needed for Mendocino subsection 20; it has a subseimo mfd according to Peter's rules (barely, see email from him), but no cubes withing according to rules here
		if(n==0) {
			System.out.println("NULL for section (no cubes):\t"+sectionIndex+"\t"+fssERF.getSolution().getRupSet().getFaultSectionData(sectionIndex).getName());
			return null;			
		}
		
		
		if(targetRateAtTargetDist>totSupraSeisRate) {	// distribute uniformly
			for(int cubeIndex:cubeDistMap.keySet()) {
				wtMap.put(cubeIndex, (float)(1.0/n));
			}
			System.out.println("UNIFORM CUBE RATES\t"+sectionIndex+"\t"+rupSet.getFaultSectionData(sectionIndex).getName());
			return wtMap;
		}

		
 //System.out.println("totSupraSeisRate="+totSupraSeisRate+"\ttargetRateAtTargetDist"+targetRateAtTargetDist+"\tN="+cubeDistMap.size());

 //System.exit(0);

		targetRateAtTargetDist /= cubeDistMap.size();
		
 //System.out.println("Reduced targetRateAtTargetDist="+targetRateAtTargetDist+"\ttargetDist="+targetDist);

		// solve for log-linear-trend values a and b (R=10^(a-bD), where D is distance and R is rate in ith cube)
		double b_trial = 0;
		double b_incr = 1.0;
		double b = Double.NaN;
		int numTries=0;
		boolean done = false;
		while(!done) {
			double b1 = b_trial;
			double totTestRate1 = 0;
			for(int cubeIndex:cubeDistMap.keySet()) {
				totTestRate1 += targetRateAtTargetDist*Math.pow(10.0,b1*(targetDist-cubeDistMap.get(cubeIndex)));
			}
			double fractDiff1 = Math.abs((totSupraSeisRate-totTestRate1)/totSupraSeisRate);
			if(fractDiff1<targetFractDiff) {
				b=b1;
				done=true;
//System.out.println("Done1:\t"+b1+"\t"+totTestRate1+"\t"+totSupraSeisRate+"\t"+fractDiff1);
				continue;
			}
	
			double b2 = b_trial+b_incr;
			double totTestRate2 = 0;
			for(int cubeIndex:cubeDistMap.keySet()) {
				totTestRate2 += targetRateAtTargetDist*Math.pow(10.0,b2*(targetDist-cubeDistMap.get(cubeIndex)));
			}
			double fractDiff2 = Math.abs((totSupraSeisRate-totTestRate2)/totSupraSeisRate);
			if(fractDiff2<targetFractDiff) {
				b=b2;
				done=true;
 //System.out.println("Done2:\t"+b2+"\t"+totTestRate2+"\t"+totSupraSeisRate+"\t"+fractDiff2);
				continue;
			}

			
//System.out.println(b_trial+"\t"+b_incr+"\t"+totTestRate1+"\t"+totTestRate2+"\t"+totSupraSeisRate);
if(numTries >1000000)
System.exit(0);

			// if it's bounded by the two tries above
			if(totTestRate1<totSupraSeisRate && totTestRate2>totSupraSeisRate) {
				b_incr /= 10.0;	// do again with smaller increment
			}
			else { // step to next increment
				b_trial += b_incr;
			}
		}
		
		double a = Math.log10(targetRateAtTargetDist) + b*targetDist;
		
 //System.out.println("N="+cubeDistMap.size()+"\ta="+a+"\tb="+b);
		

		for(int cubeIndex:cubeDistMap.keySet()) {
			double rate = Math.pow(10, a - b*cubeDistMap.get(cubeIndex));
			wtMap.put(cubeIndex, (float)(rate/totSupraSeisRate));
//Location loc = this.getCubeLocationForIndex(cubeIndex);
//System.out.println(cubeIndex+"\t"+cubeDistMap.get(cubeIndex)+"\t"+wtMap.get(cubeIndex)+"\t"+rate+"\t"+loc.getLongitude()+"\t"+loc.getLatitude()+"\t"+loc.getDepth());
		}
		
		// test
		double sum=0;
		for(int cubeIndex: wtMap.keySet()) {
			double val = wtMap.get(cubeIndex);
			if(val<0) {
				throw new RuntimeException("problem");
			}
			sum += val;
		}
		
		if(sum == 0) {
			System.out.println("sum=null for "+fssERF.getSolution().getRupSet().getFaultSectionData(sectionIndex).getName());
			return null;
		}
		
		if(sum<0.999 || sum>1.001) {
			System.out.println("N="+cubeDistMap.size()+"\ta="+a+"\tb="+b);
			System.out.println("totSupraSeisRate="+totSupraSeisRate+"\ttargetRateAtMaxDist"+targetRateAtTargetDist);
			System.out.println(subSeisMFD.toString());
			throw new RuntimeException("problem; sum="+sum+" for sectionIndex="+sectionIndex+"\t"+
					fssERF.getSolution().getRupSet().getFaultSectionData(sectionIndex).getName());
		}

		return wtMap;
	}

	
	
	/**
	 * For the given section, this returns all the cubes that are within the section polygon, together
	 * with the fractional rate of the section that each cube gets using an exponential distance decay
	 * from the surface and constrained by the total section rate and that the farthest cubes above the 
	 * seismogenic depth have a perfect GR in cumulative M6.5 rate (plus any time dependence).  If the 
	 * section MFD is non-characteristic, then the rates are distributed uniformly among cubes.
	 * 
	 * This was tested on the Mojave section only
	 * @param sectionIndex
	 * @return
	 */
	private HashMap<Integer,Float> getCubesAndFractForFaultSectionPower(int sectionIndex) {
		
		double targetFractDiff = 0.0001;
		HashMap<Integer,Float> wtMap = new HashMap<Integer,Float>();
		
		System.out.println("starting getCubesAndFractForFaultSectionExponential");
		// Make sure long-term MFDs are created
		makeLongTermSectMFDs();

		IncrementalMagFreqDist subSeisMFD = longTermSubSeisMFD_OnSectList.get(sectionIndex);
		IncrementalMagFreqDist supraSeisMFD = longTermSupraSeisMFD_OnSectArray[sectionIndex];
		
		double minSupraSeisMag = subSeisMFD.getMaxMagWithNonZeroRate() + 0.1;
		if(Double.isNaN(minSupraSeisMag)) {	// this happens for Mendocino sections outside the RELM region
//			System.out.println(sectionIndex+"\t"+fssERF.getSolution().getRupSet().getFaultSectionData(sectionIndex).getName());
			return null;
		}
		
		double totSupraSeisRate = supraSeisMFD.getCumRate(6.55);
		double targetRateAtTargetDist = subSeisMFD.getCumRate(2.55)*Math.pow(10, -1*(6.5-2.5)); // assumes supra-seis does not contribute to M>2.5 rate

		HashMap<Integer,Double> cubeDistMap = new HashMap<Integer,Double>();
		Region faultPolygon = faultPolyMgr.getPoly(sectionIndex);
		StirlingGriddedSurface surface = rupSet.getFaultSectionData(sectionIndex).getStirlingGriddedSurface(0.25, false, true);
		double lowerSeisDepth = surface.getLowerSeismogenicDepth();
		double targetDist = 0; // max dist within seismo depth
		for(int i=0; i<numCubes;i++) {
			Location cubeLoc = getCubeLocationForIndex(i);
			if(faultPolygon.contains(cubeLoc)) {
				double dist = LocationUtils.distanceToSurf(cubeLoc, surface);
				cubeDistMap.put(i, dist);
				if(cubeLoc.getDepth()<=lowerSeisDepth && dist>targetDist) {
					targetDist = dist;
				}				
			}
		}
						
		double n = cubeDistMap.size();
		if(targetRateAtTargetDist>totSupraSeisRate) {	// distribute uniformly
			for(int cubeIndex:cubeDistMap.keySet()) {
				wtMap.put(cubeIndex, (float)(1.0/n));
			}
			return wtMap;
		}

System.out.println("totSupraSeisRate="+totSupraSeisRate+"\ttargetRateAtTargetDist"+targetRateAtTargetDist+"\tN="+cubeDistMap.size());

		targetRateAtTargetDist /= cubeDistMap.size();
		
System.out.println("Reduced targetRateAtTargetDist="+targetRateAtTargetDist+"\ttargetDist="+targetDist);

		// solve for power-law trend values Ri = (Di+c)^-x, where Di is distance and Ri is rate in ith cube)
		double x_trial = 1e-6;
		double x_incr = 1.0;
		double x = Double.NaN;
		int numTries=0;
		boolean done = false;
		while(!done) {
			double x1 = x_trial;
			double totTestRate1 = 0;
			for(int cubeIndex:cubeDistMap.keySet()) {
				double val = cubeDistMap.get(cubeIndex) + Math.pow(targetRateAtTargetDist, -1.0/x1) - targetDist;
				totTestRate1 += Math.pow(val, -x1);
if(Double.isNaN(totTestRate1)) {
	System.out.println("HERE:\t"+cubeDistMap.get(cubeIndex)+"\t"+x1+"\t"+targetRateAtTargetDist+"\t"+Math.pow(targetRateAtTargetDist,x1)+"\t"+targetDist+"\t"+val+"\t"+Math.pow(val, -x1));
	System.exit(0);
}
			}
			double fractDiff1 = Math.abs((totSupraSeisRate-totTestRate1)/totSupraSeisRate);
			if(fractDiff1<targetFractDiff) {
				x=x1;
				done=true;
System.out.println("Done1:\t"+x1+"\t"+totTestRate1+"\t"+totSupraSeisRate+"\t"+fractDiff1);
				continue;
			}
	
			double x2 = x_trial+x_incr;
			double totTestRate2 = 0;
			for(int cubeIndex:cubeDistMap.keySet()) {
				double val = cubeDistMap.get(cubeIndex) + Math.pow(targetRateAtTargetDist, -1.0/x2) - targetDist;
				totTestRate2 += Math.pow(val, -x2);
			}
			double fractDiff2 = Math.abs((totSupraSeisRate-totTestRate2)/totSupraSeisRate);
			if(fractDiff2<targetFractDiff) {
				x=x2;
				done=true;
System.out.println("Done2:\t"+x2+"\t"+totTestRate2+"\t"+totSupraSeisRate+"\t"+fractDiff2);
				continue;
			}

			
System.out.println(x_trial+"\t"+x_incr+"\t"+totTestRate1+"\t"+totTestRate2+"\t"+totSupraSeisRate+"\t"+fractDiff1+"\t"+fractDiff2);
if(numTries >200)
System.exit(0);

			// if it's bounded by the two tries above
			if(totTestRate1<totSupraSeisRate && totTestRate2>totSupraSeisRate) {
				x_incr /= 10.0;	// do again with smaller increment
			}
			else { // step to next increment
				x_trial += x_incr;
			}
		}
		
		double c = Math.pow(targetRateAtTargetDist,-1.0/x) - targetDist;
		
System.out.println("N="+cubeDistMap.size()+"\tc="+c+"\tx="+x);
		

		for(int cubeIndex:cubeDistMap.keySet()) {
			double rate = Math.pow(cubeDistMap.get(cubeIndex) + c, -x);
			wtMap.put(cubeIndex, (float)(rate/totSupraSeisRate));
Location loc = this.getCubeLocationForIndex(cubeIndex);
System.out.println(cubeIndex+"\t"+cubeDistMap.get(cubeIndex)+"\t"+wtMap.get(cubeIndex)+"\t"+rate+"\t"+loc.getLongitude()+"\t"+loc.getLatitude()+"\t"+loc.getDepth());
		}
		
		// test
		double sum=0;
		for(int cubeIndex: wtMap.keySet()) {
			double val = wtMap.get(cubeIndex);
			if(val<0) {
				throw new RuntimeException("problem");
			}
			sum += val;
		}
		
		if(sum == 0) {
			System.out.println("sum=null for "+fssERF.getSolution().getRupSet().getFaultSectionData(sectionIndex).getName());
			return null;
		}
		
		if(sum<0.999 || sum>1.001) {
			System.out.println("N="+cubeDistMap.size()+"\tc="+c+"\tx="+x);
			System.out.println("totSupraSeisRate="+totSupraSeisRate+"\ttargetRateAtMaxDist"+targetRateAtTargetDist);
			System.out.println(subSeisMFD.toString());
			throw new RuntimeException("problem; sum="+sum+" for sectionIndex="+sectionIndex+"\t"+
					fssERF.getSolution().getRupSet().getFaultSectionData(sectionIndex).getName());
		}

		return wtMap;
	}

	
	
	
	
	/**
	 * This generates the following cached data and writes them to files:
	 * 
	 * sections in cubes saved to defaultSectInCubeCacheFilename
	 * fractions of sections in cubes saved to defaultFractSectInCubeCacheFilename
	 * whether cube center is inside one or more fault-section polygons is saved to defaultCubeInsidePolyCacheFilename
	 * 
	 */
	private void generateAndWriteCacheDataToFiles() {
		if(D) System.out.println("Starting ETAS.ETAS_PrimaryEventSampler.generateAndWriteListListDataToFile(); THIS WILL TAKE TIME AND MEMORY!");
		long st = System.currentTimeMillis();
		CalcProgressBar progressBar = null;
		try {
			progressBar = new CalcProgressBar("Sections to process in generateAndWriteCacheDataToFiles()", "junk");
		} catch (Exception e1) {} // headless
		ArrayList<ArrayList<Integer>> sectAtPointList = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Float>> fractionsAtPointList = new ArrayList<ArrayList<Float>>();

		int numSect = rupSet.getNumSections();
		for(int i=0; i<numCubes;i++) {
			sectAtPointList.add(new ArrayList<Integer>());
			fractionsAtPointList.add(new ArrayList<Float>());
		}
		
		boolean distributeOverPolygon = true;	// otherwise section surface assigned only to cubes it passes through
		double surfGridSpacing = 1.0;	// TODO decrease for greater accuracy; this only applies if distributeOverPolygon=false.
		if (progressBar != null) progressBar.showProgress(true);
		for(int s=0;s<numSect;s++) {
			if (progressBar != null) progressBar.updateProgress(s, numSect);
			
			if(distributeOverPolygon) {
//				HashMap<Integer,Float> cubeFractMap = getCubesAndFractForFaultSectionLinear(s);
				HashMap<Integer,Float> cubeFractMap = getCubesAndFractForFaultSectionExponential(s);
				if(cubeFractMap != null) {	// null for some Mendocino sections because they are outside the RELM region
					for(int cubeIndex:cubeFractMap.keySet()) {
						sectAtPointList.get(cubeIndex).add(s);
						fractionsAtPointList.get(cubeIndex).add(cubeFractMap.get(cubeIndex));
					}			
				}
			}
			
			else {
				Hashtable<Integer,Float> fractAtPointTable = new Hashtable<Integer,Float>(); // int is ptIndex and double is fraction there
				LocationList locsOnSectSurf = rupSet.getFaultSectionData(s).getStirlingGriddedSurface(surfGridSpacing, false, true).getEvenlyDiscritizedListOfLocsOnSurface();
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
		
System.exit(0);

		// Now compute which cubes are inside polygons
		if (D) System.out.println("Starting on insidePoly calculation");
		int numCubes = gridRegForCubes.getNodeCount();
		int[] insidePoly = new int[numCubes];	// default values are really 0
		
		if(erf instanceof FaultSystemSolutionERF) {	// otherwise all our outside polygons, and default values of 0 are appropriate
						
			GridSourceProvider gridSrcProvider = ((FaultSystemSolutionERF)erf).getSolution().getGridSourceProvider();
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
			if(D) {
				System.out.println(numCubesInside+" are inside polygons, out of "+numCubes);
				System.out.println(numBad+" were bad");
			}
		}
		
		if (progressBar != null) progressBar.showProgress(false);
		
		File cubeInsidePolyFile = new File(defaultCubeInsidePolyCacheFilename);
		try {
			MatrixIO.intArrayToFile(insidePoly,cubeInsidePolyFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// test by reading file and comparing
		try {
			int[] insidePoly2 = MatrixIO.intArrayFromFile(cubeInsidePolyFile);
			boolean ok = true;
			for(int i=0;i<insidePoly2.length;i++)
				if(insidePoly[i] != insidePoly2[i])
					ok = false;
			if(!ok) {
				throw new RuntimeException("Problem with file");
			}
		} catch (IOException e) {
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
//		InversionFaultSystemRupSet rupSet = (InversionFaultSystemRupSet)((FaultSystemSolutionERF)erf).getSolution().getRupSet();
//		FaultPolyMgr faultPolyMgr = rupSet.getInversionTargetMFDs().getGridSeisUtils().getPolyMgr();
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
			
//if(i == 255418-numFltSystSources) {
//	System.out.println("HERE IT IS "+i+"\t"+gridSeisStatus[i]+"\t"+gridSrcProvider.getGriddedRegion().getLocation(i)+
//			"\tfrac="+frac+"\t(subSeisMFD == null)="+(subSeisMFD == null)+"\t(trulyOffMFD == null)="+(trulyOffMFD == null));
//}
			
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
	public IntegerPDF_FunctionSampler old_getAveSamplerForRupture(EqkRupture mainshock) {
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
	
	/**
	 * This loops over all points on the rupture surface and creates a net (average) point sampler.
	 * Note that this moves each point on the rupture surface horizontally to four different equally-
	 * weighted points at +/- 0.025 degrees in lat and lon.  This is to stabilize the probability of
	 * triggering the sections that extend off the ends off the fault rupture, where the relative 
	 * likelihood of triggering one versus the other can change by 70% because of discretization issues 
	 * (e.g., sliding the rupture down a tiny bit can change one or both end points to jump between haveing 
	 * 4 versus 8 adjacent cubes with the unruptured extension in it).
	 * @param mainshock
	 * @return
	 */
	public IntegerPDF_FunctionSampler getAveSamplerForRupture(EqkRupture mainshock) {
		long st = System.currentTimeMillis();
		IntegerPDF_FunctionSampler aveSampler = new IntegerPDF_FunctionSampler(numCubes);
		
		CalcProgressBar progressBar=null;
		if(D) {
			progressBar = new CalcProgressBar("getAveSamplerForRupture(*)", "junk");
			progressBar.showProgress(true);
		}
		
		LocationList locList = mainshock.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();
		int progress =0;
		for(Location loc: locList) {
			
			if(D) {
				progressBar.updateProgress(progress, locList.size());
				progress += 1;
			}
			
			// set the sampler
			ArrayList<Location> locList2 = new ArrayList<Location>();
			locList2.add(new Location(loc.getLatitude()+0.005,loc.getLongitude()+0.005,loc.getDepth()));
			locList2.add(new Location(loc.getLatitude()+0.005,loc.getLongitude()-0.005,loc.getDepth()));
			locList2.add(new Location(loc.getLatitude()-0.005,loc.getLongitude()+0.005,loc.getDepth()));
			locList2.add(new Location(loc.getLatitude()-0.005,loc.getLongitude()-0.005,loc.getDepth()));
			for(Location transLoc : locList2) {
				int parLocIndex = getParLocIndexForLocation(transLoc);
				IntegerPDF_FunctionSampler sampler = getCubeSampler(parLocIndex, false);
				
				for(int i=0;i <numCubes;i++) {
					aveSampler.add(i, sampler.getY(i));
				}				
			}
			
		}
		if(D) {
			progressBar.showProgress(false);
			double sec = ((double)(System.currentTimeMillis()-st))/1000d;
			System.out.println("getAveSamplerForRupture() took (sec): "+(float)sec);
		}
		return aveSampler;
	}

	
	/**
	 * TODO No longer used?
	 * @param mainshock
	 */
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
	 * This test takes forever on a single cube, and so should be removed or modified.
	 * TODO remove this test
	 */
	public void testRandomSourcesFromCubes() {
		long minNumSamples = 1;

		System.out.println("testRandomSourcesFromCubes():");
		System.out.println("\tloop over cubes...");
		CalcProgressBar progressBar = new CalcProgressBar("loop over cubes", "junk");
		progressBar.showProgress(true);
		for(int c=0;c<numCubes;c++) {
			progressBar.updateProgress(c, numCubes);
			
			int griddeSeisRegionIndex = origGriddedRegion.indexForLocation(getCubeLocationForIndex(c));
			if(griddeSeisRegionIndex == -1)
				continue;
			Hashtable<Integer,Double> srcProbInCube = getRelativeTriggerProbOfSourcesInCube(c);
			if(srcProbInCube == null || srcProbInCube.size() == 1)
				continue;
			double minVal = 1;
			for(int srcIndex:srcProbInCube.keySet()) {
				double prob = srcProbInCube.get(srcIndex);
				if(!Double.isNaN(prob))
					if(prob < minVal)
						minVal = prob;
			}	
			
			if(minVal == 1) continue;
			
			long numSamples = minNumSamples*(long)(1.0/(double)minVal);
			
			Hashtable<Integer,Double> testSrcProbInCube = new Hashtable<Integer,Double>();
			CalcProgressBar samplesProgressBar = new CalcProgressBar("loop over samples for cube "+c, "junk");
			samplesProgressBar.showProgress(true);
			for(long i=0;i<numSamples;i++) {
				progressBar.updateProgress(i, numSamples);
				int srcIndex = getRandomSourceIndexInCube(c);
				if(testSrcProbInCube.containsKey(srcIndex)) {
					double newVal = testSrcProbInCube.get(srcIndex) + 1.0/(double)numSamples;
					testSrcProbInCube.put(srcIndex, newVal);
				}
				else {
					testSrcProbInCube.put(srcIndex, 1.0/(double)numSamples);
				}
			}
			samplesProgressBar.showProgress(false);
			System.out.println("cube "+c+"\t"+numSamples);
			for(int srcIndex: srcProbInCube.keySet()) {
				double val = srcProbInCube.get(srcIndex);
				double testVal = Double.NaN;
				if(testSrcProbInCube.containsKey(srcIndex)) {
					testVal = testSrcProbInCube.get(srcIndex);
				}
				System.out.println("\t"+srcIndex+"\t"+testVal+"\t"+val+"\t"+(testVal/val));
			}
			
			System.exit(-1);
		}
		progressBar.showProgress(false);
	}

	
	/**
	 * This tests whether source rates can be recovered from the source rates in each 
	 * cube (getNucleationRatesOfSourcesInCube()).  Results are good except at grid sources along
	 * the edge of the RELM region and for Mendocino sources that are partially outside the region.
	 */
	public void testNucleationRatesOfSourcesInCubes() {
		System.out.println("testNucleationRatesOfSourcesInCubes():");
		double[] testSrcRate = new double[this.sourceRates.length];
		System.out.println("\tloop over cubes...");
		CalcProgressBar progressBar = new CalcProgressBar("loop over cubes", "junk");
		progressBar.showProgress(true);
		for(int c=0;c<numCubes;c++) {
			progressBar.updateProgress(c, numCubes);
			Hashtable<Integer,Double> srcRatesInCube = this.getNucleationRatesOfSourcesInCube(c);
			if(srcRatesInCube != null) {
				for(int srcIndex:srcRatesInCube.keySet()) {
					double rate = srcRatesInCube.get(srcIndex);
					if(!Double.isNaN(rate))
						testSrcRate[srcIndex] += rate;
				}				
			}
		}
		progressBar.showProgress(false);
		
		System.out.println("\tloop over sources...");
		progressBar = new CalcProgressBar("loop over sources", "junk");
		progressBar.showProgress(true);
		for(int srcIndex=0; srcIndex < sourceRates.length; srcIndex++) {
			progressBar.updateProgress(srcIndex, sourceRates.length);
			double fractDiff = Math.abs(testSrcRate[srcIndex]-sourceRates[srcIndex])/sourceRates[srcIndex];
			if(fractDiff>0.0001) {
				int gridRegionIndex = srcIndex-numFltSystSources;
				if(gridRegionIndex>=0) {
					Location loc = this.origGriddedRegion.getLocation(gridRegionIndex);
					System.out.println("\tDiff="+(float)fractDiff+" for "+srcIndex+"; "+this.fssERF.getSource(srcIndex).getName()+"\t"+loc.getLatitude()+"\t"+loc.getLongitude()+"\t"+loc.getDepth());			
				}
				else {
					System.out.println("\tDiff="+(float)fractDiff+" for "+srcIndex+"; "+this.fssERF.getSource(srcIndex).getName());			
				}
			}
		}
		progressBar.showProgress(false);
	}

	
	
	/**
	 * For the given sampler, this gives the relative trigger probability of each source in the ERF.
	 * @param sampler
	 */
	public double[] getRelativeTriggerProbOfEachSource(IntegerPDF_FunctionSampler sampler) {
		long st = System.currentTimeMillis();
		double[] trigProb = new double[erf.getNumSources()];
		
//		IntegerPDF_FunctionSampler aveSampler = getAveSamplerForRupture(mainshock);

		// normalize so values sum to 1.0
		sampler.scale(1.0/sampler.getSumOfY_vals());
		
		CalcProgressBar progressBar = null;
		if(D) {
			progressBar = new CalcProgressBar("getRelativeTriggerProbOfEachSource", "junk");
			progressBar.showProgress(true);
		}
		
		// now loop over all cubes
		for(int i=0;i <numCubes;i++) {
			if(D) 
				progressBar.updateProgress(i, numCubes);

			Hashtable<Integer,Double>  relSrcProbForCube = getRelativeTriggerProbOfSourcesInCube(i);
			if(relSrcProbForCube != null) {
				for(int srcKey:relSrcProbForCube.keySet()) {
					trigProb[srcKey] += sampler.getY(i)*relSrcProbForCube.get(srcKey);
				}
			}
//			else {
//				// I confirmed that all of these are around the edges
//				Location loc = getCubeLocationForIndex(i);
//				System.out.println("relSrcProbForCube is null for cube index "+i+"\t"+loc.getLongitude()+"\t"+loc.getLatitude()+"\t"+loc.getDepth());
//			}
		}
		
		if(D)
			progressBar.showProgress(false);

		double testSum=0;
		for(int s=0; s<trigProb.length; s++)
			testSum += trigProb[s];
		if(testSum<0.9999 || testSum>1.0001)
			throw new RuntimeException("PROBLEM");
		
		if(D) {
			st = System.currentTimeMillis()-st;
			System.out.println("getRelativeTriggerProbOfEachSource took:"+((float)st/1000f)+" sec");			
		}
		
		return trigProb;
	}
	
	
	/**
	 * This takes relSrcProbs rather than a rupture in order to avoid repeating that calculation
	 * @param sampler
	 * @return ArrayList<SummedMagFreqDist>; index 0 has total MFD, and index 1 has supra-seis MFD,
	 * and index 2 has sub-seis MFD.
	 */
	public List<SummedMagFreqDist> getExpectedPrimaryMFD_PDF(double[] relSrcProbs) {
		
		long st = System.currentTimeMillis();

//		double[] relSrcProbs = getRelativeTriggerProbOfEachSource(sampler);
		SummedMagFreqDist magDist = new SummedMagFreqDist(2.05, 8.95, 70);
		SummedMagFreqDist supraMagDist = new SummedMagFreqDist(2.05, 8.95, 70);
		SummedMagFreqDist subSeisMagDist = new SummedMagFreqDist(2.05, 8.95, 70);
		SummedMagFreqDist srcMFD;
		
		double testTotProb = 0;
		for(int s=0; s<relSrcProbs.length;s++) {
			if(mfdForSrcArray == null)
				srcMFD = ERF_Calculator.getTotalMFD_ForSource(erf.getSource(s), 1.0, 2.05, 8.95, 70, true);
			else
				srcMFD = (SummedMagFreqDist)mfdForSrcArray[s].deepClone();
			srcMFD.normalizeByTotalRate();	// change to PDF
			srcMFD.scale(relSrcProbs[s]);
			double totMFD_Prob = srcMFD.getTotalIncrRate();
			if(!Double.isNaN(totMFD_Prob)) {// not sure why this is needed
				testTotProb += totMFD_Prob;
				magDist.addIncrementalMagFreqDist(srcMFD);
				if(s<numFltSystSources)
					supraMagDist.addIncrementalMagFreqDist(srcMFD);
				else
					subSeisMagDist.addIncrementalMagFreqDist(srcMFD);
			}
		}
		ArrayList<SummedMagFreqDist> mfdList = new ArrayList<SummedMagFreqDist>();
		mfdList.add(magDist);
		mfdList.add(supraMagDist);
		mfdList.add(subSeisMagDist);

		if(D) {
			System.out.println("\ttestTotProb="+testTotProb);
			st = System.currentTimeMillis()-st;
			System.out.println("getExpectedPrimaryMFD_PDF took:"+((float)st/1000f)+" sec");			
		}
		
		return mfdList;
	}
	
	
	/**
	 * 
	 * @param mainshock
	 * @return ArrayList<SummedMagFreqDist>; index 0 has total MFD, and index 1 has supra-seis MFD
	 */
	public List<SummedMagFreqDist> getExpectedPrimaryMFD_PDF_Alt(IntegerPDF_FunctionSampler sampler) {
		// normalize so values sum to 1.0
		sampler.scale(1.0/sampler.getSumOfY_vals());

		SummedMagFreqDist magDist = new SummedMagFreqDist(2.05, 8.95, 70);
		SummedMagFreqDist supraMagDist = new SummedMagFreqDist(2.05, 8.95, 70);
		for(int i=0;i<sampler.size(); i++) {
			SummedMagFreqDist mfd = getCubeMFD(i);
			if(mfd != null) {
				double total = mfd.getTotalIncrRate();
				mfd.scale(sampler.getY(i)/total);
				magDist.addIncrementalMagFreqDist(mfd);
				SummedMagFreqDist mfdSupra = getCubeMFD_SupraSeisOnly(i);
				if(mfdSupra != null) {
					mfdSupra.scale(sampler.getY(i)/total);
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
	 * This plots the probability that each subsection will trigger a
	 * supra-seis primary aftershocks, given all expected primary aftershocks.  
	 * 
	 * This also returns a String with a list of the top numToList fault-based 
	 * sources and top sections (see next method below).
	 * 
	 * TODO move this to ETAS_SimAnalysisTools
	 */
	public String plotSubSectTriggerProbGivenAllPrimayEvents(IntegerPDF_FunctionSampler sampler, File resultsDir, int numToList, 
			String nameSuffix, double expectedNumSupra) {
		String info = "";
		if(erf instanceof FaultSystemSolutionERF) {
			FaultSystemSolutionERF tempERF = (FaultSystemSolutionERF)erf;

			// normalize so values sum to 1.0
			sampler.scale(1.0/sampler.getSumOfY_vals());

			double[] sectProbArray = new double[rupSet.getNumSections()];

			// now loop over all cubes
			for(int i=0;i <numCubes;i++) {
				int[] sectInCubeArray = sectInCubeList.get(i);
				float[] fractInCubeArray = fractionSectInCubeList.get(i);
				double sum = 0;
				for(int s=0;s<sectInCubeArray.length;s++) {
					int sectIndex = sectInCubeArray[s];
					sum += totSectNuclRateArray[sectIndex]*fractInCubeArray[s];
				}
				sum += getGridSourcRateInCube(i);
				if(sum > 0) {	// avoid division by zero if all rates are zero
					for(int s=0;s<sectInCubeArray.length;s++) {
						int sectIndex = sectInCubeArray[s];
						double val = totSectNuclRateArray[sectIndex]*fractInCubeArray[s]*sampler.getY(i)/sum;
						sectProbArray[sectIndex] += val;

// cubes for sections off ends of Mojave scenario						
//if(sectIndex==1836 || sectIndex==1846) {
//	Location loc = this.getCubeLocationForIndex(i);
//	System.out.println(i+"\t"+loc.getLatitude()+"\t"+loc.getLongitude()+"\t"+loc.getDepth()+"\t"+sampler.getY(i)+"\t"+totSectNuclRateArray[sectIndex]+"\t"+fractInCubeArray[s]
//			+"\t"+val+"\t"+sum+"\t"+getGridSourcRateInCube(i)+"\t"+rupSet.getFaultSectionData(sectIndex).getName());
//}
					}
				}
			}

			// normalize:
			double sum=0;
			for(double val:sectProbArray)
				sum += val;
System.out.println("SUM TEST HERE (prob of flt rup given primary event): "+sum);

			double min=Double.MAX_VALUE, max=0.0;
			for(int sect=0;sect<sectProbArray.length;sect++) {
				sectProbArray[sect] *= expectedNumSupra/sum;
				if(sectProbArray[sect]<1e-16) // to avoid log-space problems
					sectProbArray[sect]=1e-16;
			}
			
			min = -5;
			max = 0;
			CPT cpt = FaultBasedMapGen.getParticipationCPT().rescale(min, max);;
			List<FaultSectionPrefData> faults = rupSet.getFaultSectionDataList();

			//			// now log space
			double[] values = FaultBasedMapGen.log10(sectProbArray);


			String name = "SectTriggerProb"+nameSuffix;
			String title = "Log10(Trigger Prob)";
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
	
	
	
	public void writeGMT_PieSliceDecayData(Location parLoc, String fileNamePrefix) {
		
		this.addParentLocToProcess(parLoc);
		
		double sliceLenghtDegrees = 2;
		
		try {
			FileWriter fileWriterGMT = new FileWriter(new File(GMT_CA_Maps.GMT_DIR, fileNamePrefix+"GMT.txt"));
			FileWriter fileWriterSCECVDO = new FileWriter(new File(GMT_CA_Maps.GMT_DIR, fileNamePrefix+"SCECVDO.txt"));
			CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance();
			
			int parLocIndex = getParLocIndexForLocation(parLoc);
			Location translatedParLoc = getParLocationForIndex(parLocIndex);
			float parLat = (float) translatedParLoc.getLatitude();
			float parLon = (float) translatedParLoc.getLongitude();
			IntegerPDF_FunctionSampler sampler = getCubeSampler(parLocIndex, true);
			// normalize
			sampler.scale(1.0/sampler.getSumOfY_vals());

			
//			// compute min and max
//			double minVal=Double.POSITIVE_INFINITY, maxVal=Double.NEGATIVE_INFINITY;
//			for(int i=0; i<sampler.size();i++) {
//				Location loc = this.getCubeLocationForIndex(i);
//				double latDiff = Math.abs(loc.getLatitude()-parLoc.getLatitude());
//				double lonDiff = Math.abs(loc.getLongitude()-parLoc.getLongitude());
//				double distDegrees = Math.sqrt(latDiff*latDiff+lonDiff*lonDiff);
//				if(distDegrees>sliceLenghtDegrees)
//					continue;
//				double val = Math.log10(sampler.getY(i));
//				if(val<-15)
//					continue;
//				if(minVal>val)
//					minVal=val;
//				if(maxVal<val)
//					maxVal=val;
//			}
//			
//			System.out.println("minVal="+minVal+"\tmaxVal="+maxVal);
//			System.out.println("Math.round(minVal)="+Math.round(minVal)+"\tMath.round(maxVal)="+Math.round(maxVal));
//	        cpt = cpt.rescale(Math.round(minVal+3), Math.round(maxVal));
	        
	        // hard coded:
	        cpt = cpt.rescale(-9.0, -1.0);
	        
	        cpt.writeCPTFile(new File(GMT_CA_Maps.GMT_DIR, fileNamePrefix+"_CPT.txt"));
			
			double halfCubeLatLon = cubeLatLonSpacing/2.0;
			double halfCubeDepth = depthDiscr/2.0;
			double startCubeLon = translatedParLoc.getLongitude() + halfCubeLatLon;
			double startCubeLat = translatedParLoc.getLatitude() + halfCubeLatLon;
			int numLatLon = (int)(sliceLenghtDegrees/cubeLatLonSpacing);	// 3 degrees in each direction

			// Data for squares on the EW trending vertical face
			double lat = startCubeLat;
			for(int i=0;i<numLatLon;i++) {
				double lon = startCubeLon + i*cubeLatLonSpacing;
				for(int d=0; d<numCubeDepths; d++) {
					double depth = getCubeDepth(d);
					Location cubeLoc = new Location(lat,lon,depth);
					double val = sampler.getY(this.getCubeIndexForLocation(cubeLoc));
					Color c = cpt.getColor((float)Math.log10(val));
					fileWriterGMT.write("> -G"+c.getRed()+"/"+c.getGreen()+"/"+c.getBlue()+"\n");
					fileWriterSCECVDO.write("> "+val+"\n");
					String polygonString = parLat + "\t" + (float)(lon-halfCubeLatLon) + "\t" + -(float)(depth-halfCubeDepth) +"\n";
					polygonString += parLat + "\t" + (float)(lon+halfCubeLatLon) + "\t" + -(float)(depth-halfCubeDepth) +"\n";
					polygonString += parLat + "\t" + (float)(lon+halfCubeLatLon) + "\t" + -(float)(depth+halfCubeDepth) +"\n";
					polygonString += parLat + "\t" + (float)(lon-halfCubeLatLon) + "\t" + -(float)(depth+halfCubeDepth) +"\n";
					fileWriterGMT.write(polygonString);
					fileWriterSCECVDO.write(polygonString);
				}
			}
			
			// Data for squares on the NS trending vertical face
			double lon = startCubeLon;
			for(int i=0;i<numLatLon;i++) {
				lat = startCubeLat + i*cubeLatLonSpacing;
				for(int d=0; d<numCubeDepths; d++) {
					double depth = getCubeDepth(d);
					Location cubeLoc = new Location(lat,lon,depth);
					double val = sampler.getY(this.getCubeIndexForLocation(cubeLoc));
					Color c = cpt.getColor((float)Math.log10(val));
					fileWriterGMT.write("> -G"+c.getRed()+"/"+c.getGreen()+"/"+c.getBlue()+"\n");
					fileWriterSCECVDO.write("> "+val+"\n");
					String polygonString = (float)(lat-halfCubeLatLon) + "\t" + parLon + "\t" + -(float)(depth-halfCubeDepth) +"\n";
					polygonString += (float)(lat+halfCubeLatLon) + "\t" + parLon + "\t" + -(float)(depth-halfCubeDepth) +"\n";
					polygonString += (float)(lat+halfCubeLatLon) + "\t" + parLon + "\t" + -(float)(depth+halfCubeDepth) +"\n";
					polygonString += (float)(lat-halfCubeLatLon) + "\t" + parLon + "\t" + -(float)(depth+halfCubeDepth) +"\n";
					fileWriterGMT.write(polygonString);
					fileWriterSCECVDO.write(polygonString);
				}
			}
			
			// Data for top surface at zero depth
			double depth = 0.0;
			for(int i=0;i<numLatLon;i++) {
				lat = startCubeLat + i*cubeLatLonSpacing;
				for(int j=0; j<numLatLon; j++) {
					lon = startCubeLon + j*cubeLatLonSpacing;
					Location cubeLoc = new Location(lat,lon,depth);
					int cubeIndex = this.getCubeIndexForLocation(cubeLoc);
					if(cubeIndex == -1) 
						continue;
					// round the back-side edge
					double distDegree = Math.sqrt((cubeLoc.getLatitude()-startCubeLat)*(cubeLoc.getLatitude()-startCubeLat) + (cubeLoc.getLongitude()-startCubeLon)*(cubeLoc.getLongitude()-startCubeLon));
					if(distDegree>sliceLenghtDegrees)
						continue;
					double val = sampler.getY(this.getCubeIndexForLocation(cubeLoc));
					Color c = cpt.getColor((float)Math.log10(val));
					fileWriterGMT.write("> -G"+c.getRed()+"/"+c.getGreen()+"/"+c.getBlue()+"\n");
					fileWriterSCECVDO.write("> "+val+"\n");
					String polygonString = (float)(lat-halfCubeLatLon) + "\t" + (float)(lon-halfCubeLatLon) + "\t" + depth +"\n";
					polygonString += (float)(lat+halfCubeLatLon) + "\t" + (float)(lon-halfCubeLatLon) + "\t" + depth +"\n";
					polygonString += (float)(lat+halfCubeLatLon) + "\t" + (float)(lon+halfCubeLatLon) + "\t" + depth +"\n";
					polygonString += (float)(lat-halfCubeLatLon) + "\t" + (float)(lon+halfCubeLatLon) + "\t" + depth +"\n";
					fileWriterGMT.write(polygonString);
					fileWriterSCECVDO.write(polygonString);
				}
			}	
			fileWriterGMT.close();
			fileWriterSCECVDO.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		
	}
	
	
	public void writeGMT_PieSliceRatesData(Location parLoc, String fileNamePrefix) {
		
		double sliceLenghtDegrees = 2;
		
		try {
			FileWriter fileWriterGMT = new FileWriter(new File(GMT_CA_Maps.GMT_DIR, fileNamePrefix+"GMT.txt"));
			FileWriter fileWriterSCECVDO = new FileWriter(new File(GMT_CA_Maps.GMT_DIR, fileNamePrefix+"SCECVDO.txt"));
			CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance();
			
			int parLocIndex = getParLocIndexForLocation(parLoc);
			Location translatedParLoc = getParLocationForIndex(parLocIndex);
			float parLat = (float) translatedParLoc.getLatitude();
			float parLon = (float) translatedParLoc.getLongitude();
			IntegerPDF_FunctionSampler sampler = getCubeSamplerWithERF_RatesOnly();
			// normalize
			sampler.scale(1.0/sampler.getSumOfY_vals());
			
//			// compute min and max
//			double minVal=Double.POSITIVE_INFINITY, maxVal=Double.NEGATIVE_INFINITY;
//			for(int i=0; i<sampler.size();i++) {
//				Location loc = this.getCubeLocationForIndex(i);
//				double latDiff = Math.abs(loc.getLatitude()-parLoc.getLatitude());
//				double lonDiff = Math.abs(loc.getLongitude()-parLoc.getLongitude());
//				double distDegrees = Math.sqrt(latDiff*latDiff+lonDiff*lonDiff);
//				if(distDegrees>sliceLenghtDegrees)
//					continue;
//				double val = Math.log10(sampler.getY(i));
//				if(val<-15)
//					continue;
//				if(minVal>val)
//					minVal=val;
//				if(maxVal<val)
//					maxVal=val;
//			}
//			
//			System.out.println("minVal="+minVal+"\tmaxVal="+maxVal);
//			System.out.println("Math.round(minVal)="+Math.round(minVal)+"\tMath.round(maxVal)="+Math.round(maxVal));
//	        cpt = cpt.rescale(Math.round(minVal), Math.round(maxVal));
	        
	        // hard coded:
	        cpt = cpt.rescale(-7, -5);
	        cpt = cpt.rescale(-9, -1);
	        
	        cpt.writeCPTFile(new File(GMT_CA_Maps.GMT_DIR, fileNamePrefix+"_CPT.txt"));
			
			double halfCubeLatLon = cubeLatLonSpacing/2.0;
			double halfCubeDepth = depthDiscr/2.0;
			double startCubeLon = translatedParLoc.getLongitude() + halfCubeLatLon;
			double startCubeLat = translatedParLoc.getLatitude() + halfCubeLatLon;
			int numLatLon = (int)(sliceLenghtDegrees/cubeLatLonSpacing);	// 3 degrees in each direction

			// Data for squares on the EW trending vertical face
			double lat = startCubeLat;
			for(int i=0;i<numLatLon;i++) {
				double lon = startCubeLon + i*cubeLatLonSpacing;
				for(int d=0; d<numCubeDepths; d++) {
					double depth = getCubeDepth(d);
					Location cubeLoc = new Location(lat,lon,depth);
					double val = sampler.getY(this.getCubeIndexForLocation(cubeLoc));
					Color c = cpt.getColor((float)Math.log10(val));
					fileWriterGMT.write("> -G"+c.getRed()+"/"+c.getGreen()+"/"+c.getBlue()+"\n");
					fileWriterSCECVDO.write("> "+val+"\n");
					String polygonString = parLat + "\t" + (float)(lon-halfCubeLatLon) + "\t" + -(float)(depth-halfCubeDepth) +"\n";
					polygonString += parLat + "\t" + (float)(lon+halfCubeLatLon) + "\t" + -(float)(depth-halfCubeDepth) +"\n";
					polygonString += parLat + "\t" + (float)(lon+halfCubeLatLon) + "\t" + -(float)(depth+halfCubeDepth) +"\n";
					polygonString += parLat + "\t" + (float)(lon-halfCubeLatLon) + "\t" + -(float)(depth+halfCubeDepth) +"\n";
					fileWriterGMT.write(polygonString);
					fileWriterSCECVDO.write(polygonString);
				}
			}
			
			// Data for squares on the NS trending vertical face
			double lon = startCubeLon;
			for(int i=0;i<numLatLon;i++) {
				lat = startCubeLat + i*cubeLatLonSpacing;
				for(int d=0; d<numCubeDepths; d++) {
					double depth = getCubeDepth(d);
					Location cubeLoc = new Location(lat,lon,depth);
					double val = sampler.getY(this.getCubeIndexForLocation(cubeLoc));
					Color c = cpt.getColor((float)Math.log10(val));
					fileWriterGMT.write("> -G"+c.getRed()+"/"+c.getGreen()+"/"+c.getBlue()+"\n");
					fileWriterSCECVDO.write("> "+val+"\n");
					String polygonString = (float)(lat-halfCubeLatLon) + "\t" + parLon + "\t" + -(float)(depth-halfCubeDepth) +"\n";
					polygonString += (float)(lat+halfCubeLatLon) + "\t" + parLon + "\t" + -(float)(depth-halfCubeDepth) +"\n";
					polygonString += (float)(lat+halfCubeLatLon) + "\t" + parLon + "\t" + -(float)(depth+halfCubeDepth) +"\n";
					polygonString += (float)(lat-halfCubeLatLon) + "\t" + parLon + "\t" + -(float)(depth+halfCubeDepth) +"\n";
					fileWriterGMT.write(polygonString);
					fileWriterSCECVDO.write(polygonString);
				}
			}
			
			// Data for top surface at zero depth
			double depth = 0.0;
			for(int i=0;i<numLatLon;i++) {
				lat = startCubeLat + i*cubeLatLonSpacing;
				for(int j=0; j<numLatLon; j++) {
					lon = startCubeLon + j*cubeLatLonSpacing;
					Location cubeLoc = new Location(lat,lon,depth);
					int cubeIndex = this.getCubeIndexForLocation(cubeLoc);
					if(cubeIndex == -1) 
						continue;
					// round the back-side edge
					double distDegree = Math.sqrt((cubeLoc.getLatitude()-startCubeLat)*(cubeLoc.getLatitude()-startCubeLat) + (cubeLoc.getLongitude()-startCubeLon)*(cubeLoc.getLongitude()-startCubeLon));
					if(distDegree>sliceLenghtDegrees)
						continue;
					double val = sampler.getY(this.getCubeIndexForLocation(cubeLoc));
					Color c = cpt.getColor((float)Math.log10(val));
					fileWriterGMT.write("> -G"+c.getRed()+"/"+c.getGreen()+"/"+c.getBlue()+"\n");
					fileWriterSCECVDO.write("> "+val+"\n");
					String polygonString = (float)(lat-halfCubeLatLon) + "\t" + (float)(lon-halfCubeLatLon) + "\t" + depth +"\n";
					polygonString += (float)(lat+halfCubeLatLon) + "\t" + (float)(lon-halfCubeLatLon) + "\t" + depth +"\n";
					polygonString += (float)(lat+halfCubeLatLon) + "\t" + (float)(lon+halfCubeLatLon) + "\t" + depth +"\n";
					polygonString += (float)(lat-halfCubeLatLon) + "\t" + (float)(lon+halfCubeLatLon) + "\t" + depth +"\n";
					fileWriterGMT.write(polygonString);
					fileWriterSCECVDO.write(polygonString);
				}
			}	
			fileWriterGMT.close();
			fileWriterSCECVDO.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	
	
	
	public void writeRatesCrossSectionData(Location startLoc, double lengthDegrees,String fileNamePrefix, double magThresh) {
				
		try {
			FileWriter fileWriterGMT = new FileWriter(new File(GMT_CA_Maps.GMT_DIR, fileNamePrefix+"GMT.txt"));
			FileWriter fileWriterSCECVDO = new FileWriter(new File(GMT_CA_Maps.GMT_DIR, fileNamePrefix+"SCECVDO.txt"));
			CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance();
			
			// get closest cube-center
			Location startCubeLoc = getCubeLocationForIndex(getCubeIndexForLocation(startLoc));
	        
	        // hard coded:
	        cpt = cpt.rescale(-8.5, -4.5);
	        cpt.setBelowMinColor(Color.WHITE);
	        
	        cpt.writeCPTFile(new File(GMT_CA_Maps.GMT_DIR, fileNamePrefix+"_CPT.txt"));
			
			double halfCubeLatLon = cubeLatLonSpacing/2.0;
			double halfCubeDepth = depthDiscr/2.0;
			double startCubeLon = startCubeLoc.getLongitude();
			double startCubeLat = startCubeLoc.getLatitude();
			int numLatLon = (int)(lengthDegrees/cubeLatLonSpacing);
			
//int testCubeIndex = this.getCubeIndexForLocation(new Location(34.76,-118.48,0.0));
//int cubeRegIndex = getCubeRegAndDepIndicesForIndex(testCubeIndex)[0];
//System.out.println("HERE isCubeInsideFaultPolygon[cubeRegIndex]="+isCubeInsideFaultPolygon[cubeRegIndex]);
			
			double minVal = Double.MAX_VALUE, maxVal = -Double.MAX_VALUE;

			// Data for squares on the EW trending vertical face
			double lat = startCubeLat;
			double lon = startCubeLon;
			for(int i=0;i<numLatLon;i++) {
				for(int d=0; d<numCubeDepths; d++) {
					double depth = getCubeDepth(d);
					Location cubeLoc = new Location(lat,lon,depth);
					int cubeIndex = getCubeIndexForLocation(cubeLoc);
					SummedMagFreqDist mfd = getCubeMFD(cubeIndex);
					double val = mfd.getCumRate(magThresh);
					Color c = cpt.getColor((float)Math.log10(val));
					if(minVal>Math.log10(val))
						minVal=Math.log10(val);
					if(maxVal<Math.log10(val))
						maxVal=Math.log10(val);					
					fileWriterGMT.write("> -W125/125/125 -G"+c.getRed()+"/"+c.getGreen()+"/"+c.getBlue()+"\n");
					fileWriterSCECVDO.write("> "+val+"\n");
					String polygonString = (float)(lat-halfCubeLatLon) + "\t" + (float)(lon-halfCubeLatLon) + "\t" + -(float)(depth-halfCubeDepth) +"\n";
					polygonString += (float)(lat-halfCubeLatLon) + "\t" + (float)(lon-halfCubeLatLon) + "\t" + -(float)(depth+halfCubeDepth) +"\n";
					polygonString += (float)(lat+halfCubeLatLon) + "\t" + (float)(lon+halfCubeLatLon) + "\t" + -(float)(depth+halfCubeDepth) +"\n";
					polygonString += (float)(lat+halfCubeLatLon) + "\t" + (float)(lon+halfCubeLatLon) + "\t" + -(float)(depth-halfCubeDepth) +"\n";
					fileWriterGMT.write(polygonString);
					fileWriterSCECVDO.write(polygonString);
				}
				lat+=cubeLatLonSpacing;
				lon+=cubeLatLonSpacing;
			}
			
			// Data for the surface
			double depth = 0;
			int numOtherWay = 30;
			double newStartLat = startCubeLat;
			double newStartLon = startCubeLon+cubeLatLonSpacing;
			int numLonDone=0;
			for(int j=0; j<numOtherWay; j++) {
//				newStartLon = -j*cubeLatLonSpacing+startCubeLon;
//				if( (j & 1) != 0) {
//					if(j !=0)
//						newStartLat+=cubeLatLonSpacing;
//				}
//				else
//					newStartLon -= cubeLatLonSpacing;
				if(numLonDone!=2){
					newStartLon -= cubeLatLonSpacing;
					numLonDone+=1;
				}
				else {
					newStartLat+=cubeLatLonSpacing;
					numLonDone=0;
				}
				lat = newStartLat;
				lon = newStartLon;
				for(int i=0;i<numLatLon;i++) {
					Location cubeLoc = new Location(lat,lon,depth+halfCubeDepth);
					int cubeIndex = getCubeIndexForLocation(cubeLoc);
					SummedMagFreqDist mfd = getCubeMFD(cubeIndex);
					double val = mfd.getCumRate(magThresh);
					if(minVal>Math.log10(val))
						minVal=Math.log10(val);
					if(maxVal<Math.log10(val))
						maxVal=Math.log10(val);					
					Color c = cpt.getColor((float)Math.log10(val));
//if(cubeIndex==testCubeIndex) System.out.println("HERE index, val: "+testCubeIndex+"\t"+val+"\n"+mfd);
//if(cubeIndex==testCubeIndex) System.out.println("HERE RGB: "+"> -G"+c.getRed()+"/"+c.getGreen()+"/"+c.getBlue()+"\n");
					fileWriterGMT.write("> -W125/125/125 -G"+c.getRed()+"/"+c.getGreen()+"/"+c.getBlue()+"\n");
					fileWriterSCECVDO.write("> "+val+"\n");
					String polygonString = (float)(lat-halfCubeLatLon) + "\t" + (float)(lon-halfCubeLatLon) + "\t" + depth +"\n";
					polygonString += (float)(lat+halfCubeLatLon) + "\t" + (float)(lon-halfCubeLatLon) + "\t" + depth +"\n";
					polygonString += (float)(lat+halfCubeLatLon) + "\t" + (float)(lon+halfCubeLatLon) + "\t" + depth +"\n";
					if(j != 0)
						polygonString += (float)(lat-halfCubeLatLon) + "\t" + (float)(lon+halfCubeLatLon) + "\t" + depth +"\n";
					fileWriterGMT.write(polygonString);
					fileWriterSCECVDO.write(polygonString);
					lat += cubeLatLonSpacing;
					lon += cubeLatLonSpacing;
				}
			}

			
			fileWriterGMT.close();
			fileWriterSCECVDO.close();
			
			System.out.println("Value Range:\n\tminVal="+minVal+"\n\tmaxVal="+maxVal);
			
			// write out Mojave subsection polygons
			FileWriter fileWriterPolygons = new FileWriter(new File(GMT_CA_Maps.GMT_DIR, "PolygonData.txt"));
			for(int i=1841; i>1835;i--) {
				String polygonString = "> -W\n";
				for(Location loc : faultPolyMgr.getPoly(i).getBorder()) {
					polygonString += (float)loc.getLatitude() + "\t" + (float)loc.getLongitude() + "\t" + loc.getDepth() +"\n";
				}
				fileWriterPolygons.write(polygonString);

			}
			fileWriterPolygons.close();
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		
		
	}
	
	
	/**
	 * TODO THIS IS OUT OF DATE: START FROM SCRATCH USING writeRatesCrossSectionData
	 * @param startLoc
	 * @param lengthDegrees
	 * @param fileNamePrefix
	 */
	public void writeBulgeCrossSectionData(Location startLoc, double lengthDegrees,String fileNamePrefix) {
		
		try {
			FileWriter fileWriterGMT = new FileWriter(new File(GMT_CA_Maps.GMT_DIR, fileNamePrefix+"GMT.txt"));
			FileWriter fileWriterSCECVDO = new FileWriter(new File(GMT_CA_Maps.GMT_DIR, fileNamePrefix+"SCECVDO.txt"));
			CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance();
			
			// get closest cube-center
			Location startCubeLoc = getCubeLocationForIndex(getCubeIndexForLocation(startLoc));
			
			if(mfdForSrcArray == null) {
				computeMFD_ForSrcArrays(2.05, 8.95, 70);
			}

	        
	        // hard coded:
	        cpt = cpt.rescale(-3, 3);
	        
	        cpt.writeCPTFile(new File(GMT_CA_Maps.GMT_DIR, fileNamePrefix+"_CPT.txt"));
			
			double halfCubeLatLon = cubeLatLonSpacing/2.0;
			double halfCubeDepth = depthDiscr/2.0;
			double startCubeLon = startCubeLoc.getLongitude();
			double startCubeLat = startCubeLoc.getLatitude();
			int numLatLon = (int)(lengthDegrees/cubeLatLonSpacing);

			// Data for squares on the EW trending vertical face
			double lat = startCubeLat;
			double lon = startCubeLon;
			for(int i=0;i<numLatLon;i++) {
				for(int d=0; d<numCubeDepths; d++) {
					double depth = getCubeDepth(d);
					Location cubeLoc = new Location(lat,lon,depth);
					int cubeIndex = getCubeIndexForLocation(cubeLoc);
					
					SummedMagFreqDist mfdSupra = getCubeMFD_SupraSeisOnly(cubeIndex);
					SummedMagFreqDist mfdGridded = getCubeMFD_GriddedSeisOnly(cubeIndex);
					double bulge = 1.0;
					if(mfdSupra != null &&  mfdGridded != null) {
						bulge = 1.0/ETAS_Utils.getScalingFactorToImposeGR(mfdSupra, mfdGridded, false);
						if(Double.isInfinite(bulge))
							bulge = 1e3;				
					}
					Color c = cpt.getColor((float)Math.log10(bulge));
					fileWriterGMT.write("> -G"+c.getRed()+"/"+c.getGreen()+"/"+c.getBlue()+"\n");
					fileWriterSCECVDO.write("> "+bulge+"\n");
					String polygonString = (float)(lat-halfCubeLatLon) + "\t" + (float)(lon-halfCubeLatLon) + "\t" + -(float)(depth-halfCubeDepth) +"\n";
					polygonString += (float)(lat-halfCubeLatLon) + "\t" + (float)(lon-halfCubeLatLon) + "\t" + -(float)(depth+halfCubeDepth) +"\n";
					polygonString += (float)(lat+halfCubeLatLon) + "\t" + (float)(lon+halfCubeLatLon) + "\t" + -(float)(depth+halfCubeDepth) +"\n";
					polygonString += (float)(lat+halfCubeLatLon) + "\t" + (float)(lon+halfCubeLatLon) + "\t" + -(float)(depth-halfCubeDepth) +"\n";
					fileWriterGMT.write(polygonString);
					fileWriterSCECVDO.write(polygonString);
				}
				lat+=cubeLatLonSpacing;
				lon+=cubeLatLonSpacing;
			}
			fileWriterGMT.close();
			fileWriterSCECVDO.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}


	
	/**
	 * This plots the implied MFD bulge for sections (log10 of one over the GR correction) using GMT
	 * @param resultsDir
	 * @param nameSuffix
	 * @param display
	 * @throws GMT_MapException
	 * @throws RuntimeException
	 * @throws IOException
	 */
	public void plotImpliedBulgeForSubSections(File resultsDir, String nameSuffix, boolean display) 
			throws GMT_MapException, RuntimeException, IOException {

		List<FaultSectionPrefData> faults = fssERF.getSolution().getRupSet().getFaultSectionDataList();
		double[] values = new double[faults.size()];
		
		// Make sure long-term MFDs are created
		makeLongTermSectMFDs();

System.out.println("GR Correction Factors:\nsectID\t1.0/GRcorr\tsectName");

		for(int sectIndex=0;sectIndex<values.length;sectIndex++) {
			double val;
			if(longTermSupraSeisMFD_OnSectArray[sectIndex] != null) {
				
				// TODO only need this temporarily?
				if (Double.isNaN(longTermSupraSeisMFD_OnSectArray[sectIndex].getMaxMagWithNonZeroRate())){
					System.out.println("NaN HERE: "+fssERF.getSolution().getRupSet().getFaultSectionData(sectIndex).getName());
					throw new RuntimeException("Problem");
				}
				
				val = 1.0/ETAS_Utils.getScalingFactorToImposeGR(longTermSupraSeisMFD_OnSectArray[sectIndex], longTermSubSeisMFD_OnSectList.get(sectIndex), false);
			}
			else {	// no supra-seismogenic ruptures
				throw new RuntimeException("Problem");
			}
			
			values[sectIndex] = Math.log10(val);


System.out.println(sectIndex+"\t"+(float)val+"\t"+fssERF.getSolution().getRupSet().getFaultSectionData(sectIndex).getName());
		}

		String name = "ImpliedBulgeForSubSections_"+nameSuffix;
		String title = "Log10(Bulge From 1st Gen Aft)";
		CPT cpt= FaultBasedMapGen.getLogRatioCPT().rescale(-2, 2);
		
		if(!resultsDir.exists())
			resultsDir.mkdir();
		
		FaultBasedMapGen.makeFaultPlot(cpt, FaultBasedMapGen.getTraces(faults), values, origGriddedRegion, resultsDir, name, display, false, title);
		
	}

				
			
	/**
	 * This plots the relative probability that each subsection will participate
	 * given a primary aftershocks of the supplied mainshock.  This also returns
	 * a String with a list of the top numToList fault-based sources and top sections.
	 * 
	 * TODO move this to ETAS_SimAnalysisTools
	 */
	public String plotSubSectParticipationProbGivenRuptureAndReturnInfo(ETAS_EqkRupture mainshock, double[] relSrcProbs, File resultsDir, 
			int numToList, String nameSuffix) {
		String info = "";
		if(erf instanceof FaultSystemSolutionERF) {
			double[] sectProbArray = new double[rupSet.getNumSections()];
			for(int srcIndex=0; srcIndex<numFltSystSources; srcIndex++) {
				int fltSysIndex = fssERF.getFltSysRupIndexForSource(srcIndex);
				for(Integer sectIndex:rupSet.getSectionsIndicesForRup(fltSysIndex)) {
					sectProbArray[sectIndex] += relSrcProbs[srcIndex];
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
				info += "\nProbability of sampling the rupture again:\n";
				int srcIndex = ((FaultSystemSolutionERF)erf).getSrcIndexForFltSysRup(mainshock.getFSSIndex());
				info += "\n\t"+relSrcProbs[srcIndex]+"\t"+srcIndex+"\t"+erf.getSource(srcIndex).getName()+"\n";
			}
			
			
			// below creates the string of top values; first sources, then sections			
			
			// list top fault-based ruptures
			double[] fltSrcProbs = new double[this.numFltSystSources];
			for(int i=0;i<fltSrcProbs.length;i++)
				fltSrcProbs[i]=relSrcProbs[i];
			int[] topValueIndices = ETAS_SimAnalysisTools.getIndicesForHighestValuesInArray(fltSrcProbs, numToList);
			info += "\nScenario is most likely to trigger the following fault-based sources (prob, srcIndex, mag, name):\n\n";
			for(int srcIndex : topValueIndices) {
				info += "\t"+fltSrcProbs[srcIndex]+"\t"+srcIndex+"\t"+erf.getSource(srcIndex).getRupture(0).getMag()+"\t"+erf.getSource(srcIndex).getName()+"\n";
			}
			
			// list top fault section participations
			topValueIndices = ETAS_SimAnalysisTools.getIndicesForHighestValuesInArray(sectProbArray, numToList);
			info += "\nThe following sections are most likely to participate in a triggered event:\n\n";
			List<FaultSectionPrefData> fltDataList = fssERF.getSolution().getRupSet().getFaultSectionDataList();
			for(int sectIndex :topValueIndices) {
				info += "\t"+sectProbArray[sectIndex]+"\t"+sectIndex+"\t"+fltDataList.get(sectIndex).getName()+"\n";
			}

		}
		else {
			throw new RuntimeException("erf must be instance of FaultSystemSolutionERF");
		}
		return info;
	}
	
	
	
	
	/**
	 * This method will populate the given rupToFillIn with attributes of a randomly chosen
	 * primary aftershock for the given main shock.  
	 * @return boolean tells whether it succeeded in setting the rupture
	 * @param rupToFillIn
	 */
	public boolean setRandomPrimaryEvent(ETAS_EqkRupture rupToFillIn) {
		
		ETAS_EqkRupture parRup = rupToFillIn.getParentRup();
		
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
			
//			// Old, Old way: collect those inside the cube and choose one randomly
//			LocationList locsOnRupSurf = erf_rup.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();
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
			
			
			
//			// Old way: choose the closest point on surface as the hypocenter
//			LocationList locsOnRupSurf = erf_rup.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();
//			Location hypoLoc=null;
//			Location cubeLoc= getCubeLocationForIndex(aftShCubeIndex);
//			double minDist = Double.MAX_VALUE;
//			for(Location loc:locsOnRupSurf) {
//				double dist = LocationUtils.linearDistanceFast(cubeLoc, loc);
//				if(dist<minDist) {
//					hypoLoc = loc;
//					minDist = dist;
//				}	
//			}
//			rupToFillIn.setHypocenterLocation(hypoLoc);
			
			
			// Latest way:
			// this makes it the distence from a vertex (par loc) to cube center, but we could add some randomness like below for
			// gridded seis if we want results to look better (but bad-looking results will remind us about he discretization issues
			// at large mags)
			rupToFillIn.setHypocenterLocation(getCubeLocationForIndex(aftShCubeIndex));


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
				int gridSrcIndex = randSrcIndex-numFltSystSources;
				Location gridSrcLoc = fssERF.getSolution().getGridSourceProvider().getGriddedRegion().getLocation(gridSrcIndex);
				int testIndex = fssERF.getSolution().getGridSourceProvider().getGriddedRegion().indexForLocation(hypLoc);
				if(testIndex != gridSrcIndex) {
					// check whether hypLoc is now out of region, and return false if so
					if(testIndex == -1)
						return false;
					IncrementalMagFreqDist mfd = fssERF.getSolution().getGridSourceProvider().getNodeMFD(testIndex);
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
						int testIndex2 = fssERF.getSolution().getGridSourceProvider().getGriddedRegion().indexForLocation(hypLoc);
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
	
	/**
	 * This adds + or - 0.005 degrees to both the lat and lon of the given location (sign is random,
	 * and a separate random value is applied to lat vs lon). This is used when a fault system rupture 
	 * is sampled in order to avoid numerical precision problems when it comes to samplind ajacent sections
	 * along the fault.  See getAveSamplerForRupture(*) for more info.
	 * @param loc
	 * @return
	 */
	public Location getRandomFuzzyLocation(Location loc) {
		double sign1=1, sign2=1;
		if(etas_utils.getRandomDouble() < 0.5)
			sign1=-1;
		if(etas_utils.getRandomDouble() < 0.5)
			sign2=-1;
		return new Location(loc.getLatitude()+sign1*0.005, loc.getLongitude()+sign2*0.005, loc.getDepth());
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
		
		if (disable_cache || cache_size_bytes <= 0l) {
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
		computeTotSectRateInCubesArray();
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
	 * TODO use this where the same calculation is used elsewhere (search for "numPtSrcSubPts*numPtSrcSubPts*numCubeDepths")
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
	public int getRandomSourceIndexInCube_OLD(int cubeIndex) {
		
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
	 * This returns -1 if ??????
	 * 
	 * getRelativeTriggerProbOfSourcesInCube(int cubeIndex) has a lot of redundant code, but this might be faster?
	 * 
	 * TODO this other method implies this will crash for certain cubes that have no gridded cell mapping
	 * @param srcIndex
	 * @return
	 */
	public int getRandomSourceIndexInCube(int cubeIndex) {
		
		// get gridded region index for the cube
		int griddeSeisRegionIndex = origGriddedRegion.indexForLocation(getCubeLocationForIndex(cubeIndex));
		if(griddeSeisRegionIndex == -1)	// TODO THROW EXCEPTION FOR NOW UNTIL I UNDERSTAND CONDITIONS BETTER
			throw new RuntimeException("No gridded source index for cube at: "+getCubeLocationForIndex(cubeIndex).toString());
		
		// get gridded source index
		int gridSrcIndex = numFltSystSources + griddeSeisRegionIndex;
		
		if(totalSectRateInCubeArray[cubeIndex] < 1e-15)
			return gridSrcIndex;
		else {
			double gridSrcRate = sourceRates[gridSrcIndex]/(numPtSrcSubPts*numPtSrcSubPts*numCubeDepths);	// divide rate among all the cubes in grid cell
			double fractTest = gridSrcRate/(gridSrcRate+totalSectRateInCubeArray[cubeIndex]);
			if(etas_utils.getRandomDouble() < fractTest) {
				return gridSrcIndex;
			}
			else {
				// randomly sample a section first
				int[] sectInCubeArray = sectInCubeList.get(cubeIndex);
				float[] fracts = fractionSectInCubeList.get(cubeIndex);
				IntegerPDF_FunctionSampler sectSampler = new IntegerPDF_FunctionSampler(sectInCubeArray.length);  // plus 1 for gridded source
				for(int s=0; s<sectInCubeArray.length;s++) {
					sectSampler.set(s,totSectNuclRateArray[sectInCubeArray[s]]*(double)fracts[s]);		
				}
				int randSectIndex = sectSampler.getRandomInt(etas_utils.getRandomDouble());
				int sectIndex = sectInCubeArray[randSectIndex];
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

	
	
	private void testGR_CorrFactors(int sectIndex) {
		
		if(erf instanceof FaultSystemSolutionERF && applyGR_Corr) {
			
			// Make sure long-term MFDs are created
			makeLongTermSectMFDs();

			System.out.println("Test GR Correction Factor");
			double val = ETAS_Utils.getScalingFactorToImposeGR(longTermSupraSeisMFD_OnSectArray[sectIndex], longTermSubSeisMFD_OnSectList.get(sectIndex), true);
		}

	}
	
	
	/**
	 * This creates the long-term supra- and sub-seismo MFDs for each section (if they don't already exist) held in
	 * longTermSupraSeisMFD_OnSectArray and longTermSubSeisMFD_OnSectList.
	 */
	private void makeLongTermSectMFDs() {
		
		if(longTermSupraSeisMFD_OnSectArray == null || longTermSubSeisMFD_OnSectList == null) {
			
			// here are the sub-seis MFDs
			longTermSubSeisMFD_OnSectList = fssERF.getSolution().getSubSeismoOnFaultMFD_List();
			
			// can't get supra-seism MFDs from fault-system-solution becuase some rups are zeroed out and there may 
			// be aleatory mag-area variability added in the ERF, so compute from ERF.
			
			// temporarily change the erf to Poisson to get long-term section MFDs (and test the parameter values are the same after)
			ProbabilityModelOptions probModel = (ProbabilityModelOptions)erf.getParameter(ProbabilityModelParam.NAME).getValue();
			ArrayList paramValueList = new ArrayList();
			for(Parameter param : erf.getAdjustableParameterList()) {
				paramValueList.add(param.getValue());
			}
			TimeSpan tsp = erf.getTimeSpan();
			double duration = tsp.getDuration();
			String startTimeString = tsp.getStartTimeMonth()+"/"+tsp.getStartTimeDay()+"/"+tsp.getStartTimeYear()+"; hr="+tsp.getStartTimeHour()+"; min="+tsp.getStartTimeMinute()+"; sec="+tsp.getStartTimeSecond();
			paramValueList.add(startTimeString);
			paramValueList.add(duration);
			int numParams = paramValueList.size();

			// now set ERF to poisson:
			erf.getParameter(ProbabilityModelParam.NAME).setValue(ProbabilityModelOptions.POISSON);
			erf.updateForecast();
			// get what we need
			longTermSupraSeisMFD_OnSectArray = FaultSysSolutionERF_Calc.calcNucleationMFDForAllSects(fssERF, 2.55, 8.95, 65);
			
			// set it back and test param values
			erf.getParameter(ProbabilityModelParam.NAME).setValue(probModel);
			erf.updateForecast();
			
			int testNum = erf.getAdjustableParameterList().size()+2;
			if(numParams != testNum) {
				throw new RuntimeException("PROBLEM: num parameters changed:\t"+numParams+"\t"+testNum);
			}
			int i=0;
			for(Parameter param : erf.getAdjustableParameterList()) {
				if(param.getValue() != paramValueList.get(i))
					throw new RuntimeException("PROBLEM: "+param.getValue()+"\t"+paramValueList.get(i));
				i+=1;
			}
			TimeSpan tsp2 = erf.getTimeSpan();
			double duration2 = tsp2.getDuration();
			String startTimeString2 = tsp2.getStartTimeMonth()+"/"+tsp2.getStartTimeDay()+"/"+tsp2.getStartTimeYear()+"; hr="+tsp2.getStartTimeHour()+"; min="+tsp2.getStartTimeMinute()+"; sec="+tsp2.getStartTimeSecond();
			if(!startTimeString2.equals(startTimeString))
				throw new RuntimeException("PROBLEM: "+startTimeString2+"\t"+startTimeString2);
			if(duration2 != duration)
				throw new RuntimeException("PROBLEM Duration: "+duration2+"\t"+duration);
		}

	}
	
	
	
	
	/**
	 * This computes a scale factor for each fault section, whereby multiplying the associate supra-seismogenic MFD
	 * by this factor will produced the same number of expected primary aftershocks as for a perfect GR
	 * (extrapolating the sub-seismogenic MFD to the maximum, non-zero-rate  magnitude of the supra-seismogenic MFD).
	 * 
	 * The array is all 1.0 values if the erf is not a FaultSystemSolutionERF or if applyGR_Corr=false
	 * 
	 * @return
	 */
	private void computeGR_CorrFactorsForSections() {
		
		grCorrFactorForSectArray = new double[rupSet.getNumSections()];
		
		if(erf instanceof FaultSystemSolutionERF && applyGR_Corr) {
			
			// Make sure long-term MFDs are created
			makeLongTermSectMFDs();

//System.out.println("GR Correction Factors:");

			double minCorr=Double.MAX_VALUE;
			int minCorrIndex = -1;
			for(int sectIndex=0;sectIndex<grCorrFactorForSectArray.length;sectIndex++) {
				if(longTermSupraSeisMFD_OnSectArray[sectIndex] != null) {
					
					// TODO only need this temporarily?
					if (Double.isNaN(longTermSupraSeisMFD_OnSectArray[sectIndex].getMaxMagWithNonZeroRate())){
						System.out.println("NaN HERE: "+fssERF.getSolution().getRupSet().getFaultSectionData(sectIndex).getName());
						throw new RuntimeException("Problem");
					}
					
					double val = ETAS_Utils.getScalingFactorToImposeGR(longTermSupraSeisMFD_OnSectArray[sectIndex], longTermSubSeisMFD_OnSectList.get(sectIndex), false);
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

// System.out.println(sectIndex+"\t"+(float)grCorrFactorForSectArray[sectIndex]+"\t"+fssERF.getSolution().getRupSet().getFaultSectionData(sectIndex).getName());
			}
			if(D) System.out.println("min GR Corr ("+minCorr+") at sect index: "+minCorrIndex+"\t"+fssERF.getSolution().getRupSet().getFaultSectionData(minCorrIndex).getName());
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
		int index = iDep*numCubesPerDepth+iReg;
		if(index<numCubes)
			return index;
		else
			return -1;
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
		
		double[] sectRatesTest = new double[this.rupSet.getNumSections()];
		for(int i=0;i<numCubes;i++) {
			int[] sections = sectInCubeList.get(i);
			float[] fracts = fractionSectInCubeList.get(i);
			for(int j=0; j<sections.length;j++) {
				sectRatesTest[sections[j]] += totSectNuclRateArray[sections[j]]*(double)fracts[j];
			}
		}
		// compute the rate of unassigned sections
		double rateUnassigned = 0;
		for(int s=0;s<sectRatesTest.length;s++) {
			double sectRateDiff = this.totSectNuclRateArray[s]-sectRatesTest[s];
			double fractDiff = Math.abs(sectRateDiff)/totSectNuclRateArray[s];
			if(fractDiff>0.001 & D) {
				System.out.println("\tfractDiff = "+(float)fractDiff+" for " + rupSet.getFaultSectionData(s).getName());
			}
			rateUnassigned += sectRateDiff;
		}
		
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
					else if (origGridSeisTrulyOffVsSubSeisStatus[gridRegionIndex] == 1) { // it's all subseismo
						mfdForSrcSubSeisOnlyArray[s] = mfdForSrcArray[s];
						mfdForTrulyOffOnlyArray[s] = null;
					}
					else { // it's all truy off
						mfdForSrcSubSeisOnlyArray[s] = null;
						mfdForTrulyOffOnlyArray[s] = mfdForSrcArray[s];						
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
		SummedMagFreqDist magDist = new SummedMagFreqDist(mfdForSrcArray[0].getMinX(), mfdForSrcArray[0].getMaxX(), mfdForSrcArray[0].size());
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
				for(int m=0;m<mfd.size();m++) {
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
		
		SummedMagFreqDist magDist = new SummedMagFreqDist(mfdForSrcArray[0].getMinX(), mfdForSrcArray[0].getMaxX(), mfdForSrcArray[0].size());
		for(int srcIndex:rateForSrcInCubeHashtable.keySet()) {
			if(srcIndex < numFltSystSources)
				continue;	// skip fault based sources
			SummedMagFreqDist mfd=null;
			double srcNuclRate = rateForSrcInCubeHashtable.get(srcIndex);
			int gridIndex = srcIndex-numFltSystSources;
			int cubeRegIndex = getCubeRegAndDepIndicesForIndex(cubeIndex)[0];
			if(origGridSeisTrulyOffVsSubSeisStatus[gridIndex] == 2) { // gridded seismicity and cell has both types
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
				for(int m=0;m<mfd.size();m++)
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
		
		SummedMagFreqDist magDist = new SummedMagFreqDist(mfdForSrcArray[0].getMinX(), mfdForSrcArray[0].getMaxX(), mfdForSrcArray[0].size());
		for(int srcIndex:rateForSrcHashtable.keySet()) {
			if(srcIndex < numFltSystSources) {
				IncrementalMagFreqDist mfd = mfdForSrcArray[srcIndex].deepClone();
				double totRate = mfd.getTotalIncrRate();
				mfd.scale(rateForSrcHashtable.get(srcIndex)/totRate);
				magDist.addIncrementalMagFreqDist(mfd);
//				double srcNuclRate = rateForSrcHashtable.get(srcIndex);
//				double totRate = mfd.getTotalIncrRate();
//				if(totRate>0) {
//					for(int m=0;m<mfd.size();m++) {
//						magDist.add(m, mfd.getY(m)*srcNuclRate/totRate);
//				
//					}
//				}
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
	 * This compares the sum of the cube MFDs to the total ERF MFD.
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
	
	/**
	 * This verifies that summing cube values in each grid cell equals the value from the ERF in that cell.
	 */
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
			computeMFD_ForSrcArrays(2.05, 8.95, 70);
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


	/**
	 * TODO No longer used?
	 */
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
	
	
	
	
	/**
	 * This method was written to test constructing subseismo MFDs for a fault section in
	 * different ways, in part to confirm Ned's undestanding of how subseismo MFDs for a
	 * given grid node are distributed among multiple fault-section polygons that overlap
	 * the grid node.
	 * @param sectIndex
	 */
	public void testSubSeisMFD_ForSect(int sectIndex) {
		
		// this is the target MFD
		IncrementalMagFreqDist mfd2 = ((FaultSystemSolutionERF)erf).getSolution().getSubSeismoOnFaultMFD_List().get(sectIndex);
		mfd2.setName("Subseis MFD from erf.getSolution().getSubSeismoOnFaultMFD_List().get(sectIndex)");

		// Now we sill make this from the source MFDs
		if(mfdForSrcArray == null) {
			computeMFD_ForSrcArrays(2.05, 8.95, 70);
		}


		HashMap<Integer,Double> srcFractMap = new HashMap<Integer,Double>();
		Region faultPolygon = faultPolyMgr.getPoly(sectIndex);
		System.out.println("Section Polygon:\n"+faultPolygon.getBorder().toString());
		double cubeFrac = 1.0/(DEFAULT_NUM_PT_SRC_SUB_PTS*DEFAULT_NUM_PT_SRC_SUB_PTS);
		for(int i=0;i<gridRegForCubes.getNumLocations();i++) {
			Location cubeLoc = gridRegForCubes.getLocation(i);
			if(faultPolygon.contains(cubeLoc)) {
				int srcIndex = numFltSystSources + origGriddedRegion.indexForLocation(cubeLoc);
				if(srcFractMap.containsKey(srcIndex)) {
					double newFrac = srcFractMap.get(srcIndex)+cubeFrac;
					srcFractMap.put(srcIndex,newFrac);
				}
				else {
					srcFractMap.put(srcIndex,cubeFrac);
				}
			}
		}
		System.out.println("srcFractMap");
		for(int srcIndex:srcFractMap.keySet()) {
			Location gridLoc = origGriddedRegion.getLocation(srcIndex-numFltSystSources);
			System.out.println(srcIndex+"\t"+srcFractMap.get(srcIndex).floatValue()+"\t"+gridLoc);			
		}
		
		SummedMagFreqDist mfd3 = new SummedMagFreqDist(2.05,8.95,70);
		for(int s : srcFractMap.keySet()) {
			double scaleFactor = srcFractMap.get(s)/faultPolyMgr.getNodeFraction(s-numFltSystSources); // the fraction of subseimo MFD of node that goes to this section
			IncrementalMagFreqDist gridSubSeisSrcMFD = mfdForSrcSubSeisOnlyArray[s];
			System.out.println(s+"\tnull="+(gridSubSeisSrcMFD == null)+"\t+"+origGriddedRegion.getLocation(s-numFltSystSources)+
					"\t"+origGridSeisTrulyOffVsSubSeisStatus[s-numFltSystSources]);
			IncrementalMagFreqDist scaledMFD = gridSubSeisSrcMFD.deepClone();
			scaledMFD.scale(scaleFactor);
			mfd3.addIncrementalMagFreqDist(scaledMFD);
		}
		mfd3.setName("Total Subseis MFD from gridded sources - srcFractMap");

		
		
		System.out.println("altMap");
		Map<Integer, Double> altMap = faultPolyMgr.getScaledNodeFractions(sectIndex);
		for(int gridIndex:altMap.keySet()) {
			Location gridLoc = origGriddedRegion.getLocation(gridIndex);
			System.out.println(gridIndex+numFltSystSources+"\t"+altMap.get(gridIndex).floatValue()+"\t"+gridLoc);
		}
		SummedMagFreqDist mfd1 = new SummedMagFreqDist(2.05,8.95,70);
		for(int s : altMap.keySet()) {
			double scaleFactor = altMap.get(s)/faultPolyMgr.getNodeFraction(s); // the fraction of subseimo MFD of node that goes to this section
			IncrementalMagFreqDist gridSubSeisSrcMFD = mfdForSrcSubSeisOnlyArray[s+numFltSystSources];
			System.out.println(s+"\tnull="+(gridSubSeisSrcMFD == null)+"\t+"+origGriddedRegion.getLocation(s)+
					"\t"+origGridSeisTrulyOffVsSubSeisStatus[s]);
			IncrementalMagFreqDist scaledMFD = gridSubSeisSrcMFD.deepClone();
			scaledMFD.scale(scaleFactor);
			mfd1.addIncrementalMagFreqDist(scaledMFD);
		}
		mfd1.setName("Total Subseis MFD from gridded sources - altMap");
		

		ArrayList<IncrementalMagFreqDist> mfdList = new ArrayList<IncrementalMagFreqDist>();
		mfdList.add(mfd1);
		mfdList.add(mfd2);
		mfdList.add(mfd3);
		
		GraphWindow mfd_Graph = new GraphWindow(mfdList, "Subseis MFD comparison"); 
		mfd_Graph.setX_AxisLabel("Mag");
		mfd_Graph.setY_AxisLabel("Rate");
		mfd_Graph.setYLog(true);
		mfd_Graph.setPlotLabelFontSize(22);
		mfd_Graph.setAxisLabelFontSize(20);
		mfd_Graph.setTickLabelFontSize(18);			

	}



	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		CaliforniaRegions.RELM_TESTING_GRIDDED griddedRegion = RELM_RegionUtils.getGriddedRegionInstance();
		
		FaultSystemSolutionERF_ETAS erf = ETAS_Simulator.getU3_ETAS_ERF();

		// this tests whether total subseismo MFD from grid source provider is the same as from the fault-sys solution
//		FaultSysSolutionERF_Calc.testTotSubSeisMFD(erf);
		
		
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
		
//		etas_PrimEventSampler.plotMaxMagAtDepthMap(7d, "MaxMagAtDepth7km");
//		etas_PrimEventSampler.plotBulgeDepthMap(7d, "BulgeAtDepth7km");
//		etas_PrimEventSampler.plotRateAtDepthMap(7d,7.15,"RatesAboveM7pt1_AtDepth7km");
//		etas_PrimEventSampler.plotRateAtDepthMap(7d,5.05,"RatesAboveM5pt0_AtDepth7km");
//		etas_PrimEventSampler.plotRateAtDepthMap(7d,3.05,"RatesAboveM3pt0_AtDepth7km");
//		etas_PrimEventSampler.plotRateAtDepthMap(7d,6.55,"RatesAboveM6pt5_AtDepth7km_GRcorr");

//		etas_PrimEventSampler.writeGMT_PieSliceDecayData(new Location(34., -118., 12.0), "gmtPie_SliceData");
//		etas_PrimEventSampler.writeGMT_PieSliceRatesData(new Location(34., -118., 12.0), "gmtPie_SliceData");

		
//		etas_PrimEventSampler.writeRatesCrossSectionData(new Location(34.44,-118.34,1.), 0.29,"crossSectData_Rates_mojave_onlyFault", 6.55);
//		etas_PrimEventSampler.writeRatesCrossSectionData(new Location(34.44,-118.34,1.), 0.29,"crossSectData_Rates_mojave", 6.55);

//		etas_PrimEventSampler.writeBulgeCrossSectionData(new Location(34.486,-118.283,1.), 0.75,"crossSectDataBulgeGRcorr_mojave");

		// Sections bulge plot
//		try {
//			etas_PrimEventSampler.plotImpliedBulgeForSubSections(new File(GMT_CA_Maps.GMT_DIR, "ImpliedBulgeForSubSections"), "Test", true);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		
//		etas_PrimEventSampler.testNucleationRatesOfSourcesInCubes();
//		etas_PrimEventSampler.testRandomSourcesFromCubes();
		
//		etas_PrimEventSampler.getCubesAndFractForFaultSectionLinear(1846);	// Mojave S section
//		etas_PrimEventSampler.getCubesAndFractForFaultSectionLinear(256);
		
//		etas_PrimEventSampler.getCubesAndFractForFaultSectionExponential(1267); 	// Mendocino sub-section 20
//		etas_PrimEventSampler.getCubesAndFractForFaultSectionExponential(330); 	// Cleghorn Pass, Subsection 0
//		etas_PrimEventSampler.tempSectTest();
//		etas_PrimEventSampler.getCubesAndFractForFaultSectionExponential(1846); 	// Mojave S section
//		etas_PrimEventSampler.getCubesAndFractForFaultSectionPower(1846); 	// Mojave S section
		
		
		
//		etas_PrimEventSampler.testGR_CorrFactors(1846);
		
		// to test my understanding of the mapping of subSeis gridded seismicity back on to fault sections 
		// (where more than one fault polygon may cover the grid node)
//		etas_PrimEventSampler.testSubSeisMFD_ForSect(1846);
		
		// to plot an example result in Igor
//		HashMap<Integer,Double> map = etas_PrimEventSampler.getCubesAndDistancesInsideSectionPolygon(1846);
//		for(int cubeIndex : map.keySet()) {
//			Location loc = etas_PrimEventSampler.getCubeLocationForIndex(cubeIndex);
//			System.out.println(cubeIndex+"\t"+loc.getLongitude()+"\t"+loc.getLatitude()+"\t"+loc.getDepth()+"\t"+map.get(cubeIndex));
//			
//		}
		
		
		// THIS PLOTS THE MFDS IN CUBES MOVING AWAY FROM THE MOJAVE SECTION
		// Cube loc at center of Mojave subsection:	-117.9	34.46	7.0
//		ArrayList<SummedMagFreqDist> mfdList = new ArrayList<SummedMagFreqDist>();
//		ArrayList<EvenlyDiscretizedFunc> mfdListCum = new ArrayList<EvenlyDiscretizedFunc>();
//		for(int i=0; i<8; i++) {	// last 3 are outside polygon
//			Location loc = new Location(34.46+i*0.02, -117.90+i*0.02, 7.0);
//			int cubeIndex = etas_PrimEventSampler.getCubeIndexForLocation(loc);
//			SummedMagFreqDist mfd = etas_PrimEventSampler.getCubeMFD(cubeIndex);
//			mfd.setInfo("mfd "+i);
//			mfdListCum.add(mfd.getCumRateDistWithOffset());
//			mfd.setInfo(cubeIndex+"\n"+loc+"\n"+mfd.toString());
//			mfdList.add(mfd);
//		}
//		GraphWindow mfd_Graph = new GraphWindow(mfdList, "MFDs"); 
//		mfd_Graph.setX_AxisLabel("Mag");
//		mfd_Graph.setY_AxisLabel("Rate");
//		mfd_Graph.setYLog(true);
//		mfd_Graph.setPlotLabelFontSize(22);
//		mfd_Graph.setAxisLabelFontSize(20);
//		mfd_Graph.setTickLabelFontSize(18);			
//		GraphWindow cumMFD_Graph = new GraphWindow(mfdListCum, "Cumulative MFDs"); 
//		cumMFD_Graph.setX_AxisLabel("Mag");
//		cumMFD_Graph.setY_AxisLabel("Cumulative Rate");
//		cumMFD_Graph.setYLog(true);
//		cumMFD_Graph.setPlotLabelFontSize(22);
//		cumMFD_Graph.setAxisLabelFontSize(20);
//		cumMFD_Graph.setTickLabelFontSize(18);			
		
//		etas_PrimEventSampler.getCubeMFD(1378679);	// ??????????? not sure what this was for
		
		
//		System.out.println("testing rates and MFD");
//		etas_PrimEventSampler.testRates();
//		etas_PrimEventSampler.testMagFreqDist();

//		System.out.println("testGriddedSeisRatesInCubes()");
//		etas_PrimEventSampler.testGriddedSeisRatesInCubes();

		


		// the following could be compared with an xy scatter plot (did it in Igor; looks good)
//		etas_PrimEventSampler.plotOrigERF_RatesMap("OrigERF_RatesMap");
//		etas_PrimEventSampler.plotRandomSampleRatesMap("RandomSampleRatesMap", 100000);
		
		
	}
	
	/**
	 * This is a potentially more memory efficient way of reading/storing the int value, where there is
	 * only one int value/object for each index; turns out its not better than using int[] with duplicates.
	 * 
	 * TODO remove because this is not used?
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
	
	
		
	public List<EvenlyDiscretizedFunc> generateRuptureDiagnostics(ETAS_EqkRupture rupture, double expNum, String rupInfo, File resultsDir, FileWriter info_fileWriter) throws IOException {
		
		if(D) System.out.println("Starting generateRuptureDiagnostics");
		
		File subDirName = new File(resultsDir,"Diagnostics_"+rupInfo);
		if(!subDirName.exists())
			subDirName.mkdir();
		
		IntegerPDF_FunctionSampler aveCubeSamplerForRup = getAveSamplerForRupture(rupture);

//int tempCubeIndex = 431146;
//Location cubeLoc = getCubeLocationForIndex(tempCubeIndex);
//double minDist = Double.MAX_VALUE;
//double totDecayWt=0;
//for(Location loc:rupture.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface()) {
//	Location parLoc = getParLocationForIndex(getParLocIndexForLocation(loc));
//	double dist = LocationUtils.linearDistance(cubeLoc, parLoc);
//	if(dist<minDist) minDist=dist;
//	double relLat = Math.abs(parLoc.getLatitude()-latForCubeCenter[tempCubeIndex]);
//	double relLon = Math.abs(parLoc.getLongitude()-lonForCubeCenter[tempCubeIndex]);
//	double wt = etas_LocWeightCalc.getProbAtPoint(relLat, relLon, depthForCubeCenter[tempCubeIndex], parLoc.getDepth());
//	totDecayWt += wt;
//	System.out.println(tempCubeIndex+"\t"+dist+"\t"+wt);
//}
//System.out.println("Distance for cube "+tempCubeIndex+" is "+minDist+"; totDecayWt = "+totDecayWt);
//
//tempCubeIndex = 426462;
//cubeLoc = getCubeLocationForIndex(tempCubeIndex);
//minDist = Double.MAX_VALUE;
//totDecayWt=0;
//for(Location loc:rupture.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface()) {
//	Location parLoc = getParLocationForIndex(getParLocIndexForLocation(loc));
//	double dist = LocationUtils.linearDistance(cubeLoc, parLoc);
//	if(dist<minDist) minDist=dist;
//	double relLat = Math.abs(parLoc.getLatitude()-latForCubeCenter[tempCubeIndex]);
//	double relLon = Math.abs(parLoc.getLongitude()-lonForCubeCenter[tempCubeIndex]);
//	double wt = etas_LocWeightCalc.getProbAtPoint(relLat, relLon, depthForCubeCenter[tempCubeIndex], parLoc.getDepth());
//	totDecayWt += wt;
//	System.out.println(tempCubeIndex+"\t"+dist+"\t"+wt);
//}
//System.out.println("Distance for cube "+tempCubeIndex+" is "+minDist+"; totDecayWt = "+totDecayWt);
//
//System.exit(0);

//long st2 = System.currentTimeMillis();
//if (D) System.out.println("starting plotSubSectRelativeTriggerProbGivenSupraSeisRupture");
//String info2 = "\n\n"+ plotSubSectRelativeTriggerProbGivenSupraSeisRupture(aveCubeSamplerForRup, subDirName, 30, rupInfo, expNum);
//if (D) System.out.println("plotSubSectRelativeTriggerProbGivenSupraSeisRupture took (msec) "+(System.currentTimeMillis()-st2)+"\n"+info2);
//System.exit(0);

		double[] relSrcProbs = getRelativeTriggerProbOfEachSource(aveCubeSamplerForRup);
		

		long st = System.currentTimeMillis();
		List<EvenlyDiscretizedFunc> expectedPrimaryMFDsForScenarioList = ETAS_SimAnalysisTools.getExpectedPrimaryMFDs_ForRup(rupInfo, 
				new File(subDirName,rupInfo+"_ExpPrimMFD").getAbsolutePath(), 
				getExpectedPrimaryMFD_PDF(relSrcProbs), rupture, expNum);
		
		ETAS_SimAnalysisTools.plotExpectedPrimaryMFD_ForRup(rupInfo, 
				new File(subDirName,rupInfo+"_ExpPrimMFD").getAbsolutePath(), 
				expectedPrimaryMFDsForScenarioList, rupture, expNum);
		
		if (D) System.out.println("expectedPrimaryMFDsForScenarioList took (msec) "+(System.currentTimeMillis()-st));
		
		EvenlyDiscretizedFunc supraCumMFD = expectedPrimaryMFDsForScenarioList.get(3);
		double expectedNumSupra;
		if(supraCumMFD != null)
			expectedNumSupra = supraCumMFD.getY(0);
		else
			throw new RuntimeException("Need to figure out how to handle this");

		// this is three times slower:
//		st = System.currentTimeMillis();
//		List<EvenlyDiscretizedFunc> expectedPrimaryMFDsForScenarioList2 = ETAS_SimAnalysisTools.plotExpectedPrimaryMFD_ForRup("ScenarioAlt", new File(subDirName,"scenarioExpPrimMFD_Alt").getAbsolutePath(), 
//				this.getExpectedPrimaryMFD_PDF_Alt(aveAveCubeSamplerForRup), rupture, expNum);
//		if (D) System.out.println("getExpectedPrimaryMFD_PDF_Alt took (msec) "+(System.currentTimeMillis()-st));

		// Compute Primary Event Sampler Map
		st = System.currentTimeMillis();
		plotSamplerMap(aveCubeSamplerForRup, "Primary Sampler for "+rupInfo, "PrimarySamplerMap_"+rupInfo, subDirName);
		if (D) System.out.println("plotSamplerMap took (msec) "+(System.currentTimeMillis()-st));

		// Compute subsection participation probability map
		st = System.currentTimeMillis();
		String info = plotSubSectParticipationProbGivenRuptureAndReturnInfo(rupture, relSrcProbs, subDirName, 30, rupInfo);
		if (D) System.out.println("plotSubSectParticipationProbGivenRuptureAndReturnInfo took (msec) "+(System.currentTimeMillis()-st));

		
		// for subsection trigger probabilities (different than participation).
		st = System.currentTimeMillis();
		if (D) System.out.println("expectedNumSupra="+expectedNumSupra);
		info += "\n\n"+ plotSubSectTriggerProbGivenAllPrimayEvents(aveCubeSamplerForRup, subDirName, 30, rupInfo, expectedNumSupra);
		if (D) System.out.println("plotSubSectRelativeTriggerProbGivenSupraSeisRupture took (msec) "+(System.currentTimeMillis()-st));

		
		if (D) System.out.println(info);	
		info_fileWriter.write(info+"\n");
		
//info_fileWriter.close();
//System.exit(-1);
		
		return expectedPrimaryMFDsForScenarioList;
	}


	
	/**
	 * This plots the spatial distribution of probabilities implied by the given cubeSampler
	 * (probs are summed inside each spatial bin of gridRegForRatesInSpace).
	 * 
	 * TODO move this to ETAS_SimAnalysisTools
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
//		System.out.println("xyzDataSet.getMinZ()="+xyzDataSet.getMinZ());
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
//			System.out.println("minNonZero="+minNonZero);
//			System.out.println("xyzDataSet.getMinZ()="+xyzDataSet.getMinZ());
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
			computeMFD_ForSrcArrays(2.05, 8.95, 70);
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

	
	/**
	 * TODO Move to utility class
	 * @param depth
	 * @param dirName
	 * @return
	 */
	public String plotBulgeDepthMap(double depth, String dirName) {
		
		GMT_MapGenerator mapGen = GMT_CA_Maps.getDefaultGMT_MapGenerator();
		
		CPTParameter cptParam = (CPTParameter )mapGen.getAdjustableParamsList().getParameter(GMT_MapGenerator.CPT_PARAM_NAME);
		cptParam.setValue(GMT_CPT_Files.MAX_SPECTRUM.getFileName());
		
		mapGen.setParameter(GMT_MapGenerator.MIN_LAT_PARAM_NAME,gridRegForCubes.getMinGridLat());
		mapGen.setParameter(GMT_MapGenerator.MAX_LAT_PARAM_NAME,gridRegForCubes.getMaxGridLat());
		mapGen.setParameter(GMT_MapGenerator.MIN_LON_PARAM_NAME,gridRegForCubes.getMinGridLon());
		mapGen.setParameter(GMT_MapGenerator.MAX_LON_PARAM_NAME,gridRegForCubes.getMaxGridLon());
		mapGen.setParameter(GMT_MapGenerator.GRID_SPACING_PARAM_NAME, gridRegForCubes.getLatSpacing());	// assume lat and lon spacing are same
		mapGen.setParameter(GMT_MapGenerator.LOG_PLOT_NAME,true);
//		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MODE_NAME,GMT_MapGenerator.COLOR_SCALE_MODE_FROMDATA);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MODE_NAME,GMT_MapGenerator.COLOR_SCALE_MODE_MANUALLY);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME,-3d);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME,3d);
		
		
		GriddedGeoDataSet bulgeData = new GriddedGeoDataSet(gridRegForCubes, true);
		int depthIndex = getCubeDepthIndex(depth);
		int numCubesAtDepth = bulgeData.size();
		CalcProgressBar progressBar = new CalcProgressBar("Looping over all points", "junk");
		progressBar.showProgress(true);
		
		if(mfdForSrcArray == null) {
			computeMFD_ForSrcArrays(2.05, 8.95, 70);
		}

		for(int i=0; i<numCubesAtDepth;i++) {
			progressBar.updateProgress(i, numCubesAtDepth);
			int cubeIndex = getCubeIndexForRegAndDepIndices(i, depthIndex);
			SummedMagFreqDist mfdSupra = getCubeMFD_SupraSeisOnly(cubeIndex);
			SummedMagFreqDist mfdGridded = getCubeMFD_GriddedSeisOnly(cubeIndex);
			double bulge = 1.0;
			int[] regAndDepIndex = getCubeRegAndDepIndicesForIndex(cubeIndex);
			if((mfdSupra==null || mfdSupra.getMaxY()<10e-15) && isCubeInsideFaultPolygon[regAndDepIndex[0]] == 1)
				bulge = 10e-16; // set as zero if inside polygon and no mfd supra
			else if(mfdSupra != null &&  mfdGridded != null) {
				bulge = 1.0/ETAS_Utils.getScalingFactorToImposeGR(mfdSupra, mfdGridded, false);
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
			computeMFD_ForSrcArrays(2.05, 8.95, 70);
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
		double maxZ = Math.ceil(Math.log10(xyzDataSet.getMaxZ()))+0.5;
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME,maxZ-5);
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
//		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MODE_NAME,GMT_MapGenerator.COLOR_SCALE_MODE_FROMDATA);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MODE_NAME,GMT_MapGenerator.COLOR_SCALE_MODE_MANUALLY);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME,-2.);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME,1.);


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
		// this is good for M≥2.5
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME,-2.);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME,1.);


		CaliforniaRegions.RELM_TESTING_GRIDDED mapGriddedRegion = RELM_RegionUtils.getGriddedRegionInstance();
		GriddedGeoDataSet xyzDataSet = new GriddedGeoDataSet(mapGriddedRegion, true);
		
		// initialize values to zero
		for(int i=0; i<xyzDataSet.size();i++) xyzDataSet.set(i, 0);
		
		// do this to make sure it exists
		getCubeSamplerWithERF_RatesOnly();
		
		// get numYrs yrs worth of samples
		totRate=cubeSamplerRatesOnly.calcSumOfY_Vals();
		long numSamples = (long)numYrs*(long)totRate;
		System.out.println("num random samples for map test = "+numSamples+"\ntotRate="+totRate);
		
		CalcProgressBar progressBar = new CalcProgressBar("Looping random samples", "junk");
		progressBar.showProgress(true);
		for(long i=0;i<numSamples;i++) {
			progressBar.updateProgress(i, numSamples);
			int indexFromSampler = cubeSamplerRatesOnly.getRandomInt(etas_utils.getRandomDouble());
			int[] regAndDepIndex = getCubeRegAndDepIndicesForIndex(indexFromSampler);
			int indexForMap = mapGriddedRegion.indexForLocation(gridRegForCubes.locationForIndex(regAndDepIndex[0]));	// ignoring depth
			if(indexForMap>-0) {
				double oldNum = xyzDataSet.get(indexForMap)*numYrs;
				xyzDataSet.set(indexForMap, (1.0+oldNum)/(double)numYrs);
			}
			
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

	/**
	 * Method needed because this is a subclass of CacheLoader
	 */
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
