/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.opensha.calc.MomentMagCalc;
import org.opensha.data.Location;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.FaultRuptureSource;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_TypeB_EqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2;
import org.opensha.sha.fault.FaultTrace;
import org.opensha.sha.magdist.GaussianMagFreqDist;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
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
	
	
	private ArrayList<FaultRuptureSource> charSources = new ArrayList<FaultRuptureSource>();
	private ArrayList<Frankel02_TypeB_EqkSource> grSources = new ArrayList<Frankel02_TypeB_EqkSource>();
	private ArrayList<FaultRuptureSource> lessThan6_5_Sources = new ArrayList<FaultRuptureSource>();

	
	/**
	 * Read files created after reading non-CA faults from NSHMP files
	 * 
	 * @return
	 */
	public ArrayList<ProbEqkSource> getSources(double duration, double charMagSigma, 
			double charMagTruncLevel, double rupOffset) {
		
		GaussianMagFreqDist charMFD = null;
		GutenbergRichterMagFreqDist grMFD = null;
		
		try {
			ArrayList<String> fileLines = FileUtils.loadJarFile(CHAR_FILENAME);
			int numLines = fileLines.size();
			int rakeId, srcTypeId;
			double mag=0, dip, downDipWidth, upperSeisDepth, lowerSeisDepth, latitude, longitude, rake;
			FaultTrace faultTrace;
			String faultName;
			for(int i=0; i<numLines; ) {
				String line = fileLines.get(i++);
				StringTokenizer tokenizer = new StringTokenizer(line);
				srcTypeId = Integer.parseInt(tokenizer.nextToken().trim());
				rakeId = Integer.parseInt(tokenizer.nextToken().trim());
				if(rakeId==1) rake = 0;
				else if(rakeId==2) rake =90;
				else rake = -90;
				int numMags = Integer.parseInt(tokenizer.nextToken().trim());
				if(numMags != 1) throw new RuntimeException("Only one mag supported");
				tokenizer.nextToken();  // we were told this element is useless
				faultName = "";
				while(tokenizer.hasMoreTokens()) faultName+=tokenizer.nextToken()+" ";
				// mag, rate & wt
				line = fileLines.get(i++);
				tokenizer = new StringTokenizer(line);
				if(srcTypeId==1) { // Char case
					mag = Double.parseDouble(tokenizer.nextToken().trim());
					double rate = Double.parseDouble(tokenizer.nextToken().trim());
					double moRate = rate*MomentMagCalc.getMoment(mag);
					double wt = Double.parseDouble(tokenizer.nextToken().trim());
					double wt2 = 1;
					if(mag > 6.5) wt2 = 0.666;
					moRate *= wt*wt2;
					charMFD = new GaussianMagFreqDist(UCERF2.MIN_MAG, UCERF2.MAX_MAG, UCERF2.NUM_MAG, 
							mag, charMagSigma, moRate, charMagTruncLevel, 2);
				}
				else if (srcTypeId==2) {
					double aVal=Double.parseDouble(tokenizer.nextToken().trim());
					double bVal=Double.parseDouble(tokenizer.nextToken().trim());
					double magLower=Double.parseDouble(tokenizer.nextToken().trim());
					double magUpper=Double.parseDouble(tokenizer.nextToken().trim());
					double deltaMag=Double.parseDouble(tokenizer.nextToken());
		            magLower += deltaMag/2.0;
		            magUpper -= deltaMag/2.0;
		            numMags = Math.round( (float)((magUpper-magLower)/deltaMag + 1.0) );
					double moRate = Frankel02_AdjustableEqkRupForecast.getMomentRate(magLower, numMags, deltaMag, aVal, bVal);
					double wt = Double.parseDouble(tokenizer.nextToken().trim());
					double wt2 = 0.334;
					moRate *= wt*wt2;
					grMFD = new GutenbergRichterMagFreqDist(magLower,numMags,deltaMag,moRate,bVal);					
				}
				else throw new RuntimeException("Src type not supported");

				// dip, surface width, upper seis depth, surface length
				line = fileLines.get(i++);
				tokenizer = new StringTokenizer(line);
				dip = Double.parseDouble(tokenizer.nextToken().trim());
				downDipWidth = Double.parseDouble(tokenizer.nextToken().trim());
				upperSeisDepth = Double.parseDouble(tokenizer.nextToken().trim());
				lowerSeisDepth = upperSeisDepth + downDipWidth*Math.sin((Math.toRadians(Math.abs(dip))));
				
				//fault trace
				line = fileLines.get(i++);
				int numLocations = Integer.parseInt(line.trim());
				faultTrace = new FaultTrace(faultName);
				for(int locIndex=0; locIndex<numLocations; ++locIndex) {
					line = fileLines.get(i++);
					tokenizer = new StringTokenizer(line);
					latitude = Double.parseDouble(tokenizer.nextToken());
					longitude =Double.parseDouble(tokenizer.nextToken());
					faultTrace.addLocation(new Location(latitude, longitude));
				}
				// surface
				StirlingGriddedSurface surface = new StirlingGriddedSurface(faultTrace, dip, upperSeisDepth,
						lowerSeisDepth, UCERF2.GRID_SPACING);   
				
				if(srcTypeId == 1) {
					FaultRuptureSource frs = new FaultRuptureSource(charMFD,surface, rake,duration);
					frs.setName(faultName+" Char");
					if(mag > 6.5) charSources.add(frs);
					else lessThan6_5_Sources.add(frs);
				}
				else {
					Frankel02_TypeB_EqkSource fgrs = new Frankel02_TypeB_EqkSource(grMFD,surface,
                            rupOffset, rake, duration, faultName+" GR");
					grSources.add(fgrs);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		// combine the sources
		ArrayList<ProbEqkSource> allSources = new ArrayList<ProbEqkSource>();
		allSources.addAll(charSources);
		allSources.addAll(lessThan6_5_Sources);
		allSources.addAll(grSources);
		return allSources;
	}
	
	


	
	
	public static void main(String args[]) {
		NonCA_FaultsFetcher nonCA_FaultsFetcher = new NonCA_FaultsFetcher();
	}
}
