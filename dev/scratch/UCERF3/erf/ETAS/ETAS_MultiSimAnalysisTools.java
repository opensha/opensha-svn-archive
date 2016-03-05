package scratch.UCERF3.erf.ETAS;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.StatUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.data.Range;
import org.jfree.ui.TextAnchor;
import org.opensha.commons.calc.FractileCurveCalculator;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.AbstractXY_DataSet;
import org.opensha.commons.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.function.UncertainArbDiscDataset;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.data.function.XY_DataSetList;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.data.xyz.GeoDataSetMath;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.eq.MagUtils;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.HeadlessGraphPanel;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.util.ComparablePairing;
import org.opensha.commons.util.DataUtils;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.commons.util.XMLUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;
import org.opensha.sha.earthquake.param.ProbabilityModelOptions;
import org.opensha.sha.earthquake.param.ProbabilityModelParam;
import org.opensha.sha.faultSurface.PointSurface;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.analysis.FaultBasedMapGen;
import scratch.UCERF3.analysis.FaultSysSolutionERF_Calc;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.erf.ETAS.ETAS_Simulator.TestScenario;
import scratch.UCERF3.erf.ETAS.ETAS_Params.ETAS_ParameterList;
import scratch.UCERF3.erf.utils.ProbabilityModelsCalc;
import scratch.UCERF3.griddedSeismicity.AbstractGridSourceProvider;
import scratch.UCERF3.griddedSeismicity.FaultPolyMgr;
import scratch.UCERF3.inversion.InversionTargetMFDs;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.UCERF3.utils.MatrixIO;
import scratch.UCERF3.utils.RELM_RegionUtils;
import scratch.kevin.ucerf3.etas.MPJ_ETAS_Simulator;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;

public class ETAS_MultiSimAnalysisTools {
	
	public static int calcNumWithMagAbove(List<List<ETAS_EqkRupture>> catalogs, double targetMinMag) {
		long ot = Math.round((2014.0-1970.0)*ProbabilityModelsCalc.MILLISEC_PER_YEAR); // occurs at 2014
		return calcNumWithMagAbove(catalogs, ot, targetMinMag, -1, -1);
	}
	
	public static int calcNumWithMagAbove(List<List<ETAS_EqkRupture>> catalogs, long ot, double targetMinMag,
			int triggerParentID, int maxDaysAfter) {
		HashSet<Integer> triggerParentIDs = null;
		if (triggerParentID >= 0) {
			triggerParentIDs = new HashSet<Integer>();
			triggerParentIDs.add(triggerParentID);
		}
		int num = 0;
		long maxEventTime;
		if (maxDaysAfter > 0)
			maxEventTime = ot + maxDaysAfter*ProbabilityModelsCalc.MILLISEC_PER_DAY;
		else
			maxEventTime = -1;
		catalogLoop:
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			for (ETAS_EqkRupture rup : catalog) {
				if (maxEventTime > 0 && rup.getOriginTime() > maxEventTime)
					break;
				boolean child = true;
				if (triggerParentID >= 0) {
					if (triggerParentIDs.contains(rup.getParentID()))
						// add this as a child
						triggerParentIDs.add(rup.getID());
					else
						// this is spontaneous or part of another chain
						child = false;
				}
				if (rup.getMag() > targetMinMag && child) {
					num++;
					continue catalogLoop;
				}
			}
		}
		String childAdd;
		if (triggerParentID >= 0)
			childAdd = " child";
		else
			childAdd = "";
		String dateAdd;
		if (maxDaysAfter > 0)
			dateAdd = " within "+maxDaysAfter+" days of start of catalog";
		else
			dateAdd = "";
		double percent = 100d*((double)num/(double)catalogs.size());
		System.out.println(num+"/"+catalogs.size()+" ("+(float)percent+" %) of catalogs had"
				+childAdd+" rup with M>"+(float)targetMinMag+dateAdd);
		return num;
	}
	
	public static List<Double> calcTotalMoments(List<List<ETAS_EqkRupture>> catalogs) {
		List<Double> ret = Lists.newArrayList();
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			ret.add(calcTotalMoment(catalog));
		}
		return ret;
	}
	
	public static double calcTotalMoment(List<ETAS_EqkRupture> catalog) {
		double moment = 0;
		for (ETAS_EqkRupture rup : catalog)
			moment += MagUtils.magToMoment(rup.getMag());
		return moment;
	}
	
	private static FractileCurveCalculator getFractileCalc(EvenlyDiscretizedFunc[] mfds) {
		XY_DataSetList funcsList = new XY_DataSetList();
		List<Double> relativeWeights = Lists.newArrayList();
		for (int i=0; i<mfds.length; i++) {
			funcsList.add(mfds[i]);
			relativeWeights.add(1d);
		}
		return new FractileCurveCalculator(funcsList, relativeWeights);
	}
	
	private static double mfdMinMag = 2.55;
	private static double mfdDelta = 0.1;
	private static int mfdNumMag = 66;
	private static double mfdMinY = 1e-4;
	private static double mfdMaxY = 1e4;
	
	private static int calcNumMagToTrim(List<List<ETAS_EqkRupture>> catalogs) {
		double minMag = mfdMinMag;
		double catMinMag = Double.POSITIVE_INFINITY;
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			for (ETAS_EqkRupture rup : catalog) {
				catMinMag = Math.min(catMinMag, rup.getMag());
			}
			if (catMinMag < minMag + 0.5*mfdDelta)
				// it's a full catalog
				return 0;
		}
		if (Double.isInfinite(catMinMag))
			throw new IllegalStateException("Empty catalogs!");
		int numToTrim = 0;
		while (catMinMag > (minMag + 0.5*mfdDelta)) {
			minMag += mfdDelta;
			numToTrim++;
		}
		return numToTrim;
	}
	
	/**
	 * Plots an MFD to compare with the expected MFD:
	 * 
	 * for each magnitude bin
	 * 		find fract sims with NO supra seis PRIMARY in that bin
	 * 		plot 1 - fract
	 * @param catalogs
	 * @param outputDir
	 * @param name
	 * @param prefix
	 * @throws IOException
	 */
	private static void plotExpectedSupraComparisonMFD(List<List<ETAS_EqkRupture>> catalogs,
			File outputDir, String name, String prefix) throws IOException {
		double minMag = mfdMinMag;
		int numMag = mfdNumMag;
		
		// see if we need to adjust
		int numToTrim = calcNumMagToTrim(catalogs);
		for (int i=0; i<numToTrim; i++) {
			minMag += mfdDelta;
			numMag--;
		}
		
		ArbIncrementalMagFreqDist mfd = new ArbIncrementalMagFreqDist(minMag, numMag, mfdDelta);
		
		double rate = 1d/catalogs.size();
		
		for (int i=0; i<catalogs.size(); i++) {
			List<ETAS_EqkRupture> catalog = catalogs.get(i);
			ArbIncrementalMagFreqDist subMFD = new ArbIncrementalMagFreqDist(minMag, numMag, mfdDelta);
			for (ETAS_EqkRupture rup : catalog) {
				if (rup.getFSSIndex() >= 0)
					subMFD.addResampledMagRate(rup.getMag(), rate, true);
			}
			for (int n=0; n<subMFD.size(); n++)
				if (subMFD.getY(n) == 0d)
					mfd.add(n, rate);
		}
		// now take 1 minus
		for (int n=0; n<mfd.size(); n++)
			mfd.set(n, 1d-mfd.getY(n));
		
		List<DiscretizedFunc> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		mfd.setName("Mean");
		funcs.add(mfd);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		
		PlotSpec spec = new PlotSpec(funcs, chars, name+" Supra MFD Compare To Expected",
				"Magnitude", "Incremental Rate (1/yr)");
//		spec.setLegendVisible(true);
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		gp.setUserBounds(mfdMinMag, mfd.getMaxX(), Math.pow(10d, Math.log10(mfdMinY)-2), Math.pow(10d, Math.log10(mfdMaxY)-2));
		
		setFontSizes(gp);
		
		gp.drawGraphPanel(spec, false, true);
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPNG(new File(outputDir, prefix+".png").getAbsolutePath());
		gp.saveAsPDF(new File(outputDir, prefix+".pdf").getAbsolutePath());
		gp.saveAsTXT(new File(outputDir, prefix+".txt").getAbsolutePath());
	}
	
	private static void setFontSizes(HeadlessGraphPanel gp) {
		setFontSizes(gp, 0);
	}
	
	private static void setFontSizes(HeadlessGraphPanel gp, int addition) {
		gp.setBackgroundColor(Color.WHITE);
		gp.setTickLabelFontSize(22+addition);
		gp.setAxisLabelFontSize(24+addition);
		gp.setPlotLabelFontSize(24+addition);
	}
	
	private static ArbIncrementalMagFreqDist[] plotMFD(List<List<ETAS_EqkRupture>> catalogs, double duration,
			FaultSystemSolutionERF erfForComparison, File outputDir, String name, String prefix) throws IOException {
//		double minMag = 5.05;
//		int numMag = 41;
//		double delta = 0.1;
//		double minY = 1e-4;
//		double maxY = 1e2;
		
		double minMag = mfdMinMag;
		int numMag = mfdNumMag;
		
		// see if we need to adjust
		int numToTrim = calcNumMagToTrim(catalogs);
		if (numToTrim > 0)
			System.out.println("Trimming "+numToTrim+" MFD bins");
		for (int i=0; i<numToTrim; i++) {
			minMag += mfdDelta;
			numMag--;
		}
		
//		ArbIncrementalMagFreqDist mfd = new ArbIncrementalMagFreqDist(minMag, numMag, mfdDelta);
//		mfd.setName("Total");
		ArbIncrementalMagFreqDist[] subMFDs = new ArbIncrementalMagFreqDist[catalogs.size()];
		EvenlyDiscretizedFunc[] cmlSubMFDs = new EvenlyDiscretizedFunc[catalogs.size()];
		for (int i=0; i<catalogs.size(); i++)
			subMFDs[i] = new ArbIncrementalMagFreqDist(minMag, numMag, mfdDelta);
		
		for (int i=0; i<catalogs.size(); i++) {
			List<ETAS_EqkRupture> catalog = catalogs.get(i);
			double myDuration;
			if (duration < 0)
				myDuration = calcDurationYears(catalog);
			else
				myDuration = duration;
			if (myDuration == 0)
				continue;
			double rateEach = 1d/myDuration;
			
			for (ETAS_EqkRupture rup : catalog) {
				subMFDs[i].addResampledMagRate(rup.getMag(), rateEach, true);
			}
//			for (int n=0; n<mfd.size(); n++)
//				mfd.add(n, subMFDs[i].getY(n)*rate);
			cmlSubMFDs[i] = subMFDs[i].getCumRateDistWithOffset();
		}
		
		if (outputDir == null)
			return subMFDs;
		
		IncrementalMagFreqDist comparisonMFD = null;
		if (erfForComparison != null)
			comparisonMFD = ERF_Calculator.getTotalMFD_ForERF(erfForComparison,
					subMFDs[0].getMinX(), subMFDs[0].getMaxX(), subMFDs[0].size(), true);
		
		boolean[] cumulatives = { false, true };
		
		for (boolean cumulative : cumulatives) {
//			EvenlyDiscretizedFunc myMFD;
			EvenlyDiscretizedFunc[] mySubMFDs;
			String yAxisLabel;
			String myPrefix = prefix;
			if (myPrefix == null || myPrefix.isEmpty())
				myPrefix = "";
			else
				myPrefix += "_";
			myPrefix += "mfd_";
			if (cumulative) {
//				myMFD = mfd.getCumRateDistWithOffset();
				mySubMFDs = cmlSubMFDs;
				yAxisLabel = "Cumulative Rate (1/yr)";
				myPrefix += "cumulative";
			} else {
//				myMFD = mfd;
				mySubMFDs = subMFDs;
				yAxisLabel = "Incremental Rate (1/yr)";
				myPrefix += "incremental";
			}
			
			List<XY_DataSet> funcs = Lists.newArrayList();
			List<PlotCurveCharacterstics> chars = Lists.newArrayList();
			
			File csvFile = new File(outputDir,  myPrefix+".csv");
			
			double[] fractiles = {0.025, 0.25, 0.75, 0.975};
			
			if (comparisonMFD != null) {
				EvenlyDiscretizedFunc comp;
				if (cumulative)
					comp = comparisonMFD.getCumRateDistWithOffset();
				else
					comp = comparisonMFD;
				comp.setName("Long Term ERF");
				funcs.add(comp);
				chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.GRAY));
			}
			
			getFractilePlotFuncs(mySubMFDs, fractiles, true, funcs, chars, csvFile);
			
			PlotSpec spec = new PlotSpec(funcs, chars, name+" MFD", "Magnitude", yAxisLabel);
			spec.setLegendVisible(true);
			
			HeadlessGraphPanel gp = new HeadlessGraphPanel();
			gp.setUserBounds(mfdMinMag, subMFDs[0].getMaxX(), mfdMinY, mfdMaxY);
			
			setFontSizes(gp);
			
			gp.drawGraphPanel(spec, false, true);
			gp.getCartPanel().setSize(1000, 800);
			gp.saveAsPNG(new File(outputDir, myPrefix+".png").getAbsolutePath());
			gp.saveAsPDF(new File(outputDir, myPrefix+".pdf").getAbsolutePath());
			gp.saveAsTXT(new File(outputDir, myPrefix+".txt").getAbsolutePath());
		}
		
		return subMFDs;
	}
	
	private static EvenlyDiscretizedFunc[] calcFractAboveZero(ArbIncrementalMagFreqDist[] subMFDs) {
		EvenlyDiscretizedFunc atFunc = new EvenlyDiscretizedFunc(
				subMFDs[0].getMinX(), subMFDs[0].getMaxX(), subMFDs[0].size());
		EvenlyDiscretizedFunc atOrAboveFunc = new EvenlyDiscretizedFunc(
				atFunc.getMinX()-atFunc.getDelta()*0.5, atFunc.size(), atFunc.getDelta());
		
		double fractEach = 1d/subMFDs.length;
		
		for (int i=0; i<subMFDs.length; i++) {
			ArbIncrementalMagFreqDist subMFD = subMFDs[i];
			int maxMagIndex = -1;
			for (int m=0; m<subMFD.size(); m++) {
				if (subMFD.getY(m) > 0) {
					atFunc.add(m, fractEach);
					maxMagIndex = m;
				}
			}
			for (int m=0; m<=maxMagIndex; m++)
				atOrAboveFunc.add(m, fractEach);
		}
		
		atFunc.setName("Fract With Mag");
		atOrAboveFunc.setName("Fract With ≥ Mag");
		
		return new EvenlyDiscretizedFunc[] {atFunc, atOrAboveFunc};
	}
	
	private static void plotFractWithMagAbove(List<List<ETAS_EqkRupture>> catalogs,
			ArbIncrementalMagFreqDist[] subMFDs, TestScenario scenario,
			File outputDir, String name, String prefix) throws IOException {
		if (subMFDs == null)
			subMFDs = plotMFD(catalogs, -1d, null, null, null, null);
		
		Preconditions.checkArgument(subMFDs.length > 0);
		Preconditions.checkArgument(subMFDs.length == catalogs.size());
		
//		double minMag = subMFDs[0].getMinX();
//		int numMag = subMFDs[0].size();
		double delta = subMFDs[0].getDelta();
		
//		EvenlyDiscretizedFunc atOrAboveFunc = new EvenlyDiscretizedFunc(minMag-delta*0.5, numMag, delta);
//		EvenlyDiscretizedFunc atFunc = new EvenlyDiscretizedFunc(minMag, numMag, delta);
		EvenlyDiscretizedFunc[] myFuncs = calcFractAboveZero(subMFDs);
		EvenlyDiscretizedFunc atFunc = myFuncs[0];
		EvenlyDiscretizedFunc atOrAboveFunc = myFuncs[1];

		double fractEach = 1d/subMFDs.length;
		double minY = Math.min(fractEach, 1d/10000d);
		
//		for (ArbIncrementalMagFreqDist subMFD : subMFDs) {
//			int maxIndex = -1;
//			for (int i=0; i<numMag; i++) {
//				if (subMFD.getY(i) > 0d) {
//					atFunc.add(i, fractEach);
//					maxIndex = i;
//				}
//			}
//			for (int i=0; i<=maxIndex; i++)
//				atOrAboveFunc.add(i, fractEach);
//		}
		
		List<XY_DataSet> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		funcs.add(atFunc);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 3f, Color.BLUE));
		
		funcs.add(atOrAboveFunc);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 3f, Color.BLACK));
		
		List<XYTextAnnotation> anns = null;
		
		if (scenario != null) {
			DefaultXY_DataSet xy = new DefaultXY_DataSet();
			double mag = scenario.getMagnitude();
			xy.setName("Scenario M="+(float)mag);
			xy.set(mag, 0d);
			xy.set(mag, minY);
			xy.set(mag, fractEach);
			xy.set(mag, 1d);
			
			funcs.add(xy);
			chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, new Color(0, 180, 0)));	
			
			double fractAboveMag = (double)calcNumWithMagAbove(catalogs, mag)/catalogs.size();
			
			DecimalFormat df = new DecimalFormat("0.#");
			XYTextAnnotation ann = new XYTextAnnotation(
					" "+df.format(fractAboveMag*100d)+"% > M"+df.format(mag), mag, fractAboveMag);
			ann.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
			ann.setTextAnchor(TextAnchor.BOTTOM_LEFT);
			Color red = new Color(180, 0, 0);
			ann.setPaint(red);
			anns = Lists.newArrayList(ann);
			
			xy = new DefaultXY_DataSet();
			xy.setName(null);
			xy.set(mag, fractAboveMag);
			
			funcs.add(xy);
			chars.add(new PlotCurveCharacterstics(PlotSymbol.FILLED_CIRCLE, 5f, red));	
		}
		
		PlotSpec spec = new PlotSpec(funcs, chars, name+" Fract With Mag", "Magnitude", "Fraction Of Simulations");
		spec.setLegendVisible(true);
		spec.setPlotAnnotations(anns);
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		gp.setUserBounds(atFunc.getMinX()-0.5*delta, atFunc.getMaxX(), minY, 1d);
		
		setFontSizes(gp);
		
		gp.drawGraphPanel(spec, false, true);
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPNG(new File(outputDir, prefix+".png").getAbsolutePath());
		gp.saveAsPDF(new File(outputDir, prefix+".pdf").getAbsolutePath());
		gp.saveAsTXT(new File(outputDir, prefix+".txt").getAbsolutePath());
	}
	
	/**
	 * Calculate the mode for a set of catalog MFDs. Works because y values are all integer values.
	 * @param allFuncs
	 * @param fractCalc
	 * @return
	 */
	private static EvenlyDiscretizedFunc getCatalogMode(EvenlyDiscretizedFunc[] allFuncs,
			FractileCurveCalculator fractCalc) {
		EvenlyDiscretizedFunc ret = new EvenlyDiscretizedFunc(
				allFuncs[0].getMinX(), allFuncs[0].size(), allFuncs[0].getDelta());
		
		for (int i=0; i<ret.size(); i++) {
			ArbDiscrEmpiricalDistFunc dist = fractCalc.getEmpiricalDist(i);
			double mode;
			if (dist.size() == 1)
				mode = dist.getX(0);
			else
				mode = dist.getMostCentralMode();
			
			ret.set(i, mode);
			
//			double x = ret.getX(i);
//			if (x == 6d || x == 5d || x == 4d) {
//				double median = fractCalc.getFractile(0.5).getY(i);
//				new GraphWindow(dist, "M"+(float)x+" Empirical Dist. Mode="+(float)mode
//						+", Mdedian="+(float)median);
//			}
		}
		
		return ret;
	}
	
	private static void plotMagNum(List<List<ETAS_EqkRupture>> catalogs,
			File outputDir, String name, String prefix) throws IOException {
		double minMag = mfdMinMag;
		int numMag = mfdNumMag;
		
		// see if we need to adjust
		int numToTrim = calcNumMagToTrim(catalogs);
		if (numToTrim > 0)
			System.out.println("Trimming "+numToTrim+" MFD bins");
		for (int i=0; i<numToTrim; i++) {
			minMag += mfdDelta;
			numMag--;
		}
		
		ArbIncrementalMagFreqDist[] subMagNums = new ArbIncrementalMagFreqDist[catalogs.size()];
		EvenlyDiscretizedFunc[] cmlSubMagNums = new EvenlyDiscretizedFunc[catalogs.size()];
		for (int i=0; i<catalogs.size(); i++)
			subMagNums[i] = new ArbIncrementalMagFreqDist(minMag, numMag, mfdDelta);
		ArbIncrementalMagFreqDist primaryMFD = new ArbIncrementalMagFreqDist(minMag, numMag, mfdDelta);
		
		double primaryNumEach = 1d/catalogs.size();
		
		for (int i=0; i<catalogs.size(); i++) {
			List<ETAS_EqkRupture> catalog = catalogs.get(i);
			
			for (ETAS_EqkRupture rup : catalog) {
				subMagNums[i].addResampledMagRate(rup.getMag(), 1d, true);
				int gen = rup.getGeneration();
				Preconditions.checkState(gen != 0, "This catalog has spontaneous events!");
				if (gen == 1)
					primaryMFD.addResampledMagRate(rup.getMag(), primaryNumEach, true);
			}
			cmlSubMagNums[i] = subMagNums[i].getCumRateDistWithOffset();
		}
		
		boolean[] cumulatives = { false, true };
		
		EvenlyDiscretizedFunc[] myFuncs = calcFractAboveZero(subMagNums);
		EvenlyDiscretizedFunc atFunc = myFuncs[0];
		EvenlyDiscretizedFunc atOrAboveFunc = myFuncs[1];
		
		for (boolean cumulative : cumulatives) {
			EvenlyDiscretizedFunc[] mySubMagNums;
			String yAxisLabel;
			String myPrefix = prefix;
			if (myPrefix == null || myPrefix.isEmpty())
				myPrefix = "";
			else
				myPrefix += "_";
			myPrefix += "mag_num_";
			EvenlyDiscretizedFunc myAtFunc;
			EvenlyDiscretizedFunc myPrimaryFunc;
			if (cumulative) {
//				myMFD = mfd.getCumRateDistWithOffset();
				myPrimaryFunc = primaryMFD.getCumRateDistWithOffset();
				myPrimaryFunc.setName("Primary");
				mySubMagNums = cmlSubMagNums;
				yAxisLabel = "Cumulative Number";
				myPrefix += "cumulative";
				myAtFunc = atOrAboveFunc;
			} else {
//				myMFD = mfd;
				myPrimaryFunc = primaryMFD;
				myPrimaryFunc.setName("Primary");
				mySubMagNums = subMagNums;
				yAxisLabel = "Incremental Number";
				myPrefix += "incremental";
				myAtFunc = atFunc;
			}
			
			List<XY_DataSet> funcs = Lists.newArrayList();
			List<PlotCurveCharacterstics> chars = Lists.newArrayList();
			
			File csvFile = new File(outputDir,  myPrefix+".csv");
			
			double[] fractiles = {0.025, 0.975};
			
//			getFractilePlotFuncs(mySubMagNums, fractiles, true, funcs, chars, csvFile);
			funcs.add(myPrimaryFunc);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.GREEN.darker()));
			
			getFractilePlotFuncs(mySubMagNums, fractiles, funcs, chars, csvFile,
					Color.BLACK, Color.BLUE, Color.CYAN, null, myAtFunc, myPrimaryFunc);
			
			funcs.add(myAtFunc);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.RED));
			
			for (PlotCurveCharacterstics theChar : chars)
				theChar.setLineWidth(theChar.getLineWidth()*2f);
			
			PlotSpec spec = new PlotSpec(funcs, chars, name, "Magnitude", yAxisLabel);
			spec.setLegendVisible(true);
			
			HeadlessGraphPanel gp = new HeadlessGraphPanel();
			gp.setUserBounds(myPrimaryFunc.getMinX(), subMagNums[0].getMaxX(), mfdMinY, mfdMaxY);
			gp.setLegendFontSize(20);
			
			setFontSizes(gp, 10);
			
			gp.drawGraphPanel(spec, false, true);
			gp.getCartPanel().setSize(1000, 800);
			gp.saveAsPNG(new File(outputDir, myPrefix+".png").getAbsolutePath());
			gp.saveAsPDF(new File(outputDir, myPrefix+".pdf").getAbsolutePath());
			gp.saveAsTXT(new File(outputDir, myPrefix+".txt").getAbsolutePath());
		}
	}
	
	private static void getFractilePlotFuncs(EvenlyDiscretizedFunc[] allFuncs, double[] fractiles,
			boolean mode, List<XY_DataSet> funcs, List<PlotCurveCharacterstics> chars, File csvFile)
			throws IOException {
		Color fractileColor = Color.GREEN.darker();
		Color medianColor = Color.BLUE;
		Color modeColor = mode ? Color.CYAN : null;
		Color sdomColor = Color.RED.darker();
		getFractilePlotFuncs(allFuncs, fractiles, funcs, chars, csvFile,
				fractileColor, medianColor, modeColor, sdomColor);
	}

	private static void getFractilePlotFuncs(EvenlyDiscretizedFunc[] allFuncs, double[] fractiles,
			List<XY_DataSet> funcs, List<PlotCurveCharacterstics> chars, File csvFile,
			Color fractileColor, Color medianColor, Color modeColor, Color sdomColor,
			EvenlyDiscretizedFunc... otherCSVFuncs) throws IOException {
		FractileCurveCalculator fractCalc = getFractileCalc(allFuncs);
		List<AbstractXY_DataSet> fractileFuncs = Lists.newArrayList();
		List<String> fractileNames = Lists.newArrayList();
		for (int i=0; i<fractiles.length; i++)
			fractileNames.add((float)(fractiles[i]*100d)+"%");
		
		for (int i=0; i<fractiles.length; i++) {
			double fractile = fractiles[i];
			AbstractXY_DataSet fractFunc = fractCalc.getFractile(fractile);
			fractileFuncs.add(fractFunc);
			if (fractFunc instanceof IncrementalMagFreqDist) {
				// nasty hack to fix default naming of MFD functions when you set name to null
				IncrementalMagFreqDist mfd = (IncrementalMagFreqDist)fractFunc;
				EvenlyDiscretizedFunc newFractFunc = new EvenlyDiscretizedFunc(mfd.getMinX(), mfd.getMaxX(), mfd.size());
				for (int j=0; j<mfd.size(); j++)
					newFractFunc.set(j, mfd.getY(j));
				fractFunc = newFractFunc;
			}
			
//			String nameAdd;
//			if (i == 0 || i == fractiles.length-1)
//				nameAdd = " Fractile";
//			else
//				nameAdd = "";
			
			if (i == 0)
				fractFunc.setName(Joiner.on(",").join(fractileNames)+" Fractiles");
			else
				fractFunc.setName(null);
//			fractFunc.setName((float)(fractile*100d)+"%"+nameAdd);
			funcs.add(fractFunc);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, fractileColor));
		}
		
		AbstractXY_DataSet median = fractCalc.getFractile(0.5);
		median.setName("Median");
		if (medianColor != null) {
			funcs.add(median);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, medianColor));
		}
		
		int numX = allFuncs[0].size();
		
		// will be added later
		AbstractXY_DataSet meanFunc = fractCalc.getMeanCurve();
		
		AbstractXY_DataSet modeFunc = null;
		if (modeColor != null) {
			modeFunc = getCatalogMode(allFuncs, fractCalc);
			modeFunc.setName("Mode");
			funcs.add(modeFunc);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, modeColor));
		}
		
		double[] stdDevs = new double[numX];
		double[] sdoms = new double[numX];
		EvenlyDiscretizedFunc lower95_mean = new EvenlyDiscretizedFunc(
				allFuncs[0].getMinX(), numX, allFuncs[0].getDelta());
		lower95_mean.setName("Lower/Upper 95% of Mean");
		EvenlyDiscretizedFunc upper95_mean = new EvenlyDiscretizedFunc(
				allFuncs[0].getMinX(), numX, allFuncs[0].getDelta());
