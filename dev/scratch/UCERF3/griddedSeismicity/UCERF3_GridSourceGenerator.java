package scratch.UCERF3.griddedSeismicity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.WC1994_MagLengthRelationship;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.Location;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.griddedSeis.Point2Vert_FaultPoisSource;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import com.google.common.collect.Maps;
import com.sun.msv.datatype.xsd.TotalDigitsFacet;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;

/**
 * This class generates the gridded sources for the UCERF3 background seismicity.
 * @author field
 *
 */
public class UCERF3_GridSourceGenerator {
	
	private static final CaliforniaRegions.RELM_TESTING_GRIDDED region  = new CaliforniaRegions.RELM_TESTING_GRIDDED();
	private static final WC1994_MagLengthRelationship magLenRel = new WC1994_MagLengthRelationship();
	
	private double ptSrcCutoff = 6.0;
	
	private FaultPolyMgr polyMgr;
	
	private double[] fracStrikeSlip,fracNormal,fracReverse;

	private IncrementalMagFreqDist totalOffFaultMFD;
	private IncrementalMagFreqDist realOffFaultMFD;	// this has the sub-seismo fault section rupture removed
	
	// the sub-seismogenic MFDs for those nodes that have them
	private Map<Integer, SummedMagFreqDist> nodeMFDs;
	// the sub-seismogenic MFDs for each section
	private Map<Integer, IncrementalMagFreqDist> sectSubSeisMFDs;

	private double[] srcSpatialPDF;		// from Karen or from UCERF2 (or maybe a deformation model)
	private double[] revisedSpatialPDF;	// revised to cut fault-section-polygon areas out (and renormalized)
	
	private FaultSystemSolution fss;
	
	double totalMgt5_Rate;
	
	// reference mfd values
	private double mfdMin = 5.05;
	private double mfdMax = 8.95;
	private int mfdNum = 35;
	
	/**
	 * Options:
	 * 
	 * 1) set a-values in fault-section polygons from moment-rate reduction or from smoothed seismicity
	 * 2) focal mechanism options, and finite vs point sources (cross hair, random strike, etc)?
	 * 
	 * @param fss FaultSystemSolution 
	 * @param totalOffFaultMFD
	 * @param spatialPDFsrc spatial PDF filename; defaults to KF if null
	 * @param totalMgt5_Rate
	 */
	public UCERF3_GridSourceGenerator(
			InversionFaultSystemSolution fss, 
			IncrementalMagFreqDist totalOffFaultMFD,
			String spatialPDFsrc,
			double totalMgt5_Rate) {
		
		this.fss = fss;
		
		this.totalOffFaultMFD = (totalOffFaultMFD == null) ? fss
			.getImpliedOffFaultStatewideMFD() : totalOffFaultMFD;
		mfdMin = this.totalOffFaultMFD.getMinX();
		mfdMax = this.totalOffFaultMFD.getMaxX();
		mfdNum = this.totalOffFaultMFD.getNum();
		
		this.totalMgt5_Rate = totalMgt5_Rate;
		
		polyMgr = new FaultPolyMgr(fss);
		
		// smoothed seismicity pdf and focal mechs
		initGrids(spatialPDFsrc);
		
		/*
		 *  1) compute subSeismoFaultSectMFD_Array, the sub-seismo MFD for each fault section.  Do this by
		 *  duplicating totalOffFaultMFD to faultSectionMFD, setting all rates above Mmin for the fault to zero
		 *  (to avoid double counting), and then constraining the total rate by either 
		 *  
		 *  	a) fault-section moment rate reduction (need get method for this) or
		 *  
		 *  	b) total smoothed seismicity rate inside polygon; need get method for this, and
		 *         subtract total rate of supra-seismogenic ruptures from:
		 *         faultSystemSolution.calcNucleationMFD_forSect(sectIndex, minMag, maxMag, numMag)
		 *         
		 *         Need to figure out what to do if fault section polygons overlap
		 *  
		 *  2) determine the fraction of each fault-section MFD that goes to each associated node 
		 *     (need an object that holds a list of node indices and their associated wts)
		 *  
		 *  3) determine the fraction of each node that is outside all fault-section polygons 
		 *  (fractOfNodeOutsideFaultPolygons[])
		 *  
		 *  4) compute realOffFaultMFD by subtracting all the fault-section MFDs from totalOffFaultMFD
		 *  
		 *  5) create revisedSpatialPDF by multiplying origSpatialPDF by fractOfNodeOutsideFaultPolygons[], and renormalize to
		 *  1.0.  Each value here multiplied by realOffFaultMFD is now the off-fault MFD for each node
		 *  
		 *  6) Each node also has some number of sub-seismo fault MFDs as determined in 2 (need to be able to query the
		 *     indices of fault sections associated with a given node, as well as the weight the node gets for each 
		 *     fault-section MFD).
		 *     
		 *  7) Summing all the MFDs for each node should give back totalOffFaultMFD
		 *  
		 *  Issue - if choice (a) in (1) is much higher on average than choice (b), we will have suppressed all truly off-fault
		 *  rates
		 */
		initSectionMFDs();
		initNodeMFDs();
		updateOffFaultMFD();
		updateSpatialPDF();
	}
	
