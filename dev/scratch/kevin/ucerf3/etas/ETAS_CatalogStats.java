package scratch.kevin.ucerf3.etas;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import scratch.UCERF3.erf.ETAS.ETAS_EqkRupture;
import scratch.UCERF3.erf.ETAS.ETAS_SimAnalysisTools;
import scratch.UCERF3.erf.utils.ProbabilityModelsCalc;
import scratch.UCERF3.griddedSeismicity.AbstractGridSourceProvider;

public class ETAS_CatalogStats {
	
	private static int calcNumWithMagAbove(List<List<ETAS_EqkRupture>> catalogs, double targetMinMag) {
		return calcNumWithMagAbove(catalogs, targetMinMag, -1, -1);
	}
	
	private static int calcNumWithMagAbove(List<List<ETAS_EqkRupture>> catalogs, double targetMinMag,
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
	
	private static void plotMFD(List<List<ETAS_EqkRupture>> catalogs, File outputDir, String name) throws IOException {
		ArbIncrementalMagFreqDist mfd = new ArbIncrementalMagFreqDist(5.05, 41, 0.1);
		
		double rate = 1d/catalogs.size();
		
		for (List<ETAS_EqkRupture> catalog : catalogs)
			for (ETAS_EqkRupture rup : catalog)
				mfd.addResampledMagRate(rup.getMag(), rate, true);
		
		EvenlyDiscretizedFunc cmlMFD = mfd.getCumRateDistWithOffset();
		
		List<DiscretizedFunc> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		funcs.add(mfd);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		
		PlotSpec spec = new PlotSpec(funcs, chars, name+" MFD", "Magnitude", "Incremental Rate (1/yr)");
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		gp.setUserBounds(cmlMFD.getMinX(), cmlMFD.getMaxX(), 1e-4, 1e1);
		
		gp.setBackgroundColor(Color.WHITE);
		gp.setTickLabelFontSize(18);
		gp.setAxisLabelFontSize(20);
		gp.setPlotLabelFontSize(21);
		
		gp.drawGraphPanel(spec, false, true);
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPNG(new File(outputDir, "mfd_incremental.png").getAbsolutePath());
		gp.saveAsPDF(new File(outputDir, "mfd_incremental.pdf").getAbsolutePath());
		gp.saveAsTXT(new File(outputDir, "mfd_incremental.txt").getAbsolutePath());
		
		// now cumulative
		funcs = Lists.newArrayList();
		funcs.add(cmlMFD);
		
		spec = new PlotSpec(funcs, chars, name+" MFD", "Magnitude", "Cumulative Rate (1/yr)");
		
		gp.drawGraphPanel(spec, false, true);
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPNG(new File(outputDir, "mfd_cumulative.png").getAbsolutePath());
		gp.saveAsPDF(new File(outputDir, "mfd_cumulative.pdf").getAbsolutePath());
		gp.saveAsTXT(new File(outputDir, "mfd_cumulative.txt").getAbsolutePath());
	}

	public static void main(String[] args) throws IOException {
////		File etasCatalogDir = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/2014_05_28-mojave_7/results");
//		File etasCatalogDir = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/2014_06_23-mojave_7-indep/results");
//		int triggerParentID = 0;
//		double targetMinMag = 7.050480408896166;
//		String name = "Mojave 7.05";
//		File etasCatalogDir = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/2014_05_28-la_habra/results");
		File etasCatalogDir = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/2014_08_25-napa/results.zip");
		int triggerParentID = 0;
		double targetMinMag = 6.0;
		String name = "Napa M6";
		File[] etasCatalogDirs = {etasCatalogDir};
		File outputDir = new File(etasCatalogDir.getParentFile(), "outputs_stats");
		
		int maxDaysAfter = 7;
		
//		File etasCatalogDir = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/2014_05_28-spontaneous/");
//		int triggerParentID = -1;
//		String name = "Spontaneous";
//		File[] etasCatalogDirs = { new File(etasCatalogDir, "results_1"), new File(etasCatalogDir, "results_2"),
//				new File(etasCatalogDir, "results_3"), new File(etasCatalogDir, "results_4"),
//				new File(etasCatalogDir, "results_5")};
////		double targetMinMag = 6.2;
//		double targetMinMag = 7.050480408896166;
//		File outputDir = new File(etasCatalogDir, "outputs_stats");
		
		if (!outputDir.exists())
			outputDir.mkdir();
		
//		List<List<ETAS_EqkRupture>> catalogs = ETAS_CatalogEALCalculator.loadCatalogs(
//				etasCatalogDirs, AbstractGridSourceProvider.SOURCE_MIN_MAG_CUTOFF-0.05);
		
		ZipFile zip = new ZipFile(etasCatalogDir);
		
		List<List<ETAS_EqkRupture>> catalogs = Lists.newArrayList();
		
		for (ZipEntry entry : Lists.newArrayList(Iterators.forEnumeration(zip.entries()))) {
			if (!entry.isDirectory())
				continue;
//			System.out.println(entry.getName());
			String subEntryName = entry.getName()+"simulatedEvents.txt";
			ZipEntry catEntry = zip.getEntry(subEntryName);
			if (catEntry == null)
				continue;
//			System.out.println("Loading "+catEntry.getName());
			
			try {
				List<ETAS_EqkRupture> cat = ETAS_SimAnalysisTools.loadCatalog(zip.getInputStream(catEntry));
				
				catalogs.add(cat);
			} catch (Exception e) {
//				ExceptionUtils.throwAsRuntimeException(e);
				System.out.println("Skipping catalog "+entry.getName()+": "+e.getMessage());
			}
		}
		
		calcNumWithMagAbove(catalogs, targetMinMag, triggerParentID, 1);
		calcNumWithMagAbove(catalogs, targetMinMag, triggerParentID, maxDaysAfter);
		calcNumWithMagAbove(catalogs, targetMinMag, triggerParentID, 365);
		plotMFD(catalogs, outputDir, name);
	}

}