//		upper95_mean.setName("Upper 95% of Mean");
		upper95_mean.setName(null);
		
		for (int n=0; n<numX; n++) {
			double[] vals = new double[allFuncs.length];
			for (int i=0; i<allFuncs.length; i++)
				vals[i] = allFuncs[i].getY(n);
			stdDevs[n] = Math.sqrt(StatUtils.variance(vals));
			sdoms[n] = stdDevs[n]/Math.sqrt(allFuncs.length);
			
			double mean = meanFunc.getY(n);
			lower95_mean.set(n, mean - 1.98*sdoms[n]);
			upper95_mean.set(n, mean + 1.98*sdoms[n]);
		}
		if (sdomColor != null) {
			funcs.add(lower95_mean);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, sdomColor));
			funcs.add(upper95_mean);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, sdomColor));
		}
		
		meanFunc.setName("Mean");
		funcs.add(meanFunc);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		
		if (csvFile != null) {
			CSVFile<String> csv = new CSVFile<String>(true);
			List<String> header = Lists.newArrayList("Mag", "Mean", "Std Dev", "SDOM");
			for (double fract : fractiles)
				header.add("p"+(float)(fract*100d)+"%");
			header.add("Median");
			if (modeFunc != null)
				header.add("Mode");
			header.add("Lower 95% of Mean");
			header.add("Upper 95% of Mean");
			for (EvenlyDiscretizedFunc otherFunc : otherCSVFuncs) {
				Preconditions.checkState(otherFunc.size() == meanFunc.size(), "Other func name mismatch");
				Preconditions.checkNotNull(otherFunc.getName(), "Other func must be named for CSV header");
				header.add(otherFunc.getName());
			}
			csv.addLine(header);
			
			// now mean and std dev
			for (int n=0; n<meanFunc.size(); n++) {
				List<String> line = Lists.newArrayList(meanFunc.getX(n)+"", meanFunc.getY(n)+"",
						stdDevs[n]+"", sdoms[n]+"");
				for (AbstractXY_DataSet fractFunc : fractileFuncs)
					line.add(fractFunc.getY(n)+"");
				line.add(median.getY(n)+"");
				if (modeFunc != null)
					line.add(modeFunc.getY(n)+"");
				line.add(lower95_mean.getY(n)+"");
				line.add(upper95_mean.getY(n)+"");
				for (EvenlyDiscretizedFunc otherFunc : otherCSVFuncs)
					line.add(otherFunc.getY(n)+"");
				
				csv.addLine(line);
			}
			
			csv.writeToFile(csvFile);
		}
	}
	
	public static void plotAftershockRateVsLogTimeHistForRup(List<List<ETAS_EqkRupture>> catalogs,
			TestScenario scenario, ETAS_ParameterList params, long rupOT_millis, File outputDir,
			String name, String prefix) throws IOException {
		EvenlyDiscretizedFunc[] funcsArray = new EvenlyDiscretizedFunc[catalogs.size()];
		
		double firstLogDay = -5;
		double lastLogDay = 5;
		double deltaLogDay =0.2;
		
		for (int i=0; i<catalogs.size(); i++) {
			List<ETAS_EqkRupture> catalog = catalogs.get(i);
			
			funcsArray[i] = ETAS_SimAnalysisTools.getAftershockRateVsLogTimeHistForRup(
					catalog, 0, rupOT_millis, firstLogDay, lastLogDay, deltaLogDay);
		}
		
		List<XY_DataSet> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		double[] fractiles = {0.025, 0.25, 0.75, 0.975};
		
		getFractilePlotFuncs(funcsArray, fractiles, false, funcs, chars, null);
		
		if (params != null && scenario != null) {
			HistogramFunction targetFunc = ETAS_Utils.getRateWithLogTimeFunc(params.get_k(), params.get_p(),
					scenario.getMagnitude(), ETAS_Utils.magMin_DEFAULT, params.get_c(), firstLogDay, lastLogDay, deltaLogDay);
			targetFunc.setName("Expected");
			
			funcs.add(targetFunc);
			chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.GRAY));
		}
		
		double maxY = 0;
		for (XY_DataSet xy : funcs)
			maxY = Math.max(maxY, xy.getMaxY());
		
		PlotSpec spec = new PlotSpec(funcs, chars, name+" Temporal Decay", "Log10(Days)", "Rate (per day)");
		spec.setLegendVisible(true);
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		gp.setUserBounds(-4d, 3d, 1e-3, maxY*1.2);
		
		setFontSizes(gp);
		
		gp.drawGraphPanel(spec, false, true);
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPNG(new File(outputDir, prefix+".png").getAbsolutePath());
		gp.saveAsPDF(new File(outputDir, prefix+".pdf").getAbsolutePath());
		gp.saveAsTXT(new File(outputDir, prefix+".txt").getAbsolutePath());
	}
	
	public static void plotDistDecay(List<List<ETAS_EqkRupture>> catalogs, ETAS_ParameterList params,
			RuptureSurface surf, File outputDir, String name, String prefix) throws IOException {
		EvenlyDiscretizedFunc[] funcsArray = new EvenlyDiscretizedFunc[catalogs.size()];
		
		double histLogMin = -1.5;
		double histLogMax = 4.0;
		double histLogDelta = 0.2;
		
		for (int i=0; i<catalogs.size(); i++) {
			List<ETAS_EqkRupture> catalog = catalogs.get(i);
			
			if (surf == null)
				funcsArray[i] = ETAS_SimAnalysisTools.getLogTriggerDistDecayDensityHist(
					catalog, histLogMin, histLogMax, histLogDelta);
			else
				funcsArray[i] = ETAS_SimAnalysisTools.getLogDistDecayDensityFromRupSurfaceHist(
						catalog, surf, histLogMin, histLogMax, histLogDelta);
		}
		
		List<XY_DataSet> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		double[] fractiles = {0.025, 0.25, 0.75, 0.975};
		
		getFractilePlotFuncs(funcsArray, fractiles, false, funcs, chars, null);
		
		if (params != null) {
			double distDecay = params.get_q();
			double minDist = params.get_d();
			
			EvenlyDiscretizedFunc expectedLogDistDecay = ETAS_Utils.getTargetDistDecayDensityFunc(
					funcsArray[0].getMinX(), funcsArray[0].getMaxX(), funcsArray[0].size(), distDecay, minDist);
			expectedLogDistDecay.setName("Expected");
			expectedLogDistDecay.setInfo("(distDecay="+distDecay+" and minDist="+minDist+")");
			
			funcs.add(expectedLogDistDecay);
			chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.GRAY));
		}
		
		double maxY = 0;
		double minY = Double.POSITIVE_INFINITY;
		for (XY_DataSet xy : funcs) {
			maxY = Math.max(maxY, xy.getMaxY());
			double minNonZero = Double.POSITIVE_INFINITY;
			for (Point2D pt : xy)
				if (pt.getY() > 0 && pt.getY() < minNonZero)
					minNonZero = pt.getY();
			if (!Double.isInfinite(minNonZero))
				minY = Math.min(minY, minNonZero);
		}
		
		String title;
		if (surf == null)
			title = name+" Trigger Loc Dist Decay";
		else
			title = name+" Rupture Surface Dist Decay";
		
		PlotSpec spec = new PlotSpec(funcs, chars, title,
				"Log10(Distance) (km)", "Aftershock Density (per km)");
		spec.setLegendVisible(true);
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		
		gp.setUserBounds(histLogMin+0.5*histLogDelta, 3, minY, maxY*1.2);
		
		setFontSizes(gp);
		
		gp.drawGraphPanel(spec, false, true);
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPNG(new File(outputDir, prefix+".png").getAbsolutePath());
		gp.saveAsPDF(new File(outputDir, prefix+".pdf").getAbsolutePath());
		gp.saveAsTXT(new File(outputDir, prefix+".txt").getAbsolutePath());
	}
	
	private static void plotNumEventsHistogram(List<List<ETAS_EqkRupture>> catalogs, File outputDir, String prefix)
			throws IOException {
		MinMaxAveTracker track = new MinMaxAveTracker();
		
		for (List<ETAS_EqkRupture> catalog : catalogs)
			track.addValue(catalog.size());
		
		
		HistogramFunction hist = HistogramFunction.getEncompassingHistogram(track.getMin(), track.getMax(), 5000d);
		
		for (List<ETAS_EqkRupture> catalog : catalogs)
			hist.add((double)catalog.size(), 1d);
		
		List<DiscretizedFunc> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		funcs.add(hist);
		chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.BLACK));
		
		PlotSpec spec = new PlotSpec(funcs, chars, "# Events Distribution", "# Events", "# Catalogs");
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		
		setFontSizes(gp);
		
		gp.drawGraphPanel(spec, false, false);
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPNG(new File(outputDir, prefix+".png").getAbsolutePath());
		gp.saveAsPDF(new File(outputDir, prefix+".pdf").getAbsolutePath());
		gp.saveAsTXT(new File(outputDir, prefix+".txt").getAbsolutePath());
	}
	
	private static void plotTotalMomentHistogram(List<List<ETAS_EqkRupture>> catalogs, File outputDir, String prefix)
			throws IOException {
		
		double[] moments = new double[catalogs.size()];
		
		for (int i=0; i<catalogs.size(); i++)
			for (ETAS_EqkRupture rup : catalogs.get(i))
				moments[i] += MagUtils.magToMoment(rup.getMag());
		
		double[] log10Moments = new double[moments.length];
		for (int i=0; i<moments.length; i++)
			log10Moments[i] = Math.log10(moments[i]);
		
		HistogramFunction hist = HistogramFunction.getEncompassingHistogram(
				StatUtils.min(log10Moments), StatUtils.max(log10Moments), 0.05);
		
		for (double val : log10Moments)
			hist.add(val, 1d);
		
		List<DiscretizedFunc> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		funcs.add(hist);
		chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.BLACK));
		
		PlotSpec spec = new PlotSpec(funcs, chars, "Moment Distribution", "Log10(Total Moment) (N-m)", "# Catalogs");
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		
		setFontSizes(gp);
		
		gp.drawGraphPanel(spec, false, false);
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPNG(new File(outputDir, prefix+".png").getAbsolutePath());
		gp.saveAsPDF(new File(outputDir, prefix+".pdf").getAbsolutePath());
		gp.saveAsTXT(new File(outputDir, prefix+".txt").getAbsolutePath());
	}
	
	/**
	 * Plot section participation and trigger (nucleation) rates for each fault subsection. All supraseismic events
	 * will be included, so if only children or primary events are wanted then it should be filtered externally. 
	 * @param catalogs ETAS catalogs
	 * @param rupSet rupSet
	 * @param minMags array of minimum magnitudes
	 * @param outputDir directory in which to write plots
	 * @param title title for the map
	 * @param prefix file name prefix
	 * @throws IOException 
	 * @throws RuntimeException 
	 * @throws GMT_MapException 
	 */
	public static void plotSectRates(List<List<ETAS_EqkRupture>> catalogs, double duration, FaultSystemRupSet rupSet,
			double[] minMags, File outputDir, String title, String prefix)
					throws IOException, GMT_MapException, RuntimeException {
		List<double[]> particRatesList = Lists.newArrayList();
		for (int i=0; i<minMags.length; i++)
			particRatesList.add(new double[rupSet.getNumSections()]);
		List<double[]> triggerRatesList = Lists.newArrayList();
		for (int i=0; i<minMags.length; i++)
			triggerRatesList.add(new double[rupSet.getNumSections()]);
		
		Map<Integer, List<Location>> locsForSectsMap = Maps.newHashMap();
		
		HashSet<Integer> debugSections = null;
//		debugSections = new HashSet<Integer>(rupSet.getSectionsIndicesForRup(193821));
		
		double maxDuration = 0;
		FaultPolyMgr faultPolyMgr = FaultPolyMgr.create(rupSet.getFaultSectionDataList(), InversionTargetMFDs.FAULT_BUFFER);

		for (List<ETAS_EqkRupture> catalog : catalogs) {
			double myDuration;
			if (duration < 0)
				myDuration = calcDurationYears(catalog);
			else
				myDuration = duration;
			if (myDuration == 0)
				continue;
			maxDuration = Math.max(maxDuration, myDuration);
			double fractionalRate = 1d/(catalogs.size()*myDuration);
			for (ETAS_EqkRupture rup : catalog) {
				int rupIndex = rup.getFSSIndex();
				if (rupIndex < 0)
					// not supra-seismogenic
					continue;
				int closestSectIndex = -1;
				boolean notYetFound = true;
				
				Location hypocenter = rup.getHypocenterLocation();
				Preconditions.checkNotNull(hypocenter);
				
				for (int sectIndex : rupSet.getSectionsIndicesForRup(rupIndex)) {
					for (int i=0; i<minMags.length; i++)
						if (rup.getMag() >= minMags[i])
							particRatesList.get(i)[sectIndex] += fractionalRate;
					
					// TODO This isn't quite right because more than one section polygon might contain the hypocenter; 
					// this will end up taking the first one.  I believe the only way to do this right is to save 
					// the info in the first place
					if(notYetFound && faultPolyMgr.getPoly(sectIndex).contains(hypocenter)) {
						closestSectIndex = sectIndex;
						notYetFound=false;
					}
					
//					// now calculate distance
//					List<Location> surfLocs = locsForSectsMap.get(sectIndex);
//					if (surfLocs == null) {
//						// first time we have encountered this section
//						FaultSectionPrefData sect = rupSet.getFaultSectionData(sectIndex);
//						surfLocs = sect.getStirlingGriddedSurface(1d, false, true).getEvenlyDiscritizedPerimeter();
//						locsForSectsMap.put(sectIndex, surfLocs);
//					}
//					
//					for (Location loc : surfLocs) {
//						double dist = LocationUtils.linearDistanceFast(hypocenter, loc);
//						if (dist < closestDist) {
//							closestDist = dist;
//							closestSectIndex = sectIndex;
//						}
//					}
				}
				
				if (closestSectIndex  < 0) {
					// fall back to distance calculation - polygon precision issues
					double closestDist = Double.POSITIVE_INFINITY;
					for (int sectIndex : rupSet.getSectionsIndicesForRup(rupIndex)) {
						// now calculate distance
						List<Location> surfLocs = locsForSectsMap.get(sectIndex);
						if (surfLocs == null) {
							// first time we have encountered this section
							FaultSectionPrefData sect = rupSet.getFaultSectionData(sectIndex);
							surfLocs = sect.getStirlingGriddedSurface(1d, false, true).getEvenlyDiscritizedPerimeter();
							locsForSectsMap.put(sectIndex, surfLocs);
						}
						
						for (Location loc : surfLocs) {
							double dist = LocationUtils.linearDistanceFast(hypocenter, loc);
							if (dist < closestDist) {
								closestDist = dist;
								closestSectIndex = sectIndex;
							}
						}
					}
					Preconditions.checkState(closestDist < 0.1d,
							"reverted to distance due to polygon issue but too far from perimeter: %s km", closestDist);
				}
				
				Preconditions.checkState(closestSectIndex >= 0, "fssIndex=%s, hypo=%s", rup.getFSSIndex(), hypocenter);
				for (int i=0; i<minMags.length; i++)
					if (rup.getMag() >= minMags[i])
						triggerRatesList.get(i)[closestSectIndex] += fractionalRate;
				if (debugSections != null && debugSections.contains(closestSectIndex))
					System.out.println("Ruptured "+closestSectIndex+":\t"+ETAS_CatalogIO.getEventFileLine(rup));
			}
		}
		
		CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance();
		if (maxDuration == 0d)
			return;
		double maxRate = 0;
		for (double[] particRates : particRatesList)
			maxRate = Math.max(maxRate, StatUtils.max(particRates));
		double fractionalRate = 1d/Math.max(1d, Math.round(catalogs.size()*maxDuration));
		double cptMin = Math.log10(fractionalRate);
		double cptMax = Math.ceil(Math.log10(maxRate));
		if (!Doubles.isFinite(cptMin) || !Doubles.isFinite(cptMax))
			return;
		while (cptMax <= cptMin)
			cptMax++;
		cpt = cpt.rescale(cptMin, cptMax);
		cpt.setBelowMinColor(Color.LIGHT_GRAY);
		
		List<LocationList> faults = Lists.newArrayList();
		for (int sectIndex=0; sectIndex<rupSet.getNumSections(); sectIndex++)
			faults.add(rupSet.getFaultSectionData(sectIndex).getFaultTrace());
		
		Region region = new CaliforniaRegions.RELM_TESTING();
		
		for (int i=0; i<minMags.length; i++) {
			double[] particRates = particRatesList.get(i);
			double[] triggerRates = triggerRatesList.get(i);
			
			String titleAdd;
			String prefixAdd;
			
			if (minMags[i] > 1) {
				titleAdd = " M>="+(float)minMags[i];
				prefixAdd = "_m"+(float)minMags[i];
			} else {
				titleAdd = "";
				prefixAdd = "";
			}
			
			FaultBasedMapGen.makeFaultPlot(cpt, faults, FaultBasedMapGen.log10(particRates), region, outputDir,
					prefix+"_partic"+prefixAdd,false, false, title+titleAdd+" Partic. Rate");
			
			FaultBasedMapGen.makeFaultPlot(cpt, faults, FaultBasedMapGen.log10(triggerRates), region, outputDir,
					prefix+"_trigger"+prefixAdd, false, false, title+titleAdd+" Trigger Rate");
		}
	}
	
	private static void plotMaxTriggeredMagHist(List<List<ETAS_EqkRupture>> catalogs,
			List<List<ETAS_EqkRupture>> primaryCatalogs, TestScenario scenario, File outputDir, String name, String prefix)
					throws IOException {
		HistogramFunction fullHist = HistogramFunction.getEncompassingHistogram(2.5, 9d, 0.1);
		HistogramFunction primaryHist = null;
		if (primaryCatalogs != null)
			primaryHist = new HistogramFunction(fullHist.getMinX(), fullHist.size(), fullHist.getDelta());
		
		int numEmpty = 0;
		
		List<Double> maxMags = Lists.newArrayList();
		for (int i=0; i<catalogs.size(); i++) {
			if (catalogs.get(i).isEmpty())
				numEmpty++;
			else
				maxMags.add(ETAS_SimAnalysisTools.getMaxMag(catalogs.get(i)));
		}
		populateHistWithInfo(fullHist, Doubles.toArray(maxMags));
		
		int numPrimaryEmpty = 0;
		if (primaryCatalogs != null) {
			maxMags = Lists.newArrayList();
			for (int i=0; i<primaryCatalogs.size(); i++) {
				if (primaryCatalogs.get(i).isEmpty())
					numPrimaryEmpty++;
				else
					maxMags.add(ETAS_SimAnalysisTools.getMaxMag(primaryCatalogs.get(i)));
			}
			populateHistWithInfo(primaryHist, Doubles.toArray(maxMags));
		}
		
		double maxY = fullHist.getMaxY()*1.1;
		
		List<XY_DataSet> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		funcs.add(fullHist);
		chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.BLACK));
		fullHist.setName("All Children");
		
		if (primaryHist != null) {
			funcs.add(primaryHist);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 3f, Color.CYAN));
			primaryHist.setName("Primary Aftershocks");
		}
		
		if (scenario != null) {
			XY_DataSet scenarioMag = new DefaultXY_DataSet();
			scenarioMag.set(scenario.getMagnitude(), 0d);
			scenarioMag.set(scenario.getMagnitude(), fullHist.getMaxY());
			scenarioMag.set(scenario.getMagnitude(), maxY);
			scenarioMag.setName("Scenario M="+(float)scenario.getMagnitude());
			
			funcs.add(scenarioMag);
			chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 3f, Color.GRAY));
		}
		
		PlotSpec spec = new PlotSpec(funcs, chars, name+" Max Mag Hist", "Magnitude", "Num Simulations");
		spec.setLegendVisible(true);
		
		double histDelta = fullHist.getDelta();
		double minX = fullHist.getMinX()-0.5*histDelta;
		double maxX = fullHist.getMaxX()-0.5*histDelta;
		
		if (numEmpty > 0 || numPrimaryEmpty > 0) {
			String primaryStr = "";
			if (numPrimaryEmpty > 0) {
				primaryStr = numPrimaryEmpty+"/"+primaryCatalogs.size()+" primary";
				if (numEmpty > 0)
					primaryStr = " ("+primaryStr+")";
			}
			String text;
			if (numEmpty > 0)
				text = numEmpty+"/"+catalogs.size()+primaryStr;
			else
				text = primaryStr;
			text += " catalogs empty and excluded";
			XYTextAnnotation ann = new XYTextAnnotation(text, minX+(maxX-minX)*0.05, maxY*0.95);
			ann.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
			ann.setTextAnchor(TextAnchor.TOP_LEFT);
			List<XYTextAnnotation> anns = Lists.newArrayList(ann);
			spec.setPlotAnnotations(anns);
		}
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		
		setFontSizes(gp);
		
		gp.drawGraphPanel(spec, false, false);
		gp.drawGraphPanel(spec, false, false, new Range(minX, maxX), new Range(0, maxY));
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPNG(new File(outputDir, prefix+".png").getAbsolutePath());
		gp.saveAsPDF(new File(outputDir, prefix+".pdf").getAbsolutePath());
		gp.saveAsTXT(new File(outputDir, prefix+".txt").getAbsolutePath());
	}
	
	private static void plotNumEventsPerGeneration(List<List<ETAS_EqkRupture>> catalogs,
			File outputDir, String name, String prefix) throws IOException {
		int maxGeneration = 20;
		
		EvenlyDiscretizedFunc[] allFuncs = new EvenlyDiscretizedFunc[catalogs.size()];
		boolean hasZero = false;
		for (int i=0; i<catalogs.size(); i++) {
			List<ETAS_EqkRupture> catalog = catalogs.get(i);
			int[] counts = ETAS_SimAnalysisTools.getNumAftershocksForEachGeneration(catalog, maxGeneration);
			allFuncs[i] = new EvenlyDiscretizedFunc(0, (double)maxGeneration, counts.length);
			for (int j=0; j<counts.length; j++)
				allFuncs[i].set(j, counts[j]);
			hasZero = hasZero || counts[0] > 0;
		}
		
		double[] fractiles = { 0.025, 0.975 };
		
		List<XY_DataSet> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		getFractilePlotFuncs(allFuncs, fractiles, true, funcs, chars, null);
		
		double maxY = 0;
		for (XY_DataSet func : funcs)
			maxY = Math.max(maxY, func.getMaxY());
		
		PlotSpec spec = new PlotSpec(funcs, chars, name+" Generations", "Generation", "Count");
		spec.setLegendVisible(true);
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		double minX;
		if (hasZero)
			minX = 0;
		else
			minX = 1;
		gp.setUserBounds(minX, maxGeneration, 0, maxY*1.1);
		
		setFontSizes(gp);
		
		gp.drawGraphPanel(spec, false, false);
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPNG(new File(outputDir, prefix+".png").getAbsolutePath());
		gp.saveAsPDF(new File(outputDir, prefix+".pdf").getAbsolutePath());
		gp.saveAsTXT(new File(outputDir, prefix+".txt").getAbsolutePath());
	}
	
	/**
	 * Fills the histogram with the given data and puts statistics in the info string
	 * @param hist
	 * @param data
	 */
	private static void populateHistWithInfo(HistogramFunction hist, double[] data) {
		for (double val : data)
			hist.add(val, 1d);
		
		double mean = StatUtils.mean(data);
		double median = DataUtils.median(data);
		double mode = hist.getX(hist.getXindexForMaxY());
		
		String infoStr = "Mean: "+(float)mean+"\nMedian: "+(float)median+"\nMode: "+(float)mode;
		
		hist.setInfo(infoStr);
	}
	
	private static final double MILLIS_PER_YEAR = 365.25*24*60*60*1000;
	
	private static void plotCubeNucleationRates(List<List<ETAS_EqkRupture>> catalogs, double duration,
			File outputDir, String name, String prefix, double[] mags)
					throws IOException, GMT_MapException {
		double discr = 0.02;
		GriddedRegion reg = new GriddedRegion(new CaliforniaRegions.RELM_TESTING(),
				discr, GriddedRegion.ANCHOR_0_0);
		
		GriddedGeoDataSet[] xyzs = new GriddedGeoDataSet[mags.length];
		for (int i=0; i<xyzs.length; i++)
			xyzs[i] = new GriddedGeoDataSet(reg, false);
		
		int numSkipped = 0;
		
		double maxDuration = 0d;
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			double myDuration;
			if (duration < 0)
				myDuration = calcDurationYears(catalog);
			else
				myDuration = duration;
			if (myDuration == 0)
				continue;
			maxDuration = Math.max(myDuration, maxDuration);
			double rateEach = 1d/(catalogs.size()*myDuration);
			
			for (ETAS_EqkRupture rup : catalog) {
				double mag = rup.getMag();
				Location loc = rup.getHypocenterLocation();
				int index = reg.indexForLocation(loc);
//				Preconditions.checkState(index > 0);
				if (index < 0) {
					numSkipped++;
					continue;
				}
				for (int i=0; i<mags.length; i++)
					if (mag >= mags[i])
						xyzs[i].set(index, xyzs[i].get(index)+rateEach);
			}
		}
		
		System.out.println("Skipped "+numSkipped+" events outside of region");
		
		double scalar = 1d/(catalogs.size()*Math.max(1d, Math.round(maxDuration)));
