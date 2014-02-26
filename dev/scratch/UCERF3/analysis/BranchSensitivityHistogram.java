package scratch.UCERF3.analysis;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.util.ClassUtils;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;

import scratch.UCERF3.inversion.CommandLineInversionRunner;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.logicTree.LogicTreeBranchNode;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.primitives.Doubles;

/**
 * Used to create various branch sensitivity histograms. Should be populated as each branch is
 * calculated and has convenience methods to work with LogicTreeBranchNode classes.
 * @author kevin
 *
 */
public class BranchSensitivityHistogram implements Serializable {
	
	private Table<String, String, List<Double>> valsTable;
	private Table<String, String, List<Double>> weightsTable;
	
	private String xAxisName;
	
	public BranchSensitivityHistogram(String xAxisName) {
		this.xAxisName = xAxisName;
		
		valsTable = HashBasedTable.create();
		weightsTable = HashBasedTable.create();
	}
	
	public void addValues(LogicTreeBranch branch, Double val, Double weight) {
		addValues(branch, val, weight, new String[0]);
	}
	
	/**
	 * 
	 * @param branch
	 * @param val
	 * @param weight
	 * @param extraValues
	 */
	public void addValues(LogicTreeBranch branch, Double val, Double weight, String... extraValues) {
		Preconditions.checkState(extraValues == null || extraValues.length % 2 == 0,
				"Extra values must be empty/null or supplied in category/choice pairs.");
		
		List<String> extraCategories = Lists.newArrayList();
		List<String> extraChoices = Lists.newArrayList();
		if (extraValues != null && extraValues.length > 0) {
			for (int i=0; i<extraValues.length; i++) {
				extraCategories.add(extraValues[i++]); // increment once in-between
				extraChoices.add(extraValues[i]);
			}
		}
		
		// populate each branch level
		for (int i=0; i<branch.size(); i++) {
			LogicTreeBranchNode<?> choice = branch.getValue(i);
			String categoryName = ClassUtils.getClassNameWithoutPackage(LogicTreeBranch.getEnumEnclosingClass(choice.getClass()));
			addValue(categoryName, choice.getShortName(), val, weight);
		}
		
		//  populate each extra category
		for (int i=0; i<extraCategories.size(); i++) {
			addValue(extraCategories.get(i), extraChoices.get(i), val, weight);
		}
	}
	
	public synchronized void addValue(String categoryName, String choiceName, Double val, Double weight) {
		if (!valsTable.contains(categoryName, choiceName)) {
			valsTable.put(categoryName, choiceName, new ArrayList<Double>());
			weightsTable.put(categoryName, choiceName, new ArrayList<Double>());
		}
		valsTable.get(categoryName, choiceName).add(val);
		weightsTable.get(categoryName, choiceName).add(weight);
	}
	
	public void addAll(BranchSensitivityHistogram o) {
		for (Cell<String, String, List<Double>> cell : valsTable.cellSet()) {
			String categoryName = cell.getRowKey();
			String choiceName = cell.getColumnKey();
			List<Double> value = cell.getValue();
			for (int i = 0; i < value.size(); i++) {
				double val = value.get(i);
				double weight = weightsTable.get(categoryName, choiceName).get(i);
				addValue(categoryName, choiceName, val, weight);
			}
		}
	}
	
	private HistogramFunction generateHist(String categoryName, String choiceName, double min, int num, double delta) {
		// values above/below included in last/first bin
		
		HistogramFunction hist = new HistogramFunction(min, num, delta);
		
		List<Double> vals = valsTable.get(categoryName, choiceName);
		List<Double> weights = weightsTable.get(categoryName, choiceName);
		
		Preconditions.checkState(vals.size() == weights.size());
		Preconditions.checkState(!vals.isEmpty());
		
//		double totWeight = 0d;
//		for (double weight : weights)
//			totWeight += weight;
//		double weightMult = 1d/totWeight;
		
		for (int i=0; i<vals.size(); i++) {
			double val = vals.get(i);
			if (!Doubles.isFinite(val))
				continue;
			double weight = weights.get(i);
			// use this to map below/above values
			int index = hist.getClosestXIndex(val);
			
//			hist.add(index, weight*weightMult);
			hist.add(index, weight);
		}
		
		hist.setName(choiceName);
		
		return hist;
	}
	
	/**
	 * calculate the weighted mean of all values across each choice in the given category
	 * @param categoryName
	 * @return
	 */
	public double calcMean(String categoryName) {
		return calcMean(categoryName, null);
	}
	
