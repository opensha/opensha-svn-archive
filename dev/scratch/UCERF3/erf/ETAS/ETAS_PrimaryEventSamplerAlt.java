package scratch.UCERF3.erf.ETAS;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;

import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.LocationVector;
import org.opensha.commons.mapping.gmt.GMT_MapGenerator;
import org.opensha.commons.mapping.gmt.gui.GMT_MapGuiBean;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.ImageViewerWindow;
import org.opensha.sha.magdist.SummedMagFreqDist;

import scratch.UCERF3.erf.FaultSystemSolutionPoissonERF;
import scratch.UCERF3.erf.UCERF2_FaultSysSol_ERF;

public class ETAS_PrimaryEventSamplerAlt {
	
	final static boolean D=true;
	
	// these define the points in space
	int numDepths, numRegLocsForRatesInSpace, numPointsForRates,  numPointsForParLocs, numParDepths;
	double maxDepth, depthDiscr;
	GriddedRegion gridRegForRatesInSpace;
	GriddedRegion gridRegForParentLocs;
	double regSpacing;
	
	FaultSystemSolutionPoissonERF erf;
	int numFltSystSources;
	
	// this is for each point in space
	double[] latForPoint, lonForPoint, depthForPoint;
	
	// this will hold the rate of each ERF source
	double sourceRates[];
	
	// this stores the rates of erf ruptures that go unassigned (outside the region here)
	double rateUnassigned;
	double totRate;
	
	ArrayList<double[]> fractionSrcAtPointList;
	ArrayList<int[]> srcAtPointList;
	
	IntegerPDF_FunctionSampler pointSampler;
	
	IntegerPDF_FunctionSampler[] cachedSamplers;
	
	// ETAS distance decay params
	double distDecay, minDist;
	ETAS_LocationWeightCalculatorHypDepDep[]  etasLocWtCalclist;
	
	boolean includeERF_Rates, includeSpatialDecay;
	
	
	

	/**
	 * TO DO
	 * 
	 * @param gridRegion
	 * @param erf
	 * @param maxDepth
	 * @param depthDiscr
	 * @param pointSrcDiscr - the grid spacing of off-fault/background events
	 * @param includeERF_Rates - this applies long term rates from ERF in sampling the primary event
	 * @param rupToFillIn - this applies the spatial distance decay in sampling the primary event (set false for testing)
	 */
	
