/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.griddedSeis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.opensha.data.Location;
import org.opensha.data.region.EvenlyGriddedRELM_Region;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.gui.GraphWindowAPI_Impl;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.EqkRateModel2_ERF;
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
	private final double DELTA_MAG = 0.1;
	private final double MIN_MAG = 0;

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
	public GutenbergRichterMagFreqDist getMFD_InsideRELM_Region(String fileName, double bValue, double mMin, double mMax, double weight) {
		// RELM region 
		EvenlyGriddedRELM_Region region = new EvenlyGriddedRELM_Region();
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
			GutenbergRichterMagFreqDist mfd = new GutenbergRichterMagFreqDist(MIN_MAG, (int)((mMax-MIN_MAG)/DELTA_MAG+1), DELTA_MAG, 1.0, bValue);
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
	public SummedMagFreqDist getMFD_InsideRELM_Region(String fileName, double bValue, double mMin, String mMaxFilename, double weight, boolean applyBulgeReduction) {
		// RELM region 
		EvenlyGriddedRELM_Region region = new EvenlyGriddedRELM_Region();
		SummedMagFreqDist summedMFD = new SummedMagFreqDist(EqkRateModel2_ERF.MIN_MAG, EqkRateModel2_ERF.MAX_MAG, EqkRateModel2_ERF.NUM_MAG);
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
		String Path = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_2/griddedSeis/";
		
		// C-Zones MFD. The final MFD can be checked with C-Zone MFD in GUI
		
		
		NSHMP_GridFileReader gridFileReader = new NSHMP_GridFileReader();
		SummedMagFreqDist summedMFD = new SummedMagFreqDist(EqkRateModel2_ERF.MIN_MAG, EqkRateModel2_ERF.MAX_MAG, EqkRateModel2_ERF.NUM_MAG);
		
		GutenbergRichterMagFreqDist mfd  = gridFileReader.getMFD_InsideRELM_Region("org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_2/griddedSeis/area1new.agrid.txt.asc", 0.8, 6.5, 7.6, 1);
		summedMFD.addResampledMagFreqDist(mfd, true);
		
		mfd  = gridFileReader.getMFD_InsideRELM_Region("org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_2/griddedSeis/area2new.agrid.txt.asc", 0.8, 6.5, 7.6, 1);
		summedMFD.addResampledMagFreqDist(mfd, true);
		
		mfd  = gridFileReader.getMFD_InsideRELM_Region("org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_2/griddedSeis/area3new.agrid.txt.asc", 0.8, 6.5, 7.6, 1);
		summedMFD.addResampledMagFreqDist(mfd, true);
		
		mfd  = gridFileReader.getMFD_InsideRELM_Region("org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_2/griddedSeis/area4new.agrid.txt.asc", 0.8, 6.5, 7.6, 1);
		summedMFD.addResampledMagFreqDist(mfd, true);
		
		mfd  = gridFileReader.getMFD_InsideRELM_Region("org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_2/griddedSeis/mojave.agrid.txt.asc", 0.8, 6.5, 7.6, 1);
		summedMFD.addResampledMagFreqDist(mfd, true);
		
		mfd  = gridFileReader.getMFD_InsideRELM_Region("org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_2/griddedSeis/sangreg.agrid.txt.asc", 0.8, 6.5, 7.6, 1);
		summedMFD.addResampledMagFreqDist(mfd, true);

		System.out.println(summedMFD.getCumRateDist()); 
		
		/*ArrayList funcList = new ArrayList();
		NSHMP_GridFileReader gridFileReader = new NSHMP_GridFileReader();

		String name = "agrd_brawly.out.txt.asc";
		GutenbergRichterMagFreqDist mfd  = gridFileReader.getMFD_InsideRELM_Region(Path+name, 0.8, 5, 7, 1);
		mfd.setName(name);
		funcList.add(mfd);

		name = "agrd_mendos.out.txt.asc";
		mfd  = gridFileReader.getMFD_InsideRELM_Region(Path+name, 0.8, 5, 7, 1);
		mfd.setName(name);
		funcList.add(mfd);


		name = "agrd_creeps.out.txt.asc";
		mfd  = gridFileReader.getMFD_InsideRELM_Region(Path+name, 0.9, 5, 6, 1);
		mfd.setName(name);
		funcList.add(mfd);
		
		name = "agrd_cstcal.out.txt.asc";
		SummedMagFreqDist summedMFD = new SummedMagFreqDist(EqkRateModel2_ERF.MIN_MAG, EqkRateModel2_ERF.MAX_MAG, EqkRateModel2_ERF.NUM_MAG);
		summedMFD.addResampledMagFreqDist(gridFileReader.getMFD_InsideRELM_Region(Path+name, 0.8, 5, Path+"fltmmaxCA2ch.out7.txt.asc", 0.5, true), true);
		summedMFD.addResampledMagFreqDist(gridFileReader.getMFD_InsideRELM_Region(Path+name, 0.8, 5, Path+"fltmmaxCA2gr.out7.txt.asc", 0.5, true), true);
		summedMFD.setName(name);
		funcList.add(summedMFD);
		

		name = "agrd_deeps.out.txt.asc";
		mfd  = gridFileReader.getMFD_InsideRELM_Region(Path+name, 0.9, 5, 7.2, 1);
		mfd.setName(name);
		funcList.add(mfd);
		
		name = "agrd_wuscmp.out.txt.asc";
		summedMFD = new SummedMagFreqDist(EqkRateModel2_ERF.MIN_MAG, EqkRateModel2_ERF.MAX_MAG, EqkRateModel2_ERF.NUM_MAG);
		mfd  = gridFileReader.getMFD_InsideRELM_Region(Path+name, 0.8, 5, 7, 0.5);
		summedMFD.addResampledMagFreqDist(mfd, true);
		mfd  = gridFileReader.getMFD_InsideRELM_Region(Path+name, 0.8, 5, 7, 0.5);
		summedMFD.addResampledMagFreqDist(mfd, true);
		summedMFD.setName(name);
		funcList.add(summedMFD);
		
		name = "agrd_wusext.out.txt.asc";
		summedMFD = new SummedMagFreqDist(EqkRateModel2_ERF.MIN_MAG, EqkRateModel2_ERF.MAX_MAG, EqkRateModel2_ERF.NUM_MAG);
		mfd  = gridFileReader.getMFD_InsideRELM_Region(Path+name, 0.8, 5, 7, 0.5);
		summedMFD.addResampledMagFreqDist(mfd, true);
		mfd  = gridFileReader.getMFD_InsideRELM_Region(Path+name, 0.8, 5, 7, 0.5);
		summedMFD.addResampledMagFreqDist(mfd, true);
		summedMFD.setName(name);
		funcList.add(summedMFD);
		
		GraphWindowAPI_Impl graphWindow = new GraphWindowAPI_Impl(funcList, "Mag", "Rate", "Bckground Seis");*/
		
	}
}