	/**
	 * calculate the weighted mean of all values across the given choice in the given category,
	 * or across all choices if choiceName is null
	 * @param categoryName
	 * @param choiceName
	 * @return
	 */
	public double calcMean(String categoryName, String choiceName) {
		List<Double> vals, weights;
		if (choiceName == null) {
			// over all choices. each list is independent
			vals = Lists.newArrayList();
			weights = Lists.newArrayList();
			for (String choice : getChoices(categoryName)) {
				vals.addAll(valsTable.get(categoryName, choice));
				weights.addAll(weightsTable.get(categoryName, choice));
			}
		} else {
			vals = valsTable.get(categoryName, choiceName);
			weights = weightsTable.get(categoryName, choiceName);
		}
		
		double sumWeight = 0d;
		double weightedSum = 0d;
//		double nonWeightMean = 0d;
		
		for (int i=0; i<vals.size(); i++) {
			double val = vals.get(i);
			double weight = weights.get(i);
			
			if (!Doubles.isFinite(val))
				continue;
			
//			nonWeightMean += val;
			
			sumWeight += weight;
			weightedSum += val*weight;
		}
		
//		System.out.println("calcMean="+(weightedSum / sumWeight)+" nonWeight="+(nonWeightMean/(double)vals.size()));
		
		return weightedSum / sumWeight;
	}
	
	/**
	 * calculate the weighted std dev of all values across each choice in the given category
	 * @param categoryName
	 * @return
	 */
	public double calcStdDev(String categoryName) {
		return calcStdDev(categoryName, null);
	}
	
	/**
	 * calculate the weighted std dev of all values across the given choice in the given category,
	 * or across all choices if choiceName is null
	 * @param categoryName
	 * @param choiceName
	 * @return
	 */
	public double calcStdDev(String categoryName, String choiceName) {
		List<Double> vals, weights;
		if (choiceName == null) {
			// over all choices. each list is independent
			vals = Lists.newArrayList();
			weights = Lists.newArrayList();
			for (String choice : getChoices(categoryName)) {
				vals.addAll(valsTable.get(categoryName, choice));
				weights.addAll(weightsTable.get(categoryName, choice));
			}
		} else {
			vals = valsTable.get(categoryName, choiceName);
			weights = weightsTable.get(categoryName, choiceName);
		}
		
		double sumWeight = 0d;
		double mean = calcMean(categoryName, choiceName);
		double var = 0;
		for(int i=0; i<vals.size(); i++) {
			double val = vals.get(i);
			double weight = weights.get(i);
			
			if (!Doubles.isFinite(val))
				continue;
			
			sumWeight += weight;
			var += (val-mean)*(val-mean)*weight;
		}
		var /= sumWeight;
		return Math.sqrt(var);
	}
	
	public Range getRange() {
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		
		for (Cell<String, String, List<Double>> cell : valsTable.cellSet()) {
			for (double val : cell.getValue()) {
				if (val < min)
					min = val;
				if (val > max)
					max = val;
			}
		}
		
		return new Range(min, max);
	}
	
	public Set<String> getChoices(String categoryName) {
		return valsTable.row(categoryName).keySet();
	}
	
	public Map<String, PlotSpec> getStackedHistPlots(double min, int num, double delta) {
		Map<String, PlotSpec> map = Maps.newHashMap();
		
		for (String categoryName : valsTable.rowKeySet()) {
			if (valsTable.row(categoryName).size() > 1)
				// only include if there are at least 2 choices at this level
				map.put(categoryName, getStackedHistPlot(categoryName, min, num, delta));
		}
		
		return map;
	}
	
	public PlotSpec getStackedHistPlot(String categoryName, double min, int num, double delta) {
		List<HistogramFunction> hists = Lists.newArrayList();
		
		List<String> choiceNames = Lists.newArrayList(getChoices(categoryName));
		Collections.sort(choiceNames);
		
		for (String choiceName : choiceNames)
			hists.add(generateHist(categoryName, choiceName, min, num, delta));
		
		List<HistogramFunction> stackedHists = HistogramFunction.getStackedHists(hists, true);
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
//		List<Color> colors = GraphWindow.generateDefaultColors();
		List<Color> colors = Lists.newArrayList(
				Color.BLACK, Color.BLUE, Color.RED, Color.GREEN, Color.CYAN, Color.ORANGE, Color.MAGENTA, Color.PINK, Color.YELLOW);
		Preconditions.checkState(hists.size() <= colors.size(), "Only have enough colors for "+colors.size()+" hists.");
		for (int i=0; i<stackedHists.size(); i++)
			chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, colors.get(i)));
		
		PlotSpec spec = new PlotSpec(stackedHists, chars, categoryName, xAxisName, "Density");
		spec.setLegendVisible(true);
		spec.setLegendLocation(RectangleEdge.BOTTOM);
		
		return spec;
	}

}
