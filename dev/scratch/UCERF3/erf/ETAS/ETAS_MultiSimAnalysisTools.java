package scratch.UCERF3.erf.ETAS;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.Collections;
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
import org.opensha.commons.gui.plot.HeadlessGraphPanel;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.util.ComparablePairing;
import org.opensha.commons.util.DataUtils;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.commons.util.XMLUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
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
import scratch.UCERF3.utils.FaultSystemIO;
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
		return calcNumWithMagAbove(catalogs, targetMinMag, -1, -1);
	}
	
	public static int calcNumWithMagAbove(List<List<ETAS_EqkRupture>> catalogs, double targetMinMag,
			int triggerParentID, int maxDaysAfter) {
		HashSet<Integer> triggerParentIDs = null;
		if (triggerParentID >= 0) {
			triggerParentIDs = new HashSet<Integer>();
			triggerParentIDs.add(triggerParentID);
		}
		int num = 0;
		long ot = Math.round((2014.0-1970.0)*ProbabilityModelsCalc.MILLISEC_PER_YEAR); // occurs at 2014
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
		gp.setBackgroundColor(Color.WHITE);
		gp.setTickLabelFontSize(22);
		gp.setAxisLabelFontSize(24);
		gp.setPlotLabelFontSize(24);
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
	
	private static void plotFractWithMagAbove(List<List<ETAS_EqkRupture>> catalogs,
			ArbIncrementalMagFreqDist[] subMFDs, TestScenario scenario,
			File outputDir, String name, String prefix) throws IOException {
		if (subMFDs == null)
			subMFDs = plotMFD(catalogs, -1d, null, null, null, null);
		
		Preconditions.checkArgument(subMFDs.length > 0);
		Preconditions.checkArgument(subMFDs.length == catalogs.size());
		
		double minMag = subMFDs[0].getMinX();
		int numMag = subMFDs[0].size();
		double delta = subMFDs[0].getDelta();
		
		EvenlyDiscretizedFunc atOrAboveFunc = new EvenlyDiscretizedFunc(minMag-delta*0.5, numMag, delta);
		EvenlyDiscretizedFunc atFunc = new EvenlyDiscretizedFunc(minMag, numMag, delta);

		double fractEach = 1d/subMFDs.length;
		double minY = Math.min(fractEach, 1d/10000d);
		
		for (ArbIncrementalMagFreqDist subMFD : subMFDs) {
			int maxIndex = -1;
			for (int i=0; i<numMag; i++) {
				if (subMFD.getY(i) > 0d) {
					atFunc.add(i, fractEach);
					maxIndex = i;
				}
			}
			for (int i=0; i<=maxIndex; i++)
				atOrAboveFunc.add(i, fractEach);
		}
		
		List<XY_DataSet> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		atFunc.setName("Fract With Mag");
		funcs.add(atFunc);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 3f, Color.BLUE));
		
		atOrAboveFunc.setName("Fract With ≥Mag");
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

	private static void getFractilePlotFuncs(EvenlyDiscretizedFunc[] allFuncs, double[] fractiles,
			boolean mode, List<XY_DataSet> funcs, List<PlotCurveCharacterstics> chars, File csvFile)
			throws IOException {
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
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.GREEN.darker()));
		}
		
		AbstractXY_DataSet median = fractCalc.getFractile(0.5);
		median.setName("Median");
		funcs.add(median);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLUE));
		
		int numX = allFuncs[0].size();
		
		// will be added later
		AbstractXY_DataSet meanFunc = fractCalc.getMeanCurve();
		
		AbstractXY_DataSet modeFunc = null;
		if (mode) {
			modeFunc = getCatalogMode(allFuncs, fractCalc);
			modeFunc.setName("Mode");
			funcs.add(modeFunc);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.CYAN));
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
		funcs.add(lower95_mean);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.RED.darker()));
		funcs.add(upper95_mean);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.RED.darker()));
		
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
		
		double maxDuration = 0;
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
				double closestDist = Double.POSITIVE_INFINITY;
				
				Location hypocenter = rup.getHypocenterLocation();
				Preconditions.checkNotNull(hypocenter);
				
				for (int sectIndex : rupSet.getSectionsIndicesForRup(rupIndex)) {
					for (int i=0; i<minMags.length; i++)
						if (rup.getMag() >= minMags[i])
							particRatesList.get(i)[sectIndex] += fractionalRate;
					
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
				
				Preconditions.checkState(closestSectIndex >= 0);
				for (int i=0; i<minMags.length; i++)
					if (rup.getMag() >= minMags[i])
						triggerRatesList.get(i)[closestSectIndex] += fractionalRate;
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
			File outputDir, String name, String prefix, double[] mags) throws IOException, GMT_MapException {
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
	
	private static void plotSectParticScatter(List<List<ETAS_EqkRupture>> catalogs, double duration,
			FaultSystemSolutionERF erf, File outputDir) throws IOException, GMT_MapException, RuntimeException {
		double[] minMags = { 0d };
		
		FaultSystemRupSet rupSet = erf.getSolution().getRupSet();
		
		// this is for map plotting
		CPT logRatioCPT = FaultBasedMapGen.getLogRatioCPT().rescale(-2, 2);
		CPT diffCPT = FaultBasedMapGen.getLogRatioCPT(); // will be rescaled
		List<LocationList> faults = Lists.newArrayList();
		for (FaultSectionPrefData sect : rupSet.getFaultSectionDataList())
			faults.add(sect.getFaultTrace());
		Region region = new CaliforniaRegions.RELM_TESTING();
		
		for (double minMag : minMags) {
			// each "MFD" will only have one value, for this minimum mag
			double[] subSectVals = FaultSysSolutionERF_Calc.calcParticipationRateForAllSects(erf, minMag);
			
			double[] catalogVals = new double[rupSet.getNumSections()];
			
			for (List<ETAS_EqkRupture> catalog : catalogs) {
				if (duration < 0)
					// detect duration from catalog
					duration = calcDurationYears(catalog);
				double rateEach = 1d/(catalogs.size()*duration);
				
				for (ETAS_EqkRupture rup : catalog) {
					int rupIndex = rup.getFSSIndex();
					
					if (rupIndex < 0 || rup.getMag() < minMag)
						continue;
					
					for (int sectIndex : rupSet.getSectionsIndicesForRup(rupIndex))
						catalogVals[sectIndex] += rateEach;
				}
			}
			
			String title = "Sub Section Participation";
			String prefix = "all_eqs_sect_partic";
			if (minMag > 0) {
				title += ", M≥"+(float)minMag;
				prefix += "_m"+(float)minMag;
			}
			
			CSVFile<String> csv = new CSVFile<String>(true);
			csv.addLine("Sect Index", "Sect Name", "Simulation Rate", "Long Term Rate",
					"Ratio", "Difference");
			
			double[] ratio = ratio(catalogVals, subSectVals);
			double[] diff = diff(catalogVals, subSectVals);
			
			for (int i=0; i<rupSet.getNumSections(); i++) {
				FaultSectionPrefData sect = rupSet.getFaultSectionData(i);
				
				csv.addLine(i+"", sect.getSectionName(), catalogVals[i]+"", subSectVals[i]+"",
						ratio[i]+"", diff[i]+"");
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
		else
			delta = 50d;
		
		HistogramFunction xVals = HistogramFunction.getEncompassingHistogram(0d, duration*0.98, delta);
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
			FaultSystemRupSet rupSet, int sectIndex, File outputDir) throws IOException {
		HashSet<Integer> ruptures = new HashSet<Integer>(rupSet.getRupturesForSection(sectIndex));
		
		List<Double> intervals = Lists.newArrayList();
		double maxValue = 0d;
		double sum = 0d;
		
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			double prevTime = -1;
			
			for (ETAS_EqkRupture rup : catalog) {
				if (rup.getFSSIndex() < 0 || !ruptures.contains(rup.getFSSIndex()))
					continue;
				double myTime = calcEventTimeYears(catalog, rup);
				
				if (prevTime >= 0) {
					double interval = myTime - prevTime;
					intervals.add(interval);
					maxValue = Math.max(maxValue, interval);
					sum += interval;
				}
				
				prevTime = myTime;
			}
		}
		double mean = sum/intervals.size();
		
		HistogramFunction hist = HistogramFunction.getEncompassingHistogram(0d, maxValue, 0.1*mean);
		hist.setName("Histogram");
		
		for (double interval : intervals)
			hist.add(interval, 1d);
		
		hist.normalizeBySumOfY_Vals();
		Range yRange = new Range(0d, hist.getMaxY()*1.1);
		
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
	
	private static ETAS_ParameterList loadEtasParamsFromMetadata(Element root)
			throws DocumentException, MalformedURLException {
		Element paramsEl = root.element(ETAS_ParameterList.XML_METADATA_NAME);
		
		return ETAS_ParameterList.fromXMLMetadata(paramsEl);
	}
	
	private static final String plotDirName = "plots";
	private static final String catsDirName = "selected_catalogs";
	
	public static void main(String[] args) throws IOException, GMT_MapException, RuntimeException, DocumentException {
		File mainDir = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations");
		double minLoadMag = -1;
		
		boolean plotMFDs = false;
		boolean plotExpectedComparison = false;
		boolean plotSectRates = false;
		boolean plotTemporalDecay = false;
		boolean plotDistanceDecay = false;
		boolean plotMaxMagHist = false;
		boolean plotGenerations = false;
		boolean plotGriddedNucleation = false;
		boolean writeTimeFromPrevSupra = false;
		boolean plotSectScatter = false;
		boolean plotGridScatter = false;
		boolean plotStationarity = true;
		boolean plotSubSectRecurrence = true;
		boolean writeCatsForViz = false;
		
//		boolean plotMFDs = true;
//		boolean plotExpectedComparison = true;
//		boolean plotSectRates = true;
//		boolean plotTemporalDecay = true;
//		boolean plotDistanceDecay = true;
//		boolean plotMaxMagHist = true;
//		boolean plotGenerations = true;
//		boolean plotGriddedNucleation = true;
//		boolean writeTimeFromPrevSupra = true;
//		boolean plotSectScatter = true;
//		boolean plotGridScatter = true;
//		boolean plotStationarity = true;
//		boolean plotSubSectRecurrence = true;
//		boolean writeCatsForViz = false;
		
		boolean useDefaultETASParamsIfMissing = true;
		boolean useActualDurations = false;
		
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
		
		boolean skipEmpty = true;
//		double minDurationForInclusion = 0d;
		double minDurationForInclusion = 0.5d;
		
		List<String> names = Lists.newArrayList();
		List<File> resultsZipFiles = Lists.newArrayList();
		List<TestScenario> scenarios = Lists.newArrayList();
		
		names.add("1000yr Full TD, NoLTR");
		resultsZipFiles.add(new File(mainDir, "2015_11_09-spontaneous-1000yr-full_td-noApplyLTR-combined/results_m4.bin"));
		scenarios.add(null);
		
//		names.add("100yr Full TD, CF=10");
//		resultsZipFiles.add(new File(mainDir, "2015_10_15-spontaneous-100yr-full_td-maxChar10.0/results.zip"));
//		scenarios.add(null);
		
//		names.add("100yr No ERT, CF=10");
//		resultsZipFiles.add(new File(mainDir, "2015_10_15-spontaneous-100yr-no_ert-maxChar10.0/results.zip"));
//		scenarios.add(null);
		
//		names.add("200yr Full TD, CF=10");
//		resultsZipFiles.add(new File(mainDir, "2015_10_15-spontaneous-200yr-full_td-maxChar10.0/results.zip"));
//		scenarios.add(null);
		
//		names.add("200yr No ERT, CF=10");
//		resultsZipFiles.add(new File(mainDir, "2015_10_15-spontaneous-200yr-no_ert-maxChar10.0/results.zip"));
//		scenarios.add(null);
		
//		names.add("Mojave M5 Full TD, CF=10");
//		resultsZipFiles.add(new File(mainDir, "2015_10_15-mojave_m5-full_td-maxChar10.0/results.bin"));
//		scenarios.add(TestScenario.MOJAVE_M5);
		
//		names.add("Mojave M5 No ERT, CF=10");
//		resultsZipFiles.add(new File(mainDir, "2015_10_15-mojave_m5-no_ert-maxChar10.0/results.bin"));
//		scenarios.add(TestScenario.MOJAVE_M5);
		
//		names.add("Mojave M5.5 Full TD, CF=10");
//		resultsZipFiles.add(new File(mainDir, "2015_10_15-mojave_m5p5-full_td-maxChar10.0/results.bin"));
//		scenarios.add(TestScenario.MOJAVE_M5p5);
		
//		names.add("Mojave M5.5 No ERT, CF=10");
//		resultsZipFiles.add(new File(mainDir, "2015_10_15-mojave_m5p5-no_ert-maxChar10.0/results.bin"));
//		scenarios.add(TestScenario.MOJAVE_M5p5);
		
//		names.add("Mojave M6.3 Finite Full TD, CF=10");
//		resultsZipFiles.add(new File(mainDir, "2015_10_15-mojave_m6pt3_fss-full_td-maxChar10.0/results.bin"));
//		scenarios.add(TestScenario.MOJAVE_M6pt3_FSS);
		
//		names.add("Mojave M6.3 Finite No ERT, CF=10");
//		resultsZipFiles.add(new File(mainDir, "2015_10_15-mojave_m6pt3_fss-no_ert-maxChar10.0/results.bin"));
//		scenarios.add(TestScenario.MOJAVE_M6pt3_FSS);
		
//		names.add("Mojave M6.3 Pt. Src. Full TD, CF=10");
//		resultsZipFiles.add(new File(mainDir, "2015_10_15-mojave_m6pt3_ptsrc-full_td-maxChar10.0/results.bin"));
//		scenarios.add(TestScenario.MOJAVE_M6pt3_ptSrc);
		
//		names.add("Mojave M6.3 Pt. Src. No ERT, CF=10");
//		resultsZipFiles.add(new File(mainDir, "2015_10_15-mojave_m6pt3_ptsrc-no_ert-maxChar10.0/results.bin"));
//		scenarios.add(TestScenario.MOJAVE_M6pt3_ptSrc);
		
//		names.add("Mojave M7 Full TD, CF=10");
//		resultsZipFiles.add(new File(mainDir, "2015_10_15-mojave_m7-full_td-maxChar10.0/results.bin"));
//		scenarios.add(TestScenario.MOJAVE_M7);
		
//		names.add("Mojave M7 No ERT, CF=10");
//		resultsZipFiles.add(new File(mainDir, "2015_10_15-mojave_m7-no_ert-maxChar10.0/results.bin"));
//		scenarios.add(TestScenario.MOJAVE_M7);
		
//		names.add("Mojave M7.4 Full TD, CF=10");
//		resultsZipFiles.add(new File(mainDir, "2015_10_15-mojave_m7pt4-full_td-maxChar10.0/results_m4.bin"));
//		scenarios.add(TestScenario.MOJAVE_M7pt4);
		
//		names.add("Mojave M7.4 No ERT, CF=10");
//		resultsZipFiles.add(new File(mainDir, "2015_10_15-mojave_m7pt4-no_ert-maxChar10.0/results_m4.bin"));
//		scenarios.add(TestScenario.MOJAVE_M7pt4);
		
//		names.add("Mojave M7.8 Full TD, CF=10");
//		resultsZipFiles.add(new File(mainDir, "2015_10_15-mojave_m7pt8-full_td-maxChar10.0/results_m4.bin"));
//		scenarios.add(TestScenario.MOJAVE_M7pt8);
		
//		names.add("Mojave M7.8 No ERT, CF=10");
//		resultsZipFiles.add(new File(mainDir, "2015_10_15-mojave_m7pt8-no_ert-maxChar10.0/results_m4.bin"));
//		scenarios.add(TestScenario.MOJAVE_M7pt8);
		
		for (int n=0; n<names.size(); n++) {
			String name = names.get(n);
			File resultsZipFile = resultsZipFiles.get(n);
			TestScenario scenario = scenarios.get(n);
			
			if (scenario != null && scenario.getFSS_Index() >= 0)
				scenario.updateMag(fss.getRupSet().getMagForRup(scenario.getFSS_Index()));
			
			// parent ID for the trigger rupture
			int triggerParentID;
			if (scenario == null)
				triggerParentID = -1;
			else
				triggerParentID = 0;
			
			System.out.println("Loading "+name+" from "+resultsZipFile.getAbsolutePath());
			
			System.gc();
			
			RuptureSurface surf;
			if (scenario == null)
				surf = null;
			else if (scenario.getLocation() != null)
				surf = new PointSurface(scenario.getLocation());
			else
				surf = fss.getRupSet().getSurfaceForRupupture(scenario.getFSS_Index(), 1d, false);
			
			File parentDir = resultsZipFile.getParentFile();
			
			File outputDir = new File(resultsZipFile.getParentFile(), plotDirName);
			
			File metadataFile = new File(resultsZipFile.getParentFile(), "metadata.xml");
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
			double duration;
			if (metadataRootEl != null) {
				Element paramsEl = metadataRootEl.element(MPJ_ETAS_Simulator.OTHER_PARAMS_EL_NAME);
				ot = Long.parseLong(paramsEl.attributeValue("ot"));
				duration = Double.parseDouble(paramsEl.attributeValue("duration"));
			} else {
				System.out.println("WARNING: Assuming 1 year 2014");
				ot = Math.round((2014.0-1970.0)*ProbabilityModelsCalc.MILLISEC_PER_YEAR); // occurs at 2014
				duration = 1d;
			}
			if (useActualDurations)
				duration = -1;
			
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
			Stopwatch timer = Stopwatch.createStarted();
			List<List<ETAS_EqkRupture>> catalogs = ETAS_CatalogIO.loadCatalogs(resultsZipFile, minLoadMag, true);
			timer.stop();
			long secs = timer.elapsed(TimeUnit.SECONDS);
			if (secs > 60)
				System.out.println("Catalog loading took "+(float)((double)secs/60d)+" minutes");
			else
				System.out.println("Catalog loading took "+secs+" seconds");
			
			if (skipEmpty) {
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
				if (myDuration < minDurationForInclusion) {
					catalogs.remove(i);
					skippedDuration++;
				} else {
					durationTrack.addValue(myDuration);
				}
			}
			if (skippedDuration > 0)
				System.out.println("Removed "+skippedDuration+" catalgos that were too short");
			System.out.println("Actual duration: "+durationTrack);
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
			if (triggerParentID < 0 && (duration >= 100d || catalogs.size() > 1000)) {
				System.out.println("Creating ERF for comparisons");
				erf = new FaultSystemSolutionERF(fss);
				erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.POISSON);
				erf.setParameter(IncludeBackgroundParam.NAME, IncludeBackgroundOption.INCLUDE);
				erf.getTimeSpan().setDuration(1d);
				erf.updateForecast();
			}
			
			if (plotMFDs) {
				System.out.println("Plotting MFDs");
				
				ArbIncrementalMagFreqDist[] subMFDs = plotMFD(childrenCatalogs, duration, erf, outputDir, name, fullFileName);
				if (triggerParentID >= 0)
					plotMFD(primaryCatalogs, duration, null, outputDir, subsetName+" "+name, subsetFileName+"_aftershocks");
				else
					plotMFD(primaryCatalogs, duration, erf, outputDir, subsetName+" "+name, subsetFileName+"_events");
				
				plotFractWithMagAbove(childrenCatalogs, subMFDs, scenario, outputDir, name, fullFileName+"_fract_above_mag");
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
				plotSectRates(childrenCatalogs, duration, fss.getRupSet(), minMags, outputDir,
						name+" "+fullName, fullFileName+"_sect");
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
			}
			
			if (plotSectScatter && erf != null) {
				System.out.println("Plotting section participation scatter");
				plotSectParticScatter(catalogs, duration, erf, outputDir);
			}
			
			if (plotGridScatter && erf != null) {
				System.out.println("Plotting gridded nucleation scatter");
				plotGriddedNucleationScatter(catalogs, duration, erf, outputDir);
			}
			
			if (plotStationarity && (duration > 1d || duration < 0) && triggerParentID < 0) {
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
					plotSubSectRecurrenceHist(primaryCatalogs, fss.getRupSet(), sectIndex, outputDir);
			}
			
			if (writeCatsForViz) {
				System.out.println("Writing catalogs for vizualisation in SCEC-VDO");
				writeCatalogsForViz(childrenCatalogs, scenario, new File(parentDir, catsDirName), 5);
			}
			
			if (scenario == null && writeTimeFromPrevSupra) {
				System.out.println("Plotting time since last supra");
				writeTimeFromPrevSupraHist(catalogs, outputDir);
			}
			
			writeHTML(parentDir, scenario, name, catalogs, duration);
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
			List<List<ETAS_EqkRupture>> catalogs, double duration) throws IOException {
		System.out.println("Writing HTML");
		
		FileWriter fw = new FileWriter(new File(outputDir, "HEADER.html"));
		
		fw.write("<h1 style=\"font-family:'HelveticaNeue-Light', sans-serif; font-weight:normal;\">"
				+scenName+"</h1>\n");
		fw.write("<br>\n");
		fw.write("<p style=\"font-family:'HelveticaNeue-Light', sans-serif; font-weight:normal; width:"+html_w_px+";\">\n");
		if (scenario != null) {
			fw.write("<h2>Scenario Information</h2>\n");
			fw.write("<b>Magnitude:</b> "+scenario.getMagnitude()+"<br>\n");
			fw.write("<b>Supra-seismogenic? </b> "+(scenario.getFSS_Index()>=0)+"<br>\n");
			fw.write("<br>\n");
		}
		
		fw.write("<h2>Simulation Information</h2>\n");
		fw.write("<b>Num Catalogs:</b> "+catalogs.size()+"<br>\n");
		fw.write("<b>Simulation Duration:</b> "+duration+" years<br>\n");
		fw.write("<br>\n");
		
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