//		for (GriddedGeoDataSet xyz : xyzs) // now done earlier
//			xyz.scale(scalar);
		
		// now log10
		for (GriddedGeoDataSet xyz : xyzs)
			xyz.log10();
		
		CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance();
		
		Region plotReg = new Region(new Location(reg.getMinGridLat(), reg.getMinGridLon()),
				new Location(reg.getMaxGridLat(), reg.getMaxGridLon()));
		
		for (int i=0; i<mags.length; i++) {
			GriddedGeoDataSet xyz = xyzs[i];
			
			double minZ = Math.floor(Math.log10(scalar));
			double maxZ = Math.ceil(xyz.getMaxZ());
			if (xyz.getMaxZ() == Double.NEGATIVE_INFINITY)
				maxZ = minZ+4;
			if (maxZ == minZ)
				maxZ++;
			
			Preconditions.checkState(minZ < maxZ, "minZ=%s >= maxZ=%s", minZ, maxZ);
			
			double mag = mags[i];
			String label = "Log10("+name+" M>="+(float)mag+" Nucleation Rate)";
			FaultBasedMapGen.plotMap(outputDir, prefix+"_m"+(float)mag, false,
					FaultBasedMapGen.buildMap(cpt.rescale(minZ, maxZ), null, null,
							xyzs[i], discr, plotReg, false, label));
		}
	}
	
	private static List<List<ETAS_EqkRupture>> getOnlyAftershocksFromHistorical(List<List<ETAS_EqkRupture>> catalogs) {
		List<List<ETAS_EqkRupture>> ret = Lists.newArrayList();
		
		long countHist = 0l;
		long countAll = 0l;
		
		for (List<ETAS_EqkRupture> catalog : catalogs) {
//			HashSet<Integer> eventIDs = new HashSet<Integer>();
			// detect if an event is an aftershock from a historical event by the fact that
			// it will have a parent whose ID is less than the minimum ID in the catalog
			int minIndex = Integer.MAX_VALUE;
			for (ETAS_EqkRupture rup : catalog)
				if (rup.getID() < minIndex)
					minIndex = rup.getID();
			
			List<ETAS_EqkRupture> hist = Lists.newArrayList();
			
			for (ETAS_EqkRupture rup : catalog) {
				if (rup.getParentID() >= 0 && rup.getParentID() < minIndex)
					hist.add(rup);
			}
			
			ret.add(hist);
			countHist += hist.size();
			countAll += catalog.size();
		}
		
		double percent = 100d*((double)countHist/(double)countAll);
		
		System.out.println(countHist+"/"+countAll+" ("+(float)percent+" %) are historical aftershocks");
		
		return ret;
	}
	
	/**
	 * This gets a list of historical earthquake descendants for each catalog (event is included if it's most distant 
	 * relative is an historic event, where the latter have  event IDs that are less than the least value in the catalog)
	 * @param catalogs
	 * @return
	 */
	private static List<List<ETAS_EqkRupture>> getAllDescendentsFromHistorical(List<List<ETAS_EqkRupture>> catalogs) {
		List<List<ETAS_EqkRupture>> ret = Lists.newArrayList();
		
		long countHist = 0l;
		long countAll = 0l;
		
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			HashMap<Integer,ETAS_EqkRupture> map = new HashMap<Integer,ETAS_EqkRupture>();
			// detect if an event is an aftershock from a historical event by the fact that
			// it will have a parent whose ID is less than the minimum ID in the catalog
			int minIndex = Integer.MAX_VALUE;
			for (ETAS_EqkRupture rup : catalog) {
				map.put(rup.getID(),rup);
				if (rup.getID() < minIndex)
					minIndex = rup.getID();
			}
			
			List<ETAS_EqkRupture> hist = Lists.newArrayList();
			
			for (ETAS_EqkRupture rup : catalog) {
				int parID = rup.getParentID();
				int currentID = rup.getID();
				while(parID != -1) {	// find the oldest descendant
					currentID=parID;
					if(currentID<minIndex)	// break because currentID won't be in the catalog
						break;
					parID = map.get(currentID).getParentID();
				}
				if (currentID < minIndex)
					hist.add(rup);
			}
			
			ret.add(hist);
			countHist += hist.size();
			countAll += catalog.size();
		}
		
		double percent = 100d*((double)countHist/(double)countAll);
		
		System.out.println(countHist+"/"+countAll+" ("+(float)percent+" %) are historical aftershocks");
		
		return ret;
	}

	
	private static void writeTimeFromPrevSupraHist(List<List<ETAS_EqkRupture>> catalogs, File outputDir) throws IOException {
		HistogramFunction hist = HistogramFunction.getEncompassingHistogram(0d, 20d, 1d);
		
		List<Double> allVals = Lists.newArrayList();
		
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			if (catalog.size() < 2)
				continue;
//			long durationMillis = catalog.get(catalog.size()-1).getOriginTime() - catalog.get(0).getOriginTime();
//			double myDuration = (double)durationMillis/MILLIS_PER_YEAR;
			long startTime = catalog.get(0).getOriginTime();
			long endTime = catalog.get(catalog.size()-1).getOriginTime();
			long durationMillis = endTime - startTime;
			Preconditions.checkState(durationMillis > 0l);
			
			int num = 10000;
			long delta = durationMillis / num;
			Preconditions.checkState(delta > 0);
			
			int catIndexBeforeTime = 0;
			long prevSupra = Long.MIN_VALUE;
			for (long time=startTime; time<endTime; time+=delta) {
				for (int i=catIndexBeforeTime; i<catalog.size(); i++) {
					ETAS_EqkRupture e = catalog.get(i);
					if (e.getOriginTime() > time)
						break;
					catIndexBeforeTime = i;
					if (e.getFSSIndex() >= 0)
						prevSupra = e.getOriginTime();
				}
				if (prevSupra > Long.MIN_VALUE) {
					long curDelta = time - prevSupra;
					Preconditions.checkState(curDelta >= 0);
					double curDeltaYears = curDelta / MILLIS_PER_YEAR;
					if (curDeltaYears > hist.getMaxX())
						hist.add(hist.size()-1, 1d);
					else
						hist.add(curDeltaYears, 1d);
					allVals.add(curDeltaYears);
				}
			}
		}
		hist.normalizeBySumOfY_Vals();
		
		HistogramFunction cmlHist = new HistogramFunction(hist.getMinX()-hist.getDelta()*0.5, hist.size(), hist.getDelta());
		double cmlVal = 0d;
		for (int i=hist.size(); --i>=0;) {
			double val = hist.getY(i);
			cmlVal += val;
			cmlHist.set(i, cmlVal);
		}
		
		List<XY_DataSet> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		funcs.add(hist);
		chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.BLACK));
		hist.setName("Histogram");
		
		funcs.add(cmlHist);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 3f, Color.GRAY));
		cmlHist.setName("Cumulative (≥) Histogram");
		
		double[] allValsArray = Doubles.toArray(allVals);
		double mean = StatUtils.mean(allValsArray);
		double median = DataUtils.median(allValsArray);
		
		XY_DataSet meanLine = new DefaultXY_DataSet();
		meanLine.set(mean, 0d);
		meanLine.set(mean, 1d);
		meanLine.setName("Mean="+(float)mean);
		
		funcs.add(meanLine);
		chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 3f, Color.BLUE));
		
		XY_DataSet medianLine = new DefaultXY_DataSet();
		medianLine.set(median, 0d);
		medianLine.set(median, 1d);
		medianLine.setName("Median="+(float)median);
		
		funcs.add(medianLine);
		chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 3f, Color.GREEN.darker()));
		
		PlotSpec spec = new PlotSpec(funcs, chars, "Time Since Last Supra-Seosmogenic Hist", "Time (years)", "Density");
		spec.setLegendVisible(true);
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		
		setFontSizes(gp);
		
		gp.drawGraphPanel(spec, false, false, new Range(0, hist.getMaxX()+0.5*hist.getDelta()), new Range(0d, 1d));
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPNG(new File(outputDir, "time_since_last_supra.png").getAbsolutePath());
		gp.saveAsPDF(new File(outputDir, "time_since_last_supra.pdf").getAbsolutePath());
		gp.saveAsTXT(new File(outputDir, "time_since_last_supra.txt").getAbsolutePath());
	}
	
	/**
	 * Writes out catalogs with minimum, median, and maximum total moment release to the given directory
	 * @param catalogs
	 * @param outputDir
	 * @throws IOException 
	 */
	private static void writeCatalogsForViz(List<List<ETAS_EqkRupture>> catalogs, TestScenario scenario,
			File outputDir, int numEach) throws IOException {
		Preconditions.checkState(outputDir.exists() || outputDir.mkdir());
		
		double[] fractiles = { 0, 0.5, 1};
		
		Preconditions.checkState(catalogs.size() > numEach);
//		List<Double> sortables = calcTotalMoments(catalogs);
		List<Double> sortables = Lists.newArrayList();
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			double num = 0;
			for (ETAS_EqkRupture rup : catalog)
				if (rup.getMag() > 6.5)
					num++;
			sortables.add(num);
		}
		
		String infoStr;
		if (scenario != null) {
			if (scenario.getFSS_Index() >= 0) {
				infoStr = "FSS simulation. M="+scenario.getMagnitude()+", fss ID="+scenario.getFSS_Index();
			} else {
				Location loc = scenario.getLocation();
				infoStr = "Pt Source. M="+scenario.getMagnitude()+", "
						+loc.getLatitude()+", "+loc.getLongitude()+", "+loc.getDepth();
			}
		} else {
			infoStr = "Spontaneous events";
		}

		List<ComparablePairing<Double, List<ETAS_EqkRupture>>> pairings = ComparablePairing.build(sortables, catalogs);
		// increasing in moment
		Collections.sort(pairings);
		
		for (double fractile : fractiles) {
			int index = (int)((catalogs.size()-1)*fractile);
			while (index + numEach >= catalogs.size())
				index--;
			for (int i=0; i<numEach; i++) {
				int myIndex = index+i;
				ComparablePairing<Double, List<ETAS_EqkRupture>> pairing = pairings.get(myIndex);
				
				File subDir = new File(outputDir, "fract_"+(float)fractile+"_cat"+i);
				Preconditions.checkState(subDir.exists() || subDir.mkdir());
				
				List<ETAS_EqkRupture> catalog = pairing.getData();
				
				File infoFile = new File(subDir, "infoString.txt");
				
				FileWriter fw = new FileWriter(infoFile);
				fw.write(infoStr+"\n");
				fw.write("\n");
				fw.write("Total num ruptures: "+catalog.size()+"\n");
				fw.write("Total moment: "+pairing.getComparable()+"\n");
				fw.write("Max triggered mag: "+ETAS_SimAnalysisTools.getMaxMag(catalog)+"\n");
				fw.close();
				
				File catalogFile = new File(subDir, "simulatedEvents.txt");
				ETAS_CatalogIO.writeEventDataToFile(catalogFile, catalog);
			}
		}
	}
	
	public static void plotSectParticScatter(Iterable<List<ETAS_EqkRupture>> catalogs, double duration,
			FaultSystemSolutionERF erf, File outputDir) throws IOException, GMT_MapException, RuntimeException {
		double[] minMags = { 0d };
		
		FaultSystemRupSet rupSet = erf.getSolution().getRupSet();
		
		// this is for map plotting
		CPT logRatioCPT = FaultBasedMapGen.getLogRatioCPT().rescale(-1, 1);
		CPT diffCPT = FaultBasedMapGen.getLogRatioCPT(); // will be rescaled
		Region region = new CaliforniaRegions.RELM_TESTING();
		boolean regionFilter = false;
		
		// filter out results outside of RELM region
		List<Integer> sectsToInclude = Lists.newArrayList();
		for (FaultSectionPrefData sect : rupSet.getFaultSectionDataList()) {
			for (Location loc : sect.getFaultTrace()) {
				if (!regionFilter || region.contains(loc)) {
					sectsToInclude.add(sect.getSectionId());
					break;
				}
			}
		}
		
		List<LocationList> faults = Lists.newArrayList();
		for (int sectIndex : sectsToInclude)
			faults.add(rupSet.getFaultSectionData(sectIndex).getFaultTrace());
		
		// if we ever find a case where the parent of a rupture doesn't exist,
		// then this catalog has been filtered somehow and this will be set to false
		boolean completeCatalogs = true;
		
		for (double minMag : minMags) {
			// each "MFD" will only have one value, for this minimum mag
			double[] subSectVals = FaultSysSolutionERF_Calc.calcParticipationRateForAllSects(erf, minMag);
			
			double[] catalogVals = new double[rupSet.getNumSections()];
			
			int[] totalNumForSection = new int[rupSet.getNumSections()];
			double[] fractTriggeredForSection = new double[rupSet.getNumSections()];
			// triggered by a supra anywhere in the parent chain
			double[] fractTriggeredBySupraForSection = new double[rupSet.getNumSections()];
			double[] fractTriggeredBySubForSection = new double[rupSet.getNumSections()];
			double[] fractTriggeredByHistForSection = new double[rupSet.getNumSections()];
			
			int catalogCount = 0;
			
			for (List<ETAS_EqkRupture> catalog : catalogs) {
				if (duration < 0)
					// detect duration from catalog
					duration = calcDurationYears(catalog);
//				double rateEach = 1d/(catalogs.size()*duration);
				double rateEach = 1d/duration; // normalize by catalog count later
				catalogCount++;
				
				HashMap<Integer,ETAS_EqkRupture> idToRupMap = new HashMap<Integer,ETAS_EqkRupture>();
				int minIndex = Integer.MAX_VALUE;
				if (completeCatalogs) {
					for (ETAS_EqkRupture rup : catalog) {
						idToRupMap.put(rup.getID(),rup);
						if (rup.getID() < minIndex)
							minIndex = rup.getID();
					}
				}
				
				for (ETAS_EqkRupture rup : catalog) {
					int rupIndex = rup.getFSSIndex();
					
					if (rupIndex < 0 || rup.getMag() < minMag)
						continue;
					
					for (int sectIndex : rupSet.getSectionsIndicesForRup(rupIndex)) {
						catalogVals[sectIndex] += rateEach;
						totalNumForSection[sectIndex] += 1;
						if(rup.getGeneration()>0) {
							// it's triggered
							fractTriggeredForSection[sectIndex] += 1;
							if (completeCatalogs) {
								// only can do this if catalogs are complete (not filtered)
								boolean supra = false;
								boolean hist = false;
								ETAS_EqkRupture myRup = rup;
								while (myRup.getParentID() >= 0) {
									if (myRup.getParentID() < minIndex) {
										// historical earthquake.
										hist = true;
										break;
									}
									myRup = idToRupMap.get(myRup.getParentID());
									if (myRup == null) {
										// not a complete catalog, bail
										completeCatalogs = false;
										break;
									}
									if (myRup.getFSSIndex() >= 0) {
										// it has a supra parent!
										supra = true;
										break;
									}
								}
								if (supra)
									fractTriggeredBySupraForSection[sectIndex] += 1;
								else if (hist)
									fractTriggeredByHistForSection[sectIndex] += 1;
								else
									fractTriggeredBySubForSection[sectIndex] += 1;
							}
						}
					}
				}
			}
			
			// now normalize by number of catalogs
			for (int i=0; i<catalogVals.length; i++)
				catalogVals[i] /= (double)catalogCount;
			
			if (!completeCatalogs)
				System.out.println("Cannot compute fract triggered by supra as catalog has been magnitude filtered");
			
			for (int sectIndex=0; sectIndex<rupSet.getNumSections();sectIndex++) {
				fractTriggeredForSection[sectIndex] /= (double)totalNumForSection[sectIndex];
				if (completeCatalogs) {
					fractTriggeredBySupraForSection[sectIndex] /= (double)totalNumForSection[sectIndex];
					fractTriggeredBySubForSection[sectIndex] /= (double)totalNumForSection[sectIndex];
					fractTriggeredByHistForSection[sectIndex] /= (double)totalNumForSection[sectIndex];
				}
			}
			
			// now filter out sections outside the region
			double[] filteredCatalogVals = new double[sectsToInclude.size()];
			double[] filteredSubSectVals = new double[sectsToInclude.size()];
			double[] filteredFractTriggeredForSection = new double[sectsToInclude.size()];
			double[] filteredFractTriggeredBySupraForSection = new double[sectsToInclude.size()];
			double[] filteredFractTriggeredBySubForSection = new double[sectsToInclude.size()];
			double[] filteredFractTriggeredByHistForSection = new double[sectsToInclude.size()];
			for (int i=0; i<filteredCatalogVals.length; i++) {
				int s = sectsToInclude.get(i);
				filteredCatalogVals[i] = catalogVals[s];
				filteredSubSectVals[i] = subSectVals[s];
				filteredFractTriggeredForSection[i] = fractTriggeredForSection[s];
				if (completeCatalogs) {
					filteredFractTriggeredBySupraForSection[i] = fractTriggeredBySupraForSection[s];
					filteredFractTriggeredBySubForSection[i] = fractTriggeredBySubForSection[s];
					filteredFractTriggeredByHistForSection[i] = fractTriggeredByHistForSection[s];
				}
			}
			if (minMag == minMags[0])
				System.out.println("Filtered out "+(catalogVals.length-filteredCatalogVals.length)
						+" sects outside of region");
			catalogVals = filteredCatalogVals;
			subSectVals = filteredSubSectVals;
			fractTriggeredForSection = filteredFractTriggeredForSection;
			fractTriggeredBySupraForSection = filteredFractTriggeredBySupraForSection;
			fractTriggeredBySubForSection = filteredFractTriggeredBySubForSection;
			fractTriggeredByHistForSection = filteredFractTriggeredByHistForSection;
			
			String title = "Sub Section Participation";
			String prefix = "all_eqs_sect_partic";
			if (minMag > 0) {
				title += ", M≥"+(float)minMag;
				prefix += "_m"+(float)minMag;
			}
			
			CSVFile<String> csv = new CSVFile<String>(true);
			List<String> header = Lists.newArrayList("Sect Index", "Sect Name", "Simulation Rate",
					"Long Term Rate", "Ratio", "Difference", "Fraction Triggered");
			if (completeCatalogs) {
				header.add("Fraction Triggered By Supra-Seismo");
				header.add("Fraction Triggered By Sub-Seismo");
				header.add("Fraction Triggered By Historical");
			}
			csv.addLine(header);
			
			double[] ratio = ratio(catalogVals, subSectVals);
			double[] diff = diff(catalogVals, subSectVals);
			
			for (int i=0; i<catalogVals.length; i++) {
//if(i>=1268 && i<=1282)	// filter out Mendocino off shore subsect
//	continue;
				FaultSectionPrefData sect = rupSet.getFaultSectionData(sectsToInclude.get(i));
				String sectName = sect.getSectionName().replace(",", "_");
				List<String> line = Lists.newArrayList(i+"", sectName, catalogVals[i]+"", subSectVals[i]+"",
						ratio[i]+"", diff[i]+"", fractTriggeredForSection[i]+"");
				if (completeCatalogs) {
					line.add(fractTriggeredBySupraForSection[i]+"");
					line.add(fractTriggeredBySubForSection[i]+"");
					line.add(fractTriggeredByHistForSection[i]+"");
				}
				csv.addLine(line);
			}
			csv.writeToFile(new File(outputDir, prefix+".csv"));
			
			plotScatter(catalogVals, subSectVals, title+" Scatter", "Participation Rate",
					prefix+"_scatter", outputDir);
			
			title = title.replaceAll("≥", ">=");
			FaultBasedMapGen.makeFaultPlot(logRatioCPT, faults, FaultBasedMapGen.log10(ratio), region,
					outputDir, prefix+"_ratio", false, false, title+" Ratio");
			double maxDiff = Math.max(Math.abs(StatUtils.min(diff)), Math.abs(StatUtils.max(diff)));
			FaultBasedMapGen.makeFaultPlot(diffCPT.rescale(-maxDiff, maxDiff), faults, diff, region,
					outputDir, prefix+"_diff", false, false, title+" Diff");
		}
	}
	
	
	
	/**
	 * 
	 * Note that durationForProb is not the duration of the original simulation
	 * @param catalogs
	 * @param durationForProb
	 * @param erf - this should be the original probability model (not Poisson)
	 * @param outputDir
	 * @throws IOException
	 * @throws GMT_MapException
	 * @throws RuntimeException
	 */
	public static void plotAndWriteSectProbOneOrMoreData(Iterable<List<ETAS_EqkRupture>> catalogs, double durationForProb,
			 FaultSystemSolutionERF erf, File outputDir) throws IOException, GMT_MapException, RuntimeException {
		double[] minMags = { 0d };
		
		FaultSystemRupSet rupSet = erf.getSolution().getRupSet();
		
		// this is for map plotting
		CPT logRatioCPT = FaultBasedMapGen.getLogRatioCPT().rescale(-1, 1);
		CPT diffCPT = FaultBasedMapGen.getLogRatioCPT(); // will be rescaled
		Region region = new CaliforniaRegions.RELM_TESTING();
		boolean regionFilter = false;
		
		// filter out results outside of RELM region
		List<Integer> sectsToInclude = Lists.newArrayList();
		for (FaultSectionPrefData sect : rupSet.getFaultSectionDataList()) {
			for (Location loc : sect.getFaultTrace()) {
				if (!regionFilter || region.contains(loc)) {
					sectsToInclude.add(sect.getSectionId());
					break;
				}
			}
		}
		
		List<LocationList> faults = Lists.newArrayList();
		for (int sectIndex : sectsToInclude)
			faults.add(rupSet.getFaultSectionData(sectIndex).getFaultTrace());
		
		// if we ever find a case where the parent of a rupture doesn't exist,
		// then this catalog has been filtered somehow and this will be set to false
		boolean completeCatalogs = true;
		
		erf.getTimeSpan().setDuration(durationForProb);
		erf.updateForecast();
		
		for (double minMag : minMags) {
			// each "MFD" will only have one value, for this minimum mag
			double[] subSectEquivRateVals = FaultSysSolutionERF_Calc.calcParticipationRateForAllSects(erf, minMag);
			double[] subSectExpVals = new double[rupSet.getNumSections()];
			for(int s=0;s<subSectExpVals.length;s++)
				subSectExpVals[s] = 1.0 - Math.exp(-subSectEquivRateVals[s]*durationForProb);
			
			double[] catalogVals = new double[rupSet.getNumSections()];
			
			int[] totalNumForSection = new int[rupSet.getNumSections()];
			double[] fractTriggeredForSection = new double[rupSet.getNumSections()];
			// triggered by a supra anywhere in the parent chain
			double[] fractTriggeredBySupraForSection = new double[rupSet.getNumSections()];
			double[] fractTriggeredBySubForSection = new double[rupSet.getNumSections()];
			double[] fractTriggeredByHistForSection = new double[rupSet.getNumSections()];
			
			int catalogCount = 0;
			
			for (List<ETAS_EqkRupture> catalog : catalogs) {
				// reset time of last event on section
				double[] timeOfLastEventOnSect = new double[rupSet.getNumSections()];
				for(int i=0;i<timeOfLastEventOnSect.length;i++)
					timeOfLastEventOnSect[i] = -1;

				catalogCount++;
				
				HashMap<Integer,ETAS_EqkRupture> idToRupMap = new HashMap<Integer,ETAS_EqkRupture>();
				int minIndex = Integer.MAX_VALUE;
				if (completeCatalogs) {
					for (ETAS_EqkRupture rup : catalog) {
						idToRupMap.put(rup.getID(),rup);
						if (rup.getID() < minIndex)
							minIndex = rup.getID();
					}
				}
				
				for (ETAS_EqkRupture rup : catalog) {
					int rupIndex = rup.getFSSIndex();
					
					if (rupIndex < 0 || rup.getMag() < minMag)
						continue;
					
					double eventTime = calcEventTimeYears(catalog, rup);
					
					for (int sectIndex : rupSet.getSectionsIndicesForRup(rupIndex)) {
						if(timeOfLastEventOnSect[sectIndex] == -1) {	// the first occurrence on section
							if(eventTime<durationForProb) {	// if happened within durationForProb
								catalogVals[sectIndex] += 1;
								totalNumForSection[sectIndex] += 1;
								if(rup.getGeneration()>0) {
									// it's triggered
									fractTriggeredForSection[sectIndex] += 1;
									if (completeCatalogs) {
										// only can do this if catalogs are complete (not filtered)
										boolean supra = false;
										boolean hist = false;
										ETAS_EqkRupture myRup = rup;
										while (myRup.getParentID() >= 0) {
											if (myRup.getParentID() < minIndex) {
												// historical earthquake.
												hist = true;
												break;
											}
											myRup = idToRupMap.get(myRup.getParentID());
											if (myRup == null) {
												// not a complete catalog, bail
												completeCatalogs = false;
												break;
											}
											if (myRup.getFSSIndex() >= 0) {
												// it has a supra parent!
												supra = true;
												break;
											}
										}
										if (supra)
											fractTriggeredBySupraForSection[sectIndex] += 1;
										else if (hist)
											fractTriggeredByHistForSection[sectIndex] += 1;
										else
											fractTriggeredBySubForSection[sectIndex] += 1;
									}
								}
							}
						}
						timeOfLastEventOnSect[sectIndex] = eventTime;
					}
				}
			}
			
			// now normalize by number of catalogs
			for (int i=0; i<catalogVals.length; i++)
				catalogVals[i] /= (double)catalogCount;
			
			
			
			// Compute the standard deviation of the mean
			double[] catalogValsStdom = new double[rupSet.getNumSections()];
			for (List<ETAS_EqkRupture> catalog : catalogs) {
				// reset time of last event on section
				double[] gotOneOnSectForCat = new double[rupSet.getNumSections()];
				for (ETAS_EqkRupture rup : catalog) {
					int rupIndex = rup.getFSSIndex();
					if (rupIndex < 0 || rup.getMag() < minMag)
						continue;
					double eventTime = calcEventTimeYears(catalog, rup);
					for (int sectIndex : rupSet.getSectionsIndicesForRup(rupIndex)) {
						if(eventTime<durationForProb) {	// if happened within durationForProb
							gotOneOnSectForCat[sectIndex] = 1;
						}
					}
				}
				for(int s=0;s<gotOneOnSectForCat.length;s++) {
					catalogValsStdom[s] += Math.pow(gotOneOnSectForCat[s]-catalogVals[s], 2d); // square the diff from mean
				}
			}
			// convert to stdom by dividing by N (assumes N is large); stdev is divided by sqrt(N-1), and divide by sqrt(N) again fro stdom
			for(int s=0;s<catalogValsStdom.length;s++) {
				catalogValsStdom[s] /= (double)catalogCount; 
			}
			

			
			if (!completeCatalogs)
				System.out.println("Cannot compute fract triggered by supra as catalog has been magnitude filtered");
			
			for (int sectIndex=0; sectIndex<rupSet.getNumSections();sectIndex++) {
				fractTriggeredForSection[sectIndex] /= (double)totalNumForSection[sectIndex];
				if (completeCatalogs) {
					fractTriggeredBySupraForSection[sectIndex] /= (double)totalNumForSection[sectIndex];
					fractTriggeredBySubForSection[sectIndex] /= (double)totalNumForSection[sectIndex];
					fractTriggeredByHistForSection[sectIndex] /= (double)totalNumForSection[sectIndex];
				}
			}
			
			// now filter out sections outside the region
			double[] filteredCatalogVals = new double[sectsToInclude.size()];
			double[] filteredSubSectVals = new double[sectsToInclude.size()];
			double[] filteredFractTriggeredForSection = new double[sectsToInclude.size()];
			double[] filteredFractTriggeredBySupraForSection = new double[sectsToInclude.size()];
			double[] filteredFractTriggeredBySubForSection = new double[sectsToInclude.size()];
			double[] filteredFractTriggeredByHistForSection = new double[sectsToInclude.size()];
			for (int i=0; i<filteredCatalogVals.length; i++) {
				int s = sectsToInclude.get(i);
				filteredCatalogVals[i] = catalogVals[s];
				filteredSubSectVals[i] = subSectExpVals[s];
				filteredFractTriggeredForSection[i] = fractTriggeredForSection[s];
				if (completeCatalogs) {
					filteredFractTriggeredBySupraForSection[i] = fractTriggeredBySupraForSection[s];
					filteredFractTriggeredBySubForSection[i] = fractTriggeredBySubForSection[s];
					filteredFractTriggeredByHistForSection[i] = fractTriggeredByHistForSection[s];
				}
			}
			if (minMag == minMags[0])
				System.out.println("Filtered out "+(catalogVals.length-filteredCatalogVals.length)
						+" sects outside of region");
			catalogVals = filteredCatalogVals;
			subSectExpVals = filteredSubSectVals;
			fractTriggeredForSection = filteredFractTriggeredForSection;
			fractTriggeredBySupraForSection = filteredFractTriggeredBySupraForSection;
			fractTriggeredBySubForSection = filteredFractTriggeredBySubForSection;
			fractTriggeredByHistForSection = filteredFractTriggeredByHistForSection;
			
			String title = "Sub Section Prob One Or More";
			String prefix = "all_eqs_sect_prob1orMore";
			if (minMag > 0) {
				title += ", M≥"+(float)minMag;
				prefix += "_m"+(float)minMag;
			}
			
			CSVFile<String> csv = new CSVFile<String>(true);
			List<String> header = Lists.newArrayList("SectIndex", "SectName", "Simulation Prob1orMore",
					"ExpectedProb1orMore", "Ratio", "Difference", "FractTriggered", "SimProb1orMoreStdom", "StdomNormDiff");
			if (completeCatalogs) {
				header.add("FractTrigBySupraSeis");
				header.add("FractTrigBySubSeism");
				header.add("FractTrigByHistQk");
			}
			csv.addLine(header);
			
			double[] ratio = ratio(catalogVals, subSectExpVals);
			double[] diff = diff(catalogVals, subSectExpVals);
			
			// this is now just mean corrected difference (normalizing by stdom wasn't helpful)
			double[] stdomNormDiff = new double[ratio.length];
			double meanRatio=0d;
			for(int i=0;i<ratio.length;i++)
				meanRatio += ratio[i]/ratio.length;
			System.out.println("meanRatio="+meanRatio);
			for(int i=0;i<ratio.length;i++)
				stdomNormDiff[i] = (catalogVals[i]-meanRatio*subSectExpVals[i]);///catalogValsStdom[i];

			
			for (int i=0; i<catalogVals.length; i++) {
//if(i>=1268 && i<=1282)	// filter out Mendocino off shore subsect
//	continue;
				FaultSectionPrefData sect = rupSet.getFaultSectionData(sectsToInclude.get(i));
				String sectName = sect.getSectionName().replace(",", "_");
				List<String> line = Lists.newArrayList(i+"", sectName, catalogVals[i]+"", subSectExpVals[i]+"",
						ratio[i]+"", diff[i]+"", fractTriggeredForSection[i]+"", catalogValsStdom[i]+""
						, stdomNormDiff[i]+"");
				if (completeCatalogs) {
					line.add(fractTriggeredBySupraForSection[i]+"");
					line.add(fractTriggeredBySubForSection[i]+"");
					line.add(fractTriggeredByHistForSection[i]+"");
				}
				csv.addLine(line);
			}
			csv.writeToFile(new File(outputDir, prefix+".csv"));
			
			plotScatter(catalogVals, subSectExpVals, title+" Scatter", "Prob One Or More",
					prefix+"_scatter", outputDir);
			
			title = title.replaceAll("≥", ">=");
			FaultBasedMapGen.makeFaultPlot(logRatioCPT, faults, FaultBasedMapGen.log10(ratio), region,
					outputDir, prefix+"_ratio", false, false, title+" Ratio");
			double maxDiff = Math.max(Math.abs(StatUtils.min(diff)), Math.abs(StatUtils.max(diff)));
			FaultBasedMapGen.makeFaultPlot(diffCPT.rescale(-maxDiff, maxDiff), faults, diff, region,
					outputDir, prefix+"_diff", false, false, title+" Diff");
			FaultBasedMapGen.makeFaultPlot(diffCPT.rescale(-maxDiff/2, maxDiff/2), faults, stdomNormDiff, region,
					outputDir, prefix+"_meanCorrectedDiff", false, false, title+" MeanCorrectedDiff");
			for(int i=0;i<ratio.length;i++) {
				if(subSectExpVals[i]<3e-3)
					ratio[i]=1d;
			}
			FaultBasedMapGen.makeFaultPlot(logRatioCPT, faults, FaultBasedMapGen.log10(ratio), region,
					outputDir, prefix+"_ratioFilteredProbGt0pt003", false, false, title+" Ratio");

		}
	}
	

	
	
	
	public static void plotBinnedSectParticRateVsExpRate(List<List<ETAS_EqkRupture>> catalogs, double duration,
			FaultSystemSolutionERF erf, File outputDir, String prefix) throws IOException, GMT_MapException, RuntimeException {

		FaultSystemRupSet rupSet = erf.getSolution().getRupSet();

		// this is used to fliter Mendocino sections
		List<? extends IncrementalMagFreqDist> longTermSubSeisMFD_OnSectList = erf.getSolution().getSubSeismoOnFaultMFD_List();

		HistogramFunction aveValsFunc = new HistogramFunction(-4.9, 19, 0.2);
		HistogramFunction numValsFunc = new HistogramFunction(-4.9, 19, 0.2);
		//		System.out.println(aveValsFunc.toString());

		double[] subSectExpVals = FaultSysSolutionERF_Calc.calcParticipationRateForAllSects(erf, 0.0);

		int[] totalNumForSection = new int[rupSet.getNumSections()];

		int catalogCount = 0;
		

		double[] catalogVals = new double[rupSet.getNumSections()];

		for (List<ETAS_EqkRupture> catalog : catalogs) {
			if (duration < 0)
				duration = calcDurationYears(catalog);
			double rateEach = 1d/duration; // normalize by catalog count later
			catalogCount++;


			for (ETAS_EqkRupture rup : catalog) {
				int rupIndex = rup.getFSSIndex();

				if (rupIndex < 0)
					continue;

				for (int sectIndex : rupSet.getSectionsIndicesForRup(rupIndex)) {
					catalogVals[sectIndex] += rateEach;
					totalNumForSection[sectIndex] += 1;
				}
			}


		}


		for(int s=0;s<subSectExpVals.length;s++) {
			if(longTermSubSeisMFD_OnSectList.get(s).getTotalIncrRate() == 0)
				System.out.println(erf.getSolution().getRupSet().getFaultSectionData(s).getName());
			if(catalogVals[s]>0 && longTermSubSeisMFD_OnSectList.get(s).getTotalIncrRate()>0) {
				catalogVals[s] /= catalogCount;	// ave rate over all catalogs
				double logExpVal = Math.log10(subSectExpVals[s]);
				double logSimVal = Math.log10(catalogVals[s]);
				if(logExpVal>aveValsFunc.getMinX()-aveValsFunc.getDelta()/2) {
					aveValsFunc.add(logExpVal,logSimVal);
					numValsFunc.add(logExpVal,1.0);
				}
			}
		}			

		DefaultXY_DataSet resultsFunc = new DefaultXY_DataSet();
		for(int i=0;i<numValsFunc.size();i++) {
			double num = numValsFunc.getY(i);
			if(num>0)
				resultsFunc.set(Math.pow(10, aveValsFunc.getX(i)),Math.pow(10, aveValsFunc.getY(i)/num));
			//					aveValsFunc.set(i,aveValsFunc.getY(i)/num);
		}

		String title = "Binned Sub Section Participation";

		List<XY_DataSet> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();

		DefaultXY_DataSet line = new DefaultXY_DataSet();
		line.set(1e-6, 1e-6);
		line.set(0.1, 0.1);
		funcs.add(line);
		chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.GRAY));

		funcs.add(resultsFunc);
		resultsFunc.setName("Binned observed sections rates vs expected rates");
		resultsFunc.setInfo(resultsFunc.toString());
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 3f, Color.BLACK));

		PlotSpec spec = new PlotSpec(funcs, chars, title, "Long Term Rate", "Binned Simulation Rate");
		//spec.setLegendVisible(true);

		HeadlessGraphPanel gp = new HeadlessGraphPanel();

		setFontSizes(gp);

		Range range = new Range(1e-6,0.1);

		gp.drawGraphPanel(spec, true, true, range, range);
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPNG(new File(outputDir, prefix+".png").getAbsolutePath());
		gp.saveAsPDF(new File(outputDir, prefix+".pdf").getAbsolutePath());
		gp.saveAsTXT(new File(outputDir, prefix+".txt").getAbsolutePath());
	}

	public static void calcSupraAncestorStats(Iterable<List<ETAS_EqkRupture>> catalogs, File outputDir) throws IOException {
		// total
		long numSupra = 0;
		
		// sub counts
		long numSupraSpontaneous = 0; // spontaneous
		long numSupraTriggeredSupra = 0; // have at least one supra ancestor
		long numSupraTriggeredHist = 0; // have a historical ancestor and no supra's in-between
		long numSupraTriggeredOther = 0; // should be numSupra minus the total of the above, as a check
		
		long numSupraTriggeredSupraDirect = 0; // special one for directly triggered by a supra
		
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			HashMap<Integer,ETAS_EqkRupture> idToRupMap = new HashMap<Integer,ETAS_EqkRupture>();
			int minIndex = Integer.MAX_VALUE;
			for (ETAS_EqkRupture rup : catalog) {
				idToRupMap.put(rup.getID(),rup);
				if (rup.getID() < minIndex)
					minIndex = rup.getID();
			}
			
			for (ETAS_EqkRupture rup : catalog) {
				if (rup.getFSSIndex() < 0)
					// only consider supra
					continue;
				
				numSupra++;
				
				if (rup.getParentID() < 0) {
					// spontaneous
					numSupraSpontaneous++;
				} else {
					// only can do this if catalogs are complete (not filtered)
					boolean supra = false;
					boolean hist = false;
					ETAS_EqkRupture myRup = rup;
					while (myRup.getParentID() >= 0) {
						if (myRup.getParentID() < minIndex) {
							// historical earthquake.
							hist = true;
							break;
						}
						myRup = idToRupMap.get(myRup.getParentID());
						Preconditions.checkState(myRup != null,
								"This isn't a complete catalog (was filtered), cannot track ancestors");
						if (myRup.getFSSIndex() >= 0) {
							// it has a supra parent!
							supra = true;
							// check if this is the first parent
							if (rup.getParentID() == myRup.getID())
								numSupraTriggeredSupraDirect++;
							break;
						}
					}
					if (supra)
						numSupraTriggeredSupra++;
					else if (hist)
						numSupraTriggeredHist++;
					else
						numSupraTriggeredOther++;
				}
			}
		}
		
		Preconditions.checkState(numSupraTriggeredOther ==
				(numSupra - numSupraSpontaneous - numSupraTriggeredSupra - numSupraTriggeredHist));
		
		String text = "Supra-seismogenic rupture ancestors:\n";
		text += "\tTotal num supra: "+numSupra+"\n";
		text += "\tNum spontaneous supra: "+numSupraSpontaneous+"\n";
		text += "\tNum with supra ancestor: "+numSupraTriggeredSupra
				+" ("+numSupraTriggeredSupraDirect+" of which were direct supra triggers)\n";
		text += "\tNum with hist (and no supra) ancestor: "+numSupraTriggeredHist+"\n";
		text += "\tNum triggered other: "+numSupraTriggeredOther+"\n";
		text += "\n";
		double fractSupraSpontaneous = (double)numSupraSpontaneous/(double)numSupra;
		double fractSupraTriggeredSupra = (double)numSupraTriggeredSupra/(double)numSupra;
		double fractSupraTriggeredSupraDirect = (double)numSupraTriggeredSupraDirect/(double)numSupraTriggeredSupra;
		double fractSupraTriggeredHist = (double)numSupraTriggeredHist/(double)numSupra;
		double fractSupraTriggeredOther = (double)numSupraTriggeredOther/(double)numSupra;
		text += "\tFractions: "+numSupra+"\n";
		text += "\tFract spontaneous supra: "+fractSupraSpontaneous+"\n";
		text += "\tFract with supra ancestor: "+fractSupraTriggeredSupra
				+" ("+fractSupraTriggeredSupraDirect+" of which were direct supra triggers)\n";
		text += "\tFract with hist (and no supra) ancestor: "+fractSupraTriggeredHist+"\n";
		text += "\tFract triggered other: "+fractSupraTriggeredOther+"\n";
		
		System.out.println(text);
		if (outputDir != null) {
			File outputFile = new File(outputDir, "supra_ancestor_stats.txt");
			FileWriter fw = new FileWriter(outputFile);
			fw.write(text);
			fw.close();
		}
	}
	
	private static double[] diff(double[] data1, double[] data2) {
		double[] diff = new double[data1.length];
		for (int i=0; i<data1.length; i++)
			diff[i] = data1[i] - data2[i];
		return diff;
	}
	
	private static double[] ratio(double[] data1, double[] data2) {
		double[] ratio = new double[data1.length];
		for (int i=0; i<data1.length; i++)
			ratio[i] = data1[i]/data2[i];
		return ratio;
	}
	
	private static void plotGriddedNucleationScatter(List<List<ETAS_EqkRupture>> catalogs, double duration,
			FaultSystemSolutionERF erf, File outputDir) throws IOException, GMT_MapException {
		double[] minMags = { 5d };
		
		GriddedRegion reg = RELM_RegionUtils.getGriddedRegionInstance();
		
		// this is for map plotting
		CPT logRatioCPT = FaultBasedMapGen.getLogRatioCPT().rescale(-2, 2);
		CPT diffCPT = FaultBasedMapGen.getLogRatioCPT(); // will be rescaled
		
		for (double minMag : minMags) {
			// each "MFD" will only have one value, for this minimum mag
//			double[] subSectVals = FaultSysSolutionERF_Calc.calcParticipationRateForAllSects(erf, minMag);
			GriddedGeoDataSet longTermRates = ERF_Calculator.getNucleationRatesInRegion(erf, reg, minMag, 10d);
			GriddedGeoDataSet catalogRates = new GriddedGeoDataSet(reg, longTermRates.isLatitudeX());
			
			double[] longTermVals = new double[reg.getNodeCount()];
			double[] catalogVals = new double[reg.getNodeCount()];
			
			for (int i=0; i<reg.getNodeCount(); i++)
				longTermVals[i] = longTermRates.get(i);
			
			for (List<ETAS_EqkRupture> catalog : catalogs) {
				if (duration < 0)
					// detect duration from catalog
					duration = calcDurationYears(catalog);
				double rateEach = 1d/(catalogs.size()*duration);
				
				for (ETAS_EqkRupture rup : catalog) {
					if (rup.getMag() < minMag)
						continue;
					Location hypo = rup.getHypocenterLocation();
					int index = reg.indexForLocation(hypo);
					if (index < 0)
						// outside of region
						continue;
					
					catalogVals[index] += rateEach;
				}
			}
			
			for (int i=0; i<catalogVals.length; i++)
				catalogRates.set(i, catalogVals[i]);
			
			String title = "Gridded Nucleation";
			String prefix = "all_eqs_gridded_nucl";
			if (minMag > 0) {
				title += ", M≥"+(float)minMag;
				prefix += "_m"+(float)minMag;
			}
			
			CSVFile<String> csv = new CSVFile<String>(true);
			csv.addLine("Node Index", "Latitude", "Longitude", "Simulation Rate", "Long Term Rate",
					"Ratio", "Difference");
			
			double[] ratio = ratio(catalogVals, longTermVals);
			double[] diff = diff(catalogVals, longTermVals);
			
			LocationList nodeList = reg.getNodeList();
			for (int i=0; i<reg.getNodeCount(); i++) {
				Location loc = nodeList.get(i);
				
				csv.addLine(i+"", loc.getLatitude()+"", loc.getLongitude()+"",
						catalogVals[i]+"", longTermVals[i]+"", ratio[i]+"", diff[i]+"");
			}
			csv.writeToFile(new File(outputDir, prefix+".csv"));
			
			plotScatter(catalogVals, longTermVals, title+" Scatter", "Nucleation Rate", prefix+"_scatter", outputDir);
			
			GeoDataSet ratioData = GeoDataSetMath.divide(catalogRates, longTermRates);
			ratioData.log10();
			GeoDataSet diffData = GeoDataSetMath.subtract(catalogRates, longTermRates);
			title = title.replaceAll("≥", ">=");
			FaultBasedMapGen.plotMap(outputDir, prefix+"_ratio", false,
					FaultBasedMapGen.buildMap(logRatioCPT, null, null,
							ratioData, reg.getLatSpacing(), reg,
							false, title+" Ratio"));
			double maxDiff = Math.max(Math.abs(StatUtils.min(diff)), Math.abs(StatUtils.max(diff)));
			FaultBasedMapGen.plotMap(outputDir, prefix+"_diff", false,
					FaultBasedMapGen.buildMap(diffCPT.rescale(-maxDiff, maxDiff), null, null,
							diffData, reg.getLatSpacing(), reg,
							false, title+" Diff"));
		}
	}
	
	private static void plotScatter(double[] simulationData, double[] longTermData, String title, String quantity,
			String prefix, File outputDir) throws IOException {
		Preconditions.checkArgument(simulationData.length == longTermData.length);
		DefaultXY_DataSet scatter = new DefaultXY_DataSet();
		DefaultXY_DataSet simZeroScatter = new DefaultXY_DataSet();
		
		int bothZeroCount = 0;
		
		for (int i=0; i<simulationData.length; i++) {
			if (simulationData[i] > 0)
				scatter.set(longTermData[i], simulationData[i]);
			else if (longTermData[i] > 0)
				simZeroScatter.set(longTermData[i], simulationData[i]);
			else
				bothZeroCount++;
		}
		
		System.out.println("Raw scatter range: "+scatter.getMinX()+", "+scatter.getMaxX()
				+", "+scatter.getMinY()+", "+scatter.getMaxY());
		System.out.println("Raw scatter range: "+simZeroScatter.getMinX()+", "+simZeroScatter.getMaxX()
				+", "+simZeroScatter.getMinY()+", "+simZeroScatter.getMaxY());
		
		double minRate = Math.min(scatter.getMinX(), scatter.getMinY());
		double maxRate = Math.max(scatter.getMaxX(), scatter.getMaxY());
		if (simZeroScatter.size() > 0) {
			minRate = Math.min(minRate, simZeroScatter.getMinX());
			maxRate = Math.max(maxRate, simZeroScatter.getMaxX());
		}
		if (maxRate == minRate)
			maxRate *= 10d;
		
		System.out.println("minRate="+minRate+", maxRate="+maxRate);
		
		Range range = new Range(Math.pow(10d, Math.floor(Math.log10(minRate))),
				Math.pow(10d, Math.ceil(Math.log10(maxRate))));
		
		
		List<XY_DataSet> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		DefaultXY_DataSet line = new DefaultXY_DataSet();
		line.set(range.getLowerBound(), range.getLowerBound());
		line.set(range.getUpperBound(), range.getUpperBound());
		funcs.add(line);
		chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.GRAY));
		
		funcs.add(scatter);
		scatter.setName(scatter.size()+"/"+simulationData.length+" Both Nonzero");
		chars.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 3f, Color.BLACK));
		
		if (simZeroScatter.size() > 0) {
			// plot subsections with zero rate in simulations as a different color at the bottom of the plot
			// (otherwise they would be hidden below the plot since we're in log space)
			
			// move it over to the bottom of the plot
			DefaultXY_DataSet modZeroScatter = new DefaultXY_DataSet();
			for (int i=0; i<simZeroScatter.size(); i++)
				modZeroScatter.set(simZeroScatter.getX(i), range.getLowerBound());
			funcs.add(modZeroScatter);
			modZeroScatter.setName(modZeroScatter.size()+"/"+simulationData.length+" Zero In Simulations");
			chars.add(new PlotCurveCharacterstics(PlotSymbol.X, 3f, Color.RED));
			
			System.out.println(simZeroScatter.size()+"/"+simulationData.length
					+" values are zero in simulations and nonzero long term");
		}
		if (bothZeroCount > 0)
			System.out.println(bothZeroCount+"/"+simulationData.length
					+" values are zero in both simulations and long term");
		
		PlotSpec spec = new PlotSpec(funcs, chars, title, "Long Term "+quantity, "Simulation "+quantity);
		if (simZeroScatter.size() > 0)
			spec.setLegendVisible(true);
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		
		setFontSizes(gp);
		
		gp.drawGraphPanel(spec, true, true, range, range);
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPNG(new File(outputDir, prefix+".png").getAbsolutePath());
		gp.saveAsPDF(new File(outputDir, prefix+".pdf").getAbsolutePath());
		gp.saveAsTXT(new File(outputDir, prefix+".txt").getAbsolutePath());
	}
	
	private static void plotStationarity(List<List<ETAS_EqkRupture>> catalogs, double duration,
			 File outputDir) throws IOException {
		if (duration < 0) {
			for (List<ETAS_EqkRupture> catalog : catalogs)
				duration = Math.max(duration, calcDurationYears(catalog));
		}
		
		double delta;
		if (duration <= 200d)
			delta = 10d;
		else if (duration > 5000d)
			delta = 200d;
		else if (duration > 1000d)
			delta = 100d;
		else
			delta = 50d;
		
		double histDuration;
		if (duration > 5000)
			histDuration = duration * 0.995;
		else
			histDuration = duration * 0.98;
		HistogramFunction xVals = HistogramFunction.getEncompassingHistogram(0d, histDuration, delta);
		Preconditions.checkState(xVals.size() > 1);
		
//		double binRateEach = 1d/(catalogs.size()*delta);
		double annualRateEachBin = 1d/delta;
		
		double[][] momRatesEach = new double[xVals.size()][catalogs.size()];
		double[][] m5RatesEach = new double[xVals.size()][catalogs.size()];
		
		for (int i=0; i<catalogs.size(); i++) {
			List<ETAS_EqkRupture> catalog = catalogs.get(i);
			for (ETAS_EqkRupture rup : catalog) {
				double rupTimeYears = calcEventTimeYears(catalog, rup);
				
				int xIndex = xVals.getXIndex(rupTimeYears);
				if (xIndex < 0) {
					System.out.println("What? bad x index: "+xIndex);
					System.out.println("Rup time: "+rupTimeYears);
					System.out.println("Catalog Start Time: "+calcEventTimeYears(catalog, catalog.get(0)));
					System.out.println("Hist first bin: "+xVals.getMinX()+" (delta="+delta+")");
				}
				Preconditions.checkState(xIndex >= 0);
				
				if (rup.getMag() >= 5d)
					m5RatesEach[xIndex][i] += annualRateEachBin;
				double moment = MagUtils.magToMoment(rup.getMag());
				momRatesEach[xIndex][i] += moment*annualRateEachBin;
			}
		}
		
		EvenlyDiscretizedFunc momRateMean = new EvenlyDiscretizedFunc(xVals.getMinX(), xVals.getMaxX(), xVals.size());
		EvenlyDiscretizedFunc momRateLower = new EvenlyDiscretizedFunc(xVals.getMinX(), xVals.getMaxX(), xVals.size());
		EvenlyDiscretizedFunc momRateUpper = new EvenlyDiscretizedFunc(xVals.getMinX(), xVals.getMaxX(), xVals.size());
		for (int i=0; i<xVals.size(); i++) {
			double mean = StatUtils.mean(momRatesEach[i]);
			double lower = StatUtils.percentile(momRatesEach[i], 2.5);
			double upper = StatUtils.percentile(momRatesEach[i], 97.5);
			
			momRateMean.set(i, mean);
			momRateLower.set(i, lower);
			momRateUpper.set(i, upper);
		}
		UncertainArbDiscDataset momRateDataset = new UncertainArbDiscDataset(momRateMean, momRateLower, momRateUpper);
		
		EvenlyDiscretizedFunc m5RateMean = new EvenlyDiscretizedFunc(xVals.getMinX(), xVals.getMaxX(), xVals.size());
		EvenlyDiscretizedFunc m5RateLower = new EvenlyDiscretizedFunc(xVals.getMinX(), xVals.getMaxX(), xVals.size());
		EvenlyDiscretizedFunc m5RateUpper = new EvenlyDiscretizedFunc(xVals.getMinX(), xVals.getMaxX(), xVals.size());
		for (int i=0; i<xVals.size(); i++) {
			double mean = StatUtils.mean(m5RatesEach[i]);
			double lower = StatUtils.percentile(m5RatesEach[i], 2.5);
			double upper = StatUtils.percentile(m5RatesEach[i], 97.5);
			
			m5RateMean.set(i, mean);
			m5RateLower.set(i, lower);
			m5RateUpper.set(i, upper);
		}
		UncertainArbDiscDataset m5RateDataset = new UncertainArbDiscDataset(m5RateMean, m5RateLower, m5RateUpper);
		
		List<DiscretizedFunc> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		funcs.add(momRateDataset);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SHADED_UNCERTAIN_TRANS, 1f, Color.BLUE));
		funcs.add(momRateDataset);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, PlotSymbol.FILLED_CIRCLE, 4f, Color.BLACK));
		String prefix = "stationariy_mom_rate";
		
		PlotSpec spec = new PlotSpec(funcs, chars, "Moment Rate Over Time ("+(int)delta+"yr bins)",
				"Years", "Annual Moment Rate (N-m)");
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		
		setFontSizes(gp);
		
		gp.drawGraphPanel(spec, false, true, null, new Range(momRateDataset.getLowerMinY(), momRateDataset.getUpperMaxY()));
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPNG(new File(outputDir, prefix+".png").getAbsolutePath());
		gp.saveAsPDF(new File(outputDir, prefix+".pdf").getAbsolutePath());
		gp.saveAsTXT(new File(outputDir, prefix+".txt").getAbsolutePath());
		
		funcs = Lists.newArrayList();
		funcs.add(m5RateDataset);
		funcs.add(m5RateDataset);
		prefix = "stationariy_m5_rate";
		
		spec = new PlotSpec(funcs, chars, "M≥5 Rate Over Time ("+(int)delta+"yr bins)", "Years", "Annual M≥5 Rate");
		
		gp = new HeadlessGraphPanel();
		
		setFontSizes(gp);
		
		gp.drawGraphPanel(spec, false, false, null, new Range(m5RateDataset.getLowerMinY(), m5RateDataset.getUpperMaxY()));
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPNG(new File(outputDir, prefix+".png").getAbsolutePath());
		gp.saveAsPDF(new File(outputDir, prefix+".pdf").getAbsolutePath());
		gp.saveAsTXT(new File(outputDir, prefix+".txt").getAbsolutePath());
	}
	
	
	


	
	public static void plotSubSectRecurrenceHist(List<List<ETAS_EqkRupture>> catalogs,
			FaultSystemRupSet rupSet, int sectIndex, File outputDir, double targetRI) throws IOException {
		HashSet<Integer> ruptures = new HashSet<Integer>(rupSet.getRupturesForSection(sectIndex));
		
		double numYrsForProb=10;
		double aveNumInNumYrs=0;
		int numFirst=0;
		int numFirstSpont=0;
		
		List<Double> intervals = Lists.newArrayList();
		double maxValue = 0d;
		double sum = 0d;
		
		double meanFirstHalf=0;
		double meanSecondHalf=0;
		double meanFirstInterval=0;
		double meanSecondInterval=0;
		int numFirstHalf=0;
		int numSecondHalf=0;
		int numFirstInterval=0;
		int numSecondInterval=0;
		double probOccurInNumyrs = 0;
		
		int numSpontaneous=0;
		int totNumEvents=0;
		
		List<ETAS_EqkRupture> firstCatalog = catalogs.get(0);
		double simulationDuration = calcEventTimeYears(firstCatalog, firstCatalog.get(firstCatalog.size()-1));

		
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			double prevTime = -1;
			boolean firstInterval=true;
			boolean secondInterval=false;
			
			for (ETAS_EqkRupture rup : catalog) {
				if (rup.getFSSIndex() < 0 || !ruptures.contains(rup.getFSSIndex()))
					continue;
				totNumEvents += 1.0;
				double myTime = calcEventTimeYears(catalog, rup);
				
				if(rup.getGeneration()==0)
					numSpontaneous+=1;
				
				if(prevTime == -1) {	// the first occurrance
					if(myTime<=numYrsForProb) {
						probOccurInNumyrs+=1.0;
						aveNumInNumYrs+=1.0;
					}
					numFirst+=1;
					if(rup.getGeneration()==0)
						numFirstSpont+=1;
				}

				
				if (prevTime >= 0) {
					if(myTime<=numYrsForProb) 
						aveNumInNumYrs+=1.0;
					double interval = myTime - prevTime;
					intervals.add(interval);
					maxValue = Math.max(maxValue, interval);
					sum += interval;
					if(secondInterval) {
						meanSecondInterval+=interval;
						numSecondInterval+=1;
						secondInterval=false;
					}
					if(firstInterval) {
						meanFirstInterval+=interval;
						numFirstInterval+=1;
						firstInterval=false;
						secondInterval=true;
					}
					if(myTime<=simulationDuration/2.0) {
						meanFirstHalf+=interval;
						numFirstHalf+=1;
					}
					else {
						meanSecondHalf+=interval;
						numSecondHalf+=1;
					}
				}
				
				prevTime = myTime;
			}
		}
		double mean = sum/intervals.size();
		meanFirstHalf /= (double)numFirstHalf;
		meanSecondHalf /= (double)numSecondHalf;
		meanFirstInterval /= (double)numFirstInterval;
		meanSecondInterval /= (double)numSecondInterval;
		
		String info = "Num Catalogs = "+catalogs.size()+"\n";
		

		if(!Double.isNaN(targetRI))
			info += "targetRI = "+(float)targetRI+"\n";
		info += "meanRI = "+(float)mean+"\n";
		info += "intervals.size() = "+intervals.size()+"\n";
		double fracSpontaneous = (double)numSpontaneous/(double)(totNumEvents);	// there are one more events than intervals
		info += "numSpontaneous = "+numSpontaneous+"\t("+(float)fracSpontaneous+")"+"\n";

		double testPartRate = totNumEvents/(catalogs.size()*simulationDuration);	// assumes thousand-year catalogs; num events is one minus num intervals
		info += "testPartRate = "+(float)testPartRate+"\t1/0/testPartRate="+(float)(1.0/testPartRate)+"\n";

		// Compute mean that does not include quick re-ruptures (within first 10% of ave RI)
		double meanFiltered=0;
		int numFiltered = 0;
		for(double ri:intervals) {
			if(ri/mean > 0.1) {
				meanFiltered+=ri;
				numFiltered+=1;
			}
		}
		meanFiltered /= (double)numFiltered;
		info += "meanFiltered = "+(float)meanFiltered+"\t(RIs within first 10% of ave RI excluded)\n";
		info += "numFiltered = "+numFiltered+"\n";
		info += "meanFirstHalf = "+(float)meanFirstHalf+"\t("+numFirstHalf+")"+"\n";
		info += "meanSecondHalf = "+(float)meanSecondHalf+"\t("+numSecondHalf+")"+"\n";
		info += "meanFirstInterval = "+(float)meanFirstInterval+"\t("+numFirstInterval+")"+"\n";
		info += "meanSecondInterval = "+(float)meanSecondInterval+"\t("+numSecondInterval+")"+"\n";
		info += "numFirstSpontaneous=" + numFirstSpont+"\n";
		info += "fractFirstSpontaneous=" + (float)numFirstSpont/(float)numFirst+"\n";

		probOccurInNumyrs /= (double)catalogs.size();	// fraction that had nothing in 10 years
		aveNumInNumYrs /=  (double)catalogs.size();
		info += "Prob one or more in "+numYrsForProb+" years ="+(float)(probOccurInNumyrs)+"\n";
		info += "Ave num in "+numYrsForProb+" years ="+(float)(aveNumInNumYrs)+"\n";
		
		System.out.println(info);

		HistogramFunction hist = HistogramFunction.getEncompassingHistogram(0d, maxValue, 0.1*mean);
		hist.setName("Histogram");
		hist.setInfo(info);
		
		for (double interval : intervals)
			hist.add(interval, 1d);
		
		hist.normalizeBySumOfY_Vals();
		hist.scale(1.0/hist.getDelta());	// make into a density