	// 1
	private void initSectionMFDs() {
		sectSubSeisMFDs = Maps.newHashMap();
		List<FaultSectionPrefData> faults = fss.getFaultSectionDataList();

		for (FaultSectionPrefData sect : faults) {
			int idx = sect.getSectionId();
			double minMag = fss.getMinMagForSection(idx);
			IncrementalMagFreqDist subSeisMFD = totalOffFaultMFD.deepClone();
			subSeisMFD.zeroAtAndAboveMag(minMag);
				
			MomentMethod method = MomentMethod.SPATIAL;
			if (method == MomentMethod.MO_REDUCTION) {
				// scale by moment reduction
				double reduction = fss.getSubseismogenicReducedMomentRate(idx);
				subSeisMFD.scaleToTotalMomentRate(reduction);
			} else {
				// scale by smoothed seis area
				double polySeisRate = rateForSect(polyMgr.getSectFractions(idx));
//				IncrementalMagFreqDist supraSeisMFD = fss
//					.calcNucleationMFD_forSect(idx, mfdMin, mfdMax, mfdNum);
//				double sectCumRate = supraSeisMFD.getCumRate(mfdMin);
//				double scaledRate = polySeisRate - sectCumRate;
				subSeisMFD.scaleToCumRate(mfdMin, polySeisRate);
			}
			sectSubSeisMFDs.put(idx, subSeisMFD);
		}
	}
		
	
	/*
	 * partic = particip in each node
	 */
	private double rateForSect(Map<Integer, Double> particMap) {
		double sum = 0; // sect rate M>5
		for (Integer nodeIdx : particMap.keySet()) {
			double partic = particMap.get(nodeIdx);
//			System.out.println("node: " + nodeIdx);
//			System.out.println("  " + srcSpatialPDF[nodeIdx]);
//			System.out.println("  " + totalMgt5_Rate);
//			System.out.println("  " + partic);
			sum += srcSpatialPDF[nodeIdx] * totalMgt5_Rate * partic;
		}
//		System.out.println("sum: " + sum);
		return sum;
	}

	// 2 6
	private void initNodeMFDs() {
		nodeMFDs = Maps.newHashMap();
		for (FaultSectionPrefData sect : fss.getFaultSectionDataList()) {
			int id = sect.getSectionId();
			IncrementalMagFreqDist subSeisMFD = sectSubSeisMFDs.get(id);
			Map<Integer, Double> nodeFractions = polyMgr.getNodeFractions(id);
			for (Integer nodeIdx : nodeFractions.keySet()) {
				SummedMagFreqDist nodeMFD = nodeMFDs.get(nodeIdx);
				if (nodeMFD == null) {
					nodeMFD = new SummedMagFreqDist(mfdMin, mfdMax, mfdNum);
					nodeMFDs.put(nodeIdx, nodeMFD);
				}
				double scale = nodeFractions.get(nodeIdx);
				IncrementalMagFreqDist scaledMFD = subSeisMFD.deepClone();
				scaledMFD.scale(scale);
				nodeMFD.addIncrementalMagFreqDist(scaledMFD);
			}
		}
	}
	
	// 4
	private void updateOffFaultMFD() {
		double min = totalOffFaultMFD.getMinX();
		double max = totalOffFaultMFD.getMaxX();
		int num = totalOffFaultMFD.getNum();
		SummedMagFreqDist realMFD = new SummedMagFreqDist(min, max, num);
		realMFD.addIncrementalMagFreqDist(totalOffFaultMFD);
		for (IncrementalMagFreqDist mfd : sectSubSeisMFDs.values()) {
			realMFD.subtractIncrementalMagFreqDist(mfd);
		}
		realOffFaultMFD = realMFD;
	}
	
