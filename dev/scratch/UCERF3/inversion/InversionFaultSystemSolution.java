package scratch.UCERF3.inversion;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentException;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.geo.Region;
import org.opensha.commons.geo.RegionUtils;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.util.ClassUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.gui.infoTools.PlotSpec;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.analysis.FaultSystemRupSetCalc;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.MaxMagOffFault;
import scratch.UCERF3.enumTreeBranches.MomentRateFixes;
import scratch.UCERF3.enumTreeBranches.SpatialSeisPDF;
import scratch.UCERF3.enumTreeBranches.TotalMag5Rate;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.logicTree.LogicTreeBranchNode;
import scratch.UCERF3.utils.MFD_InversionConstraint;
import scratch.UCERF3.utils.OLD_UCERF3_MFD_ConstraintFetcher;
import scratch.UCERF3.utils.OLD_UCERF3_MFD_ConstraintFetcher.TimeAndRegion;
import scratch.UCERF3.utils.RELM_RegionUtils;
import scratch.UCERF3.utils.UCERF2_MFD_ConstraintFetcher;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * This is a FaultSystemSolution that also contains parameters used in the UCERF3 Inversion
 * 
 * @author kevin
 *
 */
public class InversionFaultSystemSolution extends SimpleFaultSystemSolution {
	
	private InversionModels invModel;
	private LogicTreeBranch branch;
	
	private boolean ucerf3MFDs = true;
	private double MFDTransitionMag = Double.NaN;
	private boolean weightSlipRates = true;
	private double relativePaleoRateWt = Double.NaN;
	private double relativeMagnitudeEqualityConstraintWt = Double.NaN;
	private double relativeMagnitudeInequalityConstraintWt = Double.NaN;
	private double relativeRupRateConstraintWt = Double.NaN;
	private double relativeParticipationSmoothnessConstraintWt = Double.NaN;
	private double relativeMinimizationConstraintWt = Double.NaN;
	private double relativeMomentConstraintWt = Double.NaN;
	private double minimumRuptureRateFraction = Double.NaN;
	
	private InversionMFDs inversionMFDs;

	/**
	 * Parses the info string for inversion parameters
	 * 
	 * @param solution
	 */
	public InversionFaultSystemSolution(FaultSystemSolution solution) {
		super(solution);
		
		String info = getInfoString();
		
		ArrayList<String> infoLines = Lists.newArrayList(Splitter.on("\n").split(info));
		
		try {
			Map<String, String> invProps = loadProperties(getMetedataSection(infoLines, "Inversion Configuration Metadata"));
			Map<String, String> branchProps = loadProperties(getMetedataSection(infoLines, "Logic Tree Branch"));
			branch = loadBranch(branchProps);
			invModel = branch.getValue(InversionModels.class);
			loadInvParams(invProps);
			
			double totalRegionRateMgt5 = branch.getValue(TotalMag5Rate.class).getRateMag5();
			double mMaxOffFault = branch.getValue(MaxMagOffFault.class).getMaxMagOffFault();
			boolean applyImpliedCouplingCoeff = branch.getValue(MomentRateFixes.class).isApplyCC();
			SpatialSeisPDF spatialSeisPDF = branch.getValue(SpatialSeisPDF.class);
			inversionMFDs = new InversionMFDs(solution, totalRegionRateMgt5, mMaxOffFault, applyImpliedCouplingCoeff, spatialSeisPDF, invModel);
		} catch (RuntimeException e) {
			// can be uncommented for debugging string parse errors
//			System.out.println("******* EXCEPTION CAUGHT INSTANTIATING IFSS - PRINTING METADATA *********");
//			System.out.println(info);
//			System.out.println("*************************************************************************");
//			System.out.flush();
			throw e;
		}
	}
	
	private Map<String, String> loadProperties(ArrayList<String> section) {
		Map<String, String> props = Maps.newHashMap();
		
		for (String line : section) {
			line = line.trim();
			int ind = line.indexOf(':');
			if (ind < 0)
				continue;
			String key = line.substring(0, ind);
			String value = line.substring(ind+1).trim();
			props.put(key, value);
		}
		
		return props;
	}
	