//		Range yRange = new Range(0d, hist.getMaxY()*1.1);
		Range yRange = new Range(0d, 0.016);
		
		List<XY_DataSet> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		DecimalFormat df = new DecimalFormat("0.0");
		
		funcs.add(hist);
		chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.BLACK));
		String prefix = "sub_sect_recurrence_"+sectIndex;
		
		DefaultXY_DataSet meanLine = new DefaultXY_DataSet();
		meanLine.set(mean, yRange.getLowerBound());
		meanLine.set(mean, yRange.getUpperBound());
		meanLine.setName("Mean="+df.format(mean));
		
		funcs.add(meanLine);
		chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.CYAN));
		
		double median = DataUtils.median(Doubles.toArray(intervals));
		
		DefaultXY_DataSet medianLine = new DefaultXY_DataSet();
		medianLine.set(median, yRange.getLowerBound());
		medianLine.set(median, yRange.getUpperBound());
		medianLine.setName("Median="+df.format(median));
		
		funcs.add(medianLine);
		chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.BLUE));
		
		double mode = hist.getX(hist.getXindexForMaxY());
		
		DefaultXY_DataSet modeLine = new DefaultXY_DataSet();
		modeLine.set(mode, hist.getMaxY());
		modeLine.setName("Mode="+df.format(mode));
		
		funcs.add(modeLine);
		chars.add(new PlotCurveCharacterstics(PlotSymbol.FILLED_CIRCLE, 2f, Color.RED));
		
		if(!Double.isNaN(targetRI))
		{
			DefaultXY_DataSet targetLine = new DefaultXY_DataSet();
			targetLine.set(targetRI, yRange.getLowerBound());
			targetLine.set(targetRI, yRange.getUpperBound());
			targetLine.setName("Target="+df.format(targetRI));
			
			funcs.add(targetLine);
			chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.GREEN));			
		}

		
		PlotSpec spec = new PlotSpec(funcs, chars, rupSet.getFaultSectionData(sectIndex).getName()+" Recurrence Intervals",
				"Years", "Density");
		spec.setLegendVisible(true);
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		
		setFontSizes(gp);
		
		gp.drawGraphPanel(spec, false, false, null, yRange);
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPNG(new File(outputDir, prefix+".png").getAbsolutePath());
		gp.saveAsPDF(new File(outputDir, prefix+".pdf").getAbsolutePath());
		gp.saveAsTXT(new File(outputDir, prefix+".txt").getAbsolutePath());
	}


	
	
	public static void plotSubSectRecurrenceIntervalVsTime(List<List<ETAS_EqkRupture>> catalogs,
			FaultSystemRupSet rupSet, int sectIndex, File outputDir, double targetRI) throws IOException {
		HashSet<Integer> ruptures = new HashSet<Integer>(rupSet.getRupturesForSection(sectIndex));
				
		DefaultXY_DataSet riVsTimeScatterFunc = new DefaultXY_DataSet();
		double maxValue = 0d;
		double sum = 0d;
		
		int totNumEvents=0;
		
		List<ETAS_EqkRupture> firstCatalog = catalogs.get(0);
		double simulationDuration = calcEventTimeYears(firstCatalog, firstCatalog.get(firstCatalog.size()-1));

		
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			double prevTime = -1;
			boolean firstInterval=true;
			boolean secondInterval=false;
			
			for (ETAS_EqkRupture rup : catalog) {
				if (rup.getFSSIndex() < 0 || !ruptures.contains(rup.getFSSIndex()))
					continue;
				totNumEvents += 1.0;
				double myTime = calcEventTimeYears(catalog, rup);
				

				
				if (prevTime >= 0) {
					double interval = myTime - prevTime;
					riVsTimeScatterFunc.set(myTime,interval);
					maxValue = Math.max(maxValue, interval);
					sum += interval;
			}
				
				prevTime = myTime;
			}
		}
		double mean = sum/riVsTimeScatterFunc.size();
		
		String info = "Num Catalogs = "+catalogs.size()+"\n";
		

		if(!Double.isNaN(targetRI))
			info += "targetRI = "+(float)targetRI+"\n";
		info += "meanRI = "+(float)mean+"\n";
		info += "num intervals = "+riVsTimeScatterFunc.size()+"\n";

		riVsTimeScatterFunc.setName("RI vs Time");
		riVsTimeScatterFunc.setInfo(info);
		
		
		List<XY_DataSet> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		DecimalFormat df = new DecimalFormat("0.0");
		
		funcs.add(riVsTimeScatterFunc);
		chars.add(new PlotCurveCharacterstics(PlotSymbol.X, 2f, Color.BLACK));
		String prefix = "sub_sect_RIvsTime_"+sectIndex;
		
		DefaultXY_DataSet meanLine = new DefaultXY_DataSet();
		meanLine.set(0.0, mean);
		meanLine.set(simulationDuration, mean);
		meanLine.setName("Mean="+df.format(mean));
		
		funcs.add(meanLine);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
		
		
		if(!Double.isNaN(targetRI))
		{
			DefaultXY_DataSet targetLine = new DefaultXY_DataSet();
			targetLine.set(0.0, targetRI);
			targetLine.set(simulationDuration, targetRI);
			targetLine.setName("Target="+df.format(targetRI));
			
			funcs.add(targetLine);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.GREEN));			
		}

		
		PlotSpec spec = new PlotSpec(funcs, chars, rupSet.getFaultSectionData(sectIndex).getName()+" Recurrence Intervals vs Time",
				"Years", "RI (years)");
		spec.setLegendVisible(true);
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		
		setFontSizes(gp);
		
		Range yRange = new Range(0d, riVsTimeScatterFunc.getMaxY());

		
		gp.drawGraphPanel(spec, false, false, null, yRange);
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPNG(new File(outputDir, prefix+".png").getAbsolutePath());
		gp.saveAsPDF(new File(outputDir, prefix+".pdf").getAbsolutePath());
		gp.saveAsTXT(new File(outputDir, prefix+".txt").getAbsolutePath());
	}


	public static void plotNormRecurrenceIntForAllSubSectHist(List<List<ETAS_EqkRupture>> catalogs,
			FaultSystemSolutionERF_ETAS erf, File outputDir) throws IOException {
		
		FaultSystemRupSet rupSet = erf.getSolution().getRupSet();
		
		// compute the long-term rate of each section
		double[] longTermRateOfFltSysRup = erf.getLongTermRateOfFltSysRupInERF();
		double[] longTermPartRateForSectArray = new double[rupSet.getNumSections()];
		for(int r=0; r<rupSet.getNumRuptures(); r++) {
			List<Integer> sectIndices = rupSet.getSectionsIndicesForRup(r);
			for(int s=0;s<sectIndices.size();s++) {
				int sectID = sectIndices.get(s);
				longTermPartRateForSectArray[sectID] += longTermRateOfFltSysRup[r];
			}
		}
		
		List<Double> intervals = Lists.newArrayList();
		double maxValue = 0d;
		double sum = 0d;
				
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			double[] prevTime = new double[rupSet.getNumSections()];
			for(int i=0;i<rupSet.getNumSections();i++)
				prevTime[i] = -1;
			
			for (ETAS_EqkRupture rup : catalog) {
				if (rup.getFSSIndex() < 0)
					continue;
				double myTime = calcEventTimeYears(catalog, rup);
				for(int sectID:rupSet.getSectionsIndicesForRup(rup.getFSSIndex())) {
					if (prevTime[sectID] >= 0) {
						double interval = (myTime - prevTime[sectID])*longTermPartRateForSectArray[sectID];
						intervals.add(interval);
						maxValue = Math.max(maxValue, interval);
						sum += interval;
					}
					prevTime[sectID] = myTime;
				}
			}
		}
		double mean = sum/intervals.size();
		
		String info = "Num Catalogs = "+catalogs.size()+"\n";
		info += "meanNormRI = "+(float)mean+"\n";
		info += "intervals.size() = "+intervals.size()+"\n";

		// Compute mean that does not include quick re-ruptures (within first 10% of ave RI)
		double meanFiltered=0;
		int numFiltered = 0;
		for(double ri:intervals) {
			if(ri > 0.1) {
				meanFiltered+=ri;
				numFiltered+=1;
			}
		}
		meanFiltered /= (double)numFiltered;

		info += "meanFiltered = "+(float)meanFiltered+"\t(RIs within first 10% of ave RI excluded)\n";
		info += "numFiltered = "+numFiltered+"\n";
		
		System.out.println(info);

		HistogramFunction hist = HistogramFunction.getEncompassingHistogram(0d, maxValue, 0.1);
		hist.setName("Histogram");
		hist.setInfo(info);
		
		for (double interval : intervals)
			hist.add(interval, 1d);
		
		hist.normalizeBySumOfY_Vals();
		hist.scale(1.0/hist.getDelta());	// make into a density
		Range yRange = new Range(0d, 2.6);
		Range xRange = new Range(0d, 5.0);
		
		List<XY_DataSet> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		funcs.add(hist);
		chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.BLACK));
		String prefix = "all_sub_sect__norm_recurrence_int_hist";
		
				
		PlotSpec spec = new PlotSpec(funcs, chars, "Norm Recurrence Intervals for All Sections",
				"Normalized RI", "Density");
		spec.setLegendVisible(true);
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		
		setFontSizes(gp);
		
		gp.drawGraphPanel(spec, false, false, xRange, yRange);
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPNG(new File(outputDir, prefix+".png").getAbsolutePath());
		gp.saveAsPDF(new File(outputDir, prefix+".pdf").getAbsolutePath());
		gp.saveAsTXT(new File(outputDir, prefix+".txt").getAbsolutePath());
	}



	public static void plotSubSectNuclMagFreqDist(List<List<ETAS_EqkRupture>> catalogs,
			FaultSystemSolutionERF_ETAS erf, int sectIndex, File outputDir) throws IOException {

		FaultSystemRupSet rupSet = erf.getSolution().getRupSet();
		HashSet<Integer> ruptures = new HashSet<Integer>(rupSet.getRupturesForSection(sectIndex));
		FaultPolyMgr faultPolyMgr = FaultPolyMgr.create(rupSet.getFaultSectionDataList(), InversionTargetMFDs.FAULT_BUFFER);	// this works for U3, but not generalized
		Region subSectPoly = faultPolyMgr.getPoly(sectIndex);
		
		SummedMagFreqDist mfdSupra = new SummedMagFreqDist(2.55, 8.45, 60);
		SummedMagFreqDist mfdSupra2 = new SummedMagFreqDist(2.55, 8.45, 60);
		SummedMagFreqDist mfdSub = new SummedMagFreqDist(2.55, 8.45, 60);
		
		// get catalog duration from the first catalog
		List<ETAS_EqkRupture> firstCatalog = catalogs.get(0);
		double catalogDuration = calcEventTimeYears(firstCatalog, firstCatalog.get(firstCatalog.size()-1));
		double normFactor = catalogDuration*catalogs.size();

		
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			for (ETAS_EqkRupture rup : catalog) {
				// check whether it nucleates inside polygon
				if(subSectPoly.contains(rup.getHypocenterLocation())) {
					if(rup.getFSSIndex() < 0) {
						int index = mfdSupra.getClosestXIndex(rup.getMag());
						mfdSub.add(index, 1.0/normFactor);
					}
					else {
						int index = mfdSupra.getClosestXIndex(rup.getMag());
						mfdSupra.add(index, 1.0/normFactor);
					}
				}
				
				// set supraMFD based on nucleation spread over rup surface
				if(rup.getFSSIndex() >= 0 && ruptures.contains(rup.getFSSIndex())) {
					List<Integer>  sectionList = rupSet.getSectionsIndicesForRup(rup.getFSSIndex());
					FaultSectionPrefData sectData = rupSet.getFaultSectionData(sectIndex);
					double sectArea= sectData.getTraceLength()*sectData.getReducedDownDipWidth();
					double rupArea=0;
					for(int sectID:sectionList) {
						sectData = rupSet.getFaultSectionData(sectID);
						rupArea += sectData.getTraceLength()*sectData.getReducedDownDipWidth();
					}
					int index = mfdSupra.getClosestXIndex(rup.getMag());
					mfdSupra2.add(index, sectArea/(normFactor*rupArea));
	
				}				
			}
		}
		
		mfdSupra.setName("Simulated Supra MFD for "+rupSet.getFaultSectionData(sectIndex).getName());
		mfdSupra.setInfo("actually nucleated in section");
		mfdSupra2.setName("Simulated Supra Alt MFD for "+rupSet.getFaultSectionData(sectIndex).getName());
		mfdSupra2.setInfo("nucleation probability from section  and rupture area");
		mfdSub.setName("Simulated SubSeis MFD for "+rupSet.getFaultSectionData(sectIndex).getName());

		List<XY_DataSet> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		funcs.add(mfdSupra);
		funcs.add(mfdSupra2);
		funcs.add(mfdSub);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
		chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.BLUE));
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		
		String prefix = "sub_sect_nucl_mfd_"+sectIndex;
		
		GraphWindow plotGraph = new GraphWindow(funcs, rupSet.getFaultSectionData(sectIndex).getName()+" Nucl. MFDs",chars); 
		plotGraph.setX_AxisLabel("Magnitude (M)");
		plotGraph.setY_AxisLabel("Rate (per yr)");
		plotGraph.setY_AxisRange(1e-7, 1e-1);
