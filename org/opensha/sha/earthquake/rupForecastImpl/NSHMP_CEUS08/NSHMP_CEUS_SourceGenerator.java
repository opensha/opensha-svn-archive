/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.NSHMP_CEUS08;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.opensha.calc.magScalingRelations.magScalingRelImpl.WC1994_MagLengthRelationship;
import org.opensha.data.Location;
import org.opensha.data.region.EvenlyGriddedRectangularGeographicRegion;
import org.opensha.data.region.GeographicRegion;
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;




/**
 * Read NSHMP backgroud seismicity files for CEUS.  
 * 
 * @author Ned Field
 *
 */
public class NSHMP_CEUS_SourceGenerator extends EvenlyGriddedRectangularGeographicRegion {

	private final static WC1994_MagLengthRelationship magLenRel = new WC1994_MagLengthRelationship();

	private final static String PATH = "org/opensha/sha/earthquake/rupForecastImpl/NSHMP_CEUS08/";

	// a-val and mmax values
	private double[] adapt_cn_vals, charnCagrid1008_vals, adapt_cy_vals, gm_ab_6p6_7p1_vals, agrd_chrls3_6p8_vals,
	gm_ab_6p8_7p3_vals, agrd_chrls3_7p1_vals, gm_ab_7p0_7p5_vals, agrd_chrls3_7p3_vals, gm_ab_7p2_7p7_vals, agrd_chrls3_7p5_vals, 
	gm_j_6p6_7p1_vals, charlnA_vals, gm_j_6p8_7p3_vals, charlnB_vals, gm_j_7p0_7p5_vals, charlnarrow_vals, gm_j_7p2_7p7_vals;

	public NSHMP_CEUS_SourceGenerator() throws RegionConstraintException {
			super(24.6, 50, -115, -65, 0.1);


		// lat range: 24.6 to 50.0
		// lon range: -115.0 to -65.0
		// grid spacing: 0.1
		// num points: 127754

		readAllGridFiles();
	}


	/**
	 * This reads all grid files into arrays
	 *
	 */
	private void readAllGridFiles() {


		adapt_cn_vals = readGridFile(PATH+"adapt_cn_vals.txt");
	}

	/**
	 * this reads an NSHMP grid file.  The boolean specifies whether to add this to a running 
	 * total (sumOfAllAvals[i]).
	 * This could be modified to read binary files
	 * @param fileName
	 * @return
	 */
	public double[] readGridFile(String fileName) {
		double[] allGridVals = new double[this.getNumGridLocs()];
		System.out.println("    Working on "+fileName);
		try { 
			InputStreamReader ratesFileReader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(fileName));
			BufferedReader ratesFileBufferedReader = new BufferedReader(ratesFileReader);

			for(int line=0;line<getNumGridLocs();line++) {
				String lineString = ratesFileBufferedReader.readLine();
				StringTokenizer st = new StringTokenizer(lineString);
				double lon = new Double(st.nextToken());
				double lat = new Double(st.nextToken());
				double val = new Double(st.nextToken());

				//find index of this location
				int index = getNearestLocationIndex(new Location(lat,lon));

				allGridVals[index] = val;
			}
			ratesFileBufferedReader.close();
			ratesFileReader.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return allGridVals;
	}






	public static void main(String args[]) {
		try {
			NSHMP_CEUS_SourceGenerator srcGen = new NSHMP_CEUS_SourceGenerator();
		}catch(Exception e) {
			e.printStackTrace();
		}

	}
}
