/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.NSHMP_CEUS08;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.opensha.calc.magScalingRelations.magScalingRelImpl.WC1994_MagLengthRelationship;
import org.opensha.data.Location;
import org.opensha.data.region.EvenlyGriddedRectangularGeographicRegion;
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.griddedSeis.Point2Vert_FaultPoisSource;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;




/**
 * Read NSHMP backgroud seismicity files for CEUS.  
 * 
 * @author Ned Field
 *
 */
public class NSHMP_CEUS_SourceGenerator extends EvenlyGriddedRectangularGeographicRegion {

	private final static WC1994_MagLengthRelationship magLenRel = new WC1994_MagLengthRelationship();

	private final static String PATH = "org"+File.separator+"opensha"+File.separator+"sha"+File.separator+"earthquake"+File.separator+"rupForecastImpl"+File.separator+"NSHMP_CEUS08"+File.separator+"inputFiles"+File.separator;
	
	private double MIN_MAG = 5.0, DELTA_MAG = 0.1, MAX_MAG_DEFAULT = 7;
	// broad CEUS a-value files
	double[] adapt_cn_vals, adapt_cy_vals;
	
	// CEUS b-values
	double[] gb_vals;
	
	//CEUS Mmax files
	double[] 	gm_ab_6p6_7p1_vals,gm_ab_6p8_7p3_vals,gm_ab_7p0_7p5_vals, gm_ab_7p2_7p7_vals, 
				gm_j_6p6_7p1_vals, gm_j_6p8_7p3_vals, gm_j_7p0_7p5_vals, gm_j_7p2_7p7_vals;

		// Charleston files ????
	double[] 	agrd_chrls3_6p8_vals, agrd_chrls3_7p1_vals, agrd_chrls3_7p3_vals, agrd_chrls3_7p5_vals;
	double[] 	charlnA_vals, charlnB_vals, charlnarrow_vals, charnCagrid1008_vals;
	
	double ptSrcMagCutOff = 6.0;
	double fracStrikeSlip=1,fracNormal=0,fracReverse=0;


	public NSHMP_CEUS_SourceGenerator() throws RegionConstraintException {
			super(24.6, 50, -115, -65, 0.1);


		// lat range: 24.6 to 50.0
		// lon range: -115.0 to -65.0
		// grid spacing: 0.1
		// num points: 127755

		readAllGridFiles();
	}