//		plotGraph.setX_AxisRange(2.5d, 8.5d);
		plotGraph.setYLog(true);
		plotGraph.setPlotLabelFontSize(18);
		plotGraph.setAxisLabelFontSize(22);
		plotGraph.setTickLabelFontSize(20);

		try {
			plotGraph.saveAsPDF(new File(outputDir, prefix+".pdf").getAbsolutePath());
			plotGraph.saveAsTXT(new File(outputDir, prefix+".txt").getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
		




	public static void plotSubSectPartMagFreqDist(List<List<ETAS_EqkRupture>> catalogs,
			FaultSystemSolutionERF_ETAS erf, int sectIndex, File outputDir) throws IOException {
		
		FaultSystemRupSet rupSet = erf.getSolution().getRupSet();
		HashSet<Integer> ruptures = new HashSet<Integer>(rupSet.getRupturesForSection(sectIndex));
		
		SummedMagFreqDist mfd = new SummedMagFreqDist(5.05, 8.45, 35);
		
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			for (ETAS_EqkRupture rup : catalog) {
				if (rup.getFSSIndex() < 0 || !ruptures.contains(rup.getFSSIndex()))
					continue;
				mfd.addResampledMagRate(rup.getMag(), 1.0, true);
			}
		}
		
		// get catalog duration from the first catalog
		List<ETAS_EqkRupture> firstCatalog = catalogs.get(0);
		double catalogDuration = calcEventTimeYears(firstCatalog, firstCatalog.get(firstCatalog.size()-1));
		mfd.scale(1.0/(catalogDuration*catalogs.size()));
		mfd.setName("Simulated MFD for "+rupSet.getFaultSectionData(sectIndex).getName());

		List<XY_DataSet> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		funcs.add(mfd);
		funcs.add(mfd.getCumRateDistWithOffset());
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
		chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.BLUE));
		
		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.POISSON);
		erf.updateForecast();
		SummedMagFreqDist targetMFD = FaultSysSolutionERF_Calc.calcParticipationMFDForAllSects(erf, 5.05, 8.45, 35)[sectIndex];
		targetMFD.setName("Target MFD for "+rupSet.getFaultSectionData(sectIndex).getName());
		funcs.add(targetMFD);
		funcs.add(targetMFD.getCumRateDistWithOffset());
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.BLACK));

		String prefix = "sub_sect_part_mfd_"+sectIndex;
		
		
		GraphWindow plotGraph = new GraphWindow(funcs, rupSet.getFaultSectionData(sectIndex).getName()+" Part. MFDs",chars); 
		plotGraph.setX_AxisLabel("Magnitude (M)");
		plotGraph.setY_AxisLabel("Rate (per yr)");
		plotGraph.setY_AxisRange(1e-7, 1e-1);