	/**
	 * 
	 * @param gridRegForRatesInSpace - region for rates in space
	 * @param gridRegForParentLocs - region for parent locations (points here should be half way between those in the above)
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
	 */
	public ETAS_PrimaryEventSamplerAlt(GriddedRegion gridRegForRatesInSpace, GriddedRegion gridRegForParentLocs, FaultSystemSolutionPoissonERF erf, double sourceRates[],
			double maxDepth, double depthDiscr, double pointSrcDiscr, String oututFileNameWithPath, double distDecay, 
			double minDist, boolean includeERF_Rates, boolean includeSpatialDecay) {
		
		this.erf = erf;
		
		this.maxDepth=maxDepth;
		this.depthDiscr=depthDiscr;
		numDepths = (int)Math.round(maxDepth/depthDiscr);
		
		this.gridRegForRatesInSpace = gridRegForRatesInSpace;
		numRegLocsForRatesInSpace = gridRegForRatesInSpace.getNumLocations();
		numPointsForRates = numRegLocsForRatesInSpace*numDepths;
		
		this.gridRegForParentLocs = gridRegForParentLocs;
		numParDepths = numDepths+1;
		numPointsForParLocs = gridRegForParentLocs.getNumLocations()*numParDepths;
		
		// this is for caching samplers (one for each possible parent location)
		cachedSamplers = new IntegerPDF_FunctionSampler[numPointsForParLocs];
		
		this.sourceRates = sourceRates;
		
		this.distDecay=distDecay;
		this.minDist=minDist;
		
		this.includeERF_Rates=includeERF_Rates;
		this.includeSpatialDecay=includeSpatialDecay;

		regSpacing = gridRegForRatesInSpace.getLatSpacing();
		// do some checks
		if(gridRegForRatesInSpace.getLonSpacing() != regSpacing)
			throw new RuntimeException("gridRegForRatesInSpace.getLonSpacing() must equal gridRegForRatesInSpace.getLatSpacing()");
		if(gridRegForParentLocs.getLonSpacing() != regSpacing)
			throw new RuntimeException("gridRegForParentLocs.getLonSpacing() must equal gridRegForRatesInSpace.getLatSpacing()");
		
		latForPoint = new double[numPointsForRates];
		lonForPoint = new double[numPointsForRates];
		depthForPoint = new double[numPointsForRates];
		for(int i=0;i<numPointsForRates;i++) {
			int[] regAndDepIndex = getRegAndDepIndicesForSamplerIndex(i);
			Location loc = gridRegForRatesInSpace.getLocation(regAndDepIndex[0]);
			latForPoint[i] = loc.getLatitude();
			lonForPoint[i] = loc.getLongitude();
			depthForPoint[i] = getDepth(regAndDepIndex[1]);
			
			// test - turn off once done once
//			Location testLoc = this.getLocationForSamplerIndex(i);
//			if(Math.abs(testLoc.getLatitude()-latForPoint[i]) > 0.00001)
//				throw new RuntimeException("Lats diff by more than 0.00001");
//			if(Math.abs(testLoc.getLongitude()-lonForPoint[i]) > 0.00001)
//				throw new RuntimeException("Lons diff by more than 0.00001");
//			if(Math.abs(testLoc.getDepth()-depthForPoint[i]) > 0.00001)
//				throw new RuntimeException("Depths diff by more than 0.00001");
			
		}

		if(D) System.out.println("Initializing sourcesAtPointList & fractionsAtPointList");
		ArrayList<ArrayList<Integer>> sourcesAtPointList = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Double>> fractionsAtPointList = new ArrayList<ArrayList<Double>>();
		for(int i=0; i<numPointsForRates;i++) {
			sourcesAtPointList.add(new ArrayList<Integer>());
			fractionsAtPointList.add(new ArrayList<Double>());
		}

		int numPtSrcSubPts = (int)Math.round(pointSrcDiscr/regSpacing);
	
		rateUnassigned=0;

		int totNumSrc = erf.getNumSources();
		if(totNumSrc != sourceRates.length)
			throw new RuntimeException("Problem with number of sources");
		
		numFltSystSources = erf.getNumFaultSystemSources();
		
		CalcProgressBar progressBar = new CalcProgressBar("Sources to process in ETAS_PrimaryEventSamplerAlt", "junk");
		progressBar.showProgress(true);
	
		if(D) System.out.println("Starting loop to populate fractionSrcAtPointList & srcAtPointList");
		for(int s=0;s<totNumSrc;s++) {
			ProbEqkSource src = erf.getSource(s);
			progressBar.updateProgress(s, totNumSrc);

			// If it's not a point source:
			if(s<numFltSystSources) {
				Hashtable<Integer,Double> fractAtPointTable = new Hashtable<Integer,Double>(); // int is ptIndex and double is fraction there
				LocationList locsOnRupSurf = src.getRupture(0).getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();
				int numLocs = locsOnRupSurf.size();
				for(Location loc: locsOnRupSurf) {
					int regIndex = gridRegForRatesInSpace.indexForLocation(loc);
					int depIndex = getDepthIndex(loc.getDepth());
					if(regIndex != -1) {
						int ptIndex = depIndex*numRegLocsForRatesInSpace+regIndex;
						if(fractAtPointTable.containsKey(ptIndex)) {
							double newFrac = fractAtPointTable.get(ptIndex) + 1.0/(double)numLocs;
							fractAtPointTable.put(ptIndex,newFrac);
						}
						else {
							fractAtPointTable.put(ptIndex,1.0/(double)numLocs);
						}
					}
					else {
						rateUnassigned += sourceRates[s]/numLocs;
					}
				}	
				// now assign this hashTable
				for(Integer ptIndex : fractAtPointTable.keySet()) {
					double fract = fractAtPointTable.get(ptIndex);
					sourcesAtPointList.get(ptIndex).add(s);
					fractionsAtPointList.get(ptIndex).add(fract);
				}
			}
			else {	// It's a point source
				for(ProbEqkRupture rup: src)
					if(!rup.getRuptureSurface().isPointSurface())	// make sure they're all point surfaces
						throw new RuntimeException("All ruptures for source must have point surfaces here");

				Location centerLoc = src.getRupture(0).getRuptureSurface().getFirstLocOnUpperEdge();
				double numPts = numPtSrcSubPts*numPtSrcSubPts*numDepths;
				double ptRate = sourceRates[s]/numPts;
				double ptFrac = 1.0/numPts;
				// distribution this among the locations within the space represented by the point source
				for(int iLat=0; iLat<numPtSrcSubPts;iLat++) {
					double lat = centerLoc.getLatitude()-pointSrcDiscr/2 + iLat*regSpacing + regSpacing/2.0;
					for(int iLon=0; iLon<numPtSrcSubPts;iLon++) {
						double lon = centerLoc.getLongitude()-pointSrcDiscr/2 + iLon*regSpacing + regSpacing/2.0;
						int regIndex = gridRegForRatesInSpace.indexForLocation(new Location(lat,lon));
						if(regIndex != -1){
							for(int iDep =0; iDep<numDepths; iDep++) {
//								int ptIndex = iDep*numRegLocs+regIndex;
								int ptIndex = getSamplerIndexForRegAndDepIndices(regIndex,iDep);
								sourcesAtPointList.get(ptIndex).add(s);
								fractionsAtPointList.get(ptIndex).add(ptFrac);
							}
						}
						else {
							rateUnassigned += ptRate*numDepths;
//							System.out.println("1\t"+centerLoc.getLatitude()+"\t"+centerLoc.getLongitude()+"\t"+centerLoc.getDepth());
						}
					}
				}
			}
		}
		progressBar.showProgress(false);
		if(D) System.out.println("rateUnassigned="+rateUnassigned);
		
		
		if(D) System.out.println("Converting list types");
		fractionSrcAtPointList = new ArrayList<double[]>();
		srcAtPointList = new ArrayList<int[]> ();
		for(int i=0;i<numPointsForRates;i++) {
			ArrayList<Integer> sourceList = sourcesAtPointList.get(i);
			ArrayList<Double> fractList = fractionsAtPointList.get(i);
			int[] sourceArray = new int[sourceList.size()];
			double[] fractArray = new double[fractList.size()];
			for(int j=0;j<sourceArray.length;j++) {
				sourceArray[j] = sourceList.get(j);
				fractArray[j] = fractList.get(j);
			}
			srcAtPointList.add(sourceArray);
			fractionSrcAtPointList.add(fractArray);
		}
		if(D) System.out.println("Done converting list types");
		
		if(D) System.out.println("Running makeETAS_LocWtCalcList()");
		makeETAS_LocWtCalcList();
		if(D) System.out.println("Done running makeETAS_LocWtCalcList()");
		
		// write results to file
//		if(oututFileNameWithPath != null)
//			writeEqksAtPointArrayToFile(oututFileNameWithPath);
		
	}
	
	
	/**
	 * This method will populate the given rupToFillIn with attributes of a randomly chosen
	 * primary aftershock for the given main shock.  If a fault system rupture is sampled, the
	 * hypocenter is chosen randomly from the points on the surface that are at the point where
	 * the event was sampled from (uniform distribution, and not weighted by the distance each
	 * surface point at that location is from the source.
	 * from the source
	 * @param mainshock
	 * @param rupToFillIn
	 */
	public void setRandomPrimaryEvent(EqkRupture mainshock, ETAS_EqkRupture rupToFillIn) {
		
		// first set point on main shock that nucleates the aftershock (randomly chosen with uniform probability)
		Location parentLoc=null;
		if(mainshock.getRuptureSurface().isPointSurface()) {
			parentLoc = mainshock.getRuptureSurface().getFirstLocOnUpperEdge();
		}
		else {
			LocationList locList = mainshock.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();
			// get random point on surface
			// need to check this; Math.round() is same as (long)Math.floor(a + 0.5d)
			parentLoc = locList.get((int)Math.round(locList.size()*Math.random()-0.5));
		}
		
		// set the sampler
		int parRegIndex = gridRegForParentLocs.indexForLocation(parentLoc);
		if(parRegIndex <0)
			throw new RuntimeException("parRegIndex<0");
		int parDepIndex = getParDepthIndex(parentLoc.getDepth());
		int locIndexForPar = parDepIndex*gridRegForParentLocs.getNodeCount()+parRegIndex;
		Location tempLoc = gridRegForParentLocs.getLocation(parRegIndex);
		// the above will be null if it's out of the region
		Location translatedParLoc = new Location(tempLoc.getLatitude(),tempLoc.getLongitude(),getParDepth(parDepIndex));
		
//System.out.println("parentLoc: "+parentLoc);
//System.out.println("parDepIndex: "+parDepIndex);
//System.out.println("getParDepth(parDepIndex): "+getParDepth(parDepIndex));
//System.out.println("translatedParLoc: "+translatedParLoc);
//System.exit(0);

		IntegerPDF_FunctionSampler sampler=null;
		if(includeERF_Rates && includeSpatialDecay) {
			if(cachedSamplers[locIndexForPar] == null) {
				sampler = getPointSamplerWithDistDecay(translatedParLoc);
				cachedSamplers[locIndexForPar] = sampler;
			}
			else {
				sampler = cachedSamplers[locIndexForPar];
			}
				
		}
		else if(includeERF_Rates && !includeSpatialDecay) {
			sampler = getPointSamplerWithERF_RatesOnly();
		}
		else if(!includeERF_Rates && includeSpatialDecay) {
			if(cachedSamplers[locIndexForPar] == null) {
				sampler = getPointSamplerWithOnlyDistDecay(translatedParLoc);
				cachedSamplers[locIndexForPar] = sampler;
//System.out.println("Used this one: getPointSamplerWithOnlyDistDecay(parentLoc)");
			}
			else {
				sampler = cachedSamplers[locIndexForPar];
			}

		}
		
		int aftShPointIndex = sampler.getRandomInt();
		int randSrcIndex = getRandomSourceIndexAtPoint(aftShPointIndex);
		
		// following is needed for case where includeERF_Rates = false (point can be chosen that has no sources)
		if(randSrcIndex<0) {
//			System.out.println("working on finding a non-neg source indes");
			while (randSrcIndex<0) {
				aftShPointIndex = sampler.getRandomInt();
				randSrcIndex = getRandomSourceIndexAtPoint(aftShPointIndex);
			}
		}
		
		ProbEqkSource src = erf.getSource(randSrcIndex);
		int r=0;
		if(src.getNumRuptures() > 1) {
			r = src.drawSingleRandomEqkRuptureIndex();
		}
		int nthRup = erf.getIndexN_ForSrcAndRupIndices(randSrcIndex,r);
		ProbEqkRupture erf_rup = src.getRupture(r);
		
		// set hypocenter location & rupture surface
		if(randSrcIndex < numFltSystSources) {	// if it's a fault system source
			// need to choose point on rup surface that is the hypocenter			
			LocationList locsOnRupSurf = erf_rup.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();
			LocationList locsToSampleFrom = new LocationList();
			for(Location loc: locsOnRupSurf) {
				if(aftShPointIndex == getSamplerIndexForLocation(loc)) {
					locsToSampleFrom.add(loc);
				}
			}	
			// choose one randomly
			int hypoLocIndex = (int)Math.round(locsToSampleFrom.size()*Math.random()-0.5);
			rupToFillIn.setHypocenterLocation(locsToSampleFrom.get(hypoLocIndex));
			rupToFillIn.setRuptureSurface(erf_rup.getRuptureSurface());
		}
		else { // it's a gridded seis source
			double relLat = latForPoint[aftShPointIndex]-translatedParLoc.getLatitude();
			double relLon = lonForPoint[aftShPointIndex]-translatedParLoc.getLongitude();
			double relDep = depthForPoint[aftShPointIndex]-translatedParLoc.getDepth();
						
			Location deltaLoc = etasLocWtCalclist[parDepIndex].getRandomDeltaLoc(Math.abs(relLat), Math.abs(relLon), Math.abs(relDep));
			
			double newLat, newLon, newDep;
			if(relLat<0.0)	// neg value
				newLat = latForPoint[aftShPointIndex]-deltaLoc.getLatitude();
			else 
				newLat = latForPoint[aftShPointIndex]+deltaLoc.getLatitude();
			if(relLon<0.0)	// neg value
				newLon = lonForPoint[aftShPointIndex]-deltaLoc.getLongitude();
			else 
				newLon = lonForPoint[aftShPointIndex]+deltaLoc.getLongitude();
			if(relDep<0.0)	// neg value
				newDep = depthForPoint[aftShPointIndex]-deltaLoc.getDepth();
			else 
				newDep = depthForPoint[aftShPointIndex]+deltaLoc.getDepth();

			Location randLoc = new Location(newLat,newLon,newDep);
			
			// get a location vector pointing from the translated parent location to the actual parent location nearest point here to the srcLoc
			LocationVector corrVector = LocationUtils.vector(translatedParLoc, parentLoc);
			Location hypLoc = LocationUtils.location(randLoc, corrVector);
			// WE COULD CACHE THE ABOVE VECTORS
//			System.out.println("corrVector:\t"+corrVector.getHorzDistance()+"\t"+corrVector.getVertDistance()+"\t"+corrVector.getAzimuth());
//			System.out.println("randLoc:\t"+randLoc);
//			System.out.println("hypLoc:\t"+hypLoc);
//			System.exit(0);
			
			// this does the same thing
//			Location hypLoc = new Location(
//					randLoc.getLatitude()-(translatedParLoc.getLatitude()-parentLoc.getLatitude()),
//					randLoc.getLongitude()-(translatedParLoc.getLongitude()-parentLoc.getLongitude()),
//					randLoc.getDepth()-(translatedParLoc.getDepth()-parentLoc.getDepth()));
					
// Location hypLoc = randLoc;
// this fixes it
// hypLoc = new Location(latForPoint[samplerIndex],lonForPoint[samplerIndex],depthForPoint[samplerIndex]);
			rupToFillIn.setHypocenterLocation(hypLoc);
			rupToFillIn.setPointSurface(hypLoc);
//			rupToFillIn.setHypocenterLocation(randLoc);
//			rupToFillIn.setPointSurface(randLoc);
			
//			System.out.println("parentLoc="+parentLoc);
//			System.out.println("parentLoc nearest point="+nearestLocToSrcLoc);
//			System.out.println("aft point loc="+"\t"+latForPoint[samplerIndex]+"\t"+lonForPoint[samplerIndex]+"\t"+depthForPoint[samplerIndex]);
//			System.out.println("deltaLoc="+deltaLoc);
//			System.out.println("randLoc="+randLoc);
//			System.out.println("hypLoc="+hypLoc);
//
//			System.out.println("corrVector.getHorzDistance()"+corrVector.getHorzDistance());
//			System.exit(0);

		}
		
		// fill in the rest
		rupToFillIn.setAveRake(erf_rup.getAveRake());
		rupToFillIn.setMag(erf_rup.getMag());
		rupToFillIn.setNthERF_Index(nthRup);
		
		// distance of triggered event from parent
//		Location hypoLoc= rupToFillIn.getHypocenterLocation();
//		double relLat = parentLoc.getLatitude()-hypoLoc.getLatitude();
//		double relLon = parentLoc.getLongitude()-hypoLoc.getLongitude();
//		double relDep = parentLoc.getDepth()-hypoLoc.getDepth();
//		double distToParent = etasLocWtCalclist[parDepIndex].getDistance(relLat, relLon, relDep);
		double distToParent = LocationUtils.linearDistanceFast(parentLoc, rupToFillIn.getHypocenterLocation());
		rupToFillIn.setDistanceToParent(distToParent);
	}
	
	
	
	
	private void makeETAS_LocWtCalcList() {
		double maxDistKm=1000.0;
		double midLat = (gridRegForRatesInSpace.getMaxLat() + gridRegForRatesInSpace.getMinLat())/2.0;
		if(D) System.out.println("midLat="+midLat);
		etasLocWtCalclist = new ETAS_LocationWeightCalculatorHypDepDep[numParDepths];
		for(int iParDep=0;iParDep<numParDepths;iParDep ++) {
			etasLocWtCalclist[iParDep] = new ETAS_LocationWeightCalculatorHypDepDep(maxDistKm, maxDepth, 
											regSpacing, depthDiscr, midLat, distDecay, minDist, iParDep);
//			etasLocWtCalclist[iParDep].testRandomSamples(1000000);
		}
	}
	
	
	/**
	 * This will force updating of all the samplers
	 */
	public void declareRateChange() {
		pointSampler = null;
		cachedSamplers = new IntegerPDF_FunctionSampler[numPointsForParLocs];
	}
	
	
	
