package scratch.UCERF3.griddedSeismicity;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.WC1994_MagLengthRelationship;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.util.DataUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.griddedSeis.Point2Vert_FaultPoisSource;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.MomentRateFixes;
import scratch.UCERF3.enumTreeBranches.SpatialSeisPDF;
import scratch.UCERF3.enumTreeBranches.TotalMag5Rate;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.utils.GardnerKnopoffAftershockFilter;
import scratch.UCERF3.utils.RELM_RegionUtils;

import com.google.common.collect.Maps;

/**
 * This class generates UCERF3 background seismicity (gridded) sources.
 * 
 * @author Ned Field
 * @author Peter Powers
 */
public class UCERF3_GridSourceGenerator extends AbstractGridSourceProvider {

	private final CaliforniaRegions.RELM_TESTING_GRIDDED region = RELM_RegionUtils.getGriddedRegionInstance();

	private InversionFaultSystemSolution ifss;
	private LogicTreeBranch branch;
	private FaultPolyMgr polyMgr;
	
	// spatial pdfs of seismicity, orginal and revised (reduced and
	// renormalized) to avoid double counting with fault polygons
	private double[] srcSpatialPDF;
	private double[] revisedSpatialPDF;
	
	private double totalMgt5_Rate;

	// total off-fault MFD (sub-seismo + background)
	private IncrementalMagFreqDist realOffFaultMFD;

	// the sub-seismogenic MFDs for those nodes that have them
	private Map<Integer, SummedMagFreqDist> nodeSubSeisMFDs;

	// the sub-seismogenic MFDs for each section
	private Map<Integer, IncrementalMagFreqDist> sectSubSeisMFDs;

	// reference mfd values
	private double mfdMin = 5.05;
	private double mfdMax = 8.45;
	private int mfdNum = 35;

	/**
	 * Options:
	 * 
	 * 1) set a-values in fault-section polygons from moment-rate reduction or
	 * from smoothed seismicity
	 * 
	 * 2) focal mechanism options, and finite vs point
	 * sources (cross hair, random strike, etc)?
	 * 
	 * @param fss {@code InversionFaultSystemSolution} for which
	 *        grided/background sources should be generated
	 */
	public UCERF3_GridSourceGenerator(InversionFaultSystemSolution ifss) {

		this.ifss = ifss;
		branch = ifss.getBranch();
		srcSpatialPDF = branch.getValue(SpatialSeisPDF.class).getPDF();
		totalMgt5_Rate = branch.getValue(TotalMag5Rate.class).getRateMag5();
		realOffFaultMFD = ifss.getFinalTrulyOffFaultMFD();

		mfdMin = realOffFaultMFD.getMinX();
		mfdMax = realOffFaultMFD.getMaxX();
		mfdNum = realOffFaultMFD.getNum();

//		polyMgr = FaultPolyMgr.create(fss.getFaultSectionDataList(), 12d);
		polyMgr = ifss.getInversionMFDs().getGridSeisUtils().getPolyMgr();

//		System.out.println("   initFocalMechGrids() ...");
//		initFocalMechGrids();
		System.out.println("   initSectionMFDs() ...");
		initSectionMFDs();
		System.out.println("   initNodeMFDs() ...");
		initNodeMFDs();
		System.out.println("   updateSpatialPDF() ...");
		updateSpatialPDF();
	}


	/*
	 * Initialize the sub-seismogenic MFDs for each fault section
	 * (sectSubSeisMFDs)
	 */
	private void initSectionMFDs() {

		List<GutenbergRichterMagFreqDist> subSeisMFD_list = 
				ifss.getFinalSubSeismoOnFaultMFD_List();

		sectSubSeisMFDs = Maps.newHashMap();
		List<FaultSectionPrefData> faults = ifss.getFaultSectionDataList();
		for (int i = 0; i < faults.size(); i++) {
			sectSubSeisMFDs.put(
				faults.get(i).getSectionId(),
				subSeisMFD_list.get(i));
		}
	}