	private ArrayList<String> getMetedataSection(ArrayList<String> lines, String title) {
		ArrayList<String> section = null;
		
		for (String line : lines) {
			if (section == null) {
				if (line.contains("*") && line.contains(title)) {
					section = new ArrayList<String>();
					continue;
				}
			} else {
				if (line.contains("*"))
					break;
				section.add(line);
			}
		}
		
		return section;
	}
	
	private LogicTreeBranch loadBranch(Map<String, String> props) {
		List<Class<? extends LogicTreeBranchNode<?>>> classes = LogicTreeBranch.getLogicTreeNodeClasses();
		
		List<LogicTreeBranchNode<?>> values = Lists.newArrayList();
		
		for (String key : props.keySet()) {
			// find the associated class
			Class<? extends LogicTreeBranchNode<?>> clazz = null;
			for (Class<? extends LogicTreeBranchNode<?>> testClass : classes) {
				String className = ClassUtils.getClassNameWithoutPackage(testClass);
				if (className.startsWith(key)) {
					clazz = testClass;
					break;
				}
			}
			Preconditions.checkNotNull(clazz, "Couldn't find class for logic tree branch: "+key);
			
			String valueName = props.get(key);
			LogicTreeBranchNode<?> value = null;
			for (LogicTreeBranchNode<?> testValue : clazz.getEnumConstants()) {
				if (testValue.name().equals(valueName)) {
					value = testValue;
					break;
				}
			}
			Preconditions.checkNotNull(value, "Couldn't find matching constant for logic tree value "+key+" (node="
					+ClassUtils.getClassNameWithoutPackage(clazz)+")");
			values.add(value);
		}
		
		return LogicTreeBranch.fromValues(values);
	}
	
//	private double offFaultAseisFactor = Double.NaN;
//	private double mfdConstraintModifier = Double.NaN;
//	private boolean ucerf3MFDs = true;
//	private double bilinearTransitionMag = Double.NaN;
//	private double MFDTransitionMag = Double.NaN;
//	private boolean weightSlipRates = true;
//	private double relativePaleoRateWt = Double.NaN;
//	private double relativeMagnitudeEqualityConstraintWt = Double.NaN;
//	private double relativeMagnitudeInequalityConstraintWt = Double.NaN;
//	private double relativeRupRateConstraintWt = Double.NaN;
//	private double relativeParticipationSmoothnessConstraintWt = Double.NaN;
//	private double relativeMinimizationConstraintWt = Double.NaN;
//	private double relativeMomentConstraintWt = Double.NaN;
//	private double minimumRuptureRateFraction = Double.NaN;
	
	private void loadInvParams(Map<String, String> props) {
		if (props.containsKey("MFDTransitionMag"))
			MFDTransitionMag = Double.parseDouble(props.get("MFDTransitionMag"));
		if (props.containsKey("relativePaleoRateWt"))
			relativePaleoRateWt = Double.parseDouble(props.get("relativePaleoRateWt"));
		if (props.containsKey("relativeMagnitudeEqualityConstraintWt"))
			relativeMagnitudeEqualityConstraintWt = Double.parseDouble(props.get("relativeMagnitudeEqualityConstraintWt"));
		if (props.containsKey("relativeMagnitudeInequalityConstraintWt"))
			relativeMagnitudeInequalityConstraintWt = Double.parseDouble(props.get("relativeMagnitudeInequalityConstraintWt"));
		if (props.containsKey("relativeRupRateConstraintWt"))
			relativeRupRateConstraintWt = Double.parseDouble(props.get("relativeRupRateConstraintWt"));
		if (props.containsKey("relativeParticipationSmoothnessConstraintWt"))
			relativeParticipationSmoothnessConstraintWt = Double.parseDouble(props.get("relativeParticipationSmoothnessConstraintWt"));
		if (props.containsKey("relativeMinimizationConstraintWt"))
			relativeMinimizationConstraintWt = Double.parseDouble(props.get("relativeMinimizationConstraintWt"));
		if (props.containsKey("relativeMinimizationConstraintWt"))
			relativeMinimizationConstraintWt = Double.parseDouble(props.get("relativeMinimizationConstraintWt"));
		if (props.containsKey("relativeMomentConstraintWt"))
			relativeMomentConstraintWt = Double.parseDouble(props.get("relativeMomentConstraintWt"));
		if (props.containsKey("minimumRuptureRateFraction"))
			minimumRuptureRateFraction = Double.parseDouble(props.get("minimumRuptureRateFraction"));
		if (props.containsKey("ucerf3MFDs"))
			ucerf3MFDs = Boolean.parseBoolean(props.get("ucerf3MFDs"));
		if (props.containsKey("weightSlipRates"))
			weightSlipRates = Boolean.parseBoolean(props.get("weightSlipRates"));
	}