	/**
	 * This method
	 */
	private IntegerPDF_FunctionSampler getPointSamplerWithERF_RatesOnly() {
		if(pointSampler == null) {
			pointSampler = new IntegerPDF_FunctionSampler(numPointsForRates);
			for(int i=0;i<numPointsForRates;i++) {
				int[] sources = srcAtPointList.get(i);
				double[] fract = fractionSrcAtPointList.get(i);
				double totRate=0;
				for(int j=0; j<sources.length;j++) {
					totRate += sourceRates[sources[j]]*fract[j];
				}
				pointSampler.set(i,totRate);
			}
		}
		return pointSampler;
	}
	
	
	private IntegerPDF_FunctionSampler getPointSamplerWithDistDecay(Location srcLoc) {
		getPointSamplerWithERF_RatesOnly();	// this makes sure it is updated
		IntegerPDF_FunctionSampler sampler = new IntegerPDF_FunctionSampler(numDepths*numRegLocsForRatesInSpace);
		ETAS_LocationWeightCalculatorHypDepDep etasLocWtCalc = etasLocWtCalclist[getParDepthIndex(srcLoc.getDepth())];
		for(int index=0; index<numPointsForRates; index++) {
			double relLat = Math.abs(srcLoc.getLatitude()-latForPoint[index]);
			double relLon = Math.abs(srcLoc.getLongitude()-lonForPoint[index]);
			double relDep = Math.abs(srcLoc.getDepth()-depthForPoint[index]);
			sampler.set(index,etasLocWtCalc.getProbAtPoint(relLat, relLon, relDep)*pointSampler.getY(index));
		}
		return sampler;
	}

