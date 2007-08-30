/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.opensha.data.Location;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.FaultRuptureSource;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_TypeB_EqkSource;
import org.opensha.sha.fault.FaultTrace;
import org.opensha.sha.surface.StirlingGriddedSurface;
import org.opensha.util.FileUtils;

/**
 * It reads the Non-CA faults file to generate a list of non-CA faults.
 *
 * 
 * 
 * @author vipingupta
 *
 */
public class NonCA_FaultsFetcher {
	private final static String CHAR_FILENAME = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_3/data/NearCA_NSHMP/NonCA_FaultsChar.txt";
	private final static String GR_FILENAME = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_3/data/NearCA_NSHMP/NonCA_FaultsGR.txt";
	private final static double GRID_SPACING = 0.1;
	
	public NonCA_FaultsFetcher() {
		
	}
	
	/**
	 * Read files created after reading non-CA faults from NSHMP files
	 * 
	 * @return
	 */
	public ArrayList<FaultRuptureSource> getCharSources(double duration) {
		ArrayList<FaultRuptureSource> charSources = new ArrayList<FaultRuptureSource>();
		try {
			ArrayList<String> charFileLines = FileUtils.loadJarFile(CHAR_FILENAME);
			int numLines = charFileLines.size();
			int rakeId;
			double mag, rate, wt, dip, downDipWidth, upperSeisDepth, lowerSeisDepth, latitude, longitude, rake, prob;
			FaultTrace faultTrace;
			String faultName;
			for(int i=0; i<numLines; ) {
				String line = charFileLines.get(i++);
				StringTokenizer tokenizer = new StringTokenizer(line);
				rakeId = Integer.parseInt(tokenizer.nextToken().trim());
				if(rakeId==1) rake = 0;
				else if(rakeId==2) rake =90;
				else rake = -90;
				tokenizer.nextToken();
				tokenizer.nextToken();
				tokenizer.nextToken();
				tokenizer.nextToken();
				faultName = "";
				while(tokenizer.hasMoreTokens()) faultName+=tokenizer.nextToken()+" ";
				// mag, rate & wt
				line = charFileLines.get(i++);
				tokenizer = new StringTokenizer(line);
				mag = Double.parseDouble(tokenizer.nextToken().trim());
				rate = Double.parseDouble(tokenizer.nextToken().trim());
				prob = 1 - Math.exp(-rate*duration);
				wt = Double.parseDouble(tokenizer.nextToken().trim());

				// dip, surface width, upper seis depth, surface length
				line = charFileLines.get(i++);
				tokenizer = new StringTokenizer(line);
				dip = Double.parseDouble(tokenizer.nextToken().trim());
				downDipWidth = Double.parseDouble(tokenizer.nextToken().trim());
				upperSeisDepth = Double.parseDouble(tokenizer.nextToken().trim());
				lowerSeisDepth = upperSeisDepth + downDipWidth*Math.sin((Math.toRadians(Math.abs(dip))));
				
				//fault trace
				line = charFileLines.get(i++);
				int numLocations = Integer.parseInt(line.trim());
				faultTrace = new FaultTrace(faultName);
				for(int locIndex=0; locIndex<numLocations; ++locIndex) {
					line = charFileLines.get(i++);
					tokenizer = new StringTokenizer(line);
					latitude = Double.parseDouble(tokenizer.nextToken());
					longitude =Double.parseDouble(tokenizer.nextToken());
					faultTrace.addLocation(new Location(latitude, longitude));
				}
				// surface
				StirlingGriddedSurface surface = new StirlingGriddedSurface(faultTrace, dip, upperSeisDepth,
						lowerSeisDepth, GRID_SPACING);   
				FaultRuptureSource frs = new FaultRuptureSource(mag,surface,rake, prob);
				charSources.add(frs);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return charSources;
	}
	
	

	/**
	 * Read files created after reading non-CA faults from NSHMP files
	 * 
	 * @return
	 */
	public ArrayList<Frankel02_TypeB_EqkSource> getGRSources(double duration) {
		ArrayList<Frankel02_TypeB_EqkSource> grSources = new ArrayList<Frankel02_TypeB_EqkSource>();
		try {
			ArrayList<String> charFileLines = FileUtils.loadJarFile(GR_FILENAME);
			int numLines = charFileLines.size();
			int rakeId;
			double mag, aVal, wt, dip, downDipWidth, upperSeisDepth, lowerSeisDepth, latitude, longitude, rake, bVal, magLower, deltaMag;
			FaultTrace faultTrace;
			String faultName;
			for(int i=0; i<numLines; ) {
				String line = charFileLines.get(i++);
				StringTokenizer tokenizer = new StringTokenizer(line);
				rakeId = Integer.parseInt(tokenizer.nextToken().trim());
				if(rakeId==1) rake = 0;
				else if(rakeId==2) rake =90;
				else rake = -90;
				tokenizer.nextToken();
				tokenizer.nextToken();
				tokenizer.nextToken();
				tokenizer.nextToken();
				faultName = "";
				while(tokenizer.hasMoreTokens()) faultName+=tokenizer.nextToken()+" ";
				// mag, rate & wt
				line = charFileLines.get(i++);
				tokenizer = new StringTokenizer(line);
				
				// read the GR parameters
				aVal=Double.parseDouble(tokenizer.nextToken().trim());
				bVal=Double.parseDouble(tokenizer.nextToken().trim());
				magLower=Double.parseDouble(tokenizer.nextToken().trim());
				mag=Double.parseDouble(tokenizer.nextToken().trim());
				deltaMag=Double.parseDouble(tokenizer.nextToken());
				wt = Double.parseDouble(tokenizer.nextToken().trim());

				// dip, surface width, upper seis depth, surface length
				line = charFileLines.get(i++);
				tokenizer = new StringTokenizer(line);
				dip = Double.parseDouble(tokenizer.nextToken().trim());
				downDipWidth = Double.parseDouble(tokenizer.nextToken().trim());
				upperSeisDepth = Double.parseDouble(tokenizer.nextToken().trim());
				lowerSeisDepth = upperSeisDepth + downDipWidth*Math.sin((Math.toRadians(Math.abs(dip))));
				
				//fault trace
				line = charFileLines.get(i++);
				int numLocations = Integer.parseInt(line.trim());
				faultTrace = new FaultTrace(faultName);
				for(int locIndex=0; locIndex<numLocations; ++locIndex) {
					line = charFileLines.get(i++);
					tokenizer = new StringTokenizer(line);
					latitude = Double.parseDouble(tokenizer.nextToken());
					longitude =Double.parseDouble(tokenizer.nextToken());
					faultTrace.addLocation(new Location(latitude, longitude));
				}
				
				// surface
				StirlingGriddedSurface surface = new StirlingGriddedSurface(faultTrace, dip, upperSeisDepth,
						lowerSeisDepth, GRID_SPACING);   
				
				
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return grSources;
	}
	
	
	public static void main(String args[]) {
		NonCA_FaultsFetcher nonCA_FaultsFetcher = new NonCA_FaultsFetcher();
		nonCA_FaultsFetcher.getCharSources(1.0);
		nonCA_FaultsFetcher.getGRSources(1.0);
	}
	
}
