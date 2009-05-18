/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.griddedSeis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.region.EvenlyGriddedRELM_Region;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Point2Vert_SS_FaultPoisSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.UCERF2;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;




/**
 * Read NSHMP backgroud seismicity files.  This trims the western edge of the RELM
 * region so there are no zero rate bins (i.e., the number of locationis is the same
 * as the number of non-zero rate cells).  The number of locations in the result is
 * numLocs=7654.
 * 
 * I verified sumOfAllAvals against my hand sum using Igor.
 * 
 * @author Ned Field & Vipin Gupta
 *
 */
public class NSHMP_GridSourceGenerator extends EvenlyGriddedRELM_Region {

	private final static String PATH = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_2/griddedSeis/";
	private final static String LAT_LON_FILENAME = PATH + "LonsLats.txt";
	
	private int[] aValIndexForLocIndex;
	private double[] sumOfAllAvals;
	private int numAvals;
	// a-val and mmax values
	private double[] agrd_brawly_out, agrd_creeps_out, agrd_cstcal_out, agrd_deeps_out, 
					agrd_mendos_out, agrd_wuscmp_out, agrd_wusext_out, area1new_agrid, 
					area2new_agrid, area3new_agrid, area4new_agrid,mojave_agrid, sangreg_agrid,
					fltmmaxALLCNch_outv3, fltmmaxALLCNgr_outv3, fltmmaxCA2ch_out7, fltmmaxCA2gr_out7;
	
	private final static double B_VAL = 0.8;
	private final static double B_VAL_CREEPING = 0.9;
	private final static double DELTA_MAG = 0.1;

	private final double C_ZONES_MAX_MAG = 7.6;
	private final double DEFAULT_MAX_MAG=7.0;
	
	private double maxFromMaxMagFiles;
	
