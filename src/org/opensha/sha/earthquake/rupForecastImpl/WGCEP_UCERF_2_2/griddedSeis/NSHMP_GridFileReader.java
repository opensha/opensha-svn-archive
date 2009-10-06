/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.griddedSeis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.region.EvenlyGriddedRELM_Region;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.gui.GraphWindowAPI_Impl;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.UCERF2;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;



/**
 * Read NSHMP backgroud seismicity files
 * 
 * @author vipingupta
 *
 */
public class NSHMP_GridFileReader {

	private final static String LAT_LON_FILENAME = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_2/griddedSeis/LonsLats.txt";
	String Path = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_2/griddedSeis/";

	private final double DELTA_MAG = 0.1;
	private final double MIN_MAG = 0;
	private SummedMagFreqDist cZonesSummedMFD; // summed MFD for C Zones
	private SummedMagFreqDist bckSummedMFD; // Summed MFD for Background (C-Zones not included)
	private ArrayList cZonesMFDList, bckMFDList;
	
	
	
	public NSHMP_GridFileReader() {
		this.makeBckMFDs(); // make MFDs for background seismicity
		this.makeC_ZoneMFDs(); // make MFDs for C-Zones
	}
	
	/**
	 * Get the MFDs for C-Zones
	 * @return
	 */
	public ArrayList getC_ZonesMFDs() {
		return this.cZonesMFDList;
	}
	
	/**
	 * Return the Summed MFD for C Zones. It is not a cumulative MFD
	 * @return
	 */
	public SummedMagFreqDist getC_ZonesSummedMFD() {
		return this.cZonesSummedMFD;
	}
	
	/**
	 * Get the MFDs for Background Seismicity
	 * @return
	 */
	public ArrayList getBackgroundMFDs() {
		return this.bckMFDList;
	}
	
	/**
	 * Return the Summed MFD for Background Seismicity
	 * @return
	 */
	public SummedMagFreqDist getBackgroundSummedMFD() {
		return this.bckSummedMFD;
	}
	
	
	/**
	 * Read C-Zone files to C-Zone MFDs
	 *
	 */
	private void makeC_ZoneMFDs() {
		cZonesMFDList = new ArrayList();
		cZonesSummedMFD = new SummedMagFreqDist(UCERF2.MIN_MAG, UCERF2.MAX_MAG, UCERF2.NUM_MAG);
		
		GutenbergRichterMagFreqDist mfd  = getMFD_InsideRELM_Region(Path+"area1new.agrid.asc", 0.8, 6.5, 7.6, 1);
		cZonesMFDList.add(mfd);
		cZonesSummedMFD.addResampledMagFreqDist(mfd, true);
		
		mfd  = getMFD_InsideRELM_Region(Path+"area2new.agrid.asc", 0.8, 6.5, 7.6, 1);
		cZonesMFDList.add(mfd);
		cZonesSummedMFD.addResampledMagFreqDist(mfd, true);
		
		mfd  = getMFD_InsideRELM_Region(Path+"area3new.agrid.asc", 0.8, 6.5, 7.6, 1);
		cZonesMFDList.add(mfd);
		cZonesSummedMFD.addResampledMagFreqDist(mfd, true);
		
		mfd  = getMFD_InsideRELM_Region(Path+"area4new.agrid.asc", 0.8, 6.5, 7.6, 1);
		cZonesMFDList.add(mfd);
		cZonesSummedMFD.addResampledMagFreqDist(mfd, true);
		
		mfd  = getMFD_InsideRELM_Region(Path+"mojave.agrid.asc", 0.8, 6.5, 7.6, 1);
		cZonesMFDList.add(mfd);
		cZonesSummedMFD.addResampledMagFreqDist(mfd, true);
		
		mfd  = getMFD_InsideRELM_Region(Path+"sangreg.agrid.asc", 0.8, 6.5, 7.6, 1);
		cZonesMFDList.add(mfd);
		cZonesSummedMFD.addResampledMagFreqDist(mfd, true);
	}
	