	public InversionModels getInvModel() {
		return invModel;
	}

	public LogicTreeBranch getBranch() {
		return branch;
	}

	public boolean isUcerf3MFDs() {
		return ucerf3MFDs;
	}

	public double getMFDTransitionMag() {
		return MFDTransitionMag;
	}

	public boolean isWeightSlipRates() {
		return weightSlipRates;
	}

	public double getRelativePaleoRateWt() {
		return relativePaleoRateWt;
	}

	public double getRelativeMagnitudeEqualityConstraintWt() {
		return relativeMagnitudeEqualityConstraintWt;
	}

	public double getRelativeMagnitudeInequalityConstraintWt() {
		return relativeMagnitudeInequalityConstraintWt;
	}

	public double getRelativeRupRateConstraintWt() {
		return relativeRupRateConstraintWt;
	}

	public double getRelativeParticipationSmoothnessConstraintWt() {
		return relativeParticipationSmoothnessConstraintWt;
	}

	public double getRelativeMinimizationConstraintWt() {
		return relativeMinimizationConstraintWt;
	}

	public double getRelativeMomentConstraintWt() {
		return relativeMomentConstraintWt;
	}

	public double getMinimumRuptureRateFraction() {
		return minimumRuptureRateFraction;
	}
	
	public InversionMFDs getInversionMFDs() {
		return inversionMFDs;
	}
	
	/**
	 * This compares the MFDs in the given MFD constraints with the MFDs 
	 * implied by the Fault System Solution
	 * @param mfdConstraints
	 */
	public void plotMFDs() {
		UCERF2_MFD_ConstraintFetcher ucerf2Fetch = new UCERF2_MFD_ConstraintFetcher();
		
		// Statewide
		GraphiWindowAPI_Impl gw = getMFDPlotWindow(inversionMFDs.getTotalTargetGR(), inversionMFDs.getTargetOnFaultSupraSeisMFD(),
				RELM_RegionUtils.getGriddedRegionInstance(), ucerf2Fetch);
		gw.getGraphWindow().setVisible(true);
		
		gw = getMFDPlotWindow(inversionMFDs.getTotalTargetGR_NoCal(), inversionMFDs.noCalTargetSupraMFD,
				RELM_RegionUtils.getNoCalGriddedRegionInstance(), ucerf2Fetch);
		gw.getGraphWindow().setVisible(true);
		
		gw = getMFDPlotWindow(inversionMFDs.getTotalTargetGR_SoCal(), inversionMFDs.soCalTargetSupraMFD,
				RELM_RegionUtils.getSoCalGriddedRegionInstance(), ucerf2Fetch);
		gw.getGraphWindow().setVisible(true);
	}
	
	private boolean isStatewideDM() {
		return getDeformationModel() != DeformationModels.UCERF2_BAYAREA && getDeformationModel() != DeformationModels.UCERF2_NCAL;
	}
	
	private static IncrementalMagFreqDist newSameRange(IncrementalMagFreqDist other) {
		return new IncrementalMagFreqDist(other.getMinX(), other.getMaxX(), other.getNum());
	}
	
	public GraphiWindowAPI_Impl getMFDPlotWindow(IncrementalMagFreqDist totalMFD, IncrementalMagFreqDist targetMFD, Region region,
			UCERF2_MFD_ConstraintFetcher ucerf2Fetch) {
		
		PlotSpec spec = getMFDPlots(totalMFD, targetMFD, region, ucerf2Fetch);
		
		GraphiWindowAPI_Impl gw = new GraphiWindowAPI_Impl(spec.getFuncs(), spec.getTitle(), spec.getChars(), true);
		
		gw.setTickLabelFontSize(14);
		gw.setAxisLabelFontSize(16);
		gw.setPlotLabelFontSize(18);
		gw.setX_AxisLabel(spec.getxAxisLabel());
		gw.setY_AxisLabel(spec.getyAxisLabel());
		gw.setYLog(true);
		gw.setY_AxisRange(1e-6, 1.0);
		
		gw.getGraphWindow().setPlottingOrder(DatasetRenderingOrder.FORWARD);
		
		return gw;
	}
	