	/**
	 * This sampler ignores the long-term rates
	 * @param mainshock
	 * @param etasLocWtCalc
	 * @return
	 */
	private IntegerPDF_FunctionSampler getPointSamplerWithOnlyDistDecay(Location parLoc) {
		IntegerPDF_FunctionSampler sampler = new IntegerPDF_FunctionSampler(numPointsForRates);
//System.out.println("parDepthIndex="+parDepthIndex+" (depth="+parLoc.getDepth()+")");
		ETAS_LocationWeightCalculatorHypDepDep etasLocWtCalc = etasLocWtCalclist[getParDepthIndex(parLoc.getDepth())];
// etasLocWtCalc.testRandomSamples(1000000);
//		try{
//			FileWriter fw1 = new FileWriter("test123.txt");
//			fw1.write("relLat\trelLon\trelDep\twt\n");
			for(int index=0; index<numPointsForRates; index++) {
				double relLat = Math.abs(parLoc.getLatitude()-latForPoint[index]);
				double relLon = Math.abs(parLoc.getLongitude()-lonForPoint[index]);
				double relDep = Math.abs(parLoc.getDepth()-depthForPoint[index]);
				sampler.set(index,etasLocWtCalc.getProbAtPoint(relLat, relLon, relDep));
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
	 * This returns a negative int (-1) if there is no source at the point (which can happen if
	 * includeERF_Rates = false since these points won't be zero)
	 * @param ptIndex
	 * @return
	 */
	public int getRandomSourceIndexAtPoint(int ptIndex) {
		int[] sources = srcAtPointList.get(ptIndex);
		if(sources.length==0) {
			return -1;
		}
		else if (sources.length==1) {
			return sources[0];
		}
		else {
			double[] fracts = fractionSrcAtPointList.get(ptIndex);
			IntegerPDF_FunctionSampler sampler = new IntegerPDF_FunctionSampler(sources.length);
			for(int s=0; s<sources.length;s++) 
				sampler.set(s,sourceRates[sources[s]]*fracts[s]);
			return sources[sampler.getRandomInt()];			
		}
	}

	
	/**
	 * Region index is first element, and depth index is second
	 * @param index
	 * @return
	 */
	private int[] getRegAndDepIndicesForSamplerIndex(int index) {
		
		int[] indices = new int[2];
		indices[1] = (int)Math.floor((double)index/(double)numRegLocsForRatesInSpace);	// depth index
		if(indices[1] >= this.numDepths )
			System.out.println("PROBLEM: "+index+"\t"+numRegLocsForRatesInSpace+"\t"+indices[1]+"\t"+numDepths);
		indices[0] = index - indices[1]*numRegLocsForRatesInSpace;						// region index
		return indices;
	}
	
	public Location getLocationForSamplerIndex(int index) {
		int[] regAndDepIndex = getRegAndDepIndicesForSamplerIndex(index);
		Location regLoc = gridRegForRatesInSpace.getLocation(regAndDepIndex[0]);
		return new Location(regLoc.getLatitude(),regLoc.getLongitude(),getDepth(regAndDepIndex[1]));
	}
	
	public int getSamplerIndexForLocation(Location loc) {
		int iReg = gridRegForRatesInSpace.indexForLocation(loc);
		int iDep = getDepthIndex(loc.getDepth());
		return getSamplerIndexForRegAndDepIndices(iReg,iDep);
	}

	private int getSamplerIndexForRegAndDepIndices(int iReg,int iDep) {
		return iDep*numRegLocsForRatesInSpace+iReg;
	}


	// this tests that the rates represented here (plus unassigned rates) match that in the ERF.
	public void testRates() {
		
		System.out.println("Testing total rate");
		getPointSamplerWithERF_RatesOnly();
		
		totRate=this.pointSampler.calcSumOfY_Vals();
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
	 * This compares the MFD represented here to that in the ERF
	 * @param erf
	 */
	public void testMagFreqDist() {
		
		System.out.println("Running testMagFreqDist()");
		SummedMagFreqDist magDist = new SummedMagFreqDist(2.05, 8.95, 70);
//		getPointSampler();	// make sure it exisits
		double duration = erf.getTimeSpan().getDuration();
		for(int i=0; i<numPointsForRates;i++) {
			int[] sources = srcAtPointList.get(i);
			double[] fracts = fractionSrcAtPointList.get(i);
			for(int s=0; s<sources.length;s++) {
				SummedMagFreqDist mfd = ERF_Calculator.getTotalMFD_ForSource(erf.getSource(sources[s]), duration, 2.05, 8.95, 70, true);
				mfd.scale(fracts[s]);
				magDist.addIncrementalMagFreqDist(mfd);
				if(s>erf.getNumFaultSystemSources()) {
					System.out.println("source "+s+"\n"+mfd);
					System.exit(0);
				}
			}
		}
		magDist.setName("MFD from EqksAtPoint list");
		ArrayList<EvenlyDiscretizedFunc> magDistList = new ArrayList<EvenlyDiscretizedFunc>();
		magDistList.add(magDist);
		magDistList.add(magDist.getCumRateDistWithOffset());
		
		SummedMagFreqDist erfMFD = ERF_Calculator.getTotalMFD_ForERF(erf, 2.05, 8.95, 70, true);
		erfMFD.setName("MFD from ERF");
		magDistList.add(erfMFD);
		magDistList.add(erfMFD.getCumRateDistWithOffset());

		// Plot these MFDs
		GraphiWindowAPI_Impl magDistsGraph = new GraphiWindowAPI_Impl(magDistList, "Mag-Freq Distributions"); 
		magDistsGraph.setX_AxisLabel("Mag");
		magDistsGraph.setY_AxisLabel("Rate");
		magDistsGraph.setY_AxisRange(1e-6, magDistsGraph.getY_AxisMax());
		magDistsGraph.setYLog(true);

	}
	
	
	private int getDepthIndex(double depth) {
		return (int)Math.round((depth-depthDiscr/2.0)/depthDiscr);
	}
	
	private double getDepth(int depthIndex) {
		return (double)depthIndex*depthDiscr + depthDiscr/2;
	}
	
	private int getParDepthIndex(double depth) {
		return (int)Math.round(depth/depthDiscr);
	}
	
	private double getParDepth(int parDepthIndex) {
		return parDepthIndex*depthDiscr;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("Instantiating ERF");
		UCERF2_FaultSysSol_ERF erf = new UCERF2_FaultSysSol_ERF();
		erf.updateForecast();
		
		double sourceRates[] = new double[erf.getNumSources()];
		double duration = erf.getTimeSpan().getDuration();
		for(int s=0;s<erf.getNumSources();s++)
			sourceRates[s] = erf.getSource(s).computeTotalEquivMeanAnnualRate(duration);

//		CaliforniaRegions.RELM_GRIDDED griddedRegion = new CaliforniaRegions.RELM_GRIDDED();
		
		System.out.println("Instantiating Region");
		GriddedRegion griddedRegionForRates = new GriddedRegion(new CaliforniaRegions.RELM_TESTING(), 0.02, GriddedRegion.ANCHOR_0_0);
		GriddedRegion griddedRegionForParLocs = new GriddedRegion(new CaliforniaRegions.RELM_TESTING(), 0.02, new Location(0.01,0.01));
		
		long startTime = System.currentTimeMillis();
		System.out.println("Instantiating ETAS_PrimaryEventSamplerAlt");
		
		String testFileName = "/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_ERF/testBinaryFile";

		ETAS_PrimaryEventSamplerAlt erf_RatesAtPointsInSpace = new ETAS_PrimaryEventSamplerAlt(griddedRegionForRates, griddedRegionForParLocs, erf, 
				sourceRates, 24d,2d,0.1,testFileName, 2, 0.3,true,true);
		System.out.println("Instantiating took "+(System.currentTimeMillis()-startTime)/1000+" sec");

		// TESTS
//		erf_RatesAtPointsInSpace.testRates(erf);
//		erf_RatesAtPointsInSpace.testMagFreqDist(erf);
//		erf_RatesAtPointsInSpace.plotRatesMap("test", true, "/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_ERF/mapTest");
//		erf_RatesAtPointsInSpace.plotOrigERF_RatesMap("orig test", true, "/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_ERF/mapOrigTest", erf);
//		erf_RatesAtPointsInSpace.plotRandomSampleRatesMap("random test", true, "/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_ERF/mapRandomTest", erf,10000);

		

	}
	
	
	
	/**
	 * This plots the rates in space for the RELM region (rates are summed inside each spatial bin).
	 * 
	 * Compare this to what produced by the plotOrigERF_RatesMap(*) method (should be same)
	 * 
	 * @param label - plot label
	 * @param local - whether GMT map is made locally or on server
	 * @param dirName
	 * @return
	 */
	public String plotRatesMap(IntegerPDF_FunctionSampler pointSamplerForMap, String label, boolean local, String dirName) {
		
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


		CaliforniaRegions.RELM_GRIDDED mapGriddedRegion = new CaliforniaRegions.RELM_GRIDDED();
		GriddedGeoDataSet xyzDataSet = new GriddedGeoDataSet(mapGriddedRegion, true);
		
		// initialize values to zero
		for(int i=0; i<xyzDataSet.size();i++) xyzDataSet.set(i, 0);
		
		getPointSamplerWithERF_RatesOnly();
		for(int i=0;i<numPointsForRates;i++) {
			Location loc = getLocationForSamplerIndex(i);
			int mapLocIndex = mapGriddedRegion.indexForLocation(loc);
			if(mapLocIndex>=0) {
				double oldRate = xyzDataSet.get(mapLocIndex);
				xyzDataSet.set(mapLocIndex, pointSamplerForMap.getY(i)+oldRate);					
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
	public String plotOrigERF_RatesMap(String label, boolean local, String dirName, FaultSystemSolutionPoissonERF erf) {
		
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


		CaliforniaRegions.RELM_GRIDDED mapGriddedRegion = new CaliforniaRegions.RELM_GRIDDED();
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
	public String plotRandomSampleRatesMap(String label, boolean local, String dirName, FaultSystemSolutionPoissonERF erf, int numYrs) {
		
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


		CaliforniaRegions.RELM_GRIDDED mapGriddedRegion = new CaliforniaRegions.RELM_GRIDDED();
		GriddedGeoDataSet xyzDataSet = new GriddedGeoDataSet(mapGriddedRegion, true);
		
		// initialize values to zero
		for(int i=0; i<xyzDataSet.size();i++) xyzDataSet.set(i, 0);
		
		// get numYrs yrs worth of samples
		int numSamples = numYrs*(int)totRate;
		System.out.println("num random samples for map test = "+numSamples);
		// do this to make sure it exists
		getPointSamplerWithERF_RatesOnly();
		
		for(int i=0;i<numSamples;i++) {
			int indexFromSampler = pointSampler.getRandomInt();
			int[] regAndDepIndex = getRegAndDepIndicesForSamplerIndex(indexFromSampler);
			int indexForMap = mapGriddedRegion.indexForLocation(gridRegForRatesInSpace.locationForIndex(regAndDepIndex[0]));	// ignoring depth
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