	/*
	 * Initialize the sub-seismogenic MFDs for each grid node
	 * (nodeSubSeisMFDs) by partitioning the sectSubSeisMFDs according to
	 * the overlapping fraction of each fault section and grid node.
	 */
	private void initNodeMFDs() {
		nodeSubSeisMFDs = Maps.newHashMap();
		for (FaultSectionPrefData sect : ifss.getFaultSectionDataList()) {
			int id = sect.getSectionId();
			IncrementalMagFreqDist sectSubSeisMFD = sectSubSeisMFDs.get(id);
			Map<Integer, Double> nodeFractions = polyMgr.getNodeFractions(id);
			for (Integer nodeIdx : nodeFractions.keySet()) {
				SummedMagFreqDist nodeMFD = nodeSubSeisMFDs.get(nodeIdx);
				if (nodeMFD == null) {
					nodeMFD = new SummedMagFreqDist(mfdMin, mfdMax, mfdNum);
					nodeSubSeisMFDs.put(nodeIdx, nodeMFD);
				}
				double scale = nodeFractions.get(nodeIdx);
				IncrementalMagFreqDist scaledMFD = sectSubSeisMFD.deepClone();
				scaledMFD.scale(scale);
				nodeMFD.addIncrementalMagFreqDist(scaledMFD);
			}
		}
	}

	/*
	 * Update (normalize) the spatial PDF to account for those nodes that
	 * are partially of fully occupied by faults to whom all small magnitude
	 * events will have been apportioned.
	 */
	private void updateSpatialPDF() {
		// update pdf
		revisedSpatialPDF = new double[srcSpatialPDF.length];
		for (int i=0; i<region.getNodeCount(); i++) {
			double fraction = 1 - polyMgr.getNodeFraction(i);
			revisedSpatialPDF[i] = srcSpatialPDF[i] * fraction;
		}
		// normalize
		DataUtils.asWeights(revisedSpatialPDF);
	}


//	private void initFocalMechGrids() {
//		GridReader gRead;
//		gRead = new GridReader("StrikeSlipWts.txt");
//		fracStrikeSlip = gRead.getValues();
//		gRead = new GridReader("ReverseWts.txt");
//		fracReverse = gRead.getValues();
//		gRead = new GridReader("NormalWts.txt");
//		fracNormal = gRead.getValues();
//	}
//

//	@Override
//	public int size() {
//		return region.getNodeCount();
//	}
//
	/**
	 * Returns the sub-seismogenic MFD associated with a section.
	 * @param idx node index
	 * @return the MFD
	 */
	public IncrementalMagFreqDist getSectSubSeisMFD(int idx) {
		return sectSubSeisMFDs.get(idx);
	}

	/**
	 * Returns the sum of the sub-seismogenic MFDs of all fault sub-sections.
	 * @return the MFD
	 */
	public IncrementalMagFreqDist getSectSubSeisMFD() {
		SummedMagFreqDist sum = new SummedMagFreqDist(mfdMin, mfdMax, mfdNum);
		sum.setName("Sub-seismogenic MFD for all fault sections");
		for (IncrementalMagFreqDist mfd : sectSubSeisMFDs.values()) {
			sum.addIncrementalMagFreqDist(mfd);
		}
		return sum;
	}

//	/**
//	 * Returns the MFD associated with a grid node. This is the sum of any
//	 * unassociated and sub-seismogenic MFDs for the node.
//	 * @param idx node index
//	 * @return the MFD
//	 * @see UCERF3_GridSourceGenerator#getNodeUnassociatedMFD(int)
//	 * @see UCERF3_GridSourceGenerator#getNodeSubSeisMFD(int)
//	 */
//	public IncrementalMagFreqDist getNodeMFD(int idx) {
//		SummedMagFreqDist sumMFD = new SummedMagFreqDist(mfdMin, mfdMax, mfdNum);
//
//		IncrementalMagFreqDist nodeIndMFD = getNodeUnassociatedMFD(idx);
//		if (nodeIndMFD != null)
//			sumMFD.addIncrementalMagFreqDist(nodeIndMFD);
//
//		IncrementalMagFreqDist nodeSubMFD = getNodeSubSeisMFD(idx);
//		if (nodeSubMFD != null)
//			sumMFD.addIncrementalMagFreqDist(nodeSubMFD);
//
//		return sumMFD;
//	}
	
//	/**
//	 * Returns the MFD associated with a grid node trimmed to the supplied 
//	 * minimum magnitude and the maximum non-zero magnitude.
//	 * 
//	 * @param idx node index
//	 * @param minMag minimum magniitude to trim MFD to
//	 * @return the trimmed MFD
//	 */
//	public IncrementalMagFreqDist getNodeMFD(int idx, double minMag) {
//		return trimMFD(getNodeMFD(idx), minMag);
//	}