//		plotGraph.setX_AxisRange(2.5d, 8.5d);
		plotGraph.setYLog(true);
		plotGraph.setPlotLabelFontSize(18);
		plotGraph.setAxisLabelFontSize(22);
		plotGraph.setTickLabelFontSize(20);

		try {
			plotGraph.saveAsPDF(new File(outputDir, prefix+".pdf").getAbsolutePath());
			plotGraph.saveAsTXT(new File(outputDir, prefix+".txt").getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
//		PlotSpec spec = new PlotSpec(funcs, chars, rupSet.getFaultSectionData(sectIndex).getName()+" MFDs",
//				"Magnitude", "Rate (per yr)");
//		HeadlessGraphPanel gp = new HeadlessGraphPanel();
//		
//		setFontSizes(gp);
//		
//		gp.drawGraphPanel(spec, false, false, null, null);
//		gp.setYLog(true);
//		gp.getCartPanel().setSize(1000, 800);
//		gp.saveAsPNG(new File(outputDir, prefix+".png").getAbsolutePath());
//		gp.saveAsPDF(new File(outputDir, prefix+".pdf").getAbsolutePath());
//		gp.saveAsTXT(new File(outputDir, prefix+".txt").getAbsolutePath());
	}
	
	/**
	 * This plots the cumulative number of occurrences with time for the specified section to
	 * see if there is any increase in rate.
	 * @param catalogs
	 * @param erf
	 * @param sectIndex
	 * @param outputDir
	 * @throws IOException
	 */
	public static void plotCumNumWithTimeForSection(List<List<ETAS_EqkRupture>> catalogs,
			FaultSystemSolutionERF_ETAS erf, int sectIndex, File outputDir) throws IOException {
		
		FaultSystemRupSet rupSet = erf.getSolution().getRupSet();
		HashSet<Integer> ruptures = new HashSet<Integer>(rupSet.getRupturesForSection(sectIndex));
		
		ArbitrarilyDiscretizedFunc cumNumWithTimeFunc = new ArbitrarilyDiscretizedFunc();
		ArbitrarilyDiscretizedFunc comparisonLineFunc = new ArbitrarilyDiscretizedFunc();
		ArbitrarilyDiscretizedFunc tempFunc = new ArbitrarilyDiscretizedFunc();
				
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			for (ETAS_EqkRupture rup : catalog) {
				if (rup.getFSSIndex() < 0 || !ruptures.contains(rup.getFSSIndex()))
					continue;
				tempFunc.set(calcEventTimeYears(catalog, rup),1.0);
			}
		}
				
		double cumVal=0;
		double numSimulations = catalogs.size();
		for(int i=0;i<tempFunc.size();i++) {
			cumVal+=1.0/numSimulations;
			cumNumWithTimeFunc.set(tempFunc.getX(i),cumVal);
		}
		cumNumWithTimeFunc.setName("cumNumWithTimeFunc");

		
		comparisonLineFunc.set(cumNumWithTimeFunc.get(0));
		comparisonLineFunc.set(cumNumWithTimeFunc.get(cumNumWithTimeFunc.size()-1));
		comparisonLineFunc.setName("comparisonLineFunc");
		
		List<XY_DataSet> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		funcs.add(cumNumWithTimeFunc);
		funcs.add(comparisonLineFunc);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
		chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.BLACK));
		
		String prefix = "sub_sect_cumNumWithTime_"+sectIndex;
		
		
		GraphWindow plotGraph = new GraphWindow(funcs, rupSet.getFaultSectionData(sectIndex).getName(),chars); 
		plotGraph.setX_AxisLabel("Time (years)");
		plotGraph.setY_AxisLabel("Cum Num");
