/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.griddedSeis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.region.EvenlyGriddedRELM_Region;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.gui.GraphWindowAPI_Impl;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.EqkRateModel2_ERF;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import sun.tools.tree.ThisExpression;



/**
 * Read NSHMP backgroud seismicity files
 * 
 * @author Ned Field & Vipin Gupta
 *
 */
public class NSHMP_GridSourceGenerator extends EvenlyGriddedRELM_Region {

	private final static String PATH = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_2/griddedSeis/";
	private final static String LAT_LON_FILENAME = PATH + "LonsLats.txt";
	
	private int[] aValIndexForLocIndex;
	private int numAvals;
	// a-val and mmax values
	private double[] agrd_brawly_out, agrd_creeps_out, agrd_cstcal_out, agrd_deeps_out, 
					agrd_mendos_out, agrd_wuscmp_out, agrd_wusext_out, area1new_agrid, 
					area2new_agrid, area3new_agrid, area4new_agrid,mojave_agrid, sangreg_agrid,
					fltmmaxALLCNch_outv3, fltmmaxALLCNgr_outv3, fltmmaxCA2ch_out7, fltmmaxCA2gr_out7;
	
	
	
	  public NSHMP_GridSourceGenerator() {
		    super();
		    System.out.println("Setting aValIndexForLocIndex");
		    setA_ValIndexForLocIndex();
		    System.out.println("numAvals="+numAvals+"; numLocs="+getNumGridLocs());
		    System.out.println("reading all files");
		    readAllGridFiles();
		    System.out.println("done");
	}
	
	
	/**
	 * This determins the index in each grid file that corresponds to the ith location in the RELM regions
	 *
	 */
	private void setA_ValIndexForLocIndex() {
		aValIndexForLocIndex = new int[getNumGridLocs()];
		numAvals = 0;
		
		try { 
			// Region filename
			FileReader regionFileReader = new FileReader(LAT_LON_FILENAME); 
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
		agrd_brawly_out = readGridFile(PATH+"agrd_brawly.out.asc");
		agrd_creeps_out = readGridFile(PATH+"agrd_creeps_out.asc");
		agrd_cstcal_out = readGridFile(PATH+"agrd_cstcal_out.asc");
		agrd_deeps_out = readGridFile(PATH+"agrd_deeps_out.asc");
		agrd_mendos_out = readGridFile(PATH+"agrd_mendos_out.asc");
		agrd_wuscmp_out = readGridFile(PATH+"agrd_wuscmp_out.asc");
		agrd_wusext_out = readGridFile(PATH+"agrd_wusext_out.asc");
		area1new_agrid  = readGridFile(PATH+"area1new_agrid.asc");
		area2new_agrid = readGridFile(PATH+"area2new_agrid.asc");
		area3new_agrid = readGridFile(PATH+"area3new_agrid.asc");
		area4new_agrid = readGridFile(PATH+"area4new_agrid.asc");
		mojave_agrid = readGridFile(PATH+"mojave_agrid.asc");
		sangreg_agrid = readGridFile(PATH+"sangreg_agrid.asc");
		fltmmaxALLCNch_outv3 = readGridFile(PATH+"fltmmaxALLCNch_outv3.asc");
		fltmmaxALLCNgr_outv3 = readGridFile(PATH+"fltmmaxALLCNgr_outv3.asc");
		fltmmaxCA2ch_out7 = readGridFile(PATH+"fltmmaxCA2ch_out7.asc");
		fltmmaxCA2gr_out7 = readGridFile(PATH+"fltmmaxCA2gr_out7.asc");
	}
	
	/**
	 * this reads an NSHMP grid file
	 * This could be modified to read binary files
	 * @param fileName
	 * @return
	 */
	private double[] readGridFile(String fileName) {
		double[] allGridVals = new double[numAvals];
		System.out.println("    Working on "+fileName);
		try { 
			FileReader ratesFileReader = new FileReader(fileName); 
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
		for(int i=0;i<gridVals.length;i++)
			gridVals[i] = allGridVals[aValIndexForLocIndex[i]];
		return gridVals;
	}
	
	

	
	public static void main(String args[]) {
		NSHMP_GridSourceGenerator srcGen = new NSHMP_GridSourceGenerator();
	}
}