	/**
	 * Returns the unassociated MFD of a grid node.
	 * @param idx node index
	 * @return the MFD
	 */
	public IncrementalMagFreqDist getNodeUnassociatedMFD(int idx) {
		IncrementalMagFreqDist mfd = realOffFaultMFD.deepClone();
		mfd.scale(revisedSpatialPDF[idx]);
		return mfd;
	}

	/**
	 * Returns the sum of the unassociated MFD of all nodes.
	 * @return the MFD
	 */
	public IncrementalMagFreqDist getNodeUnassociatedMFD() {
		realOffFaultMFD.setName("Unassociated MFD for all nodes");
		return realOffFaultMFD;
	}

	/**
	 * Returns the sub-seismogenic MFD associated with a grid node, if any
	 * exists.
	 * @param idx node index
	 * @return the MFD
	 */
	public IncrementalMagFreqDist getNodeSubSeisMFD(int idx) {
		return nodeSubSeisMFDs.get(idx);
	}

	/**
	 * Returns the sum of the sub-seismogenic MFD of all nodes.
	 * @return the MFD
	 */
	public IncrementalMagFreqDist getNodeSubSeisMFD() {
		SummedMagFreqDist sum = new SummedMagFreqDist(mfdMin, mfdMax, mfdNum);
		sum.setName("Sub-seismogenic MFD for all nodes");
		for (IncrementalMagFreqDist mfd : nodeSubSeisMFDs.values()) {
			sum.addIncrementalMagFreqDist(mfd);
		}
		return sum;
	}

	/**
	 * Returns the MFD associated with a grid node, implied by the
	 * {@code spatialPDF} of seismicity and the {@code totalMgt5_Rate} supplied
	 * at initialization.
	 * @param inPoly {@code true} for MFD associated with fault polygons,
	 *        {@code false} if unassociated part requested
	 * @param idx node index
	 * @return the MFD
	 */
	public IncrementalMagFreqDist getSpatialMFD(boolean inPoly, int idx) {
		GutenbergRichterMagFreqDist mfd = new GutenbergRichterMagFreqDist(
			mfdMin, mfdMax, mfdNum);
		mfd.setAllButTotMoRate(mfdMin, mfdMax, totalMgt5_Rate, 0.8);
		double frac = polyMgr.getNodeFraction(idx);
		if (!inPoly) frac = 1 - frac;
		mfd.scale(frac);
		return mfd;
	}

//	/**
//	 * Returns the source of the requested type at the supplied index for a
//	 * forecast with a given duration.
//	 * @param idx node index
//	 * @param duration of forecast
//	 * @param filterAftershocks (Gardner-Knopoff filter)
//	 * @param isCrosshair
//	 * @return the source
//	 * @see GardnerKnopoffAftershockFilter
//	 */
//	public ProbEqkSource getSource(int idx, double duration,
//			boolean filterAftershocks, boolean isCrosshair) {
//		Location loc = region.locationForIndex(idx);
//		IncrementalMagFreqDist mfd = getNodeMFD(idx, 5.05);
//		if (filterAftershocks) scaleMFD(mfd);
//
//		return new Point2Vert_FaultPoisSource(loc, mfd, magLenRel, duration,
//			ptSrcCutoff, fracStrikeSlip[idx], fracNormal[idx],
//			fracReverse[idx], isCrosshair);
//	}

//	/*
//	 * Utility to trim the supplied MFD to the supplied min mag and the maximum
//	 * non-zero mag. This method makes the assumtions that the min mag of the 
//	 * supplied mfd is lower then the mMin, and that mag bins are centered on
//	 * 0.05.
//	 */
//	private static IncrementalMagFreqDist trimMFD(IncrementalMagFreqDist mfdIn, double mMin) {
//		// in GR nofix branches there are mfds with all zero rates
//		try {
//			double mMax = mfdIn.getMaxMagWithNonZeroRate();
//			int num = (int) ((mMax - mMin) / 0.1) + 1;
//			IncrementalMagFreqDist mfdOut = new IncrementalMagFreqDist(mMin, mMax, num);
//			for (int i=0; i<mfdOut.getNum(); i++) {
//				double mag = mfdOut.getX(i);
//				double rate = mfdIn.getY(mag);
//				mfdOut.set(mag, rate);
//			}
//			return mfdOut;
//		} catch (Exception e) {
//			System.out.println("empty MFD");
//			IncrementalMagFreqDist mfdOut = new IncrementalMagFreqDist(mMin,mMin,1);
//			mfdOut.scaleToCumRate(mMin, 0.0);
//			return mfdOut;
//		}
//	}
	
	
	
//	/*
//	 * Applies gardner Knopoff aftershock filter scaling to MFD in place.
//	 */
//	private static void scaleMFD(IncrementalMagFreqDist mfd) {
//		double scale;
//		for (Point2D p : mfd) {
//			scale = GardnerKnopoffAftershockFilter.scaleForMagnitude(p.getX());
//			p.setLocation(p.getX(), p.getY() * scale);
//			mfd.set(p);
//		}
//	}
//
//	/**
//	 * Set whether all sources should just be treated as point sources, not just
//	 * those with M&leq;6.0
//	 * 
//	 * @param usePoints
//	 */
//	public void setAsPointSources(boolean usePoints) {
//		ptSrcCutoff = (usePoints) ? 10.0 : 6.0;
//	}


