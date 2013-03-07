package scratch.UCERF3.inversion;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import scratch.UCERF3.griddedSeismicity.GridSourceProvider;
import scratch.UCERF3.griddedSeismicity.UCERF3_GridSourceGenerator;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.logicTree.LogicTreeBranchNode;
import scratch.UCERF3.utils.MFD_InversionConstraint;
import scratch.UCERF3.utils.OLD_UCERF3_MFD_ConstraintFetcher;
import scratch.UCERF3.utils.SectionMFD_constraint;
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
public class InversionFaultSystemSolution extends SimpleFaultSystemSolution implements InversionFaultSystemSolutionInterface {
	
	private InversionModels invModel;
	private LogicTreeBranch branch;
	
	private double MFDTransitionMag = Double.NaN;
	private double slipRateConstraintWt = Double.NaN;
	private boolean weightSlipRates = true;
	private double paleoRateConstraintWt = Double.NaN;
	private double paleoSlipConstraintWt = Double.NaN;
	private double magnitudeEqualityConstraintWt = Double.NaN;
	private double magnitudeInequalityConstraintWt = Double.NaN;
	private double rupRateConstraintWt = Double.NaN;
	private double participationSmoothnessConstraintWt = Double.NaN;
	private double participationConstraintMagBinSize = Double.NaN;
	private double nucleationMFDConstraintWt = Double.NaN;
	private double mfdSmoothnessConstraintWt = Double.NaN;
	private double mfdSmoothnessConstraintWtForPaleoParents = Double.NaN;
	private double minimizationConstraintWt = Double.NaN;
	private double momentConstraintWt = Double.NaN;
	private double parkfieldConstraintWt = Double.NaN;
	private double smoothnessWt = Double.NaN;
	private double eventRateSmoothnessWt = Double.NaN;
	private double minimumRuptureRateFraction = Double.NaN;
	
	private InversionMFDs inversionMFDs;
	
	double[] minMagForSectArray;
	boolean[] isRupBelowMinMagsForSects;
	
	private Map<String, Double> energies;
	private Map<String, Double> misfits;

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
			ArrayList<String> saMetadata = getMetedataSection(infoLines, "Simulated Annealing Metadata");
			if (saMetadata == null)
				saMetadata = Lists.newArrayList();
			Map<String, String> saProps = loadProperties(saMetadata);
			branch = loadBranch(branchProps);
			invModel = branch.getValue(InversionModels.class);
			loadInvParams(invProps);
			loadEnergies(saProps);
			
