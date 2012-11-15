package scratch.kevin.ucerf3.inversion;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.util.ClassUtils;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

import scratch.UCERF3.inversion.CommandLineInversionRunner;
import scratch.UCERF3.utils.MatrixIO;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CompoundSolAboveWaterlevelCalc {
	
	private static Map<String, boolean[]> getAbovesForDir(File dir) throws IOException {
		Map<String, boolean[]> abovesForPrefix = Maps.newHashMap();
		
		getAbovesForDir(dir, abovesForPrefix);
		
		System.out.println("Loaded "+abovesForPrefix.size()+" branches!");
		
		return abovesForPrefix;
	}
	
	private static void getAbovesForDir(File dir, Map<String, boolean[]> abovesForPrefix) throws IOException {
		for (File file : dir.listFiles()) {
			if (file.isDirectory())
				getAbovesForDir(dir, abovesForPrefix);
			
			String name = file.getName();
			if (!name.endsWith("_noMinRates.bin"))
				continue;
			
			String prefix = name.substring(0, name.indexOf("_noMinRates.bin"));
			if (prefix.contains("_run"))
				prefix = name.substring(0, name.indexOf("_run"));
			
			double[] rates = MatrixIO.doubleArrayFromFile(file);
			
			boolean[] aboves = abovesForPrefix.get(prefix);
			
			if (aboves == null) {
				aboves = new boolean[rates.length];
				abovesForPrefix.put(prefix, aboves);
			}
			
			for (int i=0; i<rates.length; i++)
				if (rates[i] > 0)
					aboves[i] = true;
		}
	}
	
	private static Map<String, boolean[]> getAbovesForZip(File zipFile) throws IOException {
		return getAbovesForZip(zipFile, null);
	}
	
	private static Map<String, boolean[]> getAbovesForZip(File zipFile, String grep) throws IOException {
		ZipFile zip = new ZipFile(zipFile);
		Map<String, boolean[]> abovesForPrefix = Maps.newHashMap();
		
		getAbovesForZip(zip, abovesForPrefix, grep);
		
		System.out.println("Loaded "+abovesForPrefix.size()+" branches!");
		
		return abovesForPrefix;
	}
	
	private static void getAbovesForZip(ZipFile zip, Map<String, boolean[]> abovesForPrefix, String grep) throws IOException {
		Enumeration<? extends ZipEntry> entries = zip.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			
			String name = entry.getName();
			if (!name.endsWith("_noMinRates.bin"))
				continue;
			
			if (grep != null && !grep.isEmpty() && !name.contains(grep))
				continue;
			
			String prefix = name.substring(0, name.indexOf("_noMinRates.bin"));
			if (prefix.contains("_run"))
				prefix = name.substring(0, name.indexOf("_run"));
			
			double[] rates = MatrixIO.doubleArrayFromInputStream(zip.getInputStream(entry), entry.getSize());
			
			boolean[] aboves = abovesForPrefix.get(prefix);
			
			if (aboves == null) {
				aboves = new boolean[rates.length];
				abovesForPrefix.put(prefix, aboves);
			}
			
			for (int i=0; i<rates.length; i++)
				if (rates[i] > 0)
					aboves[i] = true;
		}
	}
	
	private static int[] getAboveWaterlevelCounts(Map<String, boolean[]> abovesForPrefix) throws IOException {
		int[] aboves = null;
		for (boolean[] prefixAboves : abovesForPrefix.values()) {
			if (aboves == null)
				aboves = new int[prefixAboves.length];
			else
				Preconditions.checkState(aboves.length == prefixAboves.length);
			
			for (int i=0; i<aboves.length; i++)
				if (prefixAboves[i])
					aboves[i] = aboves[i] + 1;
		}
		
		return aboves;
	}
	
	private static void plotAboves(int[] aboves, File dir) throws IOException {
		int max = 0;
		for (int above : aboves)
			if (above > max)
				max = above;
		
		HistogramFunction hist = new HistogramFunction(0d, max+1, 1d);
		
		for (int above : aboves)
			hist.add(above, 1d);
		
		EvenlyDiscretizedFunc cml = getCmlGreaterOrEqual(hist);
		
		ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
		funcs.add(hist);
//		funcs.add(cml);
		
		ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
		chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 5f, Color.BLACK));
//		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 3f, Color.BLUE));
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		CommandLineInversionRunner.setFontSizes(gp);
		
		String title = "Rups Above Waterlevel";
		
		String xAxisName = "# Solutions With Rup Above Waterlevel";
		String yAxisName = "# Ruptures";
		
		gp.drawGraphPanel(xAxisName, yAxisName, funcs, chars, false, title);
		
		File file = new File(dir, "rups_above_waterlevel");
		
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPDF(file.getAbsolutePath()+".pdf");
		gp.saveAsPNG(file.getAbsolutePath()+".png");
		gp.saveAsTXT(file.getAbsolutePath()+".txt");
	}
	
	private static EvenlyDiscretizedFunc getCmlGreaterOrEqual(EvenlyDiscretizedFunc func) {
		EvenlyDiscretizedFunc cml = new EvenlyDiscretizedFunc(func.getMinX(), func.getNum(), func.getDelta());
		
		double tot = 0d;
		for (int i=func.getNum(); --i>=0;) {
			tot += func.getY(i);
			cml.set(i, tot);
		}
		
		return cml;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.out.println("USAGE: "+ClassUtils.getClassNameWithoutPackage(CompoundSolAboveWaterlevelCalc.class)+" <dir>");
			System.exit(2);
		}
		File dir = new File(args[0]);
		Map<String, boolean[]> abovesMap;
		if (dir.getName().endsWith(".zip")) {
			File zipFile = dir;
			dir = zipFile.getParentFile();
			Preconditions.checkArgument(zipFile.exists(), "Zip file "+zipFile.getAbsolutePath()+" doesn't exist!");
			abovesMap = getAbovesForZip(zipFile, null);
		} else {
			Preconditions.checkArgument(dir.exists(), "Dir "+dir.getAbsolutePath()+" doesn't exist!");
			Preconditions.checkArgument(dir.isDirectory(), dir.getAbsolutePath()+" isn't a directory!");
			abovesMap = getAbovesForDir(dir);
		}
		int[] aboves = getAboveWaterlevelCounts(abovesMap);
		
		plotAboves(aboves, dir);
	}

}