	  public NSHMP_GridSourceGenerator() {
		  	getLocationList();
		    
		    // trim the western edge of the RELM region where NSHMP area doesn't get to
		    // (so we don't have to filter out zero rate bins in the RELM region)
		    for(int i=0; i<locList.size();i++) {
		    		if(locList.getLocationAt(i).getLongitude() < -125.001)
		    			locList.getLocationAt(i).setLongitude(-125.0);
		    }
		    
		    // make polygon from the location list
		    createEvenlyGriddedGeographicRegion(locList, GRID_SPACING);

		    setA_ValIndexForLocIndex();
		    //System.out.println("numAvals="+numAvals+"; numLocs="+getNumGridLocs());
		    //System.out.println("reading all files");
		    readAllGridFiles();
		    //System.out.println("done");
		  
		    /*  This is to output and check the sum 
		    Location loc;
		    System.out.println("test_lat\ttest_lon\ttest_rate");
		    for(int i=0;i<this.getNumGridLocs();i++) {
		    		loc = this.getGridLocation(i);
		    		System.out.println((float)loc.getLatitude()+"\t"+
		    				(float)loc.getLongitude()+"\t"+
		    				(float)sumOfAllAvals[i]);
		    }
		    */
		    
	}
	 
	  
	 /**
	  * Get all gridded sources
	  * 
	  * @param includeC_Zones
	  * @param duration
	  * @param applyBulgeReduction
	  * @param applyMaxMagGrid
	  * @return
	  */ 
	 public ArrayList<ProbEqkSource> getAllGriddedSources(boolean includeC_Zones, double duration, 
			 boolean applyBulgeReduction, boolean applyMaxMagGrid) {
		int numSources =  getNumSources();
		ArrayList<ProbEqkSource> sources = new ArrayList<ProbEqkSource>();
		for(int i=0; i<numSources; ++i) {
			sources.add(getGriddedSource(i, includeC_Zones, duration, applyBulgeReduction, applyMaxMagGrid));
		}
		return sources;
	 }
	  
	  
	  /**
	   * Get the griddedsource at a specified index
	   * 
	   * @param srcIndex
	   * @return
	   */
	 public ProbEqkSource getGriddedSource(int srcIndex, boolean includeC_Zones, double duration, 
			 boolean applyBulgeReduction, boolean applyMaxMagGrid) {
		 SummedMagFreqDist mfdAtLoc = getTotMFD_atLoc(srcIndex,  includeC_Zones, applyBulgeReduction,  applyMaxMagGrid);
		 return new Point2Vert_SS_FaultPoisSource(this.getGridLocation(srcIndex), mfdAtLoc, null, duration, 10.0);
	 }
	 
	 
	 /**
	  * Get the number of gridded sources
	  *  
	  * @return
	  */
	 public int getNumSources() {
		 return this.getNumGridLocs();
	 }
	
	
	/**
	 * This determins the index in each grid file that corresponds to the 
	 * ith location in the RELM regions, setting the value to -1 if there is
	 * no NSHMP grid cell for any of the RELM sites (the most western lons).
	 *
	 */
	private void setA_ValIndexForLocIndex() {
		aValIndexForLocIndex = new int[getNumGridLocs()];
		//initialize values to -1 (bogus index) because not all RELM locs have a corresponding line in the grid file
		for(int i=0;i<aValIndexForLocIndex.length;i++)
			aValIndexForLocIndex[i] = -1;
		numAvals = 0;
		
		try { 
			// Region filename
			InputStreamReader regionFileReader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(LAT_LON_FILENAME));
			BufferedReader regionFileBufferedReader = new BufferedReader(regionFileReader);
			String latlonLine = regionFileBufferedReader.readLine(); // skip header line
			Location loc;
			double lat, lon;
			int fileIndex = 0;

			latlonLine = regionFileBufferedReader.readLine();
			while(latlonLine!=null) { // iterate over all lines of the file
				StringTokenizer tokenizer = new StringTokenizer(latlonLine);
				lon = Double.parseDouble(tokenizer.nextToken());
				lat = Double.parseDouble(tokenizer.nextToken());
				loc = new Location(lat, lon);
				if(this.isLocationInside(loc))
					aValIndexForLocIndex[this.getNearestLocationIndex(loc)] = fileIndex;
				latlonLine = regionFileBufferedReader.readLine();
				fileIndex += 1;
				numAvals += 1;
			}
			regionFileBufferedReader.close();
			regionFileReader.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This reads all grid files into arrays
	 *
	 */
	private void readAllGridFiles() {
		
		// 
		sumOfAllAvals = new double[numAvals];
		
		agrd_cstcal_out = readGridFile(PATH+"agrd_cstcal.out.asc",true);
		agrd_brawly_out = readGridFile(PATH+"agrd_brawly.out.asc",true);
		agrd_creeps_out = readGridFile(PATH+"agrd_creeps.out.asc",true);
		agrd_deeps_out = readGridFile(PATH+"agrd_deeps.out.asc",true);
		agrd_mendos_out = readGridFile(PATH+"agrd_mendos.out.asc",true);
		agrd_wuscmp_out = readGridFile(PATH+"agrd_wuscmp.out.asc",true);
		agrd_wusext_out = readGridFile(PATH+"agrd_wusext.out.asc",true);
		area1new_agrid  = readGridFile(PATH+"area1new.agrid.asc",true);
		area2new_agrid = readGridFile(PATH+"area2new.agrid.asc",true);
		area3new_agrid = readGridFile(PATH+"area3new.agrid.asc",true);
		area4new_agrid = readGridFile(PATH+"area4new.agrid.asc",true);
		mojave_agrid = readGridFile(PATH+"mojave.agrid.asc",true);
		sangreg_agrid = readGridFile(PATH+"sangreg.agrid.asc",true);
		fltmmaxALLCNch_outv3 = readGridFile(PATH+"fltmmaxALLCNch.outv3.asc",false);
		fltmmaxALLCNgr_outv3 = readGridFile(PATH+"fltmmaxALLCNgr.outv3.asc",false);
		fltmmaxCA2ch_out7 = readGridFile(PATH+"fltmmaxCA2ch.out7.asc",false);
		fltmmaxCA2gr_out7 = readGridFile(PATH+"fltmmaxCA2gr.out7.asc",false);
		
		int numMags = fltmmaxALLCNch_outv3.length;
		
		
		// find maximum magitude from max mag files
		maxFromMaxMagFiles = -1;
		for(int i=0; i<numMags; ++i) {
			if(fltmmaxALLCNch_outv3[i] > maxFromMaxMagFiles) 
				maxFromMaxMagFiles = fltmmaxALLCNch_outv3[i];
			if(fltmmaxALLCNgr_outv3[i] > maxFromMaxMagFiles) 
				maxFromMaxMagFiles = fltmmaxALLCNgr_outv3[i];
			if(fltmmaxCA2ch_out7[i] > maxFromMaxMagFiles) 
				maxFromMaxMagFiles = fltmmaxCA2ch_out7[i];
			if(fltmmaxCA2gr_out7[i] > maxFromMaxMagFiles) 
				maxFromMaxMagFiles = fltmmaxCA2gr_out7[i];
		}

		//System.out.println(maxFromMaxMagFiles);
		/* find indices that are zeros in all files
		// NO LONGER NEEDED SINCE I TRIM THE RELM REGION
		for(int i=0; i<agrd_brawly_out.length;i++){
			boolean allZero = true;
			if(agrd_brawly_out[i] !=0) allZero = false;
			if(agrd_creeps_out[i] !=0) allZero = false;
			if(agrd_cstcal_out[i] !=0) allZero = false;
			if(agrd_deeps_out[i] !=0) allZero = false;
			if(agrd_mendos_out[i] !=0) allZero = false;
			if(agrd_wuscmp_out[i] !=0) allZero = false;
			if(agrd_wusext_out[i] !=0) allZero = false;
			if(area1new_agrid[i] !=0) allZero = false;
			if(area2new_agrid[i] !=0) allZero = false;
			if(area3new_agrid[i] !=0) allZero = false;
			if(area4new_agrid[i] !=0) allZero = false;
			if(mojave_agrid[i] !=0) allZero = false;
			if(sangreg_agrid[i] !=0) allZero = false;

			if(allZero) {
				Location loc = this.getGridLocation(i);
				System.out.println(i+" is all zero; aValIndexForLocIndex = "+
						aValIndexForLocIndex[i]+" Location: "+
						(float)loc.getLatitude()+"\t"+
	    				(float)loc.getLongitude()+"\t"+
	    				(float)sumOfAllAvals[i]);
			}
		}
		*/
	}
	
	/**
	 * this reads an NSHMP grid file.  The boolean specifies whether to add this to a running 
	 * total (sumOfAllAvals[i]).
	 * This could be modified to read binary files
	 * @param fileName
	 * @return
	 */
	public double[] readGridFile(String fileName, boolean addToSumOfAllAvals) {
		double[] allGridVals = new double[numAvals];
//		System.out.println("    Working on "+fileName);
		try { 
			InputStreamReader ratesFileReader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(fileName));
			BufferedReader ratesFileBufferedReader = new BufferedReader(ratesFileReader);
			String ratesLine = ratesFileBufferedReader.readLine(); // skip header
			ratesLine = ratesFileBufferedReader.readLine();
			int index = 0;

			while(ratesLine!=null) { // iterate over all locations
				allGridVals[index] = Double.parseDouble(ratesLine);
				index += 1;
				ratesLine = ratesFileBufferedReader.readLine();
			}
			ratesFileBufferedReader.close();
			ratesFileReader.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		//now keep only the ones in the RELM region
		double[] gridVals = new double[getNumGridLocs()];
		for(int i=0;i<gridVals.length;i++) {
			int aValIndex = aValIndexForLocIndex[i];
			if(aValIndex != -1) {  // ignore the RELM locs outside the NSHMP region
				gridVals[i] = allGridVals[aValIndex];
				if(addToSumOfAllAvals) sumOfAllAvals[i]+=gridVals[i];
			}
		}
		return gridVals;
	}
	
	/**
	 * This creates an NSHMP mag-freq distribution from their a-value, 
	 * with an option to reduce rates at & above M 6.5 by a factor of three.
	 * @param minMag
	 * @param maxMag
	 * @param aValue
	 * @param bValue
	 * @param applyBulgeReduction
	 * @return
	 */		
	private GutenbergRichterMagFreqDist getMFD(double minMag, double maxMag, double aValue, 
											double bValue, boolean applyBulgeReduction) {
		int numMag = Math.round((float)((maxMag-minMag)/DELTA_MAG+1));
		GutenbergRichterMagFreqDist mfd = new GutenbergRichterMagFreqDist(minMag, numMag, DELTA_MAG, 1.0, bValue);
		mfd.scaleToIncrRate(minMag, aValue*Math.pow(10,-bValue*minMag));
		
//		apply bulge reduction at & above mag 6.5 if desired
		if(applyBulgeReduction && mfd.getMaxX()>=6.5) {	
			for(int i=mfd.getXIndex(6.5); i<mfd.getNum(); ++i)
				mfd.set(i, mfd.getY(i)/3);
		}
		return mfd;
	}

	
	/**
	 * This gets the total NSHMP mag-freq dist for the given array of a-values
	 * @param minMag
	 * @param maxMag
	 * @param aValueArray
	 * @param bValue
	 * @param applyBulgeReduction
	 * @return
	 */
	private GutenbergRichterMagFreqDist getTotalMFD(double minMag, double maxMag, double[] aValueArray, 
			double bValue, boolean applyBulgeReduction) {
		
		double tot_aValue = 0;
		for(int i=0; i<aValueArray.length; i++) tot_aValue += aValueArray[i];
		return getMFD(minMag, maxMag, tot_aValue, bValue, applyBulgeReduction);
	}
	
	
	public GutenbergRichterMagFreqDist getTotalC_ZoneMFD() {
		double tot_aValue = 0;
		for(int i=0; i<area1new_agrid.length; i++) 
			tot_aValue += area1new_agrid[i]+area2new_agrid[i]+area3new_agrid[i]+
						  area4new_agrid[i]+mojave_agrid[i]+sangreg_agrid[i];
		return getMFD(6.5, 7.6, tot_aValue, B_VAL, false);
	}
	
	
	/**
	 * Note that applyBulgeReduction only applies to agrd_cstcal_out
	 * Get Total MFD for Region
	 * 
	 * @param includeC_zones
	 * @param applyBulgeReduction
	 * @param applyMaxMagGrid
	 * @return
	 */
	public SummedMagFreqDist getTotMFDForRegion(boolean includeC_zones, 
			boolean applyBulgeReduction, boolean applyMaxMagGrid) {
		
		// create summed MFD
		int numMags = (int)Math.round((UCERF2.MAX_MAG-UCERF2.MIN_MAG)/DELTA_MAG) + 1;
		SummedMagFreqDist totMFD = new SummedMagFreqDist(UCERF2.MIN_MAG, UCERF2.MAX_MAG, numMags);
		int numLocs = getNumGridLocs();
		for(int locIndex=0; locIndex<numLocs; ++locIndex)
			totMFD.addResampledMagFreqDist(getTotMFD_atLoc( locIndex,  includeC_zones, 
					 applyBulgeReduction,  applyMaxMagGrid), true);
		return totMFD;
	}
	
	/**
	 * Note that applyBulgeReduction only applies to agrd_cstcal_out
	 * @param locIndex
	 * @param includeC_zones
	 * @param applyBulgeReduction
	 * @param applyMmaxGrid
	 * @return
	 */
	public SummedMagFreqDist getTotMFD_atLoc(int locIndex, boolean includeC_zones, 
			boolean applyBulgeReduction, boolean applyMaxMagGrid) {
		
		
		// find max mag among all contributions
		double maxMagAtLoc = C_ZONES_MAX_MAG;
		/*if(includeC_zones) { // if C-zones are included
			if(this.C_ZONES_MAX_MAG>maxMagAtLoc) maxMagAtLoc = C_ZONES_MAX_MAG;
		}
		else {
			if(applyMaxMagGrid) {
				if(fltmmaxALLCNch_outv3[locIndex] > maxMagAtLoc) 
					maxMagAtLoc = fltmmaxALLCNch_outv3[locIndex];
				if(fltmmaxALLCNgr_outv3[locIndex] > maxMagAtLoc) 
					maxMagAtLoc = fltmmaxALLCNgr_outv3[locIndex];
				if(fltmmaxCA2ch_out7[locIndex] > maxMagAtLoc) 
					maxMagAtLoc = fltmmaxCA2ch_out7[locIndex];
				if(fltmmaxCA2gr_out7[locIndex] > maxMagAtLoc) 
					maxMagAtLoc = fltmmaxCA2gr_out7[locIndex];
			} else {
				maxMagAtLoc = DEFAULT_MAX_MAG;
			}
		}*/
		
		// create summed MFD
		int numMags = (int)Math.round((maxMagAtLoc-UCERF2.MIN_MAG)/DELTA_MAG) + 1;
		SummedMagFreqDist mfdAtLoc = new SummedMagFreqDist(UCERF2.MIN_MAG, maxMagAtLoc, numMags);

		
		// create and add each contributing MFD
		mfdAtLoc.addResampledMagFreqDist(getMFD(5.0, 6.5, agrd_brawly_out[locIndex], B_VAL, false), true);
		mfdAtLoc.addResampledMagFreqDist(getMFD(5.0, 7.0, agrd_mendos_out[locIndex], B_VAL, false), true);	
		mfdAtLoc.addResampledMagFreqDist(getMFD(5.0, 6.0, agrd_creeps_out[locIndex], B_VAL_CREEPING, false), true);
		mfdAtLoc.addResampledMagFreqDist(getMFD(5.0, 7.2, agrd_deeps_out[locIndex], B_VAL, false), true);
		if(applyMaxMagGrid) {	 // Apply Max Mag from files
			mfdAtLoc.addResampledMagFreqDist(getMFD(5.0, fltmmaxCA2ch_out7[locIndex], 0.667*agrd_cstcal_out[locIndex], B_VAL, applyBulgeReduction), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(5.0, fltmmaxCA2gr_out7[locIndex], 0.333*agrd_cstcal_out[locIndex], B_VAL, applyBulgeReduction), true);

			mfdAtLoc.addResampledMagFreqDist(getMFD(5.0, fltmmaxALLCNch_outv3[locIndex], 0.667*agrd_wuscmp_out[locIndex], B_VAL, false), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(5.0, fltmmaxALLCNgr_outv3[locIndex], 0.333*agrd_wuscmp_out[locIndex], B_VAL, false), true);

			mfdAtLoc.addResampledMagFreqDist(getMFD(5.0, fltmmaxALLCNch_outv3[locIndex], 0.667*agrd_wusext_out[locIndex], B_VAL, false), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(5.0, fltmmaxALLCNgr_outv3[locIndex], 0.333*agrd_wusext_out[locIndex], B_VAL, false), true);
		} else { // Apply default Mag Max
			mfdAtLoc.addResampledMagFreqDist(getMFD(5.0, DEFAULT_MAX_MAG, agrd_cstcal_out[locIndex], B_VAL, applyBulgeReduction), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(5.0, DEFAULT_MAX_MAG, agrd_wuscmp_out[locIndex], B_VAL, false), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(5.0, DEFAULT_MAX_MAG, agrd_wusext_out[locIndex], B_VAL, false), true);
		}
		if(includeC_zones) { // Include C-Zones
			mfdAtLoc.addResampledMagFreqDist(getMFD(6.5, C_ZONES_MAX_MAG, area1new_agrid[locIndex], B_VAL, false), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(6.5, C_ZONES_MAX_MAG, area2new_agrid[locIndex], B_VAL, false), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(6.5, C_ZONES_MAX_MAG, area3new_agrid[locIndex], B_VAL, false), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(6.5, C_ZONES_MAX_MAG, area4new_agrid[locIndex], B_VAL, false), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(6.5, C_ZONES_MAX_MAG, mojave_agrid[locIndex], B_VAL, false), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(6.5, C_ZONES_MAX_MAG, sangreg_agrid[locIndex], B_VAL, false), true);	
		}	
		
		
		return mfdAtLoc;
	}
	
	public static void main(String args[]) {
		NSHMP_GridSourceGenerator srcGen = new NSHMP_GridSourceGenerator();
		//System.out.println(srcGen.getTotalC_ZoneMFD().getCumRateDist());
		//System.out.println(srcGen.getTotMFDForRegion(false, true, true));
		double[] area1new_agrid  = srcGen.readGridFile(PATH+"area1new.agrid.asc",false);
		
		for(int i=0; i<area1new_agrid.length; i++) System.out.println(area1new_agrid[i]);
	}
}
