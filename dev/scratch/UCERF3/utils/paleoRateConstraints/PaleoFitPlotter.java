package scratch.UCERF3.utils.paleoRateConstraints;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.dom4j.DocumentException;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.StatUtil;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.gui.infoTools.PlotSpec;

import scratch.UCERF3.AverageFaultSystemSolution;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.FaultSystemSolutionFetcher;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.inversion.CommandLineInversionRunner;
import scratch.UCERF3.inversion.UCERF2_ComparisonSolutionFetcher;
import scratch.UCERF3.utils.UCERF3_DataUtils;
import scratch.UCERF3.utils.aveSlip.AveSlipConstraint;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoFitPlotter.AveSlipFakePaleoConstraint;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;

public class PaleoFitPlotter {

	public static class AveSlipFakePaleoConstraint extends PaleoRateConstraint {
		private boolean isMultiple;
		
		public AveSlipFakePaleoConstraint(AveSlipConstraint aveSlip, int sectIndex, double slipRate) {
			super(null, aveSlip.getSiteLocation(), sectIndex, slipRate/aveSlip.getWeightedMean(),
					slipRate/aveSlip.getLowerUncertaintyBound(), slipRate/aveSlip.getUpperUncertaintyBound());
			isMultiple = false;
		}
		
		public AveSlipFakePaleoConstraint(
				AveSlipConstraint aveSlip, int sectIndex, double[] slipRates, double[] weights) {
			super(null, aveSlip.getSiteLocation(), sectIndex,
					FaultSystemSolutionFetcher.calcScaledAverage(slipRates, weights)/aveSlip.getWeightedMean(),
					StatUtils.min(slipRates)/aveSlip.getWeightedMean(),
					StatUtils.max(slipRates)/aveSlip.getWeightedMean());
			isMultiple = true;
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
						paleoRtFunc.set(j, getPaleoRateForSect(sol, mySectID, paleoProbModel));
						origRtFunc.set(j, getPaleoRateForSect(sol, mySectID, null));
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
					double rate = getPaleoRateForSect(sol, sectID, paleoProbModel);
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
		CommandLineInversionRunner.setFontSizes(gp);
		gp.setYLog(yLog);
		
		gp.drawGraphPanel(spec.getxAxisLabel(), spec.getyAxisLabel(), spec.getFuncs(), spec.getChars(), false, spec.getTitle());
		
		return gp;
	}
	