	public HeadlessGraphPanel getHeadlessMFDPlot(IncrementalMagFreqDist totalMFD, IncrementalMagFreqDist targetMFD, Region region,
			UCERF2_MFD_ConstraintFetcher ucerf2Fetch) {
		PlotSpec spec = getMFDPlots(totalMFD, targetMFD, region, ucerf2Fetch);
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		gp.setTickLabelFontSize(14);
		gp.setAxisLabelFontSize(16);
		gp.setPlotLabelFontSize(18);
		gp.setYLog(true);
		gp.setRenderingOrder(DatasetRenderingOrder.FORWARD);
		double minX = totalMFD.getMinX();
		if (minX < 5)
			minX = 5;
		gp.setUserBounds(minX, totalMFD.getMaxX(),
				1e-6, 1.0);
		gp.drawGraphPanel(spec.getxAxisLabel(), spec.getyAxisLabel(), spec.getFuncs(), spec.getChars(), true, spec.getTitle());
		
		return gp;
	}
	
	private PlotSpec getMFDPlots(IncrementalMagFreqDist totalMFD, IncrementalMagFreqDist targetMFD, Region region,
			UCERF2_MFD_ConstraintFetcher ucerf2Fetch) {
		
		ArrayList<DiscretizedFunc> funcs = new ArrayList<DiscretizedFunc>();
		ArrayList<PlotCurveCharacterstics> chars = new ArrayList<PlotCurveCharacterstics>();
		
		// Solution TODO: region issues for statewide/mendocino
		boolean statewide = region.getName().startsWith("RELM_TESTING");
		
		IncrementalMagFreqDist solMFD;
//		if (statewide)
//			solMFD = calcNucleationMFD_forRegion(null, // null since we want everything
//					totalMFD.getMinX(), 9.05, 0.1, true);
//		else
			solMFD = calcNucleationMFD_forRegion(region,
					totalMFD.getMinX(), 9.05, 0.1, true);
		solMFD.setName("Solution MFD");
		solMFD.setInfo("Inversion Solution MFD");
		funcs.add(solMFD);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2, Color.BLUE));
		
		// Overall Target
		totalMFD.setName("Total Target MFD");
		funcs.add(totalMFD);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2, Color.BLACK));
		
		// Inversion Target
		targetMFD.setName("Inversion Modified Target MFD");
		funcs.add(targetMFD);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2, Color.CYAN));
		
		// TODO Kevin add dashed line back in?
//		if (mfdConstraintModifier != 0 && mfdConstraintModifier != 1) {
//			// This is the overall target before it was multiplied
//			IncrementalMagFreqDist rolledBack = newSameRange(totalMFD);
//			for (int i=0; i<rolledBack.getNum(); i++) {
//				rolledBack.set(i, totalMFD.getY(i) / mfdConstraintModifier);
//			}
//			rolledBack.setName("Unmodified Original Target MFD");
//			rolledBack.setInfo("Total Target MFD without the mfdConstraintModifier of "+mfdConstraintModifier+" applied");
//			funcs.add(rolledBack);
//			chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 1, Color.BLACK));
//		}
		
		// Implied Off Fault TODO: use off fault from InversionMFDs - need methods for so/no cal
		IncrementalMagFreqDist solOffFaultMFD = null;
		// this could be cleaner :-/
		if (statewide) {
			solOffFaultMFD = inversionMFDs.getTotalSubSeismoOnPlusTrulyOffFaultMFD();
			solOffFaultMFD.setName("Implied Off-fault MFD for Solution");
			funcs.add(solOffFaultMFD);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2, Color.GRAY));
		}
//		} else
//			solOffFaultMFD = getImpliedOffFaultMFD(totalMFD, solMFD);
		
		// UCERF2 comparisons
		ucerf2Fetch.setRegion(region);
