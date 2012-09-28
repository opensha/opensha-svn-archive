package scratch.UCERF3.utils.paleoRateConstraints;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentException;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.gui.infoTools.PlotSpec;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.utils.UCERF3_DataUtils;
import scratch.UCERF3.utils.aveSlip.AveSlipConstraint;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoFitPlotter.AveSlipFakePaleoConstraint;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PaleoFitPlotter {

	static class AveSlipFakePaleoConstraint extends PaleoRateConstraint {
		private AveSlipFakePaleoConstraint(AveSlipConstraint aveSlip, int sectIndex, double slipRate) {
			super(null, aveSlip.getSiteLocation(), sectIndex, slipRate/aveSlip.getWeightedMean(),
					slipRate/aveSlip.getLowerUncertaintyBound(), slipRate/aveSlip.getUpperUncertaintyBound());
			
		}
	}

	public static PlotSpec getSegRateComparisonSpec(
				List<PaleoRateConstraint> paleoRateConstraint,
				List<AveSlipConstraint> aveSlipConstraints,
				FaultSystemSolution sol) {
			Preconditions.checkState(paleoRateConstraint.size() > 0, "Must have at least one rate constraint");
			Preconditions.checkNotNull(sol, "Solution cannot me null!");
			
			boolean plotAveSlipBars = true;
			
			List<FaultSectionPrefData> datas = sol.getFaultSectionDataList();
			
			ArrayList<DiscretizedFunc> funcs = new ArrayList<DiscretizedFunc>();
			Map<Integer, DiscretizedFunc> funcParentsMap = Maps.newHashMap();
			ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
			
			Color paleoProbColor = Color.RED;
			
			ArbitrarilyDiscretizedFunc paleoRateMean = new ArbitrarilyDiscretizedFunc();
			paleoRateMean.setName("Paleo Rate Constraint: Mean");
			funcs.add(paleoRateMean);
			plotChars.add(new PlotCurveCharacterstics(PlotSymbol.CIRCLE, 5f, paleoProbColor));
			ArbitrarilyDiscretizedFunc paleoRateUpper = new ArbitrarilyDiscretizedFunc();
			paleoRateUpper.setName("Paleo Rate Constraint: Upper 95% Confidence");
			funcs.add(paleoRateUpper);
			plotChars.add(new PlotCurveCharacterstics(PlotSymbol.DASH, 5f, paleoProbColor));
			ArbitrarilyDiscretizedFunc paleoRateLower = new ArbitrarilyDiscretizedFunc();
			paleoRateLower.setName("Paleo Rate Constraint: Lower 95% Confidence");
			funcs.add(paleoRateLower);
			plotChars.add(new PlotCurveCharacterstics(PlotSymbol.DASH, 5f, paleoProbColor));
			
			ArbitrarilyDiscretizedFunc aveSlipRateMean = null;
			ArbitrarilyDiscretizedFunc aveSlipRateUpper = null;
			ArbitrarilyDiscretizedFunc aveSlipRateLower = null;
			
			// create new list since we might modify it
			paleoRateConstraint = Lists.newArrayList(paleoRateConstraint);
			
			Color aveSlipColor = new Color(10, 100, 55);
			
			if (aveSlipConstraints != null) {
				aveSlipRateMean = new ArbitrarilyDiscretizedFunc();
				aveSlipRateMean.setName("Ave Slip Rate Constraint: Mean");
				funcs.add(aveSlipRateMean);
				plotChars.add(new PlotCurveCharacterstics(PlotSymbol.CIRCLE, 5f, aveSlipColor));
				
				if (plotAveSlipBars) {
					aveSlipRateUpper = new ArbitrarilyDiscretizedFunc();
					aveSlipRateUpper.setName("Ave Slip Rate Constraint: Upper 95% Confidence");
					funcs.add(aveSlipRateUpper);
					plotChars.add(new PlotCurveCharacterstics(PlotSymbol.DASH, 5f, aveSlipColor));
					
					aveSlipRateLower = new ArbitrarilyDiscretizedFunc();
					aveSlipRateLower.setName("Ave Slip Rate Constraint: Lower 95% Confidence");
					funcs.add(aveSlipRateLower);
					plotChars.add(new PlotCurveCharacterstics(PlotSymbol.DASH, 5f, aveSlipColor));
				}
				
				for (AveSlipConstraint aveSlip : aveSlipConstraints) {
					paleoRateConstraint.add(new PaleoFitPlotter.AveSlipFakePaleoConstraint(aveSlip, aveSlip.getSubSectionIndex(),
							sol.getSlipRateForSection(aveSlip.getSubSectionIndex())));
				}
			}
			
			final int xGap = 5;
			
			PaleoProbabilityModel paleoProbModel = null;
			try {
				paleoProbModel = UCERF3_PaleoProbabilityModel.load();
			} catch (IOException e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
			
			int x = xGap;
			
			HashMap<Integer, Integer> xIndForParentMap = new HashMap<Integer, Integer>();
			
			double runningMisfitTotal = 0d;
			
			Map<Integer, Double> traceLengthCache = Maps.newHashMap();
			
			Color origColor = Color.BLACK;
			
			for (int p=0; p<paleoRateConstraint.size(); p++) {
				PaleoRateConstraint constr = paleoRateConstraint.get(p);
				int sectID = constr.getSectionIndex();
				int parentID = -1;
				String name = null;
				for (FaultSectionPrefData data : datas) {
					if (data.getSectionId() == sectID) {
						if (data.getParentSectionId() < 0)
							throw new IllegalStateException("parent ID isn't populated for solution!");
						parentID = data.getParentSectionId();
						name = data.getParentSectionName();
						break;
					}
				}
				if (parentID < 0) {
					System.err.println("No match for rate constraint for section "+sectID);
					continue;
				}
				
				int minSect = Integer.MAX_VALUE;
				int maxSect = -1;
				for (FaultSectionPrefData data : datas) {
					if (data.getParentSectionId() == parentID) {
						int mySectID = data.getSectionId();
						if (mySectID < minSect)
							minSect = mySectID;
						if (mySectID > maxSect)
							maxSect = mySectID;
					}
				}
				
				Preconditions.checkState(maxSect >= minSect);
				int numSects = maxSect - minSect;
				
				int relConstSect = sectID - minSect;
				
				double paleoRateX;
				
				if (xIndForParentMap.containsKey(parentID)) {
					// we already have this parent section, just add the new rate constraint
					
					paleoRateX = xIndForParentMap.get(parentID) + relConstSect;
				} else {
					paleoRateX = x + relConstSect;
					
					EvenlyDiscretizedFunc paleoRtFunc = new EvenlyDiscretizedFunc((double)x, numSects, 1d);
					EvenlyDiscretizedFunc aveSlipRtFunc = new EvenlyDiscretizedFunc((double)x, numSects, 1d);
					EvenlyDiscretizedFunc origRtFunc = new EvenlyDiscretizedFunc((double)x, numSects, 1d);
					paleoRtFunc.setName("(x="+x+") Solution paleo rates for: "+name);
					aveSlipRtFunc.setName("(x="+x+") Solution ave slip prob visible rates for: "+name);
					origRtFunc.setName("(x="+x+") Solution original rates for: "+name);
					for (int j=0; j<numSects; j++) {
						int mySectID = minSect + j;
						paleoRtFunc.set(j, getPaleoRateForSect(sol, mySectID, paleoProbModel, traceLengthCache));
						origRtFunc.set(j, getPaleoRateForSect(sol, mySectID, null, traceLengthCache));
						aveSlipRtFunc.set(j, getAveSlipProbRateForSect(sol, mySectID));
					}
					funcs.add(origRtFunc);
					plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, origColor));
					funcs.add(aveSlipRtFunc);
					plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, aveSlipColor));
					funcs.add(paleoRtFunc);
					plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, paleoProbColor));
					
					funcParentsMap.put(parentID, paleoRtFunc);
					
					xIndForParentMap.put(parentID, x);
					
					x += numSects;
					x += xGap;
				}
				
				if (constr instanceof PaleoFitPlotter.AveSlipFakePaleoConstraint) {
					aveSlipRateMean.set(paleoRateX, constr.getMeanRate());
					if (plotAveSlipBars) {
						aveSlipRateUpper.set(paleoRateX, constr.getUpper95ConfOfRate());
						aveSlipRateLower.set(paleoRateX, constr.getLower95ConfOfRate());
					}
				} else {
					DiscretizedFunc func = funcParentsMap.get(parentID);
					double rate = getPaleoRateForSect(sol, sectID, paleoProbModel, traceLengthCache);
	//					double misfit = Math.pow(constr.getMeanRate() - rate, 2) / Math.pow(constr.getStdDevOfMeanRate(), 2);
					double misfit = Math.pow((constr.getMeanRate() - rate) / constr.getStdDevOfMeanRate(), 2);
					String info = func.getInfo();
					if (info == null || info.isEmpty())
						info = "";
					else
						info += "\n";
					info += "\tSect "+sectID+". Mean: "+constr.getMeanRate()+"\tStd Dev: "
						+constr.getStdDevOfMeanRate()+"\tSolution: "+rate+"\tMisfit: "+misfit;
					runningMisfitTotal += misfit;
					func.setInfo(info);
					
					paleoRateMean.set(paleoRateX, constr.getMeanRate());
					paleoRateUpper.set(paleoRateX, constr.getUpper95ConfOfRate());
					paleoRateLower.set(paleoRateX, constr.getLower95ConfOfRate());
				}
			}
			
			DiscretizedFunc func = funcs.get(funcs.size()-1);
			
			String info = func.getInfo();
			info += "\n\n\tTOTAL MISFIT: "+runningMisfitTotal;
			
			func.setInfo(info);
			
			return new PlotSpec(funcs, plotChars, "Paleosiesmic Constraint Fit", "", "Event Rate Per Year");
		}

	public static void showSegRateComparison(List<PaleoRateConstraint> paleoRateConstraint,
			List<AveSlipConstraint> aveSlipConstraints,
			FaultSystemSolution sol) {
		PlotSpec spec = getSegRateComparisonSpec(paleoRateConstraint, aveSlipConstraints, sol);
		
		GraphiWindowAPI_Impl w = new GraphiWindowAPI_Impl(spec.getFuncs(), spec.getTitle(), spec.getChars(), true);
		w.setX_AxisLabel(spec.getxAxisLabel());
		w.setY_AxisLabel(spec.getyAxisLabel());
	}

	public static HeadlessGraphPanel getHeadlessSegRateComparison(List<PaleoRateConstraint> paleoRateConstraint,
			List<AveSlipConstraint> aveSlipConstraints,
			FaultSystemSolution sol, boolean yLog) {
		PlotSpec spec = getSegRateComparisonSpec(paleoRateConstraint, aveSlipConstraints, sol);
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		
		gp.setYLog(yLog);
		
		gp.drawGraphPanel(spec.getxAxisLabel(), spec.getyAxisLabel(), spec.getFuncs(), spec.getChars(), false, spec.getTitle());
		
		return gp;
	}
	
	static double getPaleoRateForSect(FaultSystemSolution sol, int sectIndex,
			PaleoProbabilityModel paleoProbModel, Map<Integer, Double> traceLengthCache) {
		double rate = 0;
		for (int rupID : sol.getRupturesForSection(sectIndex)) {
			double rupRate = sol.getRateForRup(rupID);
			if (paleoProbModel != null)
				rupRate *= paleoProbModel.getProbPaleoVisible(sol, rupID, sectIndex);
			rate += rupRate;
		}
		return rate;
	}
	
	static double getAveSlipProbRateForSect(FaultSystemSolution sol, int sectIndex) {
		double rate = 0;
		for (int rupID : sol.getRupturesForSection(sectIndex)) {
			int sectIndexInRup = sol.getSectionsIndicesForRup(rupID).indexOf(sectIndex);
			double slipOnSect = sol.getSlipOnSectionsForRup(rupID)[sectIndexInRup]; 
			
			double rupRate = sol.getRateForRup(rupID) * AveSlipConstraint.getProbabilityOfObservedSlip(slipOnSect);
			rate += rupRate;
		}
		return rate;
	}
	
	public static Map<String, PlotSpec> getFaultSpecificPaleoPlotSpec(List<PaleoRateConstraint> paleoRateConstraint,
			List<AveSlipConstraint> aveSlipConstraints,
			FaultSystemSolution sol) {
		Map<String, PlotSpec> specs = Maps.newHashMap();
		
		Map<Integer, List<Integer>> namedFaultsMap = sol.getFaultModel().getNamedFaultsMap();
		
		Map<List<Integer>, List<PaleoRateConstraint>> namedFaultConstraintsMap = Maps.newHashMap();
		
		// create new list since we might modify it
		paleoRateConstraint = Lists.newArrayList(paleoRateConstraint);
		
		PaleoProbabilityModel paleoProbModel = null;
		try {
			paleoProbModel = UCERF3_PaleoProbabilityModel.load();
		} catch (IOException e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}
		
		Map<Integer, Double> traceLengthCache = Maps.newHashMap();
		
		Color origColor = Color.BLACK;
		Color aveSlipColor = new Color(10, 100, 55);
		Color paleoProbColor = Color.RED;
		
		if (aveSlipConstraints != null) {
			for (AveSlipConstraint aveSlip : aveSlipConstraints) {
				paleoRateConstraint.add(new PaleoFitPlotter.AveSlipFakePaleoConstraint(aveSlip, aveSlip.getSubSectionIndex(),
						sol.getSlipRateForSection(aveSlip.getSubSectionIndex())));
			}
		}
		
		for (PaleoRateConstraint constr : paleoRateConstraint) {
			FaultSectionPrefData sect = sol.getFaultSectionData(constr.getSectionIndex());
			Integer parentID = sect.getParentSectionId();
			List<Integer> namedFaults = namedFaultsMap.get(parentID);
			Preconditions.checkNotNull(namedFaults);
			
			List<PaleoRateConstraint> constraintsForFault = namedFaultConstraintsMap.get(namedFaults);
			if (constraintsForFault == null) {
				constraintsForFault = Lists.newArrayList();
				namedFaultConstraintsMap.put(namedFaults, constraintsForFault);
			}
			constraintsForFault.add(constr);
		}
		
		Map<Integer, List<FaultSectionPrefData>> allParentsMap = Maps.newHashMap();
		for (FaultSectionPrefData sect : sol.getFaultSectionDataList()) {
			List<FaultSectionPrefData> parentSects = allParentsMap.get(sect.getParentSectionId());
			if (parentSects == null) {
				parentSects = Lists.newArrayList();
				allParentsMap.put(sect.getParentSectionId(), parentSects);
			}
			parentSects.add(sect);
		}
		
		for (List<Integer> namedFaults : namedFaultConstraintsMap.keySet()) {
			List<PaleoRateConstraint> constraints = namedFaultConstraintsMap.get(namedFaults);
			
			ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
			ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
			
			ArbitrarilyDiscretizedFunc paleoRateMean = new ArbitrarilyDiscretizedFunc();
			paleoRateMean.setName("Paleo Rate Constraint: Mean");
			ArbitrarilyDiscretizedFunc paleoRateUpper = new ArbitrarilyDiscretizedFunc();
			paleoRateUpper.setName("Paleo Rate Constraint: Upper 95% Confidence");
			ArbitrarilyDiscretizedFunc paleoRateLower = new ArbitrarilyDiscretizedFunc();
			paleoRateLower.setName("Paleo Rate Constraint: Lower 95% Confidence");
			
			ArbitrarilyDiscretizedFunc aveSlipRateMean = new ArbitrarilyDiscretizedFunc();
			aveSlipRateMean.setName("Ave Slip Rate Constraint: Mean");
			ArbitrarilyDiscretizedFunc aveSlipRateUpper = new ArbitrarilyDiscretizedFunc();
			aveSlipRateUpper.setName("Ave Slip Rate Constraint: Upper 95% Confidence");
			ArbitrarilyDiscretizedFunc aveSlipRateLower = new ArbitrarilyDiscretizedFunc();
			aveSlipRateLower.setName("Ave Slip Rate Constraint: Lower 95% Confidence");
			
			// first create data lines
			
			// these are used to determine if we should make the x axis latitude or longitude
			double minLat = Double.POSITIVE_INFINITY;
			double maxLat = Double.NEGATIVE_INFINITY;
			double minLon = Double.POSITIVE_INFINITY;
			double maxLon = Double.NEGATIVE_INFINITY;
			
			Map<Integer, List<FaultSectionPrefData>> sectionsForFault = Maps.newHashMap();
			
			for (Integer parentID : namedFaults) {
				List<FaultSectionPrefData> sectionsForParent = allParentsMap.get(parentID);
				
				for (FaultSectionPrefData sect : sectionsForParent) {
					for (Location loc : sect.getFaultTrace()) {
						double lat = loc.getLatitude();
						double lon = loc.getLongitude();
						if (lat < minLat)
							minLat = lat;
						if (lat > maxLat)
							maxLat = lat;
						if (lon < minLon)
							minLon = lon;
						if (lon > maxLon)
							maxLon = lon;
					}
				}
				
				sectionsForFault.put(parentID, sectionsForParent);
			}
			
			double deltaLat = maxLat - minLat;
			double deltaLon = maxLon - minLon;
			boolean latitudeX = deltaLat > 0.6*deltaLon; // heavily favor latitude x
			
			PlotCurveCharacterstics sepChar =
				new PlotCurveCharacterstics(PlotLineType.DASHED, 1f, Color.GRAY);
			
			String[] parentNames = new String[namedFaults.size()];
			for (int i=0; i<namedFaults.size(); i++) {
				Integer parentID = namedFaults.get(i);
				List<FaultSectionPrefData> sectionsForParent = sectionsForFault.get(parentID);
				
				// add separators
				List<Location> sepLocs = Lists.newArrayList();
				FaultTrace firstTrace = sectionsForParent.get(0).getFaultTrace();
				FaultTrace lastTrace = sectionsForParent.get(sectionsForParent.size()-1).getFaultTrace();
				sepLocs.add(firstTrace.get(0));
				sepLocs.add(lastTrace.get(lastTrace.size()-1));
				for (Location sepLoc : sepLocs) {
					ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
					func.setName("(separator)");
					double x;
					if (latitudeX)
						x = sepLoc.getLatitude();
					else
						x = sepLoc.getLongitude();
					func.set(x, 1e-1);
					func.set(x+0.0001, 1e-4);
					funcs.add(func);
					chars.add(sepChar);
				}
				
				String name = sectionsForParent.get(0).getParentSectionName();
				parentNames[i] = name;
				
				ArbitrarilyDiscretizedFunc paleoRtFunc = new ArbitrarilyDiscretizedFunc();
				ArbitrarilyDiscretizedFunc aveSlipRtFunc = new ArbitrarilyDiscretizedFunc();
				ArbitrarilyDiscretizedFunc origRtFunc = new ArbitrarilyDiscretizedFunc();
				paleoRtFunc.setName("Solution paleo rates for: "+name);
				aveSlipRtFunc.setName("Solution ave slip prob visible rates for: "+name);
				origRtFunc.setName("Solution original rates for: "+name);
				for (FaultSectionPrefData sect : sectionsForParent) {
					int mySectID = sect.getSectionId();
					double paleoRate = getPaleoRateForSect(sol, mySectID, paleoProbModel, traceLengthCache);
					double origRate = getPaleoRateForSect(sol, mySectID, null, traceLengthCache);
					double aveSlipRate = getAveSlipProbRateForSect(sol, mySectID);
					for (Location loc : sect.getFaultTrace()) {
						double x;
						if (latitudeX)
							x = loc.getLatitude();
						else
							x = loc.getLongitude();
						paleoRtFunc.set(x, paleoRate);
						origRtFunc.set(x, origRate);
						aveSlipRtFunc.set(x, aveSlipRate);
					}
				}
				funcs.add(origRtFunc);
				chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, origColor));
				funcs.add(aveSlipRtFunc);
				chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, aveSlipColor));
				funcs.add(paleoRtFunc);
				chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, paleoProbColor));
			}
			
			// now add paleo sites
			for (PaleoRateConstraint constr : constraints) {
				double paleoRateX;
				if (latitudeX)
					paleoRateX = constr.getPaleoSiteLoction().getLatitude();
				else
					paleoRateX = constr.getPaleoSiteLoction().getLongitude();
				if (constr instanceof PaleoFitPlotter.AveSlipFakePaleoConstraint) {
					aveSlipRateMean.set(paleoRateX, constr.getMeanRate());
					aveSlipRateUpper.set(paleoRateX, constr.getUpper95ConfOfRate());
					aveSlipRateLower.set(paleoRateX, constr.getLower95ConfOfRate());
				} else {
					paleoRateMean.set(paleoRateX, constr.getMeanRate());
					paleoRateUpper.set(paleoRateX, constr.getUpper95ConfOfRate());
					paleoRateLower.set(paleoRateX, constr.getLower95ConfOfRate());
				}
			}
			
			if (aveSlipRateMean.getNum() > 0) {
				funcs.add(aveSlipRateMean);
				chars.add(new PlotCurveCharacterstics(PlotSymbol.CIRCLE, 5f, aveSlipColor));
				funcs.add(aveSlipRateUpper);
				chars.add(new PlotCurveCharacterstics(PlotSymbol.DASH, 5f, aveSlipColor));
				funcs.add(aveSlipRateLower);
				chars.add(new PlotCurveCharacterstics(PlotSymbol.DASH, 5f, aveSlipColor));
			}
			
			if (paleoRateMean.getNum() > 0) {
				funcs.add(paleoRateMean);
				chars.add(new PlotCurveCharacterstics(PlotSymbol.CIRCLE, 5f, paleoProbColor));
				funcs.add(paleoRateUpper);
				chars.add(new PlotCurveCharacterstics(PlotSymbol.DASH, 5f, paleoProbColor));
				funcs.add(paleoRateLower);
				chars.add(new PlotCurveCharacterstics(PlotSymbol.DASH, 5f, paleoProbColor));
			}
			
			String faultName = StringUtils.getCommonPrefix(parentNames);
			if (parentNames.length > 2 && (
					parentNames[0].startsWith("San Andreas")
					|| parentNames[1].startsWith("San Andreas")))
				faultName = "San Andreas";
			faultName = faultName.replaceAll("\\(", "").replaceAll("\\)", "").trim();
			if (faultName.length() < 2) {
				System.out.println("WARNING: couldn't come up with a common name for: "
						+Joiner.on(", ").join(parentNames));
				faultName = "Fault which includes "+parentNames[0];
			}
			System.out.println(faultName+"\tDeltaLat: "+deltaLat+"\tDeltaLon: "+deltaLon
					+"\tLatitudeX ? "+latitudeX);
			GraphiWindowAPI_Impl gw = new GraphiWindowAPI_Impl(funcs, faultName, chars);
			if (latitudeX)
				gw.getGraphWindow().getGraphPanel().setxAxisInverted(true);
			gw.setYLog(true);
			
			String title = "Paleo Rates/Constraints for "+faultName;
			String xAxisLabel;
			if (latitudeX)
				xAxisLabel = "Latitude (degrees)";
			else
				xAxisLabel = "Longitude (degrees)";
			String yAxisLabel = "Event Rate Per Year";
			PlotSpec spec = new PlotSpec(funcs, chars, title, xAxisLabel, yAxisLabel);
			specs.put(faultName, spec);
		}
		
		return specs;
	}
	
	public static void main(String[] args) throws IOException, DocumentException {
		File invDir = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions");
		File solFile = new File(invDir,
				"FM3_1_ZENG_EllB_DsrUni_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3" +
				"_VarPaleo10_VarMFDSmooth1000_VarSectNuclMFDWt0.01_sol.zip");
		
		FaultSystemSolution sol = SimpleFaultSystemSolution.fromZipFile(solFile);
		List<PaleoRateConstraint> paleoRateConstraint =
			UCERF3_PaleoRateConstraintFetcher.getConstraints(sol.getFaultSectionDataList());
		List<AveSlipConstraint> aveSlipConstraints = AveSlipConstraint.load(sol.getFaultSectionDataList());
		
		getFaultSpecificPaleoPlotSpec(paleoRateConstraint, aveSlipConstraints, sol);
	}

}
