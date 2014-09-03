package scratch.kevin.cybershake.etasCalcs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.opensha.commons.util.FileUtils;

import scratch.UCERF3.erf.ETAS.ETAS_EqkRupture;
import scratch.UCERF3.erf.ETAS.ETAS_SimAnalysisTools;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;

public class ETASResultCombiner {

	public static void main(String[] args) throws IOException {
		// used to combine multiple etas result zip files into a single zip file
		
		File[] zipFiles = {
				// bombay
//				new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims/2014_07_31-bombay_beach_m6/results.zip"),
//				new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims/2014_08_07-bombay_beach_m6-nospont/results.zip")
				
				// parkfield
				new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims/2014_08_01-parkfield/results.zip"),
				new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims/2014_08_07-parkfield-nospont/results.zip")
		};
		
//		File outputFile = new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims/2014_08_07-bombay_beach_m6_combined.zip");
		File outputFile = new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims/2014_08_07-parkfield-nospont_combined.zip");
		File workDir = Files.createTempDir();
		
		List<List<ETAS_EqkRupture>> catalogs = Lists.newArrayList();
		
		List<String> infoStrings = Lists.newArrayList();
		
		for (File zipFile : zipFiles) {
			ZipFile zip = new ZipFile(zipFile);
			
			for (ZipEntry entry : Lists.newArrayList(Iterators.forEnumeration(zip.entries()))) {
				if (!entry.isDirectory())
					continue;
//				if (Math.random() > 0.01)
//					continue;
//				System.out.println(entry.getName());
				String subEntryName = entry.getName()+"simulatedEvents.txt";
				ZipEntry catEntry = zip.getEntry(subEntryName);
				String infoEntryName = entry.getName()+"infoString.txt";
				ZipEntry infoEntry = zip.getEntry(infoEntryName);
				if (catEntry == null || infoEntry == null)
					continue;
				
				// make sure it's actually done
				BufferedReader reader = new BufferedReader(new InputStreamReader(zip.getInputStream(infoEntry)));
				
				StringBuilder infoString = new StringBuilder();
				boolean done = false;
				for (String line : CharStreams.readLines(reader)) {
					infoString.append(line);
					infoString.append("\n");
					if (line.contains("Total num ruptures: ")) {
						done = true;
						break;
					}
				}
				if (!done)
					continue;
//				System.out.println("Loading "+catEntry.getName());
				
				List<ETAS_EqkRupture> catalog;
				try {
					catalog = ETAS_SimAnalysisTools.loadCatalog(zip.getInputStream(catEntry), 5d);
				} catch (Exception e) {
					continue;
				}
				// now remove all spontaneous
				catalog = ETAS_SimAnalysisTools.getChildrenFromCatalog(catalog, 0);
				
				catalogs.add(catalog);
				infoStrings.add(infoString.toString());
			}
		}
		
		System.out.println("Loaded "+catalogs.size()+" catalogs");
		
		// now write new ones
		int numDigits = (""+(catalogs.size()-1)).length();
		
		List<String> zipNames = Lists.newArrayList();
		
		for (int i=0; i<catalogs.size(); i++) {
			String name = i+"";
			while (name.length() < numDigits)
				name = "0"+name;
			name = "sim_"+name;
			
			File resultsDir = new File(workDir, name);
			resultsDir.mkdir();
			File catFile = new File(resultsDir, "simulatedEvents.txt");
			File infoFile = new File(resultsDir, "infoString.txt");
			ETAS_SimAnalysisTools.writeEventDataToFile(catFile.getAbsolutePath(), catalogs.get(i));
			Files.write(infoStrings.get(i), infoFile, Charset.defaultCharset());
			zipNames.add("/"+name+"/");
			zipNames.add("/"+name+"/simulatedEvents.txt");
			zipNames.add("/"+name+"/infoString.txt");
			resultsDir.mkdir();
		}
		
		FileUtils.createZipFile(outputFile.getAbsolutePath(), workDir.getAbsolutePath()+"/", zipNames);
		
		FileUtils.deleteRecursive(workDir);
	}

}