	/**
	 * Make MFDs for Background seismicity. It does not include C-Zones
	 *
	 */
	private void makeBckMFDs() {
		bckMFDList = new ArrayList();
		bckSummedMFD = new SummedMagFreqDist(UCERF2.MIN_MAG, UCERF2.MAX_MAG, UCERF2.NUM_MAG);

	
		String name = "agrd_brawly.out.asc";
		GutenbergRichterMagFreqDist mfd  = getMFD_InsideRELM_Region(Path+name, 0.8, 5, 6.5, 1);
		mfd.setName(name);
		bckMFDList.add(mfd);
		bckSummedMFD.addResampledMagFreqDist(mfd, true);

		name = "agrd_mendos.out.asc";
		mfd  = getMFD_InsideRELM_Region(Path+name, 0.8, 5, 7, 1);
		mfd.setName(name);
		bckMFDList.add(mfd);
		bckSummedMFD.addResampledMagFreqDist(mfd, true);


		name = "agrd_creeps.out.asc";
		mfd  = getMFD_InsideRELM_Region(Path+name, 0.9, 5, 6, 1);
		mfd.setName(name);
		bckMFDList.add(mfd);
		bckSummedMFD.addResampledMagFreqDist(mfd, true);
		
		name = "agrd_cstcal.out.asc";
		SummedMagFreqDist summedMFD = new SummedMagFreqDist(UCERF2.MIN_MAG, UCERF2.MAX_MAG, UCERF2.NUM_MAG);
		summedMFD.addResampledMagFreqDist(getMFD_InsideRELM_Region(Path+name, 0.8, 5, Path+"fltmmaxCA2ch.out7.asc", 0.667, true), true);
		summedMFD.addResampledMagFreqDist(getMFD_InsideRELM_Region(Path+name, 0.8, 5, Path+"fltmmaxCA2gr.out7.asc", 0.333, true), true);
		summedMFD.setName(name);
		bckMFDList.add(summedMFD);
		bckSummedMFD.addResampledMagFreqDist(summedMFD, true);
		

		name = "agrd_deeps.out.asc";
		mfd  = getMFD_InsideRELM_Region(Path+name, 0.8, 5, 7.2, 1);
		mfd.setName(name);
		bckMFDList.add(mfd);
		bckSummedMFD.addResampledMagFreqDist(mfd, true);
		
		name = "agrd_wuscmp.out.asc";
		summedMFD = new SummedMagFreqDist(UCERF2.MIN_MAG, UCERF2.MAX_MAG, UCERF2.NUM_MAG);
		summedMFD.addResampledMagFreqDist(getMFD_InsideRELM_Region(Path+name, 0.8, 5, Path+"fltmmaxALLCNch.outv3.asc", 0.667, false), true);
		summedMFD.addResampledMagFreqDist(getMFD_InsideRELM_Region(Path+name, 0.8, 5, Path+"fltmmaxALLCNgr.outv3.asc", 0.333, false), true);
		summedMFD.setName(name);
		bckMFDList.add(summedMFD);
		bckSummedMFD.addResampledMagFreqDist(summedMFD, true);
		
		name = "agrd_wusext.out.asc";
		summedMFD = new SummedMagFreqDist(UCERF2.MIN_MAG, UCERF2.MAX_MAG, UCERF2.NUM_MAG);
		summedMFD.addResampledMagFreqDist(getMFD_InsideRELM_Region(Path+name, 0.8, 5, Path+"fltmmaxALLCNch.outv3.asc", 0.667, false), true);
		summedMFD.addResampledMagFreqDist(getMFD_InsideRELM_Region(Path+name, 0.8, 5, Path+"fltmmaxALLCNgr.outv3.asc", 0.333, false), true);
		summedMFD.setName(name);
		bckMFDList.add(summedMFD);
		bckSummedMFD.addResampledMagFreqDist(summedMFD, true);
	}