//		plotGraph.setY_AxisRange(1e-7, 1e-1);
//		plotGraph.setX_AxisRange(2.5d, 8.5d);
//		plotGraph.setYLog(true);
		plotGraph.setPlotLabelFontSize(18);
		plotGraph.setAxisLabelFontSize(22);
		plotGraph.setTickLabelFontSize(20);

		try {
			plotGraph.saveAsPDF(new File(outputDir, prefix+".pdf").getAbsolutePath());
			plotGraph.saveAsTXT(new File(outputDir, prefix+".txt").getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
//		PlotSpec spec = new PlotSpec(funcs, chars, rupSet.getFaultSectionData(sectIndex).getName()+" MFDs",
//				"Magnitude", "Rate (per yr)");
//		HeadlessGraphPanel gp = new HeadlessGraphPanel();
//		
//		setFontSizes(gp);
//		
//		gp.drawGraphPanel(spec, false, false, null, null);
//		gp.setYLog(true);
//		gp.getCartPanel().setSize(1000, 800);
//		gp.saveAsPNG(new File(outputDir, prefix+".png").getAbsolutePath());
//		gp.saveAsPDF(new File(outputDir, prefix+".pdf").getAbsolutePath());
//		gp.saveAsTXT(new File(outputDir, prefix+".txt").getAbsolutePath());
	}

	
	private static ETAS_ParameterList loadEtasParamsFromMetadata(Element root)
			throws DocumentException, MalformedURLException {
		Element paramsEl = root.element(ETAS_ParameterList.XML_METADATA_NAME);
		
		return ETAS_ParameterList.fromXMLMetadata(paramsEl);
	}
	
	public static void nedsAnalysis2() {

		System.out.println("Making ERF");
		double duration = 10;
		FaultSystemSolutionERF_ETAS erf = ETAS_Simulator.getU3_ETAS_ERF( 2012d, duration);
		FaultSystemRupSet rupSet = erf.getSolution().getRupSet();

		String dir = "/Users/field/Field_Other/CEA_WGCEP/UCERF3/UCERF3-ETAS/ResultsAndAnalysis/ScenarioSimulations";
		
		System.out.println("Reading catalogs");
		List<List<ETAS_EqkRupture>> catalogs=null;
		try {
			catalogs = ETAS_CatalogIO.loadCatalogsBinary(new File(dir+"/results_m4.bin"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		int triggerParentID = 9893;
		List<List<ETAS_EqkRupture>> primaryCatalogs = Lists.newArrayList();
		if (triggerParentID >= 0) {
			for (List<ETAS_EqkRupture> catalog : catalogs)
				primaryCatalogs.add(ETAS_SimAnalysisTools.getPrimaryAftershocks(catalog, triggerParentID));
		} else {
			for (List<ETAS_EqkRupture> catalog : catalogs)
				primaryCatalogs.add(ETAS_SimAnalysisTools.getByGeneration(catalog, 0));
		}
		
		System.out.println("catalogs.size()="+catalogs.size());
//		System.exit(-1);
		
		File outputDir = new File(dir);
		if(!outputDir.exists())
			outputDir.mkdir();

		double[] minMags = {0d};
		try {
			plotSectRates(primaryCatalogs, -1d, rupSet, minMags, outputDir, "test", "M7");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static void nedsAnalysis() {
		
		System.out.println("Making ERF");
		double duration = 10;
		FaultSystemSolutionERF_ETAS erf = ETAS_Simulator.getU3_ETAS_ERF( 2012d, duration);
		FaultSystemRupSet rupSet = erf.getSolution().getRupSet();
		
		
////		// THIS WAS TO TEST WHETHER TIME DEPENDENCE CAN EXPLAIN SYSTEMATICLY LOW SIMULATED SECTION RATES; IT CAN'T
//		// THIS ALSO SHOWS THAT TD RATES ARE BOGUS WHEN MORE THAN ONE EVENTS IS EXPECTED OVER THE DURATION
//		erf.getTimeSpan().setDuration(10000.0);
//		erf.updateForecast();
//		double[] subSectExpValsTD = FaultSysSolutionERF_Calc.calcParticipationRateForAllSects(erf, 0.0);
////		for(int s=0;s<subSectExpValsTD.length;s++)
////			System.out.println(subSectExpValsTD[s]+"\t"+rupSet.getFaultSectionData(s).getName());
////		System.exit(-1);
//		
//		erf.getTimeSpan().setDuration(1.0);
//		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.POISSON);
//		erf.updateForecast();
//		double[] subSectExpValsTI = FaultSysSolutionERF_Calc.calcParticipationRateForAllSects(erf, 0.0);
//
//		File tempOutDir = new File("/Users/field/Field_Other/CEA_WGCEP/UCERF3/UCERF3-ETAS/ResultsAndAnalysis/NoScenarioSimulations/");
//		try {
//			plotScatter(subSectExpValsTD, subSectExpValsTI,"TD vs TI for 10000 yrs", "","TD10000vsTI_sectRatesScatter", tempOutDir);
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		System.exit(-1);
		
		

		try {
			
			String dir = "/Users/field/Field_Other/CEA_WGCEP/UCERF3/UCERF3-ETAS/ResultsAndAnalysis/NoScenarioSimulations/";
			
//			String simName = "2015_12_08-spontaneous-1000yr-full_td-noApplyLTR";
//			String simName = "2016_01_28-spontaneous-1000yr-full_td-gridSeisCorr";
//			String simName = "2016_01_27-spontaneous-1000yr-newNuclWt-full_td-gridSeisCorr";
//			String simName = "2016_02_04-spontaneous-10000yr-full_td-subSeisSupraNucl-gridSeisCorr";
//			String simName = "2016_02_17-spontaneous-1000yr-scaleMFD1p14-full_td-subSeisSupraNucl-gridSeisCorr";
			
//			String simName = "2015_12_09-spontaneous-30yr-full_td-noApplyLTR";
//			String simName = "2016_01_31-spontaneous-30yr-full_td-gridSeisCorr";
//			String simName = "2016_01_31-spontaneous-30yr-newNuclWt-full_td-gridSeisCorr";
			String simName = "2016_02_18-spontaneous-30yr-scaleMFD1p14-full_td-subSeisSupraNucl-gridSeisCorr";

			
			
//			String simName = "2015_12_15-spontaneous-1000yr-mc10-applyGrGridded-full_td-noApplyLTR";
//			String simName = "2016_01_05-spontaneous-10000yr-mc10-applyGrGridded-full_td-noApplyLTR";

			
			
			
			System.out.println("Reading catalogs");
			List<List<ETAS_EqkRupture>> catalogs = ETAS_CatalogIO.loadCatalogsBinary(new File(dir+simName+"/results_m4.bin"));
			System.out.println("catalogs.size()="+catalogs.size());
//			System.exit(-1);
			
			File outputDir = new File(dir+simName);
			if(!outputDir.exists())
				outputDir.mkdir();
			
			
			// DO THIS ONE FOR 30-YEAR SIMULATIONS
//			System.out.println("ETAS_MultiSimAnalysisTools.writeSubSectRecurrenceIntervalStats(*)");
//			writeSubSectRecurrenceIntervalStats(catalogs, erf, outputDir,10d); // JUNK THIS
//			writeProbOfOneOrMoreEventsOnSectionStats(catalogs, erf, outputDir); // THIS NOW JUNK TOO
			try {
				plotAndWriteSectProbOneOrMoreData(catalogs, 10d, erf, outputDir);
			} catch (GMT_MapException e1) {
				e1.printStackTrace();
			} catch (RuntimeException e1) {
				e1.printStackTrace();
			}
			System.exit(-1);
			
			
			// DO THIS ONE FOR 1000-YEAR SIMULATIONS
			plotNormRecurrenceIntForAllSubSectHist(catalogs, erf, outputDir);
			// this not really necessary?:
			erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.POISSON);
			erf.updateForecast();
			try {
//				plotBinnedSectParticRateVsExpRate(catalogs, -1.0, erf, outputDir, "binned_sect_partic");
				plotSectParticScatter(catalogs, -1.0, erf, outputDir);
				
//				int numSubCat = 5;
//				int numInCat = catalogs.size()/numSubCat;
//				for(int i=0;i<numSubCat;i++) {
//					plotBinnedSectParticRateVsExpRate(catalogs.subList(i*numInCat, (i+1)*numInCat-1), -1.0, erf, outputDir, "binned_sect_partic"+i);
//				}
			} catch (Exception e) {
				e.printStackTrace();
			} 
			System.exit(-1);
			
			outputDir = new File(dir+simName+"/subSectPlots");
			if(!outputDir.exists())
				outputDir.mkdir();
			int[] sectIndexArray = {1906,1850,1922,1946};
			double[] sectPartRate = FaultSysSolutionERF_Calc.calcParticipationRateForAllSects(erf, 5.0);
			for(int sectIndex:sectIndexArray) {
				plotSubSectRecurrenceHist(catalogs, rupSet, sectIndex, outputDir, 1.0/sectPartRate[sectIndex]);
				plotSubSectRecurrenceIntervalVsTime(catalogs, rupSet, sectIndex, outputDir, 1.0/sectPartRate[sectIndex]);
//				double probOneOrMore = 1-Math.exp(-sectPartRate*duration);
//				System.out.println("Model Prob one or more in "+duration+" years ="+(float)probOneOrMore);
//				System.out.println("Model exp num in "+duration+" years ="+(float)(sectPartRate*duration));
				plotSubSectNuclMagFreqDist(catalogs, erf, sectIndex, outputDir);
				plotSubSectPartMagFreqDist(catalogs, erf, sectIndex, outputDir);
				plotCumNumWithTimeForSection(catalogs, erf, sectIndex, outputDir);
			}			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	private static final String plotDirName = "plots";
	private static final String catsDirName = "selected_catalogs";
	
	public static void main(String[] args) throws IOException, GMT_MapException, RuntimeException, DocumentException {
		
		nedsAnalysis();
		System.exit(-1);
		
		File mainDir = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations");
		double minLoadMag = -1;
		
//		boolean plotMFDs = false;
//		boolean plotExpectedComparison = false;
//		boolean plotSectRates = true;
//		boolean plotTemporalDecay = false;
//		boolean plotDistanceDecay = false;
//		boolean plotMaxMagHist = false;
//		boolean plotGenerations = false;
//		boolean plotGriddedNucleation = false;
//		boolean writeTimeFromPrevSupra = false;
//		boolean plotSectScatter = false;
//		boolean plotGridScatter = false;
//		boolean plotStationarity = false;
//		boolean plotSubSectRecurrence = false;
//		boolean writeCatsForViz = false;
		
		boolean plotMFDs = true;
		boolean plotExpectedComparison = false;
		boolean plotSectRates = true;
		boolean plotTemporalDecay = true;
		boolean plotDistanceDecay = true;
		boolean plotMaxMagHist = true;
		boolean plotGenerations = true;
		boolean plotGriddedNucleation = true;
		boolean writeTimeFromPrevSupra = true;
		boolean plotSectScatter = true;
		boolean plotGridScatter = true;
		boolean plotStationarity = true;
		boolean plotSubSectRecurrence = true;
		boolean writeCatsForViz = false;
		
		boolean useDefaultETASParamsIfMissing = true;
		boolean useActualDurations = true; // only applies to spontaneous runs
		
		int id_for_scenario = 0;
		
//		File resultDir = new File(mainDir, "2015_08_20-spontaneous-full_td");
//		File myOutput = new File(resultDir, "output_stats");
//		Preconditions.checkState(myOutput.exists() || myOutput.mkdir());
//		List<List<ETAS_EqkRupture>> myCatalogs = ETAS_CatalogIO.loadCatalogs(new File(resultDir, "results.zip"));
//		for (int i=0; i<myCatalogs.size(); i++) {
//			long prevTime = Long.MIN_VALUE;
//			for (ETAS_EqkRupture rup : myCatalogs.get(i)) {
//				Preconditions.checkState(prevTime <= rup.getOriginTime());
//				prevTime = rup.getOriginTime();
//			}
//		}
//		plotNumEventsHistogram(myCatalogs, myOutput, "num_events_hist");
//		plotTotalMomentHistogram(myCatalogs, myOutput, "moment_hist");
//		System.exit(0);
		
		File fssFile = new File("dev/scratch/UCERF3/data/scratch/InversionSolutions/"
				+ "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_SpatSeisU3_MEAN_BRANCH_AVG_SOL.zip");
		AbstractGridSourceProvider.SOURCE_MIN_MAG_CUTOFF = 2.55;
		FaultSystemSolution fss = FaultSystemIO.loadSol(fssFile);
		
		// only for spontaneous
		boolean skipEmpty = true;
		double minDurationForInclusion = 0d;
//		double minDurationForInclusion = 0.5d;
//		double minDurationForInclusion = 990;
		
		List<File> resultsZipFiles = Lists.newArrayList();
		
		if (args.length == 0) {
			// manual run on the laptop
			
			resultsZipFiles.add(new File(mainDir, "2016_02_19-mojave_m7-10yr-full_td-subSeisSupraNucl-gridSeisCorr-scale1.14/results_m4.bin"));
			id_for_scenario = 9893;
			
//			resultsZipFiles.add(new File(mainDir, "2016_02_19-mojave_m5-10yr-full_td-subSeisSupraNucl-gridSeisCorr-scale1.14/results_descendents.bin"));
//			id_for_scenario = 9893;
//			
//			resultsZipFiles.add(new File(mainDir, "2016_02_19-mojave_m5p5-10yr-full_td-subSeisSupraNucl-gridSeisCorr-scale1.14/results_descendents.bin"));
//			id_for_scenario = 9893;
//			
//			resultsZipFiles.add(new File(mainDir, "2016_02_19-mojave_m6pt3_fss-10yr-full_td-subSeisSupraNucl-gridSeisCorr-scale1.14/results_descendents.bin"));
//			id_for_scenario = 9893;
//			
//			resultsZipFiles.add(new File(mainDir, "2016_02_19-mojave_m6pt3_ptsrc-10yr-full_td-subSeisSupraNucl-gridSeisCorr-scale1.14/results_descendents.bin"));
//			id_for_scenario = 9893;
			
//			resultsZipFiles.add(new File(mainDir, "2016_02_19-mojave_m7-10yr-full_td-subSeisSupraNucl-gridSeisCorr-scale1.14/results_descendents.bin"));
//			id_for_scenario = 9893;
			
//			names.add("30yr Full TD");
//			resultsZipFiles.add(new File(mainDir, "2016_02_18-spontaneous-30yr-scaleMFD1p14-full_td-subSeisSupraNucl-gridSeisCorr/results_m4.bin"));
//			scenarios.add(null);
			
//			names.add("1000yr Full TD");
//			resultsZipFiles.add(new File(mainDir, "2016_02_17-spontaneous-1000yr-scaleMFD1p14-full_td-subSeisSupraNucl-gridSeisCorr/results_m4.bin"));
//			scenarios.add(null);
		} else {
			// command line arguments
			
			for (String arg : args) {
				File resultFile = new File(arg);
				Preconditions.checkState(resultFile.exists()
						&& (resultFile.getName().endsWith(".bin") || resultFile.getName().endsWith(".zip")));
				
				if (resultFile.getParentFile().getName().startsWith("2016_02_19-mojave")) {
					System.out.println("Changing scenario ID");
					id_for_scenario = 9893;
				}
				
				resultsZipFiles.add(resultFile);
			}
		}
		
		for (int n=0; n<resultsZipFiles.size(); n++) {
			File resultsFile = resultsZipFiles.get(n);
//			TestScenario scenario = scenarios.get(n);
			TestScenario scenario = null;
			File directory = resultsFile.getParentFile();
			System.out.println("Processing "+directory.getAbsolutePath());
			String dirName = directory.getName().toLowerCase();
			if (dirName.contains("spontaneous")) {
				System.out.println("Detected spontaneous");
			} else {
				for (TestScenario test : TestScenario.values()) {
					if (dirName.contains(test.name().toLowerCase())) {
						scenario = test;
						break;
					}
				}
				Preconditions.checkState(scenario != null, "Couldn't detect scenario from dir name: "+dirName);
				System.out.println("Detected scenario "+scenario.name());
			}
			
			if (scenario != null && scenario.getFSS_Index() >= 0)
				scenario.updateMag(fss.getRupSet().getMagForRup(scenario.getFSS_Index()));
			
			// parent ID for the trigger rupture
			int triggerParentID;
			if (scenario == null)
				triggerParentID = -1;
			else
				triggerParentID = id_for_scenario;
			
			System.gc();
			
			RuptureSurface surf;
			if (scenario == null)
				surf = null;
			else if (scenario.getLocation() != null)
				surf = new PointSurface(scenario.getLocation());
			else
				surf = fss.getRupSet().getSurfaceForRupupture(scenario.getFSS_Index(), 1d, false);
			
			File parentDir = resultsFile.getParentFile();
			
			File outputDir = new File(resultsFile.getParentFile(), plotDirName);
			
			File metadataFile = new File(resultsFile.getParentFile(), "metadata.xml");
			System.out.println("Metadatafile: "+metadataFile.getAbsolutePath());
			Element metadataRootEl = null;
			ETAS_ParameterList params;
			if (metadataFile.exists()) {
				System.out.println("Loading ETAS params from metadata file: "+metadataFile.getAbsolutePath());
				Document doc = XMLUtils.loadDocument(metadataFile);
				metadataRootEl = doc.getRootElement();
				params = loadEtasParamsFromMetadata(metadataRootEl);
			} else if (useDefaultETASParamsIfMissing) {
				System.out.println("Using default ETAS params");
				params = new ETAS_ParameterList();
			} else {
				params = null;
			}
			
			Long ot;
			double inputDuration;
			if (metadataRootEl != null) {
				Element paramsEl = metadataRootEl.element(MPJ_ETAS_Simulator.OTHER_PARAMS_EL_NAME);
				ot = Long.parseLong(paramsEl.attributeValue("ot"));
				inputDuration = Double.parseDouble(paramsEl.attributeValue("duration"));
			} else {
				System.out.println("WARNING: Assuming 1 year 2014");
				ot = Math.round((2014.0-1970.0)*ProbabilityModelsCalc.MILLISEC_PER_YEAR); // occurs at 2014
				inputDuration = 1d;
			}
			double duration = inputDuration;
			if (useActualDurations && scenario == null)
				duration = -1;
			
			String name;
			if (scenario == null)
				name = "";
			else
				name = scenario+" ";
			name += (int)inputDuration+"yr";
			if (params != null)
				name += " "+params.getU3ETAS_ProbModel();
			
			if (!outputDir.exists()) {
				// see if old dirs exist;
				File oldDir = new File(parentDir, "output_stats");
				if (oldDir.exists()) {
					Preconditions.checkState(oldDir.renameTo(outputDir));
				} else {
					oldDir = new File(parentDir, "outputs_stats");
					if (oldDir.exists())
						Preconditions.checkState(oldDir.renameTo(outputDir));
				}
			}
			Preconditions.checkState(outputDir.exists() || outputDir.mkdir());
			
			// load the catalogs
			System.out.println("Loading "+name+" from "+resultsFile.getAbsolutePath());
			Stopwatch timer = Stopwatch.createStarted();
			List<List<ETAS_EqkRupture>> catalogs = ETAS_CatalogIO.loadCatalogs(resultsFile, minLoadMag, true);
			timer.stop();
			long secs = timer.elapsed(TimeUnit.SECONDS);
			if (secs > 60)
				System.out.println("Catalog loading took "+(float)((double)secs/60d)+" minutes");
			else
				System.out.println("Catalog loading took "+secs+" seconds");
			
//			ETAS_CatalogIO.writeEventDataToFile(new File("/tmp/catalog_0.txt"), catalogs.get(0));
//			System.exit(0);
			
			if (skipEmpty && scenario == null) {
				int skipped = 0;
				for (int i=catalogs.size(); --i>=0;) {
					if (catalogs.get(i).isEmpty()) {
						catalogs.remove(i);
						skipped++;
					}
				}
				if (skipped > 0)
					System.out.println("Removed "+skipped+" empty catalogs.");
			}
			
			// now check actual duration
			MinMaxAveTracker durationTrack = new MinMaxAveTracker();
			int skippedDuration = 0;
			for (int i=catalogs.size(); --i>=0;) {
				List<ETAS_EqkRupture> catalog = catalogs.get(i);
				double myDuration = calcDurationYears(catalog);
				if (myDuration < minDurationForInclusion && scenario == null) {
					catalogs.remove(i);
					skippedDuration++;
				} else {
					durationTrack.addValue(myDuration);
				}
			}
			if (skippedDuration > 0)
				System.out.println("Removed "+skippedDuration+" catalgos that were too short");
			System.out.println("Actual duration: "+durationTrack);
			double meanDuration = durationTrack.getAverage();
			if (duration > 0 && DataUtils.getPercentDiff(duration, durationTrack.getMin()) > 2d)
				System.out.println("WARNING: at least 1 simulation doesn't match expected duration");
			
			List<List<ETAS_EqkRupture>> childrenCatalogs = Lists.newArrayList();
			if (triggerParentID >= 0)
				for (List<ETAS_EqkRupture> catalog : catalogs)
					childrenCatalogs.add(ETAS_SimAnalysisTools.getChildrenFromCatalog(catalog, triggerParentID));
			else
				childrenCatalogs.addAll(catalogs);
			
			MinMaxAveTracker childrenTrack = new MinMaxAveTracker();
			for (List<ETAS_EqkRupture> catalog : childrenCatalogs)
				childrenTrack.addValue(catalog.size());
			System.out.println("Children counts: "+childrenTrack);
			
//			// print out catalogs with most triggered moment
//			List<Integer> indexes = Lists.newArrayList();
//			for (int i=0; i<childrenCatalogs.size(); i++)
//				indexes.add(i);
//			List<Double> moments = calcTotalMoments(childrenCatalogs);
//			List<ComparablePairing<Double, Integer>> pairings = ComparablePairing.build(moments, indexes);
//			Collections.sort(pairings);
//			Collections.reverse(pairings);
//			System.out.println("Index\tMoment\tMax M\t# Trig\t# Supra");
//			for (int i=0; i<20; i++) {
//				ComparablePairing<Double, Integer> pairing = pairings.get(i);
//				int index = pairing.getData();
//				List<ETAS_EqkRupture> catalog = childrenCatalogs.get(index);
//				double moment = pairing.getComparable();
//				double maxMag = 0d;
//				int numSupra = 0;
//				for (ETAS_EqkRupture rup : catalog) {
//					maxMag = Math.max(maxMag, rup.getMag());
//					if (rup.getFSSIndex() >= 0)
//						numSupra++;
//				}
//				
//				System.out.println(index+"\t"+(float)moment+"\t"+(float)maxMag+"\t"+catalog.size()+"\t"+numSupra);
//			}
			
			List<List<ETAS_EqkRupture>> primaryCatalogs = Lists.newArrayList();
			if (triggerParentID >= 0) {
				for (List<ETAS_EqkRupture> catalog : catalogs)
					primaryCatalogs.add(ETAS_SimAnalysisTools.getPrimaryAftershocks(catalog, triggerParentID));
			} else {
				for (List<ETAS_EqkRupture> catalog : catalogs)
					primaryCatalogs.add(ETAS_SimAnalysisTools.getByGeneration(catalog, 0));
			}
			
			String fullName;
			String fullFileName;
			String subsetName;
			String subsetFileName;
			if (triggerParentID >= 0) {
				fullName = "Children";
				fullFileName = "full_children";
				subsetName = "Primary";
				subsetFileName = "primary";
			} else {
				fullName = "All EQs";
				fullFileName = "all_eqs";
				subsetName = "Spontaneous";
				subsetFileName = "spontaneous";
			}
			
			FaultSystemSolutionERF erf = null;
			if (triggerParentID < 0 && (meanDuration >= 100d || catalogs.size() >= 1000)) {
				System.out.println("Creating ERF for comparisons");
				erf = new FaultSystemSolutionERF(fss);
				erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.POISSON);
				erf.setParameter(IncludeBackgroundParam.NAME, IncludeBackgroundOption.INCLUDE);
				erf.getTimeSpan().setDuration(1d);
				erf.updateForecast();
			}
			
			boolean gridSeisCorr = resultsFile.getParentFile().getName().contains("gridSeisCorr")
					|| (params != null && params.getApplyGridSeisCorr());
			
			if (gridSeisCorr) {
				System.out.println("applying gridded seis comparison");
				double[] gridSeisCorrValsArray;
				try {
					gridSeisCorrValsArray = MatrixIO.doubleArrayFromFile(
							new File(ETAS_PrimaryEventSampler.defaultGriddedCorrFilename));
				} catch (IOException e) {
					throw ExceptionUtils.asRuntimeException(e);
				}
				ETAS_Simulator.D = false;
				ETAS_Simulator.correctGriddedSeismicityRatesInERF(fss, false, gridSeisCorrValsArray);
			}
			
			if (plotMFDs) {
				System.out.println("Plotting MFDs");
				
				ArbIncrementalMagFreqDist[] subMFDs = plotMFD(childrenCatalogs, duration, erf, outputDir, name, fullFileName);
				if (triggerParentID >= 0)
					plotMFD(primaryCatalogs, duration, null, outputDir, subsetName+" "+name, subsetFileName+"_aftershocks");
				else
					plotMFD(primaryCatalogs, duration, erf, outputDir, subsetName+" "+name, subsetFileName+"_events");
				
				plotFractWithMagAbove(childrenCatalogs, subMFDs, scenario, outputDir, name, fullFileName+"_fract_above_mag");
				
				if (scenario != null)
					plotMagNum(childrenCatalogs, outputDir, name, "consolidated_aftershocks");
			}
			
			if (plotExpectedComparison && triggerParentID >= 0) {
				System.out.println("Plotting Expected Comparison MFDs");
				plotExpectedSupraComparisonMFD(primaryCatalogs, outputDir, subsetName+" "+name, subsetFileName+"_supra_compare_to_expected");
				
				// now do first/last half
				int numCatalogs = primaryCatalogs.size();
				List<List<ETAS_EqkRupture>> firstHalfPrimary = primaryCatalogs.subList(0, numCatalogs/2);
				List<List<ETAS_EqkRupture>> secondHalfPrimary = primaryCatalogs.subList(firstHalfPrimary.size(), primaryCatalogs.size());
				plotExpectedSupraComparisonMFD(firstHalfPrimary, outputDir, subsetName+" "+name,
						subsetFileName+"_supra_compare_to_expected_first"+firstHalfPrimary.size());
				plotExpectedSupraComparisonMFD(secondHalfPrimary, outputDir, subsetName+" "+name,
						subsetFileName+"_supra_compare_to_expected_last"+secondHalfPrimary.size());
			}
			
			if (plotSectRates) {
				// sub section partic/trigger rates
				System.out.println("Plotting Sub Sect Rates");
				double[] minMags = { 0, 6.7, 7.8 };
//				plotSectRates(childrenCatalogs, duration, fss.getRupSet(), minMags, outputDir,
//						name+" "+fullName, fullFileName+"_sect");
				plotSectRates(primaryCatalogs, duration, fss.getRupSet(), minMags, outputDir,
						name+" "+subsetName, subsetFileName+"_sect");
			}
			
			if (plotTemporalDecay && triggerParentID >= 0) {
				// temporal decay
				System.out.println("Plotting Temporal Decay");
				plotAftershockRateVsLogTimeHistForRup(primaryCatalogs, scenario, params, ot, outputDir,
						name+" "+subsetName, subsetFileName+"_temporal_decay");
				plotAftershockRateVsLogTimeHistForRup(childrenCatalogs, scenario, params, ot, outputDir,
						name, fullFileName+"_temporal_decay");
			}
			
			if (plotDistanceDecay && triggerParentID >= 0) {
				// dist decay trigger loc
				System.out.println("Plotting Trigger Loc Dist Decay");
				plotDistDecay(primaryCatalogs, params, null, outputDir, name+" Primary", subsetFileName+"_dist_decay_trigger");
				plotDistDecay(childrenCatalogs, params, null, outputDir, name, fullFileName+"_dist_decay_trigger");
			
				// dist decay rup surf
				if (scenario != null && scenario.getFSS_Index() >= 0) {
					System.out.println("Plotting Surface Dist Decay");
					Stopwatch watch = Stopwatch.createStarted();
					plotDistDecay(primaryCatalogs, params, surf, outputDir, name+" Primary", subsetFileName+"_dist_decay_surf");
					double mins = (watch.elapsed(TimeUnit.SECONDS))/60d;
					System.out.println("Primary surf dist decay took "+(float)mins+" mins");
					watch.reset();
					watch.start();
					plotDistDecay(childrenCatalogs, params, surf, outputDir, name, fullFileName+"_dist_decay_surf");
					watch.stop();
					mins = (watch.elapsed(TimeUnit.SECONDS))/60d;
					System.out.println("Full surf dist decay took "+(float)mins+" mins");
				}
			}
			
			if (plotMaxMagHist) {
				System.out.println("Plotting max mag hist");
				plotMaxTriggeredMagHist(childrenCatalogs, primaryCatalogs, scenario, outputDir, name, "max_mag_hist");
			}
			
			if (plotGenerations) {
				System.out.println("Plotting generations");
				plotNumEventsPerGeneration(childrenCatalogs, outputDir, name, fullFileName+"_generations");
			}
			
			if (plotGriddedNucleation) {
				System.out.println("Plotting gridded nucleation");
				double[] mags = { 2.5, 6.7, 7.8 };
				plotCubeNucleationRates(childrenCatalogs, duration, outputDir, name, fullFileName+"_gridded_nucl", mags);
				plotCubeNucleationRates(primaryCatalogs, duration, outputDir, name, subsetFileName+"_gridded_nucl", mags);
				if (scenario == null && (duration > 1d || duration < 0)) {
					List<List<ETAS_EqkRupture>> histCats = getOnlyAftershocksFromHistorical(childrenCatalogs);
					plotCubeNucleationRates(histCats, duration, outputDir, name,
							fullFileName+"_gridded_nucl_historical", mags);
				}
			}
			
			if (plotSectScatter && erf != null) {
				System.out.println("Plotting section participation scatter");
				File fullResultsFile = new File(resultsFile.getParentFile(), "results.bin");
				if (fullResultsFile.exists() && resultsFile.getName().endsWith(".bin")
						&& resultsFile.getName().startsWith("results_m")) {
					System.out.println("Iterating over full results from "+fullResultsFile.getAbsolutePath());
					plotSectParticScatter(ETAS_CatalogIO.getBinaryCatalogsIterable(fullResultsFile, 0d),
							duration, erf, outputDir);
				} else {
					plotSectParticScatter(catalogs, duration, erf, outputDir);
				}
			}
			
			if (plotGridScatter && erf != null) {
				System.out.println("Plotting gridded nucleation scatter");
				plotGriddedNucleationScatter(catalogs, duration, erf, outputDir);
			}
			
			if (plotStationarity && (duration > 1d || duration < 0) && triggerParentID < 0 && catalogs.size() >= 500) {
				System.out.println("Plotting stationarity");
				plotStationarity(catalogs, duration, outputDir);
			}
			
			if (plotSubSectRecurrence && (duration > 1d || duration < 0) && triggerParentID < 0) {
				System.out.println("Plotting sub section recurrence");
				int[] sectIndexes = {
						1922, // parkfield 2
						1850 // Mojave S 13
				};
				for (int sectIndex : sectIndexes)
					plotSubSectRecurrenceHist(catalogs, fss.getRupSet(), sectIndex, outputDir, Double.NaN);
			}
			
			if (writeCatsForViz) {
				System.out.println("Writing catalogs for vizualisation in SCEC-VDO");
				writeCatalogsForViz(childrenCatalogs, scenario, new File(parentDir, catsDirName), 5);
			}
			
			if (scenario == null && writeTimeFromPrevSupra) {
				System.out.println("Plotting time since last supra");
				writeTimeFromPrevSupraHist(catalogs, outputDir);
			}
			
			writeHTML(parentDir, scenario, name, params, catalogs, inputDuration, durationTrack);
		}
	}

	public static double calcDurationYears(List<ETAS_EqkRupture> catalog) {
		return calcEventTimeYears(catalog, catalog.get(catalog.size()-1));
	}

	private static double calcEventTimeYears(List<ETAS_EqkRupture> catalog, ETAS_EqkRupture rup) {
		long durationMillis = rup.getOriginTime() - catalog.get(0).getOriginTime();
		double myDuration = (double)durationMillis/MILLIS_PER_YEAR;
		return myDuration;
	}
	
	private static final int html_w_px = 800;
	
	private static void writeHTML(File outputDir, TestScenario scenario, String scenName,
			ETAS_ParameterList params, List<List<ETAS_EqkRupture>> catalogs,
			double inputDuration, MinMaxAveTracker durationTrack)
					throws IOException {
		System.out.println("Writing HTML");
		
		FileWriter fw = new FileWriter(new File(outputDir, "HEADER.html"));
		
		fw.write("<h1 style=\"font-family:'HelveticaNeue-Light', sans-serif; font-weight:normal;\">"
				+scenName+"</h1>\n");
		fw.write("<br>\n");
		fw.write("<p style=\"font-family:'HelveticaNeue-Light', sans-serif; font-weight:normal; width:"+html_w_px+";\">\n");
		if (scenario != null) {
			fw.write("<h2>Scenario Information</h2>\n");
			fw.write("<b>Name:</b> "+scenario.name()+"<br>\n");
			fw.write("<b>Magnitude:</b> "+scenario.getMagnitude()+"<br>\n");
			fw.write("<b>Supra-seismogenic? </b> "+(scenario.getFSS_Index()>=0)+"<br>\n");
			fw.write("<br>\n");
		}
		
		fw.write("<h2>Simulation Information</h2>\n");
		fw.write("<b>Num Catalogs:</b> "+catalogs.size()+"<br>\n");
		fw.write("<b>Simulation Input Duration:</b> "+(float)inputDuration+" years<br>\n");
		fw.write("<b>Actual Duration:</b> min="+(float)durationTrack.getMin()
		+", max="+(float)durationTrack.getMax()+", avg="+(float)durationTrack.getAverage()+" years<br>\n");
		fw.write("<br>\n");
		if (params != null) {
			fw.write("<h3>ETAS Parameters</h3>\n");
			for (Parameter<?> param : params)
				fw.write("<b>"+param.getName()+":</b> "+param.getValue()+"<br>\n");
			fw.write("<br>\n");
		}
		
		File plotDir = new File(outputDir, plotDirName);
		if (plotDir.exists()) {
			fw.write("<b>Various plots can be found in the "+plotDirName+" directory below</b><br>\n");
			
			// find minMag
			double minMag = Double.POSITIVE_INFINITY;
			for (List<ETAS_EqkRupture> catalog : catalogs)
				for (ETAS_EqkRupture rup : catalog)
					minMag = Math.min(minMag, rup.getMag());
			// round minMag
			minMag = 0.1*Math.round((minMag-0.01)*10d);
			
			writePlotHTML(plotDir, scenName, minMag);
		}
		
		fw.write("</p>\n");
		
		fw.close();
	}
	
	private static void writePlotHTML(File plotDir, String scenarioName, double minMag) throws IOException {
		FileWriter fw = new FileWriter(new File(plotDir, "HEADER.html"));
		
		fw.write("<h1 style=\"font-family:'HelveticaNeue-Light', sans-serif; font-weight:normal;\">"
				+scenarioName+" Plots</h1>\n");
		fw.write("<br>\n");
		fw.write("<p style=\"font-family:'HelveticaNeue-Light', sans-serif; font-weight:normal; width:"+html_w_px+";\">\n");
		
		fw.write("<b>Plots are divided into 3 categories:</b><br>\n");
		fw.write("<b>full_children_*:</b> Plots that include all generations of child events<br>\n");
		fw.write("<b>primary_*:</b> Plots that only consider primary aftershocks<br>\n");
		fw.write("<b>(other):</b> Plots where both are included or separation is not applicable<br>\n");
		
		fw.write("<br>\n");
		fw.write("<b>MFD Plots:</b><br>\n");
		fw.write("<b>*_mfd_cumulative.*:</b> Cumulative magnitude frequency distributious across all catalogs<br>\n");
		fw.write("<b>*_mfd_incremental.*:</b> Incremental magnitude frequency distributious across all catalogs<br>\n");
		fw.write("<br>\n");
		fw.write("<b>Decay Plots:</b><br>\n");
		fw.write("<b>*_dist_decay_trigger.*:</b> Distance decay of each child rupture from the trigger location on "
				+ "the parent rupture<br>\n");
		fw.write("<b>*_temporal_decay.*:</b> Temporal decay of each child rupture relative to its parent<br>\n");
		fw.write("<br>\n");
		fw.write("<b>Map Based Plots:</b><br>\n");
		fw.write("<b>*_sect_partic.*:</b> Map view of supra-seismogenic fault section participation rates<br>\n");
		fw.write("<b>*_sect_trigger.*:</b> Map view of supra-seismogenic fault section trigger rates<br>\n");
		fw.write("<b>*_gridded_nucl_m*.*:</b> Map view of gridded nucleation rates<br>\n");
		fw.write("<br>\n");
		fw.write("<b>Other Misc Plots:</b><br>\n");
		fw.write("<b>max_mag_hist.*:</b> Histogram of maximum magnitude triggered rupture across all catalogs<br>\n");
		fw.write("<b>full_children_generations.*:</b> Number of aftershocks of each generation<br>\n");
		
		if (minMag >= 3d) {
			fw.write("<br>\n");
			fw.write("<b>NOTE: due to the number or aftershocks, only M&ge;"
					+(float)minMag+" ruptures are considered in these plots</b><br>\n");
		}
		
		fw.write("</p>\n");
		
		fw.close();
	}

}