			double totalRegionRateMgt5 = branch.getValue(TotalMag5Rate.class).getRateMag5();
			double mMaxOffFault = branch.getValue(MaxMagOffFault.class).getMaxMagOffFault();
			boolean applyImpliedCouplingCoeff = branch.getValue(MomentRateFixes.class).isApplyCC();
			SpatialSeisPDF spatialSeisPDF = branch.getValue(SpatialSeisPDF.class);
			inversionMFDs = new InversionMFDs(solution, this, totalRegionRateMgt5, mMaxOffFault, applyImpliedCouplingCoeff, spatialSeisPDF, invModel);
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
			if (key.startsWith("relative")) {
				key = key.substring(8);
				if (key.startsWith("MFD"))
					key = "mfd"+key.substring(3);
				else if (Character.isUpperCase(key.charAt(0)))
					key = new String(key.charAt(0)+"").toLowerCase()+key.substring(1);
			}
			String value = line.substring(ind+1).trim();
			// this is a special case for a bug Morgan had where she didn't add a new line to the metadata field
			if (value.contains("weightSlipRates:")) {
				int badInd = value.indexOf("weightSlipRates:");
				String weightSlipsStr = value.substring(badInd);
				String weightVal = weightSlipsStr.substring(weightSlipsStr.indexOf(":")+1).trim();
				props.put("weightSlipRates", weightVal);
				value = value.substring(0, badInd);
			}
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
			if (valueName.equals("RATE_10p6"))
				valueName = "RATE_10p0";
			LogicTreeBranchNode<?> value = null;
			for (LogicTreeBranchNode<?> testValue : clazz.getEnumConstants()) {
				if (testValue.name().equals(valueName)) {
					value = testValue;
					break;
				}
			}
			Preconditions.checkNotNull(value, "Couldn't find matching constant for logic tree value "+key+" (node="
					+ClassUtils.getClassNameWithoutPackage(clazz)+")"+" (val="+props.get(key)+")");
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
		if (props.containsKey("weightSlipRates"))
			weightSlipRates = Boolean.parseBoolean(props.get("weightSlipRates"));
		if (props.containsKey("slipRateConstraintWt"))
			slipRateConstraintWt = Double.parseDouble(props.get("slipRateConstraintWt"));
		else
			slipRateConstraintWt = 1d;
		if (props.containsKey("paleoRateConstraintWt"))
			paleoRateConstraintWt = Double.parseDouble(props.get("paleoRateConstraintWt"));
		else if (props.containsKey("paleoRateWt"))
			paleoRateConstraintWt = Double.parseDouble(props.get("paleoRateWt"));
		if (props.containsKey("paleoSlipConstraintWt"))
			paleoSlipConstraintWt = Double.parseDouble(props.get("paleoSlipConstraintWt"));
		else if (props.containsKey("paleoSlipWt"))
			paleoSlipConstraintWt = Double.parseDouble(props.get("paleoSlipWt"));
		if (props.containsKey("magnitudeEqualityConstraintWt"))
			magnitudeEqualityConstraintWt = Double.parseDouble(props.get("magnitudeEqualityConstraintWt"));
		if (props.containsKey("magnitudeInequalityConstraintWt"))
			magnitudeInequalityConstraintWt = Double.parseDouble(props.get("magnitudeInequalityConstraintWt"));
		if (props.containsKey("rupRateConstraintWt"))
			rupRateConstraintWt = Double.parseDouble(props.get("rupRateConstraintWt"));
		if (props.containsKey("participationSmoothnessConstraintWt"))
			participationSmoothnessConstraintWt = Double.parseDouble(props.get("participationSmoothnessConstraintWt"));
		if (props.containsKey("participationConstraintMagBinSize"))
			participationConstraintMagBinSize = Double.parseDouble(props.get("participationConstraintMagBinSize"));
		if (props.containsKey("nucleationMFDConstraintWt"))
			nucleationMFDConstraintWt = Double.parseDouble(props.get("nucleationMFDConstraintWt"));
		if (props.containsKey("mfdSmoothnessConstraintWt"))
			mfdSmoothnessConstraintWt = Double.parseDouble(props.get("mfdSmoothnessConstraintWt"));
		if (props.containsKey("mfdSmoothnessConstraintWtForPaleoParents"))
			mfdSmoothnessConstraintWtForPaleoParents = Double.parseDouble(props.get("mfdSmoothnessConstraintWtForPaleoParents"));
		if (props.containsKey("minimizationConstraintWt"))
			minimizationConstraintWt = Double.parseDouble(props.get("minimizationConstraintWt"));
		if (props.containsKey("momentConstraintWt"))
			momentConstraintWt = Double.parseDouble(props.get("momentConstraintWt"));
		if (props.containsKey("parkfieldConstraintWt"))
			parkfieldConstraintWt = Double.parseDouble(props.get("parkfieldConstraintWt"));
		if (props.containsKey("smoothnessWt"))
			smoothnessWt = Double.parseDouble(props.get("smoothnessWt"));
		if (props.containsKey("eventRateSmoothnessWt"))
			eventRateSmoothnessWt = Double.parseDouble(props.get("eventRateSmoothnessWt"));
		if (props.containsKey("minimumRuptureRateFraction"))
			minimumRuptureRateFraction = Double.parseDouble(props.get("minimumRuptureRateFraction"));
	}
	
	private void loadEnergies(Map<String, String> props) {
		energies = Maps.newHashMap();
		
		for (String key : props.keySet()) {
			if (!key.contains("energy"))
				continue;
			if (key.contains("Best") || key.contains("breakdown"))
				continue;
			double val = Double.parseDouble(props.get(key));
			key = key.trim();
			energies.put(key, val);
		}
	}
	
	public Map<String, Double> getEnergies() {
		return energies;
	}
	
	/**
	 * This returns the energies scaled by their weights, should be a fair comparison amung runs even with different weights.
	 * @return
	 */
	public synchronized Map<String, Double> getMisfits() {
		if (misfits == null) {
			misfits = Maps.newHashMap();
			
			for (String energyStr : energies.keySet()) {
				double eVal = energies.get(energyStr);
				double wt;
				energyStr = energyStr.substring(0, energyStr.indexOf("energy")).trim();
				if (energyStr.equals("Slip Rate"))
					wt = slipRateConstraintWt;
				else if (energyStr.equals("Paleo Event Rates"))
					wt = paleoRateConstraintWt;
				else if (energyStr.equals("Paleo Slips"))
					wt = paleoSlipConstraintWt;
				else if (energyStr.equals("Rupture Rates"))
					wt = rupRateConstraintWt;
				else if (energyStr.equals("Minimization"))
					wt = minimizationConstraintWt;
				else if (energyStr.equals("MFD Equality"))
					wt = magnitudeEqualityConstraintWt;
				else if (energyStr.equals("MFD Participation"))
					wt = participationSmoothnessConstraintWt;
				else if (energyStr.equals("MFD Nucleation"))
					wt = nucleationMFDConstraintWt;
				else if (energyStr.equals("MFD Smoothness")) {
					if (mfdSmoothnessConstraintWt > 0 && mfdSmoothnessConstraintWtForPaleoParents > 0)
						wt = Double.NaN; // impossible to make a fair comparison here when both weights are mixed
					else if (mfdSmoothnessConstraintWt > 0)
						wt = mfdSmoothnessConstraintWt;
					else if (mfdSmoothnessConstraintWtForPaleoParents > 0)
						wt = mfdSmoothnessConstraintWtForPaleoParents;
					else
						wt = 0d;
				} else if (energyStr.equals("Moment"))
					wt = momentConstraintWt;
				else if (energyStr.equals("Parkfield"))
					wt = parkfieldConstraintWt;
				else if (energyStr.equals("Event-Rate Smoothness"))
					wt = eventRateSmoothnessWt;
				else
					throw new IllegalStateException("Unknown Energy Type: "+energyStr);
				double misfit = eVal / (wt*wt);
				System.out.println(energyStr+": "+eVal+" / ("+wt+")^2 = "+misfit);
				misfits.put(energyStr, misfit);
			}
		}
		
		return misfits;
	}

	public InversionModels getInvModel() {
		return invModel;
	}

	public LogicTreeBranch getBranch() {
		return branch;
	}

	public double getMFDTransitionMag() {
		return MFDTransitionMag;
	}

	public boolean isWeightSlipRates() {
		return weightSlipRates;
	}

	public double getPaleoRateConstraintWt() {
		return paleoRateConstraintWt;
	}

	public double getPaleoSlipConstraintWt() {
		return paleoSlipConstraintWt;
	}

	public double getMagnitudeEqualityConstraintWt() {
		return magnitudeEqualityConstraintWt;
	}

	public double getMagnitudeInequalityConstraintWt() {
		return magnitudeInequalityConstraintWt;
	}

	public double getRupRateConstraintWt() {
		return rupRateConstraintWt;
	}

	public double getParticipationSmoothnessConstraintWt() {
		return participationSmoothnessConstraintWt;
	}
	
	public double getParticipationConstraintMagBinSize() {
		return participationConstraintMagBinSize;
	}

	public double getMinimumRuptureRateFraction() {
		return minimumRuptureRateFraction;
	}
	
	public InversionMFDs getInversionMFDs() {
		return inversionMFDs;
	}
	
	public double getSlipRateConstraintWt() {
		return slipRateConstraintWt;
	}
	
	public double getNucleationMFDConstraintWt() {
		return nucleationMFDConstraintWt;
	}

	public double getMfdSmoothnessConstraintWt() {
		return mfdSmoothnessConstraintWt;
	}

	public double getMfdSmoothnessConstraintWtForPaleoParents() {
		return mfdSmoothnessConstraintWtForPaleoParents;
	}

	public double getMinimizationConstraintWt() {
		return minimizationConstraintWt;
	}

	public double getMomentConstraintWt() {
		return momentConstraintWt;
	}

	public double getParkfieldConstraintWt() {
		return parkfieldConstraintWt;
	}

	public double getSmoothnessWt() {
		return smoothnessWt;
	}

	public double getEventRateSmoothnessWt() {
		return eventRateSmoothnessWt;
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
	
	// TODO this should be put in a more general location so others can use (MFD_Utils class?)
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
		return getHeadlessMFDPlot(spec, totalMFD);
	}
	
	public HeadlessGraphPanel getHeadlessMFDPlot(PlotSpec spec, IncrementalMagFreqDist totalMFD) {
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		CommandLineInversionRunner.setFontSizes(gp);
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
	
	public PlotSpec getMFDPlots(IncrementalMagFreqDist totalMFD, IncrementalMagFreqDist targetMFD, Region region,
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
			solOffFaultMFD = inversionMFDs.getTotalTargetSubSeismoOnPlusTrulyOffFaultMFD();
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
		
		String plotTitle = region.getName();
		if (plotTitle == null || plotTitle.isEmpty())
			plotTitle = "Unnamed Region";
		
		return new PlotSpec(funcs, chars, plotTitle, "Magnitude", "Incremental Rate (per yr)");
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
	 * This returns the list of final sub-seismo MFDs for each fault section (e.g., for use in an ERF).  
	 * What's returned is getInversionMFDs().getTargetSubSeismoOnFaultMFD_List() unless
	 * it's a noFix GR branch, in which case it returns getImpliedSubSeisGR_MFD_List() to
	 * account for any inversion imposed slip-rate changes.
	 * @return
	 */
	public List<GutenbergRichterMagFreqDist> getFinalSubSeismoOnFaultMFD_List() {
		List<GutenbergRichterMagFreqDist> subSeisMFD_list;
		// make sure we deal with special case for GR moFix branch
		boolean noFix = branch.getValue(MomentRateFixes.class) == MomentRateFixes.NONE;
		boolean gr = branch.getValue(InversionModels.class).isGR();
		// get post-inversion MFDs
		if (noFix && gr) {
			subSeisMFD_list = getImpliedSubSeisGR_MFD_List();
		} else {
			subSeisMFD_list = getInversionMFDs().getTargetSubSeismoOnFaultMFD_List();
		}
		return subSeisMFD_list;
	}
	
	public SummedMagFreqDist getFinalSubSeismoOnFaultMFDForParent(int parentSectionID) {
		
		SummedMagFreqDist mfd = new SummedMagFreqDist(InversionMFDs.MIN_MAG, InversionMFDs.NUM_MAG, InversionMFDs.DELTA_MAG);
		
		List<GutenbergRichterMagFreqDist> subSeismoMFDs = getFinalSubSeismoOnFaultMFD_List();
		
		for (int sectIndex=0; sectIndex<getNumSections(); sectIndex++) {
			if (getFaultSectionData(sectIndex).getParentSectionId() != parentSectionID)
				continue;
			mfd.addIncrementalMagFreqDist(subSeismoMFDs.get(sectIndex));
		}
		
		return mfd;
	}

	
	/**
	 * This returns the final truly off-fault MFD (not including subseismo on-fault ruptures).
	 * This currently returns inversionMFDs.getTargetTrulyOffFaultMFD(), but could return 
	 * getImpliedTrulyOffFaultMFD() if the latter is deemed more appropriate.
	 * 
	 * @return
	 */
	public IncrementalMagFreqDist getFinalTrulyOffFaultMFD() {
		return inversionMFDs.getTargetTrulyOffFaultMFD();
	}

	
	/**
	 * This returns the final total gridded seismicity MFD (subseismo on-fault plus truly off fault).
	 * This currently returns inversionMFDs.getTotalGriddedSeisMFD() unless it's a noFix GR branch
	 * (in which case the subseismo parts come from getImpliedSubSeisGR_MFD_List()).
	 * @return
	 */
	public IncrementalMagFreqDist getFinalTotalGriddedSeisMFD() {
		
		// make sure we deal with special case for GR noFix branch
		boolean noFix = branch.getValue(MomentRateFixes.class) == MomentRateFixes.NONE;
		boolean gr = branch.getValue(InversionModels.class).isGR();
		if (noFix && gr) { // get post-inversion MFDs
			SummedMagFreqDist totGridSeisMFD = new SummedMagFreqDist(InversionMFDs.MIN_MAG, InversionMFDs.NUM_MAG, InversionMFDs.DELTA_MAG);
			totGridSeisMFD.addIncrementalMagFreqDist(getFinalTrulyOffFaultMFD());
			for(GutenbergRichterMagFreqDist subSeisGR : getImpliedSubSeisGR_MFD_List())
				totGridSeisMFD.addIncrementalMagFreqDist(subSeisGR);
			return totGridSeisMFD;

		} else {
			return getInversionMFDs().getTotalGriddedSeisMFD();
		}
 	}
	
	/**
	 * This calculates the difference between the total target MFD and the FSS nucleation MFD inside the RELM region,
	 * which is the total gridded seismicity MFD implied by the final solution.  This differs from 
	 * getFinalTotalGriddedSeisMFD() to the extent the final FSS MFD differs from the original target, and that rupture
	 * outside the RELM region are filtered out here.
	 * @return
	 */
	public IncrementalMagFreqDist getImpliedTotalGriddedSeisMFD() {
		
		GutenbergRichterMagFreqDist regionTarget = inversionMFDs.getTotalTargetGR();
		
		IncrementalMagFreqDist nuclMFD = calcNucleationMFD_forRegion(RELM_RegionUtils.getGriddedRegionInstance(), regionTarget.getMinX(),
				regionTarget.getMaxX(), regionTarget.getNum(), false);

		double maxOffFaultMag = branch.getValue(MaxMagOffFault.class).getMaxMagOffFault();
		
		IncrementalMagFreqDist offFaultMFD = newSameRange(regionTarget);
		offFaultMFD.setTolerance(0.2);
		for (double m=offFaultMFD.getMinX(); m<=offFaultMFD.getMaxX()&&m<=maxOffFaultMag; m+=offFaultMFD.getDelta()) {
			double tVal = regionTarget.getClosestY(m);
			double myVal = nuclMFD.getClosestY(m);
//			System.out.println("implied off fault: "+m+": "+tVal+" - "+myVal+" = "+(tVal - myVal));
			offFaultMFD.set(m, tVal - myVal);
		}
		return offFaultMFD;
	}

	
	
	/**
	 * This computes the subseismogenic MFD for each section using the final (post-inversion) slip rates
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
//			double minSeismoMag = getMinMagForSection(s);
			double minSeismoMag = getUpperMagForSubseismoRuptures(s)+deltaMag/2;
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
	
	// Methods from InversionFaultSystemSolutionInterface:
	
	/**
	 * This returns the final minimum mag for a given fault section.
	 * See doc for computeMinSeismoMagForSections() for details.
	 * @param sectIndex
	 * @return
	 */
	public double getFinalMinMagForSection(int sectIndex) {
		if(minMagForSectArray == null) {
			minMagForSectArray = FaultSystemRupSetCalc.computeMinSeismoMagForSections(this,InversionFaultSystemRupSet.MIN_MAG_FOR_SEISMOGENIC_RUPS);
		}
		return minMagForSectArray[sectIndex];
	}
	
	
	/**
	 * This tells whether the given rup is below any of the final minimum magnitudes 
	 * of the sections utilized by the rup.  Actually, the test is really whether the
	 * mag falls below the lower bin edge implied by the section min mags; see doc for
	 * computeWhichRupsFallBelowSectionMinMags()).
	 * @param rupIndex
	 * @return
	 */
	public boolean isRuptureBelowSectMinMag(int rupIndex) {
		
		// see if it needs to be computed
		if(isRupBelowMinMagsForSects == null) {
			if(minMagForSectArray == null) {
				minMagForSectArray = FaultSystemRupSetCalc.computeMinSeismoMagForSections(this,InversionFaultSystemRupSet.MIN_MAG_FOR_SEISMOGENIC_RUPS);
			}
			isRupBelowMinMagsForSects = FaultSystemRupSetCalc.computeWhichRupsFallBelowSectionMinMags(this, minMagForSectArray);
		}
		
		return isRupBelowMinMagsForSects[rupIndex];

	}
	
	
	/**
	 * This returns the upper magnitude of sub-seismogenic ruptures
	 * (at the bin center).  This is the lower bin edge of the minimum
	 * seismogenic rupture minus half the MFD discretization.
	 * @param sectIndex
	 * @return
	 */
	public double getUpperMagForSubseismoRuptures(int sectIndex) {
		return SectionMFD_constraint.getLowerEdgeOfFirstBin(getFinalMinMagForSection(sectIndex)) - InversionMFDs.DELTA_MAG/2;
	}
	
	@Override
	public GridSourceProvider getGridSourceProvider() {
		return new UCERF3_GridSourceGenerator(this);
	}
	
	public static void main(String args[]) throws IOException, DocumentException {
//		SimpleFaultSystemSolution simple = SimpleFaultSystemSolution.fromFile(
//				new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/InversionSolutions/" +
//						"FM3_1_GLpABM_MaEllB_DsrTap_DrEllB_Char_VarAseis0.2_VarOffAseis0.5_VarMFDMod1_VarNone_sol.zip"));
//		SimpleFaultSystemSolution simple = SimpleFaultSystemSolution.fromFile(new File(
//						"/tmp/ucerf2_fm2_compare.zip"));
//		simple.plotMFDs(Lists.newArrayList(OLD_UCERF3_MFD_ConstraintFetcher.getTargetMFDConstraint(TimeAndRegion.ALL_CA_1850)));
		
		SimpleFaultSystemSolution simple = SimpleFaultSystemSolution.fromFile(new File(
				"/tmp/FM2_1_UC2ALL_ShConStrDrp_DsrTap_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_" +
				"SpatSeisU2_VarPaleo0.1_VarSectNuclMFDWt0.01_VarParkfield10000_sol.zip"));
		
		InversionFaultSystemSolution inv = new InversionFaultSystemSolution(simple);
		Map<String, Double> misfits = inv.getMisfits();
		for (String name : misfits.keySet()) {
			System.out.println(name+": "+misfits.get(name));
		}
//		inv.plotMFDs();
		
//		CommandLineInversionRunner.writeMFDPlots(inv, new File("/tmp"), "test_plots");
	}

}