	public static double getPaleoRateForSect(FaultSystemSolution sol, int sectIndex,
			PaleoProbabilityModel paleoProbModel) {
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
	
	public static class DataForPaleoFaultPlots implements Serializable {
		
		private Map<Integer, double[]> origSlipsMap;
		private Map<Integer, double[]> targetSlipsMap;
		private Map<Integer, double[]> solSlipsMap;
		private Map<Integer, double[]> paleoRatesMap;
		private Map<Integer, double[]> origRatesMap;
		private Map<Integer, double[]> aveSlipRatesMap;
		
		private List<Map<Integer, double[]>> allArraysList;
		
		private double weight;
		
		private DataForPaleoFaultPlots(double weight) {
			origSlipsMap = Maps.newHashMap();
			targetSlipsMap = Maps.newHashMap();
			solSlipsMap = Maps.newHashMap();
			paleoRatesMap = Maps.newHashMap();
			origRatesMap = Maps.newHashMap();
			aveSlipRatesMap = Maps.newHashMap();
			
			allArraysList = Lists.newArrayList();
			allArraysList.add(origSlipsMap);
			allArraysList.add(targetSlipsMap);
			allArraysList.add(solSlipsMap);
			allArraysList.add(paleoRatesMap);
			allArraysList.add(origRatesMap);
			allArraysList.add(aveSlipRatesMap);
			
			this.weight = weight;
		}
		
		public static DataForPaleoFaultPlots build(
				FaultSystemSolution sol,
				Map<String, List<Integer>> namedFaultsMap,
				Map<String, List<PaleoRateConstraint>> namedFaultConstraintsMap,
				Map<Integer, List<FaultSectionPrefData>> allParentsMap,
				PaleoProbabilityModel paleoProbModel,
				double weight) {
			
			DataForPaleoFaultPlots data = new DataForPaleoFaultPlots(weight);
			
			for (String name : namedFaultConstraintsMap.keySet()) {
				List<Integer> parentIDs = namedFaultsMap.get(name);
				
				for (Integer parentID : parentIDs) {
					List<FaultSectionPrefData> sects = allParentsMap.get(parentID);
					int numSects = sects.size();
					
					double[] origSlips = new double[numSects];
					double[] targetSlips = new double[numSects];
					double[] solSlips = new double[numSects];
					double[] paleoRates = new double[numSects];
					double[] origRates = new double[numSects];
					double[] aveSlipRates = new double[numSects];
					
					for (int s=0; s<numSects; s++) {
						FaultSectionPrefData sect = sects.get(s);
						int mySectID = sect.getSectionId();
						paleoRates[s] = getPaleoRateForSect(sol, mySectID, paleoProbModel);
						origRates[s] = getPaleoRateForSect(sol, mySectID, null);
						aveSlipRates[s] = getAveSlipProbRateForSect(sol, mySectID);
						// convert slips to mm/yr
						origSlips[s] = sect.getOrigAveSlipRate();
						targetSlips[s] = sol.getSlipRateForSection(sect.getSectionId())*1e3;
						solSlips[s] = sol.calcSlipRateForSect(sect.getSectionId())*1e3;
					}
					
					if (parentID == 301) {
						if (StatUtils.min(solSlips) < 1) {
							System.out.println("Solution slip less than 1 on Mojave S...WTF?");
							System.out.println("origSlips: ["+Joiner.on(",").join(Doubles.asList(origSlips))+"]");
							System.out.println("targetSlips: ["+Joiner.on(",").join(Doubles.asList(targetSlips))+"]");
							System.out.println("solSlips: ["+Joiner.on(",").join(Doubles.asList(solSlips))+"]");
							System.out.println("paleoRates: ["+Joiner.on(",").join(Doubles.asList(paleoRates))+"]");
							System.out.println("origRates: ["+Joiner.on(",").join(Doubles.asList(origRates))+"]");
							System.out.println("aveSlipRates: ["+Joiner.on(",").join(Doubles.asList(aveSlipRates))+"]");
							System.exit(1);
						}
					}
					
					data.origSlipsMap.put(parentID, origSlips);
					data.targetSlipsMap.put(parentID, targetSlips);
					data.solSlipsMap.put(parentID, solSlips);
					data.paleoRatesMap.put(parentID, paleoRates);
					data.origRatesMap.put(parentID, origRates);
					data.aveSlipRatesMap.put(parentID, aveSlipRates);
				}
			}
			return data;
		}
	}
	
	public static Map<String, PlotSpec[]> getFaultSpecificPaleoPlotSpec(
			List<PaleoRateConstraint> paleoRateConstraint,
			List<AveSlipConstraint> aveSlipConstraints,
			FaultSystemSolution sol) {
		Map<String, List<Integer>> namedFaultsMap = sol.getFaultModel().getNamedFaultsMapAlt();
		
		// create new list since we might modify it
		paleoRateConstraint = Lists.newArrayList(paleoRateConstraint);
		
		if (aveSlipConstraints != null) {
			for (AveSlipConstraint aveSlip : aveSlipConstraints) {
				paleoRateConstraint.add(new PaleoFitPlotter.AveSlipFakePaleoConstraint(aveSlip, aveSlip.getSubSectionIndex(),
						sol.getSlipRateForSection(aveSlip.getSubSectionIndex())));
			}
		}
		
		PaleoProbabilityModel paleoProbModel = null;
		try {
			paleoProbModel = UCERF3_PaleoProbabilityModel.load();
		} catch (IOException e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}
		
		Map<String, List<PaleoRateConstraint>> namedFaultConstraintsMap =
			 getNamedFaultConstraintsMap(paleoRateConstraint, sol.getFaultSectionDataList(), namedFaultsMap);
		
		Map<Integer, List<FaultSectionPrefData>> allParentsMap =
			getAllParentsMap(sol.getFaultSectionDataList());
		
		List<DataForPaleoFaultPlots> datas = Lists.newArrayList();
		if (sol instanceof AverageFaultSystemSolution && 
				((AverageFaultSystemSolution)sol).getNumSolutions() <= 10) {
			int cnt = 0;
			for (FaultSystemSolution s : (AverageFaultSystemSolution)sol) {
				System.out.println("Building paleo data for solution: "+(++cnt));
				datas.add(DataForPaleoFaultPlots.build(s, namedFaultsMap, namedFaultConstraintsMap,
						allParentsMap, paleoProbModel, 1d));
			}
		} else {
			datas.add(DataForPaleoFaultPlots.build(sol, namedFaultsMap, namedFaultConstraintsMap,
					allParentsMap, paleoProbModel, 1d));
		}
		return getFaultSpecificPaleoPlotSpecs(namedFaultsMap, namedFaultConstraintsMap, datas, allParentsMap);
	}
	
	public static Map<String, List<PaleoRateConstraint>> getNamedFaultConstraintsMap(
			List<PaleoRateConstraint> paleoRateConstraint,
			List<FaultSectionPrefData> fsd,
			Map<String, List<Integer>> namedFaultsMap) {
		Map<String, List<PaleoRateConstraint>> namedFaultConstraintsMap = Maps.newHashMap();
		
		for (PaleoRateConstraint constr : paleoRateConstraint) {
			FaultSectionPrefData sect = fsd.get(constr.getSectionIndex());
			Integer parentID = sect.getParentSectionId();
			String name = null;
			for (String faultName : namedFaultsMap.keySet()) {
				List<Integer> parentIDs = namedFaultsMap.get(faultName);
				if (parentIDs.contains(parentID)) {
					name = faultName;
					break;
				}
			}
			if (name == null) {
				System.err.println("WARNING: no named fault map for paleo constraint on parent section "
						+sect.getParentSectionName()+" (pale name="+constr.getPaleoSiteName()+")");
				continue;
			}
			
			List<PaleoRateConstraint> constraintsForFault = namedFaultConstraintsMap.get(name);
			if (constraintsForFault == null) {
				constraintsForFault = Lists.newArrayList();
				namedFaultConstraintsMap.put(name, constraintsForFault);
			}
			constraintsForFault.add(constr);
		}
		
		return namedFaultConstraintsMap;
	}
	
	public static Map<Integer, List<FaultSectionPrefData>> getAllParentsMap(
			List<FaultSectionPrefData> fsd) {
		Map<Integer, List<FaultSectionPrefData>> allParentsMap = Maps.newHashMap();
		for (FaultSectionPrefData sect : fsd) {
			List<FaultSectionPrefData> parentSects = allParentsMap.get(sect.getParentSectionId());
			if (parentSects == null) {
				parentSects = Lists.newArrayList();
				allParentsMap.put(sect.getParentSectionId(), parentSects);
			}
			parentSects.add(sect);
		}
		return allParentsMap;
	}
	
	private static List<PlotCurveCharacterstics> getCharsForFuncs(
			List<DiscretizedFunc> funcs, Color color, float mainThickness) {
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		if (funcs.size() == 1) {
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, mainThickness, color));
		} else {
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, color));
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, color));
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, mainThickness, color));
		}
		return chars;
	}
	
	private static List<DiscretizedFunc> getFuncsForScalar(
			List<DataForPaleoFaultPlots> datas,
			int arrayIndex, int parentID,
			double[][] xvals, String name) {
		List<DiscretizedFunc> funcs = Lists.newArrayList();
		
		if (datas.size() == 1) {
			ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
			func.setName(name);
			double[] array = datas.get(0).allArraysList.get(arrayIndex).get(parentID);
			for (int i=0; i<array.length; i++)
				for (double x : xvals[i])
					func.set(x, array[i]);
			funcs.add(func);
		} else {
			double[][] arrayVals = new double[xvals.length][datas.size()];
			
			double totWeight = 0;
			double[] weights = new double[datas.size()];
			
			for (int d=0; d<datas.size(); d++) {
				DataForPaleoFaultPlots data = datas.get(d);
				double[] array = data.allArraysList.get(arrayIndex).get(parentID);
				for (int s=0; s<xvals.length; s++)
					arrayVals[s][d] = array[s];
				weights[d] = data.weight;
				totWeight += data.weight;
			}
			
			for (int i=0; i<weights.length; i++)
				weights[i] = weights[i] / totWeight;
			Preconditions.checkState(totWeight > 0);
			
			ArbitrarilyDiscretizedFunc minFunc = new ArbitrarilyDiscretizedFunc();
			minFunc.setName(name+" (minimum)");
			ArbitrarilyDiscretizedFunc maxFunc = new ArbitrarilyDiscretizedFunc();
			maxFunc.setName(name+" (maximum)");
			ArbitrarilyDiscretizedFunc meanFunc = new ArbitrarilyDiscretizedFunc();
			meanFunc.setName(name+" (weighted mean)");
			
			for (int s=0; s<xvals.length; s++) {
				double[] myXvals = xvals[s];
				double[] array = arrayVals[s];
				
				double mean = FaultSystemSolutionFetcher.calcScaledAverage(array, weights);
				if (Double.isInfinite(mean))
					System.out.println("INFINITE! array=["+Joiner.on(",").join(Doubles.asList(array))
							+"], weights=["+Joiner.on(",").join(Doubles.asList(weights)));
				double min = StatUtils.min(array);
				double max = StatUtils.max(array);
				for (double x : myXvals) {
					minFunc.set(x, min);
					maxFunc.set(x, max);
					meanFunc.set(x, mean);
				}
			}
			
//			System.out.println("Max mean for "+arrayIndex+": "+meanFunc.getMaxY());
			
			funcs.add(minFunc);
			funcs.add(maxFunc);
			funcs.add(meanFunc);
		}
		
		return funcs;
	}
	
	public static Map<String, PlotSpec[]> getFaultSpecificPaleoPlotSpecs(
			Map<String, List<Integer>> namedFaultsMap,
			Map<String, List<PaleoRateConstraint>> namedFaultConstraintsMap,
			List<DataForPaleoFaultPlots> datas,
			Map<Integer, List<FaultSectionPrefData>> allParentsMap) {
		
		Color origColor = Color.BLACK;
		Color aveSlipColor = new Color(10, 100, 55);
		Color paleoProbColor = Color.RED;
		
		Map<String, PlotSpec[]> specs = Maps.newHashMap();
		
		for (String name : namedFaultConstraintsMap.keySet()) {
			List<PaleoRateConstraint> constraints = namedFaultConstraintsMap.get(name);
			List<Integer> namedFaults = namedFaultsMap.get(name);
			
			ArrayList<DiscretizedFunc> rateFuncs = Lists.newArrayList();
			ArrayList<PlotCurveCharacterstics> rateChars = Lists.newArrayList();
			ArrayList<DiscretizedFunc> slipFuncs = Lists.newArrayList();
			ArrayList<PlotCurveCharacterstics> slipChars = Lists.newArrayList();
			
			List<Location> allSepLocs = Lists.newArrayList();
			
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
			
			double maxSlip = 0d;
			
			Map<Integer, List<FaultSectionPrefData>> sectionsForFault = Maps.newHashMap();
			
			for (Integer parentID : namedFaults) {
				List<FaultSectionPrefData> sectionsForParent = allParentsMap.get(parentID);
				if (sectionsForParent == null)
					continue;
				
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
				
				for (DataForPaleoFaultPlots data : datas) {
					double origSlip = StatUtils.max(data.origSlipsMap.get(parentID));
					double solSlip = StatUtils.max(data.solSlipsMap.get(parentID));
					if (origSlip > maxSlip)
						maxSlip = origSlip;
					if (solSlip > maxSlip)
						maxSlip = solSlip;
				}
				
				sectionsForFault.put(parentID, sectionsForParent);
			}
			
			double deltaLat = maxLat - minLat;
			double deltaLon = maxLon - minLon;
			boolean latitudeX = deltaLat > 0.5*deltaLon; // heavily favor latitude x
			
			PlotCurveCharacterstics sepChar =
				new PlotCurveCharacterstics(PlotLineType.DASHED, 1f, Color.GRAY);
			
			int actualCount = 0;
			for (int i=0; i<namedFaults.size(); i++) {
				Integer parentID = namedFaults.get(i);
				List<FaultSectionPrefData> sectionsForParent = sectionsForFault.get(parentID);
				if (sectionsForParent == null)
					continue;
				actualCount++;
				
				// add separators
				FaultTrace firstTrace = sectionsForParent.get(0).getFaultTrace();
				FaultTrace lastTrace = sectionsForParent.get(sectionsForParent.size()-1).getFaultTrace();
				allSepLocs.add(firstTrace.get(0));
				allSepLocs.add(lastTrace.get(lastTrace.size()-1));
				
				double[][] xvals = new double[sectionsForParent.size()][];
				for (int s=0; s<xvals.length; s++) {
					FaultTrace trace = sectionsForParent.get(s).getFaultTrace();
					xvals[s] = new double[trace.size()];
					for (int t=0; t<trace.size(); t++) {
						Location loc = trace.get(t);
						if (latitudeX)
							xvals[s][t] = loc.getLatitude();
						else
							xvals[s][t] = loc.getLongitude();
					}
				}
				
				List<DiscretizedFunc> origSlipFuncs = getFuncsForScalar(datas, 0, parentID, xvals,
						"Original nonreduced slip rates for: "+name);
				List<DiscretizedFunc> targetSlipFuncs = getFuncsForScalar(datas, 1, parentID, xvals,
						"Target slip rates for: "+name);
				List<DiscretizedFunc> solSlipFuncs = getFuncsForScalar(datas, 2, parentID, xvals,
						"Solution slip rates for: "+name);
				List<DiscretizedFunc> paleoRtFuncs = getFuncsForScalar(datas, 3, parentID, xvals,
						"Solution paleo rates for: "+name);
				List<DiscretizedFunc> origRtFuncs = getFuncsForScalar(datas, 4, parentID, xvals,
						"Solution original rates for: "+name);
				List<DiscretizedFunc> aveSlipRtFuncs = getFuncsForScalar(datas, 5, parentID, xvals,
						"Solution ave slip prob visible rates for: "+name);
				
				// skip if no rate on any of the sections
				boolean skip = origRtFuncs.get(origRtFuncs.size()-1).getMaxY() <= 0;
				if (!skip) {
					rateFuncs.addAll(paleoRtFuncs);
					rateChars.addAll(getCharsForFuncs(paleoRtFuncs, paleoProbColor, 2f));
					rateFuncs.addAll(origRtFuncs);
					rateChars.addAll(getCharsForFuncs(origRtFuncs, origColor, 1f));
					rateFuncs.addAll(aveSlipRtFuncs);
					rateChars.addAll(getCharsForFuncs(aveSlipRtFuncs, aveSlipColor, 2f));
					if (datas.size() == 1) {
						slipFuncs.addAll(origSlipFuncs);
						slipChars.addAll(getCharsForFuncs(origSlipFuncs, Color.CYAN, 1f));
					}
					slipFuncs.addAll(targetSlipFuncs);
					slipChars.addAll(getCharsForFuncs(targetSlipFuncs, Color.BLUE, 2f));
					slipFuncs.addAll(solSlipFuncs);
					slipChars.addAll(getCharsForFuncs(solSlipFuncs, Color.MAGENTA, 2f));
				}
			}
			if (actualCount == 0)
				// no matching faults in this FM
				continue;
			
			// now add paleo sites
			for (PaleoRateConstraint constr : constraints) {
				Preconditions.checkNotNull(constr, "Paleo Constraint NULL!");
				Preconditions.checkNotNull(constr.getPaleoSiteLoction(),
						"Paleo Constraint Location NULL!");
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
				rateFuncs.add(aveSlipRateMean);
				rateChars.add(new PlotCurveCharacterstics(PlotSymbol.CIRCLE, 5f, aveSlipColor));
				rateFuncs.add(aveSlipRateUpper);
				rateChars.add(new PlotCurveCharacterstics(PlotSymbol.DASH, 5f, aveSlipColor));
				rateFuncs.add(aveSlipRateLower);
				rateChars.add(new PlotCurveCharacterstics(PlotSymbol.DASH, 5f, aveSlipColor));
			}
			
			if (paleoRateMean.getNum() > 0) {
				rateFuncs.add(paleoRateMean);
				rateChars.add(new PlotCurveCharacterstics(PlotSymbol.CIRCLE, 5f, paleoProbColor));
				rateFuncs.add(paleoRateUpper);
				rateChars.add(new PlotCurveCharacterstics(PlotSymbol.DASH, 5f, paleoProbColor));
				rateFuncs.add(paleoRateLower);
				rateChars.add(new PlotCurveCharacterstics(PlotSymbol.DASH, 5f, paleoProbColor));
			}
			
			// no longer needed
//			String[] parentNameArray = new String[parentNames.size()];
//			for (int i=0; i<parentNames.size(); i++)
//				parentNameArray[i] = parentNames.get(i);
//			String faultName = StringUtils.getCommonPrefix(parentNameArray);
//			if (parentNameArray.length > 2 && (
//					parentNameArray[0].startsWith("San Andreas")
//					|| parentNameArray[1].startsWith("San Andreas")))
//				faultName = "San Andreas";
//			faultName = faultName.replaceAll("\\(", "").replaceAll("\\)", "").trim();
//			if (faultName.length() < 2) {
//				System.out.println("WARNING: couldn't come up with a common name for: "
//						+Joiner.on(", ").join(parentNames));
//				faultName = "Fault which includes "+parentNameArray[0];
//			}
			System.out.println(name+"\tDeltaLat: "+deltaLat+"\tDeltaLon: "+deltaLon
					+"\tLatitudeX ? "+latitudeX);
			
			String paleoTitle = "Paleo Rates/Constraints for "+name;
			String slipTitle = "Slip Rates for "+name;
			String xAxisLabel;
			if (latitudeX)
				xAxisLabel = "Latitude (degrees)";
			else
				xAxisLabel = "Longitude (degrees)";
			String paleoYAxisLabel = "Event Rate Per Year";
			String slipYAxisLabel = "Slip Rate (mm/yr)";
			
			ArrayList<DiscretizedFunc> paleoOnlyFuncs = Lists.newArrayList();
			ArrayList<PlotCurveCharacterstics> paleoOnlyChars = Lists.newArrayList();
			paleoOnlyFuncs.addAll(rateFuncs);
			paleoOnlyChars.addAll(rateChars);
			
			ArrayList<DiscretizedFunc> slipOnlyFuncs = Lists.newArrayList();
			ArrayList<PlotCurveCharacterstics> slipOnlyChars = Lists.newArrayList();
			slipOnlyFuncs.addAll(slipFuncs);
			slipOnlyChars.addAll(slipChars);
			
			for (Location sepLoc : allSepLocs) {
				ArbitrarilyDiscretizedFunc paleoFunc = new ArbitrarilyDiscretizedFunc();
				ArbitrarilyDiscretizedFunc slipFunc = new ArbitrarilyDiscretizedFunc();
				paleoFunc.setName("(separator)");
				slipFunc.setName("(separator)");
				double x;
				if (latitudeX)
					x = sepLoc.getLatitude();
				else
					x = sepLoc.getLongitude();
				paleoFunc.set(x, 1e-1);
				paleoFunc.set(x+0.0001, 1e-4);
				slipFunc.set(x, 5e1);
				slipFunc.set(x+0.0001, 1e-1);
				paleoOnlyFuncs.add(paleoFunc);
				paleoOnlyChars.add(sepChar);
				slipOnlyFuncs.add(slipFunc);
				slipOnlyChars.add(sepChar);
			}
			
			PlotSpec paleoOnlySpec = new PlotSpec(
					paleoOnlyFuncs, paleoOnlyChars, paleoTitle, xAxisLabel, paleoYAxisLabel);
			PlotSpec slipOnlySpec = new PlotSpec(
					slipOnlyFuncs, slipOnlyChars, slipTitle, xAxisLabel, slipYAxisLabel);
			ArrayList<DiscretizedFunc> combinedFuncs = Lists.newArrayList(paleoOnlyFuncs);
			for (DiscretizedFunc func : slipFuncs) {
				ArbitrarilyDiscretizedFunc scaledFunc = new ArbitrarilyDiscretizedFunc(
						func.getName()+" (normalized to max slip)");
				for (int i=0; i<func.getNum(); i++) {
					Point2D pt = func.get(i);
					if (pt.getY() > 0)
						scaledFunc.set(pt.getX(), pt.getY() / maxSlip);
				}
				combinedFuncs.add(scaledFunc);
			}
			ArrayList<PlotCurveCharacterstics> combinedChars = Lists.newArrayList(paleoOnlyChars);
			combinedChars.addAll(slipChars);
			PlotSpec combinedSpec = new PlotSpec(
					combinedFuncs, combinedChars, paleoTitle, xAxisLabel, paleoYAxisLabel);
			PlotSpec[] specArray = { paleoOnlySpec, slipOnlySpec, combinedSpec };
			specs.put(name, specArray);
		}
		
		return specs;
	}
	
	public static void main(String[] args) throws IOException, DocumentException {
		File invDir = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions");
		File solFile = new File(invDir,
				"FM3_1_ZENG_EllB_DsrUni_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3" +
				"_VarPaleo10_VarMFDSmooth1000_VarSectNuclMFDWt0.01_sol.zip");
//				"FM3_1_ZENG_EllB_DsrUni_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3" +
//				"_VarPaleo0.1_VarAveSlip0.1_VarMFDSmooth1000_VarSectNuclMFDWt0.1_VarNone_sol.zip");
//				"FM2_1_UC2ALL_EllB_DsrTap_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU2" +
//				"_VarPaleo10_VarMFDSmooth1000_VarSectNuclMFDWt0.01_sol.zip");
//				"FM2_1_UC2ALL_EllB_DsrTap_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU2" +
//				"_VarPaleo0.1_VarAveSlip0.1_VarMFDSmooth1000_VarSectNuclMFDWt0.1_VarNone_sol.zip");
		FaultSystemSolution sol = SimpleFaultSystemSolution.fromZipFile(solFile);
//		FaultSystemSolution sol = UCERF2_ComparisonSolutionFetcher.getUCERF2Solution(FaultModels.FM2_1);
		List<PaleoRateConstraint> paleoRateConstraint =
			UCERF3_PaleoRateConstraintFetcher.getConstraints(sol.getFaultSectionDataList());
		List<AveSlipConstraint> aveSlipConstraints = AveSlipConstraint.load(sol.getFaultSectionDataList());
		
//		Map<String, PlotSpec> specs =
//				getFaultSpecificPaleoPlotSpec(paleoRateConstraint, aveSlipConstraints, sol);
		
//		File plotDir = new File("/tmp/paleo_fault_plots_low_fm2_tapered");
//		File plotDir = new File("/tmp/paleo_fault_plots_ucerf2");
//		File plotDir = new File("/tmp/paleo_fault_plots_lowpaleo");
		File plotDir = new File("/tmp/paleo_fault_plots");
		if (!plotDir.exists())
			plotDir.mkdir();
		
		CommandLineInversionRunner.writePaleoFaultPlots(paleoRateConstraint, aveSlipConstraints, sol, plotDir);
	}

}
