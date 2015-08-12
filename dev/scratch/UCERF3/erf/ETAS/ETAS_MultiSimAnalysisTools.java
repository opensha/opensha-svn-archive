package scratch.UCERF3.erf.ETAS;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.math3.stat.StatUtils;
import org.opensha.commons.calc.FractileCurveCalculator;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.AbstractXY_DataSet;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.data.function.XY_DataSetList;
import org.opensha.commons.eq.MagUtils;
import org.opensha.commons.gui.plot.HeadlessGraphPanel;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.util.ComparablePairing;
import org.opensha.commons.util.DataUtils;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import scratch.UCERF3.erf.utils.ProbabilityModelsCalc;
import scratch.UCERF3.griddedSeismicity.AbstractGridSourceProvider;

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
				catMinMag = Math.min(catMinMag, minMag);
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
		for (int i=0; i<numToTrim; i++) {
			minMag += mfdDelta;
			numMag--;
		}
		
		ArbIncrementalMagFreqDist mfd = new ArbIncrementalMagFreqDist(minMag, numMag, mfdDelta);
		mfd.setName("Total");
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
			for (int n=0; n<mfd.size(); n++)
				mfd.add(n, subMFDs[i].getY(n)*rate);
			cmlSubMFDs[i] = subMFDs[i].getCumRateDistWithOffset();
		}
		
		
		
		boolean[] cumulatives = { false, true };
		
		for (boolean cumulative : cumulatives) {
			EvenlyDiscretizedFunc myMFD;
			EvenlyDiscretizedFunc[] mySubMFDs;
			String yAxisLabel;
			String myPrefix = prefix;
			if (myPrefix == null || myPrefix.isEmpty())
				myPrefix = "";
			else
				myPrefix += "_";
			if (cumulative) {
				myMFD = mfd.getCumRateDistWithOffset();
				mySubMFDs = cmlSubMFDs;
				yAxisLabel = "Cumulative Rate (1/yr)";
				myPrefix += "cumulative";
			} else {
				myMFD = mfd;
				mySubMFDs = subMFDs;
				yAxisLabel = "Incremental Rate (1/yr)";
				myPrefix += "incremental";
			}
			
			FractileCurveCalculator fractCalc = getFractileCalc(mySubMFDs);
			
			List<XY_DataSet> funcs = Lists.newArrayList();
			List<PlotCurveCharacterstics> chars = Lists.newArrayList();
			
			AbstractXY_DataSet fract025 = fractCalc.getFractile(0.025);
			fract025.setName("2.5% Fractile");
			AbstractXY_DataSet fract975 = fractCalc.getFractile(0.975);
			fract975.setName("97.5% Fractile");
			funcs.add(fract025);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.GREEN.darker()));
			funcs.add(fract975);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.GREEN.darker()));
			
			AbstractXY_DataSet median = fractCalc.getFractile(0.5);
			median.setName("Median");
			funcs.add(median);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLUE));
			
			double[] stdDevs = new double[myMFD.size()];
			double[] sdoms = new double[myMFD.size()];
			EvenlyDiscretizedFunc lower95_mean = new EvenlyDiscretizedFunc(myMFD.getMinX(), myMFD.size(), myMFD.getDelta());
			lower95_mean.setName("Lower 95% of Mean");
			EvenlyDiscretizedFunc upper95_mean = new EvenlyDiscretizedFunc(myMFD.getMinX(), myMFD.size(), myMFD.getDelta());
			upper95_mean.setName("Upper 95% of Mean");
			
			for (int n=0; n<myMFD.size(); n++) {
				double[] vals = new double[subMFDs.length];
				for (int i=0; i<subMFDs.length; i++)
					vals[i] = subMFDs[i].getY(n);
				stdDevs[n] = Math.sqrt(StatUtils.variance(vals));
				sdoms[n] = stdDevs[n]/Math.sqrt(subMFDs.length);
				
				double mean = myMFD.getY(n);
				lower95_mean.set(n, mean - 1.98*sdoms[n]);
				upper95_mean.set(n, mean + 1.98*sdoms[n]);
			}
			funcs.add(lower95_mean);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.RED.darker()));
			funcs.add(upper95_mean);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.RED.darker()));
			
			myMFD.setName("Mean");
			funcs.add(myMFD);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
			
			PlotSpec spec = new PlotSpec(funcs, chars, name+" MFD", "Magnitude", yAxisLabel);
			spec.setLegendVisible(true);
			
			HeadlessGraphPanel gp = new HeadlessGraphPanel();
			gp.setUserBounds(mfdMinMag, myMFD.getMaxX(), mfdMinY, mfdMaxY);
			
			gp.setBackgroundColor(Color.WHITE);
			gp.setTickLabelFontSize(18);
			gp.setAxisLabelFontSize(20);
			gp.setPlotLabelFontSize(21);
			
			gp.drawGraphPanel(spec, false, true);
			gp.getCartPanel().setSize(1000, 800);
			gp.saveAsPNG(new File(outputDir, myPrefix+".png").getAbsolutePath());
			gp.saveAsPDF(new File(outputDir, myPrefix+".pdf").getAbsolutePath());
			gp.saveAsTXT(new File(outputDir, myPrefix+".txt").getAbsolutePath());
			
			CSVFile<String> csv = new CSVFile<String>(true);
			csv.addLine("Mag", "Mean", "Std Dev", "SDOM", "p2.5%", "p97.5%", "Median", "Lower 95% of Mean", "Upper 95% of Mean");
			
			// now mean and std dev
			for (int n=0; n<mfd.size(); n++)
				csv.addLine(myMFD.getX(n)+"", myMFD.getY(n)+"", stdDevs[n]+"", sdoms[n]+"", fract025.getY(n)+"",
						fract975.getY(n)+"", median.getY(n)+"", lower95_mean.getY(n)+"", upper95_mean.getY(n)+"");
			
			csv.writeToFile(new File(outputDir,  myPrefix+".csv"));
		}
	}
	
	public static List<List<ETAS_EqkRupture>> loadCatalogsZip(File zipFile) throws ZipException, IOException {
		return loadCatalogsZip(zipFile, -10);
	}
	
	public static List<List<ETAS_EqkRupture>> loadCatalogsZip(File zipFile, double minMag) throws ZipException, IOException {
		ZipFile zip = new ZipFile(zipFile);
		
		List<List<ETAS_EqkRupture>> catalogs = Lists.newArrayList();
		
		for (ZipEntry entry : Collections.list(zip.entries())) {
			if (!entry.isDirectory())
				continue;
//			System.out.println(entry.getName());
			String subEntryName = entry.getName()+"simulatedEvents.txt";
			ZipEntry catEntry = zip.getEntry(subEntryName);
			if (catEntry == null)
				continue;
//			System.out.println("Loading "+catEntry.getName());
			
			try {
				List<ETAS_EqkRupture> cat = ETAS_SimAnalysisTools.loadCatalog(
						zip.getInputStream(catEntry), minMag);
				
				catalogs.add(cat);
			} catch (Exception e) {
//				ExceptionUtils.throwAsRuntimeException(e);
				System.out.println("Skipping catalog "+entry.getName()+": "+e.getMessage());
			}
			if (catalogs.size() % 1000 == 0)
				System.out.println("Loaded "+catalogs.size()+" catalogs (and counting)...");
		}
		
		zip.close();
		
		System.out.println("Loaded "+catalogs.size()+" catalogs");
		
		return catalogs;
	}

	public static void main(String[] args) throws IOException {
		File mainDir = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations");
		double minLoadMag = -1;
		
		String name = "Mojave M5 Full TD";
		File resultsZipFile = new File(mainDir, "2015_08_07-mojave_m5-full_td/results.zip");
		
//		String name = "Mojave M5 Full TD, GR Corr.";
//		File resultsZipFile = new File(mainDir, "2015_08_07-mojave_m5-full_td-grCorr/results.zip");
		
//		String name = "Mojave M6 Full TD";
//		File resultsZipFile = new File(mainDir, "2015_08_07-mojave_m6-full_td/results.zip");
		
//		String name = "Mojave M6 Full TD, GR Corr.";
//		File resultsZipFile = new File(mainDir, "2015_08_07-mojave_m6-full_td-grCorr/results.zip");
		
//		String name = "Mojave M7 Full TD";
//		File resultsZipFile = new File(mainDir, "2015_08_07-mojave_m7-full_td/results.zip");
		
//		String name = "Mojave M7 Full TD, GR Corr.";
//		File resultsZipFile = new File(mainDir, "2015_08_07-mojave_m7-full_td-grCorr/results.zip");
		
//		String name = "Mojave M7 No ERT";
//		File resultsZipFile = new File(mainDir, "2015_08_07-mojave_m7-no_ert/results.zip");
//		minLoadMag = 4; // otherwise uses too much memory, 14GB wasn't enough
		
//		String name = "Mojave M7 No ERT, GR Corr.";
//		File resultsZipFile = new File(mainDir, "2015_08_07-mojave_m7-no_ert-grCorr/results.zip");
		
//		String name = "Mojave M7 Poisson";				// BAD, none completed
//		File resultsZipFile = new File(mainDir, "2015_08_07-mojave_m7-poisson/results.zip");
		
//		String name = "Mojave M7 Poisson, GR Corr.";
//		File resultsZipFile = new File(mainDir, "2015_08_07-mojave_m7-poisson-grCorr/results.zip");
		
		// parent ID for the trigger rupture
		int triggerParentID = 0;
		
		File outputDir = new File(resultsZipFile.getParentFile(), "outputs_stats");
		
		if (!outputDir.exists())
			outputDir.mkdir();
		
		// load the catalogs
		List<List<ETAS_EqkRupture>> catalogs = loadCatalogsZip(resultsZipFile, minLoadMag);
		
		// print out catalogs with most triggered moment
		List<List<ETAS_EqkRupture>> childrenCatalogs = Lists.newArrayList();
		for (List<ETAS_EqkRupture> catalog : catalogs)
			childrenCatalogs.add(ETAS_SimAnalysisTools.getChildrenFromCatalog(catalog, triggerParentID));
		List<Integer> indexes = Lists.newArrayList();
		for (int i=0; i<childrenCatalogs.size(); i++)
			indexes.add(i);
		List<Double> moments = calcTotalMoments(childrenCatalogs);
		List<ComparablePairing<Double, Integer>> pairings = ComparablePairing.build(moments, indexes);
		Collections.sort(pairings);
		Collections.reverse(pairings);
		System.out.println("Index\tMoment\tMax M\t# Trig\t# Supra");
		for (int i=0; i<20; i++) {
			ComparablePairing<Double, Integer> pairing = pairings.get(i);
			int index = pairing.getData();
			List<ETAS_EqkRupture> catalog = childrenCatalogs.get(index);
			double moment = pairing.getComparable();
			double maxMag = 0d;
			int numSupra = 0;
			for (ETAS_EqkRupture rup : catalog) {
				maxMag = Math.max(maxMag, rup.getMag());
				if (rup.getFSSIndex() >= 0)
					numSupra++;
			}
			
			System.out.println(index+"\t"+(float)moment+"\t"+(float)maxMag+"\t"+catalog.size()+"\t"+numSupra);
		}
		
		plotMFD(childrenCatalogs, outputDir, name, "full_children");
		
		List<List<ETAS_EqkRupture>> primaryCatalogs = Lists.newArrayList();
		for (List<ETAS_EqkRupture> catalog : catalogs)
			primaryCatalogs.add(ETAS_SimAnalysisTools.getPrimaryAftershocks(catalog, triggerParentID));
		
		plotMFD(primaryCatalogs, outputDir, "Primary "+name, "primary_aftershocks");
		
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

}