	//5
	private void updateSpatialPDF() {
		revisedSpatialPDF = new double[srcSpatialPDF.length];
		for (int i=0; i<region.getNodeCount(); i++) {
			double fraction =  polyMgr.getNodeFraction(i);
			revisedSpatialPDF[i] = srcSpatialPDF[i] * fraction;
		}
		// normalize
		double sum = 0.0;
		for (double d : revisedSpatialPDF) {
			sum += d;
		}
		for (int i=0; i<revisedSpatialPDF.length; i++) {
			revisedSpatialPDF[i] /= sum;
		}
	}
	
	private void initGrids(String spatialPDF) {
		if (spatialPDF == null) spatialPDF = "SmoothSeis_KF_3-12-2012.txt";
		GridReader gRead;
		gRead = new GridReader(spatialPDF);
		srcSpatialPDF = gRead.getValues(region);
		gRead = new GridReader("StrikeSlipWts.txt");
		fracStrikeSlip = gRead.getValues(region);
		gRead = new GridReader("ReverseWts.txt");
		fracReverse = gRead.getValues(region);
		gRead = new GridReader("NormalWts.txt");
		fracNormal = gRead.getValues(region);
//		System.out.println(Arrays.toString(fracReverse));
	}
	
	
	/**
	 * Returns the number of sources in the model.
	 * @return the source count
	 */
	public int getNumSources() {
		return region.getNodeCount();
	}
	
	/**
	 * This returns the sub-seismogenic MFD associated with a section.
	 * @param idx
	 * @return the MFD
	 */
	public IncrementalMagFreqDist getSectSubSeisMFD(int idx) {
		return sectSubSeisMFDs.get(idx);
	}

	/**
	 * This returns the MFD associated with a grid node.
	 * @param idx
	 * @return the MFD
	 */
	public IncrementalMagFreqDist getNodeTotalMFD(int idx) {
		SummedMagFreqDist sumMFD = new SummedMagFreqDist(mfdMin, mfdMax, mfdNum);
		
		IncrementalMagFreqDist nodeIndMFD = getNodeIndependentMFD(idx);
		if (nodeIndMFD != null) 
			sumMFD.addIncrementalMagFreqDist(nodeIndMFD);
		
		IncrementalMagFreqDist nodeSubMFD = getNodeSubSeisMFD(idx);
		if (nodeSubMFD != null) 
			sumMFD.addIncrementalMagFreqDist(nodeSubMFD);
		
		return sumMFD;
	}

	/**
	 * This returns the MFD associated with a grid node.
	 * @param idx
	 * @return the MFD
	 */
	public IncrementalMagFreqDist getNodeIndependentMFD(int idx) {
		IncrementalMagFreqDist mfd = realOffFaultMFD.deepClone();
		mfd.scale(revisedSpatialPDF[idx]);
		return mfd;
	}

	/**
	 * This returns the sub-seismogenic MFD associated with a grid node, if any
	 * exists.
	 * @param idx
	 * @return the MFD
	 */
	public IncrementalMagFreqDist getNodeSubSeisMFD(int idx) {
		return nodeMFDs.get(idx);
	}

	/**
	 * Returns the source of the requested type at the supplied index for a
	 * forecast with a given duration.
	 * @param type
	 * @param idx
	 * @param duration
	 * @return the source
	 */
	public ProbEqkSource getSource(SourceType type, int idx, double duration) {
		if (type == SourceType.RANDOM) return getRandomStrikeSource(idx, duration);
		if (type == SourceType.CROSSHAIR) return getCrosshairSource(idx, duration);
		return null;
	}
	
	/**
	 * Get the random strike gridded source at a specified index (this ignores
	 * the fixed-strike contribution)
	 * @param idx 
	 * @param duration 
	 * @return the source
	 */
	public ProbEqkSource getRandomStrikeSource(int idx, double duration) {
		IncrementalMagFreqDist mfd = getNodeTotalMFD(idx);
		Location loc = region.locationForIndex(idx);
		return new Point2Vert_FaultPoisSource(loc, mfd, magLenRel, duration,
			ptSrcCutoff, fracStrikeSlip[idx], fracNormal[idx], fracReverse[idx],
			false);
	}