	/**
	 * This reads all grid files into arrays
	 *
	 */
	private void readAllGridFiles() {


		adapt_cn_vals = readGridFile(PATH+"adapt_cn_vals.txt");
		adapt_cy_vals  = readGridFile(PATH+"adapt_cy_vals.txt");
		
		gb_vals = readGridFile(PATH+"gb_vals.txt");
		
		gm_ab_6p6_7p1_vals = readGridFile(PATH+"gm_ab_6p6_7p1_vals.txt");
		gm_ab_6p8_7p3_vals  = readGridFile(PATH+"gm_ab_6p8_7p3_vals.txt");
		gm_ab_7p0_7p5_vals  = readGridFile(PATH+"gm_ab_7p0_7p5_vals.txt");
		gm_ab_7p2_7p7_vals  = readGridFile(PATH+"gm_ab_7p2_7p7_vals.txt");
		gm_j_6p6_7p1_vals  = readGridFile(PATH+"gm_j_6p6_7p1_vals.txt");
		gm_j_6p8_7p3_vals  = readGridFile(PATH+"gm_j_6p8_7p3_vals.txt");
		gm_j_7p0_7p5_vals  = readGridFile(PATH+"gm_j_7p0_7p5_vals.txt");
		gm_j_7p2_7p7_vals = readGridFile(PATH+"gm_j_7p2_7p7_vals.txt");

		agrd_chrls3_6p8_vals  = readGridFile(PATH+"agrd_chrls3_6p8_vals.txt");
		agrd_chrls3_7p1_vals  = readGridFile(PATH+"agrd_chrls3_7p1_vals.txt");
		agrd_chrls3_7p3_vals  = readGridFile(PATH+"agrd_chrls3_7p3_vals.txt");
		agrd_chrls3_7p5_vals  = readGridFile(PATH+"agrd_chrls3_7p5_vals.txt");

		charlnA_vals  = readGridFile(PATH+"charlnA_vals.txt");
		charlnB_vals  = readGridFile(PATH+"charlnB_vals.txt");
		charlnarrow_vals  = readGridFile(PATH+"charlnarrow_vals.txt");
		charnCagrid1008_vals  = readGridFile(PATH+"charnCagrid1008_vals.txt");

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
				
				if(index == -1)
					throw new RuntimeException("Error in getting index for "+fileName);

				allGridVals[index] = val;
			}
			ratesFileBufferedReader.close();
			ratesFileReader.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return allGridVals;
	}
	
	public double getMaxMagAtLoc(int locIndex) {
		// find max mag among all contributions
		double maxMagAtLoc = gm_ab_6p6_7p1_vals[locIndex];
		if(gm_ab_6p8_7p3_vals[locIndex]>maxMagAtLoc) maxMagAtLoc = gm_ab_6p8_7p3_vals[locIndex];
		if(gm_ab_7p0_7p5_vals[locIndex]>maxMagAtLoc) maxMagAtLoc = gm_ab_7p0_7p5_vals[locIndex];
		if(gm_ab_7p2_7p7_vals[locIndex]>maxMagAtLoc) maxMagAtLoc = gm_ab_7p2_7p7_vals[locIndex];
		if(gm_j_6p6_7p1_vals[locIndex]>maxMagAtLoc) maxMagAtLoc = gm_j_6p6_7p1_vals[locIndex];
		if(gm_j_6p8_7p3_vals[locIndex]>maxMagAtLoc) maxMagAtLoc = gm_j_6p8_7p3_vals[locIndex];
		if(gm_j_7p0_7p5_vals[locIndex]>maxMagAtLoc) maxMagAtLoc = gm_j_7p0_7p5_vals[locIndex];
		if(gm_j_7p2_7p7_vals[locIndex]>maxMagAtLoc) maxMagAtLoc = gm_j_7p2_7p7_vals[locIndex];
		
//		System.out.println(maxMagAtLoc);
		
		if (maxMagAtLoc>0)
			return maxMagAtLoc;
		else
			return MAX_MAG_DEFAULT;
		
	}

	public SummedMagFreqDist getTotMFD_atLoc(int locIndex) {

		double maxMagAtLoc = getMaxMagAtLoc(locIndex);

		// create summed MFD
		int numMags = (int) Math.round((maxMagAtLoc-MIN_MAG)/DELTA_MAG) + 1;
		SummedMagFreqDist mfdAtLoc = new SummedMagFreqDist(UCERF2.MIN_MAG, maxMagAtLoc, numMags);
		
		boolean allValuesZero = true;
		
		if(adapt_cn_vals[locIndex] > 0) {
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, gm_ab_6p6_7p1_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex], 0.01667), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, gm_ab_6p8_7p3_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex], 0.0333), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, gm_ab_7p0_7p5_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex], 0.08333), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, gm_ab_7p2_7p7_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex], 0.0333), true);

			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, gm_j_6p6_7p1_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex],  0.01667), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, gm_j_6p8_7p3_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex],  0.0333), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, gm_j_7p0_7p5_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex],  0.08333), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, gm_j_7p2_7p7_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex],  0.0333), true);		
			
			allValuesZero = false;
		}

		if(adapt_cy_vals[locIndex] > 0) {
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, gm_ab_6p6_7p1_vals[locIndex], adapt_cy_vals[locIndex], gb_vals[locIndex], 0.0333), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, gm_ab_6p8_7p3_vals[locIndex], adapt_cy_vals[locIndex], gb_vals[locIndex], 0.0667), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, gm_ab_7p0_7p5_vals[locIndex], adapt_cy_vals[locIndex], gb_vals[locIndex], 0.16667), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, gm_ab_7p2_7p7_vals[locIndex], adapt_cy_vals[locIndex], gb_vals[locIndex], 0.0667), true);

			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, gm_j_6p6_7p1_vals[locIndex], adapt_cy_vals[locIndex], gb_vals[locIndex],  0.0333), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, gm_j_6p8_7p3_vals[locIndex], adapt_cy_vals[locIndex], gb_vals[locIndex],  0.0667), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, gm_j_7p0_7p5_vals[locIndex], adapt_cy_vals[locIndex], gb_vals[locIndex],  0.16667), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, gm_j_7p2_7p7_vals[locIndex], adapt_cy_vals[locIndex], gb_vals[locIndex],  0.0667), true);			

			allValuesZero = false;
		}


		// for each branch separately: 
		/*
		GutenbergRichterMagFreqDist mfd01 = getMFD(MIN_MAG, gm_ab_6p6_7p1_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex], 1);
		GutenbergRichterMagFreqDist mfd02 = getMFD(MIN_MAG, gm_ab_6p8_7p3_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex], 1);
		GutenbergRichterMagFreqDist mfd03 = getMFD(MIN_MAG, gm_ab_7p0_7p5_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex], 1);
		GutenbergRichterMagFreqDist mfd04 = getMFD(MIN_MAG, gm_ab_7p2_7p7_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex], 1);

		GutenbergRichterMagFreqDist mfd05 = getMFD(MIN_MAG, gm_j_6p6_7p1_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex],  1);
		GutenbergRichterMagFreqDist mfd06 = getMFD(MIN_MAG, gm_j_6p8_7p3_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex],  1);
		GutenbergRichterMagFreqDist mfd07 = getMFD(MIN_MAG, gm_j_7p0_7p5_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex],  1);
		GutenbergRichterMagFreqDist mfd08 = getMFD(MIN_MAG, gm_j_7p2_7p7_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex],  1);

		GutenbergRichterMagFreqDist mfd09 = getMFD(MIN_MAG, gm_ab_6p6_7p1_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex], 1);
		GutenbergRichterMagFreqDist mfd10 = getMFD(MIN_MAG, gm_ab_6p8_7p3_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex], 1);
		GutenbergRichterMagFreqDist mfd11 = getMFD(MIN_MAG, gm_ab_7p0_7p5_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex], 1);
		GutenbergRichterMagFreqDist mfd12 = getMFD(MIN_MAG, gm_ab_7p2_7p7_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex], 1);

		GutenbergRichterMagFreqDist mfd13 = getMFD(MIN_MAG, gm_j_6p6_7p1_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex],  1);
		GutenbergRichterMagFreqDist mfd14 = getMFD(MIN_MAG, gm_j_6p8_7p3_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex],  1);
		GutenbergRichterMagFreqDist mfd15 = getMFD(MIN_MAG, gm_j_7p0_7p5_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex],  1);
		GutenbergRichterMagFreqDist mfd16 = getMFD(MIN_MAG, gm_j_7p2_7p7_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex],  1);
		*/
		
		if(allValuesZero)
			return null;
		else
			return mfdAtLoc;

	}
	
	/**
	 * This creates an NSHMP mag-freq distribution from their a-value etc, 
	 * @param minMag
	 * @param maxMag
	 * @param aValue
	 * @param bValue
	 * @param weight - rates get multiplied by this number
	 * @return
	 */		
	public GutenbergRichterMagFreqDist getMFD(double minMag, double maxMag, double aValue, double bValue, double weight) {
		
		System.out.println(minMag+"\t"+maxMag+"\t"+aValue+"\t"+bValue+"\t"+weight);

		minMag += DELTA_MAG/2;
		maxMag -= DELTA_MAG/2;
		int numMag = Math.round((float)((maxMag-minMag)/DELTA_MAG+1));
		GutenbergRichterMagFreqDist mfd = new GutenbergRichterMagFreqDist(minMag, numMag, DELTA_MAG, 1.0, bValue);
		mfd.scaleToIncrRate(minMag, aValue*Math.pow(10,-bValue*minMag));

		return mfd;
	}


		public void test() {
/*			
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, gm_ab_6p6_7p1_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex], 0.01667), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, gm_ab_6p8_7p3_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex], 0.0333), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, gm_ab_7p0_7p5_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex], 0.08333), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, gm_ab_7p2_7p7_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex], 0.0333), true);

			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, gm_j_6p6_7p1_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex],  0.01667), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, gm_j_6p8_7p3_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex],  0.0333), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, gm_j_7p0_7p5_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex],  0.08333), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, gm_j_7p2_7p7_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex],  0.0333), true);
*/
			for(int i=0; i<this.getNumGridLocs(); i++) {
				if(gm_ab_6p6_7p1_vals[i] == 0)
					if(adapt_cn_vals[i] >0)
						System.out.println(i);
			}
		}

		
		/**
		 * Get the random strike gridded source at a specified index
		 * 
		 * @param srcIndex
		 * @return
		 */
		public ProbEqkSource getRandomStrikeGriddedSource(int srcIndex, double duration) {
			SummedMagFreqDist mfdAtLoc = getTotMFD_atLoc(srcIndex);
			return new Point2Vert_FaultPoisSource(this.getGridLocation(srcIndex), mfdAtLoc, magLenRel, duration, ptSrcMagCutOff,
					fracStrikeSlip,fracNormal,fracReverse, false);
		}

		
		/**
		 * Get the the point source at a specified index
		 * 
		 * @param srcIndex
		 * @return
		 */
		public ProbEqkSource getPointGriddedSource(int srcIndex, double duration) {
			SummedMagFreqDist mfdAtLoc = getTotMFD_atLoc(srcIndex);
			double magCutoff = 10;
			return new Point2Vert_FaultPoisSource(this.getGridLocation(srcIndex), mfdAtLoc, magLenRel, duration, magCutoff,
					fracStrikeSlip,fracNormal,fracReverse, false);
		}

		/**
		 * Get Crosshair gridded source at a specified index
		 * 
		 * @param srcIndex
		 * @return
		 */
		public ProbEqkSource getCrosshairGriddedSource(int srcIndex, double duration) {
			boolean includeDeeps = false;
			//boolean includeDeeps = true;
			SummedMagFreqDist mfdAtLoc = getTotMFD_atLoc(srcIndex);
			return new Point2Vert_FaultPoisSource(this.getGridLocation(srcIndex), mfdAtLoc, magLenRel, duration, ptSrcMagCutOff,
					fracStrikeSlip,fracNormal,fracReverse, true);
		}


	public static void main(String args[]) {
		try {
			NSHMP_CEUS_SourceGenerator srcGen = new NSHMP_CEUS_SourceGenerator();
			
//			srcGen.test();
			
//			for(int i=0; i< srcGen.getNumGridLocs(); i++)
//				System.out.print(srcGen.getMaxMagAtLoc(i)+", ");
	
			/* */
			System.out.println("0\t"+srcGen.getMaxMagAtLoc(0));

			Location loc = new Location(50.0,-113.7);
			int index = srcGen.getNearestLocationIndex(loc);
			System.out.println(index+"\t"+srcGen.getMaxMagAtLoc(index));
			
			SummedMagFreqDist mfd = srcGen.getTotMFD_atLoc(index);
			System.out.println(mfd);
			
//			System.out.println(srcGen.getNumGridLats());
//			System.out.println(srcGen.getNumGridLons());
//			System.out.println(srcGen.getNumGridLocs());
//			System.out.println(srcGen.getGridLocation(0));
//			System.out.println(srcGen.getGridLocation(srcGen.getNumGridLocs()-1));
		}catch(Exception e) {
			e.printStackTrace();
		}

	}
}