//		IncrementalMagFreqDist ucerf2_OnFaultTargetMFD = ucerf2Fetch.getTargetMinusBackgroundMFD();
//		ucerf2_OnFaultTargetMFD.setTolerance(0.1); 
//		ucerf2_OnFaultTargetMFD.setName("UCERF2 Target minus background+aftershocks");
//		ucerf2_OnFaultTargetMFD.setInfo(region.getName());
		IncrementalMagFreqDist ucerf2_OffFaultMFD = ucerf2Fetch.getBackgroundSeisMFD();
		ucerf2_OffFaultMFD.setName("UCERF2 Background Seismicity MFD"); 
//		funcs.add(ucerf2_OnFaultTargetMFD);
//		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1, Color.GREEN));
		funcs.add(0, ucerf2_OffFaultMFD);
		chars.add(0, new PlotCurveCharacterstics(PlotLineType.SOLID, 1, Color.MAGENTA));
		
		if (solOffFaultMFD != null) {
			// total sum
			SummedMagFreqDist totalModelMFD = new SummedMagFreqDist(solMFD.getMinX(), solMFD.getMaxX(), solMFD.getNum());
//			System.out.println(solMFD.getMinX()+"\t"+solMFD.getMaxX()+"\t"+solMFD.getNum());
//			System.out.println(solOffFaultMFD.getMinX()+"\t"+solOffFaultMFD.getMaxX()+"\t"+solOffFaultMFD.getNum());
			totalModelMFD.addIncrementalMagFreqDist(solMFD);
			totalModelMFD.addIncrementalMagFreqDist(resizeMFD(solOffFaultMFD, solMFD));
			totalModelMFD.setName("Total Model Solution MFD");
			funcs.add(totalModelMFD);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2, Color.RED));
		}
		
		String plotTitle = "Magnitude Histogram for Final Rates";
		if (region.getName() != null && !region.getName().isEmpty())
			plotTitle += " ("+region.getName()+")";
		
		return new PlotSpec(funcs, chars, plotTitle, "Magnitude", "Frequency (per bin)");
	}
	
	private static IncrementalMagFreqDist resizeMFD(IncrementalMagFreqDist smaller, IncrementalMagFreqDist target) {
		if (smaller.getMinX() == target.getMinX() && smaller.getMaxX() == target.getMaxX())
			return smaller;
		IncrementalMagFreqDist newSmall = newSameRange(target);
		for (int i=0; i<newSmall.getNum(); i++) {
			double x = newSmall.getX(i);
			double y;
			try {
				y = smaller.getY(x);
			} catch (Exception e) {
				y = 0d;
			}
			newSmall.set(i, y);
		}
		return newSmall;
	}
	
	/**
	 * This calculates the MFD for the solution and returns the total RELM testing region target minus the solution.
	 * TODO rename to getImpliedTotalGriddedSeisMFD
	 * @return
	 */
	public IncrementalMagFreqDist getImpliedOffFaultStatewideMFD() {
		IncrementalMagFreqDist mfd = getImpliedOffFaultMFD(inversionMFDs.getTotalTargetGR(), RELM_RegionUtils.getGriddedRegionInstance());
		
//		if (invModel == InversionModels.GR_CONSTRAINED || invModel == InversionModels.GR_UNCONSTRAINED) // TODO ?
//			mfd.setValuesAboveMomentRateToZero(getTotalOffFaultSeisMomentRate());
		return mfd;
	}
	
	/**
	 * This calculates the MFD for the solution and returns the target minus the solution.
	 * TODO rename to getImpliedTotalGriddedSeisMFD
	 * @param totalMFD
	 * @return
	 */
	public IncrementalMagFreqDist getImpliedOffFaultMFD(GutenbergRichterMagFreqDist totalMFD, Region region) {
		IncrementalMagFreqDist magHist = calcNucleationMFD_forRegion(region, totalMFD.getMinX(),
				totalMFD.getMaxX(), totalMFD.getNum(), false);
		return getImpliedOffFaultMFD(totalMFD, magHist);
	}
		
	/**
	 * This calculates the MFD for the solution and returns the target minus the solution.
	 * TODO rename to getImpliedTotalGriddedSeisMFD
	 * @param target
	 * @return
	 */
	private IncrementalMagFreqDist getImpliedOffFaultMFD(IncrementalMagFreqDist totalMFD, IncrementalMagFreqDist magHist) {
		return getImpliedOffFaultMFD(totalMFD, magHist, branch.getValue(MaxMagOffFault.class).getMaxMagOffFault());
	}
	
	/**
	 * This simply returns the given target MFD minus the given MFD
	 * TODO rename to getImpliedTotalGriddedSeisMFD
	 * @param magHist
	 * @param target
	 * @return
	 */
	public static IncrementalMagFreqDist getImpliedOffFaultMFD(IncrementalMagFreqDist target,
			IncrementalMagFreqDist magHist, double maxOffFaultMag) {
		IncrementalMagFreqDist offFaultMFD = newSameRange(target);
		offFaultMFD.setTolerance(0.2);
		for (double m=offFaultMFD.getMinX(); m<=offFaultMFD.getMaxX()&&m<=maxOffFaultMag; m+=offFaultMFD.getDelta()) {
			double tVal = target.getClosestY(m);
			double myVal = magHist.getClosestY(m);
//			System.out.println("implied off fault: "+m+": "+tVal+" - "+myVal+" = "+(tVal - myVal));
			offFaultMFD.set(m, tVal - myVal);
		}
		return offFaultMFD;
	}
	
	
	/**
	 * This computes the subseismogenic MFD for each sections using the final (post-inversion) slip rates
	 * assuming the section nucleates a perfect GR (after moment balancing, values above and equal to
	 * getMinMagForSection(s) are set to zero).
	 * @return
	 */
	public ArrayList<GutenbergRichterMagFreqDist> getImpliedSubSeisGR_MFD_List() {
		
		double minMag = InversionMFDs.MIN_MAG;
		double deltaMag = InversionMFDs.DELTA_MAG;
		int numMag = InversionMFDs.NUM_MAG;
		ArrayList<GutenbergRichterMagFreqDist> grNuclMFD_List = new ArrayList<GutenbergRichterMagFreqDist>();
		GutenbergRichterMagFreqDist tempGR = new GutenbergRichterMagFreqDist(minMag, numMag, deltaMag);
		for(int s=0; s<this.getNumSections(); s++) {
			
			double area = getAreaForSection(s); // SI units
			double slipRate = calcSlipRateForSect(s); // SI units
			double newMoRate = FaultMomentCalc.getMoment(area, slipRate);
			if(Double.isNaN(newMoRate)) newMoRate = 0;
			int mMaxIndex = tempGR.getClosestXIndex(getMaxMagForSection(s));
			double mMax = tempGR.getX(mMaxIndex);
			GutenbergRichterMagFreqDist gr = new GutenbergRichterMagFreqDist(minMag, numMag, deltaMag, minMag, mMax, newMoRate, 1.0);
			double minSeismoMag = getMinMagForSection(s);
			if(Double.isNaN(minSeismoMag))
				gr.scaleToCumRate(0, 0d);
			else {
				double closestMag = gr.getX(gr.getClosestXIndex(minSeismoMag));
				gr.zeroAtAndAboveMag(closestMag);
			}
			grNuclMFD_List.add(gr);
		}
		return grNuclMFD_List;
	}
	
	
	public static void main(String args[]) throws IOException, DocumentException {
//		SimpleFaultSystemSolution simple = SimpleFaultSystemSolution.fromFile(
//				new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/InversionSolutions/" +
//						"FM3_1_GLpABM_MaEllB_DsrTap_DrEllB_Char_VarAseis0.2_VarOffAseis0.5_VarMFDMod1_VarNone_sol.zip"));
		SimpleFaultSystemSolution simple = SimpleFaultSystemSolution.fromFile(new File(
						"/tmp/ucerf2_fm2_compare.zip"));
//		simple.plotMFDs(Lists.newArrayList(OLD_UCERF3_MFD_ConstraintFetcher.getTargetMFDConstraint(TimeAndRegion.ALL_CA_1850)));
		
		
		InversionFaultSystemSolution inv = new InversionFaultSystemSolution(simple);
		inv.plotMFDs();
		
//		CommandLineInversionRunner.writeMFDPlots(inv, new File("/tmp"), "test_plots");
	}

}
