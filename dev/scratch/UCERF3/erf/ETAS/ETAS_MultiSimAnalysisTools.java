package scratch.UCERF3.erf.ETAS;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.StatUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.jfree.data.Range;
import org.opensha.commons.calc.FractileCurveCalculator;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.AbstractXY_DataSet;
import org.opensha.commons.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.data.function.XY_DataSetList;
import org.opensha.commons.data.region.CaliforniaRegions;
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
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.util.ComparablePairing;
import org.opensha.commons.util.DataUtils;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.commons.util.XMLUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.PointSurface;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.analysis.FaultBasedMapGen;
import scratch.UCERF3.erf.ETAS.ETAS_Simulator.TestScenario;
import scratch.UCERF3.erf.ETAS.ETAS_Params.ETAS_ParameterList;
import scratch.UCERF3.erf.utils.ProbabilityModelsCalc;
import scratch.UCERF3.griddedSeismicity.AbstractGridSourceProvider;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.kevin.ucerf3.etas.MPJ_ETAS_Simulator;

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
		
		gp.setBackgroundColor(Color.WHITE);
		gp.setTickLabelFontSize(18);
		gp.setAxisLabelFontSize(20);
		gp.setPlotLabelFontSize(21);
		
		gp.drawGraphPanel(spec, false, true);
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPNG(new File(outputDir, prefix+".png").getAbsolutePath());
		gp.saveAsPDF(new File(outputDir, prefix+".pdf").getAbsolutePath());
		gp.saveAsTXT(new File(outputDir, prefix+".txt").getAbsolutePath());
	}
	
	private static void plotMFD(List<List<ETAS_EqkRupture>> catalogs, File outputDir, String name, String prefix)
			throws IOException {
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
		
		double rate = 1d/catalogs.size();
		
		for (int i=0; i<catalogs.size(); i++) {
			List<ETAS_EqkRupture> catalog = catalogs.get(i);
			for (ETAS_EqkRupture rup : catalog) {
				subMFDs[i].addResampledMagRate(rup.getMag(), 1d, true);
			}
//			for (int n=0; n<mfd.size(); n++)
//				mfd.add(n, subMFDs[i].getY(n)*rate);
			cmlSubMFDs[i] = subMFDs[i].getCumRateDistWithOffset();
		}
		
		
		
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
			
			getFractilePlotFuncs(mySubMFDs, fractiles, true, funcs, chars, csvFile);
			
			PlotSpec spec = new PlotSpec(funcs, chars, name+" MFD", "Magnitude", yAxisLabel);
			spec.setLegendVisible(true);
			
			HeadlessGraphPanel gp = new HeadlessGraphPanel();
			gp.setUserBounds(mfdMinMag, subMFDs[0].getMaxX(), mfdMinY, mfdMaxY);
			
			gp.setBackgroundColor(Color.WHITE);
			gp.setTickLabelFontSize(18);
			gp.setAxisLabelFontSize(20);
			gp.setPlotLabelFontSize(21);
			
			gp.drawGraphPanel(spec, false, true);
			gp.getCartPanel().setSize(1000, 800);
			gp.saveAsPNG(new File(outputDir, myPrefix+".png").getAbsolutePath());
			gp.saveAsPDF(new File(outputDir, myPrefix+".pdf").getAbsolutePath());
			gp.saveAsTXT(new File(outputDir, myPrefix+".txt").getAbsolutePath());
		}
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
		for (int i=0; i<fractiles.length; i++) {
			double fractile = fractiles[i];
			AbstractXY_DataSet fractFunc = fractCalc.getFractile(fractile);
			fractileFuncs.add(fractFunc);
			
			String nameAdd;
			if (i == 0 || i == fractiles.length-1)
				nameAdd = " Fractile";
			else
				nameAdd = "";
			
			fractFunc.setName((float)(fractile*100d)+"%"+nameAdd);
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
		lower95_mean.setName("Lower 95% of Mean");
		EvenlyDiscretizedFunc upper95_mean = new EvenlyDiscretizedFunc(
				allFuncs[0].getMinX(), numX, allFuncs[0].getDelta());
		upper95_mean.setName("Upper 95% of Mean");
		
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
	
	public static void plotAftershockRateVsLogTimeHistForRup(List<List<ETAS_EqkRupture>> catalogs, ETAS_ParameterList params,
			long rupOT_millis, File outputDir, String name, String prefix) throws IOException {
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
		
		if (params != null) {
			HistogramFunction targetFunc = ETAS_Utils.getRateWithLogTimeFunc(params.get_k(), params.get_p(),
					7d, ETAS_Utils.magMin_DEFAULT, params.get_c(), firstLogDay, lastLogDay, deltaLogDay);
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
		
		gp.setBackgroundColor(Color.WHITE);
		gp.setTickLabelFontSize(18);
		gp.setAxisLabelFontSize(20);
		gp.setPlotLabelFontSize(21);
		
		gp.drawGraphPanel(spec, false, true);
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPNG(new File(outputDir, prefix+".png").getAbsolutePath());
		gp.saveAsPDF(new File(outputDir, prefix+".pdf").getAbsolutePath());
		gp.saveAsTXT(new File(outputDir, prefix+".txt").getAbsolutePath());
	}
	
	public static void plotDistDecay(List<List<ETAS_EqkRupture>> catalogs, ETAS_ParameterList params,
			RuptureSurface surf, File outputDir, String name, String prefix) throws IOException {
		EvenlyDiscretizedFunc[] funcsArray = new EvenlyDiscretizedFunc[catalogs.size()];
		
		double histLogMin = -2.0;
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
		
		gp.setBackgroundColor(Color.WHITE);
		gp.setTickLabelFontSize(18);
		gp.setAxisLabelFontSize(20);
		gp.setPlotLabelFontSize(21);
		
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
		
		gp.setBackgroundColor(Color.WHITE);
		gp.setTickLabelFontSize(18);
		gp.setAxisLabelFontSize(20);
		gp.setPlotLabelFontSize(21);
		
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
		
		gp.setBackgroundColor(Color.WHITE);
		gp.setTickLabelFontSize(18);
		gp.setAxisLabelFontSize(20);
		gp.setPlotLabelFontSize(21);
		
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
	public static void plotSectRates(List<List<ETAS_EqkRupture>> catalogs, FaultSystemRupSet rupSet,
			double[] minMags, File outputDir, String title, String prefix)
					throws IOException, GMT_MapException, RuntimeException {
		List<double[]> particRatesList = Lists.newArrayList();
		for (int i=0; i<minMags.length; i++)
			particRatesList.add(new double[rupSet.getNumSections()]);
		List<double[]> triggerRatesList = Lists.newArrayList();
		for (int i=0; i<minMags.length; i++)
			triggerRatesList.add(new double[rupSet.getNumSections()]);
		
		double fractionalRate = 1d/(double)catalogs.size();
		
		Map<Integer, List<Location>> locsForSectsMap = Maps.newHashMap();
		
		for (List<ETAS_EqkRupture> catalog : catalogs) {
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
		double maxRate = 0;
		for (double[] particRates : particRatesList)
			maxRate = Math.max(maxRate, StatUtils.max(particRates));
		cpt = cpt.rescale(Math.log10(fractionalRate), Math.ceil(Math.log10(maxRate)));
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
					prefix+"_partic"+prefixAdd,false, false, title+titleAdd+" Participation Rate");
			
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
		
		double[] fullData = new double[catalogs.size()];
		for (int i=0; i<catalogs.size(); i++)
			fullData[i] = ETAS_SimAnalysisTools.getMaxMag(catalogs.get(i));
		populateHistWithInfo(fullHist, fullData);
		
		if (primaryCatalogs != null) {
			double[] primaryData = new double[primaryCatalogs.size()];
			for (int i=0; i<primaryCatalogs.size(); i++)
				primaryData[i] = ETAS_SimAnalysisTools.getMaxMag(primaryCatalogs.get(i));
			populateHistWithInfo(primaryHist, primaryData);
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
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		
		gp.setBackgroundColor(Color.WHITE);
		gp.setTickLabelFontSize(18);
		gp.setAxisLabelFontSize(20);
		gp.setPlotLabelFontSize(21);
		
		double histDelta = fullHist.getDelta();
		double minX = fullHist.getMinX()-0.5*histDelta;
		double maxX = fullHist.getMaxX()-0.5*histDelta;
		
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
		
		gp.setBackgroundColor(Color.WHITE);
		gp.setTickLabelFontSize(18);
		gp.setAxisLabelFontSize(20);
		gp.setPlotLabelFontSize(21);
		
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
	
	private static void plotCubeNucleationRates(List<List<ETAS_EqkRupture>> catalogs,
			File outputDir, String name, String prefix, double[] mags) throws IOException, GMT_MapException {
		double discr = 0.02;
		GriddedRegion reg = new GriddedRegion(new CaliforniaRegions.RELM_TESTING(),
				discr, GriddedRegion.ANCHOR_0_0);
		
		GriddedGeoDataSet[] xyzs = new GriddedGeoDataSet[mags.length];
		for (int i=0; i<xyzs.length; i++)
			xyzs[i] = new GriddedGeoDataSet(reg, false);
		
		// max duration from any catalog. we don't have the actual duration so we can
		// detect it as the max difference between first/last event
		double duration = 0;
		int numSkipped = 0;
		
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			long myDuration = catalog.get(catalog.size()-1).getOriginTime() - catalog.get(0).getOriginTime();
			duration = Math.max(duration, (double)myDuration/MILLIS_PER_YEAR);
			
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
						xyzs[i].set(index, xyzs[i].get(index)+1d);
			}
		}
		
		duration = Math.round(duration*100d)/100d;
		
		System.out.println("Determined duration: "+duration);
		System.out.println("Skipped "+numSkipped+" events outside of region");
		
		double scalar = 1d/(catalogs.size()*duration);
		for (GriddedGeoDataSet xyz : xyzs)
			xyz.scale(scalar);
		
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
			
			double mag = mags[i];
			String label = "Log10("+name+" M>="+(float)mag+" Nucleation Rate)";
			FaultBasedMapGen.plotMap(outputDir, prefix+"_m"+(float)mag, false,
					FaultBasedMapGen.buildMap(cpt.rescale(minZ, maxZ), null, null,
							xyzs[i], discr, plotReg, false, label));
		}
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
		
//		boolean plotMFDs = true;
//		boolean plotExpectedComparison = true;
//		boolean plotSectRates = true;
//		boolean plotTemporalDecay = true;
//		boolean plotDistanceDecay = true;
//		boolean plotMaxMagHist = true;
//		boolean writeCatsForViz = false;
		
		boolean plotMFDs = true;
		boolean plotExpectedComparison = true;
		boolean plotSectRates = true;
		boolean plotTemporalDecay = true;
		boolean plotDistanceDecay = true;
		boolean plotMaxMagHist = true;
		boolean plotGenerations = true;
		boolean plotGriddedNucleation = true;
		boolean writeCatsForViz = false;
		
		boolean useDefaultETASParamsIfMissing = true;
		
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
		FaultSystemSolution fss = FaultSystemIO.loadSol(fssFile);
		
		List<String> names = Lists.newArrayList();
		List<File> resultsZipFiles = Lists.newArrayList();
		List<TestScenario> scenarios = Lists.newArrayList();
		
//		names.add("Mojave M5 Full TD");
//		resultsZipFiles.add(new File(mainDir, "2015_08_07-mojave_m5-full_td/results.bin"));
//		scenarios.add(TestScenario.MOJAVE_M5);
//		
//		names.add("Mojave M5 Full TD, GR Corr.");
//		resultsZipFiles.add(new File(mainDir, "2015_08_07-mojave_m5-full_td-grCorr/results.bin"));
//		scenarios.add(TestScenario.MOJAVE_M5);
//		
//		names.add("Mojave M5.5 Full TD");
//		resultsZipFiles.add(new File(mainDir, "2015_08_21-mojave_m5p5-full_td/results.bin"));
//		scenarios.add(TestScenario.MOJAVE_M5p5);
//		
//		names.add("Mojave M5.5 Full TD, GR Corr.");
//		resultsZipFiles.add(new File(mainDir, "2015_08_21-mojave_m5p5-full_td-grCorr/results.bin"));
//		scenarios.add(TestScenario.MOJAVE_M5p5);
//		
//		names.add("Mojave M6 Full TD");
//		resultsZipFiles.add(new File(mainDir, "2015_08_07-mojave_m6-full_td/results.bin"));
//		scenarios.add(TestScenario.MOJAVE_M6);
//		
//		names.add("Mojave M6 Full TD, GR Corr.");
//		resultsZipFiles.add(new File(mainDir, "2015_08_07-mojave_m6-full_td-grCorr/results.bin"));
//		scenarios.add(TestScenario.MOJAVE_M6);
//		
		names.add("Mojave M7 Full TD");
		resultsZipFiles.add(new File(mainDir, "2015_08_07-mojave_m7-full_td/results.bin"));
		scenarios.add(TestScenario.MOJAVE_M7);
//		
//		names.add("Mojave M7 Full TD, GR Corr.");
//		resultsZipFiles.add(new File(mainDir, "2015_08_07-mojave_m7-full_td-grCorr/results.bin"));
//		scenarios.add(TestScenario.MOJAVE_M7);
//		
//		names.add("Mojave M7 No ERT");
//		resultsZipFiles.add(new File(mainDir, "2015_08_07-mojave_m7-no_ert/results_m4.bin"));
//		scenarios.add(TestScenario.MOJAVE_M7);
//		
//		names.add("Mojave M7 No ERT, GR Corr.");
//		resultsZipFiles.add(new File(mainDir, "2015_08_07-mojave_m7-no_ert-grCorr/results.bin"));
//		scenarios.add(TestScenario.MOJAVE_M7);
//		
////		names.add("Mojave M7 Poisson");				// BAD, none completed
////		resultsZipFiles.add(new File(mainDir, "2015_08_07-mojave_m7-poisson/results.zip"));
////		scenarios.add(TestScenario.MOJAVE_M7);
//		
//		names.add("Mojave M7 Poisson, GR Corr.");
//		resultsZipFiles.add(new File(mainDir, "2015_08_07-mojave_m7-poisson-grCorr/results_m4.bin"));
//		scenarios.add(TestScenario.MOJAVE_M7);
		
		// parent ID for the trigger rupture
		int triggerParentID = 0;
		
		for (int n=0; n<names.size(); n++) {
			String name = names.get(n);
			File resultsZipFile = resultsZipFiles.get(n);
			TestScenario scenario = scenarios.get(n);
			
			if (scenario.getFSS_Index() >= 0)
				scenario.updateMag(fss.getRupSet().getMagForRup(scenario.getFSS_Index()));
			
			System.out.println("Loading "+name+" from "+resultsZipFile.getAbsolutePath());
			
			System.gc();
			
			RuptureSurface surf;
			if (scenario.getLocation() != null)
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
			if (metadataRootEl != null) {
				ot = Long.parseLong(metadataRootEl.element(MPJ_ETAS_Simulator.OTHER_PARAMS_EL_NAME).attributeValue("ot"));
			} else {
				ot = Math.round((2014.0-1970.0)*ProbabilityModelsCalc.MILLISEC_PER_YEAR); // occurs at 2014
			}
			
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
			List<List<ETAS_EqkRupture>> catalogs = ETAS_CatalogIO.loadCatalogs(resultsZipFile, minLoadMag);
			timer.stop();
			long secs = timer.elapsed(TimeUnit.SECONDS);
			if (secs > 60)
				System.out.println("Catalog loading took "+(float)((double)secs/60d)+" minutes");
			else
				System.out.println("Catalog loading took "+secs+" seconds");
			
			List<List<ETAS_EqkRupture>> childrenCatalogs = Lists.newArrayList();
			for (List<ETAS_EqkRupture> catalog : catalogs)
				childrenCatalogs.add(ETAS_SimAnalysisTools.getChildrenFromCatalog(catalog, triggerParentID));
			
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
			for (List<ETAS_EqkRupture> catalog : catalogs)
				primaryCatalogs.add(ETAS_SimAnalysisTools.getPrimaryAftershocks(catalog, triggerParentID));
			
			if (plotMFDs) {
				System.out.println("Plotting MFDs");
				plotMFD(childrenCatalogs, outputDir, name, "full_children");
				
				plotMFD(primaryCatalogs, outputDir, "Primary "+name, "primary_aftershocks");
			}
			
			if (plotExpectedComparison) {
				System.out.println("Plotting Expected Comparison MFDs");
				plotExpectedSupraComparisonMFD(primaryCatalogs, outputDir, "Primary "+name, "primary_supra_compare_to_expected");
				
				// now do first/last half
				int numCatalogs = primaryCatalogs.size();
				List<List<ETAS_EqkRupture>> firstHalfPrimary = primaryCatalogs.subList(0, numCatalogs/2);
				List<List<ETAS_EqkRupture>> secondHalfPrimary = primaryCatalogs.subList(firstHalfPrimary.size(), primaryCatalogs.size());
				plotExpectedSupraComparisonMFD(firstHalfPrimary, outputDir, "Primary "+name,
						"primary_supra_compare_to_expected_first"+firstHalfPrimary.size());
				plotExpectedSupraComparisonMFD(secondHalfPrimary, outputDir, "Primary "+name,
						"primary_supra_compare_to_expected_last"+secondHalfPrimary.size());
			}
			
			if (plotSectRates) {
				// sub section partic/trigger rates
				System.out.println("Plotting Sub Sect Rates");
				double[] minMags = { 0, 6.7, 7.8 };
				plotSectRates(childrenCatalogs, fss.getRupSet(), minMags, outputDir, name+" Children", "full_children_sect");
				plotSectRates(primaryCatalogs, fss.getRupSet(), minMags, outputDir, name+" Primary", "primary_sect");
			}
			
			if (plotTemporalDecay) {
				// temporal decay
				System.out.println("Plotting Temporal Decay");
				plotAftershockRateVsLogTimeHistForRup(primaryCatalogs, params, ot, outputDir, name+" Primary", "primary_temporal_decay");
				plotAftershockRateVsLogTimeHistForRup(childrenCatalogs, params, ot, outputDir, name, "full_children_temporal_decay");
			}
			
			if (plotDistanceDecay) {
				// dist decay trigger loc
				System.out.println("Plotting Trigger Loc Dist Decay");
				plotDistDecay(primaryCatalogs, params, null, outputDir, name+" Primary", "primary_dist_decay_trigger");
				plotDistDecay(childrenCatalogs, params, null, outputDir, name, "full_children_dist_decay_trigger");
			
				// dist decay rup surf
				if (scenario.getFSS_Index() >= 0) {
					System.out.println("Plotting Surface Dist Decay");
					Stopwatch watch = Stopwatch.createStarted();
					plotDistDecay(primaryCatalogs, params, surf, outputDir, name+" Primary", "primary_dist_decay_surf");
					double mins = (watch.elapsed(TimeUnit.SECONDS))/60d;
					System.out.println("Primary surf dist decay took "+(float)mins+" mins");
					watch.reset();
					watch.start();
					plotDistDecay(childrenCatalogs, params, surf, outputDir, name, "full_children_dist_decay_surf");
					watch.stop();
					mins = (watch.elapsed(TimeUnit.SECONDS))/60d;
					System.out.println("Full surf dist decay took "+(float)mins+" mins");
				}
			}
			
			if (plotMaxMagHist)
				plotMaxTriggeredMagHist(childrenCatalogs, primaryCatalogs, scenario, outputDir, name, "max_mag_hist");
			
			if (plotGenerations)
				plotNumEventsPerGeneration(childrenCatalogs, outputDir, name, "full_children_generations");
			
			if (plotGriddedNucleation) {
				double[] mags = { 2.5, 6.7, 7.8 };
				plotCubeNucleationRates(childrenCatalogs, outputDir, name, "full_children_gridded_nucl", mags);
				plotCubeNucleationRates(primaryCatalogs, outputDir, name, "primary_gridded_nucl", mags);
			}
			
			if (writeCatsForViz)
				writeCatalogsForViz(childrenCatalogs, scenario, new File(parentDir, catsDirName), 5);
			
			writeHTML(parentDir, scenario, name, catalogs);
		}
	}
	
	private static final int html_w_px = 800;
	
	private static void writeHTML(File outputDir, TestScenario scenario, String scenName, List<List<ETAS_EqkRupture>> catalogs)
			throws IOException {
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
		
		// determine duration
		long maxDur = 0l;
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			long startTime = catalog.get(0).getOriginTime();
			long endTime = catalog.get(catalog.size()-1).getOriginTime();
			long delta = endTime - startTime;
			if (delta > maxDur)
				maxDur = delta;
		}
		double deltaSecs = (double)maxDur/1000d;
		double deltaMins = deltaSecs/60d;
		double deltaHours = deltaMins/60d;
		double deltaDays = deltaHours/24d;
		double deltaYears = deltaDays/365.25d;
		// round
		double duration = Math.round(deltaYears);
		
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