	/**
	 * Get Crosshair gridded source at a specified index (this ignores the
	 * fixed-strike contribution)
	 * @param idx of location for source
	 * @param duration 
	 * @return the source
	 */
	public ProbEqkSource getCrosshairSource(int idx, double duration) {
		IncrementalMagFreqDist mfd = getNodeTotalMFD(idx);
		Location loc = region.locationForIndex(idx);
		return new Point2Vert_FaultPoisSource(loc, mfd, magLenRel, duration,
			ptSrcCutoff, fracStrikeSlip[idx], fracNormal[idx], fracReverse[idx],
			true);
	}

	/**
	 * Set whether all sources should just be treated as point sources, not just
	 * those with M&leq;6.0
	 * 
	 * @param usePoints
	 */
	public void setAsPointSources(boolean usePoints) {
		ptSrcCutoff = (usePoints) ? 10.0 : 6.0;
	}

	enum MomentMethod {
		MO_REDUCTION,
		SPATIAL;
	}
	
	enum SourceType {
		RANDOM,
		CROSSHAIR
	}
	
	
//	public static void main(String[] args) {
//		UCERF3_GridSourceGenerator gridGen = new UCERF3_GridSourceGenerator(
//			invFss, null,null, 8.54);
//	}

	
	// test data
//	static InversionFaultSystemSolution invFss;
//	static {
//		
//		SimpleFaultSystemSolution tmp = null;
//		try {
//			File f = new File("tmp/invSols/reference_gr_sol.zip");
//			tmp =  SimpleFaultSystemSolution.fromFile(f);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		// a little dirty as it reads the info string, but oh well
//		invFss = new InversionFaultSystemSolution(tmp);
		
		
		
//		List<FaultSectionPrefData> fs = invFss.getFaultSectionDataList();
//		SubSectionPolygons ssp = SubSectionPolygons.build(fs);
//
//		for (FaultSectionPrefData f : fs) {
//			System.out.println(f.getSectionId() + " " + f.getName() + " " + f.getParentSectionId() + " "+ f.getParentSectionName());
//			if (f.getSectionId() == 33) {
//				System.out.println(f.getName() + " " + f.getParentSectionId() + " "+ f.getParentSectionName());
//				System.out.println(f.getZonePolygon().getBorder());
//			}
//		}
		
//		for (int i=1288; i<1306; i++) {
//			List<LocationList> ppLists = SubSectionPolygons.areaToLocLists(ssp.get(i));
//			for (LocationList ppLoc : ppLists) {
//				System.out.println(ppLoc);
//			}
//		}
		
//		double totOffFaultMoRate = invFss.getTotalOffFaultSeisMomentRate();
//		System.out.println(totOffFaultMoRate);
//		
//		IncrementalMagFreqDist gr_mfd = invFss.getInpliedOffFaultStatewideMFD();
//		System.out.println(gr_mfd);
//		gr_mfd.zeroAboveMag(7.2);
//		System.out.println(gr_mfd);
		
//		double totMoRate = gr_mfd.getTotalMomentRate();
//		System.out.println(totMoRate);
//		
//		double magLimit = gr_mfd.findMagJustAboveMomentRate(totOffFaultMoRate);
//		gr_mfd.setValuesAboveMomentRateToZero(totOffFaultMoRate);
//		System.out.println("mag Limit:" + magLimit);
//
//		double newMoRate = gr_mfd.getTotalMomentRate();
//		System.out.println("newMoRate: " + newMoRate);
//		
//		System.out.println(gr_mfd); 

//	}
	
//	private ArrayList<IncrementalMagFreqDist> mfds = Lists.newArrayList();
//	private void plotMFDs() {
//		totalOffFaultMFD.setName("totalOffFault");
//		mfds.add(totalOffFaultMFD);
//		for (int i=516; i<517; i++) {
//			IncrementalMagFreqDist mfd = sectSubSeisMFDs.get(i);
////			System.out.println(mfd);
//			mfd.setName(Integer.toString(i));
//			mfds.add(mfd);
//		}
//		plot(mfds);
//	}

	static void plot(ArrayList<IncrementalMagFreqDist> mfds) { 
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(mfds,
				"GridSeis Test");
			graph.setX_AxisLabel("Magnitude");
			graph.setY_AxisLabel("Incremental Rate");
			graph.setYLog(true);
			graph.setY_AxisRange(1e-8, 1e2);

	}
}