	/**
	 * Get Summed MFD inside RELM region
	 * 
	 * @param fileName
	 * @param bValue
	 * @param mMin
	 * @param mMax
	 * @param weight
	 * @return
	 */
	private GutenbergRichterMagFreqDist getMFD_InsideRELM_Region(String fileName, double bValue, double mMin, double mMax, double weight) {
		// RELM region 
		CaliforniaRegions.RELM_GRIDDED region = new CaliforniaRegions.RELM_GRIDDED();
		try { 
			// Region filename
			FileReader regionFileReader = new FileReader(LAT_LON_FILENAME); 
			BufferedReader regionFileBufferedReader = new BufferedReader(regionFileReader);
			String latlonLine = regionFileBufferedReader.readLine(); // skip header line
			latlonLine = regionFileBufferedReader.readLine();

			//A-Values filename
			FileReader ratesFileReader = new FileReader(fileName); 
			BufferedReader ratesFileBufferedReader = new BufferedReader(ratesFileReader);
			String ratesLine = ratesFileBufferedReader.readLine();
			ratesLine = ratesFileBufferedReader.readLine();

			double aVal, lat, lon;
			double totalA_Values = 0;
			while(ratesLine!=null) { // iterate over all locations
				aVal = Double.parseDouble(ratesLine);

				if(aVal!=0) { // check whether location is within RELM region
					StringTokenizer tokenizer = new StringTokenizer(latlonLine);
					lon = Double.parseDouble(tokenizer.nextToken());
					lat = Double.parseDouble(tokenizer.nextToken());
					if(region.isLocationInside(new Location(lat, lon))) totalA_Values+=aVal;
				}

				latlonLine = regionFileBufferedReader.readLine();
				ratesLine = ratesFileBufferedReader.readLine();
			}
			regionFileBufferedReader.close();
			regionFileReader.close();
			ratesFileBufferedReader.close();
			ratesFileReader.close();
			
			// create GutenbergRichterMagFreqDist
			GutenbergRichterMagFreqDist mfd = new GutenbergRichterMagFreqDist(MIN_MAG, (int)Math.round((mMax-MIN_MAG)/DELTA_MAG)+1, DELTA_MAG, 1.0, bValue);
			mfd.scaleToIncrRate(0.0, totalA_Values*weight);

			// set all rates below and including mMin to 0
			int num = mfd.getNum();
			double mag;
			for(int i=0; i<num; ++i) {
				mag = mfd.getX(i);
				if(mag<mMin) mfd.set(i, 0.0);
				else break;
			}
			
			return mfd;

		}catch(Exception e) {
			e.printStackTrace();
		}

		return null;

	}
	
	
	/**
	 * Get Summed MFD inside RELM region
	 * 
	 * @param fileName
	 * @param bValue
	 * @param mMin
	 * @param mMaxFilename File that specifies the Max mag 
	 * @param weight
	 * @return
	 */
	private SummedMagFreqDist getMFD_InsideRELM_Region(String fileName, double 
			bValue, double mMin, String mMaxFilename, double weight, 
			boolean applyBulgeReduction) {
		// RELM region 
		CaliforniaRegions.RELM_GRIDDED region = new CaliforniaRegions.RELM_GRIDDED();
		SummedMagFreqDist summedMFD = new SummedMagFreqDist(UCERF2.MIN_MAG, UCERF2.MAX_MAG, UCERF2.NUM_MAG);
		try { 
			// Region filename
			FileReader regionFileReader = new FileReader(LAT_LON_FILENAME); 
			BufferedReader regionFileBufferedReader = new BufferedReader(regionFileReader);
			String latlonLine = regionFileBufferedReader.readLine(); // skip header line
			latlonLine = regionFileBufferedReader.readLine();

			//A-Values filename
			FileReader ratesFileReader = new FileReader(fileName); 
			BufferedReader ratesFileBufferedReader = new BufferedReader(ratesFileReader);
			String ratesLine = ratesFileBufferedReader.readLine();
			ratesLine = ratesFileBufferedReader.readLine();

			// mMax file 
			FileReader mMaxFileReader = new FileReader(mMaxFilename); 
			BufferedReader mMaxFileBufferedReader = new BufferedReader(mMaxFileReader);
			String mMaxLine = mMaxFileBufferedReader.readLine();
			mMaxLine = mMaxFileBufferedReader.readLine();
			
			double aVal, lat, lon, mMax, mag;
			
			while(ratesLine!=null) { // iterate over all locations
				aVal = Double.parseDouble(ratesLine);
				
				if(aVal!=0) { // check whether location is within RELM region
					StringTokenizer tokenizer = new StringTokenizer(latlonLine);
					lon = Double.parseDouble(tokenizer.nextToken());
					lat = Double.parseDouble(tokenizer.nextToken());
					if(region.isLocationInside(new Location(lat, lon))) {
//						 create GutenbergRichterMagFreqDist
						mMax = Double.parseDouble(mMaxLine);
						GutenbergRichterMagFreqDist mfd = new GutenbergRichterMagFreqDist(MIN_MAG, (int)((mMax-MIN_MAG)/DELTA_MAG+1), DELTA_MAG, 1.0, bValue);
						mfd.scaleToIncrRate(0.0, aVal*weight);
						
						int num = mfd.getNum();
						
						for(int i=0; i<num && applyBulgeReduction; ++i) { // apply bulge reduction
							mag = mfd.getX(i);
							if(mag>6.5) mfd.set(i, mfd.getY(i)/3);
						}
						
						summedMFD.addResampledMagFreqDist(mfd, true);
					}
				}

				latlonLine = regionFileBufferedReader.readLine();
				ratesLine = ratesFileBufferedReader.readLine();
				mMaxLine = mMaxFileBufferedReader.readLine();
			}
			
			regionFileBufferedReader.close();
			regionFileReader.close();
			ratesFileBufferedReader.close();
			ratesFileReader.close();
			mMaxFileBufferedReader.close();
			mMaxFileReader.close();

		}catch(Exception e) {
			e.printStackTrace();
		}

		return summedMFD;

	}
	
	
	public static void main(String args[]) {
		
		NSHMP_GridFileReader gridFileReader = new NSHMP_GridFileReader();
		GraphWindowAPI_Impl graphWindow = new GraphWindowAPI_Impl(gridFileReader.getBackgroundMFDs(), "Mag", "Rate", "Bckground Seis");
		//System.out.println(gridFileReader.getC_ZonesSummedMFD().getCumRateDist());
		System.out.println(gridFileReader.getBackgroundSummedMFD());
	}
}