	public static void main(String[] args) {
		//
		//		GutenbergRichterMagFreqDist grMFD = new GutenbergRichterMagFreqDist(1.0, 1.0, 5.05, 8.05, 31);
		//		System.out.println(grMFD);
		//		scaleMFD(grMFD);
		//		System.out.println(grMFD);

		SimpleFaultSystemSolution tmp = null;
		try {
			//			File f = new File("tmp/invSols/reference_ch_sol2.zip");
//			File f = new File("/Users/pmpowers/projects/OpenSHA/tmp/invSols/refCH/FM3_1_NEOK_EllB_DsrUni_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_mean_sol.zip");
			File f = new File("/Users/pmpowers/projects/OpenSHA/tmp/invSols/refGR/FM3_1_NEOK_EllB_DsrUni_GRUnconst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_mean_sol.zip");
			System.out.println(f.exists());
			tmp = SimpleFaultSystemSolution.fromFile(f);
		} catch (Exception e) {
			e.printStackTrace();
		}
		InversionFaultSystemSolution invFss = new InversionFaultSystemSolution(tmp);
		
		UCERF3_GridSourceGenerator gridGen = new UCERF3_GridSourceGenerator(invFss);
		int numSrcs = gridGen.size();
		int numRups = 0;
		System.out.println("numSrcs: " + numSrcs);
		for (int i=0; i<numSrcs; i++) {
			numRups += gridGen.getSource(i, 1, false, false).getNumRuptures();
		}
		System.out.println("numRups: " + numRups);

		// gr nofix error test
				List<GutenbergRichterMagFreqDist> list = invFss.getImpliedSubSeisGR_MFD_List();
				System.out.println(list.size());
		
		//		UCERF3_GridSourceGenerator gridGen = new UCERF3_GridSourceGenerator(
		//			invFss, null, SpatialSeisPDF.UCERF3, 8.54, SmallMagScaling.MO_REDUCTION);
		//		System.out.println("init done");


		//		Location loc = new Location(36, -119);
		//		Location loc = new Location(34, -118.5);
		//		int locIdx = region.indexForLocation(loc);
		//		System.out.println(loc+ " " + locIdx);
		//
		//		System.out.println("SubSeis");
		//		System.out.println(gridGen.getNodeSubSeisMFD(locIdx));
		//		System.out.println("Indep");
		//		System.out.println(gridGen.getNodeIndependentMFD(locIdx));
		//		System.out.println("Total");
		//		System.out.println(gridGen.getNodeTotalMFD(locIdx));
		//
		//		Point2Vert_FaultPoisSource peq = (Point2Vert_FaultPoisSource) gridGen.getSource(GridSourceType.CROSSHAIR, locIdx, 1);
		//		System.out.println("EqRup");
		//		System.out.println( peq.getMFD());

	}

	static void plot(ArrayList<IncrementalMagFreqDist> mfds) {
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(mfds,
				"GridSeis Test");
		graph.setX_AxisLabel("Magnitude");
		graph.setY_AxisLabel("Incremental Rate");
		graph.setYLog(true);
		graph.setY_AxisRange(1e-8, 1e2);

	}

	@Override
	public GriddedRegion getGriddedRegion() {
		return region;
	}

}
