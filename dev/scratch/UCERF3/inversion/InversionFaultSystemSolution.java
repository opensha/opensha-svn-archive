package scratch.UCERF3.inversion;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentException;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.util.ClassUtils;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemSolution;
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
		// TODO add new metadata keys
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
	
	/**
	 * THIS NEEDS TO BE OBTAINED FROM AN InversionMFDs OBJECT (WHICH NEEDS TO BE REINSTANTIATED HERE)
	 * 
	 * This gets the original MFD constraints for the inversion run before modifications for off fault seismicity and
	 * fault minimum magnitudes
	 * @return
	 */
	public List<MFD_InversionConstraint> getOrigMFDConstraints() {
		// TODO Ned fill this in
		return null;
	}
	
	/**
	 * 
	 * THIS NEEDS TO BE OBTAINED FROM AN InversionMFDs OBJECT (WHICH NEEDS TO BE REINSTANTIATED HERE)
	 * 
	 * This gets the modified MFD constraints used to constrain the inversion after taking into account minimum magnitude
	 * and off fault seismicity
	 * @param origMFDConstraints
	 * @return
	 */
	public List<MFD_InversionConstraint> getModifiedTargetMFDConstraints(List<MFD_InversionConstraint> origMFDConstraints) {
//		ArrayList<MFD_InversionConstraint> clones = new ArrayList<MFD_InversionConstraint>();
//		for (MFD_InversionConstraint mfd : origMFDConstraints) {
//			clones.add(new MFD_InversionConstraint(mfd.getMagFreqDist().deepClone(), mfd.getRegion()));
//		}
//		return InversionConfiguration.getTargetMFDConstraints(clones, invModel,
//				this);
		
		return inversionMFDs.getMFD_ConstraintsForNoAndSoCal(); // TODO Ned, is this correct?
	}
	
	/**
	 * This gets the modified MFD constraints used to constrain the inversion after taking into account minimum magnitude
	 * and off fault seismicity
	 * @param origMFDConstraints
	 * @return
	 */
	public MFD_InversionConstraint getModifiedTargetMFDConstraint(MFD_InversionConstraint origMFDConstraint) {
		return getModifiedTargetMFDConstraints(Lists.newArrayList(origMFDConstraint)).get(0);
	}
	
	/**
	 * This compares the MFDs in the given MFD constraints with the MFDs 
	 * implied by the Fault System Solution		// TODO throw not yet implemented exception
	 * @param mfdConstraints
	 */
	public void plotMFDs() {
		UCERF2_MFD_ConstraintFetcher ucerf2Fetch = new UCERF2_MFD_ConstraintFetcher();
		
		List<MFD_InversionConstraint> origMFDConstraints = getPlotOriginalMFDConstraints(ucerf2Fetch);
		
		for (MFD_InversionConstraint constraint : origMFDConstraints) {
			GraphiWindowAPI_Impl gw = getMFDPlotWindow(constraint, ucerf2Fetch);
			gw.getGraphWindow().setVisible(true);
		}
	}
	
	private boolean isStatewideDM() {
		return getDeformationModel() != DeformationModels.UCERF2_BAYAREA && getDeformationModel() != DeformationModels.UCERF2_NCAL;
	}
	
	/**
	 * This returns the statewide MFD constraint (scaled if necessary by mfdConstraintModifier)
	 * @return
	 */
	public MFD_InversionConstraint getStatewideMFDConstraint() {
		return getStatewideMFDConstraint(null);
	}
	
	/**
	 * This returns the statewide MFD constraint (scaled if necessary by mfdConstraintModifier)
	 * @param ucerf2Fetch UCERF2 mfd constraint fetcher if you already have one instantiated
	 * @return
	 */
	public MFD_InversionConstraint getStatewideMFDConstraint(UCERF2_MFD_ConstraintFetcher ucerf2Fetch) {
		Preconditions.checkState(isStatewideDM(), "Can't get statewide MFD constraint for non statewide dm: "+getDeformationModel());
		
		// TODO Ned verify
		return null;
		
//		MFD_InversionConstraint allConst;
//		if (ucerf3MFDs)
//			allConst = OLD_UCERF3_MFD_ConstraintFetcher.getTargetMFDConstraint(TimeAndRegion.ALL_CA_1850);
//		else {
//			Region reg = new CaliforniaRegions.RELM_TESTING();
//			if (ucerf2Fetch == null)
//				ucerf2Fetch = new UCERF2_MFD_ConstraintFetcher();
//			ucerf2Fetch.setRegion(reg);
//			allConst = ucerf2Fetch.getTargetMFDConstraint();
//		}
//		
//		if (mfdConstraintModifier != 1 && mfdConstraintModifier > 0) {
//			IncrementalMagFreqDist magDist = allConst.getMagFreqDist();
//			for (double m=magDist.getMinX(); m<=magDist.getMaxX(); m+=magDist.getDelta()) {
//				double setVal = mfdConstraintModifier * magDist.getClosestY(m);
//				magDist.set(m, setVal);
//			}
//		}
//		
//		return allConst;
	}
	
	public List<MFD_InversionConstraint> getPlotOriginalMFDConstraints(UCERF2_MFD_ConstraintFetcher ucerf2Fetch) {
		List<MFD_InversionConstraint> origMFDConstraints = getOrigMFDConstraints();
		
		if (isStatewideDM() && origMFDConstraints.size() == 2) {
			origMFDConstraints.add(0, getStatewideMFDConstraint(ucerf2Fetch));
		}
		
		return origMFDConstraints;
	}
	
	private static IncrementalMagFreqDist newSameRange(IncrementalMagFreqDist other) {
		return new IncrementalMagFreqDist(other.getMinX(), other.getMaxX(), other.getNum());
	}
	
	private class PlotSpec {
		private ArrayList<DiscretizedFunc> funcs;
		private ArrayList<PlotCurveCharacterstics> chars;
		private String title, xAxisLabel, yAxisLabel;
		
		public PlotSpec(ArrayList<DiscretizedFunc> funcs,
				ArrayList<PlotCurveCharacterstics> chars, String title, String xAxisLabel, String yAxisLabel) {
			this.funcs = funcs;
			this.chars = chars;
			this.title = title;
			this.xAxisLabel = xAxisLabel;
			this.yAxisLabel = yAxisLabel;
		}
	}
	
	public GraphiWindowAPI_Impl getMFDPlotWindow(MFD_InversionConstraint totalTargetMFDConstraint,
			UCERF2_MFD_ConstraintFetcher ucerf2Fetch) {
		
		PlotSpec spec = getMFDPlots(totalTargetMFDConstraint, ucerf2Fetch);
		
		GraphiWindowAPI_Impl gw = new GraphiWindowAPI_Impl(spec.funcs, spec.title, spec.chars, true);
		
		gw.setX_AxisLabel(spec.xAxisLabel);
		gw.setY_AxisLabel(spec.yAxisLabel);
		gw.setYLog(true);
		gw.setY_AxisRange(1e-6, 1.0);
		
		gw.getGraphWindow().setPlottingOrder(DatasetRenderingOrder.FORWARD);
		
		return gw;
	}
	
	public HeadlessGraphPanel getHeadlessMFDPlot(MFD_InversionConstraint totalTargetMFDConstraint,
			UCERF2_MFD_ConstraintFetcher ucerf2Fetch) {
		PlotSpec spec = getMFDPlots(totalTargetMFDConstraint, ucerf2Fetch);
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		gp.setYLog(true);
		gp.setRenderingOrder(DatasetRenderingOrder.FORWARD);
		gp.setUserBounds(totalTargetMFDConstraint.getMagFreqDist().getMinX(), totalTargetMFDConstraint.getMagFreqDist().getMaxX(),
				1e-6, 1.0);
		gp.drawGraphPanel(spec.xAxisLabel, spec.yAxisLabel, spec.funcs, spec.chars, true, spec.title);
		
		return gp;
	}
	
	private PlotSpec getMFDPlots(MFD_InversionConstraint totalTargetMFDConstraint,
			UCERF2_MFD_ConstraintFetcher ucerf2Fetch) {
		Region region = totalTargetMFDConstraint.getRegion();
		
		ArrayList<DiscretizedFunc> funcs = new ArrayList<DiscretizedFunc>();
		ArrayList<PlotCurveCharacterstics> chars = new ArrayList<PlotCurveCharacterstics>();
		
		IncrementalMagFreqDist totalMFD = totalTargetMFDConstraint.getMagFreqDist();
		
		// Solution
		IncrementalMagFreqDist solMFD = calcNucleationMFD_forRegion(region,
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
		IncrementalMagFreqDist invTarget = getModifiedTargetMFDConstraint(totalTargetMFDConstraint).getMagFreqDist();
		invTarget.setName("Inversion Modified Target MFD");
		funcs.add(invTarget);
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
		
		// Implied Off Fault
		IncrementalMagFreqDist solOffFaultMFD;
		if (region.getName().contains("RELM") && region.getName().contains("TESTING"))
			solOffFaultMFD = getImpliedOffFaultStatewideMFD();
		else
			solOffFaultMFD = getImpliedOffFaultMFD(totalMFD, solMFD);
		solOffFaultMFD.setName("Implied Off-fault MFD for Solution");
		funcs.add(solOffFaultMFD);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2, Color.GRAY));
		
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
		funcs.add(ucerf2_OffFaultMFD);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1, Color.MAGENTA));
		
		String plotTitle = "Magnitude Histogram for Final Rates";
		if (region.getName() != null && !region.getName().isEmpty())
			plotTitle += " ("+region.getName()+")";
		
		return new PlotSpec(funcs, chars, plotTitle, "Magnitude", "Frequency (per bin)");
	}
	
	/**
	 * This calculates the MFD for the solution and returns the total RELM testing region target minus the solution.
	 * @return
	 */
	public IncrementalMagFreqDist getImpliedOffFaultStatewideMFD() {
		IncrementalMagFreqDist mfd = getImpliedOffFaultMFD(getStatewideMFDConstraint());
//		if (invModel == InversionModels.GR_CONSTRAINED || invModel == InversionModels.GR_UNCONSTRAINED) // TODO ?
//			mfd.setValuesAboveMomentRateToZero(getTotalOffFaultSeisMomentRate());
		return mfd;
	}
	
	/**
	 * This calculates the MFD for the solution and returns the target minus the solution.
	 * @param target
	 * @return
	 */
	public IncrementalMagFreqDist getImpliedOffFaultMFD(MFD_InversionConstraint target) {
		IncrementalMagFreqDist totalMFD = target.getMagFreqDist();
		IncrementalMagFreqDist magHist = calcNucleationMFD_forRegion(target.getRegion(), totalMFD.getMinX(),
				totalMFD.getMaxX(), totalMFD.getNum(), false);
		return getImpliedOffFaultMFD(totalMFD, magHist);
	}
		
	/**
	 * This calculates the MFD for the solution and returns the target minus the solution.
	 * @param target
	 * @return
	 */
	private IncrementalMagFreqDist getImpliedOffFaultMFD(IncrementalMagFreqDist totalMFD, IncrementalMagFreqDist magHist) {
		// TODO did I do this right?
		return getImpliedOffFaultMFD(totalMFD, magHist, branch.getValue(MaxMagOffFault.class).getMaxMagOffFault());
	}
	
	/**
	 * This simply returns the given target MFD minus the given MFD
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
	
	public static void main(String args[]) throws IOException, DocumentException {
//		SimpleFaultSystemSolution simple = SimpleFaultSystemSolution.fromFile(
//				new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/InversionSolutions/" +
//						"FM3_1_GLpABM_MaEllB_DsrTap_DrEllB_Char_VarAseis0.2_VarOffAseis0.5_VarMFDMod1_VarNone_sol.zip"));
		SimpleFaultSystemSolution simple = SimpleFaultSystemSolution.fromFile(new File(
						"/tmp/ucerf2_fm2_compare.zip"));
		simple.plotMFDs(Lists.newArrayList(OLD_UCERF3_MFD_ConstraintFetcher.getTargetMFDConstraint(TimeAndRegion.ALL_CA_1850)));
		
//		InversionFaultSystemSolution inv = new InversionFaultSystemSolution(simple);
//		inv.plotMFDs();
		
//		CommandLineInversionRunner.writeMFDPlots(inv, new File("/tmp"), "test_plots");
	}

}
